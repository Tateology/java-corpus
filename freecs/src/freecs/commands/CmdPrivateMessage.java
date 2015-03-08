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
 * Created on 28.09.2003
 */

package freecs.commands;
/**
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import freecs.Server;
*/
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;
import freecs.content.MessageState;
import freecs.core.UserManager;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdPrivateMessage extends AbstractCommand {
	private final String cmd= "/m";
	private final String version = "1.01";
	private static final ICommand selve=new CmdPrivateMessage();

	private CmdPrivateMessage () { }
	
	public static ICommand getInstance () {
		return selve;
	}
	   
    public Object instanceForSystem() {
        return this;
    }
    
    public String getCmd() {
        return cmd;
    }

	/**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    public boolean execute (MessageState msgState, String param) {
        if (param.length () < 1) {
            msgState.msgTemplate = "error.m.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
		}
		int pos = param.indexOf (" ");
		if (pos == -1) {
            msgState.usercontext = UserManager.mgr.getUserByName (param);
            if (msgState.usercontext != null) { 
                msgState.msgTemplate = "error.m.noMessage";
                msgState.sender.sendMessage (msgState.mp);
            } else {
                msgState.param = param;
                msgState.msgTemplate = "error.user.notOnServer.singular";
                msgState.sender.sendMessage(msgState.mp);
            }
            return false;
		}
		msgState.usercontext = getUser (msgState, param.substring (0, pos));
		if (msgState.usercontext == null)
            return false;
        if (msgState.sender.whisperDeactivated() && !msgState.usercontext.hasRight(IUserStates.ROLE_VIP)){
            return false;
        }
		
        if (cantHearYou(msgState, false))
            return false;
		msgState.message = param.substring (pos).trim ();
		sendPrivateMessage (msgState, msgState.usercontext, msgState.message);
        messageLog(msgState, msgState.usercontext,"PrivatMessage");
		return true;
	}
    /**
    class XmlRpcConnection {
        private XmlRpcConnection() {
            
        }
        private Object sendMessageToMailbox(String sender, String user,String message){
            XmlRpcClient client = generateConnection();
            Vector <String> params = new Vector <String>();
            params.add(sender);
            params.add(user);
            params.add(message);
            
            Object result = null;
            if (client != null) {
                try {
                    result = client.execute(Server.srv.getProperty("mailMethod"),params);
                } catch (XmlRpcException xmlrpcEx) {
                    // errors thrown at the remote server (handler or function not
                    // available etc)
                    Server.debug(this, Server.srv.getProperty("xmlRpcServer") + "/" + Server.srv.getProperty("mailMethod")+ " reports an xmlrpc-error: ", xmlrpcEx, Server.MSG_ERROR, Server.LVL_MAJOR);
                    return false;
                } catch (Exception ex) {
                    // lower level errors, network etc
                    Server.debug(this, "xmlrpc-request to "
                            + Server.srv.getProperty("xmlRpcServer") + "/"
                            + Server.srv.getProperty("mailMethod") + " failed: ", ex,
                            Server.MSG_ERROR, Server.LVL_MAJOR);
                    return false;
                }
            } else {
                Server.log(this, "client == NULL Property loginMethod?", Server.MSG_ERROR,Server.LVL_MAJOR);
            }
            return result;
        }
        
        private XmlRpcClient generateConnection(){
            XmlRpcClient client = null;
            try {           
                // create configuration
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL(Server.srv.getProperty("xmlRpcServer")));
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
            return client;
        }
    }*/
}
