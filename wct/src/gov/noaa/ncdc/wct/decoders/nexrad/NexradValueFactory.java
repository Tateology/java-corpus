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

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class NexradValueFactory {

	
	private static double[] getMaxMinValues(String palFile) throws MalformedURLException, IOException {
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/"+palFile, null);
		
    		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
    		if (palFile.endsWith(".pal")) {
        		ColorsAndValues rawCav = ColorLutReaders.parseGR2A(br);
           		br.close();
           		return new double[] { rawCav.getValues()[rawCav.getValues().length-1], rawCav.getValues()[0] };
    		}
    		else {
        		ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0];
           		br.close();
           		return new double[] { rawCav.getValues()[rawCav.getValues().length-1], rawCav.getValues()[0] };
    		}    		
    		
	}
	
	

    static public double[] getProductMaxMinValues(String variableName, int vcp, boolean classify) {
		if (variableName.equalsIgnoreCase("Reflectivity") || 
				variableName.equalsIgnoreCase("TotalReflectivityDZ") ||
				variableName.equalsIgnoreCase("Total_Power") ||
				variableName.equalsIgnoreCase("TotalPower")) {
            return getProductMaxMinValues(NexradHeader.LEVEL2_REFLECTIVITY, vcp, classify);
        }
		else if (variableName.equalsIgnoreCase("RadialVelocity") || 
				variableName.equalsIgnoreCase("Velocity") ||
				variableName.equalsIgnoreCase("RadialVelocityVR")) {
            return getProductMaxMinValues(NexradHeader.LEVEL2_VELOCITY, vcp, classify);
        }
		else if (variableName.equalsIgnoreCase("SpectrumWidth") || 
				variableName.equalsIgnoreCase("Width") ||
				variableName.equalsIgnoreCase("SpectrumWidthSW")) {
            return getProductMaxMinValues(NexradHeader.LEVEL2_SPECTRUMWIDTH, vcp, classify);
        }
        else if (variableName.equals("DifferentialReflectivity")) {
        	try {
        		return getMaxMinValues("nexrad_dp_zdr.pal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		return new double[] { -4, 8 };
        	}
        }
        else if (variableName.equals("CorrelationCoefficient")) {
        	try {
        		return getMaxMinValues("nexrad_dp_rho.pal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		return new double[] { 0, 1 };
        	}
        }
        else if (variableName.equals("DifferentialPhase")) {
        	try {
        		return getMaxMinValues("nexrad_dp_phi.pal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		return new double[] { 0, 180 };
        	}
        }
        else {
            return new double[] { 0, 200 };
        }
    }


    static public double[] getProductMaxMinValues(NexradHeader header) {
        return getProductMaxMinValues(header, false);
    }

    static public double[] getProductMaxMinValues(NexradHeader header, boolean classify) {
        if (header.getProductCode() == NexradHeader.UNKNOWN) {
            return new double[] { ((DecodeRadialDatasetSweepHeader)header).getMinValue(), 
                    ((DecodeRadialDatasetSweepHeader)header).getMaxValue() };
        }
        else {
            return getProductMaxMinValues(header.getProductCode(), header.getVCP(), classify);
        }
    }

    static public double[] getProductMaxMinValues(int productCode, int vcp, boolean classify) {
        
//        System.out.println("IN 'getProductMaxMinValues: productCode="+productCode);
        
        
        double[] vals = new double[2]; // min and max value
        switch(productCode) {
        
        case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
        	try {
        		vals = getMaxMinValues("nexrad_dp_zdr.pal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		vals = new double[] { -4, 8 };
        	}
        	break;

        case NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT:
        	try {
        		vals = getMaxMinValues("nexrad_dp_rho.pal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		vals = new double[] { 0, 1 };
        	}
        	break;
        	
        case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
        	try {
        		vals = getMaxMinValues("nexrad_l3_dkd.wctpal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		vals = new double[] { 0, 180 };
        	}
        	break;
        	
        case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
        	try {
        		vals = getMaxMinValues("nexrad_l3_oha_daa.wctpal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		vals = new double[] { 0, 8 };
        	}
        	break;

        case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
        case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
        	try {
        		vals = getMaxMinValues("nexrad_l3_ohd_dsd.wctpal");
        	} catch (Exception e) {
        		e.printStackTrace();
        		vals = new double[] { -3, 3 };
        	}
        	break;


        case NexradHeader.Q2_NATL_MOSAIC_3DREFL:
            vals[0] = -7.5;
            vals[1] = 72.5;
            break;
        case NexradHeader.LEVEL2_REFLECTIVITY:
            if (classify) {

                if (vcp == 31 || vcp == 32) {
                    vals[0] = -28.0;
                    vals[1] = 32.0;
                }
                else {                     
                    vals[0] = 0.0;
                    vals[1] = 75.0;
                }
            }
            else {
                //vals[0] = -27.0;
                //vals[1] = 75.0;
                vals[0] = -22.5;
                vals[1] = 72.5;
            }

            break;
        case NexradHeader.LEVEL2_VELOCITY:
            vals[0] = -80.0;
            vals[1] = 80.0;
            break;
        case NexradHeader.LEVEL2_SPECTRUMWIDTH:
            vals[0] = -3.0;
            vals[1] = 33.0;
            break;
        case NexradHeader.L3PC_DIGITAL_HYBRID_SCAN:
            vals[0] = -22.5;
            vals[1] = 72.5;

            break;
        case NexradHeader.L3PC_DPA:
            vals[0] = 0.0;
            vals[1] = 4.0;
            break;
        case NexradHeader.L3PC_VELOCITY_32NM:
        case NexradHeader.L3PC_VELOCITY_124NM: 
        case NexradHeader.L3PC_STORM_RELATIVE_VELOCITY_124NM:
            vals[0] = -50.0;
            vals[1] = 50.0;
            break;
        case NexradHeader.L3PC_SPECTRUM_WIDTH_32NM:
        case NexradHeader.L3PC_SPECTRUM_WIDTH_124NM:
            vals[0] = -50.0;
            vals[1] = 50.0;
            break;
        case NexradHeader.L3PC_ONE_HOUR_PRECIP: 
        case NexradHeader.L3PC_THREE_HOUR_PRECIP: 
            vals[0] = 0.0;
            vals[1] = 4.0;
            break;
        case NexradHeader.L3PC_STORM_TOTAL_PRECIP: 
            vals[0] = 0.0;
            vals[1] = 15.0;
            break;
        case NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_16LVL: 
            vals[0] = 0.0;
            vals[1] = 75.0;
            break;
        case NexradHeader.L3PC_LOW_LAYER_COMP_REFLECTIVITY: 
        case NexradHeader.L3PC_MID_LAYER_COMP_REFLECTIVITY: 
        case NexradHeader.L3PC_HIGH_LAYER_COMP_REFLECTIVITY: 
            vals[0] = 0.0;
            vals[1] = 75.0;
            break;
            /*
               case 57: 
                  c = new Color[8];
                  c[0] =  new Color(  0, 0, 0, 0);
                  c[1] =  new Color(  0,   0, 246);
                  c[2] =  new Color(  0, 200,   0);
                  c[3] =  new Color(255, 255,   0);
                  c[4] =  new Color(231, 192,   0);
                  c[5] =  new Color(214,   0,   0);
                  c[6] =  new Color(255,   0, 255);
                  c[7] =  new Color(153,  85, 201);
                  break;
             */
            // Vertically Integrated Liquid
        case NexradHeader.L3PC_VERT_INT_LIQUID: 
            vals[0] = 0.0;
            vals[1] = 70.0;
            break;
        case NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID: 
            vals[0] = -5.0;
            vals[1] = 70.0;
            break;
            // Echo Tops
        case NexradHeader.L3PC_ECHO_TOPS: 
        case NexradHeader.L3PC_ENHANCED_ECHO_TOPS: 
            vals[0] = 0.0;
            vals[1] = 70.0;
            break;

        case NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY:
            vals[0] = 0.0;
            vals[1] = 75.0;
            break;
            
        case NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT:
        case NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
        case NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
            vals[0] = -22.5;
            vals[1] = 72.5;
            break;
        case NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT:
        case NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT:
            vals[0] = -80.0;
            vals[1] = 80.0;
            break;
            
        case NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP:
            vals[0] = 0.0;
            vals[1] = 5.0;
            break;
        case NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION:
        case NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION:
        	vals[0] = 0;
        	vals[1] = 15;
        	break;
            
            // Default to BREF Color Table
        default: 
            vals[0] = 0.0;
            vals[1] = 75.0;
        /*
                  c = new Color[15];
                  c[0] =  new Color(  0, 236, 236);
                  c[1] =  new Color(  1, 160, 246);
                  c[2] =  new Color(  0,   0, 246);
                  c[3] =  new Color(  0, 255,   0);
                  c[4] =  new Color(  0, 200,   0);
                  c[5] =  new Color(  0, 144,   0);
                  c[6] =  new Color(255, 255,   0);
                  c[7] =  new Color(231, 192,   0);
                  c[8] =  new Color(255, 144,   0);
                  c[9] = new Color(255,   0,   0);
                  c[10] = new Color(214,   0,   0);
                  c[11] = new Color(192,   0,   0);
                  c[12] = new Color(255,   0, 255);
                  c[13] = new Color(153,  85, 201);
                  c[14] = new Color(235, 235, 235);
         */
        } // END switch

        return vals;

    } // END static getProductMaxMinValues(nexradProductCode)            
} // END class
