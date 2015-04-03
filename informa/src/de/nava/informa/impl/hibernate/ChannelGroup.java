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


// $Id: ChannelGroup.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Hibernate implementation of the ChannelGroupIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelGroup implements ChannelGroupIF {

  private static final long serialVersionUID = -4572648595088013842L;

  private long id = -1;
  private String title;
  private ChannelGroupIF parent;

  private Set<ChannelIF> channels;
  private Collection<ChannelGroupIF> children;

  public ChannelGroup() {
    this("Unnamed channel group");
  }

  public ChannelGroup(String title) {
    this(null, title);
  }

  public ChannelGroup(ChannelGroupIF parent, String title) {
    this.title = title;
    this.channels = Collections.synchronizedSet(new HashSet<ChannelIF>());
    this.parent = parent;
    this.children = new ArrayList<ChannelGroupIF>();
  }

  // --------------------------------------------------------------
  // implementation of ChannelGroupIF interface
  // --------------------------------------------------------------

  /**
   * @return integer representation of identity.
   */
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return title.
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String aTitle) {
    this.title = aTitle;
  }

  /**
   * @return channels.
   */
  public Set<ChannelIF> getChannels() {
    return channels;
  }

  public void setChannels(Set<ChannelIF> aChannels) {
    this.channels = Collections.synchronizedSet(aChannels);
  }

  public void add(ChannelIF channel) {
    channels.add(channel);
  }

  public void remove(ChannelIF channel) {
    channels.remove(channel);
  }

  public Collection<ChannelIF> getAll() {
    return getChannels();
  }

  public ChannelIF getById(long channelId) {
    Iterator it = getChannels().iterator();
    while (it.hasNext()) {
      ChannelIF channel = (ChannelIF) it.next();
      if (channel.getId() == channelId) {
        return channel;
      }
    }
    return null;
  }

  /**
   * @return parent group.
   */
  public ChannelGroupIF getParent() {
    return parent;
  }

  public void setParent(ChannelGroupIF group) {
    this.parent = group;
  }

  /**
   * @return children.
   */
  public Collection<ChannelGroupIF> getChildren() {
    return children;
  }

  public void setChildren(Collection<ChannelGroupIF> aChildren) {
    this.children = aChildren;
  }

  public void addChild(ChannelGroupIF child) {
    getChildren().add(child);
    child.setParent(this);
  }

  public void removeChild(ChannelGroupIF child) {
    getChildren().remove(child);
  }

  // ----------------------------------------------------------------------
  // overwrite default method implementation from Object
  // ----------------------------------------------------------------------

  /**
   * Returns a string representation of the object.
   *
   * @return  a string representation of the object.
   */
  public String toString() {
    return "[Hibernate ChannelGroup \"" + getTitle() + "\"(id=" + id + ")]";
  }
  
}
