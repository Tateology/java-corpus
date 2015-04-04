/*
 * @(#)DoubleInterpreter.java   01/01/98
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
 * Runtime interpreter operating on <code>Double</code> objects. This class
 * implements the computation of all the literals, operators and functions
 * defined in <code>Syntax</code> with <code>double</code> numbers.<br>
 * Functions and expressions may use this interpreter to compute values if the
 * value of their parameters are <code>Number</code> objects (<code>Double</code>,
 * <code>Integer</code>,...).<br>
 * This interpreter is used as the default interpreter in <code>CompiledFunction</code>
 * and <code>CompiledExpression</code> classes.<br>
 * The methods of this class are thread safe.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.Syntax
 */
public class DoubleInterpreter implements Interpreter
{
  /**
   * The double constant matching the constant FALSE (equal to <code>new Double (0)</code>).
   */
  public static final Double FALSE_DOUBLE = new Double (0);
  /**
   * The double constant matching the constant TRUE (equal to <code>new Double (1)</code>).
   */
  public static final Double TRUE_DOUBLE  = new Double (1);

  private static final Double PI_DOUBLE = new Double (Math.PI);
  private static final Double E_DOUBLE  = new Double (Math.E);

  /**
   * Returns the value of the literal <code>literal</code>. <code>literal</code>
   * may be an instance of <code>Number</code>.
   * @param literal an instance of <code>Number</code>.
   * @return the value of the literal. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>literal</code> isn't an instance of <code>Number</code>.
   */
  public Object getLiteralValue (Object literal)
  {
    if (literal instanceof Double)
      return literal;
    else if (literal instanceof Number)
      return new Double (((Number)literal).doubleValue ());
    else
      throw new IllegalArgumentException ("Literal " + literal + " not an instance of Number");
  }

  /**
   * Returns the value of the parameter <code>parameter</code>. <code>parameter</code>
   * may be an instance of <code>Number</code>.
   * This method may throw an exception if the interpreter doesn't accept the type of
   * <code>parameter</code>.
   * @param parameter an instance of <code>Number</code>.
   * @return the value of the parameter. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>parameter</code> isn't an instance of <code>Number</code>.
   */
  public Object getParameterValue (Object parameter)
  {
    if (parameter instanceof Double)
      return parameter;
    else if (parameter instanceof Number)
      return new Double (((Number)parameter).doubleValue ());
    else
      throw new IllegalArgumentException ("Parameter " + parameter + " not an instance of Number");
  }

  /**
   * Returns the value of the constant <code>constantKey</code>. <code>constantKey</code>
   * may be the key of a constant of <code>Syntax</code> (one of <code>CONSTANT_PI</code>,
   * <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code>).
   * @param constantKey the key of a constant of <code>Syntax</code>.
   * @return the value of the constant. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>constantKey</code> isn't a key of a constant of
   *         <code>Syntax</code>.
   */
  public Object getConstantValue (Object constantKey)
  {
    if (Syntax.CONSTANT_PI.equals (constantKey))
      return PI_DOUBLE;
    else if (Syntax.CONSTANT_E.equals (constantKey))
      return E_DOUBLE;
    else if (Syntax.CONSTANT_FALSE.equals (constantKey))
      return FALSE_DOUBLE;
    else if (Syntax.CONSTANT_TRUE.equals (constantKey))
      return TRUE_DOUBLE;
    else
      throw new IllegalArgumentException ("Constant key " + constantKey + " not implemented");
  }

  /**
   * Returns the value of the operation of the unary operator <code>unaryOperatorKey</code> applied
   * on the operand <code>operand</code>. <code>unaryOperatorKey</code> must be the key
   * of an unary operator of <code>Syntax</code> (one of <code>OPERATOR_POSITIVE</code>,
   * <code>OPERATOR_OPPOSITE</code>, <code>OPERATOR_LOGICAL_NOT</code>, <code>OPERATOR_BITWISE_NOT</code>).
   * @param unaryOperatorKey the key of an unary operator of <code>Syntax</code>.
   * @param operand          the operand (instance of <code>Number</code>).
   * @return the result of the operation. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>operand</code> isn't an instance of <code>Number</code>,
   *         if <code>operand</code> isn't an integer operand when <code>unaryOperatorKey</code> is the
   *         key <code>Syntax.OPERATOR_BITWISE_NOT</code> or if <code>unaryOperatorKey</code>
   *         isn't the key of an unary operator of <code>Syntax</code>.
   */
  public Object getUnaryOperatorValue (Object unaryOperatorKey, Object operand)
  {
    if (!(operand instanceof Number))
      throw new IllegalArgumentException ("Operand " + operand + " not an instance of Number");

    double number = ((Number)operand).doubleValue ();

    if (unaryOperatorKey.equals (Syntax.OPERATOR_OPPOSITE))
      return new Double (-number);
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_POSITIVE))
      return operand;
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_NOT))
      return isTrue (operand) ? FALSE_DOUBLE : TRUE_DOUBLE;
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_NOT))
    {
      if (Math.floor (number) != number)
        throw new IllegalArgumentException ("Operator operand not an integer");
      return new Double (~((Number)operand).longValue ());
    }
    else
      throw new IllegalArgumentException ("Unary operator key " + unaryOperatorKey + " not implemented");
  }

  /**
   * Returns the value of the operation of the binary operator <code>binaryOperatorKey</code> applied on
   * the two operands <code>operand1</code> and <code>operand2</code>. <code>binaryOperatorKey</code>
   * must be the key of a binary operator of <code>Syntax</code> (one of <code>OPERATOR_ADD</code>,
   * <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>,...).
   * @param binaryOperatorKey the key of a binary operator of <code>Syntax</code>.
   * @param operand1          the first operand (instance of <code>Number</code>).
   * @param operand2          the second operand (instance of <code>Number</code>).
   * @return the result of the operation. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>operand1</code> or <code>operand2</code> aren't
   *         instances of <code>Number</code>, if <code>operand1</code> or <code>operand2</code> are
   *         not integer operands when <code>binaryOperatorKey</code> is the key of a bit operator
   *         (<code>Syntax.OPERATOR_BITWISE_...</code> and Syntax.OPERATOR_SHIFT_...)
   *         or if <code>binaryOperatorKey</code> isn't the key of a binary operator of <code>Syntax</code>.
   */
  public Object getBinaryOperatorValue (Object binaryOperatorKey, Object operand1, Object operand2)
  {
    if (!(operand1 instanceof Number))
      throw new IllegalArgumentException ("Operand " + operand1 + " not an instance of Number");
    if (!(operand2 instanceof Number))
      throw new IllegalArgumentException ("Operand " + operand2 + " not an instance of Number");

    double number1 = ((Number)operand1).doubleValue ();
    double number2 = ((Number)operand2).doubleValue ();

    if (binaryOperatorKey.equals (Syntax.OPERATOR_ADD))
      return new Double (number1 + number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SUBSTRACT))
      return new Double (number1 - number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MULTIPLY))
      return new Double (number1 * number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_DIVIDE))
      return new Double (number1 / number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_POWER))
      return new Double (Math.pow (number1, number2));
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MODULO))
    {
      double modulo = number1 - number2 * (int)(number1 / number2);
      // If dividend and divisor are not of the same sign, add divisor
      if (   number1 < 0 && number2 > 0
          || number1 > 0 && number2 < 0)
        modulo += number2;
      return new Double (modulo);
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_REMAINDER))
      return new Double (number1 % number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_EQUAL))
      return number1 == number2
               ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_DIFFERENT))
      return number1 != number2
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_GREATER_OR_EQUAL))
      return number1 >= number2
               ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LESS_OR_EQUAL))
      return number1 <= number2
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_GREATER))
      return number1 > number2
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LESS))
      return number1 < number2
                 ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_OR))
      return isTrue (operand1) || isTrue (operand2)
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_AND))
      return isTrue (operand1) && isTrue (operand2)
               ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_XOR))
      return     isTrue (operand1) && !isTrue (operand2)
             || !isTrue (operand1) &&  isTrue (operand2)
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else
      if (Math.floor (number1) != number1)
        throw new IllegalArgumentException ("Operand " + operand1 + " of bit operator not an integer");
      else if (Math.floor (number2) != number2)
        throw new IllegalArgumentException ("Operand " + operand2 + " of bit operator not an integer");
      else
        if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_OR))
          return new Double ((long)number1 | (long)number2);
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_XOR))
          return new Double ((long)number1 ^ (long)number2);
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_AND))
          return new Double ((long)number1 & (long)number2);
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_LEFT))
          return new Double ((long)number1 << (long)number2);
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT))
          return new Double ((long)number1 >> (long)number2);
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT_0))
          return new Double ((long)number1 >>> (long)number2);
        else
          // User binary operators must be implemented in a sub class
          throw new IllegalArgumentException ("Binary operator key " + binaryOperatorKey + " not implemented");
  }

  /**
   * Returns the value of the common function <code>commonFunctionKey</code> with
   * the parameter <code>param</code>. <code>commonFunctionKey</code> must be the key
   * of a commomon function of <code>Syntax</code> (one of <code>FUNCTION_LN</code>,
   * <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,...).
   * @param commonFunctionKey the key of a common function of <code>Syntax</code>.
   * @param param             the parameter of the function (instance of <code>Number</code>).
   * @return the result of the function. The returned value is an instance of <code>Double</code>.
   * @throws IllegalArgumentException if <code>param</code> isn't an instance of <code>Number</code>
   *         or if <code>commonFunctionKey</code> isn't the key of a commomon function of <code>Syntax</code>.
   */
  public Object getCommonFunctionValue (Object commonFunctionKey, Object param)
  {
    if (!(param instanceof Number))
      throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");

    double number = ((Number)param).doubleValue ();

    if (commonFunctionKey.equals (Syntax.FUNCTION_LN))
      return new Double (Math.log (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_LOG))
      return new Double (Math.log (number) / Math.log (10.));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_EXP))
      return new Double (Math.exp (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SQR))
      return new Double (number * number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SQRT))
      return new Double (Math.sqrt (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_COS))
      return new Double (Math.cos (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SIN))
      return new Double (Math.sin (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_TAN))
      return new Double (Math.tan (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ACOS))
      return new Double (Math.acos (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ASIN))
      return new Double (Math.asin (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ATAN))
      return new Double (Math.atan (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_COSH))
      return new Double ((Math.exp (number) + Math.exp (-number)) / 2.);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SINH))
      return new Double ((Math.exp (number) - Math.exp (-number)) / 2.);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_TANH))
      return new Double (  (Math.exp (number) - Math.exp (-number))
                         / (Math.exp (number) + Math.exp (-number)));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_INTEGER))
      return new Double (((Number)param).longValue ());
    else if (commonFunctionKey.equals (Syntax.FUNCTION_FLOOR))
      return new Double (Math.floor (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_CEIL))
      return new Double (Math.ceil (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ROUND))
      return new Double (Math.rint (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ABS))
      return new Double (Math.abs (number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_OPPOSITE))
      return new Double (-number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_NOT))
      return isTrue (param) ? FALSE_DOUBLE : TRUE_DOUBLE;
    else
      throw new IllegalArgumentException ("Common function key " + commonFunctionKey + " not implemented");
  }

  /**
   * Returns the value <code>paramThen</code> or <code>paramElse</code> depending
   * on whether <code>isTrue (paramIf)</code> returning <code>true</code> or <code>false</code>.
   * As the implementation of the <code>supportsRecursiveCall ()</code> method in this class returns
   * <code>true</code>, this method isn't called internally to get the result of a condition.
   * @param paramIf   the condition.
   * @param paramThen the true condition value.
   * @param paramElse the false condition value.
   * @return the result of the condition.
   * @see #supportsRecursiveCall
   */
  public Object getConditionValue (Object paramIf, Object paramThen,  Object paramElse)
  {
    return isTrue (paramIf) ? paramThen : paramElse;
  }

  /**
   * Returns <code>true</code> or <code>false</code> according to the value of <code>condition</code>.
   * @param condition the value to test (instance of <code>Number</code>).
   * @return <code>false</code> if the double value of <code>condition</code> equals 0.
   *         otherwise <code>true</code>.
   */
  public boolean isTrue (Object condition)
  {
    if (!(condition instanceof Number))
      throw new IllegalArgumentException ("Condition " + condition + " not an instance of Number");
    return ((Number)condition).doubleValue () != 0.;
  }

  /**
   * Returns <code>true</code> thus enabling this interpreter to evaluate the value of recursive
   * calls.
   * @return <code>true</code>.
   * @see #getConditionValue
   */
  public boolean supportsRecursiveCall ()
  {
    return true;
  }

  /**
   * Returns the value of the function <code>function</code> with its parameters <code>parametersValue</code>.
   * This method returns the result of <code>function.computeFunction (this, parametersValue)</code>.
   * @param function        the function to compute.
   * @param parametersValue the value of function's parameters.
   * @param recursiveCall <code>true</code> if the call of this function is a recursive call, meaning that
   *                      the current evaluated function calls itself.
   * @return the result of the function.
   */
  public Object getFunctionValue (Function    function,
                                  Object []   parametersValue,
                                  boolean     recursiveCall)
  {
    return function.computeFunction (this, parametersValue);
  }
}