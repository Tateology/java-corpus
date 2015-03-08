package freecs.external;


import freecs.commands.CommandSet;
import freecs.content.ContentContainer;
import freecs.content.MessageState;
import freecs.core.MessageParser;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.external.AbstractRequestHandler;
import freecs.external.AccessForbiddenException;
import freecs.external.IRequestHandler;
import freecs.interfaces.ICommand;
import freecs.interfaces.IRequest;


public class CmdConfirmHandler extends AbstractRequestHandler {
    
    private static final String handler= "/cmdconfirm";
    private final String version = "1.04";
    
    private static final IRequestHandler selve= new CmdConfirmHandler(handler);

    
    public static IRequestHandler getHandlerInstance () {
        return selve;
    }
       
    public Object instanceForSystem() {
        return this;
    }
    
    public String getHandler() {
        return handler;
    }
    public CmdConfirmHandler(String handlerName) {
        super(handlerName);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

	

	/**
	 * class rendering 
	 */
	
	public void handle(IRequest req, ContentContainer c) throws AccessForbiddenException {
		User u = UserManager.mgr.getUserByCookie(req.getCookie());
		String param = null;
		if (req.getValue("param") != null) {
			param =req.getValue("param");
		}
		StringBuffer command = new StringBuffer("/");
		if (req.getValue("cmd") != null) {
			command.append((req.getValue("cmd")));
		}

		boolean confirm = true;
		if (req.getValue("confirm") != null) {
			if (req.getValue("confirm").equals("false")){
				confirm = false;
			} 
		}
				
		if (u==null || req.getValue("confirm") == null || req.getValue("cmd") == null || req.getValue("param") == null)
		    throw new AccessForbiddenException(true);
		
		if (confirm){
		    MessageParser mpr = new MessageParser();
            MessageState msgState = mpr.getMessageState();           
            msgState.sender = u;
            ICommand ic = CommandSet.getCommandSet().getCommand(command.toString());
            if (ic.execute(msgState, param)){}
		} else {
            MessageParser mpr = new MessageParser();
            MessageState msgState = mpr.getMessageState();           
            msgState.sender = u;
            if (command.toString().equals("/sepa")){
                StringBuffer p = new StringBuffer("sepa");
                p.append(" ");
                p.append(param);
		        ICommand ic = CommandSet.getCommandSet().getCommand("/m");
		        if (ic.execute(msgState, p.toString())){
		            msgState.sender.removeConfirmAction("/sepa");
		        }
            } else if (command.toString().equals("/q")){
                StringBuffer p = new StringBuffer("q");
                p.append(" ");
                p.append(param);
                ICommand ic = CommandSet.getCommandSet().getCommand("/m");
                if (ic.execute(msgState, p.toString())){
                    msgState.sender.removeConfirmAction("/q");
                }
            } else if (command.toString().equals("/jclosed")){
                StringBuffer p = new StringBuffer("jclosed");
                p.append(" ");
                p.append(param);
                ICommand ic = CommandSet.getCommandSet().getCommand("/m");
                if (ic.execute(msgState, p.toString())){
                    msgState.sender.removeConfirmAction("/jclosed");
                }
            } else if (command.toString().equals("/ju")){
                StringBuffer p = new StringBuffer("ju");
                p.append(" ");
                p.append(param);
                ICommand ic = CommandSet.getCommandSet().getCommand("/m");
                if (ic.execute(msgState, p.toString())){
                    msgState.sender.removeConfirmAction("/ju");
                }
            } else if (command.toString().equals("/j")){
                StringBuffer p = new StringBuffer("j");
                p.append(" ");
                p.append(param);
                ICommand ic = CommandSet.getCommandSet().getCommand("/m");
                if (ic.execute(msgState, p.toString())){
                    msgState.sender.removeConfirmAction("/j");
                }
            } else if (command.toString().equals("/away")){
                StringBuffer p = new StringBuffer("away");
                p.append(" ");
                p.append(param);
                ICommand ic = CommandSet.getCommandSet().getCommand("/m");
                if (ic.execute(msgState, p.toString())){
                    msgState.sender.removeConfirmAction("/away");
                }
            }
		}
		c.wrap ("", req.getCookieDomain());
	}
}
