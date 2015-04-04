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

package gov.noaa.ncdc.wms;

import gov.noaa.ncdc.common.DataTransferEvent;
import gov.noaa.ncdc.common.DataTransferListener;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.MapSelector;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.gc.GridCoverage;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

/**
 *
 * @author    steve.ansari
 */
public class WMSPanel extends JPanel implements ActionListener {

	private JButton jbBGRefresh;
	private JPanel mapBGPanel;
	private JCheckBox jcbAutoRefresh = new JCheckBox("Auto-Refresh", true);


	private final ArrayList<WMSLayerListItem> layerList = new ArrayList<WMSLayerListItem>();
	private JLabel jlComboDescription = new JLabel(" ", JLabel.CENTER);


	private WCTViewer nexview = null;

	private final DecimalFormat fmt2 = new DecimalFormat("0.00");

//	private final WMSData wms = new WMSData();

	public WMSPanel(WCTViewer nexview) throws ParserConfigurationException, SAXException, IOException, Exception {
		this.nexview = nexview;
		createGUI();
	}

	private void createGUI() throws ParserConfigurationException, SAXException, IOException, Exception {

//		WMSLayerList wmsLayer = layerList

		try {

			ArrayList<String> names = WMSConfigManager.getInstance().getWmsNames();
//			JComboBox jcomboBackground = new JComboBox(names.toArray());

			ArrayList<WMSRequestInfo> wmsList = WMSConfigManager.getInstance().getWmsList();
			String[] descArray = new String[wmsList.size()];
			for (int n=0; n<wmsList.size(); n++) {

				String[] indexArray = wmsList.get(n).getIndex().split(",");
				for (int i=0; i<indexArray.length; i++) {
					int index = Integer.parseInt(indexArray[i]);

					WMSLayerListItem wmsLayerListItem = null;
					if (index < layerList.size()) {
						wmsLayerListItem = layerList.get(index);
					}

					if (wmsLayerListItem == null) {
						wmsLayerListItem = new WMSLayerListItem();
						((DefaultComboBoxModel)wmsLayerListItem.getJcomboBackground().getModel()).insertElementAt("None", 0);
						wmsLayerListItem.getComboDescriptions().add("");
						layerList.add(index, wmsLayerListItem);
					}
//					((DefaultComboBoxModel)wmsLayerListItem.getJcomboBackground().getModel()).insertElementAt(wmsList.get(n).getName(), 1);
					((DefaultComboBoxModel)wmsLayerListItem.getJcomboBackground().getModel()).addElement(wmsList.get(n).getName());
					wmsLayerListItem.getComboDescriptions().add(wmsList.get(n).getInfo());


					if (wmsList.get(n).getInfo() != null) {
						descArray[n] = wmsList.get(n).getInfo();
					}
					else {
						descArray[n] = "";
					}       
				}
			}
//			comboDescriptions[0] = descArray;

//			jcomboBackground[0] = new JComboBox(new Object[] {
//			"None", 
//			"Geog. Network Streets",
//			"NASA Blue Marble",
//			"ESRI Global Map",
//			"DEMIS Global Map",
//			"Shaded Relief", 
//			"Land Cover",
//			"Topo Map", 
//			"Aerial Photo",
//			"Urban Area",
//			"Hi-Res Aerial", // could be replaced with Urban Area in the future?
//			"LANDSAT7",
//			"NASA Earth At Night",
//			"NATL RT COMP"
//			});
//			comboDescriptions[0] = new String[] {
//			" ", 
//			"US Streets, Roads, Cities, etc... from Geography Network Data",
//			"NASA Blue Marble served from DEMIS",
//			"Global Boundaries, Roads, Rivers, etc... from ESRI",
//			"Global Elevation, Relief, Boundaries, Roads, etc... from DEMIS",
//			"Shaded Relief from USGS", 
//			"Land Cover from USGS",
//			"Topo Map from TerraServer", 
//			"Aerial Photo from TerraServer",
//			"High Resolution Aerial Photos from TerraServer",
//			"High Resolution Aerial Photos from USGS",
//			"LANDSAT7 Images from USGS",
//			"Earth At Night Satellite Imagery from NASA",
//			"Real-Time National Reflectivity Composite from ISU"
//			};

//			jcomboBackground[1] = new JComboBox(new Object[] {
//			"None", 
//			"Reference",
//			"NATL RT COMP"
//			});
//			comboDescriptions[1] = new String[] {
//			" ", 
//			"Standard Political Data from USGS",
//			"Real-Time National Reflectivity Composite from ISU"
//			};
//			layerSelector[0] = new WMSLayerSelector[jcomboBackground[0].getItemCount()];
//			layerSelector[1] = new WMSLayerSelector[jcomboBackground[1].getItemCount()];



//			for (int n=0; n<jbBackground.length; n++) {
//			jbBackground[n] = new JButton("Layers");
//			jbBackground[n].addActionListener(this);
//			}

//			for (int n=0; n<jcbBWBackground.length; n++) {
//			jcbBWBackground[n] = new JCheckBox("B/W");
//			}


			jbBGRefresh = new JButton("Refresh");
			jbBGRefresh.addActionListener(this);
			TitledBorder transTitle = BorderFactory.createTitledBorder("0% < Radar Transparency > 100%");
			transTitle.setTitleJustification(TitledBorder.CENTER);

			mapBGPanel = new JPanel();
			mapBGPanel.setLayout(new RiverLayout());


//            mapBGPanel.add(new JLabel(""), "br left");
//            mapBGPanel.add(new JLabel("Layer"), "hfill");            
//            mapBGPanel.add(new JLabel("B/W"));

			for (int n=0; n<layerList.size(); n++) {

				if (layerList.get(n) != null) {
					// progress bars
					JProgressBar wmsProgressBar = layerList.get(n).getWmsProgressBar();
					JComboBox jcomboBackground = layerList.get(n).getJcomboBackground();

					wmsProgressBar.setPreferredSize(new Dimension(140, 25));
					wmsProgressBar.setStringPainted(true);
					wmsProgressBar.setString(" Background "+(n+1));
					jcomboBackground.addActionListener(this);
					jcomboBackground.setSelectedIndex(0);

					JCheckBox jcbBWBackground = new JCheckBox("B/W");
					JButton jbEdit = new JButton("Edit");
					jbEdit.addActionListener(new ActionListener() {
//                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                        }
					});

					layerList.get(n).setJbBackground(jbEdit);
					layerList.get(n).setJcbBWBackground(jcbBWBackground);
					layerList.get(n).setWmsProgressBar(wmsProgressBar);

					mapBGPanel.add(wmsProgressBar, "br left");
					mapBGPanel.add(jcomboBackground, "hfill");            
					mapBGPanel.add(jcbBWBackground);
//					mapBGPanel.add(jbEdit);

				}

			}
//			jbBackground[0].setEnabled(false); // no layer choices for most background images index = 0


			mapBGPanel.add(jlComboDescription, "br center");

			JButton showDefaultButton = new JButton("Default");
			showDefaultButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MapSelector mapSelect = nexview.getMapSelector();
					for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
						mapSelect.setLayerVisibility(n, false);
						mapSelect.setLabelVisibility(n, false);
					}                
					mapSelect.setLayerVisibility(WCTViewer.COUNTRIES, true);
					mapSelect.setLayerVisibility(WCTViewer.STATES, true);
					mapSelect.setLayerVisibility(WCTViewer.COUNTRIES_OUT, true);
					mapSelect.setLayerVisibility(WCTViewer.STATES_OUT, true);
					mapSelect.setLayerVisibility(WCTViewer.HWY_INT, true);

				}
			});

			JButton hideOverlaysButton = new JButton("Hide");
			hideOverlaysButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MapSelector mapSelect = nexview.getMapSelector();
					for (int n=0; n<WCTViewer.NUM_LAYERS; n++) {
						mapSelect.setLayerVisibility(n, false);
						mapSelect.setLabelVisibility(n, false);
					}                
				}
			});

			
			
			JButton testGMaps = new JButton("Test Google Map Overlay");
			testGMaps.addActionListener(new ActionListener() {
//				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						testGoogle();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			
			
			
			
			mapBGPanel.add(jbBGRefresh, "br left hfill");
			mapBGPanel.add(jcbAutoRefresh, "left hfill");
			
//			mapBGPanel.add(testGMaps, "left hfill");
			
			mapBGPanel.add(new JLabel(""), "left hfill");

			JPanel overlayPanel = new JPanel();
			overlayPanel.setBorder(WCTUiUtils.myTitledBorder("Overlays", 0, 3, 0, 3));
			overlayPanel.add(showDefaultButton);
			overlayPanel.add(hideOverlaysButton);
			mapBGPanel.add(overlayPanel, "right");

//			mapBGPanel.add(showDefaultButton, "right");
//			mapBGPanel.add(hideOverlaysButton, "right");



			this.setLayout(new BorderLayout());
			this.add(mapBGPanel, "North");
			this.add(new JPanel(), "South");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshWMS() {
		actionPerformed(new ActionEvent(jbBGRefresh, 0, "REFRESH WMS"));
	}

	public String getSelectedWMS(int wmsIndex) {
		return layerList.get(wmsIndex).getJcomboBackground().getSelectedItem().toString();
//		return jcomboBackground[wmsIndex].getSelectedItem().toString();
	}

	public void setSelectedWMS(int wmsIndex, String wmsName) {
//		jcomboBackground[wmsIndex].setSelectedItem(wmsName);
		layerList.get(wmsIndex).getJcomboBackground().setSelectedItem(wmsName);
	}


	public String getSelectedWMSLayers(int wmsIndex) {
		try {
//			return layerSelector[wmsIndex][jcomboBackground[wmsIndex].getSelectedIndex()].getSelectedLayersString();
			return null;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @param wmsLayers   Input comma delimited list of layer names
	 */
	public void setSelectedWMSLayers(int wmsIndex, String wmsLayers, String title, String name) {
		try {
//			if (layerSelector[wmsIndex][jcomboBackground[wmsIndex].getSelectedIndex()] == null) {
//			layerSelector[wmsIndex][jcomboBackground[wmsIndex].getSelectedIndex()] = 
//			new WMSLayerSelector(title + " Layers", 
//			WMSConfigManager.getInstance().getWmsRequestInfo(name).getCapabilitiesURL());
//			}
//			layerSelector[wmsIndex][jcomboBackground[wmsIndex].getSelectedIndex()].setSelectedLayers(wmsLayers);

			throw new Exception("NOT IMPLEMENTED");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSelectedWMSServer(int wmsIndex) {

		return layerList.get(wmsIndex).getJcomboBackground().getSelectedItem().toString();
	}



	public boolean isAutoRefresh() {
		return (jcbAutoRefresh.isSelected());
	}


	public boolean isWaiting(int index) {
//		return wmsProgressBar[index].isIndeterminate();
		return layerList.get(index).getWmsProgressBar().isIndeterminate();
	}



	// Implementation of ActionListener interface.
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();

		if (source == jbBGRefresh) {
			doRefresh();
		}
	}

	private void refreshWMSBackground(WMSData wms, int index, String name, String layers, boolean isBW) {
		refreshWMSBackground(wms, index, name, layers, isBW, 3);
	}

	private void refreshWMSBackground(WMSData wms, int index, String name, String layers, boolean isBW, int numRetries) {

		for (int n=0; n<numRetries; n++) {

			// GET WMS DATA
			try {         

				nexview.setWMSBackground(index, 
						wms.getGridCoverage(
								name, layers, isBW, nexview.getCurrentExtent(), 
								nexview.getMapPane().getWCTZoomableBounds(new java.awt.Rectangle())
						));

				n = numRetries; // don't retry     
			} catch (WMSException niae) {
				if (n == numRetries - 1) { // last time
					String message = niae.getMessage();
					javax.swing.JOptionPane.showMessageDialog(
							this, message, "WMS ERROR", JOptionPane.INFORMATION_MESSAGE);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}




	public boolean isWmsLoading() {
		for (int n=0; n<layerList.size(); n++) {
//			if (! wmsProgressBar[n].getString().startsWith("Background")) {
			if (! layerList.get(n).getWmsProgressBar().getString().startsWith("Background")) {
				return true;
			}
		}
		return false;       
	}



	private void doRefresh() {
		for (int n=0; n<layerList.size(); n++) {
			doRefresh(n);
		}
	}

	private void doRefresh(final int index) {

		final WMSPanel thisWMSPanel = this;

		gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
			public Object construct() {

				final JProgressBar wmsProgressBar = layerList.get(index).getWmsProgressBar();

				//jbBGRefresh.setEnabled(false);
				//wmsProgressBar[0].setIndeterminate(true);
				wmsProgressBar.setStringPainted(true);
				wmsProgressBar.setString("Connecting...");
				wmsProgressBar.setValue(0);


				final WMSData wms = new WMSData();
				wms.addDataTransferListeners(new DataTransferListener() {
					public void transferStarted(DataTransferEvent dte) {
						wmsProgressBar.setString("Connecting...");
					}
					public void transferProgress(DataTransferEvent dte) {
						wmsProgressBar.setString("Transferring ("+fmt2.format(wms.getDataTransfer().getRate())+") KB/s");
						wmsProgressBar.setValue(wms.getDataTransferProgress());
						//System.out.println("WMS PROGRESS: "+wms.getDataTransferProgress());
					}
					public void transferEnded(DataTransferEvent dte) {
						wmsProgressBar.setString(" Background "+(index+1));
					}
					public void transferError(DataTransferEvent dte) {
						wmsProgressBar.setString(" Background "+(index+1));
					}
				});





				String layerString = "";               
//				if (layerSelector[0][jcomboBackground[0].getSelectedIndex()] != null) {
//				layerString = layerSelector[0][jcomboBackground[0].getSelectedIndex()].getSelectedLayersString();
//				}

				thisWMSPanel.refreshWMSBackground(wms, index, getSelectedWMSServer(index), layerString, 
						layerList.get(index).getJcbBWBackground().isSelected());

				wmsProgressBar.setValue(0);
				wmsProgressBar.setString(" Background "+(index+1));
				//wmsProgressBar[0].setIndeterminate(false);

				return "DONE";
			}
		};
		worker.start();
	}



	
	
	
	
	
	
	private void testGoogle() throws MalformedURLException, WMSException, TransformException {
		Rectangle2D.Double extent = nexview.getCurrentExtent();
		Rectangle rect = nexview.getMapPane().getWCTZoomableBounds(new java.awt.Rectangle());
		String key = "ABQIAAAARlw-tpGjckBMimzShQjmWxRQvrnVnpQobCEidydVP0-cMxyC8hSlDfyDHAJ39oCSpUH58FhYxhKpyg";
		// http://maps.google.com/staticmap?center=40.714728,-73.998672&span=1,1&size=512x128&key=MAPS_API_KEY&sensor=true_or_false
		URL url = new URL("http://maps.google.com/staticmap?center="+extent.getCenterY()+","+extent.getCenterX()+
				"&span="+extent.getHeight()+","+extent.getWidth()+
				"&size="+(int)rect.getWidth()+"x"+(int)rect.getHeight()+
				"&key="+key+"&sensor=false");
		
		System.out.println(url);
		
		WMSData wmsProcessor = new WMSData();
		GridCoverage gc = wmsProcessor.getGridCoverage(url, extent, false);
		
		nexview.setWMSBackground(0, gc);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	class WMSLayerListItem {
		private JProgressBar wmsProgressBar = new JProgressBar();
		private JComboBox jcomboBackground = new JComboBox();
		private JCheckBox jcbBWBackground = new JCheckBox("B/W");
		private ArrayList<String> comboDescriptions = new ArrayList<String>();
		private JButton jbBackground = new JButton();
		private WMSLayerSelector[] layerSelector = new WMSLayerSelector[2];

		public JProgressBar getWmsProgressBar() {
			return wmsProgressBar;
		}
		public void setWmsProgressBar(JProgressBar wmsProgressBar) {
			this.wmsProgressBar = wmsProgressBar;
		}
		public JComboBox getJcomboBackground() {
			return jcomboBackground;
		}
		public void setJcomboBackground(JComboBox jcomboBackground) {
			this.jcomboBackground = jcomboBackground;
		}
		public JCheckBox getJcbBWBackground() {
			return jcbBWBackground;
		}
		public void setJcbBWBackground(JCheckBox jcbBWBackground) {
			this.jcbBWBackground = jcbBWBackground;
		}
		public ArrayList<String> getComboDescriptions() {
			return comboDescriptions;
		}
		public void setComboDescriptions(ArrayList<String> comboDescriptions) {
			this.comboDescriptions = comboDescriptions;
		}
		public JButton getJbBackground() {
			return jbBackground;
		}
		public void setJbBackground(JButton jbBackground) {
			this.jbBackground = jbBackground;
		}
		public WMSLayerSelector[] getLayerSelector() {
			return layerSelector;
		}
		public void setLayerSelector(WMSLayerSelector[] layerSelector) {
			this.layerSelector = layerSelector;
		}


	}

}

