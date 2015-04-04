/*
 * @(#)JeksFunctionParser.java   08/26/99
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
package com.eteks.jeks;

import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledFunction;
import com.eteks.parser.FunctionParser;

/**
 * Parser for functions entered by user.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksFunctionParser extends FunctionParser
{
  /**
   * Creates a function parser using an instance <code>JeksFunctionSyntax</code> as syntax.
   */
  public JeksFunctionParser ()
  {
    this (new JeksFunctionSyntax ());
  }

  /**
   * Creates a function parser that uses the function syntax <code>syntax</code>.
   */
  public JeksFunctionParser (JeksFunctionSyntax syntax)
  {
    super (syntax);
  }

  public CompiledFunction compileFunction (String functionDefinition) throws CompilationException
  {
    CompiledFunction function = super.compileFunction (functionDefinition);
    // TODO : Should forbid user functions that use a common function name in different Locales (like SUM, SOMME)
    return function;
  }
}
