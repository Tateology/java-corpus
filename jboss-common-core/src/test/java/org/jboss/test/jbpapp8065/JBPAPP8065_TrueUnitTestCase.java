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
package org.jboss.test.jbpapp8065;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.jboss.net.protocol.file.FileURLConnection;

import junit.framework.TestCase;

/**
 * Unit tests for JBPAPP-8065
 *
 * @see https://issues.jboss.org/browse/JBPAPP-8065
 * 
 * @author bmaxwell
 */
public class JBPAPP8065_TrueUnitTestCase extends TestCase
{
   public JBPAPP8065_TrueUnitTestCase(String name)
   {
      super(name);
   }
 
   public void testJBPAPP806_UseURI_True()
   {
      System.setProperty("org.jboss.net.protocol.file.useURI", "true");
      URL url = null;
      try
      {
         url = new URL("file:non-exisitant-file-" + new Date().getTime());
         try
         {            
            URLConnection urlConnection = new FileURLConnection(url);
            urlConnection.connect();
         }
         catch (IOException e)
         {
            // this will catch a FileNotFoundException
            // This is NOT the expected result when -Dorg.jboss.net.protocol.file.useURI=true
            fail("URL: " + url + " should have thrown an IllegalArgumentException when -Dorg.jboss.net.protocol.file.useURI=true see JBPAPP-8065");
         }
         catch(IllegalArgumentException iae)
         {
            // This is the expected results the url.toURI() will throw given file:non... is not a valid URI
            return;   
         }
         fail("URL: " + url + " should have thrown an IllegalArgumentException when -Dorg.jboss.net.protocol.file.useURI=true see JBPAPP-8065");
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
         fail("URL: " + url + " should not be malformed");
      }
   }
}
