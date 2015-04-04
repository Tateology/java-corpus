package gov.noaa.ncdc.wct.ui.ge;

import gov.noaa.ncdc.gis.collada.ColladaCalculator;
import gov.noaa.ncdc.gis.collada.ColladaWriter;
import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geotools.ct.MathTransform;
import org.geotools.pt.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Collada utils specific to generating Radar sweep meshes in the Weather and Climate Toolkit
 * @author steve.ansari@noaa.gov
 *
 */
public class WCTColladaUtils {

	/**
	 * Returns coordinate array with x,y,z coordinates in meters relative to radar site elevation
	 * @param originLat
	 * @param originLon
	 * @param elevationAngle
	 * @param elevationExaggeration
	 * @param extent
	 * @param numSegmentsX
	 * @param numSegmentsY
	 * @return
	 * @throws FactoryException 
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
	 */
	public static Coordinate[][] generateRadialSweepCoordinateArray(
			double originLat, double originLon, double elevationAngle,  double elevationExaggeration,
			Rectangle2D.Double extent, int numSegmentsX, int numSegmentsY
			
	 	) throws FactoryException, MismatchedDimensionException, TransformException {
		
		WCTProjections wctProj = new WCTProjections();
		// instead of x,y to lat/lon, we need lat/lon to x/y
		MathTransform transform = wctProj.getRadarTransform(originLon, originLat).inverse();
		
		double elevationSin = Math.sin(Math.toRadians(elevationAngle));
        double elevationCos = Math.cos(Math.toRadians(elevationAngle));
        double elevationTan = Math.tan(Math.toRadians(elevationAngle));

//System.out.println(extent + "::: minY "+extent.getMinY());
        
		
		Coordinate[][] coords = new Coordinate[numSegmentsY+1][numSegmentsX+1];
		double[] srcPoints = new double[2];
		double[] dstPoints = new double[2];
		
		for (int j=0; j<numSegmentsY+1; j++) {
			for (int i=0; i<numSegmentsX+1; i++) {
				
				// 1. get lat/lon at this segment
				double lon = extent.getMinX() + i*(extent.getWidth()/numSegmentsX);
				double lat = extent.getMinY() + j*(extent.getHeight()/numSegmentsY);
				
				// 2. find x and y in meters using local equal area projection for radar
                srcPoints[0] = lon;
                srcPoints[1] = lat;
                transform.transform(srcPoints, 0, dstPoints, 0, 1);
				
//				// 3. find the z using radar beam propagation equation
//                double height = NexradEquations.getRelativeBeamHeight(
//                		elevationCos, elevationSin, 
//                		Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]) );
//
//                // 3. use simple triangle height, since Google Earth already takes curvature of earth into account
//                //    when displaying models?
//                System.out.print(height+"  = from beam equation, ");
//                
//                double height2 =  NexradEquations.getRelativeBeamHeight(
//                		Math.cos(Math.toRadians(0.0)), Math.sin(Math.toRadians(0.0)), 
//                		Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]) );
//                System.out.print(height2+"  = from beam equation at 0.0 deg, ");
//                
//                double height3 =  elevationTan * Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]);
//                System.out.println(height3+"  = from triangle height ; beam diff="+(height2-height));

                
                
                // Google Earth appears to keep the height of the model in a cube x/y/z meters space, not
                // a true height related to the surface of the earth.  Because of this, a simple
                // triangle height should work.  However, we still need to add the refractive effect of
                // the atmosphere.  Lets find the amount of height attributed to refraction by comparing
                // the beam propagation equation with different refractive indices.
                double height =  elevationTan * Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]);
                double refractiveHeightToAdd = 
                		NexradEquations.getRelativeBeamHeight(
                				elevationCos, elevationSin, 
                				Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]))
                		-
                		NexradEquations.getRelativeBeamHeight(
                				elevationCos, elevationSin, 
                				Math.sqrt(dstPoints[0]*dstPoints[0] + dstPoints[1]*dstPoints[1]), 1.0)
                	;
                height = height + refractiveHeightToAdd;
                
//                System.out.println("["+i+"]["+j+"]  = "+dstPoints[0]+" , "+dstPoints[1]+" , "+height + " ||| "+lat+" , "+lon);
                coords[j][i] = new Coordinate(dstPoints[0], dstPoints[1], height*elevationExaggeration);
			}
		}
		
		return coords;
	}

	
	
	
	
	
	
	
	
	
	
	

	/**
	 * Returns coordinate array with x,y,z coordinates in meters from GeoGrid height variable
	 * @param grid - variable must contain the values of the height at each grid cell in meters.
	 * @param elevationExaggeration
	 * @param extent
	 * @param numSegmentsX
	 * @param numSegmentsY
	 * @return
	 * @throws FactoryException 
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
	 */
	public static Coordinate[][] generateGridVariableHeightCoordinateArray(
			GridDatasetRemappedRaster remappedRaster,  double elevationExaggeration,
			int numSegmentsX, int numSegmentsY
			
	 	) throws FactoryException, MismatchedDimensionException, TransformException {
		

		Rectangle2D.Double extent = remappedRaster.getBounds();

		WCTProjections wctProj = new WCTProjections();
		// instead of x,y to lat/lon, we need lat/lon to x/y
		MathTransform transform = wctProj.getRadarTransform(extent.getCenterX(), extent.getCenterY()).inverse();
		
		
		Coordinate[][] coords = new Coordinate[numSegmentsY+1][numSegmentsX+1];
		double[] srcPoints = new double[2];
		double[] dstPoints = new double[2];
		
		for (int j=0; j<numSegmentsY+1; j++) {
			for (int i=0; i<numSegmentsX+1; i++) {
				
				// 1. get lat/lon at this segment
				double lon = extent.getMinX() + i*(extent.getWidth()/numSegmentsX);
				double lat = extent.getMinY() + j*(extent.getHeight()/numSegmentsY);
				
				// 2. find x and y in meters using local equal area projection for radar
                srcPoints[0] = lon;
                srcPoints[1] = lat;
                transform.transform(srcPoints, 0, dstPoints, 0, 1);
				
//                System.out.println(i*remappedRaster.getWidth()/numSegmentsX + ", "+j*remappedRaster.getHeight()/numSegmentsY +" of "+remappedRaster.getWidth()+"/"+remappedRaster.getHeight());
//                System.out.println(i*(remappedRaster.getWidth()-1)/numSegmentsX + ", "+j*(remappedRaster.getHeight()-1)/numSegmentsY +" of "+remappedRaster.getWidth()+"/"+remappedRaster.getHeight());
                
                double height = remappedRaster.getWritableRaster().getSampleDouble(
                		i*(remappedRaster.getWidth()-1)/numSegmentsX, remappedRaster.getHeight()-1-(j*(remappedRaster.getHeight()-1)/numSegmentsY), 0) ;
                
                // now adjust for curvature of the earth
                double range = Math.sqrt(dstPoints[0]*dstPoints[0]+dstPoints[1]*dstPoints[1]);
                double heightAboveEarth = NexradEquations.getRelativeBeamHeight(Math.cos(0), Math.sin(0), range, 1.0);
                
                if (height > 0) {
                	System.out.println(height + " , "+heightAboveEarth + " "+(height*elevationExaggeration-heightAboveEarth));
                }
                
//                System.out.println("["+i+"]["+j+"]  = "+dstPoints[0]+" , "+dstPoints[1]+" , "+height + " ||| "+lat+" , "+lon);
                coords[j][i] = new Coordinate(dstPoints[0], dstPoints[1], height*elevationExaggeration-heightAboveEarth);
			}
		}
		
		return coords;
	}
	

	
	
	
	
	
	
	
	
	public static String getColladaDAE(WCTViewer viewer, String imageFilename, int elevExaggeration) 
		throws MismatchedDimensionException, FactoryException, TransformException, WCTException {
		
//		double lat = RadarHashtables.getSharedInstance().getLat(viewer.getNexradHeader().getICAO());
//		double lon = RadarHashtables.getSharedInstance().getLon(viewer.getNexradHeader().getICAO());
		double lat = viewer.getNexradHeader().getLat();
		double lon = viewer.getNexradHeader().getLon();
		Rectangle2D.Double extent = viewer.getCurrentExtent();
		double elevAngle = viewer.getLastDecodedRadarElevationAngle();
		
		return getColladaDAE(lat, lon, elevAngle, extent, imageFilename, elevExaggeration);
	}
	

	public static String getColladaDAE(double lat, double lon, double elevAngle, 
			Rectangle2D.Double extent, String imageFilename, int elevExaggeration) 
		throws MismatchedDimensionException, FactoryException, TransformException, WCTException {
		// TODO - use generic lat/lon lookup from nexradutilities.java
		

		Coordinate[][] coords = WCTColladaUtils.generateRadialSweepCoordinateArray(lat, lon, elevAngle, elevExaggeration, extent, 30, 30);
		
		ColladaCalculator collada = new ColladaCalculator();
		collada.loadData(coords);
		
		ColladaWriter writer = new ColladaWriter();
		String xml = writer.createColladaXML(collada, imageFilename);
		return xml;
	}
	
	public static void writeColladaDAE(WCTViewer viewer, String imageFilename, int elevExaggeration, File daeFile) 
		throws MismatchedDimensionException, FactoryException, TransformException, IOException, WCTException {
		
		String xml = getColladaDAE(viewer, imageFilename, elevExaggeration);
		
//		System.out.println(xml);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(daeFile));
		bw.write(xml);
		bw.close();

	}
	
	
	public static void writeColladaKML(WCTViewer viewer, String daeFilename, File kmlFile, 
			String title, String desc) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<kml xmlns=\"http://earth.google.com/kml/2.2\"> \n");
		sb.append("<Document> \n");
		sb.append("		<name>Collada Snapshot</name> \n");
		sb.append("		<visibility>1</visibility> \n");
		sb.append("		<open>1</open> \n");
		sb.append("		<description><![CDATA["+desc+"]]></description> \n");
		sb.append("		<LookAt> \n");
		sb.append("  		<altitudeMode>absolute</altitudeMode> \n");
		sb.append("  		<longitude>"+viewer.getCurrentExtent().getCenterX()+"</longitude> \n");
		sb.append("  		<latitude>"+viewer.getCurrentExtent().getCenterY()+"</latitude> \n");
		sb.append(" 		<altitude>0</altitude> \n");
		sb.append("  		<range>700000</range> \n");
		sb.append("  		<tilt>0</tilt> \n");
		sb.append("  		<heading>0</heading> \n");
		sb.append(" 	</LookAt> \n");
		
		sb.append(" 	<Placemark> \n");
		sb.append(" 	<name>"+title+"</name> \n");
		sb.append(" 	<visibility>1</visibility> \n");
		sb.append(" 	<Style id=\"default\"></Style> \n");
		sb.append(" 	<LookAt> \n");
		sb.append(" 		<altitudeMode>absolute</altitudeMode> \n");
		sb.append("  		<longitude>"+viewer.getCurrentExtent().getCenterX()+"</longitude> \n");
		sb.append("  		<latitude>"+viewer.getCurrentExtent().getCenterY()+"</latitude> \n");
		sb.append(" 		<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> \n");
		sb.append(" 	    <range>300000</range> \n");
		sb.append(" 	    <tilt>65</tilt> \n");
		sb.append(" 	    <heading>0</heading> \n");
		sb.append(" 	</LookAt> \n");
		sb.append(" 	<Model id=\"model_1\"> \n");
		sb.append(" 		<altitudeMode>absolute</altitudeMode> \n");
		sb.append(" 		<Location> \n");
		sb.append("  			<longitude>"+viewer.getNexradHeader().getLon()+"</longitude> \n");
		sb.append("  			<latitude>"+viewer.getNexradHeader().getLat()+"</latitude> \n");
//		sb.append("  			<longitude>"+viewer.getCurrentExtent().getCenterX()+"</longitude> \n");
//		sb.append("  			<latitude>"+viewer.getCurrentExtent().getCenterY()+"</latitude> \n");
		sb.append(" 			<altitude>"+viewer.getNexradHeader().getAlt()/3.28083989501312+"</altitude> \n");
		sb.append(" 		</Location> \n");
		sb.append(" 		<Orientation> \n");
		sb.append(" 			<heading>0</heading> \n");
		sb.append(" 			<tilt>0</tilt> \n");
		sb.append(" 			<roll>0</roll> \n");
		sb.append(" 		</Orientation> \n");
		sb.append(" 		<Scale> \n");
		sb.append(" 			<x>1</x> \n");
		sb.append(" 			<y>1</y> \n");
		sb.append(" 			<z>1</z> \n");
		sb.append(" 		</Scale> \n");
		sb.append(" 		<Link> \n");
		sb.append(" 			<href>"+daeFilename+"</href> \n");
		sb.append(" 		</Link> \n");
		sb.append(" 	</Model> \n");
		sb.append(" </Placemark> \n");

		sb.append("</Document> \n");
		sb.append("</kml> \n");

		
		String xml = sb.toString();		
		System.out.println(xml);
		BufferedWriter bw = new BufferedWriter(new FileWriter(kmlFile));
		bw.write(xml);
		bw.close();
		
		
	}
	
	
	
	public static void main(String[] args) {
		try {
			double lat = RadarHashtables.getSharedInstance().getLat("KGSP");
			double lon = RadarHashtables.getSharedInstance().getLon("KGSP");
			Rectangle2D.Double extent = MaxGeographicExtent.getNexradExtent(lat, lon);
			Coordinate[][] coords = generateRadialSweepCoordinateArray(lat, lon, 12.5, 1.0, extent, 50, 50);
			
			ColladaCalculator collada = new ColladaCalculator();
			collada.loadData(coords);
			
			ColladaWriter writer = new ColladaWriter();
			String xml = writer.createColladaXML(collada, null);
			
			System.out.println(xml);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("H:\\Nexrad_Viewer_Test\\Dickson\\Maya\\kmrx\\files\\radartest2.dae")));
			bw.write(xml);
			bw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
