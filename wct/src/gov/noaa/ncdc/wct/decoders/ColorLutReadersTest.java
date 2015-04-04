package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.geotools.cv.SampleDimension;
import org.junit.Test;

public class ColorLutReadersTest {

	@Test
	public void testSplitBySpaceWithQuotes() {
		
		String str = "Unique: 1.0 \"Category 1\"  255 155 55 5";
		String text = "1 2 \"333 4\" 55 6    \"77\" 8 999";
		    // 1 2 "333 4" 55 6    "77" 8 999

		    String regex = "\"([^\"]*)\"|(\\S+)";

		    Matcher m = Pattern.compile(regex).matcher(str);
		    while (m.find()) {
		        if (m.group(1) != null) {
		            System.out.println("Quoted [" + m.group(1) + "]");
		        } else {
		            System.out.println("Plain [" + m.group(2) + "]");
		        }
		    }

		
		
		String[] cols = ColorLutReaders.splitBySpaceWithQuotes(str);
		System.out.println(Arrays.toString(cols));
		for (String c : cols) {
			System.out.println("["+c.replaceAll("\"", "")+"]");
		}
	}
	
	
	@Test
	public void testCmap() throws MalformedURLException, IOException {
		
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/goes-IR3.cmap", null);
		
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        ColorsAndValues cav = ColorLutReaders.parseCmapFormat(br);
   		br.close();

        System.out.println("value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));

        Assert.assertEquals(256, cav.getValues().length);
        Assert.assertEquals(256, cav.getColors().length);
	}

	
	
	@Test
	public void testGR2A() throws MalformedURLException, IOException, WCTException {
		
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/nexrad_dp_zdr.pal", null);
		
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        ColorsAndValues cav1 = ColorLutReaders.parseGR2A(br);
   		br.close();

        System.out.println("(value size="+cav1.getValues().length+")  "+Arrays.toString(cav1.getValues()));
        System.out.println("(color size="+cav1.getColors().length+")  "+Arrays.toString(cav1.getColors()));

        double keyValue = cav1.getValues()[cav1.getValues().length/2];
        Color keyColor = cav1.getColors()[cav1.getColors().length/2];
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav1.getValues()) + 
        		" match value="+cav1.getValues()[findClosestIndex(keyValue, cav1.getValues())] +
        		" match color="+cav1.getColors()[findClosestIndex(keyValue, cav1.getValues())]
        );

        Assert.assertEquals(12, cav1.getValues().length);
        Assert.assertEquals(12, cav1.getColors().length);
        
        ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 20);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())]
        );

        Assert.assertEquals(20, cav.getValues().length);
        Assert.assertEquals(20, cav.getColors().length);

        
		cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 50);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())]
        );

        Assert.assertEquals(50, cav.getValues().length);
        Assert.assertEquals(50, cav.getColors().length);

        
        
        
        
		cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 250);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())]
        );

        Assert.assertEquals(250, cav.getValues().length);
        Assert.assertEquals(250, cav.getColors().length);

	}

	
	
	
	
	
	
	
	
	@Test
	public void testWCTPal() throws MalformedURLException, IOException, WCTException {
		
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/nexrad_dp_z.wctpal", null);
		
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        ColorsAndValues cav1 = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();

        System.out.println("(value size="+cav1.getValues().length+")  "+Arrays.toString(cav1.getValues()));
        System.out.println("(color size="+cav1.getColors().length+")  "+Arrays.toString(cav1.getColors()));

        double keyValue = cav1.getValues()[cav1.getValues().length/2];
        Color keyColor = cav1.getColors()[cav1.getColors().length/2];
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav1.getValues()) + 
        		" match value="+cav1.getValues()[findClosestIndex(keyValue, cav1.getValues())] +
        		" match color="+cav1.getColors()[findClosestIndex(keyValue, cav1.getValues())]
        );

        Assert.assertEquals(21, cav1.getValues().length);
        Assert.assertEquals(21, cav1.getColors().length);
        
        ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 35);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())] + 
        		" alpha="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())].getAlpha()
        );

        Assert.assertEquals(35, cav.getValues().length);
        Assert.assertEquals(35, cav.getColors().length);

        
		cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 50);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())] +
        		" alpha="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())].getAlpha()
        );

        Assert.assertEquals(50, cav.getValues().length);
        Assert.assertEquals(50, cav.getColors().length);

        
        
        
        
		cav = ColorsAndValues.calculateEqualColorsAndValues(cav1, 250);

        System.out.println("(value size="+cav.getValues().length+")  "+Arrays.toString(cav.getValues()));
        System.out.println("(color size="+cav.getColors().length+")  "+Arrays.toString(cav.getColors()));
        System.out.println("closest value match index=[length/2]: value="+ keyValue + " color="+keyColor+
        		"  ::: match index=" + findClosestIndex(keyValue, cav.getValues()) + 
        		" match value="+cav.getValues()[findClosestIndex(keyValue, cav.getValues())] +
        		" match color="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())] +
        		" alpha="+cav.getColors()[findClosestIndex(keyValue, cav.getValues())].getAlpha()
        );

        Assert.assertEquals(250, cav.getValues().length);
        Assert.assertEquals(250, cav.getColors().length);

	}


	
	

	
	@Test
	public void testSampleDimension() throws MalformedURLException, IOException, WCTException {
		
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/nexrad_dp_z.wctpal", null);
		
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        ColorsAndValues cav1 = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();

        System.out.println("(value size="+cav1.getValues().length+")  "+Arrays.toString(cav1.getValues()));
        System.out.println("(color size="+cav1.getColors().length+")  "+Arrays.toString(cav1.getColors()));
        
        SampleDimension sd = cav1.getSampleDimension();
        System.out.println(sd);

	}

	
	
	@Test
	public void testSampleDimensionGetColorAtValue() throws MalformedURLException, IOException, WCTException {
		
		URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/colormaps/nexrad_dp_z.wctpal", null);
		
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        ColorsAndValues cav1 = ColorLutReaders.parseWCTPal(br)[0];
   		br.close();

        System.out.println("(value size="+cav1.getValues().length+")  "+Arrays.toString(cav1.getValues()));
        System.out.println("(color size="+cav1.getColors().length+")  "+Arrays.toString(cav1.getColors()));
        
        SampleDimension sd = cav1.getSampleDimension();
        System.out.println(sd);

        System.out.println(ColorsAndValues.getColor(sd, 30));
        System.out.println(ColorsAndValues.getColor(sd, 31));
        System.out.println(ColorsAndValues.getColor(sd, 32));
        System.out.println(ColorsAndValues.getColor(sd, 33));
        System.out.println(ColorsAndValues.getColor(sd, 34));
        System.out.println(ColorsAndValues.getColor(sd, 35));
        
	}

	
	public static int findClosestIndex(double value, Double[] valueArray) {
		double delta = Double.MAX_VALUE;
		int index = -1;
		
		for (int n=0; n<valueArray.length; n++) {
			if (Math.abs(value - valueArray[n]) < delta) {
				delta = Math.abs(value - valueArray[n]);
				index = n;
			}
		}
		
		return index;
	}
}
