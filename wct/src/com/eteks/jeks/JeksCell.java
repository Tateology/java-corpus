/*
 * @(#)JeksCell.java   05/02/99
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
 * Cell of a table. This class stores the row and the column of a cell.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCell implements Serializable
{
  private int row;
  private int column;

  /**
   * Constructs a cell at coordinates (<code>row</code>,<code>column</code>).
   * @param row
   * @param column
   */
  public JeksCell (int    row,
                   int    column)
  {
    this.row    = row;
    this.column = column;
  }

  /**
   * Returns the row index of this table cell.
   * @return The row index.
   */
  public final int getRow ()
  {
    return row;
  }

  /**
   * Returns the column index of this table cell.
   * @return The column index.
   */
  public final int getColumn ()
  {
    return column;
  }

  /**
   * Returns <code>true</code> if <code>object</code> represents the same cell as this cell.
   * @param object   an object.
   * @return <code>true</code> if this cell and <code>object</code> are equal.
   */
  public boolean equals (Object object)
  {
    return    object instanceof JeksCell
           && ((JeksCell)object).row == row
           && ((JeksCell)object).column == column;
  }

  /**
   * Returns a hash code for this cell.
   * @return An integer using the column and row of the cell.
   */
  public int hashCode ()
  {
    return (row % 0xFFFF) | ((column % 0xFFFF) << 16);
  }

  /**
   * Returns a string representation of this cell (row column).
   */
  public String toString ()
  {
    return row + " " + column;
  }
}
