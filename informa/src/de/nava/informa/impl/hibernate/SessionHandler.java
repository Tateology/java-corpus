//
// Informa -- RSS Library for Java
// Copyright (c) 2002, 2003 by Niko Schmuck
//
// Niko Schmuck
// http://sourceforge.net/projects/informa
// mailto:niko_schmuck@users.sourceforge.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE. If the license is not included with this distribution,
// you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
// or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
// MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

// $Id: SessionHandler.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import java.sql.Connection;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton class from which hibernate sesssions may be retrieved needed
 * for transactions.
 *
 * @author Niko Schmuck
 */
public class SessionHandler {

  private static Log logger = LogFactory.getLog(SessionHandler.class);

  private static SessionHandler myInstance;

  private Configuration cfg;
  private SessionFactory sessFactory;
  private Connection conn;
  private Session curSession;

  /**
   * Constructor which configures hibernate, in this order:
   * <ol>
   *   <li>Reads hibernate.cfg.xml or hibernate.properties file from the
   *       CLASSPATH to retrieve information about how the database can be
   *       accessed (JDBC connection properties).</li>
   *   <li>Then reads in the definiton files for all related informa hibernate
   *       classes (*.hbm.xml)</li>
   *   <li>Finally, if supplied, applies a Properties object to do a final
   *       override.</li>
   * </ol>
   *
   * @throws HibernateException In case a problem occurred while configuring
   *         hibernate or creating the session factory.
   */
  private SessionHandler(Properties props) throws HibernateException {
    // reads in hibernate.properties implictly for database connection settings
    cfg = new Configuration();

    // attempt to use standard config file named hibernate.cfg.xml
    try {
      cfg.configure();
    } catch (HibernateException he) {
      logger.info("Can't find \"hibernate.cfg.xml\" in classpath.");
    }

    // add base classes
    cfg
      .addClass(Channel.class)
      .addClass(Item.class)
      .addClass(ItemGuid.class)
      .addClass(ItemEnclosure.class)
      .addClass(ItemSource.class)
      .addClass(Cloud.class)
      .addClass(Category.class)
      .addClass(ChannelGroup.class)
      .addClass(ChannelSubscription.class)
      .addClass(Image.class)
      .addClass(ItemMetadata.class)
      .addClass(TextInput.class);

    // If Properties were supplied then use them as the final override
    if (props != null)
      cfg.addProperties(props);

    // get session factory (expensive)
    sessFactory = cfg.buildSessionFactory();
  }

  /**
   * Returns the one and only instance which can be used to obtain a
   * {@link Session} for further operation with the hibernate objects.
   *
   * @throws HibernateException In case a problem occurred while configuring
   *         hibernate or creating the session factory.
   */
  public static synchronized SessionHandler getInstance(Properties props)
    throws HibernateException {

    if (myInstance == null) myInstance = new SessionHandler(props);
    return myInstance;
  }

  /**
   * Returns the singelton instance, calling
   * {@link #getInstance(Properties)} with properties set to null.
   */
  public static SessionHandler getInstance() throws HibernateException {
    return getInstance(null);
  }

  /**
   * Gets hibernate session object, if JDBC <code>Connection</code> was
   * set earlier this will be used for the opening a hibernate session.
   */
  public Session getSession() throws HibernateException {
    if (conn != null)
      curSession = sessFactory.openSession(conn);
    else
      curSession = sessFactory.openSession();
    return curSession;
  }

  /**
   * Gets a a new session whilst using an existing JDBC connection.
   *
   * @param conn JDBC connection
   */
  public Session getSession(Connection conn) {
    this.conn = conn;
    curSession = sessFactory.openSession(conn);
    return curSession;
  }

  /**
   * Gets the default JDBC Connection object.
   */
  public Connection getConnection() {
    return conn;
  }

  /**
   * Sets the default JDBC Connection object.
   */
  public void setConnection(Connection connection) {
    conn = connection;
  }

  /**
   * Returns true if session is open.
   */
  public boolean isSessionOpen()
  {
    return curSession.isOpen();
  }

}
