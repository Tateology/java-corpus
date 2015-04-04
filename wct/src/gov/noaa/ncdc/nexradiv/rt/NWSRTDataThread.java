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

package gov.noaa.ncdc.nexradiv.rt;


import java.net.URL;

public class NWSRTDataThread extends Thread 
{
   boolean theEnd = false;
   String saveDirectory, name;
   URL nexrad_url;
   NWSRTData nwsRTData;
   

   public NWSRTDataThread(String name, String saveDirectory, URL nexrad_url, NWSRTData nwsRTData) {
      this.name = name;
      this.saveDirectory = saveDirectory;
      this.nexrad_url = nexrad_url;
      this.nwsRTData = nwsRTData;
   }
   
   
   /**
    * Method that starts the thread
    */
   public void run() {
      
      String nwsSite = nwsRTData.getSelectedSite();
      String nwsProduct = nwsRTData.getSelectedProduct();
         
      theEnd = false;      
      while (! theEnd) {
         // Get latest file
         System.out.println("GETTING: "+nexrad_url);
         nwsRTData.downloadNexrad(saveDirectory, nexrad_url, nwsSite, nwsProduct);
         // Wait 1 minute and try again
         try {
            Thread.sleep(1000*60);
         } catch (Exception e) {
            e.printStackTrace();
         }      
      }
   } // END METHOD run()


   /**
    * Method that gracefully ends the tread 
    */
   public void endThread() {
      theEnd = true;
   } // END METHOD endThread()

   /**
    * Method that restarts the tread 
    */
   public void restartThread() {
      theEnd = false;
      start();
   } // END METHOD restartThread()

   public String getThreadName() {
      return name;
   }
   
   public void setThreadName(String name) {
      this.name = name;
   }

} // END CLASS
