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
package org.jboss.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.logging.Logger;
import org.jboss.util.loading.ContextClassLoaderSwitcher;

/** An implementation of a timed cache. This is a cache whose entries have a
    limited lifetime with the ability to refresh their lifetime. The entries
    managed by the cache implement the TimedCachePolicy.TimedEntry interface. If
    an object inserted into the cache does not implement this interface, it will
    be wrapped in a DefaultTimedEntry and will expire without the possibility of
    refresh after getDefaultLifetime() seconds.

    This is a lazy cache policy in that objects are not checked for expiration
    until they are accessed.

    @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
    @version $Revision$
*/
@SuppressWarnings("unchecked")
public class TimedCachePolicy
   extends TimerTask /* A legacy base class that is no longer used as this level */
   implements CachePolicy
{
   /** 
    * Name of system property that this class consults to determine what
    * classloader to assign to its static {@link Timer}'s thread.
    */
   public static final String TIMER_CLASSLOADER_PROPERTY = "jboss.common.timedcachepolicy.timer.classloader";
   
   /**
    * Value for {@link #TIMER_CLASSLOADER_PROPERTY} indicating the
    * {@link ClassLoader#getSystemClassLoader() system classloader} should
    * be used. This is the default value if the system property is not set.
    */
   public static final String TIMER_CLASSLOADER_SYSTEM = "system";
   /**
    * Value for {@link #TIMER_CLASSLOADER_PROPERTY} indicating the
    * {@link Class#getClassLoader() classloader that loaded this class} should
    * be used.
    */
   public static final String TIMER_CLASSLOADER_CURRENT = "current";
   /**
    * Value for {@link #TIMER_CLASSLOADER_PROPERTY} indicating the
    * {@link Thread#getContextClassLoader() thread context classloader}
    * in effect when this class is loaded should be used.
    */
   public static final String TIMER_CLASSLOADER_CONTEXT = "context";
   
   /** The interface that cache entries support.
    */
   public static interface TimedEntry
   {
      /** Initializes an entry with the current cache time. This is called when
          the entry is first inserted into the cache so that entries do not
          have to know the absolute system time.
       * @param now 
      */
      public void init(long now);
      
      /** Is the entry still valid basis the current time
       * @param now 
          @return true if the entry is within its lifetime, false if it is expired.
      */
      public boolean isCurrent(long now);
      
      /** Attempt to extend the entry lifetime by refreshing it.
          @return true if the entry was refreshed successfully, false otherwise.
      */
      public boolean refresh();
      
      /** Notify the entry that it has been removed from the cache.
      */
      public void destroy();

      /** @return the value component of the TimedEntry. This may or may not
          be the TimedEntry implementation.
      */
      public Object getValue();
   }
   
   private static final Logger log = Logger.getLogger(TimedCachePolicy.class);

   protected static Timer resolutionTimer;

   static
   {
      // Don't leak the TCCL to the resolutionTimer thread
      ContextClassLoaderSwitcher.SwitchContext clSwitchContext = null;
      try
      {
         // See if the user configured what classloader they want
         String timerCl = AccessController.doPrivileged(new PrivilegedAction<String>()
         {
            public String run()
            {
               return System.getProperty(TIMER_CLASSLOADER_PROPERTY, TIMER_CLASSLOADER_SYSTEM);
            }
         });
         
         if (TIMER_CLASSLOADER_CONTEXT.equalsIgnoreCase(timerCl) == false)
         {         
            ContextClassLoaderSwitcher clSwitcher = (ContextClassLoaderSwitcher) AccessController.doPrivileged(ContextClassLoaderSwitcher.INSTANTIATOR);
            if (TIMER_CLASSLOADER_CURRENT.equalsIgnoreCase(timerCl))
            {
               // Switches the TCCL to this class' classloader
               clSwitchContext = clSwitcher.getSwitchContext(TimedCachePolicy.class.getClassLoader());
            }
            else
            {
               if (TIMER_CLASSLOADER_SYSTEM.equalsIgnoreCase(timerCl) == false)
               {
                  log.warn("Unknown value " + timerCl + " found for property " + 
                        TIMER_CLASSLOADER_PROPERTY + " -- using the system classloader");
               }
               clSwitchContext = clSwitcher.getSwitchContext(ClassLoader.getSystemClassLoader());
            }
         }
         resolutionTimer = new Timer(true);
      }
      catch (SecurityException e)
      {
         // For backward compatibility, don't blow up, just risk leaking the TCCL
         // TODO log a WARN or something?
         resolutionTimer = new Timer(true);
      }
      finally
      {
         // Restores the TCCL
         if (clSwitchContext != null)
            clSwitchContext.reset();
      }
   }
   
   /** The map of cached TimedEntry objects. */
   protected Map entryMap;
   /** The lifetime in seconds to use for objects inserted
       that do not implement the TimedEntry interface. */
   protected int defaultLifetime;
   /** A flag indicating if entryMap should be synchronized */
   protected boolean threadSafe;
   /** The caches notion of the current time */
   protected long now;
   /** The resolution in seconds of the cach current time */
   protected int resolution;
   /** */
   protected ResolutionTimer theTimer;

   /** Creates a new TimedCachePolicy with a default entry lifetime of 30 mins
       that does not synchronized access to its policy store and uses a 60
       second resolution.
   */
   public TimedCachePolicy() 
   {
      this(30*60, false, 0);
   }
   /** Creates a new TimedCachePolicy with the given default entry lifetime
       that does not synchronized access to its policy store and uses a 60
       second resolution.
    * @param defaultLifetime 
   */
   public TimedCachePolicy(int defaultLifetime)
   {
      this(defaultLifetime, false, 0);
   }
   /** Creates a new TimedCachePolicy with the given default entry lifetime
       that does/does not synchronized access to its policy store depending
       on the value of threadSafe.
       @param defaultLifetime - the lifetime in seconds to use for objects inserted
       that do not implement the TimedEntry interface.
       @param threadSafe - a flag indicating if the cach store should be synchronized
       to allow correct operation under multi-threaded access. If true, the
       cache store is synchronized. If false the cache store is unsynchronized and
       the cache is not thread safe.
       @param resolution - the resolution in seconds of the cache timer. A cache does
       not query the system time on every get() invocation. Rather the cache
       updates its notion of the current time every 'resolution' seconds.
   */
   public TimedCachePolicy(int defaultLifetime, boolean threadSafe, int resolution)
   {
      this.defaultLifetime = defaultLifetime;
      this.threadSafe = threadSafe;
      if( resolution <= 0 )
         resolution = 60;
      this.resolution = resolution;
   }

   // Service implementation ----------------------------------------------
   /** Initializes the cache for use. Prior to this the cache has no store.
    */
   public void create()
   {
      if( threadSafe )
         entryMap = Collections.synchronizedMap(new HashMap());
      else
         entryMap = new HashMap();
      now = System.currentTimeMillis();
   }
   /** Schedules this with the class resolutionTimer Timer object for
       execution every resolution seconds.
   */
   public void start()
   {
      theTimer = new ResolutionTimer();
      resolutionTimer.scheduleAtFixedRate(theTimer, 0, 1000*resolution);
   }
   /** Stop cancels the resolution timer and flush()es the cache.
    */
   public void stop() 
   {
      theTimer.cancel();
      flush();
   }
   /** Clears the cache of all entries.
    */
   public void destroy() 
   {
      entryMap.clear();
   }

   // --- Begin CachePolicy interface methods
   /** Get the cache value for key if it has not expired. If the TimedEntry
    is expired its destroy method is called and then removed from the cache.
    @return the TimedEntry value or the original value if it was not an
       instance of TimedEntry if key is in the cache, null otherwise.
   */
   public Object get(Object key) 
   {
      TimedEntry entry = (TimedEntry) entryMap.get(key);
      if( entry == null )
         return null;

      if( entry.isCurrent(now) == false )
      {   // Try to refresh the entry
         if( entry.refresh() == false )
         {   // Failed, remove the entry and return null
            entry.destroy();
            entryMap.remove(key);
            return null;
         }
      }
      Object value = entry.getValue();
      return value;
   }
   /** Get the cache value for key. This method does not check to see if
       the entry has expired.
       @return the TimedEntry value or the original value if it was not an
       instancee of TimedEntry if key is in the cache, null otherwise.
   */
   public Object peek(Object key) 
   {
      TimedEntry entry = (TimedEntry) entryMap.get(key);
      Object value = null;
      if( entry != null )
         value = entry.getValue();
      return value;
   }
   /** Insert a value into the cache. In order to have the cache entry
       reshresh itself value would have to implement TimedEntry and
       implement the required refresh() method logic.
       @param key - the key for the cache entry
       @param value - Either an instance of TimedEntry that will be inserted without
       change, or an abitrary value that will be wrapped in a non-refreshing
       TimedEntry.
   */
   public void insert(Object key, Object value) 
   {
      if( entryMap.containsKey(key) )
         throw new IllegalStateException("Attempt to insert duplicate entry");
      TimedEntry entry = null;
      if( (value instanceof TimedEntry) == false )
      {   // Wrap the value in a DefaultTimedEntry
         entry = new DefaultTimedEntry(defaultLifetime, value);
      }
      else
      {
         entry = (TimedEntry) value;
      }
      entry.init(now);
      entryMap.put(key, entry);
   }
   /** Remove the entry associated with key and call destroy on the entry
    if found.
    */
   public void remove(Object key) 
   {
      TimedEntry entry = (TimedEntry) entryMap.remove(key);
      if( entry != null )
         entry.destroy();
   }
   /** Remove all entries from the cache.
    */
   public void flush() 
   {
      Map tmpMap = null;
      synchronized( this )
      {
         tmpMap = entryMap;
         if( threadSafe )
            entryMap = Collections.synchronizedMap(new HashMap());
         else
            entryMap = new HashMap();
      }

      // Notify the entries of their removal
      Iterator iter = tmpMap.values().iterator();
      while( iter.hasNext() )
      {
         TimedEntry entry = (TimedEntry) iter.next();
         entry.destroy();
      }
      tmpMap.clear();
   }

   public int size()
   {
      return entryMap.size();
   }
   // --- End CachePolicy interface methods

   /** Get the list of keys for entries that are not expired.
    *
    * @return A List of the keys corresponding to valid entries
    */
   public List getValidKeys()
   {
      ArrayList validKeys = new ArrayList();
      synchronized( entryMap )
      {
         Iterator iter = entryMap.entrySet().iterator();
         while( iter.hasNext() )
         {
            Map.Entry entry = (Map.Entry) iter.next();
            TimedEntry value = (TimedEntry) entry.getValue();
            if( value.isCurrent(now) == true )
               validKeys.add(entry.getKey());
         }
      }
      return validKeys;
   }

   /** Get the default lifetime of cache entries.
    @return default lifetime in seconds of cache entries.
    */
   public int getDefaultLifetime()
   {
      return defaultLifetime;
   }
   /** Set the default lifetime of cache entries for new values added to the cache.
    @param defaultLifetime - lifetime in seconds of cache values that do
    not implement TimedEntry.
    */
   public synchronized void setDefaultLifetime(int defaultLifetime)
   {
      this.defaultLifetime = defaultLifetime;
   }

   /**
    * Get the frequency of the current time snapshot.
    * @return the current timer resolution in seconds.
    */ 
   public int getResolution()
   {
      return resolution;
   }
   /** Set the cache timer resolution
    * 
    @param resolution - the resolution in seconds of the cache timer. A cache does
    not query the system time on every get() invocation. Rather the cache
    updates its notion of the current time every 'resolution' seconds.
    */ 
   public synchronized void setResolution(int resolution)
   {
      if( resolution <= 0 )
         resolution = 60;
      if( resolution != this.resolution )
      {
         this.resolution = resolution;
         theTimer.cancel();
         theTimer = new ResolutionTimer();
         resolutionTimer.scheduleAtFixedRate(theTimer, 0, 1000*resolution);
      }
   }

   /** The TimerTask run method. It updates the cache time to the
       current system time.
   */
   public void run()
   {
      now = System.currentTimeMillis();
   }

   /** Get the cache time.
       @return the cache time last obtained from System.currentTimeMillis()
   */
   public long currentTimeMillis()
   {
      return now;
   }

   /** Get the raw TimedEntry for key without performing any expiration check.
    * @param key 
       @return the TimedEntry value associated with key if one exists, null otherwise.
   */
   public TimedEntry peekEntry(Object key) 
   {
      TimedEntry entry = (TimedEntry) entryMap.get(key);
      return entry;
   }

   /** The default implementation of TimedEntry used to wrap non-TimedEntry
       objects inserted into the cache.
   */
   static class DefaultTimedEntry implements TimedEntry
   {
      long expirationTime;
      Object value;

      DefaultTimedEntry(long lifetime, Object value)
      {
         this.expirationTime = 1000 * lifetime;
         this.value = value;
      }
      public void init(long now)
      {
         expirationTime += now;
      }
      public boolean isCurrent(long now)
      {
         return expirationTime > now;
      }
      public boolean refresh()
      {
         return false;
      }
      public void destroy()
      {
      }
      public Object getValue()
      {
         return value;
      }        
   }

   /**
    
    */
   private class ResolutionTimer extends TimerTask
   {
      public void run()
      {
         TimedCachePolicy.this.run();
      }
   }
}

