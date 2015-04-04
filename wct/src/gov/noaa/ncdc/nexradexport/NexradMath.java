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
//package gov.noaa.ncdc.nexradexport;
//
//import gov.noaa.ncdc.common.SwingWorker;
//import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
//import gov.noaa.ncdc.nexradiv.legend.NexradLegendLabelFactory;
//import gov.noaa.ncdc.wct.WCTFilter;
//import gov.noaa.ncdc.wct.WCTProperties;
//import gov.noaa.ncdc.wct.decoders.DecodeException;
//import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
//import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
//import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
//import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
//import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
//import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
//import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
//import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
//import gov.noaa.ncdc.wct.export.ScanResultsManager;
//import gov.noaa.ncdc.wct.export.WCTExport.ExportFormatType;
//import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
//import gov.noaa.ncdc.wct.export.raster.RasterMathOp;
//import gov.noaa.ncdc.wct.export.raster.RasterMathOpException;
//import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
//import gov.noaa.ncdc.wct.export.raster.WCTRaster;
//import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
//import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
//import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
//import gov.noaa.ncdc.wct.io.FileScanner;
//import gov.noaa.ncdc.wct.io.ScanResults;
//import gov.noaa.ncdc.wct.io.ScanResultsComparator;
//import gov.noaa.ncdc.wct.io.SupportedDataType;
//import gov.noaa.ncdc.wct.io.WCTDirectoryScanner;
//import gov.noaa.ncdc.wct.io.WCTTransfer;
//import gov.noaa.ncdc.wct.ui.DataSelectorInterface;
//import gov.noaa.ncdc.wct.ui.DataSourcePanel;
//import gov.noaa.ncdc.wct.ui.WCTFrame;
//import gov.noaa.ncdc.wct.ui.WCTUiInterface;
//import gov.noaa.ncdc.wct.ui.WCTUiUtils;
//import gov.noaa.ncdc.wct.ui.WCTViewer;
//import gov.noaa.ncdc.wct.ui.event.SortByListener;
//
//import java.awt.BorderLayout;
//import java.awt.Font;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.image.WritableRaster;
//import java.io.File;
//import java.io.IOException;
//import java.net.ConnectException;
//import java.net.URL;
//import java.sql.SQLException;
//import java.text.DecimalFormat;
//import java.text.ParseException;
//import java.util.Vector;
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
//import javax.swing.JFrame;
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
//import javax.swing.JTextField;
//import javax.swing.KeyStroke;
//import javax.swing.ListSelectionModel;
//import javax.swing.event.ListSelectionEvent;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.XPathExpressionException;
//
//import org.geotools.gc.GridCoverage;
//import org.xml.sax.SAXException;
//
//import ucar.nc2.dt.RadialDatasetSweep;
//
///**
// *  Performs simple math operations between rasterized data and allows export to Raster formats.
// *
// * @author     steve.ansari
// * @created    July 9, 2004
// */
////public class NexradMath extends NexradFrame implements NexradInterface, ActionListener, ListSelectionListener {
//public class NexradMath extends WCTFrame 
//   implements ScanResultsManager, WCTUiInterface, DataSelectorInterface, ActionListener {
//
//
//   // JMenu Declarations
//   private JMenuBar jMenuBar;
//   private JMenu jmFile;
//   private JMenuItem jmiFileExit;
//   private JMenu jmFormat;
//   private JMenuItem jmiFormatVector, jmiFormatRaster;
//   private JRadioButtonMenuItem jmiFormatNative, jmiFormatShapefile, jmiFormatWKT, jmiFormatGeoTIFF, jmiFormatESRIAscii;
//   private JRadioButtonMenuItem jmiFormatESRIFlt, jmiFormatGrADS, jmiFormatHDF, jmiFormatNetCDF;
//   private JSeparator jmiFormatSeparator;
//   private JMenuItem jmiOptionsRaster, jmiOptionsApplyPolyR, jmiOptionsKeepWMO;
//   private JRadioButtonMenuItem jmiOptionsApplyPolyYes, jmiOptionsApplyPolyNo, jmiOptionsKeepWMOYes, jmiOptionsKeepWMONo;
//   private JSeparator jmiOptionsSeparator2;
//   private JMenu jmOptions;
//   private JRadioButtonMenuItem jmiOpMax, jmiOpMin, jmiOpAbsMax, jmiOpAvg, jmiOpSum;
//   private JMenu jmMathOp;
//   private JMenuItem jMenuItemH1, jMenuItemH2;
//   private JMenu jmHelp;
//
//   private JPanel choicePanel;
//   private JTextField jText, jOpText;
//   private JButton jbCancel, jbBrowse, jbDescribe;
//   private final JButton jbExport = new JButton("Export");
//   private final JButton jbExportView = new JButton("Export View");
//   private final JButton jbView = new JButton("View");
//
//   private JCheckBox jcbExtent;
//   private String saveDirectory = "";
//
//   // Raster properties gui stuff
//   private JDialog rPropFrame = null;
//   private JComboBox gridSizeDropDown = new JComboBox(new Object[]{"400", "600", "800", "1000", "1200", "1400", "1600"});
//   private JCheckBox fitToViewerBox = new JCheckBox();
//   private JTextField noDataTextField = new JTextField();
//   private JButton okButton = new JButton("  OK  ");
//   private JButton jbBrowseLocal;
//   private String hasString = "";
//   private String localString = "";
//   private String customString = "";
//   private File lastFolder = null;
//   private Vector passingNexradFiles = new Vector();
//
//   private FileScanner nexradFile;
//   
//   private NexradHeader header;
//   private StreamingRadialDecoder decoder;
//   
//   private RadialDatasetSweep radialDataset;
//   private DecodeRadialDatasetSweepHeader radialDatasetHeader;
//   private DecodeRadialDatasetSweep radialDatasetDecoder;
//   
//   private DecodeL3Header level3Header;
//   private DecodeXMRGHeader xmrgHeader;
//   
//   private DecodeL3Nexrad level3Decoder;
//   private DecodeXMRGData xmrgDecoder;
//   private DecodeL3Alpha alpha_decoder;
////   private WCTFilterGUI nxfilterGUI = new WCTFilterGUI("Math Filter", this, true);
//   private WCTFilter nxfilter;
//
//   private JProgressBar progress;
//
//   private String exportType = "GeoTIFF";
//   private int loopIndexValue = 0;
//
//   private DecimalFormat fmt03 = new DecimalFormat("000");
//
//   private boolean reducePolys;
//   private boolean wmoFormat;
//   private int gridSize;
//   private float gridNoData;
//   private boolean fitToViewer;
//
//   private WCTRaster rasterizer = null;
//   private WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
//   private WritableRaster processRaster = null;
//   private java.awt.geom.Rectangle2D.Double processBounds = null;
//   private NexradMetaDataExtract nexradMetaDataExtract = null;
//
//   private DataSourcePanel dataSourcePanel;
//   private boolean mathDisplay = false;
//   private int[] currentMathIndices = new int[0];
//   private boolean exitOnClose;
//
//   private String startTime;
//   
//   private boolean operationExport;
//   /**
//    *  Description of the Field
//    */
//   public final static int MATH_OP_MAX = 0;
//   /**
//    *  Description of the Field
//    */
//   public final static int MATH_OP_MIN = 1;
//   /**
//    *  Description of the Field
//    */
//   public final static int MATH_OP_ABSMAX = 2;
//   /**
//    *  Description of the Field
//    */
//   public final static int MATH_OP_AVERAGE = 3;
//   /**
//    *  Description of the Field
//    */
//   public final static int MATH_OP_SUM = 4;
//
//   private WCTViewer nexview = null;
//
//   
//   
//   private final JList resultsList = new JList();
//   private final DefaultListModel listModel = new DefaultListModel();
//   private final WCTDirectoryScanner dirScanner = new WCTDirectoryScanner();
//   private ScanResults[] scanResults = null;
//
//
//   /**
//    *Default Constructor for the NexradMath object -- will exit on close!
// * @throws ParserConfigurationException 
// * @throws IOException 
// * @throws SAXException 
// * @throws XPathExpressionException 
// * @throws NumberFormatException 
//    */
//   public NexradMath() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//      this(true);
//   }
//
//
//   /**
//    *Constructor for the NexradMath object
//    *
//    * @param  exitOnClose  Description of the Parameter
// * @throws ParserConfigurationException 
// * @throws IOException 
// * @throws SAXException 
// * @throws XPathExpressionException 
// * @throws NumberFormatException 
//    */
//   public NexradMath(boolean exitOnClose) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//      super("NEXRAD Math");
//      this.exitOnClose = exitOnClose;
//      if (exitOnClose) {
//         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//      }
//      loadSettings();
//      createGUI();
//      pack();
//      setVisible(true);
//      
//      nexradFile = new FileScanner();
//   }
//
//
//   /**
//    *Constructor for the NexradMath object for use with NexradIAViewer
//    *
//    * @param  nexview  Instance of NexradIAViewer
// * @throws ParserConfigurationException 
// * @throws IOException 
// * @throws SAXException 
// * @throws XPathExpressionException 
// * @throws NumberFormatException 
//    */
//   public NexradMath(WCTViewer nexview) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//      this(false);
//      this.nexview = nexview;
//   }
//
//
//   private void loadSettings() {
//      saveDirectory = WCTProperties.getWCTProperty("jne_export_dir");
//      if (saveDirectory == null) {
//         saveDirectory = "";
//      }      
//   }
//   
//   
//   /**
//    *  Description of the Method
//    */
//   private void createGUI() {
//
//      // Set up JMenu Menus
//      jMenuBar = new JMenuBar();
//      jmFile = new JMenu("File");
//      jmFile.setMnemonic(KeyEvent.VK_F);
//      jmiFileExit = new JMenuItem("Exit", KeyEvent.VK_X);
//      jmiFileExit.addActionListener(this);
//      if (!exitOnClose) {
//         jmiFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
//      }
//      jmFormat = new JMenu("Format");
//      jmFormat.setMnemonic(KeyEvent.VK_M);
//      jmiFormatNative = new JRadioButtonMenuItem("Native Nexrad");
//      jmiFormatNative.setMnemonic(KeyEvent.VK_N);
//      jmiFormatVector = new JMenuItem("<html><b>Vector (Polygon)</b></html>");
//      jmiFormatShapefile = new JRadioButtonMenuItem("Shapefile", true);
//      jmiFormatShapefile.setMnemonic(KeyEvent.VK_S);
//      jmiFormatWKT = new JRadioButtonMenuItem("Well-Known Text");
//      jmiFormatWKT.setMnemonic(KeyEvent.VK_W);
//      jmiFormatSeparator = new JSeparator();
//      jmiFormatRaster = new JMenuItem("<html><b>Raster</b></html>");
//      jmiFormatGeoTIFF = new JRadioButtonMenuItem("GeoTIFF", true);
//      jmiFormatGeoTIFF.setMnemonic(KeyEvent.VK_G);
//      jmiFormatESRIAscii = new JRadioButtonMenuItem("ESRI ASCII Grid");
//      jmiFormatESRIAscii.setMnemonic(KeyEvent.VK_A);
//      jmiFormatESRIFlt = new JRadioButtonMenuItem("ESRI Binary Grid");
//      jmiFormatESRIFlt.setMnemonic(KeyEvent.VK_B);
//      jmiFormatGrADS = new JRadioButtonMenuItem("GrADS Binary");
//      jmiFormatGrADS.setMnemonic(KeyEvent.VK_G);
//      jmiFormatHDF = new JRadioButtonMenuItem("HDF");
//      jmiFormatHDF.setMnemonic(KeyEvent.VK_H);
//      jmiFormatNetCDF = new JRadioButtonMenuItem("NetCDF");
//      jmiFormatNetCDF.setMnemonic(KeyEvent.VK_C);
//
//      // Disable the unavailable exports
//      jmiFormatWKT.setEnabled(false);
//      //jmiFormatGeoTIFF.setEnabled(false);
//      jmiFormatHDF.setEnabled(false);
//      //jmiFormatNetCDF.setEnabled(false);
//
//      jmiFormatNative.addActionListener(this);
//      jmiFormatShapefile.addActionListener(this);
//      jmiFormatWKT.addActionListener(this);
//      jmiFormatGeoTIFF.addActionListener(this);
//      jmiFormatESRIAscii.addActionListener(this);
//      jmiFormatESRIFlt.addActionListener(this);
//      jmiFormatGrADS.addActionListener(this);
//      jmiFormatHDF.addActionListener(this);
//      jmiFormatNetCDF.addActionListener(this);
//
//      ButtonGroup buttonGroup1 = new ButtonGroup();
//      //buttonGroup1.add(jmiFormatNative);
//      //buttonGroup1.add(jmiFormatShapefile);
//      //buttonGroup1.add(jmiFormatWKT);
//      buttonGroup1.add(jmiFormatGeoTIFF);
//      buttonGroup1.add(jmiFormatESRIAscii);
//      buttonGroup1.add(jmiFormatESRIFlt);
//      buttonGroup1.add(jmiFormatGrADS);
//      buttonGroup1.add(jmiFormatHDF);
//      buttonGroup1.add(jmiFormatNetCDF);
//
//      jmOptions = new JMenu("Options");
//      jmOptions.setMnemonic(KeyEvent.VK_O);
//      jmiOptionsRaster = new JMenuItem("Raster Options", KeyEvent.VK_R);
//      jmiOptionsRaster.addActionListener(this);
//      jmiOptionsApplyPolyR = new JMenuItem("Apply Polygon Reduction?");
//      jmiOptionsApplyPolyYes = new JRadioButtonMenuItem("Yes");
//      jmiOptionsApplyPolyNo = new JRadioButtonMenuItem("No", true);
//      jmiOptionsSeparator2 = new JSeparator();
//      jmiOptionsKeepWMO = new JMenuItem("Keep WMO File Format?");
//      jmiOptionsKeepWMOYes = new JRadioButtonMenuItem("Yes");
//      jmiOptionsKeepWMONo = new JRadioButtonMenuItem("No", true);
//
//      ButtonGroup buttonGroup3 = new ButtonGroup();
//      buttonGroup3.add(jmiOptionsApplyPolyYes);
//      buttonGroup3.add(jmiOptionsApplyPolyNo);
//      ButtonGroup buttonGroup4 = new ButtonGroup();
//      buttonGroup4.add(jmiOptionsKeepWMOYes);
//      buttonGroup4.add(jmiOptionsKeepWMONo);
//
//      jmMathOp = new JMenu("Math");
//      jmMathOp.setMnemonic(KeyEvent.VK_M);
//      jmiOpMax = new JRadioButtonMenuItem("Max", true);
//      jmiOpMin = new JRadioButtonMenuItem("Min");
//      jmiOpAbsMax = new JRadioButtonMenuItem("Abs Max");
//      jmiOpAvg = new JRadioButtonMenuItem("Average");
//      jmiOpSum = new JRadioButtonMenuItem("Sum");
//
//      ButtonGroup buttonGroup5 = new ButtonGroup();
//      buttonGroup5.add(jmiOpMax);
//      buttonGroup5.add(jmiOpMin);
//      buttonGroup5.add(jmiOpAbsMax);
//      buttonGroup5.add(jmiOpAvg);
//      buttonGroup5.add(jmiOpSum);
//
//      jMenuItemH1 = new JMenuItem("About", KeyEvent.VK_A);
//      jMenuItemH1.addActionListener(this);
//      jMenuItemH2 = new JMenuItem("Help", KeyEvent.VK_H);
//      jMenuItemH2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
//      jMenuItemH2.addActionListener(this);
//      jMenuItemH2.setEnabled(false);
//
//      jmHelp = new JMenu("Help");
//      jmHelp.setMnemonic(KeyEvent.VK_H);
//      jmFile.add(jmiFileExit);
//      jmMathOp.add(jmiOpMax);
//      jmMathOp.add(jmiOpMin);
//      jmMathOp.add(jmiOpAbsMax);
//      jmMathOp.add(jmiOpAvg);
//      jmMathOp.add(jmiOpSum);
//      //jmFormat.add(jmiFormatNative);
//      //jmFormat.add(new JSeparator());
//      //jmFormat.add(jmiFormatVector);
//      //jmFormat.add(jmiFormatShapefile);
//      //jmFormat.add(jmiFormatWKT);
//      //jmFormat.add(jmiFormatSeparator);
//      jmFormat.add(jmiFormatRaster);
//      jmFormat.add(jmiFormatGeoTIFF);
//      jmFormat.add(jmiFormatESRIAscii);
//      jmFormat.add(jmiFormatESRIFlt);
//      jmFormat.add(jmiFormatGrADS);
//      jmFormat.add(jmiFormatHDF);
//      jmFormat.add(jmiFormatNetCDF);
//      jmOptions.add(jmiOptionsRaster);
//      //jmOptions.add(new JSeparator());
//      //jmOptions.add(jmiOptionsApplyPolyR);
//      //jmOptions.add(jmiOptionsApplyPolyYes);
//      //jmOptions.add(jmiOptionsApplyPolyNo);
//      //jmOptions.add(jmiOptionsSeparator2);
//      //jmOptions.add(jmiOptionsKeepWMO);
//      //jmOptions.add(jmiOptionsKeepWMOYes);
//      //jmOptions.add(jmiOptionsKeepWMONo);
//      jmHelp.add(jMenuItemH1);
//      jmHelp.add(jMenuItemH2);
//      jMenuBar.add(jmFile);
//      jMenuBar.add(jmMathOp);
////      jMenuBar.add(jmFormat);
//      jMenuBar.add(jmOptions);
//      jMenuBar.add(jmHelp);
//
//
//      this.setJMenuBar(jMenuBar);
//
//      jText = new JTextField(10);
//      String jnxpropHas = WCTProperties.getWCTProperty("ncdc_hasnum");
//      if (jnxpropHas != null) {
//         jText.setText(jnxpropHas);
//      }
//      jbBrowseLocal = new JButton("Browse Local");
//      jbBrowseLocal.addActionListener(this);
//
//      // Create Action Buttons
//      choicePanel = new JPanel();
//      choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
//
//      jOpText = new JTextField(30);
//      jOpText.setText(saveDirectory);
//      jbBrowse = new JButton("Browse");
//      jbBrowse.addActionListener(this);
//
//      jbDescribe = new JButton("Describe");
//      jbDescribe.addActionListener(this);
//      jbView.addActionListener(this);
//      jbExport.addActionListener(this);
//      jbExportView.addActionListener(this);
//      jbExportView.setEnabled(false);
//      jbCancel = new JButton("Cancel");
//      jbCancel.addActionListener(this);
//      jbCancel.setEnabled(false);
//
//      JPanel opPanel = new JPanel();
//      opPanel.add(jbBrowse);
//      opPanel.add(jOpText);
//
//      JPanel excPanel = new JPanel();
////      excPanel.add(jbDescribe);
//      excPanel.add(jbView);
//      //excPanel.add(jbExportView);
////      excPanel.add(jbExport);
//      excPanel.add(jbCancel);
//
//      JLabel jlOpDir = new JLabel("Choose Output Directory", JLabel.LEFT);
//      //jlOpDir.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
//      jlOpDir.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
//
//      progress = new JProgressBar(0, 100);
//      progress.setString(exportType + " Export Status");
//      progress.setStringPainted(true);
//      progress.setBorder(BorderFactory.createEmptyBorder(12, 10, 2, 10));
//
//      JPanel junkPanel = new JPanel();
//      junkPanel.add(jlOpDir);
//      //choicePanel.add(jlOpDir);
//      choicePanel.add(junkPanel);
//      choicePanel.add(opPanel);
//      choicePanel.add(excPanel);
//      choicePanel.add(progress);
//
//      dataSourcePanel = new DataSourcePanel(new SubmitButtonListener(this), 
//    		  new SortByListener(this), new FilterKeyListener(this), false);
//      dataSourcePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//      resultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//      resultsList.setFont(new Font("monospaced", Font.PLAIN, 12));
//      resultsList.setModel(listModel);
//
//      JScrollPane resultsListScrollPane = new JScrollPane(resultsList);
//      resultsListScrollPane.setBorder(BorderFactory.createCompoundBorder(
//              BorderFactory.createEmptyBorder(2, 12, 2, 12),
//              BorderFactory.createEtchedBorder())
//           );
//
//      getContentPane().setLayout(new BorderLayout());
//      getContentPane().add(dataSourcePanel, "North");
//      getContentPane().add(resultsListScrollPane, "Center");
//      getContentPane().add(new JScrollPane(choicePanel), "South");
//
//      
//
//      // set up raster properties defaults
//      gridSizeDropDown.setSelectedItem("1000");
//      gridSizeDropDown.setEditable(true);
//      fitToViewerBox.setSelected(true);
//      noDataTextField.setText("-999.0");
//   }
//
//
//   /**
//    *  Gets the hASJobNumber attribute of the NexradMath object
//    *
//    * @return    The hASJobNumber value
//    */
//   public int getHASJobNumber() {
//      if (jText.getText().equals("")) {
//         return -1;
//      }
//      else {
//         return (Integer.parseInt(jText.getText()));
//      }
//   }
//
//   /**
//    * DataSelectorInterface implementation
//    */
//   public void doubleClickedList() {
//      //loadNexrad();
//   }
//   /**
//    * DataSelectorInterface implementation
//    */
//   public void enterKeyPressedFromList() {
//      //loadNexrad();
//   }
//   /**
//    * DataSelectorInterface implementation
//    */
//   public void listReloaded() {
//      //nexview.setAnimateEnabled(true);
//      //nexview.clearNexradAnimator();
//      //nexview.clearAlphaProperties();
//   }
//   
//   public void fileRequested(URL url) {
//       return;
//   }
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
//   public void setScanResults(ScanResults[] scanResults) {      
//       this.scanResults = scanResults;
//       if (scanResults == null) {
//           return;
//       }
//       WCTUiUtils.fillListModel(listModel, scanResults);
//   }
//
//   public ScanResults[] getScanResults() {
//       return this.scanResults;
//   }
//   
//   public ScanResultsComparator getSortByComparator() {
//       return dataSourcePanel.getSortByComparator();
//   }
//   
//   
//   
//   
//   
//   public URL getSelectedURL() {
//       return scanResults[resultsList.getSelectedIndex()].getUrl();
//   }
//
//   public URL getSelectedURL(int index) {
//       int[] indices = resultsList.getSelectedIndices();
//       return scanResults[indices[index]].getUrl();
//   }
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
//   // Implementation of ActionListener interface.
//   /**
//    *  Description of the Method
//    *
//    * @param  event  Description of the Parameter
//    */
//   public void actionPerformed(ActionEvent event) {
//
//      final Object source = event.getSource();
//      if (source == jbDescribe) {
//         final int[] indices = resultsList.getSelectedIndices();
//         if (nexradMetaDataExtract == null) {
//            nexradMetaDataExtract = new NexradMetaDataExtract();
//         }
//
//         for (int i = 0; i < indices.length; i++) {
//            try {
//               nexradMetaDataExtract.showDescribeNexradDialog(getSelectedURL(i), this);
//               //describeNexrad(files[indices[i]]);
//            } catch (Exception e) {
//               e.printStackTrace();
//               javax.swing.JOptionPane.showMessageDialog(null, "Metadata Error: "+e, "METADATA ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
//            }
//         }
//      }
//      else if (source == jbBrowse) {
//
//         // Export NEXRAD Shapefiles Dialog
//         // Set up File Chooser
//         JFileChooser fc = new JFileChooser(saveDirectory);
//         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//         fc.setDialogTitle("Choose Write Directory for Exported Data");
//         //fc.addChoosableFileFilter(new OpenFileFilter("shp", true, "ESRI Shapefiles (.shp/.shx/.dbf/.prj) "));
//
//         int returnVal = fc.showSaveDialog(jbBrowse);
//         if (returnVal == JFileChooser.APPROVE_OPTION) {
//            saveDirectory = fc.getSelectedFile().toString();
//            jOpText.setText(saveDirectory);
//            WCTProperties.setWCTProperty("jne_export_dir", saveDirectory);            
//         }
//      }
//      else if (source == jbExport || source == jbView || source == jbExportView) {
//         doExport(source);
//      }
//      else if (source == jbCancel) {
//         try {
//            progress.setString("Aborting Export...");
//            setLoopIndexValue(resultsList.getSelectedIndices().length);
//         } catch (NullPointerException npe) {}
//      }
//      else if (source == jmiFormatNative ||
//            source == jmiFormatShapefile || source == jmiFormatWKT ||
//            source == jmiFormatGeoTIFF || source == jmiFormatESRIAscii ||
//            source == jmiFormatESRIFlt || source == jmiFormatGrADS ||
//            source == jmiFormatHDF || source == jmiFormatNetCDF) {
//         exportType = ((JMenuItem) source).getText();
//         progress.setString(exportType + " Export Status");
//      }
//      else if (source == jmiOptionsRaster) {
//         if (rPropFrame == null) {
//            rPropFrame = new JDialog(this, "Raster Properties", true);
//            JPanel mainPanel = new JPanel();
//            JPanel panel1 = new JPanel();
//            JPanel panel2 = new JPanel();
//            JPanel panel3 = new JPanel();
//            JPanel panel4 = new JPanel();
//            rPropFrame.getContentPane().setLayout(new BoxLayout(rPropFrame.getContentPane(), BoxLayout.Y_AXIS));
//            mainPanel.setLayout(new GridLayout(4, 1));
//            mainPanel.add(new JLabel("Raster Export Properties", JLabel.CENTER));
//            panel1.setLayout(new GridLayout(1, 2));
//            panel1.add(new JLabel("Max Num. of Rows or Cols: "));
//            panel1.add(gridSizeDropDown);
//            panel2.setLayout(new GridLayout(1, 2));
//            JLabel fitLabel = new JLabel("Fit to Viewer Extent: ");
//            panel2.add(fitLabel);
//            panel2.add(fitToViewerBox);
//            panel3.setLayout(new GridLayout(1, 2));
//            panel3.add(new JLabel("No Data Value: "));
//            panel3.add(noDataTextField);
//            panel4.add(okButton);
//            okButton.addActionListener(this);
//
//            panel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            panel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//            mainPanel.add(panel1);
//            mainPanel.add(panel3);
//            mainPanel.add(panel2);
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));
//            rPropFrame.getContentPane().add(mainPanel);
//            rPropFrame.getContentPane().add(panel4);
//         }
//         rPropFrame.pack();
//         rPropFrame.setVisible(true);
//      }
//      else if (source == jMenuItemH1) {
//         String message = "Java NEXRAD Math\n" +
//               "Version " + WCTUiUtils.getVersion() + "\n" +
//               "Author: Steve Ansari\n" +
//               "National Climatic Data Center\n" +
//               "Contact: Steve.Ansari@noaa.gov";
//         JOptionPane.showMessageDialog(null, message,
//               "About", JOptionPane.INFORMATION_MESSAGE);
//      }
//      else if (source == jmiFileExit) {
//         if (exitOnClose) {
//            System.exit(1);
//         }
//         else {
//            this.dispose();
//         }
//      }
//      else if (source == okButton) {
//         rPropFrame.setVisible(false);
//      }
//
//   }
//
//
//
//
//
//
//   /**
//    *  Gets the loopIndexValue attribute of the NexradMath object
//    *
//    * @return    The loopIndexValue value
//    */
//   private int getLoopIndexValue() {
//      return loopIndexValue;
//   }
//
//
//   /**
//    *  Sets the loopIndexValue attribute of the NexradMath object
//    *
//    * @param  n  The new loopIndexValue value
//    */
//   private void setLoopIndexValue(int n) {
////System.out.println("SETTING LOOP INDEX VALUE :::::::::::::: "+n);      
//      loopIndexValue = n;
//   }
//
//
//   /**
//    *  Description of the Method
//    */
//   private void incrementLoopIndexValue() {
//      loopIndexValue++;
//   }
//
//
//   /**
//    *  Description of the Method
//    *
//    * @param  e  Description of the Parameter
//    */
//   public void valueChanged(ListSelectionEvent e) {
//// Break up menus here:   WSR   PRODUCT   DATE
//
//   }
//
//
//
//
//
//   /**
//    * Return status of Auto-Extent checkbox
//    *
//    * @return    The autoExtentSelected value
//    */
//   public boolean isAutoExtentSelected() {
//
//      return jcbExtent.isSelected();
//   }
//
//
//   /**
//    * Set status of Auto-Extent checkbox
//    *
//    * @param  selected  The new isAutoExtentSelected value
//    */
//   public void setIsAutoExtentSelected(boolean selected) {
//
//      jcbExtent.setSelected(selected);
//   }
//
//
//
//   /**
//    *  Sets the buttonsVisible attribute of the NexradMath object
//    *
//    * @param  visible  The new buttonsVisible value
//    */
//   private void setButtonsVisible(boolean visible) {
//      jbView.setEnabled(visible);
//      jbDescribe.setEnabled(visible);
//      jbExport.setEnabled(visible);
//      jbExportView.setEnabled(visible);
//   }
//
//
//   /**
//    *  Description of the Method
//    *
//    * @param  source  Description of the Parameter
//    */
//   private void doExport(final Object source) {
//
//      try {
//         
//         final int[] indices = resultsList.getSelectedIndices();
//
//         if (resultsList.getSelectedIndex() == -1) {
//            return;
//         }
//         else {
//            if (source == jbExport || source == jbExportView) {
//               operationExport = true;
//               saveDirectory = jOpText.getText();
//               if (saveDirectory.trim().length() == 0) {
//                  actionPerformed(new ActionEvent(jbBrowse, 0, "Browse"));
//               }
//               try {
//                  File test = new File(saveDirectory);
//                  if (!test.exists()) {
//                     throw new Exception();
//                  }
//               } catch (Exception e) {
//                  String message = "Invalid Directory: \n" +
//                        "<html><font color=red>" + saveDirectory + "</font></html>";
//                  JOptionPane.showMessageDialog(null, (Object) message,
//                        "DIRECTORY SELECTION ERROR", JOptionPane.ERROR_MESSAGE);
//                  return;
//               }
//            }
//            else {
//               operationExport = false;
//            }
//            
//            
//            
//            
//            /*
//            // save to check if we need to reprocess when exporting what has just been viewed
//            boolean equalIndices = true;
//            if (indices.length != currentMathIndices.length) {
//               equalIndices = false;
//            }
//            else {
//               for (int i=0; i<indices.length; i++) {
//                  if (indices[i] != currentMathIndices[i]) {
//                     equalIndices = false;
//                  } 
//               }
//            }
//            // save for next check 
//            currentMathIndices = indices;
//            */
//            progress.setString("Exporting " + indices.length + " files to " + exportType);
//
//            // Extract variables before going into thread -- that way the values can't be changed while looping!
//            reducePolys = jmiOptionsApplyPolyYes.isSelected();
//            if (jmiFormatNative.isSelected()) {
//               wmoFormat = true;
//               // always use wmo format with native export
//            }
//            else {
//               wmoFormat = jmiOptionsKeepWMOYes.isSelected();
//            }
//            gridSize = Integer.parseInt((String) gridSizeDropDown.getSelectedItem());
//            fitToViewer = fitToViewerBox.isSelected();
//
//            try {
//               gridNoData = Float.parseFloat((String) noDataTextField.getText());
//            } catch (Exception e) {
//               // check for non-numbers
//               JOptionPane.showMessageDialog(null, "The NO_DATA value must be a number!\nYou entered: " +
//                     noDataTextField.getText(), "NO DATA NUMBER ERROR", JOptionPane.ERROR_MESSAGE);
//               return;
//            }
//
//            // re-nullify the rasterizer to allow for changing raster sizes but no
//            // reconstruction if muliple files are processed at once.
//            rasterizer = null;
//
//            // Put in thread
//            SwingWorker worker =
//               new SwingWorker() {
//                  public Object construct() {
//
//                     //int n;
//                     int cnt = 0;
//
//                     try {
//
//                        setButtonsVisible(false);
//                        jbCancel.setEnabled(true);
//
//
//                        URL[] nexrad_urls = new URL[indices.length];
//                        int n = 0;
//                        
//                        setLoopIndexValue(0);
//                        while ((n = getLoopIndexValue()) < indices.length) {
//
//                           //for (int n = 0; n < indices.length; n++) {
//                           try {
//                              nexrad_urls[n] = getSelectedURL(n);
//                           } catch (Exception e) {
//                              JOptionPane.showMessageDialog(null, "Unable to connect to this file: \n" + listModel.get(indices[n]),
//                                    "NEXRAD CONNECTION ERROR", JOptionPane.ERROR_MESSAGE);
//                              e.printStackTrace();
//                              return "ERROR";
//                           }
//                           incrementLoopIndexValue();
//
//                        }
//
//                        String op = "";
//                        if (jmiOpMax.isSelected()) {
//                           if (source != jbExportView) {
//                              processNexradMathOp(nexrad_urls, MATH_OP_MAX, progress);
//                           }
//                           op = "MAX";
//                        }
//                        else if (jmiOpMin.isSelected()) {
//                           if (source != jbExportView) {
//                              processNexradMathOp(nexrad_urls, MATH_OP_MIN, progress);
//                           }
//                           op = "MIN";
//                        }
//                        else if (jmiOpAbsMax.isSelected()) {
//                           if (source != jbExportView) {
//                              processNexradMathOp(nexrad_urls, MATH_OP_ABSMAX, progress);
//                           }
//                           op = "ABSMAX";
//                        }
//                        else if (jmiOpAvg.isSelected()) {
//                           if (source != jbExportView) {
//                              processNexradMathOp(nexrad_urls, MATH_OP_AVERAGE, progress);
//                           }
//                           op = "AVG";
//                        }
//                        else if (jmiOpSum.isSelected()) {
//                           if (source != jbExportView) {
//                              processNexradMathOp(nexrad_urls, MATH_OP_SUM, progress);
//                           }
//                           op = "SUM";
//                        }
//
//                        progress.setValue(indices.length);
//
//                        String extension = "";
//                        if (jmiFormatGeoTIFF.isSelected()) {
//                           extension = ".tif";
//                        }
//                        else if (jmiFormatESRIAscii.isSelected()) {
//                           extension = ".asc";
//                        }
//                        else if (jmiFormatESRIFlt.isSelected()) {
//                           extension = ".flt";
//                        }
//                        else if (jmiFormatGrADS.isSelected()) {
//                           extension = ".grd";
//                        }
//                        else if (jmiFormatHDF.isSelected()) {
//                           extension = ".hdf";
//                        }
//                        else if (jmiFormatNetCDF.isSelected()) {
//                           extension = ".nc";
//                        }
//                        
//                        int opcnt = 0;
//                        
//                        nexradFile.scanURL(getSelectedURL(0));
//                        
//                        // Basic default
//                        String name = nexradFile.getLastScanResult().getDisplayName()+"_mathop";
//                        
//                        
//                        if (nexradFile.getLastScanResult().getDisplayName() != null) {
//                            name = nexradFile.getLastScanResult().getDisplayName().substring(0, 4) +
//                              "_" + op + "_" + nexradFile.getLastScanResult().getProductID() +
//                              "_" + fmt03.format(opcnt);
//                        }
//                        
//
//                        File f = new File(saveDirectory + "/" + name + extension);
//                        while (f.exists()) {
//                           name = nexradFile.getLastScanResult().getDisplayName().substring(0, 4) +
//                                 "_" + op + "_" + nexradFile.getLastScanResult().getProductID() +
//                                 "_" + fmt03.format(++opcnt);
//
//                           f = new File(saveDirectory + "/" + name + extension);
//
//                        }
//
//                        if (source == jbExport || source == jbExportView) {
//                           progress.setString("--- Saving Grid ---");
//                           exportNexrad(f);
//                        }
//                        else if (source == jbView) {
//                           sendNexradToViewer();
//                        }
//
//                     } catch (Exception e) {
//                        e.printStackTrace();
//                        setButtonsVisible(true);
//                     }
//                     progress.setString("Completed Export of " + indices.length + " files");
//                     jbCancel.setEnabled(false);
//                     try {
//                        Thread.sleep(1500);
//                     } catch (Exception e) {}
//                     progress.setValue(0);
//                     progress.setString(exportType + " Export Status");
//                     setButtonsVisible(true);
//                     return "Done";
//                  }
//               };
//            worker.start();
//
//         }
//         // END else
//      }
//      // END try
//      catch (Exception e) {
//         listModel.clear();
//         listModel.add(0, "------ ERROR: UNABLE TO LOAD DATA ------");
//         listModel.add(1, e.toString());
//      }
//
//   }
//
//
//   public GridCoverage getCurrentGridCoverage() {
//       return gcSupport.getGridCoverage(rasterizer, header, true, 
//               NexradColorFactory.getTransparentColors(header.getProductCode(), true, 
//               nexview.getRadarTransparency()));            
//
//   }
//   
//   /**
//    *  Description of the Method
//    */
//   private void sendNexradToViewer() {
//
//      // temporarily turn off auto-refresh
//      nexview.getMapPaneZoomChange().setRadarActive(false);
//      
//      jbExportView.setEnabled(true);
//      
//      final int[] indices = resultsList.getSelectedIndices();
//      
//      rasterizer.setWritableRaster(processRaster);         
//      /*
//      if (header.getProductType() == NexradHeader.L3DPA ||
//         header.getProductType() == NexradHeader.XMRG) {
//
//         nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header, false, 
//            NexradColorFactory.getTransparentColors(header.getProductCode(), false, 
//            nexview.getNexradTransparency())));
//            
//      }
//      else {
//         */
//         
//      // Are we smoothing?
//      gcSupport.setSmoothFactor(nexview.getRadarSmoothFactor());
//         
//         nexview.setRadarGridCoverage(gcSupport.getGridCoverage(rasterizer, header, true, 
//            NexradColorFactory.getTransparentColors(header.getProductCode(), true, 
//            nexview.getRadarTransparency())));            
//      //}
//            
//      nexview.setRadarGridCoverageVisibility(true);
//
//      String op = "";
//      if (jmiOpMax.isSelected()) {
//         op = "MAX";
//      }
//      else if (jmiOpMin.isSelected()) {
//         op = "MIN";
//      }
//      else if (jmiOpAbsMax.isSelected()) {
//         op = "ABMAX";
//      }
//      else if (jmiOpAvg.isSelected()) {
//         op = "AVG";
//      }
//      else if (jmiOpSum.isSelected()) {
//         op = "SUM";
//      }
//      
//      CategoryLegendImageProducer legend = nexview.getRadarLegendImageProducer();
//      
//      // Update legend with standard information
//      //NexradLegendLabelFactory.setStandardLegendLabels(nexview.getMetaLabels(), begHeader, files[indices[0]],
//      //      NexradLegendLabelFactory.DMS);
//      //String begdate = nexview.getMetaLabels()[2].getText();
//      NexradLegendLabelFactory.setStandardLegendLabels(legend, header, scanResults[indices[indices.length - 1]].getDisplayName(),
//            NexradLegendLabelFactory.DMS);
//            
//      if (header.getProductType() == NexradHeader.L3RADIAL ||
//          header.getProductType() == NexradHeader.L3RASTER ||
//          header.getProductType() == NexradHeader.L3DPA) {
//
//         NexradLegendLabelFactory.setSpecialLevel3LegendLabels(legend, header, false);
//      }
//      else if (header.getProductType() == NexradHeader.XMRG) {
//         NexradLegendLabelFactory.setSpecialXMRGLegendLabels(legend, (DecodeXMRGHeader)header, (DecodeXMRGData)decoder);
////         labels[3].setText("");
////         labels[4].setText("");
////         labels[5].setText("");
////         labels[6].setText(" ");
////         labels[7].setText(" ");
//      }
////      String str = labels[0].getText();
////      if (str.startsWith("MAX") || str.startsWith("MIN") ||
////         str.startsWith("ABMAX") || str.startsWith("AVG") ||
////         str.startsWith("SUM")) {
////            
////         int startindex = str.indexOf(" ");   
////         labels[0].setText(op + " " + labels[0].getText().substring(0, startindex));
////      }
////      else {
////         labels[0].setText(op + " " + labels[0].getText());
////      }
////      labels[8].setText("NEXRAD MATH OPERATION");
////      labels[9].setText("B: " + startTime);
////      labels[10].setText("E: " + labels[2].getText());
////      labels[2].setText("");
////      nexview.setNexradLegendHeader(header, legend);
//      nexview.setLegendVisibility(true);
//
//
//      // we are displaying the math result
//      mathDisplay = true;
//
//
//   }
//
//   public void setMathDisplayed(boolean mathDisplay) {
//      this.mathDisplay = mathDisplay;
//      if (! mathDisplay) {
//         jbExportView.setEnabled(false);
//      }
//   }
//   public boolean isMathDisplayed() {
//      return mathDisplay;
//   }
//   public WCTRasterizer getRasterizer() {
//      return (WCTRasterizer)rasterizer;
//   }
//   
//
//   /**
//    * Process NEXRAD Data Math Operations.
//    *
//    * @param  nexradURLs                     Array of Nexrad File URLs
//    * @param  mathOperation                  Description of the Parameter
//    * @exception  DecodeException      Description of the Exception
//    * @exception  FeatureRasterizerException  Description of the Exception
// * @throws SQLException 
// * @throws ParseException 
//    */
//   public void processNexradMathOp(URL[] nexradURLs, int mathOperation)
//          throws IOException, DecodeException, FeatureRasterizerException, ConnectException, SQLException, ParseException {
//      processNexradMathOp(nexradURLs, mathOperation, new JProgressBar());
//   }
//
//
//   /**
//    * Process NEXRAD Data Math Operations while updating supplied progress bar.
//    *
//    * @param  nexradURLs                     Array of Nexrad File URLs
//    * @param  mathOperation                  Description of the Parameter
//    * @param  progress                       Description of the Parameter
//    * @exception  DecodeException      Description of the Exception
//    * @exception  FeatureRasterizerException  Description of the Exception
// * @throws SQLException 
// * @throws ParseException 
//    */
//   public void processNexradMathOp(URL[] nexradURLs, int mathOperation, JProgressBar progress)
//          throws IOException, DecodeException, FeatureRasterizerException, ConnectException, SQLException, ParseException {
//
//      // Nullify old processRaster
//      processRaster = null;
//
//
//      boolean batchMode = false;
//      
//      
//      
//      
//
//
//      progress.setMaximum(nexradURLs.length);
//
//      java.awt.geom.Rectangle2D.Double lastBounds = null;
//      setLoopIndexValue(0);
//      for (int n = 0; n < nexradURLs.length; n++) {
//         progress.setString("Completed " + n + "/" + nexradURLs.length + " files ");
//         progress.setValue(n);
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
//         // Transfer Level-2 to temp directory for decompression if necessary
//         nexradFile.scanURL(nexradURLs[n]);
//         // Check for file compression
//         if (nexradFile.isZCompressed()) {
//            nexradURLs[n] = Level2Transfer.getNCDCLevel2UNIXZ(nexradURLs[n]);
//            nexradFile.scanURL(nexradURLs[n]);
//         }
//         else if (nexradFile.isGzipCompressed()) {
//            nexradURLs[n] = Level2Transfer.getNCDCLevel2GZIP(nexradURLs[n]);
//            nexradFile.scanURL(nexradURLs[n]);
//         }
//      else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//         nexradURLs[n] = WCTTransfer.getURL(nexradURLs[n]);
//         // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
//         nexradURLs[n] = Level2Transfer.decompressAR2V0001(nexradURLs[n]);
//         nexradFile.scanURL(nexradURLs[n]);
//      }
//      else {
//         // Transfer file to local tmp area -- force overwrite if NWS
//         if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//            nexradURLs[n] = WCTTransfer.getURL(nexradURLs[n], true);
//         }               
//         else {
//            nexradURLs[n] = WCTTransfer.getURL(nexradURLs[n]);
//         }
//         nexradFile.scanURL(nexradURLs[n]);
//      }
//         
//
//
//      
//         //================================================================================
//
//         // Get header
//         if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//               if (batchMode) {
//                  System.err.println("Level-II is not yet supported by the NEXRAD Math Tool.");
//               }
//               else {
//                  JOptionPane.showMessageDialog(null, "Level-II is not yet supported by the NEXRAD Math Tool.",
//                     "NEXRAD MATH ERROR", JOptionPane.ERROR_MESSAGE);                  
//               }
//               return;
//               /*
//            // Lazy object creation
//            if (level2Header == null) {
//               level2Header = new DecodeL2Header();
//            }
//            level2Header.decodeHeader(nexradURLs[n]);
//            header = level2Header;
//            */
//         }
//         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
//            nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//            // Lazy object creation
//            if (level3Header == null) {
//               level3Header = new DecodeL3Header();
//            }
//            level3Header.decodeHeader(nexradURLs[n]);
//            header = level3Header;
//            if (header.getProductType() == NexradHeader.L3VAD) {
//               if (batchMode) {
//                  System.err.println("The VAD Wind Profile Product (NVW) cannot be exported to other formats.");
//               }
//               else {
//                  JOptionPane.showMessageDialog(null, "The VAD Wind Profile Product (NVW) cannot be exported to other formats.",
//                     "NEXRAD EXPORT ERROR", JOptionPane.ERROR_MESSAGE);                  
//               }
//               return;
//            }
//         }
//         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
//            // Lazy object creation
//            if (xmrgHeader == null) {
//               xmrgHeader = new DecodeXMRGHeader();
//            }
//            xmrgHeader.decodeHeader(nexradURLs[n]);
//            header = xmrgHeader;
//         }
//         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
//            JOptionPane.showMessageDialog(null, "This data type is not supported with the Weather and Climate Toolkit.",
//               "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//            return;
//         }
//         else {
//            JOptionPane.showMessageDialog(null, "This is not WSR-88D Level-II, Level-III or XMRG NEXRAD Data",
//                  "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
//            return;
//         }
//      
//      
//      
//      
//         
//         // set aside the first header for the labels
//         if (n == 0) {
//            String dateString = new Integer(header.getDate()).toString();
//            startTime = dateString.substring(4, 6) + "/" + dateString.substring(6, 8) + "/" +
//               dateString.substring(0, 4) + " " + header.getHourString() + ":" + 
//               header.getMinuteString() + ":"+ header.getSecondString() +" GMT";            
//         }
//         
//         
//         
//         
//         
//            
//            
//               if (header.getProductType() == NexradHeader.L3DPA || 
//                  header.getProductType() == NexradHeader.XMRG) {
//
//                     
//                     
//                  if ((! operationExport) && (jmiOpAvg.isSelected() || jmiOpSum.isSelected())) {
//                     JOptionPane.showMessageDialog(null, 
//                        "Only the \"Export\" option is available for the Average and Sum functions for "+
//                        "DPA and XMRG Data",
//                        "MATH EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//                     return;                     
//                  }
//                     
//                     
//System.out.println("::::::::::::::::::::::::::::::::::::::::::::::: "+operationExport);
//                     
//                  // Check for native rasters - native raster export not yet supported for NetCDF and GeoTIFF
//                  if (operationExport && (jmiFormatGeoTIFF.isSelected() || jmiFormatNetCDF.isSelected())) {
//
//                     if (batchMode) {
//                        System.err.println("Export of this product is not available for GeoTIFF and NetCDF formats due the native non-WGS84 projection of the product.");
//                     }
//                     else {
//                        javax.swing.JOptionPane.showMessageDialog(null, 
//                           "Export of this product is not available for GeoTIFF and NetCDF formats due the native non-WGS84 projection of the product.", 
//                           "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                     
//                     }
//                     return;                  
//                  }               
//                     
//               }
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
//         //================================================================================
//         int pcode = header.getProductCode();
//         java.awt.geom.Rectangle2D.Double bounds = null;
//
//
//         if (header.getProductType() == NexradHeader.L3RADIAL ||
//               header.getProductType() == NexradHeader.L3RASTER ||
//               header.getProductType() == NexradHeader.L3DPA) {
//
//            if (level3Decoder == null) {
//               // initiate lazy object creation
//               level3Decoder = new DecodeL3Nexrad(level3Header);
//            }
//            System.out.println("--- START MATH DECODE DATA");
//            level3Decoder.decodeData();
//            decoder = level3Decoder;
//            System.out.println("--- FINISH MATH DECODE DATA");
//
//         }
//         else if (header.getProductType() == NexradHeader.XMRG) {
//            // initiate lazy object creation
//            if (xmrgDecoder == null) {
//               xmrgDecoder = new DecodeXMRGData(xmrgHeader);
//            }
//            System.out.println("--- START MATH DECODE DATA");
//            xmrgDecoder.decodeData();
//            decoder = xmrgDecoder;
//            System.out.println("--- FINISH MATH DECODE DATA");
//
//
//
//         }
//         else {
//            JOptionPane.showMessageDialog(null, "This product (code=" + pcode + ") is not yet supported!",
//                  "NEXRAD MATH ERROR", JOptionPane.ERROR_MESSAGE);
//            return;
//         }
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
//            if (fitToViewer) {            
//               bounds = nexview.getCurrentExtent();
//               if (nexview.getRadarSmoothFactor() > 0) {
//                  bounds = new java.awt.geom.Rectangle2D.Double(
//                     bounds.x - bounds.width/2.0,
//                     bounds.y - bounds.height/2.0,
//                     bounds.width * 2.0,
//                     bounds.height * 2.0);
//               }
//            }
//            else {
//               bounds = header.getNexradBounds();
//            }
//         
//            // Check for change in grid location
//            if (lastBounds != null && !lastBounds.equals(bounds)) {
//
//               JOptionPane.showMessageDialog(null, "The grid location has changed!\n" +
//                     "Please choose identical products and/or\n" +
//                     "deselect the \"Fit to Viewer Extent\" check\n" +
//                     "box in \"Options\" - \"Raster Options\"",
//                     "NEXRAD MATH ERROR", JOptionPane.ERROR_MESSAGE);
//               return;
//            }
//            lastBounds = bounds;
//
//            // Lazy object creation
//            if (rasterizer == null) {
//               if (rPropFrame == null) {
//                  rasterizer = new WCTRasterizer(1000, 1000, -999.0f);
//               }
//               else {
//                  rasterizer = new WCTRasterizer(gridSize, gridSize, gridNoData);
//               }
//            }
//            // set up rasterizer metadata
//            rasterizer.setLongName(NexradUtilities.getLongName(header));
//            rasterizer.setUnits(NexradUtilities.getUnits(header));
//            rasterizer.setDateInMilliseconds(header.getMilliseconds());
//       
//            if (operationExport) {
//               if (header.getProductType() == NexradHeader.L3DPA) {
//                  rasterizer = ((DecodeL3Nexrad)decoder).getDPARaster();
//               }               
//               else if (header.getProductType() == NexradHeader.XMRG) {
//                  rasterizer = ((DecodeXMRGData)decoder).getXMRGRaster();
//               }
//               else {
//                  ((WCTRasterizer)rasterizer).rasterize(decoder.getFeatures(), bounds, "colorIndex");
//               }
//            }
//            else {                       
//               ((WCTRasterizer)rasterizer).rasterize(decoder.getFeatures(), bounds, "colorIndex");
//            }
//            
//            
//            
//            // Create initial raster and reset bounds
//            if (processRaster == null) {
//               // create copy of rasterizer raster that has value of 0.0 for all data
//               processRaster = rasterizer.getWritableRaster().createCompatibleWritableRaster();
//               processBounds = rasterizer.getBounds();
//
//               // initialize process raster to NoData value
//               for (int i = 0; i < processRaster.getWidth(); i++) {
//                  for (int j = 0; j < processRaster.getHeight(); j++) {
//                     processRaster.setSample(i, j, 0, rasterizer.getNoDataValue());
//                  }
//               }
//
//            }
//
//            try {
//
//               // perform operation against existing writable raster
//               if (mathOperation == MATH_OP_MAX) {
//                  RasterMathOp.max(processRaster, processRaster, rasterizer.getWritableRaster(), rasterizer.getNoDataValue());
//               }
//               else if (mathOperation == MATH_OP_MIN) {
//                  RasterMathOp.min(processRaster, processRaster, rasterizer.getWritableRaster(), rasterizer.getNoDataValue());
//               }
//               else if (mathOperation == MATH_OP_ABSMAX) {
//                  RasterMathOp.absMax(processRaster, processRaster, rasterizer.getWritableRaster(), rasterizer.getNoDataValue());
//               }
//               else if (mathOperation == MATH_OP_SUM) {
//                  RasterMathOp.sum(processRaster, processRaster, rasterizer.getWritableRaster(), rasterizer.getNoDataValue());
//               }
//               else if (mathOperation == MATH_OP_AVERAGE) {
//                  RasterMathOp.average(processRaster, processRaster, n, rasterizer.getWritableRaster(), 1, rasterizer.getNoDataValue());
//               }
//
//            } catch (RasterMathOpException e) {
//               JOptionPane.showMessageDialog(null, "Grids to not match!", "RASTER MATH ERROR", JOptionPane.ERROR_MESSAGE);
//            }
//
//            // set bounds to current nexrad raster -- will use these bounds to check against next nexrad raster bounds
//            processBounds = rasterizer.getBounds();
//
//            /*
//            if (!NexradRasterExport.saveBinaryGrid(file, rasterizer, false)) {
//               JOptionPane.showMessageDialog(null, "Raster Export Error!", "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//            }
//
//            */
//            
//            
//            
//            
//         // check for cancelation
//         if (getLoopIndexValue() >= nexradURLs.length) {
//            n = nexradURLs.length;
//         }
//         else {
//            setLoopIndexValue(n);
//         }
//         
//            
//
//      }
//      // end nexradURLs loop
//
//   }
//
//
//   /**
//    * Save NEXRAD Data to various output format specified by static constants
//    *
//    * @param  file  Description of the Parameter
//    */
//   private void exportNexrad(File file) {
//
//      try {
//
//         // Set WritableRaster in NDITRasterizer to the MathOp Resulting processRaster
//         // Bounds and cellsize should match at this point.  If not, error would have been
//         // presented earlier.
//         rasterizer.setWritableRaster(processRaster);
//
//         try {
//
//            /*
//         if (jmiFormatShapefile.isSelected()) {
//            System.out.println("Saving: " + file + ".shp , " + file + ".shx , " + file + ".dbf , " + file + ".prj");
//            NexradVectorExport.saveShapefile(file, level3Decoder);
//         }
//         else if (jmiFormatWKT.isSelected()) {
//            //NexradVectorExport.saveWKT(file, level3Decoder);
//         }
//         */
//            //else if (jmiFormatGeoTIFF.isSelected() || jmiFormatESRIAscii.isSelected() ||
//            if (jmiFormatGeoTIFF.isSelected() || jmiFormatESRIAscii.isSelected() ||
//                  jmiFormatESRIFlt.isSelected() || jmiFormatGrADS.isSelected() ||
//                  jmiFormatHDF.isSelected() || jmiFormatNetCDF.isSelected()) {
//
//               WCTRasterExport rasterExport = new WCTRasterExport();
//                     
//               if (jmiFormatGeoTIFF.isSelected()) {
//                  System.out.println("Saving: " + file + ".tif ");
//                  rasterExport.saveGeoTIFF(file, rasterizer, GeoTiffType.TYPE_32_BIT);
//               }
//               else if (jmiFormatESRIAscii.isSelected()) {
//                  System.out.println("Saving: " + file + ".asc ");
//                  rasterExport.saveAsciiGrid(file, rasterizer);
//               }
//               else if (jmiFormatESRIFlt.isSelected()) {
//                  System.out.println("Saving: " + file + ".flt ");
//                  rasterExport.saveBinaryGrid(file, rasterizer);
//               }
//               else if (jmiFormatGrADS.isSelected()) {
//                  System.out.println("Saving: " + file + ".grd ");
//                  rasterExport.saveGrADSBinary(file, rasterizer);
//               }
//               else if (jmiFormatHDF.isSelected()) {
//                  //if (! NexradRasterExport.saveHDF(file, rasterizer))
//                  //   JOptionPane.showMessageDialog(null, "Raster Export Error!", "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//               }
//               else if (jmiFormatNetCDF.isSelected()) {
//                  System.out.println("Saving: " + file + ".nc ");
//                  rasterExport.saveNetCDF(file, rasterizer);
//               }
//            }            
//            else {
//               String message = "Only Raster Export Formats are supported for the NEXRAD Math Tool ";
//               JOptionPane.showMessageDialog(null, (Object) message,
//                  "NEXRAD MATH EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//            }
//
//         } catch (Exception e) {
//            e.printStackTrace();
//            String message = "Error writing \n" +
//                  "<html><font color=red>" + file + "</font></html>";
//            JOptionPane.showMessageDialog(null, (Object) message,
//                  "NEXRAD MATH EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//         }
//
//
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//
//   }
//
//
//   // END METHOD saveNexradAsShapefile
//
//
//   /**
//    *  Gets the outputType attribute of the NexradMath object
//    *
//    * @return    The outputType value
//    */
//   public ExportFormatType getOutputType() {
//      if (jmiFormatShapefile.isSelected() ||
//            jmiFormatWKT.isSelected()) {
//         return ExportFormatType.VECTOR;
//      }
//      else if (jmiFormatGeoTIFF.isSelected() ||
//            jmiFormatESRIAscii.isSelected() ||
//            jmiFormatESRIFlt.isSelected() ||
//            jmiFormatGrADS.isSelected() ||
//            jmiFormatHDF.isSelected() ||
//            jmiFormatNetCDF.isSelected()) {
//         return ExportFormatType.RASTER;
//      }
//      else {
//         return ExportFormatType.NATIVE;
//      }
//   }
//
//
//   /**
//    *  Gets the processRaster attribute of the NexradMath object
//    *
//    * @return    The processRaster value
//    */
//   public WritableRaster getProcessRaster() {
//      return processRaster;
//   }
//
//
//   /**
//    *  Gets the processBounds attribute of the NexradMath object
//    *
//    * @return    The processBounds value
//    */
//   public java.awt.geom.Rectangle2D.Double getProcessBounds() {
//      return processBounds;
//   }
//
//   
//   
//   public DataSourcePanel getDataSourcePanel() {
//      return dataSourcePanel;
//   }
//   
//
//   
//   
//   
//   
//   
//   
//   
//   /**
//    *  Implementation of NexradInterface
//    */
//   public FileScanner getFileScanner() {
//      return nexradFile;
//   }
//   /**
//    *  Implementation of NexradInterface
//    */
//   public DecodeL3Header getLevel3Header() {
//      return level3Header;
//   }
//   /**
//    *  Implementation of NexradInterface
//    */
//   public DecodeXMRGHeader getXMRGHeader() {
//      return xmrgHeader;
//   }
//   /**
//    *  Implementation of NexradInterface
//    */
//   public DecodeL3Nexrad getLevel3Decoder() {
//      return level3Decoder;
//   }
//   /**
//    *  Implementation of NexradInterface
//    */
//   public DecodeXMRGData getXMRGDecoder() {
//      return xmrgDecoder;
//   }
//    
//   /**
//    *  Implementation of NexradInterface
//    *  Gets the NexradFilter attribute of the NexradIAViewer object
//    *
//    * @return  nxfilter  The current NexradFilter
//    */
//   public WCTFilter getFilter() { 
//      return nxfilter;
//   }
//
//   /**
//    *  Implementation of NexradInterface
//    *  Gets the NexradFilterGUI attribute of the NexradIAViewer object
//    *
//    * @return  nxfilter  The current NexradFilterGUI
//    */
////   public WCTFilterGUI getFilterGUI() { 
////      return nxfilterGUI;
////   }
//
//   
//    /**
//     * Define implementation of NexradInterface
//     */    
//    public int getType() {
//       return WCTUiInterface.EXPORT;
//    }
//    
//   
//
//
////    @Override
//    public DecodeRadialDatasetSweep getRadialDecoder() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
////    @Override
//    public DecodeRadialDatasetSweepHeader getRadialHeader() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//
//
//
//
//
//    class SubmitButtonListener implements ActionListener {
//
//        private final NexradMath parent;
//
//        public SubmitButtonListener(NexradMath parent) {
//            this.parent = parent;
//        }
//
//
//
//        public void actionPerformed(ActionEvent evt) {
//
//
//            String dataType = dataSourcePanel.getDataType();
//            dataSourcePanel.setStatus("Listing Data ...", dataType);
//            
//            
//            try {
//
////              From: http://foxtrot.sourceforge.net/docs/tips.php
////              Avoid the temptation to modify anything from inside Job.run(). 
////              It should just take data from outside, perform some heavy operation 
////              and return the result of the operation.
////              The pattern to follow in the implementation of Job.run() is Compute and Return, see example below.  
//
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
//
//
//
//        }
//    }
//    
//
//    
//    
//    
//    final class FilterKeyListener implements KeyListener {
//
//        private final NexradMath parent;
//
//        public FilterKeyListener(NexradMath parent) {
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

