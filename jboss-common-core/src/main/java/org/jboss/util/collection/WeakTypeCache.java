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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
 * @param <T> the cached type
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 */
public abstract class WeakTypeCache<T>
{
   /** The cache */
   private Map<ClassLoader, Map<String, T>> cache = new WeakHashMap<ClassLoader, Map<String, T>>(); 
   
   /**
    * Get the information for a type
    * 
    * @param type the type
    * @return the info
    */
   @SuppressWarnings({"unchecked", "cast"})
   public T get(Type type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");

      if (type instanceof ParameterizedType)
         return getParameterizedType((ParameterizedType) type);
      else if (type instanceof Class)
         return getClass((Class<?>) type);
      else if (type instanceof TypeVariable)
         // TODO Figure out why we need this cast with the Sun compiler? 
         return (T) getTypeVariable((TypeVariable) type);
      else if (type instanceof GenericArrayType)
         return getGenericArrayType((GenericArrayType) type);
      else if (type instanceof WildcardType)
         return getWildcardType((WildcardType) type);
      else
         throw new UnsupportedOperationException("Unknown type: " + type + " class=" + type.getClass());
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
    * Instantiate for a parameterized type
    * 
    * @param type the parameterized type
    * @return the result
    */
   protected abstract T instantiate(ParameterizedType type);
   
   /**
    * Fill in the result
    * 
    * @param type the parameterized type
    * @param result the result
    */
   protected abstract void generate(ParameterizedType type, T result);

   /**
    * Get the information for a parameterized type
    * 
    * @param type the parameterized type
    * @return the info
    */
   protected T getParameterizedType(ParameterizedType type)
   {
      // First check if we already have it
      T result = peek(type);
      if (result != null)
         return result;
      
      // Instantiate
      result = instantiate(type);

      // Put the perlimanary result into the cache
      put(type, result);

      // Generate the details
      generate(type, result);
      
      return result;
   }

   /**
    * Get the information for a wildcard type
    * 
    * @param type the paremeterized type
    * @return the info
    */
   protected T getWildcardType(WildcardType type)
   {
      // TODO JBMICROCONT-131 improve this
      return get(type.getUpperBounds()[0]);
   }

   /**
    * Get the information for a type variable
    * 
    * @param <D> the declaration
    * @param type the type variable
    * @return the info
    */
   protected <D extends GenericDeclaration> T getTypeVariable(TypeVariable<D> type)
   {
      // TODO JBMICROCONT-131 improve this
      return get(type.getBounds()[0]);
   }

   /**
    * Get the information for an array type
    * 
    * @param type the array type
    * @return the info
    */
   protected T getGenericArrayType(GenericArrayType type)
   {
      // TODO JBMICROCONT-131 this needs implementing properly
      return get(Object[].class);
   }

   /**
    * Peek into the cache
    * 
    * @param type the type
    * @return the value
    */
   protected T peek(ParameterizedType type)
   {
      Class<?> rawType = (Class<?>) type.getRawType();
      ClassLoader cl = SecurityActions.getClassLoader(rawType);
      Map<String, T> classLoaderCache = getClassLoaderCache(cl);
      
      synchronized (classLoaderCache)
      {
         return classLoaderCache.get(type.toString());
      }
   }

   /**
    * Put a result into the cache
    * 
    * @param type the type
    * @param result the value
    */
   protected void put(ParameterizedType type, T result)
   {
      Class<?> rawType = (Class<?>) type.getRawType();
      ClassLoader cl = SecurityActions.getClassLoader(rawType);
      Map<String, T> classLoaderCache = getClassLoaderCache(cl);

      synchronized (classLoaderCache)
      {
         // TODO JBMICROCONT-131 something better than toString()?
         classLoaderCache.put(type.toString(), result);
      }
   }

   /**
    * Get the information for a class
    * 
    * @param clazz the class
    * @return the info
    */
   protected T getClass(Class<?> clazz)
   {
      // First check if we already have it
      T result = peek(clazz);
      if (result != null)
         return result;

      // Instantiate
      result = instantiate(clazz);

      // Put the preliminary result into the cache
      put(clazz, result);

      // Generate the details
      generate(clazz, result);
      
      return result;
   }

   /**
    * Peek into the cache
    * 
    * @param clazz the class
    * @return the value
    */
   protected T peek(Class<?> clazz)
   {
      ClassLoader cl = SecurityActions.getClassLoader(clazz);
      Map<String, T> classLoaderCache = getClassLoaderCache(cl);

      synchronized (classLoaderCache)
      {
         return classLoaderCache.get(clazz.getName());
      }
   }

   /**
    * Put a result into the cache
    * 
    * @param clazz the class
    * @param result the value
    */
   protected void put(Class<?> clazz, T result)
   {
      ClassLoader cl = SecurityActions.getClassLoader(clazz);
      Map<String, T> classLoaderCache = getClassLoaderCache(cl);

      synchronized (classLoaderCache)
      {
         classLoaderCache.put(clazz.getName(), result);
      }
   }
   
   /**
    * Get the cache for the classloader
    * 
    * @param cl the classloader
    * @return the map
    */
   protected Map<String, T> getClassLoaderCache(ClassLoader cl)
   {
      synchronized (cache)
      {
         Map<String, T> result = cache.get(cl);
         if (result == null)
         {
            result = new WeakValueHashMap<String, T>();
            cache.put(cl, result);
         }
         return result;
      }
   }
}
