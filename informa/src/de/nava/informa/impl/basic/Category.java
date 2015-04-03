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


// $Id: Category.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.basic;

import java.util.ArrayList;
import java.util.Collection;

import de.nava.informa.core.CategoryIF;

/**
 * In-Memory implementation of the CategoryIF interface.
 * 
 * @author Niko Schmuck (niko@nava.de) 
 */
public class Category implements CategoryIF {

  private static final long serialVersionUID = 8319888961720961902L;

  private long id;
  private String title;
  private String domain;
  private CategoryIF parent;
  private Collection<CategoryIF> children;

  public Category() {
    this("[Unnamed Category]");
  }
  
  public Category(String title) {
    this(null, title);
  }

  public Category(CategoryIF parent, String title) {
    this.id = IdGenerator.getInstance().getId();
    this.title = title;
    this.parent = parent;
    this.children = new ArrayList<CategoryIF>();
  }
  
  // --------------------------------------------------------------
  // implementation of CategoryIF interface
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }
  
  public CategoryIF getParent() {
    return parent;
  }

  public void setParent(CategoryIF parent) {
    this.parent = parent;
  }

  public Collection<CategoryIF> getChildren() {
    return children;
  }
  
  public void addChild(CategoryIF child) {
    children.add(child);
    child.setParent(this);
  }
  
  public void removeChild(CategoryIF child) {
    children.remove(child);
  }
  
  // --------------------------------------------------------------
  // overwrite default method implementation from Object 
  // --------------------------------------------------------------

  public boolean equals(Object obj) {
    if (!(obj instanceof CategoryIF)) {
      return false;
    }
    CategoryIF cmp = (CategoryIF) obj;

    return cmp.getTitle().equals(title)
      && (cmp.getId() == id);
  }
  
  public int hashCode() {
    return title.hashCode() + new Long(id).hashCode();
  }
  
  public String toString() {
    return "[Category (" + id + "): " + title + "]";
  }
  
}
