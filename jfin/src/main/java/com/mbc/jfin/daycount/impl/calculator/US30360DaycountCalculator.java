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

package com.mbc.jfin.daycount.impl.calculator;

import com.mbc.jfin.daycount.impl.DaycountCalculator;
import com.mbc.jfin.holiday.HolidayCalendar;
import com.mbc.jfin.schedule.SchedulePeriod;
import com.mbc.jfin.util.DateUtils;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 08:58
 */
public class US30360DaycountCalculator implements DaycountCalculator {

    public double calculateDaycountFraction(SchedulePeriod period, HolidayCalendar calendar) {

        if(period.getStart().equals(period.getEnd())) return 0;

        int dayOfMonth1 = period.getStart().getDayOfMonth();
		int dayOfMonth2 = period.getEnd().getDayOfMonth();

		int month1 = period.getStart().getMonthOfYear();
		int month2 = period.getEnd().getMonthOfYear();

		int year1 = period.getStart().getYear();
		int year2 = period.getEnd().getYear();

		boolean isLastDayOfFebruary1 = DateUtils.isLastDayOfMonth(period.getStart()) && month1 == 2;
		boolean isLastDayOfFebruary2 = DateUtils.isLastDayOfMonth(period.getEnd()) && month2 == 2;

		if(isLastDayOfFebruary1 && isLastDayOfFebruary2) {
			dayOfMonth2 = 30;
		}

		if(isLastDayOfFebruary1) {
			dayOfMonth1 = 30;
		}

		if(dayOfMonth2==31 && dayOfMonth1>=30) {
			dayOfMonth2 = 30;
		}

		if(dayOfMonth1==31) {
			dayOfMonth1 = 30;
		}

		int numerator = 360*( year2 - year1 );
		numerator+= 30 * ( month2 - month1);
		numerator+= dayOfMonth2 - dayOfMonth1;

		return numerator/360d;
    }
}
