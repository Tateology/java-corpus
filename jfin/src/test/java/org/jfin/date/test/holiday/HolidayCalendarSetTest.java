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

package org.jfin.date.test.holiday;

import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.TestCase;

import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.holiday.HolidayCalendarSet;

public class HolidayCalendarSetTest extends TestCase
{
	public void testHolidayCalendarSet()
	{

		HolidayCalendarFactory testHolidayCalendarFactory = HolidayCalendarFactory
				.newInstance("org.jfin.date.test.holiday.HolidayCalendarFactoryTestImpl");

		HolidayCalendar holidayCalendar = testHolidayCalendarFactory
				.getHolidayCalendar(new String[]
				{
						"LnB", "NyB"
				});

		assertTrue(holidayCalendar.isWeekend(new GregorianCalendar(2006, 0, 1)));
		assertFalse(holidayCalendar
				.isWeekend(new GregorianCalendar(2006, 0, 3)));

		assertTrue(holidayCalendar
				.isHoliday(new GregorianCalendar(2006, 11, 25)));
		assertTrue(holidayCalendar.isHoliday(new GregorianCalendar(2007, 0, 1)));

		HolidayCalendarSet holidayCalendarSet = (HolidayCalendarSet) holidayCalendar;

		assertEquals(holidayCalendarSet.getHolidayCalendars().size(), 2);

		Iterator it = holidayCalendarSet.getHolidayCalendars().iterator();

		NewYorkHolidayCalendar nyb = null;
		while (it.hasNext())
		{
			HolidayCalendar hc = (HolidayCalendar) it.next();

			if (hc instanceof NewYorkHolidayCalendar)
			{
				nyb = (NewYorkHolidayCalendar) hc;
			}
		}
		holidayCalendarSet.removeHolidayCalendar(nyb);

		assertFalse(holidayCalendar
				.isHoliday(new GregorianCalendar(2007, 0, 1)));

	}
}
