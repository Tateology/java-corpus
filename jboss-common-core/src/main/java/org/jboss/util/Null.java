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

import java.io.Serializable;

/**
 * A class that represents <tt>null</tt>.
 *
 * <p>{@link Null#VALUE} is used to given an object variable a dual-mode
 *    nullified value, where <tt>null</tt> would indicate that the value is 
 *    empty, and {@link Null#VALUE} would idicate that the value has been 
 *    set to <tt>null</tt> (or something to that effect).
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class Null
   implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -403173436435490144L;
   /** The primary instance of Null. */
   public static final Null VALUE = new Null();

   /** Do not allow public construction. */
   private Null() {}

   /**
    * Return a string representation.
    *
    * @return  Null
    */
   public String toString() {
      return null;
   }

   /**
    * Returns zero.
    *
    * @return  Zero.
    */
   public int hashCode() {
      return 0;
   }

   /**
    * Check if the given object is a Null instance or <tt>null</tt>.
    *
    * @param obj  Object to test.
    * @return     True if the given object is a Null instance or <tt>null</tt>.
    */
   public boolean equals(final Object obj) {
      if (obj == this) return true;
      return (obj == null || obj.getClass() == getClass());
   }
}

