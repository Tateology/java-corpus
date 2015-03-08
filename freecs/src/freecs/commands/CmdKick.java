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
import java.util.Vector;
import java.util.Iterator;

import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.util.GroupUserList;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.User;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdKick extends AbstractCommand {
	private final String cmd= "/k";
	private final String version = "1.0";
	private static final ICommand selve=new CmdKick();

	private CmdKick () { }
	
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
		if (msgState.sender.hasRole(IUserStates.ROLE_ASSHOLE)) {
            msgState.msgTemplate="error.k.noRight";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
		boolean mayKick = msgState.sender.hasRight(IUserStates.MAY_KICK);
	    Group sg = msgState.sender.getGroup ();
	    boolean isSu = sg.usrIsSu (msgState.sender);
	    if (!mayKick&& !isSu) {
	            msgState.msgTemplate = "error.noRight.noSuVipAdmin";
	            msgState.sender.sendMessage (msgState.mp);
	            return false;
	    }
        if (param.length() <1 )
            return false;

        int pos1 = param.indexOf(" ");
        int pos2 = param.indexOf(":");
        boolean multiUser = (pos1!=-1 && (pos2 > pos1+1 || pos2==-1));
        if (multiUser && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate="error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
		// get the reason for this kick if a reason was given
		if (pos2>-1) {
            String parts[] = param.split (":");
			param = parts[0];
			if (parts.length >1)
			    msgState.reason = parts[1];
		}
        msgState.targetGroup = GroupManager.mgr.getGroup ("exil");
        Vector<User> found = getMultibleUsers(msgState, param);
        if (found.size() > 1
                && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate = "error.noRight.noVipAdmin";
            msgState.sender.sendMessage(msgState.mp);
            return false; // none-vip's may only use single-user-version
		}
        GroupUserList gul = new GroupUserList();
        msgState.useRenderCache=false;
        for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
            User cu = (User) i.next();
            if (cu.getGroup().getRawName().equalsIgnoreCase("exil")){
                i.remove();
                found.remove(cu);
                continue;           	
            }
            msgState.sourceGroup = cu.getGroup();
            if (!cu.hasRight(IUserStates.FREELY_KICKABLE)) {
              	 if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)
                         && (msgState.sourceGroup==null 
                                 || !msgState.sourceGroup.equals(msgState.sender.getGroup()))) {
                     msgState.usercontext = cu;
                     msgState.msgTemplate = "error.noRight.noVipAdmin";
                     msgState.sender.sendMessage(msgState.mp);
                     return false; // none-vip's may only use single-user-version
                 } else if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)
                         && !msgState.sourceGroup.usrIsSu(msgState.sender)) {
                     msgState.usercontext = cu;
                     msgState.msgTemplate = "error.noRight.noSuVipAdmin";
                     msgState.sender.sendMessage(msgState.mp);
                     return false; // none-vip's may only use single-user-version
                 } else if (cu.hasRight(IUserStates.ROLE_GOD)) {
                     msgState.usercontext = cu;
                     msgState.msgTemplate = "error.noRight.isAdmin";
                     msgState.sender.sendMessage(msgState.mp);
                     i.remove();
                     continue;
                 } else if (cu.hasRight(IUserStates.ROLE_VIP)
                         && !msgState.sender.hasRight(IUserStates.ROLE_GOD)){
                     msgState.usercontext = cu;
                     msgState.msgTemplate = "error.noRight.isVip";
                     msgState.sender.sendMessage(msgState.mp);
                     i.remove();
                     continue;
                 } else if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                 	if (!isSu (msgState.sender, cu.getGroup(), msgState)) {
                		msgState.targetGroup = cu.getGroup();
                        if (msgState.usrList != null){
                	  	    if (msgState.usrList.length > 1)
                			    msgState.msgTemplate = "error.membership.notMember.many";
                		    else
                			    msgState.msgTemplate = "error.membership.notMember";
                		        msgState.usercontext=cu;
                		        msgState.sender.sendMessage(msgState.mp);
                		        i.remove();
                		        continue;
                        }
                }
        }
            }
            if (msgState.targetGroup == null) {
                msgState.targetGroup = GroupManager.mgr.openGroup ("exil", "punished users here", null, msgState.sender);
                if (msgState.targetGroup == null) {
                    msgState.param = "exil";
                    msgState.usercontext = cu;
                    msgState.msgTemplate = "error.k.noGroupRight";
                    msgState.sender.sendMessage(msgState.mp);
                    return false;
                }
                msgState.targetGroup.removeFromSusers(cu);
            } else if (!msgState.targetGroup.usrMayJoin(msgState.sender)) {
                msgState.param = "";
                msgState.usercontext = cu;
                msgState.msgTemplate = "error.k.noGroupRight";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            }
            msgState.usercontext = cu;
            gul.addUser(cu, null);
            msgState.targetGroup.addUser(cu, msgState.sender);
            msgState.msgTemplate = "message.k.personal";
            cu.sendMessage(msgState.mp);
        }
		if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.k.destination.singular";
            msgState.targetGroup.exclusiveSendMessage(msgState.mp, found);
            if (!msgState.sender.getGroup().equals(msgState.sourceGroup)
                    && !msgState.sender.equals(msgState.usercontext)) {
                msgState.msgTemplate="message.k.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            msgState.msgTemplate="message.k.singular";
            msgState.sourceGroup.sendMessage(msgState.mp);
			return true;
        }
        msgState.usrList = found.toArray();
        gul.sendMessage(msgState, "message.k", false);
		msgState.msgTemplate="message.k.destination.plural";
        msgState.targetGroup.exclusiveSendMessage(msgState.mp, found);
        msgState.msgTemplate="message.k.confirm.plural";
        msgState.sender.sendMessage(msgState.mp);
		return true;
	}
}