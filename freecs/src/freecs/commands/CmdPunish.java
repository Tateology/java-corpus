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
import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.ScheduledAction;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.IActionStates;
import freecs.interfaces.ICommand;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.util.GroupUserList;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdPunish extends AbstractCommand {
	private final String cmd= "/gag";
	private final String version = "1.0";
	private static final ICommand selve=new CmdPunish();

	private CmdPunish () { }
	
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
		boolean mayPunish = msgState.sender.hasRight (IUserStates.MAY_PUNISH);
		Group sg = msgState.sender.getGroup ();
		boolean isSu = sg.usrIsSu (msgState.sender);
		if (!mayPunish && !isSu) {
            msgState.msgTemplate = "error.noRight.noSuVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		if (param.length () < 1) {
			msgState.msgTemplate = "error.gag.noArg";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
        int pos = param.indexOf(":");
        if (pos != -1) {
            msgState.reason = param.substring (pos+1);
            param = param.substring (0, pos);
        } else
            msgState.reason = "";

        if (param.indexOf(" ") > -1 
            && !mayPunish) {
            msgState.msgTemplate = "error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        
        // try to treat the last argument before the ":" as timeamount
        // after this time, the punishment will be removed
        pos = param.lastIndexOf(" ");
        long secs = -1;
        if (pos > -1) try {
            secs = Long.parseLong (param.substring (pos+1), 10);
            param = param.substring (0, pos);
        } catch (NumberFormatException nfe) {
            // wasn't a number so treat it as user
        }
        GroupUserList gul = new GroupUserList();
        Vector<User> found = null;
        if (msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            found = getMultiblePunishableUsers(msgState, param);
        } else found = getMultibleUsers(msgState, param);
        msgState.useRenderCache = false;
        for (Iterator<User> i = found.iterator(); i.hasNext (); ) {
            User cu = (User) i.next();
            if (cu.equals(msgState.sender)) {
                i.remove();
                continue;
            }
            if (!mayPunish 
                && !msgState.sender.getGroup().equals(cu.getGroup())) {
                msgState.msgTemplate = "error.noRight.noVipAdmin";
                User u = msgState.usercontext;
                msgState.usercontext = cu;
                msgState.sender.sendMessage (msgState.mp);
                msgState.usercontext = u;
                i.remove();
                continue;
            }
            /**
            if (!cs.setPunishment (msgState, cu, true)) {
                i.remove();
                continue;
            }*/
            
            boolean p = true;
            if ((p && msgState.sender.isPunished()) 
        			|| cu == null || cu.isPunished () == p) 
        	   			return false;
        		if (!cu.hasRight(IUserStates.FREELY_PUNISHABLE)) {
                    if (cu.hasRight(IUserStates.ROLE_GOD)) {
                        msgState.msgTemplate = "error.noRight.isAdmin";
                        msgState.usercontext = cu;
                        msgState.sender.sendMessage (msgState.mp);  
                        i.remove();
                        continue;
                    }
                    if (cu.hasRight(IUserStates.ROLE_VIP)
                        && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
                            msgState.msgTemplate = "error.noRight.isVip";
                            msgState.usercontext = cu;
                            msgState.sender.sendMessage (msgState.mp);
                            i.remove(); 
                            continue;
                    } else if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                    	if (!isSu (msgState.sender, cu.getGroup(), msgState)) {
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
                   }
                    if (cu.hasRight (IUserStates.PROTECTED_FROM_PUNISH)
                        && !msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                            msgState.msgTemplate = "error.gag.protected";
                            msgState.usercontext = cu;
                            msgState.sender.sendMessage (msgState.mp);
                            i.remove();
                            continue;
                    }
                }
            cu.setPunish (p);
            if (cu.getGroup() != null && cu.getGroup().hasState(IGroupState.ENTRANCE))
            	Server.srv.storeUser(IActionStates.PUNISH, cu, msgState.reason, Server.srv.PUNISH_DURATION, msgState.sender.getName());
            if (cu.getGroup() == null)
               	Server.srv.storeUser(IActionStates.PUNISH, cu, msgState.reason, Server.srv.PUNISH_DURATION, msgState.sender.getName());
            if (found.size() > 1) {
                msgState.msgTemplate= "message.gag.personal";
                cu.sendMessage (msgState.mp);
            } 
            if (cu.getGroup() != null)
                gul.addUser(cu, null);
            msgState.usercontext = cu;
            if (secs > 0) {
                long millsecs = secs * 1000 + System.currentTimeMillis();
                UserManager.mgr.scheduleAction(ScheduledAction.UNPUNISH, millsecs, cu, msgState.sender);
            }
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.gag.singular";
            if (msgState.usercontext.getGroup() != null)
                msgState.usercontext.getGroup().sendMessage(msgState.mp);
            if (!msgState.sender.getGroup().equals(msgState.usercontext.getGroup())) {
                msgState.msgTemplate="message.gag.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.msgTemplate="message.gag.confirm.plural";
        msgState.usrList = found.toArray();
        msgState.sender.sendMessage (msgState.mp);
        gul.sendMessage(msgState, "message.gag", false);
		return true;
	}
}
