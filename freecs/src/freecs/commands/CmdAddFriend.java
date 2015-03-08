package freecs.commands;

import java.util.Enumeration;

import freecs.auth.AuthManager;
import freecs.content.MessageState;
import freecs.core.UserManager;
import freecs.interfaces.ICommand;

public class CmdAddFriend extends AbstractCommand {
    private final String cmd= "/f+";
    private static final ICommand selve=new CmdAddFriend ();
    private final String version = "1.0";

    private CmdAddFriend () { }
    
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
       msgState.msgTemplate = "message.fl.headline";
        msgState.sender.sendMessage (msgState.mp);
        msgState.useRenderCache = false;
        if (AuthManager.instance.isValidName(param) && !msgState.sender.getName().equalsIgnoreCase(param)){
            msgState.sender.addFriend(param);
        }
        if (msgState.sender.numberOfFriends () < 1) {
            msgState.msgTemplate = "error.fl.nofriends";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
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
