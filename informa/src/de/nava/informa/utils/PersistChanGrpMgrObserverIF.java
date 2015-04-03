// BlogBridge -- RSS Client Based on Informa
// Copyright (c) 2002 by Pito Salas
//
// Pito Salas
// http://sourceforge.net/projects/informa
// mailto:pito_salas@users.sourceforge.net
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
// Lesser General Public License for more details..
//
// $Id: PersistChanGrpMgrObserverIF.java 514 2004-05-19 16:57:30Z pitosalas $
package de.nava.informa.utils;
import de.nava.informa.core.ChannelObserverIF;

/**
 * Additional events that can be observed for PersistChanGrpMgr
 */
public interface PersistChanGrpMgrObserverIF extends ChannelObserverIF
{
    /**
     * Called to indicate either the start or end of one polling action
     * 
     * @param name Name of the PersistChanGrpMgr
     * @param pollingCount how many times this group has polled its feeds so far
     * @param startstop true for start, false for stop
     */
    void pollingNow(String name, int pollingCount, boolean startstop);
}
