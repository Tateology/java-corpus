/*
 * @(#)ConstantNode.java   05/15/2001
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
 * Node matching a constant.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class ConstantNode implements ExpressionNode
{
  /**
   * The double constant matching the constant FALSE (equal to <code>0</code>).
   */
  public static final double FALSE_DOUBLE = 0;
  /**
   * The double constant matching the constant TRUE (equal to <code>1</code>).
   */
  public static final double TRUE_DOUBLE  = 1;

  private Object  key;

  /**
   * Creates the node of a constant.
   * @param key the key of a constant of <code>Syntax</code>.
   */
  public ConstantNode (Object key)
  {
    this.key = key;
  }

  /**
   * Returns the syntactic key of this node.
   * @return a key of <code>Syntax</code>.
   */
  public Object getKey ()
  {
    return key;
  }

  /**
   * Returns the value returned by the <code>getConstantValue ()</code> method
   * of <code>interpreter</code> with the key of this constant as parameter.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue not used.
   * @return the interpreted value of this constant. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getConstantValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    return interpreter.getConstantValue (key);
  }

  /**
   * Returns the double value of this constant. The value <code>FALSE_DOUBLE</code> is
   * returned for the <code>CONSTANT_FALSE</code> constant key, and the value
   * <code>TRUE_DOUBLE</code> is returned for the <code>CONSTANT_TRUE</code> constant key.
   * @param  parametersValue not used.
   * @return the value of this constant.
   */
  public double computeExpression (double [] parametersValue)
  {
    if (Syntax.CONSTANT_PI.equals (key))
      return Math.PI;
    else if (Syntax.CONSTANT_E.equals (key))
      return Math.E;
    else if (Syntax.CONSTANT_FALSE.equals (key))
      return FALSE_DOUBLE;
    else if (Syntax.CONSTANT_TRUE.equals (key))
      return TRUE_DOUBLE;
    else
      throw new IllegalArgumentException ("Constant key " + key + " not implemented");
  }
}

