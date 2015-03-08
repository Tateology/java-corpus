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
package freecs.auth;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;


import freecs.Server;
import freecs.core.User;
import freecs.interfaces.IRequest;

/**
 * @author Stefan Pollach
 */
public class XmlRpcAuthenticator extends AbstractAuthenticator {

	private XmlRpcClient client = null;
	
	public XmlRpcAuthenticator() {
	}

	
	public void init(Properties allProps, String additionalPrefix) {
	    
		super.init(allProps, additionalPrefix);
		Server.log (this, "parsing config", Server.MSG_STATE, Server.LVL_MINOR);
		synchronized (this) {
			try {			
		        // create configuration
			    Server.log(this, "setting XmlRpc Clienturl:"+props.getProperty("server"), Server.MSG_CONFIG, Server.LVL_MAJOR);
		        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		        config.setServerURL(new URL(props.getProperty("server")));
		        config.setEnabledForExtensions(true);
		        config.setConnectionTimeout(60 * 1000);
		        config.setReplyTimeout(60 * 1000);
		        client = new XmlRpcClient();
		        // use Commons HttpClient as transport
		        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		        client.setConfig(config);	
			} catch (MalformedURLException wrongurl) {
				Server.log(this, "can't construct xmlrpc-client because of wrong url: " + wrongurl.toString(), Server.MSG_CONFIG, Server.LVL_MAJOR);
			} catch (Exception ex){
	            Server.log(this, "can't construct xmlrpc-client " + ex, Server.MSG_ERROR, Server.LVL_MAJOR);
			}
		}
	}

	/*
	 * @see freecs.auth.IAuthenticator#shutdown()
	 */
	public void shutdown() throws Exception {
		client = null;
	}

	/*
	 * @see freecs.auth.IAuthenticator#loginUser(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public User loginUser(String usr, String pwd, String userCookie, IRequest request)
			throws Exception {

        // change cookie value to check if configured
        String cookie = props.getProperty("cookie");
        String checkCookie = (cookie != null && !cookie.trim().equals("")) ? request.getProperty("c_" + cookie) : userCookie; 

        // xmlrpc doesn't know null values, so make sure
        // transmitted values are empty strings
        pwd         = (pwd == null)        ? "" : pwd;
        checkCookie = (checkCookie == null) ? "" : checkCookie;

        Vector<Serializable> params = new Vector<Serializable>();
        if ("session".equalsIgnoreCase(props.getProperty("check"))) {
            params.add(checkCookie);
        } else {
            params.add(usr);
            params.add(pwd);
        }
        Object result = null;
        if (client != null) {
            try {
                result = client.execute(props.getProperty("loginMethod"),params);
            } catch (XmlRpcException xmlrpcEx) {
                // errors thrown at the remote server (handler or function not
                // available etc)
                Server.debug(this, props.getProperty("server") + "/" + props.getProperty("loginMethod")+ " reports an xmlrpc-error: ", xmlrpcEx, Server.MSG_AUTH, Server.LVL_MAJOR);
                return null;
            } catch (Exception ex) {
                // lower level errors, network etc
                Server.debug(this, "xmlrpc-request to "
                        + props.getProperty("server") + "/"
                        + props.getProperty("loginMethod") + " failed: ", ex,
                        Server.MSG_AUTH, Server.LVL_MAJOR);
                return null;
            }
        } else {
            Server.log(this, "client == NULL Property loginMethod?", Server.MSG_ERROR,Server.LVL_MAJOR);
        }

        if (result == null || result == Boolean.FALSE) {
            // invalid return data
            return null;
        }

        if (!(result instanceof Hashtable<?, ?>)) {
            if (!"1".equals(result)
                    && !"true".equals(result)
                    && !Boolean.TRUE.equals(result))
                return null;
            return new User (usr, userCookie);
        }
        Hashtable<?, ?> userdata = (Hashtable<?, ?>) result;
        
        if (userdata.containsKey("errorcode")) {
        	// check error code returned by remote function
        	int errorCode = ((Integer)userdata.get("errorcode")).intValue();
        	if (errorCode>0) {
        		Server.log(this, "login failed as " + props.getProperty("server") + " returned code " + errorCode, Server.MSG_AUTH, Server.LVL_VERBOSE);
        		return null;
        	}
        }
        String remoteUsername = (String) userdata.get("username");
        if (!usr.equalsIgnoreCase(remoteUsername)) {
            // username given as URL-Parameter doesn't match the username retrieved via XML-RPC
            return null;
        }
        
        User u = new User (usr, userCookie);
        u.isUnregistered = false;

        if (userdata.containsKey("color")) {
            u.setColCode((String)userdata.get("color"));
        }
        if (userdata.containsKey("fadecolor")) {
            u.setFadeColCode((String)userdata.get("fadecolor"));
        }
        if (userdata.containsKey("chattime")) {
            u.setProperty("chattime", Long.valueOf((String)
            userdata.get("chattime")));
        }
        if (userdata.containsKey("id")) {
        	Object obj = userdata.get("id");
        	if (obj instanceof Number) {
        		u.setID(((Number)obj).toString());
        	} else if (obj instanceof String) {
        		u.setID((String)obj);
        	}
        }
        if (userdata.containsKey("lastlogin")) {
        	Object obj = userdata.get("lastlogin");
        	if (obj instanceof Number) {
        		u.setProperty("lastlogin", new Timestamp(((Number)obj).longValue()));
        	} else if (obj instanceof Date) {
        		u.setProperty("lastlogin", new Timestamp(((Date)obj).getTime()));
        	}
        }
        if (userdata.containsKey("friendslist")) {
        	List<String> users = parseUserList((String)userdata.get("friendslist"));
            for (Iterator<String> i = users.iterator(); i.hasNext(); ) {
                u.addFriend((String) i.next());
            }
        }
        if (parseBoolean(userdata.get("blocked"))==true) {
        	u.blocked = true;
        }
        if (parseBoolean(userdata.get("activated"))==true){
        	u.activated = true;
        }
        return (u);
	
	}

    public User loginUser(User u, String username, String password, IRequest request) throws Exception {
    	return u;
	}

	
	/*
	 * @see freecs.auth.IAuthenticator#logoutUser(freecs.core.User)
	 */
	public void logoutUser(User u) throws Exception {
	    String logoutMethod = props.getProperty("logoutMethod");
        if (logoutMethod == null
                || logoutMethod.length() <1)
            return;
		Hashtable<String, Object> userdata = new Hashtable<String, Object>();
		userdata.put("username", u.getName());
		userdata.put("chattime", new Integer((new Long(u.getChattime()).intValue())) );
		userdata.put("color", u.getColCode());
		if (Server.srv.USE_FADECOLOR)
		    userdata.put("fadecolor", u.getFadeColCode());
        userdata.put("cookie", u.getCookie());
        if (props.containsKey("cookie")) {
            String cookieName = props.getProperty("cookie");
            userdata.put("c_" + cookieName, u.getProperty("c_" + cookieName));
        }

		Vector<Serializable> params = new Vector<Serializable>();
        params.add(userdata);
        try {
            @SuppressWarnings("unused")
			Object result = client.execute(logoutMethod, params);
        } catch (XmlRpcException xmlrpcEx) {
        	// errors thrown at the remote server (handler or function not available etc)
        	Server.debug(this, props.getProperty("server") + "/" + props.getProperty("logoutMethod") + " reports an xmlrpc-error: ", xmlrpcEx, Server.MSG_AUTH, Server.LVL_MAJOR);
        } catch (Exception ex) {
        	// lower level errors, network etc
            Server.debug(this, "xmlrpc-request to " + props.getProperty("server") + "/" + props.getProperty("logoutMethod") + " failed: ", ex, Server.MSG_AUTH, Server.LVL_MAJOR);
        }
	}


}