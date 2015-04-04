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

import gov.noaa.ncdc.gis.GoogleGeocoder;
import gov.noaa.ncdc.gis.GoogleGeocoder.GoogleGeocodeResult;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentViewType;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.geotools.gui.swing.WCTZoomPane.ZoomMode;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.resources.geometry.XAffineTransform;



public class WCTToolBar extends JToolBar implements MouseListener, MouseMotionListener {


//	Each tools (as set up) has a constant integer to represent it.
//	The constant is used in a switch feature.
	public static final int MOVELEFT = 0;
	public static final int MOVERIGHT = 1;
	public static final int MOVEUP = 2;
	public static final int MOVEDOWN = 3;

	public static final int PREVZOOM = 4;
	public static final int NEXTZOOM = 5;

	public static final int ZOOMIN = 20;			
	public static final int PAN = 21;
	public static final int ZOOMOUT = 22;
	public static final int GETFEATURE = 23;
	public static final int MARKER = 24;
	public static final int BOOKMARK = 25;
    public static final int IDENTIFY = 26;
    public static final int DATA_SEARCH = 27;
    public static final int GEOCODER = 28;

	public static Cursor ZOOMIN_CURSOR;
	public static Cursor RECENTER_CURSOR;
	public static Cursor ZOOMOUT_CURSOR;
	public static Cursor MARKER_CURSOR;
    public static Cursor IDENTIFY_CURSOR;
    public static Cursor DATA_SEARCH_CURSOR;

//	The initially default tool;
	protected int toolInEffect = ZOOMIN;

//	The ZoomFactor and ZoomOutFactor determine the scale of zoom
//	that occurs when a user uses the zoom tools. Typically a user
//	will want to zoom out faster than zoom in. The zoom factors
//	act in linear but opposite manners. They are used as follows:
//	ZoomFactor: width/ZoomFactor = new width. 2-->the scale 
//	as a relative fraction is doubled.
//	ZoomOutFactor: width*ZoomOutFactor = new width. 2--> the
//	scale as a relative fraction is halved. 
	protected static double ZoomFactor = 1.3;
	protected static double ZoomOutFactor = 2.0;
	protected static double panRatio = 0.25;

	protected WCTMapPane wctMapPane;	

	// Holds previous zoom extents
	private Vector<Rectangle2D.Double> zoomExtentVector;
	private int zoomIndex = 0;



	private WCTViewer viewer;

	
    private JButton rstBut;
    private JButton upBut;
    private JButton downBut;
    private JButton leftBut;
    private JButton rightBut;

    private JButton prevZoomBut;
    private JButton nextZoomBut;


    //JToggleButton getBut = new JToggleButton (new GetFeatureAction()); getBut.setText("");
    private JToggleButton panBut;
    private JToggleButton zmInBut;
    private JToggleButton zmOutBut;
    private JToggleButton markerBut;
    private JToggleButton identifyBut;
    private JToggleButton dataSearchBut;
    private JButton bookmarkBut;
    private JButton geocoderBut;

	
	private boolean isMousePressed = false;
//	private java.awt.geom.Point2D panStartExtentCenter = null;
	private java.awt.Point panStartExtentCenter = null;
	private java.awt.geom.Point2D panStartDragPoint = null;
	private boolean isReset = false;


//	Constructor	
	public WCTToolBar(WCTViewer viewer) {
		super();
		this.viewer = viewer;
		this.wctMapPane = viewer.getMapPane();
		createGUI();
		//addMap();
	}


//	Put all of the pieces together in the JFrame;
	public void createGUI() {


		//	Define buttons based on Action internal (word?) classes defined below;	
		rstBut = new JButton (new ResetAction()); rstBut.setText("");
		upBut = new JButton(new UpAction()); upBut.setText("");
		downBut = new JButton(new DownAction()); downBut.setText("");
		leftBut = new JButton (new LeftAction()); leftBut.setText("");
		rightBut = new JButton(new RightAction()); rightBut.setText("");

		prevZoomBut = new JButton(new PrevZoomAction()); prevZoomBut.setText("");
		nextZoomBut = new JButton(new NextZoomAction()); nextZoomBut.setText("");


		//JToggleButton getBut = new JToggleButton (new GetFeatureAction()); getBut.setText("");
		panBut = new JToggleButton (new PanAction()); panBut.setText("");
		zmInBut = new JToggleButton (new ZoomInAction()); zmInBut.setText("");
		zmOutBut = new JToggleButton (new ZoomOutAction()); zmOutBut.setText("");
		markerBut = new JToggleButton (new MarkerAction()); markerBut.setText("");
        identifyBut = new JToggleButton (new IdentifyAction()); identifyBut.setText("");
		bookmarkBut = new JButton (new BookmarkAction()); 
        dataSearchBut = new JToggleButton (new DataSearchAction()); identifyBut.setText("");
        geocoderBut = new JButton (new GeocoderAction()); 
        
		//	Define the JToolBar, add buttons, and add it to the cPane;

		this.add(rstBut);
		this.add(leftBut);
		this.add(upBut);
		this.add(downBut);
		this.add(rightBut);
		this.addSeparator();

		ButtonGroup group = new ButtonGroup();		
		group.add(zmInBut);
		group.add(zmOutBut);		
		group.add(panBut);
		//group.add(getBut);
		group.add(bookmarkBut);
		group.add(markerBut);
        group.add(identifyBut);
        group.add(dataSearchBut);

		zmInBut.setSelected(true);
		this.add(zmInBut);
		this.add(panBut);
		this.add(zmOutBut);
//		this.add(prevZoomBut);
		//this.add(nextZoomBut);
		this.addSeparator();
		this.add(bookmarkBut);
		this.add(markerBut);
        this.add(identifyBut);
//		this.add(dataSearchBut);
        this.add(geocoderBut);

		// create cursors
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		try {
//			Image zoominImg = new ImageIcon(JNXFrameTools.class.getResource("/icons/ZoomIn24.gif")).getImage();
//			Image recenterImg = new ImageIcon(JNXFrameTools.class.getResource("/icons/AlignCenter16.gif")).getImage();
//			Image zoomoutImg = new ImageIcon(JNXFrameTools.class.getResource("/icons/ZoomOut16.gif")).getImage();
//			Image markerImg = new ImageIcon(WCTToolBar.class.getResource("/icons/bluepin-cursor.gif")).getImage();
			Image markerImg = new ImageIcon(WCTToolBar.class.getResource("/icons/MarkerMouseIcon.png")).getImage();
            Image identifyImg = new ImageIcon(WCTToolBar.class.getResource("/icons/QueryMouseIcon.png")).getImage();

			System.out.println(WCTToolBar.class.getResource("/icons/ZoomInEnable.png"));
			
			
			Image zoominImg = new ImageIcon(WCTToolBar.class.getResource("/icons/ZoomInEnable.png")).getImage();
			Image recenterImg = new ImageIcon(WCTToolBar.class.getResource("/icons/PanEnable.png")).getImage();
			Image zoomoutImg = new ImageIcon(WCTToolBar.class.getResource("/icons/ZoomOutEnable.png")).getImage();



			//ZOOMIN_CURSOR = toolkit.createCustomCursor(zoominImg, new Point(), "Zoom In");
			ZOOMIN_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
//			RECENTER_CURSOR = toolkit.createCustomCursor(recenterImg, new Point(), "Pan");
			RECENTER_CURSOR = new Cursor(Cursor.HAND_CURSOR);
			//ZOOMOUT_CURSOR = toolkit.createCustomCursor(zoomoutImg, new Point(), "Zoom Out");
			ZOOMOUT_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
			MARKER_CURSOR = toolkit.createCustomCursor(markerImg, new Point(5, 27), "Add Marker");
            IDENTIFY_CURSOR = toolkit.createCustomCursor(identifyImg, new Point(5, 28), "Identify");
            DATA_SEARCH_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

		} catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, "Error creating cursor: "+e, 
					"CURSOR ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);         
			e.printStackTrace();
		}
		setToolInEffect(ZOOMIN);
	}



	protected void setZoomIn(java.awt.geom.Point2D pnt) {
		Rectangle2D vizArea = wctMapPane.getVisibleArea();
		double width = vizArea.getWidth()/ZoomFactor;
		double height = vizArea.getHeight()/ZoomFactor;
		double x = pnt.getX() - (0.5 * width);
		double y = pnt.getY() - (0.5 * height);
		Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
		wctMapPane.setVisibleArea(newArea);

	}
	protected synchronized void setRecenter(java.awt.geom.Point2D pnt) {
		Rectangle2D vizArea = wctMapPane.getVisibleArea();
		double width = vizArea.getWidth();
		double height = vizArea.getHeight();
		double x = pnt.getX() - (0.5 * width);
		double y = pnt.getY() - (0.5 * height);
		Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
		wctMapPane.setVisibleArea(newArea);

	}
	protected void setZoomOut(java.awt.geom.Point2D pnt) {
		Rectangle2D vizArea = wctMapPane.getVisibleArea();
		double width = vizArea.getWidth()*ZoomOutFactor;
		double height = vizArea.getHeight()*ZoomOutFactor;
		double x = pnt.getX() - (0.5 * width);
		double y = pnt.getY() - (0.5 * height);
		Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
		wctMapPane.setVisibleArea(newArea);
	}
	protected void addMarker(java.awt.geom.Point2D pnt) {
		double x = pnt.getX();
		double y = pnt.getY();
		MarkerEditor me = viewer.showMarkerEditor();
		if (me.isAutoReverseGeocode()) {
			try {
				GoogleGeocoder geocoder = new GoogleGeocoder();
				GoogleGeocodeResult ggr = geocoder.locationSearch(y+","+x, viewer.getCurrentExtent()).get(0);
				me.setMarker(ggr.getLon(), ggr.getLat(), ggr.getFormattedAddress(), "", "");				
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error using Google Geocoding Service.  " +
						"Please validate input search location and verify internet connection", 
						"Geocoding Service Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			if (me.isFillLabelWithInfo()) {
				String label_1 = viewer.getStatusBar().getCoordinate();
				String label_2 = viewer.getStatusBar().getAzRan();
				String label_3 = viewer.getStatusBar().getZHeight();
				if (viewer.getStatusBar().getAzRan().trim().length() == 0) {
					me.setMarker(x, y, "", label_1, label_3);	
				}
				else {
					me.setMarker(x, y, label_1, label_2, label_3);				
				}
			}
			else {
				me.setMarker(x, y, "", "", "");
			}
		}
	}
	protected void addBookmark() {
		viewer.showBookmarkEditor().addBookmark();
	}

	protected void showGeocoder() {
		try {
			SearchDialog ggd = SearchDialog.getInstance(viewer);
			ggd.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void identify(Point2D center) {
	    
	    IdentifyUI identifyTool = viewer.showIdentifyTool();
	    identifyTool.identify(center);	    
	    
	}

	protected void dataSearch(Point2D center) {
	    
	    System.out.println("in data search!");
	    
//	    DataSearchUI dataSearchUI = new DataSearchUI(viewer, center);
//	    dataSearchUI.dispose();
	    
	}
	

	/**
	 * Adds zoom extent to the memory of previous zoom extents
	 */
	protected void addZoomExtent(Rectangle2D.Double extent) {
//		if (zoomExtentVector == null) {
//			zoomExtentVector = new Vector();
//		}
//
//		if (zoomIndex == 0 || (zoomIndex > 0 && ! extent.equals(zoomExtentVector.get(zoomIndex-1)))) {
//			System.out.println("ADDING ZOOM :::: ZOOM VECTOR SIZE: "+zoomExtentVector.size()+"  index="+zoomIndex);         
//			zoomExtentVector.setSize(zoomIndex);
//			zoomExtentVector.addElement(extent);
//			zoomIndex++;
//		}

	}







	/**
	 * Based on the point clicked, this routine gets a feature within
	 * a specified number of pixels (hardcoded as 5). If more that 1
	 * feature is selected it will zoom in on the selected features
	 * and write a message in the status bar.  
	 * 
	 * @param pnt
	 */
	/*
	protected void getFeature(java.awt.geom.Point2D pnt) {
		GeometryDistanceFilter filt = null;
	//  Get basic points of click;
	//  Note that Coordinate and Envelope are both VividSolutions classes.
	//  com.vividsolutions.jts.geom.Coordinate and .Envelope.
		double x = pnt.getX();
		double y = pnt.getY();

                //VividSolutions Coordinate
		Coordinate cp1 = new Coordinate(x,y); 
		Coordinate cp2 = new Coordinate(x+0.000001,y+0.000001);

	//  Filter factory for creating spatial filter.
		FilterFactory ff = new FilterFactoryImpl();

		try {	
		//	Sets up a spatial filter to look for all features in the 
		//  queryLayer that are within 5 pixels of the point clicked.
		// 	If you want to know more about this you'll have to encourage 
		// 	Chris Holmes to write his filter tutorial. By the way, there is
		//	probably a better way to do some of this.
			filt = ff.createGeometryDistanceFilter(Filter.GEOMETRY_DWITHIN);
			Envelope bbArea = new Envelope(cp1,cp2); //vividsolutions Envelope
			BBoxExpression bb = ff.createBBoxExpression(bbArea);
			filt.addRightGeometry(bb);
		//  Set search distance to 5 pixels. It is converted to the equivilant
		//  distance in mapunits.
			filt.setDistance(5/mapPane.getScaleFactor());
			filt.addLeftGeometry(
				ff.createAttributeExpression(
					queryLayer.getFeatureSource().getSchema(), 
					queryLayer.getFeatureSource().getSchema().getDefaultGeometry().getName()));
		}
		catch(IllegalFilterException e){
			statusBar.setText("Illegal Filter!");
			e.printStackTrace();
		}		

		try {
		// Get feature source from queryLayer and then use developed filter
		//	to get feature results containing only features meeting filter.
			FeatureSource fs = queryLayer.getFeatureSource();	
			FeatureResults fr = fs.getFeatures(filt);
		//	If the count is greater than 1 feature then zoom into the area
		//  of interest so the user can get a better shot at selecting.
			if (fr.getCount() > 1) {
				statusBar.setText("Too Many Features Selected. Zooming to area");
				Envelope vividEnv = fr.getBounds(); // vividsolutions.jts.geo.Envelope.
				Rectangle2D rect = new Rectangle2D.Double(
						vividEnv.getMinX(),vividEnv.getMinY(),
						vividEnv.getWidth(),vividEnv.getHeight());
				mapPane.setVisibleArea(rect);
			}
		//	If no features were selected write a message to the status bar.
			else if (fr.getCount() == 0) {
				statusBar.setText("No featues selected.");
			}
		//	If a single feature was selected (what we want) then
		//	grab it and do something with it. In this case we will
		//	send it to a highlight simulator.
			else {
				FeatureReader reader = fr.reader();
				Feature f = null;
				Feature fArr[] = new Feature[1];				
				while (reader.hasNext()){
					f = (Feature) reader.next();
				}
			//	The highlight simulator requires an array of features.
				fArr[0] = f;
				highlightFeatures(fArr);				
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	protected void highlightFeatures(Feature fArr[]){		
	//  Clear old highlighted layer highlight MemoryDataStore;
		if (map.indexOf(highlightLayer) >= 0) {
			map.removeLayer(highlightLayer);
			highlightLayer = null;
		}
		highlight = null;			
		highlight = new MemoryDataStore();

	//	Check that at least the first member of fArr is not null before 
	// 	attempting to set up new highlighted feature.			
		if (fArr[0] != null) {
		//  Add feature array to the MemoryDataStore and extract a FeatureSource.
			highlight.addFeatures(fArr);

			FeatureSource fs = null;
			try {
				fs = highlight.getFeatureSource(queryLayerType);
			} catch (IOException e) {
				statusBar.setText("Error creating highlight FeatureSource!");
				e.printStackTrace();
			}
		//  Define the style;
			StyleBuilder sb = new StyleBuilder();
			PolygonSymbolizer sym = sb.createPolygonSymbolizer(Color.YELLOW,Color.BLACK,3);
			sym.getFill();
			Style sty = sb.createStyle(sym);
			//Add the layer;	
			highlightLayer = new DefaultMapLayer(fs, sty);
			map.addLayer(highlightLayer);
		}
	}
	 */




	public void mouseClicked(MouseEvent e) {
		// only allow left button
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		java.awt.geom.Point2D pnt = null;
		GeoMouseEvent ge = (GeoMouseEvent) e; 
		java.awt.geom.Point2D center=ge.getMapCoordinate(pnt);
		int theToolNum = getToolInEffect();

		switch (theToolNum) {
		case ZOOMOUT:  //ZOOMOUT
			setZoomOut(center);
			break;
		case PAN:  //RECENTER
//			setRecenter(center);
			break;
		case GETFEATURE:  //GETFEATURE
			//getFeature(center);
			break;
		case MARKER:  //MARKER
			addMarker(center);
			break;
        case IDENTIFY:  //IDENTIFY
            identify(center);
            break;
        case DATA_SEARCH:  //DATA SEARCH
            dataSearch(center);
            break;
		default: //ZOOMIN
//			if (e.getClickCount() > 1) {
//			setFlyIn(center);
//			}
//			else {
			setZoomIn(center);	
//		}
		}
	}



    //	Unused functions of the MouseListener Interface.	
	public void mouseExited(MouseEvent e) {}		
	public void mouseReleased(MouseEvent e) {
		isMousePressed = false;
		if (getToolInEffect() == PAN) {
			wctMapPane.setMouseSelectionShape(new Rectangle2D.Float());           
		}
	}		
	public void mouseEntered(MouseEvent e) {}		
	public void mousePressed(MouseEvent e) {
		isMousePressed = true;		

		if (getToolInEffect() == PAN) {
			wctMapPane.setZoomMode(ZoomMode.PAN);


			GeoMouseEvent ge = (GeoMouseEvent) e; 
			java.awt.geom.Point2D center = ge.getMapCoordinate(null);


			this.panStartExtentCenter = new java.awt.Point(e.getX(), e.getY());

			this.panStartDragPoint = center;

//			System.out.println(panStartExtentCenter+" <--> "+panStartDragPoint);
			wctMapPane.setMouseSelectionShape(null);	          
		}
		else if (getToolInEffect() == ZOOMOUT) {
			wctMapPane.setZoomMode(ZoomMode.ZOOMOUT);
		}
		else if (getToolInEffect() == ZOOMIN) {
			wctMapPane.setZoomMode(ZoomMode.ZOOMIN);
		}
		else if (getToolInEffect() == MARKER) {
			wctMapPane.setZoomMode(ZoomMode.ZOOMIN);
		}
	}


	public void mouseDragged(MouseEvent e) {
		if (getToolInEffect() == PAN) {

			if (! e.getClass().equals(GeoMouseEvent.class)) {
				return;
			}


			AffineTransform af = wctMapPane.getZoom();
			AffineTransform change = null;
			try {
				change = af.createInverse();
			} catch (NoninvertibleTransformException excp) {
				System.out.println(excp);
				return;
			}
			int changeX = e.getX()-panStartExtentCenter.x;
			int changeY = e.getY()-panStartExtentCenter.y;

			change.translate(changeX, changeY);

			panStartExtentCenter.x = e.getX();
			panStartExtentCenter.y = e.getY();



			change.concatenate(af);
			XAffineTransform.round(change);
			wctMapPane.transform(change);

		}
	}


	public void mouseMoved(MouseEvent arg0) {
//		System.out.println("IS MOVING");

	}

	class ResetAction extends AbstractAction { 
		public ResetAction () {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/WebComponent16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/ResetExtentEnable.png"));
			putValue(NAME, "Full Extent");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Reset the map to its full extent");
			putValue(ACTION_COMMAND_KEY, "RESET");
		}
		public void actionPerformed(ActionEvent e) {
			double border = 0.05;  // leave a 5% border;
			//Rectangle2D area = fullArea;
			Rectangle2D area = wctMapPane.getPreferredArea();
			double width = area.getWidth();
			double height = area.getHeight();
			double newX = area.getX() - border*width;
			double newY = area.getY() - border*height;
			Rectangle2D.Double newArea = 
				new Rectangle2D.Double(
						newX,newY,width+(width*2*border),height+(height*2*border));
			wctMapPane.setVisibleArea(newArea);		
		}
	}

	class LeftAction extends AbstractAction { 
		public LeftAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/Back16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/arrow-left.png"));
			putValue(NAME, "Pan Left");
			putValue(SMALL_ICON, theIcon);

			putValue(SHORT_DESCRIPTION, "Move West (Left)");
			putValue(ACTION_COMMAND_KEY, "WEST");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = wctMapPane.getVisibleArea();
			double newX = area.getX() - panRatio*area.getWidth();
			Rectangle2D.Double newArea =
				new Rectangle2D.Double(newX,area.getY(),area.getWidth(),area.getHeight());
			wctMapPane.setVisibleArea(newArea);		
		}
	}
	class RightAction extends AbstractAction { 
		public RightAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/Forward16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/arrow-right.png"));
			putValue(NAME, "Pan Right");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move East (Right)");
			putValue(ACTION_COMMAND_KEY, "EAST");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = wctMapPane.getVisibleArea();
			double newX = area.getX() + panRatio*area.getWidth();
			Rectangle2D.Double newArea =
				new Rectangle2D.Double(newX,area.getY(),area.getWidth(),area.getHeight());
			wctMapPane.setVisibleArea(newArea);		
		}
	}	
	class UpAction extends AbstractAction { 
		public UpAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/Up16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/arrow-up.png"));
			putValue(NAME, "Pan Up");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move North (Up)");
			putValue(ACTION_COMMAND_KEY, "NORTH");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = wctMapPane.getVisibleArea();
			double newY = area.getY() + panRatio*area.getHeight();
			Rectangle2D.Double newArea = new Rectangle2D.Double(area.getX(),newY,area.getWidth(),area.getHeight());
			wctMapPane.setVisibleArea(newArea);	
		}
	}
	class DownAction extends AbstractAction { 
		public DownAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/Down16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/arrow-down.png"));
			putValue(NAME, "Pan Down");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move South (Down)");
			putValue(ACTION_COMMAND_KEY, "SOUTH");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = wctMapPane.getVisibleArea();
			double newY = area.getY() - panRatio*area.getHeight();
			Rectangle2D.Double newArea =
				new Rectangle2D.Double(area.getX(),newY,area.getWidth(),area.getHeight());
			wctMapPane.setVisibleArea(newArea);	
		}
	}

	class PanAction extends AbstractAction { 
		public PanAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/AlignCenter16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/PanEnable.png"));
			putValue(NAME, "Pan");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Pan");
			putValue(ACTION_COMMAND_KEY, "PAN");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(PAN);	
		}
	}	
	class ZoomInAction extends AbstractAction { 
		public ZoomInAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/ZoomIn16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/ZoomInEnable.png"));
			putValue(NAME, "ZoomIn");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Zoom to Click Point");
			putValue(ACTION_COMMAND_KEY, "ZOOMIN");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(ZOOMIN);	
		}
	}	
	class ZoomOutAction extends AbstractAction { 
		public ZoomOutAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/ZoomOut16.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/ZoomOutEnable.png"));
			putValue(NAME, "ZoomOut");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Zoom Out From Click Point");
			putValue(ACTION_COMMAND_KEY, "ZOOMOUT");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(ZOOMOUT);
		}
	}	
	class GetFeatureAction extends AbstractAction { 
		public GetFeatureAction() {
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/About16.gif"));
			putValue(NAME, "GetFeature");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Get A Feature");
			putValue(ACTION_COMMAND_KEY, "GETFEATURE");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(GETFEATURE);	
		}
	}
	class MarkerAction extends AbstractAction { 
		public MarkerAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/bluepin.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/marker2.png"));
			putValue(NAME, "Marker");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Add a Marker");
			putValue(ACTION_COMMAND_KEY, "MARKER");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(MARKER);	
		}
	}
    class IdentifyAction extends AbstractAction { 
        public IdentifyAction() {
//          ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/bluepin.gif"));
            ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/QueryEnable.png"));
            putValue(NAME, "Identify");
            putValue(SMALL_ICON, theIcon);
            putValue(SHORT_DESCRIPTION, "Identify Data Layers");
            putValue(ACTION_COMMAND_KEY, "IDENTIFY");
        }
        public void actionPerformed(ActionEvent e) {
            setToolInEffect(IDENTIFY);    
        }
    }
	class BookmarkAction extends AbstractAction { 
		public BookmarkAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/bluepin.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/bookmark.png"));
//			putValue(NAME, "Bookmark");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Add a Bookmark");
			putValue(ACTION_COMMAND_KEY, "BOOKMARK");
		}
		public void actionPerformed(ActionEvent e) {
			addBookmark();
		}
	}
	
	class GeocoderAction extends AbstractAction { 
		public GeocoderAction() {
//			ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/bluepin.gif"));
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/textsearch.png"));
//			putValue(NAME, "Bookmark");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Show Geocoder Dialog");
			putValue(ACTION_COMMAND_KEY, "GEOCODER");
		}
		public void actionPerformed(ActionEvent e) {
			showGeocoder();
		}
	}	
	
	   class DataSearchAction extends AbstractAction { 
	        public DataSearchAction() {
//	          ImageIcon theIcon = new ImageIcon(JNXFrameTools.class.getResource("/icons/bluepin.gif"));
//	            ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/bookmark.png"));
	            putValue(NAME, "Data Search");
//	            putValue(SMALL_ICON, theIcon);
	            putValue(SHORT_DESCRIPTION, "Data Search");
	            putValue(ACTION_COMMAND_KEY, "DATA SEARCH");
	        }
	        public void actionPerformed(ActionEvent e) {
	            setToolInEffect(DATA_SEARCH);
	        }
	    }

	
	
	class PrevZoomAction extends AbstractAction { 
		public PrevZoomAction() {
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/left16.gif"));
			putValue(NAME, "PrevZoom");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Previous Zoom Extent");
			putValue(ACTION_COMMAND_KEY, "PREVZOOM");
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("ZOOM VECTOR SIZE: "+zoomExtentVector.size()+"  index="+zoomIndex);         
			if (zoomIndex == 0) {
				return;
			}
			zoomIndex--;

			if (zoomIndex > 0) {
				Rectangle2D.Double newArea = (Rectangle2D.Double)zoomExtentVector.elementAt(zoomIndex-1); 
				wctMapPane.setVisibleArea(newArea);		
			}
		}
	}
	class NextZoomAction extends AbstractAction { 
		public NextZoomAction() {
			ImageIcon theIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/right16.gif"));
			putValue(NAME, "NextZoom");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Next Zoom Extent");
			putValue(ACTION_COMMAND_KEY, "NEXTZOOM");
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("ZOOM VECTOR SIZE: "+zoomExtentVector.size()+"  index="+zoomIndex);         
			if (zoomIndex == zoomExtentVector.size()) {
				return;
			}

			zoomIndex++;
			Rectangle2D.Double newArea = (Rectangle2D.Double)zoomExtentVector.elementAt(zoomIndex); 
			wctMapPane.setVisibleArea(newArea);		
		}
	}

	/**
	 * @return
	 */
	public int getToolInEffect() {
		return toolInEffect;
	}

	/**
	 * @param string
	 */
	public void setToolInEffect(int theToolNum) {
		toolInEffect = theToolNum;

		// set cursor to icon
		switch (theToolNum) {
		case PAN:
			wctMapPane.setCursor(RECENTER_CURSOR);
			break;
		case ZOOMOUT:
			wctMapPane.setCursor(ZOOMOUT_CURSOR);
			break;
		case MARKER:
			wctMapPane.setCursor(MARKER_CURSOR);
			break;
        case IDENTIFY:
            wctMapPane.setCursor(IDENTIFY_CURSOR);
            break;
        case DATA_SEARCH:
            wctMapPane.setCursor(DATA_SEARCH_CURSOR);
            break;
		default:
			wctMapPane.setCursor(ZOOMIN_CURSOR);
		}
	}
	/**
	 * Convience method to create a polygon layer based on shapefile 
	 * name and desired fill color. The outline is set to narrow and black.
	 * The opacity defaults to 0.7 since I don't know what layers this will
	 * have to coexist with. After generating a FeatureStore this calls 
	 * makePolyLayer(FeatureSource,Color) to add symbology;
	 * @param	inPath The complete path to the shape file;
	 * @param	inFile The file name to a shapefile containing lines.
	 * @param	hue The polygon fill color as type java.awt.Color (for example Color.black).
	 */
	/*
	public DefaultMapLayer makePolyLayer(String inPath, String inFile, Color hue) {
		//DefaultMapLayer aLayer = null;
		FeatureSource fs = null;
		try {
			URL rdURL = new URL("file:///" + inPath + inFile);	
		  //Create the ShapeFilDataStore and get freature source;  
			ShapefileDataStore ds = new ShapefileDataStore(rdURL);
			fs = ds.getFeatureSource(inFile.replaceAll(".shp",""));				

		} catch (MalformedURLException e) {
			System.err.println("Bad Shapefile Name or Shapefile not found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	//	call makePolyLayer with feature soruce to assign symbology;
		return makePolyLayer(fs,hue);
		//return aLayer;
	}
	 */
	/**
	 * Convience method to create a polygon layer based on a featureSource 
	 * and desired fill color. The outline is set to narrow and black.
	 * The opacity defaults to 0.7 since I don't know what layers this will
	 * have to coexist with.
	 * @param	fs A FeatureSource.
	 * @param	hue The polygon fill color as type java.awt.Color (for example Color.black).
	 */
	/*
	public DefaultMapLayer makePolyLayer(FeatureSource fs, Color hue) {
		//DefaultMapLayer aLayer = null;				
	//  Define the style;
		StyleBuilder sb = new StyleBuilder();
		PolygonSymbolizer sym = sb.createPolygonSymbolizer(hue, Color.BLACK, 0);
		sym.getFill().setOpacity(sb.literalExpression(0.7));
		Style sty = sb.createStyle(sym);
	//  Create and return the layer;	
		return new DefaultMapLayer(fs, sty);		
		//return aLayer;
	}	
	 */



	/**
	 * Convience method to create a line layer based on shapefile 
	 * name and desired color.
	 * @param	inPath Path to the shape file.
	 * @param	inFile The file name to a shapefile containing lines.
	 * @param	hue The color of the lines as type java.awt.Color (for example Color.black).
	 */
	/*
	public DefaultMapLayer makeLineLayer(String inPath, String inFile, Color hue){
		DefaultMapLayer aLayer = null;
		try {
			URL rdURL = new URL("file:///" + inPath + inFile);		
		  //Create the ShapeFilDataStore and then derive the freature source from it;  
			ShapefileDataStore ds = new ShapefileDataStore(rdURL);
			FeatureSource fs = ds.getFeatureSource(inFile.replaceAll(".shp",""));

		  //Define the style;
			StyleBuilder sb = new StyleBuilder();
			LineSymbolizer sym = sb.createLineSymbolizer(hue, 2);
			Style sty = sb.createStyle(sym);
		  //Add the layer;	
		  aLayer = new DefaultMapLayer(fs, sty);
		} 
		catch (MalformedURLException e) {
			System.err.println("Bad Shapefile Name or Shapefile not found.");
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return aLayer;
	}
	 */
	/*

	public CoordinateSystem setCS(){		
		String wkt = "PROJCS[\"NAD_1983_StatePlane_NC_FIPS_3200\", "
					+ 	"GEOGCS[\"GCS_North_American_1983\", "
					+	"DATUM[\"D_North_American_1983\", "
					+	"SPHEROID[\"GRS_1980\",6378137,298.257222101]], "
					+ 	"PRIMEM[\"Greenwich\",0], "
					+	"UNIT[\"Degree\",0.017453292519943295]], "
					+	"PROJECTION[\"Lambert_Conformal_Conic_2SP\"], "
					+	"PARAMETER[\"False_Easting\",609601.22], "
					+	"PARAMETER[\"False_Northing\",0], "
					+	"PARAMETER[\"Central_Meridian\",-79], "
					+	"PARAMETER[\"Standard_Parallel_1\",34.33333333333334], "
					+	"PARAMETER[\"Standard_Parallel_2\",36.16666666666666], "
					+	"PARAMETER[\"Latitude_Of_Origin\",33.75], "
					+	"UNIT[\"Meter\",1]]";
		//System.out.println("WKT IS: " + wkt);
		CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
		try {
			mapCS = csFactory.createFromWKT(wkt);
		}
		catch (FactoryException fe) {
			System.err.println(fe.getMessage());
			fe.printStackTrace();
		}		
		return mapCS;	
	}


	 */









	
	public void setCurrentView(CurrentViewType viewType) {
//	    if (viewType == CurrentViewType.GEOTOOLS) {
	    if (viewType != CurrentViewType.GOOGLE_EARTH) {
	        rstBut.setEnabled(true);
	        upBut.setEnabled(true);
	        downBut.setEnabled(true);
	        leftBut.setEnabled(true);
	        rightBut.setEnabled(true);
	        panBut.setEnabled(true);
	        zmInBut.setEnabled(true);
	        zmOutBut.setEnabled(true);
	        markerBut.setEnabled(true);
            identifyBut.setEnabled(true);
	        bookmarkBut.setEnabled(true);
	    }
//	    else if (viewType == CurrentViewType.GOOGLE_EARTH) {
	    else {
            rstBut.setEnabled(false);
            upBut.setEnabled(false);
            downBut.setEnabled(false);
            leftBut.setEnabled(false);
            rightBut.setEnabled(false);
            panBut.setEnabled(false);
            zmInBut.setEnabled(false);
            zmOutBut.setEnabled(false);
            markerBut.setEnabled(false);
            identifyBut.setEnabled(false);
            bookmarkBut.setEnabled(false);
        }
	}
	
}
