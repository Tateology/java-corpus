//
// Informa -- RSS Library for Java
// Copyright (c) 2002 by Niko Schmuck
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


// $Id: FormatDetector.java 763 2005-06-17 13:49:35Z spyromus $

package de.nava.informa.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.UnsupportedFormatException;

/**
 * Utility class for analysing the news channel syntax and mapping to
 * known format to ease further processing.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public final class FormatDetector {

  private static Log logger = LogFactory.getLog(FormatDetector.class);

  private static final int NR_FIRST_BYTES = 2048;


  /**
   * Guess the format of the specified news channel. For performance
   * reason it is wise to minimize the number of format guesses.
   *
   * @param url a url to the news channel.
   * @return The news channel synatx format, currently only RSS 0.91
   *         ({@link de.nava.informa.core.ChannelFormat#RSS_0_91})
   *         and RSS/RDF 1.0
   *         ({@link de.nava.informa.core.ChannelFormat#RSS_1_0})
   *         are recognized.
   * @throws UnsupportedFormatException in case a news channel format
   *         could not be guessed.
   * @throws IOException if the given url cannot be read in.
   */
  public static ChannelFormat getFormat(URL url)
    throws IOException, UnsupportedFormatException {

    logger.info("Trying to retrieve stream from " + url);
    BufferedInputStream in = new BufferedInputStream(url.openStream(),
                                                     NR_FIRST_BYTES);
     return getFormat(in);
   }

   /**
    * Guess the format of the specified news channel. For performance
    * reason it is wise to minimize the number of format guesses.
    *
    * @param in  an InputStream to the news channel.
    * @return The news channel synatx format, currently only RSS 0.91
    *         ({@link de.nava.informa.core.ChannelFormat#RSS_0_91})
    *         and RSS/RDF 1.0
    *         ({@link de.nava.informa.core.ChannelFormat#RSS_1_0})
    *         are recognized.
    * @throws UnsupportedFormatException in case a news channel format
    *         could not be guessed.
    * @throws IOException if the given url cannot be read in.
    */
   public static ChannelFormat getFormat(InputStream in)
       throws IOException, UnsupportedFormatException {

    byte[] b = new byte[NR_FIRST_BYTES];

    int bytesRead = 0;
    while (bytesRead < NR_FIRST_BYTES) {
      int bytes = in.read(b, bytesRead, NR_FIRST_BYTES - bytesRead);
      if (bytes == -1) break;
      bytesRead += bytes;
    }

    String rootElement = getRootElement(b);
    logger.debug("Detected [" + rootElement + "].");
    if (rootElement.startsWith("rss")) {
      if (rootElement.indexOf("0.91") > 0) {
        logger.info("Channel uses RSS root element (Version 0.91).");
        return ChannelFormat.RSS_0_91;
      } else if (rootElement.indexOf("0.92") > 0) {
        logger.info("Channel uses RSS root element (Version 0.92).");
        // FIXME: should really return ChannelFormat.RSS_0_92
        // when aware of all subtle differences.
        return ChannelFormat.RSS_0_92;
      } else if (rootElement.indexOf("0.93") > 0) {
        logger.info("Channel uses RSS root element (Version 0.93).");
        logger.warn("RSS 0.93 not fully supported yet, fall back to 0.92.");
        // FIXME: should really return ChannelFormat.RSS_0_93
        // when aware of all subtle differences.
        return ChannelFormat.RSS_0_92;
      } else if (rootElement.indexOf("0.94") > 0) {
        logger.info("Channel uses RSS root element (Version 0.94).");
        logger.warn("RSS 0.94 not fully supported yet, fall back to 0.92.");
        // FIXME: should really return ChannelFormat.RSS_0_94
        // when aware of all subtle differences.
        return ChannelFormat.RSS_0_92;
      } else if (rootElement.indexOf("2.0") > 0) {
        logger.info("Channel uses RSS root element (Version 2.0).");
        return ChannelFormat.RSS_2_0;
      } else {
        throw new UnsupportedFormatException("Unsupported RSS version [" +
                                             rootElement + "].");
      }
    } else if (rootElement.indexOf("rdf") >= 0) {
      logger.info("Channel uses RDF root element.");
      return ChannelFormat.RSS_1_0;
    } else if (rootElement.indexOf("feed") >= 0) {
      if (rootElement.indexOf("0.1") >= 0) {
        return ChannelFormat.ATOM_0_1;
      } else if (rootElement.indexOf("0.2") >= 0) {
        return ChannelFormat.ATOM_0_2;
      } else if (rootElement.indexOf("0.3") >= 0) {
        return ChannelFormat.ATOM_0_3;
      } else {
        throw new UnsupportedFormatException("Unsupported ATOM version [" +rootElement + "].");
      }
    } else {
      throw new UnsupportedFormatException("Not able to parse document " +
                                         "with root element [" +
                                         rootElement + "].");
    }
  }

  /**
   * Gets the name of the root element and the attributes (inclusive
   * namespace declarations).
   */
  private static final String getRootElement(byte[] b) {
    String s = new String(b);
    int startPos = 0;
    int endPos = 0;
    boolean inComment = false;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '<' && Character.isLetter(s.charAt(i+1))
          && !inComment) {
        startPos = i + 1;
        for (int j = i + 1; j < s.length(); j++) {
          if (s.charAt(j) == '>') {
            endPos = j;
            break;
          }
        }
        break;
      }
      else if (!inComment && s.charAt(i) == '<' && s.charAt(i+1) == '!'
          && s.charAt(i+2) == '-' && s.charAt(i+3) == '-')
        inComment = true;
      else if (inComment && s.charAt(i) == '-' && s.charAt(i+1) == '-'
          && s.charAt(i+2) == '>')
        inComment = false;
    } // for i
    if (startPos >= 0 && endPos >= 0 && endPos > startPos) {
      return s.substring(startPos, endPos);
    } else {
      throw new IllegalArgumentException("Unable to retrieve root " +
                                         "element from " + s);
    }
  }

}
