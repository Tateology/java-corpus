package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.raster.WCTNativeRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.media.jai.RasterFactory;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;

/**
 * Currently only used in WCTExport.  Handles simple lat/lon NetCDF dataset.
 * @author Steve.Ansari
 *
 */
public class GridDatasetNativeRaster implements WCTRaster, WCTNativeRaster {

    private static final Logger logger = Logger.getLogger(GridDatasetRemappedRaster.class.getName());


    public static final int ROW_CACHE_SIZE = 500;

    public static final int STARTING_MIN_VALUE = 999999999;
    public static final int STARTING_MAX_VALUE = -999999999;


    private static final long NATIVE_GRID_CACHE_SIZE_LIMIT = 2000*2000;

    private int numFileReads = 0;
    private int numCacheReads = 0;
    private int numRepeatRows = 0;

    private int lastRowRead = -1;
    private double[] rowData = null;

    private java.awt.geom.Rectangle2D.Double bounds;
    private GridCoverage gc;
    private WritableRaster raster;
    private String colorTableAlias = "Default";

    private double cellsize = -999;
    private boolean forceEqualXYCellsize = true;
    private int width = 800;
    private int height = 800;

    private double minValue = STARTING_MIN_VALUE;
    private double maxValue = STARTING_MAX_VALUE; 
    private double gridMinValue = STARTING_MIN_VALUE;
    private double gridMaxValue = STARTING_MAX_VALUE;
    private double displayMinValue = Double.NaN;
    private double displayMaxValue = Double.NaN;
    private Color[] displayColors = null;
    
    
    private int maxXIndex = -1;
    private int minXIndex = 100000000;
    private int maxYIndex = -1;
    private int minYIndex = 100000000;
    

    private Envelope envelope;

    private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();

    private int currentAlpha;

    private HashMap<Integer, double[]> rowDataMap = new HashMap<Integer, double[]>();
    private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();
    private Array dataCache = null;

    private int imgWidth; 
    private int imgHeight;
    private Projection proj;
    private boolean isNative = false;
    private boolean isRegularLatLon = false;
    
    private Date datetime;
    private String longName = " ";
    private String units = " ";
    private String standardName = "";
    private String variableName = "";

    private ucar.nc2.dt.GridDataset gds = null;
    private GridDatatype grid = null;
    private GridCoordSystem coordSys = null;

    private int gridIndex = 0;
    private String gridVariableName = null;
    private int timeIndex = 0;
    private int runtimeIndex = 0;
    private int zIndex = 0;
    
    private String lastDecodedFile = null;
    private boolean isNewGrid = true;
    
    private String iospClassString = "";

    private boolean isAutoMinMax = true;

    private boolean isYAxisFlipped = false;
    private long yAxisSize;

    private DataDecodeEvent event = new DataDecodeEvent(this);

    private double minValueFilter = Double.NEGATIVE_INFINITY;
    private double maxValueFilter = Double.POSITIVE_INFINITY;



    public GridDatasetNativeRaster() throws IllegalAccessException, InstantiationException {
    }


    public void scan(String source) throws Exception {
        process(source, true);
    }
    
    public void process(String source) throws Exception {
        process(source, null);
    }
    public void process(String source, java.awt.geom.Rectangle2D.Double bounds) throws Exception {
        process(source, bounds, false);
    }
    private void process(String source, boolean scanOnly) throws Exception {
        process(source, null, scanOnly);
    }
    public void process(String source, java.awt.geom.Rectangle2D.Double bounds, boolean scanOnly) throws Exception {

        if (lastDecodedFile == null || ! lastDecodedFile.equals(source) || isNewGrid) {
//            calculateStatistics(source);
            isNewGrid = false;
            lastDecodedFile = source;
            dataCache = null;
        }
        
//        this.bounds = bounds;
        String lineSectionString = "empty";
        minValue = 999999999;
        maxValue = -999999999;         
        maxXIndex = -1;
        minXIndex = 100000000;
        maxYIndex = -1;
        minYIndex = 100000000;


        logger.fine("REMAP BOUNDS: "+bounds);




        try {

            // Start decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeStarted(event);
            }



            StringBuilder errlog = new StringBuilder();
            this.gds = GridDatasetUtils.openGridDataset(source, errlog);
            if (this.gds == null) { 
                throw new Exception("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
            }
            if (this.gds.getGrids().size() == 0) { 
                throw new Exception("No Grids found in file.  Check CF-compliance for gridded data");
            }
            gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
            if (gridVariableName != null) {
                gridIndex = gds.getGrids().indexOf( gds.findGridDatatype(gridVariableName) );
            }
            
            timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
            runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
            zIndex = (getZIndex() < 0) ? 0 : getZIndex();

            gridIndex = (gridIndex > gds.getGrids().size()) ? gds.getGrids().size()-1 : gridIndex;

            this.grid = gds.getGrids().get(gridIndex);
            this.proj = grid.getProjection();
            this.imgWidth = grid.getXDimension().getLength();
            this.imgHeight = grid.getYDimension().getLength();
            this.coordSys = grid.getCoordinateSystem();
            if (coordSys.hasTimeAxis1D()) {
                CoordinateAxis1DTime timeAxis = grid.getCoordinateSystem().getTimeAxis1D();
                this.datetime = timeAxis.getTimeDate(timeIndex);
            }
            else {
                datetime = null;
            }
            this.longName = gds.getTitle();

            try {
                iospClassString = gds.getNetcdfFile().getIosp().getClass().toString();

                System.out.println(grid.getName()+" :: DATE: "+datetime+"\n :: PROJ: "+proj.paramsToString()+
                        "\n :: "+proj.getClassName()+"\n :: BBOX: "+gds.getBoundingBox().toString2());

            } catch (Exception e) {}


            if (! (this.coordSys.isLatLon() && this.coordSys.isRegularSpatial())) {
                throw new WCTExportException("Only lat/lon grids are supported for 'native' read.  Please resample the data to a lat/lon grid.");
            }


            
            // subset?
            if (bounds != null) {
                LatLonRect subsetBbox = new LatLonRect(
                		new LatLonPointImpl(bounds.getMinY(), bounds.getMinX()), 
                        new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX())
                	);
                this.grid = grid.makeSubset(null, null, subsetBbox, 1, 1, 1);
                
                
                
                
            }
            
            Array yAxisArray = this.grid.getCoordinateSystem().getYHorizAxis().read("0:1");
            Array xAxisArray = this.grid.getCoordinateSystem().getXHorizAxis().read("0:1");
            double yCellSize = Math.abs(yAxisArray.getDouble(0) - yAxisArray.getDouble(1)); 
            double xCellSize = Math.abs(xAxisArray.getDouble(0) - xAxisArray.getDouble(1)); 

            LatLonRect bbox = this.grid.getCoordinateSystem().getLatLonBoundingBox();
            bounds = new Rectangle2D.Double(
                    bbox.getLowerLeftPoint().getLongitude() - xCellSize/2.0, 
                    bbox.getLowerLeftPoint().getLatitude() - yCellSize/2.0, 
                    bbox.getWidth() + xCellSize, bbox.getHeight() + yCellSize);
            this.bounds = bounds;
            


            this.raster = RasterFactory.createBandedRaster(
                    DataBuffer.TYPE_FLOAT, 
                    this.grid.getDimension(this.grid.getXDimensionIndex()).getLength(), 
                    this.grid.getDimension(this.grid.getYDimensionIndex()).getLength(), 
                    1, null);

            this.proj = grid.getProjection();
            this.imgWidth = grid.getXDimension().getLength();
            this.imgHeight = grid.getYDimension().getLength();
            this.bounds = bounds;
            this.envelope = new Envelope(bounds);
            this.cellsize = this.bounds.getWidth() / this.raster.getWidth();
            this.longName = grid.getDescription();
            this.units = grid.getUnitsString();
            this.variableName = grid.getName();
            this.isNative = true;
            this.isRegularLatLon = true;

            if (! checkGridCacheSizeLimit()) {
                throw new WCTExportException("This lat/lon grid is too large for 'native' read. \n" +
                		"Please subset or resample the data to a smaller grid. \n" +
                		"Native x dimension size: "+imgWidth + "\n" +
                		"Native y dimension size: "+imgHeight + "\n" +
                        "Number of pixels: "+ imgWidth*imgHeight + "\n" +
                		"Max number of pixels allowed: "+ NATIVE_GRID_CACHE_SIZE_LIMIT);
            }

            
            if (scanOnly) {
                this.gds.close();
                return;
            }


            for (int i=0; i<this.raster.getWidth(); i++) {
                for (int j=0; j<this.raster.getHeight(); j++) {
                    this.raster.setSample(i, this.raster.getHeight()-j-1, 0, getCellValue(i, j));
                }
            }

            
//            System.out.println("bbox: "+bbox);
//            System.out.println("x cell size: "+this.bounds.getWidth() / this.raster.getWidth());
//            System.out.println("y cell size: "+this.bounds.getHeight() / this.raster.getHeight());
            
            this.gds.close();
            return;




        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            this.gds.close();

            numFileReads = 0;
            numCacheReads = 0;
            numRepeatRows = 0;

            // End of decode
            rowDataMap.clear();
            rowCacheOrder.clear();

            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeEnded(event);
            }
        }

    }


    /**
     * Returns true if native grid size is under the limit, which allows caching.
     * @return
     */
    private boolean checkGridCacheSizeLimit() {        
        return ! (this.coordSys.getXHorizAxis().getSize() * this.coordSys.getYHorizAxis().getSize() > NATIVE_GRID_CACHE_SIZE_LIMIT);        
    }


    public void calculateStatistics(String source) throws Exception {
        
        this.gridMaxValue = STARTING_MAX_VALUE;
        this.gridMinValue = STARTING_MIN_VALUE;
        
        StringBuilder errlog = new StringBuilder();
        this.gds = GridDatasetUtils.openGridDataset(source, errlog);
        if (this.gds == null) { 
            throw new Exception("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
        }
        if (this.gds.getGrids().size() == 0) { 
            throw new Exception("No Grids found in file: "+source);
        }
        
        gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
        runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
        timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
        zIndex = (getZIndex() < 0) ? 0 : getZIndex();
        
        this.grid = gds.getGrids().get(gridIndex);
    
        calculateStatistics();
    }
    
    
    
    public void calculateStatistics() throws Exception {
    
        
        int ncols = grid.getXDimension().getLength();
        int nrows = grid.getYDimension().getLength();
        
        int cellNum = nrows * ncols;
        int rowsRead = 0;
        int maxCellRead = 4*500000;
        int rowsToRead = (int) maxCellRead/ncols;
        System.out.println("rowsToRead: "+rowsToRead);
        while (rowsRead < nrows) {
            if (rowsRead+rowsToRead > nrows) {
                rowsToRead = nrows-rowsRead;
            }
            
            System.out.println("rowsRead: "+rowsRead);
            GridDatatype subsetGrid = grid.makeSubset(null, null, 
                    new Range(timeIndex, timeIndex), 
                    new Range(zIndex, zIndex), 
                    new Range(rowsRead, rowsRead+rowsToRead-1), 
                    null);
            
            MinMax minMax = subsetGrid.getMinMaxSkipMissingData( subsetGrid.readDataSlice(-1, -1, -1, -1) );
            System.out.println("chunk ("+rowsRead+" to "+(rowsRead+rowsToRead)+") min/max: "+minMax.min + " / " + minMax.max);
            if (gridMaxValue < minMax.max) {
                gridMaxValue = minMax.max;
            }
            if (gridMinValue > minMax.min) {
                gridMinValue = minMax.min;
            }
            
//            double[] data = (double[]) grid.readDataSlice(timeIndex, zIndex, row, -1).get1DJavaArray(Double.class);
            
            rowsRead += rowsToRead; 
        }
//        System.out.println("histo max/min: "+gridMinValue+" / "+gridMaxValue);
        
        this.gds.close();
    }
    
    
    /**
     * Read a row of data from disk.  Cache the row to a HashMap.  If the number
     * of cached rows exceeds the ROW_CACHE_SIZE value, remove the oldest row from
     * cache.
     * @param row
     * @return
     * @throws IOException
     * @throws InvalidRangeException
     */
    private double[] getRowData(int row) throws IOException, InvalidRangeException {

        lastRowRead = row;

        if (! rowDataMap.containsKey(row)) {

            rowData = (double[]) grid.readDataSlice(timeIndex, zIndex, row, -1).get1DJavaArray(Double.class);
            rowDataMap.put(row, rowData);
            rowCacheOrder.add(row);
            
            // clear oldest if needed
            if (rowDataMap.size() > ROW_CACHE_SIZE) {
                logger.fine("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
                rowDataMap.remove(rowCacheOrder.get(0));
                rowCacheOrder.remove(0);
            }

            numFileReads++;
            return rowData;
        }
        else {
            numCacheReads++;
            return rowDataMap.get(new Integer(row));
        }

    }



    
    /**
     * Read a chunk of rows of data from disk.  Cache the rows to a HashMap.  If the number
     * of cached rows exceeds the ROW_CACHE_SIZE value, remove the oldest row from
     * cache.
     * @param row
     * @return
     * @throws Exception 
     */
    private double[] getRowData(int row, int rowChunkSize) throws Exception {

        lastRowRead = row;

        if (! rowDataMap.containsKey(row)) {

            GridDatatype subsetGrid = grid.makeSubset(
                    null, // runtime 
                    null, // e
                    new Range(timeIndex, timeIndex), 
                    new Range(zIndex, zIndex), 
                    new Range(row, row+rowChunkSize-1), 
                    null // x
                );

            
            rowData = (double[]) subsetGrid.readDataSlice(-1, -1, -1, -1).get1DJavaArray(Double.class);
            if (rowData.length != rowChunkSize*grid.getXDimension().getLength()) {
                throw new Exception("Data array size read from disk ( "+rowData.length+" ) doesn't match expected: "+rowChunkSize*grid.getXDimension().getLength());
            }
            
            for (int n=0; n<rowChunkSize; n++) {
                double[] singleRowData = new double[grid.getXDimension().getLength()];
                System.arraycopy(rowData, n*grid.getXDimension().getLength(), singleRowData, 0, grid.getXDimension().getLength());
                rowDataMap.put(row, singleRowData);
                rowCacheOrder.add(row);
            
                //  clear oldest if needed
                if (rowDataMap.size() > ROW_CACHE_SIZE) {
                    logger.fine("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
                    rowDataMap.remove(rowCacheOrder.get(0));
                    rowCacheOrder.remove(0);
                }
            }

            numFileReads++;
            return rowData;
        }
        else {
            numCacheReads++;
            return rowDataMap.get(new Integer(row));
        }

    }



    private double getCellValue(int x, int y) throws Exception {

        if (dataCache == null) {

            Range rtRange = (runtimeIndex >= 0) ? new Range(runtimeIndex, runtimeIndex) : new Range(0, 0);
            Range timeRange = (timeIndex >= 0) ? new Range(timeIndex, timeIndex) : new Range(0, 0);
            Range zRange = (zIndex >= 0) ? new Range(zIndex, zIndex) : new Range(0, 0);
            
            GridDatatype subsetGrid = grid.makeSubset(
                    rtRange, // runtime
                    null, // e 
                    timeRange, 
                    zRange, 
                    null, 
                    null);

//            System.out.println("is lat/lon: "+subsetGrid.getCoordinateSystem().isLatLon());
//            System.out.println("index x: "+subsetGrid.getCoordinateSystem().getProjection().INDEX_X);
//            System.out.println("index y: "+subsetGrid.getCoordinateSystem().getProjection().INDEX_Y);
//            System.out.println("y axis: "+subsetGrid.getCoordinateSystem().getYHorizAxis().toString());
            yAxisSize = subsetGrid.getCoordinateSystem().getYHorizAxis().getSize();
            if (yAxisSize > 1) {
                Array yAxisArray = subsetGrid.getCoordinateSystem().getYHorizAxis().read("0:1");
                //            System.out.println("y[0]: "+yAxisArray.getDouble(0));
                //            System.out.println("y[1]: "+yAxisArray.getDouble(1));
                isYAxisFlipped = yAxisArray.getDouble(0) > yAxisArray.getDouble(1);
            }
                    
            
            
            for (int n = 0; n < listeners.size(); n++) {
                event.setStatus("Reading Data Into Cache...");
                listeners.get(n).decodeProgress(event);
            }
            System.out.print("READING GRID INTO CACHE");
            dataCache = subsetGrid.readDataSlice(-1, -1, -1, -1).reduce();
            System.out.println(" ... DONE!");

            for (int n = 0; n < listeners.size(); n++) {
                event.setStatus("Scanning Grid for Statistics...");
                listeners.get(n).decodeProgress(event);
            }
            System.out.print("SCANNING GRID FOR STATISTICS");
            MinMax minMax = subsetGrid.getMinMaxSkipMissingData(dataCache);
            gridMinValue = minMax.min;
            gridMaxValue = minMax.max;
            System.out.print("  min/max="+minMax.min+"/"+minMax.max);
            System.out.println(" ... DONE!");

            
            for (int n = 0; n < listeners.size(); n++) {
                event.setStatus("Resampling...");
                listeners.get(n).decodeProgress(event);
            }

        }

        double value;
        if (isYAxisFlipped) {
            value = dataCache.getDouble(dataCache.getIndex().set((int)yAxisSize-y-1, x));
        }
        else {
            value = dataCache.getDouble(dataCache.getIndex().set(y, x));
        }
        
        
        if (value < minValueFilter || value > maxValueFilter) {
            return Double.NaN;
        }
        else {
            return value;
        }
        
    }













    public void setColorTableAlias(String colorTableAlias) {
        this.colorTableAlias = colorTableAlias;
    }

    public String getColorTableAlias() {
        return this.colorTableAlias;
    }

    public int getCurrentAlpha() {
        return currentAlpha;
    }


    public GridCoverage getGridCoverage(int alpha) throws Exception {
        this.currentAlpha = alpha;

        
        if (minValue >= maxValue) {
//            throw new Exception("All data is undefined for this geographic extent.");
            maxValue = 0.000001;
            minValue = -0.000001;
        }
        if (gridMinValue >= gridMaxValue) {
//          throw new Exception("All data is undefined for this geographic extent.");
            gridMaxValue = 0.000001;
            gridMinValue = -0.000001;
        }
        if (isAutoMinMax) {
            displayMinValue = gridMinValue;
            displayMaxValue = gridMaxValue;
        }

        
        
//        GoesColorFactory gcf = GoesColorFactory.getInstance();
//        ColorsAndValues colorTable = gcf.getColorsAndValues(this);
//        Color[] colors = colorTable.getColors();
//        Double[] values = colorTable.getValues();
//
//        System.out.println("size of value array: "+values.length+" : values "+values[0]+" to "+values[values.length-1]);

        if (displayColors == null) {
            displayColors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE, Color.BLUE.brighter().brighter(), Color.GREEN, 
                Color.YELLOW, Color.ORANGE, Color.RED };
        }
        Double[] values = new Double[] { displayMinValue, displayMaxValue };
        
        if (alpha != -1) {
            displayColors = WCTUtils.applyAlphaFactor(displayColors, alpha);
        }

        double[] minValues = new double[] { values[0] };
        double[] maxValues = new double[] { values[values.length-1] };

//        if (iospClassString.endsWith("Giniiosp")) {
//            minValues = new double[] { 0 };
//            maxValues = new double[] { 254 };
//            WCTUtils.flipArray(colors);
//        }

//        System.out.println("gc width/height: "+this.raster.getWidth()+"/"+this.raster.getHeight());
//        System.out.println("gc bounds: "+envelope);
//        System.out.println("gc values min: "+minValue+"  max: "+maxValue);
//        System.out.println("gc indices minX: "+minXIndex+"  maxX: "+maxXIndex);
//        System.out.println("gc indices minY: "+minYIndex+"  maxY: "+maxYIndex);

        // override for lat/lon non-projected data
        if (this.coordSys.isLatLon() && checkGridCacheSizeLimit()) {
//            if (this.gc != null && this.gc.getName(null).equals("Native Lat/Lon Grid Image")) {
//                return this.gc;
//            }
            this.gc = new GridCoverage("Native Lat/Lon Grid Image", this.raster, 
                    GeographicCoordinateSystem.WGS84, envelope, minValues, 
                    maxValues, null, new Color[][]{ displayColors }, null);
        }
        else {
            this.gc = new GridCoverage("Remapped Grid Image", this.raster, 
                GeographicCoordinateSystem.WGS84, envelope, minValues, 
                maxValues, null, new Color[][]{ displayColors }, null);

        }
        
        return gc;
    }



    public GridCoverage getGridCoverage() throws Exception {
        return getGridCoverage(255);
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getGridMinValue() {
        return gridMinValue;
    }

    public double getGridMaxValue() {
        return gridMaxValue;
    }
    
    


    public void setDisplayMinValue(double displayMinValue) {
        this.displayMinValue = displayMinValue;
    }


    public double getDisplayMinValue() {
        return displayMinValue;
    }


    public void setDisplayMaxValue(double displayMaxValue) {
        this.displayMaxValue = displayMaxValue;
    }


    public double getDisplayMaxValue() {
        return displayMaxValue;
    }


    /**
     * Returns bounds of last processed data
     */
    public java.awt.geom.Rectangle2D.Double getBounds() {
        return bounds;
    }

    /**
     * Resets the bounds (extent) of the raster, so it is automatically 
     * calculated from the extent of the grid during the next decode.
     */
    public void resetBounds() {
        this.bounds = null;
    }


    public double getCellWidth() {
        return cellsize;
    }
    public double getCellHeight() {
        return cellsize;
    }



    public long getDateInMilliseconds() {
        if (datetime != null) {
            return datetime.getTime();
        } 
        else {
            return Long.MIN_VALUE;
        }
    }

    public String getLongName() {
        return longName;
    }

    public double getNoDataValue() {
        return Float.NaN;
    }

    public String getUnits() {
        return units;
    }

    public WritableRaster getWritableRaster() {
        return raster;
    }

    public boolean isEmptyGrid() {
        return false;
    }

    /**
     * Always returns false if this data has been remapped.
     */
     public boolean isNative() {
         return isNative;
     }
     
     public boolean isRegularLatLon() {
         return isRegularLatLon;
     }

     public void setDateInMilliseconds(long dateInMillis) {
         datetime = new Date(dateInMillis);
     }

     public void setLongName(String longName) {
         this.longName = longName;
     }

     public void setUnits(String units) {
         this.units = units;
     }

     public void setWritableRaster(WritableRaster raster) {
         this.raster = raster;
     }






     public static Dimension getEqualDimensions(java.awt.geom.Rectangle2D.Double bounds, int width, int height) {
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

         logger.info("             Original --- WIDTH: " + width + "   HEIGHT: " + height);
         logger.info("Bounds Ratio Adjusted --- WIDTH: " + ncols + "   HEIGHT: " + nrows);
         logger.info("Cellsize -- X: "+bounds.width/ncols+" Y: "+bounds.height/nrows);

         return new Dimension(ncols, nrows);
     }


     public int getWidth() {
         return width;
     }


     public void setWidth(int width) {
         this.width = width;
     }


     public int getHeight() {
         return height;
     }


     public void setHeight(int height) {
         this.height = height;
     }





     public String toString() {
         return "GridDatasetNativeRaster: \n"+
         "Height="+getHeight()+" Width="+getWidth()+
         "Cell Width="+getCellWidth()+" Cell Height"+getCellHeight()+"\n"+
         "Bounds="+getBounds()+"\n\n";
     }





     /**
      * Adds a DataDecodeListener to the list.
      *
      * @param  listener  The feature to be added to the DataDecodeListener attribute
      */
     public void addDataDecodeListener(DataDecodeListener listener) {

         if (!listeners.contains(listener)) {
             listeners.add(listener);
         }

     }


     /**
      * Removes a DataDecodeListener from the list.
      *
      * @param  listener  DataDecodeListener to remove.
      */
     public void removeDataDecodeListener(DataDecodeListener listener) {

         listeners.remove(listener);

     }

     
     
     public void setRuntimeIndex(int runtimeIndex) {
         if (this.runtimeIndex != runtimeIndex && runtimeIndex >= 0) {
             isNewGrid = true;
             System.out.println("runtimeIndex change ("+this.runtimeIndex+" to "+runtimeIndex+"): SET isNewGrid to true");
         }
         this.runtimeIndex = runtimeIndex;
     }


     public int getRuntimeIndex() {
         return runtimeIndex;
     }

     
    public void setTimeIndex(int timeIndex) {
        if (this.timeIndex != timeIndex && timeIndex >= 0) {
            isNewGrid = true;
            System.out.println("timeIndex change ("+this.timeIndex+" to "+timeIndex+"): SET isNewGrid to true");
        }
        this.timeIndex = timeIndex;
    }


    public int getTimeIndex() {
        return timeIndex;
    }


    public void setZIndex(int zIndex) {
        if (this.zIndex != zIndex && zIndex >= 0) {
            isNewGrid = true;
            System.out.println("zIndex change ("+this.zIndex+" to "+zIndex+"): SET isNewGrid to true");
        }
        this.zIndex = zIndex;
    }


    public int getZIndex() {
        return zIndex;
    }


    /**
     * This overrides 'setGridVariableName' when called.
     * @param gridIndex
     */
    public void setGridIndex(int gridIndex) {
        this.gridVariableName = null;
        if (this.gridIndex != gridIndex && gridIndex >= 0) {
            isNewGrid = true;
            System.out.println("gridIndex change ("+this.gridIndex+" to "+gridIndex+"): SET isNewGrid to true");
        }
        this.gridIndex = gridIndex;
    }

    public int getGridIndex() {
        return gridIndex;
    }

    /**
     * This overrides 'setGridIndex' when called.
     * @param gridVariableName
     */
    public void setGridVariableName(String gridVariableName) {
        this.gridVariableName = gridVariableName;
    }

    public String getGridVariableName() {
        return gridVariableName;
    }


    public ucar.nc2.dt.GridDataset getLastProcessedGridDataset() {
        return gds;
    }
    
    public GridDatatype getLastProcessedGridDatatype() {
        return grid;
    }
    
    public GridCoordSystem getLastProcessedGridCoordSystem() {
        return coordSys;
    }
    
    public Date getLastProcessedDateTime() {
        if (this.timeIndex < 0) {
            return null;
        }
        try {
            return this.coordSys.getTimeAxis1D().getTimeDate(this.timeIndex);
        } catch (Exception e) {
            return null;
        }
    }
    
    public Date getLastProcessedRuntime() {
        if (this.runtimeIndex < 0) {
            return null;
        }
        try {
            return this.coordSys.getRunTimeAxis().getTimeDate(this.runtimeIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public double getLastProcessedHeight() {
        if (this.zIndex < 0) {
            return Double.NaN;
        }
        if (this.coordSys.getVerticalAxis() == null) {
            return Double.NaN;
        }
        return this.coordSys.getVerticalAxis().getCoordValue(this.zIndex);
    }
    

    public void setAutoMinMaxValues(boolean isAutoMinMax) {
        this.isAutoMinMax = isAutoMinMax;        
    }
    public boolean isAutoMinMaxValues() {
        return this.isAutoMinMax;
    }


    public void setDisplayColors(Color[] displayColors) {
        this.displayColors = displayColors;
    }


    public Color[] getDisplayColors() {
        return displayColors;
    }


    @Override
     public String getESRIhdr() {
        String str = "ncols " + getWidth() + "\r\n" +
        "nrows " + getHeight() + "\r\n" +
        "xllcorner " + bounds.getX() + "\r\n" +
        "yllcorner " + bounds.getY() + "\r\n" +
        "cellsize " + bounds.getWidth()/getWidth() +" \r\n" +
        "nodata_value " + getNoDataValue();
        return str;
    }


    @Override
    public String getESRIprj() {
        return WCTProjections.NAD83_ESRI_PRJ;
    }


    @Override
    public String getESRIprjadf() {
        return "Lat/Lon NAD 83";
    }


    @Override
    public String getProjectionInfo() {
        return "Lat/Lon NAD 83";
    }


    @Override
    public String getStandardName() {
        return this.standardName;
    }


    @Override
    public String getVariableName() {
        return this.variableName;
    }


    @Override
    public void setStandardName(String standardName) {
        this.standardName = standardName;        
    }


    @Override
    public void setVariableName(String variable) {
        this.variableName = variable;        
    }


    public void setMinValueFilter(double minValueFilter) {
        this.minValueFilter = minValueFilter;
    }


    public double getMinValueFilter() {
        return minValueFilter;
    }


    public void setMaxValueFilter(double maxValueFilter) {
        this.maxValueFilter = maxValueFilter;
    }


    public double getMaxValueFilter() {
        return maxValueFilter;
    }


}
