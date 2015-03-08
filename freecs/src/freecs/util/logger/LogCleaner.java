/**
 * Copyright (C) 2007  Rene M.
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
 * Created on 16.02.2007
 */

package freecs.util.logger;

import java.io.File;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import freecs.Server;



/**
 * @author Rene M
 *
 * freecs.util
 */
public class LogCleaner extends Thread {
	public static LogCleaner instance = new LogCleaner();
	private HashMap<String, LogDestination> logDestinations = new HashMap<String, LogDestination>();
    
	public static void startLogCleaner () {
		if (instance.isAlive()) {
			return;
		}
		instance.setName("LogCleaner");
		instance.setPriority(Thread.MAX_PRIORITY-3);
		instance.start();
    }

	
	public void run() {
        long lastFileCheck=0;
        while (Server.srv.isRunning ()) {
        	try {
        		LogWriter.instance.cal.setTimeInMillis(System.currentTimeMillis());
        		long now = System.currentTimeMillis (),diff = now-lastFileCheck;
			    HashMap<String, LogDestination> ldWorkingCopy = new HashMap<String, LogDestination>(logDestinations);
			    if (lastFileCheck==0 || diff > 20000) {
			        Iterator<String>  it = ldWorkingCopy.keySet().iterator();
			        Vector<String> obj = new Vector<String>();
			        while (it.hasNext()){
			            obj.add(it.next());
			        }
			        int i=0;
			        for (Enumeration<String> e = obj.elements(); e.hasMoreElements(); ) {
			    		StringBuffer path = new StringBuffer((String) e.nextElement());
			    		if (Server.DEBUG)
			    		    Server.log("[LogCleaner]","run: check"+path.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
			            lastFileCheck = System.currentTimeMillis ();
			            LogDestination ld = (LogDestination) ldWorkingCopy.get(path.toString());
			            if (Server.DEBUG && ld!=null)
			                Server.log("[LogCleaner]","run: find LD:"+ld+" hour:"+LogWriter.instance.cal.get(Calendar.HOUR_OF_DAY)+" Channel:"+ld.getCloseChannel(true), Server.MSG_STATE, Server.LVL_MAJOR);
			                			            
			            if (ld != null && LogWriter.instance.cal.get(Calendar.HOUR_OF_DAY)== Server.srv.LOGFILE_DELHOUR){
			    	        if (ld.getCloseChannel(true)!= null && ld.getCloseChannel(true).isOpen()){
			    	            ld.getCloseChannel(false);
			    		        if (!ld.getCloseChannel(true).isOpen()) {
			    		        	removeDest(path.toString(), ldWorkingCopy);
			    		    	    Server.log("[LogCleaner]","close Channel: "+ld.getCloseChannel(true), Server.MSG_STATE, Server.LVL_MAJOR);
			                        Server.log("[LogCleaner]","remove Path: "+path.toString(), Server.MSG_STATE, Server.LVL_VERBOSE);
			    		            i++;
			    		        }
			    		    }   
			    		} else if (LogWriter.instance.cal.get(Calendar.HOUR_OF_DAY)== Server.srv.LOGFILE_DELHOUR && path.toString().length()>0) {
			    			       if (Server.DEBUG)
		                               Server.log("[LogFile]","search File: "+path.toString(), Server.MSG_STATE, Server.LVL_VERBOSE);

			                       File f = new File (path.toString());
			                       long logFileTimestamp = f.lastModified();
			                       now = System.currentTimeMillis ();diff = now-logFileTimestamp;
			                       if (diff > ((1000*60*60*24)* Server.srv.LOGFILE_DELDAYS)){
			            	           if (f.exists() && Server.srv.CAN_DEL_LOGS){
				                           Server.log("[LogCleaner]","delete File: "+path, Server.MSG_STATE, Server.LVL_MAJOR);
				                           f.delete();	  
				                       }
		                               Server.log("[LogCleaner]","remove Path: "+path.toString(), Server.MSG_STATE, Server.LVL_VERBOSE);
			            	           removePath(path.toString(), ldWorkingCopy);
			            	           i++;
					              }
			    		       }
			            if (i >= 50)
			            	break;
			    }
			    
			} try {
				Thread.sleep (1000);
			} catch (InterruptedException ie) {}
				} catch (Exception e) {
					Server.debug (this, "run:", e, Server.MSG_ERROR, Server.LVL_MAJOR);
				}
		    }
	}	
	
	private void removePath(String path, HashMap<String, LogDestination> ldWorkingCopy) {
		ldWorkingCopy.remove(path.toString());
        logDestinations.remove(path.toString());
	}


	private void removeDest(String path, HashMap<String, LogDestination> ldWorkingCopy) {
	     LogWriter.instance.removeLogDestinations(path.toString());
	     ldWorkingCopy.remove(path.toString());
	     logDestinations.remove(path.toString());
	}


	public void addLogDestination(StringBuffer zippath){
    	if (!logDestinations.containsKey(zippath.toString())){
            logDestinations.put(zippath.toString(), null);
            Server.log ("[LogCleaner]", "add File "+zippath, Server.MSG_STATE, Server.LVL_VERBOSE); 
    	}
    }
	
	public void addLogDestination(String path, LogDestination ld){
	    if (!logDestinations.containsKey(path)){
	        logDestinations.put(path, ld);
		    Server.log ("[LogCleaner]", "add File "+path, Server.MSG_STATE, Server.LVL_VERBOSE); 
	    }
    }
	
	public HashMap<String, LogDestination> getLogDestinations(){
		return logDestinations;
	}
	     
}

    
    