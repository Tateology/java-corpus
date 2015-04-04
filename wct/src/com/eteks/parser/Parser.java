/*
 * @(#)Parser.java   05/09/2001
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

/**
 * Abstract super class of parser collecting usefull methods for parsers.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 */
public abstract class Parser
{
  // Main constants of nodes in syntatic graph
  /**
   * Start node in the syntactic graph. Equal to <code>0</code>.
   * @see #parseExpression
   */
  protected final static int NODE_START = 0;
  /**
   * End node in the syntactic graph. This node equal to <code>Integer.MAX_VALUE</code>,
   * indicates the automaton of the parser to stop the parsing.
   * @see #parseExpression
   */
  protected final static int NODE_END = Integer.MAX_VALUE;

  /**
   * Parses the expression <code>expressionDefinition</code> from the index <code>definitionIndex</code>
   * and returns the top of the stack <code>expressionStack</code>.
   * This method implements an automaton defined with the syntactic graph implemented
   * by the <code>getBoundNode ()</code> method.
   * @param expressionDefinition the string to parse.
   * @param definitionIndex      the index from which the parsing starts.
   * @param expressionStack      stack used to accumulate the operators of different priority or <code>null</code>.
   * @param parserData           optional data. This data is passed to
   *                             <code>getLexical ()</code> and <code>getBoundNode ()</code> methods
   *                             and may be used by parsers to store additional information
   *                             if the implementation of these methods requires it.
   * @return the top of <code>expressionStack</code> if the stack isn't <code>null</code>.
   * @throws CompilationException if the parsed expression doesn't respect the syntax of the parser.
   * @see #getLexical
   * @see #getBoundNode
   */
  protected Object parseExpression (String     expressionDefinition,
                                    int        definitionIndex,
                                    Stack      expressionStack,
                                    Object     parserData) throws CompilationException
  {
    int     node;
    int     boundNode;
    Lexical lexical;

    for (node  = NODE_START;
         node != NODE_END;
         node  = boundNode,
         definitionIndex += lexical.getExtractedString ().length ())
    {
      // Get the next lexical whose code is used as a link in the syntactic graph
      lexical = getLexical (expressionDefinition, definitionIndex, parserData);

      // Get the bound node in the syntactic graph.
      // If no link of type lexical exists, getBoundNode () will throw an exception
      boundNode = getBoundNode (node, lexical,
                                definitionIndex, expressionStack, parserData);
    }

    // Return the top of the stack
    return expressionStack == null || expressionStack.empty ()
             ? null
             : expressionStack.peek ();
  }

  /**
   * Class used by the <code>getLexical ()</code> method to return information
   * about the found lexical.
   */
  protected static class Lexical
  {
    private int    code;
    private String extractedString;
    private Object value;

    public Lexical (int    code,
                    String extractedString,
                    Object value)
    {
      this.code            = code;
      this.extractedString = extractedString;
      this.value           = value;
    }

    public int getCode ()
    {
      return code;
    }

    public String getExtractedString ()
    {
      return extractedString;
    }

    public Object getValue ()
    {
      return value;
    }
  }

  /**
   * Returns the lexical found in the definition <code>expressionDefinition</code>
   * beginning at the character index <code>definitionIndex</code>.
   * The returned lexical is an instance of <code>Lexical</code> that stores its code,
   * the extracted string from the <code>expressionDefinition</code> identified as
   * a valid lexical, and its value if it can be set (its value if the lexical is a literal
   * or its key for other types of lexicals).
   * @param  expressionDefinition the string to parse.
   * @param  definitionIndex      the current index of parsing.
   * @param  parserData           contains some extra information used by the parser.
   * @return an instance of <code>Lexical</code> whose code is used as a possible link in the
   *         syntactic graph used with the automaton implemented in the <code>parseExpression ()</code> method.
   * @throws CompilationException if this method doesn't recognize any lexical at index <code>definitionIndex</code>.
   * @see #parseExpression
   */
  protected abstract Lexical getLexical (String expressionDefinition,
                                         int    definitionIndex,
                                         Object parserData) throws CompilationException;

  /**
   * Returns the node bound to <code>graphNode</code> with the link <code>lexical</code>
   * in the syntactic graph used by the automaton. As nodes and codes of the lexicals are integers,
   * this method can be implemented using a simple array describing the syntactic graph.<br>
   * <code>expressionStack</code> and <code>parserData</code> may be used
   * to perform some additional checkings not described in the syntactic graph
   * or other operations.
   * @param  graphNode       a node in the syntactic graph. First node is equal to <code>NODE_START</code>.
   * @param  lexical         the last parsed lexical.
   * @param  definitionIndex the current index of parsing.
   * @param  expressionStack stack used to accumulate the operators of different priority or <code>null</code>.
   * @param  parserData      contains some extra information used by the parser.
   * @return the node bound to <code>graphNode</code> with the link <code>lexical</code>.
   *         Returns <code>NODE_END</code> if the automaton must stop parsing.
   * @throws CompilationException if no link of type <code>lexical</code> starts from <code>graphNode</code>
   *         or another kind of error of compilation occures.
   * @see #parseExpression
   */
  protected abstract int getBoundNode (int     graphNode,
                                       Lexical lexical,
                                       int     definitionIndex,
                                       Stack   expressionStack,
                                       Object  parserData) throws CompilationException;
}
