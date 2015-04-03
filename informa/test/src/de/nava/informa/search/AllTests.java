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


// $Id: AllTests.java 297 2003-09-03 23:05:32Z niko_schmuck $

package de.nava.informa.search;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

  public AllTests(String testname) {
    super(testname);
  }

  public static Test suite() { 
    TestSuite suite = new TestSuite(); 
    suite.addTest(new TestSuite(TestChannelIndexer.class)); 
    suite.addTest(new TestSuite(TestChannelSearcher.class)); 
    return suite; 
  }
  
}
