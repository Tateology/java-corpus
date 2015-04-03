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


// $Id: Image.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;

import de.nava.informa.core.ImageIF;

/**
 * Hibernate implementation of the ImageIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class Image implements ImageIF {

  private static final long serialVersionUID = 8134982328827904229L;
	
  private long id = -1;
  private String title;
  private String description;
  private URL location;
  private URL link;
  private int width;
  private int height;

  public Image() {
    this("Unnamed image", null, null);
  }

  public Image(String title, URL location, URL link) {
    this.title = title;
    this.location = location;
    this.link = link;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  // --------------------------------------------------------------
  // implementation of ImageIF interface
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

  public String getLocationString() {
    return (location == null) ? null : location.toString();
  }

  public void setLocationString(String loc) {
    if (loc == null || loc.trim().length() == 0) {
      location = null;
      return;
    } else {
      try {
        this.location = new URL(loc);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    this.location = location;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

   /**
    * Images are equal when their locations are equal.
    */
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ImageIF)) return false;

      final ImageIF image = (ImageIF) o;

      if (location != null ? !location.equals(image.getLocation()) : image.getLocation() != null) return false;

      return true;
   }

   public int hashCode() {
      return (location != null ? location.hashCode() : 0);
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
       }
     }
   }
   
  /**
   * @return the link
   */
  public URL getLink() {
    return link;
  }

  /**
   * @param link the link to set
   */
  public void setLink(URL link) {
    this.link = link;
  }

}
