/**
 * Copyright (C) 2004 ManfredAndres
 * Created: 11.10.2004 (15:23:50)
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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import freecs.Server;
import freecs.interfaces.IReloadable;
import freecs.util.FileMonitor;


/**
 * @author Manfred Andres
 *
 * This Class acts as a single point for memberships. The definitions
 * for all Memberships will be loaded here and the corresponding 
 * Membership-objects will also be constructed here. All Membership-objects
 * will be unique, meaning, that there will only be one instance of each
 * Membership-object, having the same name. This makes it possible to
 * check equality of Membership-objects by using "==".
 */
public class MembershipManager implements IReloadable {
    public static MembershipManager instance = new MembershipManager();
    public static final Membership undefined = new Membership ("undefined", null);
    public volatile HashMap<String, Membership> memberships = new HashMap<String, Membership>();

    private File configFile;
    private long configLastModified;
    private boolean configFileAvailable;
    
    private MembershipManager() {
        init();
        FileMonitor.getFileMonitor ().addReloadable(this);
    }

    /**
     * returns a reference to the membership config. by default called 
     * membership.properties in server config directory
     * @see freecs.Server#getConfigDir()
     */
    public static File getDefaultConfigFile() {
        return new File(Server.srv.getConfigDir(), "membership.properties");
    }
    
    private void init() {
        init (getDefaultConfigFile());
    }

    private void init(File configFile) {
        try {
            this.configFile = configFile;
            this.configLastModified = configFile.lastModified();
            this.configFileAvailable = configFile.exists();
            FileInputStream in = new FileInputStream (configFile);
            Properties props = new Properties ();
            props.load (in);
            in.close ();
            init (props);
        } catch (Exception e) {
            Server.debug (null, "can't read authentication config from " + configFile + " (" + e.toString() + ")", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
    }

    private void init (Properties props) {
        // seperate the propterties for each membership
        HashMap<String, Properties> hm = new HashMap<String, Properties>();
        for (Iterator<Object> i = props.keySet().iterator(); i.hasNext(); ) {
            String currKey = (String) i.next();
            int idx = currKey.indexOf(".");
            if (idx < 0)
                continue;
            String currMembershipKey = currKey.substring(0, idx);
            String currValue = props.getProperty(currKey);
            Properties p = (Properties) hm.get(currMembershipKey);
            if (p == null) {
                p = new Properties();
                hm.put(currMembershipKey, p);
            }
            p.setProperty(currKey.substring(idx+1), currValue);
        }
        
        // create or update the membership-objects according to the config
        HashMap<String, Membership> tempMap = new HashMap<String, Membership>();
        Vector<Membership> newMemberships = new Vector<Membership>();
        for (Iterator<String> i = hm.keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            Membership m = (Membership) memberships.get(key);
            if (m!=null) {
                // update membership-object
                m.update((Properties) hm.get(key)); 
            } else {
                // a new membership-object has been found
                m = new Membership(key, (Properties) hm.get(key));
                newMemberships.add(m);
            }
            tempMap.put(key, m);
        }
        // get the membership-objects, which have been removed
        Set<String> old = memberships.keySet();
        Set<String> fresh = tempMap.keySet();
        old.removeAll(fresh);
        for (Iterator<String> i = old.iterator(); i.hasNext(); ) {
            String key = ((String) i.next()).trim().toLowerCase();
            Membership m = (Membership) memberships.get(key);
            m.cleanup();
        }
        memberships = tempMap;
        
        User[] usrArr = UserManager.mgr.ustr.toArray();
        for (int i = 0; i < usrArr.length; i++) {
            User u = usrArr[i];
            for (Iterator<Membership> j = newMemberships.iterator(); j.hasNext(); ) {
                Membership m = (Membership) j.next();
                if (u.getMembership(m.key) == undefined)
                    u.addMembership(m.key, m);
            }
        }
    }
    
    /**
     * This method is used to retrieve membership-objects corresponding
     * to the given key.
     * @param key the key to the Membership-object
     * @return the Membership-object identified by the given key
     */
    public Membership getMembership(String key) {
        return (Membership) memberships.get(key.trim().toLowerCase());
    }

    
    // INTERFACE IReloadable

    /**
     * returns the config-file, which will be checked for
     * changes.
     * @see freecs.interfaces.IReloadable#filePresent()
     */
    public File getFile() {
        return configFile;
    }

    /**
     * returns true, if the monitored file was present the last time
     * @see freecs.interfaces.IReloadable#filePresent()
     */
    public boolean filePresent() {
        return this.configFileAvailable;
    }

    /**
     * returns the last known modification-time of this file
     * @see freecs.interfaces.IReloadable#lastModified()
     */
    public long lastModified() {
        return this.configLastModified;
    }

    /**
     * the monitored file has been changed
     * @see freecs.interfaces.IReloadable#changed()
     */
    public void changed() {
        synchronized (this) {
            this.init(configFile);
        }
    }

    /**
     * the monitored file has been removed
     * @see freecs.interfaces.IReloadable#removed()
     */
    public void removed() {
        synchronized (this) {
            this.init (new Properties());
        }
    }

    /**
     * the monitored file has been created
     * @see freecs.interfaces.IReloadable#created()
     */
    public void created() {
        synchronized (this) {
            this.init (configFile);
        }
    }
}
