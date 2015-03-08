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
 */
package freecs.interfaces;
/**
 * admin = 13107
 * vip = 130991
 * user = 33
 * asshole = 917536
 */

/**
 * deffining userrights and the sate of this user
 */
public interface IUserStates {
   public static final int MAY_OPEN_GROUP             =      1;
   public static final int MAY_OPEN_MODERATED_GROUP   =      2;
   public static final int MAY_LOCK_GROUP             =      4;
   public static final int MAY_LOCK_MODERATED_GROUP   =      8;
   public static final int MAY_LOCK_STARTING_GROUP    =     16;
   public static final int MAY_JOIN_GROUP             =     32;
   public static final int MAY_JOIN_LOCKED_GROUP      =     64;
   public static final int MAY_SET_THEME              =    128;

   public static final int MAY_CHANGE_RIGHT           =    256;
   public static final int MAY_PUNISH                 =    512;
   public static final int MAY_KICK                   =   1024;
   public static final int MAY_KICK_HARD			  =   2048;
   public static final int MAY_BAN                    =   4096;

   public static final int PROTECTED_FROM_PUNISH      =   8192;
   public static final int PROTECTED_FROM_KICK        =  16384;
   public static final int PROTECTED_FROM_BAN         =  32768;
   public static final int PROTECTED_FROM_RIGHTCHANGE =  65536;

   public static final int FREELY_PUNISHABLE          = 131072;
   public static final int FREELY_KICKABLE            = 262144;
   public static final int FREELY_BANABLE             = 524288;

   public static final int IS_MODERATOR               =1048576;
   public static final int MAY_RELEASE_SERVERBAN	  =2097152;
   public static final int IS_GUEST                   =4194304;
   public static final int MAY_CALL_MEMBERSHIPS       =8388608;
   public static final int MAY_USE_SMILEY             =134217728;

   public static final int ROLE_ASSHOLE=  MAY_JOIN_GROUP |
                                          FREELY_PUNISHABLE |
                                          FREELY_KICKABLE |
                                          FREELY_BANABLE;

   public static final int ROLE_USER   =  MAY_OPEN_GROUP |
                                          MAY_JOIN_GROUP;

   public static final int ROLE_VIP    =  MAY_OPEN_GROUP |
                                          MAY_OPEN_MODERATED_GROUP |
                                          MAY_LOCK_GROUP |
                                          MAY_LOCK_MODERATED_GROUP |
                                          MAY_JOIN_GROUP |
                                          MAY_CHANGE_RIGHT |
                                          MAY_PUNISH |
                                          MAY_KICK |
										  MAY_KICK_HARD |
                                          MAY_BAN |
                                          PROTECTED_FROM_PUNISH |
                                          PROTECTED_FROM_KICK |
                                          PROTECTED_FROM_BAN |
                                          PROTECTED_FROM_RIGHTCHANGE;

    public static final int ROLE_GOD    = ROLE_VIP |
                                          MAY_SET_THEME |
                                          MAY_CALL_MEMBERSHIPS |
                                          MAY_JOIN_LOCKED_GROUP |
                                          MAY_LOCK_STARTING_GROUP |
                                          MAY_USE_SMILEY ;
                  
	
}