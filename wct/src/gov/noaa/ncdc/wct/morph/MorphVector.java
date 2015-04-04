package gov.noaa.ncdc.wct.morph;

public class MorphVector {
	
	private double lat;
	private double lon;
	private double directionAngle;
	private double magnitude;
	
	
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLat() {
		return lat;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLon() {
		return lon;
	}
	public void setDirectionAngle(double directionAngle) {
		this.directionAngle = directionAngle;
	}
	public double getDirectionAngle() {
		return directionAngle;
	}
	/**
	 * Use units of m/s
	 * @param magnitude
	 */
	public void setSpeed(double magnitude) {
		this.magnitude = magnitude;
	}
	/**
	 * Should be in units of m/s
	 * @return
	 */
	public double getSpeed() {
		return magnitude;
	}
	
	
	public String toString() {
		return lat+","+lon+" ,   "+directionAngle+" deg , "+magnitude+" m/s";
	}
}


