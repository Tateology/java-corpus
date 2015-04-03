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
// $Id: Poller.java 762 2005-05-26 10:13:23Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.toolkit.*;

/**
 * Background poller of channels and groups of channels. Default poller period of channel is
 * set to an hour.
 * <p>
 * Each Poller instance has its own scheduler to manage the schedule of updates and
 * worker threads manager to manage processing of updates.</p>
 * <p>
 * Right after the instance of Poller is created you might be interested in adding some
 * observers (<code>addObserver()</code>) to receive events on channels updates. Registered
 * observers are added to the composite observer which is following Composite pattern to
 * combine all of the listeners and present them as single instance convenient for operation.
 * This instance is passed to all newly created worker threads. Later it will be used by
 * worker threads to report about changes.</p>
 * <p>
 * <i>If you are interested in making changes directly in persistence storage then, please,
 * look at <code>PersistenceObserver</code> class. It's simple implementation of persistence
 * changer based on Observer events.</i>
 * <p>
 * You also may require to filter newly found items in channels being polled. Using Approvers
 * you can receive events only when <i>wanted</i> items are detected. Each registered Approver
 * votes for addition of newly detected item in channel. All of registered Approvers should
 * vote for addition in order to send event about newly detected item to Observers.</p>
 * <p>
 * Finally, you need to register some channels to get benefits from Poller. Using appropriate
 * methods you can register and unregister channels, request immediate updates and change
 * polling period for all regitered channels at once.</p>
 * <p>
 * Poller has different item scanning policies. By default, it uses <code>POLICY_SCAN_ALL</code>
 * which means that every item in the feed is scanned. It's possible to tell Poller to skip
 * items which are going after the first detected existing item. It is very convenient when you
 * need to find only really new items. Use <code>POLICY_SKIP_AFTER_EXISTING</code> policy for
 * that.
 * <p>
 * It's also possible to specify user-agent name to be used when making connection through
 * HTTP protocol. You can do so only during Poller construction.
 * <p>
 * When the poller will be ready to parse the feed it will pass the opened <code>InputStream</code>
 * to <code>InputSourceProviderIF</code> implementation specified during Poller creation.
 * It's convenient if some additional wrapping of input streams is required to, for example,
 * pre-process data in some way before it gets into the parser. One of the coolest applications
 * could be is reading of compressed streams, which are decompressed in real-time. The parser
 * will get decompressed copy and will not notice the change.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class Poller {
  private static final long DEFAULT_POLL_PERIOD   = 3600000; // millis (60 minutes)
  private static final int DEFAULT_WORKER_THREADS = 5;

  /** Scanning for new items in channel finishes when existing item detected. */
  public static final int POLICY_SKIP_AFTER_EXISTING =
    PollerWorkerThread.POLICY_SKIP_AFTER_EXISTING;

  /** Scanning for new items performed fully. */
  public static final int POLICY_SCAN_ALL         = PollerWorkerThread.POLICY_SCAN_ALL;

  private WorkersManager    workersManager;
  private Scheduler         scheduler;

  private CompositeObserver compositeObserver;
  private CompositeApprover compositeApprover;

  private long              globalPollPeriod = DEFAULT_POLL_PERIOD;

  private int               itemScanningPolicy;

  private String            userAgent;

  private InputSourceProviderIF inputSourceProvider;
  private InputStreamProviderIF inputStreamProvider;

  /**
   * Creates poller with default number of worker threads, POLICY_SCAN_ALL scanning policy and
   * default user agent.
   */
  public Poller() {
    this(DEFAULT_WORKER_THREADS);
  }

  /**
   * Creates poller with POLICY_SCAN_ALL scanning policy and default user agent.
   *
   * @param workerThreads desired number of worker threads.
   */
  public Poller(int workerThreads) {
    this(workerThreads, POLICY_SCAN_ALL);
  }

  /**
   * Creates poller with default user agent.
   *
   * @param workerThreads       desired number of worker threads.
   * @param itemScanningPolicy  policy of item scanning.
   */
  public Poller(int workerThreads, int itemScanningPolicy) {
    this(workerThreads, itemScanningPolicy, null);
  }

  /**
   * Creates poller.
   *
   * @param workerThreads       desired number of worker threads.
   * @param itemScanningPolicy  policy of item scanning.
   * @param userAgent           user agent to be used for HTTP connections or NULL for default.
   */
  public Poller(int workerThreads, int itemScanningPolicy, String userAgent) {
    this(workerThreads, itemScanningPolicy, userAgent, null, null);
  }

  /**
   * Creates poller.
   *
   * @param workerThreads       desired number of worker threads.
   * @param itemScanningPolicy  policy of item scanning.
   * @param userAgent           user agent to be used for HTTP connections or NULL for default.
   * @param inputSourceProvider provider of <code>InputSource</code> ready for parsing for a
   *                            given feed <code>InputStream</code>.
   * @param inputStreamProvider provider of <code>InputStream</code> ready for fetching data of
   *                            feed.
   */
  public Poller(int workerThreads, int itemScanningPolicy, String userAgent,
                InputSourceProviderIF inputSourceProvider,
                InputStreamProviderIF inputStreamProvider) {

    // Create composite objects.
    compositeObserver = new CompositeObserver();
    compositeApprover = new CompositeApprover();

    // Record setup
    this.itemScanningPolicy = itemScanningPolicy;
    this.userAgent = userAgent;
    this.inputSourceProvider = inputSourceProvider;
    this.inputStreamProvider = inputStreamProvider;

    // Initialize workers manager.
    workersManager = new WorkersManager(new PollerThreadFactory(), workerThreads);

    // Initialize scheduler.
    scheduler = new Scheduler(new SchedulerCallback());
  }

  /**
   * Sets count on number of poller threads. This method will not kill all currently running
   * extra threads. It will instruct extra threads to terminate once their job is over or
   * create new threads if new count is bigger than previous.
   *
   * @param count new count on number of poller threads.
   */
  public final void setWorkerThreads(int count) {
    workersManager.setWorkerThreads(count);
  }

  /**
   * Register new channel for background poller.
   *
   * @param channel channel to register.
   */
  public final void registerChannel(ChannelIF channel) {
    if (channel != null) scheduler.schedule(channel, globalPollPeriod, ChannelRecord.PRIO_NORMAL);
  }

  /**
   * Register new channel for background poller with custom period.
   *
   * @param channel channel.
   * @param period  period in millis.
   */
  public final void registerChannel(ChannelIF channel, long period) {
    if (channel != null) scheduler.schedule(channel, period, ChannelRecord.PRIO_NORMAL);
  }

  /**
   * Register new channel for background poller with custom period.
   *
   * @param channel channel.
   * @param delay   delay before first check.
   * @param period  period in millis.
   */
  public final void registerChannel(ChannelIF channel, long delay, long period) {
    if (channel != null) scheduler.schedule(channel, delay, period, ChannelRecord.PRIO_NORMAL);
  }

  /**
   * Performs immediate update of the channel and reworks the schedule starting from current time.
   * If channel isn't registered yet it will be registered with normal priority.
   *
   * @param channel channel to update.
   */
  public final void updateChannel(ChannelIF channel) {
    if (channel != null) scheduler.triggerNow(channel);
  }

  /**
   * Unregister the channel from background poller.
   *
   * @param channel channel to unregister.
   */
  public final void unregisterChannel(ChannelIF channel) {
    if (channel != null) scheduler.unschedule(channel);
  }

  /**
   * Adds observer to the list of interested parties.
   *
   * @param observer new observer.
   */
  public final void addObserver(PollerObserverIF observer) {
    compositeObserver.add(observer);
  }

  /**
   * Removes observer from the list.
   *
   * @param observer observer to remove.
   */
  public final void removeObserver(PollerObserverIF observer) {
    compositeObserver.remove(observer);
  }

  /**
   * Adds approver to the list.
   *
   * @param approver approver.
   */
  public final void addApprover(PollerApproverIF approver) {
    compositeApprover.add(approver);
  }

  /**
   * Removes approver from the list.
   *
   * @param approver approver object.
   */
  public final void removeApprover(PollerApproverIF approver) {
    compositeApprover.remove(approver);
  }

  /**
   * Sets global update period to the specified value. All tasks will be rescheduled.
   *
   * @param period period in millis.
   */
  public final void setPeriod(long period) {
    this.globalPollPeriod = period;
    scheduler.rescheduleAll(period);
  }

  /**
   * Callback implementation, which simply passes the request to workers manager.
   */
  private class SchedulerCallback implements SchedulerCallbackIF {
    /**
     * Invoked by scheduler when time to process channel information comes.
     *
     * @param record channel record.
     */
    public void process(ChannelRecord record) {
      workersManager.process(record);
    }
  }

  /**
   * Worker threads factory for Poller.
   */
  private class PollerThreadFactory implements WorkerThreadFactoryIF {

    /**
     * Creates new worker thread object.
     *
     * @return worker thread object.
     */
    public WorkerThread create() {
      PollerWorkerThread workerThread =
        new PollerWorkerThread(compositeObserver, compositeApprover, itemScanningPolicy,
          inputSourceProvider, inputStreamProvider);

      if (userAgent != null) workerThread.setUserAgent(userAgent);

      return workerThread;
    }
  }
}
