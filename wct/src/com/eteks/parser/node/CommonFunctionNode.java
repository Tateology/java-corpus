/*
 * @(#)CommonFunctionNode.java   01/01/98
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
package com.eteks.parser.node;

import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;

/**
 * Node matching a common function.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class CommonFunctionNode extends ConstantNode implements ParameterizedNode
{
  private ExpressionNode parameter;

  /**
   * Creates the node of a common function.
   * @param functionKey the key of a common function of <code>Syntax</code>.
   */
  public CommonFunctionNode (Object functionKey)
  {
    super (functionKey);
  }

  /**
   * Stores the parameter of this function.
   */
  public void addParameter (ExpressionNode parameter)
  {
    this.parameter = parameter;
  }

  public int getParameterCount ()
  {
    return parameter != null ? 1 : 0;
  }

  /**
   * Returns the node matching the parameter.
   * @return the node of an expression.
   */
  public ExpressionNode getParameter ()
  {
    return parameter;
  }

  /**
   * Returns the value returned by the <code>getCommonFunctionValue ()</code> method
   * of <code>interpreter</code> with the key of this function and the computed value
   * of its parameter as parameters of <code>getCommonFunctionValue ()</code>.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of this function. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getCommonFunctionValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    Object param = parameter.computeExpression (interpreter, parametersValue);
    return interpreter.getCommonFunctionValue (getKey (), param);
  }

  /**
   * Returns the double value of this function computing with its parameter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of this function.
   */
  public double computeExpression (double [] parametersValue)
  {
    double number = parameter.computeExpression (parametersValue);
    Object commonFunctionKey = getKey ();

    if (commonFunctionKey.equals (Syntax.FUNCTION_LN))
      return Math.log (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_LOG))
      return Math.log (number) / Math.log (10.);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_EXP))
      return Math.exp (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SQR))
      return number * number;
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SQRT))
      return Math.sqrt (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_COS))
      return Math.cos (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SIN))
      return Math.sin (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_TAN))
      return Math.tan (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ACOS))
      return Math.acos (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ASIN))
      return Math.asin (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ATAN))
      return Math.atan (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_COSH))
      return (Math.exp (number) + Math.exp (-number)) / 2.;
    else if (commonFunctionKey.equals (Syntax.FUNCTION_SINH))
      return (Math.exp (number) - Math.exp (-number)) / 2.;
    else if (commonFunctionKey.equals (Syntax.FUNCTION_TANH))
      return   (Math.exp (number) - Math.exp (-number))
                         / (Math.exp (number) + Math.exp (-number));
    else if (commonFunctionKey.equals (Syntax.FUNCTION_INTEGER))
      return (long)number;
    else if (commonFunctionKey.equals (Syntax.FUNCTION_FLOOR))
      return Math.floor (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_CEIL))
      return Math.ceil (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ROUND))
      return Math.rint (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ABS))
      return Math.abs (number);
    else if (commonFunctionKey.equals (Syntax.FUNCTION_OPPOSITE))
      return -number;
    else if (commonFunctionKey.equals (Syntax.FUNCTION_NOT))
      return number != 0 ? 1 : 0;
    else
      throw new IllegalArgumentException ("Common function key " + commonFunctionKey + " not implemented");
  }
}

