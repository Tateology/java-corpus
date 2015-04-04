package gov.noaa.ncdc.iosp.area;

import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;
import edu.wisc.ssec.mcidas.AreaFile;



public class GoesPrefix {
	
	short retrace;
	short craftId;
	short frameCode;
	short changeCode;
	short stepCode;
	short lineDelay;
	short sensor;
	short grayScale;
	short transMode;
	short scanCount; 
	short scanMode;
	short gridFlag;	
	short year;
	short year2;
	short day;
	short day2;
	short hour; 
	short minute;
	short ms;
	
	
	public void readPrefix(RandomAccessFile raf) throws IOException{
//		System.out.println("start pos--> " + raf.getFilePointer());
		retrace = (short)raf.readUnsignedByte();
		craftId = (short)raf.readUnsignedByte();
		raf.skipBytes(1);
		frameCode = (short)raf.readUnsignedByte();
		changeCode = (short)raf.readUnsignedByte(); 
		stepCode = (short)raf.readUnsignedByte();
		lineDelay = (short)raf.readUnsignedByte();
		sensor = (short)raf.readByte();
		grayScale = (short)raf.readUnsignedByte();
		transMode = (short)raf.readUnsignedByte();
		scanCount = (short)raf.readUnsignedByte();
		raf.skipBytes(1);	//BCD chars
		raf.skipBytes(1);	//BCD chars
		scanMode = (short)raf.readUnsignedByte();
	
		raf.skipBytes(3);	//beta cound
		
		gridFlag = (short)raf.readUnsignedByte();
		raf.skipBytes(12);
		
		year = (short)(raf.readUnsignedByte());
		year2 = (short)raf.readUnsignedByte();
		day = (short)raf.readUnsignedByte();
		day2 = (short)raf.readUnsignedByte();
		hour = (short)raf.readUnsignedByte();
		minute = (short)raf.readUnsignedByte();
		ms = (short)raf.readUnsignedByte();
		
		raf.skipBytes(91);
/**
		System.out.println("retrace---> " + retrace);
		System.out.println("craftId---> " + craftId);
		System.out.println("frameCode---> " + frameCode);
		System.out.println("changeCode---> " + changeCode);
		System.out.println("stepCode---> " + stepCode);
		System.out.println("lineDelay---> " + lineDelay);
		System.out.println("sensor---> " + sensor);
		System.out.println("grayScale---> " + grayScale);
		System.out.println("transMode---> " + transMode);
		System.out.println("scanCount---> " + scanCount);
		System.out.println("scanMode---> " + scanMode);
		System.out.println("gridFlag---> " + gridFlag);
		System.out.println("year---> " + year);
		System.out.println("year2--> " + year2);
		
		System.out.println("day---> " + day);
		System.out.println("day2---> " + day2);
		
		System.out.println("hour---> " + hour);
		System.out.println("minute---> " + minute);
		
		System.out.println("end pos --> " + raf.getFilePointer());
		
//		System.out.println("---> " + );
*/		
	}
	
	

	
	public static void main(String[] args) throws Exception{
		String filename = "C:/goes-area/preGvar/goes05.1984.011.0100.AREA_IR";
		RandomAccessFile raf = new RandomAccessFile(filename,"r");
		GoesPrefix gp = new GoesPrefix();
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		AreaFile af = new AreaFile(raf.getLocation());
		int[] dir = af.getDir();
		int offset = dir[33];
		int validCode = dir[35];
		System.out.println("offset---> " + offset);
		System.out.println("validCode---> " + validCode);
		raf.seek(offset);

		for(int i=0;i<4;i++){
			if(validCode != 0){
				raf.skipBytes(4);
			}
			gp.readPrefix(raf);
			raf.skipBytes(3822);
		}
		

		
	}




	public short getSensor() {
		return sensor;
	}

}
