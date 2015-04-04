/*
 * @(#)Curve2DDisplay.java   01/04/98
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
import java.awt.Graphics;

import com.eteks.parser.CompiledFunction;

/**
 * A canvas able to display a 2D curve defined by the equation y = f(x).
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class Curve2DDisplay extends Canvas
{
  private double [ ] values;
  private double     minimumX;
  private double     maximumX;
  private double     minimumY = 0.;
  private double     maximumY = 0.;
  private CompiledFunction function = null;

  private static final boolean  regular = true;

  public Dimension preferredSize ()
  {
    return new Dimension (300, 200);
  }

  public void computeCurve (CompiledFunction function, double minimumX, double maximumX)
  {
    this.function = function;
    this.minimumX = minimumX;
    this.maximumX = maximumX;
    values = new double [size ().width];
    double [] parameters = new double [1];
    double    pasCalcul = (maximumX - minimumX) / values.length;
    for (int i = 0; i < values.length; i++)
    {
      parameters [0] = i * pasCalcul + minimumX;
      values  [i] = function.computeFunction (parameters);

      if (!regular)
        if (i == 0)
          minimumY =
          maximumY = values [0];
        else
          if (values [i] < minimumY)
            minimumY = values [i];
          else if (values [i] > maximumY)
            maximumY = values [i];
    }

    repaint ();
  }

  public void reshape (int x, int y, int width, int height)
  {
    values = null;
    super.reshape (x, y, width, height);
    if (function != null)
      computeCurve (function, minimumX, maximumX);
  }

  public void paint (Graphics gc)
  {
    gc.setColor (Color.black);
    if (values != null)
    {
      Dimension size = size ();

      int previousPoint = regular
                            ? size.height / 2 - (int)(values [0] / (maximumX - minimumX) * size.width)
                            : (int)((maximumY - values [0]) / (maximumY - minimumY) * size.height);
      // Draw the curve joining each point
      for (int i = 1; i < values.length; i++)
      {
        int nextPoint = regular
                          ? size.height / 2 - (int)(values [i] / (maximumX - minimumX) * size.width)
                          : (int)((maximumY - values [i]) / (maximumY - minimumY) * size.height);

        if (   values [i - 1] == values [i - 1]
            && values [i] == values [i])
          gc.drawLine (i - 1, previousPoint, i, nextPoint);
        previousPoint = nextPoint;
      }

      gc.setColor (getBackground ());
      gc.draw3DRect (0, 0, size.width  - 1, size.height - 1, false);
    }
  }
}
