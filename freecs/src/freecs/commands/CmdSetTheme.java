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
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdSetTheme extends AbstractCommand {
	private final String cmd= "/t";
	private final String version = "1.01";
	private static final ICommand selve=new CmdSetTheme();

	private CmdSetTheme () { }
	
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
        msgState.targetGroup = msgState.sender.getGroup ();
        // check rights
        if (msgState.sender.hasRight(IUserStates.ROLE_VIP) && msgState.targetGroup.hasState(IGroupState.ENTRANCE) && !msgState.sender.hasRight(IUserStates.MAY_SET_THEME)){
            msgState.msgTemplate = "error.noRight.deactivated";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
            if (msgState.targetGroup.hasState(IGroupState.ENTRANCE) && !msgState.targetGroup.hasState(IGroupState.SU_CAN_SETTHEME)) {
                msgState.msgTemplate = "error.noRight.noVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            } else if (!msgState.targetGroup.usrIsSu (msgState.sender)) {
                msgState.msgTemplate = "error.noRight.noSuVipAdmin";
                msgState.sender.sendMessage (msgState.mp);
                return false;
            } else if (!msgState.targetGroup.hasState(IGroupState.SU_CAN_SETTHEME)){
            	msgState.msgTemplate = "error.noRight.deactivated";
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
        } else if (!isVip(msgState.sender, msgState.targetGroup, msgState)) {
		    if (msgState.usrList.length > 1)
		        msgState.msgTemplate = "error.membership.notMember.many";
		    else
		        msgState.msgTemplate = "error.membership.notMember";
            msgState.usercontext=null;
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        
		if (param.length() < 1) {
		    if (msgState.sender.getGroup().getTheme() == null
		            || msgState.sender.getGroup().getTheme().length() == 0)
		        return true;
            if (msgState.sender.getGroup().getTheme()!= null) {
            	msgState.param = msgState.sender.getGroup().getTheme();
            } else  msgState.param = "";
            msgState.sender.getGroup().setTheme(null, null);
            msgState.msgTemplate="message.t.removed";
            msgState.sender.getGroup().sendMessage(msgState.mp);
            return true;
        }
		if (Server.srv.MAX_GROUPTHEME_LENGTH > 0 && param.length() > Server.srv.MAX_GROUPTHEME_LENGTH) {
			param = param.substring(0,Server.srv.MAX_GROUPTHEME_LENGTH);
		}
		msgState.param = param;
	    messageLog(msgState, null,"SetTheme");
        msgState.param = msgState.targetGroup.getTheme();
        msgState.targetGroup.setTheme (param, msgState.sender);
		msgState.msgTemplate = "message.t";
        msgState.targetGroup.sendModeratedMessage (msgState.mp);
 		return true;
	}
}
