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

package gov.noaa.ncdc.nexradiv.legend;

import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
 
public class WCTLegendPanel extends JNXLegend {       

   private NexradHeader header;
   private Color[] c;
   private double relativeSize = 210.0;
   private boolean usingHeader = false;
   private boolean classify = false;
   
   private Image logo = null;
   
   private Image legendImage;
   
   private String[] dpaLabel = // must have same size as color array for dpa 
      {"< 0.10", "0.10 - 0.25", "0.25 - 0.50", "0.50 - 0.75",
       "0.75 - 1.00", "1.00 - 1.50", "1.50 - 2.00", "2.00 - 2.50",
       "2.50 - 3.00", "3.00 - 4.00", "> 4.00"};
   
   public WCTLegendPanel() {
      
      try {
         java.net.URL imageURL = WCTLegendPanel.class.getResource("noaalogo.gif");
         if (imageURL != null) {
             this.logo = Toolkit.getDefaultToolkit().createImage(imageURL);
         } else {
             System.err.println("Splash image not found");
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      
   }
   
//   public NexradLegendPanel(NexradHeader header, CategoryLegendImageProducer legend) throws Exception {
//      
////      try {
////         java.net.URL imageURL = NexradLegendPanel.class.getResource("noaalogo.gif");
////         if (imageURL != null) {
////             this.logo = Toolkit.getDefaultToolkit().createImage(imageURL);
////         } else {
////             System.err.println("Splash image not found");
////         }
////      } catch (Exception e) {
////         e.printStackTrace();
////      }
////      
//      
//      setNexradHeader(header, legend);
//   }

//   public void setClassify(boolean classify) {
//      this.classify = classify;
////System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyy setClassify: "+classify);      
//   }
   
//   public void setNexradHeader(NexradHeader header, CategoryLegendImageProducer legend) throws Exception {
//      this.header = header;
//      this.c = NexradColorFactory.getColors(header.getProductCode(), classify, header.getVersion());      
//      usingHeader = true;
//      
//      
////System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz setNexradHeader color array length: "+c.length);    
//      
//      
//      
//      resetLegendImage(legend);
//      
//   }
   
   /**
    * Resets the legend image from the provided image producer.  This should be called if legend size changes, etc... 
    * @param legend
    * @throws Exception 
    */
   public void setLegendImage(CategoryLegendImageProducer legend) throws Exception {
//       this.legendImage = getLegendImage(legend);
	   this.legendImage = legend.createLargeLegendImage(getPreferredSize());
       super.repaint();      //clears the background
       repaint();
      
   }

   
   
   
   
   
   
   
   
   
   
   
   public void setIsUsingHeader(boolean usingHeader) {
      this.usingHeader = usingHeader;
   }
   
   public boolean isUsingHeader() {
      return usingHeader;
   }
   
   public void paintComponent(Graphics g) {
      super.paintComponent(g);      //clears the background

      if (legendImage != null) {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, legendImage.getWidth(this), legendImage.getHeight(this));
          g.drawImage(legendImage, 0, 0, null);
      }
      
//      if (usingHeader && header != null) {
//         
//         Color emptyColor = new Color(  0, 0, 0, 0);
//         int numValidColors = 0;
//         for (int n=0; n<c.length; n++) {
//            if (c[n] != null && ! c[n].equals(emptyColor)) {
//               numValidColors++;
//            }
//         }
//          
//
////System.out.println("NUMBER OF VALID COLORS: "+numValidColors);
//         
//         DecimalFormat fmt = new DecimalFormat("0.00");
//         String label;
//         g.setFont(new Font("Default", Font.PLAIN, 12));
//         int pheight = (int)(relativeSize/(numValidColors+2));
//         if (pheight > 15) pheight = 15;
//         String units = NexradUtilities.getUnits(header.getProductCode());
//         g.drawString("Legend: (Category) "+units,15, 2*(int)pheight - 5);
//         g.setFont(new Font("Default", Font.PLAIN, 10));
//         
////System.out.println("pheight: "+pheight);
//            
//         /*
//         int startIndex = 0;
//         // Don't include no color in legend
//         Color emptyColor = new Color(  0, 0, 0, 0);
//         if (c[0].equals(emptyColor)) {
//            startIndex = 1;
//         }
//         
//         for (int i=startIndex; i<c.length; i++) {
//            */
//         int count = 0;
//         for (int i=1; i<c.length; i++) {
//            // Don't include empty colors in Legend
//            if (! c[c.length - i].equals(emptyColor)) {
//            
//               count++;
//               
//               //g.setColor(c[i]);
//               g.setColor(c[c.length - i]); // flip so low values are on the bottom
//               g.fillRect(20, (count+2)*pheight, 30, pheight);
//               //g.setColor(Color.black);
//               g.setColor(this.getForeground());
//               g.drawRect(20, (count+2)*pheight, 30, pheight);
//               if (header.getProductType() == NexradHeader.LEVEL2) {
//            	   label = "";
//            	   try {
//            		   label = "" + header.getDataThresholdString(c.length - i);  // flip so low values are on the bottom
//            	   } catch (Exception e) {
//            		   System.err.println(e);
//            		   e.printStackTrace();
//            		   label = "-1";
//            	   }
//               }
//               else if (header.getProductType() == NexradHeader.L3DPA || header.getProductType() == NexradHeader.XMRG) {
//                  try {
//                     //label = dpaLabel[i-1];
//                     label = "("+(c.length-i)+") "+ dpaLabel[c.length-i-1]; // flip so low values are on the bottom
//                  } catch (Exception e) {
//                     label = "("+(c.length-i)+") ";
//                  }
//               }
//               else {
//                  //label = "" + header.getDataThresholdString(i);
//                  label = "("+(c.length-i)+") " + header.getDataThresholdString(c.length - i);  // flip so low values are on the bottom
//               }
//               g.drawString(label, 65, (int)((count+3)*pheight-(pheight*.1)));
//               
//            }
//         }            
//      }   
/*      
      // Draw NOAA logo
      if (logo != null) {
         g.drawImage(logo, 10, 230, this);
      }
    */  
      
      
   } // END paintComponent        
   
   @Override
   public java.awt.Dimension getPreferredSize() {
      return new java.awt.Dimension(200, getHeight());
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
//   private Image getLegendImage(CategoryLegendImageProducer legend) throws Exception {
//       legend.setCategoryColors(RadarLegendCategoryFactory.getCategoryColors(header, classify));
//       legend.setCategoryLabels(RadarLegendCategoryFactory.getCategoryStrings(header, classify));
//       if (legend.isDrawColorMap()) {
//           legend.setLegendTitle(RadarLegendCategoryFactory.getLegendTitle(header, classify));
//       }
       
//       return legend.createLargeLegendImage(getPreferredSize());
//   }
            
} // END class
