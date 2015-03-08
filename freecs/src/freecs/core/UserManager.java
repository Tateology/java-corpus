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

import freecs.*;
import freecs.auth.AuthManager;
import freecs.content.Connection;
import freecs.interfaces.*;
import freecs.layout.TemplateSet;
import freecs.util.EntityDecoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserManager extends Thread implements IUserStates, IMessageDestination {
    public static final UserManager mgr = new UserManager();
    public static int highWaterMark = 0;

    public final byte[] TOUCH_CONTENT;

    public UserStore ustr = new UserStore();
    private HashMap<String, Vector<User>> fshipList;
    private Vector<Object>    schedule, onlineVips, notListedAsOnlineVips, guests, moderators, vips, admins, loggingIn;
    private Vector<Object> anoProxyUser;
    private volatile long lastModified=0;

    public String ustrState() {
        return ustr.state();
    }


    private UserManager () {
       try {
           CharBuffer cb = CharBuffer.wrap ("<!-- ping -->");
           TOUCH_CONTENT = Charset.forName (Server.srv.DEFAULT_CHARSET).newEncoder ().encode (cb).array();
        } catch (Exception e) {
            Server.debug("static UserManager", "touch_content-construction failed", e, Server.MSG_ERROR, Server.LVL_HALT);
            throw new RuntimeException ("failed to initialize UserManager");
        }
        fshipList = new HashMap<String, Vector<User>> ();
        onlineVips = new Vector<Object>();
        notListedAsOnlineVips = new Vector<Object> ();
        vips=new Vector<Object>();
        admins=new Vector<Object>();
        moderators=new Vector<Object>();
        guests=new Vector<Object> ();
        this.setName ("UserManager");
        schedule = new Vector<Object> ();
        loggingIn = new Vector<Object>();
        anoProxyUser = new Vector<Object>();
    }
   
	public static void startUserManager() {
        mgr.setName("UserManager");
        mgr.setPriority(Thread.MAX_PRIORITY-1);
   		mgr.start();
	}

	/**
	 * returns the number of active users
	 */
	public int getActiveUserCount () {
        return ustr.size();
	}

   public static final short  LOGIN_OK                = 0,
                              LOGIN_MISSING           = 1,
                              LOGIN_COOKIE_MISSING    = 2,
                              LOGIN_GROUP_MISSING     = 3,
                              LOGIN_GROUP_NOSTART     = 4,
                              LOGIN_GROUP_BAN		  = 5,
                              LOGIN_GROUP_LOCK		  = 6,
                              LOGIN_COOKIE_DUPLICATE  = 7,
                              LOGIN_PRESENT           = 8,
                              LOGIN_RELOAD            = 9,
                              LOGIN_FAILED            =10,
                              TECHNICAL_ERROR         =11,
                              MAX_USERS               =12,
	                          LOGIN_BLOCKED           =13,
                              USERNAME_TOO_LONG       =14,
                              USERNAME_INVALID        =15,
                              USERNAME_NOT_ACTIVATED  =16,
                              USEREMAIL_BANED         =17,
                              LOGIN_CANCELED          =18,
                              LOGIN_NOTALLOWED        =19,
                              LOGIN_GROUP_RESERVED    =20,
                              LOGIN_UNREG_NOTALLOWED  =21;
   /**
    * try to login this user. returns integer containing 0 for ok or any other number (treated as error-code)
    * @return short 0 = ok, erverything else is an errorcode
    */
	public short tryLogin (String uname, String pwd, String grp, TemplateSet ts, RequestReader req, User u, Connection conn) {
        String ltUname = uname == null ? null : uname.toLowerCase().trim();
        if ((uname != null && loggingIn.contains(ltUname))
            || (uname == null && pwd == null && grp == null
                && req.currentRequest.getCookie() != null && u != null && u.getCookie().equals(req.currentRequest.getCookie()) && u.check(System.currentTimeMillis()))) 
			return LOGIN_RELOAD;
        if (uname == null)
            return LOGIN_MISSING;
        if (uname.length() > Server.srv.MAX_USERNAME_LENGTH)
            return USERNAME_TOO_LONG;
        if (!AuthManager.instance.isValidName(uname))
            return USERNAME_INVALID;
		loggingIn.add(ltUname);
		short result = execTryLogin (uname, pwd, grp, ts, req, conn);
        loggingIn.remove(ltUname);
		return result;
	}
    
    private short execTryLogin (String uname, String pwd, String grp, TemplateSet ts, RequestReader req, Connection conn) {
        req.currPosition = RequestReader.TRYLOGIN;
        if (req.currentRequest.getCookie() == null || req.currentRequest.getCookie().length () < 1)
            return LOGIN_COOKIE_MISSING;
        if (grp == null || grp.length () < 1)
            return LOGIN_GROUP_MISSING;
        req.currPosition = RequestReader.TRYLOGIN_CHECK4PRESENCE;
		User un, uc;
		un = (User) ustr.getUserName(uname.toLowerCase ());
		uc = (User) ustr.getUserCookie(req.currentRequest.getCookie());
		int loginstate = 0;
    	if (un != null && uc == null){
    	    if (un.hasRole(IUserStates.ROLE_GOD) || un.hasDefaultRight(IUserStates.ROLE_GOD)
       	            || un.hasRole(IUserStates.ROLE_VIP) || un.hasDefaultRight(IUserStates.ROLE_VIP)){    
    	        MessageParser mp = new MessageParser();
                mp.setSender(un);
                mp.getSender().sendQuitMessage (false, null);
                un.removeNow();
                loginstate = LOGIN_PRESENT;
    	    } else return LOGIN_PRESENT;
    	}
		if (un == null && uc != null)
			return LOGIN_COOKIE_DUPLICATE;
		if (un != null && uc != null && un.equals (uc))
			return LOGIN_RELOAD;
		if (un != null && uc != null)
			return LOGIN_FAILED;
	   	User nu;
		req.currPosition = RequestReader.TRYLOGIN_AUTHENTICATE;
		try {
			nu = Server.srv.auth.loginUser (uname, pwd, req.currentRequest.getCookie(), req.currentRequest);
		} catch (NullPointerException npe) {
            Server.debug (this, "Catched NPE!", npe, Server.MSG_ERROR, Server.LVL_MAJOR);
            return TECHNICAL_ERROR;
        } catch (CanceledRequestException cre) {
            Server.log (this, "Canceled login due to " 
                    + (cre.timedOut ? 
                      "request timeout" : "connection loss to client"), 
                      Server.MSG_AUTH, Server.LVL_VERBOSE);
            return LOGIN_CANCELED;
        } catch (Exception e) {
			Server.debug (this, "tryLogin:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
			return TECHNICAL_ERROR;
		} 
		if (nu == null)
			return LOGIN_FAILED;
        if (Server.srv.isBanned(nu.getProperty("email"))) {
            return USEREMAIL_BANED;
        }        
        nu.setUserAgent(req.currentRequest.getUserAgent());
		req.currPosition = RequestReader.TRYLOGIN_CORRECT_PERMISSION;
		
		// check user-rights here. If user-rights equal 0, the rights will
		// be corrected to the IUserRights.ROLE_USER.
		if (nu.hasRole(0) || nu.hasRole(Integer.MIN_VALUE))
			nu.setPermission(IUserStates.ROLE_USER);
		// if the user has IUserRights.ROLE_USER, the config will be checked
		// for the user-rights
		if (nu.hasRole(IUserStates.ROLE_USER)) {
			String tname = nu.getName().trim().toLowerCase();
 			if (admins.contains (tname)) {
                nu.setPermission (IUserStates.ROLE_GOD);
            } else if (vips.contains (tname)) {
				nu.setPermission (IUserStates.ROLE_VIP);
	        }

		    if (moderators.contains (tname)) {
		        nu.addIsModerator();
			}
		    if (guests.contains (tname)) {
		        nu.addIsGuest();
			}
		}
		nu.conn = conn;
    	if (ustr.size () >= Server.srv.MAX_USERS 
            && !nu.hasDefaultRight(IUserStates.ROLE_VIP))
				 return MAX_USERS;
		req.currPosition = RequestReader.TRYLOGIN_SET_GROUP;
        if (nu.blocked)
            return LOGIN_BLOCKED;
        if (!nu.activated)
        	return USERNAME_NOT_ACTIVATED;
        if (!GroupManager.mgr.isStartingGroup(grp))
            return LOGIN_GROUP_NOSTART;
		Group g = GroupManager.mgr.getStartingGroup (grp);
        if (g != null) {
            if (!g.hasState(IGroupState.ENTRANCE))
                return LOGIN_GROUP_NOSTART;
            if (g.usrIsBaned(uname))
                return LOGIN_GROUP_BAN;
            if (nu.isUnregistered && g.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG))
                return LOGIN_UNREG_NOTALLOWED;
            if (!g.hasState(IGroupState.OPEN) && !nu.hasRight(IUserStates.MAY_JOIN_LOCKED_GROUP))
                return LOGIN_GROUP_LOCK;
            int reason =  GroupManager.mgr.checkReason(grp, nu,  nu);
            if (reason == IGroupReason.RESERVED){
                return LOGIN_GROUP_RESERVED;
            }
            req.currPosition = RequestReader.TRYLOGIN_SEND_LOGINMSG;
        } else {
            if (!GroupManager.mgr.isStartingGroup(grp))
                return LOGIN_GROUP_NOSTART;
            g = GroupManager.mgr.openGroup(grp, (String) GroupManager.mgr.startGroupThemes.get(grp.trim().toLowerCase()), nu);
            if (g==null){
                int reason =  GroupManager.mgr.checkReason(grp, nu, nu);
                if (reason == IGroupReason.NOT_ALLOW_JOIN_UNREG){
                    return LOGIN_UNREG_NOTALLOWED;
                } else if (reason == IGroupReason.RESERVED){
                    return LOGIN_GROUP_RESERVED;
                } else {
                    return LOGIN_GROUP_LOCK;
                }
            }
        }
        if (nu.conn != null && nu.conn.hasAnoProxy()) {
            anoProxyUser.add(nu);   
        }
        nu.addToMembershiplist();
        
        checkLocks(nu);
  
        MessageParser mprj = new MessageParser();
        mprj.setSender (nu);
        mprj.setMessageTemplate ("message.user.join.server");
        g.sendMessage (mprj);
        try {
            this.addUser (nu);
            g.addLoginUser (nu);
        } catch (Exception e) {
            Server.debug(this, "Exception during addUser: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
            return TECHNICAL_ERROR;
        }
        
        List<User> fn;
        switch (nu.notifyFriends()) {
            case User.FN_ALL:
                fn = (List<User>) fshipList.get (nu.getName ().toLowerCase());
                break;
            case User.FN_FRIEND_AGREEMENT:
                // check for strong-friend-relationship
                // if login-user and the friend have marked each other
                // as friends, a notification will be sent
                List<User> allFriends = (List<User>) fshipList.get (nu.getName ().toLowerCase());
                if (allFriends == null) {
                    fn=null;
                    break;
                }
                fn = new ArrayList<User>();
                for (Iterator<User> i = allFriends.iterator(); i.hasNext(); ) {
                    User fu = (User) i.next();
                    if (nu.isFriend(fu)) {
                        fn.add(fu);
                    }
                }
                break;
            default:
                fn = null;
        }
        sendFriendNotification (nu, g, fn);

        req.currPosition = RequestReader.TRYLOGIN_SET_PERMISSION;
		nu.setTemplateSet (ts);
		
		req.currPosition = RequestReader.TRYLOGIN_SCHEDULE_GODMSG;
	    if (loginstate == LOGIN_PRESENT && (nu.hasRight(IUserStates.ROLE_GOD) || nu.hasDefaultRight(ROLE_GOD))) {
	        MessageParser mpr = new MessageParser();
	        mpr.setUsercontext(un) ;
	        mpr.setMessageTemplate ("message.user.loginpresent");
	        nu.scheduleMessage (mpr);
	    }
	    
		req.currPosition = RequestReader.TRYLOGIN_SCHEDULE_VIPMSG;
		Membership defaultMembership = nu.getDefaultMembership();
		boolean listedAsOnlinevip = true;
		if (defaultMembership != null)
			listedAsOnlinevip = defaultMembership.listedAsOnlinevip();

		if (nu.hasRight(IUserStates.ROLE_VIP) || nu.hasDefaultRight(ROLE_VIP)) {
            MessageParser mpr = new MessageParser();
			mpr.setUsercontext(nu) ;
			mpr.setMessageTemplate ("message.user.vip");
			nu.scheduleMessage (mpr);
			if (listedAsOnlinevip) {
				onlineVips.add(nu);
			} else notListedAsOnlineVips.add(nu);
		}
	   	req.currPosition = RequestReader.TRYLOGIN_SCHEDULE_FRIENDMSGS;
	
        Vector<User> onlineFriends = new Vector<User>();
	    for (Enumeration<String> e = nu.friends (); e.hasMoreElements (); ) {
            String fname = (String) e.nextElement ();
            User cu = getUserByName (fname);
            if (cu == null) 
                continue;
            onlineFriends.add(cu);
        }
	    if (onlineFriends.size()>0) {
            MessageParser mpr = new MessageParser();
            mpr.setMessageTemplate ("message.f.headline");
            mpr.setParam(String.valueOf(onlineFriends.size()));
            nu.scheduleMessage (mpr);
            for (Enumeration<User> e = onlineFriends.elements(); e.hasMoreElements(); ) {
                User cu = (User) e.nextElement();
                mpr = new MessageParser ();
                mpr.setUsercontext (cu);
                Group gg = cu.getGroup ();
                mpr.setTargetGroup (gg);
                mpr.setMessageTemplate ("message.f.isOnline");
                nu.scheduleMessage (mpr);
            }
	    }
	    MessageParser mpr = new MessageParser();
	    mpr.setMessageTemplate ("message.user.join.server.personal");
        mpr.setSender(nu);
	    nu.scheduleMessage (mpr);
		return LOGIN_OK;
	}

   /**
    * adds a user to the lists
    * @param u The user to add
    */
	public void addUser (User u) throws Exception {
		ustr.addUser(u);
        lastModified=System.currentTimeMillis();
		if (ustr.size() > highWaterMark)
			highWaterMark = ustr.size();
	}

   /**
    * removes a user from the userlists
    * @param u the user to remove
    */
	public void removeUser (User u) {
	  	ustr.removeUser(u);
        u.clearPrivatMessageStore();
        lastModified=System.currentTimeMillis();
        if (onlineVips.contains(u))
            onlineVips.removeElement(u);               
		if (notListedAsOnlineVips.contains(u)) {
		    notListedAsOnlineVips.removeElement(u);		            
		}
        if (anoProxyUser.contains(u))
            anoProxyUser.removeElement(u);
	}

   /**
    * returns the user identifyed by the give cookie-value
    * @param c  the cookie-value
    * @return User the user identified by this cookie-value
    */
   public User getUserByCookie (String c) {
      return ustr.getUserCookie(c);
   }

   /**
    * returns the user identifyed by the give name
    * @param n  the name
    * @return User the user identified by this name
    */
    public User getUserByName (String n) {
        if (n.indexOf("&") > -1)
            n = EntityDecoder.htmlToChar (n);
        return ustr.getUserName(n);
    }

   /**
    * schedules this user to be removed
   public void scheduleToRemove (User u) {
   	  if (u.getRemoveWhen() == 0) 
   	  	return;
      removableUsers.addElement (u);
   }
    */

	/**
	 * adds a friendship-relation
	 * @param u The user which has registered fname as friend
	 * @param fname the name of the user which will be registered with u
	 */
   public void addFriendship (User u, String fname) {
      Vector<User> f = (Vector<User>) fshipList.get (fname);
      if (f == null) {
         f = new Vector<User> ();
         fshipList.put (fname, f);
      }
      f.addElement (u);
   }

	/**
	 * removes a friendship-relation
	 * @param u The user which wants to unregister fname as friend
	 * @param fname the name of the user which will be unregisterd with u
	 */
	public synchronized void removeFriendship (User u, String fname) {
		Vector<User> f = (Vector<User>) fshipList.get (fname);
		if (f == null) 
			return;
		f.remove (u);
		if (f.size()==0)
			fshipList.remove(f);
	}

   /**
    * cleanup the userspace. if a user times out, we have to remove
    * this user from all lists and also unregister his key
    * The touching of users connected via proxy-servers will also be done within here
    * Also the ScheduledActions will be triggered here
    */
    public void run () {
        long lastMessage = 0;
		while (Server.srv.isRunning ()) try {
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
			long currTime = System.currentTimeMillis ();
			long lowestValue = currTime + Math.min(Server.srv.USER_REMOVE_SCHEDULE_TIME, Server.srv.TOUCH_USER_DELAY);
            lowestValue = checkUsers (currTime, lowestValue);

			// checking the ScheduledActions
			for (Enumeration<Object> e = schedule.elements (); e.hasMoreElements (); ) {
				ScheduledAction sa = (ScheduledAction) e.nextElement ();
				if (!ustr.contains(sa.getUser())) {
					schedule.removeElement (sa);
				}
				long st = sa.getStartTime ();
				if (st > currTime) {
					if (st < lowestValue) {
                        lowestValue = st;
                    }
					continue;
				}
				try {
				    sa.execute ();
				} catch (Exception ex) {
				    Server.log(this, "Error sheduled Action ("+ex+")", Server.MSG_ERROR, Server.LVL_MAJOR);
                    // TODO: handle exception
                }
			
				schedule.removeElement (sa);
			}
			long sleepTime = lowestValue - System.currentTimeMillis ();
			if (sleepTime <= 0) 
                sleepTime = 33;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) { }
		} catch (Exception e) {
			Server.debug (this, "encountered excpetion", e, Server.MSG_ERROR, Server.LVL_MAJOR);
		}
	}


    private long checkUsers (long currTime, long lowestValue) {
       // long val = Server.srv.USER_REMOVE_SCHEDULE_TIME * 10;
        User[] uarr = ustr.toArray();
        for (int i = 0; i < uarr.length; i++) {
            User cu = uarr[i];
            if (cu == null)
                continue;
            if (!cu.check(currTime)) {
                cu.removeNow();
                continue;
            }
            long nextCheck = cu.nextCheck();
            if (nextCheck < lowestValue) {
                lowestValue = nextCheck;
            }
            cu.checkReferences();
            cu.touch (currTime);
        }
        return lowestValue;
    }
    
    private void checkLocks(User nu){
        if (nu.isEntrancePunished())
            nu.setPunish(true);
        if (Server.srv.isCollocked(nu.getName().toLowerCase()))
            nu.setCollock(true);
        if (Server.srv.isAwaylocked(nu.getName().toLowerCase()))
            nu.setAwaylock(true);
        if (Server.srv.isActlocked(nu.getName().toLowerCase()))
            nu.setActlock(true);
        Server.srv.checkPermaIgnorlistForUser(nu);
    }

    private boolean isMobileBrowser(String BrowserAgent){   
        if (BrowserAgent == null)
            return false;
        StringBuilder c_input = new StringBuilder(Server.srv.MOBILE_BROWSER_REGEX);
        c_input.trimToSize();
        Pattern p = Pattern.compile(c_input.toString().toLowerCase());
        Matcher m = p.matcher(BrowserAgent.toLowerCase());
        if (m.find()){
            Server.log(this, "found Mobile Browser ["+m.group() +"] ("+BrowserAgent+")", Server.MSG_TRAFFIC, Server.LVL_MAJOR);
            return true;
        } else return false;
    }

   /**
    * interface MessageDestination
    */
    public void sendMessage (IContainer mc) {
        synchronized (ustr) {
            for (Iterator<Object> i = ustr.iterator (); i.hasNext (); ) {
                User cu = (User) i.next ();
                cu.sendMessage (mc);
            }
        }
    }
    
    public void sendMessageWithoutMobileBrowser (IContainer mc) {
        synchronized (ustr) {
            for (Iterator<Object> i = ustr.iterator (); i.hasNext (); ) {
                User cu = (User) i.next ();
                if (!isMobileBrowser(cu.getUserAgent()))
                     cu.sendMessage (mc);
            }
        }
    }

    public Vector<Object> onlineVipList () {
        return onlineVips;
    }
    
    public Enumeration<Object> onlineVips () {
		return onlineVips.elements();
	}
	
	public Enumeration<Object> notListedAsOnlineVips () {
		return notListedAsOnlineVips.elements();
	}
	public Vector<Object> anoProxyUserList () {
        return anoProxyUser;
    }
    
    public Enumeration<Object> anoProxyUser () {
        return anoProxyUser.elements();
    }
    
    public Iterator<Object> users () {
       return ustr.iterator ();
    }

	public void scheduleAction (short action, long startTime, User usr, User sender) {
		ScheduledAction sa = new ScheduledAction (action, startTime, usr, sender);
		if (schedule.contains (sa)) 
			return;
		schedule.addElement (sa);
	}
   
	public int getHighWaterMark () {
   		return highWaterMark;
	}
	
	@SuppressWarnings("unchecked")
	public void updateVips (Vector<String> nVips) {
		Vector<String> removed = (Vector<String>) vips.clone();
		removed.removeAll(nVips);
		Vector<String> added = (Vector<String>) nVips.clone();
		added.removeAll(vips);
		UserManager umgr = UserManager.mgr;
		for (Enumeration<String> e = added.elements(); e.hasMoreElements(); ) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				cu.setPermission(IUserStates.ROLE_VIP);
			vips.addElement(uname);
		}
		for (Enumeration<String> e = removed.elements(); e.hasMoreElements();) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				cu.setPermission(IUserStates.ROLE_USER);
			while (vips.contains(uname))
				vips.removeElement(uname);
		}
	}

    @SuppressWarnings("unchecked")
	public void updateAdmins (Vector<String> nAdmins) {
        Vector<String> removed = (Vector<String>) admins.clone();
        removed.removeAll(nAdmins);
        Vector<String> added = (Vector<String>) nAdmins.clone();
        added.removeAll(admins);
        UserManager umgr = UserManager.mgr;
        for (Enumeration<String> e = added.elements(); e.hasMoreElements(); ) {
            String uname = (String) e.nextElement();
            User cu = umgr.getUserByName(uname);
            if (cu != null)
                cu.setPermission(IUserStates.ROLE_GOD);
            admins.addElement(uname);
        }
        for (Enumeration<String> e = removed.elements(); e.hasMoreElements();) {
            String uname = (String) e.nextElement();
            User cu = umgr.getUserByName(uname);
            if (cu != null)
                cu.setPermission(IUserStates.ROLE_USER);
            while (admins.contains(uname))
                admins.removeElement(uname);
        }
    }

	@SuppressWarnings("unchecked")
	public void updateModerators (Vector<String> nMod) {
		Vector<String> removed = (Vector<String>) moderators.clone();
		removed.removeAll(nMod);
		Vector<String> added = (Vector<String>) nMod.clone();
		added.removeAll(moderators);
		UserManager umgr = UserManager.mgr;
		for (Enumeration<String> e = added.elements(); e.hasMoreElements(); ) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				cu.addIsModerator();
			moderators.addElement(uname);
		}
		for (Enumeration<String> e = removed.elements(); e.hasMoreElements();) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				cu.removeIsModerator();
			while (moderators.contains(uname))
				moderators.removeElement(uname);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateGuests (Vector<String> nGuest) {
		Vector<String> removed = (Vector<String>) guests.clone();
		removed.removeAll(nGuest);
		Vector<String> added = (Vector<String>) nGuest.clone();
		added.removeAll(guests);
		UserManager umgr = UserManager.mgr;
		for (Enumeration<String> e = added.elements(); e.hasMoreElements(); ) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				cu.addIsGuest();
			guests.addElement(uname);
		}
		for (Enumeration<String> e = removed.elements(); e.hasMoreElements();) {
			String uname = (String) e.nextElement();
			User cu = umgr.getUserByName(uname);
			if (cu != null)
				 cu.removeIsGuest();
			while (guests.contains(uname))
				guests.removeElement(uname);
		}
	}

    public void sendFriendNotification (User forUser, Group inGroup, List<User> to) {
        if (to==null)
            return;
        MessageParser mpr = new MessageParser();
        mpr.setUsercontext (forUser);
        mpr.setTargetGroup (inGroup);
        mpr.setMessageTemplate ("message.f.joined");
        for (Iterator<User> e = to.iterator (); e.hasNext (); ) {
            User cu = (User) e.next ();
            if (!forUser.getGroup().equals(cu.getGroup())){
                cu.sendMessage (mpr);
                if (cu.isAway())
                   	cu.storeFriendNotification(forUser.getName());
            }             
        }
    }
	public Enumeration<Object> vips () {
		return vips.elements();
	}
	public Enumeration<Object> moderators () {
		return moderators.elements();
	}
    public Enumeration<Object> guests () {
        return guests.elements();
    }

    /**
     * method to resolve string-right-declarations to int-bitmask-values
     * @param name the name of the IUserStates-constant
     * @return the int-value of the IUserStates-constant
     */
    public static int resolveState (String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.equals("may_open_group")) { 
            return IUserStates.MAY_OPEN_GROUP;
        } else if (lowerName.equals("may_open_moderated_group")) { 
            return IUserStates.MAY_OPEN_MODERATED_GROUP;
        } else if (lowerName.equals("may_lock_group")) { 
            return IUserStates.MAY_LOCK_GROUP;
        } else if (lowerName.equals("may_lock_moderated_group")) { 
            return IUserStates.MAY_LOCK_MODERATED_GROUP;
        } else if (lowerName.equals("may_lock_starting_group")) { 
            return IUserStates.MAY_LOCK_STARTING_GROUP;
        } else if (lowerName.equals("may_join_group")) { 
            return IUserStates.MAY_JOIN_GROUP;
        } else if (lowerName.equals("may_join_locked_group")) { 
            return IUserStates.MAY_JOIN_LOCKED_GROUP;
        } else if (lowerName.equals("may_set_theme")) { 
            return IUserStates.MAY_SET_THEME;
        } else if (lowerName.equals("may_use_smiley")) { 
            return IUserStates.MAY_USE_SMILEY;
        } else if (lowerName.equals("may_call_memberships")) { 
            return IUserStates.MAY_CALL_MEMBERSHIPS;
        } else if (lowerName.equals("may_change_right")) { 
            return IUserStates.MAY_CHANGE_RIGHT;
        } else if (lowerName.equals("may_punish")) { 
            return IUserStates.MAY_PUNISH;
        } else if (lowerName.equals("may_kick")) { 
            return IUserStates.MAY_KICK;
        } else if (lowerName.equals("may_kick_hard")) { 
            return IUserStates.MAY_KICK_HARD;
        } else if (lowerName.equals("may_ban")) { 
            return IUserStates.MAY_BAN;
        } else if (lowerName.equals("protected_from_punish")) { 
            return IUserStates.PROTECTED_FROM_PUNISH;
        } else if (lowerName.equals("protected_from_kick")) { 
            return IUserStates.PROTECTED_FROM_KICK;
        } else if (lowerName.equals("protected_from_ban")) { 
            return IUserStates.PROTECTED_FROM_BAN;
        } else if (lowerName.equals("protected_from_rightchange")) { 
            return IUserStates.PROTECTED_FROM_RIGHTCHANGE;
        } else if (lowerName.equals("freely_punishable")) { 
            return IUserStates.FREELY_PUNISHABLE;
        } else if (lowerName.equals("freely_kickable")) { 
            return IUserStates.FREELY_KICKABLE;
        } else if (lowerName.equals("freely_banable")) { 
            return IUserStates.FREELY_BANABLE;
        } else if (lowerName.equals("is_moderator")) { 
            return IUserStates.IS_MODERATOR;
        } else if (lowerName.equals("may_release_serverban")) { 
            return IUserStates.MAY_RELEASE_SERVERBAN;
        } else if (lowerName.equals("is_guest")) { 
            return IUserStates.IS_GUEST;
        } else if (lowerName.equals("role_asshole")) { 
            return IUserStates.ROLE_ASSHOLE;
        } else if (lowerName.equals("role_user")) { 
            return IUserStates.ROLE_USER;
        } else if (lowerName.equals("role_vip")) { 
            return IUserStates.ROLE_VIP;
        } else if (lowerName.equals("role_god")) { 
            return IUserStates.ROLE_GOD;
        } else if (lowerName.equals("guest")) {
            return IUserStates.IS_GUEST;
        } else if (lowerName.equals("moderator")) {
            return IUserStates.IS_MODERATOR;
		} else if (lowerName.equals("vip")) {
            return IUserStates.ROLE_VIP;
		} else if (lowerName.equals("admin")) {
            return IUserStates.ROLE_GOD;
		} else if (lowerName.equals("user")) {
            return IUserStates.ROLE_USER;
		} else if (lowerName.equals("asshole")) {
            return IUserStates.ROLE_ASSHOLE;
		}
        return 0;
    }
    
    public String toString() {
        return ("[UserManager]");
    }
    
    public class UserStore {
        private Map<String, User> usrName = new HashMap<String, User>();
        private Map<String, User> usrCookie = new HashMap<String, User>();
        private User[] uarr = new User[0];
        
        private UserStore () { }
        
 
        public synchronized String state() {
        	 StringBuffer sb = new StringBuffer();
             sb.append ("<table border=1><tr><td>");
             for (Iterator<Entry<String, User>> i = usrName.entrySet().iterator(); i.hasNext(); )
                 sb.append (i.next()).append("<br>");
             sb.append ("</td><td>");
             for (Iterator<Entry<String, User>> i = usrCookie.entrySet().iterator(); i.hasNext(); )
                 sb.append (i.next()).append("<br>");
             sb.append ("</td></tr></table>");
             return sb.toString();
        }
 
        /**
         * @return the number of users currently logged in
         */
        public synchronized int size() {
            return usrName.size();
        }

        public void addUser (User u) throws Exception {
            String name = u.getName();
            String cookie = u.getCookie();
            if (name==null || name.length() < 1
                || cookie==null || cookie.length() < 1)
                    throw new Exception ("Tryed to add invalid user (cookie: " + cookie + " / name: " + name);
            name = name.trim().toLowerCase();
            if (usrName.containsKey(name)
                || usrCookie.containsKey(cookie))
                throw new Exception ("Tryed to add duplicate user: " + name);
            synchronized (this) {
                usrName.put(name, u);
                usrCookie.put(cookie, u);
                uarr=null;
            }
        }
        
        public synchronized User[] toArray() {
            if (uarr==null) {
                uarr = (User[]) usrName.values().toArray(new User[0]);
            }
            return uarr;
        }
        
        public void removeUser (User u) {
            String name = u.getName();
            String cookie = u.getCookie();
            synchronized (this) {
                if (name!=null) {
                    usrName.remove(name.trim().toLowerCase());
                    uarr=null;
                }
                if (cookie!=null)
                    usrCookie.remove(cookie);
            }
        }

        public synchronized User getUserCookie(String cookie) {
            return (User) usrCookie.get(cookie);
        }

        public User getUserName(String name) {
            if (name==null)
                return null;
            String key = name.trim().toLowerCase();
            synchronized (this) {
                return (User) usrName.get(key);
            }
        }
        
        public synchronized boolean contains (User u) {
            String cookie = u.getCookie();
            String name = u.getName();
            User c, n;
            synchronized (this) {
                c = getUserCookie(cookie);
                n = getUserName(name);
            }
            return (c!=null && n!=null); 
        }
        
        public Iterator<Object> iterator() {
            return new UserStoreIterator(this);
        }
        
        class UserStoreIterator implements Iterator<Object> {
            private Iterator<User> ci;
            private User cu;
            private UserStore ustr;
            UserStoreIterator (UserStore ustr) {
                ci = usrName.values().iterator();
                this.ustr = ustr;
            }

            public void remove() {
                synchronized (ustr) {
                    if (cu==null)
                        throw new IllegalStateException();
                    usrCookie.remove(cu.getCookie());
                    ci.remove();
                    uarr=null;
                }
            }

            public boolean hasNext() {
                return ci.hasNext();
            }

            public Object next() {
                cu = (User) ci.next();
                return cu;
            }
        }
   }

    /**
     * @return
     */
    public long lastModified() {
        return lastModified;
    }
}