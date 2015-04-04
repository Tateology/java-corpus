///**
// * NOAA's National Climatic Data Center
// * NOAA/NESDIS/NCDC
// * 151 Patton Ave, Asheville, NC  28801
// * 
// * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
// * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
// * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
// * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
// * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
// * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
// * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
// * TECHNICAL SUPPORT TO USERS.
// */
//
//package gov.noaa.ncdc.wct.export;
//
//import gov.noaa.ncdc.wct.ui.WCTUiInterface;
//import gov.noaa.ncdc.wct.ui.WCTUiUtils;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.net.URL;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JScrollPane;
//import javax.swing.JSpinner;
//import javax.swing.JTabbedPane;
//import javax.swing.JTextField;
//import javax.swing.SpinnerNumberModel;
//
//import ucar.nc2.VariableSimpleIF;
//import ucar.nc2.dt.RadialDatasetSweep;
//
//public class RadialExportProperties extends JPanel implements ActionListener {
//
//    
//    private RadialDatasetSweep radialDataset;
//
//    
//    private WCTUiInterface jnx;
//
//    private URL nexradURL;
//
//    private JTabbedPane tabPane = new JTabbedPane();
//
//    private JComboBox jcomboVariables;
//    private JRadioButton[] cutButtons;
//    private JComboBox jcomboMinDistance;
//    private JCheckBox jcbUseRF, jcbClassify, jcbExportPoints, jcbExportAllPoints;
//    private JPanel sweepPanel = new JPanel();
//    private JPanel cutSubPanel = new JPanel();
//    private JPanel cutPanel = new JPanel();
//
//
//    private JPanel cubePanel;
//    private JComboBox jcomboDataCubeNumHeights, jcomboDataCubeBottomHeight, jcomboDataCubeTopHeight, jcomboDataCubeOverlap;
//    private JCheckBox jcbEngageCube;
//
//    private JComboBox jcomboCubeGridSize = new JComboBox(new Object[]{"400", "600", "800", "1000", "1200", "1400", "1600"});
//    private JTextField jtfCubeNoData = new JTextField();
//    private JSpinner jcomboCubeSmoothFactor = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
//
//
//
//    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");
//    private final static NumberFormat numberFmt = NumberFormat.getNumberInstance();
//
//    private String lastDecodedVariableName;
//    private int lastDecodedCutIndex;
//    
//    
//    public RadialExportProperties(WCTUiInterface jnx, RadialDatasetSweep radialDataset) {      
////        super(parent, "Radial Properties", false);
//        this.jnx = jnx;
//        this.radialDataset = radialDataset;
//        createGUI();
//        // pack();
//    }
//
//    private void createGUI() {
//
//        JPanel momentPanel = new JPanel();
//        sweepPanel.setLayout(new BoxLayout(sweepPanel, BoxLayout.Y_AXIS));
//        momentPanel.setLayout(new BorderLayout());
//        cutPanel.setLayout(new BorderLayout());
//        momentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//        JPanel rfPanel = new JPanel();
//        rfPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//        rfPanel.setLayout(new BoxLayout(rfPanel, BoxLayout.Y_AXIS));
//
//
//        jcbUseRF = new JCheckBox("Show RF Values", false);
//        jcbUseRF.setEnabled(false);
//        
//        jcbClassify = new JCheckBox("Classify Data", false);
//        jcbExportPoints = new JCheckBox("Point Centroid Export", false);
//        
//        jcbExportAllPoints = new JCheckBox("All Cuts Point Export", false);
//        jcbExportAllPoints.setForeground(Color.blue);
//        jcbExportAllPoints.addActionListener(this);
//
//        jcomboMinDistance = new JComboBox(new Object[] { "0", "10", "20", "30",
//                "40", "50" });
//        jcomboMinDistance.setEditable(true);
//        JPanel minDistPanel = new JPanel();
//        minDistPanel.add(new JLabel("Min. Dist. (KM)"));
//        minDistPanel.add(jcomboMinDistance);
//        rfPanel.add(jcbExportPoints);
////        rfPanel.add(jcbUseRF);
//        rfPanel.add(jcbClassify);
//        rfPanel.add(jcbExportAllPoints);
//        // rfPanel.add(minDistPanel);
//
//        // Create unique cut buttons
//        cutSubPanel = this.makeCutButtons();
//        JPanel momentSubPanel = this.makeVariablesPanel();
//
//        //momentPanel.add(new JLabel("Moment:", JLabel.LEFT), "North");
//        momentPanel.setBorder(WCTUiUtils.myTitledBorder("Moment", 10));
//        momentPanel.add(momentSubPanel, "Center");
//        //cutPanel.add(new JLabel("Cut (Elev. Angle):", JLabel.LEFT), "North");
//        cutSubPanel.setBorder(WCTUiUtils.myTitledBorder("Cut (Elev. Angle)", 10));
//        cutPanel.add(cutSubPanel, "West");
//        rfPanel.setBorder(WCTUiUtils.myTitledBorder("Options", 10));
//        cutPanel.add(rfPanel, "Center");
//
//        sweepPanel.add(momentPanel);
//        sweepPanel.add(cutPanel);
//
//        tabPane.add(new JScrollPane(sweepPanel), "Sweep");
//
//
//
//
//
//
//        jcomboDataCubeNumHeights = new JComboBox(new Object[] { "1", "4", "6", "8", "10" });
//        jcomboDataCubeNumHeights.setEditable(true);
//        jcomboDataCubeNumHeights.setEnabled(false);
//        jcomboDataCubeBottomHeight = new JComboBox(new Object[] { "0", "1000", "2000", "4000", "8000" });
//        jcomboDataCubeBottomHeight.setEditable(true);
//        jcomboDataCubeBottomHeight.setEnabled(false);
//        jcomboDataCubeTopHeight = new JComboBox(new Object[] { "1000", "2000", "4000", "8000", "10000" });
//        jcomboDataCubeTopHeight.setEditable(true);
//        jcomboDataCubeTopHeight.setEnabled(false);
//        jcomboDataCubeOverlap = new JComboBox(new Object[] { "100", "200", "400", "800", "1000" });
//        jcomboDataCubeOverlap.setEditable(true);
//        jcomboDataCubeOverlap.setEnabled(false);
//
//        jcbEngageCube = new JCheckBox();
//        jcbEngageCube.addActionListener(this);
//        JPanel panel0 = new JPanel();
//        panel0.setLayout(new BoxLayout(panel0, BoxLayout.Y_AXIS));
//        JPanel panel1 = new JPanel();
//        panel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        panel1.setLayout(new GridLayout(1, 2));
//        panel1.add(new JLabel("Engage Cube Interpolation:"), JLabel.CENTER);
//        jcbEngageCube.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//        panel1.add(jcbEngageCube);
//        JPanel panel2 = new JPanel();
//        panel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        panel2.setLayout(new GridLayout(1, 2));
//        panel2.add(new JLabel("Num Height Layers:"), JLabel.CENTER);
//        panel2.add(jcomboDataCubeNumHeights);
//        JPanel panel3 = new JPanel();
//        panel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        panel3.setLayout(new GridLayout(1, 2));
//        panel3.add(new JLabel("Bottom Height (m):"), JLabel.CENTER);
//        panel3.add(jcomboDataCubeBottomHeight);
//        JPanel panel4 = new JPanel();
//        panel4.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        panel4.setLayout(new GridLayout(1, 2));
//        panel4.add(new JLabel("Top Height (m):"), JLabel.CENTER);
//        panel4.add(jcomboDataCubeTopHeight);
//        JPanel panel5 = new JPanel();
//        panel5.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        panel5.setLayout(new GridLayout(1, 2));
//        panel5.add(new JLabel("Layer Overlap (m):"), JLabel.CENTER);
//        panel5.add(jcomboDataCubeOverlap);
//
//        //panel0.add(new JLabel("Only NetCDF and VTK Formats Supported", JLabel.CENTER));
//        panel0.add(panel1);
//        panel0.add(panel2);
//        panel0.add(panel3);
//        panel0.add(panel4);
//        panel0.add(panel5);
//
//        JPanel cubeOptionsPanel = new JPanel();
//        cubeOptionsPanel.setLayout(new BoxLayout(cubeOptionsPanel, BoxLayout.Y_AXIS));
//        //mainPanel.add(new JLabel("Data Cube Functions", JLabel.CENTER));
//        cubeOptionsPanel.setBorder(WCTUiUtils.myTitledBorder("Data Cube Parameters", 10));
//        cubeOptionsPanel.add(panel0);
//        //cubeMainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
//
////      JPanel rasterPropPanel = new JPanel();
////      rasterPropPanel.
//
////      ((NexradExportGUI)jnx).getRasterProperties().getS
//
//
//        cubePanel = new JPanel();
////      cubePanel.setLayout(new BorderLayout());
//        cubePanel.setLayout(new BoxLayout(cubePanel, BoxLayout.Y_AXIS));
//
//
////      cubePanel.add(cubeOptionsPanel, BorderLayout.NORTH);
//        cubePanel.add(cubeOptionsPanel);
//
//
//
//
//
//        WCTExport exporter = ((WCTExportGUI)jnx).getNexradExport();
//        // set up initial values
//        jcomboCubeGridSize.setSelectedItem("600");
//        jcomboCubeGridSize.setEditable(true);
//        jtfCubeNoData.setText(fmt2.format(exporter.getExportGridNoData()));
//        jcomboCubeSmoothFactor.setValue(new Integer(exporter.getExportGridSmoothFactor()));
//
//
//        JPanel rpanel0 = new JPanel();
//        JPanel rpanel1 = new JPanel();
////        JPanel rpanel2 = new JPanel();
//        JPanel rpanel3 = new JPanel();
//        JPanel rpanel4 = new JPanel();
//        rpanel1.setLayout(new GridLayout(1, 2));
//        rpanel1.add(new JLabel("Square Grid Size: "));
//        rpanel1.add(jcomboCubeGridSize);
//        rpanel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
////        rpanel2.setLayout(new GridLayout(1, 2));
////        rpanel2.add(new JLabel("Variable Resolution: "));
////        rpanel2.add(jcbCubeVariableRes);
////        rpanel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        rpanel3.setLayout(new GridLayout(1, 2));
//        rpanel3.add(new JLabel("No Data Value: "));
//        rpanel3.add(jtfCubeNoData);
//        rpanel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        rpanel4.setLayout(new GridLayout(1, 2));
//        rpanel4.add(new JLabel("Smoothing Factor: "));
//        rpanel4.add(jcomboCubeSmoothFactor);                  
//        rpanel4.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//        rpanel0.setLayout(new BoxLayout(rpanel0, BoxLayout.Y_AXIS));
//        rpanel0.add(rpanel1);
////        rpanel0.add(rpanel2);
//        rpanel0.add(rpanel3);
//        rpanel0.add(rpanel4);
//
//
//        JPanel cubeRasterPanel = new JPanel();
//        cubeRasterPanel.setLayout(new BoxLayout(cubeRasterPanel, BoxLayout.Y_AXIS));
//        cubeRasterPanel.setBorder(WCTUiUtils.myTitledBorder("Data Cube Grid", 10));
//        cubeRasterPanel.add(rpanel0);
//
////      cubePanel.add(cubeRasterPanel, BorderLayout.SOUTH);
//        cubePanel.add(cubeRasterPanel);
//
//
////        tabPane.add(cubePanel, "Cube");
//
//
//
//        this.setLayout(new BorderLayout());
//
//        JPanel singlePanel = new JPanel(); // this keeps panel from stretching
//        singlePanel.add(tabPane);
//
//        this.add(singlePanel, "Center");
//
//    }
//
//    
//    
//    
//    private JPanel makeVariablesPanel() {
//        List vars = radialDataset.getDataVariables();
//        Vector<String> variableNames = new Vector<String>();
//        for (int n=0; n<vars.size(); n++) {
//            String varName = ((RadialDatasetSweep.RadialVariable)vars.get(n)).getName();
//            variableNames.add(varName);         
//        }
//        jcomboVariables = new JComboBox(variableNames);
//        jcomboVariables.addActionListener(this);
//        
//        JPanel varPanel = new JPanel();
//        varPanel.add(jcomboVariables);
//        
//        return varPanel;
//           
//    }
//
//
//    private void refreshVariables() {       
//        lastDecodedVariableName = jcomboVariables.getSelectedItem().toString();
//        
//        jcomboVariables.setModel(new DefaultComboBoxModel(radialDataset.getDataVariables().toArray()));
//        List<VariableSimpleIF> varList = radialDataset.getDataVariables();
//        List<String> varNameList = new ArrayList<String>();
//        for (VariableSimpleIF var : varList) {
//            varNameList.add(var.getName());
//        }
//        
//        if (lastDecodedVariableName != null && varNameList.contains(lastDecodedVariableName)) {
//            jcomboVariables.setSelectedIndex(varNameList.indexOf(lastDecodedVariableName));
//        }
//    }
//    
//
//
//    /**
//     * Sets the radial dataset for this GUI.  Will also refresh the list of moments and cuts.
//     * @param radialDataset
//     */
//    public void setRadialDatasetSweep(RadialDatasetSweep radialDataset) {
//        this.radialDataset = radialDataset;
//
//        // refresh the list
//        refreshVariables();
//        refreshCutButtons();
//    }
//
//
//
//    private JPanel makeCutButtons() {
//
//        List vars = radialDataset.getDataVariables();
//        if (vars.size() > 0) {
//            return makeCutButtons(((RadialDatasetSweep.RadialVariable)vars.get(0)).getName());
//        }
//        else {
//            JLabel label = new JLabel("No Sweep Variables Found");
//            JPanel panel = new JPanel();
//            panel.add(label);
//            return panel;
//        }
//
//    }
//
//    private JPanel makeCutButtons(String varName) {
//
//        RadialDatasetSweep.RadialVariable var = (RadialDatasetSweep.RadialVariable)radialDataset.getDataVariable(varName);
//
//
//
//        int recordNum;
//        int numOfCuts = var.getNumSweeps();
//
// 
//        cutButtons = new JRadioButton[numOfCuts];
//        for (int i=0; i<numOfCuts; i++) {
//
//            double elev = var.getSweep(i).getMeanElevation();
//            cutButtons[i] = new JRadioButton(fmt2.format(elev));
//
//            System.out.println(varName+" :: "+elev);
//
//        }      
//
//        JPanel rCutPanel = new JPanel();
//        JPanel cutPanel1 = new JPanel();
//        JPanel cutPanel2 = new JPanel();
//        cutPanel1.setLayout(new BoxLayout(cutPanel1, BoxLayout.Y_AXIS));
//        cutPanel2.setLayout(new BoxLayout(cutPanel2, BoxLayout.Y_AXIS));
//
//        int numValidButtons = 0;
//        for (int i=0; i<cutButtons.length; i++) {
//            if (cutButtons[i] != null) {
//                numValidButtons++;
//            }
//        }
//
//
//        int numAdded = 0;
//        ButtonGroup cutGroup = new ButtonGroup();
//        for (int i=0; i<cutButtons.length; i++) {
//            cutGroup.add(cutButtons[i]);
//            if (cutButtons[i] != null) {
//                if (numAdded < (int)(numValidButtons/2.0 + .5)) {
//                    cutPanel1.add(cutButtons[i]);
//                }
//                else {
//                    cutPanel2.add(cutButtons[i]);
//                }
//                numAdded++;
//            }
//        }
//
//        cutButtons[0].setSelected(true);
//
//        rCutPanel.add(cutPanel1);
//        rCutPanel.add(cutPanel2);
//
//        return rCutPanel;
//    }
//
//    public void refreshCutButtons() {
//        //System.out.println("REFRESHING CUT BUTTONS");
//
//        if (cutButtons != null && cutButtons.length > 0) {
//            for (int n=0; n<cutButtons.length; n++) {
//                if (cutButtons[n].isSelected()) {
//                    lastDecodedCutIndex = n;
//                }
//            }
//        }
//         
//        
//        try {      
////            cutPanel.remove(1);
//            cutPanel.remove(cutSubPanel);
//        } catch (Exception e) {
//        }
//        // Create unique cut buttons
//        cutSubPanel = this.makeCutButtons(jcomboVariables.getSelectedItem().toString());
////        cutPanel.add(cutSubPanel, 1);
//        
//        cutSubPanel.setBorder(WCTUiUtils.myTitledBorder("Cut (Elev. Angle)", 10));
//        cutPanel.add(cutSubPanel, "West");
//        cutPanel.updateUI();
//
//        // Disable buttons if "All Cuts Point Export" is selected
//        if (jcbExportAllPoints.isSelected()) {
//            for (int n = 0; n < cutButtons.length; n++) {
//                if (cutButtons[n] != null) {
//                    cutButtons[n].setEnabled(false);
//                }
//            }
//        }
//        
//        
//
//        cutButtons[lastDecodedCutIndex].setSelected(true);
//
//
//    }
//
//    
//    public String getVariableName() {
//        return jcomboVariables.getSelectedItem().toString();
//    }
//
//    public void setVariableName(String variableName) {
//        jcomboVariables.setSelectedItem(variableName);
//    }
//
//    public int getCut() {
//        for (int i=0; i<cutButtons.length; i++) {
//            if (cutButtons[i] != null) {
//                if (cutButtons[i].isSelected()) {
//                    return i;
//                }
//            }         
//        }
//        return 0;
//    }   
//
//    public double getCutElevation() {
//        return Double.parseDouble(cutButtons[getCut()].getText());
//    }
//
//
//    public JCheckBox getExportPoints() {
//        return jcbExportPoints;
//    }
//
//    public JCheckBox getExportAllPoints() {
//        return jcbExportAllPoints;
//    }
//
//    public boolean getUseRFvalues() {
//        return jcbUseRF.isSelected();
//    }
//
//    public boolean getClassify() {
//        return jcbClassify.isSelected();
//    }
//
//    public double getMinimumDistance() {
//        try {
////            return Double.parseDouble(jcomboMinDistance.getSelectedItem()
////                    .toString());
//            return numberFmt.parse(jcomboMinDistance.getSelectedItem().toString()).doubleValue();
//        } catch (Exception e) {
//            return 0.0;
//        }
//    }
//
//    public boolean isDataCubeEngaged() {
//        return jcbEngageCube.isSelected();
//    }
//
//    public int getDataCubeNumHeightLayers() throws ParseException {
//        //return Integer.parseInt(jcomboDataCubeNumHeights.getSelectedItem().toString());
//        return numberFmt.parse(jcomboDataCubeNumHeights.getSelectedItem().toString()).intValue();
//    }
//
//    public int getDataCubeBottomHeight() throws ParseException {
//        //return Integer.parseInt(jcomboDataCubeBottomHeight.getSelectedItem().toString());
//        return numberFmt.parse(jcomboDataCubeBottomHeight.getSelectedItem().toString()).intValue();
//    }
//
//    public int getDataCubeTopHeight() throws ParseException {
//        //return Integer.parseInt(jcomboDataCubeTopHeight.getSelectedItem().toString());
//        return numberFmt.parse(jcomboDataCubeTopHeight.getSelectedItem().toString()).intValue();
//    }
//
//    public int getDataCubeOverlap() throws ParseException {
//        //return Integer.parseInt(jcomboDataCubeOverlap.getSelectedItem().toString());
//        return numberFmt.parse(jcomboDataCubeOverlap.getSelectedItem().toString()).intValue();
//    }
//
//    public int getDataCubeGridSize() throws ParseException {
//        //return Integer.parseInt(jcomboCubeGridSize.getSelectedItem().toString());
//        return numberFmt.parse(jcomboCubeGridSize.getSelectedItem().toString()).intValue();
//    }
//
//
//    public double getDataCubeNoData() throws ParseException {
//        //return Double.parseDouble(jtfCubeNoData.getText());
//        return numberFmt.parse(jtfCubeNoData.getText()).doubleValue();
//    }
//
//    public int getDataCubeSmoothFactor() throws ParseException {
//        //return Integer.parseInt(jcomboCubeSmoothFactor.getValue().toString());
//        return numberFmt.parse(jcomboCubeSmoothFactor.getValue().toString()).intValue();
//    }
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
//    public void actionPerformed(ActionEvent evt) {
//        Object source = evt.getSource();
//        if (source == jcomboVariables) {
//            refreshCutButtons();
//        }
//        else if (source == jcbExportAllPoints) {
//
//            jcbClassify.setEnabled(!jcbExportAllPoints.isSelected());
//            jcbExportPoints.setEnabled(!jcbExportAllPoints.isSelected());
//            for (int n = 0; n < cutButtons.length; n++) {
//                if (cutButtons[n] != null) {
//                    cutButtons[n].setEnabled(!jcbExportAllPoints.isSelected());
//                }
//            }
//        }
//        else if (source == jcbEngageCube) {
//
//            jcomboDataCubeNumHeights.setEnabled(jcbEngageCube.isSelected());
//            jcomboDataCubeBottomHeight.setEnabled(jcbEngageCube.isSelected());
//            jcomboDataCubeTopHeight.setEnabled(jcbEngageCube.isSelected());
//            jcomboDataCubeOverlap.setEnabled(jcbEngageCube.isSelected());
//
//        }
//
//
//    }
//
//
//}
