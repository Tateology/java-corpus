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
// $Id: PersistenceObserver.java 700 2004-09-02 09:13:58Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.manager.PersistenceManagerIF;
import de.nava.informa.utils.manager.PersistenceManagerException;

/**
 * Watches for event about new items and calls given manager to create items in channels.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PersistenceObserver implements PollerObserverIF {
  private PersistenceManagerIF manager;

  /**
   * Creates observer.
   *
   * @param manager persistence manager to use.
   *
   * @throws IllegalArgumentException if manager isn't specified.
   */
  public PersistenceObserver(PersistenceManagerIF manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Manager should be specified.");
    }

    this.manager = manager;
  }

  /**
   * Invoked by Poller when new item is approved for addition. Item is transient
   * and should be added to specified channel.
   *
   * @param item    item added.
   * @param channel destination channel.
   */
  public final void itemFound(ItemIF item, ChannelIF channel) {
    try {
      manager.createItem(channel, item);
    } catch (PersistenceManagerException e) {
      // We can do nothing here.
    }
  }

  /**
   * Invoked by Poller when poller of the channel failed.
   *
   * @param channel channel.
   * @param e       original cause of failure.
   */
  public void channelErrored(ChannelIF channel, Exception e) {
  }

  /**
   * Invoked when Poller detected changes in channel information (title and etc).
   *
   * @param channel channel.
   */
  public void channelChanged(ChannelIF channel) {
    try {
      manager.updateChannel(channel);
    } catch (PersistenceManagerException e) {
      // We can do nothing here.
    }
  }

  /**
   * Invoked by Poller when checking of the channel started.
   *
   * @param channel channel.
   */
  public void pollStarted(ChannelIF channel) {
  }

  /**
   * Invoked by Poller when checking of the channel finished.
   *
   * @param channel channel.
   */
  public void pollFinished(ChannelIF channel) {
  }
}
