package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;

public class GoesNetcdf implements IConvertableFile {

	private static String[] CALIBRATION_VARS = {};
	
	private static String[] DEFAULT_VARS = {"sensorID","imageDate","imageTime","startLine", "startElem","numLines","numElems","dataWidth","lineRes", "elemRes","bands","crDate","crTime"};
	
	private static String[] LATLON_VARS = {"latitude","longitude"};
	
	private static String[] METADATA_VARS = {"data","prefixSize","auditTrail", "version"};
	
	private static String[] QUALITY_VARS = {};
	
	
	static String CAL_DATA_VAR = "calibratedData";
	static String RAD_DATA_VAR = "radiance";

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
		return null;
	}

	public String getChan2CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan2RadVariable() {
		return null;
	}

	public String getChan2RawVariable() {
		return null;
	}

	public String[] getChan3CalVariable() {
		return new String[] {CAL_DATA_VAR};
	}

	public String[] getChan3RadVariable() {
		return new String[] {RAD_DATA_VAR};
	}

	public String[] getChan3RawVariable() {
//		return new String[] {""};
		return null;
	}

	public String getChan4CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan4RadVariable() {
		return null;
	}

	public String getChan4RawVariable() {
		return null;
	}

	public String getChan5CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan5RadVariable() {
		return null;
	}

	public String getChan5RawVariable() {
		return null;
	}

	public List<Attribute> getGlobalAttributes() {
		List<Attribute> list = new ArrayList<Attribute>();
		list.add(new Attribute("warning!","Detector information not available.  Data was calibrated using detector 5."));
		return list;
	}

}
