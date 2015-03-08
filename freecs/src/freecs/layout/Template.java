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
package freecs.layout;

import freecs.Server;
import freecs.commands.AbstractCommand;
import freecs.commands.CommandSet;
import freecs.content.MessageState;
import freecs.core.*;
import freecs.util.FileMonitor;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IReloadable;
import freecs.interfaces.IRequest;

import java.io.*;
import java.util.*;

public class Template implements IReloadable {
    private static final int DEPENDS_ON_NOTHING         = 0;
    private static final int DEPENDS_ON_SERVER_CONFIG   = 1;
    private static final int DEPENDS_ON_USER_COUNT      = 2;
    private static final int DEPENDS_ON_GROUP_COUNT     = 4;
    private static final int DEPENDS_ON_GROUP_LIST      = 8;
    private static final int DEPENDS_ON_SESSION         =16;
    private int dependsOn=DEPENDS_ON_NOTHING;

    private volatile String eTag=null;
    private volatile long modifiedSince=0;
    
    private volatile String[] parts;
    private String tsName;
    private String name;
    private boolean isRedirect;
    private String destination;
    private TemplateSet ts;

    private File template;
    private long lastModified;
    private boolean tplPresent;

    public String getDestination () {
        return destination;
    }

   public boolean isRedirect () {
      return isRedirect;
   }

   public String getTsName () {
      return tsName;
   }

   public boolean hasToBeRendered (String eTag, long modifiedSince) {
       if (eTag==null && modifiedSince==-1)
           return true;
       if (eTag != null) {
           this.eTag = this.generateETag();
           if ((this.dependsOn & Template.DEPENDS_ON_SESSION)!=0
               || !eTag.equals(this.eTag)) // FIXME: if-not-match may transfere more than one etag
               return true;
       } else if (this.modifiedSince>modifiedSince)
           return true;
       return false;
   }

   public String render (IRequest req) {
/*      StringBuffer tsb = new StringBuffer ("rendering Template ").append (name).append (" of templateset ").append (tsName);
      Server.log (this, tsb.toString (), Server.MSG_STATE, Server.LVL_VERY_VERBOSE); */
      StringBuffer retVal = new StringBuffer ();
      for (int i = 0; i < parts.length; i++) {
         String part = parts[i];
         if (part.indexOf("©")>0 ){
        	 part = part.replaceFirst("©", "© FreeCs 2003-2009");
         } else if (part.indexOf("&copy;")>0 ){
        	 part = part.replaceFirst("&copy;", "&copy; FreeCs 2003-2009");
         } 
         if (!part.startsWith ("#")) {
            retVal.append (part);
            continue;
         }
         if (part.regionMatches 
                 (true, 0, "#active_users", 0, "#active_users".length())) {
            retVal.append (UserManager.mgr.getActiveUserCount ());
         } else if (part.regionMatches 
                 (true, 0, "#active_user_list", 0, "#active_user_list".length())) {
             retVal.append(generateUserList());
         } else if (part.regionMatches 
                 (true, 0, "#users_in_group", 0, "#users_in_group".length())) {
             retVal.append (generateUserList(req));
         } else if (part.regionMatches 
                 (true, 0, "#open_groups", 0, "#open_groups".length())) {
         	retVal.append (GroupManager.mgr.openGroupsCount()); 
         } else if (part.regionMatches 
                 (true, 0, "#bgcolor", 0, "#bgcolor".length())) {
        	 retVal.append (getBgcolor(req));
         } else if (part.regionMatches 
                     (true, 0, "#userstate_fonly", 0, "#userstate_fonly".length())) {
             retVal.append (fonly(req));               
         } else if (part.regionMatches 
                 (true, 0, "#selve", 0, "#selve".length())) {
        	retVal.append ("http://");
          	if (req.getUrl() != null){
          		retVal.append (req.getUrl());   
          	}else  retVal.append (Server.srv.getUrl ());
         } else if (part.regionMatches 
                 (true, 0, "#token", 0, "#token".length())) {
			StringBuffer c = new StringBuffer ();
			while (c.length () < 16) {
			   char x = (char) Math.ceil(Math.random() * 34);
			   if (x < 10) x = (char) (x + 48);
			   else        x = (char) (x + 87);
			   c.append(x);
			}
			retVal.append (c);
			Server.srv.addToken(c.toString(), req.getCookie());
         } else if (part.regionMatches 
                 (true, 0, "#config.", 0, "#config.".length())) {
             retVal.append(Server.srv.props.getProperty(part.substring(8).trim()));
         } else if (part.regionMatches 
                 (true, 0, "#custom.", 0, "#custom.".length())) {
            
             StringBuilder name = new StringBuilder( part.substring(8).trim());
             StringBuilder value = new StringBuilder(getUpdatePropertie(req, name.toString()));
             retVal.append(value.toString());
         } 
      }
     // String[] arr = new String[2];
      return retVal.toString();
   }

   private String generateETag() {
       if ((this.dependsOn & Template.DEPENDS_ON_SESSION) != 0)
           return null;
       if (modifiedSince < this.lastModified)
           modifiedSince = this.lastModified;
       StringBuffer sb = new StringBuffer();
       sb.append (Long.toHexString(this.lastModified/500));
       if ((this.dependsOn & Template.DEPENDS_ON_SERVER_CONFIG) != 0) {
           sb.append ("pc").append(Long.toHexString(Server.srv.lastModified()/500));
           if (modifiedSince < Server.srv.lastModified())
               modifiedSince = Server.srv.lastModified();
       }
       if ((this.dependsOn & Template.DEPENDS_ON_USER_COUNT) != 0) {
           sb.append ("puc").append(Long.toHexString(UserManager.mgr.lastModified()/500));
           if (modifiedSince < UserManager.mgr.lastModified())
               modifiedSince = UserManager.mgr.lastModified();
       }
       if ((this.dependsOn & Template.DEPENDS_ON_GROUP_COUNT) != 0) {
           sb.append ("pgc").append(Long.toHexString(GroupManager.mgr.lastModified()/500));
           if (modifiedSince < GroupManager.mgr.lastModified())
               modifiedSince = GroupManager.mgr.lastModified();
       }
       if ((this.dependsOn & Template.DEPENDS_ON_GROUP_LIST) != 0) {
           sb.append ("pgl").append(Long.toHexString(GroupManager.mgr.groupListLastModified()/500));
           if (modifiedSince < GroupManager.mgr.groupListLastModified())
               modifiedSince = GroupManager.mgr.groupListLastModified();
       }
       return sb.toString();
   }
   
   public String getName () {
      return name;
   }

	public Template(File tpl, TemplateSet ts) throws IOException {
		this.tsName = ts.getName();
		this.ts = ts;
		template = tpl;
		name = tpl.getName ();
		int pos = name.indexOf (".");
		if (pos != -1)
	   	name = name.substring (0, pos);
      	lastModified = tpl.lastModified ();
      	parts = parseFile(tpl);
      	tplPresent=true;
      	FileMonitor.getFileMonitor ().addReloadable (this);
	}

   public boolean isValide () {
      return ((parts != null && parts.length > 0) ||
              (destination != null && destination.length() > 1 && isRedirect == true));
   }

   private String[] parseFile(File tpl) throws IOException {
      if (!tpl.canRead()) {
         StringBuffer tsb = new StringBuffer ("Unable to read template '").append (tpl.getName()).append ("'");
         throw new IOException(tsb.toString ());
      }
      this.dependsOn = Template.DEPENDS_ON_NOTHING;

      Vector<String> t = new Vector<String> ();
      FileReader fr = new FileReader(tpl);
      char fcnt[] = new char[(int) tpl.length()];
      int read = fr.read(fcnt);
      if (read<1) return null;
      String raw = String.copyValueOf(fcnt);
      if (raw.toLowerCase ().startsWith ("#redirect#")) {
         this.isRedirect = true;
         destination = raw.substring (raw.lastIndexOf("#") + 1).trim ();
         if (destination.toLowerCase ().startsWith ("$selve$")) {
            StringBuffer tsb = new StringBuffer ("http://").append (Server.srv.getUrl ());
            destination = tsb.toString ();
            this.dependsOn = this.dependsOn | Template.DEPENDS_ON_SERVER_CONFIG;
         } else if (!destination.toLowerCase ().startsWith ("http://")) {
            StringBuffer tsb = new StringBuffer ("http://").append (destination);
            destination = tsb.toString ();
         }
      }

      boolean lt=false, placeholder=false;
      StringTokenizer tok = new StringTokenizer (raw, "<%>", true);
      StringBuffer currPlaceHolder = new StringBuffer(),
                   currPart = new StringBuffer();

      while (tok.hasMoreElements()) {
          String curr = tok.nextToken();
          if (curr.equals("<")) {
              if (lt)
                  currPart.append("<");
              else
                  lt=true;
          } else if (lt && curr.equals("%")) {
              placeholder=true;
              currPlaceHolder.append("#");
              while (placeholder) {
                  curr=tok.nextToken();
                  if (curr.equals("%")) {
                      curr = tok.nextToken();
                      if (curr.equals(">")) {
                          placeholder=false;
                          lt=false;
                      } else {
                          currPlaceHolder.append("%");
                          currPlaceHolder.append(curr);
                      }
                  } else {
                      currPlaceHolder.append(curr.trim());
                  }
              }
              String cplh = currPlaceHolder.toString();
              lt=false;
              currPlaceHolder = new StringBuffer();
              if (cplh.equals("#active_users")) {
                  this.dependsOn = this.dependsOn 
                                   | Template.DEPENDS_ON_USER_COUNT;
              } else if (cplh.equals("#active_user_list")) {
                  this.dependsOn = this.dependsOn
                                   | Template.DEPENDS_ON_GROUP_LIST;
              } else if (cplh.startsWith("#users_in_group")
                         || cplh.equals("#token")) {
                  this.dependsOn = this.dependsOn 
                                   | Template.DEPENDS_ON_SESSION;
              } else if (cplh.equals("#open_groups")) {
                  this.dependsOn = this.dependsOn 
                                   | Template.DEPENDS_ON_GROUP_COUNT;
              } else if (cplh.equals("#selve")) {
                  this.dependsOn = this.dependsOn 
                                   | Template.DEPENDS_ON_SERVER_CONFIG;
              } else if (cplh.startsWith("#config")) {
              	  // any kind of <% #config.XX %> property
              	  this.dependsOn = this.dependsOn 
                               | Template.DEPENDS_ON_SERVER_CONFIG;
              }
              if (cplh.toLowerCase().equals("#version")) {
                  currPart.append(Server.getVersion());
              } else {
                  t.addElement(currPart.toString());
                  t.addElement(cplh);
                  currPart = new StringBuffer();
              }
          } else if (lt) {
              currPart.append("<");
              currPart.append(curr);
              lt = false;
          } else {
              currPart.append(curr);
          }
      }
      if (currPart.length()>0) {
          t.addElement(currPart.toString());
      }
      if (currPlaceHolder.length()>0) {
          t.addElement(currPlaceHolder.toString());
      }
      /*
      String parts[] = raw.split("<%");
      for (int i = 0; i < parts.length; i++) {
         String sub[] = parts[i].split ("%>");
         if (sub.length == 1) {
            t.addElement(sub[0]);
            continue;
         } if (sub.length > 2) {
            StringBuffer tsb = new StringBuffer ("error parsing template ");
            tsb.append (tsName).append ("/").append (tpl.getName ());
            Server.log(this, tsb.toString (), Server.MSG_ERROR, Server.LVL_MAJOR);
            return(null);
         }
         StringBuffer tsb = new StringBuffer ("#").append (sub[0].trim ().toLowerCase ());
         t.addElement(tsb.toString ());
         t.addElement(sub[1]);
      } */
      return((String[]) t.toArray(new String[0]));
   }

    public String generateUserList (IRequest req) {
        String strg = req.getValue("user");
        Group g = null;        
        if (strg==null)
            strg = req.getValue("group");
        else {
            User u = UserManager.mgr.getUserByName(strg);
            if (u==null)
                return "no such user: " + strg;
        }
        MessageState msgState = new MessageState(null);
        if (strg==null) {
            strg = req.getCookie();
            if (strg == null)
                return "no cookie to get a user for";
            User u = UserManager.mgr.getUserByCookie(strg);
            if (u == null)
                return "no user to generate userlist for";
            msgState.sender = u;
            g = u.getGroup();
        }
        if (g==null) {
            g = GroupManager.mgr.getGroup(strg);
        }
        if (g==null)
            return ("unable to determine group");
        msgState.message = generateUserList (msgState, g);
        return MessageRenderer.renderTemplate(msgState, ts.getMessageTemplate("list.users"), null);
    }

    private String getBgcolor (IRequest req) {
        User u = null;
        String  strg = req.getCookie();
        StringBuilder msg = null;
    	if (ts.getMessageTemplate("constant.defaultBgcolor") != null)
    	    msg = new StringBuilder(ts.getMessageTemplate("constant.defaultBgcolor"));
        if (msg == null)
            msg = new StringBuilder("FFFFFF");
        if (strg == null)
        	return msg.toString();
        u = UserManager.mgr.getUserByCookie(strg);
        if (u == null || u.getBgColCode() == null)
            return msg.toString();
        
        return u.getBgColCode();
    }
    
    private String getUpdatePropertie (IRequest req, String cname) {
        User u = null;
        String  strg = req.getCookie();
        
        if (strg == null)
            return "";
        u = UserManager.mgr.getUserByCookie(strg);
        
        if (u == null || u.getProperty(cname) == null)
            return "";
        
        return u.getProperty(cname).toString();
    }

    private String fonly (IRequest req) {
        String strg = req.getValue("user");
        Group g = null;        
        if (strg==null)
            strg = req.getValue("group");
        else {
            User u = UserManager.mgr.getUserByName(strg);
            if (u==null)
                return "no such user: " + strg+"<br/>";
        }
        MessageState msgState = new MessageState(null);
        if (strg==null) {
            strg = req.getCookie();
            if (strg == null)
                return "no cookie to get a user for<br/>";
            User u = UserManager.mgr.getUserByCookie(strg);
            if (u == null)
                return "no user to generate link for<br/>";
            msgState.sender = u;
            g = u.getGroup();
        }
        if (g==null) {
            g = GroupManager.mgr.getGroup(strg);
        }
        if (g==null)
            return ("unable to determine group");
        if (g.hasState(IGroupState.ENTRANCE)){
            StringBuilder msg = null;
            if (ts.getMessageTemplate("constant.fonly.false") != null)
                msg = new StringBuilder(ts.getMessageTemplate("constant.fonly.false"));
            if (msg == null)
                msg = new StringBuilder("message constant.fonly.false not found");
            return msg.toString();
        } else { 
            User u = UserManager.mgr.getUserByCookie(strg);
            if (u == null)
                return "no user to generate link for<br/>";
            if (!g.hasState(IGroupState.ENTRANCE) && u.isFriendsOnly()){
                StringBuilder msg = null;
                if (ts.getMessageTemplate("constant.fonly.true") != null)
                    msg = new StringBuilder(ts.getMessageTemplate("constant.fonly.true"));
                if (msg == null)
                    msg = new StringBuilder("message constant.fonly.true not found");
                return msg.toString();
            }  else {
                if (!g.hasState(IGroupState.ENTRANCE) && !u.isFriendsOnly()){
                    StringBuilder msg = null;
                    if (ts.getMessageTemplate("constant.fonly.false") != null)
                        msg = new StringBuilder(ts.getMessageTemplate("constant.fonly.false"));
                    if (msg == null)
                        msg = new StringBuilder("message constant.fonly.false not found");
                    return msg.toString();
                }
            }
        }
        return "";
    }

    public String generateUserList (MessageState msgState, Group cg) {
        msgState.sourceGroup=cg;
        msgState.targetGroup=cg;
        return ((AbstractCommand) CommandSet.getCommandSet().getCommand("/wc")).generateUserList(msgState, true, ts);
    }
    
    public String generateUserList () {
        MessageState msgState = new MessageState(null);
        msgState.useRenderCache = false;
        StringBuffer sb = new StringBuffer();
        Group[] grps = GroupManager.mgr.currentGroupList();
        for (int i = 0; i < grps.length; i++) {
            Group g = grps[i];
            if (g.size() < 1 || !g.isValid())
                continue;
            msgState.message = generateUserList(msgState, g); 
            sb.append (MessageRenderer.renderTemplate(msgState, ts.getMessageTemplate("list.users"), null));
        }
        return sb.toString();
    }

   /* INTERFACE RELOADABLE */
   public long lastModified () {
      return lastModified;
   }

   public void changed () {
      try {
         parts = parseFile(template);
         Server.log (this, "reload: reloaded template", Server.MSG_STATE, Server.LVL_MINOR);
         lastModified = template.lastModified ();
      } catch (Exception e) {
         Server.debug (this, "reload: ", e, Server.MSG_ERROR, Server.LVL_MAJOR);
      }
   }

	public void removed () {
		ts.removeTemplate(this);
		tplPresent=false;
	}

	public boolean filePresent () {
		return tplPresent;
	}
	public void created() {
		changed();
		ts.addTemplate(this);
		tplPresent=true;
	}

   public File getFile () {
      return template;
   }

    public String getEtag() {
        return eTag;
    }
    
    private volatile String strgVal;
    public String toString() {
        if (strgVal==null) {
            StringBuffer sb = new StringBuffer ("[Template: ");
            sb.append (tsName);
            sb.append ("/");
            sb.append (name);
            sb.append ("]");
            strgVal = sb.toString();
        }
        return strgVal;
    }

    /**
     * @return
     */
    public boolean notCacheable() {
        if ((dependsOn & (DEPENDS_ON_SESSION 
                        | DEPENDS_ON_GROUP_LIST)) != 0)
            return true;
        return false;
    }
}