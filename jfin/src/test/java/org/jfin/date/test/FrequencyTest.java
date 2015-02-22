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

package org.jfin.date.test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Frequency;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;

public class FrequencyTest extends TestCase
{
	public void testFrequency()
	{
		Frequency monthly = Frequency.MONTHLY;
		Frequency quarterly = Frequency.QUARTERLY;
		Frequency semiAnnually = Frequency.SEMI_ANNUALLY;
		Frequency annually = Frequency.ANNUALLY;

		Calendar referenceCalendar = new GregorianCalendar(2005, 0, 1);

		BusinessDayConvention unadjusted = BusinessDayConvention.UNADJUSTED;

		HolidayCalendar we = HolidayCalendarFactory.newInstance()
				.getHolidayCalendar("WE");

		Calendar monthlyCal = we.advance(referenceCalendar, monthly
				.getPeriodAmount(), monthly.getPeriodUnit(), unadjusted);
		Calendar quarterlyCal = we.advance(referenceCalendar, quarterly
				.getPeriodAmount(), quarterly.getPeriodUnit(), unadjusted);
		Calendar semiAnnuallyCal = we.advance(referenceCalendar, semiAnnually
				.getPeriodAmount(), semiAnnually.getPeriodUnit(), unadjusted);
		Calendar annuallyCal = we.advance(referenceCalendar, annually
				.getPeriodAmount(), annually.getPeriodUnit(), unadjusted);

		assertEquals(monthly.getTenorDescriptor(), "1M");
		assertEquals(monthlyCal, new GregorianCalendar(2005, 1, 1));

		assertEquals(quarterly.getTenorDescriptor(), "3M");
		assertEquals(quarterlyCal, new GregorianCalendar(2005, 3, 1));

		assertEquals(semiAnnually.getTenorDescriptor(), "6M");
		assertEquals(semiAnnuallyCal, new GregorianCalendar(2005, 6, 1));

		assertEquals(annually.getTenorDescriptor(), "1Y");
		assertEquals(annuallyCal, new GregorianCalendar(2006, 0, 1));

	}
}
