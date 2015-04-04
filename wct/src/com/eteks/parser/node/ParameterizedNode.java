/*
 * @(#)ParameterizedNode.java   01/01/98
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
package com.eteks.parser.node;

/**
 * Node requiring parameters to compute its value.
 * Operators and functions implement this interface to store their operands or parameters.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public interface ParameterizedNode extends ExpressionNode
{
  /**
   * Adds the node <code>parameter</code> to the list of parameters or operands
   * of this node.
   * @param parameter the node of an expression used as parameter or operand.
   */
  void addParameter (ExpressionNode parameter);

  /**
   * Returns the current count of parameters or operands stored by this node.
   * @return the count of parameters of this node.
   */
  int getParameterCount ();
}
