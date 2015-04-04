/*
 * @(#)CompiledExpression.java   10/22/00
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
import java.util.Hashtable;

import com.eteks.parser.node.ExpressionNode;

/**
 * Compiled expression using parameters and able to compute its value
 * with an interpreter. Instances of this class are returned by the
 * <code>compileExpression ()</code> method of the <code>ExpressionParser</code> class,
 * and can be computed with parameters value. This method parses the definition of an expression
 * and builds a tree of type <code>ExpressionNode</code> matching the definition.<br>
 * The two <code>computeExpression ()</code> methods of this class allow to compute
 * the value of an expression according to the value of the parameters returned by
 * the <code>getParameterValue ()</code> method of its instance of <code>ExpressionParameter</code> :
 * <UL><LI><code>double computeExpression ()</code> is the easiest
 *         method to use if your expression computes numbers.</LI>
 *     <LI><code>Object computeExpression (Interpreter interpreter)</code>
 *         allows to choose the implementation of the interpreter that computes
 *         the constants, operators and functions of <code>Syntax</code> with the
 *         value of parameters.</LI></UL>
 * For example, the 2 following calls to <code>computeExpression ()</code> returns the same
 * result :
 * <BLOCKQUOTE><PRE> // A dummy class implementing ExpressionParameter supporting identifiers x and y
 * class XYParameter implements ExpressionParameter
 * {
 *   public Object getParameterKey (String parameter)
 *   {
 *     if (   "x".equalsIgnoreCase (parameter)
 *         || "y".equalsIgnoreCase (parameter))
 *       return parameter;
 *     else
 *       return null;
 *   }
 *
 *   public Object getParameterValue (Object parameterKey)
 *   {
 *     if ("x".equalsIgnoreCase ((String)parameterKey))
 *       return new Double (1);
 *     else // identifierKey equal to y
 *       return new Double (2);
 *   }
 * }
 *
 * ExpressionParser parser = new ExpressionParser (new XYParameter ());
 * CompiledExpression expression = parser.compileExpression ("=x * ln (x)");
 *
 * double doubleResult1 = expression.<b>computeExpression</b> ();
 *
 * Interpreter interpreter = new DoubleInterpreter ();
 * Object doubleResult2 = expression.<b>computeExpression</b> (interpreter);</PRE></BLOCKQUOTE>
 *
 * The other methods of this class returns the different properties of an expression
 * (parameters, definition,...).
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.ExpressionParser
 * @see     com.eteks.parser.Interpreter
 */
public class CompiledExpression implements Serializable
{
  private String              definition;
  private Hashtable           parameters;
  private ExpressionParameter expressionParameter;
  private ExpressionNode      expressionTree;

  /**
   * Creates a compiled expression with its parameters <code>parameters</code>,
   * its definition <code>definition</code>, its instance of <code>ExpressionParameter</code>
   * <code>expressionParameter</code> and the tree <code>expressionTree</code> matching the definition.
   * Expressions are not named.
   * @param definition          the definition of this expression. This is
   *                            the parsed string from which was created this expression.
   * @param parameters          a hashtable containing all the pairs (parameter name,  parameter key)
   *                            of the expression (extracted from the definition).
   * @param expressionParameter the instance of <code>ExpressionParameter</code> used to retrieve
   *                            the value of each parameter with its key.
   * @param expressionTree      the tree built by the parser matching the expression.
   */
  public CompiledExpression (String              definition,
                             Hashtable           parameters,
                             ExpressionParameter expressionParameter,
                             ExpressionNode      expressionTree)
  {
    this.definition          = definition;
    this.parameters          = parameters;
    this.expressionParameter = expressionParameter;
    this.expressionTree      = expressionTree;
    if (parameters == null)
      this.parameters = new Hashtable ();
  }

  /**
   * Returns the definition of this expression. This is the string parsed
   * by the instance of <code>ExpressionParser</code> that created this expression.
   * @return  the definition of this expression.
   */
  public String getDefinition ()
  {
    return definition;
  }

  /**
   * Returns a hashtable that contains all the parameters
   * found during the parsing of the definition of this expression. This
   * hashtable stores a pair of (key,value) for each parameter required by this expression
   * with its name as the key and a key matching the parameter as the value.
   * A parameter key is returned by the <code>getParameterKey ()</code> method of the
   * instance of <code>ExpressionParameter</code> of this expression, and is used
   * by the interpreter to get the value of each parameter using the
   * <code>getParameterValue ()</code> method of the instance of <code>ExpressionParameter</code>
   * of this expression.
   * @return a hashtable containing the parameters name and their matching key.
   * @see    com.eteks.parser.ExpressionParameter
   */
  public Hashtable getParameters ()
  {
    return parameters;
  }

  /**
   * Returns the number of parameters this expression is requiring at runtime.
   * @return the number of parameters (may be equal to 0) : it's equal to <code>getParameters ().size ()</code>.
   */
  public int getParameterCount ()
  {
    return parameters.size ();
  }

  /**
   * Returns the instance of <code>ExpressionParameter</code> of this expression used to get
   * the value of each parameter with its key. This is the same object
   * as the one stored by the intance of <code>ExpressionParser</code> used
   * to compile this expression.
   * @return the instance of <code>ExpressionParameter</code> of this expression.
   * @see com.eteks.parser.ExpressionParser
   */
  public ExpressionParameter getExpressionParameter ()
  {
    return expressionParameter;
  }

  /**
   * Returns the tree of this expression, instance of <code>ExpressionNode</code>.
   * The returned node is the root of the tree matching the definition of this expression.
   * For example, the expression <code>=x * ln (x)</code> has the following tree :
   * <UL><LI><code>BinaryOperatorNode</code> with <code>OPERATOR_MULTIPLY</code> key</LI>
   *     <UL><LI><code>ExpressionParameterNode</code> for parameter <code>x</code> key</LI>
   *         <LI><code>CommonFunctionNode</code> with <code>FUNCTION_LN</code> key</LI>
   *         <UL><LI><code>ExpressionParameterNode</code> for parameter <code>x</code> key</LI></UL></UL></UL>
   * @return the tree of this expression.
   */
  public ExpressionNode getExpressionTree ()
  {
    return expressionTree;
  }

  /**
   * Returns the result of the expression computed with the value of its parameters returned by
   * the <code>getParameterValue ()</code> method of the instance of <code>ExpressionParameter</code>
   * of this expression.
   * This method simply calls <code>computeExpression (null)</code> on the root of its tree.<br>
   * As this method avoid method calls to an interpreter and instantiation of objects
   * for parameters and return values, it runs much faster than the
   * <code>computeExpression (Interpreter)</code> method of this class.
   * It can compute only double numbers and all the values returned by
   * the <code>getParameterValue ()</code> method of the instance of <code>ExpressionParameter</code>
   * must be instances of <code>Number</code>.
   * @return the result of the function.
   * @see com.eteks.parser.node.ExpressionNode
   */
  public double computeExpression ()
  {
    return expressionTree.computeExpression (null);
  }

  /**
   * Returns the result of the expression computed with the value of its parameters returned by
   * the <code>getParameterValue ()</code> method of the instance of <code>ExpressionParameter</code>
   * of this expression.
   * This method simply calls <code>computeExpression (interpreter, null)</code> on the root of its tree.<br>
   * This method is thread safe.
   * @param  interpreter  the runtime interpreter used to compute the operations of <code>Syntax</code>.
   * @return the result of the function. The returned object may be of any
   *         class (<code>Double</code>, <code>String</code>, ...) according to
   *         <code>interpreter</code> implementation.
   * @see com.eteks.parser.node.ExpressionNode
   */
  public Object computeExpression (Interpreter interpreter)
  {
    return expressionTree.computeExpression (interpreter, null);
  }
}

