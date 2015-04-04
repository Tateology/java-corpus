package gov.noaa.ncdc.iosp.area;

import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Logger;

public class AreaDir {
	
    private static final Logger logger = Logger.getLogger(AreaDir.class.getName());

    
    
	private int status;
	
	private int version;
	
	private int satId;
	
	private int imageDate;
	
	private int imageTime;
	
	private int yCoord;
	
	private int xCoord;

	private int zCoord;
	
	private int numLine;
	
	private int numEle;
	
	private int bytesPixel;
	
	private int linesRes;
	
	private int elemRes;
	
	private int numChan;
	
	private int prefixBytes;
	
	private int projNum;
	
	private int creationDate;
	
	private int creationTime;
	
	private int filterMap;
	
	private int imageIdNum;
	
	private String commnets;
	
	private int priKeyCalib;
	
	private int priKeyNav;
	
	private int secKeyNav;
	
	private int validityCode;
	
	private int band8;
	
	private int actImgDate;
	
	private int actImgTime;
	
	private int actStartScan;
	
	private int lenPrefixDoc;
	
	private int lenPrefixCal;
	
	private int lenPrefixLev;
	
	private String srcType;
	
	private String calType;
	
	private int avgSample;
	
	private int poesSignal;
	
	private int poesUpDown;
	
	private String origSrcType;
	
	
	public void readScanLine(ucar.unidata.io.RandomAccessFile raf) throws IOException, EOFException{
		raf.order(ucar.unidata.io.RandomAccessFile.LITTLE_ENDIAN);
		raf.seek(0);
		status = raf.readInt();
		version = raf.readInt();
		// CLASS switched their McIDAS processing machines in 2012 and now produce LITTLE_ENDIAN byte order.
		// Files ordered from CLASS before this switch will be BIG_ENDIAN.
		// weird version indicates Big Endian file
		if (version > 100000) {
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
			raf.seek(0);
			status = raf.readInt();
			version = raf.readInt();
		}
		satId = raf.readInt();
		imageDate = raf.readInt();
		imageTime = raf.readInt();
		yCoord = raf.readInt();
		xCoord = raf.readInt();
		zCoord = raf.readInt();
		numLine = raf.readInt();
		numEle = raf.readInt();
		bytesPixel = raf.readInt();
		linesRes = raf.readInt();
		elemRes = raf.readInt();
		numChan = raf.readInt();
		prefixBytes = raf.readInt();
		projNum = raf.readInt();
		creationDate = raf.readInt();
		creationTime = raf.readInt();
		filterMap = raf.readInt();
		imageIdNum = raf.readInt();
		raf.skipBytes(16);
		commnets = raf.readString(32);
		priKeyCalib = raf.readInt();
		priKeyNav = raf.readInt();
		secKeyNav = raf.readInt();
		validityCode = raf.readInt();
		//pdl
		raf.skipBytes(32);
		band8 = raf.readInt();
		actImgDate = raf.readInt();
		actImgTime = raf.readInt();
		actStartScan = raf.readInt();
		lenPrefixDoc = raf.readInt();
		lenPrefixCal = raf.readInt();
		lenPrefixLev = raf.readInt();
		srcType = raf.readString(4);
		calType = raf.readString(4);
		avgSample = raf.readInt();
		poesSignal = raf.readInt();
		poesUpDown = raf.readInt();
		origSrcType = raf.readString(4);		
		raf.skipBytes(28);
		logger.fine("file pos at end of read: " + raf.getFilePointer());
	/**	
		System.out.println("status: " + status);
		System.out.println("version: " + version );
		System.out.println("satId: " + satId);
		System.out.println("imageDate : " + imageDate);
		System.out.println("imageTime: " + imageTime);
		System.out.println("yCoord : " + yCoord);
		System.out.println("xCoord: " + xCoord );
		System.out.println("zCoord : " + zCoord);
		System.out.println("numLine: " +numLine );
		System.out.println("numEle: " + numEle);
		System.out.println("bytesPixel : " + bytesPixel );
		System.out.println("linesRes : " + linesRes);
		System.out.println("elemRes : " + elemRes);
		System.out.println("numChan: " + numChan);
		System.out.println("prefixBytes: " + prefixBytes );
		System.out.println("projNum: " + projNum);
		System.out.println("creationDate: " + creationDate);
		System.out.println("creationTime : " + creationTime);
		System.out.println("filterMap: " +filterMap );
		System.out.println("imageIdNum: " + imageIdNum);
		System.out.println("commnets : " + commnets);
		System.out.println("priKeyCalib: " + priKeyCalib);
		System.out.println("priKeyNav: " + priKeyNav );
		System.out.println("secKeyNav: " + secKeyNav);
		System.out.println("validityCode: " + validityCode);
		System.out.println("band8: " + band8 );
		System.out.println("actImgDate : " + actImgDate);
		System.out.println("actImgTime: " + actImgTime );
		System.out.println("actStartScan: " + actStartScan );
		System.out.println("lenPrefixDoc: " + lenPrefixDoc);
		System.out.println("lenPrefixCal: " + lenPrefixCal);
		System.out.println("lenPrefixLev: " + lenPrefixLev);
		System.out.println("srcType: " + srcType);
		System.out.println("calType: " + calType);
		System.out.println("avgSample: " + avgSample);
		System.out.println("poesSignal: " +poesSignal );
		System.out.println("poesUpDown: " + poesUpDown);
		System.out.println("origSrcType : " + origSrcType);
*/
	}

	public static void main(String[] args){
		String file = "/home/afotos/testfiles/goes-area/goes11.2007.295.170015.BAND_01";
//		String file = "/home/afotos/testfiles/goes-area/preGvar/goes02.1979.011.0100-1.AREA_IR";
		//open the file using same object as netcdf
		try {
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(file,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
			AreaDir ad = new AreaDir();
			ad.readScanLine(raf);
		}catch(Exception e){
			e.printStackTrace();
		}
			
		
	}

	public int getStatus() {
		return status;
	}

	public int getVersion() {
		return version;
	}

	public int getSatId() {
		return satId;
	}

	public int getImageDate() {
		return imageDate;
	}

	public int getImageTime() {
		return imageTime;
	}

	public int getYCoord() {
		return yCoord;
	}

	public int getXCoord() {
		return xCoord;
	}

	public int getZCoord() {
		return zCoord;
	}

	public int getNumLine() {
		return numLine;
	}

	public int getNumEle() {
		return numEle;
	}

	public int getBytesPixel() {
		return bytesPixel;
	}

	public int getLinesRes() {
		return linesRes;
	}

	public int getElemRes() {
		return elemRes;
	}

	public int getNumChan() {
		return numChan;
	}

	public int getPrefixBytes() {
		return prefixBytes;
	}

	public int getProjNum() {
		return projNum;
	}

	public int getCreationDate() {
		return creationDate;
	}

	public int getCreationTime() {
		return creationTime;
	}

	public int getFilterMap() {
		return filterMap;
	}

	public int getImageIdNum() {
		return imageIdNum;
	}

	public String getCommnets() {
		return commnets;
	}

	public int getPriKeyCalib() {
		return priKeyCalib;
	}

	public int getPriKeyNav() {
		return priKeyNav;
	}

	public int getSecKeyNav() {
		return secKeyNav;
	}

	public int getValidityCode() {
		return validityCode;
	}

	public int getBand8() {
		return band8;
	}

	public int getActImgDate() {
		return actImgDate;
	}

	public int getActImgTime() {
		return actImgTime;
	}

	public int getActStartScan() {
		return actStartScan;
	}

	public int getLenPrefixDoc() {
		return lenPrefixDoc;
	}

	public int getLenPrefixCal() {
		return lenPrefixCal;
	}

	public int getLenPrefixLev() {
		return lenPrefixLev;
	}

	public String getSrcType() {
		return srcType;
	}

	public String getCalType() {
		return calType;
	}

	public int getAvgSample() {
		return avgSample;
	}

	public int getPoesSignal() {
		return poesSignal;
	}

	public int getPoesUpDown() {
		return poesUpDown;
	}

	public String getOrigSrcType() {
		return origSrcType;
	}
}
