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
 * The Connection-class is used to store connection-specific data
 * only once. 
 */
package freecs.content;

import freecs.Server;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;

public class Connection {
	// this is the remote-address of the socket-connection
	public InetAddress		peerAddress = null;
	public String 			peerIp = null;
	
	// this represents the real-client-ip if it has been identified
	public String 			clientIp=null;
	public InetAddress		clientAddress=null;
	
	// this is the proxy forward-chain (from the xForwardedFor headerfield)
	public String[]			fwChain = null;
	
	// true if the client has a direct-socket-connectino
	public boolean			isDirectlyConnected=false;
	
	public Connection (SelectionKey sk) {
		peerAddress 	= ((SocketChannel) sk.channel()).socket().getInetAddress();
		peerIp 		= peerAddress.getHostAddress();
		clientAddress 	= null;
		clientIp 			= null;
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
	
	public Connection (SelectionKey sk, String[] fwChain, boolean idc) {
		isDirectlyConnected 	= idc;
		peerAddress 			= ((SocketChannel) sk.channel()).socket().getInetAddress();
		if (peerAddress != null)
			peerIp			= peerAddress.getHostAddress();
		if (fwChain != null) {
			isDirectlyConnected 	= false;
			this.fwChain 			= fwChain;
			if (fwChain[0].indexOf(".") > -1) try {
				clientAddress = InetAddress.getByName(fwChain[0]);
				if (clientAddress != null)
					clientIp 		= clientAddress.getHostAddress();
			} catch (UnknownHostException uhe) {
				Server.debug (this, "Unable to determine real IP for " + fwChain[0], uhe, Server.MSG_STATE, Server.LVL_MINOR);
			}
			return;
		} else if (!idc) {
			return;
		}
		clientAddress = peerAddress;
		if (peerAddress != null)
			clientIp 		= clientAddress.getHostAddress();
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}

	private volatile String toStringVal = null;	
	public String toString () {
		if (toStringVal == null) {
			StringBuffer sb = new StringBuffer ();
			if (!isDirectlyConnected)
				sb.append ("proxy=");
			sb.append (peerIp);
			if (!isDirectlyConnected && clientIp != null) {
				sb.append (" clientIp=");
				sb.append (clientIp);
			}
			toStringVal=sb.toString();
		}
		return toStringVal;
	}
    
    public boolean isBanable () {
        if (this.clientIp != null)
            return true;
        if (fwChain!=null && fwChain.length > 0 
                && fwChain[0]!=null && fwChain[0].length() > 0)
            return true;
        return false;
    }
    
    public boolean hasAnoProxy () {
        if (this.clientIp != null)
            return false;
        if (fwChain!=null && fwChain.length > 0 
                && fwChain[0]!=null && fwChain[0].length() > 0)
            return false;
        return true;
    }
    
    public boolean equals (Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Connection))
            return false;
        Connection c = (Connection) o;
        if (this.clientIp == null && c.clientIp != null)
            return false;
        if (this.clientIp != null && !this.clientIp.equals(c.clientIp))
            return false;
        if (this.peerIp == null && c.peerIp != null)
            return false;
        if (this.peerIp != null && !this.peerIp.equals(c.peerIp))
            return false;
        if (this.fwChain == null && c.fwChain != null)
            return false;
        if (this.fwChain != null && !this.fwChain.equals(c.fwChain))
            return false;
        return true;
    }

    public int hashCode () {
        if (this.clientIp!=null && this.peerIp != null) {
            return (this.clientIp + "/" + this.peerIp).hashCode();
        } else if (this.peerIp!=null) {
            return this.peerIp.hashCode();
        } else {
            return this.clientIp.hashCode();
        }
    }
    
    public String getBanKey() {
        if (this.clientIp!=null)
            return this.clientIp;
        if (this.fwChain!=null && fwChain.length > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append (this.peerIp);
            sb.append (":").append(fwChain[fwChain.length -1]);
            return sb.toString();
        }
        return null;
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}