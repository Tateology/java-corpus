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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Collections factory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision$
 */
public class CollectionsFactory
{
   /**
    * Defines the map implementation
    * 
    * @param <K> the key type
    * @param <V> the value type
    * @return the map
    */
   public static final <K, V> Map<K, V> createLazyMap()
   {
      return new LazyMap<K, V>();
   }

   /**
    * Defines the list implementation
    * 
    * @param <T> the type
    * @return the list
    */
   public static final <T> List<T> createLazyList()
   {
      return new LazyList<T>();
   }

   /**
    * Defines the set implementation
    * 
    * @param <T> the type
    * @return the set
    */
   public static final <T> Set<T> createLazySet()
   {
      return new LazySet<T>();
   }

   /**
    * Defines the concurrent map implementation
    * 
    * @param <K> the key type
    * @param <V> the value type
    * @return the map
    */
   public static final <K, V> Map<K, V> createConcurrentReaderMap()
   {
      return new ConcurrentHashMap<K, V>();
   }

   /**
    * Defines the concurrent list implementation
    * 
    * @param <T> the type
    * @return the list
    */
   public static final <T> List<T> createCopyOnWriteList()
   {
      return new CopyOnWriteArrayList<T>();
   }

   /**
    * Defines the concurrent set implementation
    * 
    * @param <T> the type
    * @return the set
    */
   public static final <T> Set<T> createCopyOnWriteSet()
   {
      return new CopyOnWriteArraySet<T>();
   }

   /**
    * Defines the concurrent set implementation
    *
    * @param <T> the type
    * @return the set
    */
   public static final <T> Set<T> createConcurrentSet()
   {
      return new ConcurrentSet<T>();
   }

   /**
    * Defines the concurrent reference set implementation
    *
    * @param <T> the type
    * @return the set
    */
   public static final <T> Set<T> createConcurrentReferenceSet()
   {
      return new ConcurrentReferenceHashSet<T>();
   }
}
