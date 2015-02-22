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

package org.jfin.date;

import org.jfin.date.util.ISDADateFormat;

import java.util.Calendar;

/**
 * Class representing a period between two dates. Instances of this class may
 * also contain 'notional' start and end dates which are required to accurately
 * calculate the day count fractions of irregular length periods within a longer
 * schedule.
 */
public class Period implements Comparable
{
	private Calendar startCalendar;

	private Calendar endCalendar;

	private Calendar referenceStartCalendar;

	private Calendar referenceEndCalendar;

	/**
	 * Default constructor
	 */
	public Period()
	{

	}

	/**
	 * Copy constructor
	 * @param toCopy The period to copy
	 */
	public Period(Period toCopy) {
		this.startCalendar = toCopy.startCalendar;
		this.endCalendar = toCopy.endCalendar;
		this.referenceStartCalendar = toCopy.referenceStartCalendar;
		this.referenceEndCalendar = toCopy.referenceEndCalendar;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Period clone()
	{
		return new Period(this);
	}



	/**
	 * @param startCalendar
	 *            (Start of period).
	 * @param endCalendar
	 *            (End of period).
	 */
	public Period(Calendar startCalendar, Calendar endCalendar)
	{
		this.startCalendar = startCalendar;
		this.endCalendar = endCalendar;
	}

	/**
	 * @param startCalendar
	 *            (Start of period).
	 * @param endCalendar
	 *            (End of period).
	 * @param referenceStartCalendar
	 *            (Notional start of period).
	 * @param referenceEndCalendar
	 *            (Notional end of period).
	 */
	public Period(Calendar startCalendar, Calendar endCalendar,
			Calendar referenceStartCalendar, Calendar referenceEndCalendar)
	{
		this.startCalendar = startCalendar;
		this.endCalendar = endCalendar;
		this.referenceStartCalendar = referenceStartCalendar;
		this.referenceEndCalendar = referenceEndCalendar;
	}

	/**
	 * @return Returns the startCalendar (Start of period).
	 */
	public Calendar getStartCalendar()
	{
		return startCalendar;
	}

	/**
	 * @param calendar
	 *            The startCalendar to set (Start of period).
	 */
	public void setStartCalendar(Calendar calendar)
	{
		this.startCalendar = calendar;
	}

	/**
	 * @return Returns the endCalendar (End of period).
	 */
	public Calendar getEndCalendar()
	{
		return endCalendar;
	}

	/**
	 * @param calendar
	 *            The endCalendar to set (End of period).
	 */
	public void setEndCalendar(Calendar calendar)
	{
		this.endCalendar = calendar;
	}

	/**
	 * @return Returns the referenceEndCalendar.
	 */
	public Calendar getReferenceEndCalendar()
	{
		return referenceEndCalendar;
	}

	/**
	 * @param referenceEndCalendar
	 *            The referenceEndCalendar to set.
	 */
	public void setReferenceEndCalendar(Calendar referenceEndCalendar)
	{
		this.referenceEndCalendar = referenceEndCalendar;
	}


	/**
	 * @return Returns the referenceStartCalendar.
	 */
	public Calendar getReferenceStartCalendar()
	{
		return referenceStartCalendar;
	}

	/**
	 * @param referenceStartCalendar
	 *            The referenceStartCalendar to set.
	 */
	public void setReferenceStartCalendar(Calendar referenceStartCalendar)
	{
		this.referenceStartCalendar = referenceStartCalendar;
	}

	/**
	 * Creates a human readable String representing this period, e.g: 2005/1/1 -
	 * 2006/12/30
	 */
	public String toString()
	{
		if (referenceStartCalendar == null || referenceEndCalendar == null)
		{
			return ISDADateFormat.format(startCalendar) + " - "
					+ ISDADateFormat.format(endCalendar);
		} else
		{
			return ISDADateFormat.format(startCalendar) + " - "
					+ ISDADateFormat.format(endCalendar) + "["
					+ ISDADateFormat.format(referenceStartCalendar) + " - "
					+ ISDADateFormat.format(referenceEndCalendar) + "]";
		}
	}

	private long getDoubleMidPoint()
	{
		return startCalendar.getTimeInMillis() + endCalendar.getTimeInMillis();
	}

	private long getDoubleMidPoint(Calendar calendar)
	{
		return 2 * calendar.getTimeInMillis();
	}

	/**
	 * Compares the midpoint of this Period with arg0. Arg0 can be either
	 * another Period or a Calendar.
	 *
	 * @param arg0 The object to compare to
	 * @return -1 if arg0 before the midpoint, 0 if arg0 equals or has the same
	 *         midpoint (within an hour), 1 if arg0 after the midpoint
	 */
	public int compareTo(Object arg0)
	{
		if (arg0 instanceof Period)
		{
			Period period = (Period) arg0;

			int retval = (int) ( ( getDoubleMidPoint() - period
					.getDoubleMidPoint() ) / 60000l );

			if (retval == 0)
				return 0;

			return retval / Math.abs(retval);

		} else if (arg0 instanceof Calendar)
		{

			Calendar calendar = (Calendar) arg0;

			int retval = (int) ( ( getDoubleMidPoint() - getDoubleMidPoint(calendar) ) / 60000l );

			if (retval == 0)
				return 0;

			return retval / Math.abs(retval);

		}

		throw new RuntimeException(
				"Cannot compare org.jfin.date.Period with an object of class "
						+ arg0.getClass().getName());
	}

}
