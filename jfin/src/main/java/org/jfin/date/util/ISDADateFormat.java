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

package org.jfin.date.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class for formatting Calendars using the standard ISDA date format.
 */
public class ISDADateFormat
{
	/**
	 * Efficient method to return a Calendar formatted as YYYY/M/D
	 *
	 * @param calendar
	 * @return A string representing the date in the calendar
	 */
	public static String format(Calendar calendar)
	{
		if (calendar == null)
			return null;
		return calendar.get(Calendar.YEAR) + "/"
				+ ( calendar.get(Calendar.MONTH) + 1 ) + "/"
				+ calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Efficient method to return a Calendar formatted as YYYY/MM/DD; will
	 * always return a 10 character String
	 *
	 * @param calendar
	 * @return A string representing the date in the calendar
	 */
	public static String formatFixedLength(Calendar calendar)
	{
		return formatFixedLength(calendar, "/");
	}

	/**
	 * Efficient method to return a Calendar formatted as YYYYMMDD; will always
	 * return an 8 character String
	 *
	 * @param calendar
	 * @return A string representing the date in the calendar
	 */
	public static String formatCompact(Calendar calendar)
	{
		return formatFixedLength(calendar, "");
	}

	/**
	 * Efficient method to return a Calendar formatted as
	 * YYYYdelimeterMMdelimeterDD
	 *
	 * @param calendar
	 * @return A string representing the date in the calendar
	 */
	public static String formatFixedLength(Calendar calendar, String delimeter)
	{
		if (calendar == null)
			return null;
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		return calendar.get(Calendar.YEAR) + delimeter
				+ ( month < 10 ? "0" : "" ) + month + delimeter
				+ ( day < 10 ? "0" : "" ) + day;
	}

	/**
	 * Parses a String of the format YYYY/MM/DD into a Calendar
	 *
	 * @param in
	 *            The String to parse
	 * @return The Calendar for the given string
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static Calendar parse(String in) throws ParseException
	{
		Calendar ret = null;
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

		Date d1 = df.parse(in);
		ret = new GregorianCalendar();
		ret.set(Calendar.YEAR, d1.getYear() + 1900);
		ret.set(Calendar.MONTH, d1.getMonth());
		ret.set(Calendar.DAY_OF_MONTH, d1.getDate());

		return ret;
	}
}
