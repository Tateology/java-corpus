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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.stream.Streams;

/**
 * A collection of file utilities.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:Scott.Stark@jboss.org">Scott Stark<a/>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision$
 */
public final class Files
{
   /** The Logger instance */   
   private static final Logger log = Logger.getLogger(Files.class);
   
   /** for byte-to-hex conversions */
   private static final char[] hexDigits = new char[]
      { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
   
   /** The default size of the copy buffer. */
   public static final int DEFAULT_BUFFER_SIZE = 8192;

   /** Delete a file, or a directory and all of its contents.
    *
    * @param dir The directory or file to delete.
    * @return True if all delete operations were successfull.
    */
   public static boolean delete(final File dir)
   {
      boolean success = true;

      File files[] = dir.listFiles();
      if (files != null)
      {
         for (int i = 0; i < files.length; i++)
         {
            File f = files[i];
            if( f.isDirectory() == true )
            {
               // delete the directory and all of its contents.
               if( delete(f) == false )
               {
                  success = false;
                  log.debug("Failed to delete dir: "+f.getAbsolutePath());
               }
            }
            // delete each file in the directory
            else if( f.delete() == false )
            {
               success = false;
               log.debug("Failed to delete file: "+f.getAbsolutePath());
            }
         }
      }

      // finally delete the directory
      if( dir.delete() == false )
      {
         success = false;
         log.debug("Failed to delete dir: "+dir.getAbsolutePath());
      }

      return success;
   }

   /**
    * Delete a file or directory and all of its contents.
    *
    * @param dirname  The name of the file or directory to delete.
    * @return True if all delete operations were successfull.
    */
   public static boolean delete(final String dirname)
   {
      return delete(new File(dirname));
   }

   /**
    * Delete a directory contaning the given file and all its contents.
    *
    * @param filename a file or directory in the containing directory to delete
    * @return true if all delete operations were successfull, false if any
    * delete failed.
    */
   public static boolean deleteContaining(final String filename)
   {
      File file = new File(filename);
      File containingDir = file.getParentFile();
      return delete(containingDir);
   }

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    * @param buff    The copy buffer.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source,
         final File target,
         final byte buff[])
         throws IOException
   {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));

      int read;

      try
      {
         while ((read = in.read(buff)) != -1)
         {
            out.write(buff, 0, read);
         }
      }
      finally
      {
         Streams.flush(out);
         Streams.close(in);
         Streams.close(out);
      }
   }

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    * @param size    The size of the copy buffer.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source,
         final File target,
         final int size)
         throws IOException
   {
      copy(source, target, new byte[size]);
   }

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source, final File target)
         throws IOException
   {
      copy(source, target, DEFAULT_BUFFER_SIZE);
   }
   
   /**
    * Copy a remote/local URL to a local file
    * 
    * @param src the remote or local URL
    * @param dest the local file
    * @throws IOException upon error
    */
   public static void copy(URL src, File dest) throws IOException
   {
      log.debug("Copying " + src + " -> " + dest);
      
      // Validate that the dest parent directory structure exists
      File dir = dest.getParentFile();
      if (!dir.exists())
      {
         if (!dir.mkdirs())
         {
            throw new IOException("mkdirs failed for: " + dir.getAbsolutePath());
         }
      }
      // Remove any existing dest content
      if (dest.exists())
      {
         if (!Files.delete(dest))
         {
            throw new IOException("delete of previous content failed for: " + dest.getAbsolutePath());
         }
      }
      // Treat local and remote URLs the same
      // prepare streams, do the copy and flush
      InputStream in = new BufferedInputStream(src.openStream());
      OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      Streams.copy(in, out);
      out.flush();
      out.close();
      in.close();
   }
   
   /**
    * Used to encode any string into a string that is safe to use as 
    * a file name on most operating systems.
    * 
    * Use decodeFileName() to get back the original string.
    * 
    * Copied by Adrian's org.jboss.mq.pm.file.PersistenceManager
    * and adapted to use hex instead of decimal digits
    * 
    * @param name the filename to encode
    * @return a filesystem-friendly filename
    */   
   public static String encodeFileName(String name)
   {
      return encodeFileName(name, '@');
   }
   
   /**
    * Used to decode a file system friendly filename produced
    * by encodeFileName() method, above.
    * 
    * Copied by Adrian's org.jboss.mq.pm.file.PersistenceManager
    * and adapted to use hex instead of decimal digits
    * 
    * Note:
    *   Decoding will not work if encoding produced
    *   multi-byte encoded characters. If this is truly
    *   needed we'll have to revise the encoding.
    * 
    * @param name the filename to decode
    * @return the original name
    */
   public static String decodeFileName(String name)
   {
      return decodeFileName(name, '@');
   }
   
   /**
    * See encodeFileName(String) above.
    * 
    * @param name the filename to encode
    * @param escape the escape character to use
    * @return a filesystem-friendly filename
    */ 
   public static String encodeFileName(String name, char escape)
   {
      StringBuffer rc = new StringBuffer();
      for (int i = 0; i < name.length(); i++ )
      {
         switch (name.charAt(i))
         {
            // These are the safe characters...
            case 'a': case 'A': case 'b': case 'B': case 'c': case 'C':
            case 'd': case 'D': case 'e': case 'E': case 'f': case 'F':
            case 'g': case 'G': case 'h': case 'H': case 'i': case 'I':
            case 'j': case 'J': case 'k': case 'K': case 'l': case 'L':
            case 'm': case 'M': case 'n': case 'N': case 'o': case 'O':
            case 'p': case 'P': case 'q': case 'Q': case 'r': case 'R':
            case 's': case 'S': case 't': case 'T': case 'u': case 'U':
            case 'v': case 'V': case 'w': case 'W': case 'x': case 'X':
            case 'y': case 'Y': case 'z': case 'Z':
            case '1': case '2': case '3': case '4': case '5': 
            case '6': case '7': case '8': case '9': case '0': 
            case '-': case '_': case '.':
               rc.append(name.charAt(i));
               break;

            // Any other character needs to be encoded.
            default:
            
               // We encode the characters as <esc>hh,
               // where <esc> is the passed escape character and
               // hh is the hex value of the UTF8 byte of the character.
               // You might get <esc>hh<esc>hh since UTF8 can produce multiple
               // bytes for a single character.
               try
               {
                  byte data[] = ("" + name.charAt(i)).getBytes("UTF8");
                  for (int j = 0; j < data.length; j++)
                  {
                     rc.append(escape);
                     rc.append(hexDigits[ (data[j] >> 4) & 0xF ]); // high order digit
                     rc.append(hexDigits[ (data[j]     ) & 0xF ]); // low order digit                     
                  }
               }
               catch (UnsupportedEncodingException wonthappen)
               {
                  // nada
               }
         }
      }
      return rc.toString();
   }

   /**
    * See decodeFileName(String) above.
    *  
    * @param name the filename to decode
    * @param escape the escape character to use
    * @return the original name
    */
   public static String decodeFileName(String name, char escape)
   {
      if (name == null)
      {
         return null;
      }
      StringBuffer sbuf = new StringBuffer(name.length());
      
      for (int i = 0; i < name.length(); i++)
      {
         char c = name.charAt(i);
         if (c == escape)
         {
            char h1 = name.charAt(++i);
            char h2 = name.charAt(++i);

            // convert hex digits to integers
            int d1 = (h1 >= 'a') ? (10 + h1 - 'a')
                  : ((h1 >= 'A') ? (10 + h1 - 'A') 
                                     :  (h1 - '0'));
            
            int d2 = (h2 >= 'a') ? (10 + h2 - 'a')
                  : ((h2 >= 'A') ? (10 + h2 - 'A')
                                      : (h2 - '0'));
            
            // handling only the <esc>hh case here, as we don't know
            // if <esc>hh<esc>hh belong to the same character
            // (and we are lazy to change the encoding) - REVISIT
            byte[] bytes = new byte[] { (byte)(d1 * 16 + d2) };
            
            try 
            {
               String s = new String(bytes, "UTF8");
               sbuf.append(s);
            }
            catch (UnsupportedEncodingException wonthappen)
            {
               // nada
            }
         }
         else
         {
            sbuf.append(c);
         }
      }
      return sbuf.toString();
   }      

   /**
    * Build a relative path to the given base path.
    * @param base - the path used as the base
    * @param path - the path to compute relative to the base path
    * @return A relative path from base to path
    * @throws IOException
    */ 
   public static String findRelativePath(String base, String path)
      throws IOException
   {
      String a = new File(base).getCanonicalFile().toURI().getPath();
      String b = new File(path).getCanonicalFile().toURI().getPath();
      String[] basePaths = a.split("/");
      String[] otherPaths = b.split("/");
      int n = 0;
      for(; n < basePaths.length && n < otherPaths.length; n ++)
      {
         if( basePaths[n].equals(otherPaths[n]) == false )
            break;
      }
      System.out.println("Common length: "+n);
      StringBuffer tmp = new StringBuffer("../");
      for(int m = n; m < basePaths.length - 1; m ++)
         tmp.append("../");
      for(int m = n; m < otherPaths.length; m ++)
      {
         tmp.append(otherPaths[m]);
         tmp.append("/");
      }

      return tmp.toString();
   }
}
