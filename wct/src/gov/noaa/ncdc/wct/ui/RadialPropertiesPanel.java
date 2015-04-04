package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster.CAPPIType;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormatType;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.RadialDatasetSweep;

public class RadialPropertiesPanel extends JPanel {

    private RadialDatasetSweep radialDataset;


    private JComboBox jcomboVariables;
    private JRadioButton[] cutButtons;
//    private JPanel cutSubPanel = new JPanel();
//    private JPanel cutPanel = new JPanel();
    
    private String currentVariableName;
    private int currentCutIndex;
    private double currentCappiInMeters = Double.NaN;
    private CAPPIType currentCappiType = CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED;
    
    private JPanel elevationPanel = new JPanel();

    private int columnNum;    
    private double[] elevations;
    private boolean exportMode = false;
    private boolean showExportAllSweepsOption = false;
    private boolean exportAllSweeps = false;

    public RadialPropertiesPanel(RadialDatasetSweep radialDataset) {
        this(radialDataset, 2, false, false);
    }
    
    public RadialPropertiesPanel(RadialDatasetSweep radialDataset, int numberOfElevationColumns, boolean exportMode, boolean showExportAllSweepsOption) {
        this.radialDataset = radialDataset;
        this.columnNum = numberOfElevationColumns;
        this.exportMode = exportMode;
        this.showExportAllSweepsOption = showExportAllSweepsOption;
        
        createUI();
    }

    
    
    private void createUI() {
        this.setLayout(new RiverLayout());
        
        JPanel variablesPanel = makeVariablesPanel();
        variablesPanel.setBorder(WCTUiUtils.myTitledTopBorder("Moment: ", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

        elevationPanel.add(makeCutButtons());
        elevationPanel.setBorder(WCTUiUtils.myTitledTopBorder("Elevation: ", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));

        JPanel cappiPanel = makeCAPPIPanel();
        cappiPanel.setBorder(WCTUiUtils.myTitledTopBorder("Additional Options: ", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));
        

        this.add(variablesPanel, "p center");
        this.add(elevationPanel, "p");
        this.add(cappiPanel, "p");
        this.add(new JLabel(" "), "p");
    }
    
    
    
    
    
    
    private JPanel makeCAPPIPanel() {
    	JPanel panel = new JPanel(new RiverLayout());
    	panel.add(new JLabel("Constant Altitude (CAPPI):"), "center");
    	String[] altList = new String[] { "NONE", "1000", "2000", "3000", 
    			"4000", "5000", "6000", "7000", "8000", "9000", "10000" };
    	String[] unitsList = new String[] { "Meters", "Feet", "KM", "Miles" };
    	String[] cappiTypeList = new String[] { "IDW Squared", "Linear Weighted Avg.", 
    			"Nearest Sweep Value", "Nearest Sweep Elev. Info"
    	};
    			
    	final JComboBox<String> jcomboAltList = new JComboBox<String>(altList);
    	final JComboBox<String> jcomboUnitsList = new JComboBox<String>(unitsList);
    	final JComboBox<String> jcomboCappiType = new JComboBox<String>(cappiTypeList);
    	jcomboCappiType.setEnabled(false);
    	final JLabel jlabelCappiType = new JLabel("CAPPI Type: ");
    	jlabelCappiType.setEnabled(false);
    	
    	final JCheckBox jcbAllPoints = new JCheckBox("Export All Sweeps? ", false);
    	final JLabel jlAllSweepsExportNote = new JLabel("(point export format only)");
    	
    	jcomboAltList.setEditable(true);
    	jcomboAltList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jcomboAltList.getSelectedItem().toString().equals("NONE")) {
					elevationPanel.setEnabled(true);
					setComponentsEnabled(elevationPanel, true);
					jcbAllPoints.setEnabled(true);
					jlAllSweepsExportNote.setEnabled(true);
					jcomboCappiType.setEnabled(false);
					jlabelCappiType.setEnabled(false);
					
					currentCappiInMeters = Double.NaN;
				}
				else {
					elevationPanel.setEnabled(false);
					setComponentsEnabled(elevationPanel, false);
					jcbAllPoints.setEnabled(false);
					jlAllSweepsExportNote.setEnabled(false);
					jcomboCappiType.setEnabled(true);
					jlabelCappiType.setEnabled(true);
					
					currentCappiInMeters = processHeight(
							jcomboAltList.getSelectedItem().toString(),
							jcomboUnitsList.getSelectedItem().toString());
					
					
					System.out.println("currentCappiHeight: "+currentCappiInMeters);
				}
				
			}
    	});
    	
    	jcomboUnitsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentCappiInMeters = processHeight(
						jcomboAltList.getSelectedItem().toString(),
						jcomboUnitsList.getSelectedItem().toString());
				System.out.println("currentCappiHeight: "+currentCappiInMeters);
			}
    	});
    	
    	jcomboCappiType.addActionListener(new ActionListener() {
    		@Override
			public void actionPerformed(ActionEvent e) {
				if (jcomboCappiType.getSelectedIndex() == 0) {
					currentCappiType = CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED;
				}
				else if (jcomboCappiType.getSelectedIndex() == 1) {
					currentCappiType = CAPPIType.LINEAR_WEIGHTED_AVERAGE;
				}
				else if (jcomboCappiType.getSelectedIndex() == 2) {
					currentCappiType = CAPPIType.NEAREST_SWEEP;
				}
				else if (jcomboCappiType.getSelectedIndex() == 3) {
					currentCappiType = CAPPIType.NEAREST_ELEVATION_ANGLE;
				}
				else if (jcomboCappiType.getSelectedIndex() == 4) {
					currentCappiType = CAPPIType.DIST_TO_SWEEP_CENTER;
				}
			}
    	});
    	
    	
    	panel.add(jcomboAltList, "br");
    	panel.add(jcomboUnitsList);
    	panel.add(jlabelCappiType, "br");
    	panel.add(jcomboCappiType, "br");
    	

    	
    	
    	jcbAllPoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jcbAllPoints.isSelected()) {
					exportAllSweeps = true;
					setComponentsEnabled(elevationPanel, false);
					jcomboAltList.setEnabled(false);
					jcomboUnitsList.setEnabled(false);
				}
				else {
					exportAllSweeps = false;
					setComponentsEnabled(elevationPanel, true);
					jcomboAltList.setEnabled(true);
					jcomboUnitsList.setEnabled(true);
				}
			}
    	});    	
    	
    	if (exportMode && showExportAllSweepsOption) {
    		panel.add(jcbAllPoints, "p");
    		panel.add(jlAllSweepsExportNote, "br");
    	}

    	
    	return panel;
    }
    
    private void setComponentsEnabled(java.awt.Container c, boolean en) {
        Component[] components = c.getComponents();
        for (Component comp: components) {
            if (comp instanceof java.awt.Container)
                setComponentsEnabled((java.awt.Container) comp, en);
            comp.setEnabled(en);
        }
    }

    
    private double processHeight(String altEntry, String unitsEntry) {
    	
    	try {
    	
    		double val = Double.parseDouble(altEntry);
    		if (unitsEntry.equalsIgnoreCase("Feet")) {
    			val *= 0.3048;
    		}
    		else if (unitsEntry.equalsIgnoreCase("KM")) {
    			val *= 1000;
    		}
    		else if (unitsEntry.equalsIgnoreCase("Miles")) {
    			val *= 1609.344;
    		}
    	
    		return val;	
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		return Double.NaN;
    	}
    	
    }
    
    
    private JPanel makeVariablesPanel() {
        List vars = radialDataset.getDataVariables();
        Vector<String> variableNames = new Vector<String>();
        for (int n=0; n<vars.size(); n++) {
            String varName = ((RadialDatasetSweep.RadialVariable)vars.get(n)).getName();
            variableNames.add(varName);        
        }
        jcomboVariables = new JComboBox(variableNames);
        jcomboVariables.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentVariableName = jcomboVariables.getSelectedItem().toString();
                refreshCutButtons();
            }
        });

        JPanel varPanel = new JPanel();
        varPanel.add(jcomboVariables);

        
        this.currentVariableName = jcomboVariables.getSelectedItem().toString();
        return varPanel;

    }


    private void refreshVariables() {       
        
        System.out.println("REFRESHING VARIABLES");
        
        Object selectedVariable = jcomboVariables.getSelectedItem();
        
        System.out.println("SELECTED VARIABLE="+selectedVariable);
        List<VariableSimpleIF> varList = radialDataset.getDataVariables();
        
        ArrayList<String> varNameList = new ArrayList<String>(varList.size());
        for (VariableSimpleIF var : varList) {
            varNameList.add(var.getName());
        }
        
        jcomboVariables.setModel(new DefaultComboBoxModel(varNameList.toArray()));
        
        if (varNameList.contains(selectedVariable)) {
            jcomboVariables.setSelectedItem(selectedVariable);
            System.out.println("SETTING SELECTED VARIABLE="+selectedVariable);
        }
        
        this.currentVariableName = jcomboVariables.getSelectedItem().toString();
    }


    
    
    /**
     * Sets the radial dataset for this GUI.  Will also refresh the list of moments and cuts.
     * @param radialDataset
     */
    public void setRadialDatasetSweep(RadialDatasetSweep radialDataset) {
        this.radialDataset = radialDataset;

        // refresh the list
        refreshVariables();
        refreshCutButtons();
    }



    private JPanel makeCutButtons() {

        List vars = radialDataset.getDataVariables();
        if (vars.size() > 0) {
            return makeCutButtons(((RadialDatasetSweep.RadialVariable)vars.get(0)).getName());
        }
        else {
            JLabel label = new JLabel("No Sweep Variables Found");
            JPanel panel = new JPanel();
            panel.add(label);
            return panel;
        }

    }

    private JPanel makeCutButtons(String varName) {

        RadialDatasetSweep.RadialVariable var = (RadialDatasetSweep.RadialVariable)radialDataset.getDataVariable(varName);

        int numOfCuts = var.getNumSweeps();
        elevations = new double[numOfCuts];

        cutButtons = new JRadioButton[numOfCuts];
        for (int i=0; i<numOfCuts; i++) {

            double elev = var.getSweep(i).getMeanElevation();
            cutButtons[i] = new JRadioButton(WCTUtils.DECFMT_0D00.format(elev));

            System.out.println(varName+" :: "+elev);
            elevations[i] = elev;
            
            
            cutButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleElevationSelection();
                }
            });

        }      

        int numValidButtons = 0;
        for (int i=0; i<cutButtons.length; i++) {
            if (cutButtons[i] != null) {
                numValidButtons++;
            }
        }
        int numAdded = 0;

        JPanel rCutPanel = new JPanel();
        ButtonGroup cutGroup = new ButtonGroup();
        
        if (columnNum == 3) {
            JPanel cutPanel1 = new JPanel();
            JPanel cutPanel2 = new JPanel();
            JPanel cutPanel3 = new JPanel();
            cutPanel1.setLayout(new BoxLayout(cutPanel1, BoxLayout.Y_AXIS));
            cutPanel2.setLayout(new BoxLayout(cutPanel2, BoxLayout.Y_AXIS));
            cutPanel3.setLayout(new BoxLayout(cutPanel3, BoxLayout.Y_AXIS));

            for (int i=0; i<cutButtons.length; i++) {
                cutGroup.add(cutButtons[i]);
                if (cutButtons[i] != null) {
                    if (numAdded < (int)(numValidButtons/3.0 + .5)) {
                        cutPanel1.add(cutButtons[i]);
                    }
                    else if (numAdded < (int)(2*numValidButtons/3.0 + .5)) {
                        cutPanel2.add(cutButtons[i]);
                    }
                    else {
                        cutPanel3.add(cutButtons[i]);
                    }
                    numAdded++;
                }
            }
            rCutPanel.add(cutPanel1);
            rCutPanel.add(cutPanel2);
            rCutPanel.add(cutPanel3);
        }
        else {
            JPanel cutPanel1 = new JPanel();
            JPanel cutPanel2 = new JPanel();
            cutPanel1.setLayout(new BoxLayout(cutPanel1, BoxLayout.Y_AXIS));
            cutPanel2.setLayout(new BoxLayout(cutPanel2, BoxLayout.Y_AXIS));

            for (int i=0; i<cutButtons.length; i++) {
                cutGroup.add(cutButtons[i]);
                if (cutButtons[i] != null) {
                    if (numAdded < (int)(numValidButtons/2.0 + .5)) {
                        cutPanel1.add(cutButtons[i]);
                    }
                    else {
                        cutPanel2.add(cutButtons[i]);
                    }
                    numAdded++;
                }
            }
            rCutPanel.add(cutPanel1);
            rCutPanel.add(cutPanel2);

        }
        
        cutButtons[0].setSelected(true);


        
        this.currentCutIndex = 0;
        
        return rCutPanel;
    }

    
    private void handleElevationSelection() {
        this.currentVariableName = jcomboVariables.getSelectedItem().toString();
        for (int i=0; i<cutButtons.length; i++) {
            if (cutButtons[i] != null) {
                if (cutButtons[i].isSelected()) {
                    this.currentCutIndex = i;
                }
            }         
        }
    }
    
    
    public String getVariableName() {
        return currentVariableName;
    }
    
    public double[] getElevations() {
    	return elevations;
    }
    
    public double getCappiAltitude() {
    	return currentCappiInMeters;
    }

    public CAPPIType getCurrentCappiType() {
		return currentCappiType;
	}


	public int getCut() {
        return currentCutIndex;
    }   
    
    public void setCut(int cutIndex) {
    	this.currentCutIndex = cutIndex;
    	cutButtons[cutIndex].setSelected(true);
    }

    public double getCutElevation() {
        return Double.parseDouble(cutButtons[getCut()].getText());
    }

    
    public boolean isAllSweepsPointExportSelected() {
    	return exportAllSweeps;
    }
    
    

    public void refreshCutButtons() {
        //System.out.println("REFRESHING CUT BUTTONS");

        int selectedIndex = 0;
        for (int n=0; n<cutButtons.length; n++) {
            if (cutButtons[n] != null && cutButtons[n].isSelected()) {
                selectedIndex = n;
            }
        }
        
        try {      
            elevationPanel.removeAll();
        } catch (Exception e) {
        }
        // Create unique cut buttons
        elevationPanel.add( this.makeCutButtons(jcomboVariables.getSelectedItem().toString()) );
        elevationPanel.updateUI();
        
        if (selectedIndex >= cutButtons.length) {
            selectedIndex = cutButtons.length - 1;
        }
        cutButtons[selectedIndex].setSelected(true);
        this.currentCutIndex = selectedIndex;

        
        
		if (Double.isNaN(currentCappiInMeters)) {
			elevationPanel.setEnabled(true);
			setComponentsEnabled(elevationPanel, true);
		}
		else {
			elevationPanel.setEnabled(false);
			setComponentsEnabled(elevationPanel, false);
		}

    }


    
}
