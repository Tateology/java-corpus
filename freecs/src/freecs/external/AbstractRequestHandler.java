package freecs.external;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import freecs.Server;
import freecs.content.ContentContainer;
import freecs.interfaces.IRequest;
import freecs.interfaces.IResponseHeaders;
import freecs.util.HttpAuth;

public abstract class AbstractRequestHandler implements IRequestHandler {

	private String handlerName;
	private int securitylevel;
	
	/**
	 * construct a new handler mounted to a given name 
	 */
	public AbstractRequestHandler(String handlerName) {
		this.handlerName = handlerName;
		this.securitylevel = 1;
	}
	
	public void checkAccessIp(IRequest req, ContentContainer c) throws AccessForbiddenException {
		SelectionKey key = req.getKey ();
		InetAddress ia = null;
		try {
			SocketChannel sc = (SocketChannel) key.channel ();
			ia = sc.socket ().getInetAddress ();
		} catch (Exception e) {
			Server.debug (this, "" + ia.toString (), e, Server.MSG_STATE, Server.LVL_MAJOR);
			throw new AccessForbiddenException(true);
		}
		if (!Server.srv.isAdminHost(ia)) {
			Server.log (this, "access to " + getHandlerName() + " denied for " + ia.toString (), Server.MSG_STATE, Server.LVL_MAJOR);
			throw new AccessForbiddenException(true);
		}
	}

	public void checkAccessAuth(IRequest req, ContentContainer c) throws AccessForbiddenException {
		if (Server.srv.ADMIN_HTTP_USERNAME == null || Server.srv.ADMIN_HTTP_PASSWORD == null) {
			Server.log (this, "authentication not properly configured in server config! access denied.", Server.MSG_CONFIG, Server.LVL_MAJOR);
			// hide page if auth is not configured ok!
			throw new AccessForbiddenException(true);
		}
		
		boolean AccessForbidden = true;
		HttpAuth auth = HttpAuth.parse(req.getProperty("authorization"));
		if (auth == null || auth.username == null || auth.password == null) {
			c.setResCode(IResponseHeaders.AUTHENTICATE_CODE);
			c.wrap("Access denied.",req.getCookieDomain());
			throw new AccessForbiddenException(false);
		}
		String [] uNames = Server.srv.ADMIN_HTTP_USERNAME;
		String [] uPw    = Server.srv.ADMIN_HTTP_PASSWORD;
		String [] level  = Server.srv.ADMIN_HTTP_SECLEVEL;
		for (int i = 0; i < uNames.length; i++) {
		    if (auth.username.equals(uNames[i])){
		    	if (uPw[i] !=null && auth.password.equals(uPw[i])){
		    		AccessForbidden=false;
		    		if (level!= null && level[i]!=null)
		    			if (level[i].equals("1") || level[i].equals("2"))
		    		        securitylevel = Integer.valueOf(level[i]).intValue();
		    	}
		    }
		}
		if (AccessForbidden) {
			c.setResCode(IResponseHeaders.AUTHENTICATE_CODE);
			c.wrap("Access denied.",req.getCookieDomain());
			throw new AccessForbiddenException(false);
		}
	}
	
	/**
	 * returns the string which this handler is mounted to
	 */
	public String getHandlerName() {
		return handlerName;
	}
	
	public int slevel(){
		return securitylevel;
	}
}
