package freecs.external;

import freecs.content.ContentContainer;
import freecs.interfaces.IRequest;

public class UserlistRequestHandler extends AbstractRequestHandler {
    private static final String handler= "/userlist";
    private final String version = "1.0";
    
    private static final IRequestHandler selve= new UserlistRequestHandler(handler);

    
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
	
	public UserlistRequestHandler(String handlerName) {
		super(handlerName);
	}


	/**
	 * class rendering who's online lists
	 */
	
	public void handle(IRequest req, ContentContainer c) throws AccessForbiddenException {
		checkAccessIp(req, c);		
		StringBuffer show = new StringBuffer(""), as = new StringBuffer("");
		if (req.getValue("show") != null)
		    show = new StringBuffer(req.getValue("show"));
		if (req.getValue("as") != null)
		    as   = new StringBuffer(req.getValue("as"));
		boolean xml = ("xml".equalsIgnoreCase(as.toString())) ? true : false;		
        
		StringBuffer sb = new StringBuffer(UserlistManager.mgr.getUserlist(show, c, req, xml));			
		sb.trimToSize();
		c.wrap(sb.toString(),req.getCookieDomain());
	}
	
	
	
	
	public String toString() {
        return ("[UserlistRequestHandler]");
    }
}
