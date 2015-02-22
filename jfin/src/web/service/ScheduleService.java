/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
 *
 * <p> This file is part of jFin. </p>
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

import java.util.Calendar;
import java.util.List;

import org.jfin.date.Frequency;
import org.jfin.date.Period;
import org.jfin.date.ScheduleException;
import org.jfin.date.ScheduleGenerator;
import org.jfin.date.StubType;



public class ScheduleService
{
	public String[] getStubTypeNames() {
		StubType[] stubTypes = StubType.values();

		String[] names = new String[stubTypes.length];

		for(int i=0;i<stubTypes.length;i++) names[i] = stubTypes[i].name();

		return names;
	}

	public String[] getFrequencyNames() {
		Frequency[] frequencies = Frequency.values();

		String[] names = new String[frequencies.length];

		for(int i=0;i<frequencies.length;i++) names[i] = frequencies[i].name();

		return names;
	}

	public Calendar[][] getSchedule(Calendar startDate, Calendar endDate, String frequencyName, String stubTypeName) throws ScheduleException {
		Frequency frequency = Frequency.valueOf(frequencyName);
		StubType stubType = StubType.valueOf(stubTypeName);
		List<Period> schedule = ScheduleGenerator.generateSchedule(startDate,endDate,frequency,stubType);

		Calendar[][] calendars = new Calendar[schedule.size()][4];

		for(int i=0;i<schedule.size();i++) {
			Period period = schedule.get(i);
			calendars[i][0]=period.getStartCalendar();
			calendars[i][1]=period.getEndCalendar();
			calendars[i][2]=period.getReferenceStartCalendar();
			calendars[i][3]=period.getReferenceEndCalendar();
		}
		return calendars;
	}

}
