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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// $Id: TestFeedManager.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.FeedIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Feed;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.parsers.OPMLParser;

/**
 * @author Jean-Guy Avelin
 */
public class TestFeedManager extends InformaTestCase {

  public TestFeedManager(String name) {
    super("TestFeedManager", name);
  }

  public void testaddFeed() throws Exception {
    FeedManager FM = new FeedManager();

    File inpFile = new File(getDataDir(), "xmlhack-0.91.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);
    String url = new Feed(channel).getLocation().toString();
    FeedIF feed = FM.addFeed(url);
    assertEquals(feed, FM.addFeed(url)); // same reference
  }

  public void testhasFeed() throws Exception {
    FeedManager FM = new FeedManager();

    File inpFile = new File(getDataDir(), "xmlhack-0.91.xml");
    assertFalse(FM.hasFeed(""));

    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    String url = new Feed(channel).getLocation().toString();

    assertFalse(FM.hasFeed(url));

    FM.addFeed(url);
    assertTrue(FM.hasFeed(url));
  }

  public void testaddFeeds() throws Exception {
    FeedManager FM = new FeedManager();

    File inpFile = new File(getDataDir(), "favchannels.opml");
    String opmlUri = inpFile.toURI().toString();
    System.err.println("parsing " + opmlUri);
    Collection feeds = OPMLParser.parse(inpFile);

    Iterator it = feeds.iterator();

    // no feed present in FM
    while (it.hasNext()) {
      FeedIF opmlFeed = (FeedIF) it.next();
      assertFalse(FM.hasFeed(opmlFeed.getLocation().toString()));
    }

    Collection feeds2 = FM.addFeeds(opmlUri);
    assertEquals(19, feeds2.size());

    it = feeds2.iterator();

    while (it.hasNext()) {
      FeedIF opmlFeed = (FeedIF) it.next();
      assertTrue(FM.hasFeed(opmlFeed.getLocation().toString()));
    }

    // compare with collection returned by opml parser
    it = feeds.iterator();
    while (it.hasNext()) {
      FeedIF opmlFeed = (FeedIF) it.next();
      assertTrue(FM.hasFeed(opmlFeed.getLocation().toString()));
    }

    // / just one more test ...
    assertTrue(FM
        .hasFeed("http://www.bbc.co.uk/syndication/feeds/news/ukfs_news/world/rss091.xml"));
  }

}
