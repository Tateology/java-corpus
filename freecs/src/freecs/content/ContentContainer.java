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
 * The ContentContainer is used for delivering the entry-page, error-page, ...
 */
package freecs.content;

import freecs.*;
import freecs.core.User;
import freecs.interfaces.*;
import freecs.layout.*;
import freecs.util.HttpDateParser;

import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContentContainer implements IResponseHeaders, IContainer {
   private ByteBuffer   buf;
   private TemplateSet  ts;
   private String 		tName;
   private Template		tpl;
   private String		cookie,
						contentType="text/html";
   
   private String       eTag;
   private boolean      notModified=false;

   private volatile boolean	chunkedHdr, 
   						nocache=false, 
   						nostore=false,
						keepAlive = false, 
						isMessages = false,
						isRedirect, 
						isHTTP11=true;

   private short 		resCode = 200;
	
   public ContentContainer () {
       if (Server.TRACE_CREATE_AND_FINALIZE)
           Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

	/**
	 * wraps the cntnt param into a fully http-response with the wanted headerfields
	 * @param cntnt
	 */
   public void wrap (String cntnt, String CookieDomain) {
       wrap(cntnt, null, CookieDomain);
   }
   
   public void wrap (String cntnt, String eTag, String CookieDomain) {
	   StringBuffer sb = new StringBuffer (isHTTP11 ? "HTTP/1.1" : "HTTP/1.0");
	   switch (resCode) {
	   	case OK_CODE:
	   		sb.append (OK_HDR);
	   		break;
	   	case REDIRECT_CODE:
	   		setRedirectTo (cntnt, CookieDomain);
	   		break;
        case NOCONTENT_CODE:
            sb.append (NOCONTENT_HDR);
            prepareForSending (CharBuffer.wrap(sb.toString()));
            return;
        case AUTHENTICATE_CODE:
        	sb.append (AUTHENTICATE_HDR);
        	contentType="text/html";
        	break;
        case NOTFOUND_CODE:
            sb.append (NOTFOUND_HDR);
            contentType="text/html";
            break;
        }
		sb.append ("Content-Type: ");
		sb.append (contentType);
		sb.append ("; charset=");
		sb.append (Server.srv.DEFAULT_CHARSET);
		if (nocache) {
			sb.append ("\r\nPragma: no-cache\r\nCache-Control: no-cache");
            sb.append ("\r\nExpires: Thu, 01 Dec 1994 16:00:00 GMT");
		} 
        if (nostore) {
			sb.append ("\r\nCache-Control: no-store");
		}
        if (eTag != null) {
            sb.append ("\r\nETag: \"").append(eTag).append("\"");
            //Server.log (this, "sending eTag: " + eTag, Server.MSG_STATE, Server.LVL_MAJOR);
        }

    	sb = appendCookie (sb, CookieDomain);

		if (!isHTTP11 || !keepAlive || isMessages) {
			sb.append ("\r\nConnection: close\r\nProxy-Connection: close");
		} else {
			sb.append ("\r\nConnection: Keep-Alive\r\nProxy-Connection: Keep-Alive");
			if (!chunkedHdr) {
				sb.append("\r\nContent-Length: ");
				sb.append (cntnt.length ());
			}
		}
		if (chunkedHdr) {
			sb.append ("\r\nTransfer-Encoding: chunked\r\n\r\n");
			sb.append (Integer.toHexString (cntnt.length ()));
			sb.append ("\r\n");
			sb.append (cntnt);
			sb.append ("\r\n");
			sb.trimToSize();
			prepareForSending(CharBuffer.wrap (sb.toString()));
			return;
		}
		sb.append ("\r\n\r\n");
        if ("iso-8859-1".equals(Server.srv.DEFAULT_CHARSET)) {
            sb.append(cntnt);
            sb.trimToSize();
            prepareForSending(CharBuffer.wrap(sb.toString()));
        } else {
        	sb.trimToSize();
            CharBuffer hdrChar = CharBuffer.wrap(sb.toString());
    		prepareForSending(hdrChar, CharBuffer.wrap (cntnt));
        }
   }
   
   /**
    * sets the HTTP-Response-Code
    * @param code
    */
   public void setResCode (short code) {
		resCode=code;
   }

	/**
	 * append the cookie-header-field to the given StringBuffer
	 * @param sb the stringbuffer to append the cookie-header-field
	 * @return the stringbuffer with the cookie-header-field appended
	 */
   public StringBuffer appendCookie (StringBuffer sb, String CookieDomain) {
       if (cookie == null || CookieDomain == null) return sb;
	   sb.append ("\r\n");
	   sb.append ("Set-Cookie: FreeCSSession=");
	   sb.append (cookie);
	   sb.append ("; path=/;");
	   if (Server.srv.COOKIE_DOMAIN != null) {
		   if (Server.DEBUG)
	           Server.log("[ContenCointainer]","append FreecsSession = "+CookieDomain, Server.MSG_TRAFFIC, Server.LVL_VERY_VERBOSE);
	       sb.append (" Domain=");
	       sb.append (CookieDomain);
	   }
	   return sb;
   }

   /**
	* causes this content-container to use a specific template-set
	* @param ts the template-set to use
	*/
    public void useTemplateSet (TemplateSet ts) {
        this.ts = ts;
    }
   
    public boolean canUseTemplateset(TemplateSet t) {
       if (Server.srv.DEFAULT_TEMPLATESET == null || t == null)
		   return true;
	   StringBuffer defaultTs =  new StringBuffer (Server.srv.DEFAULT_TEMPLATESET);		
       if (defaultTs !=null){
           StringTokenizer st = new StringTokenizer(defaultTs.toString(),",");
           while (st.hasMoreTokens()) {
               StringBuilder templateName = new StringBuilder(st.nextToken());
               if (templateName.toString().equals(t.getName()))
                       return true;
           }
       }	
       if (Server.DEBUG) {
    	   StringBuffer scn = new StringBuffer ();
	       scn.append (" can not use Template ");
	       scn.append (t.getName());
	       Server.log(this,scn.toString(), Server.MSG_TRAFFIC, Server.LVL_MINOR);   		
       }
	   return false;
	}
    
    public boolean isMobileBrowser(String BrowserAgent){   
        if (BrowserAgent == null)
            return false;
        StringBuilder c_input = new StringBuilder(Server.srv.MOBILE_BROWSER_REGEX);
        c_input.trimToSize();
        Pattern p = Pattern.compile(c_input.toString().toLowerCase());
        Matcher m = p.matcher(BrowserAgent.toLowerCase());
        if (m.find()){
            Server.log(this, "found Mobile Browser ["+m.group() +"] ("+BrowserAgent+")", Server.MSG_TRAFFIC, Server.LVL_VERBOSE);
            return true;
        } else return false;
    }

    public String checkTName(User u, String name){
       if (Server.srv.getProperty("costum.userAgent") == null)
           return name;
       
       String browseragent = name;
       if(u != null && u.getUserAgent()!= null) {
          browseragent = u.getUserAgent(); 
       } else return name;

       boolean defaultFile = true;
       String ta [] = Server.srv.getProperty("costum.userAgent").split(",");
       Vector<String> tempAgent = new Vector<String>();
       for (int i = 0; i < ta.length; i++) {
           tempAgent.add(ta[i].trim().toLowerCase());
       }
       int fn = 0;
       int found = 0;
       for (Enumeration<String> e = tempAgent.elements(); e.hasMoreElements();){
           StringBuilder customAgent = new StringBuilder((String) e.nextElement());
           fn++;
           if (browseragent.toLowerCase().indexOf(customAgent.toString().toLowerCase()) >= 0){
               defaultFile = false;
               Server.log(this, "Found Browser '"+customAgent+"'("+ u.getUserAgent()+")", Server.MSG_STATE, Server.LVL_VERBOSE);
               found = fn;
               continue;
           } else {
               if (u!=null && u.getUserAgent()!=null)
                   Server.log(this, "Browser '"+customAgent+"' not found("+ u.getUserAgent()+")", Server.MSG_STATE, Server.LVL_VERBOSE);
           }
           customAgent = null;
       }
       StringBuilder fName = null;
       if (defaultFile){
           return name;
       } else {
           fName = new StringBuilder(name).append("_custom").append(found);
           if (ts == null || u == null | u.getName() == null)
        	   return name;
           tpl = ts.getTemplate (fName.toString());
           if (tpl == null){
               Server.log(this, u.getTemplateSet()+" File "+fName+ " not found", Server.MSG_STATE, Server.LVL_VERBOSE);
               fName = new StringBuilder(name).append("_custom");
               tpl = ts.getTemplate (fName.toString());
           }
           if (tpl == null) {
               Server.log(this, u.getTemplateSet()+" File "+fName+ " not found- loading default input File", Server.MSG_ERROR, Server.LVL_MAJOR);
               return name;
           }
       }
       return fName.toString();
   }

	/**
	 * set the template to be rendered for this content-container
	 * @param tName the name of the template
	 */
   public void setTemplate (String tName) {
      this.tName = tName;
   }

	/**
	 * renders the template and wraps it to a full httpresponse
	 */
   public void renderTemplate (IRequest req) {
      if (tpl == null) {
         if (this.ts == null) 
             ts = Server.srv.templatemanager.getTemplateSet ("default");
         tpl = ts.getTemplate (tName);
         if (tpl == null) 
             tpl = ts.getTemplate ("not_found");
      }
      if (tpl.isRedirect ()) {
          this.setRedirectTo(tpl.getDestination() , req.getCookieDomain());
          return;
      }
      if (!nocache && !nostore && 
              !tpl.hasToBeRendered(req.getProperty("if-none-match"), 
              HttpDateParser.parseDate(req.getProperty("if-modified-since")))) {
          StringBuffer sb = new StringBuffer (isHTTP11 ? "HTTP/1.1" : "HTTP/1.0");
          sb.append (IResponseHeaders.NOT_MODIFIED);
          sb.trimToSize();
          prepareForSending(CharBuffer.wrap (sb.toString()));
          return;
      }
      String cntnt = tpl.render (req);
      if (cntnt==null || cntnt.length () < 1) {
         Server.log (this, "renderTemplate: rendered template has no content", Server.MSG_STATE, Server.LVL_MAJOR);
         resCode=NOCONTENT_CODE;
         StringBuffer sb = new StringBuffer ();
         sb.append ("<html><body><b>The requested page could not be displayed!<br><br>Reason:</b> ");
         if (tpl == null) {
             sb.append ("No template given");
         } else {
             sb.append ("Template '");
             sb.append (tpl.getName());
             sb.append ("' has not been found on this server.");
         }
         sb.append ("</body></html>");
         wrap (sb.toString(), req.getCookieDomain());
         return;
      }
      nocache=tpl.notCacheable();
//      if (nocache)
//          Server.log (this, "not cacheable", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
      wrap (cntnt, tpl.getEtag(), req.getCookieDomain());
   }

	/**
	 * causes the response to be a chunked response
	 * the calling method has to care, that the other chunks are sent correctly
	 */
   public void useChunkedHeader () {
      chunkedHdr = true;
      keepAlive = true;
   }

	/**
	 * set/unset the keep-alive header field
	 * @param b
	 */
   public void setKeepAlive (boolean b) {
      keepAlive = b;
   }

	/**
	 * mark this http-response as http11(true)/http10(false)
	 * @param b
	 */
   public void setHTTP11 (boolean b) {
      isHTTP11 = b;
      keepAlive = true;
   }

	/**
	 * check if this response is a HTTP11 response
	 * @return boolean true if response is a HTTP11 response, false if not
	 */
   public boolean isHttp11 () {
      return isHTTP11;
   }

	/**
	 * set the cookievalue to append to the response-header-fields
	 * @param cookie
	 */
   public void setCookie (String cookie) {
      this.cookie = cookie;
   }

	/**
	 * return the bytebuffer which is ready to send
	 */
   public ByteBuffer getByteBuffer () {
      return buf;
   }

   /**
    * return the content type setting
    */
   public String getContentType() {
      return contentType;
   }

   /**
    * set the content type setting
    */
   public void setContentType(String contentType) {
      this.contentType = contentType;
   }
   
	/**
	 * prepares the response for sending
	 * if a template is set, it will be rendered
	 * if no charbuffer is present, even after rendering the template, 
	 * there is nothing to send and prepareForSending will just return
	 */
   public void prepareForSending (CharBuffer cb) {
       if (cb == null || cb.length() < 1)
           return;
       try {
           buf = Charset.forName ("iso-8859-1").newEncoder().encode(cb);
           return;
       } catch (CharacterCodingException cce) {
           Server.debug (this, "prepareForSending: ", cce, Server.MSG_ERROR, Server.LVL_MINOR);
       }
   }
   
   public void prepareForSending (CharBuffer hdr, CharBuffer cntnt) {
       if (hdr == null || hdr.capacity() < 1)
           return;
       try {
           ByteBuffer hdrBytes = Charset.forName("iso-8859-1").newEncoder().encode(hdr);
           ByteBuffer cntntBytes = Charset.forName (Server.srv.DEFAULT_CHARSET).newEncoder().encode(cntnt);
           buf = ByteBuffer.allocate(hdrBytes.capacity() + cntntBytes.capacity());
           buf.put(hdrBytes);
           buf.put(cntntBytes);
           buf.flip();
       } catch (Exception e) {
           Server.debug (this, "Exception during prepareForSending(hdr/cntnt)", e, Server.MSG_ERROR, Server.LVL_MAJOR);
       }
   }


    public boolean prepareForSending (IRequest req) {
        if (tName != null || tpl != null) 
            renderTemplate (req);
        if (buf == null)
            return false;
        return true;
    }
    
    public void setContent (byte[] cntnt) throws Exception {
        StringBuffer sb = new StringBuffer(isHTTP11 ? "HTTP/1.1" : "HTTP/1.0");
        sb.append (OK_HDR);
        sb.append ("Content-Type: ");
        sb.append (contentType);
        if (!isHTTP11 || !keepAlive || isMessages) {
            sb.append ("\r\nConnection: close\r\nProxy-Connection: close");
        } else {
            sb.append ("\r\nConnection: Keep-Alive\r\nProxy-Connection: Keep-Alive");
            sb.append("\r\nContent-Length: ");
            sb.append (cntnt.length);
        }
        sb.append ("\r\n\r\n");
        sb.trimToSize();
        CharBuffer cb = CharBuffer.wrap (sb.toString());
        ByteBuffer tempBuffer;
        try {
            tempBuffer = Charset.forName ("iso-8859-1").newEncoder().encode(cb);
        } catch (CharacterCodingException cce) {
            Server.debug (this, "prepareForSending: ", cce, Server.MSG_ERROR, Server.LVL_MINOR);
            try {
                tempBuffer = ByteBuffer.wrap (cb.toString().getBytes(Server.srv.DEFAULT_CHARSET));
            } catch (Exception e) {
               Server.debug (this, "prepareForSending: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
               throw e;
            }
        }
        this.buf = ByteBuffer.allocate(tempBuffer.capacity() + cntnt.length);
        this.buf.put(tempBuffer.array());
        this.buf.put(cntnt);
        this.buf.flip();
    }

   public boolean hasContent () {
      return (buf != null);
   }

   public boolean closeSocket () {
      return (!isHTTP11 || !keepAlive) && !isMessages;
   }
   public void setIsMessages () {
	  this.isMessages = true;
   }
   public void setNoCache () {
	  nocache = true;
   }
   public void setNoStore () {
	  nostore = true;
   }

   /**
    * construct a HTTP-Redirect-Response
    * 
    * @param dest the destination to redirect to
    */
    public void setRedirectTo(String dest, String CookieDomain) {
        StringBuffer cntnt = new StringBuffer("<html><head><title>redirection</title><head><body>Redirected to <a href=\"");
        cntnt.append(dest);
        cntnt.append("\">");
        cntnt.append(dest);
        cntnt.append("</a>");
        cntnt.append("</body></html>");
        int len = cntnt.length();
        StringBuffer sb = new StringBuffer(isHTTP11 ? "HTTP/1.1" : "HTTP/1.0");
        sb.append(REDIRECT_HDR);
        sb.append(Server.srv.DEFAULT_CHARSET);
        sb.append("\r\nLocation: ");
        sb.append(dest);
        sb.append("\r\nContent-Length: ");
        sb.append(len);
        sb = appendCookie(sb, CookieDomain);
        sb.append("\r\n\r\n");
        if ("iso-8859-1".equals(Server.srv.DEFAULT_CHARSET)) {
            sb.append(cntnt);
            sb.trimToSize();
            prepareForSending(CharBuffer.wrap(sb.toString()));
        } else {
            CharBuffer hdrChar = CharBuffer.wrap(sb.toString());
            cntnt.trimToSize();
            prepareForSending(hdrChar, CharBuffer.wrap(cntnt));
        }
        isRedirect = true;
    }
  
    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}