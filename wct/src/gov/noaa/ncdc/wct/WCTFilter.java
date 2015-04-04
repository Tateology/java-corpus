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

package gov.noaa.ncdc.wct;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class WCTFilter implements Serializable {

    public static final double NO_MIN_DISTANCE = 0.0;
    public static final double NO_MAX_DISTANCE = 1000.0;
    public static final double NO_MIN_VALUE = Double.NEGATIVE_INFINITY;
    public static final double NO_MAX_VALUE = Double.POSITIVE_INFINITY;
    public static final double NO_MIN_AZIMUTH = 0.0;
    public static final double NO_MAX_AZIMUTH = 10000.0;
    public static final double NO_MIN_HEIGHT = -999999.0;
    public static final double NO_MAX_HEIGHT = 10000000.0;


    private double minDistance = NO_MIN_DISTANCE;
    private double maxDistance = NO_MAX_DISTANCE;
    private double[] minValue = new double[] { NO_MIN_VALUE };
    private double[] maxValue = new double[] { NO_MAX_VALUE };
    private double minAzimuth = NO_MIN_AZIMUTH;
    private double maxAzimuth = NO_MAX_AZIMUTH;
    private double minHeight = NO_MIN_HEIGHT;
    private double maxHeight = NO_MAX_HEIGHT;
    private int[] valueIndices;
    private String[] categoryOverrides = new String[]{""};

    private java.awt.geom.Rectangle2D.Double extentFilter;
    //private Coordinate centerCoordinate;

    private Geometry extentGeometry, rangeGeometry;
    private GeometryFactory geoFactory = new GeometryFactory();

    public WCTFilter() {
    }

    // distance in km
    public WCTFilter(double minDistance, double maxDistance, 
            double minValue, double maxValue,
            double minAzimuth, double maxAzimuth,
            double minHeight, double maxHeight,
            java.awt.geom.Rectangle2D.Double extentFilter                       
    ) {

        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.minValue[0] = minValue;
        this.maxValue[0] = maxValue;
        this.minAzimuth = minAzimuth;
        this.maxAzimuth = maxAzimuth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        setExtentFilter(extentFilter);
    }



    // distance in km
    public WCTFilter(double minDistance, double maxDistance, 
            double[] minValue, double[] maxValue,
            double minAzimuth, double maxAzimuth,
            double minHeight, double maxHeight,
            java.awt.geom.Rectangle2D.Double extentFilter                       
    ) {

        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minAzimuth = minAzimuth;
        this.maxAzimuth = maxAzimuth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        setExtentFilter(extentFilter);
    }


    public void setDistanceRange(double minDistance, double maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public void setValueRange(double minValue, double maxValue) {
        this.minValue[0] = minValue;
        this.maxValue[0] = maxValue;
    }

    public void setValueRange(double[] minValue, double[] maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public void setHeightRange(double minHeight, double maxHeight) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public void setAzimuthRange(double minAzimuth, double maxAzimuth) {
        this.minAzimuth = minAzimuth;
        this.maxAzimuth = maxAzimuth;
        if (minAzimuth < 0) {
            minAzimuth += 360;
        }
        if (maxAzimuth < 0) {
            maxAzimuth += 360;
        }
        if (minAzimuth > 360) {
            minAzimuth = minAzimuth % 360;
        }
        if (maxAzimuth > 360) {
            maxAzimuth = maxAzimuth % 360;
        }
        if (minAzimuth > maxAzimuth) {
            double work = minAzimuth;
            minAzimuth = maxAzimuth;
            maxAzimuth = minAzimuth;
        }
    }



    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void setMinValue(double minValue) {
        this.minValue[0] = minValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue[0] = maxValue;
    }

    public void setMinValue(double[] minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(double[] maxValue) {
        this.maxValue = maxValue;
    }


    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double[] getMinValue() {
        return minValue;
    }

    public double[] getMaxValue() {
        return maxValue;
    }

    public double getMinHeight() {
        return minHeight;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setValueIndices(int[] valueIndices) {
        this.valueIndices = valueIndices;
    }

    public int[] getValueIndices() {
        return valueIndices;
    }
    
    public double getMinAzimuth() {
    	return minAzimuth;    	
    }
    
    public double getMaxAzimuth() {
    	return maxAzimuth;
    }
    

    public void setExtentFilter(java.awt.geom.Rectangle2D.Double extentFilter) {
        this.extentFilter = extentFilter;

        if (extentFilter != null) {
            // Create extent geometry  
            Envelope env = new Envelope(
                    extentFilter.getX(), 
                    extentFilter.getX() + extentFilter.getWidth(),
                    extentFilter.getY(),
                    extentFilter.getY() + extentFilter.getHeight()
            );

            extentGeometry = geoFactory.toGeometry(env);

        }
        else {
            extentGeometry = null;
        }
    }
    public java.awt.geom.Rectangle2D.Double getExtentFilter() {
        return extentFilter;
    }


    public boolean accept(Geometry poly, double dvalue, double azimuth) {      
        // Check if input geometry is within the extentFilter
        if (extentFilter != null && ! poly.intersects(extentGeometry)) {
            return false;
        }
        // Check if value is within min and max value
        if (! accept(dvalue)) {
            return false;
        }
        // Convert to positive azimuth
        if (azimuth < 0) {
            azimuth +=360.0;
        }
        // Check if azimuth is within min and max azimuth
        if (azimuth < minAzimuth || azimuth > maxAzimuth) {
            return false;
        }
        // Check if geometry intersects rangeGeometry
        if (rangeGeometry != null && ! poly.intersects(rangeGeometry)) {
            return false;
        }



        return true;
    }

    public boolean accept(Geometry poly, double dvalue, double azimuth, double height) {      
        // Check if height is within min and max distance height
        if (height < minHeight || height > maxHeight) {
            return false;
        }
        // Check other values
        if (! accept(poly, dvalue, azimuth)) {
            return false;
        }
        return true;
    }

    public boolean accept(Geometry poly, double dvalue, double azimuth, double range, double height) {      
        // Check if startrange is within min and max distance range
        if (range < minDistance || range > maxDistance) {
            return false;
        }
        // Check other values
        if (! accept(poly, dvalue, azimuth, height)) {
            return false;
        }
        return true;
    }

    public boolean accept(double dvalue, double azimuth, double range, double height) {      
        // Check if startrange is within min and max distance range
        if (range < minDistance || range > maxDistance) {
            return false;
        }

        
//        System.out.println(minHeight+" , "+maxHeight+" -- "+height+" - "+range);

        // Check if height is within min and max distance height
        if (height < minHeight || height > maxHeight) {
            return false;
        }
        // Check if value is within min and max value
        if (! accept(dvalue)) {
            return false;
        }
        // Convert to positive azimuth
        if (azimuth < 0) {
            azimuth +=360.0;
        }
        // Check if azimuth is within min and max azimuth
        if (azimuth < minAzimuth || azimuth > maxAzimuth) {
            return false;
        }
        return true;
    }


    

    public boolean accept(double dvalue) {      
        // Check if value is within min and max value
        int len = ( minValue.length < maxValue.length ) ? minValue.length : maxValue.length;
        boolean acceptVal = false;
        for (int n=0; n<len; n++) {
            if (dvalue >= minValue[n] && dvalue <= maxValue[n]) {
                acceptVal = true;
            }
        }         
        return acceptVal;
    }


    public boolean accept(double dvalue, double range) {      
        // Check if value is within min and max value
        if (! accept(dvalue)) {
            return false;
        }
        // Check if startrange is within min and max distance range
        if (range < minDistance || range > maxDistance) {
            return false;
        }

        return true;
    }

    public boolean accept(Geometry poly) {      
        // Check if input geometry intersects the extentFilter
        try {
            if (extentFilter != null && ! poly.intersects(extentGeometry)) {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }
    /**
     * Clips input poly to extent filter
     */
    public Geometry clipToExtentFilter(Geometry poly) {


        try {
            if (extentFilter != null) {
                return poly.intersection(extentGeometry);
            }
            else {
                return poly;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return poly;
        }
    }

    /**
     * Clips input poly to range filter
     */
    /*
   public Geometry clipToRangeFilter(Geometry poly) {


      try {
         poly = poly.intersection(rangeGeometry);
         //poly = poly.difference(minRangeGeometry);
         return poly;
      } catch (Exception e) {
         return poly;
      }
   }
     */


    /**
     * For Level-III filtering, category values are compared to max and min value pairs supplied in filter.  For 
     * non-numeric categories (such as 'RF'), the categoryOverrides is used to include that
     * category.  By default, non-numeric categories are ignored.
     */
    public void setCategoryOverrides(String[] categoryOverrides) {
        this.categoryOverrides = categoryOverrides;
    }

    public String[] getCategoryOverrides() {
        return this.categoryOverrides;
    }

    
    /**
     * Takes category values and compares to max and min value pairs supplied in filter.  For 
     * non-numeric categories (such as 'RF'), the categoryOverrides is used to include that
     * category.  By default, non-numeric categories are ignored.
     */
    public void setValueIndices(String[] categoriesFromHeader) throws Exception {

        if (categoriesFromHeader == null) {
            return;
        }
        
//        System.out.println(Arrays.toString(categoriesFromHeader));

//      System.out.println("CALCULATING CATEGORY FILTERING: "+
//      "\n\t VALUES: \t"+printValueRanges()+
//      "\n\t CAT. OVERRIDES: \t"+Arrays.deepToString(categoryOverrides));

        Pattern pattern = Pattern.compile("[a-zA-Z]");
        

        int len = ( minValue.length < maxValue.length ) ? minValue.length : maxValue.length;

        //  Walk through all categories & turn on if match
        boolean[] printCat = new boolean[categoriesFromHeader.length];

        for (int n = 0; n < categoriesFromHeader.length; n++) {

            if (categoriesFromHeader[n] != null && categoriesFromHeader[n].trim().length() > 0) {
                categoriesFromHeader[n] = categoriesFromHeader[n].replaceAll(">", "").replaceAll("<", "");

//                System.out.println("categories[" + n + "] = " + categoriesFromHeader[n]);  //  DEBUG

                //    ----  NON-NUMERIC CATEGORIES
                if (categoriesFromHeader[n].equalsIgnoreCase("RF")) {
                    for (int i = 0; i < categoryOverrides.length; i++) {
                        if (categoryOverrides[i].trim().toUpperCase().equals("RF")) {
                            printCat[n] = true;

                        }
                    }
                } //  end if RF
                else if (categoriesFromHeader[n].equalsIgnoreCase("TH")) {
                    for (int i = 0; i < categoryOverrides.length; i++) {
                        if (categoryOverrides[i].trim().toUpperCase().equals("TH")) {
                            printCat[n] = true;

                        }
                    }
                } //  end if TH
                else if (categoriesFromHeader[n].equalsIgnoreCase("ND")) {
                    for (int i = 0; i < categoryOverrides.length; i++) {
                        if (categoryOverrides[i].trim().toUpperCase().equals("ND")) {
                            printCat[n] = true;

                        }
                    }
                } //  end if ND
                else if (pattern.matcher(categoriesFromHeader[n]).find()) {
                    printCat[n] = true;
                }
                //    ----  end non-numeric categories    

                //  --  CHECK VALUE RANGES  --

                /*  ------------------------------------------------------------
               Values stored in categories for some products have "+" and "-"
               in front of them.  Need to strip those off to be able to 
               convert from string to integer
               ------------------------------------------------------------  */

                else {
                    //  Need to check for +/- in string:  require special handling
                    double value;
                    String tempCat; //  to hold adjusted value

                    for (int pair = 0; pair < len; pair++) {
                        if (categoriesFromHeader[n].charAt(0) == '-') {
                            //  strip off the "- "
                            tempCat = categoriesFromHeader[n].substring(2);
                            //  convert to a number & make it negative
                            value = Double.parseDouble(tempCat) * (-1);

                        }
                        else if (categoriesFromHeader[n].charAt(0) == '+') {
                            //  strip off the "+ "
                            tempCat = categoriesFromHeader[n].substring(2);
                            //  convert it to a number
                            value = Double.parseDouble(tempCat);
                        }
                        else //  zero has no sign
                        {
                            value = Double.parseDouble(categoriesFromHeader[n]);
                        }

                        //System.out.println("Value in cat " + n + " = " + value);  //  DEBUG

                        if (value >= minValue[pair] && value <= maxValue[pair]) {

                            printCat[n] = true;
                        }


                    } //  end for all pairs
                } //  end else (+/-)
                //  --  end check value ranges

            }

        } //  for all categories

        //  Assemble an integer array of indices for the filter to print
        int[] catsInFilter;
        int trueCount = 0; //  how many categories to print

        for (int j = 0; j < printCat.length; j++) {
            //System.out.println("\n " + printCat[j]);  //  DEBUG
            if (printCat[j] == true) {
                trueCount++; //  how big of an array will we need?
            }

            //System.out.println("trueCount:  " + trueCount);  //  DEBUG

        } //  for printCat

        catsInFilter = new int[trueCount];
        int k = 0;
        for (int j = 0; j < printCat.length; j++) {
            //System.out.println("\n " + printCat[j]);  //  DEBUG
            if (printCat[j] == true) {
                catsInFilter[k] = j;
                //System.out.println("j:  " + j + "\t cats-k:  " + catsInFilter[k]);  //  DEBUG
                k++;
            }

        } //  for printCat

        /*  ---------------  DEBUG ------------------
       for(int j = 0; j < catsInFilter.length; j++)
       {
           System.out.println("\n " + catsInFilter[j]);

       }  //  for catsInFilter
       //  ------------------------------------------  */

//      return catsInFilter; //  send filter to decoder





//      System.out.println("CALCULATED CATEGORIES TO INCLUDE: ");
//      for (int n=0; n<categoriesFromHeader.length; n++) {
//      System.out.println("\t"+categoriesFromHeader[n]+" \t --- "+printCat[n]);
//      }


        setValueIndices(catsInFilter);
    }


    private String printValueRanges() {
        int len = ( minValue.length < maxValue.length ) ? minValue.length : maxValue.length;
        DecimalFormat fmt3 = new DecimalFormat("0.000");
        String str = "VALUE[";
        for (int n=0; n<len; n++) {
            str += fmt3.format(minValue[n])+" to "+fmt3.format(maxValue[n]);
            if (n<len-1) {
                str += ",";
            }
        }
        str += "] ";
        return str;
    }

    public String toString() {
        return "WCT FILTER: \n\t"+printValueRanges()+"\n\tDIST["+minDistance+" to "+maxDistance+
        "]\n\tAZIMUTH["+minAzimuth+" to "+maxAzimuth+"]\n\tHEIGHT["+minHeight+" to "+maxHeight+
        "]\n\t EXTENT["+extentFilter+"]\n\tCATEGORY OVERRIDES: "+Arrays.deepToString(categoryOverrides);
    }
}
