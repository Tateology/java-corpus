// TODO:
// 1) Output Legend Panel as image file (gif) and add as overlay to GE
// 2) Remove NOAA logo from images and add as overlay to GE

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

package gov.noaa.ncdc.wct.ui.animation;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.MapSelector.DataType;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.KmzExportDialog;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities.AltitudeMode;
import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.geotools.pt.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import ucar.nc2.dt.RadialDatasetSweep;

/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    August 23, 2004
 */
public class ExportKMZThread extends Thread implements ActionListener {


    private WCTViewer viewer;
    private URL[] urlsToLoad;
    private JFrame progressFrame;
    private JProgressBar individualBar, progressBar, memBar;
    private boolean hideViewer;
    private Dimension dimension;
    private JButton cancelButton;

    private Component[] framesToHide;
    private boolean[] framesToHideVisibility;

    private boolean endThread = false;

    private StringBuffer kmlString = new StringBuffer();
    private StringBuffer kmlMetaString = new StringBuffer();
    private int kmlFrameIndex = 0;

    private double altitude = -1;
    private boolean createShadowImages = false;

    private KmzExportDialog kmzExportDialog = null;
    private boolean autoOpenKMZ = false;

    /**
     * Constructor
     *
     * @param  viewer             WCTViewer Object
     * @param  urlsToLoad         The data to load
     * @param  hideViewer         Hides the viewer during processing
     * @param  movieFile          Description of the Parameter
     * @param  exportType         Description of the Parameter
     * @param  framesToHide       Description of the Parameter
     * @param  frameRateInMillis  Description of the Parameter
     */
    public ExportKMZThread(WCTViewer viewer, Component[] framesToHide, 
            URL[] urlsToLoad, boolean hideViewer) {

        this(viewer, framesToHide, urlsToLoad, hideViewer, null);
    }


    /**
     * Constructor
     *
     * @param  viewer            NexradIAViewer Object
     * @param  urlsToLoad         The Nexrad Filenames to load
     * @param  hideViewer         Hides the viewer during processing
     * @param  movieFile          Description of the Parameter
     * @param  exportType         Description of the Parameter
     * @param  framesToHide       Description of the Parameter
     * @param  dimension          Description of the Parameter
     * @param  frameRateInMillis  Description of the Parameter
     */
    public ExportKMZThread(WCTViewer viewer, Component[] framesToHide, URL[] urlsToLoad, 
            boolean hideViewer, Dimension dimension) {

        this.viewer = viewer;
        this.framesToHide = framesToHide;
        this.urlsToLoad = urlsToLoad;
        this.hideViewer = hideViewer;
        this.dimension = dimension;
    }


    /**
     * Method that does the work
     */
    public void run() {
        RenderCompleteListener renderCompleteListener = new RenderCompleteListener() {             
            @Override
            public void renderProgress(int progress) {
                individualBar.setValue(progress);                 
            }
            @Override
            public void renderComplete() {
            }
        };
        viewer.addRenderCompleteListener(renderCompleteListener); 


        if (hideViewer) {
            viewer.setVisible(false);
            if (framesToHide != null) {
                framesToHideVisibility = new boolean[framesToHide.length];
                for (int i=0; i<framesToHide.length; i++) {                    
                    if (framesToHide[i] != null) {
                        framesToHideVisibility[i] = framesToHide[i].isVisible();
                        framesToHide[i].setVisible(false);
                    }
                }
            }
        }

        dimension = null;
        Dimension holdDim = null;
        if (dimension != null) {
            holdDim = viewer.getSize();
            viewer.setSize(dimension);
            //viewer.setCurrentExtent(gr);
        }

        // Create progress frame
        progressFrame = new WCTFrame("Export KMZ Progress");
        individualBar = new JProgressBar(0, 100);
        individualBar.setIndeterminate(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Processed 0/" + urlsToLoad.length + " Files (0 %)");
        progressBar.setValue(0);
        memBar = new JProgressBar(0, 100);
        memBar.setIndeterminate(false);
        memBar.setStringPainted(true);
        memBar.setString("0 %");
        memBar.setValue(0);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel mainPanel = new JPanel(new RiverLayout());
        if (urlsToLoad.length > 1) {
        	mainPanel.add("center", new JLabel("Animation Processing Progress", JLabel.CENTER));
        }
        else {
        	mainPanel.add("center", new JLabel("Data Processing Progress", JLabel.CENTER));
        }
        mainPanel.add("br hfill", individualBar);
        mainPanel.add("br hfill", progressBar);
        mainPanel.add("br hfill", memBar);
        mainPanel.add("br", cancelButton);

        progressFrame.getContentPane().add(mainPanel);
        
        progressFrame.pack();
        progressFrame.setSize(progressFrame.getPreferredSize().width + 100, progressFrame.getPreferredSize().height);
        progressFrame.setVisible(true);


        System.gc();


        if (viewer.getFileScanner() == null || viewer.getFileScanner().getLastScanResult() == null) {
        	viewer.loadAnimationFile(urlsToLoad[0], individualBar.getModel());
        }

        boolean autoMinMax = viewer.getMapSelector().isGridAutoMinMaxSelected();



        boolean useCollada = false;
    	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL ||
    			viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
    			viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
    		
    		if (! Double.isNaN(viewer.getLastDecodedRadarElevationAngle())) {
    			useCollada = true;
    		}
    	}
        
    	if (kmzExportDialog == null) {
    		kmzExportDialog = new KmzExportDialog(viewer, useCollada, viewer.getFileScanner().getLastScanResult().getDataType());
    		kmzExportDialog.pack();
    		kmzExportDialog.setLocationRelativeTo(viewer);
    		kmzExportDialog.setVisible(true);
    	}
        // wait for input from this modal dialog

        this.altitude = kmzExportDialog.getAltitude();
        this.createShadowImages = kmzExportDialog.isGeneralCreateShadow();

        String outFileString = kmzExportDialog.getOutputFile();
        long processTime = System.currentTimeMillis();
        if (kmzExportDialog.isSubmitted() && outFileString.trim().length() > 0) {
            File outFile = new File(outFileString);








            if (! outFile.toString().startsWith(WCTConstants.getInstance().getDataCacheLocation())) {
            	WCTProperties.setWCTProperty("kmzsavedir", outFile.getParent());
            }
            if (! outFile.toString().endsWith(".kmz")) {
                outFile = new File(outFile.toString()+".kmz");
            }
            // Check for existing file
            if (outFile.exists()) {
                String message = "The KMZ file \n" +
                "<html><font color=red>" + outFile + "</font></html>\n" +
                "already exists.\n\n" +
                "Do you want to proceed and OVERWRITE?";

                Object[] options = {"YES", "NO"};
                int choice = JOptionPane.showOptionDialog(kmzExportDialog, message, "OVERWRITE IMAGE FILE",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if (choice == 1) { // NO
                    viewer.removeRenderCompleteListener(renderCompleteListener);
                    returnToViewer();
                    return;
                }
            }
            System.out.println("SAVING: " + outFile);


            processTime = System.currentTimeMillis();

            String timestamp = String.valueOf(System.currentTimeMillis());
            // set up temp files
            File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "jnx");
            tmpdir.mkdir();
            File tmpKmzFile = new File(tmpdir + File.separator + outFile.getName()+"_"+timestamp);




            try {
                // set up the KMZ utils class
                ViewerKmzUtilities kmzUtil = new ViewerKmzUtilities(viewer);
                

                this.altitude = kmzExportDialog.getAltitude();
                this.createShadowImages = kmzExportDialog.isGeneralCreateShadow();
                kmzUtil.setDrapeOnColladaSelected( kmzExportDialog.isDrapeOnColladaSelected() );
                kmzUtil.setElevationExaggeration( kmzExportDialog.getElevationExaggeration() );
                

                // Create a buffer for reading the files
                byte[] buf = new byte[1024];

                // Create the ZIP file
                ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(tmpKmzFile));

                Rectangle2D.Double maxBounds = new Rectangle2D.Double();
                kmzUtil.initKML();




                FileScanner scannedFile = new FileScanner();
                FileScanner nextScannedFile = new FileScanner();




                // 1. Check if this is a file animation or dimension animation within a file
                if (viewer.getGridProps() != null && (
                        viewer.getGridProps().getSelectedTimeIndices().length > 1 ||
                        viewer.getGridProps().getSelectedRunTimeIndices().length > 1 ||
                        viewer.getGridProps().getSelectedZIndices().length > 1 )
                ) {
                	
                	doGridProcessing(maxBounds, kmzUtil, kmzOut, scannedFile, nextScannedFile);
                }
                else if (viewer.getRadialProps() != null &&
                		 ViewerKmzUtilities.isColladaCapable(viewer) && 
                		 kmzUtil.isDrapeOnColladaSelected()) {
                	
                	doRadialProcessing(maxBounds, kmzUtil, kmzOut, scannedFile, nextScannedFile);
                }
                else {
                	doSimpleUrlListProcessing(maxBounds, kmzUtil, kmzOut, scannedFile, nextScannedFile);
                }             

                progressBar.setString("Finalizing...");
                progressBar.setValue(100);

                kmzUtil.finishKML();

                // Write KML
                kmzOut.putNextEntry(new ZipEntry("wct.kml"));
                byte[] kmlBytes = kmzUtil.getKML().getBytes();
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

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(progressFrame, "KMZ Output Error: "+e.getMessage(),
                        "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (hideViewer) {
                    viewer.setVisible(true);
                    if (framesToHide != null) {
                        for (int i=0; i<framesToHide.length; i++) {
                            if (framesToHide[i] != null) {
                                framesToHide[i].setVisible(framesToHideVisibility[i]);
                            }
                        }
                    }
                }


                //             viewer.getMapSelector().getWMSPanel().setSelectedWMS(0, selectedWmsNames[0]);
                //             viewer.getMapSelector().getWMSPanel().setSelectedWMS(1, selectedWmsNames[0]);
                //
                //             viewer.setNexradTransparency(nexradTrans);
                //
                //             viewer.setLegendVisibility(legendVis);
                //             viewer.setGridSatelliteLegendVisibility(isSatLegendVisible);

                //             viewer.getMapSelector().setRadarLegend(radLegendType);
                //             viewer.getMapSelector().setSatelliteLegend(satLegendType);             

                if (viewer.getMapSelector().getLastIsolatedDataType() == DataType.RADAR) {
                    viewer.getMapSelector().isolateRadar();
                }
                else if (viewer.getMapSelector().getLastIsolatedDataType() == DataType.SATELLITE) {
                    viewer.getMapSelector().isolateGridSatellite(true);
                }
                else if (viewer.getMapSelector().getLastIsolatedDataType() == DataType.GRIDDED) {
                    viewer.getMapSelector().isolateGridSatellite(false);
                }
            }


            if (autoOpenKMZ) {
                try {
                    Desktop.getDesktop().open(outFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(progressFrame, "Open Error: "+e.getMessage(),
                            "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
                }
            }
            else { 

            	processTime = (long)((System.currentTimeMillis() - processTime)*.001) + 1;

            	long processTimeMin = processTime / 60;
            	long processTimeSec = processTime % 60;
            	String message = "Export Processing Time: "+processTimeMin + " min "+
            	processTimeSec+" sec ("+processTime+"s)\n\n"+
            	"Open '"+outFileString+"'?";

            	int openChoice = JOptionPane.showOptionDialog(progressFrame, message,
            			"ANIMATION EXPORT COMPLETE", JOptionPane.YES_NO_OPTION, 
            			JOptionPane.INFORMATION_MESSAGE, 
            			new ImageIcon(WCTViewer.class.getResource("/icons/ge-icon.png")), null, null);
            	if (openChoice == JOptionPane.YES_OPTION) {
            		try {
            			Desktop.getDesktop().open(outFile);
            		} catch (IOException e) {
            			e.printStackTrace();
            			JOptionPane.showMessageDialog(progressFrame, "Open Error: "+e.getMessage(),
            					"WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
            		}
            	}
            }
            
            
        }
        else {
            unhideViewer();
        }
        
        
        updateMemoryProgressBar();
        viewer.getMapSelector().setGridAutoMinMax(autoMinMax);
        viewer.getDataSelector().checkCacheStatus();

        progressFrame.dispose();

        if (dimension != null) {
            viewer.setSize(holdDim);
        }

        viewer.updateMemoryLabel();


        System.gc();

        viewer.removeRenderCompleteListener(renderCompleteListener);

        viewer.setIsLoading(false);
    }
    
    
    
    
    
    
    private void processFile(Rectangle2D.Double maxBounds, int urlIndex, 
    		FileScanner scannedFile, FileScanner nextScannedFile, 
    		ViewerKmzUtilities kmzUtil, ZipOutputStream kmzOut, 
    		boolean addShadowImages, int sweepsProcessed, int numOfSweeps) 
    throws IOException, MismatchedDimensionException, IllegalAccessException, 
    InstantiationException, FactoryException, TransformException, WCTException, ParseException {
    
    	processFile(maxBounds, urlIndex, scannedFile, nextScannedFile, 
    			kmzUtil, kmzOut, addShadowImages, sweepsProcessed, numOfSweeps, false);
    }
    
    private void processFile(Rectangle2D.Double maxBounds, int urlIndex, 
    		FileScanner scannedFile, FileScanner nextScannedFile, 
    		ViewerKmzUtilities kmzUtil, ZipOutputStream kmzOut, 
    		boolean addShadowImages, int sweepsProcessed, int numOfSweeps,
    		boolean isRadialVolume) 
    throws IOException, IllegalAccessException, 
    		InstantiationException, MismatchedDimensionException, FactoryException, 
    		TransformException, WCTException, ParseException {
    	
        try {

            Rectangle2D.Double bounds = viewer.getCurrentExtent();
            maxBounds.add(bounds);

            // ------------------------------------------
            // 1) Generate Image Frame
            // ------------------------------------------
            //                     BufferedImage image = viewer.getMapPaneBufferedImage(urlsToLoad[n], true, true, individualBar.getModel());
            viewer.loadAnimationFile(urlsToLoad[urlIndex], individualBar.getModel());

//            scannedFile = viewer.getFileScanner();
//            if (urlIndex < urlsToLoad.length-1) {
//                nextScannedFile.scanURL(urlsToLoad[urlIndex+1]);
//            }
//            else {
//                nextScannedFile = null;
//            }
//            // set these equal if we only have one frame
//            if (urlsToLoad.length == 1) {
//                nextScannedFile = scannedFile;
//            }

//            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED && 
//                    scannedFile.getLastScanResult().getTimestamp() == null) {
            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED ||
                    scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {

                Date startDate = viewer.getGridDatasetRaster().getLastProcessedDateTime();
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
                    this.altitude = kmzUtil.getGridAltitude(zIndices[0]);
                        altMode = AltitudeMode.ABSOLUTE;
                }
                else if (! Double.isNaN(this.altitude)) {
                    altMode = AltitudeMode.ABSOLUTE;
                }

                
//                AltitudeMode altMode = AltitudeMode.CLAMPED_TO_GROUND;
//                // 1st, check for valid real z height if 'real' height is selected
//                if (zIndices.length > 0 && Double.isNaN(this.altitude)) {
//                    this.altitude = kmzUtil.getGridAltitude(zIndices[0]);
//                    if (this.altitude > 1000) {
//                        altMode = AltitudeMode.ABSOLUTE;
//                    }
//                    else {
//                        altMode = AltitudeMode.CLAMPED_TO_GROUND;
//                    }
//                }
//                else if (! Double.isNaN(this.altitude)) {
//                    altMode = AltitudeMode.ABSOLUTE;
//                }

                String uniqueString = String.valueOf(System.currentTimeMillis()%100000);
                frameName = frameName+"-"+uniqueString;
                
                kmzUtil.addFrameToKMZ(kmzOut, frameName, frameName+".png", 
                        startDate, null, bounds, altMode, this.altitude, 
                        (altMode == AltitudeMode.CLAMPED_TO_GROUND) ? false : addShadowImages);
                
                
            }
            else {
            	if (ViewerKmzUtilities.isColladaCapable(viewer) && kmzUtil.isDrapeOnColladaSelected()) {
            		// special check to improve optimization -- pretty sloppy way to check, but will work for Radial data
            		if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//            			System.out.println("MAX VAL OF RADIAL RASTER:::::::::::: "+viewer.getRadialRemappedRaster().getMaxValue());
//            			System.out.println("MIN VAL OF RADIAL RASTER:::::::::::: "+viewer.getRadialRemappedRaster().getMinValue());
//            			System.out.println("ABS DIFF VAL: "+Math.abs(viewer.getRadialRemappedRaster().getMaxValue() - viewer.getRadialRemappedRaster().getMinValue()) );
            			if (Math.abs(viewer.getRadialRemappedRaster().getMaxValue() - viewer.getRadialRemappedRaster().getMinValue()) > 0.01 &&
            				Math.abs(viewer.getRadialRemappedRaster().getMaxValue() - viewer.getRadialRemappedRaster().getMinValue()) < 1000000) {
                    		kmzUtil.addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds, AltitudeMode.DRAPE_ON_MODEL, this.altitude, 
                    				addShadowImages, (! isRadialVolume) || sweepsProcessed == 0);
            			}
            		}
            		else {
                		kmzUtil.addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds, AltitudeMode.DRAPE_ON_MODEL, this.altitude, addShadowImages);                     
            		}
            		
//            		kmzUtil.addFrameToKMZ(kmzOut, scannedFile, scannedFile, bounds, AltitudeMode.DRAPE_ON_MODEL, this.altitude, addShadowImages);                     
            	}            	
                // Append to KML
            	else if (! Double.isNaN(this.altitude)) {
                    kmzUtil.addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds, AltitudeMode.ABSOLUTE, this.altitude, addShadowImages);                         
                }
                else {
                    kmzUtil.addFrameToKMZ(kmzOut, scannedFile, nextScannedFile, bounds);
                }
            }

            // do auto min/max the first time, if selected, then don't for every other image
            viewer.getMapSelector().setGridAutoMinMax(false);
            

            // Update progress bars
            updateMemoryProgressBar();
            
            int percent = (int)(100.0*WCTUtils.progressCalculator(new int[]{urlIndex, sweepsProcessed+1}, new int[]{urlsToLoad.length, numOfSweeps}));
            
//            int percent = ((int) (100 * (((double) n+1) / urlsToLoad.length)));
            progressBar.setValue(percent);
            if (numOfSweeps <= 1) {
            	progressBar.setString("Processed "+(urlIndex+1)+"/"+urlsToLoad.length+" Files (" + percent + " %)");
            }
            else {
            	progressBar.setString("Processed "+(sweepsProcessed+1)+"/"+numOfSweeps+" Sweeps and "+
            			(urlIndex)+"/"+urlsToLoad.length+" Files (" + percent + " %)");
            }
            progressFrame.setTitle("("+percent+" %) Export Animation Progress");




            // otherwise do nothing (for NO)
        } catch (Exception nde) {
            nde.printStackTrace();
            JOptionPane.showMessageDialog(progressFrame, (Object) nde.toString()+"\n URL: "+urlsToLoad[urlIndex],
                    "NEXRAD DECODE EXCEPTION", JOptionPane.ERROR_MESSAGE);
        }

    }
    
    
    

    // END METHOD run()

    private void unhideViewer() {
        if (hideViewer) {
            viewer.setVisible(true);
            if (framesToHide != null) {
                for (int i=0; i<framesToHide.length; i++) {
                    if (framesToHide[i] != null) {
                        framesToHide[i].setVisible(framesToHideVisibility[i]);
                    }
                }
            }
        }
    }
    
    private void returnToViewer() {
        progressFrame.dispose();
        unhideViewer();
        
    }
    
    

    /**
     *  Description of the Method
     */
    public void endThread() {
        endThread = true;
    }

    private void updateMemoryProgressBar() {
        Runtime r = Runtime.getRuntime();
        memBar.setValue((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0));
        memBar.setString("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0)) + " %");
    }

    private void updateProgress(int percent) {
        progressBar.setValue(percent);
        progressBar.setString(percent+" % -- Animation Progress");
        progressFrame.setTitle("("+percent+" %) Animation Progress");
    }

    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            endThread = true;
            progressBar.setString("--- Canceling ---");
        }
    }


    
    
    private void doSimpleUrlListProcessing(Rectangle2D.Double maxBounds, 
    		ViewerKmzUtilities kmzUtil, ZipOutputStream kmzOut,
    		FileScanner scannedFile, FileScanner nextScannedFile) 
    throws IllegalAccessException, InstantiationException, IOException, 
    	DecodeException, SQLException, ParseException, 
    	MismatchedDimensionException, FactoryException, 
    	TransformException, WCTException {

        for (int n=0; n<urlsToLoad.length; n++) {

            scannedFile = viewer.getFileScanner();
            if (n < urlsToLoad.length-1) {
                nextScannedFile.scanURL(urlsToLoad[n+1]);
            }
            else {
                nextScannedFile = null;
            }
            // set these equal if we only have one frame
            if (urlsToLoad.length == 1) {
                nextScannedFile = scannedFile;
            }
            
        	processFile(maxBounds, n, scannedFile, nextScannedFile, kmzUtil, kmzOut, createShadowImages, 0, 1);
        	
            if (endThread) {
                n = urlsToLoad.length;
            }
        }

    }
    
    
    private void doGridProcessing(Rectangle2D.Double maxBounds, 
    		ViewerKmzUtilities kmzUtil, ZipOutputStream kmzOut,
    		FileScanner scannedFile, FileScanner nextScannedFile) 
    throws IllegalAccessException, InstantiationException, IOException, 
    	DecodeException, SQLException, ParseException, 
    	MismatchedDimensionException, FactoryException, 
    	TransformException, WCTException {


        int gridIndex = viewer.getGridProps().getSelectedGridIndex();


        boolean doTimeDimension = false;
        boolean doRuntimeDimension = false;
        boolean doZDimension = false;


        int[] timeIndices = Arrays.copyOf(viewer.getGridProps().getSelectedTimeIndices(), 
                viewer.getGridProps().getSelectedTimeIndices().length);

        System.out.println("time indices: "+Arrays.toString(timeIndices));

        if (timeIndices != null && timeIndices.length > 0) {
            doTimeDimension = true;
        }
        else {
            timeIndices = new int[] { 0 };
        }

        for (int t=0; t<timeIndices.length; t++) {

            int[] runtimeIndices = Arrays.copyOf(viewer.getGridProps().getSelectedRunTimeIndices(), 
                    viewer.getGridProps().getSelectedRunTimeIndices().length);

            System.out.println("runtime indices: "+Arrays.toString(runtimeIndices));

            if (runtimeIndices != null && runtimeIndices.length > 0) {
                doRuntimeDimension = true;
            } 
            else {
                runtimeIndices = new int[] { 0 };
            }

            for (int rt=0; rt<runtimeIndices.length; rt++) {

                int[] zIndices = Arrays.copyOf(viewer.getGridProps().getSelectedZIndices(), 
                        viewer.getGridProps().getSelectedZIndices().length);

                System.out.println("z indices: "+Arrays.toString(zIndices));

                if (zIndices != null && zIndices.length > 0) {
                    doZDimension = true;
                }
                else {
                    zIndices = new int[] { 0 };
                }

                for (int z=0; z<zIndices.length; z++) {

                    int percent = (int) (100.0 * WCTUtils.progressCalculator(new int[] { t, rt, z }, 
                            new int[] { timeIndices.length, runtimeIndices.length, zIndices.length }) );

                    updateProgress(percent);

                    System.out.println("animation setting grid index to: "+gridIndex);
                    viewer.getGridDatasetRaster().setGridIndex(gridIndex);
                    if (doRuntimeDimension) {
                        System.out.println("animation setting runtime index to: "+runtimeIndices[rt]);
                        viewer.getGridDatasetRaster().setRuntimeIndex(runtimeIndices[rt]);
                    }
                    if (doTimeDimension) {
                        System.out.println("animation setting time index to: "+timeIndices[t]);
                        viewer.getGridDatasetRaster().setTimeIndex(timeIndices[t]);
                    }
                    if (doZDimension) {
                        System.out.println("animation setting z index to: "+zIndices[z]);
                        viewer.getGridDatasetRaster().setZIndex(zIndices[z]);
                    }





                    //                             viewer.loadFile(viewer.getCurrentDataURL(), false, false, true, false);
                    //                             BufferedImage image = viewer.getViewerBufferedImage();


                    Rectangle2D.Double bounds = viewer.getCurrentExtent();
                    maxBounds.add(bounds);

                    viewer.loadAnimationFile(viewer.getCurrentDataURL(), individualBar.getModel());

                    scannedFile.scanURL(viewer.getCurrentDataURL());
                    nextScannedFile = scannedFile;

                    String saveName = viewer.getFileScanner().getSaveName();
                    if (saveName == null || saveName.trim().length() == 0) {
                        saveName = viewer.getFileScanner().getLastScanResult().getFileName();
                    }

                    String gridName = viewer.getGridDatasetRaster().getLastProcessedGridDataset().getGrids().
                    	get(viewer.getGridProps().getSelectedGridIndex()).getName();
                    String frameName = saveName+"-var_"+gridName+"-t"+t+"-rt_"+rt+"-z_"+z;


                    // Append to KML
                    Date startDate = viewer.getGridDatasetRaster().getLastProcessedDateTime();
                    Date endDate = null;

                    if (doTimeDimension) {

                        startDate = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getTimeAxis1D().getTimeDate( 
                                viewer.getGridDatasetRaster().getTimeIndex()
                        );
                        // last timestep
                        if (t == timeIndices.length - 1) {
                            endDate = null;
                        }
                        else {
                            endDate = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getTimeAxis1D().getTimeDate( 
                                    timeIndices[t+1]
                            );
                        }
                    }

                    if (doRuntimeDimension) {

                        startDate = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getRunTimeAxis().getTimeDate( 
                                viewer.getGridDatasetRaster().getRuntimeIndex()
                        );
                        // last timestep
                        if (rt == runtimeIndices.length - 1) {
                            endDate = null;
                        }
                        else {
                            endDate = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getRunTimeAxis().getTimeDate( 
                                    runtimeIndices[rt]
                            );
                        }
                    }

                    AltitudeMode altMode = AltitudeMode.CLAMPED_TO_GROUND;
                    // 1st, check for valid real z height if 'real' height is selected
                    if (doZDimension && Double.isNaN(this.altitude)) {
                        this.altitude = kmzUtil.getGridAltitude(zIndices[0]);
                        if (this.altitude > 1000) {
                            altMode = AltitudeMode.ABSOLUTE;
                        }
                        else {
                            altMode = AltitudeMode.CLAMPED_TO_GROUND;
                        }
                    }
                    else if (! Double.isNaN(this.altitude)) {
                        altMode = AltitudeMode.ABSOLUTE;
                    }
                    
                    kmzUtil.addFrameToKMZ(kmzOut, frameName, frameName+".png", 
                            startDate, null, bounds, altMode, this.altitude, 
                            (altMode == AltitudeMode.CLAMPED_TO_GROUND) ? false : createShadowImages);

                    // do auto min/max the first time, if selected, then don't for every other image
                    viewer.getMapSelector().setGridAutoMinMax(false);
                    
                    
                    // End loop if needed
                    if (endThread) {
                        z = zIndices.length;
                        rt = runtimeIndices.length;
                        t = timeIndices.length;
                        updateProgress(100);
                    }


                }  
            }
        }
    }
    
    
    private void doRadialProcessing(Rectangle2D.Double maxBounds, 
    		ViewerKmzUtilities kmzUtil, ZipOutputStream kmzOut,
    		FileScanner scannedFile, FileScanner nextScannedFile) 
    throws IllegalAccessException, InstantiationException, IOException, 
    	DecodeException, SQLException, ParseException, 
    	MismatchedDimensionException, FactoryException, 
    	TransformException, WCTException {
    	
        RadialDatasetSweep.RadialVariable var = 
        	(RadialDatasetSweep.RadialVariable)viewer.getRadialHeader().getRadialDatasetSweep().getDataVariable(
        			viewer.getRadialProps().getRadialPropsPanel().getVariableName()
        		);
        String sweepsToProcessString = kmzExportDialog.getNumberOfSweepsToProcess();
        int sweepsToProcess;
        if (sweepsToProcessString.toUpperCase().startsWith("ALL")) {
        	sweepsToProcess = var.getNumSweeps();
        }
        else {
        	sweepsToProcess = Integer.parseInt(sweepsToProcessString.split(" ")[0]);
        }
        

        
        int currentSweep = viewer.getRadialProps().getRadialPropsPanel().getCut();
        double currentRadarSmoothFactor = viewer.getRadarSmoothFactor();
        int currentRadarTransparency = viewer.getRadarTransparency();

        WCTFilter filter = null;
        if (kmzExportDialog.isCustomSettingsEngaged()) {
        	filter = new WCTFilter();
        	filter.setMinValue(kmzExportDialog.getRadialMinValueCustom());
        	viewer.setRadarSmoothFactor(kmzExportDialog.getRadarSmoothingFactor());
        	viewer.setRadarTransparency(255-(int)Math.round((kmzExportDialog.getRadarTransparencyCustom()/100.0) * 255));
        }
  
//      	progressBar.setString("Processed 0/"+sweepsToProcess+" Sweeps and 0/"+urlsToLoad.length+" Files (0 %)");
      	                    
        int totalSweepsToProcess = sweepsToProcess;
        for (int n=0; n<urlsToLoad.length; n++) {
        	

            scannedFile = viewer.getFileScanner();
            if (n < urlsToLoad.length-1) {
                nextScannedFile.scanURL(urlsToLoad[n+1]);
            }
            else {
                nextScannedFile = null;
            }
            // set these equal if we only have one frame
            if (urlsToLoad.length == 1) {
                nextScannedFile = scannedFile;
            }
        	
            List<Integer> sweepsToUseList = new ArrayList<Integer>();
    	
            int sweepIndex = 0;
            int sweepsProcessed = 0;
        	while (sweepsProcessed < totalSweepsToProcess) {
        
        		// if we are only processing one sweep, then process the current sweep selected - don't reset to 0
//        		if (sweepsToProcess > 1) {
//        			viewer.getRadialProps().getRadialPropsPanel().setCut(sweepIndex);
//        		}
       
                
                
        		// check if next elevation angle is the same or close to the same.
        		// if so, then skip it
             	double[] elevAngles = viewer.getRadialProps().getRadialPropsPanel().getElevations();
        		if (sweepIndex > 0) {
        			while (Math.abs(elevAngles[sweepIndex-1]-elevAngles[sweepIndex]) < .3 && sweepIndex < elevAngles.length-1) {
         				sweepIndex++;
        			}
        		}
				sweepsToUseList.add(sweepIndex);
	                   		
        		
        		
        		
System.out.println("KMZ COLLADA EXPORT: PROCESSING sweep="+sweepIndex+"  elev="+elevAngles[sweepIndex]);                    		

        		
//        		processFile(maxBounds, n, scannedFile, nextScannedFile, kmzUtil, kmzOut, (sweepIndex==0), sweepsProcessed++, totalSweepsToProcess, (totalSweepsToProcess > 0));

        		sweepIndex++;
        		
        		// break loop if we have advanced to the top sweep
        		if (sweepIndex >= elevAngles.length) {
        			sweepsProcessed = totalSweepsToProcess;
        		}
        	}
    		

        	double[] elevAngles = viewer.getRadialProps().getRadialPropsPanel().getElevations();
        	for (int i=0; i<elevAngles.length; i++) {
    			System.out.print(i+" : "+elevAngles[i]);
    			if (sweepsToUseList.contains(i)) {
    				System.out.print("  USED");
    			}
    			System.out.println();
    		}
        	
        	if (sweepsToUseList.size() > sweepsToProcess) {
        		sweepsToUseList = sweepsToUseList.subList(0, sweepsToProcess);
        	}
//    		System.out.println(sweepsToUseList);
//    		if (true) {
//    			return;
//    		}
    		
        	if (n == 0) {
        		if (sweepsToUseList.size() > 1) {
        			progressBar.setString("Processed 0/"+sweepsToUseList.size()+" Sweeps and 0/"+urlsToLoad.length+" Files (0 %)");
        		}
        		else {
        			progressBar.setString("Processed 0/"+urlsToLoad.length+" Files (0 %)");
        		}        		
        	}
          	
    		for (int s=0; s<sweepsToUseList.size(); s++) {
    			viewer.getRadialProps().getRadialPropsPanel().setCut(sweepsToUseList.get(s));
    		     
        		// set filter if a special filter is set for just this animation
        		if (filter != null) {
        			viewer.setAnimationFilter(filter);
        		}

                if (endThread) {
                	s = 100000;
                    n = urlsToLoad.length;
                    break;
                }
        		
    			processFile(maxBounds, n, scannedFile, nextScannedFile, kmzUtil, kmzOut, (sweepIndex==0), 
    					s, sweepsToUseList.size(), (totalSweepsToProcess > 1));	
    		}
    				 
    		
    		
    		
    		

        	// create metadata legend image and add to kmz
//    		if (totalSweepsToProcess > 1) {
//    			
//    		
//            String frameMetaName = "meta-"+viewer.getFileScanner().getSaveName()+".png";
//            kmzUtil.addKMZImageEntry(frameMetaName, kmzUtil.getMetaBufferedImage(true), kmzOut);
//
//            // add reference to the metadata legend image in the kml
//            String kmlTimeString = null;
//            String nextKmlTimeString = null;
//            
//            try {
//            	kmlTimeString = ViewerKmzUtilities.ISO_DATE_FORMATTER.format( 
//            		ViewerKmzUtilities.FILE_SCANNER_FORMATTER.parse(scannedFile.getLastScanResult().getTimestamp()) 
//            	);
//            	nextKmlTimeString = (nextScannedFile != null) 
//            		? ViewerKmzUtilities.ISO_DATE_FORMATTER.format(
//            				ViewerKmzUtilities.FILE_SCANNER_FORMATTER.parse(nextScannedFile.getLastScanResult().getTimestamp()
//            			)) 
//            		: null;
//            } catch (Exception e) {
//            	// catch time formatting errors
//            	;
//            }
//            kmzUtil.addScreenOverlay(kmzUtil.getMetadataOverlay(frameMetaName, kmlTimeString, nextKmlTimeString, n, frameMetaName));
//            
//    		}
        }
        
        
        boolean processToReset = false;
        if (viewer.getRadialProps().getRadialPropsPanel().getCut() != currentSweep) {
			viewer.getRadialProps().getRadialPropsPanel().setCut(currentSweep);
			processToReset = true;
        }
        if (viewer.getRadarSmoothFactor() != currentRadarSmoothFactor) {
        	viewer.setRadarSmoothFactor(currentRadarSmoothFactor);
        	processToReset = true;
        }
        if (viewer.getRadarTransparency() != currentRadarTransparency) {
        	viewer.setRadarTransparency(currentRadarTransparency);
        	processToReset = true;
        }
        
        
        if (processToReset) {
//        	processFile(maxBounds, 0, scannedFile, nextScannedFile, kmzUtil, kmzOut, false, totalSweepsToProcess-1, totalSweepsToProcess);
          	progressBar.setString("Finishing...");
			viewer.loadAnimationFile(urlsToLoad[0], individualBar.getModel());
        }

    }
    
    
    
    
    
    
    
    
    
    
	public void setKmzExportDialog(KmzExportDialog kmzExportDialog) {
		this.kmzExportDialog = kmzExportDialog;
	}

	public KmzExportDialog getKmzExportDialog() {
		return kmzExportDialog;
	}

	public void setAutoOpenKMZ(boolean autoOpen) {
		this.autoOpenKMZ = autoOpen;
	}


  }

