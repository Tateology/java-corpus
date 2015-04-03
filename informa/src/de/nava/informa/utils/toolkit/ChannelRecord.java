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
// $Id: ChannelRecord.java 705 2004-09-16 14:06:00Z spyromus $
//

package de.nava.informa.utils.toolkit;

import de.nava.informa.core.ChannelIF;

/**
 * Internal channel record. Used by <code>Scheduler</code> and <code>WorkersManager</code>
 * to hold information about channels.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class ChannelRecord {
  /**
   * Low priority.
   */
  public static final int PRIO_LOW = -1;

  /**
   * Normal priority.
   */
  public static final int PRIO_NORMAL = 0;

  /**
   * High priority.
   */
  public static final int PRIO_HIGH = 1;

  private ChannelIF channel;

  private boolean formatResolved = false;

  private int priority;
  private long period;
  private boolean canceled;

  /**
   * Creates channel record.
   *
   * @param channel  channel.
   * @param period   period of poll in milliseconds.
   * @param priority priority of poll.
   */
  public ChannelRecord(ChannelIF channel, long period, int priority) {
    this.priority = priority;
    this.period = period;
    this.channel = channel;
    formatResolved = (channel.getFormat() != null);
    setCanceled(false);
  }

  /**
   * Returns priority of poll.
   *
   * @return priority of poll.
   * @see #PRIO_LOW
   * @see #PRIO_NORMAL
   * @see #PRIO_HIGH
   */
  public final int getPriority() {
    return priority;
  }

  /**
   * Sets priority of poll.
   *
   * @param priority priority of poll.
   * @see #PRIO_LOW
   * @see #PRIO_NORMAL
   * @see #PRIO_HIGH
   */
  public final void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns current period of updates.
   *
   * @return period in milliseconds.
   */
  public final long getPeriod() {
    return period;
  }

  /**
   * Sets new period of updates (just value). Actuall period will not be affected
   * as we only store the setting here.
   *
   * @param period new period setting in milliseconds.
   */
  public final void setPeriod(long period) {
    this.period = period;
  }

  /**
   * Returns channel object.
   *
   * @return channel object.
   */
  public final ChannelIF getChannel() {
    return channel;
  }

  /**
   * Returns TRUE if format of channel is resolved.
   *
   * @return TRUE if format is resolved.
   */
  public final boolean isFormatResolved() {
    return formatResolved;
  }

  /**
   * Sets the state of format resolution flag.
   *
   * @param formatResolved TRUE if channel format is already resolved.
   */
  public final void setFormatResolved(boolean formatResolved) {
    this.formatResolved = formatResolved;
  }

  /**
   * Sets cancel-status of record.
   *
   * @param aCanceled <code>true</code> to cancel processing.
   */
  public void setCanceled(boolean aCanceled) {
    this.canceled = aCanceled;
  }

  /**
   * Returns cancel-status of record.
   *
   * @return <code>true</code> if processing of this record has been canceled.
   */
  public boolean isCanceled() {
    return canceled;
  }
}
