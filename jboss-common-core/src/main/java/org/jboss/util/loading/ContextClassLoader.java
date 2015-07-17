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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A helper for context classloading.<p>
 *
 * When a security manager is installed, the
 * constructor checks for the runtime permissions
 * &quot;getClassLoader&quot;
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 */
@SuppressWarnings("unchecked")
public class ContextClassLoader
{
   /**
    * Retrieve a classloader permission
    */
   public static final RuntimePermission GETCLASSLOADER = new RuntimePermission("getClassLoader");

   /**
    * Instantiate a new context class loader
    */
   public static final NewInstance INSTANTIATOR = new NewInstance();

   /**
    * Constructor.
    * 
    * @throws SecurityException when not authroized to get the context classloader
    */
   /*package*/ ContextClassLoader()
   {
      SecurityManager manager = System.getSecurityManager();
      if (manager != null)
      {
         manager.checkPermission(GETCLASSLOADER);
      }
   }

   /**
    * Retrieve the context classloader
    *
    * @return the context classloader
    */
   public ClassLoader getContextClassLoader()
   {
      return getContextClassLoader(Thread.currentThread());
   }

   /**
    * Retrieve the context classloader for the given thread
    *
    * @param thread the thread
    * @return the context classloader
    */
   public ClassLoader getContextClassLoader(final Thread thread)
   {
      return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            return thread.getContextClassLoader();
         }
      });
   }

   private static class NewInstance
      implements PrivilegedAction
   {
      public Object run()
      {
         return new ContextClassLoader();
      }
   }
}
