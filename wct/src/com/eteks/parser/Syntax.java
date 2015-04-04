/*
 * @(#)Syntax.java   01/01/98
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
 * Syntax used by parsers. This interface specifies all the keys and
 * various methods used by a parser to check the syntax of parsed strings.<br>
 * <code>getConstantKey ()</code>, <code>getUnaryOperatorKey ()</code>,
 * <code>getBinaryOperatorKey ()</code>, <code>getConditionPartKey ()</code>,
 * <code>getCommonFunctionKey ()</code> methods are called by the parser to check
 * if a string extracted from a parsed expression is a valid lexical element or
 * not for the syntax.
 * These methods returns one of the <code>Integer</code> constants matching keys
 * described in this interface if the extracted string is respectively a constant,
 * an unary opertor, a binary operator, a condition part or a common function of the syntax.
 * Examples :
 * <UL><LI>If the string "*" is the multiplication binary operator of the implemented syntax
 * the <code>getBinaryOperatorKey ()</code> method returns <code>OPERATOR_MULTIPLY</code>.</LI>
 *     <LI>If the string "Math.sin" is the sine common function of the implemented syntax
 * the <code>getCommonFunctionKey ()</code> method returns <code>FUNCTION_SIN</code>.</LI></UL>
 * The <code>getBinaryOperatorPriority ()</code> method returns a positive number telling
 * the priority of each binary operator keys supported by the syntax. All the binary operators
 * with the same priority are left-to-right associative. All the unary operators and function
 * calls have the highest priority and are right-to-left associative.
 * The condition operator has the lowest priority.<br>
 * The <code>getLiteral ()</code> method extracts the string of a literal from parsed strings
 * and returns the value of the literal valid for the syntax.<br>
 * The other <code>get... ()</code> following methods are called by the parser to get
 * the other elements that describes a syntax :
 * <UL><LI><code>getWhiteSpaceCharacters ()</code> : returns a string that contains all the allowed white space
 *         delimiters (tab, spaces,...).</LI>
 *     <LI><code>getOpeningBracket ()</code> : returns the char used as an opening bracket.</LI>
 *     <LI><code>getClosingBracket ()</code> : returns the char used as a closing bracket.</LI>
 *     <LI><code>getParameterSeparator ()</code> : returns the char used to separate parameters in a function call.</LI>
 *     <LI><code>getDelimiters ()</code> : returns all the chars of the syntax
 *         (operators and above characters) that may be used as delimiters between literals, constants and identifiers.</LI>
 *     <LI><code>getAssignmentOperator ()</code> : returns a string used as the assignment
 *         operator by the parsers <code>FunctionParser</code> and <code>ExpressionParser</code>.</LI>
 *     <LI><code>isCaseSensitive ()</code> : returns <code>true</code> if identifiers, operators and functions
 *         of the syntax are case sensitive.</LI>
 *     <LI><code>isShortSyntax ()</code> : returns <code>true</code> if the syntax supports short cuts in expressions
 *         (no need to put between brackets common functions parameters between brackets).</LI>
 *     <LI><code>isValidIdentifier ()</code> : returns <code>true</code> if the string parameter
 *         can be considered as a user function paramater or an expression parameter.</LI></UL>
 * Finally, the <code>getFunction ()</code> method is called by the parser to check if an extracted string
 * is a user function, that may be a previous parsed function or a Java written function implementing the <code>Function</code>
 * interface. This enables to chain calls with other functions.<br>
 * Note that these methods simply returns 0 or null if a string or an element isn't
 * accepted by the syntax. You can use the <code>AbstractSyntax</code> class to help you implement
 * this interface. The classes <code>PascalSyntax</code> or <code>JavaSyntax</code> classes
 * can be used to parse PASCAL or Java expressions and functions.<br>
 * The classes implementing the <code>Interpreter</code> interface implements how to compute
 * all the litterals, operators or functions of the syntax.<br>
 * You can add a new litteral, operator or function in your syntax if you respect
 * the following steps :
 * <UL><LI>Create a new key that matches the new syntactic element. This key may be of class
 *         <code>Integer</code> or any other type as long as its unique in its category
 *         (constant, unary operator, binary operator or common function).
 *         If you create an <code>Integer</code> key, use a number greater or equal to
 *         <code>USER_STARTING_KEY</code> to avoid any problem.</LI>
 *     <LI>Return this key for the matching extracted string in the <code>get...Key ()</code> method
 *         of <code>Syntax</code> that accepts it as an lexical element of the syntax.
 *         If your syntax is a subclass of <code>AbstractSyntax</code>, you may also use the
 *         <code>set...Key ()</code> method to add directly the new key and its matching string
 *         to the syntax.</LI>
 *     <LI>Implement the computation of the new syntactic element in the equivalent
 *         <code>get...Value ()</code> method of <code>Interpreter</code> for the new key.</LI></UL>
 * For example, imagine you want to add a common function "INV" in the syntax that computes
 * the inverse of a number (1 / x). One solution could be :
 * <UL><LI>Add to one of your classes the constant key <code>FUNCTION_INVERSE</code> equal to
 *         <code>new Integer (USER_STARTING_KEY)</code>.</LI>
 *     <LI>Return the key <code>FUNCTION_INVERSE</code> in your implementation of the
 *         <code>getCommonFunctionKey ()</code> method if its string parameter is equal to "INV".
 *         If your syntax is a subclass of <code>AbstractSyntax</code>, you can also call
 *         <code>setCommonFunctionKey ("INV", FUNCTION_INVERSE)</code> on the instance
 *         of <code>Syntax</code>.</LI>
 *     <LI>Implement the computation of the key <code>FUNCTION_INVERSE</code> in the
 *         <code>getCommonFunctionValue ()</code> method of <code>Interpreter</code>.</LI></UL>
 * You should notice that the key is only used by the parser and the interpreter to
 * link the recognized element by the syntax at parsing time to its computation code
 * at interpretation time. This allows to interpret the same parsed expression or function with
 * different implementations of <code>Interpreter</code>.<br>
 * For example, the <code>com.eteks.tools.calculator.JeksCalculator</code> class implements the
 * <code>Syntax</code> and <code>Interpreter</code> interfaces in a way it uses uppercase strings for all its keys.<br>
 * Caution : The operators or functions syntax doesn't specify the type of the operands or
 * the parameters. The type is checked at runtime by the class that implements the
 * <code>Interpreter</code> interface.<br>
 * It's possible also to add a new function with any number of parameters to the syntax,
 * if this function implements the <code>Function</code> interface and if it's returned by
 * the <code>getFunction ()</code> method.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.AbstractSyntax
 * @see       com.eteks.parser.Interpreter
 * @see       com.eteks.parser.Function
 * @see       com.eteks.tools.calculator.JeksCalculator
 */
public interface Syntax
{
  // Constant keys available for this syntax
  /**
   * Key returned by <code>getConstantKey ()</code> for the constant  PI.
   */
  public final static Integer CONSTANT_PI = new Integer (0);
  /**
   * Key returned by <code>getConstantKey ()</code> for the constant number E (exp (1)).
   */
  public final static Integer CONSTANT_E = new Integer (1);
  /**
   * Key returned by <code>getConstantKey ()</code> for the constant false.
   */
  public final static Integer CONSTANT_FALSE = new Integer (2);
  /**
   * Key returned by <code>getConstantKey ()</code> for the constant true.
   */
  public final static Integer CONSTANT_TRUE = new Integer (3);


  // Unary operators keys available for this syntax
  /**
   * Key returned by <code>getUnaryOperatorKey ()</code> for the positive operator.
   */
  public final static Integer OPERATOR_POSITIVE = new Integer (1000);
  /**
   * Key returned by <code>getUnaryOperatorKey ()</code> for the opposite operator.
   */
  public final static Integer OPERATOR_OPPOSITE = new Integer (1001);
  /**
   * Key returned by <code>getUnaryOperatorKey ()</code> for the logical not operator.
   */
  public final static Integer OPERATOR_LOGICAL_NOT = new Integer (1002);
  /**
   * Key returned by <code>getUnaryOperatorKey ()</code> for the bitwise not operator.
   */
  public final static Integer OPERATOR_BITWISE_NOT = new Integer (1003);


  // Binary operators keys available for this syntax
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the add operator.
   */
  public final static Integer OPERATOR_ADD = new Integer (2000);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the substract operator.
   */
  public final static Integer OPERATOR_SUBSTRACT = new Integer (2001);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the multiply operator.
   */
  public final static Integer OPERATOR_MULTIPLY = new Integer (2002);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the divide operator.
   */
  public final static Integer OPERATOR_DIVIDE = new Integer (2003);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the power operator.
   */
  public final static Integer OPERATOR_POWER = new Integer (2004);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the modulo operator.
   */
  public final static Integer OPERATOR_MODULO = new Integer (2005);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the remainder operator.
   */
  public final static Integer OPERATOR_REMAINDER = new Integer (2006);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the equal operator.
   */
  public final static Integer OPERATOR_EQUAL = new Integer (2007);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the different operator.
   */
  public final static Integer OPERATOR_DIFFERENT = new Integer (2008);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the greater or equal operator.
   */
  public final static Integer OPERATOR_GREATER_OR_EQUAL = new Integer (2009);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the less or equal operator.
   */
  public final static Integer OPERATOR_LESS_OR_EQUAL = new Integer (2010);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the greater operator.
   */
  public final static Integer OPERATOR_GREATER = new Integer (2011);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the less operator.
   */
  public final static Integer OPERATOR_LESS = new Integer (2012);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the logical or operator.
   */
  public final static Integer OPERATOR_LOGICAL_OR = new Integer (2013);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the logical and operator.
   */
  public final static Integer OPERATOR_LOGICAL_AND = new Integer (2014);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the logical xor operator.
   */
  public final static Integer OPERATOR_LOGICAL_XOR = new Integer (2015);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the bitwise or operator.
   */
  public final static Integer OPERATOR_BITWISE_OR = new Integer (2016);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the bitwise xor operator.
   */
  public final static Integer OPERATOR_BITWISE_XOR = new Integer (2017);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the bitwise and operator.
   */
  public final static Integer OPERATOR_BITWISE_AND = new Integer (2018);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the shift left operator.
   */
  public final static Integer OPERATOR_SHIFT_LEFT = new Integer (2019);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the shift right operator.
   */
  public final static Integer OPERATOR_SHIFT_RIGHT = new Integer (2020);
  /**
   * Key returned by <code>getBinaryOperatorKey ()</code> for the shift right operator (Java operator >>>).
   */
  public final static Integer OPERATOR_SHIFT_RIGHT_0 = new Integer (2021);


  // Conditional keys available for this syntax
  /**
   * Key returned by <code>getConditionPartKey ()</code> for the if part of a condition. If the
   * syntax uses a condition with only two parts for the then and
   * else parts of the condition (like the operator ? : in Java), the <code>getConditionPartCount ()</code>
   * method returns 2.
   */
  public final static Integer CONDITION_IF = new Integer (4000);
  /**
   * Key returned by <code>getConditionPartKey ()</code> for the then part of a condition.
   */
  public final static Integer CONDITION_THEN = new Integer (4001);
  /**
   * Key returned by <code>getConditionPartKey ()</code> for the else part of a condition.
   */
  public final static Integer CONDITION_ELSE = new Integer (4002);


  // Predefined function keys available for this syntax
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the nepierian logarithm function.
   */
  public final static Integer FUNCTION_LN = new Integer (3000);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the decimal logarithm function.
   */
  public final static Integer FUNCTION_LOG = new Integer (3001);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the exponential function.
   */
  public final static Integer FUNCTION_EXP = new Integer (3002);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the square function.
   */
  public final static Integer FUNCTION_SQR = new Integer (3003);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the square root function.
   */
  public final static Integer FUNCTION_SQRT = new Integer (3004);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the cosine function.
   */
  public final static Integer FUNCTION_COS = new Integer (3005);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the sine function.
   */
  public final static Integer FUNCTION_SIN = new Integer (3006);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the tangent function.
   */
  public final static Integer FUNCTION_TAN = new Integer (3007);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the arc cosine function.
   */
  public final static Integer FUNCTION_ACOS = new Integer (3008);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the arc sine function.
   */
  public final static Integer FUNCTION_ASIN = new Integer (3009);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the arc tangent function.
   */
  public final static Integer FUNCTION_ATAN = new Integer (3010);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the hyperbolic sine function.
   */
  public final static Integer FUNCTION_COSH = new Integer (3011);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the hyperbolic sine function.
   */
  public final static Integer FUNCTION_SINH = new Integer (3012);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the hyperbolic tangent function.
   */
  public final static Integer FUNCTION_TANH = new Integer (3013);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the integer part function.
   */
  public final static Integer FUNCTION_INTEGER = new Integer (3014);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the floor value function.
   */
  public final static Integer FUNCTION_FLOOR = new Integer (3015);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the ceil value function.
   */
  public final static Integer FUNCTION_CEIL = new Integer (3016);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the round value function.
   */
  public final static Integer FUNCTION_ROUND = new Integer (3017);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the absolute value function.
   */
  public final static Integer FUNCTION_ABS = new Integer (3018);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the opposite function.
   */
  public final static Integer FUNCTION_OPPOSITE = new Integer (3019);
  /**
   * Key returned by <code>getCommonFunctionKey ()</code> for the not function.
   */
  public final static Integer FUNCTION_NOT = new Integer (3020);


  /**
   * Key starting value for user keys.
   */
  public final static int USER_STARTING_KEY = 1000000;

  /**
   * Returns the value of the literal parsed from the string <code>expression</code>
   * or <code>null</code> if <code>expression</code> doesn't start with a literal. If a
   * literal is found at the beginning of <code>expression</code>, this method extracts
   * the parsed literal in the string buffer <code>extractedLiteral</code>.
   * The extracted literal is any literal valid for the syntax, a number, a string or other kind
   * of literal value. The <code>getLiteralValue ()</code> method of <code>Interpreter</code>
   * must be implemented to return the value matching the value returned by this method that
   * the interpreter accepts to use.
   * @param  expression       the string to parse.
   * @param  extractedLiteral the literal extracted from <code>expression</code> identified
   *                          as a valid literal with the syntax of the parser. This string buffer
   *                          is emptied before the call of this method by the parser.
   * @return the value of the extracted literal or <code>null</code> if <code>expression</code>
   *         doesn't start with a literal valid for the syntax. If the literal is a number,
   *         an instance of <code>Number</code> should be returned, if the literal is a string
   *         an instance of <code>String</code> should be returned. Otherwise the returned value
   *         may be of any class, as long as the methods of the used interpreter at runtime is able
   *         to use them.
   */
  public Object getLiteral (String       expression,
                            StringBuffer extractedLiteral);

  /**
   * Returns the key matching <code>constant</code> (one of <code>CONSTANT_PI</code>,
   * <code>CONSTANT_E</code>, <code>CONSTANT_FALSE</code>, <code>CONSTANT_TRUE</code> or
   * an other user defined key). The <code>getLiteralValue ()</code> method of <code>Interpreter</code>
   * must be implemented to return a value for this key (for example <code>getLiteralValue ()</code>
   * can return the object <code>new Double (Math.PI)</code> for a key equal to <code>CONSTANT_PI</code>).<br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @param  constant the string to test. Numeric and string literals are not checked by this method.
   * @return the key matching the constant string or <code>null</code> if <code>constant</code>
   *         isn't a constant of the syntax.
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#setConstantKey
   */
  public Object getConstantKey (String constant);

  /**
   * Returns the key matching <code>unaryOperator</code> (one of <code>OPERATOR_POSITIVE</code>,
   * <code>OPERATOR_OPPOSITE</code>, <code>OPERATOR_LOGICAL_NOT</code>, <code>OPERATOR_BITWISE_NOT</code> or
   * an other user defined key). The <code>getUnaryOperatorValue ()</code> method of <code>Interpreter</code>
   * must be implemented to compute the operator of this key (for example <code>getUnaryOperatorValue ()</code>
   * can compute the opposite operation of its parameter for a key equal to <code>OPERATOR_OPPOSITE</code>).<br>
   * <code>getUnaryOperatorKey ()</code> and <code>getBinaryOperatorKey ()</code> may support
   * some synonymous operators (as the operators - or +). The parser manages to guess
   * whether a synonymous operator is an unary or a binary operator according to the context.
   * @param  unaryOperator the string to test.
   * @return the key matching the unary operator or <code>null</code> if <code>unaryOperator</code>
   *         isn't a unary operator the syntax.
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#setUnaryOperatorKey
   */
  public Object getUnaryOperatorKey (String unaryOperator);

  /**
   * Returns the key matching <code>binaryOperator</code> (one of <code>OPERATOR_ADD</code>,
   * <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>, <code>OPERATOR_DIVIDE</code>,... or
   * an other user defined key). The <code>getBinaryOperatorValue ()</code> method of <code>Interpreter</code>
   * must be implemented to compute the operator of this key (for example <code>getBinaryOperatorValue ()</code>
   * can compute the addition of its parameters for a key equal to <code>OPERATOR_ADD</code>).<br>
   * <code>getUnaryOperatorKey ()</code> and <code>getBinaryOperatorKey ()</code> may support
   * some synonymous operators (as the operators - or +). The parser manages to guess
   * whether a synonymous operator is an unary or a binary operator according to the context.
   * @param  binaryOperator the string to test.
   * @return the key matching the binary operator or <code>null</code> if <code>binaryOperator</code>
   *         isn't a binary operator of the syntax.
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#setBinaryOperatorKey
   */
  public Object getBinaryOperatorKey (String binaryOperator);

  /**
   * Returns the key matching <code>conditionPart</code> (one of <code>CONDITION_IF</code>,
   * <code>CONDITION_THEN</code>, <code>CONDITION_ELSE</code>). The <code>getConditionValue ()</code> method
   * of <code>Interpreter</code> must be implemented to return the good value if a condition is true
   * or false.<br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @param  conditionPart the string to test.
   * @return the key matching the conditional part or <code>null</code> if <code>conditionPart</code>
   *         isn't a conditional part of the syntax or if no condition is supported by the syntax.
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#setConditionPartKey
   */
  public Object getConditionPartKey (String conditionPart);

  /**
   * Returns 2 or 3 depending on whether the syntax using a condition with two or three parts (then else
   * parts or if then else parts. <br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @return 2 or 3.
   */
  public int getConditionPartCount ();

  /**
   * Returns the key matching <code>commonFunction</code> (one of <code>FUNCTION_LN</code>,
   * <code>FUNCTION_LOG</code>, <code>FUNCTION_EXP</code>, <code>FUNCTION_SQR</code>,... or
   * an other user defined key). The <code>getCommonFunctionValue ()</code> method of <code>Interpreter</code>
   * must be implemented to compute the function of this key (for example <code>getCommonFunctionValue ()</code>
   * can compute the nepierian logarithm of its parameter for a key equal to <code>FUNCTION_LN</code>).
   * Common functions have only one parameter and may be called with no brackets
   * around their parameter if <code>isShortSyntax ()</code> returns <code>true</code>.
   * @param  commonFunction the string to test.
   * @return the key matching the common function or <code>null</code> if <code>commonFunction</code>
   *         isn't a common function of the syntax.
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#setCommonFunctionKey
   */
  public Object getCommonFunctionKey (String commonFunction);

  /**
   * Returns a reference to the instance of <code>Function</code> whose name is <code>functionName</code>.
   * This enables to call in an other function a previously parsed function or a function
   * implementing the <code>Function</code> interface.
   * The <code>getFunctionValue ()</code> method of <code>Interpreter</code>
   * must be implemented to compute the returned function.<br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @param  functionName the string to test.
   * @return a <code>Function</code> reference or <code>null</code> if <code>functionName</code>
   *         isn't a function of the syntax.
   * @see    com.eteks.parser.Function
   * @see    com.eteks.parser.Interpreter
   * @see    com.eteks.parser.AbstractSyntax#addFunction
   */
  public Function getFunction (String functionName);

  /**
   * Returns the priority of the binary operator matching the key <code>binaryOperatorKey</code>. Two
   * operators may have the same priority. A greater value is returned for an operator with higher priority.
   * @param  binaryOperatorKey a binary operator key (one of <code>OPERATOR_ADD</code>,
   *            <code>OPERATOR_SUBSTRACT</code>, <code>OPERATOR_MULTIPLY</code>,
   *            <code>OPERATOR_DIVIDE</code>,... or an other user defined key).
   * @return a positive value equal to the priority of <code>binaryOperatorKey</code>.
   * @see    com.eteks.parser.AbstractSyntax#setBinaryOperatorPriority
   */
  public int getBinaryOperatorPriority (Object binaryOperatorKey);

  /**
   * Returns a string that contains all the delimiters allowed by the syntax as white spaces
   * (tab, spaces,...).
   * @return A string containing a set of white spaces or <code>null</code>.
   * @see    com.eteks.parser.AbstractSyntax#setWhiteSpaceCharacters
   */
  public String getWhiteSpaceCharacters ();

  /**
   * Returns the char used as the opening bracket of the syntax. Brackets are use to
   * brackets expressions and to pass parameters to a function.
   * @return the opening bracket char or 0 if no brackets are used.
   * @see    com.eteks.parser.AbstractSyntax#setOpeningBracket
   */
  public char getOpeningBracket ();

  /**
   * Returns the char used as the closing bracket of the syntax. Brackets are use to
   * brackets expressions and to pass parameters to a function.
   * @return the closing bracket char or 0 if no brackets are used.
   * @see    com.eteks.parser.AbstractSyntax#setClosingBracket
   */
  public char getClosingBracket ();

  /**
   * Returns the char used to separate parameters in a function call. <br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @return the separator of parameters or 0 if no multi parameters function calls
   *         are allowed by the syntax.
   * @see com.eteks.parser.AbstractSyntax#setParameterSeparator
   */
  public char getParameterSeparator ();

  /**
   * Returns all the chars of the syntax that may be used as delimiters
   * between literals, constants and other identifiers. This string must contain at least the
   * delimiters returned by <code>getWhiteSpaceCharacters ()</code> and
   * generally contains the opening and closing brackets, the separator of parameters and the
   * characters of operators, that are not part of literals, constants and identifiers.
   * The <code>AbstractSyntax</code> class computes automatically this string.
   * @return a string containing the set of delimiters of the syntax.
   * @see    com.eteks.parser.AbstractSyntax#getDelimiters
   */
  public String getDelimiters ();

  /**
   * Returns the string used as the operator of assignment by
   * the parsers <code>FunctionParser</code> and <code>ExpressionParser</code>.
   * <UL><LI>For parsing of functions, it's the string that separates the declaration of the function
   *     from its list of parameters, before the expression of the function like the char <code>=</code> in the string
   *     <code>f(x) = x + 1</code> ; <code>FunctionParser</code> requires a valid
   *     assignment operator to parse a function.</LI>
   *     <LI>For expressions parsed with an instance of <code>ExpressionParser</code>, it's the string
   *     at the beginning of the expression like the char <code>=</code> in the string
   *     <code>= A1 + 1</code>, if  this string isn't <code>null</code>.</LI></UL>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @return the operator of assignment.
   * @see    com.eteks.parser.FunctionParser
   * @see    com.eteks.parser.AbstractSyntax#setAssignmentOperator
   */
  public String getAssignmentOperator ();

  /**
   * Returns <code>true</code> if identifiers, constants, operators and function of the syntax
   * are case sensitive. Note that this method is used by the parser only for tests comparing
   * the name of functions and parameters. <code>getConstantKey ()</code>,
   * <code>getUnaryOperatorKey ()</code>, <code>getBinaryOperatorKey ()</code>,
   * <code>getConditionPartKey ()</code> and <code>getCommonFunctionKey ()</code> methods must perform
   * the good test of comparison depending on whether the syntax being case sensitive or not.<br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @return <code>true</code> if the syntax is case sensitive.
   * @see    com.eteks.parser.AbstractSyntax#AbstractSyntax
   */
  public boolean isCaseSensitive ();

  /**
   * Returns <code>true</code> if expressions parsed with this syntax supports short cuts.
   * <UL><LI>If this method returns <code>false</code>, the syntax is the one generally used in
   *         data processing, i.e. every multiply operator is written and
   *         the parameters of common functions are always between brackets
   *         (for example <code>f(x) = 2*sin(x) + 3*x</code>).</LI>
   *     <LI>If this method returns <code>true</code>, the syntax supports also the mathematical
   *         syntax, i.e. multiply operator and brackets around the parameters of common
   *         functions may be omitted. If a literal number is directly followed by an
   *         identifier, a function or an expression between brackets without
   *         any white space character, an implicit multiplication will be performed (for example :
   *         <code>f(x) = 2x</code> or <code>f(x) = 5.2E-2(ln x + 3)</code>).
   *         More generally, a multiplication is applied to two operands if they are separated
   *         by one or more white space characters (for example : <code>f(x,y) = x y ou f(x) = x ln x</code>).<br>
   *         As a common function has a higher priority than any binary operator,
   *         its parameter must be bracketed if it's the result of an operation as in this example :
   *         <code>f(x) = log(2x)</code> is different of <code>f(x) = log 2x</code>.<br>
   *         As the unary operators and calls to common functions have the highest priority and
   *         are right-to-left associative, it allows to write a function like this one :
   *         <code>f(x) = sqr log -x</code> which is the same as <code>f(x) = sqr (log (-x))</code>.
   *         Note than these short cuts are allowed only for common functions, not
   *         for user functions even if they require only one parameter.<br>
   *         Implicit multiplication isn't supported by the <code>CalculatorParser</code> class.</LI></UL>
   * @return <code>true</code> if the syntax supports short cuts.
   * @see    com.eteks.parser.AbstractSyntax#setShortSyntax
   */
  public boolean isShortSyntax ();

  /**
   * Returns <code>true</code> if the string <code>identifier</code> is a correctly written identifier
   * to be used as a user function name, a user function paramater name or an expression parameter.
   * <code>identifier</code> may contain special characters as : or . if these
   * characters are not used as delimiters.<br>
   * This method is not used by <code>CalculatorParser</code> parser.
   * @param  identifier the string to test.
   * @return <code>true</code> if <code>identifier</code> is correct.
   */
  public boolean isValidIdentifier (String identifier);
}
