//
//Informa -- RSS Library for Java
//Copyright (c) 2002 by Niko Schmuck
//
//Niko Schmuck
//http://sourceforge.net/projects/informa
//mailto:niko_schmuck@users.sourceforge.net
//
//This library is free software.
//
//You may redistribute it and/or modify it under the terms of the GNU
//Lesser General Public License as published by the Free Software Foundation.
//
//Version 2.1 of the license should be included with this distribution in
//the file LICENSE. If the license is not included with this distribution,
//you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
//or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
//MA 02139 USA.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied waranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
// $Id: ConditionalGetValues.java 540 2004-06-22 18:51:41Z jga $


package de.nava.informa.utils;

/**
 * contain the values of HTTP headers. These values are necessary
 * to implement "conditionnal get" for the feed.
 * @author Jean-Guy Avelin
 *
 */

public class ConditionalGetValues {
  String ETag = null;
  long ifModifiedSince = 0L;
  
  
  /**
   * @return Returns the eTag.
   */
  public String getETag() {
    return ETag;
  }
  
  
  /**
   * @param tag The eTag to set.
   */
  public void setETag(String tag) {
    ETag = tag;
  }
  
  
  /**
   * @return Returns the ifModifiedSince.
   */
  public long getIfModifiedSince() {
    return ifModifiedSince;
  }
  
  
  /**
   * @param ifModifiedSince The ifModifiedSince to set.
   */
  public void setIfModifiedSince(long ifModifiedSince) {
    this.ifModifiedSince = ifModifiedSince;
  }

}
