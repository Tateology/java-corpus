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

package org.jfin.date.holiday;

import org.jfin.date.Period;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for providing a layer of abstraction from the actual
 * implementation of the HolidayCalendar.
 *
 * The default HolidayCalendarFactory only provides for "WE" or weekends. It is
 * expected that integrators will want to reuse an existing service within their
 * organisation to provide an authoratitive reference of holiday calendars.
 *
 * It is not within the scope of jFin to provide this data.
 *
 * Use -HolidayCalendarFactory=ImplementationClass to use a different
 * implementation from the default.
 *
 * If you wish to use more than one concrete implementation of the
 * HolidayCalendarFactory within the same JVM use the static newInstance(String
 * holidayCalendarFactoryClassName) method.
 */
public abstract class HolidayCalendarFactory
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.holiday.HolidayCalendarFactory");

	public static String defaultHolidayCalendarFactoryClassName = "org.jfin.date.holiday.defaultimpl.HolidayCalendarFactoryImpl";

	public static String defaultHolidayCalendarFactoryClassNameParameter = "jfin.HolidayCalendarFactory";

	/**
	 * Get a new instance of the concrete implementation of the
	 * HolidayCalendarFactory based upon the System parameter
	 * jfin.HolidayCalendarFactory.
	 *
	 * If this parameter does not exist then it returns the default concrete
	 * implementation
	 * org.jfin.date.holiday.defaultimpl.HolidayCalendarFactoryImpl.
	 *
	 * @return The new HolidayCalendarFactory instance
	 */
	public static HolidayCalendarFactory newInstance()
	{
		logger.info("Constructing new default holiday calendar provider.");

		String holidayCalendarFactoryClassName = System
				.getProperty(defaultHolidayCalendarFactoryClassNameParameter);
		if (holidayCalendarFactoryClassName == null)
		{
			logger.info("No system property "
					+ defaultHolidayCalendarFactoryClassNameParameter
					+ " provided, using "
					+ defaultHolidayCalendarFactoryClassName);
			holidayCalendarFactoryClassName = defaultHolidayCalendarFactoryClassName;
		} else
		{
			logger.info("Found system property "
					+ defaultHolidayCalendarFactoryClassNameParameter + ": "
					+ holidayCalendarFactoryClassName);
		}

		return newInstance(holidayCalendarFactoryClassName);
	}

	/**
	 * <p>
	 * Create a concrete instance of a HolidayCalendarFactory based upon:
	 * </p>
	 * <ul>
	 * <li>The class name in the system property jfin.HolidayCalendarFactory
	 * <li>And if this doesn't exist:
	 * <li>Default to
	 * org.jfin.date.test.holiday.defaultimpl.HolidayCalendarFactoryImpl
	 * </ul>
	 *
	 * @return The default HolidayCalendarProvider
	 */
	public static HolidayCalendarFactory newInstance(
			String holidayCalendarFactoryClassName)
	{

		try
		{

			logger.info("Attempting to instantiate "
					+ holidayCalendarFactoryClassName);
			Class holidayCalendarProviderClass = Class
					.forName(holidayCalendarFactoryClassName);
			HolidayCalendarFactory holidayCalendarFactory = (HolidayCalendarFactory) holidayCalendarProviderClass
					.newInstance();

			return holidayCalendarFactory;
		} catch (ClassNotFoundException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a holidayCalendarProvider with class "
									+ holidayCalendarFactoryClassName
									+ ". Check that this class exists within the classpath.",
							e);
			throw new HolidayCalendarException(e);
		} catch (InstantiationException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a holidayCalendarProvider with class "
									+ holidayCalendarFactoryClassName
									+ ". Check that this class implements a default constructor.",
							e);
			throw new HolidayCalendarException(e);
		} catch (IllegalAccessException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a holidayCalendarProvider with class "
									+ holidayCalendarFactoryClassName
									+ ". Check that this class implements a public default constructor.",
							e);
			throw new HolidayCalendarException(e);
		} catch (ClassCastException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a holidayCalendarProvider with class "
									+ holidayCalendarFactoryClassName
									+ ". Check that this class extends HolidayCalendarFactory.",
							e);
			throw new HolidayCalendarException(e);
		}
	}

	/**
	 * <p>
	 * Get the HolidayCalendar that represents the locale.
	 * </p>
	 *
	 * @param locale
	 * @return The HolidayCalendar for the given Locale
	 * @throws HolidayCalendarException
	 */
	public abstract HolidayCalendar getHolidayCalendar(String locale);

	/**
	 * <p>
	 * Get the HolidayCalendar that represents the locales provided as an array
	 * of Strings.
	 * </p>
	 *
	 * @param locale
	 * @param c
	 * @return The holiday calendar
	 */
	public abstract <T extends Period> HolidayCalendar<T> getHolidayCalendar(String locale, Class<T> c);

	/**
	 * <p>
	 * Get the HolidayCalendar that represents the locales provided as an array
	 * of Strings.
	 * </p>
	 *
	 * @param locales
	 * @param c the type of the HolidayCalendar to return
	 * @return The holiday calendar
	 */
	public <T extends Period> HolidayCalendar<T> getHolidayCalendar(String[] locales, Class<T> c)
	{
		if (locales.length == 1)
		{
			return getHolidayCalendar(locales[0]);
		}

		HolidayCalendarSet holidayCalendarSet = new HolidayCalendarSet();

		for (int i = 0; i < locales.length; i++)
		{
			HolidayCalendar holidayCalendar = getHolidayCalendar(locales[i]);
			holidayCalendarSet.addHolidayCalendar(holidayCalendar);
		}

		return holidayCalendarSet;
	}

	public HolidayCalendar getHolidayCalendar(String[] locales) {
		return getHolidayCalendar(locales, Period.class);
	}

	public abstract String[] getAvailableLocales();
}
