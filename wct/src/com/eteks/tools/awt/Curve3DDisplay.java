/*
 * @(#)Curve3DDisplay.java   01/04/98
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
 package com.eteks.tools.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;

import com.eteks.parser.CompiledFunction;
import com.eteks.tools.d3d.Matrix3D;
import com.eteks.tools.d3d.Point3D;
import com.eteks.tools.d3d.Vector3D;

/**
 * A canvas able to display a 3D surface defined by the equation z = f(x,y). As the algorithm used
 * to sort hidden and visible polygons is very simple (painter algorithm), some surfaces
 * may appear incorrectly.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Curve3DDisplay extends Canvas
{
  private float [][]   values;
  private Point3D [][] points3D;
  private double       minimumX;
  private double       maximumX;
  private int          displayMode = DISPLAY_ALL_MESH;
  private Image        bitmap;
  private int          meshSize = DEFAULT_MESH_SIZE;

  private final static int    DEFAULT_X_ANGLE = 20;
  private final static int    DEFAULT_Y_ANGLE = 45;
  private final static int    DEFAULT_Z_ANGLE = 0;
  private final static float  DEFAULT_DISTANCE = 4;
  private final static float  DEFAULT_TRANSLATION = 0;
  private final static int    DEFAULT_MESH_SIZE   = 50;

  private int          mouseX;
  private int          mouseY;
  private int          angleX = DEFAULT_X_ANGLE;
  private int          angleY = DEFAULT_Y_ANGLE;
  private int          angleZ = DEFAULT_Z_ANGLE;
  private float        distance = DEFAULT_DISTANCE;
  private float        translationX = DEFAULT_TRANSLATION;
  private float        translationY = DEFAULT_TRANSLATION;
  private float        translationZ = DEFAULT_TRANSLATION;
  private float        userDistance;

  private Color        curveColor = Color.white;
  private Color        curveBackColor = Color.blue.darker ().darker ().darker ();
  private Color        curvePolygonOutlineColor = Color.darkGray;
  private Color        curveMeshColor = Color.black;
  private Vector3D     lightDirection = new Vector3D (0.25f, -0.25f, -1);
  private float        lightValue = 1.25f,
                       kd = 0.7f,
                       ambientLightValue = 0.25f,
                       ka = 0.7f;

  public final static int  DISPLAY_WITH_LIGHTING = 0;
  public final static int  DISPLAY_MESH          = 1;
  public final static int  DISPLAY_ALL_MESH      = 2;

  public void computeCurve (CompiledFunction function,
                            double           minimumX,
                            double           maximumX,
                            int              meshSize)
  {
    this.minimumX = minimumX;
    this.maximumX = maximumX;
    this.meshSize = meshSize;
    distance = distance - (float)minimumX;
    computeValuesArray (function,
                        new Double (minimumX),
                        new Double (maximumX),
                        new Double ((maximumX - minimumX) / meshSize),
                        new Double (minimumX),
                        new Double (maximumX),
                        new Double ((maximumX - minimumX) / meshSize));
  }

  public void setDisplayMode (int displayMode)
  {
    this.displayMode = displayMode;
    invalidateSurface ();
  }

  public void setDefaultPosition ()
  {
    angleX = DEFAULT_X_ANGLE;
    angleY = DEFAULT_Y_ANGLE;
    angleZ = DEFAULT_Z_ANGLE;
    distance = DEFAULT_DISTANCE;
    translationX = DEFAULT_TRANSLATION;
    translationY = DEFAULT_TRANSLATION;
    translationZ = DEFAULT_TRANSLATION;
    invalidateSurface ();
  }

  private void invalidateSurface ()
  {
    points3D = null;
    if (values != null)
      repaint ();
  }

  synchronized private void computeValuesArray (CompiledFunction function,
                                                Double   minimumX,
                                                Double   maximumX,
                                                Double   deltaX,
                                                Double   minimumZ,
                                                Double   maximumZ,
                                                Double   deltaZ)
  {
    int       dimX,
              dimZ,
              xIndex,
              zIndex;
    double [] parameters = new double [2];
    float     valX, valZ,
              pasXReel = (float)deltaX.doubleValue (),
              pasZReel = (float)deltaZ.doubleValue ();

    if (minimumX.doubleValue () > maximumX.doubleValue ())
    {
      Double temp = minimumX;
      minimumX = maximumX;
      maximumX = temp;
    } // end if

    if (minimumZ.doubleValue () > minimumZ.doubleValue ())
    {
      Double temp = minimumZ;
      minimumZ = minimumZ;
      maximumZ = temp;
    } // end if

    dimX = 1 + (int)((maximumX.doubleValue () - minimumX.doubleValue ()) / deltaX.doubleValue ());
    dimZ = 1 + (int)((maximumZ.doubleValue () - minimumZ.doubleValue ()) / deltaZ.doubleValue ());

    values = new float [dimX + 1][dimZ + 1];
    for (valX = (float)minimumX.doubleValue (), xIndex = 1;
         xIndex <= dimX;
         valX += pasXReel, xIndex++)
    {
      values [xIndex][0] = valX;
      parameters [0] = valX;
      for (valZ = (float)minimumZ.doubleValue (), zIndex = 1;
           zIndex <= dimZ;
           valZ += pasZReel, zIndex++)
      {
        if (xIndex == 1)
          values [0][zIndex] = valZ;

        parameters [1] = valZ;
        values [xIndex][zIndex] = (float)function.computeFunction (parameters);
      } // end for
    } // end for
    invalidateSurface ();
  }

  synchronized private void compute3DPoints ()
  {
    if (values == null)
      return;
    points3D = new Point3D [values.length - 1][values [0].length - 1];

    Point3D center = new Point3D (0, 0, 0);
    float   rotX = angleX * (float)Math.PI / 180;
    float   rotY = angleY * (float)Math.PI / 180;
    float   rotZ = angleZ * (float)Math.PI / 180;
    int     rotationOrder [] = {Point3D.Y_AXIS, Point3D.X_AXIS, Point3D.Z_AXIS};
    Vector3D translation = new Vector3D (translationX, -translationY, -translationZ);
    Dimension size = size ();
    float     scale = Math.min (size.width, size.height) / (1.f * (float)(maximumX - minimumX));
    userDistance = -distance * scale;
    Matrix3D displayMatrix = Matrix3D.getDisplayMatrix (center,
       	       					   new Vector3D (rotX, rotY, rotZ),
    			     			   rotationOrder,
    				    		   translation,
    					    	   0, 0, distance,
    		  				       new Point3D (size.width / 2, size.height / 2, 0),
    						       new Vector3D (scale, -scale, scale));

    for (int xIndex = 0; xIndex < points3D.length; xIndex++)
      for (int zIndex = 0; zIndex < points3D [0].length; zIndex++)
         points3D [xIndex][zIndex] = new Point3D (values [xIndex + 1][0], values [xIndex + 1][zIndex + 1], values [0][zIndex + 1])
                                       .multiplyMatrixByPoint (displayMatrix);
  }

  private Color getTriangleColor (Point3D   pointA,
                                  Point3D   pointB,
                                  Point3D   pointC)
  {
    Vector3D  pointsABCNormal = (new Vector3D (pointA, pointB)).
                                 getCrossProduct (new Vector3D (pointA, pointC));
    if (pointsABCNormal.getZ () <= 0)
      return curveBackColor;

    float lightFactor =   pointsABCNormal.getDotProduct (lightDirection)
                        / (  pointsABCNormal.getLength ()
                           * lightDirection.getLength ());
    float light       =      kd
                           * lightValue
                           * Math.abs (lightFactor)
                         + ka * ambientLightValue;
    if (light > 1)
      light = 1;

    return new Color ((int)(curveColor.getRed () * light),
                      (int)(curveColor.getGreen () * light),
                      (int)(curveColor.getBlue () * light));
  }

  private void displayPolygon (Graphics gc,
                               boolean  lit,
                               int      xIndex,
                               int      yIndex)
  {
    Dimension [] points  = new Dimension [4];
    boolean      allOutside = true;

    for (int dx = 0; dx < 2; dx++)
      for (int dy = 0; dy < 2; dy++)
        if (Double.isNaN (values [xIndex + 1 + dx][yIndex + 1 + dy]))
          // Don't display polygons with NaN values
          return;
        else
        {
          float roundedX = Math.round (points3D [xIndex + dx][yIndex + dy].getX ());
          float roundedY = Math.round (points3D [xIndex + dx][yIndex + dy].getY ());
          points [2 * dx + dy] = new Dimension ((int)roundedX, (int)roundedY);
          if (allOutside)
            allOutside =    !inside (points [2 * dx + dy].width, points [2 * dx + dy].height)
                         || points3D [xIndex + dx][yIndex + dy].getZ () < userDistance;
        }

    if (allOutside)
      return;

    // Should split the polygon in two triangles (because the 4 points may not be coplanar)
    gc.setColor (lit
                   ? getTriangleColor (points3D [xIndex][yIndex],
                                       points3D [xIndex + 1][yIndex],
                                       points3D [xIndex][yIndex + 1])
                   : curveColor);

    Polygon displayedPolygon = new Polygon ();

    displayedPolygon.addPoint (points [0].width, points [0].height);
    displayedPolygon.addPoint (points [1].width, points [1].height);
    displayedPolygon.addPoint (points [3].width, points [3].height);
    displayedPolygon.addPoint (points [2].width, points [2].height);

    gc.fillPolygon (displayedPolygon);

    displayedPolygon.addPoint (points [0].width, points [0].height);

    gc.setColor (curvePolygonOutlineColor);
    gc.drawPolygon (displayedPolygon);
  }

  private void displaySurface (Graphics     gc,
                               boolean      lit)
  {
    int     startX = 0,
            startY = 0,
            endX = 0,
            endY = 0,
            deltaX = 1,
            deltaY = 1;
    Point3D farthestPoint3D = points3D [0][0];
    // Seek the farthest displayed point to begin drawing from this point
    // (basic painter 3D algorithm)
    for (int xIndex = 0; xIndex < points3D.length; xIndex += points3D.length - 1)
      for (int yIndex = 0; yIndex < points3D [0].length; yIndex += points3D [0].length - 1)
        if (xIndex != 0 || yIndex != 0)
          if (points3D [xIndex][yIndex].getZ () < farthestPoint3D.getZ ())
          {
            farthestPoint3D = points3D [xIndex][yIndex];
            startX = xIndex;
            startY = yIndex;
          } // end if

    endX = startX == 0  ? points3D.length - 1  : 0;
    endY = startY == 0  ? points3D [0].length - 1  : 0;

    boolean goThroughAxisX = points3D [endX][startY].getZ () < points3D [startX][endY].getZ ();

    if (startX != 0)
    {
      startX--;
      deltaX = endX = -1;
    }

    if (startY != 0)
    {
      startY--;
      deltaY = endY = -1;
    }

    if (!goThroughAxisX)
      for (int  xIndex = startX; xIndex != endX; xIndex += deltaX)
        for (int yIndex = startY; yIndex != endY; yIndex += deltaY)
          displayPolygon (gc, lit, xIndex, yIndex);
    else
      for (int yIndex = startY; yIndex != endY; yIndex += deltaY)
        for (int xIndex = startX; xIndex != endX; xIndex += deltaX)
          displayPolygon (gc, lit, xIndex, yIndex);
  }

  private void displayMesh (Graphics gc)
  {
    gc.setColor (curveMeshColor);

    for (int xIndex = 0; xIndex < points3D.length; xIndex++)
      for (int yIndex = 1; yIndex < points3D [0].length; yIndex++)
        if (   values [xIndex + 1][yIndex] == values [xIndex + 1][yIndex]
            && values [xIndex + 1][yIndex + 1] == values [xIndex + 1][yIndex + 1])
        {
          int startX = (int)points3D [xIndex][yIndex - 1].getX ();
          int startY = (int)points3D [xIndex][yIndex - 1].getY ();
          int endX   = (int)points3D [xIndex][yIndex].getX ();
          int endY   = (int)points3D [xIndex][yIndex].getY ();
          if (      inside (startX, startY)
                 && points3D [xIndex][yIndex - 1].getZ () >= userDistance
              ||    inside (endX,   endY)
                 && points3D [xIndex][yIndex].getZ () >= userDistance)
            gc.drawLine (startX, startY, endX, endY);
        }

    for (int yIndex = 0; yIndex < points3D [0].length; yIndex++)
      for (int xIndex = 1; xIndex < points3D.length; xIndex++)
        if (   values [xIndex][yIndex + 1] == values [xIndex][yIndex + 1]
            && values [xIndex + 1][yIndex + 1] == values [xIndex + 1][yIndex + 1])
        {
          int startX = (int)points3D [xIndex - 1][yIndex].getX ();
          int startY = (int)points3D [xIndex - 1][yIndex].getY ();
          int endX   = (int)points3D [xIndex][yIndex].getX ();
          int endY   = (int)points3D [xIndex][yIndex].getY ();
          if (      inside (startX, startY)
                 && points3D [xIndex - 1][yIndex].getZ () >= userDistance
              ||    inside (endX,   endY)
                 && points3D [xIndex][yIndex].getZ () >= userDistance)
            gc.drawLine (startX, startY, endX, endY);
        }
  }

  public Dimension preferredSize ()
  {
    return new Dimension (300, 200);
  }

  public void reshape (int x, int y, int width, int height)
  {
    super.reshape (x, y, width, height);
  }

  public void update (Graphics gc)
  {
    paint (gc);
  }

  public void paint (Graphics gc)
  {
    if (bitmap == null)
    {
      Dimension size = size ();
      bitmap = createImage (size.width, size.height);
      paintSurface (bitmap.getGraphics ());
    }
    else
      if (points3D == null)
        paintSurface (bitmap.getGraphics ());

    gc.drawImage (bitmap, 0, 0, this);
  }

  synchronized private void paintSurface (Graphics gc)
  {
    Dimension size = size ();
    gc.setColor (getBackground ());
    gc.fillRect (0, 0, size.width, size.height);

    if (points3D == null)
      compute3DPoints ();
    if (points3D != null)
      switch (displayMode)
      {
        case DISPLAY_WITH_LIGHTING : displaySurface (gc, true);
                                     break;
        case DISPLAY_MESH          : displaySurface (gc, false);
                                     break;
        case DISPLAY_ALL_MESH      : displayMesh (gc);
                                     break;
      }

    gc.setColor (getBackground ());
    gc.draw3DRect (0, 0, size.width  - 1, size.height - 1, false);
  }

  public boolean mouseDown (Event event, int x, int y)
  {
    mouseX = x;
    mouseY = y;
    return super.mouseDown (event, x, y);
  }

  public boolean mouseDrag (Event event, int x, int y)
  {
    if (points3D != null)
    {
      Dimension size = size ();
      if ((event.modifiers & Event.SHIFT_MASK) != 0)
      {
        this.translationX = this.translationX + (x - mouseX) * 5 * (float)(maximumX - minimumX) / size.height;
        this.translationY = this.translationY + (y - mouseY) * 5 * (float)(maximumX - minimumX) / size.height;
      }
      else if ((event.modifiers & Event.CTRL_MASK) != 0)
      {
        this.angleZ = (this.angleZ + (x - mouseX) * 180 / size.width) % 360;
        this.translationZ = Math.max (this.translationZ + (mouseY - y) * 5 * (float)(maximumX - minimumX) / size.height, (float)minimumX - distance);
      }
      else
      {
        this.angleY = (this.angleY + (x - mouseX) * 180  / size.width) % 360;
        this.angleX = (this.angleX + (y - mouseY) * 180 / size.height) % 360;
      }

      mouseX = x;
      mouseY = y;
      invalidateSurface ();

      return true;
    }
    else
      return super.mouseDrag (event, x, y);
  }
}

