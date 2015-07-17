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
import java.io.FilenameFilter;

/**
 * A <em>suffix</em> based filename filter.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FilenameSuffixFilter
   implements FilenameFilter
{
   /** The suffix which files must have to be accepted. */
   protected final String suffix;

   /** Flag to signal that we want to ignore the case. */
   protected final boolean ignoreCase;

   /**
    * Construct a <tt>FilenameSuffixFilter</tt>.
    *
    * @param suffix     The suffix which files must have to be accepted.
    * @param ignoreCase <tt>True</tt> if the filter should be case-insensitive.
    */
   public FilenameSuffixFilter(final String suffix,
                               final boolean ignoreCase)
   {
      this.ignoreCase = ignoreCase;
      this.suffix = (ignoreCase ? suffix.toLowerCase() : suffix);
   }

   /**
    * Construct a case sensitive <tt>FilenameSuffixFilter</tt>.
    *
    * @param suffix  The suffix which files must have to be accepted.
    */
   public FilenameSuffixFilter(final String suffix) {
      this(suffix, false);
   }

   /**
    * Check if a file is acceptible.
    *
    * @param dir  The directory the file resides in.
    * @param name The name of the file.
    * @return     <tt>true</tt> if the file is acceptable.
    */
   public boolean accept(final File dir, final String name) {
      if (ignoreCase) {
         return name.toLowerCase().endsWith(suffix);
      }
      else {
         return name.endsWith(suffix);
      }
   }
}
