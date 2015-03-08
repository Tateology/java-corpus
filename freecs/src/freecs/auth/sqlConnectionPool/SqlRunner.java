/**
 * Copyright (C) 2005 KMFDM
 * Created: 16.01.2005 (22:40:14)
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
package freecs.auth.sqlConnectionPool;

import freecs.Server;
import freecs.core.CanceledRequestException;
import freecs.core.ConnectionBuffer;
import freecs.core.User;
import freecs.interfaces.IRequest;
import freecs.util.HashUtils;


/**
 * @author KMFDM
 *
 */
public class SqlRunner implements Runnable {
    private final String username, password, cookie;
    private final IRequest request;
    private final User u;
    private final short method;
    private final ConnectionPool connPool;
    public Exception catchedException=null;
    public User result=null;
    
    public SqlRunner (String username, String password, String cookie, IRequest request, ConnectionPool connPool) {
        // handle login
        this.u=null;
        this.username = username;
        this.password = password;
        this.cookie = cookie;
        this.request = request;
        this.connPool = connPool;
        this.method = 1;
    }
    
    public SqlRunner (User u, String username, String password, IRequest request, ConnectionPool connPool) {
        this.u = u;
        this.cookie=null;
        this.username = username;
        this.password = password;
        this.request = request;
        this.connPool = connPool;
        this.method = 2;
    }
    
    public SqlRunner (User u, ConnectionPool connPool) {
        this.username=null;
        this.password=null;
        this.request=null;
        this.cookie=null;
        this.u = u;
        this.connPool = connPool;
        this.method = 3;
    }


    public void run() {
        try {
            switch (method) {
                case 1:
                    this.result = loginUser(username, password, cookie, request);
                    break;
                case 2:
                    this.result = loginUser(u, username, password, request);
                    break;
                case 3:
                    logoutUser(u);
            }
        } catch (Exception e) {
            catchedException = e;
        }
    }

    public void logoutUser(User u) throws Exception {
        PoolElement el = null;
        boolean success = false;
        Exception le = null;
        for (int i = 0; i < connPool.size()+1; i++) {
            try {
                el = connPool.getPoolElement(3, null);
                el.logoutUser (u);
                el.release();
                success=true;
                break;
            } catch (Exception ee) {
                if (el != null) {
                    el.cleanup();
                }
                Server.debug(this, "SQL-Exception", ee, Server.MSG_ERROR, Server.LVL_MAJOR);
                catchedException = ee;
            }
        }
    }
    
    public User loginUser(String username, String password, String cookie, IRequest request) throws Exception {
        if (Server.srv.MD5_PASSWORDS)
            password = HashUtils.encodeMD5(password);
        PoolElement el=null;
        User u = null;
        boolean success = false;
        Exception le = null;
        ConnectionBuffer cb = request.getConnectionBuffer();
        Thread curr = Thread.currentThread();
        for (int i = 0; i < connPool.size()+1; i++) {
            if (curr.isInterrupted() || !cb.isValid())
                throw new CanceledRequestException ("ConnectionBuffer has been invalidated");
            try {
                // retrieve PoolElement from ConnectionPool trying 3-times
                el = connPool.getPoolElement(3, cb);
                u = el.loginUser(username, password, cookie);
                el.release();
                success=true;
                break;
            } catch (Exception ee) {
                // cleanup on exception
                if (el!=null) {
                    el.cleanup();
                }
                Server.debug(this, "Exception during loginUser", ee, Server.MSG_ERROR, Server.LVL_MAJOR);
                le = ee;
            }
        }
        if (!success) {
            if (le != null)
                throw le;
            throw new Exception ("UnknownException occured");
        }
        return u;
    }

    public User loginUser(User u, String username, String password, IRequest request) throws Exception {
        PoolElement el=null;
        boolean success = false;
        Exception le = null;
        ConnectionBuffer cb = request.getConnectionBuffer();
        Thread curr = Thread.currentThread();
        for (int i = 0; i < connPool.size()+1; i++) {
            if (curr.isInterrupted())
                throw new CanceledRequestException ("ConnectionBuffer has been invalidated");
            try {
                // retrieve PoolElement from ConnectionPool trying 3-times
                el = connPool.getPoolElement(3, cb);
                u = el.loginUser(u, password);
                el.release();
                success=true;
                break;
            } catch (Exception ee) {
                // cleanup on exception
                if (el!=null) {
                    el.cleanup();
                }
                Server.debug(this, "SQL-Exception", ee, Server.MSG_ERROR, Server.LVL_MAJOR);
                le = ee;
            }
        }
        if (!success) {
            if (le != null)
                throw le;
            throw new Exception ("UnknownException occured");
        }
        return u;
    }
    
    public String toString() {
        return "[SqlRunner]";
    }
}
