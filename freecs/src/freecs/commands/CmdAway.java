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
import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.User;
import freecs.core.UserManager;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdAway extends AbstractCommand {
	private final String cmd= "/away";
	private final String version = "1.01";
	
	private static final ICommand selve= new CmdAway();

	private CmdAway () { }
	
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

    private CmdAway checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20100205){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public boolean execute (MessageState msgState, String param) {
		if (isPunished (msgState) || isAwayLocked(msgState)) 
		    return false;
		
        boolean usrIsOnline = false;
        User cd = UserManager.mgr.getUserByName("away");
        if (cd != null){
            usrIsOnline = true;
        } 
        if (param!=null && param.length()>0){
            if (usrIsOnline && !msgState.sender.containsConfirmAction(cmd)){
                if (param != null && param.length()>0){
                    msgState.msgTemplate = "error.away.confirm";
                    msgState.param = param;
                    msgState.sender.sendMessage (msgState.mp);
                    msgState.sender.addConfirmAction(cmd);
                    return false;
                }
             }
        }
        msgState.sender.removeConfirmAction(cmd);

		msgState.sender.setAway (true);
		if (param.length () > 0) {
           msgState.sender.setAwayMessage (param);
		} else {
           msgState.sender.setAwayMessage (null);
		}
	    messageLog(msgState,null,null);
		msgState.msgTemplate ="message.away.on";
		Group grp = msgState.sender.getGroup ();
		if (grp != null)
		   grp.sendMessage (msgState.mp);
		else
		   msgState.sender.sendMessage (msgState.mp);
		return true;

	}
}
