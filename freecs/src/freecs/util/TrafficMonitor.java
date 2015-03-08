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
package freecs.util;

import freecs.Server;

import java.util.Hashtable;
import java.util.Enumeration;
import java.net.InetAddress;


public class TrafficMonitor extends Thread {
	public static final TrafficMonitor tm = new TrafficMonitor();

	private Hashtable<InetAddress, AddressState> addr;
	private long checkInterval = 10000;

	private TrafficMonitor () {
		addr = new Hashtable<InetAddress, AddressState> ();
	}

	/**
	 * Starts up the TrafficMonitor
	 */
	public static void startTrafficMonitor () {
		if (!Server.srv.USE_TRAFFIC_MONITOR)
			return;
		if (tm.isAlive())
            return;
        tm.setName("TrafficMonitor");
        tm.start ();
	}

	/**
	 * mayPass get's called for every new connection-atempt. If the configured
	 * maximum number of connection-atempts per host is reached, this method will
	 * return false. The Thread calling this method is responsible for baning this
	 * host. There is the possibility to destinguish between normal hosts and proxy-servers.
	 * Proxy-Servers will be allowed to connect more often than normal hosts.
	 * @param ia the inet-address of the host to count the connection-atempts
	 * @return true if this inet-adresses Host may pass, false if too many connection-atempts from this host where made
	 * @see freecs.Server
	 */
	public AddressState mayPass (InetAddress ia) {
		AddressState as = (AddressState) addr.get(ia);
		if (as == null) {
			addr.put (ia, new AddressState ());
			StringBuffer sb = new StringBuffer (" add InetAdress (");
			sb.append (ia.toString ());
			sb.append (")");
			Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
			return null;
		}
		as.diff = System.currentTimeMillis () - as.lastCheck;
		if (as.diff > checkInterval) {
			as.reqCount = 1;
			as.lastCheck = System.currentTimeMillis ();
			return null;
		}
		as.reqCount++;
		if (as.isProxy) {
			if (as.reqCount > Server.srv.MAX_REQUESTS_PER_PROXY_IP) 
				return as;
		} else if (as.reqCount > Server.srv.MAX_REQUESTS_PER_IP) {
			       return as;
		}
		return null;
	}

	/**
	 * markAsProxy will mark this inet-adresses Host as proxy-server. This makes it
	 * possible to distiguish between normal- and proxy-hosts. Proxy-hosts well be
	 * allowed to make more connection-atempts than normal hosts.
	 * @param ia the inet-address which was identified as proxy-server
	 */
	public void markAsProxy (InetAddress ia) {
		if (!Server.srv.USE_TRAFFIC_MONITOR || Server.srv.isAdminHost(ia)) 
			return;			
		AddressState as;
		as = (AddressState) addr.get(ia);
		if (as == null) {
			StringBuffer sb = new StringBuffer ("markAsProxy: AddressState is null(");
			sb.append (ia.toString ());
			sb.append (")");
			Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
			return;
		}
		if (!as.isProxy) {
			StringBuffer sb = new StringBuffer ("TrafficMonitor.markAsProxy: identified a proxy (");
			sb.append (ia.toString ());
			sb.append (")");
			Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
			as.isProxy = true;
		}
	}

	/**
	 * the run-method of this TrafficMonitor is responsible for cleaning up inet-addresses 
	 * which didn't connect to this Server for mor than 60000 millis.
	 */
	public void run () {
        long lastMessage=0;
		while (Server.srv.isRunning ()) try {
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
			long now = System.currentTimeMillis ();
			long lowestValue = 60000;
			for (Enumeration e = addr.keys (); e.hasMoreElements (); ) {
				InetAddress tia = (InetAddress) e.nextElement ();
				if (tia==null)
					continue;
				AddressState as = (AddressState) addr.get (tia);
				if (as==null)
					continue;
				long diff = now - as.lastCheck;
				if (diff > 60000) {
					addr.remove (tia);
				} else if (diff < lowestValue) {
					lowestValue=diff;
				}
			}
            if (lowestValue < 33)
                lowestValue = 33;
			try {
				Thread.sleep (lowestValue);
			} catch (InterruptedException ie) {}
		} catch (Exception e) {
			Server.debug (this, "run:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
		}
	}

	public class AddressState {
        volatile long lastCheck;
        public volatile long reqCount;
        public volatile long diff;
		volatile boolean isProxy = false;
		private AddressState () {
            lastCheck = System.currentTimeMillis ();
            reqCount = 1;
		}
	}
    
    public String toString() {
        return "[TrafficMonitor]";
    }
}