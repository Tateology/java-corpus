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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * LazyMap.
 * It's serializable if the elements are serializable.
 * 
 * @param <K> the key type
 * @param <V> the value type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class LazyMap<K, V> implements Map<K, V>, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   /** The delegate map */
   private Map<K, V> delegate = Collections.emptyMap();

   /**
    * Create the map implementation
    * 
    * @return the map
    */
   private Map<K, V> createImplementation()
   {
      if (delegate instanceof HashMap == false)
         return new HashMap<K, V>(delegate);
      return delegate;
   }

   public void clear()
   {
      delegate = Collections.emptyMap();
   }

   public boolean containsKey(Object key)
   {
      return delegate.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return delegate.containsValue(value);
   }

   public Set<Entry<K, V>> entrySet()
   {
      return delegate.entrySet();
   }

   public V get(Object key)
   {
      return delegate.get(key);
   }

   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }

   public Set<K> keySet()
   {
      return delegate.keySet();
   }

   public V put(K key, V value)
   {
      if (delegate.isEmpty())
      {
         delegate = Collections.singletonMap(key, value);
         return null;
      }
      else
      {
         delegate = createImplementation();
         return delegate.put(key, value);
      }
   }

   public void putAll(Map<? extends K, ? extends V> t)
   {
      delegate = createImplementation();
      delegate.putAll(t);
   }

   public V remove(Object key)
   {
      delegate = createImplementation();
      return delegate.remove(key);
   }

   public int size()
   {
      return delegate.size();
   }

   public Collection<V> values()
   {
      return delegate.values();
   }

   @Override
   public String toString()
   {
      return delegate.toString();
   }
}
