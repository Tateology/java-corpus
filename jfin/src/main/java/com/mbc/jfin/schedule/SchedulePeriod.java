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

package com.mbc.jfin.schedule;

import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 23/04/2011
 * Time: 09:46
 */
public class SchedulePeriod {
    private LocalDate start;

	private LocalDate end;

	private LocalDate referenceStart;

	private LocalDate referenceEnd;

    public SchedulePeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public SchedulePeriod(LocalDate start, LocalDate end, LocalDate referenceStart, LocalDate referenceEnd) {
        this.start = start;
        this.end = end;
        this.referenceStart = referenceStart;
        this.referenceEnd = referenceEnd;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public LocalDate getReferenceStart() {
        return referenceStart;
    }

    public LocalDate getReferenceEnd() {
        return referenceEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchedulePeriod schedulePeriod = (SchedulePeriod) o;

        if (!end.equals(schedulePeriod.end)) return false;
        if (referenceEnd != null ? !referenceEnd.equals(schedulePeriod.referenceEnd) : schedulePeriod.referenceEnd != null)
            return false;
        if (referenceStart != null ? !referenceStart.equals(schedulePeriod.referenceStart) : schedulePeriod.referenceStart != null)
            return false;
        if (!start.equals(schedulePeriod.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + (referenceStart != null ? referenceStart.hashCode() : 0);
        result = 31 * result + (referenceEnd != null ? referenceEnd.hashCode() : 0);
        return result;
    }
}


