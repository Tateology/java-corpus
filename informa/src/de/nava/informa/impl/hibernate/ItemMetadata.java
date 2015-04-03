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


// $Id: ItemMetadata.java 788 2006-01-03 00:30:39Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemMetadataIF;

/**
 * Hibernate implementation of the ItemMetadataIF interface.
 * 
 * @author Niko Schmuck (niko@nava.de) 
 */
public class ItemMetadata implements ItemMetadataIF {

  private static final long serialVersionUID = 3508275725980659697L;

  private long id = -1;
  private ItemIF item;
  private boolean markedRead;
  private int score;

  public ItemMetadata() {
    this(null);
  }

  /**
   * Default constructor which sets this metadata to unread and to
   * the default score (see
   * {@link de.nava.informa.core.ItemMetadataIF#DEFAULT_SCORE}).
   */
  public ItemMetadata(ItemIF item) {
    this.item = item;
    this.markedRead = false;
    this.score = DEFAULT_SCORE;
  }
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  // --------------------------------------------------------------
  // implementation of ItemMetadataIF interface
  // --------------------------------------------------------------

  public ItemIF getItem() {
    return item;
  }

  public void setItem(ItemIF item) {
    this.item = item;
  }

  public boolean isMarkedRead() {
    return markedRead;
  }

  public void setMarkedRead(boolean markedRead) {
    this.markedRead = markedRead;
  }
 
  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }
  
}
