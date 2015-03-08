/**
 * Copyright (C) 2004 Manfred Andres
 * Created: 23.05.2004 (22:53:20)
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
package freecs.interfaces;

import java.util.Properties;

import freecs.core.Group;
import freecs.core.MessageParser;
import freecs.core.User;


/**
 * @author Manfred Andres
 * 
 * This interface describes the functions, which will be triggerd at the
 * apropriate event taking place inside the group a plugin is attached to.
 */
public interface IGroupPlugin {
    /**
     * retrieves an instance of this GroupPlugin for the given group.
     * @param namespace the namespace deffined within the group.properties for this plugin
     * @param g the group, this instance will be attached to
     * @param props the properties given for this extension
     * @return the GroupPlugin initiated for this group
     * @throws Exception If the implementation is unable to initialize an instance for the given group
     */
    public abstract IGroupPlugin instanceForGroup(String namespace, Group g, Properties props) throws Exception;
    public abstract void remove(Group g);
    public abstract void remove(String namespace);

    /**
     * Called if a user joins the group this GroupPlugin is attached to.
     * @param u the user joining
     */
    public abstract void usrJoin(User u);
    
    /**
     * Called if a user leaves the group this GroupPlugin is attached to.
     * @param u
     */
    public abstract void usrLeaving(User u);
    
    /**
     * Called for every action of any user, which any user may receive within the group this GroupPlugin is attached to.
     * @param mpr the messageparser-object containing all informations of this action
     */
    public abstract void usrAction(MessageParser mpr);
	public abstract void grpToUsrAction(MessageParser mp);
}
