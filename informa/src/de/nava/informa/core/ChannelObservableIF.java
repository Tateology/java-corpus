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


// $Id: ChannelObservableIF.java 461 2004-01-30 20:36:06Z niko_schmuck $

package de.nava.informa.core;

/**
 * Classes implementing this interface may want to inform the
 * subscribed observers that a specific channel event has
 * happend (like for example a new item was found).</p>
 *
 * This interface is usually called Subject in the Observer
 * pattern.</p>
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface ChannelObservableIF {

  /**
   * Adds an observer to the set of observers for this object.
   */
  void addObserver(ChannelObserverIF o);

  /**
   * Removes an observer from the set of observers of this object.
   */
  void removeObserver(ChannelObserverIF o);
  
}
