package gov.noaa.ncdc.wct.ui;

import java.io.Serializable;
import java.text.DecimalFormat;


public class Bookmark implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6893824592441621216L;

	public final static DecimalFormat FMT = new DecimalFormat("0.000");
	
	private String title;
	private java.awt.geom.Rectangle2D.Double extent;

	
	public Bookmark() {
	}
	
	public Bookmark(String title, java.awt.geom.Rectangle2D.Double extent) {
		this.title = title;
		this.extent = extent;
	}
	
	
	public java.awt.geom.Rectangle2D.Double getExtent() {
		return extent;
	}

	public void setExtent(java.awt.geom.Rectangle2D.Double extent) {
		this.extent = extent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString() {
		String bounds = "("+FMT.format(extent.getMinX())+","+FMT.format(extent.getMinY())
		+" "+FMT.format(extent.getMaxX())+","+FMT.format(extent.getMaxY())+")";
		return title+"  "+bounds;
	}
}
