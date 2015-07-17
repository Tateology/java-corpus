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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.jboss.util.NullArgumentException;

/**
 * A ListQueue implements a first-in, first-out container using a List as
 * a data structure.
 *
 * @param <E> the element type
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ListQueue<E>
   extends AbstractQueue<E>
{
   /** List container */
   protected final List<E> list;

   /**
    * Construct a new <i>constrained</i> ListQueue.
    *
    * @param list    The list which will be used to store queued objects.
    * @param maxSize The maximum size of the queue.
    *
    * @exception IllegalArgumentException    List is <i>null</i>.
    */
   public ListQueue(final List<E> list, final int maxSize) {
      super(maxSize);

      if (list == null)
         throw new NullArgumentException("list");

      this.list = list;
   }

   /**
    * Construct a new <i>constrained</i> ListQueue using a
    * <code>LinkedList</code> for a data-structure.
    *
    * @param maxSize The maximum size of the queue.
    */
   public ListQueue(final int maxSize) {
      super(maxSize);
      this.list = new LinkedList<E>();
   }

   /**
    * Construct a new <i>unconstrained</i> ListQueue.
    *
    * @param list    The list which will be used to store queued objects.
    *
    * @exception IllegalArgumentException    List is <i>null</i>
    */
   public ListQueue(final List<E> list) {
      this(list, UNLIMITED_MAXIMUM_SIZE);
   }

   /**
    * Construct a new <i>unconstrained</i> ListQueue using a
    * <code>LinkedList</code> for a data-structure.
    */
   public ListQueue() {
      this(new LinkedList<E>(), UNLIMITED_MAXIMUM_SIZE);
   }

   /**
    * Appends the given element to the end of this list.
    *
    * @param obj  Object to append.
    */
   protected boolean addLast(final E obj) {
      return list.add(obj);
   }

   /**
    * Remove the first object in the queue.
    *
    * @return  First object in the queue.
    */
   protected E removeFirst() {
      return list.remove(0);
   }

   /**
    * Get the size of the queue.
    *
    * @return  The number of elements in the queue.
    */
   public int size() {
      return list.size();
   }

   /**
    * Returns an iterator over the elements in this list in proper sequence.
    * 
    * @return  An iterator over the elements in this list in proper sequence.
    */
   public Iterator<E> iterator() {
      return list.iterator();
   }

   /**
    * Get the object at the front of the queue.
    *
    * @return  Object at the front of the queue.
    *
    * @exception EmptyCollectionException    The queue is empty.
    */
   public E getFront() throws EmptyCollectionException {
      if (isEmpty())
         throw new EmptyCollectionException();

      return list.get(0);
   }

   /**
    * Get the object at the back of the queue.
    *
    * @return  Object at the back of the queue.
    *
    * @exception EmptyCollectionException    The queue is empty.
    */
   public E getBack() throws EmptyCollectionException {
      if (isEmpty())
         throw new EmptyCollectionException();

      return list.get(list.size() - 1);
   }

   /**
    * Returns an iterator over the elements in this list in reverse sequence.
    *
    * @return  An iterator over the elements in this list in reverse sequence.
    */
   public Iterator<E> reverseIterator() {
      return new ReverseListIterator<E>(list);
   }

   /**
    * Return a String representation of this queue.
    *
    * @return  String
    */
   public String toString() {
      return list.toString();
   }
}
