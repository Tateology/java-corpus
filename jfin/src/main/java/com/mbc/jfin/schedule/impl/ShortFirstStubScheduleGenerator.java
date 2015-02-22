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
import com.mbc.jfin.schedule.SchedulePeriod;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 24/04/2011
 * Time: 15:31
 */
public class ShortFirstStubScheduleGenerator extends AbstractBaseScheduleGenerator {

    public ShortFirstStubScheduleGenerator() {
        super(-1);
    }

    public ShortFirstStubScheduleGenerator(int maxPeriods) {
        super(maxPeriods);
    }

    public Schedule generate(LocalDate start, LocalDate end, ReadablePeriod frequency) throws ScheduleException {
        ArrayList<SchedulePeriod> schedulePeriods = new ArrayList<SchedulePeriod>();

        LocalDate holdDate = end;

        int periodCount = 1;

        while (holdDate.isAfter(start))
        {
            LocalDate nextDate = end.minus(multiplyPeriod(frequency, periodCount));

            LocalDate notionalStartDate = null;
            LocalDate notionalEndDate = null;

            if (nextDate.isBefore(start))
            {
                notionalStartDate = nextDate;
                notionalEndDate = holdDate;
                nextDate = start;
            }


            SchedulePeriod schedulePeriod = new SchedulePeriod(nextDate, holdDate, notionalStartDate, notionalEndDate);

            schedulePeriods.add(0,schedulePeriod);

            holdDate = nextDate;
            periodCount++;
            if(maxPeriods>0 && periodCount>maxPeriods) {
                throw new ScheduleTooLongException(maxPeriods);
            }
        }

        return new Schedule(schedulePeriods);
    }
}
