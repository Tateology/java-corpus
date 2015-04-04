/*
 * @(#)JeksExpressionSyntax.java   05/02/99
 *
 * Copyright (c) 1998-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.jeks;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;

/**
 * Localized syntax used by the parser to compile expressions defined in a cell.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksExpressionSyntax extends JeksFunctionSyntax
{
  private Hashtable cellErrors = new Hashtable ();
  private Hashtable messages   = new Hashtable ();

  private char   cellSetSeparator;
  private char   constantChar;

  private char   columnRangeMin = 'A';
  private char   columnRangeMax = 'Z';
  private char   rowRangeMin = '0';
  private char   rowRangeMax = '9';

  // Error values keys available for this syntax
  /**
   * Key for an invalid reference in a cell.
   */
  public final static Integer ERROR_ILLEGAL_CELL     = new Integer (10000);
  public final static Integer ERROR_ILLEGAL_VALUE    = new Integer (10001);
  public final static Integer ERROR_UNKNOWN_NAME     = new Integer (10002);
  public final static Integer ERROR_INVALID_NUMBER   = new Integer (10003);
  public final static Integer ERROR_DIVISION_BY_ZERO = new Integer (10004);
  public final static Integer ERROR_ERR              = new Integer (10005);

  // Jeks functions keys
  public final static Integer JEKS_FUNCTION_IF    = new Integer (20000);

  // Expression messages keys
  public final static Integer MESSAGE_CIRCULARITY_ERROR_TITLE   = new Integer (40000);
  public final static Integer MESSAGE_CIRCULARITY_ERROR_CONFIRM = new Integer (40001);
  public final static Integer MESSAGE_CIRCULARITY_ERROR_INFO    = new Integer (40002);

  /**
   * Creates an instance of <code>JeksExpressionSyntax</code> with default locale.
   */
  public JeksExpressionSyntax ()
  {
    this (Locale.getDefault ());
  }

  /**
   * Creates an instance of <code>JeksExpressionSyntax</code> initialized with the resource bundle
   * <code>com.eteks.jeks.syntax</code>. The syntax may be localized with an appropriate
   * locale. See the default locale file com/eteks/jeks/resources/syntax.properties to see which
   * type of syntax is described by a <code>JeksExpressionSyntax</code> object.
   */
  public JeksExpressionSyntax (Locale locale)
  {
    // All the syntax of the language (operators, functions name, errors, white spaces,...) is stored
    // in a resources bundle (default bundle is in english)
    super (locale);
    ResourceBundle resourceBundle = ResourceBundle.getBundle ("com.eteks.jeks.resources.syntax", locale);

    setResourceString (cellErrors, "ERROR_ILLEGAL_CELL",     ERROR_ILLEGAL_CELL);
    setResourceString (cellErrors, "ERROR_ILLEGAL_VALUE",    ERROR_ILLEGAL_VALUE);
    setResourceString (cellErrors, "ERROR_UNKNOWN_NAME",     ERROR_UNKNOWN_NAME);
    setResourceString (cellErrors, "ERROR_INVALID_NUMBER",   ERROR_INVALID_NUMBER);
    setResourceString (cellErrors, "ERROR_DIVISION_BY_ZERO", ERROR_DIVISION_BY_ZERO);
    setResourceString (cellErrors, "ERROR_ERR",              ERROR_ERR);

    // Add IF common user function
    setResourceJeksFunction ("JEKS_FUNCTION_IF", JEKS_FUNCTION_IF, new JeksFunctionIf ());

    setResourceString (messages, "MESSAGE_CIRCULARITY_ERROR_TITLE",   MESSAGE_CIRCULARITY_ERROR_TITLE);
    setResourceString (messages, "MESSAGE_CIRCULARITY_ERROR_CONFIRM", MESSAGE_CIRCULARITY_ERROR_CONFIRM);
    setResourceString (messages, "MESSAGE_CIRCULARITY_ERROR_INFO",    MESSAGE_CIRCULARITY_ERROR_INFO);

    cellSetSeparator = getResourceChar ("CELL_SET_SEPARATOR");
    constantChar     = getResourceChar ("CONSTANT_CHAR");
    String columnRange = getResourceString ("COLUMN_LETTER_RANGE");
    if (columnRange != null)
    {
      columnRangeMin = Character.toUpperCase (columnRange.charAt (0));
      columnRangeMax = Character.toUpperCase (columnRange.charAt (1));
    }
    String rowRange = getResourceString ("ROW_DIGIT_RANGE");
    if (rowRange != null)
    {
      rowRangeMin = Character.toUpperCase (rowRange.charAt (0));
      rowRangeMax = Character.toUpperCase (rowRange.charAt (1));
    }
  }

  /**
   * Returns the localized string code used to describe the cell error <code>cellErrorKey</code>.
   */
  public String getCellError (Object cellErrorKey)
  {
    return (String)cellErrors.get (cellErrorKey);
  }

  /**
   * Returns a localized error string matching the exception <code>error</code>.
   * @param error an exception thrown at the interpretation time of an expression.
   * @return a localized string or <code>null</code> if error doesn't match any forseen
   *         exception.
   */
  public String getCellError (Throwable error)
  {
    if (error instanceof IllegalCellException)
      return getCellError (ERROR_ILLEGAL_CELL);
    else if (error instanceof IndexOutOfBoundsException)
      // May be thrown while attempting to read a cell out of bounds in the model during computing
      return getCellError (ERROR_UNKNOWN_NAME);
    else if (error instanceof IllegalArgumentException)
      return getCellError (ERROR_ILLEGAL_VALUE);
    else if (error instanceof ArithmeticException)
      return getCellError (ERROR_DIVISION_BY_ZERO);
    else if (error instanceof Exception)
      return getCellError (ERROR_ERR);
    else if (error instanceof StackOverflowError)
      return getCellError (ERROR_ERR);
    else
      return null;
  }

  /**
   * Returns <code>null</code>. This method is overriden to disable the condition
   * operator in an expression. Users may use the user function IF(,,) instead.
   */
  public Object getConditionPartKey (String conditionPart)
  {
    return null;
  }

  /**
   * Returns the localized message matching the key <code>messageKey</code>.
   */
  public String getMessage (Object messageKey)
  {
    String message = (String)messages.get (messageKey);
    return message != null
             ? message
             : super.getMessage (messageKey);
  }

  /**
   * Returns the character used to separate the two cells of a set.
   * @return A seperator character.
   */
  public char getCellSetSeparator ()
  {
    return cellSetSeparator;
  }

  /**
   * Returns the character representing a constant row or column.
   * @return The constant character.
   */
  public char getConstantChar ()
  {
    return constantChar;
  }

  /**
   * Returns <code>true</code> if <code>identifier</code> is a valid identifier
   * for a function or is a cell identifier.
   */
  public boolean isValidIdentifier (String identifier)
  {
    return super.isValidIdentifier (identifier) || isCellIdentifier (identifier);
  }

  /**
   * Returns <code>true</code> if <code>character</code> is a column character.
   * @param character a char to test.
   * @return <code>true</code> if <code>character</code> is a letter in the range returned by the
   *         resource <code>COLUMN_LETTER_RANGE</code>.
   */
  private boolean isColumnLetter (char character)
  {
    character = Character.toUpperCase (character);
    return character >= columnRangeMin && character <= columnRangeMax;
  }

  /**
   * Returns <code>true</code> if <code>character</code> is a row character digit.
   * @param character a char to test.
   * @return <code>true</code> if <code>character</code> is a digit in the range returned by the
   *         resource <code>COLUMN_DIGIT_RANGE</code>.
   */
  private boolean isRowDigit (char character)
  {
    return character >= rowRangeMin && character <= rowRangeMax;
  }

  /**
   * Returns <code>true</code> if <code>identifier</code> represents a cell.
   * The syntax for a single cell is one or more letters followed by a number.
   * A set of cells is separated by the character returned by <code>getCellSetSeparator ()</code>.
   * The letters and the number may be prefixed by the constant char returned by <code>getConstantChar ()</code>.
   */
  public boolean isCellIdentifier (String identifier)
  {
    if (identifier.equals (getCellError (ERROR_ILLEGAL_CELL)))
      return true;

    if (identifier.length () < 2)
      return false;

    int index = 0;
    if (identifier.charAt (0) == getConstantChar ())
      index++;

    if (!isColumnLetter (identifier.charAt (index)))
      return false;
    while (++index < identifier.length () && isColumnLetter (identifier.charAt (index)))
      ;

    if (index == identifier.length ())
      return false;

    if (identifier.charAt (index) == getConstantChar ())
      index++;

    if (index == identifier.length () || !isRowDigit (identifier.charAt (index)))
      return false;
    while (++index < identifier.length () && isRowDigit (identifier.charAt (index)))
      ;

    if (index < identifier.length () - 2 && identifier.charAt (index) == getCellSetSeparator ())
    {
      if (identifier.charAt (++index) == getConstantChar ())
        index++;

      if (!isColumnLetter (identifier.charAt (index)))
        return false;
      while (++index < identifier.length () && isColumnLetter (identifier.charAt (index)))
        ;

      if (index == identifier.length ())
        return false;

      if (identifier.charAt (index) == getConstantChar ())
        index++;

      if (index == identifier.length () || !isRowDigit (identifier.charAt (index)))
        return false;
      while (++index < identifier.length () && isRowDigit (identifier.charAt (index)))
        ;
    }

    return index == identifier.length ();
  }

  /**
   * Returns a cell at the coordinates contained in the string <code>identifier</code>.
   * @param identifier  A string representing a cell (a group of letters followed by a number).
   *                    It may contain <code>JeksExpressionSyntax.getConstantChar ()</code> ('$' character by default).
   * @return  Returns an instance of <code>JeksCell</code> matching <code>indentifier</code>.
   */
  public JeksCell getCellAt (String identifier)
  {
    int firstColumnChar = 0;
    // Skip any leading constant char ('$') character
    if (identifier.charAt (firstColumnChar) == getConstantChar ())
      firstColumnChar++;
    int i = firstColumnChar;
    while (isColumnLetter (identifier.charAt (i)))
      i++;

    int lastColumnChar  = i;
    if (identifier.charAt (i) == getConstantChar ())
      i++;
    // Converts a group of letters to a number using radix (columnRangeMax - columnRangeMin + 1)
    int column = 0;
    for (int k = firstColumnChar; k < lastColumnChar; k++)
      column =   column * (columnRangeMax - columnRangeMin + 1)
               + Character.toUpperCase (identifier.charAt (k)) - columnRangeMin + 1;

    int row;
    try
    {
      row = getNumberFormat ().parse (identifier.substring (i)).intValue () - 1;
    }
    catch (ParseException e)
    {
      throw new IllegalArgumentException (identifier); // Shouldn't happen
    }

    return new JeksCell (row, --column);
  }

  /**
   * Returns a string equivalent to <code>row</code>.
   * @param  row A cell row index.
   * @return The string value of <code>row + 1</code>.
   */
  public String getRowName (int row)
  {
    return getNumberFormat ().format (row + 1);
  }

  /**
   * Returns a string equivalent to <code>column</code>.
   * @param  row A cell column index.
   * @return The string value of <code>column</code> using a group of letters.
   */
  public String getColumnName (int column)
  {
    String result = "";
    for ( ; column >= 0; column = column / (columnRangeMax - columnRangeMin + 1) - 1)
      result = (char)((char)(column % (columnRangeMax - columnRangeMin + 1)) + columnRangeMin) + result;
    return result;
  }

  /**
   * Returns a string equivalent to <code>cell</code>.
   * @param  cell A cell.
   * @return The string value of <code>cell</code> using a group of letters and a number
   */
  public String toString (JeksCell cell)
  {
    return toString (cell.getRow (), false, cell.getColumn (), false);
  }

  /**
   * Returns a string equivalent to <code>cell</code>.
   * @param  cell A cell.
   * @return The string value of <code>cell</code> using a group of letters and a number
   */
  public String toString (int row,    boolean rowConstant,
                          int column, boolean columnConstant)
  {
    if (getConstantChar () != 0)
      return   (columnConstant ? String.valueOf (getConstantChar ()) : "")
             + getColumnName (column)
             + (rowConstant ? String.valueOf (getConstantChar ()) : "")
             + getRowName (row);
    else
      return getColumnName (column) + getRowName (row);
  }

  /**
   * User function IF(,,)
   */
  private class JeksFunctionIf implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_IF);
    }

    public boolean isValidParameterCount (int count)
    {
      return count == 3;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return interpreter.isTrue (parametersValue [0]) ? parametersValue [1] : parametersValue [2];
    }
  }
}

