package freecs.commands;

import java.util.Enumeration;

import freecs.content.MessageState;
import freecs.core.UserManager;
import freecs.interfaces.ICommand;

public class CmdRemoveFriend extends AbstractCommand{
    private final String cmd= "/f-";
    private final String version = "1.0";
    private static final ICommand selve=new CmdRemoveFriend ();

    private CmdRemoveFriend () { }
    
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
        if (msgState.sender.numberOfFriends () < 1) {
            msgState.msgTemplate = "error.fl.nofriends";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        msgState.msgTemplate = "message.fl.headline";
        msgState.sender.sendMessage (msgState.mp);
        msgState.useRenderCache = false;
        msgState.sender.removeFriend(param);       
        for (Enumeration<String> e = msgState.sender.friends (); e.hasMoreElements (); ) {
            String uname = (String) e.nextElement ();
            msgState.usercontext = UserManager.mgr.getUserByName (uname); 
            if (msgState.usercontext == null) {
                msgState.msgTemplate = "message.fl.entry.offline";
                msgState.targetGroup = null;
                msgState.param = uname;
            } else { 
                msgState.msgTemplate = "message.fl.entry.online";
                msgState.targetGroup = msgState.usercontext.getGroup ();
                msgState.param = "";
            }
            msgState.sender.sendMessage (msgState.mp);
        }
        msgState.param = String.valueOf (msgState.sender.numberOfFriends ());
        msgState.msgTemplate = "message.fl.count";
        msgState.sender.sendMessage (msgState.mp);
        return false;
    }

}
