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

// $Id: TestHibernateStressTest.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.InformaTestCase;
import de.nava.informa.utils.PersistChanGrpMgr;
import de.nava.informa.utils.PersistChanGrpMgrObserverIF;
import de.nava.informa.utils.RssUrlTestData;

/**
 * Stress test of Informa's hibernate backend.
 *
 * @author Pito Salas
 */
public final class TestHibernateStressTest extends InformaTestCase {

  static Log logger = LogFactory.getLog(TestHibernateStressTest.class);

  private SessionHandler scaleSessHandler;
  protected PersistChanGrpMgr managers[];
  int nManagers;
  private int nChans;
  private int itemMax;
  boolean activeSemaphore;
  protected List<ItemLogEntry> itemLog;
  private List<ChannelLogEntry> channelLog;
  private ItemDeleter itemDeleterThreads[];

  private String scaleDbPath;

  /**
   * Constructor of this test case
   *
   * @param testname
   */
  public TestHibernateStressTest(String testname) {
    super("TestHibernateStressTest", testname);
    System.setProperty("sun.net.client.defaultReadTimeout", "10000");
    System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
  }

  /**
   * Basic Stress test. Simply Run a bunch of PersistentChannelGroups in parallel for a while and
   * then verify that the disk info matches what we found the first time through.
   *
   * @throws Exception
   */
  public void testgetNVerify() throws Exception {
    informaGetNVerify(10, 5, 600);
  }

  /**
   * Add/Delete Stress Test.
   *
   * @throws Exception
   */
  public void testAddDelete() throws Exception {
    informaAddDelete(5, 5, 100, 3, 50);
  }

  /**
   * Baseline test: Get a bunch of feeds from the internet, persist them, and close the database,
   * reopen it, and see if all the information is as expected.
   *
   * @param mancount - Number of PersistentChannelGroupManagers to create for the test
   * @param chanCount - Number of channels in each one
   * @param itemCount - Number of items total to fetch from internet before moving to verification
   *        phase
   * @throws Exception
   */
  private void informaGetNVerify(int mancount, int chanCount, int itemCount) throws Exception {
    this.nManagers = mancount;
    this.nChans = chanCount;
    this.itemMax = itemCount;
    scaleDbPath = "test/data/hibernate/";

    initLoggers();
    openScaleDatabase(true); // Create new virgin database
    createScaleChannelGroups();
    runUntilNitems(itemCount);
    closeScaleDatabase();

    // now, let's check that we have exactly the right stuff when we re-open
    clearChannelGroups();
    openScaleDatabase(false); // Use database that was created above
    restoreScaleChannelGroups();
    verifyChannelLogEntryValidity();
    verifyItemLogValidity();
  }

  /**
   * More sophisticated stress test which simultanously adds and deletes Articles and Channels.
   *
   * @param mancount Number of PersistChanGrpMgrs involved
   * @param chanCount Number of Informa Channels in each one
   * @param phase1itemCount Number of items logged before moving to Phase 2
   * @param deleterThreadsCount Number of deleter Threads
   * @param phase2itemCount Number of items logged before moving to Phase 3
   * @throws Exception
   */
  private void informaAddDelete(int mancount, int chanCount, int phase1itemCount,
      int deleterThreadsCount, int phase2itemCount) throws Exception {

    this.nManagers = mancount;
    this.nChans = chanCount;
    scaleDbPath = "test/data/hibernate/";

    initLoggers();

    // Phase 1: Add Channels and articles
    logger.debug("Start Phase 1");
    this.itemMax = phase1itemCount;
    openScaleDatabase(true); // Create new virgin Database
    createScaleChannelGroups(); // Add the requisite ChannelGroups and the Channels
    runUntilNitems(phase1itemCount); // Run until we have the right number of items

    // Phase 2: Continue running but start deleting articles
    logger.debug("Start Phase 2");
    createDeleterThreads(deleterThreadsCount); // Create and start threads to delete items
    runUntilNitems(phase2itemCount); // Run until we have the requested number of items

    // Phase 3: Just run the deleter threads until they are all done.
    logger.debug("Start Phase 3");
    waitForDeleterThreadsToBeDone();

    logger.debug("Test complete.");
  }

  /**
   * Create and start the indicated number of ArticleDeleterThreads.
   *
   * @param count
   */
  private void createDeleterThreads(int count) {
    itemDeleterThreads = new ItemDeleter[count];
    for (int i = 0; i < count; i++) {
      itemDeleterThreads[i] = new ItemDeleter();
      itemDeleterThreads[i].start();
      itemDeleterThreads[i].setName("Deleter: " + i);
    }
  }

  /**
   * The DeleterThreads automatically set keepGoing to false when there are no remaining items to
   * delete. This method loops (with sleeps in between) waiting for all ArticleDeleterThreads to be
   * done.
   *
   */
  private void waitForDeleterThreadsToBeDone() {
//    int timeoutCounter = 0;
//    boolean stillRunning = true;
//    while (stillRunning) {
//      assertTrue("Timed out waiting for Deleter threads to be done", timeoutCounter < 1000);
//      stillRunning = false;
//      for (int i = 0; i < itemDeleterThreads.length; i++) {
//        if (itemDeleterThreads[i].keepGoing) stillRunning = true;
//      }
//      timeoutCounter++;
//      try {
//        Thread.sleep(100);
//      } catch (InterruptedException e) {
//        return;
//      }
//
//  }
    for (int i = 0; i < itemDeleterThreads.length; i++) {
      itemDeleterThreads[i].waitForFinish();
    }
  }

  /**
   * Look through the ChannelLog, comparing what we saw during record and verify mode. There are
   * various cases that indicate errors.
   */
  private void verifyChannelLogEntryValidity() {
    synchronized (channelLog) {
      Iterator<ChannelLogEntry> chanLogIter = channelLog.iterator();
      while (chanLogIter.hasNext()) {
        ChannelLogEntry next = chanLogIter.next();
        // if recordCounter == 0 and verifyCounter != 0, we got a channel from
        // disk which we
        // didn't see from the net
        assertTrue("we got channel from disk which we didn't see from the net:" + next.theKey,
            !(next.recordCounter == 0 && next.verifyCounter > 0));

        // if recordCounter > 0 and verifyCounter == 0, we got a channel from
        // the net, which
        // we then didn't see from disk
        assertTrue("we got a channel from the net, which we then didn't see from disk:"
            + next.theKey, !(next.recordCounter > 0 && next.verifyCounter == 0));

        // If recordCounter < verifyCounter, we got a channel from disk more
        // than from
        // memory
        assertTrue("we got a channel from disk more often than from memory:" + next.theKey,
            !(next.recordCounter < next.verifyCounter));

        // If verifyCounter > 1, we got a channel from disk more than once
        assertTrue("we got a channel from disk more than once:" + next.theKey,
            !(next.verifyCounter > 1));

      }
    }
  }

  /**
   * Look through the ItemLog to see if things are consistent. N.B. We stop checking after the
   * number of items that were requested. The reason is that at that point the PersistChanGrps are
   * deactivated. However they are not killed explicitly and so they may run on a bit beyond leaving
   * the itemLog in an inconsistent state.
   *
   */
  private void verifyItemLogValidity() {
    int counter = 0;
    synchronized (itemLog) {
      Iterator<ItemLogEntry> itemLogIter = itemLog.iterator();
      while (itemLogIter.hasNext() && counter < itemMax) {
        counter++;
        ItemLogEntry next = itemLogIter.next();
        String msgHdr = next.theItem.getChannel() + ":" + next.theKey;

        // This is code is structured this way to allow me to put a breakpoint
        final boolean diskNotNet = next.recordCounter == 0 && next.verifyCounter > 0;
        if (diskNotNet) {
          assertTrue("we saw an item from disk which we didn't see from the net: " + msgHdr,
              !diskNotNet);

        }

        final boolean netNotDisk = next.recordCounter > 0 && next.verifyCounter == 0;
        if (netNotDisk) {
          assertTrue("we saw an item from the Net which we didn't see from disk: " + msgHdr,
              !netNotDisk);
        }

      }
    }
  }

  /**
   * Forget all the informa state
   */
  private void clearChannelGroups() {
    deActivateProcesses();
    managers = null;
  }

  /**
   * Create nManager test Channel Groups of nChannels Channels each.
   */
  private void createScaleChannelGroups() {
    // Create an array of ChannelGroupManagers
    managers = new PersistChanGrpMgr[nManagers];
    int channelIndex = 0;

    for (int i = 0; i < nManagers; i++) {
      managers[i] = new PersistChanGrpMgr(scaleSessHandler, true);
      assertNotNull(managers[i]);
      managers[i].createGroup(generateChanGrpName(i));
      managers[i].setGlobalObserver(new ChannelLogRecordObserver());
      for (int chans = 0; chans < nChans; chans++) {
        Channel nextChan = managers[i].addChannel(RssUrlTestData.get(channelIndex++));
        managers[i].notifyItems(nextChan);
      }
      managers[i].notifyChannels();
    }
  }

  /**
   * Create the PersistentChannelGroups and restore their state from the database. Assumes that the
   * database is already open. Assume that the managers array is null again. After reading in each
   * channel from disk, we ask Informa to notify for all the items and channels. As the channels are
   * not activated, Informa will not start accessing the internet for new channels.
   *
   * @throws Exception (handled by JUnit)
   */
  private void restoreScaleChannelGroups() throws Exception {
    managers = new PersistChanGrpMgr[nManagers];
    int channelIndex = 0;

    for (int i = 0; i < nManagers; i++) {
      managers[i] = new PersistChanGrpMgr(scaleSessHandler, true);
      assertNotNull(managers[i]);
      managers[i].createGroup(generateChanGrpName(i));
      managers[i].setGlobalObserver(new informaLogObserver());
      for (int chans = 0; chans < nChans; chans++) {
        Channel nextChan = managers[i].addChannel(RssUrlTestData.get(channelIndex++));
        managers[i].notifyItems(nextChan);
      }
      managers[i].notifyChannels();
    }
  }

  /**
   * Sleep until the indicated "N" items have been logged.
   *
   * @param N
   *
   * @throws Exception (handled by JUnit)
   */
  private void runUntilNitems(int N) throws Exception {
    setActiveSemaphor(true);
    activateProcesses();
    while (getActiveSemaphor()) {
      Thread.sleep(1000);
      setActiveSemaphor(itemLog.size() < N);
    }
    deActivateProcesses();
  }

  /**
   * Return current value of activeSemaphor. True means background RSS poller in Informa is
   * happening False means it is not
   *
   * @return current value of activeSemaphor
   */
  synchronized boolean getActiveSemaphor() {
    return activeSemaphore;
  }

  /**
   * Set activeSemaphor to new value, and return old value
   *
   * @param newval - true means background poller is active
   * @return what the status was before
   */
  synchronized private boolean setActiveSemaphor(boolean newval) {
    boolean oldval = activeSemaphore;
    activeSemaphore = newval;
    return oldval;
  }

  /**
   * Start the background processing of channels
   */
  private void activateProcesses() {
    for (int i = 0; i < nManagers; i++) {
      managers[i].activate();
    }
  }

  /**
   * Stop the background processing of channels
   */
  private void deActivateProcesses() {
    for (int i = 0; i < nManagers; i++) {
      managers[i].deActivate();
    }
  }

  /**
   * Regenerate the empty datanase
   *
   * @return @throws FileNotFoundException
   *
   * @throws FileNotFoundException
   */
  private boolean getVirginDb() throws FileNotFoundException {
    boolean fileOneIsOK, fileTwoIsOK;
    fileTwoIsOK = copyFiles(scaleDbPath + "informa.script", scaleDbPath + ".script");
    fileOneIsOK = copyFiles(scaleDbPath + "informa.properties", scaleDbPath + ".properties");
    return fileOneIsOK && fileTwoIsOK;
  }

  /**
   * Make a simple copy of one file to another
   *
   * @param src
   * @param dest
   * @return true if copy worked.
   * @throws FileNotFoundException
   */
  private boolean copyFiles(String src, String dest) throws FileNotFoundException {
    boolean result = false;
    InputStream srcStream = new FileInputStream(src);
    if (srcStream != null) {
      try {
        // Create channel on the destination
        FileOutputStream dstStream = new FileOutputStream(dest);
        int ch; // the buffer
        while ((ch = srcStream.read()) != -1) {
          dstStream.write(ch);
        }
        srcStream.close();
        dstStream.close();
        result = true;
      } catch (IOException e) {
        result = false;
      }
    }
    if (!result) fail("Failed to copy File  " + src + " to " + dest);
    return result;
  }

  /**
   * Open (or re-open) the database. Optionally begin from a blank database (virgin)
   *
   * @param virgin true means start from a fresh database
   * @throws Exception
   */
  void openScaleDatabase(boolean virgin) throws Exception {
    if (virgin) getVirginDb();

    Properties hibernateProps = new Properties();
    hibernateProps.setProperty("hibernate.connection.url", "jdbc:hsqldb:" + scaleDbPath);
    scaleSessHandler = SessionHandler.getInstance(hibernateProps);
    assertNotNull(scaleSessHandler);
  }

  /**
   * Close the database fully
   *
   * @throws Exception
   */
  private void closeScaleDatabase() throws Exception {
    scaleSessHandler.getSession().flush();
    scaleSessHandler.getSession().close();
  }

  /**
   * Generate a fake name for a generated channel
   *
   * @param i Index of channel involved
   * @return fake name
   */
  private String generateChanGrpName(int i) {
    return "Channel Group" + i;
  }

  /**
   * Called when Informa retrieves a Channel. We use it in two modes: when channels are first
   * retrieved from RSS over the network (recordmode = true), and then again when channels are
   * retrieved when the persistent hibernate database is opened, from disk (verify mode --
   * recordmode = false).
   *
   * There are two counters in the ChannelLogEntry, one that counts how often the particular Channel
   * was seen during record mode, and the other during verify mode. Depending on the circumstance we
   * can detect failure conditions.
   *
   * if recordCounter == 0 and verifyCounter != 0, we got a channel from disk which we didn't see
   * from the net if recordCounter > 0 and verifyCounter == 0, we got a channel from the net, which
   * we then didn't see from disk
   *
   * @param channel - Relevant Channel.
   * @param recordMode - are we recording (true) or verifying (false)
   */
  void chanLogUpdate(ChannelIF channel, boolean recordMode) {
    assertNotNull("ChannelRetrieved returned Null", channel);
    URL locU = channel.getLocation();
    String key = locU == null ? "<not yet>" : locU.toString();

    Iterator<ChannelLogEntry> channelIt = channelLog.iterator();
    ChannelLogEntry found = null;

    // Locate a ChannelLogEntry for specified Channel's getLocation URL; create
    // one if none is there yet
    while (channelIt.hasNext()) {
      ChannelLogEntry tmp = channelIt.next();
      if (key.equals(tmp.theKey)) {
        found = tmp;
        break;
      }
    }
    if (found == null) {
      found = new ChannelLogEntry(channel, key);
      synchronized (channelLog) {
        channelLog.add(found);
      }
    }

    // Increment the appropriate counter depending on whether we are recording
    // or verifying.
    if (recordMode) {
      found.recordCounter++;
    } else {
      found.verifyCounter++;
    }
  }

  /**
   * Called when Informa retrieves an Item.
   *
   * @param item - Item to be recorded in log
   * @param recordMode - are we recording (true) or verifying (false)
   */
  void itemLogUpdate(Item item, boolean recordMode) {
    assertNotNull("itemRetrieved returned Null", item);
    String atitle = item.getTitle();

    Iterator<ItemLogEntry> itemIt = itemLog.iterator();
    ItemLogEntry found = null;

    // Locate an ItemLog for specified item
    while (itemIt.hasNext()) {
      ItemLogEntry tmp = itemIt.next();
      if (atitle.equals(tmp.theKey)) {
        found = tmp;
        break;
      }
    }
    if (found == null) {
      found = new ItemLogEntry(item, atitle);
      synchronized (itemLog) {
        itemLog.add(found);
      }
    }

    // Increment the appropriate counter depending on whether we are recording
    // or verifying.
    if (recordMode) {
      found.recordCounter++;
    } else {
      found.verifyCounter++;
    }
  }

  /**
   * Initialize logging lists back to zero.
   */
  private void initLoggers() {
    itemLog = new ArrayList<ItemLogEntry>();
    channelLog = new ArrayList<ChannelLogEntry>();
  }

  // -------------------------------------------------------------------
  // Internal classes
  // -------------------------------------------------------------------

  /**
   * Separate Global Observers for when we are fetching Informa/RSS information from the Net or from
   * the Hiberate/informa persistent state.
   */
  class ChannelLogRecordObserver implements PersistChanGrpMgrObserverIF {

    /**
     * Called when a Channel is retrieved from the internet
     *
     * @param chan -
     */
    public void channelRetrieved(ChannelIF chan) {
      chanLogUpdate(chan, true);
    }

    /**
     * Called when an Item is retrieved from the internet
     *
     * @param newItem -
     */
    public void itemAdded(ItemIF newItem) {
      itemLogUpdate((Item) newItem, true);
    }

    /**
     * Called to indicate start and end of actual poller of feeds by Informa
     *
     * @param name name of group being polled
     * @param count count of how many times this group has been polled
     * @param now true to start, false at end
     */
    public void pollingNow(String name, int count, boolean now) {
      // No testing ramifications.
    }
  }

  /**
   * Called by Informa when interesting things happen
   *
   */
  class informaLogObserver implements PersistChanGrpMgrObserverIF {

    /**
     *
     * Called when a Channel is retrieved from persistent Informa state
     *
     * @param chan -
     */
    public void channelRetrieved(ChannelIF chan) {
      chanLogUpdate(chan, false);
    }

    /**
     * Called when an Item is retrieved from persistent Informa state
     *
     * @param newItem -
     */
    public void itemAdded(ItemIF newItem) {
      itemLogUpdate((Item) newItem, false);
    }

    /**
     * Called to indicate start and end of actual poller of feeds by Informa
     *
     * @param name name of group being polled
     * @param count count of how many times this group has been polled
     * @param now true to start, false at end
     */
    public void pollingNow(String name, int count, boolean now) {
      // No testing ramifications.
    }

  }

  /**
   * What an entry in the ChannelLog list looks like
   */
  class ChannelLogEntry {

    int recordCounter;

    int verifyCounter;

    String theKey;

    ChannelIF theChan;

    ChannelLogEntry(ChannelIF achan, String akey) {
      theChan = achan;
      theKey = akey;
      recordCounter = 0;
      verifyCounter = 0;
    }

    /**
     * Convert to a string, helpful for logging
     *
     * @return - string rendition
     */
    public String toString() {
      return "rec/ver: " + recordCounter + "/" + verifyCounter + ":" + theKey;
    }
  }

  /**
   * What an entry in the itemLog List looks like
   */
  public class ItemLogEntry {

    Item theItem;
    String theKey;
    String channelName;
    int recordCounter;
    int verifyCounter;
    boolean deleted;
    boolean unread;

    ItemLogEntry(Item anitem, String akey) {
      theItem = anitem;
      theKey = akey;
      channelName = anitem.getChannel().getTitle();
      recordCounter = 0;
      verifyCounter = 0;
      deleted = false;
      unread = true;
    }

    int getPersistChanGrpMgrIdx() {
      int result = -1;
      for (int i = 0; i < managers.length; i++) {
        if (managers[i].hasChannel((Channel) theItem.getChannel())) {
          assertTrue("Same channel in two groups", result == -1);
          result = i;
        }
      }
      assertTrue("Channel not found in any group", result != -1);
      return result;
    }

    /**
     * Convert to a string, helpful for logging
     *
     * @return - string rendition
     */
    public String toString() {
      return "rec/ver: " + recordCounter + "/" + verifyCounter + ":" + theKey + "(" + channelName
          + ")";
    }

  }

  /**
   * Thread to delete Items recorded in the ItemLog. Each thread will run through the list and
   * delete articles that were recorded in the itemLog.
   */
  class ItemDeleter extends Thread {

    boolean keepGoing;

    /**
     * Construct the thread. Start with flag to keep running.
     */
    ItemDeleter() {
      keepGoing = true;
    }

    /**
     * Wait for this thread to finish processing.
     */
    public synchronized void waitForFinish() {
      try {
        while (keepGoing) {
          wait();
        }
      } catch (InterruptedException e) {
        // Nothing to be done here. Simply pass control further if someone wishes us to
        // abort waiting.
      }
    }

    /**
     * Thread Run method. Here's the algorithm: As long as "keepGoing", look through the entries in
     * the itemLog. Locate one that has not been deleted and delete it. Mark the deleted one as
     * deleted. Sleep for a little bit of time and repeat.
     *
     * This method has fairly subtle threading and synchronization issues.
     */
    public void run() {

      // As long as parent wants this to keep on going
      while (keepGoing) {

        // Scan through the itemLog from start to end and delete all undeleted items

        boolean foundOne = false;
        ItemLogEntry[] copyItemLog = itemLog.toArray(new ItemLogEntry[0]);

        for (int i = 0; i < copyItemLog.length; i++) {
          ItemLogEntry entry = copyItemLog[i];
          synchronized (entry) {

            if (!entry.deleted) {
              // Delete it and mark it deleted, and note that we deleted one.
              PersistChanGrpMgr theGrp = managers[entry.getPersistChanGrpMgrIdx()];

              Channel theChan = (Channel) entry.theItem.getChannel();
              theGrp.deActivate();
              int before = theGrp.getItemCount(theChan);
              int after = theGrp.deleteItemFromChannel(theChan, entry.theItem);

              if (getActiveSemaphor()) theGrp.activate();
              logger.debug("Deleted. Count before =" + before + " /after: " + after);
              assertEquals("Not the rigth number of items", before, after + 1);

              entry.deleted = true;
              foundOne = true;
            }
          }
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            return;
          }
        }
        // If we didn't delete at least one, then we will scan the whole list again after sleeping.
        if (!foundOne) keepGoing = false;

      }
      // Notify everyone processing is finished. Generally this works for
      // the case if someone waits by calling waitForFinish() method.
      synchronized (this) {
        notifyAll();
      }
    }
  }
}