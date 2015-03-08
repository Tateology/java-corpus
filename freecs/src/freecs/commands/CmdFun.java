/**
 * Copyright (C) 2005  Rene M.
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
 * Created on 20.03.2007
 */

package freecs.commands;
import freecs.interfaces.ICommand;
import freecs.layout.TemplateManager;
import freecs.layout.TemplateSet;
import freecs.content.MessageState;


/**
 * @author Rene M.
 *
 * freecs.commands
 */
public class CmdFun extends AbstractCommand {
	private static String cmd= "/fun";
	private final String version = "1.0";
	private static final ICommand selve = new CmdFun();

	private CmdFun () { }
	
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
		if (param.length () < 1) {
		   msgState.msgTemplate = "error.fun.noArg";
		   msgState.sender.sendMessage (msgState.mp);
		   return false;
		}
		TemplateSet ts = msgState.sender == null
        ? TemplateManager.mgr.getTemplateSet()
        : msgState.sender.getTemplateSet();
        
		String parts[] = param.split (" ");
		String funparam = parts[0];
		StringBuilder me = new StringBuilder("funcommand.").append(funparam);
		String val = ts.getMessageTemplate(me.toString());
        if (val == null) {
           msgState.msgTemplate = "error.fun.commandnotfound";
  		   msgState.sender.sendMessage (msgState.mp);
  		   return false;
        }
        msgState.msgTemplate = me.toString();
        if (parts.length > 1){
		    msgState.param = parts[1];
        } else {
        	msgState.param = "";
        }
		
		if (msgState.moderated)
			msgState.sender.getGroup ().sendModeratorMessage (msgState.mp);
		else
			msgState.sender.getGroup ().sendMessage (msgState.mp);
		return true;
	}
}