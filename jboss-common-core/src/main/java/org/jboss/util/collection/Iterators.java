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
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;

import org.jboss.util.Null;

/**
 * A collection of <code>Iterator</code> and <code>Enumeration</code>
 * utilities.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public final class Iterators
{
   /////////////////////////////////////////////////////////////////////////
   //                    Enumeration/Iterator Conversion                  //
   /////////////////////////////////////////////////////////////////////////

   /**
    * An Enumeration to Iterator wrapper.
    */
   private static final class Enum2Iterator
      implements Iterator
   {
      private final Enumeration enumeration;
    
      public Enum2Iterator(final Enumeration enumeration) {
         this.enumeration = enumeration;
      }
   
      public boolean hasNext() {
         return enumeration.hasMoreElements();
      }
    
      public Object next() {
         return enumeration.nextElement();
      }
      
      public void remove() {
         throw new UnsupportedOperationException("Enumerations are immutable");
      }
   }

   /**
    * Return an Iterator wrapper for the given Enumeration
    *
    * @param enumeration    Enumeration to wrap
    * @return        Enumeration wrapped as an Iterator
    */
   public static Iterator forEnumeration(final Enumeration enumeration) {
      return new Enum2Iterator(enumeration);
   }

   /**
    * An Iterator to Enumeration wrapper class.
    */
   private static final class Iter2Enumeration
      implements Enumeration
   {
      private final Iterator iter;

      public Iter2Enumeration(final Iterator iter) {
         this.iter = iter;
      }

      public boolean hasMoreElements() {
         return iter.hasNext();
      }

      public Object nextElement() {
         return iter.next();
      }
   }

   /**
    * Return an Enumeration for the given Iterator.
    *
    * @param iter    Iterator to wrap.
    * @return        Enumeration wrapper.
    */
   public static Enumeration toEnumeration(final Iterator iter) {
      return new Iter2Enumeration(iter);
   }


   /////////////////////////////////////////////////////////////////////////
   //                           Iterator Wrappers                         //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Wraps an Iterator making it immutable, by disabling calls to
    * <code>remove()</code>
    */
   private static final class ImmutableIterator
      implements Iterator
   {
      private final Iterator iter;

      public ImmutableIterator(final Iterator iter) {
         this.iter = iter;
      }

      public boolean hasNext() {
         return iter.hasNext();
      }

      public Object next() {
         return iter.next();
      }

      public void remove() {
         throw new UnsupportedOperationException("iterator is immutable");
      }
   }

   /**
    * Make an Iterator immutable
    *
    * @param iter    Iterator to make immutable
    * @return        Imutable iterator
    */
   public static Iterator makeImmutable(final Iterator iter) {
      return new ImmutableIterator(iter);
   }

   /**
    * Wraps an Iterator making it synchronized.
    */
   private static final class SyncIterator
      implements Iterator
   {
      private final Iterator iter;

      public SyncIterator(final Iterator iter) {
         this.iter = iter;
      }

      public synchronized boolean hasNext() {
         return iter.hasNext();
      }

      public synchronized Object next() {
         return iter.next();
      }

      public synchronized void remove() {
         iter.remove();
      }
   }

   /**
    * Returns a synchronized version of the given Iterator.
    *
    * @param iter    Iterator to synchronize.
    * @return        Synchronized Iterator.
    */
   public static Iterator makeSynchronized(final Iterator iter) {
      return new SyncIterator(iter);
   }

   /**
    * Wraps an Enumeration making it synchronized.
    */
   private static final class SyncEnumeration
      implements Enumeration
   {
      private final Enumeration enumeration;

      public SyncEnumeration(final Enumeration enumeration) {
         this.enumeration = enumeration;
      }

      public synchronized boolean hasMoreElements() {
         return enumeration.hasMoreElements();
      }

      public synchronized Object nextElement() {
         return enumeration.nextElement();
      }
   }

   /**
    * Returns a synchronized version of the given Enumeration.
    *
    * @param enumeration    Enumeration to synchronize.
    * @return        Synchronized Enumeration.
    */
   public static Enumeration makeSynchronized(final Enumeration enumeration) {
      return new SyncEnumeration(enumeration);
   }


   /////////////////////////////////////////////////////////////////////////
   //                            Empty Iterator                           //
   /////////////////////////////////////////////////////////////////////////

   /** An empty Iterator */
   public static final Iterator EMPTY_ITERATOR = new EmptyIterator();

   /**
    * An empty Iterator
    */
   private static final class EmptyIterator
      implements Iterator
   {
      public boolean hasNext() {
         return false;
      }
   
      public Object next() { 
         throw new NoSuchElementException("no more elements");
      }
   
      public void remove() {
         throw new IllegalStateException("no more elements");
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                              Misc Methods                           //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Returns an Iterator containing the <i>union</i> of all of the elements
    * in the given iterator array.
    *
    * @param iters   Array of iterators.
    * @return        Iterator containing the <i>union</i>.
    */
   public static Iterator union(final Iterator iters[]) {
      Map map = new HashMap();

      for (int i=0; i < iters.length; i++) {
         if (iters[i] != null) {
            while (iters[i].hasNext()) {
               Object obj = iters[i].next();
               if (!map.containsKey(obj)) {
                  map.put(obj, Null.VALUE);
               }
            }
         }
      }

      return map.keySet().iterator();
   }

   /**
    * Return a delimited string representation of all of the elements
    * in the given Iterator.
    *
    * @param iter    Iterator to convert to string.
    * @param delim   Elemement delimiter.
    * @return        Delimited string value.
    */
   public static String toString(final Iterator iter, final String delim) {
      StringBuffer buff = new StringBuffer();
      while (iter.hasNext()) {
         buff.append(iter.next());

         if (iter.hasNext()) {
            buff.append(delim);
         }
      }

      return buff.toString();
   }

   /**
    * Return a comma delimited string representation of all of the elements
    * in the given Iterator.
    *
    * @param iter    Iterator to convert to string.
    * @return        Delimited string value.
    */
   public static String toString(final Iterator iter) {
      return toString(iter, ",");
   }
}
