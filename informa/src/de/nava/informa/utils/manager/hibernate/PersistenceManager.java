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

package de.nava.informa.utils.manager.hibernate;

import de.nava.informa.utils.manager.PersistenceManagerException;
import de.nava.informa.core.ChannelGroupIF;

import java.util.Map;
import java.util.HashMap;

/**
 * Hibernate Persistence Manager. This manager talks to Hibernate to store / restore
 * persistence data. Manager is multi-thread safe. It follows the rule of identities and
 * multiple calls to getting methods (for example, <code>getGroups()</code>) return
 * the same instances of objects. This makes life of developers and client applications
 * easier.
 * <p>
 * It's not enough to directly update the fields of objects in order to have the same
 * fields updated persistently. You should explicitly call <code>updateXXXX()</code>
 * methods to transfer changes to the storage. The decision to make explicit updates
 * based on the fact that automatic flushing changes to database each time the value
 * of some property changes will take too much resources when many properties are
 * updated in a single block of code.</p>
 * <p>
 * <b>Note that the manager itself does no efforts to initialize Hibernate!</b><br>
 * It uses system properties as overrides of normal Hibernate inialization ways, like
 * <code>hibernate.properties</code> and <code>hibernate.cfg.xml</code> files which
 * are loaded when this manager requires Hibernate services for the first time. Please,
 * read Hibernate documentation to learn what files and where should be placed
 * to initialize the product properly.</p>
 * <p>
 * <b>Please also note, that this implementation requires to be the only source of
 * changes to Hibernate data storage (database or something else) to operate normally!</b>
 * Basically it is not a problem at all, but if you find that it's not your case, please
 * let us know.
 * </p>
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PersistenceManager extends NonCachingPersistenceManager {

  private Map<Long, ChannelGroupIF> groups = new HashMap<Long, ChannelGroupIF>();
  private boolean groupsRead = false;

  /**
   * Creates new group of channels in persistent storage.
   *
   * @param title title of the group.
   * @return initialized and persisted group object.
   * @throws PersistenceManagerException in case of any problems.
   */
  public ChannelGroupIF createGroup(String title) throws PersistenceManagerException {

    // Create new group in persistent storage
    ChannelGroupIF group = super.createGroup(title);

    // Save new group in local cache
    groups.put(new Long(group.getId()), group);

    return group;
  }

  /**
   * Deletes group from persistent storage.
   *
   * @param group group to delete.
   * @throws PersistenceManagerException in case of any problems.
   */
  public void deleteGroup(ChannelGroupIF group) throws PersistenceManagerException {

    // Save ID and delete group in persistent storage
    long groupId = group.getId();
    super.deleteGroup(group);

    // If object existed then remove it from cache
    if (groupId > -1) {
      groups.remove(new Long(groupId));
    }
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

    // Save ID and merge groups in persistent storage
    long groupId = second.getId();
    super.mergeGroups(first, second);

    // If object existed then remove it from cache
    if (groupId > -1) groups.remove(new Long(groupId));
  }

  /**
   * Returns the list of groups available in database.
   *
   * @return list of groups.
   * @throws PersistenceManagerException in case of any problems.
   */
  public ChannelGroupIF[] getGroups() throws PersistenceManagerException {

    // Thing is that we should read the list of groups from Hibernate
    // only once per run. It's so because if we are the only source of update
    // to the storage (and it should be so) then we have up-to-date cache of
    // objects at any time due to the fact that we control all additions and
    // removals of groups.
    // So, when lost of groups required and we have not read it from Hibernate yet
    // then we do so and put the groups we don't have yet in our cache in it.
    if (!groupsRead) {
      final ChannelGroupIF[] hibernateGroups = super.getGroups();
      for (int i = 0; i < hibernateGroups.length; i++) {
        final ChannelGroupIF hibernateGroup = hibernateGroups[i];
        final Long groupId = new Long(hibernateGroup.getId());
        if (!groups.containsKey(groupId)) {
          groups.put(groupId, hibernateGroup);
        }
      }

      // Put mark that we already successfully read the list of groups from Hibernate.
      groupsRead = true;
    }

    return groups.values().toArray(new ChannelGroupIF[0]);
  }
}
