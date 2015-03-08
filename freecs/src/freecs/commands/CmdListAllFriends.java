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

import freecs.interfaces.ICommand;
import freecs.core.UserManager;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdListAllFriends extends AbstractCommand {
	private final String cmd= "/fl";
	private final String version = "1.0";
	private static final ICommand selve=new CmdListAllFriends ();

	private CmdListAllFriends () { }
	
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
		if (msgState.sender.numberOfFriends () < 1) {
			msgState.msgTemplate = "error.fl.nofriends";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		msgState.msgTemplate = "message.fl.headline";
		msgState.sender.sendMessage (msgState.mp);
		msgState.useRenderCache = false;
		for (Enumeration<String> e = msgState.sender.friends (); e.hasMoreElements (); ) {
			String uname = (String) e.nextElement ();
			msgState.usercontext = UserManager.mgr.getUserByName (uname); 
			if (msgState.usercontext == null) {
                msgState.msgTemplate = "message.fl.entry.offline";
                msgState.targetGroup = null;
				msgState.param = uname;
			} else { 
                msgState.msgTemplate = "message.fl.entry.online";
				msgState.targetGroup = msgState.usercontext.getGroup ();
				msgState.param = "";
			}
			msgState.sender.sendMessage (msgState.mp);
		}
		msgState.param = String.valueOf (msgState.sender.numberOfFriends ());
		msgState.msgTemplate = "message.fl.count";
		msgState.sender.sendMessage (msgState.mp);
		return false;
	}
}
