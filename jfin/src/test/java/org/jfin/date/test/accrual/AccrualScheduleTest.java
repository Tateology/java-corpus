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
import org.jfin.date.accrual.*;
import org.jfin.date.accrual.impl.FixedAccrualPeriod;
import org.jfin.date.accrual.impl.FlatNotionalSchedule;
import org.jfin.date.util.ISDADateFormat;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.common.Iso4217Currency;

import java.util.List;
import java.util.ArrayList;
import java.text.ParseException;

/**
 */
public class AccrualScheduleTest extends TestCase {
	public void testNetPayments() throws ParseException {
		List<Payment> payments = new ArrayList<Payment>();

		payments.add(new Payment(100, Iso4217Currency.EUR, ISDADateFormat.parse("2006/01/01")));
		payments.add(new Payment(150, Iso4217Currency.EUR, ISDADateFormat.parse("2006/01/01")));
		payments.add(new Payment(200, Iso4217Currency.EUR, ISDADateFormat.parse("2006/01/02")));
		payments.add(new Payment(300, Iso4217Currency.GBP, ISDADateFormat.parse("2006/01/02")));

		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());

		List<Payment> nettedPayments = accrualSchedule.netPayments(payments);

		assertEquals(3,nettedPayments.size());

		for(Payment payment: nettedPayments) {
			if(payment.getCurrency()==Iso4217Currency.EUR && ISDADateFormat.formatFixedLength(payment.getPaymentCalendar()).equals("2006/01/01")) {
				assertEquals(250d,payment.getAmount());
			} else if(payment.getCurrency()==Iso4217Currency.GBP) {
				assertEquals(300d,payment.getAmount());
			} else {
				assertEquals(200d,payment.getAmount());
			}
		}
	}

	public void testGetPayments() throws Exception {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());

		DaycountCalculator daycountCalculator = DaycountCalculatorFactory.newInstance().getEU30360();

		FixedAccrualPeriod accrualPeriod = new FixedAccrualPeriod();
		accrualPeriod.setStartCalendar(ISDADateFormat.parse("2006/01/01"));
		accrualPeriod.setEndCalendar(ISDADateFormat.parse("2006/04/01"));
		accrualPeriod.setPaymentType(PaymentType.IN_ADVANCE);
		accrualPeriod.setPaymentBusinessDayConvention(BusinessDayConvention.UNADJUSTED);
		accrualPeriod.setAccrualBusinessDayConvention(BusinessDayConvention.UNADJUSTED);
		accrualPeriod.setDaycountCalculator(daycountCalculator);
		accrualPeriod.setFixedRate(0.1d);
		accrualPeriod.initialise();

		accrualSchedule.add(accrualPeriod);

		FlatNotionalSchedule notionalSchedule = new FlatNotionalSchedule(100,Iso4217Currency.EUR);

		List<Payment> payments = accrualSchedule.getPayments(notionalSchedule);

		assertEquals(1,payments.size());

		Payment payment = payments.get(0);

		assertTrue(payment.getAmount()>2.3);
		assertTrue(payment.getAmount()<2.7);

		assertEquals(Iso4217Currency.EUR,payment.getCurrency());

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(payment.getPaymentCalendar()));

	}

	public void testGetStartCalendar1() {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());


		try {
			accrualSchedule.getStartCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Exception not thrown when getting start calendar from an empty accrual schedule");
	}

	public void testGetEndCalendar1() {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());


		try {
			accrualSchedule.getEndCalendar();
		} catch (AccrualException e) {
			return;
		}

		fail("Exception not thrown when getting end calendar from an empty accrual schedule");
	}

	public void testGetStartEndCalendar1() throws ParseException, AccrualException {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		accrualSchedule.add(new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/1/1"),ISDADateFormat.parse("2006/4/3"))));

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(accrualSchedule.getStartCalendar()));
		assertEquals("2006/04/03",ISDADateFormat.formatFixedLength(accrualSchedule.getEndCalendar()));
	}

	public void testGetStartEndCalendar2() throws ParseException, AccrualException {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		accrualSchedule.add(new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/1/1"),ISDADateFormat.parse("2006/4/3"))));
		accrualSchedule.add(new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/4/3"),ISDADateFormat.parse("2006/7/1"))));

		assertEquals("2006/01/01",ISDADateFormat.formatFixedLength(accrualSchedule.getStartCalendar()));
		assertEquals("2006/07/01",ISDADateFormat.formatFixedLength(accrualSchedule.getEndCalendar()));
	}

	public void testSize1() {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		assertEquals(0,accrualSchedule.size());
		assertEquals(0,accrualSchedule.getAccrualPeriods().size());
		assertTrue(accrualSchedule.isEmpty());
		assertFalse(accrualSchedule.iterator().hasNext());
	}

	public void testSize2() throws ParseException {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		accrualSchedule.add(new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/1/1"),ISDADateFormat.parse("2006/4/3"))));
		assertEquals(1,accrualSchedule.size());
		assertEquals(1,accrualSchedule.getAccrualPeriods().size());
		assertFalse(accrualSchedule.isEmpty());
		assertTrue(accrualSchedule.iterator().hasNext());
	}

	public void testContains1() throws ParseException {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		FixedAccrualPeriod period = new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/1/1"),ISDADateFormat.parse("2006/4/3")));

		assertFalse(accrualSchedule.contains(period));
	}

	public void testContains2() throws ParseException {
		AccrualSchedule accrualSchedule = new AccrualSchedule(new Payment());
		FixedAccrualPeriod period = new FixedAccrualPeriod(new Period(ISDADateFormat.parse("2006/1/1"),ISDADateFormat.parse("2006/4/3")));
		accrualSchedule.add(period);

		assertTrue(accrualSchedule.contains(period));
	}
}
