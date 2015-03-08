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
 */
package freecs.core;

import freecs.Server;
import freecs.interfaces.*;
import freecs.util.EntityDecoder;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;

public class Group implements IGroupState, IMessageDestination {
	private IGroupPlugin[] plugins;
	private String suForbiddenMembership;
	private String name, key;
	private String saveName, saveTheme;
	private User opener, themeCreator;
	private int state;
	private int minRight = IUserStates.MAY_JOIN_GROUP;
	private int questionCntr = 0, joinpunishedCntr = 0;
	private int timelockSec = 60;
	private int minSetSuRole = IUserStates.ROLE_USER;
	private User[] usrArr;
	private Vector<Object> usr;
	private Vector<User> susers;
	private Vector<String> banList;
	private Vector<String> autoSuList;
	private Membership[] memberRoom;
	private Membership[] autoSu;
	private Membership[] membershipSu;
	private Membership[] membershipRoom;
    private String lastXmlrpcMessage;
    private int recivedLastXmlrpcMessage;
    private volatile boolean valid;

    public Group(String name, String theme, User opener) {
        this.name = name;
        this.saveName = EntityDecoder.groupnameCharToHtml(name);
        this.key = name.toLowerCase().trim();
        this.saveTheme = EntityDecoder.charToHtml(theme);
        this.themeCreator = null;
        this.state = OPEN | AUTO_SU_FIRST | ALLOW_SU;
        this.usr = new Vector<Object>();
        this.susers = new Vector<User>();
        this.banList = new Vector<String>();
        this.autoSuList = new Vector<String>();
        this.memberRoom = null;
        this.autoSu = null;
        this.membershipSu = null;
        this.membershipRoom = null;
        this.opener = opener;
        this.lastXmlrpcMessage = null;
        this.recivedLastXmlrpcMessage = 0;
        this.plugins = null;

        this.valid = true;
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

    public Group(String name, String theme, int state, User opener) {
        this.name = name;
        this.saveName = EntityDecoder.groupnameCharToHtml(name);
        this.key = name.toLowerCase().trim();
        this.saveTheme = EntityDecoder.charToHtml(theme);
        this.themeCreator = null;
        this.state = state;
        this.usr = new Vector<Object>();
        this.susers = new Vector<User>();
        this.banList = new Vector<String>();
        this.autoSuList = new Vector<String>();
        this.memberRoom = null;
        this.autoSu = null;
        this.membershipSu = null;
        this.membershipRoom = null;
        this.opener = opener;
        this.lastXmlrpcMessage = null; // option for XML-RPC Handler
        this.recivedLastXmlrpcMessage = 0; // option for XML-RPC Handler
        this.plugins = null;
        this.valid = true;
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

   /**
    * returns the name of this group
    * @return the name of this group
    */
    public String getName () {
       return saveName;
    }
   
    public String getRawName() {
   	    return name;
    }
   
   /**
    * @return the key of this group
    */
   public String getKey() {
       return key;
   }
   
    public User getOpener(){
        return opener;
    }

    public void setPlugins (IGroupPlugin[] plugins) {
        this.plugins = plugins;
    }
   
    public IGroupPlugin[] getPlugins () {
        return plugins;
    }
	/**
	 * set the theme of this group
	 * @param t the theme to use for this group
	 */
    public void setTheme (String t, User u) {
        saveTheme = EntityDecoder.charToHtml(t);
        themeCreator = u;
    }
   /**
    * returns the theme of this group
    * @return the theme of this group
    */
    public String getTheme () {
        return saveTheme;
    }
   
    public User getThemeCreator(){
        return themeCreator;
    }
  
    public void setThemeCreator(User ce){
        themeCreator = ce;
    }

    public void setMemberRoom (Membership[] memberRoom) {
        this.memberRoom = memberRoom;
    }
    
    public Membership[] getMemberships () {
        return memberRoom;
    }
    /**
     * adds this user to this group
     * @param u The user joining this Group
     */
    public boolean addUser (User u) {
        GroupManager.mgr.updateGroupListLastModified();
   		return (addUser (u, u, false));
    }
   
	public void addLoginUser (User u) {
        synchronized (this) {
    		usr.addElement(u);
            usrArr=(User[]) usr.toArray(new User[0]);
        }
        
		u.setGroup(this);
	    if (usrIsAutoSu(u)) {
	        addToSusers(u);
	    }
        if (plugins!=null) {   
            for (int i = 0; i<plugins.length; i++) {   
                try {   
                    plugins[i].usrJoin(u);   
                } catch (Exception e) {   
                    Server.debug (plugins[i], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
            }   
        } 
	}
   /**
    * adds this user to this group using the right of another user
    * @param u The user to join to this group
    * @param ru The user to check for the rights for joining ths group
    * @return ture on success, false if joining is not allowed
    */
    public boolean addUser (User u, User ru) {
        return this.addUser (u, ru, false);
    } 
	public boolean addUser (User u, User ru, boolean invited) {
        if (u == null || usr.contains (u)) 
            return true;
        if (!usrMayJoin(ru)
            && (u.equals(ru) || !invited)) 
            return false;
        boolean isFirst;
        synchronized (this) {
            isFirst = usr.isEmpty();
            usr.addElement (u);
            usrArr=(User[]) usr.toArray(new User[0]);
        }
        if ((isFirst 
                && this.hasState(IGroupState.AUTO_SU_FIRST | IGroupState.ALLOW_SU)
                && !this.hasState(IGroupState.ENTRANCE) &&  !this.hasState(NO_SU_FIRST))
            || (autoSuList != null 
                && autoSuList.contains(u.getName().toLowerCase()) && this.hasState(AUTO_SU_FIRST) )) {
            addToSusers (u);
        }
        u.setGroup(this);
        if (plugins!=null & u.isLoggedIn()){
            for (int i = 0; i<plugins.length; i++) {
                try {
                    plugins[i].usrJoin(u);
                } catch (Exception e) {
                    Server.debug (plugins[i], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                }
            }
        }
        return true;
    }

    /**
     * removes this user from this group
     * @param u the user to remove from this group
     */
    public void removeUser (User u) {
        synchronized (this) {
            if (usr==null) {
                return;
            }
            usr.remove (u);
            if (usr.size() <= 0) {
                GroupManager.mgr.removeGroup (this);
                return;
            }
            usrArr=(User[]) usr.toArray(new User[0]);
        }
        while (susers.contains (u)) {
            susers.remove (u);
        }
        if (susers.size()<1
                && !this.hasState(OPEN)) {
            this.setState(OPEN);
            MessageParser mp = new MessageParser ();
            mp.setSender(u);
            mp.setMessageTemplate("message.ul");
            this.sendModeratedMessage(mp);
        }
        if (plugins!=null) {
            for (int i = 0; i<plugins.length; i++) {
                try {
                    plugins[i].usrLeaving(u);
                } catch (Exception e) {
                    Server.debug (plugins[i], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                }
            }
        }
    }

   /**
    * adds user to su-user-list of this room
    * @param u The user to add to the su-user-list of this group
    */
	public boolean addToSusers (User u) {
        if (!this.hasState(AUTO_SU_FIRST) || u.hasRole(IUserStates.ROLE_ASSHOLE))
            return false;
        if (susers.contains (u)) 
            return true;
        susers.addElement (u);
        return true;
	}

	/**
	 * removes a user from the su-user-list of this group
	 * @param u The user to remove from the su-user-list
	 */
	public void removeFromSusers (User u) {
        while (susers.contains(u))
            susers.removeElement (u);
    }
	
	public boolean usrIsAutoSu (User u) {
		if (autoSuList != null && autoSuList.contains(u.getName().toLowerCase()))
	        return true;
        if (this.autoSu == null
                || this.autoSu.length == 0)
            return false;
        if (u.getProperty("memberships") != null){
            for (int i = 0; i < this.autoSu.length; i++)
                if (u.getMembership(this.autoSu[i].key)!= null)
                    return true;
        }
        return false;
    }
    /**
     * send an excluseive message. Message goes to everybody within this group, excluding the users contained within the given vector
     * @param mc the message
     * @param exclude users which will not recieve this message
     */
    public void exclusiveSendMessage (IContainer mc, List<?> exclude) {
        if (usr == null || usrArr==null || usr.size () < 1)
            return;
        User[] uarr = usrArr;
        for (int i = 0; i < uarr.length; i++) {
            User cu = (User) uarr[i];
            if (exclude.contains(cu))
                continue;
            cu.sendMessage (mc);
        }
        PluginRenderer rp = new PluginRenderer();
        rp.checkGrpAction(mc, plugins, this);
    }

   /**
    * Interface IMessageDestination used for sending messages to this group
    * meaning all users of this group
    */

   /**
    * sends a message to all users of this group. If the group is moderated,
    * only the moderator will see the message. A moderator may send a users
    * message by typing /ack username theMessageOfTheUser. This will be done
    * by a template, causing the message to have a link which sends this 
    * acknowledgement.
    * @param mc the IContainer containing the content to send to all users of this group
    */
	public void sendMessage (IContainer mc) {
		if (usr==null || usrArr==null || usr.size () < 1)
			return;
		boolean messageToModerate = false;
		if (mc instanceof MessageParser) {
			User sender = ((MessageParser) mc).getSender();
			messageToModerate = !(sender != null && (sender.hasRight(IUserStates.IS_MODERATOR) || sender.hasRight(IUserStates.IS_GUEST)));
		}
		if (messageToModerate &&
			this.hasState(IGroupState.MODERATED)) {
                User[] uarr = usrArr;
                for (int i = 0; i<uarr.length; i++) {
				User cu = uarr[i];
				if (!cu.hasRight(IUserStates.IS_MODERATOR))
					continue;
				cu.sendMessage (mc);
			}
			return;
        }
		sendMsg(mc);
	}
   
	private void sendMsg (IContainer mc) {
        if (usrArr==null)
            return;
        User[] uarr = usrArr;
        for (int i = 0; i<uarr.length; i++) {
			User cu = uarr[i];
			cu.sendMessage (mc);
		}
        PluginRenderer rp = new PluginRenderer();
        rp.checkGrpAction(mc, plugins, this);
	}
	
	public void sendModeratorMessage (IContainer mc) {
		if (usr==null || usrArr==null || usr.size () < 1) 
			return;
		User sender = ((MessageParser) mc).getSender();
        User[] uarr = usrArr;
   		for (int i=0; i<uarr.length; i++) {
   			User cu = uarr[i];
   			if (sender.equals(cu))
   				continue;
   			cu.sendMessage (mc);
   		}
        PluginRenderer rp = new PluginRenderer();
        rp.checkGrpAction(mc, plugins, this);
	}
	
   /**
	* sends a message to all users of this group
	* @param mc the IContainer containing the content to send to all users of this group
	*/
	public void sendModeratedMessage (IContainer mc) {
		if (usr==null || usr.size () < 1) 
			return;
		sendMsg(mc);
    }

   /**
    * returns an Iterator containing all users of this group
    * @return an Iterator for looping over all groupmembers of this group
    */
    public Iterator<Object> users () {
        return usr.iterator ();
    }

    public User[] getUserArray() {
        return usrArr;
    }
    
    public int userInGroupCount(){
        return usrArr.length;
    }

	/**
	 * Check if the given user may join this group
	 * @param u The user to check the rights from
	 * @return true if the user is allowed to join, false if not
	 */
    public boolean usrMayJoin (User u) {
   		if (u==null) 
   			return false;
        if (!hasState(OPEN)
			&& !u.hasRight (IUserStates.MAY_JOIN_LOCKED_GROUP) 
			&& !susers.contains (u)) 
				return false;
		String uname = u.getName().toLowerCase();
		if (banList != null && banList.contains(uname))
			return false;
		return true;
	}
    
    public boolean usrMaySetSu (User u) {
        if ((!this.equals (u.getGroup ()) 
                && !u.hasRight (IUserStates.MAY_CHANGE_RIGHT)) 
             || !u.hasRight (this.minSetSuRole)
             || !usrIsMember(u))
           return false;
        return true;
    }

    public boolean usrHasMembership (User u) {
    	if (this.membershipSu == null
                || this.membershipSu.length == 0)
            return false;
        if (u.getProperty("memberships") != null){
            for (int i = 0; i < this.membershipSu.length; i++)
                if (u.getMembership(this.membershipSu[i].key)!= null)
                    return true;
        }
        return false;
    }
    
    public boolean isMembershipRoom (User u) {
        if (u == null)
            return true;
        if (u.hasRight(IUserStates.ROLE_GOD))
            return true;
        if (this.membershipRoom == null
                || this.membershipRoom.length == 0)
            return true;
     	 
        for (int i = 0; i < this.membershipRoom.length; i++){
            if (u.getMembership(this.membershipRoom[i].key)!= null){
                return true;
            }
        }
        return false;
    }
    
    public StringBuffer getMembershipRoom() {
    	StringBuffer membership =null;
    	if (this.membershipRoom != null && this.membershipRoom.length >0) {
    	    membership = new StringBuffer();
    	    for (int i = 0; i < this.membershipRoom.length; i++){
    	    	if (i < membershipRoom.length-1)
                    membership.append(this.membershipRoom[i].key).append("|");
    	    	else membership.append(this.membershipRoom[i].key);
            }
    	} else membership = new StringBuffer("");
    	
    	return membership;
    }
    
    public boolean usrIsMember (User u) {
        if (this.memberRoom == null
                || this.memberRoom.length == 0)
            return true;
        for (int i = 0; i < this.memberRoom.length; i++)
            if (u.getMembership(this.memberRoom[i].key)!= null)
                return true;
        return false;
    }
    
    /**
     * returns true if the given user has fulfills all creterias to join this group
     * @param u the user to join
     * @return true if all creterias are fulfilled, false if not
     */
    public boolean usrMayLock (User u) {
        if (this.hasState(IGroupState.LOCKPROTECTED))
            return false;
        if (u.hasRight(IUserStates.ROLE_GOD))
            return true;
        if (!u.hasRight(IUserStates.MAY_LOCK_GROUP)
            && !susers.contains(u))
                return false;
        if (this.hasState(IGroupState.ENTRANCE)
            && !u.hasRight(IUserStates.MAY_LOCK_STARTING_GROUP))
                return false;
        if (hasState(IGroupState.MODERATED) 
            && !u.hasRight(IUserStates.MAY_LOCK_MODERATED_GROUP))
                return false;
        if (!usrIsMember(u))
            return false;
        return true;
    }
    
    public boolean usrMayJoinPunished (User u) {
    	if (this.joinpunishedCntr >= Server.srv.JOIN_PUNISHED_COUNTER)
    		return false;
     	return true;
    }
    
    public void incrementJoinPunishedCounter() {
        if (joinpunishedCntr == Integer.MAX_VALUE)
        	joinpunishedCntr=1;
        else
        	joinpunishedCntr++;
    }
    
    public void resetJoinPunishedCounter() {
    	joinpunishedCntr=0;
    }
    
    /**
     * increments the question-counter, counting the questions asked within this group
     */
    public void incrementQuestionCounter() {
        if (questionCntr == Integer.MAX_VALUE)
            questionCntr=1;
        else
            questionCntr++;
    }

    /**
     * returns the number of questions asked within this group
     * @return number of questions asked within this group
     */
    public int getQuestionCounter() {
        return questionCntr;
    }
    
    public void resetQuestionCounter() {
        questionCntr=0;
    }

	/**
	 * Set the group-ban-state for a user
	 * @param u The user to set the state for
	 * @param on If true, the user will be banned, if false the user will be unbanned
	 */
	public void setBanForUser (String u, boolean on) {
        if (!this.isValid())
            return;
		String uname = u.toLowerCase();
        if (on && !banList.contains (uname)) 
            banList.addElement (uname);
        else if (!on) 
            banList.removeElement (uname);
	}
	
	public boolean canSetBanForUser (String u) {
        if (!this.isValid())
            return false;
		String uname = u.toLowerCase();
        if (!banList.contains (uname)) 
            return true;
        return false;
	}
	
	public boolean getBanForUser (String u) {
        if (!this.isValid())
            return false;
		String uname = u.toLowerCase();
        if (!banList.contains (uname)) 
            return true;
        return false;
	}
	
    @SuppressWarnings("unchecked")
	public Vector<String> bannedUsers () {
		return  (Vector<String>) banList.clone();
	} 

    /**
	 * Check if the given user is banned
	 * @param u The user to check the ban-state for this room
	 * @return true if the user is banned, false if not
	 */
    public boolean usrIsBaned (User u) {
        return usrIsBaned(u.getName());
    }
    public boolean usrIsBaned (String u) {
        if (banList==null)
            return false;
        String uname = u.toLowerCase();
        return banList.contains (uname);
    }

	/**
	 * Checks if a specific user is insied this room
	 * @param u The user to search for
	 * @return true if the user is within this group, false if not
	 */
   public boolean usrIsPresent (User u) {
       if (usr==null)
           return false;
      return usr.contains (u);
   }

	/**
	 * Checks if the given user is in the SU-list for this group
	 * @param u The user to check for
	 * @return true if the user is contained in the SU-list, false if not
	 */
    public boolean usrIsSu (User u) {
        if (susers==null)
            return false;
        return susers.contains (u);
    }

	/**
	 * returns the number of superusers in this grou
	 * @return number of superusers in this group
	 */
	public int suUserCount () {
        if (susers==null)
            return 0;
		return susers.size();
	}
	
	/**
	 * returns the number of users within this group
	 * @return the number of users within this group
	 */
	public int size() {
        if (usr==null)
            return 0;
		return usr.size();
	}
    
    /**
     * set the min-right to join/open this group
     * @param r
     */
    public void setMinRight(int r) {
        this.minRight = r;
    }
    
    /**
     * set the memebership to join/open this group
     * @param membershipRoom
     */
    public void setMembershipRoom(Membership[] membershipRoom) {
        this.membershipRoom = membershipRoom;
    }
    
    /**
     * set the array with user-names which automatically get su-rights
     * @param usrs string-array containing the usernames which recieve su-rights on joining this group
     */
    public void setAutoSu (String[] usrs) {
        if (autoSuList == null)
            autoSuList = new Vector<String>();
        for (int i = 0; i < usrs.length; i++) {
            String usr = usrs[i].trim().toLowerCase();
            if (!autoSuList.contains(usr))
                autoSuList.add(usr);
        }
    }
    
    public Vector<String> getAutoSuList(){
    	return autoSuList;
    }
    
    public void unsetAutoSu () {
        autoSuList = new Vector<String>();
    }
    
    public void setAutoSuMembership(Membership[] autoSu) {
        this.autoSu = autoSu;
    }
    
    public Membership[] getAutoSuMembership(){
    	return this.autoSu;
    }
    
    public void setSuForbiddenMembership (String ship) {
    	suForbiddenMembership = ship ;
    }
    public String getSuForbiddenMembership (){
    	return suForbiddenMembership ;
    }
	/**
	 * checks if a state is true for this group
	 * @param state the state which must be given
	 * @return true if the state is given, false if not
	 */
    public boolean hasState (int state) {
        return (this.state & state) == state;
    }

	/**
	 * enables the given state
	 * @param state the state to enable
	 */
    public void setState (int state) {
        this.state = this.state | (state - (this.state & state));
    }

	/**
	 * disables the given state
	 * @param state the state to disable
	 */
    public void unsetState (int state) {
        this.state = this.state - (this.state & state);
    }
    
    public synchronized void setMembershipSu(Membership[] membershipSu) {
        this.membershipSu = membershipSu;
    }
   
    public synchronized void setMinRightSu (int minRight) {
        this.minSetSuRole = minRight;
    }
    
    public boolean hasMinRightSu (int minRight) {
  	    return (this.minSetSuRole  == minRight);
    }
    
    /**
     * determines if this group has the given right
     * @param right the right this group will be queried for
     * @return true if all flaggs in right are set for this group
     */  
    public boolean hasMinRight (int right) {
   	    return (this.minRight  == right);
    }
    
    public boolean isValid () {
        return valid;
    }
    
    public void invalidate() {
        usrArr=null;
        usr=null;
        susers=null;
        suForbiddenMembership = null;   
        banList=null;
        autoSuList=null;
        memberRoom = null;
        autoSu = null;
        membershipSu = null;
        membershipRoom = null;
        opener = null;
        saveTheme = null;
        lastXmlrpcMessage=null;
        themeCreator = null;
        valid=false;
    }
	/**
	 * checks if the given group is equal to this group
	 * @return true if the two groups are equal
	 */
    public boolean equals (Object g) {
        if (this == g)
            return true;
        if (g==null) 
            return false;
        if (!(g instanceof Group))
            return false;
        String n = ((Group) g).getKey ();
        if (n == null)
            return false;
        return (n.equalsIgnoreCase (this.key));
    }
    
    public String toString () {
        StringBuffer sb = new StringBuffer();
        sb.append ("[Group ");
        sb.append (key);
        sb.append (" / state: ");
        sb.append (state);
        sb.append ("]");
        return sb.toString();
    }
    
    public int hasCode() {
        return this.key.hashCode();
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
	/**
	 * @return Returns the timelockSec.
	 */
	public int getTimelockSec() {
		return timelockSec;
	}
	/**
	 * @param timelockSec The timelockSec to set.
	 */
	public void setTimelockSec(int timelockSec) {
		this.timelockSec = timelockSec;
	}
	
	public String getLastXmlrpcMessage() {
	    return lastXmlrpcMessage;
	}

	public void setLastXmlrpcMessage(String lastXmlrpcMessage) {
	    if (lastXmlrpcMessage.equals(this.lastXmlrpcMessage))
	        recivedLastXmlrpcMessage++;
	    else recivedLastXmlrpcMessage =0;
	    this.lastXmlrpcMessage = lastXmlrpcMessage;
    }
	
	public int getRecivedLastXmlrpcMessage(){
	    return recivedLastXmlrpcMessage;
	}
}