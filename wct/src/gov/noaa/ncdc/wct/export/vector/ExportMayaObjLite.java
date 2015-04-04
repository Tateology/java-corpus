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

package gov.noaa.ncdc.wct.export.vector;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 *  Export features to shapefile one feature at a time for low memory consumption.
 *
 * @author    steve.ansari
 */
public class ExportMayaObjLite implements StreamingProcess {

   private File outfile;
   private BufferedWriter bw;
   private String geometryElement;
   private int runningTotal = 0;

   /**
    * Constructor for the ExportMayaObjLite object
    *
    * @param  outfile          The destination file. (will create .obj file)
    * @param  geometryElement  The string that represents the type of shapes (point, line, face, curve, surf).
    *                          Can be "p", "l", "f", "curv", "curv2", "surf".
    * @exception  IOException  Error when writing file?
    */
   public ExportMayaObjLite(File outfile, String geometryElement) throws IOException {
      this.outfile = outfile;
      this.geometryElement = geometryElement;
      init();
   }
   
   

   /**
    *  Sets up the writer and writes the .prj file.
    *
    * @exception  IOException  Error writing?
    */
   private void init() throws IOException {

      // Write .obj file  -- All exported NEXRAD Data is in WGS83 LatLon Projection
      // Check for .txt ending and remove it if necessary
      String fileString = outfile.toString();
      if (fileString.endsWith(".obj")) {
         outfile = new File(fileString.substring(0, fileString.length() - 4));
      }

      // Write .txt WKT file
      bw = new BufferedWriter(new FileWriter(outfile + ".obj"));


   }


   /**
    *  Implementation of LiteProcess.  Writes out the Feature to the already open Maya .obj file.
    *
    * @param  feature                   The feature to write.
    * @exception  StreamingProcessException  Error writing the feature?
    */
   public void addFeature(Feature feature) throws StreamingProcessException {
      try {
         // Extract geometry information from feature
         Geometry geometry = feature.getDefaultGeometry();
         String geomType = geometry.getGeometryType();
         //System.out.println("TYPE: "+geomType); 
         if (geomType.startsWith("Multi")) {
            GeometryCollection gcoll = (GeometryCollection)geometry;
            for (int n=0; n<gcoll.getNumGeometries(); n++) {
               Geometry innerGeometry = gcoll.getGeometryN(n);
               //System.out.println("INNER TYPE: "+innerGeometry.getGeometryType()); 
               writeData(innerGeometry);
            }
         }
         else {
            writeData(geometry);
         }

         
      } catch (IOException ioe) {
         throw new StreamingProcessException("IOException: Feature=" + feature.toString());
      }
   }




   private void writeData(Geometry geometry) throws IOException {
         
         // Extract coordinates (vertices) from geometry
         Coordinate[] coords = geometry.getCoordinates();
         
         int length = coords.length;
         // Limit to 200 records for JavaView
         //if (length > 200) {
         //   length = 200;
         //}
         
         // Loop through the coordinates
         for (int n=0; n<length; n++) {
//         for (int n=0; n<coords.length; n++) {
            bw.write("# "+(runningTotal+n+1));
            bw.newLine();
            bw.write("v "+coords[n].x+" "+coords[n].y+" 0.0");
            bw.newLine();
         }
         // Define output geometry
         //bw.write("f");
         bw.write(geometryElement);
         for (int n=0; n<length; n++) {
//         for (int n=0; n<coords.length; n++) {
            bw.write(" "+(runningTotal+n+1));
         }
         bw.newLine();
         
//         runningTotal+=coords.length;
         runningTotal+=length;
         
   }

   

   /**
    * Close the .ohj file - THIS MUST BE DONE AFTER ALL FEATURES ARE PROCESSED!
    *
    * @exception  StreamingProcessException  Error closing the writer?
    */
   public void close() throws StreamingProcessException {
      try {
         bw.flush();
         bw.close();
      } catch (IOException ioe) {
         throw new StreamingProcessException("IOException: ERROR CLOSING FILE: " + outfile);
      }
   }


   /**
    * Just in case we forgot to close...
    */
   public void finalize() {
      try {
         close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}


