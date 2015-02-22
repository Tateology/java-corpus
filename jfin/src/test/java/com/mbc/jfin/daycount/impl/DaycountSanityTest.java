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
import com.mbc.jfin.schedule.SchedulePeriod;
import junit.framework.TestCase;
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 06/05/2011
 * Time: 18:27
 */
public class DaycountSanityTest extends TestCase {
    private DaycountService service;
    private SchedulePeriod quarterlyPeriod;
    private SchedulePeriod semiPeriod;
    private SchedulePeriod annualPeriod;

    @Override
    protected void setUp() throws Exception {
        service = new DefaultDaycountServiceImpl();
        quarterlyPeriod = new SchedulePeriod(new LocalDate(2011,1,1), new LocalDate(2011,4,1));
        semiPeriod = new SchedulePeriod(new LocalDate(2011,1,1), new LocalDate(2011,7,1));
        annualPeriod = new SchedulePeriod(new LocalDate(2011,1,1), new LocalDate(2012,1,1));
    }

    public void testACTUAL_360() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.ACTUAL_360, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.ACTUAL_360, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.ACTUAL_360, null), 0.04);
    }

    public void testACTUAL_365_FIXED() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.ACTUAL_365_FIXED, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.ACTUAL_365_FIXED, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.ACTUAL_365_FIXED, null), 0.04);
    }

    public void testACTUAL_366() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.ACTUAL_366, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.ACTUAL_366, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.ACTUAL_366, null), 0.04);
    }

    public void testAFB_ACTUAL_ACTUAL() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.AFB_ACTUAL_ACTUAL, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.AFB_ACTUAL_ACTUAL, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.AFB_ACTUAL_ACTUAL, null), 0.04);
    }

    public void testEU_30_360() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.EU_30_360, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.EU_30_360, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.EU_30_360, null), 0.04);
    }

    public void testISDA_ACTUAL_ACTUAL() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.ISDA_ACTUAL_ACTUAL, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.ISDA_ACTUAL_ACTUAL, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.ISDA_ACTUAL_ACTUAL, null), 0.04);
    }

    public void testISMA_ACTUAL_ACTUAL() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.ISMA_ACTUAL_ACTUAL, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.ISMA_ACTUAL_ACTUAL, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.ISMA_ACTUAL_ACTUAL, null), 0.04);
    }

    public void testIT_30_360() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.IT_30_360, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.IT_30_360, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.IT_30_360, null), 0.04);
    }

    public void testUS_30_360() throws DaycountException {
        assertEquals(0.25d, service.calculateDaycountFraction(quarterlyPeriod, DaycountConvention.US_30_360, null), 0.01);
        assertEquals(0.50d, service.calculateDaycountFraction(semiPeriod, DaycountConvention.US_30_360, null), 0.02);
        assertEquals(1.00d, service.calculateDaycountFraction(annualPeriod, DaycountConvention.US_30_360, null), 0.04);
    }
}
