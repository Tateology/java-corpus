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
package org.jboss.util.platform;


/**
 * Provides common access to specifics about the version of <em>Java</em>
 * that a virtual machine supports.
 *
 * <p>Determines the version of the <em>Java Virtual Machine</em> by checking
 *    for the availablity of version specific classes.<p>
 *
 * <p>Classes are loaded in the following order:
 *    <ol>
 *    <li><tt>java.lang.Void</tt> was introduced in JDK 1.1</li>
 *    <li><tt>java.lang.ThreadLocal</tt> was introduced in JDK 1.2</li>
 *    <li><tt>java.lang.StrictMath</tt> was introduced in JDK 1.3</li>
 *    <li><tt>java.lang.StackTraceElement</tt> was introduced in JDK 1.4</li>
 *    <li><tt>java.lang.Enum</tt> was introduced in JDK 1.5</li>
 *    <li><tt>java.lang.management.LockInfo</tt> was introduced in JDK 1.6</li>
 *    </ol>
 * </p>
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public final class Java
{
   /** Prevent instantiation */
   private Java() {}

   /** Java version 1.0 token */
   public static final int VERSION_1_0 = 0x01;

   /** Java version 1.1 token */
   public static final int VERSION_1_1 = 0x02;

   /** Java version 1.2 token */
   public static final int VERSION_1_2 = 0x03;

   /** Java version 1.3 token */
   public static final int VERSION_1_3 = 0x04;

   /** Java version 1.4 token */
   public static final int VERSION_1_4 = 0x05;
   
   /** Java version 1.5 token */
   public static final int VERSION_1_5 = 0x06;
   
   /** Java version 1.6 token */
   public static final int VERSION_1_6 = 0x07;
   
   /** 
    * Private to avoid over optimization by the compiler.
    *
    * @see #getVersion()   Use this method to access this final value.
    */
   private static final int VERSION;

   /** Initialize VERSION. */ 
   static
   {
      // default to 1.0
      int version = VERSION_1_0;

      try
      {
         // check for 1.1
         Class.forName("java.lang.Void");
         version = VERSION_1_1;

         // check for 1.2
         Class.forName("java.lang.ThreadLocal");
         version = VERSION_1_2;

         // check for 1.3
         Class.forName("java.lang.StrictMath");
         version = VERSION_1_3;

         // check for 1.4
         Class.forName("java.lang.StackTraceElement");
         version = VERSION_1_4;
         
         // check for 1.5
         Class.forName("java.lang.Enum");
         version = VERSION_1_5;
         
         // check for 1.6
         Class.forName("java.lang.management.LockInfo");
         version = VERSION_1_6;         
      }
      catch (ClassNotFoundException ignore)
      {
      }
      VERSION = version;
   }

   /**
    * Return the version of <em>Java</em> supported by the VM.
    *
    * @return  The version of <em>Java</em> supported by the VM.
    */
   public static int getVersion()
   {
      return VERSION;
   }

   /**
    * Returns true if the given version identifer is equal to the
    * version identifier of the current virtuial machine.
    *
    * @param version    The version identifier to check for.
    * @return           True if the current virtual machine is the same version.
    */
   public static boolean isVersion(final int version)
   {
      return VERSION == version;
   }

   /**
    * Returns true if the current virtual machine is compatible with
    * the given version identifer.
    *
    * @param version    The version identifier to check compatibility of.
    * @return           True if the current virtual machine is compatible.
    */
   public static boolean isCompatible(final int version)
   {
      // if our vm is the same or newer then we are compatible
      return VERSION >= version;
   }
}
