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

public class BlackBodyTempCalibrator {


	private static final double C1 = 1.1910427E-5;

	private static final double C2 = 1.4387752;

	private double constant1;

	private double constant2;

	private double c2vc;

	private double c1vc3;

	public BlackBodyTempCalibrator(double constant1, double constant2, double vc) {
		this.constant1 = constant1 * 1E-5;
		this.constant2 = constant2 * 1E-6;
		vc = vc * 1E-3;
		this.c2vc = C2 * vc;
		this.c1vc3 = C1 * vc * vc * vc;
	}

	public float calibrate(float radiances) {
        double teStar = c2vc / (Math.log(1.0 + (c1vc3 / radiances)));
        double te = constant1 + constant2 * teStar;
		return (float) te;
	}
}