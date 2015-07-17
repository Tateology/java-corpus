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

import java.util.EventObject;

import org.jboss.util.NullArgumentException;

/**
 * A property event.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PropertyEvent
   extends EventObject
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5007209897739884086L;
   /** Property name. */
   protected final String name;

   /** Property value. */
   protected final String value;

   /**
    * Construct a new <tt>PropertyEvent</tt>.
    *
    * @param source  The source of the event.
    * @param name    The property name effected.
    * @param value   The value of the property effected.
    *
    * @throws NullArgumentException    Name or source is <tt>null</tt>.
    */
   public PropertyEvent(final Object source,
                        final String name,
                        final String value)
   {
      super(source);

      if (name == null)
         throw new NullArgumentException("name");
      // value can be null

      this.name = name;
      this.value = value;
   }

   /**
    * Construct a new <tt>PropertyEvent</tt>.
    *
    * @param source  The source of the event.
    * @param name    The property name effected.
    *
    * @throws NullArgumentException    Name or source is <tt>null</tt>.
    */
   public PropertyEvent(Object source, String name) {
      this(source, name, null);
   }

   /**
    * Get the name of the property that is effected.
    *
    * @return     Property name.
    */
   public final String getPropertyName() {
      return name;
   }

   /**
    * Get the value of the property that is effected.
    *
    * @return  The value of the property that is effected or <tt>null</tt>.
    */
   public final String getPropertyValue() {
      return value;
   }

   /**
    * Return a string representation of this event.
    *
    * @return  A string representation of this event.
    */
   public String toString() {
      return super.toString() + 
         "{ name=" + name +
         ", value=" + value +
         " }";
   }
}
