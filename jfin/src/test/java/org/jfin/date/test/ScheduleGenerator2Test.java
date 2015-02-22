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

package org.jfin.date.test;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.jfin.date.*;
import org.jfin.date.util.ISDADateFormat;

/**
 *
 */
public class ScheduleGenerator2Test extends TestCase {

	private TypedScheduleGenerator scheduleGenerator2;
	private TypedScheduleGenerator scheduleGenerator2Max;


	protected void setUp() throws Exception {
		scheduleGenerator2 = new TypedScheduleGenerator(new Period());
		scheduleGenerator2Max = new TypedScheduleGenerator(new Period(),4);
	}

	public void testLongFirst() throws ParseException, ScheduleException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.LONG_FIRST;
		assertEquals(5, scheduleGenerator2.generateSchedule(effectiveDate, maturityDate, frequency, stubType).size());
	}

	public void testLongLast() throws ParseException, ScheduleException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.LONG_LAST;
		assertEquals(5,scheduleGenerator2.generateSchedule(effectiveDate, maturityDate, frequency, stubType).size());
	}

	public void testShortFirst() throws ParseException, ScheduleException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.SHORT_FIRST;
		assertEquals(5,scheduleGenerator2.generateSchedule(effectiveDate, maturityDate, frequency, stubType).size());
	}

	public void testShortLast() throws ParseException, ScheduleException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.SHORT_LAST;
		assertEquals(5,scheduleGenerator2.generateSchedule(effectiveDate, maturityDate, frequency, stubType).size());
	}

	public void testExceedMaxPeriodsStubTypeShortLast() throws ParseException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.SHORT_LAST;
		try
		{
			scheduleGenerator2Max.generateSchedule(effectiveDate, maturityDate, frequency, stubType);
		} catch (ScheduleException e)
		{
			return;
		}
		fail("Schedule generator should have thrown an exception producing a schedule with 5 periods when maxPeriods was set to 4.");
	}


	public void testExceedMaxPeriodsStubTypeShortFirst() throws ParseException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.SHORT_FIRST;
		try
		{
			scheduleGenerator2Max.generateSchedule(effectiveDate, maturityDate, frequency, stubType);
		} catch (ScheduleException e)
		{
			return;
		}
		fail("Schedule generator should have thrown an exception producing a schedule with 5 periods when maxPeriods was set to 4.");
	}

	public void testExceedMaxPeriodsStubTypeLongLast() throws ParseException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.LONG_LAST;
		try
		{
			scheduleGenerator2Max.generateSchedule(effectiveDate, maturityDate, frequency, stubType);
		} catch (ScheduleException e)
		{
			return;
		}
		fail("Schedule generator should have thrown an exception producing a schedule with 5 periods when maxPeriods was set to 4.");
	}

	public void testExceedMaxPeriodsStubTypeLongFirst() throws ParseException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Calendar maturityDate = ISDADateFormat.parse("2011/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.LONG_FIRST;
		try
		{
			scheduleGenerator2Max.generateSchedule(effectiveDate, maturityDate, frequency, stubType);
		} catch (ScheduleException e)
		{
			return;
		}
		fail("Schedule generator should have thrown an exception producing a schedule with 5 periods when maxPeriods was set to 4.");
	}

	public void testScheduleWithMaturityString() throws ParseException, ScheduleException {
		Calendar effectiveDate = ISDADateFormat.parse("2006/3/28");
		Frequency frequency = Frequency.ANNUALLY;
		StubType stubType = StubType.NONE;
		String maturity = "5Y";
		assertEquals(5,scheduleGenerator2.generateSchedule(effectiveDate, maturity, frequency, stubType).size());
	}

	public void testMonthlySchedule() throws ParseException, ScheduleException {
		Calendar startDate = ISDADateFormat.parse("2006/3/31");
		Calendar endDate = ISDADateFormat.parse("2012/3/31");

		List<Period> schedule = scheduleGenerator2.generateSchedule(startDate,endDate,Frequency.MONTHLY,StubType.SHORT_LAST);


		for(Period period: schedule) {
			Calendar periodEndDate = period.getEndCalendar();
			assertTrue("End date "+ISDADateFormat.format(periodEndDate)+" should be the end of the month.",periodEndDate.get(Calendar.DAY_OF_MONTH)==periodEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		}

	}

	public void testTenorScheduleGeneration() throws ParseException, ScheduleException {
		Calendar startDate = ISDADateFormat.parse("2006/1/1");
		Tenor tenor = new Tenor("2Y");
		List<Period> periods = scheduleGenerator2.generateSchedule(startDate,tenor,Frequency.QUARTERLY,StubType.NONE);
		assertEquals(8,periods.size());
	}

	public void testCustomFrequency() throws ParseException, ScheduleException {
		Calendar startDate = ISDADateFormat.parse("2006/1/1");
		Calendar endDate = ISDADateFormat.parse("2007/1/1");

		List<Period> periods = scheduleGenerator2.generateSchedule(startDate,endDate,2,Calendar.MONTH,StubType.NONE);

		assertEquals(6,periods.size());
	}
}
