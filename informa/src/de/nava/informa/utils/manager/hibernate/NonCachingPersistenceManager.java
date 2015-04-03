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
// $Id: NonCachingPersistenceManager.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.manager.hibernate;

import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.hibernate.Channel;
import de.nava.informa.impl.hibernate.ChannelGroup;
import de.nava.informa.impl.hibernate.Item;
import de.nava.informa.utils.InformaUtils;
import de.nava.informa.utils.manager.PersistenceManagerException;
import de.nava.informa.utils.manager.PersistenceManagerIF;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of persistence manager interface, talking with Hibernate. This implementation
 * is not 100% usable becase it isn't confirming to the rule of using the same instances. This
 * means that each time it looks for object (for example, using method <code>getGroups()</code>)
 * it returns new instances of group objects (<code>group1 != group2</code>, but
 * <code>group1.getId() == group2.getId()</code>). Persistence Manager implementation should
 * operate with the same instances all the way and it's carefully checked by acceptance test.
 * <p>
 * There's another implementation wrapping this one -- <code>PersistenceManager</code>. It
 * conforms to the rule.</p>
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 *
 * @see PersistenceManager
 */
class NonCachingPersistenceManager implements PersistenceManagerIF {

  private static final Logger LOG = Logger.getLogger(NonCachingPersistenceManager.class.getName());

  /**
   * Creates new group of channels in persistent storage.
   *
   * @param title title of the group.
   * @return initialized and persisted group object.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public ChannelGroupIF createGroup(String title)
    throws PersistenceManagerException {

    // Create group object
    final ChannelGroupIF group = new ChannelGroup(title);
    HibernateUtil.saveObject(group);

    return group;
  }

  /**
   * Updates data in storage with data from the group object.
   *
   * @param group group object
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void updateGroup(ChannelGroupIF group)
    throws PersistenceManagerException {

    HibernateUtil.updateObject(group);
  }

  /**
   * Deletes group from persistent storage.
   *
   * @param group group to delete.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void deleteGroup(ChannelGroupIF group)
    throws PersistenceManagerException {

    // Remove all associations and delete object
    deleteGroup(group, null);
    group.setId(-1);
  }

  /**
   * Takes channels from the <code>second</code> group and put them all in <code>first</code>
   * group. Then <code>second</code> group is deleted.
   *
   * @param first  first group of channels.
   * @param second second group of channels.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void mergeGroups(ChannelGroupIF first, ChannelGroupIF second)
    throws PersistenceManagerException {

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      HibernateUtil.lock(first, session);
      HibernateUtil.lock(second, session);
      mergeGroups(first, second, session);

      tx.commit();

      second.setId(-1);
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could not merge groups.", e);
      throw new PersistenceManagerException("Could not merge groups.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Returns the list of groups available in database.
   *
   * @return list of groups.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public ChannelGroupIF[] getGroups()
    throws PersistenceManagerException {

    // Read the list of groups from Hibernate session.
    // Note that list of channels in each of groups is lazy and it will not be
    // loaded right away. So, please, don't worry about too much taken memory.
    ChannelGroupIF[] groups = null;
    try {
      final Session session = HibernateUtil.openSession();
      groups = getGroups(session);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Could not read the list of groups.", e);
      throw new PersistenceManagerException("Could not read the list of groups.", e);
    } finally {
      HibernateUtil.closeSession();
    }

    return groups == null ? new ChannelGroupIF[0] : groups;
  }

  /**
   * Creates new channel object and persists it into storage.
   *
   * @param title    title of the channel.
   * @param location location of channel data resource.
   *
   * @return newly created object.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public ChannelIF createChannel(String title, URL location)
    throws PersistenceManagerException {

    // Create channel object and perform some initialization and save
    final ChannelIF channel = new Channel(title);
    channel.setLocation(location);
    HibernateUtil.saveObject(channel);

    return channel;
  }

  /**
   * Updates data in database with data from channel object.
   *
   * @param channel channel object.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void updateChannel(ChannelIF channel)
    throws PersistenceManagerException {

    HibernateUtil.updateObject(channel);
  }

  /**
   * Adds <code>channel</code> to the <code>group</code>.
   *
   * @param channel channel to add.
   * @param group   group to use.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void addChannelToGroup(ChannelIF channel, ChannelGroupIF group)
    throws PersistenceManagerException {

    // Trick to avoid locking of session while hashcode of Channel based on location
    // is calculated. Hashcode is cached and means that will not have problems with locking.
    channel.hashCode();

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      HibernateUtil.lock(channel, session);
      group.add(channel);
      HibernateUtil.updateObject(group, session);

      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could add channel to group.", e);
      throw new PersistenceManagerException("Could add channel to group.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Deletes <code>channel</code> from the <code>group</code>.
   * This method doesn't delete channel from persistent storage. It only
   * breaks the association between channel and group.
   *
   * @param channel channel to delete.
   * @param group   group to use.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void removeChannelFromGroup(ChannelIF channel, ChannelGroupIF group)
    throws PersistenceManagerException {

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      HibernateUtil.lock(channel, session);
      group.remove(channel);
      HibernateUtil.updateObject(group, session);

      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could add channel to group.", e);
      throw new PersistenceManagerException("Could add channel to group.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Deletes channel from persistent storage.
   *
   * @param channel channel to delete.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void deleteChannel(ChannelIF channel)
    throws PersistenceManagerException {

    // This call is taken out of transaction intentionally because
    // we require separate session for that. If we go in the main
    // transaction channels will be loaded in session by this call,
    // the Id's of channel being removed and the other channel in
    // session will match, but they will be different instances and
    // session will throw exception on update.
    final ChannelGroupIF[] groups = getGroups();
    ItemIF[] items = null;

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      HibernateUtil.lock(channel, session);
      items = deleteChannel(channel, groups, session);

      tx.commit();

      // Reset Id's of objects
      channel.setId(-1);
      for (int i = 0; i < items.length; i++) {
        items[i].setId(-1);
      }
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could not delete channel.", e);
      throw new PersistenceManagerException("Could not delete channel.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Creates new item in the channel.
   *
   * @param channel channel to put new item into.
   * @param title   title of new item.
   *
   * @return new item object.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public ItemIF createItem(ChannelIF channel, String title)
    throws PersistenceManagerException {

    final ItemIF item = new Item(channel, title, null, null);

    saveCreatedItem(channel, item);

    return item;
  }

  /**
   * Creates new item using specified object as ethalon.
   * <b>Note that application <i>could</i> already add object to the channel and
   * only persistent modifications required.</b>
   *
   * @param channel channel to put new item into.
   * @param ethalon object to copy properties values from.
   *
   * @return new item object.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public ItemIF createItem(ChannelIF channel, ItemIF ethalon)
    throws PersistenceManagerException {

    // Create item by copying another's properties and save
    final ItemIF item = new Item(channel, null, null, null);
    InformaUtils.copyItemProperties(ethalon, item);

    saveCreatedItem(channel, item);

    return item;
  }

  /**
   * Updates data in database with data from item object.
   *
   * @param item item object.
   *
   * @throws PersistenceManagerException in case of any errors.
   */
  public void updateItem(ItemIF item)
    throws PersistenceManagerException {

    HibernateUtil.updateObject(item);
  }

  /**
   * Deletes the item from the persistent storage.
   *
   * @param item item to delete.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  public void deleteItem(ItemIF item)
    throws PersistenceManagerException {

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      // Create item object and save to database
      deleteItem(item, session);

      tx.commit();
      item.setId(-1);
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could not delete item.", e);
      throw new PersistenceManagerException("Could not delete item.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Atomic operations
  // -----------------------------------------------------------------------------------------------

  /**
   * Merges two groups by moving channels from second to first.
   *
   * @param first   first group.
   * @param second  second group.
   * @param session session to use or NULL.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private static void mergeGroups(ChannelGroupIF first, ChannelGroupIF second,
                                  final Session session)
    throws PersistenceManagerException {

    // Move all channels (without duplicates) from second group to the first
    first.getAll().addAll(second.getAll());
    HibernateUtil.updateObject(first, session);

    // Delete second group
    deleteGroup(second, session);
  }

  /**
   * Removes all associations with channels and deletes group object.
   *
   * @param group   object to delete.
   * @param session session to use or NULL.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private static void deleteGroup(ChannelGroupIF group, Session session)
    throws PersistenceManagerException {

    if (session != null) {
      HibernateUtil.lock(group, session);
    }

    group.getAll().clear();
    HibernateUtil.deleteObject(group, session);
  }

  /**
   * Returns list of groups available in database using given session.
   *
   * @param session session to use.
   *
   * @return list of groups.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private static ChannelGroupIF[] getGroups(final Session session)
    throws PersistenceManagerException {

    ChannelGroupIF[] groups;

    try {
      final List<?> groupsList = session.createQuery("from ChannelGroup").list();
      groups = groupsList.toArray(new ChannelGroupIF[0]);

      // At this step we load all internal collections (channels and items in channels) as
      // we will be detached from the session in the client application.
      for (int i = 0; i < groups.length; i++) {
        ChannelGroupIF group = groups[i];
        initGroupCollections(group);
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Could not read the list of groups.", e);
      throw new PersistenceManagerException("Could not read the list of groups.", e);
    }

    return groups;
  }

  /**
   * Lads collections of group.
   *
   * @param group group collections.
   */
  private static void initGroupCollections(ChannelGroupIF group) {

    // Init children collection
    group.getChildren().size();

    // Load the lists of channels and items in all channels
    ChannelIF[] channels = (ChannelIF[]) group.getAll().toArray(new ChannelIF[0]);
    for (int i = 0; i < channels.length; i++) {
      ChannelIF channel = channels[i];
      channel.getCategories().size();
      ((Channel)channel).getGroups().size();
      for (Iterator it = channel.getItems().iterator(); it.hasNext();) {
        ((ItemIF)it.next()).getCategories().size();
      }
    }
  }

  /**
   * Deletes channel and all its items. Also removes associations with groups.
   *
   * @param channel channel to delete.
   * @param groups  list of all present group. We can't get this list here because we require
   *                to get it from separate session.
   * @param session session to use or NULL.
   *
   * @return list of deleted items.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private ItemIF[] deleteChannel(ChannelIF channel, ChannelGroupIF[] groups, final Session session)
    throws PersistenceManagerException {

    // Remove channel from all groups
    for (int i = 0; i < groups.length; i++) {
      ChannelGroupIF group = groups[i];
      if (group.getAll().contains(channel)) {
        group.remove(channel);
        HibernateUtil.updateObject(group, session);
      }
    }

    // Delete all items
    final ItemIF[] items = (ItemIF[]) channel.getItems().toArray(new ItemIF[0]);
    for (int i = 0; i < items.length; i++) {
      ItemIF item = items[i];
      channel.removeItem(item);
      HibernateUtil.deleteObject(item, session);
    }

    // Delete object
    HibernateUtil.deleteObject(channel, session);

    return items;
  }

  /**
   * Saves created item to storage and associates it with channel using give session.
   *
   * @param item    item to save.
   * @param channel channel to put item in.
   * @param session session to use or NULL.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private static void createItem(final ItemIF item, ChannelIF channel, Session session)
    throws PersistenceManagerException {

    // Put item in the channel
    channel.addItem(item);

    // Saves newly created item
    // Uncomment it if someone decides to remove cascade="all"
//    HibernateUtil.saveObject(item, session);
//    HibernateUtil.updateObject(channel, session);
  }

  /**
   * Deletes item from persistent storage using sinle session object.
   *
   * @param item    item to delete.
   * @param session session to use or NULL.
   *
   * @throws PersistenceManagerException in case of any problems with Hibernate.
   */
  private static void deleteItem(ItemIF item, Session session)
    throws PersistenceManagerException {

    // Find the channel and remove item from it
    final ChannelIF channel = item.getChannel();
    if (channel != null) {
      HibernateUtil.lock(channel, session);
      channel.removeItem(item);
    } else {
      LOG.severe("Item didn't belong to any channel: " + item);
    }

    // Delete item
    HibernateUtil.deleteObject(item, session);
  }

  // -----------------------------------------------------------------------------------------------
  // Hibernate Tools
  // -----------------------------------------------------------------------------------------------

  /**
   * Saves created item.
   *
   * @param channel channel to assign to.
   * @param item    item to save.
   *
   * @throws PersistenceManagerException in case of any problems.
   */
  private void saveCreatedItem(ChannelIF channel, final ItemIF item)
    throws PersistenceManagerException {

    Transaction tx = null;
    try {
      final Session session = HibernateUtil.openSession();
      tx = session.beginTransaction();

      // Save item to database
      HibernateUtil.lock(channel, session);
      createItem(item, channel, session);

      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (HibernateException e1) {
          // We can do nothing here.
        }
      }

      LOG.log(Level.SEVERE, "Could not create item.", e);
      throw new PersistenceManagerException("Could not create item.", e);
    } finally {
      HibernateUtil.closeSession();
    }
  }
}
