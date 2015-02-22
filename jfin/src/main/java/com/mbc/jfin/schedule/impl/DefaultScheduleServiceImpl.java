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
import com.mbc.jfin.schedule.ScheduleService;
import com.mbc.jfin.schedule.ScheduleException;
import com.mbc.jfin.schedule.StubType;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 25/04/2011
 * Time: 11:23
 */
public class DefaultScheduleServiceImpl implements ScheduleService {

    private Map<StubType,ScheduleGenerator> generators;

    public DefaultScheduleServiceImpl() {
        this(-1);
    }

    public DefaultScheduleServiceImpl(int maxPeriods) {
        generators = new HashMap<StubType, ScheduleGenerator>();
        generators.put(StubType.NONE, new NoStubScheduleGenerator(maxPeriods));
        generators.put(StubType.LONG_FIRST, new LongFirstStubScheduleGenerator(maxPeriods));
        generators.put(StubType.LONG_LAST, new LongLastStubScheduleGenerator(maxPeriods));
        generators.put(StubType.SHORT_FIRST, new ShortFirstStubScheduleGenerator(maxPeriods));
        generators.put(StubType.SHORT_LAST, new ShortLastStubScheduleGenerator(maxPeriods));
    }

    public Schedule generateSchedule(LocalDate startDate, LocalDate endDate, ReadablePeriod frequency, StubType stubType) throws ScheduleException {

        ScheduleGenerator generator = generators.get(stubType);

        if(generator==null) {
            throw new ScheduleException("Unsupported stub type: "+stubType);
        }

        return generator.generate(startDate, endDate, frequency);

    }
}
