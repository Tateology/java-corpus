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


// $Id: TestChannelIndexer.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.InformaTestCase;

public class TestChannelIndexer extends InformaTestCase {

  public TestChannelIndexer(String name) {
    super("TestChannelIndexer", name);
  }

  public void testIndexItems() throws MalformedURLException, IOException {
    Collection<ItemIF> items = new ArrayList<ItemIF>();
    ItemIF itemA = new Item("Java 1.5 out", "Long awaited...",
                            new URL("http://example.org/1234"));
    items.add(itemA);
    ItemIF itemB = new Item("XML virus found", "All about it here.",
                            new URL("http://example.org/2345"));
    items.add(itemB);
    assertEquals(2, items.size());
    ChannelIndexer indexer = new ChannelIndexer(getIndexDir());
    indexer.indexItems(true, items);
    assertEquals(getIndexDir(), indexer.getIndexDir());
    assertEquals(2, indexer.getNrOfIndexedItems());
  }

  // TODO: add test case for indexItems(false, items)
    
}
