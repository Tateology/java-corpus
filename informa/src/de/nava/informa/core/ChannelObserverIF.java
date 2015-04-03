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


// $Id: ChannelObserverIF.java 510 2004-05-18 07:15:41Z niko_schmuck $

package de.nava.informa.core;

/**
 * A class implementing this interface most likely wants to react on
 * the observed event.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface ChannelObserverIF {

  /**
   * Called when a new Item is added to this Channel
   * 
   * @param newItem - Item that was added.
   */
  void itemAdded(ItemIF newItem);
  
  /**
   * Called when a new Channel is added
   * 
   * @param channel - Channel that was added
   */
  void channelRetrieved(ChannelIF channel);
  
}
