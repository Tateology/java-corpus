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

import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.Period;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;

public class DaycountSanityCheckTest extends TestCase
{
	DaycountCalculatorFactory factory = DaycountCalculatorFactory.newInstance();

	HolidayCalendar holidayCalendar = HolidayCalendarFactory.newInstance()
			.getHolidayCalendar("WE");

	Period testPeriod1 = new Period(new GregorianCalendar(2005, 0, 1),
			new GregorianCalendar(2005, 3, 1));

	double minValue1 = 0.24d;

	double maxValue1 = 0.26d;

	Period testPeriod2 = new Period(new GregorianCalendar(2005, 3, 1),
			new GregorianCalendar(2005, 0, 1));

	double minValue2 = 0.24d;

	double maxValue2 = 0.26d;

	Period testPeriod3 = new Period(new GregorianCalendar(2005, 0, 1),
			new GregorianCalendar(2005, 0, 1));

	double minValue3 = 0.0d;

	double maxValue3 = 0.0d;

	public void testActual360()
	{
		check(factory.getActual360());
	}

	public void testActual365Fixed()
	{
		check(factory.getActual365Fixed());
	}

	public void testActual366()
	{
		check(factory.getActual366());
	}

	public void testAFBActualActual()
	{
		check(factory.getAFBActualActual());
	}

	public void testBusiness252()
	{
		check(factory.getBusiness252(holidayCalendar));
	}

	public void testEU30360()
	{
		check(factory.getEU30360());
	}

	public void testISDAActualActual()
	{
		check(factory.getISDAActualActual());
	}

	public void testISMActualActual()
	{
		check(factory.getISMAActualActual());
	}

	public void testIT30360()
	{
		check(factory.getIT30360());
	}

	public void testUS30360()
	{
		check(factory.getUS30360());
	}

	public void check(DaycountCalculator calculator)
	{

		assertTrue(daycountBetween(testPeriod1, calculator, minValue1,
				maxValue1));

		assertTrue(daycountBetween(testPeriod2, calculator, minValue2,
				maxValue2));

		assertTrue(daycountBetween(testPeriod3, calculator, minValue3,
				maxValue3));
	}

	public boolean daycountBetween(Period period,
			DaycountCalculator calculator, double min, double max)
	{
		double val = calculator.calculateDaycountFraction(period);
		return ( val >= min ) && ( val <= max );
	}
}
