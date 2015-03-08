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
import java.util.Iterator;
import java.util.Vector;

import freecs.interfaces.ICommand;
import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.Server;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdRespectUser extends AbstractCommand {
    private final String cmd= "/rig";
    private final String version = "1.02";
    private static final ICommand selve=new CmdRespectUser();

    private CmdRespectUser () { }
    
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
        if (param.length () < 1) {
            msgState.msgTemplate = "error.rig.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        if (msgState.sender.numberOfIgnoredUser() < 1) {
            msgState.msgTemplate = "error.rig.noIgnoredUser";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }

        String users[] = param.split (" ");
        Vector<Object> found = new Vector<Object>();
        Vector<Object> foundList = new Vector<Object>();
        for (int i = 0; i < users.length; i++) {
            Object o = UserManager.mgr.getUserByName(users[i]);
            if (o == null){
                if (!found.contains(users[i]) && msgState.sender.userIsIgnored(users[i])){
                    if (!Server.srv.isPermaIgnoredUser(msgState.sender.getName().toLowerCase(), users[i])){
                        found.add(users[i]);
                        foundList.add(users[i]);
                    }
                }
            } else {
                if (o instanceof User) {                     
                    User u = (User) o;
                    if (!found.contains(u.getName().toLowerCase()) && msgState.sender.userIsIgnored(u.getName().toLowerCase())){
                        if (!Server.srv.isPermaIgnoredUser(msgState.sender.getName().toLowerCase(), users[i])){
                            found.add(u.getName().toLowerCase());
                            foundList.add(u);
                        }
                    }
                }
            }
        }
        
        for (Iterator<Object> e = found.iterator(); e.hasNext(); ) {
            String uname = (String) e.next();
            StringBuffer tsb = new StringBuffer ("User ").append (msgState.sender.getName ()).append (" is respecting ").append (uname).append (" again");
            Server.log (this, tsb.toString (), Server.MSG_STATE, Server.LVL_VERBOSE);
            msgState.sender.respectUser (uname);
        }
        
        if (found.size()==0)
            return false;
        if (found.size()==1) {      
            msgState.msgTemplate="message.rig.singular";
            Object o = found.get(0);
            User u = UserManager.mgr.getUserByName((String) o);
            if (u != null) {
                msgState.usercontext = u;
                msgState.param = "";
            } else {
                msgState.usercontext = null;
                msgState.param = (String) o;
            }
        } else {
            msgState.msgTemplate="message.rig.plural";
            msgState.usrList = foundList.toArray();
        }
        msgState.sender.sendMessage(msgState.mp);
        found = null; 
        foundList = null;
        return true;
    }
    
    public String toString () {
        return ("[CmdRespectUser]");
    }
}
