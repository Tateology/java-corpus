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
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that returns elements in reverse order from a list.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @param <E> 
 */
public class ReverseListIterator<E>
   implements Iterator<E>
{
   /** The list to get elements from */
   protected final List<E> list;
   
   /** The current index of the list */
   protected int current;

   /**
    * Construct a ReverseListIterator for the given list.
    *
    * @param list    List to iterate over.
    */
   public ReverseListIterator(final List<E> list) {
      this.list = list;
      current = list.size() - 1;
   }

   /**
    * Check if there are more elements.
    *
    * @return  True if there are more elements.
    */
   public boolean hasNext() {
      return current > 0;
   }

   /**
    * Get the next element.
    *
    * @return  The next element.
    *
    * @throws NoSuchElementException
    */
   public E next() {
      if (current <= 0) {
         throw new NoSuchElementException();
      }
      
      return list.get(current--);
   }

   /**
    * Remove the current element.
    */
   public void remove() {
      list.remove(current);
   }
}
