/*
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


package gov.noaa.ncdc.iosp.avhrr.ver1;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Reads TBMHeader
 * @author arthur.fotos@noaa.gov
 * 02/10/2008
 *
 * @version 2.2 
 * Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */

public class TBMHeader {
	
	private static final Logger logger = Logger.getLogger(TBMHeader.class.getName());

	private String datasetName;
	private String copyMode;
	private String startLat;
	private String endLat;
	private String startLon;
	private String endLon;
	private String startHour;
	private String startMinute;
	private String numOfMinutes;
	private String appendData;
	private String channelSelected;
	private String wordSize;
	
	public TBMHeader(){
		
	}

	public void readTBMHeader(ucar.unidata.io.RandomAccessFile raf){
		try{
			raf.seek(0);
			datasetName = raf.readString(74);
			copyMode = raf.readString(1);
			startLat = raf.readString(3);
			endLat = raf.readString(3);
			startLon = raf.readString(4);
			endLon = raf.readString(4);
			startHour = raf.readString(2);
			startMinute = raf.readString(2);
			numOfMinutes = raf.readString(3);
			appendData = raf.readString(1);
			//not really a string. not sure its relevant to determine which channels were selected
			channelSelected = raf.readString(20);
			wordSize = raf.readString(2);
			raf.skipBytes(2);
		
			logger.fine("dataset Name: " + datasetName);
			logger.fine("Total/Selective coopy: " + copyMode);			
			logger.fine("start lat: " + startLat);			
			logger.fine("end lat: " + endLat);			
			logger.fine("start lon: " + startLon);			
			logger.fine("end lon: " + endLon);			
			logger.fine("Start Hour: " + startHour);
			logger.fine("Start Minute: " + startMinute );
			logger.fine("Number of Minutes: " + numOfMinutes);
			logger.fine("Append Data Selection: " + appendData);
			logger.fine("Channels selected: " + channelSelected);
			logger.fine("Sensor Data Word size: " + wordSize);

		}catch(Exception e){logger.severe(e.toString());
		}
	
	}
	
	
	/**
	 * determines if the avhhr file has a TBM header
	 * first test to see if dataset name is available
	 * since its passed the isValid() test we know "2" should be present in byte #2. check byte 2, then advance and check byte 124
	 * @param raf
	 * @return
	 */
	public static boolean hasHeader(ucar.unidata.io.RandomAccessFile raf){

		boolean hasHeader = false;
		try { 
			String path = raf.getLocation();
			String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
			raf.seek(30);
			String datasetName = raf.readString(44);
			String copyMode = raf.readString(1);
			if(datasetName.startsWith("NSS.GHRR.")){
				hasHeader = true;
			}else if("T".equals(copyMode) || "S".equals(copyMode)){
				hasHeader = true;
			}
			
		} catch (IOException e) {
			logger.severe(e.toString());
		}
		return hasHeader;
	}
	
	/**
	 * for testing only
	 * reads the TBM header of an avhrr file
	 * @param args
	 */
	public static void main(String[] args){
//		String filename = "/home/afotos/test_files/avhrr/class/NSS.GHRR.NG.D89193.S1317.E1510.B1462930.GC";
		String filename = "/home/afotos/test_files/axel/NSS.GHRR.TN.D79182.S2123.E2313.B0368788.GC";
		ucar.unidata.io.RandomAccessFile raf = null;
		try{
			raf = new ucar.unidata.io.RandomAccessFile(filename,"r");
			raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		}catch(Exception e){
			e.printStackTrace();
		}
		TBMHeader header = new TBMHeader();
		header.readTBMHeader(raf);
	}
	
}
