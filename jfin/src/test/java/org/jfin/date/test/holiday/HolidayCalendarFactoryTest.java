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

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarException;
import org.jfin.date.holiday.HolidayCalendarFactory;

public class HolidayCalendarFactoryTest extends TestCase
{

	/*
	 * Test method for
	 * 'org.jfin.date.test.holiday.HolidayCalendarFactory.getHolidayCalendar(String)'
	 */
	public void testGetHolidayCalendar() throws HolidayCalendarException
	{

		HolidayCalendarFactory testHolidayCalendarFactory = HolidayCalendarFactory
				.newInstance("org.jfin.date.test.holiday.HolidayCalendarFactoryTestImpl");

		HolidayCalendar LnB = testHolidayCalendarFactory
				.getHolidayCalendar("LnB");
		HolidayCalendar NyB = testHolidayCalendarFactory
				.getHolidayCalendar("NyB");
		String[] holidayCalendarArray =
		{
				"LnB", "NyB"
		};
		HolidayCalendar combined = testHolidayCalendarFactory
				.getHolidayCalendar(holidayCalendarArray);

		Calendar fd1 = new GregorianCalendar(2006, 11, 25); // 25th December
		// 2006
		Calendar fd2 = new GregorianCalendar(2007, 0, 1); // 1st January 2007

		assertTrue(LnB.isHoliday(fd1));
		assertFalse(NyB.isHoliday(fd1));
		assertTrue(combined.isHoliday(fd1));
		assertFalse(LnB.isHoliday(fd2));
		assertTrue(NyB.isHoliday(fd2));
		assertTrue(combined.isHoliday(fd2));

	}

	public void testGetSpecificHolidayCalendarFactory()
	{
		System.setProperty("jfin.HolidayCalendarFactory",
				"org.jfin.date.test.holiday.HolidayCalendarFactoryTestImpl");

		assertTrue(HolidayCalendarFactory.newInstance() instanceof org.jfin.date.test.holiday.HolidayCalendarFactoryTestImpl);

		System.clearProperty("jfin.HolidayCalendarFactory");
	}

	public void testHolidayCalendarFactoryNotFound()
	{
		System.setProperty("jfin.HolidayCalendarFactory", "does.not.exist");

		try
		{
			HolidayCalendarFactory.newInstance();
		} catch (HolidayCalendarException e)
		{
			System.clearProperty("jfin.HolidayCalendarFactory");
			return;
		}

		System.clearProperty("jfin.HolidayCalendarFactory");
		fail("Found holiday calendar, even though a non existant one was specified in the jfin.HolidayCalendarFactory property");
	}

	public void testHolidayCalendarFactoryWrongClass()
	{
		System.setProperty("jfin.HolidayCalendarFactory",
				"org.jfin.date.test.holiday.BadHolidayCalendarFactory1");

		try
		{
			HolidayCalendarFactory.newInstance();
		} catch (HolidayCalendarException e)
		{
			System.clearProperty("jfin.HolidayCalendarFactory");
			return;
		}

		System.clearProperty("jfin.HolidayCalendarFactory");
		fail("Found holiday calendar factory, even though it is not an instance of HolidayCalendarFactory");
	}

	public void testHolidayCalendarFactoryNoDefaultConstructor()
	{
		System.setProperty("jfin.HolidayCalendarFactory",
				"org.jfin.date.test.holiday.BadHolidayCalendarFactory2");

		try
		{
			HolidayCalendarFactory.newInstance();
		} catch (HolidayCalendarException e)
		{
			System.clearProperty("jfin.HolidayCalendarFactory");
			return;
		}

		System.clearProperty("jfin.HolidayCalendarFactory");
		fail("Found holiday calendar factory, even though it does not have a default constructor");
	}

	public void testHolidayCalendarFactoryPrivateConstructor()
	{
		System.setProperty("jfin.HolidayCalendarFactory",
				"org.jfin.date.test.holiday.BadHolidayCalendarFactory3");

		try
		{
			HolidayCalendarFactory.newInstance();
		} catch (HolidayCalendarException e)
		{
			System.clearProperty("jfin.HolidayCalendarFactory");
			return;
		}

		System.clearProperty("jfin.HolidayCalendarFactory");
		fail("Found holiday calendar factory, even though its default constructor is private");
	}

	public void testGetSingleHolidayCalendarFromArrayMethod()
	{
		HolidayCalendarFactory testHolidayCalendarFactory = HolidayCalendarFactory
				.newInstance("org.jfin.date.test.holiday.HolidayCalendarFactoryTestImpl");

		HolidayCalendar LnB = testHolidayCalendarFactory
				.getHolidayCalendar(new String[]
				{
					"LnB"
				});

		assertTrue(LnB instanceof LondonHolidayCalendar);

	}

	public void testGetUnknownHolidayCalendar()
	{
		try
		{
			HolidayCalendarFactory.newInstance().getHolidayCalendar("FAIL");
		} catch (HolidayCalendarException e)
		{
			return;
		}

		fail("HolidayCalendarFactory failed to return an exception even though an unknown HolidayCalendar was provided.");
	}

}
