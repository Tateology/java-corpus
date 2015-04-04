package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;

public class GVARArea implements IConvertableFile {
	

	private static String[] CALIBRATION_VARS = {"lidet"};
	
	private static String[] DEFAULT_VARS = { "satId", "imageDate", "imageTime", "startLine", "startElement", "numLines", "numElements",
		"lineRes", "elementRes", "numChan", "bandMap", "actualImgDate", "actualImgTime " };
	
	private static String[] LATLON_VARS = {"latitude","longitude"};
	
	private static String[] METADATA_VARS = { "navType", "id", "refYaw", "refRoll", "refPitch", "refAttYaw", "epochDate", "timeDelta",
		"epochTimeDelta", "imcRoll", "imcRoll", "imcRoll", "longitudeDeltas", "radialDeltas", "latDeltas", "yawDeltas",
		"solarRate", "expoStartTime", "exmagr", "exticr", "matanr", "sinanr", "sinusoidMagRoll", "sinusoidPaRoll", "nummsr",
		"oapsr", "o1msdr", "mmsdr", "pamsr", "aemzr", "exmagp", "exticp", "matanp", "sinanp", "sinusoidMagPitch",
		"sinusoidPaPitch", "nummsp", "oapsp", "o1msdp", "mmsdp", "pamsp", "aemzp", "exmagy", "exticy", "matany", "sinany",
		"sinusoidMagYaw", "sinusoidPaYaw", "nummsy", "oapsy", "o1msdy", "mmsdy", "pamsy", "aemzy", "exticrm", "matanrm",
		"sinanrm", "sinusoidMagRM", "sinusoidPaRM", "nummsrm", "oapsrm", "o1msdrm", "mmsdrm", "pamsrmp", "aemzrm", "exmagpm",
		"exticpm", "matanpm", "sinanpm", "sinusoidMagPM", "sinusoidPaPM", "nummspm", "oapspm", "o1msdpm", "mmsdpm", "pamspm",
		"aemzpm", "blockId", "wordSize", "wordCount", "productId", "repeatFlag", "gvarVersionNum", "dataValid", "asciiBinary",
		"spsId", "rangeWord", "blockCount", "spsTime", "errorCheck", "spcId", "spsIdentity", "iscan", "tcurr", "tched",
		"tctrl", "tlhed", "tltrl", "tipfs", "tinfs", "tispc", "tiecl", "tibbc", "tistr", "tiran", "tiirt", "tivit", "tclmt",
		"tiona", "craftId", "sourceSPS", "lside", "licha", "risct", "l1scan", "l2scan", "lpixls", "lwords", "lzcor", "llag" };
	
	private static String[] QUALITY_VARS = {};
	
	static String RAW_DATA_VAR= "image";
	
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
		return RAD_DATA_VAR;
	}

	public String getChan1RawVariable() {
		return RAW_DATA_VAR;
	}

	public String getChan2CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan2RadVariable() {
		return RAD_DATA_VAR;
	}

	public String getChan2RawVariable() {
		return RAW_DATA_VAR;
	}

	public String[] getChan3CalVariable() {
		return new String[] {CAL_DATA_VAR};
	}

	public String[] getChan3RadVariable() {
		return new String[] {RAD_DATA_VAR};
	}

	public String[] getChan3RawVariable() {
		return new String[] {RAW_DATA_VAR};
	}

	public String getChan4CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan4RadVariable() {
		return RAD_DATA_VAR;
	}

	public String getChan4RawVariable() {
		return RAW_DATA_VAR;
	}

	public String getChan5CalVariable() {
		return CAL_DATA_VAR;
	}

	public String getChan5RadVariable() {
		return RAD_DATA_VAR;
	}

	public String getChan5RawVariable() {
		return RAW_DATA_VAR;
	}

	public List<Attribute> getGlobalAttributes() {
		return new ArrayList<Attribute>();
	}

}
