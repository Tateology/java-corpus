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
package org.jboss.util.collection;

import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * This Map will remove entries when the value in the map has been
 * cleaned from garbage collection
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ReferenceValueMap<K, V> extends AbstractMap<K, V>
{
   /** Hash table mapping keys to ref values */
   private Map<K, ValueRef<K, V>> map;

   /** Reference queue for cleared RefKeys */
   private ReferenceQueue<V> queue = new ReferenceQueue<V>();

   protected ReferenceValueMap()
   {
      map = createMap();
   }

   protected ReferenceValueMap(int initialCapacity)
   {
      map = createMap(initialCapacity);
   }

   protected ReferenceValueMap(int initialCapacity, float loadFactor)
   {
      map = createMap(initialCapacity, loadFactor);
   }

   protected ReferenceValueMap(Map<K, V> t)
   {
      this(Math.max(2*t.size(), 11), 0.75f);
      putAll(t);
   }

   protected ReferenceValueMap(Comparator<K> comparator)
   {
      map = createMap(comparator);
   }

   protected ReferenceValueMap(SortedMap<K, ValueRef<K, V>> sorted)
   {
      map = createMap(sorted);
   }
   
   /**
    * Create map.
    *
    * @return new map instance
    */
   protected abstract Map<K, ValueRef<K, V>> createMap();

   /**
    * Create map.
    *
    * @param initialCapacity the initial capacity
    * @return new map instance
    */
   protected abstract Map<K, ValueRef<K, V>> createMap(int initialCapacity);

   /**
    * Create map.
    *
    * @param initialCapacity the initial capacity
    * @param loadFactor the load factor
    * @return new map instance
    */
   protected abstract Map<K, ValueRef<K, V>> createMap(int initialCapacity, float loadFactor);

   /**
    * Create map.
    *
    * @param comparator the comparator
    * @return new map instance
    */
   protected abstract Map<K, ValueRef<K, V>> createMap(Comparator<K> comparator);

   /**
    * Create map.
    *
    * @param map the sorted map
    * @return new map instance
    */
   protected abstract Map<K, ValueRef<K, V>> createMap(SortedMap<K, ValueRef<K, V>> map);

   @Override
   public int size()
   {
      processQueue();
      return map.size();
   }

   @Override
   public boolean containsKey(Object key)
   {
      processQueue();
      return map.containsKey(key);
   }

   @Override
   public V get(Object key)
   {
      processQueue();
      ValueRef<K, V> ref = map.get(key);
      if (ref != null)
         return ref.get();
      return null;
   }

   @Override
   public V put(K key, V value)
   {
      processQueue();
      ValueRef<K, V> ref = create(key, value, queue);
      ValueRef<K, V> result = map.put(key, ref);
      if (result != null)
         return result.get();
      return null;
   }

   @Override
   public V remove(Object key)
   {
      processQueue();
      ValueRef<K, V> result = map.remove(key);
      if (result != null)
         return result.get();
      return null;
   }

   @Override
   public Set<Entry<K,V>> entrySet()
   {
      processQueue();
      return new EntrySet();
   }

   @Override
   public void clear()
   {
      processQueue();
      map.clear();
   }

   /**
    * Remove all entries whose values have been discarded.
    */
   @SuppressWarnings("unchecked")
   private void processQueue()
   {
      ValueRef<K, V> ref = (ValueRef<K, V>) queue.poll();
      while (ref != null)
      {
         // only remove if it is the *exact* same WeakValueRef
         if (ref == map.get(ref.getKey()))
            map.remove(ref.getKey());

         ref = (ValueRef<K, V>) queue.poll();
      }
   }

   /**
    * EntrySet.
    */
   private class EntrySet extends AbstractSet<Entry<K, V>>
   {
      @Override
      public Iterator<Entry<K, V>> iterator()
      {
         return new EntrySetIterator(map.entrySet().iterator());
      }

      @Override
      public int size()
      {
         return ReferenceValueMap.this.size();
      }
   }

   /**
    * EntrySet iterator
    */
   private class EntrySetIterator implements Iterator<Entry<K, V>>
   {
      /** The delegate */
      private Iterator<Entry<K, ValueRef<K, V>>> delegate;

      /**
       * Create a new EntrySetIterator.
       *
       * @param delegate the delegate
       */
      public EntrySetIterator(Iterator<Entry<K, ValueRef<K, V>>> delegate)
      {
         this.delegate = delegate;
      }

      public boolean hasNext()
      {
         return delegate.hasNext();
      }

      public Entry<K, V> next()
      {
         Entry<K, ValueRef<K, V>> next = delegate.next();
         return next.getValue();
      }

      public void remove()
      {
         throw new UnsupportedOperationException("remove");
      }
   }

   /**
    * Create new value ref instance.
    *
    * @param key the key
    * @param value the value
    * @param q the ref queue
    * @return new value ref instance
    */
   protected abstract ValueRef<K, V> create(K key, V value, ReferenceQueue<V> q);

   @Override
   public String toString()
   {
      return map.toString();
   }
}