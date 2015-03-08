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

import freecs.content.*;
import freecs.*;
import freecs.interfaces.*;
import freecs.layout.*;
import freecs.util.EntityDecoder;

import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * the user prototype storing all userdata
 */
public class User implements IUserStates, IMessageDestination {
    private static final short LOGGING_IN = 0;
    private static final short LOGGED_IN = 1;
    private static final short SCHEDULED_FOR_REMOVAL = 2;
    private static final short SENDING_QUIT_MESSAGE = 3;
    private static final short LOGGING_OUT = 4;
    private static final short LOGGED_OUT = 5;
    
    private short state = -1;
	public boolean blocked = false, activated = true, isUnregistered = true, isTempadminhost = false, isCollocked=false, isAwaylocked=false, isMelocked=false;;
	private volatile boolean messageFloodLenght = false;
	private volatile boolean whisperDeactivated = false;
    private volatile String name, cookie, userAgent, colCode, bgcolCode, fadeColCode, awayMessage, id;
    private String customTitle;
	private volatile int permissionMap, defaultPermissionMap, defaultMembershipPermissionMap, hashCode=Integer.MIN_VALUE, 
                         flooded=0, questionCounter=0, tooled=0;
	private HashMap<String, Object>		userProps;
    private long        sessionStart;
    public  long        lastSentMessage;
    public  long        toolcontrol;
    private volatile long lastActive, removeWhen = 0,lastColChange = 0, awayStart, awayTime=0, lastFloodMessage = 0;
   	public volatile long lastRecievedMessage;
	private volatile Vector<Object> whisper, storeFriendNotification;
    private volatile Vector<String> blockedServerPlugins;
	private volatile StringBuffer fadeColorUsername;
	private volatile transient Group grp = null;
	private Vector<String>		ignoreList, friendsList, templatesetList, confirmAction;
	private volatile transient Group invitedTo;
	private volatile transient User invitedBy;
	private volatile boolean away = false, isPunished = false, isHTTP11 = true, friendsOnly = false;
	private Vector<MessageParser>		schedMsgs;
	private volatile SelectionKey	sk;
	private TemplateSet	ts;
	private volatile transient User ownPrivateUser = null,  // the last user this user whispered to 
                          foreignPrivateUser = null; // the last user whispering to this user
	private volatile transient Membership lastCalledMembership = null;
	public Connection	conn;

    private HashMap<String, Membership> memberships = new HashMap<String, Membership>();
    private Membership defaultMembership = null;
    private volatile Vector<PrivateMessageStore> privateMessage = null;
    private short friendNotification = 0;
    public static final short FN_NONE = 0;
    public static final short FN_FRIEND_AGREEMENT = 1;
    public static final short FN_ALL = 2;

   /**
    * constructor for user
    */
   public User(String name, String cookie) {
	  this.privateMessage        = new Vector<PrivateMessageStore>();
      this.colCode               = null;
      this.bgcolCode             = null;
      this.fadeColCode           = null;
      this.fadeColorUsername     = new StringBuffer();
      this.name                  = name;
      this.cookie                = cookie;
      this.userAgent             = null;
      this.userProps             = new HashMap<String, Object>();
      this.sessionStart          = System.currentTimeMillis ();
      this.lastRecievedMessage   = this.sessionStart;
      this.lastActive            = this.sessionStart;
      this.ignoreList            = new Vector<String> ();
      this.friendsList           = new Vector<String> ();
      this.templatesetList       = new Vector<String> ();
      this.confirmAction		 = new Vector<String>();
      this.schedMsgs             = new Vector<MessageParser> ();
      this.whisperDeactivated    = false;
      this.blockedServerPlugins = new Vector<String>();
      this.state                 = LOGGING_IN;
      ts = Server.srv.templatemanager.getTemplateSet("default");
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

	/**
	 * schedule a message, which will be displayed when the user
	 * enters the chat (e.g. you have vip-rights)
	 * @param mpr
	 */
   public void scheduleMessage (MessageParser mpr) {
      if (mpr == null) return;
      synchronized (this) {
          if (schedMsgs == null) 
              schedMsgs = new Vector<MessageParser> ();
          schedMsgs.addElement (mpr);
      }
   }

    /**
     * Sends all scheduled messages
     */
    public void sendScheduledMessages() {
        if (schedMsgs == null) return;
        for (Iterator<MessageParser> i = schedMsgs.iterator(); i.hasNext(); ) {
            MessageParser mpr = (MessageParser) i.next ();
            this.implSendMessage(mpr);
            i.remove();
        }
        schedMsgs=null;
    }

    public boolean isJoining () {
        return state == LOGGING_IN;
    }

	/**
	 * check if the user is in the removing-process
	 * @return true if this user is schedulte for removal, false if not
	 */
    public boolean isRemoving () { 
        return this.state==SCHEDULED_FOR_REMOVAL; 
    }

    /**
     * schedule this user to be removed (if user reconnects, we asume he was accidently disconnected
     * and reuse this User-Object. After a deffined time the user get's realy logged out)
     */
    public void scheduleToRemove () {
        if (this.state>=SCHEDULED_FOR_REMOVAL) 
            return;
        this.state=SCHEDULED_FOR_REMOVAL;
        StringBuffer tsb = new StringBuffer ("scheduleToRemove: ");
        tsb.append (name);
        if (conn != null) {
            tsb.append ("@");
            tsb.append (conn.toString());
        }
        Server.log ("[User " + name + "]", tsb.toString (), Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
        this.removeWhen = System.currentTimeMillis () + Server.srv.USER_REMOVE_SCHEDULE_TIME;
        if (sk!=null) {
            ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
            cb.invalidate();
        }
    }

    public synchronized void sendQuitMessage (boolean b, String param) {
        if (state>=SENDING_QUIT_MESSAGE)
            return;
        state = SENDING_QUIT_MESSAGE;
        // if the selectionkey is valid and the channel is open, try
        // to send quit message
        Group g = grp;
        if (grp != null) {
            grp = null;
            g.removeUser(this);
        }
        if (g != null && g.size() > 0 && g.isValid()) {
            MessageParser mp = new MessageParser ();
            mp.setSender (this);
            if (b) {
                mp.setMessageTemplate ("message.user.leaving.server.kicked");
            } else {
                if (isAway()){
                    mp.setMessageTemplate ("message.away.off");
                    g.sendMessage (mp);
                }
                mp.setReason(param);
                mp.setMessageTemplate ("message.user.leaving.server");
            }
            g.sendMessage (mp);
        }
        if (sk != null && sk.isValid() && sk.channel().isOpen()) try {
            MessageParser mp = new MessageParser ();
            mp.setSender (this);
            mp.setMessageTemplate (b ? "message.kh.personal" : "message.q");
            this.implSendMessage (mp);
            Server.log ("[User " + name + "]", "removeNow: sent quit-message", Server.MSG_STATE, Server.LVL_VERBOSE);
        } catch (Exception e) {
            Server.debug ("[User " + name + "]", "removeNow: ", e, Server.MSG_ERROR, Server.LVL_MINOR);
        } else {
            CentralSelector.dropKey(sk);
        }
        removeWhen = System.currentTimeMillis() + 1000;
    }

    /**
     * loggs out the user now. skipps scheduleToRemove or get's called 
     * because schedule-time has been reached
     */
    public synchronized void removeNow () {
        if (state>=LOGGING_OUT)
            return;
        state=LOGGING_OUT;
        removeWhen=System.currentTimeMillis() + Server.srv.USER_REMOVE_SCHEDULE_TIME;
        UserManager.mgr.removeUser (this);
        for (Iterator<Entry<String, Membership>> i = memberships.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Membership> entry = (Map.Entry<String, Membership>) i.next();
            Membership m = (Membership) entry.getValue();
            m.remove(this);
        }

        // if user has a group, send a message to this group to let other users know
        // this user has left the server
        if (grp != null) {
            Group g = grp;
            grp = null;
            g.removeUser(this);
            MessageParser mp = new MessageParser ();
            mp.setSender (this);
            if (this.isAway()){
            	mp.setTargetGroup(g);
                mp.setMessageTemplate ("message.away.off");
                g.sendMessage (mp);
            }
            mp.setMessageTemplate ("message.user.leaving.server");
            g.sendMessage (mp);
        }

        // logout the user
		Server.srv.removeTempAdminhost(this);
        try {
        	Server.srv.auth.logoutUser (this);
        } catch (Exception e) {
            Server.debug ("[User " + name + "]", "removeNow: Exception during logout", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        for (Enumeration<String> e = friendsList.elements (); e.hasMoreElements (); ) {
            String fname = (String) e.nextElement ();
            UserManager.mgr.removeFriendship (this, fname);
        }
        templatesetList = null;
        fadeColorUsername = null;
        privateMessage = null;
        confirmAction = null;

        StringBuffer tsb = new StringBuffer ("logged out: ");
        tsb.append (name);
        if (conn != null) {
            tsb.append ("@");
            tsb.append (conn.toString ());
        }
        Server.log ("[User " + name + "]", tsb.toString (), Server.MSG_AUTH, Server.LVL_MINOR);
        Server.srv.removeToken(cookie);
        this.state=LOGGED_OUT;
    }
    
   /**
    * return the time the user has to be logged out
    */
   public long getRemoveWhen () {
      return removeWhen;
   }

   /**
    * keeps the lastActive time fresh and checks for flooding
    * @return false if the user has flooded or is finalizing
    */
    public synchronized boolean wasActive () {
        if (state>=LOGGING_OUT)
            return false;
        if (removeWhen != 0 || state==SCHEDULED_FOR_REMOVAL) {
            removeWhen = 0;
            state=LOGGED_IN;
        }
        long currTime = System.currentTimeMillis ();
        if ((currTime - this.lastActive) < Server.srv.FLOOD_PROTECT_MILLIS) {
            flooded++;
            if (flooded > Server.srv.FLOOD_PROTECT_TOLERANC) {
                Server.srv.banUser (this, "message.user.flooded", null, Server.srv.FLOOD_BAN_DURATION, "FloodProtection");
                return false;
            }
        } else
        	flooded = 0;
              
        long difference = toolcontrol- (currTime - this.lastActive);
        if (difference <0 ) difference = difference *-1;
        if (tooled >0)    
        	Server.log("[User " + name  + "]","TOK: " + toolcontrol + " <> " + (currTime - this.lastActive)+ " = " + difference + " Toleranc: " + Server.srv.TOOL_PROTECT_TOLERANC + " Counter: " + tooled , Server.MSG_STATE, Server.LVL_MINOR);
        if ((currTime - this.lastActive) * Server.srv.TOOL_PROTECT_MINCOUNTER > Server.srv.TOOL_PROTECT_MINMILLS) {
        	Server.log("[User " + name  + "]","TOK: "+ (currTime - this.lastActive) * Server.srv.TOOL_PROTECT_MINCOUNTER+ " <> " + Server.srv.TOOL_PROTECT_MINMILLS , Server.MSG_STATE, Server.LVL_MINOR);
        	if (difference <= Server.srv.TOOL_PROTECT_TOLERANC) {
        		tooled++;
        		if (tooled >= Server.srv.TOOL_PROTECT_COUNTER) {
        			Server.srv.banUser (this, "message.user.tooled", null, Server.srv.TOOL_BAN_DURATION, "ToolProtection");
        			return false;
        		}
        	} else 
        		tooled = 0;
        }
        toolcontrol = currTime - this.lastActive;
        this.lastActive = currTime;
        return true;
    }

   /**
    * sets the group in which this user joined
    * @param newgrp the group this user has joined
    */
	public synchronized boolean setGroup (Group newgrp) {
		if (newgrp == null)
            return false; 
        if (this.grp != null)
            this.grp.removeUser (this);
		this.grp = newgrp;
		questionCounter = 0;
        return true;
   }

   /**
    * gets the group of this user
    * @return the group this user is a member from
    */
   public Group getGroup () {
      return grp;
   }

   /**
    * gets the cookie of this user
    * @return this users cookie
    */
   public String getCookie () {
      return cookie;
   }

   /**
    * gets the last-active-time of this user
    * @return last activity as long value
    */
   public long lastActive () {
      return lastActive;
   }

   /**
    * Sets the SelectionKey of the message-output-frame of this user
    * @param sk the SelectionKey for this user's responder
    */
   public synchronized void setKey (SelectionKey sk) {
      if (!CentralSelector.isSkValid(sk)) {
          Server.log (this, "tryed to set invalid key", Server.MSG_STATE, Server.LVL_MINOR);
          this.scheduleToRemove(); 
          return;
      }
      this.sk = sk;
      ConnectionBuffer cb = (ConnectionBuffer) sk.attachment ();
      cb.setUser (this);
      conn = cb.conn;
      state=LOGGED_IN;
   }
   
   /**
    * returns the SelectionKey of this users message-output-frame
    * @return SelectionKey of this users message-output-frame
    */
   public SelectionKey getKey () {
      return sk;
   }


   /**
    * Interface IMessageDestination
    */

    /**
     * touches this user to keep proxies from closing the connection for this user
     */
    public synchronized void touch (long now) {
        if (state!=LOGGED_IN) {
            return;
        }
        if (sk == null) {
            scheduleToRemove();
            return;
        }
        if (!sk.isValid () || !sk.channel ().isOpen ()) {
            Server.log ("[User " + name + "]", "touch: droped key", Server.MSG_STATE, Server.LVL_VERBOSE);
            CentralSelector.dropKey (sk);
            return;
        }
        long diff = now - lastRecievedMessage; 
        if (diff < Server.srv.TOUCH_USER_DELAY) {
            return;
        }
        try {
            ByteBuffer clone = ByteBuffer.wrap(UserManager.mgr.TOUCH_CONTENT);
            Server.log ("[User " + name + "]", "touch", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
            ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
            cb.addToWrite (clone);
            lastRecievedMessage = now;
        } catch (Exception e) {
            Server.debug ("[User " + name + "]", "touch: catched exception during touch", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
    }

   /**
    * sends the message to this user
    * @param mc the message-container
    */
	public void sendMessage (IContainer mc) {
		if (mc==null) 
			return;
		if (state!=LOGGED_IN) 
			return;
		IGroupPlugin[] plugins = null;
		if (mc instanceof MessageParser) {
			User sender = ((MessageParser)mc).getSender();
            if (sender != null && sender.getGroup() != null)
    			plugins = sender.getGroup().getPlugins();
	      	if (sender!= null && ignoreList.contains(sender.getName().toLowerCase()) && !sender.hasRight(IUserStates.ROLE_VIP))
	         	return;
			if (sender != null && !sender.equals(this) && (!sender.hasRight(IUserStates.ROLE_VIP) || !sender.hasDefaultRight(IUserStates.ROLE_VIP))) {
			    if (isFriendsOnly() && !getGroup().hasState(IGroupState.ENTRANCE)){
			        if (!friendsList.contains(sender.getName().toLowerCase()))
			        return;
			    }
			}
		}
		if (state!=LOGGING_IN && sk == null) {
			Server.log("[User " + name + "]", "sendMessage: selectionkey was null", Server.MSG_STATE, Server.LVL_MINOR);
			scheduleToRemove ();
			return;
		} else if (sk==null)
			return;
		
        PluginRenderer rp = new PluginRenderer();
        rp.checkUsrAction(mc, plugins, this);
        plugins =null;

        implSendMessage(mc);
	}
    
    private void implSendMessage (IContainer mc) {
        if (mc instanceof PersonalizedMessage) {
            ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
            cb.addToWrite(mc.getByteBuffer());
            if (mc.closeSocket())
                cb.addToWrite(Responder.CLOSE_CONNECTION);
            lastRecievedMessage = System.currentTimeMillis ();
            return;
        }
        ((MessageParser) mc).setHTTP11 (isHTTP11 && Server.srv.USE_HTTP11);
        ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
        if (!((MessageParser) mc).addPersonalizedMessage (this, cb, blockedServerPlugins)) {
            Server.log ("[User " + name + "]", "sendMessage: there was nothing to send", Server.MSG_TRAFFIC, Server.LVL_VERY_VERBOSE);
            return;
        }
        lastRecievedMessage = System.currentTimeMillis ();
    }
    
    public Vector<String> getBlockedServerPlugins() {
        return blockedServerPlugins;
    }

    public void addBlockedServerPlugins(String pluginname) {
        this.blockedServerPlugins.add(pluginname.toLowerCase());
    }
    
    public void removeBlockedServerPlugins(String pluginname) {
        while (this.blockedServerPlugins.contains(pluginname.toLowerCase()))
            this.blockedServerPlugins.remove(pluginname.toLowerCase());
    }


   /**
    * return an Enumeration containing only this user
    */
   public Iterator<Object> users () {
      return new Iterator<Object> () {
         boolean userNotReturned = true;
         public boolean hasNext () {
            return userNotReturned;
         }
         public Iterator<Object> next () {
            userNotReturned = false;
            return this;
         }
         public void remove () { }
      };
   }

   /**
    * checks if the given user is ignored by this user
    * @param u the user to check if it is ignored by this user
    */
   public boolean userIsIgnored (String uname) {
      return ignoreList.contains (uname.toLowerCase());
   }

   /**
    * this user will ignore the given user if called
    * @param u the user to be ignored by this user
    */
	public void ignoreUser (User u) {
		String uname = u.getName().toLowerCase();
        ignoreUser(uname);
	}
    
    /**
     * add the username to the ignorelist of this user
     * @param uname the username to add to this user's ignorelist
     */
    public void ignoreUser (String uname) {
        if (ignoreList.contains (uname)) 
            return;
/*      WE MUST NOT remove the user from the invitedBy-state
        This would give the anoying user possibility to reinvite this user
        if (invitedBy != null && invitedBy.equals (u)) {
            invitedBy = null;
            invitedTo = null;
        } */
        ignoreList.addElement (uname);
    }

   /**
    * this user will no longer ignore the given user
    * @param u the user to respect again
    */
	public void respectUser (String uname) {
	
		while (ignoreList.contains (uname)) ignoreList.removeElement (uname);
   }
	
    /**
     * returns the number of ignored user of this user
     * 
     * @return the number of ignored user  of this user
     */
    public int numberOfIgnoredUser() {
        return ignoreList.size();
    }

   /**
    * sets the id of this user
    * @param id the id of this user
    */
   public void setID (String id) {
      this.id = id;
   }

   /**
    * gets the id of this user
    * @return the id of this user
    */
   public String getID () {
      return id;
   }

// ***************************** STATE-QUERY and SET
   /**
    * sets right for this user
    * @param right the rightset for this user
    * @see freecs.interfaces.IUserStates
    */
    public void setPermission (int right) {
       this.permissionMap = right;
       this.defaultPermissionMap = right;
    }
    public void setDefaultPermissionMap (int right) {
          this.defaultPermissionMap = right;
    }
    public void setDefaultMembershipPermission (int right) {
        this.defaultMembershipPermissionMap = right;
    }
    public int getPermissionMap () {
        return this.permissionMap;
    }
    public int getDefaultMembershipPermissionMap () {
        return this.defaultMembershipPermissionMap;
    }
    public int getDefaultPermissionMap () {
        return this.defaultPermissionMap;
    }

    public void setNewPermission (int right) {
        this.permissionMap = right;
    }
    
    public void resetPermission () {
    	this.permissionMap = this.defaultPermissionMap;
    }

    public synchronized void addMembership (String key, Membership m) {
        if (defaultMembership == null)
            defaultMembership = m;
        memberships.put(key, m);
    }
    
    public Membership getMembership (String key) {
        return (Membership) memberships.get(key);
    }
    
    public Membership getDefaultMembership () {
        return defaultMembership;
    }
    
    public void setDefaultMembership (Membership m) {
        defaultMembership = m;
    }

    public synchronized void removeMembership (String key) {
        memberships.remove(key);
        rebuildMemberships();
    }
    
    public synchronized void rebuildMemberships () {
        for (Iterator<Entry<String, Membership>> i = memberships.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Membership> entry = (Map.Entry<String, Membership>)i.next();
            Membership m = (Membership) entry.getValue();
            m.add(this);
        }
    }
    
    public void addIsModerator(){
    	setPermission(getPermissionMap()+ IUserStates.IS_MODERATOR);
    }
    
    public void removeIsModerator(){
   	    setPermission(getPermissionMap()- IUserStates.IS_MODERATOR);
    }
    
    public void addIsGuest(){
   	    setPermission(getPermissionMap()+ IUserStates.IS_GUEST);
    }
   
    public void removeIsGuest(){
  	    setPermission(getPermissionMap()- IUserStates.IS_GUEST);
    }

   /**
    * grants a specific right to this user additional to previous ones
    * @param right
    * @see freecs.interfaces.IUserStates
    */
   public void givePermission (int right) {
      this.permissionMap = this.permissionMap | (right - (this.permissionMap & right));
   }

   /**
    * removes right of this user
    * @param right
    * @see freecs.interfaces.IUserStates
    */
   public void takePermission (int right) {
      this.permissionMap = this.permissionMap - (this.permissionMap & right);
   }

   /**
    * determines if this user has the given right
    * @param right the right this user will be queried for
    * @return true if all flaggs in right are set for this user
    */
   public boolean hasRight (int right) {
      return ((this.permissionMap & right) == right);
   }

   public boolean hasRole (int role) {
       int clean = 0;
       if (role == IUserStates.ROLE_GOD){
           clean = this.permissionMap - (this.permissionMap & (IUserStates.IS_GUEST | IUserStates.IS_MODERATOR));
       } else clean = this.permissionMap - (this.permissionMap & (IUserStates.IS_GUEST | IUserStates.IS_MODERATOR | IUserStates.MAY_SET_THEME | IUserStates.MAY_CALL_MEMBERSHIPS |  IUserStates.MAY_USE_SMILEY ));
       return clean == role;
   }
   
   public boolean hasEntraceMinRight(int minRight, MessageState msgState, Group sg){
       if (minRight == IUserStates.ROLE_USER || this.hasRole(IUserStates.ROLE_GOD))
           return true;
       if (minRight == IUserStates.ROLE_VIP){
           if (!this.hasRight(IUserStates.ROLE_VIP)) {
               msgState.msgTemplate = "error.noRight.noVipAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       if (minRight == IUserStates.ROLE_GOD){
           if (!this.hasRight(IUserStates.ROLE_GOD)) {
               msgState.msgTemplate = "error.noRight.noAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       return true; 
   }

   public boolean hasSepaMinRight(int minRight, MessageState msgState){
       if (minRight == IUserStates.ROLE_USER || this.hasRole(IUserStates.ROLE_GOD))
           return true;
       if (minRight == IUserStates.ROLE_VIP){
           if (!this.hasRight(IUserStates.ROLE_VIP)) {
               msgState.msgTemplate = "error.noRight.noVipAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       if (minRight == IUserStates.ROLE_GOD){
           if (!this.hasRight(IUserStates.ROLE_GOD)) {
               msgState.msgTemplate = "error.noRight.noAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       return true; 
   }
   
   public boolean hasMinRight(int minRight, MessageState msgState, Group sg){
       if (minRight == IUserStates.ROLE_USER || this.hasRole(IUserStates.ROLE_GOD))
           return true;
       if (minRight == IUserStates.ROLE_VIP){
           if (!this.hasRight(IUserStates.ROLE_VIP)) {
               msgState.msgTemplate = "error.noRight.noVipAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       if (minRight == IUserStates.ROLE_GOD){
           if (!this.hasRight(IUserStates.ROLE_GOD)) {
               msgState.msgTemplate = "error.noRight.noAdmin";
               this.sendMessage (msgState.mp);
               return false;
           }
       }
       return true; 
   }

   public boolean hasDefaultRight (int right) {
       return ((this.defaultPermissionMap & right) == right);
   }
   
   public boolean hasMobileBrowser(){   
       if (this.getUserAgent() == null)
           return false;
       StringBuilder c_input = new StringBuilder(Server.srv.MOBILE_BROWSER_REGEX);
       c_input.trimToSize();
       Pattern p = Pattern.compile(c_input.toString().toLowerCase());
       Matcher m = p.matcher(this.getUserAgent().toLowerCase());
       if (m.find()){
           Server.log(this, "found Mobile Browser ["+m.group() +"] ("+this.getUserAgent()+")", Server.MSG_TRAFFIC, Server.LVL_MAJOR);
           return true;
       } else return false;
   }

   /**
    * set away-state
    * @param b true if user is away; false otherwhise
    */
   public void setAway (boolean b) {
       if (b) { 
	       awayStart = System.currentTimeMillis ();
	       whisper = new Vector<Object>();
	       storeFriendNotification = new Vector<Object>();
	   } else {
	       awayTime += System.currentTimeMillis () - awayStart;
	   }
	   away = b;
   }
   
   public void storeFriendNotification(String u){
	   Object o = UserManager.mgr.getUserByName(u);
       if (o == null)
           o = u.toString();
	   storeFriendNotification.add(o);
   }
   
   public Vector<Object> getStoreFriendNotification(){
	   return storeFriendNotification;
   }
   
   public void addWhisper(Object u) {
       Object o = UserManager.mgr.getUserByName(u.toString());
	   if (o == null)
	       o = u.toString();
	   whisper.add(o);
   }
	   
   public Vector<Object> whisper() {
       return whisper;
   }
   
   public int getWhipserSize(){
       if (whisper == null)
           return 1;
       int size = whisper.size();
       return size+1;
   }
   
   public synchronized void addPrivateMessageStore(PrivateMessageStore ps){
       if (Server.srv.MAX_PMSTORE == 0)
           return;
       privateMessage.add(ps);     
       int max = Server.srv.MAX_PMSTORE;
       if (defaultMembership != null && defaultMembership.getPrivatemessageStore() >0)
           max = defaultMembership.getPrivatemessageStore();
       if (max > 50)
           max = 50;
       int diff = privateMessage.size()-max;
       if (diff > 0){
           int i = 0;
           Vector<PrivateMessageStore> tmp = new Vector<PrivateMessageStore> (privateMessage);
           for (Iterator<PrivateMessageStore> p = tmp.iterator(); i<=diff;) {
               PrivateMessageStore pm = (PrivateMessageStore) p.next();
               i++;
               privateMessage.remove(pm);             
           }
       }                    
   }

   public void sendMessageHistory(int size){
       int i = 0;
       int psize = privateMessage.size();
       for (Iterator<PrivateMessageStore> p = privateMessage.iterator(); p.hasNext();) {
           PrivateMessageStore ps = (PrivateMessageStore) p.next();                   
           if ((psize -i) <= size){
               MessageParser mp = new MessageParser();
               mp.setSender(ps.sender);
               mp.setUsercontext(ps.sender);
               mp.setMessage(ps.message);
               mp.setMessageTemplate("message.m");
               sendMessage(mp);
           }
           i++;
       }
   }
   
   public void clearPrivatMessageStore() {
       privateMessage = null;
   }

   /**
    * set away-state
    * @return away true if user is away; false otherwhise
    */
   public boolean isAway () {
      return away;
   }
   
   /**
    * returns the time this user switched to the away-state
    * @return the away-time of this user
    */
   public long awayTime () {
   	  return awayTime;
   }

	/**
	 * checks if this user is punished
	 * @return true if the user is punished
	 */
   public boolean isPunished () {
      return isPunished;
   }

	/**
	 * sets the punishment-state of this user
	 * @param p if true, the user will be switched into punished mode
	 */
   public void setPunish (boolean p) {
      isPunished = p;
   }
   
   public void setCollock (boolean p) {
       isCollocked = p;
   }

   public void setAwaylock (boolean p) {
       isAwaylocked = p;
   }
   
   public void setActlock (boolean p) {
       isMelocked = p;
   }

	/**
	 * sets the invitation of a user for this user
	 * @param u the user inviting this user
	 * @return true if the user doesn't ignore the other user
	 */
   public boolean invitedFrom (User u) {
      if (userIsIgnored(u.getName()))
    	  return false;
      this.invitedTo = u.getGroup ();
      this.invitedBy = u;
      return true;
   }

	/**
	 * unsets the invitation-state of this user
	 */
   public void unsetInvitedTo () {
      invitedBy = null;
      invitedTo = null;
   }

	/**
	 * returns the user which has invited this user
	 * @return the inviter of this user
	 */
   public User invitedBy () {
      return invitedBy;
   }

	/**
	 * returns the group to which this user was invited to
	 * @return the group this user is invited to
	 */
   public Group invitedTo () {
      return invitedTo;
   }

	/**
	 * get the color-code for this user
	 * @return the color-code for this user
	 */
   public String getColCode () {
      return colCode;
   }

	/**
	 * Set the colorcode for this user. This method is only for internal
	 * use (e.g. assigning a colorcode automatically from the database). A
	 * colorchange triggered by the user must be done by changeColCode 
	 * @param cCode the colorcode this user will recieve
	 */
   public void setColCode (String cCode) {
      colCode = cCode;
   }
   
   public void setBgColCode (String bgcCode) {
       bgcolCode = bgcCode;
   }
  
   public String getBgColCode () {
       return bgcolCode;
   }
   
   public void setFadeColCode (String fcCode) {
       fadeColCode = fcCode;
       if (fcCode == null)
    	   fadeColorUsername = null;
   }
   
   public void setFadeColorUsername (StringBuffer fu){
 	   fadeColorUsername = new StringBuffer(fu);
   }
   
   public String getNoFadeColorUsername(){
	   StringBuffer link = new StringBuffer("<span style=\"color:#");
	   link.append(colCode);
	   link.append("\">").append(EntityDecoder.charToHtml(name)).append("</span>");
	   return link.toString();
   }
   
   public StringBuffer getFadeColorUsername (){
	   return fadeColorUsername;
   }
   
   
   public String getFadeColCode () {
       return fadeColCode;
   }
   /**
    * This is used for user-triggered colorchanges.
    * @param cCode the color-code wanted
    * @return true if allowed, if not false
    */
   public boolean changeColCode (String cCode) {
        long now = System.currentTimeMillis ();
        if ((now - lastColChange) < Server.srv.COLOR_CHANGE_INTERVAL) 
            return false;
        colCode = cCode;
        fadeColCode = null;
        fadeColorUsername = null;
        lastColChange = now;
        return true;
    }

	/**
	 * returns the templateset this user has choosen
	 * @return the templateset this user has choosen
	 */
   public TemplateSet getTemplateSet () {
      if (this.ts == null) ts = Server.srv.templatemanager.getTemplateSet ("default");
      return ts;
   }

	/**
	 * set the templateset to use for this user
	 * @param ts the templateset to use
	 */
   public void setTemplateSet (TemplateSet ts) {
      this.ts = ts;
   }

	/**
	 * return the value of the given property of this user
	 * @param k the name of the property
	 * FIXME: this should also use the templateset of this user
	 * @return the value of the property of this user
	 */
   public Object getProperty (String k) {
      if (k.equals ("isaway")) {
         return (away ? "away" : "");
      } else if (k.equals("awaymessage")) {
      	 if (!away) 
      	 	return ("");
      	 String msg = getAwayMessage();
      	 if (msg.equals(""))
      	 	return (Boolean.FALSE);
		 return (msg);
      } else if (k.equals ("sessionstart.hour")) {
         return Server.formatTimeStamp (sessionStart, Server.hourSDF);
      } else if (k.equals ("sessionstart.minute")) {
         return Server.formatTimeStamp (sessionStart, Server.minuteSDF);
      } else if (k.equals ("idletime")) {
         long diff = System.currentTimeMillis () - lastActive;
         diff = Math.round (diff / 1000);
         return (String.valueOf (diff));
      } else if (k.equals ("sessiontime")) {
         long l = (System.currentTimeMillis () - sessionStart) / 1000 / 60;
         StringBuffer tsb = new StringBuffer ();
         if (l > 60) {
            long stdn = l / 60;
            if (stdn == 1)
               tsb.append ("einer Stunde ");
            else {
               tsb.append (stdn);
               tsb.append (" Stunden ");
            }
            tsb.append (l % 60);
            tsb.append (" Minuten");
         } else if (l == 1) {
            tsb.append ("einer Minute");
         } else {
            tsb.append (l);
            tsb.append (" Minuten");
         }
         return tsb.toString ();
      }
      return userProps.get (k);
   }

	/**
	 * set the property of this user to the value given
	 * @param k the property to set for this user
	 * @param v the value for this property of this user
	 */
   public void setProperty (String k, Object v) {
      userProps.put (k, v);
   }

	/**
	 * retuns the timestamp this user logged in
	 * @return the timestamp
	 */
   public long getSessionStart () {
      return sessionStart;
   }

	/**
	 * set the HTTP/1.1 capability of this user
	 * @param b true if this users client is HTTP/1.1 capable
	 */
   public void setHTTP11 (boolean b) {
      isHTTP11 = b;
   }

   /**
    * get the name of this user
    * @return string with username
    */
   public String getName () {
      return (name);
   }

	/**
	 * adds a friend relation for this user
	 * @param fname the name of the friend
	 */
    public void addFriend (String fname) {
        if (friendsList.contains (fname)) return;
        UserManager.mgr.addFriendship (this, fname);
        friendsList.addElement (fname);
    }
  
    public void removeFriend (String fname) {
        if (!friendsList.contains (fname)) return;
        UserManager.mgr.removeFriendship(this, fname);
        friendsList.removeElement(fname);
    }
   /**
    * returns the number of friends of this user
    * @return the number of freinds of this user
    */
   public int numberOfFriends () {
      return friendsList.size ();
   }
   
   public boolean isFriend (User u) {
       return isFriend (u.name);
   }
   public boolean isFriend (String name) {
       String n = name.trim().toLowerCase();
       return this.friendsList.contains(n);
   }
   
   /**
    * returns an Enumeration containing all friends of this user.
    * @return Enumeration contaioning all friends of this user
    */
   public Enumeration<String> friends () {
      return friendsList.elements ();
   }
   
   public boolean isFriendsOnly() {
       return friendsOnly;
   }

   public void setFriendsOnly(boolean friendsOnly) {
       this.friendsOnly = friendsOnly;
   }

   /**
    * returns an Enumeration containing all friends of this user.
    * @return Enumeration contaioning all ignored user of this user
    */
   public Enumeration<String> ignoreList () {
      return ignoreList.elements ();
   }

	/**
	 * give a message when the user-object gets finalized by the garbage-collector
   public void finalize () {
      Server.log ("[User " + name + "]", "got finalized **************", Server.MSG_STATE, Server.LVL_VERBOSE);
   }
     */
   
	public boolean equals (User u) {
		if (u==null) return false;
		if (id != null && id.equals(u.getID ())) return true;
		if (name.equalsIgnoreCase (u.getName ())) return true;
		return (false);
   }

   public int hashCode () {
      if (hashCode != Integer.MIN_VALUE)
         return hashCode;
      if (id != null)
         hashCode = id.hashCode();
      else
         hashCode = name.toLowerCase().hashCode ();
      return (hashCode);
   }

	/**
	 * returns the number of questions this user has asked within this group
	 * @return the number of questions this user has asked within this group
	 */
   public int getQuestionCounter () {
      return questionCounter;
   }
   /**
    * increments the questioncounter of this user
    */
   public void incrementQuestionCounter () {
      questionCounter++;
   }
   
   /**
    * returns true if this userobject is logged out
    * @return true if this userobject is logged out
    */
   public boolean isLoggedOut () {
   	  return state==LOGGED_OUT;
   }
   
   /**
    * returns true if this userobject is logged in
    * @return true if this userobject is logged in
    */
   public boolean isLoggedIn () {
      return state==LOGGED_IN;
   }

   /**
    * stores the last user to which this user sent a private-message
    * @param pu the user who sent the private-message to this user 
    */
   public void setPrivateUser (User pu) {
      this.ownPrivateUser = pu;
   }
   
   /**
    * returns the stored user to which this user sent a private message last time
    * @return the stored user to which this user sent a private message last time
    */
   public User getPrivateUser () {
      return ownPrivateUser;
   }
   
   /**
	* stores the last user which sent a private-message to this user
	* @param pu the user who sent the private-message to this user 
	*/
   public void setForeignPrivateUser (User pu) {
	  this.foreignPrivateUser = pu;
   }

   /**
	* returns the stored user which sent a private message to this user last time
	* @return the stored user which sent a private message to this user last time
	*/
   public User getForeignPrivateUser () {
       return foreignPrivateUser;
   }
   
   public void setLastCalledMembership(Membership cms){
       this.lastCalledMembership = cms;
   }
   
   public Membership getLastCalledMembership(){
       return this.lastCalledMembership;
   }
   
   public boolean hasMembership(String key){
       if (getMembership(key)!= null)
           return true;
       return false;
   }
   
   public boolean usrMayWhisper(User u) {
       if (this.grp==null)
           return true;
       if (this.hasRight(IUserStates.IS_GUEST) 
		    && this.grp.hasState(IGroupState.SND_PRF_GUEST))
           return false;
       if (this.hasRight(IUserStates.ROLE_GOD)
            && this.grp.hasState(IGroupState.SND_PRF_GOD))
           return false;
       if (this.hasRight(IUserStates.IS_MODERATOR)
            && this.grp.hasState(IGroupState.SND_PRF_MODERATOR))
           return false;
       if (this.hasRight(IUserStates.ROLE_VIP)
            && this.grp.hasState(IGroupState.SND_PRF_VIP))
           return false;
       if (this.hasRight(IUserStates.ROLE_USER)
            && this.grp.hasState(IGroupState.SND_PRF_USER))
           return false;
       return true;
    }
   
	/**
	 * checks the references of this user and unsets them if
	 * the user referd to is logged out
	 */
	public void checkReferences() {
		if (foreignPrivateUser != null && foreignPrivateUser.isLoggedOut())
			foreignPrivateUser = null;
		if (ownPrivateUser != null && ownPrivateUser.isLoggedOut()) {
            User opu = UserManager.mgr.getUserByName(ownPrivateUser.name);
            if (opu != null)
                ownPrivateUser = opu;
        }
		if (invitedBy != null && invitedBy.isLoggedOut()) {
			invitedBy = null;
			invitedTo = null;
		}
	}

	/**
	 * Stores the message given by the command /away do display it, when the user
	 * returns from away-state (or when /w is called for this user...)
	 * @param param
	 */
	public void setAwayMessage(String param) {
		awayMessage = param;
	}
	/**
	 * returns the stored awaymessage
	 * @return the stored awaymessage
	 */
	public String getAwayMessage () {
		return awayMessage == null ? "" : awayMessage;
	}
    
    /**
     * calculate the chattime including the current session
     * @return the calculated chattime
     */
    public long getChattime () {
        Object ct = getProperty ("chattime");
        if (ct == null) {
            return ((lastActive () - getSessionStart ()) - awayTime ()) / 1000; 
        }
        return (((lastActive () - getSessionStart ()) - awayTime ()) / 1000) + ((Long) ct).longValue ();
    }
    
    public void setFriendsNotification (short mode) {
        this.friendNotification=mode;
    }
    public short notifyFriends() {
        return this.friendNotification;
    }
    
    
    /**
     * set the custom-title
     * @param et the custom-title
     */
    public void setCustomTitle (String et) {
        customTitle = et;
    }
    
    /**
     * get the custom-title which was fetched from db on login
     * @return custom-title
     */
    public String getCustomTitle () {
        return customTitle;
    }

    public synchronized long nextCheck() {
        long lowestValue = lastActive + (away ? Server.srv.USER_AWAY_TIMEOUT : Server.srv.USER_TIMEOUT);
        if ((state==LOGGING_OUT || state==SCHEDULED_FOR_REMOVAL)
            && removeWhen != 0 && removeWhen < lowestValue)
            lowestValue = removeWhen;
        return lowestValue;
    }

    /**
     * returns true, if this user is valid. Validity is determined in a "soft" way,
     * meaning, that users, which don't have a connection at the moment,
     * will also be checked for their removeWhen-Timestamp and will stay valid, until
     * their removeWhen-Timestamp is reached.
     * @return true if valid, false if not
     */
    public synchronized boolean check(long now) {
        switch (state) {
            case LOGGING_IN:
                long laDiff = now-this.lastActive;
                long seDiff = now-this.sessionStart;
                if (laDiff < Server.srv.USER_REMOVE_SCHEDULE_TIME*10
                    || seDiff < Server.srv.USER_REMOVE_SCHEDULE_TIME*10)
                    return true;
                else
                    return false;
            case SCHEDULED_FOR_REMOVAL:
                if (this.removeWhen==0) {
                    state=LOGGED_IN;
                    return true;
                }
                return this.removeWhen > now;
            case SENDING_QUIT_MESSAGE:
                return this.removeWhen > now;
            case LOGGING_OUT:
                return this.removeWhen > now;
        }

        long userTimout = Server.srv.USER_TIMEOUT;
        long userAwayTimeout =  Server.srv.USER_AWAY_TIMEOUT;
        
        long tot = lastActive + (away ? userAwayTimeout : userTimout);
        
        if (userAwayTimeout <0 && away)
            tot = Long.MAX_VALUE;
             
        if (this.hasRight(IUserStates.ROLE_VIP)) {
            long vipTimeout = Server.srv.VIP_TIMEOUT;
            if (Server.srv.VIP_TIMEOUT < 0) {
                tot = Long.MAX_VALUE;
            } else if (Server.srv.VIP_TIMEOUT > 0) {
                long vipAway = Server.srv.VIP_AWAY_TIMEOUT;
                if (vipAway < vipTimeout && vipAway > 0) {
                    vipAway = vipTimeout;
                }

                tot = lastActive + (away ? vipAway : vipTimeout);
                if (vipAway < 0 && away) {
                    tot = Long.MAX_VALUE;
                }
            }
        }
        if (this.getDefaultMembership() != null) {
			if (this.getDefaultMembership().userTimeout() < 0)
				tot = Long.MAX_VALUE;
			if (this.getDefaultMembership().userTimeout() > 0)
				tot = lastActive
						+ (this.getDefaultMembership().userTimeout() * 60000);
		}
        if (grp==null 
            || !grp.isValid() 
            || tot <=now) {
            this.sendQuitMessage(false, null);
        } else if (sk == null 
            || !sk.isValid() 
            || !sk.channel().isOpen())
            this.scheduleToRemove();
        return true;
    }

    public String toString() {
        StringBuffer tsb = new StringBuffer ("[User ");
        tsb.append (name);
        tsb.append (" [cookie=");
        tsb.append (cookie);
        tsb.append (" / state=");
        tsb.append (state);
        tsb.append (" / grp: ");
        tsb.append (grp);
        tsb.append (" / sk: ");
        if (sk!=null) {
            tsb.append ("true valid? ");
            tsb.append (sk.isValid());
            tsb.append (" open? ");
            tsb.append (sk.channel().isOpen());
        } else {
            tsb.append ("false");
        }
        tsb.append ("]]");
        return tsb.toString();
   }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
    
    /**
     * Compares the Roles of two users:<br><br>
     * returns:<br>
     *  2,3 not the same user and u is lower than 'this'<br>
     * -1 u is lower than 'this'<br>
     *  0 u is equal with 'this'<br>
     *  1 u is higher than 'his'<br>
     * @param u
     * @return
     */
    public int compareRoleTo(User u) {
    	int defaultright = this.getDefaultPermissionMap();
    	if (this.hasRole(IUserStates.ROLE_GOD)) {
            if (u.hasRole(IUserStates.ROLE_GOD))
                return 0;
        } else if (this.hasRole(IUserStates.ROLE_VIP)) {
            if (u.hasRole(IUserStates.ROLE_GOD))
                return 1;
            else if (u.hasRole(IUserStates.ROLE_VIP))
                return 0;
            else if (u.hasRole(IUserStates.ROLE_USER))
                return 2;
        } else if (this.hasRole(IUserStates.ROLE_USER)) {
        	if (!u.equals(this) && u.hasRole(IUserStates.ROLE_USER) && !u.hasRight(defaultright)) 
            	return 2;
        	if (!u.equals(this) && u.hasRole(IUserStates.ROLE_VIP) && !u.hasRight(defaultright)) 
            	return 3;
        	if (u.hasRole(IUserStates.ROLE_GOD))
                return 1;
            if (u.hasRole(IUserStates.ROLE_VIP))
                return 1;
            else if (u.hasRole(IUserStates.ROLE_USER))
                return 0;
        } else {
            // this must be an asshole
        	if (u.hasRole(IUserStates.ROLE_ASSHOLE) && defaultright == IUserStates.ROLE_USER )
                return 2;
            if (u.hasRole(IUserStates.ROLE_ASSHOLE))
                return 0;
            else
                return 1;
        }
        return -1;
    }
    
    public void setAsTempadminhost(){
    	isTempadminhost=true;
    }
    
    public boolean isTempadminhost(){
    	return isTempadminhost;
    }
    
    public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getUserAgent() {
		return this.userAgent;
	}
	
    public boolean isEntrancePunished() {
        if (Server.srv.isPunished(this.getName().toLowerCase())) {
            if (this.hasRight(IUserStates.PROTECTED_FROM_PUNISH)){
                removePunish();
                return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean isEntranceBanned() {
		if (Server.srv.isEntranceBanned(this.getName().toLowerCase()))
			return true;
		return false;
	}
    
    public boolean isColLocked() {
		if (Server.srv.isCollocked (this.getName().toLowerCase()))
			return true;
		return false;
	}
	
	public void removePunish() {
		if (Server.srv.isPunished(this.getName().toLowerCase()))
			Server.srv.removeStore(this.getName().toLowerCase(), IActionStates.PUNISH);
	}
	
	public void removeBan() {
		if (Server.srv.isEntranceBanned(this.getName().toLowerCase()))
			Server.srv.removeStore(this.getName().toLowerCase(), IActionStates.SUBAN);
	}

	public boolean canUseTemplateset(TemplateSet t) {
		if (Server.srv.DEFAULT_TEMPLATESET == null)
			return true;
		if (t == null)
			return false;
		StringBuffer defaultTs =  new StringBuffer (Server.srv.DEFAULT_TEMPLATESET);
		StringBuffer sb = new StringBuffer (this.getName());
		sb.append (" has Template ");
		sb.append (t.getName());
		Server.log(this, sb.toString(), Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
        if (defaultTs !=null){
            StringTokenizer st = new StringTokenizer(defaultTs.toString(),",");
            while (st.hasMoreTokens()) {
                StringBuilder templateName = new StringBuilder(st.nextToken());
                if (templateName.toString().equals(t.getName()))
                        return true;
            }
        }    
	    if (templatesetList.contains(t.getName()) ||  hasRight(IUserStates.ROLE_GOD)) {
	    	StringBuffer sc = new StringBuffer(this.getName());
	    	sc.append(" can use  Template ");
	    	sc.append(t.getName());
		    Server.log(this, sc.toString(), Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
 	        return true;    
	    }
	    StringBuffer scn = new StringBuffer (this.getName());
	    scn.append (" can not use Template ");
	    scn.append (t.getName());
	    Server.log(this, scn.toString(),Server.MSG_AUTH, Server.LVL_MINOR);   		
		return false;
	}
	
	public void mayUseTemplateset(String mayUseTemplateset) {
		if (mayUseTemplateset == null)
			return;
		String[] mTs = mayUseTemplateset.split(",");
    
        for (int j = 0; j < mTs.length; j++) {
        	StringBuffer sb = new StringBuffer(this.getName());
        	sb.append(" may use  Template ");
        	sb.append(mTs[j]);
     	    Server.log(this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
            templatesetList.add(mTs[j]);
        }		
	}
	
    public void setMessageFloodLenght(boolean b) {
        messageFloodLenght = b;
        long now = System.currentTimeMillis();
        lastFloodMessage = now;
    }

    public boolean hasMessageFloodLenght() {
        long now = System.currentTimeMillis();
        if ((now - lastFloodMessage) < Server.srv.MESSAGE_FLOOD_INTERVAL && messageFloodLenght) {
            return true;
        } else {
            messageFloodLenght = false;
        }
        return messageFloodLenght;
    }
	
	public void addToMembershiplist(){
	    String msList = null;
        if (isUnregistered)
            msList = "undefined";
        else msList = (String) getProperty("memberships");
        setMembershiplist(msList);
                
        if (!isUnregistered && isStandart(msList)){
            setMembershiplist(Server.srv.DEFAULT_MEMBERSHIP);
        }
        return;
	}
		
	private boolean isStandart(String mslist){
	    if (mslist == null)
	        return true;
	    if (mslist != null && mslist.length()>0)
	        return false;
	    return true;
    }

	private void setMembershiplist(String list){
	    if (list == null)
	        return;
	    String[] msArr = list.split(",");
        for (int i = 0; i < msArr.length; i++) {
             Membership cms = MembershipManager.instance.getMembership (msArr[i]);
             if (cms==null) {
                 Server.log ("[User]", "Membership for key " + msArr[i] + " hasn't been found", Server.MSG_STATE, Server.LVL_VERBOSE);
                 continue;
             }
             Server.log("[User]", "add User "+ this.getName()+" to Membershiplist "+ msArr[i], Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
             cms.addToList(this);
        }
        return;
	}

    public boolean containsConfirmAction(String confirmAction) {
        if (this.confirmAction.contains(confirmAction))
            return true;
        return false;
    }

    public void addConfirmAction(String confirmAction) {
        this.confirmAction.add(confirmAction);
    }
    
    public void removeConfirmAction(String confirmAction) {
        this.confirmAction.remove(confirmAction);
        if (this.confirmAction.size() == 0)
            this.confirmAction = new Vector<String>();
    }
    
    public boolean whisperDeactivated() {
        return whisperDeactivated;
    }

    public void setWhisperDeactivated(boolean whisperDeactivated) {
        this.whisperDeactivated = whisperDeactivated;
    }
}