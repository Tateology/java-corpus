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
// $Id: IdGenerator.java 658 2004-08-23 14:39:08Z spyromus $
//

package de.nava.informa.utils.manager.memory;

/**
 * Generator of ID sequence. Each application run it starts from 1. We don't need to
 * have unique identifiers across invocations and the implemented approach looks
 * sufficient.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public final class IdGenerator {
  private static long currentId = 1;

  /**
   * Hidden utility class constructor.
   */
  private IdGenerator() {
  }

  /**
   * Returns next available ID value.
   *
   * @return available ID.
   */
  public static long getNextId() {
    return currentId++;
  }
}
