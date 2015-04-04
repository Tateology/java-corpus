package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphVectorSource;

import java.util.HashMap;

import org.geotools.cv.SampleDimension;

public class MorphSettings {

	public static final String INFO_RADIAL_SWEEP = "RadialSweep";
	
	private MorphVectorSource morphVectorSource;
	private int numMorphSteps;
	private int swdiDateBuffer;
	private int swdiNumGridCells;
	private boolean drawMotionVectors;
	private SampleDimension sampleDimension;
	private HashMap<String, String> extraInfoMap = new HashMap<String, String>();
	
	public void setNumMorphSteps(int numMorphSteps) {
		this.numMorphSteps = numMorphSteps;
	}
	public int getNumMorphSteps() {
		return numMorphSteps;
	}
	public void setSwdiDateBuffer(int swdiDateBuffer) {
		this.swdiDateBuffer = swdiDateBuffer;
	}
	public int getSwdiDateBuffer() {
		return swdiDateBuffer;
	}
	public void setSwdiNumGridCells(int swdiNumGridCells) {
		this.swdiNumGridCells = swdiNumGridCells;
	}
	public int getSwdiNumGridCells() {
		return swdiNumGridCells;
	}
	public void setMorphVectorSource(MorphVectorSource morphVectorSource) {
		this.morphVectorSource = morphVectorSource;
	}
	public MorphVectorSource getMorphVectorSource() {
		return morphVectorSource;
	}
	public void setDrawMotionVectors(boolean drawMotionVectors) {
		this.drawMotionVectors = drawMotionVectors;
	}
	public boolean isDrawMotionVectors() {
		return drawMotionVectors;
	}
	public void setSampleDimension(SampleDimension sampleDimension) {
		this.sampleDimension = sampleDimension;
	}
	public SampleDimension getSampleDimension() {
		return sampleDimension;
	}
	public HashMap<String, String> getExtraInfoMap() {
		return extraInfoMap;
	}
	public void setExtraInfoMap(HashMap<String, String> extraInfoMap) {
		this.extraInfoMap = extraInfoMap;
	}
}
