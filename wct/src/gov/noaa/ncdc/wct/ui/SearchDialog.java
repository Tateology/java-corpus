package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.gis.GoogleGeocoder;
import gov.noaa.ncdc.gis.GoogleGeocoder.GoogleGeocodeResult;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;

public class SearchDialog extends JDialog {
	
	final static String AUTO_FILL_DATA_TYPE = "Data Type";
	final static String AUTO_FILL_FILE_FORMAT = "File Format";
	final static String AUTO_FILL_VARIABLE_NAME = "Variable Name";
	final static String AUTO_FILL_VARIABLE_DESC = "Variable Description";
	final static String AUTO_FILL_VARIABLE_UNITS = "Variable Units";

	final static String SEARCH_ENGINE_GOOGLE = "Google";
	final static String SEARCH_ENGINE_YAHOO = "Yahoo";
	final static String SEARCH_ENGINE_NASA_GCMD = "NASA GCMD";
	
	final private JTextField jtfLocation = new JTextField(50);
	final private JTextField jtfInfoDescription = new JTextField();
	final private JComboBox jcomboInfoAutoFillType = new JComboBox(
			new String[] {
					AUTO_FILL_VARIABLE_NAME,
					AUTO_FILL_VARIABLE_DESC,
					AUTO_FILL_VARIABLE_UNITS,
					AUTO_FILL_DATA_TYPE,
					AUTO_FILL_FILE_FORMAT
			});
	final private JCheckBox jcbInfoAutoFill = new JCheckBox("Auto Fill From Active Data Layer?", true);
	
	
	final private JComboBox jcomboSearchEngine = new JComboBox(
			new String[] { SEARCH_ENGINE_GOOGLE, SEARCH_ENGINE_YAHOO, SEARCH_ENGINE_NASA_GCMD }
		);
	

	private String autoFillDataType = "";
	private String autoFillFileFormat = "";
	private String autoFillVariableName = "";
	private String autoFillVariableDesc = "";
	private String autoFillVariableUnits = "";
	
	
	
	private WCTViewer viewer = null;
	private GoogleGeocoder googleGeocoder = new GoogleGeocoder();
	
    private static SearchDialog dialog = null;

	private SearchDialog(WCTViewer viewer) throws ParserConfigurationException {
		super(viewer, "Search");
		this.viewer = viewer;
		
		createUI();
		pack();
        setLocation(viewer.getX()+25, viewer.getY()+25);
//		setVisible(true);
	}
	
	public static SearchDialog getInstance(WCTViewer viewer) throws ParserConfigurationException {
		if (dialog == null) {
			dialog = new SearchDialog(viewer);
		}
		else {
//			dialog.setVisible(true);
		}
		return dialog;
	}
	
	
	
	private void createUI() {
		this.setLayout(new RiverLayout());
		
		JPanel locationSearchPanel = new JPanel(new RiverLayout());
		
		final JButton jbLocationSubmit = new JButton("Search");
		jbLocationSubmit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				locationSearch(jtfLocation.getText());
			}			
		});
		locationSearchPanel.setBorder(WCTUiUtils.myTitledBorder("Location Search", 2, 4, 2, 4));
		locationSearchPanel.add("left", new JLabel("Enter Address, City, Airport, Landmark, Lat/Lon or Zipcode: "));
		locationSearchPanel.add("br hfill", jtfLocation);
		locationSearchPanel.add(jbLocationSubmit);
		locationSearchPanel.add("br", new JLabel(" "));

		this.add(locationSearchPanel, "hfill");
		


		
		JPanel dataInfoSearchPanel = new JPanel(new RiverLayout());
        

        JButton jbInfoSearch = new JButton("Search");
        jbInfoSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					doInfoSearch();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}       	
        });

        
        jcbInfoAutoFill.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				populateDataInfoSearchText();
			}
		});

        jcomboInfoAutoFillType.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				populateDataInfoSearchText();
			}
		});
        
		
		dataInfoSearchPanel.setBorder(WCTUiUtils.myTitledBorder("Data Information Web Search", 2, 4, 2, 4));
		dataInfoSearchPanel.add("left", new JLabel("Enter Search Term: "));
		dataInfoSearchPanel.add("br hfill", jtfInfoDescription);
		dataInfoSearchPanel.add(jcomboSearchEngine);
		dataInfoSearchPanel.add(jbInfoSearch);
//		dataInfoSearchPanel.add("br", new JLabel(" "));
		dataInfoSearchPanel.add("br left", jcbInfoAutoFill);
		dataInfoSearchPanel.add("tab", new JLabel(" Fill with: "));
		dataInfoSearchPanel.add(jcomboInfoAutoFillType);

		// option to automatically update with current:
		//		datatype, file format, variable name, variable description
		
		this.add(dataInfoSearchPanel, "p hfill");

		
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
	
	
	private void doInfoSearch() throws IOException, URISyntaxException {
		
		URI searchURI = null;
		if (jcomboSearchEngine.getSelectedItem().toString().equals(SEARCH_ENGINE_GOOGLE)) {
			searchURI = new URI("http://www.google.com/search?q="+
					URLEncoder.encode(jtfInfoDescription.getText(), "UTF-8"));
		} 
		else if (jcomboSearchEngine.getSelectedItem().toString().equals(SEARCH_ENGINE_YAHOO)) {
			searchURI = new URI("http://www.yahoo.com/search?p="+
					URLEncoder.encode(jtfInfoDescription.getText(), "UTF-8"));		
		}
		else if (jcomboSearchEngine.getSelectedItem().toString().equals(SEARCH_ENGINE_NASA_GCMD)) {
			searchURI = new URI("http://gcmd.nasa.gov/KeywordSearch/Freetext.do?KeywordPath=&Portal=GCMD&MetadataType=0&Freetext="+
					URLEncoder.encode(jtfInfoDescription.getText(), "UTF-8"));			
		}
		Desktop.getDesktop().browse(searchURI);
	}
	
	private void populateDataInfoSearchText() {
		if (getInfoAutoFillType().equals(AUTO_FILL_DATA_TYPE) && isAutoFillSelected()) {
			setDataInfoSearchText("Scientific Data Type: "+autoFillDataType);			
		}
		else if (getInfoAutoFillType().equals(AUTO_FILL_FILE_FORMAT) && isAutoFillSelected()) {
			setDataInfoSearchText("File Format: "+autoFillFileFormat);			
		}
		else if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_NAME) && isAutoFillSelected()) {
			setDataInfoSearchText(autoFillVariableName);			
		}
		else if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_DESC) && isAutoFillSelected()) {
			setDataInfoSearchText(autoFillVariableDesc);			
		}
		else if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_UNITS) && isAutoFillSelected()) {
			setDataInfoSearchText("Units Abbreviation: "+autoFillVariableUnits);			
		}
	}
	
	
	private void locationSearch(String location) {
		try {
			
			GoogleGeocodeResult ggr = googleGeocoder.locationSearch(location, viewer.getCurrentExtent()).get(0);
	        
	        viewer.showMarkerEditor().addMarker(ggr.getLon(), ggr.getLat(), ggr.getFormattedAddress(), "", "");
	        Rectangle2D curExtent = viewer.getCurrentExtent();
	        viewer.setCurrentExtent(new Rectangle2D.Double(
	        		ggr.getLon()-curExtent.getWidth()/4, 
	        		ggr.getLat()-curExtent.getHeight()/4, 
	        		curExtent.getWidth()/2, 
	        		curExtent.getHeight()/2));

	        if (viewer.getGoogleEarthBrowserInternal() != null) {
	        	viewer.getGoogleEarthBrowserInternal().flyToExtent(viewer.getCurrentExtent());
	        }
	        if (viewer.getGoogleEarthBrowserExternal() != null) {
	        	viewer.getGoogleEarthBrowserExternal().flyToExtent(viewer.getCurrentExtent());
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error using Google Geocoding Service.  " +
					"Please validate input search location and verify internet connection", 
					"Geocoding Service Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	
	
	
	
	
	
	
	
	public void setDataInfoSearchText(String searchText) {
		jtfInfoDescription.setText(searchText);
	}

	public String getInfoAutoFillType() {
		return jcomboInfoAutoFillType.getSelectedItem().toString();
	}
	
	public boolean isAutoFillSelected() {
		return jcbInfoAutoFill.isSelected();
	}

	public void setAutoFillDataType(String autoFillDataType) {
		this.autoFillDataType = autoFillDataType;
		try {
			if (getInfoAutoFillType().equals(AUTO_FILL_DATA_TYPE) && 
					SearchDialog.getInstance(viewer).isAutoFillSelected()) {
				setDataInfoSearchText("Scientific Data Type: "+autoFillDataType);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getAutoFillDataType() {
		return autoFillDataType;
	}

	public void setAutoFillFileFormat(String autoFillFileFormat) {
		this.autoFillFileFormat = autoFillFileFormat;
		try {
			if (getInfoAutoFillType().equals(AUTO_FILL_FILE_FORMAT) && 
					SearchDialog.getInstance(viewer).isAutoFillSelected()) {
				setDataInfoSearchText("File Format: "+autoFillFileFormat);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getAutoFillFileFormat() {
		return autoFillFileFormat;
	}

	public void setAutoFillVariableName(String autoFillVariableName) {
		this.autoFillVariableName = autoFillVariableName;
		try {
			if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_NAME) && 
					SearchDialog.getInstance(viewer).isAutoFillSelected()) {
				setDataInfoSearchText(autoFillVariableName);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getAutoFillVariableName() {
		return autoFillVariableName;
	}

	public void setAutoFillVariableDesc(String autoFillVariableDesc) {
		this.autoFillVariableDesc = autoFillVariableDesc;
		try {
			if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_DESC) && 
					SearchDialog.getInstance(viewer).isAutoFillSelected()) {
				setDataInfoSearchText(autoFillVariableDesc);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getAutoFillVariableDesc() {
		return autoFillVariableDesc;
	}

	public void setAutoFillVariableUnits(String autoFillVariableUnits) {
		this.autoFillVariableUnits = autoFillVariableUnits;
		try {
			if (getInfoAutoFillType().equals(AUTO_FILL_VARIABLE_UNITS) && 
					SearchDialog.getInstance(viewer).isAutoFillSelected()) {
				setDataInfoSearchText("Unit Abbreviation: "+autoFillVariableUnits);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String getAutoFillVariableUnits() {
		return autoFillVariableUnits;
	}
}
