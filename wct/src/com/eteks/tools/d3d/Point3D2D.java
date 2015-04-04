/*
 * @(#)Point3D2D.java   06/22/97
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
 * 3D point with screen coordinates.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Point3D2D extends Point3D
{
  private float screenXCoordinate = 0;
  private float screenYCoordinate = 0;
  private float screenZCoordinate = 0;

  public Point3D2D (float x, float y, float z)
  {
    super (x, y, z);
  }

  public void transform (Matrix3D matriceTransf, Matrix3D matriceEcran)
  {
    Point3D pointTransf = multiplyMatrixByPoint (matriceTransf);
    coord [X_AXIS] = pointTransf.coord [X_AXIS];
    coord [Y_AXIS] = pointTransf.coord [Y_AXIS];
    coord [Z_AXIS] = pointTransf.coord [Z_AXIS];

    Point3D pointEcran = multiplyMatrixByPoint (matriceEcran);
    screenXCoordinate = pointEcran.getX ();
    screenYCoordinate = pointEcran.getY ();
    screenZCoordinate = pointEcran.getZ ();
  }

  public float getScreenXCoordinate ()
  {
    return screenXCoordinate;
  }

  public float getScreenYCoordinate ()
  {
    return screenYCoordinate;
  }

  public float getScreenZCoordinate ()
  {
    return screenZCoordinate;
  }
}
