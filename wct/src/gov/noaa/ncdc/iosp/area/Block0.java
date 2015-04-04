package gov.noaa.ncdc.iosp.area;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Block0 extends AvhrrFile{

	NumberFormat dayFormat = new DecimalFormat("000");
	NumberFormat hourFormat = new DecimalFormat("00");
	
	private byte spcid;
	private byte spsid;
	private int iscan;
	private byte[] idsub;
	
	private String tcurr;
	private String tched;
	private String tctrl;
	private String tlhed;
	private String tltrl;
	private String tipfs;
	private String tinfs;
	private String tispc;
	private String tiecl;
	private String tibbc;
	private String tistr;
	private String tiran;
	private String tiirt;
	private String tivit;
	private String tclmt;
	private String tiona;
	
	public void readBlock0(ucar.unidata.io.RandomAccessFile raf) throws IOException{
		
		spcid = raf.readByte();
		spsid = raf.readByte();
		iscan = raf.readInt();
		idsub = new byte[16];
		for(int i=0;i<16;i++){
			idsub[i] = raf.readByte();
		}
		
		String[] dates = new String[16];

		for(int z=0;z<16;z++){
			byte[] temp = new byte[8];
			for(int i=0;i<8;i++){
				temp[i] = raf.readByte();
			}
			dates[z] = createDate(temp);
		}
		tcurr = dates[0];
		tched = dates[1];
		tctrl = dates[2];
		tlhed = dates[3];
		tltrl = dates[4];
		tipfs = dates[5];
		tinfs = dates[6];
		tispc = dates[7];
		tiecl = dates[8];
		tibbc = dates[9];
		tistr = dates[10];
		tiran = dates[11];
		tiirt = dates[12];
		tivit = dates[13];
		tclmt = dates[14];
		tiona = dates[15];

		/**
		System.out.println("spcid---> " + spcid);
		System.out.println("spsid---> " + spsid);
		System.out.println("iscan---> " + iscan);
		System.out.println("idsub---> " + idsub);
//		System.out.println("---> " + );
		System.out.println("tcurr---> " + tcurr);
		System.out.println("tched---> " + tched);
		System.out.println("tctrl---> " + tctrl);
		System.out.println("tlhed---> " + tlhed);
		System.out.println("tltrl---> " + tltrl);
		System.out.println("tipfs---> " + tipfs);
		System.out.println("tinfs---> " + tinfs);
		System.out.println("tispc---> " + tispc);
		System.out.println("tiecl---> " + tiecl);
		System.out.println("tibbc---> " + tibbc);
		System.out.println("tistr---> " + tistr);
		System.out.println("tiran---> " + tiran);
		System.out.println("tiirt---> " + tiirt);
		System.out.println("tivit---> " + tivit);
		System.out.println("tclmt---> " + tclmt);
		System.out.println("tiona---> " + tiona);
		*/
		
	}
	
	
	public String createDate(byte[] data){
		String date = "";

		int yearK = readFourBitFlag(data[0],4);
		int yearH = readFourBitFlag(data[0],0);

		int yearT = readFourBitFlag(data[1],4);
		int yearO = readFourBitFlag(data[1],0);

		int doyH = readFourBitFlag(data[2],4);
		int doyT = readFourBitFlag(data[2],0);

		int doyO = readFourBitFlag(data[3],4);
		int hourT = readFourBitFlag(data[3],0);		
		
		int hour0 = readFourBitFlag(data[4],4);
		int minT = readFourBitFlag(data[4],0);

		int minO = readFourBitFlag(data[5],4);
		int secT = readFourBitFlag(data[5],0);

		int secO = readFourBitFlag(data[6],4);
		int msH = readFourBitFlag(data[6],0);

		int msT = readFourBitFlag(data[7],4);
		int msO = readFourBitFlag(data[7],0);		
		
		int year = 1000 * yearK + 100 * yearH + 10 * yearT + yearO;
		int day = 100 * doyH + 10 * doyT + doyO;
		int hour = 10 * hourT + hour0;
		int min = 10 * minT + minO;
		int sec = 10 * secT + secO;
		int ms = 100 * msH + 10 * msT + msO;
		date = Integer.toString(year) +  dayFormat.format(day) + hourFormat.format(hour) + hourFormat.format(min) + hourFormat.format(sec) + dayFormat.format(ms);
		return date;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		

	}


	public byte getSpcid() {
		return spcid;
	}

	public byte getSpsid() {
		return spsid;
	}

	public String getTched() {
		return tched;
	}

	public String getTclmt() {
		return tclmt;
	}

	public String getTctrl() {
		return tctrl;
	}

	public String getTcurr() {
		return tcurr;
	}

	public String getTibbc() {
		return tibbc;
	}

	public String getTiecl() {
		return tiecl;
	}

	public String getTiirt() {
		return tiirt;
	}

	public String getTinfs() {
		return tinfs;
	}

	public String getTiona() {
		return tiona;
	}

	public String getTipfs() {
		return tipfs;
	}

	public String getTiran() {
		return tiran;
	}

	public String getTispc() {
		return tispc;
	}

	public String getTistr() {
		return tistr;
	}

	public String getTivit() {
		return tivit;
	}

	public String getTlhed() {
		return tlhed;
	}

	public String getTltrl() {
		return tltrl;
	}

}

