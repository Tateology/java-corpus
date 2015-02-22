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

package org.jfin.date.test.accrual;

import junit.framework.TestCase;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.accrual.AccrualException;
import org.jfin.date.accrual.AccrualPeriod;
import org.jfin.date.accrual.impl.FixedAccrualPeriod;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.util.ISDADateFormat;

import java.text.ParseException;
import java.util.Calendar;

public class AccrualPeriodTest extends TestCase {

	public void testCantGetAdjustedPaymentCalendar1() {
		AccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);

		try {
			Calendar cal = accrualPeriod.getAdjustedPaymentCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Didn't return an exception trying to modify a null payment calendar with FOLLOWING");
	}

	public void testCantGetAdjustedPaymentCalendar2() {
		AccrualPeriod accrualPeriod = new FixedAccrualPeriod();

		try {
			Calendar cal = accrualPeriod.getAdjustedPaymentCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Didn't return an exception trying to modify a null payment calendar with null");
	}

	public void testCantGetAdjustedPaymentCalendar3() throws ParseException {
		AccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setPaymentCalendar(ISDADateFormat.parse("2007/10/10"));

		try {
			Calendar cal = accrualPeriod.getAdjustedPaymentCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Didn't return an exception trying to modify a 2007/10/10 payment calendar with FOLLOWING and no holiday calendar");
	}

	public void testGetUnadjustedPaymentCalendar() throws ParseException, AccrualException {
		AccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.UNADJUSTED);
		accrualPeriod.setPaymentCalendar(ISDADateFormat.parse("2007/10/10"));

		assertEquals("2007/10/10",ISDADateFormat.format(accrualPeriod.getAdjustedPaymentCalendar()));
	}

	public void testGetAdjustedPaymentCalendar() throws ParseException, AccrualException {
		AccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setPaymentCalendar(ISDADateFormat.parse("2007/11/10"));
		HolidayCalendar holidayCalendar = HolidayCalendarFactory.newInstance().getHolidayCalendar("WE");
		accrualPeriod.setPaymentHolidayCalendar(holidayCalendar);


		assertEquals("2007/11/12",ISDADateFormat.format(accrualPeriod.getAdjustedPaymentCalendar()));
	}

}
