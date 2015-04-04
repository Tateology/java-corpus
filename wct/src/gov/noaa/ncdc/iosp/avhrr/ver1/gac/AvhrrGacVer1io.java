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

import gov.noaa.ncdc.iosp.VariableInfo;
import gov.noaa.ncdc.iosp.VariableInfoManager;
import gov.noaa.ncdc.iosp.avhrr.AvhrrConstants;
import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;
import gov.noaa.ncdc.iosp.avhrr.LatLonInterpolation;
import gov.noaa.ncdc.iosp.avhrr.ver1.AvhrrCalibratorVer1;
import gov.noaa.ncdc.iosp.avhrr.ver1.DataSetHeaderVer1;
import gov.noaa.ncdc.iosp.avhrr.ver1.DataSetHeaderVer1A;
import gov.noaa.ncdc.iosp.avhrr.ver1.DataSetHeaderVer1B;
import gov.noaa.ncdc.iosp.avhrr.ver1.IDataSetHeaderVer1;
import gov.noaa.ncdc.iosp.avhrr.ver1.IScanlineVer1;
import gov.noaa.ncdc.iosp.avhrr.ver1.TBMHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.unidata.io.RandomAccessFile;


/*
 * Avhrr ver1 (pre KLM) io
 * 
 * @author arhtur fotos
 * 
 *  2/10/2008
 *  
 * @version 2.2 
 *Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */


public class AvhrrGacVer1io {
	
	private static final Logger logger = Logger.getLogger(AvhrrGacVer1io.class.getName());
    private VariableInfoManager varInfoManager = new VariableInfoManager();

	int dsVersion;

	public AvhrrGacVer1io(){
		
	}
	
	private void init(ucar.unidata.io.RandomAccessFile raf){
		try{
			readData();
		}catch(Exception e){
			e.printStackTrace();
		}
		record = new ucar.nc2.Dimension("record",1,true);
	    time = new ucar.nc2.Dimension("time",scanLines,true);
	    channels = new ucar.nc2.Dimension("channels",5,true);
	    pixels = new ucar.nc2.Dimension("pixels",409,true);
	    anchorPoints = new ucar.nc2.Dimension("anchorPoints",51,true);
	    scans = new ucar.nc2.Dimension("scans",scanLines,true);	
		charArray = new ucar.nc2.Dimension("charArray",44,true);
	    isInit = true;
	}
	
	private boolean isInit = false;
	
	private int scanLines;
	
    protected boolean dataHasBeenRead=false;
	
    private ucar.unidata.io.RandomAccessFile raFile = null;
    
	private List<IScanlineVer1> recs = new ArrayList<IScanlineVer1>();
	
	private IDataSetHeaderVer1 headerRec;
	
	
	
	//Define dimensions
    protected ucar.nc2.Dimension time, channels, pixels, anchorPoints, scans, record, charArray;
    
	//Dimension Lists
	protected List<Dimension> dimRecord = new ArrayList<Dimension>();
    protected List<Dimension> dimTime = new ArrayList<Dimension>();
    protected List<Dimension> dimChans = new ArrayList<Dimension>();
    protected List<Dimension> dimPixels = new ArrayList<Dimension>();
    protected List<Dimension> dimAnchorPoints = new ArrayList<Dimension>();
    protected List<Dimension> dimScans = new ArrayList<Dimension>();
    protected List<Dimension> dimChansPixels = new ArrayList<Dimension>();
    protected List<Dimension> dimPixelsScan = new ArrayList<Dimension>();
    protected List<Dimension> dimAnchorPointsScans = new ArrayList<Dimension>();
    protected List<Dimension> dimChanScans = new ArrayList<Dimension>();
    protected List<Dimension> dimChanPixelsScan = new ArrayList<Dimension>();
    protected List<Dimension> dimCharArray = new ArrayList<Dimension>();
    
    //Dimension Variables
    protected VariableInfo TIME, HEADER,CHANNEL, ANCHOR_POINTS,PIXELS, SCAN; 
    
///////////////////  Header Record Variables  ////////////////////////////    
    
    // General Information
    protected VariableInfo SPACECRAFT_ID, DATA_TYPE, START_YEAR, START_DAY, START_UTC, NUM_OF_SCANS, END_YEAR, END_DAY, END_UTC,
			PROCESSING_BLOCK_ID, RAMP_CALIBRATION_1, RAMP_CALIBRATION_2, RAMP_CALIBRATION_3, RAMP_CALIBRATION_4,
			RAMP_CALIBRATION_5,
			DATA_GAPS, DACS_NO_FRAME_SYNC_ERROR_COUNT, DACS_DETECTED_TIP_PARITY_ERRORS, DACS_AUXILLARY_ERRORS, CALIBRATION_PARAM_ID,
			DACS_PSEUDO_NOISE, DACS_DATASOURCE, DACS_TAPE_DIRECTION, DACS_DATA_MODE, ATTITUDE_CORRECTION, NADIR_TOLERANCE,
			DATA_START_YEAR, DATASET_NAME, EPOCH_YEAR, EPOCH_DAY, EPOCH_TIME;
    
	protected VariableInfo LATITUDE, LONGITUDE, ANCHOR_LATITUDE, ANCHOR_LONGITUDE;
	
	//Keplerian Orbital Elements
	protected VariableInfo SEMI_MAJOR_AXIS, ECCENTRICITY, INCLINATION, PERIGEE, RIGHT_ASCENTION, MEAN_ANOMALY;
	
	//Cartesian Inertial True of Date Elements
	protected VariableInfo XPOSITION_VECTOR, YPOSITION_VECTOR, ZPOSITION_VECTOR, XDOT_VECTOR, YDOT_VECTOR, ZDOT_VECTOR;
	
///////////////////  End of Header Record Variables  ////////////////////
    
	//scan line info RAW
	protected VariableInfo SCAN_LINE_NUMBER, SCAN_LINE_YEAR, SCAN_LINE_DOY, SCAN_LINE_UTC;
	
	//Quality Indicators
	protected VariableInfo FATAL_FLAG, TIME_ERROR, DATA_GAP, DATA_JITTER, CALIBRATION, NO_EARTH_LOC, ASCEND_DESCEND, PN_STATUS, BIT_SYNC_STATUS,
	SYNC_ERROR, FRAME_SYNC_LOCK, FLYWHEELING, BIT_SLIPPAGE, CHANNEL_3_CORRECTION, CHANNEL_4_CORRECTION, CHANNEL_5_CORRECTION, 
	TIP_PARITY, SYNC_ERROR_COUNT;

	//calibration coeffcients
	protected VariableInfo SLOPE_COEFFICIENT, INTERCEPT_COEFFICIENT;
	
	//Solar Zenith Angles
	protected VariableInfo NUM_ZENITH_ANGLES, ZENITH_ANGLES;
	
	//AVHRR Sensor Data
	protected VariableInfo  RAW_SENSOR_DATA, RAW_SENSOR_DATA_CHAN1, RAW_SENSOR_DATA_CHAN2, RAW_SENSOR_DATA_CHAN3, RAW_SENSOR_DATA_CHAN4, RAW_SENSOR_DATA_CHAN5;
	
	//calibrated radiances
	protected VariableInfo RADIANCE_CHAN1, RADIANCE_CHAN2, RADIANCE_CHAN3, RADIANCE_CHAN4, RADIANCE_CHAN5;
	
	//Calibrated Sensor Data
	protected VariableInfo  SENSOR_DATA, SENSOR_DATA_CHAN1, SENSOR_DATA_CHAN2, SENSOR_DATA_CHAN3, SENSOR_DATA_CHAN4, SENSOR_DATA_CHAN5;

	protected VariableInfo CLOCK_DRIFT_DELTA;
	
	public void setAttributesAndVariables(ucar.unidata.io.RandomAccessFile raf, NetcdfFile ncfile) throws IOException {
		varInfoManager.clearAll();
		raFile = raf;
		
		if(!isInit){
			init(raf);
		}

		// Global dimensions
		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
		ncfile.addAttribute(null, new Attribute("IOSP_Author", "Arthur Fotos"));

		// Define dimensions
		setUpDimensions();

		// add dimension
		ncfile.addDimension(null, time);
		ncfile.addDimension(null, record);
		ncfile.addDimension(null, channels);
		ncfile.addDimension(null, pixels);
		ncfile.addDimension(null, anchorPoints);
        ncfile.addDimension(null,scans);
        ncfile.addDimension(null,charArray);
		setVariables(ncfile);

		// Lastly, finish the file
		ncfile.finish();		
	}
	
	
    private void setVariables(ucar.nc2.NetcdfFile ncfile) {
    	//Define dimensions - coordinate variables
    	TIME = new VariableInfo("time","Time","hours since ?",DataType.INT,dimTime);
    	HEADER = new VariableInfo("record","Header Record"," ",DataType.SHORT,dimRecord);
    	CHANNEL = new VariableInfo("channels","channel"," ",DataType.SHORT,dimChans);
    	ANCHOR_POINTS = new VariableInfo("anchorPoints","anchor point",null,DataType.SHORT,dimAnchorPoints);
    	PIXELS = new VariableInfo("pixels","pixels",null,DataType.SHORT,dimPixels);
    	SCAN = new VariableInfo("scans", "Scan Line", null,DataType.INT,dimScans);
    	
    	///////////////////  Header Record Variables  ////////////////////////////    
        // General Information all versions
    	SPACECRAFT_ID = new VariableInfo("spacecraftID", "NOAA Spacecraft ID Code", null,DataType.SHORT,dimRecord);
    	DATA_TYPE = new VariableInfo("dataTypeID", "Data Type Code", null,DataType.SHORT,dimRecord);
       	START_YEAR = new VariableInfo("startYear","Dataset start year",null,DataType.SHORT,dimRecord);
       	START_DAY = new VariableInfo("startDay","Dataset start day of year","days",DataType.SHORT,dimRecord);
      	START_UTC = new VariableInfo("startUTC","Dataset start UTC time","ms since start of day",DataType.INT,dimRecord);       	//long
       	NUM_OF_SCANS = new VariableInfo("dataRecordCounts", "Count of Data Records in this Data Set", null,DataType.INT,dimRecord);
       	END_YEAR = new VariableInfo("endYear","Dataset end year",null,DataType.SHORT,dimRecord);
       	END_DAY = new VariableInfo("endDay","Dataset end day of year","days",DataType.SHORT,dimRecord);
       	END_UTC = new VariableInfo("endUTC","Dataset end UTC time","ms since start of day",DataType.INT,dimRecord);		//long
    	PROCESSING_BLOCK_ID = new VariableInfo("processingBlockId", "Processing Block Id",null,DataType.CHAR,dimCharArray);   		
    	RAMP_CALIBRATION_1 = new VariableInfo("rampCalibration1", "Auto/Ramp Calibration Channel 1","",DataType.BYTE,dimRecord);	
    	RAMP_CALIBRATION_2 = new VariableInfo("rampCalibration2", "Auto/Ramp Calibration Channel 2","",DataType.BYTE,dimRecord);	
    	RAMP_CALIBRATION_3 = new VariableInfo("rampCalibration3", "Auto/Ramp Calibration Channel 3","",DataType.BYTE,dimRecord);	
    	RAMP_CALIBRATION_4 = new VariableInfo("rampCalibration4", "Auto/Ramp Calibration Channel 4","",DataType.BYTE,dimRecord);	
    	RAMP_CALIBRATION_5 = new VariableInfo("rampCalibration5", "Auto/Ramp Calibration Channel 5","",DataType.BYTE,dimRecord);	

    	DATA_GAPS = new VariableInfo("dataGaps", "Number of data gaps", null,DataType.INT,dimRecord);
    	
    	DACS_NO_FRAME_SYNC_ERROR_COUNT = new VariableInfo("dacsNoFrameSyncErrorCount", "Count of input data frames with no frame sync errors",null,DataType.INT,dimRecord);
    	DACS_DETECTED_TIP_PARITY_ERRORS = new VariableInfo("dacsTipParityErrors","count of DACS detected tip parity errors",null,DataType.INT,dimRecord);
    	DACS_AUXILLARY_ERRORS = new VariableInfo("dacsAuxillaryErrorCount", "Sum of all auxilairy sync errors",null,DataType.INT,dimRecord);
    	CALIBRATION_PARAM_ID = new VariableInfo("calibrationParameterId", "Calibration Parameter ID",null,DataType.STRING,dimRecord);
    	DACS_PSEUDO_NOISE  = new VariableInfo("pseudoNoiseFlag","DACS Psuedo Noise Flag 0=normal data, 1=P/N data",null,DataType.BYTE,dimRecord);
    	DACS_DATASOURCE = new VariableInfo("dacsDatasource","DACS Data Source, 0=Unused,1=Fairbanks,2=Wallops,3=SOCC",null,DataType.BYTE,dimRecord);
    	DACS_TAPE_DIRECTION = new VariableInfo("dacsTapeDirection","DACS Tape Direction 0=REV, 1=FWD",null,DataType.BYTE,dimRecord);
    	DACS_DATA_MODE = new VariableInfo("dacsDataMode","DACS Data Mode, 0=Test Data, 1=Flight Data",null,DataType.BYTE,dimRecord);
    		
    	if(dsVersion == 3){	
    		ATTITUDE_CORRECTION = new VariableInfo("attitudeCorrection","attitude correction indicator: 0= no correction;1=correction applied",null,DataType.BYTE,dimRecord);
    		NADIR_TOLERANCE = new VariableInfo("nadirTolerance","Nadir earth location tolerance","km",DataType.SHORT,dimRecord,1E-1f,null,null,null);
    		DATA_START_YEAR = new VariableInfo("dataStartYear","4-digit year for start of date(effective Dec. 2 1998",null,DataType.SHORT,dimRecord);
    	}
    	DATASET_NAME = new VariableInfo("dataSetName", "Data Set Name",null,DataType.CHAR,dimCharArray);
    	if(dsVersion == 2 || dsVersion == 3){
    		EPOCH_YEAR = new VariableInfo("epochYear", "2-digit year of Epoch for orbit Vector(4 digits after March 17, 1999)",null,DataType.SHORT,dimRecord);
    		EPOCH_DAY = new VariableInfo("epochDay", "Day of Year of Epoch",null,DataType.SHORT,dimRecord);
    		EPOCH_TIME = new VariableInfo("utcTOD","Millisecond UTC epoch time of day","ms",DataType.INT,dimRecord);
    	}
//name, long_name, units, dataType, dimList, scale, offset, max, recordnumber
    	//version 2 uses doubles for these variable.  Version 3 uses long with offset
    	if(dsVersion == 2){
    		//Keplerian Orbital Elements
    		SEMI_MAJOR_AXIS = new VariableInfo("semiMajorAxis","Semi-major axis in kilometers","km", DataType.FLOAT,dimRecord);
    		ECCENTRICITY =	new VariableInfo("eccentricity", "Eccentricity",null,DataType.FLOAT, dimRecord);
    		INCLINATION = new VariableInfo("inclination","Inclination","degrees",DataType.FLOAT, dimRecord);
    		PERIGEE = new VariableInfo("perigee","Argument of Perigee","degrees",DataType.FLOAT, dimRecord);
    		RIGHT_ASCENTION = new VariableInfo("rightAscention","Right Ascension of ascending node","degrees",DataType.FLOAT,dimRecord);
    		MEAN_ANOMALY = new VariableInfo("meanAnomaly","Mean anomaly","degrees",DataType.FLOAT,dimRecord);
   	
    		//Cartesian Inertial True of Date Elements
    		XPOSITION_VECTOR = new VariableInfo("xVector","x component of position vector","km",DataType.FLOAT,dimRecord);
    		YPOSITION_VECTOR = new VariableInfo("yVector","y component of position vector","km",DataType.FLOAT,dimRecord);
    		ZPOSITION_VECTOR = new VariableInfo("zVector","z component of position vector","km",DataType.FLOAT,dimRecord);
    		XDOT_VECTOR = new VariableInfo("xdotVector", "x-do component of position vector","km/sec",DataType.FLOAT,dimRecord); 
    		YDOT_VECTOR = new VariableInfo("ydotVector", "y-do component of position vector","km/sec",DataType.FLOAT,dimRecord); 
    		ZDOT_VECTOR = new VariableInfo("zdotVector", "z-do component of position vector","km/sec",DataType.FLOAT,dimRecord);    
    	}
    	
    	if(dsVersion == 3){
    		//Keplerian Orbital Elements
    		SEMI_MAJOR_AXIS = new VariableInfo("semiMajorAxis","Semi-major axis in kilometers","km", DataType.INT,dimRecord,1E-3f,null,null,null);
    		ECCENTRICITY =	new VariableInfo("eccentricity", "Eccentricity",null,DataType.INT, dimRecord,1E-8f,null,null,null);
    		INCLINATION = new VariableInfo("inclination","Inclination","degrees",DataType.INT, dimRecord, 1E-5f,null,null,null);
    		PERIGEE = new VariableInfo("perigee","Argument of Perigee","degrees",DataType.INT, dimRecord, 1E-5f,null,null,null);
    		RIGHT_ASCENTION = new VariableInfo("rightAscention","Right Ascension of ascending node","degrees",DataType.INT,dimRecord,1E-5f,null,null,null);
    		MEAN_ANOMALY = new VariableInfo("meanAnomaly","Mean anomaly","degrees",DataType.INT,dimRecord,1E-5f,null,null,null);
   	
    		//Cartesian Inertial True of Date Elements
    		XPOSITION_VECTOR = new VariableInfo("xVector","x component of position vector","km",DataType.INT,dimRecord,1E-4f,null,null,null);  //long
    		YPOSITION_VECTOR = new VariableInfo("yVector","y component of position vector","km",DataType.INT,dimRecord,1E-4f,null,null,null); //long
    		ZPOSITION_VECTOR = new VariableInfo("zVector","z component of position vector","km",DataType.INT,dimRecord,1E-4f,null,null,null);	//long
    		XDOT_VECTOR = new VariableInfo("xdotVector", "x-do component of position vector","km/sec",DataType.INT,dimRecord,1E-6f,null,null,null); //long
    		YDOT_VECTOR = new VariableInfo("ydotVector", "y-do component of position vector","km/sec",DataType.INT,dimRecord,1E-6f,null,null,null); //long
    		ZDOT_VECTOR = new VariableInfo("zdotVector", "z-do component of position vector","km/sec",DataType.INT,dimRecord,1E-6f,null,null,null);    //long
    	}    	
   
    	/////////  End Of Header Records   //////////////////    	
    	
    	//    	scan line info
    	SCAN_LINE_NUMBER = new VariableInfo("scanLineNumber", "Scan Line Number", null,DataType.INT,dimScans);
    	SCAN_LINE_YEAR = new VariableInfo("scanLineYear", "Scan Line Year","year", DataType.INT,dimScans);
    	SCAN_LINE_DOY = new VariableInfo("scanLineDay", "Scan Line Day of Year","day",DataType.INT,dimScans);
    	SCAN_LINE_UTC = new VariableInfo("scanLineUTC", "Scan Line UTC Time of day", "ms since start of day" ,DataType.INT,dimScans);
         		
    	//quality indicators
    	FATAL_FLAG = new VariableInfo("fatalFlag","Fatal flag - data should not be used product generation",null,DataType.BYTE,dimScans);
    	TIME_ERROR = new VariableInfo("qualityTimeError","time sequence error was detected while processing frame",null,DataType.BYTE,dimScans);
    	DATA_GAP = new VariableInfo("qualityDataGap","Data Gap - a gap precedes this frame",null,DataType.BYTE,dimScans);
    	DATA_JITTER = new VariableInfo("qualityDataJitter","Data Jitter - Resync occurred on this frame",null,DataType.BYTE,dimScans);
    	CALIBRATION = new VariableInfo("qualityCalibration","Calibration - insufficient data for calibration",null,DataType.BYTE,dimScans);
    	NO_EARTH_LOC = new VariableInfo("qualityEarthLoc","No Earth Location -Earth location data no available",null,DataType.BYTE,dimScans);
    	ASCEND_DESCEND = new VariableInfo("qualityAscendDescend","Ascend/Descend Avhrr Earth location indication of ascending(=0) or descending (=1) data",null,DataType.BYTE,dimScans);
    	PN_STATUS = new VariableInfo("qualityPnStatus","P/N Status - psuedo noise(p/n) occured(=1) on frame, data not used for calibration computations",null,DataType.BYTE,dimScans);
    	BIT_SYNC_STATUS = new VariableInfo("qualityBitSyncStatus","Bit Sync Status - Drop lock during frame",null,DataType.BYTE,dimScans);
    	SYNC_ERROR = new VariableInfo("qualitySyncError","Sync Error - Frame Sync word error greater than zero",null,DataType.BYTE,dimScans);
    	FRAME_SYNC_LOCK = new VariableInfo("qualityFrameSyncLock","Frame Sync Lock - Frame sync previously dropped lock",null,DataType.BYTE,dimScans);
    	FLYWHEELING = new VariableInfo("qualityFlywheeling","Flywheeling detected during this frame",null, DataType.BYTE,dimScans);
    	BIT_SLIPPAGE = new VariableInfo("qualityBitSlippage","Bit slippage detected during this frame",null,DataType.BYTE,dimScans);
    	if(dsVersion ==3){
    		CHANNEL_3_CORRECTION = new VariableInfo("qualityChan3Correction","Channel 3 SBBC Indicator - 0= no correction, 1=corrected",null,DataType.BYTE,dimScans);
    		CHANNEL_4_CORRECTION = new VariableInfo("qualityChan4Correction","Channel 4 SBBC Indicator - 0= no correction, 1=corrected",null,DataType.BYTE,dimScans);
    		CHANNEL_5_CORRECTION = new VariableInfo("qualityChan5Correction","Channel 5 SBBC Indicator - 0= no correction, 1=corrected",null,DataType.BYTE,dimScans);
    	}

    	TIP_PARITY = new VariableInfo("qualityTipParity", "Tip Parity",null,DataType.BYTE, dimChanScans);
    	SYNC_ERROR_COUNT = new VariableInfo("qualitySyncErrorCount","Sync Errors - Number of bit errors in frame sync",null,DataType.BYTE,dimScans);
    	
    	//calibration coefficients
     	SLOPE_COEFFICIENT = new VariableInfo("slopeCoefficient","Slope Coefficients",null,DataType.INT,dimChanScans);					//long
    	INTERCEPT_COEFFICIENT = new VariableInfo("interceptCoefficient","Intercept Coefficients",null,DataType.INT,dimChanScans);					//long  
    	
       //Zenith Angles
       NUM_ZENITH_ANGLES = new VariableInfo("numZenithAngles","Number of meaningful Zenith angles/Earth location points appended to scan",null,DataType.SHORT,dimScans);
       ZENITH_ANGLES = new VariableInfo("zenithAngles","Solar Zenith Angles","degrees",DataType.FLOAT,dimAnchorPointsScans);      
       
       //earth location
       ANCHOR_LATITUDE = new VariableInfo("anchorLat","Latitude Anchor Point", "degrees north", DataType.FLOAT,dimAnchorPointsScans);
       ANCHOR_LONGITUDE = new VariableInfo("anchorLon", "Longitude Anchor Point" , "degrees east", DataType.FLOAT,dimAnchorPointsScans);       
       LATITUDE = new VariableInfo("lat","Latitude","degrees_north",DataType.FLOAT,dimPixelsScan);
       LONGITUDE = new VariableInfo("lon","Longitude","degrees_east",DataType.FLOAT,dimPixelsScan);    
       
       //Telemetry (HRPT minor frame format) - in the future maybe 
       

       //AVHRR Sensor Data
//       RAW_SENSOR_DATA = new VariableInfo("rawSensorData", "Raw AVHRR Sensor Data", null, DataType.INT,dimChanObservationsScan);									//long
       RAW_SENSOR_DATA_CHAN1 = new VariableInfo("rawSensorDataCh1", "Channel 1 Raw Sensor Data", null, DataType.INT,dimPixelsScan);					//long
       RAW_SENSOR_DATA_CHAN2 = new VariableInfo("rawSensorDataCh2", "Channel 2 Raw Sensor Data", null, DataType.INT,dimPixelsScan);					//long
       RAW_SENSOR_DATA_CHAN3 = new VariableInfo("rawSensorDataCh3", "Channel 3 Raw Sensor Data", null, DataType.INT,dimPixelsScan);					//long
       RAW_SENSOR_DATA_CHAN4 = new VariableInfo("rawSensorDataCh4", "Channel 4 Raw Sensor Data", null, DataType.INT,dimPixelsScan);					//long
       RAW_SENSOR_DATA_CHAN5 = new VariableInfo("rawSensorDataCh5", "Channel 5 Raw Sensor Data", null, DataType.INT,dimPixelsScan);					//long
       
       RADIANCE_CHAN1 = new VariableInfo("radianceChan1","Channel 1 Radiance ","W/(m^2 um steradian)", DataType.FLOAT,dimPixelsScan);
       RADIANCE_CHAN1.addAttribute(new Attribute("Warning","Values for radiance have been calculated using pre-launch values for slope and intercept."));
       RADIANCE_CHAN2 = new VariableInfo("radianceChan2","Channel 2 Radiance ","W/(m^2 um steradian)", DataType.FLOAT,dimPixelsScan);
       RADIANCE_CHAN2.addAttribute(new Attribute("Warning","Values for radiance have been calculated using pre-launch values for slope and intercept."));
       RADIANCE_CHAN3 = new VariableInfo("radianceChan3","Channel 3 Radiance ","mW/(m^2 cm^-1 steradian)", DataType.FLOAT,dimPixelsScan);
       RADIANCE_CHAN3.addAttribute(new Attribute("Warning","Values for radiance have been calculated using pre-launch values for slope and intercept."));
       RADIANCE_CHAN4 = new VariableInfo("radianceChan4","Channel 4 Radiance ","mW/(m^2 cm^-1 steradian)", DataType.FLOAT,dimPixelsScan);
       RADIANCE_CHAN4.addAttribute(new Attribute("Warning","Values for radiance have been calculated using pre-launch values for slope and intercept."));
       RADIANCE_CHAN5 = new VariableInfo("radianceChan5","Channel 5 Radiance ","mW/(m^2 cm^-1 steradian)", DataType.FLOAT,dimPixelsScan);
       RADIANCE_CHAN5.addAttribute(new Attribute("Warning","Values for radiance have been calculated using pre-launch values for slope and intercept."));
      
       //Calibrated Sensor Data
       SENSOR_DATA_CHAN1 = new VariableInfo("albedoCh1", "Channel 1 Percent Albedo", "", DataType.FLOAT,dimPixelsScan);	//long
       SENSOR_DATA_CHAN1.addAttribute(new Attribute("Warning","Values for albedo have been calculated using pre-launch values for slope and intercept."));
       SENSOR_DATA_CHAN1.addAttribute(new Attribute("coordinates", "lat lon"));
       SENSOR_DATA_CHAN2 = new VariableInfo("albedoCh2", "Channel 2 Percent Albedo","", DataType.FLOAT,dimPixelsScan);	//long
       SENSOR_DATA_CHAN2.addAttribute(new Attribute("Warning","Values for albedo have been calculated using pre-launch values for slope and intercept."));
       SENSOR_DATA_CHAN2.addAttribute(new Attribute("coordinates", "lat lon"));
       SENSOR_DATA_CHAN3 = new VariableInfo("brightnessTempCh3", "Channel 3 Brightness Temperature", "K" , DataType.FLOAT,dimPixelsScan);
       SENSOR_DATA_CHAN3.addAttribute(new Attribute("coordinates", "lat lon"));
       SENSOR_DATA_CHAN4 = new VariableInfo("brightnessTempCh4", "Channel 4 Brightness Temperature", "K", DataType.FLOAT,dimPixelsScan);
       SENSOR_DATA_CHAN4.addAttribute(new Attribute("coordinates", "lat lon"));
       SENSOR_DATA_CHAN5 = new VariableInfo("brightnessTempCh5", "Channel 5 Brightness Temperature", "K", DataType.FLOAT,dimPixelsScan);	       
       SENSOR_DATA_CHAN5.addAttribute(new Attribute("coordinates", "lat lon"));
        
       if(dsVersion == 3){
    	   CLOCK_DRIFT_DELTA = new VariableInfo("clockDriftDelta","Clock Drift Delta","ms",DataType.INT,dimScans);	//int
       }
       
    	//Set variables
    	varInfoManager.setVariables(ncfile);

    }	
	
	
	public ucar.ma2.Array getVariableValues(ucar.nc2.Variable v2, Section section) throws IOException, InvalidRangeException {
		ucar.ma2.Array dataArray = null;
		dataArray = Array.factory(v2.getDataType(), v2.getShape());
		Index dataIndex = dataArray.getIndex();
		String varName = v2.getName();	
		int[] xy = dataArray.getShape();
		readData();

		//dimensions
		if(varName.equals(TIME.getName())){
		}
			/**	
  
 		
		}else if(varName.equals(HEADER.getName())){
			dataArray.setInt(dataIndex.set(0),0);
		}else if(varName.equals(CHANNEL.getName())){
			for(int i =0;i<5;i++){
				dataArray.setInt(dataIndex.set(i),i);
			}
		}else if(varName.equals(ANCHOR_POINTS.getName())){
			for(int i=0;i<xy[0];i++){
				dataArray.setInt(dataIndex.set(i),i);
			}
		}else if(varName.equals(PIXELS.getName())){
			for(int i=0;i<xy[0];i++){
				dataArray.setInt(dataIndex.set(i),i);
			}			
		}else if(varName.equals(SCAN.getName())){
			for(int i=0;i<xy[0];i++){
				dataArray.setInt(dataIndex.set(i),i);
			}			
		}
	*/	
		//Header Values
		else if(varName.equals(DATASET_NAME.getName())){
			String s = headerRec.getDatasetName();
			for(int i=0;i<s.length();i++){
				dataArray.setObject(dataIndex.set(i),s.charAt(i));	
			}
		}else if(varName.equals(PROCESSING_BLOCK_ID.getName())){
			String s = headerRec.getProcessingId();
			for(int i=0;i<s.length();i++){
				dataArray.setObject(dataIndex.set(i),s.charAt(i));	
			}
		}else if(varName.equals(SPACECRAFT_ID.getName())){
			dataArray.setInt(dataIndex.set(0), headerRec.getSpacecraftId());
		}else if(varName.equals(DATA_TYPE.getName())){
			dataArray.setInt(dataIndex.set(0), headerRec.getDataType());
		}
		else if(varName.equals(START_YEAR.getName())){
			dataArray.setInt(dataIndex.set(0), headerRec.getStartYear());
		}
		else if(varName.equals(START_DAY.getName())){
			dataArray.setInt(dataIndex.set(0),headerRec.getStartDay());
		}
		else if(varName.equals(START_UTC.getName())){
			dataArray.setLong(dataIndex.set(0),headerRec.getStartTimeUTC());
		}
		else if(varName.equals(NUM_OF_SCANS.getName())){
			dataArray.setInt(dataIndex.set(0),headerRec.getNumberOfScans());
	    }
		else if(varName.equals(END_YEAR.getName())){
			dataArray.setInt(dataIndex.set(0), headerRec.getEndYear());
		}
		else if(varName.equals(END_DAY.getName())){
			dataArray.setInt(dataIndex.set(0),headerRec.getEndDay());
		}
		else if(varName.equals(END_UTC.getName())){
			dataArray.setLong(dataIndex.set(0),headerRec.getEndTimeUTC());
		}		

		else if(varName.equals(RAMP_CALIBRATION_1.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getRampCalibration()[0]);
	    }else if(varName.equals(RAMP_CALIBRATION_2.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getRampCalibration()[1]);
	    }else if(varName.equals(RAMP_CALIBRATION_3.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getRampCalibration()[2]);
	    }else if(varName.equals(RAMP_CALIBRATION_4.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getRampCalibration()[3]);
	    }else if(varName.equals(RAMP_CALIBRATION_5.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getRampCalibration()[4]);
	    }

		else if(varName.equals(DACS_NO_FRAME_SYNC_ERROR_COUNT.getName())){
	    	dataArray.setInt(dataIndex.set(0),headerRec.getDacsNoFrameSyncErrorCount());
	    }else if(varName.equals(DACS_DETECTED_TIP_PARITY_ERRORS.getName())){
	    	dataArray.setInt(dataIndex.set(0),headerRec.getDacsTipParityErrors());
	    }else if(varName.equals(DACS_AUXILLARY_ERRORS .getName())){
	    	dataArray.setInt(dataIndex.set(0),headerRec.getDacsAuxillaryErrors());
	    }else if(varName.equals(CALIBRATION_PARAM_ID.getName())){
	    	dataArray.setObject(dataIndex.set(0),headerRec.getCalParamId());
	    }else if(varName.equals(DACS_PSEUDO_NOISE.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getDacsPsuedoNoise());
	    }else if(varName.equals(DACS_DATASOURCE.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getDacsDataSource());
	    }else if(varName.equals(DACS_TAPE_DIRECTION.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getDacsTapeDir());
	    }else if(varName.equals(DACS_DATA_MODE.getName())){
	    	dataArray.setByte(dataIndex.set(0),headerRec.getDacsDataMode());
	    }

//////////////////  Data Record Values  ///////////////////////////////////

		
	    else if (varName.equals(SCAN_LINE_NUMBER.getName())){																		
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i),dr.getScanLine());
			}
		}
		else if (varName.equals(SCAN_LINE_YEAR.getName())){																		
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i),dr.getYear());
			}
		}else if (varName.equals(SCAN_LINE_DOY.getName())){																		
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i),dr.getDay());
			}
		}else if (varName.equals(SCAN_LINE_UTC.getName())){																		
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setLong(dataIndex.set(i),dr.getUtcTime());
			}
		}
//		quality indicators
		else if(varName.equals(FATAL_FLAG.getName())){
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getFatalFlag());
			}			
		}
		else if(varName.equals(TIME_ERROR.getName())){
			for (int i = 0; i < xy[0]; i++) {
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getTimeError());
			}			
		}		
		else if(varName.equals(DATA_GAP.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getDataGap());
			}
		}
		else if(varName.equals(DATA_JITTER.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getDataJitter());
			}
		}		
		else if(varName.equals(CALIBRATION.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getCalibration());
			}
		}
		else if(varName.equals(NO_EARTH_LOC.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getNoEarthLocation());
			}
		}
		else if(varName.equals(DATA_GAP.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getDataGap());
			}
		}
		else if(varName.equals(ASCEND_DESCEND.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getAscendDescend());
			}
		}
		else if(varName.equals(PN_STATUS.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getPnStatus());
			}			
		}
		else if(varName.equals(BIT_SYNC_STATUS.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getBitSyncStatus());				
			}			
		}
		else if(varName.equals(SYNC_ERROR.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getSyncError());
			}			
		}		
		else if(varName.equals(FRAME_SYNC_LOCK.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getFrameSyncLock());
			}
		}			
		else if(varName.equals(FLYWHEELING.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getFlyWheeling());
			}			
		}			
		else if(varName.equals(BIT_SLIPPAGE.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getBitSlippage());
			}
		}
	
		else if(varName.equals(TIP_PARITY.getName())){
			for(int i=0;i<xy[1];i++){
				IScanlineVer1 dr = recs.get(i);
				byte[] b = dr.getTipParity();
				for(int j=0;j<xy[0];j++){
					dataArray.setByte(dataIndex.set(j,i),b[j]);
				}
			}
		}

		else if(varName.equals(SYNC_ERROR_COUNT.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setByte(dataIndex.set(i),dr.getSyncErrorCount());
			}
		}

		else if(varName.equals(SLOPE_COEFFICIENT.getName())){
			for(int i=0;i<xy[1];i++){
				IScanlineVer1 dr = recs.get(i);
				int[] vals = dr.getSlopeCoeffs();
				for(int j=0;j<xy[0];j++){
					dataArray.setLong(dataIndex.set(j,i),vals[j]);
				}
			}
		}	
		else if(varName.equals(INTERCEPT_COEFFICIENT.getName())){
			for(int i=0;i<xy[1];i++){
				IScanlineVer1 dr = recs.get(i);
				int[] vals = dr.getInterceptCoeffs();
				for(int j=0;j<xy[0];j++){
					dataArray.setLong(dataIndex.set(j,i),vals[j]);
				}
			}
		}		
		else if(varName.equals( NUM_ZENITH_ANGLES.getName())){
			for(int i=0;i<xy[0];i++){
				IScanlineVer1 dr = recs.get(i);
				dataArray.setShort(dataIndex.set(i), dr.getNumLocationPoints());
			}
		}			
	
		else if(varName.equals(ZENITH_ANGLES.getName())){
			for(int i =0; i<xy[1]; i ++){
				IScanlineVer1 dr = recs.get(i);
				short[] data = dr.getZenithAngles();
				byte[] decimals = dr.getZenithDecimals();
				for(int j =0; j<xy[0];j++){
					float val = (float)data[j]/2;
					float dec = (float)decimals[j]/10;
					dataArray.setFloat(dataIndex.set(j,i), val + dec);
				}
			}
		}
		else if (varName.equals(RAW_SENSOR_DATA_CHAN1.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				short[][] data = dr.getData();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i),data[0][j]);
				}
			}
		}
		else if (varName.equals(RAW_SENSOR_DATA_CHAN2.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				short[][] data = dr.getData();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i),data[1][j]);
				}
			}
		}
		else if (varName.equals(RAW_SENSOR_DATA_CHAN3.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				short[][] data = dr.getData();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i),data[2][j]);
				}
			}
		}	
		else if (varName.equals(RAW_SENSOR_DATA_CHAN4.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				short[][] data = dr.getData();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i),data[3][j]);
				}
			}
		}	
		else if (varName.equals(RAW_SENSOR_DATA_CHAN5.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				short[][] data = dr.getData();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i),data[4][j]);
				}
			}
		}		
		else if (varName.equals(RADIANCE_CHAN1.getName())){		
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			logger.fine("shipname-->" + shipName);
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateRadianceForVisible(shipName,1,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}	
		else if (varName.equals(RADIANCE_CHAN2.getName())){		
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			logger.fine("shipname-->" + shipName);
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateRadianceForVisible(shipName,2,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}			
		else if (varName.equals(RADIANCE_CHAN3.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateRadianceForThermal(headerRec.getSpacecraftId(),3,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}	
		else if (varName.equals(RADIANCE_CHAN4.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateRadianceForThermal(headerRec.getSpacecraftId(),4,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}	
		else if (varName.equals(RADIANCE_CHAN5.getName())){																		
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateRadianceForThermal(headerRec.getSpacecraftId(),5,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}			
		//Calibrated Channel Data
		
///////////////////////////////////		

		
		//lat & lon
		else if(varName.equals(LATITUDE.getName())){
			for(int j =0;j < xy[1];j++){
				IScanlineVer1 dr = recs.get(j);
				float[] latData = dr.getAnchorLat();
				float[] lonData = dr.getAnchorLon();
				float[][] vals = LatLonInterpolation.interpolate(latData,lonData);
				for(int i =0;i <xy[0];i++){
					dataArray.setFloat(dataIndex.set((i),j),(float)vals[1][i]);
				}	
			}
				
		}else if(varName.equals(LONGITUDE.getName())){
			for(int j=0;j<xy[1];j++){
				IScanlineVer1 dr = recs.get(j);
				float[] latData = dr.getAnchorLat();
				float[] lonData = dr.getAnchorLon();
				float[][] vals = LatLonInterpolation.interpolate(latData,lonData);
				for(int i =0;i <xy[0];i++){
					dataArray.setFloat(dataIndex.set((i),j),(float)vals[0][i]);
				}	
			}
		}

		else if(varName.equals(ANCHOR_LATITUDE.getName())){
			for(int j=0;j<xy[1];j++){
				IScanlineVer1 dr = recs.get(j);
				float[] data = dr.getAnchorLat();
				for(int i =0;i < xy[0];i++){
					dataArray.setFloat(dataIndex.set(i,j),data[i]);
				}				
			}			
		}else if(varName.equals(ANCHOR_LONGITUDE.getName())){
			for(int j=0;j<xy[1];j++){
				IScanlineVer1 dr = recs.get(j);
				float[] data = dr.getAnchorLon();
				if(j==1000){
					for(int q=0;q<51;q++){
//					System.out.println(data[q]);
					}
				}
				for(int i =0;i < xy[0];i++){
					dataArray.setFloat(dataIndex.set(i,j),data[i]);
				}				
			}			
		}

//		Calibrated Sensor Data
		else if (varName.equals(SENSOR_DATA_CHAN1.getName())){			
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateAlbedo(shipName,1,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}
		else if (varName.equals(SENSOR_DATA_CHAN2.getName())){													
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateAlbedo(shipName,2,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}
		else if (varName.equals(SENSOR_DATA_CHAN3.getName())){		
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateBT(shipName,3,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}	
		else if (varName.equals(SENSOR_DATA_CHAN4.getName())){			
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateBT(shipName,4,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}	
		else if (varName.equals(SENSOR_DATA_CHAN5.getName())){				
			String shipName = AvhrrGacVer1Iosp.getSatelliteName(headerRec.getSpacecraftId(),headerRec.getDataStartYear());														
			for (int i = 0; i < xy[1]; i++) {
				IScanlineVer1 dr = recs.get(i);
				float[] calVals = AvhrrCalibratorVer1.calculateBT(shipName,5,dr,409);
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setFloat(dataIndex.set(j, i),calVals[j]);
				}
			}
		}			
//		these are only available in third version of dataset
		if(dsVersion == 3){
			if(varName.equals(CHANNEL_4_CORRECTION.getName())){
				for(int i=0;i<xy[0];i++){
					IScanlineVer1 dr = recs.get(i);
					dataArray.setByte(dataIndex.set(i),dr.getChan4Correction());
				}
			}
			else if(varName.equals(CHANNEL_5_CORRECTION.getName())){
				for(int i=0;i<xy[0];i++){
					IScanlineVer1 dr = recs.get(i);
					dataArray.setByte(dataIndex.set(i),dr.getChan5Correction());
				}
			}
		}
////////		
		
		if(dsVersion ==2){
			if(varName.equals(SEMI_MAJOR_AXIS.getName())){
				float f = headerRec.getSemimajorAxis();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(ECCENTRICITY.getName())){
				float f = headerRec.getEccentricity();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(INCLINATION.getName())){
				float f = headerRec.getInclination();
				dataArray.setFloat(dataIndex.set(0),f);
			}		
			else if(varName.equals(PERIGEE.getName())){
				float f = headerRec.getPerigee();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(RIGHT_ASCENTION.getName())){
				float f = headerRec.getRightAscension();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(MEAN_ANOMALY.getName())){
				float f = headerRec.getMeanAnomaly();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(XPOSITION_VECTOR.getName())){
				float f = headerRec.getXVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(YPOSITION_VECTOR.getName())){
				float f = headerRec.getYVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(ZPOSITION_VECTOR.getName())){
				float f = headerRec.getZVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}		
			else if(varName.equals(XDOT_VECTOR.getName())){
				float f = headerRec.getXdotVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}		
			else if(varName.equals(YDOT_VECTOR.getName())){
				float f = headerRec.getYdotVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}
			else if(varName.equals(ZDOT_VECTOR.getName())){
				float f = headerRec.getZdotVector();
				dataArray.setFloat(dataIndex.set(0),f);
			}
		}		
		
		if(dsVersion ==3){
			if(varName.equals(SEMI_MAJOR_AXIS.getName())){
				float f = headerRec.getSemimajorAxis();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(ECCENTRICITY.getName())){
				float f = headerRec.getEccentricity();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(INCLINATION.getName())){
				float f = headerRec.getInclination();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}		
			else if(varName.equals(PERIGEE.getName())){
				float f = headerRec.getPerigee();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(RIGHT_ASCENTION.getName())){
				float f = headerRec.getRightAscension();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(MEAN_ANOMALY.getName())){
				float f = headerRec.getMeanAnomaly();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(XPOSITION_VECTOR.getName())){
				float f = headerRec.getXVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(YPOSITION_VECTOR.getName())){
				float f = headerRec.getYVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(ZPOSITION_VECTOR.getName())){
				float f = headerRec.getZVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}		
			else if(varName.equals(XDOT_VECTOR.getName())){
				float f = headerRec.getXdotVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}		
			else if(varName.equals(YDOT_VECTOR.getName())){
				float f = headerRec.getYdotVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
			else if(varName.equals(ZDOT_VECTOR.getName())){
				float f = headerRec.getZdotVector();
				dataArray.setLong(dataIndex.set(0),(long)f);
			}
		}
	    
	    if(dsVersion == 2 || dsVersion == 3){
	    	if(varName.equals(EPOCH_YEAR.getName())){
	    		dataArray.setInt(dataIndex.set(0),headerRec.getEpochYear());
	    	}else if(varName.equals(EPOCH_DAY.getName())){
	    		dataArray.setInt(dataIndex.set(0),headerRec.getEpochDay());	    	
	    	}else if(varName.equals(EPOCH_TIME.getName())){
	    		dataArray.setInt(dataIndex.set(0),(int)headerRec.getUtcTOD());	    	
	    	}
	    }
		
		if(dsVersion == 3){
			if(varName.equals(CHANNEL_3_CORRECTION.getName())){
				for(int i=0;i<xy[0];i++){
					IScanlineVer1 dr = recs.get(i);
					dataArray.setByte(dataIndex.set(i),dr.getChan3Correction());
				}
			if(varName.equals( CLOCK_DRIFT_DELTA .getName())){
				for(int i=0;i<xy[0];i++){
					IScanlineVer1 dr = recs.get(i);
					dataArray.setInt(dataIndex.set(i), dr.getClockDrift());
				}
			}
		}
		}
		else{
			
		}
		return dataArray;
	}
	

	private void setUpDimensions() {
		int n = dimTime.size();
		if (n < 1) {
			dimRecord.add(record);
			dimTime.add(time);

			dimChans.add(channels);
			dimPixels.add(pixels);
			dimAnchorPoints.add(anchorPoints);
			dimScans.add(scans);
		
		    dimChansPixels.add(channels);
		    dimChansPixels.add(pixels);
		   
		    dimPixelsScan.add(pixels);
		    dimPixelsScan.add(scans);
		    
		    dimAnchorPointsScans.add(anchorPoints);
		    dimAnchorPointsScans.add(scans);
		    
		    dimChanScans.add(channels);
		    dimChanScans.add(scans);
		    
//		    dimWord.add(word);
		    
//		    dimWordScans.add(word);
//		    dimWordScans.add(scans);
		    
		    dimChanPixelsScan.add(channels);
		    dimChanPixelsScan.add(pixels);
		    dimChanPixelsScan.add(scans);
		    
//		    dimWordChanScan.add(word);
//		    dimWordChanScan.add(channels);
//		    dimWordChanScan.add(scans);
		    dimCharArray.add(charArray);
		}
	}
    
    protected void readData() throws IOException{
		if(!dataHasBeenRead){
			dsVersion = determineDatasetVersion(raFile.getLocation());
//			System.out.println("############ Dataset Version= " + dsVersion +" ##############");
			if(dsVersion == 1){
				headerRec = new DataSetHeaderVer1();
			}else if(dsVersion == 2){
				headerRec = new DataSetHeaderVer1A();
			}else if(dsVersion == 3){
				headerRec = new DataSetHeaderVer1B();
			}

			if(TBMHeader.hasHeader(raFile)){
				raFile.seek(AvhrrConstants.TBM_HEADER_SIZE);
			}else{
				raFile.seek(0);
			}
			
			headerRec.readHeader(raFile);

			scanLines = headerRec.getNumberOfScans();
			
//			System.out.println("scanLines: " + scanLines);
			
			//read each scan
			if(dsVersion == 1){
				GACScanlineVer1 dr;
				for(int i =0; i<scanLines; i++){
//					System.out.println("Scan line num: " + i);
					dr = new GACScanlineVer1();
					dr.readScanLine(raFile);
					recs.add(dr);
				}
			}else if(dsVersion == 2){
				GACScanlineVer1A dr;
				for(int i =0; i<scanLines; i++){
					dr = new GACScanlineVer1A();
					dr.readScanLine(raFile);
					recs.add(dr);
				}
			}else if(dsVersion == 3){
				GACScanlineVer1B dr;
				for(int i =0; i<scanLines; i++){
					dr = new GACScanlineVer1B();
					try{
						dr.readScanLine(raFile);
					}catch(Exception e){
						break;
					}
					recs.add(dr);
				}
			}			
			dataHasBeenRead = true;
		}
	}
    
    /**
     * calculate Avhrr GAC dataset version
     * @param filename
     * @return
     */
    protected int determineDatasetVersion(String filename){
    	int version = 0;
    	int offset = 0;
        int year;
        int jday;
		try {
            if (TBMHeader.hasHeader(raFile)){  
                offset = AvhrrConstants.TBM_HEADER_SIZE ;
            }
            raFile.seek(offset + 2);
			short temp =raFile.readShort();
			year = AvhrrFile.readSevenBitFlag(temp, 9);
			jday = AvhrrFile.readNineBitFlag(temp, 0);	    	
			
			//version1 up to 1992
			//test to see if dataset date is between 1980 & sept 8, 1992. (before 252th day of 1992)
			if((92 >= year && year >= 78) || (92 == year && 252 > jday)){
				version = 1;
				return version;
			}
			
			//version 1 1992 until Nov 11, 1994
			//check dataset date - date should be between Sept 8, 1992 and Nov 14, 1994
			if(93 == year){
				version = 2;
			}else if((92 == year && jday >= 252) || (94 == year && jday < 391)){
				version = 2;
			}

			if(year < 20){
				year +=2000;
			}
			//version 1 after Nov 14, 1994			
			if(year > 94 || (year == 94 && jday >= 319)){
				version = 3;
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
    	return version;
    }

    public boolean isValidFile(RandomAccessFile raf) throws IOException {
		return false;
	}

	public Array readData(Variable v2, Section section) throws IOException,
			InvalidRangeException {
		return null;
	}

}
