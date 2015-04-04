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

import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.Component;
import java.net.URL;

public class LoadDataThread extends Thread 
{

   private WCTViewer nexview;
   private URL nexrad_url;
   private boolean resetNexradExtent;
   private Component[] comps;
   private boolean clearAlphanumeric;
   private boolean isAlphaBackground;
   
   /**
    * Constructor
    * @param nexview            NexradIAViewer Object
    * @param fileToLoad         The Nexrad Filename to load
    * @param resetNexradExtent  Reset the to the 124nm Nexrad Extent
    * @param jButton            Button that initialized the thread -- Disable during processing so that
                                another thread cannot be simultaneously executed.
    * @param clearAlphanumeric  
    * @param isAlphaBackground  Is this a background for an alphanumeric product
    */
   LoadDataThread(WCTViewer nexview, URL nexrad_url, boolean resetNexradExtent, 
                  Component[] comps, boolean clearAlphanumeric, boolean isAlphaBackground) {
      this.nexview = nexview;
      this.nexrad_url = nexrad_url;
      this.resetNexradExtent = resetNexradExtent;
      this.comps = comps;
      this.clearAlphanumeric = clearAlphanumeric;
      this.isAlphaBackground = isAlphaBackground;
   }

  
   /**
    * Method that does the work
    */
   public void run() {
      //for (int n=0; n<300; n++) {  // FOR MEMORY LEAK TESTING
      try {
         //for (int i=0; i<comps.length; i++) {
         //   comps[i].setEnabled(false);
         //}
         nexview.setProgressBarToLoading(true);
         nexview.loadFile(nexrad_url, clearAlphanumeric, isAlphaBackground, false, resetNexradExtent);
         nexview.updateMemoryLabel();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         //for (int i=0; i<comps.length; i++) {
         //   comps[i].setEnabled(true);
         //}
      }
      //}
      
   } // END METHOD run()

   
   
} // END CLASS
