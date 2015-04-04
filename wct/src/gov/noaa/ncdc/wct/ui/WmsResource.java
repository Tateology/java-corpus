package gov.noaa.ncdc.wct.ui;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.net.URL;

public interface WmsResource {

    public URL getWmsUrl(Rectangle2D bounds) throws Exception;
    public String getName();
    public float getZIndex();
    public Image getLegendImage() throws Exception;
    public String getInfo();
    public Image getResourceLogo();
}
