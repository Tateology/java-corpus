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

public class LatLonInterpolationGnomic {
	
	public LatLonInterpolationGnomic(){
		
	}

	public  static double[][] interpolate(float[] anchorLat, float[] anchorLon){
		double[][] latlon = new double[409][2];
		
		double[] x = new double[409];
		double[] y = new double[409];
		double[] z = new double[409];
		double[] xtie = new double[51];
		double[] ytie = new double[51];
		double[] ztie = new double[51];
		
		for(int i =0;i<51;i++){
			double rlat = Math.toRadians(anchorLat[i]);
			double rlon = Math.toRadians(anchorLon[i]);
			xtie[i] = Math.cos(rlon) * Math.cos(rlat);
			ytie[i] = Math.sin(rlon) * Math.cos(rlat);
			ztie[i] = Math.sin(rlat);
		}
		
		
		x[4] = xtie[0];
		y[4] = ytie[0];
		z[4] = ztie[0];
		for (int i = 4; i > 0; i--) {
			x[4 - i] = quadraticInterpolation(xtie[0], xtie[1], xtie[2], -8 - i);
			y[4 - i] = quadraticInterpolation(ytie[0], ytie[1], ytie[2], -8 - i);
			z[4 - i] = quadraticInterpolation(ztie[0], ztie[1], ztie[2], -8 - i);
		}

		int pixelNum = 4;
		for (int t = 0; t < 49; t++) {
			pixelNum++;
			for (int i = 0; i < 7; i++) {
				x[pixelNum] = quadraticInterpolation(xtie[t], xtie[t + 1], xtie[t + 2], -7 + i);
				y[pixelNum] = quadraticInterpolation(ytie[t],ytie[t + 1], ytie[t + 2], -7 + i);
				z[pixelNum] = quadraticInterpolation(ztie[t], ztie[t + 1], ztie[t + 2], -7 + i);
				pixelNum++;
			}
			x[pixelNum] = xtie[t + 1];
			y[pixelNum] = ytie[t + 1];
			z[pixelNum] = ztie[t + 1];
		}

		for (int g = 0; g < 12; g++) {
			pixelNum++;
			x[pixelNum] = quadraticInterpolation(xtie[48], xtie[49], xtie[50], 1 + g);
			y[pixelNum] = quadraticInterpolation(ytie[48], ytie[49], ytie[50], 1 + g);
			z[pixelNum] = quadraticInterpolation(ztie[48], ztie[49], ztie[50], 1 + g);
		}

		for (int k = 0; k < 409; k++) {
			double xy = Math.sqrt(Math.pow(x[k], 2) + Math.pow(y[k], 2));
			latlon[k][0] = Math.toDegrees(Math.atan2(y[k], x[k]));
			latlon[k][1] =  Math.toDegrees(Math.atan(z[k]/xy));
		}	
		
/**
		int pn = 4;
		for(int q=0;q<51;q++){
			latlon[pn][0] = anchorLon[q];
			latlon[pn][1] = anchorLat[q];
			pn += 8;
		}
		*/
		return latlon;
	}

/**
	public static double[][][] getCalculatedCoords(double[] gLatTiePoints, double[] gLonTiePoints) {
		double[] gnomonicLat = new double[409];
		double[] gnomonicLon = new double[409];
		double[][] calculatedCoords = new double[409][2];
		int pixelNum = 3;
		gnomonicLat[4] = gLatTiePoints[0];
		gnomonicLon[4] = gLatTiePoints[0];
		for (int i = 4; i > 0; i--) {
			gnomonicLat[4 - i] = quadraticInterpolation(gLatTiePoints[0], gLatTiePoints[1], gLatTiePoints[2], -8 - i);
			gnomonicLon[4 - i] = quadraticInterpolation(gLonTiePoints[0], gLonTiePoints[1], gLonTiePoints[2], -8 - i);
		}

		for (int t = 0; t < 49; t++) {
			pixelNum++;
			for (int i = 0; i < 7; i++) {
				gnomonicLat[pixelNum] = quadraticInterpolation(gLatTiePoints[t], gLatTiePoints[t + 1], gLatTiePoints[t + 2], -7 + i);
				gnomonicLon[pixelNum] = quadraticInterpolation(gLonTiePoints[t], gLonTiePoints[t + 1], gLonTiePoints[t + 2], -7 + i);
				pixelNum++;
			}
			gnomonicLat[pixelNum] = gLatTiePoints[t + 1];
			gnomonicLon[pixelNum] = gLonTiePoints[t + 1];
		}

		for (int g = 0; g < 13; g++) {
			pixelNum++;
			gnomonicLat[pixelNum] = quadraticInterpolation(gLatTiePoints[48], gLatTiePoints[49], gLatTiePoints[50], 1 + g);
			gnomonicLon[pixelNum] = quadraticInterpolation(gLonTiePoints[48], gLonTiePoints[49], gLonTiePoints[50], 1 + g);
		}

		for (int k = 0; k < 409; k++) {
			double[] temp = convertFromGnomonicCoords(gnomonicLat[k], gnomonicLon[k]);
			calculatedCoords[k][0] = (float) Math.toDegrees(temp[0]);
			calculatedCoords[k][1] = (float) (180 - Math.toDegrees(temp[1]));
		}
		return calculatedCoords;
	}	
	
*/
	public  static double quadraticInterpolation(double d0, double d1, double d2, int p) {
		double val1 = (((d2 - d0) / 2.0) / 8.0) * p;
		double val2 = (((d2 - 2.0 * d1 + d0) / 2.0) / 64.0) * Math.pow(p,2);
		double d = d1 + val1 + val2;
		return d;
	}
	
}
