/*
 * @(#)Point3D.java   06/22/97
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
 * 3D point.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Point3D extends CoordinatesArray
{
  public static final int HOMOGENEOUS_COORD = 3;

  public Point3D ()
  {
    coord = new float [4];
    coord [HOMOGENEOUS_COORD] = 1;
  }

  public Point3D (float x, float y, float z)
  {
    this ();
    coord [X_AXIS] = x;
    coord [Y_AXIS] = y;
    coord [Z_AXIS] = z;
  }

  public Object clone ()
  {
    return new Point3D (coord [X_AXIS], coord [Y_AXIS], coord [Z_AXIS]);
  }

  public String toString ()
  {
    String retour = "Point3D :";
    for (int i = 0; i < 4; i++)
      retour += " " +  coord [i];
    return retour;
  }

  public Point3D multiplyMatrixByPoint (Matrix3D matrice)
  {
    Point3D result = new Point3D ();

    result.coord [HOMOGENEOUS_COORD] = 0;
    for (int i = X_AXIS; i <= HOMOGENEOUS_COORD; i++)
      for (int j = X_AXIS; j <= HOMOGENEOUS_COORD; j++)
        result.coord [i] += matrice.getValue (i, j) * coord [j];

    if (   result.coord [HOMOGENEOUS_COORD] != 1
        && result.coord [HOMOGENEOUS_COORD] != 0)
      for (int i = X_AXIS; i <= Z_AXIS; i++)
        result.coord [i] /= result.coord [HOMOGENEOUS_COORD];

    result.coord [HOMOGENEOUS_COORD] = 1;
    return result;
  }
}
