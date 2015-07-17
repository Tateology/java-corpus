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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A weak class cache that instantiates does not a hold a
 * strong reference to either the classloader or class.<p>
 * 
 * It creates the class specific data in two stages
 * to avoid recursion.<p>
 * 
 * instantiate - creates the data<br>
 * generate - fills in the details
 *
 * @param <T> exact value type
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class WeakClassCache<T>
{
   /** The cache */
   protected final Map<ClassLoader, Map<String, WeakReference<T>>> cache = new WeakHashMap<ClassLoader, Map<String, WeakReference<T>>>();

   /**
    * Get the information for a class
    * 
    * @param clazz the class
    * @return the info
    */
   public T get(Class<?> clazz)
   {
      if (clazz == null)
         throw new IllegalArgumentException("Null class");
      
      Map<String, WeakReference<T>> classLoaderCache = getClassLoaderCache(clazz.getClassLoader());

      WeakReference<T> weak = classLoaderCache.get(clazz.getName());
      if (weak != null)
      {
         T result = weak.get();
         if (result != null)
            return result;
      }

      T result = instantiate(clazz);

      weak = new WeakReference<T>(result);
      classLoaderCache.put(clazz.getName(), weak);
      
      generate(clazz, result);
      
      return result;
   }
   
   /**
    * Get the information for a class
    * 
    * @param name the name
    * @param cl the classloader
    * @return the info
    * @throws ClassNotFoundException when the class cannot be found
    */
   public T get(String name, ClassLoader cl) throws ClassNotFoundException
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (cl == null)
         throw new IllegalArgumentException("Null classloader");

      Class<?> clazz = cl.loadClass(name);
      return get(clazz);
   }
   
   /**
    * Instantiate for a class
    * 
    * @param clazz the class
    * @return the result
    */
   protected abstract T instantiate(Class<?> clazz);
   
   /**
    * Fill in the result
    * 
    * @param clazz the class
    * @param result the result
    */
   protected abstract void generate(Class<?> clazz, T result);
   
   /**
    * Get the cache for the classloader
    * 
    * @param cl the classloader
    * @return the map
    */
   protected Map<String, WeakReference<T>> getClassLoaderCache(ClassLoader cl)
   {
      synchronized (cache)
      {
         Map<String, WeakReference<T>> result = cache.get(cl);
         if (result == null)
         {
            result = CollectionsFactory.createConcurrentReaderMap();
            cache.put(cl, result);
         }
         return result;
      }
   }
}
