/*
 * @(#)Polygon3D.java   06/22/97
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * 3D polygon.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Polygon3D
{
  protected Point3D2D points [];
  private   int       currentPoint = 0;
  private   Color     color;
  private   Color     litColor;

  public Polygon3D (int nbPoints, Color color)
  {
    points = new Point3D2D [nbPoints];
    this.color = litColor = color;
  }

  public void addPoint (Point3D2D point)
  {
    if (currentPoint < points.length)
      points [currentPoint++] = point;
  }

  public void computeLitColor (Vector3D  lightDirection,
                               float     lightValue,
                               float     kd,
                               float     ambientLightValue,
                               float     ka)
  {
    if (currentPoint >= 3)
    {
      Vector3D points012Normal = (new Vector3D (points [1], points [0])).
               						   getCrossProduct (new Vector3D (points [1], points [2]));
      float    lightFactor =   points012Normal.getDotProduct (lightDirection)
                               / (  points012Normal.getLength ()
                                  * lightDirection.getLength ());
      float     light      =       kd
                                * lightValue
                                * Math.abs (lightFactor)
                              + ka * ambientLightValue;
      if (light > 1)
        light = 1;

      litColor = new Color ((int)(color.getRed () * light),
      			 			(int)(color.getGreen () * light),
      						(int)(color.getBlue () * light));
    }
  } // end computeLitColor ()

  public void paint (Graphics gc)
  {
    if (currentPoint >= 3)
    {
      Point3D  point1 = new Point3D (points [1].getScreenXCoordinate (), points [1].getScreenYCoordinate (), points [1].getScreenZCoordinate ());
      Vector3D points012Normal = (new Vector3D (point1, new Point3D (points [0].getScreenXCoordinate (), points [0].getScreenYCoordinate (), points [0].getScreenZCoordinate ()))).
               						   getCrossProduct (new Vector3D (point1, new Point3D (points [2].getScreenXCoordinate (), points [2].getScreenYCoordinate (), points [2].getScreenZCoordinate ())));
      if (points012Normal.getZ () > 0)
      {
        Polygon displayedPolygon = new Polygon ();

        for (int i = 0; i < points.length; i++)
          displayedPolygon.addPoint (Math.round (points [i].getScreenXCoordinate ()),
                                     Math.round (points [i].getScreenYCoordinate ()));

        gc.setColor (litColor);
        gc.fillPolygon (displayedPolygon);
      }
    }
  } // end paint ()
}

