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

public class LagrangianAnchorInterpolationLAC {

	static double[] alat = { 83.30469, 84.03125, 84.4375, 84.609375, 84.63281, 84.546875, 84.390625, 84.17969, 83.953125, 83.703125,
			83.44531, 83.17969, 82.921875, 82.65625, 82.39844, 82.140625, 81.88281, 81.63281, 81.38281, 81.13281, 80.88281,
			80.640625, 80.390625, 80.14844, 79.89844, 79.64844, 79.39844, 79.14844, 78.890625, 78.63281, 78.359375, 78.08594,
			77.80469, 77.515625, 77.21875, 76.90625, 76.58594, 76.25, 75.890625, 75.50781, 75.109375, 74.67969, 74.21875, 73.71094,
			73.16406, 72.55469, 71.875, 71.109375, 70.22656, 69.17969, 67.91406 };

	static double[] alon = { 154.90625, 165.58594, 176.11719, -174.00781, -165.13281, -157.40625, -150.80469, -145.1875, -140.42188,
			-136.35156, -132.85938, -129.83594, -127.19531, -124.86719, -122.80469, -120.953125, -119.28125, -117.765625,
			-116.38281, -115.109375, -113.921875, -112.828125, -111.796875, -110.83594, -109.921875, -109.05469, -108.22656,
			-107.4375, -106.67969, -105.953125, -105.24219, -104.55469, -103.88281, -103.22656, -102.578125, -101.9375, -101.30469,
			-100.67969, -100.0625, -99.4375, -98.80469, -98.171875, -97.53125, -96.875, -96.21094, -95.515625, -94.80469, -94.0625,
			-93.28125, -92.4375, -91.515625 };

	
	static float[] anchorLat = { 85.3778f, 86.6406f,  87.5663f, 88.1496f,
		88.3344f, 88.1636f, 87.7996f, 87.3662f, 86.9192f,
		86.4798f, 86.0556f, 85.6485f, 85.2584f, 84.8840f,
		84.5240f, 84.1766f, 83.8404f, 83.5139f, 83.1958f,
		82.8848f, 82.5796f, 82.2792f, 81.9824f, 81.6883f,
		81.3958f, 81.1040f, 80.8119f, 80.5186f, 80.2229f,
		79.9238f, 79.6204f, 79.3113f, 78.9953f, 78.6711f,
		78.3371f, 77.9917f, 77.6327f, 77.2582f, 76.8653f,
		76.4512f, 76.0121f, 75.5438f, 75.0407f, 74.4964f,
		73.9022f, 73.2471f, 72.5164f, 71.6897f, 70.7377f,
		69.6160f, 68.2516f };
	
	static float[] anchorLon = {112.2573f, 103.6450f, 90.1738f, 69.1891f,
		42.2035f, 18.3901f , 24.981f , -74.715f,  -13.9753f ,
		-18.4670f,  -21.7324f ,   -24.2089f ,   -26.1526f ,  -27.7214f ,
		-29.0173f, -30.1087f ,   -31.0432f ,   -31.8549f , -32.5687f ,
		-33.2033f, -33.7730f ,   -34.2891f ,   -34.7601f ,  -35.1933f ,
		-35.5943f, -35.9677f ,   -36.3176f ,   -36.6472f ,  -36.9591f ,
		-37.2559f, -37.5396f ,   -37.8119f ,   -38.0744f ,  -38.3285f ,
		-38.5756f, -38.8167f ,   -39.0530f ,   -39.2855f ,  -39.5152f ,
		-39.7430f, -39.9699f ,   -40.1970f ,   -40.4252f ,  -40.6557f ,
		-40.8899f,  -41.1291f ,  -41.3753f ,   -41.6307f ,  -41.8984f ,
		 -42.1828f, -42.4905f } ;

	
	/**
	 * @param args
	 */
	public static float[] interpolate(float[] anchorVals) {
		
		int m = 2;  //5 point interpolation
		int numAnchor = 51;
		int numPix = 2048;
		int numSkip =40;
		int numStart = 25;
		
		
//		double[] y = new double[409];
		float[] temp_y = new float[numPix];
		float[] temp_anchor = new float[numAnchor];
		System.arraycopy(anchorVals, 0, temp_anchor, 0, 51);
		
		int discon = 0;
		if(min(temp_anchor) < -140 && max(temp_anchor) >140.0){
			discon = 1;
			for(int i=0;i<51;i++){
				if(temp_anchor[i] <0.0){
					temp_anchor[i] = temp_anchor[i] + 360.0f;
				}
			}
		}
		
		for(int ipix=0;ipix<numPix;ipix++){
			int ia = (ipix - numStart)/numSkip;// + 1;
			int ia1 = Math.min(numAnchor, Math.max(0,ia - m));
			int ia2 = Math.max(0, Math.min(numAnchor, ia + m));
			int x = ipix;
			for(int i = ia1;i<ia2;i++){
				double L = 1.0;
				int xa_i = i * 8 + 4;
				for(int j = ia1;j<ia2;j++){
					int xa_j = j * 8 +4;
					if(i != j){
						L = L * (x-xa_j)/(xa_i-xa_j);
					}
				}
				temp_y[ipix] = (float)(temp_y[ipix] + L * temp_anchor[i]);
			}
		}
		
		for(int i=0;i<2048;i++){
//			y[i] = 0.5 * (temp_y[5*i] + temp_y[5 * i - 1]);
			if(temp_y[i] >180.0){
				temp_y[i] =temp_y[i] -360.0f;
			}
			if(temp_y[i] < -180.0){
				temp_y[i] =360.0f + temp_y[i];
			}
			
//			System.out.println(i + ": lat : lon --> "  + temp_y[i]);
		}
		return temp_y;
		}



	public static double max(float[] t) {
		float maximum = t[0];   // start with the first value
		for (int i=1; i<t.length; i++) {
			if (t[i] > maximum) {
				maximum = t[i];   // new maximum
			}
		}
		return maximum;
	}

	
	public static double min(float[] t) {
		float min = t[0];   // start with the first value
		for (int i=1; i<t.length; i++) {
			if (t[i] < min) {
				min = t[i];   // new maximum
			}
		}
		return min;
	}
	
	public static void main(String[] args){
		float[] lons = interpolate(anchorLon);
		float[] lats = interpolate(anchorLat);
		
		for(int i=0;i<2048;i++){
			//System.out.println("point " + i + " lat:lon --> " + lats[i] + " : " + lons[i]);
		}
	}
	
}
 
