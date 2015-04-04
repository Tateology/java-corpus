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


public class TestConvertAlbers 
{
   public static void main (String[] args)
   {
      AlbersEquidistant cvt;
      AlbersEquidistant cvt2;

      cvt = new AlbersEquidistant(AlbersEquidistant.CONUS);
      System.out.println("START:   LAT=   35.0  , LON=   -90.0");
      double[] xy = cvt.project(-90.0, 35.0);
      double[] lonlat;
      for(int n=0;n<10;n++) {
         
         System.out.println("COUNT: "+n+"  Y=    "+xy[1]+" , X=    "+xy[0]);
         lonlat = cvt.unproject(xy[0], xy[1]);
         System.out.println("COUNT: "+n+"  LAT=  "+lonlat[1]+" , LON=  "+lonlat[0]);
         xy = cvt.project(lonlat[0], lonlat[1]);
      } 


      System.out.println("-------------------------------------------");
/*    
      AlbersEquidistant geocvt = new AlbersEquidistant(AlbersEquidistant.CONUS);
      double[] p = geocvt.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);
      p = geocvt.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);

      GeoRectangle r = new GeoRectangle(-178.2,18.9,111.2,52.5);
      System.out.println("R= "+r);
      GeoRectangle pr = geocvt.projectedExtent(r);

      System.out.println("getX= "+r.getX()+"\ngetY= "+r.getY());

      p = geocvt.project(r.getX(), r.getY());
      System.out.println("x= "+p[0]+"\ny= "+p[1]);
      

      System.out.println("PR= "+pr);

      System.out.println("\n\nUNPROJECT CHECK:");
      p = geocvt.unproject(p[0],p[1]);
      System.out.println("lon= "+p[0]+"\nlat= "+p[1]);
 

      System.out.println("-------------------------------------------");

      PolarStereographic geocvt2 = new PolarStereographic();
      p = geocvt2.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);
      p = geocvt2.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);

      r = new GeoRectangle(-178.2,18.9,111.2,52.5);
      System.out.println("R= "+r);
      pr = geocvt2.projectedExtent(r);

      System.out.println("getX= "+r.getX()+"\ngetY= "+r.getY());

      p = geocvt2.project(r.getX(), r.getY());
      System.out.println("x= "+p[0]+"\ny= "+p[1]);


      System.out.println("PR= "+pr);

      System.out.println("\n\nUNPROJECT CHECK:");
      p = geocvt2.unproject(p[0],p[1]);
      System.out.println("lon= "+p[0]+"\nlat= "+p[1]);
 

      System.out.println("-------------------------------------------");

      HRAP geocvt3 = new HRAP();
      p = geocvt3.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);
      p = geocvt3.project(-178.2, 18.9);
      System.out.println("x= "+p[0]+"\ny= "+p[1]);

      r = new GeoRectangle(-178.2,18.9,111.2,52.5);
      System.out.println("R= "+r);
      pr = geocvt3.projectedExtent(r);

      System.out.println("getX= "+r.getX()+"\ngetY= "+r.getY());

      p = geocvt3.project(r.getX(), r.getY());
      System.out.println("x= "+p[0]+"\ny= "+p[1]);


      System.out.println("PR= "+pr);

      System.out.println("\n\nUNPROJECT CHECK:");
      p = geocvt3.unproject(p[0],p[1]);
      System.out.println("lon= "+p[0]+"\nlat= "+p[1]);

*/

   } // END main




} // END CLASS
