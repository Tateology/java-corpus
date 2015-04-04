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
 * This program reads a GAC AVHRR L1B Data Record given the file path and prints
 * some results to the screen.
 * 
 * Note: Current version of code is for NOAA L1B Version 2, but it should handle
 * other Data Record Format Versions.
 * 
 * @author Philip Jones
 * @since 2006-12-13
 */

public abstract class GACDataRecord extends AvhrrFile implements AvhrrConstants {

	// Data record to start read
	private static int init = 1; // init >= 1

	// Scan Line Information
	public int scanLine;

	public int scanYear;

	public int scanDayOfYear;

	public short satelliteDrift;

	public long scanTime;

	public short scanLineBitField;

	public byte scanLineBitField0;

	public byte scanLineBitField14;

	public byte scanLineBitField15;

	// Calibration Coefficients
	// Extra dimensions for Vis / IR channels and status
	public int[][] visSlope1 = new int[3][3];

	public int[][] visIntercept1 = new int[3][3];

	public int[][] visSlope2 = new int[3][3];

	public int[][] visIntercept2 = new int[3][3];

	public int[][] visIntersection = new int[3][3];

	public int[][] irCoefficient1 = new int[3][3];

	public int[][] irCoefficient2 = new int[3][3];

	public int[][] irCoefficient3 = new int[3][3];

	public long tipEulerAnglesTime;

	public short rollAngle;

	public short pitchAngle;

	public short yawAngle;

	public int spacecraftAltitude;

	// Extra dimension for FOV points
	public short[] solarZenithAngle = new short[51];

	public short[] satelliteZenithAngle = new short[51];

	public short[] relativeAzimuthAngle = new short[51];

	public int[] tiePointLat = new int[51];

	public int[] tiePointLon = new int[51];

	// HRPT Minor Frame Telemetry (skip)
	public short hrptID;

	public byte hrptIDbit8;

	public int[] rampCal = new int[5];

	public int[] prtReading = new int[3];

	public int patchTemp;

	public int[][] backScan = new int[3][10];

	public int[][] spaceData = new int[5][10];

	// AVHRR Sensor Data
	// Extra dimensions for Vis / IR channels and FOV points
	public short[][] earthObservations = new short[409][5];

	// Digital B Telemetry
	public short invalidTelemetryBitFlags;

	public byte invalidTelemetryBitFlags1;

	public byte invalidTelemetryBitFlags2;

	public byte invalidTelemetryBitFlags3;

	public byte invalidTelemetryBitFlags4;

	public byte invalidTelemetryBitFlags5;

	public byte invalidTelemetryBitFlags6;

	public byte invalidTelemetryBitFlags7;

	public byte invalidTelemetryBitFlags8;

	public byte invalidTelemetryBitFlags9;

	public byte invalidTelemetryBitFlags10;

	public byte invalidTelemetryBitFlags11;

	public byte invalidTelemetryBitFlags12;

	public byte invalidTelemetryBitFlags13;

	public byte invalidTelemetryBitFlags14;

	public byte invalidTelemetryBitFlags15;

	public short avhrrDigitalBData;

	public byte avhrrDigitalBData1;

	public byte avhrrDigitalBData2;

	public byte avhrrDigitalBData3;

	public byte avhrrDigitalBData4;

	public byte avhrrDigitalBData5;

	public byte avhrrDigitalBData6;

	public byte avhrrDigitalBData7;

	public byte avhrrDigitalBData8;

	public byte avhrrDigitalBData9;

	public byte avhrrDigitalBData10;

	public byte avhrrDigitalBData11;

	public byte avhrrDigitalBData12;

	public byte avhrrDigitalBData13;

	public byte avhrrDigitalBData14;

	public byte avhrrDigitalBData15;

	// Analog Housekeeping Data (TIP)
	public int invalidAnalogTelemetryBitFlags;

	public byte invalidAnalogTelemetryBitFlags1;

	public byte invalidAnalogTelemetryBitFlags2;

	public byte invalidAnalogTelemetryBitFlags3;

	public byte invalidAnalogTelemetryBitFlags4;

	public byte invalidAnalogTelemetryBitFlags5;

	public byte invalidAnalogTelemetryBitFlags6;

	public byte invalidAnalogTelemetryBitFlags7;

	public byte invalidAnalogTelemetryBitFlags8;

	public byte invalidAnalogTelemetryBitFlags9;

	public byte invalidAnalogTelemetryBitFlags10;

	public byte invalidAnalogTelemetryBitFlags11;

	public byte invalidAnalogTelemetryBitFlags12;

	public byte invalidAnalogTelemetryBitFlags13;

	public byte invalidAnalogTelemetryBitFlags14;

	public byte invalidAnalogTelemetryBitFlags15;

	public byte invalidAnalogTelemetryBitFlags16;

	public byte invalidAnalogTelemetryBitFlags17;

	public byte invalidAnalogTelemetryBitFlags18;

	public byte invalidAnalogTelemetryBitFlags19;

	public byte invalidAnalogTelemetryBitFlags20;

	public byte invalidAnalogTelemetryBitFlags21;

	public byte invalidAnalogTelemetryBitFlags22;

	public byte[] analogTelemetry = new byte[22];

	// Clouds From AVHRR (CLAVR)
	public int clavrStatusBitField;

	public byte clavrStatus;

	// Extra dimension for FOV points
	public byte[] clavrCodes = new byte[409];

	// Data Record Filler

	// calculated variables
	float[] gLatTiePoints = new float[51];

	float[] gLonTiePoints = new float[51];

	float[] gnomonicLat = new float[409];

	float[] gnomonicLon = new float[409];

	float[] calculatedLat = new float[409];

	float[] calculatedLon = new float[409];

	private static int fillerSkip = 448;

	abstract void readScanLine(ucar.unidata.io.RandomAccessFile raf);

	public short[][] getEarthObservations() {
		return earthObservations;
	}

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
			float[] temp = convertToGnomonicCoords(tiePointLat[i], tiePointLon[i]);
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

	protected void readScanLineInfo(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		scanLine = raf.readUnsignedShort();
		scanYear = raf.readUnsignedShort();
		scanDayOfYear = raf.readUnsignedShort();
		satelliteDrift = raf.readShort();
		scanTime = readUnsignedInt(raf);
		scanLineBitField = raf.readShort();
		// scanLineBitField0 - 0=3b, 1 = 3a, 2 = transition
		scanLineBitField0 = (byte) readTwoBitFlag(scanLineBitField, 0);
		scanLineBitField14 = (byte) readOneBitFlag(scanLineBitField, 14);
		scanLineBitField15 = (byte) readOneBitFlag(scanLineBitField, 15);
		raf.skipBytes(10);
	}

	protected void readCalibrationCoeffs(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		for (int k = 0; k < 3; k++) { // Vis Channels
			for (int j = 0; j < 3; j++) { // Status: Operational, Test,
											// Prelaunch
				visSlope1[j][k] = raf.readInt();
				visIntercept1[j][k] = raf.readInt();
				visSlope2[j][k] = raf.readInt();
				visIntercept2[j][k] = raf.readInt();
				visIntersection[j][k] = raf.readInt();
			}
		}

		for (int k = 0; k < 3; k++) { // IR Channels
			for (int j = 0; j < 2; j++) { // Status: Operational, Test
				irCoefficient1[j][k] = raf.readInt();
				irCoefficient2[j][k] = raf.readInt();
				irCoefficient3[j][k] = raf.readInt();
			}
		}

		raf.skipBytes(12);
	}

	protected void readNavigation(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		readNavigationStatusBitField(raf);

		tipEulerAnglesTime = readUnsignedInt(raf);

		rollAngle = raf.readShort();
		pitchAngle = raf.readShort();
		yawAngle = raf.readShort();

		spacecraftAltitude = raf.readUnsignedShort();

		// Extra dimension for FOV points
		for (int k = 0; k < 51; k++) { // 5 to 405 FOVs every 8 points (51
										// points)
			solarZenithAngle[k] = raf.readShort();
			satelliteZenithAngle[k] = raf.readShort();
			relativeAzimuthAngle[k] = raf.readShort();
		}
		raf.skipBytes(6);

		for (int k = 0; k < 51; k++) { // 5 to 405 FOVs every 8 points (51
										// points)
			tiePointLat[k] = raf.readInt();
			tiePointLon[k] = raf.readInt();
//			System.out.println("tie points=" + tiePointLat[k] + " : " + tiePointLon[k]);
		}
//		System.out.println("///////End of scan line");
		raf.skipBytes(8);
	}

	protected void readHRPTMinorFrameTelemetry(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		// Frame Sync
		raf.skipBytes(12);
		// ID
		hrptID = raf.readShort();
		hrptIDbit8 = (byte) readTwoBitFlag(hrptID, 7);
		// word 2 not needed
		raf.skipBytes(2);

		// Time Code
		raf.skipBytes(8);
		// Telemetry
		for (int i = 0; i < 5; i++) {
			rampCal[i] = raf.readUnsignedShort();
		}
		for (int i = 0; i < 3; i++) {
			prtReading[i] = raf.readUnsignedShort();
		}
		patchTemp = raf.readUnsignedShort();
		raf.skipBytes(2);

		// Back Scan
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 3; j++) {
				backScan[j][i] = raf.readUnsignedShort();
			}
		}

		// space data
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 5; j++) {
				spaceData[j][i] = raf.readUnsignedShort();
			}
		}

		// sync delta
		raf.skipBytes(2);
		// zero fill
		raf.skipBytes(2);
	}

	protected void readEarthObservations(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		int bandNum = 0;
		int indexInBand = 0;
		int[] shift = { 20, 10, 0 };

		// Multiple dimensions for Vis / IR channels and for FOV indeces
		for (int k = 0; k < SCAN_LINE_LENGTH; k++) {
			int rawValue = raf.readInt();

			for (int j = 0; j < 3; j++) {
				earthObservations[indexInBand][bandNum] = (short) ((rawValue & (0x3ff << shift[j])) >> shift[j]);
				// System.out.println("EarthObs:
				// "+earthObservations[indexInBand][bandNum][i-1]);
				// Observations end at bits 19-10 in word 682, so to break
				// j-loop
				if ((indexInBand == (RAW_SCENE_RASTER_WIDTH - 1)) && (bandNum == 4)) {
					// index=408 and channel=0
					break;
				}

				if (bandNum == 4) {
					indexInBand++;
				}
				bandNum = bandNum == 4 ? 0 : bandNum + 1;
			}
			if (k == SCAN_LINE_LENGTH - 1) {
				break;
			}
		}
		raf.skipBytes(8);
	}

	protected void readDigitalBHouseKeeping(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		invalidTelemetryBitFlags = raf.readShort();
		invalidTelemetryBitFlags1 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 1);
		invalidTelemetryBitFlags2 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 2);
		invalidTelemetryBitFlags3 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 3);
		invalidTelemetryBitFlags4 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 4);
		invalidTelemetryBitFlags5 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 5);
		invalidTelemetryBitFlags6 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 6);
		invalidTelemetryBitFlags7 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 7);
		invalidTelemetryBitFlags8 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 8);
		invalidTelemetryBitFlags9 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 9);
		invalidTelemetryBitFlags10 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 10);
		invalidTelemetryBitFlags11 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 11);
		invalidTelemetryBitFlags12 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 12);
		invalidTelemetryBitFlags13 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 13);
		invalidTelemetryBitFlags14 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 14);
		invalidTelemetryBitFlags15 = (byte) readOneBitFlag(invalidTelemetryBitFlags, 15);
		avhrrDigitalBData = raf.readShort();
		avhrrDigitalBData1 = (byte) readOneBitFlag(avhrrDigitalBData, 1);
		avhrrDigitalBData2 = (byte) readOneBitFlag(avhrrDigitalBData, 2);
		avhrrDigitalBData3 = (byte) readOneBitFlag(avhrrDigitalBData, 3);
		avhrrDigitalBData4 = (byte) readOneBitFlag(avhrrDigitalBData, 4);
		avhrrDigitalBData5 = (byte) readOneBitFlag(avhrrDigitalBData, 5);
		avhrrDigitalBData6 = (byte) readOneBitFlag(avhrrDigitalBData, 6);
		avhrrDigitalBData7 = (byte) readOneBitFlag(avhrrDigitalBData, 7);
		avhrrDigitalBData8 = (byte) readOneBitFlag(avhrrDigitalBData, 8);
		avhrrDigitalBData9 = (byte) readOneBitFlag(avhrrDigitalBData, 9);
		avhrrDigitalBData10 = (byte) readOneBitFlag(avhrrDigitalBData, 10);
		avhrrDigitalBData11 = (byte) readOneBitFlag(avhrrDigitalBData, 11);
		avhrrDigitalBData12 = (byte) readOneBitFlag(avhrrDigitalBData, 12);
		avhrrDigitalBData13 = (byte) readOneBitFlag(avhrrDigitalBData, 13);
		avhrrDigitalBData14 = (byte) readOneBitFlag(avhrrDigitalBData, 14);
		avhrrDigitalBData15 = (byte) readOneBitFlag(avhrrDigitalBData, 15);
		raf.skipBytes(12);
	}

	protected void readAnalogHouseKeepint(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		invalidAnalogTelemetryBitFlags = raf.readInt();
		invalidAnalogTelemetryBitFlags1 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 1);
		invalidAnalogTelemetryBitFlags2 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 2);
		invalidAnalogTelemetryBitFlags3 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 3);
		invalidAnalogTelemetryBitFlags4 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 4);
		invalidAnalogTelemetryBitFlags5 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 5);
		invalidAnalogTelemetryBitFlags6 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 6);
		invalidAnalogTelemetryBitFlags7 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 7);
		invalidAnalogTelemetryBitFlags8 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 8);
		invalidAnalogTelemetryBitFlags9 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 9);
		invalidAnalogTelemetryBitFlags10 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 10);
		invalidAnalogTelemetryBitFlags11 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 11);
		invalidAnalogTelemetryBitFlags12 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 12);
		invalidAnalogTelemetryBitFlags13 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 13);
		invalidAnalogTelemetryBitFlags14 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 14);
		invalidAnalogTelemetryBitFlags15 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 15);
		invalidAnalogTelemetryBitFlags16 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 16);
		invalidAnalogTelemetryBitFlags17 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 17);
		invalidAnalogTelemetryBitFlags18 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 18);
		invalidAnalogTelemetryBitFlags19 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 19);
		invalidAnalogTelemetryBitFlags20 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 20);
		invalidAnalogTelemetryBitFlags21 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 21);
		invalidAnalogTelemetryBitFlags22 = (byte) readOneBitFlag(invalidAnalogTelemetryBitFlags, 22);

		for (int k = 0; k < 22; k++) { // 22 Words
			analogTelemetry[k] = raf.readByte();
		}
		raf.skipBytes(6);
	}

	protected void readClavrData(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		clavrStatusBitField = raf.readInt();
		clavrStatus = (byte) readOneBitFlag(clavrStatusBitField, 0);
		raf.skipBytes(4);

		// If CLAVR is enabled
		if (clavrStatus == 1) {

			int fov = 0;
			short word = 0;

			for (int j = 0; j < 52; j++) { // short integer index 0 to 51
				word = raf.readShort();
				for (int k = 14; k >= 0; k -= 2) { // decrease iteration by two
													// (14 to 0)
					// shift and read two bits (0x03) from bits 14 to 0 (k) in
					// the 16-bit word
					clavrCodes[fov] = (byte) ((word & (0x03 << k)) >> k);
					// System.out.println("CLAVR: "+clavrCodes[fov][i-1]);
					fov++;
					if (fov == 409) {
						break; // break after max fov = 409 = (8 * 51) + 1
					}
				}
			}
		} else {
			// If CLAVR is disabled
			raf.skipBytes(104);
		}
	}

	public byte getScanLineBitField0() {
		return scanLineBitField0;
	}

	public short getSatelliteDrift() {
		return satelliteDrift;
	}

	public int getScanDayOfYear() {
		return scanDayOfYear;
	}

	public int getScanLine() {
		return scanLine;
	}

	public long getScanTime() {
		return scanTime;
	}

	public int getScanYear() {
		return scanYear;
	}

	public int[][] getBackScan() {
		return backScan;
	}

	public short getHrptID() {
		return hrptID;
	}

	public byte getHrptIDbit8() {
		return hrptIDbit8;
	}

	public int[] getRampCal() {
		return rampCal;
	}

	public int getPatchTemp() {
		return patchTemp;
	}

	public int[] getPrtReading() {
		return prtReading;
	}

	public int[][] getSpaceData() {
		return spaceData;
	}

	public byte[] getClavrCodes() {
		return clavrCodes;
	}

	public byte getClavrStatus() {
		return clavrStatus;
	}

	public int getSpacecraftAltitude() {
		return spacecraftAltitude;
	}

	public short getPitchAngle() {
		return pitchAngle;
	}

	public short getRollAngle() {
		return rollAngle;
	}

	public short getYawAngle() {
		return yawAngle;
	}

	public short[] getSolarZenithAngle() {
		return solarZenithAngle;
	}
	
	public int[] getTiePointLat() {
		return tiePointLat;
	}

	public int[] getTiePointLon() {
		return tiePointLon;
	}

	public int[][] getVisIntercept1() {
		return visIntercept1;
	}

	public int[][] getVisIntercept2() {
		return visIntercept2;
	}

	public int[][] getVisIntersection() {
		return visIntersection;
	}

	public int[][] getVisSlope1() {
		return visSlope1;
	}

	public int[][] getVisSlope2() {
		return visSlope2;
	}

	public int[][] getIrCoefficient1() {
		return irCoefficient1;
	}

	public int[][] getIrCoefficient2() {
		return irCoefficient2;
	}

	public int[][] getIrCoefficient3() {
		return irCoefficient3;
	}

	public int getChan3Status(){
		return scanLineBitField0;
	}
	
	abstract int getQualityIndicatorBitField();
	abstract int getScanLineQualityFlags();
	abstract int getFrameSyncErrorCount();
	abstract boolean getHasCalibrationErrors();
	abstract void readNavigationStatusBitField(ucar.unidata.io.RandomAccessFile raf) throws IOException;

}
