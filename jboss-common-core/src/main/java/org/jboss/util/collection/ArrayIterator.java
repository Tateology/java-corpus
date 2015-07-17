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

import java.io.Serializable;

import org.jboss.util.NullArgumentException;

/**
 * An array iterator.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public class ArrayIterator
   implements Iterator, Serializable, Cloneable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6604583440222021075L;
   /** Array to iterate over. */
   protected final Object[] array;

   /** The current position in the array. */
   protected int index;

   /**
    * Construct an ArrayIterator.
    *
    * @param array   The array to iterate over.
    */
   public ArrayIterator(final Object[] array) {
      if (array == null)
         throw new NullArgumentException("array");

      this.array = array;
   }

   /**
    * Returns true if there are more elements in the iteration.
    *
    * @return  True if there are more elements in the iteration.
    */
   public boolean hasNext() {
      return index < array.length;
   }

   /**
    * Returns the next element in the iteration.
    *
    * @return  The next element in the iteration.
    *
    * @throws NoSuchElementException   The are no more elements available.
    */
   public Object next() {
      if (! hasNext())
         throw new NoSuchElementException();

      return array[index++];
   }

   /**
    * Unsupported.
    *
    * @throws UnsupportedOperationException
    */
   public void remove() {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns a shallow cloned copy of this object.
    *
    * @return  A shallow cloned copy of this object.
    */
   public Object clone() {
      try {
         return super.clone();
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError();
      }
   }
}
