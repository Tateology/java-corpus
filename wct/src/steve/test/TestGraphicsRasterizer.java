package steve.test;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class TestGraphicsRasterizer {

    public static void main(String[] args) {

//        BufferedImage bimage = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage bimage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();

        int rgbVal = 10;
        System.out.println("   rgbVal bits ("+rgbVal+") \t"+getBits(rgbVal));
        
        
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(rgbVal, true));
//                g.setColor(Color.BLUE);

        int[] xCoords = { 10, 10, 20, 20, 10 };
        int[] yCoords = { 10, 20, 20, 10, 10 };
        g.fillPolygon(xCoords, yCoords, xCoords.length);

        int outRgbVal = bimage.getRGB(15, 15);
        System.out.println("outRgbVal bits ("+outRgbVal+") \t"+getBits(outRgbVal));

        
        
        
        System.out.println("OUT RGB VAL = " + outRgbVal);

        Icon icon = new ImageIcon(bimage);
        JLabel label = new JLabel(icon);

        final JFrame f = new JFrame("ImageIconExample");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(label);
        f.pack();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

    
    
    private static String getBits(int value) {
        int displayMask = 1 << 31;
        StringBuffer buf = new StringBuffer(35);

        for (int c = 1; c <= 32; c++) {
            buf.append((value & displayMask) == 0 ? '0' : '1');
            value <<= 1;

            if (c % 8 == 0)
                buf.append(' ');
        }

        return buf.toString();
    }
}
