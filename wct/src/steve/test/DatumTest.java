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


import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;

import org.geotools.ct.MathTransform;
import org.geotools.pt.CoordinatePoint;

/**
 * Tests the simple equation against the GeoTools Proj4 conversion -
 * Simple equation seems generally ok. <br><br>
 * Example results:
 * <pre>
 * C:\devel\nexrad\build>java -mx200m -classpath . steve.test.DatumTest
 * OLD   : -105.0 , 35.0
 * NEW GT: -105.0 , 35.181028743424385
 * NEW EQ: -105.0 , 35.18102677809569

 * C:\devel\nexrad\build>java -mx200m -classpath . steve.test.DatumTest
 * OLD   : -95.0 , 25.0
 * NEW GT: -95.0 , 25.147800606082996
 * NEW EQ: -95.0 , 25.147723934295694

 * C:\devel\nexrad\build>java -mx200m -classpath . steve.test.DatumTest
 * OLD   : -120.0 , 40.0
 * NEW GT: -120.00000000000001 , 40.189558914985916
 * NEW EQ: -120.0 , 40.18961056510765
 * </pre>
 */

public class DatumTest {
   
   
   public static void main(String[] args) {
  
      WCTProjections nexradProjection = new WCTProjections();
      
      try {
         
         // Convert from Spherical to WGS84 Datum (for DPA Grid Cells)
         MathTransform sphericalToWGS84Transform = nexradProjection.getSphericalToWGS84Transform(6371007.0);

         double lat = 32.000;
         double lon = -83.600;
         double[] dwork = (sphericalToWGS84Transform.transform(new CoordinatePoint(lon, lat), null)).getCoordinates();
                  

         double newlat = Math.toDegrees(Math.atan(1.00673950 * Math.tan(Math.toRadians(lat))));

         
         System.out.println("OLD   : "+lon+" , "+lat);
         System.out.println("NEW GT: "+dwork[0]+" , "+dwork[1]);         
         System.out.println("NEW EQ: "+lon+" , "+newlat);         
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      try {
         
         // Convert from WGS84 to Spherical Datum (for DPA Grid Cells)
         MathTransform WGS84ToSphericalTransform = nexradProjection.getWGS84ToSphericalTransform(6371007.0);

         double lat = 32.000;
         double lon = -83.600;
         double[] dwork = (WGS84ToSphericalTransform.transform(new CoordinatePoint(lon, lat), null)).getCoordinates();
                  

         double newlat = Math.toDegrees(Math.atan(Math.tan(Math.toRadians(lat))/1.00673950));

         
         System.out.println("OLD   : "+lon+" , "+lat);
         System.out.println("NEW GT: "+dwork[0]+" , "+dwork[1]);         
         System.out.println("NEW EQ: "+lon+" , "+newlat);         
         
       
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
