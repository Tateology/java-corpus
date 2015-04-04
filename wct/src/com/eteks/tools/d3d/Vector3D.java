/*
 * @(#)Vector3D.java   06/22/97
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
 * 3D vector.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Vector3D extends CoordinatesArray
{
  public Vector3D ()
  {
    coord = new float [3];
  }

  public Vector3D (float x, float y, float z)
  {
    this ();
    coord [X_AXIS] = x;
    coord [Y_AXIS] = y;
    coord [Z_AXIS] = z;
  }

  public Vector3D (Point3D vecteur)
  {
    this ();
    coord [X_AXIS] = vecteur.getX ();
    coord [Y_AXIS] = vecteur.getY ();
    coord [Z_AXIS] = vecteur.getZ ();
  }

  public Vector3D (Point3D point1, Point3D point2)
  {
    this ();
    coord [X_AXIS] = point1.coord [X_AXIS] - point2.coord [X_AXIS];
    coord [Y_AXIS] = point1.coord [Y_AXIS] - point2.coord [Y_AXIS];
    coord [Z_AXIS] = point1.coord [Z_AXIS] - point2.coord [Z_AXIS];
  }

  public final float getLength ()
  {
    float result = 0;

    for (int axe = X_AXIS; axe <= Z_AXIS; axe++)
      result += coord [axe] * coord [axe];

    return (float)Math.sqrt (result);
  } // end getLength ()

  public float getDotProduct (Vector3D vecteur)
  {
    int   axe;
    float result = 0;

    for (axe = X_AXIS; axe <= Z_AXIS; axe++)
      result = result + coord [axe] * vecteur.coord [axe];

    return result;
  }

  public Vector3D getCrossProduct (Vector3D vecteur)
  {
    Vector3D result = new Vector3D ();

    for (int axe = X_AXIS; axe <= Z_AXIS; axe++)
      result.coord [axe] =     coord [(axe + 1) % 3]
                             * vecteur.coord [(axe + 2) % 3]
                           -   coord [(axe + 2) % 3]
                             * vecteur.coord [(axe + 1) % 3];

    return result;
  }
}
