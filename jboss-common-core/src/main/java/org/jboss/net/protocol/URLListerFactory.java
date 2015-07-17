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
package org.jboss.net.protocol;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class URLListerFactory {
   private static HashMap defaultClasses = new HashMap();
   static {
      defaultClasses.put("file", "org.jboss.net.protocol.file.FileURLLister");
      defaultClasses.put("http", "org.jboss.net.protocol.http.DavURLLister");
      defaultClasses.put("https", "org.jboss.net.protocol.http.DavURLLister");
   }

   private HashMap classes;

   /**
    * Create a URLLister with default listers defined for file and http
    * protocols.
    */
   public URLListerFactory() {
      classes = (HashMap) defaultClasses.clone();
   }

   /**
    * Create a URL lister using the protocol from the URL
    * @param url the url defining the protocol
    * @return a URLLister capable of listing URLs of that protocol
    * @throws MalformedURLException if no lister could be found for the protocol
    */
   public URLLister createURLLister(URL url) throws MalformedURLException  {
      return createURLLister(url.getProtocol());
   }

   /**
    * Create a URL lister for the supplied protocol
    * @param protocol the protocol
    * @return a URLLister capable of listing URLs of that protocol
    * @throws MalformedURLException if no lister could be found for the protocol
    */
   public URLLister createURLLister(String protocol) throws MalformedURLException {
      try {
         String className = (String) classes.get(protocol);
         if (className == null) {
            throw new MalformedURLException("No lister class defined for protocol "+protocol);
         }

         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
         return (URLLister) clazz.newInstance();
      } catch (ClassNotFoundException e) {
         throw new MalformedURLException(e.getMessage());
      } catch (InstantiationException e) {
         throw new MalformedURLException(e.getMessage());
      } catch (IllegalAccessException e) {
         throw new MalformedURLException(e.getMessage());
      }
   }

   /**
    * Register a URLLister class for a given protocol
    * @param protocol the protocol this class will handle
    * @param className the URLLister implementation to instanciate
    */
   public void registerListener(String protocol, String className) {
      classes.put(protocol, className);
   }
}
