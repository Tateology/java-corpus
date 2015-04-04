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

package gov.noaa.ncdc.test;

import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;

import java.io.File;


/**
 *  Compares lat, lon and alt from Level-III Headers to NCDC Radar Metadata
 *
 * @author    steve.ansari
 */
public class CompareMetadata {

   /**
    *  Directory of N0R file from every available WSR
    */
   public static String datadir = "C:\\ViewerData\\testdata";


   
   public static void outputMetadata() {
        
      DecodeL3Header header = new DecodeL3Header();
      try {
         
         File[] files = new File(datadir).listFiles();
         for (int n=0; n<files.length; n++) {
            header.decodeHeader(files[n].toURL());
            System.out.println(header.getICAO() + "," + header.getLat() + ","+ header.getLon() + "," + header.getAlt());
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
   }


   
   
   public static void main(String[] args) {
      outputMetadata();
   }

}

