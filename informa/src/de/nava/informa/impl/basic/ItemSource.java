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


// $Id: ItemSource.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.util.Date;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemSourceIF;

/**
 * In-Memory implementation of the ItemSourceIF interface.
 *
 * @author Michael Harhen
 */
public class ItemSource implements ItemSourceIF {

	private static final long serialVersionUID = -7976590108892553322L;

	private ItemIF item;
  private String name;
  private String location;
  private Date timestamp;

  /**
   * Default constructor.
   * @param item the item
   */
  public ItemSource(ItemIF item) {
    this(item, null, null, null);
  }

  public ItemSource(ItemIF item, String name, String location, Date timestamp) {
    this.item = item;
    this.name = name;
    this.location = location;
    this.timestamp = timestamp;
  }
  // --------------------------------------------------------------
  // implementation of ItemSourceIF interface
  // --------------------------------------------------------------

  public ItemIF getItem() {
    return item;
  }

  public void setItem(ItemIF item) {
    this.item = item;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

}
