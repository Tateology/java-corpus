/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.ui.WCTViewerSplash;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

public class WCTSplashWindow extends JWindow {

    private JProgressBar progressBar = new JProgressBar(0, 100);
    private boolean batchMode;

    public WCTSplashWindow(JFrame frame) {
        this(frame, false);
    }

    public WCTSplashWindow(JFrame frame, boolean batchMode) {
        Image splashImage = null;

        this.batchMode = batchMode;

        if (! batchMode) {

            // Load image
//          URL imageURL = NexradIAViewerSplash.class.getResource("/images/radar-resources-small.jpg");
            URL imageURL = WCTViewerSplash.class.getResource("/images/splash-homepage.jpg");

            ImageIcon icon = new ImageIcon(imageURL);
            /*
       if (imageURL != null) {
            splashImage = Toolkit.getDefaultToolkit().createImage(imageURL);
        } else {
            System.err.println("Splash image not found");
        }

        // Center the window on the screen.
        int imgWidth = splashImage.getWidth(frame);
        int imgHeight = splashImage.getHeight(frame);
             */
            int imgWidth = icon.getIconWidth();
            int imgHeight = icon.getIconHeight();
            setSize(imgWidth, imgHeight);
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(
                    (screenDim.width - imgWidth) / 2,
                    (screenDim.height - imgHeight) / 2
            );
//          System.out.println("WIDTHS: "+screenDim.width +"  ,  " + imgWidth);
//          System.out.println("HEIGHTS: "+screenDim.height +"  ,  " + imgHeight);

            // Initialize progressBar
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 4));



            getContentPane().add(new JLabel(icon), "Center");
            getContentPane().add(progressBar, "South");

            pack();
            setVisible(true);
        }
    }

    public void setStatus(String message, int progress) {
        if (! batchMode) {
            progressBar.setString(message);
            progressBar.setValue(progress);
        }
        else {
            System.out.println(progress + " % --- " + message);
        }

//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }




    }

    public void close() {
        dispose();
    }
}
