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
// $Id: InputStreamProviderIF.java 762 2005-05-26 10:13:23Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface of <code>InputStream</code> provider which is intended to
 * return input stream for a given channel.
 */
public interface InputStreamProviderIF {

  /**
   * Return <code>InputStream</code> to be used for reading given channel.
   *
   * @param channel   channel we are going to read.
   * @param activity  activity name (like, "Fetching" or "Detecting format").
   *
   * @return initialized input stream ready for reading.
   *
   * @throws java.io.IOException in case of any problems.
   */
  InputStream getInputStreamFor(ChannelIF channel, String activity) throws IOException;
}
