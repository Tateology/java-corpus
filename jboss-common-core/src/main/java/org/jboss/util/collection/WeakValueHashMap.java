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
import java.util.Map;


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
public class WeakValueHashMap<K, V> extends ReferenceValueHashMap<K, V>
{
   /**
    * Constructs a new, empty <code>WeakValueHashMap</code> with the given
    * initial capacity and the given load factor.
    *
    * @param  initialCapacity  The initial capacity of the
    *                          <code>WeakValueHashMap</code>
    *
    * @param  loadFactor       The load factor of the <code>WeakValueHashMap</code>
    *
    * @throws IllegalArgumentException  If the initial capacity is less than
    *                                   zero, or if the load factor is
    *                                   nonpositive
    */
   public WeakValueHashMap(int initialCapacity, float loadFactor)
   {
      super(initialCapacity, loadFactor);
   }

   /**
    * Constructs a new, empty <code>WeakValueHashMap</code> with the given
    * initial capacity and the default load factor, which is
    * <code>0.75</code>.
    *
    * @param  initialCapacity  The initial capacity of the
    *                          <code>WeakValueHashMap</code>
    *
    * @throws IllegalArgumentException  If the initial capacity is less than
    *                                   zero
    */
   public WeakValueHashMap(int initialCapacity)
   {
      super(initialCapacity);
   }

   /**
    * Constructs a new, empty <code>WeakValueHashMap</code> with the default
    * initial capacity and the default load factor, which is
    * <code>0.75</code>.
    */
   public WeakValueHashMap()
   {
   }

   /**
    * Constructs a new <code>WeakValueHashMap</code> with the same mappings as the
    * specified <tt>Map</tt>.  The <code>WeakValueHashMap</code> is created with an
    * initial capacity of twice the number of mappings in the specified map
    * or 11 (whichever is greater), and a default load factor, which is
    * <tt>0.75</tt>.
    *
    * @param   t the map whose mappings are to be placed in this map.
    * @since    1.3
    */
   public WeakValueHashMap(Map<K, V> t)
   {
      super(t);
   }

   protected ValueRef<K, V> create(K key, V value, ReferenceQueue<V> q)
   {
      return WeakValueRef.create(key, value, q);
   }
}
