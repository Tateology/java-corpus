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

import java.util.Calendar;

import org.jfin.date.BusinessDayConvention;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;

public class AdjustmentService
{
	public String[] getBusinessDayConventionNames() {
		BusinessDayConvention[] conventions = BusinessDayConvention.values();

		String[] names = new String[conventions.length];

		for(int i=0;i<conventions.length;i++) {
			names[i] = conventions[i].name();
		}

		return names;
	}

	public Calendar getAdjusted(Calendar date, String holidayCalendarName, String businessDayConventionName) {
		BusinessDayConvention convention = BusinessDayConvention.valueOf(businessDayConventionName);

		HolidayCalendar holidayCalendar = HolidayCalendarFactory.newInstance().getHolidayCalendar(holidayCalendarName);

		return holidayCalendar.adjust(date, convention);
	}
}
