package freecs.core;

import java.util.Vector;

import freecs.Server;
import freecs.content.MessageState;
import freecs.core.User;
import freecs.interfaces.IContainer;
import freecs.interfaces.IGroupPlugin;
import freecs.interfaces.IServerPlugin;

/**
 * @author rm
 *
 */
public class PluginRenderer {
    
    
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer ceckGroupTheme(StringBuffer convert,MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) {  
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                StringBuffer theme = null;
                try { 
                    if (svp[s].canConvert(msgState)){
                        theme = new StringBuffer(svp[s].convertGrouptheme(msgState, convert.toString(), blockedServerPlugin));   
                        theme.trimToSize();
                    }
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (theme != null)
                    convert = new StringBuffer(theme);
            }   
        }                 
        return convert;
    }
    
    /**
     * @param convert
     * @param cu
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkAwamessagePlugin (StringBuffer convert, User cu,MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s< svp.length; s++) {   
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                StringBuffer awayMessage = null;
                try {   
                     if (svp[s].canConvert(msgState)){
                         awayMessage = new StringBuffer(svp[s].convertAwaymessage(msgState, convert.toString(), cu, blockedServerPlugin));  
                         awayMessage.trimToSize();
                     }
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (awayMessage != null)
                    convert = new StringBuffer(awayMessage);
            }   
        }                 
        return convert;
    }
       
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkGroupName(StringBuffer convert,MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) {  
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                StringBuffer grp = null;
                try {   
                   if (svp[s].canConvert(msgState)){
                       grp = new StringBuffer(svp[s].convertGroupname(msgState, convert.toString(), blockedServerPlugin));
                       grp.trimToSize();
                   }
                } catch (Exception e) {   
                   Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (grp != null)
                    convert = new StringBuffer(grp);
           }   
       }                 
       return convert;
    }
    
    /**
     * @param mc
     * @param plugins
     * @param g
     */
    public void checkGrpAction(IContainer mc, IGroupPlugin[] plugins, Group g){
        if (mc instanceof MessageParser && plugins!=null) {
            MessageParser mp = (MessageParser) mc;
            for (int i = 0; i<plugins.length; i++) {
                if (Server.DEBUG)
                    Server.log("Group.java sendMsg", "Group Plugin"+i+" "+plugins[i] + " for Group "+g.getName(), Server.MSG_STATE, Server.LVL_MAJOR);
                try {
                    plugins[i].grpToUsrAction(mp);
                } catch (Exception e) {
                    Server.debug (plugins[i], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                }
            }
        }

    }
    
    /**
     * @param convert
     * @param msgState
     * @param toupper
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkMessage(StringBuffer convert,MessageState msgState,boolean toupper, Vector<String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) {  
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }
                StringBuffer message = null;
                try {   
                    if (svp[s].canConvert(msgState)){
                        message = new StringBuffer(svp[s].convertMessage(msgState, convert.toString(), toupper, blockedServerPlugin));   
                        message.trimToSize();
                    }
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (message != null)
                    convert = new StringBuffer(message);
             }   
        }                 
        return convert;
    }
    
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkParam(StringBuffer convert, MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) {   
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                String param = null;
                try {   
                    if (svp[s].canConvert(msgState))
                        param = svp[s].convertParam(msgState, convert.toString(), blockedServerPlugin);   
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                } 
                if (param != null)
                    convert = new StringBuffer(param);
            }   
        }                 
        return convert;
    }
    
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkReason(StringBuffer convert, MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) { 
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                String reason = null;
                try {  
                    if (svp[s].canConvert(msgState))
                        reason = svp[s].convertReason(msgState, convert.toString(), blockedServerPlugin);   
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (reason != null)
                    convert = new StringBuffer(reason);
            }   
        }                 
        return convert;
    }
    
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkTargetGroup(StringBuffer convert,MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin [] svp = Server.srv.serverPlugin;
        if (svp !=null) {   
            for (int s = 0; s<svp.length; s++) {   
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                StringBuffer grp = null;
                try {  
                    if (svp[s].canConvert(msgState)){
                        grp = new StringBuffer(svp[s].convertGroupname(msgState, convert.toString(), blockedServerPlugin));
                        grp.trimToSize();
                    }
                } catch (Exception e) {   
                    Server.debug (svp[s], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);   
                }   
                if (grp != null)
                    convert = new StringBuffer(grp);
            }   
        }                 
        return convert;
    }
    
    /**
     * @param convert
     * @param msgState
     * @param blockedServerPlugin
     * @return
     */
    public StringBuffer checkTheme(StringBuffer convert,MessageState msgState, Vector <String> blockedServerPlugin){
        IServerPlugin[] svp = Server.srv.serverPlugin;
        if (svp != null) {
            for (int s = 0; s < svp.length; s++) {
                if (isBlockedPlugin(blockedServerPlugin, svp[s].getName())){
                    Server.log(this, svp[s]+" Plugin is blocked from User", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
                    continue;
                }

                StringBuffer theme = null;
                try {
                    if (svp[s].canConvert(msgState)){
                        theme = new StringBuffer(svp[s].convertGrouptheme(msgState,convert.toString(), blockedServerPlugin));
                        theme.trimToSize();
                    }
                } catch (Exception e) {
                    Server.debug(svp[s],"catched exception from plugin",e, Server.MSG_ERROR, Server.LVL_MINOR);
                }
                if (theme != null)
                    convert = new StringBuffer(theme);
            }
        }
        return convert;
    }
    
    /**
     * @param mc
     * @param plugins
     * @param u
     */
    public void checkUsrAction(IContainer mc, IGroupPlugin[] plugins, User u){
        if (mc instanceof MessageParser && plugins!=null && plugins.length>0) {
            MessageParser mp = (MessageParser) mc;
            for (int i = 0; i<plugins.length; i++) {
                if (Server.DEBUG)
                    Server.log("User.java sendMessage", "Group Plugin"+i+" "+plugins[i] + " for User "+u.getName(), Server.MSG_STATE, Server.LVL_MAJOR);
                try {
                    plugins[i].usrAction(mp);
                } catch (Exception e) {
                    Server.debug (plugins[i], "catched exception from plugin", e, Server.MSG_ERROR, Server.LVL_MINOR);
                }
            }
        }
    }
    
    /**
     * @param blockedServerPlugin
     * @param pluginname
     * @return
     */
    private boolean isBlockedPlugin (Vector <String> blockedServerPlugin, String pluginname){
        if (blockedServerPlugin==null){
            return false;
        }
        if(blockedServerPlugin.size()==0)
            return false;
        if (blockedServerPlugin.contains(pluginname.toLowerCase()))
            return true;
        return false;
    }
 
}
