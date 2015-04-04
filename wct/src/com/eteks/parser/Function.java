/*
 * @(#)Function.java   04/25/99
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

import java.io.Serializable;

/**
 * Interface of functions defined by user. These functions may be of one the two following types :
 * <UL><LI>Functions parsed the <code>compileFunction ()</code> method of a instance of
 *     <code>FunctionParser</code> : This method returns an instance of <code>CompiledFunction</code>
 *     that implements the <code>Function</code> interface.</LI>
 *     <LI>Any instance of a class that implements the <code>Function</code> interface : This
 *     enables to create some functions with a variable number of parameters (like the function
 *     <code>AND ()</code> of Jeks spreadsheet), that may run faster than functions of class
 *     <code>CompiledFunction</code> and that may use parameters of special type (like arrays
 *     of values for the <code>SUM ()</code> method of Jeks spreadsheet).</LI></UL>
 * In both cases, a function must be returned by the <code>getFunction ()</code> method of
 * the syntax of a parser, if you want to call it in the definition of an other function or an expression.<br>
 * Note that although this interface requires a JDK 1.1 library to compile because it extends
 * the <code>Serializable</code>, <code>com.eteks.parser</code> classes can run on a JVM 1.0
 * (as long as you don't process any serialization of course).
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.FunctionParser
 * @see       com.eteks.parser.Syntax
 */
public interface Function extends Serializable
{
  /**
   * Returns the name of this function.
   * @return  The public name that may be supported by a syntax to call this function in
   *          expressions parsed with instances of <code>FunctionParser</code> or <code>ExpressionParser</code>.
   * @see com.eteks.parser.Syntax#getFunction
   */
  public String getName ();

  /**
   * Returns <code>true</code> if the number of parameters <code>count</code>
   * required at runtime by this function is valid. This method is called
   * by the parser to check if the call to a function has the good parameter count.
   * @param count  Number of parameters (may be equal to 0).
   */
  public boolean isValidParameterCount (int count);

  /**
   * Returns the result of this function computed with the value of its parameters
   * <code>parametersValue</code>. The type of the parameters and of the returned value depends
   * on the interpreter used at runtime. This allows to apply computations on a wide range of
   * parameter types, from number types (with the classes <code>Long</code>,
   * <code>Double</code>,...) to strings (with the <code>String</code> class) and also booleans
   * (<code>Boolean</code>). Other classes like <code>java.math.BigDecimal</code> or <code>javax.vecmath.GVector</code>
   * and <code>javax.vecmath.GMatrix</code> can be also used.<br>
   * Depending on some implementations of this interface, this method may throw a runtime exception
   * (generally an instance of <code>IllegalArgumentException</code>) if its parameters
   * have a wrong type to perform the resquested function.<br>
   * If this method is implemented in a user function class, it may compute basic operations
   * with the methods of the current interpreter or with Java code.
   * For example, an <code>AVERAGE ()</code> function that computes the average value of
   * a variable number of parameters can be implemented in either following ways :
   * <BLOCKQUOTE><PRE> class FunctionAverage implements Function
   * {
   *   public String getName ()
   *   {
   *     return "AVERAGE";
   *   }
   *
   *   public boolean isValidParameterCount (int parameterCount)
   *   {
   *     return parameterCount > 0; // At least one parameter
   *   }
   *
   *   public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
   *   {
   *     Object sum = parametersValue [0];
   *     for (int i = 1; i < parametersValue.length; i++)
   *       // Add the parameters value with the binary operator ADD of the interpreter
   *       sum = interpreter.getBinaryOperatorValue (Syntax.OPERATOR_ADD,
   *                                                 sum, parametersValue [i]);
   *
   *     // Get the value of the literal parametersValue.length in the type used by interpreter
   *     Object parameterCount = interpreter.getLiteralValue (new Integer (parametersValue.length));
   *     // Divide the sum by parameterCount with the binary operator DIVIDE  of the interpreter
   *     return interpreter.getBinaryOperatorValue (Syntax.OPERATOR_DIVIDE,
   *                                                sum, parameterCount);
   *   }
   * }</PRE></BLOCKQUOTE>
   * or
   * <BLOCKQUOTE><PRE> class FunctionAverage implements Function
   * {
   *   public String getName ()
   *   {
   *     return "AVERAGE";
   *   }
   *
   *   public boolean isValidParameterCount (int parameterCount)
   *   {
   *     return parameterCount > 0; // At least one parameter
   *   }
   *
   *   public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
   *   {
   *     double sum = 0;
   *     for (int i = 0; i < parametersValue.length; i++)
   *       if (parametersValue [i] instanceof Number)
   *         // Add all numbers parametersValue [i]
   *         sum += ((Number)parametersValue [i]).doubleValue ();
   *       else
   *         throw new IllegalArgumentException (String.valueOf (parametersValue [i]) + " not a number");
   *
   *     return new Double (sum / parametersValue.length);
   *   }
   * }</PRE></BLOCKQUOTE>
   * Note that the second way is faster but is less generic because it supports only
   * <code>Number</code> parameters. According to the implementation of <code>interpreter</code>, the first
   * way may accept and return different type of values.
   * @param interpreter     the runtime interpreter to perform the operations of <code>Syntax</code>.
   * @param parametersValue the value of parameters (already evaluated if they are expressions).
   * @return the result of the operation of this function.
   * @see com.eteks.parser.Syntax
   * @see com.eteks.parser.Interpreter
   */
  public Object computeFunction (Interpreter interpreter, Object [] parametersValue);
}
