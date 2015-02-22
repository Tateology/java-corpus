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

package org.jfin.date;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 * TODO: Use in interpolation of tenor lists
 *
 * @author dmb
 *
 */
public class Tenor
{
	private int amount;

	private int calendarUnit;

	private static String[] spotNodeSynonyms =
	{
			"SN", "S/N", "ON", "O/N"
	};

	private static String[] tomorrowSynonyms =
	{
			"TN", "T/N"
	};

	private static DecimalFormat dayFormat = new DecimalFormat("#D");

	private static DecimalFormat weekFormat = new DecimalFormat("#W");

	private static DecimalFormat monthFormat = new DecimalFormat("#M");

	private static DecimalFormat yearFormat = new DecimalFormat("#Y");

	/**
	 * Parse a String representation of a Money Market or Swap Tenor. TODO:
	 * Implement Futures nodes
	 *
	 * @param toParse
	 * @throws ParseException
	 */
	public Tenor(String toParse) throws ParseException
	{
		parse(toParse);
	}

	public void parse(String toParse) throws ParseException
	{
		// Deal with spot node synonyms
		if (arrayContainsString(spotNodeSynonyms, toParse))
		{
			amount = 0;
			calendarUnit = Calendar.DAY_OF_YEAR;
			return;
		}

		// Deal with tomorrow synonyms
		if (arrayContainsString(tomorrowSynonyms, toParse))
		{
			amount = 1;
			calendarUnit = Calendar.DAY_OF_YEAR;
			return;
		}

		toParse = toParse.toUpperCase();
		try
		{
			amount = dayFormat.parse(toParse).intValue();
			calendarUnit = Calendar.DAY_OF_YEAR;
			return;
		} catch (ParseException e)
		{
		}

		try
		{
			amount = weekFormat.parse(toParse).intValue();
			amount *= 7;
			calendarUnit = Calendar.DAY_OF_YEAR;
			return;
		} catch (ParseException e)
		{
		}

		try
		{
			amount = monthFormat.parse(toParse).intValue();
			calendarUnit = Calendar.MONTH;
			return;
		} catch (ParseException e)
		{
		}

		try
		{
			amount = yearFormat.parse(toParse).intValue();
			calendarUnit = Calendar.YEAR;
			return;
		} catch (ParseException e)
		{
		}

		throw new ParseException("Could not parse tenor " + toParse, 0);

	}

	private static boolean arrayContainsString(String[] array, String search)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(search))
				return true;
		}
		return false;
	}

	/**
	 * @param amount
	 * @param calendarUnit
	 */
	public Tenor(int amount, int calendarUnit)
	{
		setAmount(amount);
		setCalendarUnit(calendarUnit);
	}

	/**
	 * @return Returns the amount.
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * @param amount
	 *            The amount to set.
	 */
	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	/**
	 * @return Returns the calendarUnit, e.g. Calendar.YEAR.
	 */
	public int getCalendarUnit()
	{
		return calendarUnit;
	}

	/**
	 * @param calendarUnit
	 *            The calendarUnit to set, e.g. Calendar.YEAR.
	 */
	public void setCalendarUnit(int calendarUnit)
	{
		this.calendarUnit = calendarUnit;
	}

}
