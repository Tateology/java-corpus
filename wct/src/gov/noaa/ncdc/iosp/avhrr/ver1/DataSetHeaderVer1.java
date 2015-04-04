/*
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

/*
* Avhrr GAC Dataset Header
* Dataset header valid until September 8, 1992
* http://www2.ncdc.noaa.gov/docs/podug/html/k/app-k.htm
* Table K-4. 
* @author arthur.fotos@noaa.gov
* 2/10/2008
* 
* @version 2.2 
*Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
*/

public class DataSetHeaderVer1 extends AvhrrFile implements IDataSetHeaderVer1{

	private static final Logger logger = Logger.getLogger(DataSetHeaderVer1.class.getName());
	
	
	//byte 1 Spacecraft ID
	private short spacecraftId;
	
	//byte 2 Data Type
	private byte dataType;
	
	//byte 3 -8 start time - time code for first frame of data
	private int startYear;
	private int startDay;
	private long startTimeUTC;
	
	//byte 9-10 number of scans
	private int numberOfScans;

	//byte 11-16 end time - time code  from last frame of data
	private int endYear;
	private int endDay;
	private long endTimeUTC;
	
	//byte 17-23 Processing Block ID (ASCI text)
	private String processingId;

	//byte 24 Ramp/Auto Calibration
	private byte[] rampCalibration = new byte[5];
	
	//byte 25-26 Number of data gaps
	private int dataGaps;
	
	//byte 27-32 DACS Quality
	
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
	
	//byte 36-40 (spare zero-filled)
	
	//byte 41-84 dataset name
	private String datasetName;

	
	public DataSetHeaderVer1(){
		
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
			//DACS QUALITY
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
			
			//logger.fine("file pos before dataset name: " + raf.getFilePointer());
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
			//	datasetName = new String(b,"Cp1047");
					
			raf.skipBytes(3136);
			
			//logger.fine("file pos after header read: " + raf.getFilePointer());
			logger.fine("spacecraftID: " + spacecraftId);
			logger.fine("datatype: " + dataType);			
			logger.fine("start time: " + startYear + ":" + startDay + ":" + startTimeUTC);
			logger.fine("Number of scans: " + numberOfScans);
			logger.fine("end time: " + endYear + ":" + endDay + ":" + endTimeUTC);
			logger.fine("processing Id: " + processingId);
			logger.fine("ramp calibration1-5: " + rampCalibration[0] + rampCalibration[1] + rampCalibration[2] + rampCalibration[3] + rampCalibration[3]);	
			logger.fine("data gaps--> " + dataGaps);
			logger.fine("noFrameErrorCount: " + noFrameSyncErrors);
			logger.fine("tipParityErrorCount: " + detectedTipParityErrors);
			logger.fine("auxSyncErrorCount: " + auxillaryErrors);
			logger.fine("calParamId--> " + calParamId);
			logger.fine("dataMode: " + datamode);
			logger.fine("tapeDirection: " + tapeDir);
			logger.fine("dataSource: " + dacsDatasource);
			logger.fine("pnFlag: " + pseudoNoise);			
			logger.fine("dataset name: " + datasetName);
		}catch(Exception e){
			logger.severe("Error reading header! " + e.toString());
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



	/*
	 * variable not implemented in this dataset
	 */
	public byte getAttitudeCorrection() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public long getDacsQuality() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public short getDacsStatus() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public int getDataStartYear() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getEccentricity() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public int getEpochDay() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public short getEpochYear() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getInclination() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getMeanAnomaly() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public short getNadirLocCorrection() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getPerigee() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public int getPitchCorrection() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getRightAscension() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public int getRollCorrection() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getSemimajorAxis() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public long getUtcTOD() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getXVector() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getXdotVector() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getYVector() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public int getYawCorrection() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getYdotVector() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getZVector() {
		return 0;
	}

	/*
	 * variable not implemented in this dataset
	 */
	public float getZdotVector() {
		return 0;
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

	public byte[] getRampCalibration() {
		return rampCalibration;
	}

	public short getSpacecraftId() {
		return spacecraftId;
	}	
	
	public static void main(String[] args){
		String filename = "c:/testfiles/lac/NSS.LHRR.NG.D88113.S0632.E0637.B0828383.WI";
		ucar.unidata.io.RandomAccessFile raf = null;
		try{
			raf = new ucar.unidata.io.RandomAccessFile(filename,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		}catch(Exception e){
			e.printStackTrace();
		}
		DataSetHeaderVer1 header = new DataSetHeaderVer1();
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
