/*
 * @(#)MathMLInterpreter.java   22/05/2001
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
package com.eteks.parser;

import java.util.Hashtable;

/**
 * Runtime interpreter that outputs a MathML 2.0 string.
 * MathML is an application of XML for describing mathematics and
 * is a W3C recommendation available at <A HREF="http://www.w3.org/Math">http://www.w3.org/Math</A>.<br>
 * This class interprets the contants, operators and functions
 * defined in <code>Syntax</code> with their matching elements of MathML.
 * The value of parameters passed to compute an instance of
 * <code>CompiledFunction</code> with this interpreter may be either strings
 * or numbers instances of <code>Number</code>. If they are strings the MathML
 * element &lt;ci&gt; will be used, otherwise the MathML &lt;cn&gt; will be used as these
 * parameters were literals.<br>
 * Examples of output with a <code>MathMLInterpreter</code> :
 * <BLOCKQUOTE><PRE> // Create a parser using DefaultSyntax
 * FunctionParser parser = new FunctionParser ();
 *
 * // Compile functions
 * CompiledFunction function1 = parser.compileFunction("f(x) = x ^ 2 + 2 * x + 1");
 * // Add function1 to syntax to be able to call it elsewhere
 * ((DefaultSyntax)parser.getSyntax ()).addFunction (function1);
 * CompiledFunction function2 = parser.compileFunction("g(x) = x * f(x)");
 *
 * // Interpret these functions
 * Interpreter interpreter = new MathMLInterpreter ();
 * System.out.println ("f(2.2) =>\n" + function1.computeFunction (interpreter, new Object [] {new Double (2.2)}));
 * System.out.println ("f(x) =>\n"   + function1.computeFunction (interpreter, function1.getParameters ()));
 * System.out.println ("g(y) =>\n" + function2.computeFunction (interpreter, new Object [] {"y"}));</PRE></BLOCKQUOTE>
 * The output of this examples is :
 * <BLOCKQUOTE><PRE> f(2.2) =>
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;power/&gt;
 *  &lt;cn&gt;2.2&lt;/cn&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;/apply&gt;
 *  &lt;apply&gt;
 *  &lt;times/&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;cn&gt;2.2&lt;/cn&gt;
 *  &lt;/apply&gt;
 *  &lt;/apply&gt;
 *  &lt;cn type="integer"&gt;1&lt;/cn&gt;
 *  &lt;/apply&gt;
 *
 *  f(x) =>
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;power/&gt;
 *  &lt;ci&gt;x&lt;/ci&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;/apply&gt;
 *  &lt;apply&gt;
 *  &lt;times/&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;ci&gt;x&lt;/ci&gt;
 *  &lt;/apply&gt;
 *  &lt;/apply&gt;
 *  &lt;cn type="integer"&gt;1&lt;/cn&gt;
 *  &lt;/apply&gt;
 *
 *  g(y) =>
 *  &lt;apply&gt;
 *  &lt;times/&gt;
 *  &lt;ci&gt;y&lt;/ci&gt;
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;plus/&gt;
 *  &lt;apply&gt;
 *  &lt;power/&gt;
 *  &lt;ci&gt;y&lt;/ci&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;/apply&gt;
 *  &lt;apply&gt;
 *  &lt;times/&gt;
 *  &lt;cn type="integer"&gt;2&lt;/cn&gt;
 *  &lt;ci&gt;y&lt;/ci&gt;
 *  &lt;/apply&gt;
 *  &lt;/apply&gt;
 *  &lt;cn type="integer"&gt;1&lt;/cn&gt;
 *  &lt;/apply&gt;
 *  &lt;/apply&gt;</PRE></BLOCKQUOTE>
 *
 * This interpreter supports conditions but doesn't support recursive compiled functions.<br>
 * The methods of this class are thread safe.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.Syntax
 * @see     com.eteks.parser.CompiledFunction
 */
public class MathMLInterpreter implements Interpreter
{
  private static final Hashtable constants       = new Hashtable ();
  private static final Hashtable unaryOperators  = new Hashtable ();
  private static final Hashtable binaryOperators = new Hashtable ();
  private static final Hashtable commonFunctions = new Hashtable ();

  static
  {
    // Fill the hashtables with the elements of MathML
    constants.put (Syntax.CONSTANT_PI,    "<pi/>");
    constants.put (Syntax.CONSTANT_E,     "<exponentiale/>");
    constants.put (Syntax.CONSTANT_TRUE,  "<true/>");
    constants.put (Syntax.CONSTANT_FALSE, "<false/>");

    unaryOperators.put (Syntax.OPERATOR_OPPOSITE,    "<minus/>");
    unaryOperators.put (Syntax.OPERATOR_LOGICAL_NOT, "<not/>");

    binaryOperators.put (Syntax.OPERATOR_LOGICAL_OR,       "<or/>");
    binaryOperators.put (Syntax.OPERATOR_LOGICAL_XOR,      "<xor/>");
    binaryOperators.put (Syntax.OPERATOR_LOGICAL_AND,      "<and/>");
    binaryOperators.put (Syntax.OPERATOR_EQUAL,            "<eq/>");
    binaryOperators.put (Syntax.OPERATOR_DIFFERENT,        "<neq/>");
    binaryOperators.put (Syntax.OPERATOR_GREATER_OR_EQUAL, "<geq/>");
    binaryOperators.put (Syntax.OPERATOR_LESS_OR_EQUAL,    "<leq/>");
    binaryOperators.put (Syntax.OPERATOR_GREATER,          "<gt/>");
    binaryOperators.put (Syntax.OPERATOR_LESS,             "<lt/>");
    binaryOperators.put (Syntax.OPERATOR_ADD,              "<plus/>");
    binaryOperators.put (Syntax.OPERATOR_SUBSTRACT,        "<minus/>");
    binaryOperators.put (Syntax.OPERATOR_MODULO,           "<rem/>"); // TODO is it Ok ?
    binaryOperators.put (Syntax.OPERATOR_REMAINDER,        "<rem/>");
    binaryOperators.put (Syntax.OPERATOR_DIVIDE,           "<divide/>");
    binaryOperators.put (Syntax.OPERATOR_MULTIPLY,         "<times/>");
    binaryOperators.put (Syntax.OPERATOR_POWER,            "<power/>");

    commonFunctions.put (Syntax.FUNCTION_LN,       "<ln/>");
    commonFunctions.put (Syntax.FUNCTION_LOG,      "<log/>");
    commonFunctions.put (Syntax.FUNCTION_EXP,      "<exp/>");
    commonFunctions.put (Syntax.FUNCTION_SQRT,     "<root/><degree><ci type=\"integer\">2</ci></degree>");
    commonFunctions.put (Syntax.FUNCTION_COS,      "<cos/>");
    commonFunctions.put (Syntax.FUNCTION_SIN,      "<sin/>");
    commonFunctions.put (Syntax.FUNCTION_TAN,      "<tan/>");
    commonFunctions.put (Syntax.FUNCTION_ACOS,     "<arccos/>");
    commonFunctions.put (Syntax.FUNCTION_ASIN,     "<arcsin/>");
    commonFunctions.put (Syntax.FUNCTION_ATAN,     "<arctan/>");
    commonFunctions.put (Syntax.FUNCTION_COSH,     "<cosh/>");
    commonFunctions.put (Syntax.FUNCTION_SINH,     "<sinh/>");
    commonFunctions.put (Syntax.FUNCTION_TANH,     "<tanh/>");
    // TODO MathML Integer part ?
    // commonFunctions.put (Syntax.FUNCTION_INTEGER,  "INT");
    commonFunctions.put (Syntax.FUNCTION_FLOOR,    "<floor/>");
    commonFunctions.put (Syntax.FUNCTION_CEIL,     "<ceiling/>");
    commonFunctions.put (Syntax.FUNCTION_ROUND,    "<ceiling/>");
    commonFunctions.put (Syntax.FUNCTION_ABS,      "<abs/>");
    commonFunctions.put (Syntax.FUNCTION_OPPOSITE, "<minus/>");
    commonFunctions.put (Syntax.FUNCTION_NOT,      "<not/>");
  }

  public MathMLInterpreter ()
  {
  }

   /**
   * Returns the MathML element of the literal <code>literal</code> interpreted as a MathML tag.
   * <code>literal</code> must be an instance of <code>Number</code>.
   * @param  literal an instance of <code>Number</code>.
   * @return the MathML element of the literal.
   * @throws IllegalArgumentException if <code>literal</code> isn't an instance of <code>Number</code>.
   */
  public Object getLiteralValue (Object literal)
  {
    if (literal instanceof Number)
    {
      if (   literal instanceof Integer
          || literal instanceof Long)
        return "<cn type=\"integer\">" + literal + "</cn>\n";
      else if (literal instanceof Float)
      {
        if (((Float)literal).isInfinite ())
          return "<infinity/>\n";
        else if (((Float)literal).isNaN ())
          return "<notanumber/>\n";
      }
      else if (literal instanceof Double)
      {
        if (((Double)literal).isInfinite ())
          return "<infinity/>\n";
        else if (((Double)literal).isNaN ())
          return "<notanumber/>\n";
      }

      String literalString = literal.toString ();

      if (literalString.indexOf ('E') >= 0)
        return "<cn type=\"e-notation\">" + literalString + "</cn>\n";
      else
        return "<cn>" + literalString + "</cn>\n";
    }
    else
      throw new IllegalArgumentException ("Literal " + literal + " not an instance of Number, String or Character");

  }

  /**
   * Returns the MathML element of the parameter <code>parameter</code>.
   * <code>parameter</code> must be an instance of <code>Number</code>, <code>String</code> or <code>Character</code>.
   * @param parameter an instance of <code>Number</code>, <code>String</code> or <code>Character</code>.
   * @return the MathML element of the parameter.
   * @throws IllegalArgumentException if <code>parameter</code> isn't an instance of <code>Number</code>,
   *         <code>String</code> or <code>Character</code>.
   */
  public Object getParameterValue (Object parameter)
  {
    if (parameter instanceof Number)
      return getLiteralValue (parameter);
    else if (   parameter instanceof String
             || parameter instanceof Character)
    {
      if (((String)parameter).indexOf ('<') >= 0)
        return parameter; // Already interpreted
      else
        return "<ci>" + parameter + "</ci>\n";
    }
    else
      throw new IllegalArgumentException ("Parameter " + parameter + " not an instance of Number, String or Character");
  }

  /**
   * Returns the MathML element of the constant <code>constantKey</code>. <code>constantKey</code>
   * may be the key of a constant of <code>Syntax</code> (one of <code>CONSTANT_PI</code>,
   * <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code>).
   * @param constantKey the key of a constant of <code>Syntax</code>.
   * @return the MathML element of the constant.
   * @throws IllegalArgumentException if <code>constantKey</code> isn't a key of a constant of
   *         <code>Syntax</code>.
   */
  public Object getConstantValue (Object constantKey)
  {
    String mathMLConstant = (String)constants.get (constantKey);
    if (mathMLConstant != null)
      return mathMLConstant + "\n";
    else
      throw new IllegalArgumentException ("Constant key " + constantKey + " not implemented");
  }

  /**
   * Returns the MathML element describing the unary operator <code>unaryOperatorKey</code> applied
   * on the operand <code>operand</code>. <code>unaryOperatorKey</code> must be the key
   * of an unary operator of <code>Syntax</code> (one of <code>OPERATOR_POSITIVE</code>,
   * <code>OPERATOR_OPPOSITE</code>, <code>OPERATOR_LOGICAL_NOT</code>, <code>OPERATOR_BITWISE_NOT</code>).
   * @param unaryOperatorKey the key of an unary operator of <code>Syntax</code>.
   * @param operand          the operand (a MathML element).
   * @return the MathML element matching the operation.
   * @throws IllegalArgumentException if <code>unaryOperatorKey</code> isn't the key of an unary
   *         operator of <code>Syntax</code>.
   */
  public Object getUnaryOperatorValue (Object unaryOperatorKey, Object operand)
  {
    if (Syntax.OPERATOR_POSITIVE.equals (unaryOperatorKey))
      return operand;

    String mathMLUnaryOperator = (String)unaryOperators.get (unaryOperatorKey);
    if (mathMLUnaryOperator != null)
      return   "<apply>\n"
             + mathMLUnaryOperator + "\n"
             + operand
             + "</apply>\n";
    else
      throw new IllegalArgumentException ("Unary operator key " + unaryOperatorKey + " not implemented");
  }

  /**
   * Returns the MathML element describing the binary operator <code>binaryOperatorKey</code> applied on
   * the two operands <code>operand1</code> and <code>operand2</code>. <code>binaryOperatorKey</code>
   * must be the key of a binary operator of <code>Syntax</code> (one of <code>OPERATOR_ADD</code>,
   * <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>,...).
   * @param binaryOperatorKey the key of a binary operator of <code>Syntax</code>.
   * @param operand1          the first operand (a MathML element).
   * @param operand2          the second operand (a MathML element).
   * @return the MathML element matching the operation. The returned value is an instance of <code>Double</code>,
   *         <code>Long</code>, <code>Boolean</code> or <code>String</code>.
   * @throws IllegalArgumentException if <code>binaryOperatorKey</code> isn't the key
   *         of a binary operator of <code>Syntax</code>.
   */
  public Object getBinaryOperatorValue (Object binaryOperatorKey, Object operand1, Object operand2)
  {
    String mathMLBinaryOperator = (String)binaryOperators.get (binaryOperatorKey);
    if (mathMLBinaryOperator != null)
      return   "<apply>\n"
             + mathMLBinaryOperator + "\n"
             + operand1
             + operand2
             + "</apply>\n";
    else
      throw new IllegalArgumentException ("Binary operator key " + binaryOperatorKey + " not implemented");
  }

  /**
   * Returns the MathML element describing the common function <code>commonFunctionKey</code> with
   * the parameter <code>param</code>. <code>commonFunctionKey</code> must be the key
   * of a commomon function of <code>Syntax</code> (one of <code>FUNCTION_LN</code>,
   * <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,...).
   * @param commonFunctionKey the key of a common function of <code>Syntax</code>.
   * @param param             the parameter of the function (a MathML element).
   * @return the MathML element of the function.
   * @throws IllegalArgumentException if <code>commonFunctionKey</code> isn't the key of a
   *         commomon function of <code>Syntax</code>.
   */
  public Object getCommonFunctionValue (Object commonFunctionKey, Object param)
  {
    if (Syntax.FUNCTION_SQR.equals (commonFunctionKey))
      return getBinaryOperatorValue (Syntax.OPERATOR_POWER, param, new Integer (2));

    String mathMLCommonFunction = (String)commonFunctions.get (commonFunctionKey);
    if (mathMLCommonFunction != null)
      return   "<apply>\n"
             + mathMLCommonFunction + "\n"
             + param
             + "</apply>\n";
    else
      throw new IllegalArgumentException ("Unary operator key " + commonFunctionKey + " not implemented");
  }

  /**
   * Returns the MathML element describing the condition defined by the parameters
   * <code>paramIf</code>, <code>paramThen</code> or <code>paramElse</code>.
   * @param paramIf   the condition (a MathML element).
   * @param paramThen the true condition value (a MathML element).
   * @param paramElse the false condition value (a MathML element).
   * @return the MathML element of the condition.
   */
  public Object getConditionValue (Object paramIf, Object paramThen, Object paramElse)
  {
    return   "<piecewise>\n<piece>\n"
           + paramThen
           + paramIf
           + "</piece>\n<otherwise>\n"
           + paramElse
           + "</otherwise>\n<piecewise>\n";
  }

  /**
   * Throws an <code>IllegalArgumentException</code> exception because this interpreter can't
   * evaluate values. This method won't be called internally because <code>supportsRecursiveCall ()</code> returns
   * <code>false</code>.
   * @param condition the value to test.
   * @throws IllegalArgumentException because this interpreter can't evaluate values values.
   */
  public boolean isTrue (Object param)
  {
    throw new IllegalArgumentException ("Can't evaluate condition");
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>.
   */
  public boolean supportsRecursiveCall ()
  {
    return false;
  }

  /**
   * Returns the MathML element describing the function <code>function</code> call with its
   * parameters <code>parametersValue</code>.
   * As this method returns <code>function.computeFunction (this, parametersValue)</code>,
   * if <code>function</code> is an instance of <code>CompiledFunction</code> it will return the MathML
   * element matching its expression and if <code>function</code> is a Java written function
   * it will return the MathML element returned by the function if the function is able to use this
   * interpreter.
   * @param function        the function to evaluate.
   * @param parametersValue the value of function's parameters (MathML elements).
   * @param recursiveCall   <code>true</code> if the call to this function is a recursive call, meaning that
   *                        the current evaluated function calls itself.
   * @return the MathML element of the function call.
   */
  public Object getFunctionValue (Function    function,
                                  Object []   parametersValue,
                                  boolean     recursiveCall)
  {
    if (function instanceof CompiledFunction)
    {
      if (!recursiveCall)
        return function.computeFunction (this, parametersValue);
      else
        throw new IllegalArgumentException ("Can't evaluate recursive function " + function.getName ());
    }
    else
      try
      {
        // The Java written functions may be able to use the MathMLInterpreter
        return function.computeFunction (this, parametersValue);
      }
      catch (IllegalArgumentException e)
      {
        throw new IllegalArgumentException ("Can't evaluate function " + function.getName ());
      }
  }
}