package gov.noaa.ncdc.wct.unidata.contour;

public interface StreamingContourProcess {
	
	public void processContourFeature(ContourFeature cf);
	public void finish();
}
