package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXHyperlink;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.ma2.StructureMembers.Member;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ui.PointFeatureDatasetViewer;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;
import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.XMLStore;

import com.eteks.jeks.JeksTable;
import com.eteks.jeks.JeksTableModel;

public class PointDataDialog extends JDialog {
	private final static Color[] PLOT_COLORS = new Color[] {
		Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.RED,
		Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.RED,
		Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.RED,
		Color.BLUE, Color.GREEN.darker(), Color.DARK_GRAY, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.RED,
	};

	private WCTViewer viewer = null;
	private static PointDataDialog dialog = null;
	private FeatureDataset fd = null;
	private StationTimeSeriesFeatureCollection stsfc = null;
	private List<Station> stationList = null;
	private ArrayList<String> plottedVariableList = new ArrayList<String>();
	// private String[] currentVariableUnits = null;

	private JComboBox jcomboVariables = new JComboBox(new String[]{});
	private JTabbedPane tabPane = new JTabbedPane();

	private JTextArea textArea = new JTextArea(30, 80);
	private JeksTable jeksTable = new JeksTable() {
		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
			Component cell = super.prepareRenderer(renderer, row, column);
			if (row % 2 == 0 && ! isCellSelected(row, column)) {
				cell.setBackground(new Color(235, 235, 250));
			}
			else if (isCellSelected(row, column)){
				cell.setBackground(getSelectionBackground());
			}
			else {
				cell.setBackground(getBackground());
			}
			return cell;
		}
	};
	private boolean cancel = false;
	private DateFormatter df = new DateFormatter();

	private JComboBox jcomboStationIds = new JComboBox();
	private PointDataDialog(WCTViewer viewer) {
		super(viewer, "Point Timeseries Data");
		this.viewer = viewer;

		// Show tool tips immediately
		ToolTipManager.sharedInstance().setInitialDelay(0);

		createUI();
		pack();
		setSize(new Dimension(getSize().width+300, getSize().height));
		setLocation(viewer.getX()+25, viewer.getY()+25);
	}

	public static PointDataDialog getInstance(WCTViewer viewer) {
		if (dialog == null) {
			dialog = new PointDataDialog(viewer);
		}
		return dialog;
	}



	private void createUI() {
		this.setLayout(new RiverLayout());

		this.add(new JLabel("Select Station: "));
		this.add(jcomboStationIds, "hfill");
		JPanel tablePanel = new JPanel(new RiverLayout());
		JPanel textPanel = new JPanel(new RiverLayout());
		textPanel.add(new JScrollPane(textArea), "br hfill vfill");
		tablePanel.add(new JScrollPane(jeksTable), "br hfill vfill");
		tabPane.addTab("Table", tablePanel);
//		tabPane.addTab("Text", textPanel);
		// tabPane.addTab("Plot", plotPanel);
		// tabPane.setShowCloseButtonOnTab(true);
		this.add(tabPane, "br hfill vfill");
		jcomboStationIds.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				foxtrot.Worker.post(new foxtrot.Job() {
					public Object run() {
						try {
							if (jcomboStationIds.getSelectedItem() != null) {
								printStationData(jcomboStationIds.getSelectedItem().toString().split("\\(")[0].trim());
							}
						} catch (WCTException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return "DONE";
					}
				});
			}
		});
		jeksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (! e.getValueIsAdjusting()) {
					if (jcomboVariables.getSelectedItem() != null) {
						// col '0' is the date/time column 
						if (jeksTable.getSelectedColumn() > 0) {
							jcomboVariables.setSelectedIndex(jeksTable.getSelectedColumn()-1);
						}
					}
				}
			}
		});
		// String[] vars = null;
		//        try {
		//         vars = getStationVariables(jcomboStationIds.getSelectedItem().toString().split(" ")[0]);
		//     } catch (WCTException e1) {
		// e1.printStackTrace();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		//
		// if (vars == null) {
		// JOptionPane.showMessageDialog(this, "Could not list variables for this station", 
		// "Error", JOptionPane.ERROR_MESSAGE);
		// }
		// final JComboBox jcbVariables = new JComboBox(vars);
		final JButton jbPlot = new JButton("New Plot");
		jbPlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					newPlot();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (PlotException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(jbPlot, e.getMessage());
				}
			}
		});
		final JButton jbAddPlot = new JButton("Add to Plot");
		jbAddPlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					addToPlot();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WCTException e) {
					e.printStackTrace();
				} catch (PlotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(jbPlot, e.getMessage());
				}
			}
		});
		// jcomboVariables.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent evt) {
		// try {
		// addPlot(jcomboVariables.getSelectedItem().toString());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// });
		this.add("p", new JLabel("Plot Timeseries:"));
		this.add(jcomboVariables);
		this.add(jbPlot);
		this.add(jbAddPlot);

		JButton jbCopy = new JButton("Copy");
		jbCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TextUtils.getInstance().copyToClipboard(textArea.getText());
			}
		});

		JButton jbPrint = new JButton("Print");
		jbPrint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//                try {
				//                    TextUtils.getInstance().print("Identification Results", textArea.getText());
				//                } catch (JetException e1) {
				//                    e1.printStackTrace();
				//                }
				try {
					TextUtils.getInstance().print(textArea);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton jbSave = new JButton("Save");
		jbSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TextUtils.getInstance().save(getContentPane(), textArea.getText(), "txt", "Text File");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel = true;
			}
		});
		

		JXHyperlink jxlinkLaunchToolsUIView = new JXHyperlink();
		jxlinkLaunchToolsUIView.setText("View in ToolsUI Point Obs Window");
		jxlinkLaunchToolsUIView.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
				
					String prefStore = ucar.util.prefs.XMLStore.makeStandardFilename(".unidata", "NetcdfUI22.xml");
					XMLStore store = ucar.util.prefs.XMLStore.createFromFile(prefStore, null);
					PreferencesExt prefs = store.getPreferences();


					PointFeatureDatasetViewer dv = new PointFeatureDatasetViewer(prefs, new JPanel());
					//    			dv.setDataset((PointObsDataset)tdataset);
					dv.setDataset((FeatureDatasetPoint)fd);


					JDialog dialog = new JDialog(viewer, "Point Obs Panel");
					dialog.add(dv);
					dialog.pack();
					dialog.setVisible(true);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		
		
		 this.add("hfill right", new JPanel());
		 this.add("right", jxlinkLaunchToolsUIView);

		this.add("br center", jbCopy);
		this.add(jbPrint);
		this.add(jbSave);
		this.add(jbCancel);

		

	}
	public void newPlot() throws IOException, WCTException, PlotException {

		Station station = stationList.get(jcomboStationIds.getSelectedIndex());
		// this.currentVariableUnits = getStationUnits(station);
		PlotRequestInfo plotRequestInfo = new PlotRequestInfo();
		plotRequestInfo.setVariables(new String[] { jcomboVariables.getSelectedItem().toString() });
		plotRequestInfo.setUnits(getStationUnits(station, plotRequestInfo.getVariables()));
		PlotPanel plotPanel = new PlotPanel();
		plotPanel.setPlot(stsfc, station, plotRequestInfo);
		if (tabPane.getTabCount() > 1) {
			tabPane.removeTabAt(tabPane.getTabCount()-1);
		}
		tabPane.addTab("Plot", plotPanel);
		tabPane.setSelectedComponent(plotPanel);
	}

	public void addToPlot() throws IOException, WCTException, PlotException {

		PlotRequestInfo plotRequestInfo = new PlotRequestInfo();
		Station station = stationList.get(jcomboStationIds.getSelectedIndex());
		// this.currentVariableUnits = getStationUnits(station);
		if (tabPane.getSelectedIndex() == 0) {
			newPlot();
			return;
		}

		// PlotPanel plotPanel = (PlotPanel)(tabPane.getTabComponentAt(tabPane.getSelectedIndex()));
		PlotPanel plotPanel = (PlotPanel)(tabPane.getSelectedComponent());

		System.out.println(tabPane.getTabCount());
		System.out.println(tabPane.getSelectedIndex());
		System.out.println(plotPanel.getClass());
		System.out.println(plotPanel.getPlotRequestInfo());
		String[] vars = plotPanel.getPlotRequestInfo().getVariables();
		ArrayList<String> varList = new ArrayList<String>(Arrays.asList(vars));
		String newVar = jcomboVariables.getSelectedItem().toString();
		if (! varList.contains(newVar)) {
			varList.add(newVar);
		}
		plotRequestInfo.setVariables(varList.toArray(new String[varList.size()]));
		// plotRequestInfo.setUnits(currentVariableUnits);
		plotRequestInfo.setUnits(getStationUnits(station, plotRequestInfo.getVariables()));
		System.out.println("previous variables to plot: "+Arrays.toString(vars));
		System.out.println(" current variables to plot: "+varList);
		plotPanel.setPlot(stsfc, station, plotRequestInfo);
		if (tabPane.getTabCount() > 1) {
			tabPane.removeTabAt(tabPane.getTabCount()-1);
		}
		tabPane.addTab("Plot", plotPanel);
		tabPane.setSelectedComponent(plotPanel);
	}

	/**
	 * Load a file/URL as a Station dataset.
	 * @param dataURL
	 * @throws IOException
	 * @throws WCTException
	 */
	public void process(URL dataURL) throws IOException, WCTException {
		textArea.setText("");
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setForeground(Color.GRAY);
		textArea.setCaretPosition(0);

		Formatter fmter = new Formatter();
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, dataURL.toString(), WCTUtils.getSharedCancelTask(), fmter);
		if (fd != null && fd.getFeatureType().isPointFeatureType()) {
			
		}
		else {
			System.out.println(fmter.toString());
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
		}
		process(fd);
	}
	
	public void process(FeatureDataset fd) throws IOException, WCTException {
		this.fd = fd;
		
		FeatureDatasetPoint fdp = null;
		if (fd != null && fd.getFeatureType().isPointFeatureType()) {
			fdp = (FeatureDatasetPoint)fd;
		}
		else {
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint'  ");
		}
		
		
		System.out.println("STATIONS: "+fdp.getPointFeatureCollectionList().size());

		List<ucar.nc2.ft.FeatureCollection> pfcList = fdp.getPointFeatureCollectionList();
		this.stsfc = (StationTimeSeriesFeatureCollection)(pfcList.get(0));
		stationList = stsfc.getStations();

		jcomboStationIds.removeAllItems();
		for (Station station : stationList) {
			// StationTimeSeriesFeature sf = stsfc.getStationFeature(station);

			System.out.println("Station: "+station.toString());
			jcomboStationIds.addItem(station.getName() + " ("+station.getDescription()+")");
		}
		// init variables
		jcomboVariables.setModel(new DefaultComboBoxModel(
				getStationVariables(stationList.get(0))));
		// this.currentVariableUnits = getStationUnits(stationList.get(0));
	}
	public String[] getStationVariables(String id) throws WCTException, IOException {
		return getStationVariables(stsfc.getStation(id));
	}
	public String[] getStationVariables(Station station) throws WCTException, IOException {
		if (this.stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		if (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();
			StructureData sdata = pf.getData();
			StructureMembers smembers = sdata.getStructureMembers();
			List<String> nameList = smembers.getMemberNames();
			return nameList.toArray(new String[nameList.size()]);
			
//			String[] varArray = nameList.toArray(new String[nameList.size()]);
//			for (int n=0; n<varArray.length; n++) {
//				varArray[n] = varArray[n].concat(" "+smembers.getMember(n).getDescription());
//			}
//			
//			return varArray;
		}
		else {
			throw new WCTException("No variables found for this station");
		}
	}
	public String[] getStationUnits(String id, String[] vars) throws WCTException, IOException {
		return getStationUnits(stsfc.getStation(id), vars);
	}
	public String[] getStationUnits(Station station, String[] vars) throws WCTException, IOException {
		if (this.stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		if (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();
			StructureData sdata = pf.getData();
			StructureMembers smembers = sdata.getStructureMembers();
			List<Member> memberList = smembers.getMembers();
			System.out.println(memberList);
			System.out.println(smembers.getMemberNames());
			String[] unitsArray = new String[vars.length];
			for (int i=0; i<vars.length; i++) {
				for (int n=0; n<memberList.size(); n++) {
					if (memberList.get(n).getName().equals(vars[i])) {
						unitsArray[i] = memberList.get(n).getUnitsString();
						if (unitsArray[i] == null) {
							unitsArray[i] = "No Units";
						}
					}
				}
			}
			return unitsArray;
		}
		else {
			throw new WCTException("No variable units found for this station");
		}
	}
	public void printStationData(String id) throws WCTException, IOException {
		printStationData(stsfc.getStation(id));
	}
	public void printStationData(Station station) throws WCTException, IOException {
		if (this.stsfc == null) {
			throw new WCTException("No data file has been loaded");
		}
		JeksTableModel jeksTableModel = getTableModel();
		int c = 1;

		cancel = false;
		// for (Station station : stationList) {
		StationTimeSeriesFeature sf = stsfc.getStationFeature(station);

		System.out.println("Station: "+station.toString());
		System.out.println("Location: "+sf.getLatLon());
		textArea.append("## Station: "+station.toString()+" -- "+sf.getLatLon() +" \n");

		int row = 0;
		String[] columnNames = null;
		String[] columnInfo = null;
		boolean firstTime = true;
		StringBuilder sb = new StringBuilder();
		PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
		// iterate through data for each station
		while (pfIter.hasNext()) {
			PointFeature pf = pfIter.next();

			// System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
			StructureData sdata = pf.getData();
			StructureMembers smembers = sdata.getStructureMembers();
			// System.out.println( smembers.getMemberNames().toString() );

			if (firstTime) {
				List<String> nameList = smembers.getMemberNames();
				columnNames = new String[nameList.size()+1];
				columnInfo = new String[nameList.size()+1];
				Class[] colTypes = new Class[nameList.size()];
				//                2010-03-23T12:34:00Z
				 textArea.append("#datetime           ");
				columnNames[0] = "Date/Time";
				columnInfo[0] = "<html>Date/Time<br>Time Zone: UTC</html>";
				for (int n=0; n<nameList.size(); n++) {
					columnNames[n+1] = nameList.get(n);
					Member member = smembers.getMember(n);
					 textArea.append(", "+member.getName());
					columnInfo[n+1] = "<html>"+member.getName()+"<br>"+
					member.getDescription()+"<br>"+
					member.getUnitsString()+"</html>";
					DataType dt = member.getDataType();
					System.out.println(dt);
				}
				 textArea.append("\n");
				// jeksTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Date");
				// Force the header to resize and repaint itself
				// jeksTable.getTableHeader().resizeAndRepaint();

				firstTime = false;
			}

			// if (firstTime) {
//			 textArea.append(pf.getTimeUnit().makeStandardDateString( pf.getObservationTime() ));
			 sb.append(pf.getTimeUnit().makeStandardDateString( pf.getObservationTime() ));
			jeksTableModel.setValueAt(pf.getTimeUnit().makeStandardDateString( pf.getObservationTime() ), row, 0);
			c = 1;
			for (String col : smembers.getMemberNames()) {
				String data = sdata.getScalarObject(col).toString();
				jeksTableModel.setValueAt(data, row, c++);
//				 System.out.print(col+"["+c+"]="+data+" ");
				// ---- textArea.append(","+data + getPad(col, data));
//				 textArea.append(", "+data);
//				 sb.append(", "+data);
			}
//			 textArea.append("\n");
//			 sb.append("\n");
			// System.out.println();

			// }
//			 cancel = true;
			if (cancel) {
				return;
			}

			// textArea.append("\n");


			 formatTextArea();
			 textArea.setForeground(Color.BLACK);

			row++;
			
			if (row % 100 == 0) {
				System.out.println(".... processed "+row+" rows");
			}
		}
		if (columnNames == null) {
			JOptionPane.showMessageDialog(this, "No observations are present for this station: "+station.getName()+" ("+station.getDescription()+")");
			return;
		}
		textArea.append(sb.toString());

		// System.out.println("before tablemodel copy");
		JeksTableModel jeksTableModel2 = new JeksTableModel(row, c, columnNames);
		for (int i=0; i<c; i++) {
			for (int j=0; j<row; j++) {
				jeksTableModel2.setValueAt(jeksTableModel.getValueAt(j, i), j, i);
			}
		}
		// System.out.println("after tablemodel copy");

		jeksTable.setModel(jeksTableModel2);
		jeksTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// System.out.println("after setTableModel");
		autoFitColumns(jeksTable, columnNames);

		// textArea.append("\nDONE PROCESSING: "+station.getName());
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for (int i = 0; i < jeksTable.getColumnCount(); i++) {
			TableColumn col = jeksTable.getColumnModel().getColumn(i);
			tips.setToolTip(col, columnInfo[i]);
			col.setHeaderValue(columnNames[i]);
		}
		jeksTable.getTableHeader().addMouseMotionListener(tips);

	}
	private static void autoFitColumns(JTable jeksTable, String[] columnNames) {

		if (columnNames == null) {
			return;
		}
		
		int rowSampleSize = 6;
		int rows = jeksTable.getRowCount();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)image.getGraphics();
		FontRenderContext fc = g.getFontRenderContext();
		
		for (int c=0; c<jeksTable.getColumnCount(); c++) {
			int maxWidth = -1;
			if (rowSampleSize < rows) {
				rowSampleSize = rows;
			}
			for (int r=0; r<rowSampleSize; r++) {
				int row = (int)(Math.random()*rows);
				System.out.println("checking random row: "+row+" for column "+c);
				try {
					String value = jeksTable.getValueAt(r, c).toString();
					int width = (int) jeksTable.getFont().
					createGlyphVector(fc, value).getVisualBounds().getWidth();
					maxWidth = Math.max(width, maxWidth);
				} catch (Exception e) {
					System.err.println("row: "+row+" of "+rows+" , col: "+c+" of "+jeksTable.getColumnCount()+" :: error: "+e.getMessage());
//					r--;
				}
			}
			int width = (int) jeksTable.getFont().
			createGlyphVector(fc, columnNames[c]).getVisualBounds().getWidth();
			maxWidth = Math.max(width, maxWidth);

			jeksTable.getColumnModel().getColumn(c).setPreferredWidth(maxWidth+15);
		}
	}
	private String getPad(String col, String data) {
		if (col.length() < 10) {
			StringBuilder sb = new StringBuilder();
			sb.append(col);
			for (int n=col.length(); n<10; n++) {
				sb.append(" ");
			}
			col = sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		for (int n=data.length(); n<col.length()+1; n++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	private String getPad(String str, int padToLength) {
		StringBuilder sb = new StringBuilder();
		sb.append(str);
		for (int n=str.length(); n<padToLength; n++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	private void formatTextArea() {
		int[] colLengths = null;
		String[] lines = textArea.getText().split("\n");
		for (int n=0; n<lines.length; n++) {
			if (lines[n].startsWith("##")) {
				continue;
			}
			// System.out.println(lines[n]);

			String[] cols = lines[n].split(",");
			if (colLengths == null) {
				colLengths = new int[cols.length];
			}
			if (colLengths.length != cols.length) {
				colLengths = new int[cols.length];
			}
			for (int i=0; i<cols.length; i++) {
				colLengths[i] = Math.max(colLengths[i], cols[i].length());
			}
		}
		// System.out.println(Arrays.toString(colLengths));

		StringBuilder sb = new StringBuilder();
		for (int n=0; n<lines.length; n++) {
			if (lines[n].startsWith("##")) {
				sb.append(lines[n]).append("\n");
				continue;
			}
			String[] cols = lines[n].split(",");
			for (int i=0; i<cols.length; i++) {
				sb.append(getPad(cols[i], colLengths[i]));
				if (i < cols.length-1) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		textArea.setText(sb.toString());
	}
	private JeksTableModel getTableModel() {
		JeksTableModel jeksTableModel = new JeksTableModel() {
		};
		return jeksTableModel;
	}
	class ColumnHeaderToolTips extends MouseMotionAdapter {
		TableColumn curCol;
		Map<TableColumn, String> tips = new HashMap<TableColumn, String>();
		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null) {
				tips.remove(col);
			} else {
				tips.put(col, tooltip);
			}
		}
		public void mouseMoved(MouseEvent evt) {
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			TableColumn col = null;
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}
	private static class PlotRequestInfo {
		enum PlotType { LINE, BAR, AREA };
		private String[] variables;
		private String[] units;
		private PlotType[] plotTypes;
		private Color[] plotColors;
		public void setVariables(String[] variables) {
			this.variables = variables;
		}
		public String[] getVariables() {
			return variables;
		}
		public void setPlotTypes(PlotType[] plotTypes) {
			this.plotTypes = plotTypes;
		}
		public PlotType[] getPlotTypes() {
			return plotTypes;
		}
		public void setPlotColors(Color[] plotColors) {
			this.plotColors = plotColors;
		}
		public Color[] getPlotColors() {
			return plotColors;
		}
		public void setUnits(String[] units) {
			this.units = units;
		}
		public String[] getUnits() {
			return units;
		}
	}
	class PlotPanel extends JPanel {
		private PlotRequestInfo plotRequestInfo;

		public void setPlot(StationTimeSeriesFeatureCollection stsfc, 
				Station station, PlotRequestInfo plotRequestInfo) 
		throws PlotException, IOException {
			
			
			this.plotRequestInfo = plotRequestInfo;
			ArrayList<String> uniqueUnitsList = new ArrayList<String>();
			for (String u : plotRequestInfo.getUnits()) {
				if (u != null && ! uniqueUnitsList.contains(u)) {
					uniqueUnitsList.add(u);
				}
			}

			String[] variables = plotRequestInfo.getVariables();
			String[] units = plotRequestInfo.getUnits();
			TimeSeriesCollection[] dataset = new TimeSeriesCollection[variables.length];
			for (int n=0; n<dataset.length; n++) {
				dataset[n] = new TimeSeriesCollection(TimeZone.getTimeZone("GMT"));
			}
//			for (int n=0; n<variables.length; n++) {
//				if (units[n].equals(uniqueUnitsList.get(0))) {
//					TimeSeries varSeries = new TimeSeries(variables[n], FixedMillisecond.class);
//
//					String[] plotInfo = addToTimeSeries(
//							station, 
//							variables[n], varSeries);
//
//					dataset[0].addSeries(varSeries);
//				}
//			}
			{
				TimeSeries varSeries = new TimeSeries(variables[0], FixedMillisecond.class);
				String[] plotInfo = addToTimeSeries(
						station, 
						variables[0], varSeries);
				dataset[0].addSeries(varSeries);
			}
			

			String graphTitle = "Plot";
			//    String graphTitle = plotInfo[0];
			String domainTitle = "Date/Time (GMT)";


			JFreeChart chart = ChartFactory.createTimeSeriesChart(graphTitle,// chart title
					domainTitle, // x axis label
					uniqueUnitsList.get(0), // y axis label
					dataset[0], // data
					// PlotOrientation.VERTICAL,
					true, // include legend
					true, // tooltips
					false // urls
			);

			// Set the time zone for the date axis labels
			((DateAxis)chart.getXYPlot().getDomainAxis(0)).setTimeZone(TimeZone.getTimeZone("GMT"));

			// chart.setBackgroundImage(javax.imageio.ImageIO.read(new
			// URL("http://mesohigh/img/noaaseagullbkg.jpg")));

			// NOW DO SOME OPTIONAL CUSTOMIZATION OF THE CHART...
			chart.setBackgroundPaint(Color.white);

			//        LegendTitle legend = (LegendTitle) chart.getLegend();
			//        legend.setDisplaySeriesShapes(true);

			// get a reference to the plot for further customization...
			XYPlot plot = chart.getXYPlot();
//			for (int n=1; n<uniqueUnitsList.size(); n++) {
//
//				for (int i=0; i<variables.length; i++) {
//
//					if (units[i].equals(uniqueUnitsList.get(n))) {
//						TimeSeries varSeries = new TimeSeries(variables[i], FixedMillisecond.class);
//
//						String[] plotInfo = addToTimeSeries(
//								station, 
//								variables[n], varSeries);
//
//						dataset[n].addSeries(varSeries);
//
//						// AXIS 2
//						NumberAxis axis2 = new NumberAxis(uniqueUnitsList.get(n));
//						axis2.setFixedDimension(10.0);
//						axis2.setAutoRangeIncludesZero(false);
//						axis2.setLabelPaint(PLOT_COLORS[n-1]);
//						axis2.setTickLabelPaint(PLOT_COLORS[n-1]);
//						plot.setRangeAxis(n, axis2);
//						plot.setRangeAxisLocation(n, AxisLocation.BOTTOM_OR_LEFT);
//
//						plot.setDataset(n, dataset[n]);
//						plot.mapDatasetToRangeAxis(n, n);
//
//						XYItemRenderer renderer2 = new StandardXYItemRenderer();
//						//        XYBarRenderer renderer2 = new XYBarRenderer();
//						renderer2.setSeriesPaint(0, PLOT_COLORS[n-1]);
//						plot.setRenderer(n, renderer2);
//
//
//					}
//				}

			
			
			for (int n=0; n<uniqueUnitsList.size(); n++) {

				if (n > 0) {
					NumberAxis axis2 = new NumberAxis(uniqueUnitsList.get(n));
					axis2.setFixedDimension(10.0);
					axis2.setAutoRangeIncludesZero(false);
					axis2.setLabelPaint(PLOT_COLORS[n]);
					axis2.setTickLabelPaint(PLOT_COLORS[n]);
					plot.setRangeAxis(n, axis2);
					plot.setRangeAxisLocation(n, AxisLocation.BOTTOM_OR_LEFT);
				}
				
				for (int i=1; i<variables.length; i++) {

					if (units[i].equals(uniqueUnitsList.get(n))) {
						TimeSeries varSeries = new TimeSeries(variables[i], FixedMillisecond.class);

						String[] plotInfo = addToTimeSeries(
								station, 
								variables[i], varSeries);

						dataset[i].addSeries(varSeries);

					}
					plot.setDataset(i, dataset[i]);
					plot.mapDatasetToRangeAxis(i, n);

					XYItemRenderer renderer2 = new StandardXYItemRenderer();
					//        XYBarRenderer renderer2 = new XYBarRenderer();
					renderer2.setSeriesPaint(0, PLOT_COLORS[n]);
					plot.setRenderer(i, renderer2);
					
				}


			}



			ChartPanel chartPanel = new ChartPanel(chart);
			// JPanel plotPanel = new JPanel(new RiverLayout());
			// plotPanel.add(chartPanel, "hfill");
			this.removeAll();
			this.setLayout(new BorderLayout());
			this.add(chartPanel, BorderLayout.CENTER);
		}
		/**
		 * 
		 * @param station
		 * @param variable
		 * @param varSeries
		 * @return [0]=station name and description if available
		 *   [1]=units
		 * @throws IOException
		 */
		public String[] addToTimeSeries(Station station, String variable, TimeSeries varSeries) throws IOException, PlotException {
			// Station station = stsfc.getStation(stationName);
			String[] plotInfo = new String[3];
			plotInfo[0] = station.getName();
			if (station.getDescription() != null &&
					station.getDescription().trim().length() > 0) {
				plotInfo[0] += " ("+station.getDescription()+")";
			}
			StationTimeSeriesFeature sf = stsfc.getStationFeature(station);

			System.out.println("Station: "+station.toString());
			System.out.println("Location: "+sf.getLatLon());
			textArea.append("## Station: "+station.toString()+" -- "+sf.getLatLon() +" \n");

			boolean firstTime = true;
			PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
			// iterate through data for each station
			while (pfIter.hasNext()) {
				PointFeature pf = pfIter.next();

				// System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
				StructureData sdata = pf.getData();
				StructureMembers smembers = sdata.getStructureMembers();
				// System.out.println( smembers.getMemberNames().toString() );
				if (firstTime) {
					plotInfo[1] = smembers.getMember(smembers.getMemberNames().indexOf(variable)).getUnitsString();
					firstTime = false;
				}

				try {
				
					Date date = pf.getObservationTimeAsDate();
					double val = Double.parseDouble(sdata.getScalarObject(variable).toString());
					if (val < -100) {
						val = Double.NaN;
					}
					varSeries.addOrUpdate(new FixedMillisecond(date.getTime()), val);
				
				} catch (NumberFormatException nfe) {
					throw new PlotException("Plot Error: Only numeric fields are supported.");
				}
				
			}
			return plotInfo;
		}
		public PlotRequestInfo getPlotRequestInfo() {
			return plotRequestInfo;
		}
	}
	
	class PlotException extends Exception {
		public PlotException(String msg) {
			super(msg);
		}
	}
}

