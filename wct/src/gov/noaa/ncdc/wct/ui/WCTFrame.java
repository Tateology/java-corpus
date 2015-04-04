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

package gov.noaa.ncdc.wct.ui;

import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class WCTFrame extends JFrame {
   
   public WCTFrame(String title) {
    
      super(title);
      
      
      ImageIcon small_icon = new ImageIcon(WCTFrame.class.getResource("/images/noaabullet.gif"));
      ImageIcon med_icon24 = new ImageIcon(WCTFrame.class.getResource("/images/noaa_logo_24x24.png"));
      ImageIcon med_icon32 = new ImageIcon(WCTFrame.class.getResource("/images/noaa_logo_32x32.png"));
      ImageIcon large_icon = new ImageIcon(WCTFrame.class.getResource("/images/noaa_logo_50x50.png"));
      ArrayList<Image> iconList = new ArrayList<Image>();
      iconList.add(small_icon.getImage());
      iconList.add(med_icon24.getImage());
      iconList.add(med_icon32.getImage());
      iconList.add(large_icon.getImage());
      
      setIconImages(iconList);
      
   }
}
