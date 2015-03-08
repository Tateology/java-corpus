package freecs.external.xmlrpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import freecs.Server;
import freecs.interfaces.IXmlRpcHandler;

public class XmlRpcManager {

    public static final XmlRpcManager mgr = new XmlRpcManager();
    private WebServer webServer;
   

    /**
     * wrap instance's start method.
     * start the xmlrpc-server using values from server-config. 
     */
    public static void startManager(){
        mgr.start();
    }
    
    private XmlRpcManager() {
    }

    private void start() {
        webServer = new WebServer(Server.srv.ADMIN_XMLRPC_PORT);
        webServer.setParanoid(true);
        
        StringBuffer sb = new StringBuffer("Started XmlRpc-Server on port " + Server.srv.ADMIN_XMLRPC_PORT + ", open for addresses: ");
        // parse list of allowed clients:
        StringTokenizer tok = new StringTokenizer(Server.srv.ADMIN_XMLRPC_ALLOWED, ",");
        while (tok.hasMoreTokens()) {
            String ip = tok.nextToken().trim();
            webServer.acceptClient(ip);
            sb.append (ip + ", ");
        }
        
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
      
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        HashMap<String, Object> handler = Server.srv.xmlRpcHandler;
        
        for (Iterator<String> i= handler.keySet ().iterator (); i.hasNext() ;) {
            String key = (String) i.next();
            
            IXmlRpcHandler hdl = (IXmlRpcHandler) handler.get(key);   
            try {
                phm.addHandler(key, hdl.getClass());
                Server.log(this, "added XML RPC Handler "+key+" ("+hdl.getClass().toString()+")", Server.MSG_CONFIG, Server.LVL_MAJOR);
            } catch (XmlRpcException e) {
                Server.log(this, e.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
            }
        }

        try {
            phm.addHandler("freecsXmlRpcHandler", freecs.external.xmlrpc.freecsXmlRpcHandler.class);
            Server.log(this, "added default XML RPC Handler ", Server.MSG_CONFIG, Server.LVL_MAJOR);
        } catch (XmlRpcException e) {
            Server.log(this, e.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
        }
       
        xmlRpcServer.setHandlerMapping(phm);
      
        XmlRpcServerConfigImpl serverConfig =(XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        try {
            webServer.start();
        } catch (IOException e) {
            Server.log(this, e.toString(), Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        Server.log(this, sb.toString().substring(0, sb.toString().length()-2), Server.MSG_CONFIG, Server.LVL_MINOR);
    }
}
