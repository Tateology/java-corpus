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
 * Created on 28.09.2003
 */

package freecs.commands;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import freecs.Server;
import freecs.interfaces.*;
import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;

/**
 * @author Manfred Andres
 *
 * freecs.commandset
 */
public class CmdShowIp extends AbstractCommand {
	private final String cmd= "/ip";
	private final String version = "1.01";
	private static final ICommand selve=new CmdShowIp();

	private CmdShowIp () { }
	
	public static ICommand getInstance () {
		return selve;
	}
	   
    public Object instanceForSystem() {
        return checkVersion();
    }
    
    public String getCmd() {
        return cmd;
    }

	/**
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    
    private CmdShowIp checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20100304){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public boolean execute (MessageState msgState, String param) {
		if (isPunished(msgState)) 
			return false;
        int minRight = IUserStates.ROLE_VIP;
        if (!hasMinRight(minRight, msgState.sender.getGroup(), msgState, "ip.minright"))
            return false;
 		if (param.length() < 1) {
			msgState.msgTemplate = "error.ip.noArg";
			msgState.sender.sendMessage(msgState.mp);
			return false;
		}
		boolean listAnoProxy = false;
		if (param != null){
            int pos = param.indexOf(":");
            if (pos>-1) {
                String parts[] = param.split (":");
                param = parts[0];
                if (parts.length >1)
                    if (parts[1].equals("anoproxy")){
                        if (UserManager.mgr.anoProxyUserList().size()<1){
                            msgState.msgTemplate = "error.ip.noProxyUser";
                            msgState.sender.sendMessage(msgState.mp);
                            return false;
                        }
                        listAnoProxy = true;  
                        StringBuilder anoUser = new StringBuilder();
                        for (Enumeration<Object> e = UserManager.mgr.anoProxyUser(); e.hasMoreElements (); ) {
                            User u = (User) e.nextElement ();
                            if (u == null) 
                                continue;
                            
                            anoUser.append(u.getName());
                            if (e.hasMoreElements())
                                anoUser.append(" ");
                        }
                        param = anoUser.toString();
                   }
            }
        }
        Vector<User> found = getMultiblePunishableUsers(msgState, param);
        msgState.useRenderCache=false;
        
        if (!listAnoProxy)
            msgState.msgTemplate="message.ip";
        else  msgState.msgTemplate="message.ip.anoproxy";
        
        for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
            User cu = i.next();
            msgState.usercontext = cu;
            msgState.sender.sendMessage(msgState.mp);
            if (Server.checkLogLvl(Server.MSG_STATE, Server.LVL_MINOR)) {
                StringBuffer sb = new StringBuffer();
                sb.append("User ");
                sb.append(msgState.sender.getName());
                sb.append(" has queried the IP-Address of User ");
                sb.append(cu.getName());
                Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
            }
        }
		return true;
	}
    
    public String toString() {
        return "[CmdShowIp]";
    }
}
