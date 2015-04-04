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

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradexport.NexradMetaDataExtract;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.ScanResultsManager;
import gov.noaa.ncdc.wct.export.WCTExportDialog;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.ScanResults;
import gov.noaa.ncdc.wct.io.ScanResultsComparator;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
import gov.noaa.ncdc.wct.io.WCTDirectoryScanner;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation;
import gov.noaa.ncdc.wct.ui.FavoritesManager.LoadAction;
import gov.noaa.ncdc.wct.ui.FourPanelMapView.TargetPanel;
import gov.noaa.ncdc.wct.ui.animation.WCTAnimator;
import gov.noaa.ncdc.wct.ui.event.MousePopupListener;
import gov.noaa.ncdc.wct.ui.event.SortByListener;
import gov.noaa.ngdc.nciso.ThreddsUtilitiesException;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.MathTransform;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.XMLStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    October 26, 2004
 */
public class DataSelector extends JXDialog implements ScanResultsManager, ActionListener, ListSelectionListener {



    private JButton jbDescribe, jbLoad, jbAnimate, jbExport;
    private WCTViewer viewer;
    private JCheckBox jcbExtent;
    private JLabel jlInstructions = new JLabel("Hold the 'Shift' or 'Control' keys to make multiple selections");
    private DefaultComboBoxModel dataTypeModel = 
        new DefaultComboBoxModel(new String[] { " Auto ", " GRID ", " RADIAL ", " GOES_SATELLITE ", " LEVEL-3 NEXRAD " });
    private JComboBox jcomboDataType = new JComboBox(dataTypeModel);

    private NexradMetaDataExtract nexradMetaDataExtract;
    private WCTAnimator animator;
//    private WCTExportGUI exportGUI;


    private DataSourcePanel dataSourcePanel;
    private ThreddsPanel threddsPanel;
    private final JXList resultsList = new JXList();
    private DefaultListModel listModel = new DefaultListModel();
    private ScanResults[] scanResults = null;
    private boolean[] scanResultsCacheStatus = null;

    private ArrayList<ScanResults> favoriteScanResultList = new ArrayList<ScanResults>();

    private JPanel bottomPanel, choicePanel, instructionPanel;
    private JScrollPane resultsListScrollPane;

    
    private static final Icon redIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/cache-red.png"));
    private static final Icon greenIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/cache-green.png"));
    private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();
    private final ListCellRenderer cacheRenderer = new ListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel renderer = (JLabel) DEFAULT_RENDERER.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
            renderer.setIconTextGap(12);
            
//            System.out.println("list index: "+index+
//                    "  - scanResultsCacheStatus["+resultsList.convertIndexToModel(index)+"] "+
//                    scanResultsCacheStatus[resultsList.convertIndexToModel(index)] + "    " + renderer.getText());
            
            if (scanResultsCacheStatus != null && index < scanResultsCacheStatus.length && 
                    scanResultsCacheStatus[resultsList.convertIndexToModel(index)]) {
                renderer.setIcon(greenIcon);
            }
            else {
                renderer.setIcon(redIcon);
            }
            return renderer;
        }            
    };
    
    

    
    
    /**
     *Constructor for the DataSelector object
     *
     * @param  viewer  Description of the Parameter
     */
    public DataSelector(WCTViewer viewer) {
//        super(viewer, "Data Selector", false);
        super(viewer, new JPanel());
        setTitle("Data Selector");
        setModal(false);
        this.viewer = viewer;
        createGUI();
        pack();        
    }



    
    @Override
    public void pack() {
    	if (! isVisible()) {
    		super.pack();
    	}
        setSize(new Dimension( ( super.getPreferredSize().width > 700 ) ? 700 : super.getPreferredSize().width, super.getPreferredSize().height));
    }

    private void createGUI() {
    	

        final DataSelector finalThis = this;
        dataSourcePanel = new DataSourcePanel(new SubmitButtonListener(), 
                new SortByListener(this), new FilterKeyListener(this), 
                new FavoritesActionListener(this), false);
        

        KeyListener keyListener = new KeyHandler();


        //        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
        //            new KeyEventDispatcher() {
        //              public boolean dispatchKeyEvent(KeyEvent e) {
        //                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_D) {                
        //                    showDump(getSelectedURLs()[0]);
        //                }
        //                return true;
        //              }
        //            });


        this.getContentPane().addKeyListener(keyListener);
        

        // Create Action Buttons
        choicePanel = new JPanel();
        choicePanel.setLayout(new RiverLayout());
        jcbExtent = new JCheckBox("Reset Zoom", true);
        jcbExtent.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
        jcbExtent.addActionListener(this);
        jcbExtent.addKeyListener(keyListener);
        jcbExtent.setMnemonic(KeyEvent.VK_A);

        jbDescribe = new JButton("Describe");
        //jbDescribe.setEnabled(false);
        jbDescribe.addActionListener(this);
        jbDescribe.setMnemonic(KeyEvent.VK_D);
        jbDescribe.setPreferredSize(new Dimension(100, (int)jbDescribe.getPreferredSize().getHeight()));

        jbLoad = new JButton("Load");
        jbLoad.addActionListener(this);
        jbLoad.addKeyListener(keyListener);
        jbLoad.setMnemonic(KeyEvent.VK_L);
        jbLoad.setPreferredSize(new Dimension(100, (int)jbLoad.getPreferredSize().getHeight()));


        
        jbAnimate = new JButton("Animate");
        jbAnimate.setEnabled(false);
        jbAnimate.addActionListener(this);
        jbAnimate.addKeyListener(keyListener);
        jbAnimate.setMnemonic(KeyEvent.VK_A);
        jbAnimate.setPreferredSize(new Dimension(100, (int)jbAnimate.getPreferredSize().getHeight()));

        jbExport = new JButton("Export");
//        jbExport.addActionListener(this);
//        jbExport.addKeyListener(keyListener);
//        jbExport.setMnemonic(KeyEvent.VK_E);
//        jbExport.setPreferredSize(new Dimension(120, (int)jbExport.getPreferredSize().getHeight()));


        JXHyperlink moreLink = new JXHyperlink();
        moreLink.setText("More...");
//        moreLink.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                
//            }
//        });
        
        final JPopupMenu morePopupMenu = new JPopupMenu("Additional Operations");
        JMenuItem titleItem = new JMenuItem("<html><b>Additional Operations</b></html>");
        titleItem.setToolTipText("List of additional operations");
        titleItem.setEnabled(false);
        morePopupMenu.add(titleItem);
        morePopupMenu.addSeparator();

        
        JMenuItem itemShowInFolder = new JMenuItem("Show in Folder");
        itemShowInFolder.setToolTipText("Opens the containing folder");
        itemShowInFolder.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			if (getDataSourcePanel().getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
        				Desktop.getDesktop().open(FileUtils.toFile(getSelectedURLs()[0]).getParentFile());
        			}
        			else {
        				JOptionPane.showMessageDialog(finalThis, "Not supported for the current data location", 
        						"General Error", JOptionPane.ERROR_MESSAGE);
        			}
        		} catch (IOException e1) {
        			e1.printStackTrace();
        		}
        	}
        });

        
        
        JMenuItem itemNcdump = new JMenuItem("Metadata (ncdump)");
        itemNcdump.setToolTipText("Display metadata (ncdump) for file/url");
        itemNcdump.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DataSelectionUtils.showDump(viewer, finalThis, getSelectedURLs()[0]);
            }
        });

        JMenuItem itemCopyFilename = new JMenuItem("Copy File Name(s) To Clipboard");
        itemCopyFilename.setToolTipText("Copy the file name(s) text to the system clipboard");
        itemCopyFilename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	StringBuilder sb = new StringBuilder();
            	for (URL url : getSelectedURLs()) {
            		sb.append(url.toString().substring(url.toString().lastIndexOf("/")+1)).append("\n");
            	}
            	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TextSelection(sb.toString()), null);                  
            }
        });


        
        JMenuItem itemCopyURL = new JMenuItem("Copy URL(s) To Clipboard");
        itemCopyURL.setToolTipText("Copy the URL(s) text to the system clipboard");
        itemCopyURL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	StringBuilder sb = new StringBuilder();
            	for (URL url : getSelectedURLs()) {
            		sb.append(url).append("\n");
            	}
            	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TextSelection(sb.toString()), null);                  
            }
        });
        
        

        JMenuItem itemShowNcML = new JMenuItem("NcML (NetCDF Metadata in XML)");
        itemShowNcML.setToolTipText("Display metadata as NcML XML format for file/url");
        itemShowNcML.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	DataSelectionUtils.showNcmlDump(viewer, finalThis, getSelectedURLs()[0]);
            }
        });
        

        JMenuItem itemShowISO = new JMenuItem("Generate ISO 19115 XML");
        itemShowISO.setToolTipText("Display metadata as ISO 19115 XML for file/url");
        itemShowISO.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
					DataSelectionUtils.showIso(viewer, finalThis, getSelectedURLs()[0]);
				} catch (ThreddsUtilitiesException e1) {
					e1.printStackTrace();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            }
        });
        
        

        JMenuItem itemShowUDDC = new JMenuItem("Metadata Quality (UDDC Report)");
        itemShowUDDC.setToolTipText("Display report showing use of Unidata Data Discovery Conventions for file/url");
        itemShowUDDC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
					DataSelectionUtils.showUddc(viewer, finalThis, getSelectedURLs()[0]);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (ThreddsUtilitiesException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            }
        });
        
        
        JMenuItem itemMathTool = new JMenuItem("Math Tool (BETA)");
        itemMathTool.setToolTipText("Display WCT Math Tool");
        itemMathTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
    			RasterMathDialog mathUI = new RasterMathDialog(viewer, finalThis);
            }
        });
        
        
        JMenuItem itemAddToFavorites = new JMenuItem("Add to Favorites");
        itemAddToFavorites.setToolTipText("Add to the Data Selector Favorites List");
        itemAddToFavorites.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
    			addFavorite();
            }
        });
        
        
        
        
        
        
        morePopupMenu.add(itemAddToFavorites);
        morePopupMenu.add(itemShowInFolder);
        morePopupMenu.add(itemNcdump);
        morePopupMenu.add(itemCopyFilename);
        morePopupMenu.add(itemCopyURL);
        morePopupMenu.add(itemShowNcML);
        morePopupMenu.add(itemShowISO);
        morePopupMenu.add(itemShowUDDC);
        morePopupMenu.add(itemMathTool);
        



        JMenuItem itemDataDump = new JMenuItem("Data Dump");
        itemDataDump.setToolTipText("Show Data Dump");
        itemDataDump.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {


        		try {

        			URL url = getSelectedURLs()[0];
        			boolean useCache = true;
        			if (useCache) {
        				FileScanner scannedFile = new FileScanner();
        				scannedFile.scanURL(url);
        				url = WCTDataUtils.scan(url, scannedFile, true, true, null, finalThis);
        			}


        			String prefStore = ucar.util.prefs.XMLStore.makeStandardFilename(".unidata", "NetcdfUI22.xml");
        			XMLStore store = ucar.util.prefs.XMLStore.createFromFile(prefStore, null);
        			PreferencesExt prefs = store.getPreferences();


//        			FileManager fileChooser = new FileManager(viewer);
//        			DatasetViewer dv = new DatasetViewer(prefs, fileChooser);
//        			dv.setDataset(NetcdfFile.open(url.toString()));


        			JDialog dialog = new JDialog(finalThis, "Data Dump Viewer");
//        			dialog.add(dv);
        			dialog.add(new JLabel("This feature is currently unavailable"));
        			dialog.pack();
        			dialog.setVisible(true);




        		} catch (Exception ex) {
        			System.out.println("dataset viewer problem " + ex);
        		}


        	}
        });
        morePopupMenu.add(itemDataDump);
        
        
        
        
        
        
        
        
        moreLink.addMouseListener(new MousePopupListener(moreLink, morePopupMenu));
        


        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        dataTypeModel.removeAllElements();
        dataTypeModel.addElement("Auto");
        try {
            for (SupportedDataType dataType : getDataTypes()) {
                dataTypeModel.addElement(dataType.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jcomboDataType.setModel(dataTypeModel);


        final JXHyperlink cacheLink = new JXHyperlink();
        cacheLink.setText("Cache");
        cacheLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int decision = JOptionPane.showConfirmDialog(finalThis, "Remove from cache?", "Data Cache", 
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (decision == 0) {
                    for (URL url : getSelectedURLs()) {
                        try {
                            System.out.println("removing from cache: "+url);
                            if (WCTTransfer.isInCache(url)) {
                                WCTTransfer.removeFromCache(url);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    checkCacheStatus();
                }
            }
        });

        jlInstructions.setHorizontalAlignment(JLabel.CENTER);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension((int)cacheLink.getPreferredSize().getWidth(), (int)cacheLink.getPreferredSize().getHeight()));

        instructionPanel = new JPanel(new BorderLayout());
        if (! dataSourcePanel.getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            instructionPanel.add(cacheLink, BorderLayout.WEST);
        }
        else {
            instructionPanel.remove(cacheLink);
        }
//        instructionPanel.add(cacheLink, BorderLayout.WEST);
        instructionPanel.add(jlInstructions, BorderLayout.CENTER);
        instructionPanel.add(emptyPanel, BorderLayout.EAST);
        
//        panel.setPreferredSize(new Dimension((int)choicePanel.getPreferredSize().getWidth(), (int)panel.getPreferredSize().getHeight()));
//        choicePanel.add(cacheLink, "left ");
//        choicePanel.add("center hfill", jlInstructions);

//        cacheLink.setBorder(BorderFactory.createLineBorder(Color.RED));
//        jlInstructions.setBorder(BorderFactory.createLineBorder(Color.GREEN));
//        emptyPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        
//        choicePanel.add(emptyPanel, "right");
        choicePanel.add(instructionPanel, "hfill center");
        choicePanel.add(jcbExtent, "br center");
        choicePanel.add(new JLabel("  Data Type: "));
        choicePanel.add(jcomboDataType);

        choicePanel.add(jbLoad, "br center");
        choicePanel.add(jbAnimate);
//        choicePanel.add(jbExport);

        //        JButton geButton = new JButton("Launch Ext. GE");           
        //        geButton.addActionListener(new ActionListener() {
        //            @Override
        //            public void actionPerformed(ActionEvent e) {
        //                GoogleEarthBrowser.launchWindow(nexview);
        //            }
        //        });
        //        choicePanel.add(geButton);


        //        final Component finalThis = this;
        //        JButton geIntButton = new JButton("Launch Int. GE");    
        //        geIntButton.addActionListener(new ActionListener() {
        //            @Override
        //            public void actionPerformed(ActionEvent e) {
        //                try {
        //                    final WCTImageServer server = new WCTImageServer(nexview);
        //                    nexview.setCurrentViewType(CurrentViewType.GOOGLE_EARTH);
        //                } catch (BindException ex) {
        //                    ex.printStackTrace();
        //                    JOptionPane.showMessageDialog(finalThis, 
        //                            "Only one instance of the internal Weather and Climate Toolkit \n" +
        //                            "Google Earth Browser may be open.  Please close the other \n" +
        //                            "browser and try again.", "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
        //                } catch (Exception ex) {
        //                    ex.printStackTrace();
        //                    JOptionPane.showMessageDialog(finalThis, ex.getMessage(), "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
        //                }
        //            }
        //        });
        //        choicePanel.add(geIntButton);




//        JButton ncdcMapButton = new JButton("Launch NCDC Map");    
//        ncdcMapButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final WCTFrame frame = new WCTFrame("NCDC Online Map Viewer");
//
//                try {
//                    NativeInterface.open();
//                } catch (Exception ex) {
//                    ;
//                }
//                try {
//                    NativeInterface.runEventPump();
//                } catch (Exception ex) {
//                    ;
//                }
//
//
//                JWebBrowser browser = new JWebBrowser();
//                browser.setBarsVisible(false);
//                browser.navigate("http://www.drought.gov/imageserver/NIDIS_Viewer/southeast/NIDIS_Viewer.html");
//
//                frame.setLayout(new BorderLayout());
//                frame.getContentPane().add(browser);
//
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//                        frame.setSize(800, 600);
//                        frame.setLocationByPlatform(true);
//                        frame.setVisible(true);
//
//                    }
//                });
//            }
//        });
//                choicePanel.add(ncdcMapButton);



        //        JButton refreshMapButton = new JButton("Refresh Map");
        //        refreshMapButton.addActionListener(new ActionListener() {
        //            @Override
        //            public void actionPerformed(ActionEvent arg0) {
        //                nexview.refreshCurrentView();                
        //            }
        //        });
        //        choicePanel.add(refreshMapButton);


//        final JButton contourButton = new JButton("Add Contours");
//        contourButton.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent evt) {
//        		try {
//
//        			ContourManager.contour(viewer);
//        			
//        			viewer.addRenderCompleteListener(new RenderCompleteListener() {
//						@Override
//						public void renderProgress(int progressPercent) {
//						}
//						@Override
//						public void renderComplete() {
//							try {
//								ContourManager.contour(viewer);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					});
//        			
//        			contourButton.setEnabled(false);
//        		
//        		} catch (Exception e) {
//        			e.printStackTrace();
//        		}
//        		
//        	}
//        });
//        choicePanel.add(contourButton);

        
        
        
        try {
			FavoritesManager.getInstance().loadCurrentFavoritesFromProfile();
			ArrayList<Favorite> favList = FavoritesManager.getInstance().getCurrentFavoriteList();
			for (Favorite fav : favList) {
				ScanResults scan = new ScanResults();
				scan.setDisplayName(fav.getDisplayString());
				scan.setLongName(fav.getDisplayString());
				scan.setUrl(fav.getDataURL());
				favoriteScanResultList.add(scan);
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        
		
		
		
		JButton morphButton = new JButton("Morph");
		morphButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
                try {
					foxtrot.Worker.post(new foxtrot.Task() {
					    public Object run() {

					    	doMorph(finalThis);
					    	
					    	return "DONE";
					    }
					});
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}


			}
		});
//		choicePanel.add(morphButton);
		
		
		
		
		
		JButton fourPanelButton = new JButton("4 Panel Test");
		fourPanelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final FourPanelMapView fpmv = new FourPanelMapView(viewer);
				fpmv.launchInFrame();
				
				resultsList.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						System.out.println(e.getKeyCode());
						if (e.getKeyCode() == KeyEvent.VK_1) {
							fpmv.setCurrentTargetPanel(TargetPanel.PANEL1);
							loadData();
						}
						else if (e.getKeyCode() == KeyEvent.VK_2) {
							fpmv.setCurrentTargetPanel(TargetPanel.PANEL2);
							loadData();
						}
						else if (e.getKeyCode() == KeyEvent.VK_3) {
							fpmv.setCurrentTargetPanel(TargetPanel.PANEL3);
							loadData();
						}
						else if (e.getKeyCode() == KeyEvent.VK_4) {
							fpmv.setCurrentTargetPanel(TargetPanel.PANEL4);
							loadData();
						}
					}
					@Override
					public void keyReleased(KeyEvent e) {
					}
					@Override
					public void keyTyped(KeyEvent e) {
					}
				});
				

			}
		});
//		choicePanel.add(fourPanelButton);
		
		
		
		
		
		
        JButton favsButton = new JButton("Add to Favorites");
        favsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addFavorite();
			}
        });
//        choicePanel.add(favsButton);
        

        JButton projButton = new JButton("Projection");
        projButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					
					GeometryFactory gf = new GeometryFactory();
					Envelope env = new Envelope(-130, -55, 5, 65);
					
//					CoordinateSystem cs = WCTProjections.getCoordinateSystem(
//							WCTProjections.ALBERS_EQUALAREA_CONUS_NAD83_WKT);
					CoordinateSystem cs = WCTProjections.getCoordinateSystem(
							WCTProjections.WEB_MERCATOR_WKT);
					
//					MathTransform trans = WCTProjections.getMathTransform(WCTProjections.WGS84_WKT, 
//							WCTProjections.ALBERS_EQUALAREA_CONUS_NAD83_WKT);
					MathTransform trans = WCTProjections.getMathTransform(WCTProjections.WGS84_WKT, 
							WCTProjections.WEB_MERCATOR_WKT);

					double[] src = new double[2];
					double[] dest = new double[2];
					
					MapLayer[] layers = viewer.getMapContext().getLayers();
					for (MapLayer layer : layers) {
						System.out.println("processing layer: "+layer.getTitle());
						FeatureCollection fc = layer.getFeatureSource().getFeatures().collection();
						FeatureIterator fi = fc.features();
						while (fi.hasNext()) {
							Feature f = fi.next();
							Geometry g = f.getDefaultGeometry();
							try {
								f.setDefaultGeometry(g.intersection(gf.toGeometry(env)));
							} catch (IllegalAttributeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							
							Coordinate[] coords = f.getDefaultGeometry().getCoordinates();
							for (Coordinate c : coords) {
								src[0] = c.x;
								src[1] = c.y;
								trans.transform(src, 0, dest, 0, 1);
								c.x = dest[0];
								c.y = dest[1];
//								System.out.println(c);
							}
						}
						viewer.getMapContext().removeLayer(layer);
						viewer.getMapContext().addLayer(
								new DefaultMapLayer(fc, layer.getStyle(), layer.getTitle()));
					}
					
					viewer.getMapPane().repaint();
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CannotCreateTransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
//        choicePanel.add(projButton);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        
//        JButton jbExport = new JButton("Export");
        jbExport.addKeyListener(keyListener);
        jbExport.setMnemonic(KeyEvent.VK_E);
        jbExport.setPreferredSize(new Dimension(100, (int)jbExport.getPreferredSize().getHeight()));
        jbExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getSelectedURLs().length == 0) {
                    JOptionPane.showMessageDialog(finalThis, "Please select a file.");
                    return;
                }
                

//                try {
//                foxtrot.Worker.post(new foxtrot.Task() {
//                    public Object run() {

                        try {
                            
                            WCTExportDialog wizard = new WCTExportDialog("Data Export Wizard", viewer);
                            wizard.pack();
                            wizard.setLocationRelativeTo(finalThis);
                            wizard.setVisible(true);                    
                        
                            viewer.getDataSelector().checkCacheStatus();
                            System.out.println("data export wizard done");

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(finalThis, ex.getMessage());
                        }


//                        return "DONE";
//                    }
//                });
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(finalThis, ex.getMessage());
//                }



                        
            }
        });
        choicePanel.add(jbExport);
        choicePanel.add(moreLink);
        
        
        

    	
//        JButton djtestButton = new JButton("DJTest");
//        djtestButton.addActionListener(new ActionListener()  {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				
//		        foxtrot.Worker.post(new foxtrot.Job() {
//		            public Object run() {
//
//				
//			        	SimpleWebBrowserExample.run(new String[] {});			
//
//			        	return "DONE";
//			        }			        
//			    });
//			}
//        });
//        choicePanel.add(djtestButton);
        
        
//        JButton colladaButton = new JButton("ISOSURFACE!");
//        colladaButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//			    final ViewerKmzUtilities kmzUtils = new ViewerKmzUtilities(viewer);
//			               
//			    
//			    
//                try {
//
//            		String uniqueString = String.valueOf(System.currentTimeMillis()%1000000);    
//            		
//            		File outKmlFile = new File(WCTConstants.getInstance().getDataCacheLocation()+
//                			File.separator+"iso-"+uniqueString+".kml");
//            		
//                	if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
//                		
////                	        foxtrot.Worker.post(new foxtrot.Job() {
////                	            public Object run() {
////                	                try {
//                		
//                						System.out.println(Arrays.toString(viewer.getGridSatelliteGridCoverage().
//                							getSampleDimensions()[0].getCategory(0).getColors()));
//                							
//                						Color[] colors = viewer.getGridSatelliteGridCoverage().
//                    							getSampleDimensions()[0].getCategory(0).getColors();
//
//                    						
//                						
//                						String s = JOptionPane.showInputDialog(finalThis, "Enter Value to IsoSurface: ",  "");
//                						if (s == null) {
//                							return;
//                						}
//                						float isoVal = Float.parseFloat(s);
//
//                						String s2 = JOptionPane.showInputDialog(finalThis, "Enter Elevation Exaggeration: ",  "");
//                						if (s2 == null) {
//                							return;
//                						}
//                						int elevExag = Integer.parseInt(s2);
//
//                						
//                						
//                						Color isoColor = ColorsAndValues.getColor(colors, 
//                								viewer.getGridDatasetRaster().getGridMinValue(),
//                								viewer.getGridDatasetRaster().getGridMaxValue(), 
//                								isoVal);
//
//                						
//                						System.out.println("color found for iso value of "+isoVal+" = "+isoColor);
//                						
//                						String hexColor = Integer.toHexString(isoColor.getRGB());
//                						
//                						System.out.println("hex color = "+hexColor);
//                						
//                						
//                						hexColor = hexColor.substring(0, 2) + hexColor.substring(6,8) + 
//                							hexColor.substring(4, 6) + hexColor.substring(2, 4);
//                						
//                						URL url = WCTDataUtils.scan(getSelectedURLs()[0], 
//                								viewer.getFileScanner(), true, true, getSelectedDataType());
//                	                	iso.generateIsosurfaceKML(
//                	                			url.toString(), 
//                	                			viewer.getGridProps().getSelectedGridIndex(), 
//                	                			viewer.getCurrentExtent(),
//                	                			elevExag,
//                	                			isoVal, hexColor, outKmlFile);
//                	                	
//
//                	                        	Desktop.getDesktop().open(outKmlFile);
//                	                	
//                		
////                            			return "done";
////                	                } catch (Exception e) {
////                	                    e.printStackTrace();
////                	                    return null;
////                	                }
////                	            }
////                	        });
//                	        
////                		}
//                	}
//                	else if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//                		
////            	        foxtrot.Worker.post(new foxtrot.Job() {
////            	            public Object run() {
////            	                try {
//            		
//            						System.out.println(Arrays.toString(viewer.getRadarGridCoverage().
//            							getSampleDimensions()[0].getCategory(0).getColors()));
//            							
//            						Color[] colors = viewer.getRadarGridCoverage().
//                							getSampleDimensions()[0].getCategory(0).getColors();
//
//                						
//            						
//            						String s = JOptionPane.showInputDialog(finalThis, "Enter Value to IsoSurface: ",  "");
//            						if (s == null) {
//            							return;
//            						}
//            						float isoVal = Float.parseFloat(s);
//
//            						double[] minMax = NexradValueFactory.getProductMaxMinValues(viewer.getNexradHeader());
//            						
//            						Color isoColor = ColorsAndValues.getColor(colors, 
//            								minMax[0], minMax[1], 
//            								isoVal);
//
//            						
//            						System.out.println("color found for iso value of "+isoVal+" = "+isoColor);
//            						
//            						String hexColor = Integer.toHexString(isoColor.getRGB());
//            						
//            						System.out.println("hex color = "+hexColor);
//            						
//            						
//            						hexColor = hexColor.substring(0, 2) + hexColor.substring(6,8) + 
//            							hexColor.substring(4, 6) + hexColor.substring(2, 4);
//            						
//            		
//            						URL url = WCTDataUtils.scan(getSelectedURLs()[0], 
//            								viewer.getFileScanner(), true, true, getSelectedDataType());
//            	                	WCTIsoSurface.generateIsosurfaceKML(
//            	                			url.toString(), 
//            	                			viewer.getRadialRemappedRaster().getVariableName(), 
//            	                			viewer.getCurrentExtent(),
//            	                			isoVal, hexColor, outKmlFile);
//            	                	
//
//            	                        	Desktop.getDesktop().open(outKmlFile);
//            	                	
//            		
////                        			return "done";
////            	                } catch (Exception e) {
////            	                    e.printStackTrace();
////            	                    return null;
////            	                }
////            	            }
////            	        });
//            	        
////            		}
//            	}
//
//   
//                    
//                	
//                	
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(finalThis, ex.getMessage());
//                }
//
//			}
//        });
//        choicePanel.add(colladaButton);
        
        
        

        //bottomPanel.add(listProgress, BorderLayout.NORTH);
        bottomPanel.add(choicePanel, BorderLayout.SOUTH);





        this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_NONE, WCTDropTargetHandler.getInstance()));
        
        
        
        resultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultsList.setModel(listModel);
        resultsList.addKeyListener(keyListener);
        resultsList.addMouseListener(new ClickHandler());
        resultsList.setFilterEnabled(true);

        
        
        
        // swingx stuff
//                resultsList.setHighlighters(new ColorHighlighter(Color.GRAY, Color.WHITE), new BorderHighlighter(), new ShadingColorHighlighter());
//                resultsList.setHighlighters(new BorderHighlighter(), new ShadingColorHighlighter());
                resultsList.setRolloverEnabled(true);
        resultsList.setFont(new Font("monospaced", Font.PLAIN, 12));

        
        
        
        resultsListScrollPane = new JScrollPane(resultsList);
        resultsListScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 12, 2, 12),
                BorderFactory.createEtchedBorder())
        );

        threddsPanel = new ThreddsPanel();
        threddsPanel.addKeyListener(keyListener);
        threddsPanel.getItemList().addKeyListener(keyListener);
        threddsPanel.getItemList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    loadData();
                }
            }
        });

        
        
        
        
        
        
        
        
        
        JPopupMenu listPopupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Add to Favorites");
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
//				JOptionPane.showMessageDialog(null, "added to favs");
				addFavorite();
			}
        });
        listPopupMenu.add(menuItem);
        resultsList.addMouseListener(new ListPopupListener(listPopupMenu));
        threddsPanel.getItemList().addMouseListener(new ListPopupListener(listPopupMenu));
        

        
        
        
        
        
        
        JXHyperlink saveLink = new JXHyperlink();
        saveLink.setText("Save");
        saveLink.setToolTipText("Saves the catalog.xml link in the dataset history list");
        saveLink.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String catalogLink = threddsPanel.getCurrentCatalogURL().replaceAll(".html", ".xml");
                    dataSourcePanel.setDataLocation(WCTDataSourceDB.THREDDS, catalogLink);
                    threddsPanel.process(dataSourcePanel.getDataLocation());
                    
                    System.out.println("linked clicked! "+catalogLink);
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        threddsPanel.addLink(saveLink);



        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(dataSourcePanel, BorderLayout.NORTH);

        resultsList.addListSelectionListener(this);




        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
            System.out.println("tab page change event -- SINGLE FILE");

            jbAnimate.setEnabled(false);
//            jlInstructions.setText("");
            choicePanel.remove(instructionPanel);

            getContentPane().remove(resultsListScrollPane);
            getContentPane().remove(threddsPanel);
            getContentPane().add(bottomPanel, BorderLayout.SOUTH);
            pack();
        }


        final JTabbedPane tabPane = dataSourcePanel.getDataTypeTabPane();
        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                
                dataSourcePanel.clearListFilters();
                
                if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
//                    System.out.println("tab page change event -- SINGLE FILE");

                    jbLoad.setEnabled(true);
                    jbAnimate.setEnabled(false);

                    choicePanel.remove(instructionPanel);
                    
                    getContentPane().remove(resultsListScrollPane);
                    getContentPane().remove(threddsPanel);
                    getContentPane().add(bottomPanel, BorderLayout.SOUTH);
                    pack();
                }
                else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.FAVORITES)) {
                	
                	
                    listModel = WCTUiUtils.fillListModel(listModel, 
                    		(ScanResults[])favoriteScanResultList.toArray(new ScanResults[favoriteScanResultList.size()]));
                    resultsList.setModel(listModel);
                    resultsList.setSelectedIndex(0);
                    getContentPane().remove(threddsPanel);
                    getContentPane().remove(bottomPanel);
                    getContentPane().add(resultsListScrollPane, BorderLayout.CENTER);
                    getContentPane().add(bottomPanel, BorderLayout.SOUTH);
                    pack();
                    
                    
                    int selectedTabIndex = tabPane.getSelectedIndex();
                    int selectedTypeID = new ArrayList<Integer>(DataSourcePanel.CURRENT_TAB_ORDER_LOOKUP.keySet()).get(selectedTabIndex);
                    WCTProperties.setWCTProperty("dataSourceType", String.valueOf(selectedTypeID));
                }
                else {
                    if (resultsList.getSelectedIndices().length > 1) {
                        jbDescribe.setEnabled(false);
                        jbLoad.setEnabled(false);
                        jbAnimate.setEnabled(true);
                    }
                    else {
                        jbDescribe.setEnabled(true);
                        jbLoad.setEnabled(true);
                        jbAnimate.setEnabled(true);
                    }
//                    jlInstructions.setText("Hold the 'Shift' or 'Control' keys to make multiple selections");
                    
                    if (! dataSourcePanel.getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
                        instructionPanel.add(cacheLink, BorderLayout.WEST, 0);
                    }
                    else {
                        instructionPanel.remove(cacheLink);
                    }
                    choicePanel.add(instructionPanel, "hfill center", 0);

                    getContentPane().remove(threddsPanel);
                    getContentPane().remove(resultsListScrollPane);
                    getContentPane().remove(bottomPanel);
                    pack();
                }
            }
        });



        int selectedTypeID = WCTProperties.getWCTProperty("dataSourceType") == null ? 
        		0 : Integer.parseInt(WCTProperties.getWCTProperty("dataSourceType"));        
        int selectedTabIndex = new ArrayList<Integer>(DataSourcePanel.CURRENT_TAB_ORDER_LOOKUP.keySet()).indexOf(selectedTypeID);
        try {
            tabPane.setSelectedIndex(selectedTabIndex);
        } catch (Exception e) {
        }

        pack();
        
        

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.remove(enterKey);
//        this.getRootPane().setDefaultButton(null);
        this.getRootPane().setDefaultButton(jbLoad);

        
        
    }

    
    
    private void addFavorite() {
        if (getDataSourcePanel().getDataType().equals(WCTDataSourceDB.THREDDS)) {
        	URL[] urls = threddsPanel.getSelectedDataAccessURLArray();
        	for (URL url : urls) {
        		String name = url.toString().substring(url.toString().lastIndexOf("/")+1) +
        		" (TDS: "+url.getHost()+")";
        		
        		ScanResults scan = new ScanResults();
        		scan.setUrl(url);
        		scan.setDisplayName(name);
        		scan.setLongName(name);
        		favoriteScanResultList.add(scan);
        		
        		Favorite fav = new Favorite();
        		fav.setDataURL(url);
        		fav.setDisplayString(name);
				FavoritesManager.getInstance().getCurrentFavoriteList().add(fav);
        	}
        }
        else if (getDataSourcePanel().getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
        	URL url = getSelectedURLs()[0];
        	String name = url.toString().substring(url.toString().lastIndexOf("/")+1) +
        	 " (Source: "+url.getHost()+")";

        	ScanResults scan = new ScanResults();
        	scan.setUrl(url);
        	scan.setDisplayName(name);
        	scan.setLongName(name);
			favoriteScanResultList.add(scan);

			Favorite fav = new Favorite();
			fav.setDataURL(url);
			fav.setDisplayString(name);
			FavoritesManager.getInstance().getCurrentFavoriteList().add(fav);
        }
        else if (getDataSourcePanel().getDataType().equals(WCTDataSourceDB.FAVORITES)) {
        	int[] selectedIndices = getSelectedIndices();
        	for (int n : selectedIndices) {
        		ScanResults scan = favoriteScanResultList.get(n);
        		favoriteScanResultList.add(scan);
        		
        		Favorite fav = FavoritesManager.getInstance().getCurrentFavoriteList().get(n);
        		Favorite favCopy = new Favorite();
        		favCopy.setDataURL(fav.getDataURL());
        		favCopy.setDisplayString(fav.getDisplayString());
   				FavoritesManager.getInstance().getCurrentFavoriteList().add(favCopy);
        	}
            listModel = WCTUiUtils.fillListModel(listModel, 
            		(ScanResults[])favoriteScanResultList.toArray(new ScanResults[favoriteScanResultList.size()]));
            resultsList.setModel(listModel);
            resultsList.setSelectedIndex(0);
        	
        }
        else {
			for (int n=0; n<resultsList.getSelectedIndices().length; n++) {
				favoriteScanResultList.add(scanResults[getSelectedIndices()[n]]);

				Favorite fav = new Favorite();
				fav.setDataURL(scanResults[getSelectedIndices()[n]].getUrl());
				if (scanResults[getSelectedIndices()[n]].getLongName() != null) {
					fav.setDisplayString(scanResults[getSelectedIndices()[n]].getLongName());
				}
				else {
					fav.setDisplayString(scanResults[getSelectedIndices()[n]].getFileName());
				}
				FavoritesManager.getInstance().getCurrentFavoriteList().add(fav);
			}
        }				
		
        try {
        	FavoritesManager.getInstance().saveCurrentFavoritesToProfile();
        } catch (IOException e1) {
        	e1.printStackTrace();
        }

    }
    
    
    
    
    private void doMorph(final Component finalThis) {
		
		
		URL[] urls = getSelectedURLs();
		if (urls.length != 2 && urls.length != 1) {
			JOptionPane.showMessageDialog(finalThis, "<html>Please select files: <br>" +
					"1 file to use Storm Tracking to morph <br>" +
					"	into the future (you will be asked for file)<br>" +
					"2 files to use SWDI to morph between them.</html>", "Morph Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		int numSteps = Integer.parseInt(JOptionPane.showInputDialog(
				finalThis, "Enter number of morph steps", "Enter number of morph steps", JOptionPane.INFORMATION_MESSAGE));
		
		WCTMorphOperation morph = new WCTMorphOperation();
		
		if (urls.length == 2) {		
//			morph.processWithSWDI(viewer.getCurrentExtent(), numSteps, urls[0], urls[1]); 
			
			ScanResults[] scanResults = getScanResults();
			for (ScanResults sr : scanResults) {
				if (sr.getTimestamp().equals(
						scanResults[getSelectedIndices()[0]].getTimestamp())) {
					
					if (sr.getProductID().equals("NST")) {
						System.out.println(sr);
//				    	morph.processWithStormTracking(viewer.getCurrentExtent(), numSteps, 
//				    			urls[0], urls[1], sr.getUrl());
				    	return;
					}
					
					
				}
			}

			
			
		}
		else if (urls[0].toString().startsWith("ftp://") &&
				urls[0].toString().endsWith("sn.last")){

			try {
				// 01234567890123456789012345678901234567890123456789012345678901234567890
				// ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kgsp
				// SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kgsp
				// Get Storm Tracking URL for site requested
				URL stormTrackingURL = new URL(urls[0].getProtocol()+"://"+
						urls[0].getUserInfo()+"@"+urls[0].getHost()+
						urls[0].getPath().substring(0, 31)+
						"58sti"+urls[0].getPath().substring(36));
				System.out.println(stormTrackingURL);
				
				stormTrackingURL = WCTTransfer.getURL(stormTrackingURL);
				
//				morph.processWithStormTracking(viewer.getCurrentExtent(), numSteps,
//						urls[0], stormTrackingURL);
//
//				
//				int cnt = 0;
//            	for (GridCoverage gc : morph.getMorphedGridCoverages()) {
//            		
//            		viewer.setRadarGridCoverage(gc);
//            		BufferedImage image = viewer.getViewerBufferedImage();
//
//					ImageIO.write(image, "png", new File("E:\\work\\morph\\output\\morph-nws_"+(cnt++)+".png"));
//            	}

            	
            	
				
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		else {
			try {
				
				ScanResults[] scanResults = getScanResults();
				for (ScanResults sr : scanResults) {
					if (sr.getTimestamp().equals(
							scanResults[getSelectedIndices()[0]].getTimestamp())) {
						
						if (sr.getProductID().equals("NST")) {
							System.out.println(sr);
//					    	morph.processWithStormTracking(viewer.getCurrentExtent(), numSteps, 
//					    			urls[0], sr.getUrl());
					    	return;
						}
						
						
					}
				}

				JFileChooser chooser = new JFileChooser(WCTProperties.getWCTProperty("customLocal"));
			    int returnVal = chooser.showOpenDialog(finalThis);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
//			    	morph.processWithStormTracking(viewer.getCurrentExtent(), numSteps, 
//			    			urls[0], chooser.getSelectedFile().toURI().toURL());
			    }
			    
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

    }
    
    
    
    
    




    /**
     * Implementation of ActionListener interface.
     *
     * @param  event  Description of the Parameter
     */
    //    @Override
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();
        if (source == jbDescribe) {
            if (nexradMetaDataExtract == null) {
                nexradMetaDataExtract = new NexradMetaDataExtract();
            }
            try {
                nexradMetaDataExtract.showDescribeNexradDialog(getSelectedURLs()[0], this);
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, "Metadata Error: "+e, "METADATA ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
            }
        }
        else if (source == jbLoad) {
        	if (jbLoad.getText().equals("Cancel")) {
        		cancelLoad();
        	}
        	else {
        		loadData();
        	}
        }
        else if (source == jbAnimate) {
			try {
				foxtrot.Worker.post(new foxtrot.Task() {
				    public Object run() {

				        animate();
				    	
				    	return "DONE";
				    }
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
//            animate();
        }
        else if (source == jbExport) {
//            export();
            javax.swing.JOptionPane.showMessageDialog(this, "not supported", "not supported anymore", javax.swing.JOptionPane.ERROR_MESSAGE);            
        }
    }
    // actionPerformed

    
    public void cancelLoad() {
		WCTUtils.getSharedCancelTask().setCancel(true);
		setIsLoading(false);
		viewer.setIsLoading(false);
    }
    
    
    //    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (! event.getValueIsAdjusting()) {
            if (resultsList.getSelectedIndices().length > 1) {
                jbDescribe.setEnabled(false);
                jbLoad.setEnabled(false);
                jbAnimate.setEnabled(true);
            }
            else {
                jbDescribe.setEnabled(true);
                jbLoad.setEnabled(true);
                jbAnimate.setEnabled(true);
            }
        }

    }



    /**
     * Load File selected in dataselectPanel
     */
    public void loadData() {
    	
    	if (getSelectedURLs() == null) {
    		return;
    	}

        if (getSelectedURLs().length == 0) {
            JOptionPane.showMessageDialog(this, "Please select a file.");
            return;
        }
        
        final URL dataUrl;   
        try {
            dataUrl = getSelectedURLs()[0];
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Data Select Error: "+e, "DATA SELECT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);         
            return;
        }

        loadData(dataUrl);
        
//        System.out.println("setting scanResultsCacheStatus["+getSelectedIndices()[0]+"] = "+WCTTransfer.isInCache(dataUrl));
        if (! dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE) &&
                ! dataSourcePanel.getDataType().equals(WCTDataSourceDB.THREDDS) &&
                ! dataSourcePanel.getDataType().equals(WCTDataSourceDB.FAVORITES) &&
                scanResultsCacheStatus != null) {
            scanResultsCacheStatus[getSelectedIndices()[0]] = WCTTransfer.isInCache(dataUrl);
        }
        resultsList.repaint();
        
    }   

    /**
     * Load File
     */
    public void loadData(final URL url) {

        // reset the autoRefresh if necessary
        viewer.getMapPaneZoomChange().setRadarActive(viewer.getViewProperties().isAutoRefresh());
        // reset the NexradMath display
//        NexradMath nexradMath = viewer.getNexradMath();
//        if (nexradMath != null) {
//            nexradMath.setMathDisplayed(false);
//        }

        try {
            if (url == null) {
                return;
            }
            else {
                viewer.setAlphanumericProperties(false);
                
                
                if (isUrlCompressed(url)) {
                	DecompressionDialog dd = new DecompressionDialog(this);
//                	dd.process();
                	String outputLocation = dd.getSelectedOutputLocation();
                	getDataSourcePanel().setDataLocation(WCTDataSourceDB.LOCAL_DISK, outputLocation);
                	submitListFiles();
                	return;
                }


                // Special update history and cache check for single file/url
                if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE) ||
                		dataSourcePanel.getDataType().equals(WCTDataSourceDB.FAVORITES)) {
                	
                    dataSourcePanel.updateHistory();
                    
                    if (WCTTransfer.isInCache(url)) {
                        int decision = JOptionPane.showOptionDialog(this, "File found in Toolkit data cache.\n" +
                        		"(For more cache info, use: 'View' - 'View Properties' menu)\n\nUse cache file?", 
                        		"Toolkit Data Cache", 
                                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                        
                        if (decision == 1) {
                            try {
                                WCTTransfer.removeFromCache(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(this, e.getMessage());
                            }
                        }
                    }
                }


                
                boolean backgroundThread = true;
                if (backgroundThread) {

                    foxtrot.Worker.post(new foxtrot.Task() {
                        public Object run() {

                            System.out.println("DATA SELECTOR - loading: "+url);
                            try {
                            	viewer.loadFile(url, true, false, false, jcbExtent.isSelected());
                            	viewer.updateMemoryLabel();
                            } catch (Exception e) {
                            	e.printStackTrace();
                            	JOptionPane.showMessageDialog(viewer, 
                            			"--- General Data Load Error ---\n"+
                            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
                            			"Data Load Error", JOptionPane.ERROR_MESSAGE);
                            }

                            return "DONE";
                        }
                    });
                }
                else {
                	viewer.loadFile(url, true, false, false, jcbExtent.isSelected());
                }


            }
            // END else

        }
        // END try
        catch (Exception e) {
            e.printStackTrace();
        	JOptionPane.showMessageDialog(viewer, 
        			"--- General Data Load Error ---\n"+
        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
        			"Data Load Error", JOptionPane.ERROR_MESSAGE);

        }

    }



    public boolean isUrlCompressed(URL url) {
    	String s = url.toString();
    	return s.endsWith(".tar") || s.endsWith(".tar.gz") ||
    		s.endsWith(".tar.Z") || s.endsWith(".zip");
    }


    public void checkCacheStatus() {        
        if (scanResults == null) {
            return;
        }
        
        System.out.println("checking cache status...");
        scanResultsCacheStatus = new boolean[scanResults.length];
        for (int n=0; n<scanResults.length; n++) {
            scanResultsCacheStatus[n] = WCTTransfer.isInCache(scanResults[n].getUrl());
        }
        resultsList.repaint();
        System.out.println("done.");
    }
    

    private void animate() {
        if (viewer.getCurrentViewType() == WCTViewer.CurrentViewType.GOOGLE_EARTH ||
                viewer.getCurrentViewType() == WCTViewer.CurrentViewType.GOOGLE_EARTH_SPLIT_GEOTOOLS) {
            JOptionPane.showMessageDialog(this, 
                    "Animations are not currently supported in the Google Earth view", 
                    "Animation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
//        make a new instance each time because of the need to optionally add time morphing option
//        if (animator == null) {
            animator = new WCTAnimator(viewer);
//        }
        animator.setLocationRelativeTo(this);
        animator.setVisible(true);

    }

//    private void export() {
//
//        final DataSelector finalThis = this;
//
//        try {
//
//            foxtrot.Worker.post(new foxtrot.Task() {
//                public Object run() {
//
//                    try {
//
//                        exportGUI = viewer.getNexradExportGUI();
//                        if (exportGUI == null) {
//                            exportGUI = WCTExportGUI.createInstance(viewer, "Data Exporter");
//                        }
//                        viewer.getMapPaneZoomChange().setNexradFilterGUI(exportGUI.getFilterGUI());
//
//                        exportGUI.getDataSourcePanel().setDataType(finalThis.getDataSourcePanel().getDataType());
//                        exportGUI.getDataSourcePanel().setDataLocation(finalThis.getDataSourcePanel().getDataType(), finalThis.getDataSourcePanel().getDataLocation());
//                        exportGUI.getDataSourcePanel().setSortByComparator(finalThis.getDataSourcePanel().getDataType(), finalThis.getDataSourcePanel().getSortByComparator());
//                        exportGUI.setScanResults(scanResults);
//                        exportGUI.setSelectedIndices(getSelectedIndices());
//
//                        exportGUI.setVisible(true);
//                        exportGUI.setExtendedState(Frame.NORMAL);
//
//                        return "DONE";
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
////                        JOptionPane.showMessageDialog(finalThis, e.getMessage(), "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//                    	JOptionPane.showMessageDialog(viewer, 
//                    			"--- General Data Export Error ---\n"+
//                    			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
//                    			"Data Export Error", JOptionPane.ERROR_MESSAGE);
//
//                        return "ERROR";
//                    }
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
////            JOptionPane.showMessageDialog(finalThis, e.getMessage(), "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
//        	JOptionPane.showMessageDialog(viewer, 
//        			"--- General Data Export Error ---\n"+
//        			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
//        			"Data Export Error", JOptionPane.ERROR_MESSAGE);
//
//        }
//
//    }








    /**
     *  Returns currently selected datatype override selection.  Returns 'UNKNOWN' if 'Auto' is selected.
     * @return
     * @throws ClassNotFoundException
     */
    public SupportedDataType getSelectedDataType() throws ClassNotFoundException {        
        String dataType = jcomboDataType.getSelectedItem().toString().trim();
        return getDataType(dataType);
    }

    public static SupportedDataType getDataType(String dataType) throws ClassNotFoundException {
        if (dataType.equals("Auto")) {
            return SupportedDataType.UNKNOWN;
        }


        SupportedDataType[] dataTypes = getDataTypes();

        for (SupportedDataType type : dataTypes) {
            //          System.out.println(type.name());
            if (type.name().equalsIgnoreCase(dataType)) {
                //              System.out.println("FOUND DATA TYPE MATCH!!!");
                return type;
            }
        }
        throw new ClassNotFoundException("Data type '"+dataType+"' was not found.\n" +
                "Supported data types are: "+Arrays.deepToString(dataTypes));
    }

    public static SupportedDataType[] getDataTypes() throws ClassNotFoundException {        
        SupportedDataType[] dataTypes = (SupportedDataType[]) Class.forName("gov.noaa.ncdc.wct.io.SupportedDataType").getEnumConstants();
        return dataTypes;
    }


    public void setIsLoading(boolean isLoading) {

        if (resultsList.getSelectedIndices().length > 1 && 
        		! (getDataSourcePanel().getDataType().equals(WCTDataSourceDB.THREDDS) ||
        		getDataSourcePanel().getDataType().equals(WCTDataSourceDB.SINGLE_FILE))) {
        	
            jbDescribe.setEnabled(false);
            jbLoad.setEnabled(false);
            jbAnimate.setEnabled(! isLoading);
        }
        else {
            jbDescribe.setEnabled(! isLoading);
            //            jbLoad.setEnabled(! isLoading);
            jbAnimate.setEnabled(! isLoading);
            jbExport.setEnabled(! isLoading);
            resultsList.setEnabled(! isLoading);
        }
        
        if (isLoading) {
        	jbLoad.setText("Cancel");
        }
        else {
        	jbLoad.setText("Load");
        }
    }


    /**
     * Return status of Auto-Extent checkbox
     *
     * @return    The autoExtentSelected value
     */
    public boolean isAutoExtentSelected() {

        return jcbExtent.isSelected();
    }


    /**
     * Set status of Auto-Extent checkbox
     *
     * @param  selected  The new isAutoExtentSelected value
     */
    public void setIsAutoExtentSelected(boolean selected) {

        jcbExtent.setSelected(selected);
    }









    public void addFavorite(ScanResults scanResult) {
    	favoriteScanResultList.add(scanResult); 
//        listModel = WCTUiUtils.fillListModel(listModel, scanResults);
//        resultsList.setSelectedIndex(0);
    }

    public void setScanResults(ScanResults[] scanResults) {

        this.scanResults = scanResults;        
        if (scanResults == null) {
            return;
        }
//        System.out.println("filling list model...");
        listModel = WCTUiUtils.fillListModel(listModel, scanResults);
        resultsList.setModel(listModel);
//        System.out.println("done.");

//        System.out.println("checking cache status...");
        scanResultsCacheStatus = new boolean[scanResults.length];
        for (int n=0; n<scanResults.length; n++) {
            scanResultsCacheStatus[n] = WCTTransfer.isInCache(scanResults[n].getUrl());
        }
//        System.out.println("done.");

        
        System.out.println(dataSourcePanel.getDataLocation());
        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            resultsList.setCellRenderer(DEFAULT_RENDERER);
        }
        else {
            resultsList.setCellRenderer(cacheRenderer);
        }

        
        resultsList.setSelectedIndex(0);
    }

    public ScanResults[] getScanResults() {
        return scanResults;
    }

    public ArrayList<ScanResults> getFavoritesScanResultsList() {
        return favoriteScanResultList;
    }

    
    public ScanResultsComparator getSortByComparator() {
        return dataSourcePanel.getSortByComparator();
    }




    public URL[] getSelectedURLs() {

        URL[] urls;
        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
            URL url = null;
            String source = dataSourcePanel.getDataLocation();
            try {
                if (source.startsWith("http://") || source.startsWith("ftp://")) {
                    url = new URL(source);
                }
                else {
                    url = new File(source).toURI().toURL();
                }
            } catch (Exception e) {
                return null;
            }
            urls = new URL[] { url };
        }
        else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.THREDDS)) {
            return threddsPanel.getSelectedDataAccessURLArray();
        }
        else {
            int[] indices = resultsList.getSelectedIndices();
            for (int n=0; n<indices.length; n++) {
                indices[n] = resultsList.convertIndexToModel(indices[n]);
            }
            urls = new URL[indices.length];
            for (int n=0; n<urls.length; n++) {
            	if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.FAVORITES)) {
            		urls[n] = favoriteScanResultList.get(indices[n]).getUrl();
            	}
            	else {
            		urls[n] = scanResults[indices[n]].getUrl();
            	}
            }
        }



        //        System.out.println("DATA SELECTOR: - getSelectedURL(): return "+scanResults[resultsList.getSelectedIndex()].getUrl());
        return urls;
    }

    public int[] getSelectedIndices() {
    	int[] indices = null;
        if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
        	indices = new int[] { 0 };
        }
        else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.THREDDS)) {
            indices = threddsPanel.getSelectedIndices();
        }
        else {
        	indices = resultsList.getSelectedIndices();
        	for (int n=0; n<indices.length; n++) {
//            System.out.println("converting index: "+indices[n]+" to "+resultsList.convertIndexToModel(indices[n]));
        		indices[n] = resultsList.convertIndexToModel(indices[n]);
        	}
        }
        return indices;
    }


    public DataSourcePanel getDataSourcePanel() {
        return dataSourcePanel;
    }
    
    public ThreddsPanel getThreddsPanel() {
        return threddsPanel;
    }

    public DefaultListModel getListModel() {
        return listModel;
    }

    public JXList getResultsList() {
        return resultsList;
    }


    /**
     * Executes the submit for listing files.  If this is for a single file/url source,
     * then the actual data will be loaded into the viewer rather than a file listing.
     */
    public void submitListFiles() {
        final String dataType = dataSourcePanel.getDataType();
        //            dataSourcePanel.setStatus("Listing Data ...", dataType);

        System.out.println("---- "+dataType);

        jbLoad.setEnabled(true);
        jbAnimate.setEnabled(true);
        jbExport.setEnabled(true);

        try {

            // 1st - check for special case of single file
            if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
                String source = dataSourcePanel.getDataLocation();
                URL url = null;
                if (source.startsWith("http://") || source.startsWith("ftp://")) {
                    url = new URL(source);
                }
                else {
                    url = new File(source).toURI().toURL();
                }

                loadData(url);
                return;
            }
            
            if (dataType.equals(WCTDataSourceDB.THREDDS)) {
                getContentPane().remove(resultsListScrollPane);
                getContentPane().remove(threddsPanel);
                getContentPane().remove(bottomPanel);

                int width = getContentPane().getWidth();
                
//System.out.println("1: "+threddsPanel.getSize());                    
//System.out.println("1: "+threddsPanel.getPreferredSize());

                String catalogLink = dataSourcePanel.getDataLocation().replaceAll(".html", ".xml");
                if (catalogLink.endsWith("/")) {
                    catalogLink += "catalog.xml";
                }
                else if (catalogLink.endsWith("/thredds")) {
                    catalogLink += "/catalog.xml";
                }
                dataSourcePanel.setDataLocation(WCTDataSourceDB.THREDDS, catalogLink);

                threddsPanel.process(dataSourcePanel.getDataLocation());
                
                // Update history
                dataSourcePanel.updateHistory();
                
                
                getContentPane().add(threddsPanel, BorderLayout.CENTER);
                getContentPane().add(bottomPanel, BorderLayout.SOUTH);
                validate();
                
                int height = threddsPanel.getPreferredSize().height;
                if (height < 300) {
                	height = 300;
                }
                threddsPanel.setPreferredSize(new Dimension(width, height));
                threddsPanel.setSize(new Dimension(width, height));
                validate();
                pack();
                
                
                return;
            }


            if (dataSourcePanel.getDataLocation().trim().length() == 0) {

                if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.NCDC_HAS_FTP) ||
                        dataSourcePanel.getDataType().equals(WCTDataSourceDB.CLASS_ORDER)) {
                    throw new Exception("Please enter a valid order number.");
                }
                else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
                    throw new Exception("Please enter a valid file location.");
                }
                else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.URL_DIRECTORY)) {
                    throw new Exception("Please enter a valid directory URL.");
                }
                else if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.THREDDS)) {
                    throw new Exception("Please enter a valid 'catalog.xml' URL.");
                }
            }
            if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.THREDDS) && 
                    ! dataSourcePanel.getDataLocation().trim().endsWith("catalog.xml")) {
                throw new Exception("Please enter a valid 'catalog.xml' URL.");
            }

            //              From: http://foxtrot.sourceforge.net/docs/tips.php
            //              Avoid the temptation to modify anything from inside Job.run(). 
            //              It should just take data from outside, perform some heavy operation 
            //              and return the result of the operation.
            //              The pattern to follow in the implementation of Job.run() is Compute and Return, see example below.  


            final WCTDirectoryScanner dirScanner = new WCTDirectoryScanner();
            dirScanner.addProgressListener(new GeneralProgressListener() {
                public void ended(GeneralProgressEvent event) {
                    //                        System.out.println("SCAN ENDED: "+event.getStatus());
                    dataSourcePanel.setStatus(event.getStatus(), dataType);
                    dataSourcePanel.setShowLoadingIcon(false, dataType);
                }
                public void progress(GeneralProgressEvent event) {
                    //                        System.out.println("SCAN PROGRESS: "+event.getStatus());
                    dataSourcePanel.setStatus(event.getStatus(), dataType);
                }
                public void started(GeneralProgressEvent event) {
                    //                        System.out.println("SCAN STARTED: "+event.getStatus());
                    dataSourcePanel.setStatus(event.getStatus(), dataType);
                    dataSourcePanel.setShowLoadingIcon(true, dataType);
                }                    
            });

            if (dataSourcePanel.getDataType().equals(WCTDataSourceDB.URL_DIRECTORY) && 
                    dataSourcePanel.getDataLocation().startsWith("ftp://tgftp.nws.noaa.gov/SL.us008001")) {

                dataSourcePanel.setSortByComparator(WCTDataSourceDB.URL_DIRECTORY, 
                        new ScanResultsComparator(ScanResultsComparator.CompareBy.TIMESTAMP_DESC)
                );
            }

            ScanResults[] scanResults = (ScanResults[])foxtrot.ConcurrentWorker.post(new foxtrot.Task() {
                @Override
                public Object run() throws Exception {                        
                    return dirScanner.listFiles(dataSourcePanel.getDataType(), dataSourcePanel.getDataLocation(), 
                            dataSourcePanel.getSortByComparator(), dataSourcePanel.getListAllFiles());
                }
            });
            
            if (scanResults.length == 0 && ! dataSourcePanel.getListAllFiles() && 
            		! dataSourcePanel.getDataType().equals(WCTDataSourceDB.NCDC_HAS_FTP) &&
            		! dataSourcePanel.getDataType().equals(WCTDataSourceDB.CLASS_ORDER)
            	) {
            	
            	dataSourcePanel.setListAllFiles(dataType, true);
            	scanResults = (ScanResults[])foxtrot.ConcurrentWorker.post(new foxtrot.Task() {
                    @Override
                    public Object run() throws Exception {                        
                        return dirScanner.listFiles(dataSourcePanel.getDataType(), dataSourcePanel.getDataLocation(), 
                                dataSourcePanel.getSortByComparator(), true);
                    }
                });
            }
            

            dirScanner.clearProgressListeners();


            //                System.out.println("SCAN RESULTS: "+Arrays.deepToString(scanResults));
            setScanResults(scanResults);


            if (scanResults.length > 0) {
                getContentPane().remove(threddsPanel);
                getContentPane().remove(bottomPanel);
                getContentPane().add(resultsListScrollPane, BorderLayout.CENTER);
                getContentPane().add(bottomPanel, BorderLayout.SOUTH);
                pack();

                // Update history
                dataSourcePanel.updateHistory();

            }
            else {
                dataSourcePanel.setShowLoadingIcon(false, dataSourcePanel.getDataType());
                if (dataSourcePanel.getDataType() == WCTDataSourceDB.NCDC_HAS_FTP) {
                    javax.swing.JOptionPane.showMessageDialog(this, "No valid data found for order number: "+
                            dataSourcePanel.getDataLocation()+"\nNote that HAS and CLASS orders expire after 7 days.", 
                            "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                else if (dataSourcePanel.getDataType() == WCTDataSourceDB.CLASS_ORDER) {
                    javax.swing.JOptionPane.showMessageDialog(this, "No valid data found for order number: "+
                            dataSourcePanel.getDataLocation()+"\nNote that HAS and CLASS orders expire after 7 days.", 
                            "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                else {
                    javax.swing.JOptionPane.showMessageDialog(this, "No data found for: "+dataSourcePanel.getDataLocation(), 
                            "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
                }

                getContentPane().remove(resultsListScrollPane);
                getContentPane().remove(bottomPanel);
                pack();
            }


        } catch (Exception e) {
            e.printStackTrace();
            //                JOptionPane.showMessageDialog(parent, "ADD DATA ERROR:\n"+e.getMessage(), "ADD DATA ERROR", JOptionPane.ERROR_MESSAGE);   

            dataSourcePanel.setShowLoadingIcon(false, dataSourcePanel.getDataType());
            dataSourcePanel.setStatus("", dataType);

            if (dataSourcePanel.getDataType() == WCTDataSourceDB.NCDC_HAS_FTP ||
                    dataSourcePanel.getDataType() == WCTDataSourceDB.CLASS_ORDER) {
                javax.swing.JOptionPane.showMessageDialog(this, "No order found for: "+
                        dataSourcePanel.getDataLocation()+"\nNote that HAS and CLASS orders expire after 7 days.", 
                        "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            else {
                javax.swing.JOptionPane.showMessageDialog(this, "No data found for: "+dataSourcePanel.getDataLocation(), 
                        "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            getContentPane().remove(resultsListScrollPane);
            getContentPane().remove(bottomPanel);

        }


    }













    class DataListComparator implements Comparator<ScanResults> {
        /**
         * Compare based on filename
         */
        //        @Override
        public int compare(ScanResults sr1, ScanResults sr2) {
            return sr1.getFileName().compareTo(sr2.getFileName());
        }
    }










    class SubmitButtonListener implements ActionListener {
        public SubmitButtonListener() {
        }
        public void actionPerformed(ActionEvent evt) {
            submitListFiles();
        }
    }








    final public class KeyHandler implements KeyListener {

        /**
         *  Implementation of KeyListener interface.
         *
         * @param  e  Description of the Parameter
         */
        public void keyPressed(KeyEvent e) {
            //          System.out.println(e.getKeyCode()+"  PRESSED");
        }
        /**
         *  Implementation of KeyListener interface.
         *
         * @param  e  Description of the Parameter
         */
        public void keyReleased(KeyEvent e) {
                      System.out.println(e.getKeyCode()+"  ESCAPE=" + KeyEvent.VK_ESCAPE+" ENTER="+KeyEvent.VK_ENTER+" D="+KeyEvent.VK_D);
                      System.out.println(e.getModifiers()+"  CTRL=" + KeyEvent.CTRL_DOWN_MASK);
                      
                      
                      
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
            }
            else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                loadData(getSelectedURLs()[0]);
            }
            else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_D) {                
                DataSelectionUtils.showDump(viewer, viewer.getDataSelector(), getSelectedURLs()[0]);
            }
            e.consume();
        }

        /**
         *  Implementation of KeyListener interface.
         *
         * @param  e  Description of the Parameter
         */
        public void keyTyped(KeyEvent e) {
        }

    }


    final class ClickHandler implements MouseListener {

        public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && resultsList.isEnabled()) {
//                loadData(getSelectedURLs()[0]);
                loadData();
            }
        }

        public void mouseEntered(MouseEvent arg0) {
        }

        public void mouseExited(MouseEvent arg0) {
        }

        public void mousePressed(MouseEvent arg0) {
        }

        public void mouseReleased(MouseEvent arg0) {
        }

    }



    final class FilterKeyListener implements KeyListener {

        private final DataSelector parent;
        private PatternFilter patternFilter, patternFilterTHREDDS;

        public FilterKeyListener(DataSelector parent) {
            this.parent = parent;
        }


        @Override
        public void keyPressed(KeyEvent e) {
        }
        @Override
        public void keyReleased(KeyEvent e) {
            // lazy creation to allow jxlist to be set up, filters enabled, etc...
            if (patternFilter == null) {
                patternFilter = new PatternFilter();
                patternFilterTHREDDS = new PatternFilter();
                resultsList.setFilters(new FilterPipeline(patternFilter));
//                threddsPanel.setFilters(new FilterPipeline(patternFilterTHREDDS));
            }            
            String text = ((JTextField) e.getSource()).getText();
            StringBuilder regularExpr = new StringBuilder();
            for (char c : text.toCharArray()) {
                regularExpr.append("[").append(Character.toLowerCase(c));
                regularExpr.append(Character.toUpperCase(c)).append("]");
            }
            patternFilter.setPattern(regularExpr.toString(), 0);
//            patternFilterTHREDDS.setPattern(regularExpr.toString(), 0);
        }
        @Override
        public void keyTyped(KeyEvent e) {			
        }    	
    }

    
    
    
    
    private class TextSelection implements Transferable {

        private ArrayList textFlavors = new ArrayList();
        private String text;

        public TextSelection(String text) {
            this.text = text;
            
            
            try {
                textFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
                textFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) textFlavors.toArray(new DataFlavor[textFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return textFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

            if (String.class.equals(flavor.getRepresentationClass())) {
                return text;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(text);
            }
            throw new UnsupportedFlavorException(flavor);

        }

    } 


    
    
    
    
    
    

    private class FavoritesActionListener implements ActionListener {
    	
    	Component parent = null;
    	
    	public FavoritesActionListener(Component parent) {
    		this.parent = parent;
    	}
    	

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Save List")) {
				saveFavoritesList();
			}
			else if (e.getActionCommand().equals("Load List")) {
				loadFavoritesList();
			}
//			else if (e.getActionCommand().equals("Bundle")) {
////				createFavoritesBundle();
//			}
			else if (e.getActionCommand().equals("Remove")) {
				removeFromFavoritesList();
			}
//			else if (e.getActionCommand().equals("Remove All")) {
////				removeAllFromFavoritesList();
//			}
			else if (e.getActionCommand().equals("Add to Favorites")) {
				addFavorite();
			}
		}
		
		
		public void loadFavoritesList() {

	        String wctprop = WCTProperties.getWCTProperty("favoriteListLocation");
	        File lastSaveFolder = null;
	        if (wctprop != null) {
	            lastSaveFolder = new File(wctprop);
	        }
	        else {
	            lastSaveFolder = new File(System.getProperty("user.home"));
	        }

			JFileChooser fc = new JFileChooser(lastSaveFolder);
	        fc.setDialogTitle("Open a Favorites List");
			fc.setAcceptAllFileFilterUsed(true);
			OpenFileFilter wctfavsFilter = new OpenFileFilter("wctfavs", true, "NOAA Weather and Climate Toolkit Favorites");
			fc.addChoosableFileFilter(wctfavsFilter);
	        fc.setFileFilter(wctfavsFilter);

	        
	        
	        int returnVal = fc.showOpenDialog(parent);
	        fc.setDialogType(JFileChooser.SAVE_DIALOG);
	        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            lastSaveFolder = fc.getSelectedFile().getParentFile();
	    		try {
					FavoritesManager.getInstance().loadFavorites(fc.getSelectedFile(), LoadAction.APPEND);
					
					ArrayList<Favorite> favList = FavoritesManager.getInstance().getCurrentFavoriteList();
					favoriteScanResultList.clear();
					for (Favorite fav : favList) {
						ScanResults scan = new ScanResults();
						scan.setDisplayName(fav.getDisplayString());
						scan.setLongName(fav.getDisplayString());
						scan.setUrl(fav.getDataURL());
						favoriteScanResultList.add(scan);
						
						
//						System.out.println("LOADING FAVORITE: "+scan);
					}
					
					
		            listModel = WCTUiUtils.fillListModel(listModel, 
		            		(ScanResults[])favoriteScanResultList.toArray(new ScanResults[favoriteScanResultList.size()]));
		            resultsList.setModel(listModel);
		            resultsList.setSelectedIndex(0);
					
					
		            FavoritesManager.getInstance().saveCurrentFavoritesToProfile();
		            
		            
				} catch (IOException e) {
					JOptionPane.showMessageDialog(parent, "Error loading favorites from file: "+fc.getSelectedFile(), "Load Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				WCTProperties.setWCTProperty("favoriteListLocation", lastSaveFolder.toString());
	        }
	        

		}
		
		public void saveFavoritesList() {
			
	        String wctprop = WCTProperties.getWCTProperty("favoriteListLocation");
	        File lastSaveFolder = null;
	        if (wctprop != null) {
	            lastSaveFolder = new File(wctprop);
	        }
	        else {
	            lastSaveFolder = new File(System.getProperty("user.home"));
	        }

			
			JFileChooser fc = new JFileChooser(lastSaveFolder);
	        fc.setDialogTitle("Set Save Location for Favorites List");
			fc.setAcceptAllFileFilterUsed(true);
			OpenFileFilter wctfavsFilter = new OpenFileFilter("wctfavs", true, "NOAA Weather and Climate Toolkit Favorites");
			fc.addChoosableFileFilter(wctfavsFilter);
			fc.setFileFilter(wctfavsFilter);

	        int returnVal = fc.showSaveDialog(parent);
			File file = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				int choice = JOptionPane.YES_OPTION;
				file = fc.getSelectedFile();

				WCTProperties.setWCTProperty("favoriteListLocation", file.toString());

				File extfile = null;
				String fstr = file.toString();

				// Add extension if needed
				if (!fstr.substring((int) fstr.length() - 8, (int) fstr.length()).equals(".wctfavs")) {
					extfile = new File(file + ".wctfavs");
				}
				else {
					extfile = file;
					file = new File(fstr.substring(0, (int) fstr.length() - 4));
				}

				// Check for existing file
				if (file.exists()) {
					String message = "The NOAA Weather and Climate Toolkit Favorites List File \n" +
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
					String message = "The NOAA Weather and Climate Toolkit Favorites List File \n" +
					"<html><font color=red>" + extfile + "</font></html>\n" +
					"already exists.\n\n" +
					"Do you want to proceed and OVERWRITE?";
					choice = JOptionPane.showConfirmDialog(null, (Object) message,
							"OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
				}
	        
	        
				if (choice == JOptionPane.YES_OPTION) {
		    		try {
						FavoritesManager.getInstance().saveFavorites(extfile);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(parent, "Error saving favorites to file: "+fc.getSelectedFile(), "Save Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}

			}	        
		}
	    
		
		
	    public void removeFromFavoritesList() {
	    	int[] selectedIndices = getSelectedIndices();
	    	// following from: http://stackoverflow.com/questions/4950678/remove-multiple-elements-from-arraylist
	    	List<Integer> indexList = Arrays.asList(ArrayUtils.toObject(selectedIndices));
	    	Collections.sort(indexList, Collections.reverseOrder());
	    	for (int idx : indexList) {
	    		favoriteScanResultList.remove(idx);
	    		FavoritesManager.getInstance().getCurrentFavoriteList().remove(idx);
	    	}

	    	
	    	System.out.println("----------------");
	    	System.out.println(favoriteScanResultList.size()+" "+favoriteScanResultList);
	    	System.out.println(FavoritesManager.getInstance().getCurrentFavoriteList().size()+" "+FavoritesManager.getInstance().getCurrentFavoriteList());
	    	System.out.println("----------------");
	    	
	    	
            listModel = WCTUiUtils.fillListModel(listModel, 
            		(ScanResults[])favoriteScanResultList.toArray(new ScanResults[favoriteScanResultList.size()]));
            resultsList.setModel(listModel);
            resultsList.setSelectedIndex(0);
            try {
				FavoritesManager.getInstance().saveCurrentFavoritesToProfile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
    }
    
}

