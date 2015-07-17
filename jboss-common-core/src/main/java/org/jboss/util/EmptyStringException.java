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
 * Thrown to indicate that a string was empty (aka. <code>""</code>)
 * where it must <b>not</b> be.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class EmptyStringException
   extends IllegalArgumentException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7958716001355854762L;
   /**
    * Construct a <tt>EmptyStringException</tt>.
    *
    * @param msg  Exception message.
    */
   public EmptyStringException(final String msg) {
      super(msg);
   }

   /**
    * Construct a <tt>EmptyStringException</tt>.
    */
   public EmptyStringException() {
      super();
   }
}
