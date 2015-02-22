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

import java.io.*;
import java.util.*;

/**
 * Represents a financialcalendar.com info.csv file.
 *
 * An example info.csv file is included with the db jFin distribution. For
 * information on licensing a full info.csv file, along with holiday calendars,
 * visit http://www.financialcalendar.com/
 *
 * financialcalendar.com is not associated with jFin
 */
public class InfoFile
{
	private Map<String, InfoFileEntry> entries;

    public InfoFile(String fileName) throws IOException {
        this(new File(fileName));
    }

	public InfoFile(File f) throws IOException
	{
        this(new FileReader(f));
	}

    public InfoFile(Reader reader) throws IOException
	{
		entries = new TreeMap<String, InfoFileEntry>();
		load(reader);
	}

	public void load(Reader rawReader) throws IOException
	{
        BufferedReader reader = new BufferedReader(rawReader);

		// Discard the first line, contains column headings
		String line = reader.readLine();

		while (( line = reader.readLine() ) != null)
		{
			InfoFileEntry infoFileEntry = new InfoFileEntry(line);
			entries.put(infoFileEntry.getCode(), infoFileEntry);
		}
	}

	/**
	 * Retrieves an entry based upon its code.
	 *
	 * @param code
	 *            The code to search for
	 * @return The entry for the given code, or null if the code is not found
	 */
	public InfoFileEntry getEntryByCode(String code)
	{
		return entries.get(code);
	}

	public String[] getAvailableCodes() {

		List<String> codes = new ArrayList<String>(entries.keySet());
		Collections.sort(codes);
		String[] codesArray = new String[codes.size()];

		for(int i=0;i<codes.size();i++) {
			codesArray[i] = codes.get(i);
		}

		return codesArray;
	}

}
