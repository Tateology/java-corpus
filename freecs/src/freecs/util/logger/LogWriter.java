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
 * Created on 05.10.2003
 */

package freecs.util.logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;

import freecs.Server;
import freecs.content.MessageState;
import freecs.util.ObjectBuffer;

/**
 * @author Manfred Andres
 *
 * freecs.util
 */
public class LogWriter extends Thread {
	public static LogWriter instance = new LogWriter();
    private HashMap<String, LogDestination> logDestinations = new HashMap<String, LogDestination>();
	public ObjectBuffer logQueue = new ObjectBuffer(1000);
    private Charset cs = Charset.forName(System.getProperty("file.encoding", "iso-8859-1"));

    private boolean stopped = false;
    Calendar cal = Calendar.getInstance ();

	private LogWriter () {
        this.setPriority(Thread.MAX_PRIORITY);
        this.start();
    }

	/**
     * retrieve a logDestination to write to
	 * @param path the path to the logfile
	 * @return the LogDestination object
	 */
    LogDestination getLogDestination (String path) {
        LogDestination ld = (LogDestination) logDestinations.get(path);
        if (ld == null) {
            // TODO: check for file, socket,..) and construct the correct LogDestination
            ld = new LogFile (path);
            logDestinations.put(path, ld);
        }
        return ld;
    }

    /**
     * this method is used to write messages to a logdestination
     * @param path the path for this logdestination
     * @param message the message to be logged
     */
    public void addLogMessage (String path, String message) {
        LogDestination ld = getLogDestination(path);
        StringBuffer sb = new StringBuffer(
                Server.formatDefaultTimeStamp(System.currentTimeMillis()));
        sb.append (": ");
        sb.append (message);
        sb.append (System.getProperty("line.separator", "\r\n"));
        LogEntry le = new LogEntry (ld, cs.encode (sb.toString()));
        addLogElement(le);
    }
    
    /**
     * 
     * @param type
     * @param message
     */
    public void addMessageLogMessage (MessageState messageState, int type, String message) {
    	String Regex [] ={"*",":","?","<",">","/","\"","\\"};
		StringBuffer r_new = new StringBuffer(messageState.sender.getGroup().getRawName());
		for (int i = 0; i < Regex.length; i++) {
		    r_new = new StringBuffer(r_new.toString().replace(Regex[i], "-"));
		}
		if (!messageState.sender.getGroup().getRawName().equals(r_new.toString())){
            Server.log("[LogWriter]","rename file "+messageState.sender.getGroup().getRawName()+" to "+r_new.toString(), Server.MSG_STATE, Server.LVL_MAJOR);
    	}
		StringBuffer dest = new StringBuffer(Server.LOGFILE[Server.MSG_MESSAGE]);
    	dest.append(messageState.sender.getGroup().getRawName()).append(".log");
        LogDestination ld = getLogDestination(dest.toString());
        LogEntry le = new LogEntry (ld, cs.encode(message));
        addLogElement(le);
    }
    
    /**
     * 
     * @param type
     * @param message
     */
    public void addLogMessage (int type, String message) {
        LogDestination ld = getLogDestination(Server.LOGFILE[type]);
        LogEntry le = new LogEntry (ld, cs.encode(message));
        addLogElement(le);
    }

    /**
     * Add the constructed LogEntry to the LogWriter.instance's log Queue
     * @param le the LogEntry
     */
    private void addLogElement (LogEntry le) {
        int cntr = 0;
        boolean success = false;
        while (!success) {
            synchronized (this) {
                success=logQueue.put(le);
                this.notifyAll();
            }
            cntr++;
            if (cntr>5) {
                System.out.print("LQ-full: ");
                System.out.print(le.toString());
            }
        }
    }
    
    public void removeLogDestinations(String path){
        logDestinations.remove(path);
    }

	public void stopLogging () {
		stopped = true;
	}

	public void run() {
		LogEntry le=null;
        long lastMessage=0;
		while (!stopped) try {
            ObjectBuffer workingCopy;
			synchronized (this) {
                while (logQueue.isEmpty()) try {
                    this.wait (33);
                } catch (InterruptedException ie) { }
                workingCopy = logQueue;
                logQueue = new ObjectBuffer(1000);
                this.notifyAll();
			}
            while (!workingCopy.isEmpty()) {
                le = (LogEntry) workingCopy.get();
                if (writeToChannel(le))
                    workingCopy.pop();
            }
            workingCopy.clear();
		} catch (Exception e) {
            StringBuffer sb = new StringBuffer(this.toString());
            sb.append("run:");
            sb.append(e.getMessage());
            sb.append("(");
            sb.append(e.getCause());
            sb.append(") StackTraceElements: ");
            StackTraceElement[] st = e.getStackTrace();
            sb.append (st.length);
            sb.append ("\r\n");
            for (int i = 0; i < st.length; i++) {
                sb.append ("at ");
                sb.append (st[i].getClassName());
                sb.append (" ");
                sb.append (st[i].getMethodName());
                sb.append ("(");
                sb.append (st[i].getLineNumber());
                sb.append (")\r\n");
            }
			System.out.println (sb.toString());
            if (le!=null)
                System.out.println (le.toString());
            e.printStackTrace();
		} finally {
            try {
                Thread.sleep (33);
            } catch (InterruptedException ie) { }
        }
	}

	private boolean writeToChannel (LogEntry le) {
		try {
			int written = le.ld.getChannel().write (le.buf);
			if (!le.buf.hasRemaining()) 
			    return true;
		} catch (IOException ioe) {
            StringBuffer sb = new StringBuffer(this.toString());
            sb.append("run:");
            sb.append(ioe.getMessage());
            sb.append("(");
            sb.append(ioe.getCause());
            sb.append(") StackTraceElements: ");
            StackTraceElement[] st = ioe.getStackTrace();
            sb.append (st.length);
            sb.append ("\r\n");
            for (int i = 0; i < st.length; i++) {
                sb.append ("at ");
                sb.append (st[i].getClassName());
                sb.append (" ");
                sb.append (st[i].getMethodName());
                sb.append ("(");
                sb.append (st[i].getLineNumber());
                sb.append (")\r\n");
            }
            System.out.println (sb.toString());
            return true;
		} catch (Exception e) {
            StringBuffer sb = new StringBuffer(this.toString());
            sb.append("run:");
            sb.append(e.getMessage());
            sb.append("(");
            sb.append(e.getCause());
            sb.append(") StackTraceElements: ");
            StackTraceElement[] st = e.getStackTrace();
            sb.append (st.length);
            sb.append ("\r\n");
            for (int i = 0; i < st.length; i++) {
                sb.append ("at ");
                sb.append (st[i].getClassName());
                sb.append (" ");
                sb.append (st[i].getMethodName());
                sb.append ("(");
                sb.append (st[i].getLineNumber());
                sb.append (")\r\n");
            }
            System.out.println (sb.toString());
            return true;
		}
        return false;
	}

	private class LogEntry {
        final LogDestination ld;
		final ByteBuffer buf;
		
        LogEntry (LogDestination ld, ByteBuffer buf) {
            this.ld = ld;
            this.buf = buf;
        }
	}
}