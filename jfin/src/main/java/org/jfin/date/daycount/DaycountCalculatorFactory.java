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

package org.jfin.date.daycount;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfin.date.holiday.HolidayCalendar;

/**
 * Factory class for providing a layer of abstraction from the actual
 * implementation of the DaycountCalculator.
 *
 * The default DaycountCalculator is being designed to be an authoratative
 * reference implementation.
 *
 * You may, however, wish to use your own implementation if you need to
 * replicate a different implementation for testing.
 *
 * Use -Djfin.DaycountCalculatorFactory=ImplementationClass to use a different
 * implementation from the default.
 *
 * If you wish to use more than one concrete implementation of the
 * DaycountCalculatorFactory within the same JVM use the static
 * newInstance(String daycountCalculatorFactoryClassName) method.
 */
public abstract class DaycountCalculatorFactory
{

	private static Logger logger = Logger
			.getLogger("org.jfin.date.daycount.DaycountCalculatorFactory");

	public static String daycountCalculatorFactoryClassNameParameter = "jfin.DaycountCalculatorFactory";

	public static String defaultDaycountCalculatorFactoryClassName = "org.jfin.date.daycount.defaultimpl.DaycountCalculatorFactoryImpl";

	/**
	 * Get a new instance of the concrete implementation of the
	 * DaycountCalculatorFactory based upon the System parameter
	 * jfin.DaycountCalculatorFactory.
	 *
	 * If this parameter does not exist then it returns the default concrete
	 * implementation
	 * org.jfin.date.daycount.defaultimpl.DaycountCalculatorFactoryImpl.
	 *
	 * @return The new DaycountCalculatorFactory instance
	 */
	public static DaycountCalculatorFactory newInstance()
	{
		String daycountCalculatorFactoryClassName = System
				.getProperty(daycountCalculatorFactoryClassNameParameter);
		if (daycountCalculatorFactoryClassName == null)
		{
			logger.info("No system property "
					+ daycountCalculatorFactoryClassNameParameter
					+ " provided, using "
					+ defaultDaycountCalculatorFactoryClassName);
			daycountCalculatorFactoryClassName = defaultDaycountCalculatorFactoryClassName;
		} else
		{
			logger.info("Found system property "
					+ daycountCalculatorFactoryClassNameParameter + ": "
					+ defaultDaycountCalculatorFactoryClassName);
		}

		return newInstance(daycountCalculatorFactoryClassName);
	}

	/**
	 * Get a new instance of the concrete implementation of the
	 * DaycountCalculatorFactory based upon the
	 * daycountCalculatorFactoryClassName parameter.
	 *
	 * @param daycountCalculatorFactoryClassName
	 *            the class name of the DaycountCalculatorFactory to instantiate
	 * @return The new DaycountCalculatorFactory instance
	 */
	public static DaycountCalculatorFactory newInstance(
			String daycountCalculatorFactoryClassName)
	{

		logger
				.fine("Constructing new instance of day count calculator factory.");
		DaycountCalculatorFactory factory = null;

		try
		{

			logger.fine("Attempting to instantiate "
					+ daycountCalculatorFactoryClassName);
			Class daycountCalculatorFactoryClass = Class
					.forName(daycountCalculatorFactoryClassName);
			factory = (DaycountCalculatorFactory) daycountCalculatorFactoryClass
					.newInstance();
		} catch (ClassNotFoundException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a DaycountCalculatorFactory with class "
									+ daycountCalculatorFactoryClassName
									+ ". Check that this class exists within the classpath.",
							e);
			throw new DaycountException(e);
		} catch (InstantiationException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a DaycountCalculatorFactory with class "
									+ daycountCalculatorFactoryClassName
									+ ". Check that this class implements a default constructor.",
							e);
			throw new DaycountException(e);
		} catch (IllegalAccessException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a DaycountCalculatorFactory with class "
									+ daycountCalculatorFactoryClassName
									+ ". Check that this class implements a public default constructor.",
							e);
			throw new DaycountException(e);
		} catch (ClassCastException e)
		{
			logger
					.log(
							Level.SEVERE,
							"Cannot create a DaycountCalculatorFactory with class "
									+ daycountCalculatorFactoryClassName
									+ ". Check that this class extends DaycountCalculatorFactory.",
							e);
			throw new DaycountException(e);
		}

		return factory;
	}

	/**
	 * Retrieve an instance of an ISDA Actual/Actual daycount calculator.
	 *
	 * Also known as Actual/Actual Historical.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getISDAActualActual();

	/**
	 * Retrieve an instance of an ISMA Actual/Actual daycount calculator.
	 *
	 * Usually used for Euro denominated bonds and US Treasury bonds hence also
	 * known as Actual/Actual Bond.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getISMAActualActual();

	/**
	 * Retrieve an instance of an AFB Actual/Actual daycount calculator.
	 *
	 * Also known as the AFB Method.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getAFBActualActual();

	/**
	 * Retrieve an instance of an US 30/360 daycount calculator.
	 *
	 * Also known as 30/360.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getUS30360();

	/**
	 * Retrieve an instance of an European 30/360 daycount calculator.
	 *
	 * Also known as 30E/360
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getEU30360();

	/**
	 * Retrieve an instance of an Italian 30/360 daycount calculator.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getIT30360();

	/**
	 * Retrieve an instance of an Actual/360 daycount calculator.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getActual360();

	/**
	 * Retrieve an instance of an Actual/365 Fixed daycount calculator.
	 *
	 * Also known just as Actual/365.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getActual365Fixed();

	/**
	 * Retrieve an instance of an Actual/366 Fixed daycount calculator.
	 *
	 * Also known just as Actual/365.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getActual366();

	/**
	 * Retrieve an instance of an Business/252 daycount calculator for the
	 * provided HolidayCalendar.
	 *
	 * Also sometimes referred to as Brazilian.
	 *
	 * @return The DaycountCalculator
	 */
	public abstract DaycountCalculator getBusiness252(
			HolidayCalendar holidayCalendar);

	public DaycountCalculator getDaycountCalculator(String name) {
		if(name.equals("ISDAActualActual")) {
			return getISDAActualActual();
		} else if(name.equals("ISMAActualActual")) {
			return getISMAActualActual();
		} else if(name.equals("AFBActualActual")) {
			return getAFBActualActual();
		} else if(name.equals("US30360")) {
			return getUS30360();
		} else if(name.equals("EU30360")) {
			return getEU30360();
		} else if(name.equals("IT30360")) {
			return getIT30360();
		} else if(name.equals("Actual360")) {
			return getActual360();
		} else if(name.equals("Actual365Fixed")) {
			return getActual365Fixed();
		} else if(name.equals("Actual366")) {
			return getActual366();
		} else {
			throw new DaycountException("Unknown day count calculator \""+name+"\"");
		}
	}

	public abstract String[] getAvailableDaycountCalculators();
}
