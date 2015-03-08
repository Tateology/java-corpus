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
import java.util.Vector;

import freecs.Server;
import freecs.interfaces.ICommand;
import freecs.interfaces.IGroupPlugin;
import freecs.interfaces.IGroupReason;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.layout.TemplateManager;
import freecs.layout.TemplateSet;
import freecs.content.MessageState;
import freecs.content.PrivateMessageStore;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.MessageRenderer;
import freecs.core.PluginRenderer;
import freecs.core.User;
import freecs.core.UserManager;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public abstract class AbstractCommand implements ICommand {
    private final String cmd = "Abstract";
    private final String toStringValue = createToStringValue();
    
    public boolean execute (MessageState msgState, String param) {
        Server.log (this, "not implemented", Server.MSG_ERROR, Server.LVL_MINOR);
        return false;
    }
    
    public static void messageLog(MessageState msgState,User cu,String Command){
    	StringBuffer name = new StringBuffer("[").append(msgState.sender.getName()).append("]");
	    if (msgState.sender.getGroup().hasState(IGroupState.ENTRANCE)){
	    	StringBuffer log = null;
	    	if (Command != null && Command.equals("Think")){
	    		log = new StringBuffer("Think:").append(msgState.message);
	    		log.trimToSize();
	            Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_MESSAGE, Server.LVL_MAJOR);
	            log = null;
	    	} else if (Command != null && Command.equals("SetTheme")){
	    		log = new StringBuffer("SetTheme:").append(msgState.param);
	    	    log.trimToSize();
	            Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_MESSAGE, Server.LVL_MAJOR);
	            log = null;
	    	} else if (Command != null && Command.equals("PrivatMessage")){
	            if (cu == null || cu.getName() == null)
	                    return;

	    	    if (cu.getGroup()!= null)
	    		    log = new StringBuffer("whisper to ").append("[").append(cu.getName()).append("] (room: ").append(cu.getGroup().getRawName()).append(") ").append(msgState.message);
	    	    else log = new StringBuffer("whisper to ").append("[").append(cu.getName()).append("] (room: is changeing").append(msgState.message);
	    	    log.trimToSize();
	    	    Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_MESSAGE, Server.LVL_MAJOR);
	    	    log = null;
	    	} else if (msgState.sender.isAway() && msgState.sender.getAwayMessage() != null ){
	    		if (msgState.sender.getAwayMessage().length()>0){
	    		    log = new StringBuffer("AwayReason: ").append(msgState.sender.getAwayMessage() );
	    		    log.trimToSize();
	                Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_MESSAGE, Server.LVL_MAJOR);
	                log = null;
	    		}
	        } else {
	    		log = new StringBuffer(msgState.message);
                Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_MESSAGE, Server.LVL_MAJOR);
                log = null;
	    	}
	    } else {
	    	StringBuffer log = null;
	    	if (Command != null && Command.equals("Think")){
	    		log = new StringBuffer("Think:").append(msgState.message);
	            Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_SEPAMESSAGE, Server.LVL_MAJOR);
	            log = null;
	    	} else if (Command != null && Command.equals("SetTheme")){
	    		log = new StringBuffer("SetTheme:").append(msgState.param);
	    		log.trimToSize();
	            Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_SEPAMESSAGE, Server.LVL_MAJOR);
	    	} else if (Command != null && Command.equals("PrivatMessage")){
	    	    if (cu == null || cu.getName() == null)
	    	        return;
	    		log = new StringBuffer("whisper to ").append("[").append(cu.getName()).append("] (room: ").append(cu.getGroup().getRawName()).append(") ").append(msgState.message);
	            log.trimToSize();
	    		Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_SEPAMESSAGE, Server.LVL_MAJOR);
	            log = null;
	    	} else if (msgState.sender.isAway() && msgState.sender.getAwayMessage() != null){
	    		if (msgState.sender.getAwayMessage().length()>0){
	    		    log = new StringBuffer("AwayReason: ").append(msgState.sender.getAwayMessage() );
	                Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_SEPAMESSAGE, Server.LVL_MAJOR);
	                log = null;
	    		}
	        } else {
	    		log = new StringBuffer(msgState.message);
                Server.logMessage(msgState, name.toString(), log.toString(), Server.MSG_SEPAMESSAGE, Server.LVL_MAJOR);
                log = null;
	    	}
	    }
    }
    
    boolean isPunished (MessageState msgState) {
        if (!msgState.sender.isPunished ()) 
            return false;
        msgState.msgTemplate = "error.user.punished";
        msgState.sender.sendMessage(msgState.mp);
        return true;
    }
    
    boolean cantHearYou (MessageState msgState, boolean reply){
        if (msgState.usercontext.whisperDeactivated() && !msgState.sender.hasRight(IUserStates.ROLE_VIP)){
            msgState.msgTemplate = "error.m.cantHearYou";
            msgState.sender.sendMessage(msgState.mp);
            return true;
        }
        if (!msgState.usercontext.usrMayWhisper(msgState.sender) && !reply) {
            msgState.msgTemplate = "error.m.cantHearYou";
            msgState.sender.sendMessage(msgState.mp);
            return true;
        }
        if (msgState.sender.isUnregistered &&  msgState.usercontext.getGroup().hasState(IGroupState.NOT_ALLOW_JOIN_UNREG)){
            msgState.msgTemplate = "error.m.cantHearYou";
            msgState.sender.sendMessage(msgState.mp);
            return true;
        }
        return false;
    }
    
    boolean canSuBan(MessageState msgState){
    	if (msgState.targetGroup.hasState(IGroupState.SU_CAN_BAN) && Server.srv.MAX_SU_BAN_DURATION >0)
    	    return true;
    	return false;
    }
    
    boolean isColLocked (MessageState msgState) {
        if (!msgState.sender.isCollocked) 
            return false;      
        return true;
    }
    
    boolean isAwayLocked (MessageState msgState) {
        if (!msgState.sender.isAwaylocked) 
            return false;      
        return true;
    }
    
    boolean isMeLocked (MessageState msgState) {
        if (!msgState.sender.isMelocked) 
            return false;      
        return true;
    }

    /**
     * Utility method for retrieving an User. If the user
     * hasn't been found the apropriate Message will be sent
     * to the sender.
     * @param msgState
     * @param uname
     * @return
     */
    User getUser (MessageState msgState, String uname) {
        User ru = UserManager.mgr.getUserByName (uname);
        if (ru!=null){
            msgState.sourceGroup=ru.getGroup();
        }
        if (ru == null) {
           msgState.msgTemplate = "error.user.notOnServer.singular";
           msgState.param = uname;
           msgState.sender.sendMessage (msgState.mp);
        }
        return ru;
    }

    /**
     * Utility method setting the invitation-state for one specific user
     * @param msgState The message-state to operate on
     * @param cu The user to set the invitation for
     * @return boolean true if invitation was successful, false if not
     */
    boolean setInvited (MessageState msgState, User cu) {
        User tusr = msgState.usercontext;
        msgState.usercontext = cu;
        if (msgState.sender.equals(cu.invitedBy())
            && msgState.targetGroup.equals(cu.invitedTo())) {
            msgState.msgTemplate = "error.i.alreadyInvited";
            msgState.sender.sendMessage (msgState.mp);
            msgState.usercontext = tusr;
            return false;
        }                
        if (msgState.targetGroup.equals (cu.getGroup())) {
            msgState.msgTemplate = "error.i.alreadyHere";
            msgState.sender.sendMessage (msgState.mp);
            msgState.usercontext = tusr;
            return false;
        }
        if (!cu.invitedFrom (msgState.sender)) {
            msgState.usercontext = tusr;
            return false;
        } else {
        	 msgState.msgTemplate = "message.i.personal";
        }
        if (msgState.msgTemplate==null)
            return true;
        cu.sendMessage(msgState.mp);
        return true;
    }
    
    boolean isVip (User u, Group g, MessageState msgState) {
        if (u.hasRight(IUserStates.ROLE_GOD))
            return true;
        if (!u.hasRight(IUserStates.ROLE_VIP))
            return false;
        if (u.getProperty("memberships") == null) {
            return true;
        }
        if (g == null)
        	return true;
        if (g.usrIsMember(u))
            return true;
        msgState.usrList = g.getMemberships();
        return false;
    }
    
    boolean isSu (User u, Group g, MessageState msgState) {
        if (u.hasRight(IUserStates.ROLE_GOD))
            return true;
        if (u.getProperty("memberships") == null) {
            return true;
        }
        if (!u.hasRight(IUserStates.ROLE_VIP))
            if (!g.usrIsSu(u))
        	    return false;
        if (g.usrIsMember(u))
            return true;
        msgState.usrList = g.getMemberships();
        return false;
    }
    
    boolean isMembershipLocked(MessageState msgState, String groupname){
        if (!msgState.targetGroup.isMembershipRoom(msgState.sender) 
                && msgState.targetGroup.hasState(IGroupState.JOIN_MEMBERSHIP_LOCKED)){
            msgState.param = groupname;
            msgState.msgTemplate = "error.j.membershiplocked";
            msgState.sender.sendMessage (msgState.mp);
            return true;
        }
        return false;
    }
    
    boolean hasMinRightEntrace(int minRightEntrace, Group sg, MessageState msgState, String configName){
        String roleEntrace = CommandSet.getCommandSet().getCommandSetProps().getProperty(configName);
        if (roleEntrace == null)
            if (Server.DEBUG)
                Server.log(this, "command.properties "+ configName + " not found", Server.MSG_ERROR, Server.LVL_VERBOSE);
        if (roleEntrace != null && UserManager.resolveState(roleEntrace) >0)
            minRightEntrace = UserManager.resolveState(roleEntrace);
        
        if (!msgState.sender.hasEntraceMinRight(minRightEntrace, msgState, sg)){           
            return false;
        }
        return true;
    }

    boolean hasMinRightSepa(int minRightSepa, MessageState msgState, String configName){
        if (msgState.sender.getGroup().hasState(IGroupState.ENTRANCE))
            return true;
        String roleSepa = CommandSet.getCommandSet().getCommandSetProps().getProperty(configName);
        if (roleSepa == null)
            if (Server.DEBUG)
                Server.log(this, "command.properties "+ configName + " not found", Server.MSG_ERROR, Server.LVL_VERBOSE);
        if (roleSepa != null && UserManager.resolveState(roleSepa) >0)
            minRightSepa = UserManager.resolveState(roleSepa);
        if (!msgState.sender.hasSepaMinRight(minRightSepa, msgState)){           
            return false;
        }
        return true;
    }
    
    boolean hasMinRight(int minRight, Group sg, MessageState msgState, String configName){
        String role = CommandSet.getCommandSet().getCommandSetProps().getProperty(configName);
        if (role == null)
            if (Server.DEBUG)
                Server.log(this, "command.properties "+ configName + " not found", Server.MSG_ERROR, Server.LVL_VERBOSE);
        if (role != null && UserManager.resolveState(role) >0)
            minRight = UserManager.resolveState(role);
        
        if (!msgState.sender.hasMinRight(minRight, msgState, sg)){           
            return false;
        }
        return true;
    }

    String getUserList (MessageState msgState, boolean shorten, boolean withoutBBC) {
        TemplateSet ts = msgState.sender.getTemplateSet ();
        String s = generateUserList (msgState, shorten, ts);
        if (s.length () == 0) 
            return null;
        if (withoutBBC && (Server.srv.BBC_CONVERT_GROUPNAME || Server.srv.BBC_CONVERT_GROUPTHEME))
        	msgState.param = "withoutBBC";
        msgState.msgTemplate = "list.users";
        msgState.message = s;
        s = null;
        String tpl = msgState.sender.getTemplateSet().getMessageTemplate("list.users");
        return MessageRenderer.renderTemplate (msgState, tpl, null);
    }

    public String generateUserList (MessageState msgState, boolean shorten, TemplateSet ts) {
        if (ts == null) {
            ts = TemplateManager.mgr.getTemplateSet();
        }
        Group g = msgState.sourceGroup;
        StringBuffer sb = new StringBuffer ();
        String tplcnt = ts.getMessageTemplate (shorten ? "message.user.short" : "message.user.overview");
        String seperator = ts.getMessageTemplate ("message.user.short.seperator");
        if (!shorten) 
            sb.append ("<br>");
		if (seperator != null) 
            seperator = MessageRenderer.renderTemplate (msgState, seperator, false, null);
        User[] usrs = g.getUserArray();
        if (usrs==null)
            return "";
        for (int i = 0; i < usrs.length; i++) {
            msgState.usercontext = usrs[i];
            String snippet = MessageRenderer.renderTemplate (msgState, tplcnt, false, null);
            sb.append (snippet == null ? msgState.usercontext.getName () : snippet);
            if (i<(usrs.length-1) && (shorten || snippet == null)) {
                if (seperator != null) 
                    sb.append (seperator);
                else 
                    sb.append (", ");
            }
        }
        return sb.toString();
    }

    boolean sendUserList (MessageState msgState, boolean shorten) {
        TemplateSet ts = msgState.sender.getTemplateSet ();
        String s = generateUserList (msgState, shorten, ts);
        if (s.length () == 0) 
            return false;
        msgState.msgTemplate = "list.users";
        msgState.message = s;
        s = null;
        msgState.sender.sendMessage (msgState.mp);
        return true;
	}

    /**
     * Splits up a " " (space)- seperated userlist and returns a vector containing the
     * found user-objects. Not-found-users will be collected and an "error.user.notOnServer.plural"
     * will be sent to the sender.
     * @param msgState the message-state to operate on
     * @param param the string containing the users
     * @return a vector containing all found user-objects
     */
    public Vector<User> getMultiblePunishableUsers (MessageState msgState, String param) {
        String usrs[] = param.split(" ");
        Vector<User> found = new Vector<User>();
        Vector<String> notfound = new Vector<String>();
        for (int i = 0; i < usrs.length; i++) {
            usrs[i].trim();
            User cu = UserManager.mgr.getUserByName(usrs[i]);
            if (cu == null && Server.srv.isPunishable(usrs[i])){
            	Object o = Server.srv.getPunishableKey(usrs[i]);
                if (o instanceof User) {
                	cu = (User) o;
                }
            }
            if (cu == null){
                if (!notfound.contains(usrs[i]) && usrs[i].length() > 0)
                    notfound.add(usrs[i]);
                continue;
            }
            if (!found.contains(cu))
                found.add(cu);
        }

        if (notfound.size() > 1) {
            msgState.usrList = notfound.toArray();
            msgState.msgTemplate = "error.user.notOnServer.plural";
            msgState.sender.sendMessage(msgState.mp);
        } else if (notfound.size() == 1) {
            msgState.param = (String) notfound.get(0);
            msgState.msgTemplate = "error.user.notOnServer.singular";
            msgState.sender.sendMessage(msgState.mp);
        }
        return found;
    }
    
    /**
     * Splits up a " " (space)- seperated userlist and returns a vector containing the
     * found user-objects. Not-found-users will be collected and an "error.user.notOnServer.plural"
     * will be sent to the sender.
     * @param msgState the message-state to operate on
     * @param param the string containing the users
     * @return a vector containing all found user-objects
     */
    public Vector<User> getMultibleUsers (MessageState msgState, String param) {
        String usrs[] = param.split(" ");
        Vector<User> found = new Vector<User>();
        Vector<String> notfound = new Vector<String>();
        for (int i = 0; i < usrs.length; i++) {
            usrs[i].trim();
            User cu = UserManager.mgr.getUserByName(usrs[i]);
            if (cu == null){
                if (!notfound.contains(usrs[i]) && usrs[i].length() > 0)
                    notfound.add(usrs[i]);
                continue;
            }
            if (!found.contains(cu))
                found.add(cu);
        }

        if (notfound.size() > 1) {
            msgState.usrList = notfound.toArray();
            msgState.msgTemplate = "error.user.notOnServer.plural";
            msgState.sender.sendMessage(msgState.mp);
        } else if (notfound.size() == 1) {
            msgState.param = (String) notfound.get(0);
            msgState.msgTemplate = "error.user.notOnServer.singular";
            msgState.sender.sendMessage(msgState.mp);
        }
        return found;
    }

    public void sendPrivateMessage (MessageState msgState, User cu, String message) {
		msgState.sender.setPrivateUser (cu);
		cu.setForeignPrivateUser (msgState.sender);
		msgState.usercontext = cu;
		msgState.msgTemplate = "message.m";

		if (cu.isAway()){
		    Object size = cu.getWhipserSize(); 
            msgState.param = size.toString();
		    msgState.msgTemplate = "message.m.notification";
		}
		if (cu.isAway()){
		    PrivateMessageStore ps = new PrivateMessageStore(message, msgState.sender, cu);
		    cu.addPrivateMessageStore(ps);
		    ps=null;
		}
		cu.sendMessage (msgState.mp);
		
        IGroupPlugin[] plugins = cu.getGroup().getPlugins();
        if (plugins != null){
            PluginRenderer rp = new PluginRenderer();
            rp.checkUsrAction(msgState.mp, plugins, cu);
	    }

		if (cu.isAway ()) {
			msgState.msgTemplate = "message.m.away";
			cu.addWhisper(msgState.sender.getName());
		} else msgState.msgTemplate = "message.m.confirm";
		
		msgState.sender.sendMessage (msgState.mp);
	}
	
	public boolean sendReason(String groupname, MessageState msgState){
	    int reason =  GroupManager.mgr.checkReason(groupname, msgState.sender,  msgState.sender);
        if (reason == IGroupReason.NOT_ALLOW_JOIN_UNREG){
            msgState.param = groupname;
            msgState.msgTemplate = "error.j.joinunreg";
            msgState.sender.sendMessage (msgState.mp);
            return true;
        } else if (reason == IGroupReason.RESERVED){
            msgState.param = groupname;
            msgState.msgTemplate = "error.j.reserved";
            msgState.sender.sendMessage (msgState.mp);
            return true;
        }
        return false;
	}
	/**
     * .) check the permissions of sender for the right to change su-state for this user
     * .) set the su-state wanted
     * @param msgState the message-state-object containing all infos 
     * @param cu the user which will be changed
     * @param give give or take su
     * @return true on success, false otherwise
     */
	boolean setSuRight (MessageState msgState, User cu, boolean give) {
        if (cu.hasRight (IUserStates.PROTECTED_FROM_RIGHTCHANGE))
            return false;
		Group cug = cu.getGroup ();
        if (!cug.usrMaySetSu (msgState.sender))
            return false;
		if (give)
		   return cug.addToSusers (cu);
		else
		   cug.removeFromSusers (cu);
	   return true;
	}
    
	public String toString () {
        return toStringValue;
    }

	public static short _isColorCodeValid (String col, boolean isFadeColor) {
        if (col.length() != 6)
            return 1;
        for (int i = 0; i < col.length (); i++) {
            char c = col.charAt (i);
            if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9' && c != 'a' && c != 'b' && c != 'c' && c != 'd' && c != 'e' && c != 'f') {
               return 1;
            }
        }
        if (Server.srv.COLOR_LOCK_MODE == 0)
        	return 0;
        int r = Integer.parseInt (col.substring (0,2), 16);
        int g = Integer.parseInt (col.substring (2,4), 16);
        int b = Integer.parseInt (col.substring (4,6), 16);
        int sum = r + g + b;
        if (Server.srv.COLOR_LOCK_MODE == 1){
        	if (!isFadeColor) {
                if ((765 - sum) < (64+(Server.srv.COLOR_LOCK_LEVEL*40)))
                    return 2;
        	} else {
        		if ((765 - sum) < (64+(Server.srv.FADECOLOR_LOCK_LEVEL*40)))
                return 2;
        	}
        }
        if (Server.srv.COLOR_LOCK_MODE == 2){
        	if (!isFadeColor) {
                if ((765 - sum) > (701-(Server.srv.COLOR_LOCK_LEVEL*40)))
                    return 2;
        	} else {
        		 if ((765 - sum) > (701-(Server.srv.FADECOLOR_LOCK_LEVEL*40)))
                     return 2;
        	}
        }
        return 0;
    }

    public boolean isColorCodeValid (MessageState msgState, boolean isFadeColor) {
        String colcode = msgState.param.trim().toLowerCase();
        short result = _isColorCodeValid(colcode, isFadeColor);
        if (result==0)
            return true;
        if (result==1)
            msgState.msgTemplate = "error.col.wrongCode";
        else if (result==2)
            msgState.msgTemplate = "error.col.lockedColor";
        return false;
    }
   
    private String createToStringValue() {
         return "[Command " + cmd + "]";
    }
}
