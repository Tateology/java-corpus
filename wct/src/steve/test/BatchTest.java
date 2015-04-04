package steve.test;

import gov.noaa.ncdc.wct.ui.AWTImageExport;
import gov.noaa.ncdc.wct.ui.WCTMapPane;
import gov.noaa.ncdc.wms.WMSData;
import gov.noaa.ncdc.wms.WMSException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.StyledMapRenderer;

public class BatchTest {

    
    public static void main(String[] args) {
        try {
            testMap();
            
            
        } catch (WMSException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static void testMap() throws WMSException, Exception {
    
        System.setProperty("java.awt.headless", "true"); 
        
        BufferedImage bimage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bimage.getGraphics();
        
        WCTMapPane wCTMapPane = new WCTMapPane();
        wCTMapPane.setBackground(new Color(0, 0, 55));
        wCTMapPane.setMagnifierGlass(wCTMapPane.getBackground());
        wCTMapPane.setDoubleBuffered(false);
        
        wCTMapPane.setBounds(new Rectangle(500, 500));
        
        wCTMapPane.setVisibleArea(new java.awt.geom.Rectangle2D.Double(-125.0, 22.0, 58.0, 35.0));
        wCTMapPane.setPreferredArea(new java.awt.geom.Rectangle2D.Double(-125.0, 22.0, 58.0, 35.0));
        wCTMapPane.reset();

        
        WMSData wmsData = new WMSData();
        RenderedGridCoverage rgc = new RenderedGridCoverage(
                wmsData.getGridCoverage("Demis Global", null, 
                (java.awt.geom.Rectangle2D.Double)wCTMapPane.getVisibleArea(), 
                new Rectangle(bimage.getWidth(null), bimage.getHeight(null)))
            );
                
        
        
        ((StyledMapRenderer) wCTMapPane.getRenderer()).addLayer(rgc);

        wCTMapPane.paint(g);
        
        
        AWTImageExport.saveImage(bimage, new File("batchout"), AWTImageExport.Type.PNG);
        
    }
    
    
}
