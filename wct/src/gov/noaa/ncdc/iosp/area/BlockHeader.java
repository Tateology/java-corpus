package gov.noaa.ncdc.iosp.area;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;


/**
 * reads block header of line prefix for McIdas area file
 * http://www.osd.noaa.gov/gvar/documents/G023_504.02_DCN3_Sect_3.pdf
 * Table 3-5
 * consists of 30 8-bit words  (30 bytes long)
 * @author afotos
 * Mar 10, 2008
 */
public class BlockHeader {

	private static short BLOCK_SIZE = 30;

	
	private short blockId;

	private byte wordSize;

	private int wordCount;

	private short productId;

	private byte repeatFlag;

	private byte gvarVersionNum;

	private byte dataValid;

	private byte asciiBinary;

	private byte spsId;

	private String rangeWord;

	private int blockCount;

	private String spsTime;

	private short errorCheck;

	public void readBlockHeader(ucar.unidata.io.RandomAccessFile raf) {
		try {
//System.out.println("pos before block header--> " + raf.getFilePointer());
			blockId = (short) raf.readUnsignedByte();
			wordSize = raf.readByte();
			wordCount = raf.readUnsignedShort();
			productId = raf.readShort();
			repeatFlag = raf.readByte();
			gvarVersionNum = raf.readByte();
			dataValid = raf.readByte();
			asciiBinary = raf.readByte();
			spsId = raf.readByte();
			byte temp = raf.readByte();
			rangeWord = Integer.toString(AvhrrFile.readFourBitFlag(temp,4));
			int i = AvhrrFile.readFourBitFlag(temp,0);
			rangeWord += " - " + Integer.toHexString(i);
			blockCount = raf.readUnsignedShort();
			raf.skipBytes(2);
			byte[] data = new byte[8];
			for(int j=0;j<8;j++){
				data[j] = raf.readByte();
			}
			spsTime = AreaFileUtil.readBCDDate(data);
			raf.skipBytes(4);
			errorCheck = raf.readShort();

//System.out.println("pos after block header --->" + raf.getFilePointer());
/**
System.out.println("#################  BlockHeader  Vars   ###########");	
			System.out.println("blockid: " + blockId);
			System.out.println("wordSize: " + wordSize);
			System.out.println("wordCount: " + wordCount);
			System.out.println("prodcutId: " + productId);
			System.out.println("repeatFlag : " + repeatFlag);
			System.out.println("gvarVersionNum : " + gvarVersionNum);
			System.out.println("dataValid : " + dataValid);
			System.out.println("asciiBinary: " + asciiBinary);
			System.out.println("spsId : " + spsId);
			System.out.println("rangeWord: " + rangeWord);
			System.out.println("blockCount : " + blockCount);
			System.out.println("blockCount: " + blockCount);
			System.out.println("spsTime: " + spsTime);
			System.out.println("errorCheck : " + errorCheck);
*/
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static short getBLOCK_SIZE() {
		return BLOCK_SIZE;
	}

	public short getBlockId() {
		return blockId;
	}

	public byte getWordSize() {
		return wordSize;
	}

	public int getWordCount() {
		return wordCount;
	}

	public short getProductId() {
		return productId;
	}

	public byte getRepeatFlag() {
		return repeatFlag;
	}

	public byte getGvarVersionNum() {
		return gvarVersionNum;
	}

	public byte getDataValid() {
		return dataValid;
	}

	public byte getAsciiBinary() {
		return asciiBinary;
	}

	public byte getSpsId() {
		return spsId;
	}

	public String getRangeWord() {
		return rangeWord;
	}

	public int getBlockCount() {
		return blockCount;
	}

	public String getSpsTime() {
		return spsTime;
	}

	public short getErrorCheck() {
		return errorCheck;
	}

}
