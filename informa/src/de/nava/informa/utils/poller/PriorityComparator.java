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
// $Id: PriorityComparator.java 659 2004-08-23 14:39:26Z spyromus $
//

package de.nava.informa.utils.poller;

import de.nava.informa.utils.toolkit.ChannelRecord;

import java.util.Comparator;

/**
 * Comparator for <code>ChannelRecord</code> class. Uses priority setting to decide which
 * record goes first.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PriorityComparator implements Comparator {
  /**
   * Compares its two arguments for order.  Returns a negative integer,
   * zero, or a positive integer as the first argument is less than, equal
   * to, or greater than the second.<p>
   *
   * @param o1 the first object to be compared.
   * @param o2 the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the
   *         first argument is less than, equal to, or greater than the
   *         second.
   * @throws ClassCastException if the arguments' types prevent them from
   *                            being compared by this Comparator.
   */
  public final int compare(Object o1, Object o2) {
    final ChannelRecord r1 = (ChannelRecord) o1;
    final ChannelRecord r2 = (ChannelRecord) o2;

    final int p1 = r1.getPriority();
    final int p2 = r2.getPriority();

    int result = 0;

    if (p1 < p2) {
      result = -1;
    } else if (p2 > p1) {
      result = 1;
    }

    return result;
  }
}
