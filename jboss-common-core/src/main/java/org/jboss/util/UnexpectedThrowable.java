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

/**
 * Thrown to indicate that a Throwable was caught but was not expected.
 * This is typical when catching Throwables to handle and rethrow Exceptions
 * and Errors.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class UnexpectedThrowable
   extends NestedError
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4318032849691437298L;
   /**
    * Construct a <tt>UnexpectedThrowable</tt> with the specified 
    * detail message.
    *
    * @param msg  Detail message.
    */
   public UnexpectedThrowable(final String msg) {
      super(msg);
   }

   /**
    * Construct a <tt>UnexpectedThrowable</tt> with the specified
    * detail message and nested <tt>Throwable</tt>.
    *
    * @param msg     Detail message.
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public UnexpectedThrowable(final String msg, final Throwable nested) {
      super(msg, nested);
   }

   /**
    * Construct a <tt>UnexpectedThrowable</tt> with the specified
    * nested <tt>Throwable</tt>.
    *
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public UnexpectedThrowable(final Throwable nested) {
      super(nested);
   }

   /**
    * Construct a <tt>UnexpectedThrowable</tt> with no detail.
    */
   public UnexpectedThrowable() {
      super();
   }
}
