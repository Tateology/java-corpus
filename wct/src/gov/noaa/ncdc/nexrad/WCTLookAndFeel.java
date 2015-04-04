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

import javax.swing.UIManager;

//import org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeelAddons;

public class WCTLookAndFeel {
   
    
    public static boolean isMacLookAndFeel() {
        String sysLAF = UIManager.getSystemLookAndFeelClassName();
        System.out.println("SysLAF: "+sysLAF);
        
        if (sysLAF.contains("MacLookAndFeel") || sysLAF.contains("AquaLookAndFeel")) {
            return true;
        }
        else {
            return false;
        }
    }
   
 

   /**
    *  Description of the Method
    */
   public static void configureUI() {
/*
      UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
      Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
      Options.setDefaultIconSize(new Dimension(18, 18));

      try {
         //String lafName =
         //      LookUtils.IS_OS_WINDOWS_XP
         //      ? Options.getCrossPlatformLookAndFeelClassName()
         //      : Options.getSystemLookAndFeelClassName();
         //UIManager.setLookAndFeel(lafName);



*/


    
       

//     UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

/*

         this.setUndecorated(true);
         this.setIgnoreRepaint(true);
         
         GraphicsEnvironment env = GraphicsEnvironment.
             getLocalGraphicsEnvironment();
         GraphicsDevice device = env.getDefaultScreenDevice();
         GraphicsConfiguration gc = device.getDefaultConfiguration();
         
         device.setFullScreenWindow(this);
         

*/
       
       if (isMacLookAndFeel()) {
           try {
//               System.out.println("Using native look and feel: "+sysLAF);
//               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               
//               System.out.println("Found native look and feel: "+sysLAF);
//               UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
               
               
               com.jgoodies.looks.plastic.PlasticLookAndFeel.setCurrentTheme(new com.jgoodies.looks.plastic.theme.DesertBluer());
               UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.PlasticLookAndFeel());
               
               
               return;
           } catch (Exception e) {
               
           }
       }
       

      try {
             
         com.jgoodies.looks.plastic.PlasticLookAndFeel.setCurrentTheme(new com.jgoodies.looks.plastic.theme.DesertBluer());
         UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.PlasticXPLookAndFeel());
         

//         UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         
         
      } catch (Exception e) {
         System.err.println("Can't set look & feel:" + e);
      }

    
//      try {
//          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
////          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//      } catch (Exception e) { }
      
      
      
   }



   
}
