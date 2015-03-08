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

import freecs.*;
import freecs.interfaces.*;

import java.nio.*;
import java.nio.channels.*;
import java.util.*;


public class RequestReader extends Thread {
	public static final short WAITING = 0,
							EVAL_GET_MESSAGES_APND2WRITE = 1,
							EVAL_GET_MESSAGES_SND_MSGS=2,
							EVAL_GET_MESSAGES=3,
							EVAL_GET_STATE=4,
							EVAL_GET=5,
							EVAL_POST=6,
							EVAL_POST_LOGIN=7,
							EVAL_PREP4SEND=8,
							EVAL_SEND=9,
							EVAL_SENDFINAL=10,
							EVALUATE_COMMAND=11,
							EVALUATING=12,
							PARSE_MSG=13,
							READING=14,
							EVAL_POST_LOGIN_RESULT=15,
							TRYLOGIN=16,
							TRYLOGIN_AUTHENTICATE=17,
							TRYLOGIN_CHECK_FRIENDS=18,
							TRYLOGIN_CHECK4PRESENCE=19,
							TRYLOGIN_CORRECT_PERMISSION=20,
							TRYLOGIN_SCHEDULE_FRIENDMSGS=21,
							TRYLOGIN_SCHEDULE_GODMSG=22,
							TRYLOGIN_SCHEDULE_VIPMSG=23,
							TRYLOGIN_SEND_LOGINMSG=24,
							TRYLOGIN_SET_GROUP=25,
							TRYLOGIN_SET_PERMISSION=26;
	
	
	private static Vector<RequestReader>		reqReaders = new Vector<RequestReader> ();
	private static short		readerID = 0;
	
	private long 				shutdowntime;
	private short				ID;
	private ByteBuffer			buf;
	private RequestEvaluator	evaluator;
	public  RequestQueue		reqQueue;
	public  boolean				isFixed, working;
	public long workstart;
    
    public volatile IRequest currentRequest=null;
	
	public short currPosition;
    public String currCommand;
	
	private RequestReader(short id) {
		this.ID = id;
		reqQueue = new RequestQueue (this);
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
	
	public static boolean processRequest (SelectionKey sk) {
        if (!CentralSelector.isSkValid(sk)) {
            Server.log("static RequestReader", "processRequest: current request has invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
            return true;
        }
		// this is the work-to-thread-algorithm
		// threads at the beginning have to get more requests, than threads at the
		// end of the thread-list, to enable the threads at the end of the thread-list
		// to quit working on lower usage. This is done by considering the queue usage and
		// the list-index.
		// factor = RequestQueue.size() + idx*(READER_MAX_QUEUE / MAX_READERS)
		float min = Server.srv.READER_MAX_QUEUE;
		float incr = ((float) Server.srv.READER_MAX_QUEUE) / Server.srv.MAX_READERS;
		int rrSizeBorder = (int) (reqReaders.size()/1.5);
		RequestReader minReader = null;
		for (int i = 0; i < reqReaders.size(); i++) {
			RequestReader r = (RequestReader) reqReaders.elementAt(i);
			int rqSize = r.reqQueue.size();
			if (i < rrSizeBorder && rqSize==0) {
				minReader=r;
				break;
			}
			float factor = ((float) rqSize) + i * incr;
			if (factor < min && !r.isSuspending()) {
				min = factor;
				minReader=r;
			}
		}
		if (minReader == null) {
			minReader = RequestReader.startRequestReader(false);
		}
		if (minReader == null) {
			// if no minReader may be started and every reader's factor is too high
			// we will have to loop over all threads and get the lowest requestqueue-size
			// to deliver this request
			int lowestQueue = Server.srv.READER_MAX_QUEUE+1;
			for (int i = 0; i< reqReaders.size(); i++) {
				RequestReader r = (RequestReader) reqReaders.elementAt(i);
                int factor = r.reqQueue.size();
                if (r.working)
                    factor++;
				if (factor < lowestQueue)
					minReader = r;
			}
			if (minReader==null)
				return false;
		}
		minReader.reqQueue.addKey(sk);
		return true;
	}
	
	private void restart() {
		Server.log (this, "trying to restart dead thread", Server.MSG_STATE, Server.LVL_MAJOR);
		this.start();
	}

	public static boolean[] getAliveState () {
		boolean[] res = new boolean[reqReaders.size()];
		for (int i = 0; i<res.length; i++) {
			RequestReader r = (RequestReader) reqReaders.elementAt(i);
			res[i] = r.isAlive();
			if (!res[i])
				r.restart();
		}
		return res;
	}

	public static long[][] getWorkingSince () {
		long[][] res = new long[reqReaders.size()][2];
		for (int i = 0; i<res.length; i++) {
			RequestReader r = (RequestReader) reqReaders.elementAt(i);
			if (r.working)
				res[i][0] = r.workstart;
			else
				res[i][0] = 0;
			res[i][1]=r.currPosition;
		}
		return res;
	}
	
    public static String getCurrCommant (int idx) {
        return ((RequestReader) reqReaders.elementAt(idx)).currCommand;
    }
    
	public static double[] getOveralUsage () {
		double[] res = new double[reqReaders.size()];
		for (int i = 0; i < res.length; i++) {
			RequestReader r = (RequestReader) reqReaders.elementAt(i);
			res[i] = r.reqQueue.getUsage();
		}
		return res;
	}

	/**
	 * starts a new requestreader-thread and possibly makes it as
	 * a fixed thread. A fixed thread will only suspend if the server
	 * shuts down. If the maximum number of configured RequestReader-threads
	 * is reached, null will be returned.
	 * @param fixed markes it as fixed thread if true
	 * @return the RequestReader
	 */
    public static RequestReader startRequestReader (boolean fixed) {
        if (activeReaders () >= Server.srv.MAX_READERS)
            return null;
        short cid = readerID++;
        RequestReader reqReader = new RequestReader (cid);
        reqReader.isFixed = fixed;
        if (readerID == Short.MAX_VALUE) 
            readerID = Short.MIN_VALUE;
        reqReaders.add (reqReader);
        if (fixed) {
            reqReader.setName ("FIXED-RequestReader " + cid);
            // reqReader.setPriority (Thread.MAX_PRIORITY-1);
            StringBuffer tsb = new StringBuffer ("Thread START: (FIXED THREAD, ");
            tsb.append (reqReaders.size ());
            tsb.append (" threads running)");
            Server.log ("static RequestReader", tsb.toString (), Server.MSG_STATE, Server.LVL_MAJOR);
        } else {
            reqReader.setName ("RequestReader " + cid);
            // reqReader.setPriority (Thread.MAX_PRIORITY-1);
            StringBuffer tsb = new StringBuffer ("Thread START: (").append (reqReaders.size ()).append (" threads running)");
            Server.log ("static RequestReader", tsb.toString (), Server.MSG_STATE, Server.LVL_MINOR);
        }
        reqReader.start ();
        return reqReader;
    }

	/**
	 * removes a requestreader from the requestreader-list
	 * @param reqReader the requestreader to remove
	 */
   public static void removeRequestReader (RequestReader reqReader) {
      reqReaders.remove (reqReader);
      StringBuffer tsb= new StringBuffer ("Thread STOP: (").append (reqReaders.size ()).append (" threads running)");
      Server.log ("static RequestReader", tsb.toString (), Server.MSG_STATE, Server.LVL_MINOR);
   }

	/**
	 * returns the number of requestreaders in the requestreader-list
	 * @return number of requestreaders
	 */
   public static int activeReaders () {
      return reqReaders.size ();
   }

	/**
	 * the work of a requestreader is done her
	 * .) check if the request-queue has something to read
	 * .) if there is nothing and the time between the last read
	 *    and now is higher than Server.READER_MAX_IDLETIME and the
	 *    thread is not marked as fixed, the RequestReader will suspend
	 * .) if there is something to read it will be pricessed
	 * .) if the processed request is complete, it will be evaluated
	 */
	public void run() {
		buf = ByteBuffer.allocate(Server.srv.READBUFFER_SIZE);
		evaluator = new RequestEvaluator (this);
		long lastReadTime = System.currentTimeMillis ();
		shutdowntime = 0;
        long lastMessage = 0;
		boolean suspend = false;
		while (!suspend) try {
            if (Server.DEBUG || lastMessage + 5000 > System.currentTimeMillis()) {
                Server.log (this, "loopstart", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                lastMessage = System.currentTimeMillis();
            }
      		currPosition=WAITING;
			if (!Server.srv.isRunning ()) {
				if (shutdowntime == 0)
					shutdowntime = System.currentTimeMillis () + 150000;
				if (shutdowntime < System.currentTimeMillis ()) {
					suspend = true;
					break;
				}
			}
         	long diff = Server.srv.READER_MAX_IDLETIME;
         	if (!this.isFixed) {
	            // if this reader was idle too long, make this thread disapear
            	diff = System.currentTimeMillis () - lastReadTime;
            	if (diff > Server.srv.READER_MAX_IDLETIME 
                    && activeReaders () > 1
                    && reqQueue.size() < 1)
               		break;
            	if (diff > Server.srv.READER_MAX_IDLETIME) 
            		diff = Server.srv.READER_MAX_IDLETIME;
         	}
            SelectionKey sk;
            if (Server.srv.USE_CENTRAL_REQUESTQUEUE) {
                synchronized (CentralSelector.cSel.reqQueue) {
                    if (CentralSelector.cSel.reqQueue.size() < 1) try {
                        CentralSelector.cSel.reqQueue.wait(Server.srv.READER_MAX_IDLETIME - diff);
                    } catch (InterruptedException ie) { }
                    sk = (SelectionKey) CentralSelector.cSel.reqQueue.pop();
                    CentralSelector.cSel.reqQueue.notify();
                }
            } else {
                sk = reqQueue.popKey(diff); // reqQueue.getKey (diff);
            }
         	if (sk == null) {
	            try {
    	           Thread.sleep (33);
            	} catch (InterruptedException ie) { }
            	continue;
         	}
         	currPosition=READING;
         	working = true;
         	workstart = lastReadTime = System.currentTimeMillis ();
            long start = System.currentTimeMillis();
            StringBuffer sb = new StringBuffer();
            try {
                ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
//                synchronized (cb) {
/*                    if (Server.srv.CENTRALSELECTOR_PARSES_REQUEST) { */
// FIXME: evaluate() and everything below should check for interrup-requests by 
// the RequestMonitor and throw an interrupted-exception when it is encountered
                        evaluate(sk, cb);
                        sb.append ("evaluate: took ");
/*                    } else { 
                        read(sk, cb);
                        sb.append ("read: ended. took me ");
                    // } */
//                }
            } catch (Exception e) {
                Server.debug (this, "catched Exception while reading/evaluating", e, Server.MSG_ERROR, Server.LVL_MAJOR);
                try {
                    Thread.sleep (33);
                } catch (InterruptedException ie) { }
                continue;
            } finally {
                RequestMonitor.instance.removeMonitor(this);
                working = false;
            }
            long proctime = System.currentTimeMillis () - start;
            if (Server.checkLogLvl (Server.MSG_STATE, Server.LVL_VERY_VERBOSE)) {
                sb.append (proctime);
                sb.append (" millis ");
                if (currentRequest == null) {
                    sb.append ("reading");
                } else {
                    sb.append ("reading and processing ");
                    sb.append (currentRequest.toString());
                }
                Server.log (this, sb.toString(), Server.MSG_STATE, Server.LVL_VERBOSE);
            }
      	} catch (Exception e) {
         	Server.debug (this, "(outer loop): ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
      	}
      	removeRequestReader (this);
   	}
   	
    private void evaluate(SelectionKey sk, ConnectionBuffer cb) {
        if (cb == null) {
            Server.log(this, "ConnectionBuffer was null for Selectionkey", Server.MSG_ERROR, Server.LVL_MAJOR);
            return;
        }
        if (!cb.isValid()) {
            CentralSelector.dropKey(sk);
            return;
        }
        if (cb.currentRequest != null) {
            currentRequest = cb.currentRequest;
            cb.currentRequest = null;
            evaluator.evaluate (currentRequest);
        }
    }

    public boolean isSuspending () {
		return (shutdowntime != 0 && !isFixed);
	}
    public int hashCode () { return (int) ID; }
    public boolean equals (RequestReader r) { return r.getID () == ID; }
    public short getID () { return ID; }
   
    private volatile String strgVal=null;
    public String toString () { 
        if (strgVal == null) {
            StringBuffer sb = new StringBuffer("[RequestReader ");
            if (ID < 10) {
                sb.append ("   ");
            } else if (ID < 10) {
                sb.append ("   ");
            } else if (ID < 100) {
                sb.append ("  ");
            } else if (ID < 1000) {
                sb.append (" ");
            }
            sb.append (ID);
            sb.append ("]");
            strgVal = sb.toString();
        }
        return (strgVal);  
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}