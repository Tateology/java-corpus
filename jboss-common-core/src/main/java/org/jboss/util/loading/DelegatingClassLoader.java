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
package org.jboss.util.loading;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * A URL classloader that delegates to its parent, avoiding
 * synchronization.
 *
 * A standard flag is provided so it can be used as a parent class,
 * but later subclassed and to revert to standard class loading
 * if the subclass wants to load classes.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public class DelegatingClassLoader
   extends URLClassLoader
{
   /** The value returned by {@link #getURLs()}. */
   public static final URL[] EMPTY_URL_ARRAY = {};

   /** Whether to use standard loading */
   protected boolean standard = false;

   /** Cache the parent*/
   private ClassLoader parent = null;

   /**
    * Constructor
    *
    * @param parent the parent classloader, cannot be null.
    */
   public DelegatingClassLoader(ClassLoader parent)
   {
      super(EMPTY_URL_ARRAY, parent);
      if (parent == null)
         throw new IllegalArgumentException("No parent");
      this.parent = parent;
   }

   /**
    * Constructor
    *
    * @param parent the parent classloader, cannot be null.
    * @param factory the url stream factory.
    */
   public DelegatingClassLoader(ClassLoader parent, URLStreamHandlerFactory factory)
   {
      super(EMPTY_URL_ARRAY, parent, factory);
      if (parent == null)
         throw new IllegalArgumentException("No parent");
      this.parent = parent;
   }

   /**
    * Load a class, by asking the parent
    *
    * @param className the class name to load
    * @param resolve whether to link the class
    * @return the loaded class
    * @throws ClassNotFoundException when the class could not be found
    */
   protected Class<?> loadClass(String className, boolean resolve)
      throws ClassNotFoundException
   {
      // Revert to standard rules
      if (standard)
         return super.loadClass(className, resolve);

      // Ask the parent
      Class<?> clazz = null;
      try
      {
         clazz = parent.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         // Not found in parent,
         // maybe it is a proxy registered against this classloader?
         clazz = findLoadedClass(className);
         if (clazz == null)
            throw e;
      }

      // Link the class
      if (resolve)
         resolveClass(clazz);

      return clazz;
   }
}
