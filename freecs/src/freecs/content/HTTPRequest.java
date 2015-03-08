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
package freecs.content;

import freecs.*;
import freecs.core.*;
import freecs.util.CookieGenerator;
import freecs.util.EntityDecoder;
import freecs.interfaces.*;

import java.util.Properties;
import java.util.Enumeration;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;

/**
 * implementation of a HTTP-Request
 * parses the http-header and stores the neccesary data
 * as properties which may be accessed by the getProperty-Method
 */
public class HTTPRequest implements IRequest {
	private final SelectionKey key;
    private String request;
	private Properties 			props;
    private byte method;
	private String 				action, cookie, userAgent, url, cookieDomain;
	private boolean 			isHTTP11, refererFound;
	private final ConnectionBuffer 	cb;
//	private Properties			cookies = null;

	private Connection			conn;

   public HTTPRequest (ByteBuffer buf, ConnectionBuffer cb) throws CharacterCodingException {
      this.cb = cb;
      this.key = cb.getKey ();
      try {
          Charset c = Charset.forName("iso-8859-1");
          CharsetDecoder ce = c.newDecoder();
          CharBuffer cbuf = ce.decode(buf);
          this.request = cbuf.toString();
          // this.request = Charset.forName ().newDecoder ().decode (buf).toString();
      } catch (MalformedInputException mie) {
          this.request = new String(buf.array());
      }
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public ConnectionBuffer getConnectionBuffer () {
      return cb;
   }

   /**
    * parses the HTTP request
    */
   public void parse () throws Exception {
       if (!CentralSelector.isSkValid(key)) {
           throw new Exception ("Key isn't valid anymore");
       }
      props = new Properties ();

      String parts[] = request.split ("\r\n\r\n");
      String hf[] = parts[0].split ("\r\n");

      String values[] = hf[0].split (" ");
      if ("GET".equals(values[0])) {
          method=METHOD_GET;
      } else if ("POST".equals(values[0])) {
          method=METHOD_POST;
      }
      action=parseAction(values[1]);
      isHTTP11 = values[2].equals ("HTTP/1.1") && Server.srv.USE_HTTP11;
      if (!isHTTP11 && parts.length > 1 && parts[1].substring (parts[1].length () - 2).equals ("\r\n")) {
         // it looks like http/1.0 has an extra \r\n after the values of a post-request (not included in content length)
         parts[1] = parts[1].substring (0, parts[1].length () - 2);
      }

      int pos = action.indexOf ("?");
      if (pos > -1) {
         String rest = action.substring (pos + 1);
         action = action.substring (0, pos);
         String prt[] = rest.split("&");
         for (int i = 0; i < prt.length; i++) {
             String keyval[] = prt[i].split ("=");
             if (keyval.length < 2) continue;
             keyval[0] = URLDecoder.decode(keyval[0], "UTF-8");
             try {
                keyval[1] = URLDecoder.decode(keyval[1].trim (), Server.srv.DEFAULT_CHARSET);
             } catch (IllegalArgumentException ie){
                 Server.log(this,   keyval[0]+"="+keyval[1]+" URLDecoder.decode :"+ie, Server.MSG_ERROR, Server.LVL_MAJOR);
                 continue;
             }
             StringBuffer tsb = new StringBuffer ("v_").append (keyval[0].trim ());
             props.setProperty (tsb.toString (), keyval[1]);
         }
      }
      refererFound = false;
      boolean isProxyConnection = false;
      String[] fwChain = null;
      String realIp = null;
      for (int i = 1; i < hf.length; i++) {
         int dp = hf[i].indexOf (":");
         if (dp == -1) continue;
         String key = hf[i].substring (0, dp).trim ().toLowerCase();
         String value = hf[i].substring (dp +1).trim ();
         if (key.equals ("host")) {
         	String hst = value.split (":")[0];
            InetAddress ia;
            try {
                ia = InetAddress.getByName (hst);
            } catch (UnknownHostException uhe) {
                Server.log (this, "Unable to lookup host specified in host-http-field a client claimed to get a resource from: '" + hst + "'", Server.MSG_ERROR, Server.LVL_MAJOR);
                throw uhe;
            }
            if (!"localhost".equalsIgnoreCase(hst) && Server.srv.STRICT_HOST_BINDING) {
            	boolean isServername = false;           	
                if (Server.srv.SERVER_NAME != null) {
                	StringBuffer tsb = new StringBuffer();
                	int c = 0;
                	for (Enumeration<String> e = Server.srv.SERVER_NAME.elements(); e.hasMoreElements(); ) {
            			String sv = (String) e.nextElement();
            			if (hst.equalsIgnoreCase(sv)){
            				StringBuffer sb =new StringBuffer(hst);
            				String port = Server.srv.props.getProperty("mappedPort");
            		        if (port==null)
            		            port = Server.srv.props.getProperty("port");
            		        if (!"80".equals(port)) {
            		    		sb.append(":");
            		    		sb.append(port);
            		        }
            			    url = sb.toString();
            			    if (Server.srv.COOKIE_DOMAIN == null){
                                Server.log("[HTTPRequest]","cookieDomain not configured", Server.MSG_CONFIG, Server.LVL_HALT);     
            			    }
            			    String cd[] = Server.srv.COOKIE_DOMAIN.toString().split(",");        
            			    if (cd[c].trim().toLowerCase() != null){
            			        cookieDomain= cd[c].trim().toLowerCase();
                                Server.log("[HTTPRequest]","cookieDomain = "+cookieDomain, Server.MSG_TRAFFIC, Server.LVL_VERY_VERBOSE);
                            } else {
                                Server.log("[HTTPRequest]","cookieDomain not configured", Server.MSG_ERROR, Server.LVL_MAJOR);	
                            }
                            isServername = true;
                            break;
            			}
                	    if (!hst.equalsIgnoreCase(sv)) {
                            tsb.append ("Recieved request specifying a different Host than specifiec inside configuration of this server. Recieved: ");
                            tsb.append (hst);
                            tsb.append (" Configured: ");
                            tsb.append (sv);
                        }
                	    c++;               	    
                	}
                	if (!isServername)
                		throw new Exception (tsb.toString());
                }
                if (!ia.equals (Server.srv.lh)) {               	
                	boolean isServer = false;
                	for (Enumeration<String> le = Server.srv.SERVER_NAME.elements(); le.hasMoreElements(); ) {
            			String sv = (String) le.nextElement();
            			if (hst.equalsIgnoreCase(sv)) {
            				isServer=true;
            				break;
            			}
                	    
                	}
                	if (!isServer){
                		StringBuffer tsb = new StringBuffer ("Recieved request specifying a host, which doesn't resolve to the host of this chat-server. Recieved: ");
                        tsb.append (hst);
                        throw new Exception (tsb.toString ());
                	}
                }
                if (Server.srv.COOKIE_DOMAIN != null) {
                	boolean isCookieDomain =false;
                	StringBuffer tsb = new StringBuffer();
                	String cd[] = Server.srv.COOKIE_DOMAIN.toString().split(",");
                    for (int co = 0; co < cd.length; co++) {
                         String c =(cd[co].trim().toLowerCase());           			     
            			 if (hst.endsWith(c)){
            			     isCookieDomain =true;
            			     break;
            			 }
            			 if (!hst.endsWith (c)){
            				 tsb.append("Wrong adress used: " + hst + " instead of something ending with " + c);
            			 }
                	}
                    
                	if (!isCookieDomain)
                	    throw new Exception (tsb.toString());
                	
                }
            }
        } else if (!Server.srv.ALLOW_EXTERNAL && key.equals ("referer")) {       	
        	value = value.substring(7);
        	int pos1=value.indexOf (":");
        	int pos2= value.indexOf ("/");
     	    if (pos1 > 0 && pos1<pos2)
    		    pos2 = pos1;
    	    if (pos == -1 && pos1>0)
    	        pos2 = pos1;
        	if (pos2 > 0)
               value = value.substring (0,pos2);
               	
        	for (Enumeration<InetAddress> e = Server.srv.allowedLoginHosts.elements (); e.hasMoreElements (); ) {
      	        InetAddress ia = null;
      	    	try {
      	    	    ia = InetAddress.getByName (value.toString());
                  } catch (UnknownHostException uhe) {
						Server.log("[HTTPRequest]", "UnknownHostException: "+uhe, Server.MSG_ERROR, Server.LVL_MAJOR);
			            Server.log("[HTTPRequest]", "Referer "+ value + " not found " + "Url: " + url, Server.MSG_ERROR, Server.LVL_MAJOR);
						break;
                  }
                  if (ia!=null)
                      if (((InetAddress) e.nextElement ()).equals (ia)) {
                          refererFound = true;
                          break;
                      }
      	    }
        	if (!refererFound)
                Server.log("Referer ",value +" not found", Server.MSG_TRAFFIC, Server.LVL_VERBOSE);
         } else if (key.equals ("cookie")) {
            String cookiePair[] = value.split (";");
            for (int j = 0; j < cookiePair.length; j++) {
                String cp[] = cookiePair[j].trim().split ("=");
                if (cp.length < 2) 
                    continue;
                if (!cookiePair[j].trim ().startsWith("FreeCSSession")) {
                    props.put("c_" + cp[0], cp[1]);
                    continue;
                } 
               cookie = cp[1].trim ();
               if (!CookieGenerator.checkValidity(cookie)) {
               	  cookie = null;
               }
            }
         } else if (key.equalsIgnoreCase ("x-forwarded-for")) {
         	isProxyConnection = true;
         	fwChain = value.split(",");
		 } else if (key.equalsIgnoreCase ("via")) {
		 	isProxyConnection = true;
		 } else if (key.equalsIgnoreCase ("client-ip")) {
		 	isProxyConnection = true;
		 	realIp = value;
		 } else if (key.equals("user-agent")) {
		 	userAgent = value;
         } else {
			props.setProperty (key, value);
         }
      }
      conn = new Connection (this.key, fwChain, !isProxyConnection);
      if (realIp != null) try {
      	 InetAddress realAddress = InetAddress.getByName(realIp);
      	 conn.clientAddress = realAddress;
      	 conn.clientIp = realIp;
      } catch (UnknownHostException uhe) {
      	 Server.debug (this, "parse: Headerfield client-IP contains an UnknownHost", uhe, Server.MSG_STATE, Server.LVL_MINOR);
      }
      if (parts.length < 2 ) return;
      hf = parts[1].split ("&");
      for (int i = 0; i < hf.length; i++) {
         String pair[] = hf[i].split ("=");
         if (pair.length < 2) continue;
         pair[0] = EntityDecoder.entityToChar (pair[0]);
         if (pair[0].equalsIgnoreCase ("message"))
            pair[1] = EntityDecoder.entityToHtml (pair[1].trim ());
         else
            pair[1] = EntityDecoder.entityToChar (pair[1].trim ());
         StringBuffer tsb = new StringBuffer ("v_").append (pair[0]);
         props.setProperty (tsb.toString (), pair[1]);
      }
   }

   /**
    * get properties of this request
    * @param key the name of this property
    * @return the value of the property identified by this key
    */
   public String getProperty (String key) {
      return props.getProperty (key);
   }
   
   public String getValue (String key) {
       return getProperty ("v_" + key);
   }
   
   private String parseAction (String rawAction) {
       if (rawAction.length() < 1
               || !rawAction.startsWith("http://"))
           return rawAction;
       int idx = rawAction.indexOf("/", 7);
       return rawAction.substring(idx);
   }
   
   public String getCookie (String key) {
       return getProperty ("c_");
   }
   
   public String getUrl(){
	   return url;
   }
   
   public String getCookieDomain(){
       Server.log("[HTTPRequest]","getCookieDomain = "+cookieDomain, Server.MSG_TRAFFIC, Server.LVL_VERY_VERBOSE);
	   return cookieDomain;
   }
   
   public byte getMethod () {
      return method;
   }
   public String getAction () {
      return action;
   }
   public boolean isHTTP11 () {
      return isHTTP11;
   }
   public String getCookie () {
      return cookie;
   }
   public String getUserAgent() {
   	  return userAgent;
   }
   public String getProtokol () {
      return isHTTP11 ? "HTTP/1.1" : "HTTP/1.0";
   }

	public Connection getConnectionObject() {
		return conn;
	}
	

   public SelectionKey getKey () {
      return key;
   }

   public boolean foundReferer(){
       return refererFound;
   }

   public String toString () {
       return "HTTP: " + action;
   }
   
/*    public String toString () {
        StringBuffer sb = new StringBuffer("[HTTPRequest: ");
        sb.append (method);
        sb.append (" ");
        sb.append (action);
        sb.append (" (");
        sb.append (cookie);
        sb.append (")@");
        if (conn != null) 
            sb.append (conn.toString());
        else
            sb.append ("unspeciefied");
        sb.append ("]");
        return sb.toString();
    } */

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}