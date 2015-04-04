package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingDecoder;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;

import ucar.ma2.Array;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GridDatasetDecoder implements StreamingDecoder {

    private static final Logger logger = Logger.getLogger(GridDatasetDecoder.class.getName());

    private Array dataCache = null;

    private String source;
    private java.awt.geom.Rectangle2D.Double bounds = null;
    private double gridMinValue, gridMaxValue, minValue, maxValue;

    private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();

    private Variable var;
    private int imgWidth; 
    private int imgHeight;
    private Projection proj;

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
    
    private boolean closeGridDatasetAfterDecode = true;
    
    private Date datetime;
    private String longName = "";
    private String units = "";

    private FeatureType schema;

    final static double NO_DATA_VALUE = 99999;


    public final static AttributeType[] POINT_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("value", Float.class, true, 5),
    };

    private Map<String, Object> hintsMap;

    private WCTFilter filter = null;

    private boolean includeDateAttribute = true;
    private boolean includeRuntimeAttribute = true;
    private boolean includeHeightAttribute = true;
    







    public GridDatasetDecoder() throws IllegalAccessException, InstantiationException, FactoryConfigurationError, SchemaException {

        init();
//        schema = FeatureTypeFactory.newFeatureType(POINT_ATTRIBUTES, "Grid Dataset Cell Centroid Point Attributes");

    }
    
    
    private void init() {
        hintsMap = new HashMap<String, Object>();

        hintsMap.put("attributes", POINT_ATTRIBUTES);

        // default WCTFilter for Grid data
        WCTFilter filter = new WCTFilter();
        hintsMap.put("gridFilter", filter);

    }


    /**
     * Set a decodeHint.  To get a list of supported hints and default values,
     * use 'getDecodeHints()'.  The currently supported hints are as follows: <br><br>
     * <ol>
     *  <li> <b>attributes (NOT CURRENTLY IMPLEMENTED)</b>: 
     *          AttributeType[] object that determines which set of attributes to produce.  
     *          Use the static arrays in this class - they are the only ones supported.
     *  <li> <b>gridFilter</b>: 
     *          WCTFilter object that defines filtering options on range, azimuth, 
     *          height and geographic bounds.
     * @param hintsMap
     */
    public void setDecodeHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException {
        if (! hintsMap.keySet().contains(hintKey)) {
            throw new DecodeHintNotSupportedException(this.getClass().toString(), hintKey, hintsMap);
        }

        hintsMap.put(hintKey, hintValue);
    }

    /**
     * Get the key-value pairs for the current decode hints.  
     * If no hints have been set, this will return the supported
     * hints with default values.
     * @return
     */
    public Map<String, Object> getDecodeHints() {
        return hintsMap;
    }








    public FeatureType[] getFeatureTypes() {
        return new FeatureType[] { schema };
    }

    public Rectangle2D.Double getLastDecodedExtent() {
        return bounds;
    }


    public void decodeData(StreamingProcess[] streamingProcessArray) throws DecodeException {


        String lineSectionString = "empty";
        minValue = 999999999;
        maxValue = -999999999;         



        DataDecodeEvent event = new DataDecodeEvent(this);
        try {

            // Start decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeStarted(event);
            }



            StringBuilder errlog = new StringBuilder();
            if (this.source != null) {
                this.gds = GridDatasetUtils.openGridDataset(source, errlog);
            }
            
            if (this.gds == null) { 
                throw new Exception("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
            }
            if (this.gds.getGrids().size() == 0) { 
                throw new Exception("No Grids found in file: "+source);
            }
            gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
            if (gridVariableName != null) {
                gridIndex = gds.getGrids().indexOf( gds.findGridDatatype(gridVariableName) );
            }

            timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
            runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
            zIndex = (getZIndex() < 0) ? 0 : getZIndex();

            gridIndex = (gridIndex > gds.getGrids().size()) ? gds.getGrids().size()-1 : gridIndex;
            if (gridIndex < 0) {
                throw new Exception("Grid name '"+gridVariableName+"' not found in dataset");
            }
            
            this.grid = gds.getGrids().get(gridIndex);
            
            this.filter = (WCTFilter)hintsMap.get("gridFilter");
            if (this.filter == null) {
                filter = new WCTFilter();
            }
            
            // check for spatial subset
            if (filter.getExtentFilter() != null) {
                Rectangle2D.Double r = ((WCTFilter)hintsMap.get("gridFilter")).getExtentFilter();
                LatLonRect bbox = new LatLonRect(new LatLonPointImpl(r.getMinY(), r.getMinX()), new LatLonPointImpl(r.getMaxY(), r.getMaxX()));
                this.grid = grid.makeSubset(null, null, bbox, 1, 1, 1);
                this.bounds = r;
            }
            
            
            this.proj = grid.getProjection();
            this.imgWidth = grid.getXDimension().getLength();
            this.imgHeight = grid.getYDimension().getLength();
            this.coordSys = grid.getCoordinateSystem();
            
            ArrayList<AttributeType> attributeList = new ArrayList(Arrays.asList(POINT_ATTRIBUTES));
            if (coordSys.hasTimeAxis1D()) {
                CoordinateAxis1DTime timeAxis = grid.getCoordinateSystem().getTimeAxis1D();
                this.datetime = timeAxis.getTimeDate(timeIndex);
                if (includeDateAttribute) {
                	attributeList.add(AttributeTypeFactory.newAttributeType("datetime", String.class, true, 21));
                }
            }
            else {
                this.datetime = null;
            }
            
            this.schema = FeatureTypeFactory.newFeatureType(attributeList.toArray(new AttributeType[attributeList.size()]), "Grid Dataset Cell Centroid Point Attributes");
            
            
            
            this.longName = gds.getTitle();

            try {
//                iospClassString = gds.getNetcdfFile().getIosp().getClass().toString();

                System.out.println(grid.getName()+" :: DATE: "+datetime+"\n :: PROJ: "+proj.paramsToString()+
                        "\n :: "+proj.getClassName()+"\n :: BBOX: "+gds.getBoundingBox().toString2());

            } catch (Exception e) {}

            
            
            
            if (this.bounds == null) {
                LatLonRect bbox = gds.getBoundingBox();
                this.bounds = new Rectangle2D.Double(
                        bbox.getLowerLeftPoint().getLongitude(), 
                        bbox.getLowerLeftPoint().getLatitude(), 
                        bbox.getWidth(), 
                        bbox.getHeight()
                    );
            }

            LatLonPointImpl llPoint = new LatLonPointImpl();

            GeometryFactory geoFactory = new GeometryFactory();

            //--------------------------------------------------------------
            // Now read the data and export the centroid of each grid cell
            //--------------------------------------------------------------

            double value;
            int geoIndex = 0;
            
            boolean is2DCoordVar = false;
            if (this.grid.getCoordinateSystem().getXHorizAxis().getRank() == 2 ||
                    this.grid.getCoordinateSystem().getYHorizAxis().getRank() == 2) {

                is2DCoordVar = true;
            }

            for (int y=0; y<imgHeight; y++) {
                for (int x=0; x<imgWidth; x++) {

//                    System.out.println("getting lat/lon for "+x+","+y);

                    // for some reason, we have to flip this for 2D coord variables
                    // don't know why - need to ask Caron
                    if (is2DCoordVar) {
                        llPoint = (LatLonPointImpl) coordSys.getLatLon(y, x);
                    }
                    else {
                        llPoint = (LatLonPointImpl) coordSys.getLatLon(x, y);
                    }
                    
                    

                    // No need to shift, since in NetCDF conventions use center of grid cell for coordinate array

                    if (bounds.contains(llPoint.getLongitude(), llPoint.getLatitude())) {

                        value = getCellValue(x, y);


                        if (Double.isNaN(value)) {
                            continue;
                        }


                        if (value != NO_DATA_VALUE && value < minValue) {
                            minValue = value;
                        }
                        if (value != NO_DATA_VALUE && value > maxValue) {
                            maxValue = value;
                        }


                        
                        
                        
                        if (value != NO_DATA_VALUE && filter.accept(value)) {
                           
                        	Object[] attributes = null;
                        	if (this.datetime != null && includeDateAttribute) {
                        		attributes = new Object[]{
                                        geoFactory.createPoint(new Coordinate(llPoint.getLongitude(), llPoint.getLatitude())),
                                        new Float(value),
                                        WCTUtils.ISO_DATE_FORMATTER.format(datetime)
                                };
                        	}
                        	else {
                        		attributes = new Object[]{
                                        geoFactory.createPoint(new Coordinate(llPoint.getLongitude(), llPoint.getLatitude())),
                                        new Float(value)
                                };
                        	}
                        	 // create the feature
                            Feature feature = schema.create(attributes, new Integer(geoIndex++).toString());
                            for (int n=0; n<streamingProcessArray.length; n++) {
                                streamingProcessArray[n].addFeature(feature);
                            }

                        }
                    }
                } // end x loop
                for (int i = 0; i < listeners.size(); i++) {
                    event.setProgress((int)(100*y/(double)imgHeight));
                    listeners.get(i).decodeProgress(event);
                }
            }   

            for (int n=0; n<streamingProcessArray.length; n++) {
                streamingProcessArray[n].close();
            }


        } catch (Exception e) {
            try {
                for (int n=0; n<streamingProcessArray.length; n++) {
                    streamingProcessArray[n].close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

            e.printStackTrace();
            logger.fine("\n\nSubset String: "+lineSectionString);
            throw new DecodeException(e.toString());
        } finally {

            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeEnded(event);
            }
            
            try {
                if (closeGridDatasetAfterDecode) {
                    gds.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

                    
            
            
            System.out.print("READING GRID INTO CACHE");
            dataCache = subsetGrid.readDataSlice(-1, -1, -1, -1).reduce();
            System.out.println(" ... DONE!");

            System.out.print("SCANNING GRID FOR STATISTICS");
            MinMax minMax = subsetGrid.getMinMaxSkipMissingData(dataCache);
            gridMinValue = minMax.min;
            gridMaxValue = minMax.max;
            System.out.print("  min/max="+minMax.min+"/"+minMax.max);
            System.out.println(" ... DONE!");

        }
        return dataCache.getDouble(dataCache.getIndex().set(y, x));
    }


    
    
    
    
    
    
    /**
     * Overrides 'setSource(GridDataset gds)'.  The GridDataset object is closed after the 'decode' method in this class is called.
     * @param source
     */
    public void setSource(String source) {
        isNewGrid = true;
        dataCache = null;       
        closeGridDatasetAfterDecode = true;
        this.source = source;
    }
    
    /**
     * Overrides 'setSource(String source)'.  The GridDataset object is NOT closed after the 'decode' method in this class is called.
     * @param source
     */
    public void setSource(GridDataset gds) {
        this.source = null;
        isNewGrid = true;
        dataCache = null;       
        closeGridDatasetAfterDecode = false;
        this.gds = gds;
    }
    
    
    
    public java.awt.geom.Rectangle2D.Double getBounds() {
        return bounds;
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
    
    

    
    public void setRuntimeIndex(int runtimeIndex) {
        if (this.runtimeIndex != runtimeIndex && runtimeIndex >= 0) {
            isNewGrid = true;
            dataCache = null;
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
           dataCache = null;
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
           dataCache = null;
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
           dataCache = null;
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
   


    
    public void addDataDecodeListener(DataDecodeListener l) {
        listeners.add(l);
    }
    public void removeDataDecodeListener(DataDecodeListener l) {
        listeners.remove(l);
    }
}
