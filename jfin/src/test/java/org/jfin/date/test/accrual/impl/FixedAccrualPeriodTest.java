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

package org.jfin.date.test.accrual.impl;

import junit.framework.TestCase;
import org.jfin.date.accrual.impl.FixedAccrualPeriod;
import org.jfin.date.accrual.PaymentType;
import org.jfin.date.accrual.AccrualException;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.util.ISDADateFormat;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.holiday.HolidayCalendarFactory;

import java.text.ParseException;
import java.util.Calendar;

/**
 */
public class FixedAccrualPeriodTest extends TestCase {
	public void testClone() throws ParseException {
		FixedAccrualPeriod toCopy = new FixedAccrualPeriod();
		toCopy.setDaycountCalculator(DaycountCalculatorFactory.newInstance().getEU30360());
		toCopy.setEndCalendar(ISDADateFormat.parse("2006/06/01"));
		toCopy.setFixedRate(100);
		toCopy.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		toCopy.setPaymentHolidayCalendar(HolidayCalendarFactory.newInstance().getHolidayCalendar("WE"));
		toCopy.setPaymentType(PaymentType.IN_ADVANCE);
		toCopy.setReferenceEndCalendar(ISDADateFormat.parse("2006/06/01"));
		toCopy.setReferenceStartCalendar(ISDADateFormat.parse("2006/03/01"));
		toCopy.setStartCalendar(ISDADateFormat.parse("2006/03/01"));

		toCopy.initialise();

		FixedAccrualPeriod copy = toCopy.clone();

		assertEquals(toCopy.getDaycountCalculator(),copy.getDaycountCalculator());
		assertEquals(toCopy.getEndCalendar(),copy.getEndCalendar());
		assertEquals(toCopy.getFixedRate(),copy.getFixedRate());
		assertEquals(toCopy.getPaymentBusinessDayConvention(),copy.getPaymentBusinessDayConvention());
		assertEquals(toCopy.getPaymentHolidayCalendar(),copy.getPaymentHolidayCalendar());
		assertEquals(toCopy.getPaymentType(),copy.getPaymentType());
		assertEquals(toCopy.getReferenceEndCalendar(),copy.getReferenceEndCalendar());
		assertEquals(toCopy.getReferenceStartCalendar(),copy.getReferenceStartCalendar());
		assertEquals(toCopy.getStartCalendar(),copy.getStartCalendar());
		assertEquals(toCopy.getPaymentCalendar(),copy.getPaymentCalendar());
	}

	public void testInitialiseInAdvance() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setDaycountCalculator(DaycountCalculatorFactory.newInstance().getEU30360());
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/03/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/06/01"));
		accrualPeriod.setFixedRate(100);
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.UNADJUSTED);
		accrualPeriod.setPaymentType(PaymentType.IN_ADVANCE);

		accrualPeriod.initialise();

		assertEquals(PaymentType.IN_ADVANCE,accrualPeriod.getPaymentType());

		assertEquals("2006/03/01",ISDADateFormat.formatFixedLength(accrualPeriod.getPaymentCalendar()));
	}

	public void testInitialiseInArrears() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setDaycountCalculator(DaycountCalculatorFactory.newInstance().getEU30360());
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/03/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/06/01"));
		accrualPeriod.setFixedRate(100);
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.UNADJUSTED);
		accrualPeriod.setPaymentType(PaymentType.IN_ARREARS);

		accrualPeriod.initialise();

		assertEquals(PaymentType.IN_ARREARS,accrualPeriod.getPaymentType());

		assertEquals("2006/06/01",ISDADateFormat.formatFixedLength(accrualPeriod.getPaymentCalendar()));
	}

	public void testImproperBusinessDayConvention1() {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);

		try {
			accrualPeriod.getAdjustedStartCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}

	public void testImproperBusinessDayConvention2() {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);

		try {
			accrualPeriod.getAdjustedEndCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}

	public void testImproperBusinessDayConvention3() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/01/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/04/01"));

		try {
			accrualPeriod.getAdjustedStartCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}

	public void testImproperBusinessDayConvention4() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/01/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/04/01"));

		try {
			accrualPeriod.getAdjustedEndCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}


	public void testImproperBusinessDayConvention5() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(null);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/01/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/04/01"));

		try {
			accrualPeriod.getAdjustedStartCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}

	public void testImproperBusinessDayConvention6() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setPaymentBusinessDayConvention(null);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2007/01/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2007/04/01"));

		try {
			accrualPeriod.getAdjustedEndCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Did not throw exception when expected");
	}

	public void testProperBusinessDayConvention1() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setAccrualBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2007/01/13"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2007/01/28"));
		accrualPeriod.setAccrualHolidayCalendar(HolidayCalendarFactory.newInstance().getHolidayCalendar("WE"));

		try {
			Calendar adjustedStartCalendar = accrualPeriod.getAdjustedStartCalendar();
			assertEquals("2007/01/15",ISDADateFormat.formatFixedLength(adjustedStartCalendar));
		} catch (AccrualException e) {
			fail();
		}
	}

	public void testProperBusinessDayConvention2() throws ParseException {
		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setAccrualBusinessDayConvention(BusinessDayConvention.FOLLOWING);
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2007/01/13"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2007/01/28"));
		accrualPeriod.setAccrualHolidayCalendar(HolidayCalendarFactory.newInstance().getHolidayCalendar("WE"));

		try {
			Calendar adjustedEndCalendar = accrualPeriod.getAdjustedEndCalendar();
			assertEquals("2007/01/29",ISDADateFormat.formatFixedLength(adjustedEndCalendar));
		} catch (AccrualException e) {
			fail();
		}
	}
}
