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
import org.jboss.util.threadpool.BlockingMode;
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutFactory;
import org.jboss.util.timeout.TimeoutTarget;

/**
 * Unit tests for TimeoutFactory class.
 *
 * @author  <a href="mailto:genman@noderunner.net">Elias Ross</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision$
 */
public class TimeoutFactoryTestCase extends TestCase
{
   public TimeoutFactoryTestCase(String name)
   {
      super(name);
   }

   CountDownLatch count;

   public void testBlocking() throws Exception
   {
      final int times = 5000;
      count = new CountDownLatch(times);
      TT tt = new TT();
      for (int i = 0; i < times; i++)
      {
         TimeoutFactory.createTimeout(0, tt);
      }
      count.await(60, TimeUnit.SECONDS);
      assertEquals(0, count.getCount());
   }
   public void testDefaultCtr() throws Exception
   {
      final int times = 5000;
      count = new CountDownLatch(times);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (Runnable)tt);
      }
      count.await(60, TimeUnit.SECONDS);
      assertEquals(0, count.getCount());
   }

   public void testConsecutiveTimeouts() throws Exception
   {
      final int times = 1000;      
      count = new CountDownLatch(times);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long now = System.currentTimeMillis();
      for (int i = 0; i < 10; i++)
      {
         for (int j = 0; j < 100; j++)
         {
            tf.schedule(now + i*50, (TimeoutTarget)tt);
         }
      }
      count.await(10, TimeUnit.SECONDS);
      assertEquals(0, count.getCount());
   }
   
   public void testCancel() throws Exception
   {
      final int times = 100;
      count = new CountDownLatch(times);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long at = System.currentTimeMillis() + 300;
      for (int i = 0; i < times; i++)
      {
         Timeout t = tf.schedule(at, (TimeoutTarget)tt);
         t.cancel();
      }
      count.await(5, TimeUnit.SECONDS);
      assertEquals(times, count.getCount());
   }

   public void testCancelFactory() throws Exception
   {
      final int times = 100;
      count = new CountDownLatch(times);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long at = System.currentTimeMillis() + 300;
      for (int i = 0; i < times; i++)
      {
         tf.schedule(at, (TimeoutTarget)tt);
      }
      tf.cancel();
      count.await(5, TimeUnit.SECONDS);
      assertEquals(times, count.getCount());
   }

   public void testBlockingSmallThreadPool() throws Exception
   {
      final int times = 100;
      count = new CountDownLatch(times);
      BasicThreadPool tp = new BasicThreadPool();
      tp.setMaximumQueueSize(1);
      tp.setMaximumPoolSize(1);
      tp.setBlockingMode(BlockingMode.RUN);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory(tp);
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      count.await(10, TimeUnit.SECONDS);
      assertEquals(0, count.getCount());
   }
   
   public void testAbortingSmallThreadPool() throws Exception
   {
      final int times = 50;
      count = new CountDownLatch(times);
      BasicThreadPool tp = new BasicThreadPool();
      tp.setMaximumQueueSize(1);
      tp.setMaximumPoolSize(1);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory(tp);
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      count.await(5, TimeUnit.SECONDS);
      assertTrue("Executed " + count.getCount() + " < scheduled " + times, count.getCount() < times);
   }
   
   public void testFailedTarget() throws Exception
   {
      final int times = 50;
      count = new CountDownLatch(times);
      TimeoutFactory tf = new TimeoutFactory();           
      TT tt = new TT();
      tt.fail = true;
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      Thread.sleep(500);
      assertEquals(times, count.getCount());
   }

   class TT implements TimeoutTarget, Runnable
   {

      boolean fail;

      public void timedOut(Timeout timeout)
      {
         assertTrue(timeout != null);
         run();
      }

      public void run()
      {
         if (fail)
            throw new Error("Fail");
         
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException e)
         {
         }
         count.countDown();
      }
   }
}
