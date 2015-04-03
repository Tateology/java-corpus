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
// $Id: CleanerObserverIF.java 673 2004-08-24 18:07:54Z spyromus $
//

package de.nava.informa.utils.cleaner;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ChannelIF;

/**
 * Observers receive notification events from the cleaning engine when engine
 * finds items matching all of the Matchers' rules.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public interface CleanerObserverIF {

  /**
   * Invoked when cleanup engine finds unwanted item.
   *
   * @param item    unwanted item.
   * @param channel channel this item resides in.
   */
  void unwantedItem(ItemIF item, ChannelIF channel);

  /**
   * Invoked by cleanup engine when cleaning of the channel has started.
   *
   * @param channel channel being cleaned.
   */
  void cleaningStarted(ChannelIF channel);

  /**
   * Invoked by cleanup engine when cleaning of the channel has finished.
   * 
   * @param channel channel being cleaned.
   */
  void cleaningFinished(ChannelIF channel);
}
