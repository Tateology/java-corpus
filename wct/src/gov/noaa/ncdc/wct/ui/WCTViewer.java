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

package gov.noaa.ncdc.wct.ui;

import edu.wisc.ssec.mcidas.AreaFileException;
import gov.noaa.ncdc.common.BareBonesBrowserLaunch;
import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.RotatedTextIcon;
import gov.noaa.ncdc.common.RoundedBorder;
import gov.noaa.ncdc.common.SwingWorker;
import gov.noaa.ncdc.help.JNXHelp;
import gov.noaa.ncdc.help.NewJNXFeatures;
import gov.noaa.ncdc.nexrad.WCTLookAndFeel;
import gov.noaa.ncdc.nexradiv.AlphaProperties;
import gov.noaa.ncdc.nexradiv.BaseMapStyleInfo;
import gov.noaa.ncdc.nexradiv.ClipboardImageTransfer;
import gov.noaa.ncdc.nexradiv.JNXPrintReport;
import gov.noaa.ncdc.nexradiv.MapSelector;
import gov.noaa.ncdc.nexradiv.NexradVADPanel;
import gov.noaa.ncdc.nexradiv.Q2Properties;
import gov.noaa.ncdc.nexradiv.RSLDisplayDialog;
import gov.noaa.ncdc.nexradiv.RangeRings;
import gov.noaa.ncdc.nexradiv.RangeRingsGUI;
import gov.noaa.ncdc.nexradiv.SimpleShapefileLayer;
import gov.noaa.ncdc.nexradiv.SnapshotLayer;
import gov.noaa.ncdc.nexradiv.StormSummaryGUI;
import gov.noaa.ncdc.nexradiv.WCTSplashWindow;
import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.JNXLegend;
import gov.noaa.ncdc.nexradiv.legend.LegendCategoryFactory;
import gov.noaa.ncdc.nexradiv.legend.NexradLegendLabelFactory;
import gov.noaa.ncdc.nexradiv.legend.NexradMetaPanel;
import gov.noaa.ncdc.nexradiv.legend.WCTLegendPanel;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetColorFactory;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster.Band;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeHail;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMDA;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMeso;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeQ2;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeRSL;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormStructure;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormTracking;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeTVS;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeVADText;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport.AlphaInterpolationType;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ncdc.wct.ui.BaseMapManager.DeclutterType;
import gov.noaa.ncdc.wct.ui.animation.CaptureAnimator;
import gov.noaa.ncdc.wct.ui.browser.NCDCFlexBrowser;
import gov.noaa.ncdc.wct.ui.event.LoadDataListener;
import gov.noaa.ncdc.wct.ui.event.MousePopupListener;
import gov.noaa.ncdc.wct.ui.event.SideBarButtonListener;
import gov.noaa.ncdc.wct.ui.filter.WCTFilterGUI;
import gov.noaa.ncdc.wct.ui.ge.GoogleEarthBrowser;
import gov.noaa.ncdc.wct.ui.ge.GoogleEarthBrowser.ViewController;
import gov.noaa.ncdc.wct.ui.ge.WCTImageServer;
import gov.noaa.ncdc.wct.ui.plugins.CDRImpactToolUI;
import gov.noaa.ncdc.wms.WMSData;
import gov.noaa.ncdc.wms.WMSException;
import gov.noaa.ncdc.wms.WmsLayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
import java.awt.Polygon;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.TileCache;
import javax.media.jai.WritableRenderedImageAdapter;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import opendap.dap.DAP2Exception;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.WCTStatusBar;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.map.WCTMapContext;
import org.geotools.pt.Envelope;
import org.geotools.renderer.j2d.LegendPosition;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.renderer.j2d.RenderedLogo;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacementImpl;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.opengis.referencing.operation.TransformException;

import thredds.inventory.bdb.MetadataManager;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NCdump;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

/**
 *  Weather and Climate Toolkit
 *  Developed at the National Climatic Data Center
 *
 * @author     Steve Ansari (steve.ansari@noaa.gov)
 * @created    May 3, 2004 
 */
public class WCTViewer extends WCTFrame 
implements Viewer, WCTUiInterface, DataDecodeListener, GeneralProgressListener, LoadDataListener, 
ActionListener, WindowListener, ComponentListener {



	private static final Logger logger = Logger.getLogger(WCTViewer.class.getName());

	public static enum CurrentViewType { GEOTOOLS, GOOGLE_EARTH, NCDC_NCS, NCDC_NIDIS, GOOGLE_EARTH_SPLIT_GEOTOOLS, FOUR_MAP_PANE };
	public static enum CurrentDataType { NONE, RADAR, SATELLITE, MODEL, GRIDDED };
	private CurrentDataType currentDataType = CurrentDataType.NONE;
	private CurrentViewType currentViewType = CurrentViewType.GEOTOOLS;


	public final static double RASTER_SIZE_FACTOR = 1.5;



	private String fileString = "Untitled";
	private File projectFile;
	//private StyledMapPane mapPane = new StyledMapPane();
	private WCTMapPane wctMapPane = new WCTMapPane();
	private WCTMapPaneZoomChange zoomChangeListener;
	private WCTMapContext map = new WCTMapContext();
	private StyleBuilder sb = new StyleBuilder();
	private WCTStatusBar statusBar = new WCTStatusBar(wctMapPane, this);

	private DefaultMapLayer mlNexrad, mlNexradAlpha, mlNexradAlphaLabel;
	private DefaultMapLayer mlNexradAlphaTrack, mlNexradRangeRings, mlMarkers;

	private java.awt.geom.Rectangle2D.Double nexradBounds;
	private FeatureCollection nexradFeatures = FeatureCollections.newCollection();
	private FeatureCollection rangeRingFeatures = FeatureCollections.newCollection();
	private FeatureCollection markerFeatures = FeatureCollections.newCollection();
	private FeatureType nexradSchema;



	private GridCoverage gridSatelliteGC = null;
	private RenderedGridCoverage gridSatelliteRGC = new RenderedGridCoverage(
			new GridCoverage("SATELLITE_RASTER",
					RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
					new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .00001, .00001))
			)
	);
	private URL gridSatelliteURL;



	private GridCoverage radarGC = null;
	private RenderedGridCoverage radarRGC = new RenderedGridCoverage(
			new GridCoverage("NEXRAD_RASTER",
					RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
					new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .00001, .00001))
			)
	);

	private GridCoverage contourGC = null;
	private RenderedGridCoverage contourRGC = new RenderedGridCoverage(
			new GridCoverage("CONTOUR_IMAGE_RASTER",
					RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
					new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .00001, .00001))
			)
	);

	private RenderedGridCoverage[] wmsRGC = new RenderedGridCoverage[3];

	private ArrayList<SnapshotLayer> snapshotList = new ArrayList<SnapshotLayer>();


	private boolean isLoading = false;       

	private WCTRasterizer rasterizer;
	private WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();

	private boolean memoryThreadRunning = true;

	private String nexradFile = "none";
	private URL dataUrl = null;

	private JPanel mainPanel, toolPanel, mapPanel;
	private JPanel infoPanel, metaPanel, topPanel;
	private JScrollPane infoScrollPane, vadScrollPane;
	private JComponent mapScrollPane;
	private WCTLegendPanel keyPanel;
	private NexradVADPanel vadPanel;
	private JNXLegend legend;



	private boolean useWctCache = true;

	private double lastDecodedElevationAngle = Double.NaN;

	// Projection Constants
	/**
	 *  Description of the Field
	 */
	//    public final static int LATLON = 1, ALBERS = 2, STEREO = 3, HRAP = 4, RADAR = 5;
	/**
	 *  Description of the Field
	 */
	public final static int MILES = 1, KM = 2, NAUTICAL_MI = 3;
	//    int projection = LATLON;
	// Units Constants
	/**
	 *  Description of the Field
	 */
	public final static int DMS = 1, DECI = 2;
	int geoUnits = DMS;
	int distUnits = MILES;

	// Map Themes ORDER DICTATES DISPLAY ORDER
	/**
	 *  Description of the Field
	 */
	public final static int COUNTRIES = 0;
	/**
	 *  Description of the Field
	 */
	public final static int COUNTRIES_USA = 1;
	/**
	 *  Description of the Field
	 */
	public final static int STATES = 2;
	/**
	 *  Description of the Field
	 */
	public final static int RIVERS = 3;
	/**
	 *  Description of the Field
	 */
	public final static int COUNTIES = 4;
	/**
	 *  Description of the Field
	 */
	public final static int HWY_INT = 5;
	/**
	 *  Description of the Field
	 */
	public final static int COUNTRIES_OUT = 6;
	/**
	 *  Description of the Field
	 */
	public final static int COUNTRIES_OUT_USA = 7;
	/**
	 *  Description of the Field
	 */
	public final static int STATES_OUT = 8;
	/**
	 *  Description of the Field
	 */
	public final static int CITY250 = 13, CITY100 = 12, CITY35 = 11, CITY10 = 10, CITY_SMALL = 9;
	/**
	 *  Description of the Field
	 */
	public final static int AIRPORTS = 14;
	/**
	 *  Description of the Field
	 */
	public final static int ASOS_AWOS = 15;
	/**
	 *  Description of the Field
	 */
	public final static int CRN = 16;
	/**
	 *  Description of the Field
	 */
	public final static int WSR = 17, TDWR = 18, CLIMATE_DIV = 19;
	/**
	 *  Description of the Field
	 */
	public final static int NUM_LAYERS = 20;


	public final static String ACTION_LOAD = "LOAD";
	public final static String ACTION_REFRESH = "REFRESH";




	// Local User Added Theme Counter
	int localThemeCounter = 0;

	boolean legendVisible = false;
	boolean legendHighlights = true;
	boolean nexradHighlights = true;

	// Alphanumeric Display Values and defaults
	private int alphaLineWidth = 1;
	// aka size
	private Color alphaLineColor = new Color(220, 220, 220);
	private Color alphaFillColor = new Color(220, 220, 220);
	private double alphaTransparency = 0.0;
	private int radarAlphaChannelValue = 255;
	private boolean alphaHalo = true;
	// 1 == fully transparent
	private String alphaSymbol = StyleBuilder.MARK_CIRCLE;

	private Vector<MapLayer> baseMapLayers = new Vector<MapLayer>();
	private Vector<MapLayer> baseMapLabelLayers = new Vector<MapLayer>();
	private Vector<BaseMapStyleInfo> baseMapStyleInfo = new Vector<BaseMapStyleInfo>();

	// External GUI Frames
//	private DataOrganizer dataOrganizer;
	private DataSelector dataSelect;
	private MapSelector mapSelect;
	private CaptureAnimator captureAnimate;
	private BookmarkUI bookmarkEditor;
	private IdentifyUI identifyUI;
	private ViewProperties viewProperties;
	private AlphaProperties alphaProperties;
//	private WCTExportGUI nexradExporter;
//	private NexradMath nexradMath;
	private JNXHelp jnxhelp;
	private NewJNXFeatures jnxnew;
	private RadialProperties radialProps;
	private WCTFilterGUI wctFilterGUI = new WCTFilterGUI("Data Filter", this, false);
	private WCTFilter animationFilter;
	private WCTFilter wctFilter;
	private RangeRingsGUI rangeRings;
	private MarkerEditor markerEditor;
	private StormSummaryGUI stormSummaryGUI;
	//  private NexradMosaic nexradMosaic;
	private WCTTextDialog suppleData;

	private Q2Properties q2Props = new Q2Properties(this);
	private GridDatasetProperties gridProps = null;

	private Frame animationProgressFrame;

	// Frame Tools
	WCTToolBar wctToolBar;
	//    final JComboBox jcomboViewSelector = new JComboBox(new String[] { " Standard", " Google Earth", " Google Earth Split });
//	final JComboBox jcomboViewSelector = new JComboBox(new String[] { " Standard", " Google Earth Split", " Four Pane" });
	final JComboBox<String> jcomboViewSelector = new JComboBox<String>(new String[] { " Standard", " Google Earth Split",});

	// Decoders
	private FileScanner scannedFile;
	private NexradHeader header; 
	private RadialDatasetSweep radialDataset;
	private DecodeRadialDatasetSweepHeader radialDatasetHeader;
	private DecodeL3Header level3Header;
	private DecodeXMRGHeader xmrgHeader;
	private DecodeRadialDatasetSweep radialDatasetDecoder;
	private DecodeL3Nexrad level3Decoder;
	private DecodeL3Alpha alpha_decoder;
	private DecodeXMRGData xmrgDecoder;
	private DecodeQ2 q2Decoder;
	private GoesRemappedRaster goesAreaRaster;
	private GridDatasetRemappedRaster gridDatasetRaster;
	private RadialDatasetSweepRemappedRaster radialDatasetRaster;



	// JMenu Declarations
	private JMenuBar jMenuBar1;
	private JMenu jmFile;
	private JMenuItem jmiNew, jmiPrint, jmiCopyImage, jmiSaveImage, jmiSaveKmz, jmiLaunchKmz, 
	jmiSave, jmiSaveAs, jmiOpen, jmiCacheViewer, jmiExit;
	private JMenu jmData;
	private JMenuItem jmiDataOrganizer, jmiLoadNexrad, jmiOrderData, jmiOverlay, jmiExportNexrad;
	private JMenu jmDataServicesSubMenu;
	private JMenuItem jmiDroughtMonitor, jmiSpcStormReports, jmiNhc, jmiGhcn, jmiCDRImpactTool;
	private JMenu jmView;
	private JMenuItem jmiBookmarks, jmiViewProp, jmiRangeRings, jmiMarkerEditor, jmiSavePS, jmiAlphaProp, jmiRadialProp, jmiGridProp, jmiSuppleData;
	private JMenu jmTools;
	private JMenuItem jmiDataSearch, jmiIsosurface, jmiScreenCapture, jmiCaptureAnim, jmiMath, jmiAttributeFilter, 
	jmiStormSummary, jmiNexradMosaic, jmiContourManager, jmiTimeMorphing;
	private JMenu jmHelp;
	private JMenuItem jmiAbout, jmiHelp, jmiTutorial, jmiQuickHelp, jmiNewFeatures;

	private JButton servicesSelectorButton;
	private JButton dataSelectorButton;
	private JButton mapSelectorButton;
	private JButton captureButton;
	private JButton saveImageButton;
	private JButton saveKmzButton;
	private JButton openKmzButton;
	private JButton copyImageButton;



	private JProgressBar progress;

	static Font font12 = new Font("TimesRoman", Font.PLAIN, 12);
	static Font font14 = new Font("TimesRoman", Font.PLAIN, 14);

	DecimalFormat fmt0 = new DecimalFormat("00");
	DecimalFormat fmt2 = new DecimalFormat("0.00");


	private boolean isRasterVariableRes = true;

	boolean firstTime = true;
	boolean isVADdisplayed = false;

	private WCTSplashWindow splashWindow;


	//private BufferedImage  logo = null;
	private Image logo = null;
	private RenderedLogo renderedLogo = null;
	private RenderedLogo gridSatelliteLegend = null;

	private boolean isNOAALogoPainted = true;

	//  private RenderedCategoryLegend satelliteLegend = new RenderedCategoryLegend("Satellite Legend");
	private boolean isGridSatelliteLegendVisible = true;


	private double radarSmoothFactor = 0.0;
	private double radarSmoothExtentEnlargementFactor = 3.0;
	private boolean isRadarPowerSmoothing = false;
	private double satelliteSmoothFactor = 0.0;


	private boolean firstQ2 = true;
	private boolean firstRadar = true;
	private String lastRadarID = "";

	private BoundedRangeModel animationRangeModel = null;

	private CategoryLegendImageProducer radLegendProducer = new CategoryLegendImageProducer();
	private CategoryLegendImageProducer gridSatLegendProducer = new CategoryLegendImageProducer();
	private CategoryLegendImageProducer lastDecodedLegendProducer; 

	private ColorsAndValues satColorsAndValues, gridColorsAndValues;

	private ContourUI contourDialog;
	private NdmcDroughtMonitorUI droughtMonitor;
	private SpcStormReportsUI spcStormReports;
	private GhcnOrderExportDialog ghcnTool;
	private NhcTracksUI nhcTracks;

	private HashMap<String, WmsLayer> wmsLayerMap = new HashMap<String, WmsLayer>();
	private HashMap<String, RenderedLogo> wmsLegendMap = new HashMap<String, RenderedLogo>();
	private HashMap<String, RenderedLogo> wmsLogoMap = new HashMap<String, RenderedLogo>();

	private GoogleEarthBrowser geBrowser, geExtBrowser;
	private NCDCFlexBrowser flexBrowser;
	private WCTImageServer wctImageServer;
	private JWebBrowser webMapBrowser;

	private Vector<RenderCompleteListener> renderCompleteListeners = new Vector<RenderCompleteListener>();
	public interface RenderCompleteListener {
		public void renderComplete();
		public void renderProgress(int progressPercent);
	}


	public WCTViewer() {

		super("NOAA Weather and Climate Toolkit");
		
		// init the log reader for system out messages
//		WCTSystemOutputDialog.getSharedSystemOutputDialog(this);

		
		
		
		splashWindow = new WCTSplashWindow(this, false);
		splashWindow.setStatus("Initializing Viewer", 5);

		// Initialize Theme Vector
		baseMapLayers.setSize(NUM_LAYERS);
		baseMapLabelLayers.setSize(NUM_LAYERS);
		baseMapStyleInfo.setSize(NUM_LAYERS);

		createGUI();

		viewProperties = new ViewProperties(this);
		suppleData = new WCTTextDialog(this, "", "Supplemental Data", false);

		// config JAI
		TileCache cache = JAI.getDefaultInstance().getTileCache();
		cache.setMemoryCapacity(200000000L);
		cache.setMemoryThreshold(.75f);
		JAI.getDefaultInstance().setTileCache(cache);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(0);


		try {
			registerIOSPs();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, e1.getMessage(), "INIT ERROR", JOptionPane.ERROR_MESSAGE);
		}

		try {
			loadBaseMaps();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//      pack();
		//      .setVisible(true);
		mainPanel.validate();
		mainPanel.repaint();

		splashWindow.close();

		setSize(950, 660);
		setLocation(20, 20);
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setVisible(true);

		// Load DataSelector FrameA


	}

	public void addRenderCompleteListener(RenderCompleteListener l) {
		renderCompleteListeners.add(l);
	}
	public void removeRenderCompleteListener(RenderCompleteListener l) {
		renderCompleteListeners.remove(l);
	}
	public void removeAllRenderCompleteListeners() {
		renderCompleteListeners.removeAllElements();
	}


	//**************************************************************
	//**************************************************************
	/**
	 *  Description of the Method
	 */
	private void createGUI() {

		this.addWindowListener(this);
		this.addComponentListener(this);
		ActionListener viewerEventController = new SideBarButtonListener(this);

		WCTDropTargetHandler.getInstance().registerViewer(this);        
		this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_NONE, WCTDropTargetHandler.getInstance()));


		splashWindow.setStatus("Creating GUI", 10);
		mainPanel = new JPanel();
		toolPanel = new JPanel();
		infoPanel = new JPanel();
		//metaPanel = new JPanel();
		metaPanel = new NexradMetaPanel();
		//vadPanel = new NexradVADPanel();
		topPanel = new JPanel();
		keyPanel = new WCTLegendPanel();

		//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//ASDF
		//setBackground(new Color(159, 182, 205));
		//        setBackground(new Color(220, 220, 220));
		//      setBounds(1, 1, 760, 540);
//		getContentPane().add(mainPanel);
		//ASDF
		//add(mainPanel);

		// Set Layouts
		mainPanel.setLayout(new BorderLayout());
		//        mainPanel.setBackground(new Color(220, 220, 220));
		toolPanel.setLayout(new GridLayout(1, 3));
		//infoPanel.setLayout(new GridLayout(2, 1));
		infoPanel.setLayout(new BorderLayout());
		//        infoPanel.setBackground(new Color(220, 220, 220));
		//        metaPanel.setBackground(new Color(220, 220, 220));
		//        keyPanel.setBackground(new Color(220, 220, 220));
		keyPanel.setForeground(Color.black);
		//keyPanel.setBackground(new Color(220, 220, 220));
		//      infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		//exportPanel.setLayout(new BorderLayout());
		metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
		//         keyPanel.setLayout(new BorderLayout());


		// Set up JMenu Menus
		jMenuBar1 = new JMenuBar();
		jmFile = new JMenu();
		jmiNew = new JMenuItem("New Session", KeyEvent.VK_N);
		jmiNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		jmiNew.setToolTipText("Start a New NEXRAD Viewer Session");
		jmiPrint = new JMenuItem("Print", KeyEvent.VK_P);
		//jmiPrint.setEnabled(false);
		jmiPrint.setToolTipText("Print Map and Legend (if visible)");
		jmiSaveImage = new JMenuItem("Save Image", KeyEvent.VK_S);
		jmiSaveImage.setActionCommand("Save Image");
		jmiSaveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		jmiSaveImage.setToolTipText("Save Map and Legend (if visible) to Image File (gif, jpg, png, etc)");
		jmiSaveKmz = new JMenuItem("Save KMZ", KeyEvent.VK_K);
		jmiSaveKmz.setActionCommand("Save KMZ");
		jmiSaveKmz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		jmiSaveKmz.setToolTipText("Save a KMZ file of the current Map and Legend to disk");
		jmiLaunchKmz = new JMenuItem("Launch KMZ", KeyEvent.VK_K);
		jmiLaunchKmz.setActionCommand("Launch KMZ");
		jmiLaunchKmz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		jmiLaunchKmz.setToolTipText("Opens a KMZ file of the current Map and Legend in Google Earth " +
		"or application currently registered for KMZ extensions");

		jmiCopyImage = new JMenuItem("Copy Image to Clipboard", KeyEvent.VK_C);
		jmiCopyImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		jmiCopyImage.setToolTipText("Copy Map and Legend (if visible) to system clipboard.  This can be pasted into PowerPoint, Word, Email, etc...");
		jmiCopyImage.setActionCommand("Copy Image");
		jmiSave = new JMenuItem("Save Session", KeyEvent.VK_S);
		jmiSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		jmiSave.setToolTipText("Save Current Session File");
		jmiSaveAs = new JMenuItem("Save Session As ...", KeyEvent.VK_A);
		jmiSaveAs.setToolTipText("Save Current Session File Dialog");
		jmiOpen = new JMenuItem("Open Session", KeyEvent.VK_O);
		jmiOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		jmiOpen.setToolTipText("Open a NOAA Weather and Climate Toolkit Session File (.wctproj)");
		jmiCacheViewer = new JMenuItem("Web Start Cache Viewer", KeyEvent.VK_W);
		jmiCacheViewer.setToolTipText("Start the Java Web Start Application Cache Viewer");
		jmiExit = new JMenuItem("Exit", KeyEvent.VK_X);
		jmiExit.setToolTipText("Exit the NOAA Weather and Climate Toolkit");
		jmData = new JMenu();
		jmDataServicesSubMenu = new JMenu("Data Services");
		jmDataServicesSubMenu.setToolTipText("Access to Data Services on the Internet");
		jmiDataOrganizer = new JMenuItem("Data Organizer", KeyEvent.VK_D);
		jmiDataOrganizer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		jmiDataOrganizer.setToolTipText("Load Data from NCDC, Local Disk or Custom URL");
		jmiLoadNexrad = new JMenuItem("Load Data", KeyEvent.VK_L);
		jmiLoadNexrad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		jmiLoadNexrad.setToolTipText("Load Data from NCDC, Local Disk or Custom URL");
		//        jmOrderDataSubMenu = new JMenu("Order Data");
		//        jmOrderDataSubMenu.setToolTipText("Access to Data Ordering websites with the default internet browser");
		jmiOrderData = new JMenuItem("Find/Order Data", KeyEvent.VK_R);
		jmiOrderData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		jmiOrderData.setToolTipText("Access to Data Archive/Server websites with the default internet browser");
		//        jmiOrderSatellite = new JMenuItem("Order Satellite", KeyEvent.VK_S);
		//        jmiOrderSatellite.setToolTipText("Opens Default Browser Window to NOAA CLASS System");
		//        jmiOrderNexrad = new JMenuItem("Order NEXRAD", KeyEvent.VK_R);
		//        jmiOrderNexrad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		//        jmiOrderNexrad.setToolTipText("Opens Default Browser Window to NCDC Online Inventory Search");
		//        jmiOrderXMRG = new JMenuItem("Order RFC MPE (XMRG)", KeyEvent.VK_X);
		//jmiOrderXMRG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		//        jmiOrderXMRG.setToolTipText("Opens Default Browser Window to NWS RFC XMRG Data");
		jmiOverlay = new JMenuItem("Layer Selector", KeyEvent.VK_O);
		jmiOverlay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		jmiOverlay.setToolTipText("Adjust Map Overlays and Add Custom Shapefiles");
		jmiExportNexrad = new JMenuItem("Data Exporter", KeyEvent.VK_E);
		jmiExportNexrad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		jmiExportNexrad.setToolTipText("Save Data in Multiple Common Scientific Formats");
		jmView = new JMenu();
		jmiBookmarks = new JMenuItem("Bookmark Editor", KeyEvent.VK_B);
		jmiBookmarks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		jmiBookmarks.setToolTipText("Adjust Display, Raster, Legend and WMS Background Properties");
		jmiViewProp = new JMenuItem("View Properties", KeyEvent.VK_V);
		jmiViewProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		jmiViewProp.setToolTipText("Adjust Display, Raster, Legend and WMS Background Properties");
		jmiRangeRings = new JMenuItem("Range Rings", KeyEvent.VK_R);
		jmiRangeRings.setToolTipText("Show and Adjust Radar Range Rings");
		jmiMarkerEditor = new JMenuItem("Marker Editor", KeyEvent.VK_M);
		jmiMarkerEditor.setToolTipText("Add / Edit Custom Markers");
		jmiSavePS = new JMenuItem("Save PostScript", KeyEvent.VK_O);
		jmiAlphaProp = new JMenuItem("Alphanumeric Prop.", KeyEvent.VK_A);
		jmiAlphaProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		jmiAlphaProp.setToolTipText("Adjust Alphanumeric NEXRAD Display Properties");
		jmiRadialProp = new JMenuItem("Radial Prop.", KeyEvent.VK_L);
		jmiRadialProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		jmiRadialProp.setToolTipText("Choose Radial Sweep Elevation Angle and Moment");
		jmiGridProp = new JMenuItem("Grid Prop.", KeyEvent.VK_G);
		jmiGridProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		jmiGridProp.setToolTipText("Choose Grid Dataset variable/height/time/runtime");
		jmiSuppleData = new JMenuItem("Supplemental Data", KeyEvent.VK_S);
		jmiSuppleData.setToolTipText("Display Supplemental Text Data for Supported NEXRAD Products");
		jmTools = new JMenu();
		jmiDataSearch = new JMenuItem("Data Search", KeyEvent.VK_Q);
		jmiDataSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		jmiIsosurface = new JMenuItem("Isosurface KML Generator", KeyEvent.VK_I);
		jmiIsosurface.setToolTipText("Create Isosurface of Current Grid or Radial dataset at the Current Zoom Extent");
		jmiTimeMorphing = new JMenuItem("Time Morphing Tool (BETA)", KeyEvent.VK_I);
//		jmiTimeMorphing.setToolTipText("");
		jmiScreenCapture = new JMenuItem("Screen Capture", KeyEvent.VK_S);
		jmiScreenCapture.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		jmiScreenCapture.setToolTipText("Save Map and Legend (if visible) Image in Memory");
		jmiScreenCapture.setActionCommand("Capture");
		jmiCaptureAnim = new JMenuItem("Capture Animation", KeyEvent.VK_C);
		jmiCaptureAnim.setToolTipText("Create Animation of Screen Captures");
		jmiMath = new JMenuItem("Math Tool (BETA)", KeyEvent.VK_M);
		jmiMath.setToolTipText("Conduct Simple Mathimatical Functions on data (min, max, avg, etc)");
		jmiAttributeFilter = new JMenuItem("Data Filter", KeyEvent.VK_F);
		jmiAttributeFilter.setToolTipText("Filter Data by Distance and Value");
		jmiStormSummary = new JMenuItem("Storm Summary", KeyEvent.VK_S);
		jmiStormSummary.setToolTipText("Storm Summary: Layers Intersecting NEXRAD");
		jmiNexradMosaic = new JMenuItem("NEXRAD Mosaic", KeyEvent.VK_N);
		jmiNexradMosaic.setToolTipText("Create Mosaic of Multiple NEXRAD Files");

		jmiContourManager = new JMenuItem("Contour Manager (BETA)");
		jmiContourManager.setToolTipText("Show the contour manager dialog");

		jmiDroughtMonitor = new JMenuItem("Drought Monitor");
		jmiDroughtMonitor.setToolTipText("Load Drought Monitor background maps from NDMC");
		jmiCDRImpactTool = new JMenuItem("CDR Impact Tool");
		jmiCDRImpactTool.setToolTipText("Load CDR Impact Tool");
		jmiSpcStormReports = new JMenuItem("SPC Storm Reports");
		jmiNhc = new JMenuItem("Active Hurricanes");
		jmiNhc.setToolTipText("Load Active Hurricane track and predicted path info from NOAA's National Hurricane Center");
		jmiGhcn = new JMenuItem("GHCN Export Tool");
		jmiGhcn.setToolTipText("Export GHCN (Global Historical Climate Network) data tool.");
		
		jmHelp = new JMenu();
		jmiAbout = new JMenuItem("About", KeyEvent.VK_A);

		// Special initialization for Help stuff
		jmiHelp = new JMenuItem("Help", KeyEvent.VK_H);
		//      jmiHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		//      try {
		//      String helpHS = "/docs/help/Help.hs";
		//      URL hsURL = ResourceUtils.getInstance().getResource(helpHS);
		//      HelpSet hs = new HelpSet(null, hsURL);
		//      HelpBroker hb = hs.createHelpBroker();
		//      jmiHelp.addActionListener(new CSH.DisplayHelpFromSource(hb));
		//      } catch (Exception e) {
		//      e.printStackTrace();
		//      }

		jmiTutorial = new JMenuItem("Tutorials", KeyEvent.VK_T);
		jmiQuickHelp = new JMenuItem("Quick Help", KeyEvent.VK_Q);
		jmiNewFeatures = new JMenuItem("New Features", KeyEvent.VK_N);
		jmFile.setText("File");
		jmFile.setMnemonic(KeyEvent.VK_F);
		jmFile.setToolTipText("Open/Save Session Data");
		jmData.setText("Data");
		jmData.setMnemonic(KeyEvent.VK_D);
		jmData.setToolTipText("Add/Remove Map Data");
		jmView.setText("View");
		jmView.setMnemonic(KeyEvent.VK_V);
		jmView.setToolTipText("Adjust Map Properties");
		jmTools.setText("Tools");
		jmTools.setMnemonic(KeyEvent.VK_T);
		jmTools.setToolTipText("Query, Animate, Math, Summarize");
		jmHelp.setText("Help");
		jmHelp.setMnemonic(KeyEvent.VK_H);
		jmHelp.setToolTipText("Help Pages");

		jmiNew.addActionListener(this);
		jmiPrint.addActionListener(this);
		jmiSave.addActionListener(this);
		jmiSaveAs.addActionListener(this);
		jmiOpen.addActionListener(this);
		jmiCacheViewer.addActionListener(this);
		jmiExit.addActionListener(this);
		jmiDataOrganizer.addActionListener(this);
		jmiLoadNexrad.addActionListener(this);
		jmiOrderData.addActionListener(this);
		jmiOverlay.addActionListener(this);
		jmiExportNexrad.addActionListener(this);
		jmiRangeRings.addActionListener(this);
		jmiMarkerEditor.addActionListener(this);
		jmiBookmarks.addActionListener(this);
		jmiViewProp.addActionListener(this);
		jmiSaveImage.addActionListener(viewerEventController);
		jmiSaveKmz.addActionListener(viewerEventController);
		jmiLaunchKmz.addActionListener(viewerEventController);
		jmiCopyImage.addActionListener(viewerEventController);
		//        jmiSavePS.addActionListener(this);
		jmiAlphaProp.addActionListener(this);
		jmiAlphaProp.setEnabled(false);
		jmiRadialProp.addActionListener(this);
		jmiRadialProp.setEnabled(false);
		jmiGridProp.addActionListener(this);
		jmiGridProp.setEnabled(false);
		jmiSuppleData.addActionListener(this);
		jmiDataSearch.addActionListener(this);
		jmiIsosurface.addActionListener(this);
		jmiTimeMorphing.addActionListener(this);
		// Can't animate what is not there yet
		jmiScreenCapture.addActionListener(viewerEventController);
		jmiCaptureAnim.addActionListener(this);
		jmiCaptureAnim.setEnabled(false);
		// Can't animate what is not there yet
		jmiMath.addActionListener(this);
		//jmiMath.setEnabled(false); // Can't do math on what is not there yet
		jmiAttributeFilter.addActionListener(this);
		jmiStormSummary.addActionListener(this);
		jmiNexradMosaic.addActionListener(this);
		//jmiNexradMosaic.setEnabled(false);
		jmiContourManager.addActionListener(this);


		jmiDroughtMonitor.addActionListener(this);
		jmiCDRImpactTool.addActionListener(this);
		
		jmiSpcStormReports.addActionListener(this);
		jmiGhcn.addActionListener(this);
		jmiNhc.addActionListener(this);




		jmiAbout.addActionListener(this);
		jmiTutorial.addActionListener(this);
		jmiQuickHelp.addActionListener(this);
		jmiNewFeatures.addActionListener(this);
		//jmiHelp.setEnabled(false);
		//        jmFile.add(jmiNew);
		jmFile.add(new JSeparator());
		jmFile.add(jmiOpen);
		//        jmFile.add(new JSeparator());
		jmFile.add(jmiSave);
		jmFile.add(jmiSaveAs);
		jmFile.add(new JSeparator());
		jmFile.add(jmiSaveImage);
		jmFile.add(jmiCopyImage);
		jmFile.add(new JSeparator());
		jmFile.add(jmiSaveKmz);
		int[] versionArray = WCTUtils.getJavaVersionMajorMinorBugBuild();
		if (versionArray[0] >= 1 && versionArray[1] >= 6) {
			jmFile.add(jmiLaunchKmz);
		}
		//jmFile.add(jmiPrint);
		jmFile.add(new JSeparator());
		jmFile.add(jmiCacheViewer);
		jmFile.add(new JSeparator());
		jmFile.add(jmiExit);

		jmDataServicesSubMenu.add(jmiDroughtMonitor);
//		jmDataServicesSubMenu.add(jmiSpcStormReports);
		jmDataServicesSubMenu.add(jmiGhcn);
//		jmDataServicesSubMenu.add(jmiCDRImpactTool);
		//        jmDataServicesSubMenu.add(jmiNhc);

		jmData.add(jmDataServicesSubMenu);
		//      jmData.add(jmiDataOrganizer);
		jmData.add(jmiLoadNexrad);
		jmData.add(new JSeparator());
		jmData.add(jmiOrderData);
		jmData.add(new JSeparator());
		jmData.add(jmiOverlay);
		//        jmData.add(new JSeparator());
		//        jmData.add(jmiExportNexrad);

		jmView.add(jmiBookmarks);
		jmView.add(jmiViewProp);
		jmView.add(jmiAlphaProp);
		jmView.add(jmiRadialProp);
		jmView.add(jmiGridProp);
		jmView.add(jmiSuppleData);
		jmView.add(new JSeparator());
		jmView.add(jmiRangeRings);
		jmView.add(jmiMarkerEditor);
		//jmView.add(jmiSavePS);
		jmTools.add(jmiScreenCapture);
		jmTools.add(jmiCaptureAnim);
		jmTools.add(new JSeparator());
		jmTools.add(jmiAttributeFilter);

//		jmTools.add(new JSeparator());
//		jmTools.add(jmiDataSearch);
		jmTools.add(new JSeparator());
		jmTools.add(jmiIsosurface);
		jmTools.add(jmiTimeMorphing);
		jmTools.add(jmiMath);

		
		
		//jmTools.add(jmiStormSummary);
		//jmTools.add(new JSeparator());
		//jmTools.add(jmiNexradMosaic);

//		        jmTools.add(new JSeparator());
//		        jmTools.add(jmiContourManager);

		jmHelp.add(jmiAbout);
		//      jmHelp.add(jmiHelp);
		jmHelp.add(jmiTutorial);
		jmHelp.add(jmiQuickHelp);
		jmHelp.add(jmiNewFeatures);

		jMenuBar1.add(jmFile);
		jMenuBar1.add(jmData);
		jMenuBar1.add(jmView);
		jMenuBar1.add(jmTools);
		jMenuBar1.add(jmHelp);

		//      ============== END COMMENT HERE ==========================

		// Set up Borders
		DropShadowBorder dropShadowBorder = new DropShadowBorder(Color.BLACK, 4, 0.8f, 5, false, false, true, true);
		Border border2 = BorderFactory.createCompoundBorder(dropShadowBorder, BorderFactory.createEmptyBorder(2, 2, 0, 0));
		Border mainBorder = BorderFactory.createCompoundBorder(border2, new RoundedBorder(new Color(10, 36, 106, 150), 2, 2));

//		messing around with borders
//		DropShadowBorder dropShadowBorder = new DropShadowBorder(Color.BLACK, 9, 0.8f, 5, false, false, true, true);
//		Border border2 = BorderFactory.createCompoundBorder(dropShadowBorder, BorderFactory.createEmptyBorder(2, 2, 0, 0));
//		Border mainBorder = BorderFactory.createCompoundBorder(border2, new RoundedBorder(new Color(10, 36, 106, 150), 2, 2));
		
		
		

		metaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		//infoPanel.add(metaPanel);
		//infoPanel.add(keyPanel);

		keyPanel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));


		//      infoPanel.add(metaPanel, BorderLayout.NORTH);
		infoPanel.add(keyPanel, BorderLayout.CENTER);

		//        infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		infoPanel.setBorder(mainBorder);


		progress = new JProgressBar(0, 100);
		progress.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));


		updateMemoryLabel();
		toolPanel.add(progress);


		jMenuBar1.setAlignmentX(JMenuBar.LEFT_ALIGNMENT);
		topPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));



		wctToolBar = new WCTToolBar(this);




		//mapPane.setBackground(new Color(159, 182, 205));
		wctMapPane.setBackground(new Color(0, 0, 55));
		wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
		wctMapPane.setDoubleBuffered(true);
		wctMapPane.addMouseListener(wctToolBar);
		wctMapPane.addMouseMotionListener(wctToolBar);

		mapPanel = new JPanel();
		mapPanel.setLayout(new BorderLayout());
		mapPanel.add(wctMapPane, BorderLayout.CENTER);
		mapPanel.setBorder(mainBorder);

		mapPanel.setDoubleBuffered(true);
		mainPanel.setDoubleBuffered(true);

		//      mapScrollPane = mapPane.createScrollPane();

		//JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScrollPane, infoScrollPane);




		final JPopupMenu servicesPopupMenu = new JPopupMenu("Data Services");
		JMenuItem titleItem = new JMenuItem("<html><b>Data Services</b></html>");
		titleItem.setToolTipText("List of available Data Services");
		titleItem.setEnabled(false);
		servicesPopupMenu.add(titleItem);
		servicesPopupMenu.addSeparator();

		JMenuItem item = new JMenuItem("Drought Monitor");
		item.setToolTipText("Launch the Drought Monitor display dialog");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleDroughtMonitor();
			}
		});
		JMenuItem item2 = new JMenuItem("SPC Storm Reports");
		item2.setToolTipText("Launch the SPC Storm Reports display dialog");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				 TODO change to toggle
				getSpcStormReportsUI();
			}
		});
		JMenuItem item3 = new JMenuItem("GHCN Export Tool");
		item3.setToolTipText("Launch the GHCN Export Tool dialog");
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleGhcnExportTool();
			}
		});
		JMenuItem item4 = new JMenuItem("CDR Impact Tool");
		item4.setToolTipText("Launch the CDR Impact Tool dialog");
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleCDRImpactTool();
			}
		});

		
		servicesPopupMenu.add(item);
		servicesPopupMenu.add(item3);
//		servicesPopupMenu.add(item4);

		servicesSelectorButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CCW, Font.decode(null), "Services"));
		servicesSelectorButton.setActionCommand("Services");
		servicesSelectorButton.addActionListener(viewerEventController);
		servicesSelectorButton.setToolTipText("Display Popup Menu for Available Data Services");
		servicesSelectorButton.addMouseListener(new MousePopupListener(servicesSelectorButton, servicesPopupMenu));





		dataSelectorButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CCW, Font.decode(null), "Data"));
		dataSelectorButton.setActionCommand("Data");
		dataSelectorButton.addActionListener(viewerEventController);
		dataSelectorButton.setToolTipText("Toggle Data Selector Visibility");
		mapSelectorButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CCW, Font.decode(null), "Layers"));
		mapSelectorButton.setActionCommand("Layers");
		mapSelectorButton.addActionListener(viewerEventController);
		mapSelectorButton.setToolTipText("Toggle Layer Selector Visibility");

		JXPanel leftPanel = new JXPanel();
		RiverLayout riverLayout = new RiverLayout();
		riverLayout.setHgap(0);
		riverLayout.setExtraInsets(new Insets(0, 2, 0, 2));
		leftPanel.setLayout(riverLayout);
		leftPanel.add(servicesSelectorButton);
		leftPanel.add(dataSelectorButton, "br");
		leftPanel.add(mapSelectorButton, "p");
		leftPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		//        BasicGradientPainter gradient = new BasicGradientPainter(BasicGradientPainter.WHITE_TO_CONTROL_HORIZONTAL);
		//        leftPanel.setBackgroundPainter(gradient);


		captureButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CW, Font.decode(null), "Capture"));
		captureButton.setActionCommand("Capture");
		captureButton.addActionListener(viewerEventController);
		captureButton.setToolTipText("Save a screen capture to the capture list");
		saveImageButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CW, Font.decode(null), "Save Image"));
		saveImageButton.setActionCommand("Save Image");
		saveImageButton.addActionListener(viewerEventController);
		saveImageButton.setToolTipText("Save image of the current map to disk");
		saveKmzButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CW, Font.decode(null), "Save KMZ"));
		saveKmzButton.setActionCommand("Save KMZ");
		saveKmzButton.addActionListener(viewerEventController);
		saveKmzButton.setToolTipText("Save a KMZ file of the current map to disk");
		openKmzButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CW, Font.decode(null), "Launch KMZ"));
		openKmzButton.setActionCommand("Launch KMZ");
		openKmzButton.addActionListener(viewerEventController);
		openKmzButton.setToolTipText("Opens a KMZ file of the current Map and Legend in Google Earth " +
		"or application currently registered for KMZ extensions");
		copyImageButton = new JButton(new RotatedTextIcon(RotatedTextIcon.CW, Font.decode(null), "Copy Image"));
		copyImageButton.setActionCommand("Copy Image");
		copyImageButton.addActionListener(viewerEventController);
		copyImageButton.setToolTipText("Copy image of the current map to clipboard");

		JXPanel rightPanel = new JXPanel();
		RiverLayout riverLayout2 = new RiverLayout();
		riverLayout2.setHgap(0);
		riverLayout2.setExtraInsets(new Insets(0, 2, 0, 2));
		rightPanel.setLayout(riverLayout2);
		rightPanel.add(captureButton);
		rightPanel.add(saveImageButton, "p");
		rightPanel.add(copyImageButton, "br");
		rightPanel.add(saveKmzButton, "p");
		if (versionArray[0] >= 1 && versionArray[1] >= 6) {
			rightPanel.add(openKmzButton, "br");
		}
		rightPanel.setBorder(BorderFactory.createLoweredBevelBorder());

		mainPanel.add(mapPanel, BorderLayout.CENTER);

		//        jnxTools.setBorder(BorderFactory.createRaisedBevelBorder());


		final WCTViewer finalThis = this;
		//        final JComboBox jcomboViewSelector = new JComboBox(new String[] { " Standard", " Google Earth", " Climate Services", " NIDIS" });

		jcomboViewSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (jcomboViewSelector.getSelectedItem().toString().trim().equals("Standard")) {
						setCurrentViewTypeValue(CurrentViewType.GEOTOOLS);
					}
					else if (jcomboViewSelector.getSelectedItem().toString().trim().equals("Google Earth")) {
						setCurrentViewTypeValue(CurrentViewType.GOOGLE_EARTH);
					}
					else if (jcomboViewSelector.getSelectedItem().toString().trim().equals("Google Earth Split")) {
						setCurrentViewTypeValue(CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS);
					}
					else if (jcomboViewSelector.getSelectedItem().toString().trim().equals("Climate Services")) {
						setCurrentViewTypeValue(CurrentViewType.NCDC_NCS);
					}
					else if (jcomboViewSelector.getSelectedItem().toString().trim().equals("NIDIS")) {
						setCurrentViewTypeValue(CurrentViewType.NCDC_NIDIS);
					}
					else if (jcomboViewSelector.getSelectedItem().toString().trim().equals("Four Pane")) {
						setCurrentViewTypeValue(CurrentViewType.FOUR_MAP_PANE);
					}
				} catch (BindException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(finalThis, 
							"Only one instance of the internal Weather and Climate Toolkit \n" +
							"Google Earth Browser may be open.  Please close the other \n" +
							"browser and try again.", "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
					try {
						jcomboViewSelector.setSelectedIndex(0);
						setCurrentViewTypeValue(CurrentViewType.GEOTOOLS);
					} catch (Exception ex2) {
						ex2.printStackTrace();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					//        JOptionPane.showMessageDialog(finalThis, ex.getMessage(), "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});


		this.setJMenuBar(jMenuBar1);
		JPanel topBarPanel = new JPanel(new BorderLayout());
		topBarPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		JButton geButton = null;
		try {
			geButton = new JButton(new ImageIcon(WCTViewer.class.getResource("/icons/ge-icon.png")));            
		} catch (Exception e1) {
			e1.printStackTrace();
			geButton = new JButton("Google Earth Viewer");
		}
		geButton.setToolTipText("Open the companion Google Earth viewer in another window");
		geButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        foxtrot.Worker.post(new foxtrot.Job() {
		            public Object run() {
				
						//                if (geExtBrowser == null) {
						geExtBrowser = GoogleEarthBrowser.launchWindow(finalThis);
						//                }
						//                else {
						//                    geExtBrowser.getTopLevelAncestor().setVisible(true);
						//                }

			        	return "DONE";
			        }			        
			    });

			}
		});




		JPanel viewSelectorPanel = new JPanel();
		viewSelectorPanel.add(new JLabel("Map View: "));
		viewSelectorPanel.add(jcomboViewSelector);
		viewSelectorPanel.add(geButton);

		topBarPanel.add(wctToolBar, BorderLayout.CENTER);

		if (! WCTUiUtils.classExists("org.eclipse.swt.SWT")) {
			//            jcomboViewSelector.removeAllItems();
			//            jcomboViewSelector.addItem(" Standard");
//			jcomboViewSelector.setEnabled(false);
			jcomboViewSelector.removeItem(" Google Earth Split");
			geButton.setEnabled(false);

			JLabel infoLabel = new JLabel(new ImageIcon(WCTViewer.class.getResource("/icons/question-mark.png")));
			infoLabel.addMouseListener( new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JOptionPane.showMessageDialog(finalThis, 
							"The integrated Google Earth viewer is currently only " +
							"supported on Windows operating systems. "
							, "Google Earth Info", JOptionPane.WARNING_MESSAGE);
				}
			} );
			viewSelectorPanel.add(infoLabel);
		}

		topBarPanel.add(viewSelectorPanel, BorderLayout.EAST);            

		if (! WCTLookAndFeel.isMacLookAndFeel()) {
			viewSelectorPanel.setBorder(wctToolBar.getBorder());
		}

		this.getContentPane().add(topBarPanel, BorderLayout.NORTH);
		this.getContentPane().add(leftPanel, BorderLayout.WEST);
		this.getContentPane().add(rightPanel, BorderLayout.EAST);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.getContentPane().add(statusBar, BorderLayout.SOUTH);



		try {
			ServicesMenuManager servicesManager = ServicesMenuManager.getInstance(this, jmDataServicesSubMenu, servicesPopupMenu);		
			servicesManager.addConfig(new File("ext/config/servicesMenu.xml").toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		// A global way to add a key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				//                System.out.println(e.getKeyCode()+"  ESCAPE=" + KeyEvent.VK_ESCAPE+" ENTER="+KeyEvent.VK_ENTER+" D="+KeyEvent.VK_D);
				//                System.out.println(e.getModifiers()+"  CTRL=" + KeyEvent.CTRL_DOWN_MASK);
				if (e.isShiftDown() && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_H) {                
					showNhc();
				}
				return false;
			}
		});
	}


	// END createGUI

	//==============================================================================

	// Action Performed Function
	//==============================================================================
	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
		updateMemoryLabel();

		Object source = evt.getSource();
		if (source == jmiNew) {
			clearAllData();
			setLegendVisibility(false);


			for (int n=0; n<wmsRGC.length; n++) {
				wmsRGC[n].setVisible(false);
			}

			wctMapPane.setVisibleArea(new java.awt.geom.Rectangle2D.Double(-125.0, 22.0, 58.0, 35.0));
			wctMapPane.setPreferredArea(new java.awt.geom.Rectangle2D.Double(-125.0, 22.0, 58.0, 35.0));
			wctMapPane.reset();

			updateMemoryLabel();




			dataSelect.setIsAutoExtentSelected(true);
			dataSelect.pack();
			dataSelect.setVisible(true);
			dataSelect.setLocation(this.getX()+15, this.getY()+15);
			dataSelect.setVisible(true);
			dataSelect.requestFocusInWindow();

			//mapPane.setVisibleArea(mapPane.getPreferredArea());

			fileString = "Untitled";
			projectFile = null;
			//            this.setTitle("NOAA Weather and Climate Toolkit - " + fileString);
			this.setTitle("NOAA Weather and Climate Toolkit");

		}
		else if (source == jmiPrint) {
			try {
				JNXPrintReport jnxPrint = new JNXPrintReport(this);
				jnxPrint.jetPrint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (source == jmiSave) {
			foxtrot.Worker.post(new foxtrot.Job() {
				public Object run() {
					if (fileString.equals("Untitled")) {
						saveFileAs();
					}
					else {
						saveFile();
					}
					return "Done";
				}
			});
		}
		else if (source == jmiSaveAs) {
			foxtrot.Worker.post(new foxtrot.Job() {
				public Object run() {
					saveFileAs();
					return "Done";
				}
			});
		}
		else if (source == jmiOpen) {
			// Put in thread
			try {
				foxtrot.Worker.post(new foxtrot.Job() {
					public Object run() {
						openFile();
						return "Done";
					}
				});
			} catch (Exception e) {
				javax.swing.JOptionPane.showMessageDialog(this, "OPEN FILE ERROR:\n"+e, "OPEN FILE ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);         
				e.printStackTrace();
			}
		}
		//        else if (source == jmiSaveImage) {
		//            ViewerUtilities.saveImage(this);
		//        }
		//        else if (source == jmiCopyImage) {
		//            copyViewToClipboard();
		//        }
		else if (source == jmiCacheViewer) {
			// Start the Web Start Cache Viewer
			String javaVersion = System.getProperty( "java.version" );

			//          System.out.println(javaVersion);
			//          System.out.println(javaVersion.compareTo("1.4.2"));
			//          System.out.println(javaVersion.compareTo("1.6"));
			//          System.out.println(javaVersion.compareTo("1.5.0_04.1"));

			if (javaVersion.compareTo("1.6") > 0) {
				gov.noaa.ncdc.common.GoodWindowsExec.exec(new String[] {"javaws -viewer"});
			}
			else {
				gov.noaa.ncdc.common.GoodWindowsExec.exec(new String[] {"javaws"});
			}
		}
		else if (source == jmiExit) {
			// Exit the Application
			exitProgram();
		}
//		else if (source == jmiDataOrganizer) {
//
//			// Load DataSelector Frame
//			if (dataOrganizer == null) {
//				dataOrganizer = new DataOrganizer(this);
//			}
//
//			dataOrganizer.setSize(650, 400);
//			dataOrganizer.setVisible(true);
//			dataOrganizer.setLocation(this.getX()+25, this.getY()+25);
//		}
		else if (source == jmiLoadNexrad) {
			showDataSelector();
		}
		else if (source == jmiOrderData) {
			try {
				BareBonesBrowserLaunch.openURL("http://www.ncdc.noaa.gov/wct/data.php");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "No Default Browser found.\n"+
						"Please direct you browser to \" http://www.ncdc.noaa.gov/wct/data.php \"", 
						"BROWSER CONTROL ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
			}
		}
		//        else if (source == jmiOrderSatellite) {
		//            try {
		//                BareBonesBrowserLaunch.openURL("http://www.class.noaa.gov");
		//            } catch (Exception e) {
		//                JOptionPane.showMessageDialog(this, "No Default Browser found.\n"+
		//                        "Please direct you browser to \" http://www.class.noaa.gov \"", 
		//                        "BROWSER CONTROL ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
		//            }
		//        }
		//        else if (source == jmiOrderXMRG) {
		//            try {
		//                BareBonesBrowserLaunch.openURL("http://dipper.nws.noaa.gov/hdsb/data/nexrad/nexrad.html");
		//            } catch (Exception e) {
		//                JOptionPane.showMessageDialog(this, "No Default Browser found.\n"+
		//                        "Please direct you browser to \" http://dipper.nws.noaa.gov/hdsb/data/nexrad/nexrad.html \"", 
		//                        "BROWSER CONTROL ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
		//            }
		//        }



		else if (source == jmiOverlay) {
			showMapSelector();
		}
//		else if (source == jmiExportNexrad) {
//			try {
//				// Save NEXRAD in multiple formats
//				if (nexradExporter == null) {
//					if (nexradExporter == null) {
//						nexradExporter = WCTExportGUI.createInstance(this, "Data Exporter");
//					}
//					zoomChangeListener.setNexradFilterGUI(nexradExporter.getFilterGUI());
//
//				}
//				nexradExporter.setLocation(this.getX()+25, this.getY()+25);
//				nexradExporter.setVisible(true);
//				nexradExporter.setExtendedState(Frame.NORMAL);
//
//				nexradExporter.getDataSourcePanel().setDataType(dataSelect.getDataSourcePanel().getDataType());
//				nexradExporter.getDataSourcePanel().setDataLocation(dataSelect.getDataSourcePanel().getDataType(),
//						dataSelect.getDataSourcePanel().getDataLocation());
//				nexradExporter.setScanResults(dataSelect.getScanResults());
//				nexradExporter.setSelectedIndices(dataSelect.getSelectedIndices());
//
//
//				//              DataSelectorPanel dataSelectPanel = dataSelect.getDataSelectorPanel();
//				//              if (first) {
//				//              first = false;
//				//              nexradExporter.getDataSelectorPanel().setFileLocation(dataSelectPanel.getFileLocation());
//				//              if (dataSelectPanel.getFileLocation() == DataSelectorPanel.LOCATION_NCDC_FTP) {
//				//              nexradExporter.getDataSelectorPanel().setHASNumber(dataSelectPanel.getHASNumber());
//				//              if (dataSelectPanel.getHASNumber().trim().length() > 0) {
//				//              nexradExporter.getDataSelectorPanel().listHASFiles();
//				//              }
//				//              }
//				//              else if (dataSelectPanel.getFileLocation() == DataSelectorPanel.LOCATION_LOCAL) {
//				//              nexradExporter.getDataSelectorPanel().setLocalDirectory(dataSelectPanel.getLocalDirectory());
//				//              if (dataSelectPanel.getLocalDirectory().trim().length() > 0) {
//				//              nexradExporter.getDataSelectorPanel().listLocalFiles(new File(dataSelectPanel.getLocalDirectory()));
//				//              }
//				//              }
//				//              else if (dataSelectPanel.getFileLocation() == DataSelectorPanel.LOCATION_CUSTOM) {
//				//              nexradExporter.getDataSelectorPanel().setCustomURL(dataSelectPanel.getCustomURL());
//				//              if (dataSelectPanel.getCustomURL().trim().length() > 0) {
//				//              nexradExporter.getDataSelectorPanel().listCustomFiles(dataSelectPanel.getCustomURL());
//				//              }
//				//              }
//
//				//              /*
//				//              else if (dataSelect.getFileLocation() == DataSelector.LOCATION_LOCAL) {
//				//              nexradExporter.setLocalDirectory(dataSelect.getLocalDirectory());
//				//              nexradExporter.listLocalFiles(new File(dataSelect.getLocalDirectory()));
//				//              }
//				//              else {
//				//              }
//				//              */
//				//              nexradExporter.getDataSelectorPanel().setSelectedIndex(dataSelectPanel.getSelectedIndex());
//				//              }
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		}
		else if (source == jmiBookmarks) {
			if (bookmarkEditor == null) {
				bookmarkEditor = new BookmarkUI(this);
			}
			bookmarkEditor.setSize(350, 240);
			bookmarkEditor.setVisible(true);
			bookmarkEditor.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiViewProp) {
			// Load ViewProperties Frame
			//viewProperties.pack();
			viewProperties.setSize(400, 290);
			viewProperties.setVisible(true);
			viewProperties.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiRangeRings) {
			// Lazy creation of Range Rings Frame
			if (rangeRings == null) {
				rangeRings = new RangeRingsGUI(this, this);
			}

			rangeRings.pack();
			rangeRings.setVisible(true);
			rangeRings.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiMarkerEditor) {
			// Lazy creation of Range Rings Frame
			if (markerEditor == null) {
				markerEditor = new MarkerEditor(mlMarkers, markerFeatures, this);
			}

			markerEditor.pack();
			markerEditor.setVisible(true);
			markerEditor.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiAlphaProp) {

			if (alphaProperties == null) {
				alphaProperties = new AlphaProperties(this, dataSelect);
			}

			// Load AlphaProperties Frame
			alphaProperties.pack();
			alphaProperties.setVisible(true);
			alphaProperties.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiRadialProp) {
			radialProps.setVisible(true);
			radialProps.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiGridProp) {
			gridProps.setVisible(true);
			gridProps.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiSuppleData) {
			suppleData.setVisible(true);
			suppleData.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiCaptureAnim) {

			// Load Animator Frame
			if (captureAnimate == null) {
				captureAnimate = new CaptureAnimator(this);
			}

			captureAnimate.setVisible(true);
			captureAnimate.setLocation(this.getX()+25, this.getY()+25);
			captureAnimate.pack();
			captureAnimate.setExtendedState(Frame.NORMAL);
		}
		else if (source == jmiMath) {
			RasterMathDialog mathUI = new RasterMathDialog(this);
		}
		else if (source == jmiAttributeFilter) {

			// Load Animator Frame
			//if (nxfilterGUI == null) {
			//   nxfilterGUI = new NexradFilterGUI("Nexrad Filter");
			//}

			wctFilterGUI.setVisible(true);
			wctFilterGUI.setLocation(this.getX()+25, this.getY()+25);
			wctFilterGUI.pack();
		}
//		else if (source == jmiStormSummary) {
//			// Load Animator Frame
//			if (stormSummaryGUI == null) {
//				stormSummaryGUI = new StormSummaryGUI(this);
//			}
//			stormSummaryGUI.setVisible(true);
//			stormSummaryGUI.setLocation(this.getX()+25, this.getY()+25);
//			stormSummaryGUI.setExtendedState(Frame.NORMAL);
//		}
		//      else if (source == jmiNexradMosaic) {
		//      // Load Animator Frame
		//      if (nexradMosaic == null) {
		//      nexradMosaic = new NexradMosaic(this, dataSelect);
		//      }

		//      nexradMosaic.setVisible(true);
		//      nexradMosaic.setLocation(this.getX()+25, this.getY()+25);
		//      //nexradAnimate.setSize(750,600);
		//      nexradMosaic.setExtendedState(Frame.NORMAL);
		//      }
		else if (source == jmiDataSearch) {
			
//			if (isosurfaceUI == null) {

			DataSearchDialog dataSearchUI = DataSearchDialog.getInstance(this);
			dataSearchUI.setVisible(true);
			dataSearchUI.setLocation(this.getX()+25, this.getY()+25);
			
					
//			}
//			isosurfaceUI.setVisible(true);
//			isosurfaceUI.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiIsosurface) {
			
//			if (isosurfaceUI == null) {
				IsosurfaceDialog isosurfaceUI = new IsosurfaceDialog(this);
//			}
//			isosurfaceUI.setVisible(true);
//			isosurfaceUI.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiTimeMorphing) {
			
//			if (isosurfaceUI == null) {
				MorphDialog morphDialog = MorphDialog.getInstance(this);
		        morphDialog.setVisible(true);
//			}
//			isosurfaceUI.setVisible(true);
//			isosurfaceUI.setLocation(this.getX()+25, this.getY()+25);
		}
		else if (source == jmiContourManager) {
			try {
				if (contourDialog == null) {
					contourDialog = new ContourUI(this);
				}
				contourDialog.pack();
				contourDialog.setVisible(true);
				contourDialog.setLocation(this.getX()+25, this.getY()+25);

			} catch (Throwable e) {
				e.printStackTrace();
				System.err.flush();
			}

		}
		else if (source == jmiDroughtMonitor) {
			try {
				if (droughtMonitor == null) {
					droughtMonitor = new NdmcDroughtMonitorUI(this);
				}
				droughtMonitor.setVisible(true);
				droughtMonitor.setLocation(this.getX()+25, this.getY()+25);

			} catch (Throwable e) {
				e.printStackTrace();
				System.err.flush();
			}
		}
		else if (source == jmiCDRImpactTool) {
			try {
				CDRImpactToolUI impactTool = CDRImpactToolUI.getInstance(this);
				impactTool.setVisible(true);
				impactTool.setLocation(this.getX()+25, this.getY()+25);

			} catch (Throwable e) {
				e.printStackTrace();
				System.err.flush();
			}
		}
		else if (source == jmiSpcStormReports) {
			try {
				if (spcStormReports == null) {
					spcStormReports = new SpcStormReportsUI(this);
				}
				spcStormReports.setVisible(true);
				spcStormReports.setLocation(this.getX()+25, this.getY()+25);

			} catch (Throwable e) {
				e.printStackTrace();
				System.err.flush();
			}
		}
		else if (source == jmiGhcn) {
			try {
				if (ghcnTool == null) {
					ghcnTool = GhcnOrderExportDialog.getInstance(this);
				}
				ghcnTool.setVisible(true);
				ghcnTool.setLocation(this.getX()+25, this.getY()+25);

			} catch (Throwable e) {
				e.printStackTrace();
				System.err.flush();
			}
		}
		else if (source == jmiNhc) {
			showNhc();
		}


		else if (source == jmiAbout) {
			String stableVersion = WCTUiUtils.checkCurrentStableVersion();
			String betaVersion = WCTUiUtils.checkCurrentBETAVersion();
			String upgradeString = "";
			if (! WCTUiUtils.getVersion().equals(stableVersion) && ! WCTUiUtils.getVersion().equals(betaVersion)) {
				upgradeString = "--- Update Available! --- \nGo to: http://www.ncdc.noaa.gov/wct/ \n";
			}
			if (stableVersion.equals(betaVersion)) {
				betaVersion = "N/A";
			}

			String message = "NOAA Weather and Climate Toolkit \n" +
			"Author: Steve Ansari \n" +
			"National Climatic Data Center \n" +
			"Contact: Steve.Ansari@noaa.gov \n\n" +
			"This Version: " + WCTUiUtils.getVersion()+ "\n"+
			"Current Stable Version: " + stableVersion+ "\n"+
			"Current BETA Version: " + betaVersion+ "\n"+
			upgradeString;
			JOptionPane.showMessageDialog(this, message,
					"About", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (source == jmiHelp) {
			// Nothing needed here because HelpBroker takes listens for events
		}
		else if (source == jmiTutorial) {
			try {
				BareBonesBrowserLaunch.openURL("http://www.ncdc.noaa.gov/wct/tutorials/");
			} catch (Exception e) {
				javax.swing.JOptionPane.showMessageDialog(this, "No Default Browser found.\n"+
						"Please direct you browser to \" http://www.ncdc.noaa.gov/wct/tutorials/ \"", 
						"BROWSER CONTROL ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
			}
		}
		else if (source == jmiQuickHelp) {
			// Lazy object creation
			if (jnxhelp == null) {
				jnxhelp = new JNXHelp(this, "/helphtml/help.html");
			}
			jnxhelp.setLocation(this.getX()+25, this.getY()+25);
			jnxhelp.setVisible(true);
		}
		else if (source == jmiNewFeatures) {
			// Lazy object creation
			if (jnxnew == null) {
				jnxnew = new NewJNXFeatures(this, "/helphtml/newfeatures.html");
			}
			jnxnew.setLocation(this.getX()+25, this.getY()+25);
			jnxnew.setVisible(true);
		}
		else if (evt.getActionCommand().equals(ACTION_LOAD)) {
			if (radialProps != null) {
				radialProps.setLoadButtonEnabled(false);
			}

			reloadData();
		}
		else if (evt.getActionCommand().equals(ACTION_REFRESH)) {
			if (radialProps != null) {
				radialProps.setLoadButtonEnabled(false);
			}

			refreshRadarData();
		}
	}





	//    public void showDataServicesPopUpMenu() {
	//            
	//        final JPopupMenu servicesPopupMenu = new JPopupMenu("Data Services");
	//        JMenuItem item = new JMenuItem("Drought Monitor");
	//        item.addActionListener(new ActionListener() {
	//            public void actionPerformed(ActionEvent e) {
	//                toggleDroughtMonitor();
	////                servicesPopupMenu.
	//            }
	//        });
	//        servicesPopupMenu.add(item);
	////        servicesPopupMenu.setLocation(40, 40);
	////        servicesPopupMenu.setVisible(true);
	//        
	//        
	//    }

	public void toggleDroughtMonitor() {
		try {
			if (droughtMonitor == null) {
				droughtMonitor = new NdmcDroughtMonitorUI(this);
			}
			droughtMonitor.setVisible(! droughtMonitor.isVisible());
			droughtMonitor.setLocation(this.getX()+25, this.getY()+25);

		} catch (Throwable e) {
			e.printStackTrace();
			System.err.flush();
		}
	}
	public void toggleGhcnExportTool() {
		try {
			if (ghcnTool == null) {
				ghcnTool = GhcnOrderExportDialog.getInstance(this);
			}
			ghcnTool.setVisible(! ghcnTool.isVisible());
			ghcnTool.setLocation(this.getX()+25, this.getY()+25);

		} catch (Throwable e) {
			e.printStackTrace();
			System.err.flush();
		}
	}
	public void toggleCDRImpactTool() {
		try {
			CDRImpactToolUI impactTool = CDRImpactToolUI.getInstance(this);
			impactTool.setVisible(! impactTool.isVisible());
			impactTool.setLocation(this.getX()+25, this.getY()+25);

		} catch (Throwable e) {
			e.printStackTrace();
			System.err.flush();
		}
	}


	public void showDataSelector() {
		// Load DataSelector Frame
		if (dataSelect == null) {
			dataSelect = new DataSelector(this);
		}

		//dataSelect.pack();
		dataSelect.setVisible(true);
		dataSelect.setLocation(this.getX()+39, this.getY()+25);
	}
	public void toggleDataSelector() {
		// Load DataSelector Frame
		if (dataSelect == null) {
			dataSelect = new DataSelector(this);
		}

		dataSelect.setVisible(! dataSelect.isVisible());
		dataSelect.setLocation(this.getX()+39, this.getY()+35);
	}

	public void showMapSelector() {
		// Load MapSelector Frame
		if (mapSelect == null) {
			try {
				mapSelect = new MapSelector(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			zoomChangeListener.setWMSPanel(mapSelect.getWMSPanel());
		}

		//mapSelect.pack();
		mapSelect.setLocation(this.getX()+39, this.getY()+25);
		mapSelect.setVisible(true);        
	}
	public void toggleMapSelector() {
		// Load DataSelector Frame
		if (mapSelect == null) {
			try {
				mapSelect = new MapSelector(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mapSelect.setVisible(! mapSelect.isVisible());
		mapSelect.setLocation(this.getX()+39, this.getY()+35);
	}



	public void showNhc() {
		if (nhcTracks == null) {
			nhcTracks = new NhcTracksUI(this);
		}
		nhcTracks.setVisible(true);
		nhcTracks.setLocation(this.getX()+25, this.getY()+25);
	}











	//-----------------------------------------------------------
	// Implementation of NexradDecodeListener interface
	//-----------------------------------------------------------

	/**
	 *  Implementation of NexradDecodeListener interface
	 *
	 * @param  decodeEvent  Description of the Parameter
	 */
	//  @Override
	public void decodeStarted(DataDecodeEvent decodeEvent) {
		// set overall progress for NexradExportEvent listeners
		statusBar.resetProgress(0);

		if (decodeEvent.getSource() == goesAreaRaster) {
			statusBar.setProgressText("Processing Data...");
		}
		else if (decodeEvent.getSource() == gridDatasetRaster) {
			statusBar.setProgressText("Processing Data...");
			//            statusBar.setProgressText(decodeEvent.getStatus());
		}
		else if (decodeEvent.getSource().getClass().toString().endsWith("SmoothingOperation")) {
			statusBar.setProgressText("Smoothing Data...");
		}
		else {
			statusBar.setProgressText("Decoding Data...");
			//event.setStatus("Decoding " + event.getOutputFile().getName());
		}

		if (animationRangeModel != null) {
			animationRangeModel.setValue(0);
		}
		for (int n=0; n<renderCompleteListeners.size(); n++) {
			renderCompleteListeners.get(n).renderProgress(0);
		}

	}


	/**
	 *  Implementation of NexradDecodeListener interface
	 *
	 * @param  decodeEvent  Description of the Parameter
	 */
	//  @Override
	public void decodeProgress(DataDecodeEvent decodeEvent) {

		if (decodeEvent.getSource() == goesAreaRaster ||
				decodeEvent.getSource() == gridDatasetRaster ||
				decodeEvent.getSource() == radialDatasetRaster ||
				decodeEvent.getSource().getClass().toString().endsWith("SmoothingOperation")) {

			statusBar.setProgressText(decodeEvent.getStatus());
			statusBar.resetProgress(decodeEvent.getProgress());
			if (animationRangeModel != null) {
				animationRangeModel.setValue(decodeEvent.getProgress());
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress(decodeEvent.getProgress());
			}
		}
		else {


			// set overall progress for NexradExportEvent listeners
			statusBar.resetProgress(decodeEvent.getProgress() / 2);
			if (animationRangeModel != null) {
				animationRangeModel.setValue(decodeEvent.getProgress() / 2);
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress(decodeEvent.getProgress() / 2);
			}
		}
	}


	/**
	 *  Implementation of NexradDecodeListener interface
	 *
	 * @param  decodeEvent  Description of the Parameter
	 */
	//  @Override
	public void decodeEnded(DataDecodeEvent decodeEvent) {
		if (decodeEvent.getSource() == goesAreaRaster || 
				decodeEvent.getSource() == gridDatasetRaster ||
				decodeEvent.getSource() == radialDatasetRaster ||
				decodeEvent.getSource().getClass().toString().endsWith("SmoothingOperation")) {

			statusBar.resetProgress(0);
			statusBar.setProgressText("");
			if (animationRangeModel != null) {
				animationRangeModel.setValue(0);
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress(0);
			}
		}
		else {
			statusBar.resetProgress(50);
			//statusBar.setProgressText("");
			if (animationRangeModel != null) {
				animationRangeModel.setValue(50);
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress(50);
			}
		}
	}

	public void metadataUpdate(DataDecodeEvent decodeEvent) {
		if (decodeEvent.getSource() == radialDatasetRaster) {
			if (decodeEvent.getDataType() == SupportedDataType.RADIAL) {                
				NexradLegendLabelFactory.setSpecialLevel2LegendLabels(radLegendProducer, radialDatasetHeader, radialDatasetRaster);
				try {
//					System.out.println(Arrays.toString(radLegendProducer.getCategoryColors()));
//					System.out.println(Arrays.toString(radLegendProducer.getCategoryLabels()));
					keyPanel.setLegendImage(radLegendProducer);
					keyPanel.repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	//-----------------------------------------------------------
	// Implementation of GeneralProgressListener interface
	//-----------------------------------------------------------

	/**
	 *  Implementation of GeneralProgressListener interface
	 *
	 * @param  generalEvent  Description of the Parameter
	 */
	//  @Override
	public void started(GeneralProgressEvent generalEvent) {
		// set overall progress for NexradExportEvent listeners
		if (generalEvent.getSource() == rasterizer) {
			statusBar.resetProgress(50);
			statusBar.setProgressText("Processing Data...");
			//event.setStatus("Rasterizing " + event.getOutputFile().getName());

			if (animationRangeModel != null) {
				animationRangeModel.setValue(50);
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress(50);
			}
		}

	}


	/**
	 *  Implementation of GeneralProgressListener interface
	 *
	 * @param  generalEvent  Description of the Parameter
	 */
	//  @Override
	public void progress(GeneralProgressEvent generalEvent) {

		if (generalEvent.getSource() == rasterizer) {
			// Rasterizer progress - 50% to 100% of total
			statusBar.resetProgress((int)(50 + generalEvent.getProgress() / 2));
			//event.setProgress(50 + generalEvent.getProgress() / 2);
			// set overall progress for NexradExportEvent listeners

			if (animationRangeModel != null) {
				animationRangeModel.setValue((int)(50 + generalEvent.getProgress() / 2));
			}
			for (int n=0; n<renderCompleteListeners.size(); n++) {
				renderCompleteListeners.get(n).renderProgress((int)(50 + generalEvent.getProgress() / 2));
			}
		}
	}


	/**
	 *  Implementation of GeneralProgressListener interface
	 *
	 * @param  generalEvent  Description of the Parameter
	 */
	//  @Override
	public void ended(GeneralProgressEvent generalEvent) {
		statusBar.resetProgress(0);
		statusBar.setProgressText("");
		//event.setStatus("NEXRAD Processing Complete");
		System.out.println("GENERAL PROCESS ENDED EVENT");
		if (animationRangeModel != null) {
			animationRangeModel.setValue(0);
		}
		for (int n=0; n<renderCompleteListeners.size(); n++) {
			renderCompleteListeners.get(n).renderProgress(0);
		}

	}



	//  @Override
	public void loadData() {
		reloadData();
	}


























	// END ActionPerformed FUNCTION

	//**************************************************************
	//**************************************************************

	public void reloadData() {
		// Put in thread               
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				loadFile(dataUrl, false, false, false, false);
				return "Done";
			}
		};
		worker.start();

		updateMemoryLabel();
	}

	//**************************************************************
	//**************************************************************

	public void refreshRadarData() {
		refreshRadarData(true);
	}
//	public synchronized void refreshRadarData(final boolean clipToExtent) {
	public void refreshRadarData(final boolean clipToExtent) {


		
		if (dataUrl == null || scannedFile.getLastScanResult() == null) {
			return;
		}


		gcSupport.setSmoothFactor(radarSmoothFactor);

		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_2D ||
				scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_3D) {

			return;
		}

		final Component parent = this; 

//		if (isLoading) {
//			return;
//		}
		// Put in thread               
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
//				setIsLoading(true);
				Object status = refreshRadarDataWithWait(clipToExtent);
//				setIsLoading(false);
				return status;
			}
		};
		worker.start();


		updateMemoryLabel();
	}





	public Object refreshRadarDataWithWait(final boolean clipToExtent) {
		

		// Indicate that new save is needed upon closing
		//                setTitle("NOAA Weather and Climate Toolkit - " + fileString + " *");


//		if (dataSelect != null) {
//			dataSelect.setIsLoading(true);
//		}
		setIsLoading(true);



		zoomChangeListener.setRadarActive(false);
		if (radialProps != null) {
			radialProps.setLoadButtonEnabled(false);
		}

		System.out.println("---------------- START REFRESH ---------------");

		if (nexradFeatures == null) {
			JOptionPane.showMessageDialog(this, "No NEXRAD Data is currently loaded.",
					"NEXRAD REFRESH ERROR", JOptionPane.ERROR_MESSAGE);
			setIsLoading(false);
			return "ERROR";
		}

		java.awt.geom.Rectangle2D.Double currentExtent = getCurrentExtent();
		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
			setIsLoading(false);
			return "GRIDDED";
		}
		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.POINT_TIMESERIES) {
			return "POINT";
		}
		
		
		if (header == null) {
			setIsLoading(false);
			return "";
		}
		
		java.awt.geom.Rectangle2D.Double maxExtent = MaxGeographicExtent.getNexradExtent(header);
		if (currentExtent.getWidth() > maxExtent.getWidth()) {
			currentExtent = maxExtent;
		}


		//TODO : Add smoothing bounds limit so smoothed areas is limited at a certain scale factor

		// adjust current extent if we are smoothing
		if (radarSmoothFactor > 0) {
			//        	double xShift = (currentExtent.width - currentExtent.width/radarSmoothExtentEnlargementFactor);
			//        	double yShift = (currentExtent.height - currentExtent.height/radarSmoothExtentEnlargementFactor);
			//            currentExtent = new java.awt.geom.Rectangle2D.Double(
			//                    currentExtent.x - xShift,
			//                    currentExtent.y - yShift,
			//                    currentExtent.width + 2*xShift,
			//                    currentExtent.height + 2*yShift);
			SmoothingOperation smop = new SmoothingOperation();
			currentExtent = smop.adjustSmoothingExtent(currentExtent, wctMapPane.getWidth(), wctMapPane.getHeight(), (int)radarSmoothFactor);
		}


		System.out.println("---------------- START RASTERIZER --------------------");
		// RASTERIZE THE DATA
		boolean isRasterVariableRes = ! clipToExtent; // use current extent
		try {


			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {

				boolean classify = false;
				String variableName = radialProps == null ? radialDataset.getDataVariables().get(0).toString() : radialProps.getRadialPropsPanel().getVariableName();


				radialDatasetRaster.setVariableName(variableName);
				if (radialProps != null) {
					radialDatasetRaster.setSweepIndex(radialProps.getRadialPropsPanel().getCut());
				}
				radialDatasetRaster.setWctFilter(wctFilter);
				dataUrl = WCTDataUtils.scan(dataUrl, scannedFile, useWctCache, true, dataSelect.getSelectedDataType(), this);
				if (radialProps != null && Double.isNaN(radialProps.getRadialPropsPanel().getCappiAltitude())) {
					radialDatasetRaster.process(dataUrl.toString(), currentExtent);
				}
				else {
					radialDatasetRaster.processCAPPI(dataUrl.toString(), currentExtent, 
							new double[] { radialProps.getRadialPropsPanel().getCappiAltitude() }, 
							radialProps.getRadialPropsPanel().getCurrentCappiType());
				}
				
				
				statusBar.setNexradElevationAngle(radialDatasetRaster.getLastDecodedElevationAngle());
				this.lastDecodedElevationAngle = radialDatasetRaster.getLastDecodedElevationAngle();
				radialDatasetRaster.setSmoothingFactor((int)radarSmoothFactor);
				radarGC = radialDatasetRaster.getGridCoverage(radarAlphaChannelValue);
				radarRGC.setGridCoverage(radarGC);

				radarRGC.setVisible(true);


			} 
			else if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) 
					&& header.getProductType() == NexradHeader.L3RADIAL_8BIT) {

				if (isRasterVariableRes) {
					rasterizer.rasterize(nexradFeatures, "value");
				}
				else {
					rasterizer.rasterize(nexradFeatures, currentExtent, "value");
				}		
				
				gcSupport.setWctFilter(wctFilter);
				radarGC = gcSupport.getGridCoverage(rasterizer, header, radarAlphaChannelValue);
				radarRGC.setGridCoverage(radarGC);

			}
			else {

				if (header.getProductType() != NexradHeader.L3ALPHA && 
						header.getProductType() != NexradHeader.L3VAD &&
						header.getProductType() != NexradHeader.L3GSM &&
						header.getProductType() != NexradHeader.L3RSL) {

					if (isRasterVariableRes) {
						rasterizer.rasterize(nexradFeatures, "colorIndex");
					}
					else {
						rasterizer.rasterize(nexradFeatures, currentExtent, "colorIndex");
					}

					gcSupport.setWctFilter(wctFilter);
					radarGC = gcSupport.getGridCoverage(rasterizer, header, true, 
							NexradColorFactory.getTransparentColors(header.getProductCode(), true, radarAlphaChannelValue));
					radarRGC.setGridCoverage(radarGC);

					try {
						SearchDialog.getInstance(this).setAutoFillDataType(getFileScanner().getLastScanResult().getDataType().toString());
						SearchDialog.getInstance(this).setAutoFillFileFormat("NEXRAD-based product");
						SearchDialog.getInstance(this).setAutoFillVariableUnits(rasterizer.getUnits());
						SearchDialog.getInstance(this).setAutoFillVariableName(rasterizer.getVariableName());
						SearchDialog.getInstance(this).setAutoFillVariableDesc(rasterizer.getLongName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e) {
			if (e.getMessage().startsWith("Operation canceled")) {
				setIsLoading(false);
				return "CANCELED";
			}
			else {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "General Processing Error", JOptionPane.ERROR_MESSAGE);
			}			

		} catch (FeatureRasterizerException e) {
			if (e.getMessage().startsWith("Operation canceled")) {
				setIsLoading(false);
				return "CANCELED";
			}
			else {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "General Processing Error", JOptionPane.ERROR_MESSAGE);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "General Error", JOptionPane.ERROR_MESSAGE);
		}
		System.out.println("---------------- FINISH RASTERIZER --------------------");

		radarRGC.setVisible(true);

		
		if (Math.abs(radarGC.getEnvelope().getMinimum(0) - getCurrentExtent().getMinX()) > 0.001 || 
			Math.abs(radarGC.getEnvelope().getMinimum(1) - getCurrentExtent().getMinY()) > 0.001 ||
			Math.abs(radarGC.getEnvelope().getLength(0) - getCurrentExtent().getWidth()) > 0.001 || 
			Math.abs(radarGC.getEnvelope().getLength(1) - getCurrentExtent().getHeight()) > 0.001
					) {
			System.out.println(radarGC.getEnvelope());
			System.out.println(getCurrentExtent());
//			refreshRadarData();			
			
		}
		
		
		
		zoomChangeListener.setRadarActive(true);
//		if (dataSelect != null) {
//			dataSelect.setIsLoading(false);
//			dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		}
		if (radialProps != null) {
			radialProps.setLoadButtonEnabled(true);
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		isLoading = false;
		setIsLoading(false);


		fireRenderCompleteEvent();
		return "Done";
	}







	/**
	 *  Description of the Method
	 * @throws MalformedURLException 
	 */
	public void loadBaseMaps() throws MalformedURLException {


		System.gc();
		boolean showProgressFrame = false;

		//-----------------------------*
		// Start GeoTools
		//-----------------------------*


		// Set initial coordinate system
		setCoordinateSystem(GeographicCoordinateSystem.WGS84);



		// Update splash window
		splashWindow.setStatus("Loading states base map", 15);
		BaseMapManager baseMapManager = new BaseMapManager();
		baseMapManager.loadBaseMaps(this);
		baseMapLayers.clear();
		baseMapLayers = baseMapManager.getBaseMapLayers();
		baseMapLabelLayers.clear();
		baseMapLabelLayers = baseMapManager.getBaseMapLabelLayers();
		baseMapStyleInfo.clear();
		baseMapStyleInfo = baseMapManager.getBaseMapStyleInfo();


		// Update splash window
		splashWindow.setStatus("Adding Layers to Map (0/" + NUM_LAYERS + ")", 50);


		try {


			// init the wmsRGCs
			for (int n=0; n<wmsRGC.length; n++) {
				wmsRGC[n] = new RenderedGridCoverage(new GridCoverage("OGCWMS"+n,
						RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, 2, 2, 1, null),
						new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
				)
				);

				((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(wmsRGC[n]);
				wmsRGC[n].setZOrder(2+((float)n)/100 + 0.2f);
				wmsRGC[n].setVisible(false);
			}

			((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(gridSatelliteRGC);
			gridSatelliteRGC.setZOrder(2+0.800f);
			gridSatelliteRGC.setVisible(true);

			((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(radarRGC);
			radarRGC.setZOrder(2+0.801f);
			radarRGC.setVisible(true);


			((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(contourRGC);
			contourRGC.setZOrder(300f);
			contourRGC.setVisible(true);





			// added method in JNXMapContext
			//            map.setSize(2*NUM_LAYERS + 2);
			map.setSize(200);

			// LAYERS TO INTIALLY LOAD
			for (int i = 0; i < NUM_LAYERS; i++) {
				if (baseMapLayers.elementAt(i) != null) {
					if (i == COUNTRIES || i == COUNTRIES_OUT || i == COUNTRIES_USA || i == COUNTRIES_OUT_USA ||
						i == STATES || i == STATES_OUT || i == HWY_INT) {
						map.addLayer(i, (MapLayer) baseMapLayers.elementAt(i));
						//((StyledMapRenderer)mapPane.getRenderer()).insertLayer((MapLayer) baseMapThemes.elementAt(i), i);
					}
					else {
						map.addLayer(i, new DefaultMapLayer(org.geotools.feature.FeatureCollections.newCollection(), sb.createStyle()));
					}
					splashWindow.setStatus("Adding Layers to Map (" + i + "/" + NUM_LAYERS + ")", 50 + i);
				}
				else {
					map.addLayer(i, new DefaultMapLayer(org.geotools.feature.FeatureCollections.newCollection(), sb.createStyle()));
				}
			}


			// Add range ring layer
			Style rangeRingStyle = sb.createStyle(sb.createLineSymbolizer(Color.red, 1));
			mlNexradRangeRings = new DefaultMapLayer(rangeRingFeatures, rangeRingStyle, "RANGE RINGS");
			map.addLayer(2*NUM_LAYERS+1, mlNexradRangeRings);
			//((StyledMapRenderer)mapPane.getRenderer()).insertLayer(mlNexradRangeRings, 100);

			// Add marker layer
			try {
				mlMarkers = new DefaultMapLayer(markerFeatures, MarkerEditor.getDefaultStyle(), "MARKER EDITOR");
				map.addLayer(2*NUM_LAYERS+2, mlMarkers);
				//((StyledMapRenderer)mapPane.getRenderer()).insertLayer(mlMarkers, 101);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				splashWindow.setStatus("Finishing Map", 95);
				wctMapPane.setMapContext(map);
				//((StyledMapRenderer)mapPane.getRenderer()).setMapContext(map);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Create special listener and add to mapPane
			zoomChangeListener = new WCTMapPaneZoomChange(this);
			wctMapPane.addZoomChangeListener(zoomChangeListener); 
			//          nxZoomChange.setNexradFilterGUI(nxfilterGUI);


			try {
//				URL imageURL = WCTViewer.class.getResource("/images/noaalogont-20tr.gif");
				URL imageURL = WCTViewer.class.getResource("/images/noaa_logo_50x50.gif");
				if (imageURL != null) {
					this.logo = new ImageIcon(imageURL).getImage();
				}
				else {
					System.err.println("Logo image not found");
				}
				renderedLogo = new RenderedLogo(logo);
				renderedLogo.setAlpha(0.7f);
				//mapPane.getRenderer().addLayer(renderedLogo);
				renderedLogo.setZOrder(500.0f);
			} catch (Exception e) {
				e.printStackTrace();
			}

			wctMapPane.setPaintingWhileAdjusting(false);


			// PRINT LAYER INFO
			System.out.println("============================================");
			System.out.println("============================================");
			RenderedLayer[] layers = ((StyledMapRenderer)wctMapPane.getRenderer()).getLayers();
			for (int n=0; n<layers.length; n++) {
				System.out.println("LAYER "+n+":  "+layers[n].getName(layers[n].getLocale()));
			}
			System.out.println("============================================");
			System.out.println("============================================");


		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE ADDING MAP DATA");
			e.printStackTrace();
		}

		// Set some layers visibility to false
		try {
			((MapLayer) baseMapLayers.elementAt(COUNTIES)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(RIVERS)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CITY250)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CITY100)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CITY35)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CITY10)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CITY_SMALL)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(AIRPORTS)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(ASOS_AWOS)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CRN)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(WSR)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(TDWR)).setVisible(false);
		} catch (Exception e) {}
		try {
			((MapLayer) baseMapLayers.elementAt(CLIMATE_DIV)).setVisible(false);
		} catch (Exception e) {}

		// Set all layers label visibility to false
		for (int i = 0; i < NUM_LAYERS; i++) {
			if (baseMapLabelLayers.elementAt(i) != null) {
				((MapLayer) baseMapLabelLayers.elementAt(i)).setVisible(false);
			}
		}

		// Set max and current views to conus (before NEXRAD added)
		//        wctMapPane.setVisibleArea(new java.awt.geom.Rectangle2D.Double(-127.0, 20.0, .0001, .0001));


		wctMapPane.setPreferredArea(new java.awt.geom.Rectangle2D.Double(-127.0, 20.0, 62.0, 39.0));
		//        wctMapPane.setVisibleArea(new java.awt.geom.Rectangle2D.Double(-127.0, 20.0, 62.0, 39.0));
		//        wctMapPane.reset();


		// init this now
		try {
			mapSelect = new MapSelector(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateMemoryLabel();

		//mapPane.getRenderer().setOffscreenBuffered(0.0f, 1.0f, ImageType.VOLATILE);

	}


	// END loadBaseMaps()

	/**
	 *  Loads the data file and removes both graphic (polygon) and
	 *  alphanumeric (point) nexrad data from the view.
	 *  For use with an animation thread!
	 *
	 * @param  dataUrl  Description of the Parameter
	 * @return             Description of the Return Value
	 */
	public boolean loadAnimationFile(URL dataUrl) {
		return (loadFile(dataUrl, true, false, true, false));
	}

	/**
	 *  Loads the data file and removes both graphic (polygon) and
	 *  alphanumeric (point) nexrad data from the view.
	 *  For use with an animation thread!
	 *
	 * @param  dataUrl  URL of NEXRAD File
	 * @param  rangeModel  RangeModel from a Progress Bar
	 * @return             true if successful
	 */
	public boolean loadAnimationFile(URL dataUrl, BoundedRangeModel rangeModel) {
		return (loadFile(dataUrl, true, false, true, false, rangeModel));
	}



	/**
	 *  Loads the data file and removes both graphic (polygon) and
	 *  alphanumeric (point) nexrad data from the view.
	 *
	 * @param  dataUrl  URL of NEXRAD File
	 * @return             Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl) {
		return (loadFile(dataUrl, true, false, false, false));
	}


	/**
	 *  Loads the data file and removes both graphic (polygon) and
	 *  alphanumeric (point) nexrad data from the view.
	 *
	 * @param  dataUrl  URL of NEXRAD File
	 * @param  rangeModel  RangeModel from a Progress Bar
	 * @return             Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, BoundedRangeModel rangeModel) {
		return (loadFile(dataUrl, true, false, false, false, rangeModel));
	}


	/**
	 *  Loads the data file, removes both graphic (polygon) and
	 *  alphanumeric (point) nexrad data from the view(optional) and is for a non-alphanumeric background image.
	 *
	 * @param  clearAlphanumeric  Description of the Parameter
	 * @param  dataUrl         Description of the Parameter
	 * @return                    Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, boolean clearAlphanumeric) {
		return (loadFile(dataUrl, clearAlphanumeric, false, false, false));
	}


	/**
	 *  Loads data file into viewer with all options available
	 *  (for use when it is not executed by an animation thread)
	 *
	 * @param  clearAlphanumeric  Description of the Parameter
	 * @param  isAlphaBackground  Description of the Parameter
	 * @param  dataUrl         Description of the Parameter
	 * @return                    Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, boolean clearAlphanumeric, boolean isAlphaBackground) {
		return (loadFile(dataUrl, clearAlphanumeric, isAlphaBackground, false, false));
	}


	/**
	 *  Loads data file into viewer with all options available
	 *  (for use when it is not executed by an animation thread)
	 *
	 * @param  clearAlphanumeric  Description of the Parameter
	 * @param  isAlphaBackground  Description of the Parameter
	 * @param  isAnimation        Description of the Parameter
	 * @param  dataUrl         Description of the Parameter
	 * @return                    Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, boolean clearAlphanumeric,
			boolean isAlphaBackground, boolean isAnimation) {
		return (loadFile(dataUrl, clearAlphanumeric, isAlphaBackground, isAnimation, false));
	}


	/**
	 *  Loads data file into viewer with all options available
	 *
	 * @param  clearAlphanumeric  Description of the Parameter
	 * @param  isAlphaBackground  Description of the Parameter
	 * @param  isAnimation        Description of the Parameter
	 * @param  resetExtent        Reset the view to the bounds of the nexrad site
	 * @param  dataUrl         Description of the Parameter
	 * @return                    Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, boolean clearAlphanumeric,
			boolean isAlphaBackground, boolean isAnimation, boolean resetExtent) {

		return loadFile(dataUrl, clearAlphanumeric, isAlphaBackground, 
				isAnimation, resetExtent, null);            

	}


	/**
	 *  Loads data file into viewer with all options available
	 *
	 * @param  clearAlphanumeric  Clear the alphanumeric layer? 
	 * @param  isAlphaBackground  Is this a background for an alphanumeric?
	 * @param  isAnimation        Is this an animation process?
	 * @param  resetExtent        Reset the view to the bounds of the nexrad site?
	 * @param  dataUrl         URL of NEXRAD File
	 * @param  rangeModel         BoundedRangeModel from ProgressBar
	 * @return                    Description of the Return Value
	 */
	public boolean loadFile(URL dataUrl, boolean clearAlphanumeric,
			boolean isAlphaBackground, boolean isAnimation, boolean resetExtent,
			BoundedRangeModel rangeModel) {


		System.out.println("loadNexradFile: "+dataUrl);

		this.animationRangeModel = rangeModel;

		CategoryLegendImageProducer radLegendProducerNew = new CategoryLegendImageProducer();
		radLegendProducerNew.setBackgroundColor(this.radLegendProducer.getBackgroundColor());
		radLegendProducerNew.setForegroundColor(this.radLegendProducer.getForegroundColor());
		radLegendProducerNew.setFont(this.radLegendProducer.getFont());
		this.radLegendProducer = radLegendProducerNew;

		if (isAlphaBackground) {
			radLegendProducer.setDataDescription(lastDecodedLegendProducer.getDataDescription());
		}



		if (isLoading && ! isAnimation) {
//			JOptionPane.showMessageDialog(this, "already loading...");
			return false;
		}
		
		
		isLoading = true;
		if (dataSelect == null) {
			dataSelect = new DataSelector(this);
		}

		dataSelect.setIsLoading(true);

		try {
			dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			System.gc();
			System.runFinalization();


			this.dataUrl = dataUrl;

			// Lazy object creation
			if (scannedFile == null) {
				scannedFile = new FileScanner();
			}


			try {  

				statusBar.setProgressText("Downloading File...");
				zoomChangeListener.setRadarActive(false);
				if (radialProps != null) {
					radialProps.setLoadButtonEnabled(false);
				}


				boolean checkForOpendap = 
					dataSelect.getDataSourcePanel().getDataType().equals(WCTDataSourceDB.SINGLE_FILE) || 
					dataSelect.getDataSourcePanel().getDataType().equals(WCTDataSourceDB.THREDDS) ||
					dataSelect.getDataSourcePanel().getDataType().equals(WCTDataSourceDB.FAVORITES);

				if (isAnimation) {
					dataUrl = WCTDataUtils.scan(dataUrl, scannedFile, useWctCache, checkForOpendap, 
							dataSelect.getSelectedDataType(), animationProgressFrame);
				}
				else {
					dataUrl = WCTDataUtils.scan(dataUrl, scannedFile, useWctCache, checkForOpendap, dataSelect.getSelectedDataType(), this);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
            	JOptionPane.showMessageDialog(this, 
            			"--- General Data Load Error ---\n"+
            			"This OPeNDAP URL supplied is not valid - no remote file found.", 
            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
				setIsLoading(false);
				return false;
			}
			
			
			if (WCTUtils.getSharedCancelTask().isCancel()) {
				System.out.println("cancel task is TRUE - returning...");
				WCTUtils.getSharedCancelTask().setCancel(false);
				setIsLoading(false);
				return false;
			}
			
			
			
			// AUTO detection of feature type goes here...
			// TODO
			/// 
			

//			if ( ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN ||
//					scannedFile.getLastScanResult().getDataType() == SupportedDataType.POINT_TIMESERIES ||
//					scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) &&
//					dataSelect.getSelectedDataType() == SupportedDataType.POINT_TIMESERIES) 
//					||
//					(scannedFile.getLastScanResult().getDataType() == SupportedDataType.POINT_TIMESERIES &&
//							dataSelect.getSelectedDataType() == SupportedDataType.UNKNOWN)
//				) {

			
			if ( ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.POINT_TIMESERIES ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) &&
					(dataSelect.getSelectedDataType() == SupportedDataType.POINT_TIMESERIES ||
					dataSelect.getSelectedDataType() == SupportedDataType.UNKNOWN)) 
					||
					(scannedFile.getLastScanResult().getDataType() == SupportedDataType.POINT_TIMESERIES &&
							dataSelect.getSelectedDataType() == SupportedDataType.UNKNOWN)
				) {
			
			
//				javax.swing.JOptionPane.showMessageDialog(this, 
//						"POINT DATA FOUND: "+dataUrl, 
//						"POINT DATA", 
//						javax.swing.JOptionPane.ERROR_MESSAGE);
				
				// just try auto-detection for local unknown or gridded (default for .nc) files
	            
				Formatter fmter = new Formatter();
//				FeatureDatasetPoint fdp = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(null, dataURL.toString(), WCTUtils.getSharedCancelTask(), fmter);
//				if (fdp == null) {
//					System.out.println(fmter.toString());
//					throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
//				}
//				try {
					
//				FeatureDataset fd = FeatureDatasetFactoryManager.open(null, dataUrl.toString(), 
//						WCTUtils.getSharedCancelTask(), fmter);
//				if (fd == null || ! fd.getFeatureType().isPointFeatureType()) {
//					System.out.println(fmter.toString());
//					throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
//				}
//				fd.close();

//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
				
//				TypedDataset tdataset = TypedDatasetFactory.open(null, dataUrl.toString(), WCTUtils.getSharedCancelTask(), new StringBuilder());
//				if (tdataset != null) {
//					System.out.println(tdataset.getClass());
//					tdataset.close();
//				}
	            
//				if (tdataset == null || tdataset.getClass().getName().equals("ucar.nc2.ft.FeatureDatasetPoint") ||
//						tdataset.getClass().getName().equals("ucar.nc2.dt.point.UnidataStationObsDataset")) {
					
				FeatureDataset fd = FeatureDatasetFactoryManager.open(null, dataUrl.toString(), WCTUtils.getSharedCancelTask(), fmter);
				if (fd != null && fd.getFeatureType().isPointFeatureType()) {
				
					PointDataDialog ppd = PointDataDialog.getInstance(this);
					ppd.setVisible(true);
					ppd.process(dataUrl);
								
					setIsLoading(false);
					return true;
				}
			}



			String urlString = dataUrl.toString();
			int index;
			if (urlString.indexOf("/") == -1) {
				// windows format
				index = urlString.lastIndexOf((int) '\\');
			}
			else {
				index = urlString.lastIndexOf((int) '/');
			}
			this.nexradFile = urlString.substring(index + 1, urlString.length());

			System.out.println("DATA URL: " + dataUrl);
			System.out.println("DATA FILE: " + nexradFile);





			//          if (radialDataset == null) {
			//          radialDataset = new DecodeL2Header();
			//          }
			//if (level2ITRHeader == null) {
			//   level2ITRHeader = new DecodeL2ITRHeader();
			//}         
			if (level3Header == null) {
				level3Header = new DecodeL3Header();
			}
			if (xmrgHeader == null) {
				xmrgHeader = new DecodeXMRGHeader();
			}
			if (q2Decoder == null) {
				q2Decoder = new DecodeQ2();
			}
			if (goesAreaRaster == null) {
				goesAreaRaster = new GoesRemappedRaster();
				goesAreaRaster.addDataDecodeListener(this);
			}
			if (gridDatasetRaster == null) {
				gridDatasetRaster = new GridDatasetRemappedRaster();
				gridDatasetRaster.addDataDecodeListener(this);
			}
			if (radialDatasetRaster == null) {
				radialDatasetRaster = new RadialDatasetSweepRemappedRaster();
				radialDatasetRaster.setHeight((int)(wctMapPane.getHeight()));
				radialDatasetRaster.setWidth((int)(wctMapPane.getWidth()));
				radialDatasetRaster.addDataDecodeListener(this);
			}



			//            // set all unknowns to gridded unless otherwise specified in data selector
			//            // - many aggregations and iosp-driven files have wierd or no file extensions.
			//            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {                
			////                scannedFile.getLastScanResult().setDataType(SupportedDataType.GRIDDED);
			//                scannedFile.getLastScanResult().setDataType(dataSelect.getSelectedDataType());
			//                if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {                
			//                    scannedFile.getLastScanResult().setDataType(SupportedDataType.GRIDDED);
			//                }
			//            }



			if (dataSelect.getSelectedDataType() != SupportedDataType.UNKNOWN) {
				scannedFile.getLastScanResult().setDataType(dataSelect.getSelectedDataType());
			}








			legend = keyPanel;








			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
				try {

					if (isVADdisplayed) {
						mapPanel.remove(vadScrollPane);
						mapPanel.add(wctMapPane, "Center");
						mainPanel.repaint();
					}
					isVADdisplayed = false;



					statusBar.setProgressText("Resampling GVAR Area File");
					this.gridSatelliteURL = dataUrl;

					if (gridSatelliteLegend == null) {
						gridSatelliteLegend = createGridSatelliteLegend();
					}

					if (mapSelect == null) {
						mapSelect = new MapSelector(this);
					}
					statusBar.setNexradHeader(null);
					if (radialProps != null) {
						radialProps.setVisible(false);
					}
					if (gridProps != null) {
						gridProps.setVisible(false);
					}

					mapSelect.isolateGridSatellite(true);
					currentDataType = CurrentDataType.SATELLITE;

					if (resetExtent) {
						scanSatellite();
						zoomChangeListener.setGridSatelliteActive(false);
						if (goesAreaRaster.getBounds().getX()+goesAreaRaster.getBounds().getWidth() > 180 ||
								goesAreaRaster.getBounds().isEmpty() || 
								Double.isNaN(goesAreaRaster.getBounds().getX()) || Double.isNaN(goesAreaRaster.getBounds().getY())) {
							setWctViewExtent(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), 
									new Rectangle2D.Double(-180, -90, 360, 180)));
						}
						else {
							setWctViewExtent(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), goesAreaRaster.getBounds()));
						}
					}
					refreshSatellite();
					wctMapPane.setPreferredArea(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), goesAreaRaster.getBounds()));

					//                    wctMapPane.setPreferredArea(new java.awt.geom.Rectangle2D.Double(-135.0, 12.0, 78.0, 55.0));
					//                    if (resetExtent) {
					//                        zoomChangeListener.setSatelliteActive(false);
					//                        setWctViewExtent(GoesRemappedRaster.GOES_DEFAULT_EXTENT);
					//                    }
					//                    refreshSatellite(resetExtent);
					//                    if (resetExtent) {
					//                    	Rectangle2D.Double bounds = GoesRemappedRaster.adjustGeographicBounds(wctMapPane.getSize(), goesAreaRaster.getBounds());
					//                    	zoomChangeListener.setSatelliteActive(false);
					//                    	wctMapPane.setPreferredArea(bounds);
					//                    	setWctViewExtent(bounds);
					//                    }                    
					zoomChangeListener.setGridSatelliteActive(true);

					if (wctFilterGUI != null) {
						wctFilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
					}

					return true;


				} catch (Exception e) {
					e.printStackTrace();
	            	JOptionPane.showMessageDialog(this, 
	            			"--- General Data Load Error ---\n"+
	            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
	            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					statusBar.setProgressText("");
					this.gridSatelliteURL = null;
					setIsLoading(false);
					return false;
				}
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
				try {

					zoomChangeListener.setGridSatelliteActive(false);


					if (isVADdisplayed) {
						mapPanel.remove(vadScrollPane);
						mapPanel.add(wctMapPane, "Center");
						mainPanel.repaint();
					}
					isVADdisplayed = false;

					statusBar.setProgressText("Resampling Gridded Data");
					this.gridSatelliteURL = dataUrl;

					if (gridSatelliteLegend == null) {
						gridSatelliteLegend = createGridSatelliteLegend();
					}

					if (mapSelect == null) {
						mapSelect = new MapSelector(this);
					}
					statusBar.setNexradHeader(null);

					jmiGridProp.setEnabled(true);
					jmiRadialProp.setEnabled(false);
					if (radialProps != null) {
						radialProps.setVisible(false);
					}

					if (gridProps == null) {
						gridProps = new GridDatasetProperties(this);
						gridProps.setLocation(this.getX()+52, this.getY()+35);
						gridProps.addLoadDataListener(this);
						gridProps.setViewer(this);
					}
					if (gridProps.getGridDatasetURL() == null || 
							! gridProps.getGridDatasetURL().equals(gridSatelliteURL)) {
						gridProps.setGridDatasetURL(gridSatelliteURL);
					}
					if (! isAnimation) {
						gridProps.setVisible(true);
					}


					mapSelect.isolateGridSatellite(false);
					currentDataType = CurrentDataType.GRIDDED;

					if (resetExtent) {
						scanGridDataset();
						zoomChangeListener.setGridSatelliteActive(false);
						if (gridDatasetRaster.getBounds().getX()+gridDatasetRaster.getBounds().getWidth() > 180) {
							setWctViewExtent(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), 
									new Rectangle2D.Double(-180, -90, 360, 180)));
						}
						else {
							setWctViewExtent(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), gridDatasetRaster.getBounds()));
						}
					}
					refreshGridDataset();
					wctMapPane.setPreferredArea(WCTUtils.adjustGeographicBounds(wctMapPane.getSize(), gridDatasetRaster.getBounds()));

					zoomChangeListener.setGridSatelliteActive(true);



					SimpleDateFormat scannedFileDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
					scannedFileDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
					try {
						scannedFile.getLastScanResult().setTimestamp(scannedFileDateFormat.format(gridDatasetRaster.getDateInMilliseconds()));
					} catch (Exception e) {
						// then this file doesn't have a date/time available
					}



					keyPanel.validate();
					keyPanel.repaint();




					if (wctFilterGUI != null) {
						wctFilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
					}

					return true;
				} catch (DAP2Exception e) {
					e.printStackTrace();
	            	JOptionPane.showMessageDialog(this, 
	            			"--- General Data Load Error ---\n"+
	            			"This OPeNDAP URL supplied is not valid. \n("+
	            			e.getMessage()+")", 
	            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					setIsLoading(false);
					return false;				
				} catch (IOException e) {
					e.printStackTrace();
					if (e.getMessage().startsWith("Can't open GRID at")) {
		            	JOptionPane.showMessageDialog(this, 
		            			"--- General Data Load Error ---\n"+
		            			"This file was scanned as a 'Gridded' file, but no grids were found.\n" +
		            			"Please select the appropriate data type.  If the data is 'Gridded'\n" +
		            			"check the CF-Conventions for the proper data structure or verify\n" +
		            			"that the file type is readable by the Weather and Climate Toolkit.", 
		            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					}
					else if (e.getMessage().endsWith("not a valid CDM file.")) {
		            	JOptionPane.showMessageDialog(this, 
		            			"--- General Data Load Error ---\n"+
		            			"This file format is not readable by the Weather and Climate Toolkit.\n"+
		            			"For a list of supported file formats, please refer to \n" +
		            			"http://www.unidata.ucar.edu/software/netcdf-java/formats/FileTypes.html", 
		            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					}
					else if (e.getMessage().startsWith("Operation canceled")) {
						;
					}
					else if (e.getMessage().startsWith("No Grids found in file")) {
						throw new WCTNoGridsFoundException(e.getMessage());
					}
					else {
		            	JOptionPane.showMessageDialog(this, 
		            			"--- General Data Load Error ---\n"+e.getMessage()+"\n"+
		            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
		            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					}
					statusBar.setProgressText("");
					this.gridSatelliteURL = null;
					setIsLoading(false);
					return false;				
				} catch (WCTNoGridsFoundException e) {
					e.printStackTrace();
	            	if (gridProps != null) {
	            		gridProps.setVisible(false);
	            	}
	            	JOptionPane.showMessageDialog(this, 
	            			"--- General Data Load Error ---\n"+
	            			"This file was scanned as a 'Gridded' file, but no grids were found.\n" +
	            			"Please select the appropriate data type.  If the data is 'Gridded'\n" +
	            			"check the CF-Conventions for the proper data structure or verify\n" +
	            			"that the file type is readable by the Weather and Climate Toolkit.", 
	            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					statusBar.setProgressText("");
					this.gridSatelliteURL = null;
					setIsLoading(false);
					return false;				
				} catch (Exception e) {
					e.printStackTrace();
	            	JOptionPane.showMessageDialog(this, 
	            			"--- General Data Load Error ---\n"+
	            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
	            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
					statusBar.setProgressText("");
					this.gridSatelliteURL = null;
					setIsLoading(false);
					return false;				
				}
			}
			else {
				if (mapSelect == null) {
					mapSelect = new MapSelector(this);
				}
				mapSelect.isolateRadar();
				lastDecodedLegendProducer = radLegendProducer;
				currentDataType = CurrentDataType.RADAR;
			}



			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_3D || 
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_2D
			) {



				zoomChangeListener.setRadarActive(false);

				statusBar.setProgressText("Rendering NetCDF");


				q2Decoder.decodeData(dataUrl);
				q2Props.refreshVariableList(q2Decoder);
				q2Props.refreshHeightList(q2Decoder);
				//q2Props.pack();


				if (! q2Props.isVisible()) {
					q2Props.setVisible(true);
					q2Props.setLocation(this.getX()+25, this.getY()+25);
					q2Props.setExtendedState(Frame.NORMAL);

				}

				radarGC = q2Decoder.getGridCoverage(q2Props.getSelectedVariableName(), q2Props.getSelected3DHeightIndex(), radarAlphaChannelValue);
				radarRGC.setGridCoverage(radarGC);
				radarRGC.setVisible(true);
				fireRenderCompleteEvent();



				radLegendProducer.setDataType("NOAA/NSSL NMQ");
				radLegendProducer.setDataDescription(new String[] {"NATIONAL MOSAIC", "REFLECTIVITY"});
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				radLegendProducer.setDateTimeInfo(dateFormat.format(q2Decoder.getCreationDate())+" GMT");

				radLegendProducer.setSpecialMetadata(new String[] {
						"Variable: "+q2Decoder.getLastDecodedVariableName(),
						"Height Level: "+((q2Decoder.getLastDecodedHeightLevel() < 0) ? "N/A" : 
							String.valueOf(q2Decoder.getLastDecodedHeightLevel())),
							"Height: "+((q2Decoder.getLastDecodedHeightValue() < 0) ? "N/A" : 
								fmt2.format(q2Decoder.getLastDecodedHeightValue())+" "+q2Decoder.getLastDecodedHeightUnits())
				});
				radLegendProducer.setLegendTitle(new String[] {"Legend: "+q2Decoder.getLastDecodedUnits()});


				Color[] catColors = LegendCategoryFactory.getCategoryColors(q2Decoder);
				String[] catLabels = LegendCategoryFactory.getCategoryLabels(q2Decoder);
				radLegendProducer.setCategoryColors(catColors);
				radLegendProducer.setCategoryLabels(catLabels);
				radLegendProducer.setInterpolateBetweenCategories(true);
				keyPanel.setLegendImage(radLegendProducer);



				keyPanel.validate();
				keyPanel.repaint();



				if (radialProps != null) {
					radialProps.setVisible(false);
				}
				if (gridProps != null) {
					gridProps.setVisible(false);
				}
				if (wctFilterGUI != null) {
					wctFilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
				}

				statusBar.setProgressText("");
				return true;

			}









			// Get header
			System.out.println("---------------- START HEADER DECODE --------------------");
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {


				CancelTask emptyCancelTask = new CancelTask() {
					@Override
					public boolean isCancel() {
						return false;
					}
					@Override
					public void setError(String arg0) {
					}
					@Override
					public void setProgress(String arg0, int arg1) {						
					}
				};
				try {
					
					
					System.out.println(RandomAccessFile.getOpenFiles());

					radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
							ucar.nc2.constants.FeatureType.RADIAL, 
							dataUrl.toString(), emptyCancelTask, new StringBuilder());

					System.out.println(RandomAccessFile.getOpenFiles());


				} catch (Exception e) {
					e.printStackTrace();
					if (radialDataset != null) {
						try {
							radialDataset.close();
						} catch (Exception e2) {
						}
					}
					throw new DecodeException("DECODING ERROR: "+e.getMessage(), dataUrl);
				}

				if (radialDataset == null || radialDataset.getDataVariables().size() == 0) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					NCdump.print(NetcdfFile.open(dataUrl.toString()), "", baos, null);
					logger.severe(baos.toString());

					throw new DecodeException("Could not create RadialDatasetSweep datatype.\n" +
							"If this file is a supported format, the file may be corrupt.", dataUrl);
				}


				if (radialProps != null ) {
					radialProps.getRadialPropsPanel().setRadialDatasetSweep(radialDataset);
					radialProps.pack();
				}
				

				// make a custom header object that responds to different variables
				if (radialDatasetHeader == null) {
					radialDatasetHeader = new DecodeRadialDatasetSweepHeader() {
						public short getProductCode() {
							String variableName = radialProps == null ? radialDataset.getDataVariables().get(0).toString() : 
								radialProps.getRadialPropsPanel().getVariableName();
							if (variableName.equalsIgnoreCase("Reflectivity") || 
									variableName.equalsIgnoreCase("TotalReflectivityDZ") ||
									variableName.equalsIgnoreCase("Total_Power") ||
									variableName.equalsIgnoreCase("TotalPower")) {
								return NexradHeader.LEVEL2_REFLECTIVITY;
							}
							else if (variableName.equalsIgnoreCase("RadialVelocity") || 
									variableName.equalsIgnoreCase("Velocity") ||
									variableName.equalsIgnoreCase("RadialVelocityVR")) {
								
								return NexradHeader.LEVEL2_VELOCITY;
							}
							else if (variableName.equalsIgnoreCase("SpectrumWidth") || 
									variableName.equalsIgnoreCase("Width") ||
									variableName.equalsIgnoreCase("SpectrumWidthSW")) {
								return NexradHeader.LEVEL2_SPECTRUMWIDTH;
							}
							else if (variableName.equalsIgnoreCase("DifferentialReflectivity")) {
								return NexradHeader.LEVEL2_DIFFERENTIALREFLECTIVITY;
							}
							else if (variableName.equalsIgnoreCase("CorrelationCoefficient")) {
								return NexradHeader.LEVEL2_CORRELATIONCOEFFICIENT;
							}
							else if (variableName.equalsIgnoreCase("DifferentialPhase")) {
								return NexradHeader.LEVEL2_DIFFERENTIALPHASE;
							}
							else {
								return NexradHeader.UNKNOWN;
							}
						}

					};
				}
				radialDatasetHeader.setRadialDatasetSweep(radialDataset);

				// if the file does not have lat/lon/site info encoded inside, set it here based on lookup table
				if (radialDatasetHeader.getICAO().equals("XXXX")) {
					int idx = urlString.lastIndexOf('/');
					String icao = urlString.substring(idx+1, idx+5);
					if (icao.equals("6500")) {
						icao = urlString.substring(idx+5, idx+9); 
					}

					System.err.println("SETTING SITE MANUALLY FOR: "+icao);

					RadarHashtables nxhash = RadarHashtables.getSharedInstance();
					// only reset station info if we found something from lookup.  Some data may not have ICAO, but have lat/lon.
					if (nxhash.getLat(icao) != -999) {
						radialDatasetHeader.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
					}
				}



			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
				level3Header.decodeHeader(dataUrl);
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
				JOptionPane.showMessageDialog(this, "This data type is not supported with the Weather and Climate Toolkit.",
						"NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
				setIsLoading(false);
				return false;
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
				xmrgHeader.decodeHeader(dataUrl);
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_3D) {
				System.out.println("FOUND NEXRAD_Q2_3D #1");
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
				System.out.println("FOUND GENERIC GRIDDED");
			}
			else {
				System.out.println("DEBUG: NexradFile fileType="+scannedFile.getLastScanResult().getDataType());
				JOptionPane.showMessageDialog(this, "This is not a supported file format.\n" +
						"Currently supported are:\n" +
						"  - CF-Gridded NetCDF, GRIB, GEMPAK, GINI and HDF files\n" +
						"  - CF Irregularly Gridded / Swath / Curvilinear NetCDF and HDF files\n" +
						"  - GOES Satellite AREA files\n" +
						"  - NEXRAD Level-II, Level-III or XMRG Data\n",
						"DATA LOADING ERROR", JOptionPane.ERROR_MESSAGE);

				setIsLoading(false);
				return false;
			}
			System.out.println("---------------- FINISH HEADER DECODE -------------------");




			//NexradHeader header; 
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
				header = radialDatasetHeader;
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {

				header = level3Header;
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
				header = xmrgHeader;
			}
			else {
				setIsLoading(false);
				return false;
			}



			// -------- DO SPECIAL PRODUCTS WHICH DON'T GET DISPLAYED IN VIEWER'S MAIN WINDOW ---
			if (header.getProductType() == NexradHeader.L3GSM) {

				String gsmMessage = level3Header.getGsmDisplayString(scannedFile.getLastScanResult().getFileName());
				WCTTextDialog gsmFrame = new WCTTextDialog(this, gsmMessage, "GENERAL STATUS MESSAGE");
				gsmFrame.pack();
				gsmFrame.setVisible(true);

				setIsLoading(false);
				return true;

			}

			if (header.getProductType() == NexradHeader.L3RSL) {
				System.out.println("DECODING RSL");

				DecodeRSL decoder = new DecodeRSL(level3Header);
				RSLDisplayDialog rslFrame = new RSLDisplayDialog(this, decoder);
				rslFrame.setVisible(true);

				setIsLoading(false);
				return true;
			}
			// -------- END: DO SPECIAL PRODUCTS WHICH DON'T GET DISPLAYED IN VIEWER'S MAIN WINDOW ---








			nexradBounds = header.getNexradBounds();
			if (Double.isNaN(nexradBounds.getWidth())) {
				NewRadarSiteDialog nsd = new NewRadarSiteDialog(this);
				nsd.pack();
				nsd.setLocationRelativeTo(this);
				nsd.setVisible(true);
				
				if (nsd.isCancelled()) {
					setIsLoading(false);
					return false;
				}
				
//				System.out.println(newSiteDialog.getId()+","+newSiteDialog.getLat()+","+newSiteDialog.getLon());

				if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
					radialDatasetHeader.setStationInfo(nsd.getId(), nsd.getLat(), 
							nsd.getLon(), nsd.getElevInFeet());
					header = radialDatasetHeader;
				}
				else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
						scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {

					level3Header.decodeHeader(dataUrl);
					header = level3Header;
				}
				
			}
			
			
			

			// set NexradHeader info in StatusBar
			statusBar.setNexradHeader(header);

			nexradBounds = header.getNexradBounds();
			wctMapPane.setPreferredArea(nexradBounds);



			//          TODO
			//CHANGE TO IMPLEMENT A LISTENER INTERFACE FOR siteChanged(NexradEvent e)         
			// Update range rings
			if (rangeRings != null && ! lastRadarID.equals(header.getICAO())) {
				rangeRings.loadRangeRings();
			}
			lastRadarID = header.getICAO();


			if (header.getProductType() != NexradHeader.L3VAD) {
				if (isVADdisplayed) {
					//                    mainPanel.remove(vadScrollPane);
					//                    mainPanel.add(mapScrollPane, "Center");
					//                    mainPanel.repaint();
					mapPanel.remove(vadScrollPane);
					mapPanel.add(wctMapPane, "Center");
					mainPanel.repaint();
				}
				isVADdisplayed = false;
			}               







//			System.out.println("resetExtent: "+resetExtent+" viewType: "+currentViewType);

			if (resetExtent) {

				setWctViewExtent(nexradBounds);                

				// Manually refresh WMS
				try {
					if (mapSelect != null) {
						mapSelect.getWMSPanel().refreshWMS();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}




			//================================================================================

			metaPanel.setSize(200, metaPanel.getHeight());
			metaPanel.validate();
			infoPanel.validate();
			mainPanel.validate();
			validate();
			repaint();


			//================================================================================
			System.out.println("---------------- START PRODUCT DECODE --------------------");

			// Check for unsupported level-3 product and return if unknown
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {

				if (level3Header.getProductType() == NexradHeader.UNKNOWN) {
					JOptionPane.showMessageDialog(this, "This product (code=" + level3Header.getProductCode() + ") is not yet supported!",
							"NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);

					setIsLoading(false);

					return false;
				}

				// Allow everything to finish repainting
				if (isAnimation) {
					try {                     
						Thread.sleep(200);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}





				if (level3Header.getProductType() == NexradHeader.L3VAD) {
					System.out.println("DECODING VAD");

					if (getCurrentViewType() != CurrentViewType.GEOTOOLS) {
						throw new DecodeException("The 'standard' map view must be used for this dataset.");
					}

					if (vadPanel == null) {
						vadPanel = new NexradVADPanel();
						vadPanel.setBackground(Color.white);
						JPanel junkPanel = new JPanel();
						junkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
						junkPanel.add(vadPanel);
						vadScrollPane = new JScrollPane(junkPanel);
					}
					vadPanel.setNexradHeader(level3Header);
					if (!isVADdisplayed) {
						mapPanel.remove(wctMapPane);
						mapPanel.add(vadScrollPane, "Center");
						vadPanel.repaint();
						mapPanel.repaint();

					}

					NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, level3Header, nexradFile,
							NexradLegendLabelFactory.DMS);
					NexradLegendLabelFactory.setSpecialLevel3LegendLabels(radLegendProducer, level3Header, isAlphaBackground);
					//                  keyPanel.setNexradHeader(level3Header, radLegendProducer);
					radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(header, true));
					radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(header, true));
					if (radLegendProducer.isDrawColorMap()) {
						radLegendProducer.setLegendTitle(new String[] {LegendCategoryFactory.getLegendTitle(header, true)});
					}                    
					keyPanel.setLegendImage(radLegendProducer);

					keyPanel.repaint();



					isVADdisplayed = true;
					setIsLoading(false);

					zoomChangeListener.setRadarActive(true);
					if (dataSelect != null) {
						dataSelect.setIsLoading(false);
					}
					if (radialProps != null) {
						radialProps.setLoadButtonEnabled(true);
					}




					StreamingRadialDecoder vadDecoder = new DecodeVADText(level3Header);
					// refresh supplemental data
					suppleData.setTextArray(vadDecoder.getSupplementalDataArray());


					return true;
				}

				// CHECK FOR ALPHANUMERIC PRODUCTS (HAIL, MESO, TVS, STORM STRUCTURE)
				if (level3Header.getProductType() == NexradHeader.L3ALPHA) {
					if (getCurrentViewType() != CurrentViewType.GEOTOOLS) {
						throw new DecodeException("The 'standard' map view must be used for this dataset.");
					}

					int pcode = level3Header.getProductCode();
					if (pcode == 58) {
						alpha_decoder = new DecodeStormTracking(level3Header);
					}
					else if (pcode == 59) {
						alpha_decoder = new DecodeHail(level3Header);
					}
					else if (pcode == 60) {
						alpha_decoder = new DecodeMeso(level3Header);
					}
					else if (pcode == 61) {
						alpha_decoder = new DecodeTVS(level3Header);
					}
					else if (pcode == 62) {
						alpha_decoder = new DecodeStormStructure(level3Header);
					}
					else if (pcode == 141) {
						alpha_decoder = new DecodeMDA(level3Header);
					}
					else {
						throw new Exception("ALPHANUMERIC PRODUCT CODE "+pcode+" IS NOT SUPPORTED");
					}

					// refresh supplemental data
					suppleData.setTextArray(alpha_decoder.getSupplementalDataArray());

					// refresh legend
//					NexradLegendLabelFactory.setAlphaLegendLabels(radLegendProducer, level3Header, alpha_decoder);


					//                    clearData();
					radarRGC.setVisible(false);
					gridSatelliteRGC.setVisible(false);
					radarGC = null;
					radarRGC.setGridCoverage(
							new GridCoverage("NEXRAD_RASTER",
									RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
									new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
							)
					);
					gridSatelliteGC = null;
					gridSatelliteRGC.setGridCoverage(
							new GridCoverage("SATELLITE_RASTER",
									RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
									new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
							)
					);
					nexradFeatures.clear();
					map.removeLayer(mlNexradAlpha);
					map.removeLayer(mlNexradAlphaLabel);
					map.removeLayer(mlNexradAlphaTrack);

					boolean finished = decodeAlphaNumeric();

					radLegendProducer.setSupplementalCategoryLabels(new String[] {" 1 ", "2"});
					
					radLegendProducer.setDrawColorMap(false);
					keyPanel.setLegendImage(radLegendProducer);                    
					keyPanel.repaint();

					//                  ((NexradLegendPanel) keyPanel).setNexradHeader(level3Header, radLegendProducer);            



					wctMapPane.setPreferredArea(alpha_decoder.getNexradExtent());
					if (resetExtent) {
						wctMapPane.setVisibleArea(alpha_decoder.getNexradExtent());
					}

					if (alphaProperties != null) {
						alphaProperties.refreshMatchingFileList();
					}
					keyPanel.setIsUsingHeader(false);
					keyPanel.validate();
					keyPanel.repaint();


					setIsLoading(false);
					return (finished);
				}
				else {
					radLegendProducer.setSpecialMetadata(null);
				}


			}

			//================================================================================



			// Remove the alphanumeric layer the layer loading is not for a background
			if (!isAlphaBackground) {
				map.removeLayer(mlNexradAlpha);
				map.removeLayer(mlNexradAlphaTrack);
				map.removeLayer(mlNexradAlphaLabel);
			}


			//----------------------------------------------------------------------------------------
			//------ LOAD NEXRAD DATA INTO FEATURE COLLECTION ---------------------
			//----------------------------------------------------------------------------------------

			System.out.println("DATA FILE TYPE ::: "+scannedFile.getLastScanResult().getDataType());


			java.awt.geom.Rectangle2D.Double currentExtentHold = getCurrentExtent();


			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {

				if (radialDataset.getDataVariables().size() == 0) {
					radialDataset.close();
					throw new WCTException("No data variables found for this Radial file...  " +
							"Possibly a corrupt or incomplete file?\n" +
							"Note that NEXRAD Level-III products are considered a special \n" +
							"data type (not RADIAL) by the Weather and Climate Toolkit.");
				}
				
				radLegendProducer.setSpecialMetadata(null);
				//
				String variableName = ( radialProps == null ) ? radialDataset.getDataVariables().get(0).toString() : radialProps.getRadialPropsPanel().getVariableName();

				// defaults
				boolean classify = false;
				int cut = 0;


				if (animationFilter != null) {
					wctFilter = animationFilter;
					animationFilter = null;
				}
				else if (wctFilterGUI != null && wctFilterGUI.isFilterEngaged()) {
					wctFilter = wctFilterGUI.getRadialFilter(wctFilter);
				}
				else {
					wctFilter = new WCTFilter();
				}


				// Very first time
				if (radialProps == null) {
					classify = false;

					radialProps = new RadialProperties(this, radialDataset);
					radialProps.setLocation(this.getX()+25, this.getY()+25);
					radialProps.setLoadButtonEnabled(false);
					radialProps.addLoadDataListener(this);

				} 
				else {

					// Classify the Level-II Data
					classify = radialProps.getClassify();
					String varName = radialProps.getRadialPropsPanel().getVariableName();
					cut = radialProps.getRadialPropsPanel().getCut();
					boolean useRFvalues = radialProps.getUseRFvalues();



					if (isAnimation) {
						// Always clip for animations regardless of filterGUI
						// If we are smoothing then clip to larger area
						if (radarSmoothFactor > 0) {
							SmoothingOperation smop = new SmoothingOperation();
							java.awt.geom.Rectangle2D.Double smoothingExtent = 
								smop.adjustSmoothingExtent(currentExtentHold, wctMapPane.getWidth(), wctMapPane.getHeight(), (int)radarSmoothFactor);
							setCurrentExtent(smoothingExtent);

							wctFilter.setExtentFilter(smoothingExtent);
						}
						else {
							wctFilter.setExtentFilter(this.getCurrentExtent());
						}
					}


					if (wctFilterGUI != null) {
						wctFilterGUI.setSelectedTab(WCTFilterGUI.LEVEL2_TAB);
					}
				}


				radialProps.setVisible(! isAnimation);
				if (gridProps != null) {
					gridProps.setVisible(false);
				}

				// set statusBar elevation
				this.lastDecodedElevationAngle = radialDatasetRaster.getLastDecodedElevationAngle();
				statusBar.setNexradElevationAngle(radialDatasetRaster.getLastDecodedElevationAngle());

				jmiRadialProp.setEnabled(true);
				jmiGridProp.setEnabled(false);
				if (gridProps != null) {
					gridProps.setVisible(false);
				}

				// Update legend with standard information
				NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, radialDatasetHeader, nexradFile,
						NexradLegendLabelFactory.DMS);

				// refresh supplemental data
				suppleData.setTextArray(null);

			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {

				System.out.println("DECODING ::: "+dataUrl);

				if (level3Decoder == null) {
					level3Decoder = new DecodeL3Nexrad(level3Header, nexradFeatures);
					level3Decoder.addDataDecodeListener(this);
				}
				jmiRadialProp.setEnabled(false);
				jmiGridProp.setEnabled(false);
				if (radialProps != null) {
					radialProps.setVisible(false);
				}
				if (gridProps != null) {
					gridProps.setVisible(false);
				}

				// Update legend with standard information
				NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, level3Header, nexradFile,
						NexradLegendLabelFactory.DMS);
				
				
				double[] holdMinValues = null;
				double[] holdMaxValues = null;
				

				if (isAnimation) {
					if (wctFilterGUI != null && wctFilterGUI.isFilterEngaged()) {
						wctFilter = wctFilterGUI.getLevel3Filter(wctFilter);
						wctFilter.setValueIndices(level3Header.getDataThresholdStringArray());
					}
					else {
						if (wctFilter == null) {
							wctFilter = new WCTFilter();
						}
					}
					// Always clip for animations regardless of filterGUI
					// If we are smoothing then clip to larger area
					if (radarSmoothFactor > 0) {
						SmoothingOperation smop = new SmoothingOperation();
						java.awt.geom.Rectangle2D.Double smoothingExtent = 
							smop.adjustSmoothingExtent(currentExtentHold, wctMapPane.getWidth(), wctMapPane.getHeight(), (int)radarSmoothFactor);
						setCurrentExtent(smoothingExtent);

						wctFilter.setExtentFilter(smoothingExtent);
						
						holdMinValues = Arrays.copyOf(wctFilter.getMinValue(), wctFilter.getMinValue().length);
						holdMaxValues = Arrays.copyOf(wctFilter.getMaxValue(), wctFilter.getMaxValue().length);	
						wctFilter.setMinValue(WCTFilter.NO_MIN_VALUE);
						wctFilter.setMaxValue(WCTFilter.NO_MAX_VALUE);
						
					}
					else {
						wctFilter.setExtentFilter(this.getCurrentExtent());
					}
					level3Decoder.setDecodeHint("nexradFilter", wctFilter);
					level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
				}

				else if (wctFilterGUI == null || ! wctFilterGUI.isFilterEngaged()) {

					level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
				}
				else {
					// Disable distance filtering for non-radial Level-3 products
					wctFilterGUI.getLevel3AttributeFilterPanel().setLevel3DistanceFilterEnabled(level3Header.getProductType() == NexradHeader.L3RADIAL);
					wctFilter = wctFilterGUI.getLevel3Filter(wctFilter);
					wctFilter.setValueIndices(level3Header.getDataThresholdStringArray());

					if (radarSmoothFactor > 0) {
						holdMinValues = Arrays.copyOf(wctFilter.getMinValue(), wctFilter.getMinValue().length);
						holdMaxValues = Arrays.copyOf(wctFilter.getMaxValue(), wctFilter.getMaxValue().length);	
						wctFilter.setMinValue(WCTFilter.NO_MIN_VALUE);
						wctFilter.setMaxValue(WCTFilter.NO_MAX_VALUE);
					}
					
					level3Decoder.setDecodeHint("nexradFilter", wctFilter);
					level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));

				} 

				level3Decoder.decodeData();


				System.out.println("NUM OF FEATURES: "+level3Decoder.getFeatures().size());



				if (radarSmoothFactor > 0) {
					wctFilter.setMinValue(holdMinValues);
					wctFilter.setMaxValue(holdMaxValues);
				}

				// refresh supplemental data
				try {
					suppleData.setTextArray(level3Decoder.getSupplementalDataArray());
				} catch (Exception e) {
					suppleData.setTextArray(new String[]{"ERROR DECODING SUPPLEMENTAL DATA"});
				}


				// set up status bar info 
				double elevAngle = NexradUtilities.getElevationAngle(level3Header);
				if (! Double.isNaN(elevAngle)) {
					statusBar.setNexradElevationAngle(elevAngle);
					this.lastDecodedElevationAngle = elevAngle;
				}
				else {
					this.lastDecodedElevationAngle = Double.NaN;
					statusBar.setNexradElevationAngle(WCTStatusBar.NEXRAD_ELEVATION_UNDEFINED);
				}






				nexradFeatures = level3Decoder.getFeatures();
				nexradSchema = level3Decoder.getFeatureTypes()[0];

				if (wctFilterGUI != null) {
					wctFilterGUI.setSelectedTab(WCTFilterGUI.LEVEL3_TAB);
				}
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {

				this.lastDecodedElevationAngle = Double.NaN;
				statusBar.setNexradElevationAngle(WCTStatusBar.NEXRAD_SITE_UNDEFINED);
				if (xmrgDecoder == null) {
					xmrgDecoder = new DecodeXMRGData(xmrgHeader, nexradFeatures);
					xmrgDecoder.addDataDecodeListener(this);
				}
				jmiRadialProp.setEnabled(false);
				jmiGridProp.setEnabled(false);
				if (radialProps != null) {
					radialProps.setVisible(false);
				}
				if (gridProps != null) {
					gridProps.setVisible(false);
				}

				// Update legend with standard information
				NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, xmrgHeader, nexradFile,
						NexradLegendLabelFactory.DMS);
				NexradLegendLabelFactory.setSpecialXMRGLegendLabels(radLegendProducer, xmrgHeader, xmrgDecoder);
				//              ((NexradLegendPanel) keyPanel).setClassify(true);


				if (isAnimation) {
					if (wctFilterGUI != null && wctFilterGUI.isFilterEngaged()) {
						wctFilter = wctFilterGUI.getGridFilter(wctFilter);
					}
					else {
						if (wctFilter == null) {
							wctFilter = new WCTFilter();
						}
					}
					// Always clip for animations regardless of filterGUI
					// If we are smoothing then clip to larger area
					if (radarSmoothFactor > 0) {
						//                        java.awt.geom.Rectangle2D.Double currentExtent = getCurrentExtent();
						//                    	double xShift = (currentExtent.width - currentExtent.width/radarSmoothExtentEnlargementFactor);
						//                    	double yShift = (currentExtent.height - currentExtent.height/radarSmoothExtentEnlargementFactor);
						//                        currentExtent = new java.awt.geom.Rectangle2D.Double(
						//                                currentExtent.x - xShift,
						//                                currentExtent.y - yShift,
						//                                currentExtent.width + 2*xShift,
						//                                currentExtent.height + 2*yShift);
						SmoothingOperation smop = new SmoothingOperation();
						java.awt.geom.Rectangle2D.Double smoothingExtent = 
							smop.adjustSmoothingExtent(currentExtentHold, wctMapPane.getWidth(), wctMapPane.getHeight(), (int)radarSmoothFactor);
						setCurrentExtent(smoothingExtent);

						wctFilter.setExtentFilter(smoothingExtent);
					}
					else {
						wctFilter.setExtentFilter(this.getCurrentExtent());
					}
					xmrgDecoder.setDecodeHint("nexradFilter", wctFilter);
					xmrgDecoder.decodeData();
				}
				else if (wctFilterGUI == null || ! wctFilterGUI.isFilterEngaged()) {
					xmrgDecoder.decodeData();
				}
				else {
					xmrgDecoder.setDecodeHint("nexradFilter", wctFilterGUI.getGridFilter(wctFilter));
					xmrgDecoder.decodeData();
				} 

				//xmrgDecoder.decodeData();



				nexradFeatures = xmrgDecoder.getFeatures();
				nexradSchema = xmrgDecoder.getFeatureTypes()[0];

				// refresh supplemental data
				suppleData.setTextArray(null);

				if (wctFilterGUI != null) {
					wctFilterGUI.setSelectedTab(WCTFilterGUI.GRID_TAB);
				}
			}
			else {

				this.lastDecodedElevationAngle = Double.NaN;
				statusBar.setNexradElevationAngle(WCTStatusBar.NEXRAD_SITE_UNDEFINED);
//				JOptionPane.showMessageDialog(this, "General Decode Error 1B",
//						"DATA LOADING ERROR", JOptionPane.ERROR_MESSAGE);
            	JOptionPane.showMessageDialog(this, 
            			"--- General Data Load Error ---\n"+
            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
            			"Data Load Error", JOptionPane.ERROR_MESSAGE);

				setIsLoading(false);
				return false;
			}





















			// create new rasterizer each time to avoid possible bug with repeated equal cell size calculations
			if (rasterizer != null) {
				rasterizer.removeGeneralProgressListener(this);
			}
			rasterizer = null;
			rasterizer = new WCTRasterizer((int)(wctMapPane.getHeight()*RASTER_SIZE_FACTOR), (int)(wctMapPane.getWidth()*RASTER_SIZE_FACTOR));
			rasterizer.setNoDataValue(Double.NaN);
			rasterizer.addGeneralProgressListener(this);


			// set up rasterizer metadata         
			rasterizer.setLongName(NexradUtilities.getLongName(header));
			rasterizer.setUnits(NexradUtilities.getUnits(header));
			rasterizer.setDateInMilliseconds(header.getMilliseconds());



			System.out.println("---------------- FINISH PRODUCT DECODE --------------------");


			// Update keypanel
			keyPanel.repaint();












			radLegendProducer.setDrawColorMap(true);
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
				NexradLegendLabelFactory.setSpecialLevel2LegendLabels(radLegendProducer, radialDatasetHeader, radialDatasetRaster);
				//              ((NexradLegendPanel) keyPanel).setNexradHeader(radialDatasetHeader, radLegendProducer);

				boolean classify = (radialProps == null) ? false : radialProps.getClassify();
				
				
				SampleDimensionAndLabels sd = 
					NexradSampleDimensionFactory.getSampleDimensionAndLabels(
							radialDatasetHeader.getProductCode(), false);
				radLegendProducer.setSampleDimensionAndLabels(sd);

				
				radLegendProducer.setInterpolateBetweenCategories(! classify);
				if (sd == null) {
					radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(radialDatasetHeader, classify));
					radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(radialDatasetHeader, classify));
				}
				
				radLegendProducer.setLabelEveryOtherN(1);

				if (radLegendProducer.isDrawColorMap()) {
					radLegendProducer.setLegendTitle(new String[]{ LegendCategoryFactory.getLegendTitle(radialDatasetHeader, classify) });
				}                    
				if (radialDatasetHeader.getProductCode() == NexradHeader.LEVEL2_VELOCITY || 
						radialDatasetHeader.getProductCode() == NexradHeader.LEVEL2_SPECTRUMWIDTH) {

					radLegendProducer.setSupplementalCategoryColors(new Color[] { new Color(119,   0, 125) });
					radLegendProducer.setSupplementalCategoryLabels(new String[] { "RF" });
				}

			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
				NexradLegendLabelFactory.setSpecialXMRGLegendLabels(radLegendProducer, xmrgHeader, xmrgDecoder);

				radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(xmrgHeader, true));
				radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(xmrgHeader, true));
				if (radLegendProducer.isDrawColorMap()) {
					radLegendProducer.setLegendTitle(new String[]{ LegendCategoryFactory.getLegendTitle(xmrgHeader, true) });
				}


			}
			else if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) &&
					level3Header.getProductType() == NexradHeader.L3RADIAL_8BIT) {

				NexradLegendLabelFactory.setSpecialLevel3LegendLabels(radLegendProducer, level3Header, isAlphaBackground);

				
				SampleDimensionAndLabels sd = 
					NexradSampleDimensionFactory.getSampleDimensionAndLabels(
							level3Header.getProductCode(), false);
				radLegendProducer.setSampleDimensionAndLabels(sd);

				
				boolean classify = false;    
//				if (level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION ||
//						level3Header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION) {
//					
//					radLegendProducer.setInterpolateBetweenCategories(false);
//					// do not use colors and labels from sample dimensions
//					sd = null;
//					radLegendProducer.setSampleDimensionAndLabels(null);
//				}
//				else {
//					radLegendProducer.setInterpolateBetweenCategories(! classify);
//				}
				
				
				if (sd == null) {
				radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(level3Header, classify));
				radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(level3Header, classify));
				}
				
				
				if (radLegendProducer.isDrawColorMap()) {
					radLegendProducer.setLegendTitle(new String[]{ LegendCategoryFactory.getLegendTitle(level3Header, classify) });
				}                    

			}
			else {
				NexradLegendLabelFactory.setSpecialLevel3LegendLabels(radLegendProducer, level3Header, isAlphaBackground);

				SampleDimensionAndLabels sd = 
					NexradSampleDimensionFactory.getSampleDimensionAndLabels(
							level3Header.getProductCode(), false);
				radLegendProducer.setSampleDimensionAndLabels(sd);

				
				if (level3Header.getProductType() == NexradHeader.L3RADIAL) {
					
					radLegendProducer.setInterpolateBetweenCategories(false);
					// do not use colors and labels from sample dimensions
					sd = null;
					radLegendProducer.setSampleDimensionAndLabels(null);
				}

				
				
				if (sd == null) {
					radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(level3Header, true));
					radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(level3Header, true));
				}
				if (radLegendProducer.isDrawColorMap()) {
					radLegendProducer.setLegendTitle(new String[] { LegendCategoryFactory.getLegendTitle(level3Header, true) });
				}
			}




			
			
			try {
				if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
					SearchDialog.getInstance(this).setAutoFillDataType(getFileScanner().getLastScanResult().getDataType().toString());
					SearchDialog.getInstance(this).setAutoFillFileFormat(radialDataset.getNetcdfFile().getFileTypeDescription());
					SearchDialog.getInstance(this).setAutoFillVariableUnits(radialDatasetRaster.getUnits());
					SearchDialog.getInstance(this).setAutoFillVariableName(radialDatasetRaster.getVariableName());
					SearchDialog.getInstance(this).setAutoFillVariableDesc(radialDatasetRaster.getLongName());
				} 
				else if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
						scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) 
						&& header.getProductType() == NexradHeader.L3RADIAL_8BIT) {
					
					SearchDialog.getInstance(this).setAutoFillDataType(getFileScanner().getLastScanResult().getDataType().toString());
					SearchDialog.getInstance(this).setAutoFillFileFormat("NIDS NEXRAD Level-III");
					SearchDialog.getInstance(this).setAutoFillVariableUnits(rasterizer.getUnits());
					SearchDialog.getInstance(this).setAutoFillVariableName(rasterizer.getVariableName());
					SearchDialog.getInstance(this).setAutoFillVariableDesc(rasterizer.getLongName());
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			

			if (! isAnimation) {
				refreshRadarData();         
			}
			else {





				System.out.println("---------------- START ANIMATION RESAMPLING --------------------");
				try {


					if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
						boolean classify = (radialProps == null) ? false : radialProps.getClassify();
						String variableName = radialProps == null ? radialDataset.getDataVariables().get(0).toString() : 
							radialProps.getRadialPropsPanel().getVariableName();

						radialDatasetRaster.setVariableName(variableName);
						radialDatasetRaster.setSweepIndex(radialProps.getRadialPropsPanel().getCut());
						radialDatasetRaster.setWctFilter(wctFilter);
						if (Double.isNaN(radialProps.getRadialPropsPanel().getCappiAltitude())) {
							radialDatasetRaster.process(dataUrl.toString(), getCurrentExtent());
						}
						else {
							radialDatasetRaster.processCAPPI(dataUrl.toString(), getCurrentExtent(), 
									new double[] { radialProps.getRadialPropsPanel().getCappiAltitude() },
									radialProps.getRadialPropsPanel().getCurrentCappiType());
						}


						
						
						this.lastDecodedElevationAngle = radialDatasetRaster.getLastDecodedElevationAngle();
						statusBar.setNexradElevationAngle(radialDatasetRaster.getLastDecodedElevationAngle());
						radialDatasetRaster.setSmoothingFactor((int)radarSmoothFactor);
						radarGC = radialDatasetRaster.getGridCoverage(radarAlphaChannelValue);
						//                        radarGC = radialDatasetRaster.getGridCoverage(radarAlphaChannelValue, AlphaInterpolationType.LINEAR_ASCENDING, (int)radarSmoothFactor);
						radarRGC.setGridCoverage(radarGC);

					} 
					else if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
							scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) 
							&& header.getProductType() == NexradHeader.L3RADIAL_8BIT) {

						//System.out.println("RASTERIZING 8-BIT LEVEL-III PRODUCT");                        

						if (isRasterVariableRes) {
							rasterizer.rasterize(nexradFeatures, "value");
						}
						else {
							rasterizer.rasterize(nexradFeatures, nexradBounds, "value");
						}

						gcSupport.setWctFilter(wctFilter);
						radarGC = gcSupport.getGridCoverage(rasterizer, header, radarAlphaChannelValue);
//						radarGC = gcSupport.getGridCoverage(rasterizer, header, false, 
//								NexradColorFactory.getTransparentColors(header.getProductCode(), false, radarAlphaChannelValue));
						radarRGC.setGridCoverage(radarGC);
					}
					else {
						if (isRasterVariableRes) {
							rasterizer.rasterize(nexradFeatures, "colorIndex");
						}
						else {
							rasterizer.rasterize(nexradFeatures, nexradBounds, "colorIndex");
						}

						gcSupport.setWctFilter(wctFilter);
//						radarGC = gcSupport.getGridCoverage(rasterizer, header, radarAlphaChannelValue);
						radarGC = gcSupport.getGridCoverage(rasterizer, header, true, 
								NexradColorFactory.getTransparentColors(header.getProductCode(), true, radarAlphaChannelValue));
						radarRGC.setGridCoverage(radarGC);
					}

					radarRGC.setVisible(true);


				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("---------------- FINISH ANIMATION RESAMPLE --------------------");

//				setIsLoading(false);

				// reset extent
				setCurrentExtent(currentExtentHold);


			}





			metaPanel.validate();
			infoPanel.validate();
			mainPanel.validate();


			viewProperties.setProjectionChangeEnabled(true);
			viewProperties.setDistanceUnitsEnabled(true);

			//nexradBounds = decoder.getNexradExtent();
			System.out.println("BOUNDS======  " + nexradBounds);

			// Get the Range Rings

			// Try to remove alphanumeric properties frame
			try {
				if (alphaProperties != null && clearAlphanumeric) {
					alphaProperties.dispose();
				}
			} catch (Exception e) {}





			if (currentViewType == CurrentViewType.GEOTOOLS || 
					currentViewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {

				keyPanel.setLegendImage(radLegendProducer);
			}


			keyPanel.validate();
			keyPanel.repaint();

			mainPanel.repaint();



		} catch (DecodeException nde) {
			nde.printStackTrace();
//        	JOptionPane.showMessageDialog(this, 
//        			"--- General Data Load Error ---\n"+
//        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
//        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
        	JOptionPane.showMessageDialog(this, 
        			"--- General Data Load Error ---\n"+
        			"This file format is not readable by the Weather and Climate Toolkit.\n"+
        			"For a list of supported file formats, please refer to \n" +
        			"http://www.unidata.ucar.edu/software/netcdf-java/formats/FileTypes.html\n\n" +
        			"Specific Error: "+nde.getMessage(), 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
			statusBar.setProgressText("");
			setIsLoading(false);
		} catch (IOException e) {
			if (e.getMessage().startsWith("Operation canceled")) {
				setIsLoading(false);
			}
			else {
				e.printStackTrace();
				PointDataDialog.getInstance(this).setVisible(false);
	        	JOptionPane.showMessageDialog(this, 
	        			"--- Data Load Error ---\n"+
	        			"This NetCDF file is not displayable by the Weather and Climate Toolkit,"+
	        			"\nwhich requires a structure adhering to the Climate-Forecast (CF) conventions"+
	        			"\nfor gridded (regularly or irregularly spaced) datasets."+
//	        			"Specific Error: "+e.getMessage()+
	        			"\n\nThe Metadata (ncdump) and Data Dump tools (under the more... link) can"+
	        			"\nstill be used to dump the data values and metadata.",
	        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
				setIsLoading(false);
			}
		} catch (WCTException e) {
			e.printStackTrace();
        	JOptionPane.showMessageDialog(this, 
        			"--- General Data Load Error ---\n"+
        			e.getMessage()+"\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
			statusBar.setProgressText("");
			setIsLoading(false);
		} catch (Exception me) {
			me.printStackTrace();
			statusBar.setProgressText("");
        	JOptionPane.showMessageDialog(this, 
        			"--- General Data Load Error ---\n"+
        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
			setIsLoading(false);
			return false;
		} finally {
			
			if (radialDataset != null) {
				try {
					radialDataset.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}
		return true;
	}


	private RenderedLogo createGridSatelliteLegend() {
		gridSatelliteLegend = new RenderedLogo();
		gridSatelliteLegend.setPosition(LegendPosition.SOUTH_EAST);
		gridSatelliteLegend.setZOrder(500.1f);                    
		wctMapPane.getRenderer().addLayer(gridSatelliteLegend);


		//        gridSatelliteLegend.addPropertyChangeListener(new PropertyChangeListener() {
		//            @Override
		//            public void propertyChange(PropertyChangeEvent evt) {
		//                System.out.println("legend event: "+evt.getPropertyName()+" "+evt.getNewValue());
		//            }
		//        });

		return gridSatelliteLegend;
	}
	
	public RenderedLogo getGridSatelliteLegend() {
		return gridSatelliteLegend;
	}


	/**
	 * This sets the extent in any visible viewer (geotools, google earth, etc...)
	 * @param extent
	 */
	public void setWctViewExtent(Rectangle2D.Double extent) {
		wctMapPane.setVisibleArea(extent);
		if (currentViewType == CurrentViewType.GOOGLE_EARTH) {
			geBrowser.flyToExtent(wctMapPane.getVisibleArea());
		}
		else if (currentViewType == CurrentViewType.NCDC_NCS) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Rectangle2D mapExtent = wctMapPane.getVisibleArea();
					webMapBrowser.executeJavascript("map.setExtent(new esri.geometry.Extent("+
							mapExtent.getMinX()+","+mapExtent.getMinY()+","+mapExtent.getMaxX()+","+mapExtent.getMaxY()+"));");
				}
			});
		}        
		else if (currentViewType == CurrentViewType.NCDC_NIDIS) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//                    Rectangle2D mapExtent = mapPane.getVisibleArea();
					//                    webMapBrowser.executeJavascript("NIDIS_Viewer.setExtentFromWCT("+
					//                            mapExtent.getMinX()+","+mapExtent.getMinY()+","+mapExtent.getMaxX()+","+mapExtent.getMaxY()+",4326);");
					flexBrowser.flyToExtent(wctMapPane.getVisibleArea());
				}
			});
		}        
	}


	// END METHOD loadNexradFile

	//================================================================================
	//================================================================================

	public void setIsLoading(boolean isLoading) {

		if (isLoading) {
			WCTUtils.getSharedCancelTask().setCancel(false);
		}
		
		if (! isLoading) {
			statusBar.setProgressText("");
			statusBar.setProgress(0);
		}
		
		if (radialProps != null) {
			radialProps.setLoadButtonEnabled(! isLoading);
		}

		if (isLoading) {
			if (dataSelect != null) {
				dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else {
			if (dataSelect != null) {
				dataSelect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}


		this.isLoading = isLoading;
		zoomChangeListener.setRadarActive(! isLoading);
		if (dataSelect != null) {
			dataSelect.setIsLoading(isLoading);
		}
	}

	/*
   public Style getNexradPolygonStyle(NexradHeader header) {
         // Create Filters and Style for NEXRAD Polygons!
         Color[] color = NexradColorFactory.getColors(header.getProductCode());

         Rule rules[] = new Rule[color.length];
         Style nexradStyle = sb.createStyle();
         try {
            BetweenFilter filters[] = new BetweenFilter[color.length];
            FilterFactory ffi = FilterFactory.createFilterFactory();

            for (int i = 0; i < color.length; i++) {

               filters[i] = ffi.createBetweenFilter();
               PolygonSymbolizer polysymb = sb.createPolygonSymbolizer(color[i], color[i], 1);
               polysymb.getFill().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
               polysymb.getStroke().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
               rules[i] = sb.createRule(polysymb);

               filters[i].addLeftValue(sb.literalExpression(i));
               filters[i].addRightValue(sb.literalExpression(i + 1));
               filters[i].addMiddleValue(ffi.createAttributeExpression(nexradSchema, "colorIndex"));
               rules[i].setFilter(filters[i]);

               nexradStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

         return nexradStyle;      
   }

	 */


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	private boolean decodeAlphaNumeric() {

		try {

			System.out.println("DECODING ALPHA!");

			jmiAlphaProp.setEnabled(true);

			// Set up product unique stuff
			NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, level3Header, nexradFile,
					NexradLegendLabelFactory.DMS);
			alphaSymbol = NexradLegendLabelFactory.setAlphaLegendLabels(radLegendProducer, level3Header, alpha_decoder);

			System.out.println("NUMBER OF ALPHA FEATURES: " + alpha_decoder.getFeatures().size());


			Style alphaTrackStyle = sb.createStyle(sb.createLineSymbolizer(new Color(220, 220, 220), .5));

			map.removeLayer(mlNexradAlphaTrack);
			if (alpha_decoder.getLineFeatures() != null) {
				mlNexradAlphaTrack = new DefaultMapLayer(alpha_decoder.getLineFeatures(), alphaTrackStyle);
				map.addLayer(mlNexradAlphaTrack);
			}

			Style alphaStyle;

			if (alpha_decoder.getLineFeatures() != null) {

				Rule rules[] = new Rule[5];
				alphaStyle = sb.createStyle();
				//Color startColor = new Color(220, 220, 220);
				Color startColor = alphaLineColor;
				try {
					BetweenFilter filters[] = new BetweenFilter[rules.length];
					FilterFactory ffi = FilterFactory.createFilterFactory();

					Color color = new Color(startColor.getRGB());
					// Create standard filter for all products except DPA
					for (int i = 0; i < rules.length; i++) {

						filters[i] = ffi.createBetweenFilter();
						Mark alphaTrackMark = sb.createMark(alpha_decoder.getDefaultSymbol(), color, color, 1);
						Graphic grAlphaTrack = sb.createGraphic(null, alphaTrackMark, null);
						rules[i] = sb.createRule(sb.createPointSymbolizer(grAlphaTrack));

						filters[i].addLeftValue(sb.literalExpression(i * 15));
						filters[i].addRightValue(sb.literalExpression((i + 1) * 15));
						filters[i].addMiddleValue(ffi.createAttributeExpression(alpha_decoder.getFeatureTypes()[0], "time"));
						rules[i].setFilter(filters[i]);

						alphaStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));

						color = color.darker();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			else {
				//Mark alphaMark = sb.createMark(alpha_decoder.getDefaultSymbol(), new Color(220, 220, 220), new Color(220, 220, 220), 1);
				Mark alphaMark = sb.createMark(alpha_decoder.getDefaultSymbol(), alphaLineColor, alphaLineColor, alphaLineWidth);
				Graphic grAlpha = sb.createGraphic(null, alphaMark, null);
				alphaStyle = sb.createStyle(sb.createPointSymbolizer(grAlpha));
				// update Alphanumeric Properties GUI
				if (alphaProperties == null) {
					alphaProperties = new AlphaProperties(this, dataSelect);
				}
				alphaProperties.setSelectedSymbol(alpha_decoder.getDefaultSymbol());
			}

			map.removeLayer(mlNexradAlpha);
			mlNexradAlpha = new DefaultMapLayer(alpha_decoder.getFeatures(), alphaStyle);
			map.addLayer(mlNexradAlpha);

			//org.geotools.styling.Font font = sb.createFont(new Font("Arial", Font.PLAIN, 12));
			//TextSymbolizer tsAlphaLabel = sb.createTextSymbolizer(new Color(220, 220, 220), font, "id");
			//TextSymbolizer tsAlphaLabel = sb.createTextSymbolizer(alphaLineColor, font, "id");
			TextSymbolizer tsAlphaLabel = sb.createTextSymbolizer(alphaLineColor, BaseMapManager.GT_FONT_ARRAY[alphaLineWidth-1], "id");
			tsAlphaLabel.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
			if (alphaHalo) {
				tsAlphaLabel.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			}
			Style alphaLabelStyle = sb.createStyle(tsAlphaLabel);

			map.removeLayer(mlNexradAlphaLabel);
			mlNexradAlphaLabel = new DefaultMapLayer(alpha_decoder.getFeatures(), alphaLabelStyle);
			map.addLayer(mlNexradAlphaLabel);


			//nexradBounds = tNexrad.getBounds();
			//view.setMaximumMapExtent(nexradBounds);

			viewProperties.setProjectionChangeEnabled(true);
			viewProperties.setDistanceUnitsEnabled(true);


			// Repaint the legend panel
			mainPanel.validate();
			infoPanel.validate();
			infoPanel.repaint();
			keyPanel.validate();
			keyPanel.repaint();


			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}




	public void scanGridDataset() throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException, WCTNoGridsFoundException {

		if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
			scanSatellite();
			return;
		}
		
		if (gridSatelliteURL != null) {
			statusBar.setProgressText("Scanning Grid Dataset");
			gridDatasetRaster.scan(this.gridSatelliteURL.toString());
		}
	}

	
	

	public void refreshGridDataset() {

		try {

			if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
				refreshSatellite();
				return;
			}


			gridSatelliteRGC.setVisible(true);

			if (gridSatelliteURL != null && gridSatelliteRGC.isVisible()) {
				statusBar.setProgressText("Resampling Grid Dataset");


				System.out.println("getCurrentExtent: "+getCurrentExtent());

				//                gridDatasetRaster.setGridIndex(gridProps.getSelectedGridIndex());
				//                gridDatasetRaster.setRuntimeIndex(gridProps.getSelectedRuntimeIndex());
				//                gridDatasetRaster.setTimeIndex(gridProps.getSelectedTimeIndex());
				//                gridDatasetRaster.setZIndex(gridProps.getSelectedZIndex());

//				System.out.println("steve--- gridIndex: "+gridDatasetRaster.getGridIndex());
//				System.out.println("steve--- runtimeIndex: "+gridDatasetRaster.getRuntimeIndex());
//				System.out.println("steve--- timeIndex: "+gridDatasetRaster.getTimeIndex());
//				System.out.println("steve--- zIndex: "+gridDatasetRaster.getZIndex());


				gridDatasetRaster.setMinValueFilter(wctFilterGUI.getGridFilter().getMinValue());
				gridDatasetRaster.setMaxValueFilter(wctFilterGUI.getGridFilter().getMaxValue());
				gridDatasetRaster.setForceResample(contourGC != null && contourRGC != null && contourRGC.isVisible());
				gridDatasetRaster.process(this.gridSatelliteURL.toString(), getCurrentExtent());

				//                gridDatasetRaster.process(this.gridDatasetURL.toString(), getCurrentExtent(), 
				//                        this.gridDatasetSmoothFactor);
				//



				String ctName = mapSelect.getGridColorTableName();
				if (ctName.startsWith("Satellite: ") || ctName.startsWith("Radar: ")) {
					ColorsAndValues cav = GridDatasetColorFactory.getColorsAndValues(
							ctName.substring(ctName.indexOf(" ")+1, ctName.length()));
					cav.flip();
					gridDatasetRaster.setAutoMinMaxValues(false);
					gridDatasetRaster.setDisplayColors(cav.getColors());
					gridDatasetRaster.setDisplayMinValue(Math.min(cav.getValues()[0], cav.getValues()[cav.getValues().length-1]));
					gridDatasetRaster.setDisplayMaxValue(Math.max(cav.getValues()[0], cav.getValues()[cav.getValues().length-1]));
				}
				else {
					
					mapSelect.setGridHasValidRangeAttributes(gridDatasetRaster.hasValidRangeAttributes());
					gridDatasetRaster.setAutoMinMaxValues(mapSelect.isGridAutoMinMaxSelected());
					if (mapSelect.isGridValidRangeMinMaxSelected()) {
						if (gridDatasetRaster.hasValidRangeAttributes()) {
							mapSelect.setGridColorTableMinMaxValue(gridDatasetRaster.getValidRangeMinValue(), gridDatasetRaster.getValidRangeMaxValue());
						}	
						else {
							mapSelect.setGridAutoMinMax(true);
							gridDatasetRaster.setAutoMinMaxValues(true);
							mapSelect.setGridColorTableMinMaxValue(gridDatasetRaster.getGridMinValue(), gridDatasetRaster.getGridMaxValue());
						}
					}					
					else if (mapSelect.isGridAutoMinMaxSelected()) {
						mapSelect.setGridColorTableMinMaxValue(gridDatasetRaster.getGridMinValue(), gridDatasetRaster.getGridMaxValue());
					}
					else {
						gridDatasetRaster.setDisplayMinValue(Math.min(mapSelect.getGridColorTableMinValue(), mapSelect.getGridColorTableMaxValue()));
						gridDatasetRaster.setDisplayMaxValue(Math.max(mapSelect.getGridColorTableMinValue(), mapSelect.getGridColorTableMaxValue()));
					}
					gridDatasetRaster.setDisplayColors(GridDatasetColorFactory.getColors(mapSelect.getGridColorTableName(), mapSelect.isFlipGridColorTable()));                    
				}


				if (mapSelect.getGridSatelliteTransparency() != -1) {
					int alpha = (int)(255.0*(1-mapSelect.getGridSatelliteTransparency()/100.0));
					setGridSatelliteGridCoverage(gridDatasetRaster.getGridCoverage(alpha));                    
				}
				else {      
					setGridSatelliteGridCoverage(gridDatasetRaster.getGridCoverage());
				}
				statusBar.setProgressText("");
				updateMemoryLabel();

				isGridSatelliteLegendVisible = ! mapSelect.getGridSatelliteLegendType().trim().equalsIgnoreCase("None");

				// TODO remove this try/catch?
				try {
				refreshGridDatasetLegend();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				
				gridSatelliteLegend.repaint();
				gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);


				try {
					SearchDialog.getInstance(this).setAutoFillDataType(getFileScanner().getLastScanResult().getDataType().toString());
					SearchDialog.getInstance(this).setAutoFillFileFormat(gridDatasetRaster.getLastProcessedFileTypeDescription());
					SearchDialog.getInstance(this).setAutoFillVariableUnits(gridDatasetRaster.getUnits());
					SearchDialog.getInstance(this).setAutoFillVariableName(gridDatasetRaster.getVariableName());
					SearchDialog.getInstance(this).setAutoFillVariableDesc(gridDatasetRaster.getLongName());
				} catch (Exception e) {
					e.printStackTrace();
				}

//				gridProps.setSelectedGridIndex(gridDatasetRaster.getGridIndex());
				

			}
			else {
				//                if (gridDatasetLegend != null) {
				//                    gridDatasetLegend.setVisible(false);                    
				//                }
			}
		} catch (IOException e) {
			if (e.getMessage().startsWith("Operation canceled")) {
				setIsLoading(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
        	JOptionPane.showMessageDialog(this, 
        			"--- General Data Load Error ---\n"+
        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
			statusBar.setProgressText("");
		}
		setIsLoading(false);

	}


	public void refreshGridDatasetLegend() throws Exception {

		gridSatLegendProducer.setDataType("GRIDDED DATA");
		gridSatLegendProducer.setDataDescription(new String[] { scannedFile.getLastScanResult().getFileName() });
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateTimeString = null;
		try {
			if (gridDatasetRaster.getDateInMilliseconds() != Long.MIN_VALUE) {
				dateTimeString = dateFormat.format(gridDatasetRaster.getDateInMilliseconds())+" GMT";
				gridSatLegendProducer.setDateTimeInfo(dateTimeString);
			}
		} catch (Exception e) {
			// no date or time info present for this file
		}
		String varAndUnitsString = gridDatasetRaster.getLastProcessedGridDatatype().getName()+
		" ("+gridDatasetRaster.getLastProcessedGridDatatype().getUnitsString()+")";


//		System.out.println(gridSatLegendProducer.getDateTimeInfo());

		Color[] catColors = LegendCategoryFactory.getCategoryColors(gridDatasetRaster);
		Double[] catValues = LegendCategoryFactory.getCategoryValues(gridDatasetRaster);

//		System.out.println(Arrays.toString(catValues));

		ColorsAndValues cav1 = new ColorsAndValues(catColors, catValues);
		if (cav1.getValues().length > 1 && cav1.getValues()[1] > cav1.getValues()[0]) {
			cav1.flip();
		}
		ColorsAndValues cav = ColorsAndValues.calculateEqualColorsAndValues( cav1, 51);
		String[] catLabels = cav.getLabels(new DecimalFormat("0.00"));

		//        System.out.println( gridDatasetRaster.isEmptyGrid() );
		//        System.out.println( gridDatasetRaster.getGridMinValue() );
		//        System.out.println( gridDatasetRaster.getGridMaxValue() );

		if (gridDatasetRaster.isEmptyGrid()) {
			gridSatLegendProducer.setLabelOverride(" No Data ");
		}
		else {
			gridSatLegendProducer.setLabelOverride("");
		}

		gridSatLegendProducer.setCategoryColors(WCTUtils.flipArray( cav.getColors() ));        
		gridSatLegendProducer.setCategoryLabels(catLabels);
		gridSatLegendProducer.setInterpolateBetweenCategories(true);

		try {

			String heightString = null;
			if (gridDatasetRaster.getLastProcessedHeight() >= 0) {
				heightString = fmt2.format(gridDatasetRaster.getLastProcessedHeight())+" "+ 
				gridDatasetRaster.getLastProcessedGridCoordSystem().getVerticalAxis().getUnitsString();
			}

			String runtimeString = null;
			if (gridDatasetRaster.getLastProcessedRuntime() != null) {
				runtimeString = dateFormat.format(gridDatasetRaster.getLastProcessedRuntime())+" GMT";            
			}

			String[] legendTitleArray = new String[] {
					scannedFile.getLastScanResult().getFileName(),
					((dateTimeString == null) ? "" : dateTimeString)
					+ ((dateTimeString != null && runtimeString != null) ? " | " : "")
					+ ((runtimeString == null) ? "" : "Runtime: "+runtimeString)
					+ (((dateTimeString != null || runtimeString != null) && heightString != null) ? " | " : "")
					+ ((heightString == null) ? "" : heightString),
					varAndUnitsString
			};

			//            System.out.println("legend ------ "+Arrays.toString(legendTitleArray));

			gridSatLegendProducer.setLegendTitle(legendTitleArray);
			gridSatLegendProducer.setDrawColorMap(true);
			gridSatLegendProducer.setDrawLabels(true);
			gridSatLegendProducer.setLabelEveryOtherN(10);

		} catch (Exception e) {
			e.printStackTrace();
			gridSatLegendProducer.setLegendTitle(new String[] { scannedFile.getLastScanResult().getFileName() });
			gridSatLegendProducer.setDrawColorMap(false);
			gridSatLegendProducer.setDrawLabels(false);
		}


		gridSatLegendProducer.setBackgroundColor(new Color(220, 220, 220, 210));
		gridSatLegendProducer.setMapBackgroundColor(this.getBackgroundColor());
		gridSatLegendProducer.setDrawBorder(true);
		gridSatLegendProducer.setInterpolateBetweenCategories(true);
		lastDecodedLegendProducer = gridSatLegendProducer;


		Image image = gridSatLegendProducer.createMediumLegendImage();
		gridSatelliteLegend.setInsets(new Insets(0, 0, 15, image.getWidth(this)));
		gridSatelliteLegend.setImage(image);
		gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);


		gridSatelliteLegend.repaint();
		wctMapPane.repaint();
	}


















	public void scanSatellite() throws DecodeException, WCTException, IOException, AreaFileException, InvalidRangeException, ParseException, WCTNoGridsFoundException {

		if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
			scanGridDataset();
			return;
		}
		if (gridSatelliteURL != null) {
			statusBar.setProgressText("Scanning GOES Dataset");
			goesAreaRaster.scan(this.gridSatelliteURL.toString());
		}
	}

	public void refreshSatellite() {
		refreshSatellite(false);
	}

	public void refreshSatellite(boolean resetExtent) {


		if (scannedFile != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
			refreshGridDataset();
			return;
		}


		try {
			if (gridSatelliteURL != null && gridSatelliteRGC.isVisible()) {
				statusBar.setProgressText("Resampling GVAR Area File");

				goesAreaRaster.setMinValueFilter(wctFilterGUI.getGridFilter().getMinValue()[0]);
				goesAreaRaster.setMaxValueFilter(wctFilterGUI.getGridFilter().getMaxValue()[0]);
				if (resetExtent) {
					goesAreaRaster.process(this.gridSatelliteURL.toString(), this.satelliteSmoothFactor);
				}
				else {
					goesAreaRaster.process(this.gridSatelliteURL.toString(), getCurrentExtent(), 
							this.satelliteSmoothFactor);
				}

				if (mapSelect.getGridSatelliteTransparency() != -1) {
					int alpha = (int)(255.0*(1-mapSelect.getGridSatelliteTransparency()/100.0));
					setGridSatelliteGridCoverage(goesAreaRaster.getGridCoverage(alpha));                    
				}
				else {      
					setGridSatelliteGridCoverage(goesAreaRaster.getGridCoverage());
				}
				statusBar.setProgressText("");

				refreshSatelliteLegend();
				gridSatelliteLegend.repaint();
				gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);



				try {
					SearchDialog.getInstance(this).setAutoFillDataType("NOAA GOES SATELLITE");
					SearchDialog.getInstance(this).setAutoFillFileFormat("McIDAS AREA");
					SearchDialog.getInstance(this).setAutoFillVariableUnits(goesAreaRaster.getUnits());
					SearchDialog.getInstance(this).setAutoFillVariableName(goesAreaRaster.getVariableName());
					if (scannedFile.getLastScanResult().getSourceID() != null && 
							scannedFile.getLastScanResult().getDescription() != null) {
						
						SearchDialog.getInstance(this).setAutoFillVariableDesc(
							scannedFile.getLastScanResult().getSourceID().toUpperCase()+" "+
							scannedFile.getLastScanResult().getDescription());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				if (gridSatelliteLegend != null) {
					gridSatelliteLegend.setVisible(false);                    
					//                  isSatelliteLegendVisible = false;
				}
			}
		} catch (IOException e) {
			if (e.getMessage().startsWith("Operation canceled")) {
				setIsLoading(false);
				return;
			}
			else {
				e.printStackTrace();
	        	JOptionPane.showMessageDialog(this, 
	        			"--- General Data Load Error ---\n"+ e.getMessage()+"\n"+
	        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
	        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
				statusBar.setProgressText("");
			}
		} catch (Exception e) {
			e.printStackTrace();
        	JOptionPane.showMessageDialog(this, 
        			"--- General Data Load Error ---\n"+
        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);
			statusBar.setProgressText("");
		}

		setIsLoading(false);
	}


	public void refreshSatelliteLegend() throws Exception {
		refreshSatelliteLegend(new Date(goesAreaRaster.getDateInMilliseconds()), "");
	}
	
	/**
	 * Use if a custom date is needed for data or grid coverages displayed that are not generated using the loadData method, 
	 * such as Time Morphed results.
	 * @param date
	 * @throws Exception
	 */
	public void refreshSatelliteLegend(Date date, String dateNote) throws Exception {
		Dimension imageDim = new Dimension(240, 83);

		gridSatLegendProducer.setDataType("MCIDAS AREA FILE");
		gridSatLegendProducer.setDataDescription(new String[] {"GOES SATELLITE"});
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//		gridSatLegendProducer.setDateTimeInfo(dateFormat.format(goesAreaRaster.getDateInMilliseconds())+" GMT");
		gridSatLegendProducer.setDateTimeInfo(dateFormat.format(date)+" GMT");

		System.out.println(gridSatLegendProducer.getDateTimeInfo());

		//      Color[] catColors = LegendCategoryFactory.getCategoryColors(goesAreaRaster, 16);
		//      String[] catLabels = LegendCategoryFactory.getCategoryLabels(goesAreaRaster, 16);
		Color[] catColors = LegendCategoryFactory.getCategoryColors(goesAreaRaster);
		String[] catLabels = LegendCategoryFactory.getCategoryLabels(goesAreaRaster);
		gridSatLegendProducer.setCategoryColors(catColors);
		gridSatLegendProducer.setCategoryLabels(catLabels);
		gridSatLegendProducer.setInterpolateBetweenCategories(true);

		try {

			if (goesAreaRaster.getLastBandDecoded() == Band.BAND1) {
				gridSatLegendProducer.setLegendTitle(new String[] {
						scannedFile.getLastScanResult().getSourceID().toUpperCase()+" "+
						scannedFile.getLastScanResult().getDescription(),
//						scannedFile.getLastScanResult().getTimestamp() });
						dateFormat.format(date)+" GMT "+dateNote });
				gridSatLegendProducer.setDrawColorMap(true);
				gridSatLegendProducer.setDrawLabels(false);
				//          satLegendProducer.setLabelEveryOtherN(40);
				imageDim = new Dimension(240, 47);
			}
			else {
				gridSatLegendProducer.setLegendTitle(new String[] {
						scannedFile.getLastScanResult().getSourceID().toUpperCase()+" "+
						scannedFile.getLastScanResult().getDescription(),
//						scannedFile.getLastScanResult().getTimestamp(),
						dateFormat.format(date)+" GMT "+dateNote,
						"Brightness Temp. ("+goesAreaRaster.getUnits()+")"});
				gridSatLegendProducer.setDrawColorMap(true);
				gridSatLegendProducer.setDrawLabels(true);
				gridSatLegendProducer.setLabelEveryOtherN(20);
			}

		} catch (Exception e) {
			gridSatLegendProducer.setLegendTitle(new String[] { scannedFile.getLastScanResult().getFileName() });
			gridSatLegendProducer.setDrawColorMap(false);
			gridSatLegendProducer.setDrawLabels(false);
		}


		gridSatLegendProducer.setBackgroundColor(new Color(220, 220, 220, 210));
		gridSatLegendProducer.setMapBackgroundColor(this.getBackgroundColor());
		gridSatLegendProducer.setDrawBorder(true);
		gridSatLegendProducer.setInterpolateBetweenCategories(true);
		lastDecodedLegendProducer = gridSatLegendProducer;

		Image image = gridSatLegendProducer.createMediumLegendImage(imageDim);
		
		
//		JFrame frame = new JFrame();
//		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
//		frame.pack();
//		frame.setVisible(true);
		

		gridSatelliteLegend.setInsets(new Insets(0, 0, 15, image.getWidth(this)));
		gridSatelliteLegend.setImage(image);

		gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);


		gridSatelliteLegend.repaint();
		wctMapPane.repaint();
	}




	public void setGridDatasetRaster(GridDatasetRemappedRaster gridDatasetRaster) {
		this.gridDatasetRaster = gridDatasetRaster;
	}


	public GridDatasetRemappedRaster getGridDatasetRaster() throws IllegalAccessException, InstantiationException {
		if (gridDatasetRaster == null) {
			gridDatasetRaster = new GridDatasetRemappedRaster();
		}
		return gridDatasetRaster;
	}

	public GoesRemappedRaster getGoesRaster() throws IllegalAccessException, InstantiationException {
		if (goesAreaRaster == null) {
			goesAreaRaster = new GoesRemappedRaster();
		}
		return goesAreaRaster;
	}


	public GridDatasetProperties getGridProps() {
		return gridProps;
	}

	public RadialProperties getRadialProps() {
		return radialProps;
	}


	public void setGridSatelliteLegendVisibility(boolean isVisible) {
		isGridSatelliteLegendVisible = isVisible;
		if (gridSatelliteLegend != null) {
			gridSatelliteLegend.setVisible(isVisible);
			gridSatelliteLegend.repaint();
			wctMapPane.repaint();
		}
	}
	public boolean getSatelliteLegendVisibility() {
		return this.isGridSatelliteLegendVisible;
	}












	public WCTMapPaneZoomChange getMapPaneZoomChange() {
		return zoomChangeListener;
	}

	public WCTMapPane getMapPane() {
		return wctMapPane;
	}

	/**
	 * Refresh WMS background map
	 * @param index
	 * @param type
	 */
	public void refreshWMSBackground(int index, String name) {
		refreshWMSBackground(index, name, null, false, 2);
	}
	/**
	 * 
	 * @param index
	 * @param type
	 * @param layers - if null, then use default
	 */
	public void refreshWMSBackground(int index, String name, String layers) {
		refreshWMSBackground(index, name, layers, false, 2);
	}
	/**
	 * 
	 * @param index
	 * @param type
	 * @param layers - if null, then use default
	 * @param isBW - convert to grayscale black&white image?
	 */
	public void refreshWMSBackground(int index, String name, String layers, boolean isBW) {
		refreshWMSBackground(index, name, layers, isBW, 2);
	}
	/**
	 * 
	 * @param index
	 * @param type
	 * @param layers - if null, then use default
	 * @param isBW - convert to grayscale black&white image?
	 * @param numRetries
	 */
	public void refreshWMSBackground(int index, String name, String layers, boolean isBW, int numRetries) {

		for (int n=0; n<numRetries; n++) {

			WMSData wms = new WMSData();

			// GET WMS DATA
			try {         
				//wmsRGC[index].dispose();
				wmsRGC[index].setGridCoverage(wms.getGridCoverage(
						name, layers, isBW, this.getCurrentExtent(), 
						wctMapPane.getWCTZoomableBounds(new java.awt.Rectangle())));

				wmsRGC[index].setVisible(true);
				n = numRetries; // don't retry     
			} catch (WMSException niae) {
				if (n == numRetries - 1) { // last time
//					String message = niae.getMessage();
					niae.printStackTrace();
	            	JOptionPane.showMessageDialog(this, 
	            			"--- General Data Load Error ---\n"+
	            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
	            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public void setWMSBackground(int index, GridCoverage wmsGC) throws TransformException {
		wmsRGC[index].setGridCoverage(wmsGC);
		wmsRGC[index].setVisible(true);
	}
	public void setWMSBackgroundVisibility(int index, boolean visibility) {
		wmsRGC[index].setVisible(visibility);
	}



	public void displayWMS(String name, URL wmsImageURL, Rectangle2D.Double extent, float zIndex) 
	throws WMSException, TransformException, Exception {

		displayWMS(name, wmsImageURL, extent, zIndex, null, null);
	}

	public void displayWMS(String name, URL wmsImageURL, Rectangle2D.Double extent, float zIndex, 
			RenderedLogo legend, RenderedLogo logo) 
	throws WMSException, TransformException, Exception {

		displayWMS(name, wmsImageURL, extent, zIndex, -1, null, legend, logo);
	}

	/**
	 * Display a WMS image in the viewer map pane
	 * @param name              Unique name of the WMS layer
	 * @param wmsImageURL       URL of the WMS GetMap request
	 * @param extent            Geographic extent of the image.  Should match the bbox in WMS URL
	 * @param zIndex            The z order in the map's layer stack
	 * @param alpha             The transparency of the entire image 
	 *   (a value of -1 causes the previous value to be used if the WmsLayer is not new)
	 *              
	 * @param emptyBackgroundColor      The background color that should be converted to entirely transparent
	 * @param legend            The legend overlay
	 * @param logo              The logo overlay
	 * @throws WMSException
	 * @throws TransformException
	 */
	public void displayWMS(String name, URL wmsImageURL, Rectangle2D.Double extent, float zIndex, 
			int alpha, Color emptyBackgroundColor, RenderedLogo legend, RenderedLogo logo) {


		try {         


			WMSData wmsHelper = new WMSData();

			if (wmsLayerMap.containsKey(name)) {
				WmsLayer wmsLayer = wmsLayerMap.get(name);
				if (alpha < 0) {
					alpha = wmsLayer.getAlpha();
				}
				if (emptyBackgroundColor == null) {
					emptyBackgroundColor = wmsLayer.getEmptyBackgroundColor();
				}

				System.out.println("FOUND: "+name+" - alpha="+alpha);


				GridCoverage gc = wmsHelper.getGridCoverage(wmsImageURL, extent, false, 
						alpha, emptyBackgroundColor);

				wmsLayer.getRenderedGridCoverage().setGridCoverage(gc);
			}
			else {

				GridCoverage gc = wmsHelper.getGridCoverage(wmsImageURL, extent, false,
						alpha, emptyBackgroundColor);            
				RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
				rgc.setZOrder(zIndex);
				rgc.setVisible(true);
				wctMapPane.getRenderer().addLayer(rgc);

				WmsLayer wmsLayer = new WmsLayer(rgc);
				wmsLayerMap.put(name, wmsLayer);
			}



			if (logo != null) {
				logo.setVisible(true);
				if (wmsLogoMap.containsKey(name)) {
					wctMapPane.getRenderer().removeLayer(wmsLogoMap.get(name));
				}
				wctMapPane.getRenderer().addLayer(logo);
				wmsLogoMap.put(name, logo);
			}



			if (legend != null) {
				legend.setVisible(true);
				if (wmsLegendMap.containsKey(name)) {
					wctMapPane.getRenderer().removeLayer(wmsLegendMap.get(name));
				}
				wctMapPane.getRenderer().addLayer(legend);
				wmsLegendMap.put(name, legend);
			}

		} catch (WMSException e) {
//			String message = e.getMessage();
//			javax.swing.JOptionPane.showMessageDialog(this, message, "WMS ERROR", JOptionPane.INFORMATION_MESSAGE);
        	JOptionPane.showMessageDialog(this, 
        			"--- Remote Web Service Error ---\n"+
        			"Error: "+e.getMessage()+"\n"+
        			"The site may be down or there may be a problem with the internet connection.",
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		} catch (Exception e) {
//			String message = e.getMessage();
//			javax.swing.JOptionPane.showMessageDialog(this, message, "WMS ERROR", JOptionPane.INFORMATION_MESSAGE);
        	JOptionPane.showMessageDialog(this, 
        			"--- General Map Load Error ---\n"+
        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}

	}

	public void removeWMS(String name) {
		if (wmsLayerMap.containsKey(name)) {
			wctMapPane.getRenderer().removeLayer(wmsLayerMap.get(name).getRenderedGridCoverage());
			wctMapPane.getRenderer().removeLayer(wmsLegendMap.get(name));
			wctMapPane.getRenderer().removeLayer(wmsLogoMap.get(name));
			wmsLayerMap.remove(name);
		}
	}

	public void setWMSTransparency(String name, int alpha, Color emptyBackgroundColor) throws Exception {

		GridCoverage gc;
		WmsLayer wmsLayer;

		if (wmsLayerMap.containsKey(name)) {
			wmsLayer = wmsLayerMap.get(name);
			gc = wmsLayer.getRenderedGridCoverage().getGridCoverage();
		}
		else {
			throw new Exception("WMS Layer '"+name+"' was not found");
		}

		WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)gc.getRenderedImage();
		BufferedImage bimage = img.getAsBufferedImage();

		//      BufferedImage bimage = (BufferedImage)gc.getRenderedImage();
		bimage = WMSData.getTransparentImage(bimage, alpha, emptyBackgroundColor);

		wmsLayer.setAlpha(alpha);
		wmsLayer.setEmptyBackgroundColor(emptyBackgroundColor);
		wmsLayer.getRenderedGridCoverage().setGridCoverage(
				new GridCoverage(gc.getName(null), bimage, gc.getCoordinateSystem(), gc.getEnvelope())
		);

	}



	public void setGridSatelliteGridCoverage(GridCoverage gc) throws TransformException {
		boolean isVisible = gridSatelliteRGC.isVisible();

		//        if (isVisible && ! mapSelect.getGridSatelliteLegendType().trim().equalsIgnoreCase("None")) {
		//            gridSatelliteLegend.setVisible(true);
		//        }

		gridSatelliteGC = gc;
		gridSatelliteRGC.setGridCoverage(gc);
		gridSatelliteRGC.setVisible(isVisible);
		fireRenderCompleteEvent();
	}

	public void setGridSatelliteVisibility(boolean isVisible) {
		gridSatelliteRGC.setVisible(isVisible);
		if (gridSatelliteLegend != null) {
			gridSatelliteLegend.setVisible(isVisible);
		}
		if (! isVisible && getSnapshotLayers().size() == 0) {
			if (geBrowser != null) {
				geBrowser.clearWctLayer();
			}
			if (geExtBrowser != null) {
				geExtBrowser.clearWctLayer();
			}
		}
	}

	public void setGridSatelliteTransparency(int alphaChannelValue) {
		try {           
			if (scannedFile == null || 
					scannedFile.getLastScanResult().getDataType() != SupportedDataType.GOES_SATELLITE_AREA_FORMAT &&
					(scannedFile.getLastScanResult().getDataType() != SupportedDataType.GRIDDED)) {
				return;
			}


			if (currentViewType == CurrentViewType.NCDC_NCS) {
				webMapBrowser.executeJavascript("wmsLayer.setOpacity("+alphaChannelValue/255.0+");");
				alphaChannelValue = 255;
			}


			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
				gridSatelliteGC = goesAreaRaster.getGridCoverage(alphaChannelValue);
			}
			else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
				gridSatelliteGC = gridDatasetRaster.getGridCoverage(alphaChannelValue);
			}
			gridSatelliteRGC.setGridCoverage(gridSatelliteGC);

			if (gridSatelliteRGC.isVisible()) {
				fireRenderCompleteEvent();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setGridSatelliteColorTable(String colorTableAlias) {
		try {            

			
			if (scannedFile == null) {
				return;
			}
			
			
			
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {



				String ctName = mapSelect.getGridColorTableName();
				if (ctName.startsWith("Satellite: ") || ctName.startsWith("Radar: ")) {
					ColorsAndValues cav = GridDatasetColorFactory.getColorsAndValues(
							ctName.substring(ctName.indexOf(" ")+1, ctName.length()));
					cav.flip();
					gridDatasetRaster.setAutoMinMaxValues(false);
					gridDatasetRaster.setDisplayColors(cav.getColors());
					gridDatasetRaster.setDisplayMinValue(Math.min(cav.getValues()[0], cav.getValues()[cav.getValues().length-1]));
					gridDatasetRaster.setDisplayMaxValue(Math.max(cav.getValues()[0], cav.getValues()[cav.getValues().length-1]));
				}
				else {
					gridDatasetRaster.setAutoMinMaxValues(mapSelect.isGridAutoMinMaxSelected());
					if (mapSelect.isGridAutoMinMaxSelected()) {
						mapSelect.setGridColorTableMinMaxValue(gridDatasetRaster.getGridMinValue(), gridDatasetRaster.getGridMaxValue());
					}
					else {
						gridDatasetRaster.setDisplayMinValue(Math.min(mapSelect.getGridColorTableMinValue(), mapSelect.getGridColorTableMaxValue()));
						gridDatasetRaster.setDisplayMaxValue(Math.max(mapSelect.getGridColorTableMinValue(), mapSelect.getGridColorTableMaxValue()));
					}
					gridDatasetRaster.setDisplayColors(GridDatasetColorFactory.getColors(mapSelect.getGridColorTableName(), mapSelect.isFlipGridColorTable()));                    
				}




				if (mapSelect.getGridSatelliteTransparency() != -1) {
					int alpha = (int)(255.0*(1-mapSelect.getGridSatelliteTransparency()/100.0));
					setGridSatelliteGridCoverage(gridDatasetRaster.getGridCoverage(alpha));                    
				}
				else {      
					setGridSatelliteGridCoverage(gridDatasetRaster.getGridCoverage());
				}
				statusBar.setProgressText("");

				refreshGridDatasetLegend();
				gridSatelliteLegend.repaint();
				gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);
			}
			else {

				goesAreaRaster.setColorTableAlias(colorTableAlias);

				// recalculate colors and values based on new color table
				GoesColorFactory gcf = GoesColorFactory.getInstance();
				satColorsAndValues = gcf.getColorsAndValues(goesAreaRaster);

				// reset the grid with the new color table
				gridSatelliteGC = goesAreaRaster.getGridCoverage(goesAreaRaster.getCurrentAlpha());
				gridSatelliteRGC.setGridCoverage(gridSatelliteGC);
				fireRenderCompleteEvent();

				// reset the colors in the legend
				gridSatLegendProducer.setInterpolateBetweenCategories(true);
				gridSatLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryLabels(goesAreaRaster));
				gridSatLegendProducer.setCategoryColors(satColorsAndValues.getColors());
				if (goesAreaRaster.getLastBandDecoded() == Band.BAND1) {
					gridSatelliteLegend.setImage(gridSatLegendProducer.createMediumLegendImage(new Dimension(240, 32)));
				}
				else {
					gridSatelliteLegend.setImage(gridSatLegendProducer.createMediumLegendImage(new Dimension(240, 83)));
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	/**
	 * 
	 * @param alphaChannelValue - 0 is fully transparent, 255 is opaque
	 */
	public void setRadarTransparency(int alphaChannelValue) {
		setRadarTransparency(alphaChannelValue, this.rasterizer);
	}

	public void setRadarTransparency(int alphaChannelValue, WCTRasterizer rasterizer) {
		try {
			if (currentViewType == CurrentViewType.NCDC_NCS) {
				webMapBrowser.executeJavascript("wmsLayer.setOpacity("+alphaChannelValue/255.0+");");
				alphaChannelValue = 255;
			}

			radarAlphaChannelValue = alphaChannelValue;


			if (scannedFile == null || scannedFile.getLastScanResult() == null) {
				return;
			}
			
			if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_3D ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED_NEXRAD_Q2_2D) {

				radarRGC.setGridCoverage(q2Decoder.getGridCoverage(q2Props.getSelectedVariableName(), q2Props.getSelected3DHeightIndex(), radarAlphaChannelValue));
				return;
			}


			if (radarRGC != null && scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
				
				if (radarGC == null) {
					return;
				}
				

				SampleDimensionAndLabels sdl = NexradSampleDimensionFactory.getSampleDimensionAndLabels(header.getProductCode(), false);
				if (sdl != null) {
					SampleDimension[] sdArray = WCTGridCoverageSupport.setSampleDimensionAlpha(
							new SampleDimension[] { sdl.getSampleDimension() }, radarAlphaChannelValue);

			        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(radarGC.getRenderedImage());
			        Raster data = img.getData();
			        radarGC = new GridCoverage(radarGC.getName(null), (WritableRaster)data, 
			        		GeographicCoordinateSystem.WGS84, null, radarGC.getEnvelope(), sdArray);

				}
				else {
				
					radarGC = WCTGridCoverageSupport.applyAlpha2(radarGC, radarAlphaChannelValue, AlphaInterpolationType.NONE);
				}
				
				
				radarRGC.setGridCoverage(radarGC);
				fireRenderCompleteEvent();
				return;
			}




			// Update raster rendered grid coverage
			if (radarRGC != null && rasterizer != null && header != null && rasterizer.getBounds() != null) {

				if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
						scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) &&
						header.getProductType() == NexradHeader.L3RADIAL_8BIT) {

//					radarGC = WCTGridCoverageSupport.applyAlpha2(radarGC, 255);
					radarGC = WCTGridCoverageSupport.applyAlpha2(radarGC, radarAlphaChannelValue);
					radarRGC.setGridCoverage(radarGC);

				}
				else {
//					radarGC = WCTGridCoverageSupport.applyAlpha2(radarGC, 255);
					radarGC = WCTGridCoverageSupport.applyAlpha2(radarGC, radarAlphaChannelValue);
					radarRGC.setGridCoverage(radarGC);
				}

				if (radarRGC.isVisible()) {
					fireRenderCompleteEvent();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getRadarTransparency() {
		return radarAlphaChannelValue;
	}


	public void setRadarSmoothFactor(double smoothFactor) {
		try {
			this.radarSmoothFactor = smoothFactor;
			gcSupport.setSmoothFactor(smoothFactor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public double getRadarSmoothFactor() {
		return radarSmoothFactor;
	}


	public void setRadarPowerSmoothing(boolean isPowerSmoothing) {
		this.isRadarPowerSmoothing = isPowerSmoothing;
	}

	public boolean isRadarPowerSmoothing() {
		return isRadarPowerSmoothing;
	}

	public void setSatelliteSmoothFactor(double smoothFactor) {
		this.satelliteSmoothFactor = smoothFactor;
	}
	public double getSatelliteSmoothFactor() {
		return this.satelliteSmoothFactor;
	}

	public double getLastDecodedRadarElevationAngle() {
		return lastDecodedElevationAngle;
	}


	public void snapshotCurrentLayer() {

		RenderedGridCoverage rgc = null;
		Image legendImage = null;

		String[] categoryToValueArray = null;
		String units = null;
		int alpha = 255;
		boolean isResampled = true;
		if (currentDataType == CurrentDataType.RADAR) {
			if (radarGC == null) {
				JOptionPane.showMessageDialog(this, "There is no active data layer to snapshot.", "Snapshot Error", JOptionPane.ERROR_MESSAGE);
				return;
			}			
			
			rgc = new RenderedGridCoverage(radarGC);
			alpha = getRadarTransparency();
			try {
				legendImage = radLegendProducer.createLargeLegendImage(keyPanel.getPreferredSize());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if ((scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
					scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) &&
					level3Header.getProductType() != NexradHeader.L3RADIAL_8BIT) {

				categoryToValueArray = Arrays.copyOf(getLevel3Header().getDataThresholdStringArray(),
						getLevel3Header().getDataThresholdStringArray().length);
			}
			units = getWCTRasterizer().getUnits();
		}
		else {
			if (gridSatelliteGC == null) {
				JOptionPane.showMessageDialog(this, "There is no active data layer to snapshot.", "Snapshot Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			rgc = new RenderedGridCoverage(gridSatelliteGC);
			alpha = getMapSelector().getGridSatelliteTransparency();
			try {
				legendImage = gridSatLegendProducer.createMediumLegendImage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
					units = getGoesRaster().getUnits();
				}
				else {
					units = getGridDatasetRaster().getUnits();
					isResampled = ! getGridDatasetRaster().isNative();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		rgc.setZOrder(2+0.7f+((2+snapshotList.size())*0.001f));
		rgc.setVisible(true);        
		((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);

		
//		System.out.println("added "+rgc.getName(null));
//		for (RenderedLayer rl : ((StyledMapRenderer) wctMapPane.getRenderer()).getLayers()) {
//			System.out.println(rl.toString());
//		}
		
		
		
		SnapshotLayer snapshotLayer = new SnapshotLayer();
		snapshotLayer.setRenderedGridCoverage(rgc);
		snapshotLayer.setAlpha(alpha);
		snapshotLayer.setCategoryToValueArray(categoryToValueArray);
		snapshotLayer.setUnits(units);
		snapshotLayer.setResampled(isResampled);

		String dataName = "";
		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED ||
				scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
			for (String s : lastDecodedLegendProducer.getLegendTitle()) {
				dataName += s + " ";
			}            
		}
		else {
			dataName = scannedFile.getLastScanResult().getLongName();
		}

		snapshotLayer.setName("Snapshot "+getSnapshotLayers().size()+" ("+dataName+")");
		snapshotLayer.setLegendImage(legendImage);
		snapshotList.add(snapshotLayer);

		mapSelect.addSnapshotLayer(snapshotLayer);
	}

	public ArrayList<SnapshotLayer> getSnapshotLayers() {
		return snapshotList;
	}

	public void removeSnapshotLayer(SnapshotLayer layer) {
		((StyledMapRenderer) wctMapPane.getRenderer()).removeLayer(layer.getRenderedGridCoverage());
		snapshotList.remove(layer);
		fireRenderCompleteEvent();
	}


	public void setRangeRingVisibility(boolean isVisible) {
		mlNexradRangeRings.setVisible(isVisible);
	}


	/**
	 * Sets the range rings given the range ring feature collection (0-360 degrees)
	 *
	 * @return  nexrad_file  The current NexradFile
	 */
	public void setRangeRings(double minDistance, double maxDistance, double ringIncrement,
			int distUnits, Color ringColor, int ringSize, boolean label) {

		setRangeRings(minDistance, maxDistance, ringIncrement, distUnits, 0.0, 360.0, -1.0, 
				ringColor, ringSize, label);
	}
	/**
	 * Sets the range rings given the range ring feature collection
	 *
	 * @return  nexrad_file  The current NexradFile
	 */
	public void setRangeRings(double minDistance, double maxDistance, double ringIncrement,
			int distUnits, double startAzimuth, double endAzimuth, double spokeIncrement, 
			Color ringColor, int ringSize, boolean label) {

		if (header == null || currentDataType != CurrentDataType.RADAR) {
			javax.swing.JOptionPane.showMessageDialog(this, 
					"-- No NEXRAD File Has Been Loaded --", 
					"NEXRAD RANGE RING ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);          
			return;
		}

		//      System.out.println("setRangeRings ::: LAT: "+header.getLat()+" LON: "+header.getLon());
		//      System.out.println("setRangeRings ::: minDistance: "+minDistance+" max: "+maxDistance);

		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
			javax.swing.JOptionPane.showMessageDialog(this, "Range Rings are not applicable for this product.", 
					"RANGE RINGS", javax.swing.JOptionPane.ERROR_MESSAGE);

			mlNexradRangeRings.setVisible(false);
			if (rangeRings != null) {
				rangeRings.setRangeRingsVisible(false);
			}

			return;
		}

		rangeRingFeatures = RangeRings.getRangeRingFeatures(
				header.getRadarCoordinate(),
				minDistance, maxDistance, ringIncrement, distUnits,
				startAzimuth, endAzimuth, spokeIncrement,
				rangeRingFeatures);

		try {
			//Style rangeRingStyle = sb.createStyle(sb.createLineSymbolizer(ringColor, ringSize));

			if (label) {
				org.geotools.styling.Font font = sb.createFont(BaseMapManager.FONT_ARRAY[0]);
				TextSymbolizer tsRangeRings = sb.createTextSymbolizer(ringColor, font, "label");
				//tsRangeRings.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
				tsRangeRings.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
				Rule rangeRingRule = sb.createRule(new Symbolizer[] {
						sb.createLineSymbolizer(ringColor, ringSize), 
						tsRangeRings
				});

				Style rangeRingStyle = sb.createStyle();
				rangeRingStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rangeRingRule));

				mlNexradRangeRings.setStyle(rangeRingStyle);
			}
			else {
				Style rangeRingStyle = sb.createStyle(sb.createLineSymbolizer(ringColor, ringSize));
				mlNexradRangeRings.setStyle(rangeRingStyle);
			}

			mlNexradRangeRings.setVisible(false);
			mlNexradRangeRings.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public FeatureCollection getRangeRings() {
		return rangeRingFeatures;
	}


	/**
	 * Could be null if the editor has not be shown yet
	 * @return
	 */
	public MarkerEditor getMarkerEditor() {
		return markerEditor;
	}

	/**
	 * Will create a new instance if needed, and show.
	 * @return
	 */
	public MarkerEditor showMarkerEditor() {
		// Lazy creation
		if (markerEditor == null) {
			markerEditor = new MarkerEditor(mlMarkers, markerFeatures, this);
			markerEditor.pack();
			markerEditor.setLocation(10, 10);
		}
		markerEditor.setVisible(true);
		return markerEditor;
	}

	public FeatureCollection getMarkerFeatures() {
		return markerFeatures;
	}

	public BookmarkUI showBookmarkEditor() {
		// Lazy creation
		if (bookmarkEditor == null) {
			bookmarkEditor = new BookmarkUI(this);
			bookmarkEditor.pack();
			bookmarkEditor.setLocation(10, 10);
		}
		bookmarkEditor.setVisible(true);
		return bookmarkEditor;
	}


	public IdentifyUI showIdentifyTool() {
		if (identifyUI == null) {
			identifyUI = new IdentifyUI(this);
			identifyUI.pack();
			identifyUI.setLocation(20, 20);
		}
		identifyUI.pack();
		identifyUI.setVisible(true);
		return identifyUI;
	}


	public CurrentDataType getCurrentDataType() {
		return currentDataType;
	}

//	/**
//	 *  Gets the NexradMath attribute of the WCTViewer object
//	 *
//	 * @return  nexradMath  The NexradMath object
//	 */
//	public NexradMath getNexradMath() { 
//		return nexradMath;
//	}

	/**
	 *
	 * @return  wctToolBar  
	 */
	public WCTToolBar getFrameTools() { 
		return wctToolBar;
	}


	/**
	 *  Gets the nexrad_file attribute of the WCTViewer object
	 *
	 * @return  nexrad_file  The current NexradFile
	 */
	public FileScanner getFileScanner() { 
		return scannedFile;
	}

	/**
	 *  Gets the level2Header attribute of the WCTViewer object
	 *
	 * @return  level2Header  The current DecodeL2Header
	 */
	public DecodeRadialDatasetSweepHeader getRadialHeader() { 
		return radialDatasetHeader;
	}

	/**
	 *  Gets the level3Header attribute of the WCTViewer object
	 *
	 * @return  level3Header  The current DecodeL3Header
	 */
	public DecodeL3Header getLevel3Header() { 
		return level3Header;
	}

	/**
	 *  Gets the generic header interface that represents the last decoded file.
	 *
	 * @return  header  The current header loaded into the viewer
	 */
	public NexradHeader getNexradHeader() { 
		return header;
	}

	/**
	 *  Gets the level2Decoder attribute of the WCTViewer object
	 *
	 * @return  level2Decoder  The current DecodeL2Nexrad
	 */
	public DecodeRadialDatasetSweep getRadialDecoder() { 
		return radialDatasetDecoder;
	}

	@Override
	public RadialDatasetSweepRemappedRaster getRadialRemappedRaster() {
		return radialDatasetRaster;
	}


	@Override
	public SampleDimension getSampleDimension() {
		if (lastDecodedLegendProducer != null &&
				lastDecodedLegendProducer.getSampleDimensionAndLabels() != null) {
			return lastDecodedLegendProducer.getSampleDimensionAndLabels().getSampleDimension();
		}
		else { 
//			System.out.println("returning null sd");
			return null;
		}
	}	
	
	
	/**
	 *  Gets the level3Decoder attribute of the WCTViewer object
	 *
	 * @return  level3Decoder  The current DecodeL3Nexrad
	 */
	public DecodeL3Nexrad getLevel3Decoder() { 
		return level3Decoder;
	}

	/**
	 *  Gets the WCTFilter attribute of the WCTViewer.
	 *  Don't set properties in this filter, since a new filter object
	 *  is often created with each load.
	 *
	 * @return  The last WCTFilter object created
	 */
	public WCTFilter getFilter() { 
		return wctFilter;
	}

	/**
	 * Set the filter used for animations.  This will override any filter
	 * generation from the Filter UI.  However, this must be set for each
	 * data load.  After a data load ('loadData' method), the filter will
	 * revert to use the UI-based filter or no filter (if the UI has not 
	 * been initialized).
	 * @param animationFilter
	 */
	public void setAnimationFilter(WCTFilter animationFilter) {
		this.animationFilter = animationFilter;
	}

	/**
	 *  Gets the WCTFilterGUI attribute of the WCTViewer object
	 *
	 * @return  nxfilter  The current NexradFilterGUI
	 */
	public WCTFilterGUI getFilterGUI() { 
		return wctFilterGUI;
	}



//	public WCTExportGUI getNexradExportGUI() {
//		return nexradExporter;
//	}




	/**
	 *  Gets the MapContext attribute of the WCTViewer object
	 *
	 * @return  map  The current MapContext
	 */
	public WCTMapContext getMapContext() { 
		return map;
	}


	public WCTStatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 *  Gets the current NEXRAD Feature Collection loaded into the viewer.
	 *
	 * @return    The nexradFeatures object
	 */
	public FeatureCollection getNexradFeatures() {
		return nexradFeatures;
	}

	/**
	 *  Gets the base map layers currently loaded into the viewer.
	 *
	 * @return    Vector of DefaultMapLayers
	 */
	public Vector<MapLayer> getBaseMapLayers() {
		return baseMapLayers;
	}



	/**
	 *  Sets the radarGridCoverage attribute of the WCTViewer object.
	 *
	 * @param  gc  The new radarGridCoverage value
	 */
	public void setRadarGridCoverage(GridCoverage gc) {
		try {
			radarGC = gc;
			radarRGC.setGridCoverage(gc);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public void setContourGridCoverage(GridCoverage gc) {
		try {
			contourGC = gc;
			contourRGC.setGridCoverage(gc);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GridCoverage getContourGridCoverage() {
		return contourGC;
	}

	public RenderedGridCoverage getContourRenderedGridCoverage() {
		return contourRGC;
	}



	/**
	 *  Gets the nexradRenderedGridCoverage attribute of the WCTViewer object.
	 *
	 * @return  gc  The nexradRenderedGridCoverage value
	 */
	public RenderedGridCoverage getRadarRenderedGridCoverage() {
		return radarRGC;
	}

	/**
	 *  Gets the RENDERED Grid Coverage
	 *
	 * @return  
	 */
	public RenderedGridCoverage getGridSatelliteRenderedGridCoverage() {
		return gridSatelliteRGC;
	}    

	/**
	 *  Gets the GridCoverage 
	 *
	 * @return  gc  
	 */
	public GridCoverage getRadarGridCoverage() {
		return radarGC;
	}

	/**
	 *  Gets the GridCoverage 
	 *
	 * @return  gc  
	 */
	public GridCoverage getGridSatelliteGridCoverage() {
		return gridSatelliteGC;
	}


	/**
	 *  Sets the visibility of the NEXRAD GridCoverage.
	 *
	 * @param  gc  The new nexradGridCoverage value
	 */
	public void setRadarGridCoverageVisibility(boolean isVisible) {
		try {
			radarRGC.setVisible(isVisible);
			//            fireRenderCompleteEvent();
			if (! isVisible && getSnapshotLayers().size() == 0) {
				if (geBrowser != null) {
					geBrowser.clearWctLayer();
				}
				if (geExtBrowser != null) {
					geExtBrowser.clearWctLayer();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void fireRenderCompleteEvent() {
		for (RenderCompleteListener l : renderCompleteListeners) {
			l.renderComplete();
		}
	}


	/**
	 *  Returns LegendImageProducer
	 */
	public CategoryLegendImageProducer getRadarLegendImageProducer() {
		return this.radLegendProducer;
	}
	public CategoryLegendImageProducer getGridSatelliteLegendImageProducer() {
		return this.gridSatLegendProducer;
	}
	public CategoryLegendImageProducer getLastDecodedLegendImageProducer() {
		return this.lastDecodedLegendProducer;
	}


//	public JPanel getLegend() {
//		return legend;
//	}
	public WCTLegendPanel getLargeLegendPanel() {
		return keyPanel;
	}


	public void setCoordinateSystem(CoordinateSystem cs) {
		try {
			wctMapPane.setCoordinateSystem(cs);
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, "Coordinate System Error", 
					"CS ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);         
		}
	}

	public CoordinateSystem getCoordinateSystem() {
		return wctMapPane.getCoordinateSystem();
	}


	public JPanel getInfoPanel() {
		return infoPanel;
	}



	/**
	 *  Description of the Method
	 */
	public void validateAll() {
		metaPanel.validate();
		infoPanel.validate();
		mainPanel.validate();
		this.validate();
	}



















	/**
	 *  Description of the Method
	 */
	public void clearData() {
		try {

			dataUrl = null;


			gridSatelliteURL = null;

			radarRGC.setVisible(false);
			gridSatelliteRGC.setVisible(false);


			radarGC = null;
			radarRGC.setGridCoverage(
					new GridCoverage("RADAR_RASTER",
							RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
							new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
					)
			);
			gridSatelliteGC = null;
			gridSatelliteRGC.setGridCoverage(
					new GridCoverage("SATELLITE_RASTER",
							RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, HEIGHT, WIDTH, 1, null),
							new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
					)
			);

			nexradFeatures.clear();
			map.removeLayer(mlNexradAlpha);
			map.removeLayer(mlNexradAlphaLabel);
			map.removeLayer(mlNexradAlphaTrack);
			//mlNexrad = null;

			setGridSatelliteLegendVisibility(false);
			setLegendVisibility(false);
			jmiSavePS.setEnabled(false);
			jmiAlphaProp.setEnabled(false);
			//          jmiClearNexrad.setEnabled(false);

			mlNexradRangeRings.setVisible(false);
			if (rangeRings != null) {
				rangeRings.setRangeRingsVisible(false);
			}
			if (radialProps != null) {
				radialProps.setVisible(false);
				jmiRadialProp.setEnabled(false);
			}
			if (gridProps != null) {
				gridProps.setVisible(false);
				jmiGridProp.setEnabled(false);
			}

		} catch (Exception e) {
			System.out.println("CAUGHT EXCEPTION WHILE CLEARING NEXRAD DATA: " + e);
		}
	}


	/**
	 *  Description of the Method
	 */
	public void clearAllData() {
		clearData();
		map.removeLayer(mlNexradAlpha);
		map.removeLayer(mlNexradAlphaTrack);
		map.removeLayer(mlNexradAlphaLabel);

		if (getSnapshotLayers().size() > 0) {
			fireRenderCompleteEvent();
		}
		else {
			if (geBrowser != null) {
				geBrowser.clearWctLayer();
			}
			if (geExtBrowser != null) {
				geExtBrowser.clearWctLayer();
			}
		}
	}


	/**
	 * Gets the current timestamp (YYYYMMDD HH:MM) for the most recently loaded nexradfile
	 *
	 * @return    The currentNexradTimestamp value
	 */
	public String getCurrentNexradTimestamp() {
		try {
			return scannedFile.getLastScanResult().getTimestamp();
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * Gets the current type for the most recently loaded nexradfile
	 *
	 * @return    The currentNexradTypeString value
	 */
	public String getCurrentNexradTypeString() {
		return scannedFile.getLastScanResult().getDataType()+"";
	}

	/**
	 * Gets the current type for the most recently loaded nexradfile <br>
	 * Types defined in NexradHeader interface
	 *
	 * @return    The currentNexradType value
	 */
	public int getCurrentNexradType() {
		try {      
			return (header.getProductType());
		} catch (Exception e) {
			return NexradHeader.UNKNOWN;
		}
	}



	/**
	 * Gets the current site id for the most recently loaded nexradfile (4-letter id)
	 *
	 * @return    The currentNexradSiteID value
	 */
	public String getCurrentNexradSiteID() {
		try {
			return scannedFile.getLastScanResult().getDisplayName().substring(0, 4);
		} catch (Exception e) {
			return "ERR1";
		}
	}

	/**
	 * Gets the alphanumeric map layer
	 *
	 * @return    The alphanumericTheme value
	 */
	public MapLayer getAlphanumericMapLayer() {
		return mlNexradAlpha;
	}


	/**
	 * Gets the alphanumeric supplemental data as an array of pages.
	 *
	 * @return        Array of Strings that represent, where each String is a page
	 */
	public String[] getAlphanumericSupplementalArray() throws IOException {
		return (alpha_decoder.getSupplementalDataArray());
	}


	/**
	 * Gets the current full filename for the most recently loaded nexradfile
	 *
	 * @return    The currentNexradFilename value
	 */
	public String getCurrentNexradFilename() {
		return (nexradFile);
	}

	/**
	 * Gets the current URL for the most recently loaded nexradfile
	 *
	 * @return    The currentNexradURL value
	 */
	public URL getCurrentDataURL() {
		return (dataUrl);
	}



	/**
	 * Sets the enabled/disabled property of the alphanumeric menu item
	 *
	 * @param  on  The new alphanumericProperties value
	 */
	public void setAlphanumericProperties(boolean on) {
		jmiAlphaProp.setEnabled(on);
	}


	/**
	 *  Gets the alphanumericDecoder attribute of the WCTViewer object
	 *
	 * @return    The alphanumericDecoder value
	 */
	public DecodeL3Alpha getAlphanumericDecoder() {
		return alpha_decoder;
	}


	/**
	 * Show both the alphanumeric themes
	 *
	 * @param  on  The new showAlphanumericThemes value
	 */
	public void setShowAlphanumericLayers(boolean on) {
		if (mlNexradAlpha != null) {
			mlNexradAlpha.setVisible(on);
		}
		if (mlNexradAlphaTrack != null) {
			mlNexradAlphaTrack.setVisible(on);
		}
		if (mlNexradAlphaLabel != null) {
			mlNexradAlphaLabel.setVisible(on);
		}
	}


	/**
	 * Show the alphanumeric points
	 *
	 * @param  on  The new showAlphanumericPoints value
	 */
	public void setShowAlphanumericPoints(boolean on) {
		try {
			if (mlNexradAlpha != null) {
				mlNexradAlpha.setVisible(on);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Show the alphanumeric labels
	 *
	 * @param  on  The new showAlphanumericLabels value
	 */
	public void setShowAlphanumericLabels(boolean on) {
		try {
			if (mlNexradAlphaLabel != null) {
				mlNexradAlphaLabel.setVisible(on);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Show the alphanumeric labels
	 *
	 * @param  on  The new showAlphanumericLabels value
	 */
	public void setShowAlphanumericTracks(boolean on) {
		try {
			mlNexradAlphaTrack.setVisible(on);
		} catch (Exception e) {}
	}



	/**
	 * Gets the alphanumeric line width (both points and labels)
	 *
	 * @return    The alphanumericLineWidth value
	 */
	public int getAlphanumericLineWidth() {
		try {

			return alphaLineWidth;
		} catch (NullPointerException e) {
			return 0;
		}
	}


	/**
	 * Gets the alphanumeric line color (both points and labels)
	 *
	 * @return    The alphanumericLineColor value
	 */
	public Color getAlphanumericLineColor() {
		try {
			return alphaLineColor;
		} catch (NullPointerException e) {
			return (Color.white);
		}
	}


	/**
	 * Gets the alphanumeric fill color (both points and labels)
	 * (Lets use this to store the monoshader color)
	 *
	 * @return    The alphanumericFillColor value
	 */
	public Color getAlphanumericFillColor() {
		try {
			return alphaFillColor;
		} catch (NullPointerException e) {
			return (Color.white);
		}
	}


	/**
	 * Returns the boolean status of alphaHalo (draw Halos on alphanumeric text labels?)
	 */
	public boolean getAlphaHalo() {
		return alphaHalo;
	}

	/**
	 * Sets boolean status of alphaHalo (draw Halos on alphanumeric text labels?)
	 */
	public void setAlphaHalo(boolean alphaHalo) {
		this.alphaHalo = alphaHalo;

		// Recreate/refresh the symbols for this to take effect
		setAlphanumericLabelSize(alphaLineWidth);
	}


	/**
	 * Sets the alphanumeric line width (both points and labels)
	 *
	 * @param  newWidth  The new alphanumericLineWidth value
	 */
	public void setAlphanumericLineWidth(int newWidth) {

		alphaLineWidth = newWidth;

		try {
			MapLayer ml = mlNexradAlpha;
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {

				if (alpha_decoder.getLineFeatures() != null) {

					Rule rules[] = new Rule[5];
					Style alphaStyle = sb.createStyle();
					try {
						BetweenFilter filters[] = new BetweenFilter[rules.length];
						FilterFactory ffi = FilterFactory.createFilterFactory();

						Color color = alphaFillColor;
						Color linecolor = alphaLineColor;
						// Create standard filter for all products except DPA
						for (int i = 0; i < rules.length; i++) {

							filters[i] = ffi.createBetweenFilter();
							Mark alphaTrackMark = sb.createMark(alphaSymbol, color, linecolor, newWidth);
							Graphic grAlphaTrack = sb.createGraphic(null, alphaTrackMark, null);
							rules[i] = sb.createRule(sb.createPointSymbolizer(grAlphaTrack));

							filters[i].addLeftValue(sb.literalExpression(i * 15));
							filters[i].addRightValue(sb.literalExpression((i + 1) * 15));
							filters[i].addMiddleValue(ffi.createAttributeExpression(alpha_decoder.getFeatureTypes()[0], "time"));
							rules[i].setFilter(filters[i]);

							alphaStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));

							color = color.darker();
							linecolor = linecolor.darker();
						}
						mlNexradAlpha.setStyle(alphaStyle);
						mlNexradAlphaTrack.setStyle(sb.createStyle(sb.createLineSymbolizer(
								alphaLineColor, newWidth / 2.0)));

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				else {

					Mark mark = sb.createMark(alphaSymbol, alphaLineColor,
							alphaLineColor, newWidth);
					Graphic gr = sb.createGraphic(null, mark, null);
					mlNexradAlpha.setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
				}

			}
			else {
				mlNexradAlpha.setStyle(
						sb.createStyle(sb.createLineSymbolizer(alphaLineColor, newWidth)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 * Sets the alphanumeric line color (both points and labels)
	 *
	 * @param  newColor  The new alphanumericFillColor value
	 */
	public void setAlphanumericFillColor(Color newColor) {
		try {

			alphaFillColor = newColor;

			MapLayer ml = mlNexradAlpha;
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {

				if (alpha_decoder.getLineFeatures() != null) {

					Rule rules[] = new Rule[5];
					Style alphaStyle = sb.createStyle();
					try {
						BetweenFilter filters[] = new BetweenFilter[rules.length];
						FilterFactory ffi = FilterFactory.createFilterFactory();

						Color color = new Color(newColor.getRGB());
						Color linecolor = alphaLineColor;
						// Create standard filter for all products except DPA
						for (int i = 0; i < rules.length; i++) {

							filters[i] = ffi.createBetweenFilter();
							Mark alphaTrackMark = sb.createMark(alphaSymbol, color, linecolor, alphaLineWidth);
							Graphic grAlphaTrack = sb.createGraphic(null, alphaTrackMark, null);
							rules[i] = sb.createRule(sb.createPointSymbolizer(grAlphaTrack));

							filters[i].addLeftValue(sb.literalExpression(i * 15));
							filters[i].addRightValue(sb.literalExpression((i + 1) * 15));
							filters[i].addMiddleValue(ffi.createAttributeExpression(alpha_decoder.getFeatureTypes()[0], "time"));
							rules[i].setFilter(filters[i]);

							alphaStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));

							color = color.darker();
							linecolor = linecolor.darker();
						}
						mlNexradAlpha.setStyle(alphaStyle);
						mlNexradAlphaTrack.setStyle(sb.createStyle(sb.createLineSymbolizer(newColor, alphaLineWidth)));

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				else {

					Mark mark = sb.createMark(alphaSymbol, newColor,
							alphaLineColor, alphaLineWidth);
					Graphic gr = sb.createGraphic(null, mark, null);
					mlNexradAlpha.setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
				}
			}
			else {
				mlNexradAlpha.setStyle(sb.createStyle(
						sb.createLineSymbolizer(newColor, alphaLineWidth)));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 * Sets the alphanumeric line color (both points and labels)
	 *
	 * @param  newColor  The new alphanumericLineColor value
	 */
	public void setAlphanumericLineColor(Color newColor) {
		try {

			alphaLineColor = newColor;

			MapLayer ml = mlNexradAlpha;
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {

				if (alpha_decoder.getLineFeatures() != null) {

					Rule rules[] = new Rule[5];
					Style alphaStyle = sb.createStyle();
					try {
						BetweenFilter filters[] = new BetweenFilter[rules.length];
						FilterFactory ffi = FilterFactory.createFilterFactory();

						Color color = new Color(newColor.getRGB());
						Color fillcolor = alphaFillColor;
						// Create standard filter for all products except DPA
						for (int i = 0; i < rules.length; i++) {

							filters[i] = ffi.createBetweenFilter();
							Mark alphaTrackMark = sb.createMark(alphaSymbol, fillcolor, color, alphaLineWidth);
							Graphic grAlphaTrack = sb.createGraphic(null, alphaTrackMark, null);
							rules[i] = sb.createRule(sb.createPointSymbolizer(grAlphaTrack));

							filters[i].addLeftValue(sb.literalExpression(i * 15));
							filters[i].addRightValue(sb.literalExpression((i + 1) * 15));
							filters[i].addMiddleValue(ffi.createAttributeExpression(alpha_decoder.getFeatureTypes()[0], "time"));
							rules[i].setFilter(filters[i]);

							alphaStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));

							color = color.darker();
							fillcolor = fillcolor.darker();
						}
						mlNexradAlpha.setStyle(alphaStyle);
						mlNexradAlphaTrack.setStyle(sb.createStyle(sb.createLineSymbolizer(newColor, alphaLineWidth)));

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				else {
					Mark mark = sb.createMark(alphaSymbol, alphaFillColor,
							newColor, alphaLineWidth);
					Graphic gr = sb.createGraphic(null, mark, null);
					mlNexradAlpha.setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
				}

			}
			else {
				mlNexradAlpha.setStyle(sb.createStyle(
						sb.createLineSymbolizer(newColor, alphaLineWidth)));
			}

			//org.geotools.styling.Font font = sb.createFont(new Font("Arial", Font.PLAIN, 12));
			TextSymbolizer tsAlphaLabel = sb.createTextSymbolizer(newColor, BaseMapManager.GT_FONT_ARRAY[alphaLineWidth-1], "id");
			tsAlphaLabel.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
			if (alphaHalo) {
				tsAlphaLabel.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			}
			Style alphaLabelStyle = sb.createStyle(tsAlphaLabel);
			mlNexradAlphaLabel.setStyle(alphaLabelStyle);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 * Sets the alphanumeric transparency percent (0 to 1)
	 * (Both points and labels)
	 *
	 * @param  transValue  The new alphanumericTransparency value
	 */
	public void setAlphanumericTransparency(double transValue) {

		alphaTransparency = transValue;

		try {
			MapLayer ml = mlNexradAlpha;
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(alphaSymbol, alphaFillColor,
						alphaLineColor, alphaLineWidth);
				Graphic gr = sb.createGraphic(null, mark, null);
				gr.setOpacity(sb.literalExpression(transValue));
				System.out.println("SETTING ALPHA TRANS: " + transValue);
				mlNexradAlpha.setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
			}
			else {
				mlNexradAlpha.setStyle(sb.createStyle(
						sb.createLineSymbolizer(alphaLineColor, alphaLineWidth)));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 * Gets the alphanumeric monoshader transparency percent
	 * (0-100) derived from alpha value (0-255) (single color)
	 * (Both points and labels)
	 *
	 * @return    The alphanumericTransparency value
	 */
	public double getAlphanumericTransparency() {
		return alphaTransparency;
	}


	/**
	 * Sets the alphanumeric symbol type
	 * (Circle, cross, square, triangle, xmark)
	 *
	 * @param  symbol  The new alphanumericSymbol value
	 */
	public void setAlphanumericSymbol(String symbol) {
		try {
			alphaSymbol = symbol;
			MapLayer ml = mlNexradAlpha;
			if (ml == null) {
				return;
			}
			
			if (ml.getFeatureSource().getSchema().getDefaultGeometry() == null) {
				return;
			}
			
			
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {

				if (alpha_decoder.getLineFeatures() != null) {

					Rule rules[] = new Rule[5];
					Style alphaStyle = sb.createStyle();
					try {
						BetweenFilter filters[] = new BetweenFilter[rules.length];
						FilterFactory ffi = FilterFactory.createFilterFactory();

						Color color = alphaFillColor;
						Color linecolor = alphaLineColor;
						// Create standard filter for all products except DPA
						for (int i = 0; i < rules.length; i++) {

							filters[i] = ffi.createBetweenFilter();
							Mark alphaTrackMark = sb.createMark(alphaSymbol, color, linecolor, alphaLineWidth);
							Graphic grAlphaTrack = sb.createGraphic(null, alphaTrackMark, null);
							rules[i] = sb.createRule(sb.createPointSymbolizer(grAlphaTrack));

							filters[i].addLeftValue(sb.literalExpression(i * 15));
							filters[i].addRightValue(sb.literalExpression((i + 1) * 15));
							filters[i].addMiddleValue(ffi.createAttributeExpression(alpha_decoder.getFeatureTypes()[0], "time"));
							rules[i].setFilter(filters[i]);

							alphaStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));

							color = color.darker();
							linecolor = linecolor.darker();
						}
						mlNexradAlpha.setStyle(alphaStyle);
						mlNexradAlphaTrack.setStyle(sb.createStyle(sb.createLineSymbolizer(alphaLineColor, alphaLineWidth)));

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				else {

					Mark mark = sb.createMark(symbol, alphaFillColor,
							alphaLineColor, alphaLineWidth);
					Graphic gr = sb.createGraphic(null, mark, null);
					mlNexradAlpha.setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
				}

			}
			else {
				mlNexradAlpha.setStyle(sb.createStyle(
						sb.createLineSymbolizer(alphaLineColor, alphaLineWidth)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void setAlphanumericLabelSize(int size) {

		try {

			TextSymbolizer tsAlphaLabel = sb.createTextSymbolizer(alphaLineColor, BaseMapManager.GT_FONT_ARRAY[size-1], "id");
			tsAlphaLabel.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
			if (alphaHalo) {
				tsAlphaLabel.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			}
			Style alphaLabelStyle = sb.createStyle(tsAlphaLabel);

			mlNexradAlphaLabel.setStyle(alphaLabelStyle);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}





	/**
	 * Gets the alphanumeric symbol type (integer ShaderMarker fields)
	 * (Circle, cross, square, triangle, xmark)
	 *
	 * @return    The alphanumericSymbol value
	 */
	public String getAlphanumericSymbol() {
		return alphaSymbol;
	}


	/**
	 * Gets the alphanumeric line color (both points and labels)
	 */
	public void clearNexradAlphaBackground() {
		try {
			radarRGC.setVisible(false);
			map.removeLayer(mlNexrad);
			nexradFeatures.clear();

			keyPanel.setIsUsingHeader(false);
			mainPanel.repaint();
		} catch (Exception e) {}
		return;
	}



	/**
	 * Sets the distance units for the distance/azimuth mouse-over label
	 * Sets to MILES, KM or NAUTICAL_MI (International)
	 *
	 * @param  units  The new distanceUnits value
	 */
	public void setDistanceUnits(int units) {
		distUnits = units;
		//diazdisplay.setUnits(units);
	}


	/**
	 * Gets the theme line color for the specified theme in the Theme Vector
	 *
	 * @param  index  Description of the Parameter
	 * @return        The themeLineColor value
	 */
	public Color getLayerLineColor(int index) {
		try {

			return ((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).getLineColor();
		} catch (NullPointerException e) {
			return (Color.white);
		}
	}


	// END METHOD getThemeLineColor

	/**
	 * Gets the theme fill color for the specified theme in the Theme Vector
	 *
	 * @param  index  Description of the Parameter
	 * @return        The themeFillColor value
	 */
	public Color getLayerFillColor(int index) {
		try {

			return ((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).getFillColor();
		} catch (NullPointerException e) {
			return (Color.white);
		}
	}


	// END METHOD getThemeFillColor

	/**
	 *  Gets the themeLineWidth attribute of the WCTViewer object
	 *
	 * @param  index  Description of the Parameter
	 * @return        The themeLineWidth value
	 */
	public int getLayerLineWidth(int index) {
		try {

			return ((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).getLineWidth();
		} catch (NullPointerException e) {
			return 0;
		}
	}


	// END METHOD getThemeLineWidth

	/**
	 *  Gets the themeStatus attribute of the WCTViewer object
	 *
	 * @param  index  Description of the Parameter
	 * @return        The themeStatus value
	 */
	public boolean getLayerStatus(int index) {
		return (baseMapLayers.elementAt(index) != null);
	}


	/**
	 *  Sets the themeLineColor attribute of the WCTViewer object
	 *
	 * @param  index     The new themeLineColor value
	 * @param  newColor  The new themeLineColor value
	 */
	public void setLayerLineColor(int index, Color newColor) {
		try {
			((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).setLineColor(newColor);

			if (index == STATES) {
				for (int n = 0; n < NUM_LAYERS; n++) {
					if (n == STATES) {
						continue;
					}
					
					try {
						MapLayer ml = ((MapLayer) baseMapLabelLayers.elementAt(index));
						FeatureTypeStyle[] fts = ml.getStyle().getFeatureTypeStyles();
						Style style = sb.createStyle();
						org.geotools.styling.Font font = null;
						double minScaleDenom = BaseMapStyleInfo.NO_MIN_SCALE;
						double maxScaleDenom = BaseMapStyleInfo.NO_MAX_SCALE;
						double declutterFlagAsRotation = 0;
						for (int m=0; m<fts.length; m++) {
							Symbolizer[] symbs = fts[m].getRules()[0].getSymbolizers();
							for (int i = 0; i < symbs.length; i++) {
								if (symbs[i] instanceof TextSymbolizer) {
									font = ((TextSymbolizer) symbs[i]).getFonts()[0];
									minScaleDenom = fts[m].getRules()[0].getMinScaleDenominator();
									maxScaleDenom = fts[m].getRules()[0].getMaxScaleDenominator();
									declutterFlagAsRotation = 
										Double.parseDouble(((PointPlacementImpl)((TextSymbolizer) symbs[i]).getLabelPlacement()).getRotation().toString());

									System.out.println(declutterFlagAsRotation);
								}
							}
						}
						DeclutterType declutterFlag = index == WCTViewer.CLIMATE_DIV ? DeclutterType.NONE : DeclutterType.FULL;

						ml.setStyle(BaseMapManager.generateLabelStyle(index, font, getLayerLineColor(n), newColor, 
								ml.getFeatureSource(), minScaleDenom, maxScaleDenom, declutterFlag));
						setLabelVisibility(index, !isLabelVisible(index));
						setLabelVisibility(index, !isLabelVisible(index));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
			
			
			
			
			
			
			


			MapLayer ml = ((MapLayer) baseMapLayers.elementAt(index));
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, getLayerFillColor(index),
						newColor, getLayerLineWidth(index));
				Graphic gr = sb.createGraphic(null, mark, null);
				((MapLayer) baseMapLayers.elementAt(index)).setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
			}
			else {
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}

			try {
				ml = ((MapLayer) baseMapLabelLayers.elementAt(index));
				FeatureTypeStyle[] fts = ml.getStyle().getFeatureTypeStyles();
				Style style = sb.createStyle();
				org.geotools.styling.Font font = null;
				double minScaleDenom = BaseMapStyleInfo.NO_MIN_SCALE;
				double maxScaleDenom = BaseMapStyleInfo.NO_MAX_SCALE;
				double declutterFlagAsRotation = 0;
				for (int m=0; m<fts.length; m++) {
					Symbolizer[] symbs = fts[m].getRules()[0].getSymbolizers();
					for (int i = 0; i < symbs.length; i++) {
						if (symbs[i] instanceof TextSymbolizer) {
							font = ((TextSymbolizer) symbs[i]).getFonts()[0];
							minScaleDenom = fts[m].getRules()[0].getMinScaleDenominator();
							maxScaleDenom = fts[m].getRules()[0].getMaxScaleDenominator();
							declutterFlagAsRotation = 
								Double.parseDouble(((PointPlacementImpl)((TextSymbolizer) symbs[i]).getLabelPlacement()).getRotation().toString());
						
							System.out.println(declutterFlagAsRotation);
						}
					}
				}
				DeclutterType declutterFlag = index == WCTViewer.CLIMATE_DIV ? DeclutterType.NONE : DeclutterType.FULL;
				
				ml.setStyle(BaseMapManager.generateLabelStyle(index, font, newColor, getLayerFillColor(STATES),
						ml.getFeatureSource(), minScaleDenom, maxScaleDenom, declutterFlag));
				setLabelVisibility(index, !isLabelVisible(index));
				setLabelVisibility(index, !isLabelVisible(index));
			} catch (Exception ee) {
			}

			setLayerVisibility(index, !isLayerVisible(index));
			setLayerVisibility(index, !isLayerVisible(index));

			// hack to link behavior of country layers (usa and non-usa)
			if (index == COUNTRIES_OUT) {
				setLayerLineColor(COUNTRIES_OUT_USA, newColor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	// END METHOD setThemeLineColor

	/**
	 *  Sets the themeFillColor attribute of the WCTViewer object
	 *
	 * @param  index     The new themeFillColor value
	 * @param  newColor  The new themeFillColor value
	 */
	public void setLayerFillColor(int index, Color newColor) {
		try {
			((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).setFillColor(newColor);

			if (index == STATES) {
				for (int n = STATES+1; n < NUM_LAYERS; n++) {
					MapLayer ml = ((MapLayer) baseMapLabelLayers.elementAt(n));
					try {
						FeatureTypeStyle[] fts = ml.getStyle().getFeatureTypeStyles();
						Style style = sb.createStyle();
						for (int m=0; m<fts.length; m++) {

							Symbolizer[] symbs = fts[m].getRules()[0].getSymbolizers();                 
							for (int i = 0; i < symbs.length; i++) {
								if (symbs[i] instanceof TextSymbolizer && i != index) {
									((TextSymbolizer) symbs[i]).setFill(sb.createFill(newColor));
									((TextSymbolizer) symbs[i]).setHalo(sb.createHalo(getLayerFillColor(STATES), .7, 2.2));

									style.addFeatureTypeStyle(sb.createFeatureTypeStyle(fts[m].getName(), 
											new Symbolizer[] { ((TextSymbolizer) symbs[i]) }));
								}
							}
						}
					} catch (Exception e) {
					}
				}
			}


			MapLayer ml = ((MapLayer) baseMapLayers.elementAt(index));
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, newColor,
						getLayerLineColor(index), getLayerLineWidth(index) - 1);
				Graphic gr = sb.createGraphic(null, mark, null);
				((MapLayer) baseMapLayers.elementAt(index)).setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
			}
			else if (LineString.class.isAssignableFrom(geometryClass)
					|| MultiLineString.class.isAssignableFrom(geometryClass)) {
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}
			else {
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}

			try {
				ml = ((MapLayer) baseMapLabelLayers.elementAt(index));
				FeatureTypeStyle[] fts = ml.getStyle().getFeatureTypeStyles();
				Style style = sb.createStyle();
				org.geotools.styling.Font font = null;
				double minScaleDenom = BaseMapStyleInfo.NO_MIN_SCALE;
				double maxScaleDenom = BaseMapStyleInfo.NO_MAX_SCALE;
				double declutterFlagAsRotation = 0;
				for (int m=0; m<fts.length; m++) {
					Symbolizer[] symbs = fts[m].getRules()[0].getSymbolizers();
					for (int i = 0; i < symbs.length; i++) {
						if (symbs[i] instanceof TextSymbolizer) {
							font = ((TextSymbolizer) symbs[i]).getFonts()[0];
							minScaleDenom = fts[m].getRules()[0].getMinScaleDenominator();
							maxScaleDenom = fts[m].getRules()[0].getMaxScaleDenominator();
							declutterFlagAsRotation = 
								Double.parseDouble(((PointPlacementImpl)((TextSymbolizer) symbs[i]).getLabelPlacement()).getRotation().toString());
						
							System.out.println(declutterFlagAsRotation);
						}
					}
				}

				DeclutterType declutterFlag = index == WCTViewer.CLIMATE_DIV ? DeclutterType.NONE : DeclutterType.FULL;
				
				ml.setStyle(BaseMapManager.generateLabelStyle(index, font, newColor, getLayerFillColor(STATES),
						ml.getFeatureSource(), minScaleDenom, maxScaleDenom, declutterFlag));
				setLabelVisibility(index, !isLabelVisible(index));
				setLabelVisibility(index, !isLabelVisible(index));
			} catch (Exception ee) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		setLayerVisibility(index, !isLayerVisible(index));
		setLayerVisibility(index, !isLayerVisible(index));
		
		// hack to link behavior of country layers (usa and non-usa)
		if (index == COUNTRIES) {
			setLayerFillColor(COUNTRIES_USA, newColor);
		}
	}
	// END METHOD setThemeFillColor

	/**
	 *  Sets the themeFillColor attribute of the WCTViewer object
	 *
	 * @param  index      The new themeFillColor value
	 * @param  fillColor  The new themeFillAndLineColor value
	 * @param  lineColor  The new themeFillAndLineColor value
	 */
	public void setLayerFillAndLineColor(int index, Color fillColor, Color lineColor) {
		try {
			((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).setLineColor(lineColor);
			((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).setFillColor(fillColor);

			if (index == STATES) {

				Rectangle2D.Double curExtent = getCurrentExtent();
				setCurrentExtent(new Rectangle2D.Double(-30, 35, 0.01, 0.01));


				JDialog dialog = new JDialog(this, "Style Update Progress");
				JProgressBar progressBar = new JProgressBar(0, NUM_LAYERS);
				progressBar.setStringPainted(true);
				dialog.getContentPane().add(progressBar);
				dialog.setBounds(10, 10, 300, 50);
				dialog.setVisible(true);
				for (int n = STATES + 1; n < NUM_LAYERS; n++) {          
					progressBar.setValue(n);
					progressBar.setString("Updating Style "+n+" of "+NUM_LAYERS+" Layers");
					if (n == CITY250 || n == CITY100 || 
							n == CITY35 || n == CITY10 || 
							n == CITY_SMALL || n == AIRPORTS || 
							n == ASOS_AWOS || n == WSR) {

						setLayerFillAndLineColor(n, getLayerFillColor(n), getLayerLineColor(n));
					}
					else {
						setLayerLineColor(n, getLayerLineColor(n));
					}
				}
				dialog.dispose();

				setCurrentExtent(curExtent);
			}

			MapLayer ml = ((MapLayer) baseMapLayers.elementAt(index));
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, fillColor, lineColor, getLayerLineWidth(index) - 1);
				Graphic gr = sb.createGraphic(null, mark, null);
				((MapLayer) baseMapLayers.elementAt(index)).setStyle(sb.createStyle(sb.createPointSymbolizer(gr)));
			}
			else if (LineString.class.isAssignableFrom(geometryClass)
					|| MultiLineString.class.isAssignableFrom(geometryClass)) {
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}
			else {
				
				if (index == STATES) {
					BaseMapStyleInfo bmsi = baseMapStyleInfo.elementAt(index);
					Style s = BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index));
				}
				
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}

			
			
			if (index < baseMapLabelLayers.size()) {
				try {
					ml = ((MapLayer) baseMapLabelLayers.elementAt(index));
					if (ml != null && ml.getStyle() != null) {

						FeatureTypeStyle[] fts = ml.getStyle().getFeatureTypeStyles();
						Style style = sb.createStyle();
						org.geotools.styling.Font font = null;
						double minScaleDenom = BaseMapStyleInfo.NO_MIN_SCALE;
						double maxScaleDenom = BaseMapStyleInfo.NO_MAX_SCALE;
						double declutterFlagAsRotation = 0;
						for (int m=0; m<fts.length; m++) {
							Symbolizer[] symbs = fts[m].getRules()[0].getSymbolizers();
							for (int i = 0; i < symbs.length; i++) {
								if (symbs[i] instanceof TextSymbolizer) {
									font = ((TextSymbolizer) symbs[i]).getFonts()[0];
									minScaleDenom = fts[m].getRules()[0].getMinScaleDenominator();
									maxScaleDenom = fts[m].getRules()[0].getMaxScaleDenominator();
									declutterFlagAsRotation = 
										Double.parseDouble(((PointPlacementImpl)((TextSymbolizer) symbs[i]).getLabelPlacement()).getRotation().toString());

									System.out.println(declutterFlagAsRotation);
								}
							}
						}

						DeclutterType declutterFlag = index == WCTViewer.CLIMATE_DIV ? DeclutterType.NONE : DeclutterType.FULL;

						ml.setStyle(BaseMapManager.generateLabelStyle(index, font, lineColor, getLayerFillColor(STATES), 
								ml.getFeatureSource(), minScaleDenom, maxScaleDenom, declutterFlag));


						setLabelVisibility(index, !isLabelVisible(index));
						setLabelVisibility(index, !isLabelVisible(index));

					}
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			
			setLayerVisibility(index, !isLayerVisible(index));
			setLayerVisibility(index, !isLayerVisible(index));

			
			
System.out.println(isLabelVisible(index)+"=label  "+isLayerVisible(index));			
			
			
			// hack to link behavior of country layers (usa and non-usa)
			if (index == COUNTRIES) {
				setLayerFillAndLineColor(COUNTRIES_USA, fillColor, lineColor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 *  Sets the themeLineWidth attribute of the WCTViewer object
	 *
	 * @param  index     The new themeLineWidth value
	 * @param  newWidth  The new themeLineWidth value
	 */
	public void setLayerLineWidth(int index, int newWidth) {
		try {
			((BaseMapStyleInfo) (baseMapStyleInfo.elementAt(index))).setLineWidth(newWidth);

			MapLayer ml = ((MapLayer) baseMapLayers.elementAt(index));
			Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, getLayerLineColor(index),
						getLayerLineColor(index), newWidth);
				Graphic gr = sb.createGraphic(null, mark, null);
				((MapLayer) baseMapLayers.elementAt(index)).setStyle(
						sb.createStyle(sb.createPointSymbolizer(gr)));
			}
			else {
//				((MapLayer) baseMapLayers.elementAt(index)).setStyle(
//						sb.createStyle(sb.createLineSymbolizer(getLayerLineColor(index), newWidth)));
				baseMapLayers.elementAt(index).setStyle(BaseMapManager.generateStyle(baseMapStyleInfo.elementAt(index)));
			}

			setLayerVisibility(index, !isLayerVisible(index));
			setLayerVisibility(index, !isLayerVisible(index));

			
			// hack to link behavior of country layers (usa and non-usa)
			if (index == COUNTRIES_OUT) {
				setLayerLineWidth(COUNTRIES_OUT_USA, newWidth);
			}
		} catch (NullPointerException e) {
			return;
		}
	}


	// END METHOD setThemeLineWidth


	/**
	 *  Sets the layer visibility
	 *
	 * @param  index  The new themeVisibility value
	 * @param  vis    The new themeVisibility value
	 */
	public void setLayerVisibility(int index, boolean vis) {
		try {

			zoomChangeListener.setActive(false);


			// don't unload or reload the background map
			if (index == STATES || index == COUNTRIES || index == COUNTRIES_USA) {
				logger.fine("SETTING STATES/COUNTRIES LAYER "+index+" TO "+vis);

				((MapLayer) baseMapLayers.elementAt(index)).setVisible(vis);
				return;
			}

			// NEW!  Add layer if not already added to context.
			if (vis && map.indexOf((MapLayer)baseMapLayers.elementAt(index)) == -1) {

				map.removeLayer(index);
				map.addLayer(index, (MapLayer)baseMapLayers.elementAt(index));
			}
			else {
				map.removeLayer(index);                
				map.addLayer(index, new DefaultMapLayer(org.geotools.feature.FeatureCollections.newCollection(), sb.createStyle()));
			}


			logger.fine("SETTING " + index + " TO " + vis);
			((MapLayer) baseMapLayers.elementAt(index)).setVisible(vis);

			updateMemoryLabel();


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			zoomChangeListener.setActive(true);
		}
		
		// hack to link behavior of country layers (usa and non-usa)
		if (index == COUNTRIES_OUT) {
			setLayerVisibility(COUNTRIES_OUT_USA, vis);
		}
	}


	// END METHOD setThemeVisibility

	/**
	 *  Gets the themeVisibility attribute of the WCTViewer object
	 *
	 * @param  index  The new themeVisibility value
	 * @return        The themeVisible value
	 */
	public boolean isLayerVisible(int index) {
		try {
			return ((MapLayer) baseMapLayers.elementAt(index)).isVisible();
		} catch (NullPointerException e) {
			return false;
		}
	}


	// END METHOD isThemeVisibility

	/**
	 *  Sets the labelVisibility attribute of the WCTViewer object
	 *
	 * @param  index  The new labelVisibility value
	 * @param  vis    The new labelVisibility value
	 */
	public void setLabelVisibility(int index, boolean vis) {
		try {

			if (index >= baseMapLabelLayers.size() || ((MapLayer)baseMapLabelLayers.elementAt(index)) == null) {
				return;
			}
			
			zoomChangeListener.setActive(false);
			
			((MapLayer)baseMapLabelLayers.elementAt(index)).setVisible(vis);

			System.out.println("SETTING LABEL " + index + " TO " + vis);
			logger.fine("SETTING LABEL " + index + " TO " + vis);
			//            if (vis && map.indexOf((MapLayer)baseMapLabels.elementAt(index)) == -1) {
			//                map.addLayer(index, (MapLayer)baseMapLabels.elementAt(index));
			//            }
			//            else {
			//                map.removeLayer((MapLayer)baseMapLabels.elementAt(index));
			//            }

			if (vis && map.indexOf((MapLayer)baseMapLabelLayers.elementAt(index)) == -1) {
				map.addLayer(100+index, (MapLayer)baseMapLabelLayers.elementAt(index));
			}
			else if (! vis && map.indexOf((MapLayer)baseMapLabelLayers.elementAt(index)) != -1){
				map.removeLayer((MapLayer)baseMapLabelLayers.elementAt(index));
			}


			zoomChangeListener.setActive(true);

			updateMemoryLabel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// hack to link behavior of country layers (usa and non-usa)
		if (index == COUNTRIES_OUT) {
			setLabelVisibility(COUNTRIES_OUT_USA, vis);
		}
	}


	// END METHOD setLabelVisibility

	/**
	 *  Gets the labelVisibility attribute of the WCTViewer object
	 *
	 * @param  index  The new labelVisibility value
	 * @return        The labelVisible value
	 */
	public boolean isLabelVisible(int index) {
		try {
			return ((MapLayer) baseMapLabelLayers.elementAt(index)).isVisible();
		} catch (Exception e) {
			// get any exception
			return false;
		}
	}
	// END METHOD setLabelVisibility


	// Eventually add SimpleShapefileLayer object to easily hold the shading information
	public int getLayerShapeType(int index) {   

		MapLayer ml = ((MapLayer) baseMapLayers.elementAt(index));
		Class geometryClass = ml.getFeatureSource().getSchema().getDefaultGeometry().getType();
		if (Point.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.POINT;
		}
		else if (MultiPoint.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.MULTIPOINT;
		}
		else if (LineString.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.LINE;
		}
		else if (MultiLineString.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.MULTILINE;
		}
		else if (Polygon.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.POLYGON;
		}
		else if (MultiPolygon.class.isAssignableFrom(geometryClass)) {
			return SimpleShapefileLayer.MULTIPOLYGON;
		}
		else {
			return SimpleShapefileLayer.UNKNOWN;
		}

	}

	/**
	 *  Description of the Method
	 *
	 * @param  index  Description of the Parameter
	 */
	public void removeLayer(int index) {
		try {
			map.removeLayer((MapLayer) baseMapLayers.elementAt(index));

			// Force garbage collection
			baseMapLayers.setElementAt(null, index);
			System.gc();
			return;
		} catch (NullPointerException e) {
			return;
		}
	}


	// END METHOD setThemeLineWidth

	/**
	 *  Gets the background color for the map pane.
	 *
	 * @return    The backgroundColor value
	 */
	public Color getBackgroundColor() {
		return wctMapPane.getBackground();
	}


	/**
	 *  Sets the backgroundColor attribute of the WCTViewer object
	 *
	 * @param  c  The new backgroundColor value
	 */
	public void setBackgroundColor(Color c) {
		wctMapPane.setBackground(c);
		wctMapPane.setMagnifierGlass(c);
	}


	/**
	 *  Sets the legend font
	 *
	 * @param  font  The new metaLabelFont value
	 * @throws Exception 
	 */
	public void setLegendFont(Font font) throws Exception {
		radLegendProducer.setFont(font);
		gridSatLegendProducer.setFont(font);
		refreshLegend();
	}


	/**
	 *  Sets the legendFGColor attribute of the WCTViewer object
	 *
	 * @param  fgcolor  The new legendFGColor value
	 * @throws Exception 
	 */
	public void setLegendFGColor(Color fgcolor) throws Exception {
		keyPanel.setForeground(fgcolor);
		radLegendProducer.setForegroundColor(fgcolor);
		gridSatLegendProducer.setForegroundColor(fgcolor);
		refreshLegend();
	}


	/**
	 *  Sets the legendBGColor attribute of the WCTViewer object
	 *
	 * @param  bgcolor  The new legendBGColor value
	 * @throws Exception 
	 */
	public void setLegendBGColor(Color bgcolor) throws Exception {
		//        infoPanel.setBackground(bgcolor);
		keyPanel.setBackground(bgcolor);
		radLegendProducer.setBackgroundColor(bgcolor);
		gridSatLegendProducer.setBackgroundColor(bgcolor);
		refreshLegend();
	}

	/**
	 * Refreshs the Radar (large) or Grid/Satellite (medium sized) legend.  This is needed
	 * after any change to the Radar or Grid/Satellite CategoryLegendImageProducer objects.
	 * @throws Exception
	 */
	public void refreshLegend() throws Exception {
		if (currentDataType == CurrentDataType.RADAR) {
			keyPanel.setLegendImage(radLegendProducer);
		}
		else {
			Image image = gridSatLegendProducer.createMediumLegendImage();
			gridSatelliteLegend.setInsets(new Insets(0, 0, 15, image.getWidth(this)));
			gridSatelliteLegend.setImage(image);
			gridSatelliteLegend.setVisible(isGridSatelliteLegendVisible);
		}
	}
	

	/**
	 *  Gets the legendFGColor attribute of the WCTViewer object
	 *
	 * @return    The legendFGColor value
	 */
	public Color getLegendFGColor() {
		return radLegendProducer.getForegroundColor();
	}


	/**
	 *  Gets the legendBGColor attribute of the WCTViewer object
	 *
	 * @return    The legendBGColor value
	 */
	public Color getLegendBGColor() {
		return radLegendProducer.getBackgroundColor();
	}




	/**
	 *  Sets the useWctCache attribute of the WCTViewer object - 
	 *  if false, Data files will be re-downloaded with each reload,
	 *  regardless of if the file already exists in the temp cache.  This
	 *  is needed for real-time Level-2 files, which may be partial files
	 *  at the time of download.
	 *
	 * @param  useWctCache  The new useNexradCache value
	 */
	public void setUseWctCache(boolean useWctCache) {
		this.useWctCache = useWctCache;
	}


	// END METHOD setAnimateEnable

	/**
	 * Calls the loadNexradFile method and returns an AWT Image.
	 * Uses the current extent of the viewer.
	 *
	 * @param  nexrad_url  Description of the Parameter
	 * @return             The viewerAWTImage value
	 */
	public Image getViewerAWTImage(URL nexrad_url) {
		return (getViewerAWTImage(nexrad_url, true, false));
	}


	// END METHOD getViewerImage

	/**
	 * Calls the loadNexradFile method and returns an AWT Image.
	 *
	 * @param  curExtent   Description of the Parameter
	 * @param  nexrad_url  Description of the Parameter
	 * @return             The viewerAWTImage value
	 */
	public Image getViewerAWTImage(URL nexrad_url, boolean curExtent) {
		return (getViewerAWTImage(nexrad_url, curExtent, false));
	}


	// END METHOD getViewerImage

	/**
	 * Get BufferedImage from a nexrad url (could be local file).
	 * Can create image at current extent (true) or max nexrad extent (false).
	 * Use isAnimation if called from a animation thread
	 *
	 * @param  curExtent    Description of the Parameter
	 * @param  isAnimation  Description of the Parameter
	 * @param  nexrad_url   Description of the Parameter
	 * @return              The viewerAWTImage value
	 */
	public BufferedImage getViewerBufferedImage(URL nexrad_url, boolean curExtent, boolean isAnimation) {


		//        try {
		//            Thread.sleep(400L);
		//        } catch (Exception e) {
		//            e.printStackTrace();
		//        }        



		if (isAnimation) {
			loadAnimationFile(nexrad_url);
		}
		else {
			loadFile(nexrad_url);
		}
		if (!curExtent) {
			//view.setMapExtent(nexradBounds);
		}

		//        try {
		//            Thread.sleep(400L);
		//        } catch (Exception e) {
		//            e.printStackTrace();
		//        }        

		return (getViewerBufferedImage());
	}

	/**
	 * Get BufferedImage from a nexrad url (could be local file).
	 * Can create image at current extent (true) or max nexrad extent (false).
	 * Use isAnimation if called from a animation thread 
	 *
	 * @param  curExtent    Description of the Parameter
	 * @param  isAnimation  Description of the Parameter
	 * @param  nexrad_url   Description of the Parameter
	 * @param  rangeModel   RangeModel from a progress bar -- will update this model during processing
	 * @return              The viewerAWTImage value
	 */
	public BufferedImage getViewerBufferedImage(URL nexrad_url, boolean curExtent, 
			boolean isAnimation, BoundedRangeModel rangeModel) {


		//        try {
		//            Thread.sleep(400L);
		//        } catch (Exception e) {
		//            e.printStackTrace();
		//        }        



		if (isAnimation) {
			loadAnimationFile(nexrad_url, rangeModel);
		}
		else {
			loadFile(nexrad_url, rangeModel);
		}
		if (!curExtent) {
			//view.setMapExtent(nexradBounds);
		}

		//        try {
		//            Thread.sleep(400L);
		//        } catch (Exception e) {
		//            e.printStackTrace();
		//        }        

		return (getViewerBufferedImage());
	}

	// END METHOD getViewerImage

	/**
	 * Get AWT Image from a nexrad url (could be local file).
	 * Can create image at current extent (true) or max nexrad extent (false).
	 * Use isAnimation if called from a animation thread
	 *
	 * @param  curExtent    Description of the Parameter
	 * @param  isAnimation  Description of the Parameter
	 * @param  nexrad_url   Description of the Parameter
	 * @return              The viewerAWTImage value
	 */
	public Image getViewerAWTImage(URL nexrad_url, boolean curExtent, boolean isAnimation) {

		if (isAnimation) {
			loadAnimationFile(nexrad_url);

			//mapPane.repaint(10000, 0, 0, mapPane.getWidth(), mapPane.getHeight());
			//while (mapPane.isPaintingTile()) {
			//   ;
			//}



		}
		else {
			loadFile(nexrad_url);
		}
		if (!curExtent) {
			//view.setMapExtent(nexradBounds);
		}
		return (getViewerAWTImage());
	}


	// END METHOD getViewerImage

	/**
	 * Create a BufferedImage from just the view panel and legend panel (optional).
	 *
	 * @return    The viewerBufferedImage value
	 */
	public BufferedImage getViewerBufferedImage() {

		int exportWidth = mapPanel.getWidth();
		if (legendVisible) {
			exportWidth += infoPanel.getWidth();
		}
		int exportHeight = mapPanel.getHeight();

		if (currentViewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS || currentViewType == CurrentViewType.GOOGLE_EARTH) {
			if (dataSelect != null)     dataSelect.setVisible(false);
			if (radialProps != null)    radialProps.setVisible(false);
			if (gridProps != null) 	    gridProps.setVisible(false);
			if (mapSelect != null) 	    mapSelect.setVisible(false);
			if (viewProperties != null) viewProperties.setVisible(false);
			if (captureAnimate != null) captureAnimate.setVisible(false);
			return ViewerUtilities.getImageWithSWTScreenshot(
					mapPanel.getLocationOnScreen().x, mapPanel.getLocationOnScreen().y, exportWidth, exportHeight);
		}







		BufferedImage buffImage = new BufferedImage(exportWidth, exportHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = buffImage.createGraphics();
		// paint background to legend color
		if (legendVisible) {
			g2.setColor(getLegendBGColor());
			g2.fillRect(0, 0, exportWidth, exportHeight);
		}

		// Draw NOAA logo
		if (renderedLogo != null && isNOAALogoPainted) {
			wctMapPane.getRenderer().addLayer(renderedLogo);
		}


		// Get map pane size
		java.awt.Rectangle bounds = wctMapPane.getWCTZoomableBounds(new java.awt.Rectangle());

		// Paint map pane background
		g2.setColor(wctMapPane.getBackground());
		g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

		// Paint map pane layers
		//            mapPane.getRenderer().paint(g2, bounds, mapPane.getNexradZoom(), true);
		mapPanel.paint(g2);
		//        ((NativeComponent)geBrowser.getWebBrowser().getNativeComponent()).paintComponent(buffImage);


		if (legendVisible) {
			//                g2.translate(mapPane.getWidth(), 0);
			g2.translate(mapPanel.getWidth(), 0);
			infoPanel.paint(g2);
			infoPanel.paint(g2);
			// do twice just to be sure!

			//g2.translate(-1*mapPane.getWidth(), mapPane.getHeight()-70);
			//g2.drawImage(logo, 1, 1, this);
		}

		// Draw NOAA logo
		/*
		 *  if (logo != null) {
		 *  g2.translate(-1*mapPane.getWidth(), mapPane.getHeight()-71);
		 *  g2.drawImage(logo, 1, 1, this);
		 *  g2.drawImage(logo, 1, 1, this); // twice just to be sure!
		 *  System.out.println("YES LOGO YES LOGO YES LOGO");
		 *  }
		 *  else {
		 *  System.out.println("NO LOGO NO LOGO NO LOGO");
		 *  }
		 */
		// Remove NOAA logo
		if (renderedLogo != null && isNOAALogoPainted) {
			wctMapPane.getRenderer().removeLayer(renderedLogo);
		}

		g2.dispose();



		//        buffImage = ScreenShotWithGC.getScreenshot(
		//        		mapPanel.getLocationOnScreen().x, 
		//        		mapPanel.getLocationOnScreen().y,
		//        		mapPanel.getBounds().width, 
		//        		mapPanel.getBounds().height);





		return buffImage;
		//        }
	}


	/**
	 * Get BufferedImage of just map pane from a nexrad url (could be local file).
	 * Can create image at current extent (true) or max nexrad extent (false).
	 * Use isAnimation if called from a animation thread 
	 *
	 * @param  curExtent    Description of the Parameter
	 * @param  isAnimation  Description of the Parameter
	 * @param  url   Description of the Parameter
	 * @param  rangeModel   RangeModel from a progress bar -- will update this model during processing
	 * @return              The viewerAWTImage value
	 */
	public BufferedImage getMapPaneBufferedImage(URL url, boolean curExtent, 
			boolean isAnimation, BoundedRangeModel rangeModel) {

		if (isAnimation) {
			loadAnimationFile(url, rangeModel);
		}
		else {
			loadFile(url, rangeModel);
		}
		if (!curExtent) {
			//view.setMapExtent(nexradBounds);
		}
		return (getMapPaneBufferedImage());
	}



	/**
	 * Create a BufferedImage from just the map pane
	 *
	 * @return    The map pane BufferedImage 
	 */
	public BufferedImage getMapPaneBufferedImage() {

		int exportWidth = wctMapPane.getWidth();
		int exportHeight = wctMapPane.getHeight();

		BufferedImage buffImage = new BufferedImage(exportWidth, exportHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = buffImage.createGraphics();
		// Draw NOAA logo
		if (renderedLogo != null && isNOAALogoPainted) {
			wctMapPane.getRenderer().addLayer(renderedLogo);
		}
		// Get map pane size
		java.awt.Rectangle bounds = wctMapPane.getWCTZoomableBounds(new java.awt.Rectangle());

		// Paint map pane background
		g2.setColor(wctMapPane.getBackground());
		g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

		//        g2.setColor(Color.WHITE);
		//        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

		// Paint map pane layers              
		wctMapPane.getRenderer().paint(g2, bounds, wctMapPane.getZoom(), true);
		//        mapPane.paint(g2);
		// Remove NOAA logo
		if (renderedLogo != null && isNOAALogoPainted) {
			wctMapPane.getRenderer().removeLayer(renderedLogo);
		}
		g2.dispose();

		return buffImage;
	}



	public BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage)img;  
		}   
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable properties = new Hashtable();
		String[] keys = img.getPropertyNames();
		if (keys!=null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}





	/**
	 * Create a AWT Image from just the view panel and the legend panel.
	 *
	 * @return    The viewerAWTImage value
	 */
	public Image getViewerAWTImage() {
		BufferedImage buffImage = getViewerBufferedImage();

		try {
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(buffImage, 0);
			tracker.waitForID(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Image newImage = buffImage.getScaledInstance(buffImage.getWidth(), buffImage.getHeight(), Image.SCALE_DEFAULT);
		buffImage = null;
		System.gc();

		try {
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(newImage, 0);
			tracker.waitForID(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newImage;
	}



	/**
	 * Add image to capture animator
	 *
	 * @param  image  
	 * @param  name   
	 */
	public void addCaptureImage(BufferedImage image, String name) {
		if (captureAnimate == null) {
			captureAnimate = new CaptureAnimator(this);
		}
		captureAnimate.addImage(image, name);
	}


	/**
	 * Get legend visibility
	 *
	 * @return    The legendVisibility value
	 */
	public boolean getLegendVisibility() {
		return legendVisible;
	}


	/**
	 * Set legend visibility
	 *
	 * @param  showLegend  The new legendVisibility value
	 */
	public void setLegendVisibility(boolean showLegend) {
		legendVisible = showLegend;
		if (currentViewType == CurrentViewType.GEOTOOLS 
				|| currentViewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {

			if (showLegend) {
				try {
					mainPanel.remove(infoPanel);
					mainPanel.add(infoPanel, BorderLayout.EAST);
				} catch (Exception e) {}
			}
			else {
				try {
					mainPanel.remove(infoPanel);
				} catch (Exception e) {}
			}
			mainPanel.validate();
		}
		return;
	}


	/**
	 * Get current viewer extent.
	 *
	 * @return    The currentExtent value
	 */
	public java.awt.geom.Rectangle2D.Double getCurrentExtent() {
		return ((java.awt.geom.Rectangle2D.Double) wctMapPane.getVisibleArea());
	}


	// END METHOD getCurrentExtent

	/**
	 * Get current viewer maximum (reset) extent.
	 *
	 * @return    The maxExtent value
	 */
	public java.awt.geom.Rectangle2D.Double getMaxExtent() {
		return ((java.awt.geom.Rectangle2D.Double) wctMapPane.getPreferredArea());
	}


	// END METHOD getCurrentExtent

	/**
	 * Set current viewer extent.
	 *
	 * @param  curExtent  The new currentExtent value
	 */
	public void setCurrentExtent(java.awt.geom.Rectangle2D curExtent) {
		wctMapPane.setVisibleArea(curExtent);
		return;
	}


	// END METHOD setCurrentExtent

	/**
	 * Set current viewer max (reset) extent.
	 *
	 * @param  maxExtent  The new maxExtent value
	 */
	public void setMaxExtent(java.awt.geom.Rectangle2D.Double maxExtent) {
		wctMapPane.setPreferredArea(maxExtent);
		return;
	}


	// END METHOD setCurrentExtent

	/**
	 * Get nexrad extent.
	 *
	 * @return    The nexradExtent value
	 */
	public java.awt.geom.Rectangle2D.Double getNexradExtent() {
		return (nexradBounds);
	}


	// END METHOD getNexradExtent




	/**
	 * Copy the viewer image to system clipboard
	 */
	public void copyViewToClipboard() {
		ClipboardImageTransfer cit = new ClipboardImageTransfer();
		cit.copyImageToClipboard(getViewerAWTImage());
	}

	/**
	 * Capture high quality image and save to CaptureAnimator list
	 */
	public void screenCapture() {
		if (captureAnimate == null) {
			captureAnimate = new CaptureAnimator(this);
		}
		captureAnimate.addImage(getViewerBufferedImage());
		jmiCaptureAnim.setEnabled(true);
	}

	public boolean isNOAALogoPainted() {
		return isNOAALogoPainted;
	}


	/**
	 * If true (default is true), a NOAA logo is painted in the lower left corner of all
	 * captures.  These captures are used in single frames or animations.
	 * @param isNOAALogoPainted
	 */
	public void setNOAALogoPainted(boolean isNOAALogoPainted) {
		this.isNOAALogoPainted = isNOAALogoPainted;
	}

	// END METHOD saveViewToImage

	/**
	 * Set the size of mainPanel and thus the resulting image and WCTViewer.
	 * (Merely accounts for the size of the other panels in the WCTViewer frame)
	 *
	 * @param  width   The new mainPanelSize value
	 * @param  height  The new mainPanelSize value
	 */
	public void setMainPanelSize(int width, int height) {

		setSize(width + 25, height + 128);
	}


	/**
	 * Clear the Alphanumeric Properties frame.
	 * Called when new job number is entered.
	 */
	public void clearAlphaProperties() {
		if (alphaProperties != null) {
			alphaProperties.dispose();
			alphaProperties = null;
		}
	}


//	/**
//	 *  Sets the rasterizer attribute of the WCTViewer object
//	 *
//	 * @param  gridSize  The new rasterizer value
//	 */
//	public void setRasterizer(int gridSize) {
//		setRasterizer(gridSize, false, -999.0f);
//	}


//	/**
//	 *  Sets the rasterizer attribute of the WCTViewer object
//	 *
//	 * @param  gridSize       The new rasterizer value
//	 * @param  isVariableRes  The new rasterizer value
//	 * @param  noData         The new rasterizer value
//	 */
//	public void setRasterizer(int gridSize, boolean isVariableRes, float noData) {
//
//		rasterizer = new WCTRasterizer(gridSize, gridSize, noData);
//		rasterizer.addGeneralProgressListener(this);
//		isRasterVariableRes = isVariableRes;
//	}
//
//	public void setRasterizer(int width, int height, boolean isVariableRes, float noData) {
//		rasterizer = new WCTRasterizer(height, width, noData);
//		rasterizer.addGeneralProgressListener(this);
//		isRasterVariableRes = isVariableRes;
//	}



	public WCTRasterizer getWCTRasterizer() {
		return rasterizer;
	}
	public void setWCTRasterizer(WCTRasterizer rasterizer) {
		this.rasterizer = rasterizer;
	}



	/**
	 *  Gets the viewProperties attribute of the WCTViewer object
	 *
	 * @return    The viewProperties value
	 */
	public ViewProperties getViewProperties() {
		return viewProperties;
	}


	/**
	 * Load an ESRI Shapefile from user PC.
	 */
	public void loadLocalShapefile(URL url, Color localColor) {
		try {

			System.gc();

			statusBar.setText("Loading Local Data:  " + url.getFile());

			// Enlarge the layer vector
			baseMapLayers.setSize(NUM_LAYERS + localThemeCounter + 1);
			baseMapStyleInfo.setSize(NUM_LAYERS + localThemeCounter + 1);
			// Find vector position
			int pos = NUM_LAYERS + localThemeCounter;
			ShapefileDataStore dsLocal = new ShapefileDataStore(url);
			FeatureSource fsLocal = dsLocal.getFeatureSource(dsLocal.getTypeNames()[0]);
			Class geometryClass = fsLocal.getSchema().getDefaultGeometry().getType();
			Style localStyle;
			if (Point.class.isAssignableFrom(geometryClass)
					|| MultiPoint.class.isAssignableFrom(geometryClass)) {
				Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, localColor, localColor, 1);
				Graphic gr = sb.createGraphic(null, mark, null);
				localStyle = sb.createStyle(sb.createPointSymbolizer(gr));
				baseMapStyleInfo.setElementAt(new BaseMapStyleInfo(1, localColor, localColor), pos);
			}
			else if (LineString.class.isAssignableFrom(geometryClass)
					|| MultiLineString.class.isAssignableFrom(geometryClass)) {
				localStyle = sb.createStyle(sb.createLineSymbolizer(localColor, 1));
				baseMapStyleInfo.setElementAt(new BaseMapStyleInfo(1, localColor), pos);
			}
			else {
				// Don't fill any of the polygons
				localStyle = sb.createStyle(sb.createLineSymbolizer(localColor, 1));
				baseMapStyleInfo.setElementAt(new BaseMapStyleInfo(1, localColor), pos);
			}
			baseMapLayers.setElementAt(new DefaultMapLayer(fsLocal, localStyle), pos);
			map.addLayer(pos, (MapLayer) baseMapLayers.elementAt(pos));

			localThemeCounter++;


			String urlString = url.toString();
			String urlFilename = urlString.substring(urlString.lastIndexOf('/')+1, urlString.length());
			mapSelect.addLocalLayerPanel(urlFilename);

			mapSelect.pack();
			mapSelect.setVisible(true);
			mapSelect.setLocation(1, 1);
			mapSelect.setVisible(true);

			statusBar.setText("");

		} catch (NullPointerException npe) {
			System.out.println("CAUGHT NULL POINTER EXCEPTION WHEN LOADING LOCAL SHAPEFILE");
		} catch (OutOfMemoryError merr) {
			// Free up memory
			//sfrLocal = null;
			try {
				baseMapLayers.setElementAt(null, NUM_LAYERS + localThemeCounter);
			} catch (ArrayIndexOutOfBoundsException aerr) {}
			System.gc();
			// Display error message
			//  info.dispose();
			String message = "The shapefile\n" +
			"<html><font color=red>" + url + "</font></html>\n" +
			"exceeds the allocated memory.\n\n" +
			"Please load a smaller shapefile\n" +
			"or remove some locally added layers.";

			JOptionPane.showMessageDialog(this, (Object) message,
					"MEMORY OVERLOAD", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// END catch
	}
	// END METHOD loadLocalShapefile




	/**
	 * Set the animation progress frame currently in use.
	 * @param progressFrame
	 */
	public void setAnimationProgressFrame(Frame progressFrame) {
		this.animationProgressFrame = progressFrame;
	}


	/**
	 *  Description of the Method
	 */
	public void updateMemoryLabel() {

		Runtime r = Runtime.getRuntime();
		double maxMemory = (double)r.maxMemory();
		statusBar.setText("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / maxMemory)*100.0)) + "%");
	}


	/**
	 *  Sets the progressBarToLoading attribute of the WCTViewer object
	 *
	 * @param  loading  The new progressBarToLoading value
	 */
	public void setProgressBarToLoading(boolean loading) {
		if (loading) {
			progress.setString("Loading NEXRAD Data");
			progress.setIndeterminate(true);
			//          progress.validate();
			//          this.setCursor(WAIT_CURSOR);  ASDF
			//          toolPanel.validate();
			//          toolPanel.repaint();
			//          mainPanel.validate();
			//          mainPanel.repaint();
			System.out.println("JUST SET THE PROGRESS BAR TO LOADING");
		}
		else {
			updateMemoryLabel();
		}
	}





	/**
	 *  Description of the Method
	 */
	private void saveFileAs() {



		// Set up File Chooser
		JFileChooser fc;
		if (projectFile == null) {
			fc = new JFileChooser(WCTProperties.getWCTProperty("wct_session_dir"));
		}
		else {
			fc = new JFileChooser(projectFile.getParentFile());
		}

		fc.setAcceptAllFileFilterUsed(true);
		OpenFileFilter wctprojFilter = new OpenFileFilter("wctproj", true, "NOAA Weather and Climate Toolkit Project Files");
		fc.addChoosableFileFilter(wctprojFilter);
		fc.setFileFilter(wctprojFilter);

		int returnVal = fc.showSaveDialog(this);
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			int choice = JOptionPane.YES_OPTION;
			// intialize to YES!
			file = fc.getSelectedFile();

			WCTProperties.setWCTProperty("wct_session_dir", file.toString());

			File extfile = null;
			String fstr = file.toString();

			// Add extension if needed
			if (!fstr.substring((int) fstr.length() - 8, (int) fstr.length()).equals(".wctproj")) {
				extfile = new File(file + ".wctproj");
			}
			else {
				extfile = file;
				file = new File(fstr.substring(0, (int) fstr.length() - 4));
			}

			// Check for existing file
			if (file.exists()) {
				String message = "The NOAA Weather and Climate Toolkit Project File \n" +
				"<html><font color=red>" + file + "</font></html>\n" +
				"already exists.\n\n" +
				"Do you want to proceed and OVERWRITE?";
				choice = JOptionPane.showConfirmDialog(null, (Object) message,
						"OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
				fstr = file.toString();
				file = new File(fstr.substring(0, (int) fstr.length() - 4));
			}
			// END f(file.exists())
			// Check for existing file without extension
			else if (extfile.exists()) {
				String message = "The NOAA Weather and Climate Toolkit Project File \n" +
				"<html><font color=red>" + extfile + "</font></html>\n" +
				"already exists.\n\n" +
				"Do you want to proceed and OVERWRITE?";
				choice = JOptionPane.showConfirmDialog(null, (Object) message,
						"OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			}
			// END else if(extfile.exists())
			if (choice == JOptionPane.YES_OPTION) {
				saveFile(extfile);
			}
			// END if(choice == YES_OPTION)
		}
	}


	/**
	 *  Description of the Method
	 */
	private void saveFile() {
		saveFile(projectFile);
	}


	/**
	 *  Save a project file
	 *
	 * @param  newFile  Description of the Parameter
	 */
	private void saveFile(File newFile) {
		fileString = newFile.getName();
		this.setTitle("NOAA Weather and Climate Toolkit - " + fileString);
		projectFile = newFile;



		try {
			getStatusBar().setProgressText("Saving Project...");

			WctSessionManager.saveWctSession(this, newFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Session Saving Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		try {
			Thread.sleep(100L*5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getStatusBar().setProgressText("Saving Project...  Done.");
		try {
			Thread.sleep(1000L*2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getStatusBar().setProgressText("");

	}


	/**
	 *  Open a project file with a dialog
	 */
	private void openFile() {

		// Set up File Chooser
		JFileChooser fc;
		if (projectFile == null) {
			String wctSessionDir = WCTProperties.getWCTProperty("wct_session_dir");
			fc = new JFileChooser(wctSessionDir);
		}
		else {
			fc = new JFileChooser(projectFile.getParentFile());
		}

		fc.setAcceptAllFileFilterUsed(true);
		OpenFileFilter wctprojFilter = new OpenFileFilter("wctproj", true, "NOAA Weather and Climate Toolkit Project Files");
		fc.addChoosableFileFilter(wctprojFilter);
		fc.setFileFilter(wctprojFilter);

		int returnVal = fc.showOpenDialog(this);
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			int choice = JOptionPane.YES_OPTION;
			// intialize to YES!
			file = fc.getSelectedFile();
			WCTProperties.setWCTProperty("wct_session_dir", file.toString());

			openFile(file);
		}
	}
	/**
	 *  Open a JNX file with a dialog
	 */
	public void openFile(File file) {

		fileString = file.getName();
		projectFile = file;

		// Do lazy creation of mapSelector
		if (mapSelect == null) {
			try {
				mapSelect = new MapSelector(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			zoomChangeListener.setWMSPanel(mapSelect.getWMSPanel());            
		}


		try {
			WctSessionManager.loadWctSession(this, file);
			this.setTitle("NOAA Weather and Climate Toolkit - " + fileString);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Session Open Error", JOptionPane.ERROR_MESSAGE);            
			e.printStackTrace();
		}
	}


	public MapSelector getMapSelector() {
		if (mapSelect == null) {
			try {
				mapSelect = new MapSelector(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mapSelect;
	}

	public DataSelector getDataSelector() {
		if (dataSelect == null) {
			dataSelect = new DataSelector(this);
		}
		return dataSelect;
	}

	public NdmcDroughtMonitorUI getNdmcDroughtMonitorUI() {
		if (droughtMonitor == null) {
			droughtMonitor = new NdmcDroughtMonitorUI(this);
		}
		return droughtMonitor;
	}
	public SpcStormReportsUI getSpcStormReportsUI() {
		if (spcStormReports == null) {
			spcStormReports = new SpcStormReportsUI(this);
		}
		return spcStormReports;
	}	
	public GhcnOrderExportDialog getGhncOrderExportDialog() {
		if (ghcnTool == null) {
			ghcnTool = GhcnOrderExportDialog.getInstance(this);
		}
		return ghcnTool;
	}


	// Window Listener implementation
	/**
	 *  Description of the Method
	 *
	 * @param  args  The command line arguments
	 */

	private void exitProgram() {

		String message = "<html>Closing the Weather and Climate Toolkit... <br><font color=blue> Are you sure? </font></html> ";
		int choice = JOptionPane.showConfirmDialog(this, (Object) message,
				"Exiting Program...", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			System.exit(1);

//			try {
//				if (dataOrganizer != null) {
//					dataOrganizer.closeDb();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(this, "ERROR CLOSING DATA DB");
//			}
			if (geBrowser != null) {
				geBrowser.getWebBrowser().disposeNativePeer();
			}
			if (geExtBrowser != null) {
				geExtBrowser.getWebBrowser().disposeNativePeer();
			}
			if (WCTUiUtils.classExists("org.eclipse.swt.SWT")) {
				NativeInterface.close();
			}
		}

	}


	public void windowClosing(WindowEvent e) {
		exitProgram();  
	}

	public void windowOpened(WindowEvent e) {
	}
	public void windowIconified(WindowEvent e) {
	}
	public void windowDeiconified(WindowEvent e) {
	}
	public void windowDeactivated(WindowEvent e) {
	}
	public void windowClosed(WindowEvent e) {
	}
	public void windowActivated(WindowEvent e) {
	}

	// Component Listener implementation
	/**
	 *
	 */
	public void componentResized(ComponentEvent e) {
		logger.info("COMPONENT RESIZED - TRYING TO RESET RASTERIZER TO: "+wctMapPane.getSize().toString());

		if (rasterizer != null && ( rasterizer.getHeight() != wctMapPane.getHeight() 
				|| rasterizer.getWidth() != wctMapPane.getWidth())) {


			System.out.println("    --- ACTUALLY RESETING RASTERIZER");
			rasterizer.removeGeneralProgressListener(this);

			// THIS CAUSED ERRORS ON LINUX OVER XWIN - DISPLAY MODE WAS 'null'
			//            DisplayMode displayMode = this.getGraphicsConfiguration().getDevice().getDisplayMode();
			//            int width = displayMode.getWidth();
			//            int height = displayMode.getHeight();
			//            if (width > mapPane.getWidth()*RASTER_SIZE_FACTOR || height > mapPane.getHeight()*RASTER_SIZE_FACTOR) {
			//                rasterizer = new WCTRasterizer(height, width);
			//            }
			//            else {
			rasterizer = new WCTRasterizer((int)(wctMapPane.getHeight()*RASTER_SIZE_FACTOR), (int)(wctMapPane.getWidth()*RASTER_SIZE_FACTOR));
			rasterizer.setNoDataValue(Double.NaN);
			
			
			gridDatasetRaster.setHeight((int)(wctMapPane.getHeight()));
			gridDatasetRaster.setWidth((int)(wctMapPane.getWidth()));

			radialDatasetRaster.setHeight((int)(wctMapPane.getHeight()));
			radialDatasetRaster.setWidth((int)(wctMapPane.getWidth()));

			//            }

			rasterizer.addGeneralProgressListener(this);

		}

		//        System.out.println("COMP COUNT: "+mainPanel.getComponentCount());
		if (mainPanel.getComponentCount() > 1) {
			try {
				keyPanel.setLegendImage(radLegendProducer);
			} catch (Exception e1) {
				logger.warning("UNABLE TO RESET LEGEND IMAGE");
				e1.printStackTrace();
			}
		}

		keyPanel.repaint();
		infoPanel.repaint();        
		mainPanel.repaint();




		//      int gridSize = mapPane.getHeight() > mapPane.getWidth() ? mapPane.getHeight() : mapPane.getWidth();
		//      setRasterizer(gridSize, false, -999.0f);

		//      setRasterizer(mapPane.getHeight(), mapPane.getWidth(), false, -999.0f);

		if (zoomChangeListener != null && ! isLoading) {
			zoomChangeListener.zoomChanged(new org.geotools.gui.swing.event.ZoomChangeEvent("COMPONENT RESIZE", null));
		}
	}
	public void componentHidden(ComponentEvent e) {
	}
	public void componentMoved(ComponentEvent e) {
	}
	public void componentShown(ComponentEvent e) {
	}



	/**
	 * Define implementation of NexradInterface
	 */    
	public int getWCTType() {
		return WCTUiInterface.VIEWER;
	}

	public void registerIOSPs() throws InstantiationException {
		WCTIospManager.registerWctIOSPs();
	}




	/**
	 * Sets the view type via the view combo box.  If changed, this should fire an event.
	 * @param viewType
	 * @throws BindException
	 * @throws Exception
	 */
	public void setCurrentViewType(CurrentViewType viewType) throws BindException, Exception {
		this.currentViewType = viewType;

		if (viewType == CurrentViewType.GEOTOOLS) {
			jcomboViewSelector.setSelectedItem(" Standard");
		}
		else if (viewType == CurrentViewType.GOOGLE_EARTH) {
			jcomboViewSelector.setSelectedItem(" Google Earth");
		}
		else if (viewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {
			jcomboViewSelector.setSelectedItem(" Google Earth Split");
		}
		else if (viewType == CurrentViewType.FOUR_MAP_PANE) {
			jcomboViewSelector.setSelectedItem(" Four Pane");
		}
		//        else if (viewType == CurrentViewType.GOOGLE_EARTH) {
		//            jcomboViewSelector.setSelectedItem("");
		//        }

	}


	private void setCurrentViewTypeValue(CurrentViewType viewType) throws BindException, Exception {
		this.currentViewType = viewType;

		if (viewType == CurrentViewType.GEOTOOLS) {
			jcomboViewSelector.setSelectedItem("");
		}
		else if (viewType == CurrentViewType.GOOGLE_EARTH) {
			jcomboViewSelector.setSelectedItem("");
		}

		wctToolBar.setCurrentView(viewType);

		if (viewType == CurrentViewType.GOOGLE_EARTH || viewType == CurrentViewType.NCDC_NCS ||
				viewType == CurrentViewType.NCDC_NIDIS || viewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS ) {

			if (! WCTUiUtils.classExists("org.eclipse.swt.SWT")) {
				throw new Exception("SWT libraries not found - Native Browser not supported on this operating system.");
			}
			
			mapPanel.removeAll();
			if (viewType !=  CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {
				try {
					mainPanel.remove(infoPanel);
				} catch (Exception e) {};
			}


			if (wctImageServer != null) {
				wctImageServer.shutdown();
				wctImageServer = null;
			}
			//            if (wctImageServer == null) {
			wctImageServer = new WCTImageServer(this, WCTImageServer.DEFAULT_PORT);
			//            }

			if (viewType == CurrentViewType.GOOGLE_EARTH) {

				if (geBrowser != null) {
					geBrowser.shutdown();
					geBrowser = null;
				}
				if (geBrowser == null) {
					NativeInterface.open();
					geBrowser = new GoogleEarthBrowser(WCTImageServer.DEFAULT_PORT, ViewController.GOOGLE_EARTH, false, this, true);
					try {
						NativeInterface.runEventPump();
					} catch (Exception e) {
						;
					}
				}
				else {
					geBrowser = new GoogleEarthBrowser(WCTImageServer.DEFAULT_PORT, ViewController.GOOGLE_EARTH, false, this, true);
				}            
				geBrowser.setViewer(this);
				//              geBrowser.refreshView();

				mapPanel.add(geBrowser, BorderLayout.CENTER);

			}
			else if (viewType == CurrentViewType.NCDC_NCS) {
				NativeInterface.open();

				webMapBrowser = new JWebBrowser();
				webMapBrowser.setBarsVisible(false);
				//                webMapBrowser.addWebBrowserListener(new NCSWebBrowserListener(this));



				//                flexBrowser.navigate("http://gis.ncdc.noaa.gov/maps/ncs.map");
				webMapBrowser.navigate("http://beachbumd2.ncdc.noaa.gov/maps/ncs.map");
				//                webMapBrowser.navigate("http://sansari.dev.ncdc.noaa.gov/steve/testwms.html?"+(int)(Math.random()*10000));

				//                webMapBrowser.executeJavascript("dojo.connect(map, \"onLoad\", function(event) { window.location = 'command://'+ encodeURIComponent('ncs-js load complete') });");
				webMapBrowser.executeJavascript("dojo.connect(map, \"onClick\", function(event) { window.location = 'command://'+ encodeURIComponent('ncs-js click'); });");


				try {
					NativeInterface.runEventPump();
				} catch (Exception e) {
					;
				}

				mapPanel.add(webMapBrowser, BorderLayout.CENTER);           
			}
			else if (viewType == CurrentViewType.NCDC_NIDIS) {
				//                NativeInterface.open();

				flexBrowser = new NCDCFlexBrowser(this);

				//                if (webMapBrowser == null) {
				//                    webMapBrowser = new JWebBrowser();
				//                    webMapBrowser.setBarsVisible(false);
				//                }
				//                flexBrowser.navigate("http://www.drought.gov/imageserver/NIDIS_Viewer/southeast/NIDIS_Viewer.html");
				//                webMapBrowser.navigate("http://nidis-d.ncdc.noaa.gov/imageserver/NIDIS/test/nidis-viewer");
				//                flexBrowser.navigate("http://gis.ncdc.noaa.gov/maps/ncs.map");



				//                try {
				//                    NativeInterface.runEventPump();
				//                } catch (Exception e) {
				//                    ;
				//                }

				mapPanel.add(flexBrowser, BorderLayout.CENTER);           
			}
			else if (viewType == CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {

				if (geBrowser != null) {
					geBrowser.shutdown();
					geBrowser = null;
				}
				if (geBrowser == null) {
					NativeInterface.open();
					geBrowser = new GoogleEarthBrowser(WCTImageServer.DEFAULT_PORT, ViewController.GEOTOOLS, true, this, false);
					try {
						NativeInterface.runEventPump();
					} catch (Exception e) {
						;
					}
				}
				else {
					geBrowser = new GoogleEarthBrowser(WCTImageServer.DEFAULT_PORT, ViewController.GEOTOOLS, true, this, false);
				}            
				geBrowser.setViewer(this);

				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, geBrowser, wctMapPane);
				mapPanel.add(splitPane, BorderLayout.CENTER);

				mainPanel.revalidate();
				mainPanel.repaint();

				geBrowser.setMinimumSize(new Dimension(450, 450));

				int mapPaneWidth = this.getWidth()-450-infoPanel.getWidth(); 
				//                System.out.println("wctMapPane: "+mapPaneWidth);
				if (mapPaneWidth < 450) {
					this.setSize(this.getWidth()+450-mapPaneWidth, this.getHeight());
				}

				setLegendVisibility(getLegendVisibility());

			}





			servicesSelectorButton.setEnabled(false);
			//            captureButton.setEnabled(false);
			//            saveImageButton.setEnabled(false);
			//            saveKmzButton.setEnabled(false);
			//            openKmzButton.setEnabled(false);
			//            copyImageButton.setEnabled(false);


			mainPanel.revalidate();

			//            mainPanel.removeAll();
			//            GoogleEarthBrowser ge = new GoogleEarthBrowser();
			//            mainPanel.add(ge.getPanel(), BorderLayout.CENTER);
		} 
		else if (viewType == CurrentViewType.FOUR_MAP_PANE) {
			final FourPanelMapView fpmv = new FourPanelMapView(this);
			mapPanel.removeAll();
			try {
				mainPanel.remove(infoPanel);
			} catch (Exception e) {};
			
			mapPanel.add(fpmv, BorderLayout.CENTER);
			mainPanel.revalidate();
			mainPanel.repaint();
			
			setLegendVisibility(false);

		}
		else {
			if (geBrowser != null) {
				geBrowser.shutdown();
				geBrowser = null;
			}
			if (wctImageServer != null) {
				wctImageServer.shutdown();
				wctImageServer = null;
			}


			servicesSelectorButton.setEnabled(true);
			dataSelectorButton.setEnabled(true);
			mapSelectorButton.setEnabled(true);
			captureButton.setEnabled(true);
			saveImageButton.setEnabled(true);
			saveKmzButton.setEnabled(true);
			openKmzButton.setEnabled(true);
			copyImageButton.setEnabled(true);


			mapPanel.removeAll();
			mapPanel.add(wctMapPane, BorderLayout.CENTER);
			mainPanel.revalidate();
			mainPanel.repaint();

			setCurrentExtent(new Rectangle2D.Double(
					getCurrentExtent().x+0.0001, getCurrentExtent().y+0.0001,
					getCurrentExtent().width, getCurrentExtent().height));
		}

		mapSelect.setCurrentViewType(viewType);

	}

	public CurrentViewType getCurrentViewType() {
		return currentViewType;
	}

	public void refreshCurrentView() {
		if (currentViewType == CurrentViewType.GOOGLE_EARTH) {
			geBrowser.refreshView();
		}
		else if (currentViewType == CurrentViewType.NCDC_NCS) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {


					//            System.out.println("Executing: wmsLayer.refresh();");
					//            webMapBrowser.executeJavascript("wmsLayer.refresh();");

					System.out.println("Executing: wctWmsLayer.refresh();");
					webMapBrowser.executeJavascript("refreshWctWMS();");


				}
			});


		}        
	}

	public GoogleEarthBrowser getGoogleEarthBrowserInternal() {
		return geBrowser;
	}

	public GoogleEarthBrowser getGoogleEarthBrowserExternal() {
		return geExtBrowser;
	}








	/**
	 *  The main program for the WCTViewer class
	 *
	 * @param  args  The command line arguments
	 */
	public static void main(String[] args) {
		final String[] finalArgs = args;

//	    try {
//	    	NativeInterface.initialize();
//	    	NativeInterface.open();
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//	    }

		
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				startApplication(finalArgs);
//			}
//		});
		
//	    try {
//	    	NativeInterface.runEventPump();
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//	    }

	}

	public static void startApplication(String[] args) {
		try {


			String str = "SEVERE";
			
			Logger.getLogger("org.geotools.gp").setLevel(Level.OFF);
			

			Level logLevel = Level.WARNING;
//			org.apache.log4j.Level l4jLevel = org.apache.log4j.Level.WARN;
			if (str.trim().equalsIgnoreCase("SEVERE")) {
				logLevel = Level.SEVERE;
//				l4jLevel = org.apache.log4j.Level.ERROR;
			}
			else if (str.trim().equalsIgnoreCase("WARNING")) {
				logLevel = Level.WARNING;
//				l4jLevel = org.apache.log4j.Level.WARN;
			}
			else if (str.trim().equalsIgnoreCase("INFO")) {
				logLevel = Level.INFO;
//				l4jLevel = org.apache.log4j.Level.INFO;
			}
			else if (str.trim().equalsIgnoreCase("DEBUG")) {
				logLevel = Level.FINE;
//				l4jLevel = org.apache.log4j.Level.INFO;
			}
			logger.setLevel(logLevel);
			Logger.getLogger("gov.noaa.ncdc").setLevel(logLevel);
			Logger.getLogger("net.sf.ehcache").setLevel(logLevel);

			// log4j logging - used with NCJ api
//			BasicConfigurator.configure();
//			org.apache.log4j.Logger.getLogger("ucar").setLevel(l4jLevel);
//			org.apache.log4j.Logger.getLogger("httpclient").setLevel(l4jLevel);
//			org.apache.log4j.Logger.getLogger("org.apache.commons").setLevel(l4jLevel);
//			org.apache.log4j.Logger.getLogger("net.sf.ehcache").setLevel(l4jLevel);




			System.out.println("STARTING");
			System.out.print("REGISTERING IOSP and TypedDataset");
			//          NetcdfFile.registerIOProvider(nina.test.SigmetIOServiceProvider.class);
			//          TypedDatasetFactory.registerFactory(ucar.nc2.constants.FeatureType.RADIAL, nina.test.SigmetDataset.class);
			System.out.println("   ---- Done ");


			System.out.println("WCTViewer: STARTING");
			System.out.println("WCTViewer: ARGS["+args.length+"]="+Arrays.deepToString(args));


			WCTLookAndFeel.configureUI();
			WCTViewer map = new WCTViewer();


			if (args.length == 1 && args[0].endsWith(".wctproj")) {
				try {
					map.openFile(new File(args[0]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (args.length == 1 && args[0].trim().equalsIgnoreCase("Drought-Service")) {
				NdmcDroughtMonitorUI droughtMonitor = map.getNdmcDroughtMonitorUI();
				droughtMonitor.setVisible(true);
				droughtMonitor.setLocation(map.getX()+25, map.getY()+25);
			}
			else if (args.length == 1 && args[0].trim().equalsIgnoreCase("CDR-Impact-Tool")) {
				try {
					CDRImpactToolUI impactTool = CDRImpactToolUI.getInstance(map);
					impactTool.setVisible(true);
					impactTool.setLocation(map.getX()+25, map.getY()+25);
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(map, "Unable to load CDR Impact Tool");
				}
			}
			else if (args.length == 2) {
				DataSelector dataSelect = map.getDataSelector();
				dataSelect.setLocation(map.getX()+25, map.getY()+25);
				dataSelect.setVisible(true);

				try {
					// Read file location
					int fileLocation = Integer.parseInt(args[0].trim());
					String dataType;
					if (fileLocation == 0) {
						dataType = WCTDataSourceDB.NCDC_HAS_FTP;
					}
					else if (fileLocation == 1) {
						dataType = WCTDataSourceDB.CLASS_ORDER;
					}
					else if (fileLocation == 2) {
						dataType = WCTDataSourceDB.LOCAL_DISK;
					}
					else if (fileLocation == 3) {
						dataType = WCTDataSourceDB.URL_DIRECTORY;
					}
					else if (fileLocation == 4) {
						dataType = WCTDataSourceDB.THREDDS;
					}
					else if (fileLocation == 5) {
						dataType = WCTDataSourceDB.SINGLE_FILE;
					}
					else {
						dataType = "UNKNOWN";
					}


					// Set up data selector
					dataSelect.getDataSourcePanel().setDataType(dataType);

					// Read has number or file directory (if local)
					String hasnum = "";
					String classOrder = "";
					String localDirectory = "";
					String customURL = "";
					String threddsCatalogURL = "";
					String singleURL = "";

					DefaultListModel listModel = map.getDataSelector().getListModel();
					if (fileLocation == 0) {
						hasnum = args[1];
						dataSelect.getDataSourcePanel().setDataLocation(dataType, hasnum);
						listModel.clear();
						listModel.addElement("  Press 'Submit' to List Available Data  ------------------------>");
						//                      map.getDataSelector().getDataSelectorPanel().listHASFiles();
					}
					else if (fileLocation == 1) {
						classOrder = args[1];
						dataSelect.getDataSourcePanel().setDataLocation(dataType, classOrder);
						listModel.clear();
						listModel.addElement("  Press 'Submit' to List Available Data  ------------------------>");
						//                      map.getDataSelector().getDataSelectorPanel().listHASFiles();
					}
					else if (fileLocation == 2) {
						localDirectory = args[1];
						dataSelect.getDataSourcePanel().setDataLocation(dataType, localDirectory);
						listModel.clear();
						listModel.addElement("  Press 'Submit' to List Available Data  ------------------------>");
						//                      map.getDataSelector().getDataSelectorPanel().listLocalFiles(new File(localDirectory));
					}
					else if (fileLocation == 3) {

						customURL = args[1];
						// check for encoded '://'
						if (customURL.contains("%3A%2F%2F")) {
							customURL = URLDecoder.decode(customURL, "UTF-8");
						}

						System.out.println("PRELOADING: type="+fileLocation+" ::: "+customURL);

						dataSelect.getDataSourcePanel().setDataLocation(dataType, customURL);                                                
						listModel.clear();
						listModel.addElement("  Press 'Submit' to List Available Data  ------------------------>");
					}
					else if (fileLocation == 4) {
						threddsCatalogURL = args[1];
						// check for encoded '://'
						if (threddsCatalogURL.contains("%3A%2F%2F")) {
							threddsCatalogURL = URLDecoder.decode(threddsCatalogURL, "UTF-8");
						}

						System.out.println("PRELOADING: type="+fileLocation+" ::: "+threddsCatalogURL);

						dataSelect.getDataSourcePanel().setDataLocation(dataType, threddsCatalogURL);                                                
						listModel.clear();
						listModel.addElement("  Press 'Submit' to List Available Data  ------------------------>");
					}
					else if (fileLocation == 5) {
						singleURL = args[1];
						// check for encoded '://'
						if (singleURL.contains("%3A%2F%2F")) {
							singleURL = URLDecoder.decode(singleURL, "UTF-8");
						}

						System.out.println("PRELOADING: type="+fileLocation+" ::: "+singleURL);

						dataSelect.getDataSourcePanel().setDataLocation(dataType, singleURL);                                                
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {

				DataSelector dataSelect = map.getDataSelector();
				dataSelect.setLocation(map.getX()+25, map.getY()+25);
				dataSelect.setVisible(true);

			}

			// Load WCTViewer user properties
			String version = WCTProperties.getWCTProperty("version");
			if (version == null || (version != null && ! version.equals(WCTUiUtils.getVersion()))) {
				NewJNXFeatures newFeatures = new NewJNXFeatures(map, WCTUiUtils.getVersion());
				//                newFeatures.getEditorPane().scrollToReference(WCTUiUtils.getVersion());
				newFeatures.setVisible(true);
				WCTProperties.setWCTProperty("version", WCTUiUtils.getVersion());
			}

			// Delete old files in cache
			WCTTransfer.clearTempDirectory();

			// Set Unidata metadata cache Berkley DB location
			// This has to be different than the default - otherwise the db will be locked when using ToolsUI (and IDV?),
			// causing problems opening NcML files.
			try {
				MetadataManager.setCacheDirectory(
						WCTConstants.getInstance().getCacheLocation()+File.separator+"unidata-bdb",
						10*1024*1024, 50);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			// Exit the Application
			e.printStackTrace();
			System.exit(0);
		}
	}








}

