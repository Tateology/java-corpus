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

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.ColorTableEditorUI;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentViewType;
import gov.noaa.ncdc.wms.WMSPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.StackedBox;
import org.jdesktop.swingx.StackedBox.BoxEvent;
import org.jdesktop.swingx.StackedBox.BoxListener;
import org.xml.sax.SAXException;

import ucar.units.Unit;
import ucar.units.UnitDB;
import ucar.units.UnitDBException;
import ucar.units.UnitDBManager;


public class MapSelector extends JDialog implements ActionListener {

    public static enum DataType { RADAR, SATELLITE, GRIDDED };
    private DataType lastIsolatedType = null;
    

    
    private int numLayers = WCTViewer.NUM_LAYERS;
    private WCTViewer viewer;
    private WMSPanel wmsPanel;
    private JTabbedPane tabPane;
    private JPanel jnxPanel, mainPanel;
    private JPanel dataPanel = new JPanel();
    private JPanel overlayPanel = new JPanel();
    private JPanel gePanel = new JPanel();
    private Vector layersPanel = new Vector();
    private Vector attPanel = new Vector();
    private Vector jbColor = new Vector();
    private Vector<JCheckBox> jcbVisible = new Vector<JCheckBox>();
    private Vector<JCheckBox> jcbLabel = new Vector<JCheckBox>();
    private Vector styleOptions = new Vector();
    private JButton jbLoadShapefile, jbBackground;
    private JCheckBox jcbDataLayersOnTop;
    private File lastFolder;
    private int localThemeCounter = 0;

    private boolean gridHasValidRangeAttributes = true;
    

    // Background color bogus panels
    private JLabel backgroundLabel;
    private JCheckBox jcbBogus;
    private JPanel apanel = new JPanel();
    private JPanel bpanel = new JPanel();


    private JCheckBox jcbRadar, jcbGridSatellite;
    private final JComboBox radTransparency = new JComboBox(new Object[] {
            "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
            " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
    });      
    private final JComboBox gridSatTransparency = new JComboBox(new Object[] {
            " Default" , "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
            " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
    });      
    private final JComboBox radLegend = new JComboBox(new Object[] {
            "None", "Large"
    });      
    private final JComboBox gridSatLegend = new JComboBox(new Object[] {
            "None", "Medium"
    });   
    private final JComboBox<String> gridSatUnits = new JComboBox<String>(new String[] {
    		"Native (no conversion)"
    });

    private final JComboBox satColorTableCombo = new JComboBox(new String[] {
            "Default", "Auto Grayscale Flat", "Auto Grayscale - Black/Trans to White/Opaque", 
            "Auto Grayscale - Gray/Trans to White/Opaque",
            "McIDAS_TSTORM1.ET", "McIDAS_TSTORM2.ET", "Water Vapor 1 (McIDAS_VAPOR1.ET)", "Water Vapor 2 (McIDAS_WVRBT3.ET)",
            "McIDAS_TEMPS1.ET", "McIDAS_SSTS.ET", "McIDAS_CA.ET",
            "McIDAS_BB.ET", "McIDAS_BD.ET"
    });

    private final JComboBox gridColorTableCombo = new JComboBox(new String[] {
            "Rainbow", "Grayscale", 
            "Blue-White-Red", "Blue-Black-Red", "Blue-Red", "Blue-Green-Red", "Brown-White-Green", 
            "Yellow-Blue", "Yellow-Blue-Purple", "Yellow-Purple",
            "White-Green", "Black-Green",
            "White-Blue", "Black-Blue", "White-Red", "Black-Red",
            "Radar: Reflectivity", "Radar: Diff. Reflectivity",
            "Satellite: Water Vapor (Default)", "Satellite: Infrared Window (Default)",
            "Satellite: McIDAS_TSTORM1.ET", "Satellite: McIDAS_TSTORM2.ET", 
            "Satellite: Water Vapor 1 (McIDAS_VAPOR1.ET)", "Satellite: Water Vapor 2 (McIDAS_WVRBT3.ET)",
            "Satellite: McIDAS_TEMPS1.ET", "Satellite: McIDAS_SSTS.ET", "Satellite: McIDAS_CA.ET",
            "Satellite: McIDAS_BB.ET", "Satellite: McIDAS_BD.ET"
    });
    
    private final JComboBox radColorTableCombo = new JComboBox(new String[] {
            "Default"
    });
    private final JXHyperlink radEditColorTableLink = new JXHyperlink();
            

//    private final JRadioButton jrbGridValidRangeMinMax = new JRadioButton("Valid Range", true);
//    private final JRadioButton jrbGridAutoMinMax = new JRadioButton("Auto", false);
//    private final JRadioButton jrbGridCustomMinMax = new JRadioButton("Custom", false);
    private final JComboBox jcomboGridMinMax = 
    		new JComboBox(new String[] {
    			"Auto", "Valid Range", "Custom"	
    		});
    
    
    
    private final JCheckBox jcbFlipGridColorTable = new JCheckBox("Flip?");
    private final JTextField jtfGridMinValue = new JTextField(10);
    private final JTextField jtfGridMaxValue = new JTextField(10);

    private final JSpinner radSmoothing = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    private final JSpinner gridSatSmoothing = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    private Timer timer = new Timer();

    private JPanel gridSatellitePanel = new JPanel();
    private JPanel gridColorTablePanel = new JPanel();
    private JPanel satColorTablePanel = new JPanel();

    private final StackedBox dataLayersStack = new StackedBox();
    

    public MapSelector(WCTViewer viewer) throws ParserConfigurationException, SAXException, IOException, Exception {
        super(viewer, "Layer Selector", false);
        this.viewer = viewer;
        sizeVectors(numLayers);
        createGUI();
        pack();
//        setSize(getSize().width, getSize().height+20);
        setSize(525, getSize().height+20);
    }

    // Initialize the vectors!
    private void sizeVectors(int num) {
        layersPanel.setSize(num);
        attPanel.setSize(num);
        jbColor.setSize(num);
        jcbVisible.setSize(num);
        jcbLabel.setSize(num);
        styleOptions.setSize(num);
    }

    private void createGUI() throws ParserConfigurationException, SAXException, IOException, Exception {

        // Create Panel for each Map Layer

        makeMapPanel(WCTViewer.COUNTRIES, "Countries", viewer.isLayerVisible(WCTViewer.COUNTRIES));
        makeMapPanel(WCTViewer.COUNTRIES_OUT, "Country Borders", viewer.isLayerVisible(WCTViewer.COUNTRIES_OUT));   
        makeMapPanel(WCTViewer.STATES, "States", viewer.isLayerVisible(WCTViewer.STATES));
        makeMapPanel(WCTViewer.STATES_OUT, "State Borders", viewer.isLayerVisible(WCTViewer.STATES_OUT));
        makeMapPanel(WCTViewer.COUNTIES, "Counties", viewer.isLayerVisible(WCTViewer.COUNTIES));
        makeMapPanel(WCTViewer.HWY_INT, "Major Highways", viewer.isLayerVisible(WCTViewer.HWY_INT));
        makeMapPanel(WCTViewer.RIVERS, "Rivers", viewer.isLayerVisible(WCTViewer.RIVERS));
        makeMapPanel(WCTViewer.WSR, "NEXRAD Sites", viewer.isLayerVisible(WCTViewer.WSR));
        makeMapPanel(WCTViewer.TDWR, "TDWR Sites", viewer.isLayerVisible(WCTViewer.TDWR));
        makeMapPanel(WCTViewer.CITY_SMALL, "Cities (< 10k)", viewer.isLayerVisible(WCTViewer.CITY_SMALL));
        makeMapPanel(WCTViewer.CITY10, "Cities (10k - 35k)", viewer.isLayerVisible(WCTViewer.CITY10));
        makeMapPanel(WCTViewer.CITY35, "Cities (35k - 100k)", viewer.isLayerVisible(WCTViewer.CITY35));
        makeMapPanel(WCTViewer.CITY100, "Cities (100k - 250k)", viewer.isLayerVisible(WCTViewer.CITY100));
        makeMapPanel(WCTViewer.CITY250, "Cities (Pop > 250k)", viewer.isLayerVisible(WCTViewer.CITY250));
        makeMapPanel(WCTViewer.AIRPORTS, "Airports", viewer.isLayerVisible(WCTViewer.AIRPORTS));
        makeMapPanel(WCTViewer.ASOS_AWOS, "ASOS/AWOS", viewer.isLayerVisible(WCTViewer.ASOS_AWOS));
        makeMapPanel(WCTViewer.CLIMATE_DIV, "Climate Divisions", viewer.isLayerVisible(WCTViewer.CLIMATE_DIV));
        makeMapPanel(WCTViewer.CRN, "CRN", viewer.isLayerVisible(WCTViewer.CRN));


        // Create main panel
        jnxPanel = new JPanel();
        jnxPanel.setLayout(new BoxLayout(jnxPanel, BoxLayout.Y_AXIS));
        // Add the column labels
        jnxPanel.add(makeColumnLabels());
        // Add each map layer's panel in reverse order so top layer is on top of panel
        for (int n = numLayers - 1; n >= 0; n--) {
            try {
                jnxPanel.add((JPanel)layersPanel.elementAt(n));
            } catch (NullPointerException e) {}
        }
        // Add background color panel
        jnxPanel.add(makeBackgroundPanel());

        JScrollPane scrollPane = new JScrollPane(jnxPanel);

        // Create Load Shapefile Button
        jcbDataLayersOnTop = new JCheckBox("Data Layers On Top");
        jcbDataLayersOnTop.addActionListener(this);

        jbLoadShapefile = new JButton("Load Shapefile");
        jbLoadShapefile.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(jcbDataLayersOnTop);
        buttonPanel.add(jbLoadShapefile);

        // temp location ---------------------------
        final MapSelector finalThis = this;
//      JButton loadSatButton = new JButton("Load GOES Satellite");
//      loadSatButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent arg0) {
//      finalThis.loadSatelliteLayer();
//      }
//      });
//      buttonPanel.add(loadSatButton);




        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPane, "Center");
        mainPanel.add(buttonPanel, "South");

        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.add(mainPanel, "Center");


        wmsPanel = new WMSPanel(viewer);
        viewer.getMapPaneZoomChange().setWMSPanel(wmsPanel);




        
        
        
        
        dataLayersStack.setTitleBackgroundColor(this.getContentPane().getBackground());
        dataLayersStack.setSeparatorColor(this.getContentPane().getBackground().darker());
        dataLayersStack.setBackground(this.getContentPane().getBackground());
        
        
        // put it in a scrollpane
        JScrollPane dataLayersScrollPane = new JScrollPane(dataLayersStack);
        dataLayersScrollPane.setBorder(null);
        
        
        

        JPanel dataLayersPanel = new JPanel();
        dataLayersPanel.setLayout(new RiverLayout());

        
        radColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));
        gridColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));
        satColorTableCombo.setPreferredSize(new Dimension(260, radColorTableCombo.getPreferredSize().height));
        
        
        JPanel radarPanel = new JPanel();
//        radarPanel.setBorder(WCTUiUtils.myTitledBorder("Radar", 7));
        radarPanel.setLayout(new RiverLayout());

        jcbRadar = new JCheckBox("Visible", true);
        jcbRadar.setActionCommand("Radar");
        jcbRadar.addActionListener(new DataLayersListener());

        radTransparency.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                int percent = Integer.parseInt(((String)radTransparency.getSelectedItem()).replaceAll("%","").trim());
                viewer.setRadarTransparency(255 - ((int)((percent/100.0)*255)));
            }
        });
        radTransparency.setEditable(true);

        radSmoothing.addChangeListener(new RadarSmoothingChangeListener());
        radSmoothing.setEnabled(false);
        
        radLegend.setSelectedItem("Large");
        radLegend.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                if (radLegend.getSelectedItem().toString().equals("None")) {
                    viewer.setLegendVisibility(false);
                    viewer.componentResized(new ComponentEvent(viewer, 0));
                }
                else if (radLegend.getSelectedItem().toString().equals("Large")) {
                    viewer.setLegendVisibility(true);
                    viewer.componentResized(new ComponentEvent(viewer, 0));
                }
            }
        });

        JPanel radTransPanel = new JPanel();
        radTransPanel.setLayout(new RiverLayout());
        radTransPanel.add(new JLabel("Transparency"), "center");
        radTransPanel.add(radTransparency, "br center");

        JPanel radSmoothingPanel = new JPanel();
        radSmoothingPanel.setLayout(new RiverLayout());
        radSmoothingPanel.add(new JLabel("Smoothing"), "center");
        radSmoothingPanel.add(radSmoothing, "br center");
        
        
        final String smoothingText = 
                "<html><b>NOTE:</b> The smoothing option must be used with caution.  The smoothing <br>" +
                "algorithm is basic non-scientific pixel smoothing over the resampled image, similar to smoothing <br>" +
                "photographs.  Smoothing can be helpful in making the images visually appealing and allow easier <br>" +
                "visual detection of features such as hook echoes.  However, excessive smoothing can eliminate <br>" +
                "important features in the data and reduce the intensity of small events.  </html>"
            ;
        
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(finalThis, smoothingText, "Smoothing Warning", JOptionPane.WARNING_MESSAGE);
            }
        };
        JLabel infoLabel = new JLabel(new ImageIcon(MapSelector.class.getResource("/icons/question-mark.png")));
        infoLabel.setToolTipText(smoothingText);
        infoLabel.addMouseListener(mouseListener);
        radSmoothingPanel.add(infoLabel, "right");
        
        JPanel radLegendPanel = new JPanel();
        radLegendPanel.setLayout(new RiverLayout());
        radLegendPanel.add(new JLabel("Legend"), "center");
        radLegendPanel.add(radLegend, "br center");
        
        
        radEditColorTableLink.setText("Edit");
        radEditColorTableLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ColorTableEditorUI ctEditorUI = new ColorTableEditorUI(viewer);
				ctEditorUI.setVisible(true);
				ctEditorUI.pack();
			}
        });
        
        JPanel radColorTablePanel = new JPanel();
        radColorTablePanel.setLayout(new RiverLayout());
        radColorTablePanel.add(new JLabel("Color Table: "), "left");
        radColorTablePanel.add(radColorTableCombo, "hfill");
        radColorTablePanel.add(radEditColorTableLink, "");



        radarPanel.add(jcbRadar);
        radarPanel.add(radTransPanel, "tab");
        radarPanel.add(radSmoothingPanel, "tab");
        radarPanel.add(radLegendPanel, "tab");
//        radarPanel.add(radColorTablePanel, "br left");

        jcbRadar.setEnabled(false);
        radTransparency.setEnabled(false);
        radLegend.setEnabled(false);
        radColorTableCombo.setEnabled(false);
        radEditColorTableLink.setEnabled(false);



//        satellitePanel.setBorder(WCTUiUtils.myTitledBorder("Satellite", 7));
        gridSatellitePanel.setLayout(new RiverLayout());

        jcbGridSatellite = new JCheckBox("Visible", true);
        jcbGridSatellite.setActionCommand("Satellite");
        jcbGridSatellite.addActionListener(new DataLayersListener());




//        satTransparency.setEnabled(false);
        gridSatellitePanel.setEnabled(false);

        gridSatTransparency.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                if (gridSatTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
                    viewer.setGridSatelliteTransparency(-1);
                }
                else {
                    int percent = Integer.parseInt(((String)gridSatTransparency.getSelectedItem()).replaceAll("%","").trim());
                    viewer.setGridSatelliteTransparency(255 - ((int)((percent/100.0)*255)));
                }
                viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
            }
        });
        gridSatTransparency.setEditable(true);

        gridSatSmoothing.addChangeListener(new SatelliteSmoothingChangeListener());
        gridSatSmoothing.setEnabled(false);

        gridSatLegend.setSelectedItem("Medium");
        gridSatLegend.setEnabled(false);
        gridSatLegend.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                ///  TODO: add legend handling stuff here
                if (gridSatLegend.getSelectedItem().toString().equals("Medium")) {
                    viewer.setGridSatelliteLegendVisibility(true);
                }
                else {
                    viewer.setGridSatelliteLegendVisibility(false);
                }
            }
        });
       
        gridSatUnits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					if (viewer.getGridDatasetRaster() != null) {
						String nativeUnits = gridSatUnits.getSelectedItem().toString();
//						viewer.getGridDatasetRaster().setDestinationConversionUnits(nativeUnits);

						
						UnitDB unitDB = UnitDBManager.instance();
						// try by name and then by symbol
						Unit natUnit = unitDB.getByName(nativeUnits);
						if (natUnit == null) {
							natUnit = unitDB.getBySymbol(nativeUnits);
						}
						if (natUnit != null) {
							WCTProperties.setWCTProperty(natUnit.getDerivedUnit().getName(), natUnit.getName());
						}
						
						viewer.loadData();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}        	
        });
        

		jtfGridMinValue.setEnabled(false);
        jtfGridMaxValue.setEnabled(false);

        satColorTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("CHOSE: "+satColorTableCombo.getSelectedItem().toString());
                viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
            }
        });   

        gridColorTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("CHOSE: "+gridColorTableCombo.getSelectedItem().toString());
                viewer.setGridSatelliteColorTable(gridColorTableCombo.getSelectedItem().toString());
            }
        });   
        KeyListener minMaxListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
            	try {
            		if (jtfGridMinValue.getText().trim().length() > 0 &&
            				jtfGridMaxValue.getText().trim().length() > 0) {
            			
            			viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
            		}
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
            }            
        };
        jtfGridMinValue.addKeyListener(minMaxListener);
        jtfGridMaxValue.addKeyListener(minMaxListener);


        JPanel gridSatTransPanel = new JPanel();
        gridSatTransPanel.setLayout(new RiverLayout());
        gridSatTransPanel.add(new JLabel("Transparency"), "center");
        gridSatTransPanel.add(gridSatTransparency, "br center");

//        JPanel satSmoothingPanel = new JPanel();
//        satSmoothingPanel.setLayout(new RiverLayout());
//        satSmoothingPanel.add(new JLabel("Smoothing"), "center");
//        satSmoothingPanel.add(satSmoothing, "br center");
//        JLabel satInfoLabel = new JLabel(new ImageIcon(MapSelector.class.getResource("/icons/question-mark.png")));
//        satInfoLabel.setToolTipText(smoothingText);
//        satInfoLabel.addMouseListener(mouseListener);
//        satSmoothingPanel.add(satInfoLabel, "right");

        JPanel gridSatLegendPanel = new JPanel();
        gridSatLegendPanel.setLayout(new RiverLayout());
        gridSatLegendPanel.add(new JLabel("Legend"), "center");
        gridSatLegendPanel.add(gridSatLegend, "br center");


        jcbFlipGridColorTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
            }
        });
        
        
        
        
        
        
        jcomboGridMinMax.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String selectedItem = jcomboGridMinMax.getSelectedItem().toString();
                System.out.println("CHOSE: "+selectedItem);
                try {
                	if (selectedItem.equals("Auto")) {
                		jtfGridMinValue.setEnabled(false);
                        jtfGridMaxValue.setEnabled(false);

                		jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMinValue() ));
                		jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMaxValue() ));
                		viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());

                	}
                	else if (selectedItem.equals("Valid Range")) {
                		jtfGridMinValue.setEnabled(false);
                        jtfGridMaxValue.setEnabled(false);
                        
                        if (gridHasValidRangeAttributes) {                		
                        	jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMinValue() ));
                        	jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMaxValue() ));
                        }
                        else {
                        	jcomboGridMinMax.setSelectedItem("Auto");
                            JOptionPane.showMessageDialog(viewer, "The valid_range attributes are not present for this variable.", "General Warning", JOptionPane.WARNING_MESSAGE);
                        }
                		viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
                		
                	}
                	else if (selectedItem.equals("Custom")) {
                		jtfGridMinValue.setEnabled(true);
                        jtfGridMaxValue.setEnabled(true);
                	}
                	else {
                		System.err.println("invalid selection: "+selectedItem);
                	}
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });  
        
        
        
//        ButtonGroup buttonGroup = new ButtonGroup();
//        buttonGroup.add(jrbGridAutoMinMax);
//        buttonGroup.add(jrbGridValidRangeMinMax);
//        buttonGroup.add(jrbGridCustomMinMax);
        
//        jrbGridAutoMinMax.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                jtfGridMinValue.setEnabled(jrbGridCustomMinMax.isSelected());
//                jtfGridMaxValue.setEnabled(jrbGridCustomMinMax.isSelected());
//                if (jrbGridAutoMinMax.isSelected()) {
//                    try {
//                        jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMinValue() ));
//                        jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMaxValue() ));
//                        viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        jrbGridValidRangeMinMax.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                jtfGridMinValue.setEnabled(jrbGridCustomMinMax.isSelected());
//                jtfGridMaxValue.setEnabled(jrbGridCustomMinMax.isSelected());
//                if (jrbGridValidRangeMinMax.isSelected()) {
//                    try {
//                        jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMinValue() ));
//                        jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getValidRangeMaxValue() ));
//                        viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        
//        jrbGridCustomMinMax.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                jtfGridMinValue.setEnabled(jrbGridCustomMinMax.isSelected());
//                jtfGridMaxValue.setEnabled(jrbGridCustomMinMax.isSelected());
////                if (jrbGridAutoMinMax.isSelected()) {
////                    try {
////                        jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMinValue() ));
////                        jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format( viewer.getGridDatasetRaster().getGridMaxValue() ));
////                        viewer.setGridSatelliteColorTable(satColorTableCombo.getSelectedItem().toString());
////                    } catch (Exception ex) {
////                        ex.printStackTrace();
////                    }
////                }
//            }
//        });

        
        
        
        
        jtfGridMinValue.setEnabled(false);
        jtfGridMaxValue.setEnabled(false);
        gridColorTablePanel.setLayout(new RiverLayout());
        gridColorTablePanel.add(new JLabel("Color Table: "), "left");
        gridColorTablePanel.add(gridColorTableCombo, "hfill");
        gridColorTablePanel.add(jcbFlipGridColorTable, "");
        gridColorTablePanel.add(jcomboGridMinMax, "br left");
        gridColorTablePanel.add(new JLabel("Min: "));
        gridColorTablePanel.add(jtfGridMinValue);
        gridColorTablePanel.add(new JLabel("Max: "));
        gridColorTablePanel.add(jtfGridMaxValue);

//        gridColorTablePanel.add(new JLabel("Units: "), "br left");
//        gridColorTablePanel.add(gridSatUnits, "left");


        
        satColorTablePanel.setLayout(new RiverLayout());
        satColorTablePanel.add(new JLabel("Color Table: "), "left");
        satColorTablePanel.add(satColorTableCombo, "hfill");
        
        gridSatellitePanel.add(jcbGridSatellite);
        gridSatellitePanel.add(gridSatTransPanel, "tab");
//        gridSatellitePanel.add(satSmoothingPanel, "tab");
        gridSatellitePanel.add(gridSatLegendPanel, "tab");
        gridSatellitePanel.add(satColorTablePanel, "br left");
        
        jcbGridSatellite.setEnabled(false);
        gridSatTransparency.setEnabled(false);
        gridSatSmoothing.setEnabled(false);
        gridSatLegend.setEnabled(false);
        satColorTableCombo.setEnabled(false);

        dataLayersPanel.add(radarPanel, "hfill");
        dataLayersPanel.add(gridSatellitePanel, "br hfill");
//        dataLayersPanel.add(smoothingDisclaimer, "br");

        
        JButton snapshotLayerButton = new JButton("Snapshot Layer");
        snapshotLayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                viewer.snapshotCurrentLayer();
                
//                System.out.println(viewer.getSnapshotLayers().toString());
//                RenderedLayer[] layers = ((StyledMapRenderer)viewer.getMapPane().getRenderer()).getLayers();
//                for (int n=0; n<layers.length; n++) {
//                    System.out.println("LAYER "+n+":  "+layers[n].getName(layers[n].getLocale())+"  "+layers[n].isVisible());
//                }
            }
        });

        
        JButton clearButton = new JButton("Clear Active Layer");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewer.clearAllData();
                
                viewer.setGridSatelliteVisibility(false);
                viewer.setRadarGridCoverageVisibility(false);
//                viewer.setLegendVisibility(false);
                viewer.setShowAlphanumericLayers(false);

                jcbRadar.setSelected(false);
                jcbRadar.setEnabled(false);
                radTransparency.setEnabled(false);
                radSmoothing.setEnabled(false);
                radLegend.setEnabled(false);
                viewer.setRangeRingVisibility(false);

                jcbGridSatellite.setSelected(false);
                jcbGridSatellite.setEnabled(false);
                gridSatTransparency.setEnabled(false);
                gridSatSmoothing.setEnabled(false);
                gridSatLegend.setEnabled(false);
                satColorTableCombo.setEnabled(false);
                gridColorTableCombo.setEnabled(false);
                jcomboGridMinMax.setEnabled(false);
                jcbFlipGridColorTable.setEnabled(false);
                jtfGridMaxValue.setEnabled(false);
                jtfGridMinValue.setEnabled(false);
                
            }
        });
        
        JButton clearSnapshotsButton = new JButton("Clear Snapshots");
        clearSnapshotsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            }
        });

        JXHyperlink filterLink = new JXHyperlink();
        filterLink.setText("Data Filter");
        filterLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
	            viewer.getFilterGUI().setVisible(true);
	            viewer.getFilterGUI().setLocation(getX()+getWidth()-20, getY()+38);
	            viewer.getFilterGUI().pack();
			}
        });
        

        
        
        dataLayersScrollPane.setBorder(WCTUiUtils.myBorder(0, 0, 2, 0));
        
        dataPanel.setLayout(new RiverLayout());
        dataPanel.add(dataLayersScrollPane, "hfill vfill");
        dataPanel.add(snapshotLayerButton, "br left");
        dataPanel.add(clearButton, "left");
        dataPanel.add(new JPanel(), "left hfill");
        dataPanel.add(filterLink, "right");

        
        

        
        
        
        

        
        
        
        
        
        
        dataLayersStack.addBox("Radar (Active)", radarPanel);
        dataLayersStack.addBox("Grid/Satellite (Active)", gridSatellitePanel);
        
        dataLayersStack.addBoxListener(new BoxListener() {
            @Override
            public void addedBox(BoxEvent e) {
//                System.out.println("added box");
//                viewer.getSnapshotLayers()
            }
            @Override
            public void removedBox(BoxEvent e) {
//                System.out.println("removed box");
                List<SnapshotLayer> layers = viewer.getSnapshotLayers();
                for (SnapshotLayer l : layers) {
                    System.out.println("snapshot layer name: "+l.getName()+" , event - box title: "+e.getTitle());
                    if (l.getName().equals(e.getTitle())) {
                        System.out.println("removed box layer: "+l.getName());
                        viewer.removeSnapshotLayer(l);
                        dataPanel.revalidate();
                        return;
                    }
                }
            }
        });
        
        
        
        
        
        
        
        
        tabPane = new JTabbedPane();      

        tabPane.add(dataPanel, "Data Layers");
        tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
        tabPane.add(wmsPanel, "Background Maps (WMS)");

        getContentPane().add(tabPane);







        
        

        
		// A global way to add a key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				//                System.out.println(e.getKeyCode()+"  ESCAPE=" + KeyEvent.VK_ESCAPE+" ENTER="+KeyEvent.VK_ENTER+" D="+KeyEvent.VK_D);
				//                System.out.println(e.getModifiers()+"  CTRL=" + KeyEvent.CTRL_DOWN_MASK);
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {                
					viewer.screenCapture();
				}
				return false;
			}
		});



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


    } // END METHOD createGUI()


    
    public void setGridHasValidRangeAttributes(boolean hasValidRange) {
//  "Auto", "Valid Range", "Custom"	

//    	if (hasValidRange) {
//    		jcomboGridMinMax.removeAllItems();
//    		jcomboGridMinMax.addItem("Auto");
//    		jcomboGridMinMax.addItem("Valid Range");
//    		jcomboGridMinMax.addItem("Custom");
//    	}
//    	else {
//    		jcomboGridMinMax.removeAllItems();
//    		jcomboGridMinMax.addItem("Auto");
//    		jcomboGridMinMax.addItem("Custom");
//    	}
    	this.gridHasValidRangeAttributes = hasValidRange;
    }
    
    
    
    public void addSnapshotLayer(final SnapshotLayer snapshotLayer) {
        dataLayersStack.addBox(snapshotLayer.getName(), createSnapshotLayerPanel(snapshotLayer), true, 2);
        dataPanel.validate();
    }
    
    private JPanel createSnapshotLayerPanel(final SnapshotLayer snapshotLayer) {
        return new SnapshotPanel(viewer, dataLayersStack, snapshotLayer);
    }


    
    public boolean isGridValidRangeMinMaxSelected() {
    	return jcomboGridMinMax.getSelectedItem().toString().equals("Valid Range");
    }

    
    public void setGridColorTableMinMaxValue(double minValue, double maxValue) {
        if (Math.abs(minValue) > 10000 || 
                ( Math.abs(minValue) > 0 && Math.abs(minValue) < 0.01)) {
            jtfGridMinValue.setText(WCTUtils.DECFMT_SCI.format(minValue));
        }
        else {
            jtfGridMinValue.setText(WCTUtils.DECFMT_0D0000.format(minValue));
        }
        if (Math.abs(maxValue) > 10000 || 
                ( Math.abs(maxValue) > 0 && Math.abs(maxValue) < 0.01)) {
            jtfGridMaxValue.setText(WCTUtils.DECFMT_SCI.format(maxValue));
        }
        else {
            jtfGridMaxValue.setText(WCTUtils.DECFMT_0D0000.format(maxValue));
        }
    }
    public boolean isGridAutoMinMaxSelected() {
    	return jcomboGridMinMax.getSelectedItem().toString().equals("Auto");
    }
    
    public void setGridAutoMinMax(boolean isAutoMinMax) {
    	if (isAutoMinMax) {
    		jcomboGridMinMax.setSelectedItem("Auto");
    	}
    	else {
    		jcomboGridMinMax.setSelectedItem("Custom");
    	}
    }
    
    public double getGridColorTableMinValue() {
        try {
            return Double.parseDouble(jtfGridMinValue.getText());
        } catch (Exception e) {
            return 0;
        }
    }
    public double getGridColorTableMaxValue() {
        try {
            return Double.parseDouble(jtfGridMaxValue.getText());
        } catch (Exception e) {
            return 0;
        }
    }
    public String getGridColorTableName() {
        return gridColorTableCombo.getSelectedItem().toString();
    }

    public boolean isFlipGridColorTable() {
        return jcbFlipGridColorTable.isSelected();
    }
    

    public void setRadarLegend(String type) {
        if (type.trim().equalsIgnoreCase("None")) {
            radLegend.setSelectedItem("None");
        }
        else if (type.trim().equalsIgnoreCase("Small")) {
            radLegend.setSelectedItem("Small");
        }
        else if (type.trim().equalsIgnoreCase("Medium")) {
            radLegend.setSelectedItem("Medium");
        }
        else if (type.trim().equalsIgnoreCase("Large")) {
            radLegend.setSelectedItem("Large");
        }
    }
    
    public String getRadarLegendType() {
        return radLegend.getSelectedItem().toString();
    }
    
    public void setGridSatelliteLegend(String type) {
        if (type.trim().equalsIgnoreCase("None")) {
            gridSatLegend.setSelectedItem("None");
        }
        else if (type.trim().equalsIgnoreCase("Small")) {
            gridSatLegend.setSelectedItem("Small");
        }
        else if (type.trim().equalsIgnoreCase("Medium")) {
            gridSatLegend.setSelectedItem("Medium");
        }
        else if (type.trim().equalsIgnoreCase("Large")) {
            gridSatLegend.setSelectedItem("Large");
        }
    }

    public String getGridSatelliteLegendType() {
        return gridSatLegend.getSelectedItem().toString();
    }

    
    public int getGridSatelliteTransparency() {
        if (gridSatTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
            return -1;
        }
        else {
            return Integer.parseInt(gridSatTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
        }
    }
    
    public void setGridSatelliteTransparency(int transparency) {
        if (transparency == -1) {
            gridSatTransparency.setSelectedItem("Default");
        }
        else {
            gridSatTransparency.setSelectedItem(" "+transparency+"%");
        }
    }
    
    public int getRadarTransparency() {
        if (radTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
            return -1;
        }
        else {
            return Integer.parseInt(radTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
        }
    }
    
    /**
     * If == -1, then set to 'Default'
     * @param transparencyString
     */
    public void setRadarTransparency(int transparency) {
        if (transparency == -1) {
            radTransparency.setSelectedItem("Default");
        }
        else {
            radTransparency.setSelectedItem(" "+transparency+"%");
        }
    }
    
    public int getRadarSmoothingFactor() {
        return Integer.parseInt(radSmoothing.getValue().toString());
    }
    
    public void setRadarSmoothingFactor(int smoothingFactor) {
        radSmoothing.setValue(new Integer(smoothingFactor));
    }
    
    public void refreshAvailableUnitConversions(String nativeUnits) 
    		throws UnitDBException, IllegalAccessException, InstantiationException {

    	if (nativeUnits == null) {
    		return;
    	}
    	
    	String selectedValue = gridSatUnits.getSelectedItem().toString();
    	
		UnitDB unitDB = UnitDBManager.instance();
		Iterator iter = unitDB.getIterator();
		Vector<String> compatibleUnits = new Vector<String>();
		compatibleUnits.add("Native -- no conversion ("+viewer.getGridDatasetRaster().getUnits()+")");
		
		// try by name and then by symbol
		Unit natUnit = unitDB.getByName(nativeUnits);
		if (natUnit == null) {
			natUnit = unitDB.getBySymbol(nativeUnits);
		}
		
		if (natUnit == null) {
			return;
		}
		
		// store native and derived unit
		System.out.println("-------------------------");
		String derivedUnitName = natUnit.getDerivedUnit().getName();
//		WCTProperties.setWCTProperty(derivedUnitName, natUnit.getName());
		
		System.out.println(natUnit.getDerivedUnit().getName());
//		System.out.println(natUnit.getDerivedUnit().getDimension().get);
//		System.out.println(natUnit.getDerivedUnit().getQuantityDimension().);
		System.out.println("-------------------------");
		
		
		while (iter.hasNext() && natUnit != null) {
			Unit unit = (Unit)(iter.next());
			if (natUnit.isCompatible(unit)) {
				System.out.println(unit);
//				compatibleUnits.add(unit.getName());
				compatibleUnits.add(unit.getName());
			}
		}
		
		gridSatUnits.setModel(new DefaultComboBoxModel<String>(compatibleUnits));
		
		ActionListener[] al = gridSatUnits.getActionListeners();
		for (ActionListener a : al) {
			gridSatUnits.removeActionListener(a);
		}
		
//		gridSatUnits.setSelectedItem(selectedValue);
		String preferenceUnit = WCTProperties.getWCTProperty(natUnit.getDerivedUnit().getName());
		if (preferenceUnit != null) {
			gridSatUnits.setSelectedItem(preferenceUnit);
		}
		
		for (ActionListener a : al) {
			gridSatUnits.addActionListener(a);
		}

    }













    public void setSelectedTab(int index) {
        tabPane.setSelectedIndex(index);
    }




    private JPanel makeColumnLabels() {
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(1, 2));
        JPanel apanel = new JPanel();
        apanel.setLayout(new GridLayout(1, 3));

        apanel.add(new JLabel("Color", JLabel.CENTER));
        apanel.add(new JLabel("Size", JLabel.CENTER));
        apanel.add(new JLabel("Label", JLabel.CENTER));

        jpanel.add(new JLabel("Layer", JLabel.CENTER));
        jpanel.add(apanel);

        return jpanel;

    } // END METHOD makeMapPanel(int index, String Default)

    private JPanel makeLocalColumnLabels() {
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(1, 2));
        JPanel apanel = new JPanel();
        apanel.setLayout(new GridLayout(1, 3));

        apanel.add(new JLabel("Color", JLabel.CENTER));
        apanel.add(new JLabel("Size", JLabel.CENTER));
        apanel.add(new JLabel("Remove", JLabel.CENTER));

        jpanel.add(new JLabel("Local Map Layer", JLabel.CENTER));
        jpanel.add(apanel);
        jpanel.setForeground(Color.blue);

        return jpanel;

    } // END METHOD makeMapPanel(int index, String Default)



    private void makeMapPanel(int index, String name, boolean status) {

        layersPanel.setElementAt((Object)(new JPanel()), index);
        ((JPanel)layersPanel.elementAt(index)).setLayout(new GridLayout(1, 2));
        attPanel.setElementAt((Object)(new JPanel()), index);
        ((JPanel)attPanel.elementAt(index)).setLayout(new GridLayout(1, 3));

        // Create  On/Off Button
        jcbVisible.setElementAt(new JCheckBox(name, status), index);
        ((JCheckBox)jcbVisible.elementAt(index)).addActionListener(this);
        ((JCheckBox)jcbVisible.elementAt(index)).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        ((JCheckBox)jcbVisible.elementAt(index)).setFont(((JCheckBox)jcbVisible.elementAt(index)).getFont().deriveFont(Font.BOLD));


//      System.out.println("MAKE MAP PANEL: index="+index+" name="+name+" status="+status+" color="+viewer.getThemeFillColor(index));      

        // Create Change Color Button
        JButton button = new JButton("");
        jbColor.setElementAt((Object)(button), index);
        ((JButton)jbColor.elementAt(index)).addActionListener(this);

        // Create Change Style
        String[] options = new String[5];
        options[0] = "1";
        options[1] = "2";
        options[2] = "3";
        options[3] = "4";
        options[4] = "5";
        styleOptions.setElementAt((Object)(new JComboBox(options)), index);
        ((JComboBox)styleOptions.elementAt(index)).setSelectedIndex(viewer.getLayerLineWidth(index) - 1);
        ((JComboBox)styleOptions.elementAt(index)).addActionListener(this);
        ((JComboBox)styleOptions.elementAt(index)).setBackground(Color.white);
        ((JComboBox)styleOptions.elementAt(index)).setPreferredSize(new Dimension(20, 20));

        // Create  Label On/Off Checkbox
        jcbLabel.setElementAt(new JCheckBox("", false), index);
        ((JCheckBox)jcbLabel.elementAt(index)).addActionListener(this);
        ((JCheckBox)jcbLabel.elementAt(index)).setHorizontalAlignment(SwingConstants.CENTER);


        // Set background colors      
        if (index <= 1) {
            Color c = viewer.getLayerFillColor(index);
            ((JButton)jbColor.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setBackground(c);
            ((JCheckBox)jcbLabel.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setForeground(invertColor(c));
        }
        else {
            Color c = viewer.getLayerLineColor(index);
            ((JButton)jbColor.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setBackground(c);
            ((JCheckBox)jcbLabel.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setForeground(invertColor(c));
        }
        ((JButton)jbColor.elementAt(index)).setPreferredSize(new Dimension(20, 20));


        // Disable if not applicable
        if (index <= 1) {
            ((JComboBox)styleOptions.elementAt(index)).setEnabled(false);
            ((JCheckBox)jcbLabel.elementAt(index)).setEnabled(false);
        }

        // Disable if theme did not load
        if (! viewer.getLayerStatus(index)) {
            ((JCheckBox)jcbVisible.elementAt(index)).setEnabled(false);
            ((JButton)jbColor.elementAt(index)).setEnabled(false);
            ((JButton)jbColor.elementAt(index)).setBackground(null);
            ((JComboBox)styleOptions.elementAt(index)).setEnabled(false);
            ((JComboBox)styleOptions.elementAt(index)).setBackground(null);
            ((JCheckBox)jcbLabel.elementAt(index)).setEnabled(false);
        }

        ((JPanel)attPanel.elementAt(index)).add((JButton)jbColor.elementAt(index));
        ((JPanel)attPanel.elementAt(index)).add((JComboBox)styleOptions.elementAt(index));
        ((JPanel)attPanel.elementAt(index)).add((JCheckBox)jcbLabel.elementAt(index));

        ((JPanel)layersPanel.elementAt(index)).add((JCheckBox)jcbVisible.elementAt(index));
        ((JPanel)layersPanel.elementAt(index)).setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));

        ((JPanel)layersPanel.elementAt(index)).add((JPanel)attPanel.elementAt(index));

        //layersPanel[index].setPreferredSize(new Dimension(200,40));

        return ;
        //return ((JPanel)layersPanel.elementAt(index));

    } // END METHOD makeMapPanel()


    private JPanel makeBackgroundPanel() {

        jbBackground = new JButton();
        jcbBogus = new JCheckBox("", true);
//        jcbBogus.setBackground(viewer.getBackgroundColor());
        jcbBogus.setEnabled(false);
        jcbBogus.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        JComboBox jcomboBogus = new JComboBox(new Object[] {"1"});
        jcomboBogus.setEnabled(false);
//        JCheckBox jcbBogus1 = new JCheckBox("", false);
//        jcbBogus1.setBackground(viewer.getBackgroundColor());
//        jcbBogus1.setEnabled(false);

        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(1, 2));
        apanel.setLayout(new GridLayout(1, 3));

        //apanel.add(jcbBogus1, JLabel.CENTER);
        //apanel.add(jcomboBogus, JLabel.CENTER);
        apanel.add(jbBackground, JLabel.CENTER);
        apanel.add(new JLabel("", JLabel.CENTER));
        apanel.add(new JLabel("", JLabel.CENTER));
//        apanel.setBackground(viewer.getBackgroundColor());

        backgroundLabel = new JLabel("Background");
        backgroundLabel.setFont(backgroundLabel.getFont().deriveFont(Font.BOLD));
//        backgroundLabel.setBackground(viewer.getBackgroundColor());
//        backgroundLabel.setForeground(invertColor(viewer.getBackgroundColor()));
        

        bpanel.setLayout(new BoxLayout(bpanel, BoxLayout.X_AXIS));
        bpanel.add(jcbBogus);
        bpanel.add(backgroundLabel);
//        bpanel.setBackground(viewer.getBackgroundColor());
//      jpanel.add(new JLabel("Background"));
        jpanel.add(bpanel);
        jpanel.add(apanel);

        jpanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));

        
        // Create Change Color Button
        jbBackground.addActionListener(this);
        jbBackground.setBackground(viewer.getBackgroundColor());
        jbBackground.setPreferredSize(new Dimension(20, 20));

        return jpanel;

    } // END METHOD makeBackgroundPanel()



    public void addLocalLayerPanel(String name) {
        sizeVectors(++numLayers);
        if (numLayers == (WCTViewer.NUM_LAYERS + 1)) // only add once
            jnxPanel.add(makeLocalColumnLabels());
        makeMapPanel((numLayers - 1), name, true); // 0-based
        jnxPanel.add((JPanel)layersPanel.elementAt(numLayers - 1));
        return ;
    } // END METHOD addLocalThemePanel()

    public void removeLocalLayerPanel(int index) {

        jnxPanel.remove((JPanel)layersPanel.elementAt(index));
        jnxPanel.repaint();
        pack();
        return ;
    } // END METHOD removeLocalThemePanel()


    public String getLayerName(int index) {
        try {
            return ((JCheckBox)jcbVisible.elementAt(index)).getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Color getLayerColor(int index) {
        try {
            return ((JButton)jbColor.elementAt(index)).getBackground();
        } catch (Exception e) {
            return null;
        }
    } // END METHOD getThemeColor()

    public void setLayerColor(int index, Color c) {
        try {
            ((JButton)jbColor.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setBackground(c);
            ((JCheckBox)jcbLabel.elementAt(index)).setBackground(c);
//            ((JCheckBox)jcbVisible.elementAt(index)).setForeground(invertColor(c));
        } catch (Exception e) {
        }      
    } // END METHOD setThemeColor()

    public int getLayerLineWidth(int index) {
        try {
            return ((JComboBox)styleOptions.elementAt(index)).getSelectedIndex() + 1;
        } catch (Exception e) {
            return -1;
        }
    } // END METHOD getThemeLineWidth()

    public void setLayerLineWidth(int index, int newWidth) {
        try {
            ((JComboBox)styleOptions.elementAt(index)).setSelectedIndex(newWidth-1);
        } catch (Exception e) {
        }
    } // END METHOD setThemeLineWidth()

    public boolean getLayerVisibility(int index) {
        try {
            return ((JCheckBox)jcbVisible.elementAt(index)).isSelected();
        } catch (Exception e) {
            return false;
        }
    } // END METHOD isThemeVisible()

    public void setLayerVisibility(int index, boolean visible) {
        try {
            jcbVisible.elementAt(index).setSelected(visible);
            actionPerformed(new ActionEvent(jcbVisible.elementAt(index), 0, "Manual Set Layer Visibility"));
        } catch (Exception e) {
        }
    } // END METHOD setIsThemeVisible()

    public boolean getLabelVisibility(int index) {
        try {
            return ((JCheckBox)jcbLabel.elementAt(index)).isSelected();
        } catch (Exception e) {
            return false;
        }
    } // END METHOD getLabelVisibility()

    public void setLabelVisibility(int index, boolean visible) {
        try {
            jcbLabel.elementAt(index).setSelected(visible);
            actionPerformed(new ActionEvent(jcbLabel.elementAt(index), 0, "Manual Set Label Visibility"));
        } catch (Exception e) {
        }
    } // END METHOD setLabelVisibility()

    public Color getBackgroundColor() {
        return jbBackground.getBackground();
    }

    public void setBackgroundColor(Color c) {
        jbBackground.setBackground(c);
//        apanel.setBackground(c);
//        bpanel.setBackground(c);
//        backgroundLabel.setBackground(c);
//        jcbBogus.setBackground(c);
//        jbBackground.setForeground(invertColor(c));
//        backgroundLabel.setForeground(invertColor(c));
    }



    public WMSPanel getWMSPanel() {
        return wmsPanel;
    }

    public void setDataLayersOnTop(boolean isOnTop) {
        jcbDataLayersOnTop.setSelected(isOnTop);
        if (jcbDataLayersOnTop.isSelected()) {
            viewer.getRadarRenderedGridCoverage().setZOrder(400+0.9f);
            viewer.getGridSatelliteRenderedGridCoverage().setZOrder(400+0.8f);
        }
        else {
            viewer.getRadarRenderedGridCoverage().setZOrder(1+0.9f);
            viewer.getGridSatelliteRenderedGridCoverage().setZOrder(1+0.8f);
        }
    }

    public boolean isDataLayersOnTop() {
        return jcbDataLayersOnTop.isSelected();
    }

    // Implementation of ActionListener interface.
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();
        if (source == jbLoadShapefile) {
            loadShapefile();
        }
        else if (source == jcbDataLayersOnTop) {
            setDataLayersOnTop(jcbDataLayersOnTop.isSelected());
        }
        else if (source == jbBackground) {
            Color newColor = JColorChooser.showDialog(MapSelector.this,
                    "Choose Map Layer Color",
                    jbBackground.getBackground());
            if (newColor != null) {
                jbBackground.setBackground(newColor);
                viewer.setBackgroundColor(newColor);
//                backgroundLabel.setBackground(newColor);
//                apanel.setBackground(newColor);
//                bpanel.setBackground(newColor);
//                jcbBogus.setBackground(newColor);
//                jbBackground.setForeground(invertColor(newColor));
//                backgroundLabel.setForeground(invertColor(newColor));
            }
            return;
        }


        for (int n = 0; n < numLayers; n++) {
            //for (int n=0; n<5; n++) {
            if (source == jcbVisible.elementAt(n)) {


                if (jcbVisible.elementAt(n).isSelected()) {
                    viewer.setLayerVisibility(n, true);
                    if (jcbLabel.elementAt(n).isSelected()) {
                        viewer.setLabelVisibility(n, true);
                    }
                } else {
                    viewer.setLayerVisibility(n, false);
                    viewer.setLabelVisibility(n, false);
                }
                n = numLayers;
            } // END if
            // Color changed
            else if (source == jbColor.elementAt(n)) {
                Color newColor = JColorChooser.showDialog(MapSelector.this,
                        "Choose Map Layer Color",
                        ((JButton)jbColor.elementAt(n)).getBackground());
                if (newColor != null) {


                    final int fn = n;
                    final Color fnewColor = newColor;
                    gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
                        public Object construct() {


//                            ((JCheckBox)jcbVisible.elementAt(fn)).setForeground(invertColor(fnewColor));
//                            ((JCheckBox)jcbVisible.elementAt(fn)).setBackground(fnewColor);
                            ((JButton)jbColor.elementAt(fn)).setBackground(fnewColor);
                            ((JCheckBox)jcbLabel.elementAt(fn)).setBackground(fnewColor);

                            if (fn == WCTViewer.STATES || fn == WCTViewer.COUNTRIES) {
                                viewer.setLayerFillAndLineColor(fn, fnewColor, fnewColor);
                            }
                            else if (viewer.getLayerShapeType(fn) == SimpleShapefileLayer.POINT ||
                                    viewer.getLayerShapeType(fn) == SimpleShapefileLayer.MULTIPOINT ) {

                                viewer.setLayerFillAndLineColor(fn, fnewColor, fnewColor);
                            }
                            else {
                                viewer.setLayerLineColor(fn, fnewColor);
                            }

                            return "DONE";
                        }
                    };
                    worker.start();                  





                }
                n = numLayers;
            } // END if
            // Size changed
            else if (source == styleOptions.elementAt(n)) {
                if (n == WCTViewer.STATES || n == WCTViewer.COUNTRIES) {
//                    viewer.setLayerLineWidth(n, ((JComboBox)styleOptions.elementAt(n)).getSelectedIndex() + 1 );
                }
                else {
                    viewer.setLayerLineWidth(n, ((JComboBox)styleOptions.elementAt(n)).getSelectedIndex() + 1 );
                }

                n = numLayers;
            } // END if
            // Label/Remove Checkbox
            else if (source == jcbLabel.elementAt(n)) {
                // Label things
                if (n < WCTViewer.NUM_LAYERS) {
                    if (((JCheckBox)jcbLabel.elementAt(n)).isSelected() &&
                            ((JCheckBox)jcbVisible.elementAt(n)).isSelected()) {
                        viewer.setLabelVisibility(n, true);
                    } else {
                        viewer.setLabelVisibility(n, false);
                    }
                }
                // Remove local layer
                else {
                    // Remove from view
                    viewer.removeLayer(n);
                    // Remove panel from this frame
                    removeLocalLayerPanel(n);
                }
                n = numLayers;
            } // END if



        } // END for



    } // actionPerformed


    
    
    
    
    
    
    
    
    /**
     * Opens dialog for adding shapefile to NexradIAViewer
     */
    public void loadShapefile() {
        // Set up File Chooser
        if (lastFolder == null) {
            //lastFolder = new File(System.getProperty("user.home"));
            String jnxprop = WCTProperties.getWCTProperty("local_overlay_dir");
            if (jnxprop != null) {
                lastFolder = new File(jnxprop);
            }
            else {
                lastFolder = new File(System.getProperty("user.home"));
            }
        }

        JFileChooser fc = new JFileChooser(lastFolder);

        OpenFileFilter shpFilter = new OpenFileFilter("shp", true, "ESRI Shapefiles");
        fc.addChoosableFileFilter(shpFilter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(shpFilter);

        int returnVal = fc.showOpenDialog(jbLoadShapefile);
        File file = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            lastFolder = file.getParentFile();
            WCTProperties.setWCTProperty("local_overlay_dir", lastFolder.toString());
            //This is where a real application would open the file.
            System.out.println("Opening: " + file.getName() + ".");

            // Check for matching .prj projection file
            String prj = "GEOGCS[\"GCS_North_American_1983\"," +
            "DATUM[\"D_North_American_1983\"," +
            "SPHEROID[\"GRS_1980\",6378137,298.257222101]]," +
            "PRIMEM[\"Greenwich\",0]," +
            "UNIT[\"Degree\",0.0174532925199433]]";

            String prj2 = "GEOGCS[\"GCS_North_American_1983\"," +
            "DATUM[\"D_North_American_1983\"," +
            "SPHEROID[\"GRS_1980\",6378137.0,298.257222101]]," +
            "PRIMEM[\"Greenwich\",0.0]," +
            "UNIT[\"Degree\",0.0174532925199433]]";

            String prj3 = "GEOGCS[\"GCS_WGS_1984\"," +
            "DATUM[\"D_WGS_1984\"," +
            "SPHEROID[\"WGS_1984\",6378137,298.257223563]]," +
            "PRIMEM[\"Greenwich\",0.0]," +
            "UNIT[\"Degree\",0.0174532925199433]]";

            String prj4 = "GEOGCS[\"GCS_WGS_1984\"," +
            "DATUM[\"D_WGS_1984\"," +
            "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," +
            "PRIMEM[\"Greenwich\",0.0]," +
            "UNIT[\"Degree\",0.0174532925199433]]";

            String prj5 = "GEOGCS[\"GCS_WGS_1984\","+
            "DATUM[\"D_WGS_1984\","+
            "SPHEROID[\"WGS_1984\",6378137,298.257223563]],"+
            "PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.0174532925199433]]";

            System.out.println(prj3);

            String prjfile = file.toString();
            prjfile = prjfile.substring(0, prjfile.length() - 4);
            int choice = JOptionPane.YES_OPTION;
            BufferedReader bw = null;
            try {
                bw = new BufferedReader(new FileReader(prjfile + ".prj"));
                //if (! (bw.readLine()).equals(prj)) {
                String str = bw.readLine();
                if (! ((str.trim()).equals(prj) || (str.trim()).equals(prj2) || (str.trim()).equals(prj3) 
                        || (str.trim()).equals(prj4) || (str.trim()).equals(prj5))) {

                    String message = "The projection file \n" +
                    "<html><font color=red>" + prjfile + ".prj</font></html>\n" +
                    "does not match the required projection.\n" +
                    "The data will NOT be correctly geolocated.\n\n" +
                    "Required projection parameters:\n" +
                    "GEOGCS[\"GCS_WGS_1984\",\n" +
                    "DATUM[\"D_WGS_1984\",\n" +
                    "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],\n" +
                    "PRIMEM[\"Greenwich\",0.0],\n" +
                    "UNIT[\"Degree\",0.0174532925199433]]\n\n"+                        
                    "--OR--\n\n"+
                    "GEOGCS[\"GCS_North_American_1983\",\n" +
                    "DATUM[\"D_North_American_1983\",\n" +
                    "SPHEROID[\"GRS_1980\",6378137,298.257222101]],\n" +
                    "PRIMEM[\"Greenwich\",0],\n" +
                    "UNIT[\"Degree\",0.0174532925199433]]\n\n" +
                    "The data may be INCORRECTLY geolocated.\n\n" +
                    "Do you want to proceed?";
                    choice = JOptionPane.showConfirmDialog(this, (Object) message,
                            "PROJECTION FILES DO NOT MATCH", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    //      "Please reproject or use different local data.";
                    //JOptionPane.showMessageDialog(null, (Object) message,
                    //      "PROJECTION FILES DO NOT MATCH", JOptionPane.ERROR_MESSAGE);
                    //choice = JOptionPane.NO_OPTION;
                }
            } catch (Exception e) {
                String message = "The projection file \n" +
                "<html><font color=red>" + prjfile + ".prj</font></html>\n" +
                "could not be found.\n" +
                "No projection is defined for this shapefile.\n" +
                "The data may be INCORRECTLY geolocated.\n\n" +
                "Do you want to proceed?";
                choice = JOptionPane.showConfirmDialog(this, (Object) message,
                        "PROJECTION FILE NOT FOUND", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    bw.close();
                } catch (Exception e) {
                }
            }

            if (choice == JOptionPane.YES_OPTION) {

                // Initialize progress frame
                /*
                 *  Progress info;
                 *  f = new Frame();
                 *  info = new Progress(f, false);
                 *  info.setTitle("File Reading Progress");
                 *  status = new Label("Loading: " + file.getName());
                 *  info.add(status, "Center");
                 *  info.show();
                 *  status.setForeground(new Color(57, 24, 198));
                 */
                // Check for a shapefile smaller than xxx Bytes
                if (file.length() < 55000000) {
                    // Open up the file with a Shapefile Reader
                    try {
                        Color localColor;
                        int x = localThemeCounter % 8;
                        switch (x) {
                        case 0:
                            localColor = new Color(36, 191, 230);
                            break;
                        case 1:
                            localColor = new Color(255, 10, 230);
                            break;
                        case 2:
                            localColor = new Color(38, 135, 0);
                            break;
                        case 3:
                            localColor = new Color(185, 135, 0);
                            break;
                        case 4:
                            localColor = new Color(255, 179, 48);
                            break;
                        case 5:
                            localColor = Color.red;
                            break;
                        case 6:
                            localColor = Color.blue;
                            break;
                        case 7:
                            localColor = Color.green;
                            break;
                        default:
                            localColor = new Color(185, 56, 0);
                        break;
                        }


                        viewer.loadLocalShapefile(file.toURL(), localColor);
                        localThemeCounter++;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                // END if(file.length < xxx)

                else {
                    //info.dispose();
                    String message = "The shapefile\n" +
                    "<html><font color=red>" + file + "</font></html>\n" +
                    "has a filesize of\n" +
                    "<html><font color=red>" + file.length() / 1000000.0 + "</font></html>" +
                    "megabytes\nwhich exceeds the maximum size of 55 megabytes.\n\n" +
                    "Please load a smaller shapefile.";

                    JOptionPane.showMessageDialog(null, (Object) message,
                            "LOCAL SHAPEFILE TOO LARGE", JOptionPane.ERROR_MESSAGE);
                }
                // END else
            }
            // END if (choice == JOptionPane.YES_OPTION)
        }
        // END if (returnVal == JFileChooser.APPROVE_OPTION)

        else {
            System.out.println("Open command cancelled by user.");
        }

    }




    private Color invertColor(Color c) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        int flipRed = 255-red;
        int flipGreen = 255-green;
        int flipBlue = 255-blue;

        int redDiff = Math.abs(red-flipRed);
        int greenDiff = Math.abs(green-flipGreen);
        int blueDiff = Math.abs(blue-flipBlue);

        int threshold = 80;

        if (redDiff < threshold && greenDiff < threshold && blueDiff < threshold) {
            flipRed-=threshold;
            flipGreen-=threshold;
            flipBlue-=threshold;
        }

        return (new Color(flipRed, flipGreen, flipBlue));

    }









    

























    public void isolateRadar() {
        viewer.setGridSatelliteVisibility(false);
        viewer.setRadarGridCoverageVisibility(true);
        viewer.setShowAlphanumericLayers(true);
        if (radLegend.getSelectedItem().toString().equals("None")) {
            viewer.setLegendVisibility(false);
        }
        else if (radLegend.getSelectedItem().toString().equals("Large")) {
            viewer.setLegendVisibility(true);
        }
        jcbRadar.setSelected(true);
        jcbRadar.setEnabled(true);
        radTransparency.setEnabled(true);
        radSmoothing.setEnabled(true);
        radLegend.setEnabled(true);
        viewer.setRangeRingVisibility(true);
        radColorTableCombo.setEnabled(true);
        radEditColorTableLink.setEnabled(true);

        jcbGridSatellite.setSelected(false);
        jcbGridSatellite.setEnabled(false);
        gridSatTransparency.setEnabled(false);
        gridSatSmoothing.setEnabled(false);
        gridSatLegend.setEnabled(false);
        satColorTableCombo.setEnabled(false);
        gridColorTableCombo.setEnabled(false);
        jcomboGridMinMax.setEnabled(false);
        jcbFlipGridColorTable.setEnabled(false);
        jtfGridMaxValue.setEnabled(false);
        jtfGridMinValue.setEnabled(false);

        lastIsolatedType = DataType.RADAR;
    }

    public void isolateGridSatellite(boolean isSatData) {
        viewer.setGridSatelliteVisibility(true);
        viewer.setRadarGridCoverageVisibility(false);
        viewer.setLegendVisibility(false);
        viewer.setShowAlphanumericLayers(false);

        jcbRadar.setSelected(false);
        jcbRadar.setEnabled(false);
        radTransparency.setEnabled(false);
        radSmoothing.setEnabled(false);
        radLegend.setEnabled(false);
        viewer.setRangeRingVisibility(false);
        radColorTableCombo.setEnabled(false);
        radEditColorTableLink.setEnabled(false);

        
        jcbGridSatellite.setSelected(true);
        jcbGridSatellite.setEnabled(true);
        gridSatTransparency.setEnabled(true);
//        satSmoothing.setEnabled(true);
        gridSatLegend.setEnabled(true);
        satColorTableCombo.setEnabled(true);
        gridColorTableCombo.setEnabled(true);
        jcomboGridMinMax.setEnabled(true);
        jcbFlipGridColorTable.setEnabled(true);
        if (jcomboGridMinMax.getSelectedItem().toString().equals("Custom")) {
        	jtfGridMaxValue.setEnabled(true);
        	jtfGridMinValue.setEnabled(true);
        }
        
        if (isSatData) {
            if (gridSatellitePanel.getComponent(gridSatellitePanel.getComponentCount()-1) == satColorTablePanel) {
                System.out.println("found existing sat color table panel ");
                return;
            }
            gridSatellitePanel.remove(gridColorTablePanel);
            gridSatellitePanel.add(satColorTablePanel, "br left");
            gridSatellitePanel.revalidate();
            lastIsolatedType = DataType.SATELLITE;        
        }
        else {
            if (gridSatellitePanel.getComponent(gridSatellitePanel.getComponentCount()-1) == gridColorTablePanel) {
                System.out.println("found existing sat color table panel ");
                return;
            }
            gridSatellitePanel.remove(satColorTablePanel);
            gridSatellitePanel.add(gridColorTablePanel, "br left");
            gridSatellitePanel.revalidate();
            lastIsolatedType = DataType.GRIDDED;
        }
        
    }
    
    public DataType getLastIsolatedDataType() {
        return lastIsolatedType;
    }


    public void setCurrentViewType(CurrentViewType viewType) {
        tabPane.removeAll();
        if (viewType == CurrentViewType.GEOTOOLS) {
            tabPane.add(dataPanel, "Data Layers");
            tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
            tabPane.add(wmsPanel, "Background Maps (WMS)");
        }
        else if (viewType == CurrentViewType.GOOGLE_EARTH) {
            tabPane.add(dataPanel, "Data Layers");
            gePanel = viewer.getGoogleEarthBrowserInternal().createLayerSelectionPanel();
            tabPane.add(gePanel, "Google Earth");
        }
        else if (viewType == CurrentViewType.NCDC_NCS) {
            tabPane.add(dataPanel, "Data Layers");
        }
        else {
            tabPane.add(dataPanel, "Data Layers");
            tabPane.add(new JScrollPane(overlayPanel), "Overlay Selector");
            tabPane.add(wmsPanel, "Background Maps (WMS)");
        }
    }


    public String getSatelliteColorTableName() {
        return satColorTableCombo.getSelectedItem().toString();
    }
    
    public void setSatelliteColorTableName(String colorTableName) {
        satColorTableCombo.setSelectedItem(colorTableName);
    }
    






    private class DataLayersListener implements ActionListener {

//        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Radar")) {
                viewer.setRadarGridCoverageVisibility(jcbRadar.isSelected());
                
                if (viewer.getRadarGridCoverage() == null) {
                    return;                    
                }
                    
                    
                if (jcbRadar.isSelected()) {
                    viewer.getStatusBar().setNexradHeader(viewer.getNexradHeader());
                    viewer.getMapPaneZoomChange().setRadarActive(true);
                    viewer.refreshRadarData();
                    if (radLegend.getSelectedItem().toString().equals("None")) {
                        viewer.setLegendVisibility(false);
                    }
                    else if (radLegend.getSelectedItem().toString().equals("Large")) {
                        viewer.setLegendVisibility(true);
                    }
                }
                else {
                    viewer.getStatusBar().setNexradHeader(null);
                    viewer.getMapPaneZoomChange().setRadarActive(false);
                    viewer.setLegendVisibility(false);
                }
            }
            else if (e.getActionCommand().equals("Satellite")) {
                viewer.setGridSatelliteVisibility(jcbGridSatellite.isSelected());
                if (jcbGridSatellite.isSelected()) {
                    viewer.refreshSatellite();
                }
            }
        }

    }


    private class RadarSmoothingChangeListener implements ChangeListener {
        private int waitTime = 1000;

        TimerTask task = null;

//        @Override
        public void stateChanged(ChangeEvent e) {
            if (task != null) {
                try {
                    task.cancel();
                } catch (Exception ex) {
                }
            }
            task = new TimerTask() {
                public void run() {
                    System.out.println(" EXECUTING SMOOTHING CHANGE EVENT ");
                    viewer.setRadarSmoothFactor(Double.parseDouble(radSmoothing.getValue().toString()));
                    if (jcbRadar.isSelected()) {
                        viewer.refreshRadarData();
                    }
                }
            };
            timer.schedule(task , waitTime);
        }
    }

    private class SatelliteSmoothingChangeListener implements ChangeListener {
        private int waitTime = 1000;

        TimerTask task = null;

//        @Override
        public void stateChanged(ChangeEvent e) {
            if (task != null) {
                try {
                    task.cancel();
                } catch (Exception ex) {
                }
            }
            task = new TimerTask() {
                public void run() {
                    System.out.println(" EXECUTING SMOOTHING CHANGE EVENT ");
                    viewer.setSatelliteSmoothFactor(Double.parseDouble(gridSatSmoothing.getValue().toString()));
                    if (jcbGridSatellite.isSelected()) {
                        viewer.refreshSatellite();
                    }
                }
            };
            timer.schedule(task , waitTime);
        }
    }

    public void setGridColorTableName(String gridColorTableName) {
        gridColorTableCombo.setSelectedItem(gridColorTableName);
    }



    
    


} // END CLASS
