/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/
package gov.noaa.ncdc.iosp.avhrr;

import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;

/**
* This program reads a GAC AVHRR L1B Data Record given the file path and prints some results 
* to the screen. 
*
* Note: Current version of code is for NOAA L1B Version 2, but it should handle other Data 
* Record Format Versions. 
*
* @author Philip Jones
* @since 2006-12-13
*/

public class DataRecordVer2 extends GACDataRecord{   
    
    // Data record to start read 
    private static int init = 1;     // init >= 1
    
    // Quality Indicators
    public int qualityIndicatorBitField;
    public byte qualityIndicatorBitField0;
    public byte qualityIndicatorBitField1;
    public byte qualityIndicatorBitField2;
    public byte qualityIndicatorBitField4;
    public byte qualityIndicatorBitField6;
    public byte qualityIndicatorBitField8;
    public byte qualityIndicatorBitField20;
    public byte qualityIndicatorBitField21;
    public byte qualityIndicatorBitField22;
    public byte qualityIndicatorBitField23;
    public byte qualityIndicatorBitField24;
    public byte qualityIndicatorBitField25;
    public byte qualityIndicatorBitField26;
    public byte qualityIndicatorBitField27;
    public byte qualityIndicatorBitField28;
    public byte qualityIndicatorBitField29;
    public byte qualityIndicatorBitField30;    
    public int scanLineQualityFlags;
    public byte scanLineQualityFlags4;
    public byte scanLineQualityFlags5;
    public byte scanLineQualityFlags6;
    public byte scanLineQualityFlags7;
    public byte scanLineQualityFlags11;
    public byte scanLineQualityFlags12;
    public byte scanLineQualityFlags13;
    public byte scanLineQualityFlags14;
    public byte scanLineQualityFlags15;
    public byte scanLineQualityFlags20;
    public byte scanLineQualityFlags21;
    public byte scanLineQualityFlags22;
    public byte scanLineQualityFlags23;
    public short calibrationQualityFlags;

    // Extra dimension for IR channels    
    public byte[] calibrationQualityFlags1 = new byte[3];
    public byte[] calibrationQualityFlags2 = new byte[3];
    public byte[] calibrationQualityFlags4 = new byte[3];
    public byte[] calibrationQualityFlags5 = new byte[3];
    public byte[] calibrationQualityFlags6 = new byte[3];
    public byte[] calibrationQualityFlags7 = new byte[3];
    public int frameSyncErrorCount;
    
    // Navigation 
    
    public int navigationStatusBitField;
    public byte navigationStatusBitField0;
    public byte navigationStatusBitField4;
    public byte navigationStatusBitField8;
    public byte navigationStatusBitField12;
    public byte navigationStatusBitField16;
    public byte navigationStatusBitField17;
    public byte navigationStatusBitField19;  
    
    boolean hasCalibrationErrors = false;
    
    // Data Record Filler
    
    private static int fillerSkip = 448;    

        
        public void readScanLine(ucar.unidata.io.RandomAccessFile raf){
        	try{
               
                readScanLineInfo(raf);
                // Quality Indicators
                qualityIndicatorBitField = raf.readInt();
                if(qualityIndicatorBitField != 0){
                	hasCalibrationErrors = true;
//                	System.out.println("quality Indicator error: " + scanLine);
                }
                qualityIndicatorBitField0 = (byte)readOneBitFlag(qualityIndicatorBitField, 0);
                qualityIndicatorBitField1 = (byte) readOneBitFlag(qualityIndicatorBitField, 1);
                qualityIndicatorBitField2 = (byte)readTwoBitFlag(qualityIndicatorBitField, 2);
                qualityIndicatorBitField4 = (byte)readTwoBitFlag(qualityIndicatorBitField, 4);
                qualityIndicatorBitField6 = (byte)readTwoBitFlag(qualityIndicatorBitField, 6);    
                qualityIndicatorBitField8 = (byte)readOneBitFlag(qualityIndicatorBitField, 8);
                qualityIndicatorBitField20 = (byte)readOneBitFlag(qualityIndicatorBitField, 20);
                qualityIndicatorBitField21 = (byte)readOneBitFlag(qualityIndicatorBitField, 21);
                qualityIndicatorBitField22 = (byte)readOneBitFlag(qualityIndicatorBitField, 22);
                qualityIndicatorBitField23 = (byte)readOneBitFlag(qualityIndicatorBitField, 23);
                qualityIndicatorBitField24 = (byte)readOneBitFlag(qualityIndicatorBitField, 24);
                qualityIndicatorBitField25 = (byte)readOneBitFlag(qualityIndicatorBitField, 25);
                qualityIndicatorBitField26 = (byte)readOneBitFlag(qualityIndicatorBitField, 26);
                qualityIndicatorBitField27 = (byte)readOneBitFlag(qualityIndicatorBitField, 27);
                qualityIndicatorBitField28 = (byte)readOneBitFlag(qualityIndicatorBitField, 28);
                qualityIndicatorBitField29 = (byte)readOneBitFlag(qualityIndicatorBitField, 29);
                qualityIndicatorBitField30 = (byte)readOneBitFlag(qualityIndicatorBitField, 30);                
                scanLineQualityFlags = raf.readInt();
                if(scanLineQualityFlags != 0){
                	hasCalibrationErrors = true;
//                	System.out.println("scan line has errors: " + scanLine);
                }
                scanLineQualityFlags4 = (byte)readOneBitFlag(scanLineQualityFlags, 30);
                scanLineQualityFlags5 = (byte)readOneBitFlag(scanLineQualityFlags, 5);
                scanLineQualityFlags6 = (byte)readOneBitFlag(scanLineQualityFlags, 6);
                scanLineQualityFlags7 = (byte)readOneBitFlag(scanLineQualityFlags, 7);
                scanLineQualityFlags11 = (byte)readOneBitFlag(scanLineQualityFlags, 11);
                scanLineQualityFlags12 = (byte)readOneBitFlag(scanLineQualityFlags, 12);
                scanLineQualityFlags13 = (byte)readOneBitFlag(scanLineQualityFlags, 13);
                scanLineQualityFlags14 = (byte)readOneBitFlag(scanLineQualityFlags, 14);
                scanLineQualityFlags15 = (byte)readOneBitFlag(scanLineQualityFlags, 15);
                scanLineQualityFlags20 = (byte)readOneBitFlag(scanLineQualityFlags, 20);
                scanLineQualityFlags21 = (byte)readOneBitFlag(scanLineQualityFlags, 21);
                scanLineQualityFlags22 = (byte)readOneBitFlag(scanLineQualityFlags, 22);
                scanLineQualityFlags23 = (byte)readOneBitFlag(scanLineQualityFlags, 23);  
                
                for (int k = 0; k < 3; k++){        // IR channels 3b, 4, 5
                    calibrationQualityFlags = raf.readShort();
                    if(calibrationQualityFlags != 0){
                    	hasCalibrationErrors = true; 
//                    	System.out.println("calibrationQualityFlags: " + scanLine);
                    }
                    calibrationQualityFlags1[k] = (byte)readOneBitFlag(calibrationQualityFlags, 1); 
                    calibrationQualityFlags2[k] = (byte)readOneBitFlag(calibrationQualityFlags, 2);
                    calibrationQualityFlags4[k] = (byte)readOneBitFlag(calibrationQualityFlags, 4);
                    calibrationQualityFlags5[k] = (byte)readOneBitFlag(calibrationQualityFlags, 5);
                    calibrationQualityFlags6[k] = (byte)readOneBitFlag(calibrationQualityFlags, 6);
                    calibrationQualityFlags7[k] = (byte)readOneBitFlag(calibrationQualityFlags, 7);
                }
                
                frameSyncErrorCount = raf.readUnsignedShort();    
                if(frameSyncErrorCount != 0){
                	hasCalibrationErrors = true;
//                	System.out.println("frameSyncErrorCount error: " + scanLine);
                }
                raf.skipBytes(8);
                                    
                // Calibration Coefficients
                readCalibrationCoeffs(raf);

                // Navigation 
                readNavigation(raf);
                                
                //HRPT Minor Frame Telemetry (Skip)
                readHRPTMinorFrameTelemetry(raf);
              
                // AVHRR Sensor Data                        
                readEarthObservations(raf);
                                
                // Digital B Telemetry
                readDigitalBHouseKeeping(raf);
                
                // Analog Housekeeping Data (TIP)
                readAnalogHouseKeepint(raf);
                
                // Clouds From AVHRR (CLAVR)
                readClavrData(raf);
                                                
                // Data Record Filler      
                raf.skipBytes(fillerSkip); 
        }catch(Exception e){
        	e.printStackTrace();
        }
        }

		@Override
		void readNavigationStatusBitField(RandomAccessFile raf) throws IOException {
            navigationStatusBitField = raf.readInt();
            navigationStatusBitField0 = (byte)readFourBitFlag(navigationStatusBitField, 0);
            navigationStatusBitField4 = (byte)readFourBitFlag(navigationStatusBitField, 4);
            navigationStatusBitField8 = (byte)readFourBitFlag(navigationStatusBitField, 8);
            navigationStatusBitField12 = (byte)readFourBitFlag(navigationStatusBitField, 12);
            navigationStatusBitField16 = (byte)readOneBitFlag(navigationStatusBitField, 16);   
            navigationStatusBitField17 = (byte)readOneBitFlag(navigationStatusBitField, 17);
            navigationStatusBitField19 = (byte)readTwoBitFlag(navigationStatusBitField, 19); 
		}

		public int getQualityIndicatorBitField() {
			return qualityIndicatorBitField;
		}

		public int getScanLineQualityFlags() {
			return scanLineQualityFlags;
		}

		public int getFrameSyncErrorCount() {
			return frameSyncErrorCount;
		}
		
		public boolean getHasCalibrationErrors(){
			return hasCalibrationErrors;
		}
}         
