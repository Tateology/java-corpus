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

package org.jfin.date.test.daycount;

import junit.framework.TestCase;

import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.daycount.DaycountException;
import org.jfin.date.daycount.defaultimpl.AFBActualActual;
import org.jfin.date.daycount.defaultimpl.Actual360;
import org.jfin.date.daycount.defaultimpl.Actual365Fixed;
import org.jfin.date.daycount.defaultimpl.Actual366;
import org.jfin.date.daycount.defaultimpl.EU30360;
import org.jfin.date.daycount.defaultimpl.ISDAActualActual;
import org.jfin.date.daycount.defaultimpl.ISMAActualActual;
import org.jfin.date.daycount.defaultimpl.IT30360;
import org.jfin.date.daycount.defaultimpl.US30360;

public class DaycountCalculatorFactoryTest extends TestCase
{
	/*
	 * Test method for
	 * 'org.jfin.date.test.holiday.HolidayCalendarFactory.getHolidayCalendar(String)'
	 */
	public void testGetDefaultDaycountCalculator() throws DaycountException
	{

		assertTrue(DaycountCalculatorFactory.newInstance().getActual360() != null);

		assertTrue(DaycountCalculatorFactory.newInstance()
				.getISDAActualActual() != null);

	}

	public void testGetSpecificDaycountCalculatorFactory()
	{
		System
				.setProperty("jfin.DaycountCalculatorFactory",
						"org.jfin.date.daycount.defaultimpl.DaycountCalculatorFactoryImpl");

		assertTrue(DaycountCalculatorFactory.newInstance() instanceof org.jfin.date.daycount.defaultimpl.DaycountCalculatorFactoryImpl);

		System.clearProperty("jfin.DaycountCalculatorFactory");
	}

	public void testDaycountCalculatorFactoryNotFound()
	{
		System.setProperty("jfin.DaycountCalculatorFactory", "does.not.exist");

		try
		{
			DaycountCalculatorFactory.newInstance();
		} catch (DaycountException e)
		{
			System.clearProperty("jfin.DaycountCalculatorFactory");
			return;
		}

		System.clearProperty("jfin.DaycountCalculatorFactory");
		fail("Found daycount calculator, even though a non existant one was specified in the jfin.DaycountCalculatorFactory property");
	}

	public void testDaycountCalculatorFactoryWrongClass()
	{
		System.setProperty("jfin.DaycountCalculatorFactory",
				"org.jfin.date.test.daycount.BadDaycountCalculatorFactory1");

		try
		{
			DaycountCalculatorFactory.newInstance();
		} catch (DaycountException e)
		{
			System.clearProperty("jfin.DaycountCalculatorFactory");
			return;
		}

		System.clearProperty("jfin.DaycountCalculatorFactory");
		fail("Found daycount calculator factory, even though it is not an instance of DaycountCalculatorFactory");
	}

	public void testDaycountCalculatorFactoryNoDefaultConstructor()
	{
		System.setProperty("jfin.DaycountCalculatorFactory",
				"org.jfin.date.test.daycount.BadDaycountCalculatorFactory2");

		try
		{
			DaycountCalculatorFactory.newInstance();
		} catch (DaycountException e)
		{
			System.clearProperty("jfin.DaycountCalculatorFactory");
			return;
		}

		System.clearProperty("jfin.DaycountCalculatorFactory");
		fail("Found daycount calculator factory, even though it does not have a default constructor");
	}

	public void testDaycountCalculatorFactoryPrivateConstructor()
	{
		System.setProperty("jfin.DaycountCalculatorFactory",
				"org.jfin.date.test.daycount.BadDaycountCalculatorFactory3");

		try
		{
			DaycountCalculatorFactory.newInstance();
		} catch (DaycountException e)
		{
			System.clearProperty("jfin.DaycountCalculatorFactory");
			return;
		}

		System.clearProperty("jfin.DaycountCalculatorFactory");
		fail("Found daycount calculator factory, even though its default constructor is private");
	}

	public void testGetDaycountByName() {

		DaycountCalculatorFactory defaultFactory = DaycountCalculatorFactory.newInstance();

		assertTrue(defaultFactory.getDaycountCalculator("ISDAActualActual") instanceof ISDAActualActual);

		assertTrue(defaultFactory.getDaycountCalculator("ISMAActualActual") instanceof ISMAActualActual);

		assertTrue(defaultFactory.getDaycountCalculator("AFBActualActual") instanceof AFBActualActual);

		assertTrue(defaultFactory.getDaycountCalculator("US30360") instanceof US30360);

		assertTrue(defaultFactory.getDaycountCalculator("EU30360") instanceof EU30360);

		assertTrue(defaultFactory.getDaycountCalculator("IT30360") instanceof IT30360);

		assertTrue(defaultFactory.getDaycountCalculator("Actual360") instanceof Actual360);

		assertTrue(defaultFactory.getDaycountCalculator("Actual365Fixed") instanceof Actual365Fixed);

		assertTrue(defaultFactory.getDaycountCalculator("Actual366") instanceof Actual366);

	}

	public void testUnknownDaycountCalculator() {
		DaycountCalculatorFactory defaultFactory = DaycountCalculatorFactory.newInstance();

		try {
			defaultFactory.getDaycountCalculator("DOES NOT EXIST");
		} catch(DaycountException e) {
			return;
		}

		fail("Failed to throw daycount exception for unknown daycount calculator.");
	}
}
