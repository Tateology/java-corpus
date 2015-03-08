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


/**
 * Implementing class has to take care of reading and updating chattime and other data.
 * You better extend AbstractAuthenticator to use this interface.
 */

public interface IAuthenticator {
   
    /**
	 *  called when the server is starting up or the config file changes
	 */
    public abstract void init (Properties props, String additionalPrefix) throws Exception;

	   
   /**
    * this is called when the server is going down expectedly
    * close every connection and clean up stuff before stopping
    * @throws Exception to let the log-file know that there was an error
    */
   public abstract void shutdown () throws Exception;
   
   /**
    * get's called on an attempt to log in
    * 
    * @param usr the user name of the user wanting to log in
    * @param pwd the password of the user wanting to log in
    * @param cookie the cookie associated with the user's browser
    * @return the newly constructed User on success || null otherwhise
    * @throws Exception if something unexpected happens
    */
   public abstract User loginUser (String username, String password, String cookie, IRequest request) throws Exception;
   
   /**
    * (re-)checks an existing user object and sets all properties
    * that haven't yet been set on the user object. this method can be
    * used if the actual authentication has already been done through a
    * custom authenticator but data like chatcolor etc still needs to be
    * fetched from the db. 
    */
   public abstract User loginUser (User u, String username, String password, IRequest request) throws Exception;

   
   /**
    * every action which has to be done to log out a user (update chattime and color, ...)
    * has to be done here
    * @param u the user wanting to log out
    * @throws Exception if something unexpected happens
    */
   public abstract void logoutUser (User u) throws Exception;
}