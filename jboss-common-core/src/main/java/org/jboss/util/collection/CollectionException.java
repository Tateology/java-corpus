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

/**
 * A generic collection exception.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CollectionException
   extends RuntimeException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1470947618739421094L;
   /**
    * Construct a <code>CollectionException</code> with the specified 
    * detail message.
    *
    * @param msg  Detail message.
    */
   public CollectionException(String msg) {
      super(msg);
   }

   /**
    * Construct a <code>CollectionException</code> with no detail.
    */
   public CollectionException() {
      super();
   }
}
