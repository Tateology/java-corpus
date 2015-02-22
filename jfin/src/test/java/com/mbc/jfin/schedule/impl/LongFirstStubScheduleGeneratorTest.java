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
import com.mbc.jfin.schedule.impl.LongFirstStubScheduleGenerator;
import com.mbc.jfin.schedule.impl.ScheduleGenerator;
import junit.framework.TestCase;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 24/04/2011
 * Time: 17:29
 */
public class LongFirstStubScheduleGeneratorTest extends TestCase {

    public void testLongFirstStub() throws ScheduleException {
        ScheduleGenerator generator = new LongFirstStubScheduleGenerator();

        Schedule schedule = generator.generate(new LocalDate(2006,3,28), new LocalDate(2011,4,28), Years.ONE);

		assertEquals(5,schedule.size());

        assertEquals(new LocalDate(2006,3,28), schedule.get(0).getStart() );
        assertEquals(new LocalDate(2007,4,28), schedule.get(0).getEnd() );


        assertEquals(new LocalDate(2007,4,28), schedule.get(1).getStart() );
        assertEquals(new LocalDate(2008,4,28), schedule.get(1).getEnd() );

        assertEquals(new LocalDate(2008,4,28), schedule.get(2).getStart() );
        assertEquals(new LocalDate(2009,4,28), schedule.get(2).getEnd() );

        assertEquals(new LocalDate(2009,4,28), schedule.get(3).getStart() );
        assertEquals(new LocalDate(2010,4,28), schedule.get(3).getEnd() );

        assertEquals(new LocalDate(2010,4,28), schedule.get(4).getStart() );
        assertEquals(new LocalDate(2011,4,28), schedule.get(4).getEnd() );
    }


    public void testGeneratorFail1() {
        ScheduleGenerator generator = new LongFirstStubScheduleGenerator(3);

        try {
            generator.generate(new LocalDate(2006,3,31), new LocalDate(2007,3,31), Months.THREE);
            fail("Should have thrown an exception when creating a schedule that was too long");
        } catch (ScheduleException e) {
            // Suppress - expected exception
        }
    }

}
