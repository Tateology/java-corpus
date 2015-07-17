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

import java.util.Properties;

/**
 * Provides helper methods for working with instance or class properties.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PropertyContainer
   extends PropertyMap
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -3347198703863412326L;
   /** The group name for this container. */
   protected String groupname = "<unknown>";

   /**
    * Initialize the container with a property group.
    * @param props 
    */
   public PropertyContainer(final Properties props) {
      super(props);
   }

   /**
    * Initialize the container with a property group of the given name.
    *
    * @param groupname  Property group name.
    */
   public PropertyContainer(final String groupname) {
      this(Property.getGroup(groupname));
      this.groupname = groupname;
   }

   /**
    * Initialize the container with a property group of the given class name.
    *
    * @param type    The class whos name will be the property group name.
    */
   public PropertyContainer(final Class<?> type) {
      this(type.getName());
   }
   
   /**
    * Creates a {@link FieldBoundPropertyListener} for the field and
    * property name and adds it the underlying property group.
    *
    * @param name          The field name to bind values to.
    * @param propertyName  The property name to bind to.
    *
    * @throws IllegalArgumentException    Field of property name is null or 
    *                                     empty.
    */
   protected void bindField(final String name, final String propertyName) {
      if (name == null || name.equals(""))
         throw new IllegalArgumentException("name");
      if (propertyName == null || propertyName.equals(""))
         throw new IllegalArgumentException("propertyName");

      addPropertyListener
         (new FieldBoundPropertyListener(this, name, propertyName));
   }

   /**
    * Creates a {@link FieldBoundPropertyListener} for the field and
    * property name and adds it the underlying property group.
    *
    * @param name    The field name and property to bind values to.
    *
    * @throws IllegalArgumentException    Field of property name is null or 
    *                                     empty.
    */
   protected void bindField(final String name) {
      bindField(name, name);
   }

   /**
    * Creates a {@link MethodBoundPropertyListener} for the method and
    * property name and adds it the underlying property group.
    *
    * @param name          The method name to bind values to.
    * @param propertyName  The property name to bind to.
    *
    * @throws IllegalArgumentException    Method of property name is null or 
    *                                     empty.
    */
   protected void bindMethod(final String name, final String propertyName) {
      if (name == null || name.equals(""))
         throw new IllegalArgumentException("name");
      if (propertyName == null || propertyName.equals(""))
         throw new IllegalArgumentException("propertyName");

      addPropertyListener //                opposite of field bound =(
         (new MethodBoundPropertyListener(this, propertyName, name));
   }

   /**
    * Creates a {@link MethodBoundPropertyListener} for the method and
    * property name and adds it the underlying property group.
    *
    * @param name    The method name and property to bind values to.
    *
    * @throws IllegalArgumentException    Method of property name is null or 
    *                                     empty.
    */
   protected void bindMethod(final String name) {
      bindMethod(name, name);
   }

   private String makeName(final String name) {
      return groupname + "." + name;
   }

   protected void throwException(final String name) 
      throws PropertyException
   {
      throw new PropertyException(makeName(name));
   }

   protected void throwException(final String name, final String msg) 
      throws PropertyException
   {
      throw new PropertyException(makeName(name) + ": " + msg);
   }

   protected void throwException(final String name, final String msg, final Throwable nested)
      throws PropertyException
   {
      throw new PropertyException(makeName(name) + ": " + msg, nested);
   }

   protected void throwException(final String name, final Throwable nested)
      throws PropertyException
   {
      throw new PropertyException(makeName(name), nested);
   }
}
