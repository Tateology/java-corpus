package de.nava.informa.impl.hibernate;

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


// $Id: TestChannelGroup.java 788 2006-01-03 00:30:39Z niko_schmuck $

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.InformaHibernateTestCase;

/**
 * Test for making channel groups persistent while using the hibernate
 * mapping backend.
 * 
 * @author Niko Schmuck
 */
public class TestChannelGroup extends InformaHibernateTestCase {

  private static Log logger = LogFactory.getLog(TestChannelGroup.class);

  public TestChannelGroup(String name) {
    super("TestChannelGroup", name);
  }

  public void testChannelGroups() throws Exception {
    ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;
    int chId = -1;
    int chGrpId = -1;
    // our test objects
    ChannelIF channel;
    ChannelGroupIF grp1;
    // -- first create a channel with a category assigned
    try {
      tx = session.beginTransaction();
      String chanName = "Foo Test Channel";
      channel = builder.createChannel(chanName);
      channel.setDescription("Test Channel: " + chanName);
      channel.setLocation(new URL("http://nava.de/test/channelFoo"));
      session.saveOrUpdate(channel);
      grp1 = builder.createChannelGroup("group A");
      grp1.add(channel);
      session.saveOrUpdate(grp1);
      tx.commit();
      chId = (int) channel.getId();
      chGrpId = (int) grp1.getId();
    }
    catch (HibernateException he) {
      logger.warn("trying to rollback the transaction");
      if (tx != null) tx.rollback();
      throw he;
    }
    assertTrue("No valid channel created.", chId >= 0);
    assertTrue("No valid channel group created.", chGrpId >= 0);
    // -- try to retrieve channel and the assigned category
    try {
      logger.info("Searching for channel group " + chGrpId);
      Object result = session.get(ChannelGroup.class, new Long(chGrpId));
      assertNotNull(result);
      ChannelGroupIF cg = (ChannelGroupIF) result;
      logger.info("retrieved channel group --> " + cg);
      assertEquals(1, cg.getAll().size());
      ChannelIF c = (ChannelIF) cg.getAll().iterator().next();
      assertEquals("Foo Test Channel", c.getTitle());
      assertNull(cg.getParent());
    }
    catch (HibernateException he) {
      logger.warn("Error while querying for channel");
      throw he;
    }
    // --- delete test objects
    try {
      tx = session.beginTransaction();
      session.delete(grp1);
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
