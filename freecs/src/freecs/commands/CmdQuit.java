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
import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdQuit extends AbstractCommand {
	private final String cmd= "/q";
	private final String version = "1.02";
	private static final ICommand selve=new CmdQuit();

	private CmdQuit () { }
	
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
        boolean usrIsOnline = false;
        User cd = UserManager.mgr.getUserByName("q");
        if (cd != null){
            usrIsOnline = true;
        } 
        if (usrIsOnline && !msgState.sender.containsConfirmAction(cmd)){
            if (param != null && param.length()>0){
                msgState.msgTemplate = "error.q.confirm";
                msgState.param = param;
                msgState.sender.sendMessage (msgState.mp);
                msgState.sender.addConfirmAction(cmd);
                return false;
            }
        }
        msgState.sender.removeConfirmAction(cmd);
        if (msgState.sender.isPunished())
            param = null;
		msgState.sender.sendQuitMessage (false, param);
		return true;
	}
}
