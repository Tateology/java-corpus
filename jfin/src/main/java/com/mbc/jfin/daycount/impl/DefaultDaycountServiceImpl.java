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

import com.mbc.jfin.daycount.DaycountConvention;
import com.mbc.jfin.daycount.DaycountException;
import com.mbc.jfin.daycount.DaycountService;
import com.mbc.jfin.daycount.impl.calculator.*;
import com.mbc.jfin.holiday.HolidayCalendar;
import com.mbc.jfin.schedule.SchedulePeriod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 26/04/2011
 * Time: 08:33
 */
public class DefaultDaycountServiceImpl implements DaycountService {

    private Map<DaycountConvention, DaycountCalculator> calculators;

    public DefaultDaycountServiceImpl() {
        calculators = new HashMap<DaycountConvention, DaycountCalculator>();
        calculators.put(DaycountConvention.ACTUAL_360, new Actual360DaycountCalculator());
        calculators.put(DaycountConvention.ACTUAL_365_FIXED, new Actual365FixedDaycountCalculator());
        calculators.put(DaycountConvention.ACTUAL_366, new Actual366DaycountCalculator());
        calculators.put(DaycountConvention.AFB_ACTUAL_ACTUAL, new AFBActualActualDaycountCalculator());
        calculators.put(DaycountConvention.EU_30_360, new EU30360DaycountCalculator());
        calculators.put(DaycountConvention.ISDA_ACTUAL_ACTUAL, new ISDAActualActualDaycountCalculator());
        calculators.put(DaycountConvention.ISMA_ACTUAL_ACTUAL, new ISMAActualActualDaycountCalculator());
        calculators.put(DaycountConvention.IT_30_360, new IT30360DaycountCalculator());
        calculators.put(DaycountConvention.US_30_360, new US30360DaycountCalculator());
    }

    public double calculateDaycountFraction(SchedulePeriod period, DaycountConvention convention, HolidayCalendar calendar) throws DaycountException {

        DaycountCalculator calculator = calculators.get(convention);

        if(calculator==null) {
            throw new DaycountNotImplementedException(convention);
        }


        return calculator.calculateDaycountFraction(period, calendar);
    }
}
