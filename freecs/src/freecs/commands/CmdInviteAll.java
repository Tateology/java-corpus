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
import freecs.core.GroupManager;
import freecs.core.UserManager;
import freecs.core.User;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdInviteAll extends AbstractCommand {
	private final String cmd= "/ia";
	private final String version = "1.01";
	private static final ICommand selve=new CmdInviteAll();

	private CmdInviteAll () { }

	public static ICommand getInstance () {
		return selve;
	}
	   
    public Object instanceForSystem() {
        return checkVersion();
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
    
    private CmdInviteAll checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20100304){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public boolean execute (MessageState msgState, String param) {
		if (isPunished (msgState)) return false;
		msgState.targetGroup = msgState.sender.getGroup ();
		int minRightSepa = IUserStates.ROLE_GOD;
		if (param.length () < 1 
            && !hasMinRightSepa(minRightSepa, msgState, "ia.minright.sepa")) {
			return false;
		}
//		inviting into a starting-group is prohibited
		if (msgState.targetGroup.hasState(IGroupState.ENTRANCE)) {
			msgState.msgTemplate = "error.ia.inviteStartGroup";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		if (param.length () > 0 
            && !hasMinRightSepa(minRightSepa, msgState, "ia.minright.sepa")) {
			return false;
		}
		if (param.length () < 1) {
			msgState.targetGroup = msgState.sender.getGroup ();
			int counter = 0;
            User[] usrs = UserManager.mgr.ustr.toArray();
            for (int i = 0; i < usrs.length; i++) {
                User cu = usrs[i];
                if (msgState.sender.equals (cu.invitedBy())
                    && msgState.sender.getGroup().equals(cu.invitedTo())) 
                    continue;
                msgState.msgTemplate = null;
                if (setInvited(msgState, cu)) {
                    counter++;
                }
            }
			if (counter==0)
                return false;
            msgState.param = String.valueOf (counter);
			msgState.msgTemplate = "message.ia.all";
			msgState.sender.getGroup ().sendMessage (msgState.mp);
			return true;
		}
		msgState.sourceGroup = GroupManager.mgr.getGroup (param);
        if (msgState.sourceGroup == null) {
            msgState.msgTemplate = "error.group.notExisting";
            msgState.param = param;
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        /**
        if (msgState.sourceGroup.hasState(IGroupState.ENTRANCE) 
                && !msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
			msgState.msgTemplate = "error.noRight.noVipAdmin";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}*/
        int counter = 0;
        User[] usrs=msgState.sourceGroup.getUserArray();
        if (usrs==null) {
            // if the user-array is null, this group doesn't exist anymore
            msgState.msgTemplate = "error.group.notExisting";
            msgState.param = param;
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        for (int i = 0; i < usrs.length; i++) {
            User cu = usrs[i];
            if (setInvited(msgState, cu))
                counter++;
        }
        if (counter==0)
            return false;
        msgState.param = String.valueOf (counter);
		msgState.msgTemplate = "message.ia.group";
		msgState.sender.getGroup ().sendModeratedMessage (msgState.mp);
		return true;
	}
}
