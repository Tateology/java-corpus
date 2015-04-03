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

package de.nava.informa.impl.hibernate;

import java.util.ArrayList;
import java.util.Collection;

import de.nava.informa.core.CategoryIF;

/**
 * Hibernate implementation of the CategoryIF interface.
 * 
 * @author Niko Schmuck (niko@nava.de)
 */
public class Category implements CategoryIF {

  private static final long serialVersionUID = -2471218388087500572L;

  private long id = -1;
  private String title;
  private String domain;
  private CategoryIF parent;
  private Collection<CategoryIF> children;

  public Category() {
    this("Unnamed category");
  }

  public Category(String title) {
    setTitle(title);
    this.children = new ArrayList<CategoryIF>();
  }
  
  // --------------------------------------------------------------
  // implementation of CategoryIF interface
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
  
  public void setChildren(Collection<CategoryIF> children) {
    this.children = children;
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

   /**
    * Equal when domain and title are equal.
    */
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CategoryIF)) return false;

      final CategoryIF c = (CategoryIF) o;

      if (domain != null ? !domain.equals(c.getDomain()) : c.getDomain() != null) return false;
      if (title != null ? !title.equals(c.getTitle()) : c.getTitle() != null) return false;

      return true;
   }

   public int hashCode() {
      int result;
      result = (title != null ? title.hashCode() : 0);
      result = 29 * result + (domain != null ? domain.hashCode() : 0);
      return result;
   }

  public String toString() {
    return "[Category (" + id + "): " + title + "]";
  }
  
}
