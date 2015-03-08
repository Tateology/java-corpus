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
import freecs.auth.AuthManager;
import freecs.content.*;
import freecs.external.AccessForbiddenException;
import freecs.external.CmdConfirmHandler;
import freecs.external.IRequestHandler;
import freecs.external.StateRequestHandler;
import freecs.external.StaticRequestHandler;
import freecs.external.UserlistRequestHandler;
import freecs.external.WebadminRequestHandler;
import freecs.interfaces.*;
import freecs.util.CookieGenerator;
import freecs.layout.TemplateSet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * parses the request
 * has a static parse function, which automatically decides which
 * RequestEvaluator has available resources and suplies the Request to
 * this RequestEvaluator. If it fails more than MAX_THREADSEARCHES times,
 * it creates a new RequestEvaluator-Thread.
 */
public class RequestEvaluator {
   private MessageParser mp;
   private short parserID;
   private RequestReader req;
   private HashMap<String, Object> requestHandlers;

   public RequestEvaluator (RequestReader r) {
      parserID = r.getID();
      mp = new MessageParser (r);
      req=r;
      requestHandlers = new HashMap<String, Object>();
      requestHandlers.put("/userlist", new UserlistRequestHandler("/USERLIST"));
      requestHandlers.put("/state", new StateRequestHandler("/STATE"));
      requestHandlers.put("/admin", new WebadminRequestHandler("/ADMIN"));
      requestHandlers.put("/static", new StaticRequestHandler("/static"));
      requestHandlers.put("/cmdconfirm", new CmdConfirmHandler("/cmdconfirm"));
      
      Vector<String> jarUrl = new Vector<String>();
      String url = Server.srv.props.getProperty("handlerUrl");
      if (url != null)
          jarUrl.add(url);
      if (jarUrl != null)
          loadHandler(jarUrl);
   }

	/**
	 * this decides what to do with the requst and how to answere it
	 * @param cReq The IRequest-Object containing the requestparameters
	 */
    public void evaluate(IRequest cReq) {
        if (cReq==null)
            return;
        SelectionKey key = cReq.getKey ();
        if (!CentralSelector.isSkValid(key)) {
            Server.log(this, "evaluate: request has invalid key", Server.MSG_STATE, Server.LVL_VERBOSE);
            return;
        }
        ConnectionBuffer rb = cReq.getConnectionBuffer ();
        // every key must have a ConnectionBuffer
        if (rb == null) {
            Server.log (this, "ConnectionBuffer was empty", Server.MSG_ERROR, Server.LVL_MAJOR);
            CentralSelector.dropKey(key);
            return;
        }
        req.currPosition = RequestReader.EVALUATING;
      try {
         String action = cReq.getAction ();
         byte method = cReq.getMethod ();
         String cookie = cReq.getCookie ();
         
         // init logging-message
         rb.addLog (method == IRequest.METHOD_GET ? "GET" : "POST");
         rb.addLog (action);
         rb.addLog (((HTTPRequest) cReq).isHTTP11 () ? "HTTP/1.1" : "HTTP/1.0");
         if (cookie == null)
            rb.addLog ("NO-COOKIE");
         else
            rb.addLog (cookie);
         rb.addLog (" ");
         rb.addLog (cReq.getUserAgent());
         
         // get the user identified by the given cookie
         User u = (cookie == null ? null : UserManager.mgr.getUserByCookie (cookie));
         boolean isHTTP11 = ((HTTPRequest) cReq).isHTTP11 ();

         // Check for templateset
         TemplateSet ts = null;
         String templateset = cReq.getValue ("templateset");
         if (templateset != null) {
            ts = Server.srv.templatemanager.getTemplateSet (templateset);
         } else if (u != null) {
            ts = u.getTemplateSet ();
         }

         ContentContainer c = new ContentContainer ();
         ts = checkMobileBrowser(u, ts,c,cReq);
         ((ContentContainer) c).setHTTP11 (isHTTP11);
         if (ts != null) {
            c.useTemplateSet (ts);
         }
         if (cookie == null) {
            c.setCookie (CookieGenerator.generateCookie ());
         }
         if (cookie != null && "/SEND".equals(action)) {
             RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
             boolean refererFound =  cReq.foundReferer();

             if (!Server.srv.ALLOW_EXTERNAL && !refererFound){
                 rb.logError("/SEND wrong referer");
                 c.setTemplate("not_found");
             } else {
                     if (!handleSend(u, rb, key, cReq, isHTTP11))
                         return;
                     c.wrap ("dummy", cReq.getCookieDomain());
              }
         } else if (action.toLowerCase().startsWith("/static/")  
                 || action.toLowerCase().equals("/robots.txt")
                 || action.toLowerCase().equals("/favicon.ico")) {
             RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
             try {
                 IRequestHandler reqHandler = (IRequestHandler) requestHandlers.get("/static");
                 reqHandler.handle(cReq, c);
             } catch (AccessForbiddenException noAccess) {
                 if (noAccess.hidePage() == true) {
                     c.setTemplate("not_found");
                 }
             }
         } else if (requestHandlers.containsKey(action.toLowerCase())) {
            // pass request to a registered request handler
            RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
            try {
                IRequestHandler reqHandler = ((IRequestHandler)requestHandlers.get(action.toLowerCase()));
                reqHandler.handle(cReq, c);
            } catch (AccessForbiddenException noAccess) {
                if (noAccess.hidePage() == true) {
                     c.setTemplate("not_found");
                }
            }
         } else if (method==IRequest.METHOD_GET) {
			req.currPosition=RequestReader.EVAL_GET;
            if ("/".equals(action)) {
               RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
               c.setTemplate ("start");
            } else if (cookie != null && "/LOGIN".equals (action)) {
                RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.LOGIN_TIMEOUT);
                if (u!=null) {
                    softCloseMessagesConnection(u, u.getKey());
                }
                if (UserManager.mgr.tryLogin(null,null,null,ts,req,u,null) == UserManager.LOGIN_RELOAD) {
                	if (u.canUseTemplateset(ts)){
                	    c.setTemplate ("frameset");
                        if (ts != null) { 
                            u.setTemplateSet (ts);                     
                        }
                    } else {
                    	c.setTemplate("login_notallowed");
                    }
                } else {
                	AuthManager.instance.doLogin(cReq, key, cookie, c, ts, u, isHTTP11, req);
                }
            } else if (!c.canUseTemplateset(ts) && u == null){
            	c.setTemplate("not_found");
            } else if ("/INPUT".equals(action)) {
                RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
                String tName = c.checkTName(u,"input");
                c.setTemplate (tName);
            } else if ("/MESSAGES".equals(action)) {
                RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
                handleMessagesConnection(c, u, cReq, key, isHTTP11, rb);
                return;
            } else if ("/DUMMY".equals(action)) {
               c.wrap ("dummy", cReq.getCookieDomain());
               // c.setKeepAlive (false);
            } else {
               RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.READER_TIMEOUT);
               if (ts == null) ts = Server.srv.templatemanager.getTemplateSet("default");
               StringBuffer tname = new StringBuffer();
               if (action.length() > 0) 
                   tname.append(action.substring (1).toLowerCase ());              
               if (tname != null && tname.length () > 1) {
                  c.setTemplate (tname.toString());
               } else {
                  c.setTemplate ("not_found");
               }
            }
         } else if (method==IRequest.METHOD_POST) {
            RequestMonitor.instance.addMonitor(Thread.currentThread(), System.currentTimeMillis() + Server.srv.LOGIN_TIMEOUT);
			req.currPosition=RequestReader.EVAL_POST;
            if (cookie != null && "/LOGIN".equals (action)) {
            	req.currPosition=RequestReader.EVAL_POST_LOGIN;
                if (u!=null) {
                    softCloseMessagesConnection(u, u.getKey());
                }
                AuthManager.instance.doLogin(cReq, key, cookie, c, ts, u, isHTTP11, req);
            } else {
            	c.setTemplate ("not_found");
            }
         } else {
         	c.setTemplate ("not_found");
         }
		 req.currPosition=RequestReader.EVAL_PREP4SEND;
         if (c.prepareForSending (cReq)) {
            if (!rb.isValid()) {
				rb.logError("ConnectionBuffer was invalidated");
				CentralSelector.dropKey (key);
				return;
			}
			req.currPosition=RequestReader.EVAL_SENDFINAL;
			rb.addToWrite (c.getByteBuffer());
            if (c.closeSocket())
                rb.addToWrite(Responder.CLOSE_CONNECTION);
         } else Server.log (this, "evaluate: prepareForSending failed", Server.MSG_ERROR, Server.LVL_VERY_VERBOSE);
      } catch (Exception e) {
         CentralSelector.dropKey (key);
         Server.debug (this, "evaluate: drop key", e, Server.MSG_ERROR, Server.LVL_MAJOR);
         rb.logError(e.getMessage());
      }
    }
    
    /**
     * 
     * @param u
     * @param ts
     * @param c
     * @param cReq
     * @return
     */
    private TemplateSet checkMobileBrowser(User u,TemplateSet ts, ContentContainer c, IRequest cReq){
        if (!Server.srv.REDIRECT_MOBILE_BROWSER)
            return ts;
        if (c.isMobileBrowser(cReq.getUserAgent())){
            String templateset = "mobile";
            if (ts!=null && ts.getName() != null){
                if (!ts.getName().toLowerCase().equals("mobile")){
                     ts = Server.srv.templatemanager.getTemplateSet (templateset);                   
                 }
            } else {         
                ts = Server.srv.templatemanager.getTemplateSet (templateset);       
            }
            if (ts==null || ts.getName().toLowerCase().equals("default")){
                Server.log(this, "setting Templateset default -- Templateset \"mobile\" not found", Server.MSG_ERROR, Server.LVL_MAJOR);
            }
            if (u != null)
                u.setTemplateSet(ts);
         } else {
            if (ts!=null && ts.getName() != null){
                if (ts.getName().toLowerCase().equals("mobile")){
                    ts = Server.srv.templatemanager.getTemplateSet ("default");
                    Server.log(this, "Browser is not a Mobilebrowser --- setting Templateset default -- Templateset \"mobile\" is not allowed", Server.MSG_ERROR, Server.LVL_MAJOR);
                    if (u != null)
                        u.setTemplateSet(ts);
                }
            }
        }
        return ts;
    }

    /**
     * @param c
     * @param u
     * @param req2
     * @param key
     * @param isHTTP11
     */
    private void handleMessagesConnection(ContentContainer c, User u, IRequest cReq, SelectionKey key, boolean isHTTP11, ConnectionBuffer rb) {
        req.currPosition=RequestReader.EVAL_GET_MESSAGES;
        if (u != null 
                && (u.isJoining()
                    || u.isLoggedIn()
                    || u.isRemoving())) {
                Connection conn = cReq.getConnectionObject();
                if (!u.wasActive ()) {
                    rb.logError("flooded");
                    return;
                }
                if (!rb.isValid()) {
                    CentralSelector.dropKey(key);
                    rb.logError("ConnectionBuffer was invalidated");
                    return;
                }
                synchronized (rb) {
                    rb.conn = conn;
                    rb.setIsMessageFrame(true);
                }
                SelectionKey oldKey;
                synchronized (u) {
                    oldKey = u.getKey();
                    u.setKey(key);
                }
                if (oldKey != null) {
                    // we do have an open connection for the /messages-frame
                    softCloseMessagesConnection(u, oldKey);
                }
                u.setHTTP11 (isHTTP11);
                c.setNoCache ();
                c.setNoStore ();
                c.setIsMessages ();
                String tName = c.checkTName(u, "welcome");
                c.setTemplate (tName);
                if (!c.prepareForSending (cReq)) {
                    Server.log (this, "evaluate: unable to init /MESSAGES: prepareForSending failed", Server.MSG_TRAFFIC, Server.LVL_MAJOR);
                    rb.logError("/MESSAGE prepare for sending failed");
                    CentralSelector.dropKey(key);
                    return;
                }
                req.currPosition=RequestReader.EVAL_GET_MESSAGES_APND2WRITE;
                rb.addToWrite (c.getByteBuffer());
                if (c.closeSocket())
                    rb.addToWrite(Responder.CLOSE_CONNECTION);
                req.currPosition=RequestReader.EVAL_GET_MESSAGES_SND_MSGS;
                u.sendScheduledMessages();
                return;
           } else {
              Server.log (this, "evaluate: bogous cookie or expired", Server.MSG_STATE, Server.LVL_MINOR);
              if (u==null)
                  c.setTemplate ("no_cookie");
              else
                  c.setTemplate("login_missing");
           }
    }

    /**
     * Handle /SEND
     * @param u
     * @param rb
     * @param key
     * @param cReq
     * @param isHTTP11
     */
    private boolean handleSend(User u, ConnectionBuffer rb, SelectionKey key, IRequest cReq, boolean isHTTP11) {
        req.currPosition=RequestReader.EVAL_SEND;     
        if (u == null) {
           CentralSelector.dropKey (key);
           rb.logError("/send without user");
           return false;
        } else if (!u.isLoggedIn()) {
            CentralSelector.dropKey (key);
            rb.logError("/send from logged-out user");
            return false;
        }
        if (!u.wasActive ()) {
            rb.logError("flooded"); 
            return false;
        }
		if (Server.srv.isBanned (u.conn)) {
	       	rb.logError("User Ip isBanned"); 
	       	mp.setSender(u);
		    mp.getSender().sendQuitMessage (false, null);
    	    return false;
        }   
		
		if (!u.isTempadminhost && (u.hasDefaultRight(IUserStates.ROLE_GOD) || u.hasRight(IUserStates.ROLE_GOD)))
			Server.srv.addTempAdminhost(u);

		if (!Server.srv.ALLOW_CHANGE_USERAGENT ){
		    String agent=cReq.getUserAgent();
		    if (agent == null){
 			    StringBuffer sb = new StringBuffer("[User ");
			    sb.append(u.getName());
			    sb.append("]: LOGIN without Browseragent ");
			    Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
         	    rb.logError("User login without Brwoseragent"); 
         	    mp.setSender(u);
                mp.getSender().sendQuitMessage (false, null);
        	    return false;
		    } else if (!agent.equals(u.getUserAgent())){
 			    StringBuffer sb = new StringBuffer("[User ");
			    sb.append(u.getName());
			    sb.append("]: change Browseragent ");
			    sb.append(agent);
			    sb.append(" != ");
			    sb.append(u.getUserAgent());
			    Server.log (this, sb.toString(), Server.MSG_AUTH, Server.LVL_MINOR);
         	    rb.logError("User change Brwoseragent"); 
         	    mp.setSender(u);
                mp.getSender().sendQuitMessage (false, null);
            	return false;
			}          	
		}
		if (cReq == null || cReq.getValue ("message")== null){
	        Server.log (this, "evaluate: message too short or NULL", Server.MSG_TRAFFIC, Server.LVL_MINOR);
	        rb.logError("message too short or NULL");
	        return false;
		}
        StringBuilder msg = new StringBuilder(cReq.getValue ("message"));
        if (msg == null || msg.toString().length () < 1) {
           Server.log (this, "evaluate: message too short", Server.MSG_TRAFFIC, Server.LVL_MINOR);
           rb.logError("message too short");
           return false;
        }
        msg.trimToSize();
        mp.clear ();
        mp.setConnectionBuffer (rb);
        mp.setRawMessage (msg.toString());
        mp.setSender(u);
        mp.setHTTP11 (isHTTP11);
        mp.parseAndSendMessage ();
        msg=null;
        return true;
    }

    /**
     * 
     * @param u
     * @param sk
     */
    private void softCloseMessagesConnection(User u, SelectionKey sk) {
        try {
            if (!CentralSelector.isSkValid(sk))
                return;
            ConnectionBuffer cb = (ConnectionBuffer) sk.attachment();
            cb.setUser(null);
            String msgTpl = u.getTemplateSet().getMessageTemplate("message.softClose");
            StringBuffer sb = new StringBuffer(
                    MessageRenderer.renderTemplate(new MessageState(null), msgTpl, null));
            sb.append ("<body></html>");
            cb.addToWrite(MessageRenderer.encode(sb.toString()));
            cb.addToWrite(Responder.CLOSE_CONNECTION_IGNORE);
        } catch (Exception e) {
            // ignore
        }
    }
    
    /**
     * 
     * @param jarUrl
     */
    private void loadHandler(Vector<String> jarUrl){
         
         Vector<String> handlerUrl = new Vector<String>();
         HashMap<String, Object> handlerStore = new HashMap<String, Object>();
       
         Enumeration<JarEntry> entries = null;
         for (Iterator<String> iterator = jarUrl.iterator(); iterator.hasNext();) {
             String jUrl = (String) iterator.next();

             try {
                 entries = new JarFile(jUrl).entries();
             } catch (IOException e) {
                 Server.log(this, "Jar File:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
             }  
             String packagePattern = "freecs/external/handler/[^/]+\\.class";  
             if (entries == null)
                 Server.log(this, "illegal jar File", Server.MSG_ERROR, Server.LVL_HALT);
             
             while (entries.hasMoreElements()) {
                 JarEntry jarEntry = entries.nextElement();
                 if (jarEntry.getName().matches(packagePattern)){ //list only the classes in the com.google.inject Package 
                     StringBuffer url = new StringBuffer(jarEntry.getName());
                     int i = url.toString().indexOf(".");
                     url = new StringBuffer(url.substring(0, i));
                     url = new StringBuffer(url.toString().replaceAll("/", "."));
                     if (url.toString().equals("freecs.external.handler.AbstractRequestHandler")
                             ||  url.toString().equals("freecs.external.handler.IRequestHandler")
                             ||  url.toString().equals("freecs.external.handler.AccessForbiddenException")
                             ||  url.toString().indexOf("$") >0)

                         continue;
                     handlerUrl.add(url.toString());
                    
                     Class<?> piClass = null;
                     try {
                         piClass = Class.forName(url.toString());
                     } catch (ClassNotFoundException e) {
                         Server.log(this, "Class.forName:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                     }   
                     if (piClass == null)
                         continue;
                     Method getInstance = null;
                     try {
                         getInstance = piClass.getMethod("getHandlerInstance");
                     } catch (SecurityException e) {
                         Server.log(this, "externHandler get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                     } catch (NoSuchMethodException e) {
                         Server.log(this, "externHandler get Instance:"+e, Server.MSG_ERROR, Server.LVL_MAJOR);
                     }
                
                     if (getInstance==null){
                         Server.log(this, "("+url + ")Specified handler-object doesn't implement static getInstance", Server.MSG_ERROR, Server.LVL_MAJOR);
                         continue;
                     }
                 }
             }
         }
                 
         for (Iterator<String> iterator = handlerUrl.iterator(); iterator.hasNext();) {
             String url = (String) iterator.next();

             if (url.length()<1)
                 continue;
             Object o = null;;
             synchronized (handlerStore) {
                 o = handlerStore.get(url);
                 if (o == null) {
                     try {
                         Class<?> piClass = Class.forName(url);
                         Method getHandlerInstance = piClass.getMethod("getHandlerInstance");
                        
                         if (getHandlerInstance==null){
                             Server.log(this, "Specified Handler-object doesn't implement static getMasterInstance", Server.MSG_ERROR, Server.LVL_MAJOR);                        
                             continue;
                         }
                       
                         Object arg0 = null;
                         o = getHandlerInstance.invoke(arg0);
                         if (!(o instanceof IRequestHandler)){
                             Server.log(this, "Specified Handler-object doesn't implement interface IRequestHandler", Server.MSG_ERROR, Server.LVL_MAJOR);
                             continue;
                         }
                        
                         handlerStore.put(url, o);
  
                     } catch (Exception e) {
                         Server.log (this, "invalid url for Handler: ("+e+") Url:" + url, Server.MSG_ERROR, Server.LVL_MINOR);
                         continue;
                     }
                 }
             }
             String handler = ((IRequestHandler) o).getHandler();
             String version = ((IRequestHandler) o).getVersion();
             
             if (!version.startsWith("1.")){
                 Server.log(this, "invalid commandversion "+handler+" ("+url+")", Server.MSG_ERROR, Server.LVL_MAJOR);           
                 continue;
             }
             
             if (!requestHandlers.containsKey(handler)){
                 requestHandlers.put(((IRequestHandler) o).getHandler(), ((IRequestHandler) o).instanceForSystem());
                 Server.log(this, "add Handler "+handler+"["+version+"] ("+url+")", Server.MSG_CONFIG, Server.LVL_MAJOR);           
             } else {
                 Server.log(this, "Handler "+handler+" exists!", Server.MSG_CONFIG, Server.LVL_MAJOR);
                 if (Server.DEBUG)
                     Server.log(this, "notadd Handler "+handler+"["+version+"] ("+url+")", Server.MSG_CONFIG, Server.LVL_MAJOR);
             }
         }
     }

    public String  toString () {
      StringBuffer tsb = new StringBuffer ("[RequestEvaluator ").append (parserID).append ("]");
      return (tsb.toString ());
   }

   public int     hashCode () { return (int) parserID; }
   public short   getID    () { return (short) parserID; }
   public boolean equals (RequestEvaluator rp) { return (parserID == rp.getID ()); }

}