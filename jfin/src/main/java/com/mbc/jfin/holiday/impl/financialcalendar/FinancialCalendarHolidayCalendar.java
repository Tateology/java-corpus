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
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 07/05/2011
 * Time: 13:45
 */
public class FinancialCalendarHolidayCalendar implements HolidayCalendar {

    private HolidaySource source;
    private String locale;

    public FinancialCalendarHolidayCalendar(HolidaySource source, String locale) {
        this.source = source;
        this.locale = locale;
    }

    public boolean isHoliday(LocalDate date) {
        return source.isHoliday(date,locale);
    }

    public boolean isWeekend(LocalDate date) {
        return source.isWeekend(date, locale);
    }
}
