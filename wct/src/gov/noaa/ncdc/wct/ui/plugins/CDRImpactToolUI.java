package gov.noaa.ncdc.wct.ui.plugins;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetUtils;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ncdc.wct.ui.DecompressionDialog;
import gov.noaa.ncdc.wct.ui.WCTNoGridsFoundException;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.animation.WCTAnimator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import opendap.dap.DAP2Exception;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
//import gov.noaa.ncdc.wct.ui.GenericViewerSplash;


// ==========================================================================================================
// CDRImpactToolUI
// ==========================================================================================================
public class CDRImpactToolUI extends JDialog {

	private static final long serialVersionUID = 1L;
	private static CDRImpactToolUI singleton = null;

	private final static String IMPACT_REMOTE_HOME = "http://www1.ncdc.noaa.gov/pub/data/Impact";
	private final static String IMPACT_REMOTE_DATA_HOME = "http://www1.ncdc.noaa.gov/pub/data/Impact/data";

	// private final static String IMPACT_REMOTE_HOME = "http://ftp3.ncdc.noaa.gov/pub/download/sansari";
	// private final static String IMPACT_REMOTE_DATA_HOME = "http://ftp3.ncdc.noaa.gov/pub/download/sansari/data";

	private WCTViewer viewer = null;

	private GridDataset gds = null;
	private Date[] dates;

	public final static Rectangle2D.Double FARALLONES_EXTENT = new Rectangle2D.Double(-123.46, 37.46, .8, .5);

	IMPACTData impactData = new IMPACTData();

	static MPAInformation mpaInfo;
	static MPAInformation current_MPA;

	static SortedSet<MPAInformation> mpaSet = new TreeSet<MPAInformation>();

	ArrayList<String> availableDates_ArrayList = new ArrayList<String>();
	ArrayList<String> filename_ArrayList = new ArrayList<String>();
	ArrayList<String> selectedStatParm_ArrayList = new ArrayList<String>();
	ArrayList<String> statParm_ArrayList = new ArrayList<String>();

	//	Color bgColor = new Color(155, 235, 215);
	//	Color bgColor1 = new Color(140, 212, 194);

	JButton animate_Button = new JButton("Animate");
	JButton display_Button = new JButton("Display");
	JButton analysis_Button = new JButton("Analysis");
	JButton listResults_Button = new JButton("List Results");
	JButton view_Button = new JButton("View");
	JButton description_Button = new JButton("?");

	JComboBox analysis_ListComboBox;
	JComboBox location_ListComboBox;
	JComboBox time2_ListComboBox;
	JComboBox time3_MonthListComboBox;
	JComboBox time_ListComboBox;
	JComboBox variable_ListComboBox;
	JLabel fileInfo_Label = new JLabel(" ");

	JLabel time1_EndDate_Label = new JLabel("     End: ");
	JLabel time1_StartDate_Label = new JLabel("Start: ");

	JLabel time2_EndYear_Label = new JLabel("     Year (End): ");
	JLabel time2_StartYear_Label = new JLabel("     Year (Start): ");

	JLabel time3_EndYear_Label = new JLabel("     Year (End): ");
	JLabel time3_StartYear_Label = new JLabel("     Year (Start): ");

	JLabel time4_EndYear_Label = new JLabel("     Year (End): ");
	JLabel time4_StartYear_Label = new JLabel("     Year (Start): ");

	JList availableDates_List = new JList();
	JList statParm_List = new JList();

	JPanel button_Panel = new JPanel(new GridLayout(6, 1));
	JPanel availableDatesList_Panel = new JPanel(new GridLayout(1, 1));
	JPanel listSelection_Panel = new JPanel();
	JPanel main_Panel = new JPanel();
	JPanel selectionList_Panel = new JPanel(new GridLayout(1, 2));
	JPanel selection_Panel = new JPanel();
	JPanel statParmList_Panel = new JPanel(new GridLayout(1, 1));

	JPanel time1_Panel = new JPanel();
	JPanel time1_sPanel = new JPanel();

	JPanel time2_Panel = new JPanel();
	JPanel time2_sPanel = new JPanel();

	JPanel time3_Panel = new JPanel();
	JPanel time3_sPanel = new JPanel();

	JPanel time4_Panel = new JPanel();
	JPanel time4_sPanel = new JPanel();

	JPanel timeEntry_Panel = new JPanel();

	JSpinner time2_EndYear_Spinner;
	JSpinner time2_StartYear_Spinner;

	JSpinner time3_EndYear_Spinner;
	JSpinner time3_StartYear_Spinner;

	JSpinner time4_EndYear_Spinner;
	JSpinner time4_StartYear_Spinner;

	JXDatePicker time1_EndYear_DatePicker = new JXDatePicker();
	JXDatePicker time1_StartYear_DatePicker = new JXDatePicker();

	ListSelectionModel availableDate_ListSelectionModel;
	ListSelectionModel statParm_ListSelectionModel;

	SimpleDateFormat time1_formatter = new SimpleDateFormat("MM/dd/yyyy");
	SimpleDateFormat time1_formatterYear = new SimpleDateFormat("yyyy");

	SpinnerModel time2_EndYear_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);
	SpinnerModel time2_StartYear_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);

	SpinnerModel time3_EndYear_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);
	SpinnerModel time3_StartYear_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);

	SpinnerModel time4_End_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);
	SpinnerModel time4_Start_SpinnerModel = new SpinnerNumberModel(2000, 1980, 2020, 1);

	String[] analysis_ListString = { " ", "Temporal Trend", "Probability of Occurrence", "Graphs" };

	String[] month_ListString = { 
			"January", "February", "March", 
			"April", "May", "June", 
			"July", "August", "September", 
			"October", "November", "December" 
	};

	String[] season_ListString = { 
			"Dec - Feb (Winter)", 
			"Mar - May (Spring)",
			"Jun - Aug (Summer)", 
			"Sep - Nov (Autumn)" 
	};

	String[] time_ListString = { 
			"Range Selection", 
			"Seasonal", 
			"By Month",
			"By Year" 
	};

	long time1_EndDateMillisecs = 0;
	long time1_StartDateMillisecs = 0;

	int time2_SeasonSelected = 1;

	int time2_SelectedYearEnd = 2000;
	int time2_SelectedYearStart = 2000;

	int time3_SelectedYearEnd = 2000;
	int time3_SelectedYearStart = 2000;

	int time4_SelectedYearEnd = 2000;
	int time4_SelectedYearStart = 2000;

	private CardLayout cardLayout = new CardLayout();

	protected String current_MPA_Name;
	protected String descriptionText;

	
	private String lastLoadedMpaFilename;
	

	// ==========================================================================================================
	// CDRImpactToolUI
	// ==========================================================================================================
	private CDRImpactToolUI(WCTViewer parent) {

		super(parent, "Integrated Marine Protected Area Climate Tools (IMPACT)", false);

		this.viewer = parent;

		setupViewer();

		this.setMinimumSize(new Dimension(650, 500));

		// -----------------------------------------------
		// Read the IMPACT XML file containing:
		//   - Marine Protected Area Name
		//   - Variable List
		//        - Variable Name
		//        - File Size
		//        - File Source address
		// -----------------------------------------------
		read_Impact_XML();

		current_MPA = mpaSet.first();

		descriptionText = current_MPA.getMPA_VariableDescription().get(0);

		createGUI();

		pack();

		buttonsCallbackSetup();

		// -----------------------------------------------
		// Initialize impact_Data variables
		// -----------------------------------------------
		impactData.setVariable(0);
		impactData.setTimePeriod(0);
		impactData.setSeason(0);
		impactData.setMonthYearEnd(2000);
		impactData.setMonthYearStart(2000);
		impactData.setMonthly(0);
		impactData.setSeasonYearEnd(2000);
		impactData.setSeasonYearStart(2000);
		impactData.setYearStart(2000);
		impactData.setYearEnd(2000);
		impactData.setValid(true);

		impactData.setStartDateMillisecs(time1_StartDateMillisecs);
		impactData.setEndDateMillisecs(time1_EndDateMillisecs);
	}


	// ==========================================================================================================
	// getInstance
	// ==========================================================================================================
	public static CDRImpactToolUI getInstance(WCTViewer parent) {

		if (singleton == null)
			singleton = new CDRImpactToolUI(parent);

		return singleton;
	}


	// ==========================================================================================================
	// createGUI
	// ==========================================================================================================
	private void createGUI() {

		analysis_ListComboBox = new JComboBox(analysis_ListString);

		location_ListComboBox = new JComboBox();

		fileInfo_Label.setText(current_MPA.getMPA_VariableSourceSize().get(0));

		Iterator<MPAInformation> itr = mpaSet.iterator();

		while (itr.hasNext()) {

			MPAInformation mpaElement = (MPAInformation) itr.next();

			location_ListComboBox.addItem(mpaElement.getMPA_Name());
		}

		time3_MonthListComboBox = new JComboBox(month_ListString);
		time2_ListComboBox = new JComboBox(season_ListString);
		time_ListComboBox = new JComboBox(time_ListString);
		variable_ListComboBox = new JComboBox();

		loadVariableList();

		// -----------------------------------------------
		// Create Time Panels
		// -----------------------------------------------
		createTimePanels();

		// -----------------------------------------------
		// Create Selection Panel
		// -----------------------------------------------
		selection_Panel.setLayout(new RiverLayout());

		selection_Panel.setBorder(BorderFactory.createTitledBorder("CDR Selection"));

		selection_Panel.add("p left", new JLabel("Marine Protected Area: "));
		selection_Panel.add("tab", location_ListComboBox);

		selection_Panel.add("p left", new JLabel("Variable: "));
		selection_Panel.add("tab", variable_ListComboBox);
		selection_Panel.add("tab", description_Button);
		selection_Panel.add("tab", fileInfo_Label);

		selection_Panel.add("p left", new JLabel("Time Period: "));
		selection_Panel.add("tab", time_ListComboBox);

		selection_Panel.add("p center hfill", timeEntry_Panel);

		selection_Panel.add("p center", listResults_Button);

		// -----------------------------------------------
		// Setup Available Dates List Panel
		// -----------------------------------------------
		availableDatesList_Panel.setBorder(BorderFactory.createTitledBorder("Available Dates"));
		availableDatesList_Panel.add(new JScrollPane(availableDates_List)); 

		availableDate_ListSelectionModel = availableDates_List.getSelectionModel();
		availableDate_ListSelectionModel.addListSelectionListener(new AvailableDateListSelectionHandler());

		// -----------------------------------------------
		// Setup Statistical Parameter List Panel
		// -----------------------------------------------
		statParmList_Panel.setBorder(BorderFactory.createTitledBorder("Statistical Parameters"));
		statParmList_Panel.add(new JScrollPane(statParm_List)); 

		statParm_List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		statParm_ListSelectionModel = statParm_List.getSelectionModel();
		statParm_ListSelectionModel.addListSelectionListener(new StatParmListSelectionHandler());

		// -----------------------------------------------
		// Setup Button Panel
		// -----------------------------------------------
		button_Panel.add(new JLabel(" "));
		// button_Panel.add(display_Button); // Debug
		button_Panel.add(view_Button);
		button_Panel.add(animate_Button);
		button_Panel.add(new JLabel(" "));
		button_Panel.add(analysis_Button);

		// button_Panel.add(new JLabel("Analysis"));
		// button_Panel.add(analysis_ListComboBox);

		animate_Button.setEnabled(false);
		analysis_Button.setEnabled(false);
		view_Button.setEnabled(false);

		// -----------------------------------------------
		// Setup Selection List Panel
		// -----------------------------------------------
		selectionList_Panel.add(statParmList_Panel);
		selectionList_Panel.add(availableDatesList_Panel);

		// -----------------------------------------------
		// Setup List Selection Panel
		// -----------------------------------------------
		listSelection_Panel.setLayout(new RiverLayout());

		listSelection_Panel.setBorder(BorderFactory.createTitledBorder("List Selection"));

		listSelection_Panel.add("left hfill", selectionList_Panel);
		listSelection_Panel.add("right", button_Panel);

		// -----------------------------------------------
		// Setup Main Panel
		// -----------------------------------------------
		URL imageURL = CDRImpactToolUI.class.getResource("/images/CDR.png");
		JLabel label = new JLabel(new ImageIcon(imageURL)); 

//		JLabel label = new JLabel(new ImageIcon("images/CDR.png")); 

		JPanel selectionJPanel = new JPanel(new RiverLayout());
		selectionJPanel.add("p center hfill", selection_Panel);
		selectionJPanel.add("p center hfill", listSelection_Panel);
	
		main_Panel.setLayout(new BorderLayout());
		main_Panel.add(selectionJPanel, BorderLayout.CENTER);
		main_Panel.add(label, BorderLayout.EAST);
		
		// -----------------------------------------------
		// Add Main Panel to this dialog
		// -----------------------------------------------
		this.add(main_Panel);
		this.pack();

		// -----------------------------------------------------------------------------
		// Location List ComboBox Action Listener
		// -----------------------------------------------------------------------------
		location_ListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				current_MPA_Name = (String) location_ListComboBox.getSelectedItem();

				Iterator<MPAInformation> itr = mpaSet.iterator();

				while (itr.hasNext()) {

					MPAInformation mpaElement = (MPAInformation) itr.next();

					if (mpaElement.getMPA_Name().equals(current_MPA_Name)) {
						current_MPA = mpaElement;

						descriptionText = current_MPA.getMPA_VariableDescription().get(0);
					}
				}

				loadVariableList();
			}
		});

		// -----------------------------------------------------------------------------
		// Variable List ComboBox Action Listener
		// -----------------------------------------------------------------------------
		variable_ListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				impactData.setVariable(variable_ListComboBox.getSelectedIndex());

				int selection = variable_ListComboBox.getSelectedIndex();

				fileInfo_Label.setText(current_MPA.getMPA_VariableSourceSize().get(selection));

				descriptionText = current_MPA.getMPA_VariableDescription().get(selection);
			}
		});

		// -----------------------------------------------------------------------------
		// Analysis ComboBox Action Listener
		// -----------------------------------------------------------------------------
		analysis_ListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				// -----------------------------------------------
				// -----------------------------------------------
				int selected = analysis_ListComboBox.getSelectedIndex();

				System.out.println("Selected: (" + selected + ") = " + analysis_ListString[selected]);
			}
		});

		// -----------------------------------------------------------------------------
		// Time ComboBox Action Listener
		// -----------------------------------------------------------------------------
		time_ListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				int selection = time_ListComboBox.getSelectedIndex();

				impactData.setTimePeriod(selection);

				cardLayout.show(timeEntry_Panel, "" + (selection + 1));
			}
		});
	}


	// ==========================================================================================================
	// read_IMPACT_XML
	// ==========================================================================================================
	private void read_Impact_XML() {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				StringBuilder xmlData = null;

				// --------------------------------------------------------
				// startElement
				// --------------------------------------------------------
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					xmlData = new StringBuilder();
				}

				// --------------------------------------------------------
				// endElement
				// --------------------------------------------------------
				public void endElement(String uri, String localName, String qName) throws SAXException {

					String xmlString = xmlData.toString();

					if (qName.equalsIgnoreCase("mpaname")) {
						mpaInfo = new MPAInformation();
						mpaInfo.setMPA_Name(xmlString);

						mpaSet.add(mpaInfo);
					}
					else if (qName.equalsIgnoreCase("name")) {
						mpaInfo.addMPA_VariableName(xmlString);
					}
					else if (qName.equalsIgnoreCase("size")) {
						mpaInfo.addMPA_VariableSourceSize(xmlString);
					}
					else if (qName.equalsIgnoreCase("source")) {
						mpaInfo.addMPA_VariableSource(xmlString);
					}
					else if (qName.equalsIgnoreCase("description")) {
						mpaInfo.addMPA_VariableDescription(xmlString);
					}
				}

				// --------------------------------------------------------
				// characters
				// --------------------------------------------------------
				public void characters(char ch[], int start, int length) throws SAXException {

					xmlData.append(new String(ch, start, length));
				}
			};

			// create cache dir for impact plugin
			File cacheDir = new File(WCTConstants.getInstance().getCacheLocation() +
					File.separator + "resources" + File.separator + "plugins" + File.separator + "impact");

			cacheDir.mkdirs(); 

			URL configURL = new URL(IMPACT_REMOTE_HOME + "/Impact.xml");

			configURL = WCTTransfer.getURL(configURL, cacheDir, true, this, new ArrayList<GeneralProgressListener>());

			saxParser.parse(configURL.toString(), handler);
			//saxParser.parse("Impact.xml", handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ==========================================================================================================
	// setupViewer
	// ==========================================================================================================
	private void setupViewer() {

//		viewer.getMapSelector().getWMSPanel().setSelectedWMS(0, "U.S. Nat. Geo. Soc. Topo (ESRI)");
		viewer.getMapSelector().getWMSPanel().setSelectedWMS(0, "World Imagery (ESRI)");
		viewer.getMapSelector().setGridSatelliteTransparency(50);
	}


	// ==========================================================================================================
	// loadVariableList  
	// ==========================================================================================================
	public void loadVariableList() {  

		// -----------------------------------------------
		// -----------------------------------------------
		DefaultComboBoxModel model = new DefaultComboBoxModel();  

		for (String s : current_MPA.getMPA_VariableName()) {
			model.addElement(s);
		}

		variable_ListComboBox.setModel(model);  

		// -----------------------------------------------
		// -----------------------------------------------
		fileInfo_Label.setText(current_MPA.getMPA_VariableSourceSize().get(0));
	}      


	// ==========================================================================================================
	// ListResults
	// ==========================================================================================================
	private void listResults() throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {

		// -----------------------------------------------
		// Create new list panels
		// -----------------------------------------------
		availableDates_List.clearSelection();
		statParm_List.clearSelection();

		// -----------------------------------------------
		// Open GDS File
		// -----------------------------------------------
		if (gds != null) {
			gds.close();
		}

		openGDSFile();	

		// -----------------------------------------------
		// -----------------------------------------------
		processStatisticalParms(0);

		processAvailableDates(0);

		// -----------------------------------------------
		// Update list panel displays
		// -----------------------------------------------
		availableDatesList_Panel.removeAll();
		availableDatesList_Panel.add(new JScrollPane(availableDates_List));
		availableDatesList_Panel.validate();

		statParmList_Panel.removeAll();
		statParmList_Panel.add(new JScrollPane(statParm_List));
		statParm_List.setSelectionInterval(0, 0);
		statParmList_Panel.validate();
	}


	// ==========================================================================================================
	// openGDSFile
	// ==========================================================================================================
	private void openGDSFile() throws MalformedURLException, IOException {

		String filename = current_MPA.getMPA_VariableSource().get(variable_ListComboBox.getSelectedIndex());

		System.out.println("Filename selected: " + filename);

		File cacheDir = new File(WCTConstants.getInstance().getCacheLocation() + 
				File.separator + "resources" + File.separator + "plugins" + File.separator + "impact");

		URL dataUrl = new URL(IMPACT_REMOTE_DATA_HOME + "/" + filename);

		dataUrl = WCTTransfer.getURL(dataUrl, cacheDir, false, this, new ArrayList<GeneralProgressListener>());
		if (dataUrl == null) {
			return;
		}

		String decompressedURL = dataUrl.toString().substring(0, dataUrl.toString().lastIndexOf("."));

		File decompressedFile = FileUtils.toFile(new URL(decompressedURL));

		if (! decompressedFile.exists()) {
			System.out.println("decompressing: " + FileUtils.toFile(dataUrl) + " to "+ cacheDir);
			dataUrl = DecompressionDialog.decompressAndShowProgress(this, FileUtils.toFile(dataUrl), cacheDir).toURI().toURL();
			if (dataUrl == null) {
				throw new IOException("Data decompression failed.");
			}
		}

		StringBuilder errlog = new StringBuilder();

		try {
			gds = GridDatasetUtils.openGridDataset(dataUrl.toString(), errlog);

		} catch (DAP2Exception e) {
			e.printStackTrace();
		}
	}


	// ==========================================================================================================
	// processStatisticalParms
	// ==========================================================================================================
	private void processStatisticalParms(int gridIndex) throws IOException {

		statParm_ArrayList.clear();

		GridDatatype currentGrid = gds.getGrids().get(gridIndex);

		if (currentGrid.getCoordinateSystem().hasTimeAxis1D()) {
			dates = currentGrid.getCoordinateSystem().getTimeAxis1D().getTimeDates();

			for (Date date : dates) {
				extractSelectedDates(date);
			}
		}

		for (GridDatatype grid : gds.getGrids()) {
			statParm_ArrayList.add(grid.getDescription());
		}

		statParm_List.setListData(statParm_ArrayList.toArray());	
	}


	// ==========================================================================================================
	// processAvailableDates
	// ==========================================================================================================
	private void processAvailableDates(int gridIndex) throws IOException {

		availableDates_ArrayList.clear();

		GridDatatype currentGrid = gds.getGrids().get(gridIndex);

		if (currentGrid.getCoordinateSystem().hasTimeAxis1D()) {
			dates = currentGrid.getCoordinateSystem().getTimeAxis1D().getTimeDates();

			for (Date date : dates) {
				extractSelectedDates(date);
			}
		}

		availableDates_List.setListData(availableDates_ArrayList.toArray());
	}


	// ==========================================================================================================
	// extractAvailableDates
	// ==========================================================================================================
	private void extractSelectedDates(Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		String timeString = sdf.format(date) + " GMT";

		String[] tokens = timeString.split("[ /]+");

		// -----------------
		// tokens[x]
		//
		// 0 - Year
		// 1 - Month
		// 2 - Day
		// 3 - Time
		// -----------------

		// for (int i = 0; i < tokens.length; i++)
		//      System.out.println(tokens[i]);

		String datestr = tokens[1] + "/" + tokens[2] + "/" + tokens[0];

		// System.out.println(datestr);

		Date d = null;

		try {
			d = (Date) formatter.parse(datestr);

		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		long dateMillisecs = d.getTime();

		if (impactData.isValid()) {

			// -------------------
			// Range Selection (0)
			// -------------------
			long startDateMillisecs = impactData.getStartDateMillisecs();
			long endDateMillisecs = impactData.getEndDateMillisecs();

			if (impactData.getTimePeriod() == 0) {
				if ((startDateMillisecs <= dateMillisecs) && (dateMillisecs <= endDateMillisecs)) {
					availableDates_ArrayList.add(timeString);
				}
			}
			// -------------------
			// Seasonal (1)
			// -------------------
			else if (impactData.getTimePeriod() == 1) {

				// -----------------------------------------------
				// -----------------------------------------------
				int season = impactData.getSeason();

				int seasonYearStart = impactData.getSeasonYearStart();
				int seasonYearEnd = impactData.getSeasonYearEnd();

				int month = Integer.parseInt(tokens[1]);
				int year = Integer.parseInt(tokens[0]);

				if ((year == seasonYearStart - 1) && (month == 12)) {
					availableDates_ArrayList.add(timeString);

				} else if (seasonYearStart <= year && year <= seasonYearEnd) {
					if (season == 0) {
						if ((month == 1) || (month == 2)) {
							availableDates_ArrayList.add(timeString);
						}
					} else if ((season == 1) && (month == 3 || month == 4 || month == 5)) {
						availableDates_ArrayList.add(timeString);
					} else if ((season == 2) && (month == 6 || month == 7 || month == 8)) {
						availableDates_ArrayList.add(timeString);
					}
				} else if ((season == 3) && (month == 9 || month == 10 || month == 11)) {
					availableDates_ArrayList.add(timeString);
				}
			}
			// -------------------
			// Monthly (2)
			// -------------------
			else if (impactData.getTimePeriod() == 2) {
				int monthly = impactData.getMonthly();
				int monthlyYearStart = impactData.getMonthYearStart();
				int monthlyYearEnd = impactData.getMonthYearEnd();

				int month = Integer.parseInt(tokens[1]);
				int year = Integer.parseInt(tokens[0]);

				if (monthlyYearStart <= year && year <= monthlyYearEnd) {
					if (monthly == month) {
						availableDates_ArrayList.add(timeString);
					}
				}
			}
			// -------------------
			// Year (3)
			// -------------------
			else if (impactData.getTimePeriod() == 3) {
				int yearStart = impactData.getYearStart();
				int yearEnd = impactData.getYearEnd();

				int year = Integer.parseInt(tokens[0]);

				if (yearStart <= year && year <= yearEnd) {
					availableDates_ArrayList.add(timeString);
				}
			}
		}
	}


	// ==========================================================================================================
	// viewResults
	// ==========================================================================================================
	private void viewResults() throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {

		String filename = current_MPA.getMPA_VariableSource().get(variable_ListComboBox.getSelectedIndex());

		System.out.println("===> Filename selected: " + filename);

		File cacheDir = new File(WCTConstants.getInstance().getCacheLocation() + 
				File.separator + "resources" + File.separator + "plugins" + File.separator + "impact");

		URL dataUrl = new URL(IMPACT_REMOTE_DATA_HOME + "/" + filename);

		
		try {
			dataUrl = WCTTransfer.getURL(
					dataUrl, 
					cacheDir, 
					false, 
					this, 
					new ArrayList<GeneralProgressListener>());

			
		} catch (Exception e) {
			System.out.println("error downloading file..........");
			e.printStackTrace();
		}
		
		String decompressedURL = dataUrl.toString().substring(0, dataUrl.toString().lastIndexOf("."));

		File decompressedFile = FileUtils.toFile(new URL(decompressedURL));

		if (!decompressedFile.exists()) {
			System.out.println("decompressing: " + FileUtils.toFile(dataUrl) + " to "+ cacheDir);
			
			dataUrl = DecompressionDialog.decompressAndShowProgress(this, FileUtils.toFile(dataUrl), cacheDir).toURI().toURL();
			if (dataUrl == null) {
				throw new IOException("Data decompression failed.");
			}
			
		}

		try {
//			viewer.setCurrentExtent(FARALLONES_EXTENT);
			
			boolean resetExtent = false;
			if (lastLoadedMpaFilename == null || ! lastLoadedMpaFilename.equals(filename)) {
//				JOptionPane.showMessageDialog(this, "reset extent");
				resetExtent = true;
				lastLoadedMpaFilename = filename;
			}
			

			if (viewer.getGridDatasetRaster() == null) {
				JOptionPane.showMessageDialog(this, "is null");
			}

			
			
			
//			float zOrder = viewer.getGridSatelliteRenderedGridCoverage().getZOrder();
//			float zOrderLegend = viewer.getGridSatelliteLegend().getZOrder();
			if (viewer.getGridProps() == null) {
//				viewer.getGridSatelliteRenderedGridCoverage().setZOrder(0.000001f);
//				viewer.getGridSatelliteLegend().setZOrder(0.00000012f);
				viewer.loadAnimationFile(dataUrl);
			}
//			JOptionPane.showMessageDialog(this, ""+statParm_List.getSelectedIndex());
			
			viewer.getGridDatasetRaster().setGridIndex(statParm_List.getSelectedIndex());
			viewer.getGridDatasetRaster().setTimeIndex(getSelectedDateIndex());
			viewer.getGridProps().setSelectedGridIndex(statParm_List.getSelectedIndex());
			viewer.getGridProps().setSelectedTimeIndices(getSelectedDateIndices()); 
			
			
			// ************************************
			// As per Steve's email
			// ************************************

			System.out.println("trying to load in impact tool: " + dataUrl);

			
//			 boolean gov.noaa.ncdc.wct.ui.WCTViewer.loadFile(URL dataUrl, boolean clearAlphanumeric, boolean isAlphaBackground, boolean isAnimation, boolean resetExtent)
//
//			 Loads data file into viewer with all options available
//
//			 Parameters:
//			 clearAlphanumeric Description of the Parameter
//			 isAlphaBackground Description of the Parameter
//			 isAnimation Description of the Parameter
//			 resetExtent Reset the view to the bounds of the nexrad site
//			 dataUrl Description of the Parameter
			
//			return (loadFile(dataUrl, true, false, true, false));

			// using isAnimation = true, will prevent the GridDatasetProperties window from showing up
			viewer.loadFile(dataUrl, true, false, true, resetExtent);
			
//			viewer.getGridSatelliteRenderedGridCoverage().setZOrder(zOrder);
//			viewer.getGridSatelliteLegend().setZOrder(zOrderLegend);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// ==========================================================================================================
	// getSelectedDateIndex
	// ==========================================================================================================
	private int getSelectedDateIndex() throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		String selectedDate = availableDates_List.getSelectedValue().toString();

		for (int n = 0; n < dates.length; n++) {
			if (selectedDate.equals(sdf.format(dates[n]) + " GMT")) {
				return n;
			}
		}

		return -1;
	}


	// ==========================================================================================================
	// getSelectedDateIndices
	// ==========================================================================================================
	private int[] getSelectedDateIndices() throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		ArrayList<Integer> selectedIndexList = new ArrayList<Integer>();

		int[] selectedIndices = availableDates_List.getSelectedIndices();

		for (int n = 0; n < dates.length; n++) {
			for (int index : selectedIndices) {
				String selectedDate = availableDates_List.getModel().getElementAt(index).toString();

				if (selectedDate.equals(sdf.format(dates[n]) + " GMT")) {
					selectedIndexList.add(n);
				}
			}
		}

		return ArrayUtils.toPrimitive(selectedIndexList.toArray(new Integer[selectedIndexList.size()]));
	}


	// ==========================================================================================================
	// animate
	// ==========================================================================================================
	private void animate() throws DecodeException, WCTException, IOException, WCTNoGridsFoundException, ParseException {

		if (viewer.getCurrentViewType() == WCTViewer.CurrentViewType.GOOGLE_EARTH || 
				viewer.getCurrentViewType() == WCTViewer.CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {

			JOptionPane.showMessageDialog(this,
					"Animations are not currently supported in the Google Earth view",
					"Animation Error", 
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		try {
			viewResults();
			viewer.getGridProps().setSelectedGridIndex(statParm_List.getSelectedIndex());
			viewer.getGridProps().setSelectedTimeIndices(getSelectedDateIndices()); 

		} catch (Exception e) {
			e.printStackTrace();
		} 

		WCTAnimator animator = new WCTAnimator(viewer);

		animator.setLocationRelativeTo(this);
		animator.setVisible(true);
	}


	// ==========================================================================================================
	// createTimePanels
	// ==========================================================================================================
	private void createTimePanels() {

		// ==================================================================================
		// Create Time Panels
		// ==================================================================================
		createTimePanel1();
		createTimePanel2();
		createTimePanel3();
		createTimePanel4();

		// ==================================================================================
		// Create Time Entry Panel and add time entry subpanels
		// ==================================================================================
		timeEntry_Panel.setLayout(cardLayout);

		timeEntry_Panel.add(time1_Panel, "1");
		timeEntry_Panel.add(time2_Panel, "2");
		timeEntry_Panel.add(time3_Panel, "3");
		timeEntry_Panel.add(time4_Panel, "4");
	}


	// ==========================================================================================================
	// createTimePanel1 - Date Selector
	// ==========================================================================================================
	private void createTimePanel1() {

		time1_Panel.setLayout(new RiverLayout());

		time1_Panel.setBorder(BorderFactory.createTitledBorder("Date: Range Selection"));
		time1_Panel.setVisible(true);

		time1_sPanel.add("p left", time1_StartDate_Label);
		time1_sPanel.add("tab", time1_StartYear_DatePicker);
		time1_sPanel.add("tab", time1_EndDate_Label);
		time1_sPanel.add("tab", time1_EndYear_DatePicker);

		time1_Panel.add("center", time1_sPanel);

		time1_StartYear_DatePicker.setFormats(time1_formatter);
		time1_EndYear_DatePicker.setFormats(time1_formatter);

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			time1_StartYear_DatePicker.setDate(sdf.parse("01/01/2000"));
			time1_EndYear_DatePicker.setDate(sdf.parse("02/01/2000"));

		} catch (Exception e) {
			e.printStackTrace();
		} 

		// -----------------------------------------------------
		// Create callback for pickerStart
		// -----------------------------------------------------
		time1_StartYear_DatePicker.addPropertyChangeListener(new PropertyChangeListener() { 

			public void propertyChange(PropertyChangeEvent arg0) { 
				
				startYearDatePicker();
			} 
		}); 

		// -----------------------------------------------------
		// Create callback for pickerEnd
		// -----------------------------------------------------
		time1_EndYear_DatePicker.addPropertyChangeListener(new PropertyChangeListener() { 

			public void propertyChange(PropertyChangeEvent arg0) { 
				
				endYearDatePicker();
			} 
		}); 
	}


	// ==========================================================================================================
	// createTimePanel2 - Seasonal Selector
	// ==========================================================================================================
	private void createTimePanel2() {

		time2_EndYear_Spinner = new JSpinner(time2_EndYear_SpinnerModel);
		time2_StartYear_Spinner = new JSpinner(time2_StartYear_SpinnerModel);

		time2_Panel.setLayout(new RiverLayout());

		time2_Panel.setBorder(BorderFactory.createTitledBorder("Time: Seasonal"));

		time2_Panel.setVisible(false);

		time2_sPanel.add("p left", new JLabel("By Season: "));
		time2_sPanel.add("tab", time2_ListComboBox);

		// -----------------------------------------------
		// Season ComboBox Action Listener
		// -----------------------------------------------
		time2_ListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				time2_SeasonSelected = time2_ListComboBox.getSelectedIndex();

				impactData.setSeason(time2_SeasonSelected);
			}
		});

		// -----------------------------------------------
		// Start Seasonal Year Spinner
		// -----------------------------------------------
		time2_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_StartYear_Spinner, "#"));

		time2_sPanel.add("tab", time2_StartYear_Label);
		time2_sPanel.add("tab", time2_StartYear_Spinner);

		time2_StartYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time2_SelectedYearStart = (Integer) spinner.getValue();

				impactData.setValid(true);

				if (time2_SelectedYearEnd < time2_SelectedYearStart) {
					time2_StartYear_Label.setForeground(Color.red);
					time2_StartYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time2_EndYear_Label.setForeground(Color.black);
					time2_EndYear_Label.setText("     Year (End): ");

					time2_StartYear_Label.setForeground(Color.black);
					time2_StartYear_Label.setText("     Year (Start): ");

					time2_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_StartYear_Spinner, "#"));
					time2_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_EndYear_Spinner, "#"));

					impactData.setSeasonYearStart(time2_SelectedYearStart);
				}
			}
		});

		// -----------------------------------------------
		// End Seasonal Year Spinner
		// -----------------------------------------------
		time2_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_EndYear_Spinner, "#"));

		time2_sPanel.add("tab", time2_EndYear_Label);
		time2_sPanel.add("tab", time2_EndYear_Spinner);

		time2_EndYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time2_SelectedYearEnd = (Integer) spinner.getValue();

				impactData.setValid(true);

				if (time2_SelectedYearEnd < time2_SelectedYearStart) {
					time2_EndYear_Label.setForeground(Color.red);
					time2_EndYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time2_EndYear_Label.setForeground(Color.black);
					time2_EndYear_Label.setText("     Year (End): ");

					time2_StartYear_Label.setForeground(Color.black);
					time2_StartYear_Label.setText("     Year (Start): ");

					time2_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_StartYear_Spinner, "#"));
					time2_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time2_EndYear_Spinner, "#"));

					impactData.setSeasonYearEnd(time2_SelectedYearEnd);
				}
			}
		});

		time2_Panel.add("center", time2_sPanel);
	}


	// ==========================================================================================================
	// createTimePanel3 - Monthly Time Period Selection
	// ==========================================================================================================
	private void createTimePanel3() {

		time3_StartYear_Spinner = new JSpinner(time3_StartYear_SpinnerModel);
		time3_EndYear_Spinner = new JSpinner(time3_EndYear_SpinnerModel);

		time3_Panel.setLayout(new RiverLayout());

		time3_Panel.setBorder(BorderFactory.createTitledBorder("Time: By Month"));

		time3_Panel.setVisible(false);

		time3_sPanel.add("p left", new JLabel("Month: "));
		time3_sPanel.add("tab", time3_MonthListComboBox);

		// -----------------------------------------------
		// Start Year Spinner
		// -----------------------------------------------
		time3_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_StartYear_Spinner, "#"));

		time3_sPanel.add("tab", time3_StartYear_Label);
		time3_sPanel.add("tab", time3_StartYear_Spinner);

		time3_StartYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time3_SelectedYearStart = (Integer) spinner.getValue();

				impactData.setValid(true);

				if (time3_SelectedYearEnd < time3_SelectedYearStart) {
					time3_StartYear_Label.setForeground(Color.red);
					time3_StartYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time3_EndYear_Label.setForeground(Color.black);
					time3_EndYear_Label.setText("     Year (End): ");

					time3_StartYear_Label.setForeground(Color.black);
					time3_StartYear_Label.setText("     Year (Start): ");

					time3_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_StartYear_Spinner, "#"));
					time3_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_EndYear_Spinner, "#"));

					impactData.setMonthYearStart(time3_SelectedYearStart);
				}
			}
		});

		// -----------------------------------------------
		// End Year Spinner
		// -----------------------------------------------
		time3_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_EndYear_Spinner, "#"));

		time3_sPanel.add("tab", time3_EndYear_Label);
		time3_sPanel.add("tab", time3_EndYear_Spinner);

		time3_EndYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time3_SelectedYearEnd = (Integer) spinner.getValue();

				impactData.setValid(true);

				if (time3_SelectedYearEnd < time3_SelectedYearStart) {
					time3_EndYear_Label.setForeground(Color.red);
					time3_EndYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time3_EndYear_Label.setForeground(Color.black);
					time3_EndYear_Label.setText("     Year (End): ");

					time3_StartYear_Label.setForeground(Color.black);
					time3_StartYear_Label.setText("     Year (Start): ");

					time3_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_StartYear_Spinner, "#"));
					time3_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time3_EndYear_Spinner, "#"));

					impactData.setMonthYearEnd(time3_SelectedYearEnd);
				}
			}
		});

		// -----------------------------------------------
		// Month ComboBox Action Listener
		// -----------------------------------------------
		time3_MonthListComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				impactData.setMonthly(time3_MonthListComboBox.getSelectedIndex() + 1);
			}
		});

		time3_Panel.add("center", time3_sPanel);
	}


	// ==========================================================================================================
	// CreateTimePanel4 - Year Time Period Selection
	// ==========================================================================================================
	private void createTimePanel4() {

		time4_StartYear_Spinner = new JSpinner(time4_Start_SpinnerModel);
		time4_EndYear_Spinner = new JSpinner(time4_End_SpinnerModel);

		time4_Panel.setLayout(new RiverLayout());
		time4_Panel.setBorder(BorderFactory.createTitledBorder("Time: By Year"));
		time4_Panel.setVisible(false);

		// -----------------------------------------------
		// Start Year Spinner
		// -----------------------------------------------
		time4_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_StartYear_Spinner, "#"));

		time4_sPanel.add("tab", time4_StartYear_Label);
		time4_sPanel.add("tab", time4_StartYear_Spinner);

		time4_StartYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time4_SelectedYearStart = (Integer) spinner.getValue();

				impactData.setYearStart((Integer) spinner.getValue());
				impactData.setValid(true);

				if (time4_SelectedYearEnd < time4_SelectedYearStart) {
					time4_StartYear_Label.setForeground(Color.red);
					time4_StartYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time4_EndYear_Label.setForeground(Color.black);
					time4_EndYear_Label.setText("     Year (End): ");

					time4_StartYear_Label.setForeground(Color.black);
					time4_StartYear_Label.setText("     Year (Start): ");

					time4_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_StartYear_Spinner, "#"));
					time4_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_EndYear_Spinner, "#"));

					impactData.setYearEnd(time4_SelectedYearEnd);
				}
			}
		});

		// -----------------------------------------------
		// End Year Spinner
		// -----------------------------------------------
		time4_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_EndYear_Spinner, "#"));

		time4_sPanel.add("tab", time4_EndYear_Label);
		time4_sPanel.add("tab", time4_EndYear_Spinner);

		time4_EndYear_Spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				listResults_Button.setEnabled(true);

				JSpinner spinner = (JSpinner) event.getSource();

				time4_SelectedYearEnd = (Integer) spinner.getValue();

				impactData.setYearEnd((Integer) spinner.getValue());
				impactData.setValid(true);

				if (time4_SelectedYearEnd < time4_SelectedYearStart) {
					time4_EndYear_Label.setForeground(Color.red);
					time4_EndYear_Label.setText("  *** Invalid ***  ");

					listResults_Button.setEnabled(false);

					impactData.setValid(false);
				} else {
					time4_EndYear_Label.setForeground(Color.black);
					time4_EndYear_Label.setText("     Year (End): ");

					time4_StartYear_Label.setForeground(Color.black);
					time4_StartYear_Label.setText("     Year (Start): ");

					time4_StartYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_StartYear_Spinner, "#"));
					time4_EndYear_Spinner.setEditor(new JSpinner.NumberEditor(time4_EndYear_Spinner, "#"));

					impactData.setYearEnd(time4_SelectedYearEnd);
				}
			}
		});

		time4_Panel.add("center", time4_sPanel);
	}


	// ==========================================================================================================
	// buttonsCallbackSetup
	// ==========================================================================================================
	private void buttonsCallbackSetup() {

		// ------------------------------------------------------
		// Create "List Results" Button ActionListener
		// ------------------------------------------------------
		listResults_Button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					foxtrot.Worker.post(new foxtrot.Task() {

						public Object run() throws Exception {

							listResults();

							return "DONE";
						}
					});

				} catch (DecodeException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTNoGridsFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// ------------------------------------------------------
		// Create "View" Button ActionListener
		// ------------------------------------------------------
		view_Button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					foxtrot.Worker.post(new foxtrot.Task() {

						public Object run() throws Exception {

							viewResults();

							return "DONE";
						}
					});

				} catch (DecodeException e) { 
					e.printStackTrace();
				} catch (WCTException e) { 
					e.printStackTrace();
				} catch (IOException e) { 
					e.printStackTrace();
				} catch (WCTNoGridsFoundException e) { 
					e.printStackTrace();
				} catch (Exception e) { 
					e.printStackTrace();
				}
			}
		});

		// ------------------------------------------------------
		// Create "Description" Button ActionListener
		// ------------------------------------------------------
		description_Button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				JEditorPane ep = new JEditorPane("text/html", descriptionText);

				ep.addHyperlinkListener(

						new HyperlinkListener()
						{
							@Override
							public void hyperlinkUpdate(HyperlinkEvent e)
							{
								URI uri = null;
								URL url = e.getURL();

								try {
									uri = new URI(url.toString());

								} catch (URISyntaxException e1) { 						
									e1.printStackTrace();
								}

								if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {

									//System.out.println(url.toString());

									if (Desktop.isDesktopSupported()) {
										Desktop desktop = Desktop.getDesktop();

										try {
											desktop.browse(uri);

										} catch (IOException e2) { 
											e2.printStackTrace();
										}
									} else {
										JOptionPane.showMessageDialog(null,
												"Java is not able to launch links on your computer.",
												"Cannot Launch Link", 
												JOptionPane.WARNING_MESSAGE);
									}
								}
							}
						});

				ep.setEditable(false);

				JOptionPane.showMessageDialog(null, ep);
			}
		});

		// ------------------------------------------------------
		// Create "Display" Button ActionListener
		// ------------------------------------------------------
		display_Button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					foxtrot.Worker.post(new foxtrot.Task() {

						public Object run() throws Exception {

							impactData.displayIMPACTData();

							return "DONE";
						}
					});

				} catch (DecodeException e) { 
					e.printStackTrace();
				} catch (WCTException e) { 
					e.printStackTrace();
				} catch (IOException e) { 
					e.printStackTrace();
				} catch (WCTNoGridsFoundException e) {
					e.printStackTrace();
				} catch (Exception e) { 
					e.printStackTrace();
				}
			}
		});

		// ------------------------------------------------------
		// Create "Animate" Button ActionListener
		// ------------------------------------------------------
		animate_Button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					foxtrot.Worker.post(new foxtrot.Task() {

						public Object run() throws Exception {

							animate();

							return "DONE";
						}
					});

				} catch (DecodeException e) { 
					e.printStackTrace();
				} catch (WCTException e) { 
					e.printStackTrace();
				} catch (IOException e) { 
					e.printStackTrace();
				} catch (WCTNoGridsFoundException e) { 
					e.printStackTrace();
				} catch (Exception e) { 
					e.printStackTrace();
				}
			}
		});
	}


	// ==========================================================================================================
	// startYearDatePicker
	// ==========================================================================================================
	private void startYearDatePicker() {

		time1_StartDate_Label.setText("Start: ");
		time1_StartDate_Label.setForeground(Color.black);

		listResults_Button.setEnabled(true);

		time1_StartDateMillisecs = time1_StartYear_DatePicker.getDate().getTime();

		impactData.setStartDateMillisecs(time1_StartDateMillisecs);
		impactData.setValid(true);

		if (time1_EndDateMillisecs != 0) {
			time1_EndDate_Label.setText("     End: ");
			time1_EndDate_Label.setForeground(Color.black);
		}

		if ((time1_StartDateMillisecs > time1_EndDateMillisecs) && (time1_EndDateMillisecs != 0)) {
			time1_StartDate_Label.setText("** Invalid **");
			time1_StartDate_Label.setForeground(Color.red);

			listResults_Button.setEnabled(false);

			impactData.setValid(false);
		}
	}


	// ==========================================================================================================
	// endYearDatePicker
	// ==========================================================================================================
	private void endYearDatePicker() {

		time1_EndDate_Label.setText("     End: ");
		time1_EndDate_Label.setForeground(Color.black);

		listResults_Button.setEnabled(true);

		time1_EndDateMillisecs = time1_EndYear_DatePicker.getDate().getTime();

		impactData.setEndDateMillisecs(time1_EndDateMillisecs);
		impactData.setValid(true);

		if (time1_StartDateMillisecs != 0) {
			time1_StartDate_Label.setText("Start: ");
			time1_StartDate_Label.setForeground(Color.black);
		}

		if ((time1_StartDateMillisecs > time1_EndDateMillisecs) && (time1_EndDateMillisecs != 0)) {
			time1_EndDate_Label.setText("     ** Invalid **");
			time1_EndDate_Label.setForeground(Color.red);

			listResults_Button.setEnabled(false);

			impactData.setValid(false);
		}
	}


	// ==========================================================================================================
	// StatParmListSelectionHandler
	// ==========================================================================================================
	class StatParmListSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {

			if (!event.getValueIsAdjusting()) {
				// -----------------------------------------------
				// Clear any available dates selections
				// -----------------------------------------------
				availableDates_List.clearSelection();

				animate_Button.setEnabled(false);
				analysis_Button.setEnabled(false);
				view_Button.setEnabled(false);

				try {
					//-------------------------------------------------------------
					// This doesn't work because the first index and
					// last index could be different, even for a single selection
					//-------------------------------------------------------------
					// System.out.println("First Event Index = " + event.getFirstIndex());
					// System.out.println("Last Event Index  = " + event.getLastIndex());

					//---------------------------------------------------
					// This works because the statParm_List is set
					// for SINGLE selection, so only one item will appear
					// when doing a getSelectionIndices
					//---------------------------------------------------
					int selection = 0;

					for (int i : statParm_List.getSelectedIndices()) {
						selection = i;
					}

					processAvailableDates(selection);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	// ==========================================================================================================
	// AvailableDateListSelectionHandler
	// ==========================================================================================================
	class AvailableDateListSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {

			// -----------------------------------------------
			// Populate impactData ArrayLists
			// -----------------------------------------------
			ArrayList<String> arraylist1 = new ArrayList<String>();
			ArrayList<String> arraylist2 = new ArrayList<String>();

			for (Object s : availableDates_List.getSelectedValues()) {
				arraylist1.add((String) s);
			}

			impactData.setAvailableDateList(arraylist1);

			for (Object s : statParm_List.getSelectedValues()) {
				arraylist2.add((String) s);
			}

			impactData.setStatParmList(arraylist2);

			// -----------------------------------------------
			// -----------------------------------------------
			animate_Button.setEnabled(false);
			view_Button.setEnabled(false);

			if (availableDates_List.getSelectedIndices().length > 1) {
				animate_Button.setEnabled(true);
			} else {
				view_Button.setEnabled(true);
			}
		}
	}
}
