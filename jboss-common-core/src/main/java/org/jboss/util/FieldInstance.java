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

import java.lang.reflect.Field;

/**
 * A <tt>FieldInstance</tt> refers to a specific reflected field on an object.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FieldInstance
{
   /** Field */
   protected final Field field;

   /** Instance */
   protected final Object instance;

   /**
    * Construct a new field instance.
    *
    * @param instance      The instance object the given field belongs to.
    * @param fieldName     The name of the field to find in the instance.
    *
    * @throws NullArgumentException    Instance or fieldName is <tt>null</tt>.
    * @throws NoSuchFieldException
    */
   public FieldInstance(final Object instance, final String fieldName)
      throws NoSuchFieldException
   {
      if (instance == null)
         throw new NullArgumentException("instance");
      if (fieldName == null)
         throw new NullArgumentException("fieldName");

      // Get the field object
      field = instance.getClass().getField(fieldName);

      // Check if the field is assignable ?
      if (! field.getDeclaringClass().isAssignableFrom(instance.getClass()))
         throw new IllegalArgumentException
            ("field does not belong to instance class");

      this.instance = instance;
   }

   /**
    * Get the field.
    *
    * @return  Field.
    */
   public final Field getField() {
      return field;
   }

   /**
    * Get the instance.
    *
    * @return  Instance.
    */
   public final Object getInstance() {
      return instance;
   }

   /**
    * Get the value of the field instance.
    *
    * @return  Field value.
    *
    * @throws IllegalAccessException      Failed to get field value.
    */ 
   public final Object get() throws IllegalAccessException {
      return field.get(instance);
   }

   /**
    * Set the value of the field instance
    *
    * @param value   Field value.
    *
    * @throws IllegalAccessException      Failed to set field value.
    */
   public final void set(final Object value) throws IllegalAccessException {
      field.set(instance, value);
   }
}
