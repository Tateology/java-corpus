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

package org.jfin.date.holiday;

import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract template class for all HolidayCalendar implementations.
 *
 * Provides reference implementations for adjusting Calendars by a business day
 * or by a specific amount, adjust Calendars and Periods, and find the business
 * days between two dates.
 *
 * In some implementations of the concrete HolidayCalendar it may be possible to
 * create higher performance calculations and adjustments.
 *
 * @author dmb
 *
 */
public abstract class HolidayCalendar<T extends Period>
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.holiday.HolidayCalendar");

	/**
	 * Returns true if the Calendar falls on a weekend, otherwise false.
	 *
	 * @param d
	 *            The Calendar to check
	 * @return true if the Calendar is a weekend, otherwise false
	 */
	public abstract boolean isWeekend(Calendar d);

	/**
	 * Returns true if the Calendar falls on a holiday or a weekend, otherwise
	 * false.
	 *
	 * @param d
	 *            The Calendar to check
	 * @return true if the Calendar is a holiday, otherwise false
	 */
	public abstract boolean isHoliday(Calendar d);

	/**
	 * Advances the date by offsetAmount business days according to the given
	 * holiday calendar.
	 *
	 * @param calendar
	 * @param offsetAmount
	 *            The number of business days to offset. Positive is forward,
	 *            negative backwards.
	 * @return The offset financial date
	 * @throws HolidayCalendarException
	 */
	public Calendar advanceBusinessDays(Calendar calendar, int offsetAmount)
			throws HolidayCalendarException
	{
		return advance(calendar, offsetAmount, Calendar.DAY_OF_YEAR,
				BusinessDayConvention.UNADJUSTED);
	}

	/**
	 *
	 * @param calendar
	 * @param offsetAmount
	 *            The amount to offset. Positive is forward, negative backwards.
	 * @param offsetUnit
	 *            The unit to offset by. Of form Calendar.DATE, Calendar.MONTH,
	 *            etc.
	 * @param businessDayConvention
	 * @return The offset financial date
	 * @throws HolidayCalendarException
	 */
	public Calendar advance(Calendar calendar, int offsetAmount,
			int offsetUnit, BusinessDayConvention businessDayConvention)
			throws HolidayCalendarException
	{
		if (offsetAmount == 0)
		{
			return adjust(calendar, businessDayConvention);
		} else if (offsetUnit == Calendar.DAY_OF_MONTH
				|| offsetUnit == Calendar.DATE
				|| offsetUnit == Calendar.DAY_OF_YEAR)
		{
			Calendar d1 = (Calendar) calendar.clone();
			if (offsetAmount > 0)
			{
				while (offsetAmount > 0)
				{
					d1.add(Calendar.DATE, 1);
					while (isHoliday(d1) || isWeekend(d1))
						d1.add(Calendar.DATE, 1);
					offsetAmount--;
				}
			} else
			{
				while (offsetAmount < 0)
				{
					d1.add(Calendar.DATE, -1);
					while (isHoliday(d1) || isWeekend(d1))
						d1.add(Calendar.DATE, -1);
					offsetAmount++;
				}
			}
			return d1;
		} else if (offsetUnit == Calendar.MONTH)
		{
			Calendar d1 = (Calendar) calendar.clone();
			d1.add(Calendar.MONTH, offsetAmount);
			return adjust(d1, businessDayConvention, calendar);
		} else if (offsetUnit == Calendar.YEAR)
		{
			Calendar d1 = (Calendar) calendar.clone();
			d1.add(Calendar.YEAR, offsetAmount);
			return adjust(d1, businessDayConvention, calendar);
		} else
		{
			throw new HolidayCalendarException("Unknown time period");

		}
	}


	/**
	 * Adjust the dates in the financial periods using the given business day
	 * convention and holiday calendar.
	 *
	 * @param periods
	 * @param businessDayConvention
	 * @return The adjusted periods.
	 * @throws HolidayCalendarException
	 */
	public List<T> adjust(List<T> periods, BusinessDayConvention businessDayConvention) throws HolidayCalendarException {
		List<T> returnPeriods = new ArrayList<T>();

		for(T period: periods) {
			returnPeriods.add(adjust(period,businessDayConvention));
		}

		return returnPeriods;
	}

	/**
	 * Adjust the dates in the financial period using the given business day
	 * convention and holiday calendar.
	 *
	 * @param period
	 * @param businessDayConvention
	 * @return The adjusted periods.
	 * @throws HolidayCalendarException
	 */
	public T adjust(T period,
			BusinessDayConvention businessDayConvention)
			throws HolidayCalendarException
	{

		logger.fine("Adjusting financial period " + businessDayConvention
				+ " by " + this);

		T adjustedPeriod = (T)period.clone();
		adjustedPeriod.setStartCalendar(adjust(period.getStartCalendar(),businessDayConvention));
		adjustedPeriod.setEndCalendar(adjust(period.getEndCalendar(),businessDayConvention));

		return adjustedPeriod;
	}

	/**
	 * Adjust the financial date using the given business day convention and
	 * holiday calendar.
	 *
	 * @param calendar
	 * @param businessDayConvention
	 * @return The adjusted Calendar
	 * @throws HolidayCalendarException
	 */
	public Calendar adjust(Calendar calendar,
			BusinessDayConvention businessDayConvention)
			throws HolidayCalendarException
	{
		logger.fine("Adjusting date " + calendar + " by "
				+ businessDayConvention + " by " + this);
		return adjust(calendar, businessDayConvention, null);
	}

	/**
	 * Internal method to adjust the financial date using the given business day
	 * convention and holiday calendar. Keeps hold of the original date so as to
	 * be able to handle MonthEndReference convention.
	 *
	 * @param calendar
	 * @param businessDayConvention
	 * @param originalCalendar
	 * @return The adjusted Calendar
	 * @throws HolidayCalendarException
	 */
	private Calendar adjust(Calendar calendar,
			BusinessDayConvention businessDayConvention,
			Calendar originalCalendar) throws HolidayCalendarException
	{

		logger.finer("Business Day Convention " + businessDayConvention);

		if (businessDayConvention == BusinessDayConvention.UNADJUSTED)
		{
			logger
					.finer("Business Day Convention \"Unadjusted\" returning original date.");
			return calendar;
		}

		Calendar d1 = (Calendar) calendar.clone();

		if (businessDayConvention == BusinessDayConvention.FOLLOWING
				|| businessDayConvention == BusinessDayConvention.MODIFIED_FOLLOWING)
		{

			while (isHoliday(d1) || isWeekend(d1))
			{
				logger.finest(d1 + " is a holiday in " + this
						+ " stepping forward.");
				d1.add(Calendar.DATE, 1);
			}
			if (businessDayConvention == BusinessDayConvention.MODIFIED_FOLLOWING)
			{

				if (d1.get(Calendar.MONTH) != calendar.get(Calendar.MONTH))
				{
					logger
							.finer(d1
									+ " moved over month boundary, so have to take previous date from "
									+ calendar);

					return adjust(calendar, BusinessDayConvention.PRECEDING);
				}


			}
		} else if (businessDayConvention == BusinessDayConvention.PRECEDING
				|| businessDayConvention == BusinessDayConvention.MODIFIED_PRECEDING)
		{
			while (isHoliday(d1) || isWeekend(d1))
			{
				logger.finest(d1 + " is a holiday in " + this
						+ " stepping backward.");
				d1.add(Calendar.DATE, -1);
			}
			if (businessDayConvention == BusinessDayConvention.MODIFIED_PRECEDING
					&& d1.get(Calendar.MONTH) != calendar.get(Calendar.MONTH))
			{
				logger
						.finer(d1
								+ " moved over month boundary, so have to take FOLLOWING date from "
								+ calendar);

				return adjust(calendar, BusinessDayConvention.FOLLOWING);
			}
		} else if(businessDayConvention==  BusinessDayConvention.MONTH_END_REFERENCE) {

			d1 = (Calendar)calendar.clone();

			setToEndOfMonth(d1);

			logger.finest("Date moved to " + d1);

			logger.finer("Finding last business day in month for "
					+ d1);

			return adjust(d1, BusinessDayConvention.PRECEDING);


		} else {
			throw new HolidayCalendarException(
					"Unknown business-day convention");
		}

		logger.finer("Returning calculated date " + d1);

		return d1;
	}

	/**
	 * Returns whether or not it is the last day of the month
	 *
	 * @param calendar The calendar to check
	 * @return true if it is the last day of the month, otherwise false
	 */
	public static boolean isLastDayOfMonth(Calendar calendar)
	{
		return calendar.get(Calendar.DAY_OF_MONTH) == calendar
				.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Sets the day to the last day in this month.
	 * @param calendar the calendar to modify
	 * @return The calendar at the last day of the month
	 */
	public static Calendar setToEndOfMonth(Calendar calendar)
	{
		calendar.set(Calendar.DAY_OF_MONTH, getDaysInMonth(calendar));
		return calendar;
	}

	/**
	 * Returns the number of days in the current month.
	 *
	 * @return the number of days (28-31)
	 */
	public static int getDaysInMonth(Calendar calendar)
	{
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns the number of business days between two Calendars.
	 *
	 * @param startCalendar
	 * @param endCalendar
	 * @return The number of business days
	 * @throws HolidayCalendarException
	 */
	public long getBusinessDaysBetween(Calendar startCalendar,
			Calendar endCalendar) throws HolidayCalendarException
	{
		Calendar holdCalendar = (Calendar) startCalendar.clone();
		long businessDays = 0;
		try
		{
			while (holdCalendar.before(endCalendar))
			{
				holdCalendar = advanceBusinessDays(holdCalendar, 1);
				businessDays++;
			}
			return businessDays;
		} catch (HolidayCalendarException e)
		{
			throw new HolidayCalendarException(e);
		}
	}
}
