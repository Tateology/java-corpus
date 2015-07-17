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

import java.lang.ref.WeakReference;
import java.security.AccessController;

/**
 * {@link ClassLoaderSource} implementation that returns the 
 * {@link Thread#getContextClassLoader() thread context classloader (TCCL)}
 * in effect when this class' constructor is invoked.
 * 
 * @author Brian Stansberry
 */
@SuppressWarnings("unchecked")
public class ConstructorTCCLClassLoaderSource implements ClassLoaderSource
{
   private final WeakReference classLoaderRef;
   
   public ConstructorTCCLClassLoaderSource()
   {      
      ContextClassLoader ccl = (ContextClassLoader) AccessController.doPrivileged(ContextClassLoader.INSTANTIATOR);
      ClassLoader cl = ccl.getContextClassLoader();
      
      if (cl != null)
         classLoaderRef = new WeakReference(cl);
      else
         classLoaderRef = null;
   }
   
   public ClassLoader getClassLoader()
   {      
      return classLoaderRef == null ? null : (ClassLoader) classLoaderRef.get();
   }

}
