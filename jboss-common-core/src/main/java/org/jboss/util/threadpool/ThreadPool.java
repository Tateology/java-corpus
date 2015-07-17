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
 * A thread pool.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public interface ThreadPool
{
   /**
    * Stop the pool
    *
    * @param immediate whether to shutdown immediately
    */
   public void stop(boolean immediate);

   /**
    * Wait on the queued tasks to complete.
    * This can only be called after stop.
    * 
    * @throws InterruptedException for any iterruption error
    */ 
   public void waitForTasks() throws InterruptedException;

   /**
    * Wait on the queued tasks to complete upto maxWaitTime milliseconds.
    * This can only be called after stop.
    * 
    * @param maxWaitTime the max wait time
    * @throws InterruptedException for any interruption error
    */ 
   public void waitForTasks(long maxWaitTime) throws InterruptedException;

   /**
    * Run a task wrapper
    *
    * @param wrapper the task wrapper
    */
   public void runTaskWrapper(TaskWrapper wrapper);

   /**
    * Run a task
    *
    * @param task the task
    * @throws IllegalArgumentException for a null task
    */
   public void runTask(Task task);

   /**
    * Run a runnable
    *
    * @param runnable the runnable
    * @throws IllegalArgumentException for a null runnable
    */
   public void run(Runnable runnable);

   /**
    * Run runnable with start and complete time out set explicitely.
    * 
    * @param runnable the runnable
    * @param startTimeout the start timeout
    * @param completeTimeout the complete timeout
    * @throws IllegalArgumentException for a null runnable
    */
   public void run(Runnable runnable, long startTimeout, long completeTimeout);
}
