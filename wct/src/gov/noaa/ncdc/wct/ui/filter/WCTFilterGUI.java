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

package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

public class WCTFilterGUI extends JDialog {


    public static final int LEVEL2_TAB = 0;
    public static final int LEVEL3_TAB = 1;
    public static final int GRID_TAB = 2;

    private JTabbedPane attributeTabPane, spatialTabPane;
    private JCheckBox jcbEngage;



    private boolean isSpatialFilterVisible = false;

    private SpatialFilterPanel spatialFilterPanel = new SpatialFilterPanel();
    private RadialAttributeFilterPanel radialAttributeFilterPanel = new RadialAttributeFilterPanel();
    private Level3AttributeFilterPanel level3AttributeFilterPanel = new Level3AttributeFilterPanel();
    private GridAttributeFilterPanel gridAttributeFilterPanel = new GridAttributeFilterPanel();

    private WCTViewer viewer;

    public WCTFilterGUI(String title, WCTViewer viewer, boolean isSpatialFilterVisible) {     
        super(viewer, title, false);
        this.isSpatialFilterVisible = isSpatialFilterVisible;
        this.viewer = viewer;
        createGUI();
    }

    private void createGUI() {
        attributeTabPane = new JTabbedPane();
        spatialTabPane = new JTabbedPane();










        jcbEngage = new JCheckBox("Engage Filter", true);

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(jcbEngage);

        attributeTabPane.addTab("Radial", radialAttributeFilterPanel);
        attributeTabPane.addTab("Level-III NEXRAD", level3AttributeFilterPanel);
        attributeTabPane.addTab("Grid", gridAttributeFilterPanel);

        attributeTabPane.setBorder(WCTUiUtils.myTitledBorder("Attribute Filter", 10));



        spatialTabPane.setBorder(WCTUiUtils.myTitledBorder("Spatial Extent Filter (Deci. Degrees)", 10));
        spatialTabPane.addTab("BBOX / Extent", spatialFilterPanel);



        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(attributeTabPane, BorderLayout.CENTER);
        if (isSpatialFilterVisible) {
            getContentPane().add(spatialTabPane, BorderLayout.EAST);
        }

//        JPanel messagePanel = new JPanel(new RiverLayout());
//        messagePanel.add(new JLabel("Reload data in 'Data Selector' to engage filtering."), "center");
        JButton reloadButton = new JButton("Reload");
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.reloadData();
            }
        });
        JPanel panel = new JPanel();
        panel.add(reloadButton);
        getContentPane().add(panel, BorderLayout.SOUTH);
//        getContentPane().add(messagePanel, BorderLayout.SOUTH);

//      getContentPane().add(attributeTabPane);
//      getContentPane().add(spatialPanel);

        
        
        
        
        JRootPane rootPane = this.getRootPane();
        InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

        ActionMap aMap = rootPane.getActionMap();
        aMap.put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });

    }





    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void setSelectedTab(int type) {
        if (type == LEVEL2_TAB) {
            attributeTabPane.setSelectedComponent(radialAttributeFilterPanel);
        }
        else if (type == LEVEL3_TAB) {
            attributeTabPane.setSelectedComponent(level3AttributeFilterPanel);
        }
        else if (type == GRID_TAB) {
            attributeTabPane.setSelectedComponent(gridAttributeFilterPanel);
        }
        else {
            attributeTabPane.setSelectedComponent(gridAttributeFilterPanel);
        }
    }

    public boolean isFilterEngaged() {
        return jcbEngage.isSelected();
    }

    public void setFilterIsEngaged(boolean engaged) {
        jcbEngage.setSelected(engaged);
    }


















    public WCTFilter getRadialFilter() {
        WCTFilter nxfilter = new WCTFilter();
        return (getRadialFilter(nxfilter));
    }

    public WCTFilter getRadialFilter(WCTFilter nxfilter) {
        if (nxfilter == null) {
            nxfilter = new WCTFilter();
        }
        nxfilter.setDistanceRange(radialAttributeFilterPanel.getRadialMinDistance(), radialAttributeFilterPanel.getRadialMaxDistance());
        nxfilter.setValueRange(radialAttributeFilterPanel.getRadialMinValues(), radialAttributeFilterPanel.getRadialMaxValues());
        nxfilter.setAzimuthRange(radialAttributeFilterPanel.getRadialMinAzimuth(), radialAttributeFilterPanel.getRadialMaxAzimuth());
        nxfilter.setHeightRange(radialAttributeFilterPanel.getRadialMinHeight(), radialAttributeFilterPanel.getRadialMaxHeight());

        if (getSpatialFilterPanel().isSpatialFilterEngaged()) {
            nxfilter.setExtentFilter(getSpatialFilterPanel().getSpatialExtent());
        }
        else {
            nxfilter.setExtentFilter(null);
        }
        return nxfilter;
    }


    public WCTFilter getLevel3Filter() {
        WCTFilter nxfilter = new WCTFilter();
        return (getLevel3Filter(nxfilter));      
    }

    public WCTFilter getLevel3Filter(WCTFilter nxfilter) {
        if (nxfilter == null) {
            nxfilter = new WCTFilter();
        }
        nxfilter.setDistanceRange(level3AttributeFilterPanel.getLevel3MinDistance(), level3AttributeFilterPanel.getLevel3MaxDistance());
        nxfilter.setValueRange(level3AttributeFilterPanel.getLevel3MinValues(), level3AttributeFilterPanel.getLevel3MaxValues());
        nxfilter.setCategoryOverrides(level3AttributeFilterPanel.getCategoryOverrides());
        if (getSpatialFilterPanel().isSpatialFilterEngaged()) {
            nxfilter.setExtentFilter(getSpatialFilterPanel().getSpatialExtent());
        }
        else {
            nxfilter.setExtentFilter(null);
        }
        return nxfilter;
    }

    public WCTFilter getGridFilter() {
        WCTFilter nxfilter = new WCTFilter();
        return (getGridFilter(nxfilter));      
    }

    public WCTFilter getGridFilter(WCTFilter nxfilter) {
        if (nxfilter == null) {
            nxfilter = new WCTFilter();
        }
        nxfilter.setValueRange(gridAttributeFilterPanel.getGridMinValues(), gridAttributeFilterPanel.getGridMaxValues());

        
        
        if (getSpatialFilterPanel().isSpatialFilterEngaged()) {
            nxfilter.setExtentFilter(getSpatialFilterPanel().getSpatialExtent());
        }
        else {
            nxfilter.setExtentFilter(null);
        }

        
        System.out.println("GRID FILTER::::::::::::::: "+nxfilter);

        return nxfilter;
    }



    // input of distance in km -- convert based on value in JComboBox
//    private double convertDistUnits(double inputDistance, int nexradTab) {
//        if (nexradTab == LEVEL2_TAB) {
//            if (((String)l2distUnitsCombo.getSelectedItem()).equals("Kilometers")) {
//                return inputDistance;
//            }
//            else if (((String)l2distUnitsCombo.getSelectedItem()).equals("Miles")) {
//                return inputDistance*1.60924;
//            }
//            else if (((String)l2distUnitsCombo.getSelectedItem()).equals("Nautical Mi.")) {
//                return inputDistance*1.85318;
//            }
//            else {
//                return inputDistance;
//            }
//        }
//        else if (nexradTab == LEVEL3_TAB) {
//            if (((String)l3distUnitsCombo.getSelectedItem()).equals("Kilometers")) {
//                return inputDistance;
//            }
//            else if (((String)l3distUnitsCombo.getSelectedItem()).equals("Miles")) {
//                return inputDistance*1.60924;
//            }
//            else if (((String)l3distUnitsCombo.getSelectedItem()).equals("Nautical Mi.")) {
//                return inputDistance*1.85318;
//            }
//            else {
//                return inputDistance;
//            }
//        }
//        else {
//            return inputDistance;
//        }
//    }
//
//    // input of distance in km -- convert based on value in JComboBox
//    private double convertHeightUnits(double inputHeight) {
//        if (((String)l2heightUnitsCombo.getSelectedItem()).equals("Meters")) {
//            return inputHeight;
//        }
//        else if (((String)l2heightUnitsCombo.getSelectedItem()).equals("Kilometers")) {
//            return inputHeight*1000.0;
//        }
//        else if (((String)l2heightUnitsCombo.getSelectedItem()).equals("Miles")) {
//            return inputHeight*1609.344;
//        }
//        else if (((String)l2heightUnitsCombo.getSelectedItem()).equals("Feet")) {
//            return inputHeight*0.3048;
//        }
//        else {
//            return inputHeight;
//        }
//    }


    public boolean isSpatialFilterVisible() {
        return isSpatialFilterVisible;
    }

    public void setSpatialFilterVisible(boolean isSpatialFilterVisible) {
        this.isSpatialFilterVisible = isSpatialFilterVisible;

        if (isSpatialFilterVisible) {
            if (getContentPane().getComponentCount() == 1) {
                getContentPane().add(spatialTabPane, "Center");
            }
        }
        else {
            if (getContentPane().getComponentCount() == 2) {
                getContentPane().remove(spatialTabPane);
            }
        }
    }


    public SpatialFilterPanel getSpatialFilterPanel() {
        return spatialFilterPanel;
    }

    public RadialAttributeFilterPanel getRadialAttributeFilterPanel() {
        return radialAttributeFilterPanel;
    }
    
    public Level3AttributeFilterPanel getLevel3AttributeFilterPanel() {
        return level3AttributeFilterPanel;
    }
    
    

	public void setFilters(WCTFilter level3Filter, WCTFilter radialFilter,
			WCTFilter gridFilter) {
		
		level3AttributeFilterPanel.setCategoryOverrides(level3Filter.getCategoryOverrides());
		level3AttributeFilterPanel.setLevel3MinValues(level3Filter.getMinValue());
		level3AttributeFilterPanel.setLevel3MaxValues(level3Filter.getMaxValue());

		radialAttributeFilterPanel.setRadialDistanceRange(radialFilter.getMinDistance(), radialFilter.getMaxDistance());
		radialAttributeFilterPanel.setRadialAzimuthRange(radialFilter.getMinAzimuth(), radialFilter.getMaxAzimuth());
		radialAttributeFilterPanel.setRadialMinValues(radialFilter.getMinValue());
		radialAttributeFilterPanel.setRadialMaxValues(radialFilter.getMaxValue());

		
		gridAttributeFilterPanel.setGridMinValues(gridFilter.getMinValue());
		gridAttributeFilterPanel.setGridMaxValues(gridFilter.getMaxValue());
		
	}






}
