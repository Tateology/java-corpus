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
import org.jfin.date.accrual.*;
import org.jfin.date.accrual.impl.FixedAccrualPeriod;
import org.jfin.date.accrual.impl.FloatAccrualPeriod;
import org.jfin.date.accrual.impl.FixingAdjustmentType;
import org.jfin.date.accrual.impl.FlatNotionalSchedule;
import org.jfin.date.*;
import org.jfin.date.util.ISDADateFormat;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.common.Iso4217Currency;

import java.util.Calendar;
import java.util.List;
import java.text.ParseException;
import java.text.NumberFormat;

/**
 *
 */
public class PlainSwapTest extends TestCase {
	private List<FixedAccrualPeriod> fixedAccrualPeriods;
	private List<FloatAccrualPeriod> floatAccrualPeriods;

	NotionalSchedule notionalSchedule;
	private AccrualSchedule<FixedAccrualPeriod, MockPayment> fixedSchedule;
	private AccrualSchedule<FloatAccrualPeriod, MockPayment> floatSchedule;

	private int significantDigits = 2;

	public void setUp() throws ParseException, ScheduleException {

		BusinessDayConvention paymentBusinessDayConvention = BusinessDayConvention.MODIFIED_FOLLOWING;
		BusinessDayConvention accrualBusinessDayConvention = BusinessDayConvention.MODIFIED_FOLLOWING;
		BusinessDayConvention fixingBusinessDayConvention = BusinessDayConvention.PRECEDING;

		HolidayCalendar holidayCalendar = HolidayCalendarFactory.newInstance().getHolidayCalendar("WE");
		DaycountCalculator fixedDaycountCalculator = DaycountCalculatorFactory.newInstance().getActual360();
		DaycountCalculator floatDaycountCalculator = DaycountCalculatorFactory.newInstance().getActual360();
		int fixingOffset = -2;
		FixingAdjustmentType fixingAdjustmentType = FixingAdjustmentType.BUSINESS_DAYS;
		Iso4217Currency currency = Iso4217Currency.EUR;
		double notional = 1000;
		double fixedRate = .04;
		double floatRate = .02;

		Calendar startDate = ISDADateFormat.parse("2007/1/16");
		String maturity = "10Y";

		StubType stubType = StubType.NONE;

		Frequency fixedFrequency = Frequency.SEMI_ANNUALLY;
		Frequency floatFrequency = Frequency.QUARTERLY;

		MockPayment paymentPrototype = new MockPayment();
		paymentPrototype.setTestField("TEST");

		fixedSchedule = new AccrualSchedule<FixedAccrualPeriod,MockPayment>(paymentPrototype);
		floatSchedule = new AccrualSchedule<FloatAccrualPeriod,MockPayment>(paymentPrototype);

		FixedAccrualPeriod fixedPeriodPrototype = new FixedAccrualPeriod();
		fixedPeriodPrototype.setDaycountCalculator(fixedDaycountCalculator);
		fixedPeriodPrototype.setFixedRate(fixedRate);
		fixedPeriodPrototype.setPaymentBusinessDayConvention(paymentBusinessDayConvention);
		fixedPeriodPrototype.setPaymentHolidayCalendar(holidayCalendar);
		fixedPeriodPrototype.setPaymentType(PaymentType.IN_ARREARS);
		fixedPeriodPrototype.setAccrualHolidayCalendar(holidayCalendar);
		fixedPeriodPrototype.setAccrualBusinessDayConvention(accrualBusinessDayConvention);

		FloatAccrualPeriod floatPeriodPrototype = new FloatAccrualPeriod();
		floatPeriodPrototype.setDaycountCalculator(floatDaycountCalculator);
		floatPeriodPrototype.setFixingBusinessDayConvention(fixingBusinessDayConvention);
		floatPeriodPrototype.setFixingCalendarOffsetAmount(fixingOffset);
		floatPeriodPrototype.setFixingAdjustmentType(fixingAdjustmentType);
		floatPeriodPrototype.setFixingHolidayCalendar(holidayCalendar);
		floatPeriodPrototype.setPaymentBusinessDayConvention(paymentBusinessDayConvention);
		floatPeriodPrototype.setPaymentHolidayCalendar(holidayCalendar);
		floatPeriodPrototype.setPaymentType(PaymentType.IN_ARREARS);
		floatPeriodPrototype.setAccrualHolidayCalendar(holidayCalendar);
		floatPeriodPrototype.setAccrualBusinessDayConvention(accrualBusinessDayConvention);
		floatPeriodPrototype.setFixingRate(floatRate);

		TypedScheduleGenerator<FixedAccrualPeriod> fixedScheduleGenerator = new TypedScheduleGenerator<FixedAccrualPeriod>(fixedPeriodPrototype);
		TypedScheduleGenerator<FloatAccrualPeriod> floatScheduleGenerator = new TypedScheduleGenerator<FloatAccrualPeriod>(floatPeriodPrototype);

		fixedAccrualPeriods = fixedScheduleGenerator.generateSchedule(startDate,maturity,fixedFrequency,stubType);
		floatAccrualPeriods = floatScheduleGenerator.generateSchedule(startDate,maturity,floatFrequency,stubType);

		fixedSchedule.setAccrualPeriods(fixedAccrualPeriods);
		floatSchedule.setAccrualPeriods(floatAccrualPeriods);

		notionalSchedule = new FlatNotionalSchedule(notional,currency);
	}

	public void testScheduleLengths() {
		assertEquals(20,fixedAccrualPeriods.size());
		assertEquals(40,floatAccrualPeriods.size());
	}

	public void testMockPaymentsInitialised() throws Exception {
		List<MockPayment> payments = fixedSchedule.getPayments(notionalSchedule);
		payments.addAll(floatSchedule.getPayments(notionalSchedule));

		for(MockPayment payment: payments) {
			assertEquals("TEST",payment.getTestField());
		}
	}

	double[] expectedFixedAmounts = new double[]
			{
					20.11,
					20.44,
					20.22,
					20.44,
					20.11,
					20.67,
					19.89,
					20.56,
					20.22,
					20.22,
					20.22,
					20.44,
					20.11,
					20.44,
					20.11,
					20.44,
					20.11,
					20.67,
					20.22,
					20.22
			};

	public void testFixedSchedulePayment() throws Exception {
		List<MockPayment> payments = fixedSchedule.getNettedPayments(notionalSchedule);

		for(int i=0;i<payments.size();i++) {
			MockPayment payment = payments.get(i);
			double expectedAmount = expectedFixedAmounts[i];
			double actualAmount = payment.getAmount();

			assertEquals("Payment for date "+ISDADateFormat.format(payment.getPaymentCalendar()), formatDouble(expectedAmount),formatDouble(actualAmount));

		}
	}

	public void testFormatDouble() {
		assertEquals("1.23",formatDouble(1.23));
		assertEquals("1.23",formatDouble(1.234));
		assertEquals("1.24",formatDouble(1.235));
	}

	private String formatDouble(double d1) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(significantDigits);

		return nf.format(d1);
	}

	private String[] paymentDates =
			{
					""
			};



}
