//
// Informa -- RSS Library for Java
// Copyright (c) 2002, 2003 by Niko Schmuck
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

// $Id: TestPersistChannelGrp.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.HibernateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.utils.InformaTestCase;
import de.nava.informa.utils.PersistChanGrpMgr;
import de.nava.informa.utils.PersistChanGrpMgrTask;
import de.nava.informa.utils.RssUrlTestData;

/**
 * Tests for the PersistChanGrpMgr and PersistChanGrpMgrTask classes
 * 
 * @author Pito Salas
 */
public class TestPersistChannelGrp extends InformaTestCase {

  private SessionHandler handler;

  private static Log logger = LogFactory.getLog(TestPersistChannelGrp.class);

  /**
   * Constructor.
   * 
   * @param name Name of the JUnit Test
   */
  public TestPersistChannelGrp(String name) {
    super("TestPersistChannelGrp", name);
  }

  /**
   * Common setup for every single test.
   * 
   * @throws HibernateException
   */
  protected void setUp() throws HibernateException {
    handler = SessionHandler.getInstance();
    handler.getSession(); // make sure the session is opened
  }

  /**
   * A battery of empty groups
   * 
   * @exception Exception (Caught by JUnit)
   *  
   */
  public void testEmptyGroups() throws Exception {
    PersistChanGrpMgr agroup, bgroup, cgroup;
    agroup = makeEmptyGroup("Group Foo");
    bgroup = makeEmptyGroup("Group Bar");
    cgroup = makeEmptyGroup("Group Too");
    assertValidGroup(agroup, "Group Foo", 0);
    assertValidGroup(bgroup, "Group Bar", 0);
    assertValidGroup(cgroup, "Group Too", 0);
  }

  /**
   * Create a group, and add some channels and see if all is well
   * 
   * @exception Exception (Caught by JUnit)
   */
  public void testGroupWithChannels() throws Exception {
    PersistChanGrpMgr agroup;
    agroup = makeEmptyGroup("Group Secret With Channels");
    assertValidGroup(agroup, "Group Secret With Channels", 0);

    addChannel(agroup, "Joho", "http://www.hyperorg.com/blogger/index.rdf");
    addChannel(agroup, "Raliable", "http://www.raibledesigns.com/rss/rd");
    addChannel(agroup, "Active Window", "http://www.activewin.com/awin/headlines.rss");
    addChannel(agroup, "Pitos Blog", "http://radio.weblogs.com/0125664/rss.xml");

    runPersistChanGrpTask(agroup, 3);
    assertValidGroup(agroup, "Group Secret With Channels", 4);
  }

  /**
   * Test adding a Channel to a group which already exists in a different group, which should just
   * work.
   * 
   * @throws Exception
   */
  public void testAddingExistingChannel() throws Exception {
    final PersistChanGrpMgr agroup;
    agroup = makeEmptyGroup("A Group");
    assertValidGroup(agroup, "A Group", 0);
    addGenChannels(agroup, 5, 3);

    final PersistChanGrpMgr bgroup;
    bgroup = makeEmptyGroup("B Group");
    assertValidGroup(bgroup, "B Group", 0);
    addGenChannels(bgroup, 5, 3);

    // Add a certain channel to this group
    addChannel(agroup, "Terrestial", "http://www.transterrestrial.com/index.xml");
    assertValidGroup(agroup, "A Group", 6);

    // and now add the SAME channel to a different group. make sure that it gets there
    addChannel(bgroup, "Terrestial", "http://www.transterrestrial.com/index.xml");
    assertValidGroup(bgroup, "B Group", 6);
  }

  /**
   * Test the functioning of the hasChannel() method.
   * 
   * @throws Exception
   */
  public void testChannelMembership() throws Exception {
    final PersistChanGrpMgr agroup;
    agroup = makeEmptyGroup("Membership Group");
    addGenChannels(agroup, 3, 3);

    final Iterator channels = agroup.getChannelGroup().getChannels().iterator();
    while (channels.hasNext()) {
      final Channel nextchan = (Channel) channels.next();
      assertTrue("Expected member channel not found", agroup.hasChannel(nextchan));
    }
  }
  
  /**
   * Test activation and deactivation of PersistChanGrpMgrs
   * 
   * @throws Exception
   */
  public void testGroupDeactivate() throws Exception {
    final PersistChanGrpMgr group1, group2, group3;
    group1 = makeEmptyGroup("Activation Group 1");
    group2 = makeEmptyGroup("Activation Group 2");
    group3 = makeEmptyGroup("Activation Group 3");

    addGenChannels(group1, 5, 3);
    addGenChannels(group2, 5, 3);
    addGenChannels(group3, 5, 3);
    
    assertFalse("expected group 1 to be inactive", group1.isActivated());
    assertFalse("expected group 2 to be inactive", group2.isActivated());
    assertFalse("expected group 3 to be inactive", group3.isActivated());
    
    group1.activate();
    assertTrue("expected group 1 to be active", group1.isActivated());
    assertFalse("expected group 2 to be inactive", group2.isActivated());
    assertFalse("expected group 3 to be inactive", group3.isActivated());

    Thread.sleep(500);
    
    group2.activate();
    assertTrue("expected group 1 to be active", group1.isActivated());
    assertTrue("expected group 2 to be active", group2.isActivated());
    assertFalse("expected group 3 to be inactive", group3.isActivated());
    
    Thread.sleep(500);

    group3.activate();
    assertTrue("expected group 1 to be active", group1.isActivated());
    assertTrue("expected group 2 to be active", group2.isActivated());
    assertTrue("expected group 3 to be active", group3.isActivated());
    
    Thread.sleep(500);

    group1.deActivate(true);
    assertFalse("expected group 1 to be inactive", group1.isActivated());
    assertTrue("expected group 2 to be active", group2.isActivated());
    assertTrue("expected group 3 to be active", group3.isActivated());
    
    Thread.sleep(500);

    group2.deActivate(true);
    assertFalse("expected group 1 to be inactive", group1.isActivated());
    assertFalse("expected group 2 to be inactive", group2.isActivated());
    assertTrue("expected group 3 to be active", group3.isActivated());
    
    Thread.sleep(500);

    group3.deActivate(true);
    assertFalse("expected group 1 to be inactive", group1.isActivated());
    assertFalse("expected group 2 to be inactive", group2.isActivated());
    assertFalse("expected group 3 to be inactive", group3.isActivated());   
  }

  /**
   * Test moving a Channels from one group to another
   * 
   * @throws Exception
   */
  public void testMoveChannelBetweenGroups() throws Exception {
    // Create the two groups
    PersistChanGrpMgr sourceGrp, destGrp;
    sourceGrp = makeEmptyGroup("Source Group");
    destGrp = makeEmptyGroup("Destination Group");

    // Populate source group with some channels.
    int count = 5;
    int sourceHas = count;
    int destHas = 0;
    addGenChannels(sourceGrp, count, 3);

    // Now move them one by one to the other ChannelGroup and verify the counts as we go
    for (int i = 0; i < count; i++) {
      // Verify that each group has the expected number of channels
      assertValidGroup(sourceGrp, "Source Group", sourceHas - i);
      assertValidGroup(destGrp, "Destination Group", destHas + i);

      // Identify the first channel after ensuring that there is at least one.
      Channel channelToMove;
      Collection chans = sourceGrp.getChannelGroup().getChannels();
      Iterator iter = chans.iterator();
      assertTrue(chans.size() > 0);
      channelToMove = (Channel) iter.next();
      sourceGrp.moveChannelTo(channelToMove, destGrp);
    }
  }

  /**
   * Test deleting individual items from Channels in a PersistChanGrp
   * 
   * @throws Exception
   */
  public void testDeleteItemsFromChannels() throws Exception {
    // Create a ChannelGroup and populate it with channels and items
    PersistChanGrpMgr delGrp;
    delGrp = makeEmptyGroup("deleteTest Group");
    int count = 10;
    addGenChannels(delGrp, count, 5);

    Object[] channels = delGrp.getChannelGroup().getChannels().toArray();
    assertEquals("Wrong number of channels in group", channels.length, count);

    // Iterate through all the new channels
    for (int i = 0; i < count; i++) {
      Channel aChan = (Channel) channels[i];
      Object[] items = aChan.getItems().toArray();
      int howManyItems = items.length;

      // Now iterate through all the items of this channel, and delete them one by one
      for (int j = 0; j < howManyItems; j++) {
        Item anItem = (Item) items[j];
        assertEquals("Wrong number of items after delete", howManyItems - j, aChan.getItems()
            .size());
        delGrp.deleteItemFromChannel(aChan, anItem);
      }
    }
  }

  /**
   * Test deleting channels from a group by adding a certain number, and then deleting them one at a
   * time.
   * 
   * @throws Exception
   */
  public void testRemoveChannelsFromGroup() throws Exception {
    PersistChanGrpMgr theGrp;
    int count = 5;
    theGrp = makeEmptyGroup("The Group");
    addGenChannels(theGrp, count, 5);

    for (int i = 0; i < count; i++) {
      ChannelBuilder bld = theGrp.getBuilder();
      bld.beginTransaction();
      bld.update(theGrp.getChannelGroup());

      // Check that we have the right number of Channels
      assertValidGroup(theGrp, "The Group", count - i);

      // Identify the first channel after ensuring that there is at least one.
      Channel channelToDelete;
      Collection chans = theGrp.getChannelGroup().getChannels();
      Iterator iter = chans.iterator();
      assertTrue(chans.size() > 0);
      channelToDelete = (Channel) iter.next();

      bld.endTransaction();

      // And delete it from the group
      theGrp.deleteChannel(channelToDelete);
    }
  }

  /**
   * Test activation and deactivation, with thread handling too/
   * 
   * @throws Exception
   */
  public void testStartStopGroup() throws Exception {
    PersistChanGrpMgr groupStopStart;
    groupStopStart = makeEmptyGroup("stopStartGroup");

    // Start and stop a few times
    for (int i = 0; i < 2; i++) {
      groupStopStart.activate();
      assertTrue("Channel Group is supposed to be activated", groupStopStart.isActivated());
      groupStopStart.deActivate(true);
      assertFalse("ChannelGroup is not supposed to be activated", groupStopStart.isActivated());
      Thread.sleep(500);
    }
  }
  
  /**
   * Check to see that itemCount method on PersistChanGrp works correctly.
   * 
   * @throws Exception
   */
  public void testItemCount() throws Exception {
    // Create a ChannelGroup and populate it with channels and items
    PersistChanGrpMgr countedGrp;
    countedGrp = makeEmptyGroup("Item Count Group");
    int count = 10;
    addGenChannels(countedGrp, count, 5);

    Object[] channels = countedGrp.getChannelGroup().getChannels().toArray();
    assertEquals("Wrong number of channels in group", channels.length, count);

    // Iterate through all the new channels
    for (int i = 0; i < count; i++) {
      Channel aChan = (Channel) channels[i];
      Object[] items = aChan.getItems().toArray();
      int howManyItems = items.length;
      assertEquals("Wrong Count returned", howManyItems, countedGrp.getItemCount(aChan));
    }
  }



  // ---------------------------------------------------------------------
  // Internal helper methods
  // ---------------------------------------------------------------------

  /**
   * Verify that a specified group has the expected number of Channels as well as the expected name.
   * 
   * @param gp - Group to check
   * @param name - expected name
   * @param size - expected Channels.
   * @exception Exception (caught by JUnit)
   */

  protected void assertValidGroup(PersistChanGrpMgr gp, String name, int size) throws Exception {
    ChannelGroup gpc = gp.getChannelGroup();
    assertEquals(gpc.getTitle(), name);
    assertEquals(size, gpc.getChannels().size());
  }
  

  /**
   * Helper to add a certain number of channels to a group. Note this uses the handy-dandy
   * RssUrlTestData to give us useful RSS urls.
   * 
   * @param aGroup what group to add to
   * @param count how many channels to use
   * @param taskRuns home many times to 'pump' the PersistChanGrpTask
   */
  private void addGenChannels(PersistChanGrpMgr aGroup, int count, int taskRuns) {
    int i;
    for (i = 0; i < count; i++) {
      Channel achannel;
      achannel = aGroup.addChannel(RssUrlTestData.generate());
      aGroup.notifyChannelsAndItems(achannel);
    }
    runPersistChanGrpTask(aGroup, taskRuns);
  }

  /**
   * Helper to create an empty group
   * 
   * @param name - Name to use
   * @return created empty group
   */
  public PersistChanGrpMgr makeEmptyGroup(String name) {
    PersistChanGrpMgr res;
    logger.info("Creating group: " + name);
    res = new PersistChanGrpMgr(handler, false);
    res.createGroup(name);
    logger.info("Result: " + res);
    assertEquals("Newly created group has non-zero Channels", 0, res.getChannelGroup()
        .getChannels().size());
    return res;
  }

  /**
   * Helper to add a channel to an existing group
   * 
   * @param agroup - group to receive them
   * @param label - label to use for tracing
   * @param url - url to give to informa
   */
  private void addChannel(PersistChanGrpMgr agroup, String label, String url) {
    Channel achannel;
    logger.info("Adding channel: " + label + " to " + agroup + " " + url);
    achannel = agroup.addChannel(url);
    agroup.notifyChannelsAndItems(achannel);
    runPersistChanGrpTask(agroup, 3);
  }

  /**
   * Run the PersistChanGrpTask n times for specified PersistChanGrpMgr. This does *NOT* Check
   * threading bugs but it does check the functionality of the background task.
   * 
   * @param mgr - Persistent group manager
   * @param count - How many times to manually run PersistenChanGrpMgr
   */

  protected void runPersistChanGrpTask(PersistChanGrpMgr mgr, int count) {
    PersistChanGrpMgrTask task = new PersistChanGrpMgrTask(mgr, 0);
    for (int i = 0; i < count; i++) {
      task.performUpdates();
    }
  }

}