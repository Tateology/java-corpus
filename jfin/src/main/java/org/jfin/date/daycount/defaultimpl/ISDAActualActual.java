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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.jfin.date.Period;
import org.jfin.date.ScheduleGenerator;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountException;
import org.jfin.date.util.ISDADateFormat;

/**
 * Based upon the implementation from QuantLib http://www.quantlib.org/
 *
 * If startCalendar and endCalendar are equal dates, returns zero.
 */

public class ISDAActualActual extends DaycountCalculator
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.daycount.defaultimpl.ISDAActualActual");

	/**
	 * Calculates the ISDA Actual actual day count fraction
	 * between two dates.
	 */
	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar) throws DaycountException
	{
		logger.finer("Calculating daycount fraction for "
				+ ISDADateFormat.format(startCalendar) + " - "
				+ ISDADateFormat.format(endCalendar));

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
			return 0.0;
		}

		double fraction = 0;
		List<Period> subPeriods = getPeriods(startCalendar,endCalendar);

		int[] numerators = getNumerators(subPeriods);
		int[] denominators = getDenominators(subPeriods);

		for(int i=0;i<numerators.length;i++) {
			fraction+= (double)numerators[i]/(double)denominators[i];
		}

		return fraction;
	}

	/**
	 * Calculates the ISDA Actual actual day count fraction
	 * between two dates. ISDA Actual/Actual does not
	 * vary depending on the reference period, so this
	 * is exactly equivelent to calling
	 * calculateDaycountFraction(startCalendar, endCalendar)
	 */
	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar, Calendar periodStartCalendar,
			Calendar periodEndCalendar) throws DaycountException
	{
		return calculateDaycountFraction(startCalendar, endCalendar);
	}

	/**
	 * Generate a list of the sub periods (up to and including year
	 * boundaries) that are required to calculate the ISDA Actual Actual
	 * day count fraction
	 *
	 * @param startCalendar The start calendar of the period
	 * @param endCalendar The end calendar of the period
	 * @return A list of sub periods between the start and end calendars
	 */
	public List<Period> getPeriods(Calendar startCalendar, Calendar endCalendar) {
		Calendar hold = ScheduleGenerator.copyAndReset(startCalendar);
		Calendar end = ScheduleGenerator.copyAndReset(endCalendar);

		List<Period> periods = new ArrayList<Period>();


		while(hold.get(Calendar.YEAR)<end.get(Calendar.YEAR)) {
			Calendar holdEnd = ScheduleGenerator.copyAndReset(hold);
			holdEnd.set(Calendar.YEAR,hold.get(Calendar.YEAR)+1);
			holdEnd.set(Calendar.MONTH,Calendar.JANUARY);
			holdEnd.set(Calendar.DAY_OF_MONTH,1);

			periods.add(new Period(ScheduleGenerator.copyAndReset(hold),holdEnd));
			hold = ScheduleGenerator.copyAndReset(holdEnd);
		}

		periods.add(new Period(hold,end));

		return periods;
	}

	/**
	 * Calculates the numerators for a set of periods
	 *
	 * @param subPeriods A list of the sub periods
	 * @return An array of type int containing the numberators for the periods
	 */
	public int[] getNumerators(List<Period> subPeriods) {

		int[] numerators = new int[subPeriods.size()];

		for(int i=0;i<subPeriods.size();i++) {
			Period period = subPeriods.get(i);
			numerators[i] = (int)daysBetween(period.getStartCalendar(), period.getEndCalendar());
		}

		return numerators;
	}

	/**
	 * Calculates the denominators for a set of periods
	 *
	 * @param subPeriods A list of the sub periods
	 * @return An array of type int containing the denominators of the sub periods
	 */
	public int[] getDenominators(List<Period> subPeriods) {

		int[] denominators = new int[subPeriods.size()];

		for(int i=0;i<subPeriods.size();i++) {
			Period period = subPeriods.get(i);
			denominators[i] = period.getStartCalendar().getActualMaximum(Calendar.DAY_OF_YEAR);
		}

		return denominators;
	}
}
