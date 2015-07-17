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

import java.io.File;
import java.net.URL;
import java.util.Iterator;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 2305 $
 */
@SuppressWarnings("unchecked")
public class FileProtocolArchiveBrowserFactory implements ArchiveBrowserFactory
{
   public Iterator create(URL url, ArchiveBrowser.Filter filter)
   {
      File f = new File(url.getPath());
      
      if (f.isDirectory())
      {
         return new DirectoryArchiveBrowser(f, filter);
      }
      else
      {
         return new JarArchiveBrowser(f, filter);
      }
   }
}
