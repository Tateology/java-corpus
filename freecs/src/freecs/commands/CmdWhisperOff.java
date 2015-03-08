/**
 * Copyright (C) 2011  Rene M.
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
 * Created on 21.07.2011
 */

package freecs.commands;

import freecs.Server;
import freecs.interfaces.ICommand;
import freecs.content.MessageState;

/**
 * @author Rene M.
 *
 * freecs.commands
 */
public class CmdWhisperOff extends AbstractCommand {
	private final String cmd= "/woff";
	private final String version = "1.0";
	private static final ICommand selve=new CmdWhisperOff();

	private CmdWhisperOff () { }
	
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
    
    private CmdWhisperOff checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20110721){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }
    
    public boolean execute (MessageState msgState, String param) {
        if (msgState.sender.whisperDeactivated()) {
            msgState.sender.setWhisperDeactivated(false);
            msgState.msgTemplate = "message.woff.activated";
        } else if (!msgState.sender.whisperDeactivated()) {
            msgState.sender.setWhisperDeactivated(true);
            msgState.msgTemplate = "message.woff.deactivated";
        }       
 		msgState.sender.sendMessage (msgState.mp);
		return true;
	}
}
