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

import java.util.TreeMap;
import java.util.logging.Logger;

import org.jfin.common.Iso3166Country;
import org.jfin.common.Iso4217Currency;

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

	private Iso4217Currency currency;

	private String type;

	private Iso3166Country isoCountry;

	private String define1;

	private String define2;

	private TreeMap<String, String> countryMappings;

	private static Logger logger = Logger
			.getLogger("org.jfin.date.holiday.financialcalendarimpl.InfoFileEntry");

	/**
	 * Default constructor
	 */
	public InfoFileEntry()
	{
		initialiseCountryMapping();
	}

	/**
	 * Construct an InfoFileEntry from a line in the .csv file
	 *
	 * @param lineToParse
	 */
	public InfoFileEntry(String lineToParse)
	{
		this();
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
			Iso4217Currency currency, String type, Iso3166Country isoCountry,
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
			setCode(parts[0]);
		}

		if (parts.length > 1)
		{
			setCenter(parts[1]);
		}

		if (parts.length > 2)
		{
			setCountry(parts[2]);
		}

		if (parts.length > 3)
		{
			if (parts[3].length() > 0)
			{
				try
				{
					setCurrency(Iso4217Currency.valueOf(parts[3]));
				} catch (Exception e)
				{
					logger.warning("Couldn't find currency \"" + parts[3]
							+ "\"");
				}
			}
		}

		if (parts.length > 4)
		{
			setType(parts[4]);
		}

		if (parts.length > 5)
		{
			if (parts[5].length() > 0)
			{
				try
				{
					setIsoCountry(Iso3166Country
							.valueOf(getCountryMapping(parts[5])));
				} catch (Exception e)
				{
					logger
							.warning("Couldn't find country \"" + parts[5]
									+ "\"");
				}
			}
		}

		if (parts.length > 6)
		{
			setDefine1(parts[6]);
		}

		if (parts.length > 7)
		{
			setDefine2(parts[7]);
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
	 * @param center
	 *            The center to set.
	 */
	public void setCenter(String center)
	{
		this.center = center;
	}

	/**
	 * @return Returns the code.
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *            The code to set.
	 */
	public void setCode(String code)
	{
		this.code = code;
	}

	/**
	 * @return Returns the country.
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * @param country
	 *            The country to set.
	 */
	public void setCountry(String country)
	{
		this.country = country;
	}

	/**
	 * @return Returns the currency.
	 */
	public Iso4217Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @param currency
	 *            The currency to set.
	 */
	public void setCurrency(Iso4217Currency currency)
	{
		this.currency = currency;
	}

	/**
	 * @return Returns the define1.
	 */
	public String getDefine1()
	{
		return define1;
	}

	/**
	 * @param define1
	 *            The define1 to set.
	 */
	public void setDefine1(String define1)
	{
		this.define1 = define1;
	}

	/**
	 * @return Returns the define2.
	 */
	public String getDefine2()
	{
		return define2;
	}

	/**
	 * @param define2
	 *            The define2 to set.
	 */
	public void setDefine2(String define2)
	{
		this.define2 = define2;
	}

	/**
	 * @return Returns the isoCountry.
	 */
	public Iso3166Country getIsoCountry()
	{
		return isoCountry;
	}

	/**
	 * @param isoCountry
	 *            The isoCountry to set.
	 */
	public void setIsoCountry(Iso3166Country isoCountry)
	{
		this.isoCountry = isoCountry;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return ( code == null ? "" : code ) + ","
				+ ( center == null ? "" : center ) + ","
				+ ( country == null ? "" : country ) + ","
				+ ( currency == null ? "" : currency ) + ","
				+ ( type == null ? "" : type ) + ","
				+ ( isoCountry == null ? "" : isoCountry ) + ","
				+ ( define1 == null ? "" : define1 ) + ","
				+ ( define2 == null ? "" : define2 );
	}

	public void initialiseCountryMapping()
	{
		countryMappings = new TreeMap<String, String>();
		countryMappings.put("IM", "GB"); // Map Isle Of Mann to GB
		countryMappings.put("JE", "GB"); // Map Jersey to GB
	}

	public String getCountryMapping(String country)
	{
		if (country == null)
			return null;
		if (countryMappings.get(country) == null)
			return country;
		return countryMappings.get(country);
	}

	public void getCurrencyMapping()
	{

	}
}
