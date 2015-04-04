/*
 * @(#)ConditionNode.java   01/01/98
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
 * Node matching a condition.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class ConditionNode implements ParameterizedNode
{
  private ExpressionNode ifExpression;
  private ExpressionNode thenExpression;
  private ExpressionNode elseExpression;

  /**
   * Stores the if, then and else expressions of this condition.
   */
  public void addParameter (ExpressionNode parameter)
  {
    if (ifExpression == null)
      ifExpression = parameter;
    else
      if (thenExpression == null)
        thenExpression = parameter;
      else
        elseExpression = parameter;
  }

  public int getParameterCount ()
  {
    return ifExpression != null
             ? (thenExpression != null
                 ? (elseExpression != null ? 3 : 2)
                 : 1)
             : 0;
  }

  /**
   * Returns the node matching the <i>if</i> expression.
   * @return the node of an expression.
   */
  public ExpressionNode getIfExpression ()
  {
    return ifExpression;
  }

  /**
   * Returns the node matching the <i>then</i> expression.
   * @return the node of an expression.
   */
  public ExpressionNode getThenExpression ()
  {
    return thenExpression;
  }

  /**
   * Returns the node matching the <i>else</i> expression.
   * @return the node of an expression.
   */
  public ExpressionNode getElseExpression ()
  {
    return elseExpression;
  }

  /**
   * Returns the result of this condition.
   * <br>If the <code>supportsRecursiveCall ()</code> method of the <code>interpreter</code>
   * returns <code>true</code>, the computed value of <i>then</i> or <i>else</i> expressions
   * is returned depending on whether the <code>isTrue ()</code> method of the <code>interpreter</code>
   * returning <code>true</code> or not. The computed value of the <i>if</i> expression
   * is passed as parameter to the <code>isTrue ()</code> method.<br>
   * If the <code>supportsRecursiveCall ()</code> method returns <code>false</code>,
   * this method returns the result of the <code>getConditionValue ()</code>
   * method of <code>interpreter</code> with the computed value of the <i>if</i>, <i>then</i>
   * and <i>else</i> expressions as parameters.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of the condition. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#isTrue
   * @see    com.eteks.parser.Interpreter#getConditionValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    if (interpreter.supportsRecursiveCall ())
      return interpreter.isTrue (ifExpression.computeExpression (interpreter, parametersValue))
                ? thenExpression.computeExpression (interpreter, parametersValue)
                : elseExpression.computeExpression (interpreter, parametersValue);
    else
      return interpreter.getConditionValue (ifExpression.computeExpression (interpreter, parametersValue),
                                            thenExpression.computeExpression (interpreter, parametersValue),
                                            elseExpression.computeExpression (interpreter, parametersValue));
  }

  /**
   * Returns the computed value of <i>then</i> or <i>else</i> expressions
   * depending on the computed value of the <i>if</i> expression.
   * The <i>if</i> expression is compared to <code>ConstantNode.FALSE_DOUBLE</code>.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of the condition.
   */
  public double computeExpression (double [] parametersValue)
  {
    return ifExpression.computeExpression (parametersValue) != ConstantNode.FALSE_DOUBLE
             ? thenExpression.computeExpression (parametersValue)
             : elseExpression.computeExpression (parametersValue);
  }
}
