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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** A pooled executor where the minimum pool size threads are kept alive. This
is needed in order for the waitWhenBlocked option to work because of a
race condition inside the Executor. The race condition goes something like:

RT - Requesting Thread wanting to use the pool
LT - Last Thread in the pool

RT: Check there are enough free threads to process,
   yes LT is there, so no need to create a new thread.
LT: Times out on the keep alive, LT is destroyed.
RT: Try to execute, blocks because there are no available threads.
   In fact, the pool is now empty which the executor mistakenly
   inteprets as all of them being in use.

Doug Lea says he isn't going to fix. In fact, the version in j2se 
1.5 doesn't have this option. In order for this to work, the min pool
size must be > 0.

@author Scott.Stark@jboss.org
@author adrian@jboss.org
@version $Revision$
 */
@SuppressWarnings("unchecked")
public class MinPooledExecutor extends ThreadPoolExecutor
{
   // Constants -----------------------------------------------------


   // Attributes ----------------------------------------------------

   /** The number of threads to keep alive threads */
   protected int keepAliveSize;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new executor
    * 
    * @param poolSize the maximum pool size
    */
   public MinPooledExecutor(int poolSize)
   {
      super(poolSize, 2*poolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(1024));
   }

   /**
    * Construct a new executor
    * 
    * @param queue the queue for any requests
    * @param poolSize the maximum pool size
    */
   public MinPooledExecutor(BlockingQueue queue, int poolSize)
   {
      super(poolSize, 2*poolSize, 60, TimeUnit.SECONDS, queue);
   }

   // Public --------------------------------------------------------

   /**
    * @return the number of threads to keep alive
    */
   public int getKeepAliveSize()
   {
      return keepAliveSize;
   }

   /**
    * @param keepAliveSize the number of threads to keep alive
    */
   public void setKeepAliveSize(int keepAliveSize)
   {
      this.keepAliveSize = keepAliveSize;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
