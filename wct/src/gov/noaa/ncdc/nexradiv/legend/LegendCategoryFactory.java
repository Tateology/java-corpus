package gov.noaa.ncdc.nexradiv.legend;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeQ2;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;

public class LegendCategoryFactory {

    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    private static String[] dpaLabel = // must have same size as color array for dpa 
    {"< 0.10", "0.10 - 0.25", "0.25 - 0.50", "0.50 - 0.75",
        "0.75 - 1.00", "1.00 - 1.50", "1.50 - 2.00", "2.00 - 2.50",
        "2.50 - 3.00", "3.00 - 4.00", "> 4.00"};

    
    private static String[] getCategoryStrings(String palFile, int levels) throws MalformedURLException, IOException, WCTException {

		URL url = ResourceUtils.getInstance().getJarResource(
            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
            "/config/colormaps/"+palFile, null);
	
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();
		ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, levels);
   		
//		System.out.println(rawCav);
//   		System.out.println(cav);
   		
		return WCTUtils.flipArray(cav.getLabels(WCTUtils.DECFMT_0D00));
    }
    
    
    public static String[] getCategoryStrings(NexradHeader header, boolean classify) {


    	try {

    		if (header.getProductType() == NexradHeader.LEVEL2) {
    			if (header.getProductCode() == NexradHeader.LEVEL2_VELOCITY) {

    				return WCTUtils.flipArray(new String[]{
    						"< - 60", "- 60", "- 50", " - 40", "- 30", "- 20", "- 10",
    						" 0", "+ 10", "+ 20", "+ 30", "+ 40", "+ 50", "+ 60", "> + 60" });

    			}
    			else if (header.getProductCode() == NexradHeader.LEVEL2_SPECTRUMWIDTH) {
    				return WCTUtils.flipArray(new String[]{
    						"0", "6", "12", "18", "24", "30"
    				});
    			}
    			else if (header.getProductCode() == NexradHeader.LEVEL2_REFLECTIVITY) {
    				return WCTUtils.flipArray(new String[]{
    						"<= - 20", "- 15", "- 10", "- 5", "  0", "+ 5", "+ 10", "+ 15",
    						"+ 20", "+ 25", "+ 30", "+ 35", "+ 40", "+ 45", "+ 50", "+ 55", "+ 60",
    						"+ 65", "+ 70", ">= + 75"});                
    			}
    			else if (header.getProductCode() == NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY) {
    				return getCategoryStrings("nexrad_dp_zdr.pal", 19);
    			}
    			else if (header.getProductCode() == NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT) {
    				return getCategoryStrings("nexrad_dp_rho.pal", 19);
    			}
    			else if (header.getProductCode() == NexradHeader.LEVEL2_DIFFERENTIALPHASE) {
    				return getCategoryStrings("nexrad_dp_phi.pal", 19);
    			}
    			else {
    				double minValue = ((DecodeRadialDatasetSweepHeader)header).getMinValue();
    				double maxValue = ((DecodeRadialDatasetSweepHeader)header).getMaxValue();

    				String[] labels = new String[6];
    				for (int n=0; n<labels.length; n++) {
    					labels[n] = fmt2.format(minValue + n*(maxValue-minValue)/labels.length);
    				}
    				return WCTUtils.flipArray(labels);
    			}
    		}


    		if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY) {
    			return getCategoryStrings("nexrad_dp_zdr.pal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT) {
    			return getCategoryStrings("nexrad_dp_rho.pal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE) {
    			return getCategoryStrings("nexrad_l3_dkd.wctpal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION) {
    			return getCategoryStrings("nexrad_l3_oha_daa.wctpal", 21);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION) {
    			return getCategoryStrings("nexrad_l3_sta_dsa.wctpal", 81);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE ||
    				header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE) {
    			return getCategoryStrings("nexrad_l3_ohd_dsd.wctpal", 21);
    		}

    		Color[] colors = NexradColorFactory.getColors(header.getProductCode(), classify, header.getVersion());
//    		System.out.println(Arrays.toString(colors));
    		String[] labels = new String[colors.length-1];
    		for (int n=1; n<colors.length; n++) {

    			if (header.getProductType() == NexradHeader.L3DPA || header.getProductType() == NexradHeader.XMRG) {
    				try {
    					labels[n-1] = dpaLabel[colors.length-n-1] + "  ("+(colors.length-n)+")"; // flip so low values are on the bottom
    				} catch (Exception e) {
    					labels[n-1] = "("+(colors.length-n)+")";
    				}
    			}
    			else if (header.getProductType() == NexradHeader.L3RADIAL_8BIT) {
    				if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_SCAN ||
    						header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT || 
    						header.getProductCode() == NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT || 
    						header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT) {

    					labels = WCTUtils.flipArray(new String[]{
    							"<= - 20", "- 15", "- 10", "- 5", "  0", "+ 5", "+ 10", "+ 15",
    							"+ 20", "+ 25", "+ 30", "+ 35", "+ 40", "+ 45", "+ 50", "+ 55", "+ 60",
    							"+ 65", "+ 70", ">= + 75"});
    				}
    				else if (header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT ||
    						header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT) {

    					labels = WCTUtils.flipArray(new String[]{
    							"<= - 80", "- 70", "- 60", "- 50", " - 40", "- 30", "- 20", "- 10",
    							" 0", "+ 10", "+ 20", "+ 30", "+ 40", "+ 50", "+ 60", "+ 70", ">= + 80" });
    				}
    				else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP) {

    					labels = WCTUtils.flipArray(new String[]{
    							"0.0", "0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", ">= 5.0" });
    				}
    				else if (header.getProductCode() == NexradHeader.L3PC_ENHANCED_ECHO_TOPS) {
    					labels = WCTUtils.flipArray(new String[]{
    							" 5", " 10", " 15", " 20", " 25", " 30", " 35", " 40", " 45", " 50", " 55", " 60",
    							" 65", " 70", ">= 75"});
    				}
    				else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID) {
    					labels = WCTUtils.flipArray(new String[]{
    							"0.1", " 5", " 10", " 15", " 20", " 25", " 30", " 35", " 40", " 45", " 50", " 55", " 60",
    							" 65", " 70", "> 70"});
    				}
    				else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION ||
    						header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION) {
    					labels = WCTUtils.flipArray(new String[]{
    							"(1) Biological", "(2) AP/Gr. Clutter", "(3) Ice Crystals", "(4) Dry Snow", "(5) Wet Snow",
    							"(6) Lt./Mod. Rain", "(7) Heavy Rain", "(8) Big Drops Rain", "(9) Graupel", "(10) Hail w/ Rain", 
    							"TBD", "TBD", "TBD", "(14) Unknown", "(15) RF" });
    				}
    			}
    			else {
    				if (header.getDataThresholdString(colors.length - n).trim().length() == 0) {
    					labels[n-1] = null;
    				}
    				else {
    					labels[n-1] = header.getDataThresholdString(colors.length - n) + "  ("+(colors.length-n)+") ";  // flip so low values are on the bottom
    				}
    			}
    		}

    		return labels;

    	} catch (Exception e) {
    		e.printStackTrace();
    		return new String[] {"X"};
    	}
        
    }
    
    
    private static Color[] getCategoryColors(String palFile, int levels) throws MalformedURLException, IOException, WCTException {

		URL url = ResourceUtils.getInstance().getJarResource(
				new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
				"/config/colormaps/"+palFile, null);

		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		ColorsAndValues rawCav = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();
		ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, levels);
		return WCTUtils.flipArray(cav.getColors());
    }
    
    public static Color[] getCategoryColors(NexradHeader header, boolean classify) {

        Color[] colors = NexradColorFactory.getColors(header.getProductCode(), classify, header.getVersion());

        Color[] catColors;

    	try {

    		if (header.getProductCode() == NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY) {
    			return getCategoryColors("nexrad_dp_zdr.wctpal", 19);
    		}        
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY) {
    			return getCategoryColors("nexrad_dp_zdr.wctpal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT) {
    			return getCategoryColors("nexrad_dp_rho.wctpal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT) {
    			return getCategoryColors("nexrad_dp_rho.wctpal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.LEVEL2_DIFFERENTIALPHASE) {
    			return getCategoryColors("nexrad_dp_phi.pal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE) {
    			return getCategoryColors("nexrad_l3_dkd.wctpal", 19);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION) {
    			return getCategoryColors("nexrad_l3_oha_daa.wctpal", 21);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION) {
    			return getCategoryColors("nexrad_l3_sta_dsa.wctpal", 81);
    		}
    		else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE ||
    				header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE) {
    			return getCategoryColors("nexrad_l3_ohd_dsd.wctpal", 21);
    		}
    		
    		else if (header.getProductCode() == NexradHeader.LEVEL2_VELOCITY || 
    				header.getProductCode() == NexradHeader.LEVEL2_SPECTRUMWIDTH) {

    			catColors = new Color[colors.length-2];
    			for (int n=1; n<colors.length-1; n++) {
    				catColors[n-1] = colors[n];
    			}
    		}
    		else {
    			catColors = new Color[colors.length-1];
    			for (int n=1; n<colors.length; n++) {
    				catColors[n-1] = colors[n];
    			}
    		}

        
    	} catch (Exception e) {
    		e.printStackTrace();
    		return new Color[] {Color.WHITE};
    	}


        WCTUtils.flipArray(catColors);

        return catColors;
    }
    
    
    
    
    
    
    public static String[] getCategoryLabels(DecodeQ2 q2Decoder) {
        String units = q2Decoder.getLastDecodedUnits();
        
        if (units.trim().equalsIgnoreCase("dBZ")) {
            String[] ref3Dvals = new String[]{"0", "5", "10", "15", "20", "25", "30", "35", 
                "40", "45", "50", "55", "60", "65", "70", "75"};
            
            WCTUtils.flipArray(ref3Dvals);
            return ref3Dvals;
        }
        else {
            DecimalFormat fmt = new DecimalFormat("0.00");
            double minVal = q2Decoder.getMinVal();
            double maxVal = q2Decoder.getMaxVal();            
            Color[] catColors = NexradColorFactory.getColors(NexradHeader.UNKNOWN, false);
//            Color[] catColors = getCategoryColors(q2Decoder);
            
            String[] labels = new String[catColors.length-1];
            Color emptyColor = new Color(  0, 0, 0, 0);
            int count = 0;
            for (int i=1; i<catColors.length; i++) {
               // Don't include empty colors in Legend
               if (! catColors[catColors.length-i].equals(emptyColor)) {
                  // flip so low values are on the bottom
                  labels[i-1] = fmt.format(((maxVal-minVal)/catColors.length*(catColors.length-i)));  
                  
               }
            }
            return labels;
        }

        
    }
    
    
    
    
    public static Color[] getCategoryColors(DecodeQ2 q2Decoder) {
        String units = q2Decoder.getLastDecodedUnits();
        if (units.trim().equalsIgnoreCase("dBZ")) {
            Color[] colors =  NexradColorFactory.getColors(NexradHeader.Q2_NATL_MOSAIC_3DREFL, false);
            Color[] catColors = new Color[colors.length-2];
            for (int n=2; n<colors.length; n++) {
                catColors[n-2] = colors[n];
            }
            
            WCTUtils.flipArray(catColors);
            return catColors;
        }
        else {
            Color[] colors =  NexradColorFactory.getColors(NexradHeader.UNKNOWN, false);
            
//            colors = expandColorCategories(colors, 2, 1);
            
            Color[] catColors = new Color[colors.length-1];
            for (int n=1; n<colors.length; n++) {
                catColors[n-1] = colors[n];
            }
            
            WCTUtils.flipArray(catColors);
            return catColors;
        }
    }
    

    public static String[] getCategoryLabels(GoesRemappedRaster raster) throws Exception {
        GoesColorFactory gcf = GoesColorFactory.getInstance();
//        gcf.calculateEqualColorsAndValues(raster);
//        Double[] values = gcf.getEqualColorsAndValues().getValues();
        Double[] values = gcf.getColorsAndValues(raster).getValues();
//        System.out.println("LCF: "+Arrays.deepToString(values));
        String[] labels = new String[values.length];
        
        DecimalFormat fmt0 = new DecimalFormat("0");
        for (int n=0; n<values.length; n++) {
            labels[n] = fmt0.format(values[n]);
        }
        
        return labels;
    }
    
    public static Color[] getCategoryColors(GoesRemappedRaster raster) throws Exception {
        GoesColorFactory gcf = GoesColorFactory.getInstance();
//        gcf.calculateEqualColorsAndValues(raster);
//        return gcf.getEqualColorsAndValues().getColors();          
        return gcf.getColorsAndValues(raster).getColors();
    }

    
    
    
    
    
    
    
    
    
    public static String getLegendTitle(NexradHeader header, boolean classify) {
        String units = NexradUtilities.getUnits(header.getProductCode());
        if (header.getProductType() == NexradHeader.L3RADIAL || 
                header.getProductType() == NexradHeader.L3RASTER || 
                header.getProductType() == NexradHeader.L3DPA ||
                classify) {
            
        
            return "Legend: "+units+" (Category)";
        }
        else {
            return "Legend: "+units;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public static Color[] getCategoryColors(GridDatasetRemappedRaster remappedRaster) {
        Color[] colors = remappedRaster.getDisplayColors();
        return colors;
    }
    
    public static String[] getCategoryLabels(GridDatasetRemappedRaster remappedRaster) {
        DecimalFormat fmt = new DecimalFormat("0.00");
        double minVal = remappedRaster.getDisplayMinValue();
        double maxVal = remappedRaster.getDisplayMaxValue();            
        Color[] catColors = remappedRaster.getDisplayColors();
        
        String[] labels = new String[catColors.length];
        for (int i=0; i<catColors.length; i++) {
            labels[i] = fmt.format(minVal+((maxVal-minVal)/catColors.length*(catColors.length-i)));  
        }
        return labels;
        
    }
    
    public static Double[] getCategoryValues(GridDatasetRemappedRaster remappedRaster) {
        double minVal = remappedRaster.getGridMinValue();
        double maxVal = remappedRaster.getGridMaxValue();       
        if (! remappedRaster.isAutoMinMaxValues()) {
            minVal = remappedRaster.getDisplayMinValue();
            maxVal = remappedRaster.getDisplayMaxValue();
        }
       
        Color[] catColors = remappedRaster.getDisplayColors();
        Double[] values = new Double[catColors.length];
        for (int i=0; i<catColors.length; i++) {
            values[i] = minVal+(((maxVal-minVal)/(catColors.length-1))*(catColors.length-1-i));  
        }
        return values;
        
    }
}
