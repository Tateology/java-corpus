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
 * Created on 27.02.2004
 */

package freecs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import freecs.Server;
import freecs.content.MessageState;
import freecs.core.Group;
import freecs.core.User;

/**
 * @author Manfred Andres
 *
 * freecs.util
 */
public class GroupUserList {
    HashMap<Group, Vector<User>> ht = new HashMap<Group, Vector<User>>();
    public GroupUserList () {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
    
    public void addUser (User u, Group g) {
        if (g==null)
            g = u.getGroup();
        Vector<User> v = ht.get(g);
        if (v == null)
            v = new Vector<User>();
        v.addElement(u);
        ht.put(g,v);
    }
    
    public Iterator<Vector<User>> iterator () {
        return ht.values().iterator();
    }
    
    public void sendMessage (MessageState ms, String tpl, boolean target) {
        String singular = tpl + ".singular";
        String plural = tpl + ".plural";
        ms.useRenderCache=false;
        List<Object> l = new ArrayList<Object>();
        for (int i = 0; i < ms.usrList.length; i++)
            l.add(ms.usrList[i]);
        l.add(ms.sender);
        for (Iterator<Group> i = ht.keySet().iterator(); i.hasNext(); ) {
            Group g = i.next();
            Vector<User> v = ht.get(g);
  
            if (v == null || v.size()==0)
                continue;
            if (v.size()==1) {
                ms.msgTemplate=singular;
                ms.usercontext = (User) v.get(0);
            } else if (v.size() > 1) {
                ms.usrList = v.toArray();
                ms.msgTemplate=plural;
            }
            if (target) {
                ms.targetGroup = g;
                g.exclusiveSendMessage(ms.mp, l);
            } else {
                ms.sourceGroup = g;
                g.exclusiveSendMessage(ms.mp, l);
            }
        }
   }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}