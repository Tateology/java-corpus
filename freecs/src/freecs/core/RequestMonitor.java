/**
 * Copyright (C) 2004 andres
 * Created: 08.11.2004 (10:54:00)
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

import java.util.HashMap;
import java.util.Iterator;

import freecs.Server;


/**
 * @author andres
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RequestMonitor extends Thread {
    public static RequestMonitor instance = new RequestMonitor();
    private HashMap<Thread, Long> monitors = new HashMap<Thread, Long>();

    private RequestMonitor () {
    }
    
    public synchronized void addMonitor (Thread t, long timeout) {
        if (!this.isAlive() && Server.srv.isRunning())
            this.start();
        if (t == null)
            return;
        monitors.put(t, new Long(timeout));
    }

    public synchronized void removeMonitor (Thread t) {
        monitors.remove(t);
    }
    
    private void interruptMonitored(Thread t) {
        t.interrupt();
        /* if (t instanceof RequestReader) {
            ((RequestReader) t).cancelRequest();
        }*/
        Server.log ("RequestMonitor", "interrupted thread " + t.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
    }
    
    public void run () {
        long lastMessage = 0;
        while (Server.srv.isRunning()) {
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
            long sleepTime = 1000;
            long now = System.currentTimeMillis();
            synchronized (this) {
                for (Iterator<Thread> i = monitors.keySet().iterator(); i.hasNext(); ) {
                    Thread t = (Thread) i.next();
                    long timeout = ((Long) monitors.get(t)).longValue();
                    if (timeout <= now) {
                        interruptMonitored(t);
                        i.remove();
                        continue;
                    } else {
                        long thisTimeout = now - timeout;
                        if (thisTimeout < sleepTime)
                            sleepTime = thisTimeout;
                    }
                }
            }
            if (sleepTime < 33)
                sleepTime = 33;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                // doesn't happen
            }
        }
    }
    
    public String toString () {
        return "[RequestMonitor]";
    }
}