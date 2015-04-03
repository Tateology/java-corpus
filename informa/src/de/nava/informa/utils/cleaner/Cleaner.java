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
// $Id: Cleaner.java 759 2005-03-11 11:35:40Z spyromus $
//

package de.nava.informa.utils.cleaner;

import de.nava.informa.utils.toolkit.WorkerThread;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.toolkit.Scheduler;
import de.nava.informa.utils.toolkit.SchedulerCallbackIF;
import de.nava.informa.utils.toolkit.WorkersManager;
import de.nava.informa.utils.toolkit.ChannelRecord;
import de.nava.informa.utils.toolkit.WorkerThreadFactoryIF;

/**
 * Cleaner is an utility class, which is intended to help applications with detection of
 * unwanted (old, corrupted and etc) items.
 * <p>
 * Cleaner works in background. It accepts unlimited number of observers, objects of
 * <code>CleanerObserverIF</code> type, that are notified each time main engine finds
 * unwanted items. In order to decide which item is unwanted engine uses matchers,
 * objects of <code>CleanerMatcherIF</code> type. Each matcher is a rule. You can create
 * unlimited number of rules to match items and tell that they are unwanted in some way.</p>
 * <p>
 * Engine accepts individual channels for registration. You can register channel either
 * with global or custom periods. Global period defaults to <code>DEFAULT_CLEANING_PERIOD</code>
 * right after creation and can be changed with call to <code>setPeriod(long)</code> method.</p>
 * <p>
 * At any time you can ask cleaner to unregister channel. After that it will never be cleaned
 * by engine again and it's promised that all references to the object will be removed.</p>
 * <p>
 * Cleaner uses <code>WorkersManager</code> to maintain asynchronous processing. This means
 * that there will be several working threads in memory, which will be processing scheduled
 * requests for channel cleaning. At any time you can change number of running worker threads
 * starting from 1. If number of threads you require is less than current number of threads
 * extra threads will be marked for termination and will quit right after finishing their current
 * jobs.</p>
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class Cleaner {

  private static final long DEFAULT_CLEANING_PERIOD = 3600000; // 1 hour
  private static final int DEFAULT_WORKER_THREADS = 2;

  private WorkersManager workersManager;
  private Scheduler scheduler;

  private CompositeObserver compositeObserver;
  private CompositeMatcher compositeMatcher;

  private long globalPollPeriod = DEFAULT_CLEANING_PERIOD;

  /**
   * Creates cleaner with default number of worker threads.
   * It's possible to change number of worker threads later
   * using <code>setWorkerThreads(int)</code> method.
   */
  public Cleaner() {
    this(DEFAULT_WORKER_THREADS);
  }

  /**
   * Creates cleaner with specified number of worker threads.
   * It's possible to change number of worker threads later
   * using <code>setWorkerThreads(int)</code> method.
   *
   * @param workerThreads number of worker threads.
   */
  public Cleaner(int workerThreads) {
    // Create composite objects.
    this.compositeMatcher = new CompositeMatcher();
    this.compositeObserver = new CompositeObserver();

    // Initialize workers manager.
    workersManager = new WorkersManager(new CleanerThreadFactory(), workerThreads);

    // Initialize scheduler.
    scheduler = new Scheduler(new SchedulerCallback());
  }

  /**
   * Adds observer to the list of interested parties.
   *
   * @param observer new observer.
   */
  public final void addObserver(CleanerObserverIF observer) {
    compositeObserver.add(observer);
  }

  /**
   * Adds matcher to the list.
   *
   * @param matcher new matcher.
   */
  public final void addMatcher(CleanerMatcherIF matcher) {
    compositeMatcher.add(matcher);
  }

  /**
   * Removes observer from the list.
   *
   * @param observer observer to remove.
   */
  public final void removeObserver(CleanerObserverIF observer) {
    compositeObserver.remove(observer);
  }

  /**
   * Removes matcher from the list.
   *
   * @param matcher matcher to remove.
   */
  public final void removeMatcher(CleanerMatcherIF matcher) {
    compositeMatcher.remove(matcher);
  }

  /**
   * Registers channel for scheduled cleaning with default period (1 hour).
   *
   * @param channel channel to schedule.
   */
  public final void registerChannel(ChannelIF channel) {
    if (channel != null) scheduler.schedule(channel, globalPollPeriod, ChannelRecord.PRIO_NORMAL);
  }

  /**
   * Registers channel for scheduled cleaning with given period.
   *
   * @param channel channel to schedule.
   * @param period  period to use.
   */
  public final void registerChannel(ChannelIF channel, long period) {
    if (channel != null) {
      scheduler.schedule(channel, period, ChannelRecord.PRIO_NORMAL);
    }
  }

  /**
   * Unregisters channel from cleaning.
   *
   * @param channel channel to unregister.
   */
  public final void unregisterChannel(ChannelIF channel) {
    if (channel != null) scheduler.unschedule(channel);
  }

  /**
   * Performs immediate cleaning of the channel and reworks the schedule starting from current time.
   * If channel isn't registered yet it will be registered with normal priority.
   *
   * @param channel channel to update.
   */
  public final void cleanChannel(ChannelIF channel) {
    if (channel != null) scheduler.triggerNow(channel);
  }

  /**
   * Changes the number of worker threads. In case when new number of threads is less than
   * current extra threads will not be terminated right away while they are doing their jobs.
   * Instead of this they will be marked for termination and finish their existence after
   * job is complete.
   *
   * @param count new number of worker threads.
   */
  public final void setWorkerThreads(int count) {
    workersManager.setWorkerThreads(count);
  }

  /**
   * Sets global cleaning period to the specified value. All tasks will be rescheduled.
   *
   * @param period period in millis.
   */
  public final void setPeriod(long period) {
    this.globalPollPeriod = period;
    scheduler.rescheduleAll(period);
  }

  /**
   * Factory of working threads.
   */
  private class CleanerThreadFactory implements WorkerThreadFactoryIF {
    /**
     * Creates new worker thread object.
     *
     * @return worker thread object.
     */
    public WorkerThread create() {
      return new CleanerWorkerThread(compositeObserver, compositeMatcher);
    }
  }

  /**
   * Callback from scheduler.
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
}
