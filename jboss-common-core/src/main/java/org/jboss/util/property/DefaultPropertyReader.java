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

/**
 * Reads properties from files specified via a system property.
 *
 * <p>Unless otherwise specified, propertie filenames will be read from
 *    the <tt>org.jboss.properties</tt> singleton or array property.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class DefaultPropertyReader
   extends FilePropertyReader
{
   //
   // Might want to have a org.jboss.properties.property.name or something
   // property to determine what property name to read from.
   //
   // For now just use 'properties'
   //

   /** Default property name to read filenames from */
   public static final String DEFAULT_PROPERTY_NAME = "properties";

   /**
    * Construct a <tt>DefaultPropertyReader</tt> with a specified property 
    * name.
    *
    * @param propertyName    Property name.
    */
   public DefaultPropertyReader(final String propertyName) {
      super(getFilenames(propertyName));
   }

   /**
    * Construct a <tt>DefaultPropertyReader</tt>.
    */
   public DefaultPropertyReader() {
      this(DEFAULT_PROPERTY_NAME);
   }
   
   /**
    * Get an array of filenames to load.
    *
    * @param propertyName  Property to read filenames from.
    * @return              Array of filenames.
    * @throws PropertyException
    */
   public static String[] getFilenames(final String propertyName)
      throws PropertyException
   {
      String filenames[];

      // check for singleton property first
      Object filename = PropertyManager.getProperty(propertyName);
      if (filename != null) {
         filenames = new String[] { String.valueOf(filename) };
      }
      else {
         // if no singleton property exists then look for array props
         filenames = PropertyManager.getArrayProperty(propertyName);
      }

      return filenames;
   }
}
