//
// Informa -- RSS Library for Java
// Copyright (c) 2002 by Niko Schmuck
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// $Id: HibernateUtil.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.manager.hibernate;

import de.nava.informa.impl.hibernate.SessionHandler;
import de.nava.informa.utils.manager.PersistenceManagerException;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;

import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Lock;

/**
 * Hibernate session utility. Using it to get sessions you will ensure
 * that only session is created only once per thread.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
final class HibernateUtil {

  private static final Logger       LOG = Logger.getLogger(HibernateUtil.class.getName());
  private static final ThreadLocal<Session>  SESSION = new ThreadLocal<Session>();

  private static SessionHandler     sessionHandler;
  private static boolean            inited;
  private static Lock               lock;

  static {
    inited = false;
    lock = new Lock();
  }

  /**
   * Hidden constructor of utility class.
   */
  private HibernateUtil() {
  }

  /**
   * Performs initialization.
   *
   * @throws HibernateException if problems with Hibernate occur.
   */
  private static synchronized void init()
    throws HibernateException {

    // Create the SessionFactory
    sessionHandler = SessionHandler.getInstance(System.getProperties());
    inited = true;
  }

  /**
   * Opens new session or returns currently open session for this thread.
   *
   * @return session object.
   *
   * @throws HibernateException if something with session creation goes wrong.
   */
  public static Session openSession() throws HibernateException {
    if (!inited) init();

    Session s = SESSION.get();
    if (s != null) {
      LOG.log(Level.WARNING, "Openning session more than once from the same thread!",
        new Exception("Dump"));

      s.clear();

      return s;
    } else
    {
      try {
        lock.lock();
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted waiting for session.");
      }

      s = sessionHandler.getSession();
      SESSION.set(s);
    }

    return s;
  }

  /**
   * Closes previousely opened session.
   */
  public static void closeSession() {
    Session s = SESSION.get();

    SESSION.set(null);

    if (s != null) {
      try {
        s.close();
      } catch (HibernateException e) {
        LOG.log(Level.SEVERE, "Could not close session.", e);
        // We can do nothing here.
        // The other session will be opened next time.
      }

      lock.unlock();
    } else {
      LOG.log(Level.SEVERE, "Broken sequence of calls. Session is not opened or already closed.");
      try {
        throw new NullPointerException();
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Makes a try to lock object. This will save us great number of SQL statements
   * in several cases. It's not a big problem if locking is not possible.
   *
   * @param o object to lock.
   * @param s session to lock object in.
   */
  public static void lock(Object o, Session s) {
    try {
      s.lock(o, LockMode.NONE);
    } catch (HibernateException e) {
      // Well, it's possible that object is dirty.
    }
  }

  /**
   * Saves object into storage.
   *
   * @param object  object to save.
   *
   * @throws PersistenceManagerException in case of any problems during saving.
   */
  public static void saveObject(final Object object)
    throws PersistenceManagerException {

    // Save object in new session
    saveObject(object, null);
  }

  /**
   * Saves object into storage.
   *
   * @param object  object to save.
   *
   * @throws PersistenceManagerException in case of any problems during saving.
   */
  public static void saveObject(final Object object, Session session)
    throws PersistenceManagerException {
    boolean isForeignSession = session != null;

    try {
      // Open new session if we are not in the foreign one
      if (!isForeignSession) {
        session = openSession();
      }

      session.save(object);

      // Flush only if it's our own session
      if (!isForeignSession) {
        session.flush();
        session.connection().commit();
      }
    } catch (Exception e) {
      // Rollback transaction if we are owners of session
      if (!isForeignSession) {
        try {
          session.connection().rollback();
        } catch (Exception e1) {
          // Error is not recoverable.
        }
      }

      LOG.log(Level.SEVERE, "Couldn't save object.", e);
      throw new PersistenceManagerException("Couldn't save object.", e);
    } finally {
      // Close session if we had opened it
      if (!isForeignSession) {
        closeSession();
      }
    }
  }

  /**
   * Updates object in storage.
   *
   * @param object  object to update.
   *
   * @throws PersistenceManagerException in case of any problems during updating.
   */
  public static void updateObject(final Object object)
    throws PersistenceManagerException {

    updateObject(object, null);
  }

  /**
   * Updates object in storage.
   *
   * @param object  object to update.
   * @param session session to use. If NULL then new session is opened.
   *
   * @throws PersistenceManagerException in case of any problems during updating.
   */
  public static void updateObject(final Object object, Session session)
    throws PersistenceManagerException {

    boolean isForeignSession = session != null;

    try {
      // Open new session if we are not in the foreign one
      if (!isForeignSession) {
        session = openSession();
      }

      session.update(object);

      // Flush only if it's our own session
      if (!isForeignSession) {
        session.flush();
        session.connection().commit();
      }
    } catch (Exception e) {
      // Rollback transaction if we are owners of session
      if (!isForeignSession) {
        try {
          session.connection().rollback();
        } catch (Exception e1) {
          // Error is not recoverable.
        }
      }

      LOG.log(Level.SEVERE, "Couldn't update object.", e);
      throw new PersistenceManagerException("Couldn't update object.", e);
    } finally {
      // Close session if we had opened it
      if (!isForeignSession) {
        closeSession();
      }
    }
  }

  /**
   * Deletes object from database using existing session.
   *
   * @param object  object to delete.
   * @param session session to use. If NULL then new session is opened.
   *
   * @throws PersistenceManagerException in case of any problems during deletion.
   */
  public static void deleteObject(final Object object, Session session)
    throws PersistenceManagerException {

    boolean isForeignSession = session != null;

    try {
      // Open new session if we are not in the foreign one
      if (!isForeignSession) {
        session = openSession();
      }

      session.delete(object);

      // Flush only if it's our own session
      if (!isForeignSession) {
        session.flush();
        session.connection().commit();
      }
    } catch (Exception e) {
      // Rollback transaction if we are owners of session
      if (!isForeignSession) {
        try {
          session.connection().rollback();
        } catch (Exception e1) {
          // Error is not recoverable.
        }
      }
      LOG.log(Level.SEVERE, "Couldn't delete object.", e);
      throw new PersistenceManagerException("Couldn't delete object.", e);
    } finally {
      // Close session if we had opened it
      if (!isForeignSession) {
        closeSession();
      }
    }
  }
}
