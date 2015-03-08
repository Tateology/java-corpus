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
import freecs.util.FadeColor;
import freecs.content.MessageState;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdChangeColor extends AbstractCommand {
	private final String cmd= "/col";
	private final String version = "1.0";
	private static final ICommand selve= new CmdChangeColor();

	private CmdChangeColor () { }
	
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
		if (isPunished (msgState) || isColLocked(msgState)) 
			return false;	    
        msgState.targetGroup=msgState.sender.getGroup();
		if (msgState.targetGroup == null) return false;
		if (msgState.targetGroup.hasState(IGroupState.ENTRANCE)) {
			msgState.msgTemplate = "error.col.startingGroup";
			msgState.sender.sendMessage(msgState.mp);
			return false;
		}
		if (msgState.sender.getGroup().hasState(IGroupState.MODERATED)) {
			msgState.msgTemplate = "error.col.noRight";
			msgState.sender.sendMessage (msgState.mp);
			return false;
		}
		String colCode = null,fadeColCode = null;
	    int pos1 = param.indexOf(" ");	    
		if (pos1 == -1){
		    msgState.param = param;
	    } else {
	    	String parts[] = param.split (" ");
	    	colCode = parts[0];
	    	fadeColCode = parts[1];
	    	if (!colCode.equals(fadeColCode) && msgState.sender.getName().length()>2) {
	    	    msgState.param = fadeColCode;
			    if (!isColorCodeValid(msgState, true)) {
	                msgState.sender.sendMessage(msgState.mp);
	                return false;
	            }
			    msgState.param = colCode;
			    param = colCode;
	    	} else {
	    		msgState.sender.setFadeColCode(null);
	    		param = colCode;
	    		msgState.param = colCode;
	    	}
	    }
        if (!isColorCodeValid(msgState, false)) {
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
		if (!msgState.sender.changeColCode (param)) {
		   msgState.msgTemplate = "error.col.tooOften";
           msgState.param = String.valueOf(Server.srv.COLOR_CHANGE_INTERVAL/1000);
		   msgState.sender.sendMessage (msgState.mp);
		   return false;
		}
		if (fadeColCode != null){
		    msgState.sender.setFadeColCode(fadeColCode);
		    msgState.sender.setFadeColorUsername(FadeColor.getFadeColorUsername(colCode, fadeColCode, msgState.sender.getName()));
		}
		msgState.msgTemplate = "message.col";
        msgState.targetGroup.sendModeratedMessage (msgState.mp);
		return true;
	}
}