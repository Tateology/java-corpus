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

package org.jfin.date.test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.Period;

public class PeriodTest extends TestCase
{
	public void testDefaultConstructor()
	{
		Period period = new Period();
		assertNull(period.getStartCalendar());
		assertNull(period.getEndCalendar());
		assertNull(period.getReferenceStartCalendar());
		assertNull(period.getReferenceEndCalendar());
	}

	public void testSetters()
	{
		Calendar calendar1 = new GregorianCalendar();
		Calendar calendar2 = new GregorianCalendar();

		Period period = new Period(calendar1, calendar1);
		assertEquals(calendar1, period.getStartCalendar());
		assertEquals(calendar1, period.getEndCalendar());
		assertNull(period.getReferenceStartCalendar());
		assertNull(period.getReferenceEndCalendar());

		period.setStartCalendar(calendar2);
		assertEquals(calendar2, period.getStartCalendar());

		period.setEndCalendar(calendar2);
		assertEquals(calendar2, period.getEndCalendar());

		period.setReferenceStartCalendar(calendar1);
		assertEquals(calendar1, period.getReferenceStartCalendar());

		period.setReferenceEndCalendar(calendar1);
		assertEquals(calendar1, period.getReferenceEndCalendar());

		period.setReferenceStartCalendar(calendar2);
		assertEquals(calendar2, period.getReferenceStartCalendar());

		period.setReferenceEndCalendar(calendar2);
		assertEquals(calendar2, period.getReferenceEndCalendar());
	}

	public void testToString()
	{
		Calendar calendar1 = new GregorianCalendar(2005, 0, 1);
		Calendar calendar2 = new GregorianCalendar(2006, 11, 30);

		Period period = new Period(calendar1, calendar2);

		assertEquals(period.toString(), "2005/1/1 - 2006/12/30");
	}

	public void testComparator()
	{
		Calendar calendar1 = new GregorianCalendar(2005, 0, 1);
		Calendar calendar2 = new GregorianCalendar(2006, 11, 30);

		Period period1 = new Period(calendar1, calendar1);

		Period period2 = new Period(calendar2, calendar2);

		assertTrue(period1.compareTo(period1) == 0);
		assertTrue(period1.compareTo(period2) < 0);
		assertTrue(period2.compareTo(period1) > 0);
		assertTrue(period1.compareTo(calendar1) == 0);
		assertTrue(period1.compareTo(calendar2) < 0);
		assertTrue(period2.compareTo(calendar1) > 0);
	}

	public void testComparatorMidpoint()
	{
		Calendar calendar1 = new GregorianCalendar(2005, 0, 1);
		Calendar calendar2 = new GregorianCalendar(2005, 2, 1);
		Calendar calendar3 = new GregorianCalendar(2005, 3, 1);
		Period period1 = new Period(calendar1, calendar2);
		Period period2 = new Period(calendar1, calendar3);

		assertTrue(period1.compareTo(period2) < 0);
		assertTrue(period2.compareTo(period1) > 0);

		assertTrue(period2.compareTo(calendar2) < 0);
	}

	public void testComparatorFailure()
	{
		Calendar calendar1 = new GregorianCalendar(2005, 0, 1);
		Calendar calendar2 = new GregorianCalendar(2006, 11, 30);

		Period period = new Period(calendar1, calendar2);

		try
		{
			period.compareTo("NO COMPARISON");
		} catch (RuntimeException e)
		{
			return;
		}

		fail("Failed to throw an exception when comparing a Period to a String");

	}

	public void testConstructorWithReferencePeriod() {
		Calendar calendar1 = new GregorianCalendar(2005, 0, 1);
		Calendar calendar2 = new GregorianCalendar(2005, 2, 30);
		Calendar calendar3 = new GregorianCalendar(2005, 1, 1);
		Calendar calendar4 = new GregorianCalendar(2005, 3, 30);

		Period period = new Period(calendar1,calendar2,calendar3,calendar4);

		assertEquals("2005/1/1 - 2005/3/30[2005/2/1 - 2005/4/30]",period.toString());
	}
}
