/*
 * @(#)CoordinatesArray.java   06/22/97
 *
 * Copyright (c) 2000 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.tools.d3d;

/**
 * Abstract coordinates class.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public abstract class CoordinatesArray implements Cloneable
{
  public static final int X_AXIS = 0;
  public static final int Y_AXIS = 1;
  public static final int Z_AXIS = 2;

  protected float coord [];

  public CoordinatesArray addCoordinates (CoordinatesArray tab)
  {
    CoordinatesArray result = null;

    if (coord.length >= 3 && tab.coord.length >= 3)
    {
      try
      {
        result = (CoordinatesArray)clone ();
        for (int i = X_AXIS; i < Z_AXIS; i++)
          result.coord [i] += tab.coord [i];
      }
      catch (CloneNotSupportedException exception)
      {
      }
    }

    return result;
  }

  public float getX ()
  {
    return coord [X_AXIS];
  }

  public float getY ()
  {
    return coord [Y_AXIS];
  }

  public float getZ ()
  {
    return coord [Z_AXIS];
  }

  public float getCoordinate (int axe)
  {
    return coord [axe];
  }
}
