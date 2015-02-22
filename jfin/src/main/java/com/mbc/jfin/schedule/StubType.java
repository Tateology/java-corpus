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

/**
 * <p>
 * Enumeration of the various stub types, currently supports:
 * </p>
 */
public enum StubType
{
	/*
	 * pluggable ScheduleGenerators <-- Preferred but would need to see concrete
	 * examples Rely on end developer to create specific classes for specific
	 * instruments
	 */

	/**
	 * The short stub period is at the start of the schedule, e.g.:
	 *
	 * X.X...X...X...X...X...X...X
	 */
	SHORT_FIRST,

	/**
	 * The short stub period is at the end of the schedule
	 *
	 * X...X...X...X...X...X...X.X
	 */
	SHORT_LAST,

	/**
	 * The long stub period is at the start of the schedule, e.g.:
	 *
	 * X.....X...X...X...X...X...X
	 */
	LONG_FIRST,

	/**
	 * The long stub period is at the end of the schedule
	 *
	 * X...X...X...X...X...X.....X
	 */
	LONG_LAST,

	/**
	 * There should be no stub period, if the frequency doesn't exactly fit the
	 * start/end date then an exception will be thrown
	 *
	 * X...X...X...X...X...X...X
	 */
	NONE;


}
