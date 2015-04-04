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

import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.File;
import java.net.URL;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;


public class VectorExportAggregation {
   
   public static void main(String[] args) {
      runExample(args);
      System.exit(1);
   }
   
   
   
   public static void runExample(String[] args) {
      
      try {
         
         //--------------------------------------------------------------------
         // Input directory
         //--------------------------------------------------------------------
         File dir = new File("H:\\Nexrad_Viewer_Test\\AMS07\\Newbern\\data\\hail");
         //--------------------------------------------------------------------
         // Output file for shapefile 
         //--------------------------------------------------------------------
         File outfile = new File("H:\\Nexrad_Viewer_Test\\AMS07\\Newbern\\export\\knqa_kpah_klzk-hail-20060402-0403");
         
         
         if (args != null && args.length == 2) {
            dir = new File(args[0]);
            outfile = new File(args[1]);
         }

         
         //--------------------------------------------------------------------
         // Init export shapefile
         //--------------------------------------------------------------------
         StreamingShapefileExport export = null;
         
         
         
         
         //--------------------------------------------------------------------
         // Loop through files
         //--------------------------------------------------------------------
         NexradHeader header = new DecodeL3Header();
         File[] files = dir.listFiles();
         for (int n=0; n<files.length; n++) {
            
            URL url = files[n].toURI().toURL();
            header.decodeHeader(url);
            DecodeL3Alpha alphaDecoder = NexradUtilities.getAlphaDecoder(header);
            
            if (export == null) {
               export = new StreamingShapefileExport(outfile);   
            }

            FeatureCollection features = alphaDecoder.getFeatures();
            FeatureIterator fci = features.features();
            while (fci.hasNext()) {
               export.addFeature(fci.next());               
            }
            
                        
            
         }
        
         
         
         //--------------------------------------------------------------------
         // Close the shapefile export 
         //--------------------------------------------------------------------
         export.close();
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   
   
}

