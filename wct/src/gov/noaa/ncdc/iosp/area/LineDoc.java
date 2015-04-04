package gov.noaa.ncdc.iosp.area;

import java.io.IOException;


/**
 * Defines Line Document area of the line prefix for a McIdas area file
 * http://www.osd.noaa.gov/gvar/documents/G023_504.02_DCN3_Sect_3.pdf
 * Table 3-7
 * @author afotos
 *	3-10-2008
 */
public class LineDoc {

	//spacecraft id
	private int spcid;

	//source SPS
	private int spsid;

	//detector configuration (0= side1,1023=side2)
	private int lside;

	//detector number
	private int lidet; 
	
	//source channel
	private short chan;

	//relative output scan count since start of imaging frame (1 - 1974)
	private int risct;

	private int l1scan;

	private int l2scan;

	private int lpixls;

	//number of words contained in detector record
	private int lwords;

	//value of zonal correction(pixel offset) at western edge
	private int lzcor;

	//current, latest lagged or oldest lagged scan detector data was acquired from
	private int llag;

	public void readLinDoc(ucar.unidata.io.RandomAccessFile raf) {
		try {
//			System.out.println("pos before line doc-->" + raf.getFilePointer());
			spcid = raf.readUnsignedShort();
			spsid = raf.readUnsignedShort();
			lside = raf.readUnsignedShort();

			lidet = raf.readShort();
			
			// System.out.println("detector number: " + lidet);
			chan = raf.readShort();
			// System.out.println("channel number: " + chan);
			short s1 = raf.readShort();
			short s2 = raf.readShort();
			risct = s1 * 1024 + s2;
			l1scan = raf.readUnsignedShort();
			l2scan = raf.readUnsignedShort();
			lpixls = raf.readInt();
			lwords = raf.readInt();
			lzcor = raf.readUnsignedShort();
			llag = raf.readUnsignedShort();
			raf.skipBytes(2);
			
//			System.out.println("pos after line doc -->" + raf.getFilePointer());
/**
System.out.println("#################  LinDoc  Vars   ###########");			
			System.out.println("spcid: "  + spcid);
			System.out.println("spsid : "  + spsid);
			System.out.println("lside: "  + lside);
			System.out.println("lidet: "  + lidet);
			System.out.println("chan : "  +chan);
			System.out.println("risct: "  + risct);
			System.out.println("l1scan : "  + l1scan );
			System.out.println("l2scan: "  +  l2scan);
			System.out.println("lpixls: " + lpixls);
			System.out.println("lwords: "  + lwords);
			System.out.println("lzcor: "  + lzcor);
			System.out.println("llag: "  + llag);
*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile="c:/goes-area/goes11.2007.200.000014.BAND_05";
		//open the file using same object as netcdf
		try {
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(inFile,"r");
			raf.seek(256);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public short getChan() {
		return chan;
	}

	public int getL1scan() {
		return l1scan;
	}

	public int getL2scan() {
		return l2scan;
	}

	public int getLidet() {
		return lidet;
	}

	public int getLlag() {
		return llag;
	}

	public int getLpixls() {
		return lpixls;
	}

	public int getLside() {
		return lside;
	}

	public int getLwords() {
		return lwords;
	}

	public int getLzcor() {
		return lzcor;
	}



	public int getSpcid() {
		return spcid;
	}

	public int getSpsid() {
		return spsid;
	}

	public int getRisct() {
		return risct;
	}

}
