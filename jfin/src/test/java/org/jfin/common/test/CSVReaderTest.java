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

package org.jfin.common.test;

import junit.framework.TestCase;
import org.jfin.common.CSVReader;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 *
 */
public class CSVReaderTest extends TestCase {
	public void testReader() throws IOException {
		String testFile = "this,is,a\ntest,csv,file";

		ByteArrayInputStream bin = new ByteArrayInputStream(testFile.getBytes());

		InputStreamReader isr = new InputStreamReader(bin);

		CSVReader reader = new CSVReader(isr);

		String[] line1 = reader.nextCSVLine();
		String[] line2 = reader.nextCSVLine();

		assertEquals(3,line1.length);
		assertEquals(3,line2.length);
		assertNull(reader.nextCSVLine());
	}
}
