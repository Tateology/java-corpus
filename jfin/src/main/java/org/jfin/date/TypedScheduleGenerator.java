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

import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;
import java.text.ParseException;

/**
 *
 */
public class TypedScheduleGenerator<T extends Period> {
	private T prototype;
	private int maxPeriods = -1;

	public TypedScheduleGenerator(T prototype) {
		this.prototype = prototype;
	}

	public TypedScheduleGenerator(T prototype, int maxPeriods) {
		this.prototype = prototype;
		this.maxPeriods = maxPeriods;
	}

	public T getPrototype() {
		return prototype;
	}

	public void setPrototype(T prototype) {
		this.prototype = prototype;
	}

	public int getMaxPeriods() {
		return maxPeriods;
	}

	public void setMaxPeriods(int maxPeriods) {
		this.maxPeriods = maxPeriods;
	}

	public List<T> generateSchedule(Calendar startDate,
			String maturityString, Frequency frequency,
			StubType stubType) throws ScheduleException, ParseException {
		Tenor tenor = new Tenor(maturityString);
		return generateSchedule(startDate, tenor, frequency,stubType);
	}

	public List<T> generateSchedule(Calendar startDate,
			Tenor tenor, Frequency frequency,
			StubType stubType) throws ScheduleException {
		Calendar endDate = copyAndReset(startDate);
		endDate.add(tenor.getCalendarUnit(),tenor.getAmount());
		return generateSchedule(startDate, endDate, frequency.getPeriodAmount(),frequency.getPeriodUnit(),stubType);
	}

	public List<T> generateSchedule(Calendar startDate,
			Calendar endDate, Frequency frequency,
			StubType stubType) throws ScheduleException {
		return generateSchedule(startDate, endDate, frequency.getPeriodAmount(),frequency.getPeriodUnit(),stubType);
	}

	public List<T> generateSchedule(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType) throws ScheduleException {

		switch(stubType) {
			case LONG_FIRST:
				return generateScheduleInternal(endDate,startDate,-1*frequencyAmount,frequencyUnit,stubType);
			case LONG_LAST:
				return generateScheduleInternal(startDate,endDate,frequencyAmount,frequencyUnit,stubType);
			case SHORT_FIRST:
				return generateScheduleInternal(endDate,startDate,-1*frequencyAmount,frequencyUnit,stubType);
			case SHORT_LAST:
				return generateScheduleInternal(startDate,endDate,frequencyAmount,frequencyUnit,stubType);
			default:
				return generateScheduleInternal(startDate,endDate,frequencyAmount,frequencyUnit,stubType);
		}
	}

	private List<T> generateScheduleInternal(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType) throws ScheduleException {

		startDate = copyAndReset(startDate);
		endDate = copyAndReset(endDate);
		Calendar holdDate = copyAndReset(startDate);

		List<T> schedule = new ArrayList<T>();

		int periodCount=1;
		boolean finished = false;
		boolean forwards = frequencyAmount>0;

		while(!finished) {

			Calendar nextDate1 = copyAndReset(startDate);
			nextDate1.add(frequencyUnit,periodCount*frequencyAmount);

			if(stubType.isShortStub()) {
				if(isBeyond(nextDate1,endDate,forwards) || isEqual(nextDate1,endDate)) {
					T period = createPeriod(holdDate,endDate,holdDate,nextDate1,forwards);
					addPeriodToSchedule(schedule,period,forwards);
					finished = true;
				} else {
					T period = createPeriod(holdDate,nextDate1,holdDate,nextDate1,forwards);
					addPeriodToSchedule(schedule,period,forwards);
				}
			} else if(stubType.isLongStub()) {
				Calendar nextDate2 = copyAndReset(startDate);
				nextDate2.add(frequencyUnit,(periodCount+1) * frequencyAmount);

				if(isBeyond(nextDate2,endDate,forwards)) {
					T period = createPeriod(holdDate,endDate,nextDate1,nextDate2,forwards);
					addPeriodToSchedule(schedule,period,forwards);
					finished = true;
				} else {
					T period = createPeriod(holdDate,nextDate1,holdDate,nextDate1,forwards);
					addPeriodToSchedule(schedule,period,forwards);
				}

			} else {
				if(isBeyond(nextDate1,endDate,forwards)) {
					throw new ScheduleException("StubType.NONE used when periods do not fit start and end date");
				} else {

					T period = createPeriod(holdDate,nextDate1,holdDate,nextDate1,forwards);
					addPeriodToSchedule(schedule,period,forwards);

					if(isEqual(nextDate1,endDate)) {
						finished = true;
					}

				}
			}

			holdDate = copyAndReset(nextDate1);

			periodCount++;
			if(maxPeriods>0 && periodCount>maxPeriods) {
				throw new ScheduleException("Maximum number of periods ("+maxPeriods+") exceeded.");
			}
		}

		return schedule;


	}

	private T createPeriod(Calendar startDate, Calendar endDate, Calendar referenceStartDate, Calendar referenceEndDate, boolean forwards) throws ScheduleException {
		T period = createPeriod();
		if(forwards) {
			period.setStartCalendar(startDate);
			period.setEndCalendar(endDate);
			period.setReferenceStartCalendar(referenceStartDate);
			period.setReferenceEndCalendar(referenceEndDate);
		} else {
			period.setStartCalendar(endDate);
			period.setEndCalendar(startDate);
			period.setReferenceStartCalendar(referenceEndDate);
			period.setReferenceEndCalendar(referenceStartDate);
		}

		if(period instanceof Initialisable) {
			Initialisable initialisablePeriod = (Initialisable)period;
			initialisablePeriod.initialise();
		}
		return period;
	}

	private void addPeriodToSchedule(List<T> schedule, T period, boolean forwards) {
		if(forwards) {
			schedule.add(period);
		} else {
			schedule.add(0,period);
		}
	}

	private T createPeriod() throws ScheduleException {
		if(prototype==null) {
			throw new ScheduleException("TypedScheduleGenerator requires a non null prototype to function.");
		} else {
			return (T)prototype.clone();
		}
	}

	private boolean isEqual(Calendar date1, Calendar date2) {
		return !(date1.after(date2) || date1.before(date2));
	}

	private boolean isBeyond(Calendar date1, Calendar date2, boolean forwards) {
		if(forwards) {
			return date1.after(date2);
		} else {
			return date2.after(date1);
		}
	}

	/**
	 * Copy a calendar and reset it's time to midnight to avoid
	 * problems relating to generating schedules around midnight.
	 * @param calendar The calendar to copy and reset
	 * @return Calendar the calendar which has been copied and reset
	 */
	public static Calendar copyAndReset(Calendar calendar) {
		Calendar copy = (Calendar)calendar.clone();

		copy.set(Calendar.HOUR_OF_DAY,0);
		copy.set(Calendar.MINUTE,0);
		copy.set(Calendar.SECOND,0);
		copy.set(Calendar.MILLISECOND,0);

		return copy;
	}
}
