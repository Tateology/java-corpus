/*
 * @(#)UnaryOperatorNode.java   01/01/98
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
 * Node matching an unary operator.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class UnaryOperatorNode extends ConstantNode implements ParameterizedNode
{
  private ExpressionNode operand;

  /**
   * Creates the node of an unary operator.
   * @param operatorKey the key of an unary operator of <code>Syntax</code>.
   */
  public UnaryOperatorNode (Object operatorKey)
  {
    super (operatorKey);
  }

  /**
   * Stores the operand of this operator.
   */
  public void addParameter (ExpressionNode operand)
  {
    this.operand = operand;
  }

  public int getParameterCount ()
  {
    return operand != null ? 1 : 0;
  }

  /**
   * Returns the node matching the operand.
   * @return the node of an expression.
   */
  public ExpressionNode getOperand ()
  {
    return operand;
  }

  /**
   * Returns the value returned by the <code>getUnaryOperatorValue ()</code> method
   * of <code>interpreter</code> with the key of this operator and the computed value
   * of its operand as parameters.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of this operator. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getUnaryOperatorValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    Object param = operand.computeExpression (interpreter, parametersValue);
    return interpreter.getUnaryOperatorValue (getKey (), param);
  }

  /**
   * Returns the double value of this operator node operating on it operand.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of this operator.
   */
  public double computeExpression (double []   parametersValue)
  {
    double number = operand.computeExpression (parametersValue);
    Object unaryOperatorKey = getKey ();

    if (unaryOperatorKey.equals (Syntax.OPERATOR_OPPOSITE))
      return -number;
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_POSITIVE))
      return number;
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_NOT))
      return number != FALSE_DOUBLE ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_NOT))
    {
      if (Math.floor (number) != number)
        throw new IllegalArgumentException ("Operator operand not an integer");
      return ~(long)number;
    }
    else
      throw new IllegalArgumentException ("Unary operator key " + unaryOperatorKey + " not implemented");
  }
}
