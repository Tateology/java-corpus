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
 * A task wrapper for a thread pool.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public interface TaskWrapper extends Runnable
{
   /**
    * Get the type of wait
    *
    * @return the wait type
    */
   int getTaskWaitType();

   /**
    * The priority of the task
    *
    * @return the task priority
    */
   int getTaskPriority();

   /**
    * The time before the task must be accepted
    *
    * @return the start timeout
    */
   long getTaskStartTimeout();

   /**
    * The time before the task must be completed
    *
    * @return the completion timeout
    */
   long getTaskCompletionTimeout();

   /**
    * Wait according the wait type
    */
   void waitForTask();

   /**
    * Invoked by the threadpool when it wants to stop the task
    */
   void stopTask();

   /**
    * The task has been accepted
    *
    */
   void acceptTask();

   /**
    * The task has been rejected
    *
    * @param e any error associated with the rejection
    */
   void rejectTask(RuntimeException e);

   /**
    * Is the task complete.
    *
    * @return true if compelet, false otherwise
    */
   boolean isComplete();
}
