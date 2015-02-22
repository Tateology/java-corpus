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

import java.io.File;
import java.io.IOException;

import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarException;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.Period;

public class HolidayCalendarFactoryFinancialCalendarImpl extends
		HolidayCalendarFactory
{
	HolidayFile holidayFile;

	InfoFile infoFile;

	public static String holidayCalendarHolidayFileParameter = "jfin.FinancialCalendar.HolidayFilePath";

	public static String holidayCalendarInfoFileParameter = "jfin.FinancialCalendar.InfoFilePath";

	/**
	 * Uses the system properties: jfin.FinancialCalendar.HolidayFilePath and
	 * fin.FinancialCalendar.InfoFilePath
	 *
	 * To load in the holiday list and info file from the provided paths.
	 *
	 */
	public HolidayCalendarFactoryFinancialCalendarImpl()
	{
		String holidayFilePath = System
				.getProperty("jfin.FinancialCalendar.HolidayFilePath");
		String infoFilePath = System
				.getProperty("jfin.FinancialCalendar.InfoFilePath");

		if (holidayFilePath == null)
		{
			throw new HolidayCalendarException(
					"System property jfin.FinancialCalendar.HolidayFilePath not set, cannot load HolidayCalendarFactoryFinancialCalendarImpl.");
		}

		if (infoFilePath == null)
		{
			throw new HolidayCalendarException(
					"System property jfin.FinancialCalendar.InfoFilePath not set, cannot load HolidayCalendarFactoryFinancialCalendarImpl.");
		}

		try
		{
			infoFile = new InfoFile(new File(infoFilePath));
			holidayFile = new HolidayFile(new File(holidayFilePath), infoFile);

		} catch (IOException e)
		{
			e.printStackTrace();
			throw new HolidayCalendarException(
					"IOException reading info file and holiday file, cannot load HolidayCalendarFactoryFinancialCalendarImpl.",
					e);
		}
	}


	public HolidayCalendar getHolidayCalendar(String locale)
	{
		return getHolidayCalendar(locale,Period.class);
	}

	public <T extends Period> HolidayCalendar<T> getHolidayCalendar(String locale, Class<T> c)
	{
		if (infoFile.getEntryByCode(locale) == null)
		{
			throw new HolidayCalendarException("Unknown locale \"" + locale
					+ "\"");
		} else
		{
			return new FinancialCalendarHolidayCalendar<T>(holidayFile, locale);
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
	 * @return Returns the infoFile.
	 */
	public InfoFile getInfoFile()
	{
		return infoFile;
	}

	/**
	 * @param infoFile
	 *            The infoFile to set.
	 */
	public void setInfoFile(InfoFile infoFile)
	{
		this.infoFile = infoFile;
	}


	public String[] getAvailableLocales() {
		return infoFile.getAvailableCodes();
	}
}
