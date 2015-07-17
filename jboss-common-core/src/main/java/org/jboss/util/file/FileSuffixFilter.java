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
import java.io.FileFilter;

/**
 * A <em>suffix</em> based file filter.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FileSuffixFilter
   implements FileFilter
{
   /** A list of suffixes which files must have to be accepted. */
   protected final String suffixes[];

   /** Flag to signal that we want to ignore the case. */
   protected final boolean ignoreCase;

   /**
    * Construct a <tt>FileSuffixFilter</tt>.
    *
    * @param suffixes   A list of suffixes which files mut have to be accepted.
    * @param ignoreCase <tt>True</tt> if the filter should be case-insensitive.
    */
   public FileSuffixFilter(final String suffixes[],
                           final boolean ignoreCase)
   {
      this.ignoreCase = ignoreCase;
      if (ignoreCase) {
         this.suffixes = new String[suffixes.length];
         for (int i=0; i<suffixes.length; i++) {
            this.suffixes[i] = suffixes[i].toLowerCase();
         }
      }
      else {
         this.suffixes = suffixes;
      }
   }

   /**
    * Construct a <tt>FileSuffixFilter</tt>.
    *
    * @param suffixes   A list of suffixes which files mut have to be accepted.
    */
   public FileSuffixFilter(final String suffixes[])
   {
      this(suffixes, false);
   }

   /**
    * Construct a <tt>FileSuffixFilter</tt>.
    *
    * @param suffix     The suffix which files must have to be accepted.
    * @param ignoreCase <tt>True</tt> if the filter should be case-insensitive.
    */
   public FileSuffixFilter(final String suffix,
                           final boolean ignoreCase)
   {
      this(new String[] { suffix }, ignoreCase);
   }

   /**
    * Construct a case sensitive <tt>FileSuffixFilter</tt>.
    *
    * @param suffix  The suffix which files must have to be accepted.
    */
   public FileSuffixFilter(final String suffix) {
      this(suffix, false);
   }

   /**
    * Check if a file is acceptible.
    *
    * @param file    The file to check.
    * @return        <tt>true</tt> if the file is acceptable.
    */
   public boolean accept(final File file) {
      boolean success = false;

      for (int i=0; i<suffixes.length && !success; i++) {
         if (ignoreCase)
            success = file.getName().toLowerCase().endsWith(suffixes[i]);
         else
            success = file.getName().endsWith(suffixes[i]);
      }

      return success;
   }
}
