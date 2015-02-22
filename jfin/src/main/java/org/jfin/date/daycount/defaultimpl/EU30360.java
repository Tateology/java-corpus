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
 * If startCalendar and endCalendar are equal dates, returns zero.
 */
public class EU30360 extends DaycountCalculator
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.daycount.defaultimpl.EU30360");

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
		int dayOfMonth1 = startCalendar.get(Calendar.DAY_OF_MONTH);
		int dayOfMonth2 = endCalendar.get(Calendar.DAY_OF_MONTH);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);

		if(dayOfMonth1==31) {
			dayOfMonth1 = 30;
		}
		if(dayOfMonth2==31) {
			dayOfMonth2 = 30;
		}

		int numerator = 360*( year2 - year1 );
		numerator+= 30 * ( month2 - month1);
		numerator+= dayOfMonth2 - dayOfMonth1;

		return numerator/360d;
	}

	@Override
	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar, Calendar periodStartCalendar,
			Calendar periodEndCalendar) throws DaycountException
	{
		return calculateDaycountFraction(startCalendar, endCalendar);
	}
}
