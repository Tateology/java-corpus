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

package com.mbc.jfin.util;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 26/04/2011
 * Time: 08:39
 */
public class DateUtils {
    public static int daysBetween(LocalDate start, LocalDate end) {
        return Days.daysBetween(start,end).getDays();
    }

    public static boolean isLeapYear(LocalDate date) {

        int year = date.getYear();

        return year%4==0 && (year%100!=0 || year%400==0);
    }

    public static boolean isLastDayOfMonth(LocalDate date) {
        return date.getDayOfMonth() == getDaysInMonth(date);
    }

    public static int getDaysInYear(LocalDate date) {
        if(isLeapYear(date)) {
            return 366;
        } else {
            return 365;
        }
    }

    public static int getDaysInMonth(LocalDate date) {
        switch(date.getMonthOfYear()) {
            case 2:
                if(isLeapYear(date)) {
                    return 29;
                } else {
                    return 28;
                }
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            default:
                return 30;
        }
    }
}
