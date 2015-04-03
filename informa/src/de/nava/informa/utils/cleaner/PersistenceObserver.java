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
// $Id: PersistenceObserver.java 697 2004-09-02 09:08:34Z spyromus $
//

package de.nava.informa.utils.cleaner;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.manager.PersistenceManagerIF;
import de.nava.informa.utils.manager.PersistenceManagerException;

/**
 * Watches for events about unwanted items and removes them from channel using given manager.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PersistenceObserver implements CleanerObserverIF {
  private PersistenceManagerIF manager;

  /**
   * Creates observer.
   *
   * @param manager manager to use for persistent changes.
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
   * Invoked when cleanup engine finds unwanted item.
   *
   * @param item    unwanted item.
   * @param channel channel this item resides in.
   */
  public final void unwantedItem(ItemIF item, ChannelIF channel) {
    try {
      manager.deleteItem(item);
    } catch (PersistenceManagerException e) {
      // We can do nothing here.
    }
  }

  /**
   * Invoked by cleanup engine when cleaning of the channel has started.
   *
   * @param channel channel being cleaned.
   */
  public void cleaningStarted(ChannelIF channel) {
  }

  /**
   * Invoked by cleanup engine when cleaning of the channel has finished.
   *
   * @param channel channel being cleaned.
   */
  public void cleaningFinished(ChannelIF channel) {
  }
}
