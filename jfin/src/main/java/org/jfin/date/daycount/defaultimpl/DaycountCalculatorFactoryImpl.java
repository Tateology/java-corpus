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

package org.jfin.date.daycount.defaultimpl;

import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.holiday.HolidayCalendar;

/**
 * Default implementation of the DaycountCalculatorFactory.
 */
public class DaycountCalculatorFactoryImpl extends DaycountCalculatorFactory
{

	@Override
	public DaycountCalculator getISDAActualActual()
	{
		return new ISDAActualActual();
	}

	@Override
	public DaycountCalculator getISMAActualActual()
	{
		return new ISMAActualActual();
	}

	@Override
	public DaycountCalculator getAFBActualActual()
	{
		return new AFBActualActual();
	}

	@Override
	public DaycountCalculator getUS30360()
	{
		return new US30360();
	}

	@Override
	public DaycountCalculator getEU30360()
	{
		return new EU30360();
	}

	@Override
	public DaycountCalculator getIT30360()
	{
		return new IT30360();
	}

	@Override
	public DaycountCalculator getActual360()
	{
		return new Actual360();
	}

	@Override
	public DaycountCalculator getActual365Fixed()
	{
		return new Actual365Fixed();
	}

	@Override
	public DaycountCalculator getActual366()
	{
		return new Actual366();
	}

	@Override
	public DaycountCalculator getBusiness252(HolidayCalendar holidayCalendar)
	{
		return new Business252(holidayCalendar);
	}

	/* (non-Javadoc)
	 * @see org.jfin.date.daycount.DaycountCalculatorFactory#getAvailableDaycountCalculators()
	 */
	@Override
	public String[] getAvailableDaycountCalculators()
	{
		return new String[] {
				"ISDAActualActual",
				"ISMAActualActual",
				"AFBActualActual",
				"US30360",
				"EU30360",
				"IT30360",
				"Actual360",
				"Actual365Fixed",
				"Actual366"
		};
	}



}
