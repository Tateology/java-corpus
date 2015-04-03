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


// $Id: ItemMetadataIF.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.core;

import java.io.Serializable;

/**
 * Implementing class holds information about the belonging news item.
 * This metadata contains the read status and what the score (priority
 * level for display, may be used to sort items by their relevance) of
 * the item is.
 *
 * @author Niko Schmuck (niko@nava.de) 
 */
public interface ItemMetadataIF extends Serializable {

  public static final int DEFAULT_SCORE = 100;
  
  ItemIF getItem();
  void setItem(ItemIF item);

  boolean isMarkedRead();
  void setMarkedRead(boolean markedRead);

  int getScore();
  void setScore(int score);
  
}
