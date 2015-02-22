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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.jfin.date.util.ISDADateFormat;

/**
 * Utility class for creating an UNADJUSTED schedule between two dates using a
 * given stub type and frequency.
 */
public class ScheduleGenerator
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.ScheduleGenerator");

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturityString
	 *            Parsed by Tenor, e.g. "10Y", "6M", etc
	 * @param frequency
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 * @throws ParseException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			String maturityString, Frequency frequency, StubType stubType)
			throws ScheduleException, ParseException
	{
		return generateSchedule(startDate,maturityString, frequency, stubType,-1);
	}
	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturityString
	 *            Parsed by Tenor, e.g. "10Y", "6M", etc
	 * @param frequency
	 * @param stubType
	 * @param maxPeriods
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 * @throws ParseException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			String maturityString, Frequency frequency, StubType stubType, int maxPeriods)
			throws ScheduleException, ParseException
	{
		Tenor tenor = new Tenor(maturityString);
		return generateSchedule(startDate, tenor, frequency, stubType, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturityString
	 *            Parsed by Tenor, e.g. "10Y", "6M", etc
	 * @param frequency
	 * @param stubType
	 * @param prototype
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 * @throws ParseException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			String maturityString, Frequency frequency, StubType stubType, Period prototype)
			throws ScheduleException, ParseException
	{
		return generateSchedule(startDate, maturityString, frequency, stubType, prototype, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturityString
	 *            Parsed by Tenor, e.g. "10Y", "6M", etc
	 * @param frequency
	 * @param stubType
	 * @param prototype
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 * @throws ParseException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			String maturityString, Frequency frequency, StubType stubType, Period prototype, int maxPeriods)
			throws ScheduleException, ParseException
	{
		Tenor tenor = new Tenor(maturityString);
		return generateSchedule(startDate, tenor, frequency, stubType, prototype, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturity
	 * @param frequency
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Tenor maturity, Frequency frequency, StubType stubType)
			throws ScheduleException
	{
		return generateSchedule(startDate, maturity, frequency, stubType, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturity
	 * @param frequency
	 * @param stubType
	 * @param maxPeriods
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Tenor maturity, Frequency frequency, StubType stubType, int maxPeriods)
			throws ScheduleException
	{
		Calendar endDate = (Calendar) startDate.clone();
		endDate.add(maturity.getCalendarUnit(), maturity.getAmount());

		return generateSchedule(startDate, endDate, frequency, stubType, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturity
	 * @param frequency
	 * @param stubType
	 * @param prototype
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Tenor maturity, Frequency frequency, StubType stubType, Period prototype)
			throws ScheduleException
	{
		return generateSchedule( startDate,
				 maturity,  frequency,  stubType,  prototype, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param maturity
	 * @param frequency
	 * @param stubType
	 * @param prototype
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Tenor maturity, Frequency frequency, StubType stubType, Period prototype, int maxPeriods)
			throws ScheduleException
	{
		Calendar endDate = (Calendar) startDate.clone();
		endDate.add(maturity.getCalendarUnit(), maturity.getAmount());

		return generateSchedule(startDate, endDate, frequency, stubType, prototype, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, Frequency frequency, StubType stubType)
			throws ScheduleException
	{
		return generateSchedule( startDate,
				 endDate,  frequency,  stubType, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @param stubType
	 * @param maxPeriods
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, Frequency frequency, StubType stubType, int maxPeriods)
			throws ScheduleException
	{
		return generateSchedule(startDate, endDate,
				frequency.getPeriodAmount(), frequency.getPeriodUnit(),
				stubType, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, Frequency frequency, StubType stubType, Period prototype)
			throws ScheduleException
	{
		return generateSchedule( startDate,
				 endDate,  frequency,  stubType,  prototype, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, Frequency frequency, StubType stubType, Period prototype, int maxPeriods)
			throws ScheduleException
	{
		return generateSchedule(startDate, endDate,
				frequency.getPeriodAmount(), frequency.getPeriodUnit(),
				stubType, prototype, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequencyAmount
	 * @param frequencyUnit
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType) throws ScheduleException
	{
		return generateSchedule( startDate,
				 endDate,  frequencyAmount,  frequencyUnit,
				 stubType, -1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequencyAmount
	 * @param frequencyUnit
	 * @param stubType
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType, int maxPeriods) throws ScheduleException
	{
		Period prototype = new Period();

		return generateSchedule(startDate, endDate, frequencyAmount, frequencyUnit, stubType, prototype, maxPeriods);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequencyAmount
	 * @param frequencyUnit
	 * @param stubType
	 * @param prototype
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType, Period prototype) throws ScheduleException
	{
		return generateSchedule(startDate, endDate, frequencyAmount, frequencyUnit, stubType, prototype,-1);
	}

	/**
	 * Used to generate a List containing an schedule ordered by period dates.
	 * The periods will be adjacent to each other, e.g: d1,d2 d2,d3 d3,d4
	 *
	 * @param startDate
	 * @param endDate
	 * @param frequencyAmount
	 * @param frequencyUnit
	 * @param stubType
	 * @param prototype
	 * @param maxPeriods
	 * @return A List<Period> containing the schedule
	 * @throws ScheduleException
	 */
	public static List<Period> generateSchedule(Calendar startDate,
			Calendar endDate, int frequencyAmount, int frequencyUnit,
			StubType stubType, Period prototype, int maxPeriods) throws ScheduleException
	{
		Calendar cleanEndDate = copyAndReset(endDate);
		Calendar cleanStartDate = copyAndReset(startDate);
		int periodCount = 0;
		logger.fine("Generating " +stubType.name()+ " " +frequencyAmount+":"+ getCalendarUnitDescriptor(frequencyUnit) +" schedule between "+ISDADateFormat.format(startDate)+" and "+ISDADateFormat.format(endDate));
		if (stubType == StubType.SHORT_LAST || stubType == StubType.NONE)
		{
			ArrayList<Period> schedule = new ArrayList<Period>();

			Calendar holdDate = copyAndReset(startDate);

			int count = 1;

			while (holdDate.before(cleanEndDate))
			{
				Calendar nextDate = copyAndReset(startDate);
				nextDate.add(frequencyUnit, frequencyAmount*count);
				count++;
				Calendar notionalStartDate = null;
				Calendar notionalEndDate = null;

				if (nextDate.after(cleanEndDate))
				{
					if (stubType == StubType.SHORT_LAST)
					{
						notionalStartDate = holdDate;
						notionalEndDate = nextDate;
						nextDate = cleanEndDate;
					} else
					{
						throw new ScheduleException(
								"StubType.NONE used when periods do not fit start and end date");
					}
				}
				Period toAdd = (Period)prototype.clone();
				toAdd.setStartCalendar(holdDate);
				toAdd.setEndCalendar(nextDate);
				toAdd.setReferenceStartCalendar(notionalStartDate);
				toAdd.setReferenceEndCalendar(notionalEndDate);
				schedule.add(toAdd);

				holdDate = nextDate;
				periodCount++;
				if(maxPeriods>0 && periodCount>maxPeriods) {
					throw new ScheduleException("Maximum number of periods ("+maxPeriods+") exceeded.");
				}
			}

			return schedule;
		} else if (stubType == StubType.SHORT_FIRST)
		{
			ArrayList<Period> schedule = new ArrayList<Period>();

			Calendar holdDate = copyAndReset(endDate);

			int count = 1;

			while (holdDate.after(cleanStartDate))
			{
				Calendar nextDate = copyAndReset(endDate);
				nextDate.add(frequencyUnit, -1 * frequencyAmount*count);
				count++;

				Calendar notionalStartDate = null;
				Calendar notionalEndDate = null;

				if (nextDate.before(cleanStartDate))
				{
					notionalStartDate = nextDate;
					notionalEndDate = holdDate;
					nextDate = cleanStartDate;
				}

				Period toAdd = (Period)prototype.clone();
				toAdd.setStartCalendar(nextDate);
				toAdd.setEndCalendar(holdDate);
				toAdd.setReferenceStartCalendar(notionalStartDate);
				toAdd.setReferenceEndCalendar(notionalEndDate);
				schedule.add(0, toAdd);

				holdDate = nextDate;
				periodCount++;
				if(maxPeriods>0 && periodCount>maxPeriods) {
					throw new ScheduleException("Maximum number of periods ("+maxPeriods+") exceeded.");
				}
			}
			return schedule;
		} else if (stubType == StubType.LONG_LAST)
		{
			ArrayList<Period> schedule = new ArrayList<Period>();

			Calendar holdDate = copyAndReset(startDate);

			int count = 1;

			while (holdDate.before(cleanEndDate))
			{
				Calendar nextDate = copyAndReset(startDate);
				nextDate.add(frequencyUnit, frequencyAmount*count);
				Calendar nextDate2 = copyAndReset(startDate);
				nextDate2.add(frequencyUnit, frequencyAmount*(count+1));
				count++;

				Calendar notionalStartDate = null;
				Calendar notionalEndDate = null;

				if (nextDate2.after(cleanEndDate))
				{
					notionalStartDate = nextDate;
					notionalEndDate = nextDate2;
					nextDate = cleanEndDate;
				}

				Period toAdd = (Period)prototype.clone();
				toAdd.setStartCalendar(holdDate);
				toAdd.setEndCalendar(nextDate);
				toAdd.setReferenceStartCalendar(notionalStartDate);
				toAdd.setReferenceEndCalendar(notionalEndDate);
				schedule.add(toAdd);

				holdDate = nextDate;
				periodCount++;
				if(maxPeriods>0 && periodCount>maxPeriods) {
					throw new ScheduleException("Maximum number of periods ("+maxPeriods+") exceeded.");
				}
			}

			return schedule;
		} else if (stubType == StubType.LONG_FIRST)
		{
			ArrayList<Period> schedule = new ArrayList<Period>();

			Calendar holdDate = copyAndReset(endDate);

			int count=1;
			while (holdDate.after(cleanStartDate))
			{
				Calendar nextDate = copyAndReset(endDate);
				nextDate.add(frequencyUnit, -1 * frequencyAmount*count);
				Calendar nextDate2 = copyAndReset(endDate);
				nextDate2.add(frequencyUnit, -1 * frequencyAmount*(count+1));
				count++;
				Calendar notionalStartDate = null;
				Calendar notionalEndDate = null;

				if (nextDate2.before(cleanStartDate))
				{
					notionalStartDate = nextDate2;
					notionalEndDate = nextDate;
					nextDate = cleanStartDate;
				}

				Period toAdd = (Period)prototype.clone();
				toAdd.setStartCalendar(nextDate);
				toAdd.setEndCalendar(holdDate);
				toAdd.setReferenceStartCalendar(notionalStartDate);
				toAdd.setReferenceEndCalendar(notionalEndDate);
				schedule.add(0, toAdd);

				holdDate = nextDate;
				periodCount++;
				if(maxPeriods>0 && periodCount>maxPeriods) {
					throw new ScheduleException("Maximum number of periods ("+maxPeriods+") exceeded.");
				}
			}

			return schedule;
		} else
		{
			throw new ScheduleException("Unsupported stub type " + stubType);
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

	public static String getCalendarUnitDescriptor(int calendarUnit) {
		switch(calendarUnit) {
			case Calendar.DAY_OF_MONTH:
				return "DAY";
			case Calendar.DAY_OF_WEEK:
				return "DAY";
			case Calendar.DAY_OF_YEAR:
				return "DAY";
			case Calendar.MONTH:
				return "MONTH";
			case Calendar.YEAR:
				return "YEAR";
			default:
				return "UNKNOWN";
		}
	}
}
