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


// $Id: IdGenerator.java 215 2003-06-27 07:55:02Z niko_schmuck $

package de.nava.informa.impl.basic;

import java.util.Random;

/**
 * Identity generator implemented as singleton for generating positive
 * random integers to be used as identify uniquely news channels,
 * items.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
import de.nava.informa.core.IdGeneratorIF;

public class IdGenerator implements IdGeneratorIF {

  private static IdGenerator instance;
  /** used for creating unique item IDs. */
  private static transient Random rand;
  
  private IdGenerator() {
    rand = new Random(System.currentTimeMillis());
  }

  public static IdGenerator getInstance() {
    if (instance == null) {
      instance = new IdGenerator();
    }
    return instance;
  }

  public long getId() {
    return 100000l + Math.abs(rand.nextInt());
  }
  
}
