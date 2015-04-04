package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.common.color.SimpleColorMap;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.util.NumberRange;


public class ColorsAndValues {

    private Color[] colors;
    private Double[] values;
    private String[] labels;
    


    public ColorsAndValues(Color[] colors, Double[] values) {
        this.colors = colors;
        this.values = values;
        labels = getLabels(WCTUtils.DECFMT_pDpppp);
    }

    public ColorsAndValues(Color[] colors, Double[] values, String[] labels) {
        this.colors = colors;
        this.values = values;
        this.labels = labels;
    }

    public Color[] getColors() {
        return colors;
    }

    public Double[] getValues() {
        return values;
    }
    
    public String[] getLabels() {
    	return labels;
    }
    
    public void flip() {
    	WCTUtils.flipArray(colors);
    	WCTUtils.flipArray(values);
    }

    public String[] getLabels(DecimalFormat fmt) {
        
        boolean isAllSmall = true;
        for (double val : values) {
            if (val >= 0.01) {
                isAllSmall = false;
            }
        }
        
        String[] labels = new String[values.length];
        for (int n=0; n<labels.length; n++) {
//            try {
            labels[n] = fmt.format(values[n].doubleValue());
//            } catch (Exception e) {
//                labels[n] = "";
//            }
            
            if (Math.abs(values[n].doubleValue()) > 10000 || 
                    ( Math.abs(values[n].doubleValue()) > 0 && Math.abs(values[n].doubleValue()) < 0.01) && isAllSmall) {
                labels[n] = WCTUtils.DECFMT_SCI.format(values[n].doubleValue());
            }
            
        }
        return labels;
    }
    

    public static ColorsAndValues calculateEqualColorsAndValues(ColorsAndValues colorsAndVals) throws WCTException  {
        return calculateEqualColorsAndValues(colorsAndVals, 4*36);
    }
    
    public static ColorsAndValues calculateEqualColorsAndValues(ColorsAndValues colorsAndVals, int categories) throws WCTException  {

        Color[] c = colorsAndVals.getColors();
        Double[] vals = colorsAndVals.getValues();        

        if (vals[0] > vals[1]) {
        	WCTUtils.flipArray(c); 
        	WCTUtils.flipArray(vals);
        }
        
                        
        if (c.length != vals.length) {
            throw new WCTException(c.length+" colors and "+vals.length+" values.  These must be equal!");
        }
//        System.out.println(c.length + " ::: " + vals.length);
//        for (int n=0; n<c.length; n++) {
//            System.out.println(c[n]+" ::: "+vals[n]);
//        }
        
        Color[] equalColors = new Color[categories];
        Double[] equalVals = new Double[categories];

        ArrayList<SimpleColorMap> mapList = new ArrayList<SimpleColorMap>();
        
//        System.out.println("0 -- "+vals[0]+","+vals[1]);
        mapList.add(new SimpleColorMap(vals[0]-0.0000001, vals[1], c[0], c[1]));
        for (int n=1; n<c.length-2; n++) {
//            System.out.println(n+" -- "+vals[n]+","+vals[n+1]);
            mapList.add(new SimpleColorMap(vals[n], vals[n+1], c[n], c[n+1]));
//            mapList.add(new SimpleColorMap(Math. log(vals[n]), Math.log(vals[n+1]), c[n], c[n+1]));
        }
//        System.out.println((c.length-2)+" -- "+vals[c.length-2]+","+vals[c.length-1]);
        mapList.add(new SimpleColorMap(vals[c.length-2], vals[c.length-1]+0.0000001, c[c.length-2], c[c.length-1]));

        double minVal = vals[0];
        double maxVal = vals[vals.length-1];
        double interval = (maxVal - minVal)/(categories-1);

        
        for (int n=0; n<categories; n++) {
            double val = minVal+n*interval;
//            val = Math.round(val*10000)/10000.0;

            boolean foundValue = false;

            for (int i=0; i<mapList.size(); i++) {
//                System.out.println(val + " _ " + mapList.get(i).minLevel + " _ " + mapList.get(i).maxLevel);
                if (val >= mapList.get(i).minLevel && val <= mapList.get(i).maxLevel) {
                    equalColors[n] = mapList.get(i).getColor(val);
                    equalVals[n] = val;
//                    System.out.println(n+") "+equalColors[n]+" ___ " + equalVals[n]);
                    foundValue = true;
                    break;
                }
//                else {
//                    System.out.println("no color map matched: val="+val);
//                }
            }
            
            
            
            if (! foundValue) {
                System.out.println("no color map matched: val="+val);
            }

        }
        


//      System.out.println("end color/val calc");

        return new ColorsAndValues(equalColors, equalVals);

    }
    
    
    
    public static Color getColor(Color[] colorMap, double minValue, double maxValue, double valueToQuery) {
    	
    	if (colorMap.length == 2) {
    		SimpleColorMap scm = new SimpleColorMap(minValue, maxValue, colorMap[0], colorMap[1]);
    		return scm.getColor(valueToQuery);
    	}
    	
    	
        double interval = (maxValue - minValue)/(colorMap.length);
        double range = maxValue - minValue;
        double relativePercentFromMin = (valueToQuery-minValue)/range;
        int colorIndexLow = (int)(relativePercentFromMin * colorMap.length)-1;
        int colorIndexHigh = colorIndexLow + 1;
        double colorMapRangeMin = minValue+(interval*colorIndexLow);
        double colorMapRangeMax = minValue+(interval*colorIndexHigh);
        
        SimpleColorMap scm = new SimpleColorMap(colorMapRangeMin, colorMapRangeMax, colorMap[colorIndexLow], colorMap[colorIndexHigh]);
        
        return scm.getColor(valueToQuery);
        
        
    }
    
    public static Color getColor(SampleDimension sd, float valueToQuery) {
    	
    	List<Category> catList = sd.getCategories();
    	for (Category cat : catList) {
    		
    		System.out.println("checking range of "+cat.getRange().getMinimum(true)+
    				" : "+cat.getRange().getMaximum(true)+" for isoval="+valueToQuery);
    		
    		double min = cat.getRange().getMinimum(true);
    		double max = cat.getRange().getMaximum(true);
    		boolean flip = (min > max);
    		
    		float minVal = (float)Math.min(min, max);
    		float maxVal = (float)Math.max(min, max);
    		
    		System.out.println("second check: min="+minVal+" max="+maxVal+"  valueToQuery="+valueToQuery);
    		if (valueToQuery >= minVal && valueToQuery <= maxVal) {
    			
    			Color[] colors = cat.getColors();
    			if (flip) {
    				colors = WCTUtils.flipArray(colors);
    			}
    			
    			return getColor(colors, minVal, maxVal, valueToQuery);
    		}
    	}
    	
    	// nothing found
    	return null;
    }
    
    
    
    public SampleDimension getSampleDimension() {
    	
    	flip();
    	
    	Category[] cats = new Category[colors.length-1];
    	for (int n=0; n<cats.length; n++) {
    		cats[n] = new Category("cat "+n, new Color[] { colors[n], colors[n+1] }, 
//    		cats[n] = new Category("cat "+n, new Color[] { colors[n] }, 
    				new NumberRange(Double.class, (double)n, n+0.99999999), new NumberRange(values[n], values[n+1]-0.0000000001));
    		cats[n] = cats[n].geophysics(true);
    	}
    	
    	SampleDimension sd = new SampleDimension(cats, null).geophysics(true);
    	return sd;
    }
    
    
    
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for (int n=0; n<values.length; n++) {
    		sb.append(values[n] + " ::: " + colors[n] + "\n");
    	}
    	return sb.toString();
    }

}

