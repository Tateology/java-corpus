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
import java.util.Vector;

import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdListVips extends AbstractCommand {
	private final String cmd= "/vip";
	private final String version = "1.0";
	private static final ICommand selve=new CmdListVips();

	private CmdListVips () { }
	
	public static ICommand getInstance () {
		return selve;
	}
	   
    public Object instanceForSystem() {
        return this;
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
	
	public boolean execute (MessageState msgState, String param) {
		if (param.length () > 0) {
			if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
				msgState.msgTemplate = "error.noRight.noVipAdmin";
				msgState.sender.sendMessage (msgState.mp);
				return false;
			}
			msgState.msgTemplate = "message.m.vip";
			msgState.message = param;
			for (Enumeration<Object> e = UserManager.mgr.onlineVips(); e.hasMoreElements (); ) {
				User u = (User) e.nextElement ();
				if (u == null || u.equals(msgState.sender)) 
					continue;
				u.sendMessage (msgState.mp);
			}
			for (Enumeration<Object> e = UserManager.mgr.notListedAsOnlineVips(); e.hasMoreElements (); ) {
				User u = (User) e.nextElement ();
				if (u == null || u.equals(msgState.sender)) 
					continue;
				u.sendMessage (msgState.mp);
			}
			msgState.msgTemplate = "message.m.vip.confirm";
			msgState.sender.sendMessage (msgState.mp);
		} else {
			
            Vector<Object> v =  UserManager.mgr.onlineVipList();
            if (v.size() == 0) {
                msgState.msgTemplate = "error.vip.noVipOnline";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            }
            if (v.size() == 1) {
                msgState.msgTemplate="message.vip.singular";
                msgState.usercontext = (User) v.get(0);
            } else {
                msgState.msgTemplate="message.vip.plural";
                msgState.usrList = v.toArray();
            }
            msgState.sender.sendMessage(msgState.mp);
		}
		return true;
	}
}
