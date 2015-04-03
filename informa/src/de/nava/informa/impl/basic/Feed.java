//
// Informa -- RSS Library for Java
// Copyright (c) 2002, 2003 by Niko Schmuck
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


// $Id: Feed.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.net.URL;
import java.util.Date;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.FeedIF;

/**
 * In-Memory implementation of the FeedIF interface.
 *
 * @author Niko Schmuck
 */
public class Feed implements FeedIF {

	private static final long serialVersionUID = 1349458681404088401L;

	private long id;
  private String title;
  private String text;
  private URL location;
  private URL site;
  private String contentType;
  private String copyright;
  private Date dateFound;
  private Date lastUpdated;
  private ChannelIF feed;

  /**
   * Default constructor.
   */
	public Feed() {
    this("No title");
  }

  /**
   * Convinence constrcutor - creates meta data for a preexisting feed.
   * @param channel
   */
  public Feed(ChannelIF channel)
  {
    setChannel(channel);
    setTitle(channel.getTitle());
    setLocation(channel.getLocation());
    setSite(channel.getSite());
    setCopyright(channel.getCopyright());
  }
  
  public Feed(String title) {
    this.title = title;
  }

  public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

  public String getContentType() {
    return contentType;
  }

  public String getCopyright() {
    return copyright;
  }

  public Date getDateFound() {
    return dateFound;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public URL getLocation() {
    return location;
  }

  public URL getSite() {
    return site;
  }

  public String getText() {
    return text;
  }

  public String getTitle() {
    return title;
  }

  public void setContentType(String string) {
    this.contentType = string;
  }

  public void setCopyright(String string) {
    this.copyright = string;
  }

  public void setDateFound(Date date) {
    this.dateFound = date;
  }

  public void setLastUpdated(Date date) {
    this.lastUpdated = date;
  }

  public void setLocation(URL location) {
    this.location = location;
  }

  public void setSite(URL site) {
    this.site = site;
  }

  public void setText(String string) {
    this.text = string;
  }

  public void setTitle(String string) {
    this.title = string;
  }

  public ChannelIF getChannel() {
    return feed;
  }

  public void setChannel(ChannelIF channelIF) {
    feed = channelIF;
  }

}
