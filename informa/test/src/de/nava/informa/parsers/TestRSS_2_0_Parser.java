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


// $Id: TestRSS_2_0_Parser.java 812 2006-11-26 21:52:49Z niko_schmuck $

package de.nava.informa.parsers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.utils.InformaTestCase;

public class TestRSS_2_0_Parser extends InformaTestCase {

  static ChannelIF channel;
  static URL inpURL;
  
  public TestRSS_2_0_Parser(String name)
    throws IOException, ParseException {
    
    super("TestRSS_2_0_Parser", name);
    if (channel == null) {
      File inpFile = new File(getDataDir(), "informa-projnews.xml");
      channel = FeedParser.parse(new ChannelBuilder(), inpFile);
      // for later reference
      inpURL = inpFile.toURL();
    }
  }

  public void testCreatedChannel() {
    assertEquals("SourceForge.net: SF.net Project News: RSS Library for Java", 
                 channel.getTitle());
    assertEquals(313, channel.getDescription().length());
    assertEquals(inpURL, channel.getLocation());
    assertEquals("http://sourceforge.net/projects/informa/", 
                 channel.getSite().toString());
    assertEquals(ChannelFormat.RSS_2_0, channel.getFormat());
  }

  public void testItemDetails() {
    assertEquals(5, channel.getItems().size());
    ItemIF item = searchForItem(channel, "First alpha release of Informa");
    assertNotNull("Item not found", item);
    assertEquals("First alpha release of Informa is now out", item.getTitle());
    assertEquals("http://sourceforge.net/forum/forum.php?forum_id=200619",
                 item.getLink().toString());
    assertEquals(121, item.getDescription().length());
    Calendar expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 0, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), item.getDate());
  }
  
  public void testParseW3CSynd() throws Exception {
    File inpFile = new File(getDataDir(), "msdn-rss2.xml");
    ChannelIF channel_msdn = FeedParser.parse(new ChannelBuilder(), inpFile);
    assertEquals("MSDN Just Published", channel_msdn.getTitle());
    assertEquals(63, channel_msdn.getItems().size());
    assertEquals(ChannelFormat.RSS_2_0, channel_msdn.getFormat());
  }

  public void testParseYahooBusiness() throws Exception {
    File inpFile = new File(getDataDir(), "business.rss");
    ChannelIF channel_business = FeedParser.parse(new ChannelBuilder(), inpFile);
    assertEquals("Yahoo! News - Business", channel_business.getTitle());
    assertEquals("http://news.yahoo.com/news?tmpl=index&cid=1885", 
                 channel_business.getSite().toString());
    assertEquals(9, channel_business.getItems().size());  // two pairs of duplicates
    assertEquals(ChannelFormat.RSS_2_0, channel_business.getFormat());
    ItemIF item = searchForItem(channel_business, "Taking the World by Hand (Forbes.com)");
    assertNotNull("Item not found", item);
  }

  public void testParseWithDefaultNS() throws Exception {
    File inpFile = new File(getDataDir(), "rss2withNS.xml");
    ChannelIF channel_wns = FeedParser.parse(new ChannelBuilder(), inpFile);
    assertEquals("Blogging Roller", channel_wns.getTitle());
    assertEquals(15, channel_wns.getItems().size());
    assertEquals(ChannelFormat.RSS_2_0, channel_wns.getFormat());
    assertEquals(((CategoryIF)((ItemIF)channel_wns.getItems().toArray()[0]).getCategories().toArray()[0]).getTitle(), "Roller");
  }

  public void testParseContentEncoded() throws Exception {
    File inpFile = new File(getDataDir(), "rss20-content-encoded.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    ItemIF[] items = (ItemIF[])channel.getItems().toArray(new ItemIF[0]);
    assertEquals(3, items.length);
    assertEquals("1&amp;", items[0].getDescription());
    assertEquals("2", items[1].getDescription());
    assertEquals("3&", items[2].getDescription());
  }
}
