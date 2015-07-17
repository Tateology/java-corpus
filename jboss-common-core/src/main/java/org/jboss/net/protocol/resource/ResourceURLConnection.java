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
package org.jboss.net.protocol.resource;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URL;
import java.net.MalformedURLException;

import org.jboss.net.protocol.DelegatingURLConnection;

import org.jboss.logging.Logger;

/**
 * Provides access to system resources as a URLConnection.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 */
public class ResourceURLConnection
   extends DelegatingURLConnection
{
   private static final Logger log = Logger.getLogger(ResourceURLConnection.class);

   public ResourceURLConnection(final URL url)
      throws MalformedURLException, IOException
   {
      super(url);
   }

   protected URL makeDelegateUrl(final URL url)
      throws MalformedURLException, IOException
   {
      String name = url.getHost();
      String file = url.getFile();
      if (file != null && !file.equals(""))
      {
         name += file;
      }

      // first try TCL and then SCL

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL target = cl.getResource(name);

      if (target == null)
      {
         cl = ClassLoader.getSystemClassLoader();
         target = cl.getResource(name);
      }

      if (target == null)
         throw new FileNotFoundException("Could not locate resource: " + name);

      if (log.isTraceEnabled())
      {
         log.trace("Target resource URL: " + target);
         try
         {
            log.trace("Target resource URL connection: " + target.openConnection());
         }
         catch (Exception ignore)
         {
         }
      }

      // Return a new URL as the cl version does not use the JB stream factory
      return new URL(target.toExternalForm());
   }
}
