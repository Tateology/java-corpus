/*
 * @(#)JeksFunctionSyntax.java   05/02/99
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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.parser.CompilationException;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;

/**
 * Localized syntax used by the parser to compile user functions.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksFunctionSyntax extends ResourceSyntax
{
  private Hashtable jeksFunctions    = new Hashtable ();
  private Hashtable messages         = new Hashtable ();

  private Hashtable jeksFunctionKeys = new Hashtable ();

  private NumberFormat numberFormat;
  private DateFormat   dateParser;
  private DateFormat   timeParser;
  private char         quoteCharacter;

  private Locale       locale;

  // Jeks functions keys
  public final static Integer JEKS_FUNCTION_SUM       = new Integer (20001);
  public final static Integer JEKS_FUNCTION_RAND      = new Integer (20002);
  public final static Integer JEKS_FUNCTION_MODULO    = new Integer (20003);
  public final static Integer JEKS_FUNCTION_FACT      = new Integer (20004);
  public final static Integer JEKS_FUNCTION_AND       = new Integer (20005);
  public final static Integer JEKS_FUNCTION_OR        = new Integer (20006);
  public final static Integer JEKS_FUNCTION_TRUE      = new Integer (20007);
  public final static Integer JEKS_FUNCTION_FALSE     = new Integer (20008);
  public final static Integer JEKS_FUNCTION_DATE      = new Integer (20009);
  public final static Integer JEKS_FUNCTION_DATEVALUE = new Integer (20010);
  public final static Integer JEKS_FUNCTION_NOW       = new Integer (20011);
  public final static Integer JEKS_FUNCTION_TIME      = new Integer (20012);
  public final static Integer JEKS_FUNCTION_TIMEVALUE = new Integer (20013);
  public final static Integer JEKS_FUNCTION_YEAR      = new Integer (20014);
  public final static Integer JEKS_FUNCTION_MONTH     = new Integer (20015);
  public final static Integer JEKS_FUNCTION_DAY       = new Integer (20016);
  public final static Integer JEKS_FUNCTION_WEEKDAY   = new Integer (20017);
  public final static Integer JEKS_FUNCTION_HOUR      = new Integer (20018);
  public final static Integer JEKS_FUNCTION_MINUTE    = new Integer (20019);
  public final static Integer JEKS_FUNCTION_SECOND    = new Integer (20020);
  public final static Integer JEKS_FUNCTION_CHAR      = new Integer (20021);
  public final static Integer JEKS_FUNCTION_FIND      = new Integer (20022);
  public final static Integer JEKS_FUNCTION_CODE      = new Integer (20023);

  // Messages keys
  public final static Integer MESSAGE_COMPILATION_ERROR_TITLE                   = new Integer (30000);
  public final static Integer MESSAGE_OPENING_BRACKET_EXPECTED                  = new Integer (30001);
  public final static Integer MESSAGE_INVALID_FUNCTION_NAME                     = new Integer (30002);
  public final static Integer MESSAGE_RESERVED_WORD                             = new Integer (30003);
  public final static Integer MESSAGE_FUNCTION_NAME_ALREADY_EXISTS              = new Integer (30004);
  public final static Integer MESSAGE_CLOSING_BRACKET_EXPECTED                  = new Integer (30005);
  public final static Integer MESSAGE_ASSIGN_OPERATOR_EXPECTED                  = new Integer (30006);
  public final static Integer MESSAGE_INVALID_PARAMETER_NAME                    = new Integer (30007);
  public final static Integer MESSAGE_DUPLICATED_PARAMETER_NAME                 = new Integer (30008);
  public final static Integer MESSAGE_SYNTAX_ERROR                              = new Integer (30009);
  public final static Integer MESSAGE_CLOSING_BRACKET_WITHOUT_OPENING_BRACKET   = new Integer (30010);
  public final static Integer MESSAGE_UNKOWN_IDENTIFIER                         = new Integer (30011);
  public final static Integer MESSAGE_MISSING_PARAMETERS_IN_FUNCTION_CALL       = new Integer (30012);
  public final static Integer MESSAGE_INVALID_PARAMETERS_COUNT_IN_FUNCTION_CALL = new Integer (30013);
  public final static Integer MESSAGE_THEN_OPERATOR_EXPECTED                    = new Integer (30014);
  public final static Integer MESSAGE_ELSE_OPERATOR_EXPECTED                    = new Integer (30015);
  public final static Integer MESSAGE_THEN_OPERATOR_WITHOUT_IF_OPERATOR         = new Integer (30016);
  public final static Integer MESSAGE_ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS   = new Integer (30017);
  public final static Integer MESSAGE_INVALID_JEKS_FILE                         = new Integer (30100);

  /**
   * Creates an instance of <code>JeksFunctionSyntax</code> using default locale.
   */
  public JeksFunctionSyntax ()
  {
    this (Locale.getDefault ());
  }

  /**
   * Creates an instance of <code>JeksFunctionSyntax</code> initialized with the resource bundle
   * <code>com.eteks.jeks.syntax</code>. The syntax may be localized with an appropriate
   * locale. See the default locale file com/eteks/jeks/resources/syntax.properties to see which
   * type of syntax is described by a <code>JeksFunctionSyntax</code> object.
   */
  public JeksFunctionSyntax (Locale locale)
  {
    // All the syntax of the language (interpreter, functions name, errors, white spaces,...) is stored
    // in a resources bundle (default bundle is in english)
    super (ResourceBundle.getBundle ("com.eteks.jeks.resources.syntax", locale));

    this.locale = locale;

    // Add common user function
    setResourceJeksFunction ("JEKS_FUNCTION_SUM",       JEKS_FUNCTION_SUM,       new JeksFunctionSum ());
    setResourceJeksFunction ("JEKS_FUNCTION_RAND",      JEKS_FUNCTION_RAND,      new JeksFunctionRand ());
    setResourceJeksFunction ("JEKS_FUNCTION_MODULO",    JEKS_FUNCTION_MODULO,    new JeksFunctionMod ());
    setResourceJeksFunction ("JEKS_FUNCTION_FACT",      JEKS_FUNCTION_FACT,      new JeksFunctionFact ());
    setResourceJeksFunction ("JEKS_FUNCTION_AND",       JEKS_FUNCTION_AND,       new JeksFunctionAnd ());
    setResourceJeksFunction ("JEKS_FUNCTION_OR",        JEKS_FUNCTION_OR,        new JeksFunctionOr ());
    setResourceJeksFunction ("JEKS_FUNCTION_TRUE",      JEKS_FUNCTION_TRUE,      new JeksFunctionTrue ());
    setResourceJeksFunction ("JEKS_FUNCTION_FALSE",     JEKS_FUNCTION_FALSE,     new JeksFunctionFalse ());
    setResourceJeksFunction ("JEKS_FUNCTION_DATE",      JEKS_FUNCTION_DATE,      new JeksFunctionDate ());
    setResourceJeksFunction ("JEKS_FUNCTION_DATEVALUE", JEKS_FUNCTION_DATEVALUE, new JeksFunctionDateValue ());
    setResourceJeksFunction ("JEKS_FUNCTION_NOW",       JEKS_FUNCTION_NOW,       new JeksFunctionNow ());
    setResourceJeksFunction ("JEKS_FUNCTION_TIME",      JEKS_FUNCTION_TIME,      new JeksFunctionTime ());
    setResourceJeksFunction ("JEKS_FUNCTION_TIMEVALUE", JEKS_FUNCTION_TIMEVALUE, new JeksFunctionTimeValue ());
    setResourceJeksFunction ("JEKS_FUNCTION_YEAR",      JEKS_FUNCTION_YEAR,      new JeksFunctionYear ());
    setResourceJeksFunction ("JEKS_FUNCTION_MONTH",     JEKS_FUNCTION_MONTH,     new JeksFunctionMonth ());
    setResourceJeksFunction ("JEKS_FUNCTION_DAY",       JEKS_FUNCTION_DAY,       new JeksFunctionDay ());
    setResourceJeksFunction ("JEKS_FUNCTION_WEEKDAY",   JEKS_FUNCTION_WEEKDAY,   new JeksFunctionWeekDay ());
    setResourceJeksFunction ("JEKS_FUNCTION_HOUR",      JEKS_FUNCTION_HOUR,      new JeksFunctionHour ());
    setResourceJeksFunction ("JEKS_FUNCTION_MINUTE",    JEKS_FUNCTION_MINUTE,    new JeksFunctionMinute ());
    setResourceJeksFunction ("JEKS_FUNCTION_SECOND",    JEKS_FUNCTION_SECOND,    new JeksFunctionSecond ());
    setResourceJeksFunction ("JEKS_FUNCTION_CHAR",      JEKS_FUNCTION_CHAR,      new JeksFunctionChar ());
    setResourceJeksFunction ("JEKS_FUNCTION_FIND",      JEKS_FUNCTION_FIND,      new JeksFunctionFind ());
    setResourceJeksFunction ("JEKS_FUNCTION_CODE",      JEKS_FUNCTION_CODE,      new JeksFunctionCode ());

    setResourceString (messages, "MESSAGE_COMPILATION_ERROR_TITLE",                   MESSAGE_COMPILATION_ERROR_TITLE);
    setResourceString (messages, "MESSAGE_OPENING_BRACKET_EXPECTED",                  MESSAGE_OPENING_BRACKET_EXPECTED);
    setResourceString (messages, "MESSAGE_INVALID_FUNCTION_NAME",                     MESSAGE_INVALID_FUNCTION_NAME);
    setResourceString (messages, "MESSAGE_RESERVED_WORD",                             MESSAGE_RESERVED_WORD);
    setResourceString (messages, "MESSAGE_FUNCTION_NAME_ALREADY_EXISTS",              MESSAGE_FUNCTION_NAME_ALREADY_EXISTS);
    setResourceString (messages, "MESSAGE_CLOSING_BRACKET_EXPECTED",                  MESSAGE_CLOSING_BRACKET_EXPECTED);
    setResourceString (messages, "MESSAGE_ASSIGN_OPERATOR_EXPECTED",                  MESSAGE_ASSIGN_OPERATOR_EXPECTED);
    setResourceString (messages, "MESSAGE_INVALID_PARAMETER_NAME",                    MESSAGE_INVALID_PARAMETER_NAME);
    setResourceString (messages, "MESSAGE_DUPLICATED_PARAMETER_NAME",                 MESSAGE_DUPLICATED_PARAMETER_NAME);
    setResourceString (messages, "MESSAGE_SYNTAX_ERROR",                              MESSAGE_SYNTAX_ERROR);
    setResourceString (messages, "MESSAGE_CLOSING_BRACKET_WITHOUT_OPENING_BRACKET",   MESSAGE_CLOSING_BRACKET_WITHOUT_OPENING_BRACKET);
    setResourceString (messages, "MESSAGE_UNKOWN_IDENTIFIER",                         MESSAGE_UNKOWN_IDENTIFIER);
    setResourceString (messages, "MESSAGE_MISSING_PARAMETERS_IN_FUNCTION_CALL",       MESSAGE_MISSING_PARAMETERS_IN_FUNCTION_CALL);
    setResourceString (messages, "MESSAGE_INVALID_PARAMETERS_COUNT_IN_FUNCTION_CALL", MESSAGE_INVALID_PARAMETERS_COUNT_IN_FUNCTION_CALL);
    setResourceString (messages, "MESSAGE_THEN_OPERATOR_EXPECTED",                    MESSAGE_THEN_OPERATOR_EXPECTED);
    setResourceString (messages, "MESSAGE_ELSE_OPERATOR_EXPECTED",                    MESSAGE_ELSE_OPERATOR_EXPECTED);
    setResourceString (messages, "MESSAGE_THEN_OPERATOR_WITHOUT_IF_OPERATOR",         MESSAGE_THEN_OPERATOR_WITHOUT_IF_OPERATOR);
    setResourceString (messages, "MESSAGE_ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS",   MESSAGE_ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS);
    setResourceString (messages, "MESSAGE_INVALID_JEKS_FILE",                         MESSAGE_INVALID_JEKS_FILE);

    numberFormat = NumberFormat.getInstance (locale);
    // Forbid grouping character
    numberFormat.setGroupingUsed (false);
    dateParser = DateFormat.getDateInstance (DateFormat.SHORT);
    timeParser = DateFormat.getTimeInstance (DateFormat.SHORT);

    quoteCharacter = getResourceChar (getResourceBundle (), "QUOTE_CHAR");
  }

  /**
   * Returns the locale of this syntax.
   */
  public Locale getLocale ()
  {
    return locale;
  }

  protected void setResourceString (Hashtable table,
                                    String    resourceName,
                                    Object    key)
  {
    try
    {
      table.put (key, getResourceBundle ().getString (resourceName));
    }
    catch (MissingResourceException e)
    {  }
  }

  /**
   * Returns the localized message matching the key <code>messageKey</code> (one of the
   * <code>MESSAGE_...</code> constants).
   */
  public String getMessage (Object messageKey)
  {
    return (String)messages.get (messageKey);
  }

  /**
   * Returns the localized message matching the exception.
   */
  public String getExceptionMessage (CompilationException exception)
  {
    switch (exception.getErrorNumber ())
    {
      case CompilationException.OPENING_BRACKET_EXPECTED :
        return getMessage (MESSAGE_OPENING_BRACKET_EXPECTED);
      case CompilationException.INVALID_FUNCTION_NAME :
        return getMessage (MESSAGE_INVALID_FUNCTION_NAME);
      case CompilationException.RESERVED_WORD :
        return getMessage (MESSAGE_RESERVED_WORD);
      case CompilationException.FUNCTION_NAME_ALREADY_EXISTS :
        return getMessage (MESSAGE_FUNCTION_NAME_ALREADY_EXISTS);
      case CompilationException.CLOSING_BRACKET_EXPECTED :
        return getMessage (MESSAGE_CLOSING_BRACKET_EXPECTED);
      case CompilationException.ASSIGN_OPERATOR_EXPECTED :
        return getMessage (MESSAGE_ASSIGN_OPERATOR_EXPECTED);
      case CompilationException.INVALID_PARAMETER_NAME :
        return getMessage (MESSAGE_INVALID_PARAMETER_NAME);
      case CompilationException.DUPLICATED_PARAMETER_NAME :
        return getMessage (MESSAGE_DUPLICATED_PARAMETER_NAME);
      case CompilationException.SYNTAX_ERROR :
        return getMessage (MESSAGE_SYNTAX_ERROR);
      case CompilationException.CLOSING_BRACKET_WITHOUT_OPENING_BRACKET :
        return getMessage (MESSAGE_CLOSING_BRACKET_WITHOUT_OPENING_BRACKET);
      case CompilationException.UNKOWN_IDENTIFIER :
        return getMessage (MESSAGE_UNKOWN_IDENTIFIER);
      case CompilationException.MISSING_PARAMETERS_IN_FUNCTION_CALL :
        return getMessage (MESSAGE_MISSING_PARAMETERS_IN_FUNCTION_CALL);
      case CompilationException.THEN_OPERATOR_EXPECTED :
        return getMessage (MESSAGE_THEN_OPERATOR_EXPECTED);
      case CompilationException.ELSE_OPERATOR_EXPECTED :
        return getMessage (MESSAGE_ELSE_OPERATOR_EXPECTED);
      case CompilationException.THEN_OPERATOR_WITHOUT_IF_OPERATOR :
        return getMessage (MESSAGE_THEN_OPERATOR_WITHOUT_IF_OPERATOR);
      case CompilationException.ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS :
        return getMessage (MESSAGE_ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS);
      default :
        return null;
    }
  }

  protected void setResourceJeksFunction (String   resourceName,
                                          Object   key,
                                          Function function)
  {
    try
    {
      String jeksFunction = getResourceBundle ().getString (resourceName);
      setJeksFunctionKey (jeksFunction, key);
      addFunction (function);
    }
    catch (MissingResourceException e)
    {  }
  }

  private void setJeksFunctionKey (String jeksFunction, Object jeksFunctionKey)
  {
    if (!isCaseSensitive ())
      jeksFunction = jeksFunction.toUpperCase ();
    jeksFunctions.put (jeksFunctionKey, jeksFunction);
    jeksFunctionKeys.put (jeksFunction, jeksFunctionKey);
  }

  /**
   * Returns the key matching the Jeks function of name <code>jeksFunction</code>. Jeks
   * functions may be localized.
   */
  public Object getJeksFunctionKey (String jeksFunction)
  {
    return jeksFunctionKeys.get (isCaseSensitive () ? jeksFunction : jeksFunction.toUpperCase ());
  }

  /**
   * Returns the Jeks function name matching the key <code>jeksFunctionKey</code>
   * (one of the <code>JEKS_FUNCTION_...</code> constants). Jeks functions may be localized.
   */
  public String getJeksFunction (Object jeksFunctionKey)
  {
    return (String)jeksFunctions.get (jeksFunctionKey);
  }

  /**
   * Returns the value of the literal parsed from the string <code>expression</code>
   * or <code>null</code> if <code>expression</code> doesn't start with a literal. If a
   * literal is found at the beginning of <code>expression</code>, this method extracts
   * the parsed literal in the string buffer <code>extractedLiteral</code>.
   * The extracted literal is a number, a string or a character. Numbers are parsed with
   * a localized number parser.
   * @param  expression       the string to parse.
   * @param  extractedLiteral the literal extracted from <code>expression</code> identified
   *                          as a valid literal with the syntax of the parser.
   * @return the value of the extracted literal or <code>null</code> if <code>expression</code>
   *         doesn't start with a valid literal. If the literal is a number,
   *         an instance of <code>Number</code> is returned, if the literal is a string
   *         an instance of <code>String</code> is returned.
   */
  public Object getLiteral (String       expression,
                            StringBuffer extractedLiteral)
  {
    Object literal = getLiteralNumber (expression, extractedLiteral);
    if (literal != null)
      return literal;
    else
      return getLiteralString (expression, extractedLiteral);
  }

  protected Number getLiteralNumber (String       expression,
                                     StringBuffer extractedNumber)
  {
    ParsePosition position = new ParsePosition (0);
    // Try to store a number (Long or Double)
    Number value = (Number)numberFormat.parse (expression, position);
    if (value != null)
      extractedNumber.append (expression.substring (0, position.getIndex ()));

    return value;
  }

  /**
   * Extracts the quoted string starting at the index <code>definitionIndex</code>
   * from the expression <code>expressionDefinition</code> in the string buffer
   * <code>extractedLexical</code>.
   * @param expressionDefinition the string to parse.
   * @param extractedLexical     the quoted string extracted from <code>expressionDefinition</code> identified
   *                             as a string with the syntax of the parser. This string may contain
   *                             some escape sequence.
   * @see #getQuoteCharacter
   */
  protected String getLiteralString (String       expression,
                                     StringBuffer extractedLexical)
  {
    char quoteChar = expression.charAt (0);
    if (   getQuoteCharacter () == quoteChar
        && expression.length () >= 2)
    {
      int  index = 1;
      char c;
      StringBuffer literalString = new StringBuffer ();
      while (index < expression.length ())
        if ((c = expression.charAt (index)) != quoteChar)
        {
          literalString.append (c);
          index++;
        }
        else if (   index + 1 < expression.length ()
                 && expression.charAt (index + 1) == quoteChar)
        {
          literalString.append (c);
          index += 2;
        }
        else
          break;

      if (index < expression.length ())
      {
        extractedLexical.append (expression.substring (0, index + 1));
        return literalString.toString ();
      }
    }

    return null;
  }

  /**
   * Returns the char used to quote string litterals.
   * @return <code>0</code> by default.
   */
  public char getQuoteCharacter ()
  {
    return quoteCharacter;
  }

  /**
   * Returns the number format instance used to format and parse numbers.
   * @return an instance of <code>NumberFormat</code>.
   */
  public NumberFormat getNumberFormat ()
  {
    return numberFormat;
  }

  /**
   * Returns <code>true</code> if <code>identifier</code> is a correct name of function
   * or parameter. <code>identifier</code> may contain letters, digits, or the character _.
   * Its first character can't be a digit.
   * @param identifier the string to test.
   * @return <code>true</code> if <code>identifier</code> is a correct identifier.
   */
  public boolean isValidIdentifier (String identifier)
  {
    if (   !Character.isLetter (identifier.charAt (0))
        && identifier.charAt (0) != '_')
      return false;
    for (int i = 1; i < identifier.length (); i++)
      if (   !Character.isLetter (identifier.charAt (i))
          && !Character.isDigit (identifier.charAt (i))
          && identifier.charAt (i) != '_')
        return false;
    return true;
  }

  // Math functions
  private class JeksFunctionSum implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_SUM);
    }

    public boolean isValidParameterCount (int count)
    {
      return count > 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      double somme = 0.;
      for (int i = 0; i < parametersValue.length; i++)
        if (parametersValue [i] instanceof Object [][])
        {
          Object [][] values = (Object [][])parametersValue [i];
          for (int row = 0; row < values.length; row++)
            for (int column = 0; column < values [row].length; column++)
            {
              Object value = values [row][column];
              if (   value != null
                  && !"".equals (value)
                  && value instanceof Number)
                somme += ((Number)value).doubleValue ();
            }
        }
        else
          somme += parametersValue [i] instanceof Number
                     ? ((Number)parametersValue [i]).doubleValue ()
                     : 0;

      if (somme == Math.floor (somme))
        return new Integer ((int)somme);
      else
        return new Double (somme);
    }
  }

  private class JeksFunctionRand implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_RAND);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return new Double (Math.random ());
    }
  }

  private class JeksFunctionMod implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_MODULO);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      double dividend = parametersValue [0] instanceof Number
                          ? ((Number)parametersValue [0]).doubleValue ()
                          : 0;
      double divisor  = parametersValue [1] instanceof Number
                          ? ((Number)parametersValue [1]).doubleValue ()
                          : 0;
      if (divisor == 0)
        throw new IllegalArgumentException ();
      double modulo = dividend - divisor * (int)(dividend / divisor);
      // If dividend and divisor are not of the same sign, add divisor
      if (   dividend < 0 && divisor > 0
          || dividend > 0 && divisor < 0)
        modulo += divisor;
      return new Double (modulo);
    }
  }

  private class JeksFunctionFact implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_FACT);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      double number = parametersValue [0] instanceof Number
                          ? ((Number)parametersValue [0]).doubleValue ()
                          : 0;
      if (number < 0)
        return new Double (Double.NaN);
      number = Math.floor (number);
      double result = 1;
      while (number > 0 && !Double.isInfinite (result))
        result *= number--;
      return new Double (result);
    }
  }

  // Logical functions
  private class JeksFunctionAnd implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_AND);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount > 0; // At least one parameter
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      Boolean result = Boolean.TRUE;
      for (int i = 0; result.booleanValue () && i < parametersValue.length; i++)
        // Still true only if value is different of 0 or is true
        if (!interpreter.isTrue (parametersValue [i]))
          result = Boolean.FALSE;
      return result;
    }
  }

  private class JeksFunctionOr implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_OR);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount > 0; // At least one parameter
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      Boolean result = Boolean.FALSE;
      for (int i = 0; !result.booleanValue () && i < parametersValue.length; i++)
        // Is true only if value is different of 0 or is true
        if (interpreter.isTrue (parametersValue [i]))
          result = Boolean.TRUE;
      return result;
    }
  }

  private class JeksFunctionTrue implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_TRUE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return Boolean.TRUE;
    }
  }

  private class JeksFunctionFalse implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_FALSE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return Boolean.FALSE;
    }
  }

  // Date and time functions classes
  // AUJOURDHUI() == NOW ?
  private class JeksFunctionDate implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_DATE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 3;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      int year  = parametersValue [0] instanceof Number ? ((Number)parametersValue [0]).intValue () : 1900;
      int month = parametersValue [1] instanceof Number ? ((Number)parametersValue [1]).intValue () : 1;
      int day   = parametersValue [2] instanceof Number ? ((Number)parametersValue [2]).intValue () : 1;
      Calendar calendar = Calendar.getInstance ();
      calendar.set (year, month - 1, day, 0, 0, 0);
      return calendar.getTime ();
    }
  }

  private class JeksFunctionDateValue implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_DATEVALUE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      try
      {
        if ((parametersValue [0] instanceof String))
          return dateParser.parse ((String)parametersValue [0]);
      }
      catch (ParseException e)
      { }
      throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionNow implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_NOW);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return new Date ();
    }
  }

  private class JeksFunctionTime implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_TIME);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 3;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      int hour   = parametersValue [0] instanceof Number ? ((Number)parametersValue [0]).intValue () : 0;
      int minute = parametersValue [1] instanceof Number ? ((Number)parametersValue [1]).intValue () : 0;
      int second = parametersValue [2] instanceof Number ? ((Number)parametersValue [2]).intValue () : 0;
      Calendar calendar = Calendar.getInstance ();
      calendar.set (Calendar.HOUR,   hour);
      calendar.set (Calendar.MINUTE, minute);
      calendar.set (Calendar.SECOND, second);
      return calendar.getTime ();
    }
  }

  private class JeksFunctionTimeValue implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_TIMEVALUE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      try
      {
        if ((parametersValue [0] instanceof String))
          // Should check time format
          return timeParser.parse ((String)parametersValue [0]);
      }
      catch (ParseException e)
      { }
      throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionYear implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_YEAR);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.YEAR));
      }
      else if (parametersValue [0] == null)
        return new Integer (1900);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionMonth implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_MONTH);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.MONTH) + 1);
      }
      else if (parametersValue [0] == null)
        return new Integer (1);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionDay implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_DAY);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.DATE));
      }
      else if (parametersValue [0] == null)
        return new Integer (0);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionWeekDay implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_WEEKDAY);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.DAY_OF_WEEK));
      }
      else if (parametersValue [0] == null)
        return new Integer (7);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionHour implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_HOUR);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.HOUR_OF_DAY));
      }
      else if (parametersValue [0] == null)
        return new Integer (0);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionMinute implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_MINUTE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.MINUTE));
      }
      else if (parametersValue [0] == null)
        return new Integer (0);
      else
        throw new IllegalArgumentException ();
    }
  }

  private class JeksFunctionSecond implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_SECOND);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] instanceof Date)
      {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime ((Date)parametersValue [0]);
        return new Integer (calendar.get (Calendar.SECOND));
      }
      else if (parametersValue [0] == null)
        return new Integer (0);
      else
        throw new IllegalArgumentException ();
    }
  }

  // Text functions classes
  private class JeksFunctionChar implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_CHAR);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (   !(parametersValue [0] instanceof Number)
          || ((Number)parametersValue [0]).doubleValue () < 0)
        throw new IllegalArgumentException ();
      return new Character ((char)((Number)parametersValue [0]).intValue ());
    }
  }

  private class JeksFunctionFind implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_FIND);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount >= 2 && parameterCount <= 3;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      String searchedString = parametersValue [0] != null ? parametersValue [0].toString () : "";
      String parsedString   = parametersValue [1] != null ? parametersValue [1].toString () : "";
      int    index          = parametersValue.length == 2
                                ? 1
                                : (parametersValue [2] instanceof Number
                                     ? ((Number)parametersValue [2]).intValue ()
                                     : -1);
      if (   parsedString.length () == 0
          || index < 0
          || index > parsedString.length ())
        throw new IllegalArgumentException ();
      else
      {
        int searchIndex = parsedString.indexOf (searchedString.toUpperCase (), index - 1);
        if (searchIndex == -1)
          throw new IllegalArgumentException ();
        else
          return new Integer (searchIndex + 1);
      }
    }
  }

  private class JeksFunctionCode implements Function
  {
    public String getName ()
    {
      return getJeksFunction (JEKS_FUNCTION_CODE);
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 1;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (parametersValue [0] == null)
        throw new IllegalArgumentException ();
      String string = parametersValue [0].toString ();
      return new Integer (string.charAt (0));
    }
  }
}

