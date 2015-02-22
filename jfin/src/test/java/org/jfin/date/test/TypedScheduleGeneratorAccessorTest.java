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

import junit.framework.TestCase;
import org.jfin.date.Period;
import org.jfin.date.TypedScheduleGenerator;
import org.jfin.date.accrual.impl.FixedAccrualPeriod;

public class TypedScheduleGeneratorAccessorTest extends TestCase {
	public void testSetPrototype() {
		FixedAccrualPeriod period1 = new FixedAccrualPeriod();
		FixedAccrualPeriod period2 = new FixedAccrualPeriod();

		TypedScheduleGenerator<FixedAccrualPeriod> scheduleGenerator = new TypedScheduleGenerator<FixedAccrualPeriod>(period1);

		assertEquals(period1,scheduleGenerator.getPrototype());

		scheduleGenerator.setPrototype(period2);

		assertEquals(period2,scheduleGenerator.getPrototype());
	}

	public void testSetMaxPeriods() {
		TypedScheduleGenerator<Period> scheduleGenerator = new TypedScheduleGenerator<Period>(new Period());

		assertEquals(-1,scheduleGenerator.getMaxPeriods());

		scheduleGenerator.setMaxPeriods(10);

		assertEquals(10,scheduleGenerator.getMaxPeriods());


	}
}
