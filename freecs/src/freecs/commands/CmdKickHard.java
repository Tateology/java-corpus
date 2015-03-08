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
import freecs.core.User;
import freecs.content.MessageState;
import freecs.Server;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdKickHard extends AbstractCommand {
	private final String cmd= "/kh";
	private final String version = "1.0";
	private static final ICommand selve=new CmdKickHard();

	private CmdKickHard () { }
	
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
		if (!msgState.sender.hasRight(IUserStates.MAY_KICK_HARD)) {
            msgState.msgTemplate = "error.noRight.noVipAdmin";
            msgState.sender.sendMessage(msgState.mp);
			return false;
		}
		int pos1 = param.indexOf (" ");
        int pos2 = param.indexOf (":");
        if ((pos1!=-1 && (pos2 > pos1+1 || pos2==-1))
            && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate="error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        if (pos2 > 0) {
            msgState.reason = param.substring(pos2+1);
            msgState.message = param.substring(pos2+1);
            param = param.substring(0,pos2).trim();
		}
        long time = Server.srv.DEFAULT_BAN_DURATION;
        pos1 = param.trim().lastIndexOf(" ");
        if (pos1 != -1) {
            String timeStrg = param.substring(pos1).trim();
    		long customTime=-1;
    		try {
    			customTime = Long.parseLong(timeStrg,10);
    			time = customTime;
                param = param.substring(0,pos1).trim();
    		} catch (NumberFormatException nfe) {
    			/* last arg is not a number so it will be treated as a user */
    		}
        }
		if (time > Server.srv.MAX_BAN_DURATION) 
			time = Server.srv.MAX_BAN_DURATION;
		time = time * 60 * 1000;
		
		Vector<User> found = getMultiblePunishableUsers(msgState, param);
        GroupUserList gul = new GroupUserList();
        msgState.useRenderCache=false;
        for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
            User cu = (User) i.next();
            if (cu.hasRole(IUserStates.ROLE_GOD)) {
            	msgState.usercontext= cu;
                msgState.msgTemplate = "error.noRight.isAdmin";
                msgState.sender.sendMessage(msgState.mp);
                msgState.msgTemplate = "message.kh.godinfo";
                cu.sendMessage(msgState.mp);
                i.remove();
                continue;
            } else if (cu.hasRight(IUserStates.PROTECTED_FROM_KICK)
                    && !msgState.sender.hasRole(IUserStates.ROLE_GOD)) {
            	 msgState.usercontext= cu;
                 msgState.msgTemplate = "error.noRight.isVip";
                 msgState.sender.sendMessage(msgState.mp);
                 i.remove();
                 continue;
            } else if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)
                    && !isVip (msgState.sender, cu.getGroup(), msgState)) {
			    msgState.targetGroup = cu.getGroup();
			    if (msgState.usrList.length > 1)
			        msgState.msgTemplate = "error.membership.notMember.many";
			    else
			        msgState.msgTemplate = "error.membership.notMember";
                msgState.usercontext=cu;
                msgState.sender.sendMessage(msgState.mp);
                i.remove();
                continue;
            }
            if (cu.getGroup() != null){
                msgState.sourceGroup=cu.getGroup();
                gul.addUser(cu, null);
            }
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.kh.singular";
            msgState.usercontext=(User) found.get(0);
            if (msgState.sourceGroup != null)
                msgState.sourceGroup.sendMessage(msgState.mp);
            Server.srv.banUser (found, msgState.reason, time, msgState.sender.getName());
            if (!msgState.sender.getGroup().equals (msgState.sourceGroup)) {
                msgState.msgTemplate = "message.kh.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.usrList = found.toArray();
        gul.sendMessage(msgState, "message.kh", false);
        Server.srv.banUser (found, msgState.reason, time, msgState.sender.getName());
        msgState.msgTemplate = "message.kh.confirm.plural";
        msgState.sender.sendMessage(msgState.mp);
		return true;
	}
}
