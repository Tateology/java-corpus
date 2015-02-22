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

import junit.framework.TestCase;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.util.ISDADateFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HolidayCalendarTest extends TestCase {
	public void testAdjustPeriodList() throws ParseException {
		HolidayCalendar<Period> holidayCalendar = HolidayCalendarFactory.newInstance().getHolidayCalendar("WE", Period.class);

		List<Period> periods = new ArrayList<Period>();

		Period period1 = new Period();
		period1.setStartCalendar(ISDADateFormat.parse("2007/11/10"));
		period1.setEndCalendar(ISDADateFormat.parse("2007/11/16"));

		Period period2 = new Period();
		period2.setStartCalendar(ISDADateFormat.parse("2007/11/16"));
		period2.setEndCalendar(ISDADateFormat.parse("2007/11/25"));

		periods.add(period1);
		periods.add(period2);

		List<Period> adjustedPeriods = holidayCalendar.adjust(periods, BusinessDayConvention.FOLLOWING);

		assertEquals(2,adjustedPeriods.size());

		Period adjustedPeriod1 = adjustedPeriods.get(0);
		Period adjustedPeriod2 = adjustedPeriods.get(1);

		assertEquals("2007/11/12", ISDADateFormat.format(adjustedPeriod1.getStartCalendar()));
		assertEquals("2007/11/16", ISDADateFormat.format(adjustedPeriod1.getEndCalendar()));

		assertEquals("2007/11/16", ISDADateFormat.format(adjustedPeriod2.getStartCalendar()));
		assertEquals("2007/11/26", ISDADateFormat.format(adjustedPeriod2.getEndCalendar()));
	}

	public void testIsLastDayOfMonth() throws ParseException {


		Calendar lastDay = ISDADateFormat.parse("2007/10/31");
		Calendar notLastDay = ISDADateFormat.parse("2008/2/28");

		assertTrue(HolidayCalendar.isLastDayOfMonth(lastDay));
		assertFalse(HolidayCalendar.isLastDayOfMonth(notLastDay));

	}
}
