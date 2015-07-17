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
package org.jboss.util.collection;

import java.io.Serializable;

import org.jboss.util.NullArgumentException;
import org.jboss.util.Objects;
import org.jboss.util.HashCode;
import org.jboss.util.Strings;

/**
 * An immutable compound key class.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CompoundKey
   implements Serializable, Cloneable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1181463173922935047L;
   /** The elements of the key */
   private final Object elements[];

   /**
    * Construct a CompoundKey.
    *
    * @param elements   Elements of the key.
    */
   public CompoundKey(final Object elements[]) {
      if (elements == null)
         throw new NullArgumentException("elements");

      this.elements = elements;
   }

   /**
    * Construct a CompoundKey.
    *
    * @param a    Element.
    * @param b    Element.
    */
   public CompoundKey(final Object a, final Object b) {
      this(new Object[] { a, b });
   }

   /**
    * Construct a CompoundKey.
    *
    * @param a    Element.
    * @param b    Element.
    * @param c    Element.
    */
   public CompoundKey(final Object a, final Object b, final Object c) {
      this(new Object[] { a, b, c });
   }

   /**
    * Test the equality of an object with this.
    *
    * @param obj  Object to test equality with.
    * @return     True if object is equal.
    */
   public boolean equals(final Object obj) {
      if (obj == this) return true;

      if (obj != null && obj.getClass() == getClass()) {
         CompoundKey key = (CompoundKey)obj;

         return Objects.equals(key.elements, elements);
      }

      return false;
   }

   /**
    * Get the hash code of this object.
    *
    * @return  Hash code.
    */
   public int hashCode() {
      return HashCode.generate(elements);
   }

   /**
    * Return a string representation of this object.
    *
    * @return  A string representation of this object.
    */
   public String toString() {
      return super.toString() + Strings.join(elements, "[", ",", "]");
   }

   /**
    * Return a shallow cloned copy of this object.
    *
    * @return   Shallow cloned copy of this object.
    */
   public Object clone() {
      try {
         return super.clone();
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError();
      }
   }
}
