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


// $Id: TestChannelCategory.java 788 2006-01-03 00:30:39Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.InformaHibernateTestCase;

/**
 * Test for making channel categories persistent while using the hibernate
 * mapping backend.
 *
 * @author Niko Schmuck
 */
public class TestChannelCategory extends InformaHibernateTestCase {

  private static Log logger = LogFactory.getLog(TestChannelCategory.class);

  public TestChannelCategory(String name) {
    super("TestChannelCategory", name);
  }

  public void testChannelCategories() throws Exception {
    ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;
    int chId = -1;
    // our test objects
    ChannelIF channel;
    CategoryIF cat1, cat2;
    // -- first create a channel with a category assigned
    try {
      tx = session.beginTransaction();
      // create channel
      String chanName = "Foo Test Channel";
      channel = builder.createChannel(chanName);
      channel.setDescription("Test Channel: " + chanName);
      session.saveOrUpdate(channel);
      // create cat1
      cat1 = builder.createCategory(null, "Root Cat");
      session.saveOrUpdate(cat1);
      // create cat2
      cat2 = builder.createCategory(cat1, "Agent_A");
      session.saveOrUpdate(cat2);
      channel.addCategory(cat2);
      session.saveOrUpdate(channel);
      tx.commit();
      chId = (int) channel.getId();
    }
    catch (HibernateException he) {
      logger.warn("trying to rollback the transaction");
      if (tx != null) tx.rollback();
      throw he;
    }
    assertTrue("No valid channel created.", chId >= 0);
    // -- try to retrieve channel and the assigned category
    try {
      logger.info("Searching for channel " + chId);
      Object result = session.get(Channel.class, new Long(chId));
      assertNotNull(result);
      ChannelIF c = (ChannelIF) result;
      logger.info("retrieved channel --> " + c);
      assertEquals(1, c.getCategories().size());
      CategoryIF cat = (CategoryIF) c.getCategories().iterator().next();
      assertEquals("Agent_A", cat.getTitle());
      assertNotNull(cat.getParent());
      assertEquals("Root Cat", cat.getParent().getTitle());
    }
    catch (HibernateException he) {
      logger.warn("Error while querying for channel");
      throw he;
    }
    // -- delete test objects
    try {
      tx = session.beginTransaction();
      session.delete(cat1);
      session.delete(cat2);
      session.delete(channel);
      tx.commit();
    }
    catch (HibernateException he) {
      logger.warn("trying to rollback the transaction");
      if (tx != null) tx.rollback();
      throw he;
    }
  }

}
