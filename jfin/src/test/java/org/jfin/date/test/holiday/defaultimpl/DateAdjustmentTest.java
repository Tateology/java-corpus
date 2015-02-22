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

package org.jfin.date.test.holiday.defaultimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;

import junit.framework.TestCase;

import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.util.ISDADateFormat;

public class DateAdjustmentTest extends TestCase
{

	// private static String rootPath = "../target/test/data/adjustment/";
	// private String rootPath = "src/test/data/adjustment/";
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
		rootPath = "../target/test/data/adjustment/";
		File f = new File(rootPath);
		if (!f.exists())
		{
			rootPath = "src/test/data/adjustment/";
		}
	}

	public void testFollowing()
	{
		executeTest(rootPath + "FOLLOWING.txt", BusinessDayConvention.FOLLOWING);
	}

	public void testModFollowing()
	{
		executeTest(rootPath + "modfollowing.txt",
				BusinessDayConvention.MODIFIED_FOLLOWING);
	}

	public void testPreceeding()
	{
		executeTest(rootPath + "preceeding.txt",
				BusinessDayConvention.PRECEDING);
	}

	public void testModPreceeding()
	{
		executeTest(rootPath + "modpreceeding.txt",
				BusinessDayConvention.MODIFIED_PRECEDING);
	}

	public void testMonthEndReference()
	{
		executeTest(rootPath + "monthend.txt",
				BusinessDayConvention.MONTH_END_REFERENCE);
	}

	public void executeTest(String fileName, BusinessDayConvention convention)
	{
		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();
		HolidayCalendar wend = holidayCalendarFactory.getHolidayCalendar("WE");

		File f = new File(fileName);
		FileReader fread;
		try
		{
			fread = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fread);

			String line = null;

			Period testPeriod = new Period();
			Period testAdjustedPeriod = new Period();

			while (( line = in.readLine() ) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					// Ignore comment or blank line
				} else
				{
					String[] dates = line.split("\t");

					Calendar start = ISDADateFormat.parse(dates[0]);
					Calendar target = ISDADateFormat.parse(dates[1]);

					Calendar adjusted = wend.adjust(start, convention);

					assertTrue(adjusted.get(Calendar.DAY_OF_MONTH) == target
							.get(Calendar.DAY_OF_MONTH));
					assertTrue(adjusted.get(Calendar.MONTH) == target
							.get(Calendar.MONTH));
					assertTrue(adjusted.get(Calendar.YEAR) == target
							.get(Calendar.YEAR));

					if (testPeriod.getStartCalendar() == null)
					{
						testPeriod.setStartCalendar(start);
						testAdjustedPeriod.setStartCalendar(target);
					} else
					{
						if (testPeriod.getEndCalendar() != null)
						{
							testPeriod.setStartCalendar(testPeriod
									.getEndCalendar());
							testAdjustedPeriod
									.setStartCalendar(testAdjustedPeriod
											.getEndCalendar());
						}

						testPeriod.setEndCalendar(start);
						testAdjustedPeriod.setEndCalendar(target);

						assertTrue(wend.adjust(testPeriod, convention)
								.compareTo(testAdjustedPeriod) == 0);
					}
				}
			}

			in.close();
			fread.close();

		} catch (Exception e)
		{
			fail(f.getAbsolutePath() + " " + e.getMessage());
		}

	}
}
