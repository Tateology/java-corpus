/*
 * @(#)ResourceSyntax.java   05/02/99
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

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.parser.AbstractSyntax;

/**
 * Syntax initialized from a <code>ResourceBundle</code>.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public abstract class ResourceSyntax extends AbstractSyntax
{
  private Hashtable constants            = new Hashtable ();
  private Hashtable unaryOperators       = new Hashtable ();
  private Hashtable binaryOperators      = new Hashtable ();
  private Hashtable conditionParts       = new Hashtable ();
  private Hashtable commonFunctions      = new Hashtable ();

  private ResourceBundle resourceBundle;

  /**
   * Creates an instance of <code>ResourceSyntax</code> initialized with the <code>resourceBundle</code>.
   */
  public ResourceSyntax (ResourceBundle resourceBundle)
  {
    super (getResourceBoolean (resourceBundle, "CASE_SENSITIVE"));

    this.resourceBundle = resourceBundle;

    // Syntax values are language dependant
    setResourceConstantKey ("CONSTANT_PI",    CONSTANT_PI);
    setResourceConstantKey ("CONSTANT_E",     CONSTANT_E);
    setResourceConstantKey ("CONSTANT_FALSE", CONSTANT_FALSE);
    setResourceConstantKey ("CONSTANT_TRUE",  CONSTANT_TRUE);

    // Unary operators keys available for this syntax
    setResourceUnaryOperatorKey ("OPERATOR_POSITIVE",    OPERATOR_POSITIVE);
    setResourceUnaryOperatorKey ("OPERATOR_OPPOSITE",    OPERATOR_OPPOSITE);
    setResourceUnaryOperatorKey ("OPERATOR_LOGICAL_NOT", OPERATOR_LOGICAL_NOT);
    setResourceUnaryOperatorKey ("OPERATOR_BITWISE_NOT", OPERATOR_BITWISE_NOT);

    // Binary operators keys available for this syntax
    setResourceBinaryOperatorKey ("OPERATOR_ADD",              OPERATOR_ADD);
    setResourceBinaryOperatorKey ("OPERATOR_SUBSTRACT",        OPERATOR_SUBSTRACT);
    setResourceBinaryOperatorKey ("OPERATOR_DIVIDE",           OPERATOR_DIVIDE);
    setResourceBinaryOperatorKey ("OPERATOR_MULTIPLY",         OPERATOR_MULTIPLY);
    setResourceBinaryOperatorKey ("OPERATOR_POWER",            OPERATOR_POWER);
    setResourceBinaryOperatorKey ("OPERATOR_MODULO",           OPERATOR_MODULO);
    setResourceBinaryOperatorKey ("OPERATOR_REMAINDER",        OPERATOR_REMAINDER);
    setResourceBinaryOperatorKey ("OPERATOR_EQUAL",            OPERATOR_EQUAL);
    setResourceBinaryOperatorKey ("OPERATOR_DIFFERENT",        OPERATOR_DIFFERENT);
    setResourceBinaryOperatorKey ("OPERATOR_GREATER_OR_EQUAL", OPERATOR_GREATER_OR_EQUAL);
    setResourceBinaryOperatorKey ("OPERATOR_LESS_OR_EQUAL",    OPERATOR_LESS_OR_EQUAL);
    setResourceBinaryOperatorKey ("OPERATOR_GREATER",          OPERATOR_GREATER);
    setResourceBinaryOperatorKey ("OPERATOR_LESS",             OPERATOR_LESS);
    setResourceBinaryOperatorKey ("OPERATOR_LOGICAL_OR",       OPERATOR_LOGICAL_OR);
    setResourceBinaryOperatorKey ("OPERATOR_LOGICAL_XOR",      OPERATOR_LOGICAL_XOR);
    setResourceBinaryOperatorKey ("OPERATOR_LOGICAL_AND",      OPERATOR_LOGICAL_AND);
    setResourceBinaryOperatorKey ("OPERATOR_BITWISE_OR",       OPERATOR_BITWISE_OR);
    setResourceBinaryOperatorKey ("OPERATOR_BITWISE_XOR",      OPERATOR_BITWISE_XOR);
    setResourceBinaryOperatorKey ("OPERATOR_BITWISE_AND",      OPERATOR_BITWISE_AND);
    setResourceBinaryOperatorKey ("OPERATOR_SHIFT_LEFT",       OPERATOR_SHIFT_LEFT);
    setResourceBinaryOperatorKey ("OPERATOR_SHIFT_RIGHT",      OPERATOR_SHIFT_RIGHT);
    setResourceBinaryOperatorKey ("OPERATOR_SHIFT_RIGHT_0",    OPERATOR_SHIFT_RIGHT_0);

    // Binary operators priority of this syntax
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_ADD",              OPERATOR_ADD);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_SUBSTRACT",        OPERATOR_SUBSTRACT);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_MODULO",           OPERATOR_MODULO);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_DIVIDE",           OPERATOR_DIVIDE);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_MULTIPLY",         OPERATOR_MULTIPLY);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_POWER",            OPERATOR_POWER);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_LOGICAL_OR",       OPERATOR_LOGICAL_OR);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_LOGICAL_XOR",      OPERATOR_LOGICAL_XOR);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_LOGICAL_AND",      OPERATOR_LOGICAL_AND);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_BITWISE_OR",       OPERATOR_BITWISE_OR);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_BITWISE_XOR",      OPERATOR_BITWISE_XOR);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_BITWISE_AND",      OPERATOR_BITWISE_AND);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_SHIFT_LEFT",       OPERATOR_SHIFT_LEFT);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_SHIFT_RIGHT",      OPERATOR_SHIFT_RIGHT);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_SHIFT_RIGHT_0",    OPERATOR_SHIFT_RIGHT_0);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_EQUAL",            OPERATOR_EQUAL);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_DIFFERENT",        OPERATOR_DIFFERENT);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_GREATER_OR_EQUAL", OPERATOR_GREATER_OR_EQUAL);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_LESS_OR_EQUAL",    OPERATOR_LESS_OR_EQUAL);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_GREATER",          OPERATOR_GREATER);
    setResourceBinaryOperatorPriority ("OPERATOR_PRIORITY_LESS",             OPERATOR_LESS);

    // Predifined function keys available for this syntax
    setResourceCommonFunctionKey ("FUNCTION_LN",       FUNCTION_LN);
    setResourceCommonFunctionKey ("FUNCTION_LOG",      FUNCTION_LOG);
    setResourceCommonFunctionKey ("FUNCTION_EXP",      FUNCTION_EXP);
    setResourceCommonFunctionKey ("FUNCTION_SQR",      FUNCTION_SQR);
    setResourceCommonFunctionKey ("FUNCTION_SQRT",     FUNCTION_SQRT);
    setResourceCommonFunctionKey ("FUNCTION_COS",      FUNCTION_COS);
    setResourceCommonFunctionKey ("FUNCTION_SIN",      FUNCTION_SIN);
    setResourceCommonFunctionKey ("FUNCTION_TAN",      FUNCTION_TAN);
    setResourceCommonFunctionKey ("FUNCTION_ACOS",     FUNCTION_ACOS);
    setResourceCommonFunctionKey ("FUNCTION_ASIN",     FUNCTION_ASIN);
    setResourceCommonFunctionKey ("FUNCTION_ATAN",     FUNCTION_ATAN);
    setResourceCommonFunctionKey ("FUNCTION_COSH",     FUNCTION_COSH);
    setResourceCommonFunctionKey ("FUNCTION_SINH",     FUNCTION_SINH);
    setResourceCommonFunctionKey ("FUNCTION_TANH",     FUNCTION_TANH);
    setResourceCommonFunctionKey ("FUNCTION_INTEGER",  FUNCTION_INTEGER);
    setResourceCommonFunctionKey ("FUNCTION_FLOOR",    FUNCTION_FLOOR);
    setResourceCommonFunctionKey ("FUNCTION_CEIL",     FUNCTION_CEIL);
    setResourceCommonFunctionKey ("FUNCTION_ROUND",    FUNCTION_ROUND);
    setResourceCommonFunctionKey ("FUNCTION_ABS",      FUNCTION_ABS);
    setResourceCommonFunctionKey ("FUNCTION_OPPOSITE", FUNCTION_OPPOSITE);
    setResourceCommonFunctionKey ("FUNCTION_NOT",      FUNCTION_NOT);

    // If then else keys available for this syntax
    setResourceConditionPartKey ("CONDITION_IF",   CONDITION_IF);
    setResourceConditionPartKey ("CONDITION_THEN", CONDITION_THEN);
    setResourceConditionPartKey ("CONDITION_ELSE", CONDITION_ELSE);

    setWhiteSpaceCharacters (getResourceString ("WHITE_SPACE_CHARS"));
    setOpeningBracket       (getResourceChar ("OPEN_BRACKET"));
    setClosingBracket       (getResourceChar ("CLOSE_BRACKET"));
    setParameterSeparator   (getResourceChar ("PARAMETER_SEPARATOR"));
    setAssignmentOperator   (getResourceString ("ASSIGNMENT_OPERATOR"));

    setShortSyntax   (getResourceBoolean ("SHORT_SYNTAX"));
  }

  public ResourceBundle getResourceBundle ()
  {
    return resourceBundle;
  }

  public void setResourceConstantKey (String resourceName,
                                      Object constantKey)
  {
    String constant = getResourceString (resourceName);
    if (constant != null)
    {
      setConstantKey (constant, constantKey);
      constants.put (constantKey, constant);
    }
  }

  public void setResourceUnaryOperatorKey (String resourceName,
                                           Object unaryOperatorKey)
  {
    String unaryOperator = getResourceString (resourceName);
    if (unaryOperator != null)
    {
      setUnaryOperatorKey (unaryOperator, unaryOperatorKey);
      unaryOperators.put (unaryOperatorKey, unaryOperator);
    }
  }

  public void setResourceBinaryOperatorKey (String resourceName,
                                            Object binaryOperatorKey)
  {
    String binaryOperator = getResourceString (resourceName);
    if (binaryOperator != null)
    {
      setBinaryOperatorKey (binaryOperator, binaryOperatorKey);
      binaryOperators.put (binaryOperatorKey, binaryOperator);
    }
  }

  public void setResourceBinaryOperatorPriority (String resourceName,
                                                 Object binaryOperatorKey)
  {
    String binaryOperatorPriority = getResourceString (resourceName);
    if (binaryOperatorPriority != null)
      setBinaryOperatorPriority (binaryOperatorKey, Integer.parseInt (binaryOperatorPriority));
  }

  public void setResourceConditionPartKey (String resourceName,
                                           Object conditionPartKey)
  {
    String conditionPart = getResourceString (resourceName);
    if (conditionPart != null)
    {
      setConditionPartKey (conditionPart, conditionPartKey);
      conditionParts.put (conditionPartKey, conditionPart);
    }
  }

  public void setResourceCommonFunctionKey (String resourceName,
                                            Object commonFunctionKey)
  {
    String commonFunction = getResourceString (resourceName);
    if (commonFunction != null)
    {
      setCommonFunctionKey (commonFunction, commonFunctionKey);
      commonFunctions.put (commonFunctionKey, commonFunction);
    }
  }

  public String getResourceString (String resourceName)
  {
    return getResourceString (resourceBundle, resourceName);
  }

  public static String getResourceString (ResourceBundle resourceBundle,
                                          String         resourceName)
  {
    try
    {
      String resource = resourceBundle.getString (resourceName);
      if (resource.length () > 0)
        return resource;
    }
    catch (MissingResourceException e)
    { }

    return null;
  }

  public char getResourceChar (String resourceName)
  {
    return getResourceChar (resourceBundle, resourceName);
  }

  public static char getResourceChar (ResourceBundle resourceBundle,
                                      String         resourceName)
  {
    String str = getResourceString (resourceBundle, resourceName);
    if (str != null)
      return str.charAt (0);
    else
      return 0;
  }

  public boolean getResourceBoolean (String resourceName)
  {
    return getResourceBoolean (resourceBundle, resourceName);
  }

  public static boolean getResourceBoolean (ResourceBundle resourceBundle,
                                            String         resourceName)
  {
    String str = getResourceString (resourceBundle, resourceName);
    if (str != null)
      return Boolean.valueOf (str).booleanValue ();
    else
      return false;
  }

  public String getConstant (Object constantKey)
  {
    return (String)constants.get (constantKey);
  }

  public String getUnaryOperator (Object unaryOperatorKey)
  {
    return (String)unaryOperators.get (unaryOperatorKey);
  }

  public String getBinaryOperator (Object binaryOperatorKey)
  {
    return (String)binaryOperators.get (binaryOperatorKey);
  }

  public String getConditionPart (Object conditionPartKey)
  {
    return (String)conditionParts.get (conditionPartKey);
  }

  public String getCommonFunction (Object commonFunctionKey)
  {
    return (String)commonFunctions.get (commonFunctionKey);
  }
}