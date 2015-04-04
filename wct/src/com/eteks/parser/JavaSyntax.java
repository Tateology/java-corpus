/*
 * @(#)JavaSyntax.java   01/06/98
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
 * Syntax used by parsers for functions and expressions written in Java. This syntax supports
 * the Java unary and binary operators and the static methods of the <code>java.lang.Math</code> class.
 * The multi parameters methods of <code>java.lang.Math</code> are implemented
 * as Java functions implementing the <code>Function</code> interface and added to the syntax.<br>
 * All the following operators, constants and functions are supported :
 * <UL><LI>Unary operators : <code>+ - ! ~</code></LI>
 *     <LI>Binary operators : <code>* / % + - << >> >>> < <= > >= == != & ^ | && ||</code></LI>
 *     <LI>Condition : <code>? :</code></LI>
 *     <LI>Constants : <code>Math.PI Math.E</code>. If the instance of <code>JavaSyntax</code>
 *        is created with its parameter <code>extendedSyntax</code> equal to <code>true</code>,
 *        <code>true</code> and <code>false</code> are also supported.</LI>
 *     <LI>Functions :
 *         <UL><LI><code>Math.log</code></LI>
 *             <LI><code>Math.exp</code></LI>
 *             <LI><code>Math.sqrt</code></LI>
 *             <LI><code>Math.cos</code></LI>
 *             <LI><code>Math.sin</code></LI>
 *             <LI><code>Math.tan</code></LI>
 *             <LI><code>Math.acos</code></LI>
 *             <LI><code>Math.asin</code></LI>
 *             <LI><code>Math.atan</code></LI>
 *             <LI><code>Math.floor</code></LI>
 *             <LI><code>Math.ceil</code></LI>
 *             <LI><code>Math.round</code></LI>
 *             <LI><code>Math.rint</code></LI>
 *             <LI><code>Math.abs</code></LI>
 *             <LI><code>Math.atan2</code></LI>
 *             <LI><code>Math.IEEEremainder</code></LI>
 *             <LI><code>Math.max</code></LI>
 *             <LI><code>Math.min</code></LI>
 *             <LI><code>Math.pow</code></LI>
 *             <LI><code>Math.random</code></LI></UL></UL>
 * The operators priorities are the same as in Java.<br>
 * The operator for assignment is <code>=</code>, brackets <code>( )</code> and
 * the separator of parameters <code>,</code>.<br>
 * All the following operators are not supported :
 * <UL><LI>Primary operators : <code>. [ ] new</code></LI>
 *     <LI>Unary operators : <code>++ --</code> and casts</LI>
 *     <LI>Binary operators : <code>instanceof</code></LI>
 *     <LI>Affectation operators : only <code>=</code> returned by <code>getAssignmentOperator ()</code>
 *         is used.</LI></UL>
 * The literals may be any Java literal number.
 * If <code>extendedSyntax</code> is equal to <code>true</code>, quoted characters and strings
 * are also supported as literals.<br>
 * The parsed identifiers (function name and its parameters) may be any Java identifier containing
 * letters, digits, the characters $, _ <b>or</b> . (dots). The first character of an identifier must
 * be a letter, the characters $ or _ (not a dot).
 * No type checking is done during the parsing of functions and expressions but using a
 * <code>WrapperInterpreter</code> interpreter enables to fill this lack at runtime.
 *
 * @version   1.0.2
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.FunctionParser
 * @see       com.eteks.parser.WrapperInterpreter
 */
public class JavaSyntax extends AbstractSyntax
{
  private boolean extendedSyntax;

  /**
   * Creates a Java syntax, that doesn't use an extended syntax (no boolean constants
   * and strings).
   */
  public JavaSyntax ()
  {
    this (false);
  }

  /**
   * Creates a Java syntax.
   * @param extendedSyntax if equal to <code>true</code>, this syntax supports the
   *        boolean constants <code>true</code> and <code>false</code> and quoted
   *        strings and characters.
   */
  public JavaSyntax (boolean extendedSyntax)
  {
    super (true);

    this.extendedSyntax = extendedSyntax;
    setConstantKey ("Math.PI", CONSTANT_PI);
    setConstantKey ("Math.E",  CONSTANT_E);
    if (extendedSyntax)
    {
      setConstantKey ("true",  CONSTANT_TRUE);
      setConstantKey ("false", CONSTANT_FALSE);
    }

    setUnaryOperatorKey ("+", OPERATOR_POSITIVE);
    setUnaryOperatorKey ("-", OPERATOR_OPPOSITE);
    setUnaryOperatorKey ("!", OPERATOR_LOGICAL_NOT);
    setUnaryOperatorKey ("~", OPERATOR_BITWISE_NOT);

    setBinaryOperatorKey ("+",   OPERATOR_ADD);
    setBinaryOperatorKey ("-",   OPERATOR_SUBSTRACT);
    setBinaryOperatorKey ("*",   OPERATOR_MULTIPLY);
    setBinaryOperatorKey ("/",   OPERATOR_DIVIDE);
    setBinaryOperatorKey ("%",   OPERATOR_REMAINDER);
    setBinaryOperatorKey ("==",  OPERATOR_EQUAL);
    setBinaryOperatorKey ("!=",  OPERATOR_DIFFERENT);
    setBinaryOperatorKey (">=",  OPERATOR_GREATER_OR_EQUAL);
    setBinaryOperatorKey ("<=",  OPERATOR_LESS_OR_EQUAL);
    setBinaryOperatorKey (">",   OPERATOR_GREATER);
    setBinaryOperatorKey ("<",   OPERATOR_LESS);
    setBinaryOperatorKey ("||",  OPERATOR_LOGICAL_OR);
    setBinaryOperatorKey ("&&",  OPERATOR_LOGICAL_AND);
    setBinaryOperatorKey ("|",   OPERATOR_BITWISE_OR);
    setBinaryOperatorKey ("^",   OPERATOR_BITWISE_XOR);
    setBinaryOperatorKey ("&",   OPERATOR_BITWISE_AND);
    setBinaryOperatorKey ("<<",  OPERATOR_SHIFT_LEFT);
    setBinaryOperatorKey (">>",  OPERATOR_SHIFT_RIGHT);
    setBinaryOperatorKey (">>>", OPERATOR_SHIFT_RIGHT_0);

    setConditionPartKey ("?", CONDITION_THEN);
    setConditionPartKey (":", CONDITION_ELSE);

    setCommonFunctionKey ("Math.log",   FUNCTION_LN);
    setCommonFunctionKey ("Math.exp",   FUNCTION_EXP);
    setCommonFunctionKey ("Math.sqrt",  FUNCTION_SQRT);
    setCommonFunctionKey ("Math.cos",   FUNCTION_COS);
    setCommonFunctionKey ("Math.sin",   FUNCTION_SIN );
    setCommonFunctionKey ("Math.tan",   FUNCTION_TAN);
    setCommonFunctionKey ("Math.acos",  FUNCTION_ACOS);
    setCommonFunctionKey ("Math.asin",  FUNCTION_ASIN);
    setCommonFunctionKey ("Math.atan",  FUNCTION_ATAN);
    setCommonFunctionKey ("Math.floor", FUNCTION_FLOOR);
    setCommonFunctionKey ("Math.ceil",  FUNCTION_CEIL);
    // As types are used only at the time of interpretation, Math.round () and Math.rint ()
    // can be considered as synonymous methods
    setCommonFunctionKey ("Math.round", FUNCTION_ROUND);
    setCommonFunctionKey ("Math.rint",  FUNCTION_ROUND);
    setCommonFunctionKey ("Math.abs",   FUNCTION_ABS);

    setBinaryOperatorPriority (OPERATOR_LOGICAL_OR,       1);
    setBinaryOperatorPriority (OPERATOR_LOGICAL_AND,      2);
    setBinaryOperatorPriority (OPERATOR_BITWISE_OR,       3);
    setBinaryOperatorPriority (OPERATOR_BITWISE_XOR,      4);
    setBinaryOperatorPriority (OPERATOR_BITWISE_AND,      5);
    setBinaryOperatorPriority (OPERATOR_EQUAL,            6);
    setBinaryOperatorPriority (OPERATOR_DIFFERENT,        6);
    setBinaryOperatorPriority (OPERATOR_GREATER_OR_EQUAL, 7);
    setBinaryOperatorPriority (OPERATOR_LESS_OR_EQUAL,    7);
    setBinaryOperatorPriority (OPERATOR_GREATER,          7);
    setBinaryOperatorPriority (OPERATOR_LESS,             7);
    setBinaryOperatorPriority (OPERATOR_SHIFT_LEFT,       8);
    setBinaryOperatorPriority (OPERATOR_SHIFT_RIGHT,      8);
    setBinaryOperatorPriority (OPERATOR_SHIFT_RIGHT_0,    8);
    setBinaryOperatorPriority (OPERATOR_ADD,              9);
    setBinaryOperatorPriority (OPERATOR_SUBSTRACT,        9);
    setBinaryOperatorPriority (OPERATOR_REMAINDER,        10);
    setBinaryOperatorPriority (OPERATOR_DIVIDE,           10);
    setBinaryOperatorPriority (OPERATOR_MULTIPLY,         10);

    setAssignmentOperator ("=");
    setWhiteSpaceCharacters (" \t\n\r");
    setOpeningBracket ('(');
    setClosingBracket (')');
    setParameterSeparator (',');

    // Add all the other Math methods of JDK 1.0 (not including toDegrees () and toRadians ())
    // with Java compiled functions using double types.
    addFunction (new FunctionAtan2 ());  // Math.atan2
    addFunction (new FunctionRemain ()); // Math.IEEEremainder
    addFunction (new FunctionMax ());    // Math.max
    addFunction (new FunctionMin ());    // Math.min
    addFunction (new FunctionPow ());    // Math.pow
    addFunction (new FunctionRandom ()); // Math.random
  }

  /**
   * Returns the value of the literal parsed from the string <code>expression</code>
   * or <code>null</code> if <code>expression</code> doesn't start with a literal. If a
   * literal is found at the beginning of <code>expression</code>, this method extracts
   * the parsed literal in the string buffer <code>extractedLiteral</code>.
   * The extracted literal is a number or if this instance supports an extended syntax,
   * a string or a character.
   * @param  expression       the string to parse.
   * @param  extractedLiteral the literal extracted from <code>expression</code> identified
   *                          as a valid literal with the syntax of the parser.
   * @return the value of the extracted literal or <code>null</code> if <code>expression</code>
   *         doesn't start with a valid literal. If the literal is a number,
   *         an instance of <code>Number</code> is returned, if the literal is a string
   *         an instance of <code>String</code> is returned and if the literal is a character
   *         an instance of <code>Character</code> is returned.
   * @see com.eteks.parser.AbstractSyntax#getLiteralNumber
   */
  public Object getLiteral (String       expression,
                            StringBuffer extractedLiteral)
  {
    Object literal = getLiteralNumber (expression, extractedLiteral);
    if (literal != null)
      return literal;
    else if (extendedSyntax)
      return getLiteralString (expression, extractedLiteral);
    else
      return null;
  }

  /**
   * Extracts the number from the expression <code>expression</code> in the string buffer
   * <code>extractedNumber</code>. The parsed number may be any Java literal number
   * (hexadecimal, octal or decimal number) possibly followed by a type letter d, f or l
   * (upper case or lower case).
   * @param expression      the string to parse.
   * @param extractedNumber the number extracted from <code>expression</code>.
   * @return <code>null</code> if <code>expression</code> doesn't start with a number
   *         or an instance of <code>Double</code> or <code>Long</code> according to the
   *         parsed number.
   */
  protected Number getLiteralNumber (String       expression,
                                     StringBuffer extractedNumber)
  {
    Number literal;
    char c;
    if (   expression.length () > 3
        && expression.startsWith ("0x")
        && (      (c = expression.charAt (2)) >= '0'
               && c <= '9'
            ||    c >= 'a'
               && c <= 'f'
            ||    c >= 'A'
               && c <= 'F'))
    {
      // Parse hexadecimal number
      extractedNumber.append (c);
      for (int index = 3;
              index < expression.length ()
           && (      (c = expression.charAt (index)) >= '0'
                  && c <= '9'
               ||    c >= 'a'
                  && c <= 'f'
               ||    c >= 'A'
                  && c <= 'F');
           index++)
        extractedNumber.append (c);
      literal = new Long (Long.parseLong (extractedNumber.toString ().substring (2), 16));
    }
    else if (   expression.length () > 2
             && expression.charAt (0) == '0'
             && (c = expression.charAt (1)) >= '0'
             && c <= '7')
    {
      // Parse octal number
      int index;
      for (index = 2;
              index < expression.length ()
           && (c = expression.charAt (index)) >= '0'
           && c <= '7';
           index++)
        ;

      if (   c == '.'
          || c == 'e'
          || c == 'E')
        // Double number
        literal = super.getLiteralNumber (expression, extractedNumber);
      else if (   c == '8'
               || c == '9')
        // Malformed integer of radix 10
        return null;
      else
      {
        extractedNumber.append (expression.substring (0, index));
        literal = new Long (Long.parseLong (extractedNumber.toString ().substring (1), 8));
      }
    }
    else
      literal = super.getLiteralNumber (expression, extractedNumber);

    if (   literal != null
        && expression.length () > extractedNumber.length ())
      // v1.0.2 Used >= instead of == for character 'd'
      if (   (c = expression.charAt (extractedNumber.length ())) == 'd'
          || c == 'D'
          || c == 'f'
          || c == 'F')
      {
        extractedNumber.append (c);
        if (!(literal instanceof Double))
          literal = new Double (literal.doubleValue ());
      }
      else if (   (   c == 'l'
                   || c == 'L')
               && literal instanceof Long)
        // Don't change the type just append the type letter
        extractedNumber.append (c);

    return literal;
  }

  /**
   * Extracts the quoted character or string from the expression <code>expression</code>
   * in the string buffer <code>extractedString</code>. The parsed string may be any Java literal
   * character or string possibly using escape sequence (\", \t, \u00e9,...).
   * @param expression      the string to parse.
   * @param extractedString the quoted char or string extracted from <code>expression</code>.
   * @return <code>null</code> if <code>expression</code> doesn't start with a quoted character
   *         or string or an instance of <code>Character</code> or <code>String</code> according to
   *         the parsed character or string.
   */
  protected Object getLiteralString (String       expression,
                                     StringBuffer extractedString)
  {
    if (expression.length () >= 2)
      if (expression.charAt (0) == '\'')
      {
        if (expression.length () > 2)
        {
          // Parse character
          StringBuffer buffer = new StringBuffer ();
          int c = getCharacter (expression, 1, buffer);
          if (   c != -1
              && expression.length () >= 2 + buffer.length ()
              && expression.charAt (buffer.length () + 1) == '\'')
          {
            extractedString.append ('\'');
            extractedString.append (buffer.toString ());
            extractedString.append ('\'');
            return new Character ((char)c);
          }
        }
      }
      else if (expression.charAt (0) == '\"')
      {
        // Parse string
        StringBuffer literalString = new StringBuffer ();
        StringBuffer buffer = new StringBuffer ();
        int index;
        for (index = 1;
                index < expression.length ()
             && expression.charAt (index) != '\"';
             index += buffer.length (), buffer.setLength (0))
        {
          // v1.0.1 Used 1 instead of index
          int c = getCharacter (expression, index, buffer);
          if (c == -1)
            return null;
          else
            literalString.append ((char)c);
        }
        
        if (   index < expression.length ()
               // v1.0.1 Bad comparison
            && expression.charAt (index) == '\"')
        {
          extractedString.append (expression.substring (0, index + 1));
          return literalString.toString ();
        }
      }
    return null;
  }

  protected int getCharacter (String       expression,
                              int          index,
                              StringBuffer extractedString)
  {
    char c = expression.charAt (index);
    if (c != '\\')
    {
      extractedString.append (c);
      return c;
    }
    else if (index + 1 < expression.length ())
      switch (expression.charAt (index + 1))
      {
        case 'b'  : extractedString.append ("\\b");
                    return '\b';
        case 't'  : extractedString.append ("\\t");
                    return '\t';
        case 'n'  : extractedString.append ("\\n");
                    return '\n';
        case 'f'  : extractedString.append ("\\f");
                    return '\f';
        case 'r'  : extractedString.append ("\\r");
                    return '\r';
        case '\"' : extractedString.append ("\\\"");
                    return '\"';
        case '\'' : extractedString.append ("\\\'");
                    return '\'';
        case '\\' : extractedString.append ("\\\\");
                    return '\\';
        default   :
          if (   (c = expression.charAt (index + 1)) >= '0'
              && c <= '7')
          {
            // Parse octal character
            int i = 2;
            while (   i < 4
                   && index + i < expression.length ()
                   && (c = expression.charAt (index + i)) >= '0'
                   && c <= '7')
              i++;

            if (   i < 4
                || expression.charAt (index + 1) <= '3')
            {
              // Numbers with 3 octal digits may start only with 0 1 2 or 3
              extractedString.append (expression.substring (index, index + i));
              return Integer.parseInt (expression.substring (index + 1, index + i), 8);
            }
          }
          else if (index + 5 < expression.length ())
            if (expression.charAt (index + 1) == 'u')
            {
              // Parse Unicode character
              for (int i = 2; i < 6; i++)
                if (!(     (c = expression.charAt (index + i)) >= '0'
                         && c <= '9'
                      ||    c >= 'a'
                         && c <= 'f'
                      ||    c >= 'A'
                         && c <= 'F'))
                  return -1;
              extractedString.append (expression.substring (index, index + 6));
              return Integer.parseInt (expression.substring (index + 2, index + 6), 16);
            }
      }

    return -1;
  }

  /**
   * Returns <code>true</code> if the string <code>identifier</code> is a correctly written identifier
   * usable as the name of a function or a paramater.
   * @param identifier the string to test.
   * @return the result of <code>isJavaIdentifier ()</code>.
   * @see #isJavaIdentifier
   */
  public boolean isValidIdentifier (String identifier)
  {
    return isJavaIdentifier (identifier);
  }

  /**
   * Returns <code>true</code> if the string <code>identifier</code> is a Java identifier.
   * An identifier of the syntax <code>JavaSyntax</code> may contain letters, digits,
   * the characters $, _ <b>or</b> . (dots). The first character of <code>identifier</code>
   * must be a letter, the characters $ or _ (not a dot). The dot allows to emulate static
   * fields and methods of Java.
   * @param identifier the string to test.
   * @return <code>true</code> if <code>identifier</code> is a Java identifier.
   */
  public boolean isJavaIdentifier (String identifier)
  {
    if (!Character.isJavaLetter (identifier.charAt (0)))
      return false;
    for (int i = 1; i < identifier.length (); i++)
      if (   !Character.isJavaLetterOrDigit (identifier.charAt (i))
          && identifier.charAt (i) != '.')
        return false;

    return true;
  }

  // Implementation of Java Math functions requiring more than one parameter.
  private static class FunctionAtan2 implements Function
  {
    public String getName ()
    {
      return "Math.atan2";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (   parametersValue [0] instanceof Number
          && parametersValue [1] instanceof Number)
        return new Double (Math.atan2 (((Number)parametersValue [0]).doubleValue (), ((Number)parametersValue [1]).doubleValue ()));
      else
        throw new IllegalArgumentException ("Can compute only numbers");
    }
  };

  private static class FunctionRemain implements Function
  {
    public String getName ()
    {
      return "Math.IEEEremainder";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (   parametersValue [0] instanceof Number
          && parametersValue [1] instanceof Number)
        return new Double (Math.IEEEremainder (((Number)parametersValue [0]).doubleValue (), ((Number)parametersValue [1]).doubleValue ()));
      else
        throw new IllegalArgumentException ("Can compute only numbers");
    }
  };

  private static class FunctionMax implements Function
  {
    public String getName ()
    {
      return "Math.max";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (interpreter instanceof MathMLInterpreter)
        return   "<apply>\n<max/>\n"
               + parametersValue [0]
               + parametersValue [1]
               + "</apply>\n";
      else if (   parametersValue [0] instanceof Number
               && parametersValue [1] instanceof Number)
      {
        if (   interpreter instanceof WrapperInterpreter
            && (   parametersValue [0] instanceof Integer
                || parametersValue [0] instanceof Long)
            && (   parametersValue [1] instanceof Integer
                || parametersValue [1] instanceof Long))
          return new Long (Math.max (((Number)parametersValue [0]).longValue (), ((Number)parametersValue [1]).longValue ()));
        else
          return new Double (Math.max (((Number)parametersValue [0]).doubleValue (), ((Number)parametersValue [1]).doubleValue ()));
      }
      else
        throw new IllegalArgumentException ("Can compute only numbers");
    }
  };

  private static class FunctionMin implements Function
  {
    public String getName ()
    {
      return "Math.min";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      if (interpreter instanceof MathMLInterpreter)
        return   "<apply>\n<min/>\n"
               + parametersValue [0]
               + parametersValue [1]
               + "</apply>\n";
      else if (   parametersValue [0] instanceof Number
               && parametersValue [1] instanceof Number)
      {
        if (   interpreter instanceof WrapperInterpreter
            && (   parametersValue [0] instanceof Integer
                || parametersValue [0] instanceof Long)
            && (   parametersValue [1] instanceof Integer
                || parametersValue [1] instanceof Long))
          return new Long (Math.min (((Number)parametersValue [0]).longValue (), ((Number)parametersValue [1]).longValue ()));
        else
          return new Double (Math.min (((Number)parametersValue [0]).doubleValue (), ((Number)parametersValue [1]).doubleValue ()));
      }
      else
        throw new IllegalArgumentException ("Can compute only numbers");
    }
  };

  private static class FunctionPow implements Function
  {
    public String getName ()
    {
      return "Math.pow";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 2;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      // Replace by a call to the binary operator
      return interpreter.getBinaryOperatorValue (OPERATOR_POWER, parametersValue [0], parametersValue [1]);
    }
  };

  private static class FunctionRandom implements Function
  {
    public String getName ()
    {
      return "Math.random";
    }

    public boolean isValidParameterCount (int parameterCount)
    {
      return parameterCount == 0;
    }

    public Object computeFunction (Interpreter interpreter, Object [] parametersValue)
    {
      return new Double (Math.random ());
    }
  };
}

