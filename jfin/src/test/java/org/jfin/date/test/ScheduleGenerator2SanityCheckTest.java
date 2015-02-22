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

import org.jfin.date.*;
import org.jfin.date.util.ISDADateFormat;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Iterator;
import java.text.ParseException;

import junit.framework.TestCase;

/**
 *
 */
public class ScheduleGenerator2SanityCheckTest extends TestCase {

	private TypedScheduleGenerator<Period> scheduleGenerator2;


	protected void setUp() throws Exception {
		scheduleGenerator2 = new TypedScheduleGenerator<Period>(new Period());
	}

	public void testScheduleGenerationNoStub()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 0, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.NONE;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator it = periods.iterator();

		while (it.hasNext())
		{
			Period period = (Period) it.next();
			assertTrue(calculator.calculateDaycountFraction(period) == 0.25d);

		}
	}

	public void testScheduleGenerationNoStub2() throws ParseException {
		Calendar start = ISDADateFormat.parse("2006/3/23");
		Calendar end = ISDADateFormat.parse("2011/3/23");

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.NONE;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator it = periods.iterator();

		while (it.hasNext())
		{
			Period period = (Period) it.next();
			assertTrue(calculator.calculateDaycountFraction(period) == 0.25d);

		}
	}

	public void testScheduleGenerationShortFirstStub() throws ScheduleException {
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.SHORT_FIRST;

		List<Period> periods = scheduleGenerator2.generateSchedule(start, end, frequency, stubType);
		List<Period> periods2 = ScheduleGenerator.generateSchedule(start, end, frequency, stubType);

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator<Period> it = periods.iterator();

		Period shortFirstPeriod = it.next();
		assertTrue(calculator.calculateDaycountFraction(shortFirstPeriod) < 0.25d);

		while (it.hasNext())
		{
			Period period = it.next();
			assertEquals(0.25d,calculator.calculateDaycountFraction(period));

		}
	}

	public void testScheduleGenerationShortLastStub()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.SHORT_LAST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator it = periods.iterator();

		while (it.hasNext())
		{
			Period period = (Period) it.next();
			if (it.hasNext())
			{
				assertEquals(0.25d,calculator.calculateDaycountFraction(period));
			} else
			{
				assertTrue(calculator.calculateDaycountFraction(period) < 0.25d);
			}

		}
	}

	public void testScheduleGenerationInvalidStub()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.NONE;

		try
		{
			scheduleGenerator2.generateSchedule(start, end, frequency, stubType);
		} catch (ScheduleException e)
		{
			return;
		}

		fail("Incorrectly generated a schedule with stub type 'NONE' when it could not fit exactly within the start and end dates");
	}

	public void testScheduleGenerationLongFirstStub()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.LONG_FIRST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator it = periods.iterator();

		Period longFirstPeriod = (Period) it.next();

		assertTrue(calculator.calculateDaycountFraction(longFirstPeriod) > 0.25d);

		while (it.hasNext())
		{
			Period period = (Period) it.next();
			assertTrue(calculator.calculateDaycountFraction(period) == 0.25d);

		}
	}

	public void testScheduleGenerationLongLastStub()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.LONG_LAST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		DaycountCalculator calculator = DaycountCalculatorFactory.newInstance()
				.getISMAActualActual();

		Iterator it = periods.iterator();

		while (it.hasNext())
		{
			Period period = (Period) it.next();
			if (it.hasNext())
			{
				assertTrue(calculator.calculateDaycountFraction(period) == 0.25d);
			} else
			{
				assertTrue(calculator.calculateDaycountFraction(period) > 0.25d);
			}

		}
	}

	public void testScheduleGenerationShortFirstStubNotional()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.SHORT_FIRST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		Period firstPeriod = periods.get(0);

		assertEquals("2005/2/1", ISDADateFormat.format(firstPeriod
				.getReferenceEndCalendar()));
		assertEquals("2004/11/1", ISDADateFormat.format(firstPeriod
				.getReferenceStartCalendar()));
		assertEquals("2005/2/1", ISDADateFormat.format(firstPeriod
				.getEndCalendar()));
		assertEquals("2005/1/1", ISDADateFormat.format(firstPeriod
				.getStartCalendar()));
	}

	public void testScheduleGenerationLongFirstStubNotional()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.LONG_FIRST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		Period firstPeriod = periods.get(0);

		assertEquals("2005/2/1", ISDADateFormat.format(firstPeriod
				.getReferenceEndCalendar()));
		assertEquals("2004/11/1", ISDADateFormat.format(firstPeriod
				.getReferenceStartCalendar()));

		assertEquals("2005/1/1", ISDADateFormat.format(firstPeriod
				.getStartCalendar()));
		assertEquals("2005/5/1", ISDADateFormat.format(firstPeriod
				.getEndCalendar()));
	}

	public void testScheduleGenerationShortLastStubNotional()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.SHORT_LAST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		Period lastPeriod = periods.get(periods.size() - 1);

		assertEquals("2010/4/1", ISDADateFormat.format(lastPeriod
				.getReferenceEndCalendar()));
		assertEquals("2010/1/1", ISDADateFormat.format(lastPeriod
				.getReferenceStartCalendar()));
		assertEquals("2010/2/1", ISDADateFormat.format(lastPeriod
				.getEndCalendar()));
		assertEquals("2010/1/1", ISDADateFormat.format(lastPeriod
				.getStartCalendar()));
	}

	public void testScheduleGenerationShortLongStubNotional()
	{
		Calendar start = new GregorianCalendar(2005, 0, 1);
		Calendar end = new GregorianCalendar(2010, 1, 1);

		Frequency frequency = Frequency.QUARTERLY;

		StubType stubType = StubType.LONG_LAST;

		List<Period> periods = null;

		try
		{
			periods = scheduleGenerator2.generateSchedule(start, end, frequency,
					stubType);
		} catch (ScheduleException e)
		{
			fail(e.getMessage());
		}

		Period lastPeriod = periods.get(periods.size() - 1);

		assertEquals("2010/4/1", ISDADateFormat.format(lastPeriod
				.getReferenceEndCalendar()));
		assertEquals("2010/1/1", ISDADateFormat.format(lastPeriod
				.getReferenceStartCalendar()));
		assertEquals("2010/2/1", ISDADateFormat.format(lastPeriod
				.getEndCalendar()));
		assertEquals("2009/10/1", ISDADateFormat.format(lastPeriod
				.getStartCalendar()));
	}
}
