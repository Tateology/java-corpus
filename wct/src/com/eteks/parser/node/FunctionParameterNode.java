/*
 * @(#)FunctionParameterNode.java   01/01/98
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

/**
 * Node matching a parameter of a compiled function.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class FunctionParameterNode implements ExpressionNode
{
  private int parameterIndex;

  /**
   * Creates the node for the parameter at index <code>parameterIndex</code>.
   * @param parameterIndex the index of the parameter.
   */
  public FunctionParameterNode (int parameterIndex)
  {
    this.parameterIndex = parameterIndex;
  }

  /**
   * Returns the index of this parameter.
   * @return the index of this parameter.
   */
  public int getParameterIndex ()
  {
    return parameterIndex;
  }

  /**
   * Returns the value returned by the <code>getParameterValue ()</code> method
   * of <code>interpreter</code> with the parameter in <code>parametersValue</code>
   * at the index returned by <code>getParameterIndex ()</code> as argument of
   * <code>getParameterValue ()</code>.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the interpreted value of this parameter. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getParameterValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    return interpreter.getParameterValue (parametersValue [parameterIndex]);
  }

  /**
   * Returns the double value of the parameter in <code>parametersValue</code>
   * at the index returned by <code>getParameterIndex ()</code>.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of the parameter at the index returned by <code>getParameterIndex ()</code>.
   */
  public double computeExpression (double [] parametersValue)
  {
    return parametersValue [parameterIndex];
  }
}

