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
// LatLon2Stereo.java     Author: Steve Ansari
/**
* Converts Geographic Latitude and Longitude to Polar Stereographic Coordinates.

*
* All input angles must be in decimal degrees.<br>
* Output Polar Stereographic Coordinates are in meters.<br>
* Earth Radius is defined as 6371007 meters.<br>
* 
* @author Steve Ansari
*/

package gov.noaa.ncdc.projections;


//************************************************************
public class LatLon2Stereo
{
  
   private static final double R=6371007;
   private double[] x, y;
   private static final double TOLERENCE = 1.0e-6;

   
   public static double[] convert(double dlat, double dlon, double latt, double lont) {
      double[] xy = new double[2];  
      double scale = 1.0;

        double lat = Math.toRadians(dlat);
        double lon = Math.toRadians(dlon);
        latt = Math.toRadians(latt);
        lont = Math.toRadians(lont);

        double sdlon = Math.sin(lon - lont);
        double cdlon = Math.cos(lon - lont);
        double sinlat = Math.sin(lat);
        double coslat = Math.cos(lat);
        double sinlatt = Math.sin(latt);
        double coslatt = Math.cos(latt);

        double k = 2.0 * R * scale / (1.0 + sinlatt * sinlat + coslatt * coslat * cdlon);
        xy[0] =  k * coslat * sdlon;
        xy[1] =  k * ( coslatt * sinlat - sinlatt * coslat * cdlon);
        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+xy[0]); 
        System.out.println("   LON -> X : "+dlon+" -> "+xy[1]); 


        double alpha = Math.atan( sdlon / (Math.cos(latt)*Math.tan(lat) - Math.sin(latt)*cdlon) );
        double h = Math.asin( Math.sin(lat)*Math.sin(latt) + Math.cos(lat)*Math.cos(latt)*cdlon );
        xy[0] = 2 * R * scale * Math.tan( Math.PI/4 - h/2 ) * Math.sin(alpha);
        xy[1] = 2 * R * scale * Math.tan( Math.PI/4 - h/2 ) * Math.cos(alpha);
        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+xy[0]); 
        System.out.println("   LON -> X : "+dlon+" -> "+xy[1]); 
        
        return xy;
        
        /*
        double H,DX,DXL,RADPD,REBYDX,ALONV,RERTH,ALA1,ALAT1,RMLL,SS60,ALON1,ALO1,REFLON;
        double POLEI, POLEJ, XI, XJ,ALAT,ALON,PI,ALA,ALO,RM;

        DX = 4762.5;
        H = 1.0;
        ALONV = 255;
        ALAT1 = 23.117;
        ALON1 = 240.977;
        RERTH = 6371200;
        SS60 = 1.86603;
        PI = 3.141592654;
        ALAT=dlat;
        ALON=dlon+360;

        H=1.0;
        DXL=DX;
        REFLON=ALONV-270.0;

        RADPD=PI/180.0;
        REBYDX=RERTH/DXL;
        // RADIUS TO LOWER LEFT HAND (LL) CORNER 
        ALA1=ALAT1*RADPD;
        RMLL=REBYDX*Math.cos(ALA1)*SS60/(1.0 + H * Math.sin(ALA1));
        // USE LL POINT INFO TO LOCATE POLE POINT 
        ALO1=(ALON1 - REFLON) * RADPD;
        POLEI=1.0 - RMLL * Math.cos(ALO1);
        POLEJ=1.0 - H * RMLL * Math.sin(ALO1);
        // RADIUS TO DESIRED POINT AND THE I J TOO 
        ALA=ALAT*RADPD;
        RM=REBYDX * Math.cos(ALA) * SS60/(1.0 + H * Math.sin(ALA));
        ALO=(ALON - REFLON) * RADPD;
        XI  = POLEI + RM * Math.cos(ALO);
        XJ  = POLEJ + H * RM * Math.sin(ALO);
        xy[0] = 4762.5*(XI-400.5);
        xy[1] = 4762.5*(XJ-1600.5);
        return xy;
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
   public LatLon2Stereo (double dlat, double dlon, double latt, double lont, double scale)
   {

        x = new double[1];
        y = new double[1];
/*
        double lat = Math.toRadians(dlat);
        double lon = Math.toRadians(dlon);
        latt = Math.toRadians(latt);
        lont = Math.toRadians(lont);
        // keep away from the singular point
        if ((Math.abs(lat + latt) <= TOLERENCE)) {
            lat = -latt * (1.0 - TOLERENCE);
        }

        double sdlon = Math.sin(lon - lont);
        double cdlon = Math.cos(lon - lont);
        double sinlat = Math.sin(lat);
        double coslat = Math.cos(lat);
        double sinlatt = Math.sin(latt);
        double coslatt = Math.cos(latt);

        double k = 2.0 * R * scale / (1.0 + sinlatt * sinlat + coslatt * coslat * cdlon);
        x[0] =  k * coslat * sdlon;
        y[0] =  k * ( coslatt * sinlat - sinlatt * coslat * cdlon);

        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+y[0]); 
        System.out.println("   LON -> X : "+dlon+" -> "+x[0]); 


        double alpha = Math.atan( sdlon / (Math.cos(latt)*Math.tan(lat) - Math.sin(latt)*cdlon) );
        double h = Math.asin( Math.sin(lat)*Math.sin(latt) + Math.cos(lat)*Math.cos(latt)*cdlon );
        x[0] = 2 * R * scale * Math.tan( Math.PI/4 - h/2 ) * Math.sin(alpha);
        y[0] = 2 * R * scale * Math.tan( Math.PI/4 - h/2 ) * Math.cos(alpha);

        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+y[0]); 
        System.out.println("   LON -> X : "+dlon+" -> "+x[0]); 

       */ 
        double H,DX,DXL,RADPD,REBYDX,ALONV,RERTH,ALA1,ALAT1,RMLL,SS60,ALON1,ALO1,REFLON;
        double POLEI, POLEJ, XI, XJ,ALAT,ALON,PI,ALA,ALO,RM;

        DX = 4762.5;
        H = 1.0;
        ALONV = 255;
        ALAT1 = 23.117;
        ALON1 = 240.977;
        RERTH = 6371200;
        SS60 = 1.86603;
        PI = 3.141592654;
        ALAT=dlat;
        ALON=dlon+360;

        H=1.0;
        DXL=DX;
        REFLON=ALONV-270.0;

        RADPD=PI/180.0;
        REBYDX=RERTH/DXL;
        /* RADIUS TO LOWER LEFT HAND (LL) CORNER */
        ALA1=ALAT1*RADPD;
        RMLL=REBYDX*Math.cos(ALA1)*SS60/(1.0 + H * Math.sin(ALA1));
        /* USE LL POINT INFO TO LOCATE POLE POINT */
        ALO1=(ALON1 - REFLON) * RADPD;
        POLEI=1.0 - RMLL * Math.cos(ALO1);
        POLEJ=1.0 - H * RMLL * Math.sin(ALO1);
        /* RADIUS TO DESIRED POINT AND THE I J TOO */
        ALA=ALAT*RADPD;
        RM=REBYDX * Math.cos(ALA) * SS60/(1.0 + H * Math.sin(ALA));
        ALO=(ALON - REFLON) * RADPD;
        XI  = POLEI + RM * Math.cos(ALO);
        XJ  = POLEJ + H * RM * Math.sin(ALO);

        /* Advance integer value if Grid value is negative */
/*  PERHAPS NOT NEEDED SINCE DECIMAL IS USED IN CONVERSION TO STEREOGRAPHIC COORDS 
        if(XI<0)
           XI = XI - 1;
        if(XJ<0)
           XJ = XJ - 1;
*/
/*
        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> HRAP --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+XI); 
        System.out.println("   LON -> X : "+dlon+" -> "+XJ); 
*/

// Find the integer grid cell coordinates then center.  This allows us to find our location on the fixed HRAP ConUS grid.
//XI = (int)XI + 0.5;
//XJ = (int)XJ + 0.5;

System.out.println("HRAP I: "+XI);
System.out.println("HRAP J: "+XJ);



        x[0] = 4762.5*(XI-400.5);
        y[0] = 4762.5*(XJ-1600.5);
/*
        System.out.println("-- Projection Conversion          --");
        System.out.println("-- LatLon --> Polar Stereographic --");
        System.out.println("   LAT -> Y : "+dlat+" -> "+y[0]); 
        System.out.println("   LON -> X : "+dlon+" -> "+x[0]); 
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
   public LatLon2Stereo (double[] dlat, double[] dlon, double latt, double lont, double scale)
   {
        int cnt = dlat.length; 
        x = new double[cnt];
        y = new double[cnt];

        for (int i=0; i<cnt; i++) {
/*
           double lat = Math.toRadians(dlat[i]);
           double lon = Math.toRadians(dlon[i]);
           // keep away from the singular point
           if ((Math.abs(lat + latt) <= TOLERENCE)) {
               lat = -latt * (1.0 - TOLERENCE);
           }

           double sdlon = Math.sin(lon - lont);
           double cdlon = Math.cos(lon - lont);
           double sinlat = Math.sin(lat);
           double coslat = Math.cos(lat);
           double sinlatt = Math.sin(latt);
           double coslatt = Math.cos(latt);

           double k = 2.0 * R * scale / (1.0 + sinlatt * sinlat + coslatt * coslat * cdlon);
           x[i] =  k * coslat * sdlon;
           y[i] =  k * ( coslatt * sinlat - sinlatt * coslat * cdlon);
*/


        double H,DX,DXL,RADPD,REBYDX,ALONV,RERTH,ALA1,ALAT1,RMLL,SS60,ALON1,ALO1,REFLON;
        double POLEI, POLEJ, XI, XJ,ALAT,ALON,PI,ALA,ALO,RM;

        DX = 4762.5;
        H = 1.0;
        ALONV = 255;
        ALAT1 = 23.117;
        ALON1 = 240.977;
        RERTH = 6371200;
        SS60 = 1.86603;
        PI = 3.141592654;
        ALAT=dlat[i];
        ALON=dlon[i]+360.0;

        H=1.0;
        DXL=DX;
        REFLON=ALONV-270.0;

        RADPD=PI/180.0;
        REBYDX=RERTH/DXL;
        /* RADIUS TO LOWER LEFT HAND (LL) CORNER */
        ALA1=ALAT1*RADPD;
        RMLL=REBYDX*Math.cos(ALA1)*SS60/(1.0 + H * Math.sin(ALA1));
        /* USE LL POINT INFO TO LOCATE POLE POINT */
        ALO1=(ALON1 - REFLON) * RADPD;
        POLEI=1.0 - RMLL * Math.cos(ALO1);
        POLEJ=1.0 - H * RMLL * Math.sin(ALO1);
        /* RADIUS TO DESIRED POINT AND THE I J TOO */
        ALA=ALAT*RADPD;
        RM=REBYDX * Math.cos(ALA) * SS60/(1.0 + H * Math.sin(ALA));
        ALO=(ALON - REFLON) * RADPD;
        XI  = POLEI + RM * Math.cos(ALO);
        XJ  = POLEJ + H * RM * Math.sin(ALO);

        /* Advance integer value if Grid value is negative */
/*  PERHAPS NOT NEEDED SINCE DECIMAL IS USED IN CONVERSION TO STEREOGRAPHIC COORDS 
        if(XI<0)
           XI = XI - 1;
        if(XJ<0)
           XJ = XJ - 1;
*/


// Find the integer grid cell coordinates then center.  This allows us to find our location on the fixed HRAP ConUS grid.
XI = (int)XI + 0.5;
XJ = (int)XJ + 0.5;

System.out.println("HRAP I: "+XI);
System.out.println("HRAP J: "+XJ);






        x[i] = 4762.5*(XI-400.5);
        y[i] = 4762.5*(XJ-1600.5);

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
   public double getX (int index)
   {
      return x[index];
   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract an array of converted X Coordinates. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double[] getX ()
   {
      return x;
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
   public double getY (int index)
   {
      return y[index];
   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract an array of converted Y Coordinates. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double[] getY ()
   {
      return y;
   }

   
   
   
   
   public static void main(String[] args) {
      double lat = 35.0;
      double lon = -91.0;
      
      double[] xy = convert(lat, lon, 60.0, -105.0);
      System.out.println("x: "+xy[0]+" y: "+xy[1]);
      
      LatLon2Stereo cvt = new LatLon2Stereo(lat, lon, 60.0, -105.0, 1.0);
      System.out.println("x: "+cvt.getX()[0]+" y: "+cvt.getY()[0]);
      
   }
   
   

}
