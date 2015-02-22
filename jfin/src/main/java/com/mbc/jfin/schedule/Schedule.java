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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dmb
 * Date: 23/04/2011
 * Time: 09:56
 */
public class Schedule implements Iterable<SchedulePeriod> {

    private List<SchedulePeriod> schedulePeriods;

    public Schedule(List<SchedulePeriod> schedulePeriods) {
        this.schedulePeriods = schedulePeriods;
    }

    public int size() {
        return schedulePeriods.size();
    }

    public boolean isEmpty() {
        return schedulePeriods.isEmpty();
    }

    public boolean contains(Object o) {
        return schedulePeriods.contains(o);
    }

    public Iterator<SchedulePeriod> iterator() {
        return schedulePeriods.iterator();
    }

    public boolean containsAll(Collection<?> objects) {
        return schedulePeriods.containsAll(objects);
    }

    @Override
    public boolean equals(Object o) {
        return schedulePeriods.equals(o);
    }

    @Override
    public int hashCode() {
        return schedulePeriods.hashCode();
    }

    public SchedulePeriod get(int i) {
        return schedulePeriods.get(i);
    }

    public int indexOf(Object o) {
        return schedulePeriods.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return schedulePeriods.lastIndexOf(o);
    }

    public List<SchedulePeriod> subList(int i, int i1) {
        return schedulePeriods.subList(i, i1);
    }
}
