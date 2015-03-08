/**
 * Copyright (C) 2005 manfred andres
 * Created: 13.01.2005 (19:26:31)
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
package freecs.util.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.GZIPOutputStream;

import freecs.Server;


/**
 * LogFile is an object describing a logfile. LogFile will backup logfiles on a
 * "per day" basis. OldLogfiles will have the same name with the 
 * suffix "_YYYYMMDD" (the current date).
 * @author manfred andres
 *
 */
public class LogFile implements LogDestination {
    private static SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
    
    final String path;
    FileOutputStream fos;
    WritableByteChannel fc;
    int logFileDay;
    long logFileTimestamp;
    
    
    LogFile (String path) {
        this.path = path;
    }

	public WritableByteChannel getChannel() throws FileNotFoundException, IOException {
        if (fc == null) {
            fc = createNewLogFile();
            return fc;
        }
        LogWriter.instance.cal.setTimeInMillis(System.currentTimeMillis());
        if (logFileDay==LogWriter.instance.cal.get(Calendar.DAY_OF_MONTH))
            return fc;
        backupLogFile();
        fc = createNewLogFile();
        return fc;
    }
	
	public  WritableByteChannel getCloseChannel(boolean only)throws FileNotFoundException, IOException {  
		if (only)
			return fc;

		if (logFileTimestamp >0){
            long now = System.currentTimeMillis ();
            long diff = now-logFileTimestamp;
            if (diff > ((1000*60*60*24)* Server.srv.LOGFILE_DELDAYS)){
			    closeChannel();
		    }
		}
		return fc;				           
    }
	
    private WritableByteChannel createNewLogFile () throws FileNotFoundException, IOException {
        LogWriter.instance.cal.setTimeInMillis(System.currentTimeMillis());
        File f = new File (path);
        if (f.exists()) {
            LogWriter.instance.cal.setTimeInMillis(f.lastModified());
            if (logFileDay!=LogWriter.instance.cal.get(Calendar.DAY_OF_MONTH))
               backupLogFile();
            f = new File (path);
        }
        logFileTimestamp = System.currentTimeMillis ();
        logFileDay = LogWriter.instance.cal.get(Calendar.DAY_OF_MONTH);
        fos = new FileOutputStream (f);
        return fos.getChannel();
    }
    
    private void backupLogFile() throws IOException {
        LogWriter.instance.cal.setTimeInMillis(System.currentTimeMillis());
        String backupPath;
        if (path.indexOf(".")> -1)
            backupPath = path.substring(0, path.lastIndexOf(".")) 
                    + "_" + date.format(LogWriter.instance.cal.getTime()) 
                    + path.substring(path.lastIndexOf("."));
        else
            backupPath = path + "_" + date.format(LogWriter.instance.cal.getTime());
        if (fos != null) {
            fos.flush();
            if (fc != null)
                fc.close();
            fos.close();
        }
        File f = new File(path);
        File backupFile = new File (backupPath);
        f.renameTo(backupFile);
        new LogFileShrinker(backupFile).start();
    }
    
    private void  closeChannel() throws IOException {
        File f = new File (path);
        
    	if (fos != null) {
            fos.flush();
            if (fc != null)
                fc.close();
            fos.close();
        }
    	if (f.exists() && Server.srv.CAN_DEL_LOGS){
            Server.log("[LogFile]","delete File: "+path, Server.MSG_STATE, Server.LVL_MAJOR);
            f.delete();	  
        }
    	f=null;
    	fos=null;
    }
    
    public static void main (String arg[]) {
        File f = new File (arg[0]);
        LogFileShrinker lfs = new LogFileShrinker(f);
        lfs.start();
        System.out.println("Start");
        try {
            lfs.join();
        } catch (Exception e) { }
        System.out.println("DONE");
    }
    
    
    /**
     * Subclass responsible for zipping backed up logfiles
     */
    static class LogFileShrinker extends Thread {
        final File toZip;
        
        LogFileShrinker (File f) {
            toZip = f;
        }
        
        public void run() {
            String originalFile = toZip.getAbsolutePath();
            String compressedFile = originalFile + ".gz";
            FileInputStream fis = null;
            GZIPOutputStream gzos = null;
            FileOutputStream fos = null;
            try {
                try {
                    fis = new FileInputStream (toZip);
                } catch (IOException ioe) {
                    Server.debug ("LogFileShrinker", "Unable to open uncompressed file " + originalFile, ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
                    return;
                }
                
                try {
                	StringBuffer zippath = new StringBuffer(toZip.getAbsolutePath());
                    
                	StringBuffer addPath = new StringBuffer(zippath);
                    LogCleaner.instance.addLogDestination(addPath.append(".gz"));

                    File zipped = new File(zippath + ".gz");
                    StringBuffer path = new StringBuffer(zippath + ".gz");
                    int i = 0;
                    while (zipped.exists()){
                    	i++;
                        StringBuffer path_old = new StringBuffer(zippath);
                        zippath.append("[new").append(i).append("].log");
                    	path = new StringBuffer(zippath + ".gz");
                    	
                    	LogCleaner.instance.addLogDestination(path);
                    	zipped = new File (path.toString());
                    	if (zipped.exists()){
                    		zippath = new StringBuffer(path_old);
                            Server.log ("[LogFile]", "zippedfile exists", Server.MSG_STATE, Server.LVL_VERBOSE); 
                    	}
                    }
                    fos = new FileOutputStream (zipped);
                } catch (IOException ioe) {
                    Server.debug ("LogFileShrinker", "Unable to open zip file-destination " + compressedFile, ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
                    return;
                }
                try {
                    gzos = new GZIPOutputStream (fos);
                    int read;
                    while ((read = fis.read()) > -1)
                        gzos.write(read);
                    gzos.close();
                    gzos=null;
                    fos.close();
                    fos=null;
                    fis.close();
                    fis=null;
                    if (!toZip.delete()) 
                        Server.log("LogFileShrinker", "Unable to delete original file " + originalFile, Server.MSG_ERROR, Server.LVL_MAJOR);
                } catch (IOException ioe) {
                    Server.debug ("LogFileShrinker", "Unable to compress file " + originalFile + " to " + compressedFile, ioe, Server.MSG_ERROR, Server.LVL_MAJOR);
                }
            } finally {
                if (fis != null) try {
                    fis.close();
                } catch (Exception e) {
                    // is there anything to do about these situations?
                }
                if (gzos != null) try {
                    gzos.close();
                } catch (Exception e) { }
                if (fos != null) try {
                    fos.close();
                } catch (Exception e) { }
            }
        }
    }
}