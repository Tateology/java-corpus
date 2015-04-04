/*
 * @(#)Interpreter.java   01/01/98
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
 * Runtime interpreter. This interface specifies the set of methods that computes the value
 * of the different literals, constants, operators and functions available in the <code>Syntax</code> interface.
 * Once a string is parsed by one of the parsers, these methods are called at time of interpretation to
 * evaluate the result of a function or an expression according to the value of its parameters.<br>
 * The values managed at runtime by the methods are of type <code>Object</code> : This allows to
 * apply computations on a wide range of parameter types, from number types (with the classes <code>Long</code>,
 * <code>Double</code>,...) to strings (instances of <code>String</code>) and also booleans
 * (<code>Boolean</code>). Other classes like <code>java.math.BigDecimal</code> or <code>javax.vecmath.GVector</code> and
 * <code>javax.vecmath.GMatrix</code> can be also used.<br>
 * Depending on some implementations of this interface, these methods may throw a runtime exception
 * (generally an instance of <code>IllegalArgumentException</code>) if they consider
 * that their parameters have a wrong type to perform the resquested operation or if the key of
 * a constant, operator or function of a given syntax isn't implemented.<br>
 * Note that an <code>Interpreter</code> implementation isn't obliged to implement all the default
 * keys listed in <code>Syntax</code>.  For example, the <code>com.eteks.tools.calculator.JeksCalculator</code>
 * class implements the <code>Syntax</code> interface to recognize only the operators and common functions
 * available in the calculator and uses its own keys. On the other side, its implementation
 * of <code>Interpreter</code> is able to compute only these operators and common functions
 * according to these keys.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.Syntax
 * @see       com.eteks.parser.DoubleInterpreter
 * @see       com.eteks.tools.calculator.JeksCalculator
 */
public interface Interpreter
{
  /**
   * Returns the value of the literal <code>literal</code>. <code>literal</code>
   * may be an instance of <code>Number</code>, <code>String</code> or another class
   * matching the literal recognized by the syntax. This method
   * may throw an exception if the interpreter doesn't accept one of these types.
   * @param literal a literal.
   * @return the value of the literal. The type of the returned value depends on the implementation.
   */
  Object getLiteralValue (Object literal);

  /**
   * Returns the value of the parameter <code>parameter</code>. <code>parameter</code>
   * is one of the values passed to the <code>computeFunction ()</code> method of
   * <code>Function</code> to compute the value of a function or the value returned by the
   * <code>getParameterValue ()</code> method of <code>ExpressionParameter</code> to compute
   * the value of an expression.<br>
   * This method may throw an exception if the interpreter doesn't accept the type of
   * <code>parameter</code>.
   * @param parameter a paramater.
   * @return the value of the parameter. The type of the returned value depends on the implementation.
   * @see com.eteks.parser.Function#computeFunction
   * @see com.eteks.parser.ExpressionParameter#getParameterValue
   */
  Object getParameterValue (Object parameter);

  /**
   * Returns the value of the constant <code>constantKey</code>. <code>constantKey</code>
   * may be the key of a constant of <code>Syntax</code> (one of <code>CONSTANT_PI</code>,
   * <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code>
   * or an other user defined key).
   * @param constantKey the key of a constant of <code>Syntax</code>.
   * @return the value of the constant. The type of the returned value depends on the implementation.
   */
  Object getConstantValue (Object constantKey);

  /**
   * Returns the value of the operation of the unary operator <code>unaryOperatorKey</code> applied on
   * the operand <code>operand</code>. <code>unaryOperatorKey</code> is the key of an unary operator
   * of <code>Syntax</code> (one of <code>OPERATOR_POSITIVE</code> ,<code>OPERATOR_OPPOSITE</code>,
   * <code>OPERATOR_LOGICAL_NOT</code>, <code>OPERATOR_BITWISE_NOT</code> or
   * an other user defined key).
   * @param unaryOperatorKey the key of an unary operator of <code>Syntax</code>.
   * @param operand the operand (already evaluated if it's an expression).
   * @return the result of the operation. The type of the returned value depends on the implementation.
   */
  Object getUnaryOperatorValue (Object unaryOperatorKey, Object operand);

  /**
   * Returns the value of the operation of the binary operator <code>binaryOperatorKey</code> applied on
   * the two operands <code>operand1</code> and <code>operand2</code>. <code>binaryOperatorKey</code>
   * is the key of a binary operator of <code>Syntax</code> (one of <code>OPERATOR_ADD</code>,
   * <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>,...
   * or an other user defined key).
   * @param binaryOperatorKey the key of a binary operator of <code>Syntax</code>.
   * @param operand1 the first operand (already evaluated if it's an expression).
   * @param operand2 the second operand (already evaluated if it's an expression).
   * @return the result of the operation. The type of the returned value depends on the implementation.
   */
  Object getBinaryOperatorValue (Object binaryOperatorKey, Object operand1, Object operand2);

  /**
   * Returns the value of the common function <code>commonFunctionKey</code> with
   * the parameter <code>param</code>. <code>commonFunctionKey</code> is the key of
   * a commomon function of <code>Syntax</code> (one of <code>FUNCTION_LN</code>,
   * <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,...
   * or an other user defined key).
   * @param commonFunctionKey the key of a common function of <code>Syntax</code>.
   * @param param the parameter of the function (already evaluated if it's an expression).
   * @return the result of the function. The type of the returned value depends on the implementation.
   */
  Object getCommonFunctionValue (Object commonFunctionKey, Object param);

  /**
   * Returns the value <code>paramThen</code> or <code>paramElse</code> depending on
   * whether the condition <code>paramIf</code> being true or false.
   * As <code>paramIf</code>, <code>paramThen</code> <b>and</b> <code>paramElse</code>
   * are already evaluated before the call to this method, note that this method will
   * be called to get the result of a condition only if <code>supportsRecursiveCall ()</code>
   * returns <code>false</code> (otherwise recursive calls would never end).
   * If <code>supportsRecursiveCall ()</code> returns <code>true</code>, only <code>paramThen</code>
   * or <code>paramElse</code> is evaluated depending on whether <code>isTrue (paramIf)</code>
   * returning <code>true</code> or <code>false</code>.<br>
   * @param paramIf   the condition.
   * @param paramThen the true condition value.
   * @param paramElse the false condition value.
   * @return the result of the condition. The type of the returned value depends on the implementation.
   * @see #supportsRecursiveCall
   */
  Object getConditionValue (Object paramIf, Object paramThen,  Object paramElse);

  /**
   * Returns <code>true</code> or <code>false</code> according to the value of <code>condition</code>.
   * This method is called internally if <code>supportsRecursiveCall ()</code> returns
   * <code>true</code>. It may also be used in other methods of <code>Interpreter</code> to test the boolean
   * value of its operands.
   * @param condition the value to test (already evaluated if it's an expression).
   * @return <code>true</code> or <code>false</code>.
   * @see #getConditionValue
   */
  boolean isTrue (Object condition);

  /**
   * Returns <code>true</code> if the <code>isTrue ()</code> method is able to evaluate its parameter.
   * <code>isTrue ()</code> must be able to evaluate a boolean condition to stop the recursive
   * calls when the stop condition is met during interpretation.
   * @return <code>true</code> or <code>false</code>.
   * @see #isTrue
   * @see #getConditionValue
   */
  boolean supportsRecursiveCall ();

  /**
   * Returns the value of the function <code>function</code> with its parameters <code>parametersValue</code>.
   * This method can evaluate the value of the function call symply by computing <code>function.computeFunction (this, parametersValue)</code>.
   * @param function        the function to compute.
   * @param parametersValue the value of function's parameters (already evaluated if they are expressions).
   * @param recursiveCall   <code>true</code> if the call to this function is a recursive call, meaning that
   *                        the current evaluated function calls itself.
   * @return the result of the function. The type of the returned value depends on the implementation.
   */
  Object getFunctionValue (Function    function,
                           Object []   parametersValue,
                           boolean     recursiveCall);
}