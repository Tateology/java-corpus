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
package freecs.auth;

import java.util.Properties;

import freecs.core.User;
import freecs.interfaces.IRequest;

public class NoAuthentication extends AbstractAuthenticator {
    private static NoAuthentication auth = null;

    public NoAuthentication () { }

    public void init (Properties props) throws Exception { }
    public void shutdown () throws Exception { }
    public void logoutUser (User u) throws Exception { }

    public User loginUser (String username, String password, String cookie, IRequest request) throws Exception {
        if (username == null)
            return null;
        username = username.trim();
        User u = new User (username, cookie);
        u.isUnregistered = false;
        return (u);
    }

    public User loginUser (User u, String username, String password, IRequest req) throws Exception {
    	return u;
    }
    
}