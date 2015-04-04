/*
 * @(#)CalculatorParser.java   10/23/2000
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

/**
 * Parser able to compute constant expressions operating unary and binary operators
 * and common functions on numbers. This is the kind of expressions often managed in calculators.<br>
 * The two <code>computeExpression ()</code> methods of this class allow to parse
 * and compute the value of an expression :
 * <UL><LI><code>double computeExpression (String expressionDefinition)</code> is the easiest
 *         method to compute values of type <code>double</code>.</LI>
 *     <LI><code>Object computeExpression (String expressionDefinition, Interpreter interpreter)</code>
 *         allows to choose the interpreter with which the numbers and the operators and common functions of
 *         <code>Syntax</code> will be computed.</LI></UL>
 * A parser is associated to a syntax, instance of the <code>Syntax</code> interface that
 * describes the different lexicals used by the parser and their type.<br>
 * The expression may use the following syntactic elements :
 * <UL><LI>literals (numbers) accepted by the <code>getLiteral ()</code> method of the syntax</LI>
 *     <LI>unary operators(- + ...) accepted by the <code>getUnaryOperatorKey ()</code> method of the syntax</LI>
 *     <LI>binary operators (- + / ...) accepted by the <code>getBinaryOperatorKey ()</code> method of the syntax
 *         and whose priority is returned by the <code>getBinaryOperatorPriority ()</code> method</LI>
 *     <LI>common functions (log sin ...) accepted by the <code>getCommonFunctionKey ()</code> method of the syntax.</LI></LI></UL>
 * The binary operators recognized by this parser are infixed, unary operators and common functions
 * are prefixed. Some parts of the expression may be bracketed between the characters
 * returned by <code>getOpeningBracket ()</code> and <code>getClosingBracket ()</code> methods.
 * White spaces returned by the <code>getWhiteSpaceCharacters ()</code> method
 * of the syntax may be used anywhere in the expression.
 *
 * <P>Here are a few examples of expressions that can be computed by this parser :
 * <BLOCKQUOTE><PRE> // Create a parser with the default syntax <code>DefaultSyntax</code>
 * CalculatorParser parser = new CalculatorParser ();
 *
 * // Compute expressions with default interpreter <code>DoubleInterpreter</code>
 * double doubleResult1 = parser.<b>computeExpression</b> ("3 + 4 * 5");
 * double doubleResult2 = parser.<b>computeExpression</b> ("(1 + 2 + 3 + 4) * 5");
 * double doubleResult3 = parser.<b>computeExpression</b> ("sin (0.2) ^ 2");
 * double doubleResult4 = parser.<b>computeExpression</b> ("cos (0.2 + 3.1E-1) / 2");
 * // This last instruction will throw a CompilationException
 * // because the expression to parse is invalid
 * double doubleResult5 = parser.<b>computeExpression</b> ("cos (3))");</PRE></BLOCKQUOTE>
 *
 * Only the following methods of <code>Syntax</code> are used by this parser :
 * <UL><LI><code>Object getLiteral (String expression, StringBuffer extractedLiteral)</code></LI>
 *     <LI><code>Object getUnaryOperatorKey (String unaryOperator)</code></LI>
 *     <LI><code>Object getBinaryOperatorKey (String binaryOperator)</code></LI>
 *     <LI><code>Object getCommonFunctionKey (String commonFunction)</code></LI>
 *     <LI><code>int getBinaryOperatorPriority (Object binaryOperatorKey)</code></LI>
 *     <LI><code>String getWhiteSpaceCharacters ()</code></LI>
 *     <LI><code>char getOpeningBracket ()</code></LI>
 *     <LI><code>char getClosingBracket ()</code></LI>
 *     <LI><code>String getDelimiters ()</code></LI>
 *     <LI><code>boolean isShortSyntax ()</code></LI></UL>
 * Short cuts of a syntax supports only calls to common function without brackets
 * around their parameter.<br>
 * The methods of this class are thread safe.
 *
 * @version   1.0.2
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.Syntax
 * @see       com.eteks.parser.Interpreter
 * @see       com.eteks.tools.calculator.JeksCalculator
 */
public class CalculatorParser extends Parser
{
  // Default Syntax and Interpreter aren't assigned at static initialization time
  // to avoid to load automatically the classes DefaultSyntax and DoubleInterpreter
  private static Syntax defaultSyntax;
  private static Interpreter defaultInterpreter;

  /**
   * The syntax used by the parser.
   */
  private Syntax syntax;

  /**
   * Creates a calculator's parser whose syntax is an instance of <code>DefaultSyntax</code>.
   */
  public CalculatorParser ()
  {
    if (defaultSyntax == null)
      defaultSyntax = new DefaultSyntax ();

    this.syntax = defaultSyntax;
  }

  /**
   * Creates a calculator's parser with the syntax <code>syntax</code>.
   * @param syntax syntax of the parser.
   */
  public CalculatorParser (Syntax syntax)
  {
    this.syntax = syntax;
  }

  /**
   * Returns the syntax used by this parser.
   * @return the parser's syntax
   */
  public Syntax getSyntax ()
  {
    return syntax;
  }

  /**
   * Returns the result of the constant expression <code>expressionDefinition</code>.
   * <code>expressionDefinition</code> may contain numbers, unary and binary operators,
   * common functions and brackets, according to the syntax of this parser.<br>
   * An interpreter of <code>DoubleInterpreter</code> class is used internally to compute
   * values of type <code>Double</code> and then return a value of type <code>double</code>.
   * @param  expressionDefinition  the constant expression to compute.
   * @return the result of the expression.
   * @throws CompilationException if the syntax of the expression is incorrect or if some
   *         elements (numbers, operators, functions,...) of the expression are not
   *         accepted by the syntax of the parser.
   * @see com.eteks.parser.DoubleInterpreter
   * @see #computeExpression(String, Interpreter)
   */
  public double computeExpression (String expressionDefinition) throws CompilationException
  {
    if (defaultInterpreter == null)
      defaultInterpreter = new DoubleInterpreter ();

    Object result = computeExpression (expressionDefinition, defaultInterpreter);
    return ((Double)result).doubleValue ();
  }

  /**
   * Returns the result of the constant expression <code>expressionDefinition</code>.
   * <code>expressionDefinition</code> may contain numbers, unary and binary operators,
   * common functionss and brackets, according to the syntax of this parser.
   * @param  expressionDefinition  the constant expression to compute.
   * @param  interpreter           the runtime interpreter used to compute the operations of <code>Syntax</code>.
   * @return the result of the expression. The returned object may be of any
   *         class (<code>Double</code>, <code>String</code>, ...) according to
   *         <code>interpreter</code> implementation.
   * @throws CompilationException if the syntax of the expression is incorrect or if some
   *         elements (literals, constants, operators, functions,...) of the expression are not
   *         accepted by the syntax of the parser.
   */
  public Object computeExpression (String      expressionDefinition,
                                   Interpreter interpreter) throws CompilationException
  {
    ElemCal elem = (ElemCal)parseExpression (expressionDefinition, 0,
                                             new StackCal (interpreter), null);
    return elem.operatorKeyOrOperand;
  }

  /**
   * Returns the lexical found in the definition <code>expressionDefinition</code>
   * beginning at the character index <code>definitionIndex</code>.
   * @param expressionDefinition the string to parse.
   * @param definitionIndex      the current index of parsing.
   * @param parserData           extra information not used.
   * @return a lexical with a code among <code>LEXICAL_VOID</code>, <code>LEXICAL_WHITE_SPACE</code>,
   *         <code>LEXICAL_LITERAL</code>, <code>LEXICAL_UNARY_OPERATOR</code>,
   *         <code>LEXICAL_BINARY_OPERATOR</code>, <code>LEXICAL_COMMON_FUNCTION</code>,
   *         <code>LEXICAL_OPENING_BRACKET</code>, <code>LEXICAL_CLOSING_BRACKET</code>,
   *         <code>LEXICAL_SYNONYMOUS_OPERATOR</code>.
   * @throws CompilationException if this method doesn't recognize any calculator's lexical at index <code>definitionIndex</code>.
   */
  protected Lexical getLexical (String expressionDefinition,
                                int    definitionIndex,
                                Object parserData) throws CompilationException
  {
    if (definitionIndex >= expressionDefinition.length ())
      return new Lexical (LEXICAL_VOID, "", null);

    // Search for white spaces
    StringBuffer extractedLexical = new StringBuffer ();
    while (   definitionIndex < expressionDefinition.length ()
           && syntax.getWhiteSpaceCharacters () != null
           && syntax.getWhiteSpaceCharacters ().indexOf (expressionDefinition.charAt (definitionIndex)) != -1)
      extractedLexical.append (expressionDefinition.charAt (definitionIndex++));

    if (extractedLexical.length () > 0)
      return new Lexical (LEXICAL_WHITE_SPACE, extractedLexical.toString (), null);

    char c = expressionDefinition.charAt (definitionIndex);
    if (c == syntax.getOpeningBracket ())
      return new Lexical (LEXICAL_OPENING_BRACKET, String.valueOf (c), null);
    if (c == syntax.getClosingBracket ())
      return new Lexical (LEXICAL_CLOSING_BRACKET, String.valueOf (c), null);

    String subExpression = expressionDefinition.substring (definitionIndex);
    // v1.0.1 Moved getLiteral () to the end to give an higher priority for operators

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
            || syntax.getBinaryOperatorKey (operator) != null)
          token = operator; // Don't break the loop now, some operators may be longer
                            // and start with the same characters
      }

      if (token == null)
        throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);
    }

    // Search if token is a function or an unary or binary operator
    Object key = syntax.getCommonFunctionKey (token);
    if (key != null)
      return new Lexical (LEXICAL_COMMON_FUNCTION, token, key);

    key = syntax.getUnaryOperatorKey (token);
    if (key != null)
      if (syntax.getBinaryOperatorKey (token) != null)
        return new Lexical (LEXICAL_SYNONYMOUS_OPERATOR, token, null);
      else
        return new Lexical (LEXICAL_UNARY_OPERATOR, token, key);

    key = syntax.getBinaryOperatorKey (token);
    if (key != null)
      return new Lexical (LEXICAL_BINARY_OPERATOR, token, key);

    // v1.0.1 Moved getLiteral () to the end to give an higher priority for operators
    Object lexicalValue = syntax.getLiteral (subExpression, extractedLexical);
    if (lexicalValue != null)
      return new Lexical (LEXICAL_LITERAL, extractedLexical.toString (), lexicalValue);
      
    throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex, token);
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
   * Lexical code returned by <code>getLexical ()</code> for a literal number.
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
   * Returns the node bound to <code>graphNode</code> with the link <code>lexical</code>
   * in the syntactic graph of the calculator.<br>
   * <code>lexical</code> and <code>expressionStack</code> are used to compute the current parsed expression.
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

    // Execute the transition that computes the expression
    executeTransition (transitionGraph [lexical.getCode ()][graphNode], definitionIndex,
                       lexical, expressionStack);

    return boundNode == N_END
             ? Parser.NODE_END
             : boundNode;
  }

  // Constants of nodes in syntatic graph
  private final static byte N_START  = NODE_START; // Node start
  private final static byte N_CL_BR  = 1;          // Node closing bracket or operand
  private final static byte N_C_FCT  = 2;          // Node common function
  private final static byte N_END    = 3;          // Node end

  // Constants stored in the array transitionGraph
  // used in the executeTransition () method to switch to a transition
  private final static byte ADD_BINARY_OP       = 1;
  private final static byte ADD_LITERAL         = 2;
  private final static byte ADD_COMMON_FCT      = 3;
  private final static byte ADD_OPEN_BRACKET    = 4;
  private final static byte CHECK_CLOSE_BRACKET = 5;
  private final static byte ADD_UNARY_OP        = 6;
  private final static byte CHECK_END           = 7;

  // This syntax authorize to write a parameter directly behind
  // a common function
  private final static byte shortSyntaxGraph [][] =
   // N_START  N_CL_BR  N_C_FCT
    {{-1,      N_END,   -1     },   // LEXICAL_VOID
     {N_START, N_CL_BR, N_START},   // LEXICAL_WHITE_SPACE
     {N_CL_BR, -1,      -1     },   // LEXICAL_LITERAL
     {N_START, -1,      N_START},   // LEXICAL_UNARY_OPERATOR
     {-1,      N_START, -1     },   // LEXICAL_BINARY_OPERATOR
     {N_C_FCT, -1,      -1     },   // LEXICAL_COMMON_FUNCTION
     {N_START, -1,      N_START},   // LEXICAL_OPENING_BRACKET
     {-1,      N_CL_BR, -1,    },   // LEXICAL_CLOSING_BRACKET
     {N_START, N_START, N_START}};  // LEXICAL_SYNONYMOUS_OPERATOR

  private final static byte syntaxGraph [][] =
   // N_START  N_CL_BR  N_C_FCT
    {{-1,      N_END,   -1     },   // LEXICAL_VOID
     {N_START, N_CL_BR, N_C_FCT},   // LEXICAL_WHITE_SPACE
     {N_CL_BR, -1,      -1     },   // LEXICAL_LITERAL
     {N_START, -1,      -1     },   // LEXICAL_UNARY_OPERATOR
     {-1,      N_START, -1     },   // LEXICAL_BINARY_OPERATOR
     {N_C_FCT, -1,      -1     },   // LEXICAL_COMMON_FUNCTION
     {N_START, -1,      N_START},   // LEXICAL_OPENING_BRACKET
     {-1,      N_CL_BR, -1,    },   // LEXICAL_CLOSING_BRACKET
     {N_START, N_START, -1     }};  // LEXICAL_SYNONYMOUS_OPERATOR

  private final static byte transitionGraph [][] =
   // N_START           N_CL_BR              N_C_FCT
    {{0,                CHECK_END,           0               },
     {0,                0,                   0               }, // Nothing to do
     {ADD_LITERAL,      0,                   0               },
     {ADD_UNARY_OP,     0,                   ADD_UNARY_OP    }, // v1.0.2 : Added ADD_UNARY_OP in N_C_FCT column
     {0,                ADD_BINARY_OP,       0               },
     {ADD_COMMON_FCT,   0,                   0               },
     {ADD_OPEN_BRACKET, 0,                   ADD_OPEN_BRACKET},
     {0,                CHECK_CLOSE_BRACKET, 0               },
     {ADD_UNARY_OP,     ADD_BINARY_OP,       ADD_UNARY_OP    }};

  private void executeTransition (byte    transition,
                                  int     definitionIndex,
                                  Lexical lexical,
                                  Stack   expressionStack) throws CompilationException
  {
    switch (transition)
    {
      case ADD_BINARY_OP :
        addBinaryOperator (lexical, expressionStack);
        break;
      case ADD_LITERAL :
        addLiteral (lexical, expressionStack);
        break;
      case ADD_COMMON_FCT :
        addCommonFunction  (lexical, expressionStack);
        break;
      case ADD_OPEN_BRACKET :
        addOpeningBracket (expressionStack);
        break;
      case CHECK_CLOSE_BRACKET :
        checkClosingBracket (definitionIndex, expressionStack);
        break;
      case ADD_UNARY_OP :
        addUnaryOperator (lexical, expressionStack);
        break;
      case CHECK_END :
        checkEnd (definitionIndex, expressionStack);
        break;
    }
  }

  private final static int  PRIORITY_OPENING_BRACKET   = -1;
  private final static int  PRIORITY_COMMON_FUNCTION   = Integer.MAX_VALUE - 2;
  private final static int  PRIORITY_CLOSING_BRACKET   = Integer.MAX_VALUE - 1;
  private final static int  PRIORITY_OPERAND           = Integer.MAX_VALUE;

  private void addBinaryOperator (Lexical binaryOperatorLexical,
                                  Stack   expressionStack)
  {
    Object binaryOperatorKey = binaryOperatorLexical.getValue () != null
                                 ? binaryOperatorLexical.getValue ()
                                 : syntax.getBinaryOperatorKey (binaryOperatorLexical.getExtractedString ());
    int    binaryOperatorPriority = syntax.getBinaryOperatorPriority (binaryOperatorKey);

    // Stack can't be empty : the element at top is the first operand
    while (   expressionStack.size () > 1
           && ((ElemCal)expressionStack.elementAt (expressionStack.size () - 2)).priority >= binaryOperatorPriority)
      expressionStack.pop ();

    ElemCal stackTop = (ElemCal)expressionStack.peek ();
    stackTop.priority   = binaryOperatorPriority;
    stackTop.operatorType = LEXICAL_BINARY_OPERATOR;
    stackTop.binaryOperatorFirstOperand = stackTop.operatorKeyOrOperand;
    stackTop.operatorKeyOrOperand       = binaryOperatorKey;
  }

  private void addLiteral (Lexical   literalLexical,
                           Stack     expressionStack)
  {
    expressionStack.push (new ElemCal (PRIORITY_OPERAND, LEXICAL_LITERAL,
                                       literalLexical.getValue ()));
  }

  private void addCommonFunction (Lexical commonFunctionLexical,
                                  Stack   expressionStack)
  {
    expressionStack.push (new ElemCal (PRIORITY_COMMON_FUNCTION, LEXICAL_COMMON_FUNCTION,
                                       commonFunctionLexical.getValue ()));
  }

  // Called when an opening bracket not following a function name is encountered
  private void addOpeningBracket (Stack  expressionStack)
  {
    expressionStack.push (new ElemCal (PRIORITY_OPENING_BRACKET, 0, null));
  }

  // Called when a closing bracket is encountered after a parameter or a literal in a function call or not
  private void checkClosingBracket (int    definitionIndex,
                                    Stack  expressionStack) throws CompilationException
  {
    while (   !expressionStack.empty ()
           && ((ElemCal)expressionStack.peek ()).priority != PRIORITY_OPENING_BRACKET)
      expressionStack.pop ();

    if (expressionStack.empty ())
      throw new CompilationException (CompilationException.CLOSING_BRACKET_WITHOUT_OPENING_BRACKET, definitionIndex);
    ((ElemCal)expressionStack.peek ()).priority = PRIORITY_CLOSING_BRACKET;
  }

  private void addUnaryOperator (Lexical unaryOperatorLexical,
                                 Stack   expressionStack)
  {
    Object unaryOperatorKey = unaryOperatorLexical.getValue () != null
                                ? unaryOperatorLexical.getValue ()
                                : syntax.getUnaryOperatorKey (unaryOperatorLexical.getExtractedString ());
    expressionStack.push (new ElemCal (PRIORITY_COMMON_FUNCTION, LEXICAL_UNARY_OPERATOR, unaryOperatorKey));
  }

  private void checkEnd (int    definitionIndex,
                         Stack  expressionStack) throws CompilationException
  {
    ElemCal stackTop = null;
    while (   !expressionStack.empty ()
           && (stackTop = (ElemCal)expressionStack.peek ()).priority > PRIORITY_OPENING_BRACKET)
      expressionStack.pop ();

    if (!expressionStack.empty ())
      throw new CompilationException (CompilationException.SYNTAX_ERROR, definitionIndex);

    // Push back the expression result to retrieve it in the parseExpression () method
    expressionStack.push (stackTop);
  }

  /**
   * Elements stored in the stack.
   */
  private static class ElemCal
  {
    int     priority;
    int     operatorType;
    Object  binaryOperatorFirstOperand;
    Object  operatorKeyOrOperand;

    public ElemCal (int priority, int operatorType, Object operatorKeyOrOperand)
    {
      this.priority = priority;
      this.operatorType = operatorType;
      this.operatorKeyOrOperand = operatorKeyOrOperand;
    }
  }

  /**
   * Stack storing the elements of the parsed expression. The pop () method
   * computes the expression with the interpreter.
   */
  private static class StackCal extends Stack
  {
    private Interpreter interpreter;

    public StackCal (Interpreter interpreter)
    {
      this.interpreter = interpreter;
    }

    public Object pop ()
    {
      ElemCal topElement = (ElemCal)super.pop ();
      if (   !empty ()
          && topElement.operatorKeyOrOperand != null)
      {
        ElemCal element = (ElemCal)peek ();
        if (element.operatorKeyOrOperand == null)
          element.operatorKeyOrOperand = topElement.operatorKeyOrOperand;
        else
          switch (element.operatorType)
          {
            case LEXICAL_UNARY_OPERATOR :
              element.operatorKeyOrOperand = interpreter.getUnaryOperatorValue (element.operatorKeyOrOperand,
                                                                                topElement.operatorKeyOrOperand);
              break;
            case LEXICAL_BINARY_OPERATOR :
              element.operatorKeyOrOperand = interpreter.getBinaryOperatorValue (element.operatorKeyOrOperand,
                                                                                 element.binaryOperatorFirstOperand,
                                                                                 topElement.operatorKeyOrOperand);
              break;
            case LEXICAL_COMMON_FUNCTION :
              element.operatorKeyOrOperand = interpreter.getCommonFunctionValue (element.operatorKeyOrOperand,
                                                                                 topElement.operatorKeyOrOperand);
             break;
          }
      }
      return topElement;
    }

    public Object push (Object item)
    {
      ElemCal element = (ElemCal)item;
      if (element.operatorType == LEXICAL_LITERAL)
        element.operatorKeyOrOperand = interpreter.getLiteralValue (element.operatorKeyOrOperand);
      return super.push (item);
    }
  }
}
