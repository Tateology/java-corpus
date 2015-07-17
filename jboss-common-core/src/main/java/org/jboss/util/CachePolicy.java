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
package org.jboss.util;

/**
 * Interface that specifies a policy for caches. <p>
 * Implementation classes can implement a LRU policy, a random one, 
 * a MRU one, or any other suitable policy.
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision$
 */
public interface CachePolicy
{
   /**
    * Returns the object paired with the specified key if it's 
    * present in the cache, otherwise must return null. <br>
    * Implementations of this method must have complexity of order O(1).
    * Differently from {@link #peek} this method not only return whether
    * the object is present in the cache or not, but also 
    * applies the implemented policy that will "refresh" the cached 
    * object in the cache, because this cached object
    * was really requested.
    * 
    * @param key the key paired with the object
    * @return the object
    * @see #peek
    */
   Object get(Object key);

   /**
    * Returns the object paired with the specified key if it's 
    * present in the cache, otherwise must return null. <br>
    * Implementations of this method must have complexity of order O(1).
    * This method should not apply the implemented caching policy to the 
    * object paired with the given key, so that a client can 
    * query if an object is cached without "refresh" its cache status. Real 
    * requests for the object must be done using {@link #get}.
    * 
    * @param key the key paired with the object
    * @see #get
    * @return the object
    */	
   Object peek(Object key);
   
   /**
    * Inserts the specified object into the cache following the 
    * implemented policy. <br>
    * Implementations of this method must have complexity of order O(1).
    * 
    * @param key the key paired with the object
    * @param object the object to cache
    * @see #remove
    */
   void insert(Object key, Object object);
   
   /**
    * Remove the cached object paired with the specified key. <br>
    * Implementations of this method must have complexity of order O(1).
    * 
    * @param key the key paired with the object
    * @see #insert
    */
   void remove(Object key);
   
   /**
    * Flushes the cached objects from the cache.
    */
   void flush();

   /**
    * @return the size of the cache
    */
   int size();



   void create() throws Exception;
   
   void start() throws Exception;
   
   void stop();
   
   void destroy();
}
