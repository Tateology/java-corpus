package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.ui.AWTImageExport;
import gov.noaa.ncdc.wct.ui.WCTMapPane;
import gov.noaa.ncdc.wms.WMSData;
import gov.noaa.ncdc.wms.WMSException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.StyledMapRenderer;

public class DataImageProducer {

    
    
    
    public static void getDataImage(URL dataURL, Rectangle2D.Double extent) throws WMSException, Exception {
        
//        System.setProperty("java.awt.headless", "true"); 
        
        BufferedImage bimage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bimage.getGraphics();
        
        WCTMapPane wCTMapPane = new WCTMapPane();
        wCTMapPane.setBackground(new Color(0, 0, 55));
        wCTMapPane.setMagnifierGlass(wCTMapPane.getBackground());
        wCTMapPane.setDoubleBuffered(false);
        
        wCTMapPane.setBounds(new Rectangle(500, 500));
        
        wCTMapPane.setVisibleArea(extent);
        
        
        
//        ((StyledMapRenderer) mapPane.getRenderer()).addLayer(nexview.get);

        wCTMapPane.paint(g);
        
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    public static void testMap() throws WMSException, Exception {
        
  //      System.setProperty("java.awt.headless", "true"); 
        
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


    
    
    
    
    
//    
//    public static FeatureCollection getDecodedFeatures(URL nexradURL) {
//
//        private boolean useNexradCache = true;
//        
//        
//        FileScanner scannedFile = new FileScanner();
//    
//        // Transfer file to local tmp area
//        //nexradURL = NexradTransfer.getURL(nexradURL);
//
//        
//        try {  
//
//            
//
//            scannedFile.scanURL(nexradURL);
//            // Check for file compression
//            if (scannedFile.isUNIXZLevel2()) {
//                nexradURL = Level2Transfer.getNCDCLevel2UNIXZ(nexradURL, ! useNexradCache);
//                scannedFile.scanURL(nexradURL);
//                System.out.println("DOWNLOADING .Z : "+nexradURL);               
//            }
//            else if (scannedFile.isGZIPLevel2()) {
//                nexradURL = Level2Transfer.getNCDCLevel2GZIP(nexradURL, ! useNexradCache);
//                scannedFile.scanURL(nexradURL);
//                System.out.println("DOWNLOADING .GZ : "+nexradURL);               
//            }
//            else if (scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2_AR2V0001) {
//                nexradURL = NexradTransfer.getURL(nexradURL, ! useNexradCache);  
//                // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
//                nexradURL = Level2Transfer.decompressAR2V0001(nexradURL, ! useNexradCache);
//                scannedFile.scanURL(nexradURL);
//            }
////            else if (scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_SIGMET) {
////                
////                String fileString = NDITConstants.TMP_LOCATION+File.separator+scannedFile.getLastScanResult().getFileName()+"_radial.nc";
////                if (! new File(fileString).exists()) {
////                    try {
////                        NetcdfFile ncIn = NetcdfFile.open(nexradURL.toString());
////                        ucar.nc2.FileWriter.writeToFile(ncIn, fileString);
////                        nexradURL = new File(fileString).toURI().toURL();
////                    } catch (Exception e) {
////                        new File(fileString).delete();
////                        e.printStackTrace();
////                        throw new NexradExportException("Raw Netcdf radial cache is not supported for this product.");
////                    }
////                }
////
////            }
//            else {
//                // Transfer file to local tmp area -- force overwrite if NWS
//                if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {
//                    nexradURL = NexradTransfer.getURL(nexradURL, true);
//                }               
//                else {
//                    nexradURL = NexradTransfer.getURL(nexradURL, ! useNexradCache);
//                }
//
//
//                scannedFile.scanURL(nexradURL);
//            }
//        } catch (java.net.ConnectException ce) {
//            ce.printStackTrace();
//            return null;
//        }
//
//
//
//
//
//        String urlString = nexradURL.toString();
//        int index;
//        if (urlString.indexOf("/") == -1) {
//            // windows format
//            index = urlString.lastIndexOf((int) '\\');
//        }
//        else {
//            index = urlString.lastIndexOf((int) '/');
//        }
//        String nexradFile = urlString.substring(index + 1, urlString.length());
//
//        System.out.println("NEXRAD URL: " + nexradURL);
//        System.out.println("NEXRAD FILE: " + nexradFile);
//
//
//
//
//
////        if (radialDataset == null) {
////            radialDataset = new DecodeL2Header();
////        }
//        //if (level2ITRHeader == null) {
//        //   level2ITRHeader = new DecodeL2ITRHeader();
//        //}
//        
//        DecodeL3Header level3Header;
//        DecodeXMRGHeader xmrgHeader;
//        DecodeQ2 q2Decoder;
//        GoesRemappedRaster goesAreaRaster;
//        
//        
//        if (level3Header == null) {
//            level3Header = new DecodeL3Header();
//        }
//        if (xmrgHeader == null) {
//            xmrgHeader = new DecodeXMRGHeader();
//        }
//        if (q2Decoder == null) {
//            q2Decoder = new DecodeQ2();
//        }
//        if (goesAreaRaster == null) {
//            goesAreaRaster = new GoesRemappedRaster();
//            goesAreaRaster.addDataDecodeListener(this);
//        }
//
//
//
//
//
//
//
//
//
//
//
//        
//        
//        
//        
//        
//        
//        
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.SATELLITE_GOES_AREAFILE) {
//            try {
//
//                
////                goesAreaRaster.process(nexradURL.toString(), getCurrentExtent());
////                setSatelliteGridCoverage(goesAreaRaster.getGridCoverage());
////                statusBar.setProgressText("");
//                
//                if (satelliteLegend == null) {
//                    satelliteLegend = new RenderedLogo();
//                    satelliteLegend.setPosition(LegendPosition.SOUTH_EAST);
//                    satelliteLegend.setZOrder(400.1f);                    
//                    mapPane.getRenderer().addLayer(satelliteLegend);
//                }
//
//                if (mapSelect == null) {
//                    mapSelect = new MapSelector(this);
//                }
//                statusBar.setNexradHeader(null);
//                if (radialProp != null) {
//                    radialProp.setVisible(false);
//                }
//                mapSelect.isolateSatellite();
//                refreshSatellite();
//                
//                
//                Dimension imageDim = new Dimension(240, 65);
//                
//                satLegendProducer.setDataType("MCIDAS AREA FILE");
//                satLegendProducer.setDataDescription(new String[] {"GOES SATELLITE"});
//                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
//                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//                satLegendProducer.setDateTimeInfo(dateFormat.format(goesAreaRaster.getDateInMilliseconds())+" GMT");
//                
//
////                Color[] catColors = LegendCategoryFactory.getCategoryColors(goesAreaRaster, 16);
////                String[] catLabels = LegendCategoryFactory.getCategoryLabels(goesAreaRaster, 16);
//                Color[] catColors = LegendCategoryFactory.getCategoryColors(goesAreaRaster);
//                String[] catLabels = LegendCategoryFactory.getCategoryLabels(goesAreaRaster);
//                satLegendProducer.setCategoryColors(catColors);
//                satLegendProducer.setCategoryLabels(catLabels);
//                satLegendProducer.setInterpolateBetweenCategories(true);
//                
//                if (goesAreaRaster.getLastBandDecoded() == Band.BAND1) {
//                    satLegendProducer.setLegendTitle(new String[] {scannedFile.getLastScanResult().getDisplayName() });
//                    satLegendProducer.setDrawColorMap(false);
//                    satLegendProducer.setDrawLabels(false);
////                    satLegendProducer.setLabelEveryOtherN(40);
//                    imageDim = new Dimension(240, 14);
//                }
//                else {
//                    satLegendProducer.setLegendTitle(new String[] {scannedFile.getLastScanResult().getDisplayName(), 
//                            "Brightness Temp. ("+goesAreaRaster.getUnits()+")"});
//                    satLegendProducer.setDrawColorMap(true);
//                    satLegendProducer.setDrawLabels(true);
//                    satLegendProducer.setLabelEveryOtherN(20);
//                }
//                
//                
//                satLegendProducer.setBackgroundColor(new Color(220, 220, 220, 180));
//                satLegendProducer.setMapBackgroundColor(this.getBackgroundColor());
//                satLegendProducer.setDrawBorder(true);
//                lastDecodedLegendProducer = satLegendProducer;
//                
//                Image image = satLegendProducer.createMediumLegendImage(imageDim);
//    
//                satelliteLegend.setInsets(new Insets(0, 0, 20, image.getWidth(this)));
//                satelliteLegend.setImage(image);
//                
//                satelliteLegend.setVisible(isSatelliteLegendVisible);
//
//                
//                satelliteLegend.repaint();
//                mapPane.repaint();
//                
//                return true;
//                
//                
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(this, (Object) e.toString()+"\n URL: "+nexradURL+"\n MESSAGE: "+e.getMessage(),
//                    "GOES DECODE EXCEPTION", JOptionPane.ERROR_MESSAGE);
//                statusBar.setProgressText("");
//                this.satelliteURL = null;
//                return false;
//            }
//        }
//        else {
//            if (mapSelect == null) {
//                mapSelect = new MapSelector(this);
//            }
//            mapSelect.isolateRadar();
//            lastDecodedLegendProducer = radLegendProducer;
//        }
//                
//        
//        
//        
//        
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.GRIDDED_NEXRAD_Q2_3D || 
//                scannedFile.getLastScanResult().getFileType() == FileScanner.GRIDDED_NEXRAD_Q2_2D ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.GRIDDED_NEXRAD_Q2_PCP
//        ) {
//
//
//
//            nxZoomChange.setNexradActive(false);
//
//            statusBar.setProgressText("Rendering NetCDF");
//
//
//            q2Decoder.decodeData(nexradURL);
//            q2Props.refreshVariableList(q2Decoder);
//            q2Props.refreshHeightList(q2Decoder);
//            //q2Props.pack();
//
//
//            if (! q2Props.isVisible()) {
//                q2Props.setVisible(true);
//                q2Props.setLocation(this.getLocation().x+25, this.getLocation().y+25);
//                q2Props.setExtendedState(Frame.NORMAL);
//
//            }
//
//            nexradRGC.setGridCoverage(q2Decoder.getGridCoverage(q2Props.getSelectedVariableName(), q2Props.getSelected3DHeightIndex(), nexradAlphaChannelValue));
//            nexradRGC.setVisible(true);
//
//
//            
//            radLegendProducer.setDataType("NOAA/NSSL NMQ");
//            radLegendProducer.setDataDescription(new String[] {"NATIONAL MOSAIC", "REFLECTIVITY"});
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
//            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//            radLegendProducer.setDateTimeInfo(dateFormat.format(q2Decoder.getCreationDate())+" GMT");
//            
//            radLegendProducer.setSpecialMetadata(new String[] {
//                    "Variable: "+q2Decoder.getLastDecodedVariableName(),
//                    "Height Level: "+((q2Decoder.getLastDecodedHeightLevel() < 0) ? "N/A" : 
//                        String.valueOf(q2Decoder.getLastDecodedHeightLevel())),
//                    "Height: "+((q2Decoder.getLastDecodedHeightValue() < 0) ? "N/A" : 
//                        fmt2.format(q2Decoder.getLastDecodedHeightValue())+" "+q2Decoder.getLastDecodedHeightUnits())
//            });
//            radLegendProducer.setLegendTitle(new String[] {"Legend: "+q2Decoder.getLastDecodedUnits()});
//            
//
//            Color[] catColors = LegendCategoryFactory.getCategoryColors(q2Decoder);
//            String[] catLabels = LegendCategoryFactory.getCategoryLabels(q2Decoder);
//            radLegendProducer.setCategoryColors(catColors);
//            radLegendProducer.setCategoryLabels(catLabels);
//            radLegendProducer.setInterpolateBetweenCategories(true);
//            keyPanel.setLegendImage(radLegendProducer);
//            
//            
//            
//            keyPanel.validate();
//            keyPanel.repaint();
//            
////            ((Q2Legend)q2Props.getKeyPanel()).setUnits(q2Decoder.getLastDecodedUnits());
////            ((Q2Legend)q2Props.getKeyPanel()).setMinVal(q2Decoder.getMinVal());
////            ((Q2Legend)q2Props.getKeyPanel()).setMaxVal(q2Decoder.getMaxVal());
////            ((Q2Legend)q2Props.getKeyPanel()).repaint();
////
////            legend = q2Props.getKeyPanel();
////            
////            if (firstQ2) {
////                infoPanel.removeAll();
////                infoPanel.add(q2Props.getMetaPanel(), BorderLayout.NORTH);
////                infoPanel.add(q2Props.getKeyPanel(), BorderLayout.CENTER);
////                firstQ2 = false;
////                firstRadar = true;
////            }
//
//
//
//            if (radialProp != null) {
//                radialProp.setVisible(false);
//            }
//
//            statusBar.setProgressText("");
//            return true;
//
//        }
////        else {
////            q2Props.setVisible(false);  
////
////            if (firstRadar) {
////                infoPanel.removeAll();
//////                infoPanel.add(metaPanel, BorderLayout.NORTH);
////                infoPanel.add(keyPanel, BorderLayout.CENTER);
////                firstQ2 = true;
////                firstRadar = false;
////            }
////
////        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        // Get header
//        System.out.println("---------------- START HEADER DECODE --------------------");
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2 || 
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2_AR2V0001 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_SIGMET) {
//            
//            
////            if (radialDataset != null) {
////                System.out.println("radialDataset URI: "+radialDataset.getLocationURI());
////                if (radialDataset.getLocationURI().equals(nexradURL.toURI().toString())) {
////                    System.out.println("URI MATCH!!!!");
////                }
////            }
////            System.out.println("nexradURL URI: "+nexradURL.toURI().toString());
//            
//            
//            if (radialDataset == null || 
//                    ! radialDataset.getLocationURI().equals(nexradURL.toURI().toString())) {
//                
//                
//                try {
//                
//                    radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
//                        DataType.RADIAL, 
//                        nexradURL.toString(), null, new StringBuffer());
//                    
//                
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new DecodeException("DECODING ERROR: "+e.getMessage(), nexradURL);
//                }
//                
//                if (radialDataset == null) {
//                    NCdump.print(NetcdfFile.open(nexradURL.toString()), "", System.out, null);
//                    
//                    throw new DecodeException("Could not create RadialDatasetSweep datatype...", nexradURL);
//                }
//                
//                
//                if (radialProp != null ) {
//                    radialProp.setRadialDatasetSweep(radialDataset);
//                }
//            }
//
//            try {
//            
//                NCdump.print(radialDataset.getNetcdfFile(), "", System.out, null);
//                
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println("CAUGHT EXCEPTION ncdump");
//            }
//
//            
//            // make a custom header object that responds to different variables
//            if (radialDatasetHeader == null) {
//                radialDatasetHeader = new DecodeRadialDatasetSweepHeader() {
//                    public short getProductCode() {
//                        String variableName = radialProp == null ? radialDataset.getDataVariables().get(0).toString() : radialProp.getVariableName();
//                        if (variableName.equals("Reflectivity")) {
//                            return NexradHeader.LEVEL2_REFLECTIVITY;
//                        }
//                        else if (variableName.equals("Total_Power")) {
//                            return NexradHeader.LEVEL2_REFLECTIVITY;
//                        }
//                        else if (variableName.equals("RadialVelocity")) {
//                            return NexradHeader.LEVEL2_VELOCITY;
//                        }
//                        else if (variableName.equals("Velocity")) {
//                            return NexradHeader.LEVEL2_VELOCITY;
//                        }
//                        else if (variableName.equals("SpectrumWidth")) {
//                            return NexradHeader.LEVEL2_SPECTRUMWIDTH;
//                        }
//                        else if (variableName.equals("Width")) {
//                            return NexradHeader.LEVEL2_SPECTRUMWIDTH;
//                        }
//                        else {
//                            return NexradHeader.UNKNOWN;
//                        }
//                    }
//                    
//                };
//            }
//            radialDatasetHeader.setRadialDatasetSweep(radialDataset);
//            
//            // if the file does not have lat/lon/site info encoded inside, set it here based on lookup table
//            
////            System.err.println(radialDatasetHeader.getICAO());
////            System.exit(1);
//            
//            if (radialDatasetHeader.getICAO().equals("XXXX")) {
//                int idx = urlString.lastIndexOf('/');
//                String icao = urlString.substring(idx+1, idx+5);
//                if (icao.equals("6500")) {
//                    icao = urlString.substring(idx+5, idx+9); 
//                }
//                
//                System.err.println("SETTING SITE MANUALLY FOR: "+icao);
//                
//                NexradHashtables nxhash = NexradHashtables.getSharedInstance();
//                radialDatasetHeader.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
//            }
//
//            
//            
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {
//            level3Header.decodeHeader(nexradURL);
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_UNSUPPORTED) {
//            JOptionPane.showMessageDialog(this, "This Level-III NEXRAD Product is not supported with the Java NEXRAD Software.",
//                    "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//            setIsLoading(false);
//            //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            //this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            return false;
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_XMRG) {
//            xmrgHeader.decodeHeader(nexradURL);
//            //setIsLoading(false);
//            //return false;
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.GRIDDED_NEXRAD_Q2_3D) {
//            System.out.println("FOUND NEXRAD_Q2_3D #1");
//        }
//        else {
//            System.out.println("DEBUG: NexradFile fileType="+scannedFile.getLastScanResult().getFileType());
//            JOptionPane.showMessageDialog(this, "This is not WSR-88D Level-II, Level-III or XMRG NEXRAD Data",
//                    "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//
//            setIsLoading(false);
//            //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            //this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            return false;
//        }
//        System.out.println("---------------- FINISH HEADER DECODE -------------------");
//
//
//
//
//        //NexradHeader header; 
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2 || 
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2_AR2V0001 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_SIGMET) {
//            header = radialDatasetHeader;
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {
//
//            header = level3Header;
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_XMRG) {
//            header = xmrgHeader;
//        }
//        else {
//            setIsLoading(false);
//            return false;
//        }
//
//
//        
//        // -------- DO SPECIAL PRODUCTS WHICH DON'T GET DISPLAYED IN VIEWER'S MAIN WINDOW ---
//        if (header.getProductType() == NexradHeader.L3GSM) {
//
//            String gsmMessage = level3Header.getGsmDisplayString(scannedFile.toString());
//            
//            System.out.println(gsmMessage);
//            
//            AlphaSupplementalDialog gsmFrame = new AlphaSupplementalDialog(this, gsmMessage, "GENERAL STATUS MESSAGE");
//            gsmFrame.pack();
//            gsmFrame.setVisible(true);
//
//            setIsLoading(false);
//            return true;
//
//        }
//        
//        if (header.getProductType() == NexradHeader.L3RSL) {
//            System.out.println("DECODING RSL");
//            
//            DecodeRSL decoder = new DecodeRSL(level3Header);
//            RSLDisplayDialog rslFrame = new RSLDisplayDialog(this, decoder);
//            //rslFrame.pack();
//            rslFrame.setVisible(true);
//            
//            setIsLoading(false);
//            return true;
//        }
//        // -------- END: DO SPECIAL PRODUCTS WHICH DON'T GET DISPLAYED IN VIEWER'S MAIN WINDOW ---
//
//        
//        
//        
//        
//
//
//
//
//        // set NexradHeader info in StatusBar
//        statusBar.setNexradHeader(header);
//
//
//        nexradBounds = header.getNexradBounds();
//        mapPane.setPreferredArea(nexradBounds);
//        
//        
//
////TODO
////      CHANGE TO IMPLEMENT A LISTENER INTERFACE FOR siteChanged(NexradEvent e)         
//        // Update range rings
//        if (rangeRings != null && ! lastRadarID.equals(header.getICAO())) {
//            rangeRings.loadRangeRings();
//        }
//        lastRadarID = header.getICAO();
//
//
//        if (header.getProductType() != NexradHeader.L3VAD) {
//            if (isVADdisplayed) {
//                mainPanel.remove(vadScrollPane);
//                mainPanel.add(mapScrollPane, "Center");
//                mainPanel.repaint();
//            }
//            isVADdisplayed = false;
//        }               
//
//
//
//
//
//
//
//
//
//        if (resetExtent) {
////          System.out.println("1 XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");            
//            // Turn off zoom change listener for this
////          nxZoomChange.setActive(false);
//
//            mapPane.setVisibleArea(nexradBounds);
//
//            // Manually refresh WMS
//            try {
//                if (mapSelect != null) {
//                    mapSelect.getWMSPanel().refreshWMS();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // Turn zoom change listener back on
//            //nxZoomChange.setActive(true);
////          System.out.println("2 XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");            
//
//        }
//
//
//        
//        
//        //================================================================================
//
//        metaPanel.setSize(200, metaPanel.getHeight());
//        metaPanel.validate();
//        infoPanel.validate();
//        mainPanel.validate();
//        validate();
//        repaint();
////      System.out.println("3 XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");            
//
//        // Set up the DistAzimDisplay
//        //diazdisplay.setRadarLatLon(header.getLat(), header.getLon());
//
//        //================================================================================
//        System.out.println("---------------- START PRODUCT DECODE --------------------");
//
//        // Check for unsupported level-3 product and return if unknown
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {
//
//            if (level3Header.getProductType() == NexradHeader.UNKNOWN) {
//                JOptionPane.showMessageDialog(this, "This product (code=" + level3Header.getProductCode() + ") is not yet supported!",
//                        "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//
//                setIsLoading(false);
//                //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                //this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//
//                return false;
//            }
//
//            // Allow everything to finish repainting
//            if (isAnimation) {
//                try {                     
//                    Thread.sleep(200);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            
//
//
//
//            if (level3Header.getProductType() == NexradHeader.L3VAD) {
//                System.out.println("DECODING VAD");
//
//                if (vadPanel == null) {
//                    vadPanel = new NexradVADPanel();
//                    vadPanel.setBackground(Color.white);
//                    JPanel junkPanel = new JPanel();
//                    junkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//                    junkPanel.add(vadPanel);
//                    vadScrollPane = new JScrollPane(junkPanel);
//                }
//                vadPanel.setNexradHeader(level3Header);
//                if (!isVADdisplayed) {
//                    mainPanel.remove(mapScrollPane);
//                    mainPanel.add(vadScrollPane, "Center");
//                    vadPanel.repaint();
//                    mainPanel.repaint();
//                }
//
//                NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, level3Header, nexradFile,
//                        NexradLegendLabelFactory.DMS);
//                NexradLegendLabelFactory.setSpecialLevel3LegendLabels(radLegendProducer, level3Header, isAlphaBackground);
////                keyPanel.setNexradHeader(level3Header, radLegendProducer);
//                radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(header, true));
//                radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(header, true));
//                if (radLegendProducer.isDrawColorMap()) {
//                    radLegendProducer.setLegendTitle(new String[] {LegendCategoryFactory.getLegendTitle(header, true)});
//                }                    
//                keyPanel.setLegendImage(radLegendProducer);
//                
//                keyPanel.repaint();
//
//                
//                
//                isVADdisplayed = true;
//                setIsLoading(false);
//                //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                //this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//
//                nxZoomChange.setNexradActive(true);
//                if (dataSelect != null) {
//                    dataSelect.setIsLoading(false);
//                }
//                if (radialProp != null) {
//                    radialProp.setLoadButtonEnabled(true);
//                }
//                //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//
//                //isLoading = false;
//
//
//
//
//                NexradDecoder vadDecoder = new DecodeVADText(level3Header);
//                // refresh supplemental data
//                suppleData.setText(vadDecoder.getSupplementalDataArray());
//
//
//                return true;
//            }
////          else {
////          if (isVADdisplayed) {
////          mainPanel.remove(vadScrollPane);
////          mainPanel.add(mapScrollPane, "Center");
////          mainPanel.repaint();
////          }
////          isVADdisplayed = false;
////          }
//
//            // CHECK FOR ALPHANUMERIC PRODUCTS (HAIL, MESO, TVS, STORM STRUCTURE)
//            if (level3Header.getProductType() == NexradHeader.L3ALPHA) {
//                
//                int pcode = level3Header.getProductCode();
//                if (pcode == 58) {
//                    alpha_decoder = new DecodeStormTracking(level3Header);
//                }
//                else if (pcode == 59) {
//                    alpha_decoder = new DecodeHail(level3Header);
//                }
//                else if (pcode == 60) {
//                    alpha_decoder = new DecodeMeso(level3Header);
//                }
//                else if (pcode == 61) {
//                    alpha_decoder = new DecodeTVS(level3Header);
//                }
//                else if (pcode == 62) {
//                    alpha_decoder = new DecodeStormStructure(level3Header);
//                }
//                else if (pcode == 141) {
//                    alpha_decoder = new DecodeMDA(level3Header);
//                }
//                else {
//                    throw new Exception("ALPHANUMERIC PRODUCT CODE "+pcode+" IS NOT SUPPORTED");
//                }
//
//                // refresh supplemental data
//                suppleData.setText(alpha_decoder.getSupplementalDataArray());
//
//                
//                clearNexrad();
//                boolean finished = decodeAlphaNumeric();
//                
//                radLegendProducer.setDrawColorMap(false);
//                keyPanel.setLegendImage(radLegendProducer);                    
//                keyPanel.repaint();
//
////                ((NexradLegendPanel) keyPanel).setNexradHeader(level3Header, radLegendProducer);            
//
//                
//                
//                mapPane.setPreferredArea(alpha_decoder.getNexradExtent());
//                if (resetExtent) {
//                    mapPane.setVisibleArea(alpha_decoder.getNexradExtent());
//                }
//
//                if (alphaProperties != null) {
//                    alphaProperties.refreshMatchingFileList();
//                }
//                keyPanel.setIsUsingHeader(false);
//                keyPanel.validate();
//                keyPanel.repaint();
//
//
//                setIsLoading(false);
//                //dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                //this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                return (finished);
//            }
//            else {
//                radLegendProducer.setSpecialMetadata(null);
//            }
//
//
//        }
//
//        //================================================================================
//
//
//
//        // Remove the alphanumeric layer the layer loading is not for a background
//        if (!isAlphaBackground) {
//            map.removeLayer(mlNexradAlpha);
//            map.removeLayer(mlNexradAlphaTrack);
//            map.removeLayer(mlNexradAlphaLabel);
//        }
//
//
//        //----------------------------------------------------------------------------------------
//        //------ LOAD NEXRAD DATA INTO FEATURE COLLECTION ---------------------
//        //----------------------------------------------------------------------------------------
//
//        System.out.println("NEXRAD FILE TYPE ::: "+scannedFile.getLastScanResult().getFileType());
//
//
//
//
//
//        if (scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2 || 
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_NEXRAD_LEVEL2_AR2V0001 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.RADIAL_SIGMET) {
//
//            radLegendProducer.setSpecialMetadata(null);
//
//
//
//
//
//            if (radialDatasetDecoder == null) {
//                radialDatasetDecoder = new DecodeRadialDatasetSweep((DecodeRadialDatasetSweepHeader)header);
//                radialDatasetDecoder.addDataDecodeListener(this);
//
//                radialDatasetDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.DISPLAY_POLY_ATTRIBUTES);       
//                                    
//            }
//            radialDatasetDecoder.setDecodeHint("downsample-numGates", viewProperties.getDownsampleGates());
//            radialDatasetDecoder.setDecodeHint("downsample-numRays", viewProperties.getDownsampleRays());
//
//            
//            
//            System.out.println("NUMBER OF VARIABLES PRESENT: "+radialDataset.getDataVariables().size());
//            
//            String variableName = radialProp == null ? radialDataset.getDataVariables().get(0).toString() : radialProp.getVariableName();
//            radialDatasetDecoder.setRadialVariable(
//                    (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(variableName)
//                    );
//            
//            
//            // defaults
//            boolean classify = true;
//            int cut = 0;
//            
//            
//            if (nxfilterGUI != null && nxfilterGUI.isFilterEngaged()) {
//                nxfilter = nxfilterGUI.getLevel2Filter(nxfilter);
//            }
//            else {
//                nxfilter = new NexradFilter();
//            }
//
//            
//            // Very first time
//            if (radialProp == null) {
//                classify = true;
//                
////                radialDatasetDecoder.setRadialVariable(
////                      (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable("Reflectivity")
////                      );
//                
////                radialDatasetDecoder.decodeData(classify, false, Level2Format.REFLECTIVITY, 0, false);
//                radialProp = new RadialProperties(this, radialDataset);
//                radialProp.setLocation(this.getLocation().x+25, this.getLocation().y+25);
//                radialProp.setLoadButtonEnabled(false);
//                radialProp.addLoadDataListener(this);
//                
////                System.out.println("MARK 111111111111111111111111");                        
//            } 
//            else {
//
//                // Classify the Level-II Data
//                classify = radialProp.getClassify();
//                String varName = radialProp.getVariableName();
//                cut = radialProp.getCut();
//                boolean useRFvalues = radialProp.getUseRFvalues();
//
//
//
//                if (isAnimation) {
//                    // Always clip for animations regardless of filterGUI
//                    // If we are smoothing then clip to larger area
//                    if (radarSmoothFactor > 0) {
//                        java.awt.geom.Rectangle2D.Double currentExtent = getCurrentExtent();
//                        currentExtent = new java.awt.geom.Rectangle2D.Double(
//                                currentExtent.x - currentExtent.width/2.0,
//                                currentExtent.y - currentExtent.height/2.0,
//                                currentExtent.width * 2.0,
//                                currentExtent.height * 2.0);
//                        nxfilter.setExtentFilter(currentExtent);
//                    }
//                    else {
//                        nxfilter.setExtentFilter(this.getCurrentExtent());
//                    }
//                }
//
//
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(NexradFilterGUI.LEVEL2_TAB);
//                }
//            }
//            
//            
//            radialDatasetDecoder.setDecodeHint("classify", new Boolean(classify));
//            radialDatasetDecoder.setDecodeHint("nexradFilter", nxfilter);
//            radialDatasetDecoder.setDecodeHint("startSweep", new Integer(cut));
//            radialDatasetDecoder.setDecodeHint("endSweep", new Integer(cut));
//            radialDatasetDecoder.decodeData();
//
//            
////            radialDatasetDecoder.setDecodeHint("downsample-numGates", 4);
////            radialDatasetDecoder.setDecodeHint("downsample-numRays", 2);
////            nexradLowResFeatures.clear();
////            StreamingProcess process = new StreamingProcess() {
////                public void addFeature(Feature feature)
////                        throws StreamingProcessException {
////                    nexradLowResFeatures.add(feature);
////                }
////                public void close() throws StreamingProcessException {
////                    System.out.println("STREAMING PROCESS close() ::: nexradLowResFeatures.size() = "+nexradLowResFeatures.size());
////                }           
////            };
////            radialDatasetDecoder.decodeData(new StreamingProcess[] { process } );
////            
////            
////            radialDatasetDecoder.setDecodeHint("downsample-numGates", 1);
////            radialDatasetDecoder.setDecodeHint("downsample-numRays", 1);
////            radialDatasetDecoder.decodeData();
//            
//            
//            // TODO: radialDatasetDecoder
//
//            nexradFeatures = radialDatasetDecoder.getFeatures();
//            nexradSchema = radialDatasetDecoder.getFeatureType();
//
//            
//            if (isAnimation) {
//                radialProp.setVisible(false);
//            }
//            else {
//                radialProp.setVisible(true);
//            }
//            
//            
//            // set statusBar elevation
//            statusBar.setNexradElevationAngle(radialDatasetDecoder.getLastDecodedCutElevation());
//
//
//            // Update properties
////            level2Prop.refreshCutButtons();
//
//
//            jmiLevel2Prop.setEnabled(true);
//
//
//            // Update legend with standard information
//            NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, radialDatasetHeader, nexradFile,
//                    NexradLegendLabelFactory.DMS);
////            ((NexradLegendPanel) keyPanel).setClassify(classify);
//
//            // Close file opened by Level2Format class
////            radialDatasetDecoder.getLevel2Format().close();
//
//            // refresh supplemental data
//            suppleData.setText(null);
//
//
//
//
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3 ||
//                scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {
//
//            System.out.println("DECODING ::: "+nexradURL);
//
//            if (level3Decoder == null) {
//                level3Decoder = new DecodeL3Nexrad(level3Header, nexradFeatures);
//                level3Decoder.addDataDecodeListener(this);
//            }
//            jmiLevel2Prop.setEnabled(false);
//            if (radialProp != null) {
//                radialProp.setVisible(false);
//            }
//
//            // Update legend with standard information
//            NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, level3Header, nexradFile,
//                    NexradLegendLabelFactory.DMS);
////            ((NexradLegendPanel) keyPanel).setClassify(true);
//
//
//            if (isAnimation) {
//                if (nxfilterGUI != null && nxfilterGUI.isFilterEngaged()) {
//                    nxfilter = nxfilterGUI.getLevel3Filter(nxfilter);
//                    nxfilter.setValueIndices(level3Header.getDataThresholdStringArray());
//                }
//                else {
//                    if (nxfilter == null) {
//                        nxfilter = new NexradFilter();
//                    }
//                }
//                // Always clip for animations regardless of filterGUI
//                // If we are smoothing then clip to larger area
//                if (radarSmoothFactor > 0) {
//                    java.awt.geom.Rectangle2D.Double currentExtent = getCurrentExtent();
//                    currentExtent = new java.awt.geom.Rectangle2D.Double(
//                            currentExtent.x - currentExtent.width/2.0,
//                            currentExtent.y - currentExtent.height/2.0,
//                            currentExtent.width * 2.0,
//                            currentExtent.height * 2.0);
//                    nxfilter.setExtentFilter(currentExtent);
//                }
//                else {
//                    nxfilter.setExtentFilter(this.getCurrentExtent());
//                }
//                level3Decoder.setDecodeHint("nexradFilter", nxfilter);
//                level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
//            }
//
//            else if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//
//                level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
//            }
//            else {
//                // Disable distance filtering for non-radial Level-3 products
//                nxfilterGUI.setLevel3DistanceFilterEnabled(level3Header.getProductType() == NexradHeader.L3RADIAL);
//                nxfilter = nxfilterGUI.getLevel3Filter(nxfilter);
//                nxfilter.setValueIndices(level3Header.getDataThresholdStringArray());
//                
//                level3Decoder.setDecodeHint("nexradFilter", nxfilter);
//                level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
//                
//            } 
//            
//            level3Decoder.decodeData();
//
//
//            // refresh supplemental data
//            try {
//                suppleData.setText(level3Decoder.getSupplementalDataArray());
//            } catch (Exception e) {
//                suppleData.setText(new String[]{"ERROR DECODING SUPPLEMENTAL DATA"});
//            }
//            
//
//            // set up status bar info 
//            if (level3Header.getProductCode() == NexradHeader.L3PC_VELOCITY_32NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_VELOCITY_124NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_STORM_RELATIVE_VELOCITY_124NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_SPECTRUM_WIDTH_32NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_SPECTRUM_WIDTH_124NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_124NM ||
//                    level3Header.getProductCode() == NexradHeader.L3PC_BASE_REFLECTIVITY_248NM 
//            ) {
//                statusBar.setNexradElevationAngle(level3Header.getProductSpecificValue(2) / 10.0);
//            }
//            else {
//                statusBar.setNexradElevationAngle(JNXStatusBar.NEXRAD_ELEVATION_UNDEFINED);
//            }
//
//
//
//
//
//
//            nexradFeatures = level3Decoder.getFeatures();
//            nexradSchema = level3Decoder.getFeatureType();
//
//            if (nxfilterGUI != null) {
//                nxfilterGUI.setSelectedTab(NexradFilterGUI.LEVEL3_TAB);
//            }
//        }
//        else if (scannedFile.getLastScanResult().getFileType() == FileScanner.NEXRAD_XMRG) {
//
//            statusBar.setNexradElevationAngle(JNXStatusBar.NEXRAD_SITE_UNDEFINED);
//            if (xmrgDecoder == null) {
//                xmrgDecoder = new DecodeXMRGData(xmrgHeader, nexradFeatures);
//                xmrgDecoder.addDataDecodeListener(this);
//            }
//            jmiLevel2Prop.setEnabled(false);
//            if (radialProp != null) {
//                radialProp.setVisible(false);
//            }
//
//            // Update legend with standard information
//            NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, xmrgHeader, nexradFile,
//                    NexradLegendLabelFactory.DMS);
//            NexradLegendLabelFactory.setSpecialXMRGLegendLabels(radLegendProducer, xmrgHeader, xmrgDecoder);
////            ((NexradLegendPanel) keyPanel).setClassify(true);
//
//
//            if (isAnimation) {
//                if (nxfilterGUI != null && nxfilterGUI.isFilterEngaged()) {
//                    nxfilter = nxfilterGUI.getXMRGFilter(nxfilter);
//                }
//                else {
//                    if (nxfilter == null) {
//                        nxfilter = new NexradFilter();
//                    }
//                }
//                // Always clip for animations regardless of filterGUI
//                // If we are smoothing then clip to larger area
//                if (radarSmoothFactor > 0) {
//                    java.awt.geom.Rectangle2D.Double currentExtent = getCurrentExtent();
//                    currentExtent = new java.awt.geom.Rectangle2D.Double(
//                            currentExtent.x - currentExtent.width/2.0,
//                            currentExtent.y - currentExtent.height/2.0,
//                            currentExtent.width * 2.0,
//                            currentExtent.height * 2.0);
//                    nxfilter.setExtentFilter(currentExtent);
//                }
//                else {
//                    nxfilter.setExtentFilter(this.getCurrentExtent());
//                }
//                xmrgDecoder.setDecodeHint("nexradFilter", nxfilter);
//                xmrgDecoder.decodeData();
//            }
//            else if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                xmrgDecoder.decodeData();
//            }
//            else {
//                xmrgDecoder.setDecodeHint("nexradFilter", nxfilterGUI.getXMRGFilter(nxfilter));
//                xmrgDecoder.decodeData();
//            } 
//
//            //xmrgDecoder.decodeData();
//
//
//
//            nexradFeatures = xmrgDecoder.getFeatures();
//            nexradSchema = xmrgDecoder.getFeatureType();
//
//            // refresh supplemental data
//            suppleData.setText(null);
//
//            //setIsLoading(false);
//        }
//        else {
//
//            statusBar.setNexradElevationAngle(JNXStatusBar.NEXRAD_SITE_UNDEFINED);
//            JOptionPane.showMessageDialog(this, "General Decode Error 1B",
//                    "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//
//            setIsLoading(false);
//            return false;
//        }
//
//
//        return nexradFeatures;
//
//    }
    
    
    
    
}
