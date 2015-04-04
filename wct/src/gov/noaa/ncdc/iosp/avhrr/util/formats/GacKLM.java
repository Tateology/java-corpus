package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;

public class GacKLM implements IConvertableFile {
	

	private static String[] CALIBRATION_VARS = { "solarIrradiance1", "solarIrradiance2", "solarIrradiance3A", "equivalentWidth1",
		"equivalentWidth2", "equivalentWidth3A", "ch3bConstant1", "ch4Constant1", "ch5Constant1", "ch3bConstant2",
		"ch4Constant2", "ch5Constant2", "centralWavenumber3b", "centralWavenumber4", "centralWavenumber5"};
	
	private static String[] DEFAULT_VARS = {"formatVersion", "formatVersionYear", "formatVersionDayOfYear", "logicalRecordLength",
		"blockSize", "headerRecordCount", "dataSetName", "processingBlockId", "spacecraftID", "instrumentID", "dataTypeID",
		"startDayCount", "startYear", "startDayOfYear", "startTime", "endDayCount", "endYear", "endDayOfYear", "endTime",
		"scanLineNumber", "scanLineYear", "scanLineDay", "scanLineUTC"};
	
	private static String[] LATLON_VARS = {"lat","lon"};
	
	private static String[] METADATA_VARS = { "anchorLat", "anchorLon",	"clavrStatus", "cloudFlag"};
	
	private static String[] QUALITY_VARS = { "chan3A3B", "qualityBitField", "scanLineQuality", "calibrationBitField", "frameSyncErrors",
		"dataRecordCounts", "calibratedCount", "missingCount", "datagapCount", "earthLocationError", "earthLocationErrorCode0",
		"earthLocationErrorCode1", "earthLocationErrorCode4", "earthLocationErrorCode5", "earthLocationErrorCode6",
		"earthLocationErrorCode7"};
	
	
	static String CHANNEL1_RAW = "rawSensorDataCh1";

	static String CHANNEL2_RAW = "rawSensorDataCh2";
	
	static String CHANNEL3_RAW = "rawSensorDataCh3";
	
	static String CHANNEL3A_RAW = "rawSensorDataCh3A";
	
	static String CHANNEL3B_RAW = "rawSensorDataCh3B";
	
	static String CHANNEL4_RAW = "rawSensorDataCh4";
	
	static String CHANNEL5_RAW = "rawSensorDataCh5";
	
	static String CHANNEL1_RAD = "radianceChan1";

	static String CHANNEL2_RAD = "radianceChan2";

	static String CHANNEL3A_RAD = "radianceChan3A";

	static String CHANNEL3B_RAD = "radianceChan3B";
	
	static String CHANNEL4_RAD = "radianceChan4";
	
	static String CHANNEL5_RAD = "radianceChan5";
	
	static String CHANNEL1_CAL = "albedoCh1";

	static String CHANNEL2_CAL= "albedoCh2";
	
	static String CHANNEL3A_CAL = "albedoCh3A";
	
	static String CHANNEL3B_CAL = "brightnessTempCh3B";
	
	static String CHANNEL4_CAL = "brightnessTempCh4";
	
	static String CHANNEL5_CAL = "brightnessTempCh5";
	
	
	
	
	

	public String[] getCalibrationVariables() {
		return CALIBRATION_VARS;
	}

	public String[] getDefaultVariables() {
		return DEFAULT_VARS;
	}

	public String[] getLatLonVariables() {
		return LATLON_VARS;
	}

	public String[] getMetadataVariables() {
		return METADATA_VARS;
	}

	public String[] getQualityVariables() {
		return QUALITY_VARS;
	}

	public String getChan1CalVariable() {
		return CHANNEL1_CAL;
	}

	public String getChan1RadVariable() {
		return CHANNEL1_RAD;
	}

	public String getChan1RawVariable() {
		return CHANNEL1_RAW;
	}

	public String getChan2CalVariable() {
		return CHANNEL2_CAL;
	}

	public String getChan2RadVariable() {
		return CHANNEL2_RAD;
	}

	public String getChan2RawVariable() {
		return CHANNEL2_RAW;
	}

	public String[] getChan3CalVariable() {
		return new String[] {CHANNEL3A_CAL,CHANNEL3B_CAL};
	}

	public String[] getChan3RadVariable() {
		return new String[] {CHANNEL3A_RAD,CHANNEL3B_RAD};
	}

	public String[] getChan3RawVariable() {
		return new String[] {CHANNEL3A_RAW,CHANNEL3B_RAW};
	}

	public String getChan4CalVariable() {
		return CHANNEL4_CAL;
	}

	public String getChan4RadVariable() {
		return CHANNEL4_RAD;
	}

	public String getChan4RawVariable() {
		return CHANNEL4_RAW;
	}

	public String getChan5CalVariable() {
		return CHANNEL5_CAL;
	}

	public String getChan5RadVariable() {
		return CHANNEL4_RAD;
	}

	public String getChan5RawVariable() {
		return CHANNEL5_RAW;
	}

	public List<Attribute> getGlobalAttributes() {
		List<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute(
				"Lat/Lon Interpolation",
				"Lat/Lon values are calculated using Lagrangian Interploation of anchor points for values of Latitude between =/-85 degress.  Gnomic Interpolation is used when values of Latitude are greater than 85 degrees.  Interpolated values have not been tested."));
		return atts;
	}

}
