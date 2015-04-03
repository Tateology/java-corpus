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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//

// $Id: ParserUtils.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Utility class providing convenience methods to (XML) parsing mechanisms.
 * 
 * @author Niko Schmuck (niko@nava.de)
 */
public final class ParserUtils {

  private static Log logger = LogFactory.getLog(ParserUtils.class);

  private ParserUtils() {
  }

  public static URL getURL(String toURL) {
    URL result = null;
    try {
      if ((toURL != null) && (toURL.trim().length() > 0))
          result = new URL(toURL);
    } catch (java.net.MalformedURLException e) {
      logger.warn("Invalid URL " + toURL + " given.");
    }
    return result;
  }

  public static Namespace getDefaultNS(Element element) {
    return getNamespace(element, "");
  }

  public static Namespace getNamespace(Element element, String prefix) {
    //    Namespace ns = null;
    //    Iterator it = element.getAdditionalNamespaces().iterator();
    //    while (it.hasNext()) {
    //      Namespace curNS = (Namespace) it.next();
    //      if (curNS.getPrefix().equals(prefix)) {
    //        ns = curNS;
    //        break;
    //      }
    //    }
    Namespace ns = (prefix == null) ? element.getNamespace("") : element
        .getNamespace(prefix);
    return ns;
  }

  private static SimpleDateFormat[] dateFormats = null;

  static {
    final String[] possibleDateFormats = {
      "EEE, dd MMM yyyy HH:mm:ss z", //RFC_822
      "EEE, dd MMM yyyy HH:mm zzzz",
      "yyyy-MM-dd'T'HH:mm:ssZ",
      "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz", //Blogger Atom feed has millisecs also
      "yyyy-MM-dd'T'HH:mm:sszzzz",
      "yyyy-MM-dd'T'HH:mm:ss z",
      "yyyy-MM-dd'T'HH:mm:ssz", //ISO_8601
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd'T'HHmmss.SSSz",
      "yyyy-MM-dd'T'HH:mm:ss",
      "yyyy-MM-dd"};

    dateFormats = new SimpleDateFormat[possibleDateFormats.length];
    TimeZone gmtTZ = TimeZone.getTimeZone("GMT");
    for (int i = 0; i < possibleDateFormats.length; i++) {
      dateFormats[i] = new SimpleDateFormat(possibleDateFormats[i],
          Locale.ENGLISH);
      dateFormats[i].setTimeZone(gmtTZ);
    }

  }

  // Mon, 07 Oct 2002 03:16:15 GMT
  private static SimpleDateFormat dfA = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

  // 2002-09-19T02:51:16+0200
  private static SimpleDateFormat dfB = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ssZ");

  // 2002-09-19T02:51:16
  private static SimpleDateFormat dfC = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");

  // 2002-09-19
  private static SimpleDateFormat dfD = new SimpleDateFormat("yyyy-MM-dd");

  public static Date getDate(String strdate) {
    Date result = null;
    strdate = strdate.trim();
    if (strdate.length() > 10) {

      // TODO deal with +4:00 (no zero before hour)
      if ((strdate.substring(strdate.length() - 5).indexOf("+") == 0 || strdate
          .substring(strdate.length() - 5).indexOf("-") == 0)
          && strdate.substring(strdate.length() - 5).indexOf(":") == 2) {

        String sign = strdate.substring(strdate.length() - 5,
            strdate.length() - 4);

        strdate = strdate.substring(0, strdate.length() - 5) + sign + "0"
            + strdate.substring(strdate.length() - 4);
        // logger.debug("CASE1 : new date " + strdate + " ? "
        //    + strdate.substring(0, strdate.length() - 5));

      }

      String dateEnd = strdate.substring(strdate.length() - 6);

      // try to deal with -05:00 or +02:00 at end of date
      // replace with -0500 or +0200
      if ((dateEnd.indexOf("-") == 0 || dateEnd.indexOf("+") == 0)
          && dateEnd.indexOf(":") == 3) {
        // TODO deal with GMT-00:03
        if ("GMT".equals(strdate.substring(strdate.length() - 9, strdate
            .length() - 6))) {
          logger.debug("General time zone with offset, no change ");
        } else {
          // continue treatment
          String oldDate = strdate;
          String newEnd = dateEnd.substring(0, 3) + dateEnd.substring(4);
          strdate = oldDate.substring(0, oldDate.length() - 6) + newEnd;
          // logger.debug("!!modifying string ->"+strdate);
        }
      }
    }
    int i = 0;
    while (i < dateFormats.length) {
      try {
        result = dateFormats[i].parse(strdate);
        // logger.debug("******Parsing Success "+strdate+"->"+result+" with
        // "+dateFormats[i].toPattern());
        break;
      } catch (java.text.ParseException eA) {
        logger.debug("parsing " + strdate + " ["
            + dateFormats[i].toPattern() + "] without success, trying again.");
        i++;
      }
    }

    return result;
  }

  /**
   * Tries different date formats to parse against the given string
   * representation to retrieve a valid Date object.
   */
  public static Date getDateOLD(String strdate) {
    Date result = null;

    try {
      result = dfA.parse(strdate);
    } catch (java.text.ParseException eA) {
      logger.warn("Error parsing date (A): " + eA.getMessage());
      try {
        result = dfB.parse(strdate);
      } catch (java.text.ParseException eB) {
        logger.warn("Error parsing date (B): " + eB.getMessage());
        try {
          result = dfC.parse(strdate);
          // try to retrieve the timezone anyway
          result = extractTimeZone(strdate, result);
        } catch (java.text.ParseException eC) {
          logger.warn("Error parsing date (C): " + eC.getMessage());
          try {
            result = dfD.parse(strdate);
          } catch (java.text.ParseException eD) {
            logger.warn("Error parsing date (D): " + eD.getMessage());
            eD.printStackTrace();
          }
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Parsing date '" + strdate + "' resulted in: " + result);
    }
    if (result == null) {
      logger.warn("No appropiate date could be extracted from " + strdate);

    }
    return result;
  }

  private static Date extractTimeZone(String strdate, Date thedate) {
    // try to extract -06:00
    String tzSign = strdate.substring(strdate.length() - 6,
        strdate.length() - 5);
    String tzHour = strdate.substring(strdate.length() - 5,
        strdate.length() - 3);
    String tzMin = strdate.substring(strdate.length() - 2);
    if (tzSign.equals("-") || tzSign.equals("+")) {
      int h = Integer.parseInt(tzHour);
      int m = Integer.parseInt(tzMin);
      // NOTE: this is really plus, since perspective is from GMT
      if (tzSign.equals("+")) {
        h = -1 * h;
        m = -1 * m;
      }
      Calendar cal = Calendar.getInstance();
      cal.setTime(thedate);
      cal.add(Calendar.HOUR_OF_DAY, h);
      cal.add(Calendar.MINUTE, m);
      // calculate according the used timezone
      cal.add(Calendar.MILLISECOND, localTimeDiff(cal.getTimeZone(), thedate));
      thedate = cal.getTime();
    }
    return thedate;
  }

  private static int localTimeDiff(TimeZone tz, Date date) {
    if (tz.inDaylightTime(date)) {
      int dstSavings = 0;
      if (tz.useDaylightTime()) {
        dstSavings = 3600000; // shortcut, JDK 1.4 allows cleaner impl
      }
      return tz.getRawOffset() + dstSavings;
    }
    return tz.getRawOffset();
  }

  public static String formatDate(Date aDate) {
    return dfA.format(aDate);
  }

  public static String decodeBase64(String s) {
    //use private class
    return Base64Decoder.decode(s);
  }

  /**
   * Unescapes the string by replacing some XML entities into plain symbols.
   *
   * @param value value to unescape.
   *
   * @return unescaped content.
   */
  public static String unEscape(String value) {
    value = value.replaceAll("&lt;", "<");
    value = value.replaceAll("&gt;", ">");
    value = value.replaceAll("&amp;", "&");
    value = value.replaceAll("&quot;", "\"");
    value = value.replaceAll("&apos;", "'");
    return value;
  }

  /**
   * Escapes the string by replacing reserved symbols with their XML entities.
   *
   * @param value value to escape.
   *
   * @return escaped result.
   */
  public static String escape(String value) {
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    value = value.replaceAll("&", "&amp;");
    value = value.replaceAll("\"", "&quot;");
    value = value.replaceAll("'", "&apos;");
    return value;
  }

  /**
   * Converts names of child-tags mentioned in <code>childrenNames</code> list
   * to that given case.
   *
   * @param root          root element.
   * @param childrenNames names of child tags to convert.
   */
  public static void matchCaseOfChildren(Element root, String[] childrenNames) {
    if (root == null || childrenNames.length == 0) return;

    // Prepare list of names
    int namesCount = childrenNames.length;
    Map<String, String> names = new HashMap<String, String>(namesCount);
    for (int i = 0; i < namesCount; i++) {
      String childName = childrenNames[i];
      if (childName != null) {
        String lower = childName.toLowerCase();
        if (!names.containsKey(lower)) names.put(lower, childName);
      }
    }

    // Walk through the children elements
    List elements = root.getChildren();
    for (int i = 0; i < elements.size(); i++) {
      Element child = (Element) elements.get(i);
      String childName = child.getName().toLowerCase();
      if (names.containsKey(childName)) child.setName(names.get(childName));
    }
  }

  /**
   * Converts names of child-tags mentioned in <code>childName</code> list
   * to that given case.
   *
   * @param root          root element.
   * @param childName     name of child tags to convert.
   */
  public static void matchCaseOfChildren(Element root, String childName) {
    if (root == null || childName == null) return;

    // Walk through the children elements
    List elements = root.getChildren();
    for (int i = 0; i < elements.size(); i++) {
      Element child = (Element) elements.get(i);
      String name = child.getName().toLowerCase();
      if (name.equalsIgnoreCase(childName)) child.setName(childName);
    }
  }
}
