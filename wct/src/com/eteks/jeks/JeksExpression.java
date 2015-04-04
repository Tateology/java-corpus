/*
 * @(#)JeksExpression.java   05/02/99
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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.TableModel;

import com.eteks.parser.CompiledExpression;
import com.eteks.parser.Interpreter;

/**
 * Cell values for computed expressions in a table.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksExpression extends CompiledExpression
{
  private transient Object value;

  public JeksExpression (CompiledExpression expression)
  {
    super (expression.getDefinition (), expression.getParameters (),
           expression.getExpressionParameter (), expression.getExpressionTree ());
  }

  /**
   * Returns the value of this expression computed with the <code>computeExpression ()</code>
   * method. This value is computed only at the first call of this method and each time
   * after a call to <code>invalidateValue ()</code>.
   * @exception CircularityException if this expression can't be computed because
   *            it belongs to a reference circularity.
   * @exception IllegalCellException if this expression uses an invalid expression reference (#REF!).
   * @exception IllegalArgumentException if some parameters used by this expression were forbidden.
   * @exception ArithmeticException if a integer division by 0 occured.
   * @exception StackOverflowError if the computation of this expression produced a stack overflow.
   */
  public Object getValue (Interpreter  interpreter)
  {
    if (value instanceof RuntimeException)
      throw (RuntimeException)value;
    else if (value instanceof Error)
      throw (Error)value;
    else
      try
      {
        if (value == null)
          synchronized (this)
          {
            value = computeExpression (interpreter);
          }
        // Return computed value only if it's correct
        return value;
      }
      catch (RuntimeException ex)
      {
        // Keep track of the exception and throw it again
        value = ex;
        throw ex;
      }
      catch (StackOverflowError ex)
      {
        // Keep track of the exception and throw it again
        value = ex;
        throw ex;
      }
  }

  /**
   * Invalidates the value stored by this expression to force the computation of the
   * expression next time <code>getValue ()</code> will be called.
   */
  public void invalidateValue ()
  {
    value = null;
  }

  /**
   * Invalidates the value stored by this expression with with the circularity
   * exception <code>ex</code>. This exception will be thrown when the <code>getValue ()</code>
   * method will be called next time.
   * @param ex a cicrularity exception. Generally it's the exception thrown by the
   *           <code>checkCircularity ()</code> method.
   */
  public void invalidateValue (CircularityException ex)
  {
    value = ex;
  }

  private boolean isCircular ()
  {
    return value instanceof CircularityException;
  }

  /**
   * Parses recursely all the parameters of this expression to
   * check if computing the formula of the referenced cells won't produce
   * a circularity.
   * @param     startCell  the coordinates of the cell storing this expression.
   * @exception CircularityException  if a circurlarity was detected.
   */
  public void checkCircularity (TableModel model, JeksCell startCell) throws CircularityException
  {
    if (getParameterCount () > 0)
      checkCircularity (model, startCell, new Vector ());
  }

  private void checkCircularity (TableModel model, JeksCell startCell, Vector checkedCells) throws CircularityException
  {
    for (Enumeration enu = getParameters ().elements (); enu.hasMoreElements (); )
    {
      Object cellOrSet = enu.nextElement ();
      if (!cellOrSet.equals (IllegalCellException.class))
        if (cellOrSet instanceof JeksCell)
          checkCircularity (model, startCell, (JeksCell)cellOrSet, checkedCells);
        else // JeksCellSet
          checkCircularity (model, startCell, (JeksCellSet)cellOrSet, checkedCells);
    }
  }

  private void checkCircularity (TableModel model, JeksCell startCell, JeksCellSet cellParameterSet, Vector checkedCells) throws CircularityException
  {
    for (int row = cellParameterSet.getFirstRow (); row <= cellParameterSet.getLastRow (); row++)
      for (int column = cellParameterSet.getFirstColumn (); column <= cellParameterSet.getLastColumn (); column++)
        checkCircularity (model, startCell, new JeksCell (row, column), checkedCells);
  }

  private void checkCircularity (TableModel model, JeksCell startCell, JeksCell cellParameter, Vector checkedCells) throws CircularityException
  {
    if (!checkedCells.contains (cellParameter))
      try
      {
        if (startCell.equals (cellParameter))
          throw new CircularityException ();

        checkedCells.addElement (cellParameter);

        Object value = model.getValueAt (cellParameter.getRow (), cellParameter.getColumn ());
        if (value instanceof JeksExpression)
        {
          JeksExpression expression = (JeksExpression)value;
          if (expression.isCircular ())
            throw (CircularityException)expression.value;
          else
            expression.checkCircularity (model, startCell, checkedCells);
        }
      }
      catch (CircularityException ex)
      {
        // Record the cell in the exception and throw it again
        ex.addCell (cellParameter);
        throw ex;
      }
      catch (IndexOutOfBoundsException ex)
      { } // If cellParameter is out of bounds, it will be detected during computing
  }
}
