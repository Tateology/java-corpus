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
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Set implemented by a backing Map.
 * It's serializable if the elements are serializable.
 *
 * @param <E> the element type
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class MapDelegateSet<E> extends AbstractSet<E> implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   /** The delegate map */
   private final Map<E, Object> map;
   /** The dummy object */
   private static final Object PRESENT = new Object();

   /**
    * Set the initial map.
    *
    * @param map the initial map
    */
   public MapDelegateSet(Map<E, Object> map)
   {
      if (map == null)
         throw new IllegalArgumentException("Null map");
      this.map = map;
   }

   /**
    * Returns an iterator over the elements in this set.  The elements
    * are returned in no particular order.
    *
    * @return an Iterator over the elements in this set.
    * @see java.util.ConcurrentModificationException
    */
   public Iterator<E> iterator()
   {
      return map.keySet().iterator();
   }

   /**
    * Returns the number of elements in this set (its cardinality).
    *
    * @return the number of elements in this set (its cardinality).
    */
   public int size()
   {
      return map.size();
   }

   /**
    * Returns <tt>true</tt> if this set contains no elements.
    *
    * @return <tt>true</tt> if this set contains no elements.
    */
   public boolean isEmpty()
   {
      return map.isEmpty();
   }

   /**
    * Returns <tt>true</tt> if this set contains the specified element.
    *
    * @param o element whose presence in this set is to be tested.
    * @return <tt>true</tt> if this set contains the specified element.
    */
   @SuppressWarnings({"SuspiciousMethodCalls"})
   public boolean contains(Object o)
   {
      return map.containsKey(o);
   }

   /**
    * Adds the specified element to this set if it is not already
    * present.
    *
    * @param o element to be added to this set.
    * @return <tt>true</tt> if the set did not already contain the specified
    *         element.
    */
   public boolean add(E o)
   {
      return map.put(o, PRESENT) == null;
   }

   /**
    * Removes the specified element from this set if it is present.
    *
    * @param o object to be removed from this set, if present.
    * @return <tt>true</tt> if the set contained the specified element.
    */
   public boolean remove(Object o)
   {
      return map.remove(o) == PRESENT;
   }

   /**
    * Removes all of the elements from this set.
    */
   public void clear()
   {
      map.clear();
   }

   @Override
   public String toString()
   {
      return map.keySet().toString();
   }
}