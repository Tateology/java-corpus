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
//import gov.noaa.ncdc.common.RiverLayout;
//import gov.noaa.ncdc.common.SwingWorker;
//import gov.noaa.ncdc.help.JNXHelp;
//import gov.noaa.ncdc.nexradexport.ExtractCAPPI;
//import gov.noaa.ncdc.nexradexport.NexradMetaDataExtract;
//import gov.noaa.ncdc.nexradiv.SupplementalDialog;
//import gov.noaa.ncdc.nexradiv.WCTTextDialog;
//import gov.noaa.ncdc.wct.WCTFilter;
//import gov.noaa.ncdc.wct.WCTIospManager;
//import gov.noaa.ncdc.wct.WCTProperties;
//import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
//import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
//import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
//import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
//import gov.noaa.ncdc.wct.event.DataExportEvent;
//import gov.noaa.ncdc.wct.event.DataExportListener;
//import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
//import gov.noaa.ncdc.wct.event.GeneralProgressListener;
//import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
//import gov.noaa.ncdc.wct.export.WCTExport.ExportFormatType;
//import gov.noaa.ncdc.wct.io.FileScanner;
//import gov.noaa.ncdc.wct.io.ScanResults;
//import gov.noaa.ncdc.wct.io.ScanResultsComparator;
//import gov.noaa.ncdc.wct.io.SupportedDataType;
//import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
//import gov.noaa.ncdc.wct.io.WCTDirectoryScanner;
//import gov.noaa.ncdc.wct.io.WCTTransfer;
//import gov.noaa.ncdc.wct.ui.AnalysisWizard;
//import gov.noaa.ncdc.wct.ui.DataSelectorInterface;
//import gov.noaa.ncdc.wct.ui.DataSourcePanel;
//import gov.noaa.ncdc.wct.ui.WCTFrame;
//import gov.noaa.ncdc.wct.ui.WCTUiInterface;
//import gov.noaa.ncdc.wct.ui.WCTUiUtils;
//import gov.noaa.ncdc.wct.ui.WCTViewer;
//import gov.noaa.ncdc.wct.ui.WCTViewerSplash;
//import gov.noaa.ncdc.wct.ui.event.SortByListener;
//import gov.noaa.ncdc.wct.ui.filter.WCTFilterGUI;
//
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.awt.Font;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.text.DecimalFormat;
//import java.util.Date;
//
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
//import javax.swing.DefaultListModel;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JFileChooser;
//import javax.swing.JLabel;
//import javax.swing.JList;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JProgressBar;
//import javax.swing.JRadioButtonMenuItem;
//import javax.swing.JScrollPane;
//import javax.swing.JSeparator;
//import javax.swing.JSpinner;
//import javax.swing.JTextField;
//import javax.swing.KeyStroke;
//import javax.swing.ListSelectionModel;
//import javax.swing.SpinnerNumberModel;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.XPathExpressionException;
//
//import org.xml.sax.SAXException;
//
//import ucar.nc2.NCdump;
//import ucar.nc2.constants.FeatureType;
//import ucar.nc2.dt.RadialDatasetSweep;
//import ucar.nc2.dt.TypedDatasetFactory;
//import ucar.nc2.util.CancelTask;
///**
// *  Description of the Class
// *
// * @author     steve.ansari
// * @created    July 9, 2004
// */
//public class WCTExportGUI extends WCTFrame 
//implements ScanResultsManager, WCTUiInterface, DataExportListener, DataSelectorInterface, ActionListener {
//
//
////  public static final int EXPORT_SUCCESSFUL = 0;
////  public static final int EXPORT_UNSUCCESSFUL = 1;
////  public static final int EXPORT_UNSUPPORTED = 2;
//
//    // JMenu Declarations
//    private JMenuBar jMenuBar;
//    private JMenu jmFile;
//    private JMenuItem jmiFileExit;
//    private JMenu jmFormat;
//    private JMenuItem jmiFormatVector, jmiFormatRaster;
//    private JRadioButtonMenuItem jmiFormatNative, jmiFormatRawNetcdf, jmiFormatShapefile, jmiFormatWKT, jmiFormatCSV, jmiFormatGeoTIFF, jmiFormatArcInfoAscii;
//    private JRadioButtonMenuItem jmiFormatArcInfoBinary, jmiFormatGrADS, jmiFormatGML, jmiFormatNetCDF, jmiFormatVTK;
//    private JSeparator jmiFormatSeparator;
//    private JMenuItem jmiOptionsRaster, jmiRadialProp, jmiNexradFilter;
//    private JMenu jmOptions, jmHelp;
//    private JMenuItem jMenuItemH1, jMenuItemH2, jmiNewFeatures;
//
//    private JPanel choicePanel;
//    private JTextField jOpText;
//    private JButton jbExport, jbCancel, jbBrowse, jbDescribe, jbAnalysis;
////  private NexradIAViewer nexview;
////    private String[] files;
//    private JCheckBox jcbExtent;
//    private String saveDirectory = "";
//
//    
//
//    private static final DecimalFormat fmt2 = new DecimalFormat("0.00");
//
//
//    private WCTFilterGUI nxfilterGUI;
//
//    private NexradMetaDataExtract nexradMetaDataExtract;
//    private RadialExportProperties level2Prop;
//
//    private JProgressBar progress, writeProgress;
//
//    private int loopIndexValue = 0;
//
//    private boolean reducePolys;
////    private boolean wmoFormat;
//    private int gridSize;
//    private float gridNoData;
//    private boolean gridVariableRes;
//
//    private URL lastNexradURL = null;
//
//    private boolean exitOnClose;
//
//    private boolean exportClassify = true;
//    private String exportVariable = "Reflectivity";
//    private int exportCut = 0;
//    private boolean exportUseRF = false;
//    private WCTFilter exportL2Filter, exportL3Filter, exportXMRGFilter;
//    private double exportElevation = 0.0;
//    private boolean exportPoints = false;
//    private boolean exportAllPoints = false;
//    private int exportGridSize = 1200;
//    private float exportGridNoData = -999.0f;
//    private boolean exportGridVariableRes = false;
//
//    private DataSourcePanel dataSourcePanel;
//    private RasterProperties rasterProps;
//
//    private JNXHelp jnxnew;
//
//    private boolean okToAll = false;
//
//    private WCTExport exporter;
//
//    // Special for Level-II file pre-export scan
//    private DecodeRadialDatasetSweepHeader radialDatasetHeader;
//    private DecodeL3Header level3Header;
//
//
//    private final JList resultsList = new JList();
//    private final DefaultListModel listModel = new DefaultListModel();
//    private final WCTDirectoryScanner dirScanner = new WCTDirectoryScanner();
//    private ScanResults[] scanResults = null;
//
//    
//    
//    final WCTTextDialog errorLogDialog = new WCTTextDialog(this, "", "Export Error Log: ", false);
//    
//    private WCTViewer viewer = null;
//    private static WCTExportGUI singletonInstance;
//    
//    
//    /**
//     * Singleton Constructor for the NexradExportGUI object
//     *
//     * @param  viewer  Viewer object
//     * @param  title        Frame Title
//     * @throws ParserConfigurationException 
//     * @throws IOException 
//     * @throws SAXException 
//     * @throws XPathExpressionException 
//     * @throws NumberFormatException 
//     */
//    public static WCTExportGUI createInstance(WCTViewer viewer, String title) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//        if (singletonInstance == null) {
//            singletonInstance = new WCTExportGUI(viewer, title);
//        }
//        return singletonInstance;
//    }
//    
//    
//    
//
//    
//    
//
//
//    /**
//     * Singleton Constructor for the WCTExportGUI object
//     *
//     * @param  viewer  Viewer object
//     * @param  title        Frame Title
//     * @throws ParserConfigurationException 
//     * @throws IOException 
//     * @throws SAXException 
//     * @throws XPathExpressionException 
//     * @throws NumberFormatException 
//     */
//
//	private WCTExportGUI(WCTViewer viewer, String title) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//        super(title + " ( Old Version - Gridded Data Not Supported - Use Export Wizard )");
//        this.viewer = viewer;
//        
//        exporter = new WCTExport();
//        
//        loadSettings();
//        createGUI();
//
//        // Add export progress listener
//        exporter.addDataExportListener(this);
//
//        pack();
//        setVisible(true);
//    }
//
//
//    /**
//     *  Description of the Method
//     */
//    private void loadSettings() {
//        saveDirectory = WCTProperties.getWCTProperty("jne_export_dir");
//        if (saveDirectory == null) {
//            saveDirectory = "";
//        }
//    }
//
//
//    /**
//     *  Description of the Method
//     */
//    private void createGUI() {
//
//        // configure error dialog 
//        errorLogDialog.setSize(750, 150);
//
//        
//        
//        // Set up JMenu Menus
//        jMenuBar = new JMenuBar();
//        jmFile = new JMenu("File");
//        jmFile.setMnemonic(KeyEvent.VK_F);
//        jmiFileExit = new JMenuItem("Exit", KeyEvent.VK_X);
//        jmiFileExit.addActionListener(this);
//        if (!exitOnClose) {
//            jmiFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
//        }
//        jmFormat = new JMenu("Format");
//        jmFormat.setMnemonic(KeyEvent.VK_M);
//        jmiFormatNative = new JRadioButtonMenuItem("Native Format (copy)");
//        jmiFormatNative.setMnemonic(KeyEvent.VK_N);
//        jmiFormatRawNetcdf = new JRadioButtonMenuItem("Raw NetCDF");
//        jmiFormatRawNetcdf.setMnemonic(KeyEvent.VK_T);
//        jmiFormatVector = new JMenuItem("<html><b>Vector (Polygon)</b></html>");
//        jmiFormatShapefile = new JRadioButtonMenuItem("Shapefile", true);
//        jmiFormatShapefile.setMnemonic(KeyEvent.VK_S);
//        jmiFormatWKT = new JRadioButtonMenuItem("Well-Known Text");
//        jmiFormatWKT.setMnemonic(KeyEvent.VK_W);
//        jmiFormatCSV = new JRadioButtonMenuItem("CSV (Comma Separated Text)");
//        jmiFormatCSV.setMnemonic(KeyEvent.VK_V);
//        jmiFormatSeparator = new JSeparator();
//        jmiFormatRaster = new JMenuItem("<html><b>Raster</b></html>");
//        jmiFormatGeoTIFF = new JRadioButtonMenuItem("GeoTIFF");
//        jmiFormatGeoTIFF.setMnemonic(KeyEvent.VK_G);
//        jmiFormatArcInfoAscii = new JRadioButtonMenuItem("ESRI ASCII Grid");
//        jmiFormatArcInfoAscii.setMnemonic(KeyEvent.VK_A);
//        jmiFormatArcInfoBinary = new JRadioButtonMenuItem("ESRI Binary Grid");
//        jmiFormatArcInfoBinary.setMnemonic(KeyEvent.VK_B);
//        jmiFormatGrADS = new JRadioButtonMenuItem("GrADS Binary");
//        jmiFormatGrADS.setMnemonic(KeyEvent.VK_R);
//        jmiFormatGML = new JRadioButtonMenuItem("GML");
//        jmiFormatGML.setMnemonic(KeyEvent.VK_G);
//        jmiFormatNetCDF = new JRadioButtonMenuItem("Gridded NetCDF");
//        jmiFormatNetCDF.setMnemonic(KeyEvent.VK_C);
//        jmiFormatVTK = new JRadioButtonMenuItem("VTK");
//        jmiFormatVTK.setMnemonic(KeyEvent.VK_K);
//
//        // Disable the unavailable exports
//        //jmiFormatWKT.setEnabled(false);
//        //jmiFormatGeoTIFF.setEnabled(false);
//        //jmiFormatGML.setEnabled(false);
//        //jmiFormatNetCDF.setEnabled(false);
//
//        jmiFormatNative.addActionListener(this);
//        jmiFormatRawNetcdf.addActionListener(this);
//        jmiFormatShapefile.addActionListener(this);
//        jmiFormatWKT.addActionListener(this);
//        jmiFormatCSV.addActionListener(this);
//        jmiFormatGeoTIFF.addActionListener(this);
//        jmiFormatArcInfoAscii.addActionListener(this);
//        jmiFormatArcInfoBinary.addActionListener(this);
//        jmiFormatGrADS.addActionListener(this);
//        jmiFormatGML.addActionListener(this);
//        jmiFormatNetCDF.addActionListener(this);
//        jmiFormatVTK.addActionListener(this);
//
//        ButtonGroup buttonGroup1 = new ButtonGroup();
//        buttonGroup1.add(jmiFormatNative);
//        buttonGroup1.add(jmiFormatRawNetcdf);
//        buttonGroup1.add(jmiFormatShapefile);
//        buttonGroup1.add(jmiFormatWKT);
//        buttonGroup1.add(jmiFormatCSV);
//        buttonGroup1.add(jmiFormatGeoTIFF);
//        buttonGroup1.add(jmiFormatArcInfoAscii);
//        buttonGroup1.add(jmiFormatArcInfoBinary);
////        buttonGroup1.add(jmiFormatGrADS);
////        buttonGroup1.add(jmiFormatGML);
//        buttonGroup1.add(jmiFormatNetCDF);
//        buttonGroup1.add(jmiFormatVTK);
//
//        jmOptions = new JMenu("Options");
//        jmOptions.setMnemonic(KeyEvent.VK_O);
//        jmiRadialProp = new JMenuItem("Level-II Options", KeyEvent.VK_L);
//        jmiRadialProp.addActionListener(this);
//        jmiRadialProp.setEnabled(false);
//        jmiNexradFilter = new JMenuItem("Export Filter", KeyEvent.VK_F);
//        jmiNexradFilter.addActionListener(this);
//        //jmiNexradFilter.setEnabled(false);
//        jmiOptionsRaster = new JMenuItem("Raster Options", KeyEvent.VK_R);
//        jmiOptionsRaster.addActionListener(this);
////        jmiOptionsApplyPolyR = new JMenuItem("Apply Polygon Reduction?");
////        jmiOptionsApplyPolyYes = new JRadioButtonMenuItem("Yes");
////        jmiOptionsApplyPolyNo = new JRadioButtonMenuItem("No", true);
//
////        ButtonGroup buttonGroup3 = new ButtonGroup();
////        buttonGroup3.add(jmiOptionsApplyPolyYes);
////        buttonGroup3.add(jmiOptionsApplyPolyNo);
//
//        jMenuItemH1 = new JMenuItem("About", KeyEvent.VK_A);
//        jMenuItemH1.addActionListener(this);
//        jMenuItemH2 = new JMenuItem("Help", KeyEvent.VK_H);
//        jMenuItemH2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
//        jMenuItemH2.addActionListener(this);
//        jMenuItemH2.setEnabled(false);
//        jmiNewFeatures = new JMenuItem("New Features", KeyEvent.VK_N);
//        jmiNewFeatures.addActionListener(this);
//
//        jmHelp = new JMenu("Help");
//        jmHelp.setMnemonic(KeyEvent.VK_H);
//        jmFile.add(jmiFileExit);
//        jmFormat.add(jmiFormatNative);
//        jmFormat.add(new JSeparator());
//        jmFormat.add(jmiFormatRawNetcdf);
//        jmFormat.add(new JSeparator());
//        jmFormat.add(jmiFormatVector);
//        jmFormat.add(jmiFormatShapefile);
//        jmFormat.add(jmiFormatWKT);
//        jmFormat.add(jmiFormatCSV);
////        jmFormat.add(jmiFormatGML);
//        jmFormat.add(jmiFormatSeparator);
//        jmFormat.add(jmiFormatRaster);
//        jmFormat.add(jmiFormatGeoTIFF);
//        jmFormat.add(jmiFormatArcInfoAscii);
//        jmFormat.add(jmiFormatArcInfoBinary);
////        jmFormat.add(jmiFormatGrADS);
//        //jmFormat.add(jmiFormatHDF);
//        jmFormat.add(jmiFormatNetCDF);
////        jmFormat.add(jmiFormatVTK);
////        jmOptions.add(jmiLevel2Prop);
////        jmOptions.add(new JSeparator());
//        jmOptions.add(jmiNexradFilter);
//        jmOptions.add(new JSeparator());
//        jmOptions.add(jmiOptionsRaster);
////        jmOptions.add(new JSeparator());
////        jmOptions.add(jmiOptionsApplyPolyR);
////        jmOptions.add(jmiOptionsApplyPolyYes);
////        jmOptions.add(jmiOptionsApplyPolyNo);
////        jmOptions.add(jmiOptionsSeparator2);
////        jmOptions.add(jmiOptionsKeepFileFormat);
////        jmOptions.add(jmiOptionsKeepFileFormatYes);
////        jmOptions.add(jmiOptionsKeepFileFormatNo);
//        jmHelp.add(jMenuItemH1);
//        //jmHelp.add(jMenuItemH2);
//        jmHelp.add(jmiNewFeatures);
//        jMenuBar.add(jmFile);
//        jMenuBar.add(jmFormat);
//        jMenuBar.add(jmOptions);
//        jMenuBar.add(jmHelp);
//
//
//        this.setJMenuBar(jMenuBar);
//
//        // Create Action Buttons
//        choicePanel = new JPanel();
////        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
//        choicePanel.setLayout(new RiverLayout());
//
//        jOpText = new JTextField(40);
//        jOpText.setText(saveDirectory);
//        jbBrowse = new JButton("Browse");
//        jbBrowse.addActionListener(this);
//
//        jbDescribe = new JButton("Describe");
//        //jbDescribe.setEnabled(false);
//        jbDescribe.addActionListener(this);
//        jbExport = new JButton("Export");
//        //jbExport.setEnabled(false);
//        jbExport.addActionListener(this);
//        jbCancel = new JButton("Cancel");
//        jbCancel.addActionListener(this);
//        //jbCancel.setEnabled(false);
//
//        
//        jbAnalysis = new JButton("Analysis");
//        jbAnalysis.addActionListener(this);
////        jbAnalysis.setMnemonic(KeyEvent.VK_M);
////        jbAnalysis.setPreferredSize(new Dimension(120, (int)jbAnalysis.getPreferredSize().getHeight()));
//
//        
//        
//        
//        JPanel formatPanel = new JPanel();
//        JComboBox jcomboFormats = new JComboBox(new String[] {
//                "Native Format (Copy)", "Raw NetCDF", 
//                "Point: Shapefile", "Point: Well-Known Text", "Point: CSV Text", 
//                "Polygon: Shapefile", "Polygon: Well-Known Text", 
//                "Grid: Arc/Info ASCII", "Grid: GeoTIFF", "Grid: NetCDF", "Grid: Arc/Info Binary Float"
//        });
//        formatPanel.add(jcomboFormats);
//        formatPanel.setBorder(WCTUiUtils.myTitledBorder("Output Format", 2));
//
//        JPanel outputPanel = new JPanel();
//        outputPanel.add(jbBrowse);
//        outputPanel.add(jOpText);
////        outputPanel.setBorder(WCTUiUtils.myTitledBorder("Output Directory", 2));
//
//        JPanel optionsPanel = new JPanel();
//        JComboBox jcomboOptions = new JComboBox(new String[] {
//                "Data Default", "Current View", 
//                "Bookmark", "Custom"
//        });
//        optionsPanel.add(jcomboOptions);
//        optionsPanel.setBorder(WCTUiUtils.myTitledBorder("Spatial Extent", 2));
//        
//        JPanel buttonPanel = new JPanel();
////        excPanel.add(jbDescribe);
//        buttonPanel.add(jbExport);
//        buttonPanel.add(jbAnalysis);
//        buttonPanel.add(jbCancel);
//
//
//
//        writeProgress = new JProgressBar(0, 99);
//        writeProgress.setStringPainted(true);
//        writeProgress.setString("File Export Progress");
//        JPanel writeProgressPanel = new JPanel();
//        writeProgressPanel.setLayout(new BorderLayout());
//        writeProgressPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//        writeProgressPanel.add(writeProgress, BorderLayout.CENTER);
//
//        progress = new JProgressBar(0, 100);
//        progress.setString("Overall Export Progress");
//        progress.setStringPainted(true);
//        JPanel progressPanel = new JPanel();
//        progressPanel.setLayout(new BorderLayout());
//        progressPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
//        progressPanel.add(progress, BorderLayout.CENTER);
//
//        choicePanel.add(new JLabel("Hold the 'Shift' or 'Control' keys to make multiple selections"), "center br");
////        choicePanel.add(formatPanel, "p center");
//        choicePanel.add(new JLabel("Choose Output Directory"), "p center");
//        choicePanel.add(outputPanel, "br center");
////        choicePanel.add(optionsPanel, "center");
//        choicePanel.add(buttonPanel, "center br");
//        choicePanel.add(writeProgressPanel, "p hfill");
//        choicePanel.add(progressPanel, "br hfill");
//
//        dataSourcePanel = new DataSourcePanel(new SubmitButtonListener(this), 
//        		new SortByListener(this), new FilterKeyListener(this), false);
//        dataSourcePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//        resultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        resultsList.setModel(listModel);
//        resultsList.setFont(new Font("monospaced", Font.PLAIN, 12));
//        resultsList.addKeyListener(new KeyHandler());
//
//
//        JScrollPane resultsListScrollPane = new JScrollPane(resultsList);
//        resultsListScrollPane.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createEmptyBorder(2, 12, 2, 12),
//                BorderFactory.createEtchedBorder())
//             );
//
//        getContentPane().setLayout(new BorderLayout());
//        getContentPane().add(dataSourcePanel, "North");
//        getContentPane().add(resultsListScrollPane, "Center");
//        getContentPane().add(new JScrollPane(choicePanel), "South");
//
//
//        // set up filter GUI
////        nxfilterGUI = new WCTFilterGUI("Export Filter", this, true);
//
//        // set up raster properties
//        rasterProps = new RasterProperties(this, "Raster Properties", true);
//
//
//    }
//
//
//
//    /**
//     * DataSelectorInterface implementation
//     */
//    public void doubleClickedList() {
//        //loadNexrad();
//    }
//
//
//    /**
//     * DataSelectorInterface implementation
//     */
//    public void enterKeyPressedFromList() {
//        //loadNexrad();
//    }
//
//
//    /**
//     * DataSelectorInterface implementation
//     */
//    public void listReloaded() {
//        //nexview.setAnimateEnabled(true);
//        //nexview.clearNexradAnimator();
//        //nexview.clearAlphaProperties();
//    }
//
//    /**
//     * DataSelectorInterface implementation
//     */
//    public void fileRequested(URL url) {
//        return;
//    }
//
//
//
//    //----------------------------------------------------------
//    // Implementation of NexradExportListener interface.
//    //----------------------------------------------------------
//    /**
//     *  Description of the Method
//     *
//     * @param  event  Description of the Parameter
//     */
//    public void exportStarted(DataExportEvent event) {
//        System.out.println("Export Started");
//    }
//
//
//    /**
//     *  Description of the Method
//     *
//     * @param  event  Description of the Parameter
//     */
//    public void exportProgress(DataExportEvent event) {
//        writeProgress.setValue(event.getProgress());
//        //System.out.println("Export Progress: " + event.getProgress() + " %");
//    }
//
//
//    /**
//     *  Description of the Method
//     *
//     * @param  event  Description of the Parameter
//     */
//    public void exportEnded(DataExportEvent event) {
//        System.out.println("Export Ended");
//    }
//
//
//    /**
//     *  Description of the Method
//     *
//     * @param  event  Description of the Parameter
//     */
//    public void exportStatus(DataExportEvent event) {
//        writeProgress.setString(event.getStatus());
//        System.out.println("Export Progress: " + event.getStatus());
//    }
//
//
//    
//    
//
//    
//    
////    @Override
//    public void setScanResults(ScanResults[] scanResults) {
//        this.scanResults = scanResults;
//        if (scanResults == null) {
//            return;
//        }
//        WCTUiUtils.fillListModel(listModel, scanResults);
//    }
//
////    @Override
//    public ScanResults[] getScanResults() {
//        return scanResults;
//    }
//    
////    @Override
//    public ScanResultsComparator getSortByComparator() {
//        return dataSourcePanel.getSortByComparator();
//    }
//
//    
//    
//    
//    
//    
//    public URL getSelectedURL() {
//        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
//            URL url = null;
//            String source = dataSourcePanel.getDataLocation();
//            try {
//                if (source.startsWith("http://") || source.startsWith("ftp://")) {
//                    url = new URL(source);
//                }
//                else {
//                    url = new File(source).toURI().toURL();
//                }
//            } catch (Exception e) {
//                return null;
//            }
//            return url;
//        }
//        else {
//            return scanResults[resultsList.getSelectedIndex()].getUrl();
//        }
//    }
//
//    public URL getSelectedURL(int index) {
//        
////        for (int n=0; n<scanResults.length; n++) {
////            System.out.println("scanResult["+n+"]="+scanResults[n]);
////        }
//
//        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
//            return getSelectedURL();
//        }
//        else {
//            int[] indices = resultsList.getSelectedIndices();
//            return scanResults[indices[index]].getUrl();
//        }
//    }
//    
//    public int[] getSelectedIndices() {
//        return resultsList.getSelectedIndices();
//    }
//
//    
//    
//    public void setSelectedIndices(int[] indices) {
//        resultsList.setSelectedIndices(indices);
//        if (indices.length > 0) {
//            resultsList.ensureIndexIsVisible(indices[0]);
//        }
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
//    
//    
//    
//
//    // Implementation of ActionListener interface.
//    /**
//     *  Description of the Method
//     *
//     * @param  event  Description of the Parameter
//     */
//    public void actionPerformed(ActionEvent event) {
//
//        Object source = event.getSource();
//
//        if (source == jbDescribe) {
//            final int[] indices = resultsList.getSelectedIndices();
//            if (nexradMetaDataExtract == null) {
//                nexradMetaDataExtract = new NexradMetaDataExtract();
//            }
//
//            for (int i = 0; i < indices.length; i++) {
//                try {
//                    nexradMetaDataExtract.showDescribeNexradDialog(getSelectedURL(i), this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    javax.swing.JOptionPane.showMessageDialog(this, "Metadata Error: " + e, "METADATA ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        }
//        else if (source == jbBrowse) {
//
//            // Export NEXRAD Shapefiles Dialog
//            // Set up File Chooser
//            JFileChooser fc = new JFileChooser(saveDirectory);
//            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            fc.setDialogTitle("Choose Write Directory for Exported Data");
//            //fc.addChoosableFileFilter(new OpenFileFilter("shp", true, "ESRI Shapefiles (.shp/.shx/.dbf/.prj) "));
//
//            int returnVal = fc.showSaveDialog(jbBrowse);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                saveDirectory = fc.getSelectedFile().toString();
//                jOpText.setText(saveDirectory);
//                WCTProperties.setWCTProperty("jne_export_dir", saveDirectory);
//            }
//        }
//        else if (source == jbExport) {
//            runExport();
//        }
//        else if (source == jbAnalysis) {
//        	AnalysisWizard.start(viewer, this);
//        }
//        else if (source == jbCancel) {
//            try {
//                progress.setString("Aborting Export...");
//                setLoopIndexValue(resultsList.getSelectedIndices().length);
//            } catch (NullPointerException npe) {}
//        }
////        else if (source == jmiFormatNative || source == jmiFormatRawNetcdf ||
////                source == jmiFormatShapefile || source == jmiFormatWKT || source == jmiFormatCSV ||
////                source == jmiFormatGeoTIFF || source == jmiFormatArcInfoAscii ||
////                source == jmiFormatArcInfoBinary || source == jmiFormatGrADS ||
////                source == jmiFormatGML || source == jmiFormatNetCDF || source == jmiFormatVTK) {
////            exportType = ((JMenuItem) source).getText();
////            writeProgress.setString(" ");
////            writeProgress.setValue(0);
////            progress.setString(exportType + " Export Progress");
////        }
//        else if (source == jmiRadialProp) {
//            this.getContentPane().remove(level2Prop);
//            this.getContentPane().add(level2Prop, BorderLayout.EAST);
//            this.pack();
////          level2PropFrame.setLocation(10, 10);
////          level2PropFrame.setVisible(true);
////          level2PropFrame.setExtendedState(Frame.NORMAL);
//        }
//        else if (source == jmiOptionsRaster) {
//            rasterProps.pack();
//            rasterProps.setVisible(true);
//
//        }
//        else if (source == jmiNexradFilter) {
//
//            // Load Animator Frame
//            //if (nxfilterGUI == null) {
//            //   nxfilterGUI = new NexradFilterGUI("Export Filter", this);
//            //}
//
//            nxfilterGUI.setVisible(true);
//            nxfilterGUI.setLocation(10, 20);
//            nxfilterGUI.pack();
//        }
//        else if (source == jMenuItemH1) {
//            String message = "NOAA Weather and Climate Toolkit: Data Exporter\n" +
//            "Version " + WCTUiUtils.getVersion() + "\n" +
//            "Author: Steve Ansari\n" +
//            "National Climatic Data Center\n" +
//            "Contact: Steve.Ansari@noaa.gov";
//            JOptionPane.showMessageDialog(this, message,
//                    "About", JOptionPane.INFORMATION_MESSAGE);
//        }
//        else if (source == jmiNewFeatures) {
//            // Lazy object creation
//            if (jnxnew == null) {
//                jnxnew = new JNXHelp(this, "/helphtml/newfeatures.html");
//            }
//            jnxnew.setLocation(10, 20);
//            jnxnew.setVisible(true);
//        }
//        else if (source == jmiFileExit) {
//            if (exitOnClose) {
//                System.exit(1);
//            }
//            else {
//                this.dispose();
//            }
//        }
//    }
//
//
//    // actionPerformed
//
//    /**
//     *  Description of the Method
//     */
//    private void runExport() {
//        try {
//            if (! dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE) && resultsList.getSelectedIndex() == -1) {
//                return;
//            }
//
////            files = dataSelectPanel.getFileNames();
//            saveDirectory = jOpText.getText();
//            if (saveDirectory.trim().length() == 0) {
//                actionPerformed(new ActionEvent(jbBrowse, 0, "Browse"));
//            }
//            try {
//                File test = new File(saveDirectory);
//                if (test.toString().trim().equals("")) {
//                    return;
//                }
//                else if (!test.exists()) {
//                    if (! test.mkdirs()) {
//                        throw new Exception();
//                    }
//                }
//            } catch (Exception e) {
//                String message = "Invalid Directory: \n" +
//                "<html><font color=red>" + saveDirectory + "</font></html>";
//                JOptionPane.showMessageDialog(this, (Object) message,
//                        "DIRECTORY SELECTION ERROR", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            final int[] indices = resultsList.getSelectedIndices();
//            progress.setString("Exporting " + indices.length + " "+((indices.length > 1) ? "files" : "file"));
//            progress.setMaximum(indices.length);
//
//
//
//
//            okToAll = false;
//
//            final Component parent = this;
//            
//            
//            
//            // Put in thread
//            SwingWorker worker =
//                new SwingWorker() {
//                public Object construct() {
//
//                    int n;
//                    int cnt = 0;
//
//                    try {
//
//                        setIsLoading(true);
////System.out.println("in worker thread -- "+dataSourcePanel.getDataLocation());
//                        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
//                            URL dataURL = null;
//                            try {
//                                dataURL = getSelectedURL();
//                            } catch (Exception e) {
//                                JOptionPane.showMessageDialog(parent, "Unable to connect to this file: \n" + dataURL,
//                                        "CONNECTION ERROR", JOptionPane.ERROR_MESSAGE);
//                                e.printStackTrace();
//                                return "ERROR";
//                            }
//
//                            try {
////                                System.out.println("exporting single file data: "+dataURL);
//                                exportData(dataURL);                                
//                            } catch (WCTExportException nde) {
//                                nde.printStackTrace();
//                                errorLogDialog.getTextArea().append(getScanResults()[0].getLongName() +"  [Error Date: "+new Date()+"]\n\t "+ nde.getMessage()+"\n");
//                                errorLogDialog.setVisible(true);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                errorLogDialog.getTextArea().append(getScanResults()[0].getLongName() +"  [Error Date: "+new Date()+"]\n\t "+ e.getMessage()+"\n");
//                                errorLogDialog.setVisible(true);
////                                break;
//                            }
//
//                            progress.setString("Completed Export");
//                            try {
//                                Thread.sleep(1500);
//                            } catch (Exception e) {}
//                            progress.setValue(0);
//                            progress.setString("Overall Export Progress");
//                            writeProgress.setValue(0);
//                            writeProgress.setString("File Export Progress");
//                            setIsLoading(false);
//                            return "DONE";
//                        }
//                        
//                        
//                        
//                        
//                        
//                        
//                        
//                        
//                        setLoopIndexValue(0);
//                        while ((n = getLoopIndexValue()) < indices.length) {
//                            URL dataURL = null;
//                            try {
//                                dataURL = getSelectedURL(n);
//                            } catch (Exception e) {
//                                JOptionPane.showMessageDialog(parent, "Unable to connect to this file: \n" + dataURL,
//                                        "CONNECTION ERROR", JOptionPane.ERROR_MESSAGE);
//                                e.printStackTrace();
//                                return "ERROR";
//                            }
//
//                            try {
//                                exportData(dataURL);                                
//                            } catch (WCTExportException nde) {
//                                nde.printStackTrace();
////                                setLoopIndexValue(indices.length);
////                                JOptionPane.showMessageDialog(parent, nde.getMessage(), "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//                                errorLogDialog.getTextArea().append(getScanResults()[indices[n]].getLongName() +"  [Error Date: "+new Date()+"]\n\t "+ nde.getMessage()+"\n");
//                                errorLogDialog.setVisible(true);
////                                break;
//                            } catch (NewRadialFileException l2e) {
////                                l2e.printStackTrace();
//                                setLoopIndexValue(indices.length);
////                                errorLogDialog.getTextArea().append(getScanResults()[n].getLongName() +"  [Error Date: "+new Date()+"]\n\t "+ l2e.getMessage()+"\n");
////                                errorLogDialog.setVisible(true);
//                                break;
//                            } catch (Exception e) {
//                                e.printStackTrace();
////                                setLoopIndexValue(indices.length);
////                                JOptionPane.showMessageDialog(parent, e.toString(), "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//                                errorLogDialog.getTextArea().append(getScanResults()[indices[n]].getLongName() +"  [Error Date: "+new Date()+"]\n\t "+ e.getMessage()+"\n");
//                                errorLogDialog.setVisible(true);
////                                break;
//                            }
//
//                            progress.setString("Completed " + (n + 1) + "/" + indices.length + " "+((indices.length > 1) ? "files" : "file"));
//                            progress.setValue(n + 1);
//                            incrementLoopIndexValue();
//                            cnt++;
//
//                            // Deselect each list element when finished
//                            int[] selection = new int[indices.length - cnt];
//                            System.arraycopy(indices, cnt, selection, 0, selection.length);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        setIsLoading(false);
//                    }
//                    progress.setString("Completed Export of " + cnt + " files");
//                    try {
//                        Thread.sleep(1500);
//                    } catch (Exception e) {}
//                    progress.setValue(0);
//                    progress.setString("Overall Export Progress");
//                    writeProgress.setValue(0);
//                    writeProgress.setString("File Export Progress");
//                    setIsLoading(false);
//                    return "Done";
//                }
//            };
//            worker.start();
//
//        }
//        // END try
//        catch (Exception e) {
//            e.printStackTrace();
//            listModel.clear();
//            listModel.add(0, "------ ERROR: UNABLE TO LOAD DATA ------");
//            listModel.add(1, e.toString());
//        }
//
//    }
//
//
//
//    private void showDump(final URL url) {
//        try {
//            SupplementalDialog dialog = new SupplementalDialog(this, "ncdump "+url);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            NCdump.print(url.toString(), out);
//        
//            dialog.setText(out.toString());
//            dialog.setVisible(true);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            javax.swing.JOptionPane.showMessageDialog(this, "Unable to dump netcdf structure for: "+url, 
//                    "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
//
//        }
//    }
//
//
//
//
//
//
//
//
//    /**
//     * Set isLoading value - adjusts whether buttons are enabled during loading
//     *
//     * @param  isLoading  The new isLoading value
//     */
//    public void setIsLoading(boolean isLoading) {
//        jbDescribe.setEnabled(!isLoading);
//        jbExport.setEnabled(!isLoading);
//        jbCancel.setEnabled(isLoading);
//    }
//
//
//    /**
//     *  Returns the DataSourcePanel object
//     *
//     * @return    The DataSourcePanel value
//     */
//    public DataSourcePanel getDataSourcePanel() {
//        return dataSourcePanel;
//    }
//
//
//    /**
//     * Return status of Auto-Extent checkbox
//     *
//     * @return    The autoExtentSelected value
//     */
//    public boolean isAutoExtentSelected() {
//
//        return jcbExtent.isSelected();
//    }
//
//
//    /**
//     * Set status of Auto-Extent checkbox
//     *
//     * @param  selected  The new isAutoExtentSelected value
//     */
//    public void setIsAutoExtentSelected(boolean selected) {
//
//        jcbExtent.setSelected(selected);
//    }
//
//
//    /**
//     *  Gets the loopIndexValue attribute of the NexradExportGUI object
//     *
//     * @return    The loopIndexValue value
//     */
//    private int getLoopIndexValue() {
//        return loopIndexValue;
//    }
//
//
//    /**
//     *  Sets the loopIndexValue attribute of the NexradExportGUI object
//     *
//     * @param  n  The new loopIndexValue value
//     */
//    private void setLoopIndexValue(int n) {
//        loopIndexValue = n;
//    }
//
//
//    /**
//     *  Description of the Method
//     */
//    private void incrementLoopIndexValue() {
//        loopIndexValue++;
//    }
//
//
//
//    /**
//     * Checks menu to see which output format is selected.
//     *
//     * @return    The outputFormat value
//     */
//    private ExportFormat getOutputFormat() {
//
//        // Check if Native NEXRAD format is requested
//        if (jmiFormatNative.isSelected()) {
//            return ExportFormat.NATIVE;
//        }
//        else if (jmiFormatRawNetcdf.isSelected()) {
//            return ExportFormat.RAW_NETCDF;
//        }
//        else if (jmiFormatShapefile.isSelected()) {
//            return ExportFormat.SHAPEFILE;
//        }
//        else if (jmiFormatWKT.isSelected()) {
//            return ExportFormat.WKT;
//        }
//        else if (jmiFormatCSV.isSelected()) {
//            return ExportFormat.CSV;
//        }
//        else if (jmiFormatGML.isSelected()) {
//            return ExportFormat.GML;
//        }
//        else if (jmiFormatGeoTIFF.isSelected()) {
//            return ExportFormat.GEOTIFF_GRAYSCALE_8BIT;
//        }
//        else if (jmiFormatArcInfoAscii.isSelected()) {
//            return ExportFormat.ARCINFOASCII;
//        }
//        else if (jmiFormatArcInfoBinary.isSelected()) {
//            return ExportFormat.ARCINFOBINARY;
//        }
////        else if (jmiFormatGrADS.isSelected()) {
////            return ExportFormat.GRADS;
////        }
//        //else if (jmiFormatHDF.isSelected()) {
//        //   return NexradExport.OUTPUT_HDF;
//        //}
//        else if (jmiFormatNetCDF.isSelected()) {
//            return ExportFormat.GRIDDED_NETCDF;
//        }
//        else if (jmiFormatVTK.isSelected()) {
//            return ExportFormat.VTK;
//        }
//        else {
//            return null;
//        }
//
//    }
//
//
//
//    /**
//     * Save Data to common scientific data format.
//     * Allow output filename to be derived after product is decoded.
//     * This allows for the level-2 moment and elevation angle to
//     * be included in the output filename.
//     *
//     * @param  dataURL  Input URL
//     */
//    public void exportData(URL dataURL) 
//    throws WCTExportException, IOException, Exception {
//
//        exportData(dataURL, null);
//    }
//
//
//    /**
//     * Save Data to common scientific data format
//     *
//     * @param  dataURL  Input URL
//     * @param  file       Destination file
//     */
//    public void exportData(URL dataURL, File file) 
//    throws WCTExportException, IOException, Exception {
//
//        ExportFormat outputFormat = getOutputFormat();
//        exporter.setOutputFormat(outputFormat);
//
//System.out.println("OUTPUT FORMAT:::::::::: :::::::::: :::::::::: "+outputFormat);
//
//        // Check if Native NEXRAD format is requested
//        if (outputFormat == ExportFormat.NATIVE) {
//            if (file == null) {
//                String dataURLString = dataURL.toString();
//                String dataFile = dataURLString.substring(dataURLString.lastIndexOf("/")+1);
//                file = new File(saveDirectory + File.separator + dataFile);
//            }
//            
//            System.out.println("NATIVE TRANSFER: "+dataURL+" --> "+file);
//            WCTTransfer.getURL(dataURL, file.getParentFile(), true);
//            return;
//        }
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
//        //------------------------------------------------------------------------------------------------------------------
//        // 1) Set filtering
//        //------------------------------------------------------------------------------------------------------------------
//
//        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//        System.out.println("   L2FILTER: " + exporter.getExportL2Filter());
//        System.out.println("   L3FILTER: " + exporter.getExportL3Filter());
//        System.out.println("   XMRGFILTER: " + exporter.getExportXMRGFilter());
//        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//        System.out.println("   GRID SIZE: " + exporter.getExportGridSize());
//        System.out.println("   NO DATA: " + exporter.getExportGridNoData());
//        System.out.println("   SMOOTHING: " + exporter.getExportGridSmoothFactor());
//        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//
//
//
//
//
//
//        try {
//
//
//            //============================================================================
//            // Set up the default save directory
//            exporter.setDefaultSaveDirectory(saveDirectory);
//
//
//
//
//            System.out.println("FILE:::::::::::::::::::::::::::::::::::::::::::::::::: "+file);
//            System.out.println("nexradURL:::::::::::::::::::::::::::::::::::::::::::::::::: "+dataURL);
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
//
//
//
//
//
//
//
//            FileScanner fileScanner = new FileScanner();
//            // Transfer Level-2 to temp directory for decompression if necessary
//            fileScanner.scanURL(dataURL);
//            // Check for file compression
//            if (fileScanner.isZCompressed()) {
//                dataURL = Level2Transfer.getNCDCLevel2UNIXZ(dataURL);
//                fileScanner.scanURL(dataURL);
//            }
//            else if (fileScanner.isGzipCompressed()) {
//                dataURL = Level2Transfer.getNCDCLevel2GZIP(dataURL);
//                fileScanner.scanURL(dataURL);
//            }
//            else if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//                dataURL = WCTTransfer.getURL(dataURL);
//                // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
//                dataURL = Level2Transfer.decompressAR2V0001(dataURL);
//                fileScanner.scanURL(dataURL);
//            }
//            else {
//                // Transfer file to local tmp area -- force overwrite if NWS
//                if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//                    dataURL = WCTTransfer.getURL(dataURL, true);
//                }               
//                else {
//                    dataURL = WCTTransfer.getURL(dataURL);
//                }
//                fileScanner.scanURL(dataURL);
//            }
//
//            //================================================================================
//
//            String saveName = fileScanner.getSaveName();
//            if (saveName == null || saveName.trim().length() == 0) {
//                saveName = fileScanner.getLastScanResult().getFileName();
//            }
//
//
//            // Get header
//            if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//
//                if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                    exporter.setExportRadialFilter(null);
//                }
//                else {
//                    exporter.setExportRadialFilter(nxfilterGUI.getRadialFilter());
//                }
//
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(WCTFilterGUI.LEVEL2_TAB);
//                }
//                
//                CancelTask emptyCancelTask = new CancelTask() {
//                    public boolean isCancel() {
//                        return false;
//                    }
//                    public void setError(String arg0) {
//                    }
//                };
//
//                RadialDatasetSweep radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
//                        FeatureType.RADIAL, 
//                        dataURL.toString(), emptyCancelTask, new StringBuilder());
//                radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
//                radialDatasetHeader.setRadialDatasetSweep(radialDataset);
//
//
//                if (radialDatasetHeader.getProductType() == NexradHeader.LEVEL2) {
//                    
//
//                    // Decode once so we can create a Level2ExportProperties object
//                    if (level2Prop == null) {
//                        level2Prop = new RadialExportProperties(this, radialDataset);
//                        jmiRadialProp.setEnabled(true);
//                        this.getContentPane().remove(level2Prop);
//                        this.getContentPane().add(level2Prop, BorderLayout.EAST);
//                        this.pack();
////                      level2PropFrame.removeAll();
////                      level2PropFrame.add(level2Prop);
////                      level2PropFrame.setVisible(true);
//                        // First time? Then ask user to choose a Level-2 moment and cut.
//                        lastNexradURL = dataURL;
//                        String message = "NEXRAD LEVEL-II FILE: Please choose a moment and cut (elevation angle)";
//                        if (outputFormat != ExportFormat.VTK) {
//                            JOptionPane.showMessageDialog(this, message, "LEVEL-II EXPORT", JOptionPane.INFORMATION_MESSAGE);
//                            throw new NewRadialFileException(message);
//                        }
//                    }
//
//                    if (lastNexradURL == null || !lastNexradURL.equals(dataURL)) {
//                        level2Prop.setRadialDatasetSweep(radialDataset);
//                        this.getContentPane().remove(level2Prop);
//                        this.getContentPane().add(level2Prop, BorderLayout.EAST);
////                        this.pack();
//                        
//                        //level2PropFrame.pack();
//                        //level2PropFrame.setVisible(true);
//                        // First time with this Level-2 file?
//                        //Then ask user to choose a Level-2 moment and cut.
//                        lastNexradURL = dataURL;
////                        String message = "NEW LEVEL-II FILE: Please choose a moment and cut (elevation angle)";
////                        if (outputFormat != NexradExport.OUTPUT_VTK) {
////                            JOptionPane.showMessageDialog(this, message, "LEVEL-II EXPORT", JOptionPane.INFORMATION_MESSAGE);
////                            throw new NewLevel2FileException(message);
////                        }
//                    }
//                    
//                    
//                    if (level2Prop != null) {
//
//                        exportClassify = level2Prop.getClassify();
//                        exportVariable = level2Prop.getVariableName();
//                        exportCut = level2Prop.getCut();
//                        exportUseRF = level2Prop.getUseRFvalues();
//                        exportElevation = level2Prop.getCutElevation();
//                        exportPoints = level2Prop.getExportPoints().isSelected();
//                        exportAllPoints = level2Prop.getExportAllPoints().isSelected();
//
////                        reducePolys = jmiOptionsApplyPolyYes.isSelected();
//
//                        exporter.setExportVariable(exportVariable);
//                        exporter.setExportCut(exportCut);
//                        exporter.setExportUseRF(exportUseRF);
//                        exporter.setExportClassify(exportClassify);
//                        exporter.setExportPoints(exportPoints);
//                        exporter.setExportAllPoints(exportAllPoints);
//                        exporter.setExportReducePolys(reducePolys);
//
//
//
//                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//                        System.out.println(" NEXRAD EXPORT:");
//                        System.out.println("   CUT: " + exporter.getExportCut());
//                        System.out.println("   ELEVATION: " + exportElevation);
//                        System.out.println("   MOMENT: " + exporter.getExportMoment());
//                        System.out.println("   CLASSIFY: " + exporter.getExportClassify());
//                        System.out.println("   USE-RF-VALUES: " + exporter.getExportUseRF());
//                        System.out.println("   EXPORT-POINTS: " + exporter.getExportPoints());
//                        System.out.println("   EXPORT-ALL-POINTS: " + exporter.getExportAllPoints());
//                        System.out.println("   REDUCE-POLYGONS: " + exporter.getExportReducePolys());
//                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
//
//                    }
//
//
//                }
//                level2Prop.setVisible(true);
//
//                // This will be null if no output file has been specified
//                if (file == null) {
//
//                    if (outputFormat == ExportFormat.VTK) {
//                        file = new File(saveDirectory + File.separator + fileScanner.getSaveName() + ".vtk");
//                    }
//                    else if (exportAllPoints) {
//                        file = new File(saveDirectory + File.separator +
//                                fileScanner.getSaveName(exportVariable)+"_allpnt");
//                    }
//                    else if (exportPoints) {
//                        file = new File(saveDirectory + File.separator +
//                                fileScanner.getSaveName(exportVariable, exportElevation)+"_pnt");
//                    }
//                    else {
//                        file = new File(saveDirectory + File.separator +
//                                fileScanner.getSaveName(exportVariable, exportElevation));
//                    }
//                }
//            }
//            else if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
//                    fileScanner.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//
//                if (level3Header == null) {
//                    level3Header = new DecodeL3Header();
//                }
//                level3Header.decodeHeader(dataURL);
//
//                if (level2Prop != null) {
//                    level2Prop.setVisible(false);
//                }
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(WCTFilterGUI.LEVEL3_TAB);
//                }
//
//                if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                    exporter.setExportL3Filter(null);
//                }
//                else {
//                    exporter.setExportL3Filter(nxfilterGUI.getLevel3Filter());
//                }
//                
//                if (file == null) {
//                    file = new File(saveDirectory + File.separator + saveName);
//                }
//
//            }
//            else if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
//
//                if (level2Prop != null) {
//                    level2Prop.setVisible(false);
//                }
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
//                }
//
//                if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                    exporter.setExportGridSatelliteFilter(null);
//                }
//                else {
//                    exporter.setExportGridSatelliteFilter(nxfilterGUI.getGridFilter());
//                }
//
//                if (file == null) {
//                    file = new File(saveDirectory + File.separator + saveName);
//                }
//
//            }
//            else if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
//                
////                if (getOutputType() == ExportFormatType.VECTOR) {
////                    throw new WCTExportException("Vector Export Types are currently not supported.\n" +
////                    		"Please select export type of 'Raw NetCDF', 'ASCII Grid', 'Binary Grid', 'GeoTIFF' or \n" +
////                    		"'NetCDF' from the 'Format' menu.");
////                }
//                
//                if (level2Prop != null) {
//                    level2Prop.setVisible(false);
//                }
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
//                }
//
//                if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                    exporter.setExportGridSatelliteFilter(null);
//                }
//                else {
//                    exporter.setExportGridSatelliteFilter(nxfilterGUI.getGridFilter());
//                }
//
//                if (file == null) {
//                    file = new File(saveDirectory + File.separator + saveName);
//                }
//            }
//            else if (fileScanner.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
//                
////              if (getOutputType() == ExportFormatType.VECTOR) {
////                  throw new WCTExportException("Vector Export Types are currently not supported.\n" +
////                        "Please select export type of 'Raw NetCDF', 'ASCII Grid', 'Binary Grid', 'GeoTIFF' or \n" +
////                        "'NetCDF' from the 'Format' menu.");
////              }
//
//                if (level2Prop != null) {
//                    level2Prop.setVisible(false);
//                }
//                if (nxfilterGUI != null) {
//                    nxfilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
//                }
//
//                if (nxfilterGUI == null || ! nxfilterGUI.isFilterEngaged()) {
//                    exporter.setExportGridSatelliteFilter(null);
//                }
//                else {
//                    exporter.setExportGridSatelliteFilter(nxfilterGUI.getGridFilter());
//                }
//
//                if (file == null) {
//                    file = new File(saveDirectory + File.separator + saveName);
//                }
//            }
//            else {
//                throw new WCTExportException("Unrecognized File Type.  Please review list of supported file types and naming conventions.\n URL="+dataURL);
//            }
//
//
//
//
//
//            //------------------------------------------------------------------------------------------------------------------
//            // Last-minute overrides!  Check for special non-standard extras
//            //------------------------------------------------------------------------------------------------------------------
//            if (level2Prop != null && level2Prop.isDataCubeEngaged()) {
//                ExtractCAPPI cappi = new ExtractCAPPI(
//                        level2Prop.getDataCubeNumHeightLayers(), 
//                        level2Prop.getDataCubeBottomHeight(),
//                        level2Prop.getDataCubeTopHeight(),
//                        level2Prop.getDataCubeOverlap()
//                );
//
//                cappi.setNexradFilter(exporter.getExportL2Filter());
//
//                // set cube-specific raster properties
//                System.out.println("   CUBE GRID SIZE: " + level2Prop.getDataCubeGridSize());
//                System.out.println("   CUBE NO DATA: " + level2Prop.getDataCubeNoData());
////                System.out.println("   CUBE AUTO-RESOLUTION: " + level2Prop.getDataCubeVariableRes());
//                System.out.println("   CUBE SMOOTHING: " + level2Prop.getDataCubeSmoothFactor());
//
//                exporter.setExportGridSize(level2Prop.getDataCubeGridSize());
//                exporter.setExportGridNoData((float)level2Prop.getDataCubeNoData());
////                exporter.setExportGridVariableRes(level2Prop.getDataCubeVariableRes());
//                exporter.setExportGridSmoothFactor(level2Prop.getDataCubeSmoothFactor());
//
//
//                exporter.setExtractCAPPI(cappi);
//                cappi.addGeneralProgressListener(new GeneralProgressListener() {
////                    @Override
//                    public void ended(GeneralProgressEvent event) {
//                        writeProgress.setValue(0);
//                    }
////                    @Override
//                    public void progress(GeneralProgressEvent event) {
//                        writeProgress.setValue((int)event.getProgress());
//                    }
////                    @Override
//                    public void started(GeneralProgressEvent event) {
//                        writeProgress.setValue(0);
//                    }
//                });
//            }
//            else {
//                exporter.setExtractCAPPI(null);
//            }
//
//
//
//
//
//            exporter.exportData(dataURL, file);
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
//        } catch (WCTExportNoDataException e) {
//            int choice;
//
//            if (okToAll) {
//                choice = 0;
//            }
//            else {
//
//                String message = "No decodable data is present in the NEXRAD File: \n" +
//                "<html><font color=red>" + file + "</font></html>\n" +
//                "No output file has been created.\n";
//
//                Object[] options = {"Ok", "Ok To All", "Cancel"};
//                choice = JOptionPane.showOptionDialog(null, message, "NO DATA PRESENT",
//                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
//                        null, options, options[0]);
//
//            }
//
//
//            if (choice == 1) {
//                okToAll = true;
//            }
//
//            if (choice == 0 || choice == 1) {
//                // "OK" means do nothing for now
//            }
//            else if (choice == 2) {
//                // Cancel the loop
//                setLoopIndexValue(resultsList.getSelectedIndices().length);
//            }
//
//        } catch (Exception e) {
//
//            throw e;
//
//        }
//    }
//
//
//    // END METHOD exportNexrad
//
//
//    /**
//     *  Sets the outputFormat attribute of the NexradExportGUI object
//     *
//     * @param  outputFormat  The new outputFormat value
//     */
//    public void setOutputFormat(ExportFormat outputFormat) {
//        if (outputFormat == ExportFormat.SHAPEFILE) {
//            jmiFormatShapefile.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.WKT) {
//            jmiFormatWKT.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.WKT) {
//            jmiFormatCSV.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.GML) {
//            jmiFormatGML.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
//            jmiFormatGeoTIFF.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.ARCINFOASCII) {
//            jmiFormatArcInfoAscii.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.ARCINFOBINARY) {
//            jmiFormatArcInfoBinary.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
//            jmiFormatNetCDF.setSelected(true);
//        }
//        else if (outputFormat == ExportFormat.VTK) {
//            jmiFormatVTK.setSelected(true);
//        }
//        else {
//            jmiFormatShapefile.setSelected(true);
//        }
//    }
//
//
//    /**
//     *  Gets the outputType attribute of the NexradExportGUI object
//     *
//     * @return    The outputType value
//     */
//    public ExportFormatType getOutputType() {
//        if (jmiFormatShapefile.isSelected() ||
//                jmiFormatWKT.isSelected() || jmiFormatCSV.isSelected()) {
//            return ExportFormatType.VECTOR;
//        }
//        else if (jmiFormatGeoTIFF.isSelected() ||
//                jmiFormatArcInfoAscii.isSelected() ||
//                jmiFormatArcInfoBinary.isSelected() ||
//                jmiFormatGrADS.isSelected() ||
//                jmiFormatGML.isSelected() ||
//                jmiFormatVTK.isSelected() ||
//                jmiFormatNetCDF.isSelected()) {
//            return ExportFormatType.RASTER;
//        }
//        else {
//            return ExportFormatType.NATIVE;
//        }
//    }
//
//
//
//    /**
//     *  Sets the exportClassify attribute of the NexradExportGUI object
//     *
//     * @param  exportClassify  The new exportClassify value
//     */
//    public void setExportClassify(boolean exportClassify) {
//        this.exportClassify = exportClassify;
//    }
//
//
//    /**
//     *  Gets the exportClassify attribute of the NexradExportGUI object
//     *
//     * @return    The exportClassify value
//     */
//    public boolean getExportClassify() {
//        return exportClassify;
//    }
//
//
//    /**
//     *  Sets the exportVariable attribute of the NexradExportGUI object
//     *
//     * @param  exportVariable  The new exportMoment value
//     */
//    public void setExportVariable(String exportVariable) {
//        this.exportVariable = exportVariable;
//    }
//
//
//    /**
//     *  Gets the exportVariable attribute of the NexradExportGUI object
//     *
//     * @return    The exportVariable value
//     */
//    public String getExportMoment() {
//        return exportVariable;
//    }
//
//
//    /**
//     *  Sets the exportCut attribute of the NexradExportGUI object
//     *
//     * @param  exportCut  The new exportCut value
//     */
//    public void setExportCut(int exportCut) {
//        this.exportCut = exportCut;
//    }
//
//
//    /**
//     *  Gets the exportCut attribute of the NexradExportGUI object
//     *
//     * @return    The exportCut value
//     */
//    public int getExportCut() {
//        return exportCut;
//    }
//
//
//    /**
//     *  Sets the exportUseRF attribute of the NexradExportGUI object
//     *
//     * @param  exportUseRF  The new exportUseRF value
//     */
//    public void setExportUseRF(boolean exportUseRF) {
//        this.exportUseRF = exportUseRF;
//    }
//
//
//    /**
//     *  Gets the exportUseRF attribute of the NexradExportGUI object
//     *
//     * @return    The exportUseRF value
//     */
//    public boolean getExportUseRF() {
//        return exportUseRF;
//    }
//
//
//    /**
//     * Set the NexradFilter for Level-II Data
//     *
//     * @param  exportL2Filter  The new exportL2Filter value
//     */
//    public void setExportL2Filter(WCTFilter exportL2Filter) {
//        this.exportL2Filter = exportL2Filter;
//    }
//
//
//    /**
//     * Set the NexradFilter for Level-III Data
//     *
//     * @param  exportL3Filter  The new exportL3Filter value
//     */
//    public void setExportL3Filter(WCTFilter exportL3Filter) {
//        this.exportL3Filter = exportL3Filter;
//    }
//
//
//    /**
//     * Set the NexradFilter for XMRG Data
//     *
//     * @param  exportXMRGFilter  The new exportXMRGFilter value
//     */
//    public void setExportXMRGFilter(WCTFilter exportXMRGFilter) {
//        this.exportXMRGFilter = exportXMRGFilter;
//    }
//
//
//    /**
//     * Gets the NexradFilter for Level-II Data
//     *
//     * @return    The exportL2Filter value
//     */
//    public WCTFilter getExportL2Filter() {
//        return exportL2Filter;
//    }
//
//
//    /**
//     * Gets the NexradFilter for Level-III Data
//     *
//     * @return    The exportL3Filter value
//     */
//    public WCTFilter getExportL3Filter() {
//        return exportL3Filter;
//    }
//
//
//    /**
//     * Gets the NexradFilter for XMRG Data
//     *
//     * @return    The exportXMRGFilter value
//     */
//    public WCTFilter getExportXMRGFilter() {
//        return exportXMRGFilter;
//    }
//
//
//
//    /**
//     *  Sets the exportPoints attribute of the NexradExportGUI object
//     *
//     * @param  exportPoints  The new exportPoints value
//     */
//    public void setExportPoints(boolean exportPoints) {
//        this.exportPoints = exportPoints;
//    }
//
//
//    /**
//     *  Gets the exportPoints attribute of the NexradExportGUI object
//     *
//     * @return    The exportPoints value
//     */
//    public boolean getExportPoints() {
//        return exportPoints;
//    }
//
//
//    /**
//     *  Sets the exportAllPoints attribute of the NexradExportGUI object
//     *
//     * @param  exportAllPoints  The new exportAllPoints value
//     */
//    public void setExportAllPoints(boolean exportAllPoints) {
//        this.exportAllPoints = exportAllPoints;
//    }
//
//
//    /**
//     *  Gets the exportAllPoints attribute of the NexradExportGUI object
//     *
//     * @return    The exportAllPoints value
//     */
//    public boolean getExportAllPoints() {
//        return exportAllPoints;
//    }
//
//
//    /**
//     *  Sets the exportReducePolys attribute of the NexradExportGUI object
//     *
//     * @param  reducePolys  The new exportReducePolys value
//     */
//    public void setExportReducePolys(boolean reducePolys) {
//        this.reducePolys = reducePolys;
//    }
//
//
//    /**
//     *  Gets the exportReducePolys attribute of the NexradExportGUI object
//     *
//     * @return    The exportReducePolys value
//     */
//    public boolean getExportReducePolys() {
//        return reducePolys;
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
//    public RasterProperties getRasterProperties() {
//        return rasterProps;
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
//
//    /**
//     *  Implementation of NexradInterface
//     *
//     * @return    The nexradFile value
//     */
//    public FileScanner getFileScanner() {
//        return exporter.getFileScanner();
//    }
//
//
//
//
//    /**
//     *  Implementation of NexradInterface
//     *
//     * @return    The level3Header value
//     */
//    public DecodeL3Header getLevel3Header() {
//        return exporter.getLevel3Header();
//    }
//
//
//    /**
//     *  Implementation of NexradInterface
//     *
//     * @return    The xMRGHeader value
//     */
//    public DecodeXMRGHeader getXMRGHeader() {
//        return exporter.getXMRGHeader();
//    }
//
//
//
//
//    /**
//     *  Implementation of NexradInterface
//     *
//     * @return    The level3Decoder value
//     */
//    public DecodeL3Nexrad getLevel3Decoder() {
//        return exporter.getLevel3Decoder();
//    }
//
//
//    /**
//     *  Implementation of NexradInterface
//     *
//     * @return    The xMRGDecoder value
//     */
//    public DecodeXMRGData getXMRGDecoder() {
//        return exporter.getXMRGDecoder();
//    }
//
//
//    /**
//     *  Implementation of Interface
//     *  Gets the Filter attribute of the NexradIAViewer object
//     *
//     * @return    filter  The current Filter
//     */
//    public WCTFilter getFilter() {
//        return exporter.getFilter();
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
////    @Override
//    public DecodeRadialDatasetSweep getRadialDecoder() {
//        return exporter.getRadialDatasetDecoder();
//    }
//
//
////    @Override
//    public DecodeRadialDatasetSweepHeader getRadialHeader() {
//        return exporter.getDecodeRadialDatasetSweepHeader();
//    }
//
//
//    /**
//     *  Implementation of NexradInterface
//     *  Gets the NexradFilterGUI attribute of the NexradIAViewer object
//     *
//     * @return    nxfilter  The current NexradFilterGUI
//     */
//    public WCTFilterGUI getFilterGUI() {
//        return nxfilterGUI;
//    }
//
//
//    /**
//     * Define implementation of NexradInterface
//     *
//     * @return    The type value
//     */
//    public int getType() {
//        return WCTUiInterface.EXPORT;
//    }
//
//
//
//
//    public WCTExport getNexradExport() {
//        return exporter;
//    }
//
//
//
//    public static void registerIOSPs() {
//        try {
////            NetcdfFile.registerIOProvider(gov.noaa.ncdc.iosp.area.AreaIosp.class);
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.area.AreaIosp.class);
//
//        } catch (Exception e) {
//            System.err.println("Error registering gov.noaa.ncdc.iosp.area.AreaIosp");
//        }
//    }
//
//
//    /**
//     *  Description of the Method
//     *
//     * @param  args  Description of the Parameter
//     */
//    public static void main(String args[]) {
//
//        registerIOSPs();
//        
//        try {
//
//        if (args.length != 0) {
//            // Start in batch mode
//            WCTExportBatch exportBatch = new WCTExportBatch();
//            exportBatch.runBatchMode(args);
//        }
//        else {
//            WCTViewerSplash viewer = new WCTViewerSplash();
//        }
//        
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
//
//
//
//
//    private class RasterProperties extends JDialog implements ActionListener {
//
//
//
//
//        // Raster properties gui stuff
//        private JComboBox gridCellSizeDropDown = new JComboBox(new Object[]{"Auto", "0.0075", "0.0100", "0.0125", "0.0150", "0.0175", "0.0200", "0.0250"});
//        private JComboBox gridSizeDropDown = new JComboBox(new Object[]{"Auto", "400", "600", "800", "1000", "1200", "1400", "1600"});
////        private JCheckBox variableResCheckBox = new JCheckBox();
//        private JTextField noDataTextField = new JTextField();
//        private JSpinner smoothFactorSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
//
//        private JButton okButton = new JButton("  OK  ");
//
//        private WCTExportGUI exportGUI;
//
//        private RasterProperties(WCTExportGUI exportGUI, String title, boolean modal) {
//            super(exportGUI, title, modal);
//            this.exportGUI = exportGUI;
//
//            createGUI();
//        }
//
//
//
//        /**
//         *  Description of the Method
//         */
//        private void createGUI() {
//
//            WCTExport exporter = exportGUI.getNexradExport();
//            // set up initial values
//            gridCellSizeDropDown.setSelectedItem(new String(""+exporter.getExportGridCellSize()));
//            gridCellSizeDropDown.setEditable(true);
//            gridSizeDropDown.setSelectedItem(new String(""+exporter.getExportGridSize()));
//            gridSizeDropDown.setEditable(true);
//            noDataTextField.setText(fmt2.format(exporter.getExportGridNoData()));
//            smoothFactorSpinner.setValue(new Integer(exporter.getExportGridSmoothFactor()));
//
//
//            JPanel mainPanel = new JPanel();
//            JPanel panel0 = new JPanel();
//            JPanel panel1 = new JPanel();
//            JPanel panel2 = new JPanel();
//            JPanel panel3 = new JPanel();
//            JPanel panel4 = new JPanel();
//            JPanel panel5 = new JPanel();
//
//            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
//            mainPanel.setLayout(new GridLayout(6, 1));
//            mainPanel.add(new JLabel("Raster Export Properties", JLabel.CENTER));
//            panel0.setLayout(new GridLayout(1, 2));
//            panel0.add(new JLabel("Grid Cell Size (deg): "));
//            panel0.add(gridCellSizeDropDown);
//            panel1.setLayout(new GridLayout(1, 2));
//            panel1.add(new JLabel("Square Grid Size: "));
//            panel1.add(gridSizeDropDown);
////            panel2.setLayout(new GridLayout(1, 2));
////            panel2.add(new JLabel("Variable Resolution: "));
////            panel2.add(variableResCheckBox);
//            panel3.setLayout(new GridLayout(1, 2));
//            panel3.add(new JLabel("No Data Value: "));
//            panel3.add(noDataTextField);
//            panel4.setLayout(new GridLayout(1, 2));
//            panel4.add(new JLabel("Smoothing Factor: "));
//            panel4.add(smoothFactorSpinner);                  
//
//            panel5.add(okButton);
//            okButton.addActionListener(this);
//
//            panel0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel4.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            mainPanel.add(panel0);
//            mainPanel.add(panel1);
//            mainPanel.add(panel3);
//            mainPanel.add(panel2);
//            mainPanel.add(panel4);
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));
//            getContentPane().add(mainPanel);
//            getContentPane().add(panel5);
//
//
//        }
//
//
//        public void actionPerformed(ActionEvent e) {
//
//
//            // set values in exporter
//            WCTExport exporter = exportGUI.getNexradExport();
//            if (gridCellSizeDropDown.getSelectedItem().toString().equals("Auto") && gridSizeDropDown.getSelectedItem().toString().equals("Auto")) {
//                String message = "Both the cell size and grid size cannot be set to auto!";
//                JOptionPane.showMessageDialog(this, message, "RASTER PROPERTIES", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            
//            if (gridSizeDropDown.getSelectedItem().toString().equals("Auto")) {
//                exporter.setExportGridCellSize(Double.parseDouble(gridCellSizeDropDown.getSelectedItem().toString()));
//            }
//            else if (gridCellSizeDropDown.getSelectedItem().toString().equals("Auto")) {
//                exporter.setExportGridSize(Integer.parseInt(gridSizeDropDown.getSelectedItem().toString()));
//            }
//            else {
//                String message = "Either the cell size and grid size must be set to auto!";
//                JOptionPane.showMessageDialog(this, message, "RASTER PROPERTIES", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            
//            
////            exporter.setExportGridVariableRes(variableResCheckBox.isSelected());
//            exporter.setExportGridNoData(Float.parseFloat(noDataTextField.getText()));
//            exporter.setExportGridSmoothFactor(Integer.parseInt(smoothFactorSpinner.getValue().toString()));
//
//            setVisible(false);
//        }
//
//
//
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
//
//    class SubmitButtonListener implements ActionListener {
//
//        private final WCTExportGUI parent;
//
//        public SubmitButtonListener(WCTExportGUI parent) {
//            this.parent = parent;
//        }
//
//
//
//        public void actionPerformed(ActionEvent evt) {
//
//
//            String dataType = dataSourcePanel.getDataType();
//
//                        
//            try {
//
//                // 1st - check for special case of single file
//                if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
//                    String source = dataSourcePanel.getDataLocation();
//                    System.out.println(source);
//                    URL url = null;
//                    if (source.startsWith("http://") || source.startsWith("ftp://")) {
//                        url = new URL(source);
//                    }
//                    else {
//                        url = new File(source).toURI().toURL();
//                    }
//                    exportData(url);
//                    return;
//                }
//
//                dataSourcePanel.setStatus("Listing Data ...", dataType);
//
////              From: http://foxtrot.sourceforge.net/docs/tips.php
////              Avoid the temptation to modify anything from inside Job.run(). 
////              It should just take data from outside, perform some heavy operation 
////              and return the result of the operation.
////              The pattern to follow in the implementation of Job.run() is Compute and Return, see example below.  
//
//                final WCTDirectoryScanner dirScanner = new WCTDirectoryScanner();
//                
//                ScanResults[] scanResults = (ScanResults[])foxtrot.ConcurrentWorker.post(new foxtrot.Task() {
//                    @Override
//                    public Object run() throws Exception {
//                        return dirScanner.listFiles(dataSourcePanel.getDataType(), dataSourcePanel.getDataLocation());
//                    }
//                });
//
//                // Update history
//                dataSourcePanel.updateHistory();
//
//
//                parent.setScanResults(scanResults);
//
//
//                if (scanResults.length == 0) {
//                    javax.swing.JOptionPane.showMessageDialog(parent, "No data found for: "+dataSourcePanel.getDataLocation(), 
//                            "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(parent, "ADD DATA ERROR:\n"+e, "ADD DATA ERROR", JOptionPane.ERROR_MESSAGE);         
//            }
//
//            dataSourcePanel.setStatus("", dataType);
//        }
//    }
//    
//
//    final class KeyHandler implements KeyListener {
//
//        /**
//         *  Implementation of KeyListener interface.
//         *
//         * @param  e  Description of the Parameter
//         */
//        public void keyPressed(KeyEvent e) {
////          System.out.println(e.getKeyCode()+"  PRESSED");
//        }
//        /**
//         *  Implementation of KeyListener interface.
//         *
//         * @param  e  Description of the Parameter
//         */
//        public void keyReleased(KeyEvent e) {
////          System.out.println(e.getKeyCode()+"  ESCAPE=" + KeyEvent.VK_ESCAPE+" ENTER="+KeyEvent.VK_ENTER+" D="+KeyEvent.VK_D);
////          System.out.println(e.getModifiers()+"  CTRL=" + KeyEvent.CTRL_DOWN_MASK);
//            if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_D) {                
//                showDump(getSelectedURL());
//            }
//        }
//
//        /**
//         *  Implementation of KeyListener interface.
//         *
//         * @param  e  Description of the Parameter
//         */
//        public void keyTyped(KeyEvent e) {
//        }
//
//    }
//    
//    
//    
//    
//    
//    
//    
//    final class FilterKeyListener implements KeyListener {
//
//        private final WCTExportGUI parent;
//
//        public FilterKeyListener(WCTExportGUI parent) {
//            this.parent = parent;
//        }
//        
//        
//		@Override
//		public void keyPressed(KeyEvent e) {
//		}
//		@Override
//		public void keyReleased(KeyEvent e) {
////			resultsList.
//			System.out.println("I'm sorting on "+((JTextField) e.getSource()).getText());
//			
//		}
//		@Override
//		public void keyTyped(KeyEvent e) {			
//		}    	
//    }
//
//
//    
//}

