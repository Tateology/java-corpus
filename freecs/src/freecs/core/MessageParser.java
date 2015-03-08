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
/**
 * the command set is inside here
 */
package freecs.core;

import freecs.*;
import freecs.layout.*;
import freecs.commands.AbstractCommand;
import freecs.commands.CommandSet;
import freecs.content.*;
import freecs.interfaces.*;

import java.nio.charset.Charset;
import java.nio.charset.UnmappableCharacterException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

/**
 * constructed with a sender (possible null) and a raw message, this
 * message-container is responsible for parsing the message. if the message
 * was parsed successfully, the apropriate flags get set and the parsing
 * thread decides what to do with this message.
 */
public class MessageParser implements IContainer {
	private MessageState	msgState;
	private ByteBuffer		bBuff = null;
	private HashMap<String, StringBuffer>			renderCache;
	private boolean			isHTTP11 = true;
	private CommandSet		cs;
	private RequestReader req;

	public MessageParser () {
		renderCache = new HashMap<String, StringBuffer> ();
		msgState = new MessageState(this);
		cs = CommandSet.getCommandSet ();
		clear ();
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
   public MessageParser (RequestReader r) {
      renderCache = new HashMap<String, StringBuffer> ();
      msgState = new MessageState(this);
      cs = CommandSet.getCommandSet ();
      clear ();
      req=r;
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

	/**
	 * clear the state of this messagparser
	 */
	protected void clear () {
		msgState.clear();
		renderCache.clear ();
		isHTTP11 = true;
	}

	/**
	 * set the http/1.1-capability for this message
	 * @param b if true HTTP1.1 is enabled, if false it will be disabled
	 */
   public void setHTTP11 (boolean b) {
      isHTTP11 = b;
   }

	public void setConnectionBuffer (ConnectionBuffer cb) {
		msgState.cb = cb;
	}

   /**
    * set the target to the given String
    * @param target the target
    */
   public void setParam (String target) {
       msgState.param = target;
   }

   /**
    * set the target to the given String
    * @param target the target
    */

   public void setReason (String target) {
       msgState.reason = target;
   }

   
   public void setTargetGroup (Group target) {
       msgState.targetGroup = target;
   }

   /**
    * set the source of this message to the given String
    * @param source the source
   public void setSource (String source) {
      msgState.source = source;
   }
    */

   /**
    * set the sender of this message
    * @param s the user which sent this message
    */
   public void setSender (User s) {
      msgState.sender = s;
   }
   
   public void setUsercontext (User s) {
	  msgState.usercontext = s;
   }

	/**
	 * set the message
	 * @param msg the raw message
	 */
   public void setRawMessage (String msg) {
      msgState.msg = msg.trim ();
   }

   public MessageState getMessageState() {
       return msgState;
   }
	/**
	 * return the sender of this message
	 * @return the sender of this message
	 */
   public User getSender () {
      return msgState.sender;
   }

	/**
	 * interface contentcontainer deffines the following
	 * @return the ByteBuffer containing the bytes to send
	 */
   public ByteBuffer getByteBuffer () {
      if (bBuff != null) 
          bBuff.rewind ();
      return (bBuff);
   }

   /**
    * returns always false for this, because the message-window doesn't get closed
    * except the user loggs out
    */
   public boolean closeSocket () {
      return false;
   }

	/**
	 * checks if there is actual some content to send
	 * @return true if there is content to send, false if not
	 */
   public boolean hasContent () {
      return (bBuff != null && bBuff.limit () > 0);
   }

	/**
	 * set the messagetemplate which should be used
	 * @param tplName the name of the message-template
	 */
   public void setMessageTemplate (String tplName) {
      msgState.msgTemplate = tplName;
   }

	/**
	 * get the personalized message for this user
	 * @param u the user to personalize the message for
	 * @return the IContainer containing the personalized message
	 */
   public boolean addPersonalizedMessage (User u, ConnectionBuffer cb, Vector<String> blockedServerPlugin) {
      StringBuffer result = new StringBuffer ();
      TemplateSet ts = u.getTemplateSet ();
      StringBuffer tsb = new StringBuffer (ts.getName ()).append ("/").append (msgState.msgTemplate).append ("/").append (isHTTP11);
      String rcKey = tsb.toString ();
      // check if the wanted message is contained within the render-cache
      if (!Server.srv.USE_MESSAGE_RENDER_CACHE 
              || !msgState.useRenderCache 
              || !renderCache.containsKey (rcKey)) {
    	  boolean allowedParam = true;
          String msgTpl = ts.getMessageTemplate (msgState.msgTemplate);

    	  if (msgState.msgTemplate.startsWith("funcommand.")){
    			if (msgState.param.toLowerCase().indexOf("http://")>-1 || msgState.param.toLowerCase().indexOf("www.")>-1)
    				allowedParam = false;
    			if (msgTpl.toLowerCase().indexOf("<script")>-1 )
    				msgTpl = "<b>not allowed Index</b>";
    	  }
    	  
          if (!allowedParam)
        	  msgTpl = "<b>not allowed param</b>";
         
          if (msgTpl == null || msgTpl.length () < 1) {
              tsb = new StringBuffer ("Message-Template ").append (msgState.msgTemplate).append (" was not found ");
              Server.log ("[Templateset "+ts.getName()+"]", tsb.toString (), Server.MSG_ERROR, Server.LVL_MINOR);
              if (Server.srv.DEBUG_TEMPLATESET){
                  msgTpl = tsb.toString ();
                  result.append(msgTpl);
              } else return false;
          }
          String showTime = ts.getMessageTemplate ("status.showtime");
          if (showTime != null && (showTime.equals ("true") || showTime.equals("1"))) {
              String tf = ts.getMessageTemplate ("status.showtime.timeformat");
         	  if (tf != null) result.append (MessageRenderer.renderTemplate (msgState, tf, false, blockedServerPlugin));
         	  else {
         	     result.append ("[");
         	     result.append (Server.srv.getFormatedTime ("HH:mm"));
         	     result.append ("] ");
         	  }
         }
         result.append (MessageRenderer.renderTemplate (msgState, msgTpl, blockedServerPlugin));
         renderCache.put (rcKey, result);
      } else {
         result.append (renderCache.get(rcKey));
      }
      if (result.length() < 1) return false;
      CharBuffer cbuf = CharBuffer.wrap (result);
      try {
      	if (cbuf.length() < 2) {
      		if (msgState.msgTemplate.equals ("message.q") 
                || msgState.msgTemplate.equals ("message.server.shutdown")
                || msgState.msgTemplate.equals ("message.kh.personal")) { 
                    msgState.sender.removeNow();
                    CentralSelector.dropKey(u.getKey());
            }
            return false;
      	}
        if ((msgState.msgTemplate.equals ("message.q") 
                || msgState.msgTemplate.equals ("message.server.shutdown")
                || msgState.msgTemplate.equals ("message.kh.personal"))) {
            cb.addToWrite(Charset.forName (Server.srv.DEFAULT_CHARSET).newEncoder ().encode (cbuf));
            cb.addToWrite(Responder.CLOSE_CONNECTION);
            return true;
        }
        cb.addToWrite(Charset.forName (Server.srv.DEFAULT_CHARSET).newEncoder ().encode (cbuf));
        return true;
      } catch (UnmappableCharacterException uce) {
         Server.debug (this, "getPersonalizedMessage: ", uce, Server.MSG_ERROR, Server.LVL_MINOR);
         byte[] b = result.toString ().getBytes ();
         ByteBuffer bb = ByteBuffer.wrap (b);
         if ((msgState.msgTemplate.equals ("message.q") 
                 || msgState.msgTemplate.equals ("message.server.shutdown")
                 || msgState.msgTemplate.equals ("message.kh.personal"))) {
             cb.addToWrite(bb);
             cb.addToWrite(Responder.CLOSE_CONNECTION);
             return true;
         }
         cb.addToWrite(bb);
         return true;
      } catch (Exception e) {
         Server.debug (this, "getPersonalizedMessage: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
         return false;
      }
   }

   public boolean prepareForSending () {
      if (this.bBuff != null) return true;
      return false;
   }

/*   public void finalize () {
      Server.log ("MessageParser FINALIZED*******************************", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   } */

   /**
    *--------------------- MESSAGE-PARSING -------------------
    * this is where the parsing of the message happens.
    */
	public void parseAndSendMessage () {
		if (req!=null) req.currPosition = RequestReader.PARSE_MSG;
		if (msgState.msg == null 
			|| msgState.msg.length () < 1
			|| msgState.sender == null
			|| !msgState.cb.isValid()) {
			clear ();
			return;
		}
        // check message lenght
		Group sg = msgState.sender.getGroup ();
		if (Server.srv.MESSAGE_FLOOD_LENGHT > 0 
				&& ((sg.hasState(IGroupState.ENTRANCE) && sg.hasState(IGroupState.CAN_SET_PUNISHABLE)) 
				|| sg.hasState(IGroupState.CAN_SET_PUNISHABLE))) {
		    if (msgState.msg.length() >= Server.srv.MESSAGE_FLOOD_LENGHT){
			    if (msgState.sender.hasMessageFloodLenght()){
				    clear();
				    return;
			    }
            	Server.srv.storeUser(IActionStates.ISPUNISHABLE, msgState.sender, msgState.reason, 60000, "Messagelenght");
			    msgState.sender.setMessageFloodLenght(true);
		    } else {
			    if (msgState.sender.hasMessageFloodLenght())
				    msgState.sender.setMessageFloodLenght(false);
		    }
		}
		// check if user was in away-state
		if (msgState.sender.isAway ()) {
			msgState.msgTemplate="message.away.off";
			msgState.message=msgState.sender.getAwayMessage();
			
			if (sg != null)
				sg.sendMessage (this);
			else
				msgState.sender.sendMessage (this);
			msgState.sender.setAway (false);
			Vector<Object> found = new Vector<Object>();
			Vector<Object> foundName = new Vector<Object>();
		    Vector<Object> added = msgState.sender.whisper();
		    for (Enumeration<Object> e = added.elements(); e.hasMoreElements(); ) {
		    	Object add = (Object)e.nextElement();
		    	String nick = null;
		    	if (add instanceof User){
		    	    User addu = (User) add;
		    	    nick = addu.getName();
		    	}
		    	if (nick != null){
		    	    if (!foundName.contains(nick)){
		    	        if (!found.contains(add)){
		                    found.add(add);
		                    foundName.add(nick);
		                }
		    	    }
		    	}	       		       
		    }
			if (found.size() >0) {
				if (found.size() == 1) {
					Object o= found.get(0);
	                if (o instanceof User) {
	                    msgState.usercontext = (User) o;
	                    msgState.param = "";
	                } else {
	                   msgState.usercontext = null;
	                   msgState.param = (String) o;
	                }
			        msgState.msgTemplate="message.away.whisper.singular";
				}
				if (found.size() > 1)
			        msgState.msgTemplate="message.away.whisper.plural";
			    Object size = msgState.sender.whisper().size();
			    
			    msgState.param = size.toString();
			    msgState.usrList = found.toArray();
			    msgState.sender.sendMessage (msgState.mp);
                Integer s = (Integer) size;
                msgState.sender.sendMessageHistory(s);
			}
//			 FriendNotification 
			Vector<Object> foundFriends = new Vector<Object>();
		    Vector<Object> storeFriendNotification = msgState.sender.getStoreFriendNotification();
		    
		    for (Enumeration<Object> e = storeFriendNotification.elements(); e.hasMoreElements(); ) {
		    	Object add = (Object)e.nextElement();
		    	if (add instanceof User) {
		    		User a= (User) add;
		    		if (UserManager.mgr.getUserByName(a.getName()) != null && a.getGroup() != null) {
			            if (!foundFriends.contains(add)) {
			        	    foundFriends.add(add);
			            }
		    		}		    	
		    	}
		    }
		    
		    if (foundFriends.size() >0) {
				if (foundFriends.size() == 1) {
					Object o= foundFriends.get(0);
	                if (o instanceof User) {
	                    msgState.usercontext = (User) o;
	                    msgState.param = "";
	                } else {
	                   msgState.usercontext = null;
	                   msgState.param = (String) o;
	                }
			        msgState.msgTemplate="message.away.friendnotification.singular";
				}
				if (foundFriends.size() > 1)
			        msgState.msgTemplate="message.away.friendnotification.plural";
			
			    msgState.usrList = foundFriends.toArray();
			    msgState.sender.sendMessage (msgState.mp);
			}
      	}

		if (!msgState.msg.startsWith ("/") || msgState.sender == null) {
			if (msgState.sender != null 
			        && msgState.sender.isPunished()) {
		        msgState.msgTemplate = "error.user.punished";
		        msgState.sender.sendMessage(msgState.mp);
				clear ();
				return;
			}
		
			int timelocksec = sg.getTimelockSec();
			if (msgState.sender.getGroup().hasState(IGroupState.MODERATED) 
                    && !msgState.sender.hasRight(IUserStates.IS_MODERATOR) 
                    && !msgState.sender.hasRight(IUserStates.IS_GUEST)) {
                // check the timelock if group is moderated and sender isn't guest nore moderator
				if (msgState.sender.lastSentMessage > System.currentTimeMillis() - 1 * timelocksec  * 1000) {
					msgState.msgTemplate = "error.moderated.timelock";
					msgState.param = "" + ((msgState.sender.lastSentMessage - System.currentTimeMillis() + (1 * timelocksec  * 1000)) / 1000);
					msgState.message = msgState.msg;
					msgState.sender.sendMessage(this);
					clear();
					return;
				} else {
					msgState.sender.lastSentMessage = System.currentTimeMillis();
					msgState.msgTemplate = "message.send.moderated";
					msgState.message = msgState.msg;
					msgState.sender.getGroup().sendMessage(this);
					msgState.msgTemplate = "message.send.moderated.personal";
					msgState.message = msgState.msg;
					msgState.sender.sendMessage(this);
					AbstractCommand.messageLog(msgState, null, null);
					clear();
					return;
				}
			} else {
				msgState.sender.lastSentMessage = System.currentTimeMillis();
				msgState.msgTemplate = "message.send";
				msgState.message = msgState.msg;
				msgState.sender.getGroup().sendMessage(this);
				AbstractCommand.messageLog(msgState, null, null);
				clear();
				return;
			}
				
		}

		if (req!=null) {
            req.currPosition = RequestReader.EVALUATE_COMMAND;
            req.currCommand = msgState.msg;
        }
		// retrieve the command-token
		int pos = msgState.msg.indexOf (" ");
		String cmd, param;
		if (pos > -1) {
			cmd = msgState.msg.substring(0,pos).toLowerCase();
			param = msgState.msg.substring (pos).trim ();
		} else {
			cmd = msgState.msg;
			param = "";
		}
        // "/.... text" is used for whispering to user ...
        if (param.length() > 0) {
            if (cmd.equals("/time")) {
                cmd = "/m";
                param = "time " + param;
            } else if (cmd.equals("/raq")) {
                cmd = "/m"  ;
                param = "raq " + param;
            } else if (cmd.equals("/mycol")) {
                cmd = "/m";
                param = "mycol " + param;
            } else if (cmd.equals("/fl")) {
                cmd = "/m";
                param = "fl " + param;
            } else if (cmd.equals("/a")) {
                cmd = "/m";
                param = "a " + param;
            } else if (cmd.equals("/l")) {
                cmd = "/m";
                param = "l " + param;
            } else if (cmd.equals("/ul")) {
                cmd = "/m";
                param = "ul " + param;
            }
        }
		byte result = cs.evaluate(cmd, msgState, param); 
		if (result == CommandSet.UNKNOWN_COMMAND) {
			cs.evaluate("/m", msgState, msgState.msg.substring (1));
		} else if (result==CommandSet.INTERRUPTED) {
			msgState.cb.logError("ConnectionBuffer was invalidated");
		}
		clear ();
	}
    
    private volatile String strgVal;
    public String toString () {
        if (strgVal==null) {
            StringBuffer sb = new StringBuffer("[MessageParser ");
            if (req != null)
                sb.append (this.req.toString());
            sb.append ("]");
        }
        return strgVal;
        
    }
    /**
     * @param msg
     */
    public void setMessage(String msg) {
        msgState.message = msg;
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}