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

import java.text.ParseException;
import java.util.Calendar;

import junit.framework.TestCase;

import org.jfin.date.Tenor;

public class TenorTest extends TestCase
{
	public void testTenor() throws ParseException
	{
		assertTenor("SN", 0, Calendar.DAY_OF_YEAR);
		assertTenor("S/N", 0, Calendar.DAY_OF_YEAR);
		assertTenor("ON", 0, Calendar.DAY_OF_YEAR);
		assertTenor("O/N", 0, Calendar.DAY_OF_YEAR);
		assertTenor("TN", 1, Calendar.DAY_OF_YEAR);
		assertTenor("T/N", 1, Calendar.DAY_OF_YEAR);
		assertTenor("3D", 3, Calendar.DAY_OF_YEAR);
		assertTenor("10D", 10, Calendar.DAY_OF_YEAR);
		assertTenor("1W", 7, Calendar.DAY_OF_YEAR);
		assertTenor("2W", 14, Calendar.DAY_OF_YEAR);
		assertTenor("20W", 140, Calendar.DAY_OF_YEAR);
		assertTenor("1M", 1, Calendar.MONTH);
		assertTenor("2M", 2, Calendar.MONTH);
		assertTenor("20M", 20, Calendar.MONTH);
		assertTenor("1Y", 1, Calendar.YEAR);
		assertTenor("2Y", 2, Calendar.YEAR);
		assertTenor("20Y", 20, Calendar.YEAR);
	}

	public void testBadTenor()
	{
		Tenor tenor = null;

		try
		{
			tenor = new Tenor("BAD");
		} catch (ParseException e)
		{
			assertNull(tenor);
			return;
		}

		fail("Expected Tenor to throw exception parsing a bad String");
	}

	public void testConstructor()
	{
		Tenor tenor = new Tenor(5, Calendar.YEAR);

		assertEquals(tenor.getAmount(), 5);
		assertEquals(tenor.getCalendarUnit(), Calendar.YEAR);
	}

	public void assertTenor(String toParse, int periodAmount, int periodUnit)
			throws ParseException
	{
		Tenor tenor = new Tenor(toParse);
		assertEquals(tenor.getAmount(), periodAmount);
		assertEquals(tenor.getCalendarUnit(), periodUnit);
	}
}
