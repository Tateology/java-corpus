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


// $Id: ItemFieldConstants.java 74 2002-08-20 22:02:56Z niko_schmuck $

package de.nava.informa.search;

/**
 * A class containing constants for the document fields.
 * @see ItemDocument
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface ItemFieldConstants {

  public static final String ITEM_ID = "item";
  
  public static final String CHANNEL_ID = "channel";
  
  public static final String TITLE = "title"; 
  
  public static final String DESCRIPTION = "description";

  /**
   * Contains TITLE and DESCRIPTION together, this is also the default
   * field where searches are performed with when no explict field is
   * given.
   */
  public static final String TITLE_AND_DESC = "titledesc";
  
  public static final String DATE_FOUND = "found";
  
}
