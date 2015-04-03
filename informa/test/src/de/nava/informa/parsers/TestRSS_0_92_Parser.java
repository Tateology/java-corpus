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


// $Id: TestRSS_0_92_Parser.java 504 2004-05-13 22:55:36Z niko_schmuck $

package de.nava.informa.parsers;

import java.io.File;
import java.io.IOException;

import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.utils.InformaTestCase;

public class TestRSS_0_92_Parser extends InformaTestCase {

  public TestRSS_0_92_Parser(String name)
    throws IOException, ParseException {

    super("TestRSS_0_92_Parser", name);
    this.method_name = name;
  }

  public void testParseW3CSynd() throws Exception {
    File inpFile = new File(getDataDir(), "juancolecom.rss");
    ChannelIF channel_juan = FeedParser.parse(new ChannelBuilder(), inpFile);
    assertEquals("Juan Cole   *  Informed Comment  *", channel_juan.getTitle());
    assertEquals(25, channel_juan.getItems().size());
    // TODO: For the time being RSS 0.92 is handled by the RSS 0.91 parser
    assertEquals(ChannelFormat.RSS_0_91, channel_juan.getFormat());
  }

}
