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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//


// $Id: TestItem.java 745 2005-01-12 11:05:03Z spyromus $

package de.nava.informa.impl.hibernate;

import de.nava.informa.core.ItemIF;

import java.net.URL;

/**
 * @see Item
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class TestItem extends de.nava.informa.impl.basic.TestItem {

  /**
   * @see de.nava.informa.core.ItemIF#equals
   *
   * @throws Exception in case of any exceptions
   */
  public void testHowDifferentImplementationsMatching() throws Exception {
    ItemIF item1, item2;

    item1 = new Item("a", "b", new URL("file://a"));
    item2 = new SimpleItem("a", "b", new URL("file://a"));
    assertTrue(item1.equals(item2));

    item1 = new Item("a", "b", new URL("file://a"));
    item2 = new SimpleItem("a", "b", new URL("file://b"));
    assertFalse(item1.equals(item2));

    item1 = new Item("a", "b", new URL("file://a"));
    item2 = new SimpleItem("a", "b", new URL("file://A"));
    assertTrue(item1.equals(item2));
  }
}
