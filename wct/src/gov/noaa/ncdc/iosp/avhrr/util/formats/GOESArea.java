package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;

public class GOESArea implements IConvertableFile {
	

	private static String[] CALIBRATION_VARS = {};
	
	private static String[] DEFAULT_VARS = { "satId", "imageDate", "imageTime", "startLine", "startElement", "numLines", "numElements",
		"lineRes", "elementRes", "numChan", "bandMap", "actualImgDate", "actualImgTime " };
	
	private static String[] LATLON_VARS = {"latitude","longitude"};
	
	private static String[] METADATA_VARS = { "areaStatus", "versionNum", "zCoord", "bytesPixel", "prefixBytes", "projectionNum",
		"creationDate", "creationTime", "imageId", "comment", "priKeyCalib", "priKeyNav", "secKeyNav", "validityCode", "band8",
		"actualStartScan", "prefixDocLength", "prefixCalLength", "prefixLevLength", "sourceType", "calibrationType",
		"averageSample", "poesSignal", "poesUpDown", "srcType", "calBlockOffset", "commentRecCount", "type", "iddate", "time",
		"orbitType", "declin", "rightAsc", "picCenterLine", "spinPeriod", "sweepAngle", "lineTotal", "sweepAngleDir", "eletot",
		"pitch", "yaw", "roll", "iajust", "iajtime", "iseang", "skew", "beta1scan", "beta1time", "betatime2", "beta1count",
		"beta2scan ", "beta2time", "beta2time2", "beta2scan", "gamma", "gamdot", "memo", "goesPrefix" };
	
	private static String[] QUALITY_VARS = {};
	
	static String RAW_DATA_VAR= "image";
	
	static String CAL_DATA_VAR = "calibratedData";

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
		return CAL_DATA_VAR;
	}

	public String getChan1RadVariable() {
		return null;
	}

	public String getChan1RawVariable() {
		return RAW_DATA_VAR;
	}

	public String getChan2CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan2RadVariable() {
		return null;
	}

	public String getChan2RawVariable() {
		return RAW_DATA_VAR;
	}

	public String[] getChan3CalVariable() {
		return new String[] {CAL_DATA_VAR};
	}

	public String[] getChan3RadVariable() {
		return null;
	}

	public String[] getChan3RawVariable() {
		return new String[] {RAW_DATA_VAR};
	}

	public String getChan4CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan4RadVariable() {
		return null;
	}

	public String getChan4RawVariable() {
		return RAW_DATA_VAR;
	}

	public String getChan5CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan5RadVariable() {
		return null;
	}

	public String getChan5RawVariable() {
		return RAW_DATA_VAR;
	}

	public List<Attribute> getGlobalAttributes() {
		return new ArrayList<Attribute>();
	}
}
