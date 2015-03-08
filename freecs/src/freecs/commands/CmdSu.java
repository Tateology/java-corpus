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
public class CmdSu extends AbstractCommand {
	private final String cmd= "/su";
	private final String version = "1.0";
	private static final ICommand selve=new CmdSu();

	private CmdSu () { }
	
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
            msgState.msgTemplate = "error.su.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
		msgState.targetGroup = msgState.sender.getGroup ();
        Group sg = msgState.sender.getGroup();
        if (sg == null)
            return false;
        boolean mayChangeRights = msgState.sender.hasRight (IUserStates.MAY_CHANGE_RIGHT);
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
            User cu = (User) i.next();
            if (cu.hasRight(IUserStates.ROLE_GOD)) {
                msgState.msgTemplate="error.noRight.isAdmin";
                msgState.usercontext=cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            } else if (cu.hasRight(IUserStates.ROLE_VIP)) {
                msgState.msgTemplate="error.noRight.isVip";
                msgState.usercontext=cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            } else if (cu.hasRight(IUserStates.ROLE_ASSHOLE)) {
                msgState.msgTemplate="error.su.noRight";
                msgState.usercontext=cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            }
            if (!mayChangeRights && 
                   !msgState.sender.getGroup().equals(cu.getGroup())) {
                msgState.msgTemplate="error.noRight.noVipAdmin";
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            }
            if (cu.getMembership(sg.getSuForbiddenMembership())!=null) {
            	msgState.msgTemplate="error.noRight.isSuForbiddenMembership";
                msgState.usercontext=cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            }
            if (cu.getGroup() == null  // user is currently changing groups... 
                    || cu.getGroup().usrIsSu(cu)) {
                i.remove();
                continue;
            }
            if (cu.getGroup().hasState(IGroupState.ENTRANCE)
                && !mayChangeRights
                && sg.suUserCount() >= Server.srv.MAX_SUUSERS_PER_STARTGROUP) {
                    msgState.msgTemplate = "error.su.tooManyForStartgroup";
                    msgState.param = String.valueOf(Server.srv.MAX_SUUSERS_PER_STARTGROUP);
                    msgState.sender.sendMessage(msgState.mp);
                    i.remove();
                    while (i.hasNext()) {
                    //    Object o = i.next();
                        i.remove();
                    }
                    break;
            }
            msgState.usrList=null;
			if (setSuRight (msgState, cu, true)) {
                msgState.usercontext = cu;
                gul.addUser(cu, null);
                if (found.size() > 1) {
                    msgState.msgTemplate="message.su.personal";
                    cu.sendMessage(msgState.mp);
                }
                StringBuffer sb = new StringBuffer ();
                sb.append (msgState.sender.getName());
                sb.append (" gave su-rights to user ");
                sb.append (cu.getName());
                sb.append (" in group ");
                sb.append (cu.getGroup().getRawName());
                Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_MINOR);
            } else if (!sg.usrIsSu (msgState.sender) 
                        && !msgState.sender.hasRight (IUserStates.ROLE_VIP)) {
                User u = msgState.usercontext;
                msgState.usercontext = cu;
                msgState.msgTemplate = "error.noRight.noSuVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                i.remove();
                msgState.usercontext = u;
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
            msgState.msgTemplate = "message.su.singular";
            msgState.usercontext = (User) found.get(0);
            Group g = msgState.usercontext.getGroup();
            g.sendMessage(msgState.mp);
            if (!msgState.sender.getGroup().equals(g)) {
                msgState.msgTemplate = "message.su.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.msgTemplate = "message.su.confirm.plural";
        msgState.usrList = found.toArray();
        msgState.sender.sendMessage(msgState.mp);
        gul.sendMessage(msgState, "message.su", false);
        return true;
	}
    
    public String toString() {
        return "[CmdSu]";
    }
}
