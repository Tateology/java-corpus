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

import java.sql.*;
import freecs.Server;
import freecs.auth.SQLAuthenticator;
import freecs.core.CanceledRequestException;
import freecs.core.ConnectionBuffer;

public class ConnectionPool {

	final SQLAuthenticator authenticator;
	final DbProperties dbProps;
    final PoolElement pool[];
    volatile int p = 0, idCnt=0;
    
    /**
     * Construct a new ConnectionPool for the given authenticator 
     * and the given dbProperties
     * @param authenticator the authenticator this ConnectionPool will used by.
     * @param dbProps the dbProperties describing the datastructure
     */
    public ConnectionPool (SQLAuthenticator authenticator, DbProperties dbProps) {
        this.authenticator = authenticator;
    	this.dbProps = dbProps;
        pool = new PoolElement[dbProps.poolsize];
        if (Server.TRACE_CREATE_AND_FINALIZE) {
            Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
        }
    }

	/**
	 * close every connection to the jdbc-source and remove the PooleElements 
	 */
    public void shutdown () {
        synchronized (this.pool) {
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] == null) {
                	continue;
                }
                pool[i].cleanup ();
                pool[i]=null;
            }
        }
    }

    
	/**
	 * creates an connectionpool-element
	 * @return the PoolElement connected to the jdbc-datasource
	 * @throws Exception
	 */
    private PoolElement createPoolElement () throws Exception {
        idCnt++;
        if (idCnt == Integer.MAX_VALUE) { 
            idCnt = 0;
        }
        Connection con = DriverManager.getConnection (dbProps.url, dbProps.conProps);
        return new PoolElement (this, con, dbProps, idCnt);
    }

    public PoolElement getPoolElement (int retrys, ConnectionBuffer cb) throws Exception {
        // try [retrys] times to retrieve PoolElement
        for (int i = 0; i<retrys; i++) {
            if ((cb != null 
                    	&& !cb.isValid())
                    || Thread.currentThread().isInterrupted())
                throw new CanceledRequestException ("ConnectionBuffer has been invalidated");

            PoolElement el=null;
            try {
                el = this.getPoolElement(cb);
                if (el!=null) // if el isn't null, return it (it was checked for validity)
                    return el;
            } catch (CanceledRequestException cre) {
                throw cre;
            } catch (Exception e) {
                Server.debug(Thread.currentThread() , this.toString() + "getPoolElement: ", e, Server.MSG_AUTH, Server.LVL_MAJOR);
                el = null;
            }
        }
        if (Server.srv.isRunning())
            throw new Exception ("Unable to get available PoolElement");
        else throw new Exception ("Unable to get available PoolElement - Server shutting down");
    }
    
    /**
	 * Gets the next available and valid poolelement or tryes to create a new one
	 * @return the valid and available PoolElement
	 * @throws Exception If creation of PoolElement failes
	 */
    private PoolElement getPoolElement (ConnectionBuffer cb) throws Exception {
        for (int i = 0; i < dbProps.poolsize; i++) {
            if ((cb != null 
                    	&& !cb.isValid())
                    || Thread.currentThread().isInterrupted())
                throw new CanceledRequestException ("ConnectionBuffer has been invalidated");
            if (!Server.srv.isRunning()) {
                StringBuffer sb = new StringBuffer("Created no new Connetion - Server shutting down");
          	    Server.log ("ConnectionPool",  sb.toString(), Server.MSG_AUTH, Server.LVL_VERBOSE);
         	    return null;
         	}
            p++;
            if (p > dbProps.poolsize - 1)
                p=0;
            synchronized (this.pool) {
                if (pool[p]==null) {
                    pool[p] = createPoolElement();
                }
                switch (pool[p].grab()) {
                    case PoolElement.INVALID:
                        pool[p].cleanup();
                        pool[p]=createPoolElement();
                    case PoolElement.IDLE:
                        return pool[p];
                    case PoolElement.ACTIVE:
                        continue;
                }
            }
        }
        return null;
    }
    
    public int size() {
        return pool.length;
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }

    private ConnectionPool() throws Exception { throw new Exception ("not instatiable without arguments"); }
}