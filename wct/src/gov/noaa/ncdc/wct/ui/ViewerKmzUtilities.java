package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.gis.kml.KMLUtils;
import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.nexradiv.SnapshotLayer;
import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.WCTLegendPanel;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.animation.ExportKMZThread;
import gov.noaa.ncdc.wct.ui.ge.WCTColladaUtils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.geotools.gc.GridCoverage;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


public class ViewerKmzUtilities {

	public final static String META_OVERLAY_XY = "x= \"0\" y= \"1\" xunits= \"fraction\" yunits= \"fraction\" ";
	public final static String META_SCREEN_XY = "x= \"0\" y= \"0.875\" xunits= \"fraction\" yunits= \"fraction\" ";
	public final static String META_ROTATION_XY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
	public final static String META_SIZE = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";


    private StringBuffer kmlString = new StringBuffer();
    private StringBuffer kmlMetaString = new StringBuffer();
    private int kmlFrameIndex = 0;

    private AltitudeMode altMode = AltitudeMode.CLAMPED_TO_GROUND;
    private double altitude = Double.NaN;
    private boolean createShadowImages = false;
    private boolean isDrapeOnColladaSelected = false;
    private int elevationExaggeration = 1;

    private WCTViewer viewer;

    private KmzExportDialog kmzDialog;
    
    public static final SimpleDateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final SimpleDateFormat FILE_SCANNER_FORMATTER = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    static {
        ISO_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
        FILE_SCANNER_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public enum AltitudeMode { CLAMPED_TO_GROUND, ABSOLUTE, DRAPE_ON_MODEL }

    private ArrayList<KMLGroundOverlay> kmlOverlayList = new ArrayList<KMLGroundOverlay>();
    private ArrayList<KMLModel> kmlModelList = new ArrayList<KMLModel>();


    
    
    /** 
     * Set up the util class with the current viewer 
     * @param viewer
     */
    public ViewerKmzUtilities(WCTViewer viewer) {
        this.viewer = viewer;    
    }
    

    
    /**
     * Saves a KMZ of the current Viewer map view.  Will open save dialog to choose output file.
     * @throws Exception 
     */
    public void saveKmz() throws Exception {


        if (viewer.getFileScanner() == null) {
            throw new WCTException("No data file is currently displayed");
        }
        
        boolean useCollada = isColladaCapable(viewer);
        
        kmzDialog = new KmzExportDialog(viewer, useCollada, viewer.getFileScanner().getLastScanResult().getDataType());
        kmzDialog.setModal(true);
        kmzDialog.pack();
        kmzDialog.setLocation(viewer.getX()+45, viewer.getY()+45);
        kmzDialog.setVisible(true);

        // wait for input from this modal dialog

        this.altitude = kmzDialog.getAltitude();
        this.createShadowImages = kmzDialog.isGeneralCreateShadow();
        this.isDrapeOnColladaSelected = kmzDialog.isDrapeOnColladaSelected();
        this.elevationExaggeration = kmzDialog.getElevationExaggeration();
        
        String outFileString = kmzDialog.getOutputFile();
        if (kmzDialog.isSubmitted() && outFileString.trim().length() > 0) {
            File outFile = new File(outFileString);

            WCTProperties.setWCTProperty("kmzsavedir", outFile.getParent());
            if (! outFile.toString().endsWith(".kmz")) {
                outFile = new File(outFile.toString()+".kmz");
            }
            // Check for existing file
            if (outFile.exists()) {
                String message = "The Image file \n" +
                "<html><font color=red>" + outFile + "</font></html>\n" +
                "already exists.\n\n" +
                "Do you want to proceed and OVERWRITE?";

                Object[] options = {"YES", "NO"};
                int choice = JOptionPane.showOptionDialog(viewer, message, "OVERWRITE KMZ FILE",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if (choice == 1) { // NO
                    return;
                }
            }
            System.out.println("SAVING: " + outFile);

//            if (getKmzExportDialog() != null && 
//            		Integer.parseInt(getKmzExportDialog().getNumberOfSweepsToProcess().split(" ")[0]) > 1) {

            if (getKmzExportDialog() != null && 
                	( getKmzExportDialog().getNumberOfSweepsToProcess().toUpperCase().startsWith("ALL") ||
               		Integer.parseInt(getKmzExportDialog().getNumberOfSweepsToProcess().split(" ")[0]) > 1)) {
            
            	ExportKMZThread kmzExportThread = new ExportKMZThread(
            			viewer, 
            			null, 
            			new URL[] { viewer.getCurrentDataURL() }, 
            			true
            		);
            	getKmzExportDialog().setOutputFile(outFile.toString());
            	kmzExportThread.setKmzExportDialog(getKmzExportDialog());
            	kmzExportThread.setAutoOpenKMZ(true);
            	kmzExportThread.start();
            }
            else {
                saveKmz(outFile);
            }

        }
    }
    
    
    /**
     * This shows the kmz export dialog and will set the properties/variables used in
     * the export.
     * @throws WCTException 
     */
    public boolean showConfigurationDialog() throws WCTException {
        if (viewer.getFileScanner() == null) {
            throw new WCTException("No data file is currently displayed");
        }
        
        boolean useCollada = isColladaCapable(viewer);
        
        kmzDialog = new KmzExportDialog(viewer, useCollada, viewer.getFileScanner().getLastScanResult().getDataType(), false);
        kmzDialog.setModal(true);
        kmzDialog.pack();
        kmzDialog.setLocation(viewer.getX()+45, viewer.getY()+45);
        kmzDialog.setVisible(true);

        // wait for input from this modal dialog

        this.altitude = kmzDialog.getAltitude();
        this.createShadowImages = kmzDialog.isGeneralCreateShadow();
        this.isDrapeOnColladaSelected = kmzDialog.isDrapeOnColladaSelected();
        this.elevationExaggeration = kmzDialog.getElevationExaggeration();
        
//        String outFileString = kmzDialog.getOutputFile();
//        return (kmzDialog.isSubmitted() && outFileString.trim().length() > 0);

        return kmzDialog.isSubmitted();
    }

    /**
     * Saves a KMZ of the current Viewer map view.
     * @throws Exception 
     */
    public void saveKmz(File outFile) throws Exception {
    	saveKmz(outFile, true);
    }
    
    
    /**
     * Saves a KMZ of the current Viewer map view.
     * @param outFile  output file
     * @param showOpenDialog show the 'do you want to open ...' dialog?
     * @throws Exception 
     */
    public void saveKmz(File outFile, boolean showOpenDialog) throws Exception {


            String timestamp = String.valueOf(System.currentTimeMillis());
            // set up temp files
//            File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "jnx");
            File tmpdir = new File(WCTConstants.getInstance().getDataCacheLocation());
            tmpdir.mkdir();
            File tmpKmzFile = new File(tmpdir + File.separator + outFile.getName()+"_"+timestamp);



            


            FileScanner scannedFile = viewer.getFileScanner();

            // Create a buffer for reading the files
            byte[] buf = new byte[1024];

            // Create the ZIP file
            ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(tmpKmzFile));

            Rectangle2D.Double maxBounds = new Rectangle2D.Double();
            initKML();

            Rectangle2D.Double bounds = viewer.getCurrentExtent();
            maxBounds.add(bounds);

            
            
            
            
            
            
            
            
            
            
            
//            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED && 
//                    scannedFile.getLastScanResult().getTimestamp() == null) {
            
            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED || 
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) { 

                Date startDate = viewer.getGridDatasetRaster().getLastProcessedDateTime();
                if (startDate == null) {
                    startDate = viewer.getGridDatasetRaster().getLastProcessedRuntime();
                }
                String saveName = viewer.getFileScanner().getSaveName();
                if (saveName == null || saveName.equalsIgnoreCase("null") || saveName.trim().length() == 0) {
                    saveName = viewer.getFileScanner().getLastScanResult().getFileName();
                }

                String gridName = viewer.getGridDatasetRaster().getLastProcessedGridDataset().getGrids().
                	get(viewer.getGridProps().getSelectedGridIndex()).getName();
                String frameName = saveName+"-var_"+gridName+"-t0-rt0-z0";

                int[] zIndices = Arrays.copyOf(viewer.getGridProps().getSelectedZIndices(), 
                        viewer.getGridProps().getSelectedZIndices().length);

                AltitudeMode altMode = AltitudeMode.CLAMPED_TO_GROUND;
                // 1st, check for valid real z height if 'real' height is selected
                if (zIndices.length > 0 && Double.isInfinite(this.altitude)) {
                    this.altitude = getGridAltitude(zIndices[0]);
//                    if (this.altitude > 1000) {
                        altMode = AltitudeMode.ABSOLUTE;
//                    }
//                    else {
//                        altMode = AltitudeMode.CLAMPED_TO_GROUND;
//                    }
                }
                else if (! Double.isNaN(this.altitude)) {
                    altMode = AltitudeMode.ABSOLUTE;
                }
                
                String uniqueString = String.valueOf(System.currentTimeMillis()%100000);
                frameName = frameName+"-"+uniqueString;
                
                this.altitude *= elevationExaggeration;
                
                addFrameToKMZ(kmzOut, frameName, frameName+".png", 
                        startDate, null, bounds, altMode, this.altitude, 
                        (altMode == AltitudeMode.CLAMPED_TO_GROUND) ? false : createShadowImages);
                
                
            }
            else {
            	if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
            		if (viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters() != null &&
            				! Double.isNaN(viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters()[0])) {
            			this.altitude = viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters()[0]
            				*elevationExaggeration;
            		}
            	}

            	
//            	String uniqueString = String.valueOf(System.currentTimeMillis()%100000);
//            	String saveName = null;
//            	Date date1 = null;
//            	if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//            		saveName = scannedFile.getSaveName(
//            				viewer.getRadialProps().getRadialPropsPanel().getVariableName(), 
//            				viewer.getRadialProps().getRadialPropsPanel().getCutElevation());
//            		date1 = new Date(viewer.getRadialRemappedRaster().getDateInMilliseconds());
//            	}
//            	else {
//            		saveName = scannedFile.getSaveName();
//            		date1 = (scannedFile != null && scannedFile.getLastScanResult().getTimestamp() != null) ? 
//                			FILE_SCANNER_FORMATTER.parse(scannedFile.getLastScanResult().getTimestamp()) : null;
//            	}
            	
            	
            	
//            	Date date1 = (scannedFile != null && scannedFile.getLastScanResult().getTimestamp() != null) ? 
//            			FILE_SCANNER_FORMATTER.parse(scannedFile.getLastScanResult().getTimestamp()) : null;
//            	Date date2 = (nextScannedFile != null && nextScannedFile.getLastScanResult().getTimestamp() != null) ? 
//            			FILE_SCANNER_FORMATTER.parse(nextScannedFile.getLastScanResult().getTimestamp()) : null;
//            	    	
//                addFrameToKML(saveName, saveName+"-"+uniqueString+".png",
//                        date1, date2, bounds, altMode, alt, createShadowImages, addLegendOverlays);

            	
            	
            	
            	if (isColladaCapable(viewer) && isDrapeOnColladaSelected) {
            		addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds, AltitudeMode.DRAPE_ON_MODEL, this.altitude, createShadowImages);                     
//            		addFrameToKMZ(kmzOut, saveName, saveName+"-"+uniqueString, date1, null, bounds, AltitudeMode.DRAPE_ON_MODEL, this.altitude, createShadowImages);                     
            	}            	
                // Append to KML
            	else if (! Double.isNaN(this.altitude)) {
                    addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds, AltitudeMode.ABSOLUTE, this.altitude, createShadowImages);                         
                }
                else {
                    addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds);
                }
            }
            
            
            
// old way, pending removal
//            if (createShadowImages) {
//                addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds, this.altMode, this.altitude, true, 45);                         
//            }
//            else {
//                addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds);
//            }


            finishKML();

            // Write KML
            kmzOut.putNextEntry(new ZipEntry("wct.kml"));
            byte[] kmlBytes = kmlString.toString().getBytes();
            kmzOut.write(kmlBytes, 0, kmlBytes.length);
            kmzOut.closeEntry();

            // Copy NOAA logo to KMZ
            kmzOut.putNextEntry(new ZipEntry("noaalogo.gif"));
            // Transfer bytes from the file to the ZIP file
            URL logoURL = ExportKMZThread.class.getResource("/images/noaa_logo_50x50.png");
            InputStream in = logoURL.openStream();
            int len;
            while ((len = in.read(buf)) > 0) {
                kmzOut.write(buf, 0, len);
            }
            // Complete the entry
            kmzOut.closeEntry();
            in.close();

            kmzOut.close();

            kmlString.setLength(0); // clear
            kmlMetaString.setLength(0); // clear



            // Delete old kmz if present
            try {
                if (outFile.exists()) {
                    outFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Rename file
            try {
                tmpKmzFile.renameTo(outFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (showOpenDialog) {
            	
            	int openChoice = JOptionPane.showOptionDialog(viewer, "Open '"+outFile+"'?",
                    "KMZ Export Complete", JOptionPane.YES_NO_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    new ImageIcon(WCTViewer.class.getResource("/icons/ge-icon.png")), null, null);
            	if (openChoice == JOptionPane.YES_OPTION) {
            		try {
            			Desktop.getDesktop().open(outFile);
            		} catch (IOException e) {
            			e.printStackTrace();
            			JOptionPane.showMessageDialog(viewer, "Open Error: "+e.getMessage(),
                            "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
            		}
            	}
            	
            }
    }

    
    
    public static boolean isColladaCapable(WCTViewer viewer) {
    	boolean useCollada = false;
    	if (viewer.getFileScanner() == null || viewer.getFileScanner().getLastScanResult() == null) {
    		return false;
    	}
    	
    	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL ||
    			viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
    			viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
    		
    		if (! Double.isNaN(viewer.getLastDecodedRadarElevationAngle())) {
    			useCollada = true;
    		}
    		
    		// don't use collada for cappi
        	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
        		if (viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters() != null &&
        				! Double.isNaN(viewer.getRadialRemappedRaster().getLastDecodedCappiHeightInMeters()[0])) {
        			useCollada = false;
        		}
        	}
    	}
    	return useCollada;
    }
    
    
    
    /**
     * Get a BufferedImage of just the data layer(s) from the a new, fresh, empty map pane 
     * based on the current viewer's map pane.
     * @return
     */
    public BufferedImage getCustomMapPaneBufferedImage() throws WCTException {
    	return getCustomMapPaneBufferedImage(true);
    }
    
    public BufferedImage getCustomMapPaneBufferedImage(boolean includeSnapshotLayers) throws WCTException {

        RenderedGridCoverage viewerRadarRGC = viewer.getRadarRenderedGridCoverage();
        RenderedGridCoverage viewerGridSatelliteRGC = viewer.getGridSatelliteRenderedGridCoverage();
        
        
        
        if (viewerRadarRGC.isVisible() && viewer.getNexradHeader() != null && 
                viewer.getNexradHeader().getProductType() == NexradHeader.L3VAD && 
                viewer.getNexradHeader().getProductCode() == NexradHeader.L3PC_VERTICAL_WIND_PROFILE) {
            throw new WCTException("KMZ Export of the Level-III VAD product is not supported.");
        }
        
        
        BufferedImage bimage = new BufferedImage(
                viewer.getMapPane().getWidth(), 
                viewer.getMapPane().getHeight(), 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        
        
        WCTMapPane wctMapPane = new WCTMapPane();
        wctMapPane.setBackground(new Color(0, 0, 0, 0));
        wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
        wctMapPane.setDoubleBuffered(true);
        wctMapPane.setBounds(new Rectangle(viewer.getMapPane().getWidth(), viewer.getMapPane().getHeight()));
        

//        g.setComposite(AlphaComposite.Clear);
//        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());
//        g.setComposite(AlphaComposite.Src);
        
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());        
//        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        

        RenderedGridCoverage rgc = null;
        if (viewerRadarRGC.isVisible()) {
            rgc = new RenderedGridCoverage(viewerRadarRGC.getGridCoverage());
            rgc.setZOrder(viewerRadarRGC.getZOrder());
        }
        if (viewerGridSatelliteRGC.isVisible()) {
            rgc = new RenderedGridCoverage(viewer.getGridSatelliteGridCoverage());
            rgc.setZOrder(viewerGridSatelliteRGC.getZOrder());
        }

        if (rgc != null) {
            ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);
        }
        
        // add snapshot layers?
        if (includeSnapshotLayers) {
        	for (SnapshotLayer sl : viewer.getSnapshotLayers()) {
        		if (sl.getRenderedGridCoverage().isVisible()) {
        			RenderedGridCoverage snapshotRGC = new RenderedGridCoverage(sl.getRenderedGridCoverage().getGridCoverage());
        			snapshotRGC.setZOrder(sl.getRenderedGridCoverage().getZOrder());
            
        			((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(snapshotRGC);
        		}
        	}
        }
        
        
        wctMapPane.setPreferredSize(new Dimension(viewer.getMapPane().getWidth(), viewer.getMapPane().getHeight()));
        wctMapPane.setVisibleArea(viewer.getCurrentExtent());
        
        
        
//        JFrame frame = new JFrame("Map testing frame");
//        JPanel wctMapPanel = new JPanel(new BorderLayout());
//        wctMapPanel.add(wctMapPane, BorderLayout.CENTER);
//        frame.add(wctMapPanel);
//        frame.pack();
//        frame.setVisible(true);
        
        
//        System.out.println("wctZoomableBounds: "+wctMapPane.getWCTZoomableBounds(null));
        
        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
                wctMapPane.getWCTZoomableBounds(null), viewer.getMapPane().getZoom(), true);
        
//        wctMapPanel.paint(g);
        
        
        g.dispose();

        for (RenderedLayer l : ((StyledMapRenderer) wctMapPane.getRenderer()).getLayers()) {
            l.dispose();
        }

        
        if (rgc != null) {
            ((StyledMapRenderer) wctMapPane.getRenderer()).removeAllLayers();
            try {
                rgc.getGridCoverage().dispose();
            } catch (Exception e) {
                ;
            }
            rgc.dispose();
        }

        wctMapPane = null;

        return bimage;
    }
    

    /**
     *  Get a BufferedImage of just the data layer(s) from the a new, fresh, empty map pane 
     *  based on the current viewer's map pane.
     * @return
     */
    public BufferedImage getFreshMapPaneBufferedImage(java.awt.geom.Rectangle2D.Double extent, int width, int height) throws Exception {        

//    	width = viewer.getMapPane().getWidth();
//    	height = viewer.getMapPane().getHeight();
    	
        viewer.getMapPaneZoomChange().setActive(false);
        viewer.setCurrentExtent(extent);
        viewer.getMapPaneZoomChange().setActive(true);
        
        FileScanner scannedFile = viewer.getFileScanner();
        if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
            viewer.refreshGridDataset();
        }
        else if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
            viewer.refreshSatellite();
        }
        else if (scannedFile != null && (
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL ||
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS ||
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) ) {
            viewer.refreshRadarDataWithWait(true);
        }
        
        
        RenderedGridCoverage viewerRadarRGC = viewer.getRadarRenderedGridCoverage();
        RenderedGridCoverage viewerSatelliteRGC = viewer.getGridSatelliteRenderedGridCoverage();
        
        
        
        if (viewerRadarRGC.isVisible() && viewer.getNexradHeader() != null && 
                viewer.getNexradHeader().getProductType() == NexradHeader.L3VAD && 
                viewer.getNexradHeader().getProductCode() == NexradHeader.L3PC_VERTICAL_WIND_PROFILE) {
            throw new WCTException("KMZ Export of the Level-III VAD product is not supported.");
        }
        
        
        BufferedImage bimage = new BufferedImage(
                width, 
                height, 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        
        
        WCTMapPane wctMapPane = new WCTMapPane();
        wctMapPane.setBackground(new Color(0, 0, 0, 0));
        wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
        wctMapPane.setDoubleBuffered(true);
        

        RenderedGridCoverage rgc = null;
        if (viewerRadarRGC.isVisible()) {
            rgc = new RenderedGridCoverage(viewerRadarRGC.getGridCoverage());
        }
        if (viewerSatelliteRGC.isVisible()) {
            rgc = new RenderedGridCoverage(viewer.getGridSatelliteGridCoverage());
        }

        if (rgc != null) {
            ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);
        }
        
        wctMapPane.setBounds(new Rectangle(width, height));
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());
        
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        wctMapPane.setVisibleArea(viewer.getCurrentExtent());
        
        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
                new Rectangle(width, height), viewer.getMapPane().getZoom(), true);
//                mapPane.getWCTZoomableBounds(null), viewer.getMapPane().getZoom(), true);
        
        g.dispose();
        
        if (rgc != null) {
            ((StyledMapRenderer) wctMapPane.getRenderer()).removeAllLayers();
            rgc.getGridCoverage().dispose();
            rgc.dispose();
        }

        wctMapPane = null;
        
        return bimage;

    }

    
    
    
    
    
    /**
     * Init the KML string, setting up the folder structure, etc...
     */
    public void initKML() {
        kmlString.setLength(0); // clear
        kmlMetaString.setLength(0); // clear
        kmlFrameIndex = 0;

        kmlOverlayList.clear();
        kmlModelList.clear();
        
        
        
        
        kmlString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
        kmlString.append("<kml xmlns=\"http://earth.google.com/kml/2.1\"> \n");
        kmlString.append("<Document> \n");
        kmlString.append(" <Folder> \n");
        kmlString.append("   <name>NOAA Weather and Climate Toolkit Generated Visualization</name> \n");
        kmlString.append("      <description><![CDATA[" +
                "Animation Controls: <br>" +
                "This visualization is 'time-aware'.  If multiple time steps are present, an animation is possible " +
                "by pressing the 'play' button on the time slider. <br><br>" +
                "This animation was created by NOAA's Weather and Climate Toolkit.  For more " +
                "information, please visit one of the following links. <br><br>" +
                "<a href=\"http://www.ncdc.noaa.gov\">NOAA's National Climatic Data Center (NCDC) </a> <br> " +
                "<a href=\"http://www.ncdc.noaa.gov/wct/\">NOAA Weather and Climate Toolkit</a> <br> " +
        "]]></description> \n");
        kmlString.append("   <open>1</open> \n");
        kmlString.append("   <Style> \n");
        kmlString.append("       <ListStyle> \n");
        kmlString.append("           <bgColor>ddffffff</bgColor> \n");
        kmlString.append("       </ListStyle> \n");
        kmlString.append("   </Style> \n");
        kmlString.append("   <Folder> \n");
        kmlString.append("      <name>Image Frames</name> \n");
        kmlString.append("      <visibility>0</visibility> \n");
        kmlString.append("      <description><![CDATA[" +
                "Image Frame Controls: " +
                "To adjust transparency of just the image frames, " +
        "select this folder and adjust transparency slider. ]]></description> \n");


    }



    
    
    
    
    
    
    
    
    
    
    


    /**
     * Add a line 3D cube bounding box with labels. No elevation exaggeration is needed
     * because GE will automatically exaggerate placemarks (but not overlays or models).
     * @param bounds
     * @throws Exception
     */
    public void addKML3DOutlineBoxFolder(Rectangle2D bounds) throws Exception {
        
        kmlString.append("   <Folder> \n");
        kmlString.append("   <name>Exported Data Extent - 3D Cube Outline</name> \n");
        kmlString.append("   <visibility>0</visibility> \n");
        kmlString.append("      <description><![CDATA[" +
                "This represents the extent of the exported data:  " +
                "To adjust transparency of just the legend and logo, " +
        "select this folder and adjust transparency slider. ]]></description> \n");

//        double nexradAltInMeters = viewer.getNexradHeader().getAlt()/3.28083989501312;
        // set to zero because we are doing absolute height from sea level, not radar elevation
        double nexradAltInMeters = 0;
        
        for (int n=4; n<16+1; n=n+4) {
        	
//        	String label = WCTUtils.DECFMT_0D000.format(bounds.getMinX())+" , "+WCTUtils.DECFMT_0D000.format(bounds.getMinY(), );
        	String label = n+" km";
        	if (n == 0) {
        		label += " Above Sea Level";
        	}
        	else {
        		label += " ASL";
        	}
        	String labelCoords = bounds.getMinX()+","+bounds.getMinY()+","+(n*1000 + nexradAltInMeters);
        		
        	
        	String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds, (n*1000 + nexradAltInMeters));
        	kmlString.append("    <Placemark>\n");
        	kmlString.append("      <name>Outline</name>\n");
        	kmlString.append("      <visibility>0</visibility>\n");
        	kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
        	kmlString.append(kmlGeomOutline);
        	kmlString.append("    </Placemark>\n");
        	kmlString.append("    <Placemark>\n");
        	kmlString.append("      <name>"+label+"</name>\n");
        	kmlString.append("      <visibility>0</visibility>\n");
        	kmlString.append("      <styleUrl>#pointStyle</styleUrl>\n");
        	kmlString.append("      <Point>\n");
        	kmlString.append("          <altitudeMode>absolute</altitudeMode>\n");
        	kmlString.append("          <coordinates>"+labelCoords+"</coordinates>\n");        	
        	kmlString.append("      </Point>\n");
        	kmlString.append("    </Placemark>\n");
        }
    	kmlString.append("    <Placemark>\n");
    	kmlString.append("      <name>Outline</name>\n");
    	kmlString.append("      <visibility>0</visibility>\n");
    	kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
    	kmlString.append("      <LineString>\n");
    	kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
    	kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMinY()+","+ 
    									(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMinX()+","+bounds.getMinY()+","+ 
    									(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
    	kmlString.append("      </LineString>\n");
    	kmlString.append("    </Placemark>\n");
    	kmlString.append("    <Placemark>\n");
    	kmlString.append("      <name>Outline</name>\n");
    	kmlString.append("      <visibility>0</visibility>\n");
    	kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
    	kmlString.append("      <LineString>\n");
    	kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
    	kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMinY()+","+ 
    									(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMinY()+","+ 
    									(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
    	kmlString.append("      </LineString>\n");
    	kmlString.append("    </Placemark>\n");
    	kmlString.append("    <Placemark>\n");
    	kmlString.append("      <name>Outline</name>\n");
    	kmlString.append("      <visibility>0</visibility>\n");
    	kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
    	kmlString.append("      <LineString>\n");
    	kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
    	kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMaxY()+","+ 
    									(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMaxY()+","+
    									(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
    	kmlString.append("      </LineString>\n");
    	kmlString.append("    </Placemark>\n");
    	kmlString.append("    <Placemark>\n");
    	kmlString.append("      <name>Outline</name>\n");
    	kmlString.append("      <visibility>0</visibility>\n");
    	kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
    	kmlString.append("      <LineString>\n");
    	kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
    	kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMaxY()+","+ 
    									(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMinX()+","+bounds.getMaxY()+","+
    									(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
    	kmlString.append("      </LineString>\n");
    	kmlString.append("    </Placemark>\n");
        
        

        kmlString.append("    <Style id=\"box3dStyle\"> \n");
        kmlString.append("      <LineStyle> \n");
        kmlString.append("        <width>1.0</width> \n");
        kmlString.append("      </LineStyle> \n");
        kmlString.append("      <PolyStyle> \n");
        kmlString.append("        <color>01000000</color> \n");
        kmlString.append("      </PolyStyle> \n");
        kmlString.append("    </Style> \n");
        
        kmlString.append("    <Style id=\"pointStyle\"> \n");
        kmlString.append("   	<IconStyle> \n");
        kmlString.append("   		<scale>0.5</scale> \n");
        kmlString.append("   		<Icon> \n");
        kmlString.append("   			<href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href> \n");
        kmlString.append("   		</Icon> \n");
        kmlString.append("      </IconStyle> \n");
        kmlString.append("    </Style> \n");

        kmlString.append("   </Folder> \n");


    }

    
    
    


    /**
     * Add a line bounding box and filled semi-transparent bounding box representing the extent of the data.
     * @param bounds
     * @throws Exception
     */
    public void addKMLBoundsFolder(Rectangle2D bounds) throws Exception {
        
        kmlString.append("   <Folder> \n");
        kmlString.append("   <name>Exported Data Extent</name> \n");
        kmlString.append("   <visibility>0</visibility> \n");
        kmlString.append("      <description><![CDATA[" +
                "This represents the extent of the exported data:  " +
                "To adjust transparency of just the legend and logo, " +
        "select this folder and adjust transparency slider. ]]></description> \n");

        
        String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds);
        kmlString.append("    <Placemark>                            \n");
        kmlString.append("      <name>Outline</name>                 \n");
        kmlString.append("      <visibility>1</visibility>           \n");
        kmlString.append("      <styleUrl>#outlineStyle</styleUrl>   \n");
        kmlString.append(kmlGeomOutline);
        kmlString.append("    </Placemark>                           \n");
        
        
        
        String kmlGeomBackground = KMLUtils.getExtentPolyKML(bounds);
        kmlString.append("    <Placemark>                            \n");
        kmlString.append("      <name>Background</name>              \n");
        kmlString.append("      <visibility>0</visibility>           \n");
        kmlString.append("      <styleUrl>#transPolyStyle</styleUrl> \n");
        kmlString.append(kmlGeomBackground);
        kmlString.append("    </Placemark>                           \n");

        kmlString.append("    <Style id=\"outlineStyle\">           \n");
        kmlString.append("      <LineStyle>                         \n");
        kmlString.append("        <width>4.0</width>                \n");
        kmlString.append("      </LineStyle>                        \n");
        kmlString.append("      <PolyStyle>                         \n");
        kmlString.append("        <color>01000000</color>           \n");
        kmlString.append("      </PolyStyle>                        \n");
        kmlString.append("    </Style>                              \n");

        kmlString.append("    <Style id=\"transPolyStyle\">         \n");
        kmlString.append("      <LineStyle>                         \n");
        kmlString.append("        <width>1.5</width>                \n");
        kmlString.append("      </LineStyle>                        \n");
        kmlString.append("      <PolyStyle>                         \n");
        kmlString.append("        <color>7d000000</color>           \n");
        kmlString.append("      </PolyStyle>                        \n");
        kmlString.append("    </Style>                              \n");

        kmlString.append("   </Folder> \n");


    }


    /**
     * Adds a frame to the KMZ.  This will add an image to the KMZ and update the KML with a 
     * reference to that image.
     * @param scannedFile
     * @param nextScannedFile
     * @param bounds
     * @throws TransformException 
     * @throws FactoryException 
     * @throws ParseException 
     * @throws WCTException 
     * @throws MismatchedDimensionException 
     * @throws Exception 
     */
    public void addFrameToKMZ(ZipOutputStream kmzOut, FileScanner scannedFile, FileScanner nextScannedFile, 
            Rectangle2D.Double bounds) 
    throws IOException, MismatchedDimensionException, WCTException, 
    	ParseException, FactoryException, TransformException {
        
        addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds, 
        		AltitudeMode.CLAMPED_TO_GROUND, Double.NaN, false);
    }


    /**
     * Adds a frame to the KMZ.  This will add an image to the KMZ and update the KML with a 
     * reference to that image.
     * 
     * @param kmzOut
     * @param scannedFile
     * @param nextScannedFile
     * @param bounds
     * @param alt
     * @param createShadowImages
     * @param shadowAlpha
     * @throws TransformException 
     * @throws FactoryException 
     * @throws ParseException 
     * @throws WCTException 
     * @throws MismatchedDimensionException 
     * @throws Exception
     */
    public void addFrameToKMZ(ZipOutputStream kmzOut, 
    		FileScanner scannedFile, FileScanner nextScannedFile, 
            Rectangle2D.Double bounds, AltitudeMode altMode, double alt, 
            boolean createShadowImages) 
    throws IOException, MismatchedDimensionException, WCTException, 
    ParseException, FactoryException, TransformException {

    	addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds, 
    			altMode, alt, createShadowImages, true);
    }
    /**
     * Adds a frame to the KMZ.  This will add an image to the KMZ and update the KML with a 
     * reference to that image.
     * 
     * @param kmzOut
     * @param scannedFile
     * @param nextScannedFile
     * @param bounds
     * @param alt
     * @param createShadowImages
     * @param shadowAlpha
     * @throws WCTException 
     * @throws ParseException 
     * @throws TransformException 
     * @throws FactoryException 
     * @throws MismatchedDimensionException 
     */
    public void addFrameToKMZ(ZipOutputStream kmzOut, 
    		FileScanner scannedFile, FileScanner nextScannedFile, 
            Rectangle2D.Double bounds, AltitudeMode altMode, double alt, 
            boolean createShadowImages, boolean addLegendOverlays) 
    throws IOException, WCTException, ParseException, MismatchedDimensionException, FactoryException, TransformException {
        
    	String uniqueString = String.valueOf(System.currentTimeMillis()%100000);
    	String saveName = null;
    	if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
    		saveName = scannedFile.getSaveName(
    				viewer.getRadialProps().getRadialPropsPanel().getVariableName(), 
    				viewer.getRadialProps().getRadialPropsPanel().getCutElevation());
    	}
    	else {
    		saveName = scannedFile.getSaveName();
    	}
    	
    	
        
        String frameName = saveName+"-"+uniqueString+".png";
        
        // ------------------------------------------
        // 1) Generate Image Frame
        // ------------------------------------------
        BufferedImage image = getCustomMapPaneBufferedImage();

        addKMZImageEntry(frameName, image, kmzOut);
        
        if (addLegendOverlays) {
        	// ------------------------------------------
        	// 2) Generate Metadata Image for this frame
        	// ------------------------------------------
        	String frameMetaName = "meta-"+saveName+"-"+uniqueString+".png";
        
        	addKMZImageEntry(frameMetaName, getMetaBufferedImage(), kmzOut);
        }

        // ------------------------------------------
        // 3) Add reference to image frame in KML
        // ------------------------------------------
        addFrameToKML(uniqueString, scannedFile, nextScannedFile, bounds, altMode, alt, createShadowImages, addLegendOverlays);
        
        // 4) Add collada dae model if needed
        if (altMode == AltitudeMode.DRAPE_ON_MODEL) {
        	addKMZDaeEntry(frameName+".dae", frameName, kmzOut);
        }
    }

    
    
    
    
    public void addFrameToKMZ(
    		ZipOutputStream kmzOut, String frameName, String imageName, 
    		Date startDate, Date endDate,
            Rectangle2D.Double bounds, AltitudeMode altMode, 
            double alt, boolean createShadowImages) 
    throws IOException, MismatchedDimensionException, FactoryException, TransformException, WCTException {
        
        
        // ------------------------------------------
        // 1) Generate Image Frame
        // ------------------------------------------
        BufferedImage image = getCustomMapPaneBufferedImage();

        addKMZImageEntry(imageName, image, kmzOut);

        // ------------------------------------------
        // 2) Generate Metadata Image for this frame
        // ------------------------------------------
        String frameMetaName = "meta-"+imageName;
        addKMZImageEntry(frameMetaName, getMetaBufferedImage(), kmzOut);

        // ------------------------------------------
        // 3) Add reference to image frame in KML
        // ------------------------------------------
        addFrameToKML(frameName, imageName, startDate, endDate, bounds, altMode, alt, createShadowImages, true);
        

        // 4) Add collada dae model if needed
        if (altMode == AltitudeMode.DRAPE_ON_MODEL) {
        	addKMZDaeEntry(frameName+".dae", frameName, kmzOut);
        }
    }
    
    

    /**
     * Adds a frame to the KML.  This will refer to an image frame added with 'addKMZImageEntry'
     * @param scannedFile
     * @param nextScannedFile
     * @param bounds
     * @throws ParseException 
     */
    public void addFrameToKML(String uniqueString, FileScanner scannedFile, FileScanner nextScannedFile, Rectangle2D.Double bounds) throws ParseException {
        addFrameToKML(uniqueString, scannedFile, nextScannedFile, bounds, AltitudeMode.CLAMPED_TO_GROUND, Double.NaN, false, true);
    }


    public void addFrameToKML(String uniqueString, FileScanner scannedFile, FileScanner nextScannedFile, Rectangle2D.Double bounds, 
            AltitudeMode altMode, double alt, boolean createShadowImages, boolean addLegendOverlays) throws ParseException {

    	
//    	Date date1 = (scannedFile != null && scannedFile.getLastScanResult().getTimestamp() != null) ? 
//    			FILE_SCANNER_FORMATTER.parse(scannedFile.getLastScanResult().getTimestamp()) : null;
//    	Date date2 = (nextScannedFile != null && nextScannedFile.getLastScanResult().getTimestamp() != null) ? 
//    			FILE_SCANNER_FORMATTER.parse(nextScannedFile.getLastScanResult().getTimestamp()) : null;
    	    	
    	
    	
    	String saveName = null;
    	if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
    		saveName = scannedFile.getSaveName(
    				viewer.getRadialProps().getRadialPropsPanel().getVariableName(), 
    				viewer.getRadialProps().getRadialPropsPanel().getCutElevation());
//    		if (date1 != null) {
//    			date1 = new Date(viewer.getRadialRemappedRaster().getDateInMilliseconds());
//    		}
//    		if (date2 != null && scannedFile.getLastScanResult().getTimestamp().equals(nextScannedFile.getLastScanResult().getTimestamp())) {
//    			date2 = new Date(viewer.getRadialRemappedRaster().getDateInMilliseconds());
//    		}
    		
			scannedFile.getLastScanResult().setTimestamp(FILE_SCANNER_FORMATTER.format(
					new Date(viewer.getRadialRemappedRaster().getDateInMilliseconds())));
    		if (nextScannedFile != null && 
    				scannedFile.getLastScanResult().getTimestamp().equals(nextScannedFile.getLastScanResult().getTimestamp())) {
    			
    			nextScannedFile.getLastScanResult().setTimestamp(FILE_SCANNER_FORMATTER.format(
    					new Date(viewer.getRadialRemappedRaster().getDateInMilliseconds())));
    		}
    		
    	}
    	else {
    		saveName = scannedFile.getSaveName();
    	}

    	
    	
    	
    	Date date1 = (scannedFile != null && scannedFile.getLastScanResult().getTimestamp() != null) ? 
    			FILE_SCANNER_FORMATTER.parse(scannedFile.getLastScanResult().getTimestamp()) : null;
    	Date date2 = (nextScannedFile != null && nextScannedFile.getLastScanResult().getTimestamp() != null) ? 
    			FILE_SCANNER_FORMATTER.parse(nextScannedFile.getLastScanResult().getTimestamp()) : null;
    	    	

    	
    	
        addFrameToKML(saveName, saveName+"-"+uniqueString+".png",
                date1, date2, bounds, altMode, alt, createShadowImages, addLegendOverlays);
    }
    
    /**
     * If endTime is null, then we are at the last frame.
     * @param frameName
     * @param startTime
     * @param endTime
     * @param bounds
     * @param alt
     * @param createShadowImages
     * @param shadowAlpha
     */
    public void addFrameToKML(String frameName, String imageName, Date startTime, Date endTime, 
            Rectangle2D.Double bounds, AltitudeMode altMode, double alt, boolean createShadowImages, boolean addLegendOverlays) {

        this.altMode = altMode;
        this.altitude = alt;
        this.createShadowImages = createShadowImages;

        kmlFrameIndex++;
        
        if (altMode == AltitudeMode.DRAPE_ON_MODEL) {
        	KMLModel kmlModel = new KMLModel();
        	kmlModel.setFrameName(frameName);
        	kmlModel.setTimestamp(startTime);
        	kmlModel.setEndTimestamp(endTime);
        	kmlModel.setImageName(imageName);
        	kmlModel.setFrameIndex(kmlFrameIndex);
        	kmlModel.setAltitude(alt);
        	kmlModel.setBounds(bounds);
        	kmlModel.setAddLegendOverlays(addLegendOverlays);
        	
        	kmlModelList.add(kmlModel);
        }
        else {
        	
        	KMLGroundOverlay kmlOverlay = new KMLGroundOverlay();
        	kmlOverlay.setFrameName(frameName);
        	kmlOverlay.setTimestamp(startTime);
        	kmlOverlay.setEndTimestamp(endTime);
        	kmlOverlay.setImageName(imageName);
        	kmlOverlay.setFrameIndex(kmlFrameIndex);
        	kmlOverlay.setAltitude(alt);
        	kmlOverlay.setAltitudeMode(altMode);
        	kmlOverlay.setBounds(bounds);
        	kmlOverlay.setAddLegendOverlays(addLegendOverlays);
        
        	kmlOverlayList.add(kmlOverlay);
        }
        
    }
    
    private void processKMLOverlayList() {
    	if (kmlOverlayList.size() == 0) {
    		return;
    	}

        // find average time difference between layers and use that to calculate the last time step
    	long avgTimeDiff = calculateAverageTimestep(kmlOverlayList);
        
        for (int n=0; n<kmlOverlayList.size(); n++) {
            
//            System.out.println("kmlOverlayList timestamp:  "+kmlOverlayList.get(n).getTimestamp());
            
        
            KMLGroundOverlay overlay = kmlOverlayList.get(n);
            String frameName = overlay.getFrameName();
            int frameIndex = overlay.getFrameIndex();
            String imageName = overlay.getImageName();
            double frameAlt = overlay.getAltitude();
            AltitudeMode frameAltMode = overlay.getAltitudeMode();
            Rectangle2D.Double frameBounds = overlay.getBounds();
            
            Date startTime = overlay.getTimestamp();
            Date endTime = overlay.getEndTimestamp();
            if ((endTime != null && startTime != null && startTime.equals(endTime)) ||
                (endTime == null && startTime != null)) {
            	
                if (n < kmlOverlayList.size()-1) {
                    endTime = kmlOverlayList.get(n+1).getTimestamp();
                    if (startTime.equals(endTime)) {
                    	endTime = new Date(startTime.getTime() + avgTimeDiff);
                    }
                }
                else {
                    endTime = new Date(startTime.getTime() + avgTimeDiff);
                }
            }
            
        
            
            

            if (startTime != null) System.out.println("  now: "+ISO_DATE_FORMATTER.format(startTime));
            if (endTime != null) System.out.println(" next: "+ISO_DATE_FORMATTER.format(endTime));

            String kmlTimeString = null;
            String nextKmlTimeString = null;

            if (startTime != null) {

            	if (startTime.before(endTime)) {
            		kmlTimeString = ISO_DATE_FORMATTER.format(startTime);
            		nextKmlTimeString = ISO_DATE_FORMATTER.format(endTime);
            	}
            	else {
            		nextKmlTimeString = ISO_DATE_FORMATTER.format(startTime);
            		kmlTimeString = ISO_DATE_FORMATTER.format(endTime);
            	}

                // 012345678901234567890
                // 20040812 23:34
                // YYYY-MM-DDThh:mm:ssZ
                // Add ZIP entry to output stream.
            }



            kmlString.append("   <GroundOverlay> \n");
            kmlString.append("       <name>"+frameName+"</name> \n");
            if (kmlTimeString != null) {
                kmlString.append("       <TimeSpan> \n");
                kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
                kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
                kmlString.append("       </TimeSpan> \n");
            }
            kmlString.append("       <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
            kmlString.append("       <Icon> \n");
            kmlString.append("           <href>"+imageName+"</href> \n");
            kmlString.append("           <viewBoundScale>0.75</viewBoundScale> \n");
            kmlString.append("       </Icon> \n");
            if (frameAlt > 0 || frameAltMode != AltitudeMode.CLAMPED_TO_GROUND) {
                kmlString.append("       <altitude>"+frameAlt+"</altitude>        \n");
                if (frameAltMode == AltitudeMode.ABSOLUTE) {
                    kmlString.append("       <altitudeMode>absolute</altitudeMode>   \n");
                }
                else {
                    kmlString.append("       <altitudeMode>relativeToGround</altitudeMode>   \n");
                }
            }
            kmlString.append("       <LatLonBox> \n");
            kmlString.append("           <north>"+frameBounds.getMaxY()+"</north> \n");
            kmlString.append("           <south>"+frameBounds.getMinY()+"</south> \n");
            kmlString.append("           <east>"+frameBounds.getMaxX()+"</east> \n");
            kmlString.append("           <west>"+frameBounds.getMinX()+"</west> \n");
            kmlString.append("       </LatLonBox> \n");
            kmlString.append("   </GroundOverlay> \n");



            // Optional Shadow Images
            if (createShadowImages) {
                kmlFrameIndex++;
                
                createShadowImageKML(kmlString, frameName, kmlTimeString, nextKmlTimeString, imageName, frameBounds);

            }

            // Metadata
            if (overlay.isAddLegendOverlays()) {
            	kmlMetaString.append(getMetadataOverlay(frameName+"-Metadata", kmlTimeString, nextKmlTimeString, kmlFrameIndex, "meta-"+imageName));
            }
            
        //Legend
        //        String legendOverlayXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendScreenXY = "x= \"0\" y= \"0.02\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendRotationXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendSize = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";
        //        kmlMetaString.append("   <ScreenOverlay> \n");
        //        kmlMetaString.append("      <name>"+name+"-Legend</name> \n");
        //        kmlMetaString.append("      <TimeSpan> \n");
        //        kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
        //        kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
        //        kmlMetaString.append("      </TimeSpan> \n");
        //        kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
        //        kmlMetaString.append("      <Icon><href> legend-"+frameName+"</href></Icon> \n");
        //        kmlMetaString.append("      <overlayXY " + legendOverlayXY + " /> \n");
        //        kmlMetaString.append("      <screenXY " + legendScreenXY + " /> \n");
        //        kmlMetaString.append("      <rotationXY " + legendRotationXY + " /> \n");
        //        kmlMetaString.append("      <size " + legendSize + " /> \n");
        //        kmlMetaString.append("   </ScreenOverlay>");


            
            kmlFrameIndex++;
        }
    }

    

    public String getMetadataOverlay(String frameName, String kmlTimeString, String nextKmlTimeString, int kmlFrameIndex, String imageName) {
    	
    	StringBuilder kmlMetaString = new StringBuilder();
    	
    	kmlMetaString.append("   <ScreenOverlay> \n");
    	kmlMetaString.append("      <name>"+frameName+"</name> \n");
    	if (kmlTimeString != null && nextKmlTimeString != null) {
    		kmlMetaString.append("      <TimeSpan> \n");
    		kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
    		kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
    		kmlMetaString.append("      </TimeSpan> \n");
    	}
    	kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
    	kmlMetaString.append("      <Icon><href>"+imageName+"</href></Icon> \n");
    	kmlMetaString.append("      <overlayXY " + META_OVERLAY_XY + " /> \n");
    	kmlMetaString.append("      <screenXY " + META_SCREEN_XY + " /> \n");
    	kmlMetaString.append("      <rotationXY " + META_ROTATION_XY + " /> \n");
    	kmlMetaString.append("      <size " + META_SIZE + " /> \n");
    	kmlMetaString.append("   </ScreenOverlay> \n");

    	return kmlMetaString.toString();
    }
    

    private void processKMLModelList() {
    	
    	if (kmlModelList.size() == 0) {
    		return;
    	}

    	long avgTimeDiff = calculateAverageTimestep(kmlModelList);
        
        for (int n=0; n<kmlModelList.size(); n++) {
            
//            System.out.println("kmlModelList timestamp:  "+kmlModelList(n).getTimestamp());
            
        
            KMLModel model = kmlModelList.get(n);
            String frameName = model.getFrameName();
            int frameIndex = model.getFrameIndex();
            String imageName = model.getImageName();
            double frameAlt = model.getAltitude();
            Rectangle2D.Double frameBounds = model.getBounds();
            
            Date startTime = model.getTimestamp();
            Date endTime = model.getEndTimestamp();
            if ((endTime != null && startTime != null && startTime.equals(endTime)) ||
                (endTime == null && startTime != null)) {
            	   
                if (n < kmlModelList.size()-1) {
                    endTime = kmlModelList.get(n+1).getTimestamp();
                    if (startTime.equals(endTime)) {
                    	endTime = new Date(startTime.getTime() + avgTimeDiff);
                    }
                }
                else {
                    endTime = new Date(startTime.getTime() + avgTimeDiff);
                }
            }
            
        
            
            

            if (startTime != null) System.out.println("  now: "+ISO_DATE_FORMATTER.format(startTime));
            if (endTime != null) System.out.println(" next: "+ISO_DATE_FORMATTER.format(endTime));

            String kmlTimeString = null;
            String nextKmlTimeString = null;

            if (startTime != null) {

            	if (startTime.before(endTime)) {
            		kmlTimeString = ISO_DATE_FORMATTER.format(startTime);
            		nextKmlTimeString = ISO_DATE_FORMATTER.format(endTime);
            	}
            	else {
            		nextKmlTimeString = ISO_DATE_FORMATTER.format(startTime);
            		kmlTimeString = ISO_DATE_FORMATTER.format(endTime);
            	}

                // 012345678901234567890
                // 20040812 23:34
                // YYYY-MM-DDThh:mm:ssZ
                // Add ZIP entry to output stream.
            }




    		kmlString.append(" 	<Placemark> \n");
    		kmlString.append(" 	<name>"+frameName+"</name> \n");
            if (kmlTimeString != null) {
                kmlString.append("       <TimeSpan> \n");
                kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
                kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
                kmlString.append("       </TimeSpan> \n");
            }
    		kmlString.append(" 	<visibility>1</visibility> \n");
    		kmlString.append(" 	<Style id=\"default\"></Style> \n");
    		kmlString.append(" 	<LookAt> \n");
    		kmlString.append(" 		<altitudeMode>absolute</altitudeMode> \n");
    		kmlString.append("  		<longitude>"+frameBounds.getCenterX()+"</longitude> \n");
    		kmlString.append("  		<latitude>"+frameBounds.getCenterY()+"</latitude> \n");
    		kmlString.append(" 		<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> \n");
    		kmlString.append(" 	    <range>300000</range> \n");
    		kmlString.append(" 	    <tilt>65</tilt> \n");
    		kmlString.append(" 	    <heading>0</heading> \n");
    		kmlString.append(" 	</LookAt> \n");
    		kmlString.append(" 	<Model id=\"model_1\"> \n");
    		kmlString.append(" 		<altitudeMode>absolute</altitudeMode> \n");
    		kmlString.append(" 		<Location> \n");
    		kmlString.append("  			<longitude>"+viewer.getNexradHeader().getLon()+"</longitude> \n");
    		kmlString.append("  			<latitude>"+viewer.getNexradHeader().getLat()+"</latitude> \n");
    		kmlString.append(" 			<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> \n");
    		kmlString.append(" 		</Location> \n");
    		kmlString.append(" 		<Orientation> \n");
    		kmlString.append(" 			<heading>0</heading> \n");
    		kmlString.append(" 			<tilt>0</tilt> \n");
    		kmlString.append(" 			<roll>0</roll> \n");
    		kmlString.append(" 		</Orientation> \n");
    		kmlString.append(" 		<Scale> \n");
    		kmlString.append(" 			<x>1</x> \n");
    		kmlString.append(" 			<y>1</y> \n");
    		kmlString.append(" 			<z>1</z> \n");
    		kmlString.append(" 		</Scale> \n");
    		kmlString.append(" 		<Link> \n");
    		kmlString.append(" 			<href>"+imageName+".dae</href> \n");
    		kmlString.append(" 		</Link> \n");
    		kmlString.append(" 	</Model> \n");
    		kmlString.append(" </Placemark> \n");


            // Optional Shadow Images
            if (createShadowImages) {
                kmlFrameIndex++;
                
                createShadowImageKML(kmlString, frameName, kmlTimeString, nextKmlTimeString, imageName, frameBounds);

            }

            // Metadata
            if (model.isAddLegendOverlays()) {
            	kmlMetaString.append(getMetadataOverlay(frameName+"-Metadata", kmlTimeString, nextKmlTimeString, frameIndex, "meta-"+imageName));
            }

        //Legend
        //        String legendOverlayXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendScreenXY = "x= \"0\" y= \"0.02\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendRotationXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        //        String legendSize = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";
        //        kmlMetaString.append("   <ScreenOverlay> \n");
        //        kmlMetaString.append("      <name>"+name+"-Legend</name> \n");
        //        kmlMetaString.append("      <TimeSpan> \n");
        //        kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
        //        kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
        //        kmlMetaString.append("      </TimeSpan> \n");
        //        kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
        //        kmlMetaString.append("      <Icon><href> legend-"+frameName+"</href></Icon> \n");
        //        kmlMetaString.append("      <overlayXY " + legendOverlayXY + " /> \n");
        //        kmlMetaString.append("      <screenXY " + legendScreenXY + " /> \n");
        //        kmlMetaString.append("      <rotationXY " + legendRotationXY + " /> \n");
        //        kmlMetaString.append("      <size " + legendSize + " /> \n");
        //        kmlMetaString.append("   </ScreenOverlay>");


            
            kmlFrameIndex++;
        }
    }
    
    /**
     * Add a 'ScreenOverlay' kml section to the kml document
     * @param kmlSection
     */
    public void addScreenOverlay(String kmlSection) {
    	kmlMetaString.append(kmlSection);
    }

    
    
    
    
    
    private void createShadowImageKML(StringBuffer kmlString, String frameName, 
    		String kmlTimeString, String nextKmlTimeString, String imageName, 
    		Rectangle2D.Double frameBounds) {

        kmlString.append("   <GroundOverlay> \n");
        kmlString.append("       <name>"+frameName+"_shadow</name> \n");
        if (kmlTimeString != null) {
            kmlString.append("       <TimeSpan> \n");
            kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
            kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
            kmlString.append("       </TimeSpan> \n");
        }
        kmlString.append("       <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
        kmlString.append("       <color>4b000000</color> \n");
        kmlString.append("       <Icon> \n");
        kmlString.append("           <href>"+imageName+"</href> \n");
        kmlString.append("           <viewBoundScale>0.75</viewBoundScale> \n");
        kmlString.append("       </Icon> \n");
        kmlString.append("       <LatLonBox> \n");
        kmlString.append("           <north>"+frameBounds.getMaxY()+"</north> \n");
        kmlString.append("           <south>"+frameBounds.getMinY()+"</south> \n");
        kmlString.append("           <east>"+frameBounds.getMaxX()+"</east> \n");
        kmlString.append("           <west>"+frameBounds.getMinX()+"</west> \n");
        kmlString.append("       </LatLonBox> \n");
        kmlString.append("   </GroundOverlay> \n");
    }
    
    
    
    /**
     * Assembles the KML file, linking all metadata, data and bounding box pieces.
     */
    public void finishKML() {
        
        processKMLOverlayList();
        processKMLModelList();
        
        kmlString.append("   </Folder> \n");



        try {
            addKMLBoundsFolder(viewer.getCurrentExtent());
            if (isDrapeOnColladaSelected) {
            	addKML3DOutlineBoxFolder(viewer.getCurrentExtent());
            }
        } catch (Exception e) {
            System.err.println("COULD NOT ADD CURRENT EXTENT POLYGON TO KML");
        }




        kmlString.append(getKMLScreenOverlayFolder());

        kmlString.append("   </Folder> \n");
        kmlString.append(" </Document> \n");
        kmlString.append("</kml> \n");       
    }
    
    
    /**
     * Gets the KML text - should be called after 'finishKML' to get a complete file.
     * @return
     */
    public String getKML() {
        return kmlString.toString();
    }
    
    

    private String getKMLScreenOverlayFolder() {        
        //Screen Overlay Variables
        String logo = "noaalogo.gif";

        //NOAA logo
        String logoOverlayXY = "x= \"1\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        String logoScreenXY = "x= \"1\" y= \"0.125\" xunits= \"fraction\" yunits= \"fraction\" ";
        String logoRotationXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
        String logoSize = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";

        StringBuffer sb = new StringBuffer();

        //Screen Overlays
        sb.append("<Folder> \n");
        sb.append("   <name>Overlays</name> \n");
        sb.append("   <visibility>0</visibility> \n");
        sb.append("      <description><![CDATA[" +
                "Legend and Logo Controls:  " +
                "To adjust transparency of just the legend and logo, " +
        "select this folder and adjust transparency slider. ]]></description> \n");
        sb.append("   <ScreenOverlay> \n");
        sb.append("      <name>NOAA Logo</name> \n");
        sb.append("      <Icon><href>"+logo+"</href></Icon> \n");
        sb.append("      <overlayXY " + logoOverlayXY + " /> \n");
        sb.append("      <screenXY " + logoScreenXY + " /> \n");
        sb.append("      <rotationXY " + logoRotationXY + " /> \n");
        sb.append("      <size " + logoSize + " /> \n");
        sb.append("   </ScreenOverlay> \n");
        sb.append(kmlMetaString);
        sb.append("</Folder> \n");

        return sb.toString();

    }


    public BufferedImage getLegendBufferedImage() throws IOException, AWTException {

        WCTLegendPanel legendPanel = viewer.getLargeLegendPanel(); 
        int exportWidth = legendPanel.getWidth();
        int exportHeight = legendPanel.getHeight();



        //      exportWidth = 140;
        exportHeight = exportHeight - 15;

        exportWidth = 170;
        exportHeight = 280;


        System.out.println("LEGEND IMAGE SIZE: "+exportWidth+"x"+exportHeight);

        BufferedImage buffImage = new BufferedImage(exportWidth, exportHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buffImage.createGraphics();
        ////   paint background to legend color
        g2.setColor(viewer.getLegendBGColor());
        g2.fillRect(0, 0, exportWidth, exportHeight);
        g2.setColor(viewer.getLegendFGColor());

        legendPanel.paintComponent(g2);
        g2.dispose();

        return buffImage;

    }
    

    /**
     * Returns legend image.  Returns null if none available.
     * @return
     * @throws WCTException 
     */
    public BufferedImage getMetaBufferedImage() throws WCTException  {
    	return getMetaBufferedImage(false);
    }

    /**
     * Returns legend image.  Returns null if none available.
     * @return
     * @throws WCTException 
     */
    public BufferedImage getMetaBufferedImage(boolean isRadialVolume) throws WCTException  {

        if (viewer.getGridSatelliteGridCoverage() == null && viewer.getRadarGridCoverage() == null) {
            return null;
        }
        
        CategoryLegendImageProducer legend = viewer.getLastDecodedLegendImageProducer();
        Color tmpColor = legend.getBackgroundColor();
        boolean tmpDrawBorder = legend.isDrawBorder();

        legend.setBackgroundColor(new Color(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 230));
        legend.setDrawBorder(true);

        if (isRadialVolume) {
        	legend.setSpecialMetadata(new String[] { "RADIAL VOLUME SCAN", " " });
        }
        

        BufferedImage bimage;
        if (viewer.getFileScanner() != null &&
        		(viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT
        		|| viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED)) {
            legend.setInterpolateBetweenCategories(true);
            bimage = legend.createMediumLegendImage();
        }
        else {
//            bimage = legend.createLargeLegendImage(new Dimension(180, 450));
            bimage = legend.createLargeLegendImage();
        }

        legend.setBackgroundColor(tmpColor);
        legend.setDrawBorder(tmpDrawBorder);

        return bimage;

    }



    public void addKMZImageEntry(String frameName, BufferedImage buffImage, ZipOutputStream kmzOut) 
        throws IOException {

        kmzOut.putNextEntry(new ZipEntry(frameName));

        ImageIO.write(buffImage, "png", kmzOut);

        //   Complete the image frame entry
        kmzOut.closeEntry();

        buffImage = null;
    }

    public void addKMZDaeEntry(String daeName, String imageName, ZipOutputStream kmzOut) throws IOException, MismatchedDimensionException, FactoryException, TransformException, WCTException {

        kmzOut.putNextEntry(new ZipEntry(daeName));

        kmzOut.write(WCTColladaUtils.getColladaDAE(viewer, imageName, elevationExaggeration).getBytes());

        //   Complete the image frame entry
        kmzOut.closeEntry();
    }








    
    
    
    public static BufferedImage getBufferedImage(GridCoverage gc) {
    	
    	RenderedImage rimg = gc.getRenderedImage();
        int width = rimg.getData().getWidth();
        int height = rimg.getData().getHeight();
        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
        
        Rectangle2D bounds = rgc.getPreferredArea();
        
        BufferedImage bimage = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
//        g.drawImage(rimg., 0, 0, null);
        
        WCTMapPane wctMapPane = new WCTMapPane();
      
        wctMapPane.setBackground(new Color(0, 0, 0, 0));
        wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
        wctMapPane.setDoubleBuffered(true);
        
        ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);

        wctMapPane.setBounds(bounds.getBounds());
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());
        
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        wctMapPane.setVisibleArea(bounds);
//        mapPane.paint(g);

//        AffineTransform t = viewer.getMapPane().getZoom();
        AffineTransform t = AffineTransform.getScaleInstance(1.0, 1.0);
        
//        long millis = System.currentTimeMillis();
//        System.out.println(millis+" "+t);
        
        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
                wctMapPane.getWCTZoomableBounds(null), t, true);
        
        
        g.dispose();
        
//        AWTImageExport.saveImage(bimage, new File("kmzout"+millis), AWTImageExport.Type.PNG);
        
        wctMapPane = null;
        rimg = null;
        rgc = null;
        gc = null;
        
        return bimage;

    	
    }



    
    
    
    
    
    
    
    public double getGridAltitude(int zIndex) throws IllegalAccessException, InstantiationException {
        if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis() == null) {
            return Double.NaN;
        }
        
        String vertUnits = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getUnitsString();
        if (vertUnits.equalsIgnoreCase("m") || vertUnits.equalsIgnoreCase("meter") || vertUnits.equalsIgnoreCase("meters")) {
            return viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getCoordValue(zIndex);
        }
        else if (vertUnits.equalsIgnoreCase("Pa")){
            double pressureInPa = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getCoordValue(zIndex);
            return NexradEquations.getAltitudeFromPressureInMeters(pressureInPa);

//            System.out.println("pressure in: "+pressureInPa+" Pa, altitude out: "+this.altitude);
        }
        else {
            return Double.NaN;
        }

    }

    
    

//    private static BufferedImage convertRGBAToIndexed(BufferedImage src) {
//        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
//        Graphics g = dest.getGraphics();
//        g.setColor(new Color(231,20,189));
//        g.fillRect(0, 0, dest.getWidth(), dest.getHeight()); //fill with a hideous color and make it transparent
//        dest = makeTransparent(dest,0,0);
//        dest.createGraphics().drawImage(src,0,0, null);
//        return dest;
//    }
//
//    private static BufferedImage makeTransparent(BufferedImage image, int x, int y) {
//        ColorModel cm = image.getColorModel();
//        if (!(cm instanceof IndexColorModel))
//            return image; //sorry...
//        IndexColorModel icm = (IndexColorModel) cm;
//        WritableRaster raster = image.getRaster();
//        int pixel = raster.getSample(x, y, 0); //pixel is offset in ICM's palette
//        int size = icm.getMapSize();
//        byte[] reds = new byte[size];
//        byte[] greens = new byte[size];
//        byte[] blues = new byte[size];
//        icm.getReds(reds);
//        icm.getGreens(greens);
//        icm.getBlues(blues);
//        IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues, pixel);
//        return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
//    }
    
    
    
    public void setAltMode(AltitudeMode altMode) {
		this.altMode = altMode;
	}
	public AltitudeMode getAltMode() {
		return altMode;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setCreateShadowImages(boolean createShadowImages) {
		this.createShadowImages = createShadowImages;
	}
	public boolean isCreateShadowImages() {
		return createShadowImages;
	}
	public void setDrapeOnColladaSelected(boolean isDrapeOnColladaSelected) {
		this.isDrapeOnColladaSelected = isDrapeOnColladaSelected;
	}
    public boolean isDrapeOnColladaSelected() {
    	return this.isDrapeOnColladaSelected;
    }
	public void setElevationExaggeration(int elevationExaggeration) {
		this.elevationExaggeration = elevationExaggeration;
	}
	public int getElevationExaggeration() {
		return elevationExaggeration;
	}

	public void setKmzExportDialog(KmzExportDialog kmzDialog) {
		this.kmzDialog = kmzDialog;
	}
	public KmzExportDialog getKmzExportDialog() {
		return kmzDialog;
	}



	
	
	private static <E> long calculateAverageTimestep(ArrayList<E> kmlElementList) {
		
		if (kmlElementList.size() < 2) {
			return 0;
		}
		
		// 1. get unique dates
		ArrayList<Date> uniqueDateList = new ArrayList<Date>();
		for (KMLElement kmlElement : (ArrayList<KMLElement>)kmlElementList) {
			if (! uniqueDateList.contains(kmlElement.getTimestamp())) {
				uniqueDateList.add(kmlElement.getTimestamp());
			}
		}
		
		if (uniqueDateList.size() < 2) {
			return 0;
		}
		
		// 2. calculate average timestep
		
        // find average time difference between layers and use that to calculate the last time step
        long timeDiffTotal = 0;
        for (int n=1; n<uniqueDateList.size(); n++) {
//            System.out.println("time diff: "+(kmlOverlayList.get(n).getTimestamp().getTime()-kmlOverlayList.get(n-1).getTimestamp().getTime()));
            timeDiffTotal += uniqueDateList.get(n).getTime()-uniqueDateList.get(n-1).getTime();
//            System.out.println("time diff total: "+timeDiffTotal);
        }

        return Math.round(timeDiffTotal/(uniqueDateList.size()-1));
	}
	
	private interface KMLElement {
		public Date getTimestamp();
		public Date getEndTimestamp();
	}
	
	private class KMLModel implements KMLElement {
    	private String frameName;
    	private java.awt.geom.Rectangle2D.Double bounds;
    	private double alt;
    	private int frameIndex;
    	private String imageName;
    	private Date timestamp;
    	private Date endTimestamp;
    	private boolean addLegendOverlays;
          
		public void setFrameName(String frameName) {
			this.frameName = frameName;
		}
		public String getFrameName() {
			return frameName;
		}
		public void setBounds(java.awt.geom.Rectangle2D.Double bounds) {
			this.bounds = bounds;
		}
		public java.awt.geom.Rectangle2D.Double getBounds() {
			return bounds;
		}
		public void setAltitude(double alt) {
			this.alt = alt;
		}
		public double getAltitude() {
			return alt;
		}
		public void setFrameIndex(int frameIndex) {
			this.frameIndex = frameIndex;
		}
		public int getFrameIndex() {
			return frameIndex;
		}
		public void setImageName(String imageName) {
			this.imageName = imageName;
		}
		public String getImageName() {
			return imageName;
		}
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		@Override
		public Date getTimestamp() {
			return timestamp;
		}
		public void setEndTimestamp(Date endTimestamp) {
			this.endTimestamp = endTimestamp;
		}
		@Override
		public Date getEndTimestamp() {
			return endTimestamp;
		}
		public void setAddLegendOverlays(boolean addLegendOverlays) {
			this.addLegendOverlays = addLegendOverlays;
		}
		public boolean isAddLegendOverlays() {
			return addLegendOverlays;
		}
    }
    
    
    private class KMLGroundOverlay implements KMLElement {

        
        private String frameName;
        private java.awt.geom.Rectangle2D.Double bounds;
        private AltitudeMode altitudeMode;
        private double alt;
        private int frameIndex;
        private String imageName;
        private Date timestamp;
        private Date endTimestamp;
        private boolean addLegendOverlays;

        
        public void setFrameName(String frameName) {
            this.frameName = frameName;
        }
        public String getFrameName() {
            return this.frameName;
        }

        public void setBounds(Rectangle2D.Double bounds) {
            this.bounds = bounds;
        }
        public Rectangle2D.Double getBounds() {
            return this.bounds;
        }

        public void setAltitudeMode(AltitudeMode altMode) {
            this.altitudeMode = altMode;
        }
        public AltitudeMode getAltitudeMode() {
            return this.altitudeMode;
        }

        public void setAltitude(double altitude) {
            this.alt = altitude;
        }
        public double getAltitude() {
            return this.alt;
        }

        public void setFrameIndex(int kmlFrameIndex) {
            this.frameIndex = kmlFrameIndex;
        }
        public int getFrameIndex() {
            return this.frameIndex;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
        public String getImageName() {
            return this.imageName;
        }

        public void setTimestamp(Date startTime) {
            this.timestamp = startTime;
        }
        @Override
        public Date getTimestamp() {
            return this.timestamp;
        }
		public void setEndTimestamp(Date endTimestamp) {
			this.endTimestamp = endTimestamp;
		}
		@Override
		public Date getEndTimestamp() {
			return endTimestamp;
		}
		public void setAddLegendOverlays(boolean addLegendOverlays) {
			this.addLegendOverlays = addLegendOverlays;
		}
		public boolean isAddLegendOverlays() {
			return addLegendOverlays;
		}
        
    }
}
