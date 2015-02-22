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

package org.jfin.date.test.web.example;

import java.text.ParseException;

import junit.framework.TestCase;

import org.jfin.date.web.example.PlainSwapForm;

public class PlainSwapFormTest extends TestCase
{
	public void testNumberFormat() throws ParseException {
		PlainSwapForm form = new PlainSwapForm();

		assertEquals(1000d,form.parseDouble("1k"));
		assertEquals(1000d,form.parseDouble("1K"));
		assertEquals(1000000d,form.parseDouble("1m"));
		assertEquals(1000000d,form.parseDouble("1M"));
		assertEquals(1000000000d,form.parseDouble("1b"));
		assertEquals(1000000000d,form.parseDouble("1B"));

		assertEquals(1000d,form.parseDouble("1,000"));
	}

	public void testPercentageFormat() throws ParseException {
		PlainSwapForm form = new PlainSwapForm();
		assertEquals(0.1d,form.parsePercentage("10%"));
		assertEquals(0.1d,form.parsePercentage("0.1"));
	}
}
