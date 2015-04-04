package gov.noaa.ncdc.wct.animation;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ncdc.wct.morph.MorphGeoFeaturePair;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphGridCoverageHandler;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphVectorSource;
import gov.noaa.ncdc.wct.morph.WCTMorphVectors;
import gov.noaa.ncdc.wct.ui.MorphSettings;
import gov.noaa.ncdc.wct.ui.WCTTextDialog;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import ucar.ma2.InvalidRangeException;


public class AnimationUtils {

    
    
    public static void frameProcess(
            FrameHandler[] frameHandlerArray, 
            GeneralProgressListener individualProgressListener,
            GeneralProgressListener progressListener, 
            WCTViewer viewer,
            URL[] urlsToLoad
            ) 
    throws ParseException, NumberFormatException, XPathExpressionException, SAXException, IOException, 
        ParserConfigurationException, IllegalAccessException, InstantiationException, WCTException {

    	frameProcess(frameHandlerArray, individualProgressListener, progressListener, viewer, urlsToLoad, null);
    }
    
    public static void frameProcess(
            FrameHandler[] frameHandlerArray, 
            GeneralProgressListener individualProgressListener,
            GeneralProgressListener progressListener, 
            WCTViewer viewer,
            URL[] urlsToLoad,
            MorphSettings morphSettings
            ) 
    throws ParseException, NumberFormatException, XPathExpressionException, SAXException, IOException, 
        ParserConfigurationException, IllegalAccessException, InstantiationException, WCTException {
        
        
        final GeneralProgressEvent progressEvent = new GeneralProgressEvent(viewer);
        
        // 1. Check if this is a file animation or dimension animation within a file
        if (viewer.getGridProps() != null && (
                viewer.getGridProps().getSelectedTimeIndices().length > 1 ||
                viewer.getGridProps().getSelectedRunTimeIndices().length > 1 ||
                viewer.getGridProps().getSelectedZIndices().length > 1 )
                ) {

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
                        
                        progressEvent.setProgress(percent);                        
                        progressListener.progress(progressEvent);
                        
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

                        viewer.loadFile(viewer.getCurrentDataURL(), false, false, true, false);
                        BufferedImage image = viewer.getViewerBufferedImage();

                        String displayName = viewer.getFileScanner().getLastScanResult().getDisplayName();
                        String saveName = viewer.getFileScanner().getSaveName();
                        String gridName = viewer.getGridDatasetRaster().getLastProcessedGridDataset().getGrids().
                        				get(viewer.getGridProps().getSelectedGridIndex()).getName();
                        
                        if (displayName == null || displayName.trim().length() == 0) {
                            displayName = viewer.getFileScanner().getLastScanResult().getFileName();
                        }
                        if (saveName == null || saveName.trim().length() == 0) {
                            saveName = viewer.getFileScanner().getLastScanResult().getFileName();
                        }
                        
                        
                        boolean endThread = false;
                        for (FrameHandler frameHandler : frameHandlerArray) {
                            frameHandler.processFrame(image, saveName+"-var_"+gridName+"-t"+t+"-rt_"+rt+"-z_"+z, 
                                    saveName+"-var_"+gridName+"-t"+t+"-rt_"+rt+"-z_"+z);
                            if (frameHandler.isCanceled()) {
                                endThread = true;
                            }
                        }

                        // End loop if needed
                        if (endThread) {
                            z = zIndices.length;
                            rt = runtimeIndices.length;
                            t = timeIndices.length;
                            progressEvent.setStatus("Ended");
                            progressListener.ended(progressEvent);
                        }


                    }  
                }
            }
        }
        else {
    		
            boolean isTimeMorphing = morphSettings != null;
        	final WCTMorphOperation morpher = new WCTMorphOperation();
        	final StringBuilder operationLog = new StringBuilder();
        	
            for (int i=0; i<urlsToLoad.length; i++) {
                
                BufferedImage[] images = null;

            	// special case
            	// 0         1         2         3         4         5
            	// 01234567890123456789012345678901234567890123456789012345
            	// /SL.us008001/DF.of/DC.radar/DS.p94r1/SI.kbro/sn.last

                
                if (! isTimeMorphing) {
                	images = new BufferedImage[] { viewer.getViewerBufferedImage(urlsToLoad[i], true, true) };
                }
                else if (urlsToLoad.length == 1 && (
            			(urlsToLoad[0].getProtocol().equals("ftp") &&
            			 urlsToLoad[0].getHost().equals("tgftp.nws.noaa.gov") ) ||
            		    (urlsToLoad[0].getProtocol().equals("http") &&
                    	 urlsToLoad[0].getHost().equals("weather.noaa.gov") )
            			) &&
            			urlsToLoad[0].getFile().endsWith("sn.last")) {
            		
            		String file = urlsToLoad[0].getFile();
            		
            		URL dataURL = urlsToLoad[0];
            		URL stiURL = null;
            		if (urlsToLoad[0].getProtocol().equals("ftp")) {
            			file = file.substring(0, 31) + "58sti" + file.substring(36, file.length());
            			stiURL = new URL("ftp://tgftp.nws.noaa.gov"+file);
            		}
            		else {
            			System.out.println(file);
//            			0         1          2         3         4         5         6         7
//            			0123456789012345678980123456789012345678901234567890123456789012345678901234567890
//            			/pub/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kfcx/sn.last
//            			/pub/SL.us008001/DF.of/DC.radar/DS.58sti/SI.kfcx/sn.last
            			file = file.substring(0, 35) + "58sti" + file.substring(40, file.length());            		
            			stiURL = new URL("http://weather.noaa.gov"+file);
            		}

            		try {
            			
                    	images = processTimeMorphing(viewer, morpher, individualProgressListener, 
                    			dataURL, stiURL, i, morphSettings, (i == urlsToLoad.length-2), operationLog);
            			
        			} catch (FileNotFoundException e) {
        				e.printStackTrace();

        				JOptionPane.showMessageDialog(viewer, "Error creating motion vectors. \n" +
        						"RUC Error: File not found for date requested.", 
        						"Animation Time Morphing Error", JOptionPane.ERROR_MESSAGE);
        				i = urlsToLoad.length;
        			} catch (Exception e) {
        				e.printStackTrace();
        				JOptionPane.showMessageDialog(viewer, "Error creating motion vectors. \n" +
        						"SWDI Error: "+e.getMessage(), 
        						"Animation Time Morphing Error", JOptionPane.ERROR_MESSAGE);
        				i = urlsToLoad.length;
        			}

        			
            	}
                else {
                	
                	if (i == urlsToLoad.length-1) {
                		continue;
                	}
                	
            		try {
            			
                    	images = processTimeMorphing(viewer, morpher, individualProgressListener, 
                    			urlsToLoad[i], urlsToLoad[i+1], i, morphSettings, (i == urlsToLoad.length-2), operationLog);
            			
        			} catch (FileNotFoundException e) {
        				e.printStackTrace();

        				JOptionPane.showMessageDialog(viewer, "Error creating motion vectors. \n" +
        						"RUC Error: File not found for date requested.", 
        						"Animation Time Morphing Error", JOptionPane.ERROR_MESSAGE);
        				i = urlsToLoad.length;
        			} catch (Exception e) {
        				e.printStackTrace();
        				JOptionPane.showMessageDialog(viewer, "Error creating motion vectors. \n" +
        						"SWDI Error: "+e.getMessage(), 
        						"Animation Time Morphing Error", JOptionPane.ERROR_MESSAGE);
        				i = urlsToLoad.length;
        			}
                }
                
                
                if (images == null) {
                	return;
                }
                
                int cnt = 0;
                for (BufferedImage image : images) {
                	
                	
                	int percent = (int)(100*(((double)i+1)/urlsToLoad.length));
                	progressEvent.setProgress(percent);                        
                	progressListener.progress(progressEvent);
                	String displayName;
                	String saveName;
                	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {

                		saveName = viewer.getFileScanner().getSaveName();
                		if (saveName == null || saveName.equalsIgnoreCase("null") || saveName.trim().length() == 0) {
                			saveName = viewer.getFileScanner().getLastScanResult().getFileName();
                		}

                		String gridName = viewer.getGridDatasetRaster().getLastProcessedGridDataset().getGrids().get(
                				viewer.getGridProps().getSelectedGridIndex()
                		).getName();
                		displayName = saveName+"-var_"+gridName+"-t0-rt0-z0";

                	}
                	else {
                		displayName = viewer.getFileScanner().getLastScanResult().getDisplayName();
                		saveName = viewer.getFileScanner().getSaveName();

                	}

                	
                	if (images.length > 1) {
                		displayName += "_m"+cnt+"_"+i;
                		saveName += "_m"+cnt+"_"+i;
                		cnt++;
                	}
                	
                	
                	
                	boolean endThread = false;
                	for (FrameHandler frameHandler : frameHandlerArray) {
                		
//                		System.out.println("framehandler.processFrame()");
                		
                		frameHandler.processFrame(image, displayName, saveName);
                		if (frameHandler.isCanceled()) {
                			endThread = true;
                		}
                	}

                	// End loop if needed
                	if (endThread) {
                		i = urlsToLoad.length;
                		progressEvent.setStatus("Ended");
                		progressListener.ended(progressEvent);
                	}
                
                } // end for (bufferedImages)
            } // end for (urlsToLoad)          
            
        	viewer.loadData();
        	
        	if (operationLog.toString().length() > 0) {
        		WCTTextDialog morphLogDialog = new WCTTextDialog(viewer, "Time Morphing Operation Log");
        		morphLogDialog.setText(operationLog.toString());
        		morphLogDialog.setSize(600, 250);
        		morphLogDialog.setVisible(true);
        	}
        	
        	

        } // end else (if loop across dimension)
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    private static BufferedImage[] processTimeMorphingAcrossGridDimension(final WCTViewer viewer, 
    		final GeneralProgressListener progressListener,	URL url, 
    		int firstTimeIndex, int lastTimeIndex, final MorphSettings morphSettings, final boolean keepLastFrame) 
    throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
    	IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, URISyntaxException, Exception {

    	
//        int percent = (int) (100.0 * WCTUtils.progressCalculator(new int[] { t, rt, z }, 
//                new int[] { timeIndices.length, runtimeIndices.length, zIndices.length }) );
        final GeneralProgressEvent progressEvent = new GeneralProgressEvent(viewer);
        
        
        
    	final MorphVectorSource mvSource = morphSettings.getMorphVectorSource();
    	final int numSteps = morphSettings.getNumMorphSteps();
    	final int dateBufferPercent = morphSettings.getSwdiDateBuffer();
    	final int numberOfMorphVectorGridCells = morphSettings.getSwdiNumGridCells();


    	
    	URL stormTrackingL3ProductUrl = null;
		ScanResults[] scanResults = viewer.getDataSelector().getScanResults();
		for (ScanResults sr : scanResults) {
			if (sr.getTimestamp() != null && sr.getTimestamp().equals(
					scanResults[viewer.getDataSelector().getSelectedIndices()[urlIndex]].getTimestamp())) {
				
				if (sr.getProductID() != null && sr.getProductID().equals("NST")) {
					stormTrackingL3ProductUrl = sr.getUrl();
					break;
				}							
			}
		}
		
//		String firstTimestamp = scanResults[viewer.getDataSelector().getSelectedIndices()[urlIndex]].getTimestamp();
//		String lastTimestamp = scanResults[viewer.getDataSelector().getSelectedIndices()[urlIndex+1]].getTimestamp();
//		// 20040617 12:45:06 or 20040617 12:45 format
//		SimpleDateFormat sdfMin = new SimpleDateFormat("yyyyMMdd HH:mm");
//		sdfMin.setTimeZone(TimeZone.getTimeZone("GMT"));
//		final SimpleDateFormat sdfSec = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//		sdfSec.setTimeZone(TimeZone.getTimeZone("GMT"));
//		final SimpleDateFormat sdfDisplay = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//		sdfDisplay.setTimeZone(TimeZone.getTimeZone("GMT"));
//		Date firstDate = null;
//		Date lastDate = null;
//		if (firstTimestamp.length() == 14) {
//			firstDate = sdfMin.parse(firstTimestamp);
//			lastDate = sdfMin.parse(lastTimestamp);
//		}
//		else if (firstTimestamp.length() == 17) {
//			firstDate = sdfSec.parse(firstTimestamp);
//			lastDate = sdfSec.parse(lastTimestamp);
//		}
//		final long firstDateInMillis = firstDate.getTime();
//		final long dateDiffPerStepInMillis = (lastDate.getTime() - firstDate.getTime())/numSteps;
		
		

		SmoothingOperation smop = new SmoothingOperation();
		
//		Rectangle2D.Double viewExtent = 
//			smop.adjustSmoothingExtent(viewer.getCurrentExtent(), 
//					viewer.getMapPane().getWidth(), 
//					viewer.getMapPane().getHeight(), 
//					(int)viewer.getRadarSmoothFactor());

		// TODO
		// commented out because I'm expanding by 50% in morph class
		// expand view extent to allow for morphing of image
		// ideally this extent would be smartly calculated from the motion vectors
//		Rectangle2D.Double viewExtent = 
//			smop.adjustSmoothingExtent(viewer.getCurrentExtent(), 
//					viewer.getMapPane().getWidth(), 
//					viewer.getMapPane().getHeight(), 
//					100);
		Rectangle2D.Double viewExtent = viewer.getCurrentExtent(); 


		
    	final ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

    	final WCTMorphTest morph = new WCTMorphTest();
    	morph.setDateBufferPercentForSWDIQuery(dateBufferPercent);
    	morph.setNumberOfMorphVectorGridCells(numberOfMorphVectorGridCells);

		
		MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
			@Override
			public void processGridCoverage(GridCoverage gc, Date date, int step) {
				
				System.out.println("process grid coverage: step="+step+" numSteps="+numSteps);
				
				
				
				if (step == numSteps && ! keepLastFrame) {
					return;
				}
				
	    		CategoryLegendImageProducer legendProducer = viewer.getRadarLegendImageProducer();
//	    		String dtinfo = viewer.getRadarLegendImageProducer().getDateTimeInfo();
//	    		if (dtinfo.endsWith("."+(step-1))) {
//	    			dtinfo = dtinfo.substring(0, dtinfo.length()-2)+"."+step;
//	    		}
//	    		else {
//	    			dtinfo = dtinfo+"."+step;
//	    		}
	    		String dtinfo = sdfDisplay.format(new Date(firstDateInMillis + step*dateDiffPerStepInMillis))+" GMT";
	    		if (step != 0 && step != numSteps) {
	    			dtinfo += " *";
	    		}
	    		legendProducer.setDateTimeInfo(dtinfo);
	    		try {
					viewer.getLargeLegendPanel().setLegendImage(legendProducer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				gc = WCTGridCoverageSupport.applyAlpha2(gc, viewer.getRadarTransparency());

				double baseline = viewer.getFilter().getMinValue()[0];
				if (Double.isNaN(baseline) || Double.isInfinite(baseline)) {
					baseline = -25.0;
				}
				gc = WCTGridCoverageSupport.smooth(gc, (int)viewer.getRadarSmoothFactor(), (float)baseline);

	    		viewer.setRadarGridCoverage(gc);
	    		BufferedImage image = viewer.getViewerBufferedImage();
	    		
	    		// ---- draw vector arrows
	    		if (morphSettings.isDrawMotionVectors()) {
	    		
	    			ArrayList<MorphGeoFeaturePair> gfpList = morph.getLastProcessedGeoFeaturePairList();
	    			Graphics2D g = image.createGraphics();
	    			if (gfpList != null) {
	    				for (MorphGeoFeaturePair gfp : gfpList) {

	    					Point[] pointPair = WCTMorphVectors.getImageCoordinatePair(gfp, 
	    							viewer.getCurrentExtent(), 
	    							new Dimension(viewer.getMapPane().getWidth(), viewer.getMapPane().getHeight()));

	    					//	            		System.out.println("drawing pair: "+Arrays.toString(pointPair));
	    					g.setColor(Color.CYAN);
	    					g.fillRect(pointPair[0].x-4, pointPair[0].y-4, 8, 8);
	    					g.setColor(Color.GRAY);
	    					g.fillRect(pointPair[1].x-3, pointPair[1].y-3, 6, 6);

	    					g.setColor(Color.BLACK);
	    					WCTMorphTest.drawArrow(g, pointPair[0].x, pointPair[0].y, pointPair[1].x, pointPair[1].y);
	    				}
	    			}
	    			g.dispose();
	    		}
	    		
	    		// update progress	   
	            double progressPercent = ((double)step)/numSteps*100;
	            progressEvent.setProgress(progressPercent);       
	            progressEvent.setStatus("Interpolating Morph Step "+step+"/"+numSteps);
	            progressListener.progress(progressEvent);
	            
	    		// add image
	    		imageList.add(image);          
	    		
	    		gc = null;

//				ImageIO.write(image, "png", new File("E:\\work\\morph\\output\\morph_wct_"+(i)+"_"+(cnt)+".png"));
//				
//				try {
//					BufferedImage image2 = WCTMorphTest.createImage(gc, 
//							new Dimension(gc.getRenderedImage().getWidth(), gc.getRenderedImage().getHeight()));
	//
//					ImageIO.write(image2, "png", new File("E:\\work\\morph\\output\\morph_plain_"+(i)+"_"+(cnt)+".png"));
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
			}
		};

		WCTFilter filter = new WCTFilter();
		filter.setExtentFilter(viewExtent); 
		morph.setFilter(filter);
		
		if (viewer.getRadialRemappedRaster() == null) {
			throw new WCTException("Please load a file to initialize the viewer");
		}
		
		morph.setVariableName(viewer.getRadialRemappedRaster().getLastDecodedVariableName());
		
		
    	if (stormTrackingL3ProductUrl == null) {
       		morph.processMorph(
       				numSteps, progressListener, firstURL, lastURL, mvSource, gcHandler);
    	}
    	else {
    		morph.processWithStormTracking(
    			numSteps, firstURL, lastURL, 
    			stormTrackingL3ProductUrl, gcHandler);
    	}
    	
//    	imageList.add(viewer.getViewerBufferedImage(firstURL, true, true));
//    	BufferedImage image = viewer.getViewerBufferedImage(firstURL, true, true);
    	
    	
    	return imageList.toArray(new BufferedImage[imageList.size()]);
		
	}
    */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private static BufferedImage[] processTimeMorphing(final WCTViewer viewer, final WCTMorphOperation morph, 
    		final GeneralProgressListener progressListener,
    		URL firstURL, URL lastURL, 
    		int urlIndex, final MorphSettings morphSettings, final boolean keepLastFrame, StringBuilder operationLog) 
    throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
    	IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, URISyntaxException, Exception {

    	
        final GeneralProgressEvent progressEvent = new GeneralProgressEvent(viewer);
        
        
        
    	final MorphVectorSource mvSource = morphSettings.getMorphVectorSource();
    	final int numSteps = morphSettings.getNumMorphSteps();
    	final int dateBufferPercent = morphSettings.getSwdiDateBuffer();
    	final int numberOfMorphVectorGridCells = morphSettings.getSwdiNumGridCells();


    	
    	URL stormTrackingL3ProductUrl = null;
//		ScanResults[] scanResults = viewer.getDataSelector().getScanResults();
//		for (ScanResults sr : scanResults) {
//			if (sr.getTimestamp() != null && sr.getTimestamp().equals(
//					scanResults[viewer.getDataSelector().getSelectedIndices()[urlIndex]].getTimestamp())) {
//				
//				if (sr.getProductID() != null && sr.getProductID().equals("NST")) {
//					stormTrackingL3ProductUrl = sr.getUrl();
//					break;
//				}							
//			}
//		}
    	if ((	(lastURL.getProtocol().equals("ftp") &&	lastURL.getHost().equals("tgftp.nws.noaa.gov"))
    		 || (lastURL.getProtocol().equals("http") && lastURL.getHost().equals("weather.noaa.gov"))
    		 )
    		 && lastURL.getFile().contains("/DS.58sti/") &&
    		lastURL.getFile().endsWith("sn.last")) {

    		stormTrackingL3ProductUrl = WCTTransfer.getURL(lastURL, true);
    		
    		
    		lastURL = null;
    	}
    	
		
//		final long firstDateInMillis = firstDate.getTime();
//		final long dateDiffPerStepInMillis = (lastDate.getTime() - firstDate.getTime())/numSteps;
		
		

		SmoothingOperation smop = new SmoothingOperation();
		
//		Rectangle2D.Double viewExtent = 
//			smop.adjustSmoothingExtent(viewer.getCurrentExtent(), 
//					viewer.getMapPane().getWidth(), 
//					viewer.getMapPane().getHeight(), 
//					(int)viewer.getRadarSmoothFactor());

		// TODO
		// commented out because I'm expanding by 50% in morph class
		// expand view extent to allow for morphing of image
		// ideally this extent would be smartly calculated from the motion vectors
//		Rectangle2D.Double viewExtent = 
//			smop.adjustSmoothingExtent(viewer.getCurrentExtent(), 
//					viewer.getMapPane().getWidth(), 
//					viewer.getMapPane().getHeight(), 
//					100);
		Rectangle2D.Double viewExtent = viewer.getCurrentExtent(); 


		
    	final ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

    	morph.setSmoothFactor((int)viewer.getRadarSmoothFactor());
    	morph.setDateBufferPercentForSWDIQuery(dateBufferPercent);
    	morph.setNumberOfMorphVectorGridCells(numberOfMorphVectorGridCells);
    	if (viewer.getSampleDimension() != null) {
    		morph.setSampleDimension(viewer.getSampleDimension());
    	}
    	else {
			if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL ||
	            	  viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
	            	  viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS ) {
				morph.setSampleDimension(viewer.getRadarGridCoverage().getSampleDimensions()[0]);
			}
			else {
				morph.setSampleDimension(viewer.getGridSatelliteGridCoverage().getSampleDimensions()[0]);
			}
    	}
    	
		
    	final SimpleDateFormat sdfDisplay = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	sdfDisplay.setTimeZone(TimeZone.getTimeZone("GMT"));
		MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
			@Override
			public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis) {
				
				System.out.println("process grid coverage: step="+step+" numSteps="+numSteps);
				
				
				
				if (step == numSteps && ! keepLastFrame) {
					return;
				}
				
				
				
				if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL ||
		            	  viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
		            	  viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS ) {
					
					CategoryLegendImageProducer legendProducer = viewer.getRadarLegendImageProducer();
					if (date != null) {
						String dtinfo = sdfDisplay.format(date)+" GMT";
						if (step != 0 && step != numSteps) {
							dtinfo += " *";
						}
						legendProducer.setDateTimeInfo(dtinfo);
					}
					try {
						viewer.getLargeLegendPanel().setLegendImage(legendProducer);
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					gc = WCTGridCoverageSupport.applyAlpha2(gc, viewer.getRadarTransparency());

					double baseline = viewer.getFilter().getMinValue()[0];
					if (Double.isNaN(baseline) || Double.isInfinite(baseline)) {
						baseline = -25.0;
					}
					gc = WCTGridCoverageSupport.smooth(gc, (int)viewer.getRadarSmoothFactor(), (float)baseline);
					viewer.setRadarGridCoverage(gc);
					
//					viewer.snapshotCurrentLayer();
					

//		            try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					

//					viewer.setRadarGridCoverage(gc);
//		    		try {
//						ImageIO.write(viewer.getViewerBufferedImage(), "png", 
//								new File("E:\\work\\morph\\anim\\morph-"+step+".png"));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					
//					gc = WCTGridCoverageSupport.smooth(gc, (int)viewer.getRadarSmoothFactor(), (float)baseline);
//					viewer.setRadarGridCoverage(gc);
//		    		try {
//						ImageIO.write(viewer.getViewerBufferedImage(), "png", 
//								new File("E:\\work\\morph\\anim\\morph-smoothed-"+step+".png"));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

				}
				else {
					try {

//						CategoryLegendImageProducer legendProducer = viewer.getGridSatelliteLegendImageProducer();
//						if (date != null) {
//							String dtinfo = sdfDisplay.format(date)+" GMT";
//							if (step != 0 && step != numSteps) {
//								dtinfo += " *";
//							}
//							legendProducer.setDateTimeInfo(dtinfo);
//						}
//						try {
//							Image image = legendProducer.createMediumLegendImage();
//							RenderedLogo gridSatelliteLegend = viewer.getGridSatelliteLegend();
//							gridSatelliteLegend.setInsets(new Insets(0, 0, 15, image.getWidth(null)));
//							gridSatelliteLegend.setImage(image);
//							gridSatelliteLegend.repaint();
//							
////							gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);
//
//							
////							viewer.setGri  getLargeLegendPanel().setLegendImage(legendProducer);
//						} catch (Exception e1) {
//							e1.printStackTrace();
//						}

						String dateNote = "";
						if (date != null) {
							if (step != 0 && step != numSteps) {
								dateNote = "*";
							}
						}

						viewer.refreshSatelliteLegend(date, dateNote);
						viewer.setGridSatelliteGridCoverage(gc);

					} catch (TransformException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    		

	    		
	    		
	    		
	    		
	    		BufferedImage image = viewer.getViewerBufferedImage();
	    		
	    		// ---- draw vector arrows
	    		if (morphSettings.isDrawMotionVectors()) {
	    		
	    			ArrayList<MorphGeoFeaturePair> gfpList = morph.getLastProcessedGeoFeaturePairList();
	    			Graphics2D g = image.createGraphics();
	    			if (gfpList != null) {
	    				for (MorphGeoFeaturePair gfp : gfpList) {

	    					Point[] pointPair = WCTMorphVectors.getImageCoordinatePair(gfp, 
	    							viewer.getCurrentExtent(), 
	    							new Dimension(viewer.getMapPane().getWidth(), viewer.getMapPane().getHeight()));

	    					//	            		System.out.println("drawing pair: "+Arrays.toString(pointPair));
	    					g.setColor(Color.CYAN);
	    					g.fillRect(pointPair[0].x-4, pointPair[0].y-4, 8, 8);
	    					g.setColor(Color.GRAY);
	    					g.fillRect(pointPair[1].x-3, pointPair[1].y-3, 6, 6);

	    					g.setColor(Color.BLACK);
	    					WCTMorphOperation.drawArrow(g, pointPair[0].x, pointPair[0].y, pointPair[1].x, pointPair[1].y);
	    				}
	    			}
	    			g.dispose();
	    		}
	    		
	    		// update progress	   
	            double progressPercent = ((double)step)/numSteps*100;
	            progressEvent.setProgress(progressPercent);       
	            progressEvent.setStatus("Interpolating Morph Step "+step+"/"+numSteps);
	            progressListener.progress(progressEvent);
	            
	    		// add image
	    		imageList.add(image);          
	    		
	    		gc = null;

//				ImageIO.write(image, "png", new File("E:\\work\\morph\\output\\morph_wct_"+(i)+"_"+(cnt)+".png"));
//				
//				try {
//					BufferedImage image2 = WCTMorphTest.createImage(gc, 
//							new Dimension(gc.getRenderedImage().getWidth(), gc.getRenderedImage().getHeight()));
	//
//					ImageIO.write(image2, "png", new File("E:\\work\\morph\\output\\morph_plain_"+(i)+"_"+(cnt)+".png"));
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
			}
		};

		WCTFilter filter = new WCTFilter();
		filter.setExtentFilter(viewExtent); 
		morph.setFilter(filter);
		
		if (viewer.getRadialRemappedRaster() == null) {
			throw new WCTException("Please load a file to initialize the viewer");
		}
		
		morph.setVariableName(viewer.getRadialRemappedRaster().getLastDecodedVariableName());
		
		
    	if (stormTrackingL3ProductUrl == null) {
       		morph.processMorph(
       				numSteps, progressListener, firstURL, lastURL, mvSource,morphSettings, gcHandler);
    	}
    	else {
    		morph.processWithStormTracking(
    			numSteps, firstURL, lastURL, 
    			stormTrackingL3ProductUrl, gcHandler);
    	}

    	return imageList.toArray(new BufferedImage[imageList.size()]);
		
	}


    
    
    
    
    
    
    
    
    
    
	public static int frameCount(
            FrameHandler[] frameHandlerArray, 
            GeneralProgressListener progressListener, 
            WCTViewer viewer,
            URL[] urlsToLoad
            ) throws ParseException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        
        
        
        int frameCount = 0;
        
        // 1. Check if this is a file animation or dimension animation within a file
        if (viewer.getGridProps() != null && (
                viewer.getGridProps().getSelectedTimeIndices().length > 1 ||
                viewer.getGridProps().getSelectedRunTimeIndices().length > 1 ||
                viewer.getGridProps().getSelectedZIndices().length > 1 )
                ) {

            int gridIndex = viewer.getGridProps().getSelectedGridIndex();
            
            
            boolean doTimeDimension = true;
            boolean doRuntimeDimension = true;
            boolean doZDimension = true;
            
            
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
                        frameCount++;
                    }  
                }
            }
        }
        else {
            frameCount += urlsToLoad.length;
        }
        
        return frameCount;
    }

    
    public interface FrameHandler {
        public void processFrame(BufferedImage frameImage, String displayName, String saveName);
        public void cancel();
        public boolean isCanceled();
    }

}
