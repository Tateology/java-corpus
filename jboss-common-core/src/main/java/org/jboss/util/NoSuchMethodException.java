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

import java.lang.reflect.Method;

/**
 * A better NoSuchMethodException which can take a Method object
 * and formats the detail message based on in.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NoSuchMethodException
   extends java.lang.NoSuchMethodException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -3044955578250977290L;
   /**
    * Construct a <tt>NoSuchMethodException</tt> with the specified detail 
    * message.
    *
    * @param msg  Detail message.
    */
   public NoSuchMethodException(String msg) {
      super(msg);
   }

   /**
    * Construct a <tt>NoSuchMethodException</tt> using the given method
    * object to construct the detail message.
    *
    * @param method  Method to determine detail message from.
    */
   public NoSuchMethodException(Method method) {
      super(format(method));
   }

   /**
    * Construct a <tt>NoSuchMethodException</tt> using the given method
    * object to construct the detail message.
    *
    * @param msg     Detail message prefix.
    * @param method  Method to determine detail message suffix from.
    */
   public NoSuchMethodException(String msg, Method method) {
      super(msg + format(method));
   }
   
   /**
    * Construct a <tt>NoSuchMethodException</tt> with no detail.
    */
   public NoSuchMethodException() {
      super();
   }

   public static String format(Method method)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(method.getName()).append("(");
      Class<?>[] paramTypes = method.getParameterTypes();
      for (int count = 0; count < paramTypes.length; count++) {
         if (count > 0) {
            buffer.append(",");
         }
         buffer.
            append(paramTypes[count].getName().substring(paramTypes[count].getName().lastIndexOf(".")+1));
      }
      buffer.append(")");

      return buffer.toString();
   }
}
