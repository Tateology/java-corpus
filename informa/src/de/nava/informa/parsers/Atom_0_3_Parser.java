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

// $Id: Atom_0_3_Parser.java 830 2007-01-06 22:18:13Z niko_schmuck $
package de.nava.informa.parsers;

import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.Namespace;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelParserIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.utils.AtomParserUtils;
import de.nava.informa.utils.ParserUtils;


/**
 * Parser which reads in document instances according to the Atom 0.3
 * specification and generates a news channel object. Currently the
 * support for the atom syntax is not complete.
 *
 * @author Niko Schmuck
 */
class Atom_0_3_Parser implements ChannelParserIF {
  static public final Log LOGGER = LogFactory.getLog(Atom_0_3_Parser.class);

  /**
   * Private constructor suppresses generation of a (public) default constructor.
   */
  private Atom_0_3_Parser() {}

  /**
   * Holder of the Atom_0_3_Parser instance.
   */
  private static class Atom_0_3_ParserHolder {
    private static Atom_0_3_Parser instance = new Atom_0_3_Parser();
  } 

  /**
   * Get the Atom_0_3_Parser instance.
   */
  public static Atom_0_3_Parser getInstance() {
    return Atom_0_3_ParserHolder.instance;
  }
  
  static String getValue(Element elt) {
      return AtomParserUtils.getValue(elt, elt.getAttributeValue("mode"));
  }

  /** Returns the content from content element. */
  static String getContent(Element elt) {
    if (elt == null) {
      return "";
    }

    String value = getValue(elt);
    String type = getContentType(elt);

    if ("text/plain".equals(type)) {
      value = ParserUtils.escape(value);
    }

    return value;
  }

  /** Returns the content type of element. Default is 'text/plain' according to Atom draft 0.3. */
  private static String getContentType(Element elt) {
    String type = elt.getAttributeValue("type");

    return (type == null) ? "text/plain" : type;
  }

  /** Returns copyright from element. */
  static String getCopyright(Element elt) {
    return getTitle(elt);
  }

  /**
   * Looks for "content" elements and takes first from them or looks for "summary" element if
   * "content" not found.
   *
   * @param item      item element.
   * @param namespace namespace.
   *
   * @return description for item.
   */
  public static String getDescription(Element item, Namespace namespace) {
    String strDesc = "";
    Element elDesc;

    List contents = item.getChildren("content", namespace);

    if (contents.size() > 0) {
      elDesc = (Element) contents.get(0);
    } else {
      elDesc = item.getChild("summary", namespace);
    }

    if (elDesc != null) {
      strDesc = getValue(elDesc);
    }

    return strDesc;
  }

  /** Returns the title from title element. */
  static String getTitle(Element elt) {
    if (elt == null) {
      return "";
    }

    String type = getContentType(elt);
    String value;

    if ("application/xhtml+xml".equals(type)) {
      value = elt.getValue();
    } else {
      value = AtomParserUtils.getValue(elt, elt.getAttributeValue("mode"));

      if (!"text/plain".equals(type)) {
        value = ParserUtils.unEscape(value);
      }
    }

    return value;
  }

  /**
   * @see de.nava.informa.core.ChannelParserIF#parse(de.nava.informa.core.ChannelBuilderIF, org.jdom.Element)
   */
  public ChannelIF parse(ChannelBuilderIF cBuilder, Element channel)
      throws ParseException {
    if (cBuilder == null) {
      throw new RuntimeException("Without builder no channel can " +
                                 "be created.");
    }

    Date dateParsed = new Date();
    Namespace defNS = ParserUtils.getDefaultNS(channel);

    if (defNS == null) {
      defNS = Namespace.NO_NAMESPACE;
      LOGGER.info("No default namespace found.");
    }

    // RSS 1.0 Dublin Core Module namespace
    Namespace dcNS = ParserUtils.getNamespace(channel, "dc");

    if (dcNS == null) {
      LOGGER.debug("No namespace for dublin core found");
      dcNS = defNS;
    }

    LOGGER.debug("start parsing.");

    // get version attribute
    String formatVersion = "0.3";

    if (channel.getAttribute("version") != null) {
      formatVersion = channel.getAttribute("version").getValue().trim();
      LOGGER.debug("Atom version " + formatVersion + " specified in document.");
    } else {
      LOGGER.info("No format version specified, using default.");
    }

    // --- read in channel information

    // Lower the case of these tags to simulate case-insensitive parsing
    ParserUtils.matchCaseOfChildren(channel,
                                    new String[] {
                                      "title", "description", "tagline", "ttl",
                                      "modified", "author", "generator",
                                      "copyright", "link", "entry"
                                    });

    // title element
    ChannelIF chnl = cBuilder.createChannel(channel,
                                            channel.getChildTextTrim("title",
                                                                     defNS));

    // TODO: support attributes: type, mode
    chnl.setFormat(ChannelFormat.ATOM_0_3);

    // language
    String language = channel.getAttributeValue("lang", Namespace.XML_NAMESPACE);

    if (language != null) {
      chnl.setLanguage(language);
    }

    // description element
    if (channel.getChild("description") != null) {
      chnl.setDescription(channel.getChildTextTrim("description", defNS));
    } else {
      // fallback
      chnl.setDescription(channel.getChildTextTrim("tagline", defNS));
    }

    // ttl in dc namespace
    Element ttl = channel.getChild("ttl", dcNS);

    if (ttl != null) {
      String ttlString = ttl.getTextTrim();

      if (ttlString != null) {
        chnl.setTtl(Integer.parseInt(ttlString));
      }
    }

    //  lastbuild element : modified ?
    Element modified = channel.getChild("modified", defNS);

    if (modified != null) {
      chnl.setPubDate(ParserUtils.getDate(modified.getTextTrim()));
    }

    // TODO : issued value
    /*
    if (modified != null) {
      modified = channel.getChild("issued", defNS);
      chnl.setLastBuildDate (ParserUtils.getDate(modified.getTextTrim()));
    }
    */

    // author element
    Element author = channel.getChild("author", defNS);

    if (author != null) {
      ParserUtils.matchCaseOfChildren(author, "name");
      chnl.setCreator(author.getChildTextTrim("name", defNS));
    }

    // generator element
    Element generator = channel.getChild("generator", defNS);

    if (generator != null) {
      chnl.setGenerator(generator.getTextTrim());
    }

    // copyright element
    Element copyright = channel.getChild("copyright", defNS);

    if (copyright != null) {
      chnl.setCopyright(getCopyright(copyright));
    }

    // n link elements
    // TODO : type attribut of link (text, application...)
    List links = channel.getChildren("link", defNS);
    Iterator i = links.iterator();

    while (i.hasNext()) {
      Element linkElement = (Element) i.next();

      // use first 'alternate' link
      String rel = linkElement.getAttributeValue("rel");
      String href = linkElement.getAttributeValue("href");

      if ((rel != null) && (href != null) && rel.equals("alternate")) {
        URL linkURL = ParserUtils.getURL(href);

        chnl.setSite(linkURL);

        break;
      }

      // TODO: further extraction of link information
    }

    // 1..n entry elements
    List items = channel.getChildren("entry", defNS);

    i = items.iterator();

    while (i.hasNext()) {
      Element item = (Element) i.next();

      // Lower the case of these tags to simulate case-insensitive parsing
      ParserUtils.matchCaseOfChildren(item,
                                      new String[] {
                                        "title", "link", "content", "summary",
                                        "issued", "subject"
                                      });

      // get title element
      // TODO : deal with type attribut
      Element elTitle = item.getChild("title", defNS);
      String strTitle = "<No Title>";

      if (elTitle != null) {
        strTitle = getTitle(elTitle);
        LOGGER.debug("Parsing title " + elTitle.getTextTrim() + "->" +
                     strTitle);
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Entry element found (" + strTitle + ").");
      }

      // get link element
      String strLink = AtomParserUtils.getItemLink(item, defNS);

      // get description element
      String strDesc = getDescription(item, defNS);

      // generate new news item (link to article)
      ItemIF curItem = cBuilder.createItem(item, chnl, strTitle, strDesc,
                                           ParserUtils.getURL(strLink));

      curItem.setFound(dateParsed);

      // get issued element (required)
      Element elIssued = item.getChild("issued", defNS);

      if (elIssued == null) {
        // [adewale@gmail.com, 01-May-2005] Fix for blogs which have
        // 'created' dates, but not 'issued' dates -- in clear contravention
        // of the Atom 0.3 spec.
        Element elCreated = item.getChild("created", defNS);

        if (elCreated != null) {
          curItem.setDate(ParserUtils.getDate(elCreated.getTextTrim()));
        }
      } else {
        curItem.setDate(ParserUtils.getDate(elIssued.getTextTrim()));
      }

      // get subject element
      Element elSubject = item.getChild("subject", dcNS);

      if (elSubject != null) {
        // TODO: Mulitple subject elements not handled currently
        curItem.setSubject(elSubject.getTextTrim());
      }
    }

    // set to current date
    chnl.setLastUpdated(dateParsed);

    return chnl;
  }
}
