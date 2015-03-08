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

import java.util.Iterator;
import freecs.*;
import freecs.interfaces.IThreadManagerPlugin;

public class CleanupClass extends Thread {

   public CleanupClass () {
   }

   public void run () {
       this.setName("CleanupClass");
       Server.log (this, "starting to clean up", Server.MSG_STATE, Server.LVL_MAJOR);
       Server.srv.startShutdown ();
       MessageParser mpr = new MessageParser ();
       mpr.setMessageTemplate ("message.server.shutdown");
       Server.log (this, "sending shutdownmessages...", Server.MSG_STATE, Server.LVL_MAJOR);
       Server.log (this, "Logout users...", Server.MSG_STATE, Server.LVL_MAJOR);
       UserManager.mgr.sendMessage(mpr);
       Server.log (this, "Users logged out.", Server.MSG_STATE, Server.LVL_MAJOR);
       Server.log (this, "Closing all connections...", Server.MSG_STATE, Server.LVL_MAJOR);

       // 30000 millis before the CentralSelector will be forced to stop
       long killTime = System.currentTimeMillis() + 60000;
       while (!CentralSelector.stopped) try {
           if (killTime < System.currentTimeMillis()) {
               Server.log (this, "CentralSelector didn't shutdown within 60000 millis", Server.MSG_STATE, Server.LVL_VERBOSE);
               break;
           }
           System.out.print (".");
           Thread.sleep (1000);
       } catch (InterruptedException ie) {}
       synchronized (UserManager.mgr.ustr) {
           for (Iterator<Object> i = UserManager.mgr.users (); i.hasNext (); ) try {
               User u = (User) i.next ();
               i.remove();
               u.removeNow();
           } catch (Exception e) {
               Server.debug (this, "caused Exception while removing user: ", e, Server.MSG_STATE, Server.LVL_MAJOR);
           }
       }
       
       if (Server.srv.threadManager != null) {
           Server.log(this, "---- stopping ThreadManager ----", Server.MSG_STATE, Server.LVL_MAJOR);
           IThreadManagerPlugin[] thm = Server.srv.threadManager;
           if (thm != null) {
               for (int s = 0; s < thm.length; s++) {                       
                   try {
                       thm[s].stopManager();                      
                   } catch (Exception e) {
                       Server.debug(thm[s],"catched exception from ThreadManager Plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                   }                        
               }
           }
       }      

       Server.log (this, "Shutting down authentication", Server.MSG_STATE, Server.LVL_MAJOR);
       try {
           Server.srv.auth.shutdown ();
       } catch (Exception e) {
           Server.debug (this, "caused Exception while shutting down authentication: ", e, Server.MSG_STATE, Server.LVL_MAJOR);
       }
       Server.log (this, "Final cleanup done. Exiting JVM.", Server.MSG_STATE, Server.LVL_MAJOR);
   }
    
   public String toString () {
       return "[CleanupClass]";
   }
}