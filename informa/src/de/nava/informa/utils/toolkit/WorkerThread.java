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
// $Id: WorkerThread.java 770 2005-09-24 22:35:15Z niko_schmuck $
//

package de.nava.informa.utils.toolkit;

import org.apache.commons.collections.Buffer;

/**
 * Abstract worker thread which is driven by <code>WorkersManager</code>. Worker thread runs
 * until <code>terminate</code> property is set to true. All of the threads are run in
 * daemon mode and do not prevent application from sudden quit.
 * <p>
 * In order to do something useful with channel this class should be extended with implementation
 * of <code>process()</code> method. This method is invoked each time the thread receives new
 * processing task. Current class takes care of getting new tasks, marking busy state and
 * other necessary tasks.</p>
 * <p>
 * After the task is finished worker checks <code>JobSourceIF</code> object (if specified)
 * to get new assignments and it there's no job goes to sleep for a <code>IDLE_SLEEP_TIME</code>
 * milliseconds. When manager finds new task it immediately awakens the thread. If the thread
 * doesn't receive new assignments for an <code>IDLE_SLEEP_TIME</code> it checks the
 * <code>JobSourceIF</code> object again by itself to find new task. This is done for greater
 * safety reasons to avoid situations when the queue is full of tasks and workers are in endless
 * sleep.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public abstract class WorkerThread extends Thread {

  private boolean       terminate;

  private ChannelRecord channelRecord;
  private Buffer        tasksQueue;

  /**
   * Creates named worker thread.
   *
   * @param name        name of the thread.
   */
  public WorkerThread(String name) {
    super(name);
    setDaemon(true);

    terminate = false;
  }

  /**
   * Marks this thread for termination on job completion.
   */
  public final void terminate() {
    this.terminate = true;
  }

  /**
   * Returns channel which is currently in processing.
   *
   * @return channel.
   */
  public final ChannelRecord getChannelInProcess() {
    return channelRecord;
  }

  /**
   * If this thread was constructed using a separate
   * <code>Runnable</code> run object, then that
   * <code>Runnable</code> object's <code>run</code> method is called;
   * otherwise, this method does nothing and returns.
   * <p/>
   * Subclasses of <code>Thread</code> should override this method.
   *
   * @see Thread#start()
   * @see Thread#Thread(ThreadGroup, Runnable, String)
   * @see Runnable#run()
   */
  public final void run() {
    // Loop until termination.
    while (!terminate) {
      channelRecord = null;
      channelRecord = (ChannelRecord)tasksQueue.remove();
      processRecord(channelRecord);
    }
  }

  /**
   * Processes record.
   *
   * @param record record to process.
   */
  protected abstract void processRecord(ChannelRecord record);

  /**
   * Sets the queue of tasks.
   *
   * @param queue blocking queue of tasks.
   */
  public void setQueue(Buffer queue) {
    this.tasksQueue = queue;
  }
}
