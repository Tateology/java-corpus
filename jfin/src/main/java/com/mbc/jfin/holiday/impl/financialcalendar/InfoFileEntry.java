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

/**
 * Represents an entry in the financialcalendar.com info.csv file.
 *
 * An example info.csv file is included with the db jFin distribution. For
 * information on licensing a full info.csv file, along with holiday calendars,
 * visit http://www.financialcalendar.com/
 *
 * financialcalendar.com is not associated with jFin
 */
public class InfoFileEntry
{
	private String code;

	private String center;

	private String country;

	private String currency;

	private String type;

	private String isoCountry;

	private String define1;

	private String define2;

	/**
	 * Construct an InfoFileEntry from a line in the .csv file
	 *
	 * @param lineToParse
	 */
	public InfoFileEntry(String lineToParse)
	{
		parse(lineToParse);
	}

	/**
	 * Construct an InfoFileEntry
	 *
	 * @param code
	 * @param center
	 * @param country
	 * @param currency
	 * @param type
	 * @param isoCountry
	 * @param define1
	 * @param define2
	 */
	public InfoFileEntry(String code, String center, String country,
			String currency, String type, String isoCountry,
			String define1, String define2)
	{
		this.code = code;
		this.center = center;
		this.country = country;
		this.currency = currency;
		this.type = type;
		this.isoCountry = isoCountry;
		this.define1 = define1;
		this.define2 = define2;
	}

	/**
	 * Parses a comma seperated line from a .csv file into the InfoFileEntry
	 *
	 * @param line
	 */
	public void parse(String line)
	{
		String[] parts = line.split(",");

		if (parts.length > 0)
		{
			code = parts[0];
		}

		if (parts.length > 1)
		{
			center = parts[1];
		}

		if (parts.length > 2)
		{
			country = parts[2];
		}

		if (parts.length > 3)
		{
			currency = parts[3];
		}

		if (parts.length > 4)
		{
			type = parts[4];
		}

		if (parts.length > 5)
		{
			isoCountry = parts[5];
		}

		if (parts.length > 6)
		{
			define1 = parts[6];
		}

		if (parts.length > 7)
		{
			define2 = parts[7];
		}
	}

	/**
	 * @return Returns the center.
	 */
	public String getCenter()
	{
		return center;
	}

	/**
	 * @return Returns the code.
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @return Returns the country.
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * @return Returns the currency.
	 */
	public String getCurrency()
	{
		return currency;
	}

	/**
	 * @return Returns the define1.
	 */
	public String getDefine1()
	{
		return define1;
	}

	/**
	 * @return Returns the define2.
	 */
	public String getDefine2()
	{
		return define2;
	}

	/**
	 * @return Returns the isoCountry.
	 */
	public String getIsoCountry()
	{
		return isoCountry;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

}
