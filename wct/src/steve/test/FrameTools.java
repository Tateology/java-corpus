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

package steve.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.renderer.j2d.Renderer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
/**
 * @author Casson
 */
public class FrameTools extends JFrame implements MouseListener {
	
//	Each tools (as set up) has a constant integer to represent it.
// 	The constant is used in a switch feature.
	public static int ZOOMIN = 0;			
	public static int ZOOMOUT = 1;
	public static int GETFEATURE = 2;
	public static int MOVELEFT = 3;
	public static int MOVERIGHT = 4;
	public static int MOVEUP = 5;
	public static int MOVEDOWN = 6;
//	The initially default tool;
	protected int ToolInEffect = ZOOMIN;

//	The ZoomFactor and ZoomOutFactor determine the scale of zoom
//  that occurs when a user uses the zoom tools. Typically a user
//	will want to zoom out faster than zoom in. The zoom factors
//  act in linear but opposite manners. They are used as follows:
//		ZoomFactor: width/ZoomFactor = new width. 2-->the scale 
//					as a relative fraction is doubled.
//		ZoomOutFactor: width*ZoomOutFactor = new width. 2--> the
//					scale as a relative fraction is halved. 
	protected static int ZoomFactor = 2;
	protected static int ZoomOutFactor = 3;

// An input shapefile, hardcoded for my convience, needs to include full local path;		
	private String shpFile ="c:\\devel\\nexrad\\build\\shapefiles\\states.shp";
	
//	There may be better ways to handle this. But rather than passing
//	the mapPane and mapContext to the routines I define them
//  as instance variable available to the tools in the package.
//	This serves my current purpose and the example. Alternate 
//	ideas on making the tools more independent from the main 
//  very welcome.		
	protected StyledMapPane mapPane = new StyledMapPane();	
	protected MapContext map = new DefaultMapContext();
	private Renderer mapRender;						
	private DefaultMapLayer queryLayer;
	private String queryLayerType;
	private Rectangle2D fullArea = null;
	private CoordinateSystem mapCS;
//  These are used for highlighting selected feature;                 	
	private MemoryDataStore highlight = new MemoryDataStore();
	private DefaultMapLayer highlightLayer;
//  Status bar for warnings
	protected JLabel statusBar = new JLabel("Status Bar");


	public static void main (String[] args) {
		JFrame aFrame = new FrameTools();
		aFrame.setSize(new Dimension(400,450));
		aFrame.addWindowListener(new java.awt.event.WindowAdapter(){
			public void windowClosing(java.awt.event.WindowEvent e)
				{System.exit(0);}});
		aFrame.setVisible(true);
		aFrame.show();
	}
	
	
//	Constructor	
	public FrameTools() {
		super();
		createGUI();
		addMap();
	}


//	Put all of the pieces together in the JFrame;
	public void createGUI() {

	//	In paractice I use a SpringLayout. For simplicity in this
	//	example we will have JPanel with a border layout. The
	//	Toolbar will go in North and the mapPane in the Center,
	//  and a "status bar" (JLabel) at the South.
	
	// The cPane JPanel will be set as the JFrames contentPane.				
		JPanel cPane = new JPanel(new BorderLayout());
		cPane.setOpaque(true);
		cPane.setBackground(Color.LIGHT_GRAY);
		
		

	// 	Setup and add the mapPane. The example is hard-coded for 400x400.
	//	Adding the mouseListener is required.	
		mapPane = new StyledMapPane(setCS());	
		mapPane.setPreferredSize(new Dimension(400,400));
		mapPane.addMouseListener(this);
		cPane.add(mapPane.createScrollPane(),BorderLayout.CENTER);

	//	Define buttons based on Action internal (word?) classes defined below;	
		JButton rstBut = new JButton (new ResetAction()); rstBut.setText("");
		JButton upBut = new JButton(new UpAction()); upBut.setText("");
		JButton downBut = new JButton(new DownAction()); downBut.setText("");
		JButton leftBut = new JButton (new LeftAction()); leftBut.setText("");
		JButton rightBut = new JButton(new RightAction()); rightBut.setText("");
		JToggleButton getBut = new JToggleButton (new GetFeatureAction()); getBut.setText("");
		JToggleButton zmInBut = new JToggleButton (new ZoomInAction()); zmInBut.setText("");
		JToggleButton zmOutBut = new JToggleButton (new ZoomOutAction()); zmOutBut.setText("");

	//	Define the JToolBar, add buttons, and add it to the cPane;
		JToolBar cToolBar = new JToolBar();
		cToolBar.setPreferredSize(new Dimension(400,30));
		
		cToolBar.add(rstBut);
		cToolBar.add(leftBut);
		cToolBar.add(upBut);
		cToolBar.add(downBut);
		cToolBar.add(rightBut);
		cToolBar.addSeparator();
		
		ButtonGroup group = new ButtonGroup();		
		group.add(zmInBut);
		group.add(zmOutBut);		
		group.add(getBut);
		
		cToolBar.add(zmInBut);
		cToolBar.add(zmOutBut);
		cToolBar.add(getBut);
		
		
		cPane.add(cToolBar, BorderLayout.NORTH);
		cPane.add(statusBar, BorderLayout.SOUTH);

	//	Finally, add the cPane to the JFrame;
		setContentPane(cPane);
	}


//	Add the actual layers to the map. For this example there is
//	only one, a polygon layer containing circles of different sizes.
	public void addMap() {
		try {		
		  //Create MapContext Add coordinate system;
		  //Although not used in this example, I left my makeLineLayer method in.  						
		  //DefaultMapLayer lineLayer = makeLineLayer(shapeFile,Color.RED);
			queryLayer = makePolyLayer(shpFile,Color.GRAY);
			queryLayerType = "states";
			map.addLayer(queryLayer);
		//	Store area of map for use by reset function;
			Envelope mapArea = map.getLayerBounds();
			fullArea = new Rectangle2D.Double(
					mapArea.getMinX(),mapArea.getMinY(),mapArea.getWidth(),mapArea.getHeight());

			try {
				mapPane.setMapContext(map);
			} catch (AccessControlException e) {
				System.err.println("Error setting map context!");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		//	I do not understand if this is necessary yet, or under
		//	what circumstances it is necessary. Since I have been
		//	having problems getting the JFrame and JApplet to 
		//	return proper coordinates, I set the CoordinateSystem
		//  at every chance I get.
			mapRender = mapPane.getRenderer();
			mapRender.setCoordinateSystem(mapCS);
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}



	
	
	
	protected void setZoomIn(java.awt.geom.Point2D pnt) {
		Rectangle2D vizArea = mapPane.getVisibleArea();
		double width = vizArea.getWidth()/ZoomFactor;
		double height = vizArea.getHeight()/ZoomFactor;
		double x = pnt.getX() - (0.5 * width);
		double y = pnt.getY() - (0.5 * height);
		Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
		mapPane.setVisibleArea(newArea);

	}
	protected void setZoomOut(java.awt.geom.Point2D pnt) {
		Rectangle2D vizArea = mapPane.getVisibleArea();
		double width = vizArea.getWidth()*ZoomOutFactor;
		double height = vizArea.getHeight()*ZoomOutFactor;
		double x = pnt.getX() - (0.5 * width);
		double y = pnt.getY() - (0.5 * height);
		Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
		mapPane.setVisibleArea(newArea);
	}
	

   

	protected void setFlyIn(final java.awt.geom.Point2D pnt) {
		
      
         gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
            public Object construct() {


      
      double factor;
//      mapPane.setPaintingWhileAdjusting(true);

      Rectangle2D oldArea = mapPane.getVisibleArea();
      
               System.out.println();
               System.out.println(oldArea);
               System.out.println();
               
               
               
      double oldWidth = oldArea.getWidth();
      double oldHeight = oldArea.getHeight();
      double oldX = oldArea.getX();
      double oldY = oldArea.getY();
               
      int numFrames = 120;
      for (int n=0; n<numFrames; n++) {
         factor = (n / (double)numFrames)*(ZoomFactor-1) + 1;
         double width = oldWidth/factor;
         double height = oldHeight/factor;
         double x = ((oldX+(0.5*width)) *(numFrames-n) + pnt.getX()*n)/(double)numFrames - (0.5 * width);
         double y = ((oldY+(0.5*height))*(numFrames-n) + pnt.getY()*n)/(double)numFrames - (0.5 * height);
         Rectangle2D newArea= new Rectangle2D.Double(x,y,width,height);
//System.out.println(":::: n = "+n+" RECT: "+newArea.toString());         
         mapPane.setVisibleArea(newArea);
//         mapPane.repaint();
//         mapPane.updateUI();

//System.out.println(factor+"  ::  "+newArea);

         try {
            Thread.sleep(300/numFrames);
         } catch (Exception e) {
         }
      }
      
               return "DONE";
            }
         };
         worker.start();      
      

	}
   
   
   
   
   
   
   
   
   
   
   
   
   
   
	/**
	 * Based on the point clicked, this routine gets a feature within
	 * a specified number of pixels (hardcoded as 5). If more that 1
	 * feature is selected it will zoom in on the selected features
	 * and write a message in the status bar.  
	 * 
	 * @param pnt
	 */
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
			PolygonSymbolizer sym = sb.createPolygonSymbolizer(Color.YELLOW,Color.BLUE,3);
			sym.getFill();
			Style sty = sb.createStyle(sym);
			//Add the layer;	
			highlightLayer = new DefaultMapLayer(fs, sty);
			map.addLayer(highlightLayer);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		java.awt.geom.Point2D pnt = null;
		GeoMouseEvent ge = (GeoMouseEvent) e; 
		java.awt.geom.Point2D center=ge.getMapCoordinate(pnt);
		int theToolNum = getToolInEffect();
		
		switch (theToolNum) {
			case 1:  //ZOOMOUT
				setZoomOut(center);
				break;
			case 2:  //GETFEATURE
				getFeature(center);
				break;
			default: //ZOOMIN
           setFlyIn(center);
			  //  setZoomIn(center);		  
		}
	}

//	Unused functions of the MouseListener Interface.	
	public void mouseExited(MouseEvent e) {}		
	public void mouseReleased(MouseEvent e) {}		
	public void mouseEntered(MouseEvent e) {}		
	public void mousePressed(MouseEvent e) {}
	
	class ResetAction extends AbstractAction { 
		public ResetAction () {
			ImageIcon theIcon = new ImageIcon("icons/WebComponent16.gif");
			putValue(NAME, "Full Extent");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Reset the map to its full extent");
			putValue(ACTION_COMMAND_KEY, "RESET");
		}
		public void actionPerformed(ActionEvent e) {
			double border = 0.05;  // leave a 5% border;
			Rectangle2D area = fullArea;
			double width = area.getWidth();
			double height = area.getHeight();
			double newX = area.getX() - border*width;
			double newY = area.getY() - border*height;
			Rectangle2D.Double newArea = 
				new Rectangle2D.Double(
					newX,newY,width+(width*2*border),height+(height*2*border));
			mapPane.setVisibleArea(newArea);		
		}
  	}
  	
	class LeftAction extends AbstractAction { 
		public LeftAction() {
			ImageIcon theIcon = new ImageIcon("icons/Back16.gif");
			putValue(NAME, "Pan Left");
			putValue(SMALL_ICON, theIcon);

			putValue(SHORT_DESCRIPTION, "Move West (Left)");
			putValue(ACTION_COMMAND_KEY, "WEST");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = mapPane.getVisibleArea();
			double newX = area.getX() - area.getWidth();
			Rectangle2D.Double newArea =
                            new Rectangle2D.Double(newX,area.getY(),area.getWidth(),area.getHeight());
			mapPane.setVisibleArea(newArea);		
		}
	}
	class RightAction extends AbstractAction { 
		public RightAction() {
			ImageIcon theIcon = new ImageIcon("icons/Forward16.gif");
			putValue(NAME, "Pan Right");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move East (Right)");
			putValue(ACTION_COMMAND_KEY, "EAST");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = mapPane.getVisibleArea();
			double newX = area.getX() + area.getWidth();
			Rectangle2D.Double newArea =
                            new Rectangle2D.Double(newX,area.getY(),area.getWidth(),area.getHeight());
			mapPane.setVisibleArea(newArea);		
		}
	}	
	class UpAction extends AbstractAction { 
		public UpAction() {
			ImageIcon theIcon = new ImageIcon("icons/Up16.gif");
			putValue(NAME, "Pan Up");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move North (Up)");
			putValue(ACTION_COMMAND_KEY, "NORTH");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = mapPane.getVisibleArea();
			double newY = area.getY() + area.getHeight();
			Rectangle2D.Double newArea = new Rectangle2D.Double(area.getX(),newY,area.getWidth(),area.getHeight());
			mapPane.setVisibleArea(newArea);	
		}
	}
	class DownAction extends AbstractAction { 
		public DownAction() {
			ImageIcon theIcon = new ImageIcon("icons/Down16.gif");
			putValue(NAME, "Pan Down");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Move South (Down)");
			putValue(ACTION_COMMAND_KEY, "SOUTH");
		}
		public void actionPerformed(ActionEvent e) {
			Rectangle2D area = mapPane.getVisibleArea();
			double newY = area.getY() - area.getHeight();
			Rectangle2D.Double newArea =
                             new Rectangle2D.Double(area.getX(),newY,area.getWidth(),area.getHeight());
			mapPane.setVisibleArea(newArea);	
		}
	}
	
	class ZoomInAction extends AbstractAction { 
		public ZoomInAction() {
			ImageIcon theIcon = new ImageIcon("icons/ZoomIn16.gif");
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
			ImageIcon theIcon = new ImageIcon("icons/ZoomOut16.gif");
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
			ImageIcon theIcon = new ImageIcon("icons/About16.gif");
			putValue(NAME, "GetFeature");
			putValue(SMALL_ICON, theIcon);
			putValue(SHORT_DESCRIPTION, "Get A Feature");
			putValue(ACTION_COMMAND_KEY, "GETFEATURE");
		}
		public void actionPerformed(ActionEvent e) {
			setToolInEffect(GETFEATURE);	
		}
	}

	/**
	 * @return
	 */
	public int getToolInEffect() {
		return ToolInEffect;
	}

	/**
	 * @param string
	 */
	public void setToolInEffect(int theToolNum) {
		ToolInEffect = theToolNum;
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
	public DefaultMapLayer makePolyLayer(String inFile, Color hue) {
		//DefaultMapLayer aLayer = null;
		FeatureSource fs = null;
		try {
			//URL rdURL = new File(inFile).toURL();	
         URL rdURL = FrameTools.class.getResource("/shapefiles/states.shp");
		  //Create the ShapeFilDataStore and get freature source;  
			ShapefileDataStore ds = new ShapefileDataStore(rdURL);
         String urlString = rdURL.toString();
			//fs = ds.getFeatureSource(rdURL.getFile().replaceAll(".shp",""));
         fs = ds.getFeatureSource("states");

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

	/**
	* Convience method to create a polygon layer based on a featureSource 
	* and desired fill color. The outline is set to narrow and black.
	* The opacity defaults to 0.7 since I don't know what layers this will
	* have to coexist with.
	* @param	fs A FeatureSource.
	* @param	hue The polygon fill color as type java.awt.Color (for example Color.black).
	*/
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
	
	
	/**
	* Convience method to create a line layer based on shapefile 
	* name and desired color.
	* @param	inPath Path to the shape file.
	* @param	inFile The file name to a shapefile containing lines.
	* @param	hue The color of the lines as type java.awt.Color (for example Color.black).
	*/
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
}


