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
 * Computes radiances for the visual AVHRR channels 1, 2 and 3a.
 */
public class ReflectanceFactorCalibrator {
    private static final int[] OPERATIONAL_DATA_OFFSET = {0, 15, 30};

    private int operationalDataIndex;

    private double slope1;
    private double intercept1;
    private double slope2;
    private double intercept2;
    private int intersection;
    private boolean dataRequired;

    public ReflectanceFactorCalibrator(int slope1, int intercept1, int slope2, int intercept2, int intersection) {
    	this.slope1 = slope1 * 1E-7;
        this.intercept1 = intercept1 * 1E-6;
        this.slope2 = slope2 * 1E-7;
        this.intercept2 = intercept2 * 1E-6;
        this.intersection = intersection;
    }

    public float calibrate(int counts) {
        final double reflectanceFactor;
        if (counts <= intersection) {
            reflectanceFactor = counts * slope1 + intercept1;
        } else {
            reflectanceFactor = counts * slope2 + intercept2;
        }
        return (float) (reflectanceFactor);
    }
}






