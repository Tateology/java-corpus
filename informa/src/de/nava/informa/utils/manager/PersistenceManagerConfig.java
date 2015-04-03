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
// $Id: PersistenceManagerConfig.java 662 2004-08-24 09:43:41Z spyromus $
//

package de.nava.informa.utils.manager;

/**
 * Persistence manager configuration class. At application start it reads the value of
 * JVM property <code>informa.persistencemanager</code> and
 * instantiates class with given name as persistence manager. Class, mentioned in property
 * should implement <code>PersistenceManagerIF</code> interface.
 * <p/>
 * Client application can get instance of persistence manager using
 * <code>getPersistenceManager()</code> method. Single instance of persistence manager
 * is shared.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public final class PersistenceManagerConfig {

  private static PersistenceManagerIF manager;

  /** Initialize manager. */
  static {
    // Read the value of property.
    final String propertyName = "informa.persistencemanager";
    String className = System.getProperty(propertyName);
    try {
      setPersistenceManagerClassName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Hidden constructor of utility class.
   */
  private PersistenceManagerConfig() {
  }

  /**
   * Sets the name of <code>PersistenceManagerIF</code> interface implementation class.
   * If class is successfully located instance can be taken with
   * <code>getPersistenceManager()</code> method.
   *
   * @param className name of implemenation class.
   * @throws ClassNotFoundException if class not found.
   * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
   * @throws InstantiationException if this Class represents an abstract class, an interface,
   *                                an array class, a primitive type, or void;
   *                                or if the class has no nullary constructor;
   *                                or if the instantiation fails for some other reason.
   */
  public static void setPersistenceManagerClassName(String className)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    manager = null;

    // If property is specified instantiate manager.
    if (className != null) {
      manager = (PersistenceManagerIF) Class.forName(className).newInstance();
    }
  }

  /**
   * Returns instance of persistence manager chosen by application.
   *
   * @return instance or NULL if manager is not instantiated.
   */
  public static PersistenceManagerIF getPersistenceManager() {
    return manager;
  }
}
