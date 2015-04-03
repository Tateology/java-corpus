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

// $Id: Atom_1_0_Parser.java 838 2007-06-24 19:06:06Z wieben $
package de.nava.informa.parsers;

import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.Namespace;

import de.nava.informa.core.*;
import de.nava.informa.utils.AtomParserUtils;
import de.nava.informa.utils.ParserUtils;
import de.nava.informa.impl.hibernate.Category;


/**
 * Parser which reads in document instances according to the Atom 1.0
 * specification and generates a news channel object. Currently the support for
 * the atom syntax is not complete, but work is in progress.
 * <p/>
 * This parser is based on code of Atom 0.3 parser. Some good sources of
 * information regarding Atom 1.0 are:
 * <br/>
 * http://www.atomenabled.org/developers/syndication/atom-format-spec.php
 * http://rakaz.nl/item/moving_from_atom_03_to_10
 * http://www.atomenabled.org/developers/syndication/
 * http://www-128.ibm.com/developerworks/xml/library/x-atom10.html
 *
 * @author Nilesh Bansal
 * @author Benjamin Wiedmann
 */
public class Atom_1_0_Parser implements ChannelParserIF {
  private static final Log LOGGER = LogFactory.getLog(Atom_1_0_Parser.class);

  /**
   * Private constructor suppresses generation of a (public) default constructor.
   */
  private Atom_1_0_Parser() {
  }

  /**
   * Holder of the Atom_1_0_Parser instance.
   */
  private static class Atom_1_0_ParserHolder {
    private static Atom_1_0_Parser instance = new Atom_1_0_Parser();
  }

  /**
   * Get the Atom_1_0_Parser instance.
   */
  public static Atom_1_0_Parser getInstance() {
    return Atom_1_0_ParserHolder.instance;
  }

  /**
   * a semicolon separated list of authors
   */
  static String getAuthorString(List authors, Namespace defNS) {
    String author = "";
    Iterator authorsIt = authors.iterator();

    while (authorsIt.hasNext()) {
      Element authorElt = (Element) authorsIt.next();

      if (authorElt != null) {
        //TODO author may have more information like uri and email
        ParserUtils.matchCaseOfChildren(authorElt, "name");

        if (!"".equals(author)) {
          // if more than one author, a ; separated list
          author += "; ";
        }

        author += authorElt.getChildTextTrim("name", defNS);
      }
    }

    return author;
  }

  /**
   * Returns the content type of element. Default is 'text'.
   */
  private static String getContentType(Element elt) {
    String type = elt.getAttributeValue("type");

    return (type == null) ? "text" : type;
  }

  /**
   * Looks for "content" elements and takes first from them or looks for
   * "summary" element if "content" not found.
   *
   * @param item      item element.
   * @param namespace namespace.
   * @return description for item.
   */
  public static String getDescription(Element item, Namespace namespace) {
    String strDesc = "";
    Element elDesc;

    // TODO there should be some way of knowing if we are returning summary or
    // content
    List contents = item.getChildren("content", namespace);

    if (contents.size() > 0) {
      elDesc = (Element) contents.get(0);
    } else {
      elDesc = item.getChild("summary", namespace);
    }

    if (elDesc != null) {
      strDesc = AtomParserUtils.getValue(elDesc, getMode(elDesc));
    }

    return strDesc;
  }

  /**
   * returns mode of a element based on its mime type
   */
  static String getMode(Element elt) {
    return getMode(getContentType(elt));
  }

  static String getMode(String type) {
    if (type == null) {
      // if type is not specified, text is to be assumed
      return "escaped";
    }

    if ("text".equals(type) || "html".equals(type)) {
      return "escaped";
    } else if ("xhtml".equals(type)) {
      return "xml";
    } else if (type.substring(type.length() - "xml".length()).equals("xml")) {
      return "xml";
    } else {
      return "base64";
    }
  }

  /**
   * @see de.nava.informa.core.ChannelParserIF#parse(de.nava.informa.core.ChannelBuilderIF,org.jdom.Element)
   */
  public ChannelIF parse(ChannelBuilderIF cBuilder, Element channel)
          throws ParseException {
    if (cBuilder == null) {
      throw new RuntimeException("Without builder no channel can "
              + "be created.");
    }

    Date dateParsed = new Date();
    Namespace defNS = ParserUtils.getDefaultNS(channel);

    if (defNS == null) {
      defNS = Namespace.NO_NAMESPACE;
      LOGGER.info("No default namespace found.");
    } else if ((defNS.getURI() == null) ||
            !defNS.getURI().equals("http://www.w3.org/2005/Atom")) {
      LOGGER.warn("Namespace is not really supported, still trying assuming Atom 1.0 format");
    }

    LOGGER.debug("start parsing.");

    // --- read in channel information

    // Lower the case of these tags to simulate case-insensitive parsing
    ParserUtils.matchCaseOfChildren(channel,
            new String[]{
                    "title", "subtitle", "updated",
                    "published", "author", "generator",
                    "rights", "link", "entry"
            });

    // TODO icon and logo: Feed element can have upto 1 logo and icon.
    // TODO id: Feed and all entries have a unique id string. This can
    // be the URL of the website. Supporting this will require API change.
    // TODO: Feed can optionally have category information

    // title element
    ChannelIF chnl = cBuilder.createChannel(channel,
            channel.getChildTextTrim("title",
                    defNS));

    chnl.setFormat(ChannelFormat.ATOM_1_0);

    // description element
    if (channel.getChild("subtitle") != null) {
      chnl.setDescription(channel.getChildTextTrim("subtitle", defNS));
    }

    // TODO: should we use summary element?

    // lastbuild element : updated ?
    Element updated = channel.getChild("updated", defNS);

    if (updated != null) {
      chnl.setPubDate(ParserUtils.getDate(updated.getTextTrim()));
    }

    // author element
    List authors = channel.getChildren("author", defNS);

    chnl.setCreator(getAuthorString(authors, defNS));

    // TODO we are ignoring contributors information

    // generator element
    Element generator = channel.getChild("generator", defNS);

    if (generator != null) {
      chnl.setGenerator(generator.getTextTrim());
    }

    // TODO generator can have URI and version information

    // copyright element
    Element rights = channel.getChild("rights", defNS);

    if (rights != null) {
      chnl.setCopyright(AtomParserUtils.getValue(rights, getMode(rights)));
    }

    List links = channel.getChildren("link", defNS);
    Iterator i = links.iterator();

    URL linkUrl = null;

    while (i.hasNext()) {
      Element linkElement = (Element) i.next();

      // use first 'alternate' link
      // if rel is not present, use first link without rel
      String rel = linkElement.getAttributeValue("rel");
      String href = linkElement.getAttributeValue("href");

      // TODO we need to handle relative links also
      if ((rel == null) && (href != null) && (linkUrl == null)) {
        linkUrl = ParserUtils.getURL(href);
      } else if ((rel != null) && (href != null) && rel.equals("alternate")) {
        linkUrl = ParserUtils.getURL(href);

        break;
      }
    }

    if (linkUrl != null) {
      chnl.setSite(linkUrl);
    }

    List items = channel.getChildren("entry", defNS);

    i = items.iterator();

    while (i.hasNext()) {
      Element item = (Element) i.next();

      // Lower the case of these tags to simulate case-insensitive parsing
      ParserUtils.matchCaseOfChildren(item,
              new String[]{
                      "title", "link", "content", "summary",
                      "published", "author"
              });

      // TODO entry, if copied from some other feed, may have source element
      // TODO each entry can have its own rights declaration

      // get title element
      Element elTitle = item.getChild("title", defNS);
      String strTitle = "<No Title>";

      if (elTitle != null) {
        strTitle = AtomParserUtils.getValue(elTitle, getMode(elTitle));
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

      //TODO enclosure data
      curItem.setFound(dateParsed);

      List itemAuthors = item.getChildren("author", defNS);

      curItem.setCreator(getAuthorString(itemAuthors, defNS));

      // get published element
      Element elIssued = item.getChild("published", defNS);

      if (elIssued == null) {
        // published element may not be present (but updated should be)
        Element elUpdated = item.getChild("updated", defNS);

        // TODO there should be some way to determining which one are we
        // returning
        if (elUpdated != null) {
          curItem.setDate(ParserUtils.getDate(elUpdated.getTextTrim()));
        }
      } else {
        curItem.setDate(ParserUtils.getDate(elIssued.getTextTrim()));
      }

      // get list of category elements
      List elCategoryList = item.getChildren("category", defNS);

      // categories present will be stored here
      Collection<CategoryIF> categories = new ArrayList<CategoryIF>();

      // multiple category elements may be present
      for (Object elCategoryItem : elCategoryList) {

        Element elCategory = (Element) elCategoryItem;

        // notice: atom spec. forbids to have category "term" (="subject")
        // set as inner text of category tags, so we have to read it from
        // the "term" attribute

        if (elCategory != null) {
          // TODO: what if we have more than one category element present?
          // subject would be overwritten each loop and therefore represent only
          // the last category read, so does this make any sense?

          // TODO: what about adding functionality for accessing "label" or "scheme" attributes?
          // if set, a label should be displayed instead of the value set in term

          // we keep this line not to break up things which
          // use getSubject() to read an item category
          curItem.setSubject(elCategory.getAttributeValue("term"));

          Category c = new Category(elCategory.getAttributeValue("term"));

          // add current category to category list
          categories.add(c);
        }
      }

      // assign categories
      curItem.setCategories(categories);
    }

    // set to current date
    chnl.setLastUpdated(dateParsed);

    return chnl;
  }
}
