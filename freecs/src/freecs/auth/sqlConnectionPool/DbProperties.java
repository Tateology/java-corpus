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
 * Created on 04.05.2004
 */

package freecs.auth.sqlConnectionPool;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import freecs.Server;

/**
 * @author Manfred Andres
 *
 * freecs.auth.sqlConnectionPool
 */
/**
 * An instance of DbProperties represents a parsed and validated db-properties file
 * @author Manfred Andres
 *
 * freecs.auth.sqlConnectionPool
 */
public class DbProperties {
    public boolean readOnly = false,  RS_TYPE_SCROLL_SENSITIV = false;
    public String url, table, idField, fc_username, fc_password;
    public String[] columns, names, updCols, updNames;
    public Vector<String> colV, nameV;
    private boolean cachedMetaData=false;
    public int[] types;
    public Properties conProps = new Properties();
    public int poolsize = 10, maxStmtPerCon = 1000, queryTimeout=0;
    public long conTTL = 3600000;

    /**
     * Creates a new DbProperties-instance. All values are checked for
     * validity. If properties are missing or invalid, an exception will 
     * be thrown.
     * @param p db-properties
     * @throws Exception if properties are missing or invalid
     */
    public DbProperties (Properties props, Properties mapping, Properties updateMapping) throws Exception {

    	int err=0, warn=0;

    	colV = new Vector<String>();
        nameV = new Vector<String>();

        Vector<String> updColV = new Vector<String>();
        Vector<String> updNamV = new Vector<String>();
        
        // try to load jdbc driver
        try {
            Class.forName (props.getProperty("driver"));
        } catch (Exception ex) {
            StringBuffer tsb = new StringBuffer ("Unable to load jdbc-driver '").append (props.getProperty("driver")).append ("'");
            throw new Exception (tsb.toString (), ex);
        }

        StringBuffer warnMsg = new StringBuffer();

        // copy/parse values from properties
        table		  = props.getProperty("table");
        url 		  = props.getProperty("url");
        queryTimeout  = parseInt("queryTimeout", props.getProperty("queryTimeout"), 0, warnMsg);
        poolsize	  = parseInt("poolsize", props.getProperty("poolsize"), 10, warnMsg);
        maxStmtPerCon = parseInt("conStatements", props.getProperty("conStatements"), 1000, warnMsg);
        conTTL        = parseInt("conValidityTime", props.getProperty("conValidityTime"), 60, warnMsg);
        conTTL = conTTL * 60000;
        if ("true".equalsIgnoreCase(props.getProperty("typeScrollSensitiv"))) {
        	RS_TYPE_SCROLL_SENSITIV = true;
        }
        if ("true".equalsIgnoreCase(props.getProperty("readOnly")) ||
        	"1".equalsIgnoreCase(props.getProperty("readOnly"))) {
        	readOnly = true;
        } else if ("false".equalsIgnoreCase(props.getProperty("readOnly")) ||
        	"0".equalsIgnoreCase(props.getProperty("readOnly"))) {
        	readOnly = false;
        } else {
            warnMsg.append (".) Readonly-Flag has unknown value (");
            warnMsg.append (props.getProperty("readOnly"));
            warnMsg.append ("). Defaulting to false.");
        }
        conProps.setProperty("user", props.getProperty("username"));
        conProps.setProperty("password", props.getProperty("password"));

        // then we copy the column-2-property-mappings
        for (Enumeration<?> e = mapping.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String val = mapping.getProperty(key);
            if ("id".equalsIgnoreCase(key)) {
                idField = val;
                nameV.add(key);
                colV.add(val);
            } else if ("username".equals(key)) {
                fc_username = val;
            } else if ("password".equals(key)) {
                fc_password = val;
            } else if (key.startsWith("db.")) {
            	// further jdbc properties possible
                conProps.setProperty(key.substring(3), val);
            } else if (key.equalsIgnoreCase("userrights") && val.equalsIgnoreCase("serverconfig")) {
                continue;
            } else {
                if ("chattime".equalsIgnoreCase(key)
                    || "userrights".equalsIgnoreCase(key)
                    || "color".equalsIgnoreCase(key)
					|| "cookie".equalsIgnoreCase(key)
					|| "fadecolor".equalsIgnoreCase(key)
					|| "bgcolor".equalsIgnoreCase(key)
					|| "friends".equalsIgnoreCase(key)
					|| "ignorelist".equalsIgnoreCase(key)
					// FIXME: why not load the friendslist etc?
                    /* 
                    || "notifyfriends".equalsIgnoreCase(key)
                    || "extratitle".equalsIgnoreCase(key)
                    || "blocked".equalsIgnoreCase(key) */) {
                    updNamV.add(key.toLowerCase());
                    updColV.add(val);
                }
                nameV.add(key.toLowerCase());
                colV.add(val);
            }
        }
        
        for (Enumeration<Object> e = updateMapping.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String val = updateMapping.getProperty(key);
            if (val.equalsIgnoreCase("false")){
                String keyval = mapping.getProperty(key);
                updNamV.remove(key);
                updColV.remove(keyval);
            }
            if (val.equalsIgnoreCase("true")){
                String keyval = mapping.getProperty(key);
                if (!updNamV.contains(key) && nameV.contains(key)){   
                    updNamV.add(key);
                    updColV.add(keyval);
                }
                
            }
        }

        // and now we check the crucial values 
        StringBuffer errMsg = new StringBuffer();
        if (table == null) {
            err++;
            errMsg.append (".) No tablename defined (SQLAuthenticator.table)\r\n");
        }
        if (url == null) {
            errMsg.append (".) No URL to locate the database (SQLAuthenticator.url)\r\n");
            err++;
        }
        if (fc_username==null || fc_password==null) {
            errMsg.append (".) No columns given to check user-credentials for users logging in (username, password)\r\n");
            err++;
        }
        if (!conProps.containsKey("user") || !conProps.containsKey("password")) {
            errMsg.append (".) No connection-credentials given (SQLAuthenticator.mapping.username and SQLAuthenticator.mapping.password)\r\n");
            err++;
        }
        if (warn>0) {
            warnMsg.insert(0, "Encountered warnings:\r\n");
            Server.log (this, warnMsg.toString (), Server.MSG_CONFIG, Server.LVL_MAJOR);
        }
        if (err>0) {
            errMsg.insert(0, " errors:\r\n");
            errMsg.insert(0, err);
            errMsg.insert(0, "Encountered ");
            throw new Exception (errMsg.toString ());
        }
        columns = (String[]) colV.toArray(new String[0]);
        names = (String[]) nameV.toArray(new String[0]);
        updCols = (String[]) updColV.toArray(new String[0]);
        updNames = (String[]) updNamV.toArray(new String[0]);
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

    public String column4property (String prop) {
        int idx = nameV.indexOf(prop);
        if (idx == -1)
            return null;
        return columns[idx].trim();
    }
    
    public synchronized void cacheMetaData (ResultSet rs) throws SQLException {
        if (cachedMetaData)
            return;
        Server.log(Thread.currentThread(), this.toString() + " cacheMetaData", Server.MSG_AUTH, Server.LVL_VERBOSE);
        ResultSetMetaData rsm = rs.getMetaData();
     //   Vector<Object> v = new Vector<Object>();
        types = new int[rsm.getColumnCount()];
        for (int i = 1; i < rsm.getColumnCount(); i++) {
            types[i-1] = rsm.getColumnType(i);
        }
        cachedMetaData=true;
    }
    
    public String toString() {
        return "[DbProperties]";
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

    /**
     * helper method to parse and check string values from config into integers
     * @param name name of property (used for logging)
     * @param value value of property
     * @param def default value
     * @param logger stringbuffer for error messages
     */
    private int parseInt(String name, String value, int def, StringBuffer logger) {
        try {
            int i = Integer.parseInt(value);
            if (i < 0) {
                logger.append (".) SQLAuthenticator.").append(name).append(" was set to a value below zero. Corrected to default of ").append(def).append(".\r\n");
                return def;
            } else {
                return i;
            }
        } catch (NumberFormatException nfe) {
            logger.append (".) SQLAuthenticator.").append(name).append(" wasn't a number. Corrected to default of ").append(def).append(".\r\n");
            return def;
        }
    }

}