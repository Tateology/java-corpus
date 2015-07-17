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
 * A wrapper for the task.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public class BasicTaskWrapper implements TaskWrapper
{
   /** The log */
   private static final Logger log = Logger.getLogger(BasicTaskWrapper.class);

   /** The task has not been accepted */
   public static final int TASK_NOT_ACCEPTED = 0;

   /** The task has been accepted */
   public static final int TASK_ACCEPTED = 1;

   /** The task has been started */
   public static final int TASK_STARTED = 2;

   /** The task has completed */
   public static final int TASK_COMPLETED = 3;

   /** The task was rejected */
   public static final int TASK_REJECTED = -1;

   /** The task has been stopped */
   public static final int TASK_STOPPED = -2;

   /** The state of the task */
   private int state = TASK_NOT_ACCEPTED;

   /** The state lock */
   private Object stateLock = new Object();

   /** The task */
   private Task task;

   /** The task as a string */
   private String taskString;

   /** The start time */
   private long startTime;

   /** The start timeout */
   private long startTimeout;

   /** The completion timeout */
   private long completionTimeout;

   /** The priority */
   private int priority;

   /** The wait type */
   private int waitType;

   /** The thread */
   private Thread runThread;

   /**
    * Create a task wrapper without a task
    */
   protected BasicTaskWrapper()
   {
   }

   /**
    * Create a new task wrapper
    *
    * @param task the task
    * @throws IllegalArgumentException for a null task
    */
   public BasicTaskWrapper(Task task)
   {
      setTask(task);
   }

   public int getTaskWaitType()
   {
      return waitType;
   }

   public int getTaskPriority()
   {
      return priority;
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
      synchronized (stateLock)
      {
         // Not in a valid state
         if (state != TASK_NOT_ACCEPTED)
            return;
      }

      // Accept the task
      if (taskAccepted())
         state = TASK_ACCEPTED;
      else
         state = TASK_REJECTED;

      // Notify the waiting task
      synchronized (stateLock)
      {
         stateLock.notifyAll();
      }
   }

   public void rejectTask(RuntimeException e)
   {
      synchronized (stateLock)
      {
         state = TASK_REJECTED;
         stateLock.notifyAll();
      }
      taskRejected(e);
   }

   public boolean isComplete()
   {
      return state == TASK_COMPLETED;
   }

   public void stopTask()
   {
      boolean started;
      synchronized (stateLock)
      {
         started = (state == TASK_STARTED);
         state = TASK_STOPPED;
      }
      if (started)
      {
         // Interrupt the run thread if its not null
         if( runThread != null )
         {
            runThread.interrupt();
         }
         taskStop();
      }
      else if( runThread != null && runThread.isInterrupted() )
      {
         /* If the thread has not been returned after being interrupted, then
         use the deprecated stop method to try to force the thread abort.
         */
         runThread.stop();
      }
   }

   public void waitForTask()
   {
      switch (waitType)
      {
         case Task.WAIT_FOR_START:
         {
            boolean interrupted = false;
            synchronized (stateLock)
            {
               while (state == TASK_NOT_ACCEPTED || state == TASK_ACCEPTED)
               {
                  try
                  {
                     stateLock.wait();
                  }
                  catch (InterruptedException e)
                  {
                     interrupted = true;
                  }
               }
               if (interrupted)
                  Thread.currentThread().interrupt();
               return;
            }
         }
         default:
         {
            return;
         }
      }
   }

   /**
    * Called by the thread pool executor
    */ 
   public void run()
   {
      // Get the execution thread
      this.runThread = Thread.currentThread();

      // Check for a start timeout
      long runTime = getElapsedTime();
      if (startTimeout > 0l && runTime >= startTimeout)
      {
         taskRejected(new StartTimeoutException("Start Timeout exceeded for task " + taskString));
         return;
      }

      // We are about to start, check for a stop
      boolean stopped = false;
      synchronized (stateLock)
      {
         if (state == TASK_STOPPED)
         {
            stopped = true;
         }
         else
         {
            state = TASK_STARTED;
            taskStarted();
            if (waitType == Task.WAIT_FOR_START)
               stateLock.notifyAll();
         }
      }
      if (stopped)
      {
         taskRejected(new TaskStoppedException("Task stopped for task " + taskString));
         return;
      }

      // Run the task
      Throwable throwable = null;
      try
      {
         task.execute();
      }
      catch (Throwable t)
      {
         throwable = t;
      }

      // It is complete
      taskCompleted(throwable);

      // We are completed
      synchronized (stateLock)
      {
         state = TASK_COMPLETED;
         if (waitType == Task.WAIT_FOR_COMPLETE)
            stateLock.notifyAll();
      }
   }

   /**
    * Set thetask for this wrapper
    *
    * @param task the task
    */
   protected void setTask(Task task)
   {
      if (task == null)
         throw new IllegalArgumentException("Null task");
      this.task = task;
      this.taskString = task.toString();
      this.startTime = System.currentTimeMillis();
      this.waitType = task.getWaitType();
      this.priority = task.getPriority();
      this.startTimeout = task.getStartTimeout();
      this.completionTimeout = task.getCompletionTimeout();
   }

   /**
    * Notify the task it has been accepted
    *
    * @return true when the notification succeeds, false otherwise
    */
   protected boolean taskAccepted()
   {
      try
      {
         task.accepted(getElapsedTime());
         return true;
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error during 'accepted' for task: " + taskString, t);
         return false;
      }
   }

   /**
    * Notify the task it has been rejected
    *
    * @param e any error associated with the rejection
    * @return true when the notification succeeds, false otherwise
    */
   protected boolean taskRejected(RuntimeException e)
   {
      try
      {
         task.rejected(getElapsedTime(), e);
         return true;
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error during 'rejected' for task: " + taskString, t);
         if (e != null)
            log.warn("Original reason for rejection of task: " + taskString, e);
         return false;
      }
   }

   /**
    * Notify the task it has started
    *
    * @return true when the notification succeeds, false otherwise
    */
   protected boolean taskStarted()
   {
      try
      {
         task.started(getElapsedTime());
         return true;
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error during 'started' for task: " + taskString, t);
         return false;
      }
   }

   /**
    * Notify the task it has completed
    *
    * @param throwable any throwable associated with the completion
    * @return true when the notification succeeds, false otherwise
    */
   protected boolean taskCompleted(Throwable throwable)
   {
      try
      {
         task.completed(getElapsedTime(), throwable);
         return true;
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error during 'completed' for task: " + taskString, t);
         if (throwable != null)
            log.warn("Original error during 'run' for task: " + taskString, throwable);
         return false;
      }
   }

   /**
    * Stop the task
    *
    * @return true when the notification succeeds, false otherwise
    */
   protected boolean taskStop()
   {
      try
      {
         task.stop();
         return true;
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error during 'stop' for task: " + taskString, t);
         return false;
      }
   }

   /**
    * Calculate the elapsed time since the task was started
    *
    * @return the elapsed time in millis
    */
   protected long getElapsedTime()
   {
      return System.currentTimeMillis() - startTime;
   }
   
   /**
    * Get the state as a string
    * 
    * @return the state string
    */
   protected String getStateString()
   {
      switch (state)
      {
         case TASK_NOT_ACCEPTED:
            return "NOT_ACCEPTED";
         case TASK_REJECTED:
            return "REJECTED";
         case TASK_ACCEPTED:
            return "ACCEPTED";
         case TASK_STARTED:
            return "STARTED";
         case TASK_STOPPED:
            return "STOPPED";
         case TASK_COMPLETED:
            return "COMPLETED";
         default:
            return "???";
      }
   }
}
