/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
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


/**
 * Interface for 3 versions of the GAC dataset header
 * @author afotos@noaa.gov
 *  12/1/2007
 *
 */
public interface IDataSetHeaderVer1 {

	public void readHeader(ucar.unidata.io.RandomAccessFile raf);

	public String getCalParamId();

	public int getDacsNoFrameSyncErrorCount();
	
	public int getDacsTipParityErrors();

	public int getDacsAuxillaryErrors();
	
	public byte getDacsPsuedoNoise();
	
	public byte getDacsDataSource();
	
	public byte getDacsTapeDir();
	
	public byte getDacsDataMode();
	
	public int getDataGaps();

	public String getDatasetName() ;

	public short getDataType();

	public long getEndTimeUTC() ;

	public int getNumberOfScans();

	public String getProcessingId();

	public int getDataStartYear();

	public int getEndDay();

	public int getEndYear();

	public int getStartDay();

	public byte[] getRampCalibration();

	public short getSpacecraftId();

	public long getStartTimeUTC();

	public byte getAttitudeCorrection();

	public float getEccentricity();

	public int getEpochDay() ;

	public short getEpochYear();

	public float getInclination();

	public float getMeanAnomaly();

	public short getNadirLocCorrection();

	public float getPerigee();

	public int getPitchCorrection();

	public float getRightAscension();

	public int getRollCorrection();

	public float getSemimajorAxis();

	public int getStartYear();

	public long getUtcTOD() ;

	public float getXdotVector();

	public float getXVector() ;

	public int getYawCorrection();

	public float getYdotVector() ;

	public float getYVector();

	public float getZdotVector();

	public float getZVector() ;
}
