package gov.noaa.ncdc.common;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ScreenImage
{
    /*
     *  Create a BufferedImage for Swing components.
     *  The entire component will be captured to an image.
     *
     *  @param   component Swing component to create image from
     *  @param   fileName name of file to be created or null
     *  @return image the image for the given region
     *  @exception IOException if an error occurs during writing
    */
    public static BufferedImage createImage(JComponent component, String fileName)
        throws IOException
    {
        Dimension d = component.getSize();

        if (d.width == 0)
        {
            d = component.getPreferredSize();
            component.setSize( d );
        }

        Rectangle region = new Rectangle(0, 0, d.width, d.height);
        return ScreenImage.createImage(component, region, fileName);
    }

    /*
     *  Create a BufferedImage for Swing components.
     *  All or part of the component can be captured to an image.
     *
     *  @param   component Swing component to create image from
     *  @param   region The region of the component to be captured to an image
     *  @param   fileName name of file to be created or null
     *  @return image the image for the given region
     *  @exception IOException if an error occurs during writing
    */
    public static BufferedImage createImage(JComponent component, Rectangle region, String fileName)
        throws IOException
    {
        boolean opaqueValue = component.isOpaque();
        component.setOpaque( true );
        BufferedImage image = new BufferedImage(region.width, region.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setClip( region );
        component.paint( g2d );
        g2d.dispose();
        component.setOpaque( opaqueValue );
        ScreenImage.writeImage(image, fileName);
        return image;
    }

    /*
     *  Create a BufferedImage for AWT components.
     *
     *  @param   component AWT component to create image from
     *  @param   fileName name of file to be created or null
     *  @return image the image for the given region
     *  @exception AWTException see Robot class constructors
     *  @exception IOException if an error occurs during writing
    */
    public static BufferedImage createImage(Component component, String fileName)
        throws AWTException, IOException
    {
        Point p = new Point(0, 0);
        SwingUtilities.convertPointToScreen(p, component);
        Rectangle region = component.getBounds();
        region.x = p.x;
        region.y = p.y;
        return ScreenImage.createImage(region, fileName);
    }

    /**
     *  Convenience method to create a BufferedImage of the desktop
     *
     *  @param   fileName name of file to be created or null
     *  @return image the image for the given region
     *  @exception AWTException see Robot class constructors
     *  @exception IOException if an error occurs during writing
     */
    public static BufferedImage createDesktopImage(String fileName)
        throws AWTException, IOException
    {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle region = new Rectangle(0, 0, d.width, d.height);
        return ScreenImage.createImage(region, fileName);
    }

    /**
     *  Create a BufferedImage from a rectangular region on the screen.
     *
     *  @param   region region on the screen to create image from
     *  @param   fileName name of file to be created or null
     *  @return image the image for the given region
     *  @exception AWTException see Robot class constructors
     *  @exception IOException if an error occurs during writing
     */
    public static BufferedImage createImage(Rectangle region, String fileName)
        throws AWTException, IOException
    {
        BufferedImage image = new Robot().createScreenCapture( region );
        ScreenImage.writeImage(image, fileName);
        return image;
    }

    /**
     *  Write a BufferedImage to a File.
     *
     *  @param   image image to be written
     *  @param   fileName name of file to be created
     *  @exception IOException if an error occurs during writing
    */
    public static void writeImage(BufferedImage image, String fileName)
        throws IOException
    {
        if (fileName == null) return;

        int offset = fileName.lastIndexOf( "." );
        String type = offset == -1 ? "jpg" : fileName.substring(offset + 1);

        ImageIO.write(image, type, new File( fileName ));
    }

    public static void main(String args[])
        throws Exception
    {
        final JFrame frame = new JFrame();
        final JTextArea textArea = new JTextArea(30, 60);
        final JScrollPane scrollPane = new JScrollPane( textArea );
        frame.getContentPane().add( scrollPane );

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar( menuBar );
        JMenu menu = new JMenu( "File" );
        ScreenImage.createImage(menu, "menu.jpg");
        menuBar.add( menu );
        JMenuItem menuItem = new JMenuItem( "Frame Image" );
        menu.add( menuItem );
        menuItem.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //  Let the menu close and repaint itself before taking the image

                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(50);
                            System.out.println("Creating frame.jpg");
                            frame.repaint();
                            ScreenImage.createImage(frame, "frame.jpg");
                        }
                        catch(Exception exc) { System.out.println(exc); }
                    }
                }.start();
            };
        });

        final JButton button = new JButton("Create Images");
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    System.out.println("Creating desktop.jpg");
                    ScreenImage.createDesktopImage( "desktop.jpg" );
                    System.out.println("Creating frame.jpg");
                    ScreenImage.createImage(frame, "frame.jpg");
                    System.out.println("Creating scrollpane.jpg");
                    ScreenImage.createImage(scrollPane, "scrollpane.jpg");
                    System.out.println("Creating textarea.jpg");
                    ScreenImage.createImage(textArea, "textarea.jpg");
                    System.out.println("Creating button.jpg");
                    ScreenImage.createImage(button, "button.jpg");
                    button.setText("button refreshed");
                    button.paintImmediately(button.getBounds());
                    System.out.println("Creating refresh.jpg");
                    ScreenImage.createImage(button, "refresh.jpg");
                    System.out.println("Creating region.jpg");
                    Rectangle r = new Rectangle(0, 0, 100, 16);
                    ScreenImage.createImage(textArea, r, "region.png");
                }
                catch(Exception exc) { System.out.println(exc); }
            }
        });
        frame.getContentPane().add(button, BorderLayout.SOUTH);

        try
        {
            FileReader fr = new FileReader( "ScreenImage.java" );
            BufferedReader br = new BufferedReader(fr);
            textArea.read( br, null );
            br.close();
        }
        catch(Exception e) {}

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }
}

