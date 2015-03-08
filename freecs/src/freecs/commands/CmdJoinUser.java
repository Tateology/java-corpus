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
import freecs.core.Group;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdJoinUser extends AbstractCommand {
	private final String cmd= "/ju";
	private final String version = "1.01";
	private static final ICommand selve=new CmdJoinUser();

	private CmdJoinUser () { }
	
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
    
    private CmdJoinUser checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20100204){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public boolean execute (MessageState msgState, String param) {
        boolean usrIsOnline = false;
        User cd = UserManager.mgr.getUserByName("ju");
        if (cd != null){
            usrIsOnline = true;
        } 
        if (usrIsOnline && !msgState.sender.containsConfirmAction(cmd)){
            msgState.msgTemplate = "error.ju.confirm";
            msgState.param = param;
            msgState.sender.sendMessage (msgState.mp);
            msgState.sender.addConfirmAction(cmd);
            return false;
        }
        msgState.sender.removeConfirmAction(cmd);

		User u;
		if (param.length () < 1) {
		   msgState.msgTemplate="error.ju.noArg";
		   msgState.sender.sendMessage (msgState.mp);
		   return false;
		} else if (param.indexOf (" ") > -1) {
		   u = getUser (msgState, param.substring(0, param.indexOf (" ")));
		} else {
		   u = getUser (msgState, param);
		}
		if (u == null) 
            return false;
		Group g = u.getGroup ();
		if (g == null) return false;
		if (g.equals (msgState.sender.getGroup ())) {
            msgState.targetGroup = g;
            msgState.usercontext = u;
			msgState.msgTemplate="error.ju.alreadyHere";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		ICommand ic = CommandSet.getCommandSet().getCommand("/j");
		return ic.execute (msgState, g.getRawName());
	}
}