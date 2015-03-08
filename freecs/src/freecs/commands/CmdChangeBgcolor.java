/**
 * Copyright (C) 2007  Rene M.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 * Created on 21.09.2007
 */

package freecs.commands;

import java.util.Enumeration;
import java.util.Vector;

import freecs.interfaces.ICommand;
import freecs.layout.TemplateSet;
import freecs.content.MessageState;


public class CmdChangeBgcolor extends AbstractCommand {
	private final String cmd= "/bgcol";
	private final String version = "1.0";
	private static final ICommand selve= new CmdChangeBgcolor();

	private CmdChangeBgcolor () { }
	
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
        msgState.targetGroup=msgState.sender.getGroup();
		
        if (param.equals("?")){
        	TemplateSet ts = msgState.sender.getTemplateSet();
    	   	if (ts.getMessageTemplate("constant.allowedBgcolor") == null)
                return false;
        	msgState.msgTemplate = "message.bgcol.listAllowedCode";
        	msgState.param = listAllowedBgcolor(msgState);
        	msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        
		String bgColCode = param.toLowerCase();
		msgState.param = param;
		if (bgColCode != null && bgColCode.length()>0){
            if (!isBgColorCodeValid(msgState)) {
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
            if (isAllowedBgcolor(bgColCode, msgState)){
	            msgState.sender.setBgColCode(bgColCode);
	        } else {
	            msgState.msgTemplate = "error.bgcol.notAllowedCode";
	            msgState.sender.sendMessage(msgState.mp);
	            return false;
	        }            
		} else {
			 msgState.sender.setBgColCode(null);
		}
		ICommand ic = CommandSet.getCommandSet().getCommand("/c");
		ic.execute(msgState, msgState.sender.getName());
		return true;
	}
	
	private boolean isBgColorCodeValid (MessageState msgState) {
        String colcode = msgState.param.trim().toLowerCase();
        short result = _isColorCodeValid(colcode, false);
        
        if (result==1){
            msgState.msgTemplate = "error.col.wrongCode";
            return false;
        }
        
        return true;
    }
	
	private boolean isAllowedBgcolor(String bgcol, MessageState msgState){
		Vector<String> allowedBgcolor = null;
		TemplateSet ts = msgState.sender.getTemplateSet();
	   	if (ts.getMessageTemplate("constant.allowedBgcolor") != null){
	   		allowedBgcolor = new Vector<String>();
    	    String val = ts.getMessageTemplate("constant.allowedBgcolor");
			String values[] = val.split(",");
			for (int i = 0; i < values.length; i++) {
				if (!allowedBgcolor.contains(values[i].trim().toLowerCase()))
				    allowedBgcolor.addElement(values[i].trim().toLowerCase());
			}
			if (allowedBgcolor.contains(bgcol)){
				return true;
			} else return false;
			
	   	} else return true;
	}
	
	private String listAllowedBgcolor(MessageState msgState){
		String bgColor = null;
		Vector<String> allowedBgcolor = null;
		TemplateSet ts = msgState.sender.getTemplateSet();
	   	if (ts.getMessageTemplate("constant.allowedBgcolor") != null){
	   		allowedBgcolor = new Vector<String>();
    	    String val = ts.getMessageTemplate("constant.allowedBgcolor");
			String values[] = val.split(",");
			for (int i = 0; i < values.length; i++) {
				if (!allowedBgcolor.contains(values[i].trim().toLowerCase()))
				    allowedBgcolor.addElement(values[i].trim().toLowerCase());
			}			
	   	};
	   	StringBuffer sb = new StringBuffer();
	   	for (Enumeration<String> e = allowedBgcolor.elements(); e.hasMoreElements(); ) {
			String color = (String) e.nextElement();
            sb.append (color.toUpperCase());    
			if (e.hasMoreElements())
				sb.append (", ");
		}
	   	bgColor = sb.toString();
		return bgColor;
	}
}
