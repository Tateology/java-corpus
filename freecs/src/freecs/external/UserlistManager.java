package freecs.external;

import freecs.Server;
import freecs.content.ContentContainer;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.IGroupState;
import freecs.interfaces.IRequest;
import freecs.interfaces.IServerPlugin;
import freecs.interfaces.IUserStates;
import freecs.util.HtmlEncoder;

public class UserlistManager {
	public static final UserlistManager mgr = new UserlistManager();
	
	public StringBuffer getUserlist(StringBuffer show, ContentContainer c, IRequest req,boolean xml){
		StringBuffer sb = new StringBuffer();
		if (xml) {
			c.setContentType("text/xml");
			sb.append (UserlistManager.mgr.xmlHeader);
		} else {
			c.setContentType("text/plain");
		}
		
		if ("allusers".equalsIgnoreCase(show.toString())) {
			Group[] groups = GroupManager.mgr.currentGroupList();
			for (int i=0; i<groups.length; i++) {
				if (xml) {
					renderGroupAsXml(sb, groups[i]);
					renderGroupUsersAsXml(sb, groups[i]);
					sb.append ("</room>\n");
				} else {
					renderGroupAsText(sb, groups[i]);
					renderGroupUsersAsText(sb, groups[i]);
					sb.append ("\n");
				}
			}
		} else if ("allgroups".equalsIgnoreCase(show.toString())) {
			Group[] groups = GroupManager.mgr.currentGroupList();
			for (int i=0; i<groups.length; i++) {
				if (xml) {
					renderGroupAsXml(sb, groups[i]);
					sb.append ("</room>\n");
				} else {
					renderGroupAsText(sb, groups[i]);
					sb.append("\n");
				}
			}
		} else if (req.getValue("group") != null) {
			Group g = GroupManager.mgr.getGroup(req.getValue("group"));
			if (g != null && xml) {
				renderGroupAsXml(sb, g);
				renderGroupUsersAsXml(sb, g);
				sb.append ("</room>");
			} else if (g != null) {
				renderGroupAsText(sb, g);
				renderGroupUsersAsText(sb, g);
			}
		} else if (req.getValue("user") != null) {
			User u = UserManager.mgr.getUserByName(req.getValue("user"));
			if (u != null && xml) {
				renderUserAsXml(sb, u);
			} else if (u != null) {
				renderUserAsText(sb, u);
			}
		} else {
			int activeUsers = UserManager.mgr.getActiveUserCount();
			int openGroups  = GroupManager.mgr.openGroupsCount();
			if (xml) {
				sb.append ("<users>");
				sb.append (activeUsers);
				sb.append ("</users>\n");
				sb.append ("<rooms>");
				sb.append (openGroups);
				sb.append ("</rooms>\n");
			} else {
				sb.append (activeUsers);
				sb.append ("\n");
				sb.append (openGroups);
				sb.append ("\n");
			}
		}
		if (xml) {
			sb.append (UserlistManager.mgr.xmlFooter);
		}

		return sb;
	}
	
	private void renderGroupAsText(StringBuffer sb, Group g) {
		if (g.hasState(IGroupState.OPEN)) {
			sb.append("open: ");
		} else {
			sb.append("closed: ");
		}
		sb.append (g.getRawName());
		sb.append ("\n");
		String theme = g.getTheme();
		if (theme != null && !"".equals(theme.trim())) {
			sb.append ("theme: " + theme);
			sb.append ("\n");
		}
		
	}
	
	private void renderGroupAsXml(StringBuffer sb, Group g) {
		sb.append ("<room name=\"");
		StringBuffer convert = new StringBuffer(g.getRawName());
        if (Server.srv.USE_PLUGINS){
            IServerPlugin [] svp = Server.srv.serverPlugin;
            if (svp !=null) {   
                for (int s = 0; s<svp.length; s++) {   
                    String grp = null;
                    try {   
                        if (svp[s].canConvert(g)){
                            if (g.getOpener() != null){
                                User o =  g.getOpener();
                                if (o instanceof User){
                                    if (!o.isUnregistered)
                                        grp = svp[s].convertGroupname(convert.toString(), o.getBlockedServerPlugins());
                                }
                            } else  grp = svp[s].convertGroupname(convert.toString(), null);
                        }
                    } catch (Exception e) {   
                        Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                    }   
                    if (grp != null)
                        convert = new StringBuffer(grp);
                }   
            }                 
        }
        HtmlEncoder.encodeXml(convert.toString(), sb);
		convert=null;
		sb.append ("\"");
		sb.append (" theme=\"");
		if (g.getTheme() != null) {
			convert = new StringBuffer(g.getTheme());
	        if (Server.srv.USE_PLUGINS){
                IServerPlugin [] svp = Server.srv.serverPlugin;
	            if (svp !=null) {   
	                for (int s = 0; s<svp.length; s++) {   
	                    String theme = null;
	                    try {   
	                        if (svp[s].canConvert(g) ){
	                            if (g.getThemeCreator() != null){
	                                User o =  g.getThemeCreator();
	                                if (o instanceof User){
	                                    if (!o.isUnregistered)
	                                        theme = svp[s].convertGroutheme(g.getName(), convert.toString(), o.getBlockedServerPlugins());   
	                                }
	                            } else theme = svp[s].convertGroutheme(g.getName(), convert.toString(), null);   
	                         }
	                    } catch (Exception e) {   
	                        Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
	                    }   
	                    if (theme != null)
	                        convert = new StringBuffer(theme);
	                }   
	            }                 
	        }
            HtmlEncoder.encodeXml(convert.toString(), sb);
		}
		sb.append ("\"");
		sb.append(" open=\"");
		sb.append (g.hasState(IGroupState.OPEN));
		sb.append ("\">");
	}

	private void renderUserAsText(StringBuffer sb, User u) {
		sb.append (u.getName());
        if (u.hasRole(IUserStates.ROLE_VIP)) {
            sb.append (" (vip) ");
        } else if (u.hasRole(IUserStates.ROLE_GOD)) {
            sb.append (" (admin) ");
        } else if (u.getGroup().usrIsSu(u)) {
            sb.append (" (su) ");
        } else {
            sb.append (" (user) ");
        }
        sb.append (u.getChattime());
        sb.append (" ");
        sb.append (u.getGroup().getRawName());
        sb.append (" ");
        if (u.isAway()) {
        	sb.append ("away(");
        	sb.append (u.getAwayMessage());
            sb.append (")");
        }
        if (Server.srv.USE_FADECOLOR){
        	sb.append ("fadecolor(");
        	sb.append (u.getFadeColCode());
            sb.append (")");
        }
        sb.append ("\n");
	}
	
	private void renderUserAsXml(StringBuffer sb, User u) {
		sb.append ("<user name=\"");
		HtmlEncoder.encodeXml(u.getName(), sb);
		sb.append ("\" id=\"");
		sb.append (u.getID());
		sb.append ("\" roomname=\"");
		StringBuffer convert = new StringBuffer(u.getGroup().getRawName());
        if (Server.srv.USE_PLUGINS){
            IServerPlugin [] svp = Server.srv.serverPlugin;
            if (svp !=null) {   
                for (int s = 0; s<svp.length; s++) {   
                    String grp = null;
                    try {   
                        if (svp[s].canConvert(u))
                            grp = svp[s].convertGroupname(convert.toString(), u.getBlockedServerPlugins());   
                    } catch (Exception e) {   
                        Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                    }   
                    if (grp != null)
                        convert = new StringBuffer(grp);
                }   
            }                 
        }
        HtmlEncoder.encodeXml(convert.toString(), sb);
		convert=null;
		sb.append ("\" lastActive=\"");
		sb.append (u.lastActive());
		sb.append ("\" away=\"");
		sb.append (u.isAway());
		if (u.isAway()) {
			sb.append ("\" awaymessage=\"");
			convert = new StringBuffer(u.getAwayMessage());
	        if (Server.srv.USE_PLUGINS){
                IServerPlugin [] svp = Server.srv.serverPlugin;
	            if (svp !=null) {   
	                for (int s = 0; s<svp.length; s++) {   
	                    String message = null;
	                    try {   
	                        if (svp[s].canConvert(u))
	                            message = svp[s].convertAwaymessage(convert.toString(), u.getBlockedServerPlugins());   
	                    } catch (Exception e) {   
	                        Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
	                    }   
	                    if (message != null)
	                        convert = new StringBuffer(message);
	                }   
	            }                 
	        }
            HtmlEncoder.encodeXml(convert.toString(), sb);
		}
		sb.append ("\" chattime=\"");
        sb.append (u.getChattime());
		sb.append ("\" punished=\"");
		sb.append (u.isPunished());
		if (u.hasRole(IUserStates.ROLE_VIP)) {
            sb.append ("\" vip=\"true");
		} else if (u.hasRole(IUserStates.ROLE_GOD)) {
            sb.append ("\" admin=\"true");
		} else if (u.getGroup()!= null 
				&& u.getGroup().usrIsSu(u)) {
            sb.append ("\" superuser=\"true");
		}
        if (u.getGroup() != null && u.getGroup().hasState(IGroupState.MODERATED)) {
            if (u.hasRight(IUserStates.IS_GUEST)) {
                sb.append ("\" guest=\"true");
            }
            if (u.hasRight(IUserStates.IS_MODERATOR)) {
                sb.append ("\" moderator=\"true");
            }
        }
		sb.append ("\" color=\"");
		sb.append (u.getColCode());
		if (Server.srv.USE_FADECOLOR){
			sb.append ("\" fadecolor=\"");
			sb.append (u.getFadeColCode());
			sb.append ("\" fadecolorusername=\"");
			if (u.getFadeColorUsername() != null){
			    sb.append (HtmlEncoder.encodeXml(u.getFadeColorUsername().toString()));
			} else {
				sb.append("");
			}
		}
		sb.append ("\" />\n");
	}
	
	private void renderGroupUsersAsText(StringBuffer sb, Group g) {
		User[] users = g.getUserArray();
		for (int i=0; i<users.length; i++) {
			renderUserAsText(sb, users[i]);
		}
	}
	
	
	private void renderGroupUsersAsXml(StringBuffer sb, Group g) {
		User[] users = g.getUserArray();
		for (int i=0; i<users.length; i++) {
			renderUserAsXml(sb, users[i]);
		}
	}
	
	private String xmlHeader = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<lxml>\n";
	private String xmlFooter = "\n</lxml>\n\n";

	
}