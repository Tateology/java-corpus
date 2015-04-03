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


// $Id: FeedIF.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.core;

import java.util.Date;

/**
 * <p>This interface is implemented by objects representing feeds
 * (containing metadata about channels, like for example OCS and OPML)
 * in the news channel object model.</p>
 *
 * <p>{@link WithLocationMIF#getLocation()} returns the destination of the feed 
 *      (most likely the XML source). </p>
 *
 * <p>{@link WithChannelMIF#getChannel()} retrieves the channel this feed 
 *      represents.</p>
 * <p>{@link WithChannelMIF#setChannel(ChannelIF)} sets the channel that 
 *      this feed represents</p>
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface FeedIF extends WithIdMIF, WithTitleMIF, WithLocationMIF, WithChannelMIF, WithSiteMIF {
	
  String getText();
  void setText(String text);

  String getContentType();
  void setContentType(String contentType);

  Date getDateFound();
  void setDateFound(Date dateFound);
  
  Date getLastUpdated();
  void setLastUpdated(Date lastUpdated);

}
