/*
 * @(#)FunctionParser.java   01/01/98
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

import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import com.eteks.parser.node.BinaryOperatorNode;
import com.eteks.parser.node.CommonFunctionNode;
import com.eteks.parser.node.ConditionNode;
import com.eteks.parser.node.ConstantNode;
import com.eteks.parser.node.ExpressionNode;
import com.eteks.parser.node.FunctionNode;
import com.eteks.parser.node.FunctionParameterNode;
import com.eteks.parser.node.LiteralNode;
import com.eteks.parser.node.ParameterizedNode;
import com.eteks.parser.node.UnaryOperatorNode;

/**
 * Parser able to compile functions with parameters of the form
 * <i>function (list of parameters) = expression operating on parameters</i>.
 * The <i>expression</i> may contain unary or binary operators, conditions, common functions
 * or other functions operating on literals, constants and paramaters of the function.<br>
 * A parser is associated to a syntax, instance of the <code>Syntax</code> interface that
 * describes the different lexicals used by the parser and their type.<br>
 * The <code>CompiledFunction compileFunction (String functionDefinition)</code> method
 * of this class allows to parse and compile the definition <code>functionDefinition</code>
 * of a function, that must be made of the following elements :
 * <UL><LI>The <i>function</i> name and its parameters must be strings
 *         validated by the <code>isValidIdentifier ()</code> method of the syntax of the parser.
 *         These strings mustn't be reserved words of the syntax of the parser, i.e.
 *         <code>getLiteral ()</code>, <code>getConstantKey ()</code>,
 *         <code>getUnaryOperatorKey ()</code>, <code>getBinaryOperatorKey ()</code>,
 *         <code>getConditionPartKey ()</code> and <code>getCommonFunctionKey ()</code> methods of the syntax
 *         must return <code>null</code> for these strings. The name of the function mustn't also be
 *         a function already defined by the syntax (the <code>getFunction ()</code> method
 *         of the syntax must return <code>null</code> for the name of the function).</LI>
 *     <LI>The <i>list of parameters</i> may contain as many parameters as needed by the
 *         expression, separated by the character returned by the <code>getParameterSeparator ()</code> method
 *         of the syntax. This list may be empty and must be bracketed between the characters
 *         returned by <code>getOpeningBracket ()</code> and <code>getClosingBracket ()</code> methods
 *         of the syntax.</LI>
 *     <LI>The declaration of the function and its parameters must be followed by the operator
 *         of assignment returned by the <code>getAssignmentOperator ()</code> method of the syntax.</LI>
 *     <LI>Following the operator of assignment is the expression of the function that
 *         describes the operations to apply to the parameters, using the following
 *         syntactic elements :
 *         <UL><LI>literals (numbers, quoted strings) accepted by the <code>getLiteral ()</code> method of the syntax</LI>
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
 *                 compiled previously with a parser (the <code>CompiledFunction</code> class
 *                 implements <code>Function</code>).</LI></UL>
 *         As the function may call itself recursively or call functions compiled previously,
 *         users may create any kind of computer operations (in the limit of the stack).
 *         Binary operators processed by this parser are infixed, unary operators and functions
 *         are prefixed. Some parts of the expression may be bracketed between the characters
 *         returned by <code>getOpeningBracket ()</code> and <code>getClosingBracket ()</code> methods.</LI></UL>
 *
 * White spaces returned by the <code>getWhiteSpaceCharacters ()</code> method
 * of the syntax may be used anywhere in the definition of the function.<br>
 * If the <code>compileFunction ()</code> method fails to compile a definition,
 * it throws a <code>CompilationException</code> exception
 * that describe the found error (syntax error or other error).<br>
 * Once a funtion is compiled, the <code>computeFunction ()</code> methods available for the
 * returned object allow to compute its value according to the value of the parameters
 * passed to them.
 *
 * <P>Here are a few examples of functions that can be compiled with this parser :
 * <BLOCKQUOTE><PRE> // Create a parser with the syntax <code>PascalSyntax</code>
 * FunctionParser parser = new FunctionParser (new PascalSyntax ());
 *
 * // Compile functions with this parser
 * CompiledFunction f = parser.<b>compileFunction</b> ("F(x) = x * LN (x)");
 * CompiledFunction fact = parser.<b>compileFunction</b> ("FACT(x) = IF x <= 0 THEN 1 ELSE x * FACT (x - 1)");
 * CompiledFunction cotan = parser.<b>compileFunction</b> ("COTAN(x) = 1 / TAN (x)");
 * // Add function cotan to be able to use it in other functions
 * ((PascalSyntax)parser.getSyntax ()).addFunction (cotan);
 * CompiledFunction f4 = parser.<b>compileFunction</b> ("F4(x,y,z) = 5.1E-1 * COTAN (X) * COTAN (Y) * COTAN (Z)");
 *
 * // Compute functions with double values
 * double r1 = f.<b>computeFunction</b> (new double [] {2.1});
 * double fact10 = fact.<b>computeFunction</b> (new double [] {10});
 * // Compute the 100 value of cotan between 0 and Math.PI
 * double cotanValues [] = new double [100];
 * double cotanParam  [] = new double [1];
 * for (int i = 0; i < cotanValues.length; i++)
 * {
 *   // Use the same table for parameters to avoid useless creations of table
 *   cotanParam  [0] = Math.PI / i;
 *   cotanValues [i] = cotan.<b>computeFunction</b> (cotanParam).doubleValue ();
 * }
 * double r4 = f4.<b>computeFunction</b> (new double [] {1, 3, 3});</PRE></BLOCKQUOTE>
 *
 * The methods of this class are thread safe.
 *
 * @version 1.0.2
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.parser.Syntax
 * @see     com.eteks.parser.Function
 * @see     com.eteks.parser.CompiledFunction
 */
public class FunctionParser extends Parser
{
  // Default Syntax isn't assigned at static initialization time
  // to avoid to load automatically the DefaultSyntax class
  private static Syntax defaultSyntax;

  /**
   * The syntax used by parser.
   */
  private Syntax syntax;

  /**
   * Creates a parser for functions whose syntax is an instance of <code>DefaultSyntax</code>.
   */
  public FunctionParser ()
  {
    if (defaultSyntax == null)
      defaultSyntax = new DefaultSyntax ();

    this.syntax = defaultSyntax;
  }

  /**
   * Creates a parser for functions with the syntax <code>syntax</code>.
   * @param syntax syntax of the parser.
   */
  public FunctionParser (Syntax syntax)
  {
    this.syntax = syntax;
  }

  /**
   * Returns the syntax used by this parser.
   * @return the syntax of the parser.
   */
  public Syntax getSyntax ()
  {
    return syntax;
  }

  /**
   * Compiles the definition of a function <code>functionDefinition</code> and
   * returns the instance of <code>CompiledFunction</code> matching the definition.
   * This method builds a tree of type <code>ExpressionNode</code> matching the expression
   * of the function. This tree is used to compute the value of a function at interpretation time.
   * @param  functionDefinition  the definition of the function.
   * @return an instance of <code>CompiledFunction</code>, with which values can be computed.
   * @throws CompilationException if the syntax of the function is incorrect or if some
   *         elements (literals, constants, operators, functions,...) of its expression are not
   *         accepted by the syntax of the parser.
   */
  public CompiledFunction compileFunction (String functionDefinition) throws CompilationException
  {
    if (   syntax.getAssignmentOperator () == null
        || syntax.getAssignmentOperator ().length () == 0)
      throw new IllegalArgumentException ("No assignment operator in syntax");

    String    functionName    = parseFunctionName (functionDefinition);
    String [] parametersArray = parseFunctionParametersName (functionDefinition, functionName);

    CompiledFunction function = new CompiledFunction (functionDefinition, functionName,
                                                      parametersArray, null);

    // Assign operator already checked with parameters search
    int  assignmentIndex = functionDefinition.indexOf (syntax.getAssignmentOperator ());
    ExpressionNode  expressionTree = (ExpressionNode)parseExpression (functionDefinition, assignmentIndex + syntax.getAssignmentOperator ().length (),
                                                                      function);
    function.setExpressionTree (expressionTree);
    return function;
  }

  // Search the function name in functionDefinition, checks it and returns it
  private String parseFunctionName (String functionDefinition) throws CompilationException
  {
    StringTokenizer rechStr = new StringTokenizer (functionDefinition,
                                                     (syntax.getWhiteSpaceCharacters () != null
                                                        ? syntax.getWhiteSpaceCharacters ()
                                                        : "")
                                                   + String.valueOf (syntax.getOpeningBracket ()));

    // Skips all the leading blanks
    if (!rechStr.hasMoreTokens ())
      throw new CompilationException (CompilationException.INVALID_FUNCTION_NAME, 0);

    String functionName = rechStr.nextToken ();
    // Checks if function characters are valid
    if (!syntax.isValidIdentifier (functionName))
      throw new CompilationException (CompilationException.INVALID_FUNCTION_NAME, 0, functionName);

    // Checks if the function isn't a reserved word
    if (isReservedWord (functionName))
      throw new CompilationException (CompilationException.RESERVED_WORD, 0, functionName);

    // Checks if the function name doesn't already exist
    if (syntax.getFunction (functionName) != null)
      throw new CompilationException (CompilationException.FUNCTION_NAME_ALREADY_EXISTS, 0, functionName);

    return functionName;
  }

  // Search the function parameters name in functionDefinition, checks then and returns them
  private String [] parseFunctionParametersName (String functionDefinition,
                                                 String functionName) throws CompilationException
  {
    Vector parameters;
    String whiteSpaces = syntax.getWhiteSpaceCharacters () != null ? syntax.getWhiteSpaceCharacters () : "";

    // Start parameters search after function name
    int index = functionDefinition.indexOf (functionName) + functionName.length ();
    StringTokenizer rechStr = new StringTokenizer (functionDefinition.substring (index), whiteSpaces);
    if (   !rechStr.hasMoreTokens ()
        || rechStr.nextToken ().indexOf (syntax.getOpeningBracket ()) != 0)
      throw new CompilationException (CompilationException.OPENING_BRACKET_EXPECTED, index);

    int  openingBracketIndex = functionDefinition.indexOf (syntax.getOpeningBracket ());
    int  closingBracketIndex;

    // Syntax error if no closing bracket followed with an assign operator
    if ((closingBracketIndex = functionDefinition.indexOf (syntax.getClosingBracket (), openingBracketIndex + 1)) == -1)
      throw new CompilationException (CompilationException.CLOSING_BRACKET_EXPECTED, openingBracketIndex + 1);

    rechStr = new StringTokenizer (functionDefinition.substring (closingBracketIndex + 1), whiteSpaces);
    if (   !rechStr.hasMoreTokens ()
        || !rechStr.nextToken ().startsWith (syntax.getAssignmentOperator ()))
      throw new CompilationException (CompilationException.ASSIGN_OPERATOR_EXPECTED, closingBracketIndex + 1);

    // Search for parameters
    if (openingBracketIndex + 1 == closingBracketIndex)
      parameters = new Vector (0);
    else
    {
      rechStr = new StringTokenizer (functionDefinition.substring (openingBracketIndex + 1, closingBracketIndex),
                                       whiteSpaces
                                     + (syntax.getParameterSeparator () != 0
                                          ? String.valueOf (syntax.getParameterSeparator ())
                                          : ""));

      if (!rechStr.hasMoreTokens ())
        parameters = new Vector (0);
      else
      {
        parameters = new Vector (rechStr.countTokens ());
        int definitionIndex = openingBracketIndex + 1;
        for (int i = 0; rechStr.hasMoreTokens (); i++)
        {
          String parameter = rechStr.nextToken ();

          // Checks if parameter characters are valid
          if (!syntax.isValidIdentifier (parameter))
            throw new CompilationException (CompilationException.INVALID_PARAMETER_NAME, definitionIndex, parameter);

          // Checks if parameter isn't a reserved word
          // In case the syntax supports short cuts, parameters can't have the same
          // name as any function of the syntax (otherwise, in expressions of the type
          // functionOrParam (...), the parser couldn't always make its choice between a call to the
          // function functionOrParam and an implicit multiplication of the parameter
          // functionOrParam with a bracketed expression)
          if (   isReservedWord (parameter)
              || (   syntax.isShortSyntax ()
                  && (   syntax.getFunction (parameter) != null
                      || compare (parameter, functionName, syntax.isCaseSensitive ()))))
            throw new CompilationException (CompilationException.RESERVED_WORD, definitionIndex, parameter);

          // Checks if parameter doens't already exist in function parameter list
          for (int j = 0; j < parameters.size (); j++)
            if (compare (parameter, (String)parameters.elementAt (j), syntax.isCaseSensitive ()))
              throw new CompilationException (CompilationException.DUPLICATED_PARAMETER_NAME, definitionIndex, parameter);

          parameters.addElement (parameter);
          definitionIndex = functionDefinition.indexOf (syntax.getParameterSeparator (), definitionIndex) + 1;
        }
      }
    }

    String parametersArray [] = new String [parameters.size ()];
    parameters.copyInto (parametersArray);
    return parametersArray;
  }

  /**
   * Parses the expression of <code>functionDefinition</code> from the index
   * <code>expressionStartIndex</code> and returns its <code>ExpressionNode</code> matching tree.
   * This tree is built with the automaton implemented in the <code>parseExpression ()</code> method
   * of the <code>Parser</code> class using the syntax graph and the transition graph of
   * <code>FunctionParser</code>.
   * @param  functionDefinition the string to parse.
   * @param  definitionIndex    the index from which the parsing starts.
   * @param  parserData         optional data.
   * @return the expression tree of the expression.
   * @throws CompilationException if the parsed expression doesn't respect the syntax of this parser.
   * @see    com.eteks.parser.Parser#parseExpression
   */
  protected Object parseExpression (String  functionDefinition,
                                    int     expressionStartIndex,
                                    Object  parserData) throws CompilationException
  {
    ElemFct elem = (ElemFct)parseExpression (functionDefinition, expressionStartIndex,
                                             new StackFct (), parserData);
    return elem.expression;
  }

  /**
   * Returns <code>true</code> if <code>word</code> is a reserved word of the
   * syntax used by this parser. A reserved word might be a literal, a constant, an unary
   * or binary operator, a condition part or a common function and can't be used as a
   * the name of a function or its parameters.
   * @param  word the string to check.
   * @return <code>true</code> if <code>word</code> is a reserved word.
   */
  protected boolean isReservedWord (String  word)
  {
    return    syntax.getLiteral (word, new StringBuffer ()) != null
           || syntax.getConstantKey (word) != null
           || syntax.getUnaryOperatorKey (word) != null
           || syntax.getBinaryOperatorKey (word) != null
           || syntax.getConditionPartKey (word) != null
           || syntax.getCommonFunctionKey (word) != null;
  }

  /**
   * Returns <code>true</code> if the two string <code>s1</code> and <code>s2</code> are equal,
   * ignoring the case of characters if <code>caseSensitive</code> is <code>false</code>.
   * @param  string1 first string.
   * @param  string2 second string.
   * @param  caseSensitive case sensitivity.
   * @return the result of the <code>equals ()</code> method if <code>caseSensitive</code> is
   *         is equal to <code>true</code>, otherwise returns the result of the
   *         <code>equalsIgnoreCase ()</code> method.
   */
  protected static boolean compare (String string1, String string2, boolean caseSensitive)
  {
    if (caseSensitive)
      return string1.equals (string2);
    else
      return string1.equalsIgnoreCase (string2);
  }

  /**
   * Returns the lexical found in the definition <code>functionDefinition</code>
   * beginning at the character index <code>definitionIndex</code>.
   * This method may be overriden in subclasses to parse additional kind of parameters
   * and return <code>LEXICAL_PARAMETER</code>. In that case, the <code>getParameterNode ()</code> method
   * must be overriden to return the expression node matching the additional parameters.
   * @param  functionDefinition   the string to parse.
   * @param  definitionIndex      the current index of parsing.
   * @param  parserData           used to store the current instance of <code>CompiledFunction</code>.
   * @return a lexical with a code among <code>LEXICAL_VOID</code>, <code>LEXICAL_WHITE_SPACE</code>,
   *         <code>LEXICAL_LITERAL</code>, <code>LEXICAL_UNARY_OPERATOR</code>,
   *         <code>LEXICAL_BINARY_OPERATOR</code>, <code>LEXICAL_COMMON_FUNCTION</code>,
   *         <code>LEXICAL_OPENING_BRACKET</code>, <code>LEXICAL_CLOSING_BRACKET</code>,
   *         <code>LEXICAL_SYNONYMOUS_OPERATOR</code>, <code>LEXICAL_CONSTANT</code>,
   *         <code>LEXICAL_PARAMETER</code>, <code>LEXICAL_FUNCTION</code>,
   *         <code>LEXICAL_PARAMETER_SEPARATOR</code>, <code>LEXICAL_IF</code>,
   *         <code>LEXICAL_THEN</code>, <code>LEXICAL_ELSE</code>.
   * @throws CompilationException if this method doesn't recognize any expression lexical at index <code>definitionIndex</code>.
   * @see    com.eteks.parser.ExpressionParser#getLexical
   * @see    #getParameterNode
   */
  protected Lexical getLexical (String functionDefinition,
                                int    definitionIndex,
                                Object parserData) throws CompilationException
  {
    if (definitionIndex >= functionDefinition.length ())
      return new Lexical (LEXICAL_VOID, "", null);

    // Search for white spaces
    StringBuffer extractedLexical = new StringBuffer ();
    while (   definitionIndex < functionDefinition.length ()
           && syntax.getWhiteSpaceCharacters () != null
           && syntax.getWhiteSpaceCharacters ().indexOf (functionDefinition.charAt (definitionIndex)) != -1)
      extractedLexical.append (functionDefinition.charAt (definitionIndex++));

    if (extractedLexical.length () > 0)
      return new Lexical (LEXICAL_WHITE_SPACE, extractedLexical.toString (), null);

    char c = functionDefinition.charAt (definitionIndex);
    if (c == syntax.getOpeningBracket ())
      return new Lexical (LEXICAL_OPENING_BRACKET, String.valueOf (c), null);
    if (c == syntax.getClosingBracket ())
      return new Lexical (LEXICAL_CLOSING_BRACKET, String.valueOf (c), null);
    if (c == syntax.getParameterSeparator ())
      return new Lexical (LEXICAL_PARAMETER_SEPARATOR, String.valueOf (c), null);

    String subExpression = functionDefinition.substring (definitionIndex);
    // v1.0.1 Moved getLiteral () to the end to give an higher priority to operators

    String          token  = null;
    StringTokenizer tokens = new StringTokenizer (subExpression, syntax.getDelimiters ());

    if (tokens.hasMoreTokens ())
      token = tokens.nextToken ();
    if (   token == null
        || !subExpression.startsWith (token))
    {
      // subExpression starts with a delimiter
      token = null;
      for (int i = 0; i < subExpression.length (); i++)
      {
        String operator = subExpression.substring (0, i + 1);
        if (   syntax.getUnaryOperatorKey (operator) != null
            || syntax.getBinaryOperatorKey (operator) != null
            || syntax.getConditionPartKey (operator) != null)
          token = operator; // Don't break the loop now, some operators may be longer
                            // and start with the same characters
      }

      if (token == null)
        throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);
    }
    else
    {
      // Search if token is a parameter or a constant
      if (parserData instanceof CompiledFunction)
      {
        String [] parameters = ((CompiledFunction)parserData).getParameters ();
        for (int i = 0; i < parameters.length; i++)
          if (compare (token, parameters [i], syntax.isCaseSensitive ()))
            return new Lexical (LEXICAL_PARAMETER, token, new Integer (i));
      }

      Object key = syntax.getConstantKey (token);
      if (key != null)
        return new Lexical (LEXICAL_CONSTANT, token, key);

      // Search if token is a function
      Function function = syntax.getFunction (token);
      if (function != null)
        return new Lexical (LEXICAL_FUNCTION, token, function);

      // Should authorize recursive call only if ifThenElse syntax enabled
      if (   parserData instanceof CompiledFunction
          && compare (token, ((CompiledFunction)parserData).getName (), syntax.isCaseSensitive ()))
        return new Lexical (LEXICAL_FUNCTION, token, parserData);

      key = syntax.getCommonFunctionKey (token);
      if (key != null)
        return new Lexical (LEXICAL_COMMON_FUNCTION, token, key);
    }

    // Search if token is an unary or binary operator or a condition part
    // These syntax lexicals can contain delimiters or not, so the search
    // is done outside the previous if else blocks
    Object key = syntax.getUnaryOperatorKey (token);
    if (key != null)
      if (syntax.getBinaryOperatorKey (token) != null)
        return new Lexical (LEXICAL_SYNONYMOUS_OPERATOR, token, null);
      else
        return new Lexical (LEXICAL_UNARY_OPERATOR, token, key);

    key = syntax.getBinaryOperatorKey (token);
    if (key != null)
      return new Lexical (LEXICAL_BINARY_OPERATOR, token, key);

    key = syntax.getConditionPartKey (token);
    if (key != null)
      if (Syntax.CONDITION_IF.equals (key))
        return new Lexical (LEXICAL_IF, token, key);
      else if (Syntax.CONDITION_THEN.equals (key))
        return new Lexical (LEXICAL_THEN, token, key);
      else if (Syntax.CONDITION_ELSE.equals (key))
        return new Lexical (LEXICAL_ELSE, token, key);

    // v1.0.1 Moved getLiteral () to the end to give an higher priority to operators
    Object lexicalValue = syntax.getLiteral (subExpression, extractedLexical);
    if (lexicalValue != null)
      return new Lexical (LEXICAL_LITERAL, extractedLexical.toString (), lexicalValue);
      
    // No known lexical was found, throw an exception
    throw new CompilationException (syntax.isValidIdentifier (token)
                                       ? CompilationException.UNKOWN_IDENTIFIER
                                       : CompilationException.SYNTAX_ERROR, definitionIndex, token);
  }

  /**
   * Returns the node bound to <code>graphNode</code> with the link <code>lexical</code>
   * in the syntactic graph of the calculator.<br>
   * <code>lexical</code>, <code>expressionStack</code> and <code>parserData</code>
   * are used to create the tree of the expression of the current parsed function.
   * @param  graphNode       a node in the syntactic graph. First node is equal to <code>NODE_START</code>.
   * @param  lexical         the last parsed lexical.
   * @param  definitionIndex the current index of parsing.
   * @param  expressionStack stack used to accumulate the operators of different priority or <code>null</code>.
   * @param  parserData      contains some extra information used by the parser.
   * @return the node bound to <code>graphNode</code> with the link <code>lexical</code>.
   *         Returns <code>NODE_END</code> if the automaton must stop parsing.
   * @throws CompilationException if no link of type <code>lexical</code> starts from <code>graphNode</code>
   *         or another kind of error of compilation occures.
   */
  protected int getBoundNode (int     graphNode,
                              Lexical lexical,
                              int     definitionIndex,
                              Stack   expressionStack,
                              Object  parserData) throws CompilationException
  {
    int boundNode = (syntax.isShortSyntax ()
                       ? shortSyntaxGraph
                       : syntaxGraph) [lexical.getCode ()][graphNode];
    if (boundNode == -1)
      throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);

    // Execute the transition that compiles the expression
    executeTransition (transitionGraph [lexical.getCode ()][graphNode], definitionIndex,
                       lexical, expressionStack, parserData);

    return boundNode == N_END
             ? Parser.NODE_END
             : boundNode;
  }

  // Lexical types
  /**
   * Lexical code returned by <code>getLexical ()</code> at the end of parsed string.
   */
  protected final static int LEXICAL_VOID = 0;  // Empty string
  /**
   * Lexical code returned by <code>getLexical ()</code> for a sequence of white spaces.
   * @see com.eteks.parser.Syntax#getWhiteSpaceCharacters
   */
  protected final static int LEXICAL_WHITE_SPACE = 1;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a literal (constant, number,
   * quoted string).
   * @see com.eteks.parser.Syntax#getLiteral
   */
  protected final static int LEXICAL_LITERAL = 2;
  /**
   * Lexical code returned by <code>getLexical ()</code> for an unary operator.
   * @see com.eteks.parser.Syntax#getUnaryOperatorKey
   */
  protected final static int LEXICAL_UNARY_OPERATOR = 3;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a binary operator.
   * @see com.eteks.parser.Syntax#getBinaryOperatorKey
   */
  protected final static int LEXICAL_BINARY_OPERATOR = 4;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a common function.
   * @see com.eteks.parser.Syntax#getCommonFunctionKey
   */
  protected final static int LEXICAL_COMMON_FUNCTION = 5;
  /**
   * Lexical code returned by <code>getLexical ()</code> for an opening bracket.
   * @see com.eteks.parser.Syntax#getOpeningBracket
   */
  protected final static int LEXICAL_OPENING_BRACKET = 6;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a closing bracket.
   * @see com.eteks.parser.Syntax#getClosingBracket
   */
  protected final static int LEXICAL_CLOSING_BRACKET = 7;
  /**
   * Lexical code returned by <code>getLexical ()</code> for synonymous unary and binary operators.
   */
  protected final static int LEXICAL_SYNONYMOUS_OPERATOR = 8;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a constant.
   * @see com.eteks.parser.Syntax#getConstantKey
   */
  protected final static int LEXICAL_CONSTANT = 9;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a parameter.
   */
  protected final static int LEXICAL_PARAMETER = 10;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a user function.
   * @see com.eteks.parser.Syntax#getFunction
   */
  protected final static int LEXICAL_FUNCTION = 11;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a parameter separator.
   * @see com.eteks.parser.Syntax#getParameterSeparator
   */
  protected final static int LEXICAL_PARAMETER_SEPARATOR = 12;
  /**
   * Lexical code returned by <code>getLexical ()</code> for an IF condition part.
   * @see com.eteks.parser.Syntax#getConditionPartKey
   */
  protected final static int LEXICAL_IF = 13;
  /**
   * Lexical code returned by <code>getLexical ()</code> for a THEN condition part.
   * @see com.eteks.parser.Syntax#getConditionPartKey
   */
  protected final static int LEXICAL_THEN = 14;
  /**
   * Lexical code returned by <code>getLexical ()</code> for an ELSE condition part.
   * @see com.eteks.parser.Syntax#getConditionPartKey
   */
  protected final static int LEXICAL_ELSE = 15;

  // Constants of nodes in syntatic graph
  private final static byte N_START  = NODE_START; // Node start
  private final static byte N_CL_BR  = 1;          // Node closing bracket or operand
  private final static byte N_C_FCT  = 2;          // Node common function
  private final static byte N_OP_IF  = 3;          // Node binary operator or if
  private final static byte N_U_FCT  = 4;          // Node user function
  private final static byte N_OP_BR  = 5;          // Node opening bracket
  private final static byte N_UN_OP  = 6;          // Node unary operator
  private final static byte N_MULT   = 7;          // Node implicit multiply
  private final static byte N_END    = 8;          // Node end

  // Constants stored in the array transitionGraph
  // used in the executeTransition () method to switch to a transition
  private final static byte ADD_BINARY_OP               = 1;
  private final static byte ADD_LITERAL                 = 2;
  private final static byte ADD_TERNARY_IF_LITERAL      = 3;
  private final static byte ADD_MULT_LITERAL            = 4;
  private final static byte ADD_COMMON_FCT              = 5;
  private final static byte ADD_TERNARY_IF_COMMON_FCT   = 6;
  private final static byte ADD_MULT_COMMON_FCT         = 7;
  private final static byte ADD_CONSTANT                = 8;
  private final static byte ADD_TERNARY_IF_CONSTANT     = 9;
  private final static byte ADD_MULT_CONSTANT           = 10;
  private final static byte ADD_PARAMETER               = 11;
  private final static byte ADD_TERNARY_IF_PARAMETER    = 12;
  private final static byte ADD_MULT_PARAMETER          = 13;
  private final static byte ADD_USER_FCT                = 14;
  private final static byte ADD_TERNARY_IF_USER_FCT     = 15;
  private final static byte ADD_MULT_USER_FCT           = 16;
  private final static byte ADD_OPEN_BRACKET            = 17;
  private final static byte ADD_TERNARY_IF_OPEN_BRACKET = 18;
  private final static byte ADD_MULT_OPEN_BRACKET       = 19;
  private final static byte CHECK_CLOSE_BRACKET         = 20;
  private final static byte CHECK_OPEN_CLOSE_BRACKETS   = 21;
  private final static byte CHECK_PARAMETER_SEPARATOR   = 22;
  private final static byte ADD_UNARY_OP                = 23;
  private final static byte ADD_TERNARY_IF_UNARY_OP     = 24;
  private final static byte ADD_MULT_UNARY_OP           = 25;
  private final static byte ADD_IF                      = 26;
  private final static byte CHECK_THEN                  = 27;
  private final static byte CHECK_ELSE                  = 28;
  private final static byte CHECK_END                   = 29;

  private final static byte shortSyntaxGraph [][] =
   // v1.0.2 : Added nodes (N_MULT, LEXICAL_VOID), (N_MULT, LEXICAL_BINARY_OPERATOR), (N_MULT, LEXICAL_UNARY_OPERATOR), (N_MULT, LEXICAL_SYNONYMOUS_OPERATOR),
   //          (N_MULT, LEXICAL_CLOSING_BRACKET), (N_MULT, LEXICAL_PARAMETER_SEPARATOR), (N_MULT, LEXICAL_THEN), (N_MULT, LEXICAL_ELSE)
   //          to correct the bug where a string using useless spaces like "2 * 2" throws a syntax error (not "2*2" or "2 2")
   // N_START  N_CL_BR  N_C_FCT  N_OP_IF  N_U_FCT  N_OP_BR  N_UN_OP  N_MULT
    {{-1,      N_END,   -1,      -1,      -1,      -1,      -1,      N_END  },   // LEXICAL_VOID
     {N_START, N_MULT,  N_OP_BR, N_OP_IF, N_U_FCT, N_OP_BR, N_UN_OP, N_MULT },   // LEXICAL_WHITE_SPACE
     {N_CL_BR, N_CL_BR, -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR, N_CL_BR},   // LEXICAL_LITERAL
     {N_UN_OP, -1,      N_UN_OP, N_UN_OP, -1,      N_UN_OP, N_UN_OP, N_UN_OP},   // LEXICAL_UNARY_OPERATOR
     {-1,      N_OP_IF, -1,      -1,      -1,      -1,      -1,      N_OP_IF},   // LEXICAL_BINARY_OPERATOR  
     {N_C_FCT, N_C_FCT, -1,      N_C_FCT, -1,      N_C_FCT, N_C_FCT, N_C_FCT},   // LEXICAL_COMMON_FUNCTION
     {N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR},   // LEXICAL_OPENING_BRACKET
     {-1,      N_CL_BR, -1,      -1,      -1,      N_CL_BR, -1,      N_CL_BR},   // LEXICAL_CLOSING_BRACKET
     {N_UN_OP, N_OP_IF, N_UN_OP, N_UN_OP, -1,      N_UN_OP, N_UN_OP, N_OP_IF},   // LEXICAL_SYNONYMOUS_OPERATOR
     {N_CL_BR, N_CL_BR, -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR, N_CL_BR},   // LEXICAL_CONSTANT
     {N_CL_BR, N_CL_BR, -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR, N_CL_BR},   // LEXICAL_PARAMETER
     {N_U_FCT, N_U_FCT, -1,      N_U_FCT, -1,      N_U_FCT, N_U_FCT, N_U_FCT},   // LEXICAL_FUNCTION
     {-1,      N_START, -1,      -1,      -1,      -1,      -1,      N_START},   // LEXICAL_PARAMETER_SEPARATOR
     {N_START, -1,      -1,      -1,      -1,      N_START, -1,      -1     },   // LEXICAL_IF
     {-1,      N_START, -1,      -1,      -1,      -1,      -1,      N_START},   // LEXICAL_THEN
     {-1,      N_START, -1,      -1,      -1,      -1,      -1,      N_START}};  // LEXICAL_ELSE

  private final static byte syntaxGraph [][] =
   // N_START  N_CL_BR  N_C_FCT  N_OP_IF  N_U_FCT  N_OP_BR  N_UN_OP
    {{-1,      N_END,   -1,      -1,      -1,      -1,      -1     },   // LEXICAL_VOID
     {N_START, N_CL_BR, N_C_FCT, N_OP_IF, N_U_FCT, N_OP_BR, N_UN_OP},   // LEXICAL_WHITE_SPACE
     {N_CL_BR, -1,      -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR},   // LEXICAL_LITERAL
     {N_UN_OP, -1,      -1,      N_UN_OP, -1,      N_UN_OP, N_UN_OP},   // LEXICAL_UNARY_OPERATOR
     {-1,      N_OP_IF, -1,      -1,      -1,      -1,      -1     },   // LEXICAL_BINARY_OPERATOR
     {N_C_FCT, -1,      -1,      N_C_FCT, -1,      N_C_FCT, N_C_FCT},   // LEXICAL_COMMON_FUNCTION
     {N_OP_BR, -1,      N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR, N_OP_BR},   // LEXICAL_OPENING_BRACKET
     {-1,      N_CL_BR, -1,      -1,      -1,      N_CL_BR, -1     },   // LEXICAL_CLOSING_BRACKET
     {N_UN_OP, N_OP_IF, -1,      N_UN_OP, -1,      N_UN_OP, N_UN_OP},   // LEXICAL_SYNONYMOUS_OPERATOR
     {N_CL_BR, -1,      -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR},   // LEXICAL_CONSTANT
     {N_CL_BR, -1,      -1,      N_CL_BR, -1,      N_CL_BR, N_CL_BR},   // LEXICAL_PARAMETER
     {N_U_FCT, -1,      -1,      N_U_FCT, -1,      N_U_FCT, N_U_FCT},   // LEXICAL_FUNCTION
     {-1,      N_START, -1,      -1,      -1,      -1,      -1     },   // LEXICAL_PARAMETER_SEPARATOR
     {N_START, -1,      -1,      -1,      -1,      N_START, -1     },   // LEXICAL_IF
     {-1,      N_START, -1,      -1,      -1,      -1,      -1     },   // LEXICAL_THEN
     {-1,      N_START, -1,      -1,      -1,      -1,      -1     }};  // LEXICAL_ELSE


  private final static byte transitionGraph [][] =
   // N_START                      N_CL_BR                    N_C_FCT           N_OP_IF           N_U_FCT  N_OP_BR                      N_UN_OP           N_MULT
    {{0,                           CHECK_END,                 0,                0,                0,       0,                           0,                CHECK_END                },
     {0,                           0,                         0,                0,                0,       0,                           0,                0                        }, // Nothing to do
     {ADD_TERNARY_IF_LITERAL,      ADD_MULT_LITERAL,          ADD_LITERAL,      ADD_LITERAL,      0,       ADD_TERNARY_IF_LITERAL,      ADD_LITERAL,      ADD_MULT_LITERAL         },
     {ADD_TERNARY_IF_UNARY_OP,     0,                         ADD_UNARY_OP,     ADD_UNARY_OP,     0,       ADD_TERNARY_IF_UNARY_OP,     ADD_UNARY_OP,     ADD_MULT_UNARY_OP        },
     {0,                           ADD_BINARY_OP,             0,                0,                0,       0,                           0,                ADD_BINARY_OP            },
     {ADD_TERNARY_IF_COMMON_FCT,   ADD_MULT_COMMON_FCT,       ADD_COMMON_FCT,   ADD_COMMON_FCT,   0,       ADD_TERNARY_IF_COMMON_FCT,   ADD_COMMON_FCT,   ADD_MULT_COMMON_FCT      },
     {ADD_TERNARY_IF_OPEN_BRACKET, ADD_MULT_OPEN_BRACKET,     ADD_OPEN_BRACKET, ADD_OPEN_BRACKET, 0,       ADD_TERNARY_IF_OPEN_BRACKET, ADD_OPEN_BRACKET, ADD_MULT_OPEN_BRACKET    },
     {0,                           CHECK_CLOSE_BRACKET,       0,                0,                0,       CHECK_OPEN_CLOSE_BRACKETS,   0,                CHECK_CLOSE_BRACKET      },
     {ADD_TERNARY_IF_UNARY_OP,     ADD_BINARY_OP,             ADD_UNARY_OP,     ADD_UNARY_OP,     0,       ADD_TERNARY_IF_UNARY_OP,     ADD_UNARY_OP,     ADD_BINARY_OP            },
     {ADD_TERNARY_IF_CONSTANT,     ADD_MULT_CONSTANT,         ADD_CONSTANT,     ADD_CONSTANT,     0,       ADD_TERNARY_IF_CONSTANT,     ADD_CONSTANT,     ADD_MULT_CONSTANT        },
     {ADD_TERNARY_IF_PARAMETER,    ADD_MULT_PARAMETER,        ADD_PARAMETER,    ADD_PARAMETER,    0,       ADD_TERNARY_IF_PARAMETER,    ADD_PARAMETER,    ADD_MULT_PARAMETER       },
     {ADD_TERNARY_IF_USER_FCT,     ADD_MULT_USER_FCT,         ADD_USER_FCT,     ADD_USER_FCT,     0,       ADD_TERNARY_IF_USER_FCT,     ADD_USER_FCT,     ADD_MULT_USER_FCT        },
     {0,                           CHECK_PARAMETER_SEPARATOR, 0,                0,                0,       0,                           0,                CHECK_PARAMETER_SEPARATOR},
     {ADD_IF,                      0,                         0,                0,                0,       ADD_IF,                      0,                0                        },
     {0,                           CHECK_THEN,                0,                0,                0,       0,                           0,                CHECK_THEN               },
     {0,                           CHECK_ELSE,                0,                0,                0,       0,                           0,                CHECK_ELSE               }};

  private void executeTransition (byte    transition,
                                  int     definitionIndex,
                                  Lexical lexical,
                                  Stack   expressionStack,
                                  Object  parserData) throws CompilationException
  {
    switch (transition)
    {
      case ADD_BINARY_OP :
        addBinaryOperator (lexical, expressionStack);
        break;
      case ADD_LITERAL :
        addLiteral (lexical, expressionStack);
        break;
      case ADD_TERNARY_IF_LITERAL :
        addTernaryIf (expressionStack);
        addLiteral (lexical, expressionStack);
        break;
      case ADD_MULT_LITERAL :
        addMultiply (expressionStack);
        addLiteral (lexical, expressionStack);
        break;
      case ADD_COMMON_FCT :
        addCommonFunction  (lexical, expressionStack);
        break;
      case ADD_TERNARY_IF_COMMON_FCT :
        addTernaryIf (expressionStack);
        addCommonFunction  (lexical, expressionStack);
        break;
      case ADD_MULT_COMMON_FCT :
        addMultiply (expressionStack);
        addCommonFunction (lexical, expressionStack);
        break;
      case ADD_CONSTANT :
        addConstant (lexical, expressionStack);
        break;
      case ADD_TERNARY_IF_CONSTANT :
        addTernaryIf (expressionStack);
        addConstant (lexical, expressionStack);
        break;
      case ADD_MULT_CONSTANT :
        addMultiply (expressionStack);
        addConstant (lexical, expressionStack);
        break;
      case ADD_PARAMETER :
        addParameter (definitionIndex, lexical, expressionStack, parserData);
        break;
      case ADD_TERNARY_IF_PARAMETER :
        addTernaryIf (expressionStack);
        addParameter (definitionIndex, lexical, expressionStack, parserData);
        break;
      case ADD_MULT_PARAMETER :
        addMultiply (expressionStack);
        addParameter (definitionIndex, lexical, expressionStack, parserData);
        break;
      case ADD_USER_FCT :
        addFunction (lexical, expressionStack, parserData);
        break;
      case ADD_TERNARY_IF_USER_FCT :
        addTernaryIf (expressionStack);
        addFunction (lexical, expressionStack, parserData);
        break;
      case ADD_MULT_USER_FCT :
        addMultiply (expressionStack);
        addFunction (lexical, expressionStack, parserData);
        break;
      case ADD_OPEN_BRACKET :
        addOpeningBracket (expressionStack);
        break;
      case ADD_TERNARY_IF_OPEN_BRACKET :
        addTernaryIf (expressionStack);
        addOpeningBracket (expressionStack);
        break;
      case ADD_MULT_OPEN_BRACKET :
        addMultiply (expressionStack);
        addOpeningBracket (expressionStack);
        break;
      case CHECK_CLOSE_BRACKET :
        checkClosingBracket (definitionIndex, expressionStack);
        break;
      case CHECK_OPEN_CLOSE_BRACKETS :
        checkOpeningClosingBrackets (definitionIndex, expressionStack);
        break;
      case CHECK_PARAMETER_SEPARATOR :
        checkParameterSeparator (definitionIndex, expressionStack);
        break;
      case ADD_UNARY_OP :
        addUnaryOperator (lexical, expressionStack);
        break;
      case ADD_MULT_UNARY_OP :
        addMultiply (expressionStack);
        addUnaryOperator (lexical, expressionStack);
        break;
      case ADD_TERNARY_IF_UNARY_OP :
        addTernaryIf (expressionStack);
        addUnaryOperator (lexical, expressionStack);
        break;
      case ADD_IF :
        addIf (expressionStack);
        break;
      case CHECK_THEN :
        checkThen (definitionIndex, expressionStack);
        break;
      case CHECK_ELSE :
        checkElse (definitionIndex, expressionStack);
        break;
      case CHECK_END :
        checkEnd (definitionIndex, expressionStack);
        break;
    }
  }

  private final static int  PRIORITY_USER_FUNCTION     = -6;
  private final static int  PRIORITY_OPENING_BRACKET   = -5;
  private final static int  PRIORITY_ELSE              = -4;
  private final static int  PRIORITY_THEN              = -3;
  private final static int  PRIORITY_IF                = -2;
  private final static int  PRIORITY_TERNARY_IF        = -1;
  private final static int  PRIORITY_COMMON_FUNCTION   = Integer.MAX_VALUE - 2;
  private final static int  PRIORITY_CLOSING_BRACKET   = Integer.MAX_VALUE - 1;
  private final static int  PRIORITY_OPERAND           = Integer.MAX_VALUE;

  private void addBinaryOperator (Lexical binaryOperatorLexical,
                                  Stack   expressionStack)
  {
    Object binaryOperatorKey = binaryOperatorLexical.getValue () != null
                                 ? binaryOperatorLexical.getValue ()
                                 : syntax.getBinaryOperatorKey (binaryOperatorLexical.getExtractedString ());
    addBinaryOperator (binaryOperatorKey, expressionStack);
  }

  private void addBinaryOperator (Object  binaryOperatorKey,
                                  Stack   expressionStack)
  {
    int binaryOperatorPriority = syntax.getBinaryOperatorPriority (binaryOperatorKey);

    // Stack can't be empty : the element at top is the first operand
    while (   expressionStack.size () > 1
           && ((ElemFct)expressionStack.elementAt (expressionStack.size () - 2)).priority >= binaryOperatorPriority)
      expressionStack.pop ();

    ElemFct stackTop = (ElemFct)expressionStack.peek ();
    BinaryOperatorNode binaryOperatorNode = new BinaryOperatorNode (binaryOperatorKey);
    binaryOperatorNode.addParameter (stackTop.expression);
    stackTop.priority   = binaryOperatorPriority;
    stackTop.expression = binaryOperatorNode;
  }

  private void addLiteral (Lexical   literalLexical,
                           Stack     expressionStack)
  {
    ExpressionNode node = new LiteralNode (literalLexical.getValue ());
    expressionStack.push (new ElemFct (PRIORITY_OPERAND, node));
  }

  private void addConstant (Lexical constantLexical,
                            Stack   expressionStack)
  {
    ExpressionNode node = new ConstantNode (constantLexical.getValue ());
    expressionStack.push (new ElemFct (PRIORITY_OPERAND, node));
  }

  private void addParameter (int     definitionIndex,
                             Lexical parameterLexical,
                             Stack   expressionStack,
                             Object  parserData) throws CompilationException
  {
    ExpressionNode node = getParameterNode (parameterLexical, parserData);
    if (node != null)
      expressionStack.push (new ElemFct (PRIORITY_OPERAND, node));
    else
      throw new CompilationException (CompilationException.UNKOWN_IDENTIFIER, definitionIndex,
                                      parameterLexical.getExtractedString ());
  }

  /**
   * Returns the expression node matching the extracted lexical <code>parameterLexical</code>.
   * @param  parameterLexical the lexical extracted as a parameter.
   * @param  parserData       optional user data.
   * @return an expression node or <code>null</code> if <code>parameter</code> is unknown.
   *         This shouldn't happen unless <code>getLexical ()</code> was overriden to return
   *         <code>LEXICAL_PARAMETER</code> for new cases.
   * @see    #getLexical
   */
  protected ExpressionNode getParameterNode (Lexical parameterLexical,
                                             Object  parserData)
  {
    if (parserData instanceof CompiledFunction)
      return new FunctionParameterNode (((Integer)parameterLexical.getValue ()).intValue());

    return null;
  }

  private void addCommonFunction (Lexical commonFunctionLexical,
                                  Stack   expressionStack)
  {
    ExpressionNode node = new CommonFunctionNode (commonFunctionLexical.getValue ());
    expressionStack.push (new ElemFct (PRIORITY_COMMON_FUNCTION, node));
  }

  // Called when an opening bracket not following a function name is encountered
  private void addOpeningBracket (Stack   expressionStack)
  {
    expressionStack.push (new ElemFct (PRIORITY_OPENING_BRACKET, null));
  }

  private void addUnaryOperator (Lexical unaryOperatorLexical,
                                 Stack   expressionStack)
  {
    Object unaryOperatorKey = unaryOperatorLexical.getValue () != null
                                ? unaryOperatorLexical.getValue ()
                                : syntax.getUnaryOperatorKey (unaryOperatorLexical.getExtractedString ());
    ExpressionNode node = new UnaryOperatorNode (unaryOperatorKey);
    expressionStack.push (new ElemFct (PRIORITY_COMMON_FUNCTION, node));
  }

  private void addMultiply (Stack expressionStack)
  {
    // v1.0.2 : Add multiply only if stack top isn't a binary operator
    if (!(expressionStack.peek () instanceof BinaryOperatorNode))
      addBinaryOperator (Syntax.OPERATOR_MULTIPLY, expressionStack);
  }

  private void addFunction (Lexical functionLexical,
                            Stack   expressionStack,
                            Object  parserData)
  {
    Function calledFunction = (Function)functionLexical.getValue ();
    // If calledFunction equals parserData, it is a recursive call
    ExpressionNode expression = new FunctionNode (calledFunction,
                                                  calledFunction == parserData);
    expressionStack.push (new ElemFct (PRIORITY_USER_FUNCTION, expression));
  }

  // Called when a closing bracket is encountered after a parameter or a literal
  private void checkClosingBracket (int    definitionIndex,
                                    Stack  expressionStack) throws CompilationException
  {
    ElemFct stackTop = null;

    for ( ;    !expressionStack.empty ()
            && (stackTop = (ElemFct)expressionStack.peek ()).priority > PRIORITY_OPENING_BRACKET;
            expressionStack.pop ())
      if (stackTop.priority == PRIORITY_IF)
        throw new CompilationException (CompilationException.THEN_OPERATOR_EXPECTED, definitionIndex);
      else if (stackTop.priority == PRIORITY_THEN)
             throw new CompilationException (CompilationException.ELSE_OPERATOR_EXPECTED, definitionIndex);

    if (expressionStack.empty ())
      throw new CompilationException (CompilationException.CLOSING_BRACKET_WITHOUT_OPENING_BRACKET, definitionIndex);

    if (stackTop.priority == PRIORITY_USER_FUNCTION)
    {
      FunctionNode functionNode = (FunctionNode)stackTop.expression;
      if (!functionNode.getFunction ().isValidParameterCount (functionNode.getParameterCount ()))
        throw new CompilationException (CompilationException.MISSING_PARAMETERS_IN_FUNCTION_CALL, definitionIndex);
    }

    stackTop.priority = PRIORITY_CLOSING_BRACKET;
  }

  // Called when a sequence () is encountered to check if a user function may be called without parameter
  private void checkOpeningClosingBrackets (int   definitionIndex,
                                            Stack expressionStack) throws CompilationException
  {
    ElemFct stackTop = (ElemFct)expressionStack.peek ();

    if (stackTop.priority != PRIORITY_USER_FUNCTION)
      throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);

    // Check if user function may be called without parameters
    FunctionNode functionNode = (FunctionNode)stackTop.expression;
    if (!functionNode.getFunction ().isValidParameterCount (functionNode.getParameterCount ()))
      throw new CompilationException (CompilationException.MISSING_PARAMETERS_IN_FUNCTION_CALL, definitionIndex);

    stackTop.priority = PRIORITY_CLOSING_BRACKET;
  }

  private void checkParameterSeparator (int   definitionIndex,
                                        Stack expressionStack) throws CompilationException
  {
    ElemFct stackTop = null;

    for ( ;    !expressionStack.empty ()
            && (stackTop = (ElemFct)expressionStack.peek ()).priority != PRIORITY_USER_FUNCTION;
            expressionStack.pop ())
      if (stackTop.priority == PRIORITY_OPENING_BRACKET)
        throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);
      else if (stackTop.priority == PRIORITY_IF)
             throw new CompilationException (CompilationException.THEN_OPERATOR_EXPECTED, definitionIndex);
           else if (stackTop.priority == PRIORITY_THEN)
                  throw new CompilationException (CompilationException.ELSE_OPERATOR_EXPECTED, definitionIndex);

    if (expressionStack.empty ())
      throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);

    FunctionNode functionNode = (FunctionNode)stackTop.expression;
    if (   functionNode.getFunction () instanceof CompiledFunction
        && ((CompiledFunction)functionNode.getFunction ()).getParameterCount() <= functionNode.getParameterCount ())
      throw new CompilationException (CompilationException.CLOSING_BRACKET_EXPECTED, definitionIndex);
  }

  private void addIf (Stack expressionStack)
  {
    expressionStack.push (new ElemFct (PRIORITY_IF, new ConditionNode ()));
  }

  private void checkThen (int   definitionIndex,
                          Stack expressionStack) throws CompilationException
  {
    ElemFct stackTop = null;

    for ( ;    !expressionStack.empty ()
            && (stackTop = (ElemFct)expressionStack.peek ()).priority != PRIORITY_IF
            && stackTop.priority != PRIORITY_TERNARY_IF;
            expressionStack.pop ())
      if (stackTop.priority <= PRIORITY_OPENING_BRACKET)
        throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);
      else if (stackTop.priority == PRIORITY_THEN)
             throw new CompilationException (CompilationException.ELSE_OPERATOR_EXPECTED, definitionIndex);

    if (expressionStack.empty ())
      throw new CompilationException (CompilationException.THEN_OPERATOR_WITHOUT_IF_OPERATOR, definitionIndex);

    if (stackTop.priority == PRIORITY_TERNARY_IF)
    {
      // If a ternary if is in the stack, keep it in the final expression
      ConditionNode ifThenElseNode = new ConditionNode ();
      ifThenElseNode.addParameter (stackTop.expression);
      stackTop.expression = ifThenElseNode;
    }

    stackTop.priority = PRIORITY_THEN;
  }

  private void checkElse (int   definitionIndex,
                          Stack expressionStack) throws CompilationException
  {
    ElemFct stackTop = null;

    for ( ;    !expressionStack.empty ()
            && (stackTop = (ElemFct)expressionStack.peek ()).priority != PRIORITY_THEN;
            expressionStack.pop ())
      if (stackTop.priority <= PRIORITY_OPENING_BRACKET)
        throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);
      else if (stackTop.priority == PRIORITY_IF)
             throw new CompilationException (CompilationException.THEN_OPERATOR_EXPECTED, definitionIndex);

    if (expressionStack.empty ())
      throw new CompilationException (CompilationException.ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS, definitionIndex);

    stackTop.priority = PRIORITY_ELSE;
  }

  private void checkEnd (int   definitionIndex,
                         Stack expressionStack) throws CompilationException
  {
    ElemFct stackTop = null;

    for ( ;    !expressionStack.empty ()
            && (stackTop = (ElemFct)expressionStack.peek ()).priority > PRIORITY_OPENING_BRACKET;
            expressionStack.pop ())
      if (stackTop.priority == PRIORITY_IF)
        throw new CompilationException (CompilationException.THEN_OPERATOR_EXPECTED, definitionIndex);
      else if (stackTop.priority == PRIORITY_THEN)
            throw new CompilationException (CompilationException.ELSE_OPERATOR_EXPECTED, definitionIndex);

    if (!expressionStack.empty ())
      throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);

    // Push back the expression result to retrieve it in the parseExpression () method
    expressionStack.push (stackTop);
  }

  private void addTernaryIf (Stack expressionStack)
  {
    // Add to stack ternary if only then else is (? :) used by syntax
    if (syntax.getConditionPartCount () == 2)
      expressionStack.push (new ElemFct (PRIORITY_TERNARY_IF, null));
  }

  /**
   * Elements stored in the stack.
   */
  private static class ElemFct
  {
    int            priority;
    ExpressionNode expression;

    public ElemFct (int priority, ExpressionNode expression)
    {
      this.priority   = priority;
      this.expression = expression;
    }
  }

  /**
   * Stack storing the elements of the expression of the parsed function.
   */
  private static class StackFct extends Stack
  {
    public Object pop ()
    {
      ElemFct topElement = (ElemFct)super.pop ();
      if (   !empty ()
          && topElement.expression != null)
      {
        ElemFct element = (ElemFct)peek ();
        if (element.expression instanceof ParameterizedNode)
          ((ParameterizedNode)element.expression).addParameter (topElement.expression);
        else
          element.expression = topElement.expression;
      }
      return topElement;
    }
  }
}
