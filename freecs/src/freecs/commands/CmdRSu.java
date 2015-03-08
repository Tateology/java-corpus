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
import freecs.Server;
import freecs.interfaces.ICommand;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.util.GroupUserList;
import freecs.core.Group;
import freecs.core.User;
import freecs.content.MessageState;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdRSu extends AbstractCommand {
	private final String cmd= "/rsu";
	private final String version = "1.01";
	private static final ICommand selve=new CmdRSu();

	private CmdRSu () { }
	
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
		if (param.length () < 1) {
			msgState.msgTemplate = "error.rsu.noArg";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		int minRightEntrace = IUserStates.ROLE_VIP;
		int minRightSepa = IUserStates.ROLE_USER;
        msgState.targetGroup = msgState.sender.getGroup ();
		Group sg = msgState.sender.getGroup();
        if (sg == null)
            return false;
		boolean mayChangeRights = msgState.sender.hasRight (IUserStates.MAY_CHANGE_RIGHT);

		if (!hasMinRightEntrace(minRightEntrace, sg, msgState, "rsu.minright.entrace"))
		    return false;
        if (!hasMinRightSepa(minRightSepa, msgState, "rsu.minright.sepa"))
            return false;
		if (!mayChangeRights
            && !sg.usrIsSu (msgState.sender))  {
                msgState.msgTemplate = "error.noRight.noSuVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
        }
        if (param.indexOf(" ") > -1
            && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
                msgState.msgTemplate="error.mass.noRight.noVipAdmin";
                msgState.sender.sendMessage(msgState.mp);
                return false;
        }
        msgState.useRenderCache = false;
        GroupUserList gul = new GroupUserList();
        Vector<User> found = getMultibleUsers(msgState, param);
        for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
            User cu = i.next();
            if (!cu.getGroup().usrIsSu(cu) || cu.equals( msgState.sender) || cu.hasRight(IUserStates.PROTECTED_FROM_RIGHTCHANGE)) {
                i.remove();
                continue;
            }
            msgState.usrList = null;
            if (setSuRight (msgState, cu, false)) {
                msgState.usercontext = cu;
                gul.addUser(cu, null);
                if (found.size() > 1) {
                    msgState.msgTemplate="message.rsu.personal";
                    cu.sendMessage(msgState.mp);
                }
                StringBuffer sb = new StringBuffer ();
                sb.append (msgState.sender.getName());
                sb.append (" took su-rights from user ");
                sb.append (cu.getName());
                sb.append (" in group ");
                sb.append (cu.getGroup().getRawName());
                Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
            } else if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
                if (sg.hasState (IGroupState.ENTRANCE)) {
                    msgState.msgTemplate = "error.noRight.noVipAdmin";
                    msgState.sender.sendMessage (msgState.mp);
                } else {
                    msgState.msgTemplate = "error.noRight.noSuVipAdmin";
                    msgState.sender.sendMessage (msgState.mp);
                }
                i.remove();
            } else if(!isSu(msgState.sender, cu.getGroup(), msgState)){
                if (msgState.usrList != null) { // user doesn't have apropriate membership
                    msgState.targetGroup = cu.getGroup();
                    if (msgState.usrList.length > 1){
                        msgState.msgTemplate = "error.membership.notMember.many";
                    } else {
                        msgState.msgTemplate = "error.membership.notMember";                          
                    }
                    msgState.usercontext=cu;
                    msgState.sender.sendMessage(msgState.mp);
                    i.remove();
                    continue;
                } 
            } else {
                User u = msgState.usercontext;
                msgState.usercontext = cu;
                msgState.msgTemplate = "error.noRight.deactivated";
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                msgState.usercontext = u;
            }
        }
        if (found.size() == 0)
            return false;
        if (found.size() == 1) {
            msgState.msgTemplate = "message.rsu.singular";
            msgState.usercontext = found.get(0);
            msgState.usercontext.getGroup().sendMessage(msgState.mp);
            if (!msgState.sender.getGroup().equals(msgState.usercontext.getGroup())) {
                msgState.msgTemplate = "message.rsu.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.msgTemplate = "message.rsu.confirm.plural";
        msgState.usrList = found.toArray();
        msgState.sender.sendMessage(msgState.mp);
        gul.sendMessage(msgState, "message.rsu", false);
		return true;
	}
    
    public String toString() {
        return "[CmdRSu]";
    }
}
