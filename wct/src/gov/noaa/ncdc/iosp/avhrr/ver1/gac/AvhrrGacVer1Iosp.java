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

import gov.noaa.ncdc.iosp.VariableInfoManager;
import gov.noaa.ncdc.iosp.avhrr.ArsHeader;
import gov.noaa.ncdc.iosp.avhrr.AvhrrConstants;
import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;
import gov.noaa.ncdc.iosp.avhrr.ver1.TBMHeader;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/*
 * ISOP for AVHRR GAC pre KLM data.  
 * see http://www2.ncdc.noaa.gov/docs/podug/index.htm for more information
 * This iosp covers all 3 variations of the dataset  (excluding klm)
 * @author Arthur Fotos
 * @since 2007-05-29
 *
 * @version 2.2 
 *Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */


public class AvhrrGacVer1Iosp extends AbstractIOServiceProvider {
	
	private static final Logger logger = Logger.getLogger(AvhrrGacVer1Iosp.class.getName());
    private VariableInfoManager varInfoManager = new VariableInfoManager();

	protected AvhrrGacVer1io newFile = new AvhrrGacVer1io();

	private ucar.unidata.io.RandomAccessFile raFile = null;

	
	/**
	 * determine if valid Avhrr GAC Ver1 file
	 * reads header record to determine datatype and year and day
	 * to determine is file is valid
	 * 
	 */
	public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf) {
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		int offset = 0;
        int startYear;
		try {
            if (TBMHeader.hasHeader(raf)){  
                offset = AvhrrConstants.TBM_HEADER_SIZE ;
            }
            else if (ArsHeader.hasARSHeader(raf)){  
                offset = AvhrrConstants.ARS_LENGTH;
            }
            raf.seek(offset + 1);
			byte dataType = (byte)AvhrrFile.readFourBitFlag(raf.readByte(),4);
			short temp = raf.readShort();
			startYear = AvhrrFile.readSevenBitFlag(temp, 9);

			int startDay = AvhrrFile.readNineBitFlag(temp, 0);	
			
			if (dataType != 2) {
				varInfoManager.clearAll();
				return false;
			}
			//noaa-14 stopped around late 2002/  if after 2002, probably a KMLN file
			if (startYear > 3 && startYear < 78) {
				varInfoManager.clearAll();
				return false;
			}
			if (startDay   < 1   || startDay > 366) {
				varInfoManager.clearAll();
				return false;
			}
			return true;		
        
        } catch (Exception e) {
        	varInfoManager.clearAll();
        	return false;
        }
	}

	public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException{
		this.raFile = raf;
		try {
			newFile.setAttributesAndVariables(raf, ncfile);
		} catch (IOException e) {logger.severe(e.toString());
			throw new IOException();
		}
	}

	public ucar.ma2.Array readData(Variable v2, Section section) throws IOException, InvalidRangeException {
		ucar.ma2.Array dataArray = null;
		try {
			dataArray = newFile.getVariableValues(v2, section);
		} catch (InvalidRangeException e) {logger.severe(e.toString());
		}
		return dataArray;
	}

	// methods not currently developed.
	public Array readNestedData(Variable v2, Section section) throws IOException, InvalidRangeException {
		throw (new IllegalStateException("No nested variables in AVHRR datasets."));
	}

	public String getDetailInfo() {
		return "";
	}

	public boolean syncExtend() {
		return false;
	}

	public String toStringDebug(Object o) {
		return null;
	}

	public boolean sync() {
		return false;
	}

	public void close() throws IOException {
		varInfoManager.clearAll();
		if (this.raFile != null) {
			this.raFile.close();
		}
		this.newFile = null;
		varInfoManager.clearAll();
		return;
	}

	public void setProperties(List iospProperties) {
		return;
	}

	public void setSpecial(Object arg0) {
	}
	
	
	/**
	 * non-standard method - this method is used to determine satellite number
	 * assumes that the file is valid avhrr gac dataset
	 */
	public static String getSatelliteName(ucar.unidata.io.RandomAccessFile raf){
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		int offset = 0;
		int craftid = 0;
		String name = null;
		try{
			if(TBMHeader.hasHeader(raf)){
				offset += AvhrrConstants.TBM_HEADER_SIZE;
			}
			raf.seek(offset);
			craftid = raf.readByte();
			switch(craftid){
			case 1: {
				raf.skipBytes(1);
				short temp = raf.readShort();
				int startYear = AvhrrFile.readSevenBitFlag(temp, 9);
				if(88 > startYear){
					name = "TIROS-N";
				}else{
					name = "NOAA-11";
				}
			}
			case 2:{
				name = "NOAA-6";
				break;
			}
			case 3:{
				name = "NOAA-14";
				break;
			}
			case 4:{
				name = "NOAA-7";
				break;
			}
			case 5:{
				name = "NOAA-12";
				break;
			}
			case 6:{
				name = "NOAA-8";
				break;
			}
			case 7:{
				name = "NOAA-9";
				break;
			}
			case 8:{
				name = "NOAA-10";
				break;
			}
			}
		}catch(Exception e){
			
		}
		return name;
	
	}
	
	/**
	 * non-standard method - this method is used to determine satellite name
	 * assumes that the file is valid avhrr gac dataset
	 */

	/**
	public static String getSatelliteName(String dsname){
    		int index = dsname.indexOf("GHRR.");
    		String satKey = dsname.substring(index + 5, index + 7);
    		if("TN".equals(satKey)){
    			return TIROSN;
    		}else if("NA".equals(satKey)){
    			return NOAA6;
    		}else if("NB".equals(satKey)){
    			return "NOAA-B";
    		}else if("NC".equals(satKey)){
    			return NOAA7;
    		}else if("ND".equals(satKey)){
    			return NOAA12;
    		}else if("NE".equals(satKey)){
    			return NOAA8;
    		}else if("NF".equals(satKey)){
    			return NOAA9;
    		}else if("NG".equals(satKey)){
    			return NOAA10;
    		}else if("NH".equals(satKey)){
    			return NOAA11;
    		}else if("NI".equals(satKey)){
    			return NOAA13;
    		}else if("NJ".equals(satKey)){
    			return NOAA14;
    		}else{
    			return "";
    		}
	}
*/
	
	/**
	 * non-standard method - this method is used to determine satellite name
	 * assumes that the file is valid avhrr gac dataset
	 */
	public static String getSatelliteName(int craftid, int startYear){
		String name = "";
			switch(craftid){
			case 1: {
				if(88 > startYear){
					name = "TIROS-N";
				}else{
					name = "NOAA-11";
				}
			}
			case 2:{
				name = "NOAA-6";
				break;
			}
			case 3:{
				name = "NOAA-14";
				break;
			}
			case 4:{
				name = "NOAA-7";
				break;
			}
			case 5:{
				name = "NOAA-12";
				break;
			}
			case 6:{
				name = "NOAA-8";
				break;
			}
			case 7:{
				name = "NOAA-9";
				break;
			}
			case 8:{
				name = "NOAA-10";
				break;
			}
			}
		return name;
	}	
	
	
	
	
	/**
	 * non standard method - returns number of scanlines in a avhrr  gac dataset
	 * assumes file is valid avhrr gac dataset
	 * @param raf
	 * @return
	 */
	public int getScanLineCount(ucar.unidata.io.RandomAccessFile raf){
		int count = 0;
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		//location of dataset name bytes
		int offset = 8;
		try {
            if (TBMHeader.hasHeader(raf)){  
                offset = offset + AvhrrConstants.TBM_HEADER_SIZE ;
            }
            raf.seek(offset );
            count = raf.readUnsignedShort();
		}catch(Exception e){
			e.printStackTrace();
		}		
		return count;
	}

    @Override 
	public String getFileTypeDescription() {
		return "AvhrrGacVer1Iosp";
	}

    @Override 
	public String getFileTypeId() {
		return "AvhrrGacVer1";
	}

}
