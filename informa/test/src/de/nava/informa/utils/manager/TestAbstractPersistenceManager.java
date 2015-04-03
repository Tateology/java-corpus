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
// $Id: TestAbstractPersistenceManager.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.manager;

import java.net.URL;
import java.util.Collection;

import junit.framework.TestCase;
import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

/**
 * Test on persistence manager compatibility. This test is useful to make sure
 * that your new persistence manager conforms to expectation. Let this test be
 * your manager's first client.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public abstract class TestAbstractPersistenceManager extends TestCase {

  private static final int UID_STEP = 3;

  private PersistenceManagerIF manager;

  private static int testSeqNum = 0;

  /**
   * UID's are useful to get unique test names and different values.
   */
  private int tuid1, tuid2, tuid3;
  private String tuids1, tuids2, tuids3;
  private URL url1, url2, url3;

  protected void setUp() throws Exception {
    manager = getManager();

    // Create UID's for test
    tuid1 = testSeqNum += UID_STEP;
    tuid2 = tuid1 + 1;
    tuid3 = tuid2 + 1;

    // Create test String's
    tuids1 = Integer.toString(tuid1);
    tuids2 = Integer.toString(tuid2);
    tuids3 = Integer.toString(tuid3);

    // Create test URL's
    url1 = new URL("file:///test1");
    url2 = new URL("file:///test2");
    url3 = new URL("file:///test3");
  }

  /**
   * Returns manager to be tested.
   *
   * @return manager to be tested.
   */
  protected abstract PersistenceManagerIF getManager();

  // -----------------------------------------------------------------------------------------------
  // Groups
  // -----------------------------------------------------------------------------------------------

  /**
   * Simple test of group creation. Creates group and checks the filling of data fields.
   *
   * @see PersistenceManagerIF#createGroup
   */
  public void testCreateGroup() {
    ChannelGroupIF group = null;

    try {
      // Create simple group and check how the fields are filled
      group = manager.createGroup(tuids1);
      assertNotNull("Group object should be returned.", group);

      assertTrue("ID should be initialized with some meaningful value.", -1 != group.getId());
      assertEquals("Title should be set.", tuids1, group.getTitle());

      final ChannelGroupIF group2 = findGroupById(group.getId());
      assertTrue("Manager should operate with the same objects.", group == group2);
    } catch (PersistenceManagerException e)
    {
      e.printStackTrace();
      fail();
    } finally {
      // Delete the group
      try {
        if (group != null && group.getId() != -1) manager.deleteGroup(group);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Tests how group is deleted from storage.
   *
   * @see PersistenceManagerIF#deleteGroup
   */
  public void testDeleteGroup() {
    ChannelGroupIF group = null;

    try {
      // Create simple group
      group = manager.createGroup(tuids1);
      final long groupId = group.getId();
      manager.deleteGroup(group);

      assertEquals("Object ID should be turned back to -1.", -1, group.getId());
      final ChannelGroupIF group2 = findGroupById(groupId);
      assertNull("Object should be deleted from storage.", group2);
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Tests how groups with assigned channels are deleted.
   *
   * @see PersistenceManagerIF#deleteGroup
   */
  public void testDeleteGroupCascade() {
    ChannelGroupIF group = null;
    ChannelIF channel = null;

    try {
      // Create simple group with channel
      group = manager.createGroup(tuids1);
      channel = manager.createChannel(tuids1, url1);
      manager.addChannelToGroup(channel, group);

      final long groupId = group.getId();
      manager.deleteGroup(group);

      assertEquals("Object ID should be turned back to -1.", -1, group.getId());
      final ChannelGroupIF group2 = findGroupById(groupId);
      assertNull("Object should be deleted from storage.", group2);
      assertEquals("Group still has channels assigned.", 0, group.getAll().size());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      try {
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
      } catch (PersistenceManagerException e) {
        // We can do nothing here
      }
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Channels
  // -----------------------------------------------------------------------------------------------

  /**
   * Tests simple channel creation.
   *
   * @see PersistenceManagerIF#createChannel
   */
  public void testCreateChannel() {
    // Create channel and check
    ChannelIF channel = null;

    try {
      channel = manager.createChannel(tuids1, url1);
      assertNotNull("Channel object should be returned.", channel);
      assertTrue("ID should be initialized.", -1 != channel.getId());
      assertEquals("Title should be set.", tuids1, channel.getTitle());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete channel
      try {
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
      } catch (PersistenceManagerException e) {
        // We can do nothing here
      }
    }
  }

  /**
   * Checks simple channel deletion.
   *
   * @see PersistenceManagerIF#deleteChannel
   */
  public void testDeleteChannelSimple() {
    try {
      // Create channel
      final ChannelIF channel = manager.createChannel(tuids1, url1);

      // Delete channel
      manager.deleteChannel(channel);
      assertTrue("ID should be reset to uninitialized state (-1).", -1 == channel.getId());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Tests how channel is removed from group when it's deleted and how its items
   * are deleted as well.
   *
   * @see PersistenceManagerIF#deleteChannel
   */
  public void testDeleteChannelCascade() {
    ChannelGroupIF group = null;
    ChannelIF channel = null;
    ItemIF item1 = null;
    ItemIF item2 = null;

    try {
      // Create group and channel with two items
      group = manager.createGroup(tuids1);
      channel = manager.createChannel(tuids1, url1);
      item1 = manager.createItem(channel, tuids1);
      item2 = manager.createItem(channel, tuids2);

      // Put channel in group
      manager.addChannelToGroup(channel, group);

      // Delete channel
      manager.deleteChannel(channel);

      // Check that it was removed from parent group and all of its items gone too
      assertEquals("Group still has channels.", 0, group.getAll().size());
      assertEquals("Channel still has items.", 0, channel.getItems().size());
      assertEquals("Item still has initialized ID.", -1, item1.getId());
      assertEquals("Item still has initialized ID.", -1, item2.getId());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete channel, group and items
      try {
        if (item1 != null && item1.getId() != -1) manager.deleteItem(item1);
        if (item2 != null && item2.getId() != -1) manager.deleteItem(item2);
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
        if (group != null && group.getId() != -1) manager.deleteGroup(group);
      } catch (PersistenceManagerException e) {
        // We can do nothing here
      }
    }
  }

  /**
   * Tests how channels are added to groups.
   *
   * @see PersistenceManagerIF#addChannelToGroup
   */
  public void testAddChannelToGroup() {
    ChannelIF channel = null;
    ChannelGroupIF group = null;

    try {
      // Create channel & group
      channel = manager.createChannel(tuids1, url1);
      group = manager.createGroup(tuids2);

      // Add channel to group
      manager.addChannelToGroup(channel, group);
      final Collection channels = group.getAll();
      assertEquals("Channel isn't added to group.", 1, channels.size());
      assertEquals("Wrong channel was added to group.", channel, channels.iterator().next());

      // Add duplicate channel to group
      manager.addChannelToGroup(channel, group);
      final Collection channels2 = group.getAll();
      assertEquals("Duplicate channel shouldn't be added to group.", 1, channels2.size());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete channel and group
      try {
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
        if (group != null && group.getId() != -1) manager.deleteGroup(group);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Tests how channels are removed from group. Performs non-existing channel removing check.
   *
   * @see PersistenceManagerIF#removeChannelFromGroup
   */
  public void testRemoveChannelFromGroup() {
    ChannelIF channel = null;
    ChannelGroupIF group = null;

    try {
      // Create channel & group
      channel = manager.createChannel(tuids1, url1);
      group = manager.createGroup(tuids2);

      // Add channel to group
      manager.addChannelToGroup(channel, group);

      // Remove channel from group
      manager.removeChannelFromGroup(channel, group);
      final Collection channels = group.getAll();
      assertEquals("Channel wasn't removed from group.", 0, channels.size());

      // Remove not-existing channel from group
      manager.removeChannelFromGroup(channel, group);
      final Collection channels2 = group.getAll();
      assertEquals("Channel wasn't removed from group.", 0, channels2.size());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete channel and group
      try {
        if (channel != null) manager.deleteChannel(channel);
        if (group != null) manager.deleteGroup(group);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Tests how the groups are merged.
   *
   * @see PersistenceManagerIF#mergeGroups
   */
  public void testMergeChannels() {
    ChannelGroupIF group1 = null;
    ChannelGroupIF group2 = null;
    ChannelIF channel1 = null;
    ChannelIF channel2 = null;
    ChannelIF channel3 = null;

    try {
      // Create 2 grops and 3 channels: 1 in first group and 2 in the second
      group1 = manager.createGroup(tuids1);
      group2 = manager.createGroup(tuids2);
      channel1 = manager.createChannel(tuids1, url1);
      channel2 = manager.createChannel(tuids2, url2);
      channel3 = manager.createChannel(tuids3, url3);

      final long group2Id = group2.getId();

      // Add channels to groups
      manager.addChannelToGroup(channel1, group1);
      manager.addChannelToGroup(channel2, group2);
      manager.addChannelToGroup(channel3, group2);

      // Move channels from second to first
      manager.mergeGroups(group1, group2);

      // Check the lists of channels in groups
      final Collection channels1 = group1.getAll();
      assertNotNull(channels1);
      assertEquals("Channels are incorrectly moved to first group.", 3, channels1.size());
      assertTrue("Order of channels is incorrect.", channels1.contains(channel1));
      assertTrue("Order of channels is incorrect.", channels1.contains(channel2));
      assertTrue("Order of channels is incorrect.", channels1.contains(channel3));
      assertEquals("ID of removed group is still in intialized state.", -1, group2.getId());
      assertNull("Second group still can be found in storage.", findGroupById(group2Id));
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete groups and channels
      try {
        if (group1 != null) manager.deleteGroup(group1);
        if (group2 != null && group2.getId() != -1) manager.deleteGroup(group2);
        if (channel1 != null) manager.deleteChannel(channel1);
        if (channel2 != null) manager.deleteChannel(channel2);
        if (channel3 != null) manager.deleteChannel(channel3);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Items
  // -----------------------------------------------------------------------------------------------

  /**
   * Checks simple item creation.
   *
   * @see PersistenceManagerIF#createItem
   */
  public void testCreateItem() {
    ChannelIF channel = null;
    ItemIF item = null;

    try {
      // Create channel and item
      channel = manager.createChannel(tuids1, url1);
      item = manager.createItem(channel, tuids2);

      // Check item
      assertNotNull(item);
      assertEquals("Title isn't properly initialized.", tuids2, item.getTitle());

      // Check channel
      final Collection items = channel.getItems();
      assertNotNull(items);
      assertEquals("Item was not correclty added to the channel.", 1, items.size());
      assertEquals("Incorrect item was added to the channel.", item, items.iterator().next());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete channel and item
      try {
        if (item != null) manager.deleteItem(item);
        if (channel != null) manager.deleteChannel(channel);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Checks creation of item using information from another item.
   *
   * @see PersistenceManagerIF#createItem
   */
  public void testCreateItemFromItem() {
    ChannelIF channel1 = null;
    ChannelIF channel2 = null;
    ItemIF item1 = null;
    ItemIF item2 = null;

    try {
      // Create two channels and item
      channel1 = manager.createChannel(tuids1, url1);
      channel2 = manager.createChannel(tuids2, url1);
      item1 = manager.createItem(channel1, tuids1);
      item2 = manager.createItem(channel2, item1);

      assertEquals("New item isn't matching its parent.", item1, item2);
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete items and channels
      try {
        if (item1 != null) manager.deleteItem(item1);
        if (item2 != null) manager.deleteItem(item2);
        if (channel1 != null) manager.deleteChannel(channel1);
        if (channel2 != null) manager.deleteChannel(channel2);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Tests deletion of items.
   *
   * @see PersistenceManagerIF#deleteItem
   */
  public void testDeleteItem() {
    ChannelIF channel = null;
    ItemIF item1 = null;
    ItemIF item2 = null;

    try {
      // Create channel with two items
      channel = manager.createChannel(tuids1, url1);
      item1 = manager.createItem(channel, tuids1);
      item2 = manager.createItem(channel, tuids2);

      // Delete first item
      manager.deleteItem(item1);

      // Check item and channel
      assertEquals("Item ID should be in uninitialized state (-1).", -1, item1.getId());

      final Collection items = channel.getItems();
      assertNotNull(items);
      assertEquals("Number of items in list is incorrect.", 1, items.size());
      assertTrue("Wrong item was removed.", item2 == items.iterator().next());
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    } finally {
      // Delete items and channel
      try {
        if (item1 != null && item1.getId() != -1) manager.deleteItem(item1);
        if (item2 != null && item2.getId() != -1) manager.deleteItem(item2);
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  /**
   * Tests add,delete,add item sequence.
   */
  public void testAddDeleteAddItem() {
    ChannelIF channel = null;
    ItemIF item1 = null;
    ItemIF item2 = null;

    try {
      // Create channel with two items
      channel = manager.createChannel(tuids1, url1);
      item1 = manager.createItem(channel, tuids1);

      // Delete first item
      manager.deleteItem(item1);

      item2 = manager.createItem(channel, tuids2);

      // Check item and channel
      assertEquals("Item ID should be in uninitialized state (-1).", -1, item1.getId());

      final Collection items = channel.getItems();
      assertNotNull(items);
      assertEquals("Number of items in list is incorrect.", 1, items.size());
      assertTrue("Wrong item was removed.", item2 == items.iterator().next());
    } catch (PersistenceManagerException e) {
      fail();
    } finally {
      // Delete items and channel
      try {
        if (item1 != null && item1.getId() != -1) manager.deleteItem(item1);
        if (item2 != null && item2.getId() != -1) manager.deleteItem(item2);
        if (channel != null && channel.getId() != -1) manager.deleteChannel(channel);
      } catch (PersistenceManagerException e) {
        // We can do nothing here.
      }
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Concurrency
  // -----------------------------------------------------------------------------------------------

  /**
   * Tests how concurent creation of items works.
   */
  public void testConcItemCreation()
  {
    final int itemsCount = 100;

    // Create empty channel
    ChannelIF chan = null;
    try {
      chan = manager.createChannel(tuids1, url1);
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail("Failed to create test channel.");
    }

    final ChannelIF channel = chan;

    // Create new thread which will be waiting for notification to start
    // creation of 10 items
    final RacingThread thread = new RacingThread(new ExRunnable() {

      /**
       * Runs the task and throws our any exceptions.
       *
       * @throws Exception in any case.
       */
      public void run() throws Exception {
        for (int i = 0; i < itemsCount; i++)
        {
          manager.createItem(channel, "i_tread_" + i);
        }
      }
    });

    // Start race
    boolean failed = false;
    thread.start();
    try {
      for (int i = 0; i < itemsCount; i++)
      {
        manager.createItem(channel, "i_" + i);
      }
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      failed = true;
    } finally {
      thread.waitForFinish();
    }

    // Check the results
    assertFalse("Main thread failed.", failed);
    if (thread.hasFailed()) {
      thread.getException().printStackTrace();
      fail("Racer thread failed.");
    }

    assertEquals("Incorrect number of items in channel.",
      itemsCount * 2, channel.getItems().size());
  }

  // -----------------------------------------------------------------------------------------------
  // Tools
  // -----------------------------------------------------------------------------------------------

  /**
   * Uses manager to get group by its ID.
   *
   * @param id  ID of group.
   *
   * @return group object or NULL if ID is not found.
   *
   * @throws PersistenceManagerException in cases of error.
   */
  private ChannelGroupIF findGroupById(long id)
    throws PersistenceManagerException {
    ChannelGroupIF result = null;

    final ChannelGroupIF[] groups = manager.getGroups();
    boolean found = false;
    int i = 0;
    while (i < groups.length && !found) {
      result = groups[i];
      found = result.getId() == id;
      i++;
    }

    return found ? result : null;
  }

  // -----------------------------------------------------------------------------------------------
  // Classes
  // -----------------------------------------------------------------------------------------------

  /**
   * Helper-thread to perform any concurrent tasks. Just give it Runnable task and start when
   * necessary. It will hold any exception from the task in internal variable, which is
   * accessible from outer world through <code>getException</code>.
   */
  private static class RacingThread extends Thread {

    private ExRunnable runnable;
    private Exception exception = null;
    private boolean finished;

    /**
     * Creates new single-race thread with task defined with Runnable.
     *
     * @param r task to execute.
     */
    public RacingThread(ExRunnable r) {
      runnable = r;
    }

    /**
     * Waits for the 'green light' from <code>startRace</code> and does the task.
     */
    public void run() {
      finished = false;
      // Do the job
      try {
        runnable.run();
      } catch (Exception e) {
        exception = e;
      }

      // notify about finish
      synchronized (this) {
        finished = true;
        this.notify();
      }
    }

    /**
     * Returns TRUE if thread failed to do the job. Exception is availble through
     * <code>getException()</code>.
     *
     * @return TRUE if failed to finish.
     */
    public boolean hasFailed() {
      return exception != null;
    }

    /**
     * Returns Exception from the task if any.
     *
     * @return Exception or NULL.
     */
    public Exception getException() {
      return exception;
    }

    /**
     * Waits for thread to finish.
     */
    public void waitForFinish()
    {
      synchronized(this) {
        if (!finished) {
          try {
            wait();
          } catch (InterruptedException e) {
          }
        }
      }
    }
  }

  /**
   * Runnable task, which is capable to throw exceptions out.
   */
  private static interface ExRunnable {
    /**
     * Runs the task and throws our any exceptions.
     *
     * @throws Exception in any case.
     */
    public void run() throws Exception;
  }
}
