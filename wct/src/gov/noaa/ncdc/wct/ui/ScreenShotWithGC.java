package gov.noaa.ncdc.wct.ui;
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//package org.eclipse.swt.snippets;
/*
 * GC example snippet: take a screen shot with a GC
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class ScreenShotWithGC {

  public static void main(String[] args) {
	  
//	  if (true) {
		  showScreenshot();
//		  return;
//	  }
	  
//    final Display display = new Display();
//    final Shell shell = new Shell(display);
//    shell.setLayout(new FillLayout());
//    Button button = new Button(shell, SWT.PUSH);
//    button.setText("Capture");
//    button.addListener(SWT.Selection, new Listener() {
//      public void handleEvent(Event event) {
//     /* Take the screen shot */
//        GC gc = new GC(display);
//        final Image image = new Image(display, display.getBounds());
//        gc.copyArea(image, 0, 0);
//        gc.dispose();
//
//        Shell popup = new Shell(shell, SWT.SHELL_TRIM);
//        popup.setLayout(new FillLayout());
//        popup.setText("Image");
//        popup.setBounds(50, 50, 200, 200);
//        popup.addListener(SWT.Close, new Listener() {
//          public void handleEvent(Event e) {
//            image.dispose();
//          }
//        });
//
//        ScrolledComposite sc = new ScrolledComposite(popup, SWT.V_SCROLL | SWT.H_SCROLL);
//        Canvas canvas = new Canvas(sc, SWT.NONE);
//        sc.setContent(canvas);
//        canvas.setBounds(display.getBounds());
//        canvas.addPaintListener(new PaintListener() {
//          public void paintControl(PaintEvent e) {
//            e.gc.drawImage(image, 0, 0);
//          }
//        });
//        popup.open();
//      }
//    });
//    shell.pack();
//    shell.open();
//    while (!shell.isDisposed()) {
//      if (!display.readAndDispatch())
//        display.sleep();
//    }
//    display.dispose();
  }
  
  
  
  public static void showScreenshot() { 
	  
	  JFrame frame = new JFrame();
	  frame.getContentPane().add(new JScrollPane(new JLabel(new ImageIcon(getScreenshot(Display.getDefault(), 0, 0, 20, 20)))));
	  frame.pack();
	  frame.setVisible(true);
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  public static BufferedImage getScreenshot(Display display, int x, int y, int width, int height) {
	  
//	  final Display display = new Display();
	    
	  /* Take the screen shot */
      GC gc = new GC(display);
//      final Image image = new Image(display, display.getBounds());
      final Image image = new Image(display, new Rectangle(0, 0, width, height));
      gc.copyArea(image, x, y);
      gc.dispose();
      
      BufferedImage bimage = convertToAWT(image.getImageData());
      image.dispose();
      
      return bimage;      
  }
  
  

  static BufferedImage convertToAWT(ImageData data) {
    ColorModel colorModel = null;
    PaletteData palette = data.palette;
    if (palette.isDirect) {
      colorModel = new DirectColorModel(data.depth, palette.redMask,
          palette.greenMask, palette.blueMask);
      BufferedImage bufferedImage = new BufferedImage(colorModel,
          colorModel.createCompatibleWritableRaster(data.width,
              data.height), false, null);
      WritableRaster raster = bufferedImage.getRaster();
      int[] pixelArray = new int[3];
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          RGB rgb = palette.getRGB(pixel);
          pixelArray[0] = rgb.red;
          pixelArray[1] = rgb.green;
          pixelArray[2] = rgb.blue;
          raster.setPixels(x, y, 1, 1, pixelArray);
        }
      }
      return bufferedImage;
    } else {
      RGB[] rgbs = palette.getRGBs();
      byte[] red = new byte[rgbs.length];
      byte[] green = new byte[rgbs.length];
      byte[] blue = new byte[rgbs.length];
      for (int i = 0; i < rgbs.length; i++) {
        RGB rgb = rgbs[i];
        red[i] = (byte) rgb.red;
        green[i] = (byte) rgb.green;
        blue[i] = (byte) rgb.blue;
      }
      if (data.transparentPixel != -1) {
        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
            green, blue, data.transparentPixel);
      } else {
        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
            green, blue);
      }
      BufferedImage bufferedImage = new BufferedImage(colorModel,
          colorModel.createCompatibleWritableRaster(data.width,
              data.height), false, null);
      WritableRaster raster = bufferedImage.getRaster();
      int[] pixelArray = new int[1];
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          pixelArray[0] = pixel;
          raster.setPixel(x, y, pixelArray);
        }
      }
      return bufferedImage;
    }
  }
}