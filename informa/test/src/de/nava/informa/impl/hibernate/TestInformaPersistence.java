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

// $Id: TestInformaPersistence.java 788 2006-01-03 00:30:39Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import de.nava.informa.core.ChannelBuilderException;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelObserverIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.ChannelRegistry;
import de.nava.informa.utils.InformaTestCase;

/**
 * TestInformaPersistence
 * 
 */
public class TestInformaPersistence extends InformaTestCase 
  implements ChannelObserverIF {

  SessionHandler handler;
  Connection conn;
  ChannelBuilder builder;
  ChannelRegistry channelRegistry;

  private static Log log = LogFactory.getLog(TestInformaPersistence.class);

  public TestInformaPersistence(String testname) {
    super("TestInformaPersistence", testname);
  }

  public void setUp() throws HibernateException, SQLException, 
                                          ChannelBuilderException {
    handler = SessionHandler.getInstance();
    handler.getSession();
    builder = new ChannelBuilder(handler);
    builder.beginTransaction();
    channelRegistry = new ChannelRegistry(builder);
    builder.endTransaction();
  }

  public void tearDown() throws SQLException, ChannelBuilderException {
    if (conn != null)
      conn.close();
    if (builder != null)
      builder.close();
    handler = null;
  }

  // ==================================================================
  
  public void testChannelRegistry() {
    /* these tests now work correctly
    			int chanA = createChanWithItems("Fee channel", 3);
    			int chanB = createChanWithItems("Fie channel", 12);
    			int chanC = createChanWithItems("Foe channel", 44);
    			verifyChannel(chanA);
    			verifyChannel(chanB);
    			verifyChannel(chanC);
    */

    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          log.warn("Interrupted while running testcase: " + e);
        }
      }
    };
    // TODO: investigate more about multi-threaded JUnit tests (GroboUtils?)
    t.start();
    lookupAndCreateChannel("Joho", 
                           "http://www.hyperorg.com/blogger/index.rdf");
    lookupAndCreateChannel("Bitwaste",
                           "http://www.bitwaste.com/wasted-bits/index.cgi/index.rss");
    lookupAndCreateChannel("AllConsuming",
                           "http://www.allconsuming.net/xml/recent_consumption.rss.xml");
  }

  // ==================================================================

  private Channel lookupAndCreateChannel(String label, String url) {
    log.info("Looking up channel: " + label);
    Channel achan = locateChannel(url);
    if (achan != null) {
      verifyChannel(label, achan.getId());
    } else {
      log.info(label + " not yet located.");
    }
    achan = getChannel(url, achan);
    verifyChannel(label + " that was created:", achan.getId());
    return achan;
  }

  private Channel getChannel(String string, Channel existing) {
    log.info("Getting channel: " + string);
    synchronized (builder) {
      Channel aChannel = null;
      try {
        builder.beginTransaction();
        if (existing == null) {
          aChannel =
            (Channel) channelRegistry.addChannel(new URL(string), 30, false);
        } else {
          aChannel = (Channel) channelRegistry.addChannel(existing, false, 30);
        }
        long chanid = aChannel.getId();
        log.info("Got channel: " + aChannel + "(id =" + chanid + ")");
        aChannel.addObserver(this);
        builder.endTransaction();
        channelRegistry.activateChannel(aChannel, 60);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (ChannelBuilderException e1) {
        e1.printStackTrace();
      }
      return aChannel;
    }
  }

  private Channel locateChannel(String xmlURL) {
    synchronized (builder) {
      Channel aChannel = null;
      try {
        builder.beginTransaction();
        Session sess = builder.getSession();
        List channels =
          sess.createQuery("from Channel as chan where chan.locationString = :loc")
              .setParameter("loc", xmlURL, Hibernate.STRING).list();
        log.info("***locateChannel for " + xmlURL + " produced these: " + channels);
        if (channels.size() >= 1) { // for now just take the last one
          aChannel = (Channel) channels.get(channels.size() - 1);
        }
        builder.endTransaction();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return aChannel;
    }
  }

  // TODO: split this out into InformaHibernateTestCase for better reuse

  private void verifyChannel(String label, long chan_id) {
    try {
      Session session = handler.getSession();
      log.info(label + chan_id);
      Channel aChan = (Channel) session.load(Channel.class, new Long(chan_id));
      assertEquals((long) chan_id, aChan.getId());
      assertNotNull(aChan.getTitle());
      logChannel(aChan);
      session.flush();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void logChannel(Channel aChan) {
    String logthis = "Channel: " + aChan + ":";
    Iterator items = aChan.getItems().iterator();
    while (items.hasNext()) {
      Item item = (Item) items.next();
      logthis = logthis + ":" + item;
    }
    log.info(logthis);
  }

  // --------------------------------------------------------------------------
  // currently unused
  // --------------------------------------------------------------------------

  /*
  private int createChanWithItems(String chanName, int count) throws Exception {
    synchronized (builder) {
      int chan_id = -1;
      try {
        builder.beginTransaction();
        log.info("createChanWithItems starting...");
        Channel channel = (Channel) builder.createChannel(chanName);
        chan_id = channel.getIntId();
        channel.setDescription("Test Channel: " + chanName + " ID = " + chan_id);
        log.info("created " + chanName + " ID = " + chan_id);
        log.info("saved " + chanName);

        for (int i = 0; i < count; i++) {
          builder.createItem(channel, "Item " + i + " for " + chanName,
                                      "A wonderful description!",
                                      new URL("http://www.sf.net/"));
        }
        logChannel(channel);
        builder.endTransaction();
        log.info("transaction commited. CHAN ID = " + chan_id);
      } catch (ChannelBuilderException he) {
        he.printStackTrace();
        throw he;
      }
      return chan_id;
    }
  }
  */

  // ----------------------------------------------------------------------
  // Implementation of ChannelObserverIF
  // ----------------------------------------------------------------------

  public void itemAdded(ItemIF newItem) {
    log.info("Added item " + newItem);
  }

  public void channelRetrieved(ChannelIF channel) {
    log.info("Added channel " + channel);
  }

}
