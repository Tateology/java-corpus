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
package org.jboss.util.file;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class JarProtocolArchiveBrowserFactory implements ArchiveBrowserFactory
{

   @SuppressWarnings("deprecation")
   public Iterator create(URL url, ArchiveBrowser.Filter filter)
   {
      if (url.toString().endsWith("!/"))
      {
         try
         {
            return new JarArchiveBrowser((JarURLConnection) url.openConnection(), filter);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Unable to browse url: " + url, e);
         }
      }
      else
      {
         try
         {
            return new JarStreamBrowser(url.openStream(), filter);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Unable to browse url: " + url, e);
         }
      }
   }
}
