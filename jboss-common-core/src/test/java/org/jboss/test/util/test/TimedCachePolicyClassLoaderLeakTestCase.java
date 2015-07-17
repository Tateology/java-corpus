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

package org.jboss.test.util.test;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.util.TimedCachePolicy;

import junit.framework.TestCase;

/**
 * Test for JBCOMMON-50.
 * 
 * @author Brian Stansberry
 */
public class TimedCachePolicyClassLoaderLeakTestCase extends TestCase
{
   protected void tearDown() throws Exception
   {
      System.clearProperty(TimedCachePolicy.TIMER_CLASSLOADER_PROPERTY);
   }
   
   public void testUseSystemClassLoader() throws Exception
   {
      classLoaderLeaktoTimerTest(TimedCachePolicy.TIMER_CLASSLOADER_SYSTEM, false);
   }
   
   public void testUseCurrentClassLoader() throws Exception
   {
      classLoaderLeaktoTimerTest(TimedCachePolicy.TIMER_CLASSLOADER_CURRENT, false);
   }
   
   public void testUseContextClassLoader() throws Exception
   {
      // Here we expect to leak the CL
      classLoaderLeaktoTimerTest(TimedCachePolicy.TIMER_CLASSLOADER_CONTEXT, true);
   }
   
   public void testUseBogusClassLoader() throws Exception
   {
      classLoaderLeaktoTimerTest("bogus", false);
   }
   
   private void classLoaderLeaktoTimerTest(String timerCL, boolean expectLeak) throws Exception
   {
      ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader isolated = new IsolatedTimedCachePolicyClassLoader(origClassLoader);
      ClassLoader cl = new ClassLoader(isolated){};
      WeakReference<ClassLoader> clRef = new WeakReference<ClassLoader>(cl);
      Thread.currentThread().setContextClassLoader(cl);
      Object policy = null;
      try
      {
         policy = createTimedCachePolicy(timerCL);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(origClassLoader);
      }
      
      cl = null;
      System.gc();
      if (expectLeak)
      {
         assertNotNull("ClassLoader not collected", clRef.get());
      }
      else
      {
         assertNull("ClassLoader collected", clRef.get());
      }
      
      if (policy != null)
      {
         destroyTimedCachePolicy(policy);
      }
   }
   
   private static Object createTimedCachePolicy(String timerCL) throws Exception
   {
      System.setProperty(TimedCachePolicy.TIMER_CLASSLOADER_PROPERTY, timerCL);
      Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("org.jboss.util.TimedCachePolicy");
      Object obj = clazz.newInstance();
      Method create = clazz.getDeclaredMethod("create", new Class[0]);
      Method start = clazz.getDeclaredMethod("start", new Class[0]);
      Method insert = clazz.getDeclaredMethod("insert", new Class[]{Object.class, Object.class});
      create.invoke(obj, new Object[0]);
      start.invoke(obj, new Object[0]);
      insert.invoke(obj, new Object[]{new Object(), new Object()});
      return obj;
   }
   
   private static void destroyTimedCachePolicy(Object policy) throws Exception
   {
      Class<?> clazz = policy.getClass();
      Method stop = clazz.getDeclaredMethod("stop", new Class[0]);
      Method destroy = clazz.getDeclaredMethod("destroy", new Class[0]);
      stop.invoke(policy, new Object[0]);
      destroy.invoke(policy, new Object[0]);
      
   }
   
   private static class IsolatedTimedCachePolicyClassLoader extends ClassLoader
   {      
      private Map<String, Class<?>> clazzes = new HashMap<String, Class<?>>();

      public IsolatedTimedCachePolicyClassLoader()
      {
         this(Thread.currentThread().getContextClassLoader());
      }
      
      public IsolatedTimedCachePolicyClassLoader(ClassLoader parent)
      {
         super(parent);
      }
      
      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
      {
         if (name.startsWith("org.jboss.util.TimedCachePolicy"))
         {
            Class<?> c = findClass(name);
            if (resolve)
            {
               resolveClass(c);
            }
            return c;
         }
         else
         {
            return super.loadClass(name, resolve);
         }
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException
      {
         if (name.startsWith("org.jboss.util.TimedCachePolicy"))
         {
            Class<?> clazz = clazzes.get(name);
            if (clazz == null)
            {
               String path = name.replace('.', '/').concat(".class");
               InputStream stream = getParent().getResourceAsStream(path);
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               byte[] input = new byte[1024];
               int read = 0;
               try
               {
                  while ((read = stream.read(input)) > -1)
                  {
                     baos.write(input, 0, read);
                  }
               }
               catch (IOException e)
               {
                  throw new RuntimeException(e);
               }
               byte[] bytes = baos.toByteArray();
               clazz = defineClass(name, bytes, 0, bytes.length);
               clazzes.put(name, clazz);
            }
            return clazz;
         }
         else
         {
            return super.findClass(name);
         }
      }      
      
   }

}
