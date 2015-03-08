/**
 * Copyright (C) 2006  Rene M.
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
 * Created on 08.10.2006
 */

package freecs.content;

import freecs.Server;
import freecs.core.User;
import freecs.interfaces.IActionStates;

public class ActionstoreObject {
	public volatile String msg, storedBy, room, email;      // infos on how this action happened
    public volatile String usr, cookie; // properties, which are stored
    public volatile Connection con;
    public volatile User u;
    public volatile String cu;
    public volatile long time;
    public volatile int action;
    
    public ActionstoreObject (int action, String msg, String storedBy, String room, long time) {
    	this.action = action;
        this.storedBy = storedBy;
        this.room = room;
		this.time = time;
		this.msg = msg;
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
    
    public boolean equalsActionState(int state){
        if (this.action == state)
            return true;
        return false;
    }
    
    public String rendererActionState(){
        String actionString = null;
        if (this.action== IActionStates.FLOCKCOL)
            actionString ="FLOCKCOl";
        if (this.action== IActionStates.FLOCKAWAY)
            actionString ="FLOCKAWAY";
        if (this.action== IActionStates.FLOCKME)
            actionString ="FLOCKME";
        if (this.action== IActionStates.ISPUNISHABLE)
            actionString ="ISPUNISHABLE";
        if (this.action== IActionStates.PUNISH)
            actionString ="PUNISH";
        if (this.action== IActionStates.SUBAN)
            actionString ="SUBAN";
        if (action== IActionStates.IGNOREUSER)
            actionString ="IGNOREUSER"; 
        return actionString;
    }
    
    public void clearObject(){
        this.msg = null;
        this.storedBy = null;
        this.room = null;
        this.email = null;    
        this.usr = null;
        this.cookie = null;
        this.con = null;
        this.u = null;
        this.time = 0;
        this.action = 0;
    }
} 
