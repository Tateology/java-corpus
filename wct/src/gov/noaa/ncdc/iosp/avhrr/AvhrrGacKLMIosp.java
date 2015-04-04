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


package gov.noaa.ncdc.iosp.avhrr;

import gov.noaa.ncdc.iosp.VariableInfoManager;
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
 * @author Arthur Fotos
 * @since 2008.04.01
 * @version 2.2
 * 
 *Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 *
 */

public class AvhrrGacKLMIosp extends AbstractIOServiceProvider {
	
	private static final Logger logger = Logger.getLogger(AvhrrGacKLMIosp.class.getName());

    private VariableInfoManager varInfoManager = new VariableInfoManager();

	public AvhrrGacKLMIosp() {
		
	}

	protected AVHRRio newFile = new AVHRRio();

	private ucar.unidata.io.RandomAccessFile raFile = null;

	public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf) {
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		int offset = 0;
		int dataTypeID;
        int startYear;
        int startDayOfYear;
		try {
            if (ArsHeader.hasARSHeader(raf)){  
                offset = AvhrrConstants.ARS_LENGTH;
            }
            raf.seek(offset + 3);
//          System.out.println("4th byte: " + raf.readChar());
            raf.seek((offset + 77)-1);
            dataTypeID = raf.readUnsignedShort();
            raf.seek((offset + 85)-1);
            startYear = raf.readUnsignedShort();
            raf.seek((offset + 87)-1);
            startDayOfYear = raf.readUnsignedShort();
        } catch (Exception e) {
        	varInfoManager.clearAll();
        	return false;
        }
            
        if (dataTypeID != 2) {
        	varInfoManager.clearAll();
        	return(false);
        }
        //need to determine start year
        if (startYear < 1998 || startYear > 2020) {
        	varInfoManager.clearAll();
        	return(false);
        }
        if (startDayOfYear   < 1   || startDayOfYear > 366) {
        	varInfoManager.clearAll();
        	return false;
        }
        
//       System.out.println("valid gac klm file= true");
        return true;		
	}

	public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
		this.raFile = raf;
		try {
			newFile.setAttributesAndVariables(raf, ncfile);
		} catch (InvalidRangeException e) {logger.severe(e.toString());
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
//		System.out.println("Closing " + this.newFile);
		if (this.raFile != null) {
//			System.out.println("Releasing " + this.raFile.getLocation());
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
	 * non-standard method -
	 */
	public static String getSatelliteName(ucar.unidata.io.RandomAccessFile raf){
		raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
		String satName = "";
		int offset = 0;
		short satID;
		try {
            if (ArsHeader.hasARSHeader(raf)){  
                offset = AvhrrConstants.ARS_LENGTH;
            }
            raf.seek(offset +73 );
    		 satID = (short)raf.readUnsignedByte();
//			System.out.println("Sat ID: " + satID);
			switch(satID){
				case 2: satName= "NOAA-16"; break;
				case 4: satName= "NOAA-15"; break;
				case 6: satName= "NOAA-17"; break;
				case 7: satName= "NOAA-18"; break;
				case 8: satName= "NOAA-N'"; break;				
				case 12: satName= "Metop-A"; break;
				default: return "";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return satName;
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
		//location of scanline count bytes
		int offset = 128;
		try {
            if (TBMHeader.hasHeader(raf)){  
                offset = offset + AvhrrConstants.ARS_LENGTH ;
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
		return "AvhrrGacKLMIosp";
	}

    @Override 
	public String getFileTypeId() {
		return "AvhrrGacKLM";
	}

}
