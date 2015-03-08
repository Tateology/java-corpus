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
 * Groupstates which may be set and change the behavior of this group
 */
public abstract interface IGroupState {
    public static final int ENTRANCE            =   1;
    public static final int OPEN                =   2;
    public static final int LOCKPROTECTED       =   4;
    public static final int MODERATED           =   8;
    public static final int AUTO_SU_FIRST       =  16;
    public static final int ALLOW_SU            =  32;
    public static final int SND_PRF_USER        =  64;
    public static final int SND_PRF_VIP         = 128;
    public static final int SND_PRF_MODERATOR   = 256;
    public static final int SND_PRF_GOD         = 512;
	public static final int SND_PRF_GUEST       = 1024;
	public static final int NO_SU_FIRST         = 2048;
	public static final int DEACTIVATE_HITDICE  = 4096;
    public static final int SU_CAN_BAN          = 8192;
    public static final int SU_CAN_SETTHEME     = 16384;
    public static final int CAN_SET_PUNISHABLE  = 32768;
    public static final int ALLOW_USE_BBCODES   = 65536;
    public static final int ALLOW_USE_SMILEY    = 131072;
    public static final int NOT_ALLOW_JOIN_UNREG = 262144;
    public static final int JOIN_MEMBERSHIP_LOCKED = 524288;

}