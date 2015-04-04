package gov.noaa.ncdc.iosp.avhrr.util.formats;

import java.util.List;

import ucar.nc2.Attribute;

public interface IConvertableFile {
	
	public String[] getDefaultVariables();
	
	public String[] getQualityVariables();
	
	public String[] getCalibrationVariables();
	
	public String[] getLatLonVariables();
	
	public String[] getMetadataVariables();
	
	public String getChan1RawVariable();
	
	public String getChan2RawVariable();
	
	public String[] getChan3RawVariable();
	
	public String getChan4RawVariable();
	
	public String getChan5RawVariable();
	
	public String getChan1RadVariable();
	
	public String getChan2RadVariable();
	
	public String[] getChan3RadVariable();
	
	public String getChan4RadVariable();
	
	public String getChan5RadVariable();
	
	public String getChan1CalVariable();
	
	public String getChan2CalVariable();
	
	public String[] getChan3CalVariable();
	
	public String getChan4CalVariable();
	
	public String getChan5CalVariable();
	
	public List<Attribute> getGlobalAttributes();
}
