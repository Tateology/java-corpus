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
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;
import freecs.Server;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdHitDice extends AbstractCommand {
	private final String cmd= "/td";
	private final String version = "1.01";
	private static final ICommand selve= new CmdHitDice();

	private CmdHitDice () { }
	
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

    private CmdHitDice checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20091030){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public boolean execute (MessageState msgState, String param) {
		if (isPunished (msgState)) 
			return false;
		msgState.targetGroup  = msgState.sender.getGroup ();
        if (msgState.targetGroup.hasState(IGroupState.MODERATED) || msgState.targetGroup.hasState(IGroupState.DEACTIVATE_HITDICE)) {
            msgState.msgTemplate = "error.noRight.deactivated";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        } else if (msgState.targetGroup.hasState(IGroupState.ENTRANCE)) {
            int minRight = IUserStates.ROLE_GOD;
            if (!hasMinRight(minRight, msgState.sender.getGroup(), msgState, "td.minright"))
                return false;
        } else if (param.length () < 1) {
            msgState.msgTemplate = "error.td.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
		}
		msgState.reason = param;
		param = param.toLowerCase ();
		int posd = param.indexOf ("d");
		int poss = param.indexOf (" ");
		int dies,eyes = 6;
		if (posd > -1 || poss > -1) {
		   String values[];
		   if (posd > -1) values = param.split ("d");
		   else values = param.split (" ");
		   if (values.length > 2 || values.length < 1) {
			  msgState.msgTemplate = "error.td.wrong";
			  msgState.sender.sendMessage (msgState.mp);
			  return false;
		   }
		   try {
			  dies = Integer.parseInt (values[0]);
			  eyes = Integer.parseInt (values[1]);
		   } catch (NumberFormatException nfe) {
			  msgState.msgTemplate = "error.td.wrong";
			  msgState.sender.sendMessage (msgState.mp);
			  return false;
		   }
		} else {
		   try {
			  dies = Integer.parseInt (param);
		   } catch (NumberFormatException nfe) {
			  msgState.msgTemplate = "error.td.wrong";
			  msgState.sender.sendMessage (msgState.mp);
			  return false;
		   }
		}
		if (dies < 1 || eyes < 2) {
		   msgState.msgTemplate = "error.td.wrong";
		   msgState.sender.sendMessage (msgState.mp);
		   return false;
		} else if (dies > Server.srv.MAX_DIE_NUMBER || eyes > Server.srv.MAX_DIE_EYES) {
		   msgState.msgTemplate = "error.td.toomany";
		   msgState.sender.sendMessage (msgState.mp);
		   return false;
		}
		if (dies > 1) {
		   StringBuffer sb = new StringBuffer ();
		   int total = 0;
		   for (int i = 0; i < dies; i++) {
			  sb.append (i+1);
			  sb.append (".: <b>");
			  int rnd = (int) Math.round (Math.random () * (eyes -1)) + 1;
			  sb.append (rnd);
			  total += rnd;
			  if (i < dies-1) 
                  sb.append ("</b> / ");
			  else 
                  sb.append ("</b>");
		   }
		   msgState.param = String.valueOf (total);
		   msgState.message = sb.toString ();
		} else {
		   int points = (int) Math.round (Math.random () * (eyes -1)) + 1;
           msgState.param = String.valueOf (points);
		   msgState.message = String.valueOf (points);
		}
		msgState.msgTemplate = "message.td";
		msgState.sender.getGroup ().sendMessage (msgState.mp);
		return true;
	}
}
