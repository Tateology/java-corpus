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
import com.mbc.jfin.util.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 18:44
 */
public class DefaultDateAdjustmentServiceImpl implements DateAdjustmentService {
    public LocalDate adjust(LocalDate calendar, BusinessDayConvention businessDayConvention, HolidayCalendar holidayCalendar) {


        switch (businessDayConvention) {
            case UNADJUSTED:
                return calendar;
            case FOLLOWING:
                return following(calendar,holidayCalendar);
            case MODIFIED_FOLLOWING:
                return modFollowing(calendar,holidayCalendar);
            case PRECEDING:
                return preceding(calendar,holidayCalendar);
            case MODIFIED_PRECEDING:
                return modPreceding(calendar,holidayCalendar);
            case MONTH_END_REFERENCE:
                return monthEndReference(calendar,holidayCalendar);
            default:
                throw new UnsupportedBusinessDayConventionException(businessDayConvention);
        }


    }

    private LocalDate monthEndReference(LocalDate calendar, HolidayCalendar holidayCalendar) {
        LocalDate d1 = new LocalDate(calendar.getYear(), calendar.getMonthOfYear(), DateUtils.getDaysInMonth(calendar));

		return preceding(d1, holidayCalendar);
    }

    public LocalDate following(LocalDate calendar, HolidayCalendar holidayCalendar) {
        LocalDate d1 = calendar;
        while (holidayCalendar.isHoliday(d1) || holidayCalendar.isWeekend(d1))
        {
            d1 = d1.plus(Days.ONE);
        }
        return d1;
    }

    public LocalDate modFollowing(LocalDate calendar, HolidayCalendar holidayCalendar) {
        LocalDate d1 = calendar;
        while (holidayCalendar.isHoliday(d1) || holidayCalendar.isWeekend(d1))
        {
            d1 = d1.plus(Days.ONE);
        }

        if (d1.getMonthOfYear() != calendar.getMonthOfYear())
        {
            return preceding(calendar, holidayCalendar);
        }

        return d1;
    }

    public LocalDate preceding(LocalDate calendar, HolidayCalendar holidayCalendar) {
        LocalDate d1 = calendar;
        while (holidayCalendar.isHoliday(d1) || holidayCalendar.isWeekend(d1))
        {
            d1 = d1.minus(Days.ONE);
        }
        return d1;
    }

    public LocalDate modPreceding(LocalDate calendar, HolidayCalendar holidayCalendar) {
        LocalDate d1 = calendar;
        while (holidayCalendar.isHoliday(d1) || holidayCalendar.isWeekend(d1))
        {
            d1 = d1.minus(Days.ONE);
        }

        if (d1.getMonthOfYear() != calendar.getMonthOfYear())
        {
            return following(calendar, holidayCalendar);
        }

        return d1;
    }


    private class UnsupportedBusinessDayConventionException extends RuntimeException {
        public UnsupportedBusinessDayConventionException(BusinessDayConvention businessDayConvention) {
            super("Unsupported business day convention: "+businessDayConvention);
        }
    }
}
