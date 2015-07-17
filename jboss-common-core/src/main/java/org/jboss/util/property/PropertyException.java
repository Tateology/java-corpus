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

import org.jboss.util.NestedRuntimeException;

/**
 * This exception is thrown to indicate a non-fatal problem with the 
 * property system.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PropertyException
   extends NestedRuntimeException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 452085442436956822L;
   /**
    * Construct a <tt>PropertyException</tt> with the specified detail 
    * message.
    *
    * @param msg  Detail message.
    */
   public PropertyException(String msg) {
      super(msg);
   }

   /**
    * Construct a <tt>PropertyException</tt> with the specified detail 
    * message and nested <tt>Throwable</tt>.
    *
    * @param msg     Detail message.
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public PropertyException(String msg, Throwable nested) {
      super(msg, nested);
   }

   /**
    * Construct a <tt>PropertyException</tt> with the specified
    * nested <tt>Throwable</tt>.
    *
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public PropertyException(Throwable nested) {
      super(nested);
   }

   /**
    * Construct a <tt>PropertyException</tt> with no detail.
    */
   public PropertyException() {
      super();
   }
}
