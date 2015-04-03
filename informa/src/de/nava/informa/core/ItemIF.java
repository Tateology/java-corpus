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


// $Id: ItemIF.java 487 2004-03-25 21:41:25Z pitosalas $

package de.nava.informa.core;

import java.net.URL;
import java.util.Date;

/**
 * This interface is implemented by objects representing items (that are
 * links to articles) in the news channel object model.</p>
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface ItemIF extends WithIdMIF, WithTitleMIF, WithElementsAndAttributesMIF, 
                                WithCreatorMIF, WithCategoriesMIF, WithDescriptionMIF,
                                WithLinkMIF, WithChannelMIF, WithUnreadMIF {

  ItemGuidIF getGuid();
  void setGuid(ItemGuidIF guid);

  URL getComments();
  void setComments(URL comments);

  ItemSourceIF getSource();
  void setSource(ItemSourceIF source);

  ItemEnclosureIF getEnclosure();
  void setEnclosure(ItemEnclosureIF enclosure);

  /* Dublin Core Metadata, like Creator and Subject  */

  String getSubject();
  void setSubject(String subject);

  Date getDate();
  void setDate(Date date);

  Date getFound();
  void setFound(Date found);
}
