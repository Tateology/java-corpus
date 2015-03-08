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
 * Created on 07.05.2004
 */

package freecs.content;

import freecs.Server;



public class BanObject {
	public String msg, bannedBy;      // infos on how this ban happened
    public String usr, cookie, email; // properties, which are banned
    public Connection con;
    public String hostban;
	public long time;
	public BanObject (String msg, String bannedBy, long time) {
        this.bannedBy = bannedBy;
		this.time = time;
		this.msg = msg;
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
	}
    
    public boolean equals (Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BanObject))
            return false;
        BanObject bo = (BanObject) o;
        if (this.usr == null && bo.usr != null)
            return false;
        if (this.usr != null && !this.usr.equals(bo.usr))
            return false;
        if (this.cookie == null && bo.cookie != null)
            return false;
        if (this.cookie != null && !this.cookie.equals(bo.cookie))
            return false;
        if (this.con == null && bo.con != null)
            return false;
        if (this.con != null && bo.con==null)
            return false;
        if (this.con != null && !this.con.getBanKey().equals(bo.con.getBanKey()))
            return false;
        if (this.email != null && !this.email.equals(bo.email))
            return false;
        return true;
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}