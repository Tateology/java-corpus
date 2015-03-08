package freecs.external;

import freecs.content.ContentContainer;
import freecs.interfaces.IRequest;

/**
 * interface to be implemented by classes that handle requests other than
 * for core chat actions (ie admin-pages etc) 
 */
public interface IRequestHandler {
    public abstract String getHandler();
    public abstract String getVersion(); 
    public abstract Object instanceForSystem();
	public void handle(IRequest req, ContentContainer c) throws AccessForbiddenException, Exception;
	 
}
