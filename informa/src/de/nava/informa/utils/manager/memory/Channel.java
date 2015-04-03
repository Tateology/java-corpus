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
// $Id: Channel.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.manager.memory;

import de.nava.informa.core.ChannelGroupIF;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Local implementation of <code>ChannelIF</code>.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class Channel extends de.nava.informa.impl.basic.Channel {

	private static final long serialVersionUID = -2479661776931822761L;
	private List<ChannelGroupIF> groups = new ArrayList<ChannelGroupIF>();

  /**
   * Creates channel object.
   *
   * @param id       ID of the channel.
   * @param title    title of the channel.
   * @param location URL of resource location.
   */
  public Channel(long id, String title, URL location) {
    super(title);
    setId(id);
    setLocation(location);
  }

  /**
   * Adds parent group to the list when channel is assigned to the new group.
   *
   * @param group group to add.
   */
  public final void addParentGroup(ChannelGroupIF group) {
    synchronized (group) {
      if (!groups.contains(group)) {
        groups.add(group);
      }
    }
  }

  /**
   * Removes parent group from the list when channel is unassigned from it.
   *
   * @param group group to remove.
   */
  public final void removeParentGroup(ChannelGroupIF group) {
    synchronized (group) {
      groups.remove(group);
    }
  }

  /**
   * Returns the list of parent groups.
   *
   * @return list of groups.
   */
  public final ChannelGroupIF[] getParentGroups() {
    return (ChannelGroupIF[]) groups.toArray(new ChannelGroupIF[0]);
  }
}
