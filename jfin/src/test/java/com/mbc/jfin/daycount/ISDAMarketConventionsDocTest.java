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

package com.mbc.jfin.daycount;

import com.mbc.jfin.daycount.impl.DefaultDaycountServiceImpl;
import com.mbc.jfin.schedule.SchedulePeriod;
import junit.framework.TestCase;
import org.joda.time.LocalDate;

/**
 * Slew of tests based upon the ISDA EMU Market conventions document
 * http://www.isda.org/c_and_a/pdf/mktc1198.pdf
 *
 * @author dmb
 *
 */
public class ISDAMarketConventionsDocTest extends TestCase
{
	private double notional = 10000;

	private double fixedRate = 0.1;

    private DaycountService daycountService;



	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

        daycountService = new DefaultDaycountServiceImpl();

	}

	public void testPage3() throws DaycountException {

        SchedulePeriod period = new SchedulePeriod( new LocalDate(2003,11,1), new LocalDate(2004,5,1) );

		assertEquals(497.72, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, period), 0.01);

		assertEquals(500.00, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, period), 0.01);

		assertEquals(497.27, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, period), 0.01);
	}

	public void testPage4_5() throws DaycountException {
		LocalDate notionalStartDate = new LocalDate(1998, 7, 1);
		LocalDate date1 = new LocalDate(1999, 2, 1);
		LocalDate date2 = new LocalDate(1999, 7, 1);
		LocalDate date3 = new LocalDate(2000, 7, 1);




		assertEquals(410.96, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, date2)),0.01);

		assertEquals(410.96, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, date2)),0.01);

		assertEquals(410.96, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, date2)),0.01);

		assertEquals(1001.38, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)), 0.01);

		assertEquals(1000.00, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)), 0.01);

		assertEquals(1000.00, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)));
	}

	public void testPage6() throws DaycountException {
		LocalDate notionalStartDate = new LocalDate(2002, 7, 15);
		LocalDate notionalEndDate = new LocalDate(2003, 1, 15);
		LocalDate date1 = new LocalDate(2002, 8, 15);
		LocalDate date2 = new LocalDate(2003, 7, 15);
		LocalDate date3 = new LocalDate(2004, 1, 15);

		assertEquals(915.07, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, notionalEndDate)),0.01);

		assertEquals(915.76, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, notionalEndDate)), 0.01);

		assertEquals(915.07, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, notionalEndDate)),0.01);

		assertEquals(504.00, // 504.11 In ISDA Docs, but part falls in a leap
				// year are the docs incorrect!
				getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)),0.01);

		assertEquals(500.00, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)),0.01);

		assertEquals(504.11, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3)), 0.01);

	}

	public void testPage7() throws DaycountException {
		LocalDate date1 = new LocalDate(1999, 7, 30);
		LocalDate date2 = new LocalDate(2000, 1, 30);
		LocalDate date3 = new LocalDate(2000, 6, 30);
		LocalDate notionalEndDate = new LocalDate(2000, 7, 30);

		assertEquals(503.89, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2)), 0.01);

		assertEquals(500.00, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2)), 0.01);

		assertEquals(504.11, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2)), 0.01);

		assertEquals(415.30, getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3, date2,
				notionalEndDate)), 0.01d);

		assertEquals(417.58, getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3, date2,
				notionalEndDate)), 0.01d);

		assertEquals(415.30, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date2, date3, date2,
				notionalEndDate)), 0.01);
	}

	public void testPage8_9() throws DaycountException {
		LocalDate date1 = new LocalDate(1999, 11, 30);
		LocalDate date2 = new LocalDate(2000, 4, 30);
		LocalDate notionalStartDate = new LocalDate(2000, 2, 29);
		LocalDate notionalEndDate = new LocalDate(2000, 5, 31);

		assertEquals(415.54,
				getAmount(DaycountConvention.ISDA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2, notionalStartDate,
						notionalEndDate)), 0.01d);

		assertEquals(415.76,
				getAmount(DaycountConvention.ISMA_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2, notionalStartDate,
						notionalEndDate)), 0.01d);

		assertEquals(415.30, getAmount(DaycountConvention.AFB_ACTUAL_ACTUAL, new SchedulePeriod(date1, date2,
				notionalStartDate, notionalEndDate)), 0.01d);

	}


	public double getAmount(DaycountConvention convention, SchedulePeriod period) throws DaycountException {
		double daycount = daycountService.calculateDaycountFraction(period, convention, null);


		return notional * fixedRate * daycount;

	}
}

