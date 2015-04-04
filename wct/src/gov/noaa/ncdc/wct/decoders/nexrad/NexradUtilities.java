/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.io.FileScanner;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import ucar.nc2.dt.RadialDatasetSweep;


public class NexradUtilities {

	public static StreamingRadialDecoder getDecoder(NexradHeader header) 
	throws DecodeException {

		StreamingRadialDecoder data;
		if (header.getProductType() == NexradHeader.LEVEL2) {
			data = new DecodeRadialDatasetSweep((DecodeRadialDatasetSweepHeader)header);
		}
		else if (header.getProductType() == NexradHeader.XMRG) {
			data = new DecodeXMRGData((DecodeXMRGHeader) header);
		}
		else {
			data = new DecodeL3Nexrad((DecodeL3Header) header);
		}
		return data;
	}


	public static DecodeL3Alpha getAlphaDecoder(NexradHeader header) 
	throws DecodeException, IOException {

		DecodeL3Alpha alphaDecoder = null;

		if (header.getProductType() == NexradHeader.L3ALPHA) {
			int pcode = header.getProductCode();
			if (pcode == 58) {
				alphaDecoder = new DecodeStormTracking((DecodeL3Header)header);
			}
			else if (pcode == 59) {
				alphaDecoder = new DecodeHail((DecodeL3Header)header);
			}
			else if (pcode == 60) {
				alphaDecoder = new DecodeMeso((DecodeL3Header)header);
			}
			else if (pcode == 61) {
				alphaDecoder = new DecodeTVS((DecodeL3Header)header);
			}
			else if (pcode == 62) {
				alphaDecoder = new DecodeStormStructure((DecodeL3Header)header);
			}
			else if (pcode == 141) {
				alphaDecoder = new DecodeMDA((DecodeL3Header)header);
			}

			return alphaDecoder;

		}
		else {
			throw new DecodeException("PRODUCT IS NOT ALPHANUMERIC: PCODE="+header.getProductCode(), header.getDataURL());
		}
	}




	public static String getUnits(NexradHeader header) {
		return getUnits(header.getProductCode());
	}

	public static String getUnits(int productCode) {

		//       System.out.println("IN 'getUnits: productCode="+productCode);


		String units;
		switch(productCode){          

		case NexradHeader.LEVEL2_REFLECTIVITY:
			units = "dBZ";
			break;
		case NexradHeader.LEVEL2_VELOCITY:
		case NexradHeader.LEVEL2_SPECTRUMWIDTH:
			units = "KT";
			break;

		case NexradHeader.L3PC_ECHO_TOPS:
		case NexradHeader.L3PC_ENHANCED_ECHO_TOPS:
			units = "KFT";
			break;
		case 25:
		case 27: 
		case 28:
		case 30:
		case 56:
			units = "KT";
			break;
		case 48:
			units = "KT RMS";
			break;
		case NexradHeader.L3PC_VERT_INT_LIQUID:
		case NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID:
			units = "KG/M2";
			break;
		case 19: 
		case 20: 
		case 36: 
		case 37: 
		case 38: 
		case 65:
		case 66:
		case 90:
			units = "dBZ";
			break;
		case NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP:
		case 78: 
		case 79: 
		case 80: 
		case 81:
		case NexradHeader.L3PC_ONE_HOUR_ACCUMULATION:
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
		case NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION:
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION:
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
		case NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION:
		case NexradHeader.XMRG:
			units = "IN";
			break;
		case NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE:
			units = "IN/HR";
			break;
		case 59: 
			units = "N/A";
			break;

		case NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY:
		case NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
		case NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT:
		case NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
			units = "dBZ";
			break;

		case NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT:
		case NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT:
			units = "KT";
			break;
			
		case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
			units = "DEG/KM";
			break;
			
		case NexradHeader.LEVEL2_DIFFERENTIALPHASE:
			units = "DEG";
			break;

		case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
			units = "dB";
			break;


			// Default to BREF Color Table
		default: 
			units = "";
		} // END switch

		return units;

	} // END static getUnits(nexradProductCode)

	public static String getLongName(NexradHeader header) {
		return getLongName(header.getProductCode());
	}

	public static String getLongName(int productCode) {

		String name;
		switch(productCode){          

		case NexradHeader.LEVEL2_REFLECTIVITY:
			name = "Level-II Base Reflectivity";
			break;
		case NexradHeader.LEVEL2_VELOCITY:
			name = "Level-II Base Velocity";
			break;
		case NexradHeader.LEVEL2_SPECTRUMWIDTH:
			name = "Level-II Base Spectrum Width";
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY:
			name = "Level-II Differential Reflectivity";
			break;
		case NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT:
			name = "Level-II Correlation Coefficient";
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALPHASE:
			name = "Level-II Differential Phase";
			break;

		case 41:
			name = "Level-III Echo Tops";
			break;
		case 25:
			name = "Level-III Base Velocity (32 nm)";
			break;
		case 27: 
			name = "Level-III Base Velocity (124 nm)";
			break;
		case 28:
			name = "Level-III Base Spectrum Width (32 nm)";
			break;
		case 30:
			name = "Level-III Base Spectrum Width (124 nm)";
			break;
		case 56:
			name = "Level-III Storm Relative Velocity (124 nm)";
			break;
		case 48:
			name = "Level-III VAD Wind Profile";
			break;
		case 57:
			name = "Level-III Vertical Integrated Liquid";
			break;
		case 19: 
			name = "Level-III Base Reflectivity (124 nm)";
			break;
		case 20: 
			name = "Level-III Base Reflectivity (248 nm)";
			break;
		case 36: 
			name = "Level-III Composite Reflectivity (8 levels / 248 nm)";
			break;
		case 37: 
			name = "Level-III Composite Reflectivity (16 levels / 124 nm)";
			break;
		case 38: 
			name = "Level-III Composite Reflectivity (16 levels / 248 nm)";
			break;
		case 65:
			name = "Level-III Low Layer Composite Reflectivity";
			break;
		case 66:
			name = "Level-III Mid Layer Composite Reflectivity";
			break;
		case 90:
			name = "Level-III High Layer Composite Reflectivity";
			break;
		case 78: 
			name = "Level-III One Hour Precipitation Total";
			break;
		case 79: 
			name = "Level-III Three Hour Precipitation Total";
			break;
		case 80: 
			name = "Level-III Storm Total Precipitation Total";
			break;
		case 81:
			name = "Level-III Digital Precipitation Array";
			break;
		case NexradHeader.XMRG:
			name = "Stage-III Regional Mosaic";
			break;
		case 59: 
			name = "Level-III Hail Index Alphanumeric";
			break;
		case 60: 
			name = "Level-III Mesocyclone Alphanumeric";
			break;
		case 61: 
			name = "Level-III Tornadic Vortex Signature Alphanumeric";
			break;
		case 62: 
			name = "Level-III Storm Structure Alphanumeric";
			break;
		case 94:
			name = "Level-III Base Reflectivity (124 nm)";
			break;
		case 99:
			name = "Level-III Base Velocity (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
			name = "Level-III Digital Differential Reflectivity (162 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT:
			name = "Level-III Digital Correlation Coefficient (162 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
			name = "Level-III Digital Specific Differential Phase (162 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION:
			name = "Level-III Digital Hydrometeor Classification (162 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION:
			name = "Level-III Digital Hybrid Hydrometeor Classification (124 nm)";
			break;
		case NexradHeader.L3PC_ONE_HOUR_ACCUMULATION:
			name = "Level-III One Hour Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
			name = "Level-III Digital One Hour Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
			name = "Level-III Digital One Hour Accumulation Difference (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION:
			name = "Level-III One Hour Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION:
			name = "Level-III Digital One Hour Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
			name = "Level-III Digital One Hour Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION:
			name = "Level-III Digital User Selectable Accumulation (Dual Pol.) (124 nm)";
			break;
		case NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE:
			name = "Level-III Digital Instantaneous Precipitation Rate (Dual Pol.) (124 nm)";
			break;


		default: 
			name = "Unknown";
		} // END switch

		return name;

	} // END static getLongName(nexradProductCode)            




	public static String getVariableName(NexradHeader header) {
		return getVariableName(header.getProductCode());
	}

	public static String getVariableName(int productCode) {

		String name;
		switch(productCode){          

		case NexradHeader.LEVEL2_REFLECTIVITY:
			name = "bref";
			break;
		case NexradHeader.LEVEL2_VELOCITY:
			name = "vel";
			break;
		case NexradHeader.LEVEL2_SPECTRUMWIDTH:
			name = "sw";
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY:
			name = "zdr";
			break;
		case NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT:
			name = "cc";
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALPHASE:
			name = "kdp";
			break;

		case 41:
			name = "et";
			break;
		case 25:
			name = "vel";
			break;
		case 27: 
			name = "vel";
			break;
		case 28:
			name = "sw";
			break;
		case 30:
			name = "sw";
			break;
		case 56:
			name = "srvel";
			break;
		case 48:
			name = "vad";
			break;
		case 57:
			name = "vil";
			break;
		case 19: 
			name = "bref";
			break;
		case 20: 
			name = "bref";
			break;
		case 36: 
			name = "cref";
			break;
		case 37: 
			name = "cref";
			break;
		case 38: 
			name = "cref";
			break;
		case 65:
			name = "lcref";
			break;
		case 66:
			name = "mcref";
			break;
		case 90:
			name = "hcref";
			break;
		case 78: 
			name = "ohp";
			break;
		case 79: 
			name = "thp";
			break;
		case 80: 
			name = "stp";
			break;
		case 81:
			name = "dpa";
			break;
		case NexradHeader.XMRG:
			name = "qpe";
			break;
		case 59: 
			name = "hail";
			break;
		case 60: 
			name = "meso";
			break;
		case 61: 
			name = "tvs";
			break;
		case 62: 
			name = "ss";
			break;
		case 94:
			name = "bref";
			break;
		case 99:
			name = "vel";
			break;
		case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
			name = "zdr";
			break;
		case NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT:
			name = "cc";
			break;
		case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
			name = "kdp";
			break;
		case NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION:
			name = "class";
			break;
		case NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION:
			name = "class";
			break;
		case NexradHeader.L3PC_ONE_HOUR_ACCUMULATION:
			name = "oha";
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
			name = "daa";
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
			name = "dod";
			break;
		case NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION:
			name = "sta";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION:
			name = "dsa";
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
			name = "dsd";
			break;
		case NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE:
			name = "dpr";
			break;

		default: 
			name = "value";
		} // END switch

		return name;

	} // END static getLongName(nexradProductCode)            


	public static double getElevationAngle(DecodeL3Header level3Header) {
		if (level3Header.getProductCode() == NexradHeader.L3PC_VELOCITY_32NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_VELOCITY_124NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_STORM_RELATIVE_VELOCITY_124NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_SPECTRUM_WIDTH_32NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_SPECTRUM_WIDTH_124NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_124NM ||
				level3Header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_248NM || 
				level3Header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT || 
				level3Header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT || 
				level3Header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT || 
				level3Header.getProductCode() == NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT || 
				level3Header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT || 
				level3Header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY ||
				level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY || 
				level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT ||
				level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE ||
				level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION 
				
		) {
			return level3Header.getProductSpecificValue(2) / 10.0;
		}
		else {
			return Double.NaN;
		}
	}



	public static int[] getCategoryIndices(DecodeL3Header header, double minValue, double maxValue, boolean useRF) {

		String[] headerCategories = header.getDataThresholdStringArray();
		Vector<Integer> acceptedCategories = new Vector<Integer>();
		for (int i=0; i<headerCategories.length; i++) {
			try {
				if (headerCategories[i].trim().equals("ND")) {
					;
				}
				else if (useRF && headerCategories[i].trim().equals("RF")) {
					acceptedCategories.addElement(new Integer(i));
				}
				else {
					double value = Double.parseDouble(headerCategories[i]);
					if (value >= minValue && value <= maxValue) {
						//System.out.println(value + " [ "+minValue+" - "+maxValue+" ] ");
						acceptedCategories.addElement(new Integer(i));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}         
		}
		int[] intArray = new int[acceptedCategories.size()];
		for (int i=0; i<intArray.length; i++) {
			intArray[i] = Integer.parseInt(acceptedCategories.elementAt(i).toString());
			//System.out.println("ACCEPTING: "+intArray[i]);
		}
		return intArray;

	}



	public static int stripCharsInt(String str) {
		str = str.replaceAll("<", "").replaceAll(">", "").replaceAll("=", "");
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return -999;
		}

	}
	public static double stripCharsDouble(String str) {
		str = str.replaceAll("<", "").replaceAll(">", "").replaceAll("=", "");
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return -999;
		}
	}








	/**
	 * Returns site lat/lon/alt from WCT lookup table using site id found in 
	 * file header or parsed from filename.
	 * @param radialDataset
	 * @return double[3], [0]=lat,[1]=lon,[2]=alt
	 * @throws DecodeException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static double[] lookupSiteLatLonAlt(RadialDatasetSweep radialDataset) 
	throws DecodeException, SQLException, ParseException, NumberFormatException, 
	XPathExpressionException, SAXException, IOException, ParserConfigurationException {

		String id = radialDataset.getRadarID();
		if (id.equals("XXXX")) {
			FileScanner fileScanner = new FileScanner();
			fileScanner.scanURL(new URL(radialDataset.getLocationURI()));
			id = fileScanner.getLastScanResult().getSourceID();
		}
		double siteLat = RadarHashtables.getSharedInstance().getLat(id);
		double siteLon = RadarHashtables.getSharedInstance().getLon(id);
		double siteAlt = RadarHashtables.getSharedInstance().getElev(id);
		if (siteLat == -999 || siteLon == -999) {
			siteLat = radialDataset.getCommonOrigin().getLatitude();
			siteLon = radialDataset.getCommonOrigin().getLongitude();
			siteAlt = radialDataset.getCommonOrigin().getAltitude()*3.28083989501312;
		}

		return new double[] {siteLat, siteLon, siteAlt};
	}

} // END class
