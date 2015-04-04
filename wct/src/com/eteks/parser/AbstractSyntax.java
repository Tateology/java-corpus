/*
 * @(#)AbstractSyntax.java   01/01/98
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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Class that helps to define the constants, operators, functions and other required elements
 * of a class implementing the <code>Syntax</code> interface.
 * This class implements all the methods of <code>Syntax</code> except the 
 * <code>isValidIdentifier (String identifier)</code> method.<br>
 * All the elements of a syntax returned by the getter methods of the <code>Syntax</code> interface are
 * stored in internal fields that can be set with the setter respective methods of this class. Examples :
 * <UL><LI><code>setBinaryOperatorKey ("+", OPERATOR_ADD)</code> associates the lexical
 *     <code>+</code> to the key <code>OPERATOR_ADD</code> of a syntax, so the <code>getBinaryOperatorKey ("+")</code>
 *     method will return the key <code>OPERATOR_ADD</code> for the <code>+</code> lexical at parsing time
 *     (all the constants, operators and functions are stored in different hashtables associating a
 *     string to a syntactic key).</LI>
 *     <LI><code>setOpeningBracket ('(')</code> stores the character <code>(</code>, that will be returned
 *     by the <code>getOpeningBracket ()</code> method.</LI></UL>
 * The <code>getDelimiters ()</code> method is implemented in such a way it returns a string containing 
 * the white spaces, the brackets, the parameter separator and all the characters of the (unary and binary) operators 
 * and the condition parts that are not valid identifiers with the <code>isValidIdentifier ()</code> method.<br>
 * The case sensitivity of the elements of the syntax is set at creation and can't be changed.
 * By default, a syntax isn't case sensitive.
 *
 * @version   1.0.2
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.jeks.ResourceSyntax
 * @see       com.eteks.parser.JavaSyntax
 */
public abstract class AbstractSyntax implements Syntax
{
  private Hashtable constantKeys        = new Hashtable ();
  private Hashtable unaryOperatorKeys   = new Hashtable ();
  private Hashtable binaryOperatorKeys  = new Hashtable ();
  private Hashtable conditionPartKeys   = new Hashtable ();
  private Hashtable commonFunctionKeys  = new Hashtable ();
  private Hashtable functions           = new Hashtable ();
  private Hashtable operatorsPriority   = new Hashtable ();

  private String    whiteSpaceCharacters;
  private char      openingBracket;
  private char      closingBracket;
  private char      parameterSeparator;
  private String    assignmentOperator;

  private String    delimiters;

  private boolean   caseSensitive;
  private boolean   shortSyntax;

  /**
   * Creates a syntax which is not case sensitive.
   */
  public AbstractSyntax ()
  {
    this (false);
  }

  /**
   * Creates a syntax, case sensitive according to <code>caseSensitive</code>
   * @param caseSensitive <code>true</code> if case sensitive.
   */
  public AbstractSyntax (boolean caseSensitive)
  {
    this.caseSensitive = caseSensitive;
  }

  /**
   * Returns the key matching <code>constant</code>. The association is made
   * with the <code>setConstantKey ()</code>.
   * @param constant the string to test.
   * @return the key associated with the constant <code>constant</code>.
   */
  public Object getConstantKey (String constant)
  {
    return constantKeys.get (isCaseSensitive () ? constant : constant.toUpperCase ());
  }

  /**
   * Associates the constant <code>constant</code> to the syntactic key
   * <code>constantKey</code>.
   * @param constant a string describing a constant.
   * @param constantKey a constant key (one of <code>CONSTANT_PI</code>,
   *        <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code>
   *        or an other user defined key).
   */
  public void setConstantKey (String constant, Object constantKey)
  {
    if (!isCaseSensitive ())
      constant = constant.toUpperCase ();
    constantKeys.put (constant, constantKey);
  }

  /**
   * Returns the key matching <code>unaryOperator</code>. The association is made
   * with the <code>setUnaryOperatorKey ()</code> method.
   * @param unaryOperator the string to test.
   * @return the key associated with the unary operator <code>unaryOperator</code>.
   */
  public Object getUnaryOperatorKey (String unaryOperator)
  {
    return unaryOperatorKeys.get (isCaseSensitive () ? unaryOperator : unaryOperator.toUpperCase ());
  }

  /**
   * Associates the unary operator <code>unaryOperator</code> to the syntactic key
   * <code>unaryOperatorKey</code>.
   * @param unaryOperator an unary operator.
   * @param unaryOperatorKey an unary operator key (one of <code>OPERATOR_POSITIVE</code>,
   *        <code>OPERATOR_OPPOSITE</code>, <code>OPERATOR_LOGICAL_NOT</code>,
   *        <code>OPERATOR_BITWISE_NOT</code> or an other user defined key).
   */
  public void setUnaryOperatorKey (String unaryOperator, Object unaryOperatorKey)
  {
    if (!isCaseSensitive ())
      unaryOperator = unaryOperator.toUpperCase ();
    unaryOperatorKeys.put (unaryOperator, unaryOperatorKey);
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the key matching <code>binaryOperator</code>. The association is made
   * with the <code>setBinaryOperatorKey ()</code> method.
   * @param binaryOperator the string to test.
   * @return the key associated with the binary operator <code>binaryOperator</code>.
   */
  public Object getBinaryOperatorKey (String binaryOperator)
  {
    return binaryOperatorKeys.get (isCaseSensitive () ? binaryOperator : binaryOperator.toUpperCase ());
  }

  /**
   * Associates the binary operator <code>binaryOperator</code> to the syntactic key
   * <code>binaryOperatorKey</code>.
   * @param binaryOperator a binary operator.
   * @param binaryOperatorKey a binary operator key (one of <code>OPERATOR_ADD</code>,
   *        <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MODULO</code>,
   *        <code>OPERATOR_DIVIDE</code>,... or an other user defined key)
   */
  public void setBinaryOperatorKey (String binaryOperator, Object binaryOperatorKey)
  {
    if (!isCaseSensitive ())
      binaryOperator = binaryOperator.toUpperCase ();
    binaryOperatorKeys.put (binaryOperator, binaryOperatorKey);
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the key matching <code>conditionPart</code>. The association is made
   * with the <code>setConditionPartKey ()</code> method.
   * @param conditionPart the string to test.
   * @return the key associated with the conditional part <code>conditionPart</code>.
   */
  public Object getConditionPartKey (String conditionPart)
  {
    return conditionPartKeys.get (isCaseSensitive () ? conditionPart : conditionPart.toUpperCase ());
  }

  /**
   * Returns 2 or 3 depending on whether the syntax using a condition with two or three parts.
   * If a condition part was associated to the key <code>CONDITION_IF</code> with
   * the <code>setConditionPartKey</code> method, it returns 3 otherwise it returns 2.
   * @return 2 or 3.
   */
  public int getConditionPartCount ()
  {
    return conditionPartKeys.contains (CONDITION_IF) ? 3 : 2;
  }

  /**
   * Associates the condition part <code>conditionPart</code> to the syntactic key
   * <code>conditionPartKey</code>.
   * @param conditionPart a conditional part.
   * @param conditionPartKey a conditional part key (one of <code>CONDITION_IF</code>,
   *        <code>CONDITION_THEN</code>, <code>CONDITION_ELSE</code>).
   */
  public void setConditionPartKey (String conditionPart, Object conditionPartKey)
  {
    if (   !CONDITION_IF.equals (conditionPartKey)
        && !CONDITION_THEN.equals (conditionPartKey)
        && !CONDITION_ELSE.equals (conditionPartKey))
      throw new IllegalArgumentException ("Not a valid condition part key");

    if (!isCaseSensitive ())
      conditionPart = conditionPart.toUpperCase ();
    conditionPartKeys.put (conditionPart, conditionPartKey);
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the key matching <code>commonFunction</code>. The association is made
   * with the <code>setCommonFunctionKey ()</code> method.
   * @param commonFunction the string to test.
   * @return the key associated with the common function <code>commonFunction</code>.
   */
  public Object getCommonFunctionKey (String commonFunction)
  {
    return commonFunctionKeys.get (isCaseSensitive () ? commonFunction : commonFunction.toUpperCase ());
  }

  /**
   * Associates the common function of name <code>commonFunction</code> to the syntax key
   * <code>commonFunctionKey</code>.
   * @param commonFunction a function name.
   * @param commonFunctionKey a common function key (one of <code>FUNCTION_LN</code>,
   *        <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,...
   *        or an other user defined key).
   */
  public void setCommonFunctionKey (String commonFunction, Object commonFunctionKey)
  {
    if (!isCaseSensitive ())
      commonFunction = commonFunction.toUpperCase ();
    commonFunctionKeys.put (commonFunction, commonFunctionKey);
  }

  /**
   * Returns a reference to the instance of <code>Function</code> whose name is <code>functionName</code>.
   * The <code>addFunction ()</code> method records the functions that a user wants to
   * add to a syntax.
   * @param functionName the string to test.
   * @return the <code>Function</code> reference of name <code>functionName</code>.
   */
  public Function getFunction (String functionName)
  {
    return (Function)functions.get (isCaseSensitive () ? functionName : functionName.toUpperCase ());
  }

  /**
   * Adds <code>function</code> to the recognized functions of this syntax. The name used
   * by the <code>getSyntax ()</code> method is the value returned by <code>function.getName ()</code>.<br>
   * <code>function</code> may be a function previously parsed with <code>FunctionParser</code>
   * or an intance of a Java written class implementing the <code>Function</code> interface.
   * @param function the function to add.
   */
  public void addFunction (Function function)
  {
    String functionName = isCaseSensitive ()
                            ? function.getName ()
                            : function.getName ().toUpperCase ();
    functions.put (functionName, function);
  }

  /**
   * Removes <code>function</code> from the functions of this syntax.
   * @param function the function to remove.
   */
  public void removeFunction (Function function)
  {
    String functionName = isCaseSensitive ()
                            ? function.getName ()
                            : function.getName ().toUpperCase ();
    functions.remove (functionName);
  }

  /**
   * Returns the priority of the binary operator matching the key <code>binaryOperatorKey</code>.
   * @param binaryOperatorKey a binary operator key.
   * @return the priority of <code>binaryOperatorKey</code> set with the
   *         <code>setBinaryOperatorPriority ()</code> method or 0 (the lowest priority).
   */
  public int getBinaryOperatorPriority (Object binaryOperatorKey)
  {
    Integer priority = (Integer)operatorsPriority.get (binaryOperatorKey);
    if (priority != null)
      return priority.intValue ();
    else
      return 0;
  }

  /**
   * Sets the priority of the binary operator matching the key <code>binaryOperatorKey</code>.
   * @param binaryOperatorKey a binary operator key.
   * @param priority a positive value equal to the priority of the operator.
   */
  public void setBinaryOperatorPriority (Object binaryOperatorKey, int priority)
  {
    if (priority < 0)
      throw new IllegalArgumentException ("priority can't be negative");
    operatorsPriority.put (binaryOperatorKey, new Integer (priority));
  }

  /**
   * Returns a string that contains all the delimiters allowed by the syntax as white spaces
   * (tab, spaces,...).
   * @return <code>null</code> by default.
   */
  public String getWhiteSpaceCharacters ()
  {
    return whiteSpaceCharacters;
  }

  /**
   * Sets the string that contains all the delimiters allowed by the syntax as white spaces
   * (tab, spaces,...).
   * @param whiteSpaces the white spaces.
   */
  public void setWhiteSpaceCharacters (String whiteSpaceCharacters)
  {
    this.whiteSpaceCharacters = whiteSpaceCharacters;
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the char used as the opening bracket of the syntax.
   * @return 0 by default.
   */
  public char getOpeningBracket ()
  {
    return openingBracket;
  }

  /**
   * Sets the char used as the opening bracket of the syntax.
   * @param openingBracket the opening bracket.
   */
  public void setOpeningBracket (char openingBracket)
  {
    this.openingBracket = openingBracket;
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the char used as the closing bracket of the syntax.
   * @return 0 by default.
   */
  public char getClosingBracket ()
  {
    return closingBracket;
  }

  /**
   * Sets the char used as the closing bracket of the syntax.
   * @param closingBracket the closing bracket.
   */
  public void setClosingBracket (char closingBracket)
  {
    this.closingBracket = closingBracket;
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns the char used to separate parameters in a function call.
   * @return 0 by default.
   */
  public char getParameterSeparator ()
  {
    return parameterSeparator;
  }

  /**
   * Sets the char used to separate parameters in a function call.
   * @param parameterSeparator the separator of parameters.
   */
  public void setParameterSeparator (char parameterSeparator)
  {
    this.parameterSeparator = parameterSeparator;
    delimiters = null; // Force a new search of special characters
  }

  /**
   * Returns a string containing the white spaces, the brackets, the parameter separator and
   * all the characters of the (unary and binary) operators and the condition parts that are not
   * valid identifiers with the <code>isValidIdentifier ()</code> method.
   * @return a string containing the set of characters of the syntax that
   *         may be used as delimiters between literals, constants or identifiers.
   * @see com.eteks.syntax#isValidIdentifier
   */
  public String getDelimiters ()
  {
    if (delimiters == null)
      initDelimiters ();
    return delimiters;
  }

  private void initDelimiters ()
  {
    StringBuffer delimitersBuffer = new StringBuffer ();
    if (getWhiteSpaceCharacters () != null)
      delimitersBuffer.append (getWhiteSpaceCharacters ());
    if (getOpeningBracket () != 0)
      delimitersBuffer.append (String.valueOf (getOpeningBracket ()));
    if (getClosingBracket () != 0)
      delimitersBuffer.append (String.valueOf (getClosingBracket ()));
    if (getParameterSeparator () != 0)
      delimitersBuffer.append (String.valueOf (getParameterSeparator ()));

    addInvalidChars (delimitersBuffer, unaryOperatorKeys.keys ());
    addInvalidChars (delimitersBuffer, binaryOperatorKeys.keys ());
    addInvalidChars (delimitersBuffer, conditionPartKeys.keys ());

    delimiters = delimitersBuffer.toString ();
  }

  private void addInvalidChars (StringBuffer delimitersBuffer,
                                Enumeration  strings)
  {
    while (strings.hasMoreElements ())
    {
      String s = (String)strings.nextElement ();
      if (!isValidIdentifier (s))
        for (int j = 0; j < s.length (); j++)
        {
          char c = s.charAt (j);
          if (   !isValidIdentifier (String.valueOf (c))
              && delimitersBuffer.toString ().indexOf (c) == -1)
            delimitersBuffer.append (c);
        }
    }
  }

  /**
   * Returns the string used as the operator of assignment.
   * @return <code>null</code> by default.
   */
  public String getAssignmentOperator ()
  {
    return assignmentOperator;
  }

  /**
   * Sets the string used as the operator of assignment.
   * @param the operator of assignment.
   */
  public void setAssignmentOperator (String assignmentOperator)
  {
    this.assignmentOperator = assignmentOperator;
  }

  /**
   * Returns <code>true</code> if identifiers, constants, operators and function of the syntax
   * are case sensitive. This method is used by the parser for tests comparing the name of
   * functions and parameters and also by <code>getConstantKey ()</code>, <code>getUnaryOperatorKey ()</code>,
   * <code>getBinaryOperatorKey ()</code>, <code>getConditionPartKey ()</code>, <code>getCommonFunctionKey ()</code>
   * and <code>getFunction ()</code> and their respective setter methods to compare
   * strings respecting the case sensitivity set at creation of the syntax.<br>
   * By default, a syntax isn't case sensitive.
   * @return <code>false</code> by default.
   */
  public final boolean isCaseSensitive ()
  {
    return caseSensitive;
  }

  /**
   * Returns <code>true</code> if expressions parsed with this syntax supports short cuts.
   * @return <code>false</code> by default.
   */
  public boolean isShortSyntax ()
  {
    return shortSyntax;
  }

  /**
   * Sets the capabity of expressions parsed with this syntax to support syntax short cuts.
   * @param shortSyntax <code>true</code> to enable the support of short cuts.
   * @see com.eteks.parser.Syntax#isShortSyntax
   */
  public void setShortSyntax (boolean shortSyntax)
  {
    this.shortSyntax = shortSyntax;
  }

  /**
   * Extracts the number from the expression <code>expression</code> in the string buffer
   * <code>extractedNumber</code>. The parsed number may start with a digit, ., - or +
   * followed with a decimal number and possibly with the letter E and + or - and an integer.
   * @param expression      the string to parse.
   * @param extractedNumber the number extracted from <code>expression</code>.
   * @return <code>null</code> if <code>expression</code> doesn't start with a number
   *         or an instance of <code>Double</code> or <code>Long</code> according to the
   *         parsed number.
   */
  protected Number getLiteralNumber (String       expression,
                                     StringBuffer extractedNumber)
  {
    int  expLength = expression.length ();
    
    if (expLength == 0)
      return null;
    
    int  nbrePoint = 0;
    char c = expression.charAt (0);
    char c1;

    // A number may start with a digit
    // or . followed with a digit
    // or + or - followed with a digit or . with a digit
    if (   c >= '0'
        && c <= '9')
      extractedNumber.append (c);
    // v1.0.2 number starting with . incorrectly parsed
    else if (   expLength > 1
             && c == '.'
             && (c1 = expression.charAt (1)) >= '0'
             && c1 <= '9')
    {
      extractedNumber.append (c);
      nbrePoint++;
    }
    else if (   expLength > 1
             && (   c == '+'
                 || c == '-')
             && (      (c1 = expression.charAt (1)) >= '0'
                    && c1 <= '9'
                 ||    expLength > 2
                    && c1 == '.'
                    && (c1 = expression.charAt (1)) >= '0'
                    && c1 <= '9'))
      extractedNumber.append (c);
    else
      return null;

    // It may be followed with digits and at most one . among them
    int  index = 1;
    for ( ;    index < expLength
            && (     (c = expression.charAt (index)) >= '0'
                   && c <= '9'
                ||    c == '.'
                   && nbrePoint++ == 0);
            index++)
      extractedNumber.append (c);

    char exponentCharacter = 0;
    char exponentSign      = 0;
    // It may end by an exponent character
    // followed with digits
    // or + or - and with digits
    if (   nbrePoint <= 1
        && index + 1 < expLength
        && (   (exponentCharacter = expression.charAt (index)) == 'E'
            || exponentCharacter == 'e')
        && (      (c = expression.charAt (index + 1)) >= '0'
               && c <= '9'
            ||    index + 2 < expLength
               &&    (   c == '+'
                      || c == '-')
                  && (c = expression.charAt (index + 2)) >= '0'
                  && c <= '9'))
    {
      extractedNumber.append (exponentCharacter);
      extractedNumber.append (expression.charAt (index + 1));
      for (index += 2;
              index < expLength
           && (c = expression.charAt (index)) >= '0'
           && c <= '9';
           index++)
        extractedNumber.append (c);
    }

    if (   nbrePoint > 0
        || exponentCharacter == 'E'
        || exponentCharacter == 'e')
      return new Double (extractedNumber.toString ());
    else
      return new Long (extractedNumber.toString ());
  }
}
