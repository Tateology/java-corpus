package gov.noaa.ncdc.iosp.area;

import edu.wisc.ssec.mcidas.AreaFile;
import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;


public class AreaLinePrefix {

	private int validityCode;
	
	private short crc;
	
	private String scanStat;
	
	private byte[] bcdtime = new byte[16];
	
	private Block0 b0;
	
	private BlockHeader bh;
	
	private LineDoc ldoc;

	
	public void readPrefix(ucar.unidata.io.RandomAccessFile raf) {
		try {
//			System.out.println("pos before prefix read-->" + raf.getFilePointer());
			// validity code
			validityCode = raf.readInt();
			// crc
			crc = raf.readShort();
			// scan stat
			scanStat = raf.readString(4);
			
			// bcdtime
			for(int i=0;i<8;i++){
				byte temp = raf.readByte();
				bcdtime[i*2] = (byte)AvhrrFile.readFourBitFlag(temp,0);
				bcdtime[i*2 + 1] = (byte)AvhrrFile.readFourBitFlag(temp,4);
			}
			// blkhdr
			bh = new BlockHeader();
			bh.readBlockHeader(raf);
			//linedoc
			ldoc = new LineDoc();
			ldoc.readLinDoc(raf);
			//block0
			b0 = new Block0();
			b0.readBlock0(raf);
			
			//spare
			raf.skipBytes(2);
//			System.out.println("pos after prefix read-->" + raf.getFilePointer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] arg){
//		String inFile="c:/goes-area/goes11.2007.200.000014.BAND_05";	
		String inFile="c:/goes-area/goes12.2006.148.230144.BAND_04";	
//		String inFile="/home/afotos/goes12.2006.148.230144.BAND_03";

		//open the file using same object as netcdf
		try {
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(inFile,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
			AreaFile af = new AreaFile(raf.getLocation());
			int[] dir = af.getDir();
			int offset = dir[33];
			int validCode = dir[35];
//			System.out.println("offset---> " + offset);
//			System.out.println("validCode---> " + validCode);
			raf.seek(offset);
			if(validCode != 0){
//				raf.skipBytes(4);
			}
			AreaLinePrefix alp = new AreaLinePrefix();
			alp.readPrefix(raf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public int getValidityCode() {
		return validityCode;
	}


	public short getCrc() {
		return crc;
	}


	public String getScanStat() {
		return scanStat;
	}


	public byte[] getBcdtime() {
		return bcdtime;
	}


	public Block0 getB0() {
		return b0;
	}


	public BlockHeader getBh() {
		return bh;
	}


	public LineDoc getLdoc() {
		return ldoc;
	}
}
