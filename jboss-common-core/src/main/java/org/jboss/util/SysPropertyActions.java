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

import java.security.PrivilegedAction;
import java.security.AccessController;

/**
 * Priviledged actions for the package
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
class SysPropertyActions
{
   interface SysProps
   {
      SysProps NON_PRIVILEDGED = new SysProps()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            return System.getProperty(name, defaultValue);
         }
      };
      SysProps PRIVILEDGED = new SysProps()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            PrivilegedAction action = new PrivilegedAction()
            {
               public Object run()
               {
                  return System.getProperty(name, defaultValue);
               }
            };
            return (String) AccessController.doPrivileged(action);
         }
      };
      String getProperty(String name, String defaultValue);
   }

   public static String getProperty(String name, String defaultValue)
   {
      String prop;
      if( System.getSecurityManager() == null )
         prop = SysProps.NON_PRIVILEDGED.getProperty(name, defaultValue);
      else
         prop = SysProps.PRIVILEDGED.getProperty(name, defaultValue);
      return prop;
   }
}
