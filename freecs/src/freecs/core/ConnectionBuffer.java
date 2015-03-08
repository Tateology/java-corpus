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
package freecs.core;

import freecs.*;
import freecs.content.*;
import freecs.interfaces.*;
import freecs.util.ObjectBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import freecs.util.TrafficMonitor;
import java.net.InetAddress;

/**
 * gets attached to the keys reading from a nonblocking channel
 * stores the raw request in a buffer. if the request is finished, parse gets
 * called which in turn decides which requestobject to use for this requst
 * and suplies this RequestObject to the next available RequestEvaluator
 */
public class ConnectionBuffer {
   private volatile User u;
   private int src;
   private ByteBuffer buf;
   private ByteBuffer tBuf = null;
   public  ByteBuffer rBuf = ByteBuffer.allocate (Server.srv.READBUFFER_SIZE);
   private SelectionKey sk;
   private String ts;
   private ObjectBuffer writeBuffer = new ObjectBuffer (Server.srv.INITIAL_RESPONSE_QUEUE);
   private volatile boolean valid=true;
   public Connection conn;

   private StringBuffer lsb = new StringBuffer();
   
   private static final int GET = 1;
   private static final int POST= 2;

   private int reqType = 0;
   private int so = 0;
   private int cStart   = -1;
   private int cLength  = -1;
   public volatile IRequest currentRequest;
   private boolean reading=false;
   
    private volatile long closeWhen=System.currentTimeMillis() + Server.srv.KEEP_ALIVE_TIMEOUT;


   public ConnectionBuffer (int src) {
      this.src = src;
      buf = ByteBuffer.allocate(Server.srv.READBUFFER_SIZE);
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

	/**
	 * appends to the incomplete request and checks if it has completed
	 * if the request is complete, it will be returned. NULL will be returned
	 * on the other hand.
	 * FIXME: has to get more modular to support different protocols
	 * @return IRequst The full request || null if incomplete
	 */
    public IRequest append () throws Exception {
        boolean parse = false;
        synchronized (this) {
            reading=true;
            rBuf.flip();
            if (this.buf.remaining () < rBuf.remaining ()) {
                ByteBuffer tbuf = ByteBuffer.allocate (this.buf.position () + rBuf.remaining ());
                this.buf.flip ();
                tbuf.put(this.buf);
                this.buf = tbuf;
            }
            this.buf.put(rBuf);
            rBuf.clear ();
            if (reqType == 0 && this.buf.position () > 4) {
                if (this.buf.get(0) == 'P' && this.buf.get(1) == 'O' && this.buf.get(2) == 'S' && this.buf.get(3) == 'T') {
                    reqType = POST;
                } else if (this.buf.get(0) == 'G' && this.buf.get(1) == 'E' && this.buf.get(2) == 'T') {
                    reqType = GET;
                } else {
                    this.addLog("HEADER-INVALID");
                    this.invalidate();
                    reading=false;
                    return null;
                }
            }
            if (reqType == GET) {
                if (this.buf.position() > 4096) {
                    this.addLog("HEADER>4096bytes");
                    this.invalidate();
                    reading=false;
                    return null;
                }
                if (this.buf.position () > 10 
                    && this.buf.get (this.buf.position () - 4) == '\r'
                    && this.buf.get (this.buf.position () - 3) == '\n'
                    && this.buf.get (this.buf.position () - 2) == '\r'
                    && this.buf.get (this.buf.position () - 1) == '\n') {
                        parse = true;
                }
            } else if (reqType == POST) {
                if (cLength == -1) {
                    for (; so < this.buf.position () - 15; so++) {
                        if (so > 4096 
                            || (this.buf.get(so)   == '\r' 
                                && this.buf.get(so+1) == '\n' 
                                && this.buf.get(so+2) == '\r'
                                && this.buf.get(so+3) == '\n')) {
                            this.addLog("HEADER-INVALID");
                            this.invalidate();
                            reading=false;
                            return null;
                        }
                        if (this.buf.get(so) == 'C' && this.buf.get(so+1) == 'o' 
                                && this.buf.get(so+2) == 'n' && this.buf.get(so+3) == 't' 
                                && this.buf.get(so+4) == 'e' && this.buf.get(so+5) == 'n' 
                                && this.buf.get(so+6) == 't' && this.buf.get(so+7) == '-' 
                                && (this.buf.get(so+8) == 'L' || this.buf.get(so+8) == 'l')
                                && this.buf.get(so+9) == 'e' && this.buf.get(so+10) == 'n' 
                                && this.buf.get(so+11) == 'g' && this.buf.get(so+12) == 't' 
                                && this.buf.get(so+13) == 'h' && this.buf.get(so+14) == ':') {
                            int cso = so + 14;
                            if (cso >= this.buf.capacity ()) return null;
                            while ((this.buf.get(cso) < 48 || this.buf.get(cso) > 57)) {
                                if (cso >= this.buf.capacity ()) return null;
                                cso++;
                            }
                            StringBuffer sb = new StringBuffer ();
                            while (this.buf.get(cso) >= 48 && this.buf.get(cso) <= 57) {
                                if (cso >= this.buf.capacity ()) return null;
                                sb.append ((char) this.buf.get(cso));
                                cso++;
                            }
                            so = cso;
                            cLength = Integer.parseInt (sb.toString ());
                            break;
                        }
                    }
                }
                if (cLength != -1) {
                    for (; cStart == -1 && so < this.buf.position () - 4; so++) {
                        if (so > 4096) {
                            this.addLog("HEADER>4096bytes");
                            this.invalidate();
                            reading=false;
                            return null;
                        }
                        if (this.buf.get(so)   == '\r' 
                                && this.buf.get(so+1) == '\n' 
                                && this.buf.get(so+2) == '\r'
                                && this.buf.get(so+3) == '\n') {
                            cStart = so + 4;
                            break;
                        }
                    }
                    if (cStart != -1) {
                        if ((this.buf.position () - cStart) > cLength) {
                            int diff = this.buf.position () - cStart - cLength;
                            tBuf = ByteBuffer.allocate (diff);
                            for (int pos = this.buf.position () - diff; pos < this.buf.position (); pos++) {
                                tBuf.put (this.buf.get (pos));
                            }
                            this.buf.position(cStart + cLength);
                            parse=true;
                        } else if ((this.buf.position () - cStart) == cLength) {
                            parse=true;
                        }
                    }
                }
            }
        }
        if (parse) 
            return parse();
        return null;
    }

	/**
	 * hands over this buffer to the requestparser-threads which take care of parsing the request
	 * @return IRequest The IRequest-object containing the request
	 */
    public IRequest parse () throws Exception {
        // FIXME: when we install another protocol we have to check here for the type of protocol
        IRequest req = null;
        synchronized (this) {
            this.buf.flip ();
            try {
                req = new HTTPRequest(buf, this);
            } catch (Exception e) {
                reset();
                throw e;
            }
            reading=false;
        }
        try {            
            req.parse ();
            Connection conn = req.getConnectionObject();
            if (!conn.isDirectlyConnected) {
                InetAddress ia = ((SocketChannel) sk.channel ()).socket().getInetAddress ();
                if (ia != null) {
                    TrafficMonitor.tm.markAsProxy (ia);
                }
            }
/*        } catch (Exception e) {
            Server.debug (this, "parse: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            throw e; */
        } finally {
            reset ();
        }
        return req;
    }

	private synchronized void reset () {
      if (buf.capacity () != Server.srv.READBUFFER_SIZE) {
         buf = ByteBuffer.allocate (Server.srv.READBUFFER_SIZE);
      } else {
         buf.clear ();
      }
      if (tBuf != null) {
         buf.put (tBuf);
         tBuf = null;
      }
      cStart = -1;
      cLength= -1;
      reqType= 0;
      so = 0;
      valid=true;
      reading=false;
   }

   public void setTemplateSet (String ts) {
      this.ts = ts;
   }

   public String getTemplateSet () {
      return ts;
   }

   public void setUser (User u) {
      this.u = u;
   }

   public User getUser () {
      return u;
   }

    /**
    * returns the SocketChannel of this requestbuffer
    */
   public SelectionKey getKey () {
      return sk;
   }
	
	public void setKey (SelectionKey sk) {
        if (!CentralSelector.isSkValid(sk)) {
            Server.log(this, "setKey: tryed to set invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
            return;
        }
		this.sk=sk;
		conn = new Connection (sk);
	}

	public void addToWrite (Object ic) {
        if (!CentralSelector.isSkValid(sk)) {
            Server.log (this, "addToWrite: selection-key isn't valid anymore", Server.MSG_STATE, Server.LVL_VERBOSE);
            return;
        }
        synchronized (this) {
            if (writeBuffer.isFull ()) {
                int newSize = writeBuffer.capacity () + Server.srv.INITIAL_RESPONSE_QUEUE; 
                if (newSize > Server.srv.MAX_RESPONSE_QUEUE) {
                    Server.log(this, "addToWrite: write-queue would be bigger than specified for " + toString(), Server.MSG_STATE, Server.LVL_MINOR);
                    return;
                }
                Server.log(this, "addToWrite: Expanding write-queue for " + toString(), Server.MSG_STATE, Server.LVL_MINOR);
                writeBuffer.resizeTo(newSize);
            }
            writeBuffer.put(ic);
        }
        writeToLog();
		Responder.res.addToWrite((SocketChannel) sk.channel(), this);
	}

	public ObjectBuffer getWriteQueue () {
		return writeBuffer;
	}

	public void updateKeepAliveTimeout () {
		if (isMessageFrame)
			return;
		closeWhen = System.currentTimeMillis() + Server.srv.KEEP_ALIVE_TIMEOUT;
	}
    
    public long getKeepAliveTimeout(long ts) {
        if (isMessageFrame || reading)
            return -1;
        return closeWhen;
    }
	
	public void invalidate() {
		valid=false;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	private volatile boolean isMessageFrame = false;
	public void setIsMessageFrame(boolean b) {
		// Server.log("changed state to message-frame-state", Server.MSG_STATE, Server.LVL_MAJOR);
		isMessageFrame=b;
	}
    
    public void addLog (String str) {
        lsb.append (" ");
        lsb.append (str);
    }

    public void writeToLog () {
        if (lsb.length() < 1)
            return;
        if (conn!=null && conn.peerAddress != null)
            lsb.insert(0, conn.peerAddress.getHostAddress());
        else if (conn != null)
            lsb.insert(0, conn.toString());
        else
            lsb.insert(0, "undefined");
        Server.log ("OK", lsb.toString (), Server.MSG_TRAFFIC, Server.LVL_MINOR);
        lsb = new StringBuffer();
    }

    public void logError (String reason) {
        lsb.append (" REASON: ");
        lsb.append (reason);
        if (conn != null && conn.peerAddress != null)
            lsb.insert (0, conn.peerAddress.getHostAddress());
        else if (conn != null)
            lsb.insert (0, conn.toString());
        else
            lsb.insert (0, "undefined");
        Server.log ("FAILED", lsb.toString (), Server.MSG_TRAFFIC, Server.LVL_MAJOR);
        lsb = new StringBuffer();
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}