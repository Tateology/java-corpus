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

package org.jfin.date.holiday.financialcalendarimpl;

import java.util.Calendar;
import java.util.logging.Logger;

import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.util.ISDADateFormat;

public class FinancialCalendarHolidayCalendar<T> extends HolidayCalendar
{
	private HolidayFile holidayFile;

	private String locale;

	private static Logger logger = Logger
			.getLogger("org.jfin.date.holiday.financialcalendarimpl.FinancialCalendarHolidayCalendar");

	/**
	 * @param holidayFile
	 * @param locale
	 */
	public FinancialCalendarHolidayCalendar(HolidayFile holidayFile,
			String locale)
	{
		this.holidayFile = holidayFile;
		this.locale = locale;
	}

	public boolean isWeekend(Calendar d)
	{
		String reference = HolidayFileEntry.getReference(ISDADateFormat
				.formatCompact(d), locale);

		HolidayFileEntry entry = holidayFile.getEntryByReference(reference);

		if (entry == null)
		{
			logger.fine("Found no weekend for reference " + reference);
			return false;
		}

		if (entry.getDescription().equals("Weekend"))
		{
			logger.fine("Found reference " + reference + " is a weekend");
			return true;
		} else
		{
			logger.fine("Found reference " + reference
					+ " is not a weekend, \"" + entry.getDescription() + "\"");
			return false;
		}
	}

	@Override
	public boolean isHoliday(Calendar d)
	{
		String reference = HolidayFileEntry.getReference(ISDADateFormat
				.formatCompact(d), locale);

		HolidayFileEntry entry = holidayFile.getEntryByReference(reference);

		if (entry == null)
		{
			logger.fine("Found no reference " + reference);
			return false;
		} else
		{
			logger.fine("Found reference " + reference + " is a holiday, \""
					+ entry.getDescription() + "\"");
			return true;
		}
	}

	/**
	 * @return Returns the holidayFile.
	 */
	public HolidayFile getHolidayFile()
	{
		return holidayFile;
	}

	/**
	 * @param holidayFile
	 *            The holidayFile to set.
	 */
	public void setHolidayFile(HolidayFile holidayFile)
	{
		this.holidayFile = holidayFile;
	}

	/**
	 * @return Returns the locale.
	 */
	public String getLocale()
	{
		return locale;
	}

	/**
	 * @param locale
	 *            The locale to set.
	 */
	public void setLocale(String locale)
	{
		this.locale = locale;
	}

}
