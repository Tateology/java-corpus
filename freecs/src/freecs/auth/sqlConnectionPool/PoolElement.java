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
package freecs.auth.sqlConnectionPool;

import freecs.Server;
import freecs.core.CanceledRequestException;
import freecs.core.User;
import freecs.interfaces.IUserStates;
import freecs.util.EntityDecoder;
import freecs.util.HashUtils;

import java.sql.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class PoolElement {
    public static final short INVALID = -1;
    public static final short IDLE    = 0;
    public static final short ACTIVE  = 1;
    
    volatile private PreparedStatement select = null,
                                       update = null,
                                       updateLastlogin = null,
                                       insert = null;
    volatile private String            selStrg = null,
                                       insStrg = null,
                                       updStrg = null,
                                       updLastloginStrg = null;
    private DbProperties dbp;
    private volatile boolean isValid=true;

    ConnectionPool pool;
    Connection con = null;
    int id;
    volatile int sCnt=0; 
    long validUntil;
    volatile boolean isActive = false, hasBeenUsed=false, cleanedUp=false;
    ResultSet rs;

    PoolElement (ConnectionPool pool, Connection con, DbProperties dbp, int id) throws Exception {
        if (con == null) 
            throw new Exception ("no connection supplied");
        this.pool = pool;
        this.dbp=dbp;
        this.id = id;
        this.con = con;
        con.setAutoCommit (false);
        validUntil = System.currentTimeMillis() + dbp.conTTL;
        Server.log ("SqlAuthenticator", "Created new Connetion " + this.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

	private boolean isValid() {
        if (!isValid)
            return false;
        if (con == null
            || cleanedUp)
            return false;
        if (!hasBeenUsed)
            return true;
        if (sCnt > dbp.maxStmtPerCon) {
            Server.log(this, "invalid because max-statements/connection has been reached " + dbp.maxStmtPerCon, Server.MSG_AUTH, Server.LVL_VERBOSE);
            isValid=false;
            return false;
        }
        if (validUntil <= System.currentTimeMillis()) {
            Server.log(this, "invalid because connection ttl has been reached " + dbp.conTTL, Server.MSG_AUTH, Server.LVL_VERBOSE);
            isValid=false;
            return false;
        }
        return true;
    }
    
	/**
	 * causes this PoolElement to close all open cursors and the connection to it's jdbc-source
	 */
    public synchronized void cleanup () {
        if (cleanedUp)
            return;
        try {
            if (select != null) {
                select.close();
                select = null;
            }
        } catch (SQLException se) {
            Server.debug(this, "cleanup: select.close()", se, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        try {
            if (insert != null) {
                insert.close();
                insert = null;
            }
        } catch (SQLException se) {
            Server.debug(this, "cleanup: insert.close()", se, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        try {
            if (update != null) {
                update.close();
                update = null;
            }
        } catch (SQLException se) {
            Server.debug(this, "cleanup: update.close()", se, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        try {
            if (con!=null) {
                con.close();
                con = null;
            }
        } catch (SQLException se) {
            Server.debug(this, "cleanup: connection.close()", se, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        this.pool = null;
        this.isActive = false;
        this.cleanedUp = true;
        Server.log ("SqlAuthenticator", "Closed Connetion " + this.toString(), Server.MSG_AUTH, Server.LVL_MAJOR);        
    }

    /**
     * Checks the given Statement for SQLWarnings and logs them.
     * @param s The statement to check for Warnings
     */
	public void checkWarnings(Statement s, String prefix) {
        try {
    		SQLWarning sqlW = s.getWarnings();
    		while (sqlW != null) {
    			  StringBuffer sb = new StringBuffer(this.toString());
                  sb.append (" getResultSet: Encountered SQLWarning: ");
                  sb.append (prefix);
                  sb.append (": ");
    			  sb.append (sqlW.getErrorCode());
    			  sb.append (": ");
    			  sb.append (sqlW.getCause());
    			  Server.log (Thread.currentThread(), sb.toString (), Server.MSG_ERROR, Server.LVL_MAJOR);
    			  sqlW = sqlW.getNextWarning();
    		}
        } catch (SQLException se) {
            this.isValid=false;
            Server.debug(this, "checkWarnings caused exception", se, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
	}

    public String toString() {
        StringBuffer sb = new StringBuffer("[PoolElement: ");
        sb.append (id);
        sb.append ("/");
        sb.append (sCnt);
        sb.append ("/");
        sb.append (dbp.url).append ("(").append (dbp.table).append(")");
        sb.append ("]");
        return sb.toString();
    }

    /**
     * Grabs control over this PoolElement and returns true on success
     * @return true if the control over this PoolElement was grabbed successfully
     */
    public synchronized short grab() {
        if (this.isActive)
            return ACTIVE;
        if (!isValid())
            return INVALID;
        this.isActive=true;
        this.hasBeenUsed=true;
        return IDLE;
    }

    /**
     * Clears all parameters given to the PreparedStatements and all their warnings. 
     * Afterwards this PoolElement is marked as inactive (isActive = false)
     */
    public void release() {
        try {
            if (select!=null) {
                select.clearParameters();
                select.clearWarnings();
            }
            if (update!=null) {
                update.clearParameters();
                update.clearWarnings();
            }
            if (insert!=null) {
                insert.clearParameters();
                insert.clearWarnings();
            }
        } catch (Exception se) {
            Server.debug (this, "catched exception while releasing PoolElement", se, Server.MSG_AUTH, Server.LVL_MAJOR);
        }
        this.isActive=false;
    }

    /**
     * Checks if a PreparedStatement for selection is already constructed or a 
     * new PreparedStatement will be constructed and this PreparedStatement will
     * be returned
     * @return the PreparedStatement for selection
     * @throws Exception if an Error occured
     */
    private PreparedStatement getSelect() throws Exception {
        try {
            if (select!=null)
                return select;
            if (con==null)
                throw new Exception ("No connection to retrieve a PreparedStatement from");
            StringBuffer sb = new StringBuffer ("SELECT ");
            sb.append (dbp.columns[0]);
            for (int i = 1; i<dbp.columns.length; i++) {
                sb.append (", ");
                sb.append (dbp.columns[i]);
            }
            sb.append (", ");
            sb.append (dbp.fc_password);
            sb.append (" FROM ");
            sb.append (dbp.table);
            sb.append (" WHERE ");
            sb.append (dbp.fc_username);
            sb.append (" = ?");
            selStrg = sb.toString();
            select = con.prepareStatement(selStrg, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (!dbp.RS_TYPE_SCROLL_SENSITIV){
                select = con.prepareStatement(selStrg, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            } else select = con.prepareStatement(selStrg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (dbp.queryTimeout > 0)
                select.setQueryTimeout(dbp.queryTimeout);
            return select;
        } catch (Exception e) {
            isValid=false;
            release();
            throw e;
        }
    }

    private PreparedStatement getUpdate() throws Exception {
        try {
            if (update != null)
                return update;
            if (con==null)
                throw new Exception ("No connection to retrieve a PreparedStatement from");
            StringBuffer sb = new StringBuffer("UPDATE ");
            sb.append (dbp.table);
            sb.append (" SET ");
            sb.append (dbp.updCols[0]);
            sb.append (" = ?");
            for (int i = 1; i<dbp.updCols.length; i++) {
                sb.append (", ");
                sb.append (dbp.updCols[i]);
                sb.append (" = ?");
            }
            sb.append (" WHERE ");
            if (dbp.idField != null) {
                sb.append (dbp.idField);
                sb.append (" = ?");
            } else {
                sb.append (dbp.fc_username);
                sb.append (" = ?");
            }
            updStrg = sb.toString();
            update = con.prepareStatement(updStrg);
            if (dbp.queryTimeout > 0)
                update.setQueryTimeout(dbp.queryTimeout);
            return update;
        } catch (Exception e) {
            isValid=false;
            release();
            throw e;
        }
    }

   
    /**
     * Checks if there is already a PreparedStatement for retrieving the user-data and
     * constructs it, if it doesn't exist. Afterwards the login will be checked and
     * the user-object will be constructed if the credentials are correct. Null will
     * be returned, if the credentials did not return a user-record.
     * @return User the user which is allowed to log in or null if no match was found
     * @throws Exception if technical error occures (connection problems, ...)
     */
    public User loginUser (String username, String password, String cookie) throws Exception {
        try {
            checkThread();
            PreparedStatement ps = getSelect();
            ps.setString(1, username.toLowerCase().trim());
            ResultSet rs = ps.executeQuery(); 
            sCnt++;
            Server.log(Thread.currentThread(), this.toString() + "LOGIN user uname=" + username.toLowerCase() + "/pwd=" + password + "/cookie=" + cookie + "\r\n" + selStrg, Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
            dbp.cacheMetaData(rs);
            if (!rs.next()) {
                Server.log(Thread.currentThread(), this.toString()+ "LOGIN no user mathing username and password " + username + "/" + password, Server.MSG_AUTH, Server.LVL_MINOR);
                rs.close();
                // return unregistered user (if they are allowed will be checked in auth-manager)
                return new User(username, cookie); // return an unregistered user
            } else if (!rs.isLast()) {
                Server.log(Thread.currentThread(), this.toString() + "LOGIN multible records returned for user " + username, Server.MSG_AUTH, Server.LVL_MAJOR);
                rs.close();
                // return null to make clear, that there is a problem within the db-table
                return null;
            }
            checkThread();
            String dbpwd = rs.getString(dbp.columns.length+1);
            if (dbpwd==null || !dbpwd.equals(password))
                return null;
            
            User u = new User (username, cookie);
            u.isUnregistered = false;
            
            readColumns(u, rs);
            
            checkWarnings(ps, "loginUser (getData)");
            
            checkThread();
            // if a lastlogin-property exists, we have to update the data in the db
            if (!dbp.readOnly) {
            	doLoginUpdates(u, rs);
            }
            checkWarnings(ps, "loginUser (update Data)");
            rs.close();
            Server.log (Thread.currentThread(), this.toString() + "LOGIN returning " + u, Server.MSG_AUTH, Server.LVL_MAJOR);
            u.isUnregistered = false;
            return u;
        } catch (Exception e) {
            Server.debug (this, selStrg, e, Server.MSG_AUTH, Server.LVL_MAJOR);
            isValid=false;
            release();
            throw e;
        }
    }

    public User loginUser (User u, String password) throws Exception {
        try {
            checkThread();
            PreparedStatement ps = getSelect();
            ps.setString(1, u.getName().toLowerCase().trim());
            Server.log(Thread.currentThread(), this.toString() + "LOGIN user uname=" + u.getName().toLowerCase() + "\r\n" + selStrg, Server.MSG_AUTH, Server.LVL_VERY_VERBOSE);
            ResultSet rs = ps.executeQuery();
            sCnt++;
            dbp.cacheMetaData(rs);
            if (!rs.next()) {
                Server.log(Thread.currentThread(), this.toString()+ "LOGIN no user mathing username " + u.getName(), Server.MSG_AUTH, Server.LVL_MINOR);
                rs.close();
                return u; // return unchanged user object
            } else if (!rs.isLast()) {
                Server.log(Thread.currentThread(), this.toString() + "LOGIN multible records returned for user " + u.getName(), Server.MSG_AUTH, Server.LVL_MAJOR);
                rs.close();
                return u; // return unchanged user object
            }
            
            // always check Pwd if the userobject is marked as unregistered
            // if there is an existing user having the same name but a differen password,
            // we must return login-failed (done by returning null instead of an user-object)
            if (u.isUnregistered == true) {
                String dbpwd = rs.getString(dbp.columns.length+1);
                if (dbpwd==null || !dbpwd.equals(password)) {
                    return null;
                }
                u.isUnregistered = false;
            }
            checkThread();
            
            // read all the other properties
            readColumns(u, rs);
            
            checkWarnings(ps, "loginUser (getData)");
            checkThread();
            // if a lastlogin-property exists, we have to update the data in the db
            if (!dbp.readOnly) {
            	doLoginUpdates(u, rs);
            }
            checkWarnings(ps, "loginUser (update Data)");
            rs.close();
            Server.log (Thread.currentThread(), this.toString() + "LOGIN returning " + u, Server.MSG_AUTH, Server.LVL_MAJOR);
            return u;
        } catch (Exception e) {
            Server.log(this, selStrg, Server.MSG_AUTH, Server.LVL_MAJOR);
            isValid=false;
            release();
            throw e;
        }
    }
    
    
    
    private void readColumns(User u, ResultSet rs) throws SQLException {
        for (int i = 0; i<dbp.columns.length; i++) {
            String cname = dbp.names[i];
            int idx = i+1;
            if ("userright".equals(cname)) {
                String val = rs.getString(idx);
                if (val == null || val.length()==0 || "null".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.ROLE_USER);
                } else if ("true".equalsIgnoreCase (val)
                    || "yes".equalsIgnoreCase(val)
                    || "vip".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.ROLE_VIP);
                } else if ("admin".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.ROLE_GOD);
                } else if ("moderator".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.ROLE_VIP | IUserStates.IS_MODERATOR);
                } else if ("guest".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.IS_GUEST);
                } else if ("asshole".equalsIgnoreCase(val)) {
                    u.setPermission(IUserStates.ROLE_ASSHOLE);
                } else {
                    try {
                        u.setPermission(Integer.parseInt(val));
                    } catch (NumberFormatException nfe) {
                        Server.log(Thread.currentThread(), this.toString() + "LOGIN userrights-column contains unknown value, corrected to ROLE_USER\r\n(must be null/true,yes,vip or VIP/admin/moderator/guest/user/assohle) ", Server.MSG_AUTH, Server.LVL_MAJOR);
                        u.setPermission(IUserStates.ROLE_USER);
                    }
                }
            } else if ("id".equals(cname)) {
                u.setID(rs.getString(idx));
            } else if ("color".equals(cname)) {
                u.setColCode(rs.getString(idx));
            } else if ("fadecolor".equals(cname)) {
            	Server.srv.USE_FADECOLOR =true;
            	if (rs.getString(idx)!= null ){
            	    u.setFadeColCode(rs.getString(idx));
             	}
            } else if ("bgcolor".equals(cname)) {
            	Server.srv.USE_BGCOLOR =true;
            	if (rs.getString(idx)!= null ){
            	    u.setBgColCode(rs.getString(idx));
             	}
            } else if ("chattime".equals(cname)) {
                u.setProperty("chattime", new Long(rs.getLong(idx)));
            } else if ("lastlogin".equals(cname)) {
                switch (dbp.types[i]) {
                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.NUMERIC:
                    case Types.SMALLINT:
                        u.setProperty("lastlogin", new Timestamp (rs.getLong(idx)));
                        break;
                    case Types.DATE:
                    case Types.TIMESTAMP:
                        Timestamp ts = rs.getTimestamp(idx);
                        u.setProperty("lastlogin", ts);
                        break;
                    default:
                        String s = rs.getString(idx);
                        if (rs.wasNull()) {
                            u.setProperty("lastlogin", new Timestamp (System.currentTimeMillis()));
                            break;
                        }
                        try {
                            long l = Long.parseLong (s);
                            u.setProperty("lastlogin", new Timestamp(l));
                        } catch (NumberFormatException nfe) {
                            try {
                                u.setProperty("lastlogin", Timestamp.valueOf(s));
                            } catch (IllegalArgumentException iae) {
                                Server.log (this, "LOGIN Unable to retrieve lastlogin-value! " + s, Server.MSG_AUTH, Server.LVL_MAJOR);
                            }
                        }
                }
            } else if ("friends".equals(cname)) {
            	List<?> users = pool.authenticator.parseUserList(rs.getString(idx));
                for (Iterator<?> it = users.iterator(); it.hasNext(); ) {
                    u.addFriend((String) it.next());
                }
            } else if ("ignorelist".equals(cname)) {
                List<?> ignores = pool.authenticator.parseUserList(rs.getString(idx));
                for (Iterator<?> it = ignores.iterator(); it.hasNext(); ) {
                    u.ignoreUser((String) it.next());
                }
            } else if ("notifyfriends".equals(cname)) {
                switch (dbp.types[i]) {
                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.NUMERIC:
                    case Types.SMALLINT:
                        u.setFriendsNotification(rs.getShort(idx));
                        break;
                    default:
                        u.setFriendsNotification(pool.authenticator.parseBoolean(rs.getString(idx)) ? Server.srv.FN_DEFAULT_MODE_TRUE : Server.srv.FN_DEFAULT_MODE_FALSE);
                }
                // u.setFriendsNotification(pool.authenticator.parseBoolean(rs.getString(idx)));
            } else if ("customtitle".equals(cname)) {
                u.setCustomTitle(rs.getString(idx));
            } else if ("blocked".equals(cname)) {
            	u.blocked = pool.authenticator.parseBoolean(rs.getString(idx));
            } else if ("activated".equals(cname)) {
            	u.activated = pool.authenticator.parseBoolean(rs.getString(idx));
            } else {
                String strg = getEncodedString (rs, idx);
                u.setProperty(cname, strg);
            }
        }
    }

    private void doLoginUpdates(User nu, ResultSet rs) throws Exception {
        boolean updated = false, error = false;
        long ts = System.currentTimeMillis();
        int idx = dbp.nameV.indexOf("lastlogin");
        if (idx > -1) {
            try {
                switch (dbp.types[idx]) {
                    case Types.INTEGER:
                    case Types.SMALLINT:
                        rs.updateInt(idx+1, (int) (ts/1000));
                        break;
                    case Types.BIGINT:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        rs.updateLong(idx+1, ts/1000);
                        break;
                    case Types.DATE:
                    case Types.TIMESTAMP:
                        rs.updateTimestamp(idx+1, new Timestamp(ts));
                        break;
                    default:
                        rs.updateString(idx+1, String.valueOf(ts/1000));
                }
                updated=true;
            } catch (SQLException se) {
                Server.debug (Thread.currentThread(), this.toString() + "LOGIN unable to update lastlogin", se, Server.MSG_AUTH, Server.LVL_MAJOR);
                error=true;
            }
        }
        // update the cookie too (if set in the db properties)
        idx = dbp.nameV.indexOf("cookie");
        if (idx > -1) try {
            rs.updateString(idx+1, HashUtils.encodeMD5(nu.getCookie()));
        } catch (SQLException se) {
            Server.debug (Thread.currentThread(), this.toString() + "LOGIN unable to update cookie", se, Server.MSG_AUTH, Server.LVL_MAJOR);                    
        }
        try {
            if (updated) {
                rs.updateRow();
                con.commit();
            } else if (error) {
                rs.cancelRowUpdates();
            }
        } catch (SQLException se) {
            Server.debug (Thread.currentThread(), this.toString() + "LOGIN exception during updateRow/cancelRowUpdates", se, Server.MSG_AUTH, Server.LVL_MAJOR);
        }
    }
    

    
    public void logoutUser (User u) throws Exception {
        try {
            if (dbp.readOnly || dbp.updCols == null || dbp.updCols.length < 1)
                return;
            PreparedStatement ps = getUpdate();
            for (int i = 0; i < dbp.updCols.length; i++) {
                String cname = dbp.updNames[i];
                if ("chattime".equalsIgnoreCase(cname)) {
                    ps.setLong(i+1, u.getChattime());
                } else if ("userrights".equalsIgnoreCase(cname)) {
                    ps.setInt(i+1, u.getPermissionMap());
                } else if ("fadecolor".equalsIgnoreCase(cname)) {
                    ps.setString(i+1, u.getFadeColCode());
                } else if ("bgcolor".equalsIgnoreCase(cname)) {
                    ps.setString(i+1, u.getBgColCode());
                } else if ("color".equalsIgnoreCase(cname)) {
                    ps.setString(i+1, u.getColCode());
                } else if ("friends".equalsIgnoreCase(cname)) {
                    StringBuffer sb = new StringBuffer();
                    for (Enumeration<?> e = u.friends(); e.hasMoreElements(); ) {
                        String s = (String) e.nextElement();
                        sb.append (s);
                        if (e.hasMoreElements())
                            sb.append (", ");
                    }
                    ps.setString(i+1, sb.toString());
                } else if ("ignorelist".equalsIgnoreCase(cname)) {
                    StringBuffer sb = new StringBuffer();
                    for (Enumeration<String> e = u.ignoreList(); e.hasMoreElements(); ) {
                        String s = (String) e.nextElement();
                        sb.append (s);
                        if (e.hasMoreElements())
                            sb.append (", ");
                    }
                    ps.setString(i+1, sb.toString());
                } else if ("notifyfriends".equalsIgnoreCase(cname)) {
                    int idx = dbp.nameV.indexOf("notifyfriends");
                    switch (dbp.types[idx]) {
                        case Types.BIGINT:
                        case Types.BIT:
                        case Types.DECIMAL:
                        case Types.INTEGER:
                        case Types.SMALLINT:
                            ps.setInt(i+1, u.notifyFriends());
                            break;
                        case Types.BOOLEAN:
                            ps.setBoolean(i+1, u.notifyFriends()==User.FN_ALL ? true : false);
                            break;
                        default:
                            ps.setString(i+1, u.notifyFriends()==User.FN_ALL ? "true" : "false");
                    }
                } else if ("extratitle".equalsIgnoreCase(cname)) {
                    ps.setString(i+1, u.getCustomTitle());
                } else if ("cookie".equalsIgnoreCase(cname)) {
                	// and overwrite it with "not_logged_in" when the user loggs out
                    ps.setString(i+1, "not_logged_in");
                } else if ("blocked".equalsIgnoreCase(cname)) {
                    int idx = dbp.nameV.indexOf("blocked");
                    switch (dbp.types[idx]) {
                        case Types.BIGINT:
                        case Types.BIT:
                        case Types.DECIMAL:
                        case Types.INTEGER:
                        case Types.SMALLINT:
                            ps.setInt(i+1, u.blocked ? 1 : 0);
                            break;
                        case Types.BOOLEAN:
                            ps.setBoolean(i+1, u.blocked);
                            break;
                        default:
                            ps.setString(i+1, u.blocked ? "1" : "0");
                    }
                } else if ("activated".equalsIgnoreCase(cname)) {
                    int idx = dbp.nameV.indexOf("activated");
                    switch (dbp.types[idx]) {
                        case Types.BIGINT:
                        case Types.BIT:
                        case Types.DECIMAL:
                        case Types.INTEGER:
                        case Types.SMALLINT:
                            ps.setInt(i+1, u.activated ? 1 : 0);
                            break;
                        case Types.BOOLEAN:
                            ps.setBoolean(i+1, u.activated);
                            break;
                        default:
                            ps.setString(i+1, u.activated ? "1" : "0");
                    }
                } else  {
                    Server.log(this, "save custom Property "+cname, Server.MSG_AUTH, Server.LVL_VERBOSE);
                    int idx = dbp.nameV.indexOf(cname);
                    switch (dbp.types[idx]) {
                        case Types.BIGINT:
                        case Types.BIT:
                        case Types.DECIMAL:
                        case Types.INTEGER:
                        case Types.SMALLINT:
                        case Types.BOOLEAN:
                         
                        default:
                            ps.setObject(i+1,u.getProperty(cname));                   
                    }
                 } 
            }
            if (dbp.idField != null) {
                if (u.getID()==null) {
                    Server.log(u, "Unable to store logout-data for " + u.getName() + " because of missing id-value", Server.MSG_AUTH, Server.LVL_MAJOR);
                    return;
                }
                ps.setString(dbp.updCols.length+1, u.getID());
            } else {
                ps.setString(dbp.updCols.length+1, u.getName().toLowerCase());
            }
            int rows = ps.executeUpdate();
            sCnt++;
            if (rows==1) {
                con.commit();
            } else if (rows < 1) {
                Server.log(Thread.currentThread(), this.toString() + "LOGOUT unable to update userdata! No record for: " + dbp.idField != null ? dbp.idField + " = " + u.getID() : "username = " + u.getName().toLowerCase(), Server.MSG_AUTH, Server.LVL_MAJOR);
                return;
            } else if (rows > 1) {
                Server.log(Thread.currentThread(), this.toString() + "LOGOUT unable to update userdata! More than one value would be updated: (" + dbp.idField != null ? dbp.idField + " = " + u.getID() : "username = " + u.getName().toLowerCase() + ")", Server.MSG_AUTH, Server.LVL_MAJOR);
                try {
                    con.rollback();
                    Server.log (Thread.currentThread(), this.toString() + "LOGOUT rollback successfully", Server.MSG_AUTH, Server.LVL_VERBOSE);
                } catch (SQLException se) {
                    Server.log (Thread.currentThread(), this.toString() + "LOGOUT rollback failed!!!", Server.MSG_AUTH, Server.LVL_MAJOR);
                }
            }
            checkWarnings(ps, "logoutUser");
        } catch (Exception e) {
            isValid=false;
            release();
            throw e;
        }
    }

    /**
     * replaces every < and every > with an HTML-entity and returns the value
     * @param rs
     * @param c
     * @return String The string having the replaces < and >-characters
     */
    private static String getEncodedString (ResultSet rs, int idx) {
        if (rs == null) 
            return null;
        try {
            String result = rs.getString (idx);
            if (result==null) 
                return null;
            result = result.replaceAll ("[<]", "&lt;");
            result = result.replaceAll ("[>]", "&gt;");
            result = EntityDecoder.convertFormattingCharacters(result);
            return result;
        } catch (Exception e) {
            Server.debug ("static PoolElement", "getEncodedString: error geting encoded string", e, Server.MSG_ERROR, Server.LVL_MAJOR);
        }
        return null;
    }
    
    private void checkThread() throws CanceledRequestException {
        if (Thread.currentThread().isInterrupted())
            throw new CanceledRequestException ("ConnectionBuffer has been invalidated");
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}