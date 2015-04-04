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

import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


public class AsciiPointsToPolygon {

   
   
   public static final AttributeType[] ATTRIBUTES = {
         AttributeTypeFactory.newAttributeType("geom", Geometry.class),
         AttributeTypeFactory.newAttributeType("desc", String.class, true, 20)
      };
   
   
   private static void convertFile(String file) throws Exception {

      GeometryFactory geoFactory = new GeometryFactory();
      
      File outfile = new File(file);
      
      System.out.println("WRITING SHAPEFILE: "+file+".shp");
      String desc = "WATCH/WARNING POLYGON";      
      FeatureType schema = FeatureTypeFactory.newFeatureType(ATTRIBUTES, "Attributes");
      StreamingShapefileExport exporter = new StreamingShapefileExport(outfile);
      // Write .shp , .shx and .dbf files
      
      int geoIndex=0;
/*
      Coordinate[] coords = new Coordinate[] {
         new Coordinate(-87.85, 37.12),  
         new Coordinate(-87.81, 37.06),  
         new Coordinate(-87.35, 37.18),  
         new Coordinate(-87.37, 37.5),  
         new Coordinate(-87.85, 37.12)  
      };

      Coordinate[] coords = new Coordinate[] {
         new Coordinate(-87.12, 37.41),  
         new Coordinate(-87.18, 37.41),  
         new Coordinate(-87.21, 37.39),  
         new Coordinate(-87.30, 37.39),  
         new Coordinate(-87.35, 37.32),  
         new Coordinate(-87.48, 37.26),  
         new Coordinate(-87.53, 37.36),  
         new Coordinate(-87.18, 37.58),  
         new Coordinate(-87.16, 37.58),  
         new Coordinate(-87.11, 37.45),  
         new Coordinate(-87.11, 37.43),  
         new Coordinate(-87.12, 37.41)  
      };
*/

      


      BufferedReader br = new BufferedReader(new FileReader(new File(file)));
      String str = null;
      // Read warning name
      if ( (str = br.readLine()) != null) {
         desc = str;
      }
      // Add points
      Vector pointsString = new Vector();
      while ( (str = br.readLine()) != null) {
         if (str.trim().length() != 0) {
            //System.out.println("LOADING: "+str);
            pointsString.addElement(str);
         }
      }

      
      // Convert to Coordinates - +1 allows for closing of polygon
      Coordinate[] coords = new Coordinate[pointsString.size()+1];
      for (int n=0; n<pointsString.size(); n++) {
         String s = pointsString.elementAt(n).toString().trim();  
         String[] latlonString = s.split(" ");
         //System.out.println("SPLIT INTO: [0]="+latlonString[0]+"   [1]="+latlonString[1]);
         double lat = Double.parseDouble(latlonString[0])/100.0;
         double lon = Double.parseDouble(latlonString[1])/-100.0;
         coords[n] = new Coordinate(lon, lat);
      }
      // Close polygon
      coords[pointsString.size()] = coords[0];
      
      LinearRing lr = geoFactory.createLinearRing(coords);
      Polygon poly = geoFactory.createPolygon(lr, null);
      // create the feature and convert values to inch
      Feature feature = schema.create(new Object[]{poly, desc} , new Integer(geoIndex++).toString());
      System.out.println("CREATED: "+feature);
      // add to shapefile
      exporter.addFeature(feature);
      exporter.close();
      
      
      
      
   }
   
   
   
   /**
    *  The main program for the Ascii2Shape class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {

      try {
         
         String outFile = "H:\\Nexrad_Viewer_Test\\Hutchins\\gis\\WFUS53-KPAH-152129.txt";
         convertFile(outFile);
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

