package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.DataExportListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormatType;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
import gov.noaa.ncdc.wct.ui.GridDatasetProperties;
import gov.noaa.ncdc.wct.ui.RadialPropertiesPanel;
import gov.noaa.ncdc.wct.ui.WCTTextDialog;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.filter.GridAttributeFilterPanel;
import gov.noaa.ncdc.wct.ui.filter.Level3AttributeFilterPanel;
import gov.noaa.ncdc.wct.ui.filter.RadialAttributeFilterPanel;
import gov.noaa.ncdc.wct.ui.filter.SpatialFilterPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;
import org.jdesktop.swingx.JXDialog;
import org.xml.sax.SAXException;

import ucar.ma2.Array;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.TypedDatasetFactory;

import com.jidesoft.hints.FileIntelliHints;
import com.jidesoft.swing.SelectAllUtils;

public class WCTExportDialog extends JXDialog {
    
//    public static final String[] EXPORT_FORMATS = new String[] {
//        "Shapefile (Polygon)",
//        "Shapefile (Point Centroid)",
//        "Well-Known Text (Polygon)",
//        "Well-Known Text (Point Centroid)",
//        "ASCII Grid",
//        "Binary Grid",
//        "Gridded NetCDF 3",
//        "Native NetCDF",
//        "Native Format (simple copy)"
//    };
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static final Map<String, ExportFormat> exportFormatMap = new LinkedHashMap<String, ExportFormat>();
    static {
        exportFormatMap.put("Shapefile (Polygon)", ExportFormat.SHAPEFILE);
        exportFormatMap.put("Shapefile (Point Centroid)", ExportFormat.SHAPEFILE);
        exportFormatMap.put("Shapefile", ExportFormat.SHAPEFILE);
        exportFormatMap.put("Well-Known Text (Polygon)", ExportFormat.WKT);
        exportFormatMap.put("Well-Known Text (Point Centroid)", ExportFormat.WKT);
        exportFormatMap.put("Well-Known Text", ExportFormat.WKT);
        exportFormatMap.put("ESRI ASCII Grid", ExportFormat.ARCINFOASCII);
        exportFormatMap.put("ESRI Binary Grid", ExportFormat.ARCINFOBINARY);
        exportFormatMap.put("GeoTIFF (8-bit Grayscale)", ExportFormat.GEOTIFF_GRAYSCALE_8BIT);
        exportFormatMap.put("GeoTIFF (32-bit Real Values)", ExportFormat.GEOTIFF_32BIT);
        exportFormatMap.put("Gridded NetCDF 3", ExportFormat.GRIDDED_NETCDF);
        exportFormatMap.put("Native NetCDF", ExportFormat.RAW_NETCDF);
        exportFormatMap.put("Native Format (simple copy)", ExportFormat.NATIVE);
    }

    
    private JPanel mainPanel = new JPanel();
    private JPanel statusPanel = new JPanel();
    private JPanel view0Panel = new JPanel();
    private JPanel view1Panel = new JPanel();
    private JPanel view2Panel = new JPanel();
    private JPanel view3Panel = new JPanel();
    private JPanel view4Panel = new JPanel();
    private JPanel view5Panel = new JPanel();
    private JPanel view6Panel = new JPanel();
    private JPanel view7Panel = new JPanel();
    private CardLayout cardLayout = new CardLayout();
    
    private JLabel[] statusLabels = new JLabel[] { 
            new JLabel("1) Select Format/Output"),
            new JLabel("2) Select Variables"),
            new JLabel("3) Select Spatial Extent"),
            new JLabel("4) Select Value Filter"),
            new JLabel("5) Export Format Options"),
            new JLabel("6) Review"),
            new JLabel("7) Processing Progress"),
            new JLabel("8) Summary")
    };

    private final JProgressBar exportProgressBar = new JProgressBar();
    
    private JLabel jlFileSummary = new JLabel();
    private final JTextField jtfOutputDirectory = new JTextField(25);
    private SpatialFilterPanel spatialFilterPanel = new SpatialFilterPanel();
    private RadialAttributeFilterPanel radialAttributeFilterPanel = new RadialAttributeFilterPanel();
    private Level3AttributeFilterPanel level3AttributeFilterPanel = new Level3AttributeFilterPanel();
    private GridAttributeFilterPanel gridAttributeFilterPanel = new GridAttributeFilterPanel();
    private JLabel jlExportTime = new JLabel("");
    private JLabel jlSelectedExportFormat = new JLabel("");
    private RadialPropertiesPanel radialPropsPanel;
    private GridDatasetProperties gridProps;
    private RasterExportOptionsPanel rasterOptionsPanel = new RasterExportOptionsPanel();
    private JButton nextButton1 = new JButton("Next");
    private JLabel jlGridName = new JLabel();
    private JLabel jlGridZ = new JLabel();
    private JLabel jlGridTime = new JLabel();
    private JLabel jlGridRuntime = new JLabel();
    
    private WCTViewer viewer = null;
    private WCTExport exporter;
    private SupportedDataType dataType;
    private RadialDatasetSweep firstRadialDataset;
    
    
    private final FileScanner fileScanner = new FileScanner();
    
    
    // Special for Level-II file pre-export scan
//    private DecodeRadialDatasetSweepHeader radialDatasetHeader;
//    private DecodeL3Header level3Header;

    
    private URL[] dataUrls = null;
    private boolean fileListLocked = false;
    private boolean cancelExport = false;
    private long exportStartTime;
    private boolean isAttributeFilterUsed = false;

    
    private ExportFormat selectedExportFormat = exportFormatMap.get(exportFormatMap.keySet().toArray()[0]);
    private String selectedExportFormatString = exportFormatMap.keySet().toArray()[0].toString();
    private ExportFormatType selectedExportFormatType = WCTExport.getExportFormatType(selectedExportFormat);
    
    private int pageIndex = 0;
    
    private final JLabel outLabel = new JLabel(" ");
    
    private final WCTTextDialog errorLogDialog = new WCTTextDialog(this, "", "Export Error Log: ", false);
    
    private boolean isLatLon = false;
    private boolean isRegularSpatial = false;
    
    
    
    private final ZoomChangeListener zoomListener = new ZoomChangeListener() {
        @Override
        public void zoomChanged(ZoomChangeEvent event) {
            if (spatialFilterPanel.isSpatialFilterLockedToViewer()) {
                updateSpatialExtent(viewer.getCurrentExtent());
            }
        }
    };

    
    public WCTExportDialog(String title, WCTViewer viewer) 
        throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException,
            DecodeException, SQLException, ParseException, Exception {
        
//        super(viewer, title, ModalityType.APPLICATION_MODAL);
        super(viewer, new JPanel());
        setTitle(title);
        setModal(true);
        
        this.viewer = viewer;
        
        init();
        createUI();
    }
    
    private void init() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, 
        DecodeException, SQLException, ParseException, ClassNotFoundException {
        
        exportProgressBar.setStringPainted(true);
        
        readSelectedFiles();
        
        this.exporter = new WCTExport();
        
        
        viewer.getMapPane().addZoomChangeListener(zoomListener);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                finish();
            }
        });
        
    }
    
    private void readSelectedFiles() 
        throws IOException, NumberFormatException, XPathExpressionException, 
        SAXException, ParserConfigurationException, DecodeException, SQLException, ParseException, ClassNotFoundException {
        
        this.dataUrls = viewer.getDataSelector().getSelectedURLs();
        if (dataUrls.length == 0) {
//            throw new IOException("No data files selected.");
            jlFileSummary.setText("No data files selected");
            nextButton1.setEnabled(false);
            return;
        }
        else {
            nextButton1.setEnabled(true);
        }
        
        fileScanner.scanURL(dataUrls[0]);        
//        WCTDataUtils.scan(dataUrls[0], fileScanner, true, true, viewer.getDataSelector().getSelectedDataType());

        
        
        // default all unknowns to GRIDDED type
        if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {                
            fileScanner.getLastScanResult().setDataType(SupportedDataType.GRIDDED);
        }

        // see if a custom data type override has been selected
        if (viewer.getDataSelector().getSelectedDataType() != SupportedDataType.UNKNOWN) {
        	dataType = viewer.getDataSelector().getSelectedDataType();
        }
        else {
        	dataType = fileScanner.getLastScanResult().getDataType();
        	for (URL url : dataUrls) {
        		fileScanner.scanURL(url);
        		// default all unknowns to GRIDDED type
        		if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {                
        			fileScanner.getLastScanResult().setDataType(SupportedDataType.GRIDDED);
        		}
        		if (! fileScanner.getLastScanResult().getDataType().equals(dataType)) {
        			throw new IOException("All files in multiple file selections must be the same data type.  " +
        					"\nFirst data type found: "+dataType+
        					"\nProblem data type: "+fileScanner.getLastScanResult().getDataType()+
        					"\nProblem url: "+url);
        		}
        	}
        }

        jlFileSummary.setText("Number of files selected: " + dataUrls.length);

    }
    
    private void createUI() throws IOException, Exception {
    	errorLogDialog.setSize(500, 300);
    	
        mainPanel.setLayout(cardLayout);
        
        createStatusPanel();
        createView0Panel();
        createView1Panel();
        createView2Panel();
        createView3Panel();
        createView4Panel();
        createView5Panel();
        createView6Panel();
        createView7Panel();
        
        mainPanel.add(view0Panel, "0");
        mainPanel.add(view1Panel, "1");
        mainPanel.add(view2Panel, "2");
        mainPanel.add(view3Panel, "3");
        mainPanel.add(view4Panel, "4");
        mainPanel.add(view5Panel, "5");
        mainPanel.add(view6Panel, "6");
        mainPanel.add(view7Panel, "7");
        
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(statusPanel, BorderLayout.WEST);
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        
//        JRootPane rootPane = this.getRootPane();
//        InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
//
//        ActionMap aMap = rootPane.getActionMap();
//        aMap.put("escape", new AbstractAction() {
//                public void actionPerformed(ActionEvent e) {
//                    finish();
//                }
//            });
    }
    
    
    
    private void createStatusPanel() {
        statusPanel.setLayout(new RiverLayout());
        
        statusLabels[0].setFont(statusLabels[0].getFont().deriveFont(Font.BOLD));
        statusPanel.add(statusLabels[0], "p left");
        statusPanel.add(statusLabels[1], "p left");
        statusPanel.add(statusLabels[2], "p left");
        statusPanel.add(statusLabels[3], "p left");
        statusPanel.add(statusLabels[4], "p left");
        statusPanel.add(statusLabels[5], "p left");
        statusPanel.add(statusLabels[6], "p left");
        statusPanel.add(statusLabels[7], "p left");
        
        statusPanel.setBorder(WCTUiUtils.myTitledBorder("Progress", 10, 10, 10, 10));
        
        statusPanel.setPreferredSize(new Dimension(200, (int)statusPanel.getPreferredSize().getHeight()));
        
    }
    
    private void processFormatOptions() {
        if (selectedExportFormat == ExportFormat.RAW_NETCDF || selectedExportFormat == ExportFormat.NATIVE) {
            statusLabels[1].setEnabled(false);
            statusLabels[2].setEnabled(false);
            statusLabels[3].setEnabled(false);
            statusLabels[4].setEnabled(false);
        }
        else {
            statusLabels[1].setEnabled(true);
            statusLabels[2].setEnabled(true);
            statusLabels[3].setEnabled(true);
            statusLabels[4].setEnabled(true);
        }
    }
    
    private void processFormatSelection(String selectedExportFormatString) throws WCTException {
        this.selectedExportFormatString = selectedExportFormatString;
        selectedExportFormat = exportFormatMap.get(selectedExportFormatString);
        if (selectedExportFormat == null) {
        	throw new WCTException("No output format found in lookup hashmap for this format: '"+
        			selectedExportFormatString+"'");
        }
        selectedExportFormatType = WCTExport.getExportFormatType(selectedExportFormat);
        jlSelectedExportFormat.setText(selectedExportFormatString);
        if (selectedExportFormatString.equals("UNKNOWN")) {
            jlSelectedExportFormat.setText("GRIDDED (?)");
        }
    }
    
    private void createView0Panel() {
        view0Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        
//        String varSummaryString = "Number of variables selected: " + );
        
        final JComboBox exportFormat = new JComboBox(exportFormatMap.keySet().toArray());
        String exportTypeProp = WCTProperties.getWCTProperty("export_type");
        if (exportTypeProp != null) {
            exportFormat.setSelectedItem(exportTypeProp);
            
            try {
				processFormatSelection(exportFormat.getSelectedItem().toString());
			} catch (WCTException e1) {
				e1.printStackTrace();
			}
        }

        exportFormat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
					processFormatSelection(exportFormat.getSelectedItem().toString());
				} catch (WCTException e2) {
					System.out.println("OUTPUT FORMAT SET AS: type="+selectedExportFormatType+" -- " +
							exportFormatMap.get(selectedExportFormatString)+" from "+selectedExportFormatString);
	                e2.printStackTrace();
				}
                try {
                    refreshView4Panel();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                processFormatOptions();
                
                WCTProperties.setWCTProperty("export_type", exportFormat.getSelectedItem().toString());
            }
        });
        processFormatRemover(exportFormat);

        JButton browseButton = new JButton(" Browse ");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String dir = handleBrowseEvent().toString();
                jtfOutputDirectory.setText(dir);
                WCTProperties.setWCTProperty("jne_export_dir", dir);
            }
        });

        String wctprop = WCTProperties.getWCTProperty("jne_export_dir");
        if (wctprop != null) {
            jtfOutputDirectory.setText(wctprop);
        }

        // JIDE stuff
        jtfOutputDirectory.setName("File IntelliHint");
        SelectAllUtils.install(jtfOutputDirectory);
        new FileIntelliHints(jtfOutputDirectory).setFolderOnly(true);

        
        
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, (int)cancelButton.getPreferredSize().getHeight()));
        cancelButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });

        final Component finalThis = this;
        nextButton1.setPreferredSize(new Dimension(100, (int)nextButton1.getPreferredSize().getHeight()));
        nextButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File outputDir = new File(jtfOutputDirectory.getText());
                try {
                    if (! outputDir.exists() || ! outputDir.isDirectory()) {
                        outputDir.mkdirs();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(finalThis, "Error creating new output directory: "+ex.getMessage(), 
                            "Data Export Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (jtfOutputDirectory.getText().trim().length() == 0 || ! outputDir.exists()) {
                    JOptionPane.showMessageDialog(finalThis, "Please enter a valid output directory.", "Data Export Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                WCTProperties.setWCTProperty("jne_export_dir", jtfOutputDirectory.getText());
                
//                statusLabels[0].setFont(statusLabels[0].getFont().deriveFont(Font.PLAIN));
                fileListLocked = true;
                try {
                    refreshView1Panel();
                    refreshView5Panel();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
//                if (selectedExportFormat == ExportFormat.RAW_NETCDF || selectedExportFormat == ExportFormat.NATIVE) {
//                    statusLabels[5].setFont(statusLabels[5].getFont().deriveFont(Font.BOLD));
//                    cardLayout.show(mainPanel, "6");
//                }
//                else {
//                    statusLabels[1].setFont(statusLabels[1].getFont().deriveFont(Font.BOLD));
//                    cardLayout.next(mainPanel);
//                }
                nextPage();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);
        buttonPanel.add(nextButton1);
        
        panel.add(jlFileSummary, "p left");
        panel.add(new JLabel("Select Output Format: "), "p left");
        panel.add(exportFormat, "br left hfill");
        panel.add(new JLabel("Select Output Directory: "), "p left");
        panel.add(browseButton, "br left");
        panel.add(jtfOutputDirectory, "hfill");
        
        view0Panel.setBorder(WCTUiUtils.myTitledBorder("Select Output Format and Location", 10, 10, 4, 10));

        view0Panel.add(panel, BorderLayout.CENTER);
        view0Panel.add(buttonPanel, BorderLayout.SOUTH);

        
        
        System.out.println(Arrays.toString( viewer.getDataSelector().getSelectedURLs() ));
        if (viewer.getGridProps() != null) {
            System.out.println(Arrays.toString( new int[] { viewer.getGridProps().getSelectedGridIndex() } ));
        }
        if (viewer.getRadialProps() != null) {
            System.out.println(viewer.getRadialProps().getRadialPropsPanel().getVariableName() + " , " + viewer.getRadialProps().getRadialPropsPanel().getCut() );
        }
        

        processFormatOptions();
    }

    private void processFormatRemover(JComboBox exportFormat) {


        if (dataType == SupportedDataType.RADIAL) {
            exportFormat.removeItem("Shapefile");
            exportFormat.removeItem("Well-Known Text");
        }
        else if (dataType == SupportedDataType.GOES_SATELLITE_AREA_FORMAT ||
                dataType == SupportedDataType.GRIDDED ||
                dataType == SupportedDataType.UNKNOWN) {
            exportFormat.removeItem("Shapefile (Polygon)");
            exportFormat.removeItem("Well-Known Text (Polygon)");
            exportFormat.removeItem("Shapefile");
            exportFormat.removeItem("Well-Known Text");
        }
        else {
            exportFormat.removeItem("Shapefile (Point Centroid)");
            exportFormat.removeItem("Well-Known Text (Point Centroid)");
            exportFormat.removeItem("Shapefile (Polygon)");
            exportFormat.removeItem("Well-Known Text (Polygon)");
        }
    }

    private void nextPage() {
        for (int n=pageIndex; n<statusLabels.length; n++) {
            if (n < statusLabels.length && statusLabels[n+1] != null && statusLabels[n+1].isEnabled()) {
                cardLayout.show(mainPanel, String.valueOf(n+1));
                statusLabels[pageIndex].setFont(statusLabels[pageIndex].getFont().deriveFont(Font.PLAIN));
                statusLabels[n+1].setFont(statusLabels[n+1].getFont().deriveFont(Font.BOLD));
                pageIndex = n+1;
                break;
            }
        }
    }

    private void previousPage() {
        for (int n=pageIndex; n>=0; n--) {
            if (n > 0 && statusLabels[n-1] != null && statusLabels[n-1].isEnabled()) {
                cardLayout.show(mainPanel, String.valueOf(n-1));
                statusLabels[pageIndex].setFont(statusLabels[pageIndex].getFont().deriveFont(Font.PLAIN));
                statusLabels[n-1].setFont(statusLabels[n-1].getFont().deriveFont(Font.BOLD));
                pageIndex = n-1;
                break;
            }
        }
    }
    
    
    private File handleBrowseEvent() {
        // Set up File Chooser
        //lastFolder = new File(System.getProperty("user.home"));
        String wctprop = WCTProperties.getWCTProperty("jne_export_dir");        
        File lastFolder;
        if (wctprop != null) {
            lastFolder = new File(wctprop);
        }
        else {
            lastFolder = new File(System.getProperty("user.home"));
        }
        JFileChooser fc = new JFileChooser(lastFolder);
        fc.setAcceptAllFileFilterUsed(false);
        // fake file filter that only shows directories
        fc.addChoosableFileFilter(new OpenFileFilter("987151klj241", true, "Directories Only"));
        //fc.addChoosableFileFilter(new NexradFileFilter(NexradFileFilter.SHOW_LEVEL2, true, "Nexrad Level-II"));
        //fc.addChoosableFileFilter(new NexradFileFilter(NexradFileFilter.SHOW_LEVEL3, true, "Nexrad Level-III"));
        //fc.addChoosableFileFilter(new NexradFileFilter(NexradFileFilter.SHOW_BOTH, true, "All Nexrad"));
        //fc.setSelectedFile(lastFolder);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choose Directory of Data Files");
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //lastFolder = fc.getSelectedFile().getParentFile();
            lastFolder = fc.getSelectedFile();
            return lastFolder;
        }
        else {
            return null;
        }

    }
    

    
    private void createView1Panel() throws IOException, Exception {
        
        view1Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        JButton prevButton = new JButton("Back");
        prevButton.setPreferredSize(new Dimension(100, (int)prevButton.getPreferredSize().getHeight()));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileListLocked = false;
//                statusLabels[1].setFont(statusLabels[1].getFont().deriveFont(Font.PLAIN));
//                statusLabels[0].setFont(statusLabels[0].getFont().deriveFont(Font.BOLD));
//                cardLayout.previous(mainPanel);
                previousPage();
            }
        });
        
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[1].setFont(statusLabels[1].getFont().deriveFont(Font.PLAIN));
//                statusLabels[2].setFont(statusLabels[2].getFont().deriveFont(Font.BOLD));
//                cardLayout.next(mainPanel);
                nextPage();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

    
        view1Panel.setBorder(WCTUiUtils.myTitledBorder("Variable Selection", 0, 10, 4, 10));

        // cache first dataset
//        URL url = WCTDataUtils.scan(dataUrls[0], fileScanner, true, true, viewer.getDataSelector().getSelectedDataType(), viewer);
        
        URL url = (URL) foxtrot.Worker.post(new foxtrot.Job() {
            public Object run() {
                try {
                    return WCTDataUtils.scan(dataUrls[0], fileScanner, true, true, viewer.getDataSelector().getSelectedDataType(), viewer);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        
//        gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
//            public Object construct() {
//                try {
//                    return WCTDataUtils.scan(dataUrls[0], fileScanner, true, true, viewer.getDataSelector().getSelectedDataType(), viewer);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        };
//        worker.start();                  
//        if (true) return;
//        URL url = WCTDataUtils.scan(dataUrls[0], fileScanner, true, true, viewer.getDataSelector().getSelectedDataType(), viewer);
        
        
        
        if (url == null) {
            JOptionPane.showMessageDialog(viewer, "Critical Error Scanning First Data File", "Error", JOptionPane.ERROR_MESSAGE);
        }
                
        
                
        if (dataType == SupportedDataType.RADIAL) {

            if (firstRadialDataset != null) {
                firstRadialDataset.close();
            }
            firstRadialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
                    FeatureType.RADIAL, 
                    url.toString(), WCTUtils.getSharedCancelTask(), new StringBuilder());

            
            boolean showAllPointsOption = false;
            if ((selectedExportFormat == ExportFormat.SHAPEFILE ||
            		selectedExportFormat == ExportFormat.WKT ||
            		selectedExportFormat == ExportFormat.CSV) && 
            		selectedExportFormatString.contains("(Point Centroid)") ) {
            	showAllPointsOption = true;
            }
            
            radialPropsPanel = new RadialPropertiesPanel(firstRadialDataset, 3, true, showAllPointsOption);            
            panel.add(radialPropsPanel, "center");
            
        }
        else if (dataType != SupportedDataType.GRIDDED && dataType != SupportedDataType.RADIAL) {
            panel.add(new JLabel(
                    "<html><center>This data type has a single variable.<br>" +
                    "Please press 'Next'.</center></html>"
               ), "center");
        }
        else {

//            if (viewer.getGridProps() == null) {
                gridProps = new GridDatasetProperties(viewer, ModalityType.APPLICATION_MODAL);
                gridProps.setViewer(viewer);
                gridProps.setMultipleSelectionZ(false);
                gridProps.setMultipleSelectionRuntime(false);
                gridProps.setMultipleSelectionTime(false);
//            }
//            else {
//                gridProps = viewer.getGridProps();
//            }
            gridProps.setGridDatasetURL(url);

            if (viewer.getGridProps() != null) {
                gridProps.setSelectedGridIndex(viewer.getGridProps().getSelectedGridIndex());
                gridProps.setSelectedZIndex(viewer.getGridProps().getSelectedZIndex());
                gridProps.setSelectedRuntimeIndex(viewer.getGridProps().getSelectedRuntimeIndex());
                gridProps.setSelectedTimeIndex(viewer.getGridProps().getSelectedTimeIndex());
            }
            
            final Component finalThis = this;
            JButton selectDims = new JButton("Select Grid Slice");
            selectDims.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gridProps.setModalityType(ModalityType.APPLICATION_MODAL);
                    gridProps.setLocationRelativeTo(finalThis);
                    gridProps.setVisible(true);
                    updateGridSelectionLabels();
                    
                }
            });

            updateGridSelectionLabels();

            panel.add(new JLabel("Variable: "), "p");
            panel.add(jlGridName, "br");
//            if (jlGridZ.getText().trim().length() != 0) {
                panel.add(new JLabel("Height: "), "p");
                panel.add(jlGridZ, "br");
//            }
//            if (jlGridTime.getText().trim().length() != 0) {
                panel.add(new JLabel("Time: "), "p");
                panel.add(jlGridTime, "br");
//            }
//            if (jlGridRuntime.getText().trim().length() != 0) {
                panel.add(new JLabel("Runtime: "), "p");
                panel.add(jlGridRuntime, "br");
//            }
            panel.add(selectDims, "p");
//            

        }
        
        view1Panel.add(panel, BorderLayout.CENTER);
        view1Panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    
    
    private void updateGridSelectionLabels() {
        GridDatatype grid = gridProps.getGrids().get(gridProps.getSelectedGridIndex());
        String timeString = null;
        if (gridProps.getSelectedTimeIndices().length > 0 && gridProps.getSelectedGridIndex() != -1) {
            timeString = sdf.format( grid.getCoordinateSystem().getTimeAxis1D().getTimeDate(gridProps.getSelectedTimeIndices()[0]) );
            if (gridProps.getSelectedTimeIndices().length > 1) {
                timeString += " ... " + sdf.format( grid.getCoordinateSystem().getTimeAxis1D().getTimeDate(
                        gridProps.getSelectedTimeIndices()[gridProps.getSelectedTimeIndices().length-1]) 
                    );
            }
        }
        String zString = null;
        if (gridProps.getSelectedZIndices().length > 0 && gridProps.getSelectedZIndex() != -1) {
            zString = grid.getCoordinateSystem().getVerticalAxis().getCoordValue(gridProps.getSelectedZIndices()[0]) + 
                " " +grid.getCoordinateSystem().getVerticalAxis().getUnitsString();
            if (gridProps.getSelectedZIndices().length > 1) {
                zString += " ... " + grid.getCoordinateSystem().getVerticalAxis().getCoordValue(
                        gridProps.getSelectedZIndices()[gridProps.getSelectedZIndices().length-1]) + 
                        " " +grid.getCoordinateSystem().getVerticalAxis().getUnitsString();
            } 
        }
        String runtimeString = null;
        if (gridProps.getSelectedRunTimeIndices().length > 0 && gridProps.getSelectedRuntimeIndex() != -1) {
            runtimeString = sdf.format( grid.getCoordinateSystem().getRunTimeAxis().getTimeDate(gridProps.getSelectedRunTimeIndices()[0]) );
            if (gridProps.getSelectedRunTimeIndices().length > 1) {
                runtimeString += " ... " + sdf.format( grid.getCoordinateSystem().getRunTimeAxis().getTimeDate(
                        gridProps.getSelectedRunTimeIndices()[gridProps.getSelectedRunTimeIndices().length-1]) 
                    );
            }
        }
        
        jlGridName.setText("<html>"+grid.getName()+" ("+grid.getUnitsString()+"): <br> "+grid.getDescription()+"</html>");
        jlGridZ.setText( (zString != null) ? zString : "N/A");
        jlGridTime.setText( (timeString != null) ? timeString : "N/A");
        jlGridRuntime.setText( (runtimeString != null) ? runtimeString : "N/A");
        
    }

    private void refreshView1Panel() throws IOException, Exception {
        view1Panel.removeAll();
        createView1Panel();
    }


    
    private void createView2Panel() {
        view2Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        JButton prevButton = new JButton("Back");
        prevButton.setPreferredSize(new Dimension(100, (int)prevButton.getPreferredSize().getHeight()));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileListLocked = false;
//                statusLabels[2].setFont(statusLabels[2].getFont().deriveFont(Font.PLAIN));
//                statusLabels[1].setFont(statusLabels[1].getFont().deriveFont(Font.BOLD));
//                cardLayout.previous(mainPanel);
                previousPage();
            }
        });
        
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[2].setFont(statusLabels[2].getFont().deriveFont(Font.PLAIN));
//                statusLabels[3].setFont(statusLabels[3].getFont().deriveFont(Font.BOLD));
                rasterOptionsPanel.updateFromSpatialExtent(spatialFilterPanel.getSpatialExtent());
//                cardLayout.next(mainPanel);
                
                 
                if (! spatialFilterPanel.isSpatialFilterLockedToViewer()) {
                	Rectangle2D.Double extent = spatialFilterPanel.getSpatialExtent();
                    String xywh = extent.getX()+","+extent.getY()+","+extent.getWidth()+","+extent.getHeight();
                    WCTProperties.setWCTProperty("manualExportExtent", xywh);                          
                }
                WCTProperties.setWCTProperty("isSpatialFilterLockedToViewer", 
                		String.valueOf(spatialFilterPanel.isSpatialFilterLockedToViewer()));
                
                nextPage();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);


        
        
        String manualExportExtent = WCTProperties.getWCTProperty("manualExportExtent"); 
        if (manualExportExtent != null) {
        	String[] xywh = manualExportExtent.split(",");
        	spatialFilterPanel.setManualSpatialFilterExtent(new Rectangle2D.Double(
        			Double.parseDouble(xywh[0]),
        			Double.parseDouble(xywh[1]),
        			Double.parseDouble(xywh[2]),
        			Double.parseDouble(xywh[3])
        		));
        }

        String isSpatialFilterLockedToViewer = WCTProperties.getWCTProperty("isSpatialFilterLockedToViewer");
        if (isSpatialFilterLockedToViewer != null) {
        	try {
        		spatialFilterPanel.setSpatialFilterLockedToViewer(Boolean.parseBoolean(isSpatialFilterLockedToViewer));
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        

        
        spatialFilterPanel.setSpatialFilterExtent(viewer.getCurrentExtent());
        spatialFilterPanel.setSpatialFilterEngaged(true);
        
        view2Panel.setBorder(WCTUiUtils.myTitledBorder("Spatial Extent Filter (Deci. Degrees)", 10, 10, 4, 10));

        panel.add(spatialFilterPanel, "hfill vfill");
        
        
        view2Panel.add(panel, BorderLayout.CENTER);
        view2Panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    
    
    
    

    private void createView3Panel() {
        view3Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());
        
        JButton prevButton = new JButton("Back");
        prevButton.setPreferredSize(new Dimension(100, (int)prevButton.getPreferredSize().getHeight()));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[3].setFont(statusLabels[3].getFont().deriveFont(Font.PLAIN));
//                statusLabels[2].setFont(statusLabels[2].getFont().deriveFont(Font.BOLD));
//                cardLayout.previous(mainPanel);
                previousPage();
            }
        });
        
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[3].setFont(statusLabels[3].getFont().deriveFont(Font.PLAIN));
//                statusLabels[4].setFont(statusLabels[4].getFont().deriveFont(Font.BOLD));
//                cardLayout.next(mainPanel);
                nextPage();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

    
        view3Panel.setBorder(WCTUiUtils.myTitledBorder("Attribute Filter", 10, 10, 4, 10));
        
        radialAttributeFilterPanel.setRadialMinValues(viewer.getFilterGUI().getRadialFilter().getMinValue());
        radialAttributeFilterPanel.setRadialMaxValues(viewer.getFilterGUI().getRadialFilter().getMaxValue());
        level3AttributeFilterPanel.setLevel3MinValues(viewer.getFilterGUI().getLevel3Filter().getMinValue());
        level3AttributeFilterPanel.setLevel3MaxValues(viewer.getFilterGUI().getLevel3Filter().getMaxValue());
        gridAttributeFilterPanel.setGridMinValues(viewer.getFilterGUI().getGridFilter().getMinValue());
        gridAttributeFilterPanel.setGridMaxValues(viewer.getFilterGUI().getGridFilter().getMaxValue());
        
        if (dataType == SupportedDataType.RADIAL) {
            panel.add("center", radialAttributeFilterPanel);
        }
        else if (dataType == SupportedDataType.NEXRAD_LEVEL3 || dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            panel.add("center", level3AttributeFilterPanel);
        }
        else {
            panel.add("center", gridAttributeFilterPanel);
        }
        
        view3Panel.add(panel, BorderLayout.CENTER);
        view3Panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    


    private void createView4Panel() {
        view4Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());
    
        view4Panel.setBorder(WCTUiUtils.myTitledBorder("Export Format Options", 10, 10, 4, 10));
        
        if (selectedExportFormatType == ExportFormatType.VECTOR) {
            panel.add("center", new JLabel("No "+selectedExportFormatString+" Export Options Available."));
            panel.add("center p", new JLabel("Please press 'Next'."));
        }
        else if (selectedExportFormatType == ExportFormatType.RASTER) {
            
            if (dataType == SupportedDataType.GRIDDED) {
                isLatLon = gridProps.getGrids().get(gridProps.getSelectedGridIndex()).getCoordinateSystem().isLatLon();
                isRegularSpatial = gridProps.getGrids().get(gridProps.getSelectedGridIndex()).getCoordinateSystem().isRegularSpatial();
            }

            panel.add("center", new JLabel("Raster Grid Options"));
//            panel.add("left br br", new JLabel(
//                    "lat/lon: "+ isLatLon
//              ));
//            panel.add("left br ", new JLabel(
//                    "isRegular: "+ isRegularSpatial
//              ));
            
            if (isLatLon && isRegularSpatial) {
                try {
                    Array yAxisArray = gridProps.getGrids().get(gridProps.getSelectedGridIndex()).
                    getCoordinateSystem().getYHorizAxis().read("0:1");
                    double yCellSize = Math.abs(yAxisArray.getDouble(0) - yAxisArray.getDouble(1)); 

                    panel.add("left br", new JLabel("Equally spaced Lat/Lon dataset"));
                    if (yCellSize > 10000 || yCellSize < 0.001) {
                        panel.add("left br", new JLabel("Cell Size: "+ WCTUtils.DECFMT_SCI.format(yCellSize) + " Degrees"));
                    }
                    else {
                        panel.add("left br", new JLabel("Cell Size: "+ WCTUtils.DECFMT_0D0000.format(yCellSize) + " Degrees"));                        
                    }
                } catch (Exception e) {
                    panel.add("left br", new JLabel("Cell Size: Error Reading File"));              
                }
            }
            else {
                rasterOptionsPanel.setShowSmoothingOptions( 
                        dataType == SupportedDataType.NEXRAD_LEVEL3 ||
                        dataType == SupportedDataType.NEXRAD_LEVEL3_NWS || 
                        dataType == SupportedDataType.RADIAL );
                panel.add("center br", rasterOptionsPanel);
            }
            
            
            rasterOptionsPanel.setShowNoDataOptions(
            		selectedExportFormat == ExportFormat.ARCINFOASCII ||
            		selectedExportFormat == ExportFormat.ARCINFOBINARY ||
            		selectedExportFormat == ExportFormat.CSV );
            
            rasterOptionsPanel.setShowNoDataFormatOptions(
            		selectedExportFormat == ExportFormat.ARCINFOASCII ||
            		selectedExportFormat == ExportFormat.CSV );
            
        }
        else {
            panel.add("center br", new JLabel("no export options"));
        }

        

        
        
        
        JButton prevButton = new JButton("Back");
        prevButton.setPreferredSize(new Dimension(100, (int)prevButton.getPreferredSize().getHeight()));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[4].setFont(statusLabels[4].getFont().deriveFont(Font.PLAIN));
//                statusLabels[3].setFont(statusLabels[3].getFont().deriveFont(Font.BOLD));
//                cardLayout.previous(mainPanel);
                previousPage();
            }
        });
        
        final Component finalThis = this;
        JButton nextButton = new JButton("Next");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[4].setFont(statusLabels[4].getFont().deriveFont(Font.PLAIN));
//                statusLabels[5].setFont(statusLabels[5].getFont().deriveFont(Font.BOLD));
                try {
                    processExportGridOptions();
//                    cardLayout.next(mainPanel);
                    
                    try {
                        refreshView5Panel();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    
                    nextPage();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(finalThis, ex.getMessage(), "Raster Grid Export Options", JOptionPane.ERROR_MESSAGE);
                }
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        view4Panel.add(panel, BorderLayout.CENTER);
        view4Panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshView4Panel() throws IOException, Exception {
        view4Panel.removeAll();
        createView4Panel();
    }

    private void processExportGridOptions() throws Exception {
        
        if (rasterOptionsPanel.getGridCellSizeValue() == RasterExportOptionsPanel.AUTO_GRID_CELL_SIZE_VALUE &&
                rasterOptionsPanel.getGridDimension() == RasterExportOptionsPanel.AUTO_GRID_DIMENSION_VALUE) {
            throw new Exception("Both the cell size and grid size cannot be set to auto!");
        }
        else if (rasterOptionsPanel.getGridCellSizeValue() == RasterExportOptionsPanel.AUTO_GRID_CELL_SIZE_VALUE) {
            exporter.setExportGridCellSize(-1);
            exporter.setExportGridSize(rasterOptionsPanel.getGridDimension());
        }
        else if (rasterOptionsPanel.getGridDimension() == RasterExportOptionsPanel.AUTO_GRID_DIMENSION_VALUE) {
            exporter.setExportGridCellSize(rasterOptionsPanel.getGridCellSizeValue());
        }
        else {
            throw new Exception("Either the cell size and grid size must be set to auto!");
        }
        exporter.setExportGridNoData(rasterOptionsPanel.getNoDataValue());
        exporter.setExportGridSmoothFactor(rasterOptionsPanel.getSmoothingFactor());
        exporter.setExportAsciiFormat(rasterOptionsPanel.getNoDataFormat());
    }
    

    
    private void createView5Panel() {
        view5Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        view5Panel.setBorder(WCTUiUtils.myTitledBorder("Review", 10, 10, 4, 10));
        
        
        JButton prevButton = new JButton("Back");
        prevButton.setPreferredSize(new Dimension(100, (int)prevButton.getPreferredSize().getHeight()));
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[5].setFont(statusLabels[5].getFont().deriveFont(Font.PLAIN));
//                if (selectedExportFormat == ExportFormat.RAW_NETCDF || selectedExportFormat == ExportFormat.NATIVE) {
//                    statusLabels[0].setFont(statusLabels[0].getFont().deriveFont(Font.BOLD));
//                    cardLayout.show(mainPanel, "1");
//                }
//                else {
//                    statusLabels[4].setFont(statusLabels[4].getFont().deriveFont(Font.BOLD));
//                    cardLayout.previous(mainPanel);
//                }
                previousPage();
            }
        });
        
        JButton nextButton = new JButton("Start Export");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                statusLabels[5].setFont(statusLabels[5].getFont().deriveFont(Font.PLAIN));
//                statusLabels[6].setFont(statusLabels[6].getFont().deriveFont(Font.BOLD));
//                cardLayout.next(mainPanel);
                nextPage();
                setModalityType(ModalityType.MODELESS);
                doExportInBackground();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        

        if (dataType == SupportedDataType.RADIAL) {
            isAttributeFilterUsed = 
                radialAttributeFilterPanel.getRadialMinAzimuth() != WCTFilter.NO_MIN_AZIMUTH ||
                radialAttributeFilterPanel.getRadialMaxAzimuth() != WCTFilter.NO_MAX_AZIMUTH ||
                radialAttributeFilterPanel.getRadialMinDistance() != WCTFilter.NO_MIN_DISTANCE ||
                radialAttributeFilterPanel.getRadialMaxDistance() != WCTFilter.NO_MAX_DISTANCE ||
                radialAttributeFilterPanel.getRadialMinHeight() != WCTFilter.NO_MIN_HEIGHT ||
                radialAttributeFilterPanel.getRadialMaxHeight() != WCTFilter.NO_MAX_HEIGHT ||
                radialAttributeFilterPanel.getRadialMinValues()[0] != WCTFilter.NO_MIN_VALUE ||
                radialAttributeFilterPanel.getRadialMaxValues()[0] != WCTFilter.NO_MAX_VALUE ;
        }
        else if (dataType == SupportedDataType.NEXRAD_LEVEL3 || dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            isAttributeFilterUsed =
                level3AttributeFilterPanel.getLevel3MinDistance() != WCTFilter.NO_MIN_DISTANCE ||
                level3AttributeFilterPanel.getLevel3MaxDistance() != WCTFilter.NO_MAX_DISTANCE ||
                level3AttributeFilterPanel.getLevel3MinValues()[0] != WCTFilter.NO_MIN_VALUE ||
                level3AttributeFilterPanel.getLevel3MaxValues()[0] != WCTFilter.NO_MAX_VALUE ;
        }
        else {
            isAttributeFilterUsed = 
                gridAttributeFilterPanel.getGridMinValues()[0] != WCTFilter.NO_MIN_VALUE ||
                gridAttributeFilterPanel.getGridMaxValues()[0] != WCTFilter.NO_MAX_VALUE ;
        }

        jlSelectedExportFormat.setText(selectedExportFormatString);
        panel.add(new JLabel("Number of files selected: "), "p left");
        panel.add(new JLabel(""+dataUrls.length), "tab");
        panel.add(new JLabel("Input Data Type: "), "br left");
        panel.add(new JLabel(dataType.toString()), "tab");
        panel.add(new JLabel("Output Format: "), "br left");
        panel.add(jlSelectedExportFormat, "tab");
        panel.add(new JLabel("Output Directory: "), "br left");
        panel.add(new JLabel(jtfOutputDirectory.getText()), "tab");
        
        if (selectedExportFormat != ExportFormat.RAW_NETCDF && selectedExportFormat != ExportFormat.NATIVE) {
            panel.add(new JLabel("Spatial Filter Engaged? "), "br left");
            panel.add(new JLabel((spatialFilterPanel.isSpatialFilterEngaged() ? "Yes" : "No")), "tab");
            panel.add(new JLabel("Attribute Filter Engaged? "), "br left");
            panel.add(new JLabel((isAttributeFilterUsed ? "Yes" : "No")), "tab");
            if (dataType == SupportedDataType.RADIAL) {
                panel.add(new JLabel("Moment: "), "p left");
                panel.add(new JLabel(radialPropsPanel.getVariableName()), "tab");
                panel.add(new JLabel("Elevation Angle: "), "br left");
                panel.add(new JLabel(WCTUtils.DECFMT_0D00.format(radialPropsPanel.getCutElevation())), "tab");
            }
        }
        
        view5Panel.add(panel, BorderLayout.CENTER);
        view5Panel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshView5Panel() throws IOException, Exception {
        view5Panel.removeAll();
        createView5Panel();
    }

    
    
    
    private void createView6Panel() {
        view6Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        view6Panel.setBorder(WCTUiUtils.myTitledBorder("Processing Progress", 10, 10, 4, 10));

        final JLabel statusLabel = new JLabel(" ");
        final JLabel inLabel = new JLabel(" ");
        final JProgressBar fileProgressBar = new JProgressBar();
		fileProgressBar.setStringPainted(true);
        exporter.addDataExportListener(new DataExportListener() {
            @Override
            public void exportStatus(DataExportEvent event) {
                statusLabel.setText(event.getStatus());
//                inLabel.setText(event.getDataURL().toString().substring(event.getDataURL().toString().lastIndexOf("/")+1));
                outLabel.setText(event.getOutputFile().getName());
            }
            @Override
            public void exportStarted(DataExportEvent event) {
                fileProgressBar.setValue(0);
            }
            @Override
            public void exportProgress(DataExportEvent event) {
                fileProgressBar.setValue(event.getProgress());
            }
            @Override
            public void exportEnded(DataExportEvent event) {
            }
        });
        exporter.addGeneralProgressListener(new GeneralProgressListener() {
			@Override
			public void started(GeneralProgressEvent event) {
                fileProgressBar.setValue(0);				
			}
			@Override
			public void ended(GeneralProgressEvent event) {
				fileProgressBar.setString("");
			}
			@Override
			public void progress(GeneralProgressEvent event) {
                fileProgressBar.setValue((int)event.getProgress());
				fileProgressBar.setString("Data Transfer"+event.getStatus());
                
//                System.out.println("steve: "+event.getStatus()+" , "+event.getProgress());
                
			}
        });
        
        panel.add(statusLabel, "p center");
//        panel.add(new JLabel("In: "), "br");
//        panel.add(inLabel, "tab");
        panel.add(new JLabel("Saving: "), "center br");
        panel.add(outLabel, "br");
        panel.add(new JLabel(" "), "p");
        panel.add(new JLabel(" "), "p");
        panel.add(new JLabel("File Export Progress:"), "p");
        panel.add(fileProgressBar, "hfill br");
        panel.add(new JLabel("Total Export Progress:"), "br");
        panel.add(exportProgressBar, "hfill br");
        
        

        JButton nextButton = new JButton("Cancel");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                exportProgressBar.setString("-- Canceling --");
                cancelExport = true;
                
                WCTUtils.getSharedCancelTask().setCancel(true);
                
//                statusLabels[4].setBackground(mainPanel.getBackground());
//                statusLabels[5].setBackground(Color.GREEN);
//                statusLabels[4].setFont(statusLabels[4].getFont().deriveFont(Font.PLAIN));
//                statusLabels[5].setFont(statusLabels[5].getFont().deriveFont(Font.BOLD));
//                cardLayout.next(mainPanel);

            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextButton);

        view6Panel.add(panel, BorderLayout.CENTER);
        view6Panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    
    

    private void refreshView7Panel() throws IOException, Exception {
        view7Panel.removeAll();
        createView7Panel();
    }

    private void createView7Panel() {
        view7Panel.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new RiverLayout());

        view7Panel.setBorder(WCTUiUtils.myTitledBorder("Data Export Summary", 10, 10, 4, 10));

        panel.add(new JLabel("Number of files selected: "), "p left");
        panel.add(new JLabel(""+dataUrls.length), "tab");
        panel.add(new JLabel("Input Data Type: "), "br left");
        panel.add(new JLabel(dataType.toString()), "tab");
        panel.add(new JLabel("Output Format: "), "br left");
        panel.add(new JLabel(selectedExportFormatString), "tab");
        panel.add(new JLabel("Output Directory: "), "br left");
        panel.add(new JLabel(jtfOutputDirectory.getText()), "tab");
        panel.add(new JLabel("Spatial Filter Engaged? "), "br left");
        panel.add(new JLabel((spatialFilterPanel.isSpatialFilterEngaged() ? "Yes" : "No")), "tab");
        panel.add(new JLabel("Attribute Filter Engaged? "), "br left");
        panel.add(new JLabel((isAttributeFilterUsed ? "Yes" : "No")), "tab");

        panel.add(new JLabel("Total Processing Time: "), "p left");
        panel.add(jlExportTime, "tab");
        
        
        JButton nextButton = new JButton("Done");
        nextButton.setPreferredSize(new Dimension(100, (int)nextButton.getPreferredSize().getHeight()));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabels[7].setBackground(mainPanel.getBackground());
                statusLabels[0].setBackground(Color.GREEN);
                statusLabels[7].setFont(statusLabels[7].getFont().deriveFont(Font.PLAIN));
                statusLabels[0].setFont(statusLabels[0].getFont().deriveFont(Font.BOLD));
                finish();
            }            
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextButton);

        view7Panel.add(panel, BorderLayout.CENTER);
        view7Panel.add(buttonPanel, BorderLayout.SOUTH);
    }


    public void finish() {
        if (firstRadialDataset != null) {
            try {
                firstRadialDataset.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        viewer.getMapPane().removeZoomChangeListener(zoomListener);
        cancelExport = true;
        dispose();
    }
    
    
    public void updateSpatialExtent(Rectangle2D.Double extent) {
        spatialFilterPanel.setSpatialFilterExtent(extent);
    }
    
    
    public WCTFilter createFilter() {
        WCTFilter filter = new WCTFilter();
        
        // 1. spatial stuff
        if (spatialFilterPanel.isSpatialFilterEngaged()) {
            filter.setExtentFilter(spatialFilterPanel.getSpatialExtent());
        }
        else {
            filter.setExtentFilter(null);
        }
        
        // 2. attribute stuff
        if (dataType == SupportedDataType.RADIAL) {
            filter.setAzimuthRange(radialAttributeFilterPanel.getRadialMinAzimuth(), radialAttributeFilterPanel.getRadialMaxAzimuth());
            filter.setDistanceRange(radialAttributeFilterPanel.getRadialMinDistance(), radialAttributeFilterPanel.getRadialMaxDistance());
            filter.setHeightRange(radialAttributeFilterPanel.getRadialMinHeight(), radialAttributeFilterPanel.getRadialMaxHeight());
            filter.setValueRange(radialAttributeFilterPanel.getRadialMinValues(), radialAttributeFilterPanel.getRadialMaxValues());
        }
        else if (dataType == SupportedDataType.NEXRAD_LEVEL3 || dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            filter.setDistanceRange(level3AttributeFilterPanel.getLevel3MinDistance(), level3AttributeFilterPanel.getLevel3MaxDistance());
            filter.setValueRange(level3AttributeFilterPanel.getLevel3MinValues(), level3AttributeFilterPanel.getLevel3MaxValues());
            filter.setCategoryOverrides(level3AttributeFilterPanel.getCategoryOverrides());
        }
        else {
            filter.setValueRange(gridAttributeFilterPanel.getGridMinValues(), gridAttributeFilterPanel.getGridMaxValues());
        }
        
        
        return filter;
    }

    
    
    
    
    
    
    
    private void doExportInBackground() {
        try {
            foxtrot.Worker.post(new foxtrot.Task() {
                public Object run() {

                    try {
                        doExport();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    return "DONE";
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    private void doExport() {

        System.out.println("OUTPUT FORMAT SET AS: "+exportFormatMap.get(selectedExportFormatString)+" from "+selectedExportFormatString);
        exporter.setOutputFormat(exportFormatMap.get(selectedExportFormatString));

        if (dataType == SupportedDataType.RADIAL) {
            exporter.setExportCut(radialPropsPanel.getCut());
            exporter.setExportVariable(radialPropsPanel.getVariableName());
            exporter.setExportRadialFilter(createFilter());
            exporter.setExportPoints( selectedExportFormatString.contains("(Point Centroid)") );
            exporter.setExportAllPoints(radialPropsPanel.isAllSweepsPointExportSelected());
            exporter.setExportClassify(false);
            exporter.setRadialExportCappiHeightInMeters(radialPropsPanel.getCappiAltitude());
        }
        else if (dataType == SupportedDataType.NEXRAD_LEVEL3 || dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            exporter.setExportL3Filter(createFilter());
        }
        else {
            if (dataType == SupportedDataType.GRIDDED) {
                exporter.setGridExportGridIndex(gridProps.getSelectedGridIndex());
                exporter.setGridExportZIndex(gridProps.getSelectedGridIndex());
                exporter.setGridExportRuntimeIndex(gridProps.getSelectedGridIndex());
                exporter.setGridExportTimeIndex(gridProps.getSelectedTimeIndex());
            }
            exporter.setExportGridSatelliteFilter(createFilter());
        }
        
        
        exportProgressBar.setMaximum(dataUrls.length);
        exportProgressBar.setValue(0);
        exportProgressBar.setString("");
        exportStartTime = System.currentTimeMillis();
        
        SupportedDataType dataTypeOverride = null;
        try {
            if (viewer.getDataSelector().getSelectedDataType() != SupportedDataType.UNKNOWN) { 
                dataTypeOverride = viewer.getDataSelector().getSelectedDataType(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        boolean checkOpendap = viewer.getDataSelector().getDataSourcePanel().getDataType().equals(WCTDataSourceDB.SINGLE_FILE) ||
            viewer.getDataSelector().getDataSourcePanel().getDataType().equals(WCTDataSourceDB.THREDDS);
        
        
        
        boolean okToAll = false;
        for (int n=0; n<dataUrls.length; n++) {
            
            String filename = dataUrls[n].toString().substring(dataUrls[n].toString().lastIndexOf("/")+1);
            try {
                File outFile = new File(jtfOutputDirectory.getText());
            	outLabel.setText(filename+" to cache...");
                exporter.exportData(dataUrls[n], outFile, dataTypeOverride, checkOpendap);
                exportProgressBar.setValue(n+1);
                exportProgressBar.setString("Processed "+WCTUtils.DECFMT_0.format((100.0*(n+1))/dataUrls.length) + 
                        "% ("+(n+1)+" / "+dataUrls.length+")");

                
                
                if (selectedExportFormatType == ExportFormatType.RASTER && 
                		exporter.getLastProcessedRaster() != null &&
                		exporter.getLastProcessedRaster().isNative() && ! isRegularSpatial) {
                	
                	errorLogDialog.getTextArea().append("Note: Native grid coordinate system used - " +
                			"no resampling applied.\nGrid dimensions may vary from what was previously " +
                			"selected for this special case.\n   "+
                			exporter.getFileScanner().getLastScanResult().getFileName()+
                			" ("+exporter.getFileScanner().getLastScanResult().getLongName()+")\n\n");
                	if (! errorLogDialog.isVisible()) {
                		errorLogDialog.setLocationRelativeTo(this);
                        errorLogDialog.setVisible(true);
                	}
                }
                
                
                
                
                if (cancelExport) {
                    n = dataUrls.length;
                    cancelExport = false;
                }
                
            } catch (WCTExportNoDataException nde) {
            	
                int choice;
                if (okToAll) {
                    choice = 0;
                }
                else {
                    String message = "No decodable data is present in the NEXRAD File: \n" +
                    "<html><font color=red>" + filename + "</font></html>\n" +
                    "No output file has been created.\n";

                    Object[] options = {"Ok", "Ok To All", "Cancel"};
                    choice = JOptionPane.showOptionDialog(null, message, "NO DATA PRESENT",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                }
                if (choice == 1) {
                    okToAll = true;
                }

                if (choice == 0 || choice == 1) {
                    // "OK" means do nothing for now
                }
                else if (choice == 2) {
                    // Cancel the loop
                    n = dataUrls.length;
                }

            	
            	
            	
            	errorLogDialog.getTextArea().append("No Data Found: "+
            			exporter.getFileScanner().getLastScanResult().getFileName()+
            			" ("+exporter.getFileScanner().getLastScanResult().getLongName()+")\n");
            	if (! errorLogDialog.isVisible()) {
            		errorLogDialog.setLocationRelativeTo(this);
                    errorLogDialog.setVisible(true);
            	}

                
            } catch (Exception e) {
                e.printStackTrace();
                
                errorLogDialog.getTextArea().append(exporter.getFileScanner().getLastScanResult().getFileName()+
                		" ("+exporter.getFileScanner().getLastScanResult().getLongName() +
                		")  [Error Date: "+new Date()+"]\n\t "+ e.getMessage()+"\n");
            	if (! errorLogDialog.isVisible()) {
            		errorLogDialog.setLocationRelativeTo(this);
                    errorLogDialog.setVisible(true);
            	}

                
                
//                JOptionPane.showOptionDialog(this, e.getMessage(), "Data Export Error", JOptionPane. JOptionPane.ERROR_MESSAGE);
                

//                n = dataUrls.length;
//                cancelExport = false;
            }
            
        }

        jlExportTime.setText((System.currentTimeMillis() - exportStartTime)/1000 + " seconds");
        try {
			refreshView7Panel();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        try {
            Thread.sleep(500);
        } catch (Exception e) {}

        statusLabels[6].setBackground(mainPanel.getBackground());
        statusLabels[7].setBackground(Color.GREEN);
        statusLabels[6].setFont(statusLabels[6].getFont().deriveFont(Font.PLAIN));
        statusLabels[7].setFont(statusLabels[7].getFont().deriveFont(Font.BOLD));
        cardLayout.next(mainPanel);

        
    }
    

    

}
