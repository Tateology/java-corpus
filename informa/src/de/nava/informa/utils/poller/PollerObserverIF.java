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
// $Id: PollerObserverIF.java 669 2004-08-24 17:49:14Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

/**
 * Observer of events outgoing from Poller.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public interface PollerObserverIF {
  /**
   * Invoked by Poller when new item is approved for addition. Item is transient
   * and should be added to specified channel.
   *
   * @param item    item added.
   * @param channel destination channel.
   */
  void itemFound(ItemIF item, ChannelIF channel);

  /**
   * Invoked by Poller when poller of the channel failed.
   *
   * @param channel channel.
   * @param e       original cause of failure.
   */
  void channelErrored(ChannelIF channel, Exception e);

  /**
   * Invoked when Poller detected changes in channel information (title and etc).
   *
   * @param channel channel.
   */
  void channelChanged(ChannelIF channel);

  /**
   * Invoked by Poller when checking of the channel started.
   *
   * @param channel channel.
   */
  void pollStarted(ChannelIF channel);

  /**
   * Invoked by Poller when checking of the channel finished successfully.
   *
   * @param channel channel.
   */
  void pollFinished(ChannelIF channel);
}
