//
// Informa -- RSS Library for Java
// Copyright (c) 2002-2003 by Niko Schmuck
//
// Niko Schmuck
// http://sourceforge.net/projects/informa
// mailto:niko_schmuck@users.sourceforge.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE. If the license is not included with this distribution,
// you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
// or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
// MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//


// $Id: FileUtils.java 231 2003-07-26 10:04:38Z niko_schmuck $

package de.nava.informa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class providing some convenience methods when handling files.
 */
public final class FileUtils {

  private static Log logger = LogFactory.getLog(FileUtils.class);

  private FileUtils() {
  }

  public static boolean compare(String nameExpected, String nameActual)
          throws IOException {

    return compare(new File(nameExpected), new File(nameActual));
  }

  public static boolean compare(File fileExpected, File fileActual)
          throws IOException {

    BufferedReader readExpected;
    try {
      logger.debug("Comparing golden file " + fileExpected +
                   " to " + fileActual);
      readExpected = new BufferedReader(new FileReader(fileExpected));
    } catch (IOException e) {
      logger.error("Could not read baseline: " + e);
      return false;
    }
    BufferedReader readActual =
            new BufferedReader(new FileReader(fileActual));
    return compare(readExpected, readActual);
  }

  private static boolean compare(BufferedReader readerExpected,
                                 BufferedReader readerActual)
          throws IOException {

    String lineExpected = readerExpected.readLine();
    String lineActual = readerActual.readLine();
    while (lineExpected != null && lineActual != null) {
      if (lineExpected == null || lineActual == null) {
        return false;
      }
      if (!lineExpected.equals(lineActual)) {
        return false;
      }
      lineExpected = readerExpected.readLine();
      lineActual = readerActual.readLine();
    }
    readerExpected.close();
    readerActual.close();
    return lineExpected == null && lineActual == null;
  }

  /**
   * Copies a file from <code>inFile</copy> to <code>outFile</code>.
   */
  public static void copyFile(File inFile, File outFile) {
    try {
      logger.debug("Copying file " + inFile + " to " + outFile);
      InputStream in = new FileInputStream(inFile);
      OutputStream out = new FileOutputStream(outFile);
      byte[] buf = new byte[8 * 1024];
      int n;
      while ((n = in.read(buf)) >= 0) {
        out.write(buf, 0, n);
        out.flush();
      }
      in.close();
      out.close();
    } catch (Exception e) {
      logger.warn("Error occurred while copying file " + inFile + " to " + outFile);
      e.printStackTrace();
    }
  }

}
