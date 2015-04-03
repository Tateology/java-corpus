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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// $Id: PersistenceManager.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.manager.memory;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemGuidIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ItemGuid;
import de.nava.informa.utils.InformaUtils;
import de.nava.informa.utils.manager.PersistenceManagerIF;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of persistence manager. Uses local memory to store data.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PersistenceManager implements PersistenceManagerIF {

  private Map<Long, ChannelGroup> groups;
  private Map<Long, Channel> channels;
  private Map<Long, Item> items;

  /**
   * Creates persistence manager.
   */
  public PersistenceManager() {
    groups = new HashMap<Long, ChannelGroup>();
    channels = new HashMap<Long, Channel>();
    items = new HashMap<Long, Item>();
  }

  /**
   * Creates new group of channels in persistent storage.
   *
   * @param title title of the group.
   * @return initialized and persisted group object.
   */
  public final ChannelGroupIF createGroup(String title) {
    final long id = IdGenerator.getNextId();
    final ChannelGroup group = new ChannelGroup(id, title);

    // put group in map
    groups.put(new Long(id), group);

    return group;
  }

  /**
   * Updates data in storage with data from the group object.
   *
   * @param group group object
   */
  public void updateGroup(ChannelGroupIF group) {
    // Explicit update isn't necessary as we have only memory objects
    // which are updated when we change properties values.
  }

  /**
   * Deletes group from persistent storage.
   *
   * @param group group to delete.
   */
  public final void deleteGroup(ChannelGroupIF group) {
    group.getAll().clear();
    groups.remove(new Long(group.getId()));
    group.setId(-1);
  }

  /**
   * Takes channels from the <code>second</code> group and put them all in <code>first</code>
   * group. Then <code>second</code> group is deleted.
   *
   * @param first  first group of channels.
   * @param second second group of channels.
   */
  public final void mergeGroups(ChannelGroupIF first, ChannelGroupIF second) {
    moveChannels(second, first);
    deleteGroup(second);
  }

  /**
   * Returns the list of groups available in database.
   *
   * @return list of groups.
   */
  public final ChannelGroupIF[] getGroups() {
    return groups.values().toArray(new ChannelGroupIF[0]);
  }

  /**
   * Creates new channel object and persists it into storage.
   *
   * @param title    title of the channel.
   * @param location location of channel data resource.
   * @return newly created object.
   */
  public final ChannelIF createChannel(String title, URL location) {
    final long id = IdGenerator.getNextId();
    final Channel channel = new Channel(id, title, location);

    // put channel in map
    channels.put(new Long(id), channel);

    return channel;
  }

  /**
   * Updates data in database with data from channel object.
   *
   * @param channel channel object.
   */
  public void updateChannel(ChannelIF channel) {
    // Explicit update isn't necessary as we have only memory objects
    // which are updated when we change properties values.
  }

  /**
   * Adds <code>channel</code> to the <code>group</code>.
   *
   * @param channel channel to add.
   * @param group   group to use.
   */
  public final void addChannelToGroup(ChannelIF channel, ChannelGroupIF group) {
    if (!group.getAll().contains(channel)) {
      group.add(channel);
    }

    if (channel instanceof Channel) {
      ((Channel) channel).addParentGroup(group);
    }
  }

  /**
   * Deletes <code>channel</code> from the <code>group</code>.
   * This method doesn't delete channel from persistent storage. It only
   * breaks the association between channel and group.
   *
   * @param channel channel to delete.
   * @param group   group to use.
   */
  public final void removeChannelFromGroup(ChannelIF channel, ChannelGroupIF group) {
    group.remove(channel);
    if (channel instanceof Channel) {
      ((Channel) channel).removeParentGroup(group);
    }
  }

  /**
   * Deletes channel from persistent storage.
   *
   * @param channel channel to delete.
   */
  public final void deleteChannel(ChannelIF channel) {
    // remove all associations with parent groups
    if (channel instanceof Channel) {
      final ChannelGroupIF[] groupsList = ((Channel) channel).getParentGroups();
      for (int i = 0; i < groupsList.length; i++) {
        ChannelGroupIF group = groupsList[i];

        removeChannelFromGroup(channel, group);
      }
    }

    // remove all items
    final ItemIF[] itemsList = (ItemIF[]) channel.getItems().toArray(new ItemIF[0]);
    for (int i = 0; i < itemsList.length; i++) {
      deleteItem(itemsList[i]);
    }

    // remove channel from map
    channels.remove(new Long(channel.getId()));
    channel.setId(-1);
  }

  /**
   * Creates new item in the channel.
   *
   * @param channel channel to put new item into.
   * @param title   title of new item.
   * @return new item object.
   */
  public final ItemIF createItem(ChannelIF channel, String title) {
    final long id = IdGenerator.getNextId();
    final Item item = new Item(id, title, channel);

    // put item in map
    items.put(new Long(item.getId()), item);

    // replace item in channel if it's already there
    if (channel.getItems().contains(item)) {
      channel.removeItem(item);
    }

    channel.addItem(item);

    return item;
  }

  /**
   * Creates new item using specified object as ethalon.
   * <b>Note that application <i>could</i> already add object to the channel and
   * only persistent modifications required.</b>
   *
   * @param channel channel to put new item into.
   * @param ethalon object to copy properties values from.
   * @return new item object.
   */
  public final ItemIF createItem(ChannelIF channel, ItemIF ethalon) {
    final ItemIF item = createItem(channel, ethalon.getTitle());

    // Copy values
    InformaUtils.copyItemProperties(ethalon, item);

    // Copy guid if present
    final ItemGuidIF ethalonGuid = ethalon.getGuid();
    if (ethalonGuid != null) {
      item.setGuid(new ItemGuid(item, ethalonGuid.getLocation(), ethalonGuid.isPermaLink()));
    }

    return item;
  }

  /**
   * Updates data in database with data from item object.
   *
   * @param item item object.
   */
  public void updateItem(ItemIF item) {
    // Explicit update isn't necessary as we have only memory objects
    // which are updated when we change properties values.
  }

  /**
   * Deletes the item from the persistent storage.
   *
   * @param item item to delete.
   */
  public final void deleteItem(ItemIF item) {
    if (item instanceof Item) {
      final ChannelIF parent = ((Item) item).getParent();
      parent.removeItem(item);
    }

    items.remove(new Long(item.getId()));
    item.setId(-1);
  }

  /**
   * Moves channels from source to destination group.
   *
   * @param src  source group to take channels from.
   * @param dest destination group to put channel into.
   */
  final void moveChannels(ChannelGroupIF src, ChannelGroupIF dest) {
    final ChannelIF[] secondChannels = (ChannelIF[]) src.getAll().toArray(new ChannelIF[0]);
    for (int i = 0; i < secondChannels.length; i++) {
      ChannelIF channel = secondChannels[i];

      addChannelToGroup(channel, dest);
      removeChannelFromGroup(channel, src);
    }
  }
}
