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

package gov.noaa.ncdc.nexrad;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

public class NexradFrameFullScreen extends JFrame {
   
   public NexradFrameFullScreen(String title) {
    
            super(title);
            

      
            //this.setUndecorated(true);
            //this.setIgnoreRepaint(true);
            
            GraphicsEnvironment env = GraphicsEnvironment.
                getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            
            device.setFullScreenWindow(this);
            
     //       this.setGraphicsEnvironment(gc);
            
     /*
      super(title);
      URL url = NexradFrame.class.getResource("/images/noaabullet.gif");
      ImageIcon noaa_icon = new ImageIcon(url);
      setIconImage(noaa_icon.getImage());
      */
      
   }
   
}
