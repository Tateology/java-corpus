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
package org.jboss.test.util.test.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import org.jboss.net.protocol.file.FileURLConnection;
import org.jboss.util.file.Files;

/**
 * Tests of the expected jdk file: url connection protocol handler behaviors.
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision:$
 */
public class FileURLConnectionTestCase extends TestCase
{
   public void testLastModified() throws Exception
   {
      File tmp = File.createTempFile("testLastModified", "");
      tmp.deleteOnExit();
      long lastModified = tmp.lastModified();
      System.out.println("Created file: " + tmp.getAbsolutePath() + ", lastModified:" + lastModified);
      
      URL tmpURL = tmp.toURL();
      // Test JDK's provided FileURLConnection
      checkLastModified(tmpURL.openConnection(), lastModified);
      // Test JBoss's FileURLConnection
      checkLastModified(new FileURLConnection(tmpURL), lastModified);
   }

   public void testContentLength() throws Exception
   {
      File tmp = File.createTempFile("testContentLength", "");
      tmp.deleteOnExit();
      FileOutputStream fos = new FileOutputStream(tmp);
      fos.write("testLength".getBytes());
      fos.close();
      long expectedLength = tmp.length();
      
      URL tmpURL = tmp.toURL();
      checkContentLength(tmpURL.openConnection(), expectedLength);
      checkContentLength(new FileURLConnection(tmpURL), expectedLength);
   }
   
   public void testContentType() throws Exception
   {
      File tmp = File.createTempFile("testContentType", ".txt");      
      tmp.deleteOnExit();
      
      FileOutputStream fos = new FileOutputStream(tmp);
      fos.write("A text file".getBytes());
      fos.close();
      String expectedContentType = "text/plain";
      
      URL tmpURL = tmp.toURL();
      checkContentType(tmpURL.openConnection(), expectedContentType);
    
      checkContentType(new FileURLConnection(tmpURL), expectedContentType);
      
      File dir = tmp.getParentFile();
      URL dirURL = dir.toURL();
      checkContentType(dirURL.openConnection(), expectedContentType);
      
      checkContentType(new FileURLConnection(dirURL), expectedContentType);
   }

   public void testDirectoryListing() throws Exception
   {
      // create a test directory structure
      //   TMPDIR/testDirectoryList39558
      //   TMPDIR/testDirectoryList39558/test.txt
      //   TMPDIR/testDirectoryList39558/test.dir
      File rootDir = File.createTempFile("testDirectoryList", "");
      try
      {
         rootDir.delete();
         rootDir.mkdir();
         System.out.println(rootDir);
         File tmpFile = new File(rootDir, "test.txt");
         tmpFile.createNewFile();
         System.out.println(tmpFile);
         FileOutputStream fos = new FileOutputStream(tmpFile);
         fos.write("this is a test file".getBytes());
         fos.close();
         File tmpDir = new File(rootDir, "test.dir");
         tmpDir.mkdir();
         System.out.println(tmpDir);
         
         String[] expectedList = { "test.dir", "test.txt", null };      
         URL rootURL = rootDir.toURL();
   
         // Check JDK FileURLConnection impl
         checkDirectoryListing(rootURL.openConnection(), expectedList);
         
         // Test JBoss FileURLConnection impl
         checkDirectoryListing(new FileURLConnection(rootURL), expectedList);
      }
      finally
      {
         // cleanup
         Files.delete(rootDir);
      }
   }
   
   private void checkLastModified(URLConnection conn, long expectedLastModified)
   {
      System.out.println("Got URLConnection of type: " + conn.getClass().getName());
      assertEquals(expectedLastModified, conn.getLastModified());
      
      long lastModifiedHdr = conn.getHeaderFieldDate("last-modified", 0);
      System.out.println(conn.getURL() + ", last-modified header: "+lastModifiedHdr);
      // the last-modified header is expected to strip the milliseconds to
      // comply with the (dd MMM yyyy HH:mm:ss) format, so the following assertions
      // is invalid on windows that provide millisecond accuracy to File.lastModified()
      // see, http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4504473
      // assertEquals(expectedLastModified, lastModifiedHdr);
   }
   
   private void checkContentLength(URLConnection conn, long expectedLength)
   {
      System.out.println("Got URLConnection of type: " + conn.getClass().getName());      
      int length = conn.getContentLength();
      System.out.println(conn.getURL() + ", content-length:" + length);
      assertEquals(expectedLength, length);
      int lengthHdr = conn.getHeaderFieldInt("content-length", 0);
      assertEquals(expectedLength, lengthHdr);      
   }
   
   private void checkContentType(URLConnection conn, String expectedType)
   {
      System.out.println("Got URLConnection of type: " + conn.getClass().getName());  
      String type = conn.getContentType();
      System.out.println(conn.getURL() + ", content-type: " + type);
      assertEquals(type, expectedType);      
   }
   
   private void checkDirectoryListing(URLConnection conn, String[] expectedFiles) throws IOException
   {
      System.out.println("Got URLConnection of type: " + conn.getClass().getName());      
      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      try
      {
         // verify the sorted directory list
         for (int i = 0; i < expectedFiles.length; i++)
         {
            String msg = "directory entry #" + i;
            System.out.println(msg + " : " + expectedFiles[i]);
            assertEquals(msg, expectedFiles[i], in.readLine());
         }
      }
      finally
      {
         in.close();
      }
   }
}
