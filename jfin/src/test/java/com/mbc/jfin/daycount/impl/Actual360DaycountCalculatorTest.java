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

package com.mbc.jfin.daycount.impl;

import com.mbc.jfin.daycount.impl.calculator.Actual360DaycountCalculator;
import com.mbc.jfin.schedule.*;
import com.mbc.jfin.schedule.impl.DefaultScheduleServiceImpl;
import junit.framework.TestCase;
import org.joda.time.LocalDate;
import org.joda.time.Months;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 26/04/2011
 * Time: 08:45
 */
public class Actual360DaycountCalculatorTest extends TestCase {
    DaycountCalculator calculator;

    @Override
    protected void setUp() throws Exception {
        calculator = new Actual360DaycountCalculator();
    }

    public void testRoughlyQuarter() throws ScheduleException {
        ScheduleService scheduleService = new DefaultScheduleServiceImpl();

        Schedule schedule = scheduleService.generateSchedule(new LocalDate(2011,1,10), new LocalDate(2020,1,10), Months.THREE, StubType.NONE);

        for(SchedulePeriod period: schedule) {
            assertEquals(0.25d, calculator.calculateDaycountFraction(period, null), 0.01d);
        }
    }

}
