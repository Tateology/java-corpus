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

package org.jboss.test.util.test.loading;

import org.jboss.util.loading.ClassLoaderSource;
import org.jboss.util.loading.ConstructorTCCLClassLoaderSource;

/**
 * Unit tests of {@link ConstructorTCCLClassLoaderSource}.
 *  
 * @author Brian Stansberry
 */
public class ConstructorTCCLClassLoaderSourceUnitTestCase extends ClassLoaderSourceTestBase
{

   public ConstructorTCCLClassLoaderSourceUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected ClassLoaderSource createClassLoaderSource()
   {
      return new ConstructorTCCLClassLoaderSource();
   }

   @Override
   protected ClassLoader getExpectedClassLoader(ClassLoader tccl)
   {
      return tccl;
   }
   
   /**
    * Tests that the ClassLoaderSource does not prevent garbage collection of
    * the classloader.
    */
   public void testGarbageCollectedClassLoader()
   {
      ClassLoader cl = getExtendedClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
      ClassLoaderSource cls = createClassLoaderSource();
      Thread.currentThread().setContextClassLoader(origCl);
      cl = null;
      
      System.gc();
      
      assertNull("ClassLoader garbage collected", cls.getClassLoader());
   }

}
