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

// $Id: RSS_1_0_Parser.java 830 2007-01-06 22:18:13Z niko_schmuck $

package de.nava.informa.parsers;

import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelParserIF;
import de.nava.informa.core.ChannelUpdatePeriod;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemSourceIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.TextInputIF;
import de.nava.informa.utils.ParserUtils;

/**
 * Parser which reads in document instances according to the RSS 1.0
 * (RDF) specification and generates a news channel object.
 *
 * @author Niko Schmuck
 */
class RSS_1_0_Parser implements ChannelParserIF {

  private static Log logger = LogFactory.getLog(RSS_1_0_Parser.class);
  
  /**
   * Private constructor suppresses generation of a (public) default constructor.
   */
  private RSS_1_0_Parser() {}

  /**
   * Holder of the RSS_1_0_Parser instance.
   */
  private static class RSS_1_0_ParserHolder {
    private static RSS_1_0_Parser instance = new RSS_1_0_Parser();
  } 

  /**
   * Get the RSS_1_0_Parser instance.
   */
  public static RSS_1_0_Parser getInstance() {
    return RSS_1_0_ParserHolder.instance;
  }

  public ChannelIF parse(ChannelBuilderIF cBuilder, Element root)
      throws ParseException {
    if (cBuilder == null) {
      throw new RuntimeException("Without builder no channel can "
          + "be created.");
    }
    Date dateParsed = new Date();
    Namespace defNS = ParserUtils.getDefaultNS(root);
    if (defNS == null) {
      defNS = Namespace.NO_NAMESPACE;
      logger.info("No default namespace found.");
    }

    // RSS 1.0 Dublin Core Module namespace
    Namespace dcNS = ParserUtils.getNamespace(root, "dc");
    // fall back to default name space (for retrieving descriptions)
    if (dcNS == null) {
      dcNS = defNS;
    }

    // RSS 1.0 Syndication Module namespace
    Namespace syNS = ParserUtils.getNamespace(root, "sy");

    // RSS 1.0 Aggregation Module namespace
    Namespace agNS = ParserUtils.getNamespace(root, "ag");

    // RSS 1.0 Administration Module namespace
    Namespace adminNS = ParserUtils.getNamespace(root, "admin");

    // RSS 1.0 DCTerms Module namespace
    Namespace dctermsNS = ParserUtils.getNamespace(root, "dcterms");

    // RSS 1.0 Annotation Module namespace
    Namespace annotateNS = ParserUtils.getNamespace(root, "annotate");

    // RSS091 Module namespace
    Namespace rss091NS = ParserUtils.getNamespace(root, "rss091");

    // Content namespace
    Namespace contentNS = ParserUtils.getNamespace(root, "content");

    ParserUtils.matchCaseOfChildren(root, new String[] { "channel", "item",
        "image", "textinput" });

    // Get the channel element (only one occurs)
    Element channel = root.getChild("channel", defNS);
    if (channel == null) {
      logger.warn("Channel element could not be retrieved from feed.");
      throw new ParseException("No channel element found in feed.");
    }

    // ----------------------- read in channel information

    ParserUtils.matchCaseOfChildren(channel, new String[] { "title",
        "description", "link", "creator", "managingEditor", "publisher",
        "errorReportsTo", "webMaster", "language", "rights", "copyright",
        "rating", "date", "issued", "pubdate", "lastBuildDate", "modified",
        "generatorAgent", "updatePeriod", "updateFrequency", "updateBase" });

    // title element
    ChannelIF chnl = cBuilder.createChannel(channel, channel.getChildTextTrim(
        "title", defNS));

    // set channel format
    chnl.setFormat(ChannelFormat.RSS_1_0);

    // description element
    chnl.setDescription(channel.getChildTextTrim("description", defNS));

    // link element
    chnl.setSite(ParserUtils.getURL(channel.getChildTextTrim("link", defNS)));

    // creator element
    Element creator = channel.getChild("creator", dcNS);
    if (creator == null) {
      creator = channel.getChild("managingEditor", rss091NS);
    }
    if (creator != null) {
      chnl.setCreator(creator.getTextTrim());
    }

    // publisher element
    String publisher = channel.getChildTextTrim("publisher", dcNS);
    if (publisher == null) {
      Element elErrorReportsTo = channel.getChild("errorReportsTo", adminNS);
      if (elErrorReportsTo != null) {
        publisher = elErrorReportsTo.getAttributeValue("resource", ParserUtils
            .getNamespace(elErrorReportsTo, "rdf"));
      }
    }
    if (publisher == null) {
      publisher = channel.getChildTextTrim("webMaster", rss091NS);
    }
    chnl.setPublisher(publisher);

    // language element
    Element language = channel.getChild("language", dcNS);
    if (language == null) {
      language = channel.getChild("language", rss091NS);
    }
    if (language != null) {
      chnl.setLanguage(language.getTextTrim());
    }

    // rights element
    Element copyright = channel.getChild("rights", dcNS);
    if (copyright == null) {
      copyright = channel.getChild("copyright", rss091NS);
    }
    if (copyright != null) {
      chnl.setCopyright(copyright.getTextTrim());
    }

    // 0..1 Rating element
    Element rating = channel.getChild("rating", rss091NS);
    if (rating != null) {
      chnl.setRating(rating.getTextTrim());
    }

    // 0..1 Docs element
    // use namespace URI
    chnl.setDocs(defNS.getURI());

    // 0..1 pubDate element
    Element pubDate = channel.getChild("date", dcNS);
    if (pubDate == null) {
      pubDate = channel.getChild("issued", dctermsNS);
    }
    if (pubDate == null) {
      pubDate = channel.getChild("pubdate", rss091NS);
    }
    if (pubDate != null) {
      chnl.setPubDate(ParserUtils.getDate(pubDate.getTextTrim()));
    }

    // 0..1 lastBuildDate element
    Element lastBuildDate = channel.getChild("lastBuildDate");
    if (lastBuildDate == null) {
      lastBuildDate = channel.getChild("modified", dctermsNS);
    }
    if (lastBuildDate == null) {
      lastBuildDate = channel.getChild("lastBuildDate", rss091NS);
    }
    if (lastBuildDate != null) {
      chnl.setLastBuildDate(ParserUtils.getDate(lastBuildDate.getTextTrim()));
    }

    // RSS 1.0 Administration Module support

    // 0..1 generator element
    Element elGenerator = channel.getChild("generatorAgent", adminNS);
    if (elGenerator != null) {
      Attribute generator = elGenerator.getAttribute("resource", ParserUtils
          .getNamespace(elGenerator, "rdf"));
      if (generator != null) {
        chnl.setGenerator(generator.getValue());
      }
    }

    // RSS 1.0 Syndication Module support

    // 0..1 update period element
    Element updatePeriod = channel.getChild("updatePeriod", syNS);
    if (updatePeriod != null) {
      try {
        ChannelUpdatePeriod channelUpdatePeriod = ChannelUpdatePeriod
            .valueFromText(updatePeriod.getTextTrim());
        chnl.setUpdatePeriod(channelUpdatePeriod);
      } catch (IllegalArgumentException ex) {
        logger.warn(updatePeriod.getTextTrim(), ex);
      }
    }

    // 0..1 update frequency element
    Element updateFrequency = channel.getChild("updateFrequency", syNS);
    if (updateFrequency != null) {
      chnl.setUpdateFrequency((new Integer(updateFrequency.getTextTrim()))
          .intValue());
    }

    // 0..1 update base element
    Element updateBase = channel.getChild("updateBase", syNS);
    if (updateBase != null) {
      chnl.setUpdateBase(ParserUtils.getDate(updateBase.getTextTrim()));
    }

    if ((updatePeriod != null) && updateFrequency != null) {
      int ttl = getTTL(chnl.getUpdatePeriod(), chnl.getUpdateFrequency());
      chnl.setTtl(ttl);
    }

    // item elements
    List items = root.getChildren("item", defNS);
    Iterator i = items.iterator();
    while (i.hasNext()) {
      Element item = (Element) i.next();

      ParserUtils.matchCaseOfChildren(item, new String[] { "title", "link",
          "encoded", "description", "creator", "subject", "date", "sourceURL",
          "source", "timestamp", "reference" });

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
      if (elDesc == null) {
        elDesc = item.getChild("description", dcNS);
      }
      String strDesc = "";
      if (elDesc != null) {
        strDesc = elDesc.getTextTrim();
      }

      // generate new RSS item (link to article)
      ItemIF rssItem = cBuilder.createItem(item, chnl, strTitle, strDesc,
          ParserUtils.getURL(strLink));
      rssItem.setFound(dateParsed);

      // get creator element
      Element elCreator = item.getChild("creator", dcNS);
      if (elCreator != null) {
        rssItem.setCreator(elCreator.getTextTrim());
      }

      // get subject element
      Element elSubject = item.getChild("subject", dcNS);
      if (elSubject != null) {
        // TODO: Mulitple subject elements not handled currently
        rssItem.setSubject(elSubject.getTextTrim());
      }

      // get date element
      Element elDate = item.getChild("date", dcNS);
      if (elDate != null) {
        rssItem.setDate(ParserUtils.getDate(elDate.getTextTrim()));
      }

      // get source element - default to Aggregation module, then try Dublin Core
      String sourceName = null;
      String sourceLocation = null;
      Date sourceTimestamp = null;

      Element elSourceURL = item.getChild("sourceURL", agNS);
      if (elSourceURL == null) { //  No Aggregation module - try Dublin Core
        elSourceURL = item.getChild("source", dcNS);
        if (elSourceURL != null) {
          sourceLocation = elSourceURL.getTextTrim();
          sourceName = "Source";
        }
      } else { // Aggregation module
        sourceLocation = elSourceURL.getTextTrim();
        Element elSourceName = item.getChild("source", agNS);
        if (elSourceName != null) {
          sourceName = elSourceName.getTextTrim();
        }
        Element elSourceTimestamp = item.getChild("timestamp", agNS);
        if (elSourceTimestamp != null) {
          sourceTimestamp = ParserUtils
              .getDate(elSourceTimestamp.getTextTrim());
        }
      }

      if (sourceLocation != null) {
        ItemSourceIF itemSource = cBuilder.createItemSource(rssItem,
            sourceName, sourceLocation, sourceTimestamp);
        rssItem.setSource(itemSource);
      }

      // comments element - use Annotation module
      Element elReference = item.getChild("reference", annotateNS);
      if (elReference != null) {
        Attribute resource = elReference.getAttribute("resource", ParserUtils
            .getNamespace(elReference, "rdf"));
        if (resource != null) {
          URL resourceURL = ParserUtils.getURL(resource.getValue());
          if (resourceURL != null) {
            rssItem.setComments(resourceURL);
          }
        }
      }

    }

    // image element
    Element image = root.getChild("image", defNS);
    if (image != null) {

      ParserUtils.matchCaseOfChildren(image, new String[] { "title", "url",
          "link", "width", "height", "description" });

      ImageIF rssImage = cBuilder.createImage(image.getChildTextTrim("title",
          defNS), ParserUtils.getURL(image.getChildTextTrim("url", defNS)),
          ParserUtils.getURL(image.getChildTextTrim("link", defNS)));
      Element imgWidth = image.getChild("width", defNS);
      if (imgWidth != null) {
        try {
          rssImage.setWidth(Integer.parseInt(imgWidth.getTextTrim()));
        } catch (NumberFormatException e) {
          logger.warn(e);
        }
      }
      Element imgHeight = image.getChild("height", defNS);
      if (imgHeight != null) {
        try {
          rssImage.setHeight(Integer.parseInt(imgHeight.getTextTrim()));
        } catch (NumberFormatException e) {
          logger.warn(e);
        }
      }
      Element imgDescr = image.getChild("description", defNS);
      if (imgDescr != null) {
        rssImage.setDescription(imgDescr.getTextTrim());
      }
      chnl.setImage(rssImage);
    }

    // textinput element
    Element txtinp = root.getChild("textinput", defNS);
    if (txtinp != null) {

      ParserUtils.matchCaseOfChildren(image, new String[] { "title",
          "description", "name", "link" });

      String tiTitle = null;
      if (txtinp.getChild("title", defNS) != null) {
        tiTitle = txtinp.getChild("title", defNS).getTextTrim();
      }
      String tiDescr = null;
      if (txtinp.getChild("description", defNS) != null) {
        tiDescr = txtinp.getChild("description", defNS).getTextTrim();
      }
      String tiName = null;
      if (txtinp.getChild("name", defNS) != null) {
        tiName = txtinp.getChild("name", defNS).getTextTrim();
      }
      URL tiLink = null;
      if (txtinp.getChild("link", defNS) != null) {
        tiLink = ParserUtils.getURL(txtinp.getChild("link", defNS)
            .getTextTrim());
      }
      TextInputIF rssTextInput = cBuilder.createTextInput(tiTitle, tiDescr,
          tiName, tiLink);
      chnl.setTextInput(rssTextInput);
    }

    chnl.setLastUpdated(dateParsed);

    return chnl;
  }

  /**
   * Returns the TTL value corresponding to updatePeriod and updateFrequency.
   * @param updatePeriod Channel update period.
   * @param updateFrequency the update frequency.
   * @return the TTL value.
   */
  private int getTTL(final ChannelUpdatePeriod updatePeriod, int updateFrequency) {

    int minutes;
    if (updatePeriod != null) {
      minutes = updatePeriod.getMinutesInPeriod();
    } else {
      minutes = 24 * 60;
    }

    return updateFrequency == 0 ? minutes : minutes / updateFrequency;
  }
}
