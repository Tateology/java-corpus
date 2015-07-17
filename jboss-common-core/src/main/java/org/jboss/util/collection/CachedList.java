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
import java.util.AbstractList;

import java.lang.ref.ReferenceQueue;

import org.jboss.util.SoftObject;
import org.jboss.util.Objects;

/**
 * A wrapper around a <code>List</code> which translates added objects
 * into {@link SoftObject} references, allowing the VM to garbage collect
 * objects in the collection when memory is low.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public class CachedList
   extends AbstractList
{
   /** Reference queue. */
   protected final ReferenceQueue queue = new ReferenceQueue();

   /** Wrapped list. */
   protected final List list;

   /**
    * Construct a <tt>CachedList</tt>.
    *
    * @param list    List to wrap.
    */
   public CachedList(final List list) {
      this.list = list;
   }

   /**
    * Construct a <tt>CachedList</tt> using a <tt>LinkedList</tt> for
    * storage.
    */
   public CachedList() {
      this(new LinkedList());
   }

   /**
    * Dereference the object at the given index.
    */
   private Object getObject(final int index) {
      Object obj = list.get(index);

      return Objects.deref(obj);
   }

   /**
    * Returns the element at the specified position in this list.
    *
    * @param index   Index of element to return.
    * @return        The element at the specified position.
    */
   public Object get(final int index) {
      maintain();
      return getObject(index);
   }

   /**
    * Return the size of the list.
    *
    * @return  The number of elements in the list.
    */
   public int size() {
      maintain();
      return list.size();
   }

   /**
    * Replaces the element at the specified position in this list with the 
    * specified element.
    *
    * @param index   Index of element to replace.
    * @param obj     Element to be stored at the specified postion.
    * @return        The previous element at the given index.
    */
   public Object set(final int index, final Object obj) {
      maintain();
      
      SoftObject soft = SoftObject.create(obj, queue);
      soft = (SoftObject)list.set(index, soft);

      return Objects.deref(soft);
   }

   /**
    * Inserts the specified element at the specified position in this list
    * (optional operation). Shifts the element currently at that position
    * (if any) and any subsequent elements to the right (adds one to their
    * indices).
    *
    * @param index   Index at which the specified element is to be inserted.
    * @param obj     Element to be inserted.
    */
   public void add(final int index, final Object obj) {
      maintain();

      SoftObject soft = SoftObject.create(obj, queue);
      list.add(index, soft);
   }

   /**
    * Removes the element at the specified position in this list (optional
    * operation). Shifts any subsequent elements to the left (subtracts one
    * from their indices). Returns the element that was removed from the list.
    *
    * @param index   The index of the element to remove.
    * @return        The element previously at the specified position.
    */
   public Object remove(final int index) {
      maintain();

      Object obj = list.remove(index);
      return Objects.deref(obj);
   }

   /**
    * Maintains the collection by removing garbage collected objects.
    */
   private void maintain() {
      SoftObject obj;
      int count = 0;

      while ((obj = (SoftObject)queue.poll()) != null) {
         count++;
         list.remove(obj);
      }

      if (count != 0) {
         // some temporary debugging fluff
         System.err.println("vm reclaimed " + count + " objects");
      }
   }
}
