package gov.noaa.ncdc.gis.kml;

import java.awt.Desktop;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.geotools.cs.GeodeticCalculator;

public class TourGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Rectangle2D.Double extent = new Rectangle2D.Double(-94.581, 37.025, 0.12, 0.1);
		String tourKML = generateTourKML(extent);
		System.out.println(tourKML);

		try {
			File outfile = new File("E:\\work\\isosurface\\tour.kml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
			bw.write(getKMLStart());
			bw.write(tourKML);
			bw.write(getKMLEnd());
			bw.close();
			
			Desktop.getDesktop().open(outfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public static String generateTourKML(Rectangle2D.Double extent) {
		
		GeodeticCalculator gcalc = new GeodeticCalculator();
		gcalc.setAnchorPoint(extent.getCenterX(), extent.getMaxY());
		gcalc.setDestinationPoint(extent.getCenterX(), extent.getMinY());
		double heightInMeters = gcalc.getOrthodromicDistance();
		gcalc.setAnchorPoint(extent.getMinX(), extent.getCenterY());
		gcalc.setDestinationPoint(extent.getMaxX(), extent.getCenterY());
		double widthInMeters = gcalc.getOrthodromicDistance();
		
		
//		System.out.println(heightInMeters+" , "+widthInMeters);
		
		
		
		
		
		StringBuilder sb = new StringBuilder();
		
		
		sb.append("  <gx:Tour>                                   \n");
		sb.append("    <name>Double-click to tour</name>  \n");
		sb.append("    <gx:Playlist> \n");


		// initial flyto
//		sb.append("      <gx:FlyTo>                           \n");
//		sb.append("        <gx:duration>3.0</gx:duration>     \n");
//		sb.append("        <LookAt>                           \n");
//		sb.append("          <longitude>"+extent.getCenterX()+"</longitude> \n");
//		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>   \n");
//		sb.append("          <altitude>1000</altitude>        \n");
//		sb.append("          <heading>0</heading>             \n");
//		sb.append("          <tilt>78.065</tilt>              \n");
//		sb.append("          <range>"+widthInMeters*5+"</range> \n");
//		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
//		sb.append("        </LookAt>                                      \n");
//		sb.append("      </gx:FlyTo>                                      \n");

//		sb.append("      <gx:Wait>                                       \n");
//		sb.append("       	<gx:duration>6.0</gx:duration>  \n");
//		sb.append("      </gx:Wait>                                       \n");

		
		int numRotationStops = 5;
		for (int n=0; n<numRotationStops+1; n++) {
		
			sb.append("      <gx:FlyTo>                                       \n");
			sb.append("        <gx:duration>3.0</gx:duration>                 \n");
			sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
			sb.append("        <LookAt>                                       \n");
			sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
			sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
			sb.append("          <altitude>1000</altitude>                       \n");
			sb.append("          <heading>"+(n*360.0/numRotationStops)+"</heading>             \n");
			sb.append("          <tilt>78.065</tilt>                          \n");
			sb.append("          <range>"+widthInMeters*4+"</range> \n");
			sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
			sb.append("        </LookAt>                                      \n");
			sb.append("      </gx:FlyTo>                                      \n");

//			sb.append("      <gx:Wait>                                       \n");
//			sb.append("       	<gx:duration>1.0</gx:duration>  \n");
//			sb.append("      </gx:Wait>                                       \n");

		}

		
		
		
		// initial flyto
//		sb.append("      <gx:FlyTo>                                       \n");
//		sb.append("        <gx:duration>5.0</gx:duration>                 \n");
//		sb.append("        <LookAt>                                       \n");
//		sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
//		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
//		sb.append("          <altitude>1000</altitude>                       \n");
//		sb.append("          <heading>0</heading>             \n");
//		sb.append("          <tilt>78.065</tilt>                          \n");
//		sb.append("          <range>"+widthInMeters*3+"</range> \n");
//		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
//		sb.append("        </LookAt>                                      \n");
//		sb.append("      </gx:FlyTo>                                      \n");

		for (int n=0; n<numRotationStops+1; n++) {
			
			sb.append("      <gx:FlyTo>                                       \n");
			sb.append("        <gx:duration>5.0</gx:duration>                 \n");
			sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
			sb.append("        <LookAt>                                       \n");
			sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
			sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
			sb.append("          <altitude>1000</altitude>                       \n");
			sb.append("          <heading>"+(n*-360.0/numRotationStops)+"</heading>             \n");
			sb.append("          <tilt>78.065</tilt>                          \n");
			sb.append("          <range>"+widthInMeters*2+"</range> \n");
			sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
			sb.append("        </LookAt>                                      \n");
			sb.append("      </gx:FlyTo>                                      \n");

//			sb.append("      <gx:Wait>                                       \n");
//			sb.append("       	<gx:duration>0.7</gx:duration>  \n");
//			sb.append("      </gx:Wait>                                       \n");

		}


		sb.append("      <gx:FlyTo>                                       \n");
		sb.append("        <gx:duration>5.0</gx:duration>                 \n");
		sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
		sb.append("        <LookAt>                                       \n");
		sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
		sb.append("          <altitude>1000</altitude>  \n");
		sb.append("          <heading>270.0</heading>   \n");
		sb.append("          <tilt>78.065</tilt>        \n");
		sb.append("          <range>"+widthInMeters*2+"</range> \n");
		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
		sb.append("        </LookAt>                                      \n");
		sb.append("      </gx:FlyTo>                                      \n");
		
		sb.append("      <gx:FlyTo>                                       \n");
		sb.append("        <gx:duration>5.0</gx:duration>                 \n");
		sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
		sb.append("        <LookAt>                                       \n");
		sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
		sb.append("          <altitude>100</altitude>  \n");
		sb.append("          <heading>270.0</heading>   \n");
		sb.append("          <tilt>68.065</tilt>        \n");
		sb.append("          <range>"+widthInMeters+"</range> \n");
		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
		sb.append("        </LookAt>                                      \n");
		sb.append("      </gx:FlyTo>                                      \n");
		
		sb.append("      <gx:Wait>                                       \n");
		sb.append("       	<gx:duration>1.5</gx:duration>  \n");
		sb.append("      </gx:Wait>                                       \n");

		sb.append("      <gx:FlyTo>                                       \n");
		sb.append("        <gx:duration>5.0</gx:duration>                 \n");
		sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
		sb.append("        <LookAt>                                       \n");
		sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
		sb.append("          <altitude>100</altitude>  \n");
		sb.append("          <heading>270.0</heading>   \n");
		sb.append("          <tilt>88.065</tilt>        \n");
		sb.append("          <range>"+widthInMeters*3+"</range> \n");
		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
		sb.append("        </LookAt>                                      \n");
		sb.append("      </gx:FlyTo>                                      \n");


		
		sb.append("      <gx:FlyTo>                                       \n");
		sb.append("        <gx:duration>3.0</gx:duration>                 \n");
		sb.append("        <gx:flyToMode>smooth</gx:flyToMode>            \n");
		sb.append("        <LookAt>                                       \n");
		sb.append("          <longitude>"+extent.getCenterX()+"</longitude>\n");
		sb.append("          <latitude>"+extent.getCenterY()+"</latitude>\n");
		sb.append("          <altitude>1000</altitude>  \n");
		sb.append("          <heading>0</heading>   \n");
		sb.append("          <tilt>78.065</tilt>    \n");
		sb.append("          <range>"+widthInMeters*4+"</range> \n");
		sb.append("          <altitudeMode>relativeToGround</altitudeMode>\n");
		sb.append("        </LookAt>                                      \n");
		sb.append("      </gx:FlyTo>                                      \n");

		
		sb.append("    </gx:Playlist>\n");
		sb.append("  </gx:Tour>      \n");
		
		
		
		
		return sb.toString();
	}
	
	
	private static String getKMLStart() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>      \n");
		sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"   \n");
		sb.append(" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
		sb.append("                                              \n");
		sb.append("<Document>                                    \n");
		sb.append("  <name>A tour and no features</name>       \n");
		sb.append("  <open>1</open>                              \n");
		return sb.toString();
	}
	
	private static String getKMLEnd() {
		StringBuilder sb = new StringBuilder();
		sb.append("</Document>       \n");
		sb.append("</kml>            \n");
		return sb.toString();
	}
	
	
	private static String getExtentPolygon(Rectangle2D.Double extent) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("  <Folder> \n");
		sb.append("    <Placemark id=\"extent-polygon\">  \n");
		sb.append("      <name>Polygon</name>         \n");
		sb.append("      <Polygon>                    \n");
		sb.append("        <tessellate>1</tessellate> \n");
		sb.append("        <outerBoundaryIs>          \n");
		sb.append("          <LinearRing>             \n");
		sb.append("            <coordinates>          \n");
		sb.append("              "+extent.getMinX()+","+extent.getMaxY()+",0 \n");
		sb.append("              "+extent.getMaxX()+","+extent.getMaxY()+",0 \n");
		sb.append("              "+extent.getMaxX()+","+extent.getMinY()+",0 \n");
		sb.append("              "+extent.getMinX()+","+extent.getMinY()+",0 \n");
		sb.append("              "+extent.getMinX()+","+extent.getMaxY()+",0 \n");
		sb.append("            </coordinates>         \n");
		sb.append("          </LinearRing>            \n");
		sb.append("        </outerBoundaryIs>         \n");
		sb.append("      </Polygon>                   \n");
		sb.append("    </Placemark>                   \n");
		sb.append("  </Folder> \n");
		
		return sb.toString();
	}

}
