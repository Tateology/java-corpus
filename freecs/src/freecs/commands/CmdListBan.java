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
import java.util.Enumeration;
import java.util.Vector;

import freecs.Server;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;
import freecs.core.GroupManager;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdListBan extends AbstractCommand {
	private final String cmd= "/wban";
	private final String version = "1.01";
	private static final ICommand selve=new CmdListBan();

	private CmdListBan () { }
	
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
    
    private CmdListBan checkVersion(){
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
		if (param != null && param.length() > 1) {
			msgState.targetGroup = GroupManager.mgr.getGroup(param);
			if (msgState.targetGroup == null) {
				msgState.msgTemplate="error.group.notExisting";
				msgState.param = param;
				msgState.sender.sendMessage(msgState.mp);
				return false;
			}
		} else 
            msgState.targetGroup = msgState.sender.getGroup();
		
        int minRightEntrace = IUserStates.ROLE_USER;
        int minRightSepa = IUserStates.ROLE_USER;
        if (!hasMinRightEntrace(minRightEntrace, msgState.targetGroup, msgState, "wban.minright.entrace"))
            return false;
        if (!hasMinRightSepa(minRightSepa, msgState, "wban.minright.sepa"))
            return false;

		Vector<String> bl = msgState.targetGroup.bannedUsers();
		StringBuffer sb = new StringBuffer();
		StringBuffer tsb = new StringBuffer();
		for (Enumeration<String> e = bl.elements(); e.hasMoreElements(); ) {
			String uname = (String) e.nextElement();
            sb.append ("<a href='/SEND?message=/uban ");
            sb.append (uname);
            sb.append ("' target=dummy>");
            sb.append (uname);
            sb.append ("</a>");
            tsb.append (" ");
            tsb.append (uname);
			if (e.hasMoreElements())
				sb.append (", ");
		}
		if (sb.length() < 1) {
			msgState.msgTemplate = "error.wban.nobodyBanned";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
        sb.append ("<br><a href='/SEND?message=/uban");
        sb.append (tsb);
		sb.append ("' target=dummy>");
		sb.append ("<b>ubanall</b>");
		sb.append ("</a>");

		msgState.message = sb.toString();
		msgState.msgTemplate="message.wban";
		msgState.sender.sendMessage (msgState.mp);
		return false;
	}
}
