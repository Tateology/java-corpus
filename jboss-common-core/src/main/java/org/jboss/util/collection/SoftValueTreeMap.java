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
import java.util.Comparator;
import java.util.SortedMap;

/**
 * This Map will remove entries when the value in the map has been
 * cleaned from garbage collection
 *
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SoftValueTreeMap<K, V> extends ReferenceValueTreeMap<K, V>
{
   public SoftValueTreeMap()
   {
   }

   public SoftValueTreeMap(Comparator<K> comparator)
   {
      super(comparator);
   }

   public SoftValueTreeMap(SortedMap<K, ValueRef<K, V>> sorted)
   {
      super(sorted);
   }

   protected ValueRef<K, V> create(K key, V value, ReferenceQueue<V> q)
   {
      return SoftValueRef.create(key, value, q);
   }
}