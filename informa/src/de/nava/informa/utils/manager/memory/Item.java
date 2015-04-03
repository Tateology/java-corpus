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
// $Id: Item.java 779 2005-09-27 22:17:06Z niko_schmuck $
//

package de.nava.informa.utils.manager.memory;

import de.nava.informa.core.ChannelIF;

/**
 * Local implementation of <code>ItemIF</code>.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class Item extends de.nava.informa.impl.basic.Item {
	
	private static final long serialVersionUID = 8291899713409593409L;
	private ChannelIF parent;

  /**
   * Creates item object.
   *
   * @param id     ID of item.
   * @param title  title of item.
   * @param parent parent channel.
   */
  public Item(long id, String title, ChannelIF parent) {
    setId(id);
    setTitle(title);
    setParent(parent);
  }

  /**
   * Returns parent channel of this item.
   *
   * @return parent channel.
   */
  public final ChannelIF getParent() {
    return parent;
  }

  /**
   * Sets parent channel of this item.
   *
   * @param parent parent channel.
   */
  public final void setParent(ChannelIF parent) {
    this.parent = parent;
  }
}
