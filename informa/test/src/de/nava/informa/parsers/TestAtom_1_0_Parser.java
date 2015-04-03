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

// $Id: TestAtom_1_0_Parser.java 840 2007-06-24 19:54:25Z wieben $
package de.nava.informa.parsers;

import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.AtomParserUtils;
import de.nava.informa.utils.InformaTestCase;
import de.nava.informa.utils.ParserUtils;

import java.io.File;
import java.io.IOException;

import java.util.*;


public class TestAtom_1_0_Parser extends InformaTestCase {
  public TestAtom_1_0_Parser(String name) {
    super("Atom_1_0_Parser", name);
    this.method_name = name;
  }

  void existsEntry(ChannelIF channel, String title, String content,
                   String link, String dateString) {
    Date date = ParserUtils.getDate(dateString);
    java.util.Iterator itemsIterator = channel.getItems().iterator();
    boolean found = false;

    while (itemsIterator.hasNext()) {
      Item item = (Item) itemsIterator.next();

      if (item.getDate().equals(date)) {
        found = true;

        if (title != null) {
          assertEquals(title, item.getTitle());
        }

        if (content != null) {
          assertTrue(item.getDescription().indexOf(content) >= 0);
        }

        if (link != null) {
          assertEquals(link, item.getLink().toString());
        }
      }
    }

    assertTrue(found);
  }

  public void testGetMode() {
    assertEquals("xml", Atom_1_0_Parser.getMode("xhtml"));
    assertEquals("xml", Atom_1_0_Parser.getMode("application/xml"));
    assertEquals("xml", Atom_1_0_Parser.getMode("application/mathml+xml"));
    assertEquals("xml", Atom_1_0_Parser.getMode("image/svg+xml"));
    assertEquals("escaped", Atom_1_0_Parser.getMode("html"));
    assertEquals("escaped", Atom_1_0_Parser.getMode("text"));
    assertEquals("escaped", Atom_1_0_Parser.getMode((String) null));
    assertEquals("base64", Atom_1_0_Parser.getMode("image/png"));
    assertEquals("base64", Atom_1_0_Parser.getMode("application/pdf"));
  }

  public void testParseBloggerJayant7kCheckFeedDescription()
          throws Exception {
    File inpFile = new File(getDataDir(), "atom-1.0-jayant7k.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    assertEquals(ChannelFormat.ATOM_1_0, channel.getFormat());

    assertEquals("Whatever....", channel.getTitle());
    assertEquals("Jayant Kumar", channel.getCreator());
    assertEquals("Blogger", channel.getGenerator());
    assertNull(channel.getCopyright());

    // test Last pub date 
    // should be  2006-08-17T20:50:43.593-07:00
    Calendar updtDate = new GregorianCalendar(TimeZone.getTimeZone("GMT-7:00"));

    updtDate.set(Calendar.YEAR, 2006);
    updtDate.set(Calendar.MONTH, Calendar.AUGUST);
    updtDate.set(Calendar.DAY_OF_MONTH, 17);
    updtDate.set(Calendar.HOUR_OF_DAY, 20);
    updtDate.set(Calendar.MINUTE, 50);
    updtDate.set(Calendar.SECOND, 43);
    updtDate.set(Calendar.MILLISECOND, 593);
    assertEquals(updtDate.getTime(), channel.getPubDate());
  }

  public void testParseBloggerJayant7kCheckItems() throws Exception {
    File inpFile = new File(getDataDir(), "atom-1.0-jayant7k.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    assertEquals(ChannelFormat.ATOM_1_0, channel.getFormat());
    assertEquals(25, channel.getItems().size());

    // check each item that for null values
    java.util.Iterator itemsIterator = channel.getItems().iterator();

    while (itemsIterator.hasNext()) {
      Item item = (Item) itemsIterator.next();

      assertNotNull(item.getDate());
      assertNotNull(item.getDescription());
      assertNotNull(item.getLink());
      assertNotNull(item.getTitle());
    }
  }

  public void testParseBloggerMyLetterOfTheDayCheckItems()
          throws Exception {
    File inpFile = new File(getDataDir(), "atom-1.0-myletteroftheday.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    assertEquals(ChannelFormat.ATOM_1_0, channel.getFormat());
    assertEquals(9, channel.getItems().size());

    existsEntry(channel,
            "I'm going to pre-post for tomorrow because I wont'...",
            "I'm going to pre-post for tomorrow because I wont' be able " +
                    "to post cuz I will be terribly busy having more fun " +
                    "than humanly possible",
            "http://myletteroftheday.blogspot.com/2006/08/im-going-to-pre-post-for-tomorrow.html",
            "2006-08-19T00:13:00.000");

    existsEntry(channel,
            "Dear lawn (and yes, that’s lawn with a little “L”)...",
            "I torture you, just as you torture me in the spring with your " +
                    "insistent growth and constant need of mowing. It is time " +
                    "for you to lie down, stop growing and turn completely brown.",
            "http://myletteroftheday.blogspot.com/2006/08/dear-lawn-and-yes-thats-lawn-with.html",
            "2006-08-16T21:08:00.000-04:00");

    // we'll now test category functionality
    Iterator it = channel.getItems().iterator();
    int c = 0;
    for (ItemIF itemIF : channel.getItems()) {
      Collection categoryIFs = itemIF.getCategories();
      c++;
      int catSize = categoryIFs.size();
      switch (c) {
        case 1:
          assertEquals(0, catSize);
          break;
        case 2:
          assertEquals(0, catSize);
          break;
        case 3:
          assertEquals(1, catSize);
          assertEquals("lawn care", ((CategoryIF) categoryIFs.iterator().next()).getTitle());
          break;
        case 4:
          assertEquals(2, catSize);
          Iterator itCat = categoryIFs.iterator();
          CategoryIF categoryIF = (CategoryIF) itCat.next();
          assertEquals("Comedy Central", categoryIF.getTitle());
          categoryIF = (CategoryIF) itCat.next();
          assertEquals("Stephen Colbert", categoryIF.getTitle());
          assertEquals("Stephen Colbert", itemIF.getSubject());
          break;
      }
    }
  }

  public void testParsePodcastingExample() throws Exception {
    File inpFile = new File(getDataDir(), "atom-1.0-podcastingexample.xml");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    assertEquals(ChannelFormat.ATOM_1_0, channel.getFormat());

    assertEquals(1, channel.getItems().size());

    //first item
    Item item = (Item) channel.getItems().toArray()[0];

    assertEquals(item.getTitle(), "Atom 1.0");
    assertEquals(item.getDate(), ParserUtils.getDate("2005-07-15T12:00:00Z"));
    assertTrue(item.getDescription().contains("<h1>Show Notes</h1>"));
    assertTrue(item.getDescription()
            .contains("<li>00:01:00 -- Introduction</li>"));
    assertTrue(item.getDescription()
            .contains("<li>00:15:00 -- Talking about Atom 1.0</li>"));
    assertTrue(item.getDescription().contains("<li>00:30:00 -- Wrapping up</li>"));
    assertEquals("http://www.example.org/entries/1", item.getLink().toString());
  }

  public void testPreferenceOrder() {
    assertTrue(AtomParserUtils.getPreferenceOrderForItemLinkType("text/html",
            null) > AtomParserUtils.getPreferenceOrderForItemLinkType("text/plain",
            "alternate"));
    assertTrue(AtomParserUtils.getPreferenceOrderForItemLinkType("text/html",
            "self") > AtomParserUtils.getPreferenceOrderForItemLinkType("text/plain",
            "alternate"));
    assertTrue(AtomParserUtils.getPreferenceOrderForItemLinkType("text/plain",
            null) > AtomParserUtils.getPreferenceOrderForItemLinkType("text/plain",
            "alternate"));
    assertTrue(AtomParserUtils.getPreferenceOrderForItemLinkType("text/plain",
            null) > AtomParserUtils.getPreferenceOrderForItemLinkType("application/png",
            "alternate"));
    assertTrue(AtomParserUtils.getPreferenceOrderForItemLinkType("text/html",
            "enclouser") > AtomParserUtils.getPreferenceOrderForItemLinkType("text/html",
            "alternate"));
  }
}
