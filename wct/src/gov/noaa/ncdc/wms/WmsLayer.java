package gov.noaa.ncdc.wms;

import java.awt.Color;

import org.geotools.renderer.j2d.RenderedGridCoverage;

public class WmsLayer {

    private RenderedGridCoverage rgc;
    private int alpha;
    private Color emptyBackgroundColor;
    
    
    public WmsLayer(RenderedGridCoverage rgc) {
        this.rgc = rgc;
        this.alpha = 255;
        this.emptyBackgroundColor = null;
    }
    
    
    
    
    public RenderedGridCoverage getRenderedGridCoverage() {
        return rgc;
    }
    public void setRenderedGridCoverage(RenderedGridCoverage rgc) {
        this.rgc = rgc;
    }
    public int getAlpha() {
        return alpha;
    }
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    public Color getEmptyBackgroundColor() {
        return emptyBackgroundColor;
    }
    public void setEmptyBackgroundColor(Color emptyBackgroundColor) {
        this.emptyBackgroundColor = emptyBackgroundColor;
    }
}
