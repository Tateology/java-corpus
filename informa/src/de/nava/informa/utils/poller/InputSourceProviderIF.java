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
// $Id: InputSourceProviderIF.java 757 2005-03-04 18:28:35Z spyromus $
//

package de.nava.informa.utils.poller;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.IOException;

/**
 * Interface of <code>InputSource</code> provider which is intended to
 * return input source created from the given <code>InputStream</code>.
 * It can be used to customize the string processing by wrapping the
 * stream with custom <code>Reader</code>'s or another
 * <code>FilterInputStream</code>'s.
 *
 * The source stream represents feed's stream of chars and
 * <code>InputSource</code> object will be used for parsing this feed.
 */
public interface InputSourceProviderIF {

  /**
   * Return <code>InputSource</code> to be used with given stream.
   *
   * @param stream  input stream.
   *
   * @return initialized input source ready for use in parsing.
   *
   * @throws IOException in case of any problems.
   */
  InputSource getInputSourceFor(InputStream stream) throws IOException;
}
