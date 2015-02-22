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

package org.jfin.date.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class to provide a more legible single line logger
 */
public class jfinLoggingFormatter extends Formatter
{
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd hh:mm:ss");

	public String format(LogRecord l)
	{
		StringBuffer ret = new StringBuffer(l.getLevel().getLocalizedName());

		ret.append("");
		while (ret.length() < 8)
			ret.append(" ");
		ret.append(df.format(new Date(l.getMillis())));
		ret.append(" ");
		ret.append(l.getMessage());
		ret.append(" (");
		ret.append(l.getLoggerName());
		ret.append(")");
		ret.append("\n");

		return ret.toString();
	}

}
