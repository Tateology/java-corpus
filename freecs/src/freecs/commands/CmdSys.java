/**
 * Copyright (C) 2003
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
import java.util.Iterator;
import java.util.Vector;

import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;

public class CmdSys extends AbstractCommand {
    private final String cmd= "/sys";
    private final String version = "1.0";
    private static final ICommand selve=new CmdSys();

    private CmdSys () { }

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
        if (!msgState.sender.hasRight(IUserStates.ROLE_GOD)) {
            // no Right to issue /sys-command
            msgState.msgTemplate = "error.noRight.noAdmin";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        if (param.length () < 1) {
            // no argument to /sys
            msgState.msgTemplate = "error.sys.noArg";
            msgState.sender.sendMessage (msgState.mp);
            return false;
        }
        int pos = param.indexOf(":"); 
        if (pos > -1) { // sys issued for specific users
            msgState.message = param.substring (0, pos).trim();
            Vector<User> found = getMultibleUsers(msgState, param.substring (pos+1));
            for (Iterator<User> i = found.iterator(); i.hasNext(); ) {
                User cu = (User) i.next();
                msgState.msgTemplate = "message.sys";
                cu.sendMessage(msgState.mp);
            }
            return true;
        }
        msgState.message = param.trim();
        msgState.msgTemplate = "message.sys";
        UserManager.mgr.sendMessage(msgState.mp);
        return true;
    }
}