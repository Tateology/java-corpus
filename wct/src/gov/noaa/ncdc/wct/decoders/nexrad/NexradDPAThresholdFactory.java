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

package gov.noaa.ncdc.wct.decoders.nexrad;

public class NexradDPAThresholdFactory {


   static public int getColorIndex(float value) {
            if (value > 0.0 && value < 0.10) {
               return 1;
            }
            else if (value >= 0.10 && value < 0.25) {
               return 2;
            }
            else if (value >= 0.25 && value < 0.50) {
               return 3;
            }
            else if (value >= 0.50 && value < 0.75) {
               return 4;
            }
            else if (value >= 0.75 && value < 1.00) {
               return 5;
            }
            else if (value >= 1.00 && value < 1.50) {
               return 6;
            }
            else if (value >= 1.50 && value < 2.00) {
               return 7;
            }
            else if (value >= 2.00 && value < 2.50) {
               return 8;
            }
            else if (value >= 2.50 && value < 3.00) {
               return 9;
            }
            else if (value >= 3.00 && value < 4.00) {
               return 10;
            }
            else if (value >= 4.00) {
               return 11;
            }
            else {
               return 0;
            }
      
   } // END static getUnits(nexradProductCode)            
} // END class
