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

import freecs.interfaces.ICommand;
import freecs.interfaces.IGroupState;
import freecs.content.MessageState;
import freecs.core.GroupManager;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.IUserStates;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdUnBan extends AbstractCommand {
	private final String cmd= "/uban";
	private final String version = "1.0";
	private static final ICommand selve=new CmdUnBan();

	private CmdUnBan () { }
	
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
        String parts[] = param.split (":");
        param = parts[0];
        msgState.targetGroup = null;
        if (parts.length > 1) {
            msgState.targetGroup = GroupManager.mgr.getGroup (parts[1]);
            if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)
                && !msgState.sender.getGroup().equals(msgState.targetGroup)) {
                msgState.msgTemplate="error.noRight.noVipAdmin";
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
            if (msgState.targetGroup==null) {
                msgState.param = parts[1];
                msgState.msgTemplate="error.group.notExisting";
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
        } else
            msgState.targetGroup = msgState.sender.getGroup ();
        
        if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
        	if (msgState.targetGroup.hasState(IGroupState.ENTRANCE)) {
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
		    if (msgState.usrList.length > 1)
		        msgState.msgTemplate = "error.membership.notMember.many";
		    else
		        msgState.msgTemplate = "error.membership.notMember";
            	msgState.usercontext=null;
            	msgState.sender.sendMessage(msgState.mp);
            	return false;
        }

        if (param != null && param.indexOf(" ") > 1
            && !msgState.sender.hasRight (IUserStates.ROLE_VIP)) {
                msgState.msgTemplate = "error.mass.noRight.noVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
        } else if (param == null || param.length () < 1) {
            msgState.msgTemplate = "error.uban.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        msgState.useRenderCache=false;
        String users[] = param.split (" ");
        Vector<Object> found = new Vector<Object>();
        Vector<Object> notfound = new Vector<Object>();
        for (int i = 0; i < users.length; i++) {
            Object o = UserManager.mgr.getUserByName(users[i]);
            if (o == null)
                o = users[i];
            if (!msgState.targetGroup.usrIsBaned(users[i])) {
                notfound.add(o);
            } else {
                found.add(o);
                if (o instanceof User) {
                    msgState.usercontext = (User) o;
                    msgState.targetGroup.setBanForUser (msgState.usercontext.getName(), false);
                    msgState.msgTemplate = "message.uban.personal";
                    msgState.usercontext.sendMessage (msgState.mp);
                } else {
                    msgState.targetGroup.setBanForUser((String) o, false);
                }
            }
        }
        if (notfound.size() == 1) {
            msgState.msgTemplate = "error.uban.notOnList.singular";
            Object o = notfound.get(0);
            if (o instanceof User) {
                msgState.usercontext = (User) o;
                if ( msgState.usercontext.isEntranceBanned())
            		msgState.usercontext.removeBan();
                msgState.param = "";
            } else {
                msgState.usercontext = null;
                msgState.param = (String) o;
            }
            msgState.sender.sendMessage(msgState.mp);
        } else if (notfound.size() > 1) {
            msgState.msgTemplate = "error.uban.notOnList.plural";
            msgState.usrList = notfound.toArray();
            msgState.sender.sendMessage (msgState.mp);
        }
        if (found.size() == 1) {
            Object o = found.get(0);
            if (o instanceof User) {
                msgState.usercontext = (User) o;
                msgState.param = "";
            } else {
                msgState.usercontext = null;
                msgState.param = (String) o;
            }
            if (!msgState.sender.getGroup().equals(msgState.targetGroup)) {
                msgState.msgTemplate = "message.uban.confirm.singular"; 
                msgState.sender.sendMessage(msgState.mp);
            }
            msgState.msgTemplate = "message.uban.singular";
            msgState.targetGroup.sendModeratedMessage(msgState.mp);
            return true;
        } else if (found.size() > 1) {
            msgState.usrList = found.toArray();
            if (!msgState.sender.getGroup().equals(msgState.targetGroup)) {
                msgState.msgTemplate = "message.uban.confirm.plural";
                msgState.sender.sendMessage (msgState.mp);
            }
            msgState.msgTemplate = "message.uban.plural";
            msgState.usrList = found.toArray();
            msgState.targetGroup.sendModeratedMessage(msgState.mp);
            return true;
        }
        return false;
	}
}
