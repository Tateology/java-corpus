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

import java.io.Serializable;
import java.util.Collection;

/**
 * Concurrent Set based on top of FastCopyHashMap.
 * It's serializable if the elements are serializable.
 *
 * @param <E> the element type
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class FastCopyHashSet<E> extends MapDelegateSet<E> implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new, empty set; the backing <tt>FastCopyHashMap</tt> instance has
    * default initial capacity (16) and load factor (0.75).
    */
   public FastCopyHashSet()
   {
      super(new FastCopyHashMap<E, Object>());
   }

   /**
    * Constructs a new set containing the elements in the specified
    * collection.  The <tt>FastCopyHashMap</tt> is created with default load factor
    * (0.75) and an initial capacity sufficient to contain the elements in
    * the specified collection.
    *
    * @param c the collection whose elements are to be placed into this set.
    * @throws NullPointerException if the specified collection is null.
    */
   public FastCopyHashSet(Collection<? extends E> c)
   {
      super(new FastCopyHashMap<E, Object>(Math.max((int)(c.size() / .75f) + 1, 16)));
      addAll(c);
   }

   /**
    * Constructs a new, empty set; the backing <tt>FastCopyHashMap</tt> instance has
    * the specified initial capacity and the specified load factor.
    *
    * @param initialCapacity  the initial capacity. The implementation
    *                         performs internal sizing to accommodate this many elements.
    * @param loadFactor       the load factor threshold, used to control resizing.
    *                         Resizing may be performed when the average number of elements per
    *                         bin exceeds this threshold.
    * @throws IllegalArgumentException if the initial capacity is less
    *                                  than zero, or if the load factor is nonpositive.
    */
   public FastCopyHashSet(int initialCapacity, float loadFactor)
   {
      super(new FastCopyHashMap<E, Object>(initialCapacity, loadFactor));
   }

   /**
    * Constructs a new, empty set; the backing <tt>FastCopyHashMap</tt> instance has
    * the specified initial capacity and default load factor, which is
    * <tt>0.75</tt>.
    *
    * @param initialCapacity the initial capacity of the hash table.
    * @throws IllegalArgumentException if the initial capacity is less
    *                                  than zero.
    */
   public FastCopyHashSet(int initialCapacity)
   {
      super(new FastCopyHashMap<E, Object>(initialCapacity));
   }
}