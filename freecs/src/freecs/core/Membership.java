/**
 * Copyright (C) 2004 Manfred Andres
 * Created: 11.10.2004 (15:50:57)
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

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import freecs.Server;
import freecs.content.CallMembershipObject;
import freecs.interfaces.IContainer;
import freecs.interfaces.IMessageDestination;


/**
 * @author Manfred Andres
 *
 * Representation of one single membership. Implements interface IMessageDestination
 * making it possible to send Messages to alle users belonging to this Membership.
 * @see freecs.interfaces#IMessageDestination
 */
public class Membership implements IMessageDestination {
    private Vector<Object> members = new Vector<Object>();
    
    public final String key;
    private volatile String namePrefix, nameSuffix, stringRepresentation, mayUseTemplateset;
    private volatile boolean displayDefaultVipRighttitle = false, displayDefaultModGuestTitle = true, listedAsOnlinevip = true;
    private volatile int addedStates = 0, removedStates = 0;
    private volatile int addedDefaultStates = 0, removedDefaultStates = 0;
    private volatile long userTimeout = 0;
    private volatile Vector<CallMembershipObject> callKey = new Vector<CallMembershipObject>();
    private volatile int privatemessageStore = 0;
    
    public Membership (String key, Properties p) {
        if (key == null)
            throw new RuntimeException ("unable to construct membership without key");
        this.key = key;
        if (p == null && !key.equals("undefined"))
            throw new RuntimeException ("unable to construct membership without properties");
        init(p);
    }

    private void init (Properties p) {
        if (p==null)
            return;
        boolean checkNamePrefix = false,checkNameSuffix = false,checkStringRepresentation = false,checkAddedStates = false
        ,checkRemovedStates = false,checkAddedDefaultStates = false,checkRemovedDefaultStates = false,
        checkDisplayDefaultVipRighttitle = false,checkDisplayDefaultModGuestTitle = false,checkListedAsOnlinevip = false,
        checkUserTimeout = false, checkMayUseTemplateset = false, checkPrivatemessageStore = false;
        for (Iterator<Object> i = p.keySet().iterator(); i.hasNext(); ) {
            String currKey = (String) i.next();
            if (currKey.equalsIgnoreCase ("usernameprefix")) {
                namePrefix = p.getProperty(currKey);
                checkNamePrefix = true;
            } else if (currKey.equalsIgnoreCase ("usernamesuffix")) {
                nameSuffix = p.getProperty(currKey);
                checkNameSuffix = true;
            } else if (currKey.equalsIgnoreCase ("stringrepresentation")) {
                stringRepresentation = p.getProperty(currKey);
                checkStringRepresentation = true;
            } else if (currKey.equalsIgnoreCase ("addstates")) {
                String val = p.getProperty(currKey);
                String[] states = val.split(",");
                addedStates = 0;
                for (int j = 0; j < states.length; j++) {
                    addedStates = addedStates | UserManager.resolveState(states[j]);
                }
                checkAddedStates = true;
            } else if (currKey.equalsIgnoreCase ("removestates")) {
                String val = p.getProperty(currKey);
                String[] states = val.split(",");
                removedStates = 0;
                for (int j = 0; j < states.length; j++) {
                    removedStates = removedStates | UserManager.resolveState(states[j]);
                }
                checkRemovedStates = true;
            } else if (currKey.equalsIgnoreCase ("adddefaultstates")) {
                String val = p.getProperty(currKey);
                String[] states = val.split(",");
                addedDefaultStates = 0;
                for (int j = 0; j < states.length; j++) {
                    addedDefaultStates = addedDefaultStates | UserManager.resolveState(states[j]);
                }
                checkAddedDefaultStates = true;
            } else if (currKey.equalsIgnoreCase ("removedefaultstates")) {
                String val = p.getProperty(currKey);
                String[] states = val.split(",");
                removedDefaultStates = 0;
                for (int j = 0; j < states.length; j++) {
                    removedDefaultStates = removedDefaultStates | UserManager.resolveState(states[j]);
                }
                checkRemovedDefaultStates = true;
            } else if (currKey.equalsIgnoreCase ("displaydefaultviprighttitle") 
                    && p.getProperty(currKey).equalsIgnoreCase ("true")) {
            	displayDefaultVipRighttitle = true;
            	checkDisplayDefaultVipRighttitle = true;
            } else if (currKey.equalsIgnoreCase ("displaydefaultmodguesttitle") 
                    && p.getProperty(currKey).equalsIgnoreCase ("false")) {
            	displayDefaultModGuestTitle = false;
            	checkDisplayDefaultModGuestTitle = true;
            } else if (currKey.equalsIgnoreCase ("listedasonlinevip") 
                    && p.getProperty(currKey).equalsIgnoreCase ("false")) {
            	listedAsOnlinevip = false;
            	checkListedAsOnlinevip = true;
            } else if (currKey.equalsIgnoreCase ("usertimeout")) {  
            	try {
            	    Integer o = new Integer(p.getProperty(currKey));
                    userTimeout = o.longValue();
            	} catch (NumberFormatException n) {
                    Server.log("[Membership]", "clear userTimeout for Membership "+this.getName()+" "+n, Server.MSG_ERROR, Server.LVL_MAJOR);
            		userTimeout = 0;
            	}
            	checkUserTimeout = true;
            } else if (currKey.equalsIgnoreCase ("mayusetemplateset")) {
                mayUseTemplateset = p.getProperty(currKey);
                checkMayUseTemplateset = true;
            } else if (currKey.equalsIgnoreCase ("privatemessageStore")) {  
                try {
                    Integer o = new Integer(p.getProperty(currKey));
                    privatemessageStore = o.intValue();
                } catch (NumberFormatException n) {
                    privatemessageStore = 0;
                    Server.log("[Membership]", "clear privatemessageStore for Membership "+this.getName()+" "+n, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
                checkPrivatemessageStore = true;
            }
        }
        
        if (!checkNamePrefix)
        	namePrefix = null;
        if (!checkNameSuffix)
        	nameSuffix = null;
        if (!checkStringRepresentation)
        	stringRepresentation = null;
        if (!checkAddedStates)
        	addedStates = 0;
        if (!checkRemovedStates)
        	removedStates = 0;
        if (!checkAddedDefaultStates)
        	addedDefaultStates = 0;
        if (!checkRemovedDefaultStates)
        	removedDefaultStates = 0;
        if (!checkDisplayDefaultVipRighttitle)
        	displayDefaultVipRighttitle = false;
        if (!checkDisplayDefaultModGuestTitle)
        	displayDefaultModGuestTitle = true;
        if (!checkListedAsOnlinevip)
        	listedAsOnlinevip = true;
        if (!checkUserTimeout)
        	userTimeout = 0; 
        if (!checkMayUseTemplateset)
        	mayUseTemplateset = null;
        if (!checkPrivatemessageStore)
            privatemessageStore = 0;
    }

    public synchronized void update (Properties p) {
        init(p);
        for (Iterator<Object> i = members.iterator(); i.hasNext(); ) {
            User u = (User) i.next();
            u.rebuildMemberships();
        }
    }
    
    public void add (User u) {
        u.addMembership (this.key, this);
        
        // add addedStates and remove removedStates to/from this users permissionmap
        int pMap = u.getPermissionMap();
        pMap = pMap | addedStates;
        pMap = pMap - (pMap & removedStates);
        Server.log ("["+this.key+"]", "setting user-state-map for user " + u.getName() + " to value " + pMap, Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
        u.setPermission(pMap);
        
        int dpMap = u.getDefaultMembershipPermissionMap();
        dpMap = dpMap | addedDefaultStates;
        dpMap = dpMap - (dpMap & removedDefaultStates);
        Server.log ("["+this.key+"]", "setting user-defaultmembership-state-map for user " + u.getName() + " to value " + dpMap, Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
        u.setDefaultMembershipPermission(dpMap);
        
        if (dpMap >0 && dpMap != pMap) {
        	Server.log ("["+this.key+"]", "setting user-defaultstate-map for user " + u.getName() + " to value " + dpMap, Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
        	u.setDefaultPermissionMap(dpMap);
        }
        if (mayUseTemplateset != null)
            u.mayUseTemplateset(mayUseTemplateset);
    }
    
    public void remove (User u) {
        members.remove(u);
        if (members.size() == 0){
            callKey = new Vector<CallMembershipObject>();
            Server.log(this.key, "reset callkey for Membership "+this.key, Server.MSG_STATE, Server.LVL_VERBOSE);
        } 
    }
    
    public void addToList(User u){
        if (!members.contains(u))
            members.add(u);
    }
    
    public void cleanup () {
        for (Iterator<Object> i = members.iterator(); i.hasNext(); ) {
            User u = (User) i.next();
            u.removeMembership(this.key);
        }
    }
    
    
    // Interface IMessageDestination
    
    /**
     * Send a message to all users having this membership
     * @see freecs.interfaces.IMessageDestination#sendMessage(freecs.interfaces.IContainer)
     */
    public void sendMessage(IContainer mc) {
        User[] uArr = (User[]) members.toArray(new User[0]);
        for (int i = 0; i < uArr.length; i++) {
            uArr[i].sendMessage(mc);
        }
    }

    public void sendHistory(User u){
        for (Iterator<CallMembershipObject> c = callKey.iterator(); c.hasNext();) {
            CallMembershipObject ca = (CallMembershipObject) c.next();
            MessageParser mp = new MessageParser();
            mp.setMessageTemplate("message.mcall.history");
            mp.setUsercontext(ca.cu);
            mp.setMessage(ca.message);
            mp.setParam(ca.param);
            u.sendMessage(mp);
        }
    }
    
    public synchronized void addCallKey(CallMembershipObject cm){
        callKey.add(cm);     
        int max = Server.srv.MAX_MCALL_KEY;
        int diff = callKey.size()-max;
        if (diff > 0){
            int i = 0;
            Vector<CallMembershipObject> tmp = new Vector<CallMembershipObject>(callKey);
            for (Iterator<CallMembershipObject> c = tmp.iterator(); i<=diff;) {
                CallMembershipObject ca = (CallMembershipObject) c.next();
                i++;
                callKey.remove(ca);             
            }
        }                    
    }

    /**
     * Iterator over all users having this membership
     * @see freecs.interfaces.IMessageDestination#users()
     */
    public Iterator<Object> users() {
        return members.iterator();
    }

    /**
     * return the name of this membership
     * @see freecs.interfaces.IMessageDestination#getName()
     */
    public String getName() {
        if (stringRepresentation != null)
            return stringRepresentation;
        return key;
    }
    
    public String getNamePrefix() {
    		return namePrefix;
    }
    
    public String getNameSuffix() {
    		return nameSuffix;
    }
    
    public boolean displayDefaultVipRighttitle() {
    	return displayDefaultVipRighttitle;
    }
    
    public boolean displayDefaultModGuesttitle() {
    	return displayDefaultModGuestTitle;
    }
    
    public boolean listedAsOnlinevip() {
    	return listedAsOnlinevip;
    }
    
    public long userTimeout() {
    	return userTimeout;
    }
    
    public String mayUseTemplateset(){
    	return mayUseTemplateset;
    }
    
    public int getPrivatemessageStore() {
        return privatemessageStore;
    }

    public int hashCode() {
        return key.hashCode();
    }
    
    public boolean equals (Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Membership))
            return false;
        Membership foreign = (Membership) o;
        return foreign.key.equals(this.key);
    }
    
    public String toString() {
        return this.stringRepresentation;
    }
}