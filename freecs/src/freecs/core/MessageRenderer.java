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
 * Created on 01.10.2003
 */

package freecs.core;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.StringTokenizer;
import java.util.Vector;

import freecs.Server;
import freecs.commands.CommandSet;
import freecs.content.MessageState;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IUserStates;
import freecs.layout.TemplateManager;
import freecs.layout.TemplateSet;

import freecs.util.EntityDecoder;
import freecs.util.HashUtils;

/**
 * @author Manfred Andres
 *
 * freecs.core
 */
public class MessageRenderer {
	/**
	 * renders the given template
	 * @param msgTpl the raw message-template
	 * @return the rendered template as String with all placeholders replaced by their coresponding value
	 */
   public static String renderTemplate (MessageState msgState, String msgTpl, Vector<String> blockedServerPlugin) {
	  return renderTemplate (msgState, msgTpl, msgTpl.equals ("list.users") ? false : true, blockedServerPlugin);
   }
   public static String renderTemplate (MessageState msgState, String msgTpl, boolean addBreak, Vector<String> blockedServerPlugin) {
	  if (msgTpl == null) 
          return null;
	  StringBuffer sb = new StringBuffer ();
	  for (StringTokenizer st = new StringTokenizer (msgTpl, "§"); st.hasMoreTokens (); ) {
		 String token = st.nextToken ();
		 // String result = evalAlgorithm (token);
		 sb.append (evalVariable (msgState, token, blockedServerPlugin));
	  }
      TemplateSet ts = msgState.sender == null 
      ? TemplateManager.mgr.getTemplateSet()
      : msgState.sender.getTemplateSet();
      String addB = ts.getMessageTemplate("constant.addBreak");   
      if (addB == null)
          addB = "<br>";
	  if (addBreak) sb.append(addB).append ("\r\n");
	  return (sb.toString ());
   }

   /**
	* evaluate the given variable
	* FIXME: this has to go more elegant...
	* @param token the token to evaluate
	* @return the evaluated result || the token if nothing was to evaluate
	*/
	private static String evalVariable (MessageState msgState, String token, Vector<String> blockedServerPlugins) {
        String val = null;
        String tok = token.toLowerCase();
        boolean encode = false, usePlugins = Server.srv.USE_PLUGINS; 	

        if (tok.startsWith ("sender.") || tok.startsWith ("sender ") || tok.equals("sender")
            || tok.startsWith ("user.") || tok.startsWith ("user ") || tok.equals ("user")
            || tok.startsWith ("friend.") || tok.startsWith ("friend ") || tok.equals ("friend")) { // TOKENS WITH USER-CONTEXT
            User cu = tok.startsWith("sender") ? msgState.sender : msgState.usercontext;
            int pos = tok.indexOf(".");
            if (cu != null && pos == -1) { // TOKEN for the username
                val = renderUserName (msgState, cu);
            } else if (cu != null) {
                tok = tok.substring (pos+1);
                if (tok.startsWith ("name")) {
                    val = cu.getName();
                } else if (tok.equals ("id")) {
                    val = cu.getID();
                } else if (tok.startsWith("awaymessage")) {
                    if (!cu.isAway())  // not away, no awaymessage
                        return "";
                    StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(cu.getAwayMessage()));
                    StringBuffer convert = new StringBuffer(sb_val); 
                    if (usePlugins){
                        PluginRenderer rp = new PluginRenderer();
                        convert = new StringBuffer(rp.checkAwamessagePlugin(convert, cu, msgState,cu.getBlockedServerPlugins()));
                        rp=null;
                    }
                    if (!sb_val.equals(convert)) {
                        encode =false;
                     	val = convert.toString();
                    } else {
                    	 val = cu.getAwayMessage();
                         encode=true;  
                    }                               
                   
                    if (val == null) {
                        val = ""; 
                    }
                } else if (tok.startsWith("isaway")) {
                    val = cu.isAway() ? "away" : "";
                } else if (tok.startsWith("title")) {
                    val = renderTitle(msgState, cu);
                } else if (tok.startsWith ("chattime") && !cu.isUnregistered) {
                    val = renderChattime (cu);
                } else if (tok.startsWith ("questioncounter")) {
                    val = String.valueOf(cu.getQuestionCounter());
                } else if (tok.startsWith ("linkedname")) {
                    User u = msgState.usercontext;
                    msgState.usercontext = cu;
                    TemplateSet ts = msgState.sender == null 
                        ? TemplateManager.mgr.getTemplateSet()
                        : msgState.sender.getTemplateSet();
                    val = renderTemplate(msgState, ts.getMessageTemplate("constant.linkedName"), false, blockedServerPlugins);
                    msgState.usercontext = u;
                } else if (tok.startsWith ("color")) {
                    val = cu.getColCode();
                    encode=true;
                    TemplateSet ts = msgState.sender == null 
                        ? TemplateManager.mgr.getTemplateSet()
                        : msgState.sender.getTemplateSet();
                    if (val == null)
                        val = ts.getMessageTemplate("constant.defaultColor");
                } else if (tok.startsWith ("fadecolor")) {
                    val = cu.getFadeColCode();
                    encode=true;
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("reg.text")) {
                    if (!cu.isUnregistered) {
                    	TemplateSet ts = msgState.sender == null 
                    	? TemplateManager.mgr.getTemplateSet()
    	                : msgState.sender.getTemplateSet();
                        val = ts.getMessageTemplate("constant.reg.text");	
                    }
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("group.theme")) {
                    if (tok.equals ("group.themecreator") ) {
                        val = renderGroupthemeCreator(msgState, cu.getGroup());
                    } else if (tok.startsWith ("group.theme")) {
                        val = cu.getGroup().getTheme();
                        if (val == null)
                            val = "";
                        
                        StringBuffer val_ok = new StringBuffer(val);                          
                        StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                        StringBuffer convert = new StringBuffer(sb_val); 
                                               
                        if (usePlugins){
                            PluginRenderer rp = new PluginRenderer();
                            convert = new StringBuffer(rp.ceckGroupTheme(convert, msgState, cu.getBlockedServerPlugins()));
                            rp=null;
                        }

                        if (!sb_val.equals(convert)) {
                            encode = false;
                            val = convert.toString();
                        } else {
                            val = val_ok.toString();
                     	    encode=tok.equals ("group.theme");
                        }
                    }
                } else if (tok.startsWith ("group")
                           || tok.startsWith ("group.name")) {
                    if (cu.getGroup()!=null){
                        val = cu.getGroup().getName();
                        if (msgState.sourceGroup == null)
                            msgState.sourceGroup = cu.getGroup();
                    } else  val = "";
                    StringBuffer val_ok = new StringBuffer(val);                  
                    if (tok.startsWith ("group.name") || tok.startsWith ("group")) {
                        StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                        StringBuffer convert = new StringBuffer(sb_val); 
                        if (Server.srv.USE_PLUGINS){
                            PluginRenderer rp = new PluginRenderer();
                            convert = new StringBuffer(rp.checkGroupName(convert, msgState, cu.getBlockedServerPlugins()));
                            rp=null;
                        }
                        if (!sb_val.equals(convert)) {
                            encode = false;
                            val = convert.toString();
                        } else {
                            val = val_ok.toString();
                            if (tok.startsWith ("group")){
                     	        encode=tok.startsWith("group");
                            } else encode=tok.startsWith("group.name");
                        }
                    }
                } else if (tok.startsWith ("peername")) {
                    if (cu.conn.peerAddress != null) {
                        int minRight = IUserStates.ROLE_VIP;
                        if (msgState != null && msgState.sender != null){
                            if (!hasMinRight(minRight, msgState, "ip.minright"))
                               val= "";
                            else val = cu.conn.peerAddress.getCanonicalHostName();
                        } else val = cu.conn.peerAddress.getCanonicalHostName();
                    }
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("peerip")) {
                    int minRight = IUserStates.ROLE_VIP;
                    if (msgState != null && msgState.sender != null){
                        if (!hasMinRight(minRight, msgState, "ip.minright"))
                            val= "";
                        else  val = cu.conn.peerIp;
                    } else val = cu.conn.peerIp;
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("cookie")) {
                	// only the sender himself or GOD can read the MD5 Hash of the cookie String
                	if (cu != null && msgState.sender != null && (cu.getName().equals(msgState.sender.getName()) || msgState.sender.hasRight(IUserStates.ROLE_GOD)) ) {
                		try {
							val = HashUtils.encodeMD5(cu.getCookie());
						} catch (Exception e) {
							val = e.getMessage();
						}
                	} else {
                		val = "hidden";
                	}
                	if (val == null)
                        val = "";
                } else if (tok.startsWith ("clientname")) {
                    if (cu.conn.clientAddress != null) {
                        int minRight = IUserStates.ROLE_VIP;
                        if (msgState != null && msgState.sender != null){
                            if (!hasMinRight(minRight, msgState, "ip.minright"))
                               val= "";
                            else val = cu.conn.clientAddress.getCanonicalHostName();
                        } else  val = cu.conn.clientAddress.getCanonicalHostName();                      
                    }
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("clientip")) {                  
                    int minRight = IUserStates.ROLE_VIP;
                    if (msgState != null && msgState.sender != null){
                        if (!hasMinRight(minRight, msgState, "ip.minright"))
                            val= "";
                        else  val = cu.conn.clientIp;
                    } else val = cu.conn.clientIp;
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("templateset")) {
                    val = cu.getTemplateSet().getName();
                } else if (tok.startsWith ("forwardchain")) {
                    if (cu.conn.fwChain!=null
                        && cu.conn.fwChain.length>0) {
                        StringBuffer sb = new StringBuffer ();
                        for (int j = 0; j<cu.conn.fwChain.length; j++) {
                            sb.append (cu.conn.fwChain[j]);
                            if (j < cu.conn.fwChain.length) 
                                sb.append (", ");
                        }
                        val = sb.toString();
                    } else {
                        val = "";
                    }
                } else {
                    int spc = tok.indexOf (" ");
                    Object o = null;
                    if (spc > -1) {
                        o = cu.getProperty (tok.substring(0,spc));
                    } else {
                        o = cu.getProperty (tok);
                    }
                    if (o == null) { 
                        val = "";
                    } else {
                        val = o.toString();
                        encode=true;
                    }
                }
            } else {
                val = "";
            }
        } else if (tok.startsWith("group")
                   || tok.startsWith ("sourcegroup")
                   || tok.startsWith ("targetgroup")) { // TOKENS WITH GROUP-CONTEXT
            Group cg = tok.startsWith ("sourcegroup") ? msgState.sourceGroup : msgState.targetGroup;
            int pos = tok.indexOf(".");
            if (pos == -1) {
                if (cg!=null) {
                    val = cg.getName();
                } else  {
                    val = null;
                }
                StringBuffer val_ok = new StringBuffer(val);                  
                if (tok.startsWith("group")
                        || tok.startsWith ("sourcegroup")
                        || tok.startsWith ("targetgroup")) { 
                    StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                    StringBuffer convert = new StringBuffer(sb_val); 
                    Group temp_grp =null;
                    if (tok.startsWith ("sourcegroup")){
                        temp_grp=msgState.targetGroup;
                        msgState.targetGroup=null;
                    }
                    if (tok.startsWith ("targetgroup")){
                        temp_grp=msgState.sourceGroup;
                        msgState.sourceGroup=null;                        
                    }
                    if (usePlugins){
                        PluginRenderer rp = new PluginRenderer();
                        convert = new StringBuffer(rp.checkTargetGroup(convert, msgState, msgState.sender.getBlockedServerPlugins()));
                        rp=null;
                    }
                    if (!sb_val.equals(convert)) {
                        encode = false;
                        val = convert.toString();
                        if (tok.startsWith ("sourcegroup")){
                            msgState.targetGroup = temp_grp;
                            temp_grp=null;
                        }
                        if (tok.startsWith ("targetgroup")){
                            msgState.sourceGroup=temp_grp;
                            temp_grp = null;
                        }
                    } else {
                        val = val_ok.toString();
                        if (tok.startsWith ("group")){
                 	        encode=tok.startsWith("group");
                        } else if (tok.startsWith ("sourcegroup")){
                 	        encode=tok.startsWith("sourcegroup");
                 	        msgState.targetGroup = temp_grp;
                 	        temp_grp=null;
                        } else {
                            encode=tok.startsWith("targetgroup");
                            msgState.sourceGroup=temp_grp;
                            temp_grp = null;
                        }
                    }
                }
            } else if (cg != null) {
                tok = tok.substring(pos+1);
                if (tok.startsWith ("theme")) {              	
                    if (tok.startsWith ("themecreator") ) {
                        val = renderGroupthemeCreator(msgState, cg);                     
                    } else if (tok.startsWith("theme")) {
                            val = cg.getTheme();
                            if (val == null)
                                val = "";
                            StringBuffer val_ok = new StringBuffer(val);

                            StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                            StringBuffer convert = new StringBuffer(sb_val);


                            if (usePlugins) {
                                PluginRenderer rp = new PluginRenderer();
                                convert = new StringBuffer(rp.checkTheme(convert, msgState, blockedServerPlugins));
                                rp=null;
                            }
                            if (!sb_val.equals(convert)) {
                                encode = false;
                                val = convert.toString();
                            } else {
                                val = val_ok.toString();
                                encode = tok.startsWith("theme");
                            }
                    }
                } else if (tok.startsWith ("lockstate")) {
                    TemplateSet ts = msgState.sender == null 
                        ? TemplateManager.mgr.getTemplateSet()
                        : msgState.sender.getTemplateSet();
                    if (cg.hasState(IGroupState.OPEN)) {
                        val = ts.getMessageTemplate("constant.openGroup");
                    } else {
                        val = ts.getMessageTemplate("constant.lockedGroup");
                    }
                    if (val == null)
                        val = "";
                } else if (tok.startsWith ("islocked")) {
                    if (!cg.hasState(IGroupState.OPEN)) {
                        TemplateSet ts = msgState.sender == null 
                            ? TemplateManager.mgr.getTemplateSet()
                            : msgState.sender.getTemplateSet();
                        val = ts.getMessageTemplate("constant.lockedGroup");
                    } else
                        val = "";
                } else if (tok.startsWith ("isopen")) {
                    if (cg.hasState(IGroupState.OPEN)) {
                        TemplateSet ts = msgState.sender == null 
                            ? TemplateManager.mgr.getTemplateSet()
                            : msgState.sender.getTemplateSet();
                        val = ts.getMessageTemplate("constant.openGroup");
                    } else
                        val = "";
                } else if (tok.startsWith ("questioncounter")) {
                    val = String.valueOf(cg.getQuestionCounter());
                } else if (tok.startsWith ("usersingroupcount")) {
                    val = String.valueOf(cg.userInGroupCount());
                } else if (tok.startsWith ("opener")) {
                    if (msgState.sender != null && msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
                        User u = UserManager.mgr.getUserByName(cg.getOpener().getName());
                        if (u != null){
                            if (Server.srv.USE_FADECOLOR){
                                if (u.getFadeColCode() != null && u.getFadeColorUsername()!=null){
                                    val = u.getFadeColorUsername().toString();
                                } else val = u.getNoFadeColorUsername();
                            } else val = EntityDecoder.charToHtml (u.getName ());
                        } else {
                            val = cg.getOpener().getName();
                        }
                    }
                    if (val == null)
                        val = "";
                } 
            } else {
                val = "";
            }
        } else if (tok.startsWith("message")
           || tok.startsWith ("userlist")
           || tok.startsWith ("result")) {
        	if (msgState.message==null) {
                val = "";           
            } else { // userlist or result could contain html, is that true?              
                val = msgState.message;
                StringBuffer val_ok = new StringBuffer(val);
                boolean toupper = false;
                if (tok.indexOf("touppercase") > -1) {
               	    val = val.toUpperCase();
               	    toupper = true;
               	    val_ok = new StringBuffer(val);             	
                } else if (tok.indexOf("tolowercase") > -1) {
                    val = val.toLowerCase();
                    val_ok = new StringBuffer(val);
                }
                if (tok.startsWith ("message")) {
                    StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                    StringBuffer convert = new StringBuffer(sb_val); 
                 
                    if (usePlugins){
                        PluginRenderer rp = new PluginRenderer();
                        convert =new StringBuffer(rp.checkMessage(convert, msgState, toupper, blockedServerPlugins));
                    }

                    if (!sb_val.equals(convert)) {
                        encode = false;
                        val = convert.toString();
                    } else {
                        val = val_ok.toString();
                	    encode=tok.startsWith("message");
                    }
                }
            }
        } else if (tok.startsWith ("reason")
                   || tok.startsWith ("throw")) {    
        	StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(msgState.reason == null ? "" : msgState.reason));
        	if (tok.startsWith ("reason") && msgState.reason != null) {
                StringBuffer convert = new StringBuffer(sb_val);                
                if (usePlugins){
                    PluginRenderer rp = new PluginRenderer();
                    convert = new StringBuffer(rp.checkReason(convert, msgState, blockedServerPlugins));
                }
                if (!sb_val.toString().equals(convert)) {
                    encode =false;
             	    val = convert.toString();
                } else {
             	    val = msgState.reason == null ? "" : msgState.reason;;
                    encode=true;  
                }
          	} else {
        		 val = sb_val.toString();
                 encode=true;
        	}
        } else if (tok.startsWith ("reciever")
                   || tok.startsWith ("target")
                   || tok.startsWith ("friendscount")
                   || tok.startsWith ("source")
                   || tok.startsWith ("total")
                   || tok.startsWith ("param")) { 
        	val = msgState.param;
        	StringBuffer val_ok = new StringBuffer(val);                  
            if (tok.startsWith ("param")) {
                StringBuffer sb_val = new StringBuffer(EntityDecoder.charToHtml(val));
                StringBuffer convert = new StringBuffer(sb_val); 
                if (Server.srv.USE_PLUGINS){
                    PluginRenderer rp = new PluginRenderer();
                    convert = new StringBuffer(rp.checkParam(convert, msgState, blockedServerPlugins));
                }
                if (!sb_val.equals(convert)) {
                    encode = false;
                    val = convert.toString();
                } else {
                   val = val_ok.toString();
         	       encode=tok.startsWith("param");
                }
            }
        } else if (tok.startsWith ("firstusers")
                || tok.startsWith ("firstmemberships")) {
            if (msgState.usrList == null
                || msgState.usrList.length < 2)
                    val = "";
            else {
                TemplateSet ts = msgState.sender == null 
                    ? TemplateManager.mgr.getTemplateSet()
                    : msgState.sender.getTemplateSet();
                boolean useTpl = (ts.getMessageTemplate("constant.userListItem")!=null);
                msgState.useRenderCache=false;
                User u = msgState.usercontext;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < msgState.usrList.length-1; i++) {
                    Object curr = msgState.usrList[i]; 
                    if (curr instanceof User) {
                        if (!useTpl) {
                            sb.append (((User) msgState.usrList[i]).getName());
                            encode=true;
                        } else {
                            msgState.usercontext=(User) msgState.usrList[i];
                            sb.append (renderTemplate(msgState,ts.getMessageTemplate("constant.userListItem"), false, blockedServerPlugins));
                        }
                    } else
                        sb.append (curr.toString());
                    if (i < msgState.usrList.length-2)
                        sb.append (", ");
                }
                msgState.usercontext=u;
                val = sb.toString();
            }
        } else if (tok.startsWith ("lastuser")
                || tok.startsWith ("lastmembership")) {
            Object item = msgState.usrList[msgState.usrList.length-1];
            TemplateSet ts = msgState.sender == null 
                ? TemplateManager.mgr.getTemplateSet()
                : msgState.sender.getTemplateSet();
            if (item instanceof User) {
                if (ts.getMessageTemplate("constant.userListItem")==null) {
                    val = ((User) item).getName();
					encode=true;
                } else {
                    User u = msgState.usercontext;
                    msgState.usercontext = (User) item;
                    val = renderTemplate(msgState, ts.getMessageTemplate("constant.userListItem"), false, blockedServerPlugins);
                    msgState.usercontext = u;
                }
            } else
                val = item.toString();
        } else if (tok.startsWith ("membership")) {
            if (msgState.usrList != null && msgState.usrList.length > 0) {
                val = msgState.usrList[0].toString();
            	encode=true;
            } else
                val = "";
        } else if (tok.startsWith ("time")) {
            String fstrg = "[HH:mm]";
            int pos = tok.indexOf("format="); 
            if (pos != -1) {
                fstrg = token.substring (pos+6);
                fstrg = retrieveParamValue (fstrg)[1];
            }
            val = Server.srv.getFormatedTime (fstrg);
        } else if (tok.startsWith("vipcount")) {
            val = String.valueOf(UserManager.mgr.onlineVipList().size());
        } else if (tok.startsWith("onlineusers")) {
            val = String.valueOf(UserManager.mgr.getActiveUserCount());
        } else if (tok.startsWith("opengroups")) {
            val = String.valueOf(GroupManager.mgr.openGroupsCount());
        } else if (tok.startsWith ("constant")) {
            int i = tok.indexOf(" ");
            TemplateSet ts = msgState.sender == null 
                ? TemplateManager.mgr.getTemplateSet()
                : msgState.sender.getTemplateSet();
            String msgTpl;
            if (i == -1) {
                msgTpl = token;
            } else {
                msgTpl = token.substring (0,i);
            }
            msgTpl = ts.getMessageTemplate(msgTpl);
            if (msgTpl == null) {
                val = "[Constant not found: " + msgTpl + "]";
                encode=true;
            } else {
                val = renderTemplate(msgState, msgTpl, false, blockedServerPlugins);
            }
        }
        if (val == null) 
            return (token);
        int pos = Math.max(token.indexOf (" "), token.indexOf("."));
        if (pos == -1) 
            return encode ? EntityDecoder.charToHtml(val) : val;
            
        if (!tok.startsWith("message")
            && !tok.startsWith ("userlist")
            && !tok.startsWith ("result")) {
   
            if (tok.indexOf("touppercase") > -1) {
                val = val.toUpperCase();
            } else if (tok.indexOf("tolowercase") > -1) {
                val = val.toLowerCase();
            }
  
        }
        if (encode) {
        	val = EntityDecoder.charToHtml(val);
        }
        return renderValue (msgState, val, token.substring (pos).trim (), blockedServerPlugins); 
    }

	private static String renderGroupthemeCreator(MessageState msgState, Group cg){
	    StringBuilder val = new StringBuilder("");
	    if (msgState.sender != null && cg.getThemeCreator() != null){
            if (msgState.sender.hasRight(IUserStates.ROLE_VIP) && cg.getThemeCreator().getName() != null) {
                User u = UserManager.mgr.getUserByName(cg.getThemeCreator().getName());
                if (u != null){
                    if (Server.srv.USE_FADECOLOR){
                        if (u.getFadeColCode() != null && u.getFadeColorUsername()!=null){
                            val = new StringBuilder(u.getFadeColorUsername().toString());
                        } else val =  new StringBuilder(u.getNoFadeColorUsername());
                    } else val =  new StringBuilder(EntityDecoder.charToHtml (u.getName ()));
                } else {
                    User se = cg.getThemeCreator();
                    if (se instanceof User){
                        {
                            if (Server.srv.USE_FADECOLOR){
                                if (se.getFadeColCode() != null && se.getFadeColorUsername()!=null){
                                    val =  new StringBuilder(se.getFadeColorUsername().toString());
                                } else val =  new StringBuilder(se.getNoFadeColorUsername());
                            } 
                        }
                    } else val =  new StringBuilder("");
                }
            }
        } else val =  new StringBuilder("");
        val.trimToSize();
        return val.toString();	    
	}
	
    private static String renderChattime (User cu) {
        long s = cu.getChattime(); 
        long m = s / 60;
        long h = m / 60;
        s = s - m * 60;
        m = m - h * 60;
        StringBuffer sb = new StringBuffer();
        if (h > 0)
           sb.append (h).append("h ");
        if (m > 0)
           sb.append (m).append("m ");
        sb.append (s).append ("s");
        return sb.toString();
    }

    /**
     * renders the given User's title taking the layout of msgState.sender
     * @param msgState the current message-state containing the sender
     * @param cu the user whos title should be rendered
     * @return the rendered string
     */
    private static String renderTitle (MessageState msgState, User cu) {
        StringBuffer sb = new StringBuffer();
        String val="";
		Group sg = cu.getGroup ();
        TemplateSet ts = msgState.sender == null
                        ? TemplateManager.mgr.getTemplateSet()
                        : msgState.sender.getTemplateSet();
                        
        boolean append = false, displayDefaultVipRighttitle = true, displayDefaultModGuestTitle = true;
        String seperator = ts.getMessageTemplate("constant.title.seperator");
        if (cu.getCustomTitle() != null && cu.getCustomTitle().length() > 1){
        	append = true;
        	val = ts.getMessageTemplate("constant.customtitle.prefix");
            if (val != null)
                sb.append (val);
            sb.append (cu.getCustomTitle());
            val = ts.getMessageTemplate("constant.customtitle.suffix");
            if (val != null)
                sb.append (val);
        } else {
            Membership defaultMembership = cu.getDefaultMembership();
            if (defaultMembership != null) {
            	displayDefaultVipRighttitle = defaultMembership.displayDefaultVipRighttitle();
            	displayDefaultModGuestTitle = defaultMembership.displayDefaultModGuesttitle();

            }
            if (defaultMembership != null && defaultMembership != MembershipManager.undefined) {
                val = ts.getMessageTemplate("constant.title.membership."+ defaultMembership.key);
                if (val == null) {
                    val = defaultMembership.getName();
                    if (val.length() <= 0)
                    	val = null;                    	
                }
                if (val != null) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
        }
        if (cu.hasRight(IUserStates.IS_MODERATOR) && sg.hasState(IGroupState.MODERATED))  {
                val = ts.getMessageTemplate("constant.title.moderator");
                if (val != null && displayDefaultModGuestTitle) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
            if (cu.hasRight(IUserStates.IS_GUEST) && sg.hasState(IGroupState.MODERATED)) {
                val = ts.getMessageTemplate("constant.title.guest");
                if (val != null && displayDefaultModGuestTitle) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
            if (cu.hasRole(IUserStates.ROLE_VIP)) {
                val = ts.getMessageTemplate("constant.title.vip");
                if (val != null && displayDefaultVipRighttitle) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
            if (cu.hasRole(IUserStates.ROLE_GOD)) {
                val = ts.getMessageTemplate("constant.title.god");
                if (val != null && displayDefaultVipRighttitle) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
            if (cu.getGroup() != null && cu.getGroup().usrIsSu(cu) && !cu.hasRight(IUserStates.ROLE_VIP)) {
                val = ts.getMessageTemplate("constant.title.su");
                if (val != null) {
                    if (append && seperator != null)
                        sb.append (seperator);
                    sb.append (val);
                    append = true;
                }
            }
        
        return sb.toString();
    }

    /**
     * renders the username including the visual changes depending on userrights
     * @param cu the username to render
     * @return the rendered username
     */
    private static String renderUserName (MessageState msgState, User cu) {
        if (cu==null)
            return null;
        StringBuffer tsb = new StringBuffer ();
        Group sg = cu.getGroup ();
        boolean isSu = false, isPunished = cu.isPunished (), setPrefsuffix = false;
        if (sg != null)
            isSu = sg.usrIsSu (cu);
        String pre = "",
               suf = "";
        TemplateSet ts = msgState.sender == null
                        ? TemplateManager.mgr.getTemplateSet()
                        : msgState.sender.getTemplateSet();
        Membership defaultMembership = cu.getDefaultMembership();
        if (cu.getDefaultMembership() != null) {
        	pre = ts.getMessageTemplate("constant.membership."+ defaultMembership.key + ".prefix");
            suf = ts.getMessageTemplate("constant.membership."+ defaultMembership.key + ".suffix");
            if (pre == null && suf == null) {
                pre = defaultMembership.getNamePrefix();
                suf = defaultMembership.getNameSuffix();
            }
            if (pre == null && suf == null) {
            	pre = "";
				suf = "";
            } else  setPrefsuffix = true;
        }
        if (cu.hasRight(IUserStates.IS_GUEST) && !cu.hasRole(IUserStates.ROLE_GOD)) {
            if (sg != null && sg.hasState(IGroupState.MODERATED)) {
                pre = ts.getMessageTemplate("constant.username.guest.prefix");
                suf = ts.getMessageTemplate("constant.username.guest.suffix");
                if (pre == null && suf == null) {
                    pre = Server.srv.UNAME_PREFIX_GUEST;
                    suf = Server.srv.UNAME_SUFFIX_GUEST;
                }
           }
        }
		if (cu.hasRight(IUserStates.IS_GUEST) && cu.hasRole(IUserStates.ROLE_VIP) && !setPrefsuffix) {
			pre = ts.getMessageTemplate("constant.username.vip.prefix");
			suf = ts.getMessageTemplate("constant.username.vip.suffix");
            if (pre == null && suf == null) {
                pre = Server.srv.UNAME_PREFIX_VIP;
                suf = Server.srv.UNAME_SUFFIX_VIP;
            }
            if (sg != null && sg.hasState(IGroupState.MODERATED)) {
                if ((ts.getMessageTemplate("constant.username.guest.prefix") != null)&& (ts.getMessageTemplate("constant.username.vip.prefix")!= null)) pre = ts.getMessageTemplate("constant.username.vip.prefix")+ts.getMessageTemplate("constant.username.guest.prefix"); else pre = null;
                if ((ts.getMessageTemplate("constant.username.guest.suffix") != null)&& (ts.getMessageTemplate("constant.username.vip.suffix")!= null)) suf = ts.getMessageTemplate("constant.username.guest.suffix")+ts.getMessageTemplate("constant.username.vip.suffix"); else suf = null;
                if (pre == null && suf == null) {
                    pre = Server.srv.UNAME_PREFIX_VIP+Server.srv.UNAME_PREFIX_GUEST;
                    suf = Server.srv.UNAME_SUFFIX_GUEST+Server.srv.UNAME_SUFFIX_VIP;
                }
            }
        } else if (cu.hasRight(IUserStates.IS_MODERATOR) && !cu.hasRight(IUserStates.IS_GUEST) && !cu.hasRole(IUserStates.ROLE_GOD) && !setPrefsuffix) {
                   if (sg != null && sg.hasState(IGroupState.MODERATED)) {
                       pre = ts.getMessageTemplate("constant.username.moderator.prefix");
                       suf = ts.getMessageTemplate("constant.username.moderator.suffix");
                       if (pre == null && suf == null) {
                           pre = Server.srv.UNAME_PREFIX_MODERATOR;
                           suf = Server.srv.UNAME_SUFFIX_MODERATOR;
                    }
    		  }
              if (cu.hasRight(IUserStates.IS_MODERATOR)&& cu.hasRole(IUserStates.ROLE_VIP) && !setPrefsuffix) {
                  pre = ts.getMessageTemplate("constant.username.vip.prefix");
                  suf = ts.getMessageTemplate("constant.username.vip.suffix");
                  if (pre == null && suf == null) {
                      pre = Server.srv.UNAME_PREFIX_VIP;
                      suf = Server.srv.UNAME_SUFFIX_VIP;
                  }
                  if (sg != null && sg.hasState(IGroupState.MODERATED)) {
                      if ((ts.getMessageTemplate("constant.username.moderator.prefix") != null) && (ts.getMessageTemplate("constant.username.vip.prefix") != null)) pre = ts.getMessageTemplate("constant.username.vip.prefix") + ts.getMessageTemplate("constant.username.moderator.prefix");  else pre = null;
                      if ((ts.getMessageTemplate("constant.username.moderator.suffix") != null) && (ts.getMessageTemplate("constant.username.vip.suffix") != null)) suf = ts.getMessageTemplate("constant.username.moderator.suffix")+ ts.getMessageTemplate("constant.username.vip.suffix"); else suf = null;
                      if (pre == null && suf == null) {
                          pre = Server.srv.UNAME_PREFIX_VIP+Server.srv.UNAME_PREFIX_MODERATOR;
                          suf = Server.srv.UNAME_SUFFIX_MODERATOR+Server.srv.UNAME_SUFFIX_VIP;
                     }
                  }
     		  }
		} else if (cu.hasRole(IUserStates.ROLE_GOD) && !setPrefsuffix) {
            pre = ts.getMessageTemplate("constant.username.god.prefix");
            suf = ts.getMessageTemplate("constant.username.god.suffix");
            if (pre == null && suf == null) {
                pre = Server.srv.UNAME_PREFIX_GOD;
                suf = Server.srv.UNAME_SUFFIX_GOD;
            }
	    } else if (cu.hasRole(IUserStates.ROLE_VIP) && !setPrefsuffix) {
            pre = ts.getMessageTemplate("constant.username.vip.prefix");
            suf = ts.getMessageTemplate("constant.username.vip.suffix");
            if (pre == null && suf == null) {
                pre = Server.srv.UNAME_PREFIX_VIP;
                suf = Server.srv.UNAME_SUFFIX_VIP;
            }
        } else if (isSu && !setPrefsuffix) {
            pre = ts.getMessageTemplate("constant.username.su.prefix");
            suf = ts.getMessageTemplate("constant.username.su.suffix");
            if (pre == null && suf == null) {
                pre = Server.srv.UNAME_PREFIX_SU;
                suf = Server.srv.UNAME_SUFFIX_SU;
            }
        }
        if (isPunished) {
            pre = ts.getMessageTemplate("constant.username.punished.prefix");
            suf = ts.getMessageTemplate("constant.username.punished.suffix");
            if (pre == null && suf == null) {
                pre = Server.srv.UNAME_PREFIX_PUNISHED;
                suf = Server.srv.UNAME_SUFFIX_PUNISHED;
            }
        }
        tsb.append (pre);
        if (Server.srv.USE_FADECOLOR){
            if (cu.getFadeColCode() != null && cu.getFadeColorUsername()!=null){
                tsb.append(cu.getFadeColorUsername().toString());
    	    } else tsb.append(cu.getNoFadeColorUsername());
        } else tsb.append (EntityDecoder.charToHtml (cu.getName ()));
        tsb.append (suf);
        return tsb.toString ();
    }

	/**
	 * renders the value according to the parameters
	 * @param val the value to render
	 * @param params the params for the rendering
	 * @return the rendered value
	 */
    private static String renderValue (MessageState msgState, String val, String params, Vector<String> blockedServerPlugin) {
        if (params == null || params.length() < 1)
            return val;
        StringBuffer sb = new StringBuffer ();
        String prefix = "";
        String suffix = "";
        String plower = params.toLowerCase(); 
        while (params.length () > 0) {
            if (params.startsWith ("toUpperCase")
                    || params.startsWith ("toLowerCase")
                    || params.startsWith ("touppercase")
                    || params.startsWith ("tolowercase")) {
                params = params.substring (11);
                continue; 
            }
            if (params.startsWith ("prefix")) {
                int start = params.indexOf ("=");
                if (start == -1) return ("syntax");
                params = params.substring (start);
                String vals[] = retrieveParamValue (params);
                params = vals[0];
                prefix = vals[1];
            } else if (params.startsWith ("suffix")) {
                int start = params.indexOf ("=");
                if (start == -1) return ("syntax");
                params = params.substring (start);
                String vals[] = retrieveParamValue (params);
                params = vals[0];
                suffix = vals[1];
            } else if (params.toLowerCase().startsWith ("ifempty")
                       && val.length() < 1) {
                int start = params.indexOf ("=");
                if (start == -1) return ("syntax");
                params = params.substring (start);
                String vals[] = retrieveParamValue (params);
                return vals[1];
            } else if (params.startsWith ("value") && !val.equals ("")) {
                int start = params.indexOf ("=");
                if (start == -1) return ("syntax");
                params = params.substring (start);
                String vals[] = retrieveParamValue (params);
                params = vals[0];
                val = vals[1];
            } else break;
        }
        if (val.equals ("")) return ("");
        if (prefix.indexOf("@") > -1) {
            prefix = renderTemplate (msgState, prefix.replace('@', '§'), false, blockedServerPlugin);
        }
        if (suffix.indexOf("@") > -1) {
            suffix = renderTemplate (msgState, suffix.replace('@', '§'), false, blockedServerPlugin);
        }
        sb.append (prefix);
        sb.append (val);
        sb.append (suffix);
        return sb.toString ();
    }

	public static String[] retrieveParamValue (String params) {
	  params = params.trim ();
	  StringBuffer tsb = new StringBuffer ();
	  boolean quote=false, mask=false, firstChar = false;
	  int stop = 0;
	  for (int i = 0; i < params.length (); i++) {
		 char c = params.charAt (i);
		 if (mask) {
			mask = false;
			tsb.append (c);
		 } else if (firstChar && !quote && c == ' ') {
			stop = i;
			break;
		 } else if (c == '\\') {
			mask = true;
		 } else if (!mask && !quote && c == '"') {
			quote = true;
		 } else if (!mask && c == '"') {
			stop = i+1;
			break;
		 } else if (!firstChar && c != ' ') {
			firstChar = true;
		 } else {
			tsb.append (c);
		 }
	  }
	  String result[] = new String[2];
	  if (params.length () > stop)
		 result[0] = params.substring (stop).trim ();
	  else
		 result[0] = "";

	  result[1] = tsb.toString ();
	  return result;
   }

    public static ByteBuffer encode (String s) {
        try {
            return Server.srv.defaultCsEnc.encode (CharBuffer.wrap(s));
        } catch (CharacterCodingException cce) {
            return Server.srv.defaultCs.encode(CharBuffer.wrap(s));
        }
    }
    
    private static boolean hasMinRight(int minRight, MessageState msgState, String configName){
        StringBuffer role = new StringBuffer(CommandSet.getCommandSet().getCommandSetProps().getProperty(configName));
        if (role == null)
            if (Server.DEBUG)
                Server.log("MessageRenderer", "command.properties "+ configName + " not found", Server.MSG_ERROR, Server.LVL_VERBOSE);
        if (role != null && UserManager.resolveState(role.toString()) >0)
            minRight = UserManager.resolveState(role.toString());
        role=null;
        if (minRight == IUserStates.ROLE_USER || msgState.sender.hasRole(IUserStates.ROLE_GOD) || msgState.sender.hasDefaultRight(IUserStates.ROLE_GOD))
            return true;
        if (minRight == IUserStates.ROLE_VIP){
            if (!msgState.sender.hasRight(IUserStates.ROLE_VIP)) {
                 return false;
            }
        }
        if (minRight == IUserStates.ROLE_GOD){
            if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
                return false;
            }
        }
        return true; 
    }
}



/*   public String evalAlgorithm (String token) {
if (!token.startsWith ("if(") && !token.startsWith ("if (")) return token;
String expression = token.substring (token.indexOf ("("), token.lastIndesOf (")"));
StringBuffer sb = new StringBuffer ();
int i = 0;
for (; i<expression.length (); i++) {
	 char c = expression.charAt (i);
	 if (c == '!' || c == '=' || c == ' ' || c == '<' || c == '>')
		 break;
	  sb.append (c);
}
String left = sb.toString ().trim ();
sb = new StringBuffer ();
for (; i<expression.length (); i++) {
	 char c = expression.charAt (i);
	 if (c != '!' || c != '=' || c != ' ' || c != '<' || c != '>')
		 break;
	  sb.append (c);
}
String operand = sb.toString ();
sb = new StringBuffer ();
for (; i<expression.length (); i++) {
	 char c = expression.charAt (i);
	 if (c == '!' || c == '=' || c == ' ' || c == '<' || c == '>')
		 break;
	  sb.append (c);
}
String right = sb.toString ();
if (left.startsWith ("\"")) {
	 left = left.substring (1, left.length ()-2);
} else (
	 String tstr = evalVariable (left);
	 if (tstr.length () < 1) return token;
	 left = tstr;
}
if (left.length () < 1 || operand.length () < 1 || right.length () < 1)
	 return (token)

} */
