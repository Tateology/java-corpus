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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Collection;
import java.util.StringTokenizer;
import java.net.URL;
import java.io.IOException;

/**
 * Support class for URLLister's providing protocol independent functionality.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public abstract class URLListerBase implements URLLister
{
   public Collection listMembers (URL baseUrl, String patterns,
      boolean scanNonDottedSubDirs) throws IOException
   {
      // @todo, externalize the separator?
      StringTokenizer tokens = new StringTokenizer (patterns, ",");
      String[] members = new String[tokens.countTokens ()];
      for (int i=0; tokens.hasMoreTokens (); i++)
      {
         String token = tokens.nextToken ();
         // Trim leading/trailing spaces as its unlikely they are meaningful
         members[i] = token.trim();
      }
      URLFilter filter = new URLFilterImpl (members);
      return listMembers (baseUrl, filter, scanNonDottedSubDirs);
   }

   public Collection listMembers (URL baseUrl, String patterns) throws IOException
   {
      return listMembers (baseUrl, patterns, false);
   }
   
   /**
    * Inner class representing Filter criteria to be applied to the members
    * of the returned Collection
    */
   public static class URLFilterImpl implements URLFilter
   {
      protected boolean allowAll;
      protected HashSet constants;
      
      public URLFilterImpl (String[] patterns)
      {
         constants = new HashSet (Arrays.asList (patterns));
         allowAll = constants.contains ("*");
      }
      
      public boolean accept (URL baseUrl, String name)
      {
         if (allowAll)
         {
            return true;
         }
         if (constants.contains (name))
         {
            return true;
         }
         return false;
      }
   }
   
   protected static final URLFilter acceptAllFilter = new URLFilter ()
   {
      public boolean accept (URL baseURL, String memberName)
      {
         return true;
      }
   };
}
