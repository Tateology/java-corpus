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

import java.util.Calendar;

/**
 * Utility class providing a type safe way of describing common periods.
 */
public enum Frequency
{
	/**
	 * Repeats every day
	 */
	DAILY(Calendar.DAY_OF_YEAR, 1, "1D"),

	/**
	 * Repeats every day
	 */
	WEEKLY(Calendar.DAY_OF_YEAR, 7, "1W"),

	/**
	 * Repeats every 28 days
	 */
	LUNAR_MONTHLY(Calendar.DAY_OF_YEAR, 28, "1L"),
	/**
	 * Repeats on the same day of every month
	 */
	MONTHLY(Calendar.MONTH, 1, "1M"),
	/**
	 * Repeats on the same day every three months
	 */
	QUARTERLY(Calendar.MONTH, 3, "3M"),
	/**
	 * Repeats on the same day every six months
	 */
	SEMI_ANNUALLY(Calendar.MONTH, 6, "6M"),
	/**
	 * Repeats on the same day every year
	 */
	ANNUALLY(Calendar.YEAR, 1, "1Y");

	private int periodUnit;

	private int periodAmount;

	private String tenorDescriptor;

	/**
	 * Private constructor
	 *
	 * @param periodUnit
	 *            The unit upon which the period is based (as
	 *            java.util.GregorianCalendar)
	 * @param periodAmount
	 *            The amount to adjust
	 */
	private Frequency(int periodUnit, int periodAmount, String tenorDescriptor)
	{
		this.periodUnit = periodUnit;
		this.periodAmount = periodAmount;
		this.tenorDescriptor = tenorDescriptor;
	}

	/**
	 * Returns the amount to adjust, e.g. Quarterly would return 3
	 *
	 * @return The amount to adjust, e.g. Quarterly would return 3
	 */
	public int getPeriodAmount()
	{
		return periodAmount;
	}

	/**
	 * Returns the unit upon which the period is based, e.g. Quarterly would
	 * return java.util.GregorianCalendar.MONTH
	 *
	 * @return The unit upon which the period is based, e.g. Quarterly would
	 *         return java.util.GregorianCalendar.MONTH
	 */
	public int getPeriodUnit()
	{
		return periodUnit;
	}

	/**
	 * Returns the tenor descriptor for this period
	 *
	 * @return Returns the tenorDescriptor.
	 */
	public String getTenorDescriptor()
	{
		return tenorDescriptor;
	}

}
