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
 * If startCalendar and endCalendar are equal dates, returns zero.
 */
public class ISMAActualActual extends DaycountCalculator
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.daycount.defaultimpl.ISMAActualActual");

	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar) throws DaycountException
	{
		return calculateDaycountFraction(startCalendar, endCalendar, null, null);
	}

	public double calculateDaycountFraction(Calendar startCalendar,
			Calendar endCalendar, Calendar periodStartCalendar,
			Calendar periodEndCalendar) throws DaycountException
	{

		logger.finer("Calculating daycount fraction for "
				+ ISDADateFormat.format(startCalendar) + " - "
				+ ISDADateFormat.format(endCalendar));

		if (periodStartCalendar != null && periodEndCalendar != null)
		{
			logger.finer("Provided with reference period "
					+ ISDADateFormat.format(periodStartCalendar) + " - "
					+ ISDADateFormat.format(periodEndCalendar));
		}

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

		// when the reference period is not specified, try taking
		// it equal to (d1,d2)
		Calendar refPeriodStart = ( periodStartCalendar != null ? periodStartCalendar
				: startCalendar );
		Calendar refPeriodEnd = ( periodEndCalendar != null ? periodEndCalendar
				: endCalendar );

		logger.finer("Using reference period "
				+ ISDADateFormat.format(refPeriodStart) + " - "
				+ ISDADateFormat.format(refPeriodEnd));

		if (!( refPeriodEnd.after(refPeriodStart) && refPeriodEnd
				.after(startCalendar) ))
		{
			throw new DaycountException("invalid reference period: "
					+ "date 1: " + ISDADateFormat.format(startCalendar)
					+ ", date 2: " + ISDADateFormat.format(endCalendar)
					+ ", reference period start: "
					+ ISDADateFormat.format(refPeriodStart)
					+ ", reference period end: "
					+ ISDADateFormat.format(refPeriodEnd));
		}

		// estimate roughly the length in months of a period
		// Integer months =
		// Integer(0.5+12*Real(refPeriodEnd-refPeriodStart)/365);

		double monthsEstimate = daysBetween(refPeriodStart, refPeriodEnd)
				* ( 12.0d / 365.0d );
		int months = (int) Math.round(monthsEstimate);

		logger.finer("Estimated months as " + months + " (" + monthsEstimate
				+ ")");

		if (months == 0)
		{
			logger
					.fine("Short period (Estimated months is 0), taking reference period as 1 year from d1.");
			refPeriodStart = startCalendar;
			refPeriodEnd = (Calendar) startCalendar.clone();
			refPeriodEnd.add(Calendar.YEAR, 1);
			months = 12;
		}

		double period = (double) months / 12.0;

		logger.finer("Period calculated as " + period);

		if (endCalendar.before(refPeriodEnd)
				|| endCalendar.equals(refPeriodEnd))
		{
			logger
					.finer("refPeriodEnd is a future (maybe notional) payment date");
			if (startCalendar.after(refPeriodStart)
					|| startCalendar.equals(refPeriodStart))
			{
				logger.finer("refPeriodStart is the last (maybe notional) "
						+ "payment date. "
						+ "refPeriodStart <= d1 <= d2 <= refPeriodEnd "
						+ "[maybe the equality should be enforced, since "
						+ "refPeriodStart < d1 <= d2 < refPeriodEnd "
						+ "could give wrong results] ???");

				long numerator = daysBetween(startCalendar, endCalendar);
				long denominator = daysBetween(refPeriodStart, refPeriodEnd);

				logger.fine("Calculating portion between " + "date 1: "
						+ ISDADateFormat.format(startCalendar) + ", date 2: "
						+ ISDADateFormat.format(endCalendar) + " as " + period
						+ " * " + numerator + "/" + denominator);
				return period * (double) numerator / (double) denominator;
			} else
			{
				logger.finer("refPeriodStart is the next (maybe notional) "
						+ "payment date and refPeriodEnd is the second next "
						+ "(maybe notional) payment date. "
						+ "d1 < refPeriodStart < refPeriodEnd "
						+ "AND d2 <= refPeriodEnd "
						+ "this case is long first coupon ");

				Calendar previousRef = (Calendar) startCalendar.clone();
				logger.finer("The last notional payment date is "
						+ ISDADateFormat.format(previousRef));

				//previousRef.add(Calendar.MONTH, months * -1);
				if (endCalendar.after(refPeriodStart))
					return calculateDaycountFraction(startCalendar,
							refPeriodStart, previousRef, refPeriodStart)
							+ calculateDaycountFraction(refPeriodStart,
									endCalendar, refPeriodStart, refPeriodEnd);
				else
					return calculateDaycountFraction(startCalendar,
							endCalendar, previousRef, refPeriodStart);
			}
		} else
		{
			logger
					.finer("refPeriodEnd is the last (notional?) payment date "
							+ "d1 < refPeriodEnd < d2 AND refPeriodStart < refPeriodEnd");
			if (!( refPeriodStart.before(startCalendar) || refPeriodStart
					.equals(startCalendar) ))
			{
				throw new DaycountException(
						"invalid dates: d1 < refPeriodStart < refPeriodEnd < d2");
			}
			logger.finer("refPeriodStart <= d1 < refPeriodEnd < d2");

			// the part from d1 to refPeriodEnd
			double sum = calculateDaycountFraction(startCalendar, refPeriodEnd,
					refPeriodStart, refPeriodEnd);

			logger.finer("The part from d1 to refPeriodEnd is " + sum);

			// the part from refPeriodEnd to d2
			// count how many regular periods are in [refPeriodEnd, d2],
			// then add the remaining time
			int i = 0;
			Calendar newRefStart, newRefEnd;
			do
			{
				newRefStart = (Calendar) refPeriodEnd.clone();
				newRefStart.add(Calendar.MONTH, months * i);
				newRefEnd = (Calendar) refPeriodEnd.clone();
				newRefEnd.add(Calendar.MONTH, ( months * ( i + 1 ) ));
				if (endCalendar.before(newRefEnd))
				{
					break;
				} else
				{
					sum += period;
					i++;
				}
			} while (true);
			double secondSum = calculateDaycountFraction(newRefStart,
					endCalendar, newRefStart, newRefEnd);
			logger.finer("The part from refPeriodEnd to d2 is " + sum);
			sum += secondSum;
			return sum;
		}
	}



}
