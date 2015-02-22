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
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 09:03
 */
public class ISDAActualActualDaycountCalculator implements DaycountCalculator {



    public double calculateDaycountFraction(SchedulePeriod period, HolidayCalendar calendar) {

        if(period.getStart().equals(period.getEnd())) return 0;

		double fraction = 0;
		List<SchedulePeriod> subPeriods = getPeriods(period);

		int[] numerators = getNumerators(subPeriods);
		int[] denominators = getDenominators(subPeriods);

		for(int i=0;i<numerators.length;i++) {
			fraction+= (double)numerators[i]/(double)denominators[i];
		}

		return fraction;
	}

	public List<SchedulePeriod> getPeriods(SchedulePeriod period) {
		LocalDate hold = period.getStart();
		LocalDate end = period.getEnd();

		List<SchedulePeriod> periods = new ArrayList<SchedulePeriod>();


		while(hold.getYear()<end.getYear()) {
			LocalDate holdEnd = new LocalDate(hold.getYear()+1, 1, 1);

			periods.add(new SchedulePeriod(hold,holdEnd));
			hold = holdEnd;
		}

		periods.add(new SchedulePeriod(hold,end));

		return periods;
	}

	/**
	 * Calculates the numerators for a set of periods
	 *
	 * @param subPeriods A list of the sub periods
	 * @return An array of type int containing the numberators for the periods
	 */
	public int[] getNumerators(List<SchedulePeriod> subPeriods) {

		int[] numerators = new int[subPeriods.size()];

		for(int i=0;i<subPeriods.size();i++) {
			SchedulePeriod period = subPeriods.get(i);
			numerators[i] = DateUtils.daysBetween(period.getStart(), period.getEnd());
		}

		return numerators;
	}

	/**
	 * Calculates the denominators for a set of periods
	 *
	 * @param subPeriods A list of the sub periods
	 * @return An array of type int containing the denominators of the sub periods
	 */
	public int[] getDenominators(List<SchedulePeriod> subPeriods) {

		int[] denominators = new int[subPeriods.size()];

		for(int i=0;i<subPeriods.size();i++) {
			SchedulePeriod period = subPeriods.get(i);
			denominators[i] = DateUtils.getDaysInYear(period.getStart());
		}

		return denominators;
	}
}
