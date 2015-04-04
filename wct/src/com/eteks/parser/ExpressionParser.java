/*
 * @(#)ExpressionParser.java   10/23/00
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
import java.util.StringTokenizer;

import com.eteks.parser.node.ExpressionNode;
import com.eteks.parser.node.ExpressionParameterNode;

/**
 * Parser able to compile expressions with parameters of the form
 * <i>= expression operating on parameters</i>.
 * The <i>expression</i> may contain unary or binary operators, conditions, common functions
 * or other functions operating on literals, constants and paramaters checked on the fly.<br>
 * A parser is associated to a syntax, instance of the <code>Syntax</code> interface that
 * describes the different lexicals used by the parser and their type. This parser uses
 * also an instance of <code>ExpressionParameter</code> used to check the parameters of
 * parsed expressions and to return their value at runtime.<br>
 * The <code>CompiledExpression compileExpression (String expressionDefinition)</code> method
 * of this class allows to parse and compile the definition <code>expressionDefinition</code>
 * of an expression, that must be made of the following elements :
 * <UL><LI>The expression must start with the operator of assignment returned
 *         by the <code>getAssignmentOperator ()</code> method of the syntax of the parser
 *         if it's not equal to <code>null</code>.</LI>
 *     <LI>Following the operator of assignment is the expression that describes the
 *         operations to compute, using the following syntactic elements :
 *         <UL><LI>parameters validated by the <code>isValidIdentifier ()</code> method of the syntax
 *                 and accepted by the <code>getParameterKey ()</code> method of the
 *                 instance of <code>ExpressionParameter</code> of the parser.</LI>
 *             <LI>literals (numbers, quoted strings) accepted by the <code>getLiteral ()</code> method of the syntax</LI>
 *             <LI>constants (PI true false ...) accepted by the <code>getConstantKey ()</code> method of the syntax</LI>
 *             <LI>unary operators(- ! ...) accepted by the <code>getUnaryOperatorKey ()</code> method of the syntax</LI>
 *             <LI>binary operators (- + / ...) accepted by the <code>getBinaryOperatorKey ()</code> method of the syntax
 *                 and whose priority is returned by the <code>getBinaryOperatorPriority ()</code> method</LI>
 *             <LI>conditions whose parts (? :) are accepted by the <code>getConditionPartKey ()</code> method of the syntax</LI>
 *             <LI>common functions (log sin ...) accepted by the <code>getCommonFunctionKey ()</code> method of the syntax</LI>
 *             <LI>other functions accepted by the <code>getFunction ()</code> method of the syntax.
 *                 Parameters passed to functions must be bracketed between the characters
 *                 returned by <code>getOpeningBracket ()</code> and <code>getClosingBracket ()</code>
 *                 methods of the syntax, and separated by the character returned by the
 *                 <code>getParameterSeparator ()</code> method.
 *                 These functions are instances of classes implementing the
 *                 <code>Function</code> interface. They may be classes written in Java or functions
 *                 compiled previously with a parser able to compile functions.</LI></UL>
 *         Binary operators processed by this parser are infixed, unary operators and functions
 *         are prefixed. Some parts of the expression may be bracketed between the characters
 *         returned by <code>getOpeningBracket ()</code> and <code>getClosingBracket ()</code> methods.</LI></UL>
 *
 * White spaces returned by the <code>getWhiteSpaceCharacters ()</code> method
 * of the syntax may be used anywhere in the definition of the function.<br>
 * If the <code>compileExpression ()</code> method fails to compile a definition,
 * it throws an <code>CompilationException</code> exception
 * that describe the found error (syntax error or other error).<br>
 * Once an expression is compiled, the <code>computeExpression ()</code> methods available
 * for the returned object allow to compute its value with the value of its parameters
 * returned by the <code>getParameterValue ()</code> method of its instance of
 * <code>ExpressionParameter</code>.
 *
 * <P>Here are a few examples of expressions that can be compiled with this parser. This
 * example shows how to compute new column values from the columns of a JDBC result set :
 * <BLOCKQUOTE><PRE> // A class implementing ExpressionParameter supporting the name of columns of a ResultSet
 * class ResultSetColumnParameter implements ExpressionParameter
 * {
 *   ResultSet resultSet;
 *
 *   public ResultSetColumnParameter (ResultSet resultSet)
 *   {
 *     this.resultSet = resultSet;
 *   }
 *
 *   public Object getParameterKey (String parameter)
 *   {
 *     try
 *     {
 *       // Check if parameter exists among the names of columns
 *       if (resultSet.findColumn (parameter) > 0)
 *         return parameter;
 *     }
 *     catch (SQLException e)
 *     { }
 *     return null;
 *   }
 *
 *   public Object getParameterValue (Object parameterKey)
 *   {
 *     try
 *     {
 *       // Simply return the value of the column in current row
 *       return resultSet.getObject ((String)parameterKey);
 *     }
 *     catch (SQLException e)
 *     {
 *       throw new IllegalArgumentException (e.getMessage ());
 *     }
 *   }
 * }
 *
 * // Connect to a database and select values
 * Connection connection = DriverManager.getConnection ("jdbc:....");
 * Statement  statement = connection.createStatement ();
 * ResultSet  resultSet = statement.executeQuery ("SELECT X, Y, Z FROM TABLE1");
 *
 * // Create a parser with the syntax <code>DefaultSyntax</code>
 * // and an instance of ResultSetColumnParameter
 * ExpressionParser parser = new ExpressionParser (new ResultSetColumnParameter (resultSet));
 *
 * // Compile expressions with this parser
 * CompiledExpression ex1 = parser.<b>compileExpression</b> ("= X * LN (X)");
 * CompiledExpression ex2 = parser.<b>compileExpression</b> ("= 5.E-1 * (X + Y) ^ Z");
 *
 * // Display the values of resultSet and
 * // the new values computed with default interpreter <code>DoubleInterpreter</code>
 * System.out.println ("X\tY\tZ\tX * LN (X)\t5.E-1 * (X + Y) ^ Z");
 * while (resultSet.next ())
 *   System.out.println (  String.valueOf (resultSet.getObject ("X")) + "\t"
 *                       + String.valueOf (resultSet.getObject ("Y")) + "\t"
 *                       + String.valueOf (resultSet.getObject ("Z")) + "\t"
 *                       + String.valueOf (ex1.<b>computeExpression</b> ()) + "\t"
 *                       + String.valueOf (ex2.<b>computeExpression</b> ()));</PRE></BLOCKQUOTE>
 *
 * The methods of this class are thread safe (if the methods of the class implementing
 * <code>ExpressionParameter</code> are also thread safe).
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.Syntax
 * @see     com.eteks.parser.CompiledExpression
 * @see     com.eteks.parser.ExpressionParameter
 * @see     com.eteks.jeks.JeksParameter
 * @see     com.eteks.jeks.JeksCellEditor
 */
public class ExpressionParser extends FunctionParser
{
  private ExpressionParameter  expressionParameter;

  /**
   * Creates a parser for expressions whose syntax is an instance of <code>DefaultSyntax</code>.
   * @param expressionParameter the instance of <code>ExpressionParameter</code> used
   *        to check parameters of parsed expressions.
   */
  public ExpressionParser (ExpressionParameter expressionParameter)
  {
    this.expressionParameter = expressionParameter;
  }

  /**
   * Creates a parser for expressions with the syntax <code>syntax</code>.
   * @param syntax              syntax of the parser.
   * @param expressionParameter the instance of <code>ExpressionParameter</code> used
   *        to check parameters of parsed expressions.
   */
  public ExpressionParser (Syntax              syntax,
                           ExpressionParameter expressionParameter)
  {
    super (syntax);
    this.expressionParameter = expressionParameter;
  }

  /**
   * Returns the instance of <code>ExpressionParameter</code> of this parser.
   * @return the instance of <code>ExpressionParameter</code> of this parser.
   */
  public ExpressionParameter getExpressionParameter ()
  {
    return expressionParameter;
  }

  /**
   * Compiles the definition of an expression <code>expressionDefinition</code> and
   * returns the instance of <code>CompiledExpression</code> matching the definition.
   * This method builds a tree of type <code>ExpressionNode</code> matching the expression.
   * This tree is used to compute the value of an expression at interpretation time.
   * @param  expressionDefinition  the definition of the expression.
   * @return an instance of <code>CompiledExpression</code>, with which values can be computed.
   * @throws CompilationException if the syntax of the expression is incorrect or if some of its
   *         elements (literals, constants, operators, functions,...) are not
   *         accepted by the syntax of the parser.
   */
  public CompiledExpression compileExpression (String expressionDefinition) throws CompilationException
  {
    Syntax syntax = getSyntax ();
    String whiteSpaces = syntax.getWhiteSpaceCharacters () != null
                           ? syntax.getWhiteSpaceCharacters ()
                           : "";
    StringTokenizer rechStr = new StringTokenizer (expressionDefinition, whiteSpaces);

    if (!rechStr.hasMoreTokens ())
      throw new CompilationException (CompilationException.SYNTAX_ERROR, 0);

    // The hashtable will be filled with parameters during parsing
    Hashtable  parameters = new Hashtable (1);

    int  assignmentIndex = 0;

    if (   syntax.getAssignmentOperator () != null
        && syntax.getAssignmentOperator ().length () > 0)
    {
      rechStr = new StringTokenizer (expressionDefinition, whiteSpaces);
      if (   !rechStr.hasMoreTokens ()
          || !rechStr.nextToken ().startsWith (syntax.getAssignmentOperator ()))
        throw new CompilationException (CompilationException.SYNTAX_ERROR, 0);
      assignmentIndex = expressionDefinition.indexOf (syntax.getAssignmentOperator ());
    }

    // Expression can't be empty
    if (assignmentIndex == expressionDefinition.length () - 1)
      throw new CompilationException (CompilationException.SYNTAX_ERROR, assignmentIndex + 1);

    // Reuse the parser that parse the expressions of functions
    ExpressionNode expressionTree = (ExpressionNode)parseExpression (expressionDefinition,
                                                                       assignmentIndex
                                                                     + (syntax.getAssignmentOperator () != null
                                                                          ? syntax.getAssignmentOperator ().length ()
                                                                          : 0),
                                                                     parameters);
    // Return the instance of CompiledExpression matching expressionDefinition
    return new CompiledExpression (expressionDefinition, parameters, expressionParameter, expressionTree);
  }

  protected Lexical getLexical (String        expressionDefinition,
                                int           definitionIndex,
                                Object        parserData) throws CompilationException
  {
    try
    {
      return super.getLexical (expressionDefinition, definitionIndex, parserData);
    }
    catch (CompilationException ex)
    {
      // Catch the exception to check if the extracted lexical is a parameter
      Object parameterKey;
      if (   ex.getErrorNumber () == CompilationException.UNKOWN_IDENTIFIER
          && ex.getExtractedString () != null
          && parserData instanceof Hashtable
          && expressionParameter != null
          && (parameterKey = expressionParameter.getParameterKey (ex.getExtractedString ())) != null)
      {
        ((Hashtable)parserData).put (getSyntax ().isCaseSensitive ()
                                       ? ex.getExtractedString ()
                                       : ex.getExtractedString ().toUpperCase (), parameterKey);
        return new Lexical (LEXICAL_PARAMETER, ex.getExtractedString (), parameterKey);
      }

      // Not a parameter of the expression
      throw ex;
    }
  }

  protected ExpressionNode getParameterNode (Lexical parameterLexical,
                                             Object  parserData)
  {
    if (parserData instanceof Hashtable)
      return new ExpressionParameterNode (expressionParameter, parameterLexical.getValue ());
    else
      return super.getParameterNode (parameterLexical, parserData);
  }
}
