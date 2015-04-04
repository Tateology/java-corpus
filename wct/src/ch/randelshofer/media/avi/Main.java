/**
 * @(#)Main.java  1.2  2009-08-29
 *
 * Copyright (c) 2008-2009 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.media.avi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Main.
 *
 * @author Werner Randelshofer
 * @version 1.1 2009-08-29 Added raw output.
 * <br>1.0 2008-00-15 Created.
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            test(new File("avidemo-jpg.avi"), AVIOutputStream.VideoFormat.JPG, 24, 1f);
            test(new File("avidemo-png.avi"), AVIOutputStream.VideoFormat.PNG, 24, 1f);
            test(new File("avidemo-raw.avi"), AVIOutputStream.VideoFormat.RAW, 24, 1f);
            test(new File("avidemo-rle8.avi"), AVIOutputStream.VideoFormat.RLE, 8, 1f);
            //test(new File("avidemo-rle4.avi"), AVIOutputStream.VideoFormat.RLE, 4, 1f);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void test(File file, AVIOutputStream.VideoFormat format, int depth, float quality) throws IOException {
        System.out.println("Writing " + file);
        AVIOutputStream out = null;
        Graphics2D g = null;
        try {
            out = new AVIOutputStream(file, format, depth);
            out.setVideoCompressionQuality(quality);

            out.setTimeScale(1);
            out.setFrameRate(30);

            Random rnd = new Random(0); // use seed 0 to get reproducable output
            BufferedImage img;
            switch (depth) {
                case 24:
                default: {
                    img = new BufferedImage(320, 160, BufferedImage.TYPE_INT_RGB);
                    break;
                }
                case 8: {
                    byte[] red = new byte[256];
                    byte[] green = new byte[256];
                    byte[] blue = new byte[256];
                    for (int i = 0; i < 255; i++) {
                        red[i] = (byte) rnd.nextInt(256);
                        green[i] = (byte) rnd.nextInt(256);
                        blue[i] = (byte) rnd.nextInt(256);
                    }
                    rnd.setSeed(0); // set back to 0 for reproducable output
                    img = new BufferedImage(320, 160, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(8, 256, red, green, blue));
                    break;
                }
                case 4: {
                    byte[] red = new byte[16];
                    byte[] green = new byte[16];
                    byte[] blue = new byte[16];
                    for (int i = 0; i < 15; i++) {
                        red[i] = (byte) rnd.nextInt(16);
                        green[i] = (byte) rnd.nextInt(16);
                        blue[i] = (byte) rnd.nextInt(16);
                    }
                    rnd.setSeed(0); // set back to 0 for reproducable output
                    img = new BufferedImage(320, 160, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(4, 16, red, green, blue));
                    break;
                }
            }
            g = img.createGraphics();
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, img.getWidth(), img.getHeight());

            for (int i = 0; i < 100; i++) {
                g.setColor(new Color(rnd.nextInt()));
                g.fillRect(rnd.nextInt(img.getWidth() - 30), rnd.nextInt(img.getHeight() - 30), 30, 30);
                out.writeFrame(img);
            }

        } finally {
            if (g != null) {
                g.dispose();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
