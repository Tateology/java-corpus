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



public class TestConvertStereo 
{
   public static void main (String[] args)
   {
      /*
      LatLon2Stereo cvt;
      Stereo2LatLon cvt2;

      cvt = new LatLon2Stereo(35.0, -96.0, 60.0, -105.0, 1.0);
       System.out.println("START:   LAT=   35.0  , LON=   -96.0");

      for(int n=0;n<10;n++) {
         System.out.println("COUNT: "+n+"  Y=    "+cvt.getY(0)+" , X=    "+cvt.getX(0));
         cvt2 = new Stereo2LatLon(cvt.getX(0),cvt.getY(0),60.0,-105.0);
         System.out.println("COUNT: "+n+"  LAT=  "+cvt2.getLat(0)+" , LON=  "+cvt2.getLon(0));
         cvt = new LatLon2Stereo(cvt2.getLat(0), cvt2.getLon(0), 60.0, -105.0, 1.0);
      } 
      */

      
      double[] xy = LatLon2Stereo.convert(41.0, -83.0, 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      double[] lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);
      xy = LatLon2Stereo.convert(lonlat[1], lonlat[0], 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);
      xy = LatLon2Stereo.convert(lonlat[1], lonlat[0], 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);
      xy = LatLon2Stereo.convert(lonlat[1], lonlat[0], 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);
      xy = LatLon2Stereo.convert(lonlat[1], lonlat[0], 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);
      xy = LatLon2Stereo.convert(lonlat[1], lonlat[0], 40.0, -84.0);
      System.out.println(xy[0]+" , "+xy[1]);
      lonlat = Stereo2LatLon.convert(xy[0], xy[1], 40.0, -84.0);
      System.out.println(lonlat[0]+" , "+lonlat[1]);


   } // END main




} // END CLASS
