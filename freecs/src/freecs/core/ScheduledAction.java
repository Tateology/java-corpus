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
import freecs.interfaces.IActionStates;

/**
 * used to schedule actions of a user
 *
 * freecs.core
 */
public class ScheduledAction {
   public static short  UNPUNISH = 1;

   User usr = null, sender = null;
   short action = 0;
   long startTime = 0;

   public ScheduledAction (short action, long startTime, User target, User sender) {
      this.action = action;
      this.startTime = startTime;
      this.usr = target;
      this.sender = sender;
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public long getStartTime () { return startTime; }

   public void execute () {
      if (action == 0) return;
      MessageParser mp = new MessageParser ();
      if (action == UNPUNISH) {
         usr.setPunish (false);
         mp.setMessageTemplate ("message.rgag.singular");
         mp.setUsercontext (usr);
         mp.setSender (sender);
         Server.srv.removeStore(usr.getName().toLowerCase(), IActionStates.PUNISH);
      }
      Group g = usr.getGroup ();
      if (g != null)
         g.sendMessage (mp);
   }

   public boolean equals (Object o) {
       if (!(o instanceof ScheduledAction))
           return false;
       ScheduledAction sa = (ScheduledAction) o;
       return this.action == sa.getAction () && this.usr == sa.getUser () && this.sender == sa.getSender (); 
   }
   public short getAction () { return action; }
   public User getUser () { return usr; }
   public User getSender () { return sender; }
   public int hashCode () { return usr.hashCode (); }

   public void finalize() {
       if (Server.TRACE_CREATE_AND_FINALIZE)
           Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }
}