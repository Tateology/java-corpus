/*
 * @(#)FunctionNode.java   01/01/98
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

import java.util.Vector;

import com.eteks.parser.CompiledFunction;
import com.eteks.parser.DoubleInterpreter;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;

/**
 * Node matching a function call.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class FunctionNode implements ParameterizedNode
{
  private Vector   parametersExpression = new Vector (2, 1);
  private Function function;
  private boolean  recursiveCall;

  /**
   * Creates the node of a function.
   * @param function      a previously compiled function or any instance of a Java written
   *                      class implementing the <code>Function</code> interface.
   * @param recursiveCall <code>true</code> if this is a recursive call.
   */
  public FunctionNode (Function function,
                       boolean  recursiveCall)
  {
    this.function = function;
    this.recursiveCall  = recursiveCall;
  }

  /**
   * Returns the function of this node.
   * @return a <code>Function</code> instance.
   */
  public Function getFunction ()
  {
    return function;
  }

  /**
   * Returns <code>true</code> if this is a recursive call.
   * @return <code>true</code> or <code>false</code>.
   */
  public boolean isRecursiveCall ()
  {
    return recursiveCall;
  }

  public void addParameter (ExpressionNode parameter)
  {
    parametersExpression.addElement (parameter);
  }

  public int getParameterCount ()
  {
    return parametersExpression.size ();
  }

  /**
   * Returns the expression at index <code>index</code> that will be passed
   * as parameter to this function.
   * @return the expression stored at index <code>index</code>.
   */
  public ExpressionNode getParameter (int index)
  {
    return (ExpressionNode)parametersExpression.elementAt (index);
  }

  /**
   * Returns the value returned by the <code>getFunctionValue ()</code> method
   * of <code>interpreter</code> with the instance of <code>Function</code> of this node
   * and the computed value of its parameters as parameters of <code>getFunctionValue ()</code>.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of this function. The type of the returned value
   *         depends on the implementation of the interpreter.
   * @see    com.eteks.parser.Interpreter#getFunctionValue
   */
  public Object computeExpression (Interpreter interpreter,
                                   Object []   parametersValue)
  {
    Object [] functionParameters = new Object [parametersExpression.size ()];
    for (int i = 0; i < parametersExpression.size (); i++)
      functionParameters [i] = getParameter (i).computeExpression (interpreter, parametersValue);

    return interpreter.getFunctionValue (function, functionParameters, recursiveCall);
  }

  private static Interpreter doubleInterpreter;

  /**
   * Returns the double value of this function call. If the called function isn't an instance of
   * <code>CompiledFunction</code>, an instance of <code>DoubleInterpreter</code>
   * is passed to the <code>computeFunction ()</code> of the function to
   * compute its value.
   * @param  interpreter     runtime interpreter.
   * @param  parametersValue the value of parameters passed to compute a compiled function.
   * @return the computed value of this function.
   */
  public double computeExpression (double [] parametersValue)
  {
    if (function instanceof CompiledFunction)
    {
      double [] functionParameters = new double [parametersExpression.size ()];
      for (int i = 0; i < parametersExpression.size (); i++)
        functionParameters [i] = getParameter (i).computeExpression (parametersValue);

      return ((CompiledFunction)function).computeFunction (functionParameters);
    }
    else
    {
      // As Function interface declares only the method
      // public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
      // the value is computed with the DoubleInterpreter interpreter class
      Double [] functionParameters = new Double [parametersExpression.size ()];
      for (int i = 0; i < parametersExpression.size (); i++)
        functionParameters [i] = new Double (getParameter (i).computeExpression (parametersValue));

      if (doubleInterpreter == null)
        doubleInterpreter = new DoubleInterpreter ();

      Object result = function.computeFunction (doubleInterpreter, functionParameters);
      if (result instanceof Number)
        return ((Number)result).doubleValue ();
      else
        throw new IllegalArgumentException ("Result " + result + " not an instance of Number");
    }
  }
}
