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

package gov.noaa.ncdc.wct.export.raster;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

import javax.media.jai.RasterFactory;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 *  Rasterize features onto a WritableRaster object using Java 2D Graphics/BufferedImage.
 *
 * @author     steve.ansari
 * @created    March 20, 2008
 */
public class FeatureRasterizer implements StreamingProcess {




    private int height;
    private int width;
    private int baseHeight;
    private int baseWidth;
    private double noDataValue;
    private WritableRaster raster = null;   
    private BufferedImage bimage = null;
    private Graphics2D graphics = null;




    private java.awt.geom.Rectangle2D.Double rasterBounds;
    private double minAttValue = 999999999;
    private double maxAttValue = -999999999;


    // Declare these as global
    private int[] coordGridX = new int[3500];
    private int[] coordGridY = new int[3500];
    private float value;

    private boolean emptyGrid = false;




    private Geometry extentGeometry;
    private GeometryFactory geoFactory = new GeometryFactory();
    private String attributeName = "value";



    private double cellWidth;
    private double cellHeight;  

    private static enum EqualCellsizeAdjustmentStrategy { ADJUST_GEOGRAPHIC_BOUNDS, ADJUST_RASTER_SIZE }; 
    private boolean isEqualCellsize = true;
    private EqualCellsizeAdjustmentStrategy equalCellsizeAdjustmentStrategy = EqualCellsizeAdjustmentStrategy.ADJUST_RASTER_SIZE;
    
    
    




    // The list of event listeners.
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();







    /**
     *Constructor for the FeatureRasterizer object
     *
     * @exception  FeatureRasterizerException  Description of the Exception
     */
    public FeatureRasterizer() {
        this(800, 800, -999.0f);
    }


    /**
     * Constructor for the FeatureRasterizer object - will use default 800x800 raster
     *
     * @param  noData                         No Data value for raster
     * @exception  FeatureRasterizerException  Description of the Exception
     */
    public FeatureRasterizer(float noData) {
        this(800, 800, noData);
    }


    /**
     * Constructor for the FeatureRasterizer object.  No Data value defaults to -999.0
     *
     * @param  height                         Height of raster (number of grid cells)
     * @param  width                          Width of raster (number of grid cells)
     */
    public FeatureRasterizer(int height, int width) {
        this(height, width, -999.0f);
    }

    /**
     * Constructor for the FeatureRasterizer object
     *
     * @param  height                         Height of raster (number of grid cells)
     * @param  width                          Width of raster (number of grid cells)
     * @param  noData                         No Data value for raster
     */
    public FeatureRasterizer(int height, int width, float noData) {
        this.height = height;
        this.width = width;
        this.noDataValue = noData;

        this.baseHeight = height;
        this.baseWidth = width;
        
//        System.out.println("NEW RASTERIZER: " + height + " / " + width);
        
        initRaster();
    }
    
    private void initRaster() {
        raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                width, height, 1, null);
        bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bimage.setAccelerationPriority(1.0f);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//      System.out.println("IMAGE ACCELERATED? "+bimage.getCapabilities(ge.getDefaultScreenDevice().getDefaultConfiguration()).isAccelerated());
        graphics = bimage.createGraphics();
        graphics.setPaintMode();
        graphics.setComposite(AlphaComposite.Src);
        
        clearRaster();
    }

    /**
     *  Gets the raster attribute of the FeatureRasterizer object
     *  Processes data from the FeatureCollection and approximates onto a Raster Grid.
     *
     * @param  fc                             Feature Collection with features to rasterize.
     * @param  attributeName                  Name of attribute from feature collection to provide as the cell value.
     * @exception  FeatureRasterizerException  An error when rasterizing the data
     */
    public void rasterize(FeatureCollection fc, String attributeName)
    throws FeatureRasterizerException {

        // calculate variable resolution bounds that fit around feature collection

        double edgeBuffer = 0.001;
        double x = fc.getBounds().getMinX() - edgeBuffer;
        double y = fc.getBounds().getMinY() - edgeBuffer;
        double width = fc.getBounds().getWidth() + edgeBuffer * 2;
        double height = fc.getBounds().getHeight() + edgeBuffer * 2;
        java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(x, y, width, height);
        
//        System.out.println("BOUNDS: "+bounds);
//        System.out.println("FCBNDS: "+fc.getBounds());

        
        rasterize(fc, bounds, attributeName);
        
    }

    /**
     *  Gets the raster attribute of the FeatureRasterizer object
     *  Processes data from the FeatureCollection and approximates onto a Raster Grid.
     *
     * @param  fc                             Description of the Parameter
     * @param  bounds                         Description of the Parameter
     * @param  attributeName                  Name of attribute from feature collection to provide as the cell value.
     * @exception  FeatureRasterizerException  An error when rasterizing the data
     */
    public void rasterize(FeatureCollection fc, java.awt.geom.Rectangle2D.Double bounds, String attributeName)
    throws FeatureRasterizerException {

//        System.out.println("     height/width: "+ height + " / " + width);
//        System.out.println("base height/width: "+ baseHeight + " / " + baseWidth);
        
//        height = this.baseHeight;
//        width = this.baseWidth;

        this.attributeName = attributeName;
        
        // initialize raster to NoData value
        clearRaster();

        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }

//        System.out.println("     LOCAL BOUNDS: "+bounds);
        setBounds(bounds);
//        System.out.println("RASTERIZER BOUNDS: "+rasterBounds);

        // TODO - change method calls to account for a switch to control if rasterizer should work if vis bounds > feature bounds


        // All the data should start in the lower left corner.  Don't export what we don't need.
//        double ratio = this.rasterBounds.height / this.rasterBounds.width;
//        int ncols;
//        int nrows;
//        if (ratio < 1) {
//            // wider than tall
//            nrows = (int) (ratio * height);
//            ncols = width;
//        }
//        else {
//            nrows = height;
//            ncols = (int) (height / ratio);
//        }
//
//        System.out.println("1 --- WIDTH: " + ncols + "   HEIGHT: " + nrows);

        FeatureIterator fci = fc.features();
        Feature feature;
        int size = fc.size();
        int cnt = 0;


        while (fci.hasNext()) {

        	if (WCTUtils.getSharedCancelTask().isCancel()) {
        		throw new FeatureRasterizerException("Operation canceled");
        	}

        	
            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)++cnt) / size ) * 100.0) );
                listeners.get(n).progress(event);
            }

            feature = fci.next();

            addFeature(feature);

        }
        close();



        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).ended(event);
        }



    }


    /**
     * Implementation the StreamingProcess interface.  Rasterize a single feature and 
     * update current WriteableRaster using the current settings.
     * 
     * @param  feature     The feature to rasterize and add to current WritableRaster
     */   
    public void addFeature(Feature feature) {


//      System.out.println("rasterizer - processing feature: "+feature);
        try {

            value = Float.parseFloat(feature.getAttribute(attributeName).toString());               

            if (value > maxAttValue) { maxAttValue = value; }
            if (value < minAttValue) { minAttValue = value; }

        } catch (Exception e) {	        
            e.printStackTrace();	        
            System.err.println("THE FEATURE COULD NOT BE RASTERIZED BASED ON THE '"+attributeName+
                    "' CURRENT ATTRIBUTE VALUES: '"+
                    Arrays.toString(feature.getFeatureType().getAttributeTypes())+"'");	        
            return;	        
        }




        int rgbVal = floatBitsToInt(value);

        graphics.setColor(new Color(rgbVal, true));

        // Extract polygon and rasterize!
        Geometry geometry = feature.getDefaultGeometry();
        if (geometry.intersects(extentGeometry)) {

            if (geometry.getClass().equals(MultiPolygon.class)) {
                MultiPolygon mp = (MultiPolygon)geometry;
                for (int n=0; n<mp.getNumGeometries(); n++) {
                    drawGeometry(mp.getGeometryN(n));
                }
            }
            else if (geometry.getClass().equals(MultiLineString.class)) {
                MultiLineString mp = (MultiLineString)geometry;
                for (int n=0; n<mp.getNumGeometries(); n++) {
                    drawGeometry(mp.getGeometryN(n));
                }
            }
            else if (geometry.getClass().equals(MultiPoint.class)) {
                MultiPoint mp = (MultiPoint)geometry;
                for (int n=0; n<mp.getNumGeometries(); n++) {
                    drawGeometry(mp.getGeometryN(n));
                }
            }
            else {
                drawGeometry(geometry);
            }
        }


    }

    /**
     * Implementation the StreamingProcess interface - this copies values from BufferedImage RGB to WritableRaster of floats.
     */
    public void close() {
        
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double val = Float.intBitsToFloat(bimage.getRGB(i, j));
                raster.setSample(i, j, 0, val);
            }
        }
    }
















    private void drawGeometry(Geometry geometry) {

        Coordinate[] coords = geometry.getCoordinates();


        // enlarge if needed
        if (coords.length > coordGridX.length) {
            coordGridX = new int[coords.length];
            coordGridY = new int[coords.length];
        }

        // Clear Array
        for (int i = 0; i < coords.length; i++) {
            coordGridX[i] = -1;
        }
        for (int i = 0; i < coords.length; i++) {
            coordGridY[i] = -1;
        }

        // Go through coordinate array in order received (clockwise)
        for (int n = 0; n < coords.length; n++) {
            coordGridX[n] = (int) (((coords[n].x - rasterBounds.x) / cellWidth));
            coordGridY[n] = bimage.getHeight() - (int) (((coords[n].y - rasterBounds.y) / cellHeight));
//            coordGridY[n] = bimage.getHeight() - coordGridY[n]; 
        }


        if (geometry.getClass().equals(Polygon.class)) {
            graphics.fillPolygon(coordGridX, coordGridY, coords.length);
        }
        else if (geometry.getClass().equals(LinearRing.class)) {
            graphics.drawPolyline(coordGridX, coordGridY, coords.length);
        }
        else if (geometry.getClass().equals(LineString.class)) {
            graphics.drawPolyline(coordGridX, coordGridY, coords.length);
        }
        else if (geometry.getClass().equals(Point.class)) {
            graphics.drawPolyline(coordGridX, coordGridY, coords.length);
        }

    }























    /**
     *  Gets the emptyGrid attribute of the FeatureRasterizer object
     *
     * @return    The emptyGrid value
     */
    public boolean isEmptyGrid() {
        return emptyGrid;
    }


    /**
     *  Gets the writableRaster attribute of the FeatureRasterizer object
     *
     * @return    The writableRaster value
     */
    public WritableRaster getWritableRaster() {
        return raster;
    }


    /**
     *  Sets the writableRaster attribute of the FeatureRasterizer object
     *
     * @param  raster  The new writableRaster value
     */
    public void setWritableRaster(WritableRaster raster) {
        this.raster = raster;
    }


    /**
     *  Gets the bounds attribute of the FeatureRasterizer object
     *
     * @return    The bounds value
     */
    public java.awt.geom.Rectangle2D.Double getBounds() {
        return rasterBounds;
    }

    /**
     *  Sets the bounds for the Rasterizer
     *
     * @return    The bounds value
     */
    public void setBounds(Rectangle2D.Double bounds) {
        this.rasterBounds = new Rectangle2D.Double();
        rasterBounds.setRect(bounds);
        
//
//        cellsizeX = bounds.width / (double) width;
//        cellsizeY = bounds.height / (double) height;
//
//        System.out.println("cellsizeX: " + cellsizeX + "  cellsizeY: " + cellsizeY);
//
//        if (cellsizeX > cellsizeY) {
//            cellsizeY = cellsizeX;
//        }
//        if (cellsizeY > cellsizeX) {
//            cellsizeX = cellsizeY;
//        }
//
//        cellsize = cellsizeY;
        

        if (equalCellsizeAdjustmentStrategy == EqualCellsizeAdjustmentStrategy.ADJUST_GEOGRAPHIC_BOUNDS) {
            this.rasterBounds = adjustGeographicBounds(new Dimension(this.width, this.height), this.rasterBounds);
        }
        else if (equalCellsizeAdjustmentStrategy == EqualCellsizeAdjustmentStrategy.ADJUST_RASTER_SIZE) {
            Dimension rasterizerDim = adjustDimensions(this.rasterBounds, this.width, this.height);
            setSize(rasterizerDim);            
        }
        
        this.cellWidth = this.rasterBounds.getWidth() / (double) width;
        this.cellHeight = this.rasterBounds.getHeight() / (double) height;
        
        
        
        // Clip geometries to the provided bounds      
        // Create extent geometry  
        Envelope env = new Envelope(
                this.rasterBounds.getX(), 
                this.rasterBounds.getX() + this.rasterBounds.getWidth(),
                this.rasterBounds.getY(),
                this.rasterBounds.getY() + this.rasterBounds.getHeight()
        );
        extentGeometry = geoFactory.toGeometry(env);

    }






    /**
     *  Sets the entire raster to NoData
     */
    public void clearRaster() {

//      System.out.println("CLEARING RASTER");      
        minAttValue = 999999999;
        maxAttValue = -999999999;

        // initialize raster to NoData value
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                raster.setSample(i, j, 0, noDataValue);
                bimage.setRGB(i, j, floatBitsToInt((float)noDataValue));
            }
        }
    }









    
    
    public boolean isEqualCellsize() {
        return isEqualCellsize;
    }


    public void setEqualCellsize(boolean isEqualCellsize) {
        this.isEqualCellsize = isEqualCellsize;
    }

    /**
     *  Gets the cell width (cellsize x)
     *
     * @return    The cellsize value
     */
    public double getCellWidth() {
        return cellWidth;
    }

    /**
     *  Gets the cell height (cellsize y)
     *
     * @return    The cellsize value
     */
    public double getCellHeight() {
        return cellHeight;
    }


    public EqualCellsizeAdjustmentStrategy getEqualCellsizeAdjustmentStrategy() {
        return equalCellsizeAdjustmentStrategy;
    }


    public void setEqualCellsizeAdjustmentStrategy(EqualCellsizeAdjustmentStrategy equalCellsizeAdjustmentStrategy) {
        this.equalCellsizeAdjustmentStrategy = equalCellsizeAdjustmentStrategy;
    }



    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    




    /**
     *  Get the current attribute to use as the grid cell values.
     */
    public String getAttName() {
        return attributeName;
    }

    /**
     *  Sets the current attribute to use as the grid cell values.
     */
    public void setAttName(String attName) {
        this.attributeName = attName;
    }












    public double getNoDataValue() {
        return noDataValue;
    }

    /** 
     * Will clear the raster if the value is different than the current value.
     * @param noData
     */
    public void setNoDataValue(double noData) {
        if (noData != noDataValue) {
            clearRaster();
        }
        this.noDataValue = noData;
    }


    public void setSize(Dimension size) {
        boolean resetRaster = false;
        if (this.height != (int)size.getHeight() || this.width != (int)size.getWidth()) {
            resetRaster = true;
        }
        this.height = (int)size.getHeight();
        this.width = (int)size.getWidth();
        
        if (resetRaster) {
            initRaster();
        }
    }

    
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }



    public double getMinAttValue() {
        return minAttValue;
    }

    public double getMaxAttValue() {
        return maxAttValue;
    }


















    /**
     * Adds a GeneralProgressListener to the list.
     *
     * @param  listener  The feature to be added to the GeneralProgressListener attribute
     */
    public void addGeneralProgressListener(GeneralProgressListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }


    /**
     * Removes a GeneralProgressListener from the list.
     *
     * @param  listener   GeneralProgressListener to remove.
     */
    public void removeGeneralProgressListener(GeneralProgressListener listener) {
        listeners.remove(listener);
    }





    private static int floatBitsToInt(float f) {
        ByteBuffer conv = ByteBuffer.allocate(4);
        conv.putFloat(0, f);
        return conv.getInt(0);
    }

    
    /**
     * Get the raster dimensions necessary to have equal x and y cellsizes with the supplied geographic bounds.
     * @param bounds
     * @param width
     * @param height
     * @return
     */
    public static Dimension adjustDimensions(java.awt.geom.Rectangle2D.Double bounds, int width, int height) {
        // All the data should start in the lower left corner.  Don't export what we don't need.
        double ratio = bounds.height / bounds.width;
        int ncols;
        int nrows;
        if (ratio < 1) {
           // wider than tall
           nrows = (int) (ratio * width);
           ncols = width;
        }
        else {
           nrows = height;
           ncols = (int) (ratio * height);
        }

//        System.out.println("             Original --- WIDTH: " + width + "   HEIGHT: " + height);
//        System.out.println("Bounds Ratio Adjusted --- WIDTH: " + ncols + "   HEIGHT: " + nrows);
//        System.out.println("Cellsize -- X: "+bounds.width/ncols+" Y: "+bounds.height/nrows);
        
        return new Dimension(ncols, nrows);
    }

    
    /**
     * Adjust the geographic bounds to have equal x and y cellsizes with the supplied raster dimensions.  
     * The center of the extent will remain unchanged.
     * @param imageDimension
     * @param bounds
     * @return
     */
    public static java.awt.geom.Rectangle2D.Double adjustGeographicBounds(Dimension imageDimension, java.awt.geom.Rectangle2D.Double bounds) {
        
        double imgRatio = (double)imageDimension.getWidth() / (double)imageDimension.getHeight();
        double geoRatio = bounds.getWidth() / bounds.getHeight();

        double dlon = bounds.getWidth();
        double dlat = bounds.getHeight();
        double geoCenterX = bounds.getMinX() + (dlon / 2.0);
        double geoCenterY = bounds.getMinY() + (dlat / 2.0);
        
        System.out.println(geoCenterX+","+geoCenterY);
        
        double x = imgRatio / geoRatio;
        
        if (x > 1.0) {
            dlon = dlon * x;
        }
        else {
            dlat = dlat / x;
        }
        
        return new java.awt.geom.Rectangle2D.Double(geoCenterX - dlon / 2.0, 
                geoCenterY - dlat / 2.0, 
                dlon, dlat);
    }  

    
    

    public String toString() {
        return "FEATURE RASTERIZER: WIDTH="+width+" , HEIGHT="+height+" , NODATA="+noDataValue;
    }















}

