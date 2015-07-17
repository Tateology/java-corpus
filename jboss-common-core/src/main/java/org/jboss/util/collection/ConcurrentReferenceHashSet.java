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

import java.util.EnumSet;
import java.util.Set;

/**
 * Set based on top of ConcurrentReferenceHashMap.
 * It's serializable if the elements are serializable.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @param <E> the element type
 */
public class ConcurrentReferenceHashSet<E> extends MapDelegateSet<E>
{
   /**
    * The serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new, empty set with the specified initial
    * capacity, load factor and concurrency level.
    *
    * @param initialCapacity  the initial capacity. The implementation
    *                         performs internal sizing to accommodate this many elements.
    * @param loadFactor       the load factor threshold, used to control resizing.
    *                         Resizing may be performed when the average number of elements per
    *                         bin exceeds this threshold.
    * @param concurrencyLevel the estimated number of concurrently
    *                         updating threads. The implementation performs internal sizing
    *                         to try to accommodate this many threads.
    * @throws IllegalArgumentException if the initial capacity is
    *                                  negative or the load factor or concurrencyLevel are
    *                                  nonpositive.
    */
   public ConcurrentReferenceHashSet(int initialCapacity, float loadFactor, int concurrencyLevel)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(initialCapacity, loadFactor, concurrencyLevel));
   }

   /**
    * Creates a new, empty set with the specified initial capacity
    * and load factor and with the default reference types (weak keys,
    * strong values), and concurrencyLevel (16).
    *
    * @param initialCapacity The implementation performs internal
    *                        sizing to accommodate this many elements.
    * @param loadFactor      the load factor threshold, used to control resizing.
    *                        Resizing may be performed when the average number of elements per
    *                        bin exceeds this threshold.
    * @throws IllegalArgumentException if the initial capacity of
    *                                  elements is negative or the load factor is nonpositive
    * @since 1.6
    */
   public ConcurrentReferenceHashSet(int initialCapacity, float loadFactor)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(initialCapacity, loadFactor));
   }

   /**
    * Creates a new, empty set with the specified initial capacity,
    * reference type and with default load factor (0.75) and concurrencyLevel (16).
    *
    * @param initialCapacity the initial capacity. The implementation
    *                        performs internal sizing to accommodate this many elements.
    * @param type         the reference type to use
    * @throws IllegalArgumentException if the initial capacity of
    *                                  elements is negative.
    */
   public ConcurrentReferenceHashSet(int initialCapacity, ConcurrentReferenceHashMap.ReferenceType type)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(initialCapacity, type, ConcurrentReferenceHashMap.ReferenceType.STRONG));
   }

   /**
    * Creates a new, empty reference set with the specified key
    * and value reference types.
    *
    * @param type         the reference type to use
    * @throws IllegalArgumentException if the initial capacity of
    *                                  elements is negative.
    */
   public ConcurrentReferenceHashSet(ConcurrentReferenceHashMap.ReferenceType type)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(type, ConcurrentReferenceHashMap.ReferenceType.STRONG));
   }

   /**
    * Creates a new, empty reference set with the specified reference types
    * and behavioral options.
    *
    * @param type         the reference type to use
    * @param options   the options
    * @throws IllegalArgumentException if the initial capacity of
    *                                  elements is negative.
    */
   public ConcurrentReferenceHashSet(ConcurrentReferenceHashMap.ReferenceType type, EnumSet<ConcurrentReferenceHashMap.Option> options)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(type, ConcurrentReferenceHashMap.ReferenceType.STRONG, options));
   }

   /**
    * Creates a new, empty set with the specified initial capacity,
    * and with default reference types (weak keys, strong values),
    * load factor (0.75) and concurrencyLevel (16).
    *
    * @param initialCapacity the initial capacity. The implementation
    *                        performs internal sizing to accommodate this many elements.
    * @throws IllegalArgumentException if the initial capacity of
    *                                  elements is negative.
    */
   public ConcurrentReferenceHashSet(int initialCapacity)
   {
      super(new ConcurrentReferenceHashMap<E, Object>(initialCapacity));
   }

   /**
    * Creates a new, empty set with a default initial capacity (16),
    * reference types (weak keys, strong values), default
    * load factor (0.75) and concurrencyLevel (16).
    */
   public ConcurrentReferenceHashSet()
   {
      super(new ConcurrentReferenceHashMap<E, Object>());
   }

   /**
    * Creates a new set with the same contents as the given set.
    *
    * @param s the set
    */
   public ConcurrentReferenceHashSet(Set<? extends E> s)
   {
      super(new ConcurrentReferenceHashMap<E, Object>());
      addAll(s);
   }
}