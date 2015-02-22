/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
 *
 * <p> This file is part of jFin. </p>
 *
 * <p> jFin is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. </p>
 *
 * <p> jFin is distributed in the hope that it will be useful, but <b>WITHOUT
 * ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b>
 * or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU General Public
 * License for more details. </p>
 *
 * <p> You should have received a copy of the GNU General Public License along
 * with jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA. </p>
 */

package org.jfin.date;

/**
 * <p>
 * Enumeration of the various business day conventions:
 * </p>
 *
 * <ul>
 * <li> Undajusted
 * <li> Preceding
 * <li> Modified Preceding
 * <li> Following
 * <li> Modified Following
 * <li> Month End Reference
 * </ul>
 */
public enum BusinessDayConvention
{
	/**
	 * Do not adjust.
	 */
	UNADJUSTED,
	/**
	 * Choose the first business day before the given holiday.
	 */
	PRECEDING,
	/**
	 * Choose the first business day before the given holiday unless it belongs
	 * to a different month, in which case choose the first business day after
	 * the holiday.
	 */
	MODIFIED_PRECEDING,
	/**
	 * Choose the first business day after the given holiday.
	 */
	FOLLOWING,
	/**
	 * Choose the first business day after the given holiday unless it belongs
	 * to a different month, in which case choose the first business day before
	 * the holiday.
	 */
	MODIFIED_FOLLOWING,
	/**
	 * Choose the first business day after the given holiday, if the original
	 * date falls on last business day of month result reverts to first business
	 * day before month-end
	 */
	MONTH_END_REFERENCE;

}
