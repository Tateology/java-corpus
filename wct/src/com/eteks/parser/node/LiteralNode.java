/*
 * @(#)LiteralNode.java   01/01/98
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
 * Node matching a literal value. This node stores the value of a literal.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class LiteralNode implements ExpressionNode
{
  private Object  value;

  /**
   * Creates a node of a literal whose value is <code>value</code>.
   * @param value the value of the literal.
   */
  public LiteralNode (Object value)
  {
    this.value = value;
  }

  /**
   * Returns the stored value of this literal.
   * @return the value of this literal.
   */
  public Object getValue ()
  {
    return value;
  }

  /**
   * Returns the value returned by the <code>getLiteralValue ()</code> method
   * of <code>interpreter</code> with the stored value of this literal as parameter.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue not used.
   * @return the interpreted value of this literal. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getLiteralValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    return interpreter.getLiteralValue (value);
  }

  /**
   * Returns the double value of this literal.
   * @param  parametersValue not used.
   * @return the value of this literal.
   */
  public double computeExpression (double [] parametersValue)
  {
    if (value instanceof Number)
      return ((Number)value).doubleValue ();
    else
      throw new IllegalArgumentException ("Literal " + value + " not an instance of Number");
  }
}

