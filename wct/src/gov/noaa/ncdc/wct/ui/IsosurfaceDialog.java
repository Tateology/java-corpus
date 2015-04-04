package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster.CAPPIType;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.isosurface.WCTIsoSurface;
import gov.noaa.ncdc.wct.isosurface.WCTIsoSurface.KMLShapeType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.geotools.cv.SampleDimension;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.StackedBox;

public class IsosurfaceDialog extends JXDialog {

	private WCTViewer viewer;
	
	private WCTIsoSurface isogen = new WCTIsoSurface();

	
	private JSpinner radialQualitySpinner = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));
	private double[] radialQualityCellsizeArray = new double[] {
			0.01, 0.0075, 0.006, 0.005, 0.004, 0.003, 0.002, 0.001, 0.0008, 0.0006, 0.0004     
	};
	private int[] radialQualityGridSizeMaxArray = new int[] {
			80, 140, 220, 300, 500, 650, 850, 1100, 1400, 1700, 2000  
	};
	
	private JSpinner radialSmoothingSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1));
	private JSpinner numCappisSpinner = new JSpinner(new SpinnerNumberModel(12, 2, 500, 1));
	private JComboBox<String> cappiSpacingInMetersCombo = new JComboBox<String>(new String[] {
    	"500", "1000", "1500", "2000", "2500", "3000"	
    });
	private JComboBox<String> radialIsosurfaceValues = new JComboBox<String>(new String[] {
		"42", "45", "47", "50", "42, 47, 53", "32, 42, 47, 53"
	});
	private JComboBox<String> gridIsosurfaceValues = new JComboBox<String>();

	private JComboBox<String> radialIsosurfaceShapeType = new JComboBox<String>(new String[] {
			"Polygon", "Mesh"
	});
	private JComboBox<String> gridIsosurfaceShapeType = new JComboBox<String>(new String[] {
			"Polygon", "Mesh"
	});
	private JSpinner gridElevationExaggerationSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 150, 1));
			
	private JComboBox<String> cappiTypeCombo = new JComboBox<String>(
			new String[] { "IDW Squared", "Linear Weighted Avg.", 
			"Nearest Sweep Value", "Nearest Sweep Elev. Info"
			});

	private JCheckBox jcbGridAddTour = new JCheckBox("", true);
	private JCheckBox jcbRadialAddTour = new JCheckBox("", true);
	
	
	private JProgressBar progressBar = new JProgressBar();
	
	public IsosurfaceDialog(WCTViewer viewer) {
		super(viewer, new JPanel());
		
		this.viewer = viewer;
		
        setModal(true);
        setTitle("Isosurface KML Generator (BETA)");
        
        createGUI();
        
        pack();
        setLocation(viewer.getX()+45, viewer.getY()+45);
        setVisible(true);
	}
	
	
	
	
	private void createGUI() {
		

		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setString("Processing Progress");
		
		
		this.isogen.addDataDecodeListener(new DataDecodeListener() {
			@Override
			public void decodeStarted(DataDecodeEvent event) {
				progressBar.setValue(0);
				progressBar.setString("Generating Isosurfaces...");
			}
			@Override
			public void decodeEnded(DataDecodeEvent event) {
				progressBar.setValue(100);
				progressBar.setString("Generating Isosurfaces...");
			}
			@Override
			public void decodeProgress(DataDecodeEvent event) {
				progressBar.setValue(event.getProgress());
				progressBar.setString(event.getStatus());
			}
			@Override
			public void metadataUpdate(DataDecodeEvent event) {
			}
		});

		
		
		
		
        String dir = WCTProperties.getWCTProperty("kmzsavedir");
        if (dir == null) {
            dir = "";
        }
        else {
            dir = dir + File.separator + "export.kmz"; 
        }

        
        
        
        
        
        
        try {
        
        
        JPanel mainPanel = new JPanel(new RiverLayout());
//        mainPanel.setBorder(WCTUiUtils.myTitledTopBorder("Isosurface Tool (for Grid and Radial data)", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));
    
        JPanel radialOptionsPanel = getRadialOptionsPanel();
        JPanel gridOptionsPanel = getGridOptionsPanel();

        JButton goButton = new JButton("Start");
        goButton.setActionCommand("SUBMIT");
        goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
        goButton.addActionListener(new SubmitListener(this));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
        cancelButton.addActionListener(new SubmitListener(this));

        if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {        	
        	mainPanel.add("p hfill", gridOptionsPanel);        
        }
        else if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
        	mainPanel.add("p hfill", radialOptionsPanel);   
        }
        else {
        	mainPanel.add("p hfill", new JLabel("This data type is not supported.  "));
        	cancelButton.setText("Close");
        	goButton.setEnabled(false);
        }

        

        
        JPanel buttonPanel = new JPanel(new RiverLayout());

        buttonPanel.add("p hfill", progressBar);
        buttonPanel.add("p center", goButton);
        buttonPanel.add(cancelButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        
        } catch (Exception e) {
            JButton cancelButton = new JButton("Close");
            cancelButton.setActionCommand("CANCEL");
            cancelButton.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
            cancelButton.addActionListener(new SubmitListener(this));
            
            this.getContentPane().removeAll();
        	this.getContentPane().setLayout(new RiverLayout());
        	this.getContentPane().add("p", new JLabel(
        			"Isosurface Generation is not available for this file."));
        	this.getContentPane().add("p", cancelButton);
        }
		
	}

	private JPanel getGridOptionsPanel() {
		
		JPanel gridOptionsPanel = new JPanel(new RiverLayout());
		
		gridIsosurfaceValues.setEditable(true);

        String isoUnits = "";
		try {
			isoUnits = viewer.getGridDatasetRaster().getUnits();
			double minValue = viewer.getGridDatasetRaster().getDisplayMinValue();
			double maxValue = viewer.getGridDatasetRaster().getDisplayMaxValue();
			double stepSize = (maxValue-minValue)/7;
			for (double x=minValue+stepSize; x<maxValue-stepSize; x=x+stepSize) {
				if (stepSize >= 1) {
					gridIsosurfaceValues.addItem(WCTUtils.DECFMT_0.format(x));
				}
				else if (stepSize < 1 && stepSize >= 0.1) {
					gridIsosurfaceValues.addItem(WCTUtils.DECFMT_0D0.format(x));
				}
				else if (stepSize < 0.1 && stepSize >= 0.01) {
					gridIsosurfaceValues.addItem(WCTUtils.DECFMT_0D00.format(x));
				}
				else if (stepSize < 0.01 && stepSize >= 0.001) {
					gridIsosurfaceValues.addItem(WCTUtils.DECFMT_0D000.format(x));
				}
				else if (stepSize < 0.001 && stepSize >= 0.0001) {
					gridIsosurfaceValues.addItem(WCTUtils.DECFMT_0D0000.format(x));
				}
				else {
					gridIsosurfaceValues.addItem(String.valueOf(x));
				}
			}
			gridIsosurfaceValues.addItem(gridIsosurfaceValues.getItemAt(3)+","+gridIsosurfaceValues.getItemAt(4));
			gridIsosurfaceValues.addItem(gridIsosurfaceValues.getItemAt(2)+","+gridIsosurfaceValues.getItemAt(3)
					+","+gridIsosurfaceValues.getItemAt(4));
			
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

        gridOptionsPanel.setBorder(WCTUiUtils.myTitledTopBorder("Isosurface Tool (Grid data)", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));
        gridOptionsPanel.add("p", new JLabel("Isosurface Value(s) ("+isoUnits+"): "));
        gridOptionsPanel.add("tab hfill", gridIsosurfaceValues);
        gridOptionsPanel.add("br", new JLabel("Elevation Exaggeration: "));
        gridOptionsPanel.add("tab hfill", gridElevationExaggerationSpinner);
        gridOptionsPanel.add("br", new JLabel("KML Shape Type: "));
        gridOptionsPanel.add("tab hfill", gridIsosurfaceShapeType);
        gridOptionsPanel.add("br", new JLabel("Add KML Tour?: "));
        gridOptionsPanel.add("tab hfill", jcbGridAddTour);
        

		
		return gridOptionsPanel;
	}
	
	private JPanel getRadialOptionsPanel() {
		
		JPanel radialOptionsPanel = new JPanel(new RiverLayout());
	      
        // smoothing, num cappis, cappi spacing, resampled grid cell size
        cappiSpacingInMetersCombo.setEditable(true);
        cappiSpacingInMetersCombo.setSelectedItem("1500");
        radialIsosurfaceValues.setEditable(true);
        radialIsosurfaceValues.setSelectedItem("42, 47, 53");
        String isoUnits = viewer.getRadialRemappedRaster().getUnits();
        
        radialOptionsPanel.setBorder(WCTUiUtils.myTitledTopBorder("Isosurface Tool (Radial data)", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

        
        radialOptionsPanel.add("p", new JLabel("Isosurface Value(s) ("+isoUnits+"): "));
        radialOptionsPanel.add("tab hfill", radialIsosurfaceValues);
        radialOptionsPanel.add("br", new JLabel("KML Shape Type: "));
        radialOptionsPanel.add("tab hfill", radialIsosurfaceShapeType);
        radialOptionsPanel.add("br", new JLabel("Add KML Tour?: "));
        radialOptionsPanel.add("tab hfill", jcbRadialAddTour);
        
        
        JPanel advancedOptions = new JPanel(new RiverLayout());
        advancedOptions.add("p", new JLabel("<html>Quality: <br>( Higher=Larger Output File )   </html>"));
        advancedOptions.add("tab hfill", radialQualitySpinner);
        advancedOptions.add("br", new JLabel("Smoothing Factor: "));
        advancedOptions.add("tab hfill", radialSmoothingSpinner);
        advancedOptions.add("br", new JLabel("Number of CAPPIs: "));
        advancedOptions.add("tab hfill", numCappisSpinner);
        advancedOptions.add("br", new JLabel("CAPPI Spacing (m): "));
        advancedOptions.add("tab hfill", cappiSpacingInMetersCombo);     
        advancedOptions.add("br", new JLabel("CAPPI Type: "));
        advancedOptions.add("tab hfill", cappiTypeCombo);      
        StackedBox stackedBox = new StackedBox();
        stackedBox.setTitleBackgroundColor(this.getContentPane().getBackground());
        stackedBox.setSeparatorColor(this.getContentPane().getBackground());
        stackedBox.setBackground(this.getContentPane().getBackground());

        stackedBox.addBox("Advanced", advancedOptions, false, -1, true);
        
        radialOptionsPanel.add("p hfill", stackedBox);
        
        return radialOptionsPanel;
	}
	
	private void processIsosurface() {
		
		isogen.setRadialCappiSpacingInMeters(Double.parseDouble(cappiSpacingInMetersCombo.getSelectedItem().toString()));
		isogen.setRadialExportGridSmoothFactor(Integer.parseInt(radialSmoothingSpinner.getValue().toString()));
		isogen.setRadialNumCappis(Integer.parseInt(numCappisSpinner.getValue().toString()));
		isogen.setRadialRemappedGridCellsize(radialQualityCellsizeArray[Integer.parseInt(radialQualitySpinner.getValue().toString())]);
		isogen.setRadialRemappedGridSizeMax(radialQualityGridSizeMaxArray[Integer.parseInt(radialQualitySpinner.getValue().toString())]);
		isogen.setGridSizeMax(radialQualityGridSizeMaxArray[Integer.parseInt(radialQualitySpinner.getValue().toString())]);
		if (cappiTypeCombo.getSelectedIndex() == 0) {
			isogen.setRadialCappiType(CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED);
		}
		else if (cappiTypeCombo.getSelectedIndex() == 1) {
			isogen.setRadialCappiType(CAPPIType.LINEAR_WEIGHTED_AVERAGE);
		}
		else if (cappiTypeCombo.getSelectedIndex() == 2) {
			isogen.setRadialCappiType(CAPPIType.NEAREST_SWEEP);
		}
		else if (cappiTypeCombo.getSelectedIndex() == 3) {
			isogen.setRadialCappiType(CAPPIType.NEAREST_ELEVATION_ANGLE);
		}
		

		
		IsosurfaceDialog finalThis = this;

        try {

    		String uniqueString = String.valueOf(System.currentTimeMillis()%1000000);    
    		
    		File outKmlFile = new File(WCTConstants.getInstance().getDataCacheLocation()+
        			File.separator+"iso-"+uniqueString+".kml");
    		
        	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
        		
        		isogen.setTourSelected(jcbGridAddTour.isSelected());
        		isogen.setShapeType(KMLShapeType.valueOf(gridIsosurfaceShapeType.getSelectedItem().toString().toUpperCase()));

        		System.out.println(Arrays.toString(viewer.getGridSatelliteGridCoverage().
        				getSampleDimensions()[0].getCategory(0).getColors()));

        		Color[] colors = viewer.getGridSatelliteGridCoverage().
        		getSampleDimensions()[0].getCategory(0).getColors();

//        		NumberRange numRange = viewer.getGridSatelliteGridCoverage().
//        		getSampleDimensions()[0].getCategory(0).getRange();
//        		double minVal = numRange.getMinimum();
//        		double maxVal = numRange.getMaximum();

				double minVal = viewer.getGridDatasetRaster().getDisplayMinValue();
				double maxVal = viewer.getGridDatasetRaster().getDisplayMaxValue();
        		

        		String[] isoValStrings = gridIsosurfaceValues.getSelectedItem().toString().split(",");
        		float[] isoVals = new float[isoValStrings.length];
        		String[] hexColors = new String[isoValStrings.length];
        		for (int n=0; n<isoVals.length; n++) {
        			isoVals[n] = Float.parseFloat(isoValStrings[n]);

        			Color isoColor = ColorsAndValues.getColor(colors, 
        					minVal, maxVal, isoVals[n]);

        			System.out.println("color found for iso value of "+isoVals[n]+" = "+isoColor);
        			String hexColor = Integer.toHexString(isoColor.getRGB());
        			System.out.println("hex color = "+hexColor);
        			hexColor = hexColor.substring(0, 2) + hexColor.substring(6,8) + 
        			hexColor.substring(4, 6) + hexColor.substring(2, 4);
        			hexColors[n] = hexColor;
        		}

        		
        		
        		int elevExag = Integer.parseInt(gridElevationExaggerationSpinner.getValue().toString());

//        		Color isoColor = ColorsAndValues.getColor(colors, 
//        				viewer.getGridDatasetRaster().getDisplayMinValue(),
//        				viewer.getGridDatasetRaster().getDisplayMaxValue(), 
//        				isoVal);


//        		System.out.println("color found for iso value of "+isoVal+" = "+isoColor);
//        		String hexColor = Integer.toHexString(isoColor.getRGB());
//        		System.out.println("hex color = "+hexColor);
//        		hexColor = hexColor.substring(0, 2) + hexColor.substring(6,8) + 
//        		hexColor.substring(4, 6) + hexColor.substring(2, 4);

        		//        						URL url = WCTDataUtils.scan(getSelectedURLs()[0], 
        		//        								viewer.getFileScanner(), true, true, getSelectedDataType());
        		URL url = viewer.getCurrentDataURL();
        		isogen.generateIsosurfaceKML(
        				url.toString(), 
        				viewer.getGridProps().getSelectedGridIndex(), 
        				viewer.getCurrentExtent(),
        				elevExag,
//        				new float[] { isoVal }, 
//        				new String[] { hexColor }, 
        				isoVals, 
        				hexColors, 
        				outKmlFile);


        		Desktop.getDesktop().open(outKmlFile);
        	                	
        	}
        	else if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {

        		isogen.setTourSelected(jcbRadialAddTour.isSelected());
        		isogen.setShapeType(KMLShapeType.valueOf(radialIsosurfaceShapeType.getSelectedItem().toString().toUpperCase()));

        		
        		
//        		System.out.println(Arrays.toString(viewer.getRadarGridCoverage().
//        				getSampleDimensions()[0].getCategory(0).getColors()));
//
//        		Color[] colors = viewer.getRadarGridCoverage().
//        		getSampleDimensions()[0].getCategory(0).getColors();
//
//        		NumberRange numRange = viewer.getRadarGridCoverage().
//        		getSampleDimensions()[0].getCategory(0).getRange();
//        		double minVal = numRange.getMinimum();
//        		double maxVal = numRange.getMaximum();

        		
        		SampleDimension sd = viewer.getRadarGridCoverage().getSampleDimensions()[0];

        		String[] isoValStrings = radialIsosurfaceValues.getSelectedItem().toString().split(",");
        		float[] isoVals = new float[isoValStrings.length];
        		String[] hexColors = new String[isoValStrings.length];
        		for (int n=0; n<isoVals.length; n++) {
        			isoVals[n] = Float.parseFloat(isoValStrings[n]);

//        			Color isoColor = ColorsAndValues.getColor(colors, 
//        					minVal, maxVal, isoVals[n]);

        			Color isoColor = ColorsAndValues.getColor(sd, isoVals[n]);
        			
        			if (isoColor == null) {
        				throw new Exception("No color could be found matching the value of "+isoVals[n]);
        			}
        			
        			System.out.println("color found for iso value of "+isoVals[n]+" = "+isoColor);
        			String hexColor = Integer.toHexString(isoColor.getRGB());
        			System.out.println("hex color = "+hexColor);
        			hexColor = hexColor.substring(0, 2) + hexColor.substring(6,8) + 
        			hexColor.substring(4, 6) + hexColor.substring(2, 4);
        			hexColors[n] = hexColor;
        		}



        		int cnt = 0;
        		for (URL url : viewer.getDataSelector().getSelectedURLs()) {


        			url = WCTDataUtils.scan(url, 
        					viewer.getFileScanner(), true, true, 
        					viewer.getDataSelector().getSelectedDataType());

//        			if (viewer.getDataSelector().getSelectedURLs().length == 1) {
//        				for (float iso : isoVals) {
//        					        				
//                			outKmlFile = new File(WCTConstants.getInstance().getDataCacheLocation()+
//                					File.separator+"iso-"+uniqueString+"-"+(cnt++)+".kml");
//
//        					isogen.generateIsosurfaceKML(
//            					url.toString(), 
//            					viewer.getRadialRemappedRaster().getVariableName(), 
//            					viewer.getCurrentExtent(),
//            					new float[] { iso }, hexColors, outKmlFile);
//
//        					Desktop.getDesktop().open(outKmlFile);
//        				}
//        			}
//        			else {
        			
            			outKmlFile = new File(WCTConstants.getInstance().getDataCacheLocation()+
            					File.separator+"iso-"+uniqueString+"-"+(cnt++)+".kml");

        				isogen.generateIsosurfaceKML(
        						url.toString(), 
        						viewer.getRadialRemappedRaster().getVariableName(), 
        						viewer.getCurrentExtent(),
        						isoVals, hexColors, outKmlFile);

        				Desktop.getDesktop().open(outKmlFile);

//        			}
        		}

        	}
        	
        	
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(finalThis, ex.getMessage());
        }

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

                            	
                                processIsosurface();

                        		progressBar.setValue(0);
                        		progressBar.setString("Processing Progress");

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            return "DONE";
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            	((JButton)e.getSource()).setEnabled(true);

            }
            else {
            	parent.dispose();
            }
        }


    }

}
