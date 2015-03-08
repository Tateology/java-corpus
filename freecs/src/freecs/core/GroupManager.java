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
import freecs.interfaces.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
/**
 * The GroupManager manages all groups. It is a single instantiable Thread
 *
 * freecs.core
 */
public class GroupManager implements IGroupState {
   public static final GroupManager mgr = new GroupManager ();
   public static final HashMap<String, Object> pluginStore = new HashMap<String, Object>();
   private Map<String, Group> grps = new HashMap<String, Group> ();
   private Group[] grpsArr = new Group[0];
   private int highWaterMark=0;
   private Vector<Object> moderatedGroups=new Vector<Object>(), startGroups=new Vector<Object>(), startGroupsToLowerCase=new Vector<Object>();
   private volatile long lastModified=0;
   private volatile long groupListLastChange=0;
   public HashMap<String, String> startGroupThemes=new HashMap<String, String>();

	private GroupManager () {
	}

   /**
    * adds a group to the map of available groups
    * @param g the Group to add
    */
	private synchronized void addGroup (Group g) {
		grps.put (g.getKey (), g);
        lastModified=System.currentTimeMillis();
        grpsArr=null;
		if (grps.size () > highWaterMark)
			highWaterMark = grps.size();
	}
	
	public int getHighWaterMark () {
		return highWaterMark;
	}

   /**
    * removes a group
    * @param g the Group-Object to be removed
    */
    public synchronized void removeGroup (Group g) {
        if (g == null)
            return;
        lastModified=System.currentTimeMillis();
        g.invalidate();
        grps.remove (g.getKey ());
        IGroupPlugin[] plugins = g.getPlugins();
        if (plugins!=null) {
            for (int i = 0; i<plugins.length; i++) {
                try {
                    plugins[i].remove(g);
                } catch (Exception e) {
                    Server.debug(plugins[i], "catched exception from extension while removing: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
            }
            g.setPlugins(null);
        }
        grpsArr=null;
    }

   /**
    * Return the group identified by gName
    * @param gName the name of this group
    * @return the group identified by gName || null if none existent
    */
	public Group getGroup (String gName) {
		if (gName==null) 
			return null;
		return (Group) grps.get (gName.toLowerCase ().trim());
	}

	/**
	 * Return the startinggroup identified by gName
	 * @param gName
	 * @return the group identified by gName || null if none existent
	 */
   public Group getStartingGroup (String gName) {
   	  Group g;
      g = (Group) grps.get (gName.toLowerCase ().trim());
      if (g == null) return null;
      if (!g.hasState(IGroupState.ENTRANCE)) {
         return null;
      }
      return g;
   }

	/**
	 * Opens a new group
	 * @param groupname The name of the group
	 * @param topic The theme to use for this group (possible null)
	 * @param opener The opener of this group
	 * @return the newly created group or the group this user has joined
	 */
   public Group openGroup (String groupname, String topic, User opener) {
      return openGroup (groupname, topic, opener, opener);
   }

	/**
	 * Opens a new group checking the rights of another user
	 * @param groupname The name of the group
	 * @param topic The theme to use for this group (possible null)
	 * @param opener The opener of this group
	 * @param rUser The user to check the rights for opening this group
	 * @return the newly created group or the group this user has joined
	 */
   public synchronized Group openGroup (String groupname, String topic, User opener, User rUser) {
       if (Server.srv.MAX_GROUPNAME_LENGTH > 0 && groupname.length() > Server.srv.MAX_GROUPNAME_LENGTH)
           return null;
       if (isStartingGroup(groupname)
               && (!opener.hasRight(IUserStates.ROLE_VIP)
                       || topic==null))
           topic = (String) startGroupThemes.get(groupname.trim().toLowerCase());
             
       boolean moderated = moderatedGroups.contains(groupname.trim().toLowerCase());
	   if (moderated && !rUser.hasRight(IUserStates.MAY_OPEN_MODERATED_GROUP))
				return null;
	   Group g = getGroup(groupname);
	   // if the group is already opened, try to add 
       // the given user and return the group on success
       if (g != null) {
           return g.addUser(opener, rUser) ? g : null;
       }
	   if (!rUser.hasRight (IUserStates.MAY_OPEN_GROUP) && opener.getGroup() != null) 
	       return null;
	   if (opener != null)
	       g = new Group (checkGproupname(groupname), topic, opener);
	   else g = new Group (checkGproupname(groupname), topic, rUser);
		
       if (isStartingGroup(g.getRawName())) {
           g.setState(IGroupState.ENTRANCE);
           g.setState(IGroupState.CAN_SET_PUNISHABLE);
       } else {
    	   g.setState(IGroupState.ALLOW_USE_SMILEY);
           g.setState(IGroupState.ALLOW_USE_BBCODES);
       	   g.setState(IGroupState.SU_CAN_SETTHEME);
       }
	   if (moderated)
	       g.setState(IGroupState.MODERATED);
       StringBuffer sb = new StringBuffer (Server.BASE_PATH);
       sb.append ("/grouppolicies/");
       sb.append (groupname.toLowerCase());
       sb.append (".properties");
       File f = new File (sb.toString());
       if (f.exists()) {
           g = checkProperties (f, g, rUser);
           if (g == null)
               return null;
       }
       if (opener != null){
           if (opener.isUnregistered && g.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG))
               return null;
       }
       if (!g.isMembershipRoom(opener))
       	return null;
       if (!g.addUser (opener))
           return null;
		addGroup (g);
		return g;
    }
    
    private Group checkProperties (File f, Group g, User rUser) {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream(f);
            props.load(in);
            in.close();
        } catch (FileNotFoundException fnfe) {
            return g;
        } catch (IOException ioe) {
            Server.debug(this, "crateByProperties:", ioe, Server.MSG_ERROR, Server.LVL_HALT);
            return g;
        }
        HashMap<String, Properties> map = new HashMap<String, Properties>();
        for (Iterator<Object> i = props.keySet().iterator(); i.hasNext(); ) {
            String key = i.next().toString();
            String low = key.toLowerCase();
            String val = props.getProperty(key);
            if ("moderated".equals(low) 
                && "true".equals(val)) {
                if (!rUser.hasRight(IUserStates.MAY_OPEN_MODERATED_GROUP))
                    return null;
                g.setState(IGroupState.MODERATED);
            } else if ("timelock".equals(low)) {
                if (!g.hasState(IGroupState.MODERATED))
                    return null;
                try {
                    int sec = Integer.parseInt(val);
                    g.setTimelockSec(sec);
                } catch (NumberFormatException nfe) {
                    Server.log(g, "Timelock value isn't a number", Server.MSG_ERROR, Server.LVL_MINOR);
                }
            } else if ("minuserrole".equals(low)) {
                int r = 0;
                if ("vip".equals(val))
                    r = IUserStates.ROLE_VIP;
                else if ("moderator".equals(val))
                    r = IUserStates.IS_MODERATOR;
                else if ("admin".equals(val))
                    r = IUserStates.ROLE_GOD;
                if (!rUser.hasRight(r))
                    return null;
                g.setMinRight(r);
            } else if ("membershiproom".equals(low)) {
            	String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setMembershipRoom ((Membership[]) msObjects.toArray(new Membership[0]));
            } else if ("autosulist".equals(low)) {
                g.setAutoSu (val.split(","));
            } else if ("autosumembershiplist".equals(low)) {
           	    String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setAutoSuMembership (msObjects.toArray(new Membership[0]));
           } else if ("lockprotected".equals(low) 
                        && "true".equals(val)) {
                if (!g.hasState(IGroupState.OPEN))
                    g.setState (IGroupState.OPEN);
                g.setState (IGroupState.LOCKPROTECTED);
            } else if ("autosu".equals(low)
                        && "false".equals(val)) {
                g.unsetState (IGroupState.AUTO_SU_FIRST);
            } else if ("allowsu".equals(low)
                        && "false".equals(val)) {
                g.unsetState (IGroupState.ALLOW_SU);
            }  else if ("nosufirst".equals(low)
                    && "true".equals(val)) {
                g.setState (IGroupState.NO_SU_FIRST);
            }  else if ("minuserrolesu".equals(low)) {
                if ("vip".equals(val))
                    g.setMinRightSu(IUserStates.ROLE_VIP);
                else if ("moderator".equals(val))
                    g.setMinRightSu(IUserStates.IS_MODERATOR);
                else if ("admin".equals(val))
                    g.setMinRightSu(IUserStates.ROLE_GOD);
                else
                    Server.log (this, "minuserrolesu has wrong value '" + val + "' for group " + g.getRawName(), Server.MSG_STATE, Server.LVL_MINOR);
            } else if ("soundprooffor".equals(low)) {
                String[] roles = val.toLowerCase().split(",");
                for (int j = 0; j < roles.length; j++) {
                    String curr = roles[j].trim();
                    if ("vip".equals(curr))
                        g.setState(IGroupState.SND_PRF_VIP);
					else if ("guest".equals(curr))
						g.setState(IGroupState.SND_PRF_GUEST);
                    else if ("moderator".equals(curr))
                        g.setState(IGroupState.SND_PRF_MODERATOR);
                    else if ("admin".equals(curr))
                        g.setState(IGroupState.SND_PRF_GOD);
                    else if ("user".equals(curr))
                        g.setState(IGroupState.SND_PRF_USER);
                }
            } else if ("suforbidden".equals(low)) {
                g.setSuForbiddenMembership(val);
            } else if ("memberroom".equals(low)) {
                String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setMemberRoom ((Membership[]) msObjects.toArray(new Membership[0]));
            }  else if ("allowusebbcodes".equals(low) && "true".equals(val)) {
            	g.setState(IGroupState.ALLOW_USE_BBCODES);
            } else if ("allowusebbcodes".equals(low) && "false".equals(val)) {
            	g.unsetState(IGroupState.ALLOW_USE_BBCODES);
            } else if ("allowusesmiley".equals(low) && "true".equals(val)) {
            	g.setState(IGroupState.ALLOW_USE_SMILEY);
            } else if ("allowusesmiley".equals(low) && "false".equals(val)) {
            	g.unsetState(IGroupState.ALLOW_USE_SMILEY);
            } else if ("deactivatehitdice".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.DEACTIVATE_HITDICE);
            } else if ("sucanban".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.SU_CAN_BAN);
            } else if ("sucanban".equals(low) && "false".equals(val)){
            	g.unsetState (IGroupState.SU_CAN_BAN);
            } else if ("sucansettheme".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.SU_CAN_SETTHEME);
            } else if ("sucansettheme".equals(low) && "false".equals(val)){
            	g.unsetState (IGroupState.SU_CAN_SETTHEME);
            } else if ("cansetpunishable".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.CAN_SET_PUNISHABLE);
            } else if ("cansetpunishable".equals(low) && "false".equals(val)){
                g.unsetState (IGroupState.CAN_SET_PUNISHABLE);
            } else if ("notallowjoinunreg".equals(low) && "true".equals(val)){
                g.setState (IGroupState.NOT_ALLOW_JOIN_UNREG);
            } else if ("joinmembershiplocked".equals(low)) {
                if ("true".equals(val)){
                    g.setState (IGroupState.JOIN_MEMBERSHIP_LOCKED);
                } else g.unsetState (IGroupState.JOIN_MEMBERSHIP_LOCKED);
            } else if (low.startsWith("extension.")) {
                String namespace = low.substring(10);
                int idx = namespace.indexOf(".");
                if (idx == -1) {
                    Server.log (this, "invalid GroupPolicy for extension: " + low, Server.MSG_ERROR, Server.LVL_MINOR);
                    continue;
                }
                String propName = namespace.substring(idx+1);
                namespace = namespace.substring(0,idx);
                Properties p = (Properties) map.get(namespace);
                if (p==null) {
                    p = new Properties();
                    map.put(namespace, p);
                }
                p.setProperty(propName, val);
            }
        }
        if (map.size() > 0) {
            Vector<IGroupPlugin> plugins = new Vector<IGroupPlugin>();
            for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
                String namespace = (String) i.next();
                Properties p = (Properties) map.get(namespace);
                String url = p.getProperty("url");
                Object o;
                synchronized (pluginStore) {
                    o = pluginStore.get(url);
                    if (o == null) {
                        try {
                            Class<?> piClass = Class.forName(url);
                            Method getInstance = piClass.getMethod("getMasterInstance");
                            if (getInstance==null)
                                throw new Exception ("Specified plugin-object doesn't implement static getMasterInstance");
                            o = getInstance.invoke(null);
                            if (!(o instanceof IGroupPlugin))
                                throw new Exception ("Specified plugin-object doesn't implement interface IGroupPlugin");
                            pluginStore.put(url, o);
                        } catch (Exception e) {
                            Server.log (this, "invalid url for extension: " + url, Server.MSG_ERROR, Server.LVL_MINOR);
                            continue;
                        }
                    }
                }
                try {
                    plugins.add(((IGroupPlugin) o).instanceForGroup(namespace, g, p));
                } catch (Exception e) {
                    Server.debug(this, "catched exception while getting GroupPlugin-instance", e, Server.MSG_STATE, Server.LVL_MAJOR);
                }
            }
            g.setPlugins((IGroupPlugin[]) plugins.toArray(new IGroupPlugin[0]));
        }
        return g;
    }
    
    public HashMap<String, Object> getGroupPlugins(){
        return pluginStore;
    }

	public int openGroupsCount() {
		return grps.size();
	}

    public synchronized Group[] currentGroupList () {
        if (grpsArr==null)
            grpsArr=(Group[]) grps.values().toArray(new Group[0]);
        return grpsArr; 
    } 

    public void updateStartingGroups (String[] sgNames) {
        Vector<Object> curr = (Vector) startGroups.clone();
        Vector<Object> currToLowerCase = (Vector) startGroupsToLowerCase.clone();
        Vector<String> updt = new Vector<String> ();
        Vector<String> updtToLowerCase = new Vector<String> ();
        for (int i = 0; i < sgNames.length; i++) {
        	int pos = sgNames[i].indexOf("/");
            String[] c = sgNames[i].split("/");
            String key = c[0].trim().toLowerCase(); 
            if (key.equals("exil"))
            	continue;
            updt.add(c[0].trim());
            updtToLowerCase.add(key);
            if (c.length>1)
                startGroupThemes.put(key, sgNames[i].substring(pos+1));
            else
                startGroupThemes.remove(key);
        }
        curr.removeAll(updt);
        currToLowerCase.removeAll(updtToLowerCase);
        updt.removeAll(startGroups);
        updtToLowerCase.removeAll(startGroupsToLowerCase);
        startGroups.addAll(updt);
        startGroupsToLowerCase.addAll(updtToLowerCase);
        startGroups.removeAll(curr);
        startGroupsToLowerCase.removeAll(currToLowerCase);
        synchronized (GroupManager.mgr) {
            for (Iterator<String> i = updt.iterator(); i.hasNext(); ) {
                String cName = (String) i.next();
                Group g = (Group) grps.get(cName);
                if (g==null)
                    continue;
                g.setState(IGroupState.ENTRANCE);
            }
            for (Iterator<Object> i = curr.iterator(); i.hasNext(); ) {
                String cName = (String) i.next();
                Group g = (Group) grps.get(cName);
                if (g==null)
                    continue;
                g.unsetState(IGroupState.ENTRANCE);
            }
        }
    }

	public void updateModeratedGroups (Vector<String> mg) {
   		Vector<String> removed = (Vector<String>) moderatedGroups.clone();
   		removed.removeAll(mg);
   		Vector<String> added = (Vector) mg.clone();
   		added.removeAll(moderatedGroups);
   		for (Enumeration<String> e = removed.elements(); e.hasMoreElements(); ) {
   			Group g = getGroup ((String) e.nextElement());
   			if (g==null)
   				continue;
			while (moderatedGroups.contains(g))
				moderatedGroups.remove(g);
   			g.unsetState (IGroupState.MODERATED);
   			g.setState(IGroupState.OPEN);
   		}
   		for (Enumeration<String> e = added.elements(); e.hasMoreElements(); ) {
   			String gName = (String) e.nextElement();
   			moderatedGroups.add(gName);
   			Group g = getGroup (gName);
   			moderatedGroups.add(g);
   			if (g==null)
   				continue;
   			g.setState(IGroupState.MODERATED);
   			g.unsetState (IGroupState.OPEN);
   			
   		}
	}
	
	public int checkReason (String groupname, User opener, User rUser) {            
        boolean moderated = moderatedGroups.contains(groupname.trim().toLowerCase());
        if (moderated && !rUser.hasRight(IUserStates.MAY_OPEN_MODERATED_GROUP))
                return IGroupReason.NO_RIGHT;
        
        Group g = getGroup(groupname);
        if (g == null){
            g = new Group (groupname, null, opener);
            if (isStartingGroup(g.getRawName())) {
                g.setState(IGroupState.ENTRANCE);
            } else {
                g.setState(IGroupState.ALLOW_USE_BBCODES);
                g.setState(IGroupState.SU_CAN_SETTHEME);
            }
        
            StringBuffer sb = new StringBuffer (Server.BASE_PATH);
            sb.append ("/grouppolicies/");
            sb.append (groupname.toLowerCase());
            sb.append (".properties");
            File f = new File (sb.toString());
            if (f.exists()) {
                g = checkProperties (f, g, rUser);
                if (g == null)
                    return IGroupReason.NO_RIGHT;
            }
        }
        if (opener.isUnregistered && g.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG))
            return IGroupReason.NOT_ALLOW_JOIN_UNREG;
        if (!g.isMembershipRoom(opener))
            return IGroupReason.RESERVED;
              
        return IGroupReason.CREATE;
    }
	
	private String checkGproupname(String grp){	    
	    for (Enumeration<Object> e = startGroups.elements(); e.hasMoreElements(); ) {
            String groupname =  (String) e.nextElement();
            if (groupname.toLowerCase().equals(grp.toLowerCase()))
                return groupname;
        }
	    return grp;
	}

    /**
     * @param grp The name of the group
     * @return true if this group is a startgroup, false if not
     */
    public boolean isStartingGroup(String grp) {
        return startGroupsToLowerCase.contains(grp.trim().toLowerCase());
    }

    /**
     * update groupListLastChange
     */
    public void updateGroupListLastModified () {
        groupListLastChange=System.currentTimeMillis();
    }
    /**
     * @return last grouplist modification
     */
    public long groupListLastModified() {
        return groupListLastChange;
    }
    
    /**
     * @return last usercount modification
     */
    public long lastModified() {
        return lastModified;
    }
}