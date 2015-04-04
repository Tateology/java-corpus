package gov.noaa.ncdc.wct.io;

import java.net.URL;

/**
 *  A simple object that contains info on filetype, timestamp, etc...
 *
 * @author    steve.ansari
 */
public class ScanResults {

    private String fileName;

    private SupportedDataType dataType;

    private String displayName;

    private String extension;

    private String sourceID;
    
    private String productID;

    private String timestamp;
    
    private String longName;
    
    private String description;

    private URL url;

    /**
     * Returns the value of fileName.
     *
     * @return    The fileName value
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of fileName.
     *
     * @param  fileName  The value to assign fileName.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the value of dataType.
     *
     * @return    The dataType value
     */
    public SupportedDataType getDataType() {
        return dataType;
    }

    /**
     * Sets the value of dataType.
     *
     * @param  dataType  The value to assign fileType.
     */
    public void setDataType(SupportedDataType dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the value of displayName.
     *
     * @return    The displayName value
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of displayName.
     *
     * @param  displayName  The value to assign displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the value of extension.
     *
     * @return    The extension value
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Sets the value of extension.
     *
     * @param  extension  The value to assign extension.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the value of productID.
     *
     * @return    The productID value
     */
    public String getProductID() {
        return productID;
    }

    /**
     * Sets the value of productID.
     *
     * @param  productID  The value to assign productID.
     */
    public void setProductID(String productID) {
        this.productID = productID;
    }

    /**
     *  Gets the timestamp for this file <br>
     *  <b> Level-2: </b> ex) KCCX_20040617_124506<br>
     *  timestamp = 20040617 12:45:06 = YYYYMMDD HH:MM:SS (date hour:min:sec)<br><br>
     *  <b> Level-3: </b> ex) KCCX_N0R_20040617_1245 <br>
     *  timestamp = 20040617 12:45 = YYYYMMDD HH:MM (date hour:min)<br><br>
     *
     * @return    The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of timestamp.
     *
     * @param  timestamp  The value to assign timestamp.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the long name for this file
     * @return
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Set the long name of this file
     * @param longName
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

 
    
    

    /**
     * Returns the value of url.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the value of url.
     * @param url The value to assign url.
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        return dataType+": "+getFileName()+" - '"+ getDisplayName() + "' || ext=" + getExtension() + " || productID=" + getProductID()
                + " || timestamp=" + getTimestamp() +" || longname='"+getLongName()+"'";
    }
}
