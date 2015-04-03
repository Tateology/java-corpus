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

package de.nava.informa.parsers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelParserIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ItemEnclosureIF;
import de.nava.informa.core.ItemGuidIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemSourceIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.TextInputIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.utils.ParserUtils;

/**
 * Parser which reads in document instances according to the RSS 2.0
 * specification and generates a news channel object.
 *
 * @author Anthony Eden
 * @author Niko Schmuck
 * @author Benjamin Wiedmann
 */
class RSS_2_0_Parser implements ChannelParserIF {

  private static Log logger = LogFactory.getLog(RSS_2_0_Parser.class);

  /**
   * Private constructor suppresses generation of a (public) default constructor.
   */
  private RSS_2_0_Parser() {
  }

  /**
   * Holder of the RSS_2_0_Parser instance.
   */
  private static class RSS_2_0_ParserHolder {
    private static RSS_2_0_Parser instance = new RSS_2_0_Parser();
  }

  /**
   * Get the RSS_2_0_Parser instance.
   */
  public static RSS_2_0_Parser getInstance() {
    return RSS_2_0_ParserHolder.instance;
  }

  private CategoryIF getCategoryList(CategoryIF parent, String title,
                                     Hashtable children) {
    // Assuming category hierarchy for each category element
    // is already mapped out into Hashtable tree;  Hense the children Hashtable

    // create channel builder to help create CategoryIF objects
    ChannelBuilder builder = new ChannelBuilder();

    // create current CategoryIF object; Parent may be null if at top level
    CategoryIF cat = builder.createCategory(parent, title);
    // iterate off list of keys from children list
    Enumeration itChild = children.keys();
    while (itChild.hasMoreElements()) {
      String childKey = (String) itChild.nextElement();
      // don't need to keep track of return CategoryIF since it will be added as child of another instance
      getCategoryList(cat, childKey, (Hashtable) children.get(childKey));
    }
    return cat;
  }

  /**
   * @see de.nava.informa.core.ChannelParserIF#parse(de.nava.informa.core.ChannelBuilderIF,org.jdom.Element)
   */
  public ChannelIF parse(ChannelBuilderIF cBuilder, Element root)
          throws ParseException {
    if (cBuilder == null) {
      throw new RuntimeException("Without builder no channel can "
              + "be created.");
    }
    Date dateParsed = new Date();
    logger.debug("start parsing.");

    Namespace defNS = ParserUtils.getDefaultNS(root);
    if (defNS == null) {
      defNS = Namespace.NO_NAMESPACE;
      logger.info("No default namespace found.");
    }
    Namespace dcNS = ParserUtils.getNamespace(root, "dc");
    // fall back to default name space
    if (dcNS == null) {
      dcNS = defNS;
    }

    // Content namespace
    Namespace contentNS = ParserUtils.getNamespace(root, "content");
    // fall back to default name space
    if (contentNS == null) {
      contentNS = defNS;
    }

    ParserUtils.matchCaseOfChildren(root, "channel");

    // Get the channel element (only one occurs)
    Element channel = root.getChild("channel", defNS);
    if (channel == null) {
      logger.warn("Channel element could not be retrieved from feed.");
      throw new ParseException("No channel element found in feed.");
    }

    // --- read in channel information

    ParserUtils.matchCaseOfChildren(channel, new String[]{"title",
            "description", "link", "language", "item", "image", "textinput",
            "copyright", "rating", "docs", "generator", "pubDate", "lastBuildDate",
            "category", "managingEditor", "webMaster", "cloud"});

    // 1 title element
    ChannelIF chnl = cBuilder.createChannel(channel, channel.getChildTextTrim(
            "title", defNS));

    // set channel format
    chnl.setFormat(ChannelFormat.RSS_2_0);

    // 1 description element
    chnl.setDescription(channel.getChildTextTrim("description", defNS));

    // 1 link element
    chnl.setSite(ParserUtils.getURL(channel.getChildTextTrim("link", defNS)));

    // 1 language element
    chnl.setLanguage(channel.getChildTextTrim("language", defNS));

    // 1..n item elements
    List items = channel.getChildren("item", defNS);
    Iterator i = items.iterator();
    while (i.hasNext()) {
      Element item = (Element) i.next();

      ParserUtils.matchCaseOfChildren(item, new String[]{"title", "link",
              "encoded", "description", "subject", "category", "pubDate", "date",
              "author", "creator", "comments", "guid", "source", "enclosure"});

      // get title element
      Element elTitle = item.getChild("title", defNS);
      String strTitle = "<No Title>";
      if (elTitle != null) {
        strTitle = elTitle.getTextTrim();
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Item element found (" + strTitle + ").");
      }

      // get link element
      Element elLink = item.getChild("link", defNS);
      String strLink = "";
      if (elLink != null) {
        strLink = elLink.getTextTrim();
      }

      // get description element
      Element elDesc = item.getChild("encoded", contentNS);
      if (elDesc == null) {
        elDesc = item.getChild("description", defNS);
      }
      String strDesc = "";
      if (elDesc != null) {
        strDesc = elDesc.getTextTrim();
      }

      // generate new RSS item (link to article)
      ItemIF rssItem = cBuilder.createItem(item, chnl, strTitle, strDesc,
              ParserUtils.getURL(strLink));

      // get subject element
      Element elSubject = item.getChild("subject", defNS);
      if (elSubject == null) {
        // fallback mechanism: get dc:subject element
        elSubject = item.getChild("subject", dcNS);
      }
      if (elSubject != null) {
        rssItem.setSubject(elSubject.getTextTrim());
      }

      // get category list
      // get list of <category> elements
      List listCategory = item.getChildren("category", defNS);
      if (listCategory.size() < 1) {
        // fallback mechanism: get dc:category element
        listCategory = item.getChildren("category", dcNS);
      }
      if (listCategory.size() > 0) {
        RecursiveHashtable<String> catTable = new RecursiveHashtable<String>();

        // for each category, parse hierarchy
        Iterator itCat = listCategory.iterator();
        while (itCat.hasNext()) {
          RecursiveHashtable<String> currTable = catTable;
          Element elCategory = (Element) itCat.next();
          // get contents of category element
          String[] titles = elCategory.getTextNormalize().split("/");
          for (int x = 0; x < titles.length; x++) {
            // tokenize category string to extract out hierarchy
            if (currTable.containsKey(titles[x]) == false) {
              // if token does not exist in current map, add it with child Hashtable
              currTable.put(titles[x], new RecursiveHashtable<String>());
            }
            // reset current Hashtable to child's Hashtable then iterate to next token
            currTable = currTable.get(titles[x]);
          }
        }
        ArrayList<CategoryIF> catList = new ArrayList<CategoryIF>();
        // transform cat list & hierarchy into list of CategoryIF elements
        Enumeration<String> enumCategories = catTable.keys();
        while (enumCategories.hasMoreElements()) {
          String key = enumCategories.nextElement();
          // build category list: getCategoryList(parent, title, children)
          CategoryIF cat = getCategoryList(null, key, catTable.get(key));
          catList.add(cat);
        }
        if (catList.size() > 0) {
          // if categories were actually created, then add list to item node
          rssItem.setCategories(catList);
        }
      }

      // get publication date
      Element elDate = item.getChild("pubDate", defNS);
      if (elDate == null) {
        // fallback mechanism: get dc:date element
        elDate = item.getChild("date", dcNS);
      }
      if (elDate != null) {
        rssItem.setDate(ParserUtils.getDate(elDate.getTextTrim()));
      }

      rssItem.setFound(dateParsed);

      // get Author element
      Element elAuthor = item.getChild("author", defNS);
      if (elAuthor == null) {
        // fallback mechanism: get dc:creator element
        elAuthor = item.getChild("creator", dcNS);
      }
      if (elAuthor != null)
        rssItem.setCreator(elAuthor.getTextTrim());

      // get Comments element
      Element elComments = item.getChild("comments", defNS);
      String strComments = "";
      if (elComments != null) {
        strComments = elComments.getTextTrim();
      }
      rssItem.setComments(ParserUtils.getURL(strComments));

      // get guid element
      Element elGuid = item.getChild("guid", defNS);
      if (elGuid != null) {
        String guidUrl = elGuid.getTextTrim();
        if (guidUrl != null) {
          boolean permaLink = true;
          Attribute permaLinkAttribute = elGuid.getAttribute("isPermaLink",
                  defNS);
          if (permaLinkAttribute != null) {
            String permaLinkStr = permaLinkAttribute.getValue();
            if (permaLinkStr != null) {
              permaLink = Boolean.valueOf(permaLinkStr).booleanValue();
            }
          }
          ItemGuidIF itemGuid = cBuilder.createItemGuid(rssItem, guidUrl,
                  permaLink);
          rssItem.setGuid(itemGuid);
        }
      }

      // get source element
      Element elSource = item.getChild("source", defNS);
      if (elSource != null) {
        String sourceName = elSource.getTextTrim();
        Attribute sourceAttribute = elSource.getAttribute("url", defNS);
        if (sourceAttribute != null) {
          String sourceLocation = sourceAttribute.getValue().trim();
          ItemSourceIF itemSource = cBuilder.createItemSource(rssItem,
                  sourceName, sourceLocation, null);
          rssItem.setSource(itemSource);
        }
      }

      // get enclosure element
      Element elEnclosure = item.getChild("enclosure", defNS);
      if (elEnclosure != null) {
        URL location = null;
        String type = null;
        int length = -1;
        Attribute urlAttribute = elEnclosure.getAttribute("url", defNS);
        if (urlAttribute != null) {
          location = ParserUtils.getURL(urlAttribute.getValue().trim());
        }
        Attribute typeAttribute = elEnclosure.getAttribute("type", defNS);
        if (typeAttribute != null) {
          type = typeAttribute.getValue().trim();
        }
        Attribute lengthAttribute = elEnclosure.getAttribute("length", defNS);
        if (lengthAttribute != null) {
          try {
            length = Integer.parseInt(lengthAttribute.getValue().trim());
          } catch (NumberFormatException e) {
            logger.warn(e);
          }
        }
        ItemEnclosureIF itemEnclosure = cBuilder.createItemEnclosure(rssItem,
                location, type, length);
        rssItem.setEnclosure(itemEnclosure);
      }
    }

    // 0..1 image element
    Element image = channel.getChild("image", defNS);
    if (image != null) {

      ParserUtils.matchCaseOfChildren(image, new String[]{"title", "url",
              "link", "width", "height", "description"});

      ImageIF rssImage = cBuilder.createImage(image.getChildTextTrim("title",
              defNS), ParserUtils.getURL(image.getChildTextTrim("url", defNS)),
              ParserUtils.getURL(image.getChildTextTrim("link", defNS)));
      Element imgWidth = image.getChild("width", defNS);
      if (imgWidth != null) {
        try {
          rssImage.setWidth(Integer.parseInt(imgWidth.getTextTrim()));
        } catch (NumberFormatException e) {
          logger.warn("Error parsing width: " + e.getMessage());
        }
      }
      Element imgHeight = image.getChild("height", defNS);
      if (imgHeight != null) {
        try {
          rssImage.setHeight(Integer.parseInt(imgHeight.getTextTrim()));
        } catch (NumberFormatException e) {
          logger.warn("Error parsing height: " + e.getMessage());
        }
      }
      Element imgDescr = image.getChild("description", defNS);
      if (imgDescr != null) {
        rssImage.setDescription(imgDescr.getTextTrim());
      }
      chnl.setImage(rssImage);
    }

    // 0..1 textinput element
    Element txtinp = channel.getChild("textinput", defNS);
    if (txtinp != null) {

      ParserUtils.matchCaseOfChildren(txtinp, new String[]{"title",
              "description", "name", "link"});

      TextInputIF rssTextInput = cBuilder.createTextInput(txtinp
              .getChildTextTrim("title", defNS), txtinp.getChildTextTrim(
              "description", defNS), txtinp.getChildTextTrim("name", defNS),
              ParserUtils.getURL(txtinp.getChildTextTrim("link", defNS)));
      chnl.setTextInput(rssTextInput);
    }

    // 0..1 copyright element
    Element copyright = channel.getChild("copyright", defNS);
    if (copyright != null) {
      chnl.setCopyright(copyright.getTextTrim());
    }

    // 0..1 Rating element
    Element rating = channel.getChild("rating", defNS);
    if (rating != null) {
      chnl.setRating(rating.getTextTrim());
    }

    // 0..1 Docs element
    Element docs = channel.getChild("docs", defNS);
    if (docs != null) {
      chnl.setDocs(docs.getTextTrim());
    }

    // 0..1 Generator element
    Element generator = channel.getChild("generator", defNS);
    if (generator != null) {
      chnl.setGenerator(generator.getTextTrim());
    }

    // 0..1 ttl element
    Element ttl = channel.getChild("ttl", defNS);
    if (ttl != null) {
      String ttlValue = ttl.getTextTrim();
      try {
        chnl.setTtl(Integer.parseInt(ttlValue));
      } catch (NumberFormatException e) {
        logger.warn("Invalid TTL format: '" + ttlValue + "'");
      }
    }

    // 0..1 pubDate element
    Element pubDate = channel.getChild("pubDate", defNS);
    if (pubDate != null) {
      chnl.setPubDate(ParserUtils.getDate(pubDate.getTextTrim()));
    }

    // 0..1 lastBuildDate element
    Element lastBuildDate = channel.getChild("lastBuildDate", defNS);
    if (lastBuildDate != null) {
      chnl.setLastBuildDate(ParserUtils.getDate(lastBuildDate.getTextTrim()));
    }

    // get category list
    // get list of <category> elements
    List listCategory = channel.getChildren("category", defNS);
    if (listCategory.size() < 1) {
      // fallback mechanism: get dc:category element
      listCategory = channel.getChildren("category", dcNS);
    }
    if (listCategory.size() > 0) {
      RecursiveHashtable<String> catTable = new RecursiveHashtable<String>();
      // for each category, parse hierarchy
      Iterator itCat = listCategory.iterator();
      while (itCat.hasNext()) {
        RecursiveHashtable<String> currTable = catTable;
        Element elCategory = (Element) itCat.next();
        // get contents of category element
        String[] titles = elCategory.getTextNormalize().split("/");
        for (int x = 0; x < titles.length; x++) {
          // tokenize category string to extract out hierarchy
          if (currTable.containsKey(titles[x]) == false) {
            // if token does not exist in current map, add it with child Hashtable
            currTable.put(titles[x], new RecursiveHashtable<String>());
          }
          // reset current Hashtable to child's Hashtable then iterate to next token
          currTable = currTable.get(titles[x]);
        }
      }
      ArrayList<CategoryIF> catList = new ArrayList<CategoryIF>();
      // transform cat list & hierarchy into list of CategoryIF elements
      Enumeration<String> enumCategories = catTable.keys();
      while (enumCategories.hasMoreElements()) {
        String key = enumCategories.nextElement();
        // build category list: getCategoryList(parent, title, children)
        CategoryIF cat = getCategoryList(null, key, catTable.get(key));
        catList.add(cat);
      }
      if (catList.size() > 0) {
        // if categories were actually created, then add list to item node
        chnl.setCategories(catList);
      }
    }

    // 0..1 managingEditor element
    Element managingEditor = channel.getChild("managingEditor", defNS);
    if (managingEditor != null) {
      chnl.setCreator(managingEditor.getTextTrim());
    }

    // 0..1 webMaster element
    Element webMaster = channel.getChild("webMaster", defNS);
    if (webMaster != null) {
      chnl.setPublisher(webMaster.getTextTrim());
    }

    // 0..1 cloud element
    Element cloud = channel.getChild("cloud", defNS);
    if (cloud != null) {
      String _port = cloud.getAttributeValue("port", defNS);
      int port = -1;
      if (_port != null) {
        try {
          port = Integer.parseInt(_port);
        } catch (NumberFormatException e) {
          logger.warn(e);
        }
      }
      chnl.setCloud(cBuilder.createCloud(cloud.getAttributeValue("domain",
              defNS), port, cloud.getAttributeValue("path", defNS), cloud
              .getAttributeValue("registerProcedure", defNS), cloud
              .getAttributeValue("protocol", defNS)));
    }

    chnl.setLastUpdated(dateParsed);

    // 0..1 skipHours element
    // 0..1 skipDays element

    return chnl;
  }

  /**
   * Implement type safety in a hashtable of hashtables.
   *
   * @author Italo Borssatto
   */
  private static class RecursiveHashtable<T> extends
          Hashtable<T, RecursiveHashtable<T>> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3748524793347081535L;

    /**
     * @see java.util.Hashtable#put(java.lang.Object,java.lang.Object)
     */
    @Override
    public synchronized RecursiveHashtable<T> put(T key,
                                                  RecursiveHashtable<T> value) {
      return super.put(key, value);
    }
  }

}
