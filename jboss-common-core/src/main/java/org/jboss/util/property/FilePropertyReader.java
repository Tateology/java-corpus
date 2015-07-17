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
package org.jboss.util.property;

import java.util.Properties;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import org.jboss.util.NullArgumentException;

/**
 * Reads properties from one or more files.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public class FilePropertyReader
   implements PropertyReader
{
   /** Array of filenames to load properties from */
   protected String[] filenames;

   /**
    * Construct a FilePropertyReader with an array of filenames
    * to read from.
    *
    * @param filenames  Filenames to load properties from
    */
   public FilePropertyReader(String[] filenames) {
      if (filenames == null)
         throw new NullArgumentException("filenames");

      this.filenames = filenames;
   }

   /**
    * Construct a FilePropertyReader with a single filename to read from.
    *
    * @param filename   Filename to load properties from
    */
   public FilePropertyReader(String filename) {
      this(new String[] { filename });
   }

   /**
    * Get an input stream for the given filename.
    *
    * @param filename   File name to get input stream for.
    * @return           Input stream for file.
    *
    * @throws IOException  Failed to get input stream for file.
    */
   protected InputStream getInputStream(String filename) throws IOException {
      File file = new File(filename);
      return new FileInputStream(file);
   }

   /**
    * Load properties from a file into a properties map.
    *
    * @param props      Properties map to load properties into.
    * @param filename   Filename to read properties from.
    *
    * @throws IOException              Failed to load properties from filename.
    * @throws IllegalArgumentException Filename is invalid.
    */
   protected void loadProperties(Properties props, String filename)
      throws IOException
   {
      if (filename == null)
         throw new NullArgumentException("filename");
      if (filename.equals(""))
         throw new IllegalArgumentException("filename");

      InputStream in = new BufferedInputStream(getInputStream(filename));
      props.load(in);
      in.close();
   }

   /**
    * Read properties from each specified filename
    *
    * @return  Read properties
    *
    * @throws PropertyException    Failed to read properties.
    * @throws IOException          I/O error while reading properties.
    */
   public Map readProperties()
      throws PropertyException, IOException
   {
      Properties props = new Properties();
      
      // load each specified property file
      for (int i=0; i<filenames.length; i++) {
         loadProperties(props, filenames[i]);
      }

      return props;
   }
}
