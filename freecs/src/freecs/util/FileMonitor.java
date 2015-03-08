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
package freecs.util;

import freecs.Server;
import freecs.interfaces.IReloadable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

public class FileMonitor extends Thread {
	private Vector<IReloadable> watchlist;
	private static final FileMonitor fm = new FileMonitor();

	public FileMonitor () {
		watchlist = new Vector<IReloadable> ();
	}

	public static FileMonitor getFileMonitor () {
		if (!fm.isAlive()) {
            fm.setName("FileMonitor");
			fm.setPriority (MIN_PRIORITY);
			fm.start ();
		}
		return fm;
	}

	public void addReloadable (IReloadable r) {
        if (!watchlist.contains(r))
            watchlist.addElement (r);
	}

	public void run () {
        long lastMessage=0;
		while (Server.srv.isRunning ()) {
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
			for (Enumeration e = watchlist.elements (); e.hasMoreElements (); ) {
				IReloadable cr = (IReloadable) e.nextElement ();
				File cf = cr.getFile ();
				if (cf == null) {
					StringBuffer tsb = new StringBuffer (": IReloadable has no file! "). append (cr.toString ());
					Server.log (this, tsb.toString (), Server.MSG_ERROR, Server.LVL_MAJOR);
					watchlist.remove (cr);
					continue;
				}
				boolean wasPresent = cr.filePresent();
				if (wasPresent && !cf.exists()) try {
					cr.removed();
                    removeMonitor(cr);
					continue;
				} catch (Exception ex) {
                    Server.debug (this, "remove for " + cf.getName() + " caused exception", ex, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
				if (!wasPresent && cf.exists()) try {
					cr.created();
					continue;
                } catch (Exception ex) {
                    Server.debug (this, "created for " + cf.getName() + " caused exception", ex, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
				if (cf.lastModified () != cr.lastModified ()) try {
					cr.changed ();
                } catch (Exception ex) {
                    Server.debug (this, "changed for " + cf.getName() + " caused exception", ex, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
			}
			try {
				Thread.sleep (Server.srv.FILE_CHECK_INTERVAL);
			} catch (Exception e) { }
		}
	}
    
    public String toString() {
        return "[FileMonitor]";
    }

    /**
     * @param set
     */
    public void removeMonitor(Object obj) {
        this.watchlist.remove(obj);
    }
}