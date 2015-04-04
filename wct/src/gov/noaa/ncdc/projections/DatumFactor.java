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

package gov.noaa.ncdc.projections;


/**
* Creates a Datum correction factor given a preset constant or major and minor
* radii.
* @author Steve Ansari
*/
//************************************************************
public class DatumFactor 
{
   public static final double NAD27 = 1.00681479;
   public static final double NAD83 = 1.00673950;
   public static final double WGS84 = 1.00673950;
   public static final double CLARKE1866 = 1.00681479;
   //private final double R=6371007;

   //--------------------------------------------------------------------------------

   /**
   * Convert a single lat and lon
   * @param dlat Input Latitude (degrees)
   * @param datumRatio represents preset ratio for transformation (majorR**2 / minorR**2)
   * @return new latitude value
   */
   static public double spherical2Elliptical (double dlat, double datumRatio) {
      dlat = Math.toRadians(dlat);
      dlat = Math.atan(datumRatio*Math.tan(dlat));
      return (Math.toDegrees(dlat));
   }
   static public double spherical2Elliptical (double dlat, double majorR, double minorR) {
      dlat = Math.toRadians(dlat);
      double datumRatio = Math.pow(majorR, 2) / Math.pow(minorR, 2);
      dlat = Math.atan(datumRatio*Math.tan(dlat));
      return (Math.toDegrees(dlat));
   }

   static public double elliptical2Spherical (double dlat, double datumRatio) {
      dlat = Math.toRadians(dlat);
      dlat = Math.atan((1.0/datumRatio)*Math.tan(dlat));
      return (Math.toDegrees(dlat));
   }
   static public double elliptical2Spherical (double dlat, double majorR, double minorR) {
      dlat = Math.toRadians(dlat);
      double datumRatio = Math.pow(majorR, 2) / Math.pow(minorR, 2);
      dlat = Math.atan((1.0/datumRatio)*Math.tan(dlat));
      return (Math.toDegrees(dlat));
   }
   
   
   public static void main(String[] args) {
      if (args.length == 0) {
         System.out.println("USAGE: INPUT ARG1 = latitude (decimal degrees)");  
         System.out.println("             ARG2 = datum ratio");  
         System.out.println("=================================================");  
         System.out.println("Default Test Case: Spherical Lat = 40 , ratio = DatumFactor.NAD83");           
         System.out.println("NAD83 Adjusted Lat = " + spherical2Elliptical(40.0, WGS84));  
         
      }
      
   }

}
