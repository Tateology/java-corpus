/*
 * @(#)JeksCellRenderer.java   08/23/99
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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.eteks.parser.Interpreter;

/**
 * Cell renderer for computed cells. If the rendered cell value is an instance
 * of <code>JeksExpression</code>, the result of the computed expression is rendered
 * with the default renderer of a table.
 * All other types of cell value are rendered with the default renderer a table.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCellRenderer extends DefaultTableCellRenderer
{
  private JeksExpressionSyntax syntax;
  private Interpreter          interpreter;

  private final static Object DEFAULT_VALUE = new Long (0);

  /**
   * Creates a cell renderer.
   * @param syntax the syntax is used to get the string to display if a cell
   *               couldn't be computed (invalid reference, illegal value or other errors).
   *               This string is returned by the <code>getCellError ()</code> of
   *               <code>JeksExpressionSyntax</code>.
   * @param interpreter the interpreter used to compute the value of computed cells.
   */
  public JeksCellRenderer (JeksExpressionSyntax syntax,
                           Interpreter          interpreter)
  {
    this.syntax      = syntax;
    this.interpreter = interpreter;
  }

  /**
   * Returns the component used to render cell values.
   * Overrides <code>getTableCellRendererComponent ()</code> to render the value
   * returned by <code>getExpressionValue (value)</code> if <code>value</code>
   * is an instance of <code>JeksExpression</code>.
   */
  public Component getTableCellRendererComponent (JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column)
  {
    if (value instanceof JeksExpression)
    {
      // Get the computed value to render
      value = getExpressionValue ((JeksExpression)value);
      // Once the expression is computed, its rendering is delegated
      // to the default renderer set on the table according to the class of the result
      return table.getDefaultRenderer (value.getClass ()).getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    }
    else
      // This renderer is supposed to be called only for JeksExpression values
      // but this provides a default rendering (and avoid stack overflow)
      return super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
  }

  /**
   * Returns the value to render for <code>expression</code>. The returned value is either
   * <code>expression.getValue (interpreter)</code> using the interpreter passed as
   * parameter at creation time or a localized error string if the value couldn't be computed.
   */
  public Object getExpressionValue (JeksExpression expression)
  {
    Object value;
    try
    {
      value = expression.getValue (interpreter);
      if (value == null)
        value = DEFAULT_VALUE;
      else if (value instanceof Double)
      {
        Double doubleValue = (Double)value;
        if (   doubleValue.isNaN ()
            || doubleValue.isInfinite ())
          value = (String)syntax.getCellError (JeksExpressionSyntax.ERROR_INVALID_NUMBER);
        else if (doubleValue.doubleValue () == Math.floor (doubleValue.doubleValue ()))
          value = new Long (doubleValue.longValue ());
      }
    }
    catch (Exception ex)
    {
      value = syntax.getCellError (ex);
    }
    catch (Error ex)
    {
      if ((value = syntax.getCellError (ex)) == null)
        throw ex;
    }

    return value;
  }
}
