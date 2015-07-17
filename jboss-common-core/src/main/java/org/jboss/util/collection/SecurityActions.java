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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
class SecurityActions
{
   
   interface GetClassLoaderAction 
   {
      ClassLoader getClassLoader(Class<?> clazz);
      
      GetClassLoaderAction NON_PRIVILEGED = new GetClassLoaderAction() {

         public ClassLoader getClassLoader(Class<?> clazz)
         {
            return clazz.getClassLoader();
         }};

     GetClassLoaderAction PRIVILEGED = new GetClassLoaderAction() {

         public ClassLoader getClassLoader(final Class<?> clazz)
         {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

               public ClassLoader run()
               {
                  return clazz.getClassLoader();
               }});
         }};
   }
   
   static ClassLoader getClassLoader(Class<?> clazz)
   {
      if (System.getSecurityManager() == null)
      {
         return GetClassLoaderAction.NON_PRIVILEGED.getClassLoader(clazz);
      }
      else
      {
         return GetClassLoaderAction.PRIVILEGED.getClassLoader(clazz);
      }
   }
}
