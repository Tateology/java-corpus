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
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 *
 **/
@SuppressWarnings("unchecked")
public class JarArchiveBrowser implements Iterator
{
   JarFile zip;
   Enumeration entries;
   JarEntry next;
   ArchiveBrowser.Filter filter;

   @SuppressWarnings("deprecation")
   public JarArchiveBrowser(JarURLConnection url,  ArchiveBrowser.Filter filter)
   {
      this.filter = filter;
      try
      {
         zip = url.getJarFile();
         entries = zip.entries();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      setNext();
   }

   public JarArchiveBrowser(File f, ArchiveBrowser.Filter filter)
   {
      this.filter = filter;
      try
      {
         zip = new JarFile(f);
         entries = zip.entries();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      setNext();
   }

   public boolean hasNext()
   {
      return next != null;
   }

   private void setNext()
   {
      next = null;
      while (entries.hasMoreElements() && next == null)
      {
         do
         {
            next = (JarEntry)entries.nextElement();
         } while (entries.hasMoreElements() && next.isDirectory());
         if (next.isDirectory()) next = null;

         if (next != null && !filter.accept(next.getName()))
         {
            next = null;
         }
      }
   }

   public Object next()
   {
      ZipEntry entry = next;
      setNext();

      try
      {
         return zip.getInputStream(entry);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void remove()
   {
      throw new RuntimeException("Illegal operation on ArchiveBrowser");
   }
}
