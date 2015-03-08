/**
 * Copyright (C) 2008 Rene M.
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
 * Created on 09.01.2008
 */

package freecs.interfaces;

public abstract interface IGroupReason {
    public static final int LOCKED              =   1;
    public static final int RESERVED            =   2;
    public static final int NOT_ALLOW_JOIN_UNREG=   3;
    public static final int NO_RIGHT            =   4;
    public static final int CREATE              =   5;
}                     