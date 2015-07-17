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
package org.jboss.util.timeout;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.util.NestedRuntimeException;
import org.jboss.util.ThrowableHandler;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BlockingMode;
import org.jboss.util.threadpool.ThreadPool;

/**
 * The timeout factory.
 *
 * @author <a href="osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="genman@maison-otaku.net">Elias Ross</a>  
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class TimeoutFactory
{
   /** The priority queue property */
   private static final String priorityQueueProperty = TimeoutPriorityQueue.class.getName();

   /** The priority queue default */
   private static final String priorityQueueName = TimeoutPriorityQueueImpl.class.getName();
   
   /** Our singleton instance */
   private static TimeoutFactory singleton;
   
   /** Number of TimeoutFactories created */
   private static int timeoutFactoriesCount = 0;
   
   /** The priority queue class */
   private static Class<?> priorityQueueClass;
   
   /** The default threadpool used to execute timeouts */
   private static BasicThreadPool DEFAULT_TP = new BasicThreadPool("Timeouts");
   static
   {
      DEFAULT_TP.setBlockingMode(BlockingMode.RUN);

      String priorityQueueClassName = priorityQueueName;
      ClassLoader cl = TimeoutFactory.class.getClassLoader();
      try
      {
         priorityQueueClassName = System.getProperty(priorityQueueProperty, priorityQueueName);
         cl = Thread.currentThread().getContextClassLoader();
      }
      catch (Exception ignored)
      {
      }
      
      try
      {
         priorityQueueClass = cl.loadClass(priorityQueueClassName);
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e.toString(), e);
      }
   }

   /** Used for graceful exiting */
   private AtomicBoolean cancelled = new AtomicBoolean(false);
   
   /** The daemon thread that dequeues timeouts tasks and issues
       them for execution to the thread pool */ 
   private Thread workerThread;
   
   /** Per TimeoutFactory thread pool used to execute timeouts */
   private ThreadPool threadPool;
   
   /** The priority queue */
   private TimeoutPriorityQueue queue;

   public synchronized static TimeoutFactory getSingleton()
   {
      if (singleton == null)
      {
         singleton = new TimeoutFactory(DEFAULT_TP);
      }
      return singleton;
   }
   
   /**
    *  Schedules a new timeout using the singleton TimeoutFactory
    * @param time 
    * @param target 
    * @return  the timeout
    */
   static public Timeout createTimeout(long time, TimeoutTarget target)
   {
      return getSingleton().schedule(time, target);
   }
   
   /**
    * Constructs a new TimeoutFactory that uses the provided ThreadPool
    * @param threadPool 
    */
   public TimeoutFactory(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
      try
      {
         queue = (TimeoutPriorityQueue) priorityQueueClass.newInstance(); 
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot instantiate " + priorityQueueClass,e);
      }
      
      // setup the workerThread
      workerThread = new Thread("TimeoutFactory-" + timeoutFactoriesCount++)
      {
         public void run()
         {
            doWork();
         }
      };
      workerThread.setDaemon(true);
      workerThread.start();
   }
   
   /**
    * Constructs a new TimeoutFactory that uses the default thread pool
    */
   public TimeoutFactory()
   {
      this(DEFAULT_TP);
   }
   
   /**
    * Schedules a new timeout.
    * 
    * @param time absolute time
    * @param target target to fire
    * @return the timeout
    */
   public Timeout schedule(long time, TimeoutTarget target)
   {
      if (cancelled.get())
         throw new IllegalStateException("TimeoutFactory has been cancelled");      
      if (time < 0)
         throw new IllegalArgumentException("Negative time");
      if (target == null)
         throw new IllegalArgumentException("Null timeout target");

      return queue.offer(time, target);
   }
   
   /**
    * Schedules a new timeout.
    * 
    * @param time absolute time
    * @param run runnable to run
    * @return the timeout
    */
   public Timeout schedule(long time, Runnable run)
   {
      return schedule(time, new TimeoutTargetImpl(run));
   }
   
   /**
    * Cancels all submitted tasks, stops the worker
    * thread and clean-ups everything except for the
    * thread pool. Scheduling new timeouts after cancel
    * is called results in a IllegalStateException.
    */
   public void cancel()
   {
      // obviously the singleton TimeoutFactory cannot
      // be cancelled since its reference is not accessible
      
      // let the worker thread cleanup
      if (cancelled.getAndSet(true) == false)
      {
         // Cancel the priority queue
         queue.cancel();
      }
   }
   
   /**
    * @return true if the TimeoutFactory has been cancelled,
    * false if it is operational (i.e. accepts timeout schedules).
    */
   public boolean isCancelled()
   {
      return cancelled.get();
   }

   /**
    *  Timeout worker method.
    */
   private void doWork()
   {
      while (cancelled.get() == false)
      {
         TimeoutExt work = queue.take();
         // Do work, if any
         if (work != null)
         {
            // Wrap the TimeoutExt with a runnable that invokes the target callback
            TimeoutWorker worker = new TimeoutWorker(work);
            try
            {
               threadPool.run(worker);
            }
            catch (Throwable t)
            {
               // protect the worker thread from pool enqueue errors
               ThrowableHandler.add(ThrowableHandler.Type.ERROR, t);
            }
            synchronized (work)
            {
               work.done();
            }            
         }
      }
      
      // TimeoutFactory was cancelled
      queue.cancel();
   }
   
   /**
    *  A runnable that fires the timeout callback
    */
   private static class TimeoutWorker implements Runnable
   {
      private TimeoutExt work;

      /**
       *  Create a new instance.
       *
       *  @param work The timeout that should be fired.
       */
      TimeoutWorker(TimeoutExt work)
      {
         this.work = work;
      }

      /**
       *  Override to fire the timeout.
       */
      public void run()
      {
         try
         {
            work.getTimeoutTarget().timedOut(work);
         }
         catch (Throwable t)
         {
            // protect the thread pool thread from receiving this error
            ThrowableHandler.add(ThrowableHandler.Type.ERROR, t);
         }
         synchronized (work)
         {
            work.done();
         }
      }
   }
   
   /**
    * Simple TimeoutTarget implementation that wraps a Runnable
    */
   private static class TimeoutTargetImpl implements TimeoutTarget
   {
      Runnable runnable;
      
      TimeoutTargetImpl(Runnable runnable)
      {
         this.runnable = runnable;
      }
      
      public void timedOut(Timeout ignored)
      {
         runnable.run();
      }
   }
}