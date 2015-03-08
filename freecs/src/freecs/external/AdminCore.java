package freecs.external;

import java.net.InetAddress;

import freecs.Server;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.MessageParser;
import freecs.core.User;
import freecs.core.UserManager;


/**
 * class that wraps the actual admin commands and should be used
 * by the different admin access classes (xmlrpc, webinterface etc)
 */
public class AdminCore {

	/**
	 *  wraps the kick user and block ip methods
	 */
	public static boolean kick(String username, long timeout, boolean blockIp, String msg) {
		User usr = UserManager.mgr.getUserByName(username);
		if (usr == null) {
			return false;
		}
		Server.srv.banUser(usr, msg, timeout, "admin");		
		if (blockIp == true) {
			InetAddress addr = usr.conn.peerAddress;
			Server.srv.banHost(addr, timeout, msg);
		}
		return true;
	}

	/**
	 * removes a user from the chat room politely
	 */
	public static boolean removeUser(String username) {
		User usr = UserManager.mgr.getUserByName(username);
		if (usr != null) {
            usr.sendQuitMessage(false, null);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * sends a system message to all users of the chat
	 */
	public static boolean messageToAll(String msg) {
        MessageParser mpr = new MessageParser();
        mpr.setMessageTemplate("message.sys");
        mpr.setMessage(msg);
        UserManager.mgr.sendMessage(mpr);
        return true;
	}

	/**
	 * sends a system message to all users a given group
	 */
	public static boolean messageToGroup(String msg, String groupname) {
		MessageParser mpr = new MessageParser();
		mpr.setMessageTemplate("message.sys");
		mpr.setMessage(msg);
		
		Group grp = GroupManager.mgr.getGroup(groupname);
		if (grp != null) {
			grp.sendMessage(mpr);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * sends a system message to a single user
	 */
	public static boolean messageToUser(String msg, String username) {
		MessageParser mpr = new MessageParser();
		mpr.setMessageTemplate("message.sys");
		mpr.setMessage(msg);
		
		User usr = UserManager.mgr.getUserByName(username);
		if (usr != null) {
			usr.sendMessage(mpr);
			return true;
		} else {
			return false;
		}
	}

}
