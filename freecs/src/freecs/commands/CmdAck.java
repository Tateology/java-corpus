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
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.User;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdAck extends AbstractCommand {
	private final String cmd= "/ack";
	private final String version = "1.0";
	
	private static final ICommand selve=new CmdAck();

	private CmdAck () { }
	
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
		if (!msgState.sender.hasRight(IUserStates.IS_MODERATOR)) {
			msgState.msgTemplate = "error.noRight.noModAdmin";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		if (!msgState.sender.getGroup().hasState(IGroupState.MODERATED)) {
					msgState.targetGroup=msgState.sender.getGroup();
					msgState.msgTemplate = "error.ack.groupNotModerated";
					msgState.sender.sendMessage (msgState.mp);
					return false;
				}
		if (param.length () < 1) {
			msgState.msgTemplate = "error.ack.noArg";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		
		int pos = param.indexOf(" ");
		if (pos == -1) {
			msgState.msgTemplate = "error.ack.noMessage";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		String uname = param.substring(0,pos).trim();
		User u = getUser(msgState, uname);
		if (u == null)
			return false;
		String msg = param.substring (pos).trim();
		User moderator = msgState.sender;
		Group g = moderator.getGroup();
		if (!g.equals (u.getGroup())) {
			msgState.msgTemplate = "error.ack.userNotInGroup";
			moderator.sendMessage (msgState.mp);
			return false;
		}
		msgState.sender = u;
		if (msg.startsWith ("/")) {
			pos = msg.indexOf (" ");
			ICommand ic = CommandSet.getCommandSet().getCommand(msg.substring (0,pos));
			ic.execute (msgState, msg.substring (pos).trim ());
		} else {
			msgState.msgTemplate = "message.send";
            msgState.message = msg;
			g.sendModeratedMessage (msgState.mp);
		}
		msgState.msgTemplate="message.ack";
		msgState.message = msg;
		msgState.sender = moderator;
		moderator.sendMessage (msgState.mp);
		return true;
	}
}
