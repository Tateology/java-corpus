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


// $Id: TestCreateChannels.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.utils.InformaHibernateTestCase;

/**
 * Class demonstrating the use of the hibernate backend to persist the
 * channel object model to a relational database.
 *
 * @author Niko Schmuck
 */
public class TestCreateChannels extends InformaHibernateTestCase {

  private static Log logger = LogFactory.getLog(TestCreateChannels.class);

  public TestCreateChannels(String name) {
    super("TestCreateChannels", name);
  }

  public void testCreateChannelItems() throws Exception {
    ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;
    int chId = -1;
    // -- first create a channel with two news items
    try {
      tx = session.beginTransaction();
      String chanName = "Foo Test Channel";
      ChannelIF channel = builder.createChannel(chanName, "http://www.nava.de/channelTest");
      channel.setDescription("Test Channel: " + chanName);
      session.saveOrUpdate(channel);
      ItemIF item1 = builder.createItem(channel, "Item 1 for " + chanName,
                                        "First in line", new URL("http://www.sf1.net"));
      session.saveOrUpdate(item1);
      ItemIF item2 = builder.createItem(channel, "Item 2 for " + chanName,
                                        "Second in line", new URL("http://www.sf1.net"));
      session.saveOrUpdate(item2);
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
    // -- try to retrieve channel and children
    try {
      logger.info("Searching for channel " + chId);
      Object result = session.get(Channel.class, new Long(chId));
      assertNotNull(result);
      ChannelIF c = (ChannelIF) result;
      logger.info("retrieved channel --> " + c);
      assertEquals(1, c.getItems().size()); // The itens links where the same and it's unique
      Iterator it_items = c.getItems().iterator();
      while (it_items.hasNext()) {
        ItemIF item = (ItemIF) it_items.next();
        logger.info("  * " + item);
        assertEquals(c, item.getChannel());
      }
    }
    catch (HibernateException he) {
      logger.warn("Error while querying for channel");
      throw he;
    }
  }

  public void testCreatePersistentChannelsFromFeed() throws Exception {
    ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      File inpFile = new File(getDataDir(), "xmlhack-0.91.xml");
      ChannelIF channel = FeedParser.parse(builder, inpFile);
      session.save(channel);
      tx.commit();
      assertEquals(6, channel.getItems().size());
    }
    catch (HibernateException he) {
      logger.warn("trying to rollback the transaction");
      if (tx != null) tx.rollback();
      throw he;
    }
  }

  public void testCreatePersistentChannel() throws Exception {
    ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;
    try {
      logger.info("start new hibernate transaction");
      tx = session.beginTransaction();
      ChannelIF chA = builder.createChannel("Channel A", "http://test.org/A");
      long chA_id = chA.getId();
      chA.setDescription("test channel for hibernate backend");
      logger.info("created chA: " + chA);
      ItemIF itA = builder.createItem(chA, "Simple item", "oh what a desc",
                                      new URL("http://www.sf.net/"));
      logger.info("created itA: " + itA);
      session.save(chA);
      logger.info("saved chA");
      tx.commit();
      logger.info("transaction commited");
      assertEquals(chA_id, chA.getId());
    }
    catch (HibernateException he) {
      logger.warn("trying to rollback the transaction");
      if (tx != null) tx.rollback();
      throw he;
    }
  }

}
