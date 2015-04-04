/*
 * @(#)WrapperInterpreter.java   05/02/99
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

/**
 * Runtime interpreter operating on instances of wrapping classes. This class overrides the
 * implementation of its <code>DoubleInterpreter</code> super class to
 * return values, instances of the classes <code>Double</code>, <code>Long</code>, <code>Boolean</code>,
 * <code>String</code> or <code>Character</code> according to the operated literal, operator or function.
 * The type required for parameters of the methods of this interpreter depends on
 * the computed operation. Most of them compute numbers and thus require their parameters
 * value to be instances of <code>Number</code> (<code>Double</code>, <code>Integer</code>,...).
 * As no type checking is done during the parsing of functions and expressions, this interpreter
 * suits particularly to fill this lack and compute expressions supposed to respect the Java syntax :
 * <UL><LI>The value of the literal returned by <code>getLiteralValue ()</code> is an instance of <code>Boolean</code>
 *         for the keys <code>Syntax.CONSTANT_FALSE</code> and <code>Syntax.CONSTANT_TRUE</code>.</LI>
 *     <LI>The <code>isTrue ()</code> method used for conditions accepts only a <code>Boolean</code> parameter.</LI>
 *     <LI>Operators performing comparison return a value of type <code>Boolean</code>.</LI>
 *     <LI>Logical operators accepts only <code>Boolean</code> operands.</LI>
 *     <LI>Operators computing numbers (keys <code>OPERATOR_ADD</code>, <code>OPERATOR_SUBSTRACT</code>,
 *         <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>, <code>OPERATOR_POWER</code>,
 *         <code>OPERATOR_MODULO</code> and <code>OPERATOR_REMAINDER</code>) returns
 *         an instance of <code>Long</code> only if both operators are integers (instances
 *         of <code>Integer</code> or <code>Long</code>), otherwise the returned value is
 *         an instance of <code>Double</code>.
 *     <LI>Operators performing bit comparison (keys <code>OPERATOR_BITWISE_...</code>)
 *         accepts only <code>Integer</code>, <code>Long</code> or <code>Boolean</code> operands.</LI>
 *     <LI>Operators performing bit shift (keys <code>OPERATOR_SHIFT_...</code>)
 *         accepts only <code>Integer</code> or <code>Long</code> operands.</LI>
 *     <LI><code>getLiteralValue ()</code> accepts to return instances of <code>String</code>
 *         or <code>Character</code>.</code>
 *     <LI>If a <code>WrapperInterpreter</code> interpreter is created with its parameter
 *         <code>concatenationSupported</code> equal to <code>true</code>, the binary operator of the
 *         addition performs string concatenation if one of its operands is an instance of <code>String</code>.</LI>
 *     <LI>If a <code>WrapperInterpreter</code> interpreter is created with its parameter
 *         <code>integerDivisionSupported</code> equal to <code>true</code>, the binary operator of the
 *         division returns an integer if the type of its operand are integers with possible loss of precision.</LI>
 *     <LI>If a <code>WrapperInterpreter</code> interpreter is created with its parameter
 *         <code>characterOperationSupported</code> equal to <code>true</code>, the unary and
 *         binary operators and the common functions (excepted the logical ones) accepts a character
 *         (instance of <code>Character</code>) as an integer parameter.</LI></UL>
 * These features allow to compute the following functions :
 * <BLOCKQUOTE><PRE> // Create a parser using a Java syntax that allows boolean literals and strings
 * FunctionParser parser = new FunctionParser (new JavaSyntax (true));
 *
 * // Compile different types of functions
 * Function function1 = parser.compileFunction("f(x) = \"Value of x = \" + x");
 * Function function2 = parser.compileFunction("fact (x) = x <= 0 ? 1 : x * fact (x - 1)");
 * Function function3 = parser.compileFunction("multPow2 (x,power2) = x << power2");
 *
 * // Compute these functions
 * Interpreter interpreter = new WrapperInterpreter ();
 * System.out.println (function1.computeFunction (interpreter, new Object [] {new Integer (2001)}));
 * System.out.println (function2.computeFunction (interpreter, new Object [] {new Double (20)}));
 * System.out.println (function2.computeFunction (interpreter, new Object [] {new Long (5)}));
 * System.out.println (function3.computeFunction (interpreter, new Object [] {new Integer (51), new Integer (3)}));</PRE></BLOCKQUOTE>
 * The methods of this class are thread safe.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.JavaSyntax
 * @see     com.eteks.parser.Syntax
 */
public class WrapperInterpreter extends DoubleInterpreter
{
  private boolean concatenationSupported;
  private boolean integerDivisionSupported;
  private boolean characterOperationSupported;

  /**
   * Creates an interpreter operating on instances of wrapping classes.
   * By default concatenation, division on integers and operations on characters are supported.
   */
  public WrapperInterpreter ()
  {
    this (true, true, true);
  }

  /**
   * Creates an interpreter operating on instances of wrapping classes. By default division on integers
   * and operations on characters are supported.
   * @param concatenationSupported if equal to <code>true</code>, the binary operator of the
   *        addition performs string concatenation if one of its operands is an instance of <code>String</code>.
   */
  public WrapperInterpreter (boolean concatenationSupported)
  {
    this (concatenationSupported, true, true);
  }

  /**
   * Creates an interpreter operating on instances of wrapping classes. By default
   * operations on characters are supported.
   * @param concatenationSupported if equal to <code>true</code>, the binary operator of the
   *        addition performs string concatenation if one of its operands is an instance of <code>String</code>.
   * @param integerDivisionSupported if equal to <code>true</code>, the binary operator of the
   *        division returns an integer if the type of its operand are integers (meaning 3 / 2
   *        will return 1).
   */
  public WrapperInterpreter (boolean concatenationSupported,
                             boolean integerDivisionSupported)
  {
    this (concatenationSupported, true, true);
  }

  /**
   * Creates an interpreter operating on instances of wrapping classes.
   * @param concatenationSupported if equal to <code>true</code>, the binary operator of the
   *        addition performs string concatenation if one of its operands is an instance of <code>String</code>.
   * @param integerDivisionSupported    if equal to <code>true</code>, the binary operator of the
   *        division returns an integer if the type of its operand are integers (meaning 3 / 2
   *        will return 1).
   * @param characterOperationSupported if equal to <code>true</code>, the unary and
 *          binary operators and the common functions (excepted the logical ones) accepts
 *          a character (instance of the class <code>Character</code>) as an integer parameter.
   */
  public WrapperInterpreter (boolean concatenationSupported,
                             boolean integerDivisionSupported,
                             boolean characterOperationSupported)
  {
    this.concatenationSupported      = concatenationSupported;
    this.integerDivisionSupported    = integerDivisionSupported;
    this.characterOperationSupported = characterOperationSupported;
  }

  /**
   * Returns the value of the literal <code>literal</code>. <code>literal</code>
   * may be an instance of <code>Number</code>, <code>String</code> or <code>Character</code>.
   * @param  literal an instance of <code>Number</code>, <code>String</code> or <code>Character</code>.
   * @return the value of the literal. The returned value is the object itself.
   * @throws IllegalArgumentException if <code>literal</code> isn't an instance of <code>Number</code>,
   *         <code>String</code> or <code>Character</code>.
   */
  public Object getLiteralValue (Object literal)
  {
    if (   literal instanceof Number
        || literal instanceof String
        || literal instanceof Character)
      return literal;
    else
      // true and false are constants not literals
      throw new IllegalArgumentException ("Literal " + literal + " not an instance of Number, String or Character");
  }

  /**
   * Returns the value of the parameter <code>parameter</code>. <code>parameter</code>
   * may be an instance of <code>Number</code>, <code>String</code>, <code>Character</code>
   * or <code>Boolean</code>.
   * @param  parameter an instance of <code>Number</code>, <code>String</code>, <code>Character</code>
   *         or <code>Boolean</code>.
   * @return the value of the parameter. The returned value is the object itself.
   * @throws IllegalArgumentException if <code>parameter</code> isn't an instance of <code>Number</code>,
   *         <code>String</code>, <code>Character</code> or <code>Boolean</code>.
   */
  public Object getParameterValue (Object parameter)
  {
    if (   parameter instanceof Number
        || parameter instanceof String
        || parameter instanceof Character
        || parameter instanceof Boolean)
      return parameter;
    else
      throw new IllegalArgumentException ("Parameter " + parameter + " not an instance of Number, String, Character or Boolean");
  }

  /**
   * Returns the value of the constant <code>constantKey</code>. <code>constantKey</code>
   * may be the key of a constant of <code>Syntax</code> (one of <code>CONSTANT_PI</code>,
   * <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code>).
   * @param constantKey the key of a constant of <code>Syntax</code>.
   * @return the value of the constant.
   * @throws IllegalArgumentException if <code>constantKey</code> isn't a key of a constant of
   *         <code>Syntax</code>.
   */
  public Object getConstantValue (Object constantKey)
  {
    if (Syntax.CONSTANT_TRUE.equals (constantKey))
      return Boolean.TRUE;
    else if (Syntax.CONSTANT_FALSE.equals (constantKey))
      return Boolean.FALSE;
    else
      return super.getConstantValue (constantKey);
  }

  /**
   * Returns the value of the operation of the unary operator <code>unaryOperatorKey</code> applied
   * on the operand <code>operand</code>. <code>unaryOperatorKey</code> must be the key
   * of an unary operator of <code>Syntax</code> (one of <code>OPERATOR_POSITIVE</code>,
   * <code>OPERATOR_OPPOSITE</code>, <code>OPERATOR_LOGICAL_NOT</code>, <code>OPERATOR_BITWISE_NOT</code>).
   * @param unaryOperatorKey the key of an unary operator of <code>Syntax</code>.
   * @param operand          the operand.
   * @return the result of the operation. The returned value is an instance of <code>Double</code>,
   *         <code>Long</code> or <code>Boolean</code>.
   * @throws IllegalArgumentException if <code>operand</code> isn't of the good type for the requested
   *         operator or if <code>unaryOperatorKey</code> isn't the key of an unary
   *         operator of <code>Syntax</code>.
   */
  public Object getUnaryOperatorValue (Object unaryOperatorKey, Object operand)
  {
    if (unaryOperatorKey.equals (Syntax.OPERATOR_OPPOSITE))
    {
      if (   operand instanceof Integer
          || operand instanceof Long)
        return new Long (-((Number)operand).longValue ());
      else if (   operand instanceof Float
               || operand instanceof Number)
        return new Double (-((Number)operand).doubleValue ());
      else if (   characterOperationSupported
               && operand instanceof Character)
        return new Long (-((Character)operand).charValue ());
      else
        throw new IllegalArgumentException ("Operand " + operand + " not an instance of Number" + (characterOperationSupported ? " or Character" : ""));
    }
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_POSITIVE))
    {
      if (operand instanceof Number)
        return operand;
      else if (   characterOperationSupported
               && operand instanceof Character)
        return new Long (((Character)operand).charValue ());
      else
        throw new IllegalArgumentException ("Operand " + operand + " not an instance of Number" + (characterOperationSupported ? " or Character" : ""));
    }
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_NOT))
    {
      if (!(operand instanceof Boolean))
        throw new IllegalArgumentException ("Operand " + operand + " not an instance of Boolean");
      return ((Boolean)operand).booleanValue () ? Boolean.FALSE : Boolean.TRUE;
    }
    else if (unaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_NOT))
    {
      if (   operand instanceof Integer
          || operand instanceof Long)
        return new Long (~((Number)operand).longValue ());
      else if (   characterOperationSupported
               && operand instanceof Character)
        return new Long (~((Character)operand).charValue ());
      else
        throw new IllegalArgumentException ("Operand" + operand + " not an instance of Integer or Long");
    }
    else
      throw new IllegalArgumentException ("Unary operator key " + unaryOperatorKey + " not implemented");
  }

  /**
   * Returns the value of the operation of the binary operator <code>binaryOperatorKey</code> applied on
   * the two operands <code>operand1</code> and <code>operand2</code>. <code>binaryOperatorKey</code>
   * must be the key of a binary operator of <code>Syntax</code> (one of <code>OPERATOR_ADD</code>,
   * <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>,...).
   * @param binaryOperatorKey the key of a binary operator of <code>Syntax</code>.
   * @param operand1          the first operand.
   * @param operand2          the second operand.
   * @return the result of the operation. The returned value is an instance of <code>Double</code>,
   *         <code>Long</code>, <code>Boolean</code> or <code>String</code>.
   * @throws IllegalArgumentException if <code>operand1</code> and <code>operand2</code> aren't of
   *         the good type for requested operator or if <code>binaryOperatorKey</code> isn't the key
   *         of a binary operator of <code>Syntax</code>.
   */
  public Object getBinaryOperatorValue (Object binaryOperatorKey, Object operand1, Object operand2)
  {
    if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_OR))
      return isTrue (operand1) || isTrue (operand2)
                ? Boolean.TRUE : Boolean.FALSE;
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_XOR))
    {
      boolean cond1 = isTrue (operand1);
      boolean cond2 = isTrue (operand2);
      return    !cond1 &&  cond2
             ||  cond1 && !cond2
               ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LOGICAL_AND))
      return isTrue (operand1) && isTrue (operand2)
                ? Boolean.TRUE : Boolean.FALSE;
    else if (   binaryOperatorKey.equals (Syntax.OPERATOR_ADD)
             && concatenationSupported)
      if (operand1 instanceof String)
        return (String)operand1 + operand2;
      else if (operand2 instanceof String)
        return operand1 + (String)operand2;

    // If operations on characters are supported, change the type of characters to integers
    if (characterOperationSupported)
    {
      if (operand1 instanceof Character)
        operand1 = new Integer (((Character)operand1).charValue ());
      if (operand2 instanceof Character)
        operand2 = new Integer (((Character)operand2).charValue ());
    }

    if (binaryOperatorKey.equals (Syntax.OPERATOR_ADD))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
        return new Long (((Number)operand1).longValue () + ((Number)operand2).longValue ());
      else
        return new Double (((Number)operand1).doubleValue () + ((Number)operand2).doubleValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SUBSTRACT))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
        return new Long (((Number)operand1).longValue () - ((Number)operand2).longValue ());
      else
        return new Double (((Number)operand1).doubleValue () - ((Number)operand2).doubleValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MULTIPLY))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
        return new Long (((Number)operand1).longValue () * ((Number)operand2).longValue ());
      else
        return new Double (((Number)operand1).doubleValue () * ((Number)operand2).doubleValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_DIVIDE))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (   Long.class.equals (getResultClass (operand1, operand2))
          && integerDivisionSupported)
        return new Long (((Number)operand1).longValue () / ((Number)operand2).longValue ());
      else
        return new Double (((Number)operand1).doubleValue () / ((Number)operand2).doubleValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_POWER))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
        return new Long ((long)Math.pow (((Number)operand1).longValue (), ((Number)operand2).longValue ()));
      else
        return new Double (Math.pow (((Number)operand1).doubleValue (), ((Number)operand2).doubleValue ()));
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_MODULO))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
      {
        long number1 = ((Number)operand1).longValue ();
        long number2 = ((Number)operand2).longValue ();
        long modulo = number1 - number2 * (number1 / number2);
        // If dividend and divisor are not of the same sign, add divisor
        if (   number1 < 0 && number2 > 0
            || number1 > 0 && number2 < 0)
          modulo += number2;
        return new Long (modulo);
      }
      else
      {
        double number1 = ((Number)operand1).doubleValue ();
        double number2 = ((Number)operand2).doubleValue ();
        double modulo = number1 - number2 * (long)(number1 / number2);
        // If dividend and divisor are not of the same sign, add divisor
        if (   number1 < 0 && number2 > 0
            || number1 > 0 && number2 < 0)
          modulo += number2;
        return new Double (modulo);
      }
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_REMAINDER))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      if (Long.class.equals (getResultClass (operand1, operand2)))
        return new Long (((Number)operand1).longValue () % ((Number)operand2).longValue ());
      else
        return new Double (((Number)operand1).doubleValue () % ((Number)operand2).doubleValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_EQUAL))
    {
      checkOperandsInstanceOfNumberOrBoolean (operand1, operand2);
      if (operand1 instanceof Number)
        return ((Number)operand1).doubleValue () == ((Number)operand2).doubleValue ()
                  ? Boolean.TRUE : Boolean.FALSE;
      else // operand1 and operand2 instanceof Boolean
        return operand1.equals (operand2)
                 ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_DIFFERENT))
    {
      checkOperandsInstanceOfNumberOrBoolean (operand1, operand2);
      if (operand1 instanceof Number)
        return ((Number)operand1).doubleValue () == ((Number)operand2).doubleValue ()
                  ? Boolean.FALSE : Boolean.TRUE;
      else // operand1 and operand2 instanceof Boolean
        return operand1.equals (operand2)
                 ? Boolean.FALSE : Boolean.TRUE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_OR))
    {
      checkOperandsInstanceOfIntegerLongOrBoolean (operand1, operand2);
      if (operand1 instanceof Number)
        return new Long (((Number)operand1).longValue () | ((Number)operand2).longValue ());
      else // operand1 and operand2 instanceof Boolean
        return ((Boolean)operand1).booleanValue () | ((Boolean)operand2).booleanValue ()
                  ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_XOR))
    {
      checkOperandsInstanceOfIntegerLongOrBoolean (operand1, operand2);
      if (operand1 instanceof Number)
        return new Long (((Number)operand1).longValue () ^ ((Number)operand2).longValue ());
      else // operand1 and operand2 instanceof Boolean
        return ((Boolean)operand1).booleanValue () ^ ((Boolean)operand2).booleanValue ()
                  ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_BITWISE_AND))
    {
      checkOperandsInstanceOfIntegerLongOrBoolean (operand1, operand2);
      if (operand1 instanceof Number)
        return new Long (((Number)operand1).longValue () & ((Number)operand2).longValue ());
      else // operand1 and operand2 instanceof Boolean
        return ((Boolean)operand1).booleanValue () & ((Boolean)operand2).booleanValue ()
                  ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_LEFT))
    {
      checkOperandsInstanceOfIntegerOrLong (operand1, operand2);
      return new Long (((Number)operand1).longValue () << ((Number)operand2).longValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT))
    {
      checkOperandsInstanceOfIntegerOrLong (operand1, operand2);
      return new Long (((Number)operand1).longValue () >> ((Number)operand2).longValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_SHIFT_RIGHT_0))
    {
      checkOperandsInstanceOfIntegerOrLong (operand1, operand2);
      return new Long (((Number)operand1).longValue () >>> ((Number)operand2).longValue ());
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_GREATER_OR_EQUAL))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      return ((Number)operand1).doubleValue () >= ((Number)operand2).doubleValue ()
               ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LESS_OR_EQUAL))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      return ((Number)operand1).doubleValue () <= ((Number)operand2).doubleValue ()
                ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_GREATER))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      return ((Number)operand1).doubleValue () > ((Number)operand2).doubleValue ()
                ? Boolean.TRUE : Boolean.FALSE;
    }
    else if (binaryOperatorKey.equals (Syntax.OPERATOR_LESS))
    {
      checkOperandsInstanceOfNumber (operand1, operand2);
      return ((Number)operand1).doubleValue () < ((Number)operand2).doubleValue ()
                 ? Boolean.TRUE : Boolean.FALSE;
    }
    else
      throw new IllegalArgumentException ("Binary operator key " + binaryOperatorKey + " not implemented");
  }

  private void checkOperandsInstanceOfNumberOrBoolean (Object operand1, Object operand2)
  {
    if (   !(operand1 instanceof Number)
        && !(operand1 instanceof Boolean))
      throw new IllegalArgumentException ("Operand " + operand1 + " not an instance of Number" + (characterOperationSupported ? ", Character" : "") + " or Boolean");
    if (   !(operand2 instanceof Number)
        && !(operand2 instanceof Boolean))
      throw new IllegalArgumentException ("Operand " + operand2 + " not an instance of Number" + (characterOperationSupported ? ", Character" : "") + " or Boolean");
    if (!(   operand1 instanceof Number && operand2 instanceof Number
          || operand1 instanceof Boolean && operand2 instanceof Boolean))
      throw new IllegalArgumentException ("Operands " + operand1 + " and " + operand2 + " must be of compatible type");
  }

  private void checkOperandsInstanceOfIntegerLongOrBoolean (Object operand1, Object operand2)
  {
    if (   !(operand1 instanceof Integer)
        && !(operand1 instanceof Long)
        && !(operand1 instanceof Boolean))
      throw new IllegalArgumentException ("Operand " + operand1 + " not an instance of Integer, Long" + (characterOperationSupported ? ", Character" : "") + " or Boolean");
    if (   !(operand1 instanceof Integer)
        && !(operand1 instanceof Long)
        && !(operand2 instanceof Boolean))
      throw new IllegalArgumentException ("Operand " + operand2 + " not an instance of Integer, Long" + (characterOperationSupported ? ", Character" : "") + " or Boolean");
    if (!(   operand1 instanceof Number && operand2 instanceof Number
          || operand1 instanceof Boolean && operand2 instanceof Boolean))
      throw new IllegalArgumentException ("Operands " + operand1 + " and " + operand2 + " must be of compatible type");
  }

  private void checkOperandsInstanceOfIntegerOrLong (Object operand1, Object operand2)
  {
    if (   !(operand1 instanceof Integer)
        && !(operand1 instanceof Long))
      throw new IllegalArgumentException ("Operand " + operand1 + " not an instance of Integer" + (characterOperationSupported ? ", Character" : "") + " or Long");
    if (   !(operand2 instanceof Integer)
        && !(operand2 instanceof Long))
      throw new IllegalArgumentException ("Operand " + operand2 + " not an instance of Integer" + (characterOperationSupported ? ", Character" : "") + " or Long");
  }

  private void checkOperandsInstanceOfNumber (Object operand1, Object operand2)
  {
    if (!(operand1 instanceof Number))
      throw new IllegalArgumentException ("Operand " + operand1 + " not an instance of Number");
    if (!(operand2 instanceof Number))
      throw new IllegalArgumentException ("Operand " + operand2 + " not an instance of Number");
  }

  private Class getResultClass (Object operand1, Object operand2)
  {
    if (   (   operand1 instanceof Integer
            || operand1 instanceof Long)
        && (   operand2 instanceof Integer
            || operand2 instanceof Long))
      return Long.class;
    else
      return Double.class;
  }

  /**
   * Returns the value of the common function <code>commonFunctionKey</code> with
   * the parameter <code>param</code>. <code>commonFunctionKey</code> must be the key
   * of a commomon function of <code>Syntax</code> (one of <code>FUNCTION_LN</code>,
   * <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,...).
   * @param commonFunctionKey the key of a common function of <code>Syntax</code>.
   * @param param             the parameter of the function.
   * @return the result of the function. The returned value is an instance of <code>Double</code>,
   *         <code>Long</code> or <code>Boolean</code>.
   * @throws IllegalArgumentException if <code>param</code> isn't of the good type for the
   *         requested function or if <code>commonFunctionKey</code> isn't the key of a
   *         commomon function of <code>Syntax</code>.
   */
  public Object getCommonFunctionValue (Object commonFunctionKey, Object param)
  {
    if (commonFunctionKey.equals (Syntax.FUNCTION_NOT))
      return isTrue (param) ? Boolean.FALSE : Boolean.TRUE;

    // If operations on characters are supported, change the type of characters to integers
    if (characterOperationSupported)
      if (param instanceof Character)
        param = new Integer (((Character)param).charValue ());

    if (commonFunctionKey.equals (Syntax.FUNCTION_INTEGER))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (param instanceof Long)
        return param;
      else
        return new Long (((Number)param).longValue ());
    }
    else if (commonFunctionKey.equals (Syntax.FUNCTION_FLOOR))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (param instanceof Long)
        return param;
      else if (param instanceof Integer)
        return new Long (((Integer)param).longValue ());
      else
        return new Double (Math.floor (((Number)param).doubleValue ()));
    }
    else if (commonFunctionKey.equals (Syntax.FUNCTION_CEIL))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (param instanceof Long)
        return param;
      else if (param instanceof Integer)
        return new Long (((Integer)param).longValue ());
      else
        return new Double (Math.ceil (((Number)param).doubleValue ()));
    }
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ROUND))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (param instanceof Long)
        return param;
      else if (param instanceof Integer)
        return new Long (((Integer)param).longValue ());
      else
        return new Double (Math.rint (((Number)param).doubleValue ()));
    }
    else if (commonFunctionKey.equals (Syntax.FUNCTION_ABS))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (   param instanceof Long
          || param instanceof Integer)
        return new Long (Math.abs (((Number)param).longValue ()));
      else
        return new Double (Math.abs (((Number)param).doubleValue ()));
    }
    else if (commonFunctionKey.equals (Syntax.FUNCTION_OPPOSITE))
    {
      if (!(param instanceof Number))
        throw new IllegalArgumentException ("Parameter " + param + " not an instance of Number");
      if (   param instanceof Long
          || param instanceof Integer)
        return new Long (-((Number)param).longValue ());
      else
        return new Double (-((Number)param).doubleValue ());
    }
    else
      return super.getCommonFunctionValue (commonFunctionKey, param);
  }

  /**
   * Returns <code>true</code> or <code>false</code> according to the value of <code>condition</code>.
   * @param condition the value to test (instance of <code>Boolean</code>).
   * @return the boolean value of <code>condition</code>.
   */
  public boolean isTrue (Object condition)
  {
    if (condition instanceof Boolean)
      return ((Boolean)condition).booleanValue ();
    else
      throw new IllegalArgumentException ("Parameter " + condition + " not an instance of Boolean");
  }
}
