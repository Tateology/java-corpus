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

    /**
    * NOAA L1B Version Header Data.  This abstract class contains data common to version GAC headers
    *
    * @author Arthur Fotos
    * @since 2007-06-06
    */

    public abstract class GACHeader extends AvhrrFile implements AvhrrConstants{       

        // General Information
        public String dataSetCreationSiteID;
        public int formatVersion;
        public int formatVersionYear;
        public int formatVersionDayOfYear;
        public int logicalRecordLength;
        public int blockSize;
        public int headerRecordCount;
        public String dataSetName;
        public String processingBlockID;
        public int spacecraftID;
        public int instrumentID;
        public int dataTypeID;
        public int tipSourceCode;
        public long startDayCount;
        public int startYear;
        public int startDayOfYear;
        public long startTime;     
        public long endDayCount;
        public int endYear;
        public int endDayOfYear;
        public long endTime;     
        public int cpidsYear;
        public int cpidsDayOfYear;   
        
        // Data Set Quality Indicators
        public int instrumentStatus;
        public byte instrumentStatus1;
        public byte instrumentStatus2;
        public byte instrumentStatus3;   
        public byte instrumentStatus4;
        public byte instrumentStatus5;
        public byte instrumentStatus6;
        public byte instrumentStatus7;
        public byte instrumentStatus8;
        public byte instrumentStatus9;
        public byte instrumentStatus10;
        public byte instrumentStatus11;
        public byte instrumentStatus12;
        public byte instrumentStatus13;
        public byte instrumentStatus14;
        public byte instrumentStatus15;        
        public int recordOfStatusChange;
        public long secondInstrumentstatus;
        public int dataRecordCounts;
        public int calibratedScans;
        public int missingLinesCount;
        public int dataGapsCount;
        public int framesWithoutSyncE;
        public int pacsCount;
        public int sumAuxErrors;
        public int timeError;
        public short timeErrorCode;
        public int timeErrorCode4;
        public int timeErrorCode5;
        public int timeErrorCode6;
        public int timeErrorCode7;        
        public int soccClockUpdate;
        public int earthLocationError;
        public short earthLocationErrorCode;
        public byte earthLocationErrorCode0;
        public byte earthLocationErrorCode1;
        public byte earthLocationErrorCode4;
        public byte earthLocationErrorCode5;
        public byte earthLocationErrorCode6;
        public byte earthLocationErrorCode7;         
        public short pacsStatus;
        public int pacsStatus0;
        public int pacsStatus1;
        public int pacsStatus2;        
        public int pacsDataSource;
        public String ingester;
        public String decommutation;
        
        // Calibration
        public short rampCalibrationSignal;
        public byte rampCalibrationSignal0;
        public byte rampCalibrationSignal1;
        public byte rampCalibrationSignal2;
        public byte rampCalibrationSignal3;
        public byte rampCalibrationSignal4;
        public byte rampCalibrationSignal5;         
        public int solarChCalibrationYear;
        public int solarChCalibrationDayOfYear;
        public int algorithmIDPrimary;
        public short algorithmOptionsPrimary;
        public byte algorithmOptionsPrimary3;
        public byte algorithmOptionsPrimary4;
        public byte algorithmOptionsPrimary8;
        public byte algorithmOptionsPrimary9;
        public byte algorithmOptionsPrimary13;
        public byte algorithmOptionsPrimary14;          
        public int algorithmIDSecondary;
        public short algorithmOptionsSecondary;
        public byte algorithmOptionsSecondary3;
        public byte algorithmOptionsSecondary4;
        public byte algorithmOptionsSecondary8;
        public byte algorithmOptionsSecondary9;
        public byte algorithmOptionsSecondary13;
        public byte algorithmOptionsSecondary14;        
        public short[] irTargetTemp1 = new short[6];
        public short[] irTargetTemp2 = new short[6];    
        public short[] irTargetTemp3 = new short[6];
        public short[] irTargetTemp4 = new short[6]; 
        
        // Navigation 
        public String referenceElipsoid;
        public int nadirEarthLocationTolerance;
        public int earthLocationBitField;
        public byte earthLocationBitField0;
        public byte earthLocationBitField1;  
        public byte earthLocationBitField2;
        public short rollAttitudeError;
        public short pitchAttitudeError;
        public short yawAttitudeError;
        public int vectorYear;
        public int vectorDay;
        public long vectorTimeOfDay;
        public int semiMajorAxis;
        public int eccentricity;
        public int inclination;
        public int argumentOfPerigee;
        public int rightAscension;
        public int meanAnomaly;
        public int positionVectorXComponent;
        public int positionVectorYComponent;
        public int positionVectorZComponent;
        public int velocityVectorXComponent;
        public int velocityVectorYComponent;
        public int velocityVectorZComponent;
        public long earthSunDistanceRatio;
        
        // Radiance Conversion
        public int[] solarIrradiance = new int[3];
        public int[] equivalentWidth = new int[3];
        public int[] centralWavenumber = new int[3];
        public int[] constant1 = new int[3];
        public int[] constant2 = new int[3]; 
        
        //Clouds From AVHRR (CLAVR)
        int clavrStatusField;
        byte clavrStatus;
        
        private static int fillerSkip = 3920; // for 10 bit packed data, =4944 for unpacked 8 or 16 bit per pixel data       
         
        public int getNumberOfHeaders(ucar.unidata.io.RandomAccessFile raf){
        	try{
//                System.out.println("raf point pos: " + raf.getFilePointer());
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
//                    System.out.println("headerRecordCount: "+headerRecordCount);
                    // Seek to beginning of (first/only) Header Record                
                    raf.seek(0);    
                }        		
        	}catch(IOException ioe){
        		ioe.printStackTrace();
        	}
        	return headerRecordCount;
        }
        
        public void readHeader(ucar.unidata.io.RandomAccessFile raf){
            
            try {
//                System.out.println("raf point pos: " + raf.getFilePointer());
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
                    readAnalogTelemetryCoeffs(raf);

                    
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
        

		public int getHeaderRecordCount() {
			return headerRecordCount;
		}

		public int getDataRecordCounts() {
			return dataRecordCounts;
		}

		public String getDataSetCreationSiteID() {
			return dataSetCreationSiteID;
		}

		public String getDataSetName() {
			return dataSetName;
		}

		public int getFormatVersion() {
			return formatVersion;
		}

		public int getFormatVersionDayOfYear() {
			return formatVersionDayOfYear;
		}

		public int getFormatVersionYear() {
			return formatVersionYear;
		}

		public void setHeaderRecordCount(int headerRecordCount) {
			this.headerRecordCount = headerRecordCount;
		}

		public int getBlockSize() {
			return blockSize;
		}

		public int getCpidsDayOfYear() {
			return cpidsDayOfYear;
		}

		public int getCpidsYear() {
			return cpidsYear;
		}

		public int getLogicalRecordLength() {
			return logicalRecordLength;
		}

		public int getEarthLocationError() {
			return earthLocationError;
		}

		public int getEarthLocationErrorCode4() {
			return earthLocationErrorCode4;
		}

		public int getEarthLocationErrorCode5() {
			return earthLocationErrorCode5;
		}

		public int getEarthLocationErrorCode6() {
			return earthLocationErrorCode6;
		}

		public int getEarthLocationErrorCode7() {
			return earthLocationErrorCode7;
		}

		public int[] getSolarIrradiance() {
			return solarIrradiance;
		}

		public int[] getCentralWavenumber() {
			return centralWavenumber;
		}

		public int[] getConstant1() {
			return constant1;
		}

		public int[] getConstant2() {
			return constant2;
		}

		public int[] getEquivalentWidth() {
			return equivalentWidth;
		}

		
		protected void readGeneralInfo(ucar.unidata.io.RandomAccessFile raf) throws IOException{
            dataSetCreationSiteID = readString(raf, 3);
            raf.skipBytes(1);
            formatVersion = raf.readUnsignedShort();         
            formatVersionYear = raf.readUnsignedShort();
            formatVersionDayOfYear = raf.readUnsignedShort();
            logicalRecordLength = raf.readUnsignedShort();
            blockSize = raf.readUnsignedShort();
            headerRecordCount = raf.readUnsignedShort();
            raf.skipBytes(6);
            dataSetName = readString(raf, 42);
            processingBlockID = readString(raf, 8);
            spacecraftID = raf.readUnsignedShort();
            instrumentID = raf.readUnsignedShort();
            dataTypeID = raf.readUnsignedShort();
            tipSourceCode = raf.readUnsignedShort();
            startDayCount = readUnsignedInt(raf);
            startYear = raf.readUnsignedShort();
            startDayOfYear = raf.readUnsignedShort();
            startTime = readUnsignedInt(raf);     
            endDayCount = readUnsignedInt(raf);
            endYear = raf.readUnsignedShort();
            endDayOfYear = raf.readUnsignedShort();
            endTime = readUnsignedInt(raf);     
            cpidsYear = raf.readUnsignedShort();
            cpidsDayOfYear = raf.readUnsignedShort();
            raf.skipBytes(8);			
		}
        
		protected void readDataSetQuality(ucar.unidata.io.RandomAccessFile raf) throws IOException{
            instrumentStatus = raf.readInt();
            instrumentStatus1 = (byte)readOneBitFlag(instrumentStatus, 1);
            instrumentStatus2 = (byte)readOneBitFlag(instrumentStatus, 2);
            instrumentStatus3 = (byte)readOneBitFlag(instrumentStatus, 3);   
            instrumentStatus4 = (byte)readOneBitFlag(instrumentStatus, 4);
            instrumentStatus5 = (byte)readOneBitFlag(instrumentStatus, 5);
            instrumentStatus6 = (byte)readOneBitFlag(instrumentStatus, 6);
            instrumentStatus7 = (byte)readOneBitFlag(instrumentStatus, 7);
            instrumentStatus8 = (byte)readOneBitFlag(instrumentStatus, 8);
            instrumentStatus9 = (byte)readOneBitFlag(instrumentStatus, 9);
            instrumentStatus10 = (byte)readOneBitFlag(instrumentStatus, 10);
            instrumentStatus11 = (byte)readOneBitFlag(instrumentStatus, 11);
            instrumentStatus12 = (byte)readOneBitFlag(instrumentStatus, 12);
            instrumentStatus13 = (byte)readOneBitFlag(instrumentStatus, 13);
            instrumentStatus14 = (byte)readOneBitFlag(instrumentStatus, 14);
            instrumentStatus15 = (byte)readOneBitFlag(instrumentStatus, 15);               
            raf.skipBytes(2);
            recordOfStatusChange = raf.readUnsignedShort();
            // check for status change and vars for secondInstrument status
            secondInstrumentstatus = readUnsignedInt(raf);                
            dataRecordCounts = raf.readUnsignedShort();
//            System.out.println("DataRecordCounts: " + dataRecordCounts);
            calibratedScans = raf.readUnsignedShort();
//            System.out.println("CalibratedScans: " + calibratedScans);
            missingLinesCount = raf.readUnsignedShort();
//            System.out.println("MissingLinesCount: " + missingLinesCount);
            dataGapsCount = raf.readUnsignedShort();
//            System.out.println("DataGapCounts: " + dataGapsCount);
            framesWithoutSyncE = raf.readUnsignedShort();
            pacsCount = raf.readUnsignedShort();
            sumAuxErrors = raf.readUnsignedShort();
            timeError = raf.readUnsignedShort();
            timeErrorCode = raf.readShort();
            timeErrorCode4 = readOneBitFlag(timeErrorCode, 4);
            timeErrorCode5 = readOneBitFlag(timeErrorCode, 5);
            timeErrorCode6 = readOneBitFlag(timeErrorCode, 6);
            timeErrorCode7 = readOneBitFlag(timeErrorCode, 7);                
            soccClockUpdate = raf.readUnsignedShort();
            
            readEarthLocationError(raf);
//            earthLocationError = raf.readUnsignedShort();
//            earthLocationErrorCode = raf.readShort();
//            earthLocationErrorCode4 = readOneBitFlag(earthLocationErrorCode, 4);
//            earthLocationErrorCode5 = readOneBitFlag(earthLocationErrorCode, 5);
//            earthLocationErrorCode6 = readOneBitFlag(earthLocationErrorCode, 6);
//            earthLocationErrorCode7 = readOneBitFlag(earthLocationErrorCode, 7);                
            
            pacsStatus = raf.readShort();
            pacsStatus0 = (byte)readOneBitFlag(pacsStatus, 0);
            pacsStatus1 = (byte)readOneBitFlag(pacsStatus, 1);
            pacsStatus2 = (byte)readOneBitFlag(pacsStatus, 2);
            pacsDataSource = raf.readUnsignedShort();
            raf.skipBytes(4);
            ingester = readString(raf, 8);
            decommutation = readString(raf, 8);
            raf.skipBytes(10);
		}
		
		protected void readCalibration(ucar.unidata.io.RandomAccessFile raf) throws IOException{
            
            rampCalibrationSignal = raf.readShort();
            rampCalibrationSignal0 = (byte)readOneBitFlag(rampCalibrationSignal, 0);
            rampCalibrationSignal1 = (byte)readOneBitFlag(rampCalibrationSignal, 1);
            rampCalibrationSignal2 = (byte)readOneBitFlag(rampCalibrationSignal, 2);
            rampCalibrationSignal3 = (byte)readOneBitFlag(rampCalibrationSignal, 3);
            rampCalibrationSignal4 = (byte)readOneBitFlag(rampCalibrationSignal, 4);
            rampCalibrationSignal5 = (byte)readOneBitFlag(rampCalibrationSignal, 5);                
            solarChCalibrationYear = raf.readUnsignedShort();
            solarChCalibrationDayOfYear = raf.readUnsignedShort();
            algorithmIDPrimary = raf.readUnsignedShort();
            algorithmOptionsPrimary = raf.readShort();
            algorithmOptionsPrimary3 = (byte)readOneBitFlag(algorithmOptionsPrimary, 3);
            algorithmOptionsPrimary4 = (byte)readOneBitFlag(algorithmOptionsPrimary, 4);
            algorithmOptionsPrimary8 = (byte)readOneBitFlag(algorithmOptionsPrimary, 8);
            algorithmOptionsPrimary9 = (byte)readOneBitFlag(algorithmOptionsPrimary, 9);
            algorithmOptionsPrimary13 = (byte)readOneBitFlag(algorithmOptionsPrimary, 13);
            algorithmOptionsPrimary14 = (byte)readOneBitFlag(algorithmOptionsPrimary, 14);                
            algorithmIDSecondary = raf.readUnsignedShort();
            algorithmOptionsSecondary = raf.readShort();
            algorithmOptionsSecondary3 = (byte)readOneBitFlag(algorithmOptionsSecondary, 3);
            algorithmOptionsSecondary4 = (byte)readOneBitFlag(algorithmOptionsSecondary, 4);
            algorithmOptionsSecondary8 = (byte)readOneBitFlag(algorithmOptionsSecondary, 8);
            algorithmOptionsSecondary9 = (byte)readOneBitFlag(algorithmOptionsSecondary, 9);
            algorithmOptionsSecondary13 = (byte)readOneBitFlag(algorithmOptionsSecondary, 13);
            algorithmOptionsSecondary14 = (byte)readOneBitFlag(algorithmOptionsSecondary, 14);                
            irTargetTemp1 = readShortArray(raf, 6);
            irTargetTemp2 = readShortArray(raf, 6);    
            irTargetTemp3 = readShortArray(raf, 6);        
            irTargetTemp4 = readShortArray(raf, 6);
            raf.skipBytes(8);			
		}
		
		protected void readRadianceConversion(ucar.unidata.io.RandomAccessFile raf) throws IOException{
            solarIrradiance[0] = raf.readInt();
            equivalentWidth[0] = raf.readInt();
            solarIrradiance[1] = raf.readInt();
            equivalentWidth[1] = raf.readInt();
            solarIrradiance[2] = raf.readInt();
            equivalentWidth[2] = raf.readInt();
            centralWavenumber[0] = raf.readInt();
            constant1[0] = raf.readInt();
            constant2[0] = raf.readInt();
            centralWavenumber[1] = raf.readInt();
            constant1[1] = raf.readInt();
            constant2[1] = raf.readInt();
            centralWavenumber[2] = raf.readInt();
            constant1[2] = raf.readInt();
            constant2[2] = raf.readInt();
            raf.skipBytes(12);			
		}
		
		protected void readNavigation(ucar.unidata.io.RandomAccessFile raf) throws IOException{
            referenceElipsoid = readString(raf, 8);
            nadirEarthLocationTolerance = raf.readUnsignedShort();
            readNavEarthLocationBitField(raf);
//            earthLocationBitField = raf.readShort();
//            earthLocationBitField0 = readOneBitFlag(earthLocationBitField, 0);
//            earthLocationBitField1 = readOneBitFlag(earthLocationBitField, 1);                
            raf.skipBytes(2);
            rollAttitudeError = raf.readShort();
            pitchAttitudeError = raf.readShort();
            yawAttitudeError = raf.readShort();
            vectorYear = raf.readUnsignedShort();
            vectorDay = raf.readUnsignedShort();
            vectorTimeOfDay = readUnsignedInt(raf);
            semiMajorAxis = raf.readInt();
            eccentricity = raf.readInt();
            inclination = raf.readInt();
            argumentOfPerigee = raf.readInt();
            rightAscension = raf.readInt();
            meanAnomaly = raf.readInt();
            positionVectorXComponent = raf.readInt();
            positionVectorYComponent = raf.readInt();
            positionVectorZComponent = raf.readInt();
            velocityVectorXComponent = raf.readInt();
            velocityVectorYComponent = raf.readInt();
            velocityVectorZComponent = raf.readInt();
            earthSunDistanceRatio = readUnsignedInt(raf);               
            raf.skipBytes(16);		
		}

		
		protected void readAnalogTelemetryCoeffs(ucar.unidata.io.RandomAccessFile raf) throws IOException{
			
		}
		
		protected void readClavr(ucar.unidata.io.RandomAccessFile raf) throws Exception{
			clavrStatusField = raf.readUnsignedShort();
			clavrStatus = (byte)readOneBitFlag(clavrStatusField, 0);
		}
		
		//Data Set Quality Indicators >> Earth Location Error Bit Field
		abstract void readEarthLocationError(ucar.unidata.io.RandomAccessFile raf) throws IOException;
		
		//Navigation >> Earth Location Bit Field
		abstract void readNavEarthLocationBitField(ucar.unidata.io.RandomAccessFile raf) throws IOException;

		public int getNadirEarthLocationTolerance() {
			return nadirEarthLocationTolerance;
		}

		public String getReferenceElipsoid() {
			return referenceElipsoid;
		}

		public int getSpacecraftID() {
			return spacecraftID;
		}

		public long getStartDayCount() {
			return startDayCount;
		}

		public int getStartDayOfYear() {
			return startDayOfYear;
		}

		public long getStartTime() {
			return startTime;
		}

		public int getStartYear() {
			return startYear;
		}

		public long getEarthSunDistanceRatio() {
			return earthSunDistanceRatio;
		}

		public int getEccentricity() {
			return eccentricity;
		}

		public int getArgumentOfPerigee() {
			return argumentOfPerigee;
		}

		public byte getClavrStatus() {
			return clavrStatus;
		}

		public int getClavrStatusField() {
			return clavrStatusField;
		}

		public int getEarthLocationBitField0() {
			return earthLocationBitField0;
		}

		public int getEarthLocationBitField1() {
			return earthLocationBitField1;
		}

		public int getMeanAnomaly() {
			return meanAnomaly;
		}

		public int getRightAscension() {
			return rightAscension;
		}

		public int getSemiMajorAxis() {
			return semiMajorAxis;
		}

		public int getInclination() {
			return inclination;
		}

		public int getPositionVectorXComponent() {
			return positionVectorXComponent;
		}

		public int getPositionVectorYComponent() {
			return positionVectorYComponent;
		}

		public int getPositionVectorZComponent() {
			return positionVectorZComponent;
		}

		public int getVelocityVectorXComponent() {
			return velocityVectorXComponent;
		}

		public int getVelocityVectorYComponent() {
			return velocityVectorYComponent;
		}

		public int getVelocityVectorZComponent() {
			return velocityVectorZComponent;
		}

		public short getRollAttitudeError() {
			return rollAttitudeError;
		}

		public short getPitchAttitudeError() {
			return pitchAttitudeError;
		}

		public int getVectorDay() {
			return vectorDay;
		}

		public long getVectorTimeOfDay() {
			return vectorTimeOfDay;
		}

		public int getVectorYear() {
			return vectorYear;
		}

		public short getYawAttitudeError() {
			return yawAttitudeError;
		}

		public byte getEarthLocationBitField2() {
			return earthLocationBitField2;
		}

		public byte getEarthLocationErrorCode0() {
			return earthLocationErrorCode0;
		}

		public byte getEarthLocationErrorCode1() {
			return earthLocationErrorCode1;
		}

		public String getProcessingBlockID() {
			return processingBlockID;
		}

		public int getInstrumentID() {
			return instrumentID;
		}

		public int getDataTypeID() {
			return dataTypeID;
		}

		public int getTipSourceCode() {
			return tipSourceCode;
		}

		public long getEndDayCount() {
			return endDayCount;
		}

		public int getEndDayOfYear() {
			return endDayOfYear;
		}

		public long getEndTime() {
			return endTime;
		}

		public int getEndYear() {
			return endYear;
		}
		
		public static int getVersion(ucar.unidata.io.RandomAccessFile raf){
			int version = 0;
        	try{
                // Check if ARS Header exist                        
                if (ArsHeader.hasARSHeader(raf)){   
                    // Seek to bytes in Header Record to read Header Record Count                
                    raf.seek((ARS_LENGTH + FORMAT_VER_POS)-1);               
                    version = raf.readUnsignedShort();

                }else{
                    // Seek to bytes in Header Record to read Header Record Count                
                    raf.seek((FORMAT_VER_POS)-1);               
                    version = raf.readUnsignedShort();   
                }        		
        	}catch(IOException ioe){
        		ioe.printStackTrace();
        	}			
//        	System.out.println("version: "+ version);
			return version;
		}

		public int getCalibratedScans() {
			return calibratedScans;
		}

		public int getDataGapsCount() {
			return dataGapsCount;
		}

		public int getMissingLinesCount() {
			return missingLinesCount;
		}
    }         
    
