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

import freecs.Server;
import freecs.interfaces.IActionStates;
import freecs.interfaces.ICommand;
import freecs.interfaces.IGroupState;
import freecs.content.MessageState;
import freecs.core.GroupManager;
import freecs.core.User;
import freecs.interfaces.IUserStates;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdBan extends AbstractCommand {
	private final String cmd= "/ban";
	private final String version = "1.0";
	
	private static final ICommand selve= new CmdBan();

	private CmdBan () { }
	
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
		int pos = param.indexOf(" "); 
		String parts[] = param.split (":");
		param = parts[0];
        msgState.targetGroup = null;
		if (parts.length > 1) {
			msgState.targetGroup = GroupManager.mgr.getGroup (parts[1]);
			if (msgState.targetGroup==null) {
                msgState.param = parts[1];
				msgState.msgTemplate="error.group.notExisting";
				msgState.sender.sendMessage(msgState.mp);
				return false;
			}
		} else
			msgState.targetGroup = msgState.sender.getGroup ();
		
        if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
        	if (msgState.targetGroup.hasState(IGroupState.ENTRANCE) && !canSuBan(msgState)) {
                msgState.msgTemplate = "error.noRight.noVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            } else if (!msgState.targetGroup.usrIsSu(msgState.sender)) {
                	   msgState.msgTemplate="error.noRight.noSuVipAdmin";
                	   msgState.sender.sendMessage(msgState.mp);
                	   return false;
        	}
        }

		if (!isVip(msgState.sender, msgState.targetGroup, msgState) && !isSu(msgState.sender, msgState.targetGroup, msgState)) {
		     msgState.usrList =  msgState.targetGroup.getMemberships();
			 if (msgState.usrList.length > 1){
			     msgState.msgTemplate = "error.membership.notMember.many";
			 } else {
			     msgState.msgTemplate = "error.membership.notMember";
	             msgState.usercontext=null;
	             msgState.sender.sendMessage(msgState.mp);
	             return false;
			 }
	    }
		if (param == null || param.length () < 1) {
            msgState.msgTemplate = "error.ban.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
		msgState.useRenderCache=false;
		
		long time = Server.srv.MAX_SU_BAN_DURATION;
		if (msgState.sender.hasRight(IUserStates.ROLE_VIP)) 
		    time =0;
        pos = param.trim().lastIndexOf(" ");
        if (pos != -1) {
            String timeStrg = param.substring(pos).trim();
    		long customTime=-1;
    		try {
    			customTime = Long.parseLong(timeStrg,10);
    			time = customTime;
                param = param.substring(0,pos).trim();
    		} catch (NumberFormatException nfe) {
    			/* last arg is not a number so it will be treated as a user */
    		}
        }
        if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
		    if (time > Server.srv.MAX_SU_BAN_DURATION) 
			    time = Server.srv.MAX_SU_BAN_DURATION;
        }
        if (time >0)
		    time = time * 60 * 1000;
				
		ICommand ic = CommandSet.getCommandSet().getCommand("/k");
		Vector<User> found = null;
		if (msgState.sender.hasRight(IUserStates.ROLE_VIP) 
				|| (isSu(msgState.sender, msgState.targetGroup, msgState)
						&& msgState.targetGroup.hasState(IGroupState.CAN_SET_PUNISHABLE))){
			found = getMultiblePunishableUsers(msgState, param);
		} else found = getMultibleUsers(msgState, param);
		
		if (found.size() > 1 ){
		    if (msgState.targetGroup.hasState(IGroupState.ENTRANCE) && !msgState.sender.hasRight (IUserStates.ROLE_VIP)) {
		        msgState.msgTemplate = "error.mass.noRight.noVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
		    }
        }
		
		MessageState bkup = (MessageState) msgState.clone();
        for (Iterator<User> e = found.iterator(); e.hasNext(); ) {
            User cu = (User) e.next();
            if (msgState.sender.equals(cu)){
            	e.remove();
            	msgState.inhale(bkup);
                continue;
            }
            if (!msgState.targetGroup.canSetBanForUser(cu.getName())) {
            	if (msgState.targetGroup.bannedUsers().contains(cu.getName().toLowerCase())) {
            		msgState.usercontext = cu;
            		msgState.msgTemplate = "error.ban.alreadyBaned"; 
            		msgState.sender.sendMessage (msgState.mp);
            	}
            	e.remove();
            	msgState.inhale(bkup);
                continue;
            }
            if (cu.hasRight (IUserStates.PROTECTED_FROM_BAN)
                && !msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                User luc = msgState.usercontext;
                msgState.usercontext = cu;
                msgState.msgTemplate = "error.noRight.noAdmin";
                msgState.sender.sendMessage (msgState.mp);
                msgState.usercontext=luc;
                e.remove();
                continue;
            }
            if (msgState.targetGroup.equals(cu.getGroup())) {
                if (!ic.execute(msgState, cu.getName())) {
                    e.remove();
                    msgState.inhale(bkup);
                    continue;
                }
                msgState.inhale(bkup);
            }
            if (time > 0) {
            	Server.srv.storeUser(IActionStates.SUBAN, cu, null,time, msgState.sender.getName());
            }
            msgState.usercontext = cu;
            msgState.targetGroup.setBanForUser (cu.getName(), true);
            msgState.msgTemplate = "message.ban.personal";
            cu.sendMessage (msgState.mp);
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate = "message.ban.singular";
            msgState.targetGroup.sendModeratedMessage (msgState.mp);
            if (!msgState.sender.getGroup().equals(msgState.targetGroup)) {
                msgState.msgTemplate= "message.ban.confirm.singular";
                msgState.sender.sendMessage (msgState.mp);
            }
        } else {
            msgState.usrList = found.toArray();
            msgState.msgTemplate = "message.ban.plural";
            msgState.targetGroup.sendModeratedMessage (msgState.mp);
            if (!msgState.sender.getGroup().equals(msgState.targetGroup)) {
                msgState.msgTemplate= "message.ban.confirm.plural";
                msgState.sender.sendMessage (msgState.mp);
            }
        }
		return true;
	}
}