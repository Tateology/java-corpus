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

// $Id: ItemEnclosure.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemEnclosureIF;

/**
 * Hibernate implementation of the ItemSourceIF interface.
 * 
 * @author Michael Harhen
 */
public class ItemEnclosure implements ItemEnclosureIF {

  private static final long serialVersionUID = -6885166876258838843L;

  private long id = -1;
  private ItemIF item;
  private URL location;
  private int length;
  private String type;

  public ItemEnclosure() {
    this(null);
  }

  public ItemEnclosure(ItemIF item) {
    this(item, null, null, -1);
  }

  public ItemEnclosure(ItemIF item, URL location, String type, int length) {
    this.item = item;
    this.location = location;
    this.type = type;
    this.length = length;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  // --------------------------------------------------------------
  // implementation of ItemEnclosureIF interface
  // --------------------------------------------------------------

  public ItemIF getItem() {
    return item;
  }

  public void setItem(ItemIF item) {
    this.item = item;
  }

  public String getLocationString() {
    return (location == null) ? null : location.toString();
  }

  public void setLocationString(String loc) {
    if (loc == null || loc.trim().length() == 0) {
      location = null;
      return;
    } else {
      try {
        this.location = new URL(loc);
      } catch (MalformedURLException e) {
        e.printStackTrace();
        this.location = null;
      }
    }
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    this.location = location;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

}
