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
package org.jboss.test.util.test.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jboss.util.threadpool.BasicThreadPool;

/**
 * Tests of thread pool with Tasks added to the pool
 *
 * @see org.jboss.util.threadpool.ThreadPool
 * @author <a href="adrian@jboss.org">Adrian.Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 2787 $
 */
public class ThreadPoolInterruptedThreadUnitTestCase extends TestCase
{
   public ThreadPoolInterruptedThreadUnitTestCase(String name)
   {
      super(name);
   }

   public static class TestRunnable implements Runnable
   {
      public CountDownLatch latch = new CountDownLatch(1);
      public void run()
      {
         latch.countDown();
      }
   }
   
   public void testInterruptedExecute() throws Exception
   {
      BasicThreadPool pool = new BasicThreadPool();
      TestRunnable runnable = new TestRunnable();
      
      Thread.currentThread().interrupt();
      try
      {
         pool.run(runnable);
      }
      finally
      {
         assertTrue(Thread.interrupted());
      }
      assertTrue(runnable.latch.await(10, TimeUnit.SECONDS));
   }
}
