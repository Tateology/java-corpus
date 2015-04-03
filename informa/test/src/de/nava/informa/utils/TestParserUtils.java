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


// $Id: TestParserUtils.java 758 2005-03-10 11:40:24Z spyromus $

package de.nava.informa.utils;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.List;
import java.io.StringReader;

public class TestParserUtils extends InformaTestCase {
  
  public TestParserUtils(String name) {
    super("TestParserUtils", name);
  }
  
  public void testParseDateFmt() {
    String strdate;
    Date resDate;
    Calendar expDate;

    strdate = "Wed, 07 Aug 2002 00:32:05 GMT";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 0, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "Tue, 21 Dec 2004 23:02 +0100";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2004, Calendar.DECEMBER, 21, 22, 02, 00);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);

    strdate = "2002-08-07T15:32:05-0500 ";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 20, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);

    strdate = "2002-08-07T15:32:05-05:00 ";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 20, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
 
    strdate = "2002-08-07T12:32:05+03:00 ";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 9, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "2002-08-07T12:32:05 GMT";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 12, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "2002-08-07T12:32:05 RET";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 8, 32, 5); // Reunion Time = GMT+4
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "2002-08-07T12:32:05Z";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 12, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "2002-08-07T12:32:05+3:00 ";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 9, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
    
    strdate = "2002-08-07T06:32:05-3:00 ";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 9, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
  
    strdate = "2002-08-07T12:32:05GMT-02:00";
    resDate = ParserUtils.getDate(strdate);
    expDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    expDate.set(2002, Calendar.AUGUST, 7, 14, 32, 5);
    expDate.set(Calendar.MILLISECOND, 0);
    assertEquals(expDate.getTime(), resDate);
   }

  public void testUnescape() {
    String testString = "&amp;gt; &gt; &quot; &lt; &apos;";
    //System.err.println(" decoded version = "+ParserUtils.unEscape(testString));
    assertEquals("&gt; > \" < '", ParserUtils.unEscape(testString) );
  }

  /**
   * Tests how the case of child-tags is converted.
   *
   * @throws Exception in case of any error.
   */
  public void testMatchCaseOfChildren() throws Exception
  {
    String test1 = "<a>" +
      "<Bb><C></C></Bb>" +
      "<D></D>" +
      "<E></E>" +
      "<BB></BB>" +
      "<bb></bb>" +
      "</a>";

    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(new StringReader(test1));
    Element root = doc.getRootElement();
    List elements;

    elements = root.getChildren();
    assertEquals(5, elements.size());
    assertEquals("Bb", ((Element)elements.get(0)).getName());
    assertEquals("D", ((Element)elements.get(1)).getName());
    assertEquals("E", ((Element)elements.get(2)).getName());
    assertEquals("BB", ((Element)elements.get(3)).getName());
    assertEquals("bb", ((Element)elements.get(4)).getName());

    ParserUtils.matchCaseOfChildren(root, new String[] { "bB", "e" });

    elements = root.getChildren();
    assertEquals(5, elements.size());
    assertEquals("bB", ((Element)elements.get(0)).getName());
    assertEquals("D", ((Element)elements.get(1)).getName());
    assertEquals("e", ((Element)elements.get(2)).getName());
    assertEquals("bB", ((Element)elements.get(3)).getName());
    assertEquals("bB", ((Element)elements.get(4)).getName());
  }
}
