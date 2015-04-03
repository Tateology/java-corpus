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
// $Id: HttpHeaderUtils.java 547 2004-06-24 14:00:48Z jga $

package de.nava.informa.utils;

import java.net.HttpURLConnection;

/**
 * utilities to deal with http headers
 * @author Jean-Guy Avelin
 */
public class HttpHeaderUtils {

  /**
   * add a if-modified-since property on http header.
   * @param conn
   * @param value
   */
  static public void setIfModifiedSince(HttpURLConnection conn, long value) {
    if ( value > 0 )
      conn.setIfModifiedSince( value );
  }
  
  /**
   * do nothing if etag == null otherwise add a If-None-Match property
   * in http header
   * @param conn
   * @param etag
   */
  static public void setETagValue(HttpURLConnection conn, String etag) {
    if (etag !=null) {
      conn.setRequestProperty("If-None-Match", etag);
    }
  }
  
  static public long getLastModified(HttpURLConnection conn) {
    long result = conn.getHeaderFieldDate("Last-Modified", 0L);
    return result;
  }
  
  static public String getETagValue(HttpURLConnection conn) {
    return conn.getHeaderField("ETag");
  }
  
  static public void setUserAgent(HttpURLConnection conn, String agent) {
    if ( agent == null ) 
      return;
    conn.setRequestProperty("User-Agent",agent); 
  }
}
