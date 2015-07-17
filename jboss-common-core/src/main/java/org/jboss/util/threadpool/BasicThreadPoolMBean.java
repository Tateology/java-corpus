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

import org.jboss.util.loading.ClassLoaderSource;

/**
 * Management interface for the thread pool.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public interface BasicThreadPoolMBean extends ThreadPoolMBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Get the current queue size
   *
    * @return the queue size
    */
   int getQueueSize();

   /**
    * Get the maximum queue size
    *
    * @return the maximum queue size
    */
   int getMaximumQueueSize();

   /**
    * Set the maximum queue size
    *
    * @param size the new maximum queue size
    */
   void setMaximumQueueSize(int size);

   /**
    * @return the blocking mode
    */
   BlockingMode getBlockingMode();
   
   /** Set the behavior of the pool when a task is added and the queue is full.
    * The mode string indicates one of the following modes:
    * abort - a RuntimeException is thrown
    * run - the calling thread executes the task
    * wait - the calling thread blocks until the queue has room
    * discard - the task is silently discarded without being run
    * discardOldest - check to see if a task is about to complete and enque
    *    the new task if possible, else run the task in the calling thread
    * 
    * @param mode one of run, wait, discard, discardOldest or abort without
    *    regard to case.
    */ 
   void setBlockingMode(BlockingMode mode);

   /**
    * Retrieve the thread group name
    *
    * @return the thread group name
    */
   String getThreadGroupName();

   /**
    * Set the thread group name
    *
    * @param threadGroupName - the thread group name
    */
   void setThreadGroupName(String threadGroupName);

   /**
    * Get the keep alive time
    *
    * @return the keep alive time
    */
   long getKeepAliveTime();

   /**
    * Set the keep alive time
    *
    * @param time the keep alive time
    */
   void setKeepAliveTime(long time);

   /** 
    * Gets the source of the classloader that will be set as the 
    * {@link Thread#getContextClassLoader() thread context classloader}
    * for pool threads.
    * 
    * @return the {@link ClassLoaderSource}. May return <code>null</code>.
    */
   ClassLoaderSource getClassLoaderSource();

   /** 
    * Sets the source of the classloader that will be set as the 
    * {@link Thread#getContextClassLoader() thread context classloader}
    * for pool threads. If set, whenever any new pool thread is created, it's
    * context classloader will be set to the loader provided by this source.
    * Further, when any thread is returned to the pool, its context classloader
    * will be reset to the loader provided by this source.
    * <p> 
    * If set to <code>null</code> (the default), the pool will not attempt to 
    * manage the context classloader for pool threads; instead a newly created 
    * pool thread will inherit its context classloader from whatever thread 
    * triggered the addition to the pool.  A thread returned to the pool will
    * not have its context classloader changed from whatever it was.
    * </p>
    * 
    * @param classLoaderSource the {@link ClassLoaderSource}. May be <code>null</code>.
    */
   void setClassLoaderSource(ClassLoaderSource classLoaderSource);

   // Inner classes -------------------------------------------------
}
