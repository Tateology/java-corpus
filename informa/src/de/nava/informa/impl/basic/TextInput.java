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


// $Id: TextInput.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.net.URL;
import de.nava.informa.core.TextInputIF;

/**
 * In-Memory implementation of the TextInputIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class TextInput implements TextInputIF {

	private static final long serialVersionUID = -2953653427729059796L;

	private long id;
  private String title;
  private String description;
  private String name;
  private URL link;

  public TextInput() {
    this("[Unknown TextInput]", null, null, null);
  }

  public TextInput(String title, String description, String name, URL link) {
    this.id = IdGenerator.getInstance().getId();
    this.title = title;
    this.description = description;
    this.name =  name;
    this.link = link;
  }

  // --------------------------------------------------------------
  // implementation of TextInputIF interface
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

  public URL getLink() {
    return link;
  }

  public void setLink(URL link) {
    this.link = link;
  }

}
