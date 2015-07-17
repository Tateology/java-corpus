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
import java.util.Map;
import java.util.Iterator;

import java.io.IOException;

import org.jboss.util.ThrowableHandler;

/**
 * A more robust replacement of <tt>java.lang.System</tt> for property
 * access.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
@SuppressWarnings("unchecked")
public final class PropertyManager
{
   /** Property reader list property name */
   public static final String READER_PROPERTY_NAME = "org.jboss.util.property.reader";

   /** Token which specifies the default property reader */
   public static final String DEFAULT_PROPERTY_READER_TOKEN = "DEFAULT";

   /** The default property reader name array */
   private static final String[] DEFAULT_PROPERTY_READERS = { DEFAULT_PROPERTY_READER_TOKEN };

   /** Default property container */
   private static PropertyMap props;

   /**
    * Do not allow instantiation of this class.
    */
   private PropertyManager()
   {
   }

   /**
    * Initialize the property system.
    */
   static
   {
      // construct default property container and initialze from system props
      props = new PropertyMap();
      PrivilegedAction action = new PrivilegedAction()
      {
         public Object run()
         {
            props.putAll(System.getProperties());

            // replace system props to enable notifications via System.setProperty()
            System.setProperties(props);

            // load properties from initial property readers
            String[] readerNames = getArrayProperty(READER_PROPERTY_NAME, DEFAULT_PROPERTY_READERS);

            // construct each source and read its properties
            for (int i = 0; i < readerNames.length; i++)
            {
               try
               {
                  if (readerNames[i].equals(DEFAULT_PROPERTY_READER_TOKEN))
                  {
                     load(new DefaultPropertyReader());
                  }
                  else
                  {
                     load(readerNames[i]);
                  }
               }
               catch (IOException e)
               {
                  ThrowableHandler.add(e);
               }
            }
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * Get the default <tt>PropertyMap</tt>.
    *
    * @return  Default <tt>PropertyMap</tt>.
    */
   public static PropertyMap getDefaultPropertyMap()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      return props;
   }

   /**
    * Add a property listener.
    *
    * @param listener   Property listener to add.
    */
   public static void addPropertyListener(final PropertyListener listener)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.addPropertyListener(listener);
   }

   /**
    * Add an array of property listeners.
    *
    * @param listeners     Array of property listeners to add.
    */
   public static void addPropertyListeners(final PropertyListener[] listeners)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.addPropertyListeners(listeners);
   }

   /**
    * Remove a property listener.
    *
    * @param listener   Property listener to remove.
    * @return           True if listener was removed.
    */
   public static boolean removePropertyListener(final PropertyListener listener)
   {
      return props.removePropertyListener(listener);
   }

   /**
    * Load properties from a map.
    *
    * @param prefix  Prefix to append to all map keys (or <tt>null</tt>).
    * @param map     Map containing properties to load.
    * @throws PropertyException
    */
   public static void load(final String prefix, final Map map) throws PropertyException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.load(prefix, map);
   }

   /**
    * Load properties from a map.
    *
    * @param map  Map containing properties to load.
    * @throws PropertyException 
    * @throws IOException 
    */
   public static void load(final Map map) throws PropertyException, IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.load(map);
   }

   /**
    * Load properties from a <tt>PropertyReader</tt>.
    *
    * @param reader  <tt>PropertyReader</tt> to read properties from.
    * @throws PropertyException 
    * @throws IOException 
    */
   public static void load(final PropertyReader reader) throws PropertyException, IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.load(reader);
   }

   /**
    * Load properties from a <tt>PropertyReader</tt> specifed by the 
    * given class name.
    *
    * @param classname     Class name of a <tt>PropertyReader</tt> to 
    *                      read from.
    * @throws PropertyException 
    * @throws IOException 
    */
   public static void load(final String classname) throws PropertyException, IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      props.load(classname);
   }

   /**
    * Set a property.
    *
    * @param name    Property name.
    * @param value   Property value.
    * @return        Previous property value or <tt>null</tt>.
    */
   public static String setProperty(final String name, final String value)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return (String) props.setProperty(name, value);
   }

   /**
    * Remove a property.
    *
    * @param name    Property name.
    * @return        Removed property value or <tt>null</tt>.
    */
   public static String removeProperty(final String name)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return props.removeProperty(name);
   }

   /**
    * Get a property.
    *
    * @param name          Property name.
    * @param defaultValue  Default property value.
    * @return              Property value or default.
    */
   public static String getProperty(final String name, final String defaultValue)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return props.getProperty(name, defaultValue);
   }

   /**
    * Get a property.
    *
    * @param name       Property name.
    * @return           Property value or <tt>null</tt>.
    */
   public static String getProperty(final String name)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return props.getProperty(name);
   }

   /**
    * Get an array style property.
    * 
    * @param base             Base property name.
    * @param defaultValues    Default property values.
    * @return                 Array of property values or default.
    */
   public static String[] getArrayProperty(final String base, final String[] defaultValues)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      return props.getArrayProperty(base, defaultValues);
   }

   /**
    * Get an array style property.
    *
    * @param name       Property name.
    * @return           Array of property values or empty array.
    */
   public static String[] getArrayProperty(final String name)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return props.getArrayProperty(name);
   }

   /**
    * Return an iterator over all contained property names.
    *
    * @return     Property name iterator.
    */
   public static Iterator names()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      return props.names();
   }

   /**
    * Check if this map contains a given property.
    *
    * @param name    Property name.
    * @return        True if contains property.
    */
   public static boolean containsProperty(final String name)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertyAccess(name);
      return props.containsProperty(name);
   }

   /**
    * Get a property group for the given property base.
    *
    * @param basename   Base property name.
    * @return           Property group.
    */
   public static PropertyGroup getPropertyGroup(final String basename)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      return props.getPropertyGroup(basename);
   }

   /**
    * Get a property group for the given property base at the given index.
    *
    * @param basename   Base property name.
    * @param index      Array property index.
    * @return           Property group.
    */
   public static PropertyGroup getPropertyGroup(final String basename, final int index)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPropertiesAccess();
      return props.getPropertyGroup(basename, index);
   }
}
