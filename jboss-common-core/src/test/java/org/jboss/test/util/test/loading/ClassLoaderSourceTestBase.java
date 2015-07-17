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

import junit.framework.TestCase;
import org.jboss.util.loading.ClassLoaderSource;

/**
 * Base class for testing {@link ClassLoaderSource} implementations.
 * 
 * @author Brian Stansberry
 */
public abstract class ClassLoaderSourceTestBase extends TestCase
{
   protected ClassLoader origCl;
   
   /**
    * @param name
    */
   public ClassLoaderSourceTestBase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      origCl = Thread.currentThread().getContextClassLoader();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(origCl);
      }
   }
   
   protected abstract ClassLoaderSource createClassLoaderSource();
   protected abstract ClassLoader getExpectedClassLoader(ClassLoader tccl);
   
   /**
    * Tests that the ClassLoaderSource returns the expected classloader
    * when the TCCL is null.
    */
   public void testNullTCCL()
   {
      checkCorrectClassLoaderSource(null);
   }
   
   /**
    * Tests that the ClassLoaderSource returns the expected classloader
    * when the TCCL is the basic one in place when this test is executed.
    */
   public void testOriginalTCCL()
   {
      checkCorrectClassLoaderSource(origCl);
   }
   
   /**
    * Tests that the ClassLoaderSource returns the expected classloader
    * when the TCCL is the CLS impl's own classloader.
    */
   public void testImplClassLoader()
   {
      checkCorrectClassLoaderSource(createClassLoaderSource().getClass().getClassLoader());
   }
   
   /**
    * Tests that the ClassLoaderSource returns the expected classloader
    * when the TCCL is a child classloader.
    */
   public void testDifferentTCCL()
   {
      checkCorrectClassLoaderSource(getExtendedClassLoader());
   }
   
   protected void checkCorrectClassLoaderSource(ClassLoader tccl)
   {
      Thread.currentThread().setContextClassLoader(tccl);
      ClassLoaderSource cls = createClassLoaderSource();
      assertSame("ClassLoaderSource returned expected cl", getExpectedClassLoader(tccl), cls.getClassLoader());
   }
   
   protected ClassLoader getExtendedClassLoader()
   {
      return new ExtendedClassLoader(origCl);
   }
   
   private class ExtendedClassLoader extends ClassLoader
   {
      ExtendedClassLoader(ClassLoader parent)
      {
         super(parent);
      }
   }

}
