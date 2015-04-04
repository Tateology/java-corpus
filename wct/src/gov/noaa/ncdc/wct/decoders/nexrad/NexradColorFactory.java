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
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class NexradColorFactory {


	public static Color[] getTransparentColors(String variableName, boolean classify, int alphaChannelValue) {
		Color[] c = getColors(variableName, classify);  
		for (int i=1; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), alphaChannelValue);
			}
		}      
		return c;
	}


	/**
	 * If levels = -1, then don't calculate equal colors and values - just use colors as stated.
	 * @param paletteName
	 * @param levels
	 * @return
	 */
	public static Color[] getColors(String paletteName, int levels) {
		Color[] c = null;
		try {
			URL url = ResourceUtils.getInstance().getJarResource(
					new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
					"/config/colormaps/"+paletteName, null);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			ColorsAndValues cav = null;
			if (paletteName.endsWith(".pal")) {
				cav = ColorLutReaders.parseGR2A(br);
				if (levels > 0) {
					cav = ColorsAndValues.calculateEqualColorsAndValues(cav, levels);
				}
			}
			else {
				cav = ColorLutReaders.parseWCTPal(br)[0];
				//			   System.out.println(cav);
				if (levels > 0) {
					cav = ColorsAndValues.calculateEqualColorsAndValues(cav, levels);
					//				   System.out.println(cav);
				}
			}

			c = cav.getColors();
			br.close();

			Double[] vals = cav.getValues();        
			if (vals[0] > vals[1]) {
				WCTUtils.flipArray(c); 
				WCTUtils.flipArray(vals);
			}

		} catch (Exception e) {
			e.printStackTrace();
			c = new Color[] {Color.WHITE};
		}
		return c;
	}



	public static Color[] getColors(String variableName, boolean classify) {
		if (variableName.equalsIgnoreCase("Reflectivity") || 
				variableName.equalsIgnoreCase("TotalReflectivityDZ") ||
				variableName.equalsIgnoreCase("Total_Power") ||
				variableName.equalsIgnoreCase("TotalPower")) {
			return getColors(NexradHeader.LEVEL2_REFLECTIVITY, classify);
		}
		else if (variableName.equalsIgnoreCase("RadialVelocity") || 
				variableName.equalsIgnoreCase("Velocity") ||
				variableName.equalsIgnoreCase("RadialVelocityVR")) {
			return getColors(NexradHeader.LEVEL2_VELOCITY, classify);
		}
		else if (variableName.equalsIgnoreCase("SpectrumWidth") || 
				variableName.equalsIgnoreCase("Width") ||
				variableName.equalsIgnoreCase("SpectrumWidthSW")) {
			return getColors(NexradHeader.LEVEL2_SPECTRUMWIDTH, classify);
		}
		else if (variableName.equals("DifferentialReflectivity")) {
			return getColors(NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY, classify);
		}
		else if (variableName.equals("CorrelationCoefficient")) {
			return getColors(NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT, classify);
		}
		else if (variableName.equals("DifferentialPhase")) {
			return getColors(NexradHeader.LEVEL2_DIFFERENTIALPHASE, classify);
		}
		else {
			return getColors(NexradHeader.UNKNOWN, classify);
		}
	}


	public static Color[] getColors(int productCode) {
		return getColors(productCode, true);
	}

	public static Color[] getColors(int productCode, boolean classify) {
		return getColors(productCode, classify, 0.0f);
	}

	public static Color[] getColors(int productCode, boolean classify, float productVersion) {

		Color[] c;
		switch(productCode) {          

		case NexradHeader.UNKNOWN:
			c = new Color[] {
					//new Color(195,195,195, 60),
					new Color(  0, 0, 0, 0),
					Color.BLUE,
					Color.CYAN,
					Color.GREEN,
					Color.YELLOW,
					Color.ORANGE,
					Color.RED
			};
			break;


		case NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY:
		case NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY:
			c = getColors("nexrad_dp_zdr.pal", 19);
			break;
		case NexradHeader.LEVEL2_DIFFERENTIALPHASE:
			c = getColors("nexrad_dp_phi.pal", 20);
			break;
		case NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE:
			c = getColors("nexrad_l3_dkd.wctpal", 20);
			break;
		case NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT:
		case NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT:
			c = getColors("nexrad_dp_rho.pal", 20);
			break;
		case NexradHeader.L3PC_ONE_HOUR_ACCUMULATION:
			c = getColors("nexrad_l3_oha_daa.wctpal", -1);
			break;
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION:
			c = getColors("nexrad_l3_oha_daa.wctpal", 20);
			break;
		case NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION:
		case NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION:
			c = getColors("nexrad_l3_dhc.wctpal", -1);
			break;

		case NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION:
			c = getColors("nexrad_l3_sta_dsa.wctpal", -1);
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION:
			c = getColors("nexrad_l3_sta_dsa.wctpal", 240);
			break;
		case NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE:
		case NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE:
			c = getColors("nexrad_l3_ohd_dsd.wctpal", 21);
			break;
		case NexradHeader.Q2_NATL_MOSAIC_3DREFL:
			c = new Color[] {
					new Color(195,195,195, 60),
					new Color(  0, 0, 0, 0),
					new Color(50,   79,  79),
					new Color(  0, 236, 236),
					new Color(  1, 160, 246),
					new Color(  0,   0, 246),
					new Color(  0, 255,   0),
					new Color(  0, 200,   0),
					new Color(  0, 144,   0),
					new Color(255, 255,   0),
					new Color(231, 192,   0),
					new Color(255, 144,   0),
					new Color(255,   0,   0),
					new Color(214,   0,   0),
					new Color(192,   0,   0),
					new Color(255,   0, 255),
					new Color(153,  85, 201),
					new Color(235, 235, 235)
			};
			break;
		case NexradHeader.LEVEL2_REFLECTIVITY:               
			if (classify) {


				c = new Color[17];
				c[0] =  new Color(  0, 0, 0, 0);
				c[1] =  new Color(50,   79,  79);
				c[2] =  new Color(  0, 236, 236);
				c[3] =  new Color(  1, 160, 246);
				c[4] =  new Color(  0,   0, 246);
				c[5] =  new Color(  0, 255,   0);
				c[6] =  new Color(  0, 200,   0);
				c[7] =  new Color(  0, 144,   0);
				c[8] =  new Color(255, 255,   0);
				c[9] =  new Color(231, 192,   0);
				c[10] =  new Color(255, 144,   0);
				c[11] = new Color(255,   0,   0);
				c[12] = new Color(214,   0,   0);
				c[13] = new Color(192,   0,   0);
				c[14] = new Color(255,   0, 255);
				c[15] = new Color(153,  85, 201);
				c[16] = new Color(235, 235, 235);
				break;
			}
			else {
				c = new Color[21];
				c[0] =  new Color(  0,   0,   0,   0);

				c[1] =  new Color(50,   79,  79);
				c[2] =  new Color(102, 139, 139);
				c[3] =  new Color(150, 205, 205);
				c[4] =  new Color(174, 238, 238);
				c[5] =  new Color(187, 255, 255);

				c[6] =  new Color(  0, 236, 236);
				c[7] =  new Color(  1, 160, 246);
				c[8] =  new Color(  0,   0, 246);
				c[9] =  new Color(  0, 255,   0);
				c[10] =  new Color(  0, 200,   0);
				c[11] =  new Color(  0, 144,   0);
				c[12] =  new Color(255, 255,   0);
				c[13] =  new Color(231, 192,   0);
				c[14] =  new Color(255, 144,   0);
				c[15] = new Color(255,   0,   0);
				c[16] = new Color(214,   0,   0);
				c[17] = new Color(192,   0,   0);
				c[18] = new Color(255,   0, 255);
				c[19] = new Color(153,  85, 201);
				c[20] = new Color(235, 235, 235);
				break;
			}
			//               case NexradHeader.LEVEL2_VELOCITY:
				//                  c = new Color[] {
						//                          new Color(  0, 0, 0, 0),
						//                          new Color(  0, 255,  0),
						//                          new Color(  0, 255,  0),
						//                          new Color(  0, 210,  0),
						//                          new Color(  0, 180,  0),
						//                          new Color(  0, 150,  0),
						//                          new Color(  0, 115,  0),
						//                          new Color( 40,  70, 40),
						//                          new Color( 70,  70, 70),                          
						//                          new Color( 70,  40, 40),
						//                          new Color(115,   0,  0),
						//                          new Color(150,   0,  0),
						//                          new Color(180,   0,  0),
						//                          new Color(210,   0,  0),
						//                          new Color(255,   0,  0),
						//                          new Color(255,   0,  0)
			//                  };
		case NexradHeader.LEVEL2_VELOCITY:
			c = new Color[] {
					new Color(  0, 0, 0, 0),
					new Color(  0, 255,  0),
					new Color(  0, 255,  0),
					new Color(  0, 210,  0),
					new Color(  0, 180,  0),
					new Color(  0, 150,  0),
					new Color(  0, 115,  0),
					new Color( 40,  70, 40),
					new Color( 70,  70, 70),
					new Color( 70,  40, 40),
					new Color(115,   0,  0),
					new Color(150,   0,  0),
					new Color(180,   0,  0),
					new Color(210,   0,  0),
					new Color(255,   0,  0),
					new Color(255,   0,  0),
					new Color(119,   0, 125)
			};

			//                  c[0] =  new Color(  0, 0, 0, 0);
			//                  c[1] =  new Color(  0, 255,  0);
			//                  c[2] =  new Color(  0, 210,  0);
			//                  c[3] =  new Color(  0, 180,  0);
			//                  c[4] =  new Color(  0, 150,  0);
			//                  c[5] =  new Color(  0, 115,  0);
			//                  c[6] =  new Color( 40,  70, 40);
			//                  c[7] =  new Color( 70,  70, 70);
			//                  c[8] =  new Color( 70,  40, 40);
			//                  c[9] =  new Color(115,   0,  0);
			//                  c[10] = new Color(150,   0,  0);
			//                  c[11] = new Color(180,   0,  0);
			//                  c[12] = new Color(210,   0,  0);
			//                  c[13] = new Color(255,   0,  0);
			//                  c[14] = new Color(119,   0, 125);
			/*                  
                  c[0] =  new Color(  0, 0, 0, 0);
                  c[1] =  new Color(  0, 224, 255);
                  c[2] =  new Color(  0, 138, 255);
                  c[3] =  new Color( 50,   0, 150);
                  c[4] =  new Color(  0, 251, 144);
                  c[5] =  new Color(  0, 187,   0);
                  c[6] =  new Color(  0, 143,   0);
                  c[7] =  new Color(205, 192, 159);
                  c[8] =  new Color(118, 118, 118);
                  c[9] =  new Color(248, 135,   0);
                  c[10] = new Color(255, 207,   0);
                  c[11] = new Color(255, 255,   0);
                  c[12] = new Color(174,   0,   0);
                  c[13] = new Color(208, 122,   0);
                  c[14] = new Color(255,   0,   0);
                  c[15] = new Color(119,   0, 125);
			 */                  
			break;
		case NexradHeader.LEVEL2_SPECTRUMWIDTH:
			c = new Color[] {
					//                          new Color(  0, 0, 0, 0),
					new Color(  0, 0, 0, 0),
					new Color(118, 118, 118),
					new Color(156, 156, 156),
					new Color(  0, 187,   0),
					new Color(255,   0,   0),
					new Color(208, 112,   0),
					new Color(255, 255,   0),
					new Color(119,   0, 125)
			};
			break;


		case NexradHeader.XMRG:
		case NexradHeader.L3PC_DPA:
		case NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP:
			c = new Color[12];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(  0, 255,   0);
			c[2] =  new Color(127, 255,   0);
			c[3] =  new Color(255, 255,   0);
			c[4] =  new Color(255, 128,   0);
			c[5] =  new Color(255,   0,   0);
			c[6] =  new Color(178,  34,  34);
			c[7] =  new Color(219, 112, 147);
			c[8] =  new Color(218, 112, 214);
			c[9] =  new Color(131, 111, 238);
			c[10] = new Color(  0,   0, 255);
			c[11] = new Color(  0,   0, 139);
			break;
		case NexradHeader.L3PC_VELOCITY_32NM:
		case NexradHeader.L3PC_VELOCITY_124NM:
		case NexradHeader.L3PC_STORM_RELATIVE_VELOCITY_124NM:
			c = new Color[16];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(  0, 224, 255);
			c[2] =  new Color(  0, 138, 255);
			c[3] =  new Color( 50,   0, 150);
			c[4] =  new Color(  0, 251, 144);
			c[5] =  new Color(  0, 187,   0);
			c[6] =  new Color(  0, 143,   0);
			c[7] =  new Color(205, 192, 159);
			c[8] =  new Color(118, 118, 118);
			c[9] =  new Color(248, 135,   0);
			c[10] = new Color(255, 207,   0);
			c[11] = new Color(255, 255,   0);
			c[12] = new Color(174,   0,   0);
			c[13] = new Color(208, 122,   0);
			c[14] = new Color(255,   0,   0);
			c[15] = new Color(119,   0, 125);
			break;
		case NexradHeader.L3PC_SPECTRUM_WIDTH_32NM:
		case NexradHeader.L3PC_SPECTRUM_WIDTH_124NM:
			c = new Color[8];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(118, 118, 118);
			c[2] =  new Color(156, 156, 156);
			c[3] =  new Color(  0, 187,   0);
			c[4] =  new Color(255,   0,   0);
			c[5] =  new Color(208, 112,   0);
			c[6] =  new Color(255, 255,   0);
			c[7] =  new Color(119,   0, 125);
			break;
		case NexradHeader.L3PC_ONE_HOUR_PRECIP: 
		case NexradHeader.L3PC_THREE_HOUR_PRECIP: 
		case NexradHeader.L3PC_STORM_TOTAL_PRECIP: 
			c = new Color[16];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(170, 170, 170);
			c[2] =  new Color(118, 118, 118);
			c[3] =  new Color(  0, 255, 255);
			c[4] =  new Color(  0, 175, 175);
			c[5] =  new Color(  0, 255,   0);
			c[6] =  new Color(  0, 143,   0);
			c[7] =  new Color(255,   0, 255);
			c[8] =  new Color(175,  50, 125);
			c[9] =  new Color(  0,   0, 255);
			c[10] = new Color( 50,   0, 150);
			c[11] = new Color(255, 255,   0);
			c[12] = new Color(255, 170,   0);
			c[13] = new Color(255,   0,   0);
			c[14] = new Color(174,   0,   0);
			c[15] = new Color(255, 255, 255);
			break;
		case NexradHeader.L3PC_CLUTTER_FILTER_CONTROL:
			// version 0 (Legacy)
			if ((int)productVersion == 0) {
				c = new Color[8];
				c[0] =  new Color(  0, 0, 0, 0);
				//c[0] =  new Color(100, 100, 100);
				c[1] =  new Color(242, 226,   0);
				c[2] =  new Color(239, 149,   0);
				c[3] =  new Color(245,   0,  35);
				c[4] =  new Color(171,   0,   1);
				c[5] =  new Color(241, 136, 218);
				c[6] =  new Color(211,   0, 179);
				c[7] =  new Color(  2,  93, 206);
			}
			// version 1 (ORPG)
			else {
				/*
                     c = new Color[4];
                     c[0] =  new Color(  0, 0, 0, 0);
                     //c[0] =  new Color(100, 100, 100);
                     c[1] =  new Color(242, 226,   0);
                     c[2] =  new Color(171,   0,   1);
                     c[3] =  new Color(  2,  93, 206);
				 */
				 c = new Color[8];
				 c[0] =  new Color(  0, 0, 0, 0);
				 //c[0] =  new Color(100, 100, 100);
				 c[1] =  new Color(242, 226,   0);
				 c[2] =  new Color(  0, 0, 0, 0);
				 c[3] =  new Color(  0, 0, 0, 0);
				 c[4] =  new Color(171,   0,   1);
				 c[5] =  new Color(  0, 0, 0, 0);
				 c[6] =  new Color(  0, 0, 0, 0);
				 c[7] =  new Color(  2,  93, 206);
			}                     
			break;
		case NexradHeader.L3PC_COMPOSITE_REFLECTIVITY_248NM_8LVL: 
			c = new Color[8];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(  1, 160, 246);
			c[2] =  new Color(  0, 255,   0);
			c[3] =  new Color(  0, 144,   0);
			c[4] =  new Color(231, 192,   0);
			c[5] = new Color(255,   0,   0);
			c[6] = new Color(192,   0,   0);
			c[7] = new Color(153,  85, 201);
			break;
			// VAD Wind profile
		case NexradHeader.L3PC_VERTICAL_WIND_PROFILE: 
			c = new Color[6];
			c[0] =  new Color(  0,  0, 0, 0);
			c[1] =  new Color(  0, 100, 0);
			c[2] =  new Color(230, 176, 46);
			c[3] =  new Color(240,   0,   0);
			c[4] =  new Color(  0, 161, 230);
			c[5] =  new Color(220,   0,  99);
			break;
			// LOW / MID & HI LEVEL REFLECTIVITY
		case NexradHeader.L3PC_LOW_LAYER_COMP_REFLECTIVITY: 
		case NexradHeader.L3PC_MID_LAYER_COMP_REFLECTIVITY: 
		case NexradHeader.L3PC_HIGH_LAYER_COMP_REFLECTIVITY: 
			c = new Color[8];
			c[0] =  new Color(  0,  0, 0, 0);
			c[1] =  new Color(  0,  0, 246);
			c[2] =  new Color(  0, 200, 0);
			c[3] =  new Color(255, 255,   0);
			c[4] =  new Color(231, 192,   0);
			c[5] =  new Color(214,   0,   0);
			c[6] =  new Color(255,   0, 255);
			c[7] =  new Color(153,  85, 201);
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
			c = new Color[16];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(156, 156, 156);
			c[2] =  new Color(118, 118, 118);
			c[3] =  new Color(255, 170, 170);
			c[4] =  new Color(238, 140, 140);
			c[5] =  new Color(201, 112, 112);
			c[6] =  new Color(  0, 251, 144);
			c[7] =  new Color(  0,   187,   0);
			c[8] =  new Color(255, 255, 112);
			c[9] =  new Color(208, 208,  96);
			c[10] =  new Color(255,  96,  96);
			c[11] =  new Color(218,   0,   0);
			c[12] =  new Color(174,   0,   0);
			c[13] =  new Color(  0,   0, 255);
			c[14] =  new Color(255, 255, 255);
			c[15] =  new Color(231,   0, 255);
			break;
		case NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID: 
			c = new Color[17];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(120, 120, 120);
			c[2] =  new Color(  0, 236, 236);
			c[3] =  new Color(  1, 160, 246);
			c[4] =  new Color(  0,   0, 246);
			c[5] =  new Color(  0, 255,   0);
			c[6] =  new Color(  0, 200,   0);
			c[7] =  new Color(  0, 144,   0);
			c[8] =  new Color(255, 255,   0);
			c[9] =  new Color(231, 192,   0);
			c[10] =  new Color(255, 144,   0);
			c[11] = new Color(255,   0,   0);
			c[12] = new Color(214,   0,   0);
			c[13] = new Color(192,   0,   0);
			c[14] = new Color(255,   0, 255);
			c[15] = new Color(153,  85, 201);
			c[16] = new Color(235, 235, 235);
			break;
			// Echo Tops
		case NexradHeader.L3PC_ECHO_TOPS: 
		case NexradHeader.L3PC_ENHANCED_ECHO_TOPS: 
			c = new Color[16];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(156, 156, 156);
			c[2] =  new Color(118, 118, 118);
			c[3] =  new Color(  0, 224, 255);
			c[4] =  new Color(  0, 176, 255);
			c[5] =  new Color(  0, 144, 204);
			c[6] =  new Color( 50,   0, 150);
			c[7] =  new Color(  0,   251, 144);
			c[8] =  new Color(  0,   187,   0);
			c[9] =  new Color(  0,   239,   0);
			c[10] =  new Color(254, 191,  0);
			c[11] =  new Color(255, 255,   0);
			c[12] =  new Color(174,   0,   0);
			c[13] =  new Color(255,   0,   0);
			c[14] =  new Color(255, 255, 255);
			c[15] =  new Color(231,   0, 255);
			break;

			// Digital Hybrid Scan Reflectivity
		case NexradHeader.L3PC_DIGITAL_HYBRID_SCAN:
			// TDWR 8-bit reflectivities
		case NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT:
		case NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
		case NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT:
			c = new Color[21];
			c[0] =  new Color(  0,   0,   0,   0);

			c[1] =  new Color(50,   79,  79);
			c[2] =  new Color(102, 139, 139);
			c[3] =  new Color(150, 205, 205);
			c[4] =  new Color(174, 238, 238);
			c[5] =  new Color(187, 255, 255);

			c[6] =  new Color(  0, 236, 236);
			c[7] =  new Color(  1, 160, 246);
			c[8] =  new Color(  0,   0, 246);
			c[9] =  new Color(  0, 255,   0);
			c[10] =  new Color(  0, 200,   0);
			c[11] =  new Color(  0, 144,   0);
			c[12] =  new Color(255, 255,   0);
			c[13] =  new Color(231, 192,   0);
			c[14] =  new Color(255, 144,   0);
			c[15] = new Color(255,   0,   0);
			c[16] = new Color(214,   0,   0);
			c[17] = new Color(192,   0,   0);
			c[18] = new Color(255,   0, 255);
			c[19] = new Color(153,  85, 201);
			c[20] = new Color(235, 235, 235);
			break;

		case NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT:
		case NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT:
			c = new Color[18];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(  0, 255,  0);
			c[2] =  new Color(  0, 255,  0);
			c[3] =  new Color(  0, 255,  0);
			c[4] =  new Color(  0, 210,  0);
			c[5] =  new Color(  0, 180,  0);
			c[6] =  new Color(  0, 150,  0);
			c[7] =  new Color(  0, 115,  0);
			c[8] =  new Color( 40,  70, 40);
			c[9] =  new Color( 70,  70, 70);
			c[10] =  new Color( 70,  40, 40);
			c[11] =  new Color(115,   0,  0);
			c[12] = new Color(150,   0,  0);
			c[13] = new Color(180,   0,  0);
			c[14] = new Color(210,   0,  0);
			c[15] = new Color(255,   0,  0);
			c[16] = new Color(255,   0,  0);
			c[17] = new Color(255,   0,  0);
			//                   c[18] = new Color(119,   0, 125);
			break;

			// Default to BREF Color Table
		default: 
			c = new Color[16];
			c[0] =  new Color(  0, 0, 0, 0);
			c[1] =  new Color(  0, 236, 236);
			c[2] =  new Color(  1, 160, 246);
			c[3] =  new Color(  0,   0, 246);
			c[4] =  new Color(  0, 255,   0);
			c[5] =  new Color(  0, 200,   0);
			c[6] =  new Color(  0, 144,   0);
			c[7] =  new Color(255, 255,   0);
			c[8] =  new Color(231, 192,   0);
			c[9] =  new Color(255, 144,   0);
			c[10] = new Color(255,   0,   0);
			c[11] = new Color(214,   0,   0);
			c[12] = new Color(192,   0,   0);
			c[13] = new Color(255,   0, 255);
			c[14] = new Color(153,  85, 201);
			c[15] = new Color(235, 235, 235);
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

		//System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVV GET COLORS: classify? "+classify+" size: "+c.length);

		return c;

	} // END static getColors(productCode)     


	public static Color[] getTransparentColors(int productCode, boolean classify, int alphaChannelValue) {

		Color[] c = getColors(productCode, classify);  
		for (int i=1; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), alphaChannelValue);
			}
		}      

		//      System.out.println("trans. colors: "+Arrays.toString(c));

		return c;
	}
} // END class
