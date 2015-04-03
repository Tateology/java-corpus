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

// $Id: OPMLParser.java 841 2007-06-27 18:08:20Z wieben $

package de.nava.informa.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Attribute;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import de.nava.informa.core.FeedIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.UnsupportedFormatException;
import de.nava.informa.utils.NoOpEntityResolver;

/**
 * OPML (Outline processor markup language) parser for to read in a collection
 * of news channels (feeds) that will be made available as news channel object
 * model.</p>
 * <p/>
 * Currently OPML version 1.1 is supported.
 *
 * @author Niko Schmuck
 * @author Benjamin Wiedmann
 * @see de.nava.informa.core.FeedIF
 */
public class OPMLParser {

  private static Log logger = LogFactory.getLog(OPMLParser.class);

  private OPMLParser() {
  }

  public static Collection parse(URL aURL) throws IOException, ParseException {
    return parse(new InputSource(aURL.toExternalForm()), aURL);
  }

  /**
   * Reads in a news feed definition from the specified URL.
   *
   * @return A collection of <code>FeedIF</code> objects.
   */
  public static Collection parse(String url) throws IOException, ParseException {
    URL aURL = null;
    try {
      aURL = new URL(url);
    } catch (java.net.MalformedURLException e) {
      logger.warn("Could not create URL for " + url);
    }
    return parse(new InputSource(url), aURL);
  }

  public static Collection parse(Reader reader) throws IOException, ParseException {
    return parse(new InputSource(reader), null);
  }

  public static Collection parse(InputStream stream) throws IOException, ParseException {
    return parse(new InputSource(stream), null);
  }

  public static Collection<FeedIF> parse(File aFile) throws IOException, ParseException {
    URL aURL = null;
    try {
      aURL = aFile.toURL();
    } catch (java.net.MalformedURLException e) {
      throw new IOException("File " + aFile + " had invalid URL " +
              "representation.");
    }
    return parse(new InputSource(aURL.toExternalForm()), aURL);
  }

  public static Collection<FeedIF> parse(InputSource inpSource,
                                         URL baseLocation) throws IOException, ParseException {
    // document reading without validation
    SAXBuilder saxBuilder = new SAXBuilder(false);
    // turn off DTD loading
    saxBuilder.setEntityResolver(new NoOpEntityResolver());
    try {
      Document doc = saxBuilder.build(inpSource);
      return parse(doc);
    } catch (JDOMException e) {
      throw new ParseException(e);
    }
  }

  // ------------------------------------------------------------
  // internal helper methods
  // ------------------------------------------------------------

  private static synchronized Collection<FeedIF> parse(Document doc) throws ParseException {

    logger.debug("start parsing.");
    // Get the root element (must be opml)
    Element root = doc.getRootElement();
    String rootElement = root.getName().toLowerCase();
    String opmlVersion = null;

    // Decide which parser to use
    if (rootElement.startsWith("opml")) {
      Attribute attrOpmlVersion = root.getAttribute("version");

      // there is no version information set
      if (attrOpmlVersion == null) {
        // there seems to be no opml version set, so we'll try to parse it with 1.1..
        // TODO is it worth to implement also a opml 1.0 parser? are there markable differences between opml 1.1 and 1.0?
        logger.info("Collection uses OPML root element (no version information available), trying to parse with 1.1 anyway.");
        return OPML_1_1_Parser.parse(root);
      } else {
        // version information seems to be set, so go get it
        opmlVersion = attrOpmlVersion.getValue();
      }

      // version information is set
      if (opmlVersion.indexOf("1.1") >= 0) {
        // OPML 1.1 version information is set, so we'll parse it with 1.1!
        logger.info("Collection uses OPML root element (Version 1.1).");
        return OPML_1_1_Parser.parse(root);
      } else if (opmlVersion.indexOf("1.0") >= 0) {
        // TODO is it worth to implement also a opml 1.0 parser? are there markable differences between opml 1.1 and 1.0?
        // OPML 1.0 version information is set, we'll try to parse it using 1.1 anyway..
        logger.info("Collection uses OPML root element (Version 1.0), trying to parse with 1.1 anyway.");
        return OPML_1_1_Parser.parse(root);
      } else {
        // since it is neither a 1.0 nor 1.1 opml feed we maybe cannot handle it, so it's better to throw some exception..
        throw new UnsupportedFormatException(MessageFormat.format("Unsupported OPML version information [{0}].", opmlVersion));
      }
    }

    // did not match anything
    throw new UnsupportedFormatException(MessageFormat.format("Unsupported OPML root element [{0}].", rootElement));
  }

}
