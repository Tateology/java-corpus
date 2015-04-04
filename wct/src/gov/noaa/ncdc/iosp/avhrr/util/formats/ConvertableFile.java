package gov.noaa.ncdc.iosp.avhrr.util.formats;

public abstract class ConvertableFile implements IConvertableFile {
	
	public static String[] CALIBRATION_VARS;
	
	public static String[] DEFAULT_VARS;
	
	public static String[] LATLON_VARS;
	
	public static String[] METADATA_VARS;
	
	public static String[] QUALITY_VARS;

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

}
