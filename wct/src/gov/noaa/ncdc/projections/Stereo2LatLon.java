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
// LatLon2Albers.java     Author: Steve Ansari
/**
* Converts Geographic Latitude and Longitude to Albers Equal Area Coordinates.
* May be constructed as US, Alaska or Hawaii Albers
* or projection parameters may be specified.<br><br>
*
* Separate classes used for forward and reverse conversions
* to save time wasted from unneeded calculations.<br><br>
*
* Calculation is done when object is constructed.  This saves time wasted from performing 
* the calculation each time the getX() or getY() methods are called.<br><br> 
*
* Equation taken from pg 114 of <br><B><U>
* Map Projections: Theory and Applications</U></B> by <B>Frederick Pearson</B><br><br>
* Code taken from MetApps API
*
* All input angles must be in decimal degrees.<br>
* Output Polar Stereographic Coordinates are in meters.<br>
* Earth Radius is defined as 6371007 meters.<br>
* 
* @author Steve Ansari
*/

package gov.noaa.ncdc.projections;

//************************************************************
public class Stereo2LatLon
{
  
   //private final double R=6371007;
   private final static double R=6371200;
   private double[] lat, lon;
   private static final double TOLERENCE = 1.0e-6;
   //private double latt, lont, scale, sinlatt, coslatt;

   /** 
    * Static conversion method
    * @param x        Input X-Coordinate
    * @param y        Input Y-Coordinate
    * @param latt     Input Latitude of Origin
    * @param lont     Input Longitude of Origin (Meridian)
    * @return 2-Element Array with Longitude ([0]) and Latitude ([1])
    */
   public static double[] convert (double x, double y, double latt, double lont) {

      // We are doing km!
      x /= 1000.0;
      y /= 1000.0;
      
      latt = Math.toRadians(latt);
      lont = Math.toRadians(lont);
      
      double scale=R/1000.0;
      double sinlatt = Math.sin(latt);
      double coslatt = Math.cos(latt);
      
      
      double[] lonlat = new double[2];

      
     
        double phi, lam;
        double toLat, toLon;

        double fromX = x;
        double fromY = y;

        double rho = Math.sqrt( fromX*fromX + fromY*fromY);
        double c = 2.0 * Math.atan2( rho, 2.0*scale);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        if (Math.abs(rho) < TOLERENCE)
            phi = latt;
        else
            phi = Math.asin( cosc * sinlatt + fromY * sinc * coslatt / rho);

        toLat = Math.toDegrees(phi);

        if ((Math.abs(fromX) < TOLERENCE) && (Math.abs(fromY) < TOLERENCE))
            lam = lont;
        else if (Math.abs(coslatt) < TOLERENCE)
            lam = lont + Math.atan2( fromX, ((latt > 0) ? -fromY : fromY) );
        else
            lam = lont + Math.atan2( fromX*sinc, rho*coslatt*cosc - fromY*sinc*sinlatt);

        toLon = Math.toDegrees(lam);

        lonlat[1] = (double)toLat;
        lonlat[0]= (double)toLon;
        return lonlat;

/*
        double p[] = new double[2];
        // p[0] = lon
        // p[1] = lat      

        //double lont = -105.0;
        //double latt = 60.0;
        
        x/=1000.0;
        y/=1000.0;
        
        double R = 6371.2000;
 
        double rad2deg=180.0/Math.acos(-1);
        double rr=Math.sqrt(x*x+y*y);
        p[0]=Math.toDegrees(Math.atan2(y,x))+(lont+90.0);
        p[1]=90.0 - 2.0*Math.toDegrees( (Math.atan(rr/(R*(1.0+Math.sin(Math.toRadians(latt))) )) ) );

        return p;
*/
      /*
      double[] lonlat = new double[2];
      double rad2deg=180.0/Math.acos(-1);
      double rr=Math.sqrt(x*x+y*y);
      lonlat[0]=Math.toDegrees(Math.atan2(y,x))+(lont+90.0);
//      lonlat[0]=Math.toDegrees(Math.atan2(y,x))-15.0;
      lonlat[1]=90.0 - 2.0*(Math.atan(rr/(R*(1.0+Math.sin(latt/rad2deg))))*rad2deg);
      return lonlat;
      */
   }
   
   //--------------------------------------------------------------------------------
   /**
   * Convert a single lat and lon 
   * @param dlat Input Latitude
   * @param dlon Input Longitude
   * @param latt Input Tangent Latitude of Origin
   * @param lont Input Tangent Longitude of Origin
   * @param scale Scale Factor (=1 for meters)
   */
   //------------------------------------------------------------------------------    
   public Stereo2LatLon (double x, double y, double latt, double lont)
   {

        lat = new double[1];
        lon = new double[1];

   //     x/=1000; y/=1000;

        double rad2deg=180.0/Math.acos(-1);
        double rr=Math.sqrt(x*x+y*y);
        lon[0]=Math.toDegrees(Math.atan2(y,x))+(lont+90.0);
        lat[0]=90.0 - 2.0*(Math.atan(rr/(R*(1.0+Math.sin(latt/rad2deg))))*rad2deg);

/*
        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+lat[0]+" -> "+y); 
        System.out.println("   LON -> X : "+lon[0]+" -> "+x); 
*/
   }
   //--------------------------------------------------------------------------------
   /**
   * Convert an array of points
   * @param dlat Input Latitude
   * @param dlon Input Longitude
   * @param latt Input Tangent Latitude of Origin
   * @param lont Input Tangent Longitude of Origin
   * @param scale Scale Factor (=1 for meters)
   */
   //------------------------------------------------------------------------------    
   public Stereo2LatLon (double[] x, double[] y, double latt, double lont)
   {
        int cnt = x.length; 
        lat = new double[cnt];
        lon = new double[cnt];

        for (int i=0; i<cnt; i++) {

           double rad2deg=180.0/Math.acos(-1);
           double rr=Math.sqrt(x[i]*x[i]+y[i]*y[i]);
           lon[i]=Math.toDegrees(Math.atan2(y[i],x[i]))+(lont+90.0);
           lat[i]=90.0 - 2.0*(Math.atan(rr/(R*(1.0+Math.sin(latt/rad2deg))))*rad2deg);

        }
   }

   //---------------------------------------------------------------------------------
   /**
   * Method to extract a single converted X Coordinate. (Units=meters)
   */
   //---------------------------------------------------------------------------------
//   public double getX ()
//   {
//      return x[0];
//   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract a single converted X Coordinate at a given index. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double getLat (int index)
   {
      return lat[index];
   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract an array of converted X Coordinates. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double[] getLat ()
   {
      return lat;
   }

   //---------------------------------------------------------------------------------
   /**
   * Method to extract a single converted Y Coordinate. (Units=meters)
   */
   //---------------------------------------------------------------------------------
//   public double getY ()
//   {
//      return y[0];
//   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract a single converted Y Coordinate at a given index. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double getLon (int index)
   {
      return lon[index];
   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract an array of converted Y Coordinates. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double[] getLon ()
   {
      return lon;
   }


}
