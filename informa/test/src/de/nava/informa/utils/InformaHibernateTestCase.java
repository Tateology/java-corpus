//
// Informa -- RSS Library for Java
// Copyright (c) 2002-2003 by Niko Schmuck
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


// $Id: InformaHibernateTestCase.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.utils;

import java.io.InputStream;
import java.util.Properties;

import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.impl.hibernate.SessionHandler;

/**
 * Base class for unit tests of classes related to
 * the hibernate backend of the informa library.
 *
 * @author Niko Schmuck
 */
public class InformaHibernateTestCase extends InformaTestCase {

  private static Log logger = LogFactory.getLog(InformaHibernateTestCase.class);

  /**
   * The name of the properties file containing test relevant settings.
   * This file must be accessible from the CLASSPATH ('/' means
   * default package).
   */
  public static final String PROP_FILENAME = "/hibernate-unittest.properties";

  protected static SessionHandler sessionHandler;
  protected Session session;

  public InformaHibernateTestCase(String testcase_name, String method_name) {
    super(testcase_name, method_name);
  }

  public void setUp() throws Exception {
    super.setUp();
    if (sessionHandler == null) {
      // read in properties file
      Properties props = new Properties();
      InputStream in = this.getClass().getResourceAsStream(PROP_FILENAME);
      if (in != null)
        props.load(in);
      else
        logger.warn("No test properties file (" + PROP_FILENAME +
                    ") found in CLASSPATH.");
      // use those properties for hibernate connection
      sessionHandler = SessionHandler.getInstance(props);
    }
    session = sessionHandler.getSession();
  }

  public void tearDown() throws Exception {
    session.close();
    super.tearDown();
  }

}
