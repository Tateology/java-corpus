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
import freecs.util.ObjectBuffer;
import freecs.Server;
import java.nio.channels.SelectionKey;

public class RequestQueue {
   private long 	lastCheck;
   private float  lastPerc = 0;
   private RequestReader req;

   private ObjectBuffer requests;

   public RequestQueue (RequestReader r) {
      requests = new ObjectBuffer (Server.srv.READER_MAX_QUEUE);
      req=r;
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public double getUsage () {
      double onePerc = ((double) requests.capacity ()) / ((double) 100);
      return ((double) requests.size()) / onePerc;
   }

   public boolean addKey (SelectionKey sk) {
		if (!CentralSelector.isSkValid(sk)) {
            Server.log(this, "addKey: tryed to add invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
			return true;
        }
		int i = 0;
		synchronized (requests) {
			while (!requests.put (sk)) {
				if (i > 5) {
					Server.log (this, "addKey: Tried 5 times to add key.", Server.MSG_STATE, Server.LVL_MAJOR);
                    requests.notify();
					return false;
				}
				try {
					requests.wait (200);
				} catch (InterruptedException ie) { }
				i++;
			}
			requests.notify();
		}
		return true;
	}
   
	public int size () {
		return requests.size();
	}

    public SelectionKey getKey (long timeout) {
        long returnTime = System.currentTimeMillis () + timeout;
        SelectionKey sk;
        synchronized (requests) {
            sk = (SelectionKey) requests.get ();
            while (sk == null && System.currentTimeMillis () < returnTime) {
                try {
                    long diff = returnTime - System.currentTimeMillis();
                    if (diff > 0)
                        requests.wait (diff);
                } catch (InterruptedException ie) { }
                sk = (SelectionKey) requests.get ();
            }
            requests.notify();
        }
        return sk;
    }
   
    public SelectionKey popKey (long timeout) {
        synchronized (requests) {
            SelectionKey rk = this.getKey(timeout);
            if (rk == null) 
                return null;
            if (!requests.isEmpty())
                requests.pop();
            requests.notify();
            return rk;
        }
    }
    
    public String toString() { return "[RequestQueue]"; }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}