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
// $Id: CacheSettings.java 541 2004-06-22 18:52:30Z jga $

package de.nava.informa.utils;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelFormat;

/**
 * delegation class for the various CacheSettingsIF implementation.
 * 
 * 
 * default behavior : ttl (or update period) in feed is always respected.
 * without such indication, the ttl specified by the informa user is used.
 * @author Jean-Guy Avelin
 */
public class CacheSettings implements CacheSettingsIF {

  static private CacheSettingsIF v_091;
  static private CacheSettingsIF v_100;
  static private CacheSettingsIF v_200;
  static private CacheSettingsIF v_A030;
  /* default settings */
  /* can read properties file to instantiate alternative implementations*/
  /* TODO : property file handling */
  static {
    v_091 = new RSS091Settings();
    v_100 = new RSS100Settings();
    v_200 = new RSS200Settings();
    v_A030 = new Atom030Settings();
  }

  /**
   * determine ttl for the feed, depending on ttl specified by the
   * feed producer and the ttl wanted by the informa user.
   */
  public long getTtl(ChannelIF channel, long ttl) {

    if (channel.getFormat().equals(ChannelFormat.RSS_0_91)
        || channel.getFormat().equals(ChannelFormat.RSS_0_92)
        || channel.getFormat().equals(ChannelFormat.RSS_0_93)
        || channel.getFormat().equals(ChannelFormat.RSS_0_94) ) {
      return v_091.getTtl(channel, ttl);
    } else if (channel.getFormat().equals(ChannelFormat.RSS_1_0)) {
      return v_100.getTtl(channel, ttl);
    } else if (channel.getFormat().equals(ChannelFormat.RSS_2_0)) {
      return v_200.getTtl(channel, ttl);
    }
    else if (channel.getFormat().equals(ChannelFormat.ATOM_0_3)) {
      return v_A030.getTtl(channel, ttl);
    }
    
    return CacheSettingsIF.DEFAULT_TTL;
  }

  /**
   * TODO : remove ?
   */
  public void setDefaultTtl(long defTtl) {
    v_091.setDefaultTtl(defTtl);
    v_100.setDefaultTtl(defTtl);
    v_200.setDefaultTtl(defTtl);
    v_A030.setDefaultTtl(defTtl);
  }

  public void setDefaultTtl(String type, long defTtl) {
    if (ChannelFormat.RSS_0_91.equals(type)
        || ChannelFormat.RSS_0_92.equals(type)
        || ChannelFormat.RSS_0_93.equals(type)
        || ChannelFormat.RSS_0_94.equals(type)) {
      v_091.setDefaultTtl(defTtl);
      return;
    }

    if (ChannelFormat.RSS_1_0.equals(type)) {
      v_100.setDefaultTtl(defTtl);
      return;
    }

    if (ChannelFormat.RSS_2_0.equals(type)) {
      v_200.setDefaultTtl(defTtl);
      return;
    }

    if (ChannelFormat.ATOM_0_3.equals(type)) {
      v_A030.setDefaultTtl(defTtl);
      return;
    }
    
  }
}
