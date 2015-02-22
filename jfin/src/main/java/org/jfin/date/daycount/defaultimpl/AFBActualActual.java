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

package org.jfin.date.daycount.defaultimpl;

import java.util.Calendar;
import java.util.logging.Logger;

import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountException;

/**
 * Based upon the implementation from QuantLib http://www.quantlib.org/
 *
 * If startCalendar and endCalendar are equal dates, returns zero.
 */
public class AFBActualActual extends DaycountCalculator
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.daycount.defaultimpl.AFBActualActual");

	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar) throws DaycountException
	{

		if (startCalendar.after(endCalendar))
		{
			logger.fine("Dates are wrong way round so swap over");
			Calendar holdCalendar = startCalendar;
			startCalendar = endCalendar;
			endCalendar = holdCalendar;
		}

		if (startCalendar.equals(endCalendar))
		{
			logger.fine("Dates are the same so return zero");
			return 0.0d;
		}

		Calendar newD2 = endCalendar;
		Calendar temp = (Calendar) endCalendar.clone();
		double sum = 0.0;
		while (temp.after(startCalendar))
		{
			temp = (Calendar) newD2.clone();
			temp.add(Calendar.YEAR, -1);
			if (temp.get(Calendar.DAY_OF_MONTH) == 28
					&& temp.get(Calendar.MONTH) == Calendar.FEBRUARY
					&& temp.getActualMaximum(Calendar.DAY_OF_YEAR) == 366)
			{
				temp.add(Calendar.DAY_OF_YEAR, 1);
			}
			if (temp.after(startCalendar) || temp.equals(startCalendar))
			{
				sum += 1.0;
				newD2 = temp;
			}
		}

		double den = 365.0;

		if (newD2.getActualMaximum(Calendar.DAY_OF_YEAR) == 366)
		{
			temp = (Calendar) newD2.clone();
			temp.set(Calendar.MONTH, Calendar.FEBRUARY);
			temp.set(Calendar.DAY_OF_MONTH, 29);
			if (newD2.after(temp)
					&& ( startCalendar.before(temp) || startCalendar
							.equals(temp) ))
				den += 1.0;
		} else if (startCalendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366)
		{
			temp = (Calendar) startCalendar.clone();
			temp.set(Calendar.MONTH, Calendar.FEBRUARY);
			temp.set(Calendar.DAY_OF_MONTH, 29);
			if (newD2.after(temp)
					&& ( startCalendar.before(temp) || startCalendar
							.equals(temp) ))
				den += 1.0;
		}

		return sum + daysBetween(startCalendar, newD2) / den;
	}

	@Override
	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar, Calendar periodStartCalendar,
			Calendar periodEndCalendar) throws DaycountException
	{
		return calculateDaycountFraction(startCalendar, endCalendar);
	}
}
