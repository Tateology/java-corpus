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

import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 1958 $
 */
@SuppressWarnings("unchecked")
public class JarStreamBrowser implements Iterator
{
//   ZipFile zip;
//   Enumeration entries;
   JarInputStream jar;
   JarEntry next;
   @SuppressWarnings("deprecation")
   ArchiveBrowser.Filter filter;

   @SuppressWarnings("deprecation")
   public JarStreamBrowser(File file, ArchiveBrowser.Filter filter) throws IOException
   {
      this(new FileInputStream(file), filter);
   }

   @SuppressWarnings("deprecation")
   public JarStreamBrowser(InputStream is, ArchiveBrowser.Filter filter) throws IOException
   {
      this.filter = filter;
      jar = new JarInputStream(is);
      setNext();
   }

   public boolean hasNext()
   {
      return next != null;
   }

   private void setNext()
   {
      try
      {
         if (next != null) jar.closeEntry();
         next = null;
         do
         {
            next = jar.getNextJarEntry();
         } while (next != null && (next.isDirectory() || !filter.accept(next.getName())));
         if (next == null) jar.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException("failed to browse jar", e);
      }
   }

   public Object next()
   {
      int size = (int) next.getSize();
      byte[] buf = new byte[size];
      int count = 0;
      int current = 0;
      try
      {
         while ((
                 (
                         current = jar.read(buf, count,
                                 size - count)
                 ) != -1
         ) && (count < size))
         {
            count += current;
         }
         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         setNext();
         return bais;
      }
      catch (IOException e)
      {
         try
         {
            jar.close();
         }
         catch (IOException ignored)
         {

         }
         throw new RuntimeException(e);
      }
   }

   public void remove()
   {
      throw new RuntimeException("Illegal operation on ArchiveBrowser");
   }
}
