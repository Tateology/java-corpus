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


// $Id: TestFeedParser.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.utils.InformaTestCase;

/**
 * Test class which reads in a textfile (containing one URL per line)
 * and trying to parse each one as an own test case. Expects at least
 * one news item in each individual assertion.
 *
 * @author Niko Schmuck
 */
public class TestFeedParser extends InformaTestCase {

  private static Log logger = LogFactory.getLog(InformaTestCase.class);

  private String testURL;
  private ChannelBuilderIF builder;
  
  public TestFeedParser(String testMethodName, String testURL) {
    super("TestFeedParser", testMethodName);
    this.testURL = testURL;
    this.builder = new de.nava.informa.impl.basic.ChannelBuilder();
  }

  public void testParseNewsFeedValidChannel() throws Exception {
    logger.info("Reading in feed from " + testURL);
    ChannelIF channel = FeedParser.parse(builder, new URL(testURL));
    assertNotNull("Failed parsing channel " + testURL, channel.getItems());
    assertTrue("Expected at least one item at channel " + testURL,
               channel.getItems().size() > 0  );
  }

  public void testSetSaxDriverClassName() {
    // Set invalid non-existing class
    try {
      FeedParser.setSaxDriverClassName("test.this.Case");
      fail("ClassNotFoundException should be thrown.");
    } catch (ClassNotFoundException e) {
      // Expected behavior
    }

    // Set existing, but invalid class
    try {
      FeedParser.setSaxDriverClassName(TestFeedParser.class.getName());
      fail("ClassCastException should be thrown.");
    } catch (ClassNotFoundException e) {
      fail("ClassCastException should be thrown.");
    } catch (ClassCastException e)
    {
      // Expected behavior
    }

    // Set valid driver class
    //try {
      // TODO: FeedParser.setSaxDriverClassName(XMLReaderImpl.class.getName());
    //} catch (ClassNotFoundException e) {
    //  fail("Valid driver provided. Should be no exceptions.");
    //}
  }

  public void testNonExistingDTD() throws Exception {
    String sampleFeed = "<!DOCTYPE rss PUBLIC \"-//Netscape Communications//DTD RSS 0.91//EN\"\n" +
      " \"http://MISSING.netscape.com/publish/formats/rss-0.91.dtd\">\n" +
      "<rss version=\"0.91\">\n" +
      "<channel>\n" +
      "<title>Test</title>\n" +
      "</channel>\n" +
      "</rss>";

    try {
      FeedParser.parse(builder, new StringReader(sampleFeed));
    } catch (ParseException e) {
      e.printStackTrace();
      fail("Failed to parse feed with non-existing DND.");
    }
  }

  public static Test suite() throws Exception {
    TestSuite suite = new TestSuite();
    // Read in file and construct test suite
    String line; 
    BufferedReader rdr = new BufferedReader(new FileReader(getDataDir() + FS + "newsfeeds.txt"));    
    while ((line = rdr.readLine()) != null) {
      if (line.startsWith("#")) { // Ignore line
        continue;
      }
      suite.addTest(new TestFeedParser("testParseNewsFeedValidChannel", line));
    }

    suite.addTest(new TestFeedParser("testSetSaxDriverClassName", null));
    suite.addTest(new TestFeedParser("testNonExistingDTD", null));

    return suite;
  }
}
