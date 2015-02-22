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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

public class HolidayFile
{
	private TreeMap<String, HolidayFileEntry> entries;

	public HolidayFile()
	{
		entries = new TreeMap<String, HolidayFileEntry>();
	}

	public HolidayFile(File f, InfoFile infoFile) throws IOException
	{
		this();
		load(f, infoFile);
	}

	public void load(File f, InfoFile infoFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(f));

		// Discard the first line, contains column headings
		String line = reader.readLine();

		while (( line = reader.readLine() ) != null)
		{

			HolidayFileEntry holidayFileEntry = new HolidayFileEntry(line,
					infoFile);

			entries.put(holidayFileEntry.getReference(), holidayFileEntry);
		}
	}

	/**
	 * Add an entry to the info file. The entry is keyed by its code.
	 *
	 * @param entry
	 */
	public void addEntry(HolidayFileEntry entry)
	{
		entries.put(entry.getReference(), entry);
	}

	/**
	 * Retrieves an entry based upon its code.
	 *
	 * @param code
	 *            The code to search for
	 * @return The entry for the given code, or null if the code is not found
	 */
	public HolidayFileEntry getEntryByReference(String code)
	{
		return entries.get(code);
	}
}
