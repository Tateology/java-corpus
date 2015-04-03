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

// $Id: ItemGuid.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import de.nava.informa.core.ItemGuidIF;
import de.nava.informa.core.ItemIF;

/**
 * In-Memory implementation of the ItemGuidIF interface.
 *
 * @author Michael Harhen
 */
public class ItemGuid implements ItemGuidIF {

	private static final long serialVersionUID = -2439211599593399143L;
	
	private long id;
	private ItemIF item;
  private String location;
  private boolean permaLink;

  /**
   * Default constructor.
   * @param item the item
   */
  public ItemGuid(ItemIF item) {
    this(item, null, true);
  }

  public ItemGuid(ItemIF item, String location, boolean permaLink) {
    this.item = item;
    this.location = location;
    this.permaLink = permaLink;
  }

  // --------------------------------------------------------------
  // implementation of ItemGuidIF interface
  // --------------------------------------------------------------

  public ItemIF getItem() {
    return item;
  }

  public void setItem(ItemIF item) {
    this.item = item;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public boolean isPermaLink() {
    return permaLink;
  }

  public void setPermaLink(boolean permaLink) {
    this.permaLink = permaLink;
  }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
