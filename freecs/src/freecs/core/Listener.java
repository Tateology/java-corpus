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
 * Created on 18.10.2003
 */

package freecs.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import freecs.Server;
import freecs.util.TrafficMonitor;
import freecs.util.TrafficMonitor.AddressState;

/**
 * @author Manfred Andres
 *
 * freecs.core
 */
public class Listener extends Thread {
    private HashMap<InetAddress, ServerSocketChannel> ia2ssc = null;
	private static final Listener l = new Listener();
	private Selector sel;
// 	private ServerSocketChannel ssc;
	
	public Listener() {
		try {
			sel = SelectorProvider.provider().openSelector();
		} catch (Exception e) {
			Server.debug (this, "Unable to start Listener!", e, Server.MSG_ERROR, Server.LVL_HALT);
		}
	}
    
    public static void updateSscRecieveBuffer(int rbw) {
        if (l.ia2ssc==null)
            return;
        for (Iterator<ServerSocketChannel> i = l.ia2ssc.values().iterator(); i.hasNext(); ) {
            try {
                ServerSocketChannel ssc = (ServerSocketChannel) i.next();
                if (ssc.socket().getReceiveBufferSize() == rbw)
                    continue;
                ssc.socket().setReceiveBufferSize(rbw);
            } catch (IOException ioe) {
                Server.debug("static Listener", "updateSscRecieveBuffer: exception during updating recievebuffer-window",ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
            }
        }
    }
	
	public static void startListener() throws IOException {
		if (l.ia2ssc==null)
			l.initSSC ();
		if (!l.isAlive()) {
            l.setName("Listener");
			// l.setPriority(MAX_PRIORITY-3);
			l.start();
		}
	}

    private void bindSSC (InetAddress ia) throws IOException {
        if (ia2ssc.get(ia)!=null)
            return;
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ServerSocket ssoc = ssc.socket();
        ssoc.setReceiveBufferSize(Server.srv.TCP_RECEIVE_BUFFER_WINDOW);
        ssoc.setReuseAddress(true);
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress (ia, Integer.parseInt(Server.srv.getProperty("port"))));
        if (Server.srv.allowedLoginHosts == null)
            Server.srv.allowedLoginHosts = new Vector<InetAddress>();
        Server.srv.allowedLoginHosts.addElement (ia);
        ssc.register(sel, SelectionKey.OP_ACCEPT);
        ia2ssc.put(ia, ssc);
    }
    
    private void reinitSSC () throws IOException {
        if (ia2ssc != null) {
            for (Iterator<ServerSocketChannel> i = ia2ssc.values().iterator(); i.hasNext(); ) {
                ServerSocketChannel ssc = (ServerSocketChannel) i.next();
                ssc.close();
            }
        }
        initSSC();
    }

    private void initSSC () {
        if (ia2ssc == null)
            ia2ssc = new HashMap<InetAddress, ServerSocketChannel>();
        try {
            if (Server.srv.getProperty("bindIp")!=null) {
                String addresses[] = Server.srv.getProperty("bindIp").split(",");
                for (int i = 0; i < addresses.length; i++) {
                    bindSSC(InetAddress.getByName(addresses[i]));
                }
            } else {
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                    NetworkInterface ni = (NetworkInterface) e.nextElement();
                    for (Enumeration<InetAddress> ee = ni.getInetAddresses(); ee.hasMoreElements(); )
                        bindSSC((InetAddress) ee.nextElement());
                }
            }
        } catch (IOException ioe) {
            Server.debug(this, "initSSC: exception during obtaining ip-adresses", ioe, Server.MSG_ERROR, Server.LVL_HALT);
        }
    }
    
	public void run () {
        long lastMessage = 0;
		while (Server.srv.isRunning()) {
			try {
                if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                    Server.log ("[Listener]", "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    lastMessage = System.currentTimeMillis();
                }
				while (sel.selectNow() < 1) try {
					Thread.sleep(100);
				} catch (InterruptedException ie) { }
			} catch (IOException ioe) {
				Server.debug (this, "run: ", ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
				try {
                    reinitSSC();
				} catch (IOException iioe) {
					Server.debug (this, "MAJOR ERROR ON REOPENING LISTENER!", iioe, Server.MSG_ERROR, Server.LVL_MAJOR);
					break;
				}
			} catch (Exception e) {
				Server.debug (this, "run: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
			}
			for (Iterator<SelectionKey> i = sel.selectedKeys().iterator(); i.hasNext(); ) try {
				SelectionKey ck = (SelectionKey) i.next();
				i.remove();
				if (ck.isAcceptable()) {
					accept(ck);
				} else {
					Server.log (this, ".run: SelectionKey doesn't have Accept in its interestOps! " + ck.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
				}
			} catch (CancelledKeyException cke) { }
            try {
                Thread.sleep(33);
            } catch (InterruptedException ie) {}
		}
		for (Iterator<SelectionKey> i = sel.keys().iterator(); i.hasNext(); ) {
			SelectionKey ck = (SelectionKey) i.next();
			try {
				ck.channel().close();
				ck.cancel();
				//i.remove();
			} catch (Exception e) {
				Server.debug (this, "final cleanup: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
			}
		}
	}

	private void accept (SelectionKey sk) {
		if (sk == null) return;
		ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
		SocketChannel sc;
		try {
			sc = ssc.accept();
			if (sc == null) 
				return;
			InetAddress ia = sc.socket ().getInetAddress ();
		   // check if this host is banned for the listener
		   // FIXME: This is considered ALPHA: 
		   // the traffic-monitor does the banning, this is untested
		   if (Server.srv.USE_TRAFFIC_MONITOR && !Server.srv.isAdminHost(ia)) {
				   if (Server.srv.isTrafficBanned (ia)){
				      sc.close();
				      return;
				   }
			
               AddressState as = TrafficMonitor.tm.mayPass (ia);
			   if (as!=null) {
                   sc.close();
                   StringBuffer tsb = new StringBuffer ("TrafficMonitor is refusing connection to banned host: ");
                   tsb.append (ia.getHostAddress());
                   tsb.append ("(");
                   tsb.append (ia.getHostName());
                   tsb.append (")");
                   Server.log (this, tsb.toString (), Server.MSG_TRAFFIC, Server.LVL_MAJOR);
                   tsb = new StringBuffer("reached ");
                   tsb.append (as.reqCount);
                   tsb.append (" connects within ");
                   tsb.append (as.diff);
                   tsb.append (" millis");
                   Server.srv.banHost (ia, System.currentTimeMillis() + Server.srv.HOST_BAN_DURATION, tsb.toString());
                   return;
			   }
			}
			CentralSelector.cSel.registerSC (sc, Server.REQUEST_TYPE_HTTP);
		} catch (Exception e) {
			Server.debug (this, "accept: Exception encountered during accept: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
		}
	}

    public String toString() { return "[Listener]"; }
}
