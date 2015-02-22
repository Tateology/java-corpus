/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p>Copyright (C) 2005-2011 Morgan Brown Consultancy Ltd. </p>
 *
 * <p>This file is part of jFin. </p>
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

package com.mbc.jfin.holiday.impl.financialcalendar;

public class HolidayFileEntry
{
	private String date;

	private InfoFileEntry info;

	private String description;



	/**
	 * Construct an HolidayFileEntry from a line in the .csv file and an info
	 * file
	 *
	 * @param lineToParse The line to parse
	 * @param infoFile The location of the infofile
	 */
	public HolidayFileEntry(String lineToParse, InfoFile infoFile)
	{
		parse(lineToParse, infoFile);
	}

	/**
	 * @param date The date as a string in the same format as the tricol.csv file
	 * @param info The infoFileEntry
	 * @param description The description
	 */
	public HolidayFileEntry(String date, InfoFileEntry info, String description)
	{
		this.date = date;
		this.info = info;
		this.description = description;
	}

	/**
	 * Parses a comma seperated line from a .csv file into the HolidayFileEntry
	 *
	 * @param line The line to parse
	 * @param infoFile the info file
	 */
	public void parse(String line, InfoFile infoFile)
	{
		String[] parts = line.split(",");

		if (parts.length > 0)
		{
			date = parts[0];
		}

		if (parts.length > 1)
		{
			info = infoFile.getEntryByCode(parts[1]);
		}

		if (parts.length > 2)
		{
			description = parts[2];
		}
	}

	/**
	 * @return Returns the date.
	 */
	public String getDate()
	{
		return date;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return Returns the info.
	 */
	public InfoFileEntry getInfo()
	{
		return info;
	}

	public static String getReference(String date, String infoCode)
	{
		return date + ":" + infoCode;
	}

	public String getReference()
	{
		return getReference(getDate(), getInfo().getCode());
	}

}
