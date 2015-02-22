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

package com.mbc.jfin.schedule.impl;

import com.mbc.jfin.schedule.Schedule;
import com.mbc.jfin.schedule.ScheduleException;
import com.mbc.jfin.schedule.ScheduleService;
import com.mbc.jfin.schedule.StubType;
import com.mbc.jfin.schedule.impl.DefaultScheduleServiceImpl;
import junit.framework.TestCase;
import org.joda.time.LocalDate;
import org.joda.time.Years;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 25/04/2011
 * Time: 11:28
 */
public class DefaultScheduleServiceImplTest extends TestCase {
    ScheduleService scheduleService;

    @Override
    protected void setUp() throws Exception {
        scheduleService = new DefaultScheduleServiceImpl(10);
    }

    public void testNoStub() throws ScheduleException {
        Schedule schedule = scheduleService.generateSchedule(new LocalDate(2006,3,28), new LocalDate(2011,3,28), Years.ONE, StubType.NONE);

		assertEquals(5,schedule.size());

        try {
            schedule = scheduleService.generateSchedule(new LocalDate(2006,3,28), new LocalDate(2011,4,28), Years.ONE, StubType.NONE);
            fail("Shouldn't have generated a schedule with an uneven period");
        } catch(ScheduleException e) {
            // Squelch expected
        }
    }

}
