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
 * Reads AVHRR Dataset header for dataset valid from September 8, 1992 to November 15, 1994
 * NOAA POD Guide Appendix L
 * http://www2.ncdc.noaa.gov/docs/podug/html/l/app-l.htm
 * Table L-1
 * @author arthur.fotos@noaa.gov
 * 02/10/2008
 *
 * @version 2.2 
 * Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */

public class DataSetHeaderVer1A extends AvhrrFile implements IDataSetHeaderVer1{

	private static final Logger logger = Logger.getLogger(DataSetHeaderVer1A.class.getName());
	
	private short spacecraftId;
	private byte dataType;
	private int startYear;
	private int startDay;
	private long startTimeUTC;
	private int numberOfScans;
	private int endYear;
	private int endDay;
	private long endTimeUTC;
	
	private String processingId;

	private byte[] rampCalibration = new byte[5];
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
	
	
	private String datasetName;

	private short epochYear;
	private int epochDay;
	private long utcTOD;
	
//	//Keplerian Orbital Elements
	private float semimajorAxis;
	private float eccentricity;
	private float inclination;
	private float perigee;
	private float rightAscension;
	private float meanAnomaly;
//	
//	//Cartesian Inertial true of Date Elements
	private float xVector;
	private float yVector;
	private float zVector;
	private float xdotVector;
	private float ydotVector;
	private float zdotVector;
	
	public DataSetHeaderVer1A(){
		
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
			detectedTipParityErrors = raf.readUnsignedShort();
			auxillaryErrors = raf.readUnsignedShort();
			
			calParamId = raf.readString(2);
			
			//DACS Status
			tempByte = raf.readByte();
			datamode = (byte)readOneBitFlag(tempByte, 3);
			tapeDir = (byte)readOneBitFlag(tempByte,4);
			dacsDatasource = (byte)readTwoBitFlag(tempByte,5);
			pseudoNoise = (byte)readOneBitFlag(tempByte,7);
			
			raf.skipBytes(5);

			byte[] b = new byte[42];
			for(int i=0;i<b.length;i++){
            	short s = (short)raf.readUnsignedByte();
              	b[i] = (byte)s;

			}
			
			try{
				datasetName = new String(b,"Cp037");
			}catch(UnsupportedEncodingException e){
				datasetName = new String(b);
			}			
			
			raf.skipBytes(2);
			
			epochYear = (short)raf.readUnsignedShort();
			epochDay = (short)raf.readUnsignedShort();
			utcTOD = readUnsignedInt(raf);

			//logger.fine("pos before stuff: " + raf.getFilePointer());

/**
 * the following variables are stored as 8 byte IBM floating point
 * need to use routine to read??
 * reading as doubles yields incorrect values.
 */
			
			//Keplerian Orbital Elements
			semimajorAxis = (float)raf.readFloat();
			raf.skipBytes(4);
			eccentricity = (float)raf.readFloat();
			inclination =(float)raf.readDouble();
			perigee = (float)raf.readDouble();
			rightAscension = (float)raf.readDouble();
			meanAnomaly = (float)raf.readDouble();
			
			//Cartesian Inertial true of Date Elements
			xVector = (float)raf.readDouble();
			yVector = (float)raf.readDouble();
			zVector = (float)raf.readDouble();
			xdotVector = (float)raf.readDouble();
			ydotVector = (float)raf.readDouble();
			zdotVector = (float)raf.readDouble();
			
			raf.skipBytes(3076);
			//logger.fine("file pos after header read: " + raf.getFilePointer());
		
			logger.fine("spacecraftID: " + spacecraftId);
			logger.fine("datatype: " + dataType);
			logger.fine("start time: " + startYear + ":" + startDay + ":" + startTimeUTC);
			logger.fine("Number of scans: " + numberOfScans);			
			logger.fine("end time: " + endYear + ":" + endDay + ":" + endTimeUTC);
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
			logger.fine("pnFlag: " + pseudoNoise);
			logger.fine("datasetName--> " + datasetName);
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
			
		}catch(Exception e){
			logger.severe("Error reading header!" + e.toString());
		}
	}
	
	
	public int getEndDay() {
		return endDay;
	}

	public long getEndTimeUTC() {
		return endTimeUTC;
	}

	public int getEndYear() {
		return endYear;
	}

	public int getStartDay() {
		return startDay;
	}

	public long getStartTimeUTC() {
		return startTimeUTC;
	}

	public int getStartYear() {
		return startYear;
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

	public int getNumberOfScans() {
		return numberOfScans;
	}

	public void setNumberOfScans(int numberOfScans) {
		this.numberOfScans = numberOfScans;
	}

	public String getProcessingId() {
		return processingId;
	}

	public short getSpacecraftId() {
		return spacecraftId;
	}



	public byte[] getRampCalibration(){
		return rampCalibration;
	}


	public byte getAttitudeCorrection() {
		return 0;
	}

	public int getDataStartYear() {
		return startYear;
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
		return 0 ;
	}

	public float getPerigee() {
		return perigee;
	}

	/**
	 * variable does not exist in this version
	 */
	public int getPitchCorrection() {
		return 0;
	}

	public float getRightAscension() {
		return rightAscension;
	}

	public int getRollCorrection() {
		return 0;
	}

	public float getSemimajorAxis() {
		return semimajorAxis;
	}

	public long getUtcTOD() {
		return utcTOD;
	}

	public float getXVector() {
		return xVector;
	}

	public float getXdotVector() {
		return xdotVector;
	}

	/**
	 * not implemented in this dataset version
	 */
	public float getYVector() {
		return yVector;
	}

	/**
	 * not implemented in this dataset version
	 */
	public int getYawCorrection() {
		return 0;
	}

	/**
	 * not implemented in this dataset version
	 */
	public float getYdotVector() {
		return ydotVector;
	}
	
	/**
	 * not implemented in this dataset version
	 */
	public float getZVector() {
		return zVector;
	}

	/**
	 * not implemented in this dataset version
	 */
	public float getZdotVector() {
		return zdotVector;
	}
	
	public int getDacsAuxillaryErrors() {
		return auxillaryErrors;
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

	
	public static void main(String[] args){
		String filename = "c:/testfiles/lac/NSS.LHRR.NH.D93107.S0158.E0209.B2350404.GC";
		ucar.unidata.io.RandomAccessFile raf = null;
		try{
			raf = new ucar.unidata.io.RandomAccessFile(filename,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		}catch(Exception e){
			e.printStackTrace();
		}
		DataSetHeaderVer1A header = new DataSetHeaderVer1A();
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
