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

import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
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
public abstract class ReferenceValueHashMap<K, V> extends ReferenceValueMap<K, V>
{
   protected ReferenceValueHashMap()
   {
   }

   protected ReferenceValueHashMap(int initialCapacity)
   {
      super(initialCapacity);
   }

   protected ReferenceValueHashMap(int initialCapacity, float loadFactor)
   {
      super(initialCapacity, loadFactor);
   }

   protected ReferenceValueHashMap(Map<K, V> t)
   {
      super(t);
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity, float loadFactor)
   {
      return new HashMap<K, ValueRef<K,V>>(initialCapacity, loadFactor);
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity)
   {
      return new HashMap<K, ValueRef<K,V>>(initialCapacity);
   }

   protected Map<K, ValueRef<K, V>> createMap()
   {
      return new HashMap<K, ValueRef<K,V>>();
   }

   protected Map<K, ValueRef<K, V>> createMap(Comparator<K> kComparator)
   {
      throw new UnsupportedOperationException("Cannot create HashMap with such parameters.");
   }

   protected Map<K, ValueRef<K, V>> createMap(SortedMap<K, ValueRef<K, V>> kValueRefSortedMap)
   {
      throw new UnsupportedOperationException("Cannot create HashMap with such parameters.");
   }
}