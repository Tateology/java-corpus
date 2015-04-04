package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;

import org.geotools.cv.SampleDimension;

public interface Viewer {

	/**
	 * Returns the current RadialDatasetSweepRemappedRaster object in use for decoding/resampling 
	 * Radial data.  This allows the display the range/aziumuth/height on the status bar.  Return null
	 * if the implementing viewer doesn't support Radial data.
	 * @return
	 */
	public RadialDatasetSweepRemappedRaster getRadialRemappedRaster();
	
	
	
	public SampleDimension getSampleDimension();
	
}
