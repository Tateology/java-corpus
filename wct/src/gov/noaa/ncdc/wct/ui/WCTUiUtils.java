package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.io.ScanResults;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.border.Border;

public class WCTUiUtils {

	
    public static String getVersion() {
        Properties props = new Properties();
        try {
//            URL url = ResourceUtils.getInstance().getJarResource(new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.RESOURCE_CACHE_DIR, "/config/version.properties", null);
//            URL url = WCTUiUtils.class.getResource("/version.properties");
//            InputStream in = url.openStream();
//            props.load(in);
//            in.close();
//            return props.getProperty("version");
            return "3.7.6";
        } catch (Exception e) {
            e.printStackTrace();
            return "?";
        }
    }

    public static String checkCurrentStableVersion() {
        return checkVersion("http://www.ncdc.noaa.gov/wct/app/version-stable.dat");
    }
    public static String checkCurrentBETAVersion() {
        return checkVersion("http://www.ncdc.noaa.gov/wct/app/version-beta.dat");
    }
    private static String checkVersion(String versionUrlString) {
        BufferedReader in = null;
        try {
            URL url = new URL(versionUrlString);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String currentVersion;
            if ((currentVersion = in.readLine()) == null) {
                return "?";
            }
            return currentVersion.trim();
        } catch (Exception e) {
            return "?";
        }
    }

	
	
    /**
     * Gets text size in pixels using default font
     * @param text
     */
    public static Rectangle2D getTextBounds(String text) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();
        return g.getFont().createGlyphVector(fc, text).getVisualBounds();
    }
    
    /**
     * Gets text size in pixels using given font
     * @param font
     * @param text
     */
    public static Rectangle2D getTextBounds(Font font, String text) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        FontRenderContext fc = g.getFontRenderContext();
        return font.createGlyphVector(fc, text).getVisualBounds();
    }
    
    
    /**
     * Creates a titled border
     * @param title
     * @param borderSize
     * @return
     */
    // We can do this because the same border object can be reused.
    public static Border myTitledBorder(String title, int borderSize) {
        return myTitledBorder(title, borderSize, borderSize, borderSize, borderSize);
    }//end myTitledBorder
    
    /**
     * Creates a titled border
     * @param title
     * @param borderSize
     * @return
     */
    // We can do this because the same border object can be reused.
    public static Border myTitledBorder(String title, int top, int left, int bottom, int right) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border etchedBorder  = BorderFactory.createEtchedBorder();

        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, title), 
                empty10Border);

    }//end myTitledBorder
    
    // We can do this because the same border object can be reused.
    public static Border myBorder(int top, int left, int bottom, int right) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border etchedBorder  = BorderFactory.createEtchedBorder();

        return BorderFactory.createCompoundBorder(etchedBorder, empty10Border);

    }//end myTitledBorder
    
    
    /**
     * titleJustification an integer specifying the justification of the title -- one of the following: 
     * <ul> 
     * <li> TitledBorder.LEFT 
     * <li> TitledBorder.CENTER 
     * <li> TitledBorder.RIGHT 
     * <li> TitledBorder.LEADING 
     * <li> TitledBorder.TRAILING 
     * <li> TitledBorder.DEFAULT_JUSTIFICATION (leading)
     * </ul> 
	 * titlePosition an integer specifying the vertical position of the text in relation to the border -- one of the following:
	 * <ul> 
     * <li> TitledBorder.ABOVE_TOP 
     * <li> TitledBorder.TOP (sitting on the top line) 
     * <li> TitledBorder.BELOW_TOP 
     * <li> TitledBorder.ABOVE_BOTTOM 
     * <li> TitledBorder.BOTTOM (sitting on the bottom line) 
     * <li> TitledBorder.BELOW_BOTTOM 
     * <li> TitledBorder.DEFAULT_POSITION (top) 
	 * </ul>
     * @param title
     * @param top
     * @param left
     * @param bottom
     * @param right
     * @param titleJustification
     * @param titlePosition
     * @return
     */
    public static Border myTitledTopBorder(String title, int top, int left, int bottom, int right, int titleJustification, int titlePosition) {
        Border empty10Border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        Border topBorder  = BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK);

        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(topBorder, title, titleJustification, titlePosition), 
                empty10Border);

    }//end myTitledBorder
    
    
    
    
    /**
     * Fills list model with 'getDisplayName' from each ScanResult.  This presets the size of the list model for faster loading.
     * @param listModel
     * @param scanResults
     * @return
     */
    public static DefaultListModel fillListModel(DefaultListModel listModel, ScanResults[] scanResults) {
        
        System.out.println("clearing list model...");        
//        listModel.clear();
//        listModel.setSize(0);
        listModel = new DefaultListModel();
        System.out.println("setting list model size... ["+scanResults.length+"]");        
        listModel.setSize(scanResults.length);
        System.out.println("setting list model data...");        

        // Add to list
        for (int n=0; n<scanResults.length; n++) {
            
//            System.out.println("scanResult: "+n+" of "+scanResults.length+" ::: "+scanResults[n]);
            
            if (scanResults[n] != null) {
                if (scanResults[n].getLongName() != null && ! scanResults[n].getDisplayName().trim().equals("null")) {
                    listModel.set(n, scanResults[n].getLongName());                    
                }
                else {
                    listModel.set(n, scanResults[n].getFileName());
                }
            }
            else {
                System.out.println("null scanResult: "+n+" of "+scanResults.length);
            }
        }
        
        return listModel;
    }

    public static int getComponentIndex(Container cont, Component comp) {

    	int index = -1;
    	for ( int k = 0, count = cont.getComponentCount(); k < count && index == -1; k++ ) {
    		if ( cont.getComponent( k ) == comp ) {
    			index = k;
    		}
    	}
    	return index;
	}
    
    
    



    public static boolean classExists (String className) {
        try {
            Class.forName (className);
            return true;
        }
        catch (ClassNotFoundException exception) {
            return false;
        }
    } 

    
    
    
    
}
