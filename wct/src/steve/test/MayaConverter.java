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

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.export.vector.ExportMayaObjLite;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Vector;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.IllegalAttributeException;

public class MayaConverter {
   
   public static DecimalFormat fmt3 = new DecimalFormat("0");

   
   private static Vector list = new Vector();
   


   public static void convertToObjFile() 
      throws IOException, IllegalAttributeException, StreamingProcessException, DecodeException {
      
         String path = "H:\\ESRI\\usa";
         String filename = "states";
      
         File inputFile = new File(path+File.separator+filename+".shp");

         // Load some local data
         URL data_url = inputFile.toURL();
         ShapefileDataStore data_ds = new ShapefileDataStore(data_url);
         FeatureSource data_fs = data_ds.getFeatureSource(filename);
         FeatureReader dataReader = data_fs.getFeatures().reader();
         System.out.println(data_fs.getFeatures().getCount() + " INPUT FEATURES IN "+inputFile.getName());

         // Set up output file
         File outputFile = new File("H:\\Nexrad_Viewer_Test\\Dickson\\Maya\\"+filename+".obj");
         
         
         // Set up converter
         StreamingProcess converter = new ExportMayaObjLite(outputFile, "l");
//         if (dataReader.hasNext()) {            
         while (dataReader.hasNext()) {            
            converter.addFeature(dataReader.next());
         }
         converter.close();
         
         
      
   }

   
   
   
   public static void main(String[] args) {
   
     try {
         
         
         convertToObjFile();


   
      } catch (Exception e) {
         e.printStackTrace();
      }   
   }
}
