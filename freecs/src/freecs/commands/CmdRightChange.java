/**
 * Copyright (C) 2003  Jochen 
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
 * Created on 31.08.2004
 */
package freecs.commands;

import freecs.content.MessageState;
import freecs.core.User;
import freecs.core.UserManager;
import freecs.interfaces.ICommand;
import freecs.interfaces.IUserStates;

/**
 * @author Jochen Schurich
 * 
 * a user can assign/revoke rights to other user(s) <br>
 * 
 *  
 */
public class CmdRightChange extends AbstractCommand {
    private final String cmd = "/rc";
    private final String version = "1.0";
    private static final ICommand selve = new CmdRightChange();

    private CmdRightChange() { }

    public static ICommand getInstance() {
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

    public boolean execute(MessageState msgState, String param) {
        if (param.length() < 1) {
            msgState.msgTemplate = "error.rc.noArg";
            msgState.sender.sendMessage(msgState.mp);
            return false;
        }
        String[] parts = param.split(" ");
        param = parts[0];
        // look for the user using utility-method of AbsractCommand
        User u = getUser(msgState, param);
        if (u == null)
            return false;
        msgState.usercontext = u;

        // no rights to assign or revoke found
        if (parts.length < 2) {
            // reset user to default rights
        	if(u.compareRoleTo(msgState.sender) == 2) {
          		 msgState.msgTemplate="error.noRight.noVipAdmin";
          		 msgState.sender.sendMessage(msgState.mp);
          		 return false;
        	} else if(u.compareRoleTo(msgState.sender) == 3) {
        			   msgState.msgTemplate="error.noRight.noAdmin";
        			   msgState.sender.sendMessage(msgState.mp);
        			   return false;
        	} else if (u.compareRoleTo(msgState.sender) >= 0) {
                         if (u.getPermissionMap() == u.getDefaultPermissionMap())
                        	 return false ;
        		         u.resetPermission();
                         msgState.msgTemplate = "rc.rightsReset";
                         msgState.sender.sendMessage(msgState.mp);
                         msgState.msgTemplate = "rc.newResetRight";
                         u.sendMessage(msgState.mp);
            } else {
                msgState.param = "reset";
                msgState.msgTemplate = "error.rc.notRight";
                msgState.sender.sendMessage(msgState.mp);
            }

            return true;
        }

        // all new rights have to be lower than the "donating" user and higher
        // than the "accepting" user
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() < 2)
                continue;
            if (part.startsWith("+")) {
                // add right
                part = part.substring(1);
                msgState.param = part;
                int right = UserManager.resolveState(part);
                if (right != 0) {
                	if (right== IUserStates.IS_GUEST || right== IUserStates.IS_MODERATOR
                			||  right== IUserStates.MAY_USE_SMILEY || right== IUserStates.MAY_SET_THEME) {
                		if (allowedToChangeRight(right,msgState.sender)) {
                			u.givePermission(right);
                			msgState.msgTemplate = "rc.rightAssigned";
                			msgState.sender.sendMessage(msgState.mp);
                			msgState.msgTemplate = "rc.newAssignedRight";
                			u.sendMessage(msgState.mp);
                			continue;
                		} else {
                			 if (right== IUserStates.IS_GUEST || right== IUserStates.IS_MODERATOR ){
                                 msgState.msgTemplate = "error.noRight.noModAdmin";
                                 msgState.sender.sendMessage(msgState.mp);
                                 continue;
                             } else {
                                 msgState.msgTemplate = "error.noRight.noAdmin";
                                 msgState.sender.sendMessage(msgState.mp);
                                 continue;
                             }
                		}
                	}
                }
            } else if (part.startsWith("-")) {
                // remove right
                part = part.substring(1);
                msgState.param = part;
                int right = UserManager.resolveState(part);
                if (right != 0) {
                	if (right== IUserStates.IS_GUEST || right== IUserStates.IS_MODERATOR) {
                		if (allowedToChangeRight(right,msgState.sender)) {
                			u.takePermission(right);
                			msgState.msgTemplate = "rc.rightRevoked";
                			msgState.sender.sendMessage(msgState.mp);
                			msgState.msgTemplate = "rc.newRevokedRight";
                			u.sendMessage(msgState.mp);
                			continue;
                		} else {
                			 if (right== IUserStates.IS_GUEST || right== IUserStates.IS_MODERATOR 
                					 ||  right== IUserStates.MAY_USE_SMILEY || right== IUserStates.MAY_SET_THEME){
                                 msgState.msgTemplate = "error.noRight.noModAdmin";
                                 msgState.sender.sendMessage(msgState.mp);
                                 continue;
                             } else {
                                 msgState.msgTemplate = "error.noRight.noAdmin";
                                 msgState.sender.sendMessage(msgState.mp);
                                 continue;
                             }
                		}
                	}
                }
            }
            if (part.startsWith("=")) {
                part = part.substring(1);
                msgState.param = part;
                int right = UserManager.resolveState(part);
                if (right != 0) {
                    if (right== IUserStates.ROLE_USER || right== IUserStates.ROLE_VIP || right== IUserStates.ROLE_ASSHOLE ) {
                        if (allowedToChangeRight(right,msgState.sender)) {
                            u.setNewPermission(right);
                            if (right == IUserStates.ROLE_ASSHOLE)
                            	if (u.getGroup() != null 
                                        && u.getGroup().usrIsSu(u))
                            	    u.getGroup().removeFromSusers(u);
                            msgState.msgTemplate = "rc.rightAssigned";
                            msgState.sender.sendMessage(msgState.mp);
                            msgState.msgTemplate = "rc.newAssignedRight";
                            u.sendMessage(msgState.mp);
                            continue;
                        } else {
                            msgState.msgTemplate = "error.noRight.noAdmin";
                            msgState.sender.sendMessage(msgState.mp);
                            continue;
                        }
                    }
                }
            }
            // ignore parameter
            msgState.msgTemplate = "error.rc.rightNotFound";
            msgState.sender.sendMessage(msgState.mp);
        }

        return true;
    }

    private static boolean allowedToChangeRight(int newRight, User donatingUser) {
        if (donatingUser.hasRole(IUserStates.ROLE_GOD) || donatingUser.hasDefaultRight(IUserStates.ROLE_GOD)) {
           // GOD can do EVERYTHING!
            return true;
        } else if (donatingUser.hasRight(IUserStates.IS_MODERATOR)) {
            // MODERATOR's may assign Guest's
            if (newRight == IUserStates.IS_GUEST)
                return true;
            if (newRight == IUserStates.IS_MODERATOR)
                return true;
        }   /* else if (donatingUser.hasRole(IUserStates.ROLE_VIP)) {
            // VIP's may assign MODERATOR's and GUEST's
            if (newRight == IUserStates.IS_GUEST)
                return true;
            if (newRight == IUserStates.IS_MODERATOR)
                return true;
        } */
        return false;
    }

}