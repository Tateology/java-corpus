package gov.noaa.ncdc.wct.morph;


public class MorphGeoFeaturePair {
	private String id;
	private double firstLat;
	private double firstLon;
	private double lastLat;
	private double lastLon;
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setFirstLat(double firstLat) {
		this.firstLat = firstLat;
	}
	public double getFirstLat() {
		return firstLat;
	}
	public void setFirstLon(double firstLon) {
		this.firstLon = firstLon;
	}
	public double getFirstLon() {
		return firstLon;
	}
	public void setLastLat(double lastLat) {
		this.lastLat = lastLat;
	}
	public double getLastLat() {
		return lastLat;
	}
	public void setLastLon(double lastLon) {
		this.lastLon = lastLon;
	}
	public double getLastLon() {
		return lastLon;
	}
}

