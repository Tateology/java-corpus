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

package org.jfin.date.daycount;

import java.util.Calendar;

import org.jfin.date.Period;

/**
 * Base class for DaycountCalculators. Used to interrogate the concrete
 * instances of daycount basis calculators for day count fractions between two
 * dates, or accross a period.
 *
 * @author dmb
 */
public abstract class DaycountCalculator
{

	/**
	 * Calculates the daycount fraction of the period; from the startCalendar to
	 * the endCalendar.
	 *
	 * @param period
	 * @return The day count fraction of a year
	 * @throws DaycountException
	 */
	public double calculateDaycountFraction(Period period)
			throws DaycountException
	{
		return calculateDaycountFraction(period.getStartCalendar(), period
				.getEndCalendar(), period.getReferenceStartCalendar(), period
				.getReferenceEndCalendar());

	}

	/**
	 * Calculates the daycount fraction of the period represented by the
	 * startCalendar and endCalendar
	 *
	 * @param startCalendar
	 * @param endCalendar
	 * @return The day count fraction of a year
	 * @throws DaycountException
	 */
	public abstract double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar) throws DaycountException;

	/**
	 * Calculates the daycount fraction of the period represented by the
	 * startCalendar and endCalendar with reference to the period represented by
	 * periodStartCalendar and periodEndCalendar. Used when the period
	 * represented by startCalendar and endCalendar is a long or short stub
	 * period, the periodStartCalendar and periodEndCalendar represent the
	 * 'notional' period as if it were a full period and not a stub
	 *
	 * @param startCalendar
	 * @param endCalendar
	 * @param periodStartCalendar
	 * @param periodEndCalendar
	 * @return The day count fraction of a year
	 * @throws DaycountException
	 */
	public abstract double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar, Calendar periodStartCalendar,
			Calendar periodEndCalendar) throws DaycountException;

	/**
	 * Returns the nearest calculation of the number of calendar days between
	 * two Calendars.
	 *
	 * @param d1
	 * @param d2
	 * @return The number of days between the two Calendars
	 */
	public long daysBetween(Calendar d1, Calendar d2)
	{
		return (long) Math.round(( d2.getTimeInMillis() - d1.getTimeInMillis() )
				/ ( 1000d * 60d * 60d * 24d ));
	}
}
