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
//

// $Id: TestOPML_1_1_Parser.java 843 2007-06-27 20:27:30Z wieben $

package de.nava.informa.parsers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import de.nava.informa.core.FeedIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.InformaTestCase;
import de.nava.informa.impl.basic.ChannelBuilder;

public class TestOPML_1_1_Parser extends InformaTestCase {

  static Collection<FeedIF> feeds;
  static URL inpURL;

  public TestOPML_1_1_Parser(String name) throws IOException, ParseException {
    super("TestOPML_1_1_Parser", name);
    if (feeds == null) {
      File inpFile = new File(getDataDir(), "favchannels.opml");
      feeds = OPMLParser.parse(inpFile);
      // for later reference
      inpURL = inpFile.toURL();
    }
  }

  public void testNumberFeedsReadIn() {
    assertEquals(19, feeds.size());
  }

  public void testReadInFeeds() {
    Iterator<FeedIF> it = feeds.iterator();
    boolean found = false;
    while (it.hasNext()) {
      FeedIF feed = it.next();
      if (feed.getTitle().startsWith("Google Weblog")) {
        assertEquals("Google Weblog", feed.getTitle());
        assertEquals("rss", feed.getContentType());
        assertEquals("http://google.blogspace.com/index.xml",
                feed.getLocation().toString());
        assertEquals("http://google.blogspace.com/",
                feed.getSite().toString());
        found = true;
        break;
      }
    }
    assertTrue("Couldn't find item looked for.", found);
  }

  public void testReadInOPML_10_PodcastAlleyFeed() throws IOException, ParseException {
    Collection<FeedIF> c = OPMLParser.parse(new File(getDataDir(), "opml-1.0-podcastalley.com.xml"));
    assertEquals(10, c.size());
  }
}
