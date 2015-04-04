/*
 * @(#)BinaryOperatorNode.java   01/01/98
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
 * Node matching a binary operator.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class BinaryOperatorNode extends ConstantNode implements ParameterizedNode
{
  private ExpressionNode firstOperand;
  private ExpressionNode secondOperand;

  /**
   * Creates the node of a binary operator.
   * @param operatorKey the key of a binary operator of <code>Syntax</code>.
   */
  public BinaryOperatorNode (Object operatorKey)
  {
    super (operatorKey);
  }

  /**
   * Stores the operands of this operator.
   */
  public void addParameter (ExpressionNode parameter)
  {
    if (firstOperand == null)
      firstOperand = parameter;
    else
      secondOperand = parameter;
  }

  public int getParameterCount ()
  {
    return firstOperand != null
             ? (secondOperand != null ? 2 : 1)
             : 0;
  }

  /**
   * Returns the node matching the first operand.
   * @return the node of an expression.
   */
  public ExpressionNode getFirstOperand ()
  {
    return firstOperand;
  }

  /**
   * Returns the node matching the second operand.
   * @return the node of an expression.
   */
  public ExpressionNode getSecondOperand ()
  {
    return secondOperand;
  }

  /**
   * Returns the value returned by the <code>getBinaryOperatorValue ()</code> method
   * of <code>interpreter</code> with the key of this operator and the computed value
   * of its operands as parameters.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of this operator. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getBinaryOperatorValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    Object operand1 = firstOperand.computeExpression (interpreter, parametersValue);
    Object operand2 = secondOperand.computeExpression (interpreter, parametersValue);
    return interpreter.getBinaryOperatorValue (getKey (), operand1, operand2);
  }

  /**
   * Returns the double value of this operator node operating on its operands.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of this operator.
   */
  public double computeExpression (double [] parametersValue)
  {
    double number1 = firstOperand.computeExpression (parametersValue);
    double number2 = secondOperand.computeExpression (parametersValue);
    Object binaryOperatorKey = getKey ();

    if (binaryOperatorKey.equals (Syntax.OPERATOR_ADD))
      return number1 + number2;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SUBSTRACT))
      return number1 - number2;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MULTIPLY))
      return number1 * number2;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_DIVIDE))
      return number1 / number2;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_POWER))
      return Math.pow (number1, number2);
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MODULO))
    {
      double modulo = number1 - number2 * (int)(number1 / number2);
      // If dividend and divisor are not of the same sign, add divisor
      if (   number1 < 0 && number2 > 0
          || number1 > 0 && number2 < 0)
        modulo += number2;
      return modulo;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_REMAINDER))
      return number1 % number2;
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
      return number1 != FALSE_DOUBLE || number2 != FALSE_DOUBLE
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_AND))
      return number1 != FALSE_DOUBLE && number2 != FALSE_DOUBLE
               ? TRUE_DOUBLE : FALSE_DOUBLE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_XOR))
      return    number1 != FALSE_DOUBLE && number2 == FALSE_DOUBLE
             || number1 == FALSE_DOUBLE && number2 != FALSE_DOUBLE
                ? TRUE_DOUBLE : FALSE_DOUBLE;
    else
      if (Math.floor (number1) != number1)
        throw new IllegalArgumentException ("Operand " + number1 + " of bit operator not an integer");
      else if (Math.floor (number2) != number2)
        throw new IllegalArgumentException ("Operand " + number2 + " of bit operator not an integer");
      else
        if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_OR))
          return (long)number1 | (long)number2;
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_XOR))
          return (long)number1 ^ (long)number2;
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_AND))
          return (long)number1 & (long)number2;
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_LEFT))
          return (long)number1 << (long)number2;
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT))
          return (long)number1 >> (long)number2;
        else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT_0))
          return (long)number1 >>> (long)number2;
        else
          // User binary operators must be implemented in an interpreter
          throw new IllegalArgumentException ("Binary operator key " + binaryOperatorKey + " not implemented");
  }
}
