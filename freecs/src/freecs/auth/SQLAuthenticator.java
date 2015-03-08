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

import freecs.core.CanceledRequestException;
import freecs.core.User;
import freecs.Server;
import freecs.auth.sqlConnectionPool.*;
import freecs.interfaces.IRequest;
import java.util.Properties;

public class SQLAuthenticator extends AbstractAuthenticator {
	String          data = null;
    ConnectionPool  conPool = null;
    ThreadGroup     threadGroup;

    //Plugins needed
    public static DbProperties dbProps = null;

    public SQLAuthenticator () {
    }
    
	public void init(Properties allProps, String additionalPrefix) {
        super.init (allProps, additionalPrefix);
        
        if (!props.containsKey("update.friends")){
            props.setProperty("update.friends", "false");
        }
        if (!props.containsKey("update.ignorelist")){
            props.setProperty("update.ignorelist", "false");
        }

		Properties mapping = filterProperties(props, "mapping.");
		Properties updateMapping = filterProperties(props, "update.");
		Server.log (this, "parsing db-config", Server.MSG_STATE, Server.LVL_MINOR);
        try {
        	dbProps = new DbProperties(props, mapping, updateMapping);
        	conPool = new ConnectionPool (this, dbProps);
			data = null;   // causes the next login-attempt to update the cached column-list of columns to retrieve from the db
            StringBuffer sb = new StringBuffer("[SqlRunnerGroup ");
            sb.append(dbProps.url).append("(").append(dbProps.table).append(")");
			threadGroup = new ThreadGroup (sb.toString());
		} catch (Exception e) {
			Server.debug (this, "error parsing db-config: ", e, Server.MSG_STATE, Server.LVL_MAJOR);
		}
	}


	/**
	 * server is shutting down. Close all connections to jdbc-source
	 */
    public void shutdown () throws Exception {
       conPool.shutdown ();
    }

    public User loginUser(String username, String password, String cookie, IRequest request) throws Exception {
        SqlRunner runner = new SqlRunner (username, password, cookie, request, conPool);
        Thread t = startThread(runner);
        try {
            t.join();
        } catch (InterruptedException ie) {
        	t.interrupt();
            StringBuffer sb = new StringBuffer("Login timed out for user ").append(username);
            throw new CanceledRequestException(sb.toString(), true);
        }
        if (runner.catchedException!=null)
            throw runner.catchedException;
        return runner.result;
    }

    public User loginUser(User u, String username, String password, IRequest request) throws Exception {
        SqlRunner runner = new SqlRunner (u, username, password, request, conPool);
        Thread t = startThread(runner);
        try {
            t.join();
        } catch (InterruptedException ie) {
        	t.interrupt();
            StringBuffer sb = new StringBuffer("Login timed out for user ").append(u.getName());
            throw new CanceledRequestException(sb.toString(), true);
        }
        if (runner.catchedException!=null)
            throw runner.catchedException;
        return runner.result;
	}
    
    public void logoutUser(User u) throws Exception {
        if (u==null || u.isUnregistered)
            return;
        SqlRunner runner = new SqlRunner (u, conPool);
        startThread(runner);
    }
    
    private Thread startThread(SqlRunner sr) throws Exception {
        if (threadGroup.activeCount() >= conPool.size())
            throw new Exception ("All connections are currently used");
        Thread t = new Thread(threadGroup, sr);
        t.start();
        return t;
    }
}