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

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Ascii2Shape {

   
   
   public static final AttributeType[] RASTER_ATTRIBUTES = {
         AttributeTypeFactory.newAttributeType("geom", Geometry.class),
         AttributeTypeFactory.newAttributeType("value", Long.class, true, 20)
      };
   
   
   private static void convertFile(String file) throws Exception {

      GeometryFactory geoFactory = new GeometryFactory();
      
      File outfile = new File(file);
      
      System.out.println("WRITING SHAPEFILE: "+file+".shp");      
      FeatureType schema = FeatureTypeFactory.newFeatureType(RASTER_ATTRIBUTES, "Raster Ascii Grid Attributes");
      StreamingShapefileExport exporter = new StreamingShapefileExport(outfile);
      // Write .shp , .shx and .dbf files
      
      

      
      BufferedReader br = new BufferedReader(new FileReader(new File(file)));
      
      int ncols = Integer.parseInt(br.readLine().substring(12).trim());
      int nrows = Integer.parseInt(br.readLine().substring(12).trim());
      double xllcorner = Double.parseDouble(br.readLine().substring(12).trim());
      double yllcorner = Double.parseDouble(br.readLine().substring(12).trim());
      double cellsize = Double.parseDouble(br.readLine().substring(12).trim());
      double noData = Double.parseDouble(br.readLine().substring(12).trim());
      
      
      String str;
      int rownum = 0;
      int colnum = 0;
      int geoIndex = 0;
      while ((str = br.readLine()) != null) {
         String[] cols = str.split(" ");
         colnum = cols.length;
         
         for (int n=0; n<cols.length; n++) {
            long value = (long)(Double.parseDouble(cols[n]));
            
            if (value != (long)noData) {
               double x = (n*cellsize)+xllcorner + cellsize/2.0;
               double y = ((nrows-rownum)*cellsize)+yllcorner - cellsize/2.0;
               Point point = geoFactory.createPoint(new Coordinate(x, y));
               Feature feature = schema.create(
                  new Object[] { point, new Float(value) },
                  new Integer(geoIndex++).toString()
                  );
               
               exporter.addFeature(feature);
            }
         }
         rownum++;
         
         if (rownum%100 == 0) {
            System.out.println(((double)rownum*100.0)/nrows + " PERCENT COMPLETE");
         }
      }
      
      exporter.close();
      
      System.out.println("ROWS: "+rownum);
      System.out.println("COLS: "+colnum);
      
      
      
      
   }
   
   
   
   /**
    *  The main program for the Ascii2Shape class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {

      try {
         
         String inFile = "H:\\Presentations\\AMS06_Presentation\\ESRI\\pop\\usap00ag.asc";
         convertFile(inFile);
         
         System.out.println("USAGE:\n\n"+
            "ncols         8688\n"+
            "nrows         1320\n"+
            "xllcorner     -181\n"+
            "yllcorner     18\n"+
            "cellsize      0.0416666666667\n"+
            "NODATA_value  -9999\n\n");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

