/*
 * @(#)DefaultSyntax.java   02/13/98
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
package com.eteks.parser;

/**
 * Default syntax used by parsers for functions and expressions. This syntax supports
 * the following operators, constants and functions :
 * <UL><LI>Unary operators : <code>+ -</code></LI>
 *     <LI>Binary operators : <code>^ * / MOD + -</code> (from the highest to the lowest priority)</LI>
 *     <LI>No condition</LI>
 *     <LI>Constants : <code>PI FALSE TRUE</code></LI>
 *     <LI>Functions : <code>LN LOG EXP SQR SQRT COS SIN TAN ACOS ASIN ATAN COSH SINH TANH INT
 *                     ABS OPP</code></LI></UL>
 * The literals may be any decimal or integer number.
 * The operator for assignment is <code>=</code>, brackets <code>( )</code> and
 * the separator of parameters <code>,</code>.<br>
 * The parsed identifiers (name of a function and its parameters) may contain
 * letters, digits, or the character _. The first character can't be a digit.<br>
 * This syntax isn't case sensitive. A <code>DefaultSyntax</code> object
 * may be modified with the methods of <code>AbstractSyntax</code>.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 */
public class DefaultSyntax extends AbstractSyntax
{
  /**
   * Creates a default syntax.
   */
  public DefaultSyntax ()
  {
    setConstantKey ("PI",    CONSTANT_PI);
    setConstantKey ("TRUE",  CONSTANT_TRUE);
    setConstantKey ("FALSE", CONSTANT_FALSE);

    setUnaryOperatorKey ("+", OPERATOR_POSITIVE);
    setUnaryOperatorKey ("-", OPERATOR_OPPOSITE);

    setBinaryOperatorKey ("+",   OPERATOR_ADD);
    setBinaryOperatorKey ("-",   OPERATOR_SUBSTRACT);
    setBinaryOperatorKey ("*",   OPERATOR_MULTIPLY);
    setBinaryOperatorKey ("/",   OPERATOR_DIVIDE);
    setBinaryOperatorKey ("^",   OPERATOR_POWER);
    setBinaryOperatorKey ("MOD", OPERATOR_MODULO);

    setCommonFunctionKey ("LN",   FUNCTION_LN);
    setCommonFunctionKey ("LOG",  FUNCTION_LOG);
    setCommonFunctionKey ("EXP",  FUNCTION_EXP);
    setCommonFunctionKey ("SQR",  FUNCTION_SQR);
    setCommonFunctionKey ("SQRT", FUNCTION_SQRT);
    setCommonFunctionKey ("COS",  FUNCTION_COS);
    setCommonFunctionKey ("SIN",  FUNCTION_SIN);
    setCommonFunctionKey ("TAN",  FUNCTION_TAN);
    setCommonFunctionKey ("ACOS", FUNCTION_ACOS);
    setCommonFunctionKey ("ASIN", FUNCTION_ASIN);
    setCommonFunctionKey ("ATAN", FUNCTION_ATAN);
    setCommonFunctionKey ("COSH", FUNCTION_COSH);
    setCommonFunctionKey ("SINH", FUNCTION_SINH);
    setCommonFunctionKey ("TANH", FUNCTION_TANH);
    setCommonFunctionKey ("INT",  FUNCTION_INTEGER);
    setCommonFunctionKey ("ABS",  FUNCTION_ABS);
    setCommonFunctionKey ("OPP",  FUNCTION_OPPOSITE);

    setBinaryOperatorPriority (OPERATOR_ADD,              1);
    setBinaryOperatorPriority (OPERATOR_SUBSTRACT,        1);
    setBinaryOperatorPriority (OPERATOR_MODULO,           2);
    setBinaryOperatorPriority (OPERATOR_DIVIDE,           2);
    setBinaryOperatorPriority (OPERATOR_MULTIPLY,         2);
    setBinaryOperatorPriority (OPERATOR_POWER,            3);

    setAssignmentOperator ("=");
    setWhiteSpaceCharacters (" \t\n\r");
    setOpeningBracket ('(');
    setClosingBracket (')');
    setParameterSeparator (',');
  }

  /**
   * Returns the value of the number parsed from the string <code>expression</code>
   * or <code>null</code> if <code>expression</code> doesn't start with a number. If a
   * number is found at the beginning of <code>expression</code>, this method extracts
   * it in the string buffer <code>extractedLiteral</code>.
   * @param  expression       the string to parse.
   * @param  extractedLiteral the literal extracted from <code>expression</code> identified
   *                          as a valid number with the <code>getLiteralNumber ()</code> method
   *                          of the <code>AbstractSyntax</code> class. This string buffer
   *                          is emptied before the call of this method by the parser.
   * @return the value of the extracted number or <code>null</code> if <code>expression</code>
   *         doesn't start with a number.
   * @see com.eteks.parser.AbstractSyntax#getLiteralNumber
   */
  public Object getLiteral (String       expression,
                            StringBuffer extractedLiteral)
  {
    return getLiteralNumber (expression, extractedLiteral);
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
    if (   !isLetter (identifier.charAt (0))
        && identifier.charAt (0) != '_')
      return (false);
    for (int i = 1; i < identifier.length (); i++)
      if (   !isLetter (identifier.charAt (i))
          && !isDigit (identifier.charAt (i))
          && identifier.charAt (i) != '_')
        return (false);

    return (true);
  }

  /**
   * Returns <code>true</code> if <code>character</code> is a letter.
   * By default letters are limited to ASCII letters (a to z or A to Z). Override this method
   * to have a syntax with a richer set of letters.
   * @param character a char to test.
   * @return <code>true</code> if <code>character</code> is an ASCII letter (a to z or A to Z).
   */
  public boolean isLetter (char character)
  {
    return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z';
  }

  /**
   * Returns <code>true</code> if <code>character</code> is a digit.
   * By default digits are limited to ASCII digits (1 to 9). Override this method
   * to have a syntax with a richer set of digits.
   * @param character a char to test.
   * @return <code>true</code> if <code>character</code> is an ASCII digit (1 to 9).
   */
  public boolean isDigit (char character)
  {
    return character >= '0' && character <= '9';
  }
}

