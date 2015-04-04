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

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.io.FileScanner;

public interface WCTUiInterface {
   
   
   public static final int VIEWER = 0;
   public static final int EXPORT = 1;
   public static final int MATH = 2;
   public static final int EASYJNX = 3;
   public static final int EXPORTBATCH = 4;
   
   public int getWCTType();
   
   public FileScanner getFileScanner();
   public DecodeRadialDatasetSweepHeader getRadialHeader();
   public DecodeL3Header getLevel3Header();
   public DecodeRadialDatasetSweep getRadialDecoder();
   public DecodeL3Nexrad getLevel3Decoder();
   public WCTFilter getFilter();
//   public WCTFilterGUI getFilterGUI();

}
