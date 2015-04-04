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


package gov.noaa.ncdc.iosp.avhrr.ver1.gac;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;
import gov.noaa.ncdc.iosp.avhrr.ver1.IScanlineVer1;

import java.util.logging.Logger;

/**
 * Reads datarecord for avhrr gac data original version as defined in pod guide
 * Format of GAC data record before September 8, 1992.
 * http://www2.ncdc.noaa.gov/docs/podug/html/k/app-k.htm
 * Tables K1, K3
 * @author afotos
 * 2/10/2008
 * 
 *@version 2.2 
 *Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */


public class GACScanlineVer1 extends AvhrrFile implements IScanlineVer1{

	private static final Logger logger = Logger.getLogger(GACScanlineVer1.class.getName());
	private static int RECORD_SIZE = 3220;
	
	//bytes 1 & 2 scan line number
	private int scanLine;
	
	//bytes 3-8 time code
	private short timeCode;
	int year;
	int day;
	long utcTime;
	
	// calibration variables
	private int qualityInd;
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
	
	//byte 13-52 calibration coefficients
	private int[] slopeCoeffs = new int[5];
	private int[] interceptCoeffs = new int[5];
	
	//byte 53 number of meaningful solar zenith angles and earth location points appended to the scan
	private short numLocationPoints;
	
	//bytes 54 - 104 zenith angels
	private short[] zenithAngles = new short[51];
	private byte[] zenithDecimals = new byte[51];
	
	//bytes 105 - 308 earth location
	private float[] anchorLat = new float[51];
	private float[] anchorLon = new float[51];	
	
	
//calculated vars	
	float[] gLatTiePoints = new float[51];

	float[] gLonTiePoints = new float[51];
	
	float[] gnomonicLat = new float[409];

	float[] gnomonicLon = new float[409];

	float[] calculatedLat = new float[409];

	float[] calculatedLon = new float[409];	
	
	
	//bytes 309 - 448 telemetry
	private short[] telemetry = new short[140];
	
	//bytes 449-3716 raw data
	private byte[] rawData = new byte[2728];
	
	//bytes 3177 - 3220 spare
	private short[][] data = new short[5][409];
	
	
	
	public GACScanlineVer1(){
		
	}
	

	/**
	 * for test purposes only
	 * @param raf
	 */
	private GACScanlineVer1(ucar.unidata.io.RandomAccessFile raf){
/**
		try {
			for(int i=0;i<10;i++){
				raf.seek(3342 + i * 3220);
				readScanLine(raf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	
	public void readScanLine(ucar.unidata.io.RandomAccessFile raf){
		try{
//			System.out.println("File pos before record read: " + raf.getFilePointer());
			scanLine = raf.readUnsignedShort();
//			System.out.println("scanLine: " + scanLine);

			//time code 
			timeCode = raf.readShort();
			year = AvhrrFile.readSevenBitFlag(timeCode, 9);
			day = AvhrrFile.readNineBitFlag(timeCode, 0);			
			utcTime = AvhrrFile.readUnsignedInt(raf);
			
//			qualityInd = raf.readInt();
			byte temp = raf.readByte();
			fatalFlag = (byte)readOneBitFlag(temp,0);
			timeError = (byte)readOneBitFlag(temp,1);
			dataGap = (byte)readOneBitFlag(temp,2);
			dataJitter = (byte)readOneBitFlag(temp,3);
			calibration = (byte)readOneBitFlag(temp,4);
			noEarthLocation = (byte)readOneBitFlag(temp,5);
			ascendDescend = (byte)readOneBitFlag(temp,6);
			pnStatus = (byte)readOneBitFlag(temp,7);	
			
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
			
			
			for(int i=0;i<5;i++){
				slopeCoeffs[i] = raf.readInt();
				interceptCoeffs[i] = raf.readInt();
			}
			
			numLocationPoints = (short)raf.readUnsignedByte();
			
//			System.out.println("Number of meaningful points: " + numLocationPoints);
			//53 bytes read to this point
			
			//zenith angle stored as degrees x 2
			for(int i=0;i<51;i++){
				zenithAngles[i] = (short)raf.readUnsignedByte();
			}
			//104 bytes read
		
			//lat and lon are each stored in two-byte fields in 128ths of a degree (0 to 180E positive, 0 to 180W negative)
			for(int i=0;i<51;i++){
				short s = raf.readShort();
				anchorLat[i] = (float)(s/128.0);
				s = raf.readShort();
				anchorLon[i] = (float)(s/128.0);
//				System.out.println("lat:lon " + lat[i] + ":" + lon[i]);
			}
			//308 bytes read at this point
			
			//telemetry
			raf.skipBytes(140);

//			System.out.println("File pos before earth obs: " + raf.getFilePointer());
			readEarthObservations(raf);
//			System.out.println("File pos after earth obs: " + raf.getFilePointer());

			
			//the last 44 bytes vary for the 3 pre KLM data formats   the last 22 are spares for all formats
			//read 20 bytes for extra zenith angles values
			int z = 0;
			int offset = 0;
			long filepos = raf.getFilePointer();
//			System.out.println(filepos);
			for(int i =0; i<20; i++){
				raf.seek(filepos);
				short s = raf.readShort();
				int shift = 0;
				for(int j=0;j<3;j++){
					zenithDecimals[z] = (byte)AvhrrFile.readThreeBitFlag(s, shift + offset);
					shift += 3;
					if(z == 50){
						break;
					}else{
						z++;
					}
					
					offset++;
				}
				if(offset == 8){
					offset = 0;
					filepos++;
				}
				filepos++;
			}
			raf.seek(raf.getFilePointer() -1);
//			System.out.println(raf.getFilePointer());
			raf.skipBytes(2);
			raf.skipBytes(22);
		}catch(Exception e){logger.severe(e.toString());
			e.printStackTrace();
		}
	
	}
	
	/**
	 * 5 channels x 409 points per channel (10 bit packed data) 3 channels of data per 4 bytes
	 * @param raf
	 * @throws Exception
	 */
	public void readEarthObservations(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		int chanNum = 0;
		int[] shift = { 20, 10, 0 };
		int k = 0;
		int i = 0;
		// Multiple dimensions for Vis / IR channels and for FOV indeces (2728
		// bytes total)
		while (i < 682 && k < 409) {
			int rawValue = raf.readInt();
			i++;
			for (int j = 0; j < 3; j++) {
				data[chanNum][k] = (short) ((rawValue & (0x3ff << shift[j])) >> shift[j]);
				if (chanNum == 4) {
					chanNum = 0;
					k++;
					if (k == 409) {
						break;
					}
				} else {
					chanNum++;
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public float[] getCalculatedLat() {
		getCalculatedCoors();
		return calculatedLat;
	}

	public float[] getCalculatedLon() {
		getCalculatedCoors();
		return calculatedLon;
	}

	public void getGnomonicCoords() {
		for (int i = 0; i < 51; i++) {
			float[] temp = convertToGnomonicCoords(anchorLat[i], anchorLon[i]);
			gLatTiePoints[i] = temp[0];
			gLonTiePoints[i] = temp[1];
		}
	}

	public void getCalculatedCoors() {
		getGnomonicCoords();
		int pixelNum = 3;
		gnomonicLat[4] = gLatTiePoints[0];
		gnomonicLon[4] = gLatTiePoints[0];
		for (int i = 4; i > 0; i--) {
			gnomonicLat[4 - i] = quadraticInterpolation(gLatTiePoints[0], gLatTiePoints[1], gLatTiePoints[2], -8 - i);
			gnomonicLon[4 - i] = quadraticInterpolation(gLonTiePoints[0], gLonTiePoints[1], gLonTiePoints[2], -8 - i);
		}

		for (int t = 0; t < 49; t++) {
			pixelNum++;
			for (int i = 0; i < 7; i++) {
				gnomonicLat[pixelNum] = quadraticInterpolation(gLatTiePoints[t], gLatTiePoints[t + 1], gLatTiePoints[t + 2], -7 + i);
				gnomonicLon[pixelNum] = quadraticInterpolation(gLonTiePoints[t], gLonTiePoints[t + 1], gLonTiePoints[t + 2], -7 + i);
				pixelNum++;
			}
			gnomonicLat[pixelNum] = gLatTiePoints[t + 1];
			gnomonicLon[pixelNum] = gLonTiePoints[t + 1];
		}

		for (int g = 0; g < 13; g++) {
			pixelNum++;
			gnomonicLat[pixelNum] = quadraticInterpolation(gLatTiePoints[48], gLatTiePoints[49], gLatTiePoints[50], 1 + g);
			gnomonicLon[pixelNum] = quadraticInterpolation(gLonTiePoints[48], gLonTiePoints[49], gLonTiePoints[50], 1 + g);
		}

		for (int k = 0; k < 409; k++) {
			float[] temp = convertFromGnomonicCoords(gnomonicLat[k], gnomonicLon[k]);
			calculatedLat[k] = (float) Math.toDegrees(temp[0]);
			calculatedLon[k] = (float) (180 - Math.toDegrees(temp[1]));
		}

	}

	public float quadraticInterpolation(float d0, float d1, float d2, int p) {
		float val1 = ((((d2 - d0) / 2) / 8) * p);
		float val2 = (((d2 - 2 * d1 + d0) / 2) / 64) * (p * p);
		float val = d1 + ((((d2 - d0) / 2) / 8) * p) + ((((d2 - 2 * d1 + d0) / 2) / (64)) * (p * p));
		return val;
	}

	public float[] getGnomonicLon() {
		return gnomonicLon;
	}

	public float[] convertToGnomonicCoords(double lat, double lon) {
		float[] gCoords = new float[2];
		// multiply by scale factor to get degrees
		double rLat = Math.toRadians(lat * 0.0001);
		double rLon = Math.toRadians(lon * 0.0001);
		double x = Math.sin(rLat) / Math.tan(rLon);
		double y = -Math.cos(rLat) / Math.tan(rLon);
		gCoords[0] = (float) x;
		gCoords[1] = (float) y;
//		System.out.println("gcoords: " + (float)x + " : " + (float)y);
		return gCoords;
	}

	public float[] convertFromGnomonicCoords(double x, double y) {
		float[] coords = new float[2];
		double d = -x / y;
		coords[0] = (float) Math.atan((-x / y));

		double da = Math.pow(x, 2) + Math.pow(y, 2);
		double db = Math.pow(da, 0.5);
		double dc = 1 / db;
		if(x<0){
			if (y < 0) {
				coords[1] = (float) (Math.atan(dc) + Math.toRadians(180));
			} else {
				coords[1] = (float) (Math.atan(dc) - Math.toRadians(180));
			}
		}
		return coords;

	}	
////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	
	
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
		GACScanlineVer1 header = new GACScanlineVer1(raf);
	}


	public byte getAscendDescend() {
		return ascendDescend;
	}


	public byte getBitSlippage() {
		return bitSlippage;
	}


	public byte getBitSyncStatus() {
		return bitSyncStatus;
	}


	public byte getCalibration() {
		return calibration;
	}


	public byte getDataGap() {
		return dataGap;
	}


	public byte getDataJitter() {
		return dataJitter;
	}


	public byte getFatalFlag() {
		return fatalFlag;
	}


	public byte getFifthTipParity() {
		return tipParity[4];
	}


	public byte getFirstTipParity() {
		return tipParity[0];
	}


	public byte getFlyWheeling() {
		return flyWheeling;
	}


	public byte getFouthTipParity() {
		return tipParity[3];
	}


	public byte getFrameSyncLock() {
		return frameSyncLock;
	}


	public byte getNoEarthLocation() {
		return noEarthLocation;
	}


	public short getNumLocationPoints() {
		return numLocationPoints;
	}


	public byte getPnStatus() {
		return pnStatus;
	}


	public int getQualityInd() {
		return qualityInd;
	}


	public byte getSecondTipParity() {
		return tipParity[1];
	}


	public byte getSyncError() {
		return syncError;
	}


	public byte getSyncErrorCount() {
		return syncErrorCount;
	}


	public short[] getTelemetry() {
		return telemetry;
	}


	public byte getThirdTipParity() {
		return tipParity[2];
	}

	public byte[] getTipParity(){
		return tipParity;
	}

	public short getTimeCode() {
		return timeCode;
	}


	public byte getTimeError() {
		return timeError;
	}


	public float[] getAnchorLat() {
		return anchorLat;
	}


	public float[] getAnchorLon() {
		return anchorLon;
	}


	public byte[] getRawData() {
		return rawData;
	}


	public int getScanLine() {
		return scanLine;
	}


	public int getDay() {
		return day;
	}


	public long getUtcTime() {
		return utcTime;
	}


	public int getYear() {
		return year;
	}


	public short[][] getData() {
		return data;
	}


	public short[] getZenithAngles() {
		return zenithAngles;
	}


	public byte[] getZenithDecimals() {
		return zenithDecimals;
	}


	public int[] getInterceptCoeffs() {
		return interceptCoeffs;
	}


	public int[] getSlopeCoeffs() {
		return slopeCoeffs;
	}


	/**
	 * not valid for first version of avhrr gac
	 */
	public byte getClockAdjustment() {
		return 0;
	}


	/**
	 * not valid for first version of avhrr gac
	 */
	public int getClockDrift() {
		return 0;
	}

	/**
	 * not valid for first version of avhrr gac
	 */
	public byte getChan3Correction() {
		return 0;
	}

	/**
	 * not valid for first version of avhrr gac
	 */
	public byte getChan4Correction() {
		return 0;
	}

	/**
	 * not valid for first version of avhrr gac
	 */
	public byte getChan5Correction() {
		return 0;
	}

	/**
	 * not valid for first version of avhrr gac
	 */
	public byte getFourthTipParity() {
		return 0;
	}

	
}
