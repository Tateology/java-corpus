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


// $Id: OPML_1_1_Parser.java 816 2006-12-04 23:40:49Z italobb $

package de.nava.informa.parsers;

import de.nava.informa.core.FeedIF;
import de.nava.informa.impl.basic.Feed;
import de.nava.informa.utils.ParserUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Parser which reads in document according to the OPML 1.1 specification 
 * and generates a collection of <code>FeedIF</code> instances.
 *
 * @author Niko Schmuck
 */
class OPML_1_1_Parser {

  private static Log logger = LogFactory.getLog(OPML_1_1_Parser.class);

  static Collection<FeedIF> parse(Element root) {

    Collection<FeedIF> feedColl = new ArrayList<FeedIF>();
    
    Date dateParsed = new Date();
    logger.debug("start parsing.");

    // Lower the case of these tags to simulate case-insensitive parsing
    ParserUtils.matchCaseOfChildren(root, "body");

    // Get the head element (only one should occur)
//    Element headElem = root.getChild("head");
//    String title = headElem.getChildTextTrim("title");
    
    // Get the body element (only one occurs)
    Element bodyElem = root.getChild("body");

    // 1..n outline elements
    ParserUtils.matchCaseOfChildren(bodyElem, "outline");
    List feeds = bodyElem.getChildren("outline");
    Iterator i = feeds.iterator();
    while (i.hasNext()) {
      Element feedElem = (Element) i.next();
      // get title attribute
      Attribute attrTitle = feedElem.getAttribute("title"); 
      String strTitle = "[No Title]";
      if (attrTitle != null) {
        strTitle = attrTitle.getValue();
      }
      FeedIF feed = new Feed(strTitle);
      if (logger.isDebugEnabled()) {
        logger.debug("Feed element found (" + strTitle + ").");
      }
      // get text attribute
      Attribute attrText = feedElem.getAttribute("text"); 
      String strText = "[No Text]";
      if (attrText != null) {
        strText = attrText.getValue();
      }
      feed.setText(strText);
      // get attribute type (for example: 'rss')
      Attribute attrType = feedElem.getAttribute("type"); 
      String strType = "text/xml";
      if (attrType != null) {
        strType = attrType.getValue();
      }
      feed.setContentType(strType);
      
      // TODO: handle attribute version (for example: 'RSS')
      
      // get attribute xmlUrl
      Attribute attrXmlUrl = feedElem.getAttribute("xmlUrl"); 
      if (attrXmlUrl != null && attrXmlUrl.getValue() != null) {
        feed.setLocation(ParserUtils.getURL(attrXmlUrl.getValue()));
      }      
      // get attribute htmllUrl
      Attribute attrHtmlUrl = feedElem.getAttribute("htmlUrl"); 
      if (attrHtmlUrl != null && attrHtmlUrl.getValue() != null) {
        feed.setSite(ParserUtils.getURL(attrHtmlUrl.getValue()));
      }
      // set current date
      feed.setDateFound(dateParsed);
      // add feed to collection
      feedColl.add(feed);
    }

    return feedColl;
  }
  
}
