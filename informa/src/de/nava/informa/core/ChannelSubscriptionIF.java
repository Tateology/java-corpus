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


// $Id: ChannelSubscriptionIF.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.core;

import java.io.Serializable;

/**
 * Implementing classes hold information about wether a channel should
 * be updated or not, when the channel was last time updated
 * (retrieved) and in which interval subsequent updated should be
 * executed.
 *
 * @author Niko Schmuck (niko@nava.de) 
 */
public interface ChannelSubscriptionIF extends Serializable, WithChannelMIF {

  boolean isActive();
  
  /**
   * Activate/Deactivate channel update behaviour.
   *
   * @param active - wether this channel should be updated at all or
   * not.
   */
  void setActive(boolean active);

  
  int getUpdateInterval();
  
  /**
   * Sets the interval time between channel updates.
   *
   * @param interval - time in seconds between channel update
   * retrievals.
   */
  void setUpdateInterval(int interval);
  
}
