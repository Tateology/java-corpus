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


// $Id: ChannelGroup.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.basic;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
  
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * In-Memory implementation of the ChannelGroupIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelGroup implements ChannelGroupIF {

	private static final long serialVersionUID = -4037744833783193972L;

	private long id;
  private String title;
  private Collection<ChannelIF> channels;
  private ChannelGroupIF parent;
  private List<ChannelGroupIF> children;

  public ChannelGroup() {
    this("[Unknown title]");
  }
  
  public ChannelGroup(String title) {
    this(IdGenerator.getInstance().getId(), null, title);
  }
  
  public ChannelGroup(long id, String title) {
    this(id, null, title);
  }
  
  public ChannelGroup(long id, ChannelGroupIF parent, String title) {
    this.id = id;
    this.title = title;
    this.channels = new ArrayList<ChannelIF>();
    this.parent = parent;
    this.children = new ArrayList<ChannelGroupIF>();
  }

  // --------------------------------------------------------------
  // implementation of ChannelGroupIF interface
  // --------------------------------------------------------------
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void add(ChannelIF channel) {
    channels.add(channel);
  }

  public void remove(ChannelIF channel) {
    channels.remove(channel);
  }

  public Collection<ChannelIF> getAll() {
    return channels;
  }

  public ChannelIF getById(long id) {
    Iterator it = channels.iterator();
    while (it.hasNext()) {
      ChannelIF channel = (ChannelIF) it.next();
      if (channel.getId() == id) {
        return channel;
      }
    }
    return null;
  }

  public ChannelGroupIF getParent() {
    return parent;
  }

  public void setParent(ChannelGroupIF parent) {
    this.parent = parent;
  }

  public Collection getChildren() {
    return children;
  }

  public void addChild(ChannelGroupIF child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(ChannelGroupIF child) {
    children.remove(child);
  }
  
  // ----------------------------------------------------------------------
  // overwrite default method implementation from Object 
  // ----------------------------------------------------------------------

  public String toString() {
    return "[ChannelGroup (" + id + ")]";
  }

}
