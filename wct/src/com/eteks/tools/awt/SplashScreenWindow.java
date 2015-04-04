/*
 * @(#)SplashScreenWindow.java  05/22/2001
 *
 * Copyright (c) 2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Window;

/**
 * Spash screen window.
 *
 * @version   1.0
 * @author    Emmanuel Puybaret
 * @since     Jeks 1.0
 */
public class SplashScreenWindow extends Window
{
  private Image  image;
  private Thread waitingThread;

  public SplashScreenWindow (Image image,
                             final long duration)
  {
    super (new Frame ());
    try
    {
      MediaTracker imageTracker = new MediaTracker (this);
      imageTracker.addImage (image, 0);
      imageTracker.waitForID (0);
      if (imageTracker.isErrorAny ())
        throw new IllegalArgumentException ("Can't load image");
      this.image = image;
      Dimension screenSize = getToolkit ().getScreenSize ();
      setBounds ((screenSize.width - image.getWidth (this)) / 2,
                 (screenSize. height - image.getHeight (this)) / 2,
                 image.getWidth (this), image.getHeight (this));
      show ();
      waitingThread = new Thread ()
        {
          public void run ()
          {
            try
            {
              Thread.sleep (duration);
              dispose ();
            }
            catch (InterruptedException e)
            { }
          }
        };
      waitingThread.start ();
    }
    catch (InterruptedException e)
    { }
  }

  public void join ()
  {
    try
    {
      if (waitingThread.isAlive ())
        waitingThread.join ();
    }
    catch (InterruptedException e)
    { }
  }

  public void paint (Graphics gc)
  {
    gc.drawImage (image, 0, 0, this);
  }
}
