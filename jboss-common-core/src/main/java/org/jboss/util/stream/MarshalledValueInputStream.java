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
package org.jboss.util.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * An ObjectInputStream subclass used by the MarshalledValue class to
 * ensure the classes and proxies are loaded using the thread context
 * class loader.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 37406 $
 */
public class MarshalledValueInputStream
      extends ObjectInputStream
{
   private static final Map<String, Class> primClasses = new HashMap<String, Class>(8, 1.0F);

   static
   {
      primClasses.put("boolean", boolean.class);
      primClasses.put("byte", byte.class);
      primClasses.put("char", char.class);
      primClasses.put("short", short.class);
      primClasses.put("int", int.class);
      primClasses.put("long", long.class);
      primClasses.put("float", float.class);
      primClasses.put("double", double.class);
      primClasses.put("void", void.class);
   }

   /**
    * Creates a new instance of MarshalledValueOutputStream
    *
    * @param is
    * @throws IOException
    */
   public MarshalledValueInputStream(InputStream is) throws IOException
   {
      super(is);
   }

   /**
    * Use the thread context class loader to resolve the class
    *
    * @throws java.io.IOException Any exception thrown by the underlying OutputStream.
    */
   protected Class<?> resolveClass(ObjectStreamClass v)
         throws IOException, ClassNotFoundException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      String className = v.getName();
      try
      {
         // JDK 6, by default, only supports array types (ex. [[B)  using Class.forName()
         return Class.forName(className, false, loader);
      }
      catch (ClassNotFoundException cnfe)
      {
         Class cl = primClasses.get(className);
         if (cl == null)
            throw cnfe;
         else
            return cl;
      }
   }

   protected Class<?> resolveProxyClass(String[] interfaces)
         throws IOException, ClassNotFoundException
   {
      // Load the interfaces from the thread context class loader
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?>[] ifaceClasses = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; i++)
      {
         ifaceClasses[i] = loader.loadClass(interfaces[i]);
      }

      return java.lang.reflect.Proxy.getProxyClass(loader, ifaceClasses);
   }
}
