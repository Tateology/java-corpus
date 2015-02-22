/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p>Copyright (C) 2005-2011 Morgan Brown Consultancy Ltd. </p>
 *
 * <p>This file is part of jFin. </p>
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

package com.mbc.jfin.holiday.impl.financialcalendar;

import com.mbc.jfin.holiday.HolidayCalendar;
import com.mbc.jfin.holiday.HolidayService;
import junit.framework.TestCase;
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 07/05/2011
 * Time: 14:56
 */
public class FinancialCalendarHolidayServiceImplTest extends TestCase {
    HolidayService service;

    @Override
    protected void setUp() throws Exception {
        service = new FinancialCalendarHolidayServiceImpl(
                new HolidayFile(
                        "src/test/data/financialcalendar/jfin.csv",
                        "src/test/data/financialcalendar/info.csv"
                )
        );
    }

    public void testMultipleCalendars() {
        HolidayCalendar holidayCalendar =
                service.getHolidayCalendar(new String[]{"LnB", "TkB"});

		assertTrue(holidayCalendar
				.isHoliday(new LocalDate(2006, 1, 30)));
		assertFalse(holidayCalendar
				.isWeekend(new LocalDate(2006, 1, 30)));
		assertFalse(holidayCalendar
				.isHoliday(new LocalDate(2006, 1, 15)));
		assertTrue(holidayCalendar
				.isWeekend(new LocalDate(2006, 1, 15)));
		assertTrue(holidayCalendar
				.isHoliday(new LocalDate(2006, 2, 21)));
    }
}
