/**
*      Copyright (c) 2007-2010 Work of U.S. Government.
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


package gov.noaa.ncdc.iosp.avhrr.ver1;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * Reads AVHRR Dataset header for dataset implemented November 15,1994
 * NOAA POD Guide Section 3.1
 * http://www2.ncdc.noaa.gov/docs/podug/html/c3/sec3-1.htm
 *
 * @author afotos@noaa.gov
 * 2/10/2008
 * 
 * @version 2.2 
 * Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */

public class DataSetHeaderVer1B extends AvhrrFile implements IDataSetHeaderVer1{
	
	private static final Logger logger = Logger.getLogger(DataSetHeaderVer1B.class.getName());

	//byte 1
	private short spacecraftId;
	//byte 2 Data Type
	private byte dataType;
	//byte 3-8 Start time
	private int startYear;
	private int startDay;
	private long startTimeUTC;
	//bytes 9-10 Number of scans
	private int numberOfScans;
	//bytes 11-16 End year
	private int endYear;
	private int endDay;
	private long endTimeUTC;
	//bytes 17-23 processing Block Id
	private String processingId;
	//byte 24 ramp calibraion
	private byte[] rampCalibration = new byte[5];
	//bytes 25-26 Number of data gaps
	private int dataGaps;
	//bytes 27-32 DACS Quality
	private int noFrameSyncErrors;
	private int detectedTipParityErrors;
	private int auxillaryErrors;
	//bytes 33-34 Calibration Paramter Id
	private String calParamId;
	//bytes 35 DACS Status
	byte pseudoNoise;
	byte dacsDatasource;
	byte tapeDir;
	byte datamode;

	//byte 36 Attitude correction
	private byte attitudeCorrection;
	//byte 37
	private short nadirLocCorrection;	
	//byte 38 spare
	
	//bytes 39-40
	private int dataStartYear;
	//bytes 41-84
	private String datasetName;
	//bytes 85-86
	private short epochYear;
	//bytes 87-88
	private int epochDay;
	//bytes 89-92
	private long utcTOD;
	
	//Keplerian Orbital Elements
	//bytes 93-96
	private long semimajorAxis;
	//bytes 97-100
	private long eccentricity;
	//bytes 101-104
	private long inclination;
	//bytes 105-108
	private long perigee;
	//bytes 109-112
	private long rightAscension;
	//bytes 113-116
	private long meanAnomaly;
	
	//Cartesian Inertial true of Date Elements
	private long xVector;
	//bytes 121-124
	private long yVector;
	//bytes 125-128
	private long zVector;
	//bytes 129-132
	private long xdotVector;
	//bytes 133-136
	private long ydotVector;
	//bytes 137-140
	private long zdotVector;
	
	//future use
	//bytes 141-142
	private int yawCorrection;
	//bytes 143-144
	private int rollCorrection;
	//bytes 145-146
	private int pitchCorrection;
	
	public DataSetHeaderVer1B(){
		
	}
	
	public void readHeader(ucar.unidata.io.RandomAccessFile raf){
		try{
			spacecraftId = (short)raf.readUnsignedByte();
			dataType = (byte)AvhrrFile.readFourBitFlag(raf.readByte(),4);
					
			short temp = raf.readShort();
			startYear = AvhrrFile.readSevenBitFlag(temp, 9);
			startDay = AvhrrFile.readNineBitFlag(temp, 0);			
			startTimeUTC = AvhrrFile.readUnsignedInt(raf);
			
			numberOfScans = raf.readUnsignedShort();

			
			//Endtime
			temp = raf.readShort();
			endYear = AvhrrFile.readSevenBitFlag(temp, 9);
			endDay = AvhrrFile.readNineBitFlag(temp, 0);			
			endTimeUTC = AvhrrFile.readUnsignedInt(raf);
			
			processingId = raf.readString(7);

			byte tempByte = raf.readByte();
			rampCalibration[0] = (byte)AvhrrFile.readOneBitFlag(tempByte, 3);
			rampCalibration[1] = (byte)AvhrrFile.readOneBitFlag(tempByte, 4);
			rampCalibration[2] = (byte)AvhrrFile.readOneBitFlag(tempByte, 5);
			rampCalibration[3] = (byte)AvhrrFile.readOneBitFlag(tempByte, 6);
			rampCalibration[4] = (byte)AvhrrFile.readOneBitFlag(tempByte, 7);

			dataGaps = raf.readUnsignedShort();

			//DACS Quality		
			noFrameSyncErrors = raf.readUnsignedShort();
			detectedTipParityErrors  = raf.readUnsignedShort();
			auxillaryErrors  = raf.readUnsignedShort();
			
			calParamId = raf.readString(2);
			tempByte = raf.readByte();
			
			//DACS Statis
			datamode = (byte)readOneBitFlag(tempByte, 3);
			tapeDir = (byte)readOneBitFlag(tempByte,4);
			dacsDatasource = (byte)readTwoBitFlag(tempByte,5);
			pseudoNoise = (byte)readOneBitFlag(tempByte,7);

			attitudeCorrection = raf.readByte();
			nadirLocCorrection = (short)raf.readUnsignedByte();

			raf.skipBytes(1);
			
			dataStartYear = raf.readUnsignedShort();		

			
			byte[] b = new byte[44];
			for(int i=0;i<b.length;i++){
            	short s = (short)raf.readUnsignedByte();
              	b[i] = (byte)s;
			}
			
			try{
				datasetName = new String(b,"Cp037");
			}catch(UnsupportedEncodingException e){
				datasetName = new String(b);
			}
			
			epochYear = (short)raf.readUnsignedShort();
			epochDay = (short)raf.readUnsignedShort();
			utcTOD = AvhrrFile.readUnsignedInt(raf);
			
			//Keplerian Orbial elements			
			semimajorAxis = raf.readInt();
			eccentricity = raf.readInt();
			inclination = raf.readInt();
			perigee = raf.readInt();
			rightAscension = raf.readInt();
			meanAnomaly = raf.readInt();
			
//			//Cartesian Inertial true of Date Elements
			xVector =raf.readInt();
			yVector = raf.readInt();
			zVector = raf.readInt();
			xdotVector =raf.readInt();
			ydotVector = raf.readInt();
			zdotVector = raf.readInt();
			
			raf.skipBytes(3080);
			
			logger.fine("spacecraftID: " + spacecraftId);
			logger.fine("datatype: " + dataType);
			logger.fine("start time: " + startYear + ":" + startDay + ":" + startTimeUTC);
			logger.fine("Number of scans: " + numberOfScans);
			logger.fine("processing Id: " + processingId);
			logger.fine("ramp calibration1-5: " + rampCalibration[0] + rampCalibration[1] + rampCalibration[2] + rampCalibration[3] + rampCalibration[4]);
			logger.fine("data gaps--> " + dataGaps);
			logger.fine("noFrameErrorCount: " + noFrameSyncErrors);
			logger.fine("tipParityErrorCount: " + detectedTipParityErrors);
			logger.fine("auxSyncErrorCount: " + auxillaryErrors);	
			logger.fine("calibration param id--> " + calParamId);			
			logger.fine("dataMode: " + datamode);
			logger.fine("tapeDirection: " + tapeDir);
			logger.fine("dataSource: " + dacsDatasource);
			logger.fine("pseudoNoise--> " + pseudoNoise);	
			logger.fine("attitudeCorrection--> " + attitudeCorrection);
			logger.fine("nadirLocCorrection: " + nadirLocCorrection);
			logger.fine("StartYear: " + startYear);
			logger.fine("datasetname--> " + datasetName);
			logger.fine("epochYear: " + epochYear);
			logger.fine("epochDay: " + epochDay);
			logger.fine("utcTOD: " + utcTOD);
			logger.fine("semimajorAxis: " + semimajorAxis);
			logger.fine("eccentricity: " + eccentricity);
			logger.fine("inclination: " + inclination);
			logger.fine("perigee: " + perigee);
			logger.fine("rightAscension: " + rightAscension);
			logger.fine("meanAnomaly: " + meanAnomaly);
			logger.fine("xVector: " + xVector);
			logger.fine("yVector: " + yVector);
			logger.fine("zVector: " + zVector);
			logger.fine("xdotVector: " + xdotVector);
			logger.fine("ydotVector: " + ydotVector);
			logger.fine("zdotVector: " + zdotVector);
			logger.fine("yaw--> " + raf.readShort());
			logger.fine("roll--> " + raf.readShort());
			logger.fine("pitch--> " + raf.readShort());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public String getCalParamId() {
		return calParamId;
	}


	public int getDataGaps() {
		return dataGaps;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public short getDataType() {
		return dataType;
	}

	public long getEndTimeUTC() {
		return endTimeUTC;
	}

	public int getNumberOfScans() {
		return numberOfScans;
	}

	public void setNumberOfScans(int numberOfScans) {
		this.numberOfScans = numberOfScans;
	}

	public String getProcessingId() {
		return processingId;
	}


	public int getDataStartYear() {
		return dataStartYear;
	}

	public int getEndDay() {
		return endDay;
	}

	public int getEndYear() {
		return endYear;
	}

	public int getStartDay() {
		return startDay;
	}

	public byte[] getRampCalibration(){
		return rampCalibration;
	}
	
	public short getSpacecraftId() {
		return spacecraftId;
	}

	public long getStartTimeUTC() {
		return startTimeUTC;
	}

	public byte getAttitudeCorrection() {
		return attitudeCorrection;
	}

	public float getEccentricity() {
		return eccentricity;
	}

	public int getEpochDay() {
		return epochDay;
	}

	public short getEpochYear() {
		return epochYear;
	}

	public float getInclination() {
		return inclination;
	}

	public float getMeanAnomaly() {
		return meanAnomaly;
	}

	public short getNadirLocCorrection() {
		return nadirLocCorrection;
	}

	public float getPerigee() {
		return perigee;
	}

	public int getPitchCorrection() {
		return pitchCorrection;
	}

	public float getRightAscension() {
		return rightAscension;
	}

	public int getRollCorrection() {
		return rollCorrection;
	}

	public float getSemimajorAxis() {
		return semimajorAxis;
	}

	public int getStartYear() {
		return startYear;
	}

	public long getUtcTOD() {
		return utcTOD;
	}

	public float getXdotVector() {
		return xdotVector;
	}

	public float getXVector() {
		return xVector;
	}

	public int getYawCorrection() {
		return yawCorrection;
	}

	public float getYdotVector() {
		return ydotVector;
	}

	public float getYVector() {
		return yVector;
	}

	public float getZdotVector() {
		return zdotVector;
	}

	public float getZVector() {
		return zVector;
	}
	
	public byte getDacsDataMode() {
		return datamode;
	}

	public byte getDacsDataSource() {
		return dacsDatasource;
	}

	public int getDacsNoFrameSyncErrorCount() {
		return noFrameSyncErrors;
	}

	public byte getDacsPsuedoNoise() {
		return pseudoNoise;
	}

	public byte getDacsTapeDir() {
		return tapeDir;
	}

	public int getDacsTipParityErrors() {
		return detectedTipParityErrors;
	}


	public int getDacsAuxillaryErrors() {
		return auxillaryErrors;
	}
	
	
	public static void main(String[] args){
		String filename = "c:/testfiles/lac/NSS.LHRR.NJ.D96108.S0000.E0009.B0668181.GC";
		ucar.unidata.io.RandomAccessFile raf = null;
		try{
			raf = new ucar.unidata.io.RandomAccessFile(filename,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		}catch(Exception e){
			e.printStackTrace();
		}
		DataSetHeaderVer1B header = new DataSetHeaderVer1B();
		try {
			if(TBMHeader.hasHeader(raf)){
				raf.seek(122);
			}else{
				raf.seek(0);
			}
			header.readHeader(raf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
}

