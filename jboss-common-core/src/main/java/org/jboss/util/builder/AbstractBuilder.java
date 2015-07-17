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
package org.jboss.util.builder;

// $Id: $

import java.security.PrivilegedAction;

/**
 * AbstractBuilder.
 * 
 * @param <T> the type to be built
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 1.1 $
 */
public class AbstractBuilder<T> implements PrivilegedAction<T>
{
   /** The factory class */
   private Class<T> factoryClass;
   
   /** The default factory */
   private String defaultFactory;
   
   /**
    * Create a new AbstractBuilder.
    * 
    * @param factoryClass the factory class
    * @param defaultFactory the default factory
    * @throws IllegalArgumentException for a null parameter
    */
   public AbstractBuilder(Class<T> factoryClass, String defaultFactory)
   {
      if (factoryClass == null)
         throw new IllegalArgumentException("Null factory class");
      if (defaultFactory == null)
         throw new IllegalArgumentException("Null default factory");
      this.factoryClass = factoryClass;
      this.defaultFactory = defaultFactory;
   }
   
   public T run()
   {
      Object object;
      try
      {
         String className = System.getProperty(factoryClass.getName(), defaultFactory);
         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
         object = clazz.newInstance();
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error constructing " + factoryClass.getName(), t);
      }
      
      // Cast the factory instance and report a potential ClassLoader problem
      T retObj;
      try
      {
         retObj = factoryClass.cast(object);
      }
      catch (ClassCastException ex)
      {
         String objClassName = object.getClass().getName();
         ClassLoader objLoader = object.getClass().getClassLoader();
         
         String factoryClassName = factoryClass.getName();
         ClassLoader factoryLoader = factoryClass.getClassLoader();
         
         String msg = "Cannot cast object '" + objClassName + "' to factory '" + factoryClassName + "'\n" + 
         "  factoryLoader: " + factoryLoader + "\n" + 
         "  objLoader: " + objLoader;
         
         throw new RuntimeException(msg);
      }
      
      return retObj;
   }
}
