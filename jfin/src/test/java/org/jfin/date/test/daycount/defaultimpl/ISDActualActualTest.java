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
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import org.jfin.date.Period;
import org.jfin.date.daycount.defaultimpl.ISDAActualActual;
import org.jfin.date.daycount.defaultimpl.ISMAActualActual;
import org.jfin.date.util.ISDADateFormat;

public class ISDActualActualTest extends TestCase
{
	public void testGetPeriods() throws ParseException {
		ISDAActualActual isda = new ISDAActualActual();

		Calendar start = ISDADateFormat.parse("2005/10/5");
		Calendar end = ISDADateFormat.parse("2007/3/10");

		List<Period> periods = isda.getPeriods(start,end);

		assertEquals(3,periods.size());

		Period period1 = periods.get(0);
		Period period2 = periods.get(1);
		Period period3 = periods.get(2);

		assertEquals("2005/10/5",ISDADateFormat.format(period1.getStartCalendar()));
		assertEquals("2006/1/1",ISDADateFormat.format(period1.getEndCalendar()));

		assertEquals("2006/1/1",ISDADateFormat.format(period2.getStartCalendar()));
		assertEquals("2007/1/1",ISDADateFormat.format(period2.getEndCalendar()));

		assertEquals("2007/1/1",ISDADateFormat.format(period3.getStartCalendar()));
		assertEquals("2007/3/10",ISDADateFormat.format(period3.getEndCalendar()));

	}

	public void testNumerators() throws ParseException {
		ISDAActualActual isda = new ISDAActualActual();
		Calendar start = ISDADateFormat.parse("2003/11/1");
		Calendar end = ISDADateFormat.parse("2004/5/1");
		int[] numerators = isda.getNumerators(isda.getPeriods(start,end));

		assertEquals(61,numerators[0]);
		assertEquals(121,numerators[1]);
	}

	public void testDenominators() throws ParseException {
		ISDAActualActual isda = new ISDAActualActual();
		Calendar start = ISDADateFormat.parse("2003/11/1");
		Calendar end = ISDADateFormat.parse("2004/5/1");
		int[] denominators = isda.getDenominators(isda.getPeriods(start,end));

		assertEquals(365,denominators[0]);
		assertEquals(366,denominators[1]);
	}

	public void testAmounts() throws ParseException {
		ISDAActualActual isda = new ISDAActualActual();
		Calendar start = ISDADateFormat.parse("2003/11/1");
		Calendar end = ISDADateFormat.parse("2004/5/1");

		double fraction = isda.calculateDaycountFraction(start,end);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		assertEquals("497.72",nf.format(fraction*1000));
	}
}
