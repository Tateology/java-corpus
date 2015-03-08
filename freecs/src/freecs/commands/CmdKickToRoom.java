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
import freecs.util.GroupUserList;
import freecs.core.GroupManager;
import freecs.core.User;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdKickToRoom extends AbstractCommand {
	private final String cmd= "/kc";
	private final String version = "1.01";
	private static final ICommand selve = new CmdKickToRoom();

	private CmdKickToRoom () { }
	
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
		if (isPunished (msgState)) 
			return false;
		// check if sender does have the right to kick a user
		String grpname = null;
//		Group sg = msgState.sender.getGroup ();
		boolean mayKick = msgState.sender.hasRight(IUserStates.MAY_KICK);
		if (!mayKick) {
            msgState.msgTemplate = "error.noRight.noVipAdmin";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        int pos1 = param.indexOf(" ");
        int pos2 = param.indexOf(":");
        boolean multiUser = (pos1!=-1 && (pos2 > pos1+1 || pos2==-1));
        if (multiUser 
        		&& !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate="error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
		if (pos2>-1) {
            String parts[] = param.split (":");
			param = parts[0];
			msgState.targetGroup = GroupManager.mgr.getGroup(parts[1]);
			grpname = parts[1];
		} else {
        	msgState.msgTemplate = "error.kc.noroom";
            msgState.sender.sendMessage(msgState.mp);
        	return false;
        }
        msgState.targetGroup = GroupManager.mgr.getGroup (grpname);
        Vector<User> found = getMultibleUsers(msgState, param);
 
 //       boolean created = false;
        GroupUserList gul = new GroupUserList();
        msgState.useRenderCache=false;
        for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
            User cu = i.next();
            if (cu==null){
                found.remove(i);
                i.remove();
                continue;
            }
            msgState.sourceGroup = cu.getGroup();
            if (cu.hasRight(IUserStates.ROLE_GOD)) {
            	msgState.usercontext = cu;
                msgState.msgTemplate = "error.noRight.isAdmin";
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                msgState.param = grpname;
                msgState.msgTemplate = "message.kc.godinfo";
                cu.sendMessage(msgState.mp);
                continue;
            } else if (cu.hasRight(IUserStates.ROLE_VIP)
                       && !msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
            	msgState.usercontext = cu;               
            	msgState.msgTemplate = "error.noRight.isVip";
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            } else if (cu.hasRight(IUserStates.PROTECTED_FROM_KICK)
                       && !msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                msgState.msgTemplate = "error.k.protected";
                msgState.usercontext = cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            } else if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)
                    && !isVip (msgState.sender, cu.getGroup(), msgState)) {
			    msgState.targetGroup = cu.getGroup();
                if (msgState.usrList != null) {
			        if (msgState.usrList.length > 1)
			            msgState.msgTemplate = "error.membership.notMember.many";
			        else
			            msgState.msgTemplate = "error.membership.notMember";
			    
                    msgState.usercontext=cu;
                    msgState.sender.sendMessage(msgState.mp);
                    i.remove();
                    continue;
                }
            } else if (cu.getGroup().getName().equals(grpname)) {
            	//same room
            	i.remove();
                continue;
            }
            User exclude = null;
            if (msgState.targetGroup == null) {
                msgState.targetGroup = GroupManager.mgr.openGroup (grpname, "punished users here", cu, msgState.sender);
                if (msgState.targetGroup == null) {
                    msgState.param = "";
                    msgState.usercontext = cu;
                    msgState.msgTemplate = "error.k.noGroupRight";
                    msgState.sender.sendMessage(msgState.mp);
                    return false;
                }
                gul.addUser(cu, msgState.sourceGroup);
                exclude = cu;
 //               created=true;             
            } else if (!msgState.targetGroup.usrMayJoin(msgState.sender)) {
                msgState.param = "";
                msgState.usercontext = cu;
                msgState.msgTemplate = "error.k.noGroupRight";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            }
            msgState.usercontext = cu;
            if (exclude != null && !exclude.equals(cu)) {
                gul.addUser(cu, null);
            } else if (exclude == null){
                gul.addUser(cu, null);
            }
            msgState.targetGroup.addUser(cu, msgState.sender);
            msgState.msgTemplate = "message.kc.personal";
            cu.sendMessage(msgState.mp);
         
            msgState.targetGroup.removeFromSusers(cu);
        }
 		if (found.size()==0)
            return false;
		if (found.size()==1) {
            msgState.msgTemplate="message.kc.destination.singular";
            msgState.targetGroup.exclusiveSendMessage(msgState.mp, found);
            if (!msgState.sender.getGroup().equals(msgState.sourceGroup)) {
                msgState.msgTemplate="message.kc.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            if (msgState.sourceGroup != null) {
                msgState.msgTemplate="message.kc.singular";
                msgState.sourceGroup.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.usrList = found.toArray();
        gul.sendMessage(msgState, "message.kc", false);
 
		msgState.msgTemplate="message.kc.destination.plural";
        msgState.targetGroup.exclusiveSendMessage(msgState.mp, found);
        msgState.msgTemplate="message.kc.confirm.plural";
        msgState.sender.sendMessage(msgState.mp);
 		return true;
	}
}
