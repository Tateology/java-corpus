/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.util.threadpool;

import org.jboss.logging.Logger;

/**
 * Makes a runnable a task.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public class RunnableTaskWrapper implements TaskWrapper
{
   // Constants -----------------------------------------------------

   /** The log */
   private static final Logger log = Logger.getLogger(RunnableTaskWrapper.class);

   // Attributes ----------------------------------------------------

   /** The runnable */
   private Runnable runnable;
   private boolean started;
   private Thread runThread;
   /** The start timeout */
   private long startTimeout;
   /** The completion timeout */
   private long completionTimeout;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new RunnableTaskWrapper
    *
    * @param runnable the runnable
    * @throws IllegalArgumentException for a null runnable
    */
   public RunnableTaskWrapper(Runnable runnable)
   {
      this(runnable, 0, 0);
   }
   public RunnableTaskWrapper(Runnable runnable, long startTimeout, long completeTimeout)
   {
      if (runnable == null)
         throw new IllegalArgumentException("Null runnable");
      this.runnable = runnable;
      this.startTimeout = startTimeout;
      this.completionTimeout = completeTimeout;
   }

   // Public --------------------------------------------------------

   // TaskWrapper implementation ---------------------------------------

   public int getTaskWaitType()
   {
      return Task.WAIT_NONE;
   }

   public int getTaskPriority()
   {
      return Thread.NORM_PRIORITY;
   }

   public long getTaskStartTimeout()
   {
      return startTimeout;
   }

   public long getTaskCompletionTimeout()
   {
      return completionTimeout;
   }

   public void acceptTask()
   {
      // Nothing to do
   }

   public void rejectTask(RuntimeException t)
   {
      throw t;
   }

   public void stopTask()
   {
      boolean trace = log.isTraceEnabled();
      // Interrupt the run thread if its not null
      if( runThread != null && runThread.isInterrupted() == false )
      {
         runThread.interrupt();
         if( trace )
            log.trace("stopTask, interrupted thread="+runThread);
      }
      else if( runThread != null )
      {
         /* If the thread has not been returned after being interrupted, then
         use the deprecated stop method to try to force the thread abort.
         */
         runThread.stop();
         if( trace )
            log.trace("stopTask, stopped thread="+runThread);
      }
   }

   public void waitForTask()
   {
      // Nothing to do
   }

   public boolean isComplete()
   {
      return started == true && runThread == null;
   }

   public void run()
   {
      boolean trace = log.isTraceEnabled();
      try
      {
         if( trace )
            log.trace("Begin run, wrapper="+this);
         runThread = Thread.currentThread();
         started = true;
         runnable.run();
         runThread = null;
         if( trace )
            log.trace("End run, wrapper="+this);
      }
      catch (Throwable t)
      {
         log.warn("Unhandled throwable for runnable: " + runnable, t);
      }
   }
}
