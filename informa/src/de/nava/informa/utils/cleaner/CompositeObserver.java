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
// $Id: CompositeObserver.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.cleaner;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

import java.util.List;
import java.util.Vector;

/**
 * Composite observer follows Composite pattern to combine several observers.
 * When it receives event it delivers this event to all of it's sub-observers.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
class CompositeObserver implements CleanerObserverIF {
  private List<CleanerObserverIF> observers = new Vector<CleanerObserverIF>();

  /**
   * Invoked when cleanup engine finds unwanted item.
   *
   * @param item    unwanted item.
   * @param channel channel this item resides in.
   */
  public void unwantedItem(ItemIF item, ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final CleanerObserverIF observer = (CleanerObserverIF) observers.get(i);
      try {
        observer.unwantedItem(item, channel);
      } catch (Exception e) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked by cleanup engine when cleaning of the channel has started.
   *
   * @param channel channel being cleaned.
   */
  public void cleaningStarted(ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final CleanerObserverIF observer = (CleanerObserverIF) observers.get(i);
      try {
        observer.cleaningStarted(channel);
      } catch (Exception e) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked by cleanup engine when cleaning of the channel has finished.
   *
   * @param channel channel being cleaned.
   */
  public void cleaningFinished(ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final CleanerObserverIF observer = (CleanerObserverIF) observers.get(i);
      try {
        observer.cleaningFinished(channel);
      } catch (Exception e) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Adds new observer to the list.
   *
   * @param observer new observer.
   */
  public final void add(CleanerObserverIF observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Removes observer from the list.
   *
   * @param observer registered observer.
   */
  public final void remove(CleanerObserverIF observer) {
    observers.remove(observer);
  }
}
