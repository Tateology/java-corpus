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


// $Id: FeedParser.java 816 2006-12-04 23:40:49Z italobb $

package de.nava.informa.parsers;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.UnsupportedFormatException;
import de.nava.informa.utils.NoOpEntityResolver;
import de.nava.informa.utils.ParserUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Attribute;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;

/**
 * Parser class which allows reading in of RSS news channels.
 * The concrete rules how the XML elements map to our channel object model
 * are delegated to version specific private classes.</p>
 * <p/>
 * Currently the FeedParser support RSS formats 0.9x, 1.0 (RDF), 2.0 and Atom 0.3.
 * <p/>
 * It's possible to specify class of custom XML parser to use instead of
 * standard Crimson parser (JDK). Use <code>setSaxDriverClassName()</code> method
 * for that.
 *
 * @author Niko Schmuck
 */
public class FeedParser {

  private static final Log LOGGER = LogFactory.getLog(FeedParser.class);

  private static String saxDriverClassName = null;

  /**
   * Sets the name of SAX2 Driver class to use for parsing. The class should
   * implement XMLReader interface and should exist in current class-loading
   * paths. The method will check these conditions before accepting specified
   * class name.
   *
   * @param className name of SAX2 Driver class.
   *
   * @throws ClassNotFoundException if class isn't found in the class path.
   * @throws ClassCastException if class isn't implementing XMLReader interface.
   */
  public static synchronized void setSaxDriverClassName(String className)
    throws ClassNotFoundException {

    // Check if class is available
    Class saxDriverClass = Class.forName(className);

    // Check if class implements necessary XMLReader interface
    Class[] extendsImplements = saxDriverClass.getInterfaces();
    boolean found = false;
    for (int i = 0; !found && i < extendsImplements.length; i++) {
      Class parent = extendsImplements[i];
      found = (parent == XMLReader.class);
  }
    if (!found) throw new ClassCastException("Specified class " + className +
      " does not implement XMLReader.");

    saxDriverClassName = className;
  }

  /**
   * Parser feed behind the given URL and build channel.
   *
   * @param cBuilder  specific channel builder to use.
   * @param url       URL to use as data source.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, String url)
                throws IOException, ParseException {
    URL aURL = null;
    try {
      aURL = new URL(url);
    } catch (java.net.MalformedURLException e) {
      LOGGER.warn("Could not create URL for " + url);
    }
    return parse(cBuilder, new InputSource(url), aURL);
  }

  /**
   * Parser feed behind the given URL and build channel.
   *
   * @param cBuilder  specific channel builder to use.
   * @param aURL      URL to use as data source.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, URL aURL)
                throws IOException, ParseException {
    return parse(cBuilder, new InputSource(aURL.toExternalForm()), aURL);
  }

  /**
   * Parse feed presented by Reader and build channel.
   *
   * @param cBuilder  specific channel builder to use.
   * @param reader    reader setup to read feed data.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, Reader reader)
                throws IOException, ParseException {
    return parse(cBuilder, new InputSource(reader), null);
  }

  /**
   * Parse feed presented by InputStream and build channel.
   *
   * @param cBuilder  specific channel builder to use.
   * @param stream    stream setup to read feed data.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, InputStream stream)
                throws IOException, ParseException {
    return parse(cBuilder, new InputSource(stream), null);
  }

  /**
   * Parse feed presented by file and build channel.
   *
   * @param cBuilder  specific channel builder to use.
   * @param aFile     file to read data from.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, File aFile)
                throws IOException, ParseException {
    URL aURL = null;
    try {
      aURL = aFile.toURL();
    } catch (java.net.MalformedURLException e) {
      throw new IOException("File " + aFile + " had invalid URL representation.");
    }
    return parse(cBuilder, new InputSource(aURL.toExternalForm()), aURL);
  }

  /**
   * Parse feed from input source with base location set and create channel.
   *
   * @param cBuilder      specific channel builder to use.
   * @param inpSource     input source of data.
   * @param baseLocation  base location of feed.
   *
   * @return parsed channel.
   *
   * @throws IOException    if IO errors occur.
   * @throws ParseException if parsing is not possible.
   */
  public static ChannelIF parse(ChannelBuilderIF cBuilder, InputSource inpSource,
                                URL baseLocation)
                throws IOException, ParseException {
    // document reading without validation
    SAXBuilder saxBuilder = new SAXBuilder(saxDriverClassName);

    // turn off DTD loading                     
    saxBuilder.setEntityResolver(new NoOpEntityResolver());

    try {
      Document doc = saxBuilder.build(inpSource);
      ChannelIF channel = parse(cBuilder, doc);
      channel.setLocation(baseLocation);
      return channel;
    } catch (JDOMException e) {
      throw new ParseException("Problem parsing " + inpSource.getSystemId() + ": "+ e);
    }
  }

  // ------------------------------------------------------------
  // internal helper methods
  // ------------------------------------------------------------

  private static synchronized ChannelIF parse(ChannelBuilderIF cBuilder,
      Document doc) throws ParseException {

    if (cBuilder == null) {
      throw new RuntimeException("Without builder no channel can " +
                                 "be created.");
    }
    LOGGER.debug("start parsing.");
    // Get the root element (must be rss)
    Element root = doc.getRootElement();
    String rootElement = root.getName().toLowerCase();
    // Decide which parser to use
    if (rootElement.startsWith("rss")) {
      String rssVersion = root.getAttribute("version").getValue();
      if (rssVersion.indexOf("0.91") >= 0) {
        LOGGER.info("Channel uses RSS root element (Version 0.91).");
        return RSS_0_91_Parser.getInstance().parse(cBuilder, root);
      } else if (rssVersion.indexOf("0.92") >= 0) {
        LOGGER.info("Channel uses RSS root element (Version 0.92).");
        // logger.warn("RSS 0.92 not fully supported yet, fall back to 0.91.");
        // TODO: support RSS 0.92 when aware of all subtle differences.
        return RSS_0_91_Parser.getInstance().parse(cBuilder, root);
      } else if (rootElement.indexOf("0.93") >= 0) {
        LOGGER.info("Channel uses RSS root element (Version 0.93).");
        LOGGER.warn("RSS 0.93 not fully supported yet, fall back to 0.91.");
        // TODO: support RSS 0.93 when aware of all subtle differences.
      } else if (rootElement.indexOf("0.94") >= 0) {
        LOGGER.info("Channel uses RSS root element (Version 0.94).");
        LOGGER.warn("RSS 0.94 not fully supported yet, will use RSS 2.0");
        // TODO: support RSS 0.94 when aware of all subtle differences.
        return RSS_2_0_Parser.getInstance().parse(cBuilder, root);
      } else if (rssVersion.indexOf("2.0") >= 0 || rssVersion.equals("2")) {
        LOGGER.info("Channel uses RSS root element (Version 2.0).");
        return RSS_2_0_Parser.getInstance().parse(cBuilder, root);
      } else {
        throw new UnsupportedFormatException("Unsupported RSS version [" +
                                             rssVersion + "].");
      }
    } else if (rootElement.indexOf("rdf") >= 0) {
      return RSS_1_0_Parser.getInstance().parse(cBuilder, root);
    } else if (rootElement.indexOf("feed") >= 0) {
      Attribute versionAttr = root.getAttribute("version");
      Namespace namespace = ParserUtils.getDefaultNS(root);
      if (versionAttr != null) {
          String feedVersion = versionAttr.getValue();
          if (feedVersion.indexOf("0.1") >= 0 || feedVersion.indexOf("0.2") >= 0) {
              LOGGER.info("Channel uses feed root element (Version " + feedVersion + ").");
              LOGGER.warn("This atom version is not really supported yet, assume Atom 0.3 format");
              return Atom_0_3_Parser.getInstance().parse(cBuilder, root);
          } else if (feedVersion.indexOf("0.3") >= 0) {
              LOGGER.info("Channel uses feed root element (Version 0.3).");
              return Atom_0_3_Parser.getInstance().parse(cBuilder, root);
          }
      } else if (namespace != null && namespace.getURI() != null) {
          if ( !namespace.getURI().equals("http://www.w3.org/2005/Atom")) {
              LOGGER.warn("Channel uses unknown namespace in feed root element, assume Atom 1.0 format.");
          } else {
              LOGGER.info("Channel uses feed root element (Atom 1.0 format).");
          }
          return Atom_1_0_Parser.getInstance().parse(cBuilder, root);
      }
    }

    // did not match anything
    throw new UnsupportedFormatException("Unsupported root element [" +
                                         rootElement + "].");
  }

  
  // ==========================================================

  public static void main(String args[]) throws IOException, ParseException {
    
    if (args.length < 2) {
      System.err.println("Usage: java " + FeedParser.class.getName() + 
                         " [-f <filename> | -u <url>]");
      System.exit(1);
    }
    
    String option = args[0];
    String data = args[1];
    
    ChannelIF channel = null;
    
    ChannelBuilderIF builder = new de.nava.informa.impl.basic.ChannelBuilder();
    if (option.trim().startsWith("-f")) {
      channel = FeedParser.parse(builder, new File(data));
    } else {
      channel = FeedParser.parse(builder, new URL(data));
    }

    System.out.println("Channel format: " + channel.getFormat().toString());
    System.out.println(channel);
    System.out.println("containing " + channel.getItems().size() + " items");
    Iterator items = channel.getItems().iterator();
    while (items.hasNext()) {
      ItemIF item = (ItemIF) items.next();
      System.out.println("  - " + item);
    }
  }
}
