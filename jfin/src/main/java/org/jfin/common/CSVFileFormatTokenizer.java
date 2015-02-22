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

package org.jfin.common;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to parse a single line of a .csv file and break it down into a series
 * of tokens.
 */
public class CSVFileFormatTokenizer
{
	String[] tokens;

	int point;

	NumberFormat nf = NumberFormat.getNumberInstance();

	DateFormat df = DateFormat.getInstance();

	/**
	 * Constructor
	 *
	 * @param input
	 *            A String representing a line of CSV Formatted text
	 */
	public CSVFileFormatTokenizer(String input)
	{
		initialise(input);
	}

	private String[] split(String in) {
		List<String> split = new ArrayList<String>();
		String hold = "";

		for(int i=0;i<in.length();i++) {
			char c = in.charAt(i);
			if(c==',') {
				split.add(hold);
				hold="";
			} else {
				hold+=c;
			}
		}


		split.add(hold);


		String[] splitArray = new String[split.size()];

		for(int i=0;i<split.size();i++) {
			splitArray[i] = split.get(i);
		}
		return splitArray;
	}

	/**
	 * Initialises the CSVFormat object ready for parsing the new input string.
	 *
	 * @param input
	 *            A String representing a line of CSV Formatted text
	 */
	public void initialise(String input)
	{
		String[] holdTokens = split(input);

		List<String> collectedTokens = new ArrayList<String>();

		String lastToken = null;
		boolean inQuotes = false;

		for(int i=0;i<holdTokens.length;i++) {
			String currentToken = holdTokens[i];
			if(inQuotes) {
				if(currentToken.endsWith("\"")) {
					lastToken+=","+currentToken.substring(0,currentToken.length()-1);
					collectedTokens.add(lastToken);
					inQuotes = false;
				} else {
					lastToken+=","+currentToken;
				}
			} else {
				if(currentToken.startsWith("\"")) {
					if(currentToken.equals("\"")) {
						lastToken = "";
						inQuotes = true;
					} else if(currentToken.endsWith("\"")) {
						collectedTokens.add(currentToken.substring(1,currentToken.length()-1));
					} else {
						lastToken = currentToken.substring(1);
						inQuotes = true;
					}
				} else {
					collectedTokens.add(currentToken);
				}
			}
		}

		tokens = new String[collectedTokens.size()];

		for(int i=0;i<collectedTokens.size();i++) {
			tokens[i] = collectedTokens.get(i).replaceAll("\"\"","\"");
		}

		point = 0;
	}

	/**
	 * Used to find if any more tokens are left to be parsed from the input
	 * String.
	 *
	 * @return true if there are more tokens, otherwise false
	 */
	public boolean hasNext()
	{
		return point < tokens.length;
	}

	/**
	 * Used to retrive the next token (or null if there are no more) as a
	 * String. <p/> If the original token was surrounded by quotes, for example
	 * if it contained commas, then the quotes are stripped.
	 *
	 * @return the next token as a String
	 */
	public String next()
	{
		return tokens[point++];
	}

	/**
	 * Utility static method to allow the quick conversion of a CSV formatted
	 * line into an array of Strings.
	 *
	 * @param line
	 *            The CSV Formatted line to be parsed
	 * @return an array of String containing the tokens
	 */
	public static String[] getTokens(String line)
	{
		CSVFileFormatTokenizer tokenizer = new CSVFileFormatTokenizer(line);

		return tokenizer.getTokens();
	}

	/**
	 * Utility method to allow quick conversion into an array of Strings.
	 *
	 * @return an array of String containing the tokens
	 */

	public String[] getTokens()
	{
		return tokens;
	}

	/**
	 * @return the next token as a Number
	 * @throws ParseException
	 */
	public Number nextNumber() throws ParseException
	{
		return nextNumber(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a Number
	 * @throws ParseException
	 */
	public Number nextNumber(NumberFormat numberFormat) throws ParseException
	{
		return numberFormat.parse(next());
	}

	/**
	 * @return the next token as a double
	 * @throws ParseException
	 */
	public double nextDouble() throws ParseException
	{
		return nextDouble(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a double
	 * @throws ParseException
	 */
	public double nextDouble(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).doubleValue();
	}

	/**
	 * @return the next token as an int
	 * @throws ParseException
	 */
	public int nextInt() throws ParseException
	{
		return nextInt(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as an int
	 * @throws ParseException
	 */
	public int nextInt(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).intValue();
	}

	/**
	 * @return the next token as a long
	 * @throws ParseException
	 */
	public long nextLong() throws ParseException
	{
		return nextLong(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a long
	 * @throws ParseException
	 */
	public long nextLong(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).longValue();
	}

	/**
	 * @return the next token as a byte
	 * @throws ParseException
	 */
	public byte nextByte() throws ParseException
	{
		return nextByte(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a byte
	 * @throws ParseException
	 */
	public byte nextByte(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).byteValue();
	}

	/**
	 * @return the next token as a short
	 * @throws ParseException
	 */
	public short nextShort() throws ParseException
	{
		return nextShort(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a short
	 * @throws ParseException
	 */
	public short nextShort(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).shortValue();
	}

	/**
	 * @return the next token as a float
	 * @throws ParseException
	 */
	public float nextFloat() throws ParseException
	{
		return nextFloat(nf);
	}

	/**
	 * @param numberFormat
	 *            The format to use to process the token
	 * @return the next token as a float
	 * @throws ParseException
	 */
	public float nextFloat(NumberFormat numberFormat) throws ParseException
	{
		return nextNumber(numberFormat).floatValue();
	}

	/**
	 * @return the next token as a Date
	 * @throws ParseException
	 */
	public Date nextDate() throws ParseException
	{
		return nextDate(df);
	}

	/**
	 * @param dateFormat
	 *            The format to use to process the token
	 * @return the next token as a Date
	 * @throws ParseException
	 */
	public Date nextDate(DateFormat dateFormat) throws ParseException
	{
		return dateFormat.parse(next());
	}
}
