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
import java.util.Iterator;
import java.util.Vector;

import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;
import freecs.core.User;
import freecs.Server;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdIgnore extends AbstractCommand {
	private final String cmd= "/ig";
	private final String version = "1.0";
	private static final ICommand selve= new CmdIgnore();

	private CmdIgnore () { }
	
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
        if (param.length () < 1) {
            msgState.msgTemplate = "error.ig.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        Vector<User> found = getMultibleUsers(msgState, param);
        for (Iterator<User> e = found.iterator(); e.hasNext(); ) {
            User cu = (User) e.next();
            if (cu == null)
                continue;
            if (cu.hasRight (IUserStates.ROLE_VIP)) {
                e.remove();
                continue;
            }
            if (cu.equals(msgState.sender)) {
                e.remove();
                continue;
            }
            msgState.usercontext = cu;
            StringBuffer tsb = new StringBuffer ("User ").append (msgState.sender.getName ()).append (" is ignoring ").append (cu.getName ());
            Server.log (this, tsb.toString (), Server.MSG_STATE, Server.LVL_VERBOSE);
            msgState.sender.ignoreUser(msgState.usercontext);
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.ig.singular";
        } else {
            msgState.msgTemplate="message.ig.plural";
            msgState.usrList = found.toArray();
        }
        msgState.sender.sendMessage(msgState.mp);
        return true;
	}
    
    public String toString() {
        return ("[CmdIgnore]");
    }
}
