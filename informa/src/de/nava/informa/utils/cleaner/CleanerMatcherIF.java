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
// $Id: CleanerMatcherIF.java 673 2004-08-24 18:07:54Z spyromus $
//

package de.nava.informa.utils.cleaner;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

/**
 * Matchers answer if the article in the given channel is unwanted.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public interface CleanerMatcherIF {

  /**
   * Invoked by cleaning engine to check given item in given channel for matching some rule.
   *
   * @param item    item to check.
   * @param channel channel where the item is.
   *
   * @return TRUE if item matches the rule.
   */
  boolean isMatching(ItemIF item, ChannelIF channel);
}
