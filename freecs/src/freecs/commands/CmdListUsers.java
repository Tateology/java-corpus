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
import java.nio.ByteBuffer;

import freecs.interfaces.ICommand;
import freecs.core.Group;
import freecs.core.GroupManager;
import freecs.core.MessageRenderer;
import freecs.core.UserManager;
import freecs.content.MessageState;
import freecs.content.PersonalizedMessage;

/**
 * @author Manfred Andres
 *
 * freecs.commands
 */
public class CmdListUsers extends AbstractCommand {
	private final String cmd= "/wc";
	private final String version = "1.0";
	private static final ICommand selve=new CmdListUsers();

	private CmdListUsers () { }
	
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
		boolean withoutBBC = false;
		if (param != null){
		    int pos = param.indexOf(":");
		    if (pos>-1) {
	            String parts[] = param.split (":");
				param = parts[0];
				if (parts.length >1)
				    if (parts[1].equals("default"))
				    	withoutBBC = true;	
			}
		}
		if (param == null || param.length () < 1) {
			msgState.useRenderCache = false;
            if ("false".equals(msgState.sender.getTemplateSet().getMessageTemplate("status.wc.renderUserlist")))
                return true;
            StringBuffer sb = new StringBuffer();
            Group[] grps = GroupManager.mgr.currentGroupList();
            for (int i = 0; i < grps.length; i++) {
				Group g = grps[i];
                if (!g.isValid()) {
                    GroupManager.mgr.removeGroup(g);
                    continue;
                }
				msgState.sourceGroup=g;
                msgState.targetGroup=g;
                sb.append(getUserList (msgState, true, withoutBBC));
            }
            sb.trimToSize();
            ByteBuffer bb = MessageRenderer.encode(sb.toString());
            if (bb==null)
                return false;
            msgState.msgTemplate = "message.wc.headline";
            msgState.sender.sendMessage (msgState.mp);
            msgState.sender.sendMessage(new PersonalizedMessage(bb));
            msgState.msgTemplate = "message.wc.underline";
            msgState.param = String.valueOf (UserManager.mgr.getActiveUserCount());
            msgState.reason = String.valueOf (GroupManager.mgr.openGroupsCount());
            msgState.sender.sendMessage (msgState.mp);
		} else {
		   Group g = GroupManager.mgr.getGroup (param);
		   if (g == null) {
			  msgState.msgTemplate = "error.group.notExisting";
			  msgState.param = param;
			  msgState.sender.sendMessage (msgState.mp);
		   } else {
			  msgState.sourceGroup = g;
			  sendUserList (msgState, false);
		   }
		}
		return true;
	}
}
