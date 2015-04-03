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
package de.nava.informa.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelIF;

/**
 * @author Jean-Guy Avelin
 */
public class RSS091Settings implements CacheSettingsIF {

  private static Log logger = LogFactory.getLog(RSS100Settings.class);

  private long defaultTtl = DEFAULT_TTL;

  public void setDefaultTtl(long defaultTtl) {
    this.defaultTtl = defaultTtl;
  }

  /**
   * return the ttl (in order of preference) wantedTtl (if exists) defaultTtl
   */
  public long getTtl(ChannelIF channel, long Ttlms) {
    logger.info("getTtl call RSS091 ask:" + Ttlms + " def:" + defaultTtl
        + " feed :" + channel.toString());
    if (Ttlms > MINIMAL_TTL) {

      return Ttlms;
    } else {
      return defaultTtl;
    }
  }

}