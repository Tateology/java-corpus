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

//java dependancies
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ProjTest {
    
    //hardcoded WKT strings
    private static String SOURCE_WKT = "GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
    private static String TARGET_WKT = "PROJCS[\"UTM Zone 14N\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"central_meridian\", -99.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"scale_factor\", 0.9996], PARAMETER[\"false_easting\", 500000.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";
    //private static String TARGET_WKT = "PROJCS[\"Lambert\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Lambert_Conformal_Conic_2SP\"], PARAMETER[\"semi_major\", 6378137.0], PARAMETER[\"semi_minor\", 6356752.314245179], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"standard_parallel_1\", 20.0], PARAMETER[\"standard_parallel_2\", 20.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"false_easting\", 100000.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";

    //private static String WKT_2 = "PROJCS[\"Mercator\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Mercator_2SP\"], PARAMETER[\"semi_major\", 6378137.0], PARAMETER[\"semi_minor\", 6356752.314245179], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"standard_parallel_1\", 20.0], PARAMETER[\"false_easting\", 0.0], PARAMETER[\"false_northing\", 1000000.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";
    //private static String WKT_3 = "GEOGCS[\"Sphere\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6370997.0, 0],TOWGS84[0,0,0,0,0,0,0]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
    //private static String WKT_4 = "PROJCS[\"TransverseMercator\", GEOGCS[\"Sphere\", DATUM[\"Sphere\", SPHEROID[\"Sphere\", 6370997.0, 0],TOWGS84[0,0,0,0,0,0,0]],PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"semi_major\", 6370997], PARAMETER[\"semi_minor\", 6370997], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"scale_factor\", 1.0], PARAMETER[\"false_easting\", 0.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";

    /** Factory to create coordinate systems from WKT strings*/
    private static CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
    
    /** Factory to create transformations from a source and target CS */
    private static CoordinateTransformationFactory ctFactory = CoordinateTransformationFactory.getDefault();
    
    /** Creates a new instance of transformData */
    public static void transformData() {
        try {
           
            //create the CS's and transformation
            CoordinateSystem inCS = csFactory.createFromWKT(WCTProjections.WGS84_WKT);
            CoordinateSystem outCS = csFactory.createFromWKT(WCTProjections.HRAPSTEREO_WKT);
            //System.out.println("source CS: " + inCS.getName().getCode());
            //System.out.println("target CS: " + outCS.getName().getCode());
            System.out.println("source CS: " + inCS.toString());
            System.out.println("target CS: " + outCS.toString());
            CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
            System.out.println("transform: " + transformation.getMathTransform().toString());
            CoordinateFilter transFilter = new TransformationCoordinateFilter(transformation.getMathTransform());
            
            
            double lat = 35.0;
            double lon = -95.0;
            
            GeometryFactory geoFactory = new GeometryFactory();
            Geometry geom = geoFactory.createPoint(new Coordinate(lon, lat));
System.out.println("GEOTOOLS BEFORE: "+geom);            
            geom.apply(transFilter);
System.out.println("GEOTOOLS AFTER: "+geom);            



            gov.noaa.ncdc.projections.LatLon2Stereo cvt = new gov.noaa.ncdc.projections.LatLon2Stereo(lat, lon,60.0,-105.0,1.0);
            double wsrX = cvt.getX(0);
            double wsrY = cvt.getY(0);
System.out.println("OLD BEFORE: "+lat+"  ,  "+lon);            
System.out.println("OLD AFTER: "+wsrX+"  ,  "+wsrY);            

            
            System.out.println("Done");
        } catch (Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
       transformData();
    }
    
}
