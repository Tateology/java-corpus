/*
 * @(#)PascalSyntax.java   01/01/98
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
 * Syntax used by parsers for functions and expressions written in PASCAL. This syntax supports
 * the following operators, constants and functions :
 * <UL><LI>Unary operators : <code>+ -</code></LI>
 *     <LI>Binary operators : <code>^ * / MOD + - >= <= > < = <> AND XOR OR</code>
 *     (from the highest to the lowest priority)</LI>
 *     <LI>Condition : <code>IF THEN ELSE</code></LI>
 *     <LI>Constants : <code>PI FALSE TRUE</code></LI>
 *     <LI>Functions : <code>LN LOG EXP SQR SQRT COS SIN TAN ACOS ASIN ATAN COSH SINH TANH INT
 *                     ABS OPP NOT</code></LI></UL>
 * The literals may be any decimal or integer number.
 * The operator of assignment is <code>=</code> (there's no statement BEGIN END in the syntax),
 * brackets <code>( )</code> and the separator of parameters <code>,</code>.<br>
 * The parsed identifiers (name of functions and their parameters) may contain
 * letters, digits, or the character _. The first character can't be a digit.<br>
 * This syntax isn't case sensitive.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 * @see       com.eteks.parser.FunctionParser
 */
public class PascalSyntax extends DefaultSyntax
{
  /**
   * Creates a PASCAL syntax.
   */
  public PascalSyntax ()
  {
    setBinaryOperatorKey ("=",   OPERATOR_EQUAL);
    setBinaryOperatorKey ("<>",  OPERATOR_DIFFERENT);
    setBinaryOperatorKey (">=",  OPERATOR_GREATER_OR_EQUAL);
    setBinaryOperatorKey ("<=",  OPERATOR_LESS_OR_EQUAL);
    setBinaryOperatorKey (">",   OPERATOR_GREATER);
    setBinaryOperatorKey ("<",   OPERATOR_LESS);
    setBinaryOperatorKey ("OR",  OPERATOR_LOGICAL_OR);
    setBinaryOperatorKey ("AND", OPERATOR_LOGICAL_AND);
    setBinaryOperatorKey ("XOR", OPERATOR_LOGICAL_XOR);

    setConditionPartKey ("IF",   CONDITION_IF);
    setConditionPartKey ("THEN", CONDITION_THEN);
    setConditionPartKey ("ELSE", CONDITION_ELSE);

    setCommonFunctionKey ("NOT", FUNCTION_NOT);

    setBinaryOperatorPriority (OPERATOR_LOGICAL_OR,       1);
    setBinaryOperatorPriority (OPERATOR_LOGICAL_XOR,      2);
    setBinaryOperatorPriority (OPERATOR_LOGICAL_AND,      3);
    setBinaryOperatorPriority (OPERATOR_EQUAL,            4);
    setBinaryOperatorPriority (OPERATOR_DIFFERENT,        4);
    setBinaryOperatorPriority (OPERATOR_GREATER_OR_EQUAL, 5);
    setBinaryOperatorPriority (OPERATOR_LESS_OR_EQUAL,    5);
    setBinaryOperatorPriority (OPERATOR_GREATER,          5);
    setBinaryOperatorPriority (OPERATOR_LESS,             5);
    setBinaryOperatorPriority (OPERATOR_ADD,              6);
    setBinaryOperatorPriority (OPERATOR_SUBSTRACT,        6);
    setBinaryOperatorPriority (OPERATOR_MODULO,           7);
    setBinaryOperatorPriority (OPERATOR_DIVIDE,           7);
    setBinaryOperatorPriority (OPERATOR_MULTIPLY,         7);
    setBinaryOperatorPriority (OPERATOR_POWER,            8);
  }
}

