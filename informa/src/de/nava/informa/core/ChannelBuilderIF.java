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


// $Id: ChannelBuilderIF.java 788 2006-01-03 00:30:39Z niko_schmuck $

package de.nava.informa.core;

import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.jdom.Element;

/**
 * This interface allows the channel generators (like a parser) to
 * create the channel object model independent from which
 * implementation is used (in-memory or persistent store).
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface ChannelBuilderIF {

  /**
   * Sets the runtime properties defined for this channel builder.
   * This method will be invoked by the ChannelBuilderFactory when the
   * ChannelBuilder is first created.
   *
   * @param props The parsed set of properties which may be applied to
   *              this object.
   * @throws ChannelBuilderException If the initialisation fails for
   *              some reason.
   */
  void init(Properties props) throws ChannelBuilderException;

  ChannelGroupIF createChannelGroup(String title);

  ChannelIF createChannel(String title);
  ChannelIF createChannel(String title, String location);
  ChannelIF createChannel(Element channelElement, String title);
  ChannelIF createChannel(Element channelElement, String title, String location);


  /**
   * Creates a news item and assign it to the given channel.
   */
  ItemIF createItem(ChannelIF channel, String title, String description, URL link);
  ItemIF createItem(Element itemElement, ChannelIF channel, String title, String description, URL link);
  ItemIF createItem(ChannelIF channel, ItemIF item);

  ImageIF createImage(String title, URL location, URL link);

  TextInputIF createTextInput(String title, String description, String name, URL link);

  ItemSourceIF createItemSource(ItemIF item, String name, String location, Date timestamp);
  ItemEnclosureIF createItemEnclosure(ItemIF item, URL location, String type, int length);
  ItemGuidIF createItemGuid(ItemIF item, String location, boolean permaLink);
  CloudIF createCloud(String domain, int port, String path, String registerProcedure, String protocol);

  CategoryIF createCategory(CategoryIF parent, String title);

  /**
   * Closes the builder. After this call, all references to any channel objects
   * provided by this ChannelBuilder are invalidated.

   * If the ChannelBuilder has a connection to a persistant store such as a
   * database, connections may be closed.
   */
  void close() throws ChannelBuilderException;

  public void beginTransaction() throws ChannelBuilderException;
  public void endTransaction() throws ChannelBuilderException;
  public void update(Object o) throws ChannelBuilderException;

}
