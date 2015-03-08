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

import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.User;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.util.GroupUserList;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdUnPunish extends AbstractCommand {
	private final String cmd= "/rgag";
	private final String version = "1.0";
	private static final ICommand selve=new CmdUnPunish();

	private CmdUnPunish () { }
	
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
            msgState.msgTemplate = "error.rgag.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        int pos = param.indexOf(":");
        if (pos != -1) {
            msgState.reason = param.substring (pos+1);
            param = param.substring (0, pos);
        } else
            msgState.reason = "";

        // only vip's are allowd to punish more than one user
        if (param.indexOf(" ") > -1 
            && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            msgState.msgTemplate = "error.mass.noRight.noVipAdmin";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        GroupUserList gul = new GroupUserList();
        Vector<User> found = getMultibleUsers(msgState, param);
        msgState.useRenderCache = false;
        for (Iterator<User> i = found.iterator(); i.hasNext (); ) {
            User cu = (User) i.next();
            if (cu.equals(msgState.sender)) {
                i.remove();
                continue;
            }
            // user must be >vip to unpunish users within other groups 
            if (!mayPunish 
                    && (cu.getGroup() == null 
                            || !cu.getGroup().equals(msgState.sender.getGroup()))) {
                msgState.msgTemplate = "error.noRight.noVipAdmin";
                User u = msgState.usercontext;
                msgState.usercontext = cu;
                msgState.sender.sendMessage (msgState.mp);
                msgState.usercontext = u;
                i.remove();
                continue;
            }
            boolean p = false;
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
        	if (cu.isEntrancePunished())
        		cu.removePunish();
            if (found.size() > 1) {
               msgState.msgTemplate= "message.rgag.personal";
               cu.sendMessage (msgState.mp);
            } 
            gul.addUser(cu, null);
            msgState.usercontext = cu;
        }
        if (found.size()==0)
            return false;
        if (found.size()==1) {
            msgState.msgTemplate="message.rgag.singular";
            msgState.usercontext.getGroup().sendMessage(msgState.mp);
            if (!msgState.sender.getGroup().equals(msgState.usercontext.getGroup())) {
                msgState.msgTemplate="message.rgag.confirm.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.msgTemplate="message.rgag.confirm.plural";
        msgState.usrList = found.toArray();
        msgState.sender.sendMessage (msgState.mp);
        gul.sendMessage(msgState, "message.rgag", false);
        return true;
	}
}
