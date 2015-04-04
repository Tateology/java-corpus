/*
 * @(#)CompilationException.java   01/01/98
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
 * Exception thrown during the parsing of functions and expressions. This
 * class stores the number of an error matching the type of the detected error,
 * the character index in the parsed string where the error was found and
 * eventually the lexical that caused the exception.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class CompilationException extends Exception
{
  public final static int OPENING_BRACKET_EXPECTED                     =  0;
  public final static int INVALID_FUNCTION_NAME                        =  1;
  public final static int RESERVED_WORD                                =  2;
  public final static int FUNCTION_NAME_ALREADY_EXISTS                 =  3;
  public final static int CLOSING_BRACKET_EXPECTED                     =  4;
  public final static int ASSIGN_OPERATOR_EXPECTED                     =  5;
  public final static int INVALID_PARAMETER_NAME                       =  6;
  public final static int DUPLICATED_PARAMETER_NAME                    =  7;
  public final static int SYNTAX_ERROR                                 =  8;
  public final static int CLOSING_BRACKET_WITHOUT_OPENING_BRACKET      =  9;
  public final static int UNKOWN_IDENTIFIER                            = 10;
  public final static int MISSING_PARAMETERS_IN_FUNCTION_CALL          = 11;
  public final static int INVALID_PARAMETERS_COUNT_IN_FUNCTION_CALL    = 12;
  public final static int THEN_OPERATOR_EXPECTED                       = 13;
  public final static int ELSE_OPERATOR_EXPECTED                       = 14;
  public final static int THEN_OPERATOR_WITHOUT_IF_OPERATOR            = 15;
  public final static int ELSE_OPERATOR_WITHOUT_IF_THEN_OPERATORS      = 16;

  private final static String errors [] =
    {"Opening bracket expected",
     "Invalid function name",
     "Reserved word",
     "Function name already exists",
     "Closing bracket expected",
     "Assign operator expected",
     "Invalid parameter name",
     "Duplicated parameter name",
     "Syntax error",
     "Closing bracket without opening bracket",
     "Unknown indentifier",
     "Missing parameters in function call",
     "Invalid parameters count in function call",
     "Then operator expected",
     "Else operator expected",
     "Then operator without if operator",
     "Else operator without if then operator"};

  private int    errorNumber;
  private int    characterIndex;
  private String extractedString;

  /**
   * Creates a compilation exception with a message describing this exception.
   * @param errorNumber    the detected error (one of the contants of this class).
   * @param characterIndex the character index in the parsed string where the error was detected.
   */
  public CompilationException (int errorNumber, int characterIndex)
  {
    super (errors [errorNumber] + " at index " + characterIndex);
    this.errorNumber    = errorNumber;
    this.characterIndex = characterIndex;
  }

  /**
   * Creates a compilation exception with a message describing this exception.
   * @param errorNumber     the detected error (one of the contants of this class).
   * @param characterIndex  the character index in the parsed string where the error was detected.
   * @param extractedString the invalid string.
   */
  public CompilationException (int errorNumber, int characterIndex, String extractedString)
  {
    super (errors [errorNumber] + " (" + extractedString + ") at index " + characterIndex);
    this.errorNumber     = errorNumber;
    this.characterIndex  = characterIndex;
    this.extractedString = extractedString;
  }

  /**
   * Returns the description of this exception.
   * @return a string describing this exception.
   */
  public String getError ()
  {
    return errors [errorNumber];
  }

  /**
   * Returns the number of the error of this exception.
   * @return the number of the error of this exception (one of of the contants of this class).
   */
  public int getErrorNumber ()
  {
    return errorNumber;
  }

  /**
   * Returns the character index in the parsed string where the error happened.
   * @return the character index of this exception.
   */
  public int getCharacterIndex ()
  {
    return characterIndex;
  }

  /**
   * Returns the invalid string (an invalid identifier or an other string) of this exception.
   * @return the invalid string or <code>null</code> if no string is involved for this exception.
   */
  public String getExtractedString ()
  {
    return extractedString;
  }
}

