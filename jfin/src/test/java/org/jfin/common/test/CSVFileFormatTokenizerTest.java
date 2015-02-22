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

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.jfin.common.CSVFileFormatTokenizer;

public class CSVFileFormatTokenizerTest extends TestCase
{
	public void testSimple1()
	{
		String line = "this,is,a,test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(4, tokens.length);
		assertEquals("this", tokens[0]);
		assertEquals("is", tokens[1]);
		assertEquals("a", tokens[2]);
		assertEquals("test", tokens[3]);
	}

	public void testSimple2()
	{
		String line = ",this,is,a,test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(5, tokens.length);
		assertEquals("", tokens[0]);
		assertEquals("this", tokens[1]);
		assertEquals("is", tokens[2]);
		assertEquals("a", tokens[3]);
		assertEquals("test", tokens[4]);
	}

	public void testSimple3()
	{
		String line = ",this,,is,,,a,test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(8, tokens.length);
		assertEquals("", tokens[0]);
		assertEquals("this", tokens[1]);
		assertEquals("", tokens[2]);
		assertEquals("is", tokens[3]);
		assertEquals("", tokens[4]);
		assertEquals("", tokens[5]);
		assertEquals("a", tokens[6]);
		assertEquals("test", tokens[7]);
	}

	public void testSimple4()
	{
		String line = "this,is,a,test,";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(5, tokens.length);
		assertEquals("this", tokens[0]);
		assertEquals("is", tokens[1]);
		assertEquals("a", tokens[2]);
		assertEquals("test", tokens[3]);
		assertEquals("", tokens[4]);
	}

	public void testSimple5()
	{
		String line = "this,is,\"a\",test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(4, tokens.length);
		assertEquals("this", tokens[0]);
		assertEquals("is", tokens[1]);
		assertEquals("a", tokens[2]);
		assertEquals("test", tokens[3]);
	}

	public void testSimple6()
	{
		String line = "this,is,\"a,test\"";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(3, tokens.length);
		assertEquals("this", tokens[0]);
		assertEquals("is", tokens[1]);
		assertEquals("a,test", tokens[2]);
	}

	public void testSimple7()
	{
		String line = "\"this,is\",\"a,test\"";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(2, tokens.length);
		assertEquals("this,is", tokens[0]);
		assertEquals("a,test", tokens[1]);
	}

	public void testSimple8() {
		String line = ",\",test\"";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(2, tokens.length);
		assertEquals("", tokens[0]);
		assertEquals(",test", tokens[1]);
	}

	public void testSimple9() {
		String line = "\"\"\"a test, quote\"\" said\",the test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(2, tokens.length);
		assertEquals("\"a test, quote\" said", tokens[0]);
		assertEquals("the test", tokens[1]);

	}

	public void testSimple10() {
		String line = "\"\"\"a, test, quote\"\" said\",the test";
		String[] tokens = CSVFileFormatTokenizer.getTokens(line);

		assertEquals(2, tokens.length);
		assertEquals("\"a, test, quote\" said", tokens[0]);
		assertEquals("the test", tokens[1]);

	}

	public void testSimple11() {
		String line = "\"\"\"a, test, quote\"\" said\",the test";
		CSVFileFormatTokenizer tokeniser = new CSVFileFormatTokenizer(line);

		assertTrue(tokeniser.hasNext());
		assertEquals("\"a, test, quote\" said", tokeniser.next());
		assertTrue(tokeniser.hasNext());
		assertEquals("the test", tokeniser.next());
		assertTrue(!tokeniser.hasNext());

	}

	public void testSimple12() throws ParseException {
		String line = "100,101.1,100000000000,4,4,100";
		CSVFileFormatTokenizer tokeniser = new CSVFileFormatTokenizer(line);



		assertTrue(tokeniser.hasNext());
		assertEquals(100, tokeniser.nextInt());
		assertTrue(tokeniser.hasNext());
		assertEquals(101.1d, tokeniser.nextDouble());
		assertTrue(tokeniser.hasNext());
		assertEquals(100000000000l, tokeniser.nextLong());
		assertTrue(tokeniser.hasNext());
		assertEquals(4, tokeniser.nextByte());
		assertTrue(tokeniser.hasNext());
		assertEquals(4, tokeniser.nextShort());
		assertTrue(tokeniser.hasNext());
		assertEquals(100f, tokeniser.nextFloat());
	}

	public void testSimple13() throws ParseException {
		String line = "100,12/30/06 4:58 PM";
		CSVFileFormatTokenizer tokeniser = new CSVFileFormatTokenizer(line);


		assertTrue(tokeniser.hasNext());
		Number number = tokeniser.nextNumber();
		assertEquals(100, number.intValue());

		assertTrue(tokeniser.hasNext());
		Date date = tokeniser.nextDate();
		assertNotNull(date);

	}

}
