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

/**
 * A task for a thread pool.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public interface Task
{
   // Constants -----------------------------------------------------

   /** Don't wait for task */
   static final int WAIT_NONE = 0;

   /** Synchronized start, wait for task to start */
   static final int WAIT_FOR_START = 1;

   /** Synchronized task, wait for task to complete */
   static final int WAIT_FOR_COMPLETE = 2;

   // Public --------------------------------------------------------

   /**
    * Get the type of wait
    *
    * @return the wait type
    */
   int getWaitType();

   /**
    * The priority of the task
    *
    * @return the task priority
    */
   int getPriority();

   /**
    * The time before the task must be accepted
    *
    * @return the start timeout
    */
   long getStartTimeout();

   /**
    * The time before the task must be completed
    *
    * @return the completion timeout
    */
   long getCompletionTimeout();

   /**
    * Execute the task
    */
   void execute();

   /**
    * Invoked by the threadpool when it wants to stop the task
    */
   void stop();

   /**
    * The task has been accepted
    *
    * @param time the time taken to accept the task
    */
   void accepted(long time);

   /**
    * The task has been rejected
    *
    * @param time the time taken to reject the task
    * @param t any error associated with the rejection
    */
   void rejected(long time, Throwable t);

   /**
    * The task has been started
    *
    * @param time the time taken to start the task
    */
   void started(long time);

   /**
    * The task has been completed
    *
    * @param time the time taken to reject the task
    * @param t any error associated with the completion
    */
   void completed(long time, Throwable t);

   // Inner classes -------------------------------------------------
}
