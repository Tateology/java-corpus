package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.wct.WCTException;

import org.geotools.cv.SampleDimension;

public class SampleDimensionAndLabels {

	private SampleDimension sampleDimension;
	private String[] labels;

	
	public SampleDimensionAndLabels(SampleDimension sampleDimension, String[] labels) throws WCTException {
		this.sampleDimension = sampleDimension;
		this.labels = labels;
		
//		if (sampleDimension.getCategories().size() != labels.length-1) {
//			throw new WCTException("The number of catgories in the sample dimension must" +
//					" be one less than the number of labels in the label array");
//		}
	}
	
	
	public SampleDimension getSampleDimension() {
		return sampleDimension;
	}
	public String[] getLabels() {
		return labels;
	}
}
