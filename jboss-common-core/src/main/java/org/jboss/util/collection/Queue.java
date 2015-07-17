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

import java.util.Collection;

/**
 * An iterface used to implement a first-in, first-out container.
 * 
 * @param <E> the element type
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface Queue<E>
   extends Collection<E>
{
   /** Unlimited maximum queue size identifier. */
   int UNLIMITED_MAXIMUM_SIZE = -1;

   /**
    * Get the maximum size of the queue.
    *
    * @return  Maximum pool size or {@link #UNLIMITED_MAXIMUM_SIZE}.
    */
   int getMaximumSize();

   /**
    * Set the maximum size of the queue.
    *
    * @param size    New maximim pool size or {@link #UNLIMITED_MAXIMUM_SIZE}.
    *
    * @exception IllegalArgumentException    Illegal size.
    */
   void setMaximumSize(int size) throws IllegalArgumentException;

   /**
    * Check if the queue is full.
    *
    * @return  True if the queue is full.
    */
   boolean isFull();

   /**
    * Check if the queue is empty.
    *
    * @return True if the queue is empty.
    */
   boolean isEmpty();

   /**
    * Enqueue an object onto the queue.
    *
    * @param obj     Object to enqueue.
    * @return        True if collection was modified.
    *
    * @exception FullCollectionException     The queue is full.
    */
   boolean add(E obj) throws FullCollectionException;

   /**
    * Dequeue an object from the queue.
    *
    * @return     Dequeued object.
    *
    * @exception EmptyCollectionException    The queue is empty.
    */
   E remove() throws EmptyCollectionException;

   /**
    * Get the object at the front of the queue.
    *
    * @return  Object at the front of the queue.
    *
    * @exception EmptyCollectionException    The queue is empty.
    */
   E getFront() throws EmptyCollectionException;

   /**
    * Get the object at the back of the queue.
    *
    * @return  Object at the back of the queue.
    *
    * @exception EmptyCollectionException    The queue is empty.
    */
   E getBack() throws EmptyCollectionException;
}
