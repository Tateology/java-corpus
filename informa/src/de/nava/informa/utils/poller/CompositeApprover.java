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
// $Id: CompositeApprover.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

import java.util.List;
import java.util.Vector;

/**
 * Composite approver uses all of its sub-approvers to form decision.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
class CompositeApprover implements PollerApproverIF {
  private List<PollerApproverIF> approvers = new Vector<PollerApproverIF>();

  /**
   * Decides whether it's possible to add item to the channel or no.
   *
   * @param item    item to add.
   * @param channel destination channel.
   * @return TRUE if addition is allowed.
   */
  public final boolean canAddItem(ItemIF item, ChannelIF channel) {
    boolean result = true;
    int i = 0;
    final int size = approvers.size();
    while (i < size && result) {
      final PollerApproverIF approver = (PollerApproverIF) approvers.get(i);
      result = approver.canAddItem(item, channel);
      i++;
    }

    return result;
  }

  /**
   * Adds new approver to the list.
   *
   * @param approver new approver.
   */
  public final void add(PollerApproverIF approver) {
    if (!approvers.contains(approver)) {
      approvers.add(approver);
    }
  }

  /**
   * Removes approver from the list.
   *
   * @param approver registered approver.
   */
  public final void remove(PollerApproverIF approver) {
    approvers.remove(approver);
  }
}
