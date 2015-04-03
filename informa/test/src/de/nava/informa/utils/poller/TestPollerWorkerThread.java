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
// $Id: TestPollerWorkerThread.java 762 2005-05-26 10:13:23Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.manager.PersistenceManagerIF;
import de.nava.informa.utils.manager.PersistenceManagerException;
import de.nava.informa.utils.manager.memory.PersistenceManager;
import de.nava.informa.utils.toolkit.ChannelRecord;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 * @see PollerWorkerThread
 */
public class TestPollerWorkerThread extends TestCase {
  private PersistenceManagerIF pm = new PersistenceManager();

  /**
   * Checks the case when existing channel has no duplicate items (empty).
   *
   * @see PollerWorkerThread#checkItems
   */
  public void testCheckItemsEmpty() {
    // Create worker with observer. We don't need job source for this test.
    PollerObserverIF o = new CustomObserver();
    PollerApproverIF a = new CustomApprover("wanted");
    PollerWorkerThread worker = new PollerWorkerThread(o, a, Poller.POLICY_SCAN_ALL, null, null);

    // Create temp and main channels.
    try {
      final ChannelIF cMain = pm.createChannel("main", getTestURL());
      final ChannelIF cTemp = pm.createChannel("temp", getTestURL());
      final ItemIF item1 = pm.createItem(cTemp, "wanted");
      pm.createItem(cTemp, "unwanted");

      // Check items in temp agains main channel.
      ChannelRecord rec = new ChannelRecord(cMain, 0, 0);
      worker.checkItems(cTemp, rec);

      // Check results.
      assertEquals(1, cMain.getItems().size());
      assertEquals(item1, cMain.getItems().toArray()[0]);
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Checks the case when existing channel has duplicate items.
   *
   * @see PollerWorkerThread#checkItems
   */
  public void testCheckItemsDuplicate() {
    // Create worker with observer. We don't need job source for this test.
    PollerObserverIF o = new CustomObserver();
    PollerApproverIF a = new CustomApprover("wanted");
    PollerWorkerThread worker = new PollerWorkerThread(o, a, Poller.POLICY_SCAN_ALL, null, null);

    // Create temp and main channels.
    try {
      final ChannelIF cMain = pm.createChannel("main", getTestURL());
      final ChannelIF cTemp = pm.createChannel("temp", getTestURL());
      pm.createItem(cTemp, "wanted");
      pm.createItem(cTemp, "unwanted");

      // Add duplicate "wanted" item to main channel.
      final ItemIF item3 = pm.createItem(cMain, "wanted");

      // Check items in temp agains main channel.
      worker.checkItems(cTemp, new ChannelRecord(cMain, 0, 0));

      // Check results.
      assertEquals(1, cMain.getItems().size());
      assertEquals(item3, cMain.getItems().toArray()[0]);
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * @see PollerWorkerThread#differ
   */
  public void testDiffer() {
    assertFalse(PollerWorkerThread.differ(null, null));
    assertTrue(PollerWorkerThread.differ(null, "a"));
    assertTrue(PollerWorkerThread.differ("a", null));
    assertTrue(PollerWorkerThread.differ("a", "b"));
    assertFalse(PollerWorkerThread.differ("a", "a"));
  }

  public void testChannelHasChanged() {
    try {
      final ChannelIF a = pm.createChannel("test", getTestURL());

      assertTrue(PollerWorkerThread.channelHasChanged(null, a));
      assertFalse(PollerWorkerThread.channelHasChanged(a, a));

      final ChannelIF b = pm.createChannel("test2", getTestURL());
      assertTrue(PollerWorkerThread.channelHasChanged(a, b));

      final ChannelIF c = pm.createChannel("test", getTestURL());
      assertFalse(PollerWorkerThread.channelHasChanged(a, c));
    } catch (PersistenceManagerException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Returns test URL.
   *
   * @return test URL.
   */
  private static URL getTestURL() {
    URL url = null;

    try {
      url = new URL("http://www.test.org");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return url;
  }

  /**
   * Custom observer of poller events.
   */
  private static class CustomObserver implements PollerObserverIF {
    /**
     * Invoked by Poller when new item is approved for addition. Item is transient
     * and should be added to specified channel.
     *
     * @param item    item added.
     * @param channel destination channel.
     */
    public void itemFound(ItemIF item, ChannelIF channel) {
      channel.addItem(item);
    }

    /**
     * Invoked by Poller when poller of the channel failed.
     *
     * @param channel channel.
     * @param e       original cause of failure.
     */
    public void channelErrored(ChannelIF channel, Exception e) {
    }

    /**
     * Invoked when Poller detected changes in channel information (title and etc).
     *
     * @param channel channel.
     */
    public void channelChanged(ChannelIF channel) {
    }

    /**
     * Invoked by Poller when checking of the channel started.
     *
     * @param channel channel.
     */
    public void pollStarted(ChannelIF channel) {
    }

    /**
     * Invoked by Poller when checking of the channel finished.
     *
     * @param channel channel.
     */
    public void pollFinished(ChannelIF channel) {
    }
  }

  /**
   * Custom approver, which approves items with given title.
   */
  private static class CustomApprover implements PollerApproverIF {
    private String title;

    /**
     * Creates approver.
     *
     * @param itemTitle title of the item to approve.
     */
    public CustomApprover(String itemTitle) {
      title = itemTitle;
    }

    /**
     * Decides whether it's possible to add item to the channel or no.
     *
     * @param item    item to add.
     * @param channel destination channel.
     * @return TRUE if addition is allowed.
     */
    public boolean canAddItem(ItemIF item, ChannelIF channel) {
      return title.equals(item.getTitle());
    }
  }
}
