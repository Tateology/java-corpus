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

import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;

public class Q2Legend extends JNXLegend {

   public static final int REFLECTIVITY = 0;
   public static final int UNKNOWN = -1;
   
   private DecimalFormat fmt = new DecimalFormat("0.00");
   private String units = "N/A";
   private String[] ref3Dvals = new String[]{"","No Data", "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75",""};
   
   private int type = REFLECTIVITY;
   private Color[] colors;
   private double minVal;
   private double maxVal;

   
   public Q2Legend() {
      colors = NexradColorFactory.getColors(NexradHeader.Q2_NATL_MOSAIC_3DREFL, false);
   }
   
   
   public void setType(int type) {
      this.type = type;      
   }
   
   
      
   public void paintComponent(Graphics g) {
      super.paintComponent(g);      //clears the background
      
      double relativeSize = 210.0;
      
      if (type == REFLECTIVITY) {
      
         
         Color emptyColor = new Color(  0, 0, 0, 0);
         int numValidColors = 0;
         for (int n=0; n<colors.length; n++) {
            if (colors[n] != null && ! colors[n].equals(emptyColor)) {
               numValidColors++;
            }
         }
          
         
         String label;
         g.setFont(new Font("Default", Font.PLAIN, 12));
         int pheight = (int)(relativeSize/(numValidColors+2));
         if (pheight > 15) pheight = 15;
         //String units = NexradUtilities.getUnits(header.getProductCode());
         //String units = "dBZ";
         g.drawString("Legend: ("+units+")",15, 2*(int)pheight - 5);
         g.setFont(new Font("Default", Font.PLAIN, 10));
         
         int count = 0;
         for (int i=0; i<colors.length; i++) {
            // Don't include empty colors in Legend
            if (! colors[colors.length-1-i].equals(emptyColor)) {
   
               count++;
               
               g.setColor(colors[colors.length-1-i]); // flip so low values are on the bottom
               g.fillRect(20, (count+2)*pheight, 30, pheight);
               g.setColor(this.getForeground());
               g.drawRect(20, (count+2)*pheight, 30, pheight);
               label = ref3Dvals[colors.length-1-i];  // flip so low values are on the bottom
               g.drawString(label, 65, (int)((count+3)*pheight-(pheight*.1)));
               
            }
         }
         
         
      }
      else {
         

         Color emptyColor = new Color(  0, 0, 0, 0);
         int numValidColors = 0;
         for (int n=0; n<colors.length; n++) {
            if (colors[n] != null && ! colors[n].equals(emptyColor)) {
               numValidColors++;
            }
         }
          

         
         String label;
         g.setFont(new Font("Default", Font.PLAIN, 12));
         int pheight = (int)(relativeSize/(numValidColors+2));
         if (pheight > 15) pheight = 15;
         //String units = NexradUtilities.getUnits(header.getProductCode());
         //String units = "dBZ";
         g.drawString("Legend: ("+units+")",15, 2*(int)pheight - 5);
         g.setFont(new Font("Default", Font.PLAIN, 10));
         
         int count = 0;
         for (int i=1; i<colors.length; i++) {
            // Don't include empty colors in Legend
            if (! colors[colors.length - i].equals(emptyColor)) {
   
               count++;
               
               g.setColor(colors[colors.length - i]); // flip so low values are on the bottom
               g.fillRect(20, (count+2)*pheight, 30, pheight);
               g.setColor(this.getForeground());
               g.drawRect(20, (count+2)*pheight, 30, pheight);
               // flip so low values are on the bottom
               label = fmt.format(((maxVal-minVal)/colors.length*(colors.length-i)));  
               g.drawString(label, 65, (int)((count+3)*pheight-(pheight*.1)));
               
            }
         }
         




      }
         
   }   

   public java.awt.Dimension getPreferredSize() {
      return new java.awt.Dimension(140, 220);
   }
   
   public void setUnits(String units) {
      this.units = units;
      
      if (units.equals("dBZ")) {
         System.out.println("LEGEND TYPE REFL");
         type = REFLECTIVITY;
         colors = NexradColorFactory.getColors(NexradHeader.Q2_NATL_MOSAIC_3DREFL, false);
      }
      else {
         System.out.println("LEGEND TYPE UNKNOWN");
         type = UNKNOWN;
         colors = NexradColorFactory.getColors(NexradHeader.UNKNOWN, false);
      }
      
   }

	/**
	 * Returns the value of minVal.
	 */
	public double getMinVal()
	{
		return minVal;
	}

	/**
	 * Sets the value of minVal.
	 * @param minVal The value to assign minVal.
	 */
	public void setMinVal(double minVal)
	{
		this.minVal = minVal;
	}

	/**
	 * Returns the value of maxVal.
	 */
	public double getMaxVal()
	{
		return maxVal;
	}

	/**
	 * Sets the value of maxVal.
	 * @param maxVal The value to assign maxVal.
	 */
	public void setMaxVal(double maxVal)
	{
		this.maxVal = maxVal;
	}

   


}
