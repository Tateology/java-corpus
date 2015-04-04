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

// JAI IMPORTS
import gov.noaa.ncdc.wct.export.raster.WCTNativeRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.opengis.referencing.FactoryException;


public class XMRGRaster implements WCTRaster, WCTNativeRaster {

   //public static final int WIDTH = 131;
   //public static final int HEIGHT = 131;
   private int xmrgWidth, xmrgHeight;
   public static final double NO_DATA = -999.0;
   public static final double CELLSIZE = 4762.5;
   
   private WritableRaster raster = null;
   private java.awt.geom.Rectangle2D.Double bounds = null; 

   private boolean emptyGrid = false;
   // hold metadata for grid
   private long dateInMillis = -999;
   private String longName = "";
   private String units = "";
   private String standardName = "";
   private String variableName = "";

   
   public XMRGRaster() {

//      raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, WIDTH, HEIGHT, 1, null);
   }
   
   public void init(DecodeXMRGHeader header) {
      
      longName = NexradUtilities.getLongName(header);
      dateInMillis = header.getMilliseconds();
      units = NexradUtilities.getUnits(header);
               
      xmrgWidth = header.getNumHrapX();
      xmrgHeight = header.getNumHrapY();
      raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, xmrgWidth, xmrgHeight, 1, null);
      emptyGrid = true;
      // initialize raster to NoData value
      for (int i = 0; i < xmrgWidth; i++) {
         for (int j = 0; j < xmrgHeight; j++) {
            raster.setSample(i, j, 0, NO_DATA);
         }
      }
      // get bounds for this grid
      double[] llcorner = getPolarStereoLLCorner(header.getLlHrapX(), header.getLlHrapY());
      bounds = new java.awt.geom.Rectangle2D.Double(llcorner[0], llcorner[1], xmrgWidth*CELLSIZE, xmrgHeight*CELLSIZE);
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
   
   private double[] getPolarStereoLLCorner(int llHrapX, int llHrapY) {
      
        double[] llcorner;

        // Find corners of grid
        llcorner = new double[2];
        llcorner[0] = 4762.5 * (llHrapX - 401); 
        llcorner[1] = 4762.5 * (llHrapY - 1601); 
      
        return llcorner;
      
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
      String str = "ncols " + xmrgWidth + "\r\n" +
         "nrows " + xmrgHeight + "\r\n" +
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
   
   
   public GridCoverage getGridCoverage() throws FactoryException {
            
      Color[] colors = NexradColorFactory.getColors(81, false);
      double[] vals = NexradValueFactory.getProductMaxMinValues(81, 12, false);
      double[] minValues = new double[] { vals[0] - .1 };
      double[] maxValues = new double[] { vals[1] };
      
      //bounds = new java.awt.geom.Rectangle2D.Double(-100.0, 25.0, 10.0, 10.0);
      Envelope envelope = new Envelope(bounds);
      GridCoverage gc = new GridCoverage("My colored coverage", raster,
            WCTProjections.getCoordinateSystem(WCTProjections.HRAPSTEREO_WKT),
            //org.geotools.cs.GeographicCoordinateSystem.WGS84,
            envelope, minValues, maxValues, null, new Color[][]{colors}, null);
System.out.println(gc);    
      return gc;
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

}
