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

import gov.noaa.ncdc.iosp.VariableInfo;
import gov.noaa.ncdc.iosp.VariableInfoManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

/*
 * @author Arthur Fotos
 * @since 2008.04.01
 * @version 2.2
 * 
 * Upgraded to ver2.2 now using netcdf-4.1  wrh 2009.08.01
 * Upgraded to ver3.0 now using non static public hashmap and arrayList in 
 * new VariableInfoManager.java, i.e each iosp has it's own instance.  wrh 2010.11.01
 *
 */

public class AVHRRio {
	
	private VariableInfoManager varInfoManager = new VariableInfoManager();
	
	public AVHRRio() {

	}

	private void init(ucar.unidata.io.RandomAccessFile raf) {
		try {
			readData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		record = new ucar.nc2.Dimension("record", 1, true);
		time = new ucar.nc2.Dimension("time", scanLines, true);
		channels = new ucar.nc2.Dimension("channels", 5, true);
		pixels = new ucar.nc2.Dimension("pixels", 409, true);
		anchorPoints = new ucar.nc2.Dimension("anchorPoints", 51, true);
		scans = new ucar.nc2.Dimension("scans", scanLines, true);
		word = new ucar.nc2.Dimension("word", 10, true);
		charArray = new ucar.nc2.Dimension("charArray", 44, true);
		isInit = true;
	}

	private int gacVersion = 4;

	private boolean isInit = false;

	private int scanLines;

	private int headerCount;

	protected boolean dataHasBeenRead = false;

	private ucar.unidata.io.RandomAccessFile raFile = null;

	private List<GACDataRecord> recs = new ArrayList<GACDataRecord>();

	private GACHeader headerRec;

	// Dimenstions
	// Define dimensions
	protected ucar.nc2.Dimension time, channels, pixels, anchorPoints, scans,
			record, word, charArray;

	// Dimension Lists
	protected List<Dimension> dimRecord = new ArrayList<Dimension>();
	protected List<Dimension> dimTime = new ArrayList<Dimension>();
	protected List<Dimension> dimChans = new ArrayList<Dimension>();
	protected List<Dimension> dimPixels = new ArrayList<Dimension>();
	protected List<Dimension> dimScans = new ArrayList<Dimension>();
	protected List<Dimension> dimWord = new ArrayList<Dimension>();
	protected List<Dimension> dimWordScans = new ArrayList<Dimension>();
	protected List<Dimension> dimChansPoints = new ArrayList<Dimension>();
	protected List<Dimension> dimPixelsScan = new ArrayList<Dimension>();
	protected List<Dimension> dimChanScan = new ArrayList<Dimension>();
	protected List<Dimension> dimChanPixelsScan = new ArrayList<Dimension>();
	protected List<Dimension> dimWordChanScan = new ArrayList<Dimension>();
	protected List<Dimension> dimCharArray = new ArrayList<Dimension>();
	protected List<Dimension> dimAnchorPixel = new ArrayList<Dimension>();

	// ///////////////// Header Record Variables ////////////////////////////
	// General Information
	protected VariableInfo CREATION_SITE_ID, FORMAT_VERSION,
			FORMAT_VERSION_YEAR, FORMAT_VERSION_DOY, LOGICAL_RECORD_LENGTH,
			BLOCK_SIZE, HEADER_RECORD_COUNT, DATA_SET_NAME,
			PROCESSING_BLOCK_ID, CRAFT_ID, INSTRUMENT_ID, DATA_TYPE_ID,
			TIP_SOURCE_CODE, DATA_START_DAY_COUNT, DATA_START_YEAR,
			DATA_START_DAY, DATA_START_TIME_UTC, DATA_END_DAY_COUNT,
			DATA_END_YEAR, DATA_END_DAY, DATA_END_TIME_UTC, CPIDS_YEAR,
			CPIDS_DAY;

	// Data Set Quality Indicators
	protected VariableInfo DATA_RECORDS_COUNT, CALIBRATED_COUNT,
			MISSING_SCANLINE_COUNT, DATA_GAP_COUNT, EARTH_LOCATION_ERROR,
			EARTH_LOCATION_ERROR_CODE_0, EARTH_LOCATION_ERROR_CODE_1,
			EARTH_LOCATION_ERROR_CODE_4, EARTH_LOCATION_ERROR_CODE_5,
			EARTH_LOCATION_ERROR_CODE_6, EARTH_LOCATION_ERROR_CODE_7;

	// Radiance Conversion
	protected VariableInfo SOLAR_IRRADIANCE_CHAN1, SOLAR_IRRADIANCE_CHAN2,
			SOLAR_IRRADIANCE_CHAN3A, EQUIVALENT_WIDTH_CHAN1,
			EQUIVALENT_WIDTH_CHAN2, EQUIVALENT_WIDTH_CHAN3A, CONSTANT1_CHAN3B,
			CONSTANT1_CHAN4, CONSTANT1_CHAN5, CONSTANT2_CHAN3B,
			CONSTANT2_CHAN4, CONSTANT2_CHAN5, CENTRAL_WAVE_CHAN3B,
			CENTRAL_WAVE_CHAN4, CENTRAL_WAVE_CHAN5;

	// Navigation
	protected VariableInfo ELIPSOID_MODEL_ID, NADIR_EARTH_LOC_TOLERANCE,
			EARTH_LOCATION_BIT_2, EARTH_LOCATION_BIT_1, EARTH_LOCATION_BIT_0,
			ROLL_ATTITUDE_ERROR, PITCH_ATTITUDE_ERROR, YAW_ATTITUDE_ERROR,
			VECTOR_EPOCH_YEAR, VECTOR_EPOCH_DAY, VECTOR_EPOCH_UTC,
			SEMI_MAJOR_AXIS, ECCENTRICITY, INCLINATION, ARGUMENT_OF_PERIGEE,
			RIGHT_ASCENSION, MEAN_ANOMALY, POSITION_VECT_X, POSITION_VECT_Y,
			POSITION_VECT_Z, VELOCITY_VECT_XDOT, VELOCITY_VECT_YDOT,
			VELOCITY_VECT_ZDOT, EARTH_SUN_DIST_RATIO;

	// ///////////////// End of Header Record Variables ////////////////////

	// Dimension Variables
	protected VariableInfo TIME, CHANNEL, POINT, SCAN, HEADER, WORD;

	// scan line info RAW
	protected VariableInfo SCAN_LINE_NUMBER, SCAN_LINE_YEAR, SCAN_LINE_DOY,
			SCAN_LINE_UTC, SAT_CLOCK_DRIFT_DELTA;

	// scan line info Calculated
	protected VariableInfo CHANNEL_3A_3B_BIT;

	// Quality Indicators
	protected VariableInfo QUALITY_INDICATOR_BIT_FIELD,
			SCAN_LINE_QUALITY_FLAGS, CALIBRATION_QUALITY_FLAGS,
			FRAME_SYNC_BIT_ERRORS;

	// Calibration Coefficients
	// protected VariableInfo

	// Navigation RAW
	protected VariableInfo NAV_STATUS_BIT_FIELD, EARTH_LOCATION,
			RAW_ROLL_ANGLE, RAW_PITCH_ANGLE, RAW_YAW_ANGLE, RAW_CRAFT_ALTITUDE,
			SOLAR_ZENITH_ANCHOR_ANGLES;

	// HRPT Minor Frame Telemetry
	protected VariableInfo HRPT_FRAME, PRT_READING1, PRT_READING2,
			PRT_READING3, BACK_SCAN_CHAN3B, BACK_SCAN_CHAN4, BACK_SCAN_CHAN5,
			SPACE_DATA;

	// AVHRR Sensor Data
	protected VariableInfo RAW_SENSOR_DATA, RAW_SENSOR_DATA_CHAN1,
			RAW_SENSOR_DATA_CHAN2, RAW_SENSOR_DATA_CHAN3A,
			RAW_SENSOR_DATA_CHAN3B, RAW_SENSOR_DATA_CHAN4,
			RAW_SENSOR_DATA_CHAN5;

	// AVHRR Sensor Data Calibrated
	protected VariableInfo SENSOR_DATA, SENSOR_DATA_CHAN1, SENSOR_DATA_CHAN2,
			SENSOR_DATA_CHAN3A,

			RADIANCE_CHAN1, RADIANCE_CHAN2, RADIANCE_CHAN3A, RADIANCE_CHAN3B,
			RADIANCE_CHAN4, RADIANCE_CHAN5, SENSOR_DATA_CHAN3B_TE,
			SENSOR_DATA_CHAN4_TE, SENSOR_DATA_CHAN5_TE;

	// Digital B Telemetry

	// Analog Housekeeping Data (TIP)

	// Clouds from AVHRR (CLAVR)
	protected VariableInfo CLAVR_STATUS, CLOUD_FLAG;

	protected VariableInfo LATITUDE, LONGITUDE, ANCHOR_LATITUDE,
			ANCHOR_LONGITUDE;

	/**
	 * Test to determine if valid avhrr gac file.
	 * 
	 * @param raf
	 * @return true if valid avhrr gac file, else false
	 */
	public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf) {
		int offset = 0;
		int dataTypeID;
		int startYear;
		int startDayOfYear;
		try {
			if (ArsHeader.hasARSHeader(raf)) {
				offset = AvhrrConstants.ARS_LENGTH;
			}

			raf.seek(offset + 3);
			// System.out.println("4th byte: " + raf.readChar());
			raf.seek((offset + 77) - 1);
			dataTypeID = raf.readUnsignedShort();
			raf.seek((offset + 85) - 1);
			startYear = raf.readUnsignedShort();
			raf.seek((offset + 87) - 1);
			startDayOfYear = raf.readUnsignedShort();
		} catch (Exception e) {
			return false;
		}

		if (dataTypeID != 2) {
			return (false);
		}
		// need to determine start year
		if (startYear < 1998 || startYear > 2020) {
			return (false);
		}
		if (startDayOfYear < 1 || startDayOfYear > 366) {
			return false;
		}

		return true;

	}

	public void setAttributesAndVariables(ucar.unidata.io.RandomAccessFile raf,
		NetcdfFile ncfile) throws IOException, InvalidRangeException {
			varInfoManager.clearAll();
			raFile = raf;

		if (!isInit) {
			init(raf);
		}

		// Global dimensions
		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
		ncfile.addAttribute(null, new Attribute("IOSP_Author", "Arthur Fotos"));
		ncfile.addAttribute(null, new Attribute("Documentation", ""));

		// Define dimensions
		setUpDimensions();

		// add dimension
		ncfile.addDimension(null, time);
		ncfile.addDimension(null, record);
		ncfile.addDimension(null, pixels);
		ncfile.addDimension(null, anchorPoints);
		ncfile.addDimension(null, channels);
		ncfile.addDimension(null, scans);
		ncfile.addDimension(null, word);
		ncfile.addDimension(null, charArray);
		setVariables(ncfile);
		varInfoManager.setVariables(ncfile);

		// Lastly, finish the file
		ncfile.finish();
	}

	private void setVariables(ucar.nc2.NetcdfFile ncfile) {
		// Define dimensions - coordinate variables
		TIME = varInfoManager.addVariableInfo("time", "Time", "hours since ?", DataType.INT, dimTime);
		HEADER = varInfoManager.addVariableInfo("record", "Header Record", " ",	DataType.SHORT, dimRecord);
		CHANNEL = varInfoManager.addVariableInfo("channel", "channel", " ", DataType.SHORT, dimChans);
		POINT = varInfoManager.addVariableInfo("point", "reading point", null, DataType.SHORT, dimPixels);
		SCAN = varInfoManager.addVariableInfo("scan", "Scan Line", null, DataType.INT, dimScans);
		WORD = varInfoManager.addVariableInfo("word", "Word", " ", DataType.SHORT, dimWord);

		// ///////////////// Header Record Variables
		// ////////////////////////////
		// General Information

		CREATION_SITE_ID = varInfoManager.addVariableInfo("dataSetCreationSiteID",	"Data Set Creation Site ID", null, DataType.STRING, dimRecord);
		FORMAT_VERSION = varInfoManager.addVariableInfo("formatVersion",	"NOAA Level 1b Format Version Number", null, DataType.SHORT, dimRecord);
		FORMAT_VERSION_YEAR = varInfoManager.addVariableInfo("formatVersionYear", "NOAA Level 1b Format Version Year", "year", DataType.INT,	dimRecord);
		FORMAT_VERSION_DOY = varInfoManager.addVariableInfo("formatVersionDayOfYear", "NOAA Level 1b Format Version Day Of Year", "day", DataType.INT, dimRecord);
		LOGICAL_RECORD_LENGTH = varInfoManager.addVariableInfo("logicalRecordLength", "Logical Record Length prior to processing", null, DataType.INT, dimRecord);
		BLOCK_SIZE = varInfoManager.addVariableInfo("blockSize", "Block Size of data prior to processing", null, DataType.INT, dimRecord);
		HEADER_RECORD_COUNT = varInfoManager.addVariableInfo("headerRecordCount", "Count of Header Records", null, DataType.INT, dimRecord);
		DATA_SET_NAME = varInfoManager.addVariableInfo("dataSetName", "Data Set Name", null, DataType.CHAR, dimCharArray);
		PROCESSING_BLOCK_ID = varInfoManager.addVariableInfo("processingBlockId", "Processing Block Id", null, DataType.CHAR, dimCharArray);
		CRAFT_ID = varInfoManager.addVariableInfo("spacecraftID", "NOAA Spacecraft ID Code", null, DataType.SHORT, dimRecord);
		INSTRUMENT_ID = varInfoManager.addVariableInfo("instrumentID", "Instrument ID", null, DataType.SHORT, dimRecord);
		DATA_TYPE_ID = varInfoManager.addVariableInfo("dataTypeID", "Data Type Code", null, DataType.SHORT, dimRecord);
		TIP_SOURCE_CODE = varInfoManager.addVariableInfo("tipSourceCode", "TIP Source Code", null, DataType.SHORT, dimRecord);
		DATA_START_DAY_COUNT = varInfoManager.addVariableInfo("startDayCount", "Start of Data Set Day Count", "day", DataType.INT, dimRecord);
		DATA_START_YEAR = varInfoManager.addVariableInfo("startYear", "Start of Data Set Year", "year", DataType.INT, dimRecord);
		DATA_START_DAY = varInfoManager.addVariableInfo("startDayOfYear", "Start of Data Set Day of Year", "day", DataType.INT, dimRecord);
		// DATA_START_TIME_UTC = varInfoManager.addVariableInfo("startTime", "Start of Data Set UTC Time of Day", "milliseconds", DataType.LONG,dimRecord);
		DATA_START_TIME_UTC = varInfoManager.addVariableInfo("startTime", "Start of Data Set UTC Time of Day", "milliseconds", DataType.INT, dimRecord);
		DATA_END_DAY_COUNT = varInfoManager.addVariableInfo("endDayCount", "End of Data Set Day Count", "day", DataType.INT, dimRecord);
		DATA_END_YEAR = varInfoManager.addVariableInfo("endYear", "End of Data Set Year", "year", DataType.INT, dimRecord);
		DATA_END_DAY = varInfoManager.addVariableInfo("endDayOfYear", "End of Data Set Day of Year", "day", DataType.INT, dimRecord);
		// DATA_END_TIME_UTC = varInfoManager.addVariableInfo("endTime","End of Data Set UTC Time of Day", "milliseconds", DataType.LONG,dimRecord);
		DATA_END_TIME_UTC = varInfoManager.addVariableInfo("endTime", "End of Data Set UTC Time of Day", "ms", DataType.INT,	 dimRecord);
		CPIDS_YEAR = varInfoManager.addVariableInfo("cpidsYear", "Year of Last CPIDS Update", "year", DataType.INT, dimRecord);
		CPIDS_DAY = varInfoManager.addVariableInfo("cpidsDayOfYear",	"Day of Year of Last CPIDS Update", "day", DataType.INT, dimRecord);

		// Data Set Quality Indicators
		DATA_RECORDS_COUNT = varInfoManager.addVariableInfo("dataRecordCounts", "Count of Data Records in this Data Set", null, DataType.INT, dimRecord);
		CALIBRATED_COUNT = varInfoManager.addVariableInfo("calibratedCount", "Count of Calibrated, Earth Located Scan Lines", null, DataType.INT, dimRecord);
		MISSING_SCANLINE_COUNT = varInfoManager.addVariableInfo("missingCount", "Count of Missing Scan lines", null, DataType.INT, dimRecord);
		DATA_GAP_COUNT = varInfoManager.addVariableInfo("datagapCount",	"Count of Data Gaps in Data Set", null, DataType.INT, dimRecord);
		EARTH_LOCATION_ERROR = varInfoManager.addVariableInfo("earthLocationError", "Earth Location Error Indicator", null, DataType.INT, dimRecord);
		if (gacVersion != 2) {
			EARTH_LOCATION_ERROR_CODE_0 = varInfoManager.addVariableInfo("earthLocationErrorCode0", "Earth Location in-plane maneuver(MetOp)", null, DataType.BYTE, dimRecord);
			EARTH_LOCATION_ERROR_CODE_1 = varInfoManager.addVariableInfo("earthLocationErrorCode1", "Earth Location out-of-plane maneuver(MetOp)", null,	DataType.BYTE, dimRecord);
		}
		EARTH_LOCATION_ERROR_CODE_4 = varInfoManager.addVariableInfo("earthLocationErrorCode4", "Earth Location Questionable", null, DataType.SHORT, dimRecord);
		EARTH_LOCATION_ERROR_CODE_5 = varInfoManager.addVariableInfo("earthLocationErrorCode5", "Earth Location Questionable - marginal agreement", null, DataType.SHORT, dimRecord);
		EARTH_LOCATION_ERROR_CODE_6 = varInfoManager.addVariableInfo("earthLocationErrorCode6", "Earth Location Questionable - questionable time", null,	DataType.SHORT, dimRecord);
		EARTH_LOCATION_ERROR_CODE_7 = varInfoManager.addVariableInfo("earthLocationErrorCode7", "Earth not located - bad time", null, DataType.SHORT, dimRecord);

		// Radiance Conversion
		SOLAR_IRRADIANCE_CHAN1 = varInfoManager.addVariableInfo("solarIrradiance1", "CH 1 Solar Filtered Irradiance in Wavelength", null, DataType.INT, dimRecord, 1E-1f, null, null, null);
		SOLAR_IRRADIANCE_CHAN2 = varInfoManager.addVariableInfo("solarIrradiance2", "CH 2 Solar Filtered Irradiance in Wavelength", null, DataType.INT, dimRecord, 1E-1f, null, null, null);
		SOLAR_IRRADIANCE_CHAN3A = varInfoManager.addVariableInfo("solarIrradiance3A", "CH 3A Solar Filtered Irradiance in Wavelength", null, DataType.INT, dimRecord, 1E-1f, null, null, null);
		EQUIVALENT_WIDTH_CHAN1 = varInfoManager.addVariableInfo("equivalentWidth1", "CH 1 Equivalent Filter Width in Wavelength", null, DataType.INT, dimRecord, 1E-3f, null, null, null);
		EQUIVALENT_WIDTH_CHAN2 = varInfoManager.addVariableInfo("equivalentWidth2", "CH 2 Equivalent Filter Width in Wavelength", null, DataType.INT, dimRecord, 1E-3f, null, null, null);
		EQUIVALENT_WIDTH_CHAN3A = varInfoManager.addVariableInfo("equivalentWidth3A",	"CH 3A Equivalent Filter Width in Wavelength", null, DataType.INT, dimRecord, 1E-3f, null, null, null);
		CONSTANT1_CHAN3B = varInfoManager.addVariableInfo("ch3bConstant1", "CH 3B Constant 1", null, DataType.INT, dimRecord, 1E-5f, null, null, null);
		CONSTANT1_CHAN4 = varInfoManager.addVariableInfo("ch4Constant1", "CH 4 Constant 1", null, DataType.INT, dimRecord, 1E-5f, null, null, null);
		CONSTANT1_CHAN5 = varInfoManager.addVariableInfo("ch5Constant1", "CH 5 Constant 1", null, DataType.INT, dimRecord, 1E-5f, null, null, null);
		CONSTANT2_CHAN3B = varInfoManager.addVariableInfo("ch3bConstant2", "CH 3B Constant 2", null, DataType.INT, dimRecord, 1E-6f, null, null, null);
		CONSTANT2_CHAN4 = varInfoManager.addVariableInfo("ch4Constant2", "CH 4 Constant 2", null, DataType.INT, dimRecord, 1E-6f, null, null, null);
		CONSTANT2_CHAN5 = varInfoManager.addVariableInfo("ch5Constant2", "CH 5 Constant 2", null, DataType.INT, dimRecord, 1E-6f, null, null, null);
		CENTRAL_WAVE_CHAN3B = varInfoManager.addVariableInfo("centralWavenumber3b", "CH 3B Centeral Wavenumber", null, DataType.INT, dimRecord, 1E-2f, null, null, null);
		CENTRAL_WAVE_CHAN4 = varInfoManager.addVariableInfo("centralWavenumber4", "CH 4 Centeral Wavenumber", null, DataType.INT, dimRecord, 1E-3f, null, null, null);
		CENTRAL_WAVE_CHAN5 = varInfoManager.addVariableInfo("centralWavenumber5", "CH 5 Centeral Wavenumber", null, DataType.INT, dimRecord, 1E-3f, null, null, null);

		// Navigation
		ELIPSOID_MODEL_ID = varInfoManager.addVariableInfo("referenceElipsoid", "Reference Ellipsoid Model ID", null, DataType.STRING, dimRecord);
		NADIR_EARTH_LOC_TOLERANCE = varInfoManager.addVariableInfo("nadirEarthLocationTolerance", "Nadir Earth Location Tolerance", "kilometers", DataType.INT, dimRecord, 1E-1f, null, null, null);
		if (gacVersion != 2) {
			EARTH_LOCATION_BIT_2 = varInfoManager.addVariableInfo( "earthLocationBitField2", "Earth Location Bit Field - dynamic attitude error correction", null, DataType.SHORT, dimRecord);
		}
		EARTH_LOCATION_BIT_1 = varInfoManager.addVariableInfo("earthLocationBitField1", "Earth Location Bit Field - reasonableness test active", null, DataType.SHORT, dimRecord);
		EARTH_LOCATION_BIT_0 = varInfoManager.addVariableInfo("earthLocationBitField0", "Earth Location Bit Field - attitude error correction", null, DataType.SHORT, dimRecord);
		ROLL_ATTITUDE_ERROR = varInfoManager.addVariableInfo("rollAttitudeError", "Constant Roll Attitude Error", "degree", DataType.SHORT, dimRecord, 1E-3f, null, null, null);
		PITCH_ATTITUDE_ERROR = varInfoManager.addVariableInfo("pitchAttitudeError", "Constant Pitch Attitude Error", "degree", DataType.SHORT, dimRecord, 1E-3f, null, null, null);
		YAW_ATTITUDE_ERROR = varInfoManager.addVariableInfo("yawAttitudeError",	"Constant YAW Attitude Error", "degree", DataType.SHORT, dimRecord, 1E-3f, null, null, null);
		VECTOR_EPOCH_YEAR = varInfoManager.addVariableInfo("vectorYear", "Epoch Year for Orbit Vector", "year", DataType.INT, dimRecord);
		VECTOR_EPOCH_DAY = varInfoManager.addVariableInfo("vectorDay", "Day of Epoch Year for Orbit Vector", "day", DataType.INT, dimRecord);
		// VECTOR_EPOCH_UTC = varInfoManager.addVariableInfo("vectorTimeOfDay", "Epoch UTC Time of Day for Orbit Vecotr", "milliseconds", DataType.LONG,dimRecord);
		VECTOR_EPOCH_UTC = varInfoManager.addVariableInfo("vectorTimeOfDay", "Epoch UTC Time of Day for Orbit Vecotr", "milliseconds", DataType.INT, dimRecord);

		SEMI_MAJOR_AXIS = varInfoManager.addVariableInfo("semiMajorAxis", "Semi-major Axis", "kilometers", DataType.INT, dimRecord, 1E-5f, null, null, null);
		ECCENTRICITY = varInfoManager.addVariableInfo("eccentricity", "Eccentricity", null, DataType.INT, dimRecord, 1E-8f, null, null, null);
		INCLINATION = varInfoManager.addVariableInfo("inclination", "Inclination", "degrees", DataType.INT, dimRecord, 1E-5f, null, null, null);
		ARGUMENT_OF_PERIGEE = varInfoManager.addVariableInfo("argumentOfPerigee", "Argument of Perigee", "degrees", DataType.INT, dimRecord,	1E-5f, null, null, null);
		RIGHT_ASCENSION = varInfoManager.addVariableInfo("rightAscension", "Right Ascension", "degrees", DataType.INT, dimRecord, 1E-5f, null, null, null);
		MEAN_ANOMALY = varInfoManager.addVariableInfo("meanAnomaly", "Mean Anomaly", "degrees", DataType.INT, dimRecord, 1E-5f, null, null, null);
		POSITION_VECT_X = varInfoManager.addVariableInfo("positionVectorXComponent", "Position Vector X Component", "kilometers", DataType.INT, dimRecord, 1E-5f, null, null, null);
		POSITION_VECT_Y = varInfoManager.addVariableInfo("positionVectorYComponent", "Position Vector Y Component", "kilometers", DataType.INT,	dimRecord, 1E-5f, null, null, null);
		POSITION_VECT_Z = varInfoManager.addVariableInfo("positionVectorZComponent", "Position Vector Z Component", "kilometers", DataType.INT, dimRecord, 1E-5f, null, null, null);
		VELOCITY_VECT_XDOT = varInfoManager.addVariableInfo("velocityVectorXComponent", "Velocity Vector X-dot Component", "kilometers/second",	DataType.INT, dimRecord, 1E-8f, null, null, null);
		VELOCITY_VECT_YDOT = varInfoManager.addVariableInfo("velocityVectorYComponent", "Velocity Vector Y-dot Component", "kilometers/second", DataType.INT, dimRecord, 1E-8f, null, null, null);
		VELOCITY_VECT_ZDOT = varInfoManager.addVariableInfo("velocityVectorZComponent", "Velocity Vector Z-dot Component", "kilometers/second", DataType.INT, dimRecord, 1E-8f, null, null, null);
		// EARTH_SUN_DIST_RATIO = varInfoManager.addVariableInfo("earthSunDistanceRatio","Earth/Sun Distatnce Ratio" ,null,DataType.LONG,dimRecord,1E-6f,null,null,null);
		EARTH_SUN_DIST_RATIO = varInfoManager.addVariableInfo("earthSunDistanceRatio", "Earth/Sun Distatnce Ratio", null, DataType.INT, dimRecord, 1E-6f, null, null, null);

		// /////// End Of Header Records //////////////////

		// scan line info
		SCAN_LINE_NUMBER = varInfoManager.addVariableInfo("scanLineNumber",	"Scan Line Number", null, DataType.INT, dimScans);
		SCAN_LINE_YEAR = varInfoManager.addVariableInfo("scanLineYear", "Scan Line Year", "year", DataType.INT, dimScans);
		SCAN_LINE_DOY = varInfoManager.addVariableInfo("scanLineDay", "Scan Line Day of Year", "day", DataType.INT, dimScans);
		SCAN_LINE_UTC = varInfoManager.addVariableInfo("scanLineUTC", "Scan Line UTC Time of day", "milliseconds", DataType.INT,	dimScans);
		SAT_CLOCK_DRIFT_DELTA = varInfoManager.addVariableInfo("clockDrift", "Clock Drift", "milliseconds", DataType.INT, dimScans);

		// scan line info Calculated
		CHANNEL_3A_3B_BIT = varInfoManager.addVariableInfo("chan3A3B", "Active Channel 0=3b, 1=3a, 2=transition", null, DataType.SHORT, dimScans);
		// // c) name, long_name, units, dataType, dimList, scale, offset, max, recordnumber
		QUALITY_INDICATOR_BIT_FIELD = varInfoManager.addVariableInfo("qualityBitField",	"Quality Indicator Bit Field", null, DataType.INT, dimScans);
		SCAN_LINE_QUALITY_FLAGS = varInfoManager.addVariableInfo("scanLineQuality", "Scan Line Quality Flags", null, DataType.INT, dimScans);
		CALIBRATION_QUALITY_FLAGS = varInfoManager.addVariableInfo("calibrationBitField", "Calibration Quality Flags", null, DataType.INT, dimScans);
		FRAME_SYNC_BIT_ERRORS = varInfoManager.addVariableInfo("frameSyncErrors", "Count of Frame Sync Errors", null, DataType.INT, dimScans);

		// Navigation
		RAW_ROLL_ANGLE = varInfoManager.addVariableInfo("rollAngle", "Roll Angle", "degrees", DataType.FLOAT, dimScans, -3f, null, null, null);
		RAW_PITCH_ANGLE = varInfoManager.addVariableInfo("pitchAngle", "Pitch Angle", "degrees", DataType.FLOAT, dimScans, -3f, null, null, null);
		RAW_YAW_ANGLE = varInfoManager.addVariableInfo("yawAngle", "Yaw Angle", "degrees", DataType.FLOAT, dimScans, -3f, null, null, null);
		RAW_CRAFT_ALTITUDE = varInfoManager.addVariableInfo("craftAltitude",	"Space Craft Altitude", "kilometers", DataType.FLOAT, dimScans, -1f, null, null, null);

		LATITUDE = varInfoManager.addVariableInfo("lat", "Latitude", "degrees_north", DataType.FLOAT, dimPixelsScan);
		LONGITUDE = varInfoManager.addVariableInfo("lon", "Longitude", "degrees_east", DataType.FLOAT, dimPixelsScan);

		ANCHOR_LATITUDE = varInfoManager.addVariableInfo("anchorLat", "Latitude Anchor Point", "degrees north", DataType.FLOAT, dimAnchorPixel, null, null, null, null);
		ANCHOR_LONGITUDE = varInfoManager.addVariableInfo("anchorLon", "Longitude Anchor Point", "degrees east", DataType.FLOAT, dimAnchorPixel, null, null, null, null);
		SOLAR_ZENITH_ANCHOR_ANGLES = varInfoManager.addVariableInfo("solarZenithAnchorAngle", "Solar Zenith Anchor Angle", "degrees", DataType.FLOAT, dimAnchorPixel, null, null, null, null);

		// HRPT Minor Frame Telemetry
		HRPT_FRAME = varInfoManager.addVariableInfo("hrptFrame", "Frame, 0 = GAC", null, DataType.BYTE, dimScans);
		PRT_READING1 = varInfoManager.addVariableInfo("prtReading1", "PRT Reading 1", null, DataType.INT, dimScans);
		PRT_READING2 = varInfoManager.addVariableInfo("prtReading2", "PRT Reading 2", null,	DataType.INT, dimScans);
		PRT_READING3 = varInfoManager.addVariableInfo("prtReading3", "PRT Reading 3", null,	DataType.INT, dimScans);
		BACK_SCAN_CHAN3B = varInfoManager.addVariableInfo("backScan3b", "Back Scan Reading Channel 3B", null, DataType.INT, dimWordScans);
		BACK_SCAN_CHAN4 = varInfoManager.addVariableInfo("backScan4", "Back Scan Reading Channel 4", null, DataType.INT, dimWordScans);
		BACK_SCAN_CHAN5 = varInfoManager.addVariableInfo("backScan5", "Back Scan Reading Channel 5", null, DataType.INT, dimWordScans);
		SPACE_DATA = varInfoManager.addVariableInfo("spaceData", "Space Data", null, DataType.INT, dimWordChanScan);

		// AVHRR Sensor Data

		// RAW_SENSOR_DATA = varInfoManager.addVariableInfo("rawSensorData", "Raw AVHRR Sensor Data", null, DataType.INT,dimChanPixelsScan);
		RAW_SENSOR_DATA_CHAN1 = varInfoManager.addVariableInfo("rawSensorDataCh1",	"Channel 1 Raw Sensor Data", null, DataType.INT, dimPixelsScan);
		RAW_SENSOR_DATA_CHAN2 = varInfoManager.addVariableInfo("rawSensorDataCh2",	"Channel 2 Raw Sensor Data", null, DataType.INT, dimPixelsScan);
		RAW_SENSOR_DATA_CHAN3A = varInfoManager.addVariableInfo("rawSensorDataCh3A", "Channel 3A Raw Sensor Data", null, DataType.INT, dimPixelsScan);
		RAW_SENSOR_DATA_CHAN3B = varInfoManager.addVariableInfo("rawSensorDataCh3B", "Channel 3B Raw Sensor Data", null, DataType.INT, dimPixelsScan);
		RAW_SENSOR_DATA_CHAN4 = varInfoManager.addVariableInfo("rawSensorDataCh4", "Channel 4 Raw Sensor Data", null, DataType.INT, dimPixelsScan);
		RAW_SENSOR_DATA_CHAN5 = varInfoManager.addVariableInfo("rawSensorDataCh5", "Channel 5Raw Sensor Data", null, DataType.INT, dimPixelsScan);

		RADIANCE_CHAN1 = varInfoManager.addVariableInfo("radianceChan1", "Channel 1 Radiance ", "W/(m^2 um steradian)", DataType.FLOAT, dimPixelsScan);
		RADIANCE_CHAN2 = varInfoManager.addVariableInfo("radianceChan2", "Channel 2 Radiance ", "W/(m^2 um steradian)", DataType.FLOAT, dimPixelsScan);
		RADIANCE_CHAN3A = varInfoManager.addVariableInfo("radianceChan3A", "Channel 3A Radiance ", "mW/(m^2 cm^-1 steradian)", DataType.FLOAT, dimPixelsScan);
		RADIANCE_CHAN3B = varInfoManager.addVariableInfo("radianceChan3B", "Channel 3B Radiance ", "mW/(m^2 cm^-1 steradian)", DataType.FLOAT, dimPixelsScan);
		RADIANCE_CHAN4 = varInfoManager.addVariableInfo("radianceChan4", "Channel 4 Radiance ", "mW/(m^2 cm^-1 steradian)", DataType.FLOAT, dimPixelsScan);
		RADIANCE_CHAN5 = varInfoManager.addVariableInfo("radianceChan5", "Channel 5 Radiance ", "mW/(m^2 cm^-1 steradian)", DataType.FLOAT, dimPixelsScan);

		// AVHRR Sensor Data
		// SENSOR_DATA = varInfoManager.addVariableInfo("sensorData", "AVHRR Sensor Data", null, DataType.FLOAT,dimChanPixelsScan);
		SENSOR_DATA_CHAN1 = varInfoManager.addVariableInfo("albedoCh1", "Channel 1 Albedo",	null, DataType.FLOAT, dimPixelsScan);
		SENSOR_DATA_CHAN2 = varInfoManager.addVariableInfo("albedoCh2", "Channel 2 Albedo",	null, DataType.FLOAT, dimPixelsScan);
		SENSOR_DATA_CHAN3A = varInfoManager.addVariableInfo("albedoCh3A", "Channel 3A Albedo", null, DataType.FLOAT, dimPixelsScan);
		SENSOR_DATA_CHAN3B_TE = varInfoManager.addVariableInfo("brightnessTempCh3B", "Channel 3B Brightness Temperature", "K", DataType.FLOAT,	dimPixelsScan);
		SENSOR_DATA_CHAN4_TE = varInfoManager.addVariableInfo("brightnessTempCh4", "Channel 4 Brightness Temperature", "K", DataType.FLOAT, dimPixelsScan);
		SENSOR_DATA_CHAN5_TE = varInfoManager.addVariableInfo("brightnessTempCh5", "Channel 5 Brightness Temperature", "K", DataType.FLOAT, dimPixelsScan);
		
        SENSOR_DATA_CHAN1.addAttribute(new Attribute("coordinates", "lat lon"));
        SENSOR_DATA_CHAN2.addAttribute(new Attribute("coordinates", "lat lon"));
        SENSOR_DATA_CHAN3A.addAttribute(new Attribute("coordinates", "lat lon"));
        SENSOR_DATA_CHAN3B_TE.addAttribute(new Attribute("coordinates", "lat lon"));
        SENSOR_DATA_CHAN4_TE.addAttribute(new Attribute("coordinates", "lat lon"));
        SENSOR_DATA_CHAN5_TE.addAttribute(new Attribute("coordinates", "lat lon"));


		// CLAVR
		CLAVR_STATUS = varInfoManager.addVariableInfo("clavrStatus", "CLAVR Status, 0=disable", null, DataType.BYTE, dimScans);
		CLOUD_FLAG = varInfoManager.addVariableInfo("cloudFlag", "Cloud Flag", null, DataType.BYTE, dimPixelsScan);

	}

	public ucar.ma2.Array getVariableValues(ucar.nc2.Variable v2,
		Section section) throws IOException, InvalidRangeException {
			ucar.ma2.Array dataArray = null;
			dataArray = Array.factory(v2.getDataType(), v2.getShape());
			Index dataIndex = dataArray.getIndex();
			String varName = v2.getName();

			readData();

		// Header Values
		if (varName.equals(CREATION_SITE_ID.getName())) {
			dataArray.setObject(dataIndex.set(0), headerRec.getDataSetCreationSiteID());
		} else if (varName.equals(FORMAT_VERSION.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getFormatVersion());
		} else if (varName.equals(FORMAT_VERSION_YEAR.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getFormatVersionYear());
		} else if (varName.equals(FORMAT_VERSION_DOY.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getFormatVersionDayOfYear());
		} else if (varName.equals(LOGICAL_RECORD_LENGTH.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getLogicalRecordLength());
		} else if (varName.equals(BLOCK_SIZE.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getBlockSize());
		} else if (varName.equals(HEADER_RECORD_COUNT.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getHeaderRecordCount());
		} else if (varName.equals(DATA_SET_NAME.getName())) {
			String s = headerRec.getDataSetName();
			for (int i = 0; i < s.length(); i++) {
				dataArray.setChar(dataIndex.set(i), s.charAt(i));
			}
		} else if (varName.equals(PROCESSING_BLOCK_ID.getName())) {
			String s = headerRec.getProcessingBlockID();
			for (int i = 0; i < s.length(); i++) {
				dataArray.setChar(dataIndex.set(i), s.charAt(i));
			}
		} else if (varName.equals(CRAFT_ID.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getSpacecraftID());
		} else if (varName.equals(INSTRUMENT_ID.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getInstrumentID());
		} else if (varName.equals(DATA_TYPE_ID.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getDataTypeID());
		} else if (varName.equals(TIP_SOURCE_CODE.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getTipSourceCode());
		} else if (varName.equals(DATA_START_DAY_COUNT.getName())) {
			dataArray.setLong(dataIndex.set(0), headerRec.getStartDayCount());
		} else if (varName.equals(DATA_START_YEAR.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getStartYear());
		} else if (varName.equals(DATA_START_DAY.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getStartDayOfYear());
		} else if (varName.equals(DATA_START_TIME_UTC.getName())) {
			dataArray.setLong(dataIndex.set(0), headerRec.getStartTime());
		} else if (varName.equals(DATA_END_DAY_COUNT.getName())) {
			dataArray.setLong(dataIndex.set(0), headerRec.getEndDayCount());
		} else if (varName.equals(DATA_END_YEAR.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getEndYear());
		} else if (varName.equals(DATA_END_DAY.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getEndDayOfYear());
		} else if (varName.equals(DATA_END_TIME_UTC.getName())) {
			dataArray.setLong(dataIndex.set(0), headerRec.getEndTime());
		} else if (varName.equals(CPIDS_YEAR.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getCpidsYear());
		} else if (varName.equals(CPIDS_DAY.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getCpidsDayOfYear());
		}

		// //Data Set Quality Indicators
		else if (varName.equals(DATA_RECORDS_COUNT.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getDataRecordCounts());
		} else if (varName.equals(CALIBRATED_COUNT.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getCalibratedScans());
		} else if (varName.equals(MISSING_SCANLINE_COUNT.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getMissingLinesCount());
		} else if (varName.equals(DATA_GAP_COUNT.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getDataGapsCount());
		}

		else if (varName.equals(EARTH_LOCATION_ERROR.getName())) {
			dataArray.setInt(dataIndex.set(0), headerRec.getEarthLocationError());
		}

		// else if(varName.equals(EARTH_LOCATION_ERROR_CODE_0.getName())){
			// dataArray.setShort(dataIndex.set(0),(short)headerRec.getEarthLocationErrorCode0());
		// }

		// else if(varName.equals(EARTH_LOCATION_ERROR_CODE_1.getName())){
			// dataArray.setShort(dataIndex.set(0),(short)headerRec.getEarthLocationErrorCode1());
		// }

		else if (varName.equals(EARTH_LOCATION_ERROR_CODE_4.getName())) {
			dataArray.setShort(dataIndex.set(0), (short) headerRec	.getEarthLocationErrorCode4());
		} else if (varName.equals(EARTH_LOCATION_ERROR_CODE_5.getName())) {
			dataArray.setShort(dataIndex.set(0), (short) headerRec	.getEarthLocationErrorCode5());
		} else if (varName.equals(EARTH_LOCATION_ERROR_CODE_6.getName())) {
			dataArray.setShort(dataIndex.set(0), (short) headerRec	.getEarthLocationErrorCode6());
		} else if (varName.equals(EARTH_LOCATION_ERROR_CODE_7.getName())) {
			dataArray.setShort(dataIndex.set(0), (short) headerRec	.getEarthLocationErrorCode7());
		}

		// Radiance Conversion
		else if (varName.equals(SOLAR_IRRADIANCE_CHAN1.getName())) {
			int[] data = headerRec.getSolarIrradiance();
			dataArray.setInt(dataIndex.set(0), data[0]);
		} else if (varName.equals(SOLAR_IRRADIANCE_CHAN2.getName())) {
			int[] data = headerRec.getSolarIrradiance();
			dataArray.setInt(dataIndex.set(0), data[1]);
		} else if (varName.equals(SOLAR_IRRADIANCE_CHAN3A.getName())) {
			int[] data = headerRec.getSolarIrradiance();
			dataArray.setInt(dataIndex.set(0), data[2]);
		} else if (varName.equals(EQUIVALENT_WIDTH_CHAN1.getName())) {
			int[] data = headerRec.getEquivalentWidth();
			dataArray.setInt(dataIndex.set(0), data[0]);
		} else if (varName.equals(EQUIVALENT_WIDTH_CHAN1.getName())) {
			int[] data = headerRec.getEquivalentWidth();
			dataArray.setInt(dataIndex.set(0), data[1]);
		} else if (varName.equals(EQUIVALENT_WIDTH_CHAN1.getName())) {
			int[] data = headerRec.getEquivalentWidth();
			dataArray.setInt(dataIndex.set(0), data[2]);
		} else if (varName.equals(CONSTANT1_CHAN3B.getName())) {
			int[] data = headerRec.getConstant1();
			dataArray.setInt(dataIndex.set(0), data[0]);
		} else if (varName.equals(CONSTANT1_CHAN4.getName())) {
			int[] data = headerRec.getConstant1();
			dataArray.setInt(dataIndex.set(0), data[1]);
		} else if (varName.equals(CONSTANT1_CHAN5.getName())) {
			int[] data = headerRec.getConstant1();
			dataArray.setInt(dataIndex.set(0), data[2]);
		} else if (varName.equals(CONSTANT2_CHAN3B.getName())) {
			int[] data = headerRec.getConstant2();
			dataArray.setInt(dataIndex.set(0), data[0]);
		} else if (varName.equals(CONSTANT2_CHAN4.getName())) {
			int[] data = headerRec.getConstant2();
			dataArray.setInt(dataIndex.set(0), data[1]);
		} else if (varName.equals(CONSTANT2_CHAN5.getName())) {
			int[] data = headerRec.getConstant2();
			dataArray.setInt(dataIndex.set(0), data[2]);
		} else if (varName.equals(CENTRAL_WAVE_CHAN3B.getName())) {
			int[] data = headerRec.getCentralWavenumber();
			dataArray.setInt(dataIndex.set(0), data[0]);
		} else if (varName.equals(CENTRAL_WAVE_CHAN4.getName())) {
			int[] data = headerRec.getCentralWavenumber();
			dataArray.setInt(dataIndex.set(0), data[1]);
		} else if (varName.equals(CENTRAL_WAVE_CHAN5.getName())) {
			int[] data = headerRec.getCentralWavenumber();
			dataArray.setInt(dataIndex.set(0), data[2]);
		}

		// navigation
		else if (varName.equals(ELIPSOID_MODEL_ID.getName())) {
			String data = headerRec.getReferenceElipsoid();
			dataArray.setObject(dataIndex.set(0), data);
		} else if (varName.equals(NADIR_EARTH_LOC_TOLERANCE.getName())) {
			int data = headerRec.getNadirEarthLocationTolerance();
			dataArray.setInt(dataIndex.set(0), data);
		}
		// else if(varName.equals(EARTH_LOCATION_BIT_2.getName())){
			// int data = headerRec.getEarthLocationBitField2();
			// dataArray.setInt(dataIndex.set(0),data);
		// }
		else if (varName.equals(EARTH_LOCATION_BIT_1.getName())) {
			int data = headerRec.getEarthLocationBitField1();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(EARTH_LOCATION_BIT_0.getName())) {
			int data = headerRec.getEarthLocationBitField0();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(ROLL_ATTITUDE_ERROR.getName())) {
			int data = headerRec.getRollAttitudeError();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(PITCH_ATTITUDE_ERROR.getName())) {
			int data = headerRec.getPitchAttitudeError();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(YAW_ATTITUDE_ERROR.getName())) {
			int data = headerRec.getYawAttitudeError();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(VECTOR_EPOCH_YEAR.getName())) {
			int data = headerRec.getVectorYear();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(VECTOR_EPOCH_DAY.getName())) {
			int data = headerRec.getVectorDay();
			dataArray.setObject(dataIndex.set(0), data);
		} else if (varName.equals(VECTOR_EPOCH_UTC.getName())) {
			long data = headerRec.getVectorTimeOfDay();
			dataArray.setLong(dataIndex.set(0), data);
		} else if (varName.equals(SEMI_MAJOR_AXIS.getName())) {
			int data = headerRec.getSemiMajorAxis();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(ECCENTRICITY.getName())) {
			int data = headerRec.getEccentricity();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(INCLINATION.getName())) {
			int data = headerRec.getInclination();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(ARGUMENT_OF_PERIGEE.getName())) {
			int data = headerRec.getArgumentOfPerigee();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(RIGHT_ASCENSION.getName())) {
			int data = headerRec.getRightAscension();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(MEAN_ANOMALY.getName())) {
			int data = headerRec.getMeanAnomaly();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(POSITION_VECT_X.getName())) {
			int data = headerRec.getPositionVectorXComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(POSITION_VECT_Y.getName())) {
			int data = headerRec.getPositionVectorYComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(POSITION_VECT_Z.getName())) {
			int data = headerRec.getPositionVectorZComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(VELOCITY_VECT_XDOT.getName())) {
			int data = headerRec.getVelocityVectorXComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(VELOCITY_VECT_YDOT.getName())) {
			int data = headerRec.getVelocityVectorYComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(VELOCITY_VECT_ZDOT.getName())) {
			int data = headerRec.getVelocityVectorZComponent();
			dataArray.setInt(dataIndex.set(0), data);
		} else if (varName.equals(EARTH_SUN_DIST_RATIO.getName())) {
			long data = headerRec.getEarthSunDistanceRatio();
			dataArray.setLong(dataIndex.set(0), data);
		}

		// //////////////// Data Record Values
		// ///////////////////////////////////
		int[] xy = dataArray.getShape();

		if (varName.equals(SCAN_LINE_NUMBER.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getScanLine());
			}
		} else if (varName.equals(SCAN_LINE_YEAR.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getScanYear());
			}
		} else if (varName.equals(SCAN_LINE_DOY.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getScanDayOfYear());
			}
		} else if (varName.equals(SCAN_LINE_UTC.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setLong(dataIndex.set(i), dr.getScanTime());
			}
		} else if (varName.equals(SAT_CLOCK_DRIFT_DELTA.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setShort(dataIndex.set(i), dr.getSatelliteDrift());
			}
		} else if (varName.equals(CHANNEL_3A_3B_BIT.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setShort(dataIndex.set(i), dr.getScanLineBitField0());
			}
		} else if (varName.equals(QUALITY_INDICATOR_BIT_FIELD.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr
						.getQualityIndicatorBitField());
			}
		} else if (varName.equals(SCAN_LINE_QUALITY_FLAGS.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray
						.setInt(dataIndex.set(i), dr.getScanLineQualityFlags());
			}
		} else if (varName.equals(CALIBRATION_QUALITY_FLAGS.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				// GACDataRecord dr = recs.get(i);,dr.getCalibration
				// dataArray.setInt(dataIndex.set(i))
			}
		} else if (varName.equals(FRAME_SYNC_BIT_ERRORS.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getFrameSyncErrorCount());
			}
		} else if (varName.equals(RAW_ROLL_ANGLE.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getRollAngle());
			}
		} else if (varName.equals(RAW_PITCH_ANGLE.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getPitchAngle());
			}
		} else if (varName.equals(RAW_YAW_ANGLE.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getYawAngle());
			}
		} else if (varName.equals(RAW_CRAFT_ALTITUDE.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				dataArray.setInt(dataIndex.set(i), dr.getSpacecraftAltitude());
			}
		}
		/**
		 * else if (varName.equals(RAW_SENSOR_DATA.getName())){ for (int i = 0;
		 * i < xy[2]; i++) { GACDataRecord dr = recs.get(i); short[][] data =
		 * dr.getEarthObservations(); for (int j = 0; j < xy[1]; j++) { for(int
		 * k =0; k < xy[0]; k++){ dataArray.setInt(dataIndex.set(k, j,
		 * i),data[j][k]); } } } }
		 */
		else if (varName.equals(RAW_SENSOR_DATA_CHAN1.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][0]);
				}
			}
		}

		else if (varName.equals(RAW_SENSOR_DATA_CHAN2.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][1]);
				}
			}
		} else if (varName.equals(RAW_SENSOR_DATA_CHAN3A.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int chan3status = dr.getChan3Status();
				for (int j = 0; j < xy[0]; j++) {
					if (chan3status == 1) {
						dataArray.setInt(dataIndex.set(j, i), data[j][2]);
					} else {
						dataArray.setInt(dataIndex.set(j, i), 0);
					}
				}
			}
		}

		else if (varName.equals(RAW_SENSOR_DATA_CHAN3B.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int chan3status = dr.getChan3Status();
				for (int j = 0; j < xy[0]; j++) {
					if (chan3status == 0) {
						dataArray.setInt(dataIndex.set(j, i), data[j][2]);
					} else {
						dataArray.setInt(dataIndex.set(j, i), 0);
					}
				}
			}
		} else if (varName.equals(RAW_SENSOR_DATA_CHAN4.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][3]);
				}
			}
		} else if (varName.equals(RAW_SENSOR_DATA_CHAN5.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][4]);
				}
			}
		}

		// Radiance Channel Data
		else if (varName.equals(RADIANCE_CHAN1.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec	.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][0], visIntercept1[0][0], visSlope2[0][0],
						visIntercept2[0][0], visIntersection[0][0]);
				if (!dr.getHasCalibrationErrors()) {
					for (int j = 0; j < xy[0]; j++) {
						float radVal = rfc.calibrate(data[j][0]);
						// float refVal = rrfc.calibrate(radVal);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
					}
				} else {
					for (int j = 0; j < xy[0]; j++) {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		} else if (varName.equals(RADIANCE_CHAN2.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec	.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][1], visIntercept1[0][1], visSlope2[0][1], visIntercept2[0][1], visIntersection[0][1]);
				for (int j = 0; j < xy[0]; j++) {
					if (!dr.getHasCalibrationErrors()) {
						float radVal = rfc.calibrate(data[j][1]);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
						// float refVal = rrfc.calibrate(radVal);
					} else {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		} else if (varName.equals(RADIANCE_CHAN3A.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec	.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][2], visIntercept1[0][2], visSlope2[0][2], visIntercept2[0][2], visIntersection[0][2]);
				int chan3status = dr.getChan3Status();
				for (int j = 0; j < xy[0]; j++) {
					if (chan3status == 1 && !dr.getHasCalibrationErrors()) {
						float radVal = rfc.calibrate(data[j][2]);
						// float refVal = rrfc.calibrate(radVal);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
					} else {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		} else if (varName.equals(RADIANCE_CHAN3B.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][4]);
				}
			}
		} else if (varName.equals(RADIANCE_CHAN4.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][4]);
				}
			}
		} else if (varName.equals(RADIANCE_CHAN5.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[j][4]);
				}
			}
		}

		// Calibrated Channel Data
		else if (varName.equals(SENSOR_DATA_CHAN1.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec	.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][0], visIntercept1[0][0], visSlope2[0][0], visIntercept2[0][0], visIntersection[0][0]);
				if (!dr.getHasCalibrationErrors()) {
					for (int j = 0; j < xy[0]; j++) {
						float radVal = rfc.calibrate(data[j][0]);
						// float refVal = rrfc.calibrate(radVal);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
					}
				} else {
					for (int j = 0; j < xy[0]; j++) {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		}

		else if (varName.equals(SENSOR_DATA_CHAN2.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][1], visIntercept1[0][1], visSlope2[0][1],
						visIntercept2[0][1], visIntersection[0][1]);
				for (int j = 0; j < xy[0]; j++) {
					if (!dr.getHasCalibrationErrors()) {
						float radVal = rfc.calibrate(data[j][1]);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
						// float refVal = rrfc.calibrate(radVal);
					} else {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		}

		else if (varName.equals(SENSOR_DATA_CHAN3A.getName())) {
			Radiance2ReflectanceFactorCalibrator rrfc = new Radiance2ReflectanceFactorCalibrator(
					headerRec.getEquivalentWidth()[0], headerRec.getSolarIrradiance()[0], headerRec.getEarthSunDistanceRatio());
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] visSlope1 = dr.getVisSlope1();
				int[][] visIntercept1 = dr.getVisIntercept1();
				int[][] visSlope2 = dr.getVisSlope2();
				int[][] visIntercept2 = dr.getVisIntercept2();
				int[][] visIntersection = dr.getVisIntersection();
				ReflectanceFactorCalibrator rfc = new ReflectanceFactorCalibrator(
						visSlope1[0][2], visIntercept1[0][2], visSlope2[0][2], visIntercept2[0][2], visIntersection[0][2]);
				int chan3status = dr.getChan3Status();
				for (int j = 0; j < xy[0]; j++) {
					if (chan3status == 1 && !dr.getHasCalibrationErrors()) {
						float radVal = rfc.calibrate(data[j][2]);
						// float refVal = rrfc.calibrate(radVal);
						dataArray.setFloat(dataIndex.set(j, i), radVal);
					} else {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		}

		/**
		 * else if (varName.equals(SENSOR_DATA_CHAN3B_IR.getName())){ for (int i
		 * = 0; i < xy[1]; i++) { GACDataRecord dr = recs.get(i); short[][] data
		 * = dr.getEarthObservations(); int[][] coeff1 = dr.getIrCoefficient1();
		 * int[][] coeff2 = dr.getIrCoefficient2(); int[][] coeff3 =
		 * dr.getIrCoefficient3(); int chan3status = dr.getChan3Status(); for
		 * (int j = 0; j < xy[0]; j++) { if(chan3status ==0){ float irVal =
		 * IrRadianceConverter
		 * .calibrateIrData(coeff1[0][0],coeff2[0][0],coeff3[0
		 * ][0],data[j][2],gacVersion,3); dataArray.setFloat(dataIndex.set(j,
		 * i),irVal); }else{ dataArray.setFloat(dataIndex.set(j,i),0); } } } }
		 * 
		 * else if (varName.equals(SENSOR_DATA_CHAN4_IR.getName())){ for (int i
		 * = 0; i < xy[1]; i++) { GACDataRecord dr = recs.get(i); short[][] data
		 * = dr.getEarthObservations(); int[][] coeff1 = dr.getIrCoefficient1();
		 * int[][] coeff2 = dr.getIrCoefficient2(); int[][] coeff3 =
		 * dr.getIrCoefficient3(); for (int j = 0; j < xy[0]; j++) { float irVal
		 * = IrRadianceConverter.calibrateIrData(coeff1[0][1],coeff2[0][1],
		 * coeff3[0][1],data[j][3],gacVersion,4);
		 * dataArray.setFloat(dataIndex.set(j, i),irVal); } } }
		 * 
		 * else if (varName.equals(SENSOR_DATA_CHAN5_IR.getName())){ for (int i
		 * = 0; i < xy[1]; i++) { GACDataRecord dr = recs.get(i); short[][] data
		 * = dr.getEarthObservations(); int[][] coeff1 = dr.getIrCoefficient1();
		 * int[][] coeff2 = dr.getIrCoefficient2(); int[][] coeff3 =
		 * dr.getIrCoefficient3(); for (int j = 0; j < xy[0]; j++) { float irVal
		 * = IrRadianceConverter.calibrateIrData(coeff1[0][2],coeff2[0][2],
		 * coeff3[0][2],data[j][4],gacVersion,5);
		 * dataArray.setFloat(dataIndex.set(j, i),irVal); } } }
		 */
		else if (varName.equals(SENSOR_DATA_CHAN3B_TE.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] coeff1 = dr.getIrCoefficient1();
				int[][] coeff2 = dr.getIrCoefficient2();
				int[][] coeff3 = dr.getIrCoefficient3();
				int[] const1 = headerRec.getConstant1();
				int[] const2 = headerRec.getConstant2();
				int[] cw = headerRec.getCentralWavenumber();
				int chan3status = dr.getChan3Status();
				BlackBodyTempCalibrator bbtc = new BlackBodyTempCalibrator(
						const1[0], const2[0], cw[0]);
				for (int j = 0; j < xy[0]; j++) {
					if (chan3status == 0 && !dr.getHasCalibrationErrors()) {
						float irVal = IrRadianceConverter.calibrateIrData(
								coeff1[0][0], coeff2[0][0], coeff3[0][0],
								data[j][2], gacVersion, 3);
						float teVal = bbtc.calibrate(irVal);
						dataArray.setFloat(dataIndex.set(j, i), teVal);
					} else {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		}

		else if (varName.equals(SENSOR_DATA_CHAN4_TE.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] coeff1 = dr.getIrCoefficient1();
				int[][] coeff2 = dr.getIrCoefficient2();
				int[][] coeff3 = dr.getIrCoefficient3();
				int[] const1 = headerRec.getConstant1();
				int[] const2 = headerRec.getConstant2();
				int[] cw = headerRec.getCentralWavenumber();
				BlackBodyTempCalibrator bbtc = new BlackBodyTempCalibrator(
						const1[1], const2[1], cw[1]);
				if (!dr.getHasCalibrationErrors()) {
					for (int j = 0; j < xy[0]; j++) {
						float irVal = IrRadianceConverter.calibrateIrData(
								coeff1[0][1], coeff2[0][1], coeff3[0][1],
								data[j][3], gacVersion, 4);
						float teVal = bbtc.calibrate(irVal);
						dataArray.setFloat(dataIndex.set(j, i), teVal);
					}
				} else {
					for (int j = 0; j < xy[0]; j++) {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				}
			}
		}

		else if (varName.equals(SENSOR_DATA_CHAN5_TE.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				short[][] data = dr.getEarthObservations();
				int[][] coeff1 = dr.getIrCoefficient1();
				int[][] coeff2 = dr.getIrCoefficient2();
				int[][] coeff3 = dr.getIrCoefficient3();
				int[] const1 = headerRec.getConstant1();
				int[] const2 = headerRec.getConstant2();
				int[] cw = headerRec.getCentralWavenumber();
				if (dr.getHasCalibrationErrors()) {
					// System.out.println("has errors");
					for (int j = 0; j < xy[0]; j++) {
						dataArray.setFloat(dataIndex.set(j, i),
								AvhrrConstants.INVALID_DATA_VALUE);
					}
				} else {
					BlackBodyTempCalibrator bbtc = new BlackBodyTempCalibrator(
							const1[2], const2[2], cw[2]);
					for (int j = 0; j < xy[0]; j++) {
						float irVal = IrRadianceConverter.calibrateIrData(
								coeff1[0][2], coeff2[0][2], coeff3[0][2],
								data[j][4], gacVersion, 5);
						float teVal = bbtc.calibrate(irVal);
						dataArray.setFloat(dataIndex.set(j, i), teVal);
					}
				}
			}
		}

		// /////////////////////////////////

		// lat & lon
		else if (varName.equals(ANCHOR_LATITUDE.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord dr = recs.get(j);
				int[] data = dr.getTiePointLat();
				for (int i = 0; i < 51; i++) {
					dataArray.setFloat(dataIndex.set(i, j), data[i]);
				}
			}

		} else if (varName.equals(ANCHOR_LONGITUDE.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord dr = recs.get(j);
				int[] data = dr.getTiePointLon();
				for (int i = 0; i < 51; i++) {
					dataArray.setFloat(dataIndex.set(i, j), data[i]);
				}
			}
		}

		else if (varName.equals(LATITUDE.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord dr = recs.get(j);
				int[] tempLat = dr.getTiePointLat();
				int[] tempLon = dr.getTiePointLon();
				float[] latData = new float[51];
				float[] lonData = new float[51];
				for (int i = 0; i < 51; i++) {
					latData[i] = (float) (tempLat[i] * 1E-4);
					lonData[i] = (float) (tempLon[i] * 1E-4);
				}
				float[][] vals = LatLonInterpolation.interpolate(latData,
						lonData);
				for (int i = 0; i < xy[0]; i++) {
					dataArray.setFloat(dataIndex.set((i), j),
							(float) vals[1][i]);
				}
			}
		} else if (varName.equals(LONGITUDE.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord dr = recs.get(j);
				int[] tempLat = dr.getTiePointLat();
				int[] tempLon = dr.getTiePointLon();
				float[] latData = new float[51];
				float[] lonData = new float[51];
				for (int i = 0; i < 51; i++) {
					latData[i] = (float) (tempLat[i] * 1E-4);
					lonData[i] = (float) (tempLon[i] * 1E-4);
				}
				float[][] vals = LatLonInterpolation.interpolate(latData,
						lonData);
				for (int i = 0; i < xy[0]; i++) {
					dataArray.setFloat(dataIndex.set((i), j),
							(float) vals[0][i]);
				}
			}

			// HRPT Minor Frame Telemetry

		} else if (varName.equals(HRPT_FRAME.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				byte data = dr.getHrptIDbit8();
				dataArray.setByte(dataIndex.set(i), data);
			}
		}

		else if (varName.equals(PRT_READING1.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				int data = dr.getPrtReading()[0];
				dataArray.setInt(dataIndex.set(i), data);
			}
		} else if (varName.equals(PRT_READING2.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				int data = dr.getPrtReading()[1];
				dataArray.setInt(dataIndex.set(i), data);
			}
		} else if (varName.equals(PRT_READING3.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord dr = recs.get(i);
				int data = dr.getPrtReading()[2];
				dataArray.setInt(dataIndex.set(i), data);
			}
		}

		else if (varName.equals(BACK_SCAN_CHAN3B.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				int[][] data = dr.getBackScan();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[0][j]);
				}
			}
		}

		else if (varName.equals(BACK_SCAN_CHAN4.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				int[][] data = dr.getBackScan();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[1][j]);
				}
			}
		}

		else if (varName.equals(BACK_SCAN_CHAN5.getName())) {
			for (int i = 0; i < xy[1]; i++) {
				GACDataRecord dr = recs.get(i);
				int[][] data = dr.getBackScan();
				for (int j = 0; j < xy[0]; j++) {
					dataArray.setInt(dataIndex.set(j, i), data[2][j]);
				}
			}
		}

		else if (varName.equals(SPACE_DATA.getName())) {
			for (int i = 0; i < xy[2]; i++) {
				GACDataRecord dr = recs.get(i);
				int[][] data = dr.getSpaceData();
				for (int j = 0; j < xy[1]; j++) {
					for (int k = 0; k < xy[0]; k++) {
						dataArray.setInt(dataIndex.set(k, j, i), data[j][k]);
					}
				}
			}
		}

		// clavr data
		else if (varName.equals(CLAVR_STATUS.getName())) {
			for (int i = 0; i < xy[0]; i++) {
				GACDataRecord gac = recs.get(i);
				byte b = gac.getClavrStatus();
				dataArray.setByte(dataIndex.set(i), b);
			}
		}

		else if (varName.equals(CLOUD_FLAG.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord gac = recs.get(j);
				byte[] data = gac.getClavrCodes();
				for (int i = 0; i < xy[0]; i++) {
					dataArray.setByte(dataIndex.set(i, j), data[i]);
				}
			}
		}

		// solar zenith angles
		else if (varName.equals(SOLAR_ZENITH_ANCHOR_ANGLES.getName())) {
			for (int j = 0; j < xy[1]; j++) {
				GACDataRecord dr = recs.get(j);
				short[] data = dr.getSolarZenithAngle();
				for (int i = 0; i < 51; i++) {
					dataArray.setFloat(dataIndex.set(i, j), data[i]);
				}
			}
		}

		else {

		}

		return dataArray;
		// return (dataArray.sectionNoReduce(section).copy());
	}

	
    public void close() throws IOException {
        varInfoManager.clearAll();
    }
	
	
	private void setUpDimensions() {
		int n = dimTime.size();
		if (n < 1) {
			dimRecord.add(record);
			dimTime.add(time);
			// dimLat.add(lat);
			// dimLon.add(lon);
			dimChans.add(channels);
			dimPixels.add(pixels);
			dimScans.add(scans);

			// dimLatLon.add(lat);
			// dimLatLon.add(lon);

			// dimLatLonChan.add(lat);
			// dimLatLonChan.add(lon);
			// dimLatLonChan.add(channel);

			dimChansPoints.add(channels);
			dimChansPoints.add(pixels);

			dimPixelsScan.add(pixels);
			dimPixelsScan.add(scans);

			dimChanScan.add(channels);
			dimChanScan.add(scans);

			dimWord.add(word);

			dimWordScans.add(word);
			dimWordScans.add(scans);

			dimChanPixelsScan.add(channels);
			dimChanPixelsScan.add(pixels);
			dimChanPixelsScan.add(scans);

			dimWordChanScan.add(word);
			dimWordChanScan.add(channels);
			dimWordChanScan.add(scans);

			dimCharArray.add(charArray);

			dimAnchorPixel.add(anchorPoints);
			dimAnchorPixel.add(scans);
		}
	}

	protected void readData() throws IOException {
		if (!dataHasBeenRead) {
			ArsHeader header = new ArsHeader();
			if (header.hasARSHeader(raFile)) {
				header.readArsHeader(raFile);
			}
			gacVersion = GACHeader.getVersion(raFile);
			if (gacVersion == 2) {
				headerRec = new DataSetHeaderVer2();
			} else {
				headerRec = new DataSetHeaderVer4();
			}
			raFile.seek(0);
			// System.out.println("file pos before header read " +
			// raFile.getFilePointer());
			headerRec.readHeader(raFile);
			// System.out.println("file pos after header read " +
			// raFile.getFilePointer());
			headerCount = headerRec.getHeaderRecordCount();
			scanLines = headerRec.getDataRecordCounts();
			// System.out.println("header Count: " + headerCount);
			// System.out.println("scanLines: " + scanLines);
			// read each scanline in file
			if (gacVersion == 2) {
				for (int i = 0; i < scanLines; i++) {
					DataRecordVer2 dr = new DataRecordVer2();
					dr.readScanLine(raFile);
					recs.add(dr);
				}
			} else {
				for (int i = 0; i < scanLines; i++) {
					DataRecordVer4 dr = new DataRecordVer4();
					dr.readScanLine(raFile);
					recs.add(dr);
				}
			}
			dataHasBeenRead = true;
		}
	}
	
}
