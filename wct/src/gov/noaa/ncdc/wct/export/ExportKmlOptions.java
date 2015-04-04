package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.ui.AbstractKmzUtilities.AltitudeMode;

import java.net.URL;

public class ExportKmlOptions {

	private AltitudeMode altMode;
	private int elevationExaggeration;
	private URL paletteURL;
	private double[] minMaxOverride = new double[2];
	private URL logoURL;
	private String legendLocationKML;
	private String logoLocationKML;
	private boolean drawColorMapTransparentBackgroundPattern;
	
	public void setAltMode(AltitudeMode altMode) {
		this.altMode = altMode;
	}
	public AltitudeMode getAltMode() {
		return altMode;
	}
	public void setElevationExaggeration(int elevationExaggeration) {
		this.elevationExaggeration = elevationExaggeration;
	}
	public int getElevationExaggeration() {
		return elevationExaggeration;
	}
	public void setPaletteURL(URL paletteURL) {
		this.paletteURL = paletteURL;
	}
	public URL getPaletteURL() {
		return paletteURL;
	}
	public void setMinMaxOverride(double[] minMaxOverride) {
		this.minMaxOverride = minMaxOverride;
	}
	public double[] getMinMaxOverride() {
		return minMaxOverride;
	}
	public void setLogoURL(URL logoURL) {
		this.logoURL = logoURL;
	}
	public URL getLogoURL() {
		return logoURL;
	}
	public String getLegendLocationKML() {
		return legendLocationKML;
	}
	public void setLegendLocationKML(String legendLocationKML) {
		this.legendLocationKML = legendLocationKML;
	}
	public String getLogoLocationKML() {
		return logoLocationKML;
	}
	public void setLogoLocationKML(String logoLocationKML) {
		this.logoLocationKML = logoLocationKML;
	}
	public boolean isDrawColorMapTransparentBackgroundPattern() {
		return drawColorMapTransparentBackgroundPattern;
	}
	public void setDrawColorMapTransparentBackgroundPattern(
			boolean drawColorMapTransparentBackgroundPattern) {
		this.drawColorMapTransparentBackgroundPattern = drawColorMapTransparentBackgroundPattern;
	}
	
	
}
