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


// $Id: TestRSS_0_91_Exporter.java 504 2004-05-13 22:55:36Z niko_schmuck $

package de.nava.informa.exporters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import de.nava.informa.core.ChannelExporterIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.utils.InformaTestCase;

public class TestRSS_0_91_Exporter extends InformaTestCase {

  public TestRSS_0_91_Exporter(String name) {
    super("TestRSS_0_91_Exporter", name);
  }

  public void testExportChannel()
    throws IOException, MalformedURLException, ParseException {

    String ch_title = "The Great Demo Channel";
    String ch_desc = "Just a very simple short description.";
    
    // create dummy channel
    ChannelIF channel = new Channel(ch_title);
    channel.setDescription(ch_desc);
    channel.setSite(new URL("http://nava.de"));
    ItemIF itemA = new Item("Bugo", "All about it!",
                            new URL("http://nava.de/huhu2002"));
    itemA.setFound(new Date());
    channel.addItem(itemA);
    // TODO: what about markup here ???
    ItemIF itemB = new Item("SoCool",
                            "????**$12 @??? # <strong>should</strong> work",
                            new URL("http://nava.de/very/nested/98"));
    itemB.setFound(new Date());
    channel.addItem(itemB);
    assertEquals(2, channel.getItems().size());
    // export this channel to file (encoding: utf-8)
    String basename = "export-rss091.xml";
    String exp_file = getOutputDir() + FS + basename;
    ChannelExporterIF exporter = new RSS_0_91_Exporter(exp_file);
    exporter.write(channel);

    // clean channel object
    channel = null;
    
    // read in again
    File inpFile = new File(exp_file);
    channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    assertEquals(ch_title, channel.getTitle());
    assertEquals(ch_desc, channel.getDescription());
    
    Collection items = channel.getItems();
    assertEquals(2, items.size());
    Iterator it = items.iterator();
    ItemIF item = (ItemIF) it.next();
    if (item.equals(itemA)) {
      assertEquals(item, itemA);
      item = (ItemIF) it.next();
      assertEquals(item, itemB);
    } else {
      assertEquals(item, itemB);
      item = (ItemIF) it.next();
      assertEquals(item, itemA);
    }
  }

}
