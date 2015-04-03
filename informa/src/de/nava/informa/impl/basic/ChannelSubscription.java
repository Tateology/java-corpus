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


// $Id: ChannelSubscription.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import de.nava.informa.core.ChannelSubscriptionIF;
import de.nava.informa.core.ChannelIF;

/**
 * In-Memory implementation of the ChannelSubscriptionIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelSubscription implements ChannelSubscriptionIF {

	private static final long serialVersionUID = -5353083835760132316L;

	private ChannelIF channel;
  private boolean active;
  private int updateInterval;

  /**
   * Default constructor sets to an inactive channel (with an update
   * interval of 3 hours, used when activated).
   * @param channel the channel.
   */
  public ChannelSubscription(ChannelIF channel) {
    this(channel, false, 3 * 60 * 60);
  }

  public ChannelSubscription(ChannelIF channel, boolean active, int interval) {
    this.channel = channel;
    this.active = active;
    this.updateInterval = interval;
  }

  // --------------------------------------------------------------
  // implementation of ChannelSubscriptionIF interface
  // --------------------------------------------------------------

  public ChannelIF getChannel() {
    return channel;
  }

  public void setChannel(ChannelIF channel) {
    this.channel = channel;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getUpdateInterval() {
    return updateInterval;
  }

  public void setUpdateInterval(int interval) {
    this.updateInterval = interval;
  }

}
