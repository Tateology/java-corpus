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
 * 
 * Created on 22.10.2003
 */

package freecs.core;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import freecs.Server;
import freecs.interfaces.IContainer;
import freecs.util.ObjectBuffer;

/**
 * @author Manfred Andres
 *
 * freecs.core
 */
public class Responder extends Thread {
	public static final Responder res = new Responder();
    public static final Object CLOSE_CONNECTION = new Object();
    public static final Object CLOSE_CONNECTION_IGNORE = new Object();
	private Selector sel;
	
	private Responder() {
		try {
			sel = SelectorProvider.provider().openSelector();
		} catch (Exception e) {
			Server.debug (this, "Unable to start Responder!", e, Server.MSG_ERROR, Server.LVL_HALT);
		}
	}
	
	public static void startResponder() {
		if (!res.isAlive()) {
            res.setName("Responder");
			res.start();
        }
		res.setPriority(MAX_PRIORITY);
	}

	public void addToWrite(SocketChannel sc, ConnectionBuffer cb) {
		if (!cb.isValid())
			return;
		SelectionKey sk = sc.keyFor(res.sel);
        if (sk!=null && !CentralSelector.isSkValid(sk)) {
            Server.log(this, "addToWrite: tryed to add invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
            return;
        }
		if (sk == null) try {
			sc.register(sel, SelectionKey.OP_WRITE, cb);
		} catch (ClosedChannelException cce) {
			Server.debug (this, "addToWrite: Channel closed", cce, Server.MSG_ERROR, Server.LVL_VERY_VERBOSE);
			dropKey(sk);
		}
	}
	
	public void run() {
        long lastMessage = 0;
		while (Server.srv.isRunning() || CentralSelector.cSel.isAlive()) try {
           // long now = System.currentTimeMillis();
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
            int readKeys=0;
			try {
                readKeys = sel.selectNow(); 
            } catch (CancelledKeyException cke) {
                Server.log(this, "CancelledKeyException while selectNow()", Server.MSG_ERROR, Server.LVL_VERBOSE);
			} catch (IOException ioe) {
				Server.debug(this, "run: ", ioe, Server.MSG_ERROR, Server.LVL_VERBOSE);
			} catch (Exception e) {
				Server.debug(this, "run: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
			}
            if (readKeys < 1) {
                try {
                    Thread.sleep(33);
                } catch (InterruptedException ie) { }
                continue;
            }
			Set<SelectionKey> ks = sel.selectedKeys();
			if (ks!=null && !ks.isEmpty()) {
				for (Iterator<SelectionKey> i = ks.iterator(); i.hasNext(); ) {
					SelectionKey ck = (SelectionKey) i.next();
					i.remove();
                    if (!CentralSelector.isSkValid(ck)) {
                        Server.log (this, "run: current request has invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
                        dropKey(ck);
                        continue;
                    }
					ConnectionBuffer cb = (ConnectionBuffer) ck.attachment();
                    SocketChannel sc = (SocketChannel) ck.channel();
                    ObjectBuffer ob = cb.getWriteQueue();
                    Object o;
                    while (true) {
                        synchronized (ob) {
                            if (ob.isEmpty())
                                break;
                            o = ob.get();
                            if (o==null) {
                                // should not happen
                                Server.log(this, "WriteQueue contained null", Server.MSG_ERROR, Server.LVL_VERBOSE);                               
                                //ob.pop();
                                try {
                                    cb.addToWrite(CLOSE_CONNECTION);
                                } catch(Exception e){
                                    Server.log(this, "WriteQueue contained null close Conection "+e, Server.MSG_ERROR, Server.LVL_VERBOSE);                               
                                }
                                if (!ck.isValid())
                                    Server.log(this, "WriteQueue contained null-Connection is no more valid", Server.MSG_ERROR, Server.LVL_VERBOSE);                               
                                try {
                                    dropKey(ck);
                                } catch(Exception dk){
                                    Server.log(this, "WriteQueue contained null drop Key "+dk, Server.MSG_ERROR, Server.LVL_VERBOSE);                               
                                }

                                ob=null;
                                break;
                            }
                        }
                        if (o==CLOSE_CONNECTION_IGNORE) {
                            ck.cancel();
                            ck.channel().close();
                            break;
                        }
                        if (o==CLOSE_CONNECTION) {
                            dropKey(ck);
                            break;
                        }
                        synchronized (cb) {
                            cb.updateKeepAliveTimeout();
                        }
                        ByteBuffer bb = o instanceof ByteBuffer ? (ByteBuffer) o : ((IContainer) o).getByteBuffer();
                        if (!writeContent(sc, bb))
                            break;
                        if (o instanceof IContainer && ((IContainer) o).closeSocket()) {
                            dropKey(ck);
                            break;
                        }
                        synchronized (ob) {
                            ob.pop();
                        }
                    }
				}
			}
			try {
				Thread.sleep(33);
			} catch (InterruptedException ie) { }
		} catch (Exception e) {
			Server.debug (this, "run: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
		}
	}
	
    private void dropKey (SelectionKey sk) {
        if (sk == null) 
            return;
        synchronized (sk) {
            sk.cancel();
        }
        SocketChannel sc = (SocketChannel) sk.channel();
        CentralSelector.dropChannel(sc);
	}
	
	public void dropChannel (SocketChannel sc) {
		SelectionKey sk = sc.keyFor(sel);
		if (sk != null) synchronized (sk) {
			sk.cancel();
        }
	}
	
	/**
	 * writes out the content. if there is nothing more to write in this bytebuffer or
	 * the channel isn't open anymore, true will be returned, false otherwise
	 * @param sc
	 * @param bb
	 * @return
	 * @throws IOException
	 */
	private boolean writeContent (SocketChannel sc, ByteBuffer bb) {
	   int written = 0;
	   try {
		  written = sc.write (bb);
	   } catch (IOException ioe) {
		 // is thrown if the connection was closed from remote host...
		 // do nothing than...
		 Server.log(this, "writeContent: remote host has closed connection", Server.MSG_TRAFFIC, Server.LVL_VERY_VERBOSE);
         CentralSelector.dropChannel(sc);
		 return true;
	   } catch (BufferOverflowException boe) {
         Server.log(this, "writeContent: bufferoverflowexception which should not apear acording to apidoc of sun...", Server.MSG_STATE, Server.LVL_MAJOR);   
       }
	   if (written < 0) {
		 return true;
	   }
	   if (bb.remaining () > 0)
		  return false;
	   return true;
	}
    
    public String toString() {
        return "[Responder]";
    }
}
