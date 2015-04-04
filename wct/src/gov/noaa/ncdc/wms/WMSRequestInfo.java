package gov.noaa.ncdc.wms;

public class WMSRequestInfo {

    private String name;
    private String index;
    private String capabilitiesURL;
    private String defaultLayers;
    private String imageType;
    private String transparent;
    private String version;
    private String info;
    
    
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }   
    public void setIndex(String index) {
        this.index = index;
    }
    public String getIndex() {
        return index;
    }
    public void setCapabilitiesURL(String capabilitiesURL) {
        this.capabilitiesURL = capabilitiesURL;
    }
    public String getCapabilitiesURL() {
        return capabilitiesURL;
    }
    public void setDefaultLayers(String defaultLayers) {
        this.defaultLayers = defaultLayers;
    }
    public String getDefaultLayers() {
        return defaultLayers;
    }
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    public String getImageType() {
        return imageType;
    }
    public void setTransparent(String transparent) {
        this.transparent = transparent;
    }
    public String getTransparent() {
        return transparent;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public String getInfo() {
        return info;
    }
    public String toString() {
        return this.name+": "+this.capabilitiesURL+" , "+this.defaultLayers+" , "+this.imageType+" , "+this.transparent+" , "+this.version;
    }

}
