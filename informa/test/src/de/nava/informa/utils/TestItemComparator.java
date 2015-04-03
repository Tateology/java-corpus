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


// $Id: TestItemComparator.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

public class TestItemComparator extends InformaTestCase {

  public TestItemComparator(String name) {
    super("TestItemComparator", name);
  }

  public void testSort() throws Exception {

    File inpFile = new File(getDataDir(), "snipsnap-org.rss");
    ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpFile);

    // convert from List to Array
    Set<ItemIF> itemsSet = channel.getItems();
    ItemIF[] items = itemsSet.toArray(new ItemIF[itemsSet.size()]);
    // sort news items
    Arrays.sort(items, new ItemComparator(true));

    // compare dates
    Calendar cal = Calendar.getInstance();

    cal.set(2002, Calendar.OCTOBER, 16, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    compareDates(cal, items, 0);

    cal.set(2002, Calendar.OCTOBER, 14, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    compareDates(cal, items, 1);

    cal.set(2002, Calendar.OCTOBER, 10, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    compareDates(cal, items, 2);

    cal.set(2002, Calendar.OCTOBER, 1, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    compareDates(cal, items, 8);

    cal.set(2002, Calendar.SEPTEMBER, 30, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    compareDates(cal, items, 9);

    /*
    for (int i = 0; i < items.length; i++) {
      ItemIF item = (ItemIF) items[i];
      System.out.println("--> title: " + item.getTitle() +
                         ", date: " + item.getDate());
    }
    */
  }

  private void compareDates(Calendar expectedCal, Object[] actualItems,
                            int index) {
    ItemIF item = (ItemIF) actualItems[index];
    // ignore milliseconds
    long milliExp = expectedCal.getTime().getTime();
    long milliAct = item.getDate().getTime();

    assertEquals("Wrong date for item " + (index+1),
                 milliExp / 1000, milliAct / 1000);
  }

}
