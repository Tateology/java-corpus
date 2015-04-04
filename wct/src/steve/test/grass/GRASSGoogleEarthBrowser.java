package steve.test.grass;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.RoundedBorder;
import gov.noaa.ncdc.gis.kml.KMLGeometryConverter;
import gov.noaa.ncdc.gis.kml.KMLUtils;
import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.nexradiv.ClipboardImageTransfer;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities;
import gov.noaa.ncdc.wct.ui.ViewerUtilities;
import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.net.BindException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.jdesktop.swingx.border.DropShadowBorder;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

/**
 * 
 * Java wrapper for javascript Google Earth plugin.
 * Basic flow of events:
 * 
 * 1) Network link is established that listens for changes in Google Earth view.
 *    This is only applicable for the 'GOOGLE_EARTH' view controller type.  Otherwise,
 *    we wait for a refresh call from the external Geotools controller.
 *    
 * 2)  'refreshView' method will generate KML that describes the updated view (extent,
 *    image overlay, legend, model) and fly the Google Earth instance to that
 *    extent.
 *   
 * 
 * @author steve.ansari
 *
 */

public class GRASSGoogleEarthBrowser extends JPanel {

    private static final String LS = System.getProperty("line.separator");   

    final private JWebBrowser webBrowser = new JWebBrowser();
    private WCTViewer viewer = null;
    
    private boolean autoFlyTo = true;
    private double layerOpacity = 1.0;
    
    public static enum ViewController { GOOGLE_EARTH, GEOTOOLS };
    public static enum HeightMode { REAL_3D, DRAPE_2D, BOTH };
//    private final static ViewController VIEW_CONTROLLER = ViewController.GEOTOOLS;

    private JRadioButton jrb2dButton = null;
    private JRadioButton jrb3dButton = null;
    private JRadioButton jrbBothButton = null;
    
    private ViewController viewController = ViewController.GOOGLE_EARTH;    
    private int port;
    private boolean includeStatusPanel = true;
    private Frame parent = null;
    private boolean showLegend = true;
    private HeightMode heightMode = HeightMode.BOTH;
    private double elevationExaggeration = 1;
    private double gridHeightInMeters = Double.NaN;
    
    
    private boolean bordersVisible = true;
    private boolean roadsVisible = true;
    private boolean buildingsVisible = false;
    private boolean terrainVisible = true;
    private boolean sunVisible = false;
    private boolean atmosphereVisible = true;
    private boolean scaleVisible = true;
    private boolean overviewVisible = false;
    private boolean gridVisible = false;
    
    private JDialog layersDialog;

    private RenderCompleteListener rcl = new RenderCompleteListener() {
        @Override
        public void renderComplete() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	initView();
                }
            });
        }
        @Override
        public void renderProgress(int progress) {
        }
    };

    
    
    /* Standard main method to try that test as a standalone application. */
//    public static void main(String[] args) {
//        UIUtils.setPreferredLookAndFeel();
//        NativeInterface.open();
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                JFrame frame = new WCTFrame("WCT Google Earth View");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                
//                GoogleEarthTest ge = new GoogleEarthTest();
//                
//                
//                frame.getContentPane().add(ge.getPanel(), BorderLayout.CENTER);
//                frame.setSize(800, 600);
//                frame.setLocationByPlatform(true);
//                frame.setVisible(true);
//            }
//        });
//        NativeInterface.runEventPump();
        
//        createInstance(WCTImageServer.DEFAULT_PORT, );
//    }

    
    
    
    /**
     * @param port port for http server
     * @param viewController who is controlling this view/
     * @param includeStatusPanel include the status/control bar in the bottom.  
     * @param parent parent frame for dialog popups in the status/control bar.  Can be null if 'includeStatusPanel' is false. 
     * @param showLegend
     */
    public GRASSGoogleEarthBrowser(int port, ViewController viewController, boolean includeStatusPanel, 
            Frame parent, boolean showLegend) {
        
        this.port = port;
        this.viewController = viewController;
        this.includeStatusPanel = includeStatusPanel;
        this.parent = parent;
        this.showLegend = showLegend;
        init();
    }


    
    
    public static void createFrame(final GRASSGoogleEarthBrowser geBrowser, final JFrame parent) {
        
        parent.setSize(800, 600);
        geBrowser.setSize(800, 600);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                parent.setSize(800, 600);
                parent.setLocationByPlatform(true);
                parent.setVisible(true);
                parent.getContentPane().setLayout(new BorderLayout());
                parent.getContentPane().add(geBrowser, BorderLayout.CENTER);
//                geFrame.getContentPane().add(geBrowser.createControlPanel(geFrame), BorderLayout.SOUTH);

                
                
            }
        });
    }
    
    private void init() {
//        foxtrot.Worker.post(new foxtrot.Job() {
//            public Object run() {
//            	NativeInterface.initialize();
//            	NativeInterface.open();
//            	return "DONE";
//            }
//        });
//    	NativeInterface.initialize();
//    	NativeInterface.open();
        createUI();
        
//        foxtrot.Worker.post(new foxtrot.Job() {
//            public Object run() {
//                try {
//                    NativeInterface.runEventPump();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            	return "DONE";
//            }
//        });

//        try {
//            NativeInterface.runEventPump();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    
    public void shutdown() {
//        ge.getWebBrowser().disposeNativePeer();

//        try {
//            viewer.setCurrentViewType(CurrentViewType.GEOTOOLS);
//        } catch (BindException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        NativeInterface.close();
    }
    
    private void createUI() {

//        this.getContentPane().add(this.getPanel(), BorderLayout.CENTER);
        this.setLayout(new BorderLayout());
        this.add(this.getPanel(), BorderLayout.CENTER);
        if (this.includeStatusPanel) {
        	JPanel bottomPanel = createControlPanel(this.parent);
//            bottomPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.add(bottomPanel, BorderLayout.SOUTH);
        }
        
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    }
    

    public JPanel createControlPanel(final Frame parent) {
        
        final JCheckBox jcbAutoFlyTo = new JCheckBox("Auto Fly-To?", true);
        jcbAutoFlyTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("event: jcbAutoFlyTo: "+jcbAutoFlyTo.isSelected());
                autoFlyTo = jcbAutoFlyTo.isSelected();
            }
        });
        
        final JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                refreshView(autoFlyTo);  
            }
        });
        
        String text = (viewController == ViewController.GEOTOOLS) ? ViewController.GOOGLE_EARTH.toString() : ViewController.GEOTOOLS.toString();
        final JButton toggleViewButton = new JButton(text);
        toggleViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleViewButton.getText().equals(ViewController.GEOTOOLS.toString())) {
                    setViewController(ViewController.GEOTOOLS);
                    viewer.setCurrentExtent(viewer.getMaxExtent());
                    toggleViewButton.setText(ViewController.GOOGLE_EARTH.toString());
                }
                else if (toggleViewButton.getText().equals(ViewController.GOOGLE_EARTH.toString())) {
                    setViewController(ViewController.GOOGLE_EARTH);
                    toggleViewButton.setText(ViewController.GEOTOOLS.toString());
                }
            }
        });
        
        final JSlider transSlider = new JSlider(0, 100, 100);
        transSlider.addChangeListener(new ChangeListener() {
        	int lastValue = 0;
            @Override
            public void stateChanged(ChangeEvent e) {
            	// we divide and multiply to round to the nearest 4th interval in the range
            	if ((transSlider.getValue()/4)*4 != lastValue) {            	
            		// webBrowser.executeJavascript("alert(ge.getFeatures().getLastChild().getChildNodes().getLength());");
            		layerOpacity = transSlider.getValue()/100.0;
            		webBrowser.executeJavascript("ge.getFeatures().getLastChild().setOpacity("+layerOpacity+");");
            		lastValue = (transSlider.getValue()/4)*4;
            	}
            }
        });
        transSlider.setPreferredSize(new Dimension(100, (int)transSlider.getPreferredSize().getHeight()));

        final JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyViewToClipboard();
			}        	
        });

        final JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewerUtilities.saveComponentImageChooserWithSWTScreenshot(webBrowser);
			}        	
        });
        
        final JButton layersButton = new JButton("Options");
        layersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLayersDialog(parent);
            }
        });
        
        
   

        JPanel bottomPanel = new JPanel(new RiverLayout(10, 2));
        DropShadowBorder dropShadowBorder = new DropShadowBorder(Color.BLACK, 4, 0.8f, 5, false, false, true, true);
        Border border2 = BorderFactory.createCompoundBorder(dropShadowBorder, BorderFactory.createEmptyBorder(2, 2, 0, 0));
        //        Border mainBorder = BorderFactory.createCompoundBorder(border2, new LineBorder(new Color(70, 70, 70), 1, true));
        Border mainBorder = BorderFactory.createCompoundBorder(border2, new RoundedBorder(new Color(10, 36, 106, 150), 2, 2));
        bottomPanel.setBorder(mainBorder);
        
        
        JPanel controlPanel = new JPanel();
        controlPanel.add(jcbAutoFlyTo);
//        controlPanel.add(refreshButton, "left");
        controlPanel.add(new JLabel("Transparency: "));
        
//        JPanel transSliderPanel = new JPanel();
//        transSliderPanel.add(transSlider);
//        transSliderPanel.setBorder(WCTUiUtils.myTitledTopBorder("Transparency:", 0, 0, 0, 0, TitledBorder.CENTER, TitledBorder.ABOVE_TOP));
        
        controlPanel.add(transSlider);
//        controlPanel.add(buttonPanel);
//        controlPanel.add(layersButton);
        
        
        bottomPanel.add(controlPanel, "left");
        
//        bottomPanel.add(buttonPanel, "left br");
//        bottomPanel.add(new JLabel("Elevation Exaggeration: "));
//        bottomPanel.add(elevExSpinner);
        bottomPanel.add(new JLabel(""), "hfill");
        
////        controlPanel.add(toggleViewButton, "right");
        bottomPanel.add(copyButton, "right");
        bottomPanel.add(saveButton, "right");
        bottomPanel.add(layersButton, "right");
        
        
//        return controlPanel;
        return bottomPanel;


    }
    
    /**
     * Copy the view image to system clipboard
     */
    public void copyViewToClipboard() {
        ClipboardImageTransfer cit = new ClipboardImageTransfer();
        cit.copyImageToClipboard(ViewerUtilities.getComponentImageWithSWTScreenshot(webBrowser));
    }
    
    public void showLayersDialog(Frame parent) {
        if (layersDialog == null) {
            layersDialog = new JDialog(parent, "Google Earth Layer Selector", false);
            layersDialog.add(createLayerSelectionPanel());
            
            JRootPane rootPane = layersDialog.getRootPane();
            InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

            ActionMap aMap = rootPane.getActionMap();
            aMap.put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    layersDialog.dispose();
                }
            });

        }
        
        
        layersDialog.pack();
        layersDialog.setLocation(parent.getX()+20, parent.getY()+20);
        layersDialog.setVisible(true);
    }
    

    public JPanel getPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        final JPanel webBrowserPanel = new JPanel(new BorderLayout());
        webBrowserPanel.setBackground(Color.BLACK);
//        webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
//        webBrowserPanel.setBorder(BorderFactory.createEmptyBorder());
        
//        webBrowserPanel.addMouseMotionListener(new MouseMotionListener() {
//            @Override
//            public void mouseDragged(MouseEvent e) {
//            }
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                System.out.println("mouse moved: "+e.toString());
//            }
//        });
//        
//        webBrowserPanel.addMouseListener(new MouseAdapter() {
//            public void mouseEntered(MouseEvent e) {
//                System.out.println("Mouse entered");
////                webBrowserPanel.requestFocus();
//                webBrowser.transferFocus();
//                webBrowser.requestFocus();
//                webBrowser.grabFocus();
//            }
//            public void mouseExited(MouseEvent e) {
//                System.out.println("Mouse exited");
//            }
//        });
        
        
        webBrowser.setBackground(Color.BLACK);
        webBrowser.setBorder(BorderFactory.createEmptyBorder());
        webBrowser.setButtonBarVisible(false);
        webBrowser.setBarsVisible(false);
        webBrowser.setStatusBarVisible(false);
        webBrowser.addWebBrowserListener(new WebBrowserListener() {
            @Override
            public void commandReceived(WebBrowserCommandEvent e) {
                System.out.println("WEB BROWSER EVENT -- commandReceived:  "+e.getCommand());
                if (viewer != null && e.getCommand().equals("ge load complete")) {
                    try {
                        if (viewer.getCurrentDataURL() != null) {
                            viewer.getStatusBar().setProgressText("Google Earth Initialization...");
                        }
                        
                        System.out.println("ge load : viewer extent: "+viewer.getCurrentExtent());
                        flyToExtent(viewer.getCurrentExtent());
                        if (includeStatusPanel) {
                            refreshView();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
//                System.out.println("WEB BROWSER EVENT -- ladingProgressChanged:  "+e.getWebBrowser().getLoadingProgress());
            }
            @Override
            public void locationChangeCanceled(WebBrowserNavigationEvent arg0) {
            }
            @Override
            public void locationChanged(WebBrowserNavigationEvent arg0) {
            }
            @Override
            public void locationChanging(WebBrowserNavigationEvent arg0) {
            }
            @Override
            public void statusChanged(WebBrowserEvent e) {
//                System.out.println("WEB BROWSER EVENT -- statusChange:  "+e.getWebBrowser().getStatusText());
            }
            @Override
            public void titleChanged(WebBrowserEvent arg0) {
            }
            @Override
            public void windowClosing(WebBrowserEvent arg0) {
            }
            @Override
            public void windowOpening(WebBrowserWindowOpeningEvent arg0) {
            }
            @Override
            public void windowWillOpen(WebBrowserWindowWillOpenEvent arg0) {
            }
        });
        webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
        panel.add(webBrowserPanel, BorderLayout.CENTER);

        webBrowser.setSize(800, 600);
        webBrowserPanel.setSize(800, 600);
        panel.setSize(800, 600);
//        webBrowser.setHTMLContent(getHTML());
        
        
        System.out.println("steve 1");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				webBrowser.setHTMLContent(getHTML());
			}
		});
		System.out.println("steve 2");
		webBrowser.navigate("http://www.google.com/earth/explore/products/plugin.html");
        
//        webBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        webBrowserPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        webBrowser.getNativeComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return panel;
    }
    
    public void refreshView() {
        refreshView(autoFlyTo);
    }
    
    public void refreshView(boolean flyToExtent) {

        Rectangle2D.Double extent = this.viewer.getCurrentExtent();
        
//        webBrowser.executeJavascript("refreshImage("+extent.getMinX()+","+extent.getMinY()+","+extent.getMaxX()+","+extent.getMaxY()+");");
        
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("'<?xml version=\"1.0\" encoding=\"UTF-8\"?>            '+ \n");
        sb.append("'<kml xmlns=\"http://www.opengis.net/kml/2.2\"         '+ \n");
        sb.append("'     xmlns:gx=\"http://www.google.com/kml/ext/2.2\"   '+ \n");
        sb.append("'     xmlns:kml=\"http://www.opengis.net/kml/2.2\"     '+ \n");
        sb.append("'     xmlns:atom=\"http://www.w3.org/2005/Atom\">      '+ \n");
        sb.append("'<Document>                                 '+ \n");
        sb.append("'<Folder>                                   '+ \n");
        
        
        KMLGeometryConverter kmlConv = new KMLGeometryConverter();
        FeatureIterator iter = viewer.getMarkerFeatures().features();
        while (viewer.getMarkerEditor() != null &&
        		viewer.getMarkerEditor().getMarkerVisibility() && iter.hasNext()) {
        	try {
        		Feature feature = iter.next();
        		sb.append("'    <Placemark> '+\n");
        		sb.append("'      <name>"+feature.getAttribute("label1")+"</name> '+\n");
        		sb.append(convertToJavascriptString(kmlConv.processGeometry(feature.getDefaultGeometry())));
        		sb.append("'    </Placemark> '+\n");
        		
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }

        
        
        
        if (heightMode == HeightMode.DRAPE_2D || heightMode == HeightMode.BOTH) { 
        	sb.append("'<GroundOverlay>                                 '+ \n");
        	sb.append("' <name>WCT Image Overlay</name>                 '+ \n");
        	sb.append("' <Icon>                                         '+ \n");
        	sb.append("'     <href>http://localhost:"+port+"/image?cacheBuster="+(int)(Math.random()*10000000)+"</href>   '+ \n");
        	sb.append("'     <viewBoundScale>0.75</viewBoundScale>      '+ \n");
        	sb.append("' </Icon>                                        '+ \n");
        	sb.append("' <altitude>0</altitude> '+ \n");
        	sb.append("' <altitudeMode>clampToGround</altitudeMode> '+ \n");
        	sb.append("' <LatLonBox>                                    '+ \n");
        	sb.append("'     <north>"+extent.getMaxY()+"</north>        '+ \n");
        	sb.append("'     <south>"+extent.getMinY()+"</south>        '+ \n");
        	sb.append("'     <east>"+extent.getMaxX()+"</east>          '+ \n");
        	sb.append("'     <west>"+extent.getMinX()+"</west>          '+ \n");
        	sb.append("' </LatLonBox>                                   '+ \n");
        	sb.append("'<LookAt>                                                            '+\n");
        	sb.append("'  <longitude>"+extent.getCenterX()+"</longitude>  '+\n");
        	sb.append("'  <latitude>"+extent.getCenterY()+"</latitude>    '+\n");
        	sb.append("'  <altitude>1000</altitude>               '+\n");
        	//        sb.append("'  <heading>0</heading>                 '+\n");
        	//        sb.append("'  <tilt>0</tilt>                       '+\n");
        	sb.append("'  <range>"+ (Math.max(extent.getWidth(), extent.getHeight()) *120*1000) +"</range>   '+\n");
        	sb.append("'  <altitudeMode>clampToGround</altitudeMode>         '+\n");
        	sb.append("'</LookAt>                              '+\n");
        	sb.append("'</GroundOverlay>                                '+ \n");
        }
        
        
        if (showLegend) {
            sb.append("'<ScreenOverlay>                                 '+ \n");
            sb.append("' <name>WCT Legend Overlay</name>                '+ \n");
            sb.append("' <Icon>                                         '+ \n");
            sb.append("'     <href>http://localhost:"+port+"/legend?cacheBuster="+(int)(Math.random()*10000000)+"</href>   '+ \n");
            sb.append("'     <viewBoundScale>0.75</viewBoundScale>      '+ \n");
            sb.append("' </Icon>                                        '+ \n");
            sb.append("' <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> '+ \n");
            sb.append("' <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> '+ \n");
            sb.append("'</ScreenOverlay>                                 '+ \n");
        }
        
        try {

            String kmlGeomOutline = KMLUtils.getExtentLineKML(extent);
            String kmlGeomBackground = KMLUtils.getExtentPolyKML(extent);

            sb.append("'    <Placemark>                            '+\n");

//          sb.append("<LookAt id="ID">                                                            ");
            sb.append("'<LookAt>                                                            '+\n");
            sb.append("'  <longitude>"+extent.getCenterX()+"</longitude>  '+\n");
            sb.append("'  <latitude>"+extent.getCenterY()+"</latitude>    '+\n");
            sb.append("'  <altitude>1000</altitude>               '+\n");
//            sb.append("'  <heading>0</heading>                 '+\n");
//            sb.append("'  <tilt>0</tilt>                       '+\n");
            sb.append("'  <range>"+ (Math.max(extent.getWidth(), extent.getHeight()) *120*1000) +"</range>   '+\n");
            sb.append("'  <altitudeMode>clampToGround</altitudeMode>         '+\n");
            sb.append("'</LookAt>                              '+\n");

            
            sb.append("'      <name>Outline</name>                 '+\n");
            sb.append("'      <visibility>1</visibility>           '+\n");
            sb.append("'      <styleUrl>#outlineStyle</styleUrl>   '+\n");
            if (extent.getWidth() < 360) {
                sb.append(convertToJavascriptString(kmlGeomOutline));
            }
            sb.append("'    </Placemark>                           '+\n");
//            sb.append("'    <Placemark>                            '+\n");
//            sb.append("'      <name>Background</name>              '+\n");
//            sb.append("'      <visibility>0</visibility>           '+\n");
//            sb.append("'      <styleUrl>#transPolyStyle</styleUrl> '+\n");
//            sb.append(convertToJavascriptString(kmlGeomBackground));
//            sb.append("'    </Placemark>                          '+\n");
            

            
            

            if (heightMode == HeightMode.BOTH || heightMode == HeightMode.REAL_3D) {

//                if (viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {
            	if (ViewerKmzUtilities.isColladaCapable(viewer)) {

            	
            	sb.append("'  <Placemark> '+\n");
            	sb.append("' 	<name>model</name> '+\n");
            	//            if (kmlTimeString != null) {
            	//                sb.append("       <TimeSpan> '+\n");
            	//                sb.append("           <begin>"+kmlTimeString+"</begin> '+\n");
            	//                sb.append("           <end>"+nextKmlTimeString+"</end> '+\n");
            	//                sb.append("       </TimeSpan> '+\n");
            	//            }
            	sb.append("' 	<visibility>1</visibility> '+\n");
            	sb.append("' 	<Style id=\"default\"></Style> '+\n");
            	sb.append("' 	<LookAt> '+\n");
            	sb.append("' 		<altitudeMode>absolute</altitudeMode> '+\n");
            	sb.append("'  		<longitude>"+viewer.getCurrentExtent().getCenterX()+"</longitude> '+\n");
            	sb.append("'  		<latitude>"+viewer.getCurrentExtent().getCenterY()+"</latitude> '+\n");
            	sb.append("' 		<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> '+\n");
            	sb.append("' 	    <range>300000</range> '+\n");
            	sb.append("' 	    <tilt>65</tilt> '+\n");
            	sb.append("' 	    <heading>0</heading> '+\n");
            	sb.append("' 	</LookAt> '+\n");
            	sb.append("' 	<Model id=\"model_1\"> '+\n");
            	sb.append("' 		<altitudeMode>absolute</altitudeMode> '+\n");
            	sb.append("' 		<Location> '+\n");
            	sb.append("'  			<longitude>"+viewer.getNexradHeader().getLon()+"</longitude> '+\n");
            	sb.append("'  			<latitude>"+viewer.getNexradHeader().getLat()+"</latitude> '+\n");
            	sb.append("' 			<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> '+\n");
            	sb.append("' 		</Location> '+\n");
            	sb.append("' 		<Orientation> '+\n");
            	sb.append("' 			<heading>0</heading> '+\n");
            	sb.append("' 			<tilt>0</tilt> '+\n");
            	sb.append("' 			<roll>0</roll> '+\n");
            	sb.append("' 		</Orientation> '+\n");
            	sb.append("' 		<Scale> '+\n");
            	sb.append("' 			<x>1</x> '+\n");
            	sb.append("' 			<y>1</y> '+\n");
            	sb.append("' 			<z>"+elevationExaggeration+"</z> '+\n");
            	sb.append("' 		</Scale> '+\n");
            	sb.append("' 		<Link> '+\n");
            	// DAE link must indicate a 'file' with a .dae extension.
            	sb.append("' 			<href>http://localhost:"+port+"/dae/"+(int)Math.floor(Math.random()*10000)+".dae</href> '+\n");
            	sb.append("' 		</Link> '+\n");
            	sb.append("' 	</Model> '+\n");
            	sb.append("' </Placemark> '+\n");
            	
            	}
            	else if (! Double.isNaN(gridHeightInMeters)) {
                	sb.append("'<GroundOverlay>                                 '+ \n");
                	sb.append("' <name>WCT 3D Image Overlay</name>                 '+ \n");
                	sb.append("' <Icon>                                         '+ \n");
                	sb.append("'     <href>http://localhost:"+port+"/image?cacheBuster="+(int)(Math.random()*10000000)+"</href>   '+ \n");
                	sb.append("'     <viewBoundScale>0.75</viewBoundScale>      '+ \n");
                	sb.append("' </Icon>                                        '+ \n");
                	sb.append("' <altitude>"+gridHeightInMeters+"</altitude>          '+ \n");
                	sb.append("' <gx:altitudeMode>absolute</gx:altitudeMode> '+ \n");
                	sb.append("' <LatLonBox>                                    '+ \n");
                	sb.append("'     <north>"+extent.getMaxY()+"</north>        '+ \n");
                	sb.append("'     <south>"+extent.getMinY()+"</south>        '+ \n");
                	sb.append("'     <east>"+extent.getMaxX()+"</east>          '+ \n");
                	sb.append("'     <west>"+extent.getMinX()+"</west>          '+ \n");
                	sb.append("' </LatLonBox>                                   '+ \n");
                	sb.append("'<LookAt>                                                            '+\n");
                	sb.append("'  <longitude>"+extent.getCenterX()+"</longitude>  '+\n");
                	sb.append("'  <latitude>"+extent.getCenterY()+"</latitude>    '+\n");
                	sb.append("'  <altitude>1000</altitude>               '+\n");
                	//        sb.append("'  <heading>0</heading>                 '+\n");
                	//        sb.append("'  <tilt>0</tilt>                       '+\n");
                	sb.append("'  <range>"+ (Math.max(extent.getWidth(), extent.getHeight()) *120*1000) +"</range>   '+\n");
                	sb.append("'  <altitudeMode>clampToGround</altitudeMode>         '+\n");
                	sb.append("'</LookAt>                              '+\n");
                	sb.append("'</GroundOverlay>                                '+ \n");
            	}
            
            }
    		
    		
    		
    		
    		
    		
    		

//    		sb.append("'    <Placemark>                           '+\n");
//    		sb.append("'      <name>Outline</name>                 '+\n");
//            sb.append("'      <visibility>1</visibility>           '+\n");
//            sb.append("'      <styleUrl>#outlineStyle2</styleUrl>   '+\n");
//            if (extent.getWidth() < 360) {
//                sb.append(convertToJavascriptString(kmlGeomOutline));
//            }
//            sb.append("'    </Placemark>                           '+\n");
    		
    		
            sb.append("'    <Style id=\"outlineStyle\">           '+\n");
            sb.append("'      <LineStyle>                         '+\n");
            sb.append("'        <width>2.0</width>                '+\n");
            sb.append("'      </LineStyle>                        '+\n");
            sb.append("'      <PolyStyle>                         '+\n");
            sb.append("'        <color>01000000</color>           '+\n");
            sb.append("'      </PolyStyle>                        '+\n");
            sb.append("'    </Style>                              '+\n");

 
            sb.append("'    <Style id=\"outlineStyle2\">           '+\n");
            sb.append("'      <LineStyle>                         '+\n");
            sb.append("'        <color>39943232</color>   '+\n");
            sb.append("'        <width>12.0</width>                '+\n");
            sb.append("'      </LineStyle>                        '+\n");
            sb.append("'      <PolyStyle>                         '+\n");
            sb.append("'        <color>01000000</color>           '+\n");
            sb.append("'      </PolyStyle>                        '+\n");
            sb.append("'    </Style>                              '+\n");

            
            
            
            sb.append("'</Folder> '+ \n");
            sb.append("'<LookAt> '+\n");
            sb.append("'  <longitude>"+extent.getCenterX()+"</longitude>  '+\n");
            sb.append("'  <latitude>"+extent.getCenterY()+"</latitude> '+\n");
            sb.append("'  <altitude>1000</altitude> '+\n");
//            sb.append("'  <heading>0</heading>                 '+\n");
//            sb.append("'  <tilt>0</tilt>                       '+\n");
            sb.append("'  <range>"+ (Math.max(extent.getWidth(), extent.getHeight()) *120*1000) +"</range>   '+\n");
            sb.append("'  <altitudeMode>clampToGround</altitudeMode> '+\n");
            sb.append("'</LookAt> '+\n");
            sb.append("'</Document> '+ \n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
        
        sb.append("'</kml> '\n");
        
        System.out.println(sb.toString().replaceAll("'", "").replaceAll("\\+", ""));
        
        webBrowser.executeJavascript("refreshImageWithKML("+sb.toString()+");");
        webBrowser.executeJavascript("ge.getFeatures().getLastChild().setOpacity("+layerOpacity+");");

        
        
        
        System.out.println("flyToExtent: "+flyToExtent);
        if (viewController == ViewController.GEOTOOLS && flyToExtent) {
//            webBrowser.executeJavascript("flyToExtent();");
            flyToExtent(extent);
        }

        viewer.getStatusBar().setProgressText("");

    }
    
    
    public void flyToExtent(Rectangle2D extent) {
        int range = (int)(Math.max(extent.getWidth(), extent.getHeight()) *100*1000);
        final String javascript = "flyToBBoxExtent("+extent.getCenterX()+","+extent.getCenterY()+","+range+");";
        System.out.println(javascript);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getWebBrowser().executeJavascript(javascript);
            }
        });
    }
    
    public void clearWctLayer() {
        final String javascript = "clearWctLayer();"; 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getWebBrowser().executeJavascript(javascript);
            }
        });
    }
    
    private static String convertToJavascriptString(String str) {
        StringBuilder sb = new StringBuilder();
        for (String s : str.split("\n")) {
            sb.append("'"+s+"'+\n");
        }
        return sb.toString();
    }
    
    
    
    
    private String getHTML() {
        StringBuilder sb = new StringBuilder();
//        sb.append(" <!--                                                                                                        \n");   
//        sb.append("   Copyright (c) 2009 Google inc.                                                                            \n");
//        sb.append("                                                                                                             \n");
//        sb.append("   You are free to copy and use this sample.                                                                 \n");
//        sb.append("   License can be found here: http://code.google.com/apis/ajaxsearch/faq/#license                            \n");
//        sb.append(" -->                                                                                                         \n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append(" <html xmlns=\"http://www.w3.org/1999/xhtml\">                                                               \n");
        sb.append("   <head>                                                                                                    \n");
        sb.append("     <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>                                \n");
        sb.append("     <title>Google Earth API Sample</title>                                                                  \n");
//        sb.append("     <script src=\"http://www.google.com/jsapi?key=ABQIAAAA1XbMiDxx_BTCY2_FkPh06RRaGTYH6UMl8mADNa0YKuWNNa8VNxQEerTAUcfkyrr6OwBovxn7TDAH5Q\"></script>  \n");
        sb.append("     <script src=\"http://www.google.com/jsapi?key=ABQIAAAAIoZKb9JrajP0IwKLFr335hTsk_juWuwt_LNtOqByAr93uT9HuRTzovVKWJSi3TQGBPsWMWPyWdIQdg&client=gme-noaa&sensor=false&channel=NESDIS.NCDC.RSAD.WCT\"></script>  \n");
        sb.append("     <script type=\"text/javascript\">                                                                       \n");
        sb.append("     var ge;                                                                                                 \n");
        sb.append("     var isViewRefreshNeeded = true;                                                                               \n");
        sb.append("                                                                                                             \n");
        sb.append("     google.load(\"earth\", \"1\");                                                                          \n");
        sb.append("                                                                                                             \n");
        sb.append("     function init() {                                                                                       \n");
        sb.append("       google.earth.createInstance('map3d', initCallback, failureCallback);                                  \n");
        sb.append("       document.getElementById('map3d').style.position=\"absolute\";                                           \n");
        sb.append("       document.getElementById('map3d').style.top=\"0\";                                           \n");
        sb.append("       document.getElementById('map3d').style.left=\"0\";                                           \n");
        sb.append("       document.getElementById('map3d').style.width=\"100%\";                                           \n");
        sb.append("       document.getElementById('map3d').style.height=\"100%\";                                           \n");
        sb.append("     }                                                                                                       \n");
        sb.append("                                                                                                             \n");
        sb.append("     function initCallback(instance) {                                                                       \n");
        sb.append("       ge = instance;                                                                                        \n");
        sb.append("       ge.getWindow().setVisibility(true);                                                                   \n");
        sb.append("                                                                                                             \n");
        sb.append("       // add a navigation control                                                                           \n");
        sb.append("       ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);                                          \n");
        sb.append("       ge.getOptions().setScaleLegendVisibility(true);                                             \n");
        sb.append("       ge.getOptions().setStatusBarVisibility(true);                                               \n");
        sb.append("                                                                                                             \n");
        sb.append("       // add some layers                                                                                    \n");
        sb.append("       ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);                                            \n");
        sb.append("       ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);                                              \n");
        sb.append("                                                                                                             \n");
        sb.append("       var netlink = ge.parseKml(                                                                           \n");        
        sb.append("       '<?xml version=\"1.0\" encoding=\"UTF-8\"?>                                               '+ \n");
        sb.append("       '<kml xmlns=\"http://www.opengis.net/kml/2.2\">                                           '+ \n");
        sb.append("       '  <Folder>                                                                               '+ \n");
        sb.append("       '    <name>Network Links</name>                                                           '+ \n");
        sb.append("       '    <visibility>1</visibility>                                                           '+ \n");
        sb.append("       '    <open>1</open>                                                                       '+ \n");
        sb.append("       '    <description>Network link example 2</description>                                    '+ \n");
        sb.append("       '    <NetworkLink>                                                                        '+ \n");
        sb.append("       '      <name>View Centered Placemark</name>                                               '+ \n");
        sb.append("       '      <visibility>1</visibility>                                                         '+ \n");
        sb.append("       '      <open>1</open>                                                                     '+ \n");
        sb.append("       '      <description>The view-based refresh allows the remote server to calculate          '+ \n");
        sb.append("       '         the center of your screen and return a placemark.</description>                 '+ \n");
        sb.append("       '      <refreshVisibility>0</refreshVisibility>                                           '+ \n");
        sb.append("       '      <flyToView>0</flyToView>                                                           '+ \n");
        sb.append("       '      <Link>                                                                             '+ \n");
        sb.append("       '        <href>http://localhost:"+port+"/kml</href>                                           '+ \n");   
//        sb.append("       '        <refreshInterval>2</refreshInterval>                                             '+ \n");
        sb.append("       '        <viewRefreshMode>onStop</viewRefreshMode>                                        '+ \n");
        sb.append("       '        <viewRefreshTime>1</viewRefreshTime>                                             '+ \n");
//        sb.append("       '        <viewFormat>BBOX=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth];WIDTH=[horizPixels];HEIGHT=[vertPixels];isViewRefreshNeeded='+isViewRefreshNeeded+'</viewFormat>'+ \n");        
        sb.append("       '        <viewFormat>BBOX=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth];" +
                                              "WIDTH=[horizPixels];HEIGHT=[vertPixels];" +
                                              "CAMERA_LON=[cameraLon];CAMERA_LAT=[cameraLat];CAMERA_ALT=[cameraAlt]</viewFormat>'+ \n");        
        sb.append("       '      </Link>                                                                            '+ \n");
        sb.append("       '    </NetworkLink>                                                                       '+ \n");
        sb.append("       '  </Folder>                                                                              '+ \n");
        sb.append("       '</kml>                                                                                  '); \n");
        sb.append("                                                                                                             \n");
        if (viewController == ViewController.GOOGLE_EARTH) {
            sb.append("       ge.getFeatures().appendChild(netlink);                                                               \n");
        }
        sb.append("                                                                                                             \n");
//        sb.append("       // Fly to the Pentagon                                                                                \n");
//        sb.append("       var la = ge.createLookAt('');                                                                         \n");
//        sb.append("       la.set(38.867, -77.0565, 500, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 45, 900);                            \n");
//        sb.append("       ge.getView().setAbstractView(la);                                                                     \n");
        sb.append("                                                                                                             \n");
//        sb.append("       document.getElementById('installed-plugin-version').innerHTML =                                       \n");
//        sb.append("         ge.getPluginVersion().toString();                                                                   \n");
        
        
        sb.append("          window.location = 'command://'+ encodeURIComponent('ge load complete');                            \n");
        
        
        sb.append("     } \n");
        sb.append("\n");
        sb.append("     function failureCallback(errorCode) { \n");
        sb.append("     } \n");
        sb.append("\n");
        sb.append("\n");
        sb.append("     function refreshLegendImage() {                                 \n");
        sb.append("         // Create the GroundOverlay                                 \n");
        sb.append("         var legendOverlay = ge.createScreenOverlay('');             \n");
        
        sb.append("         // Specify the image path and assign it to the GroundOverlay  \n");
        sb.append("         var icon = ge.createIcon('');                               \n");
        sb.append("         icon.setHref(\"http://localhost:"+port+"/legend/?cacheBuster=\"+Math.floor(Math.random()*10000) );      \n");
        sb.append("         legendOverlay.setIcon(icon);                                \n");
        
        sb.append("         // Specify the location                                     \n");
        sb.append("         screenOverlay.getOverlayXY().setXUnits(ge.UNITS_PIXELS);    \n");
        sb.append("         screenOverlay.getOverlayXY().setYUnits(ge.UNITS_PIXELS);    \n");
        sb.append("         screenOverlay.getOverlayXY().setX(100);    \n");
        sb.append("         screenOverlay.getOverlayXY().setY(100);    \n");

        sb.append("         // Add the GroundOverlay to Earth                           \n");
        sb.append("         if (ge.getFeatures().hasChildNodes()) {                     \n");
        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());       \n");     
        sb.append("         }                                                           \n");
        sb.append("         ge.getFeatures().appendChild(groundOverlay);                \n");
        
        sb.append("     }                                                               \n");
        sb.append("\n");
        sb.append("\n");
        sb.append("     function refreshImage(minX, minY, maxX, maxY) {  \n");
        sb.append("         // Create the GroundOverlay                                 \n");
        sb.append("         var groundOverlay = ge.createGroundOverlay('');             \n");
        sb.append("                                                            \n");
        sb.append("         // Specify the image path and assign it to the GroundOverlay  \n");
        sb.append("         var icon = ge.createIcon('');                               \n");
//        sb.append("         icon.setHref(\"http://localhost:"+port+"/image/\");             \n");
        sb.append("         icon.setHref(\"http://localhost:"+port+"/image/?cacheBuster=\"+Math.floor(Math.random()*10000) );      \n");
        sb.append("         groundOverlay.setIcon(icon);                                \n");
//        sb.append("alert('mark 1');                                                                     \n");
        sb.append("         // Specify the geographic location                          \n");
        sb.append("         var latLonBox = ge.createLatLonBox('');                     \n");
        sb.append("         latLonBox.setBox(minY, maxY, minX, maxX, 0);                \n");
        sb.append("         groundOverlay.setLatLonBox(latLonBox);                      \n");
        sb.append("         groundOverlay.setFlyToView(true);                           \n");        
        sb.append("                                                                     \n");
        sb.append("         // Add the GroundOverlay to Earth                           \n");
        sb.append("         if (ge.getFeatures().hasChildNodes()) {                     \n");
        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());       \n");     
        sb.append("         }                                                           \n");
        sb.append("         ge.getFeatures().appendChild(groundOverlay);                \n");     
        sb.append("                                                                     \n");
        sb.append("     }                                                               \n");
        sb.append("                                                                     \n");
        sb.append("     function refreshImageWithKML(kml) {                             \n");
        sb.append("         var kmlOverlay = ge.parseKml(kml)                           \n");
        sb.append("                                                                     \n");
        sb.append("                                                                     \n");
        sb.append("         // Add the GroundOverlay to Earth                           \n");
        if (viewController == ViewController.GOOGLE_EARTH) {
            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 1) { \n");
        }
        else {
            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 0) { \n");
        }
        // comment this out to keep adding layers
        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());       \n");
        sb.append("         ge.getFeatures().appendChild(kmlOverlay);                   \n");
        sb.append("     }                                                               \n");

        sb.append("     function flyToExtent() {                                               \n");
        if (viewController != ViewController.GOOGLE_EARTH) {
            sb.append("         if (ge.getFeatures().getLastChild().getAbstractView()) {           \n");
            sb.append("              ge.getView().setAbstractView(ge.getFeatures().getLastChild().getAbstractView());   \n");
            sb.append("         }                                                                  \n");
        }
        sb.append("     }                                                                      \n");
        sb.append("                                                                            \n");
        sb.append("                                                                            \n");
        sb.append("     function flyToBBoxExtent(centerX, centerY, range) {                           \n");
//        sb.append("          alert('this is alert: '+centerX+' , '+centerY+' , '+range);                                                                  \n");
        sb.append("          var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);  \n");
        sb.append("          lookAt.setLatitude(centerY);                                      \n");
        sb.append("          lookAt.setLongitude(centerX);                                     \n");
        sb.append("          lookAt.setRange(range);                                           \n");
        sb.append("          ge.getView().setAbstractView(lookAt);                             \n");
        sb.append("                                                                            \n");
//        sb.append("          isViewRefreshNeeded = false;                                      \n");
        sb.append("     }                                                                      \n");
        sb.append("                                                                            \n");
        sb.append("                                                                            \n");
        sb.append("     function clearWctLayer() { \n");
        if (viewController == ViewController.GOOGLE_EARTH) {
            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 1) { \n");
        }
        else {
            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 0) { \n");
        }
        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());  \n");     
        sb.append("         }  \n");
        sb.append("     }  \n");
        
        sb.append("                                                                                                             \n");
        sb.append("     </script>                                                                                               \n");
        sb.append("   </head>                                                                                                   \n");
        sb.append("   <body onload=\"init()\" style=\"font-family: arial, sans-serif; font-size: 13px;\" scroll=\"no\">         \n");
//        sb.append("     <div id=\"map3d\" style=\"width: 500px; height: 380px;\"></div>                                         \n");
        sb.append("     <div id=\"map3d\" style=\"width: 100%; height: 100%; \"></div>    \n");
//        sb.append("     <br>                                                                                                    \n");
//        sb.append("     <div>Installed Plugin Version: <span id=\"installed-plugin-version\" style=\"font-weight: bold;\">Loading...</span></div> \n");
        sb.append("   </body>                                                                                                   \n");
        sb.append(" </html>                                                                                                     \n");

        
        String[] lines = sb.toString().split("\n");
        for (int n=0; n<lines.length; n++) {
            System.out.println(n+": "+lines[n]);
        }
        
        
        return sb.toString();
    }

    public void setViewer(final WCTViewer viewer) {
    	if (this.viewer != null) {
    		this.viewer.removeRenderCompleteListener(rcl);
    	}
        this.viewer = viewer;
        System.out.println("SETTING VIEWER OBJECT IN GE BROWSER: "+viewer);
        viewer.addRenderCompleteListener(rcl);
    }

    public WCTViewer getViewer() {
        return viewer;
    }

    
    
    
    private void initView() {
        if (viewer.getCurrentDataType() == CurrentDataType.GRIDDED) {
            try {
                if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis() != null) {
                    String vertUnits = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getUnitsString();
                    if (vertUnits.equalsIgnoreCase("m") || vertUnits.equalsIgnoreCase("meter") || 
                    				vertUnits.equalsIgnoreCase("meters")) {
                    	setGridHeightInMeters(viewer.getGridDatasetRaster().getLastProcessedHeight());
                    }   
                    else if (vertUnits.equalsIgnoreCase("Pa")) {
                    	
                        double pressureInPa = viewer.getGridDatasetRaster().getLastProcessedHeight();
                        double alt = NexradEquations.getAltitudeFromPressureInMeters(pressureInPa);                                

                        System.out.println("pressure in: "+pressureInPa+" Pa, altitude out: "+alt);
                    	setGridHeightInMeters(alt);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshView(isAutoFlyTo());
    }
    

    public int getPort() {
        return port;
    }


    public void setViewController(ViewController viewController) {
        this.viewController = viewController;
        this.webBrowser.setHTMLContent(getHTML());
    }


    public ViewController getViewController() {
        return viewController;
    }
    
    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }

    public boolean isAutoFlyTo() {
        return autoFlyTo;
    }
    
	public void setHeightMode(HeightMode heightMode) {
		this.heightMode = heightMode;
	}
	public HeightMode getHeightMode() {
		return heightMode;
	}
	/**
	 * Sets the elevationExaggeration - this causes both the 'z' scale factor
	 * in the model kml and the javascript terrain exaggeration to be set.
	 * (ge.getOptions.setTerrainExaggeration(value))
	 * @param elevationExaggeration
	 */
    public void setElevationExaggeration(final double elevationExaggeration) {
		this.elevationExaggeration = elevationExaggeration;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                webBrowser.executeJavascript("ge.getOptions().setTerrainExaggeration("+elevationExaggeration+");");
                refreshView(false);
            }
        });

	}
	public double getElevationExaggeration() {
		return elevationExaggeration;
	}

	public void setGridHeightInMeters(double gridHeightInMeters) {
		this.gridHeightInMeters = gridHeightInMeters;
	}

	public double getGridHeightInMeters() {
		return gridHeightInMeters;
	}
	
	
	
	public RenderCompleteListener getRenderCompleteListener() {
		return rcl;
	}
	
	







//	private static GoogleEarthBrowser ge = null;
//    private static WCTFrame geFrame = null;
//    
//    public static GoogleEarthBrowser launchWindow(final WCTViewer viewer) {
//        
//    	
//    	
//        try {
//
////            if (ge != null) {
//////                geFrame.setLocation(viewer.getX()+15, viewer.getY()+15);
////                geFrame.setVisible(true);
////                geFrame.setExtendedState(Frame.NORMAL);
////                geFrame.requestFocusInWindow();
////
////                return ge;
////            }
//            
//            
//            final WCTImageServer server = new WCTImageServer(viewer, WCTImageServer.DEFAULT_PORT+1);
//            
//            final WCTFrame geFrame = new WCTFrame("WCT Google Earth View");
//            ge = new GoogleEarthBrowser(WCTImageServer.DEFAULT_PORT+1, ViewController.GEOTOOLS, true, geFrame, true);
//            createFrame(ge, geFrame);
//            geFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            ge.setViewer(viewer);
//
//            geFrame.addWindowListener(new WindowListener() {
//                @Override
//                public void windowActivated(WindowEvent e) {
//                }
//                @Override
//                public void windowClosed(WindowEvent e) {
//                }
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    System.out.print("Shutting down WCTImageServer...");
//                    server.shutdown();
//                    ge.shutdown();
//                	viewer.removeRenderCompleteListener(ge.getRenderCompleteListener());
//                    System.out.println("  Done.");
//                }
//                @Override
//                public void windowDeactivated(WindowEvent e) {
//                }
//                @Override
//                public void windowDeiconified(WindowEvent e) {
//                }
//                @Override
//                public void windowIconified(WindowEvent e) {
//                }
//                @Override
//                public void windowOpened(WindowEvent e) {
//                }
//            });
//
//
//
//            //        GoogleEarthBrowser.main(null);                    
//            //        nexview.setCurrentViewType(CurrentViewType.GOOGLE_EARTH);
//
//            return ge;
//
//        } catch (BindException ex) {
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(viewer, 
//                    "Only one instance of the internal Weather and Climate Toolkit \n" +
//                    "Google Earth Browser may be open.  Please close the other \n" +
//                    "browser and try again.", "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
//            return null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            //        JOptionPane.showMessageDialog(finalThis, ex.getMessage(), "WCT Google Earth Browser Error", JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//        
//    }


    public void setBordersVisible(boolean bordersVisible) {
        webBrowser.executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, "+bordersVisible+");");
        this.bordersVisible = bordersVisible;
    }


    public boolean isBordersVisible() {
        return bordersVisible;
    }


    public void setRoadsVisible(boolean roadsVisible) {
        webBrowser.executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, "+roadsVisible+");");
        this.roadsVisible = roadsVisible;
    }


    public boolean isRoadsVisible() {
        return roadsVisible;
    }


    public void setBuildingsVisible(boolean buildingsVisible) {
        webBrowser.executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_BUILDINGS, "+buildingsVisible+");");
        this.buildingsVisible = buildingsVisible;
    }


    public boolean isBuildingsVisible() {
        return buildingsVisible;
    }


    public void setTerrainVisible(boolean terrainVisible) {
        webBrowser.executeJavascript("ge.getLayerRoot().enableLayerById(ge.LAYER_TERRAIN, "+terrainVisible+");");
        this.terrainVisible = terrainVisible;
    }


    public boolean isTerrainVisible() {
        return terrainVisible;
    }


    public void setSunVisible(boolean sunVisible) {
        webBrowser.executeJavascript("ge.getSun().setVisibility("+sunVisible+");");
        this.sunVisible = sunVisible;
    }


    public boolean isSunVisible() {
        return sunVisible;
    }


    public void setAtmosphereVisible(boolean atmosphereVisible) {
        webBrowser.executeJavascript("ge.getOptions().setAtmosphereVisibility("+atmosphereVisible+");");
        this.atmosphereVisible = atmosphereVisible;
    }


    public boolean isAtmosphereVisible() {
        return atmosphereVisible;
    }


    public void setScaleVisible(boolean scaleVisible) {
        webBrowser.executeJavascript("ge.getOptions().setScaleLegendVisibility("+scaleVisible+");");
        this.scaleVisible = scaleVisible;
    }


    public boolean isScaleVisible() {
        return scaleVisible;
    }


    public void setOverviewVisible(boolean overviewVisible) {
        webBrowser.executeJavascript("ge.getOptions().setOverviewMapVisibility("+overviewVisible+");");
        this.overviewVisible = overviewVisible;
    }


    public boolean isOverviewVisible() {
        return overviewVisible;
    }


    public void setGridVisible(boolean gridVisible) {
        webBrowser.executeJavascript("ge.getOptions().setGridVisibility("+gridVisible+");");
        this.gridVisible = gridVisible;
    }


    public boolean isGridVisible() {
        return gridVisible;
    }


    
    


    public JPanel createLayerSelectionPanel() {
        
        JPanel panel = new JPanel();
        
        // init google earth map layer panel
        final JCheckBox jcbBorders = new JCheckBox("Borders", true);
        final JCheckBox jcbRoads = new JCheckBox("Roads", true);
        final JCheckBox jcbBuildings = new JCheckBox("Buildings", false);
        final JCheckBox jcbTerrain = new JCheckBox("Terrain", true);
        final JCheckBox jcbSun = new JCheckBox("Sun", false);
        final JCheckBox jcbAtmosphere = new JCheckBox("Atmosphere", true);
        final JCheckBox jcbScale = new JCheckBox("Scale", true);
        final JCheckBox jcbOverviewMap = new JCheckBox("Overview Map", false);
        final JCheckBox jcbGrid = new JCheckBox("Lat/Lon Grid", false);
        
        ActionListener geLayerListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == jcbBorders) {
                    setBordersVisible(jcbBorders.isSelected());
                }
                else if (e.getSource() == jcbRoads) {
                    setRoadsVisible(jcbRoads.isSelected());
                }
                else if (e.getSource() == jcbBuildings) {
                    setBuildingsVisible(jcbBuildings.isSelected());
                }
                else if (e.getSource() == jcbTerrain) {
                    setTerrainVisible(jcbTerrain.isSelected());
                }
                else if (e.getSource() == jcbSun) {
                    setSunVisible(jcbSun.isSelected());
                }
                else if (e.getSource() == jcbAtmosphere) {
                    setAtmosphereVisible(jcbAtmosphere.isSelected());
                }
                else if (e.getSource() == jcbScale) {
                    setScaleVisible(jcbScale.isSelected());
                }
                else if (e.getSource() == jcbOverviewMap) {
                    setOverviewVisible(jcbOverviewMap.isSelected());
                }
                else if (e.getSource() == jcbGrid) {
                    setGridVisible(jcbGrid.isSelected());
                }
            }
        };

        jcbBorders.addActionListener(geLayerListener);
        jcbRoads.addActionListener(geLayerListener);
        jcbBuildings.addActionListener(geLayerListener);
        jcbTerrain.addActionListener(geLayerListener);
        jcbSun.addActionListener(geLayerListener);
        jcbAtmosphere.addActionListener(geLayerListener);
        jcbScale.addActionListener(geLayerListener);
        jcbOverviewMap.addActionListener(geLayerListener);
        jcbGrid.addActionListener(geLayerListener);
        
        panel.setLayout(new RiverLayout());
        panel.add(jcbBorders, "br");
        panel.add(jcbRoads, "br");
        panel.add(jcbBuildings, "br");
        panel.add(jcbTerrain, "br");
        panel.add(jcbSun, "br");
        panel.add(jcbAtmosphere, "br");
        panel.add(jcbScale, "br");
        panel.add(jcbOverviewMap, "br");
        panel.add(jcbGrid, "br");
        
        panel.setBorder(WCTUiUtils.myTitledBorder("Layers", 5));
        
        
        jrb2dButton = new JRadioButton("2D (Drape)");        
        jrb3dButton = new JRadioButton("3D (Real Height)");        
        jrbBothButton = new JRadioButton("Both", true);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(jrb2dButton);
        buttonGroup.add(jrb3dButton);
        buttonGroup.add(jrbBothButton);
        JPanel buttonPanel = new JPanel(new RiverLayout(10, 5));
        buttonPanel.add(jrb2dButton, "br");
        buttonPanel.add(jrb3dButton, "br");
        buttonPanel.add(jrbBothButton, "br");
        jrb2dButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (((JRadioButton)(evt.getSource())).isSelected()) {
					setHeightMode(HeightMode.DRAPE_2D);
					refreshView(false);
				}
			}
        });
        jrb3dButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (((JRadioButton)(evt.getSource())).isSelected()) {
					setHeightMode(HeightMode.REAL_3D);
					refreshView(false);
				}
			}
        });
        jrbBothButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (((JRadioButton)(evt.getSource())).isSelected()) {
					setHeightMode(HeightMode.BOTH);
					refreshView(false);
				}
			}
        });
        
        final JSpinner elevExSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 0.25));
        elevExSpinner.addChangeListener(new ChangeListener() {
        	
            private Timer timer = new Timer();
            private TimerTask task;
            private int waitTime = 1000;
        	
			@Override
			public void stateChanged(ChangeEvent evt) {
//				setElevationExaggeration(Double.parseDouble(((JSpinner)evt.getSource()).getValue().toString()));
//				refreshView(false);
				
		        if (task != null) {
		            try {
		                task.cancel();
		            } catch (Exception e) {
		            }
		        }
		        startTimer();
			}
			
			
			
		    private synchronized void startTimer() {
		        
		        task = new TimerTask() {
		            public void run() {
//		            	System.out.println("DOING SPINNER EVENT");
						setElevationExaggeration(Double.parseDouble(elevExSpinner.getValue().toString()));
//						refreshView(false);
	            }
		        };

		        timer.purge();
		        try {
		            timer.schedule(task , waitTime);
		        } catch (Exception e) {
		            System.err.println(e.getMessage());
		            System.err.println("Creating new zoom timer object...");
		            timer = new Timer();
		            timer.schedule(task , waitTime);
		        }
		    }

        });
        elevExSpinner.setPreferredSize(new Dimension(75, (int)elevExSpinner.getPreferredSize().getHeight()));
        
        
        
        buttonPanel.add(new JLabel("Elevation Exaggeration: "), "p");
        buttonPanel.add(elevExSpinner, "br");
        buttonPanel.setBorder(WCTUiUtils.myTitledBorder("Data View", 5));
        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panel, BorderLayout.WEST);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        return mainPanel;
    }




}

