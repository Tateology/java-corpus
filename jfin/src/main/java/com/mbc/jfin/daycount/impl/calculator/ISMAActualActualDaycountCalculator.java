/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p>Copyright (C) 2005-2011 Morgan Brown Consultancy Ltd. </p>
 *
 * <p>This file is part of jFin. </p>
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

package com.mbc.jfin.daycount.impl.calculator;

import com.mbc.jfin.daycount.impl.DaycountCalculator;
import com.mbc.jfin.daycount.impl.InvalidReferencePeriodException;
import com.mbc.jfin.holiday.HolidayCalendar;
import com.mbc.jfin.schedule.SchedulePeriod;
import com.mbc.jfin.util.DateUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 09:13
 */
public class ISMAActualActualDaycountCalculator implements DaycountCalculator {

    public double calculateDaycountFraction(SchedulePeriod schedulePeriod, HolidayCalendar calendar) {
        return  calculateDaycountFraction(schedulePeriod);
    }

    private double calculateDaycountFraction(SchedulePeriod schedulePeriod) {

        if(schedulePeriod.getStart().equals(schedulePeriod.getEnd())) return 0;

		// when the reference period is not specified, try taking
		// it equal to (d1,d2)
		LocalDate refPeriodStart = ( schedulePeriod.getReferenceStart() != null ? schedulePeriod.getReferenceStart() : schedulePeriod.getStart() );
		LocalDate refPeriodEnd = ( schedulePeriod.getReferenceEnd() != null ? schedulePeriod.getReferenceEnd() : schedulePeriod.getEnd() );


        LocalDate startCalendar = schedulePeriod.getStart();
        LocalDate endCalendar = schedulePeriod.getEnd();

		if (!( refPeriodEnd.isAfter(refPeriodStart) && refPeriodEnd
				.isAfter(startCalendar) ))
		{
			throw new InvalidReferencePeriodException(schedulePeriod);
		}

		// estimate roughly the length in months of a period
		// Integer months =
		// Integer(0.5+12*Real(refPeriodEnd-refPeriodStart)/365);

		double monthsEstimate = DateUtils.daysBetween(refPeriodStart, refPeriodEnd)
				* ( 12.0d / 365.0d );
		int months = (int) Math.round(monthsEstimate);

		if (months == 0)
		{
			refPeriodStart = startCalendar;
			refPeriodEnd = startCalendar.plus(Years.ONE);
			months = 12;
		}

		double period = (double) months / 12.0;


		if (endCalendar.isBefore(refPeriodEnd)
				|| endCalendar.equals(refPeriodEnd))
		{
			if (startCalendar.isAfter(refPeriodStart)
					|| startCalendar.equals(refPeriodStart))
			{
				long numerator = DateUtils.daysBetween(startCalendar, endCalendar);
				long denominator = DateUtils.daysBetween(refPeriodStart, refPeriodEnd);

				return period * (double) numerator / (double) denominator;
			} else
			{


				LocalDate previousRef = startCalendar;

				//previousRef.add(Calendar.MONTH, months * -1);
				if (endCalendar.isAfter(refPeriodStart))
					return calculateDaycountFraction(new SchedulePeriod(startCalendar,
							refPeriodStart, previousRef, refPeriodStart))
							+ calculateDaycountFraction(new SchedulePeriod(refPeriodStart,
									endCalendar, refPeriodStart, refPeriodEnd));
				else
					return calculateDaycountFraction(new SchedulePeriod(startCalendar,
							endCalendar, previousRef, refPeriodStart));
			}
		} else
		{
			if (!( refPeriodStart.isBefore(startCalendar) || refPeriodStart
					.equals(startCalendar) ))
			{
				throw new InvalidReferencePeriodException(schedulePeriod);
			}

			// the part from d1 to refPeriodEnd
			double sum = calculateDaycountFraction(new SchedulePeriod(startCalendar, refPeriodEnd,
					refPeriodStart, refPeriodEnd));


			// the part from refPeriodEnd to d2
			// count how many regular periods are in [refPeriodEnd, d2],
			// then add the remaining time
			int i = 0;
			LocalDate newRefStart, newRefEnd;
			do
			{
				newRefStart = refPeriodEnd.plus(Months.months(months*i));
				newRefEnd = refPeriodEnd.plus(Months.months(months*(i+1)));
				if (endCalendar.isBefore(newRefEnd))
				{
					break;
				} else
				{
					sum += period;
					i++;
				}
			} while (true);
			double secondSum = calculateDaycountFraction(new SchedulePeriod(newRefStart,
					endCalendar, newRefStart, newRefEnd));

			sum += secondSum;
			return sum;
		}
	}
}
