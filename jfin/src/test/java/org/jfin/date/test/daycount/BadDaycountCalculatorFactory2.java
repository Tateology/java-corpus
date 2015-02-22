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

import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.holiday.HolidayCalendar;

public class BadDaycountCalculatorFactory2 extends DaycountCalculatorFactory
{

	public BadDaycountCalculatorFactory2(String astring)
	{

	}

	@Override
	public DaycountCalculator getISDAActualActual()
	{
		return null;
	}

	@Override
	public DaycountCalculator getISMAActualActual()
	{
		return null;
	}

	@Override
	public DaycountCalculator getAFBActualActual()
	{
		return null;
	}

	@Override
	public DaycountCalculator getUS30360()
	{
		return null;
	}

	@Override
	public DaycountCalculator getEU30360()
	{
		return null;
	}

	@Override
	public DaycountCalculator getIT30360()
	{
		return null;
	}

	@Override
	public DaycountCalculator getActual360()
	{
		return null;
	}

	@Override
	public DaycountCalculator getActual365Fixed()
	{
		return null;
	}

	@Override
	public DaycountCalculator getActual366()
	{
		return null;
	}

	@Override
	public DaycountCalculator getBusiness252(HolidayCalendar holidayCalendar)
	{
		return null;
	}

	@Override
	public String[] getAvailableDaycountCalculators()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
