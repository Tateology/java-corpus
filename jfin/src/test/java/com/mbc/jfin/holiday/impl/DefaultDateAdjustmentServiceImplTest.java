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

package com.mbc.jfin.holiday.impl;

import com.mbc.jfin.holiday.BusinessDayConvention;
import com.mbc.jfin.holiday.DateAdjustmentService;
import com.mbc.jfin.holiday.HolidayCalendar;
import junit.framework.TestCase;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 20:15
 */
public class DefaultDateAdjustmentServiceImplTest extends TestCase {

    DateAdjustmentService service;
    HolidayCalendar weekendCalendar;
    HolidayCalendar holidayCalendar;

    @Override
    protected void setUp() throws Exception {
        service = new DefaultDateAdjustmentServiceImpl();

        weekendCalendar = new WeekendHolidayCalendar();

        holidayCalendar = new HolidayCalendar() {
            public boolean isHoliday(LocalDate date) {
                return date.getDayOfWeek()== DateTimeConstants.SATURDAY || date.getDayOfWeek()== DateTimeConstants.SUNDAY;
            }

            public boolean isWeekend(LocalDate date) {
                return false;
            }
        };
    }

    public void testFollowing() {
        assertEquals(new LocalDate(2005,12,29), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,28), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,3,3), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.FOLLOWING, weekendCalendar));

        assertEquals(new LocalDate(2005,12,29), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2008,2,28), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2008,3,3), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.FOLLOWING, holidayCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.FOLLOWING, holidayCalendar));
    }

    public void testModFollowing() {
        assertEquals(new LocalDate(2005,12,29), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,28), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2008,3,3), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,13), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.MODIFIED_FOLLOWING, weekendCalendar));
    }

    public void testPreceding() {
        assertEquals(new LocalDate(2005,12,29), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,28), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,10), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,10), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.PRECEDING, weekendCalendar));
    }

    public void testModPreceding() {
        assertEquals(new LocalDate(2005,12,29), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2006,1,2), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,28), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2008,3,3), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,10), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
        assertEquals(new LocalDate(2006,2,10), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.MODIFIED_PRECEDING, weekendCalendar));
    }

    public void testMonthEndReference() {
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2005,12,29), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2005,12,30), service.adjust(new LocalDate(2005,12,31), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2006,1,31), service.adjust(new LocalDate(2006,1,1), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,28), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2008,2,29), service.adjust(new LocalDate(2008,2,29), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2008,3,31), service.adjust(new LocalDate(2008,3,1), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2006,2,28), service.adjust(new LocalDate(2006,2,11), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
        assertEquals(new LocalDate(2006,2,28), service.adjust(new LocalDate(2006,2,12), BusinessDayConvention.MONTH_END_REFERENCE, weekendCalendar));
    }
}
