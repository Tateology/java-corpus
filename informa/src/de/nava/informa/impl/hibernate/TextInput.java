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


// $Id: TextInput.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import de.nava.informa.core.TextInputIF;

/**
 * Hibernate implementation of the TextInputIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class TextInput implements TextInputIF {

  private static final long serialVersionUID = -6371730164672647148L;
	
  private long id = -1;
  private String title;
  private String description;
  private String name;
  private URL link;

  public TextInput() {
    this("[Unknown TextInput]", null, null, null);
  }

  public TextInput(String title, String description, String name, URL link) {
    this.title = title;
    this.description = description;
    this.name =  name;
    this.link = link;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  // --------------------------------------------------------------
  // implementation of TextInputIF interface
  // --------------------------------------------------------------

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLinkString() {
    return (link == null) ? null : link.toString();
  }

  public void setLinkString(String loc) {
    if (loc == null || loc.trim().length() == 0) {
      link = null;
      return;
    } else {
      try {
        this.link = new URL(loc);
      } catch (MalformedURLException e) {
        e.printStackTrace();
        this.link = null;
      }
    }
  }

  public URL getLink() {
    return link;
  }

  public void setLink(URL link) {
    this.link = link;
  }

}
