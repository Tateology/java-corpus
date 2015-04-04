/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * 
 * This software is distributed under the Open Source Definition, 
 * which may be found at http://www.opensource.org/osd.html.
 * 
 * In particular, redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided that the 
 * following conditions are met:
 * 
 *  1. Redistributions of source code must retain this notice, this 
 *     list of conditions, and the following disclaimer.
 *  2. Redistributions in binary form must provide access to this 
 *     notice, this list of conditions, the following disclaimer, 
 *     and the underlying source code.
 *  3. All modifications to this software must be clearly documented, 
 *     and are solely the responsibility of the agent making the 
 *     modifications.
 *  4. If significant modifications or enhancements are made to this 
 *     software, the Java NEXRAD Tools development team leader  
 *     (Steve.Ansari@noaa.gov) should be notified.
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE IN THE PUBLIC DOMAIN AND ARE 
 * FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS 
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
*
* USES A SPHERICAL MODEL OF THE EARTH !!!  WILL NOT WORK WITH STANDARD ESRI ALBERS PROJECTION
*
*
* Separate classes used for forward and reverse conversions
* to save time wasted from unneeded calculations.<br><br>
*
* Calculation is done when object is constructed.  This saves time wasted from performing 
* the calculation each time the getX() or getY() methods are called.<br><br> 
*
* Equation taken from pg 114 of <br><B><U>
* Map Projections: Theory and Applications</U></B> by <B>Frederick Pearson</B><br><br>
*
* All input angles must be in decimal degrees.<br>
* Output Albers Coordinates are in meters.<br>
* Earth Radius is defined as 6371007 meters.<br>
*
* @author Steve Ansari
*/
package gov.noaa.ncdc.projections;


//************************************************************
public class LatLon2Albers
{
  
   private final double R=6371007;
   private double phi1, phi2, lamb0, S, latorigin;
   private double dlamb, rho1, rho2, f1, f2, f3, x, y, work, correction;

   //--------------------------------------------------------------------------------
   /** Construct default conversion using preset parameters for ConUS, AL and HI
   * @param dlat Input Latitude
   * @param dlon Input Longitude
   * @param Region Preset Region to define Albers Projection ("Alaska", "Hawaii" or "ConUS" (default)) 
   */
   //------------------------------------------------------------------------------    
   public LatLon2Albers (double dlat, double dlon, String Region)
   {
      // Default Region to ConUS
      if (Region.equals("Alaska"))
      {
         phi1=55; phi2=65; lamb0=-154; S=1.0; latorigin=50;        
      }
      else if (Region.equals("Hawaii"))
      {
         phi1=8; phi2=18; lamb0=-157; S=1.0; latorigin=13;
      }
      else
      {
         phi1=29.5; phi2=45.5; lamb0=-96; S=1.0; latorigin=37.5;
      }
   
      // Check for a latitude of origin that differs from the midpoint of the two standard parallels
      if (latorigin != (phi1 + phi2)/2)
      {
         // Find correction factor to adjust for latitude of origin
	 
         dlamb=0;  // (lamb0 - lamb0)
     
         // Convert everything to radians for use with sin and cos  
         double dtlat=Math.toRadians(latorigin);
         double dtdlon=Math.toRadians(lamb0);
         double tphi1=Math.toRadians(phi1);
         double tphi2=Math.toRadians(phi2);
         double tlamb0=Math.toRadians(lamb0);

         // Perform calculation upon initialization so it's only done once
         rho1 = (2*R*Math.cos(tphi1)) / (Math.sin(tphi1) + Math.sin(tphi2));
         rho2 = (2*R*Math.cos(tphi2)) / (Math.sin(tphi1) + Math.sin(tphi2));

         f1 = (rho1 + rho2) / 2;
         f2 = (Math.sin(tphi1) + Math.sin(tphi2)) / 2;
         f3 = Math.sqrt( (rho1*rho1) + (4*R*R*(Math.sin(tphi1) - Math.sin(dtlat)) / (Math.sin(tphi1) + Math.sin(tphi2)) ) );
    
         work=dlamb*f2;
         work=Math.toRadians(work);
  
         correction = S*(f1-f3*Math.cos(work));	
      }
      else
         correction = 0;

   
      dlamb=dlon-lamb0;
     
      // Convert everything to radians for use with sin and cos  
      dlat=Math.toRadians(dlat);
      dlon=Math.toRadians(dlon);
      phi1=Math.toRadians(phi1);
      phi2=Math.toRadians(phi2);
      lamb0=Math.toRadians(lamb0);

      // Perform calculation upon initialization so it's only done once
      rho1 = (2*R*Math.cos(phi1)) / (Math.sin(phi1) + Math.sin(phi2));
      rho2 = (2*R*Math.cos(phi2)) / (Math.sin(phi1) + Math.sin(phi2));

      f1 = (rho1 + rho2) / 2;
      f2 = (Math.sin(phi1) + Math.sin(phi2)) / 2;
      f3 = Math.sqrt( (rho1*rho1) + (4*R*R*(Math.sin(phi1) - Math.sin(dlat)) / (Math.sin(phi1) + Math.sin(phi2)) ) );
    
      work=dlamb*f2;
      work=Math.toRadians(work);
  
      x = S*f3*Math.sin(work);
      y = S*(f1-f3*Math.cos(work)) - correction;

   }
   //--------------------------------------------------------------------------------
   /** Construct conversion using custom parameters.<br>
   * @param lat Input Latitude
   * @param lon Input Longitude
   * @param p1 Standard Parallel 1 (Must be higher latitude) 
   * @param p2 Standard Parallel 2 (p2 < p1)
   * @param lt0 Latitude of Origin 
   * @param ln0 Central Meridian
   * @param S0 Scale Ratio
   */
   //------------------------------------------------------------------------------
   public LatLon2Albers (double dlat, double dlon, double p1, double p2, double lt0, double ln0, double S0)
   {
      // Set up custom region
      phi1=p1;
      phi2=p2;
      lamb0=ln0;
      latorigin=lt0;
      S=S0;
   
      // Check for a latitude of origin that differs from the midpoint of the two standard parallels
      if (latorigin != (phi1 + phi2)/2)
      {
         // Find correction factor to adjust for latitude of origin
         dlamb=0; // (lamb0 - lamb0)
     
         // Convert everything to radians for use with sin and cos  
         double dtlat=Math.toRadians(latorigin);
         double dtdlon=Math.toRadians(lamb0);
         double tphi1=Math.toRadians(phi1);
         double tphi2=Math.toRadians(phi2);
         double tlamb0=Math.toRadians(lamb0);

         // Perform calculation upon initialization so it's only done once
         rho1 = (2*R*Math.cos(tphi1)) / (Math.sin(tphi1) + Math.sin(tphi2));
         rho2 = (2*R*Math.cos(tphi2)) / (Math.sin(tphi1) + Math.sin(tphi2));

         f1 = (rho1 + rho2) / 2;
         f2 = (Math.sin(tphi1) + Math.sin(tphi2)) / 2;
         f3 = Math.sqrt( (rho1*rho1) + (4*R*R*(Math.sin(tphi1) - Math.sin(dtlat)) / (Math.sin(tphi1) + Math.sin(tphi2)) ) );
    
         work=dlamb*f2;
         work=Math.toRadians(work);
  
         correction = S*(f1-f3*Math.cos(work));	 
	 
      }
      else
         correction = 0;

         
      dlamb=dlon-lamb0;
     
      // Convert everything to radians for use with sin and cos  
      dlat=Math.toRadians(dlat);
      dlon=Math.toRadians(dlon);
      phi1=Math.toRadians(phi1);
      phi2=Math.toRadians(phi2);
      lamb0=Math.toRadians(lamb0);

      // Perform calculation upon initialization so it's only done once
      rho1 = (2*R*Math.cos(phi1)) / (Math.sin(phi1) + Math.sin(phi2));
      rho2 = (2*R*Math.cos(phi2)) / (Math.sin(phi1) + Math.sin(phi2));

      f1 = (rho1 + rho2) / 2;
      f2 = (Math.sin(phi1) + Math.sin(phi2)) / 2;
      f3 = Math.sqrt( (rho1*rho1) + (4*R*R*(Math.sin(phi1) - Math.sin(dlat)) / (Math.sin(phi1) + Math.sin(phi2)) ) );
    
      work=dlamb*f2;
      work=Math.toRadians(work);
  
      x = S*f3*Math.sin(work);
      y = S*(f1-f3*Math.cos(work)) - correction;
   }
   //---------------------------------------------------------------------------------
   /**
   * Method to extract the converted X Coordinate. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double getX ()
   {
      return x;
   }

   //---------------------------------------------------------------------------------
   /**
   * Method to extract the converted Y Coordinate. (Units=meters)
   */
   //---------------------------------------------------------------------------------
   public double getY ()
   {
      return y;
   }


}
