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

package gov.noaa.ncdc.wct.decoders.nexrad;

//JAI IMPORTS
import gov.noaa.ncdc.projections.HRAPProjection;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.export.raster.WCTNativeRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;

import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class DPARaster implements WCTRaster, WCTNativeRaster {

    public static final int WIDTH = 131;
    public static final int HEIGHT = 131;
    public static final double NO_DATA = -999.0;
    public static final double CELLSIZE = 4762.5;

    private WritableRaster raster = null;
    private java.awt.geom.Rectangle2D.Double bounds = null; 

    private boolean emptyGrid = false;

    private WCTProjections nexradProjection = new WCTProjections();

    // hold metadata for grid
    private long dateInMillis = -999;
    private String longName = "One Hour Precipiation Total";
    private String units = "kg m-2";
    private String standardName = "rainfall_amount";
    private String variableName = "One_Hour_Precipitation_Total";


    public DPARaster() {

        raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, WIDTH, HEIGHT, 1, null);
    }

    public void init(DecodeL3Header header) throws DecodeException {
        try {

            longName = NexradUtilities.getLongName(header);
            dateInMillis = header.getMilliseconds();
            units = NexradUtilities.getUnits(header);



            double wsrLon = header.getLon();
            double wsrLat = header.getLat();
            emptyGrid = true;
            // initialize raster to NoData value
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    raster.setSample(i, j, 0, NO_DATA);
                }
            }
            // get bounds for this grid
            double[] llcorner = getPolarStereoLLCorner(wsrLon, wsrLat);
            bounds = new java.awt.geom.Rectangle2D.Double(llcorner[0], llcorner[1], WIDTH*CELLSIZE, HEIGHT*CELLSIZE);
        } catch (Exception e) {
            throw new DecodeException("DPARaster Init Error", header.getDataURL());
        }
    }

    public void setCellValue(int x, int y, float value) {
        raster.setSample(x, y, 0, value);
        if (value != NO_DATA) {
            emptyGrid = false;
        }
    }

    public double getCellValue(int x, int y) {
        return (raster.getSampleFloat(x, y, 0));      
    }

    private double[] getPolarStereoLLCorner(double wsrLon, double wsrLat) 
    throws FactoryException, TransformException {

        HRAPProjection hrapProj = new HRAPProjection();
        double[] wsrHrapXY = hrapProj.forward(wsrLat, wsrLon);
        int[] llCornerHrapXY = new int[2];
        double[] llCornerStereoXY = new double[2];

        // Find lower left grid cell in WSR LFM 131x131 grid.
        // By converting to integer we are now in the lower left
        // corner of the grid cell.
        llCornerHrapXY[0] = (int)(wsrHrapXY[0] - 65); 
        llCornerHrapXY[1] = (int)(wsrHrapXY[1] - 65); 

        // Convert from HRAP to Polar Stereographic Units (meters)
        llCornerStereoXY[0] = 4762.5 * (llCornerHrapXY[0] - 401);
        llCornerStereoXY[1] = 4762.5 * (llCornerHrapXY[1] - 1601);

        return llCornerStereoXY;      

    }

    public double getCellWidth() {
        return CELLSIZE;
    }
    public double getCellHeight() {
        return CELLSIZE;
    }

    public double getNoDataValue() {
        return NO_DATA;
    }

    public java.awt.geom.Rectangle2D.Double getBounds() {
        return bounds;
    }

    public void setWritableRaster(WritableRaster raster) {
        this.raster = raster;
    }

    public WritableRaster getWritableRaster() {
        return raster;
    }

    public boolean isNative() {
        return true;
    }

    public String getESRIhdr() {
        String str = "ncols " + WIDTH + "\r\n" +
        "nrows " + HEIGHT + "\r\n" +
        "xllcorner " + bounds.getX() + "\r\n" +
        "yllcorner " + bounds.getY() + "\r\n" +
        "cellsize 4762.5\r\n" +
        "nodata_value " + NO_DATA;
        return str;
    }

    public String getESRIprjadf() {
        String str = "PROJECTION STEREOGRAPHIC\r\n" +
        "UNITS meters\r\n" +
        "PARAMETERS 6371200 6371200\r\n" +
        "2\r\n" +
        "-105 00 00.0\r\n" +
        "60 00 00.0\r\n" +
        "NorthPole\r\n" +
        "60 00 00.0\r\n";
        return str;
    }

    public String getProjectionInfo() {
        String str = "Projection:  	Stereographic (Polar view)\r\n" +
        "Units:	meters\r\n" +
        "Longitude of center of projection:	-105 00 00.0\r\n" +
        "Latitude of center of projection:	60 00 00.0\r\n" +
        "Latitude of standard parallel:	60 00 00.0\r\n" +
        "X Shift (optional):	0\r\n" +
        "Y Shift (optional):	0\r\n\r\n" +
        "Custom spheroid:	Semimajor axis: 6371200, Semimajor axis: 6371200\r\n";
        return str;
    }

    public String getESRIprj() {
        String str = "PROJCS[\"User_Defined_Stereographic_North_Pole\","+
            "GEOGCS[\"GCS_User_Defined\",DATUM[\"D_User_Defined\","+
            "SPHEROID[\"User_Defined_Spheroid\",6371200.0,0.0]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"+
            "PROJECTION[\"Stereographic_North_Pole\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],"+
                "PARAMETER[\"Central_Meridian\",-105.0],PARAMETER[\"Standard_Parallel_1\",60.0],UNIT[\"Meter\",1.0]]";
        
        return str;
    }
    
    public boolean isEmptyGrid() {
        return emptyGrid;
    }

    /**
     *  Gets the longName attribute of the NexradRasterizer object
     *
     * @return    The long name for the raster
     */
    public String getLongName() {
        return longName;
    }

    /**
     *  Gets the units attribute of the NexradRasterizer object
     *
     * @return    The long name for the raster
     */
    public String getUnits() {
        return units;
    }

    /**
     *  Gets the longName attribute of the NexradRasterizer object
     *
     * @return    The long name for the raster
     */
    public long getDateInMilliseconds() {
        return dateInMillis;
    }



    /**
     *  Sets the longName attribute of the NexradRasterizer object
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /**
     *  Sets the units attribute of the NexradRasterizer object
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     *  Sets the longName attribute of the NexradRasterizer object
     */
    public void setDateInMilliseconds(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    @Override
    public String getStandardName() {
        return standardName;
    }

    @Override
    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    @Override
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
















}
