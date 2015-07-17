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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * Interface defining methods that can be used to list the contents of a URL
 * collection irrespective of the protocol.
 */
@SuppressWarnings("unchecked")
public interface URLLister {
   /**
    * List the members of the given collection URL that match the patterns
    * supplied and, if it contains directory that contains NO dot in the name and
    * scanNonDottedSubDirs is true, recursively finds URL in these directories.
    * @param baseUrl the URL to list; must end in "/"
    * @param patterns the patterns to match (separated by ',')
    * @param scanNonDottedSubDirs enables recursive search for directories containing no dots
    * @return a Collection of URLs that match
    * @throws IOException if there was a problem getting the list
    */
   Collection listMembers(URL baseUrl, String patterns, boolean scanNonDottedSubDirs) throws IOException;
   
   /**
    * List the members of the given collection URL that match the patterns
    * supplied. Doesn't recursively list files contained in directories.
    * @param baseUrl the URL to list; must end in "/"
    * @param patterns the patterns to match (separated by ',')
    * @return a Collection of URLs that match
    * @throws IOException if there was a problem getting the list
    */
   Collection listMembers(URL baseUrl, String patterns) throws IOException;

   /**
    * List the members of the given collection that are accepted by the filter
    * @param baseUrl the URL to list; must end in "/"
    * @param filter a filter that is called to determine if a member should
    *               be returned
    * @param scanNonDottedSubDirs enables recursive search for directories containing no dots
    * @return a Collection of URLs that match
    * @throws IOException if there was a problem getting the list
    */
   Collection listMembers(URL baseUrl, URLFilter filter, boolean scanNonDottedSubDirs) throws IOException;

   /**
    * List the members of the given collection that are accepted by the filter
    * @param baseUrl the URL to list; must end in "/"
    * @param filter a filter that is called to determine if a member should
    *               be returned
    * @return a Collection of URLs that match
    * @throws IOException if there was a problem getting the list
    */
   Collection listMembers(URL baseUrl, URLFilter filter) throws IOException;

   /**
    * Interface defining a filter for listed members.
    */
   public interface URLFilter {
      /**
       * Determine whether the supplied memberName should be accepted
       * @param baseURL the URL of the collection
       * @param memberName the member of the collection
       * @return true to accept
       */
      boolean accept(URL baseURL, String memberName);
   }
}
