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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class DirectoryArchiveBrowser implements Iterator
{
   private Iterator files;

   @SuppressWarnings("deprecation")
   public DirectoryArchiveBrowser(File file, ArchiveBrowser.Filter filter)
   {
      ArrayList list = new ArrayList();
      try
      {
         create(list, file, filter);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      files = list.iterator();
   }

   @SuppressWarnings("deprecation")
   public static void create(List list, File dir, ArchiveBrowser.Filter filter) throws Exception
   {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
         {
            create(list, files[i], filter);
         }
         else
         {
            if (filter.accept(files[i].getAbsolutePath()))
            {
               list.add(files[i]);
            }
         }
      }
   }

   public boolean hasNext()
   {
      return files.hasNext();
   }

   public Object next()
   {
      File fp = (File) files.next();
      try
      {
         return new FileInputStream(fp);
      }
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void remove()
   {
      throw new RuntimeException("Illegal operation call");
   }


}
