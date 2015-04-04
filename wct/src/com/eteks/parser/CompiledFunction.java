/*
 * @(#)CompiledFunction.java   04/25/99
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

import com.eteks.parser.node.ExpressionNode;

/**
 * Compiled functions with a fixed number of parameters and able to compute its value
 * with an interpreter. Instances of this class are returned by the
 * <code>compileFunction ()</code> method of the <code>FunctionParser</code> class,
 * and can be computed with the value of its parameters. This method parses the definition
 * of a function and builds a tree of type <code>ExpressionNode</code> matching the expression
 * of the function.<br>
 * The two <code>computeFunction ()</code> methods of this class allow to compute
 * the value of a function according to the value of the parameters passed to the method :
 * <UL><LI><code>double computeFunction (double [] parametersValue)</code> is the easiest
 *         and the fastest method to use if your function computes double numbers.</LI>
 *     <LI><code>Object computeFunction (Interpreter interpreter, Object [] parametersValue)</code>
 *         allows to choose the implementation of the interpreter that computes the
 *         constants, operators and functions of <code>Syntax</code> with <code>parametersValue</code>.</LI></UL>
 * For example, the 2 following calls to <code>computeFunction ()</code> returns the same
 * result :
 * <BLOCKQUOTE><PRE> FunctionParser parser = new FunctionParser ();
 * CompiledFunction function = parser.compileFunction ("f(x) = x * ln (x)");
 *
 * double doubleResult1 = function.<b>computeFunction</b> (new double [] {2});
 *
 * Interpreter interpreter = new DoubleInterpreter ();
 * Object doubleResult2 = function.<b>computeFunction</b> (interpreter, new Double [] {new Double (2)});</PRE></BLOCKQUOTE>
 *
 * The other methods of this class returns the different properties of a function (name,
 * parameters, definition,...).
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.FunctionParser
 * @see     com.eteks.parser.Interpreter
 */
public class CompiledFunction implements Function
{
  private String         definition;
  private String         name;
  private String []      parameters;
  private ExpressionNode expressionTree;

  /**
   * Creates a compiled function named <code>name</code> with its parameters
   * <code>parameters</code>, its definition <code>definition</code> and the
   * tree <code>expressionTree</code> matching the expression of the function.
   * @param definition     the definition of this function. This is
   *                       the parsed string from which was created this function.
   * @param name           the name of this function (extracted from the definition)
   * @param parameters     the name of its parameters (extracted from the definition)
   * @param expressionTree the tree built by the parser matching the expression of the function.
   */
  public CompiledFunction (String         definition,
                           String         name,
                           String []      parameters,
                           ExpressionNode expressionTree)
  {
    this.definition     = definition;
    this.name           = name;
    this.parameters     = parameters;
    this.expressionTree = expressionTree;
    if (parameters == null)
      this.parameters = new String [0];
  }

  /**
   * Returns the definition of this function. This is the string parsed
   * by the instance of <code>FunctionParser</code> that created this function.
   * @return  the definition of this function.
   */
  public String getDefinition ()
  {
    return definition;
  }

  /**
   * Returns the name of this function.
   * @return  a string which is the public name that may be supported by a syntax to call
   *          this function in expressions parsed with instances of <code>FunctionParser</code> or
   *          <code>ExpressionParser</code>.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Returns the array of the name of the parameters this function is requiring at runtime.
   * @return an array containing the parameters name.
   */
  public String [] getParameters ()
  {
    return parameters;
  }

  /**
   * Returns the number of parameters this function is requiring at runtime.
   * @return the number of parameters (may be equal to 0).
   */
  public int getParameterCount ()
  {
    return parameters.length;
  }

  /**
   * Returns <code>true</code> if <code>count</code> is equal to the required number
   * of parameters.
   */
  public boolean isValidParameterCount (int count)
  {
    return parameters.length == count;
  }

  /**
   * Returns the tree of the expression of this function. The returned
   * node is the root of the tree matching the expression defined in the function. For example,
   * the function <code>f(x) = x * ln (x)</code> has the following tree :
   * <UL><LI><code>BinaryOperatorNode</code> with <code>OPERATOR_MULTIPLY</code> key</LI>
   *     <UL><LI><code>FunctionParameterNode</code> for parameter at index 0</LI>
   *         <LI><code>CommonFunctionNode</code> with <code>FUNCTION_LN</code> key</LI>
   *         <UL><LI><code>FunctionParameterNode</code> for parameter at index 0</LI></UL></UL></UL>
   * @return the tree of this function.
   */
  public ExpressionNode getExpressionTree ()
  {
    return expressionTree;
  }

  /**
   * Modifies the tree of the expression. This method is friendly to be able to create an
   * instance of <code>CompiledFunction</code> and set afterwards its expression tree.
   * <code>FunctionParser</code> requires this feature to be able to build a
   * tree referencing this function in case of recursive calls.
   * @param expressionTree the new expression tree.
   */
  void setExpressionTree (ExpressionNode expressionTree)
  {
    this.expressionTree = expressionTree;
  }

  /**
   * Returns the result of the function computed with the value of its parameters.
   * This method simply calls <code>computeExpression (parametersValue)</code> on the root
   * of its tree.<br>
   * As this method avoid method calls to an interpreter and instantiation of objects
   * for parameters and return values, it runs much faster than the
   * <code>computeFunction (Interpreter, Object [])</code> method of this class,
   * but of course it can compute only double numbers.
   * @param  parametersValue  the value of parameters required to compute this function.
   * @return the result of the function.
   * @see com.eteks.parser.node.ExpressionNode
   */
  public double computeFunction (double [] parametersValue)
  {
    if (      parametersValue == null
           && parameters.length > 0
        ||    parametersValue != null
           && parametersValue.length < parameters.length)
      throw new IllegalArgumentException ("Too few parameters");

    return expressionTree.computeExpression (parametersValue);
  }

  /**
   * Returns the result of the function computed with the value of its parameters <code>parametersValue</code>.
   * This method simply calls <code>computeExpression (interpreter, parametersValue)</code> on the root
   * of its tree.<br>
   * This method is thread safe.
   * @param  interpreter     the runtime interpreter used to compute the operations of <code>Syntax</code>.
   * @param  parametersValue the value of parameters required to compute this function
   *         (already evaluated if they are expressions). <code>parametersValue</code>
   *         may be <code>null</code> if this function doesn't require parameters.
   * @return the result of the function. The returned object may be of any
   *         class (<code>Double</code>, <code>String</code>, ...) according to
   *         <code>interpreter</code> implementation.
   * @throws IllegalArgumentException if the parametersValue size is different of the required one.
   * @see    com.eteks.parser.node.ExpressionNode
   */
  public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
  {
    if (      parametersValue == null
           && parameters.length > 0
        ||    parametersValue != null
           && parametersValue.length < parameters.length)
      throw new IllegalArgumentException ("Too few parameters");

    return expressionTree.computeExpression (interpreter, parametersValue);
  }
}
