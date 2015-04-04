/*
 * @(#)ExpressionParameterNode.java   05/09/2001
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

import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.Interpreter;

/**
 * Node matching a parameter of a compiled expression.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class ExpressionParameterNode implements ExpressionNode
{
  private ExpressionParameter expressionParameter;
  private Object              parameterKey;

  /**
   * Creates the node for the parameter of key <code>parameterKey</code>.
   * @param expressionParameter the instance of <code>ExpressionParameter</code>
   *                            that will return the value of a parameter depending on its key.
   * @param parameterKey        the key of this parameter.
   */
  public ExpressionParameterNode (ExpressionParameter expressionParameter,
                                  Object              parameterKey)
  {
    this.expressionParameter = expressionParameter;
    this.parameterKey        = parameterKey;
  }

  /**
   * Returns the key of this parameter.
   * @return the key of this parameter.
   */
  public Object getParameterKey ()
  {
    return parameterKey;
  }

  /**
   * Returns the instance of <code>ExpressionParameter</code> able to returns the key
   * of a parameter and its value.
   * @return an instance of <code>ExpressionParameter</code>.
   */
  public ExpressionParameter getExpressionParameter ()
  {
    return expressionParameter;
  }

  /**
   * Returns the value returned by the <code>getParameterValue ()</code> method
   * of <code>interpreter</code> with the returned value of <code>getParameterValue ()</code>
   * with the key of this parameter as argument of <code>getParameterValue ()</code>.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue not used.
   * @return the interpreted value of this parameter. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getParameterValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    return interpreter.getParameterValue (expressionParameter.getParameterValue (parameterKey));
  }

  /**
   * Returns the double value returned by the <code>getParameterValue ()</code> method
   * of the the <code>ExpressionParameter</code> instance of this parameter and its key as
   * parameter. The <code>getParameterValue ()</code> method must return an instance of
   * <code>Number</code>
   * @param  parametersValue not used.
   * @return the value of this parameter.
   */
  public double computeExpression (double [] parametersValue)
  {
    Object parameter = expressionParameter.getParameterValue (parameterKey);
    if (parameter instanceof Number)
      return ((Number)parameter).doubleValue ();
    else
      throw new IllegalArgumentException ("Parameter " + parameter + " not an instance of Number");
  }
}

