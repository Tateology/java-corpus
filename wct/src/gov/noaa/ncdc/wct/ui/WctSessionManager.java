package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.nexradiv.MapSelector;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentViewType;
import gov.noaa.ncdc.wct.ui.WctSession.FilterInfo;
import gov.noaa.ncdc.wct.ui.WctSession.GridFilter;
import gov.noaa.ncdc.wct.ui.WctSession.GridInfo;
import gov.noaa.ncdc.wct.ui.WctSession.Level3Filter;
import gov.noaa.ncdc.wct.ui.WctSession.OverlayInfo;
import gov.noaa.ncdc.wct.ui.WctSession.RadialFilter;
import gov.noaa.ncdc.wct.ui.WctSession.RadialInfo;
import gov.noaa.ncdc.wct.ui.WctSession.SatelliteInfo;
import gov.noaa.ncdc.wct.ui.WctSession.UiInfo;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class WctSessionManager {

    private static final Logger logger = Logger.getLogger(WctSessionManager.class.getName());

    
	public static WctSession createWctSession(WCTViewer viewer) {
		WctSession wctSession = new WctSession();
		MapSelector mapSelector = viewer.getMapSelector();
		DataSelector dataSelector = viewer.getDataSelector();
		
		UiInfo uiInfo = new UiInfo();
		uiInfo.setMainX(viewer.getLocation().x);
		uiInfo.setMainY(viewer.getLocation().y);
		uiInfo.setMainWidth(viewer.getSize().width);
		uiInfo.setMainHeight(viewer.getSize().height);
		wctSession.setUiInfo(uiInfo);
		
//		LegendInfo legendInfo = new LegendInfo();
//		legendInfo.setBackgroundColor(viewer.getViewProperties().get)
		
		OverlayInfo[] overlayInfoArray = new OverlayInfo[WCTViewer.NUM_LAYERS];
		for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
			overlayInfoArray[n] = new OverlayInfo();
			overlayInfoArray[n].setLayerName(mapSelector.getLayerName(n));
			overlayInfoArray[n].setLayerVisible(mapSelector.getLayerVisibility(n));
			overlayInfoArray[n].setLayerColor(mapSelector.getLayerColor(n));
			overlayInfoArray[n].setLayerSize(mapSelector.getLayerLineWidth(n));
			overlayInfoArray[n].setLabelVisible(mapSelector.getLabelVisibility(n));
		}
		wctSession.setOverlayInfoArray(overlayInfoArray);
		for (int n=0; n<3; n++) {
		    String wmsName = mapSelector.getWMSPanel().getSelectedWMS(n);
	        wctSession.setWMSLayer(n, wmsName);
		}
		
		wctSession.setExtent(viewer.getCurrentExtent());
		wctSession.setDataLocationType(dataSelector.getDataSourcePanel().getDataType());
		wctSession.setDataLocation(dataSelector.getDataSourcePanel().getDataLocation());
		
		wctSession.setAutoExtentSelected(dataSelector.isAutoExtentSelected());
		
		wctSession.setSelectedDataIndices(dataSelector.getSelectedIndices());
		URL[] selectedDataUrlArray = dataSelector.getSelectedURLs();
		String[] dataUrlStringArray = new String[selectedDataUrlArray.length];
		for (int n=0; n<dataUrlStringArray.length; n++) {
		    dataUrlStringArray[n] = selectedDataUrlArray[n].toString();
		}
		wctSession.setSelectedDataURLs(dataUrlStringArray);
		
		
		
		FilterInfo filterInfo = new FilterInfo();
		filterInfo.setLevel3Filter(convertToSessionLevel3Filter( viewer.getFilterGUI().getLevel3Filter() ));
		filterInfo.setRadialFilter(convertToSessionRadialFilter( viewer.getFilterGUI().getRadialFilter() ));
		filterInfo.setGridFilter(convertToSessionGridFilter( viewer.getFilterGUI().getGridFilter() ));
		wctSession.setFilterInfo(filterInfo);
		
		wctSession.setCurrentDataType(viewer.getCurrentDataType());
		if (viewer.getCurrentDataType() == CurrentDataType.RADAR) {
		    RadialInfo radialInfo = new RadialInfo();
		    radialInfo.setRadarTransparency(mapSelector.getRadarTransparency());
		    radialInfo.setSmoothingFactor((int)viewer.getRadarSmoothFactor());
		    wctSession.setRadialInfo(radialInfo);
		}
		else if (viewer.getCurrentDataType() == CurrentDataType.SATELLITE) {
		    
		    SatelliteInfo satInfo = new SatelliteInfo();
		    satInfo.setLegendType(mapSelector.getGridSatelliteLegendType());
		    satInfo.setSatColorTableName(mapSelector.getSatelliteColorTableName());
		    satInfo.setTransparency(mapSelector.getGridSatelliteTransparency());
		    
		    wctSession.setSatelliteInfo(satInfo);
		}
		else if (viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {

		    GridInfo gridInfo = new GridInfo();
		    
            gridInfo.setGridSatTransparency(mapSelector.getGridSatelliteTransparency());
            gridInfo.setGridSatelliteLegendType(mapSelector.getGridSatelliteLegendType());
            gridInfo.setGridColorTableName(mapSelector.getGridColorTableName());
            gridInfo.setGridColorTableMinValue(mapSelector.getGridColorTableMinValue());
            gridInfo.setGridColorTableMaxValue(mapSelector.getGridColorTableMaxValue());
          
            gridInfo.setSelectedGridIndex(viewer.getGridProps().getSelectedGridIndex());
            gridInfo.setSelectedRuntimeIndices(viewer.getGridProps().getSelectedRunTimeIndices());
            gridInfo.setSelectedTimeIndices(viewer.getGridProps().getSelectedTimeIndices());
            gridInfo.setSelectedZIndices(viewer.getGridProps().getSelectedZIndices());
            
            wctSession.setGridInfo(gridInfo);
            
          
		}
		
		
		
		
		
        wctSession.setCurrentViewType(viewer.getCurrentViewType());
		
		return wctSession;
	}
	
	public static void saveWctSession(WCTViewer viewer, File outFile) throws IOException {

		WctSession wctSession = createWctSession(viewer);
		saveObjectData(wctSession, outFile);
	}
	
	public static void saveWctSession(WctSession wctSession, File outFile) throws IOException {
		saveObjectData(wctSession, outFile);
	}
	
	
	
	public static void loadWctSession(final WCTViewer viewer, File sessionFile) 
		throws IOException, ClassNotFoundException {

	    // 0. Init session object from file
	    final WctSession wctSession = loadObjectData(sessionFile);

        viewer.getStatusBar().setProgress(0);
        viewer.getStatusBar().setProgressText("Loading Project...");
	        
        viewer.clearAllData();

	    // 1. Clear WMS layers before loading
        for (int n=0; n<3; n++) {
            viewer.getMapSelector().getWMSPanel().setSelectedWMS(n, "None");    
        }
        viewer.getMapSelector().getWMSPanel().refreshWMS();
        
        // 2. Set extent to 'nowhere' which expedites loading of base map overlays	    
		Rectangle2D.Double curExtent = viewer.getCurrentExtent();
		viewer.setCurrentExtent(new Rectangle2D.Double(-30, 35, 0.01, 0.01));
		
		// 3. Set Location and Size of main viewer window
		try {
		    viewer.setLocation(wctSession.getUiInfo().getMainX(), wctSession.getUiInfo().getMainY());
		    viewer.setSize(wctSession.getUiInfo().getMainWidth(), wctSession.getUiInfo().getMainHeight());
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
        // 4. Load overlays
		try {
			loadWctSessionOverlays(viewer, wctSession);
		} catch (WCTException e) {
            JOptionPane.showMessageDialog(viewer, "Session loading error: "+e.getMessage());
		}
		
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		        try {
//		            viewer.setCurrentViewType(wctSession.getCurrentViewType());
		            
//                    System.out.println("before viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds("+
//                            viewer.getMapPane().getSize()+" --- "+ wctSession.getExtent());
//		            viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds(viewer.getMapPane().getSize(), wctSession.getExtent()));
//                    System.out.println("after  viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds("+
//                            viewer.getMapPane().getSize()+" --- "+ wctSession.getExtent());
		        } catch (Exception e1) {
		            e1.printStackTrace();
		        }
		    }		        
		});
		
		try {
		    System.out.println("setting data location info: type="+wctSession.getDataLocationType()+" location="+wctSession.getDataLocation());
            viewer.getDataSelector().getDataSourcePanel().setDataType(wctSession.getDataLocationType());
		    viewer.getDataSelector().getDataSourcePanel().setDataLocation(wctSession.getDataLocationType(), wctSession.getDataLocation());
		    viewer.getDataSelector().setIsAutoExtentSelected(wctSession.isAutoExtentSelected());
		    viewer.showDataSelector();
		    
		    
		    SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    
                    try {
                        if (wctSession.getCurrentViewType() == CurrentViewType.GOOGLE_EARTH) {
                            viewer.setCurrentViewType(CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS);
                        }
                        else {
                            viewer.setCurrentViewType(wctSession.getCurrentViewType());
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    
                    viewer.getDataSelector().submitListFiles();
                    System.out.println(Arrays.toString(wctSession.getSelectedDataIndices()));
                    
                    viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds(viewer.getMapPane().getSize(), wctSession.getExtent()));

                    try {
                        FilterInfo filterInfo = wctSession.getFilterInfo();
                        
                        
                        if (filterInfo != null) {
                        
                    	convertLevel3ToWCTFilter(filterInfo.getLevel3Filter());
                		convertRadialToWCTFilter(filterInfo.getRadialFilter());
                		convertGridToWCTFilter(filterInfo.getGridFilter());
                		
                  						
                        viewer.getFilterGUI().setFilters(
                        		convertLevel3ToWCTFilter(filterInfo.getLevel3Filter()), 
                        		convertRadialToWCTFilter(filterInfo.getRadialFilter()), 
                        		convertGridToWCTFilter(filterInfo.getGridFilter())
                        	);
                        
                        
                        }
                        
					} catch (Exception e) {
						e.printStackTrace();
					}

                    

					viewer.clearData();
					
                    if (wctSession.getSelectedDataIndices() != null && wctSession.getSelectedDataIndices().length > 0) {
                        viewer.getDataSelector().getResultsList().setSelectedIndices(wctSession.getSelectedDataIndices());
                        if (wctSession.getSelectedDataIndices().length == 1) {
                            viewer.getDataSelector().setIsAutoExtentSelected(false);
                            
                            
//                            viewer.getMapPaneZoomChange().setActive(false);
//                            viewer.getDataSelector().loadData();
//                            viewer.loadFile(viewer.getDataSelector().getSelectedURLs()[0]);
                            
                            
//                            viewer.addRenderCompleteListener(new RenderCompleteListener() {
//								@Override
//								public void renderComplete() {
//		                            if (wctSession.getCurrentDataType() == CurrentDataType.RADAR) {
//		                                RadialInfo radialInfo = wctSession.getRadialInfo();
//		                                viewer.getMapSelector().setRadarTransparency(radialInfo.getRadarTransparency());
//		                                viewer.getMapSelector().setRadarSmoothingFactor(radialInfo.getSmoothingFactor());
//		                            }
//		                            else if (wctSession.getCurrentDataType() == CurrentDataType.SATELLITE) {
//		                                SatelliteInfo satInfo = wctSession.getSatelliteInfo();
//		                                viewer.getMapSelector().setGridSatelliteTransparency(satInfo.getTransparency());
//		                                viewer.getMapSelector().setGridSatelliteLegend(satInfo.getLegendType());
//		                                viewer.getMapSelector().setSatelliteColorTableName(satInfo.getSatColorTableName());
//		                                
//		                            }
//		                            else if (wctSession.getCurrentDataType() == CurrentDataType.GRIDDED) {
//		                                
//		                                GridInfo gridInfo = wctSession.getGridInfo();
//		                                viewer.getMapSelector().setGridSatelliteTransparency(gridInfo.getGridSatTransparency());
//		                                viewer.getMapSelector().setGridSatelliteLegend(gridInfo.getGridSatelliteLegendType());
//		                                viewer.getMapSelector().setGridColorTableMinMaxValue(gridInfo.getGridColorTableMinValue(), gridInfo.getGridColorTableMaxValue());
//		                                viewer.getMapSelector().setGridColorTableName(gridInfo.getGridColorTableName());
//		                             
//		                                viewer.getGridProps().setSelectedGridIndex(gridInfo.getSelectedGridIndex());
//		                                viewer.getGridProps().setSelectedRuntimeIndices(gridInfo.getSelectedRuntimeIndices());
//		                                viewer.getGridProps().setSelectedTimeIndices(gridInfo.getSelectedTimeIndices());
//		                                viewer.getGridProps().setSelectedZIndices(gridInfo.getSelectedZIndices());
//		                            }
//		                            
//		                            
////		                            JOptionPane.showMessageDialog(viewer, "proceed after data has loaded.");
////		                            viewer.removeRenderCompleteListener(this);
////		                            viewer.getMapPaneZoomChange().setActive(true);
//								}
//
//								@Override
//								public void renderProgress(int progressPercent) {
//								}
//                            });
                            
                            
                            
                            
                            if (wctSession.getCurrentDataType() == CurrentDataType.RADAR) {
                                RadialInfo radialInfo = wctSession.getRadialInfo();
                                viewer.getMapSelector().setRadarTransparency(radialInfo.getRadarTransparency());
                                if (radialInfo.getSmoothingFactor() > 0) {
//                                	JOptionPane.showMessageDialog(viewer, "proceed after data has loaded.");
                                }
                                viewer.getMapSelector().setRadarSmoothingFactor(radialInfo.getSmoothingFactor());
                            }
                            else if (wctSession.getCurrentDataType() == CurrentDataType.SATELLITE) {
                                SatelliteInfo satInfo = wctSession.getSatelliteInfo();
                                viewer.getMapSelector().setGridSatelliteTransparency(satInfo.getTransparency());
                                viewer.getMapSelector().setGridSatelliteLegend(satInfo.getLegendType());
                                viewer.getMapSelector().setSatelliteColorTableName(satInfo.getSatColorTableName());
                                
                            }
                            else if (wctSession.getCurrentDataType() == CurrentDataType.GRIDDED) {
                                
                                GridInfo gridInfo = wctSession.getGridInfo();
                                viewer.getMapSelector().setGridSatelliteTransparency(gridInfo.getGridSatTransparency());
                                viewer.getMapSelector().setGridSatelliteLegend(gridInfo.getGridSatelliteLegendType());
                                viewer.getMapSelector().setGridColorTableMinMaxValue(gridInfo.getGridColorTableMinValue(), gridInfo.getGridColorTableMaxValue());
                                viewer.getMapSelector().setGridColorTableName(gridInfo.getGridColorTableName());
                             
                                viewer.getGridProps().setSelectedGridIndex(gridInfo.getSelectedGridIndex());
                                viewer.getGridProps().setSelectedRuntimeIndices(gridInfo.getSelectedRuntimeIndices());
                                viewer.getGridProps().setSelectedTimeIndices(gridInfo.getSelectedTimeIndices());
                                viewer.getGridProps().setSelectedZIndices(gridInfo.getSelectedZIndices());
                            }
                            
//                            viewer.getMapPaneZoomChange().setActive(true);
                            
                            
                            viewer.getDataSelector().loadData();

                        }
                    }
                    

//                    try {
//                        FilterInfo filterInfo = wctSession.getFilterInfo();
//                        
//                        
//                        if (filterInfo != null) {
//                        
//                    	convertLevel3ToWCTFilter(filterInfo.getLevel3Filter());
//                		convertRadialToWCTFilter(filterInfo.getRadialFilter());
//                		convertGridToWCTFilter(filterInfo.getGridFilter());
//                		
//                  						
//                        viewer.getFilterGUI().setFilters(
//                        		convertLevel3ToWCTFilter(filterInfo.getLevel3Filter()), 
//                        		convertRadialToWCTFilter(filterInfo.getRadialFilter()), 
//                        		convertGridToWCTFilter(filterInfo.getGridFilter())
//                        	);
//                        
//                        
//                        }
//                        
//                        
//                        
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
					
//                    viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds(viewer.getMapPane().getSize(), wctSession.getExtent()));

                    for (int n=0; n<3; n++) {
                        viewer.getMapSelector().getWMSPanel().setSelectedWMS(n, wctSession.getWMSLayer(n));    
                    }
                    viewer.getMapSelector().getWMSPanel().refreshWMS();
                    
                    
                }
		    });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(viewer, "Session loading error: "+e.getMessage());
        }
        
        
//        viewer.setCurrentExtent(WCTUtils.adjustGeographicBounds(viewer.getMapPane().getSize(), wctSession.getExtent()));
//
//        for (int n=0; n<3; n++) {
//            viewer.getMapSelector().getWMSPanel().setSelectedWMS(n, wctSession.getWMSLayer(n));    
//        }
//        viewer.getMapSelector().getWMSPanel().refreshWMS();
        
        
        try {

//            System.out.println("setting data location info: type="+wctSession.getDataLocationType()+" location="+wctSession.getDataLocation());
//            viewer.getDataSelector().getDataSourcePanel().setDataType(wctSession.getDataLocationType());
//            viewer.getDataSelector().getDataSourcePanel().setDataLocation(wctSession.getDataLocationType(), wctSession.getDataLocation());
//            viewer.showDataSelector();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(viewer, "Session loading error: "+e.getMessage());
        }
        
        
        
		
		viewer.getStatusBar().setProgress(0);
        viewer.getStatusBar().setProgressText("");
	}
	
	public static void loadWctSessionOverlays(WCTViewer viewer, WctSession session) throws WCTException {
	    MapSelector mapSelector = viewer.getMapSelector();
	    OverlayInfo[] overlayInfoArray = session.getOverlayInfoArray();

	    if (overlayInfoArray.length != WCTViewer.NUM_LAYERS) {
	    	throw new WCTException("Old Session File.  Viewer map overlays have changed in this version.  \n" +
	    			"Please manually update the overlays and re-save the session file.");
	    }
	    
	    try {
	    
	    	for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {

	    		System.out.println(
	    				overlayInfoArray[n].getLayerName()+" : "+
	    				overlayInfoArray[n].isLayerVisible()+" : "+
	    				overlayInfoArray[n].getLayerColor()+" : "+
	    				overlayInfoArray[n].getLayerSize()+" : "+
	    				overlayInfoArray[n].isLabelVisible()
	    		);
	    		
	    		if (overlayInfoArray[n].getLayerName() == null || 
	    				overlayInfoArray[n].getLayerColor() == null) {
	    			continue;
	    		}


	    		mapSelector.setLayerColor(n, overlayInfoArray[n].getLayerColor());
	    		if (n == WCTViewer.STATES || n == WCTViewer.COUNTRIES || n == WCTViewer.COUNTRIES_USA) {
	    			logger.fine("SETTING STATES OR COUNTRIES TO: "+overlayInfoArray[n].getLayerColor());
	    			viewer.setLayerFillColor(n, overlayInfoArray[n].getLayerColor());
	    		}
	    		else {
	    			viewer.setLayerLineColor(n, overlayInfoArray[n].getLayerColor());


	    			mapSelector.setLayerLineWidth(n, overlayInfoArray[n].getLayerSize());
	    			viewer.setLayerLineWidth(n, overlayInfoArray[n].getLayerSize());
	    		}



	    		mapSelector.setLabelVisibility(n, false);            
	    		mapSelector.setLabelVisibility(n, overlayInfoArray[n].isLabelVisible());            

	    		mapSelector.setLayerVisibility(n, false);          
	    		mapSelector.setLayerVisibility(n, overlayInfoArray[n].isLayerVisible());          


	    		viewer.getStatusBar().setProgress((n+1)*100/WCTViewer.NUM_LAYERS);
	    	}
	    
	    } catch (Exception e) {
	    	throw new WCTException(e.getMessage());
	    }
	}
	
	
	
	private static RadialFilter convertToSessionRadialFilter(WCTFilter filter) {
		RadialFilter f = new RadialFilter();
		f.setMinValues(filter.getMinValue());
		f.setMaxValues(filter.getMaxValue());
		f.setMinAzimuth(filter.getMinAzimuth());
		f.setMaxAzimuth(filter.getMaxAzimuth());
		f.setMinDistance(filter.getMinDistance());
		f.setMaxDistance(filter.getMaxDistance());
		f.setMinHeight(filter.getMinHeight());
		f.setMaxHeight(filter.getMaxHeight());
		return f;
	}
	
	private static WCTFilter convertRadialToWCTFilter(RadialFilter filter) {
		WCTFilter f = new WCTFilter();
		if (filter == null) {
			return f;
		}
		f.setMinValue(filter.getMinValues());
		f.setMaxValue(filter.getMaxValues());
		f.setAzimuthRange(filter.getMinAzimuth(), filter.getMaxAzimuth());
		f.setMinDistance(filter.getMinDistance());
		f.setMaxDistance(filter.getMaxDistance());
		f.setMinHeight(filter.getMinHeight());
		f.setMaxHeight(filter.getMaxHeight());
		return f;
	}
	
	private static GridFilter convertToSessionGridFilter(WCTFilter filter) {
		GridFilter f = new GridFilter();
		f.setMinValues(filter.getMinValue());
		f.setMaxValues(filter.getMaxValue());
		return f;
	}
	
	private static WCTFilter convertGridToWCTFilter(GridFilter filter) {
		WCTFilter f = new WCTFilter();
		if (filter == null) {
			return f;
		}
		f.setMinValue(filter.getMinValues());
		f.setMaxValue(filter.getMaxValues());
		return f;
	}
	
	private static Level3Filter convertToSessionLevel3Filter(WCTFilter filter) {
		Level3Filter f = new Level3Filter();
		f.setMinValues(filter.getMinValue());
		f.setMaxValues(filter.getMaxValue());
		f.setCategoryOverrides(filter.getCategoryOverrides());
		return f;
	}
	
	private static WCTFilter convertLevel3ToWCTFilter(Level3Filter filter) {
		WCTFilter f = new WCTFilter();
		if (filter == null) {
			return f;
		}
		f.setMinValue(filter.getMinValues());
		f.setMaxValue(filter.getMaxValues());
		f.setCategoryOverrides(filter.getCategoryOverrides());
		return f;
	}
	
	
	
	
	public static WctSession loadObjectData(File objFile) throws IOException, ClassNotFoundException {

		System.out.println("LOADING FROM: "+objFile);
		
		String fileContents = FileUtils.readFileToString(objFile);
		
		if (! objFile.exists()) {
			throw new FileNotFoundException(objFile + " DOES NOT EXIST");
		}
		// read serialized xml sessions (>= version 3.5.2)
		else if (fileContents.startsWith("<?xml version=\"1.0\"")) {
			XStream xstream = new XStream(new StaxDriver());
			return (WctSession)xstream.fromXML(fileContents);
		}
		// read serialized object sessions (before version 3.5.2)
		else {
			// Read from disk using FileInputStream.
			FileInputStream fin = new FileInputStream (objFile);

			// Read object using ObjectInputStream.
			ObjectInputStream objIn = new ObjectInputStream (fin);
			
			// Read object using XMLDecoder
//			XMLDecoder objIn = new XMLDecoder(fin);

			// Read an object.
			Object obj = objIn.readObject();

			objIn.close();
			fin.close();

			// Is the object that you read in, say, an instance
			// of the WctSession class?
			if (obj instanceof WctSession) {
			  // Cast object to a Vector
			    return (WctSession) obj;
			}
			else {
				System.err.println("COULD NOT LOAD WctSession OBJECT\n" +
						objFile+" IS OF TYPE: "+obj.toString());
				return null;
			}
		}
	}
	
	public static void saveObjectData(WctSession wctSession, File objFile) throws IOException {
		// create dir if needed
		if (! objFile.getParentFile().exists()) {
			objFile.getParentFile().mkdirs();
		}
		
		// Use a FileOutputStream to send data to a file
//		FileOutputStream fout = new FileOutputStream(objFile);

		// Use an ObjectOutputStream to send object data to the
		// FileOutputStream for writing to disk.
//		ObjectOutputStream objOut = new ObjectOutputStream(fout);
		
		// Use XMLEncoder to send object data to disk
//		XMLEncoder objOut = new XMLEncoder(fout);
//		objOut.setExceptionListener(new ExceptionListener() {
//            @Override
//            public void exceptionThrown(Exception e) {
//                e.printStackTrace();
//            }
//        });

		// Pass our object to the ObjectOutputStream's
		// writeObject() method to cause it to be written out
		// to disk.
//		objOut.writeObject(wctSession);

//		objOut.close();
//		fout.close();
		
		
		
		File xmlFile = new File(objFile.toString());
		XStream xstream = new XStream(new StaxDriver());
		BufferedWriter bw = new BufferedWriter(new FileWriter(xmlFile));
		bw.write(xstream.toXML(wctSession));
		bw.close();
		
	}
}
