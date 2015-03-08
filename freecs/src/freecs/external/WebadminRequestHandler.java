package freecs.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import freecs.Server;
import freecs.content.ActionstoreObject;
import freecs.content.BanObject;
import freecs.content.ContentContainer;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.Membership;
import freecs.core.MembershipManager;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.IGroupPlugin;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IRequest;
import freecs.interfaces.IServerPlugin;
import freecs.interfaces.IUserStates;
import freecs.layout.Template;
import freecs.layout.TemplateSet;
import freecs.util.EntityDecoder;


public class WebadminRequestHandler extends AbstractRequestHandler {
    private static final String handler= "/admin";
    private final String version = "1.0";
    
    private static final IRequestHandler selve= new WebadminRequestHandler(handler);

    
    public static IRequestHandler getHandlerInstance () {
        return selve;
    }
       
    public Object instanceForSystem() {
        return this;
    }
    
    public String getHandler() {
        return handler;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

	public WebadminRequestHandler(String handlerName) {
		super(handlerName);
	}
	
	public void handle(IRequest req, ContentContainer c) throws AccessForbiddenException {
		checkAccessIp(req, c);
		checkAccessAuth(req, c);
	
		StringBuffer sb = new StringBuffer();
		renderTemplate(req, "admin_header", sb);
		
		StringBuffer action = null;
		if (req.getValue("do") != null)
			action = new StringBuffer(req.getValue("do"));
		else action = new StringBuffer();
        if ("removeuser".equalsIgnoreCase(action.toString())) {
        	removeUser(req, sb);
        } else if ("removeright".equalsIgnoreCase(action.toString())) {
        	removeRight(req, sb);
        } else if ("giveright".equalsIgnoreCase(action.toString())) {
        	giveRight(req, sb);
        } else if ("unpunish".equalsIgnoreCase(action.toString())) {
        	unpunishUser(req, sb);
        } else if ("changestate".equalsIgnoreCase(action.toString())) {
        	changeState(req, sb);
        } else if ("refreshgp".equalsIgnoreCase(action.toString())) {
        	refreshGp(req, sb);
        } else if ("removeban".equalsIgnoreCase(action.toString())) {
        	removeBan(req, sb);
        } else if ("removeaction".equalsIgnoreCase(action.toString())) {
        	removeActionstore(req, sb);
        } else if ("sendmessage".equalsIgnoreCase(action.toString())) {
        	sendMessage(req, sb);
        } else if ("sendmessagetouser".equalsIgnoreCase(action.toString())) {
            sendMessageToUser(req, sb);
    	} else if ("sendmessagetogroup".equalsIgnoreCase(action.toString())) {
            sendMessageToGroup(req, sb);
    	} else if ("grouplist".equalsIgnoreCase(action.toString())) {
        	renderGrouplist(req, sb);
        } else if ("userlist".equalsIgnoreCase(action.toString())) {
        	renderUserlist(req, sb);
		} else if ("searchuser".equalsIgnoreCase(action.toString())) {
        	searchUser(req, sb);
		} else if ("searchgroup".equalsIgnoreCase(action.toString())) {
        	searchGroup(req, sb);
		} else if ("banlist".equalsIgnoreCase(action.toString())) {
			renderBanlist(req, sb);
		} else if ("actionstorelist".equalsIgnoreCase(action.toString())) {
			renderActionstorelist(req, sb);
		} else if ("configoverview".equalsIgnoreCase(action.toString())) {
			renderConfigoverview(req, sb);
		} else if ("shutdown".equalsIgnoreCase(action.toString())) {
            // Server.srv.startShutdown();
			if (slevel() == 1){
        	    SelectionKey key = req.getKey ();
    		    InetAddress ia = null;
    		    try {
    			   SocketChannel sc = (SocketChannel) key.channel ();
    			   ia = sc.socket ().getInetAddress ();
    		    } catch (Exception e) {
    			    Server.debug (this, "" + ia.toString (), e, Server.MSG_STATE, Server.LVL_MAJOR);
    			    throw new AccessForbiddenException(true);
    		    }
        	
                Server.log ("[Admin]", "Server shutdown "+ia.getHostAddress(), Server.MSG_STATE, Server.LVL_MAJOR);
                System.exit(0);
			} else {
				sb.append("Access denied.");
			}
        } else {
        	renderTemplate(req, "admin_index", sb);
            TemplateSet ts = Server.srv.templatemanager.getTemplateSet("admin");
            Template tpl = ts.getTemplate("admin_index");
            if (tpl == null){
               standartIndex(req, sb);
            }
        }
        sb.append("</body></html>");
        c.wrap(sb.toString(), req.getCookieDomain());
    }
    
    private void standartIndex(IRequest req, StringBuffer sb){
        sb.append("<form action=/ADMIN method=post>");            
        sb.append("send message: <input type=text name=msg><input type=submit value=send>");   
        sb.append("<input type=hidden name=do value=sendmessage></form>");   
        sb.append("<form action=/ADMIN method=post>");   
        sb.append("send message: <input type=text name=msg><br>");   
        sb.append(" to user: <input type=text name=username><input type=submit value=send>");   
        sb.append("<input type=hidden name=do value=sendmessagetouser></form>");   
        sb.append("<form action=/ADMIN method=post>");   
        sb.append("send message: <input type=text name=msg><br>");   
        sb.append(" to group: <input type=text name=groupname><input type=submit value=send>");   
        sb.append("<input type=hidden name=do value=sendmessagetogroup></form>");   

        sb.append("<form action=/ADMIN method=post>");   
        sb.append("<input type=text name=usr>");   
        sb.append("<input type=submit value=search&nbsp;User>");   
        sb.append("<input type=hidden name=do value=searchuser></form>");   
        sb.append("<form action=/ADMIN method=post>");   
        sb.append("<input type=text name=group>");   
        sb.append("<input type=submit value=search&nbsp;Group>");   
        sb.append("<input type=hidden name=do value=searchgroup></form>");   

        sb.append("<a href=/ADMIN?do=userlist>show&nbsp;userlist</a>&nbsp;|&nbsp;");   
        sb.append("<a href=/ADMIN?do=banlist>show&nbsp;banlist</a>&nbsp;|&nbsp;");   
        sb.append("<a href=/ADMIN?do=actionstorelist>show&nbsp;actionstorelist</a>&nbsp;|&nbsp;");   
        sb.append("<a href=/ADMIN?do=grouplist>grouplist</a>&nbsp;|&nbsp;");   
        sb.append("<a href=/ADMIN?do=configoverview>configoverview</a>&nbsp;|&nbsp;");   
        sb.append("<a href=/ADMIN?do=shutdown>shutdown</a>"); 
    }
	
	private void refreshGp(IRequest req, StringBuffer sb) {
		String group = req.getValue("group");
		Group g = GroupManager.mgr.getGroup(group);
		StringBuffer file = new StringBuffer (Server.BASE_PATH);
        file.append ("/grouppolicies/");
        file.append (group.toLowerCase());
        file.append (".properties");
        File f = new File (file.toString());
        if (f.exists()) {
            g = checkProperties (f, g);
            sb.append ("<b>FreeCS-Grouplist</b><br><table class=mainTable>");
      		displayGroup(g, sb);
      	    sb.append ("</table>");
        } else {
           sb.append ("<b>FreeCS-Grouplist</b><br><table class=mainTable>");
           displayGroup(g, sb);
           sb.append ("<tr>");
   		   sb.append ("<td class=name>");
  		   sb.append ("File:").append(file).append("&nbsp;not&nbsp;found");
  		   sb.append ("</td></tr>");
  	       sb.append ("</table>");
        }
	}

	private Group checkProperties (File f, Group g) {
		
		if (g == null)
			return null;
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream(f);
            props.load(in);
            in.close();
        } catch (FileNotFoundException fnfe) {
            return g;
        } catch (IOException ioe) {
            Server.debug(this, "crateByProperties:", ioe, Server.MSG_ERROR, Server.LVL_HALT);
            return g;
        }
        HashMap<String, Properties> map = new HashMap<String, Properties>();
        for (Iterator<Object> i = props.keySet().iterator(); i.hasNext(); ) {
            String key = i.next().toString();
            String low = key.toLowerCase();
            String val = props.getProperty(key);
            if ("timelock".equals(low)) {
                if (!g.hasState(IGroupState.MODERATED))
                    return null;
                try {
                    int sec = Integer.parseInt(val);
                    g.setTimelockSec(sec);
                } catch (NumberFormatException nfe) {
                    Server.log(g, "Timelock value isn't a number", Server.MSG_ERROR, Server.LVL_MINOR);
                }
            } else if ("membershiproom".equals(low)) {
            	String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setMembershipRoom ((Membership[]) msObjects.toArray(new Membership[0]));
            } else if ("autosulist".equals(low)) {
            	g.unsetAutoSu();
                g.setAutoSu (val.split(","));
            } else if ("autosumembershiplist".equals(low)) {
            	 String[] memberships = val.toLowerCase().split(",");
                 Vector<Membership> msObjects = new Vector<Membership>();
                 for (int j = 0; j < memberships.length; j++) {
                     Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                     if (cms == null)
                         continue;
                     msObjects.add(cms);
                 }
                 g.setAutoSuMembership ((Membership[]) msObjects.toArray(new Membership[0]));
            } else if ("lockprotected".equals(low) 
                        && "true".equals(val)) {
                if (!g.hasState(IGroupState.OPEN))
                    g.setState (IGroupState.OPEN);
                g.setState (IGroupState.LOCKPROTECTED);
            } else if ("autosu".equals(low)
                        && "false".equals(val)) {
                g.unsetState (IGroupState.AUTO_SU_FIRST);
            } else if ("allowsu".equals(low)
                        && "false".equals(val)) {
                g.unsetState (IGroupState.ALLOW_SU);
            }  else if ("nosufirst".equals(low)
                    && "true".equals(val)) {
                g.setState (IGroupState.NO_SU_FIRST);
            }  else if ("minuserrolesu".equals(low)) {
                if ("vip".equals(val))
                    g.setMinRightSu(IUserStates.ROLE_VIP);
                else if ("moderator".equals(val))
                    g.setMinRightSu(IUserStates.IS_MODERATOR);
                else if ("admin".equals(val))
                    g.setMinRightSu(IUserStates.ROLE_GOD);
                else
                    Server.log (this, "minuserrolesu has wrong value '" + val + "' for group " + g.getRawName(), Server.MSG_STATE, Server.LVL_MINOR);
            } else if ("membershipsu".equals(low)) {
            	String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setMembershipSu ((Membership[]) msObjects.toArray(new Membership[0]));
            } else if ("soundprooffor".equals(low)) {
                String[] roles = val.toLowerCase().split(",");
                for (int j = 0; j < roles.length; j++) {
                    String curr = roles[j].trim();
                    if ("vip".equals(curr))
                        g.setState(IGroupState.SND_PRF_VIP);
					else if ("guest".equals(curr))
						g.setState(IGroupState.SND_PRF_GUEST);
                    else if ("moderator".equals(curr))
                        g.setState(IGroupState.SND_PRF_MODERATOR);
                    else if ("admin".equals(curr))
                        g.setState(IGroupState.SND_PRF_GOD);
                    else if ("user".equals(curr))
                        g.setState(IGroupState.SND_PRF_USER);
                }
            } else if ("suforbidden".equals(low)) {
                g.setSuForbiddenMembership(val);
            } else if ("memberroom".equals(low)) {
                String[] memberships = val.toLowerCase().split(",");
                Vector<Membership> msObjects = new Vector<Membership>();
                for (int j = 0; j < memberships.length; j++) {
                    Membership cms = MembershipManager.instance.getMembership(memberships[j]);
                    if (cms == null)
                        continue;
                    msObjects.add(cms);
                }
                g.setMemberRoom ((Membership[]) msObjects.toArray(new Membership[0]));
            } else if ("allowusebbcodes".equals(low) && "true".equals(val)) {
            	g.setState(IGroupState.ALLOW_USE_BBCODES);          	
            } else if ("allowusebbcodes".equals(low) && "false".equals(val)) {
            	g.unsetState(IGroupState.ALLOW_USE_BBCODES);
            } else if ("allowusesmiley".equals(low) && "true".equals(val)) {
                g.setState(IGroupState.ALLOW_USE_SMILEY);
            } else if ("allowusesmiley".equals(low) && "false".equals(val)) {
                g.unsetState(IGroupState.ALLOW_USE_SMILEY);
            } else if ("deactivatehitdice".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.DEACTIVATE_HITDICE);
            } else if ("sucanban".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.SU_CAN_BAN);
            } else if ("sucanban".equals(low) && "false".equals(val)){
            	g.unsetState (IGroupState.SU_CAN_BAN);
            } else if ("sucansettheme".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.SU_CAN_SETTHEME);
            } else if ("sucansettheme".equals(low) && "false".equals(val)){
             	g.unsetState (IGroupState.SU_CAN_SETTHEME);
            } else if ("cansetpunishable".equals(low) && "true".equals(val)){
            	g.setState (IGroupState.CAN_SET_PUNISHABLE);
            } else if ("notallowjoinunreg".equals(low) && "true".equals(val)){
                g.setState (IGroupState.NOT_ALLOW_JOIN_UNREG);
            } else if (low.startsWith("extension.")) {
                String namespace = low.substring(10);
                int idx = namespace.indexOf(".");
                if (idx == -1) {
                    Server.log (this, "invalid GroupPolicy for extension: " + low, Server.MSG_ERROR, Server.LVL_MINOR);
                    continue;
                }
                String propName = namespace.substring(idx+1);
                namespace = namespace.substring(0,idx);
                Properties p = (Properties) map.get(namespace);
                if (p==null) {
                    p = new Properties();
                    map.put(namespace, p);
                }
                p.setProperty(propName, val);
            }
        }
        
        if (map.size() > 0) {
            Vector<IGroupPlugin> plugins = new Vector<IGroupPlugin>();
            for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
                String namespace = (String) i.next();
                Properties p = (Properties) map.get(namespace);
                String url = p.getProperty("url");
                Object o;
                synchronized (GroupManager.pluginStore) {
                    o = GroupManager.pluginStore.get(url);
                    if (o == null) {
                        try {
                            Class<?> piClass = Class.forName(url);
                            Method getInstance = piClass.getMethod("getMasterInstance");
                            if (getInstance==null)
                                throw new Exception ("Specified plugin-object doesn't implement static getMasterInstance");
                            o = getInstance.invoke(null);
                            if (!(o instanceof IGroupPlugin))
                                throw new Exception ("Specified plugin-object doesn't implement interface IGroupPlugin");
                            GroupManager.pluginStore.put(url, o);
                        } catch (Exception e) {
                            Server.log (this, "invalid url for extension: ("+e+") " + url, Server.MSG_ERROR, Server.LVL_MINOR);
                            continue;
                        }
                    }
                }
                try {
                    plugins.add(((IGroupPlugin) o).instanceForGroup(namespace, g, p));
                } catch (Exception e) {
                    Server.debug(this, "catched exception while getting GroupPlugin-instance", e, Server.MSG_STATE, Server.LVL_MAJOR);
                }
            }
            g.setPlugins((IGroupPlugin[]) plugins.toArray(new IGroupPlugin[0]));
        }      
        return g;
    }
	
	private void changeState(IRequest req, StringBuffer sb) {
		 String group = req.getValue("group");
		 String state = req.getValue("state");
		 String right = req.getValue("right");
		 Group g = GroupManager.mgr.getGroup(group);
		 if (g != null){
			 if (state.equals("timelocksub")){
				 int t_old=g.getTimelockSec();
				 g.setTimelockSec(t_old-5);
			 } else if (state.equals("timelockadd")){
				 int t_old=g.getTimelockSec();
				 g.setTimelockSec(t_old+5);
			 } else if (state.equals("allowusebbcodes")){
			     if (g.hasState(IGroupState.ALLOW_USE_BBCODES)){
				     g.unsetState(IGroupState.ALLOW_USE_BBCODES);
			      } else g.setState(IGroupState.ALLOW_USE_BBCODES);
		     } else if (state.equals("allowusesmiley")) {
	                if (g.hasState(IGroupState.ALLOW_USE_SMILEY)) {
	                    g.unsetState(IGroupState.ALLOW_USE_SMILEY);
	                } else {
	                    g.setState(IGroupState.ALLOW_USE_SMILEY);
	                }
	         } else if (state.equals("lockprotected")){
			     if (g.hasState(IGroupState.LOCKPROTECTED)){
				     g.unsetState(IGroupState.LOCKPROTECTED);
			     } else {
			    	 g.setState (IGroupState.OPEN);
                     g.setState (IGroupState.LOCKPROTECTED);
                 }
		     } else if (state.equals("allowsu")){
			     if (g.hasState(IGroupState.ALLOW_SU)){
				     g.unsetState(IGroupState.ALLOW_SU);
			     } else g.setState(IGroupState.ALLOW_SU);
		     } else if (state.equals("minuserrolesu")) {
	                if (right.equals("user")) {
	                    g.setMinRightSu(IUserStates.ROLE_USER);
	                } else if (right.equals("vip")) {
	                    g.setMinRightSu(IUserStates.ROLE_VIP);
	                } else if (right.equals("moderator")) {
	                    g.setMinRightSu(IUserStates.IS_MODERATOR);
	                } else if (right.equals("admin")) {
	                    g.setMinRightSu(IUserStates.ROLE_GOD);
	                }
		     } else if (state.equals("moderated")){
			     if (g.hasState(IGroupState.MODERATED)){
				     g.unsetState(IGroupState.MODERATED);
			     } else g.setState(IGroupState.MODERATED);
		     } else if (state.equals("hitdice")){
			     if (g.hasState(IGroupState.DEACTIVATE_HITDICE)){
				     g.unsetState(IGroupState.DEACTIVATE_HITDICE);
			     } else g.setState(IGroupState.DEACTIVATE_HITDICE);
		     } else if (state.equals("nosufirst")){
			     if (g.hasState(IGroupState.NO_SU_FIRST)){
				     g.unsetState(IGroupState.NO_SU_FIRST);
			     } else g.setState(IGroupState.NO_SU_FIRST);
		     } else if (state.equals("sucanban")){
			     if (g.hasState(IGroupState.SU_CAN_BAN)){
				     g.unsetState(IGroupState.SU_CAN_BAN);
			     } else g.setState(IGroupState.SU_CAN_BAN);
		     } else if (state.equals("sucansettheme")){
			     if (g.hasState(IGroupState.SU_CAN_SETTHEME)){
				     g.unsetState(IGroupState.SU_CAN_SETTHEME);
			     } else g.setState(IGroupState.SU_CAN_SETTHEME);
		     } else if (state.equals("cansetpunishable")){
			     if (g.hasState(IGroupState.CAN_SET_PUNISHABLE)){
				     g.unsetState(IGroupState.CAN_SET_PUNISHABLE);
			     } else g.setState(IGroupState.CAN_SET_PUNISHABLE);
		     } else if (state.equals("joinunreg")) {
	             if (g.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG)) {
	                 g.unsetState(IGroupState.NOT_ALLOW_JOIN_UNREG);
	             } else {
	                 g.setState(IGroupState.NOT_ALLOW_JOIN_UNREG);
	             }
		     } else if (state.equals("joinmembershiplocked")) {
	                if (g.hasState(IGroupState.JOIN_MEMBERSHIP_LOCKED)) {
	                    g.unsetState(IGroupState.JOIN_MEMBERSHIP_LOCKED);
	                } else {
	                    g.setState(IGroupState.JOIN_MEMBERSHIP_LOCKED);
	                }
	         }
	   		 sb.append ("<b>FreeCS-Grouplist</b><br /><table class=mainTable>");
	   		 displayGroup(g, sb);
	   	     sb.append ("</table>");
		 } else {
			 sb.append ("<b>FreeCS-Grouplist</b><br /><table class=mainTable>");
	         sb.append("<tr>");
	         sb.append("<td class=name");
	         sb.append("group not found");
	         sb.append("</td></tr>");
	         sb.append ("</table>");
		 }
	}

	private void removeRight(IRequest req, StringBuffer sb) {
		 String usrName = req.getValue("name");
		 String right = req.getValue("right");
		 User   ur = UserManager.mgr.getUserByName (usrName);
	        if (ur!=null) {
	        	if (right.equals("user")){
	        		ur.setNewPermission(IUserStates.ROLE_ASSHOLE);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("vip")){
	        		ur.setNewPermission(IUserStates.ROLE_USER);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("admin")){
	        		ur.setNewPermission(IUserStates.ROLE_USER);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("moderator")){
	        		ur.takePermission(IUserStates.IS_MODERATOR);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
                    displayUser(ur, sb);
	        	} else if (right.equals("guest")){
	        		ur.takePermission(IUserStates.IS_GUEST);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	}
	        	sb.append ("</table>");
	        } else {
	    		sb.append ("<table class=mainTable>");
	            sb.append("<tr>");
	            sb.append("<td class=name");
	    		sb.append("user not found");
	    		sb.append("</td></tr>");
	    		sb.append ("</table>");
	        }		 
	}
	
	private void giveRight(IRequest req, StringBuffer sb) {
		 String usrName = req.getValue("name");
		 String right = req.getValue("right");
		 User   ur = UserManager.mgr.getUserByName (usrName);
	        if (ur!=null) {
	        	if (right.equals("user")){
	        		ur.setNewPermission(IUserStates.ROLE_USER);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else  if (right.equals("vip")){
	        		ur.setNewPermission(IUserStates.ROLE_VIP);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("admin")){
	        		if (slevel() == 1)
	        		    ur.setNewPermission(IUserStates.ROLE_GOD);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("moderator")){
	        		ur.givePermission(IUserStates.IS_MODERATOR);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	} else if (right.equals("guest")){
	        		ur.givePermission(IUserStates.IS_GUEST);
	        		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
	        		displayUser(ur, sb);
	        	}   		
	        	sb.append ("</table>");
	        } else {
	        	sb.append ("<table class=mainTable>");
                sb.append("<tr>");
                sb.append("<td class=name >");
    		    sb.append("user not found");
    		    sb.append("</td></tr>");
    		    sb.append ("</table>");
	        }		 
	}
	
	private void unpunishUser(IRequest req, StringBuffer sb) {
        String usrName = req.getValue("name");
        User ur = UserManager.mgr.getUserByName (usrName);
        if (ur!=null) {
            ur.setPunish(false);
    		sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
            displayUser(ur, sb);
        } else {
        	sb.append ("<table class=mainTable>");
            sb.append("<tr>");
            sb.append("<td class=name >");
		    sb.append("user not found");
		    sb.append("</td></tr>");
        }
	}
	
	private void removeUser(IRequest req, StringBuffer sb) {
        String usrName = req.getValue("name");
        String usrCookie = req.getValue("cookie");
        String force = req.getValue("force");
        User ur = UserManager.mgr.getUserByCookie(usrCookie);
        if (ur==null)
           ur = UserManager.mgr.getUserByName (usrName);
        if (ur!=null) {
            ur.sendQuitMessage(false, null);
            if ("true".equalsIgnoreCase(force)) {
                UserManager.mgr.ustr.removeUser(ur);
                sb.append("forcibly removed user");
            } else {
                sb.append("removed user");
            } 
        } else {
            sb.append("user not found");
        }
	}

	private void removeBan(IRequest req, StringBuffer sb) {
        String val = req.getValue("name");
        if (val != null) {
           if (Server.srv.removeBan(val))
               sb.append("removed ban for user " + val);
           else
               sb.append("No ban found for user " + val);
        } else {
            val = req.getValue("host");
            if (Server.srv.removeBan(val))
                sb.append("removed ban for host " + val);
            else
                sb.append("No ban found for host " + val);
        }
	}
	
	private void removeActionstore(IRequest req, StringBuffer sb) {
        String val = req.getValue("name");
        String action = req.getValue("action");
        int reason = new Integer(action).intValue();
        if (val != null) {
           if (Server.srv.removeStore(val, reason))
               sb.append("removed action for user " + val);
           else
               sb.append("No action found for user " + val);
        } 
	}

	private void sendMessage(IRequest req, StringBuffer sb) {
        if (req.getValue("msg") == null) {
            sb.append("no message!");
        } else {
        	AdminCore.messageToAll(req.getValue("msg"));
            sb.append("message sent!");
        }
	}
	private void sendMessageToUser(IRequest req, StringBuffer sb) {
        if (req.getValue("msg") == null || req.getValue("username") == null) {
            sb.append("no message or username!");
        } else {
        	AdminCore.messageToUser(req.getValue("msg"),req.getValue("username"));
            sb.append("message sent!");
        }
	}
	private void sendMessageToGroup(IRequest req, StringBuffer sb) {
        if (req.getValue("msg") == null || req.getValue("groupname") == null) {
            sb.append("no message or groupname!");
        } else {
        	AdminCore.messageToGroup(req.getValue("msg"),req.getValue("groupname"));
            sb.append("message sent!");
        }
	}

	private void renderUserlist(IRequest req, StringBuffer sb) {
        sb.append ("<b>FreeCS-Userlist</b><br><table class=mainTable>");
        User[] users = UserManager.mgr.ustr.toArray();
		 for (int i = 0; i < users.length; i++) {
             User u = users[i];
             displayUser(u, sb);
		 }
	}
	
	private void searchUser(IRequest req, StringBuffer sb) {
		String usrName = req.getValue("usr");
		User u = null;
		sb.append ("<b>FreeCS-Usersearch</b><br><table class=mainTable>");
		if (usrName == null ){
			sb.append ("<br>missing Username");
		} else u = UserManager.mgr.getUserByName(usrName);
		
        if (u != null) {
        	displayUser(u, sb);
        } else if (usrName != null) {sb.append ("<br>User not found");}
	}
	
	private void searchGroup(IRequest req, StringBuffer sb) {
		String group = req.getValue("group");
		Group g = null;
		sb.append ("<b>FreeCS-Groupsearch</b><br><table class=mainTable>");
		if (group == null){
			sb.append ("<br>missing Groupname");
		} else g = GroupManager.mgr.getGroup(group); 
		
        if (g != null) {
        	displayGroup(g, sb);
        } else if (group != null){ sb.append ("<br>group (").append(group).append(") not found");}
	}
	
    private void displayGroup(Group g, StringBuffer sb){
    	if (g == null){
    		sb.append("group not found");
    		return;
    	}
    	String groupname = g.getRawName().toLowerCase();	
    	sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Groupname");
		sb.append ("</td>");
		sb.append ("<td width=200px class=param>");
		sb.append (groupname);
		sb.append ("</td>");	
		sb.append ("<td class=name>");
		sb.append ("Entrace");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.ENTRANCE))
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");   		
		sb.append ("</td>");	
		sb.append ("<td class=name>");
		sb.append ("Open");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.OPEN))
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");   		
		sb.append ("</td>");			
		sb.append ("<td class=name>");
		sb.append ("Lockprotected");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.LOCKPROTECTED))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=lockprotected&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
  		else sb.append ("<a href=\"/ADMIN?do=changestate&state=lockprotected&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");	
		sb.append ("<td class=name>");
		sb.append ("Allow&nbsp;SU");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.ALLOW_SU))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=allowsu&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=allowsu&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");	
		
		sb.append ("<td class=name>");
		sb.append ("Minright&nbsp;SU");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasMinRightSu(IUserStates.ROLE_USER))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=minuserrolesu&group=").append(groupname).append("&right=vip\">").append("USER").append("</a>");
 		else if (g.hasMinRightSu(IUserStates.ROLE_VIP))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=minuserrolesu&group=").append(groupname).append("&right=admin\">").append("VIP").append("</a>");
 		else  if (g.hasMinRightSu(IUserStates.ROLE_GOD))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=minuserrolesu&group=").append(groupname).append("&right=moderator\">").append("ADMIN").append("</a>");
 		else  if (g.hasMinRightSu(IUserStates.IS_MODERATOR))
   		    sb.append ("<a href=\"/ADMIN?do=changestate&state=minuserrolesu&group=").append(groupname).append("&right=user\">").append("MOD").append("</a>");		
 		sb.append ("</td>");	
 		sb.append ("<td class=name>");
		sb.append ("Minright&nbsp;Open");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasMinRight(IUserStates.ROLE_USER) || g.hasMinRight(IUserStates.MAY_JOIN_GROUP))
   		    sb.append ("USER");
 		else if (g.hasMinRight(IUserStates.ROLE_VIP))
   		    sb.append ("VIP");
 		else  if (g.hasMinRight(IUserStates.ROLE_GOD))
   		    sb.append ("ADMIN");
 		else  if (g.hasMinRight(IUserStates.IS_MODERATOR))
   		    sb.append ("MOD");		
 		sb.append ("</td>");
 		sb.append ("<td class=name>");
		sb.append ("Membership&nbsp;Open");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.getMembershipRoom() != null)
		    sb.append (g.getMembershipRoom());		
		else sb.append("<img src =\"../static/no.gif\" border =\"0\">");
 		sb.append("</td>");
 		sb.append("\r\n<td class=name>");
        sb.append("Membershiplock");
        sb.append("\r\n</td>");
        sb.append("\r\n<td class=param>");
        if (g.hasState(IGroupState.JOIN_MEMBERSHIP_LOCKED)) {
            sb.append("\r\n<a href=\"/ADMIN?do=changestate&state=joinmembershiplocked&group=");
            sb.append(groupname);
            sb.append( "\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
        } else {
            sb.append("\r\n<a href=\"/ADMIN?do=changestate&state=joinmembershiplocked&group=")
            .append(groupname)
            .append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
        }
        sb.append("\r\n</td>");
         
		sb.append ("<tr>");
		sb.append ("<td class=name colspan=2>");
		sb.append ("<a href=\"/ADMIN?do=refreshgp&&group=").append(groupname).append("\"><img src =\"../static/refresh.gif\" border =\"0\" alt=\"refresh Grouppolice\"></a>");
		sb.append ("</td>");
		sb.append ("<td class=name>");
		sb.append ("BBC");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.ALLOW_USE_BBCODES))
		    sb.append ("<a href=\"/ADMIN?do=changestate&state=allowusebbcodes&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
		else sb.append ("<a href=\"/ADMIN?do=changestate&state=allowusebbcodes&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");			
		sb.append ("<td class=name>");
		sb.append ("Hitdice");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (!g.hasState(IGroupState.DEACTIVATE_HITDICE))
  		    sb.append ("<a href=\"/ADMIN?do=changestate&state=hitdice&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=hitdice&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");
		sb.append ("<td class=name>");
		sb.append ("No&nbsp;SU first");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.NO_SU_FIRST))
  		    sb.append ("<a href=\"/ADMIN?do=changestate&state=nosufirst&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=nosufirst&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");
		sb.append ("<td class=name>");
		sb.append ("SU&nbsp;setTheme");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.SU_CAN_SETTHEME))
  		    sb.append ("<a href=\"/ADMIN?do=changestate&state=sucansettheme&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=sucansettheme&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>"); 
		sb.append ("<td class=name>");
		sb.append ("SU&nbsp;Ban");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.SU_CAN_BAN))
  		    sb.append ("<a href=\"/ADMIN?do=changestate&state=sucanban&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=sucanban&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>"); 
		sb.append ("<td class=name>");
		sb.append ("AutoSuList");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.getAutoSuList() != null && g.getAutoSuList().size()>0){
		    for (Enumeration<String> e = g.getAutoSuList().elements(); e.hasMoreElements(); ) {
			    sb.append((String) e.nextElement());
			    if (e.hasMoreElements()){
			    	sb.append("<b>,</b>");
			    }
            }
		} else sb.append("<img src =\"../static/no.gif\" border =\"0\">");
		sb.append ("</td>");
		sb.append ("<td class=name>");
		sb.append ("AutoSuMembershipList");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.getAutoSuMembership() != null && g.getAutoSuMembership().length>0){
			Membership[] values = g.getAutoSuMembership();
		    for (int i=0;i< values.length; i++ ) {
			    sb.append(values[i].key);
			    if (i<values.length-1){
				    sb.append("<b>,</b>");
			    }
            }
		} else sb.append("<img src =\"../static/no.gif\" border =\"0\">");
		sb.append ("<td class=name>");
		sb.append ("MemberRoom");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.getMemberships() != null && g.getMemberships().length>0){
			Membership[] values = g.getMemberships();
		    for (int i=0;i< values.length; i++ ) {
			    sb.append(values[i].key);
			    if (i<values.length-1){
				    sb.append("<b>,</b>");
			    }
            }
		} else sb.append("<img src =\"../static/no.gif\" border =\"0\">");
		sb.append ("</td>");	
		sb.append ("</tr>");	
		sb.append ("\r\n<tr>");
		sb.append ("\r\n<td class=name colspan=2>");
        sb.append ("\r\n<img title=\"opener\" src=\"../static/schluessel.gif\">  ");
        User u = UserManager.mgr.getUserByName(g.getOpener().getName());
        if (u != null){
            if (Server.srv.USE_FADECOLOR){
                if (u.getFadeColCode() != null && u.getFadeColorUsername()!=null){
                    sb.append(u.getFadeColorUsername().toString());
                } else sb.append(u.getNoFadeColorUsername());
            } else sb.append (EntityDecoder.charToHtml (u.getName ()));
        } else {
            if (g.getOpener() instanceof User)
                sb.append("<i>").append(g.getOpener().getName()).append("</i>");
        }
		sb.append ("\r\n</td>");		
		sb.append ("<td class=name>");
		sb.append ("Punishable");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.CAN_SET_PUNISHABLE))
		    sb.append ("<a href=\"/ADMIN?do=changestate&state=cansetpunishable&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
		else sb.append ("<a href=\"/ADMIN?do=changestate&state=cansetpunishable&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");
		sb.append ("<td class=name>");
		sb.append ("Moderated");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		if (g.hasState(IGroupState.MODERATED))
  		    sb.append ("<a href=\"/ADMIN?do=changestate&state=moderated&group=").append(groupname).append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
 		else sb.append ("<a href=\"/ADMIN?do=changestate&state=moderated&group=").append(groupname).append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
		sb.append ("</td>");
		
        if (Server.srv.USE_SMILEY) {
            sb.append("\r\n<td class=name>");
            sb.append("Smiley");
            sb.append("\r\n</td>");
            sb.append("\r\n<td class=param>");
            
            if (g.hasState(IGroupState.ALLOW_USE_SMILEY)) {
                sb.append("<a href=\"/ADMIN?do=changestate&state=allowusesmiley&group=")
                .append(groupname)
                .append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
            } else {
                sb.append("\r\n<a href=\"/ADMIN?do=changestate&state=allowusesmiley&group=")
                .append(groupname)
                .append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
            }
            sb.append("\r\n</td>");
        }

		sb.append ("<td class=name>");
		sb.append ("Mod&nbsp;Timelock");
		sb.append ("</td>");	
		sb.append ("<td class=param>");
		sb.append (g.getTimelockSec());
		sb.append ("&nbsp;sec<br>");
		sb.append ("<a style=\"text-decoration:none\" href=\"/ADMIN?do=changestate&state=timelocksub&group=").append(groupname).append("\">-</a>");
		sb.append ("<b>/</b>");
 		sb.append ("<a style=\"text-decoration:none\" href=\"/ADMIN?do=changestate&state=timelockadd&group=").append(groupname).append("\">+</a>");
 		sb.append("\r\n</td>");
        sb.append("\r\n<td class=name>");
        sb.append("join Unreg");
        sb.append("\r\n</td>");
        sb.append("\r\n<td class=param>");
        if (g.hasState(IGroupState.NOT_ALLOW_JOIN_UNREG)){
            sb.append("\r\n<a href=\"/ADMIN?do=changestate&state=joinunreg&group=")
            .append(groupname)
            .append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"change\"></a>");
        } else {
            sb.append("\r\n<a href=\"/ADMIN?do=changestate&state=joinunreg&group=")
            .append(groupname)
            .append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"change\"></a>");
        }
        sb.append("\r\n</td>");     
        sb.append("\r\n</tr>\r\n");
    }
    
    private void displayUser(User u, StringBuffer sb) {
        sb.append("\r\n<tr>");
        sb.append("\r\n<td class=logout><a href=\"/admin?do=removeuser&name=");
        sb.append(u.getName().toLowerCase());
        sb.append("&cookie=");
        sb.append(u.getCookie());
        sb.append("\">logout</a>");
        sb.append("\r\n</td>");
        sb.append("\r\n<td class=remove>");
        sb.append("\r\n<a href=\"/admin?do=removeuser&name=");
        sb.append(u.getName().toLowerCase());
        sb.append("&cookie=");
        sb.append(u.getCookie());
        sb.append("&force=true\">remove</a>");
        sb.append("</td>");
        sb.append("\r\n<td class=username>");
        sb.append(u.getName());
        sb.append("\r\n</td>");
        
        sb.append(has_Right(u, IUserStates.ROLE_USER, "user"));    
        sb.append(has_Right(u, IUserStates.ROLE_VIP, "vip"));
        sb.append(has_Right(u, IUserStates.ROLE_GOD, "admin"));
        sb.append(has_Right(u, IUserStates.IS_MODERATOR, "moderator"));
        sb.append(has_Right(u, IUserStates.IS_GUEST, "guest"));
        sb.append(has_RightAsshole(u, IUserStates.ROLE_ASSHOLE));
        
        if (Server.srv.USE_SMILEY)
            sb.append(may_use_Smiley(u));
        sb.append(may_set_Theme(u));
        sb.append(may_call_Memberships(u));
        
        sb.append("\r\n<td class=name>");
        sb.append("is&nbsp;punished");
        sb.append("\r\n<td class=param>");
        if (u.isPunished()) {
            sb.append("\r\n<a href=\"/ADMIN?do=unpunish&name=")
              .append(u.getName().toLowerCase())
              .append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"remove\"></a>");
        } else {
            sb.append("<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        sb.append("\r\n<td class=name>");
        sb.append("is&nbsp;reg");
        sb.append("\r\n<td class=param>");
        if (!u.isUnregistered) {
            sb.append("<img src =\"../static/ok.gif\">");
        } else {
            sb.append("<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        sb.append("\r\n<td class=groupname>in&nbsp;");
        Group g = u.getGroup();
        sb.append(g != null ? g.getRawName()
                : "<i>currently changing group</i>");
        sb.append("\r\n</td>");
        sb.append("\r\n</tr>\r\n");
    }
	
    private void renderBanlist(IRequest req, StringBuffer sb) {
        BanObject[] bArr = Server.srv.getBanList();
        sb.append("<b>FreeCS-BanList</b><br />");
        if (bArr.length < 1) {
            sb.append("There are no BanObjects at the moment");
        } else {
            StringBuffer ub = new StringBuffer(
                    "<table border=\"0\"><tr><td class=\"param\"><b>User (ip)</b></td><td class=\"param\"><b>Banned by</b></td><td class=\"param\">Banned until</td><td class=\"param\">Release</td><td class=\"param\">Message</td></tr>");
            StringBuffer hb = new StringBuffer(
                    "<table border=\"0\"><tr><td class=\"param\"><b>Host</b></td><td class=\"param\"><b>Hostname</b></td><td class=\"param\">Banned until</td><td class=\"param\">Release</td><td class=\"param\">details</td></tr>");
            StringBuffer ib = new StringBuffer(
            "<table border=\"0\"><tr><td class=\"param\"><b>IP Block</b></td></tr>");

            Vector<BanObject> v = new Vector<BanObject>();
            for (int i = 0; i < bArr.length; i++) {
                BanObject curr = bArr[i];
                if (curr.hostban == null) {
                    if (v.contains(curr)) {
                        continue;
                    }
                    v.add(curr);
                    ub.append("<tr><td class=\"param\">");
                    ub.append(curr.usr);
                    ub.append(" (");
                    ub.append(curr.con);
                    ub.append(")</td><td class=\"param\">");
                    ub.append(curr.bannedBy);
                    ub.append("</td><td class=\"param\">");
                    ub.append(Server.formatDefaultTimeStamp(curr.time));
                    ub.append("</td><td class=\"param\">");
                    ub.append("<a href=\"/admin?do=removeban&name=");
                    ub.append(curr.usr);
                    ub.append("\">X</a></td><td  class=\"param\">");
                    ub.append(curr.msg);
                    ub.append("</td></tr>");
                } else {
                    hb.append("<tr><td class=\"param\">");
                    hb.append(curr.hostban);
                    hb.append("</td><td class=\"param\">");
                    boolean displayBan = Boolean.parseBoolean(Server.srv.getProperty("displayhostname"));
                    if (displayBan){
                        InetAddress ia;
                        try { 
                            ia = InetAddress.getByName(curr.hostban);
                            hb.append(ia.getHostName());
                        } catch (UnknownHostException e) {
                             Server.log(this,"Unknown Host "+curr.hostban+"("+ e.toString()+")", Server.MSG_ERROR, Server.LVL_MAJOR);
                        }   
                    } else  hb.append("---");
                    hb.append("</td><td class=\"param\">");
                    if (curr.time > 0) {
                        hb.append(Server.formatDefaultTimeStamp(curr.time));
                    } else {
                        hb.append("--.--.----");
                    }
                    hb.append("</td><td class=\"param\">");
                    hb.append("<a href=\"/admin?do=removeban&host=");
                    hb.append(curr.hostban);
                    hb.append("\">X</a></td><td class=\"param\">");
                    hb.append(curr.msg);
                    hb.append("</td></tr>");
                }
            }
            StringBuffer ipBlock = null;
            if (Server.srv.props.getProperty("permaIpBlock") != null)
                ipBlock = new StringBuffer(Server.srv.props.getProperty("permaIpBlock"));
            if (ipBlock != null) {
                String values[] = ipBlock.toString().split(",");
                for (int i = 0; i < values.length; i++){
                    ib.append("<tr><td class=\"param\">");
                    ib.append(values[i]);
                    ib.append("</td></tr>");
                 }               
            } 

            ub.append("</table>");
            hb.append("</table>");
            sb.append("<table border=\"0\"><tr><td valign=\"top\">");
            sb.append(ub.toString());
            sb.append("</td><td width=\"2\" bgcolor=\"#000000\"></td><td valign=\"top\">");
            sb.append(hb.toString());
            sb.append("</td><td width=\"2\" bgcolor=\"#000000\"></td><td valign=\"top\">");
            sb.append(ib.toString());
            sb.append("</td></tr></table>");
        }
        sb.append("</table>");
    }
	
    private void renderActionstorelist(IRequest req, StringBuffer sb) {
        ActionstoreObject[] pArr = Server.srv.getStoreList();
        sb.append("<b>FreeCS-ActionstoreList</b><br>");
        if (pArr.length < 1) {
            sb.append("There are no ActionstoreObjects at the moment");
        } else {
            StringBuffer ub = new StringBuffer();
            StringBuffer hb = new StringBuffer();
            ub.append("<table border=0><tr><td class=\"param\"><b>Action</b></td><td class=\"param\"><b>Room</b></td><td class=\"param\"><b>User </b></td><td class=\"param\"><b>Email </b></td><td class=\"param\"><b>Stored by</b></td><td class=\"param\"><b>Stored User</b></td><td class=\"param\">Stored until</td><td class=\"param\">Release</td><td class=\"param\">Message</td></tr>");
            for (int i = 0; i < pArr.length; i++) {
                ActionstoreObject curr = pArr[i];
                if (curr.usr != null) {
                    ub.append("<tr><td class=\"param\">");
                    ub.append(curr.rendererActionState());
                    ub.append("</td><td class=\"param\">");
                    ub.append(curr.room);
                    ub.append("</td><td class=\"param\">");
                    ub.append(curr.usr);
                    ub.append("</td><td class=\"param\">");
                    ub.append(curr.email);
                    ub.append("</td><td class=\"param\">");
                    ub.append(curr.storedBy);
                    ub.append("</td><td class=\"param\">");
                    ub.append(curr.cu);
                    ub.append("</td><td class=\"param\">");
                    ub.append(Server.formatDefaultTimeStamp(curr.time));
                    ub.append("</td><td class=\"param\">");
                    ub.append("<a href=\"/admin?do=removeaction&name=");
                    ub.append(curr.usr);
                    ub.append("&action=");
                    ub.append(curr.action);
                    ub.append("\">X</a></td><td class=\"param\">");
                    ub.append(curr.msg);
                    ub.append("</td></tr>");
                }
            }
            ub.append("</table>");
            sb.append("<table border=0><tr><td valign=top>");
            sb.append(ub.toString());
            sb.append("</td><td width=2 bgcolor=#000000></td><td valign=top>");
            sb.append(hb.toString());
            sb.append("</td></tr></table>");
        }
        sb.append("</table>");
    }

	private void renderGrouplist(IRequest req, StringBuffer sb) {
        Group[] grps = GroupManager.mgr.currentGroupList();
        sb.append ("<b>FreeCS Grouplist</b><br>");
		sb.append ("<table class=mainTable>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("open group(s): ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (GroupManager.mgr.openGroupsCount());
		sb.append ("</td>");	
		sb.append ("</tr>");       
        for (int i = 0; i < grps.length; i++) {
        	Group g = grps[i];
        	displayGroup(g, sb);  
        }
        sb.append("</table");
	}
	
	private void renderConfigoverview(IRequest req, StringBuffer sb) {
		sb.append ("<b>FreeCS-Config Overview</b><br>");
		sb.append ("<table class=mainTable>");
        sb.append("<td class=name>");
        sb.append("System: ");
        sb.append("</td>");
        sb.append("<td class=param>");
        if (System.getProperty("java.version") != null)
            sb.append(System.getProperty("java.version"));
        sb.append(" ");
        if (System.getProperty("os.name") != null)
            sb.append(System.getProperty("os.name"));
        sb.append(" ");
        if (System.getProperty("os.version") != null)
            sb.append(System.getProperty("os.version"));
        sb.append(" ");
        sb.append("</td>");
        sb.append("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Plugin: ");
		sb.append ("</td>");
        
        int r = 1;
        for (Iterator<String> i = Server.srv.pluginStore.keySet()
                .iterator(); i.hasNext();) {
            String key = (String) i.next();
            sb.append("<td class=param>");
            sb.append(key);
            sb.append("</td>");
            r++;
            if (r >= 6){
                r=0;
                sb.append("</tr>");
                sb.append("<tr>");
                sb.append("<td class=param>");
                sb.append("</td>");
            }
        }
        for (Iterator<String> i = GroupManager.mgr.getGroupPlugins().keySet()
                .iterator(); i.hasNext();) {
            String key = (String) i.next();
            sb.append("<td class=param>");
            sb.append(key);
            sb.append("</td>");
            r++;
            if (r >= 6){
                r=0;
                sb.append("</tr>");
                sb.append("<tr>");
                sb.append("<td class=param>");
                sb.append("</td>");
            }
        }
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("maxUsers: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_USERS);
		sb.append ("</td>");	
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Usertimeout: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.USER_TIMEOUT == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.USER_TIMEOUT/60000+" min");
		sb.append ("</td>");	
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Userawaytimeout: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.USER_AWAY_TIMEOUT == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.USER_AWAY_TIMEOUT/60000+" min");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Viptimeout: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.VIP_TIMEOUT == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else if (Server.srv.VIP_TIMEOUT == 0)
			sb.append ("same as User");
		else sb.append (Server.srv.VIP_TIMEOUT/60000+" min");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append("<tr>");
        sb.append("<td class=name>");
        sb.append("VipAwaytimeout: ");
        sb.append("</td>");
        sb.append("<td class=param>");
        if (Server.srv.VIP_AWAY_TIMEOUT == -1) {
            sb.append("<img src =\"../static/no.gif\">");
        } else if (Server.srv.VIP_AWAY_TIMEOUT == 0) {
            sb.append("same as User");
        } else {
            sb.append(Server.srv.VIP_AWAY_TIMEOUT / 60000 + " min");
        }
        sb.append("</td>");
        sb.append("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Suusers: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_SUUSERS_PER_STARTGROUP);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Groupnamelength: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.MAX_GROUPNAME_LENGTH == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.MAX_GROUPNAME_LENGTH);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Groupthemelength: "); 
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.MAX_GROUPTHEME_LENGTH == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.MAX_GROUPTHEME_LENGTH);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Banduration: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_BAN_DURATION+" min");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Floodbanduration: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.FLOOD_BAN_DURATION+" millis");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Floodprotectmillis: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.FLOOD_PROTECT_MILLIS);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Floodprotecttoleranc: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.FLOOD_PROTECT_TOLERANC);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Toolbanduration: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.TOOL_BAN_DURATION+" millis");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Toolprotectcounter: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.TOOL_PROTECT_COUNTER);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Toolprotecttoleranc: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.TOOL_PROTECT_TOLERANC);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Toolprotectminmills: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.TOOL_PROTECT_MINMILLS);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Toolprotectmincounter: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.TOOL_PROTECT_MINCOUNTER);
		sb.append ("</td>");
		sb.append ("</tr>");
		
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Max Su Banduration: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.MAX_SU_BAN_DURATION == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.MAX_SU_BAN_DURATION).append(" min");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Punish Duration: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.PUNISH_DURATION == -1)
			sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.PUNISH_DURATION /1000).append(" sec");
		sb.append ("</td>");
		sb.append ("</tr>");
        sb.append ("<tr>");
        sb.append ("<td class=name>");
        sb.append ("Lock Duration: ");
        sb.append( "</td>");
        sb.append ("<td class=param>");
        if (Server.srv.MAX_FLOCK_DURATION == -1) {
            sb.append("<img src =\"../static/no.gif\">");
        } else {
            sb.append(Server.srv.MAX_FLOCK_DURATION).append(" min");
        }
        sb.append ("</td>");
        sb.append ("</tr>");
		sb.append ("<td class=name>");
		sb.append ("<b>Use Trafficmonitor:</b> ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.USE_TRAFFIC_MONITOR)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("maxRequestsPerProxy:");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_REQUESTS_PER_PROXY_IP);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("maxRequestsPerIP:");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_REQUESTS_PER_IP);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Startgroups/Theme: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
        String sgroups = Server.srv.props.getProperty("startgroups");
        String sgNames[] = sgroups.split(",");
        for (int i = 0; i < sgNames.length; i++) {
            int pos = sgNames[i].indexOf("/");
            String[] c = sgNames[i].split("/");
            String key = c[0].trim().toLowerCase();
            if (key.equals("exil")) {
                continue;
            }

            if (Server.srv.USE_PLUGINS && Server.srv.serverPlugin!=null) {
                IServerPlugin [] svp = Server.srv.serverPlugin;
                if (svp !=null) { 
                    String gName = null;
                    for (int s = 0; s<svp.length; s++) {   
                        
                        try {   
                            gName =  svp[s].convertGroupname(key, null);   
                        } catch (Exception e) {   
                            Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                        }                  
                    }   
                    sb.append(gName);
                }                 
            } else {
                sb.append(key);
            }

            if (c.length > 1) {
                String theme = sgNames[i].substring(pos + 1);
                if (Server.srv.USE_PLUGINS && Server.srv.serverPlugin!=null) {
                    String gTheme = null;
                    IServerPlugin [] svp = Server.srv.serverPlugin;
                    if (svp !=null) {   
                        for (int s = 0; s<svp.length; s++) {                             
                            try {   
                                gTheme = svp[s].convertGroutheme(key, theme, null);   
                            } catch (Exception e) {   
                                Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                            }   
                        }   
                        sb.append("(").append(gTheme).append(")");
                    }                 
                } else {
                    sb.append("(").append(theme).append(")");
                }

            }
            
            sb.append("</td>");
            if (i < sgNames.length - 1) {
                sb.append("<td class=param>");
            }
        }
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td class=name>");
        sb.append("<b>use Plugins: </>");
        sb.append("</td>");
        sb.append("<td class=param>");
        if (Server.srv.USE_PLUGINS) {
            sb.append("<img src =\"../static/ok.gif\">");
        } else {
            sb.append("<img src =\"../static/no.gif\">");
        }
        sb.append("</td>");
        sb.append("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("<b>use BBC: </>");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.USE_BBC)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");		
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("bbcConvertGroupname: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.BBC_CONVERT_GROUPNAME)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("bbcConvertGrouptheme: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.BBC_CONVERT_GROUPTHEME)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("maxBBCTags: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MAX_BBCTAGS);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>Font</b>RightEntrace: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_FONT_RIGHT_ENTRACE);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>Font</b>RightSepa: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_FONT_RIGHT_SEPA);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>B</b>RightEntrace: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_B_RIGHT_ENTRACE);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>B</b>RightSepa: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_B_RIGHT_SEPA);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>I</b>RightEntrace: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_I_RIGHT_ENTRACE);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>I</b>RightSepa: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_I_RIGHT_SEPA);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>U</b>RightEntrace: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_U_RIGHT_ENTRACE);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("minBbc<b>U</b>RightSepa: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.MIN_BBC_U_RIGHT_SEPA);
		sb.append ("</td>");
		sb.append ("</tr>");
		
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("<b>canDelLogs: </>");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.CAN_DEL_LOGS)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("logfileDelhour: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.LOGFILE_DELHOUR);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("logfileDeldays: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.LOGFILE_DELDAYS);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("<b>Use Fadecolor:</b> ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.USE_FADECOLOR)
		    sb.append ("<img src =\"../static/ok.gif\">");
		else sb.append ("<img src =\"../static/no.gif\">");
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("<b>Colorlock: </b>");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.COLOR_LOCK_MODE == 0)
		    sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.COLOR_LOCK_MODE);
		sb.append ("</td>");
		sb.append ("</tr>");		
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Locklevel: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		sb.append (Server.srv.COLOR_LOCK_LEVEL);
		sb.append ("</td>");
		sb.append ("</tr>");		
		sb.append ("<tr>");
		sb.append ("<td class=name>");
		sb.append ("Fadelocklevel: ");
		sb.append ("</td>");
		sb.append ("<td class=param>");
		if (Server.srv.FADECOLOR_LOCK_LEVEL == -1)
		    sb.append ("<img src =\"../static/no.gif\">");
		else sb.append (Server.srv.FADECOLOR_LOCK_LEVEL);
		sb.append ("</td>");
		sb.append ("</tr>");
		sb.append ("</table>");
	}
	private void renderTemplate(IRequest req, String name, StringBuffer sb) {
		sb.append(renderTemplate(req, name));
	}
	
	private String renderTemplate(IRequest req, String name) {
        TemplateSet ts = Server.srv.templatemanager.getTemplateSet ("admin");
        Template tpl = ts.getTemplate (name);
        if (tpl == null){
            Server.log(this, "File "+name+" not loaded", Server.MSG_ERROR, Server.LVL_MAJOR);
            return "";  
        }
        return tpl.render(req);
	}
    private String may_set_Theme(User u) {
        StringBuffer sb = new StringBuffer("\r\n<td class=name>");
        sb.append("\r\nSet Theme");
        sb.append("\r\n<td class=param>");
        if (u.hasRight(IUserStates.MAY_SET_THEME)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        return sb.toString();       
    }
    
    private String may_use_Smiley(User u) {
        StringBuffer sb = new StringBuffer("\r\n<td class=name>");
        sb.append("\r\nSM");
        sb.append("\r\n<td class=param>");
        if (u.hasRight(IUserStates.MAY_USE_SMILEY)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        return sb.toString();
    }
       
    private String may_call_Memberships(User u) {
        StringBuffer sb = new StringBuffer("\r\n<td class=name>");
        sb.append("\r\nMcall");
        sb.append("\r\n<td class=param>");
        if (u.hasRight(IUserStates.MAY_CALL_MEMBERSHIPS)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        return sb.toString();
    }
    
    private String has_Right(User u,int right,String rname) {
        String displayName = null;
        if (rname.equals("moderator")){
            displayName = "mod";
        } else displayName = rname;
        StringBuffer sb = new StringBuffer("\r\n<td class=name>");
        sb.append(displayName.toUpperCase());
        sb.append("\r\n<td class=param>");
        if (u.hasDefaultRight(right)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td><td class=param>");
        if (u.hasRight(right)) {
            sb.append("\r\n<a href=\"/ADMIN?do=removeright&right=").append(rname).
            append("&name=").append(u.getName().toLowerCase())
              .append("\"><img src =\"../static/ok.gif\" border =\"0\" alt=\"remove\"></a>");
        } else {
            sb.append("\r\n<a href=\"/ADMIN?do=giveright&right=").append(rname).
            append("&name=").append(u.getName().toLowerCase())
              .append("\"><img src =\"../static/no.gif\" border =\"0\" alt=\"give\"></a>");
        }
        sb.append("\r\n</td>");
       return sb.toString();
    }

    private String has_RightAsshole(User u,int right) {
        StringBuffer sb = new StringBuffer("\r\n<td class=name>");
        sb.append("\r\nASSHOLE");
        sb.append("\r\n<td class=param>");
        if (u.hasDefaultRight(right)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td><td class=param>");
        if (u.hasRight(right)) {
            sb.append("\r\n<img src =\"../static/ok.gif\">");
        } else {
            sb.append("\r\n<img src =\"../static/no.gif\">");
        }
        sb.append("\r\n</td>");
        return sb.toString();
    }  


}
