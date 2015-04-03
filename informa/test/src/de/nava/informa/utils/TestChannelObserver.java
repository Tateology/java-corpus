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

// $Id: TestChannelObserver.java 314 2003-09-17 20:22:08Z niko_schmuck $

package de.nava.informa.utils;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelObservableIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;

public class TestChannelObserver extends InformaTestCase {

  public TestChannelObserver(String name) {
    super("TestChannelObserver", name);
  }

  public void testObserve() {
    ChannelIF channel = new Channel("Niko's log");
    SimpleChannelObserver observer = new SimpleChannelObserver();
    ((ChannelObservableIF) channel).addObserver(observer);
    assertEquals(0, channel.getItems().size());
    ItemIF item = new Item("Bongo", "Rongoo", null);
    channel.addItem(item);
    assertEquals(1, channel.getItems().size());
    assertTrue(channel.getItems().contains(item));
    assertEquals(item, observer.getMyAddedItem());
    
  }
  
}
