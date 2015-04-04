/**
*      Copyright (c) 2007-2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/

package gov.noaa.ncdc.iosp.avhrr;


/**
 * Interpolation of anchor lat and lon points for avhrr gac data
 * 5 point lagrangian interpolation is used for anchor points between -85 and 85
 * degrees of latitude. Gnomic interpolation is used for points outside of that range.
 * @author afotos
 * Feb. 21 2008
 */
public class LatLonInterpolation {
	
	public LatLonInterpolation(){
		
	}

	public  static float[][] interpolate(float[] anchorLat, float[] anchorLon){
		float[][] latlon = new float[2][409];
		double[][] gVals = new double[409][2];
		float[] lat = LagrangianAnchorInterpolation.interpolate(anchorLat);
		float[] lon = LagrangianAnchorInterpolation.interpolate(anchorLon);
		float max = findAbsMax(anchorLat);
		if(max >= 85.0){
			gVals = LatLonInterpolationGnomic.interpolate(anchorLat, anchorLon);
			for(int i=0;i<409;i++){
//				System.out.println(i + "   " + gVals[i][0] + ":" + gVals[i][1]  + " -->" + lon[i] + ":" + lat[i]);
			}
		}
		
		for(int i=0;i<409;i++){
			if(lat[i] >= 85.0 && isAnchorPoint(i)){
				latlon[0][i] = (float)gVals[i][0];
				latlon[1][i] = (float)gVals[i][1];
//				System.out.println(i + "   " + gVals[i][0] + ":" + gVals[i][1]  + " -->" + lon[i] + ":" + lat[i]);
			}else{
				latlon[0][i] = lon[i];
				latlon[1][i] = lat[i];
			}
			
		}
		
		return latlon;
	}

	/**
	 * finds the maximum value(absolute value)  in an array
	 * @param t
	 * @return
	 */
	private static float findAbsMax(float[] t){
		float maximum = Math.abs(t[0]);   // start with the first value
		for (int i=1; i<t.length; i++) {
			if (Math.abs(t[i]) > maximum) {
				maximum = Math.abs(t[i]);   // new maximum
			}
		}
		return maximum;
	}
	
	/**
	 * determines if point is an anchorPoint
	 * @param i
	 * @return
	 */
	private static boolean isAnchorPoint(int q){
		if((q-4) %8 == 0){
			return true;
		}else{
			return false;
		}
	}
}
