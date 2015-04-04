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

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.NoninvertibleTransformException;
/**
 *  Geotools Proj4 implementation of Projections and Coordinate Systems.
 * 
 * @author    steve.ansari
 */
public class WCTProjections {

   /**
    * Standard WGS84 Geographic Coordinate System
    */
   //ESRI .prj file: GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]]
   public static final String WGS84_WKT = "GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "+
      "PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
      
   public final static String WGS84_ESRI_PRJ = "GEOGCS[\"GCS_WGS_1984\"," +
       "DATUM[\"D_WGS_1984\"," +
       "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," +
       "PRIMEM[\"Greenwich\",0.0]," +
       "UNIT[\"Degree\",0.0174532925199433]]";

   
   /**
    * Standard NAD83 Geographic Coordinate System - ESRI .prj string
    */
   public static final String NAD83_ESRI_PRJ = "GEOGCS[\"GCS_North_American_1983\"," +
   		"DATUM[\"D_North_American_1983\",SPHEROID[\"GRS_1980\",6378137,298.257222101]]," +
   		"PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.0174532925199433]]";
   /**
    * Standard NAD83 Geographic Coordinate System - NOAA STANDARD
    */
   //ESRI .prj file: GEOGCS["GCS_North_American_1983",DATUM["D_North_American_1983",SPHEROID["GRS_1980",6378137,298.257222101]],PRIMEM["Greenwich",0],UNIT["Degree",0.0174532925199433]] 
   public static final String NAD83_WKT = "GEOGCS[\"NAD83\", DATUM[\"NAD83\", SPHEROID[\"GRS_1980\", 6378137.0, 298.25722210100002],TOWGS84[0,0,0,0,0,0,0]], "+
      "PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
      
   /**
    * Standard NAD27 Geographic Coordinate System
    */
   //ESRI .prj file: GEOGCS["GCS_North_American_1927",DATUM["D_North_American_1927",SPHEROID["Clarke_1866",6378206.4,294.9786982]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]] 
   public static final String NAD27_WKT = "GEOGCS[\"NAD27\", DATUM[\"NAD27\", SPHEROID[\"Clarke_1866\", 6378206.4, 294.9786982],TOWGS84[0,0,0,0,0,0,0]], "+
      "PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.0174532925199433], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";

   
   public final static String HRAP_POLAR_STEREOGRAPHIC_PRJ = "PROJCS[\"User_Defined_Stereographic_North_Pole\","+
   "GEOGCS[\"GCS_User_Defined\",DATUM[\"D_User_Defined\",SPHEROID[\"User_Defined_Spheroid\",6371200.0,0.0]],"+
   "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"+
   "PROJECTION[\"Stereographic_North_Pole\"],PARAMETER[\"False_Easting\",0.0],"+
   "PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",-105.0],"+
   "PARAMETER[\"Standard_Parallel_1\",60.0],UNIT[\"Meter\",1.0]]";

   
   /**
    * HRAP Polar Stereographic Projection with Spherical Earth (6371007 meters).  <br>
    * Parameters: <br><br>
    * latitude_of_origin = 60.0 <br>
    * central_meridian = -105.0 <br>
    */
   //public static final String HRAPSTEREO_WKT = "PROJCS[\"Polar_Stereographic\",GEOGCS[\"Sphere\","+
   public static final String HRAPSTEREO_WKT = "PROJCS[\"Stereographic_North_Pole\",GEOGCS[\"Sphere\","+
         "DATUM[\"Sphere\",SPHEROID[\"Sphere\",6371200.0,0],TOWGS84[0,0,0,0,0,0,0]],"+
         "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"+
         "PROJECTION[\"Polar_Stereographic\"],"+
         //"PROJECTION[\"Stereographic_North_Pole\"],"+
         "PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],"+
         "PARAMETER[\"central_meridian\",-105.0],"+
         "PARAMETER[\"latitude_of_origin\",60.0],UNIT[\"metre\",1.0]]";
         //"PARAMETER[\"latitude_of_origin\",23.117],UNIT[\"metre\",1.0]]";
         
/*
   public static final String HRAPSTEREO_WKT = "PROJCS[\"User_Defined_Stereographic_North_Pole\","+
      "GEOGCS[\"GCS_User_Defined\",DATUM[\"D_User_Defined\",SPHEROID[\"User_Defined_Spheroid\",6371200.0,0.0]],"+
      "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Stereographic_North_Pole\"],"+
      "PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],"+
      "PARAMETER[\"Central_Meridian\",-105.0],"+
      //"PARAMETER[\"Standard_Parallel_1\",60.0],"+
      "PARAMETER[\"latitude_of_origin\",60.0],"+
      "UNIT[\"Meter\",1.0]]";
         */
   /**
    * Albers Equal-Area ConUS Projection with NAD83 Datum.  <br>
    * Parameters: <br><br>
    * standard_parallel_1 = 29.5 <br>
    * standard_parallel_2 = 45.5 <br>
    * latitude_of_origin = 37.5 <br>
    * central_meridian = -96.0 <br>
    */
   public static final String ALBERS_EQUALAREA_CONUS_NAD83_WKT = "PROJCS[\"Albers_Conic_Equal_Area\",GEOGCS[\"NAD83\","+
         "DATUM[\"NAD83\",SPHEROID[\"GRS_1980\",6378137.0,298.25722210100002],TOWGS84[0,0,0,0,0,0,0]],"+
         "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.017453292519943295]],"+
         "PROJECTION[\"Albers_Conic_Equal_Area\"],"+
         "PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],"+
         "PARAMETER[\"central_meridian\",-96.0],"+
         "PARAMETER[\"standard_parallel_1\",29.5],"+
         "PARAMETER[\"standard_parallel_2\",45.5],"+
         "PARAMETER[\"latitude_of_origin\",37.5]," +
         "UNIT[\"metre\",1.0]]";
         
   
   public static final String WEB_MERCATOR_WKT = "PROJCS[\"WGS_1984_Web_Mercator\",GEOGCS[\"GCS_WGS_1984_Major_Auxiliary_Sphere\"," +
   		"DATUM[\"WGS_1984_Major_Auxiliary_Sphere\"," +
//   		"SPHEROID[\"WGS_1984_Major_Auxiliary_Sphere\",6378137.0,0.0]]," +
   		"SPHEROID[\"WGS_1984_Major_Auxiliary_Sphere\",6378137.0,0.0],TOWGS84[0,0,0,0,0,0,0]]," +
   		"PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]," +
   		"PROJECTION[\"Mercator_1SP\"]," +
   		"PARAMETER[\"False_Easting\",0.0]," +
   		"PARAMETER[\"False_Northing\",0.0]," +
   		"PARAMETER[\"Central_Meridian\",0.0]," +
//   		"PARAMETER[\"latitude_of_origin\",0.0]," +
   		"UNIT[\"Meter\",1.0]]";
   
   
   // Factory to create coordinate systems from WKT strings
   private static final CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
    
   // Factory to create transformations from a source and target CS 
   private static final CoordinateTransformationFactory ctFactory = CoordinateTransformationFactory.getDefault();
      
   
   public static MathTransform getMathTransform(String inputCS_WKT, String outputCS_WKT) 
      throws FactoryException, CannotCreateTransformException {
         
      CoordinateSystem inCS = csFactory.createFromWKT(inputCS_WKT);      
      CoordinateSystem outCS = csFactory.createFromWKT(outputCS_WKT);      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      return transformation.getMathTransform();
   }
   
   
   public static CoordinateSystem getCoordinateSystem(String inputWKT) throws FactoryException {
      return csFactory.createFromWKT(inputWKT);
   }
   
   
   /**
    *  Gets a CoordinateSystem given a NexradHeader object that provides lat and lon of the Radar site. <br><br>
    *  The CoordinateSystem is an AlbersEqualArea projection with the secant latitudes at +- 1 deg from 
    *  the Radar site to a WGS84 unprojected (Lon,Lat) Geographic Coordinate System.
    *
    * @param     lon WGS84 lon for radar site.
    * @param     lat WGS84 lat for radar site.
    * @return    The MathTransform object
    */
    
   public CoordinateSystem getRadarCoordinateSystem(NexradHeader header) 
      throws FactoryException, CannotCreateTransformException {
         
      return getRadarCoordinateSystem(header.getLon(), header.getLat());
   }
   
   /**
    *  Gets a CoordinateSystem given a NexradHeader object that provides lat and lon of the Radar site. <br><br>
    *  The CoordinateSystem is an AlbersEqualArea projection with the secant latitudes at +- 1 deg from 
    *  the Radar site to a WGS84 unprojected (Lon,Lat) Geographic Coordinate System.
    *
    * @param   header  NexradHeader object that provides central lon and lat coordinates for radar site.
    * @return    The MathTransform object
    */
   public CoordinateSystem getRadarCoordinateSystem(double lon, double lat) 
      throws FactoryException, CannotCreateTransformException {
         
//       System.out.println("GETTING RADAR PROJ FROM: ( "+lon+" , "+lat+" )");
       
      // Geotools Proj4 implementation of Projections and Coordinate Systems
      // ESRI .prj file: PROJCS["USA_Contiguous_Albers_Equal_Area_Conic",GEOGCS["GCS_North_American_1983",DATUM["D_North_American_1983",SPHEROID["GRS_1980",6378137.0,298.257222101]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Albers"],PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",-96.0],PARAMETER["Standard_Parallel_1",29.5],PARAMETER["Standard_Parallel_2",45.5],PARAMETER["Latitude_Of_Origin",37.5],UNIT["Meter",1.0]]
       
       String wsrWKT = "PROJCS[\"Albers_Conic_Equal_Area\",GEOGCS[\"NAD83\","+
       "DATUM[\"NAD83\",SPHEROID[\"GRS_1980\",6378137.0,298.25722210100002],TOWGS84[0,0,0,0,0,0,0]],"+
       "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.017453292519943295]],"+
       "PROJECTION[\"Albers_Conic_Equal_Area\"],"+
       "PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],"+
       "PARAMETER[\"central_meridian\","+lon+"],"+
       "PARAMETER[\"standard_parallel_1\","+(lat-1.0)+"],"+
       "PARAMETER[\"standard_parallel_2\","+(lat+1.0)+"],"+
       "PARAMETER[\"latitude_of_origin\","+lat+"],UNIT[\"metre\",1.0]]";
       
//      String wsrWKT = "PROJCS[\"Albers_Conic_Equal_Area\",GEOGCS[\"WGS84\","+
//         "DATUM[\"WGS84\",SPHEROID[\"WGS84\",6378137.0,298.257223563]],"+
//         "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"+
//         "PROJECTION[\"Albers_Conic_Equal_Area\"],"+
//         "PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],"+
//         "PARAMETER[\"central_meridian\","+lon+"],"+
//         "PARAMETER[\"standard_parallel_1\","+(lat-1.0)+"],"+
//         "PARAMETER[\"standard_parallel_2\","+(lat+1.0)+"],"+
//         "PARAMETER[\"latitude_of_origin\","+lat+"],UNIT[\"metre\",1.0]]";
      /*   
      wsrWKT = "PROJCS[\"Albers_Conic_Equal_Area\",GEOGCS[\"Sphere\","+
         "DATUM[\"Sphere\",SPHEROID[\"Sphere\",6371007.0,0],TOWGS84[0,0,0,0,0,0,0]],"+
         "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"+
         "PROJECTION[\"Albers_Conic_Equal_Area\"],"+
         "PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],"+
         "PARAMETER[\"central_meridian\","+header.getLon()+"],"+
         "PARAMETER[\"standard_parallel_1\","+(header.getLat()-1.0)+"],"+
         "PARAMETER[\"standard_parallel_2\","+(header.getLat()+1.0)+"],"+
         "PARAMETER[\"latitude_of_origin\","+header.getLat()+"],UNIT[\"metre\",1.0]]";
      */ 
   
      return csFactory.createFromWKT(wsrWKT);
   }
   
   
   
   /**
    *  Gets a MathTransform given a NexradHeader object that provides lat and lon of the Radar site. <br><br>
    *  MathTransform designates a the transform from a AlbersEqualArea projection with the secant latitudes at +- 1 deg from 
    *  the Radar site to a WGS84 unprojected (Lon,Lat) Geographic Coordinate System.
    *
    * @param   header  NexradHeader object that provides central lon and lat coordinates for radar site.
    * @return    The MathTransform object
    */
   public MathTransform getRadarTransform(NexradHeader header) 
      throws FactoryException, CannotCreateTransformException {

      return getRadarTransform(header.getLon(), header.getLat());
   }
   
   
   /**
    *  Gets a MathTransform given the WGS84 lat and lon of the Radar site. <br><br>
    *  MathTransform designates a the transform from a AlbersEqualArea projection with the secant latitudes at +- 1 deg from 
    *  the Radar site to a WGS84 unprojected (Lon,Lat) Geographic Coordinate System.
    *
    * @param   header  NexradHeader object that provides central lon and lat coordinates for radar site.
    * @return    The MathTransform object
    */
   public MathTransform getRadarTransform(double lon, double lat) 
      throws FactoryException, CannotCreateTransformException {
      
         
      CoordinateSystem inCS = getRadarCoordinateSystem(lon, lat);      
      CoordinateSystem outCS = csFactory.createFromWKT(WGS84_WKT);      
      
      // Create transformation to convert from WSR Projection to WGS84      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      
      return transformation.getMathTransform();
      
   }

   
   
   /**
    *  MathTransform designates a the transform from a Polar Stereographic sphereical projection 
    *  to a WGS84 unprojected (Lon,Lat) Geographic Coordinate System.  Used to convert 
    *  HRAP / Stereographic Grid Cells to WGS84 polygons.
    *  
    * @return    The MathTransform object
    */
   public MathTransform getStereoToWGS84Transform() 
      throws FactoryException, CannotCreateTransformException {
      
      CoordinateSystem inCS = csFactory.createFromWKT(HRAPSTEREO_WKT);      
      CoordinateSystem outCS = csFactory.createFromWKT(WGS84_WKT);      
      
      // Create transformation to convert from WSR Projection to WGS84      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      
      return transformation.getMathTransform();
      
   }
   
   /**
    *  MathTransform designates a the transform from a NAD83 unprojected (Lon,Lat) 
    *  Geographic Coordinate System to a Polar Stereographic spherical projection. 
    *  Used to convert Radar Coordinates (NAD83) to Stereographic coordinates.
    *  
    * @return    The MathTransform object
    */
   public MathTransform getNAD83ToStereoTransform() 
      throws FactoryException, CannotCreateTransformException {
               
      CoordinateSystem inCS = csFactory.createFromWKT(NAD83_WKT);      
      CoordinateSystem outCS = csFactory.createFromWKT(HRAPSTEREO_WKT);      
      
      // Create transformation to convert from WSR Projection to WGS84      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      
      return transformation.getMathTransform();
      
   }
   
   /**
    *  MathTransform designates a the transform from a Spherical Earth unprojected (Lon,Lat) 
    *  Geographic Coordinate System to a WGS84 Datum. 
    *  Used to convert HRAP Spherical Earth Coordinates to WGS84.
    *  
    * @param  r  Radius of Earth in meters
    *
    * @return    The MathTransform object
    */
   public MathTransform getSphericalToWGS84Transform(double r) 
      throws FactoryException, CannotCreateTransformException {
               
      String sphericalWKT = "GEOGCS[\"Sphere\", DATUM[\"Sphere\", SPHEROID[\"Sphere\", "+r+", 0],TOWGS84[0,0,0,0,0,0,0]], "+
      "PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
      
      CoordinateSystem inCS = csFactory.createFromWKT(sphericalWKT);      
      CoordinateSystem outCS = csFactory.createFromWKT(WGS84_WKT);      
      
      // Create transformation to convert from WSR Projection to WGS84      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      
      return transformation.getMathTransform();
      
   }
   
   /**
    *  MathTransform designates a the transform from a WGS84 unprojected datm (Lon, Lat)
    *  to a Spherical Earth unprojected (Lon,Lat). 
    *  
    * @param  r  Radius of Earth in meters
    *
    * @return    The MathTransform object
    */
   public MathTransform getWGS84ToSphericalTransform(double r) 
      throws FactoryException, CannotCreateTransformException, NoninvertibleTransformException {

      return getSphericalToWGS84Transform(r).inverse();
   }
   
   /**
    *  MathTransform designates a the transform from a Spherical Earth unprojected (Lon,Lat) 
    *  Geographic Coordinate System to a NAD83 Datum. 
    *  Used to convert HRAP Spherical Earth Coordinates to WGS84.
    *  
    * @param  r  Radius of Earth in meters
    *
    * @return    The MathTransform object
    */
   public MathTransform getSphericalToNAD83Transform(double r) 
      throws FactoryException, CannotCreateTransformException {
               
      String sphericalWKT = "GEOGCS[\"Sphere\", DATUM[\"Sphere\", SPHEROID[\"Sphere\", "+r+", 0],TOWGS84[0,0,0,0,0,0,0]], "+
      "PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
      
      CoordinateSystem inCS = csFactory.createFromWKT(sphericalWKT);      
      CoordinateSystem outCS = csFactory.createFromWKT(NAD83_WKT);      
      
      // Create transformation to convert from WSR Projection to WGS84      
      CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
      
      return transformation.getMathTransform();
      
   }
   
   /**
    *  MathTransform designates a the transform from a NAD83 unprojected datm (Lon, Lat)
    *  to a Spherical Earth unprojected (Lon,Lat). 
    *  
    * @param  r  Radius of Earth in meters
    *
    * @return    The MathTransform object
    */
   public MathTransform getNAD83ToSphericalTransform(double r) 
      throws FactoryException, CannotCreateTransformException, NoninvertibleTransformException {

      return getSphericalToNAD83Transform(r).inverse();
   }
   
}

