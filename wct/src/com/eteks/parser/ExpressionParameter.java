/*
 * @(#)ExpressionParameter.java   04/24/99
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

import java.io.Serializable;

/**
 * Parameters supported in parsed expressions.
 * Classes implementing this interface are used by the <code>compileExpression ()</code> method of the
 * <code>ExpressionParser</code> class to check the parameters of an expression at parsing time
 * and is used by the <code>computeExpression ()</code> method of the <code>ExpressionParameterNode</code>
 * class to get the value of each parameter at runtime.<br>
 * For each lexical accepted as a correct identifier by the <code>isValidIdentifier ()</code> method
 * of the syntax of a parser, the <code>getParameterKey ()</code> method is called to check if the
 * identifier is supported as a parameter in an expression. This method returns either <code>null</code>
 * if it doesn't accept the parameter as a valid one, or a key matching the parameter.
 * This key (that may be the identifier itself) is passed to the <code>getParameterValue ()</code> method
 * at runtime to get the value of the matching parameter.<br>
 * For example, the <code>JeksParameter</code> class implements this interface to check if an identifier
 * is a valid cell of a table and to return the value of that cell. The schema of this class looks like :
 * <BLOCKQUOTE><PRE> public class JeksParameter implements ExpressionParameter
 * {
 *   // private data
 *
 *   // constructor
 *
 *   public Object getParameterKey (String parameter)
 *   {
 *     // if parameter is a cell (A1, a$2,...)
 *     //   return an instance of JeksCell as parameter key
 *
 *     // else if parameter is a set of cells (C3:D4, $A1:$A5,...)
 *     //   return an instance of JeksCellSet as parameter key
 *
 *     // else return null;
 *   }
 *
 *   public Object getParameterValue (Object parameterKey)
 *   {
 *     if (parameterKey instanceof JeksCell)
 *       // return the value of the cell matching parameterKey stored in the table
 *
 *     else // parameterKey instanceof JeksCellSet
 *       // return a double dimension array that contains the values of the cells
 *       // matching parameterKey stored in the table
 *   }
 * }</PRE></BLOCKQUOTE>
 * (read JeksParameter.java for full source).
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.jeks.JeksParameter
 * @see     com.eteks.parser.ExpressionParser
 * @see     com.eteks.parser.Syntax
 * @see     com.eteks.parser.CompiledExpression
 */
public interface ExpressionParameter extends Serializable
{
  /**
   * Returns a key matching the identifier <code>parameter</code>, if <code>parameter</code> is a
   * valid parameter in expressions parsed with this instance.
   * @param parameter  the identifier of a parameter. The identifier is already valid for the
   *        <code>isValidIdentifier ()</code> method of the syntax used by the parser.
   * @return <code>null</code> if <code>parameter</code> is not valid.
   *         Otherwise the key returned will be the one passed to <code>getParameterValue ()</code>
   *         at run time. The key may be the parameter itself.
   */
  Object getParameterKey (String parameter);

  /**
   * Returns the value matching <code>parameterKey</code>.
   * This value may be of any type (<code>Double</code>, <code>String</code>
   * or other).
   * @param parameterKey the key of the parameter returned by <code>getParameterKey</code>.
   * @return The value matching <code>parameterKey</code>.
   */
  Object getParameterValue (Object identifierKey);
}
