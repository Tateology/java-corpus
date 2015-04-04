/**
 * 
 */
package gov.noaa.ncdc.wct.export.raster;

/**
 * @author steve.ansari
 *
 */
public class WCTRasterizer extends FeatureRasterizer implements WCTRaster {

    
    // hold metadata for grid
    private long dateInMillis = -999;
    private String longName = "";
    private String units = "";
    private String standardName = "";
    private String variableName = "value";

    
    
    
    
    /**
     * 
     */
    public WCTRasterizer() {
    }

    /**
     * @param noData
     */
    public WCTRasterizer(float noData) {
        super(noData);
    }

    /**
     * @param height
     * @param width
     */
    public WCTRasterizer(int height, int width) {
        super(height, width);
    }

    /**
     * @param height
     * @param width
     * @param noData
     */
    public WCTRasterizer(int height, int width, float noData) {
        super(height, width, noData);
    }

    
    
    
    
    
    
    
    
    
    
    
    /**
     *  Gets the longName attribute of the FeatureRasterizer object
     *
     * @return    The long name for the raster
     */
    public String getLongName() {
        return longName;
    }

    /**
     *  Gets the units attribute of the FeatureRasterizer object
     *
     * @return    The long name for the raster
     */
    public String getUnits() {
        return units;
    }

    /**
     *  Gets the longName attribute of the FeatureRasterizer object
     *
     * @return    The long name for the raster
     */
    public long getDateInMilliseconds() {
        return dateInMillis;
    }



    /**
     *  Sets the longName attribute of the FeatureRasterizer object
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /**
     *  Sets the units attribute of the FeatureRasterizer object
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     *  Sets the longName attribute of the FeatureRasterizer object
     */
    public void setDateInMilliseconds(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }


    /**
     *  Gets the native attribute of the FeatureRasterizer object
     *
     * @return    The native value
     */
    public boolean isNative() {
        return false;
    }



    @Override
    public String getStandardName() {
        return this.standardName;
    }


    @Override
    public String getVariableName() {
        return this.variableName;
    }


    @Override
    public void setStandardName(String standardName) {
        this.standardName = standardName;        
    }


    @Override
    public void setVariableName(String variable) {
        this.variableName = variable;        
    }

}
