/*
 * @(#)ExpressionNode.java   01/01/98
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

import java.io.Serializable;

import com.eteks.parser.Interpreter;

/**
 * Interface implemented by the nodes of the tree of a compiled expression.
 * This tree is built during the parsing of a function or of an expression, and
 * is used at interpretation time to get its value.
 * The <code>computeExpression ()</code> methods of an instance of <code>ExpressionNode</code>
 * computes the value of a node at interpretation time. One method uses the matching methods
 * of its <code>Interpreter</code> parameter to compute the value of a node,
 * and the other method computes double values directly.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.Interpreter
 * @see     com.eteks.parser.CompiledFunction
 * @see     com.eteks.parser.CompiledExpression
 */
public interface ExpressionNode extends Serializable
{
  /**
   * Returns the value of the expression matching this node. This method can return
   * a computed value returned by one of the methods of <code>Interpreter</code>,
   * the value of a parameter of a function stored in the array <code>parametersValue</code>
   * or any other type of value.
   * @param  interpreter     the runtime interpreter used to compute of the value of the different literals,
   *                         constants, operators and functions available in the <code>Syntax</code> interface
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of the expression.
   * @see    com.eteks.parser.CompiledFunction
   */
  Object computeExpression (Interpreter interpreter,
                            Object []   parametersValue);

  /**
   * Returns the value of the expression matching this node. This method is implemented
   * to compute directly the double value result of this node operation.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the value of the expression.
   * @see    com.eteks.parser.CompiledFunction
   */
  double computeExpression (double [] parametersValue);
}
