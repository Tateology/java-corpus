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
// $Id: WorkersManager.java 830 2007-01-06 22:18:13Z niko_schmuck $
//

package de.nava.informa.utils.toolkit;

import de.nava.informa.utils.poller.PriorityComparator;
import org.apache.commons.collections.BinaryHeap;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class manages worker threads. Its main responsibility is to create / remove worker threads
 * and distribute tasks among them. This is an effort to hide complexity of threading logic from
 * the rest of toolkit. Worker manager receives processing requests from external tools and assigns
 * them to free worker threads. If there's no free worker thread left manager puts item in queue.
 * When worker thread finishes its job it asks for next job using <code>JobSource</code> callback
 * interface. If there is at least one job in queue manager assigns it to worker. If not then
 * worker goes to rest until manager has something to assign.
 * <p>
 * Worker manager uses instance of <code>WorkerThreadFactoryIF</code> specified on creation
 * to create new worker threads. Client application should use it to create and initialize
 * task-specific workers, which will be started as independent threads.</p>
 * <p>
 * It's possible to tune number of threads working at a given time using
 * <code>setWorkerThreads(int)</code> method call. Right after the number of threads will change
 * manager will stop unnecessary threads (only after running task completion) or create
 * new worker threads.</p>
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class WorkersManager {
  private static final int DEFAULT_WORKER_THREADS = 5;
  private static final int DEFAULT_QUEUE_LIMIT = 25;

  private List<WorkerThread> workers = new ArrayList<WorkerThread>();

  // This code intentionally uses deprecated classes! Old commons library was 150 Kb and
  // current is 550 Kb. Many applications (including BlogBridge) will be enforced to replace
  // small lib with giantic new one if we start using new classes instead these.
  // When support of these will be finished, please replace:
  //    PriorityQueue             -> Buffer
  //    SynchronizedPriorityQueue -> SynchronizedBuffer
  //    BinaryHeap                -> PriorityBuffer
  private Buffer queue =
    BufferUtils.blockingBuffer(new BinaryHeap(DEFAULT_QUEUE_LIMIT, new PriorityComparator()));

  private WorkerThreadFactoryIF workerThreadsFactory;

  /**
   * Creates worker manager with default number of worker threads.
   *
   * @param factory worker threads factory.
   */
  public WorkersManager(WorkerThreadFactoryIF factory) {
    this(factory, DEFAULT_WORKER_THREADS);
  }

  /**
   * Creates worker manager.
   *
   * @param factory       worker threads factory.
   * @param workerThreads number of worker threads.
   */
  public WorkersManager(WorkerThreadFactoryIF factory, int workerThreads) {
    this.workerThreadsFactory = factory;

    // Protect ourselves from incorrect parameters.
    if (workerThreads <= 0) {
      workerThreads = DEFAULT_WORKER_THREADS;
    }

    setWorkerThreads(workerThreads);
  }

  /**
   * Changes number of worker threads.
   *
   * @param count new number of worker threads.
   */
  public final void setWorkerThreads(int count) {
    synchronized (workers) {
      // If we have more than specified number of working threads then terminate unwanted.
      int curWorkerThreads = workers.size();
      for (int i = curWorkerThreads - 1; i >= count; i--) {
        final WorkerThread worker = workers.get(i);
        worker.terminate();
        workers.remove(worker);
      }

      // If we have less than specified number of thread then add some.
      curWorkerThreads = workers.size();
      for (int i = curWorkerThreads; i < count; i++) {
        // Create new worker using custom factory.
        final WorkerThread worker = workerThreadsFactory.create();
        worker.setQueue(queue);

        // Add worker to the list and start.
        workers.add(worker);
        worker.start();
      }
    }
  }

  /**
   * Terminates all worker threads.
   */
  public final void terminateAll() {
    synchronized (workers) {
      int count = workers.size();
      for (int i = count - 1; i >= 0; i--) {
        workers.get(i).terminate();
        workers.remove(i);
      }
    }
  }

  /**
   * Put the record in processing.
   *
   * @param record record to process.
   */
  public final void process(ChannelRecord record) {
    if (!isInProcess(record)) {
      putRecordInQueue(record);
    }
  }

  /**
   * Checks if the channel is not currently in processing.
   *
   * @param record channel record.
   * @return TRUE if is in processing.
   */
  private boolean isInProcess(ChannelRecord record) {
    boolean found = false;

    synchronized (workers) {
      Iterator<WorkerThread> i = workers.iterator();
      while (!found && i.hasNext()) {
        WorkerThread worker = i.next();
        found = worker.getChannelInProcess() == record;
      }
    }

    return found;
  }

  /**
   * Put task in processing queue. During putting the task algorythm analyzes the priority of
   * insertion and evaluates index of new item in queue.
   *
   * @param record record to put in queue.
   */
  @SuppressWarnings("unchecked")
  private void putRecordInQueue(ChannelRecord record) {
    queue.add(record);
  }
}
