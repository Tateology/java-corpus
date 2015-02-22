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

package org.jfin.date.test.holiday.financialcalendarimpl;

import java.io.File;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarException;
import org.jfin.date.holiday.HolidayCalendarFactory;

public class FinancialCalendarHolidayCalendarTest extends TestCase
{
	private String rootPath = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		rootPath = "src/test/data/financialcalendar/";
		File f = new File(rootPath);
		if(!f.exists()) {
			rootPath = "../src/test/data/financialcalendar/";
		}
	}

	public void testGetHolidayCalendar()
	{
		System
				.setProperty(
						"jfin.HolidayCalendarFactory",
						"org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl");
		System.setProperty("jfin.FinancialCalendar.HolidayFilePath", rootPath
				+ "jfin.csv");
		System.setProperty("jfin.FinancialCalendar.InfoFilePath", rootPath
				+ "info.csv");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();

		assertTrue(holidayCalendarFactory instanceof org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl);

		System.clearProperty("jfin.HolidayCalendarFactory");
		System.clearProperty("jfin.FinancialCalendar.HolidayFilePath");
		System.clearProperty("jfin.FinancialCalendar.InfoFilePath");
	}

	public void testLondonHolidayCalendar()
	{
		System
				.setProperty(
						"jfin.HolidayCalendarFactory",
						"org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl");
		System.setProperty("jfin.FinancialCalendar.HolidayFilePath", rootPath
				+ "jfin.csv");
		System.setProperty("jfin.FinancialCalendar.InfoFilePath", rootPath
				+ "info.csv");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();

		HolidayCalendar LnB = holidayCalendarFactory.getHolidayCalendar("LnB");

		assertTrue(LnB.isHoliday(new GregorianCalendar(2006, 0, 30)));
		assertTrue(!LnB.isWeekend(new GregorianCalendar(2006, 0, 30)));
		assertTrue(LnB.isHoliday(new GregorianCalendar(2006, 0, 15)));
		assertTrue(LnB.isWeekend(new GregorianCalendar(2006, 0, 15)));
		assertTrue(!LnB.isHoliday(new GregorianCalendar(2006, 1, 21)));

		System.clearProperty("jfin.HolidayCalendarFactory");
		System.clearProperty("jfin.FinancialCalendar.HolidayFilePath");
		System.clearProperty("jfin.FinancialCalendar.InfoFilePath");
	}

	public void testBadHolidayCalendar()
	{
		System
				.setProperty(
						"jfin.HolidayCalendarFactory",
						"org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl");
		System.setProperty("jfin.FinancialCalendar.HolidayFilePath", rootPath
				+ "jfin.csv");
		System.setProperty("jfin.FinancialCalendar.InfoFilePath", rootPath
				+ "info.csv");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();

		try
		{
			holidayCalendarFactory.getHolidayCalendar("TEST");
		} catch (HolidayCalendarException e)
		{
			System.clearProperty("jfin.HolidayCalendarFactory");
			System.clearProperty("jfin.FinancialCalendar.HolidayFilePath");
			System.clearProperty("jfin.FinancialCalendar.InfoFilePath");
			return;
		}

		fail("Failed to throw an exception finding HolidayCalendar TEST");

	}

	public void testCombinedHolidayCalendar()
	{
		System
				.setProperty(
						"jfin.HolidayCalendarFactory",
						"org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl");
		System.setProperty("jfin.FinancialCalendar.HolidayFilePath", rootPath
				+ "jfin.csv");
		System.setProperty("jfin.FinancialCalendar.InfoFilePath", rootPath
				+ "info.csv");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();

		HolidayCalendar holidayCalendar = holidayCalendarFactory
				.getHolidayCalendar(new String[]
				{
						"LnB", "TkB"
				});

		assertTrue(holidayCalendar
				.isHoliday(new GregorianCalendar(2006, 0, 30)));
		assertTrue(!holidayCalendar
				.isWeekend(new GregorianCalendar(2006, 0, 30)));
		assertTrue(holidayCalendar
				.isHoliday(new GregorianCalendar(2006, 0, 15)));
		assertTrue(holidayCalendar
				.isWeekend(new GregorianCalendar(2006, 0, 15)));
		assertTrue(holidayCalendar
				.isHoliday(new GregorianCalendar(2006, 1, 21)));

		System.clearProperty("jfin.HolidayCalendarFactory");
		System.clearProperty("jfin.FinancialCalendar.HolidayFilePath");
		System.clearProperty("jfin.FinancialCalendar.InfoFilePath");

	}

	public void testAvailableLocales() {

		System
				.setProperty(
						"jfin.HolidayCalendarFactory",
						"org.jfin.date.holiday.financialcalendarimpl.HolidayCalendarFactoryFinancialCalendarImpl");
		System.setProperty("jfin.FinancialCalendar.HolidayFilePath", rootPath
				+ "jfin.csv");
		System.setProperty("jfin.FinancialCalendar.InfoFilePath", rootPath
				+ "info.csv");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();

		String[] availableLocales = holidayCalendarFactory.getAvailableLocales();

		assertEquals(3,availableLocales.length);
		assertEquals("LnB",availableLocales[0]);
		assertEquals("NYB",availableLocales[1]);
		assertEquals("TkB",availableLocales[2]);

		System.clearProperty("jfin.HolidayCalendarFactory");
		System.clearProperty("jfin.FinancialCalendar.HolidayFilePath");
		System.clearProperty("jfin.FinancialCalendar.InfoFilePath");
	}
}
