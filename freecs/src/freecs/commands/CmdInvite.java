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
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.core.User;
import freecs.content.MessageState;


/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdInvite extends AbstractCommand {
    private final String cmd= "/i";
    private final String version = "1.0";
	private static final ICommand selve=new CmdInvite();

	private CmdInvite () { }
	
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
		if (isPunished (msgState)) return false;
		msgState.targetGroup = msgState.sender.getGroup ();
		if ((msgState.targetGroup.hasState(IGroupState.MODERATED) 
                && !msgState.sender.hasRight (IUserStates.MAY_OPEN_MODERATED_GROUP)) 
            || (!msgState.sender.hasRight (IUserStates.ROLE_VIP) 
                && !msgState.sender.getGroup ().usrIsSu (msgState.sender))) {
			if (!msgState.sender.getGroup ().usrIsSu (msgState.sender) && !msgState.sender.hasRight (IUserStates.ROLE_VIP)) {
				msgState.msgTemplate = "error.noRight.noSuVipAdmin";
				msgState.sender.sendMessage (msgState.mp);
			} else {
			msgState.msgTemplate = "error.i.noRight";
			msgState.sender.sendMessage (msgState.mp);
			}
			return false;
		}
		if (param.length () < 1) {
			msgState.msgTemplate = "error.i.noArg";
            msgState.sender.sendMessage(msgState.mp);
			return false;
		}
        if (param.indexOf (" ") > -1
            && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate="error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        Vector<User> found = getMultibleUsers(msgState, param);
        msgState.useRenderCache = false;
        for (Iterator<User> e = found.iterator(); e.hasNext(); ) {
            User cu = (User) e.next();
            msgState.msgTemplate = "message.i.personal";
            if (!setInvited (msgState, cu)) {
                e.remove();
                continue;
            }
            msgState.usercontext = cu;
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.i.singular";
        } else {
            msgState.msgTemplate="message.i.plural";
            msgState.usrList = found.toArray();
        }
        msgState.sender.sendMessage(msgState.mp);
		return true;
	}
}
