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

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

import java.util.List;
import java.util.Vector;

/**
 * Composite observer delivers all received events to sub observers.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
class CompositeObserver implements PollerObserverIF {
  private List<PollerObserverIF> observers = new Vector<PollerObserverIF>();

  /**
   * Invoked by Poller when new item is approved for addition. Item is transient
   * and should be added to specified channel.
   *
   * @param item    item added.
   * @param channel destination channel.
   */
  public final void itemFound(ItemIF item, ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final PollerObserverIF observer = (PollerObserverIF) observers.get(i);
      try {
        observer.itemFound(item, channel);
      } catch (RuntimeException e) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked by Poller when poller of the channel failed.
   *
   * @param channel channel.
   * @param e       original cause of failure.
   */
  public final void channelErrored(ChannelIF channel, Exception e) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final PollerObserverIF observer = (PollerObserverIF) observers.get(i);
      try {
        observer.channelErrored(channel, e);
      } catch (RuntimeException e1) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked when Poller detected changes in channel information (title and etc).
   *
   * @param channel channel.
   */
  public final void channelChanged(ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final PollerObserverIF observer = (PollerObserverIF) observers.get(i);
      try {
        observer.channelChanged(channel);
      } catch (RuntimeException e1) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked by Poller when checking of the channel started.
   *
   * @param channel channel.
   */
  public final void pollStarted(ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final PollerObserverIF observer = (PollerObserverIF) observers.get(i);
      try {
        observer.pollStarted(channel);
      } catch (RuntimeException e1) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Invoked by Poller when checking of the channel finished.
   *
   * @param channel channel.
   */
  public final void pollFinished(ChannelIF channel) {
    final int size = observers.size();
    for (int i = 0; i < size; i++) {
      final PollerObserverIF observer = (PollerObserverIF) observers.get(i);
      try {
        observer.pollFinished(channel);
      } catch (RuntimeException e1) {
        // Do not care about exceptions from sub-observers.
      }
    }
  }

  /**
   * Adds new observer to the list.
   *
   * @param observer new observer.
   */
  public final void add(PollerObserverIF observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Removes observer from the list.
   *
   * @param observer registered observer.
   */
  public final void remove(PollerObserverIF observer) {
    observers.remove(observer);
  }
}
