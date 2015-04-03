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
// $Id: TestHibernatePersistenceManager.java 778 2005-09-25 20:30:11Z niko_schmuck $
//

package de.nava.informa.utils.manager.hibernate;

import de.nava.informa.utils.FileUtils;
import de.nava.informa.utils.InformaTestCase;
import de.nava.informa.utils.manager.PersistenceManagerIF;
import de.nava.informa.utils.manager.TestAbstractPersistenceManager;

import java.io.File;

/**
 * Hibernate persistence manager test.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class TestHibernatePersistenceManager extends TestAbstractPersistenceManager {

  private final static String FS = System.getProperty("file.separator");

  static {
    final String sourcePath = InformaTestCase.getDataDir() + FS + "hibernate";

    String dest = InformaTestCase.getOutputDir() + FS + "hibernate";

    File destDir = new File(dest);
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    destDir.deleteOnExit();

    FileUtils.copyFile(new File(sourcePath + FS + "informa.script"), new File(dest + FS + "informa.script"));
    FileUtils.copyFile(new File(sourcePath + FS + "informa.properties"), new File(dest + FS + "informa.properties"));

    // Set properties
    System.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    System.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    System.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:" + dest + FS + "informa");
    System.setProperty("hibernate.connection.username", "sa");
    System.setProperty("hibernate.connection.password", "");
    System.setProperty("hibernate.show_sql", "false");
  }

  /**
   * Returns manager to be tested.
   *
   * @return manager to be tested.
   */
  protected PersistenceManagerIF getManager() {
    return new PersistenceManager();
  }
}
