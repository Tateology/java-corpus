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
import freecs.interfaces.ICommand;
import freecs.interfaces.IReloadable;
import freecs.util.FileMonitor;
import freecs.content.MessageState;
import freecs.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CommandSet implements IReloadable {
	public static final byte UNKNOWN_COMMAND=-1;
	public static final byte TRUE=1;
	public static final byte FALSE=0;
	public static final byte INTERRUPTED = Byte.MIN_VALUE;
	private static final CommandSet cs = new CommandSet();
	private HashMap<String, Object>allCmds, availableCmds;
	private Properties props;	
	private File 	cfgFile = null;
	private boolean cfgFilePresent = false;
	private long	cfgFileLastModified;

	private CommandSet () {
		props = new Properties();
		allCmds 		= initAllCommands();
		availableCmds 	= initAvailableCommands ();
	}
	
	public void checkCommendSet (){
	       allCmds         = initAllCommands();
	       availableCmds   = initAvailableCommands ();
	}
	
	public Properties getCommandSetProps(){
	    return props;
	}

	private HashMap<String, Object> initAvailableCommands () {
		cfgFile = new File (Server.BASE_PATH + "/config", "command.properties");
		HashMap<String, Object> available = checkActivatedCommands ();
		FileMonitor.getFileMonitor().addReloadable (this);
		return available;
	}
	
	private HashMap<String, Object> checkActivatedCommands () {
		props = new Properties();
	    props.setProperty("f+", "false");
	    props.setProperty("f-", "false");

		if (cfgFile.exists() && cfgFile.isFile()) try {
			cfgFilePresent = true;
			cfgFileLastModified = cfgFile.lastModified();
			FileInputStream in = new FileInputStream(cfgFile);
			props.load(in);
			in.close();
		} catch (FileNotFoundException fnfe) {
			// never
		} catch (IOException ioe) {
			Server.log(this, "Unable to read command.properties", Server.MSG_ERROR, Server.LVL_MAJOR);
		} else {
			cfgFilePresent = false;
		}
		if (props == null)
			return allCmds;
		HashMap<String, Object> available = new HashMap<String, Object>();
		for (Iterator<String> i= allCmds.keySet ().iterator (); i.hasNext() ;) {
			String curr = (String) i.next();
			String key = curr.substring(1).toLowerCase();
			String value = curr = props.getProperty (key);
			if (curr != null 
				&& (curr.equals("off") 
					|| curr.equals ("false")))
				continue;
			StringBuilder cmdKey = new StringBuilder("/").append(key);
			ICommand cmd = (ICommand) allCmds.get(cmdKey.toString());
			available.put(cmdKey.toString(), cmd);
		}
		return available;
	}
	
	// FIXME: should be done automatically (placing a class which name
	// starts with Cmd into freecs.commands should automatically load
	// this class on startup)
	private HashMap<String, Object> initAllCommands() {
		HashMap<String, Object> all = new HashMap<String, Object> (Server.srv.allCommands);
		return all;
	}
	
	public static CommandSet getCommandSet () {
		return cs;
	}
	
	public ICommand getCommand (String cmd) {
		return (ICommand) allCmds.get(cmd);
	} 

	public byte evaluate (String cmd, MessageState msgState, String param) {
        try {
            return evaluate (cmd, msgState, param, false);
        } catch (Exception e) {
            Server.debug(cmd, "evaluation caused exception", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        return -1;
	}
	
	public byte evaluate (String cmd, MessageState msgState, String param, boolean moderated) {
        try {
    		if (!msgState.cb.isValid())
    			return (INTERRUPTED);
    		
    		if (cmd.startsWith("/flock")){
    		    if (!cmd.equals("/flock")) {
    			    StringBuffer param_temp = new StringBuffer(cmd.substring(6));
    			    param_temp.append(" ");
    			    param_temp.append(param);
    			    param = param_temp.toString();
    			    cmd = "/flock";
     		    } else {
    		    	StringBuffer param_temp = new StringBuffer("col");
    			    param_temp.append(" ");
    			    param_temp.append(param);
    			    param = param_temp.toString();
    			    cmd = "/flock";
     		    }
    		}   
    		
    		ICommand cmdObj = (ICommand) availableCmds.get(cmd);
    		if (!Server.srv.USE_BGCOLOR && cmd.equals("/bgcol"))
    			cmdObj=null;
    		if (cmdObj == null) 
    			return (UNKNOWN_COMMAND);
    		return (cmdObj.execute(msgState, param) ? TRUE : FALSE);
        } catch (Exception e) {
            Server.debug (cmd, "evaluation caused exception", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        return -1;
	}

	/**
	 * Interface IReloadable's methods are deffined below here
	 */
	public boolean filePresent() {
		return cfgFilePresent;
	}

	public File getFile() {
		return cfgFile;
	}

	public long lastModified() {
		return cfgFileLastModified;
	}

	public void changed() {
		Server.log (this, "changed: reloaded commandset", Server.MSG_STATE, Server.LVL_MINOR);
		availableCmds=checkActivatedCommands ();
	}

	public void removed() {
		Server.log (this, "removed: removed commandset", Server.MSG_STATE, Server.LVL_MINOR);
		availableCmds=checkActivatedCommands ();
	}
	
	public void created() {
		Server.log (this, "created: loaded commandset", Server.MSG_STATE, Server.LVL_MINOR);
		availableCmds=checkActivatedCommands ();
	}
	public String toString () {
		return ("[CommandSet]");
	}
}
