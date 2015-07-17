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

import java.lang.ref.SoftReference;
import java.lang.ref.ReferenceQueue;

/**
 * Convenience class to wrap an <tt>Object</tt> into a <tt>SoftReference</tt>.
 *
 * <p>Modified from <tt>java.util.WeakHashMap.WeakKey</tt>.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public final class SoftObject
   extends SoftReference
{
   /** The hash code of the nested object */
   protected final int hashCode;
   
   /**
    * Construct a <tt>SoftObject</tt>.
    *
    * @param obj  Object to reference.
    */
   public SoftObject(final Object obj) {
      super(obj);
      hashCode = obj.hashCode();
   }
   
   /**
    * Construct a <tt>SoftObject</tt>.
    *
    * @param obj     Object to reference.
    * @param queue   Reference queue.
    */
   public SoftObject(final Object obj, final ReferenceQueue queue) {
      super(obj, queue);
      hashCode = obj.hashCode();
   }
   
   /**
    * Check the equality of an object with this.
    *
    * @param obj  Object to test equality with.
    * @return     True if object is equal.
    */
   public boolean equals(final Object obj) {
      if (obj == this) return true;

      if (obj != null && obj.getClass() == getClass()) {
         SoftObject soft = (SoftObject)obj;
         
         Object a = this.get();
         Object b = soft.get();
         if (a == null || b == null) return false;
         if (a == b) return true;

         return a.equals(b);
      }

      return false;
   }
   
   /**
    * Return the hash code of the nested object.
    *
    * @return  The hash code of the nested object.
    */
   public int hashCode() {
      return hashCode;
   }


   /////////////////////////////////////////////////////////////////////////
   //                            Factory Methods                          //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Create a <tt>SoftObject</tt> for the given object.
    *
    * @param obj     Object to reference.
    * @return        <tt>SoftObject</tt> or <tt>null</tt> if object is null.
    */
   public static SoftObject create(final Object obj) {
      if (obj == null) return null;
      else return new SoftObject(obj);
   }
   
   /**
    * Create a <tt>SoftObject</tt> for the given object.
    *
    * @param obj     Object to reference.
    * @param queue   Reference queue.
    * @return        <tt>SoftObject</tt> or <tt>null</tt> if object is null.
    */
   public static SoftObject create(final Object obj, 
                                   final ReferenceQueue queue) 
   {
      if (obj == null) return null;
      else return new SoftObject(obj, queue);
   }
}
