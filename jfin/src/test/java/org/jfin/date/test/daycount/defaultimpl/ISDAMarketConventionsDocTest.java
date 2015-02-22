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

package org.jfin.date.test.daycount.defaultimpl;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;

/**
 * Slew of tests based upon the ISDA EMU Market conventions document
 * http://www.isda.org/c_and_a/pdf/mktc1198.pdf
 *
 * @author dmb
 *
 */
public class ISDAMarketConventionsDocTest extends TestCase
{
	private double notional = 10000;

	private double fixedRate = 0.1;

	private NumberFormat nf = null;

	private DaycountCalculator isdaActAct = DaycountCalculatorFactory
			.newInstance().getISDAActualActual();

	private DaycountCalculator afbActAct = DaycountCalculatorFactory
			.newInstance().getAFBActualActual();

	private DaycountCalculator ismaActAct = DaycountCalculatorFactory
			.newInstance().getISMAActualActual();

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	}

	public void testPage3()
	{
		Calendar date1 = new GregorianCalendar(2003, Calendar.NOVEMBER, 1);
		Calendar date2 = new GregorianCalendar(2004, Calendar.MAY, 1);

		assertEquals("497.72", getAmount(isdaActAct, date1, date2));

		assertEquals("500.00", getAmount(ismaActAct, date1, date2));

		assertEquals("497.27", getAmount(afbActAct, date1, date2));
	}

	public void testPage4_5()
	{
		Calendar notionalStartDate = new GregorianCalendar(1998, Calendar.JULY,
				1);
		Calendar date1 = new GregorianCalendar(1999, Calendar.FEBRUARY, 1);
		Calendar date2 = new GregorianCalendar(1999, Calendar.JULY, 1);
		Calendar date3 = new GregorianCalendar(2000, Calendar.JULY, 1);

		assertEquals("410.96", getAmount(isdaActAct, date1, date2,
				notionalStartDate, date2));

		assertEquals("410.96", getAmount(ismaActAct, date1, date2,
				notionalStartDate, date2));

		assertEquals("410.96", getAmount(afbActAct, date1, date2,
				notionalStartDate, date2));

		assertEquals("1,001.38",
				getAmount(isdaActAct, date2, date3));

		assertEquals("1,000.00", getAmount(ismaActAct, date2, date3));

		assertEquals("1,000.00", getAmount(afbActAct, date2, date3));
	}

	public void testPage6()
	{
		Calendar notionalStartDate = new GregorianCalendar(2002, Calendar.JULY,
				15);
		Calendar notionalEndDate = new GregorianCalendar(2003,
				Calendar.JANUARY, 15);
		Calendar date1 = new GregorianCalendar(2002, Calendar.AUGUST, 15);
		Calendar date2 = new GregorianCalendar(2003, Calendar.JULY, 15);
		Calendar date3 = new GregorianCalendar(2004, Calendar.JANUARY, 15);

		assertEquals("915.07", getAmount(isdaActAct, date1, date2,
				notionalStartDate, notionalEndDate));

		assertEquals("915.76", getAmount(ismaActAct, date1, date2,
				notionalStartDate, notionalEndDate));

		assertEquals("915.07", getAmount(afbActAct, date1, date2,
				notionalStartDate, notionalEndDate));

		assertEquals("504.00", // 504.11 In ISDA Docs, but part falls in a leap
				// year are the docs incorrect!
				getAmount(isdaActAct, date2, date3));

		assertEquals("500.00", getAmount(ismaActAct, date2, date3));

		assertEquals("504.11", getAmount(afbActAct, date2, date3));

	}

	public void testPage7()
	{
		Calendar date1 = new GregorianCalendar(1999, Calendar.JULY, 30);
		Calendar date2 = new GregorianCalendar(2000, Calendar.JANUARY, 30);
		Calendar date3 = new GregorianCalendar(2000, Calendar.JUNE, 30);
		Calendar notionalEndDate = new GregorianCalendar(2000, Calendar.JULY,
				30);

		assertEquals("503.89", getAmount(isdaActAct, date1, date2));

		assertEquals("500.00", getAmount(ismaActAct, date1, date2));

		assertEquals("504.11", getAmount(afbActAct, date1, date2));

		assertEquals("415.30", getAmount(isdaActAct, date2, date3, date2,
				notionalEndDate));

		assertEquals("417.58", getAmount(ismaActAct, date2, date3, date2,
				notionalEndDate));

		assertEquals("415.30", getAmount(afbActAct, date2, date3, date2,
				notionalEndDate));
	}

	public void testPage8_9()
	{
		Calendar date1 = new GregorianCalendar(1999, Calendar.NOVEMBER, 30);
		Calendar date2 = new GregorianCalendar(2000, Calendar.APRIL, 30);
		Calendar notionalStartDate = new GregorianCalendar(2000,
				Calendar.FEBRUARY, 29);
		Calendar notionalEndDate = new GregorianCalendar(2000, Calendar.MAY, 31);

		assertEquals("415.54",
				getAmount(isdaActAct, date1, date2, notionalStartDate,
						notionalEndDate));

		assertEquals("415.76",
				getAmount(ismaActAct, date1, date2, notionalStartDate,
						notionalEndDate));

		assertEquals("415.30", getAmount(afbActAct, date1, date2,
				notionalStartDate, notionalEndDate));

	}

	public String getAmount(DaycountCalculator calculator, Calendar date1,
			Calendar date2)
	{
		return getAmount(calculator, date1, date2, null, null);
	}

	public String getAmount(DaycountCalculator calculator, Calendar date1,
			Calendar date2, Calendar period1, Calendar period2)
	{
		double daycount = calculator.calculateDaycountFraction(date1, date2,
				period1, period2);

		double amount = notional * fixedRate * daycount;

		return nf.format(amount);
	}
}
