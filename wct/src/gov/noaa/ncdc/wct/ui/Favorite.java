package gov.noaa.ncdc.wct.ui;

import java.net.URL;

public class Favorite {

	private URL dataURL;
	private String displayString;
	
	
	
	public void setDataURL(URL dataURL) {
		this.dataURL = dataURL;
	}
	public URL getDataURL() {
		return dataURL;
	}
	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}
	public String getDisplayString() {
		return displayString;
	}
}
