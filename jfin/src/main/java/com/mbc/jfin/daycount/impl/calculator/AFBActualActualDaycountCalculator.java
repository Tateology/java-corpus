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
import com.mbc.jfin.holiday.HolidayCalendar;
import com.mbc.jfin.schedule.SchedulePeriod;
import com.mbc.jfin.util.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Years;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 05/05/2011
 * Time: 09:04
 */
public class AFBActualActualDaycountCalculator implements DaycountCalculator {
    public double calculateDaycountFraction(SchedulePeriod period, HolidayCalendar calendar) {
        int daysBetween = DateUtils.daysBetween(period.getStart(), period.getEnd());

        if(daysBetween==0) return 0;

        LocalDate newD2 = period.getEnd();
		LocalDate temp = period.getEnd();

		double sum = 0.0;
		while (temp.isAfter(period.getStart()))
		{
			temp = newD2;
			temp = temp.minus(Years.ONE);
			if (
                    temp.getDayOfMonth() == 28 &&
			        temp.getMonthOfYear() == 2 &&
					DateUtils.isLeapYear(temp))
			{
				temp = temp.plus(Days.ONE);
			}
			if (temp.isAfter(period.getStart()) || temp.equals(period.getStart()))
			{
				sum += 1.0;
				newD2 = temp;
			}
		}

		double den = 365.0;

		if (DateUtils.isLeapYear(newD2))
		{
			temp = newD2;

            temp = new LocalDate(temp.getYear(), 2, 29);

			if (
                    newD2.isAfter(temp)
					&& ( period.getStart().isBefore(temp) || period.getStart().equals(temp) )
                )
				den += 1.0;
		} else if (DateUtils.isLeapYear(period.getStart()))
		{

            temp = new LocalDate(period.getStart().getYear(), 2, 29);

			if (newD2.isAfter(temp)
					&& ( period.getStart().isBefore(temp) || period.getStart()
							.equals(temp) ))
				den += 1.0;
		}

		return sum + DateUtils.daysBetween(period.getStart(), newD2) / den;

    }
}
