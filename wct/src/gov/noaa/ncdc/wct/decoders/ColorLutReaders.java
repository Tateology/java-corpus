package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.util.NumberRange;

public class ColorLutReaders {

	/**
	 * Reads color and value map information from 'cmap' color scale lookup tables.
	 * Values must be in descending order!
	 * @param br
	 * @return
	 * @throws IOException
	 */
	public static ColorsAndValues parseCmapFormat(BufferedReader br) throws IOException {
		
        ArrayList<Color> colorList = new ArrayList<Color>();
        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<String> labelList = new ArrayList<String>();
        String str;
        while ((str=br.readLine()) != null) {

            if (str.trim().length() == 0) {
                continue;
            }

            // 0         1         2         3         4
            // 012345678901234567890123456789012345678901234567890
            //       255     255     255     255   163.0
            int r = Integer.parseInt(str.substring(0, 12).trim());
            int g = Integer.parseInt(str.substring(13, 20).trim());
            int b = Integer.parseInt(str.substring(21, 28).trim());
            int a = Integer.parseInt(str.substring(29, 34).trim());
            double val = Double.parseDouble(str.substring(35, str.length()).trim());

            colorList.add(new Color(r, g, b, a));
            valueList.add(new Double(val));                
           	labelList.add(str.substring(35, str.length()).trim());
        }

        return new ColorsAndValues(
        		(Color[]) colorList.toArray(new Color[colorList.size()]), 
        		(Double[]) valueList.toArray(new Double[valueList.size()]),
        		(String[]) labelList.toArray(new String[labelList.size()])
        	);

	}
	
	
	/**
	 * Reads a GR2A (Gibson Ridge Level-II Analyst) .pal file.
	 * Values must be in descending order!
	 * @param br
	 * @return
	 * @throws IOException
	 */
	public static ColorsAndValues parseGR2A(BufferedReader br) throws IOException {

        ArrayList<Color> colorList = new ArrayList<Color>();
        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<String> labelList = new ArrayList<String>();
        String str;
        double scale = 1;
        while ((str=br.readLine()) != null) {

            if (str.startsWith("Scale:")) {
                scale = Double.parseDouble(str.split("[\\s]+")[1]);
            }

        	
            if (str.trim().length() == 0 || ! str.startsWith("Color:")) {
                continue;
            }

            // 0         1         2         3         4
            // 012345678901234567890123456789012345678901234567890
            // Color: 180.0   255  140  255
            // Color:  100  255  180  215
            String[] cols = str.split("[\\s]+");
//            System.out.println( Arrays.toString(cols) );

            int r = Integer.parseInt(cols[2]);
            int g = Integer.parseInt(cols[3]);
            int b = Integer.parseInt(cols[4]);
            int a = 255;
            double val = Double.parseDouble(cols[1])/scale;
//
            colorList.add(new Color(r, g, b, a));
            valueList.add(new Double(val));                
            if (scale == 1) {
            	labelList.add(cols[1]);
            }
            else {
            	labelList.add(WCTUtils.DECFMT_pDpppp.format(val));
            }
        }

        return new ColorsAndValues(
        		(Color[]) colorList.toArray(new Color[colorList.size()]), 
        		(Double[]) valueList.toArray(new Double[valueList.size()]),
        		(String[]) labelList.toArray(new String[labelList.size()])
        	);

	}
	
	
	

	
	
	
	
	
	
	
	
	/**
	 * Reads a WCT (Weather and Climate Toolkit) .wctpal file.
	 * Values must be in descending order!
	 * @param br
	 * @return
	 * @throws IOException
	 */
	public static ColorsAndValues[] parseWCTPal(BufferedReader br) throws IOException {

        ArrayList<Color> colorList = new ArrayList<Color>();
        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<String> labelList = new ArrayList<String>();
        
        ArrayList<Color> uniqueQualColorList = new ArrayList<Color>();
        ArrayList<Double> uniqueQualValueList = new ArrayList<Double>();
        ArrayList<String> uniqueQualLabelList = new ArrayList<String>();
        
        String str;
        double scale = 1;
        while ((str=br.readLine()) != null) {        	

            if (str.startsWith("Scale:")) {
                scale = Double.parseDouble(str.split("[\\s]+")[1]);
            }

            if (str.trim().length() == 0 || 
            		! (str.startsWith("Color:") || str.startsWith("Unique:"))) {
                continue;
            }

            // col[0] col[1]  [col2] [col3] [col4] [col5]
            // Color: 180.0   255    140    255    255   ( R G B A )
            // Color: 100     255    180    215          (R G B)
            // or --
            // Color: 100.0   #ab2eff  (6 digit RGB hex)
            // -- or --
            // Color: 100.0   #ab2ef10 (8 digit RGBA hex)
            
            
           // col[0]  col[1] [col2]      [col3][col4][col5][col6]            
//            Unique: -200   RF           119   0    125
//			  Unique: 1     "Bio Return"  220   220  220    255
//			  Unique: 1     "Bio Return"  #ab2eff  (6 digit RGB hex)
//			  Unique: 1     "Bio Return"  #ab2ef10 (8 digit RGBA hex)
            if (str.startsWith("Unique:")) {

            	String[] cols = splitBySpaceWithQuotes(str);
//              System.out.println( Arrays.toString(cols) );

                int r, g, b;
                int a = 255;
                if (cols.length == 4) {
                	int[] rgba = parseHexColors(cols[3]);
                	r = rgba[0];
                	g = rgba[1];
                	b = rgba[2];
                	a = rgba[3];
                }
                else {
                	r = Integer.parseInt(cols[3]);
                	g = Integer.parseInt(cols[4]);
                	b = Integer.parseInt(cols[5]);
                	if (cols.length > 6) {
                		a = Integer.parseInt(cols[6]);
                	}
                }            	
            	
            	double val = Double.NaN;
            	if (! cols[1].equalsIgnoreCase("NA")) {
            		val = Double.parseDouble(cols[1]);
            	}
            	//
            	uniqueQualColorList.add(new Color(r, g, b, a));
            	uniqueQualValueList.add(new Double(val));        
           		uniqueQualLabelList.add(cols[2].replaceAll("\"", ""));

            }
            else {

            	String[] cols = str.split("[\\s]+");
//              System.out.println( Arrays.toString(cols) );


                int r, g, b;
                int a = 255;
                if (cols.length == 3) {
                	int[] rgba = parseHexColors(cols[2]);
                	r = rgba[0];
                	g = rgba[1];
                	b = rgba[2];
                	a = rgba[3];
                }
                else {
                	r = Integer.parseInt(cols[2]);
                	g = Integer.parseInt(cols[3]);
                	b = Integer.parseInt(cols[4]);
                	if (cols.length > 5) {
                		a = Integer.parseInt(cols[5]);
                	}
                }
            	
            	double val = Double.NaN;
            	if (! cols[1].equalsIgnoreCase("NA")) {
            		val = Double.parseDouble(cols[1])/scale;
            	}
            	//
            	colorList.add(new Color(r, g, b, a));
            	valueList.add(new Double(val));        
            	if (scale == 1) {
            		labelList.add(cols[1]);
            	}
            	else {
            		labelList.add(WCTUtils.DECFMT_pDpppp.format(val));
            	}
            }

        }

        // return array, element 0 = normal color,value range categories,
        // element 1 = unique color at specific value catories
        ColorsAndValues[] cavArray = new ColorsAndValues[2];
        cavArray[0] = new ColorsAndValues(
        		(Color[]) colorList.toArray(new Color[colorList.size()]), 
        		(Double[]) valueList.toArray(new Double[valueList.size()]),
        		(String[]) labelList.toArray(new String[labelList.size()])
        	);
        cavArray[1] = new ColorsAndValues(
        		(Color[]) uniqueQualColorList.toArray(new Color[uniqueQualColorList.size()]), 
        		(Double[]) uniqueQualValueList.toArray(new Double[uniqueQualValueList.size()]),
        		(String[]) uniqueQualLabelList.toArray(new String[uniqueQualLabelList.size()])
        	);
         
        return cavArray; 

	}
	

	
	

	
	public static SampleDimensionAndLabels convertToSampleDimensionAndLabels(ColorsAndValues cav) throws WCTException {
		return convertToSampleDimensionAndLabels(cav, null);
	}
	
	public static SampleDimensionAndLabels convertToSampleDimensionAndLabels(
			ColorsAndValues cav, ColorsAndValues uniqueQualColorsAndValues) throws WCTException {
		
		Double[] values = cav.getValues();
		Color[] colors = cav.getColors();
		
		
		ArrayList<Category> catList = new ArrayList<Category>();
		ArrayList<String> labelList = new ArrayList<String>();

		if (cav.getColors().length > 1) {

			int diffSign = (values[1]-values[0] > 0) ? -1 : 1;
			int numQualCats = uniqueQualColorsAndValues.getColors().length+1;
			for (int n=0; n<values.length-1; n++) {

				//			System.out.println(values[n]+"  "+values[n+1]);
				if (String.valueOf(values[n]).equals(String.valueOf(values[n+1]))) {
//					System.out.println(values[n]+" and "+values[n+1]+" are equal -- skipping...");
					n++;
				}

				// start with 1 because 0=NoData
				// value ranges are assigned to sections of the 0-255 indexed image (256 possible colors).
				NumberRange indexedRange = new NumberRange(
						(int)( (n*((255.0-numQualCats)/(values.length-1)))+1 ), 
						(int)( (n+1)*((255.0-numQualCats)/(values.length-1)) )
						);

				NumberRange valueRange = new NumberRange(values[n], values[n+1]+(diffSign*0.000001));


//				System.out.println("  n="+n+" values.length="+values.length+" numQualCats: "+numQualCats+"  "+indexedRange+"   "+valueRange);

				catList.add( new Category(cav.getLabels()[n]+":"+cav.getLabels()[n+1], 
						new Color[] { colors[n], colors[n+1] },
						indexedRange, valueRange).geophysics(true) );

				labelList.add(cav.getLabels()[n]);



				//			System.out.println("added cat("+n+") : "+catList.get(n)+"   label: "+labelList.get(n));

			}
			// add last label
			labelList.add(cav.getLabels()[values.length-1]);

		}
		
		/*
		 
		                                                                                                                                                                                                                                                                                                                                                                            
Unique: 15 sdf   #77007D
Unique: 14 asdf   #E700FF
# 13-11 are TBD
Unique: 13 asdf   #FFFFFF
Unique: 12 asdf   #FFFFDF
Unique: 11 asdf   #FFFFBF
Unique: 10 asdf   #FF0000
Unique: 9 asdf  #D28484
Unique: 8 asdf  #D0D060
Unique: 7 asdf  #00BB00
Unique: 6 asdf  #00FB90
Unique: 5 asdf  #0090FF
Unique: 4 asdf  #00FFFF
Unique: 3 "Hard Rain"  #FFB0B0
Unique: 2 AP  #767676
Unique: 1 Bio  #9C9C9C
#  leave out zero, so it is not in the legend
Unique: 0:ND  #000000


		 */
		

		
		Double[] uniqueValues = uniqueQualColorsAndValues.getValues();
		Color[] uniqueColors = uniqueQualColorsAndValues.getColors();
		String[] uniqueLabels = uniqueQualColorsAndValues.getLabels();
		
		for (int n=0; n<uniqueValues.length; n++) {
			
			catList.add( new Category("Unique:"+uniqueLabels[n], 
					new Color[] { uniqueColors[n], uniqueColors[n] },
					new NumberRange(255-(n*2)-1, 255-(n*2)), 
					new NumberRange(uniqueValues[n]-0.000001, uniqueValues[n]+0.000001)).geophysics(true) );
			
			labelList.add(uniqueLabels[n]);

		}
		
		
		
//		catList.add(new Category("RF", new Color(117, 0, 125), 255).geophysics(true));
//		labelList.add("RF");

		// add no data category and label
		catList.add(Category.NODATA.geophysics(true));
		labelList.add("No Data");

		
		SampleDimension sd = new SampleDimension(catList.toArray(new Category[catList.size()]), null);
		
		return new SampleDimensionAndLabels(sd, labelList.toArray(new String[labelList.size()]));
	}
	
	
	
	
	
	
	
	
	
	
	
	public static int[] parseHexColors(String hexString) throws NumberFormatException {
		int r,g,b;
		int a = 255;
    	// process hex colors
    	if (hexString.length() == 7) {
    		r = Integer.parseInt(hexString.substring(1, 3), 16);
    		g = Integer.parseInt(hexString.substring(3, 5), 16);
    		b = Integer.parseInt(hexString.substring(5, 7), 16);
    	}
    	else if (hexString.length() == 9) {
    		r = Integer.parseInt(hexString.substring(1, 3), 16);
    		g = Integer.parseInt(hexString.substring(3, 5), 16);
    		b = Integer.parseInt(hexString.substring(5, 7), 16);
    		a = Integer.parseInt(hexString.substring(7), 16);
    	}
    	else {
    		throw new NumberFormatException("attempting to parse 6 or 8 digit hex string, but string " +
    				"does not appear to be valid: "+hexString);
    	}
    	return new int[] { r, g, b, a };
	}
	
	
	
	public static String[] splitBySpaceWithQuotes(String text) {
//	   String text = "1 2 \"333 4\" 55 6    \"77\" 8 999";
	    // 1 2 "333 4" 55 6    "77" 8 999

	    String regex = "\"([^\"]*)\"|(\\S+)";

	    Matcher m = Pattern.compile(regex).matcher(text);
	    ArrayList<String> resultList = new ArrayList<String>();
	    while (m.find()) {
	        if (m.group(1) != null) {
//	            System.out.println("Quoted [" + m.group(1) + "]");
	        	resultList.add(m.group(1));
	        } else {
//	            System.out.println("Plain [" + m.group(2) + "]");
	        	resultList.add(m.group(2));
	        }
	    }
	    return resultList.toArray(new String[resultList.size()]);
	}

	
}
