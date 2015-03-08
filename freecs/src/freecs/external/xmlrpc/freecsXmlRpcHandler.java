/**
 * Copyright (C) 2009  Rene M.
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
package freecs.external.xmlrpc;




import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.xmlrpc.XmlRpcException;

import freecs.Server;

import freecs.core.RequestReader;
import freecs.core.UserManager;
import freecs.external.AdminCore;
import freecs.interfaces.IXmlRpcHandler;
/**
 * @author Rene M.
 */

public class freecsXmlRpcHandler implements IXmlRpcHandler { 
    private final String handler ="freecsXmlRpcHandler";
    private final String version = "1.0";

    private static final IXmlRpcHandler selve = new freecsXmlRpcHandler();
    public freecsXmlRpcHandler(){}
    public static IXmlRpcHandler getInstance () {
        return selve;
    }
       
    public Object instanceForSystem() {
        return checkVersion();
    }
    
    private freecsXmlRpcHandler checkVersion(){
        String freecsversion  = Server.getVersion().substring(4, 12);
        int v = new Integer(freecsversion).intValue();
        if (v >= 20091030){
            return this;
        }
        Server.log(this, "Wrong Chatversion", Server.MSG_ERROR, Server.LVL_MAJOR);
        return null;
    }

    public String getHandlername() {
        return handler;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }


    /**
     * kick a user from the chat.
     * freecsXmlRpcHandler.kick(username, timeout, blockIp)
     */
    public boolean kick(String username, long timeout, boolean blockIp ) throws Exception {
        if (username==null)
            throw new XmlRpcException (0, "illegal arguments for freecsXmlRpcHandler.messageToGroup");
 
        return AdminCore.kick(username, timeout, blockIp, "xmlrpc-remote");
    }
    
    /**
     * removes a user from the chat (the polite version)
     * freecsXmlRpcHandler.removeUser(username)
     */
    public boolean removeUser(String username) throws Exception {
        if (username==null)
            throw new XmlRpcException (0, "illegal arguments for freecsXmlRpcHandler.messageToGroup");
 
        return AdminCore.removeUser(username);
    }
    
    /**
     * sends a system message to all users of the chat
     * freecsXmlRpcHandler.messageToAll(message)
     */
    public boolean messageToAll(String msg) throws Exception {
        if (msg==null)
            throw new XmlRpcException (0, "illegal arguments for freecsXmlRpcHandler.messageToGroup");
 
         return AdminCore.messageToAll(msg);
    }

    /**
     * sends a system message to all users of a given group
     * freecsXmlRpcHandler.messageToAll(message, groupname)
     */
    public boolean messageToGroup(String msg,String groupname ) throws Exception {
        if (msg==null || groupname==null)
            throw new XmlRpcException (0, "illegal arguments for freecsXmlRpcHandler.messageToGroup");
        
        
        return AdminCore.messageToGroup(msg, groupname);
    }

    /**
     * sends a system message to a single user
     * freecsXmlRpcHandler.messageToUser(message, username)
     */
    public boolean messageToUser(String msg, String username) throws Exception {
        if (msg==null || username==null)
            throw new XmlRpcException (0, "illegal arguments for freecsXmlRpcHandler.messageToUser");

        Server.log(this, "messageToUser: " + msg+"/"+username, Server.MSG_STATE, Server.LVL_MAJOR);
        return AdminCore.messageToUser(msg, username);
    }

    public Hashtable<Object,Object> state(String param) {
        Hashtable<Object,Object> result = new Hashtable<Object,Object>();
        result.put("error", "");
        result.put("freeThreads", new Integer(Server.srv.MAX_READERS
                - RequestReader.activeReaders()));
        result.put("maxThreads", new Integer(Server.srv.MAX_READERS));
        result.put("usersHighWaterMark", new Integer(UserManager.mgr
                .getHighWaterMark()));
        result.put("users", new Integer(UserManager.mgr.getActiveUserCount()));
        Runtime r = Runtime.getRuntime();
        result.put("vmFreeMem", new Integer(Math.round(r.freeMemory() / 1024)));
        result.put("vmTotalMem",
                new Integer(Math.round(r.totalMemory() / 1024)));
        // FIXME: at the moment only for linux
        Hashtable<Object,Object> lomd = getLinuxMemoryDetails(r);
        if (lomd != null) {
            result.putAll(lomd);
        }
        return result;
    }
    
    private Hashtable<Object,Object>  getLinuxMemoryDetails(Runtime r) {
        
        try {
            // systemcall for checking memory
            Process p = r.exec("cat /proc/meminfo");
            try {
                int returnCode = p.waitFor();
                if (returnCode>0) {
                    // return ht;
                    return null;
                }
            } catch (InterruptedException ie) {}

            // read in stdout and stderr
            StringBuffer out = new StringBuffer();
            StringBuffer err = new StringBuffer();
            InputStream os = p.getInputStream();
            int b = os.read();
            while (b != -1) {
                out.append((char) b);
                b = os.read();
            }
            os = p.getErrorStream();
            b = os.read();
            while (b != -1) {
                err.append((char) b);
                b = os.read();
            }

            Hashtable<Object,Object>  ht = new Hashtable<Object,Object> ();
            // if stderr isn't empty return the error
            if (err.length() > 0) {
                Server.log("XmlRpc", "error while getting System-Memory-Information: " + err.toString(), Server.MSG_ERROR, Server.LVL_MINOR);
                ht.put("error", err.toString());
                return ht;
            }
            
            // parse the content of /proc/meminfo
            StringTokenizer st = new StringTokenizer(out.toString(), "\r\n");
            String props[] = st.nextToken().trim().split(":");
            StringTokenizer vst = new StringTokenizer(st.nextToken(), " ");
            String prefix = vst.nextToken().trim();
            int i = 0;
            while (vst.hasMoreTokens() && i < props.length) {
                props[i]=props[i].trim();
                ht.put(prefix + props[i], new Integer(Integer.parseInt(vst.nextToken())));
                i++;
            }
            vst = new StringTokenizer(st.nextToken(), " ");
            prefix = vst.nextToken().trim();
            i = 0;
            while (vst.hasMoreTokens() && i < props.length) {
                ht.put(prefix + props[i], new Integer(Integer.parseInt(vst.nextToken())));
                i++;
            }
            
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                int idx = line.indexOf(":");
                if (idx < 0)
                    continue;
                String key = line.substring(0, idx);
                Integer iObj = new Integer (
                        Integer.parseInt(
                                line.substring(idx+1, line.length()-3).trim()));
                ht.put(key, iObj);
            }
            return ht;
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
}
