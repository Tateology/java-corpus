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

package org.jboss.test.util.test.threadpool;

import junit.framework.TestCase;
import org.jboss.util.loading.ClassLoaderSource;
import org.jboss.util.threadpool.BasicThreadPool;

/**
 * Tests thread context classloader management by BasicThreadPool.
 * 
 * @author Brian Stansberry
 */
public class BasicThreadPoolTCCLTestCase extends TestCase
{
   protected ClassLoader origCl;
   
   /**
    * @param name
    */
   public BasicThreadPoolTCCLTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      origCl = Thread.currentThread().getContextClassLoader();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(origCl);
      }
   }
   
   public void testNullClassLoaderSource() throws Exception
   {
      ClassLoader existing = origCl == null ? getExtendedClassLoader() : origCl;
      Thread.currentThread().setContextClassLoader(existing);
      runClassLoaderSourceTest(null, existing);
      
      Thread.currentThread().setContextClassLoader(null);
      runClassLoaderSourceTest(null, null);      
   }
   
   public void testConfiguredClassLoaderSource() throws Exception
   {
      ClassLoaderSource source = new TestClassLoaderSource();
      runClassLoaderSourceTest(source, source.getClassLoader());
   }
   
   private void runClassLoaderSourceTest(ClassLoaderSource source, ClassLoader expected) throws Exception
   {
      ThreadGroup group = new ThreadGroup("Test");
      BasicThreadPool pool = new BasicThreadPool("Test", group);
      // Only one thread so we can use it twice to confirm 
      // it gets cleaned after the first task
      pool.setMaximumPoolSize(1);
      pool.setClassLoaderSource(source);
      
      Task task = new Task();
      pool.run(task);
      
      // Wait for task completion
      synchronized (task)
      {
         if (!task.done)
         {
            task.wait();
         }
      }
      
//      ClassLoader expected = source == null ? null : source.getClassLoader();
      assertEquals(expected, task.incomingCl);
      
      Task task2 = new Task();
      pool.run(task2);
      
      // Wait for task completion
      synchronized (task2)
      {
         if (!task2.done)
         {
            task2.wait();
         }
      }
      
      assertSame(expected, task.incomingCl);
      
      assertEquals("Pool size limited", 1, pool.getPoolSize());
   }
   
   private ClassLoader getExtendedClassLoader()
   {
      return new ExtendedClassLoader(origCl);
   }
   
   private class ExtendedClassLoader extends ClassLoader
   {
      ExtendedClassLoader(ClassLoader parent)
      {
         super(parent);
      }
   }
   
   private class Task implements Runnable
   {
      private ClassLoader incomingCl;
      private boolean done;
      
      public void run()
      {
         incomingCl = Thread.currentThread().getContextClassLoader();
         
         // Deliberately pollute the TCCL
         ClassLoader leakedCl = BasicThreadPoolTCCLTestCase.this.getExtendedClassLoader();
         Thread.currentThread().setContextClassLoader(leakedCl);
         
         // Wake up the test thread
         synchronized (this)
         {
            done = true;         
            notifyAll();
         }
      }    
   }
   
   private class TestClassLoaderSource implements ClassLoaderSource
   {
      private ClassLoader cl;
      
      TestClassLoaderSource()
      {
         cl = BasicThreadPoolTCCLTestCase.this.getExtendedClassLoader();
      }

      public ClassLoader getClassLoader()
      {
         return cl;
      }
   }

}
