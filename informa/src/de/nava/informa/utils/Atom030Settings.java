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
//$Id: Atom030Settings.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.utils;

import de.nava.informa.core.ChannelIF;

/**
 * @author Jean-Guy Avelin
 */
public class Atom030Settings implements CacheSettingsIF {

  //private static Log logger = LogFactory.getLog(Atom030Settings.class);

  private long defaultTtl = DEFAULT_TTL;

  /**
   *  
   */
  public void setDefaultTtl(long defaultTtl) {
    this.defaultTtl = defaultTtl;
  }

  /**
   * return in order of preference: feed producer ttl (if exists and < wantedTtl)
   * wantedTtl (if exists) defaultTtl (if exists)
   */
  public long getTtl(ChannelIF channel, long ttlms) {
    //TODO : correct this ... getTtl() when atom parsing complete
    if (channel.getTtl() > 0) {
      long channelTtl  = channel.getTtl() * (60 * 1000); // ttl in feed in minutes
      if (ttlms > channelTtl) {
        return ttlms;
      }
      return channelTtl;   
    }
    
    if (ttlms > MINIMAL_TTL )
      return ttlms;
    
    return this.defaultTtl;

  }
}
