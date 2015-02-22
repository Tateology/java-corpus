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

import junit.framework.TestCase;
import java.util.Calendar;
import org.jfin.date.util.ISDADateFormat;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.BusinessDayConvention;

public class MonthEndReferenceOffsetTest extends TestCase {

	/*
	 * Based upon test data provided by scottj
	 *
	 * today	27-Sep-06	0
	 * Spot	29-Sep-06	2
	 *
	 * 1M	31-Oct-06	32
	 * 2M	30-Nov-06	62
	 * 3M	29-Dec-06	91
	 * 6M	30-Mar-07	182
	 * 9M	29-Jun-07	273
	 *
	 */
	private Calendar startCalendar = null;
	private HolidayCalendar wend = null;
	private BusinessDayConvention convention = null;

	protected void setUp() throws Exception {
		startCalendar = ISDADateFormat.parse("2006/9/27");

		HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory
				.newInstance();
		wend = holidayCalendarFactory.getHolidayCalendar("WE");

		convention = BusinessDayConvention.MONTH_END_REFERENCE;
	}

	public void testSpot() {
		String target = "2006/9/29";

		String adjusted = ISDADateFormat.format(wend.adjust(startCalendar,convention));

		assertEquals(target,adjusted);
	}

	public void test1M() {
		String target = "2006/10/31";
		String adjusted = ISDADateFormat.format(wend.advance(startCalendar,1,Calendar.MONTH,convention));

		assertEquals(target,adjusted);
	}

	public void test2M() {
		String target = "2006/11/30";
		String adjusted = ISDADateFormat.format(wend.advance(startCalendar,2,Calendar.MONTH,convention));

		assertEquals(target,adjusted);
	}

	public void test3M() {
		String target = "2006/12/29";
		String adjusted = ISDADateFormat.format(wend.advance(startCalendar,3,Calendar.MONTH,convention));

		assertEquals(target,adjusted);
	}

	public void test6M() {
		String target = "2007/3/30";
		String adjusted = ISDADateFormat.format(wend.advance(startCalendar,6,Calendar.MONTH,convention));

		assertEquals(target,adjusted);
	}

	public void test9M() {
		String target = "2007/6/29";
		String adjusted = ISDADateFormat.format(wend.advance(startCalendar,9,Calendar.MONTH,convention));

		assertEquals(target,adjusted);
	}
}
