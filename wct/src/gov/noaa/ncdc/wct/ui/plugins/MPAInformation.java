package gov.noaa.ncdc.wct.ui.plugins;

import java.util.ArrayList;

// ==========================================================================================================
// MPAInformation - Marine Protected Area Information
// ==========================================================================================================
class MPAInformation implements Comparable<MPAInformation> {

	String mpa_Name;

	ArrayList<String> mpa_VariableName   = new ArrayList<String>();
	ArrayList<String> mpa_VariableSource = new ArrayList<String>();
	ArrayList<String> mpa_VariableSourceSize = new ArrayList<String>();
	ArrayList<String> mpa_VariableDescription = new ArrayList<String>();

	public MPAInformation() { }
	
	public void addMPA_VariableName(String string) {
		mpa_VariableName.add(string);
	}

	public void addMPA_VariableSource(String string) {
		mpa_VariableSource.add(string);
	}
	
	public void addMPA_VariableSourceSize(String string) {
		mpa_VariableSourceSize.add(string);
	}
	
	public void addMPA_VariableDescription(String string) {
		mpa_VariableDescription.add(string);
	}
	
	public void insert(String name, String source, String size, String description) {
		mpa_VariableName.add(name);
		mpa_VariableSource.add(source);		
		mpa_VariableSourceSize.add(size);
		mpa_VariableDescription.add(description);

	}
	
	public String getMPA_Name() {
		return mpa_Name;
	}

	public void setMPA_Name(String mPA_Name) {
		mpa_Name = mPA_Name;
	}

	public ArrayList<String> getMPA_VariableName() {
		return mpa_VariableName;
	}

	public void setMPA_VariableName(ArrayList<String> mPA_VariableName) {
		mpa_VariableName = mPA_VariableName;
	}

	public ArrayList<String> getMPA_VariableSource() {
		return mpa_VariableSource;
	}

	public void setMPA_VariableSource(ArrayList<String> mPA_VariableSource) {
		mpa_VariableSource = mPA_VariableSource;
	}
	
	public ArrayList<String> getMPA_VariableSourceSize() {
		return mpa_VariableSourceSize;
	}
	
	public ArrayList<String> getMPA_VariableDescription() {
		return mpa_VariableDescription;
	}

	public void setMPA_VariableSourceSize(ArrayList<String> mPA_VariableSourceSize) {
		mpa_VariableSourceSize = mPA_VariableSourceSize;
	}

	public void setMPA_VariableDescription(ArrayList<String> mPA_VariableDescription) {
		mpa_VariableDescription = mPA_VariableDescription;
	}

	@Override
	public int compareTo(MPAInformation mpa) {			
		   return this.getMPA_Name().compareTo(mpa.getMPA_Name()); 
	}
}
