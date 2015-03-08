/**
 * Copyright (C) 2003  Manfred Andres
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/**
 * The CentralSelector does the actual IO. All connections get Registered
 * and will be served when they are ready for action.
 * 
 * When a connection comes in, it get's registered with the CentralSelector.
 * If a connectino has content, the content will be automatically directed to
 *    one RequestReader's io-queue
 * If content must be written, the ConnectinoBuffer's write-queue attached to
 *    the connection will store it, untill the connection is ready for write-action
 */
package freecs.core;

import freecs.Server;
import freecs.util.ObjectBuffer;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CancelledKeyException;

public class CentralSelector extends Thread {
   public static boolean stopped  = false;
   public static final CentralSelector cSel = new CentralSelector();
   private Selector     sel      = null;
   private long         rqLastChecked, nextUnavailableMessage=0;
   public  ObjectBuffer dropKeys;
   
   public ObjectBuffer reqQueue = new ObjectBuffer (Server.srv.MAX_READERS*10);

   private CentralSelector () {
   	  dropKeys = new ObjectBuffer (10000);
      if (!initCsel ())
         Server.log (this, "construct: unable to init Csel", Server.MSG_ERROR, Server.LVL_HALT);
   }

   private boolean initCsel () {
		if (sel == null || !sel.isOpen ()) try {
			sel = SelectorProvider.provider ().openSelector ();
		} catch (IOException ioe) {
			Server.debug (this, "initCsel:", ioe, Server.MSG_ERROR, Server.LVL_HALT);
			return false;
		}
		if (sel != null && sel.isOpen ())
	 		return true;
		return false;
   }

	public static void startCentralSelector () {
        cSel.setName("CentralSelector");
		if (!cSel.isAlive())
			cSel.start();
		// cSel.setPriority(MAX_PRIORITY-2);
	}

   public int keyCount () {
      Set keys = sel.keys ();
      return keys.size ();
   }
   
	public void registerSC (SocketChannel sc, int reqType) throws IOException, ClosedChannelException {
		if (sc == null) return;
		sc.configureBlocking (false);
		ConnectionBuffer cb = new ConnectionBuffer (reqType);
        cb.setKey (sc.register (sel, SelectionKey.OP_READ, cb));
	}

    public void run () {
        Server.log (this, "starting up", Server.MSG_STATE, Server.LVL_MINOR);
        int sdc = 500;
        long lastMessage = 0;
        Thread katc = new Thread(new KeepAliveTimeoutChecker());
        katc.start();
        while (Server.srv.isRunning () || sel.keys().size() > 0) try {
            if (!Server.srv.isRunning ()) {
                sdc--;
                if (sdc <= 0) break;
            }
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart: known sockets=" + sel.keys().size(), Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
            while (!dropKeys.isEmpty()) {
                SelectionKey sc;
                synchronized (dropKeys) {
                    sc = (SelectionKey) dropKeys.pop();
                    dropKeys.notify();
                }
                implCloseChannel (sc);
            }
            long now = System.currentTimeMillis();
            try {
                if (sel.selectNow() < 1) {
                    try {
                        Thread.sleep (33);
                    } catch (InterruptedException ie) { }
                    continue;
                }
            } catch (Exception e) {
                Server.debug (this, "run (select):", e, Server.MSG_ERROR , Server.LVL_MAJOR);
            }
            Set keys = sel.selectedKeys();
            if (keys!=null && !keys.isEmpty()) {
                for (Iterator i = keys.iterator (); i.hasNext (); ) {
                    SelectionKey ck = (SelectionKey) i.next ();
                    i.remove();
                    try {
                        if (!CentralSelector.isSkValid(ck)) {
                            Server.log (this, "run: current key is invalid", Server.MSG_STATE, Server.LVL_VERBOSE);
                            continue;
                        }
                        if (ck.isReadable ()) {
                            readIn (ck);
                        }
                    } catch (CancelledKeyException cke) { }
                }
            }
            try {
                Thread.sleep (33);
            } catch (InterruptedException ie) { }
        } catch (Exception e) {
            Server.debug (this, "(outer loop): ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        katc.interrupt();
        if (sel != null) try {
            Server.log (this, "closing down", Server.MSG_ERROR, Server.LVL_MAJOR);
            sel.close ();
        } catch (Exception e) {
            Server.debug (this, "shutting down: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        Server.log (this, "suspended", Server.MSG_ERROR, Server.LVL_MINOR);
        stopped = true;
        // cSelList.remove (this);
        // Server.log (cSelList.size () + " CentralSelectors in cSelList", Server.MSG_STATE, Server.LVL_MINOR);
    }

	private void readIn (SelectionKey sk) {
        if (!CentralSelector.isSkValid(sk)) {
            Server.log (this, "readIn: current request has invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
            return;
        }
		ConnectionBuffer cb = (ConnectionBuffer) sk.attachment ();
        int bytesRead;
		try {
            synchronized (cb) {
                SocketChannel sc = (SocketChannel) sk.channel ();
                bytesRead = sc.read (cb.rBuf);
                if (bytesRead < 0) {
                    // Server.log ("CentralSelector.readIn: droped key", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    implCloseChannel (sk);
                    return;
                } else if (bytesRead == 0) {
                    Server.log (this, "readIn: no data from socket", Server.MSG_STATE, Server.LVL_VERBOSE);
                    return;
                }
                cb.updateKeepAliveTimeout();
                cb.currentRequest = cb.append();
                if (cb.currentRequest != null) {
                    addRequest(sk, cb);
                }
                return;
            }
		} catch (IOException ioe) {
			Server.debug (this, "readIn: droped key (IOException)", ioe, Server.MSG_ERROR, Server.LVL_VERY_VERBOSE);
            implCloseChannel (sk);
			cb.logError (ioe.getMessage());
		} catch (Exception e) {
			Server.debug (this, "readIn: Exception encountered while reading: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            implCloseChannel (sk);
			cb.logError (e.getMessage());
		}
    }
    
    public void addRequest(SelectionKey sk, ConnectionBuffer cb) {
        if (Server.srv.USE_CENTRAL_REQUESTQUEUE 
                && !this.addRequestToQueue (sk)) {
            implCloseChannel (sk);
            if (nextUnavailableMessage >= System.currentTimeMillis())
                return;
            cb.logError ("RequestQueue is full");
            Server.log (this, "readIn: RequestQueue is full", Server.MSG_ERROR, Server.LVL_MAJOR);
            nextUnavailableMessage += 1000;
        } else if (!Server.srv.USE_CENTRAL_REQUESTQUEUE
                && !RequestReader.processRequest(sk)) {
            implCloseChannel (sk);
            if (nextUnavailableMessage >= System.currentTimeMillis())
                return;
            cb.logError ("No available requestreader");
            Server.log (this, "readIn: No availabel requestreader to process request", Server.MSG_ERROR, Server.LVL_MAJOR);
            nextUnavailableMessage += 1000;
        }
    }

	private void implCloseChannel (SelectionKey sk) {
		try {
			ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
			if (cb != null) {
                cb.invalidate();
                User u = cb.getUser();
				if (u!=null && sk.equals(u.getKey()) && !u.isRemoving() && !u.isLoggedOut()) {
                    StringBuffer sb = new StringBuffer ("implCloseChannel: droped key for user ").append (u.getName ());
                    Server.log ("static CentralSelector", sb.toString (), Server.MSG_STATE, Server.LVL_VERBOSE);
    			    u.scheduleToRemove();
                }
            }
            SocketChannel sc = (SocketChannel) sk.channel();
            Responder.res.dropChannel(sc);
            synchronized (sc) {
                Socket s = sc.socket();
                s.close();
                sc.close();
            }
            sk.cancel();
		} catch (Exception e) {
			Server.debug (this, "closeChannel: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            sk.cancel();
		}
	}

	public static void dropKey (SelectionKey sk) {
		if (sk == null) return;
		ConnectionBuffer cb = (ConnectionBuffer) sk.attachment ();
		if (cb != null) {
            cb.invalidate();
		}
        addToDropKeys (sk);
	}

	public static void dropChannel (SocketChannel sc) {
		SelectionKey sk = sc.keyFor(cSel.sel);
		if (sk == null) {
            try {
                sc.close();
            } catch (IOException e) {
                Server.debug ("static CentralSelector", "dropChannle:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            }
            return;
        }
        ConnectionBuffer cb = (ConnectionBuffer) sk.attachment ();
        if (cb != null) {
            cb.invalidate();
        }
        addToDropKeys (sk);
	}


    private static void addToDropKeys (SelectionKey sk) {
        long now = System.currentTimeMillis();
        long stop = now + 5000;
        synchronized (cSel.dropKeys) {
            boolean success=cSel.dropKeys.put(sk);
            while (!success && stop > now) {
                try {
                    now = System.currentTimeMillis();
                    long waitTime = stop - now;
                    if (waitTime > 32)
                        cSel.dropKeys.wait(stop - now);
                } catch (InterruptedException ie) { }
                success=cSel.dropKeys.put(sk);
            }
            if (!success)
                Server.log("static CentralSelector", "dropKey: unable to add dropkey", Server.MSG_ERROR, Server.LVL_MAJOR);
            cSel.dropKeys.notify();
        }
    }

    public static boolean isSkValid (SelectionKey sk) {
        if (!chkSk(sk)) {
            if (sk != null && cSel.equals(sk.selector()))
                dropKey (sk);
            return false;
        }
        return true;
    }
    
    private static boolean chkSk (SelectionKey sk) {
        if (sk == null)
            return false;
        try {
            ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
            if (cb == null || !cb.isValid())
                return false;
            if (!sk.isValid() || !sk.channel().isOpen()) {
                cb.invalidate();
                return false;
            }
            Socket s = ((SocketChannel) sk.channel()).socket();
            if (s.isInputShutdown() || s.isOutputShutdown()) {
                cb.invalidate();
                return false;
            }
            if (cb != null) {
                if (!cb.isValid())
                    return false;
            }
        } catch (Exception e) {
            Server.debug ("static CentralSelector", "SelectionKey-Check:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            return false;
        }
        return true;
    }
    
    private boolean addRequestToQueue(SelectionKey sk) {
        long stop = System.currentTimeMillis() + 1000;
        boolean success=false;
        try {
            synchronized (this.reqQueue) {
                if (reqQueue.contains(sk))
                    return true;
                success = reqQueue.put(sk);
                while (!success
                        && stop > System.currentTimeMillis()) { 
                    this.reqQueue.wait(stop - System.currentTimeMillis());
                    success = reqQueue.put(sk);
                }
                if (success)
                    this.reqQueue.notify();
            }
        } catch (Exception e) {
            Server.debug (this, "addRequestToQueue caused exception:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        if (reqQueue.size() > ((this.reqQueue.capacity()/1.5)/RequestReader.activeReaders()))
            RequestReader.startRequestReader(false);
        return success;
    }

    public boolean equals (Object o) {
        return o instanceof Selector && o.equals(sel);
    }

    public String toString() {
        return "[CentralSelector]";
    }
    
    private class KeepAliveTimeoutChecker implements Runnable {
        private short loglvl = Server.LVL_VERY_VERBOSE;
        KeepAliveTimeoutChecker() { }
        public void run () {
            long nextCheck = 0;
            while (Server.srv.isRunning()) {
                long now = System.currentTimeMillis();
                if (nextCheck>now) {
                    long diff = Math.max(nextCheck - now, 33);
                    Server.log("KeepAliveCheck", "sleeping for " + diff + " millis", Server.MSG_STATE, loglvl);
                    try {
                        Thread.sleep(diff);
                    } catch (InterruptedException ie) { /* ok */ }
                    now = System.currentTimeMillis();
                }
                nextCheck = now + Server.srv.KEEP_ALIVE_TIMEOUT;
                SelectionKey[] checkArr;
                Server.log("KeepAliveCheck", "sync on selector", Server.MSG_STATE, loglvl);
                synchronized (CentralSelector.cSel.sel) {
                    if (!CentralSelector.cSel.sel.isOpen()) {
                        Server.log ("KeepAliveTimeoutChecker", "Selector closed. Shutting down KeepAliveTimeoutChecker", Server.MSG_STATE, Server.LVL_MINOR);
                        return;
                    }
                    Set keyset = CentralSelector.cSel.sel.keys();
                    Server.log("KeepAliveCheck", "sync on selectors keyset", Server.MSG_STATE, loglvl);
                    synchronized (keyset) {
                        checkArr = (SelectionKey[]) keyset.toArray(new SelectionKey[0]);
                    }
                }
                Server.log("KeepAliveCheck", "processing " + checkArr.length + "keys", Server.MSG_STATE, loglvl);
                for (int i = 0; i < checkArr.length; i++) {
                    SelectionKey sk = checkArr[i];
                    if (!sk.isValid() || !sk.channel().isOpen())
                        continue;
                    ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
                    synchronized (cb) {
                        long kato = cb.getKeepAliveTimeout(now);
                        if (kato < 0) // no timeout
                            continue;
                        if (kato <= now) {
                            Server.log("KeepAliveCheck", "closing connection to " + cb.conn, Server.MSG_STATE, loglvl);
                            CentralSelector.dropKey(sk);
                        } else if (kato < nextCheck)
                            nextCheck = kato;
                    }
                }
                Server.log("KeepAliveCheck", "checking took me " + (System.currentTimeMillis()-now) + " millis", Server.MSG_STATE, loglvl);
            }
        }
    }
}
