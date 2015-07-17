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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This Map will remove entries when the value in the map has been
 * cleaned from garbage collection
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ReferenceValueTreeMap<K, V> extends ReferenceValueMap<K, V>
{
   protected ReferenceValueTreeMap()
   {
   }

   protected ReferenceValueTreeMap(Comparator<K> comparator)
   {
      super(comparator);
   }

   protected ReferenceValueTreeMap(SortedMap<K, ValueRef<K, V>> sorted)
   {
      super(sorted);
   }

   protected Map<K, ValueRef<K, V>> createMap()
   {
      return new TreeMap<K, ValueRef<K,V>>();
   }

   protected Map<K, ValueRef<K, V>> createMap(Comparator<K> comparator)
   {
      return new TreeMap<K, ValueRef<K,V>>(comparator);
   }

   protected Map<K, ValueRef<K, V>> createMap(SortedMap<K, ValueRef<K, V>> map)
   {
      return new TreeMap<K, ValueRef<K,V>>(map);
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity)
   {
      throw new UnsupportedOperationException("Cannot create TreeMap with such parameters.");
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity, float loadFactor)
   {
      throw new UnsupportedOperationException("Cannot create TreeMap with such parameters.");
   }
}