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
package org.jboss.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Array;

import java.lang.ref.Reference;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.jboss.util.stream.Streams;

/**
 * A collection of <code>Object</code> utilities.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@SuppressWarnings("unchecked")
public final class Objects
{
   /////////////////////////////////////////////////////////////////////////
   //                           Coercion Methods                          //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Get a compatible constructor for the given value type
    *
    * @param type       Class to look for constructor in
    * @param valueType  Argument type for constructor
    * @return           Constructor or null
    */
   public static Constructor getCompatibleConstructor(final Class type,
                                                      final Class valueType)
   {
      // first try and find a constructor with the exact argument type
      try {
         return type.getConstructor(new Class[] { valueType });
      }
      catch (Exception ignore) {
         // if the above failed, then try and find a constructor with
         // an compatible argument type

         // get an array of compatible types
         Class[] types = type.getClasses();

         for (int i=0; i<types.length; i++) {
            try {
               return type.getConstructor(new Class[] { types[i] });
            }
            catch (Exception ignore2) {}
         }
      }

      // if we get this far, then we can't find a compatible constructor
      return null;
   }

   /////////////////////////////////////////////////////////////////////////
   //                            Cloning Methods                          //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Copy an serializable object deeply.
    *
    * @param obj  Object to copy.
    * @return     Copied object.
    *
    * @throws IOException
    * @throws ClassNotFoundException
    */
   public static Object copy(final Serializable obj)
      throws IOException, ClassNotFoundException
   {
      ObjectOutputStream out = null;
      ObjectInputStream in = null;
      Object copy = null;
      
      try {
         // write the object
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         out = new ObjectOutputStream(baos);
         out.writeObject(obj);
         out.flush();

         // read in the copy
         byte data[] = baos.toByteArray();
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         in = new ObjectInputStream(bais);
         copy = in.readObject();
      }
      finally {
         Streams.close(out);
         Streams.close(in);
      }

      return copy;
   }
   

   /////////////////////////////////////////////////////////////////////////
   //                              Misc Methods                           //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Dereference the given object if it is <i>non-null</i> and is an
    * instance of <code>Reference</code>.  If the object is <i>null</i>
    * then <i>null</i> is returned.  If the object is not an instance of
    * <code>Reference</code>, then the object is returned.
    *
    * @param obj  Object to dereference.
    * @return     Dereferenced object.
    */
   @SuppressWarnings("unchecked")
   public static Object deref(final Object obj) {
      if (obj != null && obj instanceof Reference) {
         Reference ref = (Reference)obj;
         return ref.get();
      }

      return obj;
   }

   /**
    * Dereference an object
    * 
    * @param <T> the expected type
    * @param obj the object or reference
    * @param expected the expected type
    * @return the object or null
    */
   public static <T> T deref(final Object obj, Class<T> expected)
   {
      Object result = deref(obj);
      if (result == null)
         return null;
      return expected.cast(result);
   }
   
   /**
    * Check if the given object is an array (primitve or native).
    *
    * @param obj  Object to test.
    * @return     True of the object is an array.
    */
   public static boolean isArray(final Object obj) {
      if (obj != null)
         return obj.getClass().isArray();
      return false;
   }

   /**
    * @return an Object array for the given object.
    *
    * @param obj  Object to convert to an array.  Converts primitive
    *             arrays to Object arrays consisting of their wrapper
    *             classes.  If the object is not an array (object or primitve)
    *             then a new array of the given type is created and the
    *             object is set as the sole element.
    */
   public static Object[] toArray(final Object obj) {
      // if the object is an array, the cast and return it.
      if (obj instanceof Object[]) {
         return (Object[])obj;
      }

      // if the object is an array of primitives then wrap the array
      Class type = obj.getClass();
      Object array; 
      if (type.isArray()) {
         int length = Array.getLength(obj);
         Class componentType = type.getComponentType();
         array = Array.newInstance(componentType, length);
         for (int i=0; i<length; i++) {
            Array.set(array, i, Array.get(obj, i));
         }
      }
      else {
         array = Array.newInstance(type, 1);
         Array.set(array, 0, obj);
      }

      return (Object[])array;
   }

   /**
    * Test the equality of two object arrays.
    *
    * @param a       The first array.
    * @param b       The second array.
    * @param deep    True to traverse elements which are arrays.
    * @return        True if arrays are equal.
    */
   public static boolean equals(final Object[] a, final Object[] b,
                                final boolean deep)
   {
      if (a == b) return true;
      if (a == null || b == null) return false;
      if (a.length != b.length) return false;

      for (int i=0; i<a.length; i++) {
         Object x = a[i];
         Object y = b[i];

         if (x != y) return false;
         if (x == null || y == null) return false;
         if (deep) {
            if (x instanceof Object[] && y instanceof Object[]) {
               if (! equals((Object[])x, (Object[])y, true)) return false;
            }
            else {
               return false;
            }
         }
         if (! x.equals(y)) return false;
      }

      return true;
   }

   /**
    * Test the equality of two object arrays.
    *
    * @param a    The first array.
    * @param b    The second array.
    * @return     True if arrays are equal.
    */
   public static boolean equals(final Object[] a, final Object[] b) {
      return equals(a, b, true);
   }

   /**
    * Test the equality of two objects.
    * They can be an (multi dimensional) array.
    *
    * @param first the first obejct
    * @param second the second object
    * @return true if equal, false otherwise
    */
   public static boolean equals(Object first, Object second)
   {
      if (isArray(first))
      {
         if (isArray(second) == false)
            return false;

         int lenght = Array.getLength(first);
         if (lenght != Array.getLength(second))
            return false;

         for (int i = 0; i < lenght; i++)
         {
            if (equals(Array.get(first, i), Array.get(second, i)) == false)
               return false;
         }

         return true;
      }
      else
      {
         return JBossObject.equals(first, second);
      }
   }
}
