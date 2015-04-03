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


// $Id: TestChannelSearcher.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelGroup;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.InformaTestCase;



public class TestChannelSearcher extends InformaTestCase {

  ChannelGroupIF channels;
  ItemIF itemA;
  long channelId;
  
  public TestChannelSearcher(String name) {
    super("TestChannelSearcher", name);
  }

  protected void setUp() throws MalformedURLException {
    ChannelIF channelA = new Channel("example.org");
    channelId = channelA.getId();
    itemA = new Item("Java 1.5 out", "Long awaited...",
                     new URL("http://example.org/1234"));
    channelA.addItem(itemA);
    ItemIF itemB = new Item("XML virus found", "All about it here.",
                            new URL("http://example.org/2345"));
    channelA.addItem(itemB);
    ItemIF itemC = new Item("Quiet Slashdot", "No news today.",
                            new URL("http://example.org/3456"));
    channelA.addItem(itemC);
    channels = new ChannelGroup("Default");
    channels.add(channelA);
  }
  
  public void testIndexItems() throws IOException, QueryParseException {
    assertEquals("Default", channels.getTitle());
    ChannelIF channelA = channels.getById(channelId);
    assertEquals(3, channelA.getItems().size());
    // create the full-text index
    ChannelIndexer indexer = new ChannelIndexer(getIndexDir());
    indexer.indexChannels(true, channels.getAll());
    assertEquals(getIndexDir(), indexer.getIndexDir());
    assertEquals(3, indexer.getNrOfIndexedItems());
    // query the full-text index
    ChannelSearcher searcher = new ChannelSearcher(getIndexDir());
    List results = searcher.search(channels, "java");
    assertEquals(1, results.size());
    ItemResult result = (ItemResult) results.get(0);
    assertEquals(itemA, result.getItem());
    assertEquals(0.61f, result.getScore(), 0.01f);
  }
    
}
