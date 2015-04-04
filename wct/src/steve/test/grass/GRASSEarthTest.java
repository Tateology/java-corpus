package steve.test.grass;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.gis.kml.KMLUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

public class GRASSEarthTest extends JFrame {
    final private JWebBrowser webBrowser = new JWebBrowser();
    private int port = 8765;
    
    private GEGrassControlPanel controlPanel = new GEGrassControlPanel(this);
    private GEGrassCommandPanel commandPanel = null;
    
//    private GRASSUtils grass = new GRASSUtils("C:\\Program Files\\GRASS 6.4.2", "/c/ncsu/gis582/data", "nc_ll", "coursework");
    private GRASSUtils grass = new GRASSUtils();
	
    private final static DecimalFormat fmt4 = new DecimalFormat("#.####");
    
    
	public static void main(String[] args) {
		NativeInterface.open();
		
	    
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

        		GRASSEarthTest viewer = new GRASSEarthTest();
//        		viewer.setSize(800, 600);
//        		viewer.setVisible(true);
            }
        });
		
		
        System.out.println("mark 1");
        NativeInterface.runEventPump();
        System.out.println("mark 2");
	}
	
	
	public GRASSEarthTest() {
		createUI();
		
		this.setSize(1100, 660);
		this.setVisible(true);
		this.setTitle("GE-GRASS");
		
		GEGrassInitDialog initDialog = new GEGrassInitDialog(this);
		initDialog.setVisible(true);
		initDialog.configureGRASS(grass);
		
		
		try {
			GRASSImageServer imageServer = new GRASSImageServer(8765, grass);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void createUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		splitPane.setRightComponent(getPanel());
		splitPane.setLeftComponent(getCommandConsolePanel());
		
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
	}
	
	public GRASSUtils getGrassUtils() {
		return grass;
	}
	
	public JPanel getCommandConsolePanel() {
		JPanel panel = new JPanel(new RiverLayout());
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {

//						refreshView(true, "rast", "elev_ned_1arcsec@PERMANENT");	
						refreshView(true, "rast", "developed_lake@coursework");	
//						new File("grass6output.png").delete();
						
		            }
		        });
				
			}			
		});
//		panel.add(refreshButton);
		
		commandPanel = new GEGrassCommandPanel(this);
		panel.add(commandPanel);
		
		return panel;
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
                String command = e.getCommand();
                if (command.startsWith("bbox=")) {
                	String bbox = command.substring("bbox=".length());
                	String[] bboxFields = bbox.split(",");
                	String grassCommand = "g.region n="+fmt4.format(Double.parseDouble(bboxFields[0]))+
                			" s="+fmt4.format(Double.parseDouble(bboxFields[1]))+
                			" w="+fmt4.format(Double.parseDouble(bboxFields[2]))+
                		    " e="+fmt4.format(Double.parseDouble(bboxFields[3]));
                	commandPanel.executeCommand(grassCommand, false);
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
        
        
//        System.out.println("steve 1");
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				webBrowser.setHTMLContent(getHTML());
//			}
//		});
//		System.out.println("steve 2");
//		webBrowser.navigate("http://www.google.com/earth/explore/products/plugin.html");
        
//        webBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        webBrowserPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        webBrowser.getNativeComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return panel;
    }
    
    public JWebBrowser getWebBrowser() {
    	return webBrowser;
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
        sb.append("     <script type=\"text/javascript\">  \n");
        sb.append("     var ge;  \n");
        sb.append("     var isViewRefreshNeeded = true;  \n");
        sb.append("\n");
        sb.append("     google.load(\"earth\", \"1\"); \n");
        sb.append("\n");
        sb.append("     function init() {  \n");                         
//        sb.append(" alert('mark 2');  ");    
        sb.append("       google.earth.createInstance('map3d', initCallback, failureCallback);  \n");
        sb.append("       document.getElementById('map3d').style.position=\"absolute\";  \n");
        sb.append("       document.getElementById('map3d').style.top=\"0\"; \n");
        sb.append("       document.getElementById('map3d').style.left=\"0\"; \n");
        sb.append("       document.getElementById('map3d').style.width=\"100%\"; \n");
        sb.append("       document.getElementById('map3d').style.height=\"100%\"; \n");
        sb.append("     } \n");
        sb.append("\n");
        sb.append("     function initCallback(instance) { \n");       
//        sb.append(" alert('mark 1');  ");
        sb.append("       ge = instance; \n");
        sb.append("       ge.getWindow().setVisibility(true); \n");
        sb.append("\n");
        sb.append("       // add a navigation control \n");
        sb.append("       ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO); \n");
        sb.append("       ge.getOptions().setScaleLegendVisibility(true); \n");
        sb.append("       ge.getOptions().setStatusBarVisibility(true); \n");
        sb.append("\n");
        sb.append("       // add some layers  \n");
        sb.append("       ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);   \n");
        sb.append("       ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);   \n");
        sb.append("    \n");
        
        
        sb.append("          window.location = 'command://'+ encodeURIComponent('ge load complete'); \n");
//        sb.append(" alert('mark 1');  ");
        
        sb.append("     } \n");
        sb.append("\n");
        sb.append("     function failureCallback(errorCode) { \n");
        sb.append("     } \n");
        sb.append("\n");
        sb.append("\n");
//        sb.append("     function refreshLegendImage() {                                 \n");
//        sb.append("         // Create the GroundOverlay                                 \n");
//        sb.append("         var legendOverlay = ge.createScreenOverlay('');             \n");
//        
//        sb.append("         // Specify the image path and assign it to the GroundOverlay  \n");
//        sb.append("         var icon = ge.createIcon('');                               \n");
//        sb.append("         icon.setHref(\"http://localhost:"+port+"/legend/?cacheBuster=\"+Math.floor(Math.random()*10000) );      \n");
//        sb.append("         legendOverlay.setIcon(icon);                                \n");
//        
//        sb.append("         // Specify the location                                     \n");
//        sb.append("         screenOverlay.getOverlayXY().setXUnits(ge.UNITS_PIXELS);    \n");
//        sb.append("         screenOverlay.getOverlayXY().setYUnits(ge.UNITS_PIXELS);    \n");
//        sb.append("         screenOverlay.getOverlayXY().setX(100);    \n");
//        sb.append("         screenOverlay.getOverlayXY().setY(100);    \n");
//
//        sb.append("         // Add the GroundOverlay to Earth                           \n");
//        sb.append("         if (ge.getFeatures().hasChildNodes()) {                     \n");
//        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());       \n");     
//        sb.append("         }                                                           \n");
//        sb.append("         ge.getFeatures().appendChild(groundOverlay);                \n");
//        
//        sb.append("     }                                                               \n");
//        sb.append("\n");
//        sb.append("\n");
//        sb.append("     function refreshImage(minX, minY, maxX, maxY) {  \n");
//        sb.append("         // Create the GroundOverlay                                 \n");
//        sb.append("         var groundOverlay = ge.createGroundOverlay('');             \n");
//        sb.append("                                                            \n");
//        sb.append("         // Specify the image path and assign it to the GroundOverlay  \n");
//        sb.append("         var icon = ge.createIcon('');                               \n");
////        sb.append("         icon.setHref(\"http://localhost:"+port+"/image/\");             \n");
//        sb.append("         icon.setHref(\"http://localhost:"+port+"/image/?cacheBuster=\"+Math.floor(Math.random()*10000) );      \n");
//        sb.append("         groundOverlay.setIcon(icon);                                \n");
//        sb.append("alert('mark 1');                                                                     \n");
//        sb.append("         // Specify the geographic location                          \n");
//        sb.append("         var latLonBox = ge.createLatLonBox('');                     \n");
//        sb.append("         latLonBox.setBox(minY, maxY, minX, maxX, 0);                \n");
//        sb.append("         groundOverlay.setLatLonBox(latLonBox);                      \n");
//        sb.append("         groundOverlay.setFlyToView(true);                           \n");        
//        sb.append("                                                                     \n");
//        sb.append("         // Add the GroundOverlay to Earth                           \n");
//        sb.append("         if (ge.getFeatures().hasChildNodes()) {                     \n");
//        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());       \n");     
//        sb.append("         }                                                           \n");
//        sb.append("         ge.getFeatures().appendChild(groundOverlay);                \n");     
//        sb.append("                                                                     \n");
//        sb.append("     }                                                               \n");
//        sb.append("                                                                     \n");
        sb.append("     function refreshImageWithKML(kml) {  \n");
        sb.append("         var kmlOverlay = ge.parseKml(kml); \n");
        sb.append(" \n");
        sb.append(" \n");
        sb.append("         // Add the GroundOverlay to Earth  \n");
        sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 0) { \n");
        // comment this out to keep adding layers
        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild()); \n");
        sb.append("         } \n");
        sb.append("         ge.getFeatures().appendChild(kmlOverlay);  \n");
        sb.append("     }  \n");
//
//        sb.append("     function flyToExtent() {                                               \n");
////        if (viewController != ViewController.GOOGLE_EARTH) {
////            sb.append("         if (ge.getFeatures().getLastChild().getAbstractView()) {           \n");
////            sb.append("              ge.getView().setAbstractView(ge.getFeatures().getLastChild().getAbstractView());   \n");
////            sb.append("         }                                                                  \n");
////        }
//        sb.append("     }                                                                      \n");
//        sb.append("                                                                            \n");
//        sb.append("                                                                            \n");
        sb.append("     function flyToBBoxExtent(centerX, centerY, range) { \n");
//        sb.append("          alert('this is alert: '+centerX+' , '+centerY+' , '+range);                                                                  \n");
        sb.append("          var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);  \n");
        sb.append("          lookAt.setLatitude(centerY);                                      \n");
        sb.append("          lookAt.setLongitude(centerX);                                     \n");
        sb.append("          lookAt.setRange(range);                                           \n");
        sb.append("          ge.getView().setAbstractView(lookAt);                             \n");
        sb.append("                                                                            \n");
//        sb.append("          isViewRefreshNeeded = false;                                      \n");
        sb.append("     } \n");
        sb.append(" \n");
        sb.append(" \n");
        
        
        sb.append("     function getBBoxExtent() { \n");
//      sb.append("          alert('this is alert: '+centerX+' , '+centerY+' , '+range);                                                                  \n");
      sb.append("          var bbox = ge.getView().getViewportGlobeBounds();  \n");
//      sb.append("          alert(bbox.getWest());                                     \n");
      sb.append("          window.location = 'command://'+ encodeURIComponent('bbox='+bbox.getNorth()+','+bbox.getSouth()+','+bbox.getWest()+','+bbox.getEast()); \n");
//      sb.append("          var str = bbox.getNorth()+','+bbox.getSouth()+','+bbox.getWest()+','+bbox.getEast(); \n");
//      sb.append("          var str=\"test\";                              \n");
//      sb.append("          return str'; \n");
      sb.append("                                      \n");
      sb.append("                                                                            \n");
//      sb.append("          isViewRefreshNeeded = false;                                      \n");
      sb.append("     } \n");
      sb.append(" \n");
      sb.append(" \n");
        
//        sb.append("     function clearWctLayer() { \n");
////        if (viewController == ViewController.GOOGLE_EARTH) {
////            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 1) { \n");
////        }
////        else {
//            sb.append("         if (ge.getFeatures().hasChildNodes() && ge.getFeatures().getChildNodes().getLength() > 0) { \n");
////        }
//        sb.append("             ge.getFeatures().removeChild(ge.getFeatures().getLastChild());  \n");     
//        sb.append("         }  \n");
//        sb.append("     }  \n");
        
        sb.append("                                                                                                             \n");
        sb.append("     </script>                                                                                               \n");
        sb.append("   </head>                                                                                                   \n");
//        sb.append("   <body onload='init()' id='body'>  \n");
        sb.append("   <body onload='init()' id='body' style='font-family: arial, sans-serif; font-size: 13px;' scroll='no'>         \n");
//        sb.append("     <div id=\"map3d\" style=\"width: 500px; height: 380px;\"></div>     \n");
        sb.append("     <div id='map3d' style='width: 100%; height: 100%; '></div>    \n");
//        sb.append("     <br>                                                                                                    \n");
//        sb.append("     <div>Installed Plugin Version: <span id=\"installed-plugin-version\" style=\"font-weight: bold;\">Loading...</span></div> \n");
        sb.append("   </body>                                                                                                   \n");
        sb.append(" </html>                                                                                                     \n");

        
        String[] lines = sb.toString().split("\n");
        for (int n=0; n<lines.length; n++) {
            System.out.println(lines[n]);
//            System.out.println(n+": "+lines[n]);
        }
        
        
        return sb.toString();
    }
    
    
    
    
    
    
    public void refreshView(boolean flyToExtent) {
        refreshView(flyToExtent, null, null);
    }
    

    public void refreshView(boolean flyToExtent, String type, String layerName) {
        
        StringBuilder sb = new StringBuilder();
        
        Rectangle2D.Double extent = grass.getRegionInfoExtent();
        flyToExtent(extent);
        
        sb.append("'<?xml version=\"1.0\" encoding=\"UTF-8\"?>            '+ \n");
        sb.append("'<kml xmlns=\"http://www.opengis.net/kml/2.2\"         '+ \n");
        sb.append("'     xmlns:gx=\"http://www.google.com/kml/ext/2.2\"   '+ \n");
        sb.append("'     xmlns:kml=\"http://www.opengis.net/kml/2.2\"     '+ \n");
        sb.append("'     xmlns:atom=\"http://www.w3.org/2005/Atom\">      '+ \n");
        sb.append("'<Document> '+ \n");
        sb.append("'<Folder> '+ \n");
        
        
        
        
        	sb.append("'<GroundOverlay>                                 '+ \n");
        	sb.append("' <name>WCT Image Overlay</name>                 '+ \n");
        	sb.append("' <Icon>                                         '+ \n");
        	if (layerName == null) {
        		sb.append("'     <href>http://localhost:"+port+"/image?cacheBuster="+
        				(int)(Math.random()*10000000)+"</href>   '+ \n");
        	}
        	else {
        		sb.append("'     <href>http://localhost:"+port+"/image?cacheBuster="+
        			(int)(Math.random()*10000000)+
        			";LAYER="+layerName+";TYPE="+type+"</href> '+ \n");
        	}
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
       
        
//        if (showLegend) {
//            sb.append("'<ScreenOverlay>                                 '+ \n");
//            sb.append("' <name>WCT Legend Overlay</name>                '+ \n");
//            sb.append("' <Icon>                                         '+ \n");
//            sb.append("'     <href>http://localhost:"+port+"/legend?cacheBuster="+(int)(Math.random()*10000000)+"</href>   '+ \n");
//            sb.append("'     <viewBoundScale>0.75</viewBoundScale>      '+ \n");
//            sb.append("' </Icon>                                        '+ \n");
//            sb.append("' <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> '+ \n");
//            sb.append("' <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> '+ \n");
//            sb.append("'</ScreenOverlay>                                 '+ \n");
//        }
        
        try {

            String kmlGeomOutline = KMLUtils.getExtentLineKML(extent);
            String kmlGeomBackground = KMLUtils.getExtentPolyKML(extent);

            sb.append("'    <Placemark>                            '+\n");

            sb.append("'<LookAt> '+\n");
            sb.append("'  <longitude>"+extent.getCenterX()+"</longitude>  '+\n");
            sb.append("'  <latitude>"+extent.getCenterY()+"</latitude>    '+\n");
            sb.append("'  <altitude>1000</altitude>               '+\n");
            sb.append("'  <range>"+ (Math.max(extent.getWidth(), extent.getHeight()) *120*1000) +"</range>   '+\n");
            sb.append("'  <altitudeMode>clampToGround</altitudeMode>         '+\n");
            sb.append("'</LookAt>                              '+\n");

            
            sb.append("'      <name>Outline</name>                 '+\n");
            sb.append("'      <visibility>1</visibility>           '+\n");
            sb.append("'      <styleUrl>#outlineStyle</styleUrl>   '+\n");
//            if (extent.getWidth() < 360) {
//                sb.append(convertToJavascriptString(kmlGeomOutline));
//            }
            sb.append("'    </Placemark>                           '+\n");
            

            
            

    		
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
//        webBrowser.executeJavascript("ge.getFeatures().getLastChild().setOpacity("+layerOpacity+");");

    }
    
    public void flyToExtent(Rectangle2D extent) {
        int range = (int)(Math.max(extent.getWidth(), extent.getHeight()) *100*1000);
        final String javascript = "flyToBBoxExtent("+extent.getCenterX()+","+extent.getCenterY()+","+range+");";
        System.out.println(javascript);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                webBrowser.executeJavascript(javascript);
            }
        });
    }
    
    
    public void askForBBoxExtent() {
        final String javascript = "getBBoxExtent();";
        System.out.println(javascript);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 webBrowser.executeJavascript(javascript);
//                String bbox = webBrowser.executeJavascriptWithResult(javascript).toString();
//                System.out.println(bbox);
            }
        });
    }

    
}
