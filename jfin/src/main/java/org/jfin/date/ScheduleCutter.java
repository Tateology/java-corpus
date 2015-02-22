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

import org.jfin.date.util.ISDADateFormat;

import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Class which provides functionality required to 'cut' one schedule
 * by another schedule.
 *
 * This is used, for example, when you have a seperate schedule for the
 * accrual periods from the notional, and the notional changes at a different
 * frequency, or on different dates, from the accrual periods.
 */
public class ScheduleCutter<T extends Period> {

	private static Logger logger = Logger.getLogger("org.jfin.date.ScheduleCutter");
	private static ScheduleCutterPeriodComparator scheduleCutterPeriodComparator = new ScheduleCutterPeriodComparator();

	/**
	 * Cuts the scheduleToCut using the dates contained within the cutTemplate
	 *
	 * @param scheduleToCut The schedule which contains the periods which are going to be cut
	 * @param cutTemplate The schedule which contains the dates which are used to cut the scheduleToCut
	 * @return A list of periods from scheduleToCut, cut and cloned to match the cutTemplate
	 * @throws ScheduleException Throws a schedule exception if there are errors during the cut operation
	 */
	public List<T> cutSchedules(List<T> scheduleToCut, List<? extends Period> cutTemplate) throws ScheduleException {
		List<Calendar> cutCalendars = new ArrayList<Calendar>();

		logger.finer("Creating distinct list of dates in the cut template.");

		for(Period cutTemplatePeriod: cutTemplate) {
			if(!cutCalendars.contains(cutTemplatePeriod.getStartCalendar())) {
				cutCalendars.add(cutTemplatePeriod.getStartCalendar());
			}
			if(!cutCalendars.contains(cutTemplatePeriod.getEndCalendar())) {
				cutCalendars.add(cutTemplatePeriod.getEndCalendar());
			}
		}

		return cutScheduleByDates(scheduleToCut,cutCalendars);
	}

	public List<T> cutScheduleByDates(List<T> scheduleToCut, List<Calendar> cutDates) throws ScheduleException {

		logger.finer("Ensuring that the schedule to cut does not have overlapping periods.");

		Collections.sort(scheduleToCut,scheduleCutterPeriodComparator);

		for(int i=1;i<scheduleToCut.size();i++) {
			Period period1 = scheduleToCut.get(i-1);
			Period period2 = scheduleToCut.get(i);

			if(period1.getEndCalendar().compareTo(period2.getStartCalendar())>0) {
				throw new ScheduleException("Could not cut schedule with overlapping periods (period "+(i-1)+": "+period1+" and period "+i+": "+period2);
			}
		}

		logger.finer("Cutting schedule");

		List<T> resultantSchedule = new ArrayList<T>();

		for(T period: scheduleToCut) {

			resultantSchedule.addAll(cutPeriodByDates(period,cutDates));

		}



		return resultantSchedule;
	}

	/**
	 * Cuts the period based upon the cutDates and returns a List of the resultant periods or a List containing the original period if it
	 * wasn't cut.
	 *
	 * @param period The period to cut
	 * @param cutDates The dates with which to perform the cut
	 * @return The resultant period or periods
	 */
	public List<T> cutPeriodByDates(T period, List<Calendar> cutDates) {
		List<T> resultantSchedule = new ArrayList<T>();

		for(Calendar date: cutDates) {
			if(periodContainsDate(period,date)) {
				logger.finest("Cutting period "+period+" by date "+ ISDADateFormat.format(date));
				T cutPeriod1 = (T)period.clone();
				T cutPeriod2 = (T)period.clone();
				cutPeriod1.setEndCalendar(date);
				cutPeriod2.setStartCalendar(date);

				resultantSchedule.addAll(cutPeriodByDates(cutPeriod1,cutDates));
				resultantSchedule.addAll(cutPeriodByDates(cutPeriod2,cutDates));

				return resultantSchedule;
			}
		}

		logger.finest("Period "+period+" not cut by any of the cut dates");

		resultantSchedule.add(period);

		return resultantSchedule;
	}

	/**
	 * Returns true if the date provided is *within* the period; the date given must be between
	 * the start and end date of the period excluding the start and end date.
	 *
	 * @param period The period to check
	 * @param date The date to check
	 * @return true if the date is between the start and end date of the period excluding the start and end date, otherwise false
	 */
	public boolean periodContainsDate(T period, Calendar date) {

		return period.getStartCalendar().before(date) && period.getEndCalendar().after(date);

	}
}
