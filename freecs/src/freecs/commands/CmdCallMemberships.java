/**
 * Copyright (C) 2008 Rene M.
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
 * Created on 09.10.2008
 */

package freecs.commands;


import freecs.content.CallMembershipObject;
import freecs.content.MessageState;
import freecs.core.Membership;
import freecs.core.MembershipManager;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;

public class CmdCallMemberships extends AbstractCommand {
	private final String cmd= "/mcall";
	private final String version = "1.0";
	private static final ICommand selve=new CmdCallMemberships();

	private CmdCallMemberships () { }
	
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
	    msgState.usercontext = msgState.sender;
	    if (!msgState.sender.hasRight(IUserStates.MAY_CALL_MEMBERSHIPS)) {
            msgState.msgTemplate = "error.noRight.deactivated";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
	    if (isPunished (msgState)) 
            return false;
	    String parts[] = param.split (":");       
        Membership cms = null;
          
        if (parts.length >1){
            if (parts[1].equals("listhistory")  && msgState.sender.getLastCalledMembership() != null){
                msgState.sender.getLastCalledMembership().sendHistory( msgState.sender);
                return true;
            } else  cms = MembershipManager.instance.getMembership (parts[1]);
        }
        if (cms != null)
            param = parts[0];
        if (cms != null){
            if (!msgState.sender.hasMembership(cms.key)){
                msgState.msgTemplate = "error.membership.notMember";
                msgState.usercontext=null;
                msgState.sender.sendMessage(msgState.mp);
                return false;
            }
		    if (param.length () > 0) {			
			    msgState.msgTemplate = "message.mcall";
			    msgState.message = param;     
			    if (cms.getName() != null)
			        msgState.param = cms.key;
			    else msgState.param ="";
			    msgState.sender.setLastCalledMembership(cms);
                cms.sendMessage(msgState.mp);	
                CallMembershipObject cm = new CallMembershipObject(cms, msgState.param,  msgState.message,  msgState.sender);
                cms.addCallKey(cm);
                return true;
		    }
        }
        if (msgState.sender.getLastCalledMembership() != null){
            msgState.msgTemplate = "message.mcall";
            msgState.message = param;     
            cms = MembershipManager.instance.getMembership (msgState.sender.getLastCalledMembership().key);
            if (cms.getName() != null)
                msgState.param = cms.key;
            else msgState.param ="";
            cms.sendMessage(msgState.mp); 
            CallMembershipObject cm = new CallMembershipObject(cms, msgState.param,  msgState.message,  msgState.sender);
            cms.addCallKey(cm);
            return true;
        }
		return true;
	}
}
