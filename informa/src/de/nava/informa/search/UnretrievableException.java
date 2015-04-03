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


// $Id: UnretrievableException.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.search;

/**
 * An exception thrown when a formerly existing channel or news item
 * now not any longer can be retrieved from the channel
 * collection. This can be caused mainly because of two reasons: the
 * full-text index could be out-of-date OR the give channel collection
 * is not identical to the indexed one.
 *
 * @author Niko Schmuck (niko@nava.de) 
 */
public class UnretrievableException extends RuntimeException {

	private static final long serialVersionUID = 1292158163384671848L;

	public UnretrievableException() {
    super();
  }

  public UnretrievableException(String message) {
    super("Unable to retrieve " + message);
  }

  public UnretrievableException(Throwable cause) {
    // Java 1.3 does not support this constructor
    super("UnretrievableException: " + cause.getMessage());
  }

}
