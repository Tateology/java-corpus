package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.OpenFileFilter;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXDialog;

public class KmzExportDialog extends JXDialog {

	private WCTViewer viewer;

    private JTextField jtfOutputFile;
    private JComboBox jcomboAltitude, jcomboColladaAltitude;
    private JSpinner radialElevationExSpinner, generalElevationExSpinner;
    private JCheckBox jcbGeneralCreateShadow, jcbRadialCreateShadow;
    private JButton goButton;
    private JButton cancelButton;

    
    
    final JCheckBox jcbEngageCustom = new JCheckBox("");
    final JSpinner radSmoothingCustom = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    final JComboBox radTransparencyCustom = new JComboBox(new Object[] {
            "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
            " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
    });      
    final JComboBox radialMinValueComboCustom = new JComboBox(new Object[] { "NONE", "20.0", "25.0", "30.0", "35.0", "40.0", "45.0" });
    
    

    
	private JComboBox jcomboSweeps = new JComboBox(new String[] {
			"1 (Current Sweep)", "3 (Lowest 3 Sweeps)",
			"5 (Lowest 5 Sweeps)", "7 (Lowest 7 Sweeps)", "All (Full Volume Scan)"
	});

	private JComboBox jcomboSettings = new JComboBox(new String[] {
			"None - Use current viewer settings",
			"'REF Smooth-6'",
			"'REF Smooth-10'",
			"'REF Filter >20 dBZ'",
			"'REF Filter >25 dBZ'",
			"'REF Filter >30 dBZ'",
			"'REF Filter >35 dBZ'",
			"'REF Filter >40 dBZ'",
			"'REF Smooth-6  >20 dBZ'",
			"'REF Smooth-10 >20 dBZ'",
			"'REF Smooth-6  >25 dBZ'",
			"'REF Smooth-10 >25 dBZ'",
			"'REF Smooth-6  >30 dBZ'",
			"'REF Smooth-10 >30 dBZ'",
			"'REF Smooth-6  >35 dBZ'",
			"'REF Smooth-10 >35 dBZ'",
			"'REF Smooth-6  >40 dBZ'",
			"'REF Smooth-10 >40 dBZ'"
	});
    
    private boolean formSubmitted = false;
    private boolean isColladaCapable;
    private SupportedDataType dataType;
    private boolean showFileChoiceSection;

    public KmzExportDialog(WCTViewer viewer, boolean isColladaCapable, SupportedDataType dataType) {
    	this(viewer, isColladaCapable, dataType, true);
    }

    public KmzExportDialog(WCTViewer viewer, boolean isColladaCapable, SupportedDataType dataType, boolean showFileChoiceSection) {
        super(viewer, new JPanel());
        setModal(true);
        
        this.viewer = viewer;
        this.isColladaCapable = isColladaCapable;
        this.dataType = dataType;
        this.showFileChoiceSection = showFileChoiceSection;
        
        setTitle("KMZ Export");
        createGUI();
        
    }


    private void createGUI() {

        String dir = WCTProperties.getWCTProperty("kmzsavedir");
        if (dir == null) {
            dir = "";
        }
        else {
            dir = dir + File.separator + "export.kmz"; 
        }
        
        jtfOutputFile = new JTextField(dir, 30);
        jcomboAltitude = new JComboBox(new String[] {
                "Drape over terrain, even if real vertical data value present",
        		"Real vertical height if present, otherwise drape on surface",
                "10,000",
                "20,000",
                "30,000"
        });
        jcomboColladaAltitude = new JComboBox(new String[] {
                "Drape on actual radar sweep 3D model",
                "Drape over surface terrain"
        });
        final String[] colladaAltitudeInfo = new String[] {
        		"(Actual height of radar beam center)",
        		"(Does not represent the actual height of the data)"
        };
        final String[] generalAltitudeInfo = new String[] {
        		" ",
        		" ",
        		"(This height is for visual effect only)",
        		"(This height is for visual effect only)",
        		"(This height is for visual effect only)"
        };
        String elevExValue = WCTProperties.getWCTProperty("kmz-elev-exaggeration");
        radialElevationExSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        generalElevationExSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        if (elevExValue != null) {
        	radialElevationExSpinner.setValue(Integer.parseInt(elevExValue));
        	generalElevationExSpinner.setValue(Integer.parseInt(elevExValue));
        }
        
        jcomboAltitude.setEditable(true);


        jcbGeneralCreateShadow = new JCheckBox("", true);
        jcbGeneralCreateShadow.setEnabled(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
//      mainPanel.add("p center", new JLabel("KMZ Export Options"));
        mainPanel.setBorder(WCTUiUtils.myTitledBorder("KMZ Export Options", 8));

        
        final JLabel generalAltitudeLabel = new JLabel(generalAltitudeInfo[0]);
        JPanel elevPanel = new JPanel(new RiverLayout());
        elevPanel.add("p left", new JLabel("Altitude (m): "));
        elevPanel.add("tab", jcomboAltitude);
        elevPanel.add("br tab", generalAltitudeLabel);
        elevPanel.add("p left", new JLabel("Elevation Exaggeration: "));
        elevPanel.add("tab", generalElevationExSpinner);

        final JLabel radialInfoLabel = new JLabel(colladaAltitudeInfo[0]);
        final JPanel radialPanel = new JPanel(new RiverLayout());
        radialPanel.add("p left", new JLabel("Altitude (m): "));
        radialPanel.add("tab ", jcomboColladaAltitude);
        radialPanel.add("br tab", radialInfoLabel);
        radialPanel.add("p left", new JLabel("Elevation Exaggeration: "));
        radialPanel.add("tab", radialElevationExSpinner);
        jcbRadialCreateShadow = new JCheckBox("", true);
        radialPanel.add("p left", new JLabel("Create Shadow? "));
        radialPanel.add("tab hfill", jcbRadialCreateShadow);
        
        
        
        radTransparencyCustom.setEditable(true);
        radialMinValueComboCustom.setEditable(true);

        radSmoothingCustom.setValue((int)viewer.getRadarSmoothFactor());
        radTransparencyCustom.setSelectedItem(viewer.getMapSelector().getRadarTransparency());
        if (viewer.getFilterGUI().getRadialAttributeFilterPanel().getRadialMinValues()[0] != WCTFilter.NO_MIN_VALUE) {
        	radialMinValueComboCustom.setSelectedItem(viewer.getFilterGUI().getRadialAttributeFilterPanel().getRadialMinValues()[0]);
        }
        
        ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jcbEngageCustom.setSelected(true);
			}
        };
        ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				jcbEngageCustom.setSelected(true);
			}
        };
        radSmoothingCustom.addChangeListener(changeListener);
        radTransparencyCustom.addActionListener(actionListener);
        radialMinValueComboCustom.addActionListener(actionListener);
        
        
        final JPanel customSettingsPanel = new JPanel(new RiverLayout());
        if (dataType == SupportedDataType.RADIAL) {
            radialPanel.add("p left", new JLabel("Sweeps to Include: "));
            radialPanel.add("tab", jcomboSweeps);
            
            customSettingsPanel.setBorder(WCTUiUtils.myTitledBorder("Custom Display Settings", 3));
            customSettingsPanel.add("left", new JLabel("Engage Settings Override: "));
            customSettingsPanel.add("tab", jcbEngageCustom);
            customSettingsPanel.add("br", new JLabel("Smoothing Factor: "));
            customSettingsPanel.add("tab", radSmoothingCustom);
            customSettingsPanel.add("br", new JLabel("Min Value Filter: "));
            customSettingsPanel.add("tab", radialMinValueComboCustom);
            customSettingsPanel.add("br", new JLabel("Transparency (%): "));
            customSettingsPanel.add("tab", radTransparencyCustom);
            
            radialPanel.add("p left", customSettingsPanel);
        }
        
        jcomboAltitude.addActionListener(new ActionListener() {
			@Override
	        public void actionPerformed(ActionEvent e) {
				generalAltitudeLabel.setText(generalAltitudeInfo[jcomboAltitude.getSelectedIndex()]);
	            if (jcomboAltitude.getSelectedItem().toString().startsWith("Drape")) {
	                jcbGeneralCreateShadow.setEnabled(false);
	            }
	            else {
	                jcbGeneralCreateShadow.setEnabled(true);
	            }
	        } 	
        });
        
        jcomboColladaAltitude.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jcbRadialCreateShadow.setEnabled(jcomboColladaAltitude.getSelectedIndex() == 0);
				radialElevationExSpinner.setEnabled(jcomboColladaAltitude.getSelectedIndex() == 0);
				radialInfoLabel.setText(colladaAltitudeInfo[jcomboColladaAltitude.getSelectedIndex()]);
				
                jcomboSweeps.setEnabled(jcomboColladaAltitude.getSelectedIndex() == 0);
                for (Component c : customSettingsPanel.getComponents()) {
                	c.setEnabled(jcomboColladaAltitude.getSelectedIndex() == 0);
                }

			}        	
        });

        if (isColladaCapable) {
//        	mainPanel.add(new JLabel("Radial Data KMZ Options"), BorderLayout.NORTH);
            mainPanel.add(radialPanel, BorderLayout.CENTER);
            jcbGeneralCreateShadow.setEnabled(true);
        }
        else {
        	jcbGeneralCreateShadow.setEnabled(false);
//        	mainPanel.add(new JLabel("Standard KMZ Options"), BorderLayout.NORTH);
            mainPanel.add(elevPanel, BorderLayout.CENTER);
        }
        

        JPanel outputPanel = new JPanel(new RiverLayout());
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new BrowseListener(this));
        outputPanel.add("p left", new JLabel("Output File: "));
        outputPanel.add("tab hfill", jtfOutputFile);
        outputPanel.add(browseButton);
        if (showFileChoiceSection) {
        	mainPanel.add(outputPanel, BorderLayout.SOUTH);
        }

        JPanel buttonPanel = new JPanel(new RiverLayout());
        goButton = new JButton("Go");
        goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
        goButton.addActionListener(new SubmitListener(this));
        
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
        cancelButton.addActionListener(new SubmitListener(this));
        buttonPanel.add("p center", goButton);
        buttonPanel.add(cancelButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        
        
        
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.remove(enterKey);
//        this.getRootPane().setDefaultButton(null);
        this.getRootPane().setDefaultButton(goButton);
    }






    private final class BrowseListener implements ActionListener {

        private Dialog parent;

        public BrowseListener(Dialog parent) {
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            // Set up File Chooser
            JFileChooser fc = new JFileChooser(WCTProperties.getWCTProperty("kmzsavedir"));
            //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle("Choose output file");
            OpenFileFilter kmzFilter = new OpenFileFilter("kmz", true, "KMZ Files");
            fc.addChoosableFileFilter(kmzFilter);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(kmzFilter);

            int returnVal = fc.showSaveDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {




                File outFile = fc.getSelectedFile();
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
                    int choice = JOptionPane.showOptionDialog(parent, message, "OVERWRITE IMAGE FILE",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    if (choice == 1) { // NO
                        return;
                    }
                }
                System.out.println("SAVING: " + outFile);
                jtfOutputFile.setText(outFile.toString());

            }
        }

    }

    
    
    /**
     * If the 'Go' button has been pressed, this will return true.  Otherwise, if false it means canceled.
     * @return
     */
    public boolean isSubmitted() {
        return formSubmitted;
    }
    
    /**
     * Returns the output file
     * @return
     */
    public String getOutputFile() {
        return jtfOutputFile.getText();
    }
    
    public void setOutputFile(String outFile) {
    	jtfOutputFile.setText(outFile);
    }
    
    /**
     * Should we create the 'shadow' images draped over the surface?
     * @return
     */
    public boolean isGeneralCreateShadow() {
        return jcbGeneralCreateShadow.isEnabled() && jcbGeneralCreateShadow.isSelected();
    }
    
    /**
     * Should we create the 'shadow' images draped over the surface?
     * @return
     */
    public boolean isRadialCreateShadow() {
        return jcbRadialCreateShadow.isEnabled() && jcbRadialCreateShadow.isSelected();
    }
    
    /**
     * Returns the altitude to place the image, 'Double.POSITIVE_INFINITY' to use real height dimension
     * value from data if available (or drape if not available), or 'Double.NaN' to drape over terrain 
     * even if a height dimension is present.
     * @return
     */
    public double getAltitude() {
        String str = jcomboAltitude.getSelectedItem().toString();
        double alt = Double.NaN;
        if (str.startsWith("Drape")) {
            return Double.NaN;
        }
        else if (str.startsWith("Real")) {
        	return Double.POSITIVE_INFINITY;
        }
        else {
            try {
                alt = Double.parseDouble(str.replaceAll(",", ""));
                return alt;
            } catch (Exception e) {
                return Double.NaN;
            }
        }        
    }
    
    /**
     * Is the 'drape on collada' selected
     * @return
     */
	public boolean isDrapeOnColladaSelected() {
		return (isColladaCapable && jcomboColladaAltitude.getSelectedIndex() == 0);
	}

	public int getElevationExaggeration() {
		if (isColladaCapable) {
			return Integer.parseInt(radialElevationExSpinner.getValue().toString());
		}
		else {
			return Integer.parseInt(generalElevationExSpinner.getValue().toString());
		}
	}

	public String getNumberOfSweepsToProcess() {
		return jcomboSweeps.getSelectedItem().toString();
	}
	
	public boolean isCustomSettingsEngaged() {
		return jcbEngageCustom.isSelected();
	}
	
    public int getRadarTransparencyCustom() {
        if (radTransparencyCustom.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
            return -1;
        }
        else {
            return Integer.parseInt(radTransparencyCustom.getSelectedItem().toString().replaceAll("%", "").trim());
        }
    }
	
    public int getRadarSmoothingFactor() {
        return (int)Double.parseDouble(radSmoothingCustom.getValue().toString());
    }

    public double getRadialMinValueCustom() {
        try {
            return (Double.parseDouble(radialMinValueComboCustom.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MIN_VALUE);
        }
    }

	
	
	
	
	private void saveProperties() {
		if (isColladaCapable) {
			WCTProperties.setWCTProperty("kmz-elev-exaggeration", String.valueOf(radialElevationExSpinner.getValue()));
		}
		else {
			WCTProperties.setWCTProperty("kmz-elev-exaggeration", String.valueOf(generalElevationExSpinner.getValue()));
		}
	}


	private final class SubmitListener implements ActionListener {
        private Dialog parent;
        public SubmitListener(Dialog parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == goButton) {
            	saveProperties();
                formSubmitted = true;
            }
            parent.dispose();
        }


    }

}