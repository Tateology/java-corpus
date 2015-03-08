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
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdAccept extends AbstractCommand {
	private final String cmd= "/a";
	private final String version = "1.0";
	
	private static final ICommand selve=new CmdAccept();

	private CmdAccept () { }
	
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
		if (isPunished(msgState)) 
			return false;
		msgState.targetGroup = msgState.sender.invitedTo ();
		if (msgState.targetGroup == null) 
			return false;
		if (!msgState.sender.invitedBy ().hasRight(IUserStates.ROLE_GOD) && isMembershipLocked(msgState, msgState.targetGroup.getRawName())){
            return false;       
        }
		msgState.usercontext = msgState.sender.invitedBy ();
		if (!msgState.targetGroup.usrIsPresent (msgState.usercontext)) {
			msgState.msgTemplate = "error.a.noLongerValid";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
        if (msgState.targetGroup != null 
                && msgState.targetGroup.usrIsBaned (msgState.sender)) {
            msgState.msgTemplate = "error.j.banned";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        msgState.sourceGroup = msgState.sender.getGroup ();
		if (msgState.targetGroup.equals (msgState.sourceGroup)) {
			msgState.sender.unsetInvitedTo ();
			msgState.msgTemplate="error.j.alreadyHere";
			msgState.sender.sendMessage(msgState.mp);
			return false;
		}
		if (!msgState.targetGroup.addUser (msgState.sender, msgState.usercontext, true)) {
            msgState.msgTemplate = "error.a.noLongerValid";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        msgState.msgTemplate = "message.a";
        msgState.usercontext.sendMessage (msgState.mp);
        msgState.msgTemplate = "message.a.personal";
        msgState.sender.sendMessage(msgState.mp);
		msgState.sender.unsetInvitedTo ();
		msgState.msgTemplate = "message.user.leaving.group";
		msgState.sourceGroup.sendModeratedMessage (msgState.mp);
		msgState.msgTemplate = "message.user.join.group";
		msgState.targetGroup.sendModeratedMessage (msgState.mp);
		return true;
	}
}
