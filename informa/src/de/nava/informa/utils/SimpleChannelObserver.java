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


// $Id: SimpleChannelObserver.java 178 2003-04-06 17:30:37Z niko_schmuck $

package de.nava.informa.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ChannelObserverIF;

/**
 * Simple implementation of the ChannelOberserverIF interface for
 * keeping track of the added news item (and also to properly handle
 * it by the logging facility).
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class SimpleChannelObserver implements ChannelObserverIF {

  private static Log logger = LogFactory.getLog(SimpleChannelObserver.class);

  private ItemIF myAddedItem;

  public SimpleChannelObserver() {
    super();
  }
  
  public ItemIF getMyAddedItem() {
    return myAddedItem;
  }

  // ------------------------------------------------------------
  // Implementation of ChannelObserverIF
  // ------------------------------------------------------------
  
  public void itemAdded(ItemIF newItem) {
    myAddedItem = newItem;
    logger.info("A new item was added: " + newItem);
  }

  public void channelRetrieved(ChannelIF channel) {
    logger.info("Channel " + channel + " updated.");
  }
  
}
