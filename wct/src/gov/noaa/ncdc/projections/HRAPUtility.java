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

//***********************************************************
// HRAPProjection.java     Author: Steve Ansari
/**
 * Converts Geographic Latitude and Longitude to and from
 * Polar Stereographic HRAP Coordinates. <br><br>
 *
 * All transformations assume Spherical Earth Datum.  (This is ok
 * for NEXRAD calculations because NEXRAD Precipitation Processing
 * also uses Spherical Earth model)
 *
 * All input angles must be in decimal degrees.<br>
 * Output Polar Stereographic Coordinates are in HRAP grid cell units.<br>
 * Earth Radius is defined as 6372000 meters.<br>
 *
 * @author    Steve Ansari
 */

package gov.noaa.ncdc.projections;

import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

//************************************************************
/**
 *  Converts shapefiles from WGS84/NAD83 to HRAP or Polar Stereographic coordinates
 *
 * @author    steve.ansari
 */
public class HRAPUtility {
   
   public final static int HRAP = 0;
   public final static int POLAR_STEREO = 1;
   public final static int WGS84 = 2;
   
   public final static int HRAP_X = 1121;
   public final static int HRAP_Y = 881;
   



   // The list of event listeners.
   private Vector listeners = new Vector();


   
   
   /**
    *  Converts shapefile from WGS84/NAD83 coordinates to the selected format
    */
   public void convertShpFile(File inFile, int outFormat) throws Exception {
      convertShpFile(inFile, inFile.getParentFile(), outFormat);
   }
   
   public void convertShpFile(File inFile, File outDir, int outFormat) throws Exception {

      URL shapeURL = inFile.toURI().toURL();
      String filename = inFile.getName();
      // strip extension
      filename = filename.substring(0, filename.length() - 4);

      // get input features
      ShapefileDataStore data_ds = new ShapefileDataStore(shapeURL);
      FeatureSource data_fs = data_ds.getFeatureSource(filename);
      FeatureReader dataReader = data_fs.getFeatures().reader();
      System.out.println(data_fs.getFeatures().getCount() + " INPUT FEATURES IN " + inFile.getName());

      // initiate the HRAP projection
      HRAPProjection hrapProj = new HRAPProjection();

      String ext;
      if (outFormat == HRAP) {
         ext = "_HRAP.shp";
      }
      else if (outFormat == POLAR_STEREO) {
         ext = "_STER.shp";
      }
      else {
         throw new Exception("INCORRECT OUTPUT FORMAT SPECIFIED");  
      }
      
      File outFile = new File(outDir + File.separator + filename + ext);
      StreamingShapefileExport shpExport = new StreamingShapefileExport(outFile, WCTProjections.HRAP_POLAR_STEREOGRAPHIC_PRJ);

      // then create the destination data store and write them all to
      // to the disk
      ShapefileDataStore dest = new ShapefileDataStore(inFile.toURI().toURL());

      while (dataReader.hasNext()) {
         Feature dataFeature = dataReader.next();
         Geometry dataGeometry = dataFeature.getDefaultGeometry();
         Coordinate[] coords = dataGeometry.getCoordinates();
         for (int n = 0; n < coords.length; n++) {
            double[] hrapxy = hrapProj.forward(coords[n].y, coords[n].x);
            // convert from HRAP to stereographic
            if (outFormat == HRAP) {
               coords[n].x = hrapxy[0];
               coords[n].y = hrapxy[1];
            }
            else if (outFormat == POLAR_STEREO) {
               coords[n].x = 4762.5*(hrapxy[0]-401);
               coords[n].y = 4762.5*(hrapxy[1]-1601);
            }
         }
         // write new feature
         shpExport.addFeature(dataFeature);
      }

      // close exporter
      shpExport.close();
      // close input datastore reader
      dataReader.close();


   }
   
   
   
   
   
   
   
   
   
   public void generateHRAPGrid(File outFile, int projType) 
      throws SchemaException, IllegalAttributeException, StreamingProcessException, IOException, Exception {
      

         AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
         AttributeType hrapX = AttributeTypeFactory.newAttributeType("hrapX", Integer.class, true, 4);
         AttributeType hrapY = AttributeTypeFactory.newAttributeType("hrapY", Integer.class, true, 4);
         AttributeType[] attTypes = { geom, hrapX, hrapY };
         FeatureType schema = FeatureTypeFactory.newFeatureType(attTypes, "HRAP Grid Attributes");


         StreamingShapefileExport streamingProcess;

         if (projType == HRAP || projType == POLAR_STEREO) {
             streamingProcess = new StreamingShapefileExport(outFile, WCTProjections.HRAP_POLAR_STEREOGRAPHIC_PRJ);
         }
         else if (projType == WGS84) {
             streamingProcess = new StreamingShapefileExport(outFile, WCTProjections.WGS84_ESRI_PRJ);
         }
         //else if (projType == NAD83_ESRI_PRJ) {
         //   liteProcess = new ExportShapefileLite(outFile, schema, ExportShapefileLite.NAD83);
         //}
         else {
            throw new Exception("INCORRECT OUTPUT FORMAT SPECIFIED");  
         }
         
         GeometryFactory geoFactory = new GeometryFactory();
         
         // initiate the HRAP projection
         HRAPProjection hrapProj = new HRAPProjection();

         Coordinate[] coords = new Coordinate[5];
   
         
         
            
         GeneralProgressEvent event = new GeneralProgressEvent(this);
   
         // Start
         // --------------
         for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).started(event);
         }
   
         
         
         int geoIndex=0;
         for (int j=0; j<HRAP_Y; j++) {
            
            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
               event.setProgress( (int)( ( ((double)j) / HRAP_Y ) * 100.0) );
               ((GeneralProgressListener) listeners.get(n)).progress(event);
            }
               
            for (int i=0; i<HRAP_X; i++) {

      
               if (projType == HRAP) {
                  // Add ll corner
                  coords[0] = new Coordinate(i+1, j+1);
                  // Add ul corner
                  coords[1] = new Coordinate(i+1, j+2);
                  // Add ur corner
                  coords[2] = new Coordinate(i+2, j+2);
                  // Add lr corner
                  coords[3] = new Coordinate(i+2, j+1);
                  // Add ll corner again to close ring
                  coords[4] = new Coordinate(i+1, j+1);
               }
               else if (projType == POLAR_STEREO) {                                    
                  coords[0] = new Coordinate(4762.5*((i+1)-401), 4762.5*((j+1)-1601));
                  coords[1] = new Coordinate(4762.5*((i+1)-401), 4762.5*((j+2)-1601));
                  coords[2] = new Coordinate(4762.5*((i+2)-401), 4762.5*((j+2)-1601));
                  coords[3] = new Coordinate(4762.5*((i+2)-401), 4762.5*((j+1)-1601));
                  coords[4] = new Coordinate(4762.5*((i+1)-401), 4762.5*((j+1)-1601));
               }
               else if (projType == WGS84) {
                  double[] wgs84LL = hrapProj.reverse(i+1, j+1);
                  double[] wgs84UL = hrapProj.reverse(i+1, j+2);
                  double[] wgs84UR = hrapProj.reverse(i+2, j+2);
                  double[] wgs84LR = hrapProj.reverse(i+2, j+1);
                  coords[0] = new Coordinate(wgs84LL[0], wgs84LL[1]);
                  coords[1] = new Coordinate(wgs84UL[0], wgs84UL[1]);
                  coords[2] = new Coordinate(wgs84UR[0], wgs84UR[1]);
                  coords[3] = new Coordinate(wgs84LR[0], wgs84LR[1]);
                  coords[4] = new Coordinate(wgs84LL[0], wgs84LL[1]);

               }
               // create linear ring and polygon geometries               
               LinearRing lr = geoFactory.createLinearRing(coords);
               Polygon poly = geoFactory.createPolygon(lr, null);
               
               // create the feature
               Feature feature = schema.create(
                     new Object[]{
                     (Geometry) poly,
                     new Integer(i+1),
                     new Integer(j+1)
                     }, new Integer(geoIndex++).toString());
                     
               // add to lite process
               streamingProcess.addFeature(feature);
            }
         }


         // End
         // --------------
         for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).ended(event);
         }
   

         
         streamingProcess.close();
            
            
      
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


   
   
   
   
   



   /**
    *  The main program for the HRAPUtility class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {
      try {
         
         HRAPUtility util = new HRAPUtility();
         //util.convertShpFile(new File("H:\\ESRI\\usa\\states.shp"), util.HRAP);
         
         //util.generateHRAPGrid(new File("H:\\Nexrad_Viewer_Test\\1.1.0\\HRAP\\hrapGrid-WGS84.shp"), util.WGS84);
         util.generateHRAPGrid(new File("H:\\Nexrad_Viewer_Test\\1.1.0\\HRAP\\hrapGrid-WGS84-DF.shp"), util.WGS84);
         //util.generateHRAPGrid(new File("H:\\Nexrad_Viewer_Test\\1.1.0\\HRAP\\hrapGrid-HRAP.shp"), util.HRAP);
         
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

}

