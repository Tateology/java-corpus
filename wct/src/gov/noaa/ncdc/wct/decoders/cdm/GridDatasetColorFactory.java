package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class GridDatasetColorFactory {

    public static Color[] getColors(String name, boolean isFlipped) throws Exception {
        Color[] colors = null;
        if (name.equalsIgnoreCase("rainbow")) {
            colors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE, Color.BLUE.brighter().brighter(), Color.GREEN, 
                    Color.YELLOW, Color.ORANGE, Color.RED };
            
            
            System.out.println(Arrays.toString(colors));
            
        }
        else if (name.equalsIgnoreCase("grayscale")) {
            colors = new Color[] { Color.BLACK, Color.WHITE };
        }
        else if (name.equalsIgnoreCase("blue-white-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.WHITE, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("blue-black-red")) {
            colors = new Color[] { Color.BLUE.brighter(), Color.BLACK, Color.RED.brighter() };
        }
        else if (name.equalsIgnoreCase("blue-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("blue-green-red")) {
            colors = new Color[] { Color.BLUE.darker(), Color.GREEN, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("brown-white-green")) {
            colors = new Color[] { new Color(184, 153, 125), Color.WHITE, new Color(118, 229, 119) };
        }
        else if (name.equalsIgnoreCase("white-green")) {
            colors = new Color[] { Color.WHITE, Color.GREEN.darker() };
        }
        else if (name.equalsIgnoreCase("black-green")) {
            colors = new Color[] { Color.BLACK, Color.GREEN.darker() };
        }
        else if (name.equalsIgnoreCase("white-blue")) {
            colors = new Color[] { Color.WHITE, Color.BLUE.darker() };
        }
        else if (name.equalsIgnoreCase("black-blue")) {
            colors = new Color[] { Color.BLACK, Color.CYAN };
        }
        else if (name.equalsIgnoreCase("white-red")) {
            colors = new Color[] { Color.WHITE, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("black-red")) {
            colors = new Color[] { Color.BLACK, Color.RED.darker() };
        }
        else if (name.equalsIgnoreCase("yellow-purple")) {
            colors = new Color[] { Color.YELLOW, new Color(122, 55, 139) };
        }
        else if (name.equalsIgnoreCase("yellow-blue-purple")) {
            colors = new Color[] { Color.YELLOW, Color.BLUE, new Color(122, 55, 139) };
        }
        else if (name.equalsIgnoreCase("yellow-blue")) {
            colors = new Color[] { Color.YELLOW, Color.ORANGE, Color.CYAN, Color.BLUE.darker().darker() };
        }
        else {
            throw new Exception("Color table name of '"+name+"' is not recognized.");
        }
        
        if (isFlipped) {
            colors = WCTUtils.flipArray(colors);            
        }
        return colors;
    }
    
    
    public static ColorsAndValues getColorsAndValues(String colorTableName) throws MalformedURLException, WCTException, IOException {
    	
    	GoesColorFactory goesColorFactory = GoesColorFactory.getInstance();
    	
    	if (colorTableName.equalsIgnoreCase("Water Vapor (Default)")) {
    		URL url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    "/config/colormaps/goes-IR3.cmap", null);
    		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ColorsAndValues cav = ColorLutReaders.parseCmapFormat(br);
            cav.flip();
            br.close();
            return cav;
    	}
    	else if (colorTableName.equalsIgnoreCase("Infrared Window (Default)")) {
    		URL url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    "/config/colormaps/goes-IR4.cmap", null);
    		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ColorsAndValues cav = ColorLutReaders.parseCmapFormat(br);
            cav.flip();
            br.close();
//     System.out.println(cav);
            return cav;
    	}    	
    	else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
                    goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 1 (McIDAS_VAPOR1.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-VAPOR1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Water Vapor 2 (McIDAS_WVRBT3.ET)")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-WVRBT3.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TSTORM2.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TSTORM2.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_TEMPS1.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-TEMPS1.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_SSTS.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-SSTS.ET", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_CA.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-ca.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BB.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bb.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("McIDAS_BD.ET")) {
            return ColorsAndValues.calculateEqualColorsAndValues(
            		goesColorFactory.readETColorsAndValues(ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/goes-bd.et", null))
               );
        }
        else if (colorTableName.equalsIgnoreCase("Reflectivity")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/nexrad_dp_z.pal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseGR2A(br);
        	br.close();
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	cav.flip();
//        	System.out.println(rawCav);
//     		System.out.println(cav);
        	return cav;
        }
        else if (colorTableName.equalsIgnoreCase("Diff. Reflectivity")) {

        	URL url = ResourceUtils.getInstance().getJarResource(
        			new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
        			"/config/colormaps/nexrad_dp_zdr.pal", null);

        	BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        	ColorsAndValues rawCav = ColorLutReaders.parseGR2A(br);
        	br.close();
        	ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues(rawCav, 19);
        	cav.flip();
        	return cav;
        }
        else {
            throw new WCTException("Color table name of '"+colorTableName+"' is not recognized.");
        }
    	
    }
}
