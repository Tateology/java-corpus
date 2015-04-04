/*
 * @(#)JeksCellSet.java   05/02/99
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

import java.io.Serializable;

/**
 * Contiguous set of table cells.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCellSet implements Serializable
{
  private int firstRow;
  private int firstColumn;
  private int lastRow;
  private int lastColumn;

  /**
   * Constructs a set of cells from the cell at coordinates
   * <code>(firstRow,firstColumn)</code> to the one at coordinates
   * <code>(lastRow,lastColumn)</code>. The coordinates don't need
   * to be ordonated (<code>firstRow</code> may be greater than
   * <code>lastRow</code>).
   * @param firstRow    The coordinates of the first cell.
   * @param firstColumn
   * @param lastRow     The coordinates of the last cell.
   * @param lastColumn
   */
  public JeksCellSet (int firstRow,
                      int firstColumn,
                      int lastRow,
                      int lastColumn)
  {
    if (firstRow <= lastRow)
    {
      this.firstRow    = firstRow;
      this.lastRow     = lastRow;
    }
    else
    {
      this.firstRow    = lastRow;
      this.lastRow     = firstRow;
    }

    if (firstColumn <= lastColumn)
    {
      this.firstColumn = firstColumn;
      this.lastColumn  = lastColumn;
    }
    else
    {
      this.firstColumn = lastColumn;
      this.lastColumn  = firstColumn;
    }
  }

  /**
   * Constructs a set of cells from the cell <code>firstCell</code> to the cell
   * <code>lastCell</code>.
   * @param firstCell    The first cell.
   * @param lastCell     The last cell.
   */
  public JeksCellSet (JeksCell firstCell,
                             JeksCell lastCell)
  {
    this (firstCell.getRow (), firstCell.getColumn (), lastCell.getRow (), lastCell.getColumn ());
  }

  /**
   * Returns the row index of the first cell of this table cells set.
   * @return The row index.
   */
  public final int getFirstRow ()
  {
    return firstRow;
  }

  /**
   * Returns the column index of the first cell of this table cells set.
   * @return The column index.
   */
  public final int getFirstColumn ()
  {
    return firstColumn;
  }

  /**
   * Returns the row index of the last cell of this table cells set.
   * @return The row index.
   */
  public final int getLastRow ()
  {
    return lastRow;
  }

  /**
   * Returns the column index of the last cell of this table cells set.
   * @return The column index.
   */
  public final int getLastColumn ()
  {
    return lastColumn;
  }

  /**
   * Returns <code>true</code> if the cell at coordinates <code>(row,column)</code>
   * is contained in this cells set.
   * @param row      The coordinates of a cell.
   * @param column
   * @return <code>true</code> if this cells set contains the cell.
   */
  public boolean containsCell (int row, int column)
  {
    return    firstRow <= row
           && row <= lastRow
           && firstColumn <= column
           && column <= lastColumn;
  }
}
