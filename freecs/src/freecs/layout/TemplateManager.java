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
package freecs.layout;

import freecs.Server;
import freecs.interfaces.IReloadable;
import freecs.util.FileMonitor;

import java.io.*;
import java.util.*;

public class TemplateManager implements IReloadable {
   private HashMap tSets;
   public static TemplateManager mgr;
   public static File reloadable;
   public static long lastModified=0;

   public TemplateManager() throws IOException {
      tSets = new HashMap();
      loadTemplates();
      TemplateManager.mgr = this;
      lastModified = reloadable.lastModified();
      FileMonitor.getFileMonitor ().addReloadable (this);
   }

    public TemplateSet getTemplateSet () {
        return getTemplateSet("default");
    }
    public TemplateSet getTemplateSet (String name) {
        if (name == null || !tSets.containsKey (name))
           return (TemplateSet) tSets.get("default");
        return (TemplateSet) tSets.get(name);
     }

   private void loadTemplates() throws IOException {
      StringBuffer tsb = new StringBuffer (Server.BASE_PATH).append ("/templatesets");
      File tFile = new File(tsb.toString ());
      if (!tFile.exists()) {
         if (!tFile.mkdir()) { throw new IOException("Unable to create directory 'templateset'"); }
         tsb = new StringBuffer (Server.BASE_PATH).append ("/templatesets/default");
         tFile = new File(tsb.toString ());
         if (!tFile.exists()) {
            if (!tFile.mkdir()) { throw new IOException("Unable to create directory 'default'"); }
         }
         throw new IOException("No templates available. (Directories created)");
      }
      if (!tFile.isDirectory()) {
      	throw new IOException("'templatesets' isn't a directory"); 
      }
      reloadable=tFile;
	  File defaultTs = new File (tFile.getCanonicalPath(), "default");
	  if (!defaultTs.exists()) {
	  	 throw new IOException ("The default-layout was not present!");
	  }
	  constructTemplateSet(defaultTs);
      File fList[] = tFile.listFiles ();
      for (int i = 0; i < fList.length; i++) {
         if (!fList[i].isDirectory () || fList[i].getName().equals("default")) 
         	continue;
         constructTemplateSet(fList[i]);
      }
      if (!tSets.containsKey("default")) throw new IOException ("Unable to read default-template-set");
   }

	private void constructTemplateSet (File f) throws IOException {
		TemplateSet tSet = new TemplateSet (f, this);
		if (!tSet.isValide ()) 
			return;
		StringBuffer tsb = new StringBuffer ("parsed valide templateset ");
		tsb.append (tSet.getName ());
		Server.log(this, tsb.toString (), Server.MSG_STATE, Server.LVL_MINOR);
		tSets.put (f.getName (), tSet);
	}
    
    public String toString() {
        return "[TemplateManager]";
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#getFile()
     */
    public File getFile() {
        return reloadable;
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#filePresent()
     */
    public boolean filePresent() {
        return reloadable.exists();
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#lastModified()
     */
    public long lastModified() {
        return lastModified;
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#changed()
     */
    public void changed() {
        lastModified = reloadable.lastModified();
        if (!reloadable.isDirectory()) {
            Server.log (this, "templateset-directory is a file NOT a directory!", Server.MSG_STATE, Server.LVL_MAJOR);
            return;
        }
        File files[] = reloadable.listFiles();
        for (int i = 0; i < files.length; i++) {
            TemplateSet ts = (TemplateSet) tSets.get(files[i].getName());
            if (ts == null) try {
                Server.log (this, "constructing newly added templateset " + files[i].getName(), Server.MSG_STATE, Server.LVL_MAJOR);
                constructTemplateSet (files[i]);
                continue;
            } catch (IOException ioe) {
                Server.debug (this, "constructTemplateSet caused Exception for " + files[i].getName(), ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
            }
            ts.reload(files[i]);
        }
        for (Iterator i = this.tSets.keySet().iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            File subdir = new File(reloadable, name);
            if (!subdir.exists()) {
                Server.log (this, "removing templateset " + name, Server.MSG_STATE, Server.LVL_MAJOR); 
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#removed()
     */
    public void removed() {
        Server.log (this, "templateset-directory has been removed!", Server.MSG_STATE, Server.LVL_MAJOR);
    }

    /* (non-Javadoc)
     * @see freecs.interfaces.IReloadable#created()
     */
    public void created() {
        changed();
    }
}
