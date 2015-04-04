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

package gov.noaa.ncdc.iosp.avhrr;

public interface AvhrrConstants {
	
	
    public static final String VIS_RADIANCE_UNIT = "mW / (m^2 sr nm)";
    public static final String IR_RADIANCE_UNIT = "mW / (m^2 sr cm^-1)";
    public static final String TEMPERATURE_UNIT = "K";
    public static final String REFLECTANCE_UNIT = "%";
    public static final String COUNTS_UNIT = "DL"; // dimensionless
    public static final String UNIT_DEG = "deg";
    public static final String UNIT_M = "m";
    public static final String UNIT_MM = "mm";
    public static final String UNIT_KM = "km";
    public static final String UNIT_M_PER_S = "m/s";
    public static final String UNIT_KM_PER_S = "km/s";
    public static final String UNIT_YEARS = "year";
    public static final String UNIT_DAYS = "day";
    public static final String UNIT_MINUTES = "min";
    public static final String UNIT_MS = "ms";  
    public static final String UNIT_DATE = "date";
    public static final String UNIT_BYTES = "bytes";
    public static final String UNIT_BITS = "bits";
    
    public final static int CHAR_SIZE = 1; 
    public static final int ARS_LENGTH = 512;       // ARS Header
    public static final int DSH_LENGTH = 4608;       // Data Set Header
    public static final int DSR_LENGTH = 4608;       // Data Set Record
    public static final int HR_COUNT_POS = 15;      // Header Record Count Position
    public static final int DR_COUNT_POS = 129;      // Data Record Count Position
    public static final int FORMAT_VER_POS = 5;
    
    public static final int RAW_SCENE_RASTER_WIDTH = 409;   
    public static final int SCAN_LINE_LENGTH = 682;
	
    public static final float INVALID_DATA_VALUE = -100;
	
	
	//TMB Header Size (bytes)
    public static int TBM_HEADER_SIZE = 122;
    
	//Ars Header Byte Size
	public static final int ARS_HEADER_BYTES = 512;
	
	//Header Record Sizes
	
	
	//Data Record Bytes Sizes
	public static final int DATA_RECORD_BYTES = 4608; 
	public static final int SCAN_LINE_INFO_BYTES = 24;
	public static final int QUALITY_INDICATORS_BYTES = 24;
	public static final int CALIBRATION_COEFF_BYTES = 264;
	public static final int NAVIGATION_BYTES = 744;
	public static final int HRPT_FRAME_BYTES = 208;
	public static final int AVHRR_DATA_BYTES = 2736;
	public static final int DIGITAL_B_BYTES = 16;
	public static final int ANALOG_HOUSEKEEPING_BYTES = 32;
	public static final int CLAVR_BYTES = 112;
	
	
    public static final String[] CH_STRINGS = {
        "1", //CH_1
        "2", //CH_2
        "3a", //CH_3A
        "3b", //CH_3B
        "4", //CH_4
        "5", //CH_5
    };

    public static final int[] CH_DATASET_INDEXES = {
        0, // CH_1
        1, // CH_2
        2, // CH_3A
        2, // CH_3B
        3, // CH_4
        4, // CH_5
    };

    //satellite names
    static String TIROSN = "TIROSs-N";
    static String NOAA6 = "NOAA-6";
    static String NOAA7 = "NOAA-7";
    static String NOAA8 = "NOAA-8";
    static String NOAA9 = "NOAA-9";
    static String NOAA10 = "NOAA-10";
    static String NOAA11 = "NOAA-11";
    static String NOAA12 = "NOAA-12";
    static String NOAA13 = "NOAA-13";
    static String NOAA14 = "NOAA-14";
    static String NOAA15 = "NOAA-15";
    static String NOAA16 = "NOAA-16";
    static String NOAA17 = "NOAA-17";
    static String NOAA18 = "NOAA-18";
    static String NOAA19 = "NOAA-19";
    
}
