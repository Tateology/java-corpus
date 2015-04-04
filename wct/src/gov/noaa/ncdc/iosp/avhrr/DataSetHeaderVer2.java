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
/**
import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;

public class DataSetHeaderVer2 {
    
    // General Information
    private String dataSetCreationSiteID;	//3 byte char
    private int formatVersion;				//2 byte unsigned int
    private int formatVersionYear;			//2 byte unsigned int
    private int formatVersionDayOfYear;		//2 byte unsigned int
    private int logicalRecordLength;		//2 byte unsigned int
    private int blockSize;					//2 byte unsigned int
    private int headerRecordCount;			//2 byte unsigned int
    private String dataSetName;				//42 byte char
    private String processingBlockID;		//8 byte char
    private int spacecraftID;				//2 byte unsigned int
    private int instrumentID;				//2 byte unsigned int
    private int dataTypeID;					//2 byte unsigned int
    private int tipSourceCode;				//2 byte unsigned int
    private long startDayCount;				//4 byte unsigned int
    private int startYear;					//2 byte unsigned int
    private int startDayOfYear;				//2 byte unsigned int
    private long startTime;     			//4 byte unsigned int
    private long endDayCount;				//4 byte unsigned int
    private int endYear;					//2 byte unsigned int
    private int endDayOfYear;				//2 byte unsigned int
    private long endTime;     				//4 byte unsigned int
    private int cpidsYear;					//2 byte unsigned int
    private int cpidsDayOfYear; 			//2 byte unsigned int
	
    // Data Set Quality Indicators
    private int instrumentStatus;			//4 byte unsigned int - (contains 15 bit level indicators)
    private int recordOfStatusChange;		//2 byte unsigned int
    private long secondInstrumentstatus;	//4 byte unsigned int(long)
    private int dataRecordCounts;			//2 byte unsigned int
    private int calibratedScans;			//2 byte unsigned int
    private int missingLinesCount;			//2 byte unsigned int
    private int dataGapsCount;				//2 byte unsigned int
    private int framesWithoutSyncE;			//2 byte unsigned int
    private int pacsCount;					//2 byte unsigned int
    private int sumAuxErrors;				//2 byte unsigned int
    private int timeError;					//2 byte unsigned int
    private short timeErrorCode;			//2 byte unsigned int (contains 4 1-bit indicators)
    private int soccClockUpdate;			//2 byte unsigned int
    private int earthLocationError;			//2 byte unsigned int
    private short earthLocationErrorCode;	//2 byte unsigned int (contains 4 1-bit indicators)
    private short pacsStatus;				//2 byte unsigned int (contains 3 1-bit indicators)
    private int pacsDataSource;				//2 byte unsigned int
    private String ingester;				//8 byte char
    private String decommutation;			//8 byte char
	
    // Calibration
    private short rampCalibrationSignal;	//2 byte unsigned int (contains 6 1-bit indicators)
    private int solarChCalibrationYear;		//2 byte unsigned int
    private int solarChCalibrationDayOfYear;//2 byte unsinged int
    private int algorithmIDPrimary;			//2 byte unsigned int
    private short algorithmOptionsPrimary;	//2 byte unsigned int (contains 9 1-bit indicators)
    private int algorithmIDSecondary;		//2 byte ungigned int
    private short algorithmOptionsSecondary;//2 byte unsigned int (contains 9 1-bit indicators)
    private short[] irTargetTemp1;			//(6) 2 byte singed ints
    private short[] irTargetTemp2;    		//(6) 2 byte singed ints
    private short[] irTargetTemp3;        	//(6) 2 byte singed ints
    private short[] irTargetTemp4;			//(6) 2 byte singed ints
    //skip 8 bytes
    
    
    // Radiance Conversion
    private int[] solarIrradiance = new int[3]; 		//(4) 4 byte ints
    private int[] equivalentWidth = new int[3];			//(4) 4 byte ints
    private int[] centralWavenumber = new int[3];		//(4) 4 byte ints
    private int[] constant1 = new int[3];				//(4) 4 byte ints
    private int[] constant2 = new int[3];  				//(4) 4 byte ints
    
    // Navigation 	
    private String referenceElipsoid;				//8 byte char
    private int nadirEarthLocationTolerance;		//2 byte unsigned
    private int earthLocationBitField;				//2 byte unsigned (contains 2 1-bit indicators)
    private int rollAttitudeError;					//2 byte int
    private int pitchAttitudeError;					//2 byte int
    private int yawAttitudeError;					//2 byte int
    private int vectorYear;							//2 byte unsigned
    private int vectorDay;							//2 byte unsigned
    private long vectorTimeOfDay;					//4 byte unsigned
    private int semiMajorAxis;						//4 byte int
    private int eccentricity;						//4 byte int
    private int inclination;						//4 byte int
    private int argumentOfPerigee;					//4 byte int
    private int rightAscension;						//4 byte int
    private int meanAnomaly;						//4 byte int
    private int positionVectorXComponent;			//4 byte int
    private int positionVectorYComponent;			//4 byte int
    private int positionVectorZComponent;			//4 byte int
    private int velocityVectorXComponent;			//4 byte int
    private int velocityVectorYComponent;			//4 byte int
    private int velocityVectorZComponent;			//4 byte unsigned
    private long earthSunDistanceRatio;    			//4 byte int
    //skip 4 bytes
    
    //Analog Telemetry Conversions
    int[] patchTempConverCoeff = new int[5];  		//2 byte ints
    int[] patchTempExtConverCoeff = new int[5]; 	//2 byte ints
    int[] patchPowerConverCoeff = new int[5];		//2 byte ints
    int[] radiatorTempConverCoeff = new int[5];		//2 byte ints
    int[] blackBodyTemp1Coeff = new int[5];			//2 byte ints
    int[] blackBodyTemp2Coeff = new int[5];			//2 byte ints
    int[] blackBodyTemp3Coeff = new int[5];			//2 byte ints
    int[] blackBodyTemp4Coeff = new int[5];			//2 byte ints
    int[] electronicsCurrentCoeff = new int[5]; 	//2 byte ints
    int[] motorCurrentCoeff = new int[5];			//2 byte ints
    int[] earthShieldPosCoeff = new int[5];			//2 byte ints
    int[] electronicsTempCoeff = new int[5];		//2 byte ints
    int[] coolerHousingTempCoeff = new int[5];		//2 byte ints
    int[] baseplateTempCoeff = new int[5];			//2 byte ints
    int[] motorHousingTempCoeff = new int[5];		//2 byte ints
    int[] ADconverterTempCoeff = new int[5];		//2 byte ints
    int[] detector4BiasVoltageCoeff = new int[5];	//2 byte ints
    int[] detector5BiasVoltageCoeff = new int[5];	//2 byte ints
    int[] chan3bBlackBodyViewCoeff = new int[5];	//2 byte ints
    int[] chan4BlackBodyViewCoeff = new int[5];		//2 byte ints
    int[] chan5BlackBodyViewCoeff = new int[5];		//2 byte ints
    int[] refVoltageCoeff = new int[5];				//2 byte ints
	*/

import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;

    /**
    * This program reads a GAC AVHRR L1B Header Record given the file path and prints results 
    * to the screen. 
    *
    * Note: Current version of code is for NOAA L1B Version 2, but should handle other Header 
    * Record Format Versions. 
    *
    * @author Philip Jones
    * @since 2006-12-13
    */

    public class DataSetHeaderVer2 extends GACHeader{       
        
        // ANALOG TELEMETRY CONVERSION COEFFICIENTS        
        public short[] patchTemp = new short[5];
        public short[] patchTempExt = new short[5];
        public short[] patchPower = new short[5];
        public short[] radiatorTemp = new short[5];
        public short[] blackbodyTemp1 = new short[5];
        public short[] blackbodyTemp2 = new short[5];
        public short[] blackbodyTemp3 = new short[5];
        public short[] blackbodyTemp4 = new short[5];
        public short[] electronicsCurrent = new short[5];
        public short[] motorCurrent = new short[5];
        public short[] earthShieldPosition = new short[5];
        public short[] electronicsTemp = new short[5];
        public short[] coolerHousingTemp = new short[5];
        public short[] baseplateTemp = new short[5];
        public short[] motorHousingTemp = new short[5];
        public short[] adConverterTemp = new short[5];
        public short[] detector4BiasVoltage = new short[5];
        public short[] detector5BiasVoltage = new short[5];
        public short[] ch3bBlackbodyView = new short[5];
        public short[] ch4BlackbodyView = new short[5];
        public short[] ch5BlackbodyView = new short[5];
        public short[] referenceVoltage = new short[5];
        
        // Filler
        private static int fillerSkip = 3920; // for 10 bit packed data, =4944 for unpacked 8 or 16 bit per pixel data       
       
        public void readHeader(ucar.unidata.io.RandomAccessFile raf){
            
            try {

                // Check if ARS Header exist                        
                if (ArsHeader.hasARSHeader(raf)){   
                    // Seek to bytes in Header Record to read Header Record Count                
                    raf.seek((ARS_LENGTH + HR_COUNT_POS)-1);               
                    headerRecordCount = raf.readUnsignedShort();
//                    System.out.println("headerRecordCount: "+headerRecordCount);
                    // Seek to beginning of (first/only) Header Record            
                    raf.seek(ARS_LENGTH);

                }else{
                    // Seek to bytes in Header Record to read Header Record Count                
                    raf.seek((HR_COUNT_POS)-1);               
                    headerRecordCount = raf.readUnsignedShort();
 //                   System.out.println("headerRecordCount: "+headerRecordCount);
                    // Seek to beginning of (first/only) Header Record                
                    raf.seek(0);    
                } 
                // Loop through and read Header Record(s)
                
                for (int i = 0; i < headerRecordCount; i++){                
                    
                    // General Information
                    readGeneralInfo(raf);
                    
                    // Data Set Quality Indicators
                    readDataSetQuality(raf);
                    
                    // Calibration
                    readCalibration(raf);
           
                    // Radiance Conversion
                    readRadianceConversion(raf);
                    // Navigation 
                    readNavigation(raf);

                    // ANALOG TELEMETRY CONVERSION COEFFICIENTS                  
                    patchTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    patchTempExt = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    patchPower = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    radiatorTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    blackbodyTemp1 = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    blackbodyTemp2 = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    blackbodyTemp3 = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    blackbodyTemp4 = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    electronicsCurrent = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    motorCurrent = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    earthShieldPosition = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    electronicsTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    coolerHousingTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    baseplateTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    motorHousingTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    adConverterTemp = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    detector4BiasVoltage = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    detector5BiasVoltage = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    ch3bBlackbodyView = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    ch4BlackbodyView = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    ch5BlackbodyView = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    referenceVoltage  = readShortArray(raf, 5);
                    raf.skipBytes(2);
                    
                    // Filler
                    
                    raf.skipBytes(fillerSkip);                 

                }   // end for loop read of Header Record            
 //               raf.close();
            }catch(IOException ioe){
            	ioe.printStackTrace();
            }
            finally {
//                raf.close();
            }             
        }

		@Override
	void readEarthLocationError(RandomAccessFile raf) throws IOException {
//		System.out.println("DataSetHeaderVer2 >> readEarthLocationError");
		earthLocationError = raf.readUnsignedShort();
		earthLocationErrorCode = raf.readShort();
		earthLocationErrorCode4 = (byte)readOneBitFlag(earthLocationErrorCode, 4);
		earthLocationErrorCode5 = (byte)readOneBitFlag(earthLocationErrorCode, 5);
		earthLocationErrorCode6 = (byte)readOneBitFlag(earthLocationErrorCode, 6);
		earthLocationErrorCode7 = (byte)readOneBitFlag(earthLocationErrorCode, 7);

	}

	@Override
	void readNavEarthLocationBitField(ucar.unidata.io.RandomAccessFile raf) throws IOException {
//		System.out.println("DataSetHeaderVer2 >> readNavEarthLocationBitField");
		earthLocationBitField = raf.readShort();
		earthLocationBitField0 = (byte)readOneBitFlag(earthLocationBitField, 0);
		earthLocationBitField1 = (byte)readOneBitFlag(earthLocationBitField, 1);
	}
        
    }         
    
