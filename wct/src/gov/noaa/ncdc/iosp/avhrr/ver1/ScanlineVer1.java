package gov.noaa.ncdc.iosp.avhrr.ver1;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;

public abstract class ScanlineVer1 extends AvhrrFile implements IScanlineVer1{
	
	private int scanLine;
	int year;
	int day;
	long utcTime;
	
	private int[] slopeCoeffs = new int[5];
	private int[] interceptCoeffs = new int[5];
	private short numLocationPoints;
	private short[] zenithAngles = new short[51];
	private byte[] zenithDecimals = new byte[51];
	private float[] anchorLat = new float[51];
	private float[] anchorLon = new float[51];
	private short[][] data;
	
	private int clockDrift;
	private byte clockAdjustment;
	
	protected void readScanlineInfo(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		scanLine = raf.readUnsignedShort();
		
		//time code 
		short timeCode = raf.readShort();
		year = readSevenBitFlag(timeCode, 9);
		day = readNineBitFlag(timeCode, 0);			
		utcTime = readUnsignedInt(raf);
	}
	
	protected void readQualityIndicators(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		
	}
	
	protected void readCalibrationCoeffs(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		for(int i=0;i<5;i++){
			slopeCoeffs[i] = raf.readInt();
			interceptCoeffs[i] = raf.readInt();
		}
	}
	
	public short getNumLocationPoints() {
		return numLocationPoints;
	}

	public short[] getZenithAngles() {
		return zenithAngles;
	}

	public float[] getAnchorLat() {
		return anchorLat;
	}

	public float[] getAnchorLon() {
		return anchorLon;
	}

	protected void readLocationData(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		numLocationPoints = (short)raf.readUnsignedByte();

		//zenith angle stored as degrees x 2
		for(int i=0;i<51;i++){
			zenithAngles[i] = (short)raf.readUnsignedByte();
		}
		
		//lat and lon are each stored in two-byte fields in 128ths of a degree (0 to 180E positive, 0 to 180W negative)
		for(int i=0;i<51;i++){
			short s = raf.readShort();
			anchorLat[i] = (float)(s/128.0);
			s = raf.readShort();
			anchorLon[i] = (float)(s/128.0);
		}		
	}
	
	protected void readTelemetry(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		raf.skipBytes(140);
	}
	
	protected void readGACSensorData(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		data = new short[5][409];
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

	protected void readLACSensorData(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		data = new short[5][2048];
		int chanNum = 0;
		int[] shift = { 20, 10, 0 };
		int k = 0;
		int i = 0;
		// Multiple dimensions for Vis / IR channels and for FOV indeces (2728
		// bytes total)
		while (i < 3414 && k < 2048) {
			int rawValue = raf.readInt();
			i++;
			for (int j = 0; j < 3; j++) {
				data[chanNum][k] = (short) ((rawValue & (0x3ff << shift[j])) >> shift[j]);
				if (chanNum == 4) {
					chanNum = 0;
					k++;
					if (k == 2048) {
						break;
					}
				} else {
					chanNum++;
				}
			}
		}		
	}	
	
	
	protected void readZenithAngleDecimals(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		int z = 0;
		int offset = 0;
		long filepos = raf.getFilePointer();
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
	}
	
	protected void readClockDrift(ucar.unidata.io.RandomAccessFile raf) throws Exception {
		short tempS = raf.readShort();
		clockAdjustment = (byte)readOneBitFlag(tempS,0);
		clockDrift = readSevenBitFlag(tempS,1);
		clockDrift =  clockDrift/2;
	}

	public int getScanLine() {
		return scanLine;
	}

	public void setScanLine(int scanLine) {
		this.scanLine = scanLine;
	}

	public int getYear() {
		return year;
	}

	public int getDay() {
		return day;
	}

	public long getUtcTime() {
		return utcTime;
	}

	public int getClockDrift() {
		return clockDrift;
	}

	public byte getClockAdjustment() {
		return clockAdjustment;
	}

	public int[] getInterceptCoeffs() {
		return interceptCoeffs;
	}

	public int[] getSlopeCoeffs() {
		return slopeCoeffs;
	}

	public byte[] getZenithDecimals() {
		return zenithDecimals;
	}

	public short[][] getData() {
		return data;
	}
	

}
