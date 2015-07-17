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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.Serializable;

/**
 * LazySet.
 * It's serializable if the elements are serializable.
 *
 * @param <T> the element type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class LazySet<T> implements Set<T>, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   /** The delegate set */
   private Set<T> delegate = Collections.emptySet();

   /**
    * Create the set implementation
    * 
    * @return the set
    */
   private Set<T> createImplementation()
   {
      if (delegate instanceof HashSet == false)
         return new HashSet<T>(delegate);
      return delegate;
   }

   public boolean add(T o)
   {
      if (delegate.isEmpty())
      {
         delegate = Collections.singleton(o);
         return true;
      }
      else
      {
         delegate = createImplementation();
         return delegate.add(o);
      }
   }

   public boolean addAll(Collection<? extends T> c)
   {
      delegate = createImplementation();
      return delegate.addAll(c);
   }

   public void clear()
   {
      delegate = Collections.emptySet();
   }

   public boolean contains(Object o)
   {
      return delegate.contains(o);
   }

   public boolean containsAll(Collection<?> c)
   {
      return delegate.containsAll(c);
   }

   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }

   public Iterator<T> iterator()
   {
      return delegate.iterator();
   }

   public boolean remove(Object o)
   {
      delegate = createImplementation();
      return delegate.remove(o);
   }

   public boolean removeAll(Collection<?> c)
   {
      delegate = createImplementation();
      return delegate.removeAll(c);
   }

   public boolean retainAll(Collection<?> c)
   {
      delegate = createImplementation();
      return delegate.retainAll(c);
   }

   public int size()
   {
      return delegate.size();
   }

   public Object[] toArray()
   {
      return delegate.toArray();
   }

   public <U> U[] toArray(U[] a)
   {
      return delegate.toArray(a);
   }

   @Override
   public String toString()
   {
      return delegate.toString();
   }
}
