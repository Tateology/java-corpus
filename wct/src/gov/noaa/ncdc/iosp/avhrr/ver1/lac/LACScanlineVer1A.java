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


package gov.noaa.ncdc.iosp.avhrr.ver1.lac;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;
import gov.noaa.ncdc.iosp.avhrr.ver1.ScanlineVer1;

import java.util.logging.Logger;


/**
 * Reads AVHRR LAC Dataset for dataset valid from September 8, 1992 to November 15, 1994
 * NOAA POD Guide Appendix L
 * http://www2.ncdc.noaa.gov/docs/podug/html/l/app-l.htm
 * Table L-2
 * @author afotos
 * 2/10/2008
 * 
 *@version 2.2 
 *Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */


public class LACScanlineVer1A extends ScanlineVer1 {

	private static final Logger logger = Logger.getLogger(LACScanlineVer1A.class.getName());
	private static int RECORD_SIZE = 7400;
	
	
	// calibration variables
	//byte 9
	private byte fatalFlag;
	private byte timeError;
	private byte dataGap;
	private byte dataJitter;
	private byte calibration;
	private byte noEarthLocation;
	private byte ascendDescend;
	private byte pnStatus;
	
	//byte 10
	private byte bitSyncStatus;
	private byte syncError;
	private byte frameSyncLock;
	private byte flyWheeling;
	private byte bitSlippage;

	//byte 11
	byte[] tipParity = new byte[5];
	//byte 12;
	byte syncErrorCount;

	
	
	
	public LACScanlineVer1A(){
		
	}
	

	
	public void readScanLine(ucar.unidata.io.RandomAccessFile raf){
		try{
			readScanlineInfo(raf);
			
			byte temp = raf.readByte();
			fatalFlag = (byte)AvhrrFile.readOneBitFlag(temp,0);
			timeError = (byte)AvhrrFile.readOneBitFlag(temp,1);
			dataGap = (byte)AvhrrFile.readOneBitFlag(temp,2);
			dataJitter = (byte)AvhrrFile.readOneBitFlag(temp,3);
			calibration = (byte)AvhrrFile.readOneBitFlag(temp,4);
			noEarthLocation = (byte)AvhrrFile.readOneBitFlag(temp,5);
			ascendDescend = (byte)AvhrrFile.readOneBitFlag(temp,6);
			pnStatus = (byte)AvhrrFile.readOneBitFlag(temp,7);	
			
			temp = raf.readByte();
			bitSyncStatus = (byte)readOneBitFlag(temp,0);
			syncError = (byte)readOneBitFlag(temp,1);
			frameSyncLock = (byte)readOneBitFlag(temp,2);
			flyWheeling = (byte)readOneBitFlag(temp,3);
			bitSlippage = (byte)readOneBitFlag(temp,4);
			
			temp = raf.readByte();
			tipParity[0] = (byte)readOneBitFlag(temp, 0);
			tipParity[1] = (byte)readOneBitFlag(temp, 1);
			tipParity[2] = (byte)readOneBitFlag(temp, 2);
			tipParity[3] = (byte)readOneBitFlag(temp, 3);
			tipParity[4] = (byte)readOneBitFlag(temp, 4);
			
			syncErrorCount = raf.readByte();
						
			readCalibrationCoeffs(raf);
			
			readLocationData(raf);

			
			//telemetry
			raf.skipBytes(140);

			readLACSensorData(raf);
			
			readZenithAngleDecimals(raf);
			

			raf.seek(raf.getFilePointer() -1);

			raf.skipBytes(2);  //no clock drift for this version
			raf.skipBytes(674);
 			logger.fine("File pointer position after read --> " + raf.getFilePointer());
		}catch(Exception e){logger.severe(e.toString());
			e.printStackTrace();
		}
	
	}


	public byte[] getTipParity() {
		return tipParity;
	}

	public byte getSyncErrorCount() {
		return syncErrorCount;
	}

	


	/**
	 * for test only
	 * 
	 * @param args
	 */
	
	public static void main(String[] args){
		String filename = "/home/afotos/avhrr/class/NSS.GHRR.NG.D89193.S1317.E1510.B1462930.GC";
		ucar.unidata.io.RandomAccessFile raf = null;
		try{
			raf = new ucar.unidata.io.RandomAccessFile(filename,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
			raf.seek(3342);
		}catch(Exception e){
			e.printStackTrace();
		}
		LACScanlineVer1A header = new LACScanlineVer1A();
	}

	public byte getFatalFlag() {
		return fatalFlag;
	}

	public byte getTimeError() {
		return timeError;
	}

	public byte getDataGap() {
		return dataGap;
	}

	public byte getDataJitter() {
		return dataJitter;
	}

	public byte getCalibration() {
		return calibration;
	}

	public byte getNoEarthLocation() {
		return noEarthLocation;
	}

	public byte getAscendDescend() {
		return ascendDescend;
	}

	public byte getPnStatus() {
		return pnStatus;
	}

	public byte getBitSyncStatus() {
		return bitSyncStatus;
	}

	public byte getSyncError() {
		return syncError;
	}

	public byte getFrameSyncLock() {
		return frameSyncLock;
	}

	public byte getFlyWheeling() {
		return flyWheeling;
	}

	public byte getBitSlippage() {
		return bitSlippage;
	}

	public void setSyncErrorCount(byte syncErrorCount) {
		this.syncErrorCount = syncErrorCount;
	}

	public byte getChan3Correction() {
		return 0;
	}

	public byte getChan4Correction() {
		return 0;
	}


	public byte getChan5Correction() {
		return 0;
	}

}
