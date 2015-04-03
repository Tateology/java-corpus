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


// $Id: TestItemOrder.java 314 2003-09-17 20:22:08Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.InformaTestCase;

public class TestItemOrder extends InformaTestCase {

  private static ChannelBuilderIF builder = new ChannelBuilder();
  
  public TestItemOrder(String testname) {
    super("TestItemOrder", testname);
  }

  private static ChannelIF prepare() throws MalformedURLException {
    ChannelIF chA = builder.createChannel("myChannel");
    builder.createItem(chA, "first item", "descr1", new URL("http://1.net/"));
    builder.createItem(chA, "second item", "22", new URL("http://2.net/"));
    builder.createItem(chA, "third item", "333", new URL("http://3.net/"));
    builder.createItem(chA, "fourth item", "4", new URL("http://4.net/"));
    builder.createItem(chA, "fifth item", "5555", new URL("http://5.net/"));
    builder.createItem(chA, "sixth item", "6", new URL("http://6.net/"));
    builder.createItem(chA, "seventh item", "7777", new URL("http://7.net/"));
    builder.createItem(chA, "eight item", "8", new URL("http://8.net/"));
    return chA;
  }

  public void testCreatedItemCount() throws MalformedURLException {
    ChannelIF channel = prepare();
    assertEquals(8, channel.getItems().size());
  }
  
  public void testCreatedItemInOrder() throws MalformedURLException {
    ChannelIF channel = prepare();
    Iterator it = channel.getItems().iterator();
    int idx = 1;
    while (it.hasNext()) {
      ItemIF item = (ItemIF) it.next();
      assertEquals("http://" + idx + ".net/", item.getLink().toString());
      idx++;
    }
  }

  public void testCreatedItemAddOne() throws MalformedURLException {
    ChannelIF channel = prepare();
    ItemIF firstItem = (ItemIF) channel.getItems().iterator().next();
    channel.removeItem(firstItem);
    assertEquals(7, channel.getItems().size());
    builder.createItem(channel, "another one", "9", new URL("http://9.net/"));
    assertEquals(8, channel.getItems().size());
    Iterator it = channel.getItems().iterator();
    int idx = 2;
    while (it.hasNext()) {
      ItemIF item = (ItemIF) it.next();
      assertEquals("http://" + idx + ".net/", item.getLink().toString());
      idx++;
    }
  }

  public void testRetrieveItem() throws MalformedURLException {
    ChannelIF channel = prepare();
    ItemIF firstItem = (ItemIF) channel.getItems().iterator().next();
    long firstId = firstItem.getId();    
    ItemIF retrievedItem = channel.getItem(firstId);
    assertEquals(firstItem, retrievedItem);
  }

}
