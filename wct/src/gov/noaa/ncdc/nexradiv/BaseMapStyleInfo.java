/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.nexradiv;

import java.awt.Color;

public class BaseMapStyleInfo {

	private int lineWidth;
	private Color lineColor;
	private Color fillColor;
	private double minScale;
	private double maxScale;

	public static final double NO_MIN_SCALE = 0;
	public static final double NO_MAX_SCALE = 1000000000;

	public BaseMapStyleInfo(int lineWidth, Color lineColor) {
		this(lineWidth, lineColor, null);
	}
	public BaseMapStyleInfo(double minScale, double maxScale, int lineWidth, Color lineColor) {
		this(minScale, maxScale, lineWidth, lineColor, null);
	}
	public BaseMapStyleInfo(int lineWidth, Color lineColor, Color fillColor) {
		this(NO_MIN_SCALE, NO_MAX_SCALE, lineWidth, lineColor, fillColor);
	}   
	public BaseMapStyleInfo(double minScale, double maxScale, int lineWidth, Color lineColor, Color fillColor) {
		this.minScale = minScale;
		this.maxScale = maxScale;
		this.lineWidth = lineWidth;
		this.lineColor = lineColor;
		this.fillColor = fillColor;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}
	public double getMinScale() {
		return minScale;
	}
	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}
	public double getMaxScale() {
		return maxScale;
	}


}
