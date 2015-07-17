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
import java.lang.ref.SoftReference;

/**
 * Soft value ref. 
 *
 * @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @param <K> the key type
 * @param <V> the value type
 */
class SoftValueRef<K, V> extends SoftReference<V> implements ValueRef<K, V>
{
   /**
    * The key
    */
   public K key;

   /**
    * Safely create a new SoftValueRef
    *
    * @param <K> the key type
    * @param <V> the value type
    * @param key the key
    * @param val the value
    * @param q   the reference queue
    * @return the reference or null if the value is null
    */
   static <K, V> SoftValueRef<K, V> create(K key, V val, ReferenceQueue<V> q)
   {
      if (val == null)
         return null;
      else
         return new SoftValueRef<K, V>(key, val, q);
   }

   /**
    * Create a new SoftValueRef.
    *
    * @param key the key
    * @param val the value
    * @param q   the reference queue
    */
   private SoftValueRef(K key, V val, ReferenceQueue<V> q)
   {
      super(val, q);
      this.key = key;
   }

   public K getKey()
   {
      return key;
   }

   public V getValue()
   {
      return get();
   }

   public V setValue(V value)
   {
      throw new UnsupportedOperationException("setValue");
   }

   @Override
   public String toString()
   {
      return String.valueOf(get());
   }
}
