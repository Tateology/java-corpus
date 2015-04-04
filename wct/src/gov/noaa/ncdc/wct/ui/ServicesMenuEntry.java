package gov.noaa.ncdc.wct.ui;

public class ServicesMenuEntry {

	public enum LocationType { REMOTE_DIR, THREDDS_CATALOG, DIRECT_URL, UNKNOWN };
	
	private String id;
	private String name;
	private String location;
	private LocationType locationType;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public LocationType getLocationType() {
		return locationType;
	}
	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
