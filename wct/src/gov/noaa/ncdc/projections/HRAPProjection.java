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
* @author Steve Ansari
*/

package gov.noaa.ncdc.projections;


//************************************************************
public class HRAPProjection
{
  
   private static final double r=6372000;
   private double[] xy = new double[2];
   private double lat0 = Math.toRadians(60.0);
   private double lon0 = Math.toRadians(-105.0);
   private double R, stereoX, stereoY, lonprime;
   
   
   
   /**
    * Forward projection (Lat/Lon to HRAP) <br><br>
    * From Chapter 3 of:
    * <a href="http://www.ce.utexas.edu/prof/maidment/GISHydro/docs/reports/seann/rep95_3.htm">
    * <i> A GIS Procedure for Merging NEXRAD Precipitation Data and
        Digital Elevation Models to Determine Rainfall-Runoff Modeling Parameters </i>,
        by Seann M Reed and David R. Maidment.
    * </a>
    * <br><br>
    */
   public double[] forward(double lat, double lon) {

      lat = Math.toRadians(lat);
      lon = Math.toRadians(lon);
      
      R = r*Math.cos(lat) * ( (1+Math.sin(lat0)) / (1+Math.sin(lat)) );
      stereoX = R * Math.cos(Math.toRadians(Math.toDegrees(lon) + 15));
      stereoY = R * Math.sin(Math.toRadians(Math.toDegrees(lon) + 15));
      
      xy = new double[2];
      xy[0] = (stereoX/4762.5) + 401;
      xy[1] = (stereoY/4762.5) + 1601;
      
      /*
      System.out.println("-- LatLon --> HRAP --");
      System.out.println("   LAT -> Y : "+lat+" -> "+xy[1]); 
      System.out.println("   LON -> X : "+lon+" -> "+xy[0]); 
      */
      
      
      return xy;
        
   }
   
   
   /**
    * Reverse projection (HRAP to Lat/Lon) <br><br>
    * From Chapter 3 of:
    * <a href="http://www.ce.utexas.edu/prof/maidment/GISHydro/docs/reports/seann/rep95_3.htm">
    * <i> A GIS Procedure for Merging NEXRAD Precipitation Data and
        Digital Elevation Models to Determine Rainfall-Runoff Modeling Parameters </i>,
        by Seann M Reed and David R. Maidment.
    * </a>
    * <br><br>
    */
   public double[] reverse(double hrapx, double hrapy) {

      stereoX = 4762.5 * (hrapx - 401);
      stereoY = 4762.5 * (hrapy - 1601);
      
      R = Math.sqrt( (stereoX*stereoX) + (stereoY*stereoY) );
      
      if (stereoY>0) {
         lonprime = 270.0 - Math.toDegrees(lon0) - Math.toDegrees(Math.atan2(stereoY, stereoX));
      }
      else {
         lonprime = -90.0 - Math.toDegrees(lon0) - Math.toDegrees(Math.atan2(stereoY, stereoX));
      }
      
      // longitude
      if (lonprime < 180) {
         xy[0] = lonprime * -1;
      }
      else {
         xy[0] = 360.0 - lonprime; 
      }
      
      // latitude
      xy[1] = 90.0 - 2*( Math.toDegrees(Math.atan( R/(r*(1+Math.sin(lat0))) )) );

      
      /*      
      System.out.println("-- Polar Stereographic HRAP --> Lat/Lon -- ");
      System.out.println("   HRAP -> Y : "+hrapy+" -> "+xy[1]); 
      System.out.println("   HRAP -> X : "+hrapx+" -> "+xy[0]);
      */
 
      return xy;
        
   }

   
   
   
   public static void main(String[] args) {
//      double lat = 30.0; 
//      double lon = -95.0;
      double lat = 23.117; 
      double lon = -119.017;
      
      HRAPProjection proj = new HRAPProjection();
      
      double[] xy = proj.forward(lat, lon);
      
      System.out.println("-- LatLon --> HRAP --");
      System.out.println("   LAT -> Y : "+lat+" -> "+xy[1]); 
      System.out.println("   LON -> X : "+lon+" -> "+xy[0]);
      
      double hrapx = xy[0];
      double hrapy = xy[1];
      
      xy = proj.reverse(hrapx, hrapy);
      
      System.out.println("-- Polar Stereographic HRAP --> Lat/Lon -- ");
      System.out.println("   HRAP -> Y : "+hrapy+" -> "+xy[1]); 
      System.out.println("   HRAP -> X : "+hrapx+" -> "+xy[0]);
      
      hrapx = 1.5;
      hrapy = 1.5;
      xy = proj.reverse(hrapx, hrapy);
      
      System.out.println("-- Polar Stereographic HRAP --> Lat/Lon -- ");
      System.out.println("   HRAP -> Y : "+hrapy+" -> "+xy[1]); 
      System.out.println("   HRAP -> X : "+hrapx+" -> "+xy[0]);
      
      
   }
   
   
}
