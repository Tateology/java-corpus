/*
 * @(#)CircularityException.java   05/02/99
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

import java.util.Vector;

/**
 * Exception thrown by the <code>checkCircularity ()</code> method
 * of <code>JeksExpression</code>, if a circularity is detected in the cell's references
 * of a table.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 * @see     com.eteks.jeks.JeksExpression
 */
public class CircularityException extends RuntimeException
{
  private Vector cells = new Vector (2, 2);

  /**
   * Adds a cell to the list of cells of the exception.
   * @param cell a cell.
   */
  public void addCell (JeksCell cell)
  {
    if (!cells.contains (cell))
      cells.addElement (cell);
  }

  /**
   * Returns a vector containing the cells that are in the exception.
   * @return A vector of <code>JeksCell</code> instances.
   */
  public final Vector getCells ()
  {
    return cells;
  }

  public String getMessage ()
  {
    return "cicularity detected at cells " + getCells ();
  }
}

