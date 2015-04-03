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


// $Id: TestChannelBuilder.java 314 2003-09-17 20:22:08Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.net.MalformedURLException;
import java.net.URL;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.InformaTestCase;

public class TestChannelBuilder extends InformaTestCase {

  public TestChannelBuilder(String testname) {
    super("TestChannelBuilder", testname);
  }

  public void testCreateChannel() {
    ChannelBuilderIF builder = new ChannelBuilder();
    ChannelIF chA = builder.createChannel("myChannel");
    assertEquals("myChannel", chA.getTitle());
  }

  public void testCreateItem() throws MalformedURLException {
    ChannelBuilderIF builder = new ChannelBuilder();
    ChannelIF chA = builder.createChannel("myChannel");
    ItemIF itA = builder.createItem(chA, "first item", "descr of item",
                                    new URL("http://sf.net/projects/informa"));
    itA.setCreator("TestChannelBuilder");
    assertEquals("first item", itA.getTitle());
    itA = null;
    // test retrieval
    ItemIF itB = (ItemIF) chA.getItems().iterator().next();
    assertEquals("first item", itB.getTitle());
    assertEquals("TestChannelBuilder", itB.getCreator());    
  }

}
