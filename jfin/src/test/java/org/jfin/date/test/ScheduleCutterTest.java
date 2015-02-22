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

import org.jfin.date.Period;
import org.jfin.date.ScheduleCutter;
import org.jfin.date.ScheduleException;
import org.jfin.date.util.ISDADateFormat;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.text.ParseException;

import junit.framework.TestCase;

/**
 *
 */
public class ScheduleCutterTest extends TestCase {


	public void testCutSinglePeriodByDate() throws ParseException {
		Calendar startDate = ISDADateFormat.parse("2006/01/01");
		Calendar endDate = ISDADateFormat.parse("2006/03/01");
		Calendar cutDate = ISDADateFormat.parse("2006/02/01");

		List<Calendar> cutDates = new ArrayList<Calendar>();

		cutDates.add(cutDate);

		Period period = new Period(startDate,endDate);

		ScheduleCutter<Period> scheduleCutter = new ScheduleCutter<Period>();

		List<Period> resultPeriods = scheduleCutter.cutPeriodByDates(period,cutDates);

		assertEquals(2,resultPeriods.size());

		Period cutPeriod1 = resultPeriods.get(0);
		Period cutPeriod2 = resultPeriods.get(1);

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(cutPeriod1.getStartCalendar()));
		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod1.getEndCalendar()));

		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod2.getStartCalendar()));
		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(cutPeriod2.getEndCalendar()));

	}

	public void testCutSinglePeriodByDates() throws ParseException {
		Calendar startDate = ISDADateFormat.parse("2006/01/01");
		Calendar endDate = ISDADateFormat.parse("2006/04/01");
		Calendar cutDate1 = ISDADateFormat.parse("2006/02/01");
		Calendar cutDate2 = ISDADateFormat.parse("2006/03/01");

		List<Calendar> cutDates = new ArrayList<Calendar>();

		cutDates.add(cutDate1);
		cutDates.add(cutDate2);

		Period period = new Period(startDate,endDate);

		ScheduleCutter<Period> scheduleCutter = new ScheduleCutter<Period>();

		List<Period> resultPeriods = scheduleCutter.cutPeriodByDates(period,cutDates);

		assertEquals(3,resultPeriods.size());

		Period cutPeriod1 = resultPeriods.get(0);
		Period cutPeriod2 = resultPeriods.get(1);
		Period cutPeriod3 = resultPeriods.get(2);

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(cutPeriod1.getStartCalendar()));
		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod1.getEndCalendar()));

		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod2.getStartCalendar()));
		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(cutPeriod2.getEndCalendar()));

		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(cutPeriod3.getStartCalendar()));
		assertEquals("2006/04/01",ISDADateFormat.formatFixedLength(cutPeriod3.getEndCalendar()));

	}

	public void testCutMultiplePeriodsByDates() throws ParseException, ScheduleException {
		Calendar startDate1 = ISDADateFormat.parse("2006/01/01");
		Calendar endDate1 = ISDADateFormat.parse("2006/04/01");
		Calendar startDate2 = ISDADateFormat.parse("2006/04/01");
		Calendar endDate2 = ISDADateFormat.parse("2006/07/01");
		Calendar cutDate1 = ISDADateFormat.parse("2006/02/01");
		Calendar cutDate2 = ISDADateFormat.parse("2006/03/01");

		List<Calendar> cutDates = new ArrayList<Calendar>();

		cutDates.add(cutDate1);
		cutDates.add(cutDate2);

		Period period1 = new Period(startDate1,endDate1);
		Period period2 = new Period(startDate2,endDate2);

		List<Period> periodsToCut = new ArrayList<Period>();
		periodsToCut.add(period1);
		periodsToCut.add(period2);

		ScheduleCutter<Period> scheduleCutter = new ScheduleCutter<Period>();

		List<Period> resultPeriods = scheduleCutter.cutScheduleByDates(periodsToCut,cutDates);

		assertEquals(4,resultPeriods.size());

		Period cutPeriod1 = resultPeriods.get(0);
		Period cutPeriod2 = resultPeriods.get(1);
		Period cutPeriod3 = resultPeriods.get(2);
		Period cutPeriod4 = resultPeriods.get(3);

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(cutPeriod1.getStartCalendar()));
		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod1.getEndCalendar()));

		assertEquals("2006/02/01",ISDADateFormat.formatFixedLength(cutPeriod2.getStartCalendar()));
		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(cutPeriod2.getEndCalendar()));

		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(cutPeriod3.getStartCalendar()));
		assertEquals("2006/04/01",ISDADateFormat.formatFixedLength(cutPeriod3.getEndCalendar()));

		assertEquals("2006/04/01",ISDADateFormat.formatFixedLength(cutPeriod4.getStartCalendar()));
		assertEquals("2006/07/01",ISDADateFormat.formatFixedLength(cutPeriod4.getEndCalendar()));

	}

	public void testOverlappingPeriods() throws ParseException {
		Calendar startDate1 = ISDADateFormat.parse("2006/01/01");
		Calendar endDate1 = ISDADateFormat.parse("2006/04/01");
		Calendar startDate2 = ISDADateFormat.parse("2006/03/01");
		Calendar endDate2 = ISDADateFormat.parse("2006/06/01");
		Calendar cutDate1 = ISDADateFormat.parse("2006/02/01");

		List<Calendar> cutDates = new ArrayList<Calendar>();

		cutDates.add(cutDate1);

		Period period1 = new Period(startDate1,endDate1);
		Period period2 = new Period(startDate2,endDate2);

		List<Period> periodsToCut = new ArrayList<Period>();
		periodsToCut.add(period1);
		periodsToCut.add(period2);

		ScheduleCutter<Period> scheduleCutter = new ScheduleCutter<Period>();


		try {
			List<Period> resultPeriods = scheduleCutter.cutScheduleByDates(periodsToCut,cutDates);

		} catch (ScheduleException e) {
			return;
		}

		fail("Failed to throw Schedule Exception.");
	}


}
