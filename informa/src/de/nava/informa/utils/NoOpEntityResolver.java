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


// $Id: NoOpEntityResolver.java 178 2003-04-06 17:30:37Z niko_schmuck $

package de.nava.informa.utils;

import java.io.StringReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An EntityResolver that resolves the DTD without actually reading
 * the separate file.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class NoOpEntityResolver implements EntityResolver {

  private static Log logger = LogFactory.getLog(NoOpEntityResolver.class);
  
  public InputSource resolveEntity(String publicId, String systemId) {
    if (logger.isDebugEnabled()) {
      logger.debug("publicId: " + publicId +
                   ", systemId: " + systemId);
    }
    return new InputSource(new StringReader(""));
  }

}
