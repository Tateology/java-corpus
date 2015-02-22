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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Collector of HolidayCalendars, indistinguishable from any other
 * HolidayCalendar. It can also (unusually) contain sets of other sets.
 */
public class HolidayCalendarSet extends HolidayCalendar
{
	private HashSet<HolidayCalendar> calendars;

	/**
	 * Default constructor
	 */
	public HolidayCalendarSet()
	{
		calendars = new HashSet<HolidayCalendar>();
	}

	/**
	 * Adds a particular holidayCalendar to the set
	 *
	 * @param holidayCalendar
	 */
	public void addHolidayCalendar(HolidayCalendar holidayCalendar)
	{
		calendars.add(holidayCalendar);
	}

	/**
	 * Removes a particular instance of a holidayCalendar from this set
	 *
	 * @param holidayCalendar
	 */
	public void removeHolidayCalendar(HolidayCalendar holidayCalendar)
	{
		calendars.remove(holidayCalendar);
	}

	/**
	 * Returns a set containing all of the holidayCalendars
	 *
	 * @return The Set of holidayCalendars
	 */
	public Set<HolidayCalendar> getHolidayCalendars()
	{
		return calendars;
	}

	/**
	 * Returns an iterator containing all of the holidayCalendars
	 *
	 * @return The Iterator of holidayCalendars
	 */
	public Iterator getHolidayCalendarIterator()
	{
		return calendars.iterator();
	}

	/**
	 * Interrogates all of the contained holidayCalendars returning true if any
	 * one of them is a weekend.
	 *
	 * @param d
	 *            The calendar to check
	 * @return true if any one of the set is a weekend, otherwise false
	 */
	public boolean isWeekend(Calendar d)
	{
		Iterator it = getHolidayCalendarIterator();

		while (it.hasNext())
		{
			HolidayCalendar h = (HolidayCalendar) it.next();
			if (h.isWeekend(d))
				return true;
		}
		return false;
	}

	/**
	 * Interrogates all of the contained holidayCalendars returning true if any
	 * one of them is a holiday.
	 *
	 * @param d
	 *            The calendar to check
	 * @return true if any one of the set is a holiday, otherwise false
	 */
	public boolean isHoliday(Calendar d)
	{
		Iterator it = getHolidayCalendarIterator();

		while (it.hasNext())
		{
			HolidayCalendar h = (HolidayCalendar) it.next();
			if (h.isHoliday(d))
				return true;
		}
		return false;
	}

}
