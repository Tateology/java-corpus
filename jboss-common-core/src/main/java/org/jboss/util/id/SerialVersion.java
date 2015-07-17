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
package org.jboss.util.id;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Serialization version compatibility mode constants.<p>
 *
 * Contains static constants and attributes to help with serialization
 * versioning.<p>
 * 
 * Set the system property <pre>org.jboss.j2ee.LegacySerialization</pre>
 * to serialization compatibility with jboss-4.0.1 and earlier. The
 * serialVersionUID values were synched with the j2ee 1.4 ri classes and
 * explicitly set in jboss-4.0.2 which is what
 *
 * @author  <a href="mailto:Adrian.Brock@JBoss.com">Adrian Brock</a>.
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class SerialVersion
{
   // Static --------------------------------------------------------

   /** Legacy, jboss-4.0.1 through jboss-4.0.0 */
   public static final int LEGACY = 0;

   /** The serialization compatible with Sun's RI, jboss-4.0.2+ */
   public static final int JBOSS_402 = 1;

   /**
    * The serialization version to use
    */
   public static int version = JBOSS_402;

   /** Determine the serialization version */
   static
   {
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            try
            {
               if (System.getProperty("org.jboss.j2ee.LegacySerialization") != null)
                  version = LEGACY;
            }
            catch (Throwable ignored)
            {
            }
            return null;
         }
      });
   }
}
