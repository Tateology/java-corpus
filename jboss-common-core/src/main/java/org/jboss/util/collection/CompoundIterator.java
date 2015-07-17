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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A compound iterator, which iterates over all of the elements in the
 * given iterators.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public class CompoundIterator
   implements Iterator
{
   /** The array of iterators to iterate over. */
   protected final Iterator iters[];

   /** The index of the current iterator. */
   protected int index;

   /**
    * Construct a CompoundIterator over the given array of iterators.
    *
    * @param iters   Array of iterators to iterate over.
    *
    * @throws IllegalArgumentException    Array is <kk>null</kk> or empty.
    */
   public CompoundIterator(final Iterator iters[]) {
      if (iters == null || iters.length == 0)
         throw new IllegalArgumentException("array is null or empty");
     
      this.iters = iters;
   }

   /**
    * Check if there are more elements.
    *
    * @return  True if there are more elements.
    */
   public boolean hasNext() {
      for (; index < iters.length; index++) {
         if (iters[index] != null && iters[index].hasNext()) {
            return true;
         }
      }

      return false;
   }

   /**
    * Return the next element from the current iterator.
    *
    * @return  The next element from the current iterator.
    *
    * @throws NoSuchElementException   There are no more elements.
    */
   public Object next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      }

      return iters[index].next();
   }

   /**
    * Remove the current element from the current iterator.
    *
    * @throws IllegalStateException
    * @throws UnsupportedOperationException
    */
   public void remove() {
      iters[index].remove();
   }
}
