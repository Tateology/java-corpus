package steve.test.grass;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.RoundedBorder;
import gov.noaa.ncdc.nexradiv.ClipboardImageTransfer;
import gov.noaa.ncdc.wct.ui.ViewerUtilities;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.border.DropShadowBorder;

public class GEGrassControlPanel extends JPanel {

	private GRASSEarthTest grassEarth = null;
	private boolean autoFlyTo = true;
	private double layerOpacity;
    private JDialog layersDialog;

    private boolean bordersVisible = true;
    private boolean roadsVisible = true;
    private boolean buildingsVisible = false;
    private boolean terrainVisible = true;
    private boolean sunVisible = false;
    private boolean atmosphereVisible = true;
    private boolean scaleVisible = true;
    private boolean overviewVisible = false;
    private boolean gridVisible = false;

    private double elevationExaggeration = 1;

	public GEGrassControlPanel(GRASSEarthTest grassEarth) {
		this.grassEarth = grassEarth;
		init();
	}

    private void init() {
        
        final JCheckBox jcbAutoFlyTo = new JCheckBox("Auto Fly-To?", autoFlyTo);
        jcbAutoFlyTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("event: jcbAutoFlyTo: "+jcbAutoFlyTo.isSelected());
                autoFlyTo = jcbAutoFlyTo.isSelected();
            }
        });
        
//        final JButton refreshButton = new JButton("Refresh");
//        refreshButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
//                refreshView(autoFlyTo);  
//            }
//        });
//        
//        String text = (viewController == ViewController.GEOTOOLS) ? ViewController.GOOGLE_EARTH.toString() : ViewController.GEOTOOLS.toString();
//        final JButton toggleViewButton = new JButton(text);
//        toggleViewButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (toggleViewButton.getText().equals(ViewController.GEOTOOLS.toString())) {
//                    setViewController(ViewController.GEOTOOLS);
//                    viewer.setCurrentExtent(viewer.getMaxExtent());
//                    toggleViewButton.setText(ViewController.GOOGLE_EARTH.toString());
//                }
//                else if (toggleViewButton.getText().equals(ViewController.GOOGLE_EARTH.toString())) {
//                    setViewController(ViewController.GOOGLE_EARTH);
//                    toggleViewButton.setText(ViewController.GEOTOOLS.toString());
//                }
//            }
//        });
//        
        final JSlider transSlider = new JSlider(0, 100, 100);
        transSlider.addChangeListener(new ChangeListener() {
        	int lastValue = 0;
            @Override
            public void stateChanged(ChangeEvent e) {
            	// we divide and multiply to round to the nearest 4th interval in the range
            	if ((transSlider.getValue()/4)*4 != lastValue) {            	
            		// parent.getWebBrowser().executeJavascript("alert(ge.getFeatures().getLastChild().getChildNodes().getLength());");
            		layerOpacity = transSlider.getValue()/100.0;
            		grassEarth.getWebBrowser().executeJavascript("ge.getFeatures().getLastChild().setOpacity("+layerOpacity+");");
            		lastValue = (transSlider.getValue()/4)*4;
            	}
            }
        });
        transSlider.setPreferredSize(new Dimension(100, (int)transSlider.getPreferredSize().getHeight()));

        final JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
						copyViewToClipboard();
						JOptionPane.showMessageDialog(copyButton.getParent(), "-- Image copied to clipboard --");
		            }
		        });
			}        	
        });

        final JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewerUtilities.saveComponentImageChooserWithSWTScreenshot(grassEarth.getWebBrowser());
			}        	
        });
        
        final JButton layersButton = new JButton("GE Options");
        layersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLayersDialog(grassEarth);
            }
        });
        
        final JButton grassOptionsButton = new JButton("GRASS Options");
        grassOptionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GEGrassInitDialog grassInitDialog = new GEGrassInitDialog(grassEarth);
                grassInitDialog.setVisible(true);
                grassInitDialog.configureGRASS(grassEarth.getGrassUtils());
            }
        });
        
        final JButton setRegionFromViewButton = new JButton("Set Region from View");
        setRegionFromViewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				grassEarth.askForBBoxExtent();
//				grassEarth.getGrassUtils().
			}        	
        });
        
        
   

        this.setLayout(new RiverLayout(10, 2));
        DropShadowBorder dropShadowBorder = new DropShadowBorder(Color.BLACK, 4, 0.8f, 5, false, false, true, true);
        Border border2 = BorderFactory.createCompoundBorder(dropShadowBorder, BorderFactory.createEmptyBorder(2, 2, 0, 0));
        //        Border mainBorder = BorderFactory.createCompoundBorder(border2, new LineBorder(new Color(70, 70, 70), 1, true));
        Border mainBorder = BorderFactory.createCompoundBorder(border2, new RoundedBorder(new Color(10, 36, 106, 150), 2, 2));
        this.setBorder(mainBorder);
        
        
        JPanel controlPanel = new JPanel();
        controlPanel.add(jcbAutoFlyTo);
//        controlPanel.add(refreshButton, "left");
        controlPanel.add(new JLabel("Transparency: "));
        
//        JPanel transSliderPanel = new JPanel();
//        transSliderPanel.add(transSlider);
//        transSliderPanel.setBorder(WCTUiUtils.myTitledTopBorder("Transparency:", 0, 0, 0, 0, TitledBorder.CENTER, TitledBorder.ABOVE_TOP));
        
        controlPanel.add(transSlider);
//        controlPanel.add(buttonPanel);
//        controlPanel.add(layersButton);
        
        
        this.add(controlPanel, "left");
        
//        this.add(buttonPanel, "left br");
//        this.add(new JLabel("Elevation Exaggeration: "));
//        this.add(elevExSpinner);
        this.add(new JLabel(""), "hfill");
        
////        controlPanel.add(toggleViewButton, "right");
        this.add(setRegionFromViewButton, "right");
        this.add(copyButton, "right");
        this.add(saveButton, "right");
        this.add(layersButton, "right");
        this.add(grassOptionsButton, "right");
        
        


    }
    
    /**
     * Copy the view image to system clipboard
     */
    public void copyViewToClipboard() {
        ClipboardImageTransfer cit = new ClipboardImageTransfer();
        cit.copyImageToClipboard(ViewerUtilities.getComponentImageWithSWTScreenshot(grassEarth.getWebBrowser()));
    }
    
    public void showLayersDialog(Frame parent) {
        if (layersDialog == null) {
            layersDialog = new JDialog(parent, "Google Earth Layer Selector", false);
            layersDialog.add(createLayerSelectionPanel());
            
            JRootPane rootPane = layersDialog.getRootPane();
            InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

            ActionMap aMap = rootPane.getActionMap();
            aMap.put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    layersDialog.dispose();
                }
            });

        }
        
        
        layersDialog.pack();
        layersDialog.setLocation(parent.getX()+20, parent.getY()+20);
        layersDialog.setVisible(true);
    }
    
    


    public JPanel createLayerSelectionPanel() {
        
        JPanel panel = new JPanel();
        
        // init google earth map layer panel
        final JCheckBox jcbBorders = new JCheckBox("Borders", true);
        final JCheckBox jcbRoads = new JCheckBox("Roads", true);
        final JCheckBox jcbBuildings = new JCheckBox("Buildings", false);
        final JCheckBox jcbTerrain = new JCheckBox("Terrain", true);
        final JCheckBox jcbSun = new JCheckBox("Sun", false);
        final JCheckBox jcbAtmosphere = new JCheckBox("Atmosphere", true);
        final JCheckBox jcbScale = new JCheckBox("Scale", true);
        final JCheckBox jcbOverviewMap = new JCheckBox("Overview Map", false);
        final JCheckBox jcbGrid = new JCheckBox("Lat/Lon Grid", false);
        
        ActionListener geLayerListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == jcbBorders) {
                    setBordersVisible(jcbBorders.isSelected());
                }
                else if (e.getSource() == jcbRoads) {
                    setRoadsVisible(jcbRoads.isSelected());
                }
                else if (e.getSource() == jcbBuildings) {
                    setBuildingsVisible(jcbBuildings.isSelected());
                }
                else if (e.getSource() == jcbTerrain) {
                    setTerrainVisible(jcbTerrain.isSelected());
                }
                else if (e.getSource() == jcbSun) {
                    setSunVisible(jcbSun.isSelected());
                }
                else if (e.getSource() == jcbAtmosphere) {
                    setAtmosphereVisible(jcbAtmosphere.isSelected());
                }
                else if (e.getSource() == jcbScale) {
                    setScaleVisible(jcbScale.isSelected());
                }
                else if (e.getSource() == jcbOverviewMap) {
                    setOverviewVisible(jcbOverviewMap.isSelected());
                }
                else if (e.getSource() == jcbGrid) {
                    setGridVisible(jcbGrid.isSelected());
                }
            }
        };

        jcbBorders.addActionListener(geLayerListener);
        jcbRoads.addActionListener(geLayerListener);
        jcbBuildings.addActionListener(geLayerListener);
        jcbTerrain.addActionListener(geLayerListener);
        jcbSun.addActionListener(geLayerListener);
        jcbAtmosphere.addActionListener(geLayerListener);
        jcbScale.addActionListener(geLayerListener);
        jcbOverviewMap.addActionListener(geLayerListener);
        jcbGrid.addActionListener(geLayerListener);
        
        panel.setLayout(new RiverLayout());
        panel.add(jcbBorders, "br");
        panel.add(jcbRoads, "br");
        panel.add(jcbBuildings, "br");
        panel.add(jcbTerrain, "br");
        panel.add(jcbSun, "br");
        panel.add(jcbAtmosphere, "br");
        panel.add(jcbScale, "br");
        panel.add(jcbOverviewMap, "br");
        panel.add(jcbGrid, "br");
        
        panel.setBorder(WCTUiUtils.myTitledBorder("Layers", 5));
        
        JPanel buttonPanel = new JPanel(new RiverLayout(10, 5));
        
        
        final JSpinner elevExSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 0.25));
        elevExSpinner.addChangeListener(new ChangeListener() {
        	
            private Timer timer = new Timer();
            private TimerTask task;
            private int waitTime = 1000;
        	
			@Override
			public void stateChanged(ChangeEvent evt) {
//				setElevationExaggeration(Double.parseDouble(((JSpinner)evt.getSource()).getValue().toString()));
//				refreshView(false);
				
		        if (task != null) {
		            try {
		                task.cancel();
		            } catch (Exception e) {
		            }
		        }
		        startTimer();
			}
			
			
			
		    private synchronized void startTimer() {
		        
		        task = new TimerTask() {
		            public void run() {
//		            	System.out.println("DOING SPINNER EVENT");
						setElevationExaggeration(Double.parseDouble(elevExSpinner.getValue().toString()));
//						refreshView(false);
	            }
		        };

		        timer.purge();
		        try {
		            timer.schedule(task , waitTime);
		        } catch (Exception e) {
		            System.err.println(e.getMessage());
		            System.err.println("Creating new zoom timer object...");
		            timer = new Timer();
		            timer.schedule(task , waitTime);
		        }
		    }

        });
        elevExSpinner.setPreferredSize(new Dimension(75, (int)elevExSpinner.getPreferredSize().getHeight()));
        
        
        
        buttonPanel.add(new JLabel("Elevation Exaggeration: "), "p");
        buttonPanel.add(elevExSpinner, "br");
        buttonPanel.setBorder(WCTUiUtils.myTitledBorder("Data View", 5));
        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panel, BorderLayout.WEST);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        return mainPanel;
    }


    public void setBordersVisible(boolean bordersVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, "+bordersVisible+");");
        this.bordersVisible = bordersVisible;
    }


    public boolean isBordersVisible() {
        return bordersVisible;
    }


    public void setRoadsVisible(boolean roadsVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, "+roadsVisible+");");
        this.roadsVisible = roadsVisible;
    }


    public boolean isRoadsVisible() {
        return roadsVisible;
    }


    public void setBuildingsVisible(boolean buildingsVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_BUILDINGS, "+buildingsVisible+");");
        this.buildingsVisible = buildingsVisible;
    }


    public boolean isBuildingsVisible() {
        return buildingsVisible;
    }


    public void setTerrainVisible(boolean terrainVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_TERRAIN, "+terrainVisible+");");
        this.terrainVisible = terrainVisible;
    }


    public boolean isTerrainVisible() {
        return terrainVisible;
    }


    public void setSunVisible(boolean sunVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getSun().setVisibility("+sunVisible+");");
        this.sunVisible = sunVisible;
    }


    public boolean isSunVisible() {
        return sunVisible;
    }


    public void setAtmosphereVisible(boolean atmosphereVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getOptions().setAtmosphereVisibility("+atmosphereVisible+");");
        this.atmosphereVisible = atmosphereVisible;
    }


    public boolean isAtmosphereVisible() {
        return atmosphereVisible;
    }


    public void setScaleVisible(boolean scaleVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getOptions().setScaleLegendVisibility("+scaleVisible+");");
        this.scaleVisible = scaleVisible;
    }


    public boolean isScaleVisible() {
        return scaleVisible;
    }


    public void setOverviewVisible(boolean overviewVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getOptions().setOverviewMapVisibility("+overviewVisible+");");
        this.overviewVisible = overviewVisible;
    }


    public boolean isOverviewVisible() {
        return overviewVisible;
    }


    public void setGridVisible(boolean gridVisible) {
        grassEarth.getWebBrowser().executeJavascript("ge.getOptions().setGridVisibility("+gridVisible+");");
        this.gridVisible = gridVisible;
    }


    public boolean isGridVisible() {
        return gridVisible;
    }
    
	/**
	 * Sets the elevationExaggeration - this causes both the 'z' scale factor
	 * in the model kml and the javascript terrain exaggeration to be set.
	 * (ge.getOptions.setTerrainExaggeration(value))
	 * @param elevationExaggeration
	 */
    public void setElevationExaggeration(final double elevationExaggeration) {
		this.elevationExaggeration = elevationExaggeration;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                grassEarth.getWebBrowser().executeJavascript("ge.getOptions().setTerrainExaggeration("+elevationExaggeration+");");
//                parent.refreshView(false);
            }
        });

	}
	public double getElevationExaggeration() {
		return elevationExaggeration;
	}

}
