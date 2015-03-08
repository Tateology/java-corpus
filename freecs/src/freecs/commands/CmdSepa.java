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
import freecs.core.User;
import freecs.core.UserManager;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdSepa extends AbstractCommand {
	private final String cmd= "/sepa";
	private final String version = "1.02";
	private static final ICommand selve=new CmdSepa();

	private CmdSepa () { }

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
		if (param.length () < 1) {
			msgState.msgTemplate = "error.sepa.noArg";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		boolean usrIsOnline = false;
		User cd = UserManager.mgr.getUserByName("sepa");
		if (cd != null){
		    usrIsOnline = true;
		} 
        if (usrIsOnline && !msgState.sender.containsConfirmAction(cmd)){
            msgState.msgTemplate = "error.sepa.confirm";
            msgState.param = param;
            msgState.sender.sendMessage (msgState.mp);
            msgState.sender.addConfirmAction(cmd);
            return false;
        }
        msgState.sender.removeConfirmAction(cmd);
		int pos = param.indexOf (":");
		String joinGroup;
		String topic = null;
		if (pos > -1) {
			joinGroup = param.substring (0,pos).trim();
			topic = param.substring (pos+1).trim();
		} else {
			joinGroup = param;
		}
		if (joinGroup.length() <1 ) {
			msgState.msgTemplate="error.sepa.noArg";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		if (Server.srv.MAX_GROUPNAME_LENGTH > 0 
	            && joinGroup.length() > Server.srv.MAX_GROUPNAME_LENGTH)
			joinGroup = joinGroup.substring (0,Server.srv.MAX_GROUPNAME_LENGTH);
		if (joinGroup.equalsIgnoreCase ("exil") || 
			!msgState.sender.hasRight (IUserStates.MAY_OPEN_GROUP)) {
            msgState.param = joinGroup;
			msgState.msgTemplate = "error.sepa.noRight";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		msgState.sourceGroup = msgState.sender.getGroup ();
        msgState.targetGroup = GroupManager.mgr.getGroup(joinGroup);
        if (msgState.sender.getGroup().equals(msgState.targetGroup)) {
            msgState.msgTemplate = "error.sepa.alreadyHere";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        if (msgState.targetGroup != null) {
            if (msgState.sender.isUnregistered && msgState.targetGroup.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG)) {
                msgState.param = joinGroup;
                msgState.msgTemplate = "error.j.joinunreg";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            } else if (isMembershipLocked(msgState, joinGroup)){
                return false;       
            } else {
                msgState.msgTemplate = "error.sepa.alreadyExists";
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
        }
        msgState.targetGroup = GroupManager.mgr.openGroup (joinGroup, topic, msgState.sender);
        if (msgState.targetGroup == null) {
            if (sendReason(joinGroup, msgState)){
                return false;
            } else {
        	    msgState.param = joinGroup;
        	    msgState.msgTemplate = "error.sepa.noRight";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            }
        } 
        if (topic != null){
            msgState.targetGroup.setThemeCreator(msgState.sender);
        }
        
        if (!msgState.targetGroup.usrMayLock(msgState.sender)) {
            msgState.msgTemplate = "message.user.leaving.group";
            msgState.sourceGroup.sendMessage(msgState.mp);
            msgState.msgTemplate = "message.j";
            msgState.sender.sendMessage(msgState.mp);
            msgState.msgTemplate = "error.sepa.l.noRight";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        } 
        msgState.targetGroup.unsetState(IGroupState.OPEN);
        msgState.msgTemplate="message.sepa";
        msgState.sourceGroup.sendMessage(msgState.mp);
        msgState.msgTemplate="message.sepa.confirm";
        msgState.sender.sendMessage(msgState.mp);
        return true;
    }
}
