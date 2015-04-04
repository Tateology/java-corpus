/*
 * @(#)JeksParameter.java   05/02/99
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

import javax.swing.table.TableModel;

import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.Interpreter;

/**
 * Parameters supported in computed expressions.
 * This class uses keys of class <code>JeksCell</code> or <code>JeksCellSet</code>
 * for parameters matching a cell or a cell set.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksParameter implements ExpressionParameter
{
  private JeksExpressionSyntax syntax;
  private Interpreter          interpreter;
  private TableModel           tableModel;

  /**
   * Creates a <code>JeksParameter</code>.
   * @param syntax      expression syntax used to get the syntax of cells and create
   *                    the key of a cell or of a cell set.
   * @param interpreter the interpreter used to compute the value of a cell.
   * @param tableModel  the table model used to get a stored value for a given cell key.
   */
  public JeksParameter (JeksExpressionSyntax syntax,
                        Interpreter          interpreter,
                        TableModel           tableModel)
  {
    this.syntax      = syntax;
    this.interpreter = interpreter;
    this.tableModel  = tableModel;
  }

  /**
   * Returns the key matching parameter or <code>null</code>. For a valid parameter,
   * it returns the matching <code>JeksCell</code> instance if <code>parameter</code>
   * is a cell or the matching <code>JeksCellSet</code> instance if <code>parameter</code>
   * is a cell set or <code>IllegalCellException.class</code> if <code>parameter</code>
   * is an illegal cell identifier (#REF!).
   */
  public Object getParameterKey (String parameter)
  {
    if (syntax.isCellIdentifier (parameter))
    {
      if (parameter.equals (syntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL)))
        return IllegalCellException.class;

      JeksCell cell1;
      JeksCell cell2 = null;
      int      separatorIndex = parameter.indexOf (syntax.getCellSetSeparator ());

      if (separatorIndex < 0)
        // If parameter is a cell (no separator in parameter), return a JeksCell instance as parameter key
        return syntax.getCellAt (parameter);
      else
      {
        // If parameter is a cells set, return a JeksCellSet instance as parameter key
        cell1 = syntax.getCellAt (parameter.substring (0, separatorIndex));
        cell2 = syntax.getCellAt (parameter.substring (separatorIndex + 1));
        return new JeksCellSet (cell1, cell2);
      }

      // If cells bounds are out of bounds, it will be detected during computing
      // and an IndexOutOfBoundsException exception will be thrown
    }
    else
      return null;
  }

  /**
   * Returns the value of the parameter matching <code>parameterKey</code>.
   * If <code>parameterKey</code> is an instance of <code>JeksCell</code> it returns
   * the value of the cell, if <code>parameterKey</code> is an instance of
   * <code>JeksCellSet</code> it returns the values of the cell set in an
   * <code>Object [][]</code> array, and if <code>parameterKey</code> is
   * <code>IllegalCellException.class</code> it throws an exception
   * of <code>IllegalCellException</code> class.
   * @param parameterKey a key returned by the <code>getParameterKey ()</code> method.
   * @exception IllegalCellException if <code>parameterKey</code> is
   *            <code>IllegalCellException.class</code> meaning the cell is invalid.
   */
  public Object getParameterValue (Object parameterKey)
  {
    if (parameterKey.equals (IllegalCellException.class))
      throw new IllegalCellException ();
    else if (parameterKey instanceof JeksCell)
    {
      JeksCell cell = (JeksCell)parameterKey;
      Object value = tableModel.getValueAt (((JeksCell)parameterKey).getRow (),
                                            ((JeksCell)parameterKey).getColumn ());
      if (value instanceof JeksExpression)
        return ((JeksExpression)value).getValue (interpreter);
      else
        return value;
    }
    else // parameterKey instanceof JeksCellSet
    {
      // Build an array to store the values of the cells set
      JeksCellSet parameter = (JeksCellSet)parameterKey;
      int firstRow = parameter.getFirstRow ();
      int lastRow  = parameter.getLastRow ();
      int firstColumn = parameter.getFirstColumn ();
      int lastColumn  = parameter.getLastColumn ();
      Object [][] values = new Object [lastRow - firstRow + 1][];
      for (int row = firstRow, i = 0; row <= lastRow; row++, i++)
      {
        values [i] = new Object [lastColumn - firstColumn + 1];
        for (int column = firstColumn, j = 0; column <= lastColumn; column++, j++)
        {
          Object value = tableModel.getValueAt (row, column);
          if (value instanceof JeksExpression)
            values [i][j] = ((JeksExpression)value).getValue (interpreter);
          else
            values [i][j] = value;
        }
      }
      return values;
    }
  }
};
