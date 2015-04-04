package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.DataExportListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTMathOp;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;
import org.jdesktop.swingx.JXDialog;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.TimeUnit;

import com.jidesoft.hints.FileIntelliHints;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.SelectAllUtils;


public class RasterMathDialog extends JXDialog {

	private WCTViewer viewer;
	
	private JComboBox operationTypeCombo = new JComboBox(new String[] {
	    	"Max", "Min", "Average", "Absolute Max",
	    	"Absolute Max (Signed)", "Sum", "Diff"	
	    });

	
	private JButton exportButton, cancelButton;
	
	private JProgressBar progressBar = new JProgressBar();
	
	private int processedCountInLoop = 0;
	private boolean shouldCancel = false;
	private boolean isOperationAcrossDimensions = false;
	private SupportedDataType dataType;
	
	private ArrayList<String> sourceList = new ArrayList<String>();
	private ArrayList<String> productList = new ArrayList<String>();
	
	private WCTRaster processRaster = null;
	
	
	/**
	 * Show the WCT Math Dialog
	 * @param viewer
	 */
	public RasterMathDialog(WCTViewer viewer) {
		this(viewer, viewer);
	}
	
	public RasterMathDialog(WCTViewer viewer, Component parent) {
		super(viewer, new JPanel());
		
		this.viewer = viewer;
		
        setModal(true);
        setTitle("Math Tool (BETA)");
        
        createGUI();
        
        pack();
        setLocation(parent.getX()+45, parent.getY()+45);
        setVisible(true);
	}
	
	
	
	
	private void createGUI() {
		

		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setString("Processing Progress");
   
        
        
        


		try {


			JPanel mainPanel = new JPanel(new RiverLayout());
			//        mainPanel.setBorder(WCTUiUtils.myTitledTopBorder("Math Tool (for Grid and Radial data)", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));



			JButton goButton = new JButton("Start");
			goButton.setActionCommand("SUBMIT");
			goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
			goButton.addActionListener(new SubmitListener(this));
			exportButton = new JButton("Export");
			exportButton.setActionCommand("EXPORT");
			exportButton.setPreferredSize(new Dimension(60, (int)exportButton.getPreferredSize().getHeight()));
			exportButton.addActionListener(new SubmitListener(this));
			exportButton.setEnabled(false);
			cancelButton = new JButton("Close");
			cancelButton.setActionCommand("CLOSE");
			cancelButton.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
			cancelButton.addActionListener(new SubmitListener(this));


//			System.out.println(Arrays.toString(viewer.getDataSelector().getSelectedURLs()));
//			System.out.println(Arrays.toString(viewer.getGridProps().getSelectedRunTimeIndices()));
//			System.out.println(Arrays.toString(viewer.getGridProps().getSelectedTimeIndices()));
//			System.out.println(Arrays.toString(viewer.getGridProps().getSelectedZIndices()));
			
			
			boolean multipleAvailable = false;
			if (viewer.getDataSelector().getSelectedIndices().length > 1) {
				multipleAvailable = true;
			}
			else if (viewer.getGridProps() != null && 
					viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED &&
					(viewer.getGridProps().getSelectedRunTimeIndices().length > 1 ||
							viewer.getGridProps().getSelectedTimeIndices().length > 1 ||
							viewer.getGridProps().getSelectedZIndices().length > 1)
			) {
				multipleAvailable = true;
				isOperationAcrossDimensions = true;
			}



			JPanel generalOptionsPanel = getGeneralOptionsPanel();
			if (multipleAvailable) {
				mainPanel.add(generalOptionsPanel, "hfill");
			}
			else {
				mainPanel.add(getNoFilesSelectedPanel(), "hfill");
				goButton.setEnabled(false);
			}


			JPanel buttonPanel = new JPanel(new RiverLayout());

			buttonPanel.add("p hfill", progressBar);
			buttonPanel.add("p center", goButton);
			buttonPanel.add(exportButton);
			buttonPanel.add(cancelButton);

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);


		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "General Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	
	private JPanel getGeneralOptionsPanel() {
		
		JPanel panel = new JPanel(new RiverLayout());
		
		panel.setBorder(WCTUiUtils.myTitledTopBorder("General Math Tool Options", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

        
		panel.add("p", new JLabel("Operation Type: "));
		panel.add("tab hfill", operationTypeCombo);
//		panel.add("br", new JLabel("KML Shape Type: "));
//		panel.add("tab hfill", radialIsosurfaceShapeType);

		
		
		
		return panel;
	}
	
	
	
	private JPanel getNoFilesSelectedPanel() {
		
		JPanel panel = new JPanel(new RiverLayout());
		
		panel.setBorder(WCTUiUtils.myTitledTopBorder("General Math Tool Options", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

        
		panel.add("p", new JLabel("<html><center>The Math Tool is only available<br>" +
				"for multiple files or grid slices.</center></html>"));		
		
		
		return panel;
	}
	
	
	
	
	
	
	
	
	
	
	
	private void process() throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
		IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
		URISyntaxException, ParseException, Exception {
		
		sourceList.clear();
		productList.clear();
		
		

		final int[] selectedIndices = viewer.getDataSelector().getSelectedIndices();
		URL[] selectedURLs = viewer.getDataSelector().getSelectedURLs();
		
		String operation = operationTypeCombo.getSelectedItem().toString();
		if (operation.equals("Diff") && ! isDiffAvailable()) {
			throw new Exception("For the 'Diff' operation, exactly 2 files or layers must be selected");
		}
		
		
		WCTExport exporter = new WCTExport();
//		exporter.setForceResample(true);
		exporter.setOutputFormat(WCTExport.ExportFormat.WCT_RASTER_OBJECT_ONLY);
		WCTFilter exportFilter = new WCTFilter();
		Rectangle2D.Double curExtent = viewer.getCurrentExtent();
		Rectangle2D extent = curExtent.createIntersection(new Rectangle2D.Double(-180, -90, 360, 180));
		exportFilter.setExtentFilter((Rectangle2D.Double)extent);
		exporter.setExportL3Filter(exportFilter);
		exporter.setExportRadialFilter(exportFilter);
		exporter.setExportGridSatelliteFilter(exportFilter);
		
		// Add progress listener on exporter
		exporter.addDataExportListener(new DataExportListener() {
			@Override
			public void exportStarted(DataExportEvent event) {
			}
			@Override
			public void exportEnded(DataExportEvent event) {
			}
			@Override
			public void exportProgress(DataExportEvent event) {
				int numToProcess;
					if (isOperationAcrossDimensions) {
			    		int[] rtIndices = viewer.getGridProps().getSelectedRunTimeIndices();
			    		int[] tIndices = viewer.getGridProps().getSelectedTimeIndices();
			    		int[] zIndices = viewer.getGridProps().getSelectedZIndices();
			    		if (rtIndices.length == 0) {
			    			rtIndices = new int[] { -1 };
			    		}
			    		if (tIndices.length == 0) {
			    			tIndices = new int[] { -1 };
			    		}
			    		if (zIndices.length == 0) {
			    			zIndices = new int[] { -1 };
			    		}

						numToProcess = rtIndices.length*tIndices.length*zIndices.length;
					}
					else {
						numToProcess = selectedIndices.length;
					}
					double progress = WCTUtils.progressCalculator(
							new int[] { getProcessedCount(), event.getProgress() }, 
							new int[] { numToProcess, 100 } );
					
		
				
				progressBar.setValue((int)(progress*100));
				if (shouldCancel) {
					progressBar.setString("Canceling...");
				}
				else {
					if (isOperationAcrossDimensions) {
						progressBar.setString("Processing Grid ("+getProcessedCount()+" of "+numToProcess+")");
					}
					else {
//						progressBar.setString(event.getStatus());
						progressBar.setString("Processing File ("+getProcessedCount()+" of "+numToProcess+")");
					}
				}
			}

			@Override
			public void exportStatus(DataExportEvent event) {
			}
			
		});
		
		
		
		
		
		
		shouldCancel = false;
		
		
		
		
		
		
		
		if (isOperationAcrossDimensions) {
			int gridIndex = viewer.getGridProps().getSelectedGridIndex();
    		int[] rtIndices = viewer.getGridProps().getSelectedRunTimeIndices();
    		int[] tIndices = viewer.getGridProps().getSelectedTimeIndices();
    		int[] zIndices = viewer.getGridProps().getSelectedZIndices();
    		
    		System.out.println(Arrays.toString(rtIndices));
    		System.out.println(Arrays.toString(tIndices));
    		System.out.println(Arrays.toString(zIndices));
    		
    		if (rtIndices.length == 0) {
    			rtIndices = new int[] { -1 };
    		}
    		if (tIndices.length == 0) {
    			tIndices = new int[] { -1 };
    		}
    		if (zIndices.length == 0) {
    			zIndices = new int[] { -1 };
    		}
    		
    		
			exporter.setGridExportGridIndex(gridIndex);
    		for (int rt=0; rt<rtIndices.length; rt++) {
    			if (rtIndices[rt] >= 0) {
    				exporter.setGridExportRuntimeIndex(rtIndices[rt]);
    			}
        		for (int t=0; t<tIndices.length; t++) {
        			if (tIndices[t] >= 0) {
        				exporter.setGridExportTimeIndex(tIndices[t]);
        			}
            		for (int z=0; z<zIndices.length; z++) {
            			if (zIndices[z] >= 0) {
            				exporter.setGridExportZIndex(zIndices[z]);
            			}
            			
        				if (shouldCancel) {
        					rt = rtIndices.length;
        					t = tIndices.length;
        					z = zIndices.length;
        					continue;				
        				}
        				
        				
        				processRaster = processLayer(exporter, processRaster, selectedURLs[0], processedCountInLoop);
        				processedCountInLoop++;
            			
            		}
        		}
    		}
    		

		}
		else {
			for (int n=0; n<selectedIndices.length; n++) {

				if (shouldCancel) {
					n = selectedIndices.length;
					continue;				
				}
				processRaster = processLayer(exporter, processRaster, selectedURLs[n], processedCountInLoop);
				processedCountInLoop++;
			}
		}
		processedCountInLoop = 0;
		progressBar.setValue(0);
		progressBar.setString("");
		
	
		
		GridCoverage gc = getGridCoverageFromProcessedRaster(exporter, processRaster);
//		System.out.println(gc.getEnvelope());
		
		viewer.fireRenderCompleteEvent();

		viewer.getMapPaneZoomChange().setGridSatelliteActive(false);
		viewer.getMapPaneZoomChange().setRadarActive(false);
		if (dataType == SupportedDataType.NEXRAD_LEVEL3 ||
				dataType == SupportedDataType.NEXRAD_LEVEL3_NWS ||
				dataType == SupportedDataType.RADIAL) {
			
			viewer.setRadarGridCoverage(gc);
			viewer.setRadarGridCoverageVisibility(true);
		}
		else {
			viewer.getGridDatasetRaster().setWritableRaster(processRaster.getWritableRaster());
			viewer.getGridDatasetRaster().setBounds(processRaster.getBounds());
			viewer.getGridDatasetRaster().setDisplayMinValue(viewer.getGridDatasetRaster().getMinValue());
			viewer.getGridDatasetRaster().setDisplayMaxValue(viewer.getGridDatasetRaster().getMaxValue());
			viewer.setGridSatelliteGridCoverage(gc);
			viewer.setGridSatelliteVisibility(true);
			
			
			viewer.setGridSatelliteColorTable(viewer.getGridDatasetRaster().getColorTableAlias());
		}
		
		
		
		processLegendChanges(exporter, processRaster);
		// if no data has been loaded into the viewer yet, ignore the legend
		if (viewer.getFileScanner() != null) {
			viewer.refreshLegend();
		}

		
		// enable export button so results can be saved
		exportButton.setEnabled(true);
	}


	private boolean isDiffAvailable() {
		URL[] selectedURLs = viewer.getDataSelector().getSelectedURLs();
		String operation = operationTypeCombo.getSelectedItem().toString();
		if (viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {
			
			int[] rtIndices = viewer.getGridProps().getSelectedRunTimeIndices();
			int[] tIndices = viewer.getGridProps().getSelectedTimeIndices();
			int[] zIndices = viewer.getGridProps().getSelectedZIndices();
			
			int rtNumSelected = (rtIndices.length == 0) ? -1 : rtIndices.length;
			int tNumSelected =  (tIndices.length == 0) ? -1 : tIndices.length;
			int zNumSelected =  (zIndices.length == 0) ? -1 : zIndices.length;

			
			System.out.println(selectedURLs.length + " ,,,, "+Math.abs(rtNumSelected * tNumSelected * zNumSelected) );
			
			if (operation.equalsIgnoreCase("Diff") && 
					(selectedURLs.length > 2 || 
						(selectedURLs.length == 1 && Math.abs(rtNumSelected * tNumSelected * zNumSelected) != 2))) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return (operation.equalsIgnoreCase("Diff") && selectedURLs.length == 2); 
		}

	}
	
	
	
	
	private WCTRaster processLayer(WCTExport exporter, WCTRaster processRaster, URL dataURL, int overallIndex) {
		
		System.out.println("Processing: "+dataURL);
		try {
			exporter.exportData(dataURL, File.createTempFile("wct", ".obj"));

			WCTRaster raster = exporter.getLastProcessedRaster();

			String operation = operationTypeCombo.getSelectedItem().toString();
			// Create initial raster and reset bounds
			if (processRaster == null) {
				DateFormatter df = new DateFormatter();
				// create copy of raster that has value of 0.0 for all data
				processRaster = WCTMathOp.createEmptyRasterCopy(raster);
				processRaster.setLongName("WCT Math "+operation+" operation output grid of: "+raster.getLongName()+
						" -- First timestep: "+df.toDateTimeStringISO( new Date(raster.getDateInMilliseconds()) ));
				processRaster.setVariableName(raster.getVariableName());
				processRaster.setStandardName(raster.getStandardName());
				processRaster.setUnits(raster.getUnits());
			}
			// use last processed date for date dimension
			processRaster.setDateInMilliseconds(raster.getDateInMilliseconds());

			if (operation.equalsIgnoreCase("Max")) {
				WCTMathOp.max(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Min")) {
				WCTMathOp.min(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Average")) {
				WCTMathOp.average(processRaster, processRaster, overallIndex, raster, 1, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Absolute Max")) {
				WCTMathOp.absMax(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Absolute Max (Signed)")) {
				WCTMathOp.absMaxSigned(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Sum")) {
				WCTMathOp.sum(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else if (operation.equalsIgnoreCase("Diff")) {
				WCTMathOp.diff(processRaster, processRaster, raster, raster.getNoDataValue());
			}
			else {
				throw new WCTException("math operation type of "+operation+ " is not supported.");
			}

			//	            System.out.println(WCTMathOp.getStatsIgnoreNoData(raster));
			//	            System.out.println(WCTMathOp.getStatsIgnoreNoData(processRaster));

			//				scanResult = viewer.getDataSelector().getScanResults()[selectedIndices[n]];


			// check previous data type
			if (dataType != null && dataType != exporter.getLastProcessedDataType()) {
				JOptionPane.showMessageDialog(this, "WCT Math Tool Warning", 
						"Warning: New data type found: "+dataType, JOptionPane.WARNING_MESSAGE);
			}
			this.dataType = exporter.getLastProcessedDataType();

			checkDatasetSpecificInfo(exporter);


			//				System.out.println("n="+n+"   "+scanResult.getDataType());
			System.out.println("n="+overallIndex+"   "+dataType);


		} catch (Exception e) {
			e.printStackTrace();
			reportException(e);
		}

		return processRaster;
	}
	
	
	
	

	private void checkDatasetSpecificInfo(WCTExport exporter) {
		if (! sourceList.contains(exporter.getFileScanner().getLastScanResult().getSourceID())) {
			sourceList.add(exporter.getFileScanner().getLastScanResult().getSourceID());
		}
		if (! productList.contains(exporter.getFileScanner().getLastScanResult().getProductID())) {
			productList.add(exporter.getFileScanner().getLastScanResult().getProductID());
		}
		

//		SupportedDataType dataType = exporter.getLastProcessedDataType();
//		if (dataType == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
//			// check for goes id or band changes
//			sourceList.add(exporter.getFileScanner().getLastScanResult().getSourceID());
//			productList.add(exporter.getFileScanner().getLastScanResult().getProductID());
//		}
//		else if (dataType == SupportedDataType.NEXRAD_LEVEL3 ||
//				dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//			// check for site and product changes
//			
//		}
//		else if (dataType == SupportedDataType.RADIAL) {
//			// check for site and variable changes
//			exporter.getLastProcessedRadialRemappedRaster().getLastDecodedSweepHeader().getICAO();
//		}
//		else if (dataType == SupportedDataType.GRIDDED){
//			// check for variable changes
//		}
		
		
		
	}

	
	private void processLegendChanges(WCTExport exporter, WCTRaster processRaster) throws Exception {
		SupportedDataType dataType = exporter.getLastProcessedDataType();

		if (dataType == SupportedDataType.NEXRAD_LEVEL3 ||
				dataType == SupportedDataType.NEXRAD_LEVEL3_NWS ||
				dataType == SupportedDataType.RADIAL) {

			CategoryLegendImageProducer clip = viewer.getRadarLegendImageProducer();

			if (sourceList.size() > 1) {
				clip.setDataDescription(new String[] { "MULTIPLE SITES" });
			}
			clip.setDateTimeInfo("MATH OPERATION RESULTS");
		
		}
		else {
			CategoryLegendImageProducer clip = viewer.getLastDecodedLegendImageProducer();

			if (sourceList.size() > 1) {
				clip.setDataDescription(new String[] { "MULTIPLE SOURCES" });
			}
			clip.setDateTimeInfo("MATH OPERATION RESULTS");
		}
	}
	
	
	private GridCoverage getGridCoverageFromProcessedRaster(WCTExport exporter, WCTRaster processRaster) throws Exception {
		GridCoverage gc = null;
		WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
		SupportedDataType dataType = exporter.getLastProcessedDataType();
		
		int radTransparency = 255 - ((int)((viewer.getMapSelector().getRadarTransparency()/100.0)*255));
		int gridSatTransparency = 255 - ((int)((viewer.getMapSelector().getGridSatelliteTransparency()/100.0)*255));
		
//		if (scanResult.getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
		if (dataType == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
			GoesRemappedRaster goes = exporter.getLastProcessedGoesRemappedRaster();
			goes.setWritableRaster(processRaster.getWritableRaster());
			gc = goes.getGridCoverage(gridSatTransparency);
		}
//		else if (scanResult.getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
//				scanResult.getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
		else if (dataType == SupportedDataType.NEXRAD_LEVEL3 ||
				dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {

			gc = gcSupport.getGridCoverage(processRaster, exporter.getLevel3Header(), radTransparency);

		}
//		else if (scanResult.getDataType() == SupportedDataType.RADIAL) {
		else if (dataType == SupportedDataType.RADIAL) {
			RadialDatasetSweepRemappedRaster radialRaster = exporter.getLastProcessedRadialRemappedRaster();
			radialRaster.setWritableRaster(processRaster.getWritableRaster());
			radialRaster.setSmoothingFactor(viewer.getMapSelector().getRadarSmoothingFactor());
			gc = radialRaster.getGridCoverage(radTransparency);
		}
		else {
			GridDatasetRemappedRaster grid = exporter.getLastProcessedGridDatasetRemappedRaster();
			grid.setWritableRaster(processRaster.getWritableRaster());
			gc = grid.getGridCoverage(gridSatTransparency);
		}
		return gc;
	}
	
	
	
	private final class SubmitListener implements ActionListener {
        private Dialog parent;
        public SubmitListener(Dialog parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equalsIgnoreCase("SUBMIT")) {
//            	saveProperties();
//                formSubmitted = true;
            	
            	((JButton)e.getSource()).setEnabled(false);
                try {
                    foxtrot.Worker.post(new foxtrot.Task() {
                        public Object run() {

                            try {

                            	cancelButton.setText("Cancel");
                                cancelButton.setActionCommand("CANCEL");
                                process();
                                cancelButton.setEnabled(true);
                            	cancelButton.setText("Close");
                                cancelButton.setActionCommand("CLOSE");

                        		progressBar.setValue(0);
                        		progressBar.setString("Processing Progress");

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                reportException(ex);
                            }

                            return "DONE";
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            	((JButton)e.getSource()).setEnabled(true);

            }
            else if (e.getActionCommand().equalsIgnoreCase("EXPORT")) {
            	showExportDialog();
            }
            else if (e.getActionCommand().equalsIgnoreCase("CANCEL")) {
            	setShouldCancel(true);
                cancelButton.setEnabled(false);
            	cancelButton.setText("Canceling...");
            }
            else {
            	parent.dispose();
            }
        }


    }

	
	private void reportException(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
	}
	
	private int getProcessedCount() {
		return processedCountInLoop;
	}

	public void setShouldCancel(boolean shouldCancel) {
		this.shouldCancel = shouldCancel;
	}

	public boolean isShouldCancel() {
		return shouldCancel;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void showExportDialog() {

		final JTextField jtfOutDir = new JTextField(25);
		final JTextField jtfOutFile = new JTextField(10);

        String dir = WCTProperties.getWCTProperty("math_export_dir");
        if (dir == null) {
            dir = "";
        }
        String file = WCTProperties.getWCTProperty("math_export_file");
        if (file == null) {
        	file = "";
        }
        jtfOutDir.setText(dir);
        jtfOutFile.setText(file);
        
        // JIDE stuff
        jtfOutDir.setName("File IntelliHint");
        SelectAllUtils.install(jtfOutDir);
        new FileIntelliHints(jtfOutDir).setFolderOnly(true);

        
        
		final JComboBox jcomboFormat = new JComboBox(ExportFormat.values());
		jcomboFormat.removeItem(ExportFormat.NATIVE);
		jcomboFormat.removeItem(ExportFormat.CSV);
		jcomboFormat.removeItem(ExportFormat.GML);
		jcomboFormat.removeItem(ExportFormat.KMZ);
		jcomboFormat.removeItem(ExportFormat.RAW_NETCDF);
		jcomboFormat.removeItem(ExportFormat.SHAPEFILE);
		jcomboFormat.removeItem(ExportFormat.VTK);
		jcomboFormat.removeItem(ExportFormat.WKT);
		jcomboFormat.removeItem(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		
		final JButton jbBrowse = new JButton("Browse");
		final Component finalThis = this;
		final String lastDir = dir;
		final String lastFile = file;
		jbBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                
                FolderChooser folderChooser = new FolderChooser() {
                    protected JDialog createDialog(Component parent)
                    	throws HeadlessException {
                    	JDialog dlg = super.createDialog(parent);
                    	dlg.setLocation((int)jbBrowse.getLocationOnScreen().getX()+jbBrowse.getWidth()+4, 
                    			(int)jbBrowse.getLocationOnScreen().getY());
                    	return dlg;
                    }
                };
                folderChooser.setRecentListVisible(false);
                folderChooser.setAvailableButtons(FolderChooser.BUTTON_REFRESH | FolderChooser.BUTTON_DESKTOP 
                		| FolderChooser.BUTTON_MY_DOCUMENTS);
                if (lastDir.length() > 0) {
                    File lastFolderFile = folderChooser.getFileSystemView().createFileObject(lastDir);
                    folderChooser.setCurrentDirectory(lastFolderFile);
                }
                folderChooser.setFileHidingEnabled(true);
                folderChooser.setPreferredSize(new Dimension(320, 500));
                int result = folderChooser.showOpenDialog(finalThis);
                if (result == FolderChooser.APPROVE_OPTION) {
                    File selectedFile = folderChooser.getSelectedFile();
                    if (selectedFile != null) {
                    	jtfOutDir.setText(selectedFile.toString());
                    }
                    else {
                    	jtfOutDir.setText("");
                    }

                }
                
            }
        });
		final JDialog exportDialog = new JDialog(this, 
				"Math Results Export", ModalityType.DOCUMENT_MODAL);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(WCTUiUtils.myTitledTopBorder("Select Export Format and File", 0, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

		mainPanel.setLayout(new RiverLayout());
		mainPanel.add("p", new JLabel("Output Location: "));
//		mainPanel.add(new JLabel(" "), "tab hfill");
//		mainPanel.add(jbBrowse, "right");
				
		mainPanel.add(jtfOutDir, "br left hfill");
		mainPanel.add(jbBrowse, "right");
		mainPanel.add(new JLabel("Format: "), "p left");
		mainPanel.add(jcomboFormat, "tab hfill");
		mainPanel.add(new JLabel("Filename: "), "p");
		mainPanel.add(jtfOutFile, "tab hfill");
		
		JButton jbExport = new JButton("Export");
		jbExport.setPreferredSize(new Dimension(60, (int)jbExport.getPreferredSize().getHeight()));		
		JButton jbCancel = new JButton("Cancel");
		jbCancel.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
		
		jbExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				exportDialog.dispose();
				try {
					doExportInBackground(jtfOutDir.getText(), jtfOutFile.getText(), (ExportFormat)jcomboFormat.getSelectedItem());
				} catch (Exception e) {
					reportException(e);
				}
			}			
		});		
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
//				process = false;
				exportDialog.dispose();
			}			
		});
		mainPanel.add(jbExport, "p center");
		mainPanel.add(jbCancel);
		
		exportDialog.add(mainPanel);
		
		exportDialog.pack();
		exportDialog.setLocationRelativeTo(this);
		exportDialog.setVisible(true);
		
		
		
		System.out.println(jtfOutDir);
		System.out.println(jtfOutFile);
		System.out.println(jcomboFormat.getSelectedItem());
	}
	
	
	private void doExportInBackground(final String outDir, final String outFile, final ExportFormat format) throws Exception {
		
            foxtrot.Worker.post(new foxtrot.Task() {
                public Object run() {

                    try {
                    	doExport(outDir, outFile, format);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        reportException(ex);
                    }

                    return "DONE";
                }
            });

	}
	
	
	private void doExport(final String outDir, String outFile, final ExportFormat format) {

        WCTProperties.setWCTProperty("math_export_dir", outDir);
        WCTProperties.setWCTProperty("math_export_file", outFile);

		
		final String finalOutFile = outFile;
    	WCTRasterExport rasterExport = new WCTRasterExport();
    	rasterExport.addGeneralProgressListener(new GeneralProgressListener() {
			@Override
			public void started(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
			}
			@Override
			public void ended(GeneralProgressEvent event) {
				progressBar.setValue(0);
				progressBar.setString("");
			}
			@Override
			public void progress(GeneralProgressEvent event) {
				progressBar.setValue((int)event.getProgress());
				progressBar.setString("Saving File: "+(int)event.getProgress()+"% complete");
			}    		
    	});
    	
    	try {
    		
    		
    		if (format == ExportFormat.ARCINFOASCII) {
    			if (! outFile.endsWith(".asc")) {
    				outFile = outFile + ".asc";
    			}
    			rasterExport.saveAsciiGrid(new File(outDir+File.separator+outFile), processRaster);
    		}
    		else if (format == ExportFormat.ARCINFOBINARY) {
    			if (! outFile.endsWith(".flt")) {
    				outFile = outFile + ".flt";
    			}
    			rasterExport.saveBinaryGrid(new File(outDir+File.separator+outFile), processRaster, true);
    		}
    		else if (format == ExportFormat.GRIDDED_NETCDF) {
    			if (! outFile.endsWith(".nc")) {
    				outFile = outFile + ".nc";
    			}
    			rasterExport.saveNetCDF(new File(outDir+File.separator+outFile), processRaster);
    		}
    		else if (format == ExportFormat.GEOTIFF_32BIT) {
    			if (! outFile.endsWith(".tif")) {
    				outFile = outFile + ".tif";
    			}
    			rasterExport.saveGeoTIFF(new File(outDir+File.separator+outFile), processRaster, GeoTiffType.TYPE_32_BIT);
    		}
    		else if (format == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
    			if (! outFile.endsWith(".tif")) {
    				outFile = outFile + ".tif";
    			}
    			rasterExport.saveGeoTIFF(new File(outDir+File.separator+outFile), processRaster, GeoTiffType.TYPE_8_BIT);
    		}
		} catch (WCTExportNoDataException e1) {
			e1.printStackTrace();
		} catch (WCTExportException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}	
	}
}
