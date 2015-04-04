/**
*      Copyright (c) 2008 Work of U.S. Government.
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

package gov.noaa.ncdc.iosp.avhrr.util;

public interface ConverterConstants {
	
	//checkbox options
	static String CB_ALL_VARS = "allVariables";
	static String CB_ALL_CHANNELS = "allChannels";
	static String CB_CHANNEL1 = "channel1";
	static String CB_CHANNEL2 = "channel2";
	static String CB_CHANNEL3 = "channel3";
	static String CB_CHANNEL4 = "channel4";
	static String CB_CHANNEL5 = "channel5";
	static String CB_RAW_DATA = "rawData";
	static String CB_RADIANCE = "radiance";
	static String CB_BT = "brightnessTemp";
	static String CB_QUALITY = "qualityIndicators";
	static String CB_CALIBRATION = "calibrationCoeff";
	static String CB_METADATA = "metadata";
	static String CB_LAT_LON = "latlon";
	
	//OPTIONS
	static String OPTION_ALLCHAN = "allChannels";
	static String OPTION_ALLVAR = "allvar";
	static String OPTION_AVHRR = "avhrr";
	static String OPTION_CALIBRATION = "calibration";
	static String OPTION_CH1 = "ch1";
	static String OPTION_CH2 = "ch2";
	static String OPTION_CH3 = "ch3";
	static String OPTION_CH4 = "ch4";
	static String OPTION_CH5 = "ch5";
	static String OPTION_CLOBBER = "clobber";
	static String OPTION_DEFAULT = "default";
	static String OPTION_LATLON = "latlon";
	static String OPTION_METADATA = "metadata";
	static String OPTION_NOGUI = "nogui";
	static String OPTION_RADIANCE = "radiance";
	static String OPTION_RAW = "raw";
	static String OPTION_TEMP = "temperature";
	static String OPTION_QUALITY = "quality";
	
	/**
	//Channel variable names
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
	
	static String ANCHOR_LAT = "anchorLat";
	
	static String ANCHOR_LON = "anchorLon";
	
	static String LON = "lat";
	
	static String LAT = "lon";
	*/
	
/**	
	static String[] DEFAULT_VER1 = { "spacecraftID", "dataTypeID", "startYear", "startDay", "startUTC", "dataRecordCounts",
			"endYear", "endDay", "endUTC", "processingBlockId", "dataSetName","scanLineNumber","scanLineYear","scanLineDay",
			"scanLineUTC"};
*/
/**	
	static String[] DEFAULT_KLM = { "formatVersion", "formatVersionYear", "formatVersionDayOfYear", "logicalRecordLength",
			"blockSize", "headerRecordCount", "dataSetName", "processingBlockId", "spacecraftID", "instrumentID", "dataTypeID",
			"startDayCount", "startYear", "startDayOfYear", "startTime", "endDayCount", "endYear", "endDayOfYear", "endTime",
			"scanLineNumber", "scanLineYear", "scanLineDay", "scanLineUTC"};
*/
	
	static String[] DEFAULT_GOES = {"satId", "imageDate", "imageTime", "startLine", "startElement", "numLines", "numElements",
		"lineRes", "elementRes", "numChan", "bandMap", "actualImgDate", "actualImgTime " };
	
	/**
	static String[] DEFAULT_GVAR = { "satId", "imageDate", "imageTime", "startLine", "startElement", "numLines", "numElements",
			"lineRes", "elementRes", "numChan", "bandMap", "actualImgDate", "actualImgTime " };
	*/
	
	static String[] QUALITY_GOES = {};
	
	static String[] QUALITY_GVAR = {};

	/**
	static String[] QUALITY_VER1 = { "fatalFlag", "qualityTimeError", "qualityDataGap", "qualityDataJitter", "qualityCalibration",
			"qualityEarthLoc", "qualityAscendDescend", "qualityPnStatus", "qualityBitSyncStatus", "qualitySyncError",
			"qualityFrameSyncLock", "qualityFlywheeling", "qualityBitSlippage", "qualityChan3Correction", "qualityChan4Correction",
			"qualityChan5Correction", "qualityTipParity", "qualitySyncErrorCount", "dataGaps", "dacsNoFrameSyncErrorCount",
			"dacsTipParityErrors", "dacsAuxillaryErrorCount", "calibrationParameterId", "pseudoNoiseFlag", "dacsDatasource",
			"dacsTapeDirection", "dacsDataMode" };
	*/
	
	/**
	static String[] QUALITY_KLM = { "chan3A3B", "qualityBitField", "scanLineQuality", "calibrationBitField", "frameSyncErrors",
			"dataRecordCounts", "calibratedCount", "missingCount", "datagapCount", "earthLocationError", "earthLocationErrorCode0",
			"earthLocationErrorCode1", "earthLocationErrorCode4", "earthLocationErrorCode5", "earthLocationErrorCode6",
			"earthLocationErrorCode7" };
	*/
	static String[] CALIBRATION_GOES = {};
	
	static String[] CALIBRATION_GVAR = {"idet"};
	
//	static String[] CALIBRATION_VER1 = {"slopeCoefficient", "interceptCoefficient"};		
	
	/**
	static String[] CALIBRATION_KLM = { "solarIrradiance1", "solarIrradiance2", "solarIrradiance3A", "equivalentWidth1",
			"equivalentWidth2", "equivalentWidth3A", "ch3bConstant1", "ch4Constant1", "ch5Constant1", "ch3bConstant2",
			"ch4Constant2", "ch5Constant2", "centralWavenumber3b", "centralWavenumber4", "centralWavenumber5" };
	*/
	
//	static String[] OTHER_VER1 ={"numZenithAngles",  "zenithAngles", "anchorLat", "anchorLon"} ;
	
	/**
	static String[] OTHER_KLM = { "semiMajorAxis", "eccentricity", "inclination", "argumentOfPerigee", "rightAscension",
			"meanAnomaly", "positionVectorYComponent", "positionVectorZComponent", "velocityVectorXComponent",
			"velocityVectorYComponent", "velocityVectorZComponent", "earthSunDistanceRatio", "earthLocationBitField1",
			"earthLocationBitField0", "rollAttitudeError", "pitchAttitudeError", "yawAttitudeError", "vectorYear", "vectorDay",
			"vectorTimeOfDay",  "clockDrift", "rollAngle",
			"pitchAngle", "yawAngle", "craftAltitude", "hrptFrame", "prtReading", "prtReading", "prtReading", "backScan3b",
			"backScan4", "backScan5", "spaceData" };
	*/
	static String[] METADATA_GOES = { "areaStatus", "versionNum", "zCoord", "bytesPixel", "prefixBytes", "projectionNum",
			"creationDate", "creationTime", "imageId", "comment", "priKeyCalib", "priKeyNav", "secKeyNav", "validityCode", "band8",
			"actualStartScan", "prefixDocLength", "prefixCalLength", "prefixLevLength", "sourceType", "calibrationType",
			"averageSample", "poesSignal", "poesUpDown", "srcType", "calBlockOffset", "commentRecCount", "type", "iddate", "time",
			"orbitType", "declin", "rightAsc", "picCenterLine", "spinPeriod", "sweepAngle", "lineTotal", "sweepAngleDir", "eletot",
			"pitch", "yaw", "roll", "iajust", "iajtime", "iseang", "skew", "beta1scan", "beta1time", "betatime2", "beta1count",
			"beta2scan ", "beta2time", "beta2time2", "beta2scan", "gamma", "gamdot", "memo", "goesPrefix" };
	
	/**
	static String[] METADATA_GVAR = { "navType", "id", "refYaw", "refRoll", "refPitch", "refAttYaw", "epochDate", "timeDelta",
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
*/
	
//	static String[] METADATA_KLM = { "anchorLat", "anchorLon",	"clavrStatus", "cloudFlag" };
	
//	static String[] METADATA_VER1= {"numZenithAngles", "zenithAngles" , "anchorLat", "anchorLon"};
	
//	static String[] RAW_CHANNEL_VARS_VER1 = { CHANNEL1_RAW,  CHANNEL2_RAW, CHANNEL3_RAW, CHANNEL4_RAW, CHANNEL5_RAW};
	
//	static String[] RAD_CHANNEL_VARS_VER1 = 	{ CHANNEL1_RAD,CHANNEL2_RAD,CHANNEL3_RAD,CHANNEL4_RAD,CHANNEL5_RAD};

//	static String[] CAL_CHANNEL_VARS_VER1 = 	{ CHANNEL1_CAL,CHANNEL2_CAL,CHANNEL3_CAL,CHANNEL4_CAL,CHANNEL5_CAL};
	
//	static String[] RAW_CHANNEL_VARS_KLM = { CHANNEL1_RAW,  CHANNEL2_RAW,  CHANNEL3A_RAW, CHANNEL3B_RAW, CHANNEL4_RAW, CHANNEL5_RAW};
	
//	static String[] RAD_CHANNEL_VARS_KLM  = 	{ CHANNEL1_RAD,CHANNEL2_RAD, CHANNEL3A_RAD, CHANNEL3B_RAD, CHANNEL4_RAD,CHANNEL5_RAD};

//	static String[] CAL_CHANNEL_VARS_KLM  = 	{ CHANNEL1_CAL,CHANNEL2_CAL,  CHANNEL3A_CAL, CHANNEL3B_CAL, CHANNEL4_CAL,CHANNEL5_CAL};
	
//	static String[] RAW_CHANNEL_VARS_AREA = {"image"};
	
//	static String[] CAL_CHANNEL_VARS_AREA = {"calibratedData"};
	//file size options
	static int DEFAULT_VARS_SIZE_VER1 = 500;
	static int DEFAULT_VARS_SIZE_KLM = 1000;
	static int DEFAULT_VARS_SIZE_GVAR = 1500;
	static int DEFAULT_VARS_SIZE_GOES = 0;
	static int DEFAULT_VARS_SIZE_GOES_NETCDF = 200;
	static int QUALITY_SIZE_VER1 = 200000;
	static int QUALITY_SIZE_KLM = 200000;
	static int QUALITY_SIZE_GOES = 0;
	static int QUALITY_SIZE_GVAR = 0;
	static int QUALITY_SIZE_GOES_NETCDF = 0;
	static int CALIBRATION_SIZE_VER1 = 450000;
	static int CALIBRATION_SIZE_KLM = 100000;
	static int CALIBRATION_SIZE_GOES = 0;
	static int CALIBRATION_SIZE_GVAR = 0;
	static int CALIBRATION_SIZE_GOES_NETCDF = 0;
	static int LAT_LON_SIZE = 3124;
	static int METADATA_SIZE_VER1 = 577;
	static int METADATA_SIZE_KLM = 336;
	static int METADATA_SIZE_GOES = 462;
	static int METADATA_SIZE_GVAR = 0;
	static int METADATA_SIZE_GOES_NETCDF = 0;
	static double ALL_VARS_SIZE_VER1 = 8.4;
	static double ALL_VARS_SIZE_KLM = 7.0;
	static double ALL_VARS_SIZE_GOES = 13.34;
	static double ALL_VARS_SIZE_GVAR = 6.7;
	static double ALL_VARS_SIZE_GOES_NETCDF = 1.58;

}
