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
package gov.noaa.ncdc.iosp.avhrr.ver1;

import gov.noaa.ncdc.iosp.avhrr.AvhrrConstants;


/**
 * Calibration methods for Avhrr Gac data
 * derived from following doc
 * http://www2.ncdc.noaa.gov/docs/podug/html/c3/sec3-3.htm
 * 
 * @author afotos 
 * 2/10/2008
 *
 */
public class AvhrrCalibratorVer1 implements AvhrrConstants{
	/**
	 * visible channel calibration constants
	 * http://www2.ncdc.noaa.gov/docs/podug/html/c3/sec3-3.htm
	 * Table 3.3.2-2
	 */
	private static double[] TIROSN_W=  {0.325 , 0.303};
	private static double[] TIROSN_F= {443.3, 313.5 }; 
	private static double[] NOAA6_W  = {0.109,  0.223}; 
	private static double[] NOAA6_F = {179.0, 233.7}; 
	private static double[] NOAA7_W = {0.108, 0.249};
	private static double[] NOAA7_F = {177.5, 261.9};	
	private static double[] NOAA8_W = {0.113, 0.230};
	private static double[] NOAA8_F = {183.4, 242.8};
	private static double[] NOAA9_W = {0.117, 0.239} ;
	private static double[] NOAA9_F = {191.3, 251.8};
	private static double[] NOAA10_W =  {0.108 , 0.222} ;
	private static double[] NOAA10_F = {178.8 , 231.5};
	private static double[] NOAA11_W = {0.113, 0.229};
	private static double[] NOAA11_F = {184.1, 241.1};
	private static double[] NOAA12_W = {0.124, 0.219} ;
	private static double[] NOAA12_F =  {200.1, 229.9};
    private static double[] NOAA13_W = { 0.121, 0.243};
    private static double[] NOAA13_F =  {194.09,  249.42};
    private static double[] NOAA14_W = {0.136,  0.245} ;
    private static double[] NOAA14_F = {221.42, 252.29};
 
    //central wave numbers for each satellite from 
    //http://www2.ncdc.noaa.gov/docs/podug/html/c1/sec1-4.htm
    //only values for 1 temperature range are used
	static double[] TIROSN_CW = {2635.15, 911.54 ,0.00};  //Table 1.4.1-1    225k - 275k  Range
	static double[] NOAA6_CW = {2653.90, 911.41, 0.00};  //Table 1.4.2-1    225k - 275k  Range
	static double[] NOAA7_CW = {2670.3, 926.80, 840.500 };  //Table 1.4.3-1    225k - 275k  Range
	static double[] NOAA8_CW = {2636.05, 913.865, 0.00}; //Table 1.4.4-1   225k - 275k  Range
	static double[] NOAA9_CW = {2674.81, 929.02, 844.80 };  //Table 1.4.5-1    225-275 Range
	static double[] NOAA10_CW = {2657.60, 909.18, 0.00};  //Table 1.4.6-1   225-275K Range
	static double[] NOAA11_CW = {2668.15, 927.36, 841.81 };  //Table 1.4.7-1.  225-275K Range
	static double[] NOAA12_CW = {2636.669, 920.5504, 837.0251 };  //Table 1.4.8-1  230-270K Range
	static double[] NOAA13_CW = {2640.147, 924.5165, 836.4339 }; //Table 1.4.9-1   230-270K Range
	static double[] NOAA14_CW = { 2642.807, 928.8284, 834.8066}; //Table 1.4.10-1 230-270K Range
	
	//http://www2.ncdc.noaa.gov/docs/podug/html/c3/sec3-3.htm
	//Table 3.3.2-1. Pre-launch slopes and intercepts for AVHRR Channels 1 and 2.
	static double[] TIROSN_SLOPE = {0.1071, 0.1051};
	static double[] TIROSN_INTERCEPT = { -3.9,  -3.5};
	static double[] NOAA6_SLOPE = {0.1071 , 0.1058};
	static double[] NOAA6_INTERCEPT = {  -4.1136, -3.4539};	
	static double[] NOAA7_SLOPE = { 0.1068 , 0.1069};
	static double[] NOAA7_INTERCEPT = {-3.4400 , -3.488};	
	static double[] NOAA8_SLOPE = { 0.1060, 0.1060};
	static double[] NOAA8_INTERCEPT = { -4.1619, -4.1492};	
	static double[] NOAA9_SLOPE = {0.1063 , 0.1075};
	static double[] NOAA9_INTERCEPT = { -3.8464, -3.8770};	
	static double[] NOAA10_SLOPE = { 0.1059, 0.1061};
	static double[] NOAA10_INTERCEPT = {-3.5279 ,  -3.4766 };	
	static double[] NOAA11_SLOPE = { 0.0906, 0.0900};
	static double[] NOAA11_INTERCEPT = { -3.730, -3.390 };	
	static double[] NOAA12_SLOPE = {0.1042 ,0.1014 };
	static double[] NOAA12_INTERCEPT = {-4.4491 , -3.9925 };	
	static double[] NOAA13_SLOPE = {0.1076 ,  0.1035};
	static double[] NOAA13_INTERCEPT = { -3.9747 , -3.8280 };	
	static double[] NOAA14_SLOPE = {0.1081 , 0.1090};
	static double[] NOAA14_INTERCEPT = {-3.8648 , -3.6749};	

    /**
     * 3.3.1 Thermal Channel Calibration - equation 3.3.1-2
     * 
     * @param shipId - String - ship name
     * @param chanNum - IR channel, chan 3, 4, or 5
     * @param data - raw counts for each pixel
     * @return  float[]  brightness temperatures 
     */
    public static float[] calculateBT(String shipName, int chanNum, IScanlineVer1 dr, int pixels){
    	double c1 = 1.1910659;
    	double c2 = 1.438833;
    	
    	//calculate Enegry first
    	float[] values = new float[pixels];
    	double slope, intercept;
    	slope = dr.getSlopeCoeffs()[chanNum -1];
    	slope = slope/Math.pow(2, 30);
    	intercept = dr.getInterceptCoeffs()[chanNum - 1];
    	intercept = intercept/Math.pow(2,22);
    	short[][] data = dr.getData();
    	for(int i=0;i<pixels;i++){
    		short count  = data[chanNum - 1][i];
    		double e = slope * count + intercept;
    		double v = findCentralWaveNum(shipName , chanNum);
    		double tmp = (c1 * Math.pow(v,3))/e;
    	    double teStar = (c2 * v) / (Math.log(1.0 + tmp));
    		values[i] =  (float) teStar;
    	}
    	return values;
    }
    
    
    /**
     * returns central wave number used for calculating brightnessTemp
     * @param shipName
     * @param chanNum
     * @return central wav number
     */
    private static double findCentralWaveNum(String shipName, int chanNum){
    	double cwnum = 0.00;
    	
    	if(TIROSN.equals(shipName)){
    		cwnum =  TIROSN_CW[chanNum -3];
    	}else if(NOAA6.equals(shipName)){
    		cwnum =  NOAA6_CW[chanNum -3];
    	}else if(NOAA7.equals(shipName)){
    		cwnum =  NOAA7_CW[chanNum -3];
    	}else if(NOAA8.equals(shipName)){
    		cwnum =  NOAA8_CW[chanNum -3];
    	}else if(NOAA9.equals(shipName)){
    		cwnum =  NOAA9_CW[chanNum -3];
    	}else if(NOAA10.equals(shipName)){
    		cwnum =  NOAA10_CW[chanNum -3];
    	}else if(NOAA11.equals(shipName)){
    		cwnum =  NOAA11_CW[chanNum -3];
    	}else if(NOAA12.equals(shipName)){
    		cwnum =  NOAA12_CW[chanNum -3];
    	}else if(NOAA13.equals(shipName)){
    		cwnum =  NOAA13_CW[chanNum -3];
    	}else if(NOAA14.equals(shipName)){
    		cwnum =  NOAA14_CW[chanNum -3];
    	}
    	return cwnum;
    }
    
    /**
     * section 3.3.1 Thermal Channel Calibration
     * equation 3.3.1-1
     * Calculates the Radiance for Avhrr IR channels (3,4,5)
     * @param shipId
     * @param chanNum
     * @param dr
     * @return - radiance (mW/(m^2-sr-cm-1)
     */
    public static float[] calculateRadianceForThermal(short shipId, int chanNum, IScanlineVer1 dr, int pixels){
    	float[] vals = new float[pixels];
    	double slope, intercept, radiance;
    	slope = dr.getSlopeCoeffs()[chanNum -1];
    	slope = slope/Math.pow(2, 30);
    	intercept = dr.getInterceptCoeffs()[chanNum - 1];
    	intercept = intercept/Math.pow(2,22);
    	short[][] data = dr.getData();
    	for(int i=0;i<pixels;i++){
    		short count  = data[chanNum - 1][i];
    		radiance = slope * count + intercept;
    		vals[i] =  (float)radiance;
    	}  	
    	return vals;
    }
    
    /**
     * returns equivalent width constant for visible channels
     *  http://www2.ncdc.noaa.gov/docs/podug/html/c3/sec3-3.htm
     * @param shipId
     * @param chanNum
     * @return
     */
    private static double findEquivWidth(String shipName, int chanNum){
    	double w = 0.0;
    	if(TIROSN.equals(shipName)){
    		w =  TIROSN_W[chanNum -1];
    	}else if(NOAA6.equals(shipName)){
    		w =  NOAA6_W[chanNum -1];
    	}else if(NOAA7.equals(shipName)){
    		w =  NOAA7_W[chanNum -1];
    	}else if(NOAA8.equals(shipName)){
    		w =  NOAA8_W[chanNum -1];
    	}else if(NOAA9.equals(shipName)){
    		w =  NOAA9_W[chanNum -1];
    	}else if(NOAA10.equals(shipName)){
    		w =  NOAA10_W[chanNum -1];
    	}else if(NOAA11.equals(shipName)){
    		w =  NOAA11_W[chanNum -1];
    	}else if(NOAA12.equals(shipName)){
    		w =  NOAA12_W[chanNum -1];
    	}else if(NOAA13.equals(shipName)){
    		w =  NOAA13_W[chanNum -1];
    	}else if(NOAA14.equals(shipName)){
    		w =  NOAA14_W[chanNum -1];
    	}
    	return w;
    }
    
    
    /**
     * returns spectral irradiance for a satellite/channel
     * Constant F in table 
     * Table 3.3.2-2 Values of W and F for AVHRR Channels 1 and 2
     * @param shipName
     * @param chanNum
     * @return spectral irradiance  W/m^2
     */
    private static double findSpectralIrrad(String shipName, int chanNum){
    	double f = 0.0;
    	if(TIROSN.equals(shipName)){
    		f =  TIROSN_F[chanNum -1];
    	}else if(NOAA6.equals(shipName)){
    		f =  NOAA6_F[chanNum -1];
    	}else if(NOAA7.equals(shipName)){
    		f =  NOAA7_F[chanNum -1];
    	}else if(NOAA8.equals(shipName)){
    		f =  NOAA8_F[chanNum -1];
    	}else if(NOAA9.equals(shipName)){
    		f =  NOAA9_F[chanNum -1];
    	}else if(NOAA10.equals(shipName)){
    		f =  NOAA10_F[chanNum -1];
    	}else if(NOAA11.equals(shipName)){
    		f =  NOAA11_F[chanNum -1];
    	}else if(NOAA12.equals(shipName)){
    		f =  NOAA12_F[chanNum -1];
    	}else if(NOAA13.equals(shipName)){
    		f =  NOAA13_F[chanNum -1];
    	}else if(NOAA14.equals(shipName)){
    		f =  NOAA14_F[chanNum -1];
    	}

    	return f;    	
    }	

    
    /**
     * Calculate percent albedo 
     * 
     * using slope & intercept provided in dataset instead of pre-launch values - is this correct???????
     * 
     * 3.3.2 Visible Channel Calibration 
     * equation 3.3.2-1 
     * @param shipName
     * @param chanNum
     * @param dr
     * @return return albedo
     */
	public static float[] calculateAlbedo(String shipName, int chanNum, IScanlineVer1 dr, int pixels){
		float[] vals = new float[pixels];
//		double[][] chanslope = {{0.1071,0.1051},{0.1071,0.1058},{0.1068,0.1069},{0.1060,0.1060},{0.1063,0.1075},{0.1059,0.1058},{0.0950,0.1061},{0.1042,0.1014}};
//		double[][] chanintercept = {{-3.9,-3.5},{-4.1,-3.5},{-3.4,-3.5},{-4.2,-4.2},{-3.8,-3.9},{-3.7,-3.6},{-3.8,-3.6},{-4.4,-4.0}};
		double slope=0;
		double intercept=0;
    	if(TIROSN.equals(shipName)){
    		slope =  TIROSN_SLOPE[chanNum -1];
    		intercept = TIROSN_INTERCEPT[chanNum -1];
    	}else if(NOAA6.equals(shipName)){
    		slope =  NOAA6_SLOPE[chanNum -1];
    		intercept = NOAA6_INTERCEPT[chanNum -1];
    	}else if(NOAA7.equals(shipName)){
    		slope =  NOAA7_SLOPE[chanNum -1];
    		intercept = NOAA7_INTERCEPT[chanNum -1];
    	}else if(NOAA8.equals(shipName)){
    		slope =  NOAA8_SLOPE[chanNum -1];
    		intercept = NOAA9_INTERCEPT[chanNum -1];
    	}else if(NOAA9.equals(shipName)){
    		slope  =  NOAA9_SLOPE[chanNum -1];
    		intercept = NOAA9_INTERCEPT[chanNum -1];
    	}else if(NOAA10.equals(shipName)){
    		slope =  NOAA10_SLOPE[chanNum -1];
    		intercept = NOAA10_INTERCEPT[chanNum -1];
    	}else if(NOAA11.equals(shipName)){	
    		slope =  NOAA11_SLOPE[chanNum -1];
    		intercept = NOAA11_INTERCEPT[chanNum -1];
    	}else if(NOAA12.equals(shipName)){
    		slope =  NOAA12_SLOPE[chanNum -1];
    		intercept = NOAA12_INTERCEPT[chanNum -1];
    	}else if(NOAA13.equals(shipName)){
    		slope =  NOAA13_SLOPE[chanNum -1];
    		intercept = NOAA13_INTERCEPT[chanNum -1];
    	}else if(NOAA14.equals(shipName)){
    		slope =  NOAA14_SLOPE[chanNum -1];
    		intercept = NOAA14_INTERCEPT[chanNum -1];
    	}
  
    	short data[][] = dr.getData();
		for(int i=0;i<pixels;i++){
			vals[i] = (float)(slope * data[chanNum-1][i] + intercept);
		}
		return vals;
	}
	
	
    /**
     * Calculate Radiance for visible Avhrr Channels (1 & 2)
     * 3.3.2 Visible Channel Calibration
     * equation 3.3.2-5
	 * @param shipId
	 * @param chanNum
	 * @param dr
     * @return spectral radiance  (W/(m^2 -micrometer -sr))
     */
    public static float[] calculateRadianceForVisible(String shipName, int chanNum, IScanlineVer1 dr, int pixels){
    	float[] vals = new float[pixels];
    	double radiance;
    	float[] albedo = calculateAlbedo(shipName, chanNum, dr,pixels);
		double w = findEquivWidth(shipName, chanNum);
		double f = findSpectralIrrad(shipName, chanNum);
 
		for(int i=0;i<pixels;i++){
    		radiance = albedo[i] * (f/(100.00 * Math.PI * w));
    		vals[i] =  (float)radiance;
    	}  	
    	return vals;
    }
}
