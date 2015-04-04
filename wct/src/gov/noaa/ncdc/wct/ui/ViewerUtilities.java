package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.WCTProperties;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

import org.eclipse.swt.widgets.Display;


public class ViewerUtilities {

    

    /**
     * Shows chooser for output file and output file type.
     * @param viewer
     */
    public static void saveImage(WCTViewer viewer) {
        AWTImageExport imgExport = new AWTImageExport(viewer.getViewerAWTImage());
        File savedir = imgExport.exportAWTChooser(viewer, WCTProperties.getWCTProperty("imgsavedir"));
        WCTProperties.setWCTProperty("imgsavedir", savedir.toString());
        return;
    }
    
    public static void saveImage(WCTViewer viewer, File outfile, AWTImageExport.Type type) {
        AWTImageExport imgExport = new AWTImageExport(viewer.getViewerAWTImage());
        File[] outFile = new File[] { outfile };
        imgExport.exportImage(outFile, type);
        return;        
        
    }
    
    /**
     * Gives save image dialog for a screenshot of a component using SWT.  Component must be visible and on top.
     * @param comp
     */
    public static void saveComponentImageChooserWithSWTScreenshot(final Component comp) {
    	final Display display = Display.getDefault();
    	display.syncExec(
    	  new Runnable() {
    	    public void run(){
    	
        		BufferedImage bimage = ScreenShotWithGC.getScreenshot(display, 
        				comp.getLocationOnScreen().x, 
        				comp.getLocationOnScreen().y, 
        				comp.getWidth(), 
        				comp.getHeight());
        		AWTImageExport export = new AWTImageExport(bimage);
        		File savedir = export.exportAWTChooser(comp, WCTProperties.getWCTProperty("imgsavedir"));
                WCTProperties.setWCTProperty("imgsavedir", savedir.toString());
		    }
		  });
    }
    

    /**
     * Saves a screenshot of a component using SWT.  Component must be visible and on top.
     * @param comp
     */
    public static void saveComponentImageWithSWTScreenshot(final Component comp, final File outFile, final AWTImageExport.Type outType) {
    	final Display display = Display.getDefault();
    	display.syncExec(
    	  new Runnable() {
    	    public void run(){
    	
        		BufferedImage bimage = ScreenShotWithGC.getScreenshot(display, 
        				comp.getLocationOnScreen().x, 
        				comp.getLocationOnScreen().y, 
        				comp.getWidth(), 
        				comp.getHeight());
        		AWTImageExport export = new AWTImageExport(bimage);
        		export.exportImage(new File[] { outFile }, outType);
		    }
		  });
    }
    
    
    public static BufferedImage getComponentImageWithSWTScreenshot(final Component comp) {
    	final Display display = Display.getDefault();
    	final BufferedImage[] images = new BufferedImage[1];
    	display.syncExec(
    	  new Runnable() {
    	    public void run(){
    	
        		BufferedImage bimage = ScreenShotWithGC.getScreenshot(display, 
        				comp.getLocationOnScreen().x, 
        				comp.getLocationOnScreen().y, 
        				comp.getWidth(), 
        				comp.getHeight());
        		images[0] = bimage;
		    }
		  });
		return images[0];
    }

    public static BufferedImage getImageWithSWTScreenshot(final int x, final int y, final int width, final int height) {
    	final Display display = Display.getDefault();
    	final BufferedImage[] images = new BufferedImage[1];
    	display.syncExec(
    	  new Runnable() {
    	    public void run(){
    	
        		BufferedImage bimage = ScreenShotWithGC.getScreenshot(display, x, y, width, height);
        		images[0] = bimage;
		    }
		  });
		return images[0];
    }
    
    
        
    
}
