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

import org.joda.time.LocalDate;

import java.io.*;
import java.util.TreeMap;

public class HolidayFile implements HolidaySource
{
	private TreeMap<String, HolidayFileEntry> entries;

    public HolidayFile(String holidayFileName, String infoFileName) throws IOException {
        this(new File(holidayFileName), new InfoFile(infoFileName));
    }

    public HolidayFile(String fileName, InfoFile infoFile) throws IOException {
        this(new File(fileName), infoFile);
    }

	public HolidayFile(File f, InfoFile infoFile) throws IOException
	{
		this(new FileReader(f), infoFile);
	}

    public HolidayFile(Reader reader, InfoFile infoFile) throws IOException
	{
		entries = new TreeMap<String, HolidayFileEntry>();
		load(reader, infoFile);
	}

	public void load(Reader rawReader, InfoFile infoFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(rawReader);

		// Discard the first line, contains column headings
		String line = reader.readLine();

		while (( line = reader.readLine() ) != null)
		{

			HolidayFileEntry holidayFileEntry = new HolidayFileEntry(line,
					infoFile);

			entries.put(holidayFileEntry.getReference(), holidayFileEntry);
		}
	}

    public boolean isWeekend(LocalDate date, String locale) {
        HolidayFileEntry entry = entryFor(date, locale);
        if(entry==null) return false;
        return entry.getDescription().equals("Weekend");
    }

    public boolean isHoliday(LocalDate date, String locale) {
        HolidayFileEntry entry = entryFor(date, locale);
        if(entry==null) return false;
        return !entry.getDescription().equals("Weekend");
    }

    private HolidayFileEntry entryFor(LocalDate date, String locale) {
        String reference = HolidayFileEntry.getReference(formatDate(date), locale);
        return entries.get(reference);
    }

    private String formatDate(LocalDate date) {
        return date.toString("yyyyMMdd");
    }
}
