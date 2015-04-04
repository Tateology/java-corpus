package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;

public class GacVer1 implements IConvertableFile {

	private static String[] CALIBRATION_VARS = { "slopeCoefficient", "interceptCoefficient" };

	private static String[] DEFAULT_VARS = { "spacecraftID", "dataTypeID", "startYear", "startDay", "startUTC", "dataRecordCounts",
			"endYear", "endDay", "endUTC", "processingBlockId", "dataSetName", "scanLineNumber", "scanLineYear", "scanLineDay",
			"scanLineUTC" };

	private static String[] LATLON_VARS = { "lat", "lon" };

	private static String[] METADATA_VARS = { "numZenithAngles", "zenithAngles", "anchorLat", "anchorLon" };

	private static String[] QUALITY_VARS = { "fatalFlag", "qualityTimeError", "qualityDataGap", "qualityDataJitter",
			"qualityCalibration", "qualityEarthLoc", "qualityAscendDescend", "qualityPnStatus", "qualityBitSyncStatus",
			"qualitySyncError", "qualityFrameSyncLock", "qualityFlywheeling", "qualityBitSlippage", "qualityChan3Correction",
			"qualityChan4Correction", "qualityChan5Correction", "qualityTipParity", "qualitySyncErrorCount", "dataGaps",
			"dacsNoFrameSyncErrorCount", "dacsTipParityErrors", "dacsAuxillaryErrorCount", "calibrationParameterId",
			"pseudoNoiseFlag", "dacsDatasource", "dacsTapeDirection", "dacsDataMode" };
	
	static String CHANNEL1_RAW = "rawSensorDataCh1";

	static String CHANNEL2_RAW = "rawSensorDataCh2";
	
	static String CHANNEL3_RAW = "rawSensorDataCh3";
	
	static String CHANNEL3A_RAW = "rawSensorDataCh3A";
	
	static String CHANNEL3B_RAW = "rawSensorDataCh3B";
	
	static String CHANNEL4_RAW = "rawSensorDataCh4";
	
	static String CHANNEL5_RAW = "rawSensorDataCh5";
	
	static String CHANNEL1_RAD = "radianceChan1";

	static String CHANNEL2_RAD = "radianceChan2";
	
	static String CHANNEL3_RAD = "radianceChan3";
	static String CHANNEL3A_RAD = "radianceChan3A";
	static String CHANNEL3B_RAD = "radianceChan3B";
	
	static String CHANNEL4_RAD = "radianceChan4";
	
	static String CHANNEL5_RAD = "radianceChan5";
	
	static String CHANNEL1_CAL = "albedoCh1";

	static String CHANNEL2_CAL= "albedoCh2";
	
	static String CHANNEL3_CAL = "brightnessTempCh3";
	
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
		return new String[] {CHANNEL3_CAL};
	}

	public String[] getChan3RadVariable() {
		return new String[] {CHANNEL3_RAD};
	}

	public String[] getChan3RawVariable() {
		return new String[] {CHANNEL3_RAW};
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
		return CHANNEL5_RAD;
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
