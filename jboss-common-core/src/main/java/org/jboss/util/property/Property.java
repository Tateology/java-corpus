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
package org.jboss.util.property;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Provides shorter method names for working with the {@link PropertyManager}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
@SuppressWarnings("unchecked")
public final class Property
{
   /** Platform dependent line separator. */
   public static String LINE_SEPARATOR;

   /** Platform dependant file separator. */
   public static String FILE_SEPARATOR;

   /** Platform dependant path separator. */
   public static String PATH_SEPARATOR;
   
   static
   {
      PrivilegedAction action = new PrivilegedAction()
      {
         public Object run()
         {
            LINE_SEPARATOR = Property.get("line.separator");
            FILE_SEPARATOR = Property.get("file.separator");
            PATH_SEPARATOR = Property.get("path.separator");
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }
   
   /**
    * Add a property listener
    *
    * @param listener   Property listener to add
    */
   public static void addListener(PropertyListener listener)
   {
      PropertyManager.addPropertyListener(listener);
   }

   /**
    * Add an array of property listeners
    *
    * @param listeners     Array of property listeners to add
    */
   public static void addListeners(PropertyListener[] listeners)
   {
      PropertyManager.addPropertyListeners(listeners);
   }

   /**
    * Remove a property listener
    *
    * @param listener   Property listener to remove
    * @return           True if listener was removed
    */
   public static boolean removeListener(PropertyListener listener)
   {
      return PropertyManager.removePropertyListener(listener);
   }

   /**
    * Set a property
    *
    * @param name    Property name
    * @param value   Property value
    * @return        Previous property value or null
    */
   public static String set(String name, String value)
   {
      return PropertyManager.setProperty(name, value);
   }

   /**
    * Remove a property
    *
    * @param name    Property name
    * @return        Removed property value or null
    */
   public static String remove(String name)
   {
      return PropertyManager.getProperty(name);
   }

   /**
    * Get a property
    *
    * @param name          Property name
    * @param defaultValue  Default property value
    * @return              Property value or default
    */
   public static String get(String name, String defaultValue)
   {
      return PropertyManager.getProperty(name, defaultValue);
   }

   /**
    * Get a property
    *
    * @param name       Property name
    * @return           Property value or null
    */
   public static String get(String name)
   {
      return PropertyManager.getProperty(name);
   }

   /**
    * Get an array style property
    * 
    * @param base          Base property name
    * @param defaultValues Default property values
    * @return              Array of property values or default
    */
   public static String[] getArray(String base, String[] defaultValues)
   {
      return PropertyManager.getArrayProperty(base, defaultValues);
   }

   /**
    * Get an array style property
    *
    * @param name       Property name
    * @return           Array of property values or empty array
    */
   public static String[] getArray(String name)
   {
      return PropertyManager.getArrayProperty(name);
   }

   /**
    * Check if a property of the given name exists.
    *
    * @param name    Property name
    * @return        True if property exists
    */
   public static boolean exists(String name)
   {
      return PropertyManager.containsProperty(name);
   }

   /**
    * Get a property group for the given property base
    *
    * @param basename   Base property name
    * @return           Property group
    */
   public static PropertyGroup getGroup(String basename)
   {
      return PropertyManager.getPropertyGroup(basename);
   }

   /**
    * Get a property group for the given property base at the given index
    *
    * @param basename   Base property name
    * @param index      Array property index
    * @return           Property group
    */
   public static PropertyGroup getGroup(String basename, int index)
   {
      return PropertyManager.getPropertyGroup(basename, index);
   }
}
