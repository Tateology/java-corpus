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

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comment
 *
 * @deprecated
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public abstract class ArchiveBrowser
{
   public interface Filter
   {
      boolean accept(String filename);
   }

   //use concurrent hashmap since a protocol can be added on the fly through the public attribute
   public static Map factoryFinder = new ConcurrentHashMap();

   static
   {
      factoryFinder.put("file", new FileProtocolArchiveBrowserFactory());
      factoryFinder.put("jar", new JarProtocolArchiveBrowserFactory());
   }

   public static Iterator getBrowser(URL url, Filter filter)
   {
      ArchiveBrowserFactory factory = (ArchiveBrowserFactory)factoryFinder.get(url.getProtocol());
      if (factory == null) throw new RuntimeException("Archive browser cannot handle protocol: " + url);
      return factory.create(url, filter);
   }
}
